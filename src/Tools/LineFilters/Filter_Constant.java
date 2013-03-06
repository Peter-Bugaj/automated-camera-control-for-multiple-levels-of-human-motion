package Tools.LineFilters;



public class Filter_Constant implements Filter{

	private float c = 1.0f;
	
	public Filter_Constant(float c) {
		this.c=c;
	}
	
	public float f(float x) {
		return c;
	}
}
