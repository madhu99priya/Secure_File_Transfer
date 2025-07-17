package blockchain;

import java.util.*;

public class Blockchain {
    private List<Block> chain = new ArrayList<>();

    public void addBlock(String timestamp, String flowID) {
        // chain.add(new Block(timestamp, flowID));
    }

    public boolean isFlowIdUsed(String flowID) {
        return chain.stream().anyMatch(block -> block.flowID.equals(flowID));
    }
}
