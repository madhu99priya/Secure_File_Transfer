import java.io.*;
import java.net.*;

public class FileSender {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;
        String filePath = "sample.txt"; // Replace with your file

        try (Socket socket = new Socket(host, port)) {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // Send file name and size first
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            // Send file content
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }

            fis.close();
            dos.close();
            socket.close();
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
