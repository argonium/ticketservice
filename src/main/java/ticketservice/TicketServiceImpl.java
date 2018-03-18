package ticketservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implement the TicketService interface to provide methods to
 * get the number of available seats, hold seats, and reserve seats.
 *
 * @author Mike Wallace
 */
public class TicketServiceImpl implements TicketService {

    /** Default timeout value (in milliseconds) for a seat hold. */
    private static final long DEFAULT_HOLD_TIMEOUT = 2_000L;

    /** The current map of seat holds. */
    private final Map<SeatHold, SeatBlock> holds = new HashMap<>(20);

    /** The current map of reserved seats. */
    private final Map<Integer, SeatBlock> reserved = new HashMap<>(20);

    /** The maximum age for a seat hold before the seats become available again. */
    private final long holdTimeout;

    /** Store info on which seats are held. */
    private final List<SeatStatus> seats;

    /** The ID of the next entry in the holds map. */
    private int holdId = Integer.MIN_VALUE;

    /**
     * Public constructor.  Used to inject the necessary fields (venue, etc.).
     */
    public TicketServiceImpl() {
        this(DEFAULT_HOLD_TIMEOUT, new Venue());
    }

    /**
     * Constructor taking the timeout value (in milliseconds) for
     * a seat hold.
     *
     * @param holdTimeout the length of time a seat can be held (milliseconds)
     * @param venue the venue customers are buying tickets for
     */
    public TicketServiceImpl(final long holdTimeout, final Venue venue) {

        // Save the timeout value for seat holds
        this.holdTimeout = holdTimeout;

        // Initialize the list of seats, with a default status of OPEN
        final int numSeats = venue.getNumberOfSeats();
        seats = new ArrayList<>(numSeats);
        for (int i = 0; i < numSeats; ++i) {
            seats.add(new SeatStatus(i, SeatStatus.Status.OPEN));
        }
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    @Override
    public int numSeatsAvailable() {
        // Return the number of venue seats that are either open, or held but expired
        return (int) seats.stream().filter(s -> s.isAvailable(getSeatHoldBlock(s), holdTimeout)).count();
    }

    /**
     * Helper method to get the instance from the list of seat holdings, or null.
     *
     * @param seat the seat of interest
     * @return if the seat is held, return the info on the hold; else, return null
     */
    private SeatBlock getSeatHoldBlock(final SeatStatus seat) {
        return holds.get(new SeatHold(seat.getSeatLookupId()));
    }

    /**
     * Find and hold the best available seats for a customer.
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related information
     */
    @Override
    public SeatHold findAndHoldSeats(final int numSeats, final String customerEmail) {

        // Check the input
        if (customerEmail == null) {
            // An email address is required
            return null;
        } else if ((numSeats < 1) || (numSeats > numSeatsAvailable())) {
            // Either zero or negative seats requests, or user asked for more seats than are free
            return null;
        }

        SeatHold seatHold = null;
        synchronized (this) {

            // Find the first available seat, and check if it has enough open consecutive seats
            final int lastSeatIndex = seats.size() - numSeats;
            for (int seatId = 0; seatId <= lastSeatIndex; ++seatId) {

                // If this seat is available, then check the next (n - 1) seats
                // to see if we have a sufficiently large block of consecutive
                // seats for the hold
                final SeatStatus seat = seats.get(seatId);
                if (seat.isAvailable(getSeatHoldBlock(seat), holdTimeout)) {

                    // See if the next (n - 1) seats are also available
                    final int maxSeatId = seatId + numSeats;
                    int nextSeatId = seatId + 1;
                    boolean result = true;
                    for (; nextSeatId < maxSeatId; ++nextSeatId) {
                        final SeatStatus nextSeat = seats.get(nextSeatId);
                        if (!nextSeat.isAvailable(getSeatHoldBlock(nextSeat), holdTimeout)) {
                            result = false;
                            break;
                        }
                    }

                    // Check if we found enough consecutive available seats
                    if (result) {
                        // We found a block of open seats
                        final int id = holdId++;
                        seatHold = new SeatHold(id);
                        holds.put(seatHold, new SeatBlock(id, customerEmail, seatId, numSeats));

                        // Update the seats array
                        final int lastSeatId = seatId + numSeats - 1;
                        for (int currSeat = seatId; currSeat <= lastSeatId; ++currSeat) {
                            seats.get(currSeat).setSeatStatus(SeatStatus.Status.HELD);
                            seats.get(currSeat).setSeatLookupId(id);
                        }

                        // Break out of the loop since we held the seats
                        break;
                    } else {
                        // No match found, so update the seatId iterator variable
                        seatId = nextSeatId;
                    }
                }
            }
        }

        // If we found a set of consecutive seats to hold, return the info now; else return null
        return seatHold;
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(final int seatHoldId, final String customerEmail) {

        // Check the input
        if (customerEmail == null) {
            return null;
        }

        // Check if we have a seat-hold with the provided ID
        final SeatHold seatHold = new SeatHold(seatHoldId);
        final SeatBlock block = holds.get(seatHold);
        if (block == null) {
            // The seat-hold was not found
            return null;
        } else if (block.getAge() > holdTimeout) {

            // The seat hold has expired, so mark the seats as open
            synchronized(this) {
                updateSeatStatus(block.getStartingSeat(), block.getNumberSeats(), SeatStatus.Status.OPEN);
                holds.remove(seatHold);
            }

            return null;
        } else if (!customerEmail.equalsIgnoreCase(block.getEmail())) {
            // The customer email does not match
            return null;
        }

        // Mark the seats as reserved
        synchronized (this) {
            updateSeatStatus(block.getStartingSeat(), block.getNumberSeats(), SeatStatus.Status.RESERVED);
            reserved.put(block.getId(), new SeatBlock(block.getId(), block.getEmail(),
                       block.getStartingSeat(), block.getNumberSeats()));
            holds.remove(seatHold);
        }

        // Return the reservation ID (reuse the hold ID)
        return Integer.toString(block.getId());
    }

    /**
     * Helper method to mark a block of seats as having a specific status.
     *
     * @param startingSeat the starting seat index
     * @param numberSeats the number of consecutive seats
     * @param seatStatus the new seat status
     */
    private void updateSeatStatus(final int startingSeat, final int numberSeats, SeatStatus.Status seatStatus) {
        for (int index = startingSeat; index < (startingSeat + numberSeats); ++index) {
            seats.get(index).setSeatStatus(seatStatus);
        }
    }

    /**
     * Helper method to get the hold information by ID.
     *
     * @param hold the hold key
     * @return the corresponding value in the hold map, or null
     */
    public SeatBlock getHoldById(final SeatHold hold) {
        return holds.get(hold);
    }

    /**
     * Helper method to get the reserve information by ID.
     *
     * @param reservedId the reserve key
     * @return the corresponding value in the reserved map, or null
     */
    public SeatBlock getReservedById(final int reservedId) {
        return reserved.get(reservedId);
    }
}
