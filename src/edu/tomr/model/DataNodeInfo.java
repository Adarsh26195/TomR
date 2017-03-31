package edu.tomr.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by muchhals on 2/10/17.
 */
public class DataNodeInfo {

    @JsonProperty private String ipAddress;
    @JsonProperty private String name;
    @JsonProperty private String status;

    public DataNodeInfo() {}

    public DataNodeInfo(String ipAddress, String name, String status) {
        this.ipAddress = ipAddress;
        this.name = name;
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() { return name; }

    public String getStatus() { return status; }
}
