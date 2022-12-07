package app.notone.core.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import app.notone.core.Vector2f;

/**
 * Helper function for the Separating-Axis-Theorem
 */
public class SAT {
    /**
     * Checks if two non-axis-aligned rectangles are intersecting
     * @param r1 the points describing the first rectangle
     * @param r2 the points describing the second rectangle
     * @return true if the are intersecting
     */
    public static boolean rectangleRectangleIntersection(@NonNull float[] r1, @NonNull float[] r2) {
        if (r1.length != 8 || r2.length != 8) {
            throw new IllegalArgumentException("The arrays representing the rectangles require 8 elements.");
        }

        Vector2f axes[] = {
                new Vector2f(r1[0], r1[1]).subtract(new Vector2f(r1[2], r1[3])),
                new Vector2f(r1[2], r1[3]).subtract(new Vector2f(r1[4], r1[5])),
                new Vector2f(r2[0], r2[1]).subtract(new Vector2f(r2[2], r2[3])),
                new Vector2f(r2[2], r2[3]).subtract(new Vector2f(r2[4], r2[5]))
        };

        for (Vector2f axis : axes) {
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

            if (projectionsRect1[3] < projectionsRect2[0] || projectionsRect2[3] < projectionsRect2[0]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a line intersects a rectangle
     * @param p the points describing the line
     * @param r the points describing the rectangle
     * @return true if they are intersecting
     */
    public static boolean lineRectangleIntersection(@NonNull float[] p, @NonNull float[] r) {
        if (r.length != 8 || p.length != 4) {
            throw new IllegalArgumentException("The arrays representing the rectangles require 8 elements and the path requires 4 elements.");
        }

        Vector2f axes[] = {
                new Vector2f(r[0], r[1]).subtract(new Vector2f(r[2], r[3])),
                new Vector2f(r[2], r[3]).subtract(new Vector2f(r[4], r[5])),
                new Vector2f(p[0], p[1]).subtract(new Vector2f(p[2], p[3]))
        };

        for (Vector2f axis : axes) {
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

            if (projectionsLine[1] < projectionsRect[0] || projectionsRect[3] < projectionsLine[0]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if two non-axis-aligned rectangles intersect. One of the rectangles is constructed from a point and a given side-length.
     * @param x x-Coordinate of the point
     * @param y y-Coordinate of the point
     * @param sideLength side-length of the constructed rectangle
     * @param r the other rectangle
     * @return true if they are intersecting
     */
    public static boolean rectangularPointRectangleIntersection(float x, float y, float sideLength, @NonNull float[] r) {
        return rectangleRectangleIntersection(r, new float[] {
                x - sideLength / 2, y - sideLength / 2,
                x + sideLength / 2, y - sideLength / 2,
                x + sideLength / 2, y + sideLength / 2,
                x - sideLength / 2, y + sideLength / 2
        });
    }

    /**
     * Checks if any line from a list of lines intersects a non-axis-aligned rectangle
     * @param p list of points describing multiple lines (or a path)
     * @param r list of points describing a rectangle
     * @return true if one of the line-segments intersects the rectangle
     */
    public static boolean linesRectangleIntersection(@NonNull ArrayList<Float> p, @NonNull float[] r) {
        if (r.length != 8 || p.size() < 4) {
            throw new IllegalArgumentException(
                    String.format("The list representing the rectangle require 8 and the path require at least 4 elements. But the rectangle contains %d and the path %d elements.", r.length, p.size())
            );
        }

        ArrayList<Vector2f> axes = new ArrayList<>();
        axes.add(new Vector2f(r[0], r[1]).subtract(new Vector2f(r[2], r[3])));
        axes.add(new Vector2f(r[2], r[3]).subtract(new Vector2f(r[4], r[5])));
        for (int i = 0; i < p.size() - 2; i += 2) {
            axes.add(new Vector2f(p.get(i), p.get(i + 1)).subtract(new Vector2f(p.get(i + 2), p.get(i + 3))));
        }

        for (Vector2f axis : axes) {
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
            for (int i = 0; i < p.size(); i += 2) {
                projectionsLine.add(new Vector2f(p.get(i), p.get(i + 1)).dotProduct(axis));
            }
            Collections.sort(projectionsLine);

            if (projectionsLine.get(projectionsLine.size() - 1) < projectionsRect[0] || projectionsRect[3] < projectionsLine.get(0)) {
                return false;
            }
        }

        return true;
    }
}
