package blockchain;

public class Block {
    public String timestamp;
    public String flowID;

    public Block(String timestamp, String flowID) {
        this.timestamp = timestamp;
        this.flowID = flowID;
    }
}
