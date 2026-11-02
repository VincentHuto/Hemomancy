# Vesper Fight Overhaul — Design

Date: 2026-10-16
Status: draft for approval

Vesper is male. Use he/him in code, docs, lang and comments. Fix any stray "her" for Vesper you touch (for example, the puppet appearance paragraph in `docs/HEMOMANCY_REFERENCE.md` §puppeteering says "her puppets").

## 1. Intent

Both Vesper phases work, but they have built up update debt and fall short of the recent Annetta Knowles rebuild. Phase 1 (the Crowned Refusal, Vesper riding his mount) is the weaker of the two:
- attacks cut off early or come with no telegraph
- movement and target tracking stall
- the flight half feels stiff
- the weak spot is a frozen boss with a tiny orb
- the puppet musters predate the current puppet system

Phase 2 (the Evening Star) is pretty good, but its attacks are isolated beats rather than a fighter's flow. It also uses only two manipulations per school.

The fight should feel **fluid, readable and escalating**:
- Each attack is telegraphed and plays out in full.
- The boss moves like a creature, not a state machine.
- The player's choices in phase 1 (which puppets they kill) shape phase 2.

Success: playing the full ordeal end to end, nothing freezes, nothing cuts off, each big hit is readable before it lands, the mount reads as a huge insect, and the three phase-2 acts feel distinct.

## 2. Goals, non-goals, anti-goals

**Goals**
1. Phase 1 stability: one controller owns movement, facing and the attack timeline. Attacks always finish or wind down cleanly.
2. Phase 1 telegraphs: every attack has a readable wind-up using the Hemomancy/HutosLib suite.
3. Fluid flight: banked arcs, eased acceleration, committed dives.
4. New **Burrow** attack (ground version and dive-entry version).
5. Weak spot reworked into a **wound drained with Blood Absorption**.
6. Puppet **roster** system using all current shapes, with carry-over into phase 2.
7. Mount rebuilt as a generated model with insectoid limbs and large wings. Vesper the rider keeps his current model.
8. Phase 2 in three acts (Locked, Open, Sickles) with chained movesets and cross-school manipulation casting.
9. A review client to look at and drive the fight.

**Non-goals**
- Changing the ordeal entry, retry, owner binding, victory, Memory of Vesper reward, Bloom seal or Silent Archon finalization (`VesperOrdealManager` contracts stay as they are).
- Rebuilding Vesper's own rider model or the Evening Star model geometry.
- Rebalancing player manipulations. Boss-side variants may be tuned separately.
- Changing the arena layout or floor renderer beyond the burrow fissure overlay.

**Anti-goals (failure even if it "works")**
- Damage the player couldn't have seen coming.
- Vanilla particle clouds dominating focal moments (AGENTS.md visual rule; Hematic Riposte is the bar, see `docs/MANIPULATION_VISUALS.md`).
- The boss freezing in place (AI off) as a "vulnerable" state.
- Puppets that pop in or vanish instead of descending and unspooling.
- Any save that leaves Vesper permanently stuck (persisted `NoAI`, an orphaned attack state).
- Copper, brass or bronze accents on the mount (blood-side models: hematic iron, bone, lacquer, chitin).

## 3. Constraints

- NeoForge 1.21.1. Registries via `DeferredHolder`. Payload networking per `PacketHandler`.
- Pure rule classes for timing and selection, unit-tested like the existing `Vesper*Rules` and the Annetta `*CombatRules`.
- Old saves: mid-fight Vesper entities from before this change must load into a sane state (fall back to IDLE / grounded / no active wound). No inferred progress.
- Puppets go through the shared lifecycle: `PuppetStrings.descend` / `unspool`, the control-thread renderer, and the trial or bound behavior. No second summon registry.
- Docs updated with mechanics: `docs/HEMOMANCY_REFERENCE.md` §5.10 and the puppeteering section, plus `docs/LORE_REFERENCE.md` if any lore line changes.

## 4. Phase 1 — the Crowned Refusal

### 4.1 Controller (stability)

Replace the vanilla goal soup (`MeleeAttackGoal`, `MoveTowardsTargetGoal`, stroll, look goals) with a single **mount controller** that every tick decides:
- **locomotion:** approach, strafe, hold, reposition or fly
- **facing:** turn-rate-limited body yaw toward a desired heading
- **the active move:** a timeline of named stages

Keep only the float goal and the target selectors.

- Every move is a timeline: `wind-up → active → recovery`. Stage boundaries live in pure rules (the existing `VesperMountAttackRules` style).
- **Interrupt policy:** higher-priority events (wound opening, wing growth, transition) never hard-cut a move mid-active. They queue and take over at the next recovery boundary, or force a short authored **stagger** (a mini-recovery) if the move is in wind-up. The one exception is the death transition, which plays a stagger.
- A missed grab finishes its own whiff recovery instead of skipping ahead.
- Navigation is issued by the controller only. No per-tick `stop()` from attacks.
- Tracking: during wind-up the mount tracks the target at a limited turn rate and then **locks** its aim at the commit tick, so the player can sidestep a committed lunge.
- The vulnerable state never uses `setNoAi`. Remove existing `NoAI` from loaded Vespers.
- Target loss (target dead, creative or spectator, out of range): finish the current move, then patrol the arena centre. No stalling.

### 4.2 Telegraph pass (existing ground moves)

Keep the existing ground moves (Royal Scuttle, Pincer Vice, Stinger Script, Brood Trample, Carapace Aneurysm, Grab Impalement) but give each one:
- a body-language wind-up in the new animations
- a ground telegraph in the custom suite: fissure lines, rings, glow motes on the floor
- a sound cue at the wind-up start

The Stinger Script spikes show their spawn points before they erupt. The Puppet Muster is replaced by §4.6.

### 4.3 Flight

Wing growth stays at 50% health, as an authored flare and shiver of the new wings.

Flight movement becomes velocity-based steering with:
- a max turn rate
- acceleration and deceleration
- body **bank** (roll) into turns and pitch into climbs and dives, synced for the model

Circling follows smooth arcs around the target, not a point snapped every tick. Sortie attacks:
- **Diving Rend** (existing): hover-brake wind-up with wings cupped, then a committed straight dive at the locked point.
- **Tail Fusillade** (existing): a strafing pass, not a hover.
- **Dive-Burrow** (new): see §4.4. A Diving Rend can commit *into* the floor and become a short burrow.

Landing is a flared, wing-braked touchdown with a skid, then grounded recovery.

### 4.4 Burrow (new)

**Ground Burrow.** A ground move, selectable from the grounded rotation once wings exist, or from ~70% health. Builder decides the exact gate.
1. **Dive (wind-up).** Vesper leans forward and tucks low on the throne, grabbing the mount by the neck with his free hand. The mount rears and plunges headfirst into the floor. Burst of floor-chips and blood motes, then the fissure opens.
2. **Hunt (10–15 s).** The mount travels under the floor toward the player. It is invisible and non-collidable, with its position server-authoritative. A **fissure trail** follows its true position: cracks drawn on the arena floor plus floor-stone chip particles and dark glow welling from the cracks. Every few seconds it surfaces a strike under or near the player, alternating:
   - **Claws:** a raking upthrust that deals armor-ignoring damage.
   - **Tail:** a stinger upthrust that applies poison and blood loss.
   Each strike has its own tell: the trail stalls and swells for ~0.5–0.75 s, then the strike erupts there. The claws or tail are visible above the floor for the strike.
3. **Rumble.** The trail goes quiet and the particles stop. A screen shake builds on players in the arena, with a rising low rumble. This lasts long enough to react (~1.5–2 s). The eruption point locks on the player's position at a fixed moment during the rumble, so moving away after the lock dodges it.
4. **Eruption.** Vesper and the mount burst out of the floor at the locked point. A hit deals massive damage and a strong upward launch.
5. **Recovery (~3–4 s).** Vesper gathers himself, standing exposed. This is the punish window for good dodgers and the recovery window for players who were hit.

**Dive-Burrow.** The same cycle from a flight Diving Rend: a straight dive into the floor, then a 5–7 s hunt (one or two strikes), rumble, eruption, recovery on the ground. This ends the sortie.

**Floor and visuals.** The arena floor is barrier blocks drawn by `VesperFightFloorRenderer`. Burrowing is a visual and logical effect, not real block breaking. The fissure is a client overlay on the floor surface (extend `VesperFightFissureStyle`/`VesperFightStoneSurface`), fed by synced trail data. Chip particles are a custom floor-stone-coloured Hemo particle, not vanilla block debris, because barrier debris would be wrong. Camera shake reuses the shake approach of `CardinalRiteImpactClientState`, driven by a new payload. Never shake players outside the arena.

**Summoned (non-ordeal) Vespers.** These burrow too, with the recorded flight-arena centre as their arena. They have no custom floor, so they get the particle trail and chips only (no overlay). The shake reaches players within the arena half-extent of that centre.

**Stability.** Burrow state persists. If a save or reload happens mid-burrow, the mount surfaces at its current position into recovery. The arena clamp keeps the trail inside the safe half-extent.

### 4.5 Weak spot → the Wound

The three anchors (at 72%, 42% and 12% health, as now) become three distinct **wounds** at different places on the mount. My call: a carapace split behind the throne, a throat or neck seam, and the abdomen under the tail base. The rider and throne stay intact.

- **Opening.** Reaching a threshold opens the wound on its own; no player "heavy hit" is needed, so no build can soft-lock against the health floor. The opening happens at the current move's next recovery boundary, or immediately if the mount is idle. The mount staggers and shrieks, the scute splits, an exposed pulsing core shows, and blood wells. The health floor still holds until the wound is drained, as today.
- **Drain.** Players channel **Blood Absorption** on the wound. Route this through the same hook the Evening Star's defeat uses (`VesperBloodAbsorptionInteractions`), extended to the Crowned Refusal's active wound. A visible blood stream flows from the wound to the player's hand or staff. Absorption drains a wound meter. My call: draining also feeds the player a little blood, like normal Absorption.
- **Hits** on the wound keep it from scabbing. As a soft-lock fallback (for a player with no Absorption tool once every puppet is struck), hits also drain at 10% of the Absorption rate. Absorption remains the real answer.
- **He fights back.** Vesper does not freeze. While a wound is open the mount uses a reduced, wound-guarding rotation: it turns to shield the wound, uses tail sweeps and bites aimed at the drainer, and shakes them off. Staying on the wound is a skill test.
- **Scabbing.** If no one drains or strikes the wound for a few seconds, it begins to scab over and slowly loses part of its progress, down to a floor (not all). The wound never fully closes while its threshold is active, so it can't soft-lock.
- **Puppet backlash.** Killing one of Vesper's puppets while a wound is open tears it: a chunk of drain (the existing 10-of-40 equivalent) plus a scab reset. This rewards dealing with puppets.
- **Break.** A full drain ruptures the wound in a big custom burst, and the mount weakens (the health floor moves to the next threshold). The third break starts the transition, as now.
- **Eligibility.** The owner-only rule matches the ordeal: summoned (non-ordeal) Vespers accept any player.
- **Old saves:** an `ActiveThroneAnchor` loads as an opened wound with its stored progress.

### 4.6 Puppet roster

Vesper owns a **roster** of seven puppet shapes: Veinwing Vulture, Marrow Spitter, Gorebound Hulk, Scarlet Mummer, Sanguine Hound, Cinder Bellows and Mnemonist Puppet. The **Ringmaster Pattern is excluded** (user decision). Vesper is the conductor here.

- **Muster:** a telegraphed move. Vesper raises the Crossbar and strings drop. He calls up to 3 shapes at once (any mix), arriving via `PuppetStrings.descend`, with control threads from his Crossbar (existing Vesper thread rendering).
- **Killed = struck.** A shape the player kills is removed from the roster for the rest of the fight and never called again. The kill also tears an open wound (§4.5).
- **Recall.** Shapes that survive long enough may be recalled by Vesper (the unspool exit), return to the roster, and can be called again later. The total out at once is still 3.
- **Coverage.** The muster scheduler prefers shapes that haven't been on the field yet, so every shape gets a turn before repeats.
- **Lifecycle fix.** Vesper's puppets must not run through the player-owner path of `BoundSummonBehavior` (today `ownerUUID = boss UUID` makes `ownerOnServer` empty, so they unspool). Use the trial or encounter path, with Vesper as the controller: targeting the ordeal owner, leashed to the arena, drops and XP suppressed (keep `VesperEncounterPuppetEvents`). End of phase 1 unspools every puppet still out; it does not discard them.
- **Persistence.** The roster state (per shape: available, out, or struck) lives on the boss NBT and is copied to the Evening Star through `VesperOrdealManager.copyOrdeal` (or alongside it).

**Carry-over.** Every shape **not struck** when phase 1 ends grants the Evening Star that shape's borrowed attack (§5.4). This includes shapes that were never called.

### 4.7 Transition

Keep the cocoon transition. Update it for the new mount model and make sure no puppet, burrow or wound state leaks into it. The forced stagger leads into the cocoon.

### 4.8 Mount model rebuild

The mount is split from the rider:
- **Rider:** Vesper's current Blockbench parts and texture stay. He still needs new rider poses for the neck grab and tuck, Crossbar raise, wound recoil, and flight lean.

**Animation authoring (user decision):** all new motion, for both phases, is written in Java, procedural or keyframe, the way Annetta's `_anim.java` files work. The existing Blockbench-exported `VesperTheCrownedRefusalAnimations` / `VesperTheEveningStarAnimations` stop being the source of truth for anything new. Reuse, replace or delete their clips as needed, and don't keep the `.bbmodel` animation tracks in sync. Model geometry for Vesper and the Evening Star is unchanged.
- **Mount:** a new generator script `tools/model_export/vesper_mount.py` with a `vesper_mount_anim.java` beside it, emitting the mount model Java and its atlas, Annetta-style (see `tools/model_export/stained_priestess.py` and `annetta_kit.py`). The script also writes a `--preview` sheet.

Mount design requirements:
- **Insectoid limbs:** segmented legs (coxa, femur, tibia, hooked tarsus) driven by procedural gait code. Alternating tripod gait, overshoot and settle on the joints, claw twitches, and continual small idle motion. Pincers and tail articulated the same way. Nothing reads as a stiff box swinging on one hinge.
- **Wings:** large paired insect wings (two pairs) with translucent veined membranes spanning well past the body. They fold flat along the back when grounded, flare and shiver on growth, beat as a blur in flight, and cup and brake for dives. They can use a procedural membrane layer like the Priestess webbing if boxes can't sell it.
- **Head and neck (new):** the current mount has no head. The rebuild adds a neck and a carapaced head with mandibles between the claw arms, giving Vesper a real nape to grab for the burrow.
- **Anchors** for the rider seat, the neck grab point, the three wound sites, the claw and tail strike tips, and the throat for the burrow dive.
- **Palette:** the existing red/black, chitin, bone and lacquer. No copper.

The user reviews renders of the new mount before it goes into the fight.

## 5. Phase 2 — the Evening Star

### 5.1 Acts

| Health | Act | Behavior |
|---|---|---|
| 100% → 67% | **Locked** | One weapon per stance for the full stance timer. Each weapon has a **chained moveset** (openers, follow-ups and finishers, chosen by range and the player's response). Manipulations come only from that stance's school. |
| 67% → 33% | **Open** | Swaps weapon and school mid-animation, at authored blend points, and casts **any** school's fight manipulations. Authored cross-school **synergy chains** (examples below). |
| 33% → 0 | **Sickles** | The dual-sickle act, moved from 30% to 33% and fully expanded (§5.5). |

Puppet-borrowed attacks (§5.4) are available in all three acts.

### 5.2 Flow

- Each weapon action defines exit stances, and the next action is chosen from those that can start from that pose, so chains blend instead of resetting to idle.
- Stance and weapon morphs happen as part of an action (a morph-strike) instead of the separate 30-tick freeze (`isMorphTelegraph`) that currently cancels the action.
- One combat controller replaces `tick` / `tickRage` duplication, with a per-act selector.

### 5.3 Manipulation casting

Players cast through each manipulation's own `getAction` code. Entities cast through `EntityManipulationEffects`, a separate, simpler copy covering 32 manipulations, and that copy is shared with Wills through `WillManipulationCaster`.

**Decision (my call):** Vesper gets his own boss caster (e.g. `VesperManipulationCaster`). It delegates to `EntityManipulationEffects` for the 32 it already covers and implements the rest Vesper-side. It reuses the player manipulations' visual helpers and particle and payload paths, so the casts *look* like the real thing. The shared `SUPPORTED_CASTS` list and Will behavior stay unchanged, and player manipulation classes aren't refactored. Where an existing entity copy is visibly weaker than the player version (e.g. `umbralStep` as a plain teleport with the Enderman sound), the Vesper caster upgrades it to the player visuals. Scope: every manipulation that makes sense in a fight (offense, defense, movement, control). Utility, station, item, summon and player-aim-only manipulations stay excluded. A boss-only variant is allowed where a manipulation needs one (for example, a short-range Venous Travel blink), but it isn't required.

The builder produces a coverage table (manipulation → castable / boss-variant / excluded with a reason) in the reference doc. Example synergy chains for the Open act:
- Ferric Sanguine Magnetism (pull, iron tag), then a Ductilis bolt through the magnetized target
- Congeatio Glacial Grasp, then a Mortem shatter or Exsanguinate
- Flammeus Sanguine Ignition, then a Lux Hematic Flare detonation
- Tenebris Umbral Step behind the player, then an Animus blade string

### 5.4 Puppet-borrowed attacks

One authored Evening Star attack per surviving shape. Proposed (builder may refine):

| Shape | Borrowed attack |
|---|---|
| Gorebound Hulk | Thread-yank: Crossbar threads haul the player in, into a ground slam |
| Marrow Spitter | Lobbed marrow volley that leaves short-lived caustic pools |
| Veinwing Vulture | Wing-assisted leap and dive strike from above |
| Scarlet Mummer | Mummer decoys: afterimages that draw attention, then burst |
| Sanguine Hound | Loosed Blood Curs that chase briefly, then rupture |
| Cinder Bellows | Ember breath cone |
| Mnemonist Puppet | Replays the biggest hit the player dealt him recently as a thread lash |

These attacks use the custom suite and are telegraphed like everything else. The borrowed attacks a phase-2 Vesper has are shown to the player at his awakening: a brief puppet-sigil flare per inherited shape.

### 5.5 The Sickle act

**Today:** five isolated moves (Cyclone, Pounce, Cross Rend, Hook, Crescents) chosen by distance with a no-repeat rule. Between moves he strafes, and the strafe direction flips every 6 ticks, which reads as jitter. The Hook pulls the player in, but nothing follows it up. Spins are a fixed 42° per tick with no wind-up or momentum.

**Goal:** the sickle act is Vesper at his most fluid, a dual-wielding reaper whose pulls and spins feed each other. It draws on the player's own two-mode Living Sickle (Short Reap / Blood Hook), so a player who later earns the sickle recognizes his style.

**Movement between strings.** He circles with committed direction: he picks a side and holds it for 1–2 s, then switches with a visible plant-and-pivot. He closes the gap with step-ins and backsteps, not a sideways shuffle. Sickles stay in motion (flourishes and blade-flicks) so idle never looks frozen.

**Momentum spins.** Spins accelerate into rotation and decelerate out, and the build-up is the tell. A spin's exit direction and speed carry into the next move: a spin can slingshot into a pounce, or bleed out into a backstep. Spin rate, travel and duration vary per variant.

**Strings.** Each string has an opener, branch points and exits. At every branch point he continues, branches or stops, based on:
- whether the last hit landed
- whether the player is shielding
- the player's distance
- which way the player moved

| String | Opener → branches | Notes |
|---|---|---|
| **Reaping String** | Alternating left/right reaps, 2–5 hits of varied length and rhythm (a delayed hit, a double-quick pair) → ends in Cross Rend, a spin-out, or a hook-throw as the player backs off | Bread and butter. Rhythm variation is the point |
| **Hook & Reel** | Blood Hook thrown (with a glint) → on a catch, reels the player in, then one of: **Reel-Cyclone** (spin while reeling, so they're pulled into the blades), **Reaper's Rise** (a rising cut that launches), or **Pass-Through** (he reels *himself* past the player and cuts from behind) | The pull becomes a combo starter, not an end. A miss recovers into the Reaping String |
| **Gallows Wheel** | Both hooks thrown wide, tether lines crossing the player's position → he spins and the lines sweep and tighten around him like a wheel | Jump or duck the lines. Readable tether visuals |
| **Cyclone family** | **Travelling Cyclone** (tracks the player at a limited turn rate), **Reverse Cyclone** (a slow vortex drawing the player in, then an outward burst), **Feint Cyclone** (a short spin cancelled into a pounce) | Same opener pose, different tells per variant |
| **Pounce family** | **Pounce** → landing spin, or a **short-landing feint** → instant hook | Pounce now has follow-ups |
| **Crescent family** | **Fan** (spread), **Staggered** (timed volleys), **Ground-skimmers** (low crescents to jump), **Returning crescent** (boomerangs back through him) | Ranged pressure with varied dodge answers |
| **Anchor Hook** | Hooks the arena floor and slingshots to reposition, ending in a pounce or a crescent | Mobility; gets him out of corners and stops him stalling |

**Shield answer.** A caught hook rips a raised shield aside (disabling it), then the reel follows. Shield turtling stops being the dominant answer to the act.

**Counterplay.** Breaking a reel (my call): damaging Vesper enough during the reel, or sprinting away against it for long enough, snaps the tether and staggers him briefly. Every string ends in a readable recovery, and longer strings end in longer recoveries.

**Last stand (below ~10%).** Once only, he chains **Harvest**: a hook, reel, Reel-Cyclone, Cross Rend and a returning-crescent volley, then the longest recovery of the fight. It is a big, readable climax and a punish window.

**Presentation.**
- Dual-wield animation is written in Java (per the authoring decision): both arms are independent, and the off-hand sickle anticipates and follows through.
- Spins show motion trails on the blades.
- Hooks and tethers draw as living blood tendrils (the existing `LivingSickleHookEntity` tendril), with reel tension shown on the line.
- Puppet-borrowed attacks (§5.4) remain available but are rarer in this act, so the sickles are the focus.

## 6. Review client

Add `runVesperReviewClient`, following the Annetta review client pattern. It needs:
- still scenes: mount turnaround, wings folded / flared / flying, burrow fissure, each wound open, Evening Star acts
- live scenes: phase 1 against an invulnerable target, burrow cycle, flight sortie, muster, phase 2 per act, plus each sickle string (with a shielding target for the hook rip)

## 7. Testing

- Pure rules tests for:
  - controller stage timing and interrupt policy
  - burrow stages, strike alternation and eruption lock
  - wound open / drain / scab / break
  - roster scheduling, struck, recall and carry-over
  - flight steering limits
  - act thresholds
  - phase 2 chain graph validity
  - sickle string branch selection, momentum carry-over, reel-break rules, and Harvest firing once
  - manipulation coverage table vs registry
- Update the source-contract tests that the change invalidates (`VesperCombatImplementationSourceTest` and siblings) rather than deleting coverage.
- GameTests where they exist (`VesperOrdealRecoveryGameTests`): ordeal recovery still passes, and new save/reload mid-burrow and mid-wound fixtures pass.
- `./gradlew.bat build`, `test` and `alphaCheck` pass. Final visual proof comes from the review client.

## 8. Left to the builder

- Exact tick timings, damage numbers and gates within the spirit above.
- The internal shape of the controller and chain graph.
- Wound site placement, within the "distinct places on the mount" rule.
- Which manipulations need boss variants.
- How the work is staged across the plan. Suggested order: phase 1 controller and telegraphs → mount model → flight → burrow → wound → roster → phase 2 acts and flow → manipulation coverage → borrowed attacks → docs.

## Calls I made (for the user to check)

- Burrow strikes each get their own short stall-and-swell tell. The eruption point locks during the rumble.
- Burrow visuals are a floor-overlay fissure plus custom chip particles (the floor is barrier blocks, so no real debris).
- Wound sites: carapace behind the throne, neck seam, under the tail base.
- Hits keep a wound open but only Absorption drains it. Draining feeds the player a little blood. Abandoned wounds scab and lose part of their progress.
- The mount fights back while wounded instead of freezing.
- Muster scheduling prefers uncalled shapes. Phase 2 shows inherited shapes as sigil flares at awakening.
- The sickle act starts at 33% (was 30%).
- Sickle act: committed-side circling, momentum spins, the string roster in §5.5, the shield-rip hook, tether breaks from damage or sprinting, and a once-only Harvest last stand below ~10%.
- A "chained moveset" means 3–4 moves per weapon (opener, 1–2 follow-ups, finisher), up from 2 today.
- Hits drain wounds at 10% of the Absorption rate as an anti-soft-lock fallback.
- The rebuilt mount gains a head and neck (it had none).
- A wound opens by itself at its threshold (no soft-lock), scabs only down to a floor, and a puppet kill tears it (a drain chunk).
- Vesper gets his own manipulation caster. Wills and the shared entity cast list stay unchanged.
- Summoned Vespers burrow with particles only, with no floor overlay.
- The borrowed-attack concepts in §5.4.
