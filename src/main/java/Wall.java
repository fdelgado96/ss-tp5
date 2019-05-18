public class Wall {

    public double initialX, initialY, finalX, finalY, enx, eny, m, c;

    public Wall(double initialX, double initialY, double finalX, double finalY) {
        this.initialX = initialX;
        this.initialY = initialY;
        this.finalX = finalX;
        this.finalY = finalY;
        this.m = finalY - initialY / finalX - initialX;
        this.c = initialY - m*initialX;

        double magnitude = Math.sqrt(Math.pow(finalX - initialX,  2) + Math.pow(finalY - initialY, 2));
        this.enx = -(finalY - initialY)/magnitude;
        this.eny = (finalX - initialX)/magnitude;
    }

    private double centerToWall(Particle particle) {
        if(
                Math.sqrt(Math.pow(particle.x - initialX,  2) + Math.pow(particle.y - initialY, 2)) > particle.r ||
                Math.sqrt(Math.pow(particle.x - finalX,  2) + Math.pow(particle.y - finalY, 2)) > particle.r
        ){
            return particle.r+1;
        }

        if(initialX == finalX){
            return particle.x -initialX;
        }

        if(initialY == finalY) {
            return particle.y -initialY;
        }

        return Math.sqrt(Math.pow(particle.x - ((particle.y - c) / m),  2) + Math.pow(particle.y - (m*particle.x + c), 2));
    }

    public double getOverlap(Particle particle){
        double overlap = particle.r - centerToWall(particle);
        return overlap >= 0 ? overlap : 0;
    }

    //TODO: Arreglar projección sobre versores normales mal hecha
    public double getNormalRelVel(Particle particle) {
        return particle.vx /enx + particle.vy /eny;
    }


}
