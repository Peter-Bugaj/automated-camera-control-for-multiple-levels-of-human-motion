package main;


import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLAutoDrawable;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import CameraMotion.CameraScene;
import CameraMotion.CameraScenes;

import IO.AMCReader;
import IO.IReader;

import IO.ASFData.SkeletonNode;
import IO.Joints.JointSticks;
import Tools.VectorTools;
import Tools.PCA.JacobiResult;
import Tools.PCA.PCATools;


/**
 * @author Piotr Bugaj
 * @date May 21, 2010
 */

public class Launcher implements GLEventListener, KeyListener {

    /**------------Main data needed to run the animation-------------------**/
    /**Physical location of the model**/
    private String modelLocation;


    /**The animation to read**/
    private int animationNumber = ControlVariables.animationNumber;

    /**The   variable  that  will  allow   for   the
     * alternation between different object files**/
    private int objectNumber = 0;

    /**The current camera scene controlling the camera**/
    private CameraScene current_scene = null;

    /**Motion capture data in the form of lines stored per frame**/
    private float [][][][] data_per_frame = null;

    /**The two most largest PCA components for
     * the motion capture data  per frame
     * FORM: p1, c, p2,
     * where c is the center point of the data,
     * p1 and p2 are the PCA vectors**/
    private float [][][] pcaPerFrame = null;

    /**The joint sticks existing for each frame**/
    private JointSticks [] joint_sticks_per_frame = null;

    /**The position of each camera, organized into scenes**/
    private CameraScenes cameraScenes = null;

    private float bS = ControlVariables.backgroundScale;

    /**GUI Interface**/
    private GUI gui = new GUI();

    /**GL toolkit**/
    private GL gl;
    /**--------------------------------------------------------------------**/

    /**--------------------------------------------------------------------**/
    /**                          DATA HELPER FUNCTIONS                     **/
    /**--------------------------------------------------------------------**/
    /**Given the line data, return the data as a matrix of distinct points
     * of size n by m, n being the number of points, m being the dimension**/
    private float[][] getPoints(float[][][]lines) {

        /**Iterate through the lines and collect all the points**/
        Hashtable<String, float[]> distinct_points =
                new Hashtable<String, float[]>();
        for(int i = 0; i < lines.length; i++) {

            float[][]line = lines[i];

            float[]p_a = new float[]{line[0][0], line[0][1], line[0][2]};
            float[]p_b = new float[]{line[1][0], line[1][1], line[1][2]};

            distinct_points.put(p_a[0]+" "+p_a[1]+" "+p_a[2], p_a);
            distinct_points.put(p_b[0]+" "+p_b[1]+" "+p_b[2], p_b);
        }

        /**Collect the points into a m by n vector,
         * where n is the number of points**/
        float [][] points = new float[distinct_points.size()][3];

        Enumeration<String> keys = distinct_points.keys();
        int value_counter = 0;
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            float [] key_value = distinct_points.get(key);
            points[value_counter] = key_value;
            value_counter++;
        }

        return points;
    }

    /**Get the PCA components per frame: for each frame vector is of the form:
     * p1, c, p2, where c is the center data point, p1 and p2 are the PCA
     * vectors**/
    private float[][][] getPCAPerFrame() {

        float [][][] pca_per_frame = new float[this.data_per_frame.length][3][3];

        for(int i = 0; i < this.data_per_frame.length; i++) {
            float [][][]lines = this.data_per_frame[i];

            /**Get the set of distinct point for these lines in
             * a n by m matrix (n being the number of points)**/
            float [][] distinct_points = getPoints(lines);

            /**Convert the points to a m by n matrix**/
            float [][] distinct_points_T = VectorTools.getTranspose(distinct_points);

            /**Get the average point for the skeleton points**/
            float [] points_x = distinct_points_T[0];
            float [] points_y = distinct_points_T[1];
            float [] points_z = distinct_points_T[2];
            float[] avg_point = new float[]{
                    PCATools.getMean(points_x),
                    PCATools.getMean(points_y),
                    PCATools.getMean(points_z)
            };

            /**Get the PCA vector for the set of skeleton points**/
            JacobiResult jr = PCATools.getPCAUsingJacobi(distinct_points_T);
            float [] pca_vector1 = jr.getVector_f(2);
            float [] pca_vector2 = jr.getVector_f(1);
            float [] pca_vector3 = jr.getVector_f(0);

            /**Get the PCA vector to draw a line**/
            float [] p1 = VectorTools.add(avg_point, pca_vector1);
            float [] p2 = VectorTools.add(avg_point, pca_vector2);
            float [] p3 = VectorTools.add(avg_point, pca_vector3);

            pca_per_frame[i] = new float[][]{
                    p1, avg_point, p2, p3
            };
        }


        return pca_per_frame;
    }


    /**--------------------------------------------------------------------**/
    /**                           RENDERING FUNCTIONS                      **/
    /**--------------------------------------------------------------------**/
    /**Initialize and set up lights and material for the animation**/
    private void initLightMaterial() {

        /**Initialize the lighting affect**/

        float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        float[] lightAmbient = {0.7f, 0.7f, 0.7f, 1.0f};
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 0f};
        float[] lightPosition= {0.0f, 0.0f, 1f, 0f};

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, lightSpecular, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);


        FloatBuffer shininess= FloatBuffer.wrap(new float[] {120.0f});
        FloatBuffer spec= FloatBuffer.wrap(new float[] {0.1f,0.1f,0.1f,0});
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, spec);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);

        /**Turn on the lights**/
        gl.glEnable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_LIGHTING);

        /**Initialize the shade model and material**/
        gl.glShadeModel(GL.GL_SMOOTH);
        gl.glEnable(GL.GL_COLOR_MATERIAL);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
    }

    /**Return the corresponding colour in float vector format**/
    private float [] setUpColour(String colour) {
        float [] currentColour = new float[3];
        if(colour.equals("BLACK")) {
            currentColour = new float[]{0.0f, 0.0f, 0.0f};
        } else if(colour.equals("RED")) {
            currentColour = new float[]{1.0f, 0.0f, 0.0f};
        } else if(colour.equals("GREEN")) {
            currentColour = new float[]{0.0f, 1.0f, 0.0f};
        } else if(colour.equals("BLUE")) {
            currentColour = new float[]{0.0f, 0.0f, 1.0f};
        } else if(colour.equals("WHITE")) {
            currentColour = new float[]{1.0f, 1.0f, 1.0f};
        } else if(colour.equals("NAVY")) {
            currentColour = new float[]{0.098f, 0.48627f, 0.53725f};
        } else if(colour.equals("LIGHT PURPLE")) {
            currentColour = new float[]{231f/255f, 109f/255f, 208f/255f};
        } else if(colour.equals("BABY BLUE")) {
            currentColour = new float[]{182f/255f, 217f/255f, 222f/255f};
        } else if(colour.equals("LIGHT GREEN")) {
            currentColour = new float[]{112f/255f, 165f/255f, 137f/255f};
        } else if(colour.equals("GOLD")) {
            currentColour = new float[]{255f/255f, 215f/255f, 0f/255f};
        } else if(colour.equals("PLATINUM")) {
            currentColour = new float[]{204f/255f, 198f/255f, 173f/255f};
        } else if(colour.equals("YELLOW")) {
            currentColour = new float[]{247f/255f, 252f/255f, 69f/255f};
        } else if(colour.equals("DARK BROWN")) {
            currentColour = new float[]{79f/255f, 73f/255f, 54f/255f};
        } else if(colour.equals("MAROON")) {
            currentColour = new float[]{128f/255f, 0f/255f, 0f/255f};
        } else if(colour.equals("LAVENDER")) {
            currentColour = new float[]{230f/255f, 230f/255f, 250f/255f};
        } else if(colour.equals("IVORY")) {
            currentColour = new float[]{255f/255f, 255f/255f, 240f/255f};
        } else if(colour.equals("ROSY BROWN")) {
            currentColour = new float[]{188f/255f, 143f/255f, 143f/255f};
        } else if(colour.equals("PINK")) {
            currentColour = new float[]{255f/255f, 105f/255f, 180f/255f};
        } else if(colour.equals("AQUA MARINE")) {
            currentColour = new float[]{102f/255f, 205f/255f, 170f/255f};
        } else if(colour.equals("DARK SEA GREEN")) {
            currentColour = new float[]{143f/255f, 188f/255f, 143f/255f};
        }

        return currentColour;
    }

    /**Main rendering function. Draws everything in JOGL**/
    /**This display is updated as the animation plays**/
    public void display(GLAutoDrawable gLDrawable) {

        /**----------------------------------------------------------------**/
        /**Initialize JOGL**/
        gl = gui.initGL(gLDrawable.getGL());

        /**Initialize the lights and materials**/
        initLightMaterial();
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glDepthFunc(GL.GL_ALWAYS);
        gl.glDepthRange(10, -10);
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/

        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        /**@MAIN RENDERING START**/
        gl.glPushMatrix();


        /**@ANIMATION RENDERING START**/
        int current_frame = objectNumber;
        ControlVariables.
        speedIntervalCounter++;
        if(ControlVariables.speedScroller == 0) {
            /**Do nothing**/
        }
        else if(ControlVariables.
                speedIntervalCounter >=
                (ControlVariables.maxSpeedScrollerVal-ControlVariables.
                        speedScroller)) {
        	current_scene = cameraScenes.getCurrentScene();
            current_frame = current_scene.getNextFrameNumber();
            ControlVariables.
            speedIntervalCounter = 0;
        }
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/

        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        objectNumber = current_frame;
        float [] camera_center = current_scene.getCamCenter(objectNumber);
        float [] camera_orn = current_scene.getCamOrientation(objectNumber);
        float camera_zoom = current_scene.getZoomValue(objectNumber);
        //System.out.println(camera_zoom);

        camera_orn = VectorTools.norm(camera_orn);
        camera_orn = VectorTools.mult(camera_zoom, camera_orn);
        
        if(ControlVariables.auto_control) {
        	gl.glPushMatrix();
        	gui.glu.gluLookAt(
        			camera_orn[0]-camera_center[0],
        			Math.abs(camera_orn[1])-camera_center[1],
        			camera_orn[2]-camera_center[2],

        			0-camera_center[0],
        			0-camera_center[1],
        			0-camera_center[2],

        			0,1,0);
        }
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        if(!ControlVariables.auto_control) {
        	/**Perform the user controls**/
        	gl.glTranslatef(-ControlVariables.
        			xMovementScroller*2.5f,
        			-25f -ControlVariables.
        			yMovementScroller*2.5f,
        			-125f + (-ControlVariables.
        					zMovementScroller*2.5f));

    		gl.glRotatef(-ControlVariables.
    				xRotationScroller, 1.0f, 0.0f, 0.0f);
    		gl.glRotatef(-ControlVariables.yRotationScroller, 0.0f, 1.0f, 0.0f);
    		gl.glRotatef(ControlVariables.zRotationScroller, 0.0f, 0.0f, 1.0f);
        }
		/**----------------------------------------------------------------**/
		/**----------------------------------------------------------------**/
		
		/**----------------------------------------------------------------**/
		/**----------------------------------------------------------------**/
        /**Retrieve the required arrays for the object number**/
        float [][][] lines =  data_per_frame[objectNumber];

        /**Draw the mesh**/
        if(ControlVariables.
                meshVisibility.equals("ON")) {
        	drawBackground();
        	drawGround();
            drawLineMesh(lines);
            //drawTestSquare();
        }
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
        /**Draw the PCA vector**/
        if(ControlVariables.
                meshVisibility.equals("ON")) {
            //drawPCAVectors(pca_s);
        }

        /**Render the PCA normal**/
        if(ControlVariables.
                meshVisibility.equals("ON")) {
        	//drawPCANormal(camera_orn, camera_center);
        }

        gl.glPopMatrix();

        /**@ANIMATION RENDERING END**/

        gl.glPopMatrix();
        /**@MAIN RENDERING END**/
        /**----------------------------------------------------------------**/
        /**----------------------------------------------------------------**/
    }

    /**Draw a background behind the animation**/
    private void drawBackground() {

    	for(int i = -10; i <  10; i++) {
    		for(int j = -10; j < 10; j++) {

    			if(
    					(Math.abs(i%2) == 0 && Math.abs(j%2) == 1) ||
    					(Math.abs(i%2) == 1 && Math.abs(j%2) == 0)) {

    				gl.glColor4f(0.5f, 0.8f, 0.5f, 1.0f);
    			} else {
    				gl.glColor4f(0.7f, 1f, 0.7f, 1.0f);
    			}
    	        gl.glBegin(GL.GL_POLYGON);

    	        gl.glNormal3f(0.0f,1, 0.0f);
    	        gl.glVertex3d(2*bS*i, 2*bS*(j+1), bS*-20);
    	        gl.glVertex3d(2*bS*i, 2*bS*j, bS*-20);
    	        gl.glVertex3d(2*bS*(i+1),2*bS*j, bS*-20);
    	        gl.glVertex3d(2*bS*(i+1), 2*bS*(j+1), bS*-20);

    	        gl.glEnd();
    		}
    	}


    	for(int i = -1; i <  1; i++) {
    		for(int j = -1; j < 1; j++) {

    			if(
    					(Math.abs(i%2) == 0 && Math.abs(j%2) == 1) ||
    					(Math.abs(i%2) == 1 && Math.abs(j%2) == 0)) {

    				gl.glColor4f(0.8f, 0.5f, 0.8f, 1.0f);
    			} else {
    				gl.glColor4f(1f, 0.7f, 1f, 1.0f);
    			}
    	        gl.glBegin(GL.GL_POLYGON);

    	        gl.glNormal3f(0.0f,1, 0.0f);
    	        gl.glVertex3d(20*bS*i, 20*bS*(j+1), bS*20);
    	        gl.glVertex3d(20*bS*i, 20*bS*j, bS*20);
    	        gl.glVertex3d(20*bS*(i+1),20*bS*j, bS*20);
    	        gl.glVertex3d(20*bS*(i+1), 20*bS*(j+1), bS*20);

    	        gl.glEnd();
    		}
    	}



    	for(int i = -5; i <  5; i++) {
    		for(int j = -5; j < 5; j++) {

    			if(
    					(Math.abs(i%2) == 0 && Math.abs(j%2) == 1) ||
    					(Math.abs(i%2) == 1 && Math.abs(j%2) == 0)) {

    				gl.glColor4f(0.5f, 0.5f, 0.8f, 1.0f);
    			} else {
    				gl.glColor4f(0.7f, 0.7f, 1f, 1.0f);
    			}
    	        gl.glBegin(GL.GL_POLYGON);

    	        gl.glNormal3f(0.0f,1, 0.0f);
    	        gl.glVertex3d(bS*-20, 4*bS*i, 4*bS*(j+1));
    	        gl.glVertex3d(bS*-20, 4*bS*i, 4*bS*j);
    	        gl.glVertex3d(bS*-20, 4*bS*(i+1),4*bS*j);
    	        gl.glVertex3d(bS*-20, 4*bS*(i+1), 4*bS*(j+1));

    	        gl.glEnd();
    		}
    	}


    	for(int i = -2; i <  2; i++) {
    		for(int j = -2; j < 2; j++) {

    			if(
    					(Math.abs(i%2) == 0 && Math.abs(j%2) == 1) ||
    					(Math.abs(i%2) == 1 && Math.abs(j%2) == 0)) {

    				gl.glColor4f(0.8f, 0.5f, 0.5f, 1.0f);
    			} else {
    				gl.glColor4f(1f, 0.7f, 0.7f, 1.0f);
    			}
    	        gl.glBegin(GL.GL_POLYGON);

    	        gl.glNormal3f(0.0f,1, 0.0f);
    	        gl.glVertex3d(bS*20, 10*bS*i, 10*bS*(j+1));
    	        gl.glVertex3d(bS*20, 10*bS*i, 10*bS*j);
    	        gl.glVertex3d(bS*20, 10*bS*(i+1),10*bS*j);
    	        gl.glVertex3d(bS*20, 10*bS*(i+1), 10*bS*(j+1));

    	        gl.glEnd();
    		}
    	}
    }

    /**Draw the ground for the animation**/
    private void drawGround() {

    	for(int i = -20; i <  20; i++) {
    		for(int j = -20; j < 20; j++) {

    			if(
    					(Math.abs(i%2) == 0 && Math.abs(j%2) == 1) ||
    					(Math.abs(i%2) == 1 && Math.abs(j%2) == 0)) {

    				gl.glColor4f(0.3f, 0.4f, 0.3f, 1.0f);
    			} else {
    				gl.glColor4f(0.1f, 1.0f, 0.1f, 1.0f);
    			}
    	        gl.glBegin(GL.GL_POLYGON);

    	        gl.glNormal3f(0.0f,1, 0.0f);
    	        gl.glVertex3d(bS*i, 0.0f, bS*(j+1));
    	        gl.glVertex3d(bS*i, 0.0f, bS*j);
    	        gl.glVertex3d(bS*(i+1), 0.0f, bS*j);
    	        gl.glVertex3d(bS*(i+1), 0.0f, bS*(j+1));

    	        gl.glEnd();
    		}
    	}
    }

	public static float [] a1 = new float[]{160,20, -120};
	public static float [] a2 = new float[]{-160,20, -120};
	public static float [] a3 = new float[]{-160,60, -120};
	public static float [] a4 = new float[]{160,60, -120};
    /**Draw the test square**/
    @SuppressWarnings("unused")
	private void drawTestSquare() {
    	/**
        gl.glBegin(GL.GL_POLYGON);

        gl.glNormal3f(0.0f,1, 0.0f);
        gl.glVertex3d(a1[0],a1[1],a1[2]);
        gl.glVertex3d(a2[0],a2[1],a2[2]);
        gl.glVertex3d(a3[0],a3[1],a3[2]);
        gl.glVertex3d(a4[0],a4[1],a4[2]);

        gl.glEnd();**/
    }
    
    /**Draw the lines of the motion capture**/
    private void drawLineMesh(
            float[][][]lines) {

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);



        for(int i = 0; i < lines.length; i++) {
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glShadeModel(GL.GL_SPECULAR);
            float[]currentColour = setUpColour(ControlVariables.
                    meshColour);
            gl.glColor4f(
                    currentColour[0],
                    currentColour[1],
                    currentColour[2], 1.0f);
            float [] normal =
                    VectorTools.getNorm(
                            new float[]{
                                    lines[i][0][0],
                                    lines[i][0][1],
                                    lines[i][0][2]},
                        new float[]{lines[i][1][0],
                                    lines[i][1][1],
                                    lines[i][1][2]},
                        new float[]{lines[i][1][0]+(ControlVariables.scale*1f),
                                    lines[i][1][1]+(ControlVariables.scale*1f),
                                    lines[i][1][2]+(ControlVariables.scale*1f)}
                            );
            gl.glNormal3f(
                    normal[0],
                    normal[1],
                    normal[2]);

            gl.glVertex3f(
                    lines[i][0][0],
                    lines[i][0][1],
                    lines[i][0][2]);
            gl.glVertex3f(
                    lines[i][1][0],
                    lines[i][1][1],
                    lines[i][1][2]);
            gl.glVertex3f(
                    lines[i][1][0]+(ControlVariables.scale*1f),
                    lines[i][1][1]+(ControlVariables.scale*1f),
                    lines[i][1][2]+(ControlVariables.scale*1f));
            gl.glEnd();



            gl.glBegin(GL.GL_TRIANGLES);
            currentColour = setUpColour(ControlVariables.
                    meshColour);
            gl.glColor4f(
                    currentColour[0],
                    currentColour[1],
                    currentColour[2], 1.0f);

            normal =
                    VectorTools.getNorm(
                            new float[]{
                                    lines[i][0][0],
                                    lines[i][0][1],
                                    lines[i][0][2]},
                                    new float[]{
                                    lines[i][1][0],
                                    lines[i][1][1],
                                    lines[i][1][2]},
                                    new float[]{
                                    lines[i][1][0]-(ControlVariables.scale*1f),
                                    lines[i][1][1],
                                    lines[i][1][2]-(ControlVariables.scale*1f)}
                            );

            gl.glNormal3f(
                    normal[0],
                    normal[1],
                    normal[2]);

            gl.glVertex3f(
                    lines[i][0][0],
                    lines[i][0][1],
                    lines[i][0][2]);
            gl.glVertex3f(
                    lines[i][1][0],
                    lines[i][1][1],
                    lines[i][1][2]);
            gl.glVertex3f(
                    lines[i][1][0]-(ControlVariables.scale*1f),
                    lines[i][1][1],
                    lines[i][1][2]-(ControlVariables.scale*1f));
            gl.glEnd();
        }


    }

    /**Draw the PCA vectors**/
    @SuppressWarnings("unused")
	private void drawPCAVectors(
            float[][]pca_s) {

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);


        float[]p1= pca_s[0];
        float[]c= pca_s[1];
        float[]p2= pca_s[2];

        /**Render these vectors for display**/
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex3f(
                c[0],c[1]+0.01f,c[2]+0.03f);
        gl.glVertex3f(
                c[0],c[1],c[2]);
        gl.glVertex3f(
                p1[0],p1[1],p1[2]);
        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex3f(
                c[0],c[1]+0.01f,c[2]+0.03f);
        gl.glVertex3f(
                c[0],c[1],c[2]);
        gl.glVertex3f(
                p2[0],p2[1],p2[2]);
        gl.glEnd();
    }

    /**Draw the PCA vectors**/
    @SuppressWarnings("unused")
	private void drawPCANormal(
            float[]pca_normal, float [] pca_cent) {

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);


        float[]p1= VectorTools.add(VectorTools.mult(100.5f, pca_normal), pca_cent);


        /**Render these vectors for display**/
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex3f(
        		pca_cent[0],pca_cent[1],pca_cent[2]);
        gl.glVertex3f(
        		p1[0],p1[1]-50.2f,p1[2]);
        gl.glVertex3f(
                p1[0],p1[1]+50.2f,p1[2]);
        gl.glEnd();
    }


    /**--------------------------------------------------------------------**/
    /**                             GUI FUNCTIONS                          **/
    /**--------------------------------------------------------------------**/
    /**Close the GUI and Mesh Display**/
    protected void exit(){
        gui.animator.stop();
        gui.frame.dispose();
        System.exit(0);
    }

    /**Initialize all the GUI components**/
    protected void initGUI() {
        gui.initGUI(this);
    }

    /**Useless function required for interface implementation**/
    public void init(GLAutoDrawable gLDrawable) {
        GL gl = gLDrawable.getGL();
        gl.glShadeModel(GL.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gLDrawable.addKeyListener(this);
    }

    /**Useless function required for interface implementation**/
    public void displayChanged(GLAutoDrawable gLDrawable,
            boolean modeChanged, boolean deviceChanged) {
    }

    /**Useless function required for interface implementation**/
    public void reshape(GLAutoDrawable gLDrawable,
            int x,int y, int width, int height) {
        GL gl = gLDrawable.getGL();
        if(height <= 0) {
            height = 1;
        }

        float h = (float)width / (float)height;
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gui.glu.gluPerspective(50.0f, h, 1.0, 1000.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    /**Useless function required for interface implementation**/
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exit();
        }
    }

    /**Useless function required for interface implementation**/
    public void keyReleased(KeyEvent e) {

    }

    /**Useless function required for interface implementation**/
    public void keyTyped(KeyEvent e) {
    }


    /**--------------------------------------------------------------------**/
    /**                             IO FUNCTIONS                           **/
    /**--------------------------------------------------------------------**/
    /**Read in all the object files from a directory
     * as described in the provided property file**/
    private void readObjects() {

    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/
    	/**Read the meta data file**/
        Properties metaFile = new Properties();
        try {
            metaFile.load(new FileInputStream
                    ("models_amc/meta.properties"));
        } catch (FileNotFoundException e) {
            System.out.println(
                    "Failed to read property file for object files");
            System.exit(1);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(
                    "Failed to read property file for object files");
            System.exit(1);
            e.printStackTrace();
        }

        /**Get the name of the input files**/
        modelLocation = metaFile.getProperty(animationNumber+"");
    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/


    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/
        /**Store the motion capture points per frame**/
        IReader iR = new AMCReader("models_amc\\"+modelLocation);
        iR.readData();
        this.data_per_frame = iR.getData();

        /**Get the joint sticks for each frame**/
        joint_sticks_per_frame = iR.getJointSticksPerFrame();

        /**Get the main root node for each frame**/
        SkeletonNode [] node_per_frame = iR.getSkeletonRoot();
        
        /**Calculate the two main PCA vectors for the points per frame**/
        pcaPerFrame = getPCAPerFrame();
    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/


    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/
        /**Create the class for managing data and movement for each scene
         * using the data and pca vectors for each frame**/
        cameraScenes = new CameraScenes(
        		pcaPerFrame,
        		data_per_frame,
        		joint_sticks_per_frame,
        		node_per_frame);

        /**Find the camera orientations that change very abruptly. These
         * abrupt changes can be smoothened as well however the filter
         * will then required a very high coefficient, thus making it
         * insensitive to smaller changes.**/

        /**Thus cut scenes at locations with large area. Ignore small areas.
         * Small areas will be considered by the filter as requiring little
         * motion, hence an abrupt orientation from a small to large area
         * will be smoothen correctly, as the small area will have little
         * effect on the weight.**/

        /**For the case where area adjust locally, figure out which changes
         * are abrupt, ignoring small areas that are small to the local
         * average**/

        /**To note: only the y-axis and x-axis rotation will be affected,
         * as the normal is one dimensional and is affected by the x-y
         * plane. The camera stares at this plane without rotating around
         * the normal**/
        cameraScenes.divideToSubFramesForRotation();

        /**Smoothen the camera motion, given the camera
         * orientation  and  area  for  each  frame **/
        cameraScenes.smoothenOrientationsPerScene();
        
        /**Find the first  level motion within the scenes**/
        if(ControlVariables.auto_control) {
        	cameraScenes.findFirstLevelMotion();
        	cameraScenes.applyFirstLevelMotionScenes();
        }
        /**Initialize the first camera scene**/
        current_scene = cameraScenes.getCurrentScene();
        
        
        /**Find third level motion within the scenes**/
        if(ControlVariables.auto_control) {
        	cameraScenes.findThirdLevelMotion();
        	cameraScenes.applyThirdLevelMotionScenes();
        }
    	/**==========================================================**/
    	/**----------------------------------------------------------**/
    	/**==========================================================**/
    }


    /**--------------------------------------------------------------------**/
    /**                             MAIN FUNCTIONS                         **/
    /**--------------------------------------------------------------------**/
    /**Class Constructor**/
    public Launcher() {
    }

    public static void main(String[] args) {
        Launcher aR = new Launcher();

        aR.readObjects();
        aR.initGUI();
    }
}