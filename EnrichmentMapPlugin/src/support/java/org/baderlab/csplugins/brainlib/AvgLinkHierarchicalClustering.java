package org.baderlab.csplugins.brainlib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Copyright (c) 2005 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: GaryBader
 * * Date: Aug 4, 2005
 * * Time: 5:59:00 PM
 */

/**
 * Implements average linkage hierarchical clustering
 */
public class AvgLinkHierarchicalClustering {
    /**
     * result: The clustering solution. Each row in the matrix describes one linking event,
     * with the two columns containing the integer identifier of the nodes that were joined.
     * The original elements are numbered 0..nelements-1, nodes are numbered
     * -1..-(nelements-1).
     */
    protected int[][] result = null;
    /**
     * linkDistance: For each node, the distance between the two subnodes that were joined. The
     * number of nodes is equal to the number of items clustered minus one.
     */
    protected double[] linkDistance = null;
    protected DistanceMatrix distanceMatrix = null;
    protected int nelements = 0;    //number of elements in the distance matrix
    protected int[] leafOrder; //stores the order of the final output of the matrix - filled during clustering
    protected boolean optimalLeafOrdering = true; //by default, use optimal leaf ordering (otherwise heuristic is used)
    protected boolean singleLinkage = false;    //by default, use average linkage clustering
    protected ArrayList labelHighlight; //a list of LabelColorPair objects

    
    public final static String LEAF_ORDERING_BARJOSEPH2003 = "Bar-Joseph";

    /**
     * linkedLeaves: pairs of leaves linked at each node of the result. Each row in this matrix
     * describes one linking event, with the 2 columns containing the integer identifier of the leaves
     * that were joined.
     */
    protected int[][] linkedLeaves = null;

    /**
     * @param distanceMatrix The distance matrix, with zeros along the diagonal.
     *                       The distance matrix is a ragged array containing the distances
     *                       As the distance matrix is symmetric, with zeros on the diagonal, only the
     *                       lower triangular half of the distance matrix is saved.
     *                       Distances must be normalized to 0..1 since these methods
     *                       need to convert from distances to similarities occasionally.
     */
    public AvgLinkHierarchicalClustering(DistanceMatrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        nelements = distanceMatrix.getMatrixDimension();
        result = new int[nelements - 1][2];
        linkedLeaves = new int[nelements - 1][2];
        linkDistance = new double[nelements];
        leafOrder = new int[nelements];
    }

    /**
     * Get the String label of the clustered element at original index 'elementIndex'
     *
     * @param elementIndex The index of the element in the original distance matrix used to create the clustering
     * @return The String element label
     */
    public String getLabel(int elementIndex) {
        String elementLabel = (String) distanceMatrix.getLabels().get(elementIndex);
        return elementLabel;
    }

    /**
     * Private class to hold a pair of integers for distance matrix lookup
     */
    private class Pair {
        public int i, j;

        public Pair() {
            this.i = 0;
            this.j = 0;
        }
    }

    /**
     * This function searches the distance matrix to find the pair with the shortest
     * distance between them. The indices of the pair are returned in pair; the
     * distance itself is returned by the function.
     *
     * @param pair index i and j of the pair stored in a Pair object
     * @return The shortest distance found
     */
    private double findClosestPair(int nNodes, Pair pair, DistanceMatrix dmInternal) {
        double distance = dmInternal.getValue(1, 0);
        for (int i = 0; i < nNodes; i++) {
            for (int j = 0; j < i; j++) {
                if (dmInternal.getValue(i, j) < distance) {
                    distance = dmInternal.getValue(i, j);
                    pair.i = i;
                    pair.j = j;
                }
            }
        }
        //if closest pair not found in loop, the 1,0 is the closest pair and this should be captured outside of this call
        return distance;
    }

    /**
     * Performs hierarchical clustering using the distance matrix provided in the constructor
     * (average linkage clustering)
     * This clustering code ported to Java from Michiel de Hoon's open source clustering library
     * http://bonsai.ims.u-tokyo.ac.jp/%7Emdehoon/software/software.html
     */
    public void run() {
        /* Keep track of the number of elements in each cluster
        * (needed to calculate the average) */
        int[] number = new int[nelements];
        /* Setup a list specifying to which cluster a gene belongs */
        int[] clusterid = new int[nelements];
        for (int j = 0; j < nelements; j++) {
            number[j] = 1;
            clusterid[j] = j;
        }

        //copy the distance matrix so that the original matrix is not affected by this method
        DistanceMatrix dmInternal = distanceMatrix.copy();

        //hierarchical clustering step
        Pair pair = new Pair();
        for (int nNodes = nelements; nNodes > 1; nNodes--) {
            pair.i = 1; //set to start coordinates of the search in findClosestPair
            pair.j = 0;
            linkDistance[nelements - nNodes] = findClosestPair(nNodes, pair, dmInternal);
            int isaved = pair.i;
            int jsaved = pair.j;
            int sum = 0;

            /* Save linked leaves from the closest pair */
            linkedLeaves[nelements - nNodes][0] = pair.i;
            linkedLeaves[nelements - nNodes][1] = pair.j;

            /* Save result */
            result[nelements - nNodes][0] = clusterid[isaved];
            result[nelements - nNodes][1] = clusterid[jsaved];

            if (!singleLinkage) {

                /* Update the distances - average linkage */
                sum = number[isaved] + number[jsaved];
                for (int j = 0; j < jsaved; j++) {
                    dmInternal.setValue(jsaved, j, dmInternal.getValue(isaved, j) * number[isaved]
                            + dmInternal.getValue(jsaved, j) * number[jsaved]);
                    dmInternal.setValue(jsaved, j, dmInternal.getValue(jsaved, j) / sum);
                }
                for (int j = jsaved + 1; j < isaved; j++) {
                    dmInternal.setValue(j, jsaved, dmInternal.getValue(isaved, j) * number[isaved]
                            + dmInternal.getValue(j, jsaved) * number[jsaved]);
                    dmInternal.setValue(j, jsaved, dmInternal.getValue(j, jsaved) / sum);
                }
                for (int j = isaved + 1; j < nNodes; j++) {
                    dmInternal.setValue(j, jsaved, dmInternal.getValue(j, isaved) * number[isaved]
                            + dmInternal.getValue(j, jsaved) * number[jsaved]);
                    dmInternal.setValue(j, jsaved, dmInternal.getValue(j, jsaved) / sum);
                }

                for (int j = 0; j < isaved; j++)
                    dmInternal.setValue(isaved, j, dmInternal.getValue(nNodes - 1, j));
                for (int j = isaved + 1; j < nNodes - 1; j++)
                    dmInternal.setValue(j, isaved, dmInternal.getValue(nNodes - 1, j));

            }

            /* Update number of elements in the clusters */
            number[jsaved] = sum;
            number[isaved] = number[nNodes - 1];

            /* Update clusterids */
            clusterid[jsaved] = nNodes - nelements - 1;
            clusterid[isaved] = clusterid[nNodes - 1];
        }

        dmInternal = null; //free this potentially large matrix

        //Create an ordering for the leaves
        if (optimalLeafOrdering) {
            orderLeavesBarJoseph2003(result, distanceMatrix, leafOrder);
        } else {
            orderLeavesEisenHeuristic(leafOrder);
        }
    }

    public void old_run() {
        /* Keep track of the number of elements in each cluster
        * (needed to calculate the average) */
        int[] number = new int[nelements];
        /* Setup a list specifying to which cluster a gene belongs */
        int[] clusterid = new int[nelements];
        for (int j = 0; j < nelements; j++) {
            number[j] = 1;
            clusterid[j] = j;
        }

        //copy the distance matrix so that the original matrix is not affected by this method
        DistanceMatrix dmInternal = distanceMatrix.copy();

        //hierarchical clustering step
        Pair pair = new Pair();
        for (int nNodes = nelements; nNodes > 1; nNodes--) {
            pair.i = 1; //set to start coordinates of the search in findClosestPair
            pair.j = 0;
            linkDistance[nelements - nNodes] = findClosestPair(nNodes, pair, dmInternal);
            int isaved = pair.i;
            int jsaved = pair.j;
            int sum = 0;

            /* Save result */
            result[nelements - nNodes][0] = clusterid[isaved];
            result[nelements - nNodes][1] = clusterid[jsaved];

            /* Update the distances - average linkage */
            sum = number[isaved] + number[jsaved];
            for (int j = 0; j < jsaved; j++) {
                dmInternal.setValue(jsaved, j, dmInternal.getValue(isaved, j) * number[isaved]
                        + dmInternal.getValue(jsaved, j) * number[jsaved]);
                dmInternal.setValue(jsaved, j, dmInternal.getValue(jsaved, j) / sum);
            }
            for (int j = jsaved + 1; j < isaved; j++) {
                dmInternal.setValue(j, jsaved, dmInternal.getValue(isaved, j) * number[isaved]
                        + dmInternal.getValue(j, jsaved) * number[jsaved]);
                dmInternal.setValue(j, jsaved, dmInternal.getValue(j, jsaved) / sum);
            }
            for (int j = isaved + 1; j < nNodes; j++) {
                dmInternal.setValue(j, jsaved, dmInternal.getValue(j, isaved) * number[isaved]
                        + dmInternal.getValue(j, jsaved) * number[jsaved]);
                dmInternal.setValue(j, jsaved, dmInternal.getValue(j, jsaved) / sum);
            }

            for (int j = 0; j < isaved; j++)
                dmInternal.setValue(isaved, j, dmInternal.getValue(nNodes - 1, j));
            for (int j = isaved + 1; j < nNodes - 1; j++)
                dmInternal.setValue(j, isaved, dmInternal.getValue(nNodes - 1, j));

            /* Update number of elements in the clusters */
            number[jsaved] = sum;
            number[isaved] = number[nNodes - 1];

            /* Update clusterids */
            clusterid[jsaved] = nNodes - nelements - 1;
            clusterid[isaved] = clusterid[nNodes - 1];
        }

        dmInternal = null; //free this potentially large matrix

        //Create an ordering for the leaves
        if (optimalLeafOrdering) {
            orderLeavesBarJoseph2003(result, distanceMatrix, leafOrder);
        } else {
            orderLeavesEisenHeuristic(leafOrder);
        }
    }

    /**
     * Takes hierarchical clustering output and divides the elements in the tree structure
     * into clusters. The number of clusters is specified by the user.
     * <p/>
     * This clustering code ported to Java from Michiel de Hoon's open source clustering library
     * http://bonsai.ims.u-tokyo.ac.jp/%7Emdehoon/software/software.html
     *
     * @param nclusters The number of clusters to be formed. Ranges from 1..nelements.
     * @return int[nelements] - The number of the cluster to which each element was assigned.
     */
    public int[] cutTree(int nclusters) {
        int i, j, k;
        int icluster = 0;
        int n = nelements - nclusters; /* number of nodes to join */
        int[] nodeid;
        int clusterid[] = new int[nelements];
        /* Check the tree */
        boolean flag = false;
        if (nclusters > nelements || nclusters < 1) flag = true;
        for (i = 0; i < nelements - 1; i++) {
            if (result[i][0] >= nelements || result[i][0] < -i ||
                    result[i][1] >= nelements || result[i][1] < -i) {
                flag = true;
                break;
            }
        }
        /* Assign all elements to cluster -1 and return if an error is found. */
        if (flag) {
            for (i = 0; i < nelements; i++) clusterid[i] = -1;
            return null;
        }
        /* The tree array is safe to use. */
        for (i = nelements - 2; i >= n; i--) {
            k = result[i][0];
            if (k >= 0) {
                clusterid[k] = icluster;
                icluster++;
            }
            k = result[i][1];
            if (k >= 0) {
                clusterid[k] = icluster;
                icluster++;
            }
        }
        nodeid = new int[n];
        for (i = 0; i < n; i++) nodeid[i] = -1;
        for (i = n - 1; i >= 0; i--) {
            if (nodeid[i] < 0) {
                j = icluster;
                nodeid[i] = j;
                icluster++;
            } else
                j = nodeid[i];
            k = result[i][0];
            if (k < 0) nodeid[-k - 1] = j;
            else clusterid[k] = j;
            k = result[i][1];
            if (k < 0) nodeid[-k - 1] = j;
            else clusterid[k] = j;
        }
        return clusterid;
    }

    /**
     * Checks if optimal leaf ordering is turned on (this is the default)
     *
     */
    public boolean isOptimalLeafOrdering() {
        return optimalLeafOrdering;
    }

    /**
     * If true (default), uses Bar-Joseph 2003 optimal leaf ordering
     * If false, uses Eisen heuristic leaf ordering
     *
     * @param optimalLeafOrdering
     */
    public void setOptimalLeafOrdering(boolean optimalLeafOrdering) {
        this.optimalLeafOrdering = optimalLeafOrdering;
    }

    public void setLeafOrderingMethod(String leafOrderingMethod) {
        if (leafOrderingMethod == LEAF_ORDERING_BARJOSEPH2003) {
            this.optimalLeafOrdering = true;
        }
        else {
            this.optimalLeafOrdering = false;
        }

    }

    public boolean isSingleLinkage() {
        return singleLinkage;
    }
    public void setSingleLinkage(boolean flag) {
        singleLinkage = flag;
    }

    /**
     * Returns the clustering solution as a HierarchicalClusteringResultTree object.
     * Note: Each time this method is called, a HierarchicalClusteringResultTree
     * is created from more efficient internal data structures.
     *
     * @return The root of a tree describing the clustering solution
     */
    public HierarchicalClusteringResultTree getResult() {
        return convertResultTreeToTreeClass(result, linkDistance, leafOrder);
    }

    /** 
     * Convert hierarchical clustering result tree to HierarchicalClusteringResultTree
     * Note: converts from the leaves up towards the root
     */
    private HierarchicalClusteringResultTree convertResultTreeToTreeClass(int[][] resultTree, double[] linkDistance, int[] leafOrder) {
        //save the leaf order list for convenient searching
        ArrayList leafOrderList = new ArrayList();
        for (int i = 0; i < leafOrder.length; i++) {
            int index = leafOrder[i];
            leafOrderList.add(new Integer(index));
        }

        List<String> clusteredElementLabels = distanceMatrix.getLabels();

        /**
         * resultTree: Each row in the matrix describes one linking event,
         * with the two columns containing the name of the nodes that were joined.
         * The original elements are numbered 0..nelements-1, nodes are numbered
         * -1..-(nelements-1).
         */
        HierarchicalClusteringResultTree t = null; //root
        HierarchicalClusteringResultTree[] internalNodeList = new HierarchicalClusteringResultTree[resultTree.length + 1]; //one for each internal node (node index starts at 1)
        for (int i = 0; i < resultTree.length; i++) {
            HierarchicalClusteringResultTree tleft = null;
            HierarchicalClusteringResultTree tright = null;
            //left
            if (resultTree[i][0] >= 0) { //leaf
                tleft = new HierarchicalClusteringResultTree(resultTree[i][0], leafOrderList.indexOf(new Integer(resultTree[i][0])),
                        (String) clusteredElementLabels.get(resultTree[i][0]));
            } else { //node
                tleft = internalNodeList[-resultTree[i][0]];
            }
            //right
            if (resultTree[i][1] >= 0) { //leaf
                tright = new HierarchicalClusteringResultTree(resultTree[i][1], leafOrderList.indexOf(new Integer(resultTree[i][1])),
                        (String) clusteredElementLabels.get(resultTree[i][1]));
            } else { //node
                tright = internalNodeList[-resultTree[i][1]];
            }
            HierarchicalClusteringResultTree leftLeaf = new HierarchicalClusteringResultTree(linkedLeaves[i][0], leafOrderList.indexOf(new Integer(linkedLeaves[i][0])),
                    (String) clusteredElementLabels.get(linkedLeaves[i][0]));
            HierarchicalClusteringResultTree rightLeaf = new HierarchicalClusteringResultTree(linkedLeaves[i][1], leafOrderList.indexOf(new Integer(linkedLeaves[i][1])),
                    (String) clusteredElementLabels.get(linkedLeaves[i][1]));

            t = internalNodeList[i + 1] = new HierarchicalClusteringResultTree(tleft, tright, i + 1, linkDistance[i], leftLeaf, rightLeaf);
        }
        return t;
    }

    /**
     * Gets the number of elements that were clustered
     */
    public int getNelements() {
        return nelements;
    }

    /**
     * Return the maximum distance found in the clustering result
     */
    public double getMaxDistance() {
        double maxDistance = 0.0;

        for (int i = 0; i < linkDistance.length; i++) {
            maxDistance = Math.max(maxDistance, linkDistance[i]);
        }

        return maxDistance;
    }

    /**
     * Return the order to output the leaves
     * Array values are leaf indices, so i..length provides the order of leaf indices
     */
    public int[] getLeafOrder() {
        return leafOrder;
    }

    /**
     * Order the leaves on the tree in a reasonable order. This imposes an ordering on the rows of the
     * distance matrix. (Used for the Eisen heuristic leaf ordering)
     *
     * @param order       Original order of elements
     * @param nodeorder   Order of nodes
     * @param nodecounts  Number of elements per node (cluster)
     * @param NodeElement Representation of tree - each row represents a node and contains 2 children
     * @param leafOrder   Will store the output order for the leaves
     */
    private void treeSort(double[] order, double[] nodeorder, int[] nodecounts, int NodeElement[][], int[] leafOrder) {
        int nNodes = nelements - 1;
        double[] neworder = new double[nelements]; /* initialized to 0.0 */
        int[] clusterids = new int[nelements];
        for (int i = 0; i < nelements; i++) {
            clusterids[i] = i;
        }
        for (int i = 0; i < nNodes; i++) {
            int i1 = NodeElement[i][0];
            int i2 = NodeElement[i][1];
            double order1 = (i1 < 0) ? nodeorder[-i1 - 1] : order[i1];
            double order2 = (i2 < 0) ? nodeorder[-i2 - 1] : order[i2];
            int count1 = (i1 < 0) ? nodecounts[-i1 - 1] : 1;
            int count2 = (i2 < 0) ? nodecounts[-i2 - 1] : 1;
            /* If order1 and order2 are equal, their order is determined by
            * the order in which they were clustered */
            if (i1 < i2) {
                double increase = (order1 < order2) ? count1 : count2;
                for (int j = 0; j < nelements; j++) {
                    int clusterid = clusterids[j];
                    if (clusterid == i1 && order1 >= order2) neworder[j] += increase;
                    if (clusterid == i2 && order1 < order2) neworder[j] += increase;
                    if (clusterid == i1 || clusterid == i2) clusterids[j] = -i - 1;
                }
            } else {
                double increase = (order1 <= order2) ? count1 : count2;
                for (int j = 0; j < nelements; j++) {
                    int clusterid = clusterids[j];
                    if (clusterid == i1 && order1 > order2) neworder[j] += increase;
                    if (clusterid == i2 && order1 <= order2) neworder[j] += increase;
                    if (clusterid == i1 || clusterid == i2) clusterids[j] = -i - 1;
                }
            }
        }

        //keep track of how to output the final matrix so that it is this order
        sort(neworder, leafOrder);

        return;
    }

    /**
     * Sets up an index table given the data, such that data[index[]] is in
     * increasing order. The array data is unchanged.
     */
    private void sort(double data[], int index[]) {
        /*this is like a 2-column sort in Excel, where one colomn defines the sort and then the other
        column contains the order you want*/
        class DataIndexPair implements Comparable {
            public double data;
            public int index;

            public DataIndexPair(double data, int index) {
                this.data = data;
                this.index = index;
            }

            public int compareTo(Object o) {
                DataIndexPair that = (DataIndexPair) o;
                return (int) (this.data - that.data);
            }
        }
        TreeSet indexValue = new TreeSet();
        for (int i = 0; i < data.length; i++) {
            DataIndexPair dataIndexDataIndexPair = new DataIndexPair(data[i], i);
            indexValue.add(dataIndexDataIndexPair);
        }
        //values are sorted by keys
        Iterator values = indexValue.iterator();
        int i = 0;
        while (values.hasNext()) {
            DataIndexPair dataIndexPair = (DataIndexPair) values.next();
            index[i] = dataIndexPair.index;
            i++;
        }
    }

    /**
     * Orders leaf nodes according to a heuristic
     * Note: final ordering highly dependent on input ordering
     * <p/>
     * Ported from Michael Eisen's Cluster software code
     * http://rana.lbl.gov/EisenSoftware.htm
     *
     * @param leafOrder
     */
    private void orderLeavesEisenHeuristic(int[] leafOrder) {
        int nNodes = nelements - 1;
        double[] nodeorder = new double[nNodes];
        int[] nodecounts = new int[nNodes]; //number of elements in a node(cluster)
        //order of elements to cluster - this is just the index of the elements of the DistanceMatrix
        double[] origOrder = new double[nelements];  //order of elements in the distance matrix
        for (int i = 0; i < origOrder.length; i++) {
            origOrder[i] = i;
        }

        for (int i = 0; i < nNodes; i++) {
            int min1 = result[i][0];
            int min2 = result[i][1];
            /* min1 and min2 are the elements that are to be joined */
            double order1;
            double order2;
            int counts1;
            int counts2;
            if (min1 < 0) {
                int index1 = -min1 - 1;
                order1 = nodeorder[index1];
                counts1 = nodecounts[index1];
                linkDistance[i] = Math.max(linkDistance[i], linkDistance[index1]);
            } else {
                order1 = origOrder[min1];
                counts1 = 1;
            }
            if (min2 < 0) {
                int index2 = -min2 - 1;
                order2 = nodeorder[index2];
                counts2 = nodecounts[index2];
                linkDistance[i] = Math.max(linkDistance[i], linkDistance[index2]);
            } else {
                order2 = origOrder[min2];
                counts2 = 1;
            }

            nodecounts[i] = counts1 + counts2;
            nodeorder[i] = (counts1 * order1 + counts2 * order2)
                    / (counts1 + counts2);
        }

        /* Now set up order based on the tree structure */
        for (int i = 0; i < nelements; i++) {
            leafOrder[i] = i;      //geneindex should be nelements long - global and filled here
        }
        treeSort(origOrder, nodeorder, nodecounts, result, leafOrder);
    }

    //following code starting here is for optimal leaf ordering
    private final int rightTree = 2;
    private final int leftTree = 1;
    private final double maxAdd = 2;

    private class LeafPair {
        private int leftLeaf;
        private int rightLeaf;
        private LeafPair preLeft;
        private LeafPair preRight;
        private int n1, n2;

        // Pair for best tree construction
        public LeafPair(int l, int r, LeafPair pl, LeafPair pr, int t1, int t2) {
            leftLeaf = l;
            rightLeaf = r;
            preLeft = pl;
            preRight = pr;
            n1 = t1;
            n2 = t2;
        }
    }

    private class LeafDist implements Comparable {
        public int n;
        public double dist;
        public LeafPair best;

        public LeafDist(int to, double d, LeafPair p) {
            n = to;
            dist = d;
            best = p;
        }

        public int compareTo(Object o) {
            LeafDist ld = (LeafDist) o;
            //Changed the comparator.  In Java 7 compareTo method throws a  java.lang.IllegalArgumentException: Comparison method violates its general contract!
            //In Java 7 they changed the implementation of the compareto method cause this thrown exception
            //suggestions on web included forcing java to use the old implementation, checking for Nan values or changing the implementation.
            //Checking for Nan did not solve the problem.
            if(this.dist > ld.dist)
            	return 1;
            if(this.dist < ld.dist)
            	return -1;
            else
            	return 0;

            //return (int) (this.dist - ld.dist);
        }
    }

    private class Leaf {
        private int index;
        private LeafDist[] curDist, newDist;
        private double[][] distMat;
        private int listSize, newSize;
        public double bestNew;

        void setSize(int size) {
            listSize = size;
        }

        int giveSize() {
            return listSize;
        }

        LeafDist[] giveList() {
            return curDist;
        }

        void initNewSize(int nSize) {
            newDist = new LeafDist[nSize];
        }

        void initNewDist() {
            newDist = null;
        }

        int giveIndex() {
            return index;
        }

        // Each gene is assigned a leaf, initially it does not have
        // any pairing gene.
        public Leaf(int num, double[][] mat) {
            index = num;
            distMat = mat;
            LeafPair oneLeaf = new LeafPair(index, -1, null, null, 1, 0);
            curDist = new LeafDist[1];
            curDist[0] = new LeafDist(index, 0, oneLeaf);
            listSize = 1;
            newDist = null;
            newSize = 0;
            bestNew = Double.MAX_VALUE;
        }

        // replace previous pair list (from previous subtree) with
        // a new one from the current subtree
        void replace() {
            int i;
            // delete previous distance list
            for (i = 0; i < listSize; i++) {
                curDist[i] = null;
            }
            curDist = null;

            listSize = newSize;
            // bestNew becomes the maximum distance that may help in future
            // trees.
            bestNew += maxAdd;
            curDist = new LeafDist[listSize];
            for (i = 0; i < newSize; i++) {
                curDist[i] = newDist[i];
            }
            newDist = null;
            Arrays.sort(curDist);
            newSize = 0;
            bestNew = Double.MAX_VALUE;
        }

        // Adds corner to list of corners, and finds the best distance
        // with corner on the other side.
        void addToNew(Leaf[] corner1, Leaf[] corner2, int n1, int n2, int c1, int c2, double max1) {
            int fromNum, fromIndex;
            LeafDist[] fDist;
            Leaf curLeaf;
            int i, j;
            double[] bestC = new double[nelements + 1];
            double curVal, bestD;
            double bestPos;
            int[] bForC = new int[nelements + 1];
            int cForD = 0, dPlace = 0;
            double maxAC = Double.MAX_VALUE;
            for (j = 0; j < c1; j++) {
                fromIndex = corner1[j].giveIndex();
                bestC[fromIndex] = Double.MAX_VALUE;
                for (i = 0; i < listSize; i++) {
                    bestPos = curDist[i].dist + max1;
                    if (bestPos > bestC[fromIndex]) // optimization
                        i = listSize;
                    else {
                        curVal = curDist[i].dist + distMat[curDist[i].n][fromIndex];
                        if (curVal < bestC[fromIndex]) {
                            bestC[fromIndex] = curVal;
                            bForC[fromIndex] = i;
                        }
                    }
                }
                if (bestC[fromIndex] < maxAC)
                    maxAC = bestC[fromIndex];
            }

            for (j = 0; j < c2; j++) {
                bestD = Double.MAX_VALUE;
                curLeaf = corner2[j];
                fromNum = curLeaf.giveSize();
                fromIndex = curLeaf.giveIndex();
                fDist = curLeaf.giveList();
                for (i = 0; i < fromNum; i++) {
                    bestPos = curLeaf.curDist[i].dist + maxAC;
                    if (bestPos > bestD)
                        i = fromNum; // optimization
                    else {
                        curVal = bestC[fDist[i].n] + curLeaf.curDist[i].dist;
                        if (curVal < bestD) {
                            bestD = curVal;
                            cForD = curLeaf.curDist[i].n;
                            dPlace = i;
                        }
                    }
                }
                LeafPair newPair = new LeafPair(index, fromIndex,
                        curDist[bForC[cForD]].best, fDist[dPlace].best, n1, n2);
                newDist[newSize] = new LeafDist(fromIndex, bestD, newPair);
                newSize++;
                LeafPair cornerPair = new LeafPair(fromIndex, index,
                        fDist[dPlace].best, curDist[bForC[cForD]].best, n2, n1);
                curLeaf.addNewDist(index, bestD, cornerPair);
            }
            bForC = null;
            bestC = null;
        }

        // adds a new pair to the dist list
        void addNewDist(int n, double dist, LeafPair p) {
            newDist[newSize] = new LeafDist(n, dist, p);
            newSize++;
        }

        // the optimization for the last two subtrees, no need
        // to compute distance to all leaves, the best will suffice (see paper).
        int findLast(Leaf[] corner1, Leaf[] corner2, int n1, int n2) {
            LeafDist[] myDist;
            Leaf curLeaf;
            int i, j;
            double curVal, best = Double.MAX_VALUE;
            double myVal;
            int myInd = 0, bestIndl = 0, bestIndr = 0, mBestl = 0, mBestr = 0;
            LeafPair lpre = null, rpre = null;
            LeafDist[][] fDist = new LeafDist[n2][];
            for (i = 0; i < n2; i++) {
                fDist[i] = corner2[i].giveList();
            }
            for (j = 0; j < n1; j++) {
                curLeaf = corner1[j];
                myDist = curLeaf.giveList();
                myVal = myDist[0].dist;
                myInd = curLeaf.giveIndex();
                for (i = 0; i < n2; i++) {
                    curVal = myVal + fDist[i][0].dist + distMat[myInd][corner2[i].giveIndex()];
                    if (best > curVal) {
                        best = curVal;
                        bestIndl = myDist[0].n;
                        bestIndr = fDist[i][0].n;
                        mBestl = myInd;
                        mBestr = corner2[i].giveIndex();
                    }
                }
            }
            LeafPair newPair;
            int place = 0, size;
            for (i = 0; i < n2; i++) {
                if (corner2[i].giveIndex() == bestIndr) {
                    size = corner2[i].giveSize();
                    for (j = 0; j < size; j++) {
                        if (fDist[i][j].n == mBestr) {
                            rpre = fDist[i][j].best;
                            j = size;
                        }
                    }
                    i = n2;
                }
            }

            for (i = 0; i < n1; i++) {
                if (corner1[i].giveIndex() == bestIndl) {
                    size = corner1[i].giveSize();
                    myDist = corner1[i].giveList();
                    for (j = 0; j < size; j++) {
                        if (myDist[j].n == mBestl) {
                            lpre = myDist[j].best;
                            j = size;
                        }
                    }
                    newPair = new LeafPair(bestIndl, bestIndr, lpre, rpre, n1, n2);
                    place = i;
                    corner1[i].initNewSize(1);
                    corner1[i].addNewDist(bestIndr, best, newPair);
                    corner1[i].replace();
                    i = n1;
                }
            }
            fDist = null;
            return place;
        }
    }

    private class Tree {
        private Tree left, right;
        private int numLeafs;
        private Leaf[] allLeafs; //list of Leaf instances
        private int nodeNum;
        private double[][] mat;

        // generate a leaf (tree with only one node)
        public Tree(int index, double[][] m) {
            mat = m;
            nodeNum = index;
            allLeafs = new Leaf[1];
            Leaf myLeaf = new Leaf(index, mat);
            allLeafs[0] = myLeaf;
            numLeafs = 1;
            left = right = null;
        }

        // combine two trees
        public Tree(Tree t1, Tree t2, int nNum) {
            nodeNum = nNum;
            mat = t1.giveMat();
            int n1 = t1.giveNumLeafs();
            int n2 = t2.giveNumLeafs();
            numLeafs = n1 + n2;
            allLeafs = new Leaf[numLeafs];
            int i;
            Leaf[] l = t1.giveLeafs();
            for (i = 0; i < n1; i++)
                allLeafs[i] = l[i];
            l = t2.giveLeafs();
            for (i = 0; i < n2; i++)
                allLeafs[n1 + i] = l[i];
            left = t1;
            right = t2;
        }

        // compute the optimal order for this tree
        int compDist() {
            if (numLeafs == 1) { // no nodes to flip
                return 0;
            }
            int i, j;
            left.compDist(); // compute optimal order matrix t subtree
            right.compDist();
            int n1 = left.giveNumLeafs();
            int n2 = right.giveNumLeafs();
            if (n1 + n2 == nelements) { // last two subtrees
                return lastTree(n1, n2);
                // this is an optimization which
                // reduces the running time by half, see paper for details
            }
            if (n1 > 1 && n2 > 1) {
                return compDist(n1, n2); // calling another optimization
            } else { // one subtree has only one leaf
                Leaf[] l1 = left.giveLeafs();
                Leaf[] l2 = right.giveLeafs();
                for (j = 0; j < n2; j++)
                    l2[j].initNewSize(n1);
                for (i = 0; i < n1; i++) {
                    l1[i].initNewSize(n2);
                    // this function actually computes the set of optimal orders
                    // of leftmost and rightmost leaves in the combined tree.
                    l1[i].addToNew(l2, l2, n1, n2, n2, n2, Double.MIN_VALUE);
                    l1[i].replace();
                }
                for (j = 0; j < n2; j++)
                    l2[j].replace();
            }
            return 0;
        }

        // optimization, perfromed for combining the last two subtrees
        int lastTree(int n1, int n2) {

            Leaf[] l1 = left.giveLeafs();
            Leaf[] l2 = right.giveLeafs();
            int res = l1[0].findLast(l1, l2, n1, n2);
            return res;
        }

        // optimization which allows for early termination of the search
        // see paper for details
        int compDist(int tot1, int tot2) {
            Tree t1 = left;
            Tree t2 = right;
            Tree t1l = t1.left;
            Tree t1r = t1.right;
            Tree t2l = t2.left;
            Tree t2r = t2.right;
            int n1 = t1l.giveNumLeafs();
            int n2 = t1r.giveNumLeafs();
            int n3 = t2l.giveNumLeafs();
            int n4 = t2r.giveNumLeafs();
            Leaf[] l1 = t1l.giveLeafs();
            Leaf[] l2 = t1r.giveLeafs();
            Leaf[] l3 = t2l.giveLeafs();
            Leaf[] l4 = t2r.giveLeafs();
            Leaf[] c2 = t2.giveLeafs();
            double mint1lt2l, mint1lt2r, mint1rt2l, mint1rt2r;
            mint1lt2l = mint1lt2r = mint1rt2l = mint1rt2r = 1;
            int i, j, i1, i2, j3, j4;
            // compute minimum similarity between genes in these
            // two subtrees
            for (i = 0; i < n1; i++) {
                i1 = l1[i].giveIndex();
                for (j = 0; j < n3; j++) {
                    j3 = l3[j].giveIndex();
                    if (mat[i1][j3] < mint1rt2r) {
                        mint1rt2r = mat[i1][j3];
                    }
                }
                for (j = 0; j < n4; j++) {
                    j4 = l4[j].giveIndex();
                    if (mat[i1][j4] < mint1rt2l) {
                        mint1rt2l = mat[i1][j4];
                    }
                }
            }
            for (i = 0; i < n2; i++) {
                i2 = l2[i].giveIndex();
                for (j = 0; j < n3; j++) {
                    j3 = l3[j].giveIndex();
                    if (mat[i2][j3] < mint1lt2r) {
                        mint1lt2r = mat[i2][j3];
                    }
                }
                for (j = 0; j < n4; j++) {
                    j4 = l4[j].giveIndex();
                    if (mat[i2][j4] < mint1lt2l) {
                        mint1lt2l = mat[i2][j4];
                    }
                }
            }
            for (j = 0; j < tot2; j++)
                c2[j].initNewSize(tot1);
            for (i = 0; i < n1; i++) {
                l1[i].initNewSize(tot2);
                // use precomputed minimums to terminate the search early
                l1[i].addToNew(l4, l3, tot1, tot2, n4, n3, mint1lt2l);
                l1[i].addToNew(l3, l4, tot1, tot2, n3, n4, mint1lt2r);
                l1[i].replace();
            }
            for (i = 0; i < n2; i++) {
                l2[i].initNewSize(tot2);
                l2[i].addToNew(l4, l3, tot1, tot2, n4, n3, mint1rt2l);
                l2[i].addToNew(l3, l4, tot1, tot2, n3, n4, mint1rt2r);
                l2[i].replace();
            }
            for (j = 0; j < tot2; j++)
                c2[j].replace();
            return 0;
        }

        // called by the main program to return the optimal order
        // of the tree leaves
        Object[] returnOrder() {
            int start = compDist();
            LeafDist[] myDist = allLeafs[start].giveList();
            Double bestDist = new Double(myDist[0].dist);
            LeafPair best = myDist[0].best;
            int[] res = new int[numLeafs];
            // used to find the correct order of the leaves
            // of the tree
            compTree(best.preLeft, res, 0, best.n1 - 1, leftTree);
            compTree(best.preRight, res, best.n1, numLeafs - 1, rightTree);
            Object[] ret = new Object[2];
            ret[0] = res;
            ret[1] = bestDist;
            return ret;
        }

        // computes the new order of the leaves, based on the optimal
        // ordering computation (using the 'best' struct).
        void compTree(LeafPair best, int[] res, int start, int last, int l) {
            if (start == last) {
                res[start] = best.leftLeaf;
                return;
            }
            if (l == leftTree) {
                compTree(best.preLeft, res, start, start + best.n1 - 1, leftTree);
                compTree(best.preRight, res, start + best.n1, last, rightTree);
            }
            if (l == rightTree) {
                compTree(best.preLeft, res, start + best.n2, last, rightTree);
                compTree(best.preRight, res, start, start + best.n2 - 1, leftTree);
            }
        }

        // computes the sum of similarities in the current order
        // of the tree leaves
        double curDist(double[][] m) {
            if (numLeafs == 1)
                return 0;
            double d1 = left.curDist(m);
            double d2 = right.curDist(m);
            int lCorner = left.findRight();
            int rCorner = right.findLeft();
            return (d1 + d2 + m[lCorner][rCorner]);
        }

        // find the rightmost leaf
        int findRight() {
            if (numLeafs == 1)
                return allLeafs[0].giveIndex();
            return right.findRight();
        }

        int findLeft() {
            if (numLeafs == 1)
                return allLeafs[0].giveIndex();
            return left.findLeft();
        }

        // finds the initial order of the tree leaves
        int[] initOrder() {
            int[] res = new int[numLeafs + 1];
            fillArray(res, 0, numLeafs - 1);
            return res;
        }

        void fillArray(int[] array, int s, int l) {
            if (numLeafs == 1) {
                array[s] = allLeafs[0].giveIndex();
                return;
            }
            int n1 = left.giveNumLeafs();
            left.fillArray(array, s, s + n1 - 1);
            right.fillArray(array, s + n1, l);
            return;
        }

        boolean isLeaf() {
            return (numLeafs == 1);
        }

        int giveIndex() {
            return nodeNum;
        }

        double[][] giveMat() {
            return mat;
        }

        int giveNumLeafs() {
            return numLeafs;
        }

        Leaf[] giveLeafs() {
            return allLeafs;
        }
    }

    //TODO: reimplement similarity matrix for efficiency purposes
    /**
     * Find an optimal leaf ordering
     * Note: exact ordering still depends on input order, but much less than heuristic ordering
     * <p/>
     * This code is ported from Ziv Bar-Joseph's optimal leaf ordering code available here:
     * http://www.cs.cmu.edu/~zivbj/ and as described in the following papers:
     * <p/>
     * Bar-Joseph Z, Gifford DK, Jaakkola TS
     * Fast optimal leaf ordering for hierarchical clustering
     * Bioinformatics. 2001;17 Suppl 1:S22-9
     * <p/>
     * Bar-Joseph Z, Demaine ED, Gifford DK, Srebro N, Hamel AM, Jaakkola TS
     * K-ary clustering with optimal leaf ordering for gene expression data
     * Bioinformatics. 2003 Jun 12;19(9):1070-8
     *
     * @param resultTree The result of hierarchical clustering
     * @param dm         The distance matrix used to cluster
     */
    private void orderLeavesBarJoseph2003(int[][] resultTree, DistanceMatrix dm, int[] leafOrder) {
        //convert dm to double matrix for this code - all indices in this code start from 1
        double[][] mat = new double[dm.getMatrixDimension() + 1][dm.getMatrixDimension() + 1];
        for (int i = 1; i < dm.getMatrixDimension() + 1; i++) {
            for (int j = 1; j < dm.getMatrixDimension() + 1; j++) {
                mat[i][j] = (double) (dm.getValue(i - 1, j - 1) - 1.0f); //convert to negative similarity matrix - assumes dm is normalized
            }
        }
        //convert resultTree to Tree
        Tree t = convertResultTreeToTreeClass(resultTree, mat);

        //debug
        //t = barJosephClustering(mat);

        //double init = -1 * t.curDist(mat); // initial ordering score
        //System.out.println("Initial sum of similarities: " + init);
        Object[] ret = t.returnOrder(); // the optimal leaf algorithm call
        int[] arr = (int[]) ret[0];
        //move optimal ordering to leafOrder
        for (int i = 0; i < nelements; i++) {
            leafOrder[i] = arr[i] - 1;
        }
        //double min = -1 * ((Double) ret[1]).doubleValue();
        //System.out.println("Optimal sum of similarities: " + min);
        //System.out.println("Improvement: " + ((min - init) / (init) * 100) + "%");
    }

    /**
     * Convert hierarchical clustering result tree to Tree
     */
    private Tree convertResultTreeToTreeClass(int[][] resultTree, double[][] mat) {
        Tree t = null; //root
        Tree[] tlist = new Tree[nelements]; //one for each internal node
        for (int i = 0; i < nelements - 1; i++) {
            Tree tleft = null;
            Tree tright = null;
            if (resultTree[i][0] >= 0) { //leaf
                tleft = new Tree(resultTree[i][0] + 1, mat); //convert to 1..nelements
            } else { //node
                tleft = tlist[-resultTree[i][0]];
            }
            if (resultTree[i][1] >= 0) { //leaf
                tright = new Tree(resultTree[i][1] + 1, mat); //convert to 1..nelements
            } else { //node
                tright = tlist[-resultTree[i][1]];
            }
            t = tlist[i + 1] = new Tree(tright, tleft, i + 1); //right/left is reversed here in bar joseph code (not very important)
        }
        return t;
    }

    //from original bar joseph code - only here for debug purposes
    private Tree barJosephClustering(double[][] m) {
        int i, j, k;
        int num = nelements;
        double[][] newM = new double[num + 1][];
        Tree[] allTrees = new Tree[num + 1];
        // copy similarity matrix
        for (i = 1; i < num + 1; i++) {
            newM[i] = new double[num + 1];
            for (j = 1; j < num + 1; j++) {
                newM[i][j] = m[i][j];
            }
        }
        // initially each gene is assigned to its own cluster
        for (i = 1; i < num + 1; i++) {
            allTrees[i] = new Tree(i, m);
        }
        double max;
        int r = 0, l = 0;
        double lSize, rSize;
        Tree temp;
        for (i = 1; i < num; i++) {
            max = Double.MIN_VALUE;
            // find minimum entry in (converted) similarity matrix
            for (k = 1; k < num; k++) {
                if (allTrees[k] != null) {
                    for (j = k + 1; j < num + 1; j++) {
                        if (allTrees[j] != null) {
                            if (max < (-1 * newM[j][k])) {
                                max = (-1 * newM[j][k]);
                                l = k;
                                r = j;
                            }
                        }
                    }
                }
            }
            rSize = allTrees[r].giveNumLeafs();
            lSize = allTrees[l].giveNumLeafs();
            System.out.print("NODE" + i + "X" + '\t');
            if (allTrees[l].isLeaf())
                System.out.print("GENE" + allTrees[l].giveIndex() + "X\t");
            else
                System.out.print("NODE" + allTrees[l].giveIndex() + "X\t");
            if (allTrees[r].isLeaf())
                System.out.print("GENE" + allTrees[r].giveIndex() + "X\t");
            else
                System.out.print("NODE" + allTrees[r].giveIndex() + "X\t");
            System.out.println(max);

            temp = allTrees[l];
            allTrees[l] = null;
            // combine the two clusters
            allTrees[l] = new Tree(temp, allTrees[r], i);
            allTrees[r] = null;
            // update similarity matrix
            for (j = 1; j < num + 1; j++) {
                if (allTrees[j] != null && j != l) {
                    newM[j][l] = (lSize * newM[j][l] + rSize * newM[j][r]) / (lSize + rSize);
                    newM[l][j] = newM[j][l];
                }
            }
        }
        Tree res = null;
        // find root of the tree
        for (i = 1; i < num + 1; i++) {
            if (allTrees[i] != null) {
                res = allTrees[i];
            }
        }
        return res;
    }

    /**
     * Write the distance matrix out as a Cytoscape SIF file (stores the connections) and an EA file (stores the
     * distance measure of each connection)
     *
     * @param sifFileName           The SIF filename
     * @param edgeAttributeFileName The edge attribute filename
     * @param distanceCutoff        Don't output connections above this cutoff
     * @throws IOException If there is a problem writing either file
     *                     TODO: move this and other format writer methods into a utility class. Also create a method to copy this
     *                     TODO: information directly into Cytoscape from a Cytoscape plugin
     */
    public void writeResultsToCytoscapeFormat(File sifFileName, File edgeAttributeFileName, double distanceCutoff) throws IOException {
        //initialization
        List<String> labels = distanceMatrix.getLabels();
        BufferedWriter brSIF = new BufferedWriter(new FileWriter(sifFileName));
        BufferedWriter brEA = new BufferedWriter(new FileWriter(edgeAttributeFileName));

        //create EA file header
        brEA.write("ClusterDistance");
        brEA.newLine();

        //convert distance matrix to Cytoscape format
        for (int i = 0; i < nelements; i++) {
            for (int j = i; j < nelements; j++) {
                if ((distanceMatrix.getValue(i, j) <= distanceCutoff) && (distanceMatrix.getValue(i, j) != 0.0)) {
                    //create SIF file
                    brSIF.write(labels.get(i) + "\tcl\t" + labels.get(j));
                    brSIF.newLine();
                    //create EA file
                    brEA.write(labels.get(i) + " (cl) " + labels.get(j) + " = " + distanceMatrix.getValue(i, j));
                    brEA.newLine();
                }
            }
        }
        brSIF.close();
        brEA.close();
    }

    /**
     * Saves the clustering results to GTR format for loading up into e.g Java Treeview
     * http://jtreeview.sourceforge.net
     * Column 1: NodeIdentifier
     * Column 2: Left child of node
     * Column 3: Right child of node
     * Column 4: Correlation between the left and right child
     */
    public String writeResultsToGTRFormat() {
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");
        for (int i = 0; i < result.length; i++) {
            //node identifier
            sb.append("NODE" + (i + 1) + "X" + "\t");
            //left child
            sb.append((result[i][0] >= 0 ? "GENE" + Math.abs(result[i][0]) : "NODE" + Math.abs(result[i][0])) + "X" + "\t");
            //right child
            sb.append(((result[i][1] >= 0) ? "GENE" + Math.abs(result[i][1]) : "NODE" + Math.abs(result[i][1])) + "X" + "\t");
            sb.append((1 - linkDistance[i]) + lineSep);
        }
        return sb.toString();
    }

    /**
     * Used to store label list / color pairs for highlighting output of CDT files
     */
    private class LabelColorPair {
        private String color;
        private ArrayList labels;

        public LabelColorPair(String color, ArrayList labels) {
            this.color = color;
            this.labels = labels;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public ArrayList getLabels() {
            return labels;
        }

        public void setLabels(ArrayList labels) {
            this.labels = labels;
        }
    }

    /**
     * Set a list of labels to highlight in the CDT file output
     * Labels should not overlap previously set labels
     * If a single label name is set to be highlighted in two or more colors, the behavior is undefined.
     *
     * @param labelsToHighlight The list of label Strings to highlight
     * @param color             The color to highlight in the typical HTML format e.g. #FFFFFF=white, #FFFF00=yellow
     *                          Background color will always be white.
     */
    public void setLabelHighlightInCDTOutput(ArrayList labelsToHighlight, String color) {
        if (labelHighlight == null) {
            labelHighlight = new ArrayList();
        }
        LabelColorPair lcp = new LabelColorPair(color, labelsToHighlight);
        labelHighlight.add(lcp);
    }

    /**
     * Saves the distance matrix as a CDT file suitable for loading into a tree/cluster viewing program like jtreeview
     * Elements are highlighted in according to colors set by the setLabelHighlightInCDTOutput method
     */
    public String toCDTString() {
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");
        //header line 1
        if (labelHighlight != null) {
            sb.append("GID\tUNIQID\tNAME\tBGCOLOR\tGWEIGHT");
        } else {
            sb.append("GID\tUNIQID\tNAME\tGWEIGHT");
        }
        List<String> labels = distanceMatrix.getLabels();
        for (int i = 0; i < labels.size(); i++) {
            int indexi = leafOrder[i];
            sb.append("\t" + (String) labels.get(indexi));
        }
        sb.append(lineSep);
        //header line 2
        sb.append("EWEIGHT\t\t\t");
        for (int i = 0; i < nelements; i++) {
            sb.append("\t1.000000");
        }
        sb.append(lineSep);
        //rows
        for (int i = 0; i < nelements; i++) {
            int indexi = leafOrder[i];
            sb.append("GENE" + indexi + "X\t" + labels.get(indexi) + "\t" + labels.get(indexi) + "\t");
            if (labelHighlight != null) {
                //go through labelHighlight list to check for a label highlight color
                boolean found = false;
                for (int j = 0; j < labelHighlight.size(); j++) {
                    LabelColorPair labelColorPair = (LabelColorPair) labelHighlight.get(j);
                    ArrayList labelsToHighlight = labelColorPair.getLabels();
                    if (labelsToHighlight.contains(labels.get(indexi))) {
                        found = true;
                        sb.append(labelColorPair.getColor());
                        break;
                    }
                }
                if (!found) {
                    //output the default
                    sb.append("#FFFFFF");
                }
            }
            sb.append("\t1.000000");
            for (int j = 0; j < nelements; j++) {
                int indexj = leafOrder[j];
                sb.append("\t" + distanceMatrix.getValue(indexi, indexj));
            }
            sb.append(lineSep);
        }
        return sb.toString();
    }
}
