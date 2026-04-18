import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private LocalDate date;
    private String category;
    private double amount;
    private String description;
    private String type;
    
    public Transaction(String id, LocalDate date, String category, double amount, 
                       String description, String type) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.type = type;
    }
    
    public Transaction(LocalDate date, String category, double amount, 
                       String description, String type) {
        this.id = "TXN-" + System.currentTimeMillis();
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.type = type;
    }
    
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    
    public void setDate(LocalDate date) { this.date = date; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    
    public String toCsv() {
        String escaped = "\"" + description.replace("\"", "\"\"") + "\"";
        return String.format("%s,%s,%s,%.2f,%s,%s", id, date, category, amount, escaped, type);
    }
    
    public static Transaction fromCsv(String line) {
        try {
            String[] parts = parseCsvLine(line);
            if (parts.length < 6) return null;
            LocalDate date = LocalDate.parse(parts[1].trim());
            String desc = parts[4].trim();
            if (desc.startsWith("\"") && desc.endsWith("\"")) {
                desc = desc.substring(1, desc.length() - 1);
                desc = desc.replace("\"\"", "\"");
            }
            return new Transaction(parts[0].trim(), date, parts[2].trim(), 
                    Double.parseDouble(parts[3].trim()), desc, parts[5].trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String[] parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
    
    @Override
    public String toString() {
        return String.format("%s | %s | %s | ₱%.2f | %s", date, type, category, amount, description);
    }
}
