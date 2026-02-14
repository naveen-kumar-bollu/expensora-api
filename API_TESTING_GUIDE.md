# Expensora API - Complete Testing Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [Categories](#categories)
4. [Expenses](#expenses)
5. [Income](#income)
6. [Budgets](#budgets)
7. [Recurring Transactions](#recurring-transactions)
8. [Dashboard](#dashboard)
9. [Reports](#reports)
10. [Insights](#insights)

---

## Getting Started

### Base URL
```
http://localhost:8080
```

### Swagger UI
Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui/index.html
```

### Authentication
Most endpoints require JWT authentication. After logging in or registering, include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication

### 1. Register a New User

**Endpoint:** `POST /auth/register`  
**Authentication:** Not required  

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Testing Steps:**
1. Open Swagger UI or use curl/Postman
2. Send POST request with user details
3. Save the `token` from the response for subsequent requests
4. Save the `refreshToken` for token refresh

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "SecurePassword123!"
  }'
```

---

### 2. Login

**Endpoint:** `POST /auth/login`  
**Authentication:** Not required  

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Testing Steps:**
1. Use the email and password from registration
2. Send POST request
3. Save the JWT token for authenticated requests

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePassword123!"
  }'
```

---

### 3. Refresh Access Token

**Endpoint:** `POST /auth/refresh`  
**Authentication:** Not required  

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

---

### 4. Get Current User Profile

**Endpoint:** `GET /auth/me`  
**Authentication:** Required  

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "createdAt": "2026-02-14T10:30:00",
  "updatedAt": "2026-02-14T10:30:00"
}
```

**Testing Steps:**
1. Include JWT token in Authorization header
2. Send GET request
3. Verify user details are returned

**cURL Example:**
```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Update User Profile

**Endpoint:** `PUT /auth/profile`  
**Authentication:** Required  

**Request Body:**
```json
{
  "name": "John Updated Doe"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Updated Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "createdAt": "2026-02-14T10:30:00",
  "updatedAt": "2026-02-14T11:45:00"
}
```

---

### 6. Change Password

**Endpoint:** `POST /auth/change-password`  
**Authentication:** Required  

**Request Body:**
```json
{
  "oldPassword": "SecurePassword123!",
  "newPassword": "NewSecurePassword456!"
}
```

**Response:** `200 OK`

**Testing Steps:**
1. Provide correct old password
2. Provide new password
3. After successful change, use new password for login

---

### 7. Logout

**Endpoint:** `POST /auth/logout`  
**Authentication:** Required  

**Response:** `200 OK`

**Testing Steps:**
1. Send logout request with valid JWT
2. Refresh token will be invalidated
3. After logout, the token should no longer work

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Categories

### 8. Get All Categories

**Endpoint:** `GET /categories`  
**Authentication:** Required  

**Query Parameters:**
- `type` (optional): Filter by "INCOME" or "EXPENSE"

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Food & Dining",
    "type": "EXPENSE",
    "color": "#FF5733",
    "icon": "🍔",
    "isDefault": true,
    "userId": null,
    "createdAt": "2026-02-14T10:00:00",
    "updatedAt": "2026-02-14T10:00:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "name": "Salary",
    "type": "INCOME",
    "color": "#4CAF50",
    "icon": "💰",
    "isDefault": true,
    "userId": null,
    "createdAt": "2026-02-14T10:00:00",
    "updatedAt": "2026-02-14T10:00:00"
  }
]
```

**Testing Steps:**
1. Get all categories without filter to see both INCOME and EXPENSE
2. Test with `?type=EXPENSE` to see only expense categories
3. Test with `?type=INCOME` to see only income categories
4. Verify both default and user-created categories appear

**cURL Example:**
```bash
# Get all categories
curl -X GET http://localhost:8080/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by type
curl -X GET "http://localhost:8080/categories?type=EXPENSE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 9. Create a Custom Category

**Endpoint:** `POST /categories`  
**Authentication:** Required  

**Request Body:**
```json
{
  "name": "Gym Membership",
  "type": "EXPENSE",
  "color": "#9C27B0",
  "icon": "💪"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "name": "Gym Membership",
  "type": "EXPENSE",
  "color": "#9C27B0",
  "icon": "💪",
  "isDefault": false,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-02-14T12:00:00",
  "updatedAt": "2026-02-14T12:00:00"
}
```

**Testing Steps:**
1. Create an expense category
2. Create an income category
3. Verify created categories appear in GET /categories
4. Try creating category with emoji icon
5. Try creating category with hex color code

**cURL Example:**
```bash
curl -X POST http://localhost:8080/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gym Membership",
    "type": "EXPENSE",
    "color": "#9C27B0",
    "icon": "💪"
  }'
```

---

### 10. Update a Category

**Endpoint:** `PUT /categories/{id}`  
**Authentication:** Required  

**Request Body:**
```json
{
  "name": "Fitness & Gym",
  "type": "EXPENSE",
  "color": "#E91E63",
  "icon": "🏋️"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "name": "Fitness & Gym",
  "type": "EXPENSE",
  "color": "#E91E63",
  "icon": "🏋️",
  "isDefault": false,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-02-14T12:00:00",
  "updatedAt": "2026-02-14T12:30:00"
}
```

**Testing Steps:**
1. Get category ID from previous create operation
2. Update the category with new values
3. Verify changes are reflected
4. Note: Cannot update default categories

---

### 11. Delete a Category

**Endpoint:** `DELETE /categories/{id}`  
**Authentication:** Required  

**Response:** `204 No Content`

**Testing Steps:**
1. Delete a user-created category
2. Verify the category no longer appears in GET /categories
3. Note: Cannot delete default categories

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/categories/550e8400-e29b-41d4-a716-446655440010 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Expenses

### 12. Create an Expense

**Endpoint:** `POST /expenses`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 45.50,
  "description": "Grocery shopping at Walmart",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "expenseDate": "2026-02-14",
  "notes": "Weekly groceries",
  "tags": "groceries,food,walmart"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "amount": 45.50,
  "description": "Grocery shopping at Walmart",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "categoryName": "Food & Dining",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expenseDate": "2026-02-14",
  "notes": "Weekly groceries",
  "tags": "groceries,food,walmart",
  "createdAt": "2026-02-14T13:00:00",
  "updatedAt": "2026-02-14T13:00:00"
}
```

**Testing Steps:**
1. Get category ID from GET /categories
2. Create expense with valid data
3. Test with different amounts (decimals, whole numbers)
4. Test with past dates
5. Test with and without notes/tags
6. Verify expense appears in GET /expenses

**cURL Example:**
```bash
curl -X POST http://localhost:8080/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 45.50,
    "description": "Grocery shopping at Walmart",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "expenseDate": "2026-02-14",
    "notes": "Weekly groceries",
    "tags": "groceries,food,walmart"
  }'
```

---

### 13. Get All Expenses (with filters)

**Endpoint:** `GET /expenses`  
**Authentication:** Required  

**Query Parameters:**
- `startDate` (optional): Filter from this date (YYYY-MM-DD)
- `endDate` (optional): Filter to this date (YYYY-MM-DD)
- `categoryId` (optional): Filter by category UUID
- `search` (optional): Search in description/notes
- `minAmount` (optional): Minimum amount
- `maxAmount` (optional): Maximum amount
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (e.g., expenseDate,desc)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440020",
      "amount": 45.50,
      "description": "Grocery shopping at Walmart",
      "categoryId": "550e8400-e29b-41d4-a716-446655440001",
      "categoryName": "Food & Dining",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "expenseDate": "2026-02-14",
      "notes": "Weekly groceries",
      "tags": "groceries,food,walmart",
      "createdAt": "2026-02-14T13:00:00",
      "updatedAt": "2026-02-14T13:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

**Testing Steps:**
1. Get all expenses without filters
2. Filter by date range: `?startDate=2026-02-01&endDate=2026-02-28`
3. Filter by category
4. Search by keyword: `?search=grocery`
5. Filter by amount range: `?minAmount=10&maxAmount=100`
6. Test pagination: `?page=0&size=10`
7. Test sorting: `?sort=expenseDate,desc`
8. Combine multiple filters

**cURL Examples:**
```bash
# Get all expenses
curl -X GET http://localhost:8080/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by date range
curl -X GET "http://localhost:8080/expenses?startDate=2026-02-01&endDate=2026-02-28" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Search expenses
curl -X GET "http://localhost:8080/expenses?search=grocery" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Pagination and sorting
curl -X GET "http://localhost:8080/expenses?page=0&size=10&sort=expenseDate,desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 14. Get Expense by ID

**Endpoint:** `GET /expenses/{id}`  
**Authentication:** Required  

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "amount": 45.50,
  "description": "Grocery shopping at Walmart",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "categoryName": "Food & Dining",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expenseDate": "2026-02-14",
  "notes": "Weekly groceries",
  "tags": "groceries,food,walmart",
  "createdAt": "2026-02-14T13:00:00",
  "updatedAt": "2026-02-14T13:00:00"
}
```

**Testing Steps:**
1. Use an expense ID from previous operations
2. Verify all expense details are returned
3. Test with invalid ID (should return 404)

**cURL Example:**
```bash
curl -X GET http://localhost:8080/expenses/550e8400-e29b-41d4-a716-446655440020 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 15. Update an Expense

**Endpoint:** `PUT /expenses/{id}`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 52.75,
  "description": "Grocery shopping at Walmart - Updated",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "expenseDate": "2026-02-14",
  "notes": "Weekly groceries including cleaning supplies",
  "tags": "groceries,food,walmart,cleaning"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "amount": 52.75,
  "description": "Grocery shopping at Walmart - Updated",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "categoryName": "Food & Dining",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expenseDate": "2026-02-14",
  "notes": "Weekly groceries including cleaning supplies",
  "tags": "groceries,food,walmart,cleaning",
  "createdAt": "2026-02-14T13:00:00",
  "updatedAt": "2026-02-14T14:30:00"
}
```

**Testing Steps:**
1. Update expense amount
2. Change category
3. Update description and notes
4. Verify updatedAt timestamp changes
5. All fields are optional in update

---

### 16. Delete an Expense

**Endpoint:** `DELETE /expenses/{id}`  
**Authentication:** Required  

**Response:** `204 No Content`

**Testing Steps:**
1. Delete an expense by ID
2. Verify expense no longer appears in GET /expenses
3. Try to get deleted expense by ID (should return 404)

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/expenses/550e8400-e29b-41d4-a716-446655440020 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 17. Bulk Delete Expenses

**Endpoint:** `POST /expenses/bulk-delete`  
**Authentication:** Required  

**Request Body:**
```json
[
  "550e8400-e29b-41d4-a716-446655440020",
  "550e8400-e29b-41d4-a716-446655440021",
  "550e8400-e29b-41d4-a716-446655440022"
]
```

**Response:** `204 No Content`

**Testing Steps:**
1. Create multiple expenses
2. Get their IDs
3. Send bulk delete request with array of IDs
4. Verify all expenses are deleted

**cURL Example:**
```bash
curl -X POST http://localhost:8080/expenses/bulk-delete \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '["550e8400-e29b-41d4-a716-446655440020","550e8400-e29b-41d4-a716-446655440021"]'
```

---

## Income

### 18. Create an Income

**Endpoint:** `POST /incomes`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 5000.00,
  "description": "Monthly Salary - February 2026",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "incomeDate": "2026-02-01",
  "notes": "Regular monthly salary",
  "tags": "salary,monthly,work"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440030",
  "amount": 5000.00,
  "description": "Monthly Salary - February 2026",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "Salary",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "incomeDate": "2026-02-01",
  "notes": "Regular monthly salary",
  "tags": "salary,monthly,work",
  "createdAt": "2026-02-14T13:00:00",
  "updatedAt": "2026-02-14T13:00:00"
}
```

**Testing Steps:**
1. Get an income category ID from GET /categories?type=INCOME
2. Create income with various amounts
3. Test with different dates
4. Add notes and tags
5. Verify income appears in GET /incomes

**cURL Example:**
```bash
curl -X POST http://localhost:8080/incomes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "description": "Monthly Salary - February 2026",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "incomeDate": "2026-02-01",
    "notes": "Regular monthly salary",
    "tags": "salary,monthly,work"
  }'
```

---

### 19. Get All Incomes (with filters)

**Endpoint:** `GET /incomes`  
**Authentication:** Required  

**Query Parameters:**
- `startDate` (optional): Filter from this date (YYYY-MM-DD)
- `endDate` (optional): Filter to this date (YYYY-MM-DD)
- `categoryId` (optional): Filter by category UUID
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (e.g., incomeDate,desc)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440030",
      "amount": 5000.00,
      "description": "Monthly Salary - February 2026",
      "categoryId": "550e8400-e29b-41d4-a716-446655440002",
      "categoryName": "Salary",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "incomeDate": "2026-02-01",
      "notes": "Regular monthly salary",
      "tags": "salary,monthly,work",
      "createdAt": "2026-02-14T13:00:00",
      "updatedAt": "2026-02-14T13:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

**Testing Steps:**
1. Get all incomes
2. Filter by date range
3. Filter by category
4. Test pagination
5. Test sorting

**cURL Example:**
```bash
# Get all incomes
curl -X GET http://localhost:8080/incomes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Filter by date range
curl -X GET "http://localhost:8080/incomes?startDate=2026-02-01&endDate=2026-02-28" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 20. Get Income by ID

**Endpoint:** `GET /incomes/{id}`  
**Authentication:** Required  

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440030",
  "amount": 5000.00,
  "description": "Monthly Salary - February 2026",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "Salary",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "incomeDate": "2026-02-01",
  "notes": "Regular monthly salary",
  "tags": "salary,monthly,work",
  "createdAt": "2026-02-14T13:00:00",
  "updatedAt": "2026-02-14T13:00:00"
}
```

---

### 21. Get Monthly Income Summary

**Endpoint:** `GET /incomes/summary`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
5000.00
```

**Testing Steps:**
1. Create multiple incomes for a month
2. Get summary for that month
3. Verify sum is correct
4. Test with different months
5. Test with month having no income (should return 0)

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/incomes/summary?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 22. Update an Income

**Endpoint:** `PUT /incomes/{id}`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 5200.00,
  "description": "Monthly Salary - February 2026 (With Bonus)",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "incomeDate": "2026-02-01",
  "notes": "Salary with performance bonus",
  "tags": "salary,monthly,work,bonus"
}
```

**Response:** `200 OK`

**Testing Steps:**
1. Update income amount
2. Change description
3. Update category
4. Modify notes and tags

---

### 23. Delete an Income

**Endpoint:** `DELETE /incomes/{id}`  
**Authentication:** Required  

**Response:** `204 No Content`

**Testing Steps:**
1. Delete an income by ID
2. Verify it no longer appears in GET /incomes

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/incomes/550e8400-e29b-41d4-a716-446655440030 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Budgets

### 24. Create a Budget

**Endpoint:** `POST /budgets`  
**Authentication:** Required  

**Request Body:**
```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 300.00,
  "month": 2,
  "year": 2026
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 300.00,
  "month": 2,
  "year": 2026,
  "createdAt": "2026-02-14T15:00:00",
  "updatedAt": "2026-02-14T15:00:00"
}
```

**Testing Steps:**
1. Get an expense category ID
2. Create budget for current month
3. Set reasonable budget amount
4. Try creating duplicate budget for same category/month (should fail)
5. Create budgets for multiple categories

**cURL Example:**
```bash
curl -X POST http://localhost:8080/budgets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 300.00,
    "month": 2,
    "year": 2026
  }'
```

---

### 25. Get Budgets for a Month

**Endpoint:** `GET /budgets`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
[
  {
    "budgetId": "550e8400-e29b-41d4-a716-446655440040",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryName": "Food & Dining",
    "budgetAmount": 300.00,
    "spentAmount": 45.50,
    "remainingAmount": 254.50,
    "percentageUsed": 15.17,
    "isOverBudget": false,
    "month": 2,
    "year": 2026
  }
]
```

**Testing Steps:**
1. Get budgets for current month
2. Compare budgetAmount with spentAmount
3. Verify percentageUsed calculation
4. Check isOverBudget flag when spending exceeds budget
5. Test with different months

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/budgets?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 26. Get Budget History for a Category

**Endpoint:** `GET /budgets/history`  
**Authentication:** Required  

**Query Parameters:**
- `categoryId` (required): Category UUID

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440040",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 300.00,
    "month": 2,
    "year": 2026
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440041",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 280.00,
    "month": 1,
    "year": 2026
  }
]
```

**Testing Steps:**
1. Create budgets for same category across multiple months
2. Get history to see budget trends
3. Analyze spending patterns over time

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/budgets/history?categoryId=550e8400-e29b-41d4-a716-446655440001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 27. Update a Budget

**Endpoint:** `PUT /budgets/{id}`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 350.00,
  "month": 2,
  "year": 2026
}
```

**Response:** `200 OK`

**Testing Steps:**
1. Update budget amount
2. Verify new amount is reflected
3. Check if spending alerts change based on new budget

---

### 28. Delete a Budget

**Endpoint:** `DELETE /budgets/{id}`  
**Authentication:** Required  

**Response:** `204 No Content`

**Testing Steps:**
1. Delete a budget by ID
2. Verify it no longer appears in GET /budgets

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/budgets/550e8400-e29b-41d4-a716-446655440040 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Recurring Transactions

### 29. Create a Recurring Transaction

**Endpoint:** `POST /recurring-transactions`  
**Authentication:** Required  

**Request Body:**
```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 5000.00,
  "description": "Monthly Salary",
  "transactionType": "INCOME",
  "frequency": "MONTHLY",
  "startDate": "2026-02-01",
  "endDate": null
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440050",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 5000.00,
  "description": "Monthly Salary",
  "transactionType": "INCOME",
  "frequency": "MONTHLY",
  "startDate": "2026-02-01",
  "endDate": null,
  "lastExecutionDate": null,
  "active": true,
  "createdAt": "2026-02-14T16:00:00",
  "updatedAt": "2026-02-14T16:00:00"
}
```

**Testing Steps:**
1. Create a recurring income (salary)
2. Create a recurring expense (rent, subscriptions)
3. Test different frequencies:
   - DAILY: Daily recurring transaction
   - WEEKLY: Weekly recurring transaction
   - MONTHLY: Monthly recurring transaction
4. Test with and without end date
5. Verify transactions are created automatically by scheduler

**cURL Example:**
```bash
curl -X POST http://localhost:8080/recurring-transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "amount": 5000.00,
    "description": "Monthly Salary",
    "transactionType": "INCOME",
    "frequency": "MONTHLY",
    "startDate": "2026-02-01",
    "endDate": null
  }'
```

**Expense Example:**
```bash
curl -X POST http://localhost:8080/recurring-transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "550e8400-e29b-41d4-a716-446655440008",
    "amount": 1200.00,
    "description": "Monthly Rent",
    "transactionType": "EXPENSE",
    "frequency": "MONTHLY",
    "startDate": "2026-02-01",
    "endDate": "2026-12-01"
  }'
```

---

### 30. Get All Recurring Transactions

**Endpoint:** `GET /recurring-transactions`  
**Authentication:** Required  

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440050",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "categoryName": "Salary",
    "amount": 5000.00,
    "description": "Monthly Salary",
    "transactionType": "INCOME",
    "frequency": "MONTHLY",
    "startDate": "2026-02-01",
    "endDate": null,
    "lastExecutionDate": "2026-02-01",
    "active": true,
    "createdAt": "2026-02-14T16:00:00"
  }
]
```

**Testing Steps:**
1. Get all recurring transactions
2. Verify both active and inactive transactions
3. Check lastExecutionDate to see when it last ran
4. Monitor active status

**cURL Example:**
```bash
curl -X GET http://localhost:8080/recurring-transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 31. Update a Recurring Transaction

**Endpoint:** `PUT /recurring-transactions/{id}`  
**Authentication:** Required  

**Request Body:**
```json
{
  "amount": 5200.00,
  "description": "Monthly Salary (Increased)",
  "frequency": "MONTHLY",
  "endDate": null
}
```

**Response:** `200 OK`

**Testing Steps:**
1. Update amount for salary increase/decrease
2. Change frequency
3. Set or update end date
4. Verify future transactions reflect changes

---

### 32. Delete a Recurring Transaction

**Endpoint:** `DELETE /recurring-transactions/{id}`  
**Authentication:** Required  

**Response:** `204 No Content`

**Testing Steps:**
1. Delete recurring transaction
2. Verify it stops creating new transactions
3. Note: Past transactions created by this recurring entry remain

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/recurring-transactions/550e8400-e29b-41d4-a716-446655440050 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Dashboard

### 33. Get Monthly Dashboard Summary

**Endpoint:** `GET /dashboard/summary`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
{
  "totalIncome": 5000.00,
  "totalExpenses": 1250.75,
  "balance": 3749.25,
  "budgetUtilization": 65.5,
  "topSpendingCategory": "Food & Dining",
  "month": 2,
  "year": 2026
}
```

**Testing Steps:**
1. Create some income and expenses for a month
2. Get dashboard summary
3. Verify calculations:
   - balance = totalIncome - totalExpenses
   - budgetUtilization percentage
4. Check topSpendingCategory matches highest spending
5. Test with different months

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/dashboard/summary?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 34. Get Category Breakdown

**Endpoint:** `GET /dashboard/category-breakdown`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
[
  {
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryName": "Food & Dining",
    "totalAmount": 450.50,
    "percentage": 36.0,
    "transactionCount": 12
  },
  {
    "categoryId": "550e8400-e29b-41d4-a716-446655440003",
    "categoryName": "Transportation",
    "totalAmount": 300.00,
    "percentage": 24.0,
    "transactionCount": 8
  }
]
```

**Testing Steps:**
1. Create expenses in different categories
2. Get category breakdown
3. Verify percentage adds up to 100%
4. Check transaction counts
5. Verify sorted by amount (highest first)

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/dashboard/category-breakdown?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 35. Get Monthly Trend

**Endpoint:** `GET /dashboard/monthly-trend`  
**Authentication:** Required  

**Query Parameters:**
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
[
  {
    "month": 1,
    "year": 2026,
    "totalIncome": 5000.00,
    "totalExpenses": 3500.00,
    "balance": 1500.00
  },
  {
    "month": 2,
    "year": 2026,
    "totalIncome": 5000.00,
    "totalExpenses": 1250.75,
    "balance": 3749.25
  }
]
```

**Testing Steps:**
1. Create transactions across multiple months
2. Get yearly trend
3. Analyze income/expense patterns
4. Identify months with highest/lowest balance
5. Useful for financial planning

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/dashboard/monthly-trend?year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 36. Get Top Spending Category

**Endpoint:** `GET /dashboard/top-spending-category`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
"Food & Dining"
```

**Testing Steps:**
1. Create expenses in various categories
2. Get top spending category
3. Verify it matches category with highest total
4. Use for budget adjustment recommendations

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/dashboard/top-spending-category?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Reports

### 37. Export Expenses to CSV

**Endpoint:** `GET /reports/expenses/csv`  
**Authentication:** Required  

**Query Parameters:**
- `month` (optional): Filter by month (1-12)
- `year` (optional): Filter by year (e.g., 2026)

**Response:** `200 OK` (CSV file download)
```csv
ID,Amount,Description,Category,Date,Notes,Tags,Created At
550e8400-e29b-41d4-a716-446655440020,45.50,"Grocery shopping",Food & Dining,2026-02-14,"Weekly groceries","groceries,food",2026-02-14T13:00:00
```

**Testing Steps:**
1. Export all expenses (no filters)
2. Export for specific month/year
3. Verify CSV file downloads
4. Open in Excel/Google Sheets
5. Verify data integrity

**cURL Example:**
```bash
# Export all expenses
curl -X GET http://localhost:8080/reports/expenses/csv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o expenses.csv

# Export for specific month
curl -X GET "http://localhost:8080/reports/expenses/csv?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o expenses_feb_2026.csv
```

---

### 38. Export Income to CSV

**Endpoint:** `GET /reports/income/csv`  
**Authentication:** Required  

**Query Parameters:**
- `month` (optional): Filter by month (1-12)
- `year` (optional): Filter by year (e.g., 2026)

**Response:** `200 OK` (CSV file download)
```csv
ID,Amount,Description,Category,Date,Notes,Tags,Created At
550e8400-e29b-41d4-a716-446655440030,5000.00,"Monthly Salary",Salary,2026-02-01,"Regular salary","salary,monthly",2026-02-14T13:00:00
```

**Testing Steps:**
1. Export all income
2. Export for specific month/year
3. Verify CSV format
4. Use for tax preparation or financial analysis

**cURL Example:**
```bash
# Export all income
curl -X GET http://localhost:8080/reports/income/csv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o income.csv

# Export for specific month
curl -X GET "http://localhost:8080/reports/income/csv?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o income_feb_2026.csv
```

---

### 39. Generate Monthly Summary Report

**Endpoint:** `GET /reports/monthly-summary`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK` (Text file download)
```
MONTHLY FINANCIAL SUMMARY
Month: February 2026

INCOME SUMMARY
--------------
Total Income: $5,000.00
Number of Transactions: 1

  - Salary: $5,000.00

EXPENSE SUMMARY
---------------
Total Expenses: $1,250.75
Number of Transactions: 15

  - Food & Dining: $450.50 (36%)
  - Transportation: $300.00 (24%)
  - Entertainment: $200.25 (16%)
  - Other: $300.00 (24%)

BALANCE
-------
Net Balance: $3,749.25
Savings Rate: 74.98%

BUDGET PERFORMANCE
------------------
Food & Dining: $450.50 / $300.00 (150% - Over Budget)
Transportation: $300.00 / $400.00 (75% - On Track)
```

**Testing Steps:**
1. Generate report for current month
2. Download and view text file
3. Verify all calculations
4. Use for monthly financial review

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/reports/monthly-summary?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o monthly_summary_feb_2026.txt
```

---

## Insights

### 40. Get Financial Insights

**Endpoint:** `GET /insights`  
**Authentication:** Required  

**Query Parameters:**
- `month` (required): Month number (1-12)
- `year` (required): Year (e.g., 2026)

**Response:** `200 OK`
```json
{
  "month": 2,
  "year": 2026,
  "insights": [
    {
      "type": "BUDGET_ALERT",
      "severity": "WARNING",
      "message": "You've spent 150% of your Food & Dining budget this month",
      "recommendation": "Consider reducing dining out or increasing your budget",
      "categoryId": "550e8400-e29b-41d4-a716-446655440001"
    },
    {
      "type": "SPENDING_TREND",
      "severity": "INFO",
      "message": "Your spending is 20% lower than last month",
      "recommendation": "Great job! Keep up the good savings habits",
      "categoryId": null
    },
    {
      "type": "TOP_CATEGORY",
      "severity": "INFO",
      "message": "Food & Dining is your highest expense category at 36% of total spending",
      "recommendation": "Look for ways to optimize food expenses",
      "categoryId": "550e8400-e29b-41d4-a716-446655440001"
    }
  ],
  "savingsRate": 74.98,
  "comparisonWithLastMonth": {
    "incomeChange": 0.0,
    "expenseChange": -20.5,
    "trend": "IMPROVING"
  }
}
```

**Testing Steps:**
1. Create expenses and budgets
2. Get insights for current month
3. Review different insight types:
   - BUDGET_ALERT: Budget warnings
   - SPENDING_TREND: Spending patterns
   - TOP_CATEGORY: Analysis of top categories
   - SAVINGS_OPPORTUNITY: Ways to save
4. Check severity levels (INFO, WARNING, CRITICAL)
5. Follow recommendations to improve finances

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/insights?month=2&year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Complete End-to-End Testing Workflow

### Scenario: New User Complete Journey

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Sarah Johnson","email":"sarah@example.com","password":"SecurePass123!"}'
# Save the token from response

export TOKEN="your-token-here"

# 2. Get profile
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Get default categories
curl -X GET http://localhost:8080/categories \
  -H "Authorization: Bearer $TOKEN"

# 4. Create a custom category
curl -X POST http://localhost:8080/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Gym","type":"EXPENSE","color":"#9C27B0","icon":"💪"}'

# 5. Record income
curl -X POST http://localhost:8080/incomes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":5000,"description":"Monthly Salary","categoryId":"<salary-category-id>","incomeDate":"2026-02-01","notes":"February salary","tags":"salary,work"}'

# 6. Create expenses
curl -X POST http://localhost:8080/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":50,"description":"Groceries","categoryId":"<food-category-id>","expenseDate":"2026-02-14","notes":"Weekly shopping","tags":"food,groceries"}'

# 7. Set budgets
curl -X POST http://localhost:8080/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"categoryId":"<food-category-id>","amount":300,"month":2,"year":2026}'

# 8. Create recurring transaction
curl -X POST http://localhost:8080/recurring-transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"categoryId":"<rent-category-id>","amount":1200,"description":"Monthly Rent","transactionType":"EXPENSE","frequency":"MONTHLY","startDate":"2026-02-01"}'

# 9. View dashboard
curl -X GET "http://localhost:8080/dashboard/summary?month=2&year=2026" \
  -H "Authorization: Bearer $TOKEN"

# 10. Get insights
curl -X GET "http://localhost:8080/insights?month=2&year=2026" \
  -H "Authorization: Bearer $TOKEN"

# 11. Export reports
curl -X GET "http://localhost:8080/reports/expenses/csv?month=2&year=2026" \
  -H "Authorization: Bearer $TOKEN" \
  -o my_expenses.csv

# 12. Logout
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## Testing with Swagger UI

### Step-by-Step Swagger Testing:

1. **Open Swagger UI**
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

2. **Register/Login**
   - Expand "Authentication" section
   - Click "POST /auth/register" 
   - Click "Try it out"
   - Fill in request body
   - Click "Execute"
   - Copy the token from response

3. **Authorize**
   - Click "Authorize" button (🔒) at top
   - Enter: `Bearer <your-token>`
   - Click "Authorize"
   - Click "Close"

4. **Test Endpoints**
   - Now all protected endpoints are accessible
   - Click any endpoint → "Try it out" → Fill params → "Execute"
   - View responses in real-time

5. **Monitor Requests**
   - Each request shows:
     - Request URL
     - Response Code
     - Response Body
     - Response Headers

---

## Common Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-02-14T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/expenses"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-02-14T16:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/expenses"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-02-14T16:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/expenses/550e8400-e29b-41d4-a716-446655440020"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-02-14T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found",
  "path": "/expenses/invalid-id"
}
```

---

## Testing Checklist

### Authentication
- [ ] User registration works
- [ ] Login with correct credentials
- [ ] Login fails with wrong password
- [ ] JWT token is returned
- [ ] Token works for protected endpoints
- [ ] Token refresh works
- [ ] Logout invalidates token
- [ ] Password change works
- [ ] Profile update works

### Categories
- [ ] Get all default categories
- [ ] Filter by INCOME type
- [ ] Filter by EXPENSE type
- [ ] Create custom category
- [ ] Update custom category
- [ ] Delete custom category
- [ ] Cannot delete default categories

### Expenses
- [ ] Create expense with all fields
- [ ] Create expense with minimal fields
- [ ] Get all expenses
- [ ] Filter by date range
- [ ] Filter by category
- [ ] Search expenses
- [ ] Filter by amount range
- [ ] Pagination works
- [ ] Sorting works
- [ ] Get expense by ID
- [ ] Update expense
- [ ] Delete expense
- [ ] Bulk delete expenses

### Income
- [ ] Create income
- [ ] Get all incomes
- [ ] Filter incomes
- [ ] Get monthly summary
- [ ] Update income
- [ ] Delete income

### Budgets
- [ ] Create budget
- [ ] Get budgets for month
- [ ] Budget shows spending correctly
- [ ] Over-budget flag works
- [ ] Get budget history
- [ ] Update budget
- [ ] Delete budget
- [ ] Cannot create duplicate budgets

### Recurring Transactions
- [ ] Create recurring income
- [ ] Create recurring expense
- [ ] Test DAILY frequency
- [ ] Test WEEKLY frequency
- [ ] Test MONTHLY frequency
- [ ] Transactions auto-create
- [ ] Update recurring transaction
- [ ] Delete recurring transaction

### Dashboard
- [ ] Monthly summary shows correct data
- [ ] Category breakdown percentages correct
- [ ] Monthly trend shows all months
- [ ] Top spending category correct

### Reports
- [ ] Export expenses CSV
- [ ] Export income CSV
- [ ] Generate monthly summary
- [ ] Files download correctly
- [ ] Data in exports is accurate

### Insights
- [ ] Insights generated
- [ ] Budget alerts show
- [ ] Spending trends analyzed
- [ ] Recommendations provided
- [ ] Severity levels correct

---

## Performance Testing Tips

1. **Test with Large Data Sets**
   - Create 100+ expenses
   - Test pagination performance
   - Verify query response times

2. **Concurrent Users**
   - Create multiple user accounts
   - Test simultaneous requests
   - Verify isolation between users

3. **Date Range Queries**
   - Test with 1 year of data
   - Test extreme date ranges
   - Verify indexing performance

---

## Security Testing

1. **Authentication**
   - Test expired tokens
   - Test invalid tokens
   - Test without token

2. **Authorization**
   - Try accessing other user's data
   - Verify user isolation
   - Test role-based access

3. **Input Validation**
   - Test with negative amounts
   - Test with future dates
   - Test with very large amounts
   - Test SQL injection attempts
   - Test XSS attempts

---

## Troubleshooting

### Token Not Working
- Check Bearer prefix
- Verify token hasn't expired
- Re-login if needed

### 403 Forbidden
- Ensure using correct Authorization header
- Check token is valid

### Data Not Showing
- Verify user isolation
- Check date filters
- Confirm data was created successfully

### Pagination Issues
- Check page parameter (0-based)
- Verify size parameter
- Check totalElements in response

---

## Contact & Support

For issues or questions:
- GitHub: [expensora-api](https://github.com/naveen-kumar-bollu/expensora-api)
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs

---

**Last Updated:** February 14, 2026
**API Version:** 1.0.0
