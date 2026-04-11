---
agent: ask
model: GPT-5.3-Codex
description: "Create standardized issues using MCP-first workflow (single issue by default, orchestration when split is needed)"
---

Before creation, decide issue type:

1. Use a single issue when the work can be completed as one coherent task without independent sub-tracks.
2. Use orchestration only when the work must be split into multiple child tasks with independent scope or sequencing.

Then create GitHub issues using this priority order:

1. Use MCP GitHub issue tools in this environment.

Rules:

- If orchestration is selected: create exactly one parent issue and all child task issues, then update parent issue body to include child links.
- If single is selected: create exactly one issue.
- Run sequentially, not in parallel.
- Report all created URLs at the end.

Before execution:

- Confirm repo target.
- Confirm template structure for selected issue type.
