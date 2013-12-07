/**
 * All information in a complete OSC message from the server
 */
public class SoundData {
    public double loudness,
            brightness,
            noisiness,
            pitch_cooked_hz,
            pitch_cooked_midi,
            pitch_raw_midi,
            pitch_raw_amp;

    public boolean attack;

    public long time;

    public double[] peaks, // don't know what these are
            bark,  // like on the front of a stereo
            frame; // one frame of waveform amplitude data

    public SoundData() {
        // prevent null pointer buisness
        peaks = new double[0];
        bark = new  double[0];
        frame = new double[0];
        time = System.currentTimeMillis();
    }

    public boolean hasPitchData() {
        return (pitch_cooked_hz != 0 || pitch_cooked_midi != 0 || pitch_raw_midi != 0 || pitch_raw_amp != 0);
    }
}
