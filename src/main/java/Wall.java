public class Wall {

    public double initialX, initialY, finalX, finalY;
    private boolean horizontal;

    public Wall(double initialX, double initialY, double finalX, double finalY) {
        this.initialX = initialX;
        this.initialY = initialY;
        this.finalX = finalX;
        this.finalY = finalY;

        if (initialY == finalY) {
            horizontal = true;
        } else if (initialY != finalY) {
            throw new RuntimeException("Diagonal walls aren't supported.");
        }
    }

    private double particleCenterToWall(Particle particle) {
        if(
            Math.sqrt(Math.pow(particle.x - initialX,  2) + Math.pow(particle.y - initialY, 2)) > particle.r ||
            Math.sqrt(Math.pow(particle.x - finalX,  2) + Math.pow(particle.y - finalY, 2)) > particle.r
        ){
            return particle.r+1;
        }

        if(horizontal) {
            return particle.y -initialY;
        }

        return particle.x -initialX;
    }

    public double getOverlap(Particle particle){
        double overlap = particle.r - particleCenterToWall(particle);
        return overlap >= 0 ? overlap : 0;
    }

    public double getNormalRelVel(Particle particle) {
        if(horizontal)
            return particle.vy;
        else
            return particle.vx;
    }


}
