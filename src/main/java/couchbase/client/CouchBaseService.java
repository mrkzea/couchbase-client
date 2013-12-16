package couchbase.client;


import java.util.List;

public interface CouchBaseService  {

    List<Rule> getAllDocumentsFromDb();

    Rule getDocument(String key);

    List<String> getAllKeysFromDB();

    boolean addDocument(String key, Rule value);

    boolean addDocuments(List<Rule> rules);

    boolean updateDocuments(List<Rule> rules);

    boolean updateDatabaseWithFilesContent();

    boolean flushBucket();

    boolean delete(String ruleId);

}
