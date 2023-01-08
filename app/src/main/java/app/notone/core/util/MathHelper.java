package app.notone.core.util;


import android.graphics.RectF;

/**
 * Helper class for various math functions
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class MathHelper {
    /**
     * Converts a RectF to a list of floats from bottom-left
     * clockwise to bottom-right
     *
     * @param rect RectF
     * @return float[8] with [x, y, x, y, ...]
     */
    public static float[] rectToFloatArray(RectF rect) {
        return new float[]{
                rect.left, rect.bottom,
                rect.left, rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom
        };
    }
}
