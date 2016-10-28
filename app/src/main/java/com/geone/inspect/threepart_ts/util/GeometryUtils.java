package com.geone.inspect.threepart_ts.util;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

public class GeometryUtils {

	public GeometryUtils() {
		// TODO Auto-generated constructor stub
	}

	/** 根据point和radius构建Polygon */
	public static Polygon getCircle(Point center, double radius) {
		Polygon polygon = new Polygon();
		getCircle(center, radius, polygon);
		return polygon;
	}

	private static void getCircle(Point center, double radius, Polygon circle) {
		circle.setEmpty();
		Point[] points = getPoints(center, radius);
		circle.startPath(points[0]);
		for (int i = 1; i < points.length; i++)
			circle.lineTo(points[i]);
	}

	private static Point[] getPoints(Point center, double radius) {
		Point[] points = new Point[50];
		double sin;
		double cos;
		double x;
		double y;
		for (double i = 0; i < 50; i++) {
			sin = Math.sin(Math.PI * 2 * i / 50);
			cos = Math.cos(Math.PI * 2 * i / 50);
			x = center.getX() + radius * sin;
			y = center.getY() + radius * cos;
			points[(int) i] = new Point(x, y);
		}
		return points;
	}

	// Point----
	private static String formatPointXY(Point p) {
		return p.getX() + " " + p.getY();
	}

	private static String formatPointXYZ(Point p) {
		return p.getX() + " " + p.getY() + " 0";
	}

	/** 判断Point是否为空 */
	public static boolean isPointEmpty(Point point) {
		if (point == null || point.isEmpty()) {
			return true;
		}
		return false;
	}

	/** 判断shapeStr是否为Point */
	public static boolean isPoint(String shapeStr) {
		if (shapeStr == null || shapeStr.isEmpty()) {
			return false;
		}
		if (shapeStr.contains("POINT")) {
			return true;
		}
		return false;
	}

	/** 判断geometry是否为Point */
	public static boolean isPoint(Geometry geometry) {
		if (geometry == null) {
			return false;
		}
		if (Geometry.isPoint(geometry.getType().value())) {
			return true;
		}
		return false;
	}

	// Polyline----
	public static String parsePolylineToString(Polyline polyline) {
		String tempStr = "LINESTRING (";
		for (int i = 0; i < polyline.getPointCount(); i++) {
			tempStr += formatPointXYZ(polyline.getPoint(i));
			if (i == polyline.getPointCount() - 1) {
				continue;
			}
			tempStr += ",";
		}

		tempStr += ")";

		return tempStr;
	}

	public static Polyline parseStringToPolyline(String shapeString) {
		Polyline mPolyline = new Polyline();
		shapeString = shapeString.replace("LINESTRING (", "");
		shapeString = shapeString.replace(")", "");
		String[] points = shapeString.split(",");

		for (int i = 0; i < points.length; i++) {
			String pointStr = points[i].trim();
			String[] xy = pointStr.split(" ");

			double x = Double.parseDouble(xy[0]);
			double y = Double.parseDouble(xy[1]);

			Point p = new Point(x, y);
			if (i == 0) {
				mPolyline.startPath(p);
			} else {
				mPolyline.lineTo(p);
			}
		}
		return mPolyline;
	}
}
