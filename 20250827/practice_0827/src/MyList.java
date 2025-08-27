public class MyList {
    static class ListNode {
        public int val;
        public ListNode next;

        public ListNode(int val) {
            this.val = val;
        }
    }

    public ListNode head;

    public int size() {
        ListNode cur = head;
        int count = 0;
        while (cur != null) {
            count++;
            cur = cur.next;
        }
        return count;
    }

    public void addLast(int data) {
        ListNode toAdd = new ListNode(data);
        if (head == null) {
            head = toAdd;
            return;
        }
        ListNode cur = head;
        while (cur.next != null) {
            cur = cur.next;
        }
        cur.next = toAdd;
    }

    public void display() {
        ListNode cur = head;
        while (cur != null) {
            System.out.print(cur.val+" ");
            cur = cur.next;
        }
        System.out.println();
    }

    public void reverse() {
        ListNode curNode = head.next;

        head.next = null;

        while (curNode != null) {
            ListNode nextNode = curNode.next;
            curNode.next = head;
            curNode = nextNode;
        }
    }

    public boolean isPalindrome() {
        if (head == null) {
            return true;
        }
        ListNode fast = head;
        ListNode slow = head;

        while (fast!=null && fast.next!=null) {      //找到中间节点,slow指向的节点就是中间节点
            fast = fast.next.next;
            slow = slow.next;
        }

        ListNode cur = slow.next;

        while (cur != null) {       //反转后半部分链表
            ListNode next = cur.next;
            cur.next = slow;
            slow = cur;
            cur = next;
        }

        while (head != slow) {             //判断回文:从前后两个方向(head和slow)往中间走并比较值是否相等
            if (head.val != slow.val) {
                return false;
            }
            if (head.next == slow) {       //偶数个时的处理
                return true;
            }
            head = head.next;
            slow = slow.next;
        }
        return true;
    }

    public boolean hasCycle() {
        ListNode fast = head;
        ListNode slow = head;

        while (fast!=null && fast.next!=null) {
            fast = fast.next.next;
            slow = slow.next;
            if (fast == slow) {
                return true;
            }
        }
        return false;
    }
}
