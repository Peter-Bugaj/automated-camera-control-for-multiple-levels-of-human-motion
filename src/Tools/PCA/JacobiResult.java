package Tools.PCA;

import java.util.Arrays;
import java.util.Hashtable;

import Tools.VectorTools;

public class JacobiResult {

	private double [][] vectors_d = null;
	private float [][] vectors_f = null;

	private double [] values_d = null;
	private float [] values_f = null;

	private boolean sorted = false;

	public JacobiResult() {
	}


	/**Set the eigenvalues**/
	public void setValues(double [] values) {
		sorted = false;
		this.values_d=values;
	}
	/**Set the eigenvalues**/
	public void setValues(float [] values) {
		sorted = false;
		this.values_f=values;
	}
	/**Set the eigenvectors**/
	public void setVectors(double [][] vectors) {
		sorted = false;
		this.vectors_d=VectorTools.getTranspose(vectors);
		for(int i = 0; i < this.values_d.length; i++) {
			this.vectors_d[i] = VectorTools.mult(-1, vectors_d[i]);
		}
	}
	/**Set the eigenvectors**/
	public void setVectors(float [][] vectors) {
		sorted = false;
		this.vectors_f=VectorTools.getTranspose(vectors);
		for(int i = 0; i < this.values_f.length; i++) {
			this.vectors_f[i] = VectorTools.mult(-1, vectors_f[i]);
		}
	}


	public double [] getVector_d(int n) {
		if(!sorted)sort_d();
		return vectors_d[n];
	}
	public float [] getVector_f(int n) {
		if(!sorted)sort_f();
		return vectors_f[n];
	}
	
	/**Sort the eigen values, and eigen vectors corresponding to the sorted values**/
	public void sort_f() {
		
		/**Store the associated between eigen values and eigen vectors**/
		Hashtable<String, float[]> temp = new Hashtable<String, float[]>();
		for(int i = 0; i < values_f.length; i++) {
			temp.put(values_f[i]+"", vectors_f[i].clone());
		}
		Arrays.sort(values_f);
		
		for(int i = 0; i < values_f.length; i++) {
			vectors_f[i] = temp.get(values_f[i]+"");
		}
	}
	/**Sort the eigen values, and eigen vectors corresponding to the sorted values**/
	public void sort_d() {
		
		/**Store the associated between eigen values and eigen vectors**/
		Hashtable<String, double[]> temp = new Hashtable<String, double[]>();
		for(int i = 0; i < values_d.length; i++) {
			temp.put(values_d[i]+"", vectors_d[i].clone());
		}
		Arrays.sort(values_d);
		
		for(int i = 0; i < values_d.length; i++) {
			vectors_d[i] = temp.get(values_d[i]+"");
		}
	}
}
