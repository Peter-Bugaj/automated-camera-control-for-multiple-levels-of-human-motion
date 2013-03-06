package Tools.LineFilters;

import main.ControlVariables;

public class AFilter {

	public static Filter getFilter() {
		if(ControlVariables.filter_types_rot[ControlVariables.filter_type_rot].equals("constant")) {
			System.out.println("Filter type: constant");
			return new Filter_Constant(ControlVariables.filter_constant_rot);

		} else if(ControlVariables.filter_types_rot[ControlVariables.filter_type_rot].equals("gaussian")) {
			System.out.println("Filter type: gaussian");
			return new Filter_Gaussian();

		} else {
			System.out.println("Filter type: gaussian");
			return new Filter_Gaussian();

		}
	}
}
