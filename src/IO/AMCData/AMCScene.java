package IO.AMCData;

import java.util.Hashtable;

public class AMCScene {

    /**Frame stored per number: i -> frame at number i **/
    private Hashtable<String, Frame> frames = null;

    /**Total number of frames stored so far**/
    private int total_frames = 0;


    /**Class constructor**/
    public AMCScene() {
        frames = new Hashtable<String, Frame>();
    }

    /**Add a frame**/
    public void addFrame(Frame f) {
        frames.put(total_frames+"", f);
        total_frames++;
    }

    /**Get a frame at a given number**/
    public Frame getFrame(int frame_num) {
        return frames.get(frame_num+"");
    }

    /**Get the total number of frames**/
    public int getTotalFrames() {
        return total_frames;
    }
}
