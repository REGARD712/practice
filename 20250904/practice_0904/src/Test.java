public class Test {
    public static void main(String[] args) {
        PraLinkedlist list = new PraLinkedlist();
        list.add(1);
        list.add(2);
        list.add(3);
        list.display();

        list.remove(2);
        list.display();
    }
}
