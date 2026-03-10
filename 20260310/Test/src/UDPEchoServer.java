import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPEchoServer {
    private DatagramSocket socket = null;

    // 构造方法，指定端口号
    public UDPEchoServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    // start方法处理请求并返回响应
    public void start() throws IOException {
        System.out.println("服务器启动");

        while (true) {
            // 1. 接受请求
            DatagramPacket requestPacket = new DatagramPacket(new byte[4096], 4096);
            socket.receive(requestPacket);
            //    将接收到的二进制数据转为字符串
            String request = new String(requestPacket.getData(), 0, requestPacket.getLength());

            // 2. 处理请求
            String response = process(request);

            // 3. 根据response构造数据报，目的ip和目的端口通过requestPacket来获取
            DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.getBytes().length,
                    requestPacket.getSocketAddress());
            socket.send(responsePacket);

            // 4. 打印日志
            System.out.printf("[%s:%d] req: %s, resp: %s\n", requestPacket.getAddress().toString(), requestPacket.getPort(),
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
