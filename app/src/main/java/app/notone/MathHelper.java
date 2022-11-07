package app.notone;

import android.graphics.Path;

import androidx.annotation.NonNull;

import java.util.ArrayList;

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
    public static boolean lineSegmentIntersectsCircle(@NonNull Vector2f P, @NonNull Vector2f Q, @NonNull Vector2f O, float radius) {
        if(P.distance(Q) <= radius && (P.distance(O) <= radius || Q.distance(O) <= radius)) {
            return true;
        }

        float minDist;
        final Vector2f OP = P.subtract(O);
        final Vector2f QP = P.subtract(Q);
        final Vector2f OQ = Q.subtract(O);
        final Vector2f PQ = Q.subtract(P);

        float max_dist = Math.max(P.distance(O), Q.distance(O));
        if(OP.dotProduct(QP) > 0 && OQ.dotProduct(PQ) > 0) {
            minDist = 2.f * triangleArea(O, P, Q) / P.distance(Q);
        }
        else {
            minDist = Math.min(O.distance(P), O.distance(Q));
        }
        return minDist <= radius && max_dist >= radius;



//        float minDist = 2.f * triangleArea(O, P, Q) / P.distance(Q);
//        if(minDist <= radius) {
//            return true;
//        }
//        else {
//            return false;
//        }
    }

    /**
     * compute the area of a triangle defined by three points
     * @param A first point of the triangle
     * @param B second point of the triangle
     * @param C third point of the triangle
     * @return area of the triangle
     */
    public static float triangleArea(@NonNull Vector2f A, @NonNull Vector2f B, @NonNull Vector2f C) {
        Vector2f AB = B.subtract(A);
        Vector2f AC = C.subtract(A);
        return Math.abs(AB.crossProduct(AC))/2.f;
    }

    public static boolean pathIntersectsCircle(@NonNull ArrayList<Float> path, @NonNull Vector2f center, float radius) {
        if(path.size() == 2) {
            return new Vector2f(path.get(0), path.get(1)).distance(center) <= radius;
        }

        for(int i = 2; i < path.size() - 2; i+=2) {
            Vector2f begin = new Vector2f(path.get(i), path.get(i+1)); //point in the path
            Vector2f end = new Vector2f(path.get(i+2), path.get(i+3)); //next point in the path
            //check if the line segment intersects with the circle
            if(MathHelper.lineSegmentIntersectsCircle(begin, end, center, radius)) {
                return true;
            }
        }
        return false;
    }

    public static boolean pathIntersectsCircle(@NonNull Path path, @NonNull Vector2f center, float radius) {
        float[] points = path.approximate(0.5f);

        if(points.length == 3) {
            return new Vector2f(points[1], points[2]).distance(center) <= radius;
        }

        for(int j = 0; j < points.length - 6; j+=3) {
            Vector2f begin = new Vector2f(points[j+1], points[j+2]); //point in the path
            Vector2f end = new Vector2f(points[j+4], points[j+5]); //next point in the path
            //check if the line segment intersects with the circle
            if(MathHelper.lineSegmentIntersectsCircle(begin, end, center, radius)) {
                return true;
            }
        }
        return false;
    }
}
