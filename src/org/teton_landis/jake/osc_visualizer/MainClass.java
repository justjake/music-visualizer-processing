package org.teton_landis.jake.osc_visualizer;

import processing.core.PApplet;

/**
 * Program entry point
 * osc_visualizer
 * by Jake Teton-Landis
 *
 * @date 12/8/13 4:28 AM
 */
public class MainClass {
    public  static final String[] WELCOME_TEXT = new String[] {
            "OSC Visualizer",
            "  (c) Jake Teton-Landis, 2013",
            "  <just.1.jake@gmail.com>",
            "  --window to run in a window",
            "  --help for help"
    };
    public static final String[] HELP_TEXT = new String[] {
            "Controls:",
            "  Arrow keys - change model offest on X/Y axis",
            "  mouse - change model rotation",
            "  j/k - change model offset on Z axis",
            "  1 through 0 - view preset offset/rotation",
            "  p - print position info",
            "  z/k - change signal minimum cuttoff",
            "  m - print MIDI value debug info"
    };

    static void printEachLine(String[] text) {
        for (String line:text)
            PApplet.println(line);
    }

    public  static void main(String args[]) {
        boolean use_fs = true;

        printEachLine(WELCOME_TEXT);

        if (args.length > 0) {
            for( String a : args) {
                if (a.equals("--window") || a.equals("--windowed")) {
                    PApplet.println("opening in a window...");
                    use_fs = false;
                }

                if (a.equals("--help")) {
                    printEachLine(HELP_TEXT);
                }
            }
        }

        if (use_fs) {
            VisualizerApplet.main("org.teton_landis.jake.osc_visualizer.VisualizerApplet", args);
        } else {
            VisualizerWindowed.main("org.teton_landis.jake.osc_visualizer.VisualizerWindowed", args);
        }
    }
}
