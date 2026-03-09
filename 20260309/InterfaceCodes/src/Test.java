interface USB {
    void openDevice();
    void closeDevice();
}

class Mouse implements USB {
    @Override
    public void openDevice() {
        System.out.println("打开鼠标");
    }

    @Override
    public void closeDevice() {
        System.out.println("关闭鼠标");
    }

    public void click() {
        System.out.println("鼠标点击");
    }
}

class KeyBoard implements USB {
    @Override
    public void openDevice() {
        System.out.println("打开键盘");
    }

    @Override
    public void closeDevice() {
        System.out.println("关闭键盘");
    }

    public void inPut() {
        System.out.println("键盘输入");
    }
}

class Laptop {

    public void powerOn() {
        System.out.println("将笔记本开机");
    }

    public void powerOff() {
        System.out.println("将笔记本关机");
    }

    // 使用USB功能
    public void useUSB(USB usb) {
        // 打开USB
        usb.openDevice();
        // 使用USB：鼠标或键盘
        if (usb instanceof Mouse) {
            Mouse mouse = (Mouse) usb;
            mouse.click();
        } else if (usb instanceof KeyBoard) {
            KeyBoard keyBoard = (KeyBoard) usb;
            keyBoard.inPut();
        }
        // 关闭USB
        usb.closeDevice();
    }
}

public class Test {
    public static void main(String[] args) {
        Laptop laptop = new Laptop();
        laptop.powerOn();
        // 使用鼠标
        laptop.useUSB(new Mouse());
        // 使用键盘
        laptop.useUSB(new KeyBoard());
    }
}
