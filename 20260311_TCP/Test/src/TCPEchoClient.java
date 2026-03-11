import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPEchoClient {
    Socket socket = null;
    // 构造方法
    public TCPEchoClient(String serverIp, int serverPort) throws IOException {
        socket = new Socket(serverIp, serverPort);  // 这里的socket会自动存储对端信息
    }
    public void start() {
        Scanner scanner = new Scanner(System.in);  // 用于接收从控制台输入的信息
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            Scanner scannerNet = new Scanner(inputStream);  // 接收客户端的请求
            PrintWriter writer = new PrintWriter(outputStream);
            while (true) {
                // 1. 从控制台获取请求
                System.out.println("请输入要发送的内容：");
                String request = scanner.next();
                // 2. 发送请求到服务器
                writer.println(request);
                writer.flush();
                // 3. 接收服务器的响应
                String response = scannerNet.next();
                // 4. 打印服务器响应
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
