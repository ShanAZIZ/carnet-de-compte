# Workflow & Tooling

## Source of Truth

- ALWAYS check the official Angular documentation first (angular.dev) before applying any pattern. Use the `angular-cli` MCP server (`search_documentation`, `get_best_practices`) rather than memory.
- Blog posts, Stack Overflow, and older Angular idioms are NOT authoritative. If a rule file conflicts with angular.dev, angular.dev wins — say so and update the rule file.

## Scaffolding: always the Angular CLI

NEVER hand-write a file the CLI can generate. Run `ng generate` (preferably through the `angular-cli` MCP `run_target`) so naming, suffixes, and conventions match the official schematics:

| Need | Command |
| --- | --- |
| Component | `ng g component <feature>/<name>` |
| Service | `ng g service <feature>/<name>` |
| Directive / Pipe | `ng g directive <path>` / `ng g pipe <path>` |
| Guard / Interceptor / Resolver | `ng g guard <path>` / `ng g interceptor <path>` / `ng g resolver <path>` |
| Dependency or lib setup | `ng add <package>` |
| Angular upgrade / API migration | `ng update` / `ng g @angular/core:<migration>` |

- Run it from the workspace root and pass the path so the file lands in the feature directory, not `src/app/`.
- Use `--dry-run` first when unsure where files will land; `--skip-tests` only when a spec genuinely makes no sense.
- Only edit generated files afterwards — never recreate their boilerplate by hand, and never rename away the CLI's naming.

## Tooling

- Linter: `angular-eslint` (`eslint.config.js`). Run `npm run lint` before declaring work done; fix findings, do not disable rules without a comment explaining why.
- Formatter: `prettier`. No manual style debates.
- Tests: `vitest` through the Angular `unit-test` builder — run with `npm test`. Do NOT add Karma, Jasmine, or Jest.
- Keep `*.spec.ts` next to the file it tests.
- No new dependency for what a few lines of stdlib, native platform, or an already-installed package covers.
