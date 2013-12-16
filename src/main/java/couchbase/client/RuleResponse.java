package couchbase.client;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static couchbase.client.Util.fromObjectToString;

public class RuleResponse {
    private String body;
    private Integer statusCode;

    public RuleResponse(String body, Integer statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public RuleResponse(String key, Rule rule) {
        this.statusCode = rule.getStatusCode();
        this.body = fromObjectToString(key, rule.getBody());
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
