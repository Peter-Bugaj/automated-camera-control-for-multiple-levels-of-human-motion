package CameraMotion.ThirdLevelMotion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import main.ControlVariables;

import IO.ASFData.SkeletonNode;
import IO.ASFData.SkeletonWrapper;

import Tools.VectorTools;
import Tools.PCA.PCATools;


public class ThirdLevelMotionControl {

	/**The list of skeleton nodes this controller will access and use**/
	private SkeletonNode [] skeletonNodes = null;
	/**List  of wrappers for  each skeleton to help keep track of  information
	 * about each skeleton within the implementation of specific algorithms**/
	private  SkeletonWrapper []skeletonWrappers = null;
	/**The centroids to be calculated for each skeleton**/
	private double[][] centroid_per_frame = null;
	/**The camera orientations for each skeleton**/
	private float[][]cam_orns = null;

	/**Class constructor**/
	public ThirdLevelMotionControl(
			SkeletonNode [] skeletonNodes,
			float[][]cam_orns) {
		
		this.skeletonNodes = skeletonNodes;
		this.cam_orns = cam_orns;

		/**Wrap the skeletons into objects containing a hashtable
		 * of the skeleton and the associated main root**/
		skeletonWrappers = new SkeletonWrapper[skeletonNodes.length];
		for(int i = 0; i < skeletonNodes.length; i++) {
			skeletonWrappers[i] = new SkeletonWrapper(skeletonNodes[i]);
		}
	}


	/**Get the third level motion scenes given
	 * the node skeletons stored per  frame**/
	public ThirdLevelMotionScene [] getThirdLevelScenes() {

		/**-------------------------------------------------------**/
		SkeletonNode[][] stationary_nodes = getStationaryNodes();
		/**-------------------------------------------------------**/
		/**-------------------------------------------------------**/
		getSigNodes(stationary_nodes);
		/**-------------------------------------------------------**/
		ThirdLevelFrameGroup [] frame_groups =
				groupIntoScenes();
		/**-------------------------------------------------------**/
		/**-------------------------------------------------------**/
		ThirdLevelFrameGroup [] filtered_groups =
				removeOverlappingScenes(frame_groups);
		/**-------------------------------------------------------**/
		/**-------------------------------------------------------**/
		/**Wrap the skeletons into objects containing a hashtable
		 * of the skeleton and the associated main root**/
		skeletonWrappers = new SkeletonWrapper[skeletonNodes.length];
		for(int i = 0; i < skeletonNodes.length; i++) {
			skeletonWrappers[i] = new SkeletonWrapper(skeletonNodes[i]);
		}
		/**-------------------------------------------------------**/
		/**-------------------------------------------------------**/
		/**Organize these frame groups into third level scenes**/
		ThirdLevelMotionScene [] thirdLevelMotionScenes =
				new ThirdLevelMotionScene[filtered_groups.length];
		for(int i = 0; i < filtered_groups.length; i++) {
			thirdLevelMotionScenes[i] = new ThirdLevelMotionScene(
					filtered_groups[i],
					skeletonWrappers,
					skeletonNodes,
					centroid_per_frame,
					cam_orns
					);
		}
		/**-------------------------------------------------------**/
		/**-------------------------------------------------------**/
		return thirdLevelMotionScenes;
		/**-------------------------------------------------------**/
	}

	/**Get the root nodes out of the list of
	 * node  skeletons  stored  per frame**/
	private SkeletonNode [][] getStationaryNodes() {

		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**Get the skeleton centroid for each frame**/
		centroid_per_frame = new double[skeletonNodes.length][3];
		for(int i = 0; i < skeletonNodes.length; i++) {
			double [] centroid_i = new double[]{0,0,0};
			double node_counter = 0;
			
			SkeletonNode[] root_iter = skeletonNodes[i].getIterator();
			for(int j  = 0; j < root_iter.length; j++) {
				centroid_i = VectorTools.add(centroid_i, root_iter[j].getPoint());
				node_counter++;
			}

			centroid_i = VectorTools.mult((1.0/node_counter), centroid_i);
			centroid_per_frame[i] = centroid_i;
		}
		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**=============================================================**/
		/**-------------------------------------------------------------**/
		/**Modify  the  skeletons within  each  frame,
		 * subtracting the centroid from each point**/
		for(int i = 0; i < skeletonNodes.length; i++) {
			SkeletonNode[] root_iter = skeletonNodes[i].getIterator();
			for(int j  = 0; j < root_iter.length; j++) {
				double [] temp_point = root_iter[j].getPoint();
				double [] mod_point = VectorTools.sub(temp_point, centroid_per_frame[i]);
				root_iter[j].setPoint(mod_point);
			}
		}
		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**=============================================================**/
		/**-------------------------------------------------------------**/
		/**Calculate  the  displacement  of each node  in each
		 * skeleton  in each  frame.    Displacement is  based
		 * on the node in current frame, nodes in two previous
		 * frames,   and  nodes  in  two   upcoming  frames**/
		int group_size = ControlVariables.third_level_disp_filter_size;
		int group_size_half = (group_size/2)+(group_size % 2);
		for(int i = 0; i < skeletonNodes.length; i++) {

			/**-----------------------------------------------------**/
			/**|||||||||||||||||||||||||||||||||||||||||||||||||||||**/
			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			int total_filter_size_i =
					(group_size_half*2) +
					(Math.min(i-group_size_half, 0)) +
					(Math.min(skeletonNodes.length - (i+group_size_half+1), 0));
			/**-----------------------------------------------------**/
			/**|||||||||||||||||||||||||||||||||||||||||||||||||||||**/
			/**-----------------------------------------------------**/
			for(int k = 0; k < root_iter.length; k++) {

				/**---------------------------------------------**/
				/**<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<**/
				String next_root_name = root_iter[k].getName();

				int filter_spread_counter = 0;
				double [] disp_sum = new double[]{0,0,0};
				/**---------------------------------------------**/
				/**<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<**/
				/**---------------------------------------------**/
				for(int j = i-group_size_half; j < i; j++) {
					if(j < 0) continue;
					else {
						double [] temp_point_a = skeletonWrappers[j].getFromHash(next_root_name).getPoint();
						double [] temp_point_b = skeletonWrappers[j+1].getFromHash(next_root_name).getPoint();
						double [] temp_disp = VectorTools.sub(temp_point_b,temp_point_a);

						disp_sum = VectorTools.add(disp_sum, temp_disp);
						filter_spread_counter++;
					}
				}
				/**---------------------------------------------**/
				/**<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<**/
				/**---------------------------------------------**/
				for(int j = i; j < i+group_size_half; j++) {
					if((j+1) >= skeletonWrappers.length) continue;
					else {
						double [] temp_point_a = skeletonWrappers[j].getFromHash(next_root_name).getPoint();
						double [] temp_point_b = skeletonWrappers[j+1].getFromHash(next_root_name).getPoint();
						double [] temp_disp = VectorTools.sub(temp_point_b,temp_point_a);

						disp_sum = VectorTools.add(disp_sum, temp_disp);
						filter_spread_counter++;
					}
				}
				/**---------------------------------------------**/
				/**<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<**/
				/**---------------------------------------------**/
				if(total_filter_size_i != filter_spread_counter) {
					System.out.println("A1278");System.exit(1);
				}
				disp_sum = VectorTools.mult((1.0/(filter_spread_counter+0.0)), disp_sum);
				root_iter[k].setDisplacement(disp_sum);
				/**<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<**/
				/**---------------------------------------------**/
			}
			/**|||||||||||||||||||||||||||||||||||||||||||||||||||||**/
			/**-----------------------------------------------------**/
		}
		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**=============================================================**/
		/**-------------------------------------------------------------**/
		/**Go  through all the skeletons and store all  the displacement in
		 * one array, to be later used for standard deviation calculation*/
		FileWriter f = null;
		try {f = new FileWriter("x.txt");} 
		catch (IOException e1) {e1.printStackTrace();}
		BufferedWriter bf = new BufferedWriter(f);
		
		double [] stds_per_frame = new double[skeletonNodes.length];
		for(int i = 0; i < skeletonNodes.length; i++) {

			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			double [] i_displacements = new double[root_iter.length];

			for(int j = 0; j < root_iter.length; j++){
				try {bf.write(VectorTools.mag(root_iter[j].getDisplacement())+"\t\t");}
				catch (IOException e) {e.printStackTrace();System.out.println("A89791");System.exit(1);}
				i_displacements[j] = (VectorTools.mag(root_iter[j].getDisplacement()));
			}

			try {bf.write(";\n");}
			catch (IOException e) {e.printStackTrace();System.out.println("N7234");System.exit(1);}
			stds_per_frame[i] = PCATools.getStdDev(i_displacements);
		}
		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**-------------------------------------------------------------**/
		/**Find the nodes in each skeleton per frame
		 * with displacement above a certain multiple
		 * of standard deviation of all displacement**/
		SkeletonNode [][] stationary_nodes_per_frame =
				new SkeletonNode[skeletonNodes.length][0];
		for(int i = 0; i < skeletonNodes.length; i++) {

			Vector<SkeletonNode> stationary_nodes_i = new Vector<SkeletonNode>();
			double i_std = stds_per_frame[i];

			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			for(int k = 0; k < root_iter.length; k++) {

				if(VectorTools.mag(root_iter[k].getDisplacement()) < ControlVariables.statDispThres*i_std) {
					stationary_nodes_i.add(root_iter[k]);
					root_iter[k].setStationary(true);
				}
			}

			stationary_nodes_per_frame[i] = new SkeletonNode[stationary_nodes_i.size()];
			stationary_nodes_i.toArray(stationary_nodes_per_frame[i]);
		}
		/**-------------------------------------------------------------**/
		/**=============================================================**/
		/**-------------------------------------------------------------**/
		return stationary_nodes_per_frame;
		/**=============================================================**/
		/**-------------------------------------------------------------**/
	}

	/**Get the significant nodes out of the list
	 * of  node  skeletons  stored per  frame**/
	private void getSigNodes(SkeletonNode[][] stationary_nodes) {

		/**-----------------------------------------------------------------------**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		FileWriter fw = null;
		try {fw = new FileWriter("y.txt");}
		catch (IOException e) {e.printStackTrace();System.out.println("C8923");System.exit(1);}
		BufferedWriter bw = new BufferedWriter(fw);

		for(int i = 0; i < skeletonNodes.length; i++) {

			/**---------------------------------------------------------------**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**Find the leaves for this skeleton**/
			LinkedList<SkeletonNode> leaf_nodes = new LinkedList<SkeletonNode>();
			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			for(int k = 0; k < root_iter.length; k++)
				if( root_iter[k].getChildren().length == 0)
					leaf_nodes.push(root_iter[k]);
			/**---------------------------------------------------------------**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**---------------------------------------------------------------**/
			/**Calculate the significant nodes, starting from each leaf**/
			while(leaf_nodes.size() > 0) {

				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				SkeletonNode next_leaf_node = leaf_nodes.removeLast();
				boolean continue_onto_parent = true;
				if(next_leaf_node.isStationary())
					continue_onto_parent = false;
				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
				if(next_leaf_node.getMotionValue() == -1) {

					/**Compute the angle size for the motion value**/
					double motion_val = getAngleDisplacement(next_leaf_node, i);
					if(next_leaf_node.getChildren().length > 0) {

						SkeletonNode[]children = next_leaf_node.getChildren();
						for(int j = 0; j < children.length; j++) {

							double child_motion_val = children[j].getMotionValue();
							if(children[j].isStationary()) {
								child_motion_val = 0;
								continue_onto_parent = false;
							}
							motion_val += child_motion_val;
						}
						next_leaf_node.setMotionValue(motion_val);
					} else {
						next_leaf_node.setMotionValue(motion_val);
					}
				}
				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
				else if(next_leaf_node.getMotionValue() == -1)
					continue_onto_parent = false;
				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
				if(continue_onto_parent)
					if(next_leaf_node.getParent() != null)
						leaf_nodes.push(next_leaf_node.getParent());

				try {bw.write(next_leaf_node.getMotionValue()+"\t\t");}
				catch (IOException e) {e.printStackTrace();System.out.println("A1287");System.exit(1);}
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
			}
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**---------------------------------------------------------------**/
			try {bw.write(";\n");}
			catch (IOException e) {System.out.println("P70234");System.exit(1);e.printStackTrace();}
		}
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**-----------------------------------------------------------------------**/
	}

	/**Group significant nodes existing in sequence
	 * over a time frame,  into sub-scene groups**/
	private ThirdLevelFrameGroup [] groupIntoScenes() {

		/**-----------------------------------------------------------------------**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**Get all the motion values within each frame into one array
		 * and  computed the standard  deviation of these values. **/
		Vector<Double> all_motion_values = new Vector<Double>();
		for(int i = 0; i < skeletonNodes.length; i++) {
			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			for(int k = 0; k < root_iter.length; k++)
				all_motion_values.add(root_iter[k].getMotionValue());
		}

		double[]all_motion_values_arr = new double[all_motion_values.size()];
		Enumeration<Double> mot_val_enum = all_motion_values.elements();
		int mot_val_counter = 0;

		while(mot_val_enum.hasMoreElements()) {
			double next_val = mot_val_enum.nextElement();
			if(next_val > 0) {
				all_motion_values_arr[mot_val_counter++] = next_val;
			}
		}

		double motion_val_std = PCATools.getStdDev(all_motion_values_arr);
		double num_stds = ControlVariables.num_third_level_motion_val_stds;
		/**-----------------------------------------------------------------------**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**-----------------------------------------------------------------------**/
		/**Create frame groups for each node that has been
		 * found to contain a significant  motion value**/
		for(int i = 0; i < skeletonWrappers.length-ControlVariables.min_slack_size; i++) {

			/**---------------------------------------------------------------**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			SkeletonWrapper main_wrapper = skeletonWrappers[i];
			SkeletonNode main_wrapper_root = main_wrapper.getRoot();
			double main_wrapper_root_size = main_wrapper_root.getSize();

			/**---------------------------------------------------------------**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**---------------------------------------------------------------**/
			SkeletonNode [] root_iter = main_wrapper_root .getIterator();
			int roots_iterated = 0;

			for(int k = 0; k < root_iter.length; k++) {
				roots_iterated++;
				
				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				boolean first_level = 		root_iter[k].isFirstLevel();
				double next_root_mot_val =	root_iter[k].getMotionValue();
				double next_root_size =		root_iter[k].getSize();
				int next_root_mag =			root_iter[k].getMag();
				double [] next_root_disp =	root_iter[k].getDisplacement();

				String next_root_name = 	root_iter[k].getName();
				String next_root_parent_name =
						root_iter[k].getParent() == null ? next_root_name : root_iter[k].getParent().getName();
				/**-------------------------------------------------------**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
				/**See if a frame group can be created from this group.
				 * Not existing in the wrapped hashtable means in  must
				 * belong to some other frame group already.**/
				double motion_val_threshold = num_stds*motion_val_std;
				if(
						(!first_level) &&
						(next_root_mag > 1) &&
						(main_wrapper.getFromHash(next_root_name) != null) &&
						(next_root_mot_val > motion_val_threshold) &&
						(ControlVariables.skeleton_sub_size_factor*next_root_size < main_wrapper_root_size)
						) {

					ThirdLevelFrameGroup tlfg = createFrameGroup(
							skeletonWrappers,
							i,
							next_root_parent_name,
							next_root_mot_val,
							next_root_disp,
							motion_val_threshold);
					if(tlfg.getRightBoundOffset() > ControlVariables.min_slack_size)
						root_iter[k].setTLFG(tlfg);
					
					/**Make sure a left bound is created here**/
					int abc = 11;
					if(abc<0) {
						System.out.println(
								"Frame: "+ i +
								" Roots iterated: "+roots_iterated+
								" Frame Group Size: " + tlfg.getRightBoundOffset());
					}
				}
				/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
				/**-------------------------------------------------------**/
			}
			/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
			/**---------------------------------------------------------------**/
		}
		/**-----------------------------------------------------------------------**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**-----------------------------------------------------------------------**/
		/**Extract all the frame groups collected**/
		Vector<ThirdLevelFrameGroup> all_frame_groups = new Vector<ThirdLevelFrameGroup>();
		for(int i = 0; i < skeletonNodes.length; i++) {

			SkeletonNode [] root_iter = skeletonNodes[i].getIterator();
			for(int k = 0; k < root_iter.length; k++) {

				ThirdLevelFrameGroup temp_TLFG = root_iter[k].getTLFG();
				if(temp_TLFG != null)
					all_frame_groups.add(temp_TLFG);
			}
		}
		ThirdLevelFrameGroup [] all_frame_groups_arr =
				new ThirdLevelFrameGroup [all_frame_groups.size()];
		all_frame_groups.toArray(all_frame_groups_arr);
		/**=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=**/
		/**-----------------------------------------------------------------------**/

		return all_frame_groups_arr;
	}

	/**Create a possible frame group for the segment defined
	 * by root_name for the specific frame number**/
	/**Create the frame group for this root**/
	private ThirdLevelFrameGroup createFrameGroup(
			SkeletonWrapper [] skeletonWrappers,
			int frame_number,
			String root_name,
			double root_mot_val,
			double [] root_disp,
			double motion_val_threshold) {

		/**-----------------------------------------------**/
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		int right_off_set = 0;
		int left_off_set = 0;

		/**How many times a frame can be compared before offset
		 * for finding a group is no longer increased**/
		int comp_tolerance = ControlVariables.tl_compare_tolerance;
		for(int k = frame_number+1; k < skeletonWrappers.length; k++) {

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			SkeletonWrapper temp_wrapper_k = skeletonWrappers[k];
			SkeletonNode root_i_plus_k =
					temp_wrapper_k.getFromHash(root_name);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/
			boolean is_first_level = root_i_plus_k.isFirstLevel();
			double root_mot_val_i_plus_k =
					root_i_plus_k.getMotionValue();

			double val_dif = Math.abs(
					root_mot_val_i_plus_k -
					root_mot_val);
			double a = (root_mot_val_i_plus_k/motion_val_threshold);
			boolean small_sig_change =
					((val_dif*ControlVariables.frame_group_dif_fac/a) <
							Math.max(root_mot_val,root_mot_val_i_plus_k));

			boolean motion_sig_enough = (root_mot_val_i_plus_k > 0);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/
			double [] root_disp_i_plus_k =
					root_i_plus_k.getDisplacement();
			double ang_in_direction = VectorTools.ang(
					root_disp,
					new double[]{0,0,0},
					root_disp_i_plus_k);
			boolean same_direction_change =
					(Math.abs(ang_in_direction) <
							ControlVariables.max_ang_third_level_motion);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/

			if(same_direction_change && small_sig_change && motion_sig_enough && (!is_first_level)) {
				right_off_set++;
			} else {
				comp_tolerance--;
				if(comp_tolerance < 0 || is_first_level) {
					break;
				} else {
					right_off_set++;
				}
			}
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/
		}

		/**-----------------------------------------------**/
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		/**-----------------------------------------------**/
		comp_tolerance = ControlVariables.tl_compare_tolerance;
		for(int k = frame_number-1; k >= 0; k--) {

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			SkeletonWrapper temp_wrapper_k = skeletonWrappers[k];
			SkeletonNode root_i_plus_k =
					temp_wrapper_k.getFromHash(root_name);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/

			double root_mot_val_i_plus_k =
					root_i_plus_k.getMotionValue();

			double val_dif = Math.abs(
					root_mot_val_i_plus_k -
					root_mot_val);
			double a = (root_mot_val_i_plus_k/motion_val_threshold);
			boolean small_sig_change =
					((val_dif*ControlVariables.frame_group_dif_fac/a) < Math.max(root_mot_val,root_mot_val_i_plus_k));

			boolean motion_sig_enough = (root_mot_val_i_plus_k > 0);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/
			boolean is_first_level = root_i_plus_k.isFirstLevel();
			double [] root_disp_i_plus_k =
					root_i_plus_k.getDisplacement();
			double ang_in_direction = VectorTools.ang(
					root_disp,
					new double[]{0,0,0},
					root_disp_i_plus_k);
			boolean same_direction_change =
					(Math.abs(ang_in_direction) <
							ControlVariables.max_ang_third_level_motion);

			/**| | | | | | | | | | | | | | | | | | | |**/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/

			if(same_direction_change && small_sig_change && motion_sig_enough && (!is_first_level)) {
				left_off_set++;
			} else {
				comp_tolerance--;
				if(comp_tolerance < 0 || is_first_level) {
					break;
				} else {
					left_off_set++;
				}
			}
			/**!  !  !  !  !  !  !  !  !  !  !  !  !  **/
			/**| | | | | | | | | | | | | | | | | | | |**/
		}

		/**-----------------------------------------------**/
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		/**-----------------------------------------------**/

		/**Create the frame group**/
		ThirdLevelFrameGroup tlfg =
				new ThirdLevelFrameGroup(
						frame_number-left_off_set,
						root_name,
						root_mot_val);

		tlfg.setRightBoundOffset(right_off_set+left_off_set);
		tlfg.setLeftBoundOffset(0);
		return tlfg;
		/**= = = = = = = = = = = = = = = = = = = = = = = =**/
		/**-----------------------------------------------**/
	}

	/**Remove the less significant sub-scenes that have
	 * overlapping nodes with more significant sub-scenes**/
	private ThirdLevelFrameGroup [] removeOverlappingScenes(
			ThirdLevelFrameGroup [] frame_groups) {

		/**-------------------------------------------------------------------**/
		/**===================================================================**/
		/**Collect all the motion values for the extracted frame groups
		 * and calculate the standard deviation of these values**/
		double [] tlfg_mot_vals = new double[frame_groups.length];
		for(int i = 0; i < frame_groups.length; i++)
			tlfg_mot_vals[i] = frame_groups[i].getFrameGroupMotionValue();
		final double tlfg_mot_val_std = PCATools.getStdDev(tlfg_mot_vals);
		/**-------------------------------------------------------------------**/
		/**===================================================================**/
		/**===================================================================**/
		/**-------------------------------------------------------------------**/
		/**Sort the frame groups by their significance**/
		Arrays.sort(frame_groups, new Comparator<ThirdLevelFrameGroup>() {

			public int compare(ThirdLevelFrameGroup a, ThirdLevelFrameGroup b) {

				double val_a = a.getFrameGroupMotionValue();
				double length_a = a.getRightBoundOffset()+a.getLeftBoundOffset();

				double val_b = b.getFrameGroupMotionValue();
				double length_b = b.getRightBoundOffset()+a.getLeftBoundOffset();

				double comp_a = length_a +
						Math.pow((val_a/tlfg_mot_val_std),ControlVariables.third_mot_val_weight_var);
				double comp_b = length_b +
						Math.pow((val_b/tlfg_mot_val_std),ControlVariables.third_mot_val_weight_var);

				if(comp_a < comp_b) {
					return 1;
				} else if(comp_a > comp_b) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		/**-------------------------------------------------------------------**/
		/**===================================================================**/
		/**===================================================================**/
		/**-------------------------------------------------------------------**/
		/**Filter overlapping groups**/
		Vector<ThirdLevelFrameGroup> filtered_groups = new Vector<ThirdLevelFrameGroup>();
		for(int i = 0; i < frame_groups.length; i++) {

			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/**Get the next frame group**/
			int root_frame_i =			frame_groups[i].getFrameNumber();
			int group_right_offset_i = 	frame_groups[i].getRightBoundOffset();
			String root_name_i = 		frame_groups[i].getRootName();

			boolean overlaps = false;
			SkeletonNode root_node = skeletonWrappers[root_frame_i].getFromHash(root_name_i);

			/**Get the list of children names belonging under this frame group**/
			String [] segments_strs = null;
			if(root_node == null)
				overlaps = true;
			else {
				if(root_node.isFirstLevel())
					overlaps=true;
				else {
					SkeletonNode [] root_iter = root_node.getIterator();
					segments_strs = new String[root_iter.length];
					for(int k = 0; k < root_iter.length; k++)
						segments_strs[k] = root_iter[k].getName();
				}
			}
			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
			/**Using the frame  number and offset,  iterate through the
			 * skeleton wrappers and remove the segment nodes using the
			 * segment names from the hashes from the skeleton wrappers
			 * between those frames iterated.**/

			/**This way the nodes will not appear in the between the  same
			 * frames when iteratng through frame groups with lower motion
			 * significance.  This will result in  frame groups with lower
			 * significance to be filtered out that have overlapping
			 * segments with this frame group**/
			overlaps = overlaps || takeoutOverlaps(
							root_frame_i,
							group_right_offset_i,
							segments_strs);
			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
			if(!overlaps && (frame_groups[i].getRightBoundOffset() >= ControlVariables.min_slack_size))
				filtered_groups.add(frame_groups[i]);
			/** | | | | | | | | | | | | | | | | | | | | | | | | | | | | |-**/
			/**- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -**/
		}

		/**-------------------------------------------------------------------**/
		/**===================================================================**/
		/**===================================================================**/
		/**-------------------------------------------------------------------**/
		ThirdLevelFrameGroup []filtered_groups_arr =
				new ThirdLevelFrameGroup[filtered_groups.size()];
		filtered_groups.toArray(filtered_groups_arr);
		/**===================================================================**/
		/**-------------------------------------------------------------------**/

		return filtered_groups_arr;
	}

	/**Remove  segments  so that lower  significant  frame
	 * groups don't overlap and detect overlaping segments
	 * belonging to  higher  significant  frame  groups**/
	private boolean takeoutOverlaps(
			int root_frame_idx,
			int group_right_offset_idx,
			String [] segments_strs) {

		for(int i = root_frame_idx; i < (root_frame_idx+group_right_offset_idx); i++) {
			for(int j = 0; j < segments_strs.length; j ++) {
				String test_segment = segments_strs[j];
				SkeletonNode test_node = skeletonWrappers[i].getFromHash(test_segment);

				/**If node exist for this frame group within the skeleton
				 * wrappers, remove it so that it does not appear in this
				 * location for other frame groups with lower position in
				 * the sorted list of frame groups**/
				if(test_node != null) {
					skeletonWrappers[i].remopveFromHash(test_segment);
					if(test_node.isFirstLevel()) {
						return true;
					}
				}

				/**If this node doesn't exit  then  it must already
				 * belong  to some  other frame  group  with higher
				 * significance. So filter this frame group out.**/
				else {
					return true;
				}
			}
		}

		return false;
	}

	/**Get the angle of rotation the node went though, using it's parent and previous nodes**/
	private double getAngleDisplacement(SkeletonNode root, int f) {

		/**------------------------------------------------------**/
		double ang = 0;
		int ang_counter = 0;
		/**------------------------------------------------------**/
		/**||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**------------------------------------------------------**/
		SkeletonNode parent =	root.getParent();
		if(parent==null) return ang;
		String root_name = 		root.getName();
		/**------------------------------------------------------**/
		/**||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**------------------------------------------------------**/
		int group_size = ControlVariables.third_level_ang_filter_size;
		int group_size_half = (group_size/2)+(group_size % 2);
		int total_filter_size_i = (group_size_half*2) +
				(Math.min(f-group_size_half, 0)) +
				(Math.min(skeletonNodes.length - (f+group_size_half+1), 0));
		/**------------------------------------------------------**/
		/**||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**------------------------------------------------------**/
		for(int j = f-group_size_half; j < f; j++) {
			if(j < 0) continue;
			else {

				SkeletonNode root_j_1 = skeletonWrappers[j+1].getFromHash(root_name);
				SkeletonNode parent_j_1 = root_j_1.getParent();

				SkeletonNode root_j = skeletonWrappers[j].getFromHash(root_name);
				SkeletonNode parent_j = root_j.getParent();


				double [] r_j = root_j.getPoint();
				double [] p_j = parent_j.getPoint();

				double [] r_j_1 = root_j_1.getPoint();
				double [] p_j_1 = parent_j_1.getPoint();

				double [] par_diff = VectorTools.sub(p_j, p_j_1);
				r_j_1 = VectorTools.add(par_diff, r_j_1);

				ang += VectorTools.ang(r_j_1, p_j, r_j);
				ang_counter++;
			}
		}
		/**------------------------------------------------------**/
		/**||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**------------------------------------------------------**/
		for(int j = f; j < f+group_size_half; j++) {
			if((j+1) >= skeletonWrappers.length) continue;
			else {

				SkeletonNode root_j_1 = skeletonWrappers[j+1].getFromHash(root_name);
				SkeletonNode parent_j_1 = root_j_1.getParent();

				SkeletonNode root_j = skeletonWrappers[j].getFromHash(root_name);
				SkeletonNode parent_j = root_j.getParent();


				double [] r_j = root_j.getPoint();
				double [] p_j = parent_j.getPoint();

				double [] r_j_1 = root_j_1.getPoint();
				double [] p_j_1 = parent_j_1.getPoint();

				double [] par_diff = VectorTools.sub(p_j, p_j_1);
				r_j_1 = VectorTools.add(par_diff, r_j_1);

				ang += VectorTools.ang(r_j_1, p_j, r_j);
				ang_counter++;
			}
		}
		/**------------------------------------------------------**/
		/**||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**------------------------------------------------------**/
		if(total_filter_size_i != ang_counter) {
			System.out.println(total_filter_size_i);
			System.out.println(ang_counter);
			System.exit(1);
		}
		return((1.0)/(ang_counter+0.0))*ang;
		/**------------------------------------------------------**/
	}
}
