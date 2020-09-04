package managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.kafka.client.KafkaClient;
public class EventProducer {
   static Logger logger = LoggerFactory.getLogger(EventProducer.class);
   static KafkaClient kafkaclientobj = new KafkaClient();
    public static void writeToKafka(String event,String topic) {
        try {
            logger.info(" an event to kafka topic "  + "\nEvent is" + event);
            kafkaclientobj.send(event,topic);
        }
        catch (Exception e) {
             logger.info("Failure while sending an event to kafka topic " + e + "\nEvent is" + event);
        }
    }
}
