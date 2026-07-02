# Project Overview — Green Lifestyle Market (GLM)

## What it is
A web marketplace where the UTeM community and the public buy and sell eco-friendly products — new, used, and donated — with online FPX payment, and learn sustainable living through educational content.

## Why it exists (problems solved)
1. **No easy way to sell used belongings** → items are thrown away instead of reused.
2. **Unnecessary resource consumption** → buying new when good used items exist.
3. **Hard to get additional income** → students and staff need a low-friction side income channel.

## Objectives
1. User-friendly marketplace for eco-friendly products (new + pre-owned).
2. Educate users on sustainable choices via an articles section.
3. Foster community: campus meetups, seller profiles, mutual trust via order lifecycle.
4. Give sellers real income with trustworthy escrow payments and payouts.

## Actors
| Actor | Description |
|---|---|
| Guest | Unauthenticated visitor. Browse, search, read articles. |
| Buyer | Registered user purchasing items. Every registered user can buy. |
| Seller | Registered user with ≥1 listing and a verified bank account for payouts. Same account as Buyer. |
| Admin | Platform operator. Moderation, refunds, payouts, reconciliation, articles, categories. |
| ToyyibPay | External payment gateway (FPX). Dev sandbox in this phase. |

## Business model
- Buyer pays item price + shipping (if any) via ToyyibPay into the **platform account** (escrow).
- Platform deducts a **5% commission on item subtotal** (not on shipping).
- Admin pays the seller the net amount after the buyer confirms receipt (or auto-complete after 7 days).

## Scope boundaries (v1)
**In:** listings, cart, multi-seller checkout (order split per seller), ToyyibPay FPX, meetup + shipping fulfilment, cancellations, manual refunds, seller payouts, notifications, articles, admin panel, in-app help.
**Out (future):** ratings/reviews, chat between users, auctions, automated refunds, mobile app, delivery API integration, multi-language toggle.

## Success criteria
- A buyer completes checkout → payment → receipt confirmation end-to-end in the dev sandbox.
- A seller lists an item and receives a recorded payout after order completion.
- Every ringgit in ToyyibPay reconciles to an order and a payout/refund (docs/financial-rules.md).
