package characters;

public class Point {
    public double x;
    public double y;
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    public static double getDistance(Point first, Point second){
        double dx = first.x - second.x;
        double dy = first.y - second.y;
        
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}
