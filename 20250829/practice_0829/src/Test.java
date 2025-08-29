public class Test {
    static class ListNode {
        public int value;
        public ListNode next;

        public ListNode(int value) {
            this.value = value;
        }
    }

    public ListNode head;

    public void addLast(int data) {
        ListNode toAdd = new ListNode(data);
        ListNode cur = head;
        if (head == null) {
            head = toAdd;
            return;
        }
        while (cur.next != null) {
            cur = cur.next;
        }
        cur.next = toAdd;
    }

    public void display() {
        ListNode cur = head;
        while (cur != null) {
            System.out.print(cur.value+" ");
            cur = cur.next;
        }
        System.out.println();
    }

    //获取链表倒数第k个节点
    public static ListNode getKthNode(ListNode head,int k) {
        ListNode fast = head;
        ListNode slow = head;

        while (k-1 != 0) {
            fast = fast.next;
            k--;
        }

        while (fast.next!=null) {
            fast = fast.next;
            slow = slow.next;
        }

        return slow;
    }

    public static void main(String[] args) {
        Test list = new Test();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);
        list.addLast(5);
        list.display();
    }
}


