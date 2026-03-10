interface USB {
    double brand = 3.0;  // 默认是被 public static final修饰的
    void openDevice();
    void closeDevice();
}

//class Mouse implements USB {
//
//    @Override
//    void openDevice() {
//        System.out.println("打开鼠标");
//    }
//
//    @Override
//    void closeDevice() {
//        System.out.println("关闭鼠标");
//    }
//}

public class Test {
    public static void main1(String[] args) {
//        Mouse mouse = new Mouse();
//        mouse.openDevice();
//        mouse.closeDevice();
    }

    public static void main(String[] args) {
        System.out.println(USB.brand);  // 可以通过接口名访问，说明是静态的

        USB.brand = 2.0;
    }
}
