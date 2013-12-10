package org.teton_landis.jake.osc_visualizer;

import processing.core.*;
import oscP5.*;

import java.awt.*;
import java.util.*;

public class VisualizerApplet extends PApplet{

    // display params for the cube thing
    public static final boolean LIGHTS = false;
    public static final int SPACING = -200;
    public static final int DEPTH = 40;
//    public static final PVector POSITION = new PVector(-100.0f, -300.0f, -200.0f);
//    public static final PVector ROTATION = new PVector(0.29031897f, -0.33824158f, 0.0f);

    public static final HashMap<Character, Util.PositionRotation> PresetViews;
    static {
        PresetViews = new Util.DefaultHashMap<Character, Util.PositionRotation>(
                new Util.PositionRotation(
                        new PVector(-200, -300, -300),
                        new PVector(-0.33800054f, -0.43132663f, 0.0f)
        ));
        // default
        PresetViews.put('1', new Util.PositionRotation(
            new PVector(-200, -300, -300),
            new PVector(-0.33800054f, -0.43132663f, 0.0f)
        ));
        // close up
        PresetViews.put('2', new Util.PositionRotation(
                new PVector(100, -200, 0), new PVector(0.018700838f, -0.16370988f, 0)
        ));
        // tail
        PresetViews.put('3', new Util.PositionRotation(
                new PVector(-1000.0f, -400.0f, -1100.0f), new PVector(-0.7961495f, -0.4487797f, 0)
        ));
        // left
        PresetViews.put('4', new Util.PositionRotation(
                new PVector(500, -200, 100), new PVector(0.52920985f, -0.18698001f, 0)
        ));
        // very close
        PresetViews.put('5', new Util.PositionRotation(
                new PVector(100, -100, 100), new PVector(0.021973372f, 0.010822058f, 0.0f)
        ));

    }
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

    public Util.Color key_color;

    // mouse rotation
    public Util.PositionRotation pos_rot;
    public PVector prev_rot;
    public PVector click;


    // spectrograph
    public Spectrograph spectro;

    public HashMap<Integer, Integer> midi_counts;
    public int midi_max = 0, midi_min = 999;

    // public boolean is_fullscreen = false;
    boolean awaiting_render_attack;

    public boolean sketchFullScreen() {
        return true;
    }

    private Dimension getDefaultSize() {
        int patch_width = 1280, patch_height = 720;
        if (displayWidth > 1920 && displayHeight > 1080) {
            // go big on nice big monitors
            patch_width = 1920;
            patch_height = 1080;
        }
        if (sketchFullScreen()) {
            patch_height = displayHeight;
            patch_width = displayWidth;
        }
        return new Dimension(patch_width, patch_height);
    }

    public void setup() {
        util = new Util(this);
        spectro = new Spectrograph(this, DEPTH, SPACING, DIRECTION);

        // set up OSC message listener
        OscProperties settings = new OscProperties();
        settings.setListeningPort(PORT);
        settings.setDatagramSize(99999); // big enough

        // basic Processing config
        Dimension p_size = getDefaultSize();
        size(p_size.width, p_size.height, P3D);
        frameRate(FRAMERATE);
        sphereDetail(12);
        //stroke(0);
        noStroke();
        colorMode(HSB);

        // parse incoming OSC datagrams into an object
        parser = new MessageParser();
        current_sound = new SoundData();
        latest_with_pitch = new SoundData();
        midi_counts = new Util.DefaultHashMap<Integer, Integer>(0);

        // values determined by experimentation
        // rotate_x = 100.26076f;
        // rotate_y = 106.40513f;

        // determined by experiment
        click = new PVector();
        prev_rot = new PVector();
        pos_rot = PresetViews.get('1').clone();
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

        // false sound. blackness
        if (sound.bark.length == 0) {
            saturation = 0;
            hue = 0;
            brightness = 0;
        }

        return util.new Color(
                hue,// hue        0-360
                saturation, // saturation 0-100 (nice at 70)
                brightness  // brightness 0-100 (nice at 97)
        );
    }

    // various drawing functions

    public void draw() {
        pushMatrix();
        translate(pos_rot.position.x, pos_rot.position.y, pos_rot.position.z);
        rotateX(pos_rot.rotation.y);
        rotateY(pos_rot.rotation.x);
        rotateZ(pos_rot.rotation.z);

        key_color = color_from_sound(latest_with_pitch);
        background(key_color.clone().setBrightness(key_color.brightness / 2).color());

        if (LIGHTS) lights();

        Util.Color spec_color = key_color.clone().reflect();
        spec_color.setBrightness((int) map(spec_color.brightness, 0, 255, 210, 255));
        spectro.pushPlane(util.doubleToFloatArray(current_sound.bark), spec_color);

        spectro.draw(key_color);

        popMatrix();
    }

    PVector screenTo2pi(PVector v) {
        v.x = map(v.x, 0, width, 0, TWO_PI);
        v.y = map(v.y, 0, height, 0, TWO_PI);
        return v;
    }

    public void mousePressed() {
        // log start location of drag
        click = screenTo2pi(new PVector(mouseX, mouseY));
        prev_rot = pos_rot.rotation;
    }

    public void mouseDragged() {
        PVector drag = screenTo2pi(new PVector(mouseX, mouseY));
        drag.sub(click);
        pos_rot.rotation = prev_rot.get();
        pos_rot.rotation.add(drag);
    }

    public void keyReleased() {
        // zoom
        if (key == 'j') {
            pos_rot.position.z += 100;
        }
        if (key == 'k') {
            pos_rot.position.z -= 100;
        }

        // set position from preset
        if ("1234567890".indexOf(key) > -1) {
            pos_rot = PresetViews.get(key).clone();
        }

        // location
        if (keyCode == UP) {
            pos_rot.position.y -= 100;
        }
        if (keyCode == DOWN) {
            pos_rot.position.y += 100;
        }
        if (keyCode == LEFT) {
            pos_rot.position.x += 100;
        }
        if (keyCode == RIGHT) {
            pos_rot.position.x -= 100;
        }

        // print music debug info
        if (key == 'm') {
            println(midi_counts.toString());
            println("max: "+ midi_max,
                    " min: "+ midi_min);
        }

        // change minimum signal to display cube
        if (key == 'z')
            spectro.signal_min -= 1;
        if (key == 'x')
            spectro.signal_min += 1;

        // print position/angle info
        if (key == 'p') {
            println("Position: " + pos_rot.position.toString());
            println("Rotation: " + pos_rot.rotation.toString());
            println("Signal min: "+spectro.signal_min);
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
