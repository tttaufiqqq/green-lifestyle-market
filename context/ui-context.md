# UI Context

## References
- shadcn/ui default look with a green-forward palette; marketplace layout patterns from Carousell/Mudah (card grid, sticky filters); admin patterns from shadcn dashboard examples.
- Diagrams: one ASCII mockup per page in `docs/diagrams/pages/`.

## Design tokens
| Token | Value | Use |
|---|---|---|
| `--primary` | emerald-600 `#059669` | CTAs, active nav |
| `--primary-hover` | emerald-700 | hover states |
| `--accent` | amber-500 | badges (e.g., "Used — Good") |
| `--bg` | zinc-50 | page background |
| `--surface` | white | cards, modals |
| `--text` | zinc-900 / zinc-500 | primary / secondary text |
| `--danger` | red-600 | destructive actions, errors |
| Radius | `rounded-2xl` cards, `rounded-lg` inputs | |
| Font | Inter, system fallback | |
| Spacing | Tailwind scale; page gutter `px-4 md:px-8` | |

## Page hierarchy
```
Public:   Home / Browse ─ Product Detail ─ Articles ─ Article Detail ─ Login ─ Register ─ Help (/help)
Buyer:    Cart ─ Checkout ─ Payment Result ─ My Orders ─ Order Detail ─ Profile ─ Notifications
Seller:   My Listings ─ Create/Edit Listing ─ My Sales ─ Sale Detail ─ Payouts (read-only) ─ Bank Account
Admin:    Dashboard ─ Orders ─ Refunds ─ Payouts ─ Reconciliation ─ Users ─ Categories ─ Articles CMS
```

## Layout structure
- **Public/buyer/seller:** top navbar (logo, search bar, Sell button, cart badge, bell, avatar menu) + content + footer. Mobile: bottom tab bar (Home, Search, Sell, Orders, Profile).
- **Admin:** persistent left sidebar + topbar; content in cards.
- Grid: product cards 2-col mobile, 4-col desktop. All lists paginated (12/page catalog, 20/page tables).

## Modal Usage Principles
Modals only for: confirmations, quick forms, detail views, edit forms. Never stack modals. Every modal: title + body + close button + Escape closes. Table-row modals: skeleton while loading, prev/next navigation, inline edit, unsaved-changes guard, inline errors. No background scroll while open. On mobile, modals render as bottom sheets. Saves are explicit and confirmed (no optimistic save for money-related actions; optimistic allowed for notification read-state only).
