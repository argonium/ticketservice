package ticketservice;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Test the behavior of the TicketService implementation.
 */
public class TicketServiceImplTest {

    /**
     * Test the logic for getting the number of available seats.
     */
    @Test
    public void testNumAvailableSeats() {

        // Create a service and confirm that we get the correct number of seats
        final TicketServiceImpl service = new TicketServiceImpl(100L, new Venue(20, 25));
        Assert.assertEquals(500, service.numSeatsAvailable());

        // Hold some seats
        final SeatHold hold = service.findAndHoldSeats(10, "a@example.com");
        Assert.assertNotNull(hold);
        Assert.assertEquals(490, service.numSeatsAvailable());

        // Sleep for more time than the timeout above.  This should cause the held seats to get freed.
        sleep(250L);
        Assert.assertEquals(500, service.numSeatsAvailable());
    }

    /**
     * Test the logic for finding and holding seats.
     */
    @Test
    public void testFindAndHoldSeats() {

        // Create a service and confirm that we can hold seats
        final TicketServiceImpl service = new TicketServiceImpl(300L, new Venue(20, 25));

        // Hold some seats and then confirm the data is as expected
        final SeatHold hold1 = service.findAndHoldSeats(20, "a@example.com");
        Assert.assertNotNull(hold1);
        final SeatBlock block1 = service.getHoldById(hold1);
        Assert.assertEquals(0, block1.getStartingSeat());
        Assert.assertEquals(20, block1.getNumberSeats());
        Assert.assertEquals(480, service.numSeatsAvailable());

        // Confirm we can't hold zero seats
        final SeatHold emptyHold = service.findAndHoldSeats(0, "a@example.com");
        Assert.assertNull(emptyHold);

        // Confirm that we can't hold with a null email
        final SeatHold noEmailHold = service.findAndHoldSeats(10, null);
        Assert.assertNull(noEmailHold);

        // Hold some more seats
        final SeatHold hold2 = service.findAndHoldSeats(30, "b@example.com");
        Assert.assertNotNull(hold2);
        final SeatBlock block2 = service.getHoldById(hold2);
        Assert.assertEquals(20, block2.getStartingSeat());
        Assert.assertEquals(30, block2.getNumberSeats());
        Assert.assertEquals(450, service.numSeatsAvailable());

        // Hold all of the remaining seats
        final SeatHold hold3 = service.findAndHoldSeats(450, "b@example.com");
        Assert.assertNotNull(hold3);
        final SeatBlock block3 = service.getHoldById(hold3);
        Assert.assertEquals(50, block3.getStartingSeat());
        Assert.assertEquals(450, block3.getNumberSeats());
        Assert.assertEquals(0, service.numSeatsAvailable());

        // Try to hold 1 seat - this should fail as no seats are available
        final SeatHold hold4 = service.findAndHoldSeats(1, "b@example.com");
        Assert.assertNull(hold4);

        // Sleep more than the timeout
        sleep(400L);

        // We exceeded the timeout, so should be able to reserve some seats now
        final SeatHold hold5 = service.findAndHoldSeats(5, "b@example.com");
        Assert.assertNotNull(hold5);
        final SeatBlock block5 = service.getHoldById(hold5);
        Assert.assertEquals(0, block5.getStartingSeat());
        Assert.assertEquals(5, block5.getNumberSeats());
        Assert.assertEquals(495, service.numSeatsAvailable());
    }

    /**
     * Test the logic for reserving seats.
     */
    @Test
    public void testReserveSeats() {

        // Create a service and confirm that we can hold seats
        final TicketServiceImpl service = new TicketServiceImpl(300L, new Venue(20, 25));

        // Hold some seats and then confirm the data is as expected
        final SeatHold hold1 = service.findAndHoldSeats(20, "a@example.com");
        Assert.assertNotNull(hold1);

        // Confirm that we can't reserve with a different email address
        final String reserve1 = service.reserveSeats(hold1.getId(), "b@example.com");
        Assert.assertNull(reserve1);

        // Confirm that we can't reserve with a different ID
        final String reserve2 = service.reserveSeats(hold1.getId() + 1, "a@example.com");
        Assert.assertNull(reserve2);

        // Confirm that we can reserve with the same info
        final String reserve3 = service.reserveSeats(hold1.getId(), "a@example.com");
        Assert.assertNotNull(reserve3);
    }

    /**
     * Test the logic for holding seats in a multithreaded test.
     */
    @Test
    public void multiThreadedTest() {

        // Create a service so we can test multithreading
        final TicketServiceImpl service = new TicketServiceImpl(1000L, new Venue(20, 50));

        // Launch threads to reserve seats at the same time.  The venue has 1000 seats,
        // so we launch 200 threads each trying to hold 5 seats concurrently.
        final int numThreads = 200;

        // Create the list of completable futures we want to later run
        @SuppressWarnings("unchecked")
        final CompletableFuture<SeatHold>[] futures = new CompletableFuture[numThreads];

        // Create each future to find and hold 5 seats, and add to the list for running below
        for (int i = 0; i < numThreads; ++i) {
            // Create our future and add to the array
            final CompletableFuture<SeatHold> future = CompletableFuture.supplyAsync(() ->
                    service.findAndHoldSeats(5, "sam@example.com"));
            futures[i] = future;
        }

        // Add the array of completable futures to the thread manager
        final CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);

        // Retrieve the output of each future
        final List<SeatHold> holdList = new ArrayList<>(numThreads);
        try {
            // Run all of the futures
            combinedFuture.get();

            // For each month, get the completed future and add to the output list
            for (int i = 0; i < numThreads; ++i) {
                holdList.add(futures[i].get());
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Exception in CompletableFuture test: " + e.getMessage());
        }

        // Confirm that no two holds have the same ID
        Assert.assertEquals(numThreads, holdList.size());
        final Set<Integer> holdIdSet = new HashSet<>(numThreads);
        for (SeatHold hold : holdList) {

            // First assert that the hold ID is not already in our set
            Assert.assertFalse(holdIdSet.contains(hold.getId()));

            // Now add the current hold ID to the set
            holdIdSet.add(hold.getId());
        }
    }

    /**
     * Helper method to sleep for the specified number of milliseconds.
     *
     * @param delay the number of milliseconds to sleep
     */
    private void sleep(final long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Nothing to do here
        }
    }
}
