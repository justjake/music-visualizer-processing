class double_arr {
  public double[] data;
  public int length;
  
  double_arr(double[] d, int l) {
    length = l;
    data = d;
  }
  
  double_arr() {
    data = new double[]{0};
    length = 1;
  }
  
  float[] toFloatArray() {
    float[] fs = new float[this.length];
    for (int i=0; i<this.length; i++) {
      fs[i] = (float) this.data[i];
    }
    return fs;
  }
}

class SoundData {
  double loudness,
        brightness,
        noisiness;
  
  double time;
  
  double_arr peaks;
  double_arr bark;
  double_arr frame;
  public SoundData() {
    // prevent null pointer buisness
    peaks = new double_arr();
    bark = new double_arr();
    frame = new double_arr();
  }
}

class SoundDataParser {
  SoundData data;
  
  SoundDataParser() {
    flush();
  }
  
  // hard-coded double array creator... i see no way to do
  // this sort of thing sensibly in a statically-typed closure-free language
  double_arr doubleArray(OscMessage mess) {
    String typetag = mess.typetag();
    int size;
    double[] res;
    
    // find the right length, screening out string values "N"
    size = typetag.indexOf('s');
    if (size == -1) size = typetag.length();
    
    res = new double[size];
    
    for (int i=0; i<size; i++) {
      // i hope branch mispredicts here aren't too slow
      if (typetag.charAt(i) == 'i') {
        res[i] = (double) mess.get(i).intValue();
        continue;
      }
      
      res[i] = mess.get(i).doubleValue();
    }
    
    return new double_arr(res, size);
  }
  
  Boolean checkDouble(OscMessage mess, String tag) {
    return mess.checkAddrPattern(tag) && mess.checkTypetag("d");
  }
  
  Boolean parse(OscMessage mess) {
    // seems like each AddrPattern will need its own if
    // time implemented first because it is a special case
    if (mess.checkAddrPattern("/time")) {
      // data.time = mess.get(0).doubleValue(); // time has typetag t
      return true; // always the last message, done parsing.
    }
    
    // arrays
    if (mess.checkAddrPattern("/peaks")) {
      data.peaks = doubleArray(mess);
    }
    
    if (mess.checkAddrPattern("/bark")) {
      data.bark = doubleArray(mess);
    }
    
    // may contain some integer values and thus derp a bit
    if (mess.checkAddrPattern("/frame")) {
      data.frame = doubleArray(mess);
    }
        
    // scalars
    if (checkDouble(mess, "/loudness")) {
      data.loudness = mess.get(0).doubleValue();
    }
    
    if (checkDouble(mess, "/brightness")) {
      data.brightness = mess.get(0).doubleValue();
    }
    
    if (checkDouble(mess, "/noisiness")) {
      data.noisiness = mess.get(0).doubleValue();
    }
    
    return false;
  }
  
  SoundData flush() {
    SoundData ret = this.data;
    this.data = new SoundData();
    return ret;
  }
}
