package org.sunbird.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestKit;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.sunbird.common.JsonUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.search.client.ElasticSearchUtil;
import org.sunbird.search.util.SearchConstants;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SearchBaseActorTest {

    protected static ActorSystem system = null;
    protected static final String SEARCH_ACTOR = "SearchActor";
    private static String[] keywords = new String[]{"hindi story", "NCERT", "Pratham", "एकस्टेप", "हिन्दी", "हाथी और भालू", "worksheet", "test"};
    private static String[] contentTypes = new String[]{"Resource", "Collection", "Asset"};
    private static String[] ageGroup = new String[]{"<5","5-6", "6-7", "7-8","8-10",">10","Other"};
    private static String[] gradeLevel = new String[]{"Kindergarten","Class 1", "Class 2", "Class 3", "Class 4","Class 5","Other"};
    
    @BeforeClass
    public static void beforeTest() throws Exception {
        system = ActorSystem.create();
        
    }


    protected Request getSearchRequest() {
        Request request = new Request();
        return setSearchContext(request, SEARCH_ACTOR , "INDEX_SEARCH");
    }

    protected Request getCountRequest() {
        Request request = new Request();
        return setSearchContext(request, SEARCH_ACTOR , "COUNT");
    }

    protected Request getMetricsRequest() {
        Request request = new Request();
        return setSearchContext(request, SEARCH_ACTOR , "METRICS");
    }

    protected Request getGroupSearchResultsRequest() {
        Request request = new Request();
        return setSearchContext(request, SEARCH_ACTOR , "GROUP_SEARCH_RESULT_BY_OBJECTTYPE");
    }

    protected Request setSearchContext(Request request, String manager, String operation) {
        request.setOperation(operation);
        return request;
    }
    
    protected Response getResponse(Request request, Props props){
        try{
            ActorRef searchMgr = system.actorOf(props);
            Future<Object> future = Patterns.ask(searchMgr, request, 30000);
            Object obj = Await.result(future, Duration.create(30.0, TimeUnit.SECONDS));
            Response response = null;
            if (obj instanceof Response) {
                response = (Response) obj;
            } else {
                response = ERROR(SearchConstants.SYSTEM_ERROR, "System Error", ResponseCode.SERVER_ERROR);
            }
            return response;
        } catch (Exception e) {
            throw new ServerException(SearchConstants.SYSTEM_ERROR, e.getMessage(), e);
        }
    }

    protected Response getSearchResponse(Request request) {
        final Props props = Props.create(SearchActor.class);
        return getResponse(request, props);
    }

    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }

    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(ResponseParams.StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

    protected static void createCompositeSearchIndex() throws Exception {
        SearchConstants.MVC_SEARCH_INDEX = "testcompositeindex";
        ElasticSearchUtil.initialiseESClient(SearchConstants.MVC_SEARCH_INDEX,
                Platform.config.getString("search.es_conn_info"));
        System.out.println("creating index: " + SearchConstants.MVC_SEARCH_INDEX);
        String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
        String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}";
        String esindex = "{\"aliases\":{\"mvc-content\":{}},\"settings\":{\"index\":{\"max_ngram_diff\":\"29\",\"mapping\":{\"total_fields\":{\"limit\":\"1500\"}},\"number_of_shards\":\"5\",\"analysis\":{\"filter\":{\"mynGram\":{\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"],\"min_gram\":\"1\",\"type\":\"nGram\",\"max_gram\":\"30\"}},\"analyzer\":{\"cs_index_analyzer\":{\"filter\":[\"lowercase\",\"mynGram\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"keylower\":{\"filter\":\"lowercase\",\"tokenizer\":\"keyword\"},\"cs_search_analyzer\":{\"filter\":[\"lowercase\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"ml_custom_analyzer\":{\"type\":\"standard\",\"stopwords\":[\"_english_\",\"_hindi_\"]}}},\"number_of_replicas\":\"1\"}},\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"organisation\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"channel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"framework\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"board\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"medium\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"subject\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"gradeLevel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"description\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"language\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appId\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appIcon\":{\"type\":\"text\",\"index\":false},\"appIconLabel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentEncoding\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"identifier\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"node_id\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}},\"nodeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"mimeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"resourceType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"allowedContentTypes\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"objectType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"posterImage\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"artifactUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"launchUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"previewUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"streamingUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"downloadUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"status\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"pkgVersion\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}},\"lastUpdatedOn\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}},\"textbook_name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"source\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"sourceURL\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"ml_Keywords\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentText\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentTextVector\":{\"type\":\"dense_vector\",\"dims\":768},\"label\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"standard\"},\"all_fields\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}}";
        ElasticSearchUtil.addIndex(SearchConstants.MVC_SEARCH_INDEX,
                SearchConstants.MVC_SEARCH_INDEX_TYPE, settings, mappings,esindex);
        insertTestRecords();
    }

    private static void insertTestRecords() throws Exception {
        for (int i=1; i<=30; i++) {
            Map<String, Object> content = getContentTestRecord(null, i, null);
            String id = (String) content.get("identifier");
            addToIndex(id, content);
        }
        Map<String, Object> content = getContentTestRecord("do_10000031", 31, null);
        content.put("name", "31 check name match");
        content.put("description", "हिन्दी description");
        addToIndex("do_10000031", content);

        content = getContentTestRecord("do_10000032", 32, null);
        content.put("name", "check ends with value32");
        addToIndex("do_10000032", content);

        content = getContentTestRecord("do_10000033", 33, "test-board1");
        content.put("name", "Content To Test Consumption");
        addToIndex("10000033", content);

        content = getContentTestRecord("do_10000034", 34, "test-board3");
        content.put("name", "Textbook-10000034");
        content.put("description", "Textbook for other tenant");
        content.put("status","Live");
        content.put("relatedBoards", new ArrayList<String>(){{
            add("test-board1");
            add("test-board2");
        }});
        addToIndex("10000034", content);
    }

    private static void addToIndex(String uniqueId, Map<String, Object> doc) throws Exception {
        String jsonIndexDocument = JsonUtils.serialize(doc);
        ElasticSearchUtil.addDocumentWithId(SearchConstants.MVC_SEARCH_INDEX,
                 uniqueId, jsonIndexDocument);
    }

    private static Map<String, Object> getContentTestRecord(String id, int index, String board) {
        String objectType = "Content";
        Date d = new Date();
        Map<String, Object> map = getTestRecord(id, index, "do", objectType);
        map.put("name", "Content_" + System.currentTimeMillis() + "_name");
        map.put("code", "code_" + System.currentTimeMillis());
        map.put("contentType", getContentType());
        map.put("createdOn", new Date().toString());
        map.put("lastUpdatedOn", new Date().toString());
        if(StringUtils.isNotBlank(board))
            map.put("board",board);
        Set<String> ageList = getAgeGroup();
        if (null != ageList && !ageList.isEmpty())
            map.put("ageGroup", ageList);
        Set<String> grades = getGradeLevel();
        if (null != grades && !grades.isEmpty())
            map.put("gradeLevel", grades);
        if (index % 5 == 0) {
            map.put("lastPublishedOn", d.toString());
            map.put("status", "Live");
            map.put("size", 1000432);
        } else {
            map.put("status", "Draft");
            if (index % 3 == 0)
                map.put("size", 564738);
        }
        Set<String> tagList = getTags();
        if (null != tagList && !tagList.isEmpty() && index % 7 != 0)
            map.put("keywords", tagList);
        map.put("downloads", index);
        return map;
    }

    private static Map<String, Object> getTestRecord(String id, int index, String prefix, String objectType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(id))
            map.put("identifier", id);
        else {
            long suffix = 10000000 + index;
            map.put("identifier", prefix + "_" + suffix);
        }
        map.put("objectType", objectType);
        return map;
    }

    private static String getContentType() {
        return contentTypes[RandomUtils.nextInt(3)];
    }

    private static Set<String> getTags() {
        Set<String> list = new HashSet<String>();
        int count = RandomUtils.nextInt(9);
        for (int i=0; i<count; i++) {
            list.add(keywords[RandomUtils.nextInt(8)]);
        }
        return list;
    }

    private static Set<String> getAgeGroup() {
        Set<String> list = new HashSet<String>();
        int count = RandomUtils.nextInt(2);
        for (int i=0; i<count; i++) {
            list.add(ageGroup[RandomUtils.nextInt(6)]);
        }
        return list;
    }
    
    private static Set<String> getGradeLevel() {
        Set<String> list = new HashSet<String>();
        int count = RandomUtils.nextInt(2);
        for (int i=0; i<count; i++) {
            list.add(gradeLevel[RandomUtils.nextInt(6)]);
        }
        return list;
    }
    
}
