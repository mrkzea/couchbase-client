package couchbase.client;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class CouchBaseServiceTest  {


    private CouchBaseService couchBaseService;

    private Gson gson = new Gson();


    @Before
    public void setUp(){
        couchBaseService = new CouchBaseServiceImpl();
    }


    @Test
    public void testThatItIsPossibleToInsertDocumentIntoDb() {

        String statusCode = "200";
        String[] keys = {".*/betoffer/mostpopular/3way\\.json.*", ".*/betoffer/event/\\d+\\.json.*", ".*/group\\.json.*", ".*/betoffer/group/\\d+\\.json.*"};

        String ruleJson = "{\n" +
                "  \"key\": \"TO_BE_SET\",\n" +
                "  \"statusCode\": \"200\",\n" +
                "  \"index\": \"3\",\n" +
                "  \"body\": {\n" +
                "    \"events\": [\n" +
                "      {\n" +
                "        \"id\": 483322,\n" +
                "        \"name\": \"Manchester United - Arsenal\",\n" +
                "        \"homeName\": \"Manchester United\",\n" +
                "        \"awayName\": \"Arsenal\",\n" +
                "        \"sport\": \"FOOTBALL\",\n" +
                "        \"boUri\": \"/offering/api/v2/kambi/betoffer/event/483322.json\",\n" +
                "        \"participants\": [\n" +
                "          {\n" +
                "            \"participantId\": 123,\n" +
                "            \"name\": \"Manchester United\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"participantId\": 456,\n" +
                "            \"name\": \"Arsenal\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        List<Rule> rules = new ArrayList<Rule>();
        for (int i = 0; i < 4; i++) {
            Rule rule = gson.fromJson(ruleJson, Rule.class);
            rule.setKey(keys[i]);
            rule.setStatusCode(Integer.valueOf(statusCode));
            rules.add(rule);
        }
        boolean isSuccess = couchBaseService.addDocuments(rules);
        Assert.assertTrue(isSuccess);

    }


    @Test
    public void testThatItIsPossibleToRetrieveAllDocumentsFromDb() throws Exception {
        List<Rule> allDocumentsFromDb = couchBaseService.getAllDocumentsFromDb();
        Assert.assertTrue(allDocumentsFromDb.size() > 0);
        Assert.assertTrue(allDocumentsFromDb.get(0).getKey() != null);
        Assert.assertTrue(allDocumentsFromDb.get(0).getBody() != null);
    }


    @Test
    public void testAddAllFilesIntoCouchbase() throws Exception {
        boolean b = couchBaseService.updateDatabaseWithFilesContent();
        Assert.assertTrue(b);
    }

    @Test
    public void testFlushingTheBucket() {
        List<Rule> before = couchBaseService.getAllDocumentsFromDb();
        Assert.assertTrue(couchBaseService.flushBucket());
        List<Rule> after = couchBaseService.getAllDocumentsFromDb();
        Assert.assertTrue(after.size() == 0);
        boolean added = couchBaseService.addDocuments(before);
        Assert.assertTrue(added);
        after = couchBaseService.getAllDocumentsFromDb();
        Assert.assertTrue(after.size() == before.size());
    }


    @Test
    public void testUpdatingBucket() throws Exception {
        List<Rule> before = couchBaseService.getAllDocumentsFromDb();
        System.out.println("Before size = " + before.size());
        Assert.assertTrue(before.size() > 0);
        before.add(before.get(0));
        Assert.assertTrue(couchBaseService.updateDocuments(before));
        List<Rule> after = couchBaseService.getAllDocumentsFromDb();
        System.out.println("After size = " + after.size());
        Assert.assertTrue(after.size() == before.size() + 1);
    }

}
