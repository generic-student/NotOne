package app.notone.core.util;

/**
 * Class describing the standard paper sizes in inches and their conversions
 * to pixels.
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class PageSize {
    /**
     * Width of the page in inches
     */
    private final float widthInches;
    /**
     * Height of the page in inches
     */
    private final float heightInches;

    /**
     * Dimensions of an A1 paper after DIN 476
     */
    public static final PageSize A1 = new PageSize(23.39f, 33.11f);
    /**
     * Dimensions of an A2 paper after DIN 476
     */
    public static final PageSize A2 = new PageSize(16.54f, 23.39f);
    /**
     * Dimensions of an A3 paper after DIN 476
     */
    public static final PageSize A3 = new PageSize(11.69f, 16.54f);
    /**
     * Dimensions of an A4 paper after DIN 476
     */
    public static final PageSize A4 = new PageSize(8.29f, 11.69f);
    /**
     * Dimensions of an A5 paper after DIN 476
     */
    public static final PageSize A5 = new PageSize(5.83f, 8.29f);

    public PageSize(float width, float height) {
        this.widthInches = width;
        this.heightInches = height;
    }

    /**
     * Returns the Height of the page given a dpi
     *
     * @param dpi Dots per inch
     * @return Height in pixels
     */
    public int getHeightPixels(float dpi) {
        return (int) (heightInches * dpi);
    }

    /**
     * Returns the Width of the page given a dpi
     *
     * @param dpi Dots per inch
     * @return Width in pixels
     */
    public int getWidthPixels(float dpi) {
        return (int) (widthInches * dpi);
    }
}
