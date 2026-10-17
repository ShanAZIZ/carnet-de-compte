# UI: Tailwind + daisyUI only

- Build the UI with daisyUI components (`btn`, `card`, `input`, `modal`, `table`, `badge`, …) first; fall back to raw Tailwind utilities for layout and spacing.
- Do NOT write custom CSS, `@apply`, inline `style` bindings for anything a utility covers, or add another UI/CSS library. Component `.css` files should stay empty or absent.
- Use daisyUI semantic colors (`primary`, `base-100`, `error`, …) — never hard-coded hex or arbitrary values like `text-[#1a1a1a]`.
- Theming goes through daisyUI themes in `src/styles.css`, nowhere else.
- Use `NgOptimizedImage` for all static images (it does not work for inline base64 images).

## Accessibility

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.
