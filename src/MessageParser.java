import oscP5.*;
import processing.core.PApplet;

/**
 * build a SoundData object from successive OSC messages until a complete
 * bundle is re-constructed and then flush()'d
 */
class MessageParser {
    SoundData data;

    MessageParser() {
        flush();
    }

    double[] doubleArray(OscMessage mess) {
        String typetag = mess.typetag();
        int size;
        // find the right length, screening out string values "N"
        size = typetag.indexOf('s');
        if (size == -1) size = typetag.length();

        double[] res = new double[size];

        for (int i=0; i<size; i++) {
            // i hope branch mispredicts here aren't too slow
            if (typetag.charAt(i) == 'i') {
                res[i] = (double) (mess.get(i).intValue());
                continue;
            }

            res[i] = mess.get(i).doubleValue();
        }

        return res;
    }

    Boolean checkDouble(OscMessage mess, String tag) {
        return mess.checkAddrPattern(tag) && mess.checkTypetag("d");
    }

    /*
    Ideally this should be done with reflection, but that's a bit more complicated than
    its worth right now.
     */
    Boolean parse(OscMessage mess) {
        // seems like each AddrPattern will need its own if
        // time implemented first because it is a special case
        if (mess.checkAddrPattern("/time")) {
            // data.time = mess.get(0).doubleValue(); // time has typetag t
            data.time = System.currentTimeMillis(); // innacurate to the sound but ez
            return true; // always the last message, done parsing.
        }

        // arrays
        if (mess.checkAddrPattern("/peaks")) {
            data.peaks = doubleArray(mess);
            return false;
        }

        if (mess.checkAddrPattern("/bark")) {
            data.bark = doubleArray(mess);
            return false;
        }

        // may contain some integer values and thus derp a bit
        if (mess.checkAddrPattern("/frame")) {
            data.frame = doubleArray(mess);
            return false;
        }

        // values that always exist
        if (checkDouble(mess, "/loudness")) {
            data.loudness = mess.get(0).doubleValue();
            return false;
        }

        if (checkDouble(mess, "/brightness")) {
            data.brightness = mess.get(0).doubleValue();
            return false;
        }

        if (checkDouble(mess, "/noisiness")) {
            data.noisiness = mess.get(0).doubleValue();
            return false;
        }

        // pitch detection values -- only sometimes
        if (checkDouble(mess, "/pitch/raw/midi")) {
            data.pitch_raw_midi = mess.get(0).doubleValue();
            return false;
        }
        if (checkDouble(mess, "/pitch/raw/amp")) {
            data.pitch_raw_amp = mess.get(0).doubleValue();
            return false;
        }
        if (checkDouble(mess, "/pitch/cooked/midi")) {
            data.pitch_cooked_midi = mess.get(0).doubleValue();
            return false;
        }
        if (checkDouble(mess, "/pitch/cooked/hz")) {
            data.pitch_cooked_hz = mess.get(0).doubleValue();
            return false;
        }

        // ATTACK!!!
        if(mess.checkAddrPattern("/attack")) {
            data.attack = true;
            return false;
        }

        // pitch values that exist only sometimes

        // missed all other signals. log everything
        PApplet.println("### unknown message type detected ###");
        PApplet.print(" addrpattern: "+mess.addrPattern());
        PApplet.println(" typetag: "+mess.typetag());
        return false;
    }

    /**
     * get the current SoundData and start building a new one.
     * the parse loop looks like
     * if (parser.parse(message)) { return parser.flush() }
     */
    SoundData flush() {
        SoundData ret = this.data;
        this.data = new SoundData();
        return ret;
    }
}