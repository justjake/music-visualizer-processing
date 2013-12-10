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
            "  https://github.com/justjake/music-visualizer-processing",
            "  --window to run in a window",
            "  --help for help"
    };
    public static final String[] HELP_TEXT = new String[] {
            "Controls:",
            "  Arrow keys   : change model offest on X/Y axis",
            "  mouse        : change model rotation",
            "  j/k          : change model offset on Z axis",
            "  1 through 0  : view preset offset/rotation",
            "  p            : print position info",
            "  z/k          : change signal minimum cuttoff",
            "  m            : print MIDI value debug info"
    };

    static void printEachLine(String[] text) {
        for (String line:text)
            PApplet.println(line);
    }

    public  static void main(String args[]) {
        printEachLine(WELCOME_TEXT);

        VisualizerApplet.Parameters params = VisualizerApplet.Parameters.Parse(args);

        if (params.failure != null) {
            System.err.println("Failure: "+params.failure.getMessage());
            params.parser.printUsage(System.err);
            System.exit(1);
        }

        if (params.show_help) {
            printEachLine(HELP_TEXT);
            System.out.println("Command-Line Options:");
            params.parser.printUsage(System.out);
            System.exit(0);
        }

        if (params.window) {
            VisualizerWindowed.main("org.teton_landis.jake.osc_visualizer.VisualizerWindowed", args);
        } else {
            VisualizerApplet.main("org.teton_landis.jake.osc_visualizer.VisualizerApplet", args);
        }
    }
}
