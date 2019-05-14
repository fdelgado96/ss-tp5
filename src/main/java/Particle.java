public class Particle {

    public double x, y, vx, vy, fx, fy, m = 0.01, prev_x, prev_y, r;

    private int id;

    public Particle(int id, double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.id = id;
    }

    public double getRelVel(Particle other) {
        double overlap = r + other.r - centerDistance(other);
        return overlap >= 0 ? overlap : 0;
    }

    public double getOverlap(Particle other) {
        double overlap = r + other.r - centerDistance(other);
        return overlap >= 0 ? overlap : 0;
    }

    public double getOverlap(Wall wall) {
        double closestWallX = 1;
        double closestWallY = 1;
        double overlap = r - Math.sqrt(Math.pow(closestWallX, 2) + Math.pow(closestWallY, 2));
        return overlap >= 0 ? overlap : 0;
    }

    public double centerDistance(Particle other){
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
    }

    public double enx(Particle other) {
        return (other.x - x)/centerDistance(other);
    }

    public double eny(Particle other) {
        return (other.y - y)/centerDistance(other);
    }

//    private double[] closestWallXY(Wall wall) {
//        double wx = 0, wy = 0;
//        if (wall.horizontal) {
//            wx = x1 - p.x;
//            double lower = y1 < y2 ? y1 : y2;
//            double upper = y1 < y2 ? y2 : y1;
//            if (p.y < lower) {
//                wy = lower - p.y;
//            } else if (p.y > upper) {
//                wy = upper - p.y;
//            }
//        } else {
//            dy = y1 - p.y;
//            double lower = x1 < x2 ? x1 : x2;
//            double upper = x1 < x2 ? x2 : x1;
//            if (p.x < lower) {
//                dx = lower - p.x;
//            } else if (p.x > upper) {
//                dx = upper - p.x;
//            }
//        }
//        return new double[2](){wx, wy};
//    }
}
