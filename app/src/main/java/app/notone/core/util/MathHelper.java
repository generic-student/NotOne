package app.notone.core.util;


import android.graphics.RectF;

/**
 * Helper class for various math functions
 */
public class MathHelper {
    /**
     * Converts a RectF to a list of floats
     * @param rect
     * @return float[8] with [x, y, x, y, ...]
     */
    public static float[] rectToFloatArray(RectF rect) {
        return new float[] {
          rect.left, rect.bottom,
          rect.left, rect.top,
          rect.right, rect.top,
          rect.right, rect.bottom
        };
    }
}
