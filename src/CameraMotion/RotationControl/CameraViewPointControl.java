package CameraMotion.RotationControl;

import java.util.Arrays;

import main.ControlVariables;

import Tools.VectorTools;
import Tools.LineFilters.AFilter;
import Tools.LineFilters.Filter;

public class CameraViewPointControl {


    /**Filter to use for the data curve**/
    private Filter filter = AFilter.getFilter();

    /**Number of neighbours to account from left and right, for the filter**/
    private int filter_spread = ControlVariables.filter_spread_rot;


    /**Smoothen  out the  camera  motion for each frame,  given  the
     * camera view direction per frame, and motion area per frame**/
    /**Good with gaussian 35, constant 15**/
    public float[][] smoothenCameraMotion(
            float [][]camera_orn_per_frame,
            float [] pca_area_per_frame) {

        float [][] new_camera_orn_per_frame = new float[camera_orn_per_frame.length][3];

        for(int i = 0; i < camera_orn_per_frame.length; i++) {

            float [] vec_sum = new float[]{0.0f,0.0f,0.0f};

            for(int j = 0; j <= filter_spread; j++) {
                if(i-j > -1) {

                    float [] frame_i_m_j = camera_orn_per_frame[i-j];
                    frame_i_m_j = VectorTools.mult(filter.f(-j/filter_spread*2.5f),frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }

            for(int j = 1; j <= filter_spread; j++) {
                if(i+j < camera_orn_per_frame.length) {

                    float [] frame_i_m_j = camera_orn_per_frame[i+j];
                    frame_i_m_j = VectorTools.mult(filter.f(j/filter_spread*2.5f),frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }

            vec_sum = VectorTools.mult(1.0f/((filter_spread*2)+1.0f), vec_sum);
            new_camera_orn_per_frame[i] = vec_sum;
        }
        return new_camera_orn_per_frame;
    }

    /**Smoothen  out the  camera  motion for each frame,  given  the
     * camera view direction per frame, and motion area per frame**/
    /**Good with gaussian 35, constant 15**/
    public float[][] smoothenCameraMotion_UsingArea(
            float [][]camera_orn_per_frame,
            float [] pca_area_per_frame) {

        /**Get the largest and **/
        float [] temp_areas = pca_area_per_frame.clone();
        Arrays.sort(temp_areas);

        float smallest_area = temp_areas[0];
        float largest_area = temp_areas[temp_areas.length-1];
        float area_diff = largest_area-smallest_area;

        float [][] new_camera_orn_per_frame = new float[camera_orn_per_frame.length][3];



        for(int i = 0; i < camera_orn_per_frame.length; i++) {

            float [] vec_sum = new float[]{0.0f,0.0f,0.0f};

            for(int j = 0; j <= filter_spread; j++) {
                if(i-j > -1) {

                    float [] frame_i_m_j = camera_orn_per_frame[i-j];
                    float area_i_m_j = pca_area_per_frame[i-j];

                    float area_factor = (area_i_m_j-smallest_area)/area_diff;

                    frame_i_m_j = VectorTools.mult(filter.f(-j/filter_spread*2.5f) * area_factor ,frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }

            for(int j = 1; j <= filter_spread; j++) {
                if(i+j < camera_orn_per_frame.length) {

                    float [] frame_i_m_j = camera_orn_per_frame[i+j];
                    float area_i_m_j = pca_area_per_frame[i+j];

                    float area_factor = (area_i_m_j-smallest_area)/area_diff;

                    frame_i_m_j = VectorTools.mult(filter.f(j/filter_spread*2.5f) * area_factor ,frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }

            vec_sum = VectorTools.mult(1.0f/((filter_spread*2)+1.0f), vec_sum);
            new_camera_orn_per_frame[i] = vec_sum;
        }
        return new_camera_orn_per_frame;
    }

    /**Smoothen  out the  camera  motion for each frame,  given  the
     * camera view direction per frame, and motion area per frame**/
    /**Good with gaussian 35, constant 15**/
    public float[][] smoothenCameraMotion_UsingAdjustingArea(
            float [][]camera_orn_per_frame,
            float [] pca_area_per_frame) {

        float [][] new_camera_orn_per_frame = new float[camera_orn_per_frame.length][3];
        for(int i = 0; i < camera_orn_per_frame.length; i++) {

            float [] vec_sum = new float[]{0.0f,0.0f,0.0f};


            /**Calculate the highest and lowest area between frame i-filter_spread and i+filter_spread**/
            float [] areas_on_filter_spread =
                    new float[(filter_spread*5*2)+1];
            for(int j = 0; j <= filter_spread*5; j++) {
                if(i-j > -1) {
                    areas_on_filter_spread[-j+filter_spread*5] = pca_area_per_frame[i-j];
                } else {
                    int k = i-j;

                    areas_on_filter_spread[-j+filter_spread*5] = pca_area_per_frame[i-j-k];
                }
            }

            for(int j = 1; j <= filter_spread*5; j++) {
                if(i+j < camera_orn_per_frame.length) {
                    areas_on_filter_spread[j+filter_spread*5] = pca_area_per_frame[i+j];
                } else {
                    int k = i+j;
                    areas_on_filter_spread[j+filter_spread*5] = pca_area_per_frame[i+j-k];
                }
            }
            Arrays.sort(areas_on_filter_spread);
            float smallest_local_area= areas_on_filter_spread[0];
            float largest_local_area = areas_on_filter_spread[areas_on_filter_spread.length-1];
            float area_diff = largest_local_area-smallest_local_area;



            /**Apply the filter, using the area difference, along with the smallest and largest area**/
            for(int j = 0; j <= filter_spread; j++) {
                if(i-j > -1) {

                    float [] frame_i_m_j = camera_orn_per_frame[i-j];
                    float area_i_m_j = pca_area_per_frame[i-j];

                    float area_factor = area_i_m_j/area_diff;

                    frame_i_m_j = VectorTools.mult(filter.f(-j/filter_spread*2.5f) * area_factor ,frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }

            for(int j = 1; j <= filter_spread; j++) {
                if(i+j < camera_orn_per_frame.length) {

                    float [] frame_i_m_j = camera_orn_per_frame[i+j];
                    float area_i_m_j = pca_area_per_frame[i+j];

                    float area_factor = area_i_m_j/area_diff;

                    frame_i_m_j = VectorTools.mult(filter.f(j/filter_spread*2.5f) * area_factor ,frame_i_m_j);

                    vec_sum = VectorTools.add(vec_sum, frame_i_m_j);
                }
            }


            vec_sum = VectorTools.mult(1.0f/((filter_spread*2)+1.0f), vec_sum);
            new_camera_orn_per_frame[i] = vec_sum;
        }
        return new_camera_orn_per_frame;
    }
}
