package CameraMotion;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import Tools.VectorTools;
import Tools.PCA.PCATools;

import main.ControlVariables;

import CameraMotion.RotationControl.CameraViewPointControl;
import CameraMotion.ThirdLevelMotion.ThirdLevelMotionControl;
import CameraMotion.ThirdLevelMotion.ThirdLevelMotionScene;
import CameraMotion.ZoomControl.CameraZoomControl;
import CameraMotion.ZoomControl.FirstLevelMotionScene;

import IO.ASFData.SkeletonNode;
import IO.Joints.JointStick;
import IO.Joints.JointSticks;


public class CameraScenes {

	/**--------------------------------------------------------------------**/
	/**                             GLOBAL VARIABLES                       **/
	/**--------------------------------------------------------------------**/

	/**=================================================**/
	/**-------------------------------------------------**/
	/**=================================================**/
	/**List of camera orientations for each scene**/
	private Vector<CameraScene> cameraScenes = null;

	/**Number of the current camera scene being read**/
	private int current_scene_number = 0;

	/**The PCA vector per frame**/
	private float [][][] pcaPerFrame = null;

	/**Camera center of view for each frame**/
	private float [][][][] data_per_frame = null;

	/**The camera orientations per frame calculated from the PCA vectors**/
	private float [][] camera_orn_per_frame = null;

	/**The joint sticks belonging to each frame**/
	private JointSticks [] joint_sticks_per_frame = null;

	/**The skeleton nodes stored per frame**/
	SkeletonNode [] node_per_frame = null;
	/**=================================================**/
	/**-------------------------------------------------**/
	/**=================================================**/


	/**--------------------------------------------------------------------**/
	/**                             MAIN FUNCTIONS                         **/
	/**--------------------------------------------------------------------**/
	/**Class constructor**/
	public CameraScenes (
			float [][][] pcaPerFrame,
			float [][][][] data_per_frame,
			JointSticks [] joint_sticks_per_frame,
			SkeletonNode [] node_per_frame) {

		this.joint_sticks_per_frame=joint_sticks_per_frame;
		this.pcaPerFrame = pcaPerFrame;
		this.data_per_frame = data_per_frame;
		this.node_per_frame = node_per_frame;
		init();
	}
	/**Initialize the data structure needed to compute camera control**/
	public void init() {

		/**Get the camera orientation per frame given the PCA vectors**/
		camera_orn_per_frame = getCameraOrnPerFrame(pcaPerFrame);

		/**Get the camera centers per frame given the PCA vectors**/
		float [][] camera_center_per_frame = getCameraCenterPerFrame(pcaPerFrame);

		/**Get the area covered by the points projected on the
		 * plane spanned by the PCA vectors for each frame**/
		float [][] pca_area_per_frame = getAreaPerFrame(pcaPerFrame);

		/**Calculate the displacement per frame**/
		float [][] dis_per_frame = getDisPerFrame(camera_center_per_frame);

		/**Calculate the acceleration value for each frame**/
		float [] acc_per_frame = getAccPerFrame(dis_per_frame);

		/**Calculate  the default  zoom  value  for  each
		 * frame given the objects area for each frame**/
		float [] zoom_val_per_frame = getDefaultZoomValPerFrame(pca_area_per_frame);


		/**Initialize the vector for storing scenes,
		 * and create and add the first scene**/
		cameraScenes = new Vector<CameraScene>();
		CameraScene cs = new CameraScene();
		cameraScenes.add(cs);



		/**Add the created set of orientations to the first scene**/
		cs.addOrientations(camera_orn_per_frame);

		/**Add the acceleration and displacement values**/
		cs.addAccelerations(acc_per_frame);
		cs.addDisplacements(dis_per_frame);


		/**Add the frame numbers for the entire data set to the first scene**/
		int [] frame_numbers = new int [data_per_frame.length];
		for(int i = 0; i < data_per_frame.length; i++) {
			frame_numbers[i] = i;
		}
		cs.addFrameNumbers(frame_numbers);

		/**Add the camera center to the first scene**/
		cs.addCameraCenters(camera_center_per_frame);

		/**Add the PCA areas to the first scene**/
		cs.addPCAAreas(pca_area_per_frame);

		/**Add the default zoom value for each frame**/
		cs.addZoomVals(zoom_val_per_frame);
	}


	/**--------------------------------------------------------------------**/
	/**           MAIN ALGORITHMS FOR CAMERA CONTROL: ROTATION             **/
	/**--------------------------------------------------------------------**/
	/**Smoothen the camera motion, given the camera
	 * orientation  and  area  for  each  frame **/
	public void smoothenOrientationsPerScene() {

		CameraViewPointControl cvc = new CameraViewPointControl();
		if(ControlVariables.filter_techniques_rot[ControlVariables.filtering_technique_rot].equals("PCA_weighted")) {
			System.out.println("Using PCA weighted filtering");

			Enumeration<CameraScene> cs_enum = cameraScenes.elements();
			while(cs_enum.hasMoreElements()) {
				CameraScene cs = cs_enum.nextElement();

				float [][] cs_orientations = cs.getOrientations();
				float [][] cs_areas_inf = cs.getPCAAreas();
				float [] cs_areas = new float[cs_areas_inf.length];
				for(int z = 0; z < cs_areas_inf.length; z++) {
					cs_areas[z] = cs_areas_inf[z][0];
				}
				
				cs_orientations = cvc.smoothenCameraMotion(
						cs_orientations, cs_areas);
				cs.addOrientations(cs_orientations);
			}
		}
		if(ControlVariables.filter_techniques_rot[ControlVariables.filtering_technique_rot].equals("area_weighted")) {
			System.out.println("Using area weighted filtering");

			Enumeration<CameraScene> cs_enum = cameraScenes.elements();
			while( cs_enum.hasMoreElements()) {
				CameraScene cs = cs_enum.nextElement();

				float [][] cs_orientations = cs.getOrientations();
				float [][] cs_areas_inf = cs.getPCAAreas();
				float [] cs_areas = new float[cs_areas_inf.length];
				for(int z = 0; z < cs_areas_inf.length; z++) {
					cs_areas[z] = cs_areas_inf[z][0];
				}

				cs_orientations = cvc.smoothenCameraMotion_UsingArea(
						cs_orientations, cs_areas);
				cs.addOrientations(cs_orientations);
			}
		}
		if(ControlVariables.filter_techniques_rot[ControlVariables.filtering_technique_rot].equals("adjusted_area_weighted")) {
			System.out.println("Using adjusting area weighted filtering");

			Enumeration<CameraScene> cs_enum = cameraScenes.elements();
			while( cs_enum.hasMoreElements()) {
				CameraScene cs = cs_enum.nextElement();

				float [][] cs_orientations = cs.getOrientations();
				float [][] cs_areas_inf = cs.getPCAAreas();
				float [] cs_areas = new float[cs_areas_inf.length];
				for(int z = 0; z < cs_areas_inf.length; z++) {
					cs_areas[z] = cs_areas_inf[z][0];
				}
				
				cs_orientations = cvc.smoothenCameraMotion_UsingAdjustingArea(
						cs_orientations, cs_areas);
				cs.addOrientations(cs_orientations);
			}
		}
	}
	/**Divide the scene into sub scenes where the rotation changes greatly**/
	public void divideToSubFramesForRotation() {

		/**======================================================================**/
		/**======================================================================**/
		/**The storage for the new scenes created**/
		Vector<CameraScene> new_camera_sub_scenes = new Vector<CameraScene>();

		/**Iterate through the current scenes
		 * and subdivide them if needed**/
		Enumeration<CameraScene> current_sub_scenes_enum = cameraScenes.elements();
		/**======================================================================**/
		/**======================================================================**/
		while(current_sub_scenes_enum.hasMoreElements()) {


			/**-------------------------------------------------------------**/
			CameraScene current_sub_scene = current_sub_scenes_enum.nextElement();

			float [][] current_sub_scene_orientations = current_sub_scene.getOrientations();
			float [][] current_sub_scene_centers = current_sub_scene.getCameraCenters();
			int [] current_sub_scene_frame_numbers = current_sub_scene.getFrameNumbers();
			float [][] current_sub_scene_pca_areas = current_sub_scene.getPCAAreas();
			float [] current_sub_scene_acc_vals = current_sub_scene.getAccValues();
			float [][] current_sub_scene_dis_vals = current_sub_scene.getDisVectors();
			float [] current_sub_scene_zoom_vals = current_sub_scene.getZoomValues();


			/**-------------------------------------------------------------**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**-------------------------------------------------------------**/
			/**Find the locations where the angles change abruptly**/
			Vector<Integer> abrupt_locations = new Vector<Integer>();

			float [] last_orn = current_sub_scene_orientations[0];

			int sub_scene_length = 0;
			for(int i = 1; i < current_sub_scene_orientations.length; i++) {
				float [] cur_orn = current_sub_scene_orientations[i];
				float angle = VectorTools.ang(last_orn, new float []{0,0,0}, cur_orn);

				/**An abrupt locations must have a specific
				 * change in angle, and be in a position
				 * within the scene where the resulting
				 * sub-scene will not be short**/
				if(
						angle > ControlVariables.abrupt_angle_size_rot &&
						sub_scene_length > ControlVariables.min_scene_length_rot &&
						(current_sub_scene_orientations.length-i) > ControlVariables.min_scene_length_rot) {

					abrupt_locations.add(i);
					sub_scene_length = 0;
				}
				last_orn = cur_orn.clone();
				sub_scene_length++;
			}
			abrupt_locations.add(current_sub_scene_orientations.length);
			/**-------------------------------------------------------------**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**-------------------------------------------------------------**/


			/**Create the camera scenes at the abrupt locations**/
			Integer [] abrupt_locations_arr = new Integer[abrupt_locations.size()];
			abrupt_locations.toArray(abrupt_locations_arr);
			CameraScene [] new_camera_sub_scenes_array =
					new CameraScene [abrupt_locations_arr.length];


			for(int i = 0; i < abrupt_locations_arr.length; i++) {

				/**Get the starting and ending indexes of the current subscene**/
				int start_arr_indx = 0;
				int end_arr_indx = 0;
				if(i == 0) {
					start_arr_indx = 0;
					end_arr_indx = abrupt_locations_arr[i];
				} else {
					start_arr_indx = abrupt_locations_arr[i-1];
					end_arr_indx = abrupt_locations_arr[i];
				}
				if(start_arr_indx-ControlVariables.frame_overlap_rot >= 0) {
					start_arr_indx-=ControlVariables.frame_overlap_rot;
				}
				if(end_arr_indx+ControlVariables.frame_overlap_rot < abrupt_locations_arr.length) {
					end_arr_indx+=ControlVariables.frame_overlap_rot;
				}

				CameraScene new_sub_scene = new CameraScene();
				new_camera_sub_scenes_array[i] = new_sub_scene;


				float [][] new_orn_arr = new float[end_arr_indx-start_arr_indx][3];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_orn_arr[j-start_arr_indx] = current_sub_scene_orientations[j].clone();
				}
				new_sub_scene.addOrientations(new_orn_arr);


				float [][] new_centers_arr = new float[end_arr_indx-start_arr_indx][3];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_centers_arr[j-start_arr_indx] = current_sub_scene_centers[j].clone();
				}
				new_sub_scene.addCameraCenters(new_centers_arr);


				int [] new_frame_numbers_arr = new int [end_arr_indx-start_arr_indx];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_frame_numbers_arr[j-start_arr_indx] = current_sub_scene_frame_numbers[j];
				}
				new_sub_scene.addFrameNumbers(new_frame_numbers_arr);


				float [][] new_areas_arr = new float[end_arr_indx-start_arr_indx][3];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_areas_arr[j-start_arr_indx] = current_sub_scene_pca_areas[j];
				}
				new_sub_scene.addPCAAreas(new_areas_arr);


				float [] new_acc_vals_arr = new float[end_arr_indx-start_arr_indx];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_acc_vals_arr[j-start_arr_indx] = current_sub_scene_acc_vals[j];
				}
				new_sub_scene.addAccelerations(new_acc_vals_arr);


				float [][] new_dis_vals_arr = new float[end_arr_indx-start_arr_indx][3];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_dis_vals_arr[j-start_arr_indx] = current_sub_scene_dis_vals[j];
				}
				new_sub_scene.addDisplacements(new_dis_vals_arr);


				float [] new_zoom_vals_arr = new float[end_arr_indx-start_arr_indx];
				for(int j = start_arr_indx; j < end_arr_indx; j++) {
					new_zoom_vals_arr[j-start_arr_indx] = current_sub_scene_zoom_vals[j];
				}
				new_sub_scene.addZoomVals(new_zoom_vals_arr);
			}


			/**-------------------------------------------------------------**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**+  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +  +**/
			/**-------------------------------------------------------------**/
			for(int i = 0; i < new_camera_sub_scenes_array.length; i++) {
				new_camera_sub_scenes.add(new_camera_sub_scenes_array[i]);
			}
			/**-------------------------------------------------------------**/
		}
		/**======================================================================**/
		/**======================================================================**/
		this.cameraScenes = new_camera_sub_scenes;
		/**======================================================================**/
		/**======================================================================**/
	}
	/**Divide the scene into sub scenes where the zooming level changes greatly**/
	public void divideToSubFramesForZoom() {
		//collectOrientations();
	}


	/**--------------------------------------------------------------------**/
	/**       MAIN ALGORITHMS FOR CAMERA CONTROL: Third Level Motion       **/
	/**--------------------------------------------------------------------**/
	public void findThirdLevelMotion() {
		ThirdLevelMotionControl tlmc =
				new ThirdLevelMotionControl(
						node_per_frame,
						camera_orn_per_frame);
		ThirdLevelMotionScene[] thirdLevelMotionScenes = tlmc.getThirdLevelScenes();

		/**Iterate through the current scenes**/
		Enumeration<CameraScene> current_sub_scenes_enum = cameraScenes.elements();
		while (current_sub_scenes_enum.hasMoreElements()) {

			CameraScene cs = current_sub_scenes_enum.nextElement();
			cs.addThirdLevelMotionScenes(thirdLevelMotionScenes);
		}
	}
	/**Apply  third  level motion effect  for each  scene, given
	 * the third level motion objects stored per camera scene**/
	public void applyThirdLevelMotionScenes() {
		/**Iterate through the current scenes**/
		Enumeration<CameraScene> current_sub_scenes_enum = cameraScenes.elements();
		while (current_sub_scenes_enum.hasMoreElements()) {

			CameraScene cs = current_sub_scenes_enum.nextElement();
			cs.addThirdLevelMotionSubScenes();
		}
	}

	/**--------------------------------------------------------------------**/
	/**             MAIN ALGORITHMS FOR CAMERA CONTROL: ZOOM               **/
	/**--------------------------------------------------------------------**/
	/**Iterate through the frames, through each  scene,
	 * and find the occurrence of first level motion**/
	public void findFirstLevelMotion() {

		/**Iterate through the current scenes**/
		Enumeration<CameraScene> current_sub_scenes_enum = cameraScenes.elements();
		while (current_sub_scenes_enum.hasMoreElements()) {

			/**Get the stored vectors and acceleration values**/
			CameraScene cs = current_sub_scenes_enum.nextElement();
			float [] acc_vals = cs.getAccValues();
			float [][] dis_vectors = cs.getDisVectors();
			int [] frame_numbers = cs.getFrameNumbers();
			float [] def_zoom_vals = cs.getZoomValues();
			float [][] pca_areas = cs.getPCAAreas();
			float [][] cam_centers = cs.getCameraCenters();
			float [][] cam_orns = cs.getOrientations();

			/**Smooth the acceleration values**/
			CameraZoomControl czc = new CameraZoomControl();
			acc_vals = czc.smoothAccVals(acc_vals.clone());

			/**Find the frames where the acceleration value is significant:
			 * returned array of zeros and +/-ones. =/- Ones marking the frames
			 * with significant positive or negative acceleration, zero values
			 * set otherwise **/
			float [] sig_acc = czc.findSigAccVals(acc_vals);

			/**Create the first motion objects, marking the frame numbers where
			 * acceleration starts (fi), where its zero, and where it is negative (fi+k)**/
			Vector<FirstLevelMotionScene> firstMotionScenes =
					findFirstLevelMotionHelper(
							sig_acc,
							frame_numbers);

			/**Set the zoom levels for each motion object**/
			computeFirstLevelMotionScenes(
					firstMotionScenes,
					def_zoom_vals,
					dis_vectors,
					pca_areas,
					cam_centers,
					cam_orns);


			FirstLevelMotionScene [] fms_arr =
					new FirstLevelMotionScene[firstMotionScenes.size()];
			firstMotionScenes.toArray(fms_arr);
			cs.addFirstMotionScenes(fms_arr);

			for(int  j = 0; j < fms_arr.length; j++) {
				FirstLevelMotionScene flms_j = fms_arr[j];
				int fi = flms_j.getFi();
				int f2L = flms_j.getF2L();

				for(int k = fi-80; k < f2L+100/**TODO**/; k++) {
					if(k < node_per_frame.length && k >= 0) {
						SkeletonNode node = node_per_frame[k];
						node.setFirstLevelIndicator();
					}
				}
			}
		}
	}
	/**Helper function to the method above**/
	private Vector<FirstLevelMotionScene> findFirstLevelMotionHelper(
			float [] sig_acc,
			int [] frame_numbers) {

		Vector<FirstLevelMotionScene> firstMotionScenes =
				new Vector<FirstLevelMotionScene>();
		/**Find all the scenes containing first level motion**/
		for(int i = 0; i < sig_acc.length; i++) {

			if(sig_acc[i] == 1) {

				/**Create the first motion scene object**/
				FirstLevelMotionScene fms_i = new FirstLevelMotionScene();
				firstMotionScenes.add(fms_i);

				int fi = frame_numbers[i];
				fms_i.setFi(fi);

				int fn_offset = frame_numbers[0];
				fms_i.setOffSet(fn_offset);

				/**Find the next frame where the acceleration stops**/
				while(sig_acc[i] != 0) {
					if(i == sig_acc.length-1){
						break;
					}
					i++;
				}
				int fL = frame_numbers[i];
				fms_i.setFL(fL);

				int f2L = (int)(fL+(ControlVariables.fLf2L_fac*(fL-fi)));
				if(f2L > sig_acc.length-1){
					f2L = sig_acc.length-1;
				}
				fms_i.setF2L(f2L);

				int fk = (int)(f2L+(ControlVariables.f2Lfk_fac*(fL-fi)));
				if(fk > sig_acc.length-1){
					fk = sig_acc.length-1;
				}
				fms_i.setFk(fk);
			}
		}

		/**Sort the scenes by size**/
		FirstLevelMotionScene [] fms_size_arr =
				new FirstLevelMotionScene[firstMotionScenes.size()];
		firstMotionScenes.toArray(fms_size_arr);
		Arrays.sort(fms_size_arr, new Comparator<FirstLevelMotionScene>(){
			public int compare(
					FirstLevelMotionScene a,
					FirstLevelMotionScene b) {

				int fi = a.getFi();
				int fi_2 = b.getFi();
				int fL = a.getFL();
				int fL_2 = b.getFL();

				if((fL-fi) > (fL_2-fi_2)) {
					return -1;
				}
				else if((fL-fi) < (fL_2-fi_2)) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		/**Detect the  scenes that overlap,  and either fix
		 * them or mark them with a zero and remove them**/
		 for(int i = 0; i < fms_size_arr.length; i++) {

			 int fi = fms_size_arr[i].getFi();
			 int fk = fms_size_arr[i].getFk();
			 for(int j  = (i+1); j < fms_size_arr.length; j++) {

				 FirstLevelMotionScene fms_j = fms_size_arr[j];
				 int fi2 = fms_size_arr[j].getFi();
				 int fk2 = fms_size_arr[j].getFk();

				 /**Case:  fi2    fi     fk2     fk**/
				 if((fk2 <= fk) && (fk2 > fi) && (fi2 < fi)) {
					 fms_j.setFk(fi);

					 int old_fi = fms_j.getFi();
					 int new_fk = fms_j.getFk();
					 int f_diff = new_fk-old_fi;

					 float total_fac = 1.0f + ControlVariables.fLf2L_fac + ControlVariables.f2Lfk_fac;
					 float fifL_diff = (1.0f/total_fac)*f_diff;
					 float fLf2L_diff = (ControlVariables.fLf2L_fac/total_fac)*f_diff;

					 int new_fL = old_fi+(int)(fifL_diff);
					 int new_f2L = new_fL+(int)(fLf2L_diff);

					 fms_j.setFL(new_fL);
					 fms_j.setF2L(new_f2L);
				 }
				 /**Case:   fi    fi2    fk    fk2**/
				 else if((fk2 > fk) && (fi2 >= fi) && (fi2 < fk)) {
					 fms_j.setFi(fk);
				 } else {
					 fms_j.setFi(0);
					 fms_j.setFk(0);
				 }
			 }
		 }
		 Vector<FirstLevelMotionScene> firstMotionScenes_ret =
				 new Vector<FirstLevelMotionScene>();
		 for(int i = 0; i < fms_size_arr.length; i++) {
			 FirstLevelMotionScene fms_i = fms_size_arr[i];
			 int fi = fms_i.getFi();
			 int fk = fms_i.getFk();

			 if((fk-fi) > 0) {
				 firstMotionScenes_ret.add(fms_i);
				 //System.out.println(fi+" "+fms_i.getFL()+" "+fms_i.getF2L()+" "+fk);
			 }

		 }
		 return firstMotionScenes_ret;
	}

	/**Compute  the zoom  levels for  the  first  motion
	 * scenes, given the starting default zoom values**/
	private void computeFirstLevelMotionScenes(
			Vector<FirstLevelMotionScene> firstMotionScenes,
			float [] def_zoom_vals,
			float [][] dis_vectors,
			float [][] pca_areas,
			float [][] cam_centers,
			float [][] cam_orns) {

		/**-------------------------------------------------------------------------**/
		/**|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**-------------------------------------------------------------------------**/
		Enumeration<FirstLevelMotionScene> fms_enum = firstMotionScenes.elements();
		while(fms_enum.hasMoreElements()) {
			FirstLevelMotionScene fms_next = fms_enum.nextElement();

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/
			/**Get the required frame numbers**/
			int frame_offset = fms_next.getOffSet();
			int frame_fi = fms_next.getFi();
			int frame_fL = fms_next.getFL();
			int frame_f2L = fms_next.getF2L();

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/
			/**Set the starting zoom value and camera center for frame fi**/
			float fi_zoom = def_zoom_vals[frame_fi-frame_offset];
			float [] fi_center = cam_centers[frame_fi-frame_offset];
			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/

			/**Set the required variables for frame fi+L**/
			float [] fL_area = pca_areas[frame_fL-frame_offset];
			float fL_w1 = fL_area[1];float fL_w2 = fL_area[2];
			if(fL_w1 < fL_w2) {
				fL_w1 = fL_w2;
			}
			//System.out.println("fL_w1: "+fL_w1);
			float [] fL_pos = cam_centers[frame_fL-frame_offset];
			float [] f2L_pos = cam_centers[frame_f2L-frame_offset];

			//System.out.println("P1: "+fL_pos[0]+" "+fL_pos[1]+" "+fL_pos[2]);
			//System.out.println("P2: "+f2L_pos[0]+" "+f2L_pos[1]+" "+f2L_pos[2]);

			float [] f2L_fL_displacement_world = VectorTools.sub(f2L_pos, fL_pos);
			float [] f2L_fL_displacement_cam = VectorTools.transformPoint(
					cam_orns[frame_fL-frame_offset],
					fL_pos,
					f2L_fL_displacement_world);
			
			float f2L_fL_displacement_cam_mag = VectorTools.mag(new float[]{
					f2L_fL_displacement_cam[0], f2L_fL_displacement_cam[1], 0
			});
			f2L_fL_displacement_cam_mag = Math.max( /**TODO: check**/
					f2L_fL_displacement_cam[0],
					f2L_fL_displacement_cam[1]);

			/**Test by storing points of a square and seeing how they render using
			 * this computed matrix transformation**/
			/**
            Launcher.a1 = VectorTools.mult(
                    fL_matrix_z,
                    VectorTools.mult(
                            fL_matrix_y,
                            VectorTools.mult(
                                    fL_matrix_x,Launcher.a1)));
            Launcher.a2 = VectorTools.mult(
                    fL_matrix_z,
                    VectorTools.mult(
                            fL_matrix_y,
                            VectorTools.mult(
                                    fL_matrix_x,Launcher.a2)));
            Launcher.a3 = VectorTools.mult(
                    fL_matrix_z,
                    VectorTools.mult(
                            fL_matrix_y,
                            VectorTools.mult(
                                    fL_matrix_x,Launcher.a3)));
            Launcher.a4 = VectorTools.mult(
                    fL_matrix_z,
                    VectorTools.mult(
                            fL_matrix_y,
                            VectorTools.mult(
                                    fL_matrix_x,Launcher.a4)));**/
			//System.out.println(f2L_fL_displacement_world_mag);
			//System.out.println(f2L_fL_displacement_cam_mag);


			float fL_zoom = (/** 2* **/fL_w1 /** /2 **/ +
					2*f2L_fL_displacement_cam_mag) * ControlVariables.zoom_factor;
			float [] fL_center = f2L_pos;

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/

			/**Set the required variables for the frame fi+2L**/
			float f2L_zoom = fL_zoom;
			float [] f2L_center = f2L_pos;

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/

			/**Store the starting and ending zoom value within the first motion scene**/
			fms_next.setFiZoom(fi_zoom);
			fms_next.setFLZoom(fL_zoom);
			fms_next.setF2LZoom(f2L_zoom);

			fms_next.setFiCenter(fi_center);
			fms_next.setFLCenter(fL_center);
			fms_next.setF2LCenter(f2L_center);

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/

			/**Compute the camera centers and zoom levels between the frame numbers
			 * and  the zoom levels  and camera centers at  those frame  numbers**/
			fms_next.computeZoomLevels();
			fms_next.computeCameraCenters(pca_areas);

			/**---------------------------------------------------------------**/
			/**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
			/**---------------------------------------------------------------**/
		}
		/**-------------------------------------------------------------------------**/
		/**|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||**/
		/**-------------------------------------------------------------------------**/
	}
	/**Apply  first  level motion effect  for each scene, given
	 * the first level motion objects store per camera scene**/
	public void applyFirstLevelMotionScenes() {

		Enumeration<CameraScene> cs_enum = cameraScenes.elements();
		while(cs_enum.hasMoreElements()) {

			CameraScene cs = cs_enum.nextElement();

			/**Modify the zoom values and camera centers  in
			 * this  camera  scene,  using  the first  level
			 * motion scenes stored for this camera scene**/
			cs.applyFirstLevelMotionScenes();
		}
	}


	/**--------------------------------------------------------------------**/
	/**             FUNCTIONS FOR ITERATING THROUGH THE SCENES             **/
	/**--------------------------------------------------------------------**/
	/**Returning the current camera scene being read**/
	public CameraScene getCurrentScene() {
		CameraScene cs = cameraScenes.elementAt(current_scene_number);
		if(cs.hasMoreFrames()) {
			return cs;
		} else {
			current_scene_number++;

			if(current_scene_number >= cameraScenes.size()) {
				current_scene_number = 0;
				CameraScene next_cs = cameraScenes.elementAt(current_scene_number);
				next_cs.resetFrameIter();
				return next_cs;
			} else {
				CameraScene next_cs = cameraScenes.elementAt(current_scene_number);
				next_cs.resetFrameIter();
				return next_cs;
			}
		}
	}


	/**--------------------------------------------------------------------**/
	/**               FUNCTIONS FOR SETTING UP DATA STRUCTURES             **/
	/**--------------------------------------------------------------------**/
	/**Get the view point direction per frame for the camera, using the
	 * information from the PCA components**/
	private float [][] getCameraOrnPerFrame(float [][][] pcaPerFrame) {

		float [][] camera_orn_per_frame = new float[pcaPerFrame.length][3];

		for(int i = 0; i < pcaPerFrame.length; i++) {
			float [][] pca_s = pcaPerFrame[i];

			JointSticks js = joint_sticks_per_frame[i];
			JointStick orienting_js = js.getJointStick(ControlVariables.joint_stick_name);
			double [] s = orienting_js.getStartPoint();
			double [] e = orienting_js.getEndPoint();
			double [] e_s = VectorTools.sub(e,s);
			float [] e_s_f = new float [] {(float) e_s[0], (float) e_s[1], (float) e_s[2]};


			/**Retrieve the required PCAs for the object number**/
			float [] p1 =         pca_s[0].clone();
			float [] p2 =         pca_s[2].clone();
			float [] avg =         pca_s[1].clone();

			float [] p1_avg = VectorTools.sub(p1,avg);
			float [] p2_avg = VectorTools.sub(p2,avg);

			/**Get the normal to the two PCA components**/
			float [] pca_normal =  VectorTools.norm(VectorTools.cross(p1_avg, p2_avg));

			float [] pca_normal2 = new float[]{
					-ControlVariables.pcaNormalsLength*pca_normal[0],
					-ControlVariables.pcaNormalsLength*pca_normal[1],
					ControlVariables.pcaNormalsLength*Math.abs(pca_normal[2])
			};

			float [] pca_normal_avg = VectorTools.add(pca_normal, avg);
			float side = VectorTools.dot(pca_normal_avg, e_s_f);

			/**Ensure this vector is aligned with the joint stick.
			 * Otherwise change the direction of the z component**/
			if(ControlVariables.joint_stick_face_direction*side > 0) {
				pca_normal2 = new float[]{
						-ControlVariables.pcaNormalsLength*pca_normal[0],
						-ControlVariables.pcaNormalsLength*pca_normal[1],
						-ControlVariables.pcaNormalsLength*Math.abs(pca_normal[2])
				};
			}

			camera_orn_per_frame[i] = pca_normal2;
		}

		return camera_orn_per_frame;
	}
	/**Get the center of position of the camera for each frame**/
	private float[][] getCameraCenterPerFrame(float [][][] pcaPerFrame) {

		float [] [] camera_center_per_frame = new float[pcaPerFrame.length][3];

		for(int i = 0; i < pcaPerFrame.length; i++) {
			float [][] pca_s = pcaPerFrame[i];

			float [] center = new float[]{
					-pca_s[1][0],
					-pca_s[1][1],
					-pca_s[1][2]
			};

			camera_center_per_frame[i] = center;
		}

		return camera_center_per_frame;
	}
	/**Get the largest area spanned per frame from
	 * the view point of the two PCA vectors**/
	private float [][] getAreaPerFrame(float [][][] pcaPerFrame) {

		float [][] pca_area_per_frame = new float[pcaPerFrame.length][3];

		for(int i = 0; i < pcaPerFrame.length; i++) {
			float [][] pca_s = pcaPerFrame[i];

			float [] avg_pt = pca_s[1].clone();


			float [] pca1 = VectorTools.sub(pca_s[0], avg_pt).clone();
			float [] pca2 = VectorTools.sub(pca_s[2], avg_pt).clone();
			float [] pca3 = VectorTools.sub(pca_s[3], avg_pt).clone();

			float [][] V = new float[][]{pca1,pca2,pca3};


			float [][][]lines = data_per_frame[i];
			float [][] distinct_points = getPoints(lines);
			float [][] distinct_points_T = VectorTools.getTranspose(distinct_points);
			for(int j = 0; j < distinct_points_T.length; j++) {
				distinct_points_T[j] = PCATools.getAdjustedSet(distinct_points_T[j]);
			}


			pca_area_per_frame[i] = PCATools.getPCAArea(distinct_points_T, V);
		}

		return pca_area_per_frame;
	}
	/**Given the line data, return the data as a matrix of distinct points
	 * of size n by m, n being the number of points, m being the dimension**/
	private float[][] getPoints(float[][][]lines) {

		/**Iterate through the lines and collect all the points**/
		Hashtable<String, float[]> distinct_points =
				new Hashtable<String, float[]>();
				for(int i = 0; i < lines.length; i++) {

					float[][]line = lines[i];

					float[]p_a = new float[]{line[0][0], line[0][1], line[0][2]};
					float[]p_b = new float[]{line[1][0], line[1][1], line[1][2]};

					distinct_points.put(p_a[0]+" "+p_a[1]+" "+p_a[2], p_a);
					distinct_points.put(p_b[0]+" "+p_b[1]+" "+p_b[2], p_b);
				}

				/**Collect the points into a n by m vector,
				 * where n is the number of points**/
				float [][] points = new float[distinct_points.size()][3];

				Enumeration<String> keys = distinct_points.keys();
				int value_counter = 0;
				while(keys.hasMoreElements()) {
					String key = keys.nextElement();
					float [] key_value = distinct_points.get(key);
					points[value_counter] = key_value;
					value_counter++;
				}

				return points;
	}
	/**Given the position of object in each frame, calculate the displacement
	 * of the object position from the previous frame**/
	public float [][] getDisPerFrame(float [][] camera_center_per_frame) {
		float [][] dis_per_frame = new float [camera_center_per_frame.length][3];

		for(int i = 0; i < camera_center_per_frame.length; i++) {
			if(i == 0) {
				dis_per_frame[i] = new float []{0.0f,0.0f,0.0f};
			} else {
				dis_per_frame[i] =  VectorTools.sub(camera_center_per_frame[i], camera_center_per_frame[i-1]);
			}
		}
		return dis_per_frame;
	}
	/**Given the displacement after each frame, calculate the acceleration,
	 * given the current and upcoming displacement**/
	public float [] getAccPerFrame(float [][] dis_per_frame) {

		/**
        1, 2, 5, 1, 4, 3, 2, 6, 7, 3, 1, 5
        0, 1, 3,-4, 3,-1,-1, 4, 1,-4,-2, 4

        1, 2, 1, 1, 2, 0, 3, 3, 3, 2, 2, 0
        1, 2,-7, 7,-4, 0, 5,-3,-5, 2, 6, 0
		 **/

		float [] acc_per_frame = new float [dis_per_frame.length];

		for(int i = 0; i < dis_per_frame.length; i++) {
			if(i == (dis_per_frame.length - 1)) {
				acc_per_frame[i] = 0;
			} else {
				acc_per_frame[i] =
						VectorTools.mag(dis_per_frame[i+1]) - VectorTools.mag(dis_per_frame[i]);
			}
		}
		return acc_per_frame;
	}
	/**Given the motion area for each frame, calculate
	 * the default zoom value for each frame**/
	public float [] getDefaultZoomValPerFrame(float [][] pca_area_per_frame) {

		float [] zoom_val_per_frame = new float [pca_area_per_frame.length];

		for(int i = 0; i < pca_area_per_frame.length; i++) {
			float [] area_i = pca_area_per_frame[i];

			float w1 = area_i[1];
			float w2 = area_i[2];
			if(w1 < w2) {
				w1 = w2;
			}

			zoom_val_per_frame[i] = w1*ControlVariables.zoom_factor;
		}

		return zoom_val_per_frame;
	}
}