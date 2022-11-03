package app.notone;

import android.graphics.Path;

/**
 * Helper class for various math functions
 */
public class MathHelper {
    /**
     * check if a line segment and a circle intersect
     * @param P Point A of the line segment
     * @param Q Point B of the line segment
     * @param O Center point of the circle
     * @param radius Radius of the circle
     * @return boolean
     */
    public static boolean lineSegmentIntersectsCircle(Point2D P, Point2D Q, Point2D O, float radius) {
        float minDist = Float.MIN_VALUE;
        final Point2D OP = P.subtract(O);
        final Point2D QP = P.subtract(Q);
        final Point2D OQ = Q.subtract(O);
        final Point2D PQ = Q.subtract(P);

        float max_dist = Math.max(P.distance(O), Q.distance(O));
        if(OP.dotProduct(QP) > 0 && OQ.dotProduct(PQ) > 0) {
            minDist = 2.f * triangleArea(O, P, Q) / P.distance(Q);
        }
        else {
            minDist = Math.min(O.distance(P), O.distance(Q));
        }
        if(minDist <= radius && max_dist >= radius) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * compute the area of a triangle defined by three points
     * @param A first point of the triangle
     * @param B second point of the triangle
     * @param C third point of the tirangle
     * @return area of the triangle
     */
    public static float triangleArea(Point2D A, Point2D B, Point2D C) {
        Point2D AB = B.subtract(A);
        Point2D AC = C.subtract(A);
        return Math.abs(AB.crossProduct(AC))/2.f;
    }

    public static boolean pathIntersectsCircle(Path path, Point2D center, float radius) {
        float[] points = path.approximate(0.5f);
        for(int j = 0; j < points.length - 6; j+=3) {
            Point2D begin = new Point2D(points[j+1], points[j+2]); //point in the path
            Point2D end = new Point2D(points[j+4], points[j+5]); //next point in the path
            //check if the line segment intersects with the circle
            if(MathHelper.lineSegmentIntersectsCircle(begin, end, center, radius)) {
                return true;
            }
        }
        return false;
    }
}
