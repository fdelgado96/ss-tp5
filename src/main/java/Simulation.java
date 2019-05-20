import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation {

    private static final int    BASE = 1;                       // DT base
    private static final int    EXP = 5;                        // DT exp
    private static final double DT = BASE * Math.pow(10, -EXP); // Step delta time
    private static final int    N = 300;                         // Number of particles
    private static final double G = -10;                        // Gravity on 'y' axis
    private static final double WIDTH = 0.3;
    private static final double HEIGHT = 1;
    private static final double SLIT_SIZE = 0.15;
    private static final double k = 10e5;
    private static final double gamma = 70;
    private static final double MIN_PARTICLE_R = 0.01;          // Min particle radius
    private static final double MAX_PARTICLE_R = 0.015;         // Max particle radius
    private static final double STEP_PRINT_DT = 0.1;
    private static final double ANIMATION_DT = 1.0 / 60;          // DT to save a simulation state
    private static final double MEASURE_DT = 60;                // DT to save a simulation state
    private static final double MAX_SIM_TIME = 10;             // Max simulation time in seconds

    public static double              simTime = 0; //Simulation time in seconds
    private static List<Particle> particles = new ArrayList<>(N);
    private static ArrayList<Wall>     walls = new ArrayList<>(4);

    private static List<List<Particle>> savedStates = new ArrayList<>();
    private static ArrayList<Double> kineticEnergy = new ArrayList<>();


    public static void main(String[] args) throws Exception{

        PrintWriter writer = new PrintWriter("data/" + N + "_" + BASE + "e-" + EXP + "_simulation.xyz");

        initWalls(WIDTH, HEIGHT, SLIT_SIZE);
        initParticles(N, WIDTH, HEIGHT, MIN_PARTICLE_R, MAX_PARTICLE_R);
        CellIndexMethod cellIndexMethod;
        saveMeasures();
        writeState(writer);

        int lastFrame = 1, lastMeasure = 1, lastStepPrint = 0;
        System.out.println("Starting simulation");

        List<Particle> outParticles = new ArrayList<>();

        while(simTime < MAX_SIM_TIME) {
            cellIndexMethod = new CellIndexMethod(particles, WIDTH, HEIGHT, 2*MAX_PARTICLE_R);
            for(Particle particle : particles){
                particle.clearForces();
                particle.fy += particle.m * G;
                for(Wall wall : walls){
                    if(wall.getOverlap(particle) > 0)
                        applyForce(wall, particle);
                }
            }
            cellIndexMethod.calculateForces();

            outParticles = particles.stream().parallel().peek(p -> {
                p.move(DT);
            }).filter(Simulation::isOut).collect(Collectors.toList());

            outParticles.stream().forEach(Simulation::reinsert);

            simTime += DT;

            if (simTime / STEP_PRINT_DT > lastStepPrint) {
                System.out.println(String.format("simTime: %.2f", simTime));
                lastStepPrint++;
            }
            if (simTime / ANIMATION_DT > lastFrame) {
                writeState(writer);
                lastFrame++;
            }
            if (simTime / MEASURE_DT > lastMeasure) {
                saveMeasures();
                lastMeasure++;
            }
        }

        saveMeasures();
        System.out.println("Finished simulation");

        System.out.println("Printing measures");
        writer.close();

        printList(kineticEnergy, "data/" + N + "_" + BASE + "e-" + EXP + "_kineticEnergy.csv");

    }

    private static void reinsert(Particle p) {
        p.clearVelocities();

        boolean valid = false;
        while (!valid) {
            p.x = p.r + Math.random() * (WIDTH - 2 * p.r);
            p.y = (2 + Math.random()) * HEIGHT / 3 - MAX_PARTICLE_R*1.1;
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


    private static void applyForce(Wall w, Particle p) {

        double normalRelVel = w.getNormalRelVel(p);

        double overlap = w.getOverlap(p);

        double fn = -k*overlap - gamma*normalRelVel;

        double fx = fn * w.enx;
        double fy = fn * w.eny;


        p.fx += fx;
        p.fy += fy;
    }

    private static void initWalls(double width, double height, double slitSize) {
        walls.add(new Wall(0, height, width, height, 0, 1));

        walls.add(new Wall(0, 0, 0, height, -1, 0));
        walls.add(new Wall(0, 0, width, 0, 0, -1));
        walls.add(new Wall(width, 0, width, height, 1, 0));

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

    private static void printList(ArrayList<Double> list, String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename);
            list.forEach(writer::println);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveMeasures() {
        kineticEnergy.add(particles.parallelStream().map(Particle::kineticEnergy).reduce(0.0, (d1, d2) -> d1 + d2));
    }

    private static void writeState(PrintWriter writer) {
        writer.println(particles.size() + 2);
        writer.println();
        writer.println("-2 0.0 0.0 0.00000001 0.0 0.0");
        writer.println(String.format(Locale.ENGLISH, "-1 %f %f 0.00000001 0.0 0.0", WIDTH, HEIGHT));
        particles.stream().parallel().forEach(writer::println);
    }
}
