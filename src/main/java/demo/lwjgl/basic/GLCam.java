package demo.lwjgl.basic;

import static com.bulletphysics.demos.opengl.IGL.GL_MODELVIEW;
import static com.bulletphysics.demos.opengl.IGL.GL_PROJECTION;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import lib.lwjgl.glmodel.GL_Matrix;
import lib.lwjgl.glmodel.GL_Vector;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.VectorUtil;

/**
 * Change the position and orientation of a GLCamera based on input keys.  This
 * class moves the camera according to how much time has elapsed, so will
 * produce consistent response to input, regardless of processor speed.
 *
 * Andrew Tucker
 * 
 * @see GLCamera.java
 */
public class GLCam {

	private String direction = "";		//String that stores KEY_*direction* for panning camera
	private boolean isRunning = false;	//true if change in camera is still calculating/drawing
	private boolean zoomOut = true;		//handles the snap out when panning camera
	private boolean zoomIn = false;		//handles snap in when panning camera
	private boolean up = false;			//true if currently in top-down view -- changed by KEY_UP and KEY_DOWN
	private int quadrant = 1;			//keeps track of camera quadrant location relative to camera origin (origin is quadrant 1)

	private float rotationSpeed = 460.0f;//rotation speed of panning camera -- degrees rotated per second
	private float rotationSpeedHigh = 600.0f;  //used to return camera to camera origin quickly before rotating to top-down display
	private float elevation = 0f; 		//degree of elevation when rotating about the x-axis 
	private float runningElevation = 0f; //calculated degree of x-axis elevation to be moved on current camera update
	private float runningDegrees = 0f;	//current degrees from origin while panning camera is running
	private float prevRotation = 0f;		//previous rotation from origin as of last camera button input
	private float currRotation = 0f;		//current rotation from origin
	private float camDist = 15f;			//distance from target (used for rotation)
	private float currCamDist = 15f;		//the camera's current distance from target
	private float zoomDist = 20f;		//distance to zoom out to when panning

	//not used for panning camera
	public long prevTime = Sys.getTime();//previous time used to halt constant camera change if button is held down
	public float camSpeedR = 90;         // degrees per second
	public float camSpeedXZ = 10;        // units per second
	public float camSpeedY  = 10;        // units per second

	public GLCamera camera;  // camera that we'll be moving

	double avgSecsPerFrame=.01;   // to smooth out motion, keep a moving average of frame time deltas
	double numToAvg = 50;         // number of frames to average (about one second)
	
	Clock clock = new Clock();
	
 



	/**
	 * Create a GLCameraMover with the camera that it will be moving.
	 *
	 * @param cam
	 */
	public GLCam(GLCamera cam) {
		camera = cam;
	}

	/**
	 * Create a GLCameraMover.  Will create a default camera.
	 *
	 * @param cam
	 */
	public GLCam() {
		camera = new GLCamera(0,0,10, 0,0,0, 0,1,0);
	}
	
	//added
	/**
	 * @return String containing camera direction
	 */
	public String getDirection() {
		return direction;
	}
	
	//added
	public int getQuadrant() {
		return quadrant;
	}

	 //added
	 /**
	  * Updates the camera's current quadrant. All quadrants are relative to camera's origin as of 
	  * the start of the application.  Camera's origin is quadrant 1. Quadrant 1 ranges from origin to 90 degrees, exclusive.
	  * Quadrant 2 ranges from origin + 90 degrees to 180 degrees, exclusive.  etc.
	  * @param s String that determines whether the quadrant is incremented or decremented
	  * 
	  * See updatePan(String s)  -- Quadrants are used to re-orient camera to origin for top-down view.
	  */
	 public void updateQuadrant(String s) {
		 if (s.equals("PLUS")) {
			 quadrant++;
			 if (quadrant > 4) {
				 quadrant = 1;
			 }
		 }
		 if (s.equals("MINUS")) {
			 quadrant--;
			 if (quadrant < 1) {
				 quadrant = 4;
			 }
		 }
	 }

	 //added -- brought in from jbullet, altered to use GL_Vector
	 /**
	  * 
	  * @param vec GL_Vector -- vector to set coordinate
	  * @param num int -- coordinate to set: 0 = x; 1 = y; 2 = z
	  * @param value float --value to set coordiate (int num) to 
	  */
	 public static void setCoord(GL_Vector vec, int num, float value) {
		 switch (num) {
		 case 0: vec.x = value; break;
		 case 1: vec.y = value; break;
		 case 2: vec.z = value; break;
		 default: throw new InternalError();
		 }
	 }

	 
	 //added
	 public float getDeltaTimeMicroseconds() {
		 //#ifdef USE_BT_CLOCK
		 float dt = clock.getTimeMicroseconds();
		 //clock.reset();
		 return dt;
		 //#else
		 //return btScalar(16666.);
		 //#endif
	 }
	 
	 public void resetClock() {
		 clock.reset();
	 }

	 //added
	 /**
	  * Creates a rotation vector (quaternion) to rotate camera
	  * 
	  * @param quaternion Quat4f -- quaternion to use as rotation vector
	  * @param axis GL_Vector -- the camera's up vector
	  * @param angle Float -- angle for rotation
	  * 
	  * @see updatePan(String s) -- used to rotate camera up/down
	  */
	 public static void setRotationVector(Quat4f quaternion, GL_Vector axis, float angle) {
		 float d = axis.length();
		 assert (d != 0f);
		 float s = (float)Math.sin(angle * 0.5f) / d;
		 quaternion.set(axis.x * s, axis.y * s, axis.z * s, (float) Math.cos(angle * 0.5f));
	 }

	 //added
	 /**
	  * Updates the panning camera's position iff camera position calculation is not complete
	  * @param s String based on directional key input -- "LEFT" = KEY_LEFT, "RIGHT" = KEY_RIGHT, "UP" = KEY_UP, "DOWN" = KEY_DOWN
	  */
	 public void updatePan(String s) {
		 if(isRunning){
			 //rotate -90.0 degrees from camera's current position about the y-axis
			 if (s.equals("LEFT")) {
				 //zooms out prior to camera rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * 	(getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has camera zoom met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 //update the camera's current zoom distance and set appropriate vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //rotate the camera if zoom out is complete
				 if(!zoomOut && !zoomIn) {
					 runningDegrees = ((float) (-rotationSpeed * (getDeltaTimeMicroseconds()/1000000f))) + currRotation;

					 //has the degree of rotation met, or surpassed, its goal of 90 degrees?
					 if(runningDegrees <= prevRotation - 90.0f) {
						 runningDegrees = prevRotation - 90.0f;
						 prevRotation = runningDegrees;
						 zoomIn = true;


					 }

					 //update the camera's current degree of rotation
					 currRotation = runningDegrees;
					 //update all vectors/matrices for camera position/look-at/up 
					 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //zoom camera back in to normal viewing distance after position rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 //update the current zoom distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

			 }

			 //rotate 90 degrees from camera's current position about the y-axis
			 if (s.equals("RIGHT")) {
				 //zoom camera out before rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 
					 //update the current camera distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //rotate camera after zoom out
				 if(!zoomOut && !zoomIn){
					 runningDegrees = ((float) (rotationSpeed * (getDeltaTimeMicroseconds()/1000000f))) + currRotation;

					 //has the degree of rotation met, or surpassed, its goal of 90 degrees?
					 if(runningDegrees >= prevRotation + 90.0f) {
						 runningDegrees = prevRotation + 90.0f;
						 prevRotation = runningDegrees;
						 zoomIn = true;
					 }

					 //update the camera's current position and apply to all applicable camera vectors
					 currRotation = runningDegrees;
					 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
					 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
					 System.out.println(cameraPos);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //zoom camera back in to normal viewing distance after rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 
					 //update the current zoom distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }
			 }

			 //return camera to camera origin and rotate 90 degrees about the x-axis (top-down view)
			 if(s.equals("UP")) {
				 if(currRotation%360.0f != 0.0f) {
					 //delete
					 System.out.println("currRotation pre:" +currRotation);

					 //zoom camera out before rotation
					 if(zoomOut && !zoomIn) {
						 float currZoom = (float) (((zoomDist*2) * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
						 //has the zoom distance met, or surpassed, its goal
						 if (currZoom >= zoomDist) {
							 currZoom = zoomDist;
							 zoomOut = false;
						 }
						 
						 //update the camera's current zoom distance and apply to all applicable camera vectors
						 currCamDist = currZoom;
						 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
						 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }
					 
					 //rotate the camera after zoom out
					 if(!zoomOut && !zoomIn) {
						 //rotate 90 degrees about the y-axis if the camera is in quadrant 4
						 if(quadrant == 4) {
							 runningDegrees = ((float) ((rotationSpeedHigh*0.7f) * (getDeltaTimeMicroseconds()/1000000f) )) + currRotation;
							 //has the rotation met, or surpassed, its goal?
							 if(runningDegrees >= 0.0f) {
								 runningDegrees = 0.0f;
								 prevRotation = runningDegrees;
								 currRotation = runningDegrees;
								 quadrant = 1;

							 }
						 }

						 //rotate the camera -90 degrees about the y-axis if the camera is in any quadrant < 4
						 if(quadrant <4){
							 runningDegrees = ((float) (-rotationSpeedHigh * (getDeltaTimeMicroseconds()/1000000f) )) + currRotation;
							 //has the rotation met, or surpassed, its goal?
							 if(runningDegrees <= 0.0f) {
								 runningDegrees = 0.0f;
								 prevRotation = runningDegrees;
								 currRotation = runningDegrees;
								 quadrant = 1;

							 }
						 }
						 
						 //update the camera's current position and apply to all applicable camera vectors
						 currRotation = runningDegrees;

						 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
						 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }
				 }

				 //has the camera returned to the camera's origin?
				 if(currRotation%360.0f == 0.0f) {
					
					 //zoom out if up rotation was called from camera origin
					 if(zoomOut && !zoomIn) {
						 float currZoom = (float) (((zoomDist*2) * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
						 //has the zoom distance met, or surpassed, its goal?
						 if (currZoom >= zoomDist) {
							 currZoom = zoomDist;
							 zoomOut = false;
						 }
						 
						 //update the camera distance and apply to all applicable camera vectors
						 currCamDist = currZoom;
						 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
						 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }

					 //rotate 90 degrees about the x-axis if zoom out is complete
					 if(!zoomIn && !zoomOut) {
						 runningElevation = ((float) ((rotationSpeed) * (getDeltaTimeMicroseconds()/1000000f))) + elevation;
						 //has the rotation met, or surpassed, its goal?
						 if(runningElevation >= 90f) {
							 runningElevation = 90f;
							 elevation = runningElevation;
							 zoomIn = true;
						 }

						 //update the current elevation and apply to all applicable camera vectors/matrices
						 elevation = runningElevation;
						
						 float radianElevation = elevation * 0.01745329251994329547f; // radians per degree for elevation
						 float radianCurrRotation = 0f; //always zero, camera has returned to its position of origin any time top-down is activated
						 int forwardAxis = 2;  //always 2 for x axis rotation from camera origin -- will need to be changed if we want to change how
						 //the top-down view works

						 Quat4f rot = new Quat4f();
						 GL_Vector cameraUp = camera.UpVector;
						 setRotationVector(rot, cameraUp, radianCurrRotation);
				
						 GL_Vector eyePos = new GL_Vector(0f,0f,0f);
						
						 //changed
						 setCoord(eyePos, forwardAxis, zoomDist);
						
						 GL_Vector forward = new GL_Vector(eyePos.x, eyePos.y, eyePos.z);
						 //				if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
						 //					forward.set(1f, 0f, 0f);
						 //				}

						 Vector3f right = new Vector3f();
						 Vector3f camUp3f = new Vector3f(cameraUp.x, cameraUp.y, cameraUp.z);
						 Vector3f for3f = new Vector3f(forward.x, forward.y, forward.z);
						 right.cross(camUp3f, for3f);
						
						 Quat4f roll = new Quat4f();
						 GL_Vector rightVector = new GL_Vector(right.x, right.y, right.z);
						 setRotationVector(roll, rightVector, -radianElevation);

						 Matrix3f tmpMat1 = new Matrix3f();
						 Matrix3f tmpMat2 = new Matrix3f();
						 tmpMat1.set(rot);
						 tmpMat2.set(roll);
						 tmpMat1.mul(tmpMat2);
						 Vector3f tmpVector = new Vector3f();
						 tmpVector.set(eyePos.x, eyePos.y, eyePos.z);
						 tmpMat1.transform(tmpVector);
						 GL_Vector finalEye = new GL_Vector(tmpVector.x, tmpVector.y, tmpVector.z);

						 camera.Position = finalEye;
						 camera.UpVector = cameraUp;
						 GL_Vector view = new GL_Vector(-finalEye.x, -finalEye.y, -finalEye.z);
						 camera.ViewDir = view;

					 }

					 //zoom in after 90 degree rotation about the x-axis
					 if(!zoomOut && zoomIn) {
						 float currZoom = (float) ((-zoomDist * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
						 //has the camera met, or surpassed, its goal?
						 if (currZoom <= camDist) {
							 currZoom = camDist;
							 zoomOut = true;
							 zoomIn = false;
							 isRunning = false;
						 }
						 //update the camera distance and apply to the camera position vector
						 currCamDist = currZoom;
						 GL_Vector eyePos = new GL_Vector(camera.Position.x, currCamDist, camera.Position.z);
						 camera.Position = eyePos;
					 }
				 }

			 }

			 //rotate the camera -90 degrees about the x-axis 
			 if(s.equals("DOWN")){
				 //zoom out prior to rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has the camera zoom met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 //update the camera distance and apply to all applicable vectors
					 currCamDist = currZoom;
					 GL_Vector eyePos = new GL_Vector(camera.Position.x, currCamDist, camera.Position.z);
					 camera.Position = eyePos;
				 }

				 //rotate -90 degrees about the x-axis post zoom out
				 if(!zoomOut && ! zoomIn){
					 runningElevation = ((float) ((-rotationSpeed*0.7) * (getDeltaTimeMicroseconds()/1000000f))) + elevation;
					 //has the rotation met, or surpassed, its goal?
					 if(runningElevation <= 0f) {
						 runningElevation = 0f;
						 elevation = runningElevation;
						 zoomIn = true;
					 }

					 //update the camera's elevation and apply to all applicable vectors/matrices
					 elevation = runningElevation;
					 float radianElevation = elevation * 0.01745329251994329547f; // radians per degree for elevation
					 float radianCurrRotation = 0f; //always zero, camera has returned to its position of origin any time top-down is activated
					 int forwardAxis = 2;  //always 2 for x axis rotation from camera origin -- will need to be changed if we want to change how
					 					   //the top-down view works

					 Quat4f rot = new Quat4f();
					 GL_Vector cameraUp = camera.UpVector;
					 setRotationVector(rot, cameraUp, radianCurrRotation);
					 GL_Vector eyePos = new GL_Vector(0f,0f,0f);
					 setCoord(eyePos, forwardAxis, zoomDist);
					 GL_Vector forward = new GL_Vector(eyePos.x, eyePos.y, eyePos.z);
					 //				if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
					 //					forward.set(1f, 0f, 0f);
					 //				}

					 Vector3f right = new Vector3f();
					 Vector3f camUp3f = new Vector3f(cameraUp.x, cameraUp.y, cameraUp.z);
					 Vector3f for3f = new Vector3f(forward.x, forward.y, forward.z);
					 right.cross(camUp3f, for3f);
					 Quat4f roll = new Quat4f();
					 GL_Vector rightVector = new GL_Vector(right.x, right.y, right.z);
					 setRotationVector(roll, rightVector, -radianElevation);

					 Matrix3f tmpMat1 = new Matrix3f();
					 Matrix3f tmpMat2 = new Matrix3f();
					 tmpMat1.set(rot);
					 tmpMat2.set(roll);
					 tmpMat1.mul(tmpMat2);
					 Vector3f tmpVector = new Vector3f();
					 tmpVector.set(eyePos.x, eyePos.y, eyePos.z);
					 tmpMat1.transform(tmpVector);
					 GL_Vector finalEye = new GL_Vector(tmpVector.x, tmpVector.y, tmpVector.z);
					 camera.Position = finalEye;
					 camera.UpVector = cameraUp;
					 GL_Vector view = new GL_Vector(-finalEye.x, -finalEye.y, -finalEye.z);
					 camera.ViewDir = view;

				 }
				 
				 //zoom in after x-axis rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (getDeltaTimeMicroseconds()/1000000f)) + currCamDist);
					 //has the zoom met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 
					 //update the current distance and apply to the camera position vector
					 currCamDist = currZoom;
					 GL_Vector eyePos = new GL_Vector(camera.Position.x, camera.Position.y, currCamDist);
					 camera.Position = eyePos;
				 }
			 }			 
		 }
	 }

	 
	 public void updatePanOld(String s) {
		 if(isRunning){
			 //rotate -90.0 degrees from camera's current position about the y-axis
			 if (s.equals("LEFT")) {
				 //zooms out prior to camera rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * 	(GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has camera zoom met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 //update the camera's current zoom distance and set appropriate vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //rotate the camera if zoom out is complete
				 if(!zoomOut && !zoomIn) {
					 runningDegrees = ((float) (-rotationSpeed * (GLApp.getSecondsSinceLastFrame()))) + currRotation;

					 //has the degree of rotation met, or surpassed, its goal of 90 degrees?
					 if(runningDegrees <= prevRotation - 90.0f) {
						 runningDegrees = prevRotation - 90.0f;
						 prevRotation = runningDegrees;
						 zoomIn = true;


					 }

					 //update the camera's current degree of rotation
					 currRotation = runningDegrees;
					 //update all vectors/matrices for camera position/look-at/up 
					 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //zoom camera back in to normal viewing distance after position rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 //update the current zoom distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

			 }

			 //rotate 90 degrees from camera's current position about the y-axis
			 if (s.equals("RIGHT")) {
				 //zoom camera out before rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 
					 //update the current camera distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //rotate camera after zoom out
				 if(!zoomOut && !zoomIn){
					 runningDegrees = ((float) (rotationSpeed * (GLApp.getSecondsSinceLastFrame()))) + currRotation;

					 //has the degree of rotation met, or surpassed, its goal of 90 degrees?
					 if(runningDegrees >= prevRotation + 90.0f) {
						 runningDegrees = prevRotation + 90.0f;
						 prevRotation = runningDegrees;
						 zoomIn = true;
					 }

					 //update the camera's current position and apply to all applicable camera vectors
					 currRotation = runningDegrees;
					 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
					 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
					 System.out.println(cameraPos);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }

				 //zoom camera back in to normal viewing distance after rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has the zoom distance met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 
					 //update the current zoom distance and apply to all applicable camera vectors
					 currCamDist = currZoom;
					 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
					 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
					 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
					 camera.viewDir( camDir );
					 float apRot = (float) 90.0;             
					 camera.RotateY(apRot);
				 }
			 }

			 //return camera to camera origin and rotate 90 degrees about the x-axis (top-down view)
			 if(s.equals("UP")) {
				 if(currRotation%360.0f != 0.0f) {
					 //delete
					 System.out.println("currRotation pre:" +currRotation);

					 //zoom camera out before rotation
					 if(zoomOut && !zoomIn) {
						 float currZoom = (float) (((zoomDist*2) * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
						 //has the zoom distance met, or surpassed, its goal
						 if (currZoom >= zoomDist) {
							 currZoom = zoomDist;
							 zoomOut = false;
						 }
						 
						 //update the camera's current zoom distance and apply to all applicable camera vectors
						 currCamDist = currZoom;
						 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
						 camera.MoveTo(cameraPos.x, cameraPos.y+0f, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }
					 
					 //rotate the camera after zoom out
					 if(!zoomOut && !zoomIn) {
						 //rotate 90 degrees about the y-axis if the camera is in quadrant 4
						 if(quadrant == 4) {
							 runningDegrees = ((float) ((rotationSpeedHigh*0.7f) * (GLApp.getSecondsSinceLastFrame()) )) + currRotation;
							 //has the rotation met, or surpassed, its goal?
							 if(runningDegrees >= 0.0f) {
								 runningDegrees = 0.0f;
								 prevRotation = runningDegrees;
								 currRotation = runningDegrees;
								 quadrant = 1;

							 }
						 }

						 //rotate the camera -90 degrees about the y-axis if the camera is in any quadrant < 4
						 if(quadrant <4){
							 runningDegrees = ((float) (-rotationSpeedHigh * (GLApp.getSecondsSinceLastFrame()) )) + currRotation;
							 //has the rotation met, or surpassed, its goal?
							 if(runningDegrees <= 0.0f) {
								 runningDegrees = 0.0f;
								 prevRotation = runningDegrees;
								 currRotation = runningDegrees;
								 quadrant = 1;

							 }
						 }
						 
						 //update the camera's current position and apply to all applicable camera vectors
						 currRotation = runningDegrees;

						 GL_Vector cameraPos = GL_Vector.rotationVector(runningDegrees).mult(zoomDist);
						 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }
				 }

				 //has the camera returned to the camera's origin?
				 if(currRotation%360.0f == 0.0f) {
					
					 //zoom out if up rotation was called from camera origin
					 if(zoomOut && !zoomIn) {
						 float currZoom = (float) (((zoomDist*2) * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
						 //has the zoom distance met, or surpassed, its goal?
						 if (currZoom >= zoomDist) {
							 currZoom = zoomDist;
							 zoomOut = false;
						 }
						 
						 //update the camera distance and apply to all applicable camera vectors
						 currCamDist = currZoom;
						 GL_Vector cameraPos = GL_Vector.rotationVector(currRotation).mult(currZoom);
						 camera.MoveTo(cameraPos.x, cameraPos.y, cameraPos.z);
						 GL_Vector camDir = GL_Vector.crossProduct(new GL_Vector(0f,1f,0f),cameraPos);
						 camera.viewDir( camDir );
						 float apRot = (float) 90.0;             
						 camera.RotateY(apRot);
					 }

					 //rotate 90 degrees about the x-axis if zoom out is complete
					 if(!zoomIn && !zoomOut) {
						 runningElevation = ((float) ((rotationSpeed) * (GLApp.getSecondsSinceLastFrame()))) + elevation;
						 //has the rotation met, or surpassed, its goal?
						 if(runningElevation >= 90f) {
							 runningElevation = 90f;
							 elevation = runningElevation;
							 zoomIn = true;
						 }

						 //update the current elevation and apply to all applicable camera vectors/matrices
						 elevation = runningElevation;
						
						 float radianElevation = elevation * 0.01745329251994329547f; // radians per degree for elevation
						 float radianCurrRotation = 0f; //always zero, camera has returned to its position of origin any time top-down is activated
						 int forwardAxis = 2;  //always 2 for x axis rotation from camera origin -- will need to be changed if we want to change how
						 //the top-down view works

						 Quat4f rot = new Quat4f();
						 GL_Vector cameraUp = camera.UpVector;
						 setRotationVector(rot, cameraUp, radianCurrRotation);
				
						 GL_Vector eyePos = new GL_Vector(0f,0f,0f);
						
						 //changed
						 setCoord(eyePos, forwardAxis, zoomDist);
						
						 GL_Vector forward = new GL_Vector(eyePos.x, eyePos.y, eyePos.z);
						 //				if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
						 //					forward.set(1f, 0f, 0f);
						 //				}

						 Vector3f right = new Vector3f();
						 Vector3f camUp3f = new Vector3f(cameraUp.x, cameraUp.y, cameraUp.z);
						 Vector3f for3f = new Vector3f(forward.x, forward.y, forward.z);
						 right.cross(camUp3f, for3f);
						
						 Quat4f roll = new Quat4f();
						 GL_Vector rightVector = new GL_Vector(right.x, right.y, right.z);
						 setRotationVector(roll, rightVector, -radianElevation);

						 Matrix3f tmpMat1 = new Matrix3f();
						 Matrix3f tmpMat2 = new Matrix3f();
						 tmpMat1.set(rot);
						 tmpMat2.set(roll);
						 tmpMat1.mul(tmpMat2);
						 Vector3f tmpVector = new Vector3f();
						 tmpVector.set(eyePos.x, eyePos.y, eyePos.z);
						 tmpMat1.transform(tmpVector);
						 GL_Vector finalEye = new GL_Vector(tmpVector.x, tmpVector.y, tmpVector.z);

						 camera.Position = finalEye;
						 camera.UpVector = cameraUp;
						 GL_Vector view = new GL_Vector(-finalEye.x, -finalEye.y, -finalEye.z);
						 camera.ViewDir = view;

					 }

					 //zoom in after 90 degree rotation about the x-axis
					 if(!zoomOut && zoomIn) {
						 float currZoom = (float) ((-zoomDist * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
						 //has the camera met, or surpassed, its goal?
						 if (currZoom <= camDist) {
							 currZoom = camDist;
							 zoomOut = true;
							 zoomIn = false;
							 isRunning = false;
						 }
						 //update the camera distance and apply to the camera position vector
						 currCamDist = currZoom;
						 GL_Vector eyePos = new GL_Vector(camera.Position.x, currCamDist, camera.Position.z);
						 camera.Position = eyePos;
					 }
				 }

			 }

			 //rotate the camera -90 degrees about the x-axis 
			 if(s.equals("DOWN")){
				 //zoom out prior to rotation
				 if(zoomOut && !zoomIn) {
					 float currZoom = (float) (((zoomDist*2) * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has the camera zoom met, or surpassed, its goal?
					 if (currZoom >= zoomDist) {
						 currZoom = zoomDist;
						 zoomOut = false;
					 }
					 //update the camera distance and apply to all applicable vectors
					 currCamDist = currZoom;
					 GL_Vector eyePos = new GL_Vector(camera.Position.x, currCamDist, camera.Position.z);
					 camera.Position = eyePos;
				 }

				 //rotate -90 degrees about the x-axis post zoom out
				 if(!zoomOut && ! zoomIn){
					 runningElevation = ((float) ((-rotationSpeed*0.7) * (GLApp.getSecondsSinceLastFrame()))) + elevation;
					 //has the rotation met, or surpassed, its goal?
					 if(runningElevation <= 0f) {
						 runningElevation = 0f;
						 elevation = runningElevation;
						 zoomIn = true;
					 }

					 //update the camera's elevation and apply to all applicable vectors/matrices
					 elevation = runningElevation;
					 float radianElevation = elevation * 0.01745329251994329547f; // radians per degree for elevation
					 float radianCurrRotation = 0f; //always zero, camera has returned to its position of origin any time top-down is activated
					 int forwardAxis = 2;  //always 2 for x axis rotation from camera origin -- will need to be changed if we want to change how
					 					   //the top-down view works

					 Quat4f rot = new Quat4f();
					 GL_Vector cameraUp = camera.UpVector;
					 setRotationVector(rot, cameraUp, radianCurrRotation);
					 GL_Vector eyePos = new GL_Vector(0f,0f,0f);
					 setCoord(eyePos, forwardAxis, zoomDist);
					 GL_Vector forward = new GL_Vector(eyePos.x, eyePos.y, eyePos.z);
					 //				if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
					 //					forward.set(1f, 0f, 0f);
					 //				}

					 Vector3f right = new Vector3f();
					 Vector3f camUp3f = new Vector3f(cameraUp.x, cameraUp.y, cameraUp.z);
					 Vector3f for3f = new Vector3f(forward.x, forward.y, forward.z);
					 right.cross(camUp3f, for3f);
					 Quat4f roll = new Quat4f();
					 GL_Vector rightVector = new GL_Vector(right.x, right.y, right.z);
					 setRotationVector(roll, rightVector, -radianElevation);

					 Matrix3f tmpMat1 = new Matrix3f();
					 Matrix3f tmpMat2 = new Matrix3f();
					 tmpMat1.set(rot);
					 tmpMat2.set(roll);
					 tmpMat1.mul(tmpMat2);
					 Vector3f tmpVector = new Vector3f();
					 tmpVector.set(eyePos.x, eyePos.y, eyePos.z);
					 tmpMat1.transform(tmpVector);
					 GL_Vector finalEye = new GL_Vector(tmpVector.x, tmpVector.y, tmpVector.z);
					 camera.Position = finalEye;
					 camera.UpVector = cameraUp;
					 GL_Vector view = new GL_Vector(-finalEye.x, -finalEye.y, -finalEye.z);
					 camera.ViewDir = view;

				 }
				 
				 //zoom in after x-axis rotation
				 if(!zoomOut && zoomIn) {
					 float currZoom = (float) ((-zoomDist * (GLApp.getSecondsSinceLastFrame())) + currCamDist);
					 //has the zoom met, or surpassed, its goal?
					 if (currZoom <= camDist) {
						 currZoom = camDist;
						 zoomOut = true;
						 zoomIn = false;
						 isRunning = false;
					 }
					 
					 //update the current distance and apply to the camera position vector
					 currCamDist = currZoom;
					 GL_Vector eyePos = new GL_Vector(camera.Position.x, camera.Position.y, currCamDist);
					 camera.Position = eyePos;
				 }
			 }			 
		 }
	 }
	 
	 /**
	  * Called to handle camera controls iff a camera operation is not already in progress
	  */
	 public void handleRotKeysPan() {
		 //handles these keys iff camera is not already running and the camera is not in top-down view
		 if(!isRunning && !up){
			 //key press to handle -90 degree rotation about the y-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
				 isRunning = true;
				 direction = "LEFT";
				 String s = "MINUS";
				 updateQuadrant(s);
				 prevRotation = currRotation;
				 updatePan(direction);
			 }

			 //key press to handle 90 degree rotation about the y-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
				 isRunning = true;
				 direction = "RIGHT";
				 String s = "PLUS";
				 updateQuadrant(s);
				 prevRotation = currRotation;
				 updatePan(direction);
			 }

			 //key press to handle return to camera origin and 90 rotation about the x-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				 up = true;
				 if(quadrant == 4) {
					 currRotation = -90.0f;
				 }
				 if(quadrant == 3) {
					 currRotation = 180f;
				 }
				 if(quadrant == 2) {
					 currRotation = 90f;
				 }
				 if(quadrant == 1) {
					 currRotation = 0f;
				 }
				 prevRotation = currRotation;
				 isRunning = true;
				 direction = "UP";
				 updatePan(direction);
			 }
		 }
		 
		 //handles these keys iff the camera is not already running and top-down view is current camera view
		 if (!isRunning && up) { 
			 //key press to rotate camera -90 degrees about the x-axis, returning camera to origin
			 if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				 isRunning = true;
				 up = false;
				 quadrant = 1;
				 direction = "DOWN";
				 updatePan(direction);
			 }
		 }

	 }

	 
	 public void handleRotKeysPanOld() {
		 //handles these keys iff camera is not already running and the camera is not in top-down view
		 if(!isRunning && !up){
			 //key press to handle -90 degree rotation about the y-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
				 isRunning = true;
				 direction = "LEFT";
				 String s = "MINUS";
				 updateQuadrant(s);
				 prevRotation = currRotation;
				 updatePanOld(direction);
			 }

			 //key press to handle 90 degree rotation about the y-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
				 isRunning = true;
				 direction = "RIGHT";
				 String s = "PLUS";
				 updateQuadrant(s);
				 prevRotation = currRotation;
				 updatePanOld(direction);
			 }

			 //key press to handle return to camera origin and 90 rotation about the x-axis
			 if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				 up = true;
				 if(quadrant == 4) {
					 currRotation = -90.0f;
				 }
				 if(quadrant == 3) {
					 currRotation = 180f;
				 }
				 if(quadrant == 2) {
					 currRotation = 90f;
				 }
				 if(quadrant == 1) {
					 currRotation = 0f;
				 }
				 prevRotation = currRotation;
				 isRunning = true;
				 direction = "UP";
				 updatePanOld(direction);
			 }
		 }
		 
		 //handles these keys iff the camera is not already running and top-down view is current camera view
		 if (!isRunning && up) { 
			 //key press to rotate camera -90 degrees about the x-axis, returning camera to origin
			 if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				 isRunning = true;
				 up = false;
				 quadrant = 1;
				 direction = "DOWN";
				 updatePanOld(direction);
			 }
		 }

	 }
	 
	/**
	 * Create a GLCameraMover with the camera that it will be moving.
	 *
	 * @param cam
	 */
	public void setCamera(GLCamera cam) {
		camera = cam;
	}
	


	/**
	 * Tell camera to adjust modelview matrix to new view position and orientation.
	 *
	 */
	public void render() {
		 
		 camera.Render();
	 }
}

