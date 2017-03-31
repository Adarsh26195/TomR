package edu.tomr.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by muchhals on 3/25/17.
 */
public class DataTuple {

    @JsonProperty("key")
    private final String key;
    @JsonProperty("value")
    private final String value;

    public DataTuple(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public DataTuple() {
        this.key = null;
        this.value = null;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
