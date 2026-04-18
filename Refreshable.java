import java.util.List;

public interface Refreshable {
    /**
     * Called when the transaction list has been updated.
     */
    void refresh(List<Transaction> transactions);
}