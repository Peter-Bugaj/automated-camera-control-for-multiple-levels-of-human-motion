package IO.ASFData;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import CameraMotion.ThirdLevelMotion.ThirdLevelFrameGroup;
import Tools.VectorTools;

public class SkeletonNode {

	/**Children belonging to this node**/
	private Vector<SkeletonNode> children = new Vector<SkeletonNode>();
	/**Name of the point on the body defined by this node**/
	private String node_name = "undef";
	/**Parent of this node**/
	private SkeletonNode parent = null;

	/**Bone data for this node**/
	private BoneData boneData = null;

	/**Point present at this node**/
	private double [] point = null;
	/**Displacement of this node from the previous node**/
	private double [] displacement = null;
	/**The motion value present at this node**/
	private double motion_value = -1.0f;
	/**Whether or not this node is stationary**/
	private boolean is_stationary = false;
	/**Whether or not this node belong to first level motion**/
	private boolean at_first_level = false;
	/**Size of the skeleton stored at this node**/
	double size = -1;
	/**Number of nodes stored under this root (including the root)))**/
	private int mag = -1;

	/**A third level frame group associated with this node, if any**/
	private ThirdLevelFrameGroup tlfg = null;


	/**Class constructor**/
	public SkeletonNode(String node_name) {
		this.node_name=node_name;
	}
	/**Method for cloning**/
	public SkeletonNode clone() {

		SkeletonNode sn = new SkeletonNode(this.node_name);
		sn.setPoint(this.point.clone());
		sn.setParent(null);
		Enumeration<SkeletonNode> children_enum = children.elements();
		while(children_enum.hasMoreElements()) {
			SkeletonNode next_child = children_enum.nextElement();

			SkeletonNode cloned_child = next_child.clone();
			cloned_child.setParent(sn);
			sn.addChild(cloned_child);
		}

		return sn;
	}
	/**Method for cloning**/
	public String toString(String tab) {

		StringBuffer str = new StringBuffer(tab);
		for(int i = 0; i < point.length; i++) {
			str.append(" "+point[i]);
		}
		str.append(" (");

		Enumeration<SkeletonNode> children_enum = children.elements();
		while(children_enum.hasMoreElements()) {
			SkeletonNode next_child = children_enum.nextElement();
			str.append(next_child.toString(tab+tab));
		}
		str.append(") ");
		return str.toString();
	}
	/**Get the enumeration of the nodes present under this node**/
	public SkeletonNode[] getIterator() {
		Vector<SkeletonNode> vec = new Vector<SkeletonNode>();
		vec.add(this);
		SkeletonNode[]children = getChildren();
		for(int i = 0; i < children.length; i++) {
			SkeletonNode[] sub_elements = children[i].getIterator();
			for(int j = 0; j < sub_elements.length; j++) {
				vec.add(sub_elements[j]);
			}
		}
		SkeletonNode[] vec_arr = new SkeletonNode[vec.size()];
		vec.toArray(vec_arr);
		return vec_arr;
	}

	/**Set the bone data for this node**/
	public void setBoneData(BoneData boneData) {
		this.boneData=boneData;
	}

	/**Add a child node to this node**/
	public void addChild(SkeletonNode child) {
		children.add(child);
	}

	/**Add a parent to this node**/
	public void setParent(SkeletonNode parent) {
		this.parent=parent;
	}

	/**Associate a point with this node**/
	public void setPoint(double [] point) {
		this.point=point.clone();
	}

	/**Set a displacement value for this node**/
	public void setDisplacement(double [] displacement) {
		this.displacement= new double []{displacement[0],displacement[1],displacement[2]};
		if(VectorTools.mag(displacement)>10) {
			double[]a=new double[0];
			a[9]=a[4];
		}
	}

	/**Set a value describing motion for this node**/
	public void setMotionValue(double motion_value) {
		this.motion_value = motion_value;
	}

	/**Indicate whether or not this node is defined as being stationary**/
	public void setStationary(boolean is_stationary) {
		this.is_stationary = is_stationary;
	}

	/**Set the third level frame group for this node**/
	public void setTLFG(ThirdLevelFrameGroup tlfg) {
		this.tlfg = tlfg;
	}

	/**Indicate this skeleton is part of first level motion**/
	public void setFirstLevelIndicator() {
		at_first_level=true;
		SkeletonNode [] temp = getChildren();
		for(int i = 0; i < temp.length; i++) {
			temp[i].setFirstLevelIndicator();
		}
	}


	/**Get the bone data for this node**/
	public BoneData getBoneData() {
		return boneData;
	}

	/**Get the parent node belonging to this node**/
	public SkeletonNode getParent() {
		return parent;
	}

	/**Get the child nodes belonging to this node**/
	public SkeletonNode[] getChildren() {
		SkeletonNode[] temp = new SkeletonNode[children.size()];
		children.toArray(temp);
		return temp;
	}

	/**Get the segment name represented by this node**/
	public String getName() {
		return node_name;
	}

	/**The point of the segment at this node**/
	public double [] getPoint() {
		return point.clone();
	}

	/**Get the displacement value stored at this node**/
	public double [] getDisplacement() {
		if(displacement==null) {
			return null;
		}
		return new double[]{displacement[0],displacement[1],displacement[2]};
	}

	/**Get the motion value stored at this node**/
	public double getMotionValue() {
		return motion_value;
	}

	/**Whether or not this node is stationary**/
	public boolean isStationary() {
		return is_stationary;
	}
	
	/**Indication of whether or not this node belongs to a first level motion scene**/
	public boolean isFirstLevel() {
		return at_first_level;
	}

	public double getSize() {
		if(size < 0) {

			double calc_size = 0;

			Enumeration<SkeletonNode> children_enum = children.elements();
			double [] sizes = new double[children.size()];
			int sizes_counter = 0;
			while(children_enum.hasMoreElements()) {

				SkeletonNode next_child = children_enum.nextElement();

				double dist_from_child =
						VectorTools.mag(
								VectorTools.sub(next_child.getPoint(), point)
								);
				sizes[sizes_counter++] = dist_from_child + next_child.getSize();
			}

			Arrays.sort(sizes);
			if(sizes.length > 0) {
				calc_size += sizes[sizes.length-1];
			}
			if(sizes.length > 1) {
				calc_size += sizes[sizes.length-2];
			}

			size = calc_size;
		}
		return size;
	}

	/**Get the number of nodes stored under this root (including the root)))**/
	public int getMag() {
		if(mag < 0) {

			int calc_mag = 1;

			Enumeration<SkeletonNode> children_enum = children.elements();
			while(children_enum.hasMoreElements()) {

				SkeletonNode next_child = children_enum.nextElement();
				calc_mag += next_child.getMag();
			}
			
			mag = calc_mag;
			return mag;
		}
		return mag;
	}
	
	public ThirdLevelFrameGroup getTLFG() {
		return tlfg;
	}
}
