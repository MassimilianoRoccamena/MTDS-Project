#include "contiki.h"
#include "lib/random.h"
#include "sys/ctimer.h"
#include "sys/etimer.h"
#include "net/ipv6/uip-ds6.h"

#include "simple-udp.h"

#include <stdio.h>
#include <string.h>

#include "sys/log.h"
#define LOG_MODULE 		"App"
#define LOG_LEVEL 		LOG_LEVEL_INFO

#define UDP_PORT 		1234
#define CONTACTS_INTERVAL	(5 * CLOCK_SECOND)
#define VERBOSE_LEVEL		1

static struct simple_udp_connection contacts_connection;

/*---------------------------------------------------------------------------*/
PROCESS(contacts_process, "Contacts detection process");
AUTOSTART_PROCESSES(&contacts_process);
/*---------------------------------------------------------------------------*/
static void contacts_receiver(struct simple_udp_connection *c,
         			const uip_ipaddr_t *sender_addr,
         			uint16_t sender_port,
         			const uip_ipaddr_t *receiver_addr,
         			uint16_t receiver_port,
         			const uint8_t *data,
         			uint16_t datalen)
{
  if (VERBOSE_LEVEL > 0) {
    LOG_INFO("Detected contact with ");
    LOG_INFO_6ADDR(sender_addr);
    LOG_INFO_("\n");
  }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(contacts_process, ev, data)
{
  static struct etimer contacts_timer;

  uip_ipaddr_t addr;
  uip_create_linklocal_allnodes_mcast(&addr);

  PROCESS_BEGIN();
  
  simple_udp_register(&contacts_connection, UDP_PORT, NULL, UDP_PORT, contacts_receiver);
  etimer_set(&contacts_timer, CONTACTS_INTERVAL);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&contacts_timer));
    etimer_reset(&contacts_timer);
    
    if (VERBOSE_LEVEL > 1)
      LOG_INFO("Sending broadcast");

    simple_udp_sendto(&contacts_connection, "", sizeof(""), &addr);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
