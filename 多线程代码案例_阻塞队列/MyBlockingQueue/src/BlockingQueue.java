// 基于数组的阻塞队列
class MyBlockingQueue {
    private String[] queue = null;
    private int head;
    private int tail;
    private int size;
    public MyBlockingQueue(int capacity) {
        this.queue = new String[capacity];
    }
    public void put(String elem) throws InterruptedException {
        synchronized (this) {
            while (size >= queue.length) {
                // 当数组已满，阻塞
                this.wait();
            }
            queue[tail++] = elem;
            if (tail >= queue.length) {
                tail = 0;
            }
            size++;
            this.notify();  // 唤醒take方法的等待
        }
    }
    public String take() throws InterruptedException {
        synchronized (this) {
            while (size == 0) {
                this.wait();
            }
            // 取队头元素
            String ret = queue[head++];
            if (head >= queue.length) {
                head = 0;
            }
            size--;
            this.notify();  // 唤醒put方法的等待
            return ret;
        }
    }
}
public class BlockingQueue {
}
