package org.baderlab.csplugins.brainlib;

import java.util.List;

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
 * * Time: 8:26:57 PM
 */

/**
 * Implements a ragged array distance matrix for use with AvgLinkHierarchicalClustering
 * Only stores the lower triangle without the diagonal. Assumes the matrix is symmetric around the diagonal
 * and the diagonal is all zeros.
 */
public class DistanceMatrix {
    private double[] distanceMatrix = null;
    private int matrixDim = 0;
    private List<String> labels = null;
    private double minimumDistance = Double.MAX_VALUE;
    private int mini = -1;
    private int minj = -1;

    /**
     * Initializes a distance matrix of the given dimension
     *
     * @param matrixDim The dimension of the matrix
     */
    public DistanceMatrix(int matrixDim) {
        if (matrixDim < 2) {
            throw new RuntimeException("Distance matrix size must be larger than 1");
        }
        this.matrixDim = matrixDim;
        //allocate enough space for lower triangle of the matrix minus the diagonal
        distanceMatrix = new double[(matrixDim * matrixDim - 1) / 2];
    }

    /**
     * Gets the dimension of the matrix
     *
     */
    public int getMatrixDimension() {
        return matrixDim;
    }

    /**
     * Put a value in the matrix at position (i,j)
     */
    public void setValue(int i, int j, double value) {
        if (j >= i) {
            //don't store the diagonal or upper triangle
            return;
        }
        //calculate the position in the array given the lower triangular matrix coordinates
        distanceMatrix[((i * (i - 1)) / 2) + j] = value;
    }

    /**
     * Get a value from the matrix at position (i,j)
     */
    public double getValue(int i, int j) {
        if (i == j) {
            //return 0.0 for the diagonal
            return 0.0;
        }
        if (j > i) {
            //convert to lower triangle - the matrix is assumed to be symmetric
            int oldj = j;
            j = i;
            i = oldj;
        }
        //calculate the position in the array given the lower triangular matrix coordinates
        return (distanceMatrix[((i * (i - 1)) / 2) + j]);
    }

    /**
     * Sets the labels of the elements in this matrix. The elements of the ArrayList must be
     * Strings corresponding to the elements in this matrix (in the same order)
     */
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * Gets the labels for the distanceMatrix
     *
     * @return An ArrayList containing Strings in the order corresponding to the rows or columns of the distance matrix
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Calculates a distance matrix given a distance metric implemented in the DistanceMetric class
     * Note: you need to extend the DistanceMetric class and override the 'calc' method for this
     * to do something useful.
     *
     * @param objectList     The list of objects to use to calculate an NxN distance matrix
     * @param distanceMetric The distance metric to use
     */
    public void calcDistances(List<float[]> objectList, DistanceMetric distanceMetric) {
        //calculate the lower triangle of the distance matrix
        for (int i = 0; i < objectList.size(); i++) {
            float[] object1 = objectList.get(i);
            for (int j = 0; j < i; j++) {
                float[] object2 = objectList.get(j);
                double distance = distanceMetric.calc(object1, object2);
                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    mini = i;
                    minj = j;
                }
                this.setValue(i, j, distance);
            }
        }
    }

    /**
     * Returns the minimum distance in the distance matrix.
     */
    public double getMinimumDistance() {
        return this.minimumDistance;
    }

    /**
     * Returns the "i" index of the matrix where (i,j) contains the minimum distance value
     */
    public int getMinimumI() {
        return this.mini;
    }

    /**
     *  Returns the "j" index of the matrix where (i,j) contains the minimum distance value
     */
    public int getMinimumJ() {
        return this.minj;
    }

    /**
     * Returns an exact copy of this DistanceMatrix object
     */
    public DistanceMatrix copy() {
        DistanceMatrix dm = new DistanceMatrix(this.matrixDim);
        System.arraycopy(this.distanceMatrix, 0, dm.distanceMatrix, 0, this.distanceMatrix.length);
        dm.setLabels(this.getLabels());
        return dm;
    }

    //TODO: normalize 0..1 distance matrix for other distance metrics - hierarchical clustering alg makes this assumption

    /**
     * Return the string representation of this matrix
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");
        //header
        for (int i = 0; i < labels.size(); i++) {
            sb.append("\t" + (String) labels.get(i));
        }
        sb.append(lineSep);
        //labels rows
        for (int i = 0; i < matrixDim; i++) {
            sb.append(labels.get(i) + "\t");
            for (int j = 0; j < i; j++) {
                sb.append(this.getValue(i, j));
                if (j < i) {
                    //don't output a tab for the last one
                    sb.append("\t");
                }
            }
            sb.append(lineSep);
        }
        return (sb.toString());
    }
}
