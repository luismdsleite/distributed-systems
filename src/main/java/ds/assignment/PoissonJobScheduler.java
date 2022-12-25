package ds.assignment;

import java.util.Random;

import poisson.PoissonProcess;

public class PoissonJobScheduler {
    private double lambda;
    private PoissonJob job;
    private Random rng;
    
    public PoissonJobScheduler(double lambda, Random rng, PoissonJob job) {
        this.lambda = lambda;
        this.job = job;
        this.rng = rng;
    }

    public double getLambda() {
        return lambda;
    }


    public Thread schedulerThread() {
        return new Thread(
                new Runnable() {
                    public void run() {
                        PoissonProcess pp = new PoissonProcess(lambda, rng);
                        while (true)
                            try {
                                Thread.sleep((long) pp.timeForNextEvent() * 1000);
                                job.execute();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                    }
                });
    }

}
