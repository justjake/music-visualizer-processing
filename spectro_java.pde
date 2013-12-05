// OSC stuff to communicate with the sound server
import oscP5.*;
import netP5.*;

static int W = 1000;
static int H = 500;
static int PORT = 11337;
static int MAX_AMPLITUDE = 200; // scale factor for bark drawing

OscP5 oscP5;
NetAddress remote;
SoundData latest_data;
SoundDataParser parser = new SoundDataParser();

Spectrograph spectro;
Spectrograph wave;

void setup() {
  size(W, H); 
  //noLoop();
  frameRate(30);
  
  OscProperties props = new OscProperties();
  props.setListeningPort(PORT);
  props.setDatagramSize(99999);
  oscP5 = new OscP5(this, props);
  
  spectro = new Spectrograph(64);
  wave = new Spectrograph(64);
  latest_data = new SoundData();
}

void draw() {
  // replace specrograph
  // TODO: why can't i just set the field value with a float[]???
  float[] bark = latest_data.bark.toFloatArray();
  float[] frame = latest_data.frame.toFloatArray();

  for (int i=0; i<latest_data.bark.length; i++) {
    bark[i] = bark[i] + 96.0;
  }

  for (int i=0; i<latest_data.frame.length; i++) {
    frame[i] = frame[i] + 1.0;
  }
  
  spectro.size = latest_data.bark.length;
  spectro.graph = bark;
  
  wave.size = latest_data.frame.length;
  wave.graph = frame;
  
  background(200);
  spectro.draw(1000, 250, MAX_AMPLITUDE); // max amplitude determined by some rough experimentation
  
  translate(0, H/2);
  wave.draw(W, H/2, 2.0); // between 1 and -1 by definition
  
  // magic numbers create a nice opacity from the microphone's typical value.
  // stroke(20, (float) ((27.0 + latest_data.loudness) * 40));
  stroke(20);
}

/* handle the latest OSC message by printing the peaks meter */
void mouseClicked() {
  // just an alias
  double_arr peaks = latest_data.bark;
  
  for (int i=0; i<peaks.length; i++) {
    print("barks["+i+"] -> " + peaks.data[i] + ", ");
  }
  println("length = " + peaks.length);
  println("loudness = " + latest_data.loudness);
  redraw();
}

/* trggered on incomming OSC messages to the oscP5 object 
 * executes in its own thread, so must be thread safe, i think.
*/
void oscEvent(OscMessage mess) {
  //  print("### received an osc message.");
  //  print(" addrpattern: "+mess.addrPattern());
  //  println(" typetag: "+mess.typetag());
  
  if (parser.parse(mess)) {
    latest_data = parser.flush();
    // println("finished parsing an OSC bundle");
  }
}
