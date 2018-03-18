package ticketservice;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the behavior of the Venue class.
 */
public class VenueTest {

    @Test
    public void testVenueConstructor() {

        // Confirm we can create venues with a positive number of rows and columns,
        // and we cannot creates venues with negative numbers or zero.
        Assert.assertTrue(canCreateVenue(3, 5));
        Assert.assertFalse(canCreateVenue(3, 0));
        Assert.assertFalse(canCreateVenue(-8, 6));
        Assert.assertFalse(canCreateVenue(-5, -5));

        // Confirm the number of computed seats is correct.
        final Venue venue = new Venue(3, 6);
        Assert.assertEquals(venue.getNumberOfSeats(), 18);
    }

    /**
     * Helper method to return whether creating the venue was successful
     * or threw an exception.
     *
     * @param numRows the number of rows
     * @param numCols the number of seats per row
     * @return whether the venue was successfully created
     */
    private boolean canCreateVenue(final int numRows, final int numCols) {

        boolean result;
        try {
            new Venue(numRows, numCols);
            result = true;
        } catch (RuntimeException re) {
            result = false;
        }

        return result;
    }
}
