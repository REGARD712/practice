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
            // 1.从控制台接收信息
            System.out.println("请输入要发送的内容：");
            if (!scanner.hasNext()) {
                break;
            }
            String request = scanner.next();
            // 2.构造数据报并发送信息
            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length,
                    InetAddress.getByName(serverIp), serverPort);
            socket.send(requestPacket);
            // 3.接收响应并打印
            DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws IOException {
        UDPEchoClient client = new UDPEchoClient("127.0.0.1", 9090);
        client.start();
    }
}
