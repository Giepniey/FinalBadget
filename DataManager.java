import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.sql.*;

public class DataManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/badget_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    public void saveTransactions(List<Transaction> transactions) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Clear old transactions
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM transactions");
                }
                
                // Insert all transactions
                String sql = "INSERT INTO transactions (id, date, category, amount, description, type) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (Transaction t : transactions) {
                        pstmt.setString(1, t.getId());
                        pstmt.setString(2, t.getDate().toString());
                        pstmt.setString(3, t.getCategory());
                        pstmt.setDouble(4, t.getAmount());
                        pstmt.setString(5, t.getDescription());
                        pstmt.setString(6, t.getType());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                
                conn.commit();
                System.out.println("✓ Transactions saved to database: " + transactions.size() + " records");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Database error saving transactions: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM transactions";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    String category = rs.getString("category");
                    double amount = rs.getDouble("amount");
                    String description = rs.getString("description");
                    String type = rs.getString("type");
                    transactions.add(new Transaction(id, date, category, amount, description, type));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error loading transactions: " + e.getMessage());
            System.err.println("Using empty transaction list. Make sure MySQL is running and database is set up.");
            e.printStackTrace();
        }
        return transactions;
    }
    
    public void saveTransactionHistory(TransactionHistory history) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO transaction_history (id, transaction_id, change_type, previous_values, new_values, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, history.getId());
                pstmt.setString(2, history.getTransactionId());
                pstmt.setString(3, history.getChangeType());
                pstmt.setString(4, history.getPreviousValues());
                pstmt.setString(5, history.getNewValues());
                pstmt.setString(6, history.getTimestamp().toString());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Database error saving transaction history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<TransactionHistory> loadTransactionHistory(String transactionId) {
        List<TransactionHistory> history = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM transaction_history WHERE transaction_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, transactionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String txnId = rs.getString("transaction_id");
                        String changeType = rs.getString("change_type");
                        String prevValues = rs.getString("previous_values");
                        String newValues = rs.getString("new_values");
                        String timestamp = rs.getString("timestamp");
                        history.add(new TransactionHistory(id, txnId, changeType, prevValues, newValues, timestamp));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error loading transaction history: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }
}
