package IO;

import IO.ASFData.SkeletonNode;
import IO.Joints.JointSticks;

public interface IReader {

    public void readData();

    public float [][][][] getData();

    public JointSticks [] getJointSticksPerFrame();

    public SkeletonNode [] getSkeletonRoot();
}
