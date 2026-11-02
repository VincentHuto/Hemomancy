# Hemomancy Portable Mod Agent Prompt Design

## Goal

Create a reusable prompt file for future Codex, Copilot, and similar coding harness sessions working in the Hemomancy repository. The prompt should make an agent effective in this specific Minecraft NeoForge mod environment, while keeping documentation and wiki maintenance part of normal implementation work.

The canonical prompt file will live at:

`docs/agents/hemomancy-mod-agent.md`

## Context

Hemomancy is a Minecraft NeoForge 1.21.1 mod using Java 21, NeoForge attachments, payload-based networking, HutosLib, local compatibility jars, generated and hand-authored resources, and a large body of reference documentation. Existing project guidance already lives in `AGENTS.md`, source-of-truth implementation docs live in `docs/HEMOMANCY_REFERENCE.md`, lore guidance lives in `docs/LORE_REFERENCE.md`, and player-facing GitHub Wiki pages are tracked under `wiki/`.

The new prompt should not replace `AGENTS.md`. It should act as the fuller portable operating profile for future agents, especially tools that do not automatically load root agent instructions.

## Recommended Approach

Use a dedicated portable prompt file in `docs/agents/hemomancy-mod-agent.md`.

This is preferred over expanding `AGENTS.md` because the full operating profile can be detailed without making every session's root instructions overly large. It is also preferred over duplicating harness-specific files because one canonical prompt reduces drift.

Add a short pointer in `AGENTS.md` telling future agents and harnesses to consult `docs/agents/hemomancy-mod-agent.md` when they need the full Hemomancy operating profile.

## Prompt Structure

The prompt file should include these sections:

1. Identity and mission
   - Define the agent as a Hemomancy-focused Minecraft mod development assistant.
   - State that code, resources, docs, wiki, and lore consistency are all part of the job.

2. Startup checklist
   - Read `AGENTS.md`.
   - Confirm project version details from `gradle.properties` and `build.gradle`.
   - Prefer current code over stale documentation.
   - Check `git status --short` before edits.
   - Use `rg`/`rg --files` for exploration.

3. Minecraft and NeoForge constraints
   - Use NeoForge 1.21.1 APIs and `net.neoforged.*` imports.
   - Do not revive legacy Forge registration, capability provider, or `SimpleChannel` networking patterns.
   - Preserve Java 21 and existing Gradle/composite-build assumptions.

4. Architecture map
   - Summarize `Hemomancy.java`, `common/init/*Init.java`, `client/`, `common/`, `compat/`, `config/`, `mixin/`, data paths, resources, datagen, and test locations.
   - Include capability and networking patterns that future agents must follow.

5. Documentation contract
   - Treat docs and wiki updates as required whenever behavior changes.
   - Require updates to `docs/HEMOMANCY_REFERENCE.md` for implementation, balance, status, registry, capability, networking, resource, or recipe changes.
   - Require updates to `docs/LORE_REFERENCE.md` for lore, faction, character, cosmology, or tone changes.
   - Require updates to `docs/MNA_COMPATIBILITY_BRAINSTORM.md` when Mana and Artifice design or dormant compat assumptions change.
   - Require updates to relevant `wiki/*.md` pages for player-facing behavior, progression, items, blocks, systems, lore, compatibility, or developer guidance.

6. Wiki update matrix
   - Map gameplay onboarding to `wiki/Getting-Started.md`.
   - Map Harbinger progression, degrees, bloodlines, armor, drudges, morphlings, and Qliphoth content to `wiki/Harbinger-Path.md`.
   - Map Unstained purification, Lethean mechanics, White Humor, copper tools, and Pale Lady content to `wiki/Unstained-Path.md`.
   - Map blood volume, tendencies, manipulations, vascular systems, skills, effects, and blood routing to `wiki/Blood-Systems.md`.
   - Map narrative, factions, cosmology, characters, structures, and endings to `wiki/Lore-and-Story.md`.
   - Map dependencies, JEI, dormant MnA/Curios assumptions, tags, config, and modpack guidance to `wiki/Mod-Compatibility.md`.
   - Map APIs, build/test guidance, architecture, resources, datagen, and contribution guidance to `wiki/Developer-Reference.md`.
   - Map broad navigation or major feature additions to `wiki/Home.md` and `wiki/README.md` when appropriate.

7. Lore and tone guardrails
   - Preserve the morally gray Harbinger and Unstained framing.
   - Treat fungal cosmic horror and blood-memory biology as core themes.
   - Avoid simple villain/hero framing.
   - Preserve existing vocabulary and distinction between internal tendency names and enzyme item names.

8. Resource and data expectations
   - New gameplay content may require language entries, models, textures, recipes, loot tables, tags, dialogue inquiry JSON, datagen providers, JEI surfaces, and wiki updates.
   - Agents should check existing resources before adding new assets or IDs.

9. Implementation discipline
   - Make scoped changes.
   - Preserve user changes in dirty worktrees.
   - Prefer existing helper APIs and local patterns.
   - Add tests or resource validation proportional to risk.
   - Run `./gradlew.bat build` or focused validation when practical, and report failures honestly.

10. Completion checklist
    - Code/resources changed.
    - Reference docs updated or explicitly deemed unnecessary.
    - Wiki pages updated or explicitly deemed unnecessary.
    - Lore tone checked.
    - NeoForge 1.21 imports/patterns checked.
    - Build/test/datagen command run or reason not run.

## Error Handling and Ambiguity

If the agent cannot determine which documentation page is affected, it should update the narrowest obviously relevant page and mention any remaining uncertainty. If a docs claim conflicts with code, the agent should prefer code, update stale docs when in scope, and call out the correction.

If a requested feature touches dormant MnA or Curios compatibility, the agent should not remove Gradle exclusions or import dormant compat classes unless real dependencies are added and the user explicitly wants that integration work.

## Testing and Verification

The implementation plan should verify:

- `docs/agents/hemomancy-mod-agent.md` exists and is readable as a standalone prompt.
- `AGENTS.md` points to the portable prompt without duplicating it.
- The prompt contains explicit docs and wiki update rules.
- The prompt references NeoForge 1.21.1, Java 21, payload networking, attachments, resource paths, and lore guardrails.
- `git diff --check` passes after edits.

No Gradle build is required for this documentation-only prompt unless later edits touch code or resources.

