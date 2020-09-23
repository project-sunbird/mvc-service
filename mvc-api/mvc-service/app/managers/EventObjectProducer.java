package managers;

import org.sunbird.common.JsonUtils;
import org.sunbird.common.Platform;
import org.sunbird.search.util.SearchConstants;
import java.util.Map;
import java.util.UUID;

public class EventObjectProducer {
    private static String processID;
        public static void addToEventObj(Map<String,Object> content, String contentId) throws Exception  {

                Map<String,Object> eventObj = JsonUtils.deserialize(SearchConstants.autocreatejobevent, Map.class);
                final long timeinmillisecond = System.currentTimeMillis();
                eventObj.put("ets", timeinmillisecond);
                UUID uniqueKey = UUID.randomUUID();
                String mid = "VD." + timeinmillisecond + "." + uniqueKey;
                eventObj.put("mid", mid);
                Map<String,Object> object = (Map<String,Object>) eventObj.get("object");
                object.put("id", contentId);
                Map<String,Object> context = (Map<String,Object>) eventObj.get("context");
                context.put("channel", content.get("channel").toString());
                Map<String,Object> edata = (Map<String,Object>) eventObj.get("edata");
                edata.put("repository",  Platform.config.getString("sunbird_mvc_base_url") + "/api/content/v1/read/" + contentId);
                   if(content.containsKey("concepts")) {
                    content.remove("concepts");
                   }
                 if(content.containsKey("questions")) {
                    content.remove("questions");
                  }
                 content.put("processId",getContentProcessId());
                edata.put("metadata", content);
                EventProducer.writeToKafka(JsonUtils.serialize(eventObj), Platform.config.getString("kafka.topics.instruction"));

        }
      public void setContentProcessID(String id) {
          this.processID = id;
        }
        public static String getContentProcessId(){
               return processID;
        }

}
