#include "contiki.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/sicslowpan.h"
#include "rpl.h"
#include "simple-udp.h"
#include "mqtt.h"
#include "sys/etimer.h"
#include "leds.h"

#include "sys/log.h"
#define LOG_MODULE 	"ClientTriggerer"
#define LOG_LEVEL 	LOG_LEVEL_INFO

#include <string.h>

/*===========================================================================*/

#define MAX_TCP_SEGMENT_SIZE    	32
/*---------------------------------------------------------------------------*/
#define BROKER_PORT         		1883
#define BROADCAST_PORT 		   	1234
/*---------------------------------------------------------------------------*/
#define RETRY_FOREVER              	0xFF
#define RECONNECT_INTERVAL         	(CLOCK_SECOND * 2)
#define RECONNECT_ATTEMPTS         	RETRY_FOREVER
#define CONNECTION_STABLE_TIME     	(CLOCK_SECOND * 5)
#define NET_CONNECT_PERIODIC       	(CLOCK_SECOND >> 2)
/*---------------------------------------------------------------------------*/
#define STATE_INIT            		0
#define STATE_REGISTERED      		1
#define STATE_CONNECTING      		2
#define STATE_CONNECTED       		3
#define STATE_READY      		4
#define STATE_DISCONNECTED    		5
#define STATE_NEWCONFIG       		6
#define STATE_CONFIG_ERROR    		0xFE
#define STATE_ERROR           		0xFF
/*---------------------------------------------------------------------------*/
#define CONFIG_AUTH_TOKEN_LEN    	32
#define CONFIG_CMD_TYPE_LEN      	8
#define CONFIG_IP_ADDR_STR_LEN  	64
/*---------------------------------------------------------------------------*/
#define DEFAULT_AUTH_TOKEN          	"AUTHZ"
#define DEFAULT_SUBSCRIBE_CMD_TYPE  	"+"
/*---------------------------------------------------------------------------*/
#define CONTACTS_QUEUE_SIZE 		4
/*---------------------------------------------------------------------------*/
#define BROKER_PROCESS_T            (CLOCK_SECOND * 2)
#define BROADCAST_PROCESS_T         (BROKER_PROCESS_T / CONTACTS_QUEUE_SIZE)
#define SIGNAL_PROCESS_T            (CLOCK_SECOND * 10)
/*---------------------------------------------------------------------------*/
#define CONTACT_PUB_TOPIC   	"iot/contact/produce"
#define EVENT_PUB_TOPIC   	"iot/event/produce"
#define EVENT_SUB_TOPIC       	"iot/event/consume"
/*---------------------------------------------------------------------------*/
#define SHORT_SIZE 		150
#define LONG_SIZE 		300

/*===========================================================================*/

PROCESS(broker_process, "Backend communication process");
PROCESS(broadcast_process, "Contacts detection process");
PROCESS(signal_process, "Events triggering process");
AUTOSTART_PROCESSES(&broker_process, &broadcast_process, &signal_process);

/*===========================================================================*/

typedef struct mqtt_client_config {
  char auth_token[CONFIG_AUTH_TOKEN_LEN];
  char broker_ip[CONFIG_IP_ADDR_STR_LEN];
  char cmd_type[CONFIG_CMD_TYPE_LEN];
  uint16_t broker_port;
} mqtt_client_config_t;
static mqtt_client_config_t conf;
/*---------------------------------------------------------------------------*/
static struct mqtt_connection broker_connection;
static struct simple_udp_connection broadcast_connection;
/*---------------------------------------------------------------------------*/
static struct timer connection_life;
static uint8_t connect_attempt;

static struct etimer broker_process_timer;
static struct etimer broadcast_process_timer;
static struct etimer signal_process_timer;
/*---------------------------------------------------------------------------*/
static uint8_t state;
/*---------------------------------------------------------------------------*/
static char contact_pub_topic[SHORT_SIZE];
static char event_pub_topic[SHORT_SIZE];
static char event_sub_topic[SHORT_SIZE];
/*---------------------------------------------------------------------------*/
static char my_id[SHORT_SIZE];
static char others_ids[CONTACTS_QUEUE_SIZE][SHORT_SIZE];
static uint8_t others_count = 0;

static char contact_msg[LONG_SIZE];
static char event_msg[SHORT_SIZE];

static uint8_t signal_event = 0;

/*===========================================================================*/

static void event_of_interest(void)
{
  LOG_INFO("EVENT OF INTEREST RECEIVED!\n");
}

static void mqtt_receiver(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    LOG_INFO("MQTT: connected\n");
    timer_set(&connection_life, CONNECTION_STABLE_TIME);
    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    LOG_INFO("MQTT: disconnected, reason %u\n", *((mqtt_event_t *)data));
    state = STATE_DISCONNECTED;
    process_poll(&broker_process);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    LOG_INFO("MQTT: message received\n");
    event_of_interest();
    break;
  }
  case MQTT_EVENT_SUBACK: {
    LOG_INFO("MQTT: started listening\n");
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    LOG_INFO("MQTT: stopped listening\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    LOG_INFO("MQTT: message sent\n");
    break;
  }
  default:
    LOG_WARN("MQTT: unhandled event %i\n", event);
    break;
  }
}
/*---------------------------------------------------------------------------*/
static int build_my_id(void)
{
  int len = snprintf(my_id, SHORT_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);
  if(len < 0 || len >= SHORT_SIZE) {
    LOG_ERR("Client ID: %d, buffer %d\n", len, SHORT_SIZE);
    return 0;
  }
  LOG_INFO("My ID is %s\n", my_id);
  return 1;
}

static int build_sub_topic(void)
{
  int len = snprintf(event_sub_topic, SHORT_SIZE, EVENT_SUB_TOPIC);
  if(len < 0 || len >= SHORT_SIZE) {
    LOG_ERR("Sub topic: %d, buffer %d\n", len, SHORT_SIZE);
    return 0;
  }
  strcat(event_sub_topic, "/");
  strcat(event_sub_topic, my_id);
  return 1;
}

static int build_pub_topics(void)
{
  int len = snprintf(contact_pub_topic, SHORT_SIZE, CONTACT_PUB_TOPIC);
  if(len < 0 || len >= SHORT_SIZE) {
    LOG_ERR("Pub topic: %d, buffer %d\n", len, SHORT_SIZE);
    return 0;
  }
  len = snprintf(event_pub_topic, SHORT_SIZE, EVENT_PUB_TOPIC);
  if(len < 0 || len >= SHORT_SIZE) {
    LOG_ERR("Pub topic: %d, buffer %d\n", len, SHORT_SIZE);
    return 0;
  }
  return 1;
}
/*---------------------------------------------------------------------------*/
static void init_config(void)
{
  memset(&conf, 0, sizeof(mqtt_client_config_t));
  memcpy(conf.auth_token, DEFAULT_AUTH_TOKEN, strlen(DEFAULT_AUTH_TOKEN));
  memcpy(conf.broker_ip, BROKER_IP_ADDR, strlen(BROKER_IP_ADDR));
  memcpy(conf.cmd_type, DEFAULT_SUBSCRIBE_CMD_TYPE, 1);
  conf.broker_port = BROKER_PORT;
}

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
  if(build_pub_topics() == 0) {
    state = STATE_CONFIG_ERROR;
    return;
  }
  state = STATE_INIT;
  etimer_set(&broker_process_timer, 0);
  return;
}
/*---------------------------------------------------------------------------*/
static void subscribe(void)
{
  mqtt_status_t status;

  status = mqtt_subscribe(&broker_connection, NULL, event_sub_topic, MQTT_QOS_LEVEL_0);

  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
    LOG_WARN("Tried to listen for event of interest, but command queue was full\n");
  }
  LOG_INFO("Listening for events of interest\n");
}

static void publish(void)
{
  // Contacts
  for (int i=0; i< others_count; i++) {
    char *other_id = others_ids[i];

    strcpy(contact_msg, "{\"my_id\":\"");
    strcat(contact_msg, my_id);
    strcat(contact_msg, "\",\"other_id\":\"");
    strcat(contact_msg, other_id);
    strcat(contact_msg, "\"}");

    mqtt_publish(&broker_connection, NULL,
		 contact_pub_topic, (uint8_t*) contact_msg,
               	 strlen(contact_msg), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

    LOG_INFO("Contact %d sent\n", i+1);
  }
  others_count = 0;
  
  // Event of interest
  if (signal_event == 1) {
    strcpy(event_msg, "{\"my_id\":\"");
    strcat(event_msg, my_id);
    strcat(event_msg, "\"}");

    mqtt_publish(&broker_connection, NULL,
		 event_pub_topic, (uint8_t*) event_msg,
               	 strlen(event_msg), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

    LOG_INFO("Event of interest sent\n");
    signal_event = 0;
  }
}
/*---------------------------------------------------------------------------*/
static void update_broker_process(void)
{
  switch(state) {
  case STATE_INIT:
    mqtt_register(&broker_connection, &broker_process, my_id, mqtt_receiver, MAX_TCP_SEGMENT_SIZE);
    mqtt_set_username_password(&broker_connection, "use-token-auth", conf.auth_token);

    broker_connection.auto_reconnect = 0;
    connect_attempt = 1;

    state = STATE_REGISTERED;
    LOG_INFO("Init broker communication\n");

  case STATE_REGISTERED:
    if(uip_ds6_get_global(ADDR_PREFERRED) != NULL) {
      LOG_INFO("Joined broker network, connect attempt %u\n", connect_attempt);
      mqtt_connect(&broker_connection, conf.broker_ip, conf.broker_port, BROKER_PROCESS_T * 3);
      state = STATE_CONNECTING;
    }
    etimer_set(&broker_process_timer, NET_CONNECT_PERIODIC);
    return;

  case STATE_CONNECTING:
    LOG_INFO("Connecting to broker, retry %u...\n", connect_attempt);
    break;

  case STATE_CONNECTED:

  case STATE_READY:
    if(timer_expired(&connection_life)) {
      connect_attempt = 0;
    }

    if(mqtt_ready(&broker_connection) && broker_connection.out_buffer_sent) {
      if(state == STATE_CONNECTED) {
        subscribe();
        state = STATE_READY;
      } else {
        publish();
      }
    }
    else {
      LOG_INFO("Sending to broker... (MQTT state=%d, q=%u)\n", broker_connection.state, broker_connection.out_queue_full);
    }
    break;

  case STATE_DISCONNECTED:
    LOG_INFO("Disconnected from broker\n");
    if(connect_attempt < RECONNECT_ATTEMPTS || RECONNECT_ATTEMPTS == RETRY_FOREVER) {
      clock_time_t interval;
      mqtt_disconnect(&broker_connection);
      connect_attempt++;

      interval = connect_attempt < 3 ? 	RECONNECT_INTERVAL << connect_attempt
					: RECONNECT_INTERVAL << 3;

      LOG_INFO("Disconnected from broker, attempt %u in %lu ticks\n", connect_attempt, interval);

      etimer_set(&broker_process_timer, interval);

      state = STATE_REGISTERED;
      return;
    } else {
      state = STATE_ERROR;
      LOG_ERR("Aborting broker connection after %u attempts\n", connect_attempt - 1);
    }
    break;

  case STATE_CONFIG_ERROR:
    LOG_ERR("Bad broker connection configuration\n");
    return;

  case STATE_ERROR:

  default:
    LOG_WARN("Unhandled broker process state: 0x%02x\n", state);
    return;
  }

  etimer_set(&broker_process_timer, BROKER_PROCESS_T);
}
/*---------------------------------------------------------------------------*/
static void udp_receiver(struct simple_udp_connection *c,
         			const uip_ipaddr_t *sender_addr,
         			uint16_t sender_port,
         			const uip_ipaddr_t *receiver_addr,
         			uint16_t receiver_port,
         			const uint8_t *data,
         			uint16_t datalen) {

  char *received_id = (char*) data;
  
  LOG_INFO("Detected contact with %s", received_id);
  
  for (int i=0; i<others_count; i++) {
    if (strcmp(received_id, others_ids[i]) == 0) {
      LOG_INFO(", skipped\n");
      return;
    }
  }

  char *other_id = others_ids[others_count];
  strcpy(other_id, received_id);
  others_count = others_count + 1;

  LOG_INFO(", saved\n");
}

/*===========================================================================*/

PROCESS_THREAD(broker_process, ev, data)
{
  PROCESS_BEGIN();

  LOG_INFO("Started backend communication process\n");

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

  LOG_INFO("Started contacts detection process\n");
  
  simple_udp_register(&broadcast_connection, BROADCAST_PORT, NULL, BROADCAST_PORT, udp_receiver);
  etimer_set(&broadcast_process_timer, BROADCAST_PROCESS_T);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&broadcast_process_timer));
    etimer_reset(&broadcast_process_timer);
    
    LOG_INFO("Sending contacts broadcast\n");

    simple_udp_sendto(&broadcast_connection, my_id, sizeof(my_id), &addr);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(signal_process, ev, data)
{
  PROCESS_BEGIN();

  LOG_INFO("Started event triggering process\n");

  etimer_set(&signal_process_timer, SIGNAL_PROCESS_T);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&signal_process_timer));
    etimer_reset(&signal_process_timer);

    LOG_INFO("Triggering event of interest\n");

    signal_event = 1;
  }

  PROCESS_END();
}
