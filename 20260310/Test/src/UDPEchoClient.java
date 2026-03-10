import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPEchoClient {
    private DatagramSocket socket = null;
    // 由于UDP本身不会存储对端信息，因此额外创建变量记录ip和端口号
    private String serverIp;
    private int serverPort;

    // 构造方法：参数需要传入指定的ip和端口号
    public UDPEchoClient(String ip, int port) throws SocketException {
        this.serverIp = ip;
        this.serverPort = port;
        socket = new DatagramSocket();  // 这里的端口号让系统自动分配
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
            // 1. 将要发送的内容打包成二进制数据报
            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length,
                    InetAddress.getByName(serverIp), serverPort);

            // 2. 发送数据报
            socket.send(requestPacket);

            // 3. 接收服务器的响应
            DatagramPacket responsePacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(responsePacket);

            // 4. 将接收到的响应转换成字符串并打印
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws IOException {
        UDPEchoClient client = new UDPEchoClient("127.0.0.1", 9090);
        client.start();
    }
}
