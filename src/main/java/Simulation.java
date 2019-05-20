import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation {

    private static final int    BASE = 5;                       // DT base
    private static final int    EXP = 6;                        // DT exp
    private static final double DT = BASE * Math.pow(10, -EXP); // Step delta time
    private static final int    N = 100;                         // Number of particles
    private static final double G = -10;                        // Gravity on 'y' axis
    private static final double WIDTH = 0.3;
    private static final double HEIGHT = 1;
    private static final double SLIT_SIZE = 0.15;
    private static final double k = 10e5;
    private static final double gamma = 70;
    private static final double MIN_PARTICLE_R = 0.01;          // Min particle radius
    private static final double MAX_PARTICLE_R = 0.015;         // Max particle radius
    private static final double STEP_PRINT_DT = 1;
    private static final double ANIMATION_DT = 1.0 / 1000;          // DT to save a simulation state
    private static final double MEASURE_DT = 60;                // DT to save a simulation state
    private static final double MAX_SIM_TIME = 5;             // Max simulation time in seconds

    private static double              simTime = 0; //Simulation time in seconds
    private static List<Particle> particles = new ArrayList<>(N);
    private static ArrayList<Wall>     walls = new ArrayList<>(4);

    private static List<List<Particle>> savedStates = new ArrayList<>();
    private static ArrayList<Double> kineticEnergy = new ArrayList<>();
    private static ArrayList<Double> flow = new ArrayList<>();

    public static void main(String[] args) throws Exception{
        PrintWriter writer = new PrintWriter("data/" + N + "_" + BASE + "e-" + EXP + "_simulation.xyz");

        initWalls(WIDTH, HEIGHT, SLIT_SIZE);
        initParticles(N, WIDTH, HEIGHT, MIN_PARTICLE_R, MAX_PARTICLE_R);

        saveMeasures(0);
        writeState(writer);

        int lastFrame = 1, lastMeasure = 1, lastStepPrint = 0;
        System.out.println("Starting simulation");

        List<Particle> outParticles;
        List<Double> exitTimes = new LinkedList<>();

        while(simTime < MAX_SIM_TIME) {
            // Clear forces and add interaction forces with walls to particles and add G force too
            particles.stream().parallel().forEach(p -> {
                p.clearForces();
                p.fy += p.m * G;
                for (Wall w : walls) {
                    if (w.getOverlap(p) > 0) {
                        applyForce(w, p);
                    }
                }
            });

            // Add interaction forces between particles
            IntStream.range(0, particles.size()).parallel().forEach(i -> {
                Particle pi = particles.get(i);

                for (int j = i + 1; j < particles.size(); j++) {
                    Particle pj = particles.get(j);

                    if (pi.getOverlap(pj) > 0) {
                        applyForce(pi, pj);
                    }
                }
            });

            // Move particles a DT time and filter the ones that are out
            outParticles = particles.stream().parallel().peek(p -> {
                p.move(DT);
            }).filter(Simulation::isOut).collect(Collectors.toList());

            // Get all in particles
            //particles = particles.stream().parallel().filter(Simulation::isIn).collect(Collectors.toList());

            // Add DT to simulation time
            simTime += DT;

            // Record exit times
            outParticles.forEach((_) -> exitTimes.add(simTime));

            // For each out particle reinsert it on top comparing to in particles
            outParticles.forEach(Simulation::reinsert);

            if (simTime / STEP_PRINT_DT > lastStepPrint) {
                System.out.println(String.format("simTime: %.2f", simTime));
                lastStepPrint++;
            }

            if (simTime / ANIMATION_DT > lastFrame) {
                writeState(writer);
                lastFrame++;
            }

            if (simTime / MEASURE_DT > lastMeasure) {
                saveMeasures(outParticles.size());
                lastMeasure++;
            }
        }
        saveMeasures(outParticles.size());
        System.out.println("Finished simulation");

        System.out.println("Printing measures");
        writer.close();

        printList(kineticEnergy, "data/" + N + "_" + BASE + "e-" + EXP + "_kineticEnergy.csv");
        printList(exitTimes, "data/" + N + "_" + BASE + "e-" + EXP + "_exitTimes.csv");
        printList(flow, "data/" + N + "_" + BASE + "e-" + EXP + "_flow.csv");

    }

    private static void reinsert(Particle p) {
        p.clearVelocities();

        boolean valid = false;
        while (!valid) {
            p.x = p.r + Math.random() * (WIDTH - 2 * p.r);
            p.y = (2 + Math.random()) * HEIGHT / 3;
            valid = particles.stream().parallel().allMatch(p2 -> p2 == p || p2.getOverlap(p) == 0);
        }
        //particles.add(p);
    }

    private static boolean isOut(Particle p) {
        return p.y <= - HEIGHT / 10;
    }

    private static boolean isIn(Particle p) {
        return !isOut(p);
    }

    private static void applyForce(Particle p1, Particle p2) {

        double enx = p1.enx(p2);
        double eny = p1.eny(p2);

        double normalRelVel = p1.getNormalRelVel(p2);

        double overlap = p1.getOverlap(p2);

        double fn = -k*overlap - gamma*normalRelVel;

        double fx = fn * enx;
        double fy = fn * eny;

        p1.fx += fx;
        p1.fy += fy;
        p2.fx -= fx;
        p2.fy -= fy;
    }

    private static void applyForce(Wall w, Particle p) {

        double normalRelVel = w.getNormalRelVel(p);

        double overlap = w.getOverlap(p);

        double fn = -k*overlap - gamma*normalRelVel;

        double fx = fn * w.enx;
        double fy = fn * w.eny;


        p.fx -= fx;
        p.fy -= fy;
    }

    private static void initWalls(double width, double height, double slitSize) {
        walls.add(new Wall(0, 0, 0, height, 1, 0));
        walls.add(new Wall(0, 0, width, 0, 0, 1));
        walls.add(new Wall(width, 0, width, height, -1, 0));

//        walls.add(new Wall(width / 2, 0, width / 2, (height - slitSize) / 2));
//        walls.add(new Wall(width / 2, (height + slitSize) / 2, width / 2, height));
    }

    private static void initParticles(int n, double width, double height, double minRadius, double maxRadius) {
        while (particles.size() < n) {
            double particleRadius = minRadius + Math.random() * (maxRadius - minRadius);
            double x = particleRadius + Math.random() * (width - 2 * particleRadius);
            double y = particleRadius + Math.random() * (height - 2 * particleRadius);

            Particle newParticle = new Particle(particles.size(), x, y, particleRadius);

            boolean valid = particles.stream().parallel().allMatch(p -> p.getOverlap(newParticle) == 0);

            if (valid) {
                particles.add(newParticle);
            }
        }
    }

    private static void printList(List<Double> list, String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename);
            list.forEach(writer::println);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveMeasures(double outParticlesSize) {
        kineticEnergy.add(particles.parallelStream().map(Particle::kineticEnergy).reduce(0.0, (d1, d2) -> d1 + d2));
        flow.add(outParticlesSize);
    }

    private static void writeState(PrintWriter writer) {
        writer.println(particles.size() + 2);
        writer.println();
        writer.println("-2 0.0 0.0 0.00000001 0.0 0.0");
        writer.println(String.format(Locale.ENGLISH, "-1 %f %f 0.00000001 0.0 0.0", WIDTH, HEIGHT));
        particles.stream().parallel().forEach(writer::println);
    }
}
