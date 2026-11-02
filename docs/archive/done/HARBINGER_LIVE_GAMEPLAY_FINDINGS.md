# Harbinger progression: live gameplay findings

Test date: 10 September 2026. This report summarizes the actual client campaign in [the evidence journal](HARBINGER_GAMEPLAY_CAMPAIGN.md). It supersedes the journal's earlier, chronological statements that later degrees were still pending. Production gameplay was not changed: the approved approach was to preserve failures and continue in disposable saves with declared assists.

Recommended follow-up work is organized in the [grouped repair plan and dependency register](harbinger_repair_plan/README.md), with linked prerequisites, code entry points, gameplay recommendations and acceptance checks for each task.

## What was actually played

The primary character began at D0 in a generated survival world, met a generated Hermit, and earned D1 through D8 using the real rites. A second copy took the Silent branch: actual pruning, Vesper's anchors and both phases, final absorption, return, Silent Archon reward, and Memory of Vesper graft. This was **not an uninterrupted, unassisted survival completion**. The unmodified journey was already blocked at its first assignment reward. Later continuation required dialogue workarounds, supplied raw materials, travel, recipe overrides, and a pome-consumption workaround.

The final D8 ordeal ran on Normal with full Chitinite, Thorn, four supports, normal attacks and repairs, without combat invulnerability or a mid-fight blood grant. It lasted approximately 7 minutes 20 seconds. Vesper is different: the unassisted Normal attempt died after approximately 23 seconds. A separate mechanics-coverage run used Resistance V and Saturation, then Strength X in phase two. Its completion verifies encounter transitions and reward flow, **not ordinary difficulty or a legitimate survival victory**.

Real crafting interfaces, inventory actions, NPC conversations, blood extraction, machine processing, weaving, carving, companion calls, damage, death, respawn, and save/reload were exercised. Two actual clients also joined a dedicated server, joined the same Fane, checked personal crafting access and a shared centrifuge, and reconnected after a restart. Three separate dedicated particle failures interrupted that portion.

The opt-in driver supplies normal client inputs and records state and native screenshots. Coordinates sometimes came from read-only server data. It is not a human dexterity benchmark: navigation, unattended pauses, fixed camera targets, and native GLFW hold limitations affected some attempts. Failed setup actions and contaminated trials are explicitly excluded in the journal.

## Priority findings

Priority here means the order in which to restore a playable journey, not a numerical severity score. These are observed failures unless stated otherwise.

| Priority | Failure and player consequence | Evidence and recommended repair |
|---|---|---|
| Critical | Dedicated server crashes with a learned memory left in a lit brazier and on Cardinal Rite helix startup. Projection ignition also disconnects its actor. Basic shared progression is unsafe. | Separate reproductions, saved crashes, and both clients' disconnects. Remove client-only particle classes from server execution paths; validate ignition, persistent aura and rite startup on a dedicated server separately. |
| Critical | The awakened Living Staff crashes the client when cycling into Blade. Completing the Silent reward introduces a crash into an ordinary control. | `Duplicate delegates`, 14:49:48 UTC; `LivingStaffMorphRenderer` foil path. A test-only no-glint component prevented repeated transitions from crashing. Correct distinct foil/base buffer handling; test awakened and ordinary forms. |
| Blocking | First Blood reports rewards but delivers none through the normal NPC hub. Later instructional topics can disappear too. | Completed D1 objectives, empty reward result; original focused dialogue delivered the actual rewards. Repair hub navigation/event routing and acknowledge completion only after server delivery. Recheck Vicar, Alchemist, Mnemonist and Anchorite. |
| Blocking | Monolith construction is D7-gated while its Throne-dependent route is needed at D6; important target cells are enclosed. | Actual rejected/unreachable production recipes. Only a copied-world datapack lowered the Monolith gate and exposed targets. Reconcile the prerequisite graph and make every interaction target reachable in the authored structure. |
| Blocking | A cardinal-crafted Bloodwell does not grant the personal-crafting access it requires. | Personally completed Fane, then denied the resulting well's GUI. Award the canonical craft/access credit on cardinal completion, including interruption and reload cases. |
| Blocking | Eating the last pome loses its source association and leaves the tree awaiting consumption. | Two clean first-pome attempts: player count increased, tree lifecycle remained pending. Preserve source data before shrinking the stack; notify the originating tree exactly once. Test last item, stacked item and reload. |
| Blocking | Completed Silent Archon remains unable to use ordinary blood manipulations; new puppet trials also reject completed D8 Apotheos. Hematic Fortification accepts and charges the completed Silent player, then rejects its finish. | Actual post-completion refusals at nine pomes. `BloodManipulation` exempts D8 rather than completed Silent; puppet startup and cardinal completion have separate gates. Centralize pending-choice versus completed-path rules, validate before investment, and exercise both endings. |
| Blocking | Equipped morphling bond reaches 50/50, but its Jar still records 0/50; normal next feeding is refused. | HUD, authoritative item data, removal/reinsertion, failed Incubator feed. Persist the same creature identity and earned bond back to its container before inventory transitions. |
| Blocking | Seven Primal Staff actives produce no advertised effect or cost. | Normal uses with high blood and comparative Primal fixtures. `LivingStaffItem.releaseUsing` passes the Staff stack to the morphling's stage check. Pass the selected creature's actual data; verify cost, cooldown and effect for each active. Gravecap's on-kill power is a separate case. |
| High | Monolith accepts the cornerstone choice and says it separated, but grants nothing. | Normal root navigation and acceptance, unchanged inventory/claim state. Block speaker ID `-1` is rejected by entity validation in `DialogueOptionPacket`. Handle authenticated block conversations explicitly. |
| High | Alembic flask filling cannot meet its cost: 2,000 capacity versus 2,500 required. | Live filling refusal and current constants. Choose a consistent flask/tank transfer contract; surface capacity and stopped-processing reasons. Other distillation recipes do work. |
| High | Syringe/rack loading duplicates empty vial containers; a dropped Living Syringe reward vanishes. | Eight station vials plus eight remaining empty rack vials; zero-lifespan syringe drop. Transfer ownership once and make intended physical rewards survive pickup. |
| High | Loom stocking silently switches the recipe, and a saved active weave loses its configuration. | Extra ANIMUS enzymes changed Blood Shot to Halo; reload produced a configuration-change failure. Pin a chosen recipe independently of reservoir amount and persist/recover the active weave transaction. |
| High | Returning later can remove required teaching access. | D6 Vicar lacks the D5 consecration-kit route. Preserve unfinished assignments across degree changes rather than replacing the whole task surface. |
| High | Relocating an iron-mined Monolith destroys the investment. | Actual mining produced no block item; runtime resources lack its drop table. Ship the intended loot data and retest relocation after personal crafting. |
| Follow-up | Chamber visit flags disappear on death although degree/capacity survive. | Before/after player state confirms the loss; save/reload alone worked. Actual post-death chair consequences remain unverified. Audit clone persistence before claiming a permanent travel lock. |

The first repair batch should address crashes, reward routing, crafting dependencies, pome lifecycle and completed-path gates. Fine damage tuning is premature while these prevent players from reaching or using their builds.

## The journey as it feels in play

| Stage | Strongest moment | Main friction or gap |
|---|---|---|
| D0 to D1 | The generated Hermit, displayed heart, dormant charm and short health-paid initiation form a concrete introduction. The rite accurately charged four health. | Two brief terrain searches do not establish discoverability. Black portraits, clipped greetings and later wandering NPCs weaken guidance. The first useful tool meets a steep blood delay immediately after initiation. |
| D1 to D3 | Bloodcraft produces tangible machines; different animal samples produce different enzymes; actual machine output leads toward memory work. | First reward failure, conflicting default keys, loose-vial versus syringe control mismatch, vanished syringe reward, and unclear recipe acquisition. |
| D3 to D4 | Pulling wandering colored strands into the Loom is distinctive. Finishing the first scar through carving, learning and fitting gives a real sense of bodily customization. | The Loom can make an unusable Halo before Blood Shot mastery; the chapter still advances. The generated teaching station refuses the student until personally crafted. Loading a stencil does not explain tracing. |
| D5 | Founding a bloodline/Fane and recruiting NPC helpers expands the player's role beyond solitary crafting. Support sigils visibly affect the battlefield. | Orientation-dependent sockets and an explicitly "Optional" sigil that is required. Personal Bloodwell access fails; helpers can intercept the same clicks needed to repair anchors. |
| D6 | Chamber entry, a real Arbor purchase, paid Throne Trance and armor upgrades provide useful preparation choices. | Monolith/Throne construction breaks the route. Iron Vigil failed; the successful Chitinite/support/helper setup changed several variables, so armor alone cannot be credited. Older armor lessons disappear at the next rank. |
| D7 | The Monolith shatter, living tree, real pome harvests and branching revelation are strong payoff moments. | Pome consumption stops the tree. Repair totals imply completion while individual anchors remain dry. Fungal travel and the recurring voice need clearer, less intrusive timing. |
| D8 / Silent | Apotheos survives a substantial multiwave ordeal; Silent pruning leads to a distinct multipart boss, final absorption, return and Staff graft. | Nine-pome gates survive completed endings. The Silent reward crashes weapon morphing. Vesper's natural difficulty is unresolved after the short baseline loss and assisted continuation. |

The largest holistic weakness is **unreliable communication of state**. Dialogue says an item was awarded when it was not. A rite says a sigil is optional when it is required. Total blood exceeds the displayed target while a dry anchor blocks progress. The Loom reports a first weave even when the result cannot yet be learned. Players cannot reliably distinguish their own mistake from a broken transition.

Use one authoritative outcome for advancement, reward delivery and dialogue. Show the exact next unmet condition, including the affected anchor or missing personal craft. Keep unfinished lessons visible at higher degrees. Reserve unsolicited whisper screens for safe moments or a dismissible notification rather than replacing crafting and combat preparation interfaces.

## Crafting, materials and resource balance

The broad dependencies make thematic sense: sampling supplies enzymes, enzymes support distillation and weaving, scars alter the body, armor and Fane infrastructure prepare later ordeals. The defects are in specific transitions, availability and feedback, not a need to replace the whole crafting identity.

| Measured example | What it suggests |
|---|---|
| D1 begins at zero blood; roughly 1 blood/second passive recovery; initial utility conjuration costs 1,000. | About 16.7 minutes from passive recovery alone. A normally absorbed 10-HP cow returned about 10 blood. Give the opening lesson a practical, taught source at the scale its tools demand. This campaign did not establish the best natural mining route. |
| Blood Rocks provide 250 with a three-second cooldown; source drop chance is 2.5% from venous stone. | Useful as supplied acceleration, but raw-material availability was not tested enough to certify this as a balanced starter economy. Do not confuse rocks consumed in the fixture with rocks naturally acquired. |
| Eight cow samples produced 17 enzymes, five powder and 250 blood; two produced three enzymes, one powder and the same 250 blood. | The bulk incentive and flat blood output need explicit explanation and intentional tuning. Unlimited harmless resampling of one cow makes species access more consequential than livestock quantity. |
| Alembic tooth processing produced six paste from three teeth in about five seconds; normal tincture distillation also succeeded. | The machine has working value beyond its broken flask transfer. Explain its independent processing, tank, flask and crystal functions together. Near-full processing should say why it stopped rather than continue saying "Distilling". |
| A mature crystal yielded the whole crystal with a normal iron pick; an earlier growth stage yielded shards. | The actual harvest contract conflicts with the stated Silk Touch expectation. Decide which drops are intended and teach maturity/tool requirements. |
| Blood Shot weave costs 50; stocking enough matching enzymes selected Halo at 400. | This is an eightfold surprise plus a mastery prerequisite, not just a small efficiency difference. Recipe choice must be stable and visible before payment. |
| First Thorn: 100 to learn, 500 to prepare its Effigy pattern, 50 to commit; actual modifier +1 armor and -5% movement speed. | The multi-step ritual has character, but the player needs to see the final tradeoff before paying and understand learning versus active fitting. |
| The Throne's 8,000 feed completed with a 5,500-capacity player and an ordinary 2,500 flask refill. | A cost above capacity is not automatically impossible. Keep gradual feed/refill timing clear. This success used the declared accessible-target recipe override. |

Rare armor reagents, Quintessence and repeated construction supplies were provided as gathering assists. Their natural acquisition rates are **not certified balanced**. A nearby Mausoleum locate failed and its current biome tag is empty, supporting a missing worldgen route; other boss loot sources remain possible and untested. Resolve that availability gap before deciding whether Fane and armor prices are fair.

## Rites, weaving, scars and teachings

Cardinal Rites have a recognizable preparation/defense/repair rhythm. Animated supports, boundary investment, helper assignment and visible completion are worth retaining. The later successful setups also reveal a risk: Bastion often did most of the killing, while timed threats vanished and the player's decisive task became repairing a nearly full anchor. That can be a valid defensive ritual, but it should be described and balanced as one. It is misleading to treat every timeout as a kill-all combat victory.

Repeated 49/50 anchors, unreachable outer repair angles, support targeting order and NPC interception turn meaningful defense into uncertain cleanup. Show per-anchor readiness and repair direction; reconcile Reservoir behavior with remaining deficits. Decide whether failed Lens/counterfeit objectives should really advance on timeout. The tested late rites rewarded preparation, but combat alone was not their dominant check.

The Loom's physical dragging is a peak once the controls are understood. Teach fixed-distance dragging and preparation before charging; reserve enough enzymes for the introductory drift allowance. Cancellation currently consumes investment and deserves an explicit preview. Reload must preserve a recoverable weave.

Scars have a coherent distinction between carving, learning and fitting, but that distinction is taught across too many fragile handoffs. The first Anchorite lesson correctly reacted to actual learning and fitting once accessed. It selected a tendency-based Thorn rather than offering a free initial choice. Provide a preview of the selected scar and its reason, clear stencil-versus-trace feedback, and consistent use of the supplied teaching facilities.

The later Thelemic/Noetic distinction is substantive: bodily memory use strains a section, while matching Cerebral Scar routing concerns projected memories. The normal hub hid the D5 instruction. A real Alembic-distilled Sanguine Fists dose learned and equipped the memory, and ordinary activation/punches produced real strain even on Silent Archon. Approximately 240 practice punches crossed into Varicose, the Anchorite recognized it, and normal diagnosis opened the vascular view. A normally crafted salve repaired 25 arm points and advanced the treatment step. This is a working mechanical lesson, although requiring deliberate damage for its own sake risks becoming repetitive training on an inert target.

The radial selection for that probe was a declared saved-state assist; no learning, reserve, strain or quest objective was granted. Completing the subsequent Fortification preparation spent 200 blood across four anchors and sealed the real daemon, only to hit the nine-pome rejection at culmination. The completed Silent path therefore cannot finish this late-returning lesson. The D6 rerouting chain and all alternative scar builds remain unverified. Its instruction to complete the "Fourth Degree rite" should distinguish Hematic Fortification from the degree-advancement rite already completed.

## Morphlings versus puppeteering

These should remain different choices: morphlings offer an intimate, persistent growth companion/build modifier; puppets offer prepared, replaceable battlefield bodies paid for with thread. The first real Vulture trial, Spindle preparation and call made the latter distinction understandable.

Morphling progression currently breaks at its defining promise: earned attachment to the creature does not survive the Jar handoff. Comparative higher stages therefore used explicit tier fixtures. Seven Primal actives failed, preventing a fair assessment of their intended payoff. All eight strains were actually incubated and equipped, but the first combat loop did not reset starting health/effects consistently. It cannot support a passive power ranking or a claim that one late strain is intrinsically weakest. Fix bond persistence and active dispatch before changing growth prices or tier strengths.

Puppet results below are deliberately narrower: a single Normal zombie, stationary iron-armored owner, default defensive AI, normal calls and supplied D5/shape prerequisites after one earned Vulture trial. No active owner attacks or developed Mnemonist echo loadout. The last four used a larger enclosed arena after an excluded fall death, so exact timings are not a perfectly controlled ranking.

| Form / unlock | Zombie outcome | Puppet health afterward |
|---|---|---|
| Marrow Spitter / D3 | Dead in ~6 s | 22/22 |
| Veinwing Vulture / D3 | Dead in ~8 s | 2/14 |
| Gorebound Hulk / D4 | Dead in ~7 s | 49.12/55 |
| Ringmaster Pattern / D4 | 15.39/20 at 25 s; puppet died | 0 |
| Sanguine Hound / D4 | Dead in ~7 s | 21/30 |
| Scarlet Mummer / D4 | Dead in ~13 s | 0.48/24 |
| Mnemonist Puppet / D5 | 4.01/20 at 25 s; puppet died | 0 |

Early Marrow remained useful and Hulk delivered a clear durability payoff. Higher rank did not imply better unattended protection. Ringmaster's additional-body support role and Mnemonist's configured echoes need a guided demonstration and honest preparation UI, otherwise a later purchase can feel like a downgrade. These baseline results do not establish their group/build ceilings.

Normal recall and resummoning restored a damaged Vulture to 14/14 for its ordinary 28-thread call cost. Attrition is therefore a refueling/redeployment decision. Balance thread supply, recall opportunities and battlefield uptime together, not HP in isolation. Native command-wheel interaction remains unverified because this driver's key state does not satisfy its GLFW mouse-hold check; that limitation is not a game defect.

## Fanes, NPCs and multiplayer

Actual recruitment increased Bloodwell capacity to 7,000 with two supplied NPCs, roles participated in later rites, and two real players could join and retain membership across restart. Shared centrifuge contents synchronized. The second player needed their own actual craft before access; performing it unlocked the shared station. These are meaningful working multiplayer behaviors.

The policy needs better teaching: a community station looks communal while requiring each member to repeat a personal craft. The Bloodwell's missing credit makes the same rule fail for its founder. A shared spent Hermit display supplied no second starter heart in the tested onboarding route; the campaign did not establish the intended alternative for every new member. Role/permission UI also offered nonleader actions the server then rejected. Hide or explain unavailable actions and provide an explicit new-member starting route.

Natural NPC availability was not surveyed broadly; one tested outpost is not enough to conclude a world-wide shortage. Travel assistance and temporary positioning made recruitment and assignment probes possible. Helpers are valuable but should avoid interaction rays through ritual anchors, and critical instructors need reliable location and task reminders.

Monolith lore navigation worked in its targeted normal-dialogue check, but the cornerstone grant failed and earlier room guidance ignored already completed history. These are separate issues. The Book of Observances opened with readable locked assignments, 0/14 and a missing NPC icon. Those observances belong to the **Unstained** path. Opening that book is not a test of all fourteen assignments, purification or Clarity.

## Remaining verification and evidence

This is broad live coverage with preserved blockers, not exhaustive certification. Outstanding areas include a full unaided resource/discovery run; normal Vesper victory with a competent combat route; completed advanced Artificer and Anchorite chains after their gates are repaired; all Unstained observances; native held radial/wheel usability; matched passive morphling comparisons; active multi-puppet support/echo builds; long-duration economy; and a complete dedicated-server replay after particle fixes. Death/Chamber flag consequences also need the actual chair revisit.

The evidence journal records individual attempts and exclusions. Native screenshots and action/message journals are under `build/gameplay-campaign/{solo,observer,host}/`; preserved world branches are under `build/gameplay-campaign/checkpoints/`. Scripts, crash copies, combat result JSON and client logs are under `build/progression-audit/`. These runtime artifacts are local and may be ignored by Git. Screenshot names describe intended checkpoints; the accompanying journal determines whether the action succeeded.

No production balance patch, clean full-suite pass, or unassisted end-to-end completion is claimed. The deliverable is an executable live-testing harness, preserved reproduction states and an implementation-grounded repair order. Follow-up fixes should replay the specific failed player action before broadening into balance tuning.

Final verification: `compileGameTestJava` succeeded with all six tasks up to date against the driver used in this campaign. The three operator Python files parsed successfully; report links and recorded combat JSON were checked. The final saved player independently confirms earned Varicose, diagnosis and treatment objectives, `SILENT_ARCHON`, and `hematic_fortification: false`. All three remaining campaign clients were normally stopped after saving, and their input queues are empty. This does not constitute a full unit/GameTest suite run.
