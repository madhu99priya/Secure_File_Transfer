import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import blockchain.Blockchain;

public class FileReceiver {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started. Waiting for sender...");

        Socket socket = serverSocket.accept();
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        Blockchain blockchain = new Blockchain();

        String flowID = dis.readUTF();
        String timestamp = dis.readUTF();

        System.out.println("Received flowID: " + flowID);
        System.out.println("Received timestamp: " + timestamp);

        System.out.println("Checking for replay attack...");
        if (blockchain.isFlowIdUsed(flowID)) {
            System.out.println("Replay attack detected. Exiting.");
            socket.close();
            return;
        } else {
            blockchain.addBlock(timestamp, flowID);
            System.out.println("No replay detected. Block added to blockchain.");
        }

        int encLen = dis.readInt();
        byte[] encBytes = new byte[encLen];
        dis.readFully(encBytes);

        int sigLen = dis.readInt();
        byte[] sigBytes = new byte[sigLen];
        dis.readFully(sigBytes);

        PublicKey senderPublic = loadPublicKey("keys/sender_public.key");
        PrivateKey receiverPrivate = loadPrivateKey("keys/receiver_private.key");

        System.out.println("Loaded sender public key.");
        System.out.println("Loaded receiver private key.");

        System.out.println("Verifying signature...");
        if (!verifySignature(encBytes, sigBytes, senderPublic)) {
            System.out.println("Invalid signature. Aborting.");
            return;
        }
        System.out.println("Signature verified successfully.");

        System.out.println("Decrypting the data...");
        byte[] decrypted = decrypt(encBytes, receiverPrivate);
        System.out.println("Decryption completed.");

        new FileOutputStream("received_file.txt").write(decrypted);
        System.out.println("Saved to received_file.txt");
        System.out.println("Decrypted File Content:\n" + new String(decrypted));

        dis.close();
        socket.close();
        serverSocket.close();
        System.out.println("File received successfully.");
    }

    public static boolean verifySignature(byte[] data, byte[] sig, PublicKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(data);
        return signature.verify(sig);
    }

    public static byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
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
