# Crimson Troupe audit, 2026-11-06: consolidated ledger

Five read-only audits of the Circus on `neo-1.21.1` at `40a1b3658`, each written by its own agent against the code
and data, measured against the Brined Court, the Saint combat kit (93070b745) and the current AGENTS.md standards.
This file is the roll-up. Each line cites the finding id in its report, and the report holds the evidence
(`path:line`). Nothing was built, run or edited. The coordinating session re-checked four High claims in source:
B-1 (`CircusPerformerEntity.hurt` only counts hits whose source entity is the owning player), D-2 (captives are cut
by a plain right-click in `BloodDrunkPuppeteerEntity.mobInteract`), P-2/W-4 (`circusDistortion` appears nowhere in
`src`) and Q-2 (`KnownSummonEvents.grantSummon`, called from `CircusRingmasterEntity:342`, has no Degree check).

## The reports

| Report | Area | Findings |
| --- | --- | --- |
| [01-quests-and-progression.md](01-quests-and-progression.md) | journey from discovery to the endings, school, Side work, dialogue, voice, trade, rewards, gates | 17 (4 High, 8 Medium) |
| [02-bosses-and-encounters.md](02-bosses-and-encounters.md) | Ringmaster finale, carousel, Succession/Liberation, performers as fighters, C-13 mini-boss ladder | 15 (4 High, 7 Medium) |
| [03-puppetry-powers-and-visuals.md](03-puppetry-powers-and-visuals.md) | the seven bodies, Ringmaster Pattern, Thread Ripper, mental powers, perception, models, Puppet Interference | 16 (3 High, 8 Medium) |
| [04-worldgen-and-structures.md](04-worldgen-and-structures.md) | pavilion, camps, stage, placement, pipeline, set pieces, discovery trail, loot | 18 (4 High, 7 Medium) |
| [05-tests-architecture-and-docs.md](05-tests-architecture-and-docs.md) | test inventory and gate, AGENTS.md compliance, doc drift | 33 (4 High, 15 Medium) |

## The shape

The middle is good. The apprenticeship (seven professors, ordeals, practicals, investigations, guest acts) is
complete and in places better tracked than anything the Court has (01). Everything around it lags the Court and the
Saints:

1. **The culmination is cut off.** The Ringmaster can't be clicked from the floor in survival (Q-1/D-57), because
   the private galleries and canopy that acclimation was meant to make solid were never built (P-2, W-4, D-1). When
   it is reached, it needs none of the school and no Degree: a D0 player can earn the D4 Ringmaster Pattern (Q-2,
   Q-7).
2. **The finale isn't a boss fight by today's bar.** It has no move timeline, telegraphs, intro window or arena
   record, and uses vanilla particles and sounds only. The Ringmaster throws four knives from a perch, idles through
   CAROUSEL, then melees as a vanilla mob (B-2, B-3, B-6, B-9). Puppet damage doesn't count in the Puppeteering
   school's own finale (B-1). There are two stuck states (B-4).
3. **It's not yet a mental path.** Of attention, illusion, decoy, displacement, flash and sound, the player gets only
   the Mummer's automatic taunt (P-1). The Ringmaster Pattern adds nothing for a one-Crossbar player (P-3). Six of
   eight bodies are damage dealers (P-4). There's no `MorphlingStrains`-style table (P-5).
4. **The sites are September vanilla.** The pavilion is the old 33x33 arena plus six red-wool boxes (64% wool and
   planks). The five camps are one tent layout (98.6% vanilla). The pipeline can't be re-run (W-1..W-3). There's no
   discovery trail (W-9), and the only site outcome is six cobwebs (W-11).
5. **The proofs don't run.** The best Circus tests live only in `playthrough_validation`, which `alphaCheck` skips.
   Ten JVM tests check a pavilion that no longer ships (T-1, T-2). The Court and the Saints each have five gated
   batches; the Circus has one.

## A. Fix now (no design call needed)

Ordered by player impact. Effort S / M / L.

| # | Fix | Ids | Effort |
| --- | --- | --- | --- |
| A1 | Move the Scarlet Waybill's structure search off the server thread, like the Court's Relay Chart; drop the per-tick search while held | Q-3, W-7, A-4 | M |
| A2 | Guard `CircusRingmasterEntity.die()` on Succession and release site ownership on any death or removal; respawn or replace performers killed in a failed Liberation so Succession can leave CAROUSEL | B-4, D-62 | M |
| A3 | Topper and Thread Ripper memory through `PendingDelivery` claims (living player, confirmed insertion) | Q-5, A-3 (guest pay is a decision) | S |
| A4 | Circus perception into the `PlayerStateSync` table: owner-only, dirty-flag flush, no per-second broadcast, no own respawn/dimension resends | A-1, Q-15 | M |
| A5 | Live act state out of player root keys into the `CircusProgress` attachment; delete leftover migrations and dead code | A-2, A-6, Q-15 | M |
| A6 | Degree checks through `ProgressionGates`; summon ids through a constant table, not literals | A-9, A-5 | S |
| A7 | Delete `CircusPavilionTemplate` and its ten JVM tests, or point them at the shipped 65x65 NBT | T-1, W-3 | S |
| A8 | Put a compact finale and the living-observation rule in the gated `crimson_troupe_validation` batch; move the Ringmaster tests into it; fix CT label collisions | T-2, T-7, T-8 | M |
| A9 | Pavilion light: rooms and arena rim lit above the spawn threshold | W-10 | S |
| A10 | Fix the Summons tab's wrong offering for Hound, Bellows and Pattern | P-5 | S |
| A11 | Brass off the Crossbar and Juggler (hematic iron, bone, lacquer) | P-10 | S |
| A12 | Telegraph wind-ups on every Circus attack (Saint kit: 12 ticks melee, 20 area); the Ringmaster's throw animation before the knife | B-2 | M |
| A13 | Docs: CIRCUS.md "Where it lives", worldgen numbers, status line, Liberation mechanism as built, missing geometry marked not built, LORE §6.3b, wiki, OPEN_QUESTIONS rows for this audit's questions | D-1..D-13, Q-16, W-18, P-16 | M |
| A14 | Remove the 25 vanilla-particle allowances as each file is touched (falls out of B-9 / P-6 / W-12 work) | A-8 | M |

## B. Rebuild tracks (each waits on decisions in C)

These are track-sized, in the shape the Court passes used. A suggested order follows the dependency: reachability
and gating first, because everything after the school depends on reaching the Ringmaster.

1. **Reach and gate the culmination.** Make the perch reachable, either with private per-viewer geometry as designed
   or with real galleries for everyone. Tie the pact and finale to Degree and to school work. Add a route-progress
   view. Decide the Unstained rule. (Q-1, Q-2, Q-6, Q-7, Q-9, P-2, W-4; C1, C2, C3)
2. **Ringmaster fight rebuild on the boss kit.** Move timeline and replay clock, an intro window, an arena record,
   `canHarmFor` targeting, and a moveset made from the Circus's own mental tools (decoys, displacement, flash,
   attention) rather than knives plus melee. Real acts instead of timers. Bespoke VFX, sounds and music. Ending beats.
   A generator-built jointed model. (B-1..B-3, B-5..B-14, P-9, P-11; C4..C7)
3. **Mental powers and the puppet table.** One `PuppetShapes` table plus a guardrail, like `MorphlingStrains`. One
   mental verb per body (or Circus-taught memories). Re-role the overlapping D3 bodies. A real Ringmaster Pattern.
   A puppet HUD and command-state threads. Custom sounds. A review client. (P-1, P-3..P-7, P-12, P-14, P-15; C8..C11)
4. **Sites rebuild in `tools/worldgen/circus/`.** A pavilion and five distinct camps as set pieces at the mausoleum
   and Court-seat bar. A placement gate that works, with spacing from villages and from each other. A discovery
   trail. Outcome states for a Succeeded or Liberated pavilion. A worldgen validation run and a review client.
   (W-1..W-3, W-5, W-6, W-8, W-9, W-11, W-13..W-17; C12..C16)
5. **C-13 mini-boss ladder.** Professors or performers as midgame encounters leading to the Ringmaster, possibly
   replacing finale acts 2 to 4. The sketch is in 02 "Mini-boss ladder". (B-5, Q-8; C5)
6. **After the finale.** A role for the successor, trade that reflects the school, and the Liberation consequence.
   (Q-8, Q-17, B-13)

## C. Decisions the owner must make

Deduplicated across the five reports. The reports give the options and their evidence.

**Reach and progression**

1. Private per-viewer geometry at 500/1000 acclimation (as designed, with the multiplayer and collision cost), or
   real galleries and perch for everyone? (P-2, W-4, D-57)
2. Should the pact or finale need D4, and should it need school work (practicals or The Wrong Audience for
   Succession, The Missing Understudy for Liberation)? Should talking to performers still give acclimation? (Q-2, Q-7)
3. What is the Unstained rule for the Circus? Right now an Unstained Successor silently loses the Pattern. (Q-6)
4. A D3 pointer to the Circus before the D4 Waybill (PT-21)? And should anything point at the abandoned stage (D-58)? (Q-4, W-9)

**The fight**

5. Move the Circus bosses onto the generic half of the Saint/boss kit (and does that settle G-2c)? Build C-13 now
   (D-18), and do the mini-bosses replace acts 2 to 4? (B-3, B-5)
6. Should puppet damage count as the owner's in acts and the finale? Should troupe area attacks spare bystanders?
   Solo finale, or co-op with scaling? (B-1, B-8, A-10)
7. Liberation: is the hand-sever the design (Thread Ripper is the prize, a Summa memory most Liberators can't learn
   yet), or should Thread Ripper be granted earlier and required? Can it unravel Vesper's puppets, as it can now?
   (D-2, B-7, P-9)
8. What happens to performers killed in a failed Liberation and to survivors after a ruin? Should Succession end
   with a yield scene, like the Kindled Champion's? Does the Ringmaster get music? (B-4, B-13, B-9)

**Powers**

9. Where the mental verbs live: one act per body, Circus-taught memories, or docs only? (P-1)
10. Ringmaster Pattern: a free slot, or a group act? (P-3)
11. Re-role the Vulture or the Juggler (both D3 ranged volleys)? (P-4)
12. Puppet Interference: keep both penalties (Morphlings ×1.25 blood, puppets ×1.5 thread at 75% range) or one?
    Add a real Court–Circus synergy? (P-13)
13. Circus NPCs onto the jointed rig now or later? Are faculty costumes exempt from the no-brass rule? (P-11, P-10)

**Sites**

14. A full rebuild in a new `tools/worldgen/circus/` generator, or a re-dress of the shell around the old arena?
    (W-1..W-3)
15. Which set pieces the big top and each camp get, and how rare pavilions and camps should be. (W-1, W-2, W-5)
16. Do Circus sites hold loot? What does a Succeeded or Liberated pavilion look like afterwards? (W-16, W-11)

**Small**

17. Guest pay: an automatic `PendingDelivery` claim, or stays "collect from a Threadkeeper"? Is it one guest act a
    day per school or per professor? (A-3, Q-11)
18. Is it OK to add one to two minutes to `alphaCheck` for a gated compact finale? (T-2)
19. The open playthrough probes D-59 (practicals in Peaceful), D-60 (unreachable Threadkeeper briefing), D-61
    (misleading finale lines) and D-63 (killed target stalls a practical) are still yours (`OPEN_QUESTIONS.md` §1.6).

## D. Never watched in a client

None of these has been seen in a client: the finale presentation (bar, opening scene, phase cards), the carousel and
horse arcs, perception stages, performer threads, the rebuilt Bellows, Juggler, Hulk and faculty models, the camps,
the Luminal Cicada, or fresh-chunk placement. There is no Circus review client. `npcTradeReviewClient` only captures
the Ringmaster's Trade box.
