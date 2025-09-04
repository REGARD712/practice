public class PraLinkedlist {
    static class ListNode {
        public int val;
        public ListNode prev;
        public ListNode next;

        public ListNode(int val) {
            this.val = val;
        }
    }

    public ListNode head;
    public ListNode tail;
    public int usedSize;

    public void add(int data) {
        ListNode toAdd = new ListNode(data);
        if (isEmpty()) {
            head = tail = toAdd;
            usedSize++;
            return;
        }
        tail.next = toAdd;
        toAdd.prev = tail;
        tail = tail.next;
        usedSize++;
    }

    public void remove(int data) {
        ListNode cur = head;
        if (isEmpty()) {
            return;
        }

        while (cur != null) {
            if (cur.val == data) {
                ListNode next = cur.next;
                cur.prev.next = next;
                next.prev = cur.prev;
                cur = cur.next;
            }
        }
    }

    public void display() {
        ListNode cur = head;
        while (cur != null) {
            System.out.print(cur.val+" ");
            cur = cur.next;
        }
        System.out.println();
    }

    public boolean isEmpty() {
        return usedSize == 0;
    }
}
