package ticketservice;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the behavior of the SeatBlock class.
 */
public class SeatBlockTest {

    /**
     * Test the behavior of the age of a seat block.
     */
    @Test
    public void testAge() {

        // Create a seat block so we can test its age after sleeping
        final SeatBlock block = new SeatBlock(1, "a@b.com", 1, 10);
        try {
            final long sleepTime = 300L;
            Thread.sleep(sleepTime);

            // Confirm the age is in a reasonable range
            final long age = block.getAge();
            Assert.assertTrue((age >= sleepTime) && (age < (sleepTime + 200L)));
        } catch (InterruptedException ie) {
            // Nothing to do here
        }
    }
}
