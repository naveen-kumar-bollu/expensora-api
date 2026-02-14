# Render Deployment Guide for Expensora API

## Issues Fixed

1. ✅ **Forced PostgreSQL usage** - Added explicit database driver configuration
2. ✅ **Disabled H2 fallback** - Excluded H2 from runtime and auto-configuration
3. ✅ **Added PostgreSQL dialect** - Ensures proper SQL generation for PostgreSQL

## Render Configuration Steps

### 1. Create PostgreSQL Database on Render

1. Go to your Render Dashboard
2. Click **New** → **PostgreSQL**
3. Name: `expensora-db` (or your preferred name)
4. Database: `expensora`
5. User: (auto-generated)
6. Region: Choose same as your web service
7. Click **Create Database**

### 2. Configure Web Service Environment Variables

In your Render Web Service dashboard, go to **Environment** and add these variables:

#### Required Variables:

```bash
# Database - Use "Internal Database URL" from your PostgreSQL service
DATABASE_URL=<copy from PostgreSQL service Internal Database URL>

# Example format:
# postgresql://expensora_user:password@dpg-xxxxx-a/expensora

# JWT Secret - Generate with: openssl rand -hex 32
JWT_SECRET=<your-secure-256-bit-secret>

# JWT Expiration (10 hours in milliseconds)
JWT_EXPIRATION=36000000

# JWT Refresh Token Expiration (7 days in milliseconds)
JWT_REFRESH_EXPIRATION=604800000

# Database Configuration
DDL_AUTO=none
SHOW_SQL=false

# CORS - Your frontend URL(s)
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-app-preview.vercel.app

# Port (Render sets this automatically)
PORT=8080
```

#### Important Notes:

- **Use Internal Database URL**: Copy the "Internal Database URL" from your PostgreSQL service (not External)
- **Format**: Should be `postgresql://username:password@host/database`
- **Do NOT add** `DB_USERNAME` or `DB_PASSWORD` separately - they're included in `DATABASE_URL`

### 3. Database Initialization

Since `DDL_AUTO=none`, you need to initialize your database schema manually:

#### Option A: Using Render Shell (Recommended)

1. Go to your PostgreSQL service on Render
2. Click **Connect** → **External Connection**
3. Use provided credentials with a PostgreSQL client (e.g., pgAdmin, DBeaver)
4. Run your SQL schema file: `database-schema.sql`

#### Option B: Using psql CLI

```bash
# Connect to your Render PostgreSQL
psql -h <hostname> -U <username> -d expensora

# Run schema file
\i database-schema.sql
```

### 4. Deploy Settings

In your Render Web Service configuration:

**Build Command:**
```bash
./mvnw clean package -DskipTests
```

**Start Command:**
```bash
java -jar target/expensora-api-0.0.1-SNAPSHOT.jar
```

**Docker:**
- If using Dockerfile: Ensure it's in the root of `expensora-api` directory
- Build Context Path: `expensora-api`

### 5. Verify Deployment

After deployment:

1. Check logs for successful startup:
   ```
   Started ExpensoraApiApplication in X seconds
   ```

2. Verify PostgreSQL connection:
   ```
   HHH10001005: Database info:
   Database JDBC URL [jdbc:postgresql://...
   Database driver: PostgreSQL JDBC Driver
   Database dialect: PostgreSQLDialect
   ```

3. Test API health:
   ```
   https://your-app.onrender.com/actuator/health
   ```

4. Test authentication:
   ```
   POST https://your-app.onrender.com/api/auth/register
   ```

## Troubleshooting

### H2 Database Still Being Used

**Symptom:** Logs show `H2Dialect` or `jdbc:h2:mem:`

**Solutions:**
1. Verify `DATABASE_URL` is set correctly in Render environment variables
2. Ensure you're using **Internal Database URL** from your PostgreSQL service
3. Check that PostgreSQL service is running
4. Verify format: `postgresql://user:pass@host/database` (not `postgres://`)

### Old Code/Schema Issues

**Symptom:** Errors about `month` column instead of `budget_month`

**Solutions:**
1. Trigger a fresh build on Render (Manual Deploy → Clear cache)
2. Ensure latest code is pushed to your Git repository
3. Verify Render is connected to the correct branch

### Connection Timeout

**Symptom:** Can't connect to PostgreSQL

**Solutions:**
1. Use **Internal Database URL** (starts with internal hostname)
2. Ensure Web Service and PostgreSQL are in the same region
3. Check PostgreSQL service is not suspended (free tier sleeps after inactivity)

### Schema Not Found

**Symptom:** Table doesn't exist errors

**Solutions:**
1. Run `database-schema.sql` on your PostgreSQL database
2. Or temporarily set `DDL_AUTO=update` to auto-create tables (not recommended for production)

## Security Checklist

- [ ] Generate new JWT_SECRET (don't use default)
- [ ] Set strong CORS_ALLOWED_ORIGINS (specific domains only)
- [ ] Use Internal Database URL (not External)
- [ ] Don't commit secrets to Git
- [ ] Enable Render's Auto-Deploy on Git push
- [ ] Set up database backups on Render

## Next Steps

1. **Commit and push changes:**
   ```bash
   git add .
   git commit -m "fix: Force PostgreSQL and prevent H2 fallback"
   git push
   ```

2. **Trigger Render deployment:**
   - Auto-deploy should trigger
   - Or manually deploy from Render dashboard

3. **Initialize database schema:**
   - Connect to PostgreSQL and run `database-schema.sql`

4. **Monitor logs:**
   - Watch for successful PostgreSQL connection
   - Verify no H2 references

5. **Test API endpoints:**
   - Register a test user
   - Login and get JWT token
   - Test protected endpoints
