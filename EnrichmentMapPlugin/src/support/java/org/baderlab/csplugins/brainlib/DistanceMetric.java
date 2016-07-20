package org.baderlab.csplugins.brainlib;

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
 * * Time: 9:11:52 PM
 */

/**
 * Class for calculation of a distance metric for hierarchical clustering
 */
public class DistanceMetric {
    /**
     * Calculates the distance between two objects. If two objects are identical, the distance between
     * them in any metric is zero.
     *
     * @param object1 Object1
     * @param object2 Object2
     * @return the distance between the objects
     */
    public double calc(Object object1, Object object2) {
        //this is a basic method that must be overridden by a real distance method
        if (object1.equals(object2)) {
            return 0.0;
        }
        return 1.0;
    }
}
