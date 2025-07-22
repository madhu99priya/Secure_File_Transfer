package blockchain;

import java.io.Serializable;
import java.security.MessageDigest;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    public String timestamp;
    public String flowID;
    public String previousHash;
    public String hash;

    public Block(String timestamp, String flowID, String previousHash) {
        this.timestamp = timestamp;
        this.flowID = flowID;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    // Calculate SHA-256 hash of the block
    private String calculateHash() {
        try {
            String input = timestamp + flowID + previousHash;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error calculating block hash", e);
        }
    }

    @Override
    public String toString() {
        return "Block {" +
                "\n  flowID='" + flowID + '\'' +
                ",\n  timestamp='" + timestamp + '\'' +
                ",\n  previousHash='" + previousHash + '\'' +
                ",\n  hash='" + hash + '\'' +
                "\n}";
    }
}
