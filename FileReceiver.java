import java.io.*;
import java.net.*;

public class FileReceiver {
    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            // Receive file name
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            // Save file
            FileOutputStream fos = new FileOutputStream("received_" + fileName);
            byte[] buffer = new byte[4096];
            int read;
            long totalRead = 0;
            while (totalRead < fileSize
                    && (read = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) > 0) {
                totalRead += read;
                fos.write(buffer, 0, read);
            }

            fos.close();
            dis.close();
            socket.close();
            System.out.println("File received successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
