package CameraMotion.ZoomControl;

import Tools.VectorTools;


public class FirstLevelMotionScene {

    /**Frame number of fi**/
    private int fi = 0;
    /**Frame number of fi+k**/
    private int fk = 0;

    /**Frame number of fi+L**/
    private int fL = 0;
    /**Frame number of fi+2L**/
    private int f2L = 0;

    /**The offset of the frame numbers**/
    private int frame_num_offset = 0;

    /**The starting zoom value**/
    private float fi_zoom_val = 0.0f;
    /**The end zoom value**/
    private float fL_zoom_val = 0.0f;
    /**The zoom value at fi + 2L**/
    private float f2L_zoom_val = 0.0f;

    /**The starting camera center value**/
    private float [] fi_center = null;
    /**The end camera center**/
    private float [] fL_center = null;
    /**The camera center at fi + 2L**/
    private float [] f2L_center = null;

    /**The set of zoom values computed for this scnene**/
    private float [] zoom_values = null;
    /**The set of camera centers computed for this scnene**/
    private float [][] camera_centers = null;


    /**Class constructor**/
    public FirstLevelMotionScene() {
    }


    /**Set frame number of fi**/
    public void setFi(int fi) {
        this.fi=fi;
    }
    /**Set frame number of fi+k**/
    public void setFk(int fk) {
        this.fk=fk;
    }
    /**Set frame number of fi+L**/
    public void setFL(int fL) {
        this.fL=fL;
    }
    /**Set frame number of fi+2L**/
    public void setF2L(int f2L) {
        this.f2L=f2L;
    }

    /**Set the offset of the frame numbers**/
    public void setOffSet(int offset) {
        frame_num_offset = offset;
    }

    /**Set the starting zoom value**/
    public void setFiZoom(float fi_zoom_val) {
        this.fi_zoom_val=fi_zoom_val;
    }
    /**Set the ending zoom value**/
    public void setFLZoom(float fL_zoom_val) {
        this.fL_zoom_val=fL_zoom_val;;
    }
    /**Set the zoom value at fi + 2L**/
    public void setF2LZoom(float f2L_zoom_val) {
        this.f2L_zoom_val=f2L_zoom_val;;
    }

    /**Set the starting camera center**/
    public void setFiCenter(float [] fi_center) {
        this.fi_center=fi_center;
    }
    /**Set the ending camera center**/
    public void setFLCenter(float [] fL_center) {
        this.fL_center=fL_center;
    }
    /**Set the camera center at fi + 2L**/
    public void setF2LCenter(float [] f2L_center) {
        this.f2L_center=f2L_center;
    }


    /**Return frame number of fi**/
    public int getFi() {
        return fi;
    }
    /**Return frame number of fi+k**/
    public int getFk() {
        return fk;
    }
    /**Return frame number of fi+L**/
    public int getFL() {
        return fL;
    }
    /**Return frame number of fi+2L**/
    public int getF2L() {
        return f2L;
    }

    /**Return the frame number offset**/
    public int getOffSet() {
        return frame_num_offset;
    }

    /**Get the starting zoom value**/
    public float getFiZoom() {
        return fi_zoom_val;
    }
    /**Get the ending zoom value**/
    public float getFLZoom() {
        return fL_zoom_val;
    }
    /**Get the zoom value at fi + 2L**/
    public float getF2LZoom() {
        return f2L_zoom_val;
    }

    /**Get the starting camera center**/
    public float [] getFiCenter() {
        return fi_center;
    }
    /**Get the ending camera center**/
    public float [] getFLCenter() {
        return fL_center;
    }
    /**Get the camera center at fi + 2L**/
    public float [] getF2LCenter() {
        return f2L_center;
    }

    /**Get the computed camera centers for this scene**/
    public float [][] getCameraCenters() {
        return camera_centers.clone();
    }
    /**Get the computed zoom values for this scene**/
    public float [] getZoomValues() {
        return zoom_values.clone();
    }


    /**Compute the zoom levels between the marked frames
     * and  associated  zoom values  at those  frames**/
    public void computeZoomLevels() {
        zoom_values = new float[fk-fi];

        /**Get the zoom level increment required between
         * the zoom level at frame fi  and frame fi+L**/
        float b = (fL_zoom_val-fi_zoom_val)/(fL-fi);
        float neg_b = (fi_zoom_val - fL_zoom_val)/(fk-f2L);

        for(int i = 0; i < (fL-fi); i++) {
            zoom_values[i] = fi_zoom_val + (i*b);
            //System.out.println("zoom1: "+zoom_values[i]);
        }
        for(int i = (fL-fi); i < (f2L-fi); i++) {
            zoom_values[i] = fL_zoom_val;
            //System.out.println("zoom2: "+zoom_values[i]);
        }
        for(int i = (f2L-fi); i < (fk-fi); i++) {
            zoom_values[i] = fL_zoom_val + ((i-(f2L-fi))*neg_b);
            //System.out.println("zoom3: "+zoom_values[i]);
        }
    }
    /**Compute the camera centers between the marked frames
     * and  associated  camera centers at  those  frames**/
    public void computeCameraCenters(float [][] pca_areas) {
        camera_centers = new float[f2L-fi][3];

        float [] pos_shift = VectorTools.sub(fL_center, fi_center);
        float [] inc_shift = VectorTools.mult(1.0f/(fL-fi-0.0f), pos_shift);

        for(int i = 0; i < (fL-fi); i++) {
            camera_centers[i] = VectorTools.add(fi_center, VectorTools.mult(i, inc_shift));
        }

        for(int i = (fL-fi); i < (f2L-fi); i++) {
            camera_centers[i] = f2L_center;
        }
    }
}
