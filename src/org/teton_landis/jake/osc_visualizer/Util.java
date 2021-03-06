package org.teton_landis.jake.osc_visualizer;

import processing.core.*;
import java.util.*;
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

    /*
    Wrapper around PApplet#color for EZ transforms
     */
    public class Color {
        public int hue;
        public int saturation;
        public int brightness;
        public int opacity;

        public Color(int hue_, int saturation_, int brightness_, int opacity_) {
            hue = hue_;
            saturation = saturation_;
            brightness = brightness_;
            opacity = opacity_;
        }

        public Color(int hue_, int saturation_, int brightness_) {
            this(hue_, saturation_, brightness_, 255);
        }

        public Color clone() {
            return new Color(hue, saturation, brightness, opacity);
        }

        // returns the Processing color as a hex int
        public int color() {
            return parent.color(hue, saturation, brightness, opacity);
        }

        // mutation methods

        public Color setHue(int hue_) {
            hue = hue_;
            return this;
        }
        public Color setSaturation(int saturation_) {
            saturation = saturation_;
            return this;
        }
        public Color setBrightness(int brightness_) {
            brightness = brightness_;
            return this;
        }
        public Color setOpacity(int opacity_) {
            opacity = opacity_;
            return this;
        }

        public Color reflect() {
            hue = (hue + 255/2) % 255;
            return this;
        }
    }

    // implmentation from stack overflow
    // http://stackoverflow.com/questions/7519339/hashmap-to-return-default-value-for-non-found-keys
    //
    // consider using a treemap for sorted keys, but I have concerns about where there is hidden
    // threading buisness going on in org.teton_landis.jake.osc_visualizer.VisualizerApplet.java with the oscP5 things, so I ain't touching a
    // > Note that this implementation is not synchronized. If multiple threads access a map concurrently,
    // > and at least one of the threads modifies the map structurally, it must be synchronized externally.
    //  -- TreeMap javadoc
    public static class DefaultHashMap<K,V> extends HashMap<K,V> {
        protected V defaultValue;
        public DefaultHashMap(V defaultValue) {
            this.defaultValue = defaultValue;
        }
        @Override
        public V get(Object k) {
            V v = super.get(k);
            return ((v == null) && !this.containsKey(k)) ? this.defaultValue : v;
        }
    }

    /**
     * useful for storing scene setup stuff
     */
    public static class PositionRotation {
        public PVector position;
        public PVector rotation;

        PositionRotation(PVector pos, PVector rot) {
            position = pos;
            rotation = rot;
        }

        // deep copy
        public PositionRotation clone() {
           return new PositionRotation(
                   position.get(), rotation.get()
           );
        }
    }
}
