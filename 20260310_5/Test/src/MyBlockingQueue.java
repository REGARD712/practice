public class MyBlockingQueue {
    private String[] data = null;
    private int head;
    private int tail;
    private int size;
    // 构造方法
    public MyBlockingQueue(int capacity) {
        data = new String[capacity];
    }
    public void put(String elem) throws InterruptedException {
        synchronized (this) {
            // 判断队列是否已满
            while (size >= data.length) {
                this.wait();
                //return;
            }
            // 入队列
            data[tail++] = elem;
            // 判断当前tail是否越界
            if (tail >= data.length) {
                tail = 0;
            }
            size++;
            this.notify();
        }
    }
    public String take() throws InterruptedException {
        synchronized (this) {
            // 判断队列是否为空
            while (size == 0) {
                this.wait();
                //return null;
            }
            // 获取队头元素
            String ret = data[head++];
            // 判断当前head是否越界
            if (head >= data.length) {
                head = 0;
            }
            size--;
            this.notify();
            return ret;
        }
    }
}
