import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.Cipher;

public class FileSender {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 5000);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        String flowID = UUID.randomUUID().toString();
        String timestamp = Long.toString(System.currentTimeMillis());

        System.out.println("Generating flowID and timestamp...");
        System.out.println("flowID: " + flowID);
        System.out.println("timestamp: " + timestamp);

        dos.writeUTF(flowID);
        dos.writeUTF(timestamp);

        File file = new File("send_this.txt");
        byte[] fileBytes = new byte[(int) file.length()];
        new FileInputStream(file).read(fileBytes);
        System.out.println("Original File Content:\n" + new String(fileBytes));

        PrivateKey senderPrivate = loadPrivateKey("keys/sender_private.key");
        PublicKey receiverPublic = loadPublicKey("keys/receiver_public.key");

        System.out.println("Loaded sender private key.");
        System.out.println("Loaded receiver public key.");
        System.out.println("Encrypting file with receiver's public key...");

        byte[] encryptedData = encrypt(fileBytes, receiverPublic);

        System.out.println("Signing encrypted data with sender's private key...");
        byte[] signature = signData(encryptedData, senderPrivate);

        System.out.println("Encrypted data length: " + encryptedData.length);
        System.out.println("Signature length: " + signature.length);

        dos.writeInt(encryptedData.length);
        dos.write(encryptedData);

        dos.writeInt(signature.length);
        dos.write(signature);

        dos.close();
        socket.close();
        System.out.println("Data sent successfully.");
    }

    public static byte[] encrypt(byte[] data, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(data);
        return signature.sign();
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
