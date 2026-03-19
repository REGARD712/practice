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
                processConnection(clientSocket);
            });
        }
    }

    private void processConnection(Socket clientSocket) {
        System.out.printf("[%s:%d] 客户端上线\n", clientSocket.getInetAddress(), clientSocket.getPort());
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {
            Scanner scanner = new Scanner(inputStream);
            PrintWriter writer = new PrintWriter(outputStream);
            while (true) {
                if (!scanner.hasNext()) {
                    System.out.printf("[%s:%d] 客户端下线\n", clientSocket.getInetAddress(), clientSocket.getPort());
                    break;
                }
                String request = scanner.next();
                String response = process(request);
                writer.println(response);
                writer.flush();
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
