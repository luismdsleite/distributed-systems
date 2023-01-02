
package ds.assignment.poissonjob;

/**
 * Job executed by the {@link ds.assignment.poissonjob.PoissonJobScheduler} multiple times
 * with a poisson distribution
 */
public interface PoissonJob {
    /**
     * Method executed with a Poisson Distribution
     */
    public void execute();
}