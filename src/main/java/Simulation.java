import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.IntStream;

public class Simulation {

    private static final int    BASE = 5;                       // DT base
    private static final int    EXP = 4;                        // DT exp
    private static final double DT = BASE * Math.pow(10, -EXP); // Step delta time
    private static final int    N = 100;                        // Number of particles
    private static final double G = -10;                        // Gravity on 'y' axis
    private static final double WIDTH = 400;
    private static final double HEIGHT = 200;
    private static final double SLIT_SIZE = 10;
    private static final double k = 10e5;
    private static final double gamma = 70;
    private static final double MIN_PARTICLE_R = 0.01;          // Min particle radius
    private static final double MAX_PARTICLE_R = 0.015;         // Max particle radius
    private static final double RM = 1;                         // Min initial distance between particles
    private static final double R_MAX_SQ = 25;                  // Max interaction distance (squared)
    private static final double ANIMATION_DT = 1 / 24;          // DT to save a simulation state
    private static final double MAX_SIM_TIME = 100;             // Max simulation time in seconds

    private static double              simTime = 0; //Simulation time in seconds
    private static ArrayList<Particle> particles = new ArrayList<>(N);
    private static ArrayList<Wall>     walls = new ArrayList<>(4);

    private static ArrayList<ArrayList<Particle>> savedStates = new ArrayList<>();

    public static void main(String[] args) throws Exception{
        PrintWriter writer = new PrintWriter("data/" + N + "_" + BASE + "e-" + EXP + "_simulation.xyz");
        saveState(particles);

        initWalls(WIDTH, HEIGHT, SLIT_SIZE);
        initParticles(N, WIDTH, HEIGHT, RM, MIN_PARTICLE_R, MAX_PARTICLE_R);

        int lastFrame = 1;
        System.out.println("Starting simulation");

        while(simTime < MAX_SIM_TIME) {
            // Clear forces and add interaction forces with walls to particles and add G force too
            particles.stream().parallel().forEach(p -> {
                p.clearForces();
                p.fy += G;
                for (Wall w : walls) {
                    double dist_sq = w.distance_sq(p);
                    if (dist_sq <= R_MAX_SQ) {
                        applyForce(w, p);
                    }
                }
            });

            // Add interaction forces between particles
            IntStream.range(0, particles.size()).parallel().forEach(i -> {
                Particle pi = particles.get(i);

                for (int j = i + 1; j < particles.size(); j++) {
                    Particle pj = particles.get(j);

                    double dist_sq = pi.centerDistanceSQ(pj);
                    if (dist_sq <= R_MAX_SQ) {
                        applyForce(pi, pj);
                    }
                }
            });

            // Move particles a DT time
            particles.stream().parallel().forEach(p -> p.move(DT));

            // Add DT to simulation time
            simTime += DT;

            if (simTime / ANIMATION_DT > lastFrame) {
                saveState(particles);
                lastFrame++;
            }
        }
        System.out.println("Simulation ended");

    }


    private static void applyForce(Particle p1, Particle p2) {

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

    private static void applyForce(Wall wall, Particle p2, double distSQ) {
    }

    private static void initWalls(double width, double height, double slitSize) {
        walls.add(new Wall(0, 0, 0, height));
        walls.add(new Wall(0, 0, width, 0));
        walls.add(new Wall(0, height, width, height));
        walls.add(new Wall(width, 0, width, height));

        walls.add(new Wall(width / 2, 0, width / 2, (height - slitSize) / 2));
        walls.add(new Wall(width / 2, (height + slitSize) / 2, width / 2, height));
    }

    private static void initParticles(int n, double width, double height, double rMinDistance, double minRadius, double maxRadius) {
        while (particles.size() < n) {
            double x = rMinDistance + Math.random() * (width / 2 - 2 * rMinDistance);
            double y = rMinDistance + Math.random() * (height - 2 * rMinDistance);
            double particleRadius = minRadius + Math.random() * (maxRadius - minRadius);

            Particle newParticle = new Particle(particles.size(), x, y, particleRadius);

            boolean valid = particles.stream().parallel().allMatch(p -> p.isValid(newParticle, rMinDistance));

            if (valid) {
                particles.add(newParticle);
            }
        }
    }

    private static void saveState(ArrayList<Particle> particles) {
        savedStates.add(particles);
    }

    private static void writeStates(PrintWriter writer, ArrayList<Particle> particles) {
        savedStates.forEach(state -> {
            writer.println(particles.size() + 2);
            writer.println();
            writer.println("-2 0.0 0.0 0.00000001 0.0 0.0");
            writer.println(String.format(Locale.ENGLISH, "-1 %f %f 0.00000001 0.0 0.0", WIDTH, HEIGHT));
            particles.stream().parallel().forEach(writer::println);
        });
    }
}
