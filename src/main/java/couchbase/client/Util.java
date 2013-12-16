package couchbase.client;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class Util {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String fromObjectToString(String key, Object rule) {
        try {
            return mapper.writeValueAsString(rule);
        } catch (IOException e) {
            throw new RuntimeException("Cant convert rule " + key + " to String! ");
        }
    }

    public static List<Rule> fromStringToListOfRules(String rules) {
        try {
            return mapper.readValue(rules, new TypeReference<List<Rule>>() { });
        } catch (IOException e) {
            throw new RuntimeException("Cant convert rules string to List");
        }
    }


    public static Rule fromStringToRule(String key, String rule) {
        try {
            return mapper.readValue(rule, Rule.class);
        } catch (IOException e) {
            throw new RuntimeException("Cant convert rule " + key + " to String! ");
        }
    }



    public static String streamToString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }


    public static String cleanString(String inString) {
        String stringToReturn = StringUtils.deleteWhitespace(inString);
        stringToReturn = StringUtils.remove(inString, "\n");
        stringToReturn = StringUtils.remove(stringToReturn, "\t");
        return stringToReturn;
    }


}
