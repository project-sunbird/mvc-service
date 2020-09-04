package managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.Platform;
import org.sunbird.search.util.SearchConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorListManager {
    String text = "";
    private Logger logger = LoggerFactory.getLogger(EventProducer.class);

    public  Map<String,Object> setVectorList(String strRequest) {
        try {

            Map<String, Object> strReq = JsonUtils.deserialize(strRequest, Map.class);
            Map<String,Object> originalFIlter = (Map<String,Object>)strReq.get("filters");
            logger.info("Received request for vector mode " + originalFIlter);
            List<String> level1 = null, level2 = null, level3 = null;
            if (originalFIlter.get("level1Name") != null) {
                level1 = (List<String>)originalFIlter.get("level1Name");
                logger.info("Level 1 name " + level1);
                text =  iterateList(level1);
                  originalFIlter.remove("level1Name");
            }
            if (originalFIlter.get("level2Name") != null) {
                level2 = (List<String>)originalFIlter.get("level2Name");
                logger.info("Level 2 name " + level2);
                text =  iterateList(level2);
                 originalFIlter.remove("level2Name");
            }
            if (originalFIlter.get("level3Name") != null) {
                level3 = (List<String>)originalFIlter.get("level3Name");
                logger.info("Level 3 name " + level2);
                text =  iterateList(level3);
                 originalFIlter.remove("level3Name");
            }
            text = text.trim();
            if(text.equals("")) {
                return null;
            }
            Map<String,Object> obj = JsonUtils.deserialize(SearchConstants.mlvectorListRequest,Map.class);
            Map<String,Object> req = ((HashMap<String,Object>) (obj.get("request")));
            ArrayList<Object> text1 = (ArrayList<Object>) req.get("text");
            text1.add(text);
            String vectorurl = Platform.config.hasPath("ml_vector_api") ? Platform.config.getString("ml_vector_api") + ":1729/ml/vector/search": "";
            vectorurl = vectorurl.trim();
            logger.info("Text on which vector will be generated " + text1);
            logger.info("Making vector api call " +  vectorurl );
            Map<String,Object> respobj = JsonUtils.deserialize(Postman.POST(JsonUtils.serialize(obj),"http://"+vectorurl),Map.class);
            logger.info("Response received from vector search api " + respobj.toString());
            Map<String,Object> result = (HashMap<String,Object>) respobj.get("result");
            ArrayList<Object> contentTextVectorList = result.get("vector") != null ? (ArrayList<Object>) result.get("vector") : null;
            if (contentTextVectorList != null) {
                logger.info("Content text vector received");
                contentTextVectorList = (ArrayList<Object>) contentTextVectorList.get(0);
                ArrayList<Double> vector= new ArrayList<Double>();
                for(int i = 0 ; i < contentTextVectorList.size() ; i++) {
                    vector.add((Double) contentTextVectorList.get(i));
                }
                strReq.put("query_vector", vector);
            }
            logger.info("New request " + strReq);
            return strReq;
        }
        catch (Exception e) {
            logger.info("Exception in vector request " + e);
            return null;
        }
    }

    private  String iterateList(List<String> chapterevelNmae) {

        for(int i = 0 ; i < chapterevelNmae.size() ; i++) {
            text = text + chapterevelNmae.get(i) + " ";
        }
        return text;
    }
}
