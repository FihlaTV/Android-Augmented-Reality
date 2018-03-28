package edu.rit.ad8454.capstonerit;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.*;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.rit.ad8454.capstonerit.Graphics.CVDraw;
import edu.rit.ad8454.capstonerit.Graphics.GLRenderer;

public class CameraActivity extends Activity implements CvCameraViewListener2{

    // Make global and reuse OpenCV instances like Mat because creating new objects is expensive

    private static final String TAG = "CameraActivity";
    private JavaCameraView mOpenCvCameraView;
    private int camDim[] = {640, 480};          // TODO: higher resolution
    private Mat imgFrame;
    private Mat imgFrameBlur;
    private Tracker tracker;
    private CVDraw cvDraw;
    private boolean skipFrame = false;
    private List <Point> boxCorners;
    //private GLRenderer myGLRenderer;
    //private float offsetFactX, offsetFactY;
    //private float scaleFactX, scaleFactY;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.opencv_camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(camDim[0], camDim[1]);

        // set up OpenGL view
        /*
        GLSurfaceView myGLView = new GLSurfaceView(this);
        myGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        myGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //myGLRenderer = new GLRenderer();
        //myGLView.setRenderer(myGLRenderer);
        addContentView(myGLView, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT));
        myGLView.setZOrderMediaOverlay(true);
        //myGLRenderer.setVidDim(camDim[0], camDim[1]);
        */
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        imgFrame = new Mat(height, width, CvType.CV_8UC1);
        imgFrameBlur = new Mat(height, width, CvType.CV_8UC1);
        tracker = new Tracker();
        cvDraw = new CVDraw();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        imgFrame = inputFrame.gray();

        if(!tracker.hasReferenceFeatures) {
            return inputFrame.rgba();
        }

        if(skipFrame) {
            imgFrame = inputFrame.rgba();
            cvDraw.drawCube(imgFrame, boxCorners);
        }

        imgFrameBlur = imgFrame.clone();
        //Imgproc.GaussianBlur(imgFrameBlur, imgFrameBlur, new Size(5, 5), 3);
        if (! tracker.computePerspectiveCorners(imgFrameBlur)) {
            return inputFrame.rgba();
        }

        imgFrame = inputFrame.rgba();
        boxCorners = tracker.getProjectedPoints();
        Log.e("hang", "drawCube");
        cvDraw.drawCube(imgFrame, boxCorners);
        Log.e("hang", "done");

        skipFrame = !skipFrame;
        return imgFrame;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!tracker.hasReferenceFeatures) {
            //Imgproc.GaussianBlur(imgFrame, imgFrame, new Size(5, 5), 3);
            tracker.setReferenceFeatures(imgFrame);
        }
        return super.onTouchEvent(event);
    }
}
