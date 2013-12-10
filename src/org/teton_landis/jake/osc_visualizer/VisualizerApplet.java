package org.teton_landis.jake.osc_visualizer;

import org.kohsuke.args4j.*;
import processing.core.*;
import oscP5.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class VisualizerApplet extends PApplet{

    // constant configuration knobs
    public static final boolean DEFAULT_LIGHTS = false;
    public static final int DEFAULT_SPACING = -200;
    public static final int DEFAULT_DEPTH = 40;
    public static final int DEFAULT_FRAMERATE = 30;
    public static final int DEFAULT_PORT = 11337; // for OSC messages
    public static final Spectrograph.Direction DEFAULT_DIRECTION = Spectrograph.Direction.FORWARD;

    public static class Parameters {
        @Option(name="--lights", usage="Enable default lighting")
        public boolean lights = DEFAULT_LIGHTS;

        @Option(name="--spacing", usage="Space between planes in the spectrograph")
        public int spacing = DEFAULT_SPACING;

        @Option(name="--depth", usage="Number of planes in the spectrograph")
        public int depth = DEFAULT_DEPTH;

        @Option(name="--framerate", usage="FPS target")
        public int framerate = DEFAULT_FRAMERATE;

        @Option(name="--port", usage="UDP port for incoming OSC messages")
        public int port = DEFAULT_PORT;

        @Option(name="--backwards", usage="Run spectrograph back-to-front")
        public boolean go_backwards = false;

        @Option(name="--window", usage="Run in window instead of full screen")
        public boolean window = false;

        @Option(name="--help", usage="Show help text")
        public boolean show_help;

        @Argument
        public List<String> args = new ArrayList<String>();

        public CmdLineException failure;
        public CmdLineParser parser;

        public static Parameters Parse(String[] args_to_parse) {
            Parameters res = new Parameters();
            res.parser = new CmdLineParser(res);

            try {
                res.parser.parseArgument(args_to_parse);
            } catch(CmdLineException e) {
                // non-null failure
                res.failure = e;
            }
            return res;
        }
    }

    // this hashmap is a colleciton of different viewport settings for the visualizer
    // switch between them with the keyboard
    // state '1' is the default
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

    public Util util;
    public Parameters params;

    // spectrograph
    public Spectrograph spectro;

    // for OSC message recieving
    public OscP5 oscP5;
    public MessageParser parser;

    // sound state
    public SoundData current_sound;
    public SoundData latest_with_pitch;
    boolean awaiting_render_attack;
    public HashMap<Integer, Integer> midi_counts;
    public int midi_max = 0, midi_min = 999;

    // stage position/rotation
    public Util.PositionRotation pos_rot;
    public PVector prev_rot;
    public PVector click;

    // other settings
    public Util.Color key_color;
    public boolean use_lights;

    // always fullscreen because --full-screen is broken.
    // see VisualizerWindowed for a version that's not full screen
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
        cursor(WAIT);
        util = new Util(this);

        // parse args
        params = Parameters.Parse(args);
        Spectrograph.Direction dir = DEFAULT_DIRECTION;
        if (params.go_backwards)
            dir = Spectrograph.Direction.BACKWARD;


        // set titlebar if one exists
        if (frame != null) {
            frame.setTitle("OSC Visualizer");
            frame.setResizable(true);
        }

        // basic Processing config
        size(getDefaultSize().width, getDefaultSize().height, P3D); // this takes a while...
        frameRate(params.framerate);
        sphereDetail(12);
        noStroke();
        colorMode(HSB);
        background(0);

        // core of the visualizer
        spectro = new Spectrograph(this, params.depth, params.spacing, dir);

        // parse incoming OSC datagrams into an object
        parser = new MessageParser();
        current_sound = new SoundData();
        latest_with_pitch = new SoundData();

        // keep track of what notes we see
        midi_counts = new Util.DefaultHashMap<Integer, Integer>(0);

        // state for model repositioning
        click = new PVector();
        prev_rot = new PVector();
        pos_rot = PresetViews.get('1').clone();

        // other settings
        use_lights = params.lights;

        // start OSC, set up OSC message listener
        OscProperties settings = new OscProperties();
        settings.setListeningPort(params.port);
        settings.setDatagramSize(99999); // big enough, derp
        oscP5 = new OscP5(this, settings);

        noCursor();
    }

    // function run by oscP5 whenever an OSC messages is recieved
    public void oscEvent(OscMessage mess) {
        // parse incoming OscMessages and then put the result
        // into an Applet field when parsing is complete
        if (parser.parse(mess)) {
            current_sound = parser.flush();
            // not every OSC packet has pitch data
            if (current_sound.hasPitchData()) {
                latest_with_pitch = current_sound;

                // keep track of all incoming MIDI values
                int midi = (int) latest_with_pitch.pitch_raw_midi;
                midi_max = max(midi, midi_max);
                midi_min = min(midi, midi_min);

                // used for statistics
                midi_counts.put(midi, midi_counts.get(midi) + 1);
            }

            if (current_sound.attack)
                awaiting_render_attack = true;
        }
    }


    public void draw() {
        pushMatrix();
        // move and rotate stage
        translate(pos_rot.position.x, pos_rot.position.y, pos_rot.position.z);
        rotateX(pos_rot.rotation.y);
        rotateY(pos_rot.rotation.x);
        rotateZ(pos_rot.rotation.z);

        // background color from pitch
        key_color = color_from_sound(latest_with_pitch);
        background(key_color.clone().setBrightness(key_color.brightness / 2).color());

        if (use_lights) lights();

        // save current sound data into the spectrograph
        Util.Color spec_color = key_color.clone().reflect();
        spec_color.setBrightness((int) map(spec_color.brightness, 0, 255, 210, 255));
        spectro.pushPlane(util.doubleToFloatArray(current_sound.bark), spec_color);

        spectro.draw(key_color);

        popMatrix();
    }



    // UTILITY FUNCTIONS

    // return a background color based on the current MIDI note value
    // returned as a 0xaabbcc color.
    Util.Color color_from_sound(SoundData sound) {
        // map the midi value of this sound to RGB color space
        // between the midi values we've seen
        int hue = (int) map((float) sound.pitch_raw_midi, midi_min, midi_max, 0, 255);
        // magic numbers hand-tuned by experimentation. You can always adjust this by
        // cranking up the source volume ;)
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

    // map mouse to spherical rotation
    PVector screenTo2pi(PVector v) {
        v.x = map(v.x, 0, width, 0, TWO_PI);
        v.y = map(v.y, 0, height, 0, TWO_PI);
        return v;
    }




    // EVENT HANDLERS

    public void mousePressed() {
        // log start location of drag
        click = screenTo2pi(new PVector(mouseX, mouseY));
        prev_rot = pos_rot.rotation;

        cursor(MOVE);
    }

    // rotate stage
    public void mouseDragged() {
        PVector drag = screenTo2pi(new PVector(mouseX, mouseY));
        drag.sub(click);
        pos_rot.rotation = prev_rot.get();
        pos_rot.rotation.add(drag);
    }

    public void mouseReleased() {
        noCursor();
    }

    public void keyReleased() {
        // zoom
        if (key == 'j')
            pos_rot.position.z += 100;
        if (key == 'k')
            pos_rot.position.z -= 100;

        // set position from preset
        if ("1234567890".indexOf(key) > -1) {
            pos_rot = PresetViews.get(key).clone();
        }

        // location
        if (keyCode == UP)
            pos_rot.position.y -= 100;
        if (keyCode == DOWN)
            pos_rot.position.y += 100;
        if (keyCode == LEFT)
            pos_rot.position.x -= 100;
        if (keyCode == RIGHT)
            pos_rot.position.x += 100;

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

        // toggle lights
        if (key == 'l')
            use_lights = (! use_lights);

        // print position/angle info
        if (key == 'p') {
            println("Position: " + pos_rot.position.toString());
            println("Rotation: " + pos_rot.rotation.toString());
            println("Signal min: "+spectro.signal_min);
        }
    }
}
