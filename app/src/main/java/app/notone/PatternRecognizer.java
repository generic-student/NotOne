package app.notone;

import java.util.ArrayList;

class PatternRecognizer {
    public static boolean isLine(double bound, ArrayList<Float> stroke) {
        return bound < getLinearRegressionRsquared(stroke);
    }

    public static boolean isSquare(double bound, ArrayList<Float> stroke) {
        return false;
    }

    public static ArrayList<Float> getSquare(Stroke stroke) {
        return null;
    }

    public static ArrayList<Float> getStraight(Stroke currentStroke) {
        ArrayList<Float> straight = new ArrayList<>();
        straight.add(currentStroke.getPathPoints().get(0));
        straight.add(currentStroke.getPathPoints().get(1));
        straight.add(currentStroke.getPathPoints().get(currentStroke.getPathPoints().size() - 2));
        straight.add(currentStroke.getPathPoints().get(currentStroke.getPathPoints().size() - 1));
        return straight;
    }

    private static double getLinearRegressionRsquared(ArrayList<Float> xy) {
        if (xy.size() % 2 != 0) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = xy.size() / 2;

        double sum_x = 0.0,
                sum_y = 0.0;
        for (int i = 0; i < n; i += 2) {
            sum_x += xy.get(i);
            sum_y += xy.get(i + 1);
        }
        double average_x = sum_x / n;
        double average_y = sum_y / n;

        double sum_sq_av_dif_x = 0.0,
                sum_sq_av_dif_y = 0.0,
                sum_sq_av_dif_xy = 0.0;
        for (int i = 0; i < n; i += 2) {
            sum_sq_av_dif_x += (xy.get(i) - average_x) * (xy.get(i) - average_x);
            sum_sq_av_dif_y += (xy.get(i + 1) - average_y) * (xy.get(i + 1) - average_y);
            sum_sq_av_dif_xy += (xy.get(i) - average_x) * (xy.get(i + 1) - average_y);
        }
        double a = sum_sq_av_dif_xy / sum_sq_av_dif_x;
        double b = average_y - a * average_x;
        // y = a * x + b

        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i += 2) {
            double fit = a * xy.get(i) + b;
            ssr += (fit - average_y) * (fit - average_y);
        }

        double r_sq = ssr / sum_sq_av_dif_y;
        return r_sq;
    }
}
