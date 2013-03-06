/**
 * 
 */
package WeightFunctions;

/**
 * @author Piotr Bugaj
 *
 */
public interface ILineWeightFilter {

	
	public void calculateLineFunction(int numPoints);
	
	public void setParams(float [] params);
	
	public float[] getLineFunction();
}
