-- ====================================================================================================
-- Expensora Database Schema Script
-- PostgreSQL Database Initialization Script
-- Generated: 2026-02-14
-- ====================================================================================================

-- Create database (run this separately if database doesn't exist)
-- CREATE DATABASE expensora;
-- \c expensora;

-- ====================================================================================================
-- Drop existing tables (in correct order to handle foreign key constraints)
-- ====================================================================================================
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS recurring_transaction CASCADE;
DROP TABLE IF EXISTS budget CASCADE;
DROP TABLE IF EXISTS income CASCADE;
DROP TABLE IF EXISTS expense CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ====================================================================================================
-- Create USERS table
-- ====================================================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    refresh_token TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for email lookups
CREATE INDEX idx_users_email ON users(email);

-- ====================================================================================================
-- Create CATEGORY table
-- ====================================================================================================
CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    color VARCHAR(7), -- Hex color code (e.g., #FF5733)
    icon VARCHAR(100), -- Icon name or emoji
    is_default BOOLEAN DEFAULT FALSE,
    user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for user-specific category lookups
CREATE INDEX idx_category_user_id ON category(user_id);
CREATE INDEX idx_category_type ON category(type);

-- ====================================================================================================
-- Create EXPENSE table
-- ====================================================================================================
CREATE TABLE expense (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    description VARCHAR(500),
    category_id UUID,
    user_id UUID NOT NULL,
    expense_date DATE NOT NULL,
    notes TEXT,
    tags VARCHAR(500), -- Comma-separated tags
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_expense_user_id ON expense(user_id);
CREATE INDEX idx_expense_category_id ON expense(category_id);
CREATE INDEX idx_expense_date ON expense(expense_date);
CREATE INDEX idx_expense_user_date ON expense(user_id, expense_date);

-- ====================================================================================================
-- Create INCOME table
-- ====================================================================================================
CREATE TABLE income (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    description VARCHAR(500),
    category_id UUID,
    user_id UUID NOT NULL,
    income_date DATE NOT NULL,
    notes TEXT,
    tags VARCHAR(500), -- Comma-separated tags
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_income_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_income_user_id ON income(user_id);
CREATE INDEX idx_income_category_id ON income(category_id);
CREATE INDEX idx_income_date ON income(income_date);
CREATE INDEX idx_income_user_date ON income(user_id, income_date);

-- ====================================================================================================
-- Create BUDGET table
-- ====================================================================================================
CREATE TABLE budget (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    month INTEGER NOT NULL CHECK (month >= 1 AND month <= 12),
    year INTEGER NOT NULL CHECK (year >= 2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT unique_budget_user_category_month_year UNIQUE (user_id, category_id, month, year)
);

-- Indexes for efficient queries
CREATE INDEX idx_budget_user_id ON budget(user_id);
CREATE INDEX idx_budget_category_id ON budget(category_id);
CREATE INDEX idx_budget_month_year ON budget(month, year);

-- ====================================================================================================
-- Create RECURRING_TRANSACTION table
-- ====================================================================================================
CREATE TABLE recurring_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    description VARCHAR(500),
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    frequency VARCHAR(50) NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    start_date DATE NOT NULL,
    end_date DATE,
    last_execution_date DATE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT check_end_date_after_start CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Indexes for efficient queries
CREATE INDEX idx_recurring_user_id ON recurring_transaction(user_id);
CREATE INDEX idx_recurring_category_id ON recurring_transaction(category_id);
CREATE INDEX idx_recurring_active ON recurring_transaction(active);
CREATE INDEX idx_recurring_next_execution ON recurring_transaction(last_execution_date, active);

-- ====================================================================================================
-- Create NOTIFICATION table
-- ====================================================================================================
CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_read ON notification(read);
CREATE INDEX idx_notification_user_read ON notification(user_id, read);

-- ====================================================================================================
-- Insert Default Categories (System Categories)
-- ====================================================================================================

-- Default EXPENSE Categories
INSERT INTO category (id, name, type, color, icon, is_default, user_id) VALUES
(gen_random_uuid(), 'Food & Dining', 'EXPENSE', '#FF5733', '🍔', TRUE, NULL),
(gen_random_uuid(), 'Transportation', 'EXPENSE', '#3498DB', '🚗', TRUE, NULL),
(gen_random_uuid(), 'Shopping', 'EXPENSE', '#E74C3C', '🛍️', TRUE, NULL),
(gen_random_uuid(), 'Entertainment', 'EXPENSE', '#9B59B6', '🎬', TRUE, NULL),
(gen_random_uuid(), 'Bills & Utilities', 'EXPENSE', '#F39C12', '💡', TRUE, NULL),
(gen_random_uuid(), 'Healthcare', 'EXPENSE', '#16A085', '🏥', TRUE, NULL),
(gen_random_uuid(), 'Education', 'EXPENSE', '#2ECC71', '📚', TRUE, NULL),
(gen_random_uuid(), 'Housing', 'EXPENSE', '#34495E', '🏠', TRUE, NULL),
(gen_random_uuid(), 'Insurance', 'EXPENSE', '#95A5A6', '🛡️', TRUE, NULL),
(gen_random_uuid(), 'Personal Care', 'EXPENSE', '#E91E63', '💅', TRUE, NULL),
(gen_random_uuid(), 'Travel', 'EXPENSE', '#00BCD4', '✈️', TRUE, NULL),
(gen_random_uuid(), 'Gifts & Donations', 'EXPENSE', '#FF9800', '🎁', TRUE, NULL),
(gen_random_uuid(), 'Other Expenses', 'EXPENSE', '#607D8B', '📝', TRUE, NULL);

-- Default INCOME Categories
INSERT INTO category (id, name, type, color, icon, is_default, user_id) VALUES
(gen_random_uuid(), 'Salary', 'INCOME', '#4CAF50', '💰', TRUE, NULL),
(gen_random_uuid(), 'Freelance', 'INCOME', '#8BC34A', '💼', TRUE, NULL),
(gen_random_uuid(), 'Investment', 'INCOME', '#CDDC39', '📈', TRUE, NULL),
(gen_random_uuid(), 'Business', 'INCOME', '#FFC107', '🏢', TRUE, NULL),
(gen_random_uuid(), 'Rental Income', 'INCOME', '#FF5722', '🏘️', TRUE, NULL),
(gen_random_uuid(), 'Interest', 'INCOME', '#795548', '🏦', TRUE, NULL),
(gen_random_uuid(), 'Gifts', 'INCOME', '#9C27B0', '🎁', TRUE, NULL),
(gen_random_uuid(), 'Refunds', 'INCOME', '#3F51B5', '↩️', TRUE, NULL),
(gen_random_uuid(), 'Other Income', 'INCOME', '#009688', '📝', TRUE, NULL);

-- ====================================================================================================
-- Create Functions and Triggers for automatic updated_at timestamp
-- ====================================================================================================

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for all tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_category_updated_at BEFORE UPDATE ON category
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_updated_at BEFORE UPDATE ON expense
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_income_updated_at BEFORE UPDATE ON income
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_budget_updated_at BEFORE UPDATE ON budget
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_recurring_transaction_updated_at BEFORE UPDATE ON recurring_transaction
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_updated_at BEFORE UPDATE ON notification
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ====================================================================================================
-- Sample Data (Optional - Remove if not needed)
-- ====================================================================================================

-- Create a test user (password is 'password123' - you should hash this in production)
-- INSERT INTO users (name, email, password, role) 
-- VALUES ('Test User', 'test@expensora.com', '$2a$10$XPT6SYLhXGzR0vNnL7y7EO5K7Y0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0Z0', 'USER');

-- ====================================================================================================
-- Verification Queries
-- ====================================================================================================
-- You can run these queries to verify the schema was created successfully

-- List all tables
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;

-- Count records in each table
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'category', COUNT(*) FROM category
UNION ALL SELECT 'expense', COUNT(*) FROM expense
UNION ALL SELECT 'income', COUNT(*) FROM income
UNION ALL SELECT 'budget', COUNT(*) FROM budget
UNION ALL SELECT 'recurring_transaction', COUNT(*) FROM recurring_transaction
UNION ALL SELECT 'notification', COUNT(*) FROM notification;

-- ====================================================================================================
-- End of Schema Script
-- ====================================================================================================
