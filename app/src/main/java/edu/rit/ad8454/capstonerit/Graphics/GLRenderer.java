package edu.rit.ad8454.capstonerit.Graphics;

/**
 * Created by Ajinkya on 3/6/2018.
 */

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import edu.rit.ad8454.capstonerit.Sensors;

public class GLRenderer implements GLSurfaceView.Renderer {

    // global variables for cube parameters
    private Cube mCube = new Cube();
    private float mCubeRotation;

    private GL10 gl;

    private boolean renderCube = false;
    private float XScale = 0;
    private float YScale = 0;
    private float posX = 0;
    private float posY = 0;
    private int vidWidth = 0;
    private int vidHeight = 0;
    private float cubeSize = 1.0f;
    private double initSizeLength = -1;
    private double rotSpeed = 0.250f;
    private Sensors sensors;
    private float[] sensorRotationMatrix;    // make private
    private float[] poseMatrix = new float[16];
    float[] mProjectionGL = new float[16];
    int mSurfaceWidth = 0;
    int mSurfaceHeight = 0;

    public GLRenderer(Sensors sensors) {
        this.sensors = sensors;
    }


    public void setPoseMatrix(float[] poseMatrix) {
        sensorRotationMatrix = poseMatrix;
        //this.poseMatrix = poseMatrix;
    }

    public void setProjectionMatrix() {
        float mFOVY = 4.5f;
        float mFOVX = 6.0f;
        int mHeightPx = 480;
        int mWidthPx = 640;
        float mNear = 0.1f;
        float mFar = 10f;
        float right = (float)Math.tan(0.5f * mFOVX * Math.PI / 180f) * mNear;
        float aspectRatio = (float)mWidthPx / (float)mHeightPx;
        float top = right / aspectRatio;

        Matrix.frustumM(mProjectionGL, 0,
                -right, right, -top, top, mNear, mFar);
    }


    /**
     * Method to set cube rotation speed
     */
    public void setCubeRotation(int val){
        rotSpeed = 0.250f * val*2;
        Log.e("Cube speed", rotSpeed+"");
    }


    /**
     * Method to set cube scale size
     */
    public void setCubeSize(double sizeLength){
        if(initSizeLength < 0)
            initSizeLength = sizeLength;
        cubeSize = (float) ((sizeLength/initSizeLength));
    }


    /**
     * Method to set cube visibility
     */
    public void setRenderCube(boolean val){
        renderCube = val;
    }


    /**
     * Method to set frame dimensions
     */
    public void setVidDim(int w, int h){
        vidWidth = w;
        vidHeight = h;
        XScale = 8.0f * 2 / vidWidth;
        YScale = 8.0f * 2 / vidHeight;
    }


    /**
     * Method to set cube position
     */
    public void setPos(double x, double y){
        x -= vidWidth/2;
        y -= vidHeight/2;
        posX = (float) x * XScale;
        posY = (float) y * -YScale;
        renderCube = true;
    }


    /**
     * Method to render cube according to parameters
     */
    public void onDrawFrame(GL10 gl) {
        Log.e("cube", renderCube + "");
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        if(!renderCube) {
            return;
        }

        //float[] debug = sensors.getSensorRotationMatrix();

        //sensorRotationMatrix = sensors.getSensorRotationMatrix();

        /*
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3.0f);
        gl.glMultMatrixf(sensorRotationMatrix, 0);
        mCube.draw(gl);
        */


        gl.glLoadIdentity();
        gl.glMultMatrixf(sensorRotationMatrix, 0);
        gl.glTranslatef(0f, 0f, -10f);
        gl.glScalef(cubeSize, cubeSize, cubeSize);
        mCube.draw(gl);
    }


    /**
     * Method called initially on surface creation
     */
    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        this.gl = gl;
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //gl.glEnable(GL10.GL_CULL_FACE);
        gl.glDisable(GL10.GL_DITHER);

        /*
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);

        */

    }


    /**
     * Method called everytime surface changes.
     * Ideally should never be called as surface should not change.
     */
    public void onSurfaceChanged( GL10 gl, int width, int height ) {

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        /*
        gl.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 100);
        */
    }
}