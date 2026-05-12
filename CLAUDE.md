# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CoTales is a collaborative storytelling platform. This is the Spring Boot REST API backend supporting user management, story creation, character building, and token-based monetization.

## Build & Development Commands

```bash
# Run the application
mvn spring-boot:run

# Build (compile + test + format check)
mvn clean verify

# Build without format check
mvn clean install

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=CoTalesApiApplicationTests

# Apply code formatting (must pass before commits)
mvn spotless:apply

# Check formatting only
mvn spotless:check
```

**Pre-commit hook** automatically runs Spotless on staged `.java` files — run `mvn spotless:apply` before staging if you hit formatting errors.

## Environment Setup

Requires a PostgreSQL database. Set these environment variables (or use a `.env` file):

```
DATASOURCE_URL=jdbc:postgresql://localhost:5432/cotales
DATASOURCE_USERNAME=<user>
DATASOURCE_PASSWORD=<password>
JWT_SECRET=<HS256 key>
```

Flyway manages the schema — no manual DDL needed. Hibernate `ddl-auto` is set to `none`.

## Architecture

**Tech stack**: Java 21, Spring Boot 4.0.1, Spring Data JPA, Spring Security + OAuth2, PostgreSQL, Flyway, MapStruct, Lombok, SpringDoc OpenAPI.

**Package convention**: Feature-based (`com.cotales.cotales_api.<domain>.<subdomain>`). The `user` domain is split into `user/`, `account/`, and `profile/` subpackages.

**Base controller**: `common.ApiController` is an abstract class that sets the API path prefix — all REST controllers extend it. The prefix is configured via `api.prefix` in `application.yaml` (default `/api/v1`), so changing the version is a one-line config change. Child controllers declare only their resource path (e.g., `/users`).

**Entity patterns**:
- Soft deletes via `deletedAt` / `bannedAt` timestamps (no hard deletes)
- Audit fields `createdAt` / `updatedAt` on all entities
- `Account` and `Profile` use `@MapsId` to share the `User` primary key (one-to-one via shared PK, not a FK column)
- `Account` abstracts multi-provider auth — each row has a `provider` enum (GOOGLE, GITHUB, PASSWORD) and `providerUserId`

**DTO mapping**: Use MapStruct for all entity ↔ DTO conversions. Mapper interfaces go in the same feature package as their entity.

**Formatting**: Google Java Format (AOSP style, 4-space indent). Enforced by Spotless — never bypass with `--no-verify`.

## Database Schema

The full schema is in `src/main/resources/db/migration/`. Key domains beyond the current Java entities:

- **Stories & Paragraphs** — stories support continuation (chaining), paragraphs are ordered content units
- **Characters** — with inter-character relations and story assignments
- **Social** — follows, likes
- **Monetization** — token transactions, tips, payouts, story/paragraph unlocks
- **Roles** — user roles (UserRoles join table)

When adding a new entity, write a Flyway migration rather than relying on Hibernate schema generation.

## Infrastructure (Terraform)

All AWS infrastructure lives in `infra/`. Requires [Terraform >= 1.6](https://developer.hashicorp.com/terraform/install) and AWS CLI configured with an admin account.

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # set aws_region
terraform init
terraform apply

# Get values for GitHub secrets
terraform output
terraform output -raw ec2_private_key_pem > cotales.pem && chmod 600 cotales.pem
terraform output -raw github_actions_secret_access_key
```

**Architecture:** 1 EC2 t3.micro running Docker + PostgreSQL in Docker on the same instance. One server handles everything — no ALB, no RDS, no ECS. ~$8/month (free tier for 12 months).

**Server first-boot setup** (do once after `terraform apply`):
```bash
ssh -i cotales.pem ec2-user@<server_ip>
cp deploy/.env.example ~/.env   # fill in POSTGRES_PASSWORD, JWT_SECRET
# Copy docker-compose.yml (CI/CD does this automatically on every deploy)
```

**Scaling path (when ready):** migrate to ECS Fargate + RDS by re-enabling the removed Terraform files. The Dockerfile, ECR repo, and IAM setup are already in place — only the compute and database layers change.

## CI/CD

**Branch → deploy mapping:**
| Branch | Spring profile | Deploys? |
|--------|---------------|----------|
| `develop` | `dev` | Yes → same server |
| `main` | `prod` | Yes → same server |
| feature branches / PRs | — | CI only (no deploy) |

**GitHub repository secrets** (Settings → Secrets → Actions):

| Secret | Where to get it |
|--------|----------------|
| `AWS_ACCESS_KEY_ID` | `terraform output github_actions_access_key_id` |
| `AWS_SECRET_ACCESS_KEY` | `terraform output -raw github_actions_secret_access_key` |
| `AWS_REGION` | same value as `aws_region` in tfvars |
| `ECR_REPOSITORY` | `terraform output ecr_repository_name` |
| `EC2_HOST` | `terraform output server_ip` |
| `EC2_SSH_PRIVATE_KEY` | contents of `cotales.pem` (paste full PEM including header/footer) |

## API Documentation

SpringDoc generates OpenAPI docs at `/swagger-ui.html` when the app is running.
