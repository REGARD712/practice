import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {
    // 工作队列
    private BlockingQueue<Runnable> queue = null;
    public MyThreadPool(int nThreads) {
        // 工作队列的容量设置为1000
        queue = new ArrayBlockingQueue<>(1000);

        // 创建n个线程
        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread(() -> {
                try {
                    // 循环读取任务
                    while (true) {
                        Runnable task = queue.take();
                        // 取到任务就执行
                        task.run();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }
    }

    public void submit(Runnable task) throws InterruptedException {
        queue.put(task);
    }
}
