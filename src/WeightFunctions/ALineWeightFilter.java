/**
 * 
 */
package WeightFunctions;

/**
 * @author Piotr Bugaj
 *
 */
public abstract class ALineWeightFilter implements ILineWeightFilter {

	public static final String LN = "LN";
	public static final String POW = "POW";
	public static final String NONE = "NONE";
	
	protected float[] lineFunction;
	
	/**Parameters used with the functions being calculated**/
	protected float [] params;
	
	/**Reset the current parameters to the given ones**/
	public void setParams(float []params) {
		if(params != null) {
			for(int i = 0; i < this.params.length; i++) {
				this.params[i] = params[i];
			}
		}
	}
	
	/**Return the function values**/
	public float [] getLineFunction() {
		return lineFunction;
	}
	
}
