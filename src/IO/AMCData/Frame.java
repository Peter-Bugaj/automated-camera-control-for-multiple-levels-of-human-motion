package IO.AMCData;

import java.util.Hashtable;

public class Frame {

    /**Number for this frame**/
    private int frame_number = 0;

    /**Rotation for each body segment: body-segment name --> rotation values**/
    private Hashtable<String, double[]> body_rotations = null;

    /**Position of the root**/
    private double [] root_position = null;

    /**Orientation of the root**/
    private double [] root_orientation = null;



    /**Class constructor**/
    public Frame() {
        body_rotations = new Hashtable<String, double[]>();
    }


    /**Set the frame number**/
    public void setFrameNumber(int frame_number) {
        this.frame_number=frame_number;
    }

    /**Set the orientation of the root**/
    public void setRootOrientation(double[]root_orientation) {
        this.root_orientation=root_orientation;
    }

    /**Set the position of the root**/
    public void setRootPosition(double[]root_position) {
        this.root_position=root_position;
    }

    /**Add rotation values for a segment**/
    public void addSegmentRotation(String segment_name, double[]seg_rotation) {
        body_rotations.put(segment_name, seg_rotation);
    }



    /**Get the orientation of the root**/
    public double[] getRootOrientation() {
        return root_orientation;
    }

    /**Get the position of the root**/
    public double[] getRootPosition() {
        return root_position;
    }
    /**Get the frame number**/
    public int getFrameNumber() {
        return frame_number;
    }

    /**Get rotation values for a segment**/
    public double[] getSegmentRotation(String segment_name) {
        return body_rotations.get(segment_name);
    }
}
