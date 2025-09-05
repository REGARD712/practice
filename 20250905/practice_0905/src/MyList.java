public class MyList implements IList {
    static class ListNode {
        public int val;
        public ListNode next;

        public ListNode(int val) {
            this.val = val;
        }
    }

    public ListNode head;

    @Override
    public int size() {
        ListNode cur = head;
        int count = 0;
        while (cur != null) {
            count++;
            cur = cur.next;
        }
        return count;
    }

    @Override
    public void display() {
        ListNode cur = head;
        while (cur != null) {
            System.out.print(cur.val+" ");
            cur = cur.next;
        }
        System.out.println();
    }

    @Override
    public boolean contains(int data) {
        ListNode cur = head;
        while (cur != null) {
            if (cur.val == data) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    @Override
    public void addFirst(int data) {
        ListNode toAdd = new ListNode(data);
        if (head == null) {
            head = toAdd;
            return;
        }
        toAdd.next = head;
        head = toAdd;
    }

    @Override
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

    @Override
    public void addIndex(int index,int data) {
        int len = size();
        if (index<=0 && index>len) {
            throw new IllegalIndexException("Illegal index!!");
        }
        if (index == 0) {
            addFirst(data);
            return;
        }
        if (index == len) {
            addLast(data);
            return;
        }
        ListNode cur = head;
        ListNode toAdd = new ListNode(data);
        while (index-1 != 0) {
            cur = cur.next;
        }
        toAdd.next = cur.next;
        cur.next = toAdd;
    }

    @Override
    public void remove(int data) {
        if (head == null) {
            return;
        }
        if (head.val == data) {
            head = head.next;
            return;
        }
        ListNode cur = findNodeOf(data);
        ListNode next = cur.next;
        cur.next = next.next;
    }

    private ListNode findNodeOf(int data) {
        ListNode cur = head;
        while (cur != null) {
            if (cur.next.val == data) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    @Override
    public void removeAllData(int data) {
        if (head == null) {
            return;
        }
        ListNode cur = head;
        ListNode prev = head;
        while (cur != null) {
            if (cur.val == data) {
                prev.next = cur.next;
                cur = cur.next;
            } else {
                prev = cur;
                cur = cur.next;
            }
        }
        if (head.val == data) {
            head = head.next;
        }
    }

    @Override
    public void clear() {
        ListNode cur = head;
        while (cur != null) {
            ListNode next = cur.next;
            cur = null;
            cur = next;
        }
        head = null;
    }
}
