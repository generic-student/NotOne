package app.notone.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import app.notone.R;

/**
 * XML inflatable class that contains the data and layout of a presetpen for the canvastoolbar
 * @author Luca Hackel
 * @since 202212XX
 */

public class PresetPenButton extends androidx.appcompat.widget.AppCompatImageButton {
    private static final String TAG = "NotOnePresetPenButton";

    /* assigned by the constructors */
    public Spinner mDDMenuColor;
    public Spinner mDDMenWeight;
    public int mDDMenuColorId;
    public int mDDMenuWeightId;
    public int mColor2IndexMapId;
    public int mDDMenuColorIndex;
    public int mDDMenuWeightIndex;
    public float mDDMenuWeightValue;

    /**
     * xml inflation constructor
     */
    public PresetPenButton(@NonNull Context context) {
        super(context);
    }

    /**
     * xml inflation constructor
     */
    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * xml inflation constructor
     */
    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * create presetpen from current spinner settings
     * @param fragmentActivity     the activity that contains the ddmenus
     * @param ddownmpencolorsid    the id of the color chooser dd
     * @param ddownmpenweightsid   the id of the weight chooser dd
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
     * @param fragmentActivity      the activity that contains the ddmenus
     * @param ddownmpencolorsid     the id of the color chooser ddm
     * @param ddownmpenweightsid    the id of the weight chooser ddm
     * @param colorindexmapid       maps the index of the ddownmcolors (from R.array.pen_colors) to actual colors (from R.array.pen_color_values)
     * @param ddmenucolorindex      index of the color in the ddm of the preset pen
     * @param ddmenuweightindex     index of the weight in the ddm of the preset pen
     * @param ddmenuweightvalue     value of the weight in the ddm of the preset pen
     */
    public PresetPenButton(Context context, FragmentActivity fragmentActivity,
                           int ddownmpencolorsid, int ddownmpenweightsid,
                           int colorindexmapid, int ddmenucolorindex,
                           int ddmenuweightindex, float ddmenuweightvalue){
        super(context);

        this.mDDMenuColorId = ddownmpencolorsid;
        this.mDDMenuWeightId = ddownmpenweightsid;
        this.mColor2IndexMapId = colorindexmapid;

        this.mDDMenuColor = fragmentActivity.findViewById(mDDMenuColorId);
        this.mDDMenWeight = fragmentActivity.findViewById(mDDMenuWeightId);

        this.mDDMenuColorIndex = ddmenucolorindex;
        this.mDDMenuWeightIndex = ddmenuweightindex;
        this.mDDMenuWeightValue = ddmenuweightvalue;

        setLayout(context);
    }

    /**
     * inialize the layout of the button
     * @param context
     */
    public void setLayout(Context context) {
        /* positioning */
        float scale = (float) ((2 - 2 * Math.exp(-mDDMenuWeightValue / 8))); // 0 to 2
        scale = scale < 0.5 ? 1 : scale;
        int sizeXY = (int) (70 * scale);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeXY, sizeXY);
        params.gravity = Gravity.CENTER;

        setScaleType(ScaleType.FIT_CENTER);
        setLayoutParams(params);

        /* color and res */
        int[] colorIndexMap = getResources().getIntArray(mColor2IndexMapId);

        TypedValue selectableItemBackgroundResourceHolder = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, selectableItemBackgroundResourceHolder, true);
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_pen);

        setForeground(ContextCompat.getDrawable(context, selectableItemBackgroundResourceHolder.resourceId));
        setBackground(ContextCompat.getDrawable(context, android.R.color.transparent));
        setImageDrawable(icon);
        setColorFilter(colorIndexMap[mDDMenuColorIndex]);
    }
}
