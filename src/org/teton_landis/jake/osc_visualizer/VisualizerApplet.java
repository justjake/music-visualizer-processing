package org.teton_landis.jake.osc_visualizer;

import processing.core.*;
import oscP5.*;
import java.util.*;

public class VisualizerApplet extends PApplet{

    // display params for the cube thing
    public static final boolean LIGHTS = false;
    public static final int SPACING = -200;
    public static final int DEPTH = 30;
    public static final PVector POSITION = new PVector(-100.0f, -300.0f, -200.0f);
    public static final PVector ROTATION = new PVector(0.29031897f, -0.33824158f, 0.0f);
            // public static final int Z_OFFSET = 500;
    public static final int FRAMERATE = 30;
    public static final Spectrograph.Direction DIRECTION = Spectrograph.Direction.FORWARD;

    public static final int PORT = 11337; // for OSC messages

    // a whole lotta state
    public Util util;

    public OscP5 oscP5;
    public SoundData current_sound;
    public SoundData latest_with_pitch;
    public MessageParser parser;

    public Grid grid;

    public Util.Color key_color;

    // mouse rotation
    public PVector rotation;
    public PVector prev_rot;
    public PVector click;

    public PVector position;

    // spectrograph
    public Spectrograph spectro;

    public HashMap<Integer, Integer> midi_counts;
    public int midi_max = 0, midi_min = 999;

    boolean awaiting_render_attack;


    public void setup() {
        util = new Util(this);
        spectro = new Spectrograph(this, DEPTH, SPACING, DIRECTION);

        // grid with 5 random sources
        grid = new Grid(this, 50, 50);
        for (int i=0; i<5; i++) {
            PVector force_pos = PVector.random2D();
            force_pos.mult(50.0f);
            grid.forces.add(new Grid.Force(force_pos, 5.0f));
        }

        // set up OSC message listener
        OscProperties settings = new OscProperties();
        settings.setListeningPort(PORT);
        settings.setDatagramSize(99999); // big enough

        // basic Processing config
        size(1920, 1080, P3D);
        frameRate(FRAMERATE);
        sphereDetail(12);
        //stroke(0);
        noStroke();
        colorMode(HSB);

        // parse incoming OSC datagrams into an object
        parser = new MessageParser();
        current_sound = new SoundData();
        latest_with_pitch = new SoundData();
        midi_counts = util.new DefaultHashMap<Integer, Integer>(0);

        // values determined by experimentation
        // rotate_x = 100.26076f;
        // rotate_y = 106.40513f;

        // determined by experiment
        click = new PVector();
        prev_rot = new PVector();
        position = POSITION.get();
        rotation = ROTATION.get();

        // start OSC
        oscP5 = new OscP5(this, settings);
    }

    public void oscEvent(OscMessage mess) {
        //  print("### received an osc message.");
        //  print(" addrpattern: "+mess.addrPattern());
        //  println(" typetag: "+mess.typetag());

        if (parser.parse(mess)) {
            current_sound = parser.flush();
            // not every OSC packet has pitch data;
            if (current_sound.hasPitchData()) {
                latest_with_pitch = current_sound;
                int midi = (int) latest_with_pitch.pitch_raw_midi;
                midi_max = max(midi, midi_max);
                midi_min = min(midi, midi_min);
                // used for statistic tracking of the values we see
                midi_counts.put(midi, midi_counts.get(midi) + 1);
            }
            if (current_sound.attack)
                awaiting_render_attack = true;
        }
    }

    // Utility functions
    
    // return a background color based on the current MIDI note value
    // returned as a 0xaabbcc color.
    Util.Color color_from_sound(SoundData sound) {
        int hue = (int) map((float) sound.pitch_raw_midi, midi_min, midi_max, 0, 255);
        int brightness = (int) map((float) sound.loudness, -15, 0, 80, 0.7f * 255.0f);
        int saturation = (int) (0.6f * 255.0f);

        // this almost never happens, just because there's a low chance that
        // there's still an attack sound in the current_sound field on a screen refresh
        if (sound.attack==true || awaiting_render_attack) {
            saturation = 0;
            brightness = 255;
            awaiting_render_attack = false;
        }

        return util.new Color(
                hue,// hue        0-360
                saturation, // saturation 0-100 (nice at 70)
                brightness  // brightness 0-100 (nice at 97)
        );
    }

    // various drawing functions


    void draw_grid() {
        stroke(255);
        grid.width = width/5;
        grid.height = height/10;
        grid.draw(width, height);
    }

    public void draw() {
        pushMatrix();
        translate(position.x, position.y, position.z);
        rotateX(rotation.y);
        rotateY(rotation.x);
        rotateZ(rotation.z);

        key_color = color_from_sound(latest_with_pitch);
        background(key_color.clone().setBrightness(key_color.brightness / 2).color());

        if (LIGHTS) lights();

        Util.Color spec_color = key_color.clone().reflect();
        spec_color.setBrightness((int) map(spec_color.brightness, 0, 255, 210, 255));
        spectro.pushPlane(util.doubleToFloatArray(current_sound.bark), spec_color);

        spectro.draw(key_color);

        // draw_grid();
        popMatrix();
    }

    PVector screenTo2pi(PVector v) {
        v.x = map(v.x, 0, width, 0, TWO_PI);
        v.y = map(v.y, 0, height, 0, TWO_PI);
        return v;
    }

    public void mousePressed() {
        Util.Scaler sx = util.new Scaler(0.0, width, 0.0, grid.width);
        Util.Scaler sy = util.new Scaler(0.0, height, 0.0, grid.height);
        grid.forces.add(new Grid.Force(new PVector(
                (float) sx.scale(mouseX),
                (float) sy.scale(mouseY)),
                5.0f));

        println("rotation: "+rotation.toString());

        click = screenTo2pi(new PVector(mouseX, mouseY));
        prev_rot = rotation;
    }

    public void mouseDragged() {
        PVector drag = screenTo2pi(new PVector(mouseX, mouseY));
        drag.sub(click);
        rotation = prev_rot.get();
        rotation.add(drag);
    }

    public void keyReleased() {
        // full screen
        if (key == 'j') {
            position.z += 100;
        }
        if (key == 'k') {
            position.z -= 100;
        }

        if (keyCode == UP) {
            position.y -= 100;
        }

        if (keyCode == DOWN) {
            position.y += 100;
        }
        if (keyCode == LEFT) {
            position.x += 100;
        }

        if (keyCode == RIGHT) {
            position.x -= 100;
        }

        if (key == 'm') {
            println(midi_counts.toString());
            println("max: "+ midi_max,
                    " min: "+ midi_min);
        }

        // print position/angle info
        if (key == 'p') {
            println("Position: "+position.toString());
            println("Rotation: "+rotation.toString());

        }
    }

//    public void mousePressed() {
//        PVector click = new PVector(mouseX, mouseY);
//
//        println("click: x " + mouseX + " y " + mouseY);
//z`
//        if (last_click != null) {
//            line(last_click.x, last_click.y, click.x, click.y);
//        }
//        last_click = click;
//    }

}
