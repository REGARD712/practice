import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1000);

        // 生产者
        Thread producer = new Thread(() -> {
            int n = 0;
            while (true) {
                try {
                    queue.put(n);
                    System.out.println("生产元素" + n);
                    n++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "producer");

        // 消费者
        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    Integer n = queue.take();
                    System.out.println("消费元素" + n);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "consumer");

        producer.start();
        consumer.start();
    }
}
