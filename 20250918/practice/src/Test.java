public class Test {
    static class TreeNode {
        char val;
        TreeNode left;
        TreeNode right;

        public TreeNode(char val) {
            this.val = val;
        }
    }

    TreeNode root;
    public boolean isSubtree(TreeNode root, TreeNode subRoot) {
        if (root == null) return false;

        if (isSameTree(root,subRoot)) return true;
        if (isSubtree(root.left,subRoot)) return true;
        if (isSubtree(root.right,subRoot)) return true;

        return false;
    }

    public boolean isSameTree(TreeNode p, TreeNode q) {
        // 判断结构是否一样
        if ((p!=null && q==null) || (p==null && q!=null)) {
            return false;
        }
        // 判断节点是否都为空
        if (p==null && q==null) {
            return true;
        }
        // 判断结点的值是否一样
        if (p.val != q.val) {
            return false;
        }
        // 若结构一样且值一样，就检查左右子节点
        return isSameTree(p.left,q.left)
                && isSameTree(p.right,q.right);
    }
}
