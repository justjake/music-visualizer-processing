package org.teton_landis.jake.osc_visualizer;

import processing.core.*;
import java.util.*;


/**
 * org.teton_landis.jake.osc_visualizer.Grid - a simple distortion grid with bulge forces
 * osc_visualizer
 * by Jake Teton-Landis
 *
 * @date 12/5/13 4:05 PM
 */
public class Grid {

    public static final float PI = 3.141592f;

    public static class Force {
        public PVector pos;
        public float mag;
        public float radius;

        public Force(PVector _pos, float _rad, float _mag) {
            pos = _pos;
            radius = _rad;
            mag = _mag;
        }

        public Force(PVector _pos, float _rad) {
            this(_pos, _rad, 5.0f);
        }

        // returns the motion this force exerts on a given point
        // inverse square law
        public PVector forceOnPoint(PVector pt) {
            float distance = pos.dist(pt);
            float delta = (3*mag) / (4*PI*distance*distance);
            if (delta<1) delta = 0;

            PVector force = PVector.sub(pos, pt);
            force.normalize();
            force.mult(delta);
            return force;
        }
    }

    private Util util;
    public PApplet parent;
    public List<Force> forces;
    // public PVector[][] lines;

    public int width;
    public int height;

    public Grid(PApplet _parent, int rows, int cols) {
        forces = new ArrayList<Force>();
        parent = _parent;
        util = new Util(parent);
        width = cols;
        height = rows;
    }

    void draw(int out_w, int out_h) {
        Util.Scaler sx = util.new Scaler(0, width, 0, out_w);
        Util.Scaler sy = util.new Scaler(0, height, 0, out_h);

//        PApplet.println("drawing grid. w="+width+" h="+height);
        // loop through all the points drawing lines for each row
        for (float y=0; y<=height; y++) {
            PVector prev_pt = null;
            for (float x=0; x<=width; x++) {
                PVector motion = new PVector(0,0);
                PVector pt = new PVector(x, y);

                // loop through forcces and add up the displacement
                for (Force f : forces) {
                    // distance between force and cur point
                    PVector delta = f.forceOnPoint(pt);
//                    PApplet.println(String.format("point:%s, force:%s, delta:%s",
//                                                  pt.toString(), f.pos.toString(), delta.toString()));
                    motion.add(f.forceOnPoint(pt));
                }
                pt.add(motion);
                if (prev_pt != null) {
                    parent.line(
                            (float) sx.scale(prev_pt.x),
                            (float) sy.scale(prev_pt.y),
                            (float) sx.scale(pt.x),
                            (float) sy.scale(pt.y));
                }
                prev_pt = pt;
            }
        }

    }
}
