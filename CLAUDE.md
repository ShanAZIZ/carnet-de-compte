Carnet de compte — monorepo. Two independent projects, each with its own `CLAUDE.md` and its own rules in its `.claude/rules/`.

| Project | Stack | Read |
| --- | --- | --- |
| `cdc-frontend/` | Angular 20 + TypeScript + daisyUI | `cdc-frontend/CLAUDE.md` |
| `cdc-backend/` | Kotlin + Ktor + Gradle | `cdc-backend/CLAUDE.md` |

Always work from the directory of the project at hand: `npm`/`ng` in `cdc-frontend/`, `./gradlew` in `cdc-backend/`. Paths inside a project's rules are relative to that project.

## Common to both projects

- No new dependency for what the stdlib, the platform, or an already-installed package covers.
- No speculative abstraction: no interface with a single implementation, no layer "for later".
- One change = the shortest diff that works, placed where all callers route through.
- A comment is an admission the code is unreadable. Default: no comment. Fix the naming, the signature, the structure instead.
  The only survivors: a *why* the code physically cannot carry — an external constraint, a trade-off, a deliberate shortcut and its ceiling. One line, and only if it still holds after the rewrite.
  Never: a restatement, a section banner, a TODO, a framework convention, a commented-out block, a comment on self-evident code. Delete it, and delete the stale ones you walk past.
  Not comments, keep them: tool directives (`// @ts-check`, `@Suppress`, eslint-disable with its reason), KDoc/TSDoc on a public API consumed elsewhere.
- Lint and tests of the touched project pass before the work is declared done.
- Commit messages in English, imperative mood, one intent per commit.

A change that crosses both projects (API contract, data format) must be described on both sides: the endpoint in the backend, the type and the call in the frontend. Never change one side of a shared contract alone.
