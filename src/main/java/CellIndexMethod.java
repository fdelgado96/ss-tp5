

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
    private double cellWidth, cellHeight;
    private ParticleNode[][] grid;
    private HashMap<String, ParticleNode> particleNodeMap = new HashMap<>();
    private static final double k = 10e5;
    private static final double gamma = 70;

    public CellIndexMethod(List<Particle> particles, double width, double height, double interactionRadius) {
        this.M = (int)(width/interactionRadius);
        this.N = (int)(height*1.1/interactionRadius)+1;
        cellWidth = width/M;
        cellHeight = height/N;
        grid = new ParticleNode[N][M];
        for(Particle p : particles) {
            int cellXIndex = (int)(p.x/cellWidth);
            int cellYIndex = (int)(p.y/cellHeight);
            ParticleNode particleNode = particleNodeMap.get(String.format("%d-%d", cellYIndex, cellXIndex));
            if(particleNode != null){
                particleNode.particleList.add(p);
            } else {
                LinkedList<Particle> particlesList = new LinkedList<>();
                particlesList.add(p);
                particleNode = new ParticleNode(particlesList);
                particleNodeMap.put(String.format("%d-%d", cellYIndex, cellXIndex), particleNode);
            }
            grid[cellYIndex][cellXIndex] = particleNode;
        }

    }

    private void checkUp(int y, int x){
        if(outOfGrid(1, y+1) || grid[y+1][x] == null)
            return;
        check(grid[y][x], grid[y+1][x]);
    }

    private void checkUpRight(int y, int x){
        if(outOfGrid(0, x+1) || outOfGrid(1, y+1) || grid[y+1][x+1] == null)
            return;
        check(grid[y][x], grid[y+1][x+1]);
    }

    private void checkRight(int y, int x){
        if(outOfGrid(0, x+1) || grid[y][x+1] == null)
            return;
        check(grid[y][x], grid[y][x+1]);
    }

    private void checkDownRight(int y, int x){
        if(outOfGrid(1, y-1) || grid[y-1][x] == null)
            return;
        check(grid[y][x], grid[y-1][x]);

    }

    private boolean outOfGrid(int axis, int index){
        if(axis == 0){
            return index >= M || index < 0;
        } else {
            return index >= N || index < 0;
        }
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

    private void checkSelf(int y, int x){
        if(grid[y][x] == null){
            return;
        }
        ParticleNode node = grid[y][x];
        int max = node.particleList.size();
        for(int i = 0; i < max; i++){
            for(int j = i+1; j < max; j++){
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


    public void calculateForces() {
        for(int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                if(grid[i][j] == null)
                    continue;

                checkSelf(i, j);
                checkUp(i, j);
                checkUpRight(i, j);
                checkRight(i, j);
                checkDownRight(i, j);
            }
        }
    }


}
