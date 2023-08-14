package app.notone.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.fragment.app.FragmentActivity;
import app.notone.R;

/**
 * XML inflatable class that contains the data and layout of a presetpen for the canvastoolbar
 * @author Luca Hackel
 * @since 202212XX
 */

public class PresetPenButton extends MaterialButton {
    private static final String TAG = "NotOnePresetPenButton";
    private int id = 0;

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
        super(context, null, com.google.android.material.R.attr.materialIconButtonFilledTonalStyle);
    }

    /**
     * xml inflation constructor
     */
    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, com.google.android.material.R.attr.materialIconButtonFilledTonalStyle);
    }

    /**
     * xml inflation constructor
     */
    public PresetPenButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, com.google.android.material.R.attr.materialIconButtonFilledTonalStyle);
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
        super(context, null, com.google.android.material.R.attr.materialIconButtonFilledTonalStyle);

        this.mDDMenuColorId = ddownmpencolorsid;
        this.mDDMenuWeightId = ddownmpenweightsid;
        this.mColor2IndexMapId = colorindexmapid;

        this.mDDMenuColor = fragmentActivity.findViewById(mDDMenuColorId);
        this.mDDMenWeight = fragmentActivity.findViewById(mDDMenuWeightId);

        this.mDDMenuColorIndex = mDDMenuColor.getSelectedItemPosition();
        this.mDDMenuWeightIndex = mDDMenWeight.getSelectedItemPosition();
        this.mDDMenuWeightValue = Float.parseFloat(mDDMenWeight.getSelectedItem().toString());

        if(this.id == 0)
            this.id = generateViewId();
        setId(this.id);

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
        super(context, null, com.google.android.material.R.attr.materialIconButtonFilledTonalStyle);

        this.mDDMenuColorId = ddownmpencolorsid;
        this.mDDMenuWeightId = ddownmpenweightsid;
        this.mColor2IndexMapId = colorindexmapid;

        this.mDDMenuColor = fragmentActivity.findViewById(mDDMenuColorId);
        this.mDDMenWeight = fragmentActivity.findViewById(mDDMenuWeightId);

        this.mDDMenuColorIndex = ddmenucolorindex;
        this.mDDMenuWeightIndex = ddmenuweightindex;
        this.mDDMenuWeightValue = ddmenuweightvalue;

        if(this.id == 0)
            this.id = generateViewId();
        setId(this.id);

        setLayout(context);
    }

    @Override
    public void addOnCheckedChangeListener(@NonNull OnCheckedChangeListener listener) {
        super.addOnCheckedChangeListener(listener);

    }

    /**
     * initialize the layout of the button
     * @param context
     */
    private void setLayout(Context context) {
        /* positioning */
       LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
        setContentDescription("PresetPen");

//        setScaleType(ScaleType.FIT_CENTER);

        /* color and res */
        int[] colorIndexMap = getResources().getIntArray(mColor2IndexMapId);
//        setHighlightColor(colorIndexMap[mDDMenuColorIndex]);

        setBackgroundColor(colorIndexMap[mDDMenuColorIndex]);
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_pen);
        setIcon(icon);

        addOnCheckedChangeListener((button, isChecked) -> {
            if(isChecked){
                setBackgroundColor(colorIndexMap[mDDMenuColorIndex]);
                setIconSize(80);
                setIconPadding(0); // dont increase button size
                ScaleAnimation scaleAnimation = new ScaleAnimation(
                        1f, 1.03f, // From 1x to 1.5x scale
                        1f, 1.03f,
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point X (center)
                        Animation.RELATIVE_TO_SELF, 0.5f  // Pivot point Y (center)
                );
                scaleAnimation.setDuration(200); // Animation duration in milliseconds

                Interpolator customInterpolator = PathInterpolatorCompat.create(0.000f, 0.000f, 0.0f, 1);
                scaleAnimation.setInterpolator(customInterpolator);
                button.setAnimation(scaleAnimation); // Apply the animation

            } else {
                int desaturatedColor =
                        ColorUtils.blendARGB(colorIndexMap[mDDMenuColorIndex], android.graphics.Color.WHITE, 0.5f);
                setBackgroundColor(desaturatedColor);
                setIconSize(0);
            }
        });
    }

}
