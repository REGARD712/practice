import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPEchoClient {
    private Socket socket = null;
    public TCPEchoClient(String serverIp, int serverPort) throws IOException {
        socket = new Socket(serverIp, serverPort);
    }
    public void start() {
        Scanner scanner = new Scanner(System.in);
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            Scanner scannerNet = new Scanner(inputStream);
            PrintWriter writer = new PrintWriter(outputStream);
            while (true) {
                System.out.println("请输入要发送的内容：");
                if (!scanner.hasNext()) {
                    break;
                }
                String request = scanner.next();
                writer.println(request);
                writer.flush();
                String response = scannerNet.next();
                System.out.println(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        TCPEchoClient client = new TCPEchoClient("127.0.0.1", 9090);
        client.start();
    }
}
