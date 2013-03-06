package CameraMotion.ZoomControl;

import main.ControlVariables;

import Tools.LineFilters.AFilter;
import Tools.LineFilters.Filter;
import Tools.PCA.PCATools;

public class CameraZoomControl {

    /**Filter to use for the data curve**/
    private Filter filter = AFilter.getFilter();

    /**Number of neighbours to account from left and right, for the filter**/
    private int filter_spread = ControlVariables.filter_spread_acc;

    /**Number of standard deviations to use for the threshold**/
    private float threshold = ControlVariables.zoom_devs;

    /**Smoothen the list of acceleration values, using a filter**/
    public float [] smoothAccVals(float [] acc_vals) {

        float [] new_acc_vals = new float[acc_vals.length];

        for(int i = 0; i < acc_vals.length; i++) {

            float acc_sum = 0.0f;
            for(int j = 0; j <= filter_spread; j++) {
                if(i-j > -1) {

                    float  acc_i_m_j = acc_vals[i-j];
                    acc_i_m_j = filter.f(-j/filter_spread*2.5f)*acc_i_m_j;

                    acc_sum += acc_i_m_j;
                }
            }

            for(int j = 1; j <= filter_spread; j++) {
                if(i+j < acc_vals.length) {

                    float acc_i_m_j = acc_vals[i+j];

                    acc_i_m_j = filter.f(j/filter_spread*2.5f)*acc_i_m_j;

                    acc_sum += acc_i_m_j;
                }
            }

            int neg_dif = i - filter_spread;
            int pos_dif = i + filter_spread;
            if(neg_dif > 0) {
                neg_dif = 0;
            } else {
                neg_dif = Math.abs(neg_dif);
            }
            if(pos_dif < acc_vals.length) {
                pos_dif = 0;
            } else {
                pos_dif = pos_dif - acc_vals.length;
            }

            acc_sum *= 1.0f/( (filter_spread-pos_dif) + (filter_spread-neg_dif) +  1.0f);
            new_acc_vals[i] = acc_sum;
        }
        return new_acc_vals;
    }

    /**Find the significant acceleration values**/
    public float [] findSigAccVals(float [] acc_vals) {

        float std_dv= PCATools.getStdDev(acc_vals);
        float std_dv_threshold = std_dv*threshold;

        /**Vector marking which acceleration valuse are above a certain threshold**/
        float [] sig_acc_vals = new float [acc_vals.length];

        /**Detect the acceleration values above the threshold**/
        for(int i = 0; i < acc_vals.length; i++) {
            if(acc_vals[i] > std_dv_threshold) {
                sig_acc_vals[i] = 1;
            } else {
                sig_acc_vals[i] = 0;
            }
        }

        return sig_acc_vals;
    }
}
