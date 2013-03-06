package CameraMotion.ThirdLevelMotion;


public class ThirdLevelFrameGroup {

	/**The frame number of this skeleton wrapper**/
	private int frame_number = 0;
	
	/**The motion value centered at this frame group**/
	private double motion_value = 0.0;

	/**The  number of  frames  offset from  the  center
	 * marking the left boundary of this frame group**/
	private int left_bound_offset = 0;
	
	/**The  number  of  frames  offset from  the  center
	 * marking the right boundary of this frame group**/
	private int right_bound_offset = 0;
	
	/**The name of the root of this frame group**/
	private String root_name;


	/**Class constructor**/
	public ThirdLevelFrameGroup(
			int frame_number,
			String root_name,
			double motion_value) {

		this.frame_number = frame_number;
		this.motion_value = motion_value;
		this.root_name = root_name;
	}


	/**Set the left bound offset**/
	public void setLeftBoundOffset(int left_bound_offset) {
		this.left_bound_offset = left_bound_offset;
	}
	/**Set the right bound offset**/
	public void setRightBoundOffset(int right_bound_offset) {
		this.right_bound_offset = right_bound_offset;
	}


	/**Get the left bound offset**/
	public int getLeftBoundOffset() {
		return left_bound_offset;
	}
	/**Get the right bound offset**/
	public int getRightBoundOffset() {
		return right_bound_offset;
	}

	/**Get the frame numer**/
	public int getFrameNumber() {
		return frame_number;
	}
	/**Get the motion value centered at this frame group**/
	public double getFrameGroupMotionValue() {
		return motion_value;
	}
	/**Get the name of the root of this frame group**/
	public String getRootName() {
		return this.root_name;
	}
}
