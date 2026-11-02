# 6. Verify the result in play

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

Collect evidence as each ticket lands. This phase closes cross-system gaps rather than repeating every test indiscriminately.

The [Area 6 acceptance results](06-acceptance-results.md) record the regression pass, remote checks, corrective fixes and all 97 per-power results. Full acceptance remains open where the report lists unresolved checks.

## M32 — Close the audit with focused gameplay and visual evidence

- [ ] Complete this task and record evidence.

**Scope:** All 97 active powers; retired IDs remain compatibility-only.

**Depends on:** M01–M31. **Work unit:** Focused acceptance pass.

**Do:** Run the regression checks created by earlier fixes, then one normal acquisition route, one solo combat route, one allied/PvP-disabled scenario and one dedicated-server remote-client pass. Sample ordinary enemies, bosses, invalid targets and low-blood states. Revisit every power in the coverage table and amend the original audit with links to actual evidence.

**Done when:** Every task has its own recorded checks and remaining limitations. No task is marked complete solely because compileJava passes. Unresolved live checks remain open; the audit distinguishes fixed defects, chosen balance values, completed visual work and deferred optional polish.

**Visual acceptance:** apply the [required visual standard](05-visuals-and-audio.md#required-visual-standard-for-every-manipulation) to all 97 active powers. Keep a per-power result: already meets the concept, revised with captured evidence, or still pending. Inspect actual motion and signature forms, including Crimson Coronation's bloody swords; generic vanilla particle emission cannot count as a completed replacement. Screenshots of icons or HUDs do not establish casting-effect quality.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
