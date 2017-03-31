package edu.tomr.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by muchhals on 3/25/17.
 */
public class PostRequestPayload {

    @JsonProperty("data") private DataTuple[] data;

    public PostRequestPayload(DataTuple[] data) {
        this.data = data;
    }

    public PostRequestPayload() {}

    public DataTuple[] getData() {
        return data;
    }
}
