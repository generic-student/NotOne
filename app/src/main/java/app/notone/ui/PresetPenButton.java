package app.notone.ui;

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
    private static final String TAG = "NotOnePresetPenButton";

    public Spinner mDDMenuColor;
    public Spinner mDDMenWeight;
    public int mDDMenuColorId;
    public int mDDMenuWeightId;
    public int mColor2IndexMapId;
    public int mDDMenuColorIndex;
    public int mDDMenuWeightIndex;
    public float mDDMenuWeightValue;

    /**
     * xml inflation constructors
     */
    public PresetPenButton(@NonNull Context context) {
        super(context);
    }

    /**
     * xml inflation constructors
     */
    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * xml inflation constructors
     */
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

        this.mDDMenuColorId = ddownmpencolorsid;
        this.mDDMenuWeightId = ddownmpenweightsid;
        this.mColor2IndexMapId = colorindexmapid;

        this.mDDMenuColor = fragmentActivity.findViewById(mDDMenuColorId);
        this.mDDMenWeight = fragmentActivity.findViewById(mDDMenuWeightId);

        this.mDDMenuColorIndex = mDDMenuColor.getSelectedItemPosition();
        this.mDDMenuWeightIndex = mDDMenWeight.getSelectedItemPosition();
        this.mDDMenuWeightValue = Float.parseFloat(mDDMenWeight.getSelectedItem().toString());

        setLayout(context);
    }

    /**
     * create presetpen from json data
     * @param context
     * @param fragmentActivity
     * @param ddownmpencolorsid
     * @param ddownmpenweightsid
     * @param colorindexmapid
     * @param mDDMenuColorIndex
     * @param mmdownWeightindex
     * @param ddMenuWeightValue
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity,
                           int ddownmpencolorsid, int ddownmpenweightsid,
                           int colorindexmapid, int mDDMenuColorIndex, int mmdownWeightindex, float ddMenuWeightValue){
        super(context);

        this.mDDMenuColorId = ddownmpencolorsid;
        this.mDDMenuWeightId = ddownmpenweightsid;
        this.mColor2IndexMapId = colorindexmapid;

        this.mDDMenuColor = fragmentActivity.findViewById(mDDMenuColorId);
        this.mDDMenWeight = fragmentActivity.findViewById(mDDMenuWeightId);

        this.mDDMenuColorIndex = mDDMenuColorIndex;
        this.mDDMenuWeightIndex = mmdownWeightindex;
        this.mDDMenuWeightValue = ddMenuWeightValue;

        setLayout(context);
    }

    /**
     * inialize the layout of the button
     * @param context
     */
    public void setLayout(Context context) {
        /* position */
        float scale = (float) ((2 - 2 * Math.exp(-mDDMenuWeightValue / 8)));
        scale = scale < 0.5 ? 1 : scale;
        setScaleX(scale);
        setScaleY(scale);
        // setPadding(0,0,0,0); // TODO improve padding to make it consistent between all pens
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(100/scale), LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        /* color and res */
        int[] colorIndexMap = getResources().getIntArray(mColor2IndexMapId);
        setColorFilter(colorIndexMap[mDDMenuColorIndex]);
        setBackground(ContextCompat.getDrawable(context, android.R.color.transparent));
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(context, outValue.resourceId));
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pen));
    }
}
