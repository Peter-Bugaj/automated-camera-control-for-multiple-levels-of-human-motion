package CameraMotion.ThirdLevelMotion;

import main.ControlVariables;

public class SquareRegion {

	/**  x1 -------------------|| **/
	private double [] x1 = null;
	/**  || -------------------x2 **/
	private double [] x2 = null;

	/**  || -------------------|| **/
	/**  || -------------------|| **/
	/**  || -------------------|| **/

	/**  || -------------------x3 **/
	private double [] x3 = null;
	/**  x4 -------------------|| **/
	private double [] x4 = null;

	/**The area of the square**/
	private double area = -1;
	/**Longest side of this circle**/
	private double longest_width = -1;


	/**Testing going on here...**/
	public static void main(String [] args) {
		SquareRegion sr = new SquareRegion(
				new double[]{3,10},
				new double[]{6,10},
				new double[]{6, 5},
				new double[]{3, 5},0
				);

		double test = sr.getOverlapArea(
				new double[]{6.1,11},
				new double[]{3.2,5.5}
				);
		System.out.println(test);
	}
	private double j;
	/**Class constructor**/
	public SquareRegion(
			double [] x1,
			double [] x2,
			double [] x3,
			double [] x4, double j) {
		this.j=j;
		this.x1=x1;
		this.x2=x2;
		this.x3=x3;
		this.x4=x4;
	}

	
	/**See if the segment  defined by the  root
	 * and leaf overlaps the square boundary**/
	public double getOverlapArea(
			double [] root, double [] leaf) {

		/**------------------------------------------------------**/
		/**======================================================**/
		double area = getArea();
		if(x1[1] != x2[1] || x3[1] != x4[1] || x2[0] != x3[0] || x4[0] != x1[0]) {
			System.out.println("Invalid square");
			System.exit(1);
		}
		double min_segment_width =
				Math.max((x1[0]-x2[0]), (x2[1]-x3[1]))/
				ControlVariables.segment_witdh_factor/2;

		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/
		double length_top = 0.0;
		double length_bot = 0.0;
		double length_lef = 0.0;
		double length_rig = 0.0;

		boolean from_center = false;
		boolean bot = false;
		boolean top = false;
		boolean lef = false;
		boolean rig = false;

		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/

		/**Case where x1x2 is intersected from the outside**/
		if((leaf[1] < x1[1])   &&   (root[1] >= x1[1])) {
			length_top = Math.abs(x1[1]-leaf[1]);
			top = true;
			//System.out.println("One: "+length_top);
		}
		/**Case where x1x2 is intersected from the bottom from the center**/
		if((leaf[1] >= x1[1])   &&   (root[1] < x1[1])) {
			from_center = true;
			length_top = Math.abs(x1[1]-root[1]);
			top = true;
			//System.out.println("First check: "+from_center);
		}		

		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/

		/**Case where x3x4 is intersected from the bottom**/
		if((leaf[1] > x3[1])   &&   (root[1] <= x4[1])) {
			length_top = Math.abs(x3[1]-leaf[1]);
			//System.out.println("Two: "+length_bot);
			bot = true;
		}
		/**Case where x3x4 is intersected from the bottom from the center**/
		if((leaf[1] <= x3[1])   &&   (root[1] > x4[1])) {
			from_center = true;
			length_bot = Math.abs(x3[1]-root[1]);
			//System.out.println("Second check: "+from_center);
			bot = true;
		}	

		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/

		/**Case where x2x3 is intersected from the right**/
		if((leaf[0] < x2[0])   &&   (root[0] >= x2[0])) {
			length_rig = Math.abs(x2[0]-leaf[0]);
			//System.out.println("Three: "+length_rig);
			rig = true;
		}
		/**Case where x2x3 is intersected from the right from the center**/
		if((leaf[0] >= x2[0])   &&   (root[0] < x2[0])) {
			from_center = true;
			length_top = Math.abs(x2[0]-root[0]);
			//System.out.println("Third check: "+from_center);
			rig = true;
		}
		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/

		/**Case where x4x1 is intersected from the left**/
		if((leaf[0] > x4[0])   &&   (root[0] <= x4[0])) {
			//System.out.println("Four: "+length_lef);
			length_lef = Math.abs(x4[0]-leaf[0]);
			lef = true;
		}
		/**Case where x4x1 is intersected from the left from the center**/
		if((leaf[0] <= x4[0])   &&   (root[0] > x4[0])) {
			from_center = true;
			length_lef = Math.abs(x4[0]-root[0]);
			//System.out.println("Fourth check: "+from_center);
			lef = true;
		}
		/**------------------------------------------------------**/
		/**======================================================**/
		/**======================================================**/
		/**------------------------------------------------------**/
		if(bot || top || lef || rig) {
			length_bot = Math.max(length_bot, min_segment_width);
			length_top = Math.max(length_top, min_segment_width);
			length_lef = Math.max(length_lef, min_segment_width);
			length_rig = Math.max(length_rig, min_segment_width);
		}


		double sub_area = Math.min(
				(length_bot+length_top)*
				(length_rig+length_lef), area);
		if(from_center) {
			//System.out.println("\tsub_area: "+sub_area);
			sub_area = area;
		}
		//System.out.println((length_bot+length_top));
		//System.out.println((length_rig+length_lef));

		//System.out.println(sub_area);
		//System.out.println(area);
		/**======================================================**/
		/**------------------------------------------------------**/
		return sub_area/area;
	}

	/**Get the area of the square**/
	public double getArea() {
		if(area < 0) {
			area = Math.abs((x1[0]-x2[0])*(x2[1]-x3[1]));
			if(area == 0) {
				System.exit(1);
			}
		}
		return area;
	}
	
	/**Get the longest width of the square**/
	public double getLongestWdith() {
		if(longest_width < 0) {
			longest_width = Math.max(Math.abs(x1[0]-x2[0]), Math.abs(x2[1]-x3[1]));
			if(longest_width == 0) {
				System.exit(1);
			}
		}
		if(j != Math.abs(x1[0]-x2[0]) && j != Math.abs(x2[1]-x3[1])) {
			System.out.println(j);
			System.out.println(Math.abs(x1[0]-x2[0]));
			System.out.println(Math.abs(x2[1]-x3[1]));
			System.exit(1);
		}
		return longest_width;
	}
}
