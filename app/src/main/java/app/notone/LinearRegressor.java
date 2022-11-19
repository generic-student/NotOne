package app.notone;

import java.util.ArrayList;

class LinearRegressor {
    public static double getRsquared(ArrayList<Float> xyList) {
        float[] x = new float[xyList.size()/2];
        float[] y = new float[xyList.size()/2];
        int i = 0;
        int jx = 0;
        int jy = 0;
        for (float v: xyList) {
            if((i & 1) == 0 ) {
                x[jx] = v;
                jx++;
            }
            else {
                y[jy] = v;
                jy++;
            }
            i++;
        }

        if (x.length != y.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = x.length;

        // first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (i = 0; i < n; i++) {
            sumx  += x[i];
            sumx2 += x[i]*x[i];
            sumy  += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double slope  = xybar / xxbar;
        double intercept = ybar - slope * xbar;

        // more statistical analysis
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (i = 0; i < n; i++) {
            double fit = slope*x[i] + intercept;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }

        double r2 = ssr / yybar;
        return r2;
    }
}
