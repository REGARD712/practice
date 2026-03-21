import java.util.PriorityQueue;

class MyTimerTask implements Comparable<MyTimerTask> {
    // 要执行的任务
    private Runnable task;
    // 对应的执行时刻
    private long time;

    // 构造方法
    public MyTimerTask(Runnable task, long time) {
        this.task = task;
        this.time = time;
    }

    public void run() {
        task.run();
    }

    // 定义比较规则，重写 compareTo方法
    @Override
    public int compareTo(MyTimerTask o) {  // 时刻越小，越早执行
        return (int)(this.time - o.time);  // 返回值是 int，强转
    }

    public long getTime() {
        return time;
    }
}

class MyTimer {
    private PriorityQueue<MyTimerTask> queue = new PriorityQueue<>();

    public void schedule(Runnable task, long delay) {
        synchronized (this) {
            // 构造任务对象
            MyTimerTask myTimerTask = new MyTimerTask(task, System.currentTimeMillis() + delay);
            // 将任务添加到队列中
            queue.add(myTimerTask);
        }
    }

    // 构造方法
    public MyTimer() {
        // 创建一个线程，负责执行任务
        Thread t = new Thread(() -> {
            // 重复取出任务
            while (true) {
                // 保障原子性防止出现线程安全问题
                synchronized (this) {
                    if (!queue.isEmpty()) {
                        continue;
                    }
                    MyTimerTask task = queue.peek();
                    // 判断任务是否到达执行时机
                    if (System.currentTimeMillis() < task.getTime()) {
                        continue;
                    } else {
                        task.run();
                        queue.poll();
                    }
                }
            }
        });
    }
}

public class MyTimerDemo {
    public static void main(String[] args) {
        MyTimer timer = new MyTimer();
        timer.schedule(() -> {
            System.out.println("执行任务");
        }, 2000);
    }
}
