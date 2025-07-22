import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import javax.crypto.*;

import crypto.RSAUtil;

public class FileSender {
    @SuppressWarnings("resource")
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

        System.out.println("Signing file with sender's private key");
        byte[] signature = RSAUtil.signBytes(fileBytes, senderPrivate);

        // Combine file + signature
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(fileBytes);
        baos.write(signature);
        byte[] combinedData = baos.toByteArray();

        // Generate AES key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 128-bit AES
        SecretKey aesKey = keyGen.generateKey();

        // Encrypt combined data with AES
        System.out.println("Encrypting the signed file with AES key");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = aesCipher.doFinal(combinedData);

        // Encrypt AES key with RSA
        System.out.println("Encrypting the AES key with receiver's public key");
        byte[] encryptedAesKey = RSAUtil.encryptBytes(aesKey.getEncoded(), receiverPublic);

        // Send encrypted AES key
        dos.writeInt(encryptedAesKey.length);
        dos.write(encryptedAesKey);

        // Send encrypted data
        dos.writeInt(encryptedData.length);
        dos.write(encryptedData);

        dos.close();
        socket.close();
        System.out.println("All the data sent successfully.");
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
