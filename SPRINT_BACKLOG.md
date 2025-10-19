 Music Store — 4-Sprint Backlog

Overview
- Goal: Deliver a shippable, production-ready Music Store incrementally over 4 two-week sprints. Focus in early sprints is stability and core customer flows; later sprints add admin/analytics improvements, artist features, polishing, and technical debt cleanup.

Assumptions
- Sprint length: 2 weeks (10 working days).
- Team: 3 developers + 1 QA + 1 product owner (used for capacity/estimates).
- Estimates use story points (1, 2, 3, 5, 8). 1 point ≈ 1 developer-day of work for planning purposes.
- Environment: Java Spring Boot app (Maven); DB is relational; JWT auth already present.
- Priority: Fix critical bugs (BigDecimal/Repo methods, serialization loops) first to avoid runtime failures.

How to use this backlog
- Each sprint contains Epics → User Stories → Tasks with estimates and acceptance criteria.
- Dependencies and risks are listed per story where applicable.

========================
Sprint 1 (Weeks 1-2) — Stabilize & Core Customer Flow (Priority: Critical fixes + Browse & Auth)
Goal: Stop regressions, fix critical bugs, make authentication, music browsing, and playback/download flows stable.

Velocity target: 20–25 story points

Epics / Stories

1. Critical bug fixes and repo/type safety (8 pts)
- Tasks:
  - Audit and add missing repository methods used by services (orderRepository.sumTotal... etc.) (3 pts)
  - Ensure monetary repository methods return BigDecimal; fix service conversions (2 pts)
  - Fix serialization circular references (validate @JsonIgnore and DTOs) and run integration sanity checks (2 pts)
  - Unit tests for changed repository methods and BigDecimal conversions (1 pt)
- Acceptance criteria:
  - App starts locally; no ClassCastException from repository returns
  - BigDecimal precision preserved in order/analytics endpoints
  - No JSON infinite recursion on endpoints used by smoke tests
- Dependencies: DB schema migration might be required
- Risks: DB schema mismatch if migrations aren't applied

2. Authentication & Authorization hardening (3 pts)
- Tasks:
  - Review `JwtUtil` & `JwtAuthenticationFilter` flows for expired token handling (1 pt)
  - Add explicit error messages & status codes for auth failures (1 pt)
  - Add integration test for login and one protected endpoint (1 pt)
- Acceptance criteria:
  - Login works; protected endpoints return 401/403 clearly

3. Core browsing & music metadata endpoints (3 pts)
- Tasks:
  - Test and fix `GET /api/music` and `GET /api/music/{id}` endpoints for correct DTO output (1 pt)
  - Ensure static resource serving (music files) works via `StaticResourceConfig` and test direct playback/download (1 pt)
  - Add a small smoke test script to fetch a music file (1 pt)
- Acceptance criteria:
  - Clients can list music and fetch metadata and download a sample file

4. Basic UI/test smoke & CI checks (4 pts)
- Tasks:
  - Add or update simple integration smoke tests (login -> list music -> add to cart) (3 pts)
  - Fix any test failures discovered (1 pt)
- Acceptance criteria:
  - Smoke test passes locally

Sprint 1 Done definition
- All critical runtime errors fixed
- Authentication stable with meaningful errors
- Customers can browse and download sample music
- Smoke tests pass locally

========================
Sprint 2 (Weeks 3-4) — Customer Checkout, Cart, Orders, and Support Tickets
Goal: Complete purchase flow and make support ticket chat robust and tested.

Velocity target: 20–25 story points

Epics / Stories

1. Cart & Checkout (8 pts)
- Tasks:
  - Harden `CartService` and `OrderService` flows; add validation and transaction boundaries (3 pts)
  - Implement or fix `OrderRepository` methods referenced by services, and ensure totals use BigDecimal (2 pts)
  - Add checkout integration test (create cart, checkout, create order record) (2 pts)
  - Add audit logging for orders (1 pt)
- Acceptance criteria:
  - End-to-end checkout flow works locally and order records are consistent

2. Payment integration stub & refund endpoint (5 pts)
- Tasks:
  - Implement a payment gateway adapter interface and a local mock/stub implementation for testing (2 pts)
  - Implement `PUT /api/admin/orders/{orderId}/refund` with audit trail (2 pts)
  - Tests for refund flow (1 pt)
- Acceptance criteria:
  - Payments can be simulated; refunds update order status and create audit log

3. Chat-based Ticket support polish (6 pts)
- Tasks:
  - Verify the ticket endpoints: create ticket, post messages, get conversation (2 pts)
  - Add server-side validation on message payloads and size limits (1 pt)
  - Add unit/integration tests for conversation retrieval and posting messages (2 pts)
  - Add basic rate-limiting safeguard on ticket message posting (1 pt)
- Acceptance criteria:
  - Tickets conversation is complete and consistent; messages persist correctly and are retrievable

4. Monitoring & logging improvements (3 pts)
- Tasks:
  - Ensure `AuditLog` entries are created for key actions (login, checkout, refund) (2 pts)
  - Add a simple health-check endpoint for deployment (1 pt)
- Acceptance criteria:
  - Audit logs created; health-check returns 200

Sprint 2 Done definition
- Reliable checkout and refund flows with tests
- Ticket chat is stable and validated
- Audit logs for key operations present

========================
Sprint 3 (Weeks 5-6) — Admin Features, Analytics, Content Moderation
Goal: Provide admin management and analytics panels, complete content flagging & moderation.

Velocity target: 20 story points

Epics / Stories

1. Admin user & content management (6 pts)
- Tasks:
  - Implement/verify `POST /api/admin/users/create`, `GET /api/admin/users`, update, delete, status toggle (3 pts)
  - Add validation, and admin audit logs on user changes (1 pt)
  - Add tests for admin user flows (2 pts)
- Acceptance criteria:
  - Admins can create, list (paged), update, disable/enable users; changes audited

2. Flagging & content moderation (6 pts)
- Tasks:
  - Implement customer-facing flagging endpoint `POST /api/music/{id}/flag` (2 pts)
  - Implement admin endpoints to list flagged music and unflag/delete (2 pts)
  - Convert flaggedByCustomerId to proper FK relation (or document schema migration) (1 pt)
  - Tests for flagging/unflagging flows (1 pt)
- Acceptance criteria:
  - Flagging persists metadata (when/who/reason); admin can act on flagged items

3. Admin analytics & reports (5 pts)
- Tasks:
  - Implement `GET /api/admin/analytics/overview` and basic `detailed` endpoints returning real counts (2 pts)
  - Fix placeholder analytics methods in services (2 pts)
  - Add an endpoint to generate a simple CSV report for sales (1 pt)
- Acceptance criteria:
  - Admin analytics return real data from DB and match sample queries

4. Security hardening for dangerous admin actions (3 pts)
- Tasks:
  - Lockdown server shutdown behind an environment flag + confirm param + audit log (2 pts)
  - Add role checks and logging for refund and system management endpoints (1 pt)
- Acceptance criteria:
  - Server shutdown endpoint cannot be invoked in production-like environments without explicit confirmation

Sprint 3 Done definition
- Admin can moderate content and manage users
- Analytics endpoints serve real data
- Dangerous admin operations are guarded and audited

========================
Sprint 4 (Weeks 7-8) — Artist features, polishing, tests, deployment readiness
Goal: Add artist flows, playlists, reviews polishing, final QA, and deployment prep.

Velocity target: 20 story points

Epics / Stories

1. Artist portal & upload flow (6 pts)
- Tasks:
  - Artist registration flows & profile management (2 pts)
  - Music upload flow (multipart) using `FileStorageService` (2 pts)
  - Server-side validation of uploads (file size/type), store metadata (1 pt)
  - Tests for upload and retrieval (1 pt)
- Acceptance criteria:
  - Artists can upload tracks; uploaded files are stored and accessible via metadata endpoints

2. Playlists & recommendations (5 pts)
- Tasks:
  - Finalize `PlaylistService` and `PlaylistApiController` for create/update/delete and add/remove tracks (2 pts)
  - Add endpoint to fetch playlists with tracks (1 pt)
  - Add basic recommendation/stub (e.g., top-rated or recent) for discovery (2 pts)
- Acceptance criteria:
  - Playlists fully functional and discoverable

3. Reviews moderation and rating recalculation (4 pts)
- Tasks:
  - Ensure deleting a review triggers rating recalculation and is audited (2 pts)
  - Add admin endpoints for review moderation if not already done (1 pt)
  - Tests for rating recalculation path (1 pt)
- Acceptance criteria:
  - Ratings updated correctly after review deletion or change

4. Final QA, tests, and deployment checklist (5 pts)
- Tasks:
  - Add/expand integration tests, unit tests to achieve target coverage for critical modules (2 pts)
  - Create deployment checklist / README with env vars and migration steps (1 pt)
  - Run full local build & smoke tests, fix issues (2 pts)
- Acceptance criteria:
  - Build succeeds; tests pass; README contains deployment steps

Sprint 4 Done definition
- Artist uploads, playlists, review moderation done
- Codebase polished, tested, and documented for deployment

========================
Backlog (Unscheduled / Future)
- Full payment gateway integration (Stripe/PayPal) and PCI considerations
- Advanced analytics (charts, cohort analysis)
- Search improvements (Elasticsearch) and recommendation engine
- Rate limiting & abuse prevention across endpoints
- Migration scripts for DB changes (add columns, FK relations)
- Performance tuning and caching for heavy queries

Risks & Mitigations
- DB schema mismatches: add migration scripts and test on staging before production
- Sensitive admin actions: require multi-step confirmations and audit logs
- Monetary precision: ensure BigDecimal everywhere for money fields

Appendix — Mapping of known critical issues to backlog
- Missing repo methods and BigDecimal type inconsistencies → Sprint 1 (Critical fixes story)
- Ticket circular references and JSON infinite recursion → Sprint 1 (serialization fixes)
- Flagging FK and naming inconsistency → Sprint 3 (flagging & FK conversion story)

Contact / Next steps
- If you want, I can:
  - Convert this backlog into JIRA-friendly CSV or JSON with labels and components
  - Break high-priority Sprint 1 stories into GitHub issues with checklist tasks and assign estimates


---
Generated: 2025-10-04

