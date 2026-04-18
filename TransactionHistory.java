import java.io.Serializable;
import java.time.LocalDateTime;

public class TransactionHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String transactionId;
    private String changeType;
    private String previousValues;
    private String newValues;
    private LocalDateTime timestamp;
    
    public TransactionHistory(String transactionId, String changeType, 
                            String previousValues, String newValues) {
        this.id = "HIST-" + System.currentTimeMillis();
        this.transactionId = transactionId;
        this.changeType = changeType;
        this.previousValues = previousValues;
        this.newValues = newValues;
        this.timestamp = LocalDateTime.now();
    }
    
    public TransactionHistory(String id, String transactionId, String changeType,
                            String previousValues, String newValues, LocalDateTime timestamp) {
        this.id = id;
        this.transactionId = transactionId;
        this.changeType = changeType;
        this.previousValues = previousValues;
        this.newValues = newValues;
        this.timestamp = timestamp;
    }
    
    public TransactionHistory(String id, String transactionId, String changeType,
                            String previousValues, String newValues, String timestamp) {
        this.id = id;
        this.transactionId = transactionId;
        this.changeType = changeType;
        this.previousValues = previousValues;
        this.newValues = newValues;
        this.timestamp = LocalDateTime.parse(timestamp);
    }
    
    public String getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getChangeType() { return changeType; }
    public String getPreviousValues() { return previousValues; }
    public String getNewValues() { return newValues; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    public String toCsv() {
        String prev = previousValues != null ? previousValues.replace(",", ";") : "";
        String next = newValues != null ? newValues.replace(",", ";") : "";
        return String.format("%s,%s,%s,%s,%s,%s",
                id, transactionId, changeType, prev, next, timestamp);
    }
    
    public static TransactionHistory fromCsv(String line) {
        try {
            String[] parts = line.split(",", 6);
            if (parts.length < 6) return null;
            
            return new TransactionHistory(parts[0], parts[1], parts[2],
                    parts[3].replace(";", ","), parts[4].replace(";", ","),
                    LocalDateTime.parse(parts[5]));
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s on Transaction %s at %s", 
                changeType, timestamp.toLocalDate(), transactionId, timestamp.toLocalTime());
    }
}
