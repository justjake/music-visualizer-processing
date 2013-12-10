package org.teton_landis.jake.osc_visualizer;

import oscP5.*;

/**
 * Exists only to override sketchFullScreen because Processing seems to be unable
 * to detect the --present or --full-screen options as outlined in the documentation.
 * osc_visualizer
 * by Jake Teton-Landis
 *
 * @date 12/9/13 9:18 PM
 */
public class VisualizerWindowed extends VisualizerApplet {
    public boolean sketchFullScreen() {
        return false;
    }
    // required or else oscEvent doesn't work :(
    public void oscEvent(OscMessage mess) {
        super.oscEvent(mess);
    }
}
