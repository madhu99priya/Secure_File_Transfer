package blockchain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Blockchain implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Block> chain = new ArrayList<>();

    public Blockchain() {
        // Add genesis block if chain is empty
        if (chain.isEmpty()) {
            chain.add(new Block("0", "GENESIS", "0"));
        }
    }

    public void addBlock(String timestamp, String flowID) {
        Block previousBlock = chain.get(chain.size() - 1);
        Block newBlock = new Block(timestamp, flowID, previousBlock.hash);
        chain.add(newBlock);
        System.out.println("Block added to blockchain.");
    }

    public boolean isFlowIdUsed(String flowID) {
        return chain.stream().anyMatch(block -> block.flowID.equals(flowID));
    }

    public void printBlockchain() {
        System.out.println("======= Current Blockchain =======");
        for (Block block : chain) {
            System.out.println(block);
            System.out.println("----------------------------------");
        }
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Blockchain loadFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Blockchain) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing blockchain found. Creating new one...");
            return new Blockchain();
        }
    }
}
