package edu.tomr.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by muchhals on 2/10/17.
 */
public class DataNodeInfo {

    @JsonProperty private String dnsName;
    @JsonProperty private String ipAddress;

    public DataNodeInfo() {}

    public DataNodeInfo(String dnsName, String ipAddress) {
        this.dnsName = dnsName;
        this.ipAddress = ipAddress;
    }

    public String getDnsName() {
        return dnsName;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
