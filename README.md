# AI-Ops-Assistant

A conversational, multi-tenant assistant that lets engineers ask natural-language questions about system health — *"why did service X spike at 2am?"*, *"show me the error rate for the last hour"* — and get back a plain-English answer backed by real metric queries and inline charts.

**🔗 Live demo:** `Work in Progress`
**🔗 API docs:** `Work in Progress/swagger-ui.html` (if you enable springdoc-openapi)

> Note: the backend runs on Render's free tier and spins down after ~15 minutes of inactivity. The first request after idle time may take 30-50 seconds to wake up — please be patient on first load.

---

## Problem

Engineers lose time context-switching between dashboards, log aggregators, and metrics tools just to answer simple operational questions. This project explores whether an LLM agent — given structured, guardrailed access to the same telemetry a human would query — can shortcut that process into a single conversational interface, without hallucinating numbers it never actually retrieved.

## Architecture

```
┌─────────────┐      ┌──────────────────────┐      ┌─────────────┐
│  Dashboard  │ HTTP │   Spring Boot API     │      │  PostgreSQL  │
│ (static UI) │◄────►│  ─────────────────    │◄────►│  (metrics,  │
│             │      │  Auth · REST · Agent  │      │   users)    │
└─────────────┘      │  Orchestration Layer  │      └─────────────┘
                      │                       │
                      │  ┌─────────────────┐  │      ┌─────────────┐
                      │  │ Function-calling│──┼─────►│    Redis    │
                      │  │   Tool Registry │  │      │  (Upstash)  │
                      │  └────────┬────────┘  │      └─────────────┘
                      │           │           │
                      └───────────┼───────────┘
                                  ▼
                          ┌───────────────┐
                          │   LLM API     │
                          │ (OpenAI/Groq) │
                          └───────────────┘
```

**Flow:** User asks a question → backend sends it to the LLM with a registry of callable tools (`getServiceMetrics`, `getErrorLogs`, `compareServices`) → LLM decides which tool(s) to call → backend validates parameters and executes against Postgres → results go back to the LLM → LLM composes a natural-language summary → frontend renders the summary plus an inline chart.

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA | REST APIs, auth, ORM |
| Agent orchestration | OpenAI function calling (or MCP server) | Structured, auditable tool invocation instead of free-form prompting |
| Database | PostgreSQL (Neon) | Stores synthetic metrics + user/tenant data |
| Cache | Redis (Upstash) | Caches repeated metric queries, cuts LLM/DB round-trips |
| Dashboard | Spring Boot static resources | Lightweight operational view |
| Observability | Spring Boot Actuator | Exposes health/metrics endpoints for the app itself |
| Hosting | Render (backend), Vercel (frontend), Neon (DB), Upstash (Redis) | Free tiers, GitHub auto-deploy |
| CI | GitHub Actions | Runs `mvn test` on every push |

## Key Design Decisions

- **Synthetic telemetry, clearly labeled.** There's no real production infra to monitor here, so a scheduled job generates realistic time-series data (CPU%, latency, error rate) for a handful of mock services. This is stated explicitly rather than implied to be real — the point of the project is the agent/orchestration layer, not the data source.
- **Guardrailed tool-calling, not open-ended prompting.** Every LLM-requested function call is validated against known service names and allowed time ranges before it touches the database. This mirrors how you'd actually want an AI agent to interact with production systems — never trust model output as executable input without validation.
- **Caching layer.** Repeated or similar queries within a short window are served from Redis rather than re-querying Postgres or re-invoking the LLM, cutting latency and cost.
- **Multi-tenancy.** Users are scoped to the services/data they're authorized to see, via basic JWT-based auth — a deliberately simple but real implementation of tenant isolation.

## Evaluation

| Question | Expected tool call | Correct? | Notes |
|---|---|---|---|
| "What's the error rate for checkout-service in the last hour?" | `getServiceMetrics` | ✅ | |
| "Compare latency between auth-service and checkout-service" | `compareServices` | ✅ | |
| "Why is payment-service down?" (no such service) | — should decline gracefully | ✅ | Confirms guardrails reject unknown service names |

## Running Locally

```bash
# Backend and dashboard
cd backend
bash ./app.sh start

# Check or stop it later
bash ./app.sh status
bash ./app.sh stop

Open http://localhost:8080 after starting the backend.

The script requires Bash and Maven, so on Windows use Git Bash or WSL.
```

Requires: Java 17+, Node 18+, a Postgres instance (local Docker or Neon), an OpenAI (or Groq) API key.

## Known Limitations

- Metrics are synthetic, not pulled from real infrastructure — this is a portfolio demo of the agent/orchestration pattern, not a production monitoring tool.
- Free-tier Render backend cold-starts after inactivity (~30-50s first request).
- Single LLM provider currently wired in; no automatic fallback if the provider is rate-limited.

## Roadmap / What I'd Add Next

- Real Prometheus/Grafana data source integration instead of synthetic data.
- Streaming responses (token-by-token) instead of waiting for the full LLM completion.
- Expose the tool registry as an actual MCP server so any MCP-compatible client can use it, not just this frontend.

---

**Author:** Hrutu Surve