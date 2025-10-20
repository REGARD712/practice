import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        MyList list = new MyList();

        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);
        list.addLast(5);
        list.display();
        System.out.println(list.size());
        list.reverse();
        list.display();
    }
}
