package org.baderlab.csplugins.brainlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * HierarchicalClusteringResultTree structure capturing the output of a hierarchical clustering algorithm (for convenience)
 * Unique labels that correspond to the original data are stored in the tree and should be used to map back to the original data
 */
public class HierarchicalClusteringResultTree {
    public HierarchicalClusteringResultTree left; //left child
    public HierarchicalClusteringResultTree right; //right child
    public int nodeIndex; //node number for non leaves, original data index for leaves
    public double distance; //For internal node: the cluster distance these children were joined at
    public double leafOrderIndex; //For leaf node: the order to position the leaf at for output
    public boolean leaf; //true for leaf node, false for internal node
    public int numberOfLeaves; //For internal node: the number of leaves that this node is an ancestor of
    public String leafLabel;  //the label of a leaf (could be null)
    public List flatLeftChildrenLeaves; //a flat list of all leaves that are children of the left child node
    public List flatRightChildrenLeaves; //a flat list of all leaves that are children of the right child node
    public HierarchicalClusteringResultTree leftLeaf;   //index of the joined leaf under the left child
    public HierarchicalClusteringResultTree rightLeaf;  //index of the joined leaf under the right child

    /**
     * Create a leaf node
     *
     * @param leafIndex      The index of the leaf in the original data
     * @param leafOrderIndex The order the leaf should be drawn
     */
    public HierarchicalClusteringResultTree(int leafIndex, int leafOrderIndex, String label) {
        this.nodeIndex = leafIndex;
        this.distance = 0.0;
        this.leafOrderIndex = leafOrderIndex;
        leaf = true;
        this.numberOfLeaves = 1;
        this.leafLabel = label;

    }

    /**
     * Create an internal node
     *
     * @param left     The left child
     * @param right    The right child
     * @param nodeNum  The index of the node
     * @param distance The distance these nodes are joined at
     */
    public HierarchicalClusteringResultTree(HierarchicalClusteringResultTree left, HierarchicalClusteringResultTree right, int nodeNum, double distance) {
        if ((left == null) || (right == null)) {
            throw new RuntimeException("Null passed to tree constructor");
        }
        this.left = left;
        this.right = right;
        this.nodeIndex = nodeNum;
        this.distance = distance;
        //the leaf order index of an internal node is the average of its children
        this.leafOrderIndex = (left.leafOrderIndex + right.leafOrderIndex) / 2;
        leaf = false;
        this.numberOfLeaves += (left.numberOfLeaves + right.numberOfLeaves);
        //maintain a flat list of labels for
        flatLeftChildrenLeaves = new ArrayList();
        getListOfChildrenLeaves(left, flatLeftChildrenLeaves);
        flatRightChildrenLeaves = new ArrayList();
        getListOfChildrenLeaves(right, flatRightChildrenLeaves);
    }

    /**
     * Create an internal node
     *
     * @param left     The left child
     * @param right    The right child
     * @param nodeNum  The index of the node
     * @param distance The distance these nodes are joined at
     */
    public HierarchicalClusteringResultTree(HierarchicalClusteringResultTree left, HierarchicalClusteringResultTree right, int nodeNum, double distance,
                                            HierarchicalClusteringResultTree leftLeaf, HierarchicalClusteringResultTree rightLeaf) {
        if ((left == null) || (right == null)) {
            throw new RuntimeException("Null passed to tree constructor");
        }
        this.left = left;
        this.right = right;
        this.nodeIndex = nodeNum;
        this.distance = distance;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        //the leaf order index of an internal node is the average of its children
        this.leafOrderIndex = (left.leafOrderIndex + right.leafOrderIndex) / 2;
        leaf = false;
        this.numberOfLeaves += (left.numberOfLeaves + right.numberOfLeaves);
        //maintain a flat list of labels for
        flatLeftChildrenLeaves = new ArrayList();
        getListOfChildrenLeaves(left, flatLeftChildrenLeaves);
        flatRightChildrenLeaves = new ArrayList();
        getListOfChildrenLeaves(right, flatRightChildrenLeaves);
    }

    /**
     * Helper method to maintain a list of children leaves at each node
     *
     * @param child    The child to collect the leaves from
     * @param leafList The leaf list to maintain
     */
    private void getListOfChildrenLeaves(HierarchicalClusteringResultTree child, List leafList) {
        if (child.leaf) {
            leafList.add(child);
        } else {
            //this assumes that the tree is built from the leaves up towards the root
            leafList.addAll(child.flatLeftChildrenLeaves);
            leafList.addAll(child.flatRightChildrenLeaves);
        }
    }

    /**
     * Checks if two HierarchicalClusteringResultTree objects are equal.
     *
     * @return True if their children are equal (irrespective of direction of the children)
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HierarchicalClusteringResultTree)) return false;

        final HierarchicalClusteringResultTree that = (HierarchicalClusteringResultTree) obj;

        List objLeftChildren = that.flatLeftChildrenLeaves;
        List objRightChildren = that.flatRightChildrenLeaves;
        if (this.leaf == true && that.leaf == true) {
            //both leaves, ok to compare
            //leaves are equal if they have the same label (can't compare indices, since these are likely different
            //across different clusterings
            if (this.leafLabel == null || that.leafLabel == null) {
                throw new RuntimeException("Labels must be defined for the cluster results in order to determine equality.");
            }
            if (this.leafLabel.equalsIgnoreCase(that.leafLabel)) {
                return true;
            }
            return false;
        } else if (this.leaf == false && that.leaf == false) {
            //both internal nodes, ok to compare
            boolean leftEqual = false;
            boolean rightEqual = false;
            //check if left child is in other node
            if (this.flatLeftChildrenLeaves.containsAll(objLeftChildren) && (objLeftChildren.containsAll(this.flatLeftChildrenLeaves))) {
                leftEqual = true;
            } else
            if (this.flatLeftChildrenLeaves.containsAll(objRightChildren) && (objRightChildren.containsAll(this.flatLeftChildrenLeaves))) {
                leftEqual = true;
            }

            //check if right child is in other node
            if (this.flatRightChildrenLeaves.containsAll(objLeftChildren) && (objLeftChildren.containsAll(this.flatRightChildrenLeaves))) {
                rightEqual = true;
            } else
            if (this.flatRightChildrenLeaves.containsAll(objRightChildren) && (objRightChildren.containsAll(this.flatRightChildrenLeaves))) {
                rightEqual = true;
            }

            if (leftEqual && rightEqual) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a hashcode based on the labels present in each left and right child
     *
     */
    public int hashCode() {
        int result;
        String concatenatedLeafLabelsLeft = concatenateLeafLabelsInOrder(flatLeftChildrenLeaves);
        String concatenatedLeafLabelsRight = concatenateLeafLabelsInOrder(flatRightChildrenLeaves);
        result = (concatenatedLeafLabelsLeft != null ? concatenatedLeafLabelsLeft.hashCode() : 0);
        result = 31 * result + (concatenatedLeafLabelsRight != null ? concatenatedLeafLabelsRight.hashCode() : 0);
        return result;
    }

    /**
     * Helper method for hashCode - creates a concatenated list of ordered leaf labels
     */
    private String concatenateLeafLabelsInOrder(List childrenLeaves) {
        TreeSet sortedLabels = new TreeSet();
        StringBuffer sb = new StringBuffer();
        //sort the leaf labels
        for (int i = 0; i < childrenLeaves.size(); i++) {
            HierarchicalClusteringResultTree node = (HierarchicalClusteringResultTree) childrenLeaves.get(i);
            sortedLabels.add(node.leafLabel);
        }
        //concatenate the leaf labels in sorted order
        for (Iterator iterator = sortedLabels.iterator(); iterator.hasNext();) {
            String leafLabel = (String) iterator.next();
            sb.append(leafLabel);
        }
        return sb.toString();
    }
}
