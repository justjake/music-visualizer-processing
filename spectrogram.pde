class SpaceMap {
  float from_width,
    from_max_y,
    to_width,
    to_height;
    
  SpaceMap(float fw, float max_y, float tw, float th) {
    from_width = fw;
    from_max_y = max_y;
    to_width = tw;
    to_height = th;
  }
  
  float X(float x) {
    return (to_width/from_width) * x;
  }
  
  float Y(float y) {
    return to_height - (to_height / from_max_y) * y;
  }
}
    
/**
 * a graph
 */
class Spectrograph {
  public int size = 64;
  public float[] graph;
  
  Spectrograph(float[] g, int s) {
    size = s;
    graph = g;
  }
  
  Spectrograph(int s) {
    size = s;
    graph = new float[s];
    this.randomize(0.0, 1.0);
  }
  
  void mutate(float coeff) {
    for (int i = 0; i < size; i++) {
      graph[i] = random(0, coeff) * graph[i];
    }
  }
  
  void randomize(float low, float high) {
    for (int i=0; i < size; i++) {
      graph[i] = random(low, high);
    }
  }
  
  /**
   * plot all points in the base graph into screen-space coorinates as
   * an ordered array of line point PVectors
   */
  PVector[] plot(float w, float h, float max_Y) {
    SpaceMap smap = new SpaceMap(size, max_Y, w, h);
    PVector[] plotted = new PVector[size];
    
    for (int i=0; i<size; i++) {
      plotted[i] = new PVector(smap.X(i), smap.Y(graph[i]));
    }
    
    return plotted;
  }
  
  void draw(float w, float h, float max_Y) {
    PVector[] points = this.plot(w, h, max_Y);
    for (int i=1; i<size; i++) {
      line(points[i-1].x, points[i-1].y, points[i].x, points[i].y);
    }
  }
}
