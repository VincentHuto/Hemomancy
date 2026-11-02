# Crimson Troupe expansion

Implement inline in the current checkout. Preserve unrelated changes; do not stage, commit, push, or create a worktree.

## Accepted design

The Troupe is the main Puppeteering school. Personal instruction precedes an existing Cardinal Ordeal; practical examinations follow unlocking. Existing learned shapes remain usable. Harbinger Main progression and the two existing Circus endings stay independent of apprenticeship.

Professors: Acrobat/Vulture (D3), Dagger Thrower/Marrow Juggler (D3), Stilt Walker/Mummer (D4), Strongman/Hulk (D4), Beast Tamer/Hound (D4), Fire Breather/Cinder Bellows (D4), Retired Understudy/Mnemonist (D5), plus Threadkeeper and Ringmaster.

Juggler retains the marrow_spitter save/registry identities: floating hunched frame, crimson robe, three theatrical masks, six arms, juggling and sequential daggers with unchanged aggregate damage/cadence. Hulk retains identities/stats and gains the constructed Strongman design. Cinder Bellows is a lamia with a gaunt upper body, visible lung-sac, and a coil/inflate/breathe/recover cycle: D4, 30 HP, speed .24, call 48, upkeep 16, five-block 60-degree cone, 20/40/40 ticks, two base damage every ten breathing ticks; no terrain fire. Its 900 mL Dominion Lesser ordeal consumes its frame only after manifestation and preserves the Crossbar.

The divided faculty have practical advice, histories, disagreements, and progress-aware dialogue. Safe teacher-owned demonstrations reuse the real puppet entities without player ownership, charge, loot, trial credit, or visitor aggression.

Five 25x25 teaching camp variants cover every ordinary shape. A 65x65 new pavilion surrounds the legacy central arena with Threadhouse, rehearsal rings, rigging loft, wardrobe wagon, faculty mess and understudy quarters. Abandoned stages supply story clues. Preserve the legacy template and existing placed sites. Validate actual full footprints. Camps use spacing 24/separation 8; pavilion frequency stays unchanged. Waybill normal use finds the next eligible teacher; sneak use finds an unfinished pavilion.

Assignments: An Empty Ring; seven personal practicals; The Wrong Audience; The Missing Understudy; repeat guest performances. Actual server-observed proof, Side ledger next actions, resume/retry/cleanup, exact pending guest reward delivery (four Filaments once per Minecraft day). Acclimation: 100 Empty Ring, 100 each practical, 150 each story chain, existing cap 1000. No extra finale prerequisite. Preserve captive deaths, outcomes and rewards; add aftermath dialogue. Other camps keep teaching available.

## Execution checklist

- [x] Curriculum, server teaching gates, recipe knowledge, grandfathering and persistence.
- [x] Puppet combat, Java models, textures, renderers and editable Blockbench sources.
- [x] Faculty, dialogue, paired demonstrations and finale isolation.
- [x] Teaching camps, abandoned stages, grand pavilion and Waybill navigation.
- [x] Practical/story/guest quests, ledger and rewards.
- [x] Endings, building decorations, books/references/wiki and integrated verification attempted.

## Verification

Focused red/green JVM behavior tests; mod-loaded real item/entity/dialogue/persistence GameTests; model/UV/resource contracts; compileGameTestJava and assemble; combined runtime/resource gate and broader checks. Native acceptance: fresh generation, visuals, first apprenticeship, endings, cold restart and multiplayer. Report any unperformed acceptance honestly.

Implemented and verified as detailed in [the implementation report](../../circus/08-crimson-troupe-implementation-report.md). Focused checks pass; the full checkout gates retain unrelated failures. Native acceptance remains outstanding.
