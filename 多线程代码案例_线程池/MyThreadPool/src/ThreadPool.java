import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// 实现一个固定线程个数的线程池
class MyThreadPool {
    // 工作队列，用于存放任务：先不初始化
    private BlockingQueue<Runnable> queue = null;

    // 构造方法：参数 n表示线程池中的固定线程个数
    public MyThreadPool(int n) {
        queue = new ArrayBlockingQueue<>(100);

        // 创建 n个线程，分别从线程池中取出任务并执行
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        // 取出任务，当队列为空时进入阻塞
                        Runnable task = queue.take();
                        // 执行任务
                        task.run();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
            });
            t.start();
        }
    }

    // submit方法：负责将任务放入工作队列中
    public void submit(Runnable task) throws InterruptedException {
        // 将任务放入队列，当队列已满，就会进入阻塞
        queue.put(task);
    }
}

public class ThreadPool {
    public static void main(String[] args) throws InterruptedException {
        // 测试线程池
        MyThreadPool threadPool = new MyThreadPool(5);  // 固定线程个数是5
        // 创建线程池对象时调用了构造方法，这时已经在阻塞状态（take方法），因为工作队列中没有任务

        for (int i = 0; i < 100; i ++) {
            int id = i;
            // 使用线程池处理 100个任务
            threadPool.submit(() -> {
                // 描述具体任务细节
                System.out.println(Thread.currentThread().getName() + " id = " + id);
            });
        }
    }
}
