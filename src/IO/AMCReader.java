package IO;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import main.ControlVariables;

import IO.AMCData.AMCScene;
import IO.AMCData.Frame;
import IO.ASFData.BoneData;
import IO.ASFData.SkeletonNode;
import IO.Joints.JointSticks;
import Tools.MatrixStack;
import Tools.VectorTools;

/**Class for reading AMC data**/
public class AMCReader extends AReader implements IReader {

    /**Scale for the motion data read**/
    private float scale = ControlVariables.scale;

    /**Data specific to the AMC file**/
    private String ang_type = "";
    private double mass = 0.0f;
    private double length = 0.0f;

    private String rot_order = "XYZ";
    private String [] mov_order = null;

    private double [] root_pos = null;
    private double [] root_orn = null;

    private Hashtable<String, BoneData> name_bone_data=
            new Hashtable<String, BoneData>();
    private Hashtable<String, BoneData> id_bone_data=
            new Hashtable<String, BoneData>();

    /**Sticks attached to various joints to keep track
     * of their orientation.  Store for each frame.**/
    private JointSticks [] joint_sticks_per_frame = null;

    /**Rotation Data**/
    private AMCScene rotation_frames = null;

    /**Skeleton Data**/
    private SkeletonNode main_root = null;
    private SkeletonNode [] root_per_frame = null;
    private double [][] main_matrix = null;

    /**Flag**/
    private boolean asf_file_data_flag = false;
    /**Flag**/
    private boolean bone_data_read_flag = false;
    /**Flag**/
    private boolean amc_file_data_flag = false;


    private String [][] rot_all = new String [][] {
            new String[]{"X","Y","Z"},
            //new String[]{"Z","Y","X"},
    };
    private String first_rot = "X";
    private String second_rot = "Y";
    private String third_rot = "Z";


    /**Test runner**/
    public static void main(String [] args) {
        IReader iR = new AMCReader("models_amc\\A_pulls_B_by_the_elbow_B_resists");
        iR.readData();
    }

    /**Class constructor**/
    public AMCReader(String file_name) {
        super(file_name);
    }




    /**Read in the AMC data**/
    public void readData() {

        /**Read the rotation data**/
        rotation_frames = new AMCScene();
        readAMCData();


        /**Read the skeleton data**/
        readASFData();


        /**-------------------------------------------------------------------**/
        /**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
        /**-------------------------------------------------------------------**/
        /**Generate the original points for frame zero from the skeleton data**/
        super.data_per_frame = new float[rotation_frames.getTotalFrames()*rot_all.length][][][];
        root_per_frame = new SkeletonNode[rotation_frames.getTotalFrames()];

        /**Given the original skeleton, generate the points at each node**/
        generateOriginalSkeletonPoints(main_root);
        /**Adjust the points in the skeleton, given
         *  the axis for rotation for each point**/
        //rotateOriginalPoints();

        /**-------------------------------------------------------------------**/
        /**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
        /**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
        /**-------------------------------------------------------------------**/
        for(int r = 0; r < rot_all.length; r++) {

            String [] r_rotations = rot_all[r];
            this.first_rot = r_rotations[0];
            this.second_rot = r_rotations[1];
            this.third_rot = r_rotations[2];

            joint_sticks_per_frame = new JointSticks [rotation_frames.getTotalFrames()];
            for(int frame_i = 0; frame_i < rotation_frames.getTotalFrames(); frame_i++) {

                JointSticks joint_sticks = new JointSticks();
                joint_sticks_per_frame[frame_i] = joint_sticks;

                /**Rotate the points in the skeleton based on rotation data for frame i**/
                rotatePoints(rotation_frames.getFrame(frame_i), joint_sticks, frame_i);
                /**Create the lines to  be rendered from the rotate points**/
                generateRenderLines((r*rotation_frames.getTotalFrames())+frame_i);

                /**Reset to the original points**/
                generateOriginalSkeletonPoints(main_root);
            }
        }
        /**-------------------------------------------------------------------**/
        /**| | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |**/
        /**-------------------------------------------------------------------**/
    }

    /**Helper to reading main data for ASF**/
    private void readASFData() {

        /**==========================================================================**/
        /**Read the ASF data                                                         **/
        /**==========================================================================**/
        super.openFile(file_location+"\\a.asf");
        try {
            String next_line = "";
            while((next_line = super.br.readLine()) != null) {

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the version==============================**/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":version")) {
                    if(asf_file_data_flag)System.out.println();

                    if(asf_file_data_flag)System.out.println("\tVersion: "+next_line.split(":version ")[1]);
                    next_line = super.br.readLine();
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the name=================================**/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":name")) {
                    if(asf_file_data_flag)System.out.println();

                    if(asf_file_data_flag)System.out.println("\tName: "+next_line.split(":name ")[1]);
                    next_line = super.br.readLine();
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the units================================**/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":units")) {
                    if(asf_file_data_flag)System.out.println();

                    next_line = super.br.readLine();
                    mass = Float.parseFloat(next_line.split("mass")[1]);

                    next_line = super.br.readLine();
                    length = Float.parseFloat(next_line.split("length")[1]);

                    next_line = super.br.readLine();
                    ang_type = next_line.split("angle ")[1];

                    if(asf_file_data_flag)System.out.println("\tAngle: " + ang_type);
                    if(asf_file_data_flag)System.out.println("\tMass: " + mass);
                    if(asf_file_data_flag)System.out.println("\tLength: " + length);
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the root=================================**/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":root")) {
                    if(asf_file_data_flag)System.out.println();

                    next_line = super.br.readLine();
                    String temp = next_line.split("order ")[1].trim();
                    mov_order = temp.split(" ");
                    if(
                            !mov_order[0].equals("TX") ||
                            !mov_order[1].equals("TY") ||
                            !mov_order[2].equals("TZ") ||
                            !mov_order[3].equals("RX") ||
                            !mov_order[4].equals("RY") ||
                            !mov_order[5].equals("RZ")) {
                        System.out.println("Invalid Mov Order!");
                        System.exit(1);
                    }
                    if(asf_file_data_flag)System.out.print("\tMov Order: ");
                    for(int i =0; i < mov_order.length; i++) {
                        if(asf_file_data_flag)System.out.print(mov_order[i]+" ");
                    }
                    if(asf_file_data_flag)System.out.println();

                    next_line = super.br.readLine();
                    rot_order = next_line.split("axis ")[1].trim();
                    if(asf_file_data_flag)System.out.println("\tRot Order: " + rot_order);
                    if(
                            !rot_order.equals("XYZ")) {
                        System.out.println("Invalid Rot Order!");
                        System.exit(1);
                    }

                    next_line = super.br.readLine();
                    String position_string = next_line.split("position")[1].trim();
                    String []position_split = position_string.split(" ");
                    root_pos = new double[] {
                            Double.parseDouble(position_split[0]),
                            Double.parseDouble(position_split[1]),
                            Double.parseDouble(position_split[2])
                    };
                    if(asf_file_data_flag)System.out.println("\tRoot position: "+
                            root_pos[0] + " " +root_pos[1] + " " +root_pos[2]);


                    next_line = super.br.readLine();
                    String orientation_string = next_line.split("orientation")[1].trim();
                    String []orientation_split = orientation_string.split(" ");
                    root_orn = new double[] {
                            Double.parseDouble(orientation_split[0]),
                            Double.parseDouble(orientation_split[1]),
                            Double.parseDouble(orientation_split[2])
                    };
                    if(asf_file_data_flag)System.out.println("\tRoot orientation: "+
                            root_orn[0] + " " +root_orn[1] + " " +root_orn[2]);
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the bone data============================**/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":bonedata")) {
                    if(asf_file_data_flag)System.out.println();
                    next_line = readBoneData(next_line);
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/

                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                /**Read the hierarchy                            **/
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
                if(next_line.startsWith(":hierarchy")) {
                    if(asf_file_data_flag)System.out.println();
                    next_line = readHierarchyData(next_line);
                }
                /**||||||||||||||||||||||||||||||||||||||||||||||**/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.closeFile();
        /**==========================================================================**/
    }
    private String readBoneData(String next_line) throws IOException {

        while(!next_line.startsWith(":hierarchy")) {
            next_line = super.br.readLine();


            /**************BEGIN****************/
            if(next_line.contains("begin")) {

                BoneData temp_bone_data = new BoneData();
                if(bone_data_read_flag)System.out.println("\tBone data");

                while(!next_line.contains("end")) {

                    next_line = super.br.readLine();
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    if(next_line.contains("id")) {
                        byte id = Byte.parseByte(next_line.split("id")[1].trim());
                        temp_bone_data.setId(id);
                        if(bone_data_read_flag)System.out.println("\t\tId: "+id);

                        id_bone_data.put(""+id, temp_bone_data);
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    if(next_line.contains("name")) {
                        String name = next_line.split("name")[1].trim();
                        temp_bone_data.setName(name);
                        if(bone_data_read_flag)System.out.println("\t\tName: "+name);

                        name_bone_data.put(""+name, temp_bone_data);
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    if(next_line.contains("direction")) {
                        String temp = next_line.split("direction")[1].trim();
                        String [] temp_split = temp.split(" ");
                        double [] temp_dir = new double[]{
                                Double.parseDouble(temp_split[0]),
                                Double.parseDouble(temp_split[1]),
                                Double.parseDouble(temp_split[2])};

                        temp_bone_data.setDirection(temp_dir);
                        if(bone_data_read_flag)System.out.println("\t\tDirection: "+temp_dir[0]+" "+temp_dir[0]+" "+temp_dir[2]);
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    if(next_line.contains("length")) {
                        float length = Float.parseFloat(next_line.split("length")[1].trim());

                        temp_bone_data.setLength(length);
                        if(bone_data_read_flag)System.out.println("\t\tLength: "+length);
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    if(next_line.contains("axis")) {
                        String temp = next_line.split("axis")[1].trim();
                        String [] temp_split = temp.split("([ ]+)");
                        double [] axis = new double[] {
                                Double.parseDouble(temp_split[0].trim()),
                                Double.parseDouble(temp_split[1].trim()),
                                Double.parseDouble(temp_split[2].trim()),
                        };
                        String axis_order = temp_split[3];

                        temp_bone_data.setAxis(axis);
                        if(!axis_order.equals("XYZ")) {
                            System.out.println("Invalid axis order: "+axis_order);
                            System.exit(1);
                        }
                        temp_bone_data.setAxisOrder(axis_order);
                        if(bone_data_read_flag)System.out.println("\t\tAxis: "+axis[0]+" "+axis[1]+" "+axis[2]);
                        if(bone_data_read_flag)System.out.println("\t\tAxis Order: "+axis_order);
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    int num_dof = 0;
                    if(next_line.contains("dof")) {
                        String temp = next_line.split("dof")[1].trim();
                        String [] temp_split = temp.split("([ ]+)");
                        boolean [] dof_bool = new boolean[]{false, false, false};
                        for(int i = 0; i < temp_split.length; i++) {
                            if(temp_split[i].trim().equals("rx")) {
                                dof_bool[0]=true;
                                num_dof++;
                            }
                            if(temp_split[i].trim().equals("ry")) {
                                dof_bool[1]=true;
                                num_dof++;
                            }
                            if(temp_split[i].trim().equals("rz")) {
                                dof_bool[2]=true;
                                num_dof++;
                            }
                        }
                        temp_bone_data.setDofx(dof_bool[0]);
                        temp_bone_data.setDofy(dof_bool[1]);
                        temp_bone_data.setDofz(dof_bool[2]);
                        if(bone_data_read_flag)System.out.println("\t\tDOF: "+dof_bool[0]+" "+dof_bool[1]+" "+dof_bool[2]);

                        //Limits......
                        double [][] limits = new double[][]{
                                new double[]{0.0,0.0},
                                new double[]{0.0,0.0},
                                new double[]{0.0,0.0}
                        };
                        int num_lims = 0;
                        for(int i = 0; i < dof_bool.length; i++) {
                            if(dof_bool[i]==true) {
                                next_line = super.br.readLine();
                                int brac_start = next_line.indexOf("(");
                                int brac_end = next_line.indexOf(")");
                                String temp_limit = next_line.substring(brac_start+1, brac_end);
                                String [] temp_limit_split = temp_limit.split(" ");
                                limits[i] = new double[]{
                                        Double.parseDouble(temp_limit_split[0]),
                                        Double.parseDouble(temp_limit_split[1])
                                };
                                num_lims++;
                            }
                        }
                        if(num_dof != num_lims) {
                            System.out.println("Missing limits");
                            System.exit(1);
                        }
                        if(bone_data_read_flag)System.out.println("\t\t"+limits[0][0]+" "+limits[0][1]);
                        if(bone_data_read_flag)System.out.println("\t\t"+limits[1][0]+" "+limits[1][1]);
                        if(bone_data_read_flag)System.out.println("\t\t"+limits[2][0]+" "+limits[2][1]);

                        if(dof_bool[0]) {
                            temp_bone_data.setLimit("x", limits[0]);
                        }
                        if(dof_bool[1]) {
                            temp_bone_data.setLimit("y", limits[1]);
                        }
                        if(dof_bool[2]) {
                            temp_bone_data.setLimit("z", limits[2]);
                        }
                    }
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                    /**|||||||||||||||||||||||**/
                }
            }
            /***************END*****************/
        }

        return next_line;
    }
    private String readHierarchyData(String next_line) throws IOException {

        /**Hashtable storing the skeleton nodes by name**/
        Hashtable<String, SkeletonNode> skeleton_nodes =
                new Hashtable<String, SkeletonNode>();

        /**Begin tag**/
        next_line = super.br.readLine();
        if(next_line.contains("begin")) {
            next_line = super.br.readLine();
            while(!next_line.contains("end")) {

                /**Read the next node association**/
                String [] temp_split = next_line.trim().split(" ");


                /**Get the parent node and add it to hashtable if it does not yet exist**/
                String parent_node_name = temp_split[0];
                if(!skeleton_nodes.containsKey(parent_node_name)) {
                    SkeletonNode skeleton_node =
                            new SkeletonNode(parent_node_name);
                    skeleton_nodes.put(parent_node_name, skeleton_node);
                }
                SkeletonNode parent_node = skeleton_nodes.get(parent_node_name);


                /**Add the children**/
                for(int i = 1; i < temp_split.length; i++) {
                    String child_name = temp_split[i];
                    SkeletonNode child_node = new SkeletonNode(child_name);

                    skeleton_nodes.put(child_name, child_node);
                    parent_node.addChild(child_node);

                    child_node.setParent(parent_node);
                }


                /**Iterate to the next line**/
                next_line = super.br.readLine();
            }
        }


        /**Get the root node**/
        main_root = skeleton_nodes.get("root");
        /**Combine the skeleton with the bone info**/
        addSkeletonBoneData(main_root, "");

        return next_line;
    }
    public void addSkeletonBoneData(SkeletonNode root_node, String tab) {

        BoneData root_data = name_bone_data.get(root_node.getName());
        if(root_data != null) {
            root_node.setBoneData(root_data);
            //System.out.println(tab+root_data.getId());
        } else {
            //System.out.println(""+0);
        }

        SkeletonNode[]children = root_node.getChildren();
        for(int i = 0;i < children.length; i++) {
            addSkeletonBoneData(children[i], tab+"\t");
        }
    }


    /**Helper to reading main data for AMC**/
    private void readAMCData() {
        /**==========================================================================**/
        /**Read the AMC data                                                         **/
        /**==========================================================================**/
        super.openFile(file_location+"\\a.amc");
        try {
            String next_line = super.br.readLine();
            while(next_line!= null) {


                /**------------------------------------------------------**/
                /**------------------------------------------------------**/
                /**Case where a new frame number is parsed**/
                if(next_line.matches("(^)([ ])*[0-9][0-9]*([ ])*($)")) {


                    /**--------------------------------------------**/
                    /**--------------------------------------------**/
                    /**Create and store the next frame**/
                    Frame new_frame = new Frame();
                    new_frame.setFrameNumber(Integer.parseInt(next_line));
                    rotation_frames.addFrame(new_frame);
                    if(amc_file_data_flag) System.out.println(next_line);
                    /**--------------------------------------------**/
                    /**--------------------------------------------**/
                    /**Get the root position and orientation**/
                    next_line = super.br.readLine();
                    if(next_line.contains("root")) {
                        String [] temp_split = next_line.split("([ ])+");

                        double [] temp_pos = new double[]{
                                Double.parseDouble(temp_split[1]),
                                Double.parseDouble(temp_split[2]),
                                Double.parseDouble(temp_split[3]),
                        };
                        double [] temp_orn = new double[]{
                                Double.parseDouble(temp_split[4]),
                                Double.parseDouble(temp_split[5]),
                                Double.parseDouble(temp_split[6]),
                        };

                        if(amc_file_data_flag)System.out.println(
                                "\tRoot POS: "+temp_pos[0]+" "+temp_pos[1]+" "+temp_pos[2]);
                        if(amc_file_data_flag)System.out.println(
                                "\tRoot ORN: "+temp_orn[0]+" "+temp_orn[1]+" "+temp_orn[2]);

                        new_frame.setRootOrientation(temp_orn);
                        new_frame.setRootPosition(temp_pos);
                    }
                    /**--------------------------------------------**/
                    /**--------------------------------------------**/
                    /**Read the rotation data for each segment**/
                    next_line = super.br.readLine();
                    while(
                            (next_line != null) &&
                            (!next_line.matches("(^)([ ])*[0-9][0-9]*([ ])*($)"))) {


                        /**Get the segment name and rotation data for that segment**/
                        String [] temp_split = next_line.split("([ ])+");
                        String segment_name = temp_split[0];

                        int [] negation = new int []{1, 1, 1};
                        if(
                                segment_name.equals("lradius") ||
                                segment_name.equals("rradius") ||
                                segment_name.equals("lhumerus") ||
                                segment_name.equals("rhumerus") ||
                                segment_name.equals("lclavicle") ||
                                segment_name.equals("rclavicle")
                                ) {
                            negation = new int []{-1, 1, -1};
                        }

                        double [] rotation_data = new double[temp_split.length-1];
                        for(int i = rotation_data.length-1; i >=0; i--) {
                            rotation_data[i] = negation[i]*Double.parseDouble(temp_split[i+1]);
                        }
                        if(amc_file_data_flag)System.out.println("\tSegment: "+segment_name);
                        if(amc_file_data_flag)System.out.print("\tSegment Rotation: ");
                        for(int i = 0; i < rotation_data.length; i++) {
                            if(amc_file_data_flag)System.out.print(rotation_data[i]+" ");
                        }
                        if(amc_file_data_flag)System.out.println();


                        /**Store the rotation data for the segment**/
                        new_frame.addSegmentRotation(segment_name, rotation_data);


                        next_line = super.br.readLine();
                    }
                    /**--------------------------------------------**/
                    /**--------------------------------------------**/

                } else {
                    next_line = super.br.readLine();
                }
                /**------------------------------------------------------**/
                /**------------------------------------------------------**/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.closeFile();
        /**==========================================================================**/
    }


    /**Generate  points for the skeleton,  given relative
     * point of parent and any existing transformation**/
    private void generateOriginalSkeletonPoints(SkeletonNode root) {

        SkeletonNode parent = root.getParent();
        if(parent==null) {
            root.setPoint(this.root_pos);
        } else {

            double [] parent_point = parent.getPoint();

            BoneData root_data = root.getBoneData();
            double [] root_direction = root_data.getDirection();
            double root_length = root_data.getLength()*scale;

            double [] root_point =
                    VectorTools.getVector(parent_point, root_direction, root_length);

            root.setPoint(root_point);
        }

        SkeletonNode[] children = root.getChildren();
        for(int i = 0; i < children.length; i++) {
            generateOriginalSkeletonPoints(
                    children[i]);
        }
    }
    /**Given points in the skeleton, generate a set of lines**/
    private void generateRenderLines(int frame) {
        Vector<float[][]> lines_vec =
                new Vector<float[][]>();
                generateRenderLinesHelper(lines_vec, main_root);

                super.data_per_frame[frame] = new float[lines_vec.size()][2][3];
                lines_vec.toArray(super.data_per_frame[frame]);
    }
    /**Helper function for locating lines**/
    private void generateRenderLinesHelper(
            Vector<float[][]> lines_vec, SkeletonNode root) {

        SkeletonNode [] children = root.getChildren();
        for(int i = 0; i < children.length; i++) {
            lines_vec.add(new float[][]{
                    new float[]{
                            (float) root.getPoint()[0],
                            (float) root.getPoint()[1],
                            (float) root.getPoint()[2]},
                            new float[]{
                            (float) children[i].getPoint()[0],
                            (float) children[i].getPoint()[1],
                            (float) children[i].getPoint()[2]}
            });

            //System.out.println(root.getPoint()[0] + " "+root.getPoint()[1] + " "+root.getPoint()[2]);
            //System.out.println(children[i].getPoint()[0]+ " "+children[i].getPoint()[1]+ " "+children[i].getPoint()[2]);

            generateRenderLinesHelper(lines_vec, children[i]);
        }
    }
    /**Adjust the points in the skeleton given the
     * rotation angles at each node**/
    @SuppressWarnings("unused")
    private void rotateOriginalPoints() {

        MatrixStack m_stack = new MatrixStack();

        SkeletonNode [] children = main_root.getChildren();
        for(int i = 0; i < children.length; i++) {
            rotateOriginalPointsHelper(main_root.getPoint(), children[i], m_stack);
        }
    }
    private void rotateOriginalPointsHelper(
            double [] prev_parent_point,
            SkeletonNode root,
            MatrixStack m_stack) {

        /**Get the parent to root vector**/
        double [] last_root_point = root.getPoint();
        double [] cur_root_point = new double []{
                last_root_point[0],
                last_root_point[1],
                last_root_point[2]};
        double [] parent_to_root_vector = VectorTools.sub(cur_root_point, prev_parent_point);
        if(
                Math.abs(
                        (VectorTools.mag(parent_to_root_vector) - root.getBoneData().getLength()*scale)/
                        VectorTools.mag(parent_to_root_vector)
                        ) > 0.0001

                ) {

            System.out.println("Error: length has changed");
            System.out.println(VectorTools.mag(parent_to_root_vector));
            System.out.println(root.getBoneData().getLength()*scale);
            System.exit(1);
        }


        /**Get the rotation matrixes**/
        BoneData rootBoneData = root.getBoneData();
        double [] axis = rootBoneData.getAxis();
        m_stack.pushMatrix(VectorTools.gen_rot_X(axis[0]/180.0*Math.PI));
        m_stack.pushMatrix(VectorTools.gen_rot_Y(axis[1]/180.0*Math.PI));
        m_stack.pushMatrix(VectorTools.gen_rot_Z(axis[2]/180.0*Math.PI));

        /**Get the current parent point.**/
        double [] cur_parent_point = root.getParent().getPoint();

        /**Rotate the point at the node**/
        double [] rotated_vector = VectorTools.mult(m_stack.getTop(), parent_to_root_vector);
        double [] new_root_point = VectorTools.add(rotated_vector, cur_parent_point);
        root.setPoint(new_root_point);
        if(
                Math.abs(
                        (VectorTools.mag(rotated_vector) - root.getBoneData().getLength()*scale)/
                        VectorTools.mag(rotated_vector)
                        ) > 0.0001

                ) {

            System.out.println("Error: length has changed");
            System.out.println(VectorTools.mag(rotated_vector));
            System.out.println(root.getBoneData().getLength()*scale);
            System.exit(1);
        }

        /**Update the children**/
        SkeletonNode[]children=root.getChildren();
        for(int i =0; i < children.length; i++) {
            rotateOriginalPointsHelper(last_root_point, children[i], m_stack);
        }


        m_stack.popMatrix();
        m_stack.popMatrix();
        m_stack.popMatrix();
    }
    /**Adjust the points in the skeleton given
     * the rotation angles in the frame data**/
    private void rotatePoints(Frame frame, JointSticks joint_sticks, int frame_number) {

        MatrixStack m_stack = new MatrixStack();

        double [] root_pos = frame.getRootPosition();
        double [] root_orn = frame.getRootOrientation();
        root_pos = VectorTools.mult(1.0f*scale, root_pos);


        /**Set up the matrixes for the root orientation**/
        doRotation(m_stack, root_orn, 0, 0);
        doRotation(m_stack, root_orn, 1, 1);
        doRotation(m_stack, root_orn, 2, 2);
        main_matrix = m_stack.getTop();

        /**Remove the matrixes used for the root orientation**/
        m_stack.popMatrix();
        m_stack.popMatrix();
        m_stack.popMatrix();

        /**Store the old root position and set up the new one**/
        double[] last_root_point = new double[]{
                main_root.getPoint()[0],
                main_root.getPoint()[1],
                main_root.getPoint()[2]
        };
        main_root.setPoint(root_pos);

        SkeletonNode [] children = main_root.getChildren();
        for(int i = 0; i < children.length; i++) {
            rotatePointsHelper(last_root_point, children[i], frame, m_stack, joint_sticks);
        }
        root_per_frame[frame_number] = main_root.clone();
    }
    private void rotatePointsHelper(
            double [] prev_parent_point,
            SkeletonNode root,
            Frame frame,
            MatrixStack m_stack,
            JointSticks joint_sticks) {

        /**Get the rotation data for this root**/
        String segment_name = root.getName();
        double [] rotation_data = frame.getSegmentRotation(segment_name);

        /**Get the DOF data for this root**/
        BoneData root_data = root.getBoneData();

        /**Get the parent to root vector using the previous parent
         * point (not the current), provided in the arguments**/
        double [] last_root_point = root.getPoint();
        double [] cur_root_point = new double []{
                last_root_point[0],
                last_root_point[1],
                last_root_point[2]};


        double [] new_root_point = generateTransformedPoint(
                root,
                cur_root_point,
                prev_parent_point,
                rotation_data,
                segment_name,
                m_stack);
        root.setPoint(new_root_point);


        /**Create a joint stick for this joint name**/
        double [] joint_stick_point = new double []{0,0,1};
        double [] stick_to_original_root = VectorTools.sub(joint_stick_point, last_root_point);
        double [] rotated_stick = VectorTools.mult(m_stack.getTop(), stick_to_original_root);
        double [] completed_stick = VectorTools.add(rotated_stick,new_root_point);
        joint_sticks.addJointStick(segment_name, new_root_point, completed_stick);

        /**Update the children**/
        SkeletonNode[]children=root.getChildren();
        for(int i =0; i < children.length; i++) {
            rotatePointsHelper(last_root_point, children[i], frame, m_stack, joint_sticks);
        }


        /**Remove the matrixes from the stack**/
        if(root_data.getDofx()) {
            m_stack.popMatrix();
        }
        if(root_data.getDofy()) {
            m_stack.popMatrix();
        }
        if(root_data.getDofz()) {
            m_stack.popMatrix();
        }
    }

    private void doRotation(
            MatrixStack m_stack,
            double [] angles,
            int num_type,
            int rotation_number) {

        String rot_type = "";
        if(num_type==0) {
            rot_type=first_rot;
        }
        else if(num_type==1) {
            rot_type=second_rot;
        }
        else {
            rot_type=third_rot;
        }

        if(rot_type.equals("X")) {
            m_stack.pushMatrix(VectorTools.gen_rot_X(angles[rotation_number]/180.0f*Math.PI));
        } else if(rot_type.equals("Y")) {
            m_stack.pushMatrix(VectorTools.gen_rot_Y(angles[rotation_number]/180.0f*Math.PI));
        } else {
            m_stack.pushMatrix(VectorTools.gen_rot_Z(angles[rotation_number]/180.0f*Math.PI));
        }
    }
    /**Transform a current root point, given its parent root point,
     * transformation matrix stack, rotation data, and segment name**/
    public double [] generateTransformedPoint(
            SkeletonNode root,
            double [] cur_root_point,
            double [] prev_parent_point,
            double [] rotation_data,
            String segment_name,
            MatrixStack m_stack) {

        BoneData root_data = root.getBoneData();


        double [] parent_to_root_vector = VectorTools.sub(cur_root_point, prev_parent_point);
        if(
                Math.abs(
                        (VectorTools.mag(parent_to_root_vector) - root.getBoneData().getLength()*scale)/
                        VectorTools.mag(parent_to_root_vector)
                        ) > 0.0001

                ) {

            System.out.println("Error: length has changed");
            System.out.println(VectorTools.mag(parent_to_root_vector));
            System.out.println(root.getBoneData().getLength()*scale);
            System.exit(1);
        }

        /**Get the rotation matrixes given the rotation and DOF data**/
        int dof_counter = 0;
        if(root_data.getDofx()) {
            doRotation(m_stack, rotation_data, 0, dof_counter);
            dof_counter++;
        }
        if(root_data.getDofy()) {
            doRotation(m_stack, rotation_data, 1, dof_counter);
            dof_counter++;
        }
        if(root_data.getDofz()) {
            doRotation(m_stack, rotation_data, 2, dof_counter);
            dof_counter++;
        }

        if(dof_counter == 0 || dof_counter != rotation_data.length) {
            if(!segment_name.equals("lhipjoint")&&!segment_name.equals("rhipjoint")) {
                System.out.println("Error: Invalid rotation data: "+segment_name);
                System.exit(1);
            }
        }


        /**Get the current parent node point**/
        double [] cur_parent_point = root.getParent().getPoint();


        /**Rotate the root point around the current parent node**/
        double [] rotated_vector = VectorTools.mult(main_matrix , VectorTools.mult(m_stack.getTop(), parent_to_root_vector));

        double [] new_root_point = VectorTools.add(rotated_vector,cur_parent_point);

        if(
                Math.abs(
                        (VectorTools.mag(rotated_vector) - root.getBoneData().getLength()*scale)/
                        VectorTools.mag(rotated_vector)
                        ) > 0.0001

                ) {

            System.out.println("Error: length has changed");
            System.out.println(VectorTools.mag(rotated_vector));
            System.out.println(root.getBoneData().getLength()*scale);
            System.exit(1);
        }

        return new_root_point;
    }


    /**Return the joint direction indicators for each joint in each frame**/
    public JointSticks [] getJointSticksPerFrame() {
        return this.joint_sticks_per_frame;
    }
    /**Return the skeleton root node for each frame**/
    public SkeletonNode [] getSkeletonRoot() {
    	return this.root_per_frame;
    }
}
