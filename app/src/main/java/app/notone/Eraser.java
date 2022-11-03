package app.notone;

import android.graphics.RectF;

import java.util.ArrayList;

public class Eraser {
    public static int erase(ArrayList<Stroke> strokes, Point2D eraserPosition, float eraserRadius) {
        int strokesErased = 0;
        RectF bounds = new RectF();

        //check if the current cursor position intersects the bounds of one of the strokes and remove it
        for(int i = strokes.size() - 1; i >= 0; i--) {
            strokes.get(i).getPath().computeBounds(bounds, true);

            //check if the outer bounds that encompass the entire path intersects with the cursor
            if(bounds.isEmpty() || bounds.contains(eraserPosition.x, eraserPosition.y)) {
                if(MathHelper.pathIntersectsCircle(strokes.get(i).getPath(), eraserPosition, eraserRadius)) {
                    strokes.remove(i);
                    strokesErased++;
                }
            }
        }

        return strokesErased;
    }

}
