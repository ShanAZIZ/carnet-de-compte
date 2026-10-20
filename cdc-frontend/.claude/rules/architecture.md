# Architecture & Code Structure

## File Organization

- Follow the official Angular style guide for naming: `my-thing.ts`, `my-thing.html`, `my-thing.css`, `my-thing.spec.ts` (no `.component`/`.service` suffixes in Angular v20+).
- One feature = one directory under `src/app/<feature>/`, lazy-loaded via its own routes file.
- Co-locate: a service, util, type, or pipe used by a single component lives in that component's directory. Promote it to `src/app/shared/` (or the feature root) only when a second consumer actually appears — never preemptively.
- `src/app/core/` for app-wide singletons (auth, http interceptors, app config), `src/app/shared/` for genuinely reused UI and utils.
- No barrel files (`index.ts`) — import from the real path.

## Member Order

Keep every file harmonised. Order inside a class:

1. `input()` / `model()` / `output()`
2. injected dependencies (`inject()`)
3. state signals (`signal`)
4. derived state (`computed`, `linkedSignal`)
5. lifecycle hooks / `effect`
6. public methods
7. private methods (`#private` or `private`)

## Quality

- Order inside a file: imports (external → `@angular/*` → internal), then types/constants, then the decorated class. One exported Angular symbol per file.
- Naming: `PascalCase` classes, `camelCase` members, `SCREAMING_SNAKE_CASE` module-level constants, booleans read as predicates (`isLoading`, `hasError`).
- Mark everything not used by the template as `private`/`#private`, and `readonly` whenever it is never reassigned.
- No comments that restate the code. Comment only the non-obvious *why*.
- A file that exceeds ~200 lines or a method that exceeds ~20 is a split signal, not a target.

## Forms (Signal Forms)

Order inside a component that owns a form — slots into the class order above (model signal and `form()` are state, so they stay in slots 3–4, before lifecycle):

1. form model signal — the raw data shape (`signal<LoginData>({ ... })`)
2. validation schema — `schema()` applied to the model, declared before the field tree
3. field tree — `form(model, schema)`, the only thing the template binds through
4. derived form state — `computed()` over the field tree (`canSubmit`, error summaries)
5. submit handler — `submit()` call, placed first among public methods
6. field-level helpers — private, after the submit handler

Rules:

- Declare validators in a `schema()` (own file when reused, inline `form()` argument when used once) — never scatter `required()`/`email()` calls across the template or submit handler.
- Bind with `[formField]` and read with `value()`. The template never reaches into the model signal directly.
- A form model type lives next to its schema. Both are co-located with the component unless a second form consumes them.
- Validation messages belong to the validator, not the template.
