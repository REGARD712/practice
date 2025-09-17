class MyBinaryTree {
    static class TreeNode {
        public char val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(char val) {
            this.val = val;
        }
    }

    public TreeNode root;
    public int nodeSize;

    public TreeNode create() {
        TreeNode A = new TreeNode('A');
        TreeNode B = new TreeNode('B');
        TreeNode C = new TreeNode('C');
        TreeNode D = new TreeNode('D');
        TreeNode E = new TreeNode('E');
        TreeNode F = new TreeNode('F');
        TreeNode G = new TreeNode('G');
        TreeNode H = new TreeNode('H');

        A.left = B;
        A.right = C;
        B.left = D;
        B.right = E;
        C.left = F;
        C.right = G;
        D.left = H;

        return A;
    }

    public void preOrder(TreeNode root) {
        if (root == null) return;

        System.out.print(root.val + " ");
        preOrder(root.left);
        preOrder(root.right);
    }

    public void inOrder(TreeNode root) {
        if (root == null) return;

        inOrder(root.left);
        System.out.print(root.val + " ");
        inOrder(root.right);
    }

    public void postOrder(TreeNode root) {
        if (root == null) return;

        postOrder(root.left);
        postOrder(root.right);
        System.out.print(root.val + " ");
    }

    public int size(TreeNode root) {
        if (root == null) return 0;

        return size(root.left) + size(root.right) + 1;
    }

    public int leafNodeCount(TreeNode root) {
        if (root == null) return 0;

        if (root.left==null && root.right==null) return 1;

        return leafNodeCount(root.left) + leafNodeCount(root.right);
    }

    public int KthLevelNodeCount(TreeNode root, int k) {
        if (root == null) return 0;

        if (k == 1) return 1;

        return KthLevelNodeCount(root.left,k-1) + KthLevelNodeCount(root.right,k-1);
    }

    public TreeNode findVal(TreeNode root, char val) {
        if (root == null) return null;

        if (root.val == val) return root;

        TreeNode leftRet = findVal(root.left,val);
        if (leftRet != null) return leftRet;

        TreeNode rightRet = findVal(root.right,val);
        if (rightRet != null) return rightRet;

        return null;
    }

    public int getHeight(TreeNode root) {
        if (root == null) return 0;

        int leftHeight = getHeight(root.left);
        int rightHeight = getHeight(root.right);

        return Math.max(leftHeight,rightHeight) + 1;
    }
}

public class Test {
    public static void main(String[] args) {
        MyBinaryTree bt = new MyBinaryTree();

        MyBinaryTree.TreeNode root = bt.create();

        bt.preOrder(root);
        System.out.println();

        bt.inOrder(root);
        System.out.println();

        bt.postOrder(root);
        System.out.println();

        System.out.println(bt.size(root));
//        System.out.println(bt.leafNodeCount(root));
//        System.out.println(bt.KthLevelNodeCount(root,3));
        MyBinaryTree.TreeNode ret = bt.findVal(root,'G');
        System.out.println(ret.val);
    }
}
