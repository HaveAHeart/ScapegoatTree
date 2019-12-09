import javax.swing.*;
import java.awt.*;
import java.util.*;

import static java.lang.Integer.max;

public class ScapegoatTree<T extends Comparable> implements Set{
    private TreeNode<T> root;
    private double alpha; //balance coefficient
    private int size;
    private Class classOfT;

    //constructor and getter for root

    public ScapegoatTree(T value, double alpha) {
        if (alpha >= 0.5 && alpha < 1) this.alpha = alpha;
        else throw new IllegalArgumentException("alpha should be in [0.5 ; 1) range. Current alpha: " + alpha);
        this.root = new TreeNode<>(value);
        size = 1;
        classOfT = value.getClass();
    }

    public TreeNode<T> getRoot() { return root; }

    public int getMaxHeight(TreeNode<T> node) {
        if (node == null) return 0;
        else return 1 + max(getMaxHeight(node.getLeftChild()), getMaxHeight(node.getRightChild()));
    }

    public int size() { return size; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScapegoatTree<?> that = (ScapegoatTree<?>) o;
        return Double.compare(that.alpha, alpha) == 0 &&
                size == that.size &&
                Objects.equals(root, that.root) &&
                Objects.equals(classOfT, that.classOfT);
    }

    @Override
    public int hashCode() { return Objects.hash(root, size, classOfT); }

    @Override
    public boolean isEmpty() { return root == null; }

    @Override
    public boolean contains(Object o) {
        if (o == null || root == null) return false;
        if (!classOfT.equals(o.getClass())) return false;
        T searchValue = (T) o;
        return root.search(searchValue) != null;
    }



    @Override
    public Iterator iterator() {
        return new Iterator() {
            ArrayDeque<TreeNode<T>> currQueue = new ArrayDeque<>();
            TreeNode<T> currNode;
            T currValue;

            @Override
            public boolean hasNext() {
                if (root == null) return false;
                if (currNode == null) return true;
                return !currQueue.isEmpty();
            }

            @Override
            public Object next() {
                if (currNode == null) {
                    currNode = root;
                    currValue = currNode.getValue();
                    if (currNode.getRightChild() != null) currQueue.add(currNode.getRightChild());
                    if (currNode.getLeftChild() != null) currQueue.add(currNode.getLeftChild());
                    return currValue;
                }
                currNode = currQueue.pop();
                currValue = currNode.getValue();
                if (currNode.getRightChild() != null) currQueue.add(currNode.getRightChild());
                if (currNode.getLeftChild() != null) currQueue.add(currNode.getLeftChild());
                return currValue;
            }
        };

    }

    @Override
    public Object[] toArray() {
        ArrayList<T> values = new ArrayList<>();
        root.getSubtreeAsList(true, values);
        return values.toArray();
    }

    @Override
    public boolean add(Object o) {
        if (o == null) return false;
        if (!classOfT.equals(o.getClass())) return false;
        if (this.contains(o)) return false;

        T addValue = (T) o;
        if (root == null) {
            root = new TreeNode<>(addValue);
            return true;
        }

        ArrayDeque<TreeNode<T>> path = new ArrayDeque<>();

        root.addAsChild(new TreeNode<>(addValue), path);
        size++;

        while (!path.isEmpty()) {
            TreeNode<T> node = path.pop();
            double currAlpWeight = node.getWeight() * alpha;
            double rightWeight = 0.0;
            double leftWeight = 0.0;
            if (node.getRightChild() != null) rightWeight = node.getRightChild().getWeight();
            if (node.getLeftChild() != null) leftWeight = node.getLeftChild().getWeight();
            if (rightWeight > currAlpWeight || leftWeight > currAlpWeight){
                rebuild(true, node, path); //Scapegoat found - balance time!
                break;
            }
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) return false;
        if (!classOfT.equals(o.getClass())) return false;
        if (!this.contains(o)) return false;
        T removeValue = (T) o;

        TreeNode<T> removingNode = root.search(removeValue);
        if (removingNode == root && this.size == 1) {
            root = null;
            return true;
        }

        ArrayDeque<TreeNode<T>> path = new ArrayDeque<>();
        root.findPath(removingNode, path);
        rebuild(false, removingNode, path);
        size--;

        rebuild(true, root, new ArrayDeque<>());

        return true;
    }

    @Override
    public boolean addAll(Collection c) {
        boolean isSuccessful = true;
        for (Object element : c) isSuccessful = this.add(element);
        return isSuccessful;
    }

    @Override
    public void clear() { root = null; }

    @Override
    public boolean removeAll(Collection c) {
        boolean isSuccessful = true;
        for (Object element : c) isSuccessful = this.remove(element);
        return isSuccessful;
    }

    @Override
    public boolean retainAll(Collection c) {
        boolean isSuccessful = true;
        for (Object element : this) if (!c.contains(element)) isSuccessful = this.remove(element);
        return isSuccessful;
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object element : c) if (!this.contains(element)) return false;
        return true;
    }

    @Override
    public Object[] toArray(Object[] a) {
        if (a.length > size) return this.toArray();
        ArrayList<T> values = new ArrayList<>();
        root.getSubtreeAsList(true, values);
        a = values.toArray();
        return a;
    }

    private void rebuild(boolean saveCurr, TreeNode<T> node, ArrayDeque<TreeNode<T>> path) {
        //System.out.println("rebuilding for scapegoat " + node.getValue());
        ArrayList<T> subtreeArr = new ArrayList<>();

        node.getSubtreeAsList(saveCurr, subtreeArr);
        //System.out.println("subtree is ");
        //subtreeArr.forEach(System.out::println);

        int medianInd = (subtreeArr.size() - 1)/2;

        if (node == root) {
            //when the scapegoat is a root, we can not get the parent node
            root = new TreeNode<>(subtreeArr.get(medianInd));
            //System.out.println("new root is " + root.getValue());
            root.recursiveIns(subtreeArr, 0, medianInd - 1);
            root.recursiveIns(subtreeArr, medianInd + 1, subtreeArr.size() - 1);
        }
        else {
            //parent node is the next node in the path we followed
            TreeNode<T> parentNode = path.pop();
            //System.out.println("parent node is " + parentNode.getValue());
            //removal part - if we need to remove an element with no children
            if (subtreeArr.size() == 0 && !saveCurr) {
                if (node.getValue().compareTo(parentNode.getValue()) <= 0) parentNode.setLeftChild(null);
                else parentNode.setRightChild(null);
                return;
            }
            //
            TreeNode<T> newScapeGoat = new TreeNode<>(subtreeArr.get(medianInd));
            if (newScapeGoat.getValue().compareTo(parentNode.getValue()) <= 0) parentNode.setLeftChild(newScapeGoat);
            else parentNode.setRightChild(newScapeGoat);
            //System.out.println("new scapegoat is " + newScapeGoat.getValue());
            newScapeGoat.recursiveIns(subtreeArr, 0, (medianInd - 1));
            newScapeGoat.recursiveIns(subtreeArr, (medianInd + 1), subtreeArr.size() - 1);
        }
    }

}
