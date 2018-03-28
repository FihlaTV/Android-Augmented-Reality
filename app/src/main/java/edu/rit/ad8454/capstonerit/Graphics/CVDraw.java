package edu.rit.ad8454.capstonerit.Graphics;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajinkya on 3/6/2018.
 */

public class CVDraw {

    MatOfPoint boxSide = new MatOfPoint();
    List<Point> points = new ArrayList<>(4);
    Scalar sideColor = new Scalar(250, 250, 250, 127);

    public void drawBorder (Mat image, Point[] corners, Scalar color) {
        Imgproc.line(image, corners[0], corners[1], color, 4);
        Imgproc.line(image, corners[1], corners[2], color, 4);
        Imgproc.line(image, corners[2], corners[3], color, 4);
        Imgproc.line(image, corners[3], corners[0], color, 4);
    }

    public void drawCube (Mat image, List<Point> boxCorners) {
        if (boxCorners == null) {
            return;
        }

        boxSide.fromList(getSubListByIdx(new int[]{3, 2, 6, 7}, boxCorners));
        Imgproc.fillConvexPoly(image, boxSide, sideColor, Core.LINE_AA, 0);

        boxSide.fromList(getSubListByIdx(new int[]{2, 1, 5, 6}, boxCorners));
        Imgproc.fillConvexPoly(image, boxSide, sideColor, Core.LINE_AA, 0);

        boxSide.fromList(getSubListByIdx(new int[]{1, 0, 4, 5}, boxCorners));
        Imgproc.fillConvexPoly(image, boxSide, sideColor, Core.LINE_AA, 0);

        boxSide.fromList(getSubListByIdx(new int[]{0, 3, 7, 4}, boxCorners));
        Imgproc.fillConvexPoly(image, boxSide, sideColor, Core.LINE_AA, 0);

        boxSide.fromList(boxCorners.subList(4, 8));
        Imgproc.fillConvexPoly(image, boxSide, new Scalar(0, 127, 0), Core.LINE_AA, 0);
    }

    private List<Point> getSubListByIdx(int[] allIdx, List<Point> boxCorners) {
        points.clear();
        for(int idx: allIdx) {
            points.add(boxCorners.get(idx));
        }
        return points;
    }
}
