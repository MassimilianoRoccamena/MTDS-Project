#include "contiki.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/sicslowpan.h"
#include "rpl.h"
#include "simple-udp.h"
#include "mqtt.h"
#include "sys/etimer.h"
#include "leds.h"

#include "sys/log.h"
#define LOG_MODULE 	"PERSONAL_DEVICE"
#define LOG_LEVEL 	LOG_LEVEL_INFO

#include <string.h>

/*===========================================================================*/

//#define UIP_CONF_TCP 				1
#define MAX_TCP_SEGMENT_SIZE    		32
//#define IEEE802154_CONF_DEFAULT_CHANNEL      	21
/*---------------------------------------------------------------------------*/
#define BROKER_IP_ADDR		   	"fd00::1"
#define DEFAULT_ORG_ID             	"contacts-org"
#define BROADCAST_PORT 		   	1234
/*---------------------------------------------------------------------------*/
#define RETRY_FOREVER              0xFF
#define RECONNECT_INTERVAL         (CLOCK_SECOND * 2)
#define RECONNECT_ATTEMPTS         RETRY_FOREVER
#define CONNECTION_STABLE_TIME     (CLOCK_SECOND * 5)
#define NET_CONNECT_PERIODIC       (CLOCK_SECOND >> 2)
/*---------------------------------------------------------------------------*/
#define STATE_INIT            0
#define STATE_REGISTERED      1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_PUBLISHING      4
#define STATE_DISCONNECTED    5
#define STATE_NEWCONFIG       6
#define STATE_CONFIG_ERROR    0xFE
#define STATE_ERROR           0xFF
/*---------------------------------------------------------------------------*/
#define CONFIG_ORG_ID_LEN        32
#define CONFIG_TYPE_ID_LEN       32
#define CONFIG_AUTH_TOKEN_LEN    32
#define CONFIG_CMD_TYPE_LEN      8
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
#define DEFAULT_TYPE_ID             "native"
#define DEFAULT_AUTH_TOKEN          "AUTHZ"
#define DEFAULT_SUBSCRIBE_CMD_TYPE  "+"
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (60 * CLOCK_SECOND)
#define DEFAULT_KEEP_ALIVE_TIMER    60
/*---------------------------------------------------------------------------*/
#define BROKER_PROCESS_T            (CLOCK_SECOND >> 1)
#define BROADCAST_PROCESS_T         (CLOCK_SECOND >> 1)
/*---------------------------------------------------------------------------*/
#define PUB_TOPIC   		"iot/event/produce"
#define SUB_TOPIC       	"iot/event/consume"
/*---------------------------------------------------------------------------*/
#define BUFFER_SIZE 64
#define APP_BUFFER_SIZE 512

/*===========================================================================*/

PROCESS(broker_process, "Backend communication process");
PROCESS(broadcast_process, "Contacts detection process");
AUTOSTART_PROCESSES(&broker_process, &broadcast_process);

/*===========================================================================*/

typedef struct mqtt_client_config {
  char org_id[CONFIG_ORG_ID_LEN];
  char type_id[CONFIG_TYPE_ID_LEN];
  char auth_token[CONFIG_AUTH_TOKEN_LEN];
  char broker_ip[CONFIG_IP_ADDR_STR_LEN];
  char cmd_type[CONFIG_CMD_TYPE_LEN];
  clock_time_t pub_interval;
  uint16_t broker_port;
} mqtt_client_config_t;
static mqtt_client_config_t conf;
/*---------------------------------------------------------------------------*/
static struct mqtt_connection broker_connection;
static struct simple_udp_connection broadcast_connection;
/*---------------------------------------------------------------------------*/
static uint8_t state;
/*---------------------------------------------------------------------------*/
static char my_id[BUFFER_SIZE];
static char other_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct timer connection_life;
static uint8_t connect_attempt;
static struct etimer broker_process_timer;
static struct etimer broadcast_process_timer;
/*---------------------------------------------------------------------------*/
static void on_broker_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    LOG_INFO("Connected\n");
    timer_set(&connection_life, CONNECTION_STABLE_TIME);
    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    LOG_INFO("Disconnected: reason %u\n", *((mqtt_event_t *)data));
    state = STATE_DISCONNECTED;
    process_poll(&broker_process);
    break;
  }
  case MQTT_EVENT_PUBLISH: {						// <=== Consumer handler
    LOG_INFO("EVENT OF INTEREST RECEIVED! DANGER!\n");
    break;
  }
  case MQTT_EVENT_SUBACK: {
    LOG_INFO("Started listening for events of interest\n");
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    LOG_INFO("Stopped listening for events of interest\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    LOG_INFO("Event of interest sent\n");
    break;
  }
  default:
    LOG_WARN("Unhadled broker event: %i\n", event);
    break;
  }
}
/*---------------------------------------------------------------------------*/
static int build_pub_topic(void)
{
  int len = snprintf(pub_topic, BUFFER_SIZE, PUB_TOPIC);
  if(len < 0 || len >= BUFFER_SIZE) {
    LOG_ERR("Pub topic: %d, buffer %d\n", len, BUFFER_SIZE);
    return 0;
  }
  return 1;
}
/*---------------------------------------------------------------------------*/
static int build_sub_topic(void)
{
  int len = snprintf(sub_topic, BUFFER_SIZE, SUB_TOPIC);
  if(len < 0 || len >= BUFFER_SIZE) {
    LOG_INFO("Sub topic: %d, buffer %d\n", len, BUFFER_SIZE);
    return 0;
  }
  return 1;
}
/*---------------------------------------------------------------------------*/
static int build_my_id(void)
{
  int len = snprintf(my_id, BUFFER_SIZE, "d:%s:%s:%02x%02x%02x%02x%02x%02x",
                     conf.org_id, conf.type_id,
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);
  if(len < 0 || len >= BUFFER_SIZE) {
    LOG_INFO("Client ID: %d, buffer %d\n", len, BUFFER_SIZE);
    return 0;
  }
  memcpy(app_buffer, my_id, BUFFER_SIZE);
  return 1;
}
/*---------------------------------------------------------------------------*/
static void update_config(void)
{
  if(build_my_id() == 0) {
    state = STATE_CONFIG_ERROR;
    return;
  }
  if(build_sub_topic() == 0) {
    state = STATE_CONFIG_ERROR;
    return;
  }
  if(build_pub_topic() == 0) {
    state = STATE_CONFIG_ERROR;
    return;
  }
  state = STATE_INIT;
  etimer_set(&broker_process_timer, 0);
  return;
}
/*---------------------------------------------------------------------------*/
static void init_config()
{
  memset(&conf, 0, sizeof(mqtt_client_config_t));
  memcpy(conf.org_id, DEFAULT_ORG_ID, strlen(DEFAULT_ORG_ID));
  memcpy(conf.type_id, DEFAULT_TYPE_ID, strlen(DEFAULT_TYPE_ID));
  memcpy(conf.auth_token, DEFAULT_AUTH_TOKEN, strlen(DEFAULT_AUTH_TOKEN));
  memcpy(conf.broker_ip, BROKER_IP_ADDR, strlen(BROKER_IP_ADDR));
  memcpy(conf.cmd_type, DEFAULT_SUBSCRIBE_CMD_TYPE, 1);
  conf.broker_port = DEFAULT_BROKER_PORT;
  conf.pub_interval = DEFAULT_PUBLISH_INTERVAL;
}
/*---------------------------------------------------------------------------*/
static void subscribe(void)
{
  mqtt_status_t status;

  status = mqtt_subscribe(&broker_connection, NULL, sub_topic, MQTT_QOS_LEVEL_0); // <===== QoS

  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
    LOG_INFO("Tried to subscribe but command queue was full\n");
  }
  LOG_INFO("Subscribed\n");
}
/*---------------------------------------------------------------------------*/
static void publish(void)
{
  memcpy(app_buffer+sizeof(char)*BUFFER_SIZE, other_id, BUFFER_SIZE);

  mqtt_publish(&broker_connection, NULL,
		pub_topic, (uint8_t *)app_buffer,
               	strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

  LOG_INFO("Published\n");
}
/*---------------------------------------------------------------------------*/
static void connect_to_broker(void)
{
  mqtt_connect(&broker_connection, conf.broker_ip, conf.broker_port, conf.pub_interval * 3);
  state = STATE_CONNECTING;
}
/*---------------------------------------------------------------------------*/
static void update_broker_process(void)
{
  switch(state) {
  case STATE_INIT:
    mqtt_register(&broker_connection, &broker_process, my_id, on_broker_event, MAX_TCP_SEGMENT_SIZE);

    mqtt_set_username_password(&broker_connection, "use-token-auth", conf.auth_token);

    broker_connection.auto_reconnect = 0;
    connect_attempt = 1;

    state = STATE_REGISTERED;
    LOG_INFO("Init\n");

  case STATE_REGISTERED:
    if(uip_ds6_get_global(ADDR_PREFERRED) != NULL) {
      LOG_INFO("Joined network, connect attempt %u\n", connect_attempt);
      connect_to_broker();
    }
    etimer_set(&broker_process_timer, NET_CONNECT_PERIODIC);
    return;

  case STATE_CONNECTING:
    LOG_INFO("Connecting: retry %u...\n", connect_attempt);
    break;

  case STATE_CONNECTED:

  case STATE_PUBLISHING:
    if(timer_expired(&connection_life)) {
      connect_attempt = 0;
    }

    if(mqtt_ready(&broker_connection) && broker_connection.out_buffer_sent) {
      if(state == STATE_CONNECTED) {
        subscribe();
        state = STATE_PUBLISHING;
      } else {
        publish();
      }
      etimer_set(&broker_process_timer, conf.pub_interval);

      LOG_INFO("Publishing\n");
      return;
    }
    else {
      LOG_INFO("Publishing... (MQTT state=%d, q=%u)\n", broker_connection.state, broker_connection.out_queue_full);
    }
    break;

  case STATE_DISCONNECTED:
    LOG_INFO("Disconnected\n");
    if(connect_attempt < RECONNECT_ATTEMPTS || RECONNECT_ATTEMPTS == RETRY_FOREVER) {
      clock_time_t interval;
      mqtt_disconnect(&broker_connection);
      connect_attempt++;

      interval = connect_attempt < 3 ? 	RECONNECT_INTERVAL << connect_attempt
					: RECONNECT_INTERVAL << 3;

      LOG_INFO("Disconnected: attempt %u in %lu ticks\n", connect_attempt, interval);

      etimer_set(&broker_process_timer, interval);

      state = STATE_REGISTERED;
      return;
    } else {
      state = STATE_ERROR;
      LOG_ERR("Aborting connection after %u attempts\n", connect_attempt - 1);
    }
    break;

  case STATE_CONFIG_ERROR:
    LOG_ERR("Bad configuration.\n");
    return;

  case STATE_ERROR:

  default:
    LOG_WARN("Unhandled process state: 0x%02x\n", state);
    return;
  }

  etimer_set(&broker_process_timer, BROKER_PROCESS_T);
}
/*---------------------------------------------------------------------------*/
static void on_broadcast_receive(struct simple_udp_connection *c,
         			const uip_ipaddr_t *sender_addr,
         			uint16_t sender_port,
         			const uip_ipaddr_t *receiver_addr,
         			uint16_t receiver_port,
         			const uint8_t *data,
         			uint16_t datalen) {
  
  LOG_INFO("Detected contact with ");
  LOG_INFO_6ADDR(sender_addr);
  LOG_INFO("\n");

  memcpy(other_id, data, datalen);
}

/*===========================================================================*/

PROCESS_THREAD(broker_process, ev, data)
{
  PROCESS_BEGIN();

  LOG_INFO("Started broker process");

  init_config();
  update_config();

  while(1) {

    PROCESS_YIELD();

    if (ev == PROCESS_EVENT_TIMER && data == &broker_process_timer) {
      update_broker_process();
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(broadcast_process, ev, data)
{
  uip_ipaddr_t addr;
  uip_create_linklocal_allnodes_mcast(&addr);

  PROCESS_BEGIN();

  LOG_INFO("Started broker process");
  
  simple_udp_register(&broadcast_connection, BROADCAST_PORT, NULL, BROADCAST_PORT, on_broadcast_receive);
  etimer_set(&broadcast_process_timer, BROADCAST_PROCESS_T);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&broadcast_process_timer));
    etimer_reset(&broadcast_process_timer);
    
    LOG_INFO("Sending broadcast");

    simple_udp_sendto(&broadcast_connection, my_id, sizeof(my_id), &addr);
  }

  PROCESS_END();
}
