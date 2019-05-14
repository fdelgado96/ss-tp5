public class Simulation {

    public static double k = 10e5, gamma = 70, d, ;

    public static void main(String[] args){

    }

    public static void applyForce(Particle p1, Particle p2) {

        double enx = p1.enx(p2);
        double eny = p1.eny(p2);

        double relVel = p1.getRelVel(p2);

        double overlap = p1.getOverlap(p2);

        double fn = -k*overlap - gamma*relVel;

        double fx = fn * enx;
        double fy = fn * eny;

        p1.fx -= fx;
        p1.fy -= fy;
        p2.fx += fx;
        p2.fy += fy;
    }
}
