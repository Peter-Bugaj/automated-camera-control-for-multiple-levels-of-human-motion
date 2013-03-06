package IO.Joints;

public class JointStick {

    private double [] start_point = null;
    private double [] end_point = null;

    public JointStick(double [] start_point, double [] end_point) {
        this.start_point=start_point;
        this.end_point=end_point;
    }
    public double [] getStartPoint() {
        return start_point;
    }
    public double [] getEndPoint() {
        return end_point;
    }
}
