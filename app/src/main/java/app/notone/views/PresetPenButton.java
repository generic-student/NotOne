package app.notone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import app.notone.CanvasView;
import app.notone.R;

public class PresetPenButton extends androidx.appcompat.widget.AppCompatImageButton {
    private final String TAG = "NotOnePresetPenButton";
    public Spinner mDdownmColor;
    public Spinner mDdownmWeight;
    public int mddownColorIndex;
    public int mddownWeightIndex;
    private float mddownmWeight;

    public PresetPenButton(@NonNull Context context) {
        super(context);
    }

    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param context
     * @param fragmentActivity
     * @param ddownm_pen_colors
     * @param ddownm_pen_weights
     * @param colorIndexMap      maps the index of the ddownmcolors (from R.array.pen_colors) to actual colors (from R.array.pen_color_values)
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity, int ddownm_pen_colors, int ddownm_pen_weights, int[] colorIndexMap) {
        super(context);
        this.mDdownmColor = fragmentActivity.findViewById(ddownm_pen_colors);
        this.mDdownmWeight = fragmentActivity.findViewById(ddownm_pen_weights);

        this.mddownColorIndex = mDdownmColor.getSelectedItemPosition();
        this.mddownWeightIndex = mDdownmWeight.getSelectedItemPosition();
        this.mddownmWeight = Float.parseFloat(mDdownmWeight.getSelectedItem().toString());

        set(context, colorIndexMap);
    }

    public PresetPenButton(Context context, FragmentActivity fragmentActivity, int ddownm_pen_colors, int ddownm_pen_weights, int[] colorIndexMap, int mddownColorIndex, int mmdownWeightindex, float mddownmWeight){
        super(context);
        Log.d(TAG, "PresetPenButton: Generating Button");
        this.mDdownmColor = fragmentActivity.findViewById(ddownm_pen_colors);
        this.mDdownmWeight = fragmentActivity.findViewById(ddownm_pen_weights);

        this.mddownColorIndex = mddownColorIndex;
        this.mddownWeightIndex = mmdownWeightindex;
        this.mddownmWeight = mddownmWeight;

        set(context, colorIndexMap);
    }

    public void set(Context context, int[] colorIndexMap) {
        // visuals
        // position
        float scale = (float) ((2 - 2 * Math.exp(-mddownmWeight / 8)));
        scale = scale < 0.5 ? 1 : scale;
        setScaleX(scale);
        setScaleY(scale);
        // setPadding(0,0,0,0);
        Log.d(TAG, "PresetPenButton: " + (100/scale));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(100/scale), LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        // color and res
        setColorFilter(colorIndexMap[mddownColorIndex]);
        setBackground(ContextCompat.getDrawable(context, android.R.color.transparent));
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pen));
    }
}
