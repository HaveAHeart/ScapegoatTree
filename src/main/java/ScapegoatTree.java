import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Stack;

public class ScapegoatTree<T extends Comparable> {
    private TreeNode<T> root;
    private double alpha; //balance coefficient
    private int size;
    private int maxSize;


    //constructor and getter for root
    public ScapegoatTree(T value, double alpha) {
        if (alpha >= 0.5 && alpha < 1) this.alpha = alpha;
        else throw new IllegalArgumentException("alpha should be in [0.5 ; 1) range. Current alpha: " + alpha);
        this.root = new TreeNode<>(value);
        size = 1;
        maxSize = 1;
    }

    public TreeNode<T> getRoot() { return root; }

    public boolean contains(T value) { return search(value, root) != null; }

    private TreeNode<T> search(T value, TreeNode<T> currNode) {
        int compareVal = value.compareTo(currNode.getValue());
        //System.out.println("current search position is " + currNode.getValue());
        if (compareVal == 0) return currNode;
        else if (compareVal < 0) {
            if (currNode.getLeftChild() == null) return null;
            //System.out.println("searching at the left of " + currNode.getValue());
            return search(value, currNode.getLeftChild());
        }
        else {
            if ((currNode.getRightChild() == null)) return null;
            //System.out.println("searching at the right of " + currNode.getValue());
            return search(value, currNode.getRightChild());
        }
    }

    public int size() { return root.getWeight(); }

    //getting the subtree as the sorted ArrayList<T>
    public void getSubtreeAsList(boolean includeCurr, TreeNode<T> node, ArrayList<T> result) {
        //smaller values
        if (node.getLeftChild() != null) getSubtreeAsList(true, node.getLeftChild(), result);
        //this value
        if (includeCurr) result.add(node.getValue());
        //greater values
        if (node.getRightChild() != null) getSubtreeAsList(true, node.getRightChild(), result);
    }

    //node insertion - if the inserted element breaks the balance of the tree - look for the scapegoat and rebuild it
    private void recursiveIns(ArrayList<T> values, int start, int end, TreeNode<T> currNode) {
        if (start < 0 || end > values.size() - 1 || start > end) return;
        if (start == end) {
            //recursion exit
            int compareVal = values.get(start).compareTo(currNode.getValue());
            TreeNode<T> newNode = new TreeNode<>(values.get(start));

            if (compareVal <= 0) {
                currNode.setLeftChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as left child for " + currNode.getValue());
            } else {
                currNode.setRightChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as right child for " + currNode.getValue());
            }
        }
        else {
            int medianInd = (start + end)/2;
            TreeNode<T> newNode = new TreeNode<>(values.get(medianInd));
            addAsChild(newNode, currNode);
            //System.out.println("calling recursive insertions: " + start + "-" + (medianInd -1) + " and " + (medianInd + 1) + "-" + end);
            recursiveIns(values, start, (medianInd - 1), newNode);
            recursiveIns(values, (medianInd + 1), end, newNode);
        }
    }

    private void findPath(TreeNode<T> node, TreeNode<T> currNode, Stack<TreeNode<T>> path) {
        if (currNode == null) throw new NoSuchElementException("no node for " + node.getValue().toString());
        int compareVal = node.getValue().compareTo(currNode.getValue());
        path.push(currNode);

        if (compareVal < 0 && currNode.getLeftChild() != null) {
            findPath(node, currNode.getLeftChild(), path);
        }
        else if (compareVal > 0 && currNode.getRightChild() != null) {
            findPath(node, currNode.getRightChild(), path);
        }
    }

    private void rebuild(boolean saveCurr, TreeNode<T> node, Stack<TreeNode<T>> path) {
        //System.out.println("rebuilding for scapegoat " + node.getValue());
        ArrayList<T> subtreeArr = new ArrayList<>();

        getSubtreeAsList(saveCurr, node, subtreeArr);
        //System.out.println("subtree is ");
        //subtreeArr.forEach(System.out::println);

        int medianInd = (subtreeArr.size() - 1)/2;

        if (node == root) {
            //when the scapegoat is a root, we can not get the parent node
            root = new TreeNode<>(subtreeArr.get(medianInd));
            //System.out.println("new root is " + root.getValue());
            recursiveIns(subtreeArr, 0, medianInd - 1, root);
            recursiveIns(subtreeArr, medianInd + 1, subtreeArr.size() - 1, root);
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
            recursiveIns(subtreeArr, 0, (medianInd - 1), newScapeGoat);
            recursiveIns(subtreeArr, (medianInd + 1), subtreeArr.size() - 1, newScapeGoat);
        }
    }

    public void add(T value) {
        Stack<TreeNode<T>> path = new Stack<>();
        addAsChild(new TreeNode<>(value), root, path);
        size++;
        if (size > maxSize) maxSize = size;

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
    }

    private void addAsChild(TreeNode<T> newNode, TreeNode<T> currNode) {
        if (newNode.getValue().compareTo(currNode.getValue()) <= 0) {
            if (currNode.getLeftChild() == null) {
                currNode.setLeftChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as left child for " + currNode.getValue());
            }
            else addAsChild(newNode, currNode.getLeftChild());
        }
        else {
            if (currNode.getRightChild() == null) {
                currNode.setRightChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as right child for " + currNode.getValue());
            }
            else addAsChild(newNode, currNode.getRightChild());
        }
    }

    private void addAsChild(TreeNode<T> newNode, TreeNode<T> currNode, Stack<TreeNode<T>> currPath) {
        currPath.push(currNode);
        if (newNode.getValue().compareTo(currNode.getValue()) <= 0) {
            if (currNode.getLeftChild() == null) {
                currNode.setLeftChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as left child for " + currNode.getValue());
            }
            else addAsChild(newNode, currNode.getLeftChild(), currPath);
        }
        else {
            if (currNode.getRightChild() == null) {
                currNode.setRightChild(newNode);
                //System.out.println("value " + newNode.getValue() + " added as right child for " + currNode.getValue());
            }
            else addAsChild(newNode, currNode.getRightChild(), currPath);
        }
    }

    //node removal - rebalance all the subtree, or even all the tree, if currSize < alpha * lastRebalanceSize
    public void remove(T value) {
        TreeNode<T> removingNode = search(value, root);

        if (removingNode == null)
            throw new NoSuchElementException("there is no such value in the tree: " + value.toString());
        if (removingNode == root && this.size == 1)
            throw new IllegalArgumentException("last element of tree can not be removed");
        //System.out.println("removing " + removingNode.getValue());
        Stack<TreeNode<T>> path = new Stack<>();
        findPath(removingNode, root, path);
        path.pop();
        rebuild(false, removingNode, path);
        size--;

        if (size < maxSize * alpha) {
            rebuild(true, root, new Stack<>());
            maxSize = size;
        }
    }



}
