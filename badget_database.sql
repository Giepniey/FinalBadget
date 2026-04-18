-- ============================================
-- BADGET DATABASE SETUP SCRIPT FOR MYSQL
-- ============================================
-- This script creates the database structure
-- for the Badget Budget & Expense Tracker application

-- Create Database
CREATE DATABASE IF NOT EXISTS badget_db;
USE badget_db;

-- ============================================
-- TABLE 1: TRANSACTIONS
-- ============================================
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(50) PRIMARY KEY,
    date DATE NOT NULL,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    type VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE 2: TRANSACTION HISTORY
-- ============================================
CREATE TABLE IF NOT EXISTS transaction_history (
    id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    change_type VARCHAR(50),
    previous_values TEXT,
    new_values TEXT,
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SAMPLE DATA (Optional)
-- ============================================
-- Insert sample transactions
INSERT INTO transactions (id, date, category, amount, description, type) VALUES
('TXN-1234567890', '2026-04-10', 'Food & Dining', 25.50, 'Lunch at cafe', 'Expense'),
('TXN-1234567891', '2026-04-08', 'Salary', 3000.00, 'Monthly salary', 'Income'),
('TXN-1234567892', '2026-04-09', 'Transportation', 50.00, 'Gas', 'Expense'),
('TXN-1234567893', '2026-04-07', 'Entertainment', 100.00, 'Movie and dinner', 'Expense'),
('TXN-1234567894', '2026-04-06', 'Shopping', 75.00, 'Groceries', 'Expense');

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify setup is complete:
-- SELECT COUNT(*) as total_transactions FROM transactions;
-- SELECT * FROM transactions;

-- ============================================
-- DATABASE SETUP COMPLETE!
-- ============================================
