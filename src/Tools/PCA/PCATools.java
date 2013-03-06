/**
 *
 */
package Tools.PCA;

import java.util.Arrays;

import Tools.VectorTools;


/**
 * @author Piotr Bugaj
 *
 */
public class PCATools {


    public static void main(String [] args) {

        double [] x = new double[]{2.5,    0.5,    2.2,    1.9,    3.1,    2.3,    2,    1,    1.5,    1.1};
        double [] y = new double[]{2.4,    0.7,    2.9,    2.2,    3.0,    2.7,    1.6,1.1,1.6,    0.9};

        double [][] set = new double[][]{x, y};
        double [][] cov_m = getCovMatrix(set);

        System.out.println(cov_m[0][0]+" "+cov_m[0][1]);
        System.out.println(cov_m[1][0]+" "+cov_m[1][1]);

        JacobiResult jr = JacobiEigenvalueAlgorithm.runAlgorithm(cov_m);
        double [] d = jr.getVector_d(1);
        double [] d2 = jr.getVector_d(0);
        
        System.out.println(d2[0] +" "+d2[1]);
        System.out.println(d[0] +" "+d[1]);
        
        /**Transform the points**/
        
        
        /**Find the 2D area, using the two axis with the large spread of data**/
    }



    /**Get the mean value of the set**/
    public static double getMean(double [] set) {
        double sum = 0.0;
        for(int i = 0; i < set.length; i++) {
            sum+=set[i];
        }
        if(set.length==0) {
            return 0;
        } else {
            return sum/set.length;
        }
    }

    /**Subtract the set medium from the set itself**/
    public static double [] getAdjustedSet(double [] set) {
        double mean = getMean(set);

        double [] new_set = new double [set.length];
        for(int i =0; i < set.length; i++) {
            new_set[i] = set[i]-mean;
        }
        return new_set;
    }
    
    /**Get the largest area covered by the points, transformed by
     * the basis of the PCA vectors. Input:
     * set: m by n matrix - m dimensions, n points
     * V:   k by n matrix - k being number of eigen values (1-m),
     *      highest on top, m being left of eigen value dimension**/
    public static double [] getPCAArea(double [][] set, double [][] V) {
    	
    	double [][] new_set = VectorTools.mult(V, set);
    	
    	double [] widths = new double [new_set.length];
    	for(int i = 0; i < new_set.length; i++) {
    		
    		double [] temp_set = new_set[i].clone();
    		Arrays.sort(temp_set);
    		
    		widths[i] = Math.abs(temp_set[0] - temp_set[temp_set.length-1]);
    	}
    	Arrays.sort(widths);
    	
    	double area = widths[widths.length-1]*widths[widths.length-2];
    	return new double[]{area, widths[widths.length-1],widths[widths.length-2]};
    }

    /**Get the standard deviation for set a**/
    public static double getStdDev(double [] set_a) {
    	double [] new_a = getAdjustedSet(set_a);
    	for(int i = 0; i < new_a.length; i++) {
    		new_a[i] = new_a[i]*new_a[i];
    	}
    	return Math.sqrt(getMean(new_a));
    }

    /**Get the covariance between set a and set b**/
    public static double getCovarience(double [] set_a, double [] set_b) {
        double cov = 0.0;

        double [] new_a = getAdjustedSet(set_a);
        double [] new_b = getAdjustedSet(set_b);

        double sum = 0.0;

        for(int i = 0; i < set_a.length; i++) {
            sum += new_a[i]*new_b[i];
        }
        cov = sum/(set_a.length-1);

        return cov;
    }

    /**Input: m by n matrix of data points (m dimensions, n points)**/
    public static double [][] getCovMatrix(double [][] set) {

        double [][]cov_matrix = new double[set.length][set.length];

        for(int dimension_i = 0; dimension_i < set.length; dimension_i++) {
            for(int dimension_j = 0; dimension_j < set.length; dimension_j++) {

                double [] i_dimension_points = set[dimension_i];
                double [] j_dimension_points = set[dimension_j];

                double cov_ij = getCovarience(i_dimension_points, j_dimension_points);
                cov_matrix[ dimension_i][ dimension_j] = cov_ij;
            }
        }

        return cov_matrix;
    }

    /**Get the PCA component iteratively using:
     * Roweis, Sam. "EM Algorithms for PCA and SPCA."
     * Advances in Neural Information Processing Systems.
     * Ed. Michael I. Jordan, Michael J. Kearns, and
     * Sara A. Solla The MIT Press, 1998.**/
    public static double [] getEgPowerIter(double [][] set) {

    	double [][] adjusted_set = new double[set.length][set[0].length];
        for(int i = 0; i < adjusted_set.length; i++) {
        	adjusted_set[i] = getAdjustedSet(set[i]);
        }
        
        double [][] set_T = VectorTools.getTranspose(adjusted_set);
        int dim_M = set.length;

        double [] p = new double [dim_M];
        for(int i = 0; i < p.length; i++) {
            p[i]=1.0;
        }
        int c = 100;


        for(int i = 0; i < c; i++) {

            double [] t = new double[dim_M];
            for(int j = 0; j < t.length; j++) {
                t[j]=0;
            }
            for(int row_j = 0; row_j < set_T.length; row_j++) {

                double [] x = set_T[row_j];
                t = VectorTools.add(t,  VectorTools.mult(VectorTools.dot(x,p),x));
            }

            double [] p2 = VectorTools.mult( (1/VectorTools.mag(t)), t);
            if(VectorTools.mag(VectorTools.sub(p, p2)) < 0.0001) {
            	p = p2;
                break;
            } else {
                p = p2;
            }
        }

        return p;
    }




    /**Get the mean value of the set**/
    public static float getMean(float [] set) {
        float sum = 0.0f;
        for(int i = 0; i < set.length; i++) {
            sum+=set[i];
        }
        if(set.length==0) {
            return 0;
        } else {
            return sum/(set.length+0.0f);
        }
    }

    /**Subtract the set medium from the set itself**/
    public static float [] getAdjustedSet(float [] set) {
        float mean = getMean(set);

        float [] new_set = new float [set.length];
        for(int i =0; i < set.length; i++) {
            new_set[i] = set[i]-mean;
        }
        return new_set;
    }

    /**Get the largest area covered by the points, transformed by
     * the basis of the PCA vectors. Input:
     * set: m by n matrix - m dimensions, n points
     * V:   k by n matrix - k being number of eigen values (1-m),
     *      highest on top, m being left of eigen value dimension**/
    public static float [] getPCAArea(float [][] set, float [][] V) {
    	
    	
    	float [][] new_set = set.clone();
    	float [] widths = new float [new_set.length];
    	for(int i = 0; i < new_set.length; i++) {
    		
    		float [] temp_set = new_set[i].clone();
    		Arrays.sort(temp_set);
    		
    		widths[i] = Math.abs(temp_set[0] - temp_set[temp_set.length-1]);
    	}
    	Arrays.sort(widths);
    	
    	//float area = widths[widths.length-1]*widths[widths.length-2];
    	float prev_width1 = widths[widths.length-1];
    	float prev_width2 = widths[widths.length-2];
    	//System.out.println("A1: "+area+" W1: "+widths[widths.length-1]+" W1: "+widths[widths.length-2]);
    	
    	
    	new_set = VectorTools.mult(V, set.clone());
    	
    	widths = new float [new_set.length];
    	for(int i = 0; i < new_set.length; i++) {
    		
    		float [] temp_set = new_set[i].clone();
    		Arrays.sort(temp_set);
    		
    		widths[i] = Math.abs(temp_set[0] - temp_set[temp_set.length-1]);
    	}
    	Arrays.sort(widths);
    	
    	float area2 = widths[widths.length-1]*widths[widths.length-2];
    	
    	/**
    	System.out.println("A2: "+area2+" W2: "+widths[widths.length-1]+" W2: "+widths[widths.length-2]);
    	if(area > area2*1.2f) {
    		System.out.println();
    		System.exit(1);
    	}**/
    	
    	return new float[]{area2, prev_width1, prev_width2};
    }
    
    /**Get the standard deviation for set a**/
    public static float getStdDev(float [] set_a) {
    	float [] new_a = getAdjustedSet(set_a);
    	for(int i = 0; i < new_a.length; i++) {
    		new_a[i] = new_a[i]*new_a[i];
    	}
    	return (float) Math.sqrt(getMean(new_a));
    }
    
    /**Get the covariance between set a and set b**/
    public static float getCovarience(float [] set_a, float [] set_b) {
        float cov = 0.0f;

        float [] new_a = getAdjustedSet(set_a);
        float [] new_b = getAdjustedSet(set_b);

        float sum = 0.0f;

        for(int i = 0; i < set_a.length; i++) {
            sum += new_a[i]*new_b[i];
        }
        cov = sum/(set_a.length);

        return cov;
    }

    /**Input: m by n matrix of data points (m dimensions, n points)**/
    public static float [][] getCovMatrix(float [][] set) {

        float [][]cov_matrix = new float[set.length][set.length];

        for(int dimension_i = 0; dimension_i < set.length; dimension_i++) {
            for(int dimension_j = 0; dimension_j < set.length; dimension_j++) {

                float [] i_dimension_points = set[dimension_i];
                float [] j_dimension_points = set[dimension_j];

                float cov_ij = getCovarience(i_dimension_points, j_dimension_points);
                cov_matrix[ dimension_i][ dimension_j] = cov_ij;
            }
        }

        return cov_matrix;
    }

    /**m by n data vector (m: number of dimensions    n: number of points)**/
    public static JacobiResult getPCAUsingJacobi(float [][] set) {
    	
        float [][] adjusted_set = new float[set.length][set[0].length];
        for(int i = 0; i < adjusted_set.length; i++) {
        	adjusted_set[i] = getAdjustedSet(set[i]);
        }

        float [][] cov_m = getCovMatrix(set);
        JacobiResult jr = JacobiEigenvalueAlgorithm.runAlgorithm(cov_m);

    	return jr;
    }
    
    /**Get the PCA component iteratively using:
     * Roweis, Sam. "EM Algorithms for PCA and SPCA."
     * Advances in Neural Information Processing Systems.
     * Ed. Michael I. Jordan, Michael J. Kearns, and
     * Sara A. Solla The MIT Press, 1998.**/
    public static float [] getEgPowerIter(float [][] set) {

        float [][] adjusted_set = new float[set.length][set[0].length];
        for(int i = 0; i < adjusted_set.length; i++) {
        	adjusted_set[i] = getAdjustedSet(set[i]);
        }
        
        float [][] set_T = VectorTools.getTranspose(adjusted_set);
        int dim_M = set.length;

        float [] p = new float [dim_M];
        for(int i = 0; i < p.length; i++) {
            p[i]=1.0f;
        }
        int c = 100;


        for(int i = 0; i < c; i++) {

            float [] t = new float[dim_M];
            for(int j = 0; j < t.length; j++) {
                t[j]=0;
            }
            for(int row_j = 0; row_j < set_T.length; row_j++) {

                float [] x = set_T[row_j];
                t = VectorTools.add(t,  VectorTools.mult(VectorTools.dot(x,p),x));
            }

            float [] p2 = VectorTools.mult( (1/VectorTools.mag(t)), t);
            if(VectorTools.mag(VectorTools.sub(p, p2)) < 0.0001) {
            	p = p2;
                break;
            } else {
                p = p2;
            }
        }

        return p;
    }

}