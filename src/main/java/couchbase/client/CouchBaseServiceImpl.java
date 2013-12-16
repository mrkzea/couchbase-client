package couchbase.client;

import com.couchbase.client.ClusterManager;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.clustermanager.BucketType;
import com.couchbase.client.clustermanager.FlushResponse;
import com.couchbase.client.protocol.views.*;
import couchbase.client.xjc.generated.GetRuleType;
import couchbase.client.xjc.generated.RulesType;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang.StringEscapeUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static couchbase.client.Util.fromObjectToString;
import static couchbase.client.Util.fromStringToRule;


public class CouchBaseServiceImpl implements CouchBaseService {

    /* couchbase server location */
    private final String url = "http://app0243.proxmox.swe1.com:8091/pools";

    CouchbaseClient client;
    ClusterManager manager;

    private final String password = "";

    private java.lang.String bucketName = "mock-server";

    private final String designDocName  = "mockserver" ;
    private final String viewName       = "mockserver_view";
    private final String adminUser      = "admin";
    private final String adminPass      = "adminadmin";


    @PostConstruct
    public void init() {
        List<URI> uris = new LinkedList<URI>();
        uris.add(URI.create(url));
        //System.setProperty("viewmode", "development");
        manager = new ClusterManager(uris, adminUser, adminPass);
        try {
            client = new CouchbaseClient(uris, bucketName, password);
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to db " + e);
        }
    }



    public List<String> getAllKeysFromDB(){
        List<String> keyList = new ArrayList<String>();
        View view = client.getView(designDocName, viewName);
        Query query = new Query();
        query.setIncludeDocs(false);
        ViewResponse result = client.query(view, query);
        Iterator<ViewRow> itr = result.iterator();
        while (itr.hasNext()) {
            ViewRow row = itr.next();
            keyList.add(row.getValue());
        }
       return keyList;
    }




    public List<Rule> getAllDocumentsFromDb() {

        List<Rule> list = new ArrayList<Rule>();

        View view = client.getView(designDocName, viewName);

        Query query = new Query();
        query.setIncludeDocs(true);


        ViewResponse result = client.query(view, query);

        Iterator<ViewRow> itr = result.iterator();

        while (itr.hasNext()) {
            ViewRow row = itr.next();
            Object document = row.getDocument();
            Rule rule = fromStringToRule(row.getKey(), document.toString());
            rule.setKey(row.getValue());
            list.add(rule);
        }

      /*  Collections.sort(list, new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getIndex() > o2.getIndex() ? 1 : -1 ;
            }
        });*/

        return list;

    }



    @Override
    public Rule getDocument(String key) {
        Object document = client.get(key);
        return fromStringToRule(key, document.toString());
    }



    public boolean updateDocuments(List<Rule> rules){
        boolean isSuccess = false;
        if(isSetOfRulesCorrect(rules)){
                isSuccess = addDocuments(rules);
        }
        return isSuccess;
    }



    @Override
    public boolean flushBucket() {
        FlushResponse flushResponse = manager.flushBucket(bucketName);
        return "OK".equals(flushResponse.name());

    }



    @Override
    public boolean delete(String ruleId) {
        OperationFuture<Boolean> setOp = client.delete(ruleId);
        try {

            boolean deleted = setOp.get(30, TimeUnit.SECONDS).booleanValue();
            return deleted;

        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (ExecutionException ignore) {
            ignore.printStackTrace();
        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out...");
        }
        return false;
    }



    public boolean addDocuments(List<Rule> rules) {
        boolean isSuccess = true;
        for(Rule rule : rules){
            boolean added = addDocument(rule.getKey(), rule);
            if(!added){
                isSuccess = false;
                break;
            }
        }
        return isSuccess;
    }


    private boolean isSetOfRulesCorrect(List<Rule> rules) {
        try{
            checkArgument(rules.size() > 0);
            if(rules.size() > 0){
                for(Rule rule : rules){
                    checkState(rule.getKey() != null);
                    checkState(rule.getBody() != null);
                    checkState(rule.getStatusCode() != null);
                }
            }
        }catch(IllegalStateException e){
            return false;
        }catch(IllegalArgumentException e){
            return false;
        }
        return true;
    }


    public boolean addDocument(String key, Rule rule) {

        String jsonRule = fromObjectToString(key, rule);
        OperationFuture<Boolean> setOp = client.set(key, 0, jsonRule );
        try {

            boolean added = setOp.get(30, TimeUnit.SECONDS).booleanValue();
            return added;

        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (ExecutionException ignore) {
            ignore.printStackTrace();
        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out...");
        }
        return false;
    }



    /* This method will insert to the Couchbase all the rules from rules.xml file */
    public boolean updateDatabaseWithFilesContent(){

        boolean isSuccess = false;

        try {
            RulesType rulesType = new RulesFileParser().parseRulesFile();

            int order = 0;
            for (GetRuleType getRuleType : rulesType.getGetRuleTypeList()) {


                String key = getRuleType.getUrl();
                String content = Util.streamToString(getClass().getClassLoader().getResourceAsStream(getRuleType.getFile()));

                String couchJson = buildCouchJson(key, content, order);
                Rule rule = fromStringToRule(key, couchJson);

                if (addDocument(key,rule )) {
                    isSuccess = true;
                } else{
                    isSuccess = false;
                    break;
                }
                order++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }

        return isSuccess;
    }



    private String buildCouchJson(String key, String content, int order) {

        String ruleJson = "{\n" +
                "  \"key\": \"SET\",\n" +
                "  \"statusCode\": \"0\",\n" +
                "  \"index\": \"0\",\n" +
                "  \"body\":" + content + "\n" +
                "}";

        Rule rule = fromStringToRule(key, ruleJson);
        rule.setKey(key);
        rule.setIndex(order);
        rule.setStatusCode(200);
        return fromObjectToString(key, rule);
    }



    private String decodeIntoResponse(String couchJson){
        return StringEscapeUtils.unescapeXml(couchJson);
    }


    private void createBucket(String name){
        manager.createNamedBucket(BucketType.COUCHBASE, name, 100, 1, password, false);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private void createView(String docName, String viewName){
        DesignDocument designDoc = new DesignDocument(docName);

        String mapFunction =
                "function (doc, meta) {\n" +
                        "  emit(meta.id, null);\n" +
                        "}";

        ViewDesign viewDesign = new ViewDesign(viewName,mapFunction);
        designDoc.getViews().add(viewDesign);
        client.createDesignDoc( designDoc );
    }

}
