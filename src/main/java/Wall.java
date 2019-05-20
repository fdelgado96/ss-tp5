public class Wall {

    public double initialX, initialY, finalX, finalY, enx, eny;

    public Wall(double initialX, double initialY, double finalX, double finalY, double enx, double eny) {
        this.initialX = initialX;
        this.initialY = initialY;
        this.finalX = finalX;
        this.finalY = finalY;
        this.enx = enx;
        this.eny = eny;
    }

    private double particleCenterToWall(Particle particle) {
//        if (Math.sqrt(Math.pow(particle.x - initialX,  2) + Math.pow(particle.y - initialY, 2)) > particle.r &&
//            Math.sqrt(Math.pow(particle.x - finalX,  2) + Math.pow(particle.y - finalY, 2)) > particle.r) {
//            return particle.r+1;
//        }

        if (initialX == finalX) {
            if (particle.y >= initialY && particle.y <= finalY)
                return Math.abs(particle.x - initialX);
            else
                return particle.r;
        } else {
            if (particle.x >= initialX && particle.x <= finalX)
                return Math.abs(particle.y - initialY);
            else
                return particle.r;
        }
    }

    public double getOverlap(Particle particle){
        double overlap = particle.r - particleCenterToWall(particle);
        return overlap >= 0 ? overlap : 0;
    }

    public double getNormalRelVel(Particle particle) {
        return particle.vx * enx + particle.vy * eny;
    }


}
