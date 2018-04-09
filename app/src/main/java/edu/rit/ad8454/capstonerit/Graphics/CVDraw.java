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
    Scalar sideColor = new Scalar(250, 0, 0);
    List<MatOfPoint> temp = new ArrayList<>();

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

        /*
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
        */

        temp.clear();

        /*
        boxSide.fromList(getSubListByIdx(new int[]{3, 2, 6, 7}, boxCorners));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, sideColor, 2, Core.LINE_AA, 0);
        boxSide.fromList(getSubListByIdx(new int[]{2, 1, 5, 6}, boxCorners));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, sideColor, 2, Core.LINE_AA, 0);
        boxSide.fromList(getSubListByIdx(new int[]{1, 0, 4, 5}, boxCorners));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, sideColor, 2, Core.LINE_AA, 0);
        boxSide.fromList(getSubListByIdx(new int[]{0, 3, 7, 4}, boxCorners));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, sideColor, 2, Core.LINE_AA, 0);
        boxSide.fromList(boxCorners.subList(4, 8));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, new Scalar(0, 255, 0), 2, Core.LINE_AA, 0);
        */

        boxSide.fromList(getSubListByIdx(new int[]{0, 1, 2, 3}, boxCorners));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, new Scalar(0, 0, 255), 2, Core.LINE_AA, 0);
        Imgproc.line(image, boxCorners.get(0), boxCorners.get(4), sideColor, 2, Core.LINE_AA, 0);
        Imgproc.line(image, boxCorners.get(1), boxCorners.get(5), sideColor, 2, Core.LINE_AA, 0);
        Imgproc.line(image, boxCorners.get(2), boxCorners.get(6), sideColor, 2, Core.LINE_AA, 0);
        Imgproc.line(image, boxCorners.get(3), boxCorners.get(7), sideColor, 2, Core.LINE_AA, 0);
        boxSide.fromList(boxCorners.subList(4, 8));
        temp.add(boxSide);
        Imgproc.polylines(image, temp, true, new Scalar(0, 255, 0), 2, Core.LINE_AA, 0);
    }

    private List<Point> getSubListByIdx(int[] allIdx, List<Point> boxCorners) {
        points.clear();
        for(int idx: allIdx) {
            points.add(boxCorners.get(idx));
        }
        return points;
    }
}
