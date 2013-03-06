package IO.Joints;

import java.util.Hashtable;

public class JointSticks {

    private Hashtable<String, JointStick> sticks_per_name = null;

    public JointSticks() {
        sticks_per_name = new Hashtable<String, JointStick>();
    }

    public void addJointStick(String name, double [] start_point, double [] end_point) {
        JointStick js = new JointStick(start_point, end_point);
        sticks_per_name.put(name, js);
    }
    public JointStick getJointStick(String name) {
        return sticks_per_name.get(name);
    }
}
