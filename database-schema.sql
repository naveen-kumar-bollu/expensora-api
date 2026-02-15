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
DROP TABLE IF EXISTS user_challenge CASCADE;
DROP TABLE IF EXISTS challenge CASCADE;
DROP TABLE IF EXISTS user_achievement CASCADE;
DROP TABLE IF EXISTS achievement CASCADE;
DROP TABLE IF EXISTS transaction_split CASCADE;
DROP TABLE IF EXISTS interpersonal_debt CASCADE;
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
    icon VARCHAR(100), -- Icon name or emoji
    color VARCHAR(7), -- Hex color code
    is_default BOOLEAN DEFAULT FALSE, -- Default account for transactions
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
-- Create TRANSACTION_SPLIT table (Feature 19: Split Transactions)
-- ====================================================================================================
CREATE TABLE transaction_split (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_id UUID,
    income_id UUID,
    category_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    percentage NUMERIC(5, 2),
    description VARCHAR(500),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_split_expense FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE,
    CONSTRAINT fk_split_income FOREIGN KEY (income_id) REFERENCES income(id) ON DELETE CASCADE,
    CONSTRAINT fk_split_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT fk_split_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT check_transaction_type CHECK ((expense_id IS NOT NULL AND income_id IS NULL) OR (expense_id IS NULL AND income_id IS NOT NULL))
);

-- Indexes for efficient queries
CREATE INDEX idx_split_expense_id ON transaction_split(expense_id);
CREATE INDEX idx_split_income_id ON transaction_split(income_id);
CREATE INDEX idx_split_user_id ON transaction_split(user_id);
CREATE INDEX idx_split_category_id ON transaction_split(category_id);

-- ====================================================================================================
-- Create INTERPERSONAL_DEBT table (Feature 19: Split Transactions - Interpersonal debts)
-- ====================================================================================================
CREATE TABLE interpersonal_debt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creditor_user_id UUID NOT NULL, -- User who is owed money
    debtor_user_id UUID NOT NULL, -- User who owes money
    amount NUMERIC(19, 2) NOT NULL CHECK (amount >= 0),
    original_amount NUMERIC(19, 2) NOT NULL CHECK (original_amount > 0),
    description VARCHAR(500),
    expense_id UUID, -- Link to the original split expense
    is_settled BOOLEAN DEFAULT FALSE,
    settled_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interpersonal_debt_creditor FOREIGN KEY (creditor_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_interpersonal_debt_debtor FOREIGN KEY (debtor_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_interpersonal_debt_expense FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE SET NULL,
    CONSTRAINT check_different_users CHECK (creditor_user_id != debtor_user_id)
);

-- Indexes for efficient queries
CREATE INDEX idx_interpersonal_debt_creditor ON interpersonal_debt(creditor_user_id);
CREATE INDEX idx_interpersonal_debt_debtor ON interpersonal_debt(debtor_user_id);
CREATE INDEX idx_interpersonal_debt_settled ON interpersonal_debt(is_settled);
CREATE INDEX idx_interpersonal_debt_expense ON interpersonal_debt(expense_id);

-- ====================================================================================================
-- Create ACHIEVEMENT table (Feature 21: Gamification)
-- ====================================================================================================
CREATE TABLE achievement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    icon VARCHAR(100),
    badge_color VARCHAR(7),
    points INTEGER NOT NULL DEFAULT 0,
    achievement_type VARCHAR(50) NOT NULL CHECK (achievement_type IN ('STREAK', 'BUDGET', 'SAVINGS', 'DEBT', 'GOAL', 'USAGE', 'MILESTONE')),
    criteria_type VARCHAR(50) NOT NULL CHECK (criteria_type IN ('CONSECUTIVE_DAYS', 'BUDGET_ADHERENCE', 'SAVINGS_RATE', 'DEBT_PAYOFF', 'GOAL_COMPLETION', 'TRANSACTION_COUNT', 'AMOUNT_SAVED')),
    criteria_value NUMERIC(19, 2), -- The threshold value for criteria
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient queries
CREATE INDEX idx_achievement_type ON achievement(achievement_type);
CREATE INDEX idx_achievement_active ON achievement(is_active);

-- ====================================================================================================
-- Create USER_ACHIEVEMENT table (Feature 21: Gamification)
-- ====================================================================================================
CREATE TABLE user_achievement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    achievement_id UUID NOT NULL,
    earned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    progress NUMERIC(5, 2) DEFAULT 100.00, -- Percentage of achievement completion
    is_notified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_achievement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievement_achievement FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_achievement UNIQUE (user_id, achievement_id)
);

-- Indexes for efficient queries
CREATE INDEX idx_user_achievement_user_id ON user_achievement(user_id);
CREATE INDEX idx_user_achievement_achievement_id ON user_achievement(achievement_id);
CREATE INDEX idx_user_achievement_earned_date ON user_achievement(earned_date);

-- ====================================================================================================
-- Create CHALLENGE table (Feature 21: Gamification)
-- ====================================================================================================
CREATE TABLE challenge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    challenge_type VARCHAR(50) NOT NULL CHECK (challenge_type IN ('SAVINGS', 'BUDGET', 'NO_SPEND', 'INCOME', 'GOAL', 'CUSTOM')),
    target_amount NUMERIC(19, 2),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    points_reward INTEGER NOT NULL DEFAULT 0,
    icon VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    is_global BOOLEAN DEFAULT TRUE, -- Whether all users can join
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_challenge_dates CHECK (end_date >= start_date)
);

-- Indexes for efficient queries
CREATE INDEX idx_challenge_type ON challenge(challenge_type);
CREATE INDEX idx_challenge_active ON challenge(is_active);
CREATE INDEX idx_challenge_dates ON challenge(start_date, end_date);

-- ====================================================================================================
-- Create USER_CHALLENGE table (Feature 21: Gamification)
-- ====================================================================================================
CREATE TABLE user_challenge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    challenge_id UUID NOT NULL,
    current_progress NUMERIC(19, 2) DEFAULT 0,
    target_progress NUMERIC(19, 2),
    is_completed BOOLEAN DEFAULT FALSE,
    completion_date TIMESTAMP,
    points_earned INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_challenge_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_challenge_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_challenge UNIQUE (user_id, challenge_id)
);

-- Indexes for efficient queries
CREATE INDEX idx_user_challenge_user_id ON user_challenge(user_id);
CREATE INDEX idx_user_challenge_challenge_id ON user_challenge(challenge_id);
CREATE INDEX idx_user_challenge_completed ON user_challenge(is_completed);

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

CREATE TRIGGER update_transaction_split_updated_at BEFORE UPDATE ON transaction_split
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_interpersonal_debt_updated_at BEFORE UPDATE ON interpersonal_debt
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_achievement_updated_at BEFORE UPDATE ON achievement
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_achievement_updated_at BEFORE UPDATE ON user_achievement
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_challenge_updated_at BEFORE UPDATE ON challenge
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_challenge_updated_at BEFORE UPDATE ON user_challenge
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ====================================================================================================
-- Insert Default Achievements (Feature 21: Gamification)
-- ====================================================================================================

INSERT INTO achievement (name, description, icon, badge_color, points, achievement_type, criteria_type, criteria_value) VALUES
('First 30 Days', 'Use the app for 30 consecutive days', '🎯', '#4CAF50', 100, 'USAGE', 'CONSECUTIVE_DAYS', 30),
('Budget Master', 'Stay within budget for 3 consecutive months', '💪', '#2196F3', 200, 'BUDGET', 'BUDGET_ADHERENCE', 3),
('Savings Superstar', 'Save 20% or more of your income', '⭐', '#FFC107', 150, 'SAVINGS', 'SAVINGS_RATE', 20),
('Debt Crusher', 'Successfully pay off a debt', '🔨', '#F44336', 250, 'DEBT', 'DEBT_PAYOFF', 1),
('Century Saver', 'Save $100 or more in a single month', '💯', '#9C27B0', 100, 'SAVINGS', 'AMOUNT_SAVED', 100),
('Goal Getter', 'Complete your first savings goal', '🎯', '#00BCD4', 150, 'GOAL', 'GOAL_COMPLETION', 1),
('Transaction Tracker', 'Record 100 transactions', '📊', '#795548', 100, 'USAGE', 'TRANSACTION_COUNT', 100),
('Early Bird', 'Record your first transaction within 24 hours of signing up', '🐦', '#FFEB3B', 50, 'MILESTONE', 'TRANSACTION_COUNT', 1),
('Budget Beginner', 'Create your first budget', '📝', '#8BC34A', 50, 'MILESTONE', 'BUDGET_ADHERENCE', 1),
('Triple Goal Winner', 'Complete 3 financial goals', '🏆', '#FF9800', 300, 'GOAL', 'GOAL_COMPLETION', 3),
('Six Month Streak', 'Use the app for 180 consecutive days', '🔥', '#E91E63', 500, 'STREAK', 'CONSECUTIVE_DAYS', 180),
('Year Long Warrior', 'Use the app for 365 consecutive days', '👑', '#673AB7', 1000, 'STREAK', 'CONSECUTIVE_DAYS', 365),
('Super Saver', 'Save $1,000 or more in a single month', '💎', '#3F51B5', 300, 'SAVINGS', 'AMOUNT_SAVED', 1000),
('Budget Champion', 'Stay within budget for 6 consecutive months', '🏅', '#009688', 400, 'BUDGET', 'BUDGET_ADHERENCE', 6),
('Debt Free', 'Pay off all your debts', '🎊', '#CDDC39', 500, 'DEBT', 'DEBT_PAYOFF', 999); -- 999 means all debts

-- ====================================================================================================
-- Sample Data (Optional - Remove if not needed)
-- ====================================================================================================

-- Create a test user (password is 'password' - you should hash this in production)
INSERT INTO users (id, name, email, password, role) 
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Test User', 'test@expensora.com', '$2b$12$CG/utaylcjUmC3YWXhf9G.jNknv79Z.7.I3CIqkPAgh0M5qu3bEja', 'USER')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    role = EXCLUDED.role;

-- Sample accounts for test user
INSERT INTO account (id, name, account_type, initial_balance, current_balance, currency, icon, color, is_default, active, user_id) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Main Checking', 'BANK_CHECKING', 0.00, 5000.00, 'USD', '🏦', '#4CAF50', TRUE, TRUE, '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440002', 'Savings Account', 'BANK_SAVINGS', 0.00, 10000.00, 'USD', '💰', '#2196F3', FALSE, TRUE, '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440003', 'Credit Card', 'CREDIT_CARD', 0.00, -500.00, 'USD', '💳', '#F44336', FALSE, TRUE, '550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

-- Sample expenses for test user
INSERT INTO expense (id, amount, description, expense_date, category_id, user_id, account_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', 25.50, 'Lunch at restaurant', '2026-02-10', (SELECT id FROM category WHERE name = 'Food & Dining' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440011', 50.00, 'Gas for car', '2026-02-09', (SELECT id FROM category WHERE name = 'Transportation' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440012', 100.00, 'Movie tickets', '2026-02-08', (SELECT id FROM category WHERE name = 'Entertainment' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440003')
ON CONFLICT (id) DO NOTHING;

-- Sample incomes for test user
INSERT INTO income (id, amount, description, income_date, category_id, user_id, account_id) VALUES
('550e8400-e29b-41d4-a716-446655440020', 3000.00, 'Monthly salary', '2026-02-01', (SELECT id FROM category WHERE name = 'Salary' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440021', 500.00, 'Freelance project', '2026-02-05', (SELECT id FROM category WHERE name = 'Freelance' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001')
ON CONFLICT (id) DO NOTHING;

-- Sample budgets for test user
INSERT INTO budget (id, amount, budget_month, budget_year, category_id, user_id) VALUES
('550e8400-e29b-41d4-a716-446655440030', 400.00, 2, 2026, (SELECT id FROM category WHERE name = 'Food & Dining' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440031', 200.00, 2, 2026, (SELECT id FROM category WHERE name = 'Entertainment' AND is_default = TRUE LIMIT 1), '550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

-- Sample goals for test user
INSERT INTO goal (id, name, description, goal_type, target_amount, current_amount, target_date, priority, user_id) VALUES
('550e8400-e29b-41d4-a716-446655440040', 'Emergency Fund', 'Save for emergencies', 'SAVINGS', 5000.00, 1000.00, '2026-12-31', 1, '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440041', 'Vacation Fund', 'Save for summer vacation', 'SAVINGS', 2000.00, 500.00, '2026-06-01', 2, '550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

-- Sample debts for test user
INSERT INTO debt (id, name, debt_type, principal_amount, current_balance, interest_rate, minimum_payment, start_date, target_payoff_date, user_id) VALUES
('550e8400-e29b-41d4-a716-446655440050', 'Student Loan', 'STUDENT_LOAN', 15000.00, 12000.00, 5.5, 200.00, '2020-09-01', '2026-09-01', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440051', 'Car Loan', 'AUTO_LOAN', 8000.00, 6000.00, 4.2, 150.00, '2023-01-01', '2027-01-01', '550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

-- Sample household for test user
INSERT INTO household (id, name, description, owner_id) VALUES
('550e8400-e29b-41d4-a716-446655440060', 'My Family', 'Family household for shared expenses', '550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT (id) DO NOTHING;

-- User-household association
INSERT INTO user_household (id, user_id, household_id, role) VALUES
('550e8400-e29b-41d4-a716-446655440061', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440060', 'ADMIN')
ON CONFLICT (id) DO NOTHING;

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
UNION ALL SELECT 'import_history', COUNT(*) FROM import_history
UNION ALL SELECT 'transaction_split', COUNT(*) FROM transaction_split
UNION ALL SELECT 'interpersonal_debt', COUNT(*) FROM interpersonal_debt
UNION ALL SELECT 'achievement', COUNT(*) FROM achievement
UNION ALL SELECT 'user_achievement', COUNT(*) FROM user_achievement
UNION ALL SELECT 'challenge', COUNT(*) FROM challenge
UNION ALL SELECT 'user_challenge', COUNT(*) FROM user_challenge;
select * from users;
-- ====================================================================================================
-- End of Schema Script
-- ====================================================================================================
