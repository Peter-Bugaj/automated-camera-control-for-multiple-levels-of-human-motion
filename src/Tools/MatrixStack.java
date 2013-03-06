package Tools;

import java.util.LinkedList;

public class MatrixStack {

	private LinkedList<double[][]>matrixStack = null;
	
	public static void main(String [] args) {
		LinkedList<String> g = new LinkedList<String>();
		g.push("A");
		g.push("B1");
		g.push("B2");
		g.push("C");
		System.out.println(g.getFirst());
		System.out.println(g.getLast());
		System.out.println(g.pop());
		g.push("D");
		System.out.println(g.getFirst());
		System.out.println(g.getLast());
	}
	
	public MatrixStack() {
		matrixStack = new LinkedList<double[][]>();
	}
	
	public void pushMatrix(double[][] m) {
		if(matrixStack.size() > 0) {
			double [][]top = getTop();
			double [][]next = VectorTools.mult(m,top);
			matrixStack.push(next);
		} else {
			matrixStack.push(m);
		}
	}
	public void popMatrix() {
		if(matrixStack.size() > 0) {
			matrixStack.pop();
		}
	}
	public double[][]getTop() {
		if(matrixStack.size() > 0) {
			return matrixStack.getFirst();
		} else {
			return new double[][]{
					new double[]{1.0,0.0,0.0},
					new double[]{0.0,1.0,0.0},
					new double[]{0.0,0.0,1.0}
			};
		}
	}
}
