package IO.ASFData;

import java.util.Hashtable;

public class BoneData {

    /**===================================================**/
    private byte id = 0;
    private String name = "";

    private double [] direction = null;
    private double length = 0.0f;

    private double [] axis = null;
    private String axis_order = null;

    private boolean dof_x;
    private boolean dof_y;
    private boolean dof_z;

    private Hashtable<String, double []> limits =
            new  Hashtable<String, double []>();
    /**===================================================**/
    /**===================================================**/
    /**===================================================**/


    /**===================================================**/
    /**===================================================**/
    /**===================================================**/
    public void setId(byte id) {
        this.id=id;
    }
    public void setName(String name) {
        this.name=name;
    }
    public void setDirection(double [] direction) {
        this.direction=direction;
    }
    public void setLength(double length) {
        this.length=length;
    }
    public void setAxis(double[]axis){
        this.axis=axis;
    }
    public void setAxisOrder(String axis_order) {
        this.axis_order=axis_order;
    }
    public void setDofx(boolean dofx) {
        this.dof_x=dofx;
    }
    public void setDofy(boolean dofy) {
        this.dof_y=dofy;
    }
    public void setDofz(boolean dofz) {
        this.dof_z=dofz;
    }
    public void setLimit(String dof, double [] limit) {
        limits.put(dof, limit);
    }
    /**===================================================**/
    /**===================================================**/
    /**===================================================**/


    /**===================================================**/
    /**===================================================**/
    /**===================================================**/
    public byte getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public double [] getDirection() {
        return direction;
    }
    public double getLength() {
        return length;
    }
    public double [] getAxis() {
        return new double[]{ axis[0], axis[1],axis[2]};
    }
    public String getAxisOrder() {
        return axis_order;
    }
    public boolean getDofx() {
        return dof_x;
    }
    public boolean getDofy() {
        return dof_y;
    }
    public boolean getDofz() {
        return dof_z;
    }
    public double[] getLimits(String dof){
        return limits.get(dof);
    }
    /**===================================================**/
}