import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPEchoClient {
    private DatagramSocket socket = null;
    // 由于UDP本身不存储对端信息，使用变量记录
    private String serverIp;
    private int serverPort;

    // 构造方法
    public UDPEchoClient(String Ip, int port) throws SocketException {
        this.serverIp = Ip;
        this.serverPort = port;
        socket = new DatagramSocket();  // 这里让系统自动分配端口号
    }

    // start方法
    public void start() throws IOException {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("请输入要发送的内容：");
            if (!in.hasNext()) {
                break;
            }
            String request = in.next();
            // 构造数据报
            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length,
                    InetAddress.getByName(serverIp), serverPort);
            // 发送数据报
            socket.send(requestPacket);
            // 接收响应数据报
            DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(responsePacket);
            // 将接收到的响应数据报转为字符串
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            // 打印响应
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws IOException {
        UDPEchoClient client = new UDPEchoClient("127.0.0.1", 9090);
        client.start();
    }
}
