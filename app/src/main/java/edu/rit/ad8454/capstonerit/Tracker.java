package edu.rit.ad8454.capstonerit;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ajinkya on 3/4/2018.
 */

public class Tracker {

    boolean hasReferenceFeatures = false;
    private List<Point> listOfPoints = new LinkedList<>();

    private final int MIN_MATCH_NUM = 180;
    private final MatOfDouble CALIB_MAT = new MatOfDouble();
    private final MatOfDouble DIST = new MatOfDouble(0.262383, -0.953104, -0.005358, 0.002628, 1.163314);

    private ORB orb;        // try others
    private FlannBasedMatcher flann;
    private BFMatcher bfMatcher;

    private MatOfKeyPoint refKeyPoints = new MatOfKeyPoint();
    private MatOfPoint2f matchedRefKeyPoints = new MatOfPoint2f();
    private Mat refDescriptors = new Mat();

    private MatOfKeyPoint targetKeyPoints = new MatOfKeyPoint();
    private MatOfPoint2f matchedTargetKeyPoints = new MatOfPoint2f();
    private Mat targetDescriptors = new Mat();

    private MatOfDMatch matches = new MatOfDMatch();
    private Mat refImageCornersMat2D = new Mat(4, 1, CvType.CV_32FC2);
    private MatOfPoint3f refImageCornersMat3D = new MatOfPoint3f();
    private MatOfPoint3f graphicsAxisCornersMat3D = new MatOfPoint3f();
    private MatOfPoint2f perspectiveCornersMatOfPoint2f;
    private Mat perspectiveCornersMat = new Mat();
    private MatOfPoint2f projectedPoints = new MatOfPoint2f();
    private MatOfPoint projectedPointsForConvexTest = new MatOfPoint();
    private Mat mask = new Mat();
    private Mat homography;         // check if needs to be released
    private MatOfDouble rvec = new MatOfDouble();
    private MatOfDouble tvec = new MatOfDouble();
    private MatOfDouble rotationMat = new MatOfDouble();
    private final float[] poseMatrix = new float[16];

    Tracker() {
        orb = ORB.create();
        //flann = FlannBasedMatcher.create();
        bfMatcher = BFMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT, true);
        createCalibMat();
    }

    private void createCalibMat() {
        CALIB_MAT.create(3, 3, CvType.CV_64FC1);
        CALIB_MAT.put(0, 0, 517.306408);
        CALIB_MAT.put(0, 1, 0.0);
        CALIB_MAT.put(0, 2, 318.643040);
        CALIB_MAT.put(1, 0, 0.0);
        CALIB_MAT.put(1, 1, 516.469215);
        CALIB_MAT.put(1, 2, 255.313989);
        CALIB_MAT.put(2, 0, 0.0);
        CALIB_MAT.put(2, 1, 0.0);
        CALIB_MAT.put(2, 2, 1.0);
    }

    void setReferenceFeatures(Mat refImage) {
        orb.detectAndCompute(refImage, mask, refKeyPoints, refDescriptors);
        setRefImageCorners(refImage.size());
        setAxisCorners(refImage.size());
        hasReferenceFeatures = true;
    }

    private void setRefImageCorners(Size refSize) {
        refImageCornersMat2D.put(0, 0, new double[] {0, 0});
        refImageCornersMat2D.put(1, 0, new double[] {refSize.width, 0});
        refImageCornersMat2D.put(2, 0, new double[] {refSize.width, refSize.height});
        refImageCornersMat2D.put(3, 0, new double[] {0, refSize.height});

        refImageCornersMat3D.fromArray(
                new Point3(0, 0,0),
                new Point3(refSize.width, 0, 0),
                new Point3(refSize.width, refSize.height, 0),
                new Point3(0, refSize.height, 0)
        );
    }

    /**
     * Assumes landscape. So width > height
     */
    private void setAxisCorners(Size refSize) {
        double offset = (refSize.width - refSize.height) / 2;
        double oneThird = refSize.height / 3;

        double x0 = offset + oneThird;
        double x1 = refSize.width - offset - oneThird;
        double y0 = oneThird;
        double y1 = refSize.height - oneThird;
        double z = -oneThird;
        graphicsAxisCornersMat3D.fromArray(
                new Point3(x0, y0, 0),
                new Point3(x1, y0, 0),
                new Point3(x1, y1, 0),
                new Point3(x0, y1, 0),
                new Point3(x0, y0, z),
                new Point3(x1, y0, z),
                new Point3(x1, y1, z),
                new Point3(x0, y1, z)
        );
    }

    boolean computePerspectiveCorners(Mat targetImage) {
        Log.e("hang", "detectAndCompute");
        orb.detectAndCompute(targetImage, mask, targetKeyPoints, targetDescriptors);
        Log.e("hang", "match");
        bfMatcher.match(targetDescriptors, refDescriptors, matches);    // method signature is opposite for Python!

        Log.e("hang", "isMatched");
        boolean isMatched = setMatchingKeyPoints();
        if (!isMatched) {
            return false;
        }

        Log.e("hang", "homography");
        homography = Calib3d.findHomography(matchedRefKeyPoints, matchedTargetKeyPoints, Calib3d.RANSAC, 5.0);
        Log.e("hang", "perspectiveTransform");
        Core.perspectiveTransform(refImageCornersMat2D, perspectiveCornersMat, homography);

        return true;
    }

    List<Point> getProjectedPoints() {
        perspectiveCornersMatOfPoint2f = new MatOfPoint2f(perspectiveCornersMat);
        Log.e("hang", "solvePnP");
        Calib3d.solvePnP(refImageCornersMat3D, perspectiveCornersMatOfPoint2f, CALIB_MAT, DIST, rvec, tvec);
        Log.e("hang", "projectPoints");
        Calib3d.projectPoints(graphicsAxisCornersMat3D, rvec, tvec, CALIB_MAT, DIST, projectedPoints);

        perspectiveCornersMatOfPoint2f.release();
        projectedPointsForConvexTest.fromList(projectedPoints.toList().subList(0, 4));
        if(!Imgproc.isContourConvex(projectedPointsForConvexTest)){
            return null;
        }
        projectedPointsForConvexTest.fromList(projectedPoints.toList().subList(4, 8));
        if(!Imgproc.isContourConvex(projectedPointsForConvexTest)){
            return null;
        }
        return projectedPoints.toList();
    }

    float[] getPoseMatrix() {
        rvec.put(0, 0, rvec.get(0, 0)[0] * -1.0);
        Calib3d.Rodrigues(rvec, rotationMat);

        poseMatrix[0] = (float) rotationMat.get(0, 0)[0];
        poseMatrix[1] = (float) rotationMat.get(0, 1)[0];
        poseMatrix[2] = (float) rotationMat.get(0, 2)[0];
        poseMatrix[3] = 0f;
        poseMatrix[4] = (float)rotationMat.get(1, 0)[0];
        poseMatrix[5] = (float)rotationMat.get(1, 1)[0];
        poseMatrix[6] = (float)rotationMat.get(1, 2)[0];
        poseMatrix[7] = 0f;
        poseMatrix[8] = (float)rotationMat.get(2, 0)[0];
        poseMatrix[9] = (float)rotationMat.get(2, 1)[0];
        poseMatrix[10] = (float)rotationMat.get(2, 2)[0];
        poseMatrix[11] = 0f;
        poseMatrix[12] = (float)tvec.get(0, 0)[0];
        poseMatrix[13] = -(float)tvec.get(1, 0)[0];
        poseMatrix[14] = -(float)tvec.get(2, 0)[0];
        poseMatrix[15] = 1f;

        return poseMatrix;
    }

    private boolean setMatchingKeyPoints() {
        List<DMatch> matchesAsList = matches.toList();

        Log.e("isMatched", matchesAsList.size()+"");
        if(matchesAsList.size() < MIN_MATCH_NUM){
            return false;
        }

        LinkedList<Point> refList = new LinkedList<>();
        LinkedList<Point> targetList = new LinkedList<>();
        for(DMatch match: matchesAsList){
            refList.addLast(refKeyPoints.toList().get(match.trainIdx).pt);
            targetList.addLast(targetKeyPoints.toList().get(match.queryIdx).pt);
        }
        matchedRefKeyPoints.fromList(refList);
        matchedTargetKeyPoints.fromList(targetList);

        return true;
    }
}
