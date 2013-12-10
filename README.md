# Music Visualizer

This is my final project in Music 158 at UC Berkeley.

Receives OSC messages with audio data on port 11337 from a server built in Max MSP with
the CNMAT externals library.

## Requirements

- Processing 2.x, a creative programming environment and library:
  http://processing.org/reference/

- osc5p, a library for receiving Open Sound Control messages in Processing/Java:
  http://www.sojamo.de/libraries/oscP5/

- Max MSP or Max MSP Runtime (32-bit only), a proprietary visual programming system with
  excellent sound tools.
  http://cycling74.com/downloads/runtime/


## Usage

Run by double-clicking the "client" jar, or running `java -jar osc_visualizer.jar`. From the built-in
program help:

    OSC Visualizer
      (c) Jake Teton-Landis, 2013
      <just.1.jake@gmail.com>
      https://github.com/justjake/music-visualizer-processing
      --window to run in a window
      --help for help
    Controls:
      Arrow keys   : change model offest on X/Y axis
      mouse        : change model rotation
      j/k          : change model offset on Z axis
      1 through 0  : view preset offset/rotation
      p            : print position info
      z/k          : change signal minimum cuttoff
      m            : print MIDI value debug info
    Command-Line Options:
     --backwards   : Run spectrograph back-to-front
     --depth N     : Number of planes in the spectrograph
     --framerate N : FPS target
     --help        : Show help text
     --lights      : Enable default lighting
     --port N      : UDP port for incoming OSC messages
     --spacing N   : Space between planes in the spectrograph
     --window      : Run in window instead of full screen

## So Pretty!

### Video

https://www.youtube.com/watch?v=E9wJh83FKSY

### Screenshots

![preset1](https://f.cloud.github.com/assets/296279/1713173/2754500e-6185-11e3-8efe-db235b09e1d0.PNG)
![far-purple](https://f.cloud.github.com/assets/296279/1713176/2d42d4f4-6185-11e3-8343-8154fc65f212.PNG)
![server](https://f.cloud.github.com/assets/296279/1713517/c63955fa-618c-11e3-85fe-3ff8bd351390.PNG)


