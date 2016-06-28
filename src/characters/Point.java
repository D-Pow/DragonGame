package characters;

public class Point {
    public double x;
    public double y;
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    /**
     * Gets the distance between two points.
     * 
     * @param first
     *          First point
     * @param second
     *          Second point
     * @return 
     *          Double value of the distance between points
     */
    public static double getDistance(Point first, Point second){
        double dx = first.x - second.x;
        double dy = first.y - second.y;
        
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}
