# 🌱 Stage 3 — Spring Boot REST API
## The Job-Ready Milestone

> SmartBank — Java Backend Learning Path
> The BIGGEST stage. SmartBank transforms from a console app
> into a real REST API. This is where you become job-ready.
> Same SmartBank project — evolved, not replaced.

---

## THE TRANSFORMATION

```
BEFORE (Stage 1-2):          AFTER (Stage 3):
  console app                  REST API (web service)
  menu + keyboard input        HTTP endpoints
  runs in terminal             runs as a server
  one user                     many concurrent users
  text-file storage            (→ database in Stage 4)

  → other apps/frontends can call it over HTTP
  → this is what companies actually build
```

---

## THE 5 PARTS (~35 steps)

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART A — SPRING CORE (the foundation)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 1:  Spring Boot Intro & Project Setup
           → what Spring/Spring Boot is, starters, auto-config
           → convert SmartBank to a Spring Boot app

  Step 2:  Inversion of Control & Dependency Injection
           → the CORE concept — Spring's IoC container
           → your manual Main.java wiring → Spring does it
           → @Component, @Service, @Repository, @Autowired

  Step 3:  Beans, Scopes & Lifecycle
           → what a bean is, singleton vs prototype
           → @Bean, @Configuration, bean lifecycle

  Step 4:  Configuration & Properties
           → application.properties/yml
           → @Value, @ConfigurationProperties
           → externalized config

  Step 5:  Spring Profiles
           → dev / test / prod environments
           → @Profile, profile-specific configs

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART B — REST API (the heart of the stage)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 6:  REST Fundamentals
           → HTTP methods, status codes, REST principles
           → resources, statelessness, idempotency

  Step 7:  Controllers & Endpoints
           → @RestController, @GetMapping, @PostMapping, etc.
           → SmartBank endpoints: POST /accounts, GET /accounts/{id}

  Step 8:  Request Handling
           → @PathVariable, @RequestParam, @RequestBody
           → mapping HTTP → your service layer

  Step 9:  DTOs & Mapping
           → why you DON'T expose domain objects directly
           → request/response DTOs, mapping (MapStruct)
           → the DTO pattern (from Phase 6 preview)

  Step 10: Response Handling & Status Codes
           → ResponseEntity, proper status codes
           → 200/201/204/400/404/409...

  Step 11: Validation
           → @Valid, @NotNull, @Positive, custom validators
           → validating incoming requests

  Step 12: Exception Handling
           → @ExceptionHandler, @ControllerAdvice
           → global error handling, consistent error responses
           → mapping your BankExceptions → HTTP errors

  Step 13: API Design & Versioning
           → resource naming, versioning strategies
           → pagination, filtering, sorting

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART C — DATA & PERSISTENCE (Spring Data)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 14: Spring Data JPA Intro
           → JPA, Hibernate, entities, @Entity, @Id
           → object ↔ table mapping (builds on JDBC, Phase 6)

  Step 15: Repositories
           → JpaRepository, derived queries, @Query
           → replacing your AccountRepository with Spring Data

  Step 16: Entity Relationships
           → @OneToMany, @ManyToOne (accounts → transactions)
           → fetch types, cascade, the N+1 problem

  Step 17: Transactions
           → @Transactional (your transfer, done right)
           → propagation, isolation, rollback rules
           → connects to your Phase 5 concurrency knowledge

  Step 18: Database Migrations
           → Flyway / Liquibase — versioned schema changes
           → (full database setup lands in Stage 4)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART D — SECURITY (dense but essential)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 19: Spring Security Intro
           → the filter chain, authentication vs authorization
           → (Decorator pattern from Phase 4 — filters wrap!)

  Step 20: Authentication
           → users, passwords, encoding (BCrypt)
           → UserDetailsService

  Step 21: JWT (token-based auth)
           → stateless auth for REST APIs
           → issuing + validating tokens

  Step 22: Authorization
           → roles, @PreAuthorize, method security
           → protecting SmartBank endpoints (who can transfer?)

  Step 23: Security Best Practices
           → CORS, CSRF, secure headers, secrets management

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART E — QUALITY & DELIVERY (senior polish)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 24: Testing Spring Apps
           → @SpringBootTest, @WebMvcTest, MockMvc
           → test slices, @MockBean
           → testing controllers, services, repositories

  Step 25: API Documentation (OpenAPI/Swagger)
           → springdoc-openapi, Swagger UI
           → auto-generated interactive docs
           → (reflection + annotations — Phase 6 payoff)

  Step 26: Actuator & Observability
           → health checks, metrics, /actuator endpoints
           → readiness/liveness (matters for k8s in Stage 6)

  Step 27: Logging
           → structured logging, log levels, SLF4J/Logback
           → correlation IDs

  Step 28: Async & Performance
           → @Async, thread pools, virtual threads
           → (your Phase 5 concurrency, applied in Spring)
           → CompletableFuture for external calls

  Step 29: Containerize the Spring App
           → update the Dockerfile for Spring Boot
           → spring-boot-maven-plugin (replaces shade)
           → the fat JAR, Spring-optimized

  Step 30: Deploy the REST API
           → run it as a container, expose the port
           → docker compose (API + placeholder for DB)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART F — CONSOLIDATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Step 31: End-to-End Review
           → full request lifecycle: HTTP → controller →
             service → repository → response
  Step 32: API Polish & Best Practices
  Step 33: Portfolio Presentation (the API as a showpiece)
  Step 34: Stage 3 Interview (Spring Boot senior questions)
  Step 35: Stage 3 Review → ready for Stage 4 (real database)
```

---

## HOW YOUR PAST WORK PAYS OFF HERE

```
Phase 4 (SOLID/patterns):
  → DIP → Spring's dependency injection IS this
  → Decorator → Spring Security filters wrap each other
  → your patterns show up as Spring's core design

Phase 5 (concurrency):
  → @Transactional, isolation levels
  → @Async, virtual threads in Spring
  → many users hitting endpoints = the concurrency you learned

Phase 6 (expert concepts):
  → reflection → how @Autowired, @Component work
  → annotations → Spring is ALL annotations
  → JDBC → what Spring Data/Hibernate sit on top of
  → JSON → what @RequestBody/@ResponseBody do (Jackson)
  → the "magic" isn't magic — you know the machinery

Stage 2 (tooling):
  → Maven → Spring's pom, starters, BOM
  → Docker → containerize the API
  → Compose → API + database (Stage 4)
```

---

## THE MILESTONE

```
JOB-READY POINT:
  → roughly after Part B + basic Part C (REST API + data)
  → you can build a working, documented, tested REST API
  → THIS is enough to start applying for backend roles

SENIOR-QUALITY:
  → Parts D + E (security, testing, observability)
  → this is what pushes from "can build an API" to
    "builds production-grade services"

  → don't rush. Stage 3 is the highest-value stage.
  → ~6-10 weeks of real depth here defines your readiness.
```

---

## WHAT SMARTBANK BECOMES

```
By the end of Stage 3, SmartBank is:
  → a REST API with documented endpoints
  → validated requests, proper error responses
  → JPA persistence (ready for real DB in Stage 4)
  → secured with JWT + role-based access
  → tested (controllers, services, repositories)
  → documented (Swagger UI)
  → observable (actuator, logging)
  → containerized and runnable

  → a genuine, production-shaped backend service
  → the centerpiece of your portfolio
  → the deployable service your Terraform track (Stage 6)
    will provision infrastructure for
```

---

*Stage 3: Spring Boot. The big one. Where SmartBank becomes real
and you become job-ready. Take it slow, go deep — this is the
foundation of your senior backend career.*