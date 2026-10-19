# Unit Tests

## Where

- One spec per source file, named `<file>.spec.ts`, sitting next to it. No `__tests__/`, no mirrored `test/` tree.
- Test the public surface: what the template renders, what a service returns. Never a `#private` member, never an implementation detail.
- No spec for a file with no logic (routes, config, plain type files).

## Structure

- One top-level `describe` per file, named after the symbol under test (`describe('LoginForm', ...)`).
- Group with a nested `describe` only when a method or behaviour has several cases. Otherwise flat `it`s.
- `it('...')` reads as a sentence stating observable behaviour: `it('disables submit while the form is invalid')`. No `should` prefix, no `works`, no `test 1`.
- Arrange / Act / Assert, in that order, separated by a blank line. One behaviour per `it`, and prefer one assertion — several only when they describe the same outcome.

## Angular specifics

- `TestBed.configureTestingModule({ imports: [Thing] })` in `beforeEach`; standalone components go in `imports`, never `declarations`.
- Build the component with `TestBed.createComponent()` and keep `fixture` + instance in `beforeEach` so each `it` starts clean.
- Await `fixture.whenStable()` before asserting on rendered DOM. Do NOT call `detectChanges()` manually in zoneless code.
- Set inputs with `fixture.componentRef.setInput()`, read outputs by subscribing — never assign the signal directly.
- Query the DOM by accessible handle (role, label, text) over CSS class, so a Tailwind/daisyUI restyle doesn't break the test.
- Replace a dependency with `providers: [{ provide: Dep, useValue: stub }]`. Plain object literals as stubs; `vi.fn()` only when the call itself is the assertion.
- HTTP: `provideHttpClient()` + `provideHttpClientTesting()`, flush with `HttpTestingController` and `verify()` in `afterEach`.
- Signal state: assert through `computed()`/rendered output, not by reading every intermediate signal.

## Quality

- `vitest` only (`npm test`). Never Karma, Jasmine, or Jest; `vi` for mocks and fake timers.
- No logic in tests: no `if`, no loop building assertions. Repeated cases go through `it.each`.
- Shared setup is a local factory function at the bottom of the spec, not a cross-file helper — promote it only when a second spec needs it.
- A spec over ~150 lines means the component does too much. Split the component, not the spec.
- No snapshot tests, no coverage-chasing tests that assert nothing meaningful.
