package managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.kafka.client.KafkaClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Postman.class,EventProducer.class, KafkaClient.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*","javax.management.*"})
public class MVCContentCreateTest {
    ObjectMapper mapper = new ObjectMapper();
private String req = "{\"request\":{\"content\":[{\"board\":\"State(Tamil Nadu)\",\"subject\":[\"English\"],\"medium\":[\"Telegu\"],\"gradeLevel\":[\"Class 4\",\"Class 5\",\"Class 6\"],\"textbook_name\":[\"Science\"],\"level1Name\":[\"Sorting Materials Into Groups\"],\"level1Concept\":[\"Materials\"],\"source\":[\"Diksha 1\"],\"sourceURL\":\"https://diksha.gov.in/play/content/do_31283180221267968012331\"}]}}";
private String getResponse = "{\"id\":\"api.content.read\",\"ver\":\"1.0\",\"ts\":\"2020-07-31T09:29:44.690Z\",\"params\":{\"resmsgid\":\"5e847d20-d310-11ea-bb67-2b84cd2a1815\",\"msgid\":\"5e83e0e0-d310-11ea-bb67-2b84cd2a1815\",\"status\":\"successful\",\"err\":null,\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"content\":{\"ownershipType\":[\"createdBy\"],\"copyright\":\"Ekstep\",\"previewUrl\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/ecml/do_31249815833451724828554-latest\",\"keywords\":[\"Story\"],\"subject\":[\"Mathematics\"],\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/ecar_files/do_31249815833451724828554/squares-and-square-roots_1559121118657_do_31249815833451724828554_5.0.ecar\",\"language\":[\"English\"],\"source\":\"\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\",\"variants\":{\"spine\":{\"ecarUrl\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/ecar_files/do_31249815833451724828554/squares-and-square-roots_1559121118938_do_31249815833451724828554_5.0_spine.ecar\",\"size\":28269}},\"objectType\":\"Content\",\"gradeLevel\":[\"Class 8\"],\"me_totalRatingsCount\":3,\"appIcon\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/do_31249815833451724828554/artifact/assetsmath_square_1_6112_1521832091_1521832091202.thumb.png\",\"appId\":\"prod.diksha.app\",\"copyType\":\"Enhance\",\"contentEncoding\":\"gzip\",\"artifactUrl\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/do_31249815833451724828554/artifact/1527141453728_do_31249815833451724828554.zip\",\"collaborators\":[\"5469\"],\"me_totalPlaySessionCount\":\"{\\\"portal\\\":15}\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2020-07-31T03:05:38.679+0000\",\"contentType\":\"Resource\",\"identifier\":\"do_31249815833451724828554\",\"lastUpdatedBy\":\"539\",\"audience\":[\"Learner\"],\"me_totalTimeSpentInSec\":\"{\\\"portal\\\":1252}\",\"visibility\":\"Default\",\"author\":\"Ekstep\",\"consumerId\":\"89490534-126f-4f0b-82ac-3ff3e49f3468\",\"mediaType\":\"content\",\"ageGroup\":[\">10\"],\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"lastPublishedBy\":\"ekstep\",\"version\":2,\"license\":\"CC BY 4.0\",\"prevState\":\"Review\",\"size\":516641,\"lastPublishedOn\":\"2019-05-29T09:11:58.657+0000\",\"domain\":[\"numeracy\"],\"name\":\"Squares and square roots\",\"publisher\":\"\",\"attributions\":[\"\"],\"status\":\"Live\",\"totalQuestions\":14,\"code\":\"org.ekstep.literacy.story.17889.fork.6112.1525654093\",\"origin\":\"do_312474333408550912120050\",\"description\":\"Students Resources\",\"streamingUrl\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/ecml/do_31249815833451724828554-latest\",\"posterImage\":\"https://ekstep-public-prod.s3-ap-south-1.amazonaws.com/content/do_312466848489717760216939/artifact/assetsmath_square_1_6112_1521832091_1521832091202.png\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-05-24T00:56:26.363+0000\",\"copyrightYear\":2019,\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2019-05-29T09:11:58.048+0000\",\"dialcodeRequired\":\"No\",\"owner\":\"Ruchi Mazumdar\",\"lastStatusChangedOn\":\"2019-05-30T11:35:14.409+0000\",\"creator\":\"Siri Duvvuru\",\"os\":[\"All\"],\"totalScore\":14,\"pkgVersion\":5,\"versionKey\":\"1539186559643\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCF\",\"s3Key\":\"ecar_files/do_31249815833451724828554/squares-and-square-roots_1559121118657_do_31249815833451724828554_5.0.ecar\",\"me_averageRating\":4,\"lastSubmittedOn\":\"2019-05-29T09:02:03.562+0000\",\"createdBy\":\"6112\",\"compatibilityLevel\":2,\"board\":\"CBSE\",\"resourceType\":\"Read\"}}}";
private Map<String,Object> validateResponse = new HashMap<String,Object>();
@Before
public void setup(){
    initMocks(this);
}

    @Test
  public void testContentCreateIfValidContent() throws Exception {
        validateResponse.put("statuscode","200");
        validateResponse.put("response",getResponse);
        KafkaClient kafkaClient = mock(KafkaClient.class);
        PowerMockito.doNothing().when(kafkaClient).send(Mockito.anyString(),Mockito.anyString());
        PowerMockito.mockStatic(Postman.class);
        Mockito.when(Postman.GET(Mockito.anyString())).thenReturn(validateResponse);
        ReadJson readJson = new ReadJson();
        readJson.read(req,false);
    }
    @Test
    public void testContentCreateIfSourceURLisInValid() throws Exception {
        validateResponse.put("statuscode","400");
        KafkaClient kafkaClient = mock(KafkaClient.class);
        PowerMockito.doNothing().when(kafkaClient).send(Mockito.anyString(),Mockito.anyString());
        PowerMockito.mockStatic(Postman.class);
        Mockito.when(Postman.GET(Mockito.anyString())).thenReturn(validateResponse);
        ReadJson readJson = new ReadJson();
        readJson.read(req,false);
    }
    @Test
    public void testContentCreateIfStatusCodeHasError() throws Exception {
        KafkaClient kafkaClient = mock(KafkaClient.class);
        PowerMockito.doNothing().when(kafkaClient).send(Mockito.anyString(),Mockito.anyString());
        PowerMockito.mockStatic(Postman.class);
        Mockito.when(Postman.GET(Mockito.anyString())).thenReturn(Mockito.anyMap());
        ReadJson readJson = new ReadJson();
        readJson.read(req,false);
    }
    @Test
    public void testContentCreateIfContentMetaHasError() throws Exception {
        validateResponse.put("statuscode","200");
        KafkaClient kafkaClient = mock(KafkaClient.class);
        PowerMockito.doNothing().when(kafkaClient).send(Mockito.anyString(),Mockito.anyString());
        PowerMockito.mockStatic(Postman.class);
        Mockito.when(Postman.GET(Mockito.anyString())).thenReturn(validateResponse);
        ReadJson readJson = new ReadJson();
        readJson.read(req,false);
    }
    public Map<String, Object> getEvent(String message) throws IOException {
        return  mapper.readValue(message,Map.class);
    }
}
