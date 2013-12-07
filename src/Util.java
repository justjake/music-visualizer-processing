import processing.core.*;
public class Util {

    private PApplet parent;
    public Util(PApplet p_) {
        parent = p_;
    }

    public float[] doubleToFloatArray(double[] ar) {
        float[] far = new float[ar.length];
        for (int i=0; i<ar.length; i++) {
            far[i] = (float) ar[i];
        }
        return far;
    }

    public class Scaler {
        public double from_min, from_max, to_min, to_max;

        Scaler(double fmin, double fmax, double tmin, double tmax) {
            from_max = fmax;
            from_min = fmin;
            to_min = tmin;
            to_max = tmax;
        }

        public double scale(double n) {
            double in_range = from_max - from_min;
            double out_range = to_max - to_min;

            return ((n - from_min) / in_range) * out_range + to_min;
        }

        public void test(double expected, double n) {
            parent.println("scaler.scale(n) -> " + this.scale(n) + ", expected "+expected);
        }
    }
}
