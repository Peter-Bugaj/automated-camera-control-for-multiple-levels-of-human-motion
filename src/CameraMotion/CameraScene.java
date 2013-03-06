package CameraMotion;


import java.util.Hashtable;

import main.ControlVariables;

import CameraMotion.ThirdLevelMotion.ThirdLevelFrameGroup;
import CameraMotion.ThirdLevelMotion.ThirdLevelMotionScene;
import CameraMotion.ZoomControl.FirstLevelMotionScene;


public class CameraScene {

	/**----------------------------------------------------------------------------**/
	/**Camera orientations belonging to this scene**/
	private float[][] camera_orientations = null;
	/**The frame numbers belonging to this camera scene**/
	private int [] frame_numbers = null;
	/**The camera centers to this camera scene**/
	private float [][] camera_centers = null;
	/**The motion area of each frame in the scene**/
	private float [][] pca_area_per_frame = null;
	/**The numerical filter for this scene, iterating through the frame number**/
	private int frame_iterator = 0;
	/**List of displacement vectors per frame**/
	private float[][] dis_per_frame = null;
	/**List of acceleration values per frame**/
	private float[] acc_per_frame = null;
	/**List of zoom values per frame**/
	private float[] zoom_val_per_frame = null;
	/**----------------------------------------------------------------------------**/


	/**----------------------------------------------------------------------------**/
	/**                   Variables dealing with third level motion                **/
	/**----------------------------------------------------------------------------**/
	/**The first motion scenes belonging to this scene**/
	private FirstLevelMotionScene [] fms = null;
	/**----------------------------------------------------------------------------**/


	/**----------------------------------------------------------------------------**/
	/**                   Variables dealing with third level motion                **/
	/**----------------------------------------------------------------------------**/
	/**Pointer to the list of third level motion scenes**/
	private ThirdLevelMotionScene[] thirdLevelMotionScenes = null;
	/**Hashtale associating each third level motion scene with a frame number**/
	private Hashtable<String, ThirdLevelMotionScene> tlms_per_framenumner = null;
	/**The current third level motion scene being played, if any**/
	private ThirdLevelMotionScene cur_tlms = null;
	/**Indication of whether a third motion
	 * level sub-scene is being iterated**/
	private boolean third_level_ms_active = false;

	/**The   current  camera  centers  stored  for  the
	 * current third motion level scene being played**/
	private float[][] third_level_camera_centers = null;
	/**The  current camera zoom  levels stored  for the
	 * current third motion level scene being played**/
	private float [] third_level_camera_zoom_levels = null;
	/**The current camera zoom orientations stored for the
	 * current  third motion  level scene being  played**/
	private float [][] third_level_camera_orientations = null;

	/**The start frame of the third level
	 * motion scene currently iterated**/
	private int tlms_start_frame = -1;
	/**The end  frame of the  third level
	 * motion scene currently iterated**/
	private int tlms_end_frame = -1;
	/**The current third level motion scene frame number**/
	private int tlms_frame_number = -1;
	/**The counter for the current third level motion scene frame number**/
	private int tlms_frame_number_counter = -1;
	/**----------------------------------------------------------------------------**/


	/**Class constructor**/
	public CameraScene () {
		frame_iterator = 0;
	}


	/**----------------------------------------------------------------------------**/
	/**Add a list of camera orientation**/
	public void addOrientations(float [][] camera_orientations) {
		this.camera_orientations = camera_orientations.clone();
	}
	/**Add a list of frame numbers**/
	public void addFrameNumbers(int [] frame_numbers) {
		this.frame_numbers=frame_numbers.clone();
	}
	/**Add a list of camera centers**/
	public void addCameraCenters(float [][] camera_centers) {
		this.camera_centers=camera_centers.clone();
	}
	/**Add a list of motion areas**/
	public void addPCAAreas(float [][] pca_area_per_frame) {
		this.pca_area_per_frame=pca_area_per_frame.clone();
	}
	/**Add a list of acceleration values**/
	public void addAccelerations(float [] acc_per_frame) {
		this.acc_per_frame=acc_per_frame.clone();
	}
	/**Add a list of displacement vectorss**/
	public void addDisplacements(float [][] dis_per_frame) {
		this.dis_per_frame=dis_per_frame.clone();
	}
	/**Add the list of zoom values**/
	public void addZoomVals(float [] zoom_val_per_frame) {
		this.zoom_val_per_frame = zoom_val_per_frame.clone();
	}
	/**----------------------------------------------------------------------------**/


	/**----------------------------------------------------------------------------**/
	/**                   Functions dealing with first level motion                **/
	/**----------------------------------------------------------------------------**/
	/**Add a list of first motion scenes**/
	public void addFirstMotionScenes(FirstLevelMotionScene [] fms) {
		this.fms = fms.clone();
	}
	/**----------------------------------------------------------------------------**/


	/**Add a pointer to the list of third level motion scenes**/
	/**----------------------------------------------------------------------------**/
	/**                   Functions dealing with third level motion                **/
	/**----------------------------------------------------------------------------**/
	public void addThirdLevelMotionScenes(
			ThirdLevelMotionScene[] thirdLevelMotionScenes) {
		this.thirdLevelMotionScenes = thirdLevelMotionScenes;
	}
	/**----------------------------------------------------------------------------**/


	/**Get the camera orientations**/
	public float [][] getOrientations() {
		return camera_orientations.clone();
	}
	/**Get the frame numbers**/
	public int [] getFrameNumbers() {
		return frame_numbers.clone();
	}
	/**Get the camera center**/
	public float [][] getCameraCenters() {
		return camera_centers.clone();
	}
	/**Get the pca areas**/
	public float [][] getPCAAreas() {
		return pca_area_per_frame.clone();
	}
	/**Get the displacement vectors**/
	public float [][] getDisVectors() {
		return dis_per_frame.clone();
	}
	/**Get the acceleration values**/
	public float [] getAccValues() {
		return acc_per_frame.clone();
	}
	/**Get the zoom values**/
	public float [] getZoomValues() {
		return zoom_val_per_frame.clone();
	}


	/**Get the number of camera orientations for this scene**/
	public int getNumberOrientations() {
		return camera_orientations.length;
	}
	/**Get the number of frames for this scene**/
	public int getNumberFrames() {
		return frame_numbers.length;
	}
	/**Get the number of camera centers for this scene**/
	public int getNumberCameraCenters() {
		return camera_centers.length;
	}
	/**Get the number of motion areas stored**/
	public int getNumberPCAAreas() {
		return pca_area_per_frame.length;
	}
	/**Get the number of displacement vectors stored**/
	public int getNumberDisVectors() {
		return dis_per_frame.length;
	}
	/**Get the number of acceleration values stored**/
	public int getNumberAccValues() {
		return acc_per_frame.length;
	}
	/**Get the list of first motion scenes**/
	public FirstLevelMotionScene [] getFirstMotionScenes() {
		return fms;
	}

	public int getNextFrameNumber() {
		int t= (frame_iterator-1);

		if(third_level_ms_active) {
			tlms_frame_number++;
			tlms_frame_number_counter++;

			if((tlms_frame_number_counter) == tlms_end_frame-tlms_start_frame-1) {
				third_level_ms_active = false;
				frame_iterator += tlms_frame_number_counter-1 - 50;
			} else {
				t = tlms_frame_number;
			}
		}

		if(!third_level_ms_active) {

			t = frame_iterator;
			frame_iterator++;

			/**If  the frame  number  marks the  center
			 * of  a third  level sub-scene,  show that
			 * scene starting from this frame number**/
			if(tlms_per_framenumner != null &&
					tlms_per_framenumner.containsKey((t/**+25**/)+"")) {

				third_level_ms_active = true;
				cur_tlms = tlms_per_framenumner.get((t/**+25**/)+"");

				third_level_camera_centers = cur_tlms.getCenters();
				third_level_camera_zoom_levels = cur_tlms.getZoomLevels();
				third_level_camera_orientations = cur_tlms.getOrientations();

				ThirdLevelFrameGroup tlfg = cur_tlms.getTLFG();

				tlms_start_frame = tlfg.getFrameNumber();
				tlms_end_frame = tlfg.getFrameNumber()+tlfg.getRightBoundOffset();


				tlms_frame_number = frame_iterator+0;//25;
				tlms_frame_number_counter = 0;

				return t+frame_numbers[0];
			} else {
				return t+frame_numbers[0];
			}
		}

		//System.out.println(t+frame_numbers[0]);
		return t+frame_numbers[0];
	}


	/**Reset iterator**/
	public void resetFrameIter() {
		frame_iterator = 0;
	}
	/**Check if iterator has more values**/
	public boolean hasMoreFrames() {
		if(frame_iterator == frame_numbers.length) {
			return false;
		}
		return true;
	}


	/**Get a specific PCA area by frame number**/
	public float [] getPCAArea(int f) {
		return this.pca_area_per_frame[f-frame_numbers[0]];
	}
	/**Get a specific camera orientation by frame number**/
	public float [] getCamOrientation(int f) {
		if(this.third_level_ms_active) {
			return this.third_level_camera_orientations
					[tlms_frame_number_counter];
		} else {
			return this.camera_orientations[f-frame_numbers[0]].clone();
		}
	}
	/**Get a specific camera center by frame number**/
	public float [] getCamCenter(int f) {
		if(this.third_level_ms_active) {
			return this.third_level_camera_centers
					[tlms_frame_number_counter];
		} else {
			return this.camera_centers[f-frame_numbers[0]].clone();
		}
	}
	/**Get a specific acceleration value by frame number**/
	public float getAccValue(int f) {
		return this.acc_per_frame[f-frame_numbers[0]];
	}
	/**Get a specific zoom value by frame number**/
	public float getZoomValue(int f) {
		if(this.third_level_ms_active) {
			return this.third_level_camera_zoom_levels
					[tlms_frame_number_counter];
		} else {
			return this.zoom_val_per_frame[f-frame_numbers[0]];
		}
	}
	/**Get a specific displacement vector by frame number**/
	public float [] getDisplacement(int f) {
		return this.dis_per_frame[f-frame_numbers[0]].clone();
	}


	/**Modify the zoom values and camera centers  in
	 * this  camera  scene,  using  the first  level
	 * motion scenes stored for this camera scene**/
	public void applyFirstLevelMotionScenes() {

		/**Iterate through each first level motion scene**/
		for(int i = 0; i < fms.length; i++) {

			FirstLevelMotionScene fms_i = fms[i];
			float [] zooms_i = fms_i.getZoomValues();
			float [][] centers_i = fms_i.getCameraCenters();
			int fi = fms_i.getFi();
			int f2L = fms_i.getF2L();
			int fk = fms_i.getFk();
			int off = fms_i.getOffSet();

			if(frame_numbers[0] != off) {
				System.out.println(off);
				System.out.println(frame_numbers[0]);
				System.exit(1);
			}
			/**Change the zoom values and camera centers in
			 * this camera scene to the zoom values and camera
			 * centers in this first level motion scene**/
			for(int j = (fi-off); j < (fk-off); j++) {
				zoom_val_per_frame[j] = zooms_i[j-(fi-off)];
				//System.out.println("zoom: "+zoom_val_per_frame[j]);
			}
			for(int j = (fi-off); j < (f2L-off); j++) {
				camera_centers[j] = centers_i[j-(fi-off)];
			}
		}
	}


	/**Using the  list of third level  motion
	 * subscenes, add in  addition sub-scenes
	 * to represent the third level motion**/
	public void addThirdLevelMotionSubScenes() {
		tlms_per_framenumner =
				new Hashtable<String, ThirdLevelMotionScene>();
		for(int i = 0; i < thirdLevelMotionScenes.length; i++) {
			
			if(thirdLevelMotionScenes[i]==null) {
				continue;
			}
			if(thirdLevelMotionScenes[i].overlap_fac > ControlVariables.overlap_thres /**TODO: make variable private**/) {
				continue;
			}
			
			ThirdLevelMotionScene tlms_i = thirdLevelMotionScenes[i];
			ThirdLevelFrameGroup tlfg_i = tlms_i.getTLFG();
			int f_i = tlfg_i.getFrameNumber();
			int f_i_top = f_i + (tlfg_i.getLeftBoundOffset()+tlfg_i.getRightBoundOffset());
			for(int j = i+1; j < thirdLevelMotionScenes.length; j++) {
				ThirdLevelMotionScene tlms_j= thirdLevelMotionScenes[j];

				if(tlms_j == null) {
					continue;
				}
				
				ThirdLevelFrameGroup tlfg_j = tlms_j.getTLFG();				
				int f_j = tlfg_j.getFrameNumber();
				int f_j_top = f_j + (tlfg_j.getLeftBoundOffset()+tlfg_j.getRightBoundOffset());
				if(
						((f_i_top+ControlVariables.minInBetSubSnLen) > f_j && f_j > f_i) ||
						((f_j_top+ControlVariables.minInBetSubSnLen) > f_i && f_i > f_j)
						) {
					thirdLevelMotionScenes[j] = null;
				}
			}
			

			System.out.println("\n"+tlfg_i.getRootName());
			System.out.println(tlfg_i.getFrameNumber()+"\n");

			tlms_per_framenumner.put(f_i+"", tlms_i);
			//break;
		}
	}
}