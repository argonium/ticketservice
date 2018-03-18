package ticketservice;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the behavior of the SeatStatus class.
 */
public class SeatStatusTest {

    /**
     * Test the code to check if a seat is available.
     */
    @Test
    public void testIsAvailable() {

        // Create an open seat.  It should be available.
        final SeatStatus status = new SeatStatus(1, SeatStatus.Status.OPEN);
        final SeatBlock block = new SeatBlock(1, null, 1, 1);
        Assert.assertTrue(status.isAvailable(block, 500L));

        // Mark the seat as held.  The max age is in the past, so the seat should be available.
        status.setSeatStatus(SeatStatus.Status.HELD);
        Assert.assertTrue(status.isAvailable(block, -10L));

        // Confirm that reserved seats are not available, regardless of the max age.
        status.setSeatStatus(SeatStatus.Status.RESERVED);
        Assert.assertFalse(status.isAvailable(block, 500L));

        // Mark the seat as held and sleep.
        status.setSeatStatus(SeatStatus.Status.HELD);
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            // Nothing to do here
        }

        // Confirm that the seat is available with a low max-age (less than the sleep time)
        Assert.assertTrue(status.isAvailable(block, 50L));

        // Confirm the seat is not available with a max-age greater than the sleep time
        Assert.assertFalse(status.isAvailable(block, 1000L));
    }

    /**
     * Test the code to change a seat status.
     */
    @Test
    public void testStatusChange() {

        // Create the initial seat status instance.  It should have no lookup ID.
        final SeatStatus status = new SeatStatus(1, SeatStatus.Status.OPEN);
        Assert.assertEquals(status.getSeatLookupId(), -1);

        // Change the status to Held.  Test the lookup ID.
        status.setSeatStatus(SeatStatus.Status.HELD);
        status.setSeatLookupId(10);
        Assert.assertEquals(status.getSeatLookupId(), 10);

        // Change the status back to Open.  This should clear the lookup ID.
        status.setSeatStatus(SeatStatus.Status.OPEN);
        Assert.assertEquals(status.getSeatLookupId(), -1);
    }
}
