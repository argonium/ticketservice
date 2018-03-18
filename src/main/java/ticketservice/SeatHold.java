package ticketservice;

/**
 * A simple POJO used as the key for the map of held and reserved seat blocks.
 */
public final class SeatHold {

    /** The integer ID. */
    private int id;

    /**
     * Constructor taking the ID.
     *
     * @param id the ID of the instance
     */
    public SeatHold(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * We use this class as a map key, so override equals() and
     * hashCode() to use ID as the hash value.
     *
     * @param o the object we're comparing to
     * @return whether the two instances are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return (id == ((SeatHold) o).id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SeatHold{" +
                "id=" + id +
                '}';
    }
}
