# JWT & OAuth Authentication — Deep Dive Guide
### Using `expensora-api` as a Real-World Example

---

## Table of Contents

1. [What is Authentication vs Authorization?](#1-what-is-authentication-vs-authorization)
2. [JWT (JSON Web Token)](#2-jwt-json-web-token)
   - [What is JWT?](#21-what-is-jwt)
   - [JWT Structure — The 3 Parts](#22-jwt-structure--the-3-parts)
   - [How JWT Works — Full Flow](#23-how-jwt-works--full-flow)
   - [JWT in Expensora-API — Code Walkthrough](#24-jwt-in-expensora-api--code-walkthrough)
   - [Access Token vs Refresh Token](#25-access-token-vs-refresh-token)
   - [Why Stateless? SessionCreationPolicy.STATELESS Explained](#26-why-stateless-sessioncreationpolicystateless-explained)
3. [OAuth 2.0](#3-oauth-20)
   - [What is OAuth?](#31-what-is-oauth)
   - [Key Roles in OAuth](#32-key-roles-in-oauth)
   - [OAuth 2.0 Grant Types (Flows)](#33-oauth-20-grant-types-flows)
   - [Authorization Code Flow — Detailed](#34-authorization-code-flow--detailed)
   - [JWT vs OAuth — Are They Different Things?](#35-jwt-vs-oauth--are-they-different-things)
   - [How Expensora-API Could Add Google OAuth](#36-how-expensora-api-could-add-google-oauth)
4. [Security Best Practices Used in Expensora-API](#4-security-best-practices-used-in-expensora-api)
5. [Interview Questions — 2 Years Experience Level](#5-interview-questions--2-years-experience-level)

---

## 1. What is Authentication vs Authorization?

Before diving into JWT and OAuth, it is essential to understand two terms that are frequently confused:

| Term | Meaning | Example in Expensora |
|---|---|---|
| **Authentication** | Proving WHO you are | Logging in with email + password |
| **Authorization** | Deciding WHAT you can access | Only the logged-in user can see their own expenses |

In expensora-api:
- **Authentication** happens at `/auth/login` — the system verifies your credentials and gives you a token.
- **Authorization** happens on every other request — the system checks if your token allows you to access that resource.

---

## 2. JWT (JSON Web Token)

### 2.1 What is JWT?

JWT (pronounced "jot") is an **open standard (RFC 7519)** for securely transmitting information between two parties (client and server) as a compact, self-contained JSON object. The important characteristics are:

- **Compact** — small enough to be sent in an HTTP header, URL, or cookie.
- **Self-contained** — the token itself carries all the information the server needs; no database lookup is required to validate it.
- **Stateless** — the server does not need to store session data anywhere.

Think of a JWT as a **sealed envelope**. When you log in, the server puts your user info inside the envelope, seals it with its private signature, and hands it back to you. Every time you make a request, you show this envelope, and the server can verify the seal is authentic without checking any records.

---

### 2.2 JWT Structure — The 3 Parts

A JWT looks like this:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA4NTAyMDAwLCJleHAiOjE3MDg1MzgwMDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

It has exactly **three parts separated by dots (`.`)**:

```
HEADER.PAYLOAD.SIGNATURE
```

#### Part 1 — Header

The header describes the token's type and the hashing algorithm used.

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- `alg`: The signing algorithm. In expensora-api, `HS256` (HMAC-SHA256) is used — a symmetric algorithm where the same secret key is used to sign and verify.
- `typ`: Always `JWT`.

This JSON is then **Base64Url encoded** (not encrypted!) to form Part 1.

#### Part 2 — Payload (Claims)

The payload contains the actual data, called **claims**. Claims are statements about the user.

```json
{
  "sub": "user@example.com",
  "iat": 1708502000,
  "exp": 1708538000
}
```

| Claim | Full Name | Meaning |
|---|---|---|
| `sub` | Subject | Who the token is about (email in expensora) |
| `iat` | Issued At | When the token was created (Unix timestamp) |
| `exp` | Expiration | When the token expires (Unix timestamp) |

In expensora-api, only the email is stored in the `sub` claim. You can also add **custom claims** like `role`, `userId`, etc.

> **Important:** The payload is Base64Url encoded, NOT encrypted. Anyone can decode it. Never put sensitive data (passwords, card numbers) in the payload.

#### Part 3 — Signature

This is what makes JWT secure. It is created by taking:

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

In expensora-api, the secret key comes from `application.properties`:

```properties
jwt.secret=${JWT_SECRET:generate_your_own_256bit_secret_here}
```

And in `JwtUtil.java`, this secret is used to create the `SecretKey`:

```java
// JwtUtil.java
public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long jwtExpiration,
        @Value("${jwt.refresh-expiration}") long refreshExpiration) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.jwtExpiration = jwtExpiration;
    this.refreshExpiration = refreshExpiration;
}
```

If someone changes even one character in the payload, the signature will no longer match. This is how the server knows the token was not tampered with.

---

### 2.3 How JWT Works — Full Flow

Here is the complete flow of JWT authentication in expensora-api:

```
CLIENT                              SERVER (expensora-api)
  |                                        |
  |  POST /auth/login                      |
  |  { email: "x@x.com", pass: "1234" }   |
  |--------------------------------------> |
  |                                        |  1. AuthController.login() is called
  |                                        |  2. AuthenticationManager.authenticate()
  |                                        |     checks email + password against DB
  |                                        |  3. JwtUtil.generateToken(email) creates
  |                                        |     an Access Token (expires in 10 hours)
  |                                        |  4. UserService.generateRefreshToken()
  |                                        |     creates a Refresh Token (expires in 7 days)
  |                                        |     and saves it to users table in DB
  |                                        |
  |  { accessToken: "eyJ...", }            |
  |  { refreshToken: "eyJ..." }            |
  |<--------------------------------------- |
  |                                        |
  |  GET /expenses  (with Access Token)    |
  |  Header: Authorization: Bearer eyJ... |
  |--------------------------------------> |
  |                                        |  1. JwtAuthenticationFilter intercepts
  |                                        |  2. Extracts token from Authorization header
  |                                        |  3. JwtUtil.extractUsername() reads `sub` claim
  |                                        |  4. UserDetailsService.loadUserByUsername()
  |                                        |     loads user from DB
  |                                        |  5. JwtUtil.validateToken() checks:
  |                                        |     - username matches
  |                                        |     - token is not expired
  |                                        |  6. Sets Authentication in SecurityContext
  |                                        |  7. Request proceeds to ExpenseController
  |                                        |
  |  200 OK — expense list                 |
  |<--------------------------------------- |
  |                                        |
  |--- Access Token expires after 10h ----|
  |                                        |
  |  POST /auth/refresh                    |
  |  { refreshToken: "eyJ..." }            |
  |--------------------------------------> |
  |                                        |  1. JwtUtil.extractUsername() reads email
  |                                        |     from refresh token
  |                                        |  2. Loads user from DB, compares stored
  |                                        |     refresh token against provided token
  |                                        |  3. Validates token is not expired
  |                                        |  4. Generates a new Access Token
  |                                        |
  |  { accessToken: "new eyJ..." }         |
  |<--------------------------------------- |
```

---

### 2.4 JWT in Expensora-API — Code Walkthrough

Let's trace every class involved in JWT, in the order they participate in a request.

---

#### Step 1 — Configuration: `application.properties`

```properties
jwt.secret=${JWT_SECRET:generate_your_own_256bit_secret_here}
jwt.expiration=${JWT_EXPIRATION:36000000}          # 10 hours in milliseconds
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800000}   # 7 days in milliseconds
```

- `JWT_SECRET`: A strong, random, 256-bit secret. The comment even tells you to generate it with `openssl rand -hex 32`.
- `jwt.expiration`: How long the access token lives (10 hours = 36,000,000 ms).
- `jwt.refresh-expiration`: How long the refresh token lives (7 days = 604,800,000 ms).

---

#### Step 2 — Token Creation: `JwtUtil.java`

This is the core utility class responsible for all JWT operations.

```java
// Generates a short-lived ACCESS token
public String generateToken(String username) {
    return createToken(username);
}

// Generates a long-lived REFRESH token
public String generateRefreshToken(String username) {
    return Jwts.builder()
            .setSubject(username)          // stores email in "sub" claim
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
}

private String createToken(String subject) {
    return Jwts.builder()
            .setSubject(subject)           // stores email in "sub" claim
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();                    // serializes to compact JWT string
}
```

The `Jwts.builder()` pattern is from the **jjwt** library. It builds the header, payload, and signature, then calls `.compact()` to serialize everything into the `header.payload.signature` string.

**Validating a token:**

```java
// Parses the token and extracts all claims
private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(secretKey)      // uses the same secret key to verify signature
            .build()
            .parseClaimsJws(token)         // throws JwtException if signature is invalid or expired
            .getBody();
}

// Validates that the token belongs to the right user and hasn't expired
public Boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
}
```

If the token was tampered with, `parseClaimsJws()` will throw a `JwtException` because the recalculated signature will not match the one in the token.

---

#### Step 3 — Login Endpoint: `AuthController.java`

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
    try {
        // Step 1: Spring Security verifies email + password against DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        // Step 2: Generate access token using the authenticated email
        String token = jwtUtil.generateToken(authentication.getName());

        // Step 3: Generate refresh token and save to DB
        String refreshToken = userService.generateRefreshToken(authentication.getName());

        // Step 4: Return both tokens to client
        return ResponseEntity.ok(new AuthResponseDto(token, refreshToken));

    } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
    }
}
```

Notice that `authenticationManager.authenticate()` delegates to `UserDetailsService.loadUserByUsername()` internally, which you implemented in `UserServiceImpl`:

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority(user.getRole().name()))
    );
}
```

Spring Security automatically compares the provided password against the BCrypt-encoded password in the DB using `BCryptPasswordEncoder` (configured in `AppConfig.java`).

---

#### Step 4 — Refresh Token Storage: `UserServiceImpl.java`

The refresh token is stored in the `users` table:

```java
@Override
public String generateRefreshToken(String email) {
    String refreshToken = jwtUtil.generateRefreshToken(email);
    User user = findByEmail(email);
    user.setRefreshToken(refreshToken);      // saved to DB column
    userRepository.save(user);
    return refreshToken;
}
```

This is important — the refresh token is stored in the `User` entity's `refreshToken` field. When the client calls `/auth/refresh`, the server compares the provided token against the one stored in the DB:

```java
@Override
public String refreshAccessToken(String refreshToken) {
    String email = jwtUtil.extractUsername(refreshToken);
    User user = findByEmail(email);

    // Security check: does this token match what we saved?
    if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
        throw new RuntimeException("Invalid refresh token");
    }

    // Is the token still valid (not expired)?
    if (!jwtUtil.validateToken(refreshToken, email)) {
        throw new RuntimeException("Refresh token expired");
    }

    return jwtUtil.generateToken(email);   // issue a new access token
}
```

And on logout, the refresh token is erased from the DB, making it impossible to get new access tokens:

```java
@Override
public void logout(String email) {
    User user = findByEmail(email);
    user.setRefreshToken(null);     // invalidates the refresh token
    userRepository.save(user);
}
```

---

#### Step 5 — Request Interception: `JwtAuthenticationFilter.java`

This filter runs on **every single HTTP request** before it reaches any controller. It extends `OncePerRequestFilter`, which guarantees it executes exactly once per request.

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {

    // Step 1: Read the Authorization header
    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;

    // Step 2: Check if it starts with "Bearer "
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);  // strip "Bearer " prefix
        try {
            username = jwtUtil.extractUsername(jwt);  // extract email from token
        } catch (ExpiredJwtException e) {
            // Token is valid but expired — send specific error
            sendErrorResponse(response, 401, "Token Expired", "Your session has expired. Please log in again.");
            return;
        } catch (JwtException e) {
            // Token is malformed or signature is invalid
            sendErrorResponse(response, 401, "Invalid Token", "The provided token is invalid or malformed.");
            return;
        }
    }

    // Step 3: If we have a username and no authentication yet in the context
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
            // Step 4: Create authentication object
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Step 5: Store in SecurityContext — marks user as authenticated
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    // Step 6: Continue to the next filter or controller
    filterChain.doFilter(request, response);
}
```

The `SecurityContextHolder` is a thread-local store. Once you set the authentication here, any controller in the same request thread can retrieve it via:

```java
SecurityContextHolder.getContext().getAuthentication().getName()
```

This is exactly what `AuthController.getCurrentUser()` does:

```java
@GetMapping("/me")
public ResponseEntity<UserDto> getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userService.findByEmail(email);
    return ResponseEntity.ok(userMapper.toDto(user));
}
```

---

#### Step 6 — Security Rules: `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())   // CSRF not needed with JWT (stateless)
        .authorizeHttpRequests(authz -> authz
            // These endpoints do NOT require a token
            .requestMatchers("/", "/health", "/error").permitAll()
            .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // Everything else REQUIRES a valid JWT
            .anyRequest().authenticated()
        )
        // Use stateless sessions — no HttpSession is created
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Place JwtAuthenticationFilter BEFORE Spring Security's default login filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

The line `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` ensures your JWT filter runs first in the filter chain, populating the `SecurityContext` before Spring Security tries to check if the user is authenticated.

---

### 2.5 Access Token vs Refresh Token

| Feature | Access Token | Refresh Token |
|---|---|---|
| **Purpose** | Authenticate API requests | Get a new access token |
| **Lifetime** | Short — 10 hours in expensora | Long — 7 days in expensora |
| **Sent with** | Every API request in the `Authorization` header | Only to `/auth/refresh` |
| **Stored on client** | Memory or localStorage | Secure cookie or localStorage |
| **Stored on server** | Not stored (stateless) | Stored in `users.refreshToken` column |
| **Revocable?** | No (until it expires naturally) | Yes (server can set it to NULL on logout) |

**Why two tokens?**

If access tokens had a very long lifetime (e.g., 30 days), a stolen token would give an attacker 30 days of access and you cannot revoke it because there is no server-side state. 

By keeping access tokens short-lived (10 hours) and using refresh tokens that ARE stored in the DB, you get the best of both worlds:
- Stateless, fast verification for most requests.
- Ability to revoke long-term access by deleting the refresh token on logout.

---

### 2.6 Why Stateless? `SessionCreationPolicy.STATELESS` Explained

Traditionally, web apps used **sessions**:
1. User logs in.
2. Server creates a session object and stores it in memory (or Redis).
3. Server sends back a `JSESSIONID` cookie.
4. Every request sends this cookie, server looks up session in memory.

**Problems with sessions at scale:**
- If you have 3 servers behind a load balancer, the session on Server A is not known to Server B.
- You need a shared session store (Redis), adding complexity.
- Every request requires a DB/cache lookup just to verify identity.

**With JWT (stateless):**
- The server generates a token and sends it to the client.
- The server stores NOTHING about the session.
- Every request carries the token; the server just verifies the signature (pure CPU, no DB lookup).
- Works perfectly with horizontally-scaled microservices.

In expensora-api, `SessionCreationPolicy.STATELESS` tells Spring Security to never create or use an `HttpSession`. Each request is completely independent.

---

## 3. OAuth 2.0

### 3.1 What is OAuth?

OAuth 2.0 is an **authorization framework** (RFC 6749) that allows a third-party application to access a user's resources on another service **without the user giving away their password**.

The classic example everyone knows: **"Login with Google"** or **"Login with GitHub"**.

When you click "Login with Google" on an app like expensora-api:
- You are redirected to Google's login page.
- You log in to Google directly (expensora-api never sees your Google password).
- Google asks: "Do you allow expensora-api to access your email and profile?"
- You click Allow.
- Google redirects back to expensora-api with a special code.
- Expensora-api exchanges that code for your Google profile information.
- Expensora-api creates (or finds) your account and logs you in.

---

### 3.2 Key Roles in OAuth

| Role | Who it is in the Google Login example |
|---|---|
| **Resource Owner** | You (the user) |
| **Client** | Expensora-API (the app wanting access) |
| **Authorization Server** | Google's auth server (`accounts.google.com`) |
| **Resource Server** | Google's API (`googleapis.com`) that holds your profile data |

---

### 3.3 OAuth 2.0 Grant Types (Flows)

OAuth 2.0 defines several "grant types" for different scenarios:

#### 1. Authorization Code (Most Common — Web Apps)
Used when: Your app has a backend server.
Example: Expensora-API wanting Google Login.

```
User → clicks "Login with Google" on Expensora
     → redirected to: accounts.google.com/o/oauth2/auth
       ?client_id=EXPENSORA_CLIENT_ID
       &redirect_uri=https://expensora.app/auth/callback
       &response_type=code
       &scope=email profile
User → logs in at Google, grants permission
     → redirected back to: https://expensora.app/auth/callback?code=AUTH_CODE_HERE
Expensora Backend → POST to accounts.google.com/token
                    { code: AUTH_CODE_HERE, client_secret: SECRET }
                  ← receives: { access_token, id_token, refresh_token }
Expensora Backend → GET googleapis.com/oauth2/v2/userinfo
                    Header: Authorization: Bearer access_token
                  ← receives: { email, name, picture }
Expensora → creates/finds user, issues its OWN JWT to the client
```

#### 2. Client Credentials (Machine to Machine)
Used when: No user is involved. One service calls another.
Example: An automated report generator service calling expensora-api.

```
Service A → POST https://auth-server.com/token
            { grant_type: client_credentials, client_id: X, client_secret: Y }
          ← { access_token: "eyJ..." }
Service A → GET https://expensora.api/internal/reports
            Authorization: Bearer eyJ...
```

#### 3. Implicit (Deprecated)
Was used by single-page apps in the browser, but is now deprecated due to security concerns. **Do not use.**

#### 4. Device Code
Used when: The device has no browser (e.g., Smart TV, CLI tools).
Example: Logging in to an app on a Apple TV using your phone.

---

### 3.4 Authorization Code Flow — Detailed

This is the most important flow to understand for interviews. Let's break it down step by step:

```
Step 1: Authorization Request
─────────────────────────────
CLIENT ──────────────────────────────────────────► AUTHORIZATION SERVER
 (Expensora)                                           (Google)

GET https://accounts.google.com/o/oauth2/auth
    ?response_type=code
    &client_id=123456.apps.googleusercontent.com    ← who is asking
    &redirect_uri=https://expensora.app/callback    ← where to return
    &scope=email profile                            ← what access is needed
    &state=RANDOM_CSRF_TOKEN                        ← prevents CSRF attacks


Step 2: User Authenticates & Grants Permission
───────────────────────────────────────────────
Google shows login page → user logs in → user clicks "Allow"


Step 3: Authorization Code Returned
────────────────────────────────────
AUTHORIZATION SERVER ────────────────────────────────────────► CLIENT
       (Google)                                              (Expensora)

Redirects to: https://expensora.app/callback
              ?code=4/0AfUuAbcXYZ123         ← single-use, short-lived code
              &state=RANDOM_CSRF_TOKEN       ← Expensora must verify this matches


Step 4: Token Exchange (Backend — never exposed to browser)
──────────────────────────────────────────────────────────
CLIENT ──────────────────────────────────────────► AUTHORIZATION SERVER
 (Expensora Backend)                                    (Google)

POST https://accounts.google.com/token
Body: {
  grant_type: "authorization_code",
  code: "4/0AfUuAbcXYZ123",
  redirect_uri: "https://expensora.app/callback",
  client_id: "123456.apps.googleusercontent.com",
  client_secret: "SUPER_SECRET"                 ← only backend knows this
}


Step 5: Token Response
──────────────────────
AUTHORIZATION SERVER ────────────────────────────────────────► CLIENT
       (Google)                                              (Expensora)

{
  "access_token": "ya29.a0...",        ← to access Google APIs
  "id_token": "eyJhbGciOiJSUzI1N...", ← JWT containing user's Google info
  "refresh_token": "1//06...",
  "expires_in": 3600
}


Step 6: Get User Profile
────────────────────────
CLIENT ──────────────────────────────────────────► RESOURCE SERVER
 (Expensora Backend)                                (Google APIs)

GET https://www.googleapis.com/oauth2/v2/userinfo
Authorization: Bearer ya29.a0...

Response: {
  "email": "user@gmail.com",
  "name": "John Doe",
  "picture": "https://..."
}


Step 7: Issue Expensora's Own JWT
──────────────────────────────────
Expensora finds or creates user in its own DB with the Google email.
Then calls JwtUtil.generateToken(email) — the same JWT flow we use today.
Returns Expensora's OWN access token back to the frontend.
```

---

### 3.5 JWT vs OAuth — Are They Different Things?

This is one of the most common misconceptions. They are **not competing technologies**:

| JWT | OAuth 2.0 |
|---|---|
| A **token format** — defines how to structure and sign a token | An **authorization framework** — defines flows for granting access |
| Answers: "What does this token look like and how is it verified?" | Answers: "How does a client get permission to access a resource?" |
| Expensora uses JWT as the format for its own tokens | Google uses OAuth 2.0 as the protocol for "Login with Google" |
| Can be used without OAuth | OAuth often uses JWT as the token format |

**The relationship:** OAuth 2.0 often uses JWT tokens internally. When Google returns an `id_token` in Step 5 above, that IS a JWT. The two technologies are complementary.

**What expensora-api uses today:** Custom JWT authentication — no OAuth. It handles login/password itself and issues its own JWTs. OAuth would only be needed if you wanted social login (Google, GitHub) or if expensora-api needed to call third-party APIs on behalf of users.

---

### 3.6 How Expensora-API Could Add Google OAuth

Here is how you would add "Login with Google" to expensora-api using Spring Security OAuth2:

**1. Add dependency to `pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**2. Add Google credentials to `application.properties`:**
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=email,profile
```

**3. Add a new OAuth2 callback endpoint to `AuthController.java`:**
```java
@GetMapping("/oauth2/callback/google")
public ResponseEntity<?> googleCallback(@AuthenticationPrincipal OAuth2User oauthUser) {
    String email = oauthUser.getAttribute("email");
    String name = oauthUser.getAttribute("name");

    // Find existing user or create new one
    User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setRole(Role.USER);
                newUser.setPassword("");   // no password for OAuth users
                return userRepository.save(newUser);
            });

    // Issue expensora's own JWT — same as regular login
    String token = jwtUtil.generateToken(user.getEmail());
    String refreshToken = userService.generateRefreshToken(user.getEmail());
    return ResponseEntity.ok(new AuthResponseDto(token, refreshToken));
}
```

**4. Update `SecurityConfig.java`:**
```java
http
    // ... existing config ...
    .oauth2Login(oauth2 -> oauth2
        .defaultSuccessUrl("/auth/oauth2/callback/google", true)
    );
```

After this, Google handles all login complexity — expensora-api still issues its own JWT at the end, so the rest of the app works exactly the same.

---

## 4. Security Best Practices Used in Expensora-API

| Practice | Where it is in the code | Why it matters |
|---|---|---|
| **BCrypt password hashing** | `AppConfig.java` — `BCryptPasswordEncoder` | Bcrypt is a slow, salted hash. Even if the DB is stolen, passwords cannot be reversed in reasonable time. |
| **Short-lived access tokens** | `jwt.expiration=36000000` (10h) | Limits damage if a token is stolen. |
| **Refresh token stored in DB** | `User.refreshToken` field | Enables token revocation on logout. |
| **Secrets in environment variables** | `${JWT_SECRET}`, not hardcoded | Prevents accidental credential leaks in Git. |
| **CSRF disabled (correct for JWT)** | `csrf.disable()` in `SecurityConfig` | CSRF attacks only work with cookies. JWT in headers is not vulnerable to CSRF. |
| **CORS configured** | `CorsConfigurationSource` bean | Prevents unauthorized domains from calling the API. |
| **Stateless sessions** | `SessionCreationPolicy.STATELESS` | Prevents session fixation attacks. |
| **Specific error messages for expired vs invalid tokens** | `JwtAuthenticationFilter` — separate catch blocks | Helps the client know whether to try refreshing or force re-login. |
| **Public endpoints explicitly listed** | `.requestMatchers("/auth/register"...).permitAll()` | "Deny by default" — everything is protected unless explicitly allowed. |
| **256-bit minimum secret key length** | `Keys.hmacShaKeyFor(...)` | HS256 requires at least 256 bits of key material. Using a short key is cryptographically weak. |

---

## 5. Interview Questions — 2 Years Experience Level

These are the kinds of questions you will face in interviews at this experience level, along with detailed answers you can give based on expensora-api.

---

### Q1. What is a JWT and what are its three parts?

**Answer:**

JWT stands for JSON Web Token. It is an open standard (RFC 7519) for representing claims securely between two parties. It has three Base64Url-encoded parts separated by dots.

1. **Header** — contains the algorithm (e.g., `HS256`) and token type (`JWT`).
2. **Payload** — contains the claims. In my expensora-api project, I store the user's email in the `sub` (subject) claim, plus `iat` (issued at) and `exp` (expiration).
3. **Signature** — `HMACSHA256(base64(header) + "." + base64(payload), secret_key)`. This ensures the token was not tampered with.

The payload is NOT encrypted — it is just Base64 encoded, so anyone can read it. The signature only guarantees integrity, not confidentiality.

---

### Q2. What is the difference between an access token and a refresh token?

**Answer:**

In expensora-api:
- **Access token** — short-lived (10 hours). Sent in the `Authorization: Bearer` header on every API request. The server validates it by verifying the signature — no DB lookup needed. Cannot be revoked early.
- **Refresh token** — long-lived (7 days). Sent only to `/auth/refresh`. It is stored in the `users` table in the database (`User.refreshToken` field). When the user logs out, we set it to NULL, which revokes it.

The reason for two tokens is a trade-off: access tokens are stateless and fast, but cannot be revoked. Refresh tokens are stateful (stored in DB), which allows revocation, but they are used rarely (only when the access token expires), so the DB overhead is minimal.

---

### Q3. How does JwtAuthenticationFilter work?

**Answer:**

`JwtAuthenticationFilter` extends `OncePerRequestFilter`, meaning it runs exactly once for every request. The logic is:

1. Read the `Authorization` header.
2. If it starts with `Bearer `, extract the token.
3. Call `JwtUtil.extractUsername()` to get the email from the `sub` claim.
4. Load the `UserDetails` from the DB using `UserDetailsService.loadUserByUsername()`.
5. Call `JwtUtil.validateToken()` to check the username matches and the token is not expired.
6. If valid, create a `UsernamePasswordAuthenticationToken` and set it in the `SecurityContextHolder`.
7. Call `filterChain.doFilter()` to pass the request to the next filter or controller.

If the token is expired or invalid, the filter writes a JSON error response directly and returns without calling `filterChain.doFilter()`, so no controller is ever reached.

---

### Q4. Why is CSRF disabled in your SecurityConfig?

**Answer:**

CSRF (Cross-Site Request Forgery) attacks work by tricking a user's browser into sending a request to a site where the user is already logged in. This works because browsers automatically send cookies with requests.

In expensora-api, authentication uses JWT in the `Authorization` header, not cookies. Browsers do not automatically send headers with cross-site requests, so CSRF attacks are not possible. Therefore, Spring Security's CSRF protection (which adds and validates a CSRF token) is unnecessary and can be safely disabled with `csrf.disable()`.

---

### Q5. What does `SessionCreationPolicy.STATELESS` do?

**Answer:**

It tells Spring Security to never create an `HttpSession` and never use an existing `HttpSession` to retrieve the security context. Each request must provide the JWT — the server stores nothing between requests. This makes the API stateless and horizontally scalable (any server instance can handle any request because there is no shared session state).

---

### Q6. What is the difference between OAuth 2.0 and JWT?

**Answer:**

They solve different problems. JWT is a **token format** — it defines how to structure, sign, and verify a token. OAuth 2.0 is an **authorization framework** — it defines protocols for how an application gets permission to access resources on behalf of a user.

They are complementary: OAuth 2.0 flows often produce JWT tokens. For example, when Google returns an `id_token` in the OAuth2 Authorization Code flow, that id_token is a JWT.

Expensora-API currently uses custom JWT authentication (no OAuth). If we added "Login with Google", that would be OAuth 2.0, but at the end of the OAuth flow, we would still issue our own JWT to the frontend.

---

### Q7. What are OAuth 2.0 grant types? Which one would you use for a web app?

**Answer:**

OAuth 2.0 defines four main grant types:

1. **Authorization Code** — for web apps with a backend server. The client gets a short-lived code, exchanges it for a token on the backend. This is the most secure and most common. This is what I would use for expensora-api to add Google Login.
2. **Client Credentials** — for machine-to-machine communication with no user involved. One service authenticates itself to another.
3. **Implicit** — deprecated. Was used by SPAs but is no longer recommended because the token was exposed in the URL.
4. **Device Code** — for devices without a browser (Smart TVs, CLI tools).

For a web app like expensora, I would use **Authorization Code** with PKCE (Proof Key for Code Exchange) as an additional security measure.

---

### Q8. What is PKCE and why is it needed?

**Answer:**

PKCE (Proof Key for Code Exchange, pronounced "pixy") is an extension to the Authorization Code flow. It prevents **authorization code interception attacks**.

Without PKCE, a malicious app on the same device could intercept the authorization code from the redirect URL and exchange it for a token before the legitimate app does.

With PKCE:
1. The client generates a random `code_verifier`.
2. It hashes it to create a `code_challenge` and sends it with the authorization request.
3. After receiving the code, the client sends the original `code_verifier` in the token exchange.
4. The auth server verifies `hash(code_verifier) == code_challenge`. Only the app that started the flow can complete it.

It is now required for public clients (mobile apps, SPAs) and recommended for all OAuth flows.

---

### Q9. How would you handle token expiry gracefully on the client side?

**Answer:**

The standard approach is:

1. The client makes an API request and receives a `401 Unauthorized` response.
2. In expensora-api, we return a specific JSON body: `{ "error": "Token Expired", "message": "Your session has expired..." }`.
3. The client catches this and automatically calls `POST /auth/refresh` with the refresh token.
4. `/auth/refresh` returns a new access token.
5. The client retries the original failed request with the new access token.

This flow is typically implemented using an HTTP interceptor (in Angular: `HttpInterceptor`, in React: Axios interceptors). The user never sees the token expiry — the refresh happens seamlessly in the background.

If the refresh token itself is expired, the server returns an error for that too, and the client redirects the user to the login page.

---

### Q10. If a JWT cannot be revoked, how do you log a user out?

**Answer:**

You are right that a stateless JWT cannot be revoked before its expiry. This is a known trade-off. There are three approaches:

1. **Short expiry + Refresh token revocation** — what expensora-api does. Access tokens expire in 10 hours. On logout, the refresh token in the DB is set to NULL. The user cannot get a new access token after logout. The old access token still works for up to 10 hours, but you accept this risk by keeping the window small.

2. **Token blacklist** — store revoked JWTs in a Redis cache keyed by their `jti` (JWT ID) claim until they naturally expire. Every request checks if the token's `jti` is in the blacklist. This adds a Redis lookup to every request but achieves instant revocation. It partially re-introduces statefulness.

3. **Very short expiry** — use 5–15 minute access token lifetimes. Any "revoked" token expires so quickly it is acceptable. The client must refresh frequently.

For expensora-api, approach 1 is the right balance for a personal finance app.

---

### Q11. What is the difference between `authentication` and `authorization` in Spring Security?

**Answer:**

- **Authentication** is handled by `AuthenticationManager` and `UserDetailsService`. It answers "who are you?" by verifying credentials. In `JwtAuthenticationFilter`, once the token is validated, we call `userDetailsService.loadUserByUsername()` to load the user and create an `Authentication` object in the `SecurityContext`.

- **Authorization** is handled by the rules in `SecurityConfig.filterChain()`. The `.authorizeHttpRequests()` block answers "can you do this?" — for example, `.anyRequest().authenticated()` means every request not explicitly permitted requires a valid authentication in the `SecurityContext`.

The filter runs authentication → then Spring Security checks authorization → then the controller runs.

---

### Q12. What is the `SecurityContextHolder` and why is it thread-local?

**Answer:**

`SecurityContextHolder` is Spring Security's mechanism to store the current user's authentication information. By default, it uses a `ThreadLocal` strategy, meaning each thread has its own separate copy.

In a web application, each incoming HTTP request is handled by one thread (with traditional blocking servers like Tomcat). The request lifecycle is:

1. Thread picks up request.
2. `JwtAuthenticationFilter` sets `SecurityContextHolder.getContext().setAuthentication(authToken)`.
3. Controller runs on the same thread, reads `SecurityContextHolder.getContext().getAuthentication()`.
4. Response is sent.
5. Spring Security clears the `SecurityContext` after the request — important to prevent bleeding between requests.

Using `ThreadLocal` means you never need to pass the authenticated user around as a parameter — any code running in the same thread can access it directly. This is why `AuthController.getCurrentUser()` can just call `SecurityContextHolder.getContext().getAuthentication().getName()` without receiving the user as a parameter from the filter.

---

### Q13. What hashing algorithm is used for passwords in expensora-api, and why not MD5 or SHA-256?

**Answer:**

Expensora-api uses **BCrypt** via Spring Security's `BCryptPasswordEncoder` (configured in `AppConfig.java`).

MD5 and SHA-256 are fast hashing algorithms — a modern GPU can compute billions of MD5 hashes per second, making brute-force attacks feasible.

BCrypt is intentionally slow. It includes a **cost factor** (work factor) — you can configure how many iterations to perform. The default in Spring's `BCryptPasswordEncoder` is 10 (2^10 = 1024 rounds). This makes BCrypt ~1000x slower than SHA-256 for an attacker. As hardware gets faster, you increase the cost factor.

BCrypt also automatically generates a **random salt** per password and stores it within the hash string itself, so two users with the same password will have completely different hashes. This prevents rainbow table attacks.

---

### Q14. What would happen if someone stole the JWT secret key?

**Answer:**

If the JWT secret is compromised, an attacker can:
1. Forge any JWT token for any user without knowing their password.
2. Read any existing token's payload (though it is already readable without the secret).
3. Sign new tokens that appear valid to the server.

The immediate response should be:
1. Rotate the secret — change `JWT_SECRET` in the environment variables and redeploy.
2. All existing tokens become invalid immediately (because they were signed with the old secret), effectively logging out all users.
3. Users will need to log in again with their credentials to get tokens signed with the new secret.

This is why the secret must never be committed to Git (expensora-api correctly uses `${JWT_SECRET}` environment variable) and must be a cryptographically random value of at least 256 bits (as noted in the `application.properties.example` comments: `openssl rand -hex 32`).

---

*End of guide — built specifically from the expensora-api source code.*
