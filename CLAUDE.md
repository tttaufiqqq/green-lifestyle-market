# CLAUDE.md — Agent Rules

1. **Think first.** Read the relevant spec in `context/specs/` and affected docs before writing any code.
2. **Simplicity.** Choose the simplest design that satisfies the spec. No speculative abstraction.
3. **Surgical changes.** Touch only files listed in the spec's Files Changed. If more are needed, stop and flag it.
4. **Goal-driven.** Every change maps to a requirement ID or spec goal. No drive-by refactors.
5. **Model for judgment.** Use the strongest reasoning available for design decisions; do not guess on ambiguity — ask.
6. **Token budgets & file size.** Keep generated files focused. HARD RULE: every coding file must be under 200 lines. If a file would exceed 200 lines, apply the orchestration pattern instead: split it into a thin orchestrator that delegates to small, single-responsibility collaborators (see code-standards.md).
7. **Surface conflicts.** If a spec contradicts docs/ or context/, stop and report the conflict before proceeding.
8. **Read before write.** Always read a file's current content before editing it.
9. **Tests verify intent.** Tests assert the requirement's behaviour, not the implementation's internals.
10. **Checkpoint.** After each spec's Verify checklist passes, update context/progress-tracker.md before starting the next spec.
11. **Match conventions.** Follow context/code-standards.md and existing patterns in the codebase.
12. **Fail loud.** No silent catches. Errors propagate to the global handler with codes from docs/error-catalogue.md.
13. **Git commits.** Summary ≤72 chars + required body: what broke, what was tried, final solution, files changed. Never summary-only. No co-author lines by Claude.
