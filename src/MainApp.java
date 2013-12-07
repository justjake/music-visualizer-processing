import processing.core.*;
import oscP5.*;

import java.util.Arrays;
import java.util.Collections;

public class MainApp extends PApplet{

    // display params for the cube thing
    public static final int SPACING = -200;
    public static final int DEPTH = 20;

    public static final int PORT = 11337; // for OSC messages

    // a whole lotta state
    public Util util;

    public OscP5 oscP5;
    public SoundData current_sound;
    public SoundData latest_with_pitch;
    public MessageParser parser;

    public float[][] planes;
    public Grid grid;

    public int key_color;

    // mouse rotation
    public PVector rotation;
    public PVector prev_rot;
    public PVector click;


    public void setup() {
        util = new Util(this);
        planes = new float[DEPTH][];

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
        frameRate(30);
        sphereDetail(12);
        //stroke(255);
        noStroke();
        colorMode(HSB);

        // parse incoming OSC datagrams into an object
        parser = new MessageParser();
        current_sound = new SoundData();
        latest_with_pitch = new SoundData();

        // values determined by experimentation
        // rotate_x = 100.26076f;
        // rotate_y = 106.40513f;
        rotation = new PVector();
        prev_rot = new PVector();

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
            if (current_sound.hasPitchData())
                latest_with_pitch = current_sound;
        }
    }

    // Utility functions
    
    // return a background color based on the current MIDI note value
    // returned as a 0xaabbcc color.
    int color_from_sound(SoundData sound) {
        int hue = (int) map((float) sound.pitch_raw_midi, 20, 120, 0, 255);
        int brightness = (int) map((float) sound.loudness, -15, 0, 80, 0.8f * 255.0f);
        int saturation = 70;

        if (sound.attack==true) {
            saturation = 0;
            brightness = 255;
        }

        return color(
                hue,// hue        0-360
                saturation, // saturation 0-100 (nice at 70)
                brightness,  // brightness 0-100 (nice at 97)
                0.08f * 255
        );
    }

    // various drawing functions
    void draw_planes() {
        int cube_space = (int) ((width * .3) / 25.0);
        int cube_height = (int) ((width * .7) / 25.0);
        //println("space="+cube_space+" height="+cube_height);
        // spectrograph columns

        // push
        planes[0] = util.doubleToFloatArray(current_sound.bark);
        Collections.rotate(Arrays.asList(planes), -1);

        noStroke();
        for (int p=0; p<planes.length && planes[p] != null; p++) {
            int z = SPACING * p;
            // depth shading. there should be a way to do this with light, though.
            float frontness = map(p, 0, planes.length-1, 1, 0);
            int box_color = lerpColor(key_color, color(0, 0, 255, 0.8f * 255), 1.0f-frontness);
            if (p==planes.length-1) box_color = color(0,0, 255, 255);
            fill(box_color);
            //if (p==0) fill(230);
            //if (latest_with_pitch.attack) fill(255);

            for (int i=0; i<planes[0].length; i++) {
                pushMatrix();
                // translate to col start location
                int x = cube_space + (int) ((cube_space + cube_height)*(i + 0.5));
                int y = height + cube_height + cube_space;
                translate(x, y, z);

                float mini = min(planes[p]);
                int cubes = (int) map(planes[p][i], -15, 30, 0, 31.0f);

                for (int j=0; j<cubes; j++) {
                    // draw some cubes bro
                    //rect(0, 0, cube_height, cube_height);
                    box(cube_height);
                    //sphere(cube_height / 2);
                    translate(0, -1 * (cube_space + cube_height), 0);
                }

                popMatrix();
            }
        }
    }


    void draw_grid() {
        stroke(255);
        grid.width = width/5;
        grid.height = height/10;
        grid.draw(width, height);
    }

    public void draw() {

        rotateX(rotation.y);
        rotateY(rotation.x);

        key_color = color_from_sound(latest_with_pitch);
        background(key_color);
        // lights();
        draw_planes();
        // draw_grid();
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
