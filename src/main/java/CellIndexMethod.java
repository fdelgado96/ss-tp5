

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CellIndexMethod {

    private class ParticleNode{
        public LinkedList<Particle> particleList;

        public ParticleNode(LinkedList<Particle> particleList) {
            this.particleList = particleList;
        }
    }

    private int N, M; //The grid is of N rows by M columns
    private double cellLength, cellHeight;
    private ParticleNode[][] grid;
    private HashMap<String, ParticleNode> particleNodeMap = new HashMap<>();
    private static final double k = 10e5;
    private static final double gamma = 70;

    public CellIndexMethod(List<Particle> particles, double length, double height, double interactionRadius) {
        this.N = (int)(length/interactionRadius);
        cellLength = length/N;
        cellHeight = length/M;
        this.M = (int)(height/interactionRadius);
        grid = new ParticleNode[N][M];
        for(Particle p : particles) {
            int cellXIndex = (int)(p.x/cellLength);
            int cellYIndex = (int)(p.y/cellHeight);
            ParticleNode particleNode = particleNodeMap.get(String.format("%d-%d", cellXIndex, cellYIndex));
            if(particleNode != null){
                particleNode.particleList.add(p);
            } else {
                LinkedList<Particle> particlesList = new LinkedList<>();
                particlesList.add(p);
                particleNode = new ParticleNode(particlesList);
                particleNodeMap.put(String.format("%d-%d", cellXIndex, cellYIndex), particleNode);
            }
            grid[cellXIndex][cellYIndex] = particleNode;
        }

    }

    private void checkUp(int x, int y){
        if(y > M)
            return;
        check(grid[x][y], grid[x][y+1]);
    }


    private void checkUpRight(int x, int y){
        if(y > M || x > N)
            return;
        check(grid[x][y], grid[x+1][y+1]);
    }


    private void checkRight(int x, int y){
        if(x > N)
            return;
        check(grid[x][y], grid[x+1][y]);
    }


    private void checkDownRight(int x, int y){
        if(y > M)
            return;
        check(grid[x][y], grid[x][y-1]);
    }

    private void check(ParticleNode node, ParticleNode otherNode){
        for(Particle particle : node.particleList){
            for(Particle otherParticle : otherNode.particleList){
                if (particle.getOverlap(otherParticle) > 0) {
                    applyForce(particle, otherParticle);
                }
            }
        }
    }

    private void checkSelf(int x, int y){
        ParticleNode node = grid[x][y];
        int max = node.particleList.size();
        for(int i = 0; i < max; i++){
            for(int j = i; j < max; j++){
                if (node.particleList.get(i).getOverlap(node.particleList.get(j)) > 0) {
                    applyForce(node.particleList.get(i), node.particleList.get(j));
                }
            }
        }
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


    public void calculateForces() {
        for(int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                checkSelf(i, j);
                checkUp(i, j);
                checkUpRight(i, j);
                checkRight(i, j);
                checkDownRight(i, j);
            }
        }
    }


}
