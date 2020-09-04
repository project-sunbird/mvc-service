package managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.Platform;
import org.sunbird.search.util.SearchConstants;

import java.util.HashMap;
import java.util.Map;
public class GetContentMetadata {
    static Logger logger = LoggerFactory.getLogger(GetContentMetadata.class);
    public static boolean validateSourceURL(String sourceurl) {
        String respcode = "";
        try {
            respcode = Postman.GET(sourceurl).get("statuscode").toString();
            // check if source url is valid or not
            if (respcode.equals("200")) {
                return true;
            }
        }
        catch (Exception e) {
            logger.info("GetContentMetadata :: ValidateSourceUrl ::: Exception is " + e + "\n Source url is " + sourceurl);
        }

        return false;
    }
    public static void getMetadata(Map<String,Object> contentobj, String sourceurl,boolean flagforMVC) {
        String contentreadurl = Platform.config.getString("sunbird_content_url"), contentreadapi = "/content/v3/read/";
        if(flagforMVC) {
            contentreadurl = SearchConstants.dikshaurl;
            contentreadapi = SearchConstants.contentreadapi;
        }
        String resp = "", contentId = "";
        try {
                contentId = sourceurl.substring(sourceurl.lastIndexOf('/') + 1);
                resp = Postman.GET(contentreadurl + contentreadapi + contentId).get("response").toString();
                Map<String,Object> respobj = JsonUtils.deserialize(resp,Map.class);
                Map<String,Object> result = (HashMap<String,Object>) respobj.get("result");

                Map<String,Object> content = (HashMap<String,Object>) result.get("content");
                // club metadata of diksha and from json
                content.putAll(contentobj);
               EventObjectProducer.addToEventObj(content, contentId);

        }
        catch(Exception e)
        {
            logger.info("GetContentMetadata :: getMetadata ::: Exception is " + e + "\n ContentObject is " + contentobj.toString());
        }
     }

   }
