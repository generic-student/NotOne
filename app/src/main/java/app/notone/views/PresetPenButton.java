package app.notone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import app.notone.R;

public class PresetPenButton extends androidx.appcompat.widget.AppCompatImageButton {
    private final String TAG = "NotOnePresetPenButton";

    public Spinner ddMenuColor;
    public Spinner ddMenWeight;
    public int mddMenuColorId;
    public int mddMenuWeightId;
    public int mcolorindexMapId;
    public int mddMenuColorIndex;
    public int mddMenuWeightIndex;
    public float mddMenuWeightValue;

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
     * create presetpen from current spinner settings
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
        this.mddMenuColorId = ddownmpencolorsid;
        this.mddMenuWeightId = ddownmpenweightsid;
        this.mcolorindexMapId = colorindexmapid;

        this.ddMenuColor = fragmentActivity.findViewById(mddMenuColorId);
        this.ddMenWeight = fragmentActivity.findViewById(mddMenuWeightId);

        this.mddMenuColorIndex = ddMenuColor.getSelectedItemPosition();
        this.mddMenuWeightIndex = ddMenWeight.getSelectedItemPosition();
        this.mddMenuWeightValue = Float.parseFloat(ddMenWeight.getSelectedItem().toString());

        setLayout(context);
    }

    /**
     * create presetpen from json data
     * @param context
     * @param fragmentActivity
     * @param ddownmpencolorsid
     * @param ddownmpenweightsid
     * @param colorindexmapid
     * @param mddMenuColorIndex
     * @param mmdownWeightindex
     * @param ddMenuWeightValue
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity,
                           int ddownmpencolorsid, int ddownmpenweightsid,
                           int colorindexmapid, int mddMenuColorIndex, int mmdownWeightindex, float ddMenuWeightValue){
        super(context);

        this.mddMenuColorId = ddownmpencolorsid;
        this.mddMenuWeightId = ddownmpenweightsid;
        this.mcolorindexMapId = colorindexmapid;

        this.ddMenuColor = fragmentActivity.findViewById(mddMenuColorId);
        this.ddMenWeight = fragmentActivity.findViewById(mddMenuWeightId);

        this.mddMenuColorIndex = mddMenuColorIndex;
        this.mddMenuWeightIndex = mmdownWeightindex;
        this.mddMenuWeightValue = ddMenuWeightValue;

        setLayout(context);
    }

    public void setLayout(Context context) {
        /* position */
        float scale = (float) ((2 - 2 * Math.exp(-mddMenuWeightValue / 8)));
        scale = scale < 0.5 ? 1 : scale;
        setScaleX(scale);
        setScaleY(scale);
        // setPadding(0,0,0,0); // TODO improve padding to make it consistent between all pens
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(100/scale), LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        /* color and res */
        int[] colorIndexMap = getResources().getIntArray(mcolorindexMapId);
        setColorFilter(colorIndexMap[mddMenuColorIndex]);
        setBackground(ContextCompat.getDrawable(context, android.R.color.transparent));
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pen));
    }
}
