package edu.tomr.model;

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonProperty;

import edu.tomr.utils.Constants;

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
    public List<String> getIpAddressList(String filterStatus) {
        return dataNodes.stream().filter(node -> node.getStatus().equals(filterStatus))
                                 .map(DataNodeInfo::getIpAddress)
                                 .collect(Collectors.toList());
    }

}
