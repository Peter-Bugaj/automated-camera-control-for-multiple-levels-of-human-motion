package Tools;
/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date June 7, 2010
 */
public class VectorTools {

	public static void main(String [] args) {
		System.out.println(Math.cos(45.0f/180.0f*Math.PI));
		float [][] res = VectorTools.mult(
				new float[][]{ 
						new float[]{14, 9, 3},
						new float[]{2, 11, 15},
						new float[]{0, 12, 17},
						new float[]{5, 2, 3}
				},
				new float[][]{
						new float[]{12, 25},
						new float[]{9, 10},
						new float[]{8, 5}
				}
				);
		for(int i = 0; i < res.length; i++) {
			for(int j = 0; j < res[i].length; j++) {
				System.out.print(res[i][j]+ " ");
			}
			System.out.println();
		}
		
		float [] v1 = new float[]{1.0f, 1.0f, 0.0f};
		System.out.println(v1[0]+" "+v1[1]+" "+v1[2]);
		float[][] m_rot_z = gen_rot_Z( (float)  (180.0f/180.0f*Math.PI));
		

		v1 = VectorTools.mult(m_rot_z, v1);
		m_rot_z = gen_rot_Z( (float)  (0.0f/180.0f*Math.PI));
		v1 = VectorTools.mult(m_rot_z, v1);
		
		System.out.println(v1[0]+" "+v1[1]+" "+v1[2]);
	}


	/**Get the norm given a triangle of three points**/
	public static float [] getNorm(float [] a, float[] b, float[] c) {
		float []ab = VectorTools.sub(a,b);
		float []cb = VectorTools.sub(c,b);

		return VectorTools.cross(ab, cb);
	}
	
	/**Generate a rotation matrix for x-axis**/
	public static float[][] gen_rot_X(float ang) {
		return new float[][]{
			new float[]{1.0f, 	0.0f, 					0.0f},
			new float[]{0.0f,	(float) Math.cos(ang),	-(float) Math.sin(ang)},
			new float[]{0.0f,	(float) Math.sin(ang),	(float) Math.cos(ang)}
		};
	}
	/**Generate a rotation matrix for y-axis**/
	public static float[][] gen_rot_Y(float ang) {
		return new float[][]{
			new float[]{(float) Math.cos(ang),	0.0f,	(float) Math.sin(ang)},
			new float[]{0.0f, 					1.0f, 	0.0f},
			new float[]{-(float) Math.sin(ang),	0.0f,	(float) Math.cos(ang)}
		};
	}
	/**Generate a rotation matrix for z-axis**/
	public static float[][] gen_rot_Z(float ang) {
		return new float[][]{
			new float[]{(float) Math.cos(ang),	-(float) Math.sin(ang),	0.0f},
			new float[]{(float) Math.sin(ang),	(float) Math.cos(ang), 	0.0f},
			new float[]{0.0f, 					0.0f, 					1.0f}
		};
	}
	
	/**Transform a point, given the camera position and orientation**/
	public static float [] transformPoint(
			float [] cam_orn,
			float [] cam_pos,
			float [] o_point) {

		float [] fL_orn = sub(cam_orn, cam_pos);
		float fL_angx = ang(
				new float []{0.0f, fL_orn[1], fL_orn[2]},
				new float []{0.0f, 0.0f, 0.0f},
				new float []{0.0f, 0.0f, 1.0f});
		//fL_angx = 0;
		float [][] fL_matrix_x = gen_rot_X(-fL_angx/180.0f*(float)Math.PI);
		float fL_angy = ang(
				new float []{fL_orn[0], 0.0f, fL_orn[2]},
				new float []{0.0f, 0.0f, 0.0f},
				new float []{0.0f, 0.0f, 1.0f});
		// fL_angy = 0;
		float [][] fL_matrix_y = gen_rot_Y(fL_angy/180.0f*(float)Math.PI);
		//System.out.println("Ang y: "+fL_angy);
		float fL_angz = ang(
				new float []{fL_orn[0], fL_orn[1], 0.0f},
				new float []{0.0f, 0.0f, 0.0f},
				new float []{1.0f, 0.0f, 0.0f});
		/**TODO: check this**/ fL_angz= 0;
		float [][] fL_matrix_z = gen_rot_Z((90-fL_angz)/180.0f*(float)Math.PI);
		//System.out.println("Ang z: "+fL_angz);

		/**float f2L_fL_displacement_world_mag = VectorTools.mag(new float[]{
                f2L_fL_displacement_world[0], f2L_fL_displacement_world[1], 0
        });**/
		float [] n_point =
				mult(fL_matrix_z,
						mult(fL_matrix_y,
								mult(fL_matrix_x, o_point)));
		return n_point;
	}
	
	/**Multiply a matrix with a vector: m1 * v1 **/
	public static float[] mult(float[][]m1, float[]v1) {
		
		float [] result = new float[m1.length];

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1.length; r1++) {	

				/**Multiply r1 by column c2**/
				float r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1[r1].length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[r1][c1]*v1[c1]);
				}

				result[r1] = r1_x_m2c2;
			}

		return result;
	}
	
	/**Multiply a vector with a matrix: m1 * v1 **/
	public static float[] mult_r(float[]v1, float[][]m1) {
		
		float [] result = new float[m1.length];

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1[0].length; r1++) {	

				/**Multiply r1 by column c2**/
				float r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1.length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[c1][r1]*v1[c1]);
				}

				result[r1] = r1_x_m2c2;
			}

		return result;
	}
	
	/**Multiply two matrixes: m1 * m2**/
	public static float[][] mult(float[][]m1, float[][]m2) {

		float [][] result = new float[m1.length][m2[0].length];

		/**Iterate through the columns of m2**/
		for(int c2 = 0; c2 < m2[0].length; c2++) {

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1.length; r1++) {	

				/**Multiply r1 by column c2**/
				float r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1[r1].length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[r1][c1]*m2[c1][c2]);
				}

				result[r1][c2] = r1_x_m2c2;
			}
		}

		return result;
	}

	/**Subtract two vector: v1 - v2**/
	public static float [] sub(float [] v1, float []v2) {
		float [] ans = new float[v1.length];
		for(int i = 0; i < ans.length; i++) {
			ans[i] = v1[i]-v2[i];
		}
		return ans;
	}

	/**Multiply vector by scalar: s * v2**/
	public static float [] mult(float s, float []v2) {
		
		float [] ans = new float[v2.length];
		for(int i = 0; i < ans.length; i++) {
			ans[i]=s*v2[i];
		}
		return ans;
	}


	/**Generate point B, given point A, a direction, and length**/
	public static float [] getVector(float[] p1, float [] dir, float length) {

		//System.out.println("P1: "+p1[0]+" "+p1[1]+" "+p1[2]);
		float [] p_t1 = add(dir, p1);
		//System.out.println("PT1: "+p_t1[0]+" "+p_t1[1]+" "+p_t1[2]);

		float mag1 = mag(sub(p_t1, p1));
		float fac = length/mag1;



		return add(p1, mult(fac, dir));
	}

	/**Add two vectors: v1 + v2**/
	public static float [] add(float [] v1, float []v2) {
		float [] ans = new float[v1.length];
		for(int i = 0; i < v1.length; i++) {
			ans[i] = v1[i] + v2[i];
		}
		return ans;
	}

	/**Calculate distance between two vectors**/
	public static float distance(float [] v1, float []v2) {
		return mag(sub(v1, v2));
	}

	/**Calculate the distance between two vertexes by
	 * finding the average distance between the point
	 * in each dimension. Useful if the distance is
	 * required to compare values, but where the exact
	 * value of the distance is not required**/
	public static float distanceQuick(float [] v1, float []v2) {
		
		float dist = 0.0f;
		for(int i = 0; i < v1.length; i++) {
			dist += Math.abs((v2[i]-v1[i]));
		}
		
		return dist;
	}

	/**Calculate the cross product between two vertexes**/
	public static float [] cross(float [] v1, float []v2) {

		float [] cross = new float []{(v1[1]*v2[2] - v1[2]*v2[1]),
				(v1[2]*v2[0] - v1[0]*v2[2]),
				(v1[0]*v2[1] - v1[1]*v2[0])};

		return cross;
	}

	/**Calculate the angle at vertex 'c' between vertex 'a' and 'b'**/
	public static float ang(float [] a, float [] c, float [] b) {
		float [] v1 = new float[]{a[0]-c[0], a[1]-c[1], a[2]-c[2]};
		float [] v2 = new float[]{b[0]-c[0], b[1]-c[1], b[2]-c[2]};

		double angle = Math.acos(
				dot(v1, v2)/(mag(v1)*mag(v2))
				);

		return (float) ((180*angle)/Math.PI);
	}

	/**Calculate the dot product of two vectors: v1 * v2**/
	public static float dot(float [] v1, float [] v2) {
		float dot = 0.0f;
		for(int i =0; i < v1.length; i++) {
			dot += v1[i]*v2[i];
		}
		return dot;
	}

	/**Calculate norm of the given vector: |v1|**/
	public static float [] norm(float [] v1) {
		float magnitude = mag(v1);
		float [] norm = new float[v1.length];
		for(int i=0; i < norm.length; i++) {
			norm[i] = v1[i]/magnitude;
		}
		return norm;
	}

	/**Calculate magnitude of the vector**/
	public static float mag(float [] v1) {
		double sum = 0.0f;
		for(int i =0; i < v1.length; i++) {
			sum = sum+Math.pow(v1[i], 2);
		}
		return (float) Math.sqrt(sum);
	}

	/**Get the matrix transpose**/
	public static float [][] getTranspose(float [][] m) {

		float [][] transpose = new float[m[0].length][m.length];
		
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m[i].length; j++) {
				transpose[j][i] = m [i][j];
			}
		}
		
		return transpose;
	}


	/**Generate a rotation matrix for x-axis**/
	public static double[][] gen_rot_X(double ang) {
		return new double[][]{
			new double[]{1.0, 	0.0, 					0.0},
			new double[]{0.0,	Math.cos(ang),	-Math.sin(ang)},
			new double[]{0.0,	Math.sin(ang),	Math.cos(ang)}
		};
	}
	/**Generate a rotation matrix for y-axis**/
	public static double[][] gen_rot_Y(double ang) {
		return new double[][]{
			new double[]{Math.cos(ang),	0.0,	Math.sin(ang)},
			new double[]{0.0, 					1.0, 	0.0},
			new double[]{-Math.sin(ang),	0.0,	Math.cos(ang)}
		};
	}
	/**Generate a rotation matrix for z-axis**/
	public static double[][] gen_rot_Z(double ang) {
		return new double[][]{
			new double[]{Math.cos(ang),	-Math.sin(ang),	0.0},
			new double[]{Math.sin(ang),	Math.cos(ang), 	0.0},
			new double[]{0.0, 					0.0, 					1.0}
		};
	}

	/**Multiply a matrix with a vector: m1 * v1 **/
	public static double[] mult(double[][]m1, double[]v1) {
		
		double [] result = new double[m1.length];

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1.length; r1++) {	

				/**Multiply r1 by column c2**/
				double r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1[r1].length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[r1][c1]*v1[c1]);
				}

				result[r1] = r1_x_m2c2;
			}

		return result;
	}
	
	/**Multiply two matrixes: m1 * m2**/
	public static double[][] mult(double[][]m1, double[][]m2) {

		double [][] result = new double[m1.length][m2[0].length];

		/**Iterate through the columns of m2**/
		for(int c2 = 0; c2 < m2[0].length; c2++) {

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1.length; r1++) {	

				/**Multiply r1 by column c2**/
				double r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1[r1].length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[r1][c1]*m2[c1][c2]);
				}

				result[r1][c2] = r1_x_m2c2;
			}
		}

		return result;
	}

	/**Subtract two vector: v1 - v2**/
	
	/**Multiply a vector with a matrix: m1 * v1 **/
	public static double[] mult_r(double[]v1, double[][]m1) {
		
		double [] result = new double[m1.length];

			/**Iterate through the rows of m1**/
			for(int r1 = 0; r1 < m1[0].length; r1++) {	

				/**Multiply r1 by column c2**/
				double r1_x_m2c2 = 0.0f;
				for(int c1 = 0; c1 < m1.length; c1++) {
					r1_x_m2c2 = r1_x_m2c2 + (m1[c1][r1]*v1[c1]);
				}

				result[r1] = r1_x_m2c2;
			}

		return result;
	}
	
	public static double [] sub(double [] v1, double []v2) {
		double [] ans = new double[v1.length];
		for(int i = 0; i < ans.length; i++) {
			ans[i] = v1[i]-v2[i];
		}
		return ans;
	}

	/**Multiply vector by scalar: s * v2**/
	public static double [] mult(double s, double []v2) {
		
		double [] ans = new double[v2.length];
		for(int i = 0; i < ans.length; i++) {
			ans[i]=s*v2[i];
		}
		return ans;
	}


	/**Generate point B, given point A, a direction, and length**/
	public static double [] getVector(double[] p1, double [] dir, double length) {

		//System.out.println("P1: "+p1[0]+" "+p1[1]+" "+p1[2]);
		double [] p_t1 = add(dir, p1);
		//System.out.println("PT1: "+p_t1[0]+" "+p_t1[1]+" "+p_t1[2]);

		double mag1 = mag(sub(p_t1, p1));
		double fac = length/mag1;



		return add(p1, mult(fac, dir));
	}

	/**Add two vectors: v1 + v2**/
	public static double [] add(double [] v1, double []v2) {
		double [] ans = new double[v1.length];
		for(int i = 0; i < v1.length; i++) {
			ans[i] = v1[i] + v2[i];
		}
		return ans;
	}

	/**Calculate distance between two vectors**/
	public static double distance(double [] v1, double []v2) {
		return mag(sub(v1, v2));
	}

	/**Calculate the distance between two vertexes by
	 * finding the average distance between the point
	 * in each dimension. Useful if the distance is
	 * required to compare values, but where the exact
	 * value of the distance is not required**/
	public static double distanceQuick(double [] v1, double []v2) {
		
		double dist = 0.0f;
		for(int i = 0; i < v1.length; i++) {
			dist += Math.abs((v2[i]-v1[i]));
		}
		
		return dist;
	}

	/**Calculate the cross product between two vertexes**/
	public static double [] cross(double [] v1, double []v2) {

		double [] cross = new double []{(v1[1]*v2[2] - v1[2]*v2[1]),
				(v1[2]*v2[0] - v1[0]*v2[2]),
				(v1[0]*v2[1] - v1[1]*v2[0])};

		return cross;
	}

	/**Calculate the angle at vertex 'c' between vertex 'a' and 'b'**/
	public static double ang(double [] a, double [] c, double [] b) {
		double [] v1 = new double[]{a[0]-c[0], a[1]-c[1], a[2]-c[2]};
		double [] v2 = new double[]{b[0]-c[0], b[1]-c[1], b[2]-c[2]};

		double angle = Math.acos(
				dot(v1, v2)/(mag(v1)*mag(v2))
				);

		return 180.0*angle/Math.PI;
	}

	/**Calculate the dot product of two vectors: v1 * v2**/
	public static double dot(double [] v1, double [] v2) {
		double dot = 0.0;
		for(int i =0; i < v1.length; i++) {
			dot += v1[i]*v2[i];
		}
		return dot;
	}

	/**Calculate norm of the given vector: |v1|**/
	public static double [] norm(double [] v1) {
		double magnitude = mag(v1);
		double [] norm = new double[v1.length];
		for(int i=0; i < norm.length; i++) {
			norm[i] = v1[i]/magnitude;
		}
		return norm;
	}

	/**Calculate magnitude of the vector**/
	public static double mag(double [] v1) {
		double sum = 0.0f;
		for(int i =0; i < v1.length; i++) {
			sum = sum+Math.pow(v1[i], 2);
		}
		return Math.sqrt(sum);
	}

	/**Get the matrix transpose**/
	public static double [][] getTranspose(double [][] m) {
		
		double [][] transpose = new double[m[0].length][m.length];
		
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m[i].length; j++) {
				transpose[j][i] = m [i][j];
			}
		}
		
		return transpose;
	}
}
