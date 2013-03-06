package CameraMotion.ThirdLevelMotion;

import java.util.LinkedList;
import java.util.Vector;

import main.ControlVariables;

import IO.ASFData.SkeletonNode;
import IO.ASFData.SkeletonWrapper;

import Tools.VectorTools;
import Tools.PCA.JacobiResult;
import Tools.PCA.PCATools;


public class ThirdLevelMotionScene {

	/**Pointer to the frame group**/
	private ThirdLevelFrameGroup tlfg = null;
	/**Pointer to the shared skeleton wrappers**/
	private SkeletonWrapper [] skeletonWrappers = null;
	/**Pointer to the skeletons**/
	private SkeletonNode [] skeletons = null;
	/**The original centroid of each skeleton**/
	private double[][] centroid_per_skeleton = null;
	/**The camera orientation for each skeleton**/
	private float[][] cam_orns = null;

	/**Points belonging under each sub-scene of this scene**/
	private double [][][] sequence_points = null;
	private double [][][] sequence_points2 = null;
	/**The centroid stored for each sub-scene**/
	private double [][] centroids = null;
	/**The region surrounding each set of
	 * points belonging to a sub-scene**/
	private SquareRegion [] square_regions = null;


	/**Indication of whether a skeleton has too much overlap**/
	private boolean [] too_much_overlap = null; /**TODO: this is currently not used**/
	public double overlap_fac = 0.0;

	/**Camera orientation per sub-scene**/
	private float [][] camera_centers = null;
	/**Camera zoom levels for each sub-scene**/
	private float [] camera_zoom_levels = null;
	/**The camera scene stored for each sub-scene**/
	private float [][] camera_orientations = null;
	/**The pca vectors for determining the camera orientations**/
	private float [][][] pca_vectors = null;


	/**Class constructor**/
	public ThirdLevelMotionScene(
			ThirdLevelFrameGroup tlfg,
			SkeletonWrapper[] skeletonWrappers,
			SkeletonNode [] skeletons,
			double[][] centroid_per_frame,
			float [][] cam_orns) {

		this.tlfg = tlfg;
		this.skeletonWrappers = skeletonWrappers;
		this.skeletons = skeletons;
		this.centroid_per_skeleton=centroid_per_frame;
		this.cam_orns = cam_orns;

		/**Collect the points for all the
		 * frames in this motion scene**/
		collectPoints();

		/**Transform the points when calculating the squares and centroids**/
		transformPoints();

		/**Calculate the centroid of each sub-scene**/
		calculateCentroids();

		/**Calculate the square region for each sub-scene**/
		calculateSquares();

		/**Take skeleton points, transform them, and see
		 * if  they  overlap the  squares  significantly
		 * throughout the frames in the sub-sequences**/
		checkSegmentOverlap();

		/**Compute the camera orientations and
		 * zoom levels for each  sub-scene **/
		computeCameraControls();
	}


	/**Collect the points for all the frames in this motion scene**/
	private void collectPoints() {

		/**--------------------------------------------------------------**/
		/**==============================================================**/
		/**Get the frame group information**/
		String root_name = tlfg.getRootName();

		int right_offset = tlfg.getRightBoundOffset();
		int left_offset = tlfg.getLeftBoundOffset();
		int frame_number = tlfg.getFrameNumber();
		int frame_length = (left_offset+right_offset);
		/**--------------------------------------------------------------**/
		/**==============================================================**/
		/**==============================================================**/
		/**--------------------------------------------------------------**/

		/**Get the points belonging to each frame**/
		sequence_points = new double[frame_length][][];
		sequence_points2 = new double[frame_length][][];
		int counter = 0;
		for(int i = 0; i < frame_length; i++) {

			SkeletonWrapper wrapper_i = skeletonWrappers[i+frame_number];
			SkeletonNode root_i = wrapper_i.getFromHash(root_name);

			counter++;
			/**Store the points existing under this
			 * root, excluding the root itself**/
			sequence_points[i] = getPointsForRoot(root_i, i+frame_number);
			sequence_points2[i] = getPointsForRoot(root_i, i+frame_number);
			if(sequence_points[i].length==0) {
				System.out.println("Mag must be one: "+root_i.getMag());
				System.exit(1);
			}
		}
		if(counter != frame_length || frame_length == 0) {
			System.out.println(counter +" vs "+ frame_length);
			System.exit(1);
		}
		/**==============================================================**/
		/**--------------------------------------------------------------**/
	}

	/**Get the points present under this root (excluding the root)**/
	private double [][] getPointsForRoot(SkeletonNode root_i, int f) {

		LinkedList<SkeletonNode> root_stack = new LinkedList<SkeletonNode>();
		SkeletonNode [] root_children = root_i.getChildren();
		for(int i = 0; i < root_children.length; i++) {
			root_stack.add(root_children[i]);
		}

		int loop_counter = 0;
		int total_size = 0;
		for(int r = 0; r < root_i.getMag()-1; r++) {
			loop_counter++;
			total_size += /**Math.max((3.0-loop_counter),1);**/Math.max((loop_counter-(root_i.getMag()-1)+3),1);
		}
		double [][] points  = new double[total_size][];
		
		
		int point_counter = 0;
		loop_counter = 0;
		while(root_stack.size() > 0) {
			SkeletonNode next_root = root_stack.pop();
			
			loop_counter++;
			double t_weight = Math.max((loop_counter-(root_i.getMag()-1)+3),1);
			double [] next_point = next_root.getPoint();
			for(int r = 0; r < t_weight; r++) {
				points[point_counter++] = VectorTools.add(next_point, centroid_per_skeleton[f]);
			}
			
			SkeletonNode [] children = next_root.getChildren();
			for(int i = 0; i < children.length; i++) {
				root_stack.add(children[i]);
			}
		}
		if(point_counter != points.length) {
			double [] a  = new double[0];
			a[0] = a[9];
		}
		return points;
	}

	/**Transform points based on the provided camera orientation**/
	private void transformPoints() {

		int offset = tlfg.getFrameNumber();

		for(int i = 0; i < sequence_points2.length; i++) {
			for(int j = 0; j < sequence_points2[i].length; j++) {
				
				double [] point_i_j = sequence_points2[i][j];
				int frame_number = offset+i;
				float [] point_i_j_t = 
						VectorTools.transformPoint(
								cam_orns[frame_number],
								new float[]{
										(float) centroid_per_skeleton[frame_number][0],
										(float) centroid_per_skeleton[frame_number][1],
										(float) centroid_per_skeleton[frame_number][2]},
										new float[]{
										(float) point_i_j[0],
										(float) point_i_j[1],
										(float) point_i_j[2]});
				sequence_points2[i][j] = new double[]{
						point_i_j_t[0],
						point_i_j_t[1],
						point_i_j_t[2]
				};
			}
		}
	}

	/**Calculate the centroid of each sub-scene**/
	private void calculateCentroids() {

		centroids = new double [sequence_points.length][];
		for(int i = 0; i < sequence_points.length; i++) {
			double [][] skeleton_i = sequence_points[i];

			double [] sum = new double[]{0.0,0.0,0.0};
			double counter = 0;

			for(int k = 0; k < skeleton_i.length; k++) {
				double [] node_i_k = skeleton_i[k];
				
				/**Put more weight on the root nodes**/
				sum = VectorTools.add(node_i_k, sum);
				counter++;
			}
			
			counter = Math.max(counter,1);
			centroids[i] = VectorTools.mult(1.0/counter, sum);
		}
	}

	/**Calculate the square region for each sub-scene**/
	private void calculateSquares() {
		
		square_regions = new SquareRegion[sequence_points2.length];
		for(int i = 0; i < sequence_points2.length; i++) {

			Vector<double[]> sub_scene_i_points = new Vector<double[]>();

			double [][] skeleton_i =  sequence_points2[i];
			for(int k = 0; k < skeleton_i.length; k++) {
				double [] node_i_k = skeleton_i[k];
				
				sub_scene_i_points.add(node_i_k);
			}

			/**---------------------------------------------------------**/
			/**---------------------------------------------------------**/
			double [][] sub_scene_i_points_arr =
					new double[sub_scene_i_points.size()][3];
			sub_scene_i_points.toArray(sub_scene_i_points_arr);
			/**---------------------------------------------------------**/
			/**---------------------------------------------------------**/
			/**Store  the minimum  x, y and maximum  x, y
			 * coordinates of the points in this frame**/
			double min_x = 10.0e99;
			double min_y = 10.0e99;

			double max_x = -10.0e99;
			double max_y = -10.0e99;

			for(int j = 0; j < sub_scene_i_points_arr.length; j++) {
				double [] next_point = sub_scene_i_points_arr[j];
				min_x = Math.min(next_point[0], min_x);
				min_y = Math.min(next_point[1], min_y);

				max_x = Math.max(next_point[0], max_x);
				max_y = Math.max(next_point[1], max_y);
			}

			double[]s1 = new double[]{min_x, max_y};
			double[]s2 = new double[]{max_x, max_y};
			double[]s3 = new double[]{max_x, min_y};
			double[]s4 = new double[]{min_x, min_y};
			square_regions[i] = new SquareRegion(s1,s2,s3,s4, Math.abs(min_y-max_y));
			/**---------------------------------------------------------**/
		}
	}

	/**Take skeleton points, transform them, and see
	 * if  they  overlap the  squares  significantly
	 * throughout the frames in the sub-sequences**/
	public void checkSegmentOverlap() {
		int frame_number = tlfg.getFrameNumber();
		int right_offset = tlfg.getRightBoundOffset();

		/**--------------------------------------------------------------------------**/
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		double [] area_factors = new double[square_regions.length];
		for(int i = 0; i < area_factors.length; i++) {
			area_factors[i] = 0;
		}
		/**--------------------------------------------------------------------------**/
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		/**--------------------------------------------------------------------------**/
		for(int i = frame_number ; i < frame_number +right_offset; i++) {

			/**------------------------------------------------------------------**/
			/**||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
			SkeletonNode [] root_iter = skeletons[i].getIterator();
			for(int k = 0; k < root_iter.length; k++) {
				
				SkeletonNode next_node = root_iter[k];
				SkeletonNode [] children = next_node.getChildren();

				/**----------------------------------------------------------**/
				/**TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT**/
				/**A node making up  a segment must have at least one child**/
				if(children.length > 0) {

					/**--------------------------------------------------**/
					/**==================================================**/
					/**Create  the  segment  from  each
					 * child and test it for overlap**/
					for(int j = 0; j < children.length; j++) {

						/**------------------------------------------**/
						/**| | | | | | | | | | | | | | | | | | | | | **/
						double [] root_point = next_node.getPoint();
						double [] leaf_point = children[j].getPoint();

						SquareRegion sr = square_regions[i-frame_number];
						/**Points should be transformed according to the orientation for that segment.
						 * Points should include both the segment being checked for overlap
						 * and the points making up the circle**/
						area_factors[i-frame_number] = sr.getOverlapArea(root_point, leaf_point);
						/**| | | | | | | | | | | | | | | | | | | | | **/
						/**------------------------------------------**/
					}
					/**==================================================**/
					/**--------------------------------------------------**/
				}
				/**TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT**/
				/**----------------------------------------------------------**/
			}
			/**||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
			/**------------------------------------------------------------------**/
		}
		/**--------------------------------------------------------------------------**/
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		/**--------------------------------------------------------------------------**/
		too_much_overlap = new boolean[square_regions.length];
		for(int i = 0; i < area_factors.length; i++) {
			overlap_fac += area_factors[i];
			too_much_overlap[i] = (area_factors[i] > ControlVariables.overlap_thres);
		}
		overlap_fac = overlap_fac/(area_factors.length+0.0);
		/**MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM**/
		/**--------------------------------------------------------------------------**/
	}

	/**Compute the camera orientations and
	 * zoom levels for each  sub-scene **/
	public void computeCameraControls() {

		/**--------------------------------------------------------------**/
		camera_centers = new float[sequence_points.length][];
		camera_zoom_levels = new float[sequence_points.length];
		camera_orientations = new float[sequence_points.length][];
		pca_vectors = new float[sequence_points.length][][];
		/**--------------------------------------------------------------**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**--------------------------------------------------------------**/
		/**Compute the camera centers**/
		float [][] temp_centers = new float[sequence_points.length][];
		for(int i = 0; i < camera_centers.length; i++) {
			temp_centers[i] =
					new float[]{
					-(float) centroids[i][0],
					-(float) centroids[i][1],
					-(float) centroids[i][2]/**TODO: change**/
			};
		}
		for(int i = 0; i < camera_centers.length; i++) {
			float [] temp_sum = new float []{0,0,0};
			int temp_sum_counter = 0;

			for(int j = 0; j < ControlVariables.centSpreadTLMS; j++) {
				if((i-j) >= 0) {
					temp_sum = VectorTools.add(temp_centers[i-j],temp_sum);
					temp_sum_counter++;
				}
			}
			for(int j = 1; j < ControlVariables.centSpreadTLMS+1; j++) {
				if((i+j) < camera_zoom_levels.length) {
					temp_sum = VectorTools.add(temp_centers[i+j],temp_sum);
					temp_sum_counter++;
				}
			}
			camera_centers[i] = VectorTools.mult((1.0f)/(temp_sum_counter+0.0f), temp_sum);
		}
		/**--------------------------------------------------------------**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**--------------------------------------------------------------**/
		/**Compute the zoom levels with smoothing applied**/
		float [] camera_zoom_levels_temp = new float[camera_zoom_levels.length];
		for(int i = 0; i < camera_zoom_levels_temp.length; i++) {
			camera_zoom_levels_temp[i] =
					(float) (square_regions[i].getLongestWdith()*ControlVariables.tl_zoom_factor );
		}
		for(int i = 0; i < camera_zoom_levels.length; i++) {
			float temp_sum = 0;
			int temp_sum_counter = 0;

			for(int j = 0; j < ControlVariables.centSpreadTLMS; j++) {
				if((i-j) >= 0) {
					temp_sum += camera_zoom_levels_temp[i-j];
					temp_sum_counter++;
				}
			}
			for(int j = 1; j < ControlVariables.centSpreadTLMS+1; j++) {
				if((i+j) < camera_zoom_levels.length) {
					temp_sum += camera_zoom_levels_temp[i+j];
					temp_sum_counter++;
				}
			}
			camera_zoom_levels[i] = temp_sum/(temp_sum_counter+0.0f);
		}
		/**--------------------------------------------------------------**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**--------------------------------------------------------------**/
		/**Compute the camera orientations**/
		for(int i = 0; i < sequence_points.length; i++) {

			/**-------------------------------------------------**/
			Vector<float[]>  skeleton_i_points = new Vector<float[]>();
			double [][] skeleton_i = sequence_points[i];

			for(int k = 0; k < skeleton_i.length; k++) {

				double [] node_i_k = skeleton_i[k];
				skeleton_i_points.add(new float[]{
						(float) node_i_k[0],
						(float) node_i_k[1],
						(float) node_i_k[2]});
			}
			/**=================================================**/
			/**=================================================**/
			float [][] skeleton_i_j_points_arr =
					new float[skeleton_i_points.size()][3];
			skeleton_i_points.toArray(skeleton_i_j_points_arr);
			/**=================================================**/
			/**=================================================**/
			/**Convert the points to a m by n matrix**/
			float [][] skeleton_i_j_points_T =
					VectorTools.getTranspose(skeleton_i_j_points_arr);


			/**Get the average point for the skeleton points**/
			float [] skeleton_i_j_points_x = skeleton_i_j_points_T[0];
			float [] skeleton_i_j_points_y = skeleton_i_j_points_T[1];
			float [] skeleton_i_j_points_z = skeleton_i_j_points_T[2];
			float [] skeleton_i_j_avg_point = new float[]{
					PCATools.getMean(skeleton_i_j_points_x),
					PCATools.getMean(skeleton_i_j_points_y),
					PCATools.getMean(skeleton_i_j_points_z)
			};

			/**Get the PCA vector for the set of skeleton points**/
			JacobiResult jr = PCATools.getPCAUsingJacobi(skeleton_i_j_points_T);
			float [] skeleton_i_j_pca_vector1 = jr.getVector_f(2);
			float [] skeleton_i_j_pca_vector2 = jr.getVector_f(1);
			float [] skeleton_i_j_pca_vector3 = jr.getVector_f(0);

			/**Get the PCA vector to draw a line**/
			float [] skeleton_i_j_p1 = VectorTools.add(skeleton_i_j_avg_point, skeleton_i_j_pca_vector1);
			float [] skeleton_i_j_p2 = VectorTools.add(skeleton_i_j_avg_point, skeleton_i_j_pca_vector2);
			float [] skeleton_i_j_p3 = VectorTools.add(skeleton_i_j_avg_point, skeleton_i_j_pca_vector3);

			pca_vectors[i] = new float[][]{
					skeleton_i_j_p1,
					skeleton_i_j_avg_point,
					skeleton_i_j_p2,
					skeleton_i_j_p3
			};

			/**---------------------------------------------------------**/
		}
		/**--------------------------------------------------------------**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()**/
		/**--------------------------------------------------------------**/
		camera_orientations = getCameraOrnPerFrame(pca_vectors);
		/**--------------------------------------------------------------**/
	}

	private float [][] getCameraOrnPerFrame(float [][][] pcaPerFrame) {

		float [][] camera_orn_per_frame_temp = new float[pcaPerFrame.length][3];
		float [][] camera_orn_per_frame = new float[pcaPerFrame.length][3];

		for(int i = 0; i < pcaPerFrame.length; i++) {

			float [][] pca_s = pcaPerFrame[i];


			/**Retrieve the required PCAs for the object number**/
			float [] p1 =         pca_s[0].clone();
			float [] p2 =         pca_s[2].clone();
			float [] avg =         pca_s[1].clone();

			float [] p1_avg = VectorTools.sub(p1,avg);
			float [] p2_avg = VectorTools.sub(p2,avg);

			/**Get the normal to the two PCA components**/
			float [] pca_normal =  VectorTools.norm(VectorTools.cross(p1_avg, p2_avg));

			float [] pca_normal2 = new float[]{
					-ControlVariables.pcaNormalsLength*Math.abs(pca_normal[0]),
					-ControlVariables.pcaNormalsLength*Math.abs(pca_normal[1]),
					ControlVariables.pcaNormalsLength*Math.abs(pca_normal[2])
			};

			camera_orn_per_frame_temp[i] = pca_normal2;
		}
		for(int i = 0; i < camera_orn_per_frame_temp.length; i++) {
			float [] temp_sum = new float[]{0,0,0};
			int temp_sum_counter = 0;

			for(int j = 0; j < ControlVariables.ornSpreadTLMS; j++) {
				if((i-j) >= 0) {
					temp_sum_counter++;
					temp_sum = VectorTools.add(temp_sum, camera_orn_per_frame_temp[i-j]);
				}
			}
			for(int j = 1; j < ControlVariables.ornSpreadTLMS+1; j++) {
				if((i+j) < camera_orn_per_frame_temp.length) {
					temp_sum_counter++;
					temp_sum = VectorTools.add(temp_sum, camera_orn_per_frame_temp[i+j]);
				}
			}

			camera_orn_per_frame[i] = VectorTools.mult((1.0f/(temp_sum_counter+0.0f)), temp_sum);
		}
		return camera_orn_per_frame;
	}

	/**Get the third level frame group**/
	public ThirdLevelFrameGroup getTLFG() {
		return this.tlfg;
	}

	/**Get the computed camera centers for this third level scene**/
	public float [][] getCenters() {
		return camera_centers;
	}


	/**Get the computed camera zoom levels for this third level scene**/
	public float [] getZoomLevels() {
		return camera_zoom_levels;
	}

	/**Get the computed camera orientations for this third level scene**/
	public float [][] getOrientations() {
		return camera_orientations;
	}
}
