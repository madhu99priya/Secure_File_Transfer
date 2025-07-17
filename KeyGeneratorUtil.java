import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyGeneratorUtil {
    public static void generateKeyPair(String name) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        // Ensure the keys directory exists
        File dir = new File("keys");
        if (!dir.exists()) {
            dir.mkdirs(); // Create directory if it doesn't exist
        }

        // Write private key
        try (FileOutputStream fos = new FileOutputStream("keys/" + name + "_private.key")) {
            fos.write(pair.getPrivate().getEncoded());
        }

        // Write public key
        try (FileOutputStream fos = new FileOutputStream("keys/" + name + "_public.key")) {
            fos.write(pair.getPublic().getEncoded());
        }
    }

    public static void main(String[] args) throws Exception {
        generateKeyPair("sender");
        generateKeyPair("receiver");
        System.out.println("Keys generated successfully in /keys directory");
    }
}
