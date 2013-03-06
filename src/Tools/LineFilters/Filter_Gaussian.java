package Tools.LineFilters;



public class Filter_Gaussian implements Filter{

	public float f(float x) {
		
		double exp = Math.pow(Math.E, -Math.pow(x, 2)/(2*Math.pow(1, 2))  );
		double denom_comp = 1/(Math.sqrt(Math.PI*2));
		
		return (float) (denom_comp*exp);
	}
}
