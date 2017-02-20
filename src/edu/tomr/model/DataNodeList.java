package edu.tomr.model;

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by muchhals on 2/10/17.
 */
public class DataNodeList {

    @JsonProperty("dataNodes")
    private List<DataNodeInfo> dataNodes;

    public DataNodeList() {}

    public DataNodeList(List<DataNodeInfo> dataNodes) {
        this.dataNodes = dataNodes;
    }

    // Additional helper methods
    public List<String> getIpAddressList() {
        return dataNodes.stream().map(DataNodeInfo::getIpAddress).collect(Collectors.toList());
    }

}
