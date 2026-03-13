import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPEchoServer {
    private DatagramSocket socket = null;
    public UDPEchoServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
    public void start() throws IOException {
        System.out.println("服务器启动");
        while (true) {
            // 1.接收客户端请求
            DatagramPacket requestPacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(requestPacket);
            // 2.将收到的请求转成字符串
            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
            // 3.根据请求计算响应
            String response = process(request);
            // 4.返回响应给客户端
            DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.getBytes().length,
                    requestPacket.getSocketAddress());
            socket.send(responsePacket);
            // 5.打印日志
            System.out.printf("[%s:%d] req: %s, resp: %s\n", requestPacket.getSocketAddress(), requestPacket.getPort(),
                    request, response);
        }
    }

    private String process(String request) {
        return request;
    }

    public static void main(String[] args) throws IOException {
        UDPEchoServer server = new UDPEchoServer(9090);
        server.start();
    }
}
