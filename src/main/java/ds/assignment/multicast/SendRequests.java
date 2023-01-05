package ds.assignment.multicast;

import java.util.Random;

import ds.assignment.poissonjob.PoissonJob;

public class SendRequests implements PoissonJob {
    private Random rng;
    private MulticastService service;

    public SendRequests(MulticastService service) {
        this.service = service;
    }

    @Override
    public void execute() {
        service.generateRandomEvent();
    }

}
