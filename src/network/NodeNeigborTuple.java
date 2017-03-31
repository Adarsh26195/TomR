package network;

/**
 * Created by muchhals on 3/29/17.
 */
public class NodeNeigborTuple {

    private final String node;

    private final String rightNeighbor;

    public NodeNeigborTuple(String node, String rightNeighbor) {
        this.node = node;
        this.rightNeighbor = rightNeighbor;
    }

    public NodeNeigborTuple(String node) {
        this.node = node;
        this.rightNeighbor = null;
    }

    public String getNode() {
        return node;
    }

    public String getRightNeighbor() {
        return rightNeighbor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeNeigborTuple that = (NodeNeigborTuple) o;

        return node.equals(that.node);

    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return "NodeNeigborsTuple{" +
                "node='" + node + '\'' +
                ", rightNeighbor='" + rightNeighbor + '\'' +
                '}';
    }
}
