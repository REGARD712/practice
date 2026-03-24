import java.util.PriorityQueue;
/*
* 1. 按照四步骤写出大概框架
* 2. 针对线程安全问题进行加锁优化
* 3. 处理因加锁导致的时间误差问题
* 4. 考虑队列为空时的处理
* */


// 用一个类表示任务
class MyTimerTask implements Comparable<MyTimerTask>{
    // 任务
    private Runnable task;
    // 记录任务执行的时刻
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
    // 返回值小的作为堆顶元素，优先执行
    @Override
    public int compareTo(MyTimerTask o) {
        return (int)(this.time - o.time);
    }

    public long getTime() {
        return time;
    }
}

// 定时器类
class MyTimer {
    // 任务队列
    private PriorityQueue<MyTimerTask> queue = new PriorityQueue<>();

    // shcedule方法：将任务提交到队列中
    public void schedule(Runnable task, long delay) {
        // 为保证线程安全，加锁
        synchronized (this) {
            MyTimerTask timerTask = new MyTimerTask(task, System.currentTimeMillis() + delay);
            queue.offer(timerTask);
            // 唤醒线程
            this.notify();
        }
    }

    // 构造方法中创建一个线程，负责取出任务并按规定的时间执行
    public MyTimer() {
        Thread t = new Thread(() -> {
            try {
                // 为保障线程安全，加锁
                synchronized (this) {
                    while (true) {
                        // 取出任务
                        while (queue.isEmpty()) {
                            // continue;
                            this.wait();
                        }
                        MyTimerTask task = queue.peek();
                        // 判断当前任务是否到达执行时机
                        if (System.currentTimeMillis() < task.getTime()) {
                            // 系统时间比任务执行时间小，说明还未达到执行时机
                            //continue;
                            this.wait(task.getTime() - System.currentTimeMillis());
                        } else {
                            // 执行任务
                            task.run();
                            queue.poll();
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }
}

public class Timer {
    public static void main(String[] args) {
        MyTimer timer = new MyTimer();

        timer.schedule(() -> {
            System.out.println("timer 3000");
        }, 3000);

        timer.schedule(() -> {
            System.out.println("timer 1000");
        }, 1000);

        timer.schedule(() -> {
            System.out.println("timer 2000");
        }, 2000);

        System.out.println("main");
    }
}
