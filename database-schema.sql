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
DROP TABLE IF EXISTS debt_payment CASCADE;
DROP TABLE IF EXISTS debt CASCADE;
DROP TABLE IF EXISTS user_household CASCADE;
DROP TABLE IF EXISTS household CASCADE;
DROP TABLE IF EXISTS import_history CASCADE;
DROP TABLE IF EXISTS goal_contribution CASCADE;
DROP TABLE IF EXISTS goal CASCADE;
DROP TABLE IF EXISTS transfer CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS recurring_transaction CASCADE;
DROP TABLE IF EXISTS budget CASCADE;
DROP TABLE IF EXISTS income CASCADE;
DROP TABLE IF EXISTS expense CASCADE;
DROP TABLE IF EXISTS account CASCADE;
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
    parent_id UUID, -- For subcategories
    user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE CASCADE
);

-- Index for user-specific category lookups
CREATE INDEX idx_category_user_id ON category(user_id);
CREATE INDEX idx_category_type ON category(type);
CREATE INDEX idx_category_parent_id ON category(parent_id);

-- ====================================================================================================
-- Create ACCOUNT table
-- ====================================================================================================
CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('BANK_CHECKING', 'BANK_SAVINGS', 'CREDIT_CARD', 'CASH', 'DIGITAL_WALLET', 'INVESTMENT')),
    initial_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_account_user_id ON account(user_id);
CREATE INDEX idx_account_active ON account(active);

-- ====================================================================================================
-- Create EXPENSE table
-- ====================================================================================================
CREATE TABLE expense (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    description VARCHAR(500),
    category_id UUID,
    user_id UUID NOT NULL,
    account_id UUID,
    expense_date DATE NOT NULL,
    notes TEXT,
    tags VARCHAR(500), -- Comma-separated tags
    is_tax_deductible BOOLEAN DEFAULT FALSE,
    tax_category VARCHAR(100), -- Business, Medical, Charity, etc.
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE SET NULL
);

-- Indexes for efficient queries
CREATE INDEX idx_expense_user_id ON expense(user_id);
CREATE INDEX idx_expense_category_id ON expense(category_id);
CREATE INDEX idx_expense_date ON expense(expense_date);
CREATE INDEX idx_expense_user_date ON expense(user_id, expense_date);
CREATE INDEX idx_expense_tax_deductible ON expense(is_tax_deductible);

-- ====================================================================================================
-- Create INCOME table
-- ====================================================================================================
CREATE TABLE income (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    description VARCHAR(500),
    category_id UUID,
    user_id UUID NOT NULL,
    account_id UUID,
    income_date DATE NOT NULL,
    notes TEXT,
    tags VARCHAR(500), -- Comma-separated tags
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_income_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_income_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE SET NULL
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
    budget_month INTEGER NOT NULL CHECK (budget_month >= 1 AND budget_month <= 12),
    budget_year INTEGER NOT NULL CHECK (budget_year >= 2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT unique_budget_user_category_month_year UNIQUE (user_id, category_id, budget_month, budget_year)
);

-- Indexes for efficient queries
CREATE INDEX idx_budget_user_id ON budget(user_id);
CREATE INDEX idx_budget_category_id ON budget(category_id);
CREATE INDEX idx_budget_month_year ON budget(budget_month, budget_year);

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
-- Create TRANSFER table
-- ====================================================================================================
CREATE TABLE transfer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    description VARCHAR(500),
    transfer_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_from_account FOREIGN KEY (from_account_id) REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_to_account FOREIGN KEY (to_account_id) REFERENCES account(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_transfer_from_account ON transfer(from_account_id);
CREATE INDEX idx_transfer_to_account ON transfer(to_account_id);
CREATE INDEX idx_transfer_date ON transfer(transfer_date);

-- ====================================================================================================
-- Create GOAL table
-- ====================================================================================================
CREATE TABLE goal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    goal_type VARCHAR(50) NOT NULL CHECK (goal_type IN ('SAVINGS', 'DEBT_PAYOFF', 'INVESTMENT', 'PURCHASE')),
    target_amount NUMERIC(19, 2) NOT NULL CHECK (target_amount >= 0),
    current_amount NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (current_amount >= 0),
    target_date DATE NOT NULL,
    icon VARCHAR(100),
    color VARCHAR(7),
    priority INTEGER CHECK (priority >= 1 AND priority <= 5),
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_goal_user_id ON goal(user_id);
CREATE INDEX idx_goal_completed ON goal(completed);
CREATE INDEX idx_goal_priority ON goal(priority);

-- ====================================================================================================
-- Create GOAL_CONTRIBUTION table
-- ====================================================================================================
CREATE TABLE goal_contribution (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    notes TEXT,
    contribution_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contribution_goal FOREIGN KEY (goal_id) REFERENCES goal(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_contribution_goal_id ON goal_contribution(goal_id);
CREATE INDEX idx_contribution_date ON goal_contribution(contribution_date);

-- ====================================================================================================
-- Create NOTIFICATION table
-- ====================================================================================================
CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) CHECK (type IN ('INFO', 'WARNING', 'SUCCESS', 'ERROR')),
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_read ON notification(read);

-- ====================================================================================================
-- Create DEBT table (Feature 7: Debt & Loan Tracking)
-- ====================================================================================================
CREATE TABLE debt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    debt_type VARCHAR(50) NOT NULL CHECK (debt_type IN ('CREDIT_CARD', 'PERSONAL_LOAN', 'AUTO_LOAN', 'MORTGAGE', 'STUDENT_LOAN', 'OTHER')),
    principal_amount NUMERIC(19, 2) NOT NULL CHECK (principal_amount >= 0),
    current_balance NUMERIC(19, 2) NOT NULL CHECK (current_balance >= 0),
    interest_rate NUMERIC(5, 2), -- Annual percentage (e.g., 5.75 for 5.75%)
    minimum_payment NUMERIC(19, 2),
    start_date DATE NOT NULL,
    target_payoff_date DATE,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    user_id UUID NOT NULL,
    account_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_debt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_debt_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE SET NULL
);

-- Indexes for efficient queries
CREATE INDEX idx_debt_user_id ON debt(user_id);
CREATE INDEX idx_debt_is_active ON debt(is_active);
CREATE INDEX idx_debt_user_active ON debt(user_id, is_active);

-- ====================================================================================================
-- Create DEBT_PAYMENT table (Feature 7: Debt & Loan Tracking)
-- ====================================================================================================
CREATE TABLE debt_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    debt_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    principal_paid NUMERIC(19, 2) NOT NULL CHECK (principal_paid >= 0),
    interest_paid NUMERIC(19, 2) NOT NULL CHECK (interest_paid >= 0),
    payment_date DATE NOT NULL,
    notes TEXT,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_debt_payment_debt FOREIGN KEY (debt_id) REFERENCES debt(id) ON DELETE CASCADE,
    CONSTRAINT fk_debt_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_debt_payment_debt_id ON debt_payment(debt_id);
CREATE INDEX idx_debt_payment_user_id ON debt_payment(user_id);
CREATE INDEX idx_debt_payment_date ON debt_payment(payment_date);

-- ====================================================================================================
-- Create HOUSEHOLD table (Feature 8: Shared Finances/Family Accounts)
-- ====================================================================================================
CREATE TABLE household (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_household_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_household_owner_id ON household(owner_id);
CREATE INDEX idx_household_is_active ON household(is_active);

-- ====================================================================================================
-- Create USER_HOUSEHOLD table (Feature 8: Shared Finances/Family Accounts)
-- ====================================================================================================
CREATE TABLE user_household (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    household_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'EDITOR', 'VIEWER')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_household_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_household_household FOREIGN KEY (household_id) REFERENCES household(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_household UNIQUE (user_id, household_id)
);

-- Indexes for efficient queries
CREATE INDEX idx_user_household_user_id ON user_household(user_id);
CREATE INDEX idx_user_household_household_id ON user_household(household_id);
CREATE INDEX idx_user_household_active ON user_household(is_active);

-- ====================================================================================================
-- Create IMPORT_HISTORY table (Feature 6: Data Import/Export)
-- ====================================================================================================
CREATE TABLE import_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(500) NOT NULL,
    format VARCHAR(50) NOT NULL CHECK (format IN ('BANK_CSV', 'MINT_CSV', 'YNAB4', 'CUSTOM_CSV')),
    total_records INTEGER DEFAULT 0,
    successful_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    duplicate_records INTEGER DEFAULT 0,
    error_log TEXT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_import_history_user_id ON import_history(user_id);
CREATE INDEX idx_import_history_status ON import_history(status);
CREATE INDEX idx_import_history_created_at ON import_history(created_at);

-- ====================================================================================================
-- Insert Default Categories (System Categories)
-- ====================================================================================================

-- Default EXPENSE Categories
INSERT INTO category (id, name, type, color, icon, is_default, user_id) VALUES
(gen_random_uuid(), 'Food & Dining', 'EXPENSE', '#FF5733', '🍔', TRUE, NULL),
(gen_random_uuid(), 'Transportation', 'EXPENSE', '#3498DB', '🚗', TRUE, NULL),
(gen_random_uuid(), 'Shopping', 'EXPENSE', '#9B59B6', '🛍️', TRUE, NULL),
(gen_random_uuid(), 'Entertainment', 'EXPENSE', '#E74C3C', '🎬', TRUE, NULL),
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

CREATE TRIGGER update_account_updated_at BEFORE UPDATE ON account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_updated_at BEFORE UPDATE ON expense
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_income_updated_at BEFORE UPDATE ON income
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_budget_updated_at BEFORE UPDATE ON budget
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_recurring_transaction_updated_at BEFORE UPDATE ON recurring_transaction
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transfer_updated_at BEFORE UPDATE ON transfer
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_goal_updated_at BEFORE UPDATE ON goal
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_goal_contribution_updated_at BEFORE UPDATE ON goal_contribution
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_updated_at BEFORE UPDATE ON notification
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_debt_updated_at BEFORE UPDATE ON debt
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_debt_payment_updated_at BEFORE UPDATE ON debt_payment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_household_updated_at BEFORE UPDATE ON household
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_household_updated_at BEFORE UPDATE ON user_household
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_import_history_updated_at BEFORE UPDATE ON import_history
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
UNION ALL SELECT 'account', COUNT(*) FROM account
UNION ALL SELECT 'expense', COUNT(*) FROM expense
UNION ALL SELECT 'income', COUNT(*) FROM income
UNION ALL SELECT 'budget', COUNT(*) FROM budget
UNION ALL SELECT 'recurring_transaction', COUNT(*) FROM recurring_transaction
UNION ALL SELECT 'transfer', COUNT(*) FROM transfer
UNION ALL SELECT 'goal', COUNT(*) FROM goal
UNION ALL SELECT 'goal_contribution', COUNT(*) FROM goal_contribution
UNION ALL SELECT 'notification', COUNT(*) FROM notification
UNION ALL SELECT 'debt', COUNT(*) FROM debt
UNION ALL SELECT 'debt_payment', COUNT(*) FROM debt_payment
UNION ALL SELECT 'household', COUNT(*) FROM household
UNION ALL SELECT 'user_household', COUNT(*) FROM user_household
UNION ALL SELECT 'import_history', COUNT(*) FROM import_history;

-- ====================================================================================================
-- End of Schema Script
-- ====================================================================================================
