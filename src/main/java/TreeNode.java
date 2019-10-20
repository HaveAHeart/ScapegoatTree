import java.util.Objects;

public class TreeNode<T extends Comparable> {
    private T value;
    private TreeNode<T> leftChild = null;
    private TreeNode<T> rightChild = null;

    //constructor, getters and setters
    public TreeNode(T value) {
        this.value = value;
    }
    public TreeNode<T> getRightChild() { return rightChild; }
    public void setRightChild(TreeNode<T> rightChild) { this.rightChild = rightChild; }

    public TreeNode<T> getLeftChild() { return leftChild; }
    public void setLeftChild(TreeNode<T> leftChild) { this.leftChild = leftChild; }

    public T getValue() { return value; }

    //overriding equals+hashcode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(value, treeNode.value) &&
                Objects.equals(leftChild, treeNode.leftChild) &&
                Objects.equals(rightChild, treeNode.rightChild);
    }
    @Override
    public int hashCode() {
        return Objects.hash(value, leftChild, rightChild);
    }

    public int getWeight() {
        int leftWeight = 0;
        if (leftChild != null) leftWeight = leftChild.getWeight();
        int rightWeight = 0;
        if (rightChild != null) rightWeight = rightChild.getWeight();
        return leftWeight + 1 + rightWeight;
    }

}
