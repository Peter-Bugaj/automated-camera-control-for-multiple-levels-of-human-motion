/**
 * 
 */
package main;

import java.awt.BorderLayout;
import java.awt.Choice;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Animator;

/**
 * @author Piotr Bugaj
 *
 */
public class GUI {

	/**------------Main GL variables---------------------------------------**/
	protected GLU glu = new GLU();
	protected GLCanvas canvas = new GLCanvas();
	protected Frame frame;
	protected Animator animator = new Animator(canvas); 
	protected GL gl;
	/**--------------------------------------------------------------------**/
	
	
	/**------------GUI-----------------------------------------------------**/
	protected Container mainBottomBox;
	protected Container mainTopBox;

	protected Container movementBox;
	protected Container rotationBox;
	protected Container speedBox;

	protected Container meshColourBox;

	
	/**GUI for controlling the x movement of the mesh**/
	protected Scrollbar xAxisMovement;
	protected TextComponent xAxisMovementText;
	
	/**GUI for controlling the y movement of the mesh**/
	protected Scrollbar yAxisMovement;
	protected TextComponent yAxisMovementText;

	/**GUI for controlling the z movement of the mesh**/
	protected Scrollbar zAxisMovement;
	protected TextComponent zAxisMovementText;

	/**GUI for controlling the x rotation of the mesh**/
	protected Scrollbar xRotation;
	protected TextComponent xRotationText;

	/**GUI for controlling the y rotation of the mesh**/
	protected Scrollbar yRotation;
	protected TextComponent yRotationText;

	/**GUI for controlling the z rotation of the mesh**/
	protected Scrollbar zRotation;
	protected TextComponent zRotationText;

	/**Variable describing the dimension of the GUI**/
	protected final int frameWidth = 980;
	protected final int frameHeight = 570;


	/**GUI for controlling the speed of animation**/
	protected Scrollbar speedBar;
	protected TextComponent speedTextComponent;

	/**GUI for controlling wheather the mesh is visible or not**/
	protected Choice meshVisibilityChooser;
	/**GUI for controlling the colour the mesh**/
	protected Choice meshColourChooser;
	/**--------------------------------------------------------------------**/
	
	
	/**Set up GL**/
	protected GL initGL(GL gl) {
		this.gl = gl;
		return gl;
	}
	
	/**Set up the interface**/
	protected void initGUI(final Launcher aR) {

		/**Set up the main frame and canvas**/
		frame = new Frame("Motion Cap Renderer");
		frame.setSize(frameWidth, frameHeight);
		frame.setLayout(new BorderLayout());

		canvas.addGLEventListener(aR);
		canvas.setSize(frameWidth, frameHeight-40);
		frame.add(canvas, BorderLayout.CENTER);

		/**Set up the GUI containers**/
		mainBottomBox = new Container();
		mainBottomBox.setSize(frameWidth, 40);
		mainBottomBox.setLayout(new GridLayout(1, 4));

		mainTopBox = new Container();
		mainTopBox.setSize(frameWidth, 40);
		mainTopBox.setLayout(new GridLayout(1, 4));

		movementBox = new Container();
		movementBox.setSize(frameWidth, 20);
		movementBox.setLayout(new FlowLayout());

		rotationBox = new Container();
		rotationBox.setSize(frameWidth, 20);
		rotationBox.setLayout(new FlowLayout());

		speedBox = new Container();
		speedBox.setSize(frameWidth, 20);
		speedBox.setLayout(new FlowLayout());

		meshColourBox = new Container();
		meshColourBox.setSize(frameWidth, 20);
		meshColourBox.setLayout(new FlowLayout());

		/**Label for the controls**/
		Label rotationText = new Label("Rotation");
		Label movementText = new Label("Movement");
		Label speedText = new Label("Speed");

		Label meshColourText = new Label("Mesh Colour");
		
		
		rotationText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		movementText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		speedText.setFont(
				new Font("monospaced", Font.BOLD, 15));
		meshColourText.setFont(
				new Font("monospaced", Font.BOLD, 15));

		Label xLabel = new Label("x");
		xLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		xLabel.setAlignment(Label.CENTER);
		Label yLabel = new Label("y");
		yLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		yLabel.setAlignment(Label.CENTER);
		Label zLabel = new Label("z");
		zLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		zLabel.setAlignment(Label.CENTER);
		Label xLabel2 = new Label("x");
		xLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		xLabel2.setAlignment(Label.CENTER);
		Label yLabel2 = new Label("y");
		yLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		yLabel2.setAlignment(Label.CENTER);
		Label zLabel2 = new Label("z");
		zLabel2.setFont(new Font("monospaced", Font.BOLD, 9));
		zLabel2.setAlignment(Label.CENTER);

		Label speedLabel = new Label("f/s");
		speedLabel.setFont(new Font("monospaced", Font.BOLD, 9));
		speedLabel.setAlignment(Label.CENTER);

		String [] colours = new String[]{"DARK BROWN", "BLACK", "RED",
				"GREEN", "BLUE", "WHITE", "LIGHT PURPLE", "NAVY",
				"BABY BLUE", "LIGHT GREEN", "GOLD", "PLATINUM",
				"YELLOW", "MAROON", "LAVENDER", "IVORY",
				"ROSY BROWN", "PINK", "AQUA MARINE", "DARK SEA GREEN"};

		/**Set up the GUI for controlling the colour of the mesh**/
		meshColourChooser = new Choice();
		for(int i = 0; i < colours.length; i++) {
			meshColourChooser.add(colours[i]);
		}
		meshColourChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						ControlVariables.meshColour = meshColourChooser.getSelectedItem();
					}
				});
		
		/**Set up the GUI for controlling the visibility of the mesh**/
		meshVisibilityChooser = new Choice();
		meshVisibilityChooser.add("ON");
		meshVisibilityChooser.add("OFF");
		meshVisibilityChooser.addItemListener(
				new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						ControlVariables.meshVisibility =
							meshVisibilityChooser.getSelectedItem();
					}
				});

		/**Set up the GUI for controlling speed**/
		Container speedTextContainer = new Container();
		speedTextContainer.setLayout(new BorderLayout());
		speedTextComponent = new TextField("" + 19);
		speedTextComponent.addTextListener(        	
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = speedTextComponent.getText();
						try{
							int temp2 = Integer.parseInt(temp);
							ControlVariables.
							speedScroller = temp2;
							speedBar.setValue(temp2);
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		speedTextContainer.add(speedTextComponent, BorderLayout.CENTER);
		speedTextContainer.add(speedLabel, BorderLayout.SOUTH);

		speedBar = new Scrollbar(Scrollbar.VERTICAL, 19, 0, 0, 21);
		speedBar.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						speedScroller = e.getValue();
						speedTextComponent.setText("" + e.getValue());
					}
				}
		);

		/**Text and text fields for the controls**/
		Container zRotationTextContainer = new Container();
		zRotationTextContainer.setLayout(new BorderLayout());
		zRotationText = new TextField("0");
		zRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = zRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								ControlVariables.
								zRotationScroller = -temp2;
								zRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
								ControlVariables.
								zRotationScroller = -temp2;
								zRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		zRotationTextContainer.add(zRotationText, BorderLayout.CENTER);
		zRotationTextContainer.add(zLabel, BorderLayout.SOUTH);

		Container yRotationTextContainer = new Container();
		yRotationTextContainer.setLayout(new BorderLayout());
		yRotationText = new TextField(""+((int) ControlVariables.yRotationScroller));
		yRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = yRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								ControlVariables.
								yRotationScroller = -temp2;
								yRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
								ControlVariables.
								yRotationScroller = -temp2;
								yRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		yRotationTextContainer.add(yRotationText, BorderLayout.CENTER);
		yRotationTextContainer.add(yLabel, BorderLayout.SOUTH);

		Container xRotationTextContainer = new Container();
		xRotationTextContainer.setLayout(new BorderLayout());
		xRotationText = new TextField(""+(int) ControlVariables.xRotationScroller);
		xRotationText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = xRotationText.getText();
						try{
							float temp2 = Float.parseFloat(temp);
							if((temp2 <= 179) && (temp2 > 0)) {
								ControlVariables.
								xRotationScroller = -temp2;
								xRotation.setValue(-((int)temp2));
							} else if(((temp2 <= 360) && (temp2 >= 180)) ||
									(temp2==0)) {
								ControlVariables.
								xRotationScroller = -temp2;
								xRotation.setValue(-((int)temp2) + 360);
							}

						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		xRotationTextContainer.add(xRotationText, BorderLayout.CENTER);
		xRotationTextContainer.add(xLabel, BorderLayout.SOUTH);

		Container zAxisMovementTextContainer = new Container();
		zAxisMovementTextContainer.setLayout(new BorderLayout());
		zAxisMovementText = new TextField(""+(-1*(int) ControlVariables.zMovementScroller));
		zAxisMovementText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = zAxisMovementText.getText();
						try{
							ControlVariables.
							zMovementScroller = -(Float.parseFloat(temp));
							zAxisMovement.setValue(((int)ControlVariables.
									zMovementScroller));
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		zAxisMovementTextContainer.add(zAxisMovementText,BorderLayout.CENTER);
		zAxisMovementTextContainer.add(zLabel2, BorderLayout.SOUTH);

		Container yAxisMovementTextContainer = new Container(); 
		yAxisMovementTextContainer.setLayout(new BorderLayout());
		yAxisMovementText = new TextField(""+(-1*(int) ControlVariables.yMovementScroller));
		yAxisMovementText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = yAxisMovementText.getText();
						try{
							ControlVariables.
							yMovementScroller = -(Float.parseFloat(temp));
							yAxisMovement.setValue(((int)ControlVariables.
									yMovementScroller));
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		yAxisMovementTextContainer.add(yAxisMovementText,BorderLayout.CENTER);
		yAxisMovementTextContainer.add(yLabel2, BorderLayout.SOUTH);

		Container xAxisMovementTextContainer = new Container();
		xAxisMovementTextContainer.setLayout(new BorderLayout());
		xAxisMovementText = new TextField(""+(-1*(int) ControlVariables.xMovementScroller));
		xAxisMovementText.addTextListener(
				new TextListener(){
					public void textValueChanged(TextEvent e){
						String temp = xAxisMovementText.getText();
						try{
							float temp2 = -(Float.parseFloat(temp));
							if(temp2 <= 50 || temp2 >= -50) {
								ControlVariables.
								xMovementScroller = temp2;
								xAxisMovement.
								setValue(((int)ControlVariables.
										xMovementScroller));
							}
						} catch(NumberFormatException e1){
							/**Silent fail - dont care about this value**/
						}
					}
				});
		xAxisMovementTextContainer.add(xAxisMovementText,BorderLayout.CENTER);
		xAxisMovementTextContainer.add(xLabel2, BorderLayout.SOUTH);

		/**Set up the scrollbars**/
		xAxisMovement = new Scrollbar(Scrollbar.VERTICAL, (int) ControlVariables.xMovementScroller, 0, -50, 50);
		xAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						xMovementScroller = e.getValue();
						xAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		yAxisMovement = new Scrollbar(Scrollbar.VERTICAL, (int) ControlVariables.yMovementScroller, 0, -50, 50);
		yAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						yMovementScroller = e.getValue();
						yAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		zAxisMovement = new Scrollbar(Scrollbar.VERTICAL, (int) ControlVariables.zMovementScroller, 0, -50, 50);
		zAxisMovement.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						zMovementScroller = e.getValue();
						zAxisMovementText.setText("" + -1*e.getValue());
					}
				}
		);

		xRotation = new Scrollbar(Scrollbar.VERTICAL, 360-(int) ControlVariables.xRotationScroller, 0, -180, 182);
		xRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						xRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							xRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
							xRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							xRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							xRotationText.setText("" + 180);
						}
					}
				}
		);

		yRotation = new Scrollbar(Scrollbar.VERTICAL, 360-(int) ControlVariables.yRotationScroller, 0, -180, 182);
		yRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						yRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							yRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
							yRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							yRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							yRotationText.setText("" + 180);
						}
					}
				}
		);

		zRotation = new Scrollbar(Scrollbar.VERTICAL, 360-(int) ControlVariables.zRotationScroller, 0, -180, 182);
		zRotation.addAdjustmentListener(
				new AdjustmentListener(){
					public void adjustmentValueChanged(AdjustmentEvent e){
						ControlVariables.
						zRotationScroller = e.getValue();
						if((e.getValue() >= 0)  && (e.getValue() <= 180)) {
							zRotationText.setText("" +
									(360 + ((-1)*e.getValue())));
						} else if((e.getValue() >= -179)  &&
								(e.getValue() < 0)){
							zRotationText.setText("" + -1*e.getValue());
						} else if (e.getValue() == 181){
							zRotationText.setText("" + 179);
						} else if (e.getValue() == -180){
							zRotationText.setText("" + 180);
						}
					}
				}
		);

		/**Add together all the created GUI components**/		
		meshColourBox.add(meshColourText);
		meshColourBox.add(meshColourChooser);
		meshColourBox.add(meshVisibilityChooser);

		speedBox.add(speedText);
		speedBox.add(speedTextContainer);
		speedBox.add(speedBar);
		

		movementBox.add(movementText);

		movementBox.add(xAxisMovementTextContainer);
		movementBox.add(xAxisMovement);

		movementBox.add(yAxisMovementTextContainer);
		movementBox.add(yAxisMovement);

		movementBox.add(zAxisMovementTextContainer);
		movementBox.add(zAxisMovement);


		rotationBox.add(rotationText);

		rotationBox.add(xRotationTextContainer);
		rotationBox.add(xRotation);

		rotationBox.add(yRotationTextContainer);
		rotationBox.add(yRotation);

		rotationBox.add(zRotationTextContainer);
		rotationBox.add(zRotation);


		mainBottomBox.add(movementBox);
		mainBottomBox.add(rotationBox);
		mainBottomBox.add(speedBox);


		mainTopBox.add(meshColourBox);

		frame.add(mainTopBox, BorderLayout.NORTH);
		frame.add(mainBottomBox, BorderLayout.SOUTH);

		/**Finalize the display**/
		frame.setUndecorated(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				aR.exit();
			}
		});
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}	
}