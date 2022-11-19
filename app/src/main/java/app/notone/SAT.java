package app.notone;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SAT {
    public static boolean rectangleRectangleIntersection(@NonNull float[] r1, @NonNull float[] r2) {
        if(r1.length != 8 || r2.length != 8) {
            throw new IllegalArgumentException("The arrays representing the rectangles require 8 elements.");
        }

        Vector2f axes[] = {
                new Vector2f(r1[0], r1[1]).subtract(new Vector2f(r1[2], r1[3])),
                new Vector2f(r1[2], r1[3]).subtract(new Vector2f(r1[4], r1[5])),
                new Vector2f(r2[0], r2[1]).subtract(new Vector2f(r2[2], r2[3])),
                new Vector2f(r2[2], r2[3]).subtract(new Vector2f(r2[4], r2[5]))
        };

        for(Vector2f axis : axes) {
            //find the min and max for rect1
            float projectionsRect1[] = {
                    new Vector2f(r1[0], r1[1]).dotProduct(axis),
                    new Vector2f(r1[2], r1[3]).dotProduct(axis),
                    new Vector2f(r1[4], r1[5]).dotProduct(axis),
                    new Vector2f(r1[6], r1[7]).dotProduct(axis)
            };
            Arrays.sort(projectionsRect1);

            //find the min and max for rect2
            float projectionsRect2[] = {
                    new Vector2f(r2[0], r2[1]).dotProduct(axis),
                    new Vector2f(r2[2], r2[3]).dotProduct(axis),
                    new Vector2f(r2[4], r2[5]).dotProduct(axis),
                    new Vector2f(r2[6], r2[7]).dotProduct(axis)
            };
            Arrays.sort(projectionsRect2);

            if(projectionsRect1[3] < projectionsRect2[0] || projectionsRect2[3] < projectionsRect2[0]) {
                return false;
            }
        }

        return true;
    }

    public static boolean LineRectangleIntersection(@NonNull float[] p, @NonNull float[] r) {
        if(r.length != 8 || p.length != 4) {
            throw new IllegalArgumentException("The arrays representing the rectangles require 8 elements and the path requires 4 elements.");
        }

        Vector2f axes[] = {
                new Vector2f(r[0], r[1]).subtract(new Vector2f(r[2], r[3])),
                new Vector2f(r[2], r[3]).subtract(new Vector2f(r[4], r[5])),
                new Vector2f(p[0], p[1]).subtract(new Vector2f(p[2], p[3]))
        };

        for(Vector2f axis : axes) {
            //find the min and max for rect1
            float projectionsRect[] = {
                    new Vector2f(r[0], r[1]).dotProduct(axis),
                    new Vector2f(r[2], r[3]).dotProduct(axis),
                    new Vector2f(r[4], r[5]).dotProduct(axis),
                    new Vector2f(r[6], r[7]).dotProduct(axis)
            };
            Arrays.sort(projectionsRect);

            //find the min and max for rect2
            float projectionsLine[] = {
                    new Vector2f(p[0], p[1]).dotProduct(axis),
                    new Vector2f(p[2], p[3]).dotProduct(axis)
            };
            Arrays.sort(projectionsLine);

            if(projectionsLine[1] < projectionsRect[0] || projectionsRect[3] < projectionsLine[0]) {
                return false;
            }
        }

        return true;
    }

    public static boolean LinesRectangleIntersection(@NonNull ArrayList<Float> p, @NonNull float[] r) {
        if(r.length != 8 || p.size() < 4) {
            throw new IllegalArgumentException("The arrays representing the rectangles require 8 elements and the path requires 4 elements.");
        }

        ArrayList<Vector2f> axes = new ArrayList<>();
        axes.add(new Vector2f(r[0], r[1]).subtract(new Vector2f(r[2], r[3])));
        axes.add(new Vector2f(r[2], r[3]).subtract(new Vector2f(r[4], r[5])));
        for(int i = 0; i < p.size() - 2; i+=2) {
            axes.add(new Vector2f(p.get(i), p.get(i+1)).subtract(new Vector2f(p.get(i+2), p.get(i+3))));
        }

        for(Vector2f axis : axes) {
            //find the min and max for rect1
            float projectionsRect[] = {
                    new Vector2f(r[0], r[1]).dotProduct(axis),
                    new Vector2f(r[2], r[3]).dotProduct(axis),
                    new Vector2f(r[4], r[5]).dotProduct(axis),
                    new Vector2f(r[6], r[7]).dotProduct(axis)
            };
            Arrays.sort(projectionsRect);

            //find the min and max for rect2
            ArrayList<Float> projectionsLine = new ArrayList<>();
            for(int i = 0; i < p.size(); i+=2) {
                projectionsLine.add(new Vector2f(p.get(i), p.get(i+1)).dotProduct(axis));
            }
            Collections.sort(projectionsLine);

            if(projectionsLine.get(projectionsLine.size() - 1) < projectionsRect[0] || projectionsRect[3] < projectionsLine.get(0)) {
                return false;
            }
        }

        return true;
    }
}
