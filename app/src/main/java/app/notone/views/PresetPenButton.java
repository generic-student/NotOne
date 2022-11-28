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
    public int ddownm_pen_colorsid;
    public int ddownm_pen_weightsid;
    public int color_index_mapid;
    public Spinner mDdownmColor;
    public Spinner mDdownmWeight;
    public int mddownColorIndex;
    public int mddownWeightIndex;
    public float mddownmWeight;

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
     * create from current spinner settings
     * @param context
     * @param fragmentActivity
     * @param ddownmpencolorsid
     * @param ddownmpenweightsid
     * @param colorindexmapid      maps the index of the ddownmcolors (from R.array.pen_colors) to actual colors (from R.array.pen_color_values)
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity,
                           int ddownmpencolorsid, int ddownmpenweightsid,
                           int colorindexmapid) {
        super(context);
        this.ddownm_pen_colorsid = ddownmpencolorsid;
        this.ddownm_pen_weightsid = ddownmpenweightsid;
        this.color_index_mapid = colorindexmapid;

        this.mDdownmColor = fragmentActivity.findViewById(ddownm_pen_colorsid);
        this.mDdownmWeight = fragmentActivity.findViewById(ddownm_pen_weightsid);

        this.mddownColorIndex = mDdownmColor.getSelectedItemPosition();
        this.mddownWeightIndex = mDdownmWeight.getSelectedItemPosition();
        this.mddownmWeight = Float.parseFloat(mDdownmWeight.getSelectedItem().toString());

        set(context);
    }

    /*
    not from current spinner settings but from backup json
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity,
                           int ddownmpencolorsid, int ddownmpenweightsid,
                           int colorindexmapid, int mddownColorIndex, int mmdownWeightindex, float mddownmWeight){
        super(context);

        this.ddownm_pen_colorsid = ddownmpencolorsid;
        this.ddownm_pen_weightsid = ddownmpenweightsid;
        this.color_index_mapid = colorindexmapid;

        this.mDdownmColor = fragmentActivity.findViewById(ddownm_pen_colorsid);
        this.mDdownmWeight = fragmentActivity.findViewById(ddownm_pen_weightsid);

        this.mddownColorIndex = mddownColorIndex;
        this.mddownWeightIndex = mmdownWeightindex;
        this.mddownmWeight = mddownmWeight;

        set(context);
    }

    public void set(Context context) {
        // visuals
        // position
        float scale = (float) ((2 - 2 * Math.exp(-mddownmWeight / 8)));
        scale = scale < 0.5 ? 1 : scale;
        setScaleX(scale);
        setScaleY(scale);
        // setPadding(0,0,0,0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(100/scale), LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        // color and res
        int[] colorIndexMap = getResources().getIntArray(color_index_mapid);
        setColorFilter(colorIndexMap[mddownColorIndex]);
        setBackground(ContextCompat.getDrawable(context, android.R.color.transparent));
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pen));
    }
}
