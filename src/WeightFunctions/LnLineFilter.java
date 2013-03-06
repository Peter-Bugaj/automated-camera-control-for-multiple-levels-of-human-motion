/**
 *
 */
package WeightFunctions;

/**
 * @author Piotr Bugaj
 * @date June 25th, 2010
 */
public class LnLineFilter extends ALineWeightFilter implements ILineWeightFilter{

    public static void main(String[] args) {

    }

    public LnLineFilter() {
        params = new float[]{2.0f, 4.5f, -0.9f};
    }

    public void calculateLineFunction(int numPoints) {
        super.lineFunction = new float[numPoints];

        for(int i = 0 ;i < super.lineFunction.length; i++) {
            float x = i/numPoints-1;
            float y = (float) (Math.log((x*params[0]) + params[1]) + params[2]);

            super.lineFunction[i] = y;
        }
    }

}
