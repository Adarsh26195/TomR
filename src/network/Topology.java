package network;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by muchhals on 3/29/17.
 */
public class Topology {

    private static Set<NodeNeigborTuple> set = new HashSet<>();

    public static boolean addNodeTopology(String ipAddress, String rNeighbor) {
        NodeNeigborTuple tuple = new NodeNeigborTuple(ipAddress, rNeighbor);
        return set.add(tuple);
    }

    public static boolean removeNodeTopology(String ipAddress) {
        return set.remove(new NodeNeigborTuple(ipAddress));
    }

    public static int size() {
        return set.size();
    }

    public static String getNeighbor(String ipAddress) {
        return set.stream()
                  .filter(tuple -> tuple.getNode().equals(ipAddress))
                  .map(NodeNeigborTuple::getRightNeighbor)
                  .collect(Collectors.toList()).get(0);
    }

    public static String getOriginalNode(String neighborIpAddress) {
        return set.stream()
                .filter(tuple -> tuple.getRightNeighbor().equals(neighborIpAddress))
                .map(NodeNeigborTuple::getNode)
                .collect(Collectors.toList()).get(0);
    }

}
