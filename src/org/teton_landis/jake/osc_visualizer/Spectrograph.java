package org.teton_landis.jake.osc_visualizer; /**
 * Handles setup and drawing for the main spectrograph visualization
 * osc_visualizer
 * by Jake Teton-Landis
 *
 * @date 12/7/13 10:27 PM
 */
import processing.core.*;
import java.util.*;

public class Spectrograph {


    public enum Direction {
        FORWARD, BACKWARD;
    }

    private class Plane {
        float[] peaks;
        Util.Color color;

        Plane(float[] peaks_, Util.Color color_) {
            peaks = peaks_;
            color = color_;
        }
    }

    public PApplet parent;
    public int z_spacing;
    private Plane[] planes;
    public Direction direction;

    public int signal_max;
    public int signal_min;

    public Spectrograph(PApplet parent_, int depth, int z_spacing_, Direction direction_) {
        parent = parent_;
        planes = new Plane[depth];
        z_spacing = z_spacing_;
        direction = direction_;
        signal_min = -18;
        signal_max = 30;
    }

    void pushPlane(float[] peaks, Util.Color color) {
        // insert at front of array, dropping first value,
        // then put at back

        Plane plane = new Plane(peaks, color);

        if (direction == Direction.BACKWARD) {
            planes[0] = plane;
            Collections.rotate(Arrays.asList(planes), -1);
            return;
        }

        // rotate array one, then drop last element by replacing with new
        Collections.rotate(Arrays.asList(planes), 1);
        planes[0] = plane;
    }

    void draw(Util.Color key_color) {
        int cube_space = (int) ((parent.width * .3) / 25.0);
        int cube_height = (int) ((parent.width * .7) / 25.0);
        //println("space="+cube_space+" height="+cube_height);
        // spectrograph columns

        // usually we'd push right here

        parent.noStroke();
        for (int p=0; p<planes.length && planes[p] != null; p++) {

            // depth shading. there should be a way to do this with light, though.
            float frontness = PApplet.map(p, 0, planes.length-1, 1, 0);
            if (direction==Direction.BACKWARD)
                frontness = 1.0f - frontness;

            // based on plane place, with only current color recognized
            int box_color = parent.lerpColor(
                    key_color.clone().setOpacity(20).color(), // color at N frames ago
                    key_color.clone().reflect().setOpacity(255).setBrightness(200).color(), // color at now
                    frontness);

            // based on the color of the note
            // box_color = planes[p].color.setOpacity((int) PApplet.map(frontness, 0, 1, 0, 255)).color();

            // Currrent sound row is all-white
            if (direction==Direction.BACKWARD && p==planes.length-1)
                box_color = parent.color(0, 0, 255, 255);
            if (direction==Direction.FORWARD && p==0)
                box_color = parent.color(0, 0, 255, 255);

            parent.fill(box_color);

            // z offset for this row
            int z = z_spacing * p;

            for (int i=0; i<planes[p].peaks.length; i++) {
                parent.pushMatrix();
                // translate to col start location
                int x = cube_space + (int) ((cube_space + cube_height)*(i + 0.5));
                int y = parent.height + cube_height + cube_space;
                parent.translate(x, y, z);

                // here is where we decide the maginitude of the signal to display as cubes
                // the magic numbers here look pretty good with my music
                // the source space is a number an amblitude in decibels, fom -96db to 30db
                // my test signals are only interesting between about -15 and 30db
                float unadjusted_height =  PApplet.map(planes[p].peaks[i], signal_min, signal_max, 0, 31.0f);
                int cubes = (int) (unadjusted_height * PApplet.map(i, 0, planes[p].peaks.length-1, 0.5f, 2));

                for (int j=0; j<cubes; j++) {
                     parent.box(cube_height);
//                    parent.sphere(cube_height / 2);
                    parent.translate(0, -1 * (cube_space + cube_height), 0);
                }

                parent.popMatrix();
            }
        }
    }
}
