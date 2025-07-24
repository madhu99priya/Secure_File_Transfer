import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import blockchain.Blockchain;
import crypto.RSAUtil;

public class FileReceiver {
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started. Waiting for sender...");

        Socket socket = serverSocket.accept();
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        // Load existing blockchain from file (or start fresh -> when we initailly using
        // )
        Blockchain blockchain = Blockchain.loadFromFile("blockchain_data.ser");

        // Receive flowID and timestamp from sender
        String flowID = dis.readUTF();
        String timestamp = dis.readUTF();

        System.out.println("Received flowID: " + flowID);
        System.out.println("Received timestamp: " + timestamp);

        System.out.println("Checking for replay attack...");
        if (blockchain.isFlowIdUsed(flowID)) {
            System.out.println("Replay attack detected! This flowID was already used.");
            socket.close();
            return;
        } else {
            System.out.println("No replay attack detected. Block is addeding to blockchain.");
            blockchain.addBlock(timestamp, flowID);
            blockchain.saveToFile("blockchain_data.ser");
            System.out.println("Block is added to blockchain.");
        }

        // Print the current blockchain for audit
        blockchain.printBlockchain();

        // Load keys
        PrivateKey receiverPrivate = loadPrivateKey("keys/receiver_private.key");
        PublicKey senderPublic = loadPublicKey("keys/sender_public.key");

        System.out.println("\nLoaded receiver private key.");
        System.out.println("Loaded sender public key.");

        // Receive encrypted AES key
        int aesKeyLen = dis.readInt();
        byte[] encryptedAesKey = new byte[aesKeyLen];
        dis.readFully(encryptedAesKey);

        // Decrypt AES key using RSA
        System.out.println("Decrypting the AES key using receiver's private key");
        byte[] aesKeyBytes = RSAUtil.decryptBytes(encryptedAesKey, receiverPrivate);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Receive encrypted file + signature
        int encDataLen = dis.readInt();
        byte[] encryptedData = new byte[encDataLen];
        dis.readFully(encryptedData);

        // Decrypt the combined data
        System.out.println("Decrypting the signed file with the AES key ");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] combinedData = aesCipher.doFinal(encryptedData);

        // Separate file content and signature
        int signatureLength = 256; // for 2048-bit RSA
        int fileLength = combinedData.length - signatureLength;

        byte[] fileBytes = Arrays.copyOfRange(combinedData, 0, fileLength);
        byte[] signature = Arrays.copyOfRange(combinedData, fileLength, combinedData.length);

        System.out.println("Verifying signature using the sender's public key");
        if (!RSAUtil.verifyBytes(fileBytes, signature, senderPublic)) {
            System.out.println("Invalid signature. Aborting file save.");
            return;
        }

        // Save decrypted content to file
        System.out.println("Saving file content of the received file");
        new FileOutputStream("received_file.txt").write(fileBytes);
        System.out.println("File saved as received_file.txt");
        System.out.println("Decrypted File Content:\n" + new String(fileBytes));

        // Clean up
        dis.close();
        socket.close();
        serverSocket.close();
        System.out.println("File received successfully.");
    }

    public static PrivateKey loadPrivateKey(String file) throws Exception {
        byte[] bytes = java.nio.file.Files.readAllBytes(new File(file).toPath());
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static PublicKey loadPublicKey(String file) throws Exception {
        byte[] bytes = java.nio.file.Files.readAllBytes(new File(file).toPath());
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }
}
