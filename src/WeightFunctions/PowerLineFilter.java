/**
 *
 */
package WeightFunctions;

/**
 * @author Piotr Bugaj
 * @date June 25th, 2010
 */
public class PowerLineFilter extends ALineWeightFilter implements ILineWeightFilter{

    public static void main(String[] args) {

    }

    /**Set default function parameters**/
    public PowerLineFilter() {
        params = new float[]{-1.0f, 1.0f, 1.0f};
    }

    public void calculateLineFunction(int numPoints) {
        super.lineFunction = new float[numPoints];

        for(int i = 0 ;i < super.lineFunction.length; i++) {
            float x = (i + 0.0f)/(numPoints - 1.0f);
            float y = (float) (Math.pow(params[0]*x, params[1]) + params[2]);

            super.lineFunction[i] = y;
        }
    }
}
