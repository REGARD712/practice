class Animal {
    protected String name;
    public Animal(String name) {
        this.name = name;
    }
}

class Cat extends Animal implements IRunning {

    public Cat(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("猫猫" + name + "正在地上跑");
    }
}

class Bird extends Animal implements IFlying {

    public Bird(String name) {
        super(name);
    }

    @Override
    public void fly() {
        System.out.println("鸟儿" + name + "正在天上飞");
    }
}

class Fish extends Animal implements ISwimming {

    public Fish(String name) {
        super(name);
    }

    @Override
    public void swim() {
        System.out.println("鱼儿" + name + "正在水中游");
    }
}

class Frog extends Animal implements ISwimming, IRunning {

    public Frog(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("青蛙" + name + "正在地上跳");
    }

    @Override
    public void swim() {
        System.out.println("青蛙" + name + "正在水里游");
    }
}

class Duck extends  Animal implements IRunning, ISwimming, IFlying {

    public Duck(String name) {
        super(name);
    }

    @Override
    public void fly() {
        System.out.println("鸭子" + name + "正在天上飞");
    }

    @Override
    public void run() {
        System.out.println("鸭子" + name + "正在地上跑");
    }

    @Override
    public void swim() {
        System.out.println("鸭子" + name + "正在水里游");
    }
}

public class Test {
    public static void main(String[] args) {
        Cat cat = new Cat("花花");
        Bird bird = new Bird("翠翠");
        Fish fish = new Fish("富贵");
        Duck duck = new Duck("鸭鸭");
        cat.run();
        bird.fly();
        fish.swim();
        duck.run();
        duck.swim();
        duck.fly();
    }
}
