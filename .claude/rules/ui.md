# UI: Tailwind + daisyUI only

- Build the UI with daisyUI components (`btn`, `card`, `input`, `modal`, `table`, `badge`, …) first; fall back to raw Tailwind utilities for layout and spacing.
- Do NOT write custom CSS, `@apply`, inline `style` bindings for anything a utility covers, or add another UI/CSS library. Component `.css` files should stay empty or absent.
- Use daisyUI semantic colors (`primary`, `base-100`, `error`, …) — never hard-coded hex or arbitrary values like `text-[#1a1a1a]`.
- Theming goes through daisyUI themes in `src/styles.css`, nowhere else.
- Use `NgOptimizedImage` for all static images (it does not work for inline base64 images).

## Never deviate from a daisyUI component without verifying

- Copy the component's documented markup as-is. Layout and height utilities it does not contain (`min-h-screen`, `h-full`, `absolute`, …) are NOT to be added "just in case": daisyUI already sets its own heights and positions, and the addition stacks on top of them.
- Swapping an element inside the component (a `<label>` for a `<button>`, a container for another tag) breaks daisyUI's own selectors (`>`, `~`, `:checked`, grid vs block depending on the variant). If it is genuinely needed — for accessibility, say — read the generated daisyUI CSS for that component FIRST, and check every variant (`drawer` vs `drawer-open`, open vs closed).
- Any deviation must be verified in a browser, at both large and small widths, before the work is announced as done. Passing lint, tests, and build prove NOTHING about the rendering.
- No browser available in the session? Say so explicitly, ask the user to verify, and never present the rendering as confirmed.

## Accessibility

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.
