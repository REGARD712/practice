import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPEchoClient {
    private DatagramSocket socket = null;
    private String serverIp;
    private int serverPort;
    public UDPEchoClient(String serverIp, int serverPort) throws SocketException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        socket = new DatagramSocket();
    }
    public void start() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入要发送的内容：");
            if (!scanner.hasNext()) {
                break;
            }
            // 1. 接收控制台
            String request = scanner.next();
            // 2. 发送请求
            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length,
                    InetAddress.getByName(serverIp), serverPort);
            socket.send(requestPacket);
            // 3. 接收响应
            DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(responsePacket);
            // 4. 打印响应
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws IOException {
        UDPEchoClient client = new UDPEchoClient("127.0.0.1", 9090);
        client.start();
    }
}
