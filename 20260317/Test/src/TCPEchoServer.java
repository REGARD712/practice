import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPEchoServer {
    private ServerSocket serverSocket = null;
    public TCPEchoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    public void start() throws IOException {
        System.out.println("服务器启动");

        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            pool.submit(() -> {
                processConnetion(clientSocket);
            });
        }
    }

    private void processConnetion(Socket clientSocket) {
        System.out.printf("[%s:%d] 客户端上线\n", clientSocket.getInetAddress(), clientSocket.getPort());
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {
            Scanner scanner = new Scanner(inputStream);
            PrintWriter writer = new PrintWriter(outputStream);
            while (true) {
                // 1. 读取请求
                if (!scanner.hasNext()) {
                    System.out.printf("[%s:%d] 客户端下线\n", clientSocket.getInetAddress(), clientSocket.getPort());
                    break;
                }
                String request = scanner.next();
                // 2. 根据请求计算响应
                String response = process(request);
                // 3. 返回响应
                writer.println(response);
                writer.flush();
                // 4. 打印日志
                System.out.printf("[%s:%d] req: %s, resp: %s\n", clientSocket.getInetAddress(), clientSocket.getPort(),
                        request, response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String process(String request) {
        return request;
    }

    public static void main(String[] args) throws IOException {
        TCPEchoServer server = new TCPEchoServer(9090);
        server.start();
    }
}
