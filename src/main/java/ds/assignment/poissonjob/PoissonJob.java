
package ds.assignment.poissonjob;

/**
 * Job executed by the {@code ds.assignment.PoissonJobScheduler} multiple times
 * with a poisson distribution
 */
public interface PoissonJob {
    /**
     * Method executed with a Poisson Distribution
     */
    public void execute();
}