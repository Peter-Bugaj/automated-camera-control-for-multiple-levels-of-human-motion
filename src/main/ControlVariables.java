/**
 *
 */
package main;

/**
 * @author Piotr Bugaj
 *
 */
public class ControlVariables {

    /**----------------------------------------------------------------**/
    /**                        SCENE VARIABLES                         **/
    /**----------------------------------------------------------------**/
    /**Animation number to be played**/
	private final static int []animations = new int[]{
		26, 25, 24,
		23, 20, 18, 
		17, 15, 14, 
		8,  7,  6,
		5,  4,  1};
    protected final static int animationNumber = animations[5];


    /**Variables for controlling the movement of the mesh**/
    protected static float xMovementScroller = 7;
    protected static float yMovementScroller = -2;
    protected static float zMovementScroller = -11;

    /**Variables for controlling the rotation of the mesh**/
    protected static float xRotationScroller = 0;
    protected static float yRotationScroller = 90;
    protected static float zRotationScroller = 0;

    protected static final boolean auto_control = true;
    
    /**Scale for the motion data read**/
    public final static float scale = 1f;
    /**Scale for the background data rendered**/
    public final static float backgroundScale = 10.0f;

    public final static float distanceFromCharacter = 55.0f;

    /**Variable for controlling the speed of animation**/
    protected static float speedScroller = 19;
    protected static float maxSpeedScrollerVal = 20;
    
    protected static int speedIntervalCounter = 0;
        /**Variables for controlling the colour of
     * motion effects  and the mesh  itself**/
    protected static String meshColour = "DARK BROWN";

    /**Variable for controlling whether the mesh is visible or not**/
    protected static String meshVisibility = "ON";


    /**----------------------------------------------------------------**/
    /**                   ZOOM VALUE RELATED VARIABLES                 **/
    /**----------------------------------------------------------------**/
    /**Factor for controlling the default zoom value**/
    public final static float zoom_factor = 1.5f;
    /**Number of stand deviations used for
     * detecting significant acceleration**/
    public final static float zoom_devs = 1.5f;
    /**Filter size for smoothing accelertion values**/
    public final static int filter_spread_acc = 35;
    /**Time for the object taken to get back in the center of the
     * frame with respect to the time it took to zoom out**/
    public final static float fLf2L_fac = 1.0f;
    /**Time it takes to zoom in with respect
     * to the time it took to zoom out**/
    public final static float f2Lfk_fac = 1.5f;


    /**----------------------------------------------------------------**/
    /**                THIRD LEVEL MOTION RELATED VARIABLES            **/
    /**----------------------------------------------------------------**/
    /**How many times bigger the main  skeleton
     * should at least be from the sub-skeleton
     * viewed  in  their  level  sub - cenes**/
    public final static double skeleton_sub_size_factor = 2.0;
    /**How significant motion at nodes should be in order
     * to be considered a significant third level motion.
     * Significance is determined in terms of number of
     * standard deviations from all significant motion.**/
    public final static double num_third_level_motion_val_stds = 2.0;
    /**How many times a frame can be compared before offset
     * for finding a group is no longer increased**/
	public final static int tl_compare_tolerance = 25;
    /**How much a motion value difference can be before
     * it can no longer belong to a frame group**/
	public final static double frame_group_dif_fac = 2;
	/**Max change in angle between third level motion displacement**/
	public final static double max_ang_third_level_motion = 90;
	/**Weight variable used for adding significance to
	 * third level motion given its motion value**/
	public final static double third_mot_val_weight_var = 2.5;
	/**The proposed minimum length of third level sub-scenes**/
	public final static int tlms_length = 1;
	/**The  width of the  segments present in  third  level
	 * sub-scenes with respect to the width of the frame**/
	public final static double segment_witdh_factor = 1.0/10;
	/**How much of an average overlap factor is
	 * allow in a  third level motion  scene**/
	public final static double overlap_thres = 0.2;
	/**Size   of  the   weight  filter   used   when
	 * calculating the displacement for each node**/
	public static final int third_level_disp_filter_size = 50;
	/**Size of the weight filter used when calculating
	 * the   angular  displacement  for  each  node**/
	public static final int third_level_ang_filter_size = 100;	
	/**Size   of  the   weight  filter   used   when
	 * Number of times to apply the filter**/
	public static final int num_of_filters = 1;
	/**The minimum size left for creating third level motion scenes**/
	public static final int min_slack_size = 100;
	/**Number of standard deviations for stationary displacemnet threshold**/
	public static final float statDispThres = 1.5f;
	/**Minimum number  of frames before  another
	 * thrid scene is allowed to be displayed**/
	public static final float minInBetSubSnLen = 80;
	/**Filter  spread   for  calculating   camera
	 * centers for a third level motion scenes**/
	public static final int centSpreadTLMS = 15;
	/**Filter spread for calculating camera zoom
	 * levels for a third level motion scenes**/
	public static final int zoomSpreadTLMS = 25;	
	/**Filter spread for calculating camera orientations
	 * levels  for   a  third  level   motion  scenes**/
	public static final int ornSpreadTLMS = 35;
    /**Factor for controlling the default zoom value**/
    public final static float tl_zoom_factor = 2.5f;
	
/**TODO: Fix centroid. Should be closer positioned to the joint**/
/**TODO: add ground contact**/
    
    /**----------------------------------------------------------------**/
    /**                ROTATION MOTION RELATED VARIABLES               **/
    /**----------------------------------------------------------------**/
    /**Type of filter used for smoothing the rotation motion**/
    public final static int filter_type_rot = 1;
    public final static float filter_constant_rot = 1;
    public final static String [] filter_types_rot =
            new String[]{"constant", "gaussian"};

    /**Filter size for smoothing rotation motion**/
    public final static int filter_spread_rot = 80;

    /**The technique used for filtering data for rotation motion**/
    public final static int filtering_technique_rot = 1;
    public final static String [] filter_techniques_rot = new String[]{
        "PCA_weighted",
        "area_weighted",
        "adjusted_area_weighted"};

    /**How abrupt can the angle of the character
     * change before a scene has to be subdivided**/
    public final static float abrupt_angle_size_rot = 700.0f;

    /**How many frame should pass by before another subdivision
     * of a scene is allowed**/
    public final static int min_scene_length_rot = 200;

    /**The amount frames can overlap for sub frames dealing with rotation**/
    public final static int frame_overlap_rot = 0;


    /**----------------------------------------------------------------**/
    /**                     ORIENTATION VARIABLES                      **/
    /**----------------------------------------------------------------**/
    /**The name of the joint stick the camera should be oriented with**/
    public final static String joint_stick_name = "head";

    /**Which direction should the camera face the joint stick**/
    public final static int joint_stick_face_direction = 1;


    /**----------------------------------------------------------------**/
    /**                           TEST VARIABLES                       **/
    /**----------------------------------------------------------------**/
    /**Length of the PCA normals rendered for testing**/
    public final static int pcaNormalsLength = 7;
    /**----------------------------------------------------------------**/
}