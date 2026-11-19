# Chamber ordeals: the Strongman pilot

2026-11-17. Moves a Crimson Troupe puppet-body ordeal from the open world into a private stage in the Chamber of Will,
with the professor intervening from outside the ring and both drawn as their "true selves". Pilot: the Strongman and
the Gorebound Hulk, end to end; the other six professors follow on the proven pattern after the owner judges it.

## Intent (the owner's words, 2026-11-17)

- Ordeals in the open are messy and crowded. Inside the Chamber of Will they are private, and they can show the
  performers' mental (Noetic) power and carry effects the world cannot.
- The **puppet is the main combatant**; beating it is still the pass.
- The **professor floats outside the arena**, above and around it, out of reach, and **intervenes from outside** as
  the fight goes on, as added challenge.
- Tone: **a teacher who will not coddle**. The player is at least a Vicar, no novice, but these are masters of one
  craft who will not see it mistreated or taught to just anyone. Fair, telegraphed, unrelenting; they judge rather
  than praise.
- Being drawn into their Chamber shows their **true selves**: the professor and the puppet are drawn with different,
  more dramatic, more visceral models than their physical forms; the puppet wilder and less tamed.

## Goals

- The Gorebound Hulk ordeal runs on a Strongman stage in the Chamber of Will, private to the player.
- The Strongman, invulnerable and out of reach, circles above the ring and intervenes with telegraphed moves that
  ramp as the Hulk weakens.
- The Strongman and the Hulk wear true-self models while in the ordeal.
- The Strongman speaks at the start, at each ramp, and at the end (judgement), in his voice sheet.
- A shared ordeal framework the other six professors plug into with a stage, an intervention table, two true-self
  models and lines.

## Non-goals

- The other six professors (after the pilot is judged).
- Any change to what the ordeal unlocks, its recipe, cost, Degree gate or the practical that follows.
- Multiplayer helpers in the stage (the ordeal is the caster's alone).

## Anti-goals

- An intervention that cannot be read or dodged (every one is telegraphed on the troupe's move kit).
- A professor who can be hit, pathed to or cheesed from the arena.
- A true self that is a recolour of the physical model (owner rule: each is its own generated model).
- Vanilla particles; copper, brass or gold on the troupe.

## Constraints

- The ordeals run at Degree 3 to 5, before most players own a Chamber room (D6). The stage is not the player's room:
  it is its own region of the `hemomancy:chamber_of_will` dimension.
- The visit uses `ChamberVisitService`'s existing safety: inventory and crafting snapshots, cursor safety, return on
  death or logout, observational-visit rules for pending deliveries. A new `ChamberVisitMode.ORDEAL`.
- Rite kinds behave through `RiteKind` hooks (`RiteKindGuardrailTest`); start with `CardinalRiteActivation.tryStart`.
- Boss-kit timing: `BossMoveTimeline`, `BossReplayClock`; footprints through `CircusEffects`.
- Text through `en_us.json` keys; the Strongman's lines follow `docs/NPC_VOICES.md`.

## Approaches

- **A. The professor as a real entity on the troupe's move kit (chosen), with true-self rendering (owner's
  addition).** The professor is a `CircusFacultyEntity`, invulnerable and on a path above the ring; interventions are
  `BossMoveTimeline` moves with `CircusEffects` footprints, from a per-professor table. The ordeal's professor and
  puppet carry a synced true-self flag; their renderers swap to separate generated true-self models.
- B. A director with a client-drawn figure. Rejected: the figure and its hazards would have to be kept in step, and the
  performer moves, poses and telegraphs would not be reused.
- C. One generic arena for all professors. Rejected: works against showing each performer's craft.

## Decided

**Flow.**
1. The player performs the Gorebound Hulk Ordeal as now. When the rite reaches the moment it would manifest the
   puppet, it instead begins an `ORDEAL` visit: the player is drawn into their Strongman stage, the rite on the floor
   holds (a `RiteKind` hook), and the Hulk manifests on the stage.
2. The fight runs on the stage. The Strongman opens with a line, then circles and intervenes.
3. **Win:** the Hulk is beaten; the Strongman gives his judgement; after a short beat the player returns to the rite
   floor and the rite completes as it does today (the body unlocked).
4. **Loss:** the player dies or is beaten down (the Chamber's existing return), logs out, or the stage times out: the
   player returns, the rite fails as a failed trial does today, and the Strongman's line says why.

**The stage.** The Strongman's Iron Yard, authored by the circus generator (`tools/worldgen/circus/ordeal_stages.py`):
a ring of radius 9 on a floor of chalked iron and bone, weight racks and plinths round the rim beyond reach, sinew
cables up into a vascular dark, the Chamber's own sky. One stage per player, placed in the Chamber dimension on a
grid apart from the rooms (its own row of `CHAMBER_SPACING`-wide cells, keyed by the player's chamber id). Built
fresh at the start of each ordeal; the player cannot leave the ring's floor (the Chamber's movement clamp, reading
the stage's shape).

**The Strongman's interventions (pilot table).** The Hulk's health sets the stage of the fight: from full to
two-thirds one move at a time, from two-thirds to a third two in turn, below a third three and quicker. Each has the
kit's wind-up floor (20 ticks for an area move) and a footprint.
- *Dropped weight:* a phantom iron weight falls onto a marked circle (radius 2) near the player.
- *Ground heave:* he strikes the rim; a ring wave runs inward across the floor; jump it.
- *Bar sweep* (from two-thirds): a bar the length of the ring sweeps across at knee height; jump or duck the line.
- *Hold the line* (below a third): he grips the cables and the floor tilts toward the Hulk for 3 seconds, a pull
  the player walks against.
Damage is set so a player who reads them takes little and one who ignores them is punished; the numbers are the
builder's within that.

**True selves.**
- *The Strongman:* towering and theatrical, flayed-crimson muscle under a torn strongman's leotard, iron weights
  grown into his fists, a moustache of bone wire, cables of sinew running from his back up into the dark. Drawn
  half-lit, with a crimson rim; never fully solid.
- *The Gorebound Hulk:* wilder than its physical form: its yoke burst, chains trailing, more mass on the shoulders,
  steaming blood-vents, a lower, more animal stance.
- Both from the troupe's rig generators (`tools/circus/`), on the jointed rig, at 16 texels a block. The physical
  models are untouched.

**Voice.** Six to ten Strongman lines: an opening, one per ramp, the judgement on a win, a dismissal on a loss.
In his sheet's voice; the judgement acknowledges the craft, not the player.

**Code home.** `common/encounter/ordeal/` (the shared framework: `OrdealStage`, `OrdealDirector`,
`OrdealInterventions`), `common/entity/npc/circus/` for the professor's ordeal moves, the true-self flag on the
existing entities, `client/` renderers choosing the model.

## Stakes (owner ruling, 2026-11-17)

- **Real stakes.** The player fights with their real inventory: potions, arrows and durability spent stay spent. A
  killing blow does not kill: it is caught, the player is thrown out of the Strongman's mind with nothing dropped, and
  the rite fails. A failed attempt is punished once, not twice.

## The key: the professor's blood (owner request, 2026-11-17)

- The ordeal's brazier offerings gain a **Blood Vial holding the professor's own blood** (`BloodVialItem` with
  `entity_type` `hemomancy:circus_strongman`), alongside the Gorebound Yoke and the Sanguine Quintessence. It is the
  key to his mind and is consumed by the rite.
- *(My calls.)* The professor gives the vial when he teaches the lesson (the instruction that reveals the recipe).
  After a failed ordeal he gives another when asked, once per in-game day: he will not coddle. A player cannot draw
  it from him with a syringe; only his gift fills it. The brazier ingredient matches the vial by its recorded entity
  type (a component-matching ingredient), so any other Blood Vial does not fit.

## Builder-check calls (mine, 2026-11-17)

- **Whose Chamber.** The stage is the professor's Chamber of Will, not the player's: the player is dragged into the
  Strongman's mind. So it counts for none of the player's own Chamber progression: no room growth, no sky theme of
  theirs, no attunement, no Chamber Liber pages or advancements, and the systems that read
  `ChamberVisitService.isActive` for the player's own room ignore an `ORDEAL` visit. The stage carries its own look.
- **No open-world fallback.** The Hulk ordeal always runs on the stage. When a stage cannot be used (a Chamber visit
  or projection already running, the dimension missing) the rite refuses to start, with a line saying why.
- **The Strongman in the stage** is an ordeal-only true self spawned for the fight, not the faculty entity the player
  met; he exists only there. His lines may read what the player was taught (`CircusApprenticeshipProgress`); a win
  records nothing beyond what the rite already records.
- **The floor while away.** The rite on the floor is held, its chunk kept loaded by a ticket for the length of the
  ordeal, and settled when the player returns. If the floor or Focus is broken meanwhile, the player is pulled back
  and the ordeal fails. The rite's existing rule that a caster in another level snuffs it gives way to the hold.

## Left to the builder

- Exact intervention numbers, cadence within the ramps, stage dimensions within the description.
- How the stage cell is addressed beyond "its own grid, keyed by chamber id".
- The professor's flight path shape and height.

## Verification

- Unit tests for the pure rules: ramp stage from health, intervention pick order, stage-cell addressing, the win and
  loss outcomes.
- GameTests: the ordeal visit begins, the Hulk is beaten, the rite completes and the body is unlocked; a death and a
  logout inside return the player with inventory intact and the rite failed.
- A review client scene: the stage, the Strongman's true self circling, each intervention's telegraph, the Hulk's
  true self, a win and a loss.
- Docs: `docs/CIRCUS.md`, `docs/CARDINAL_RITES.md` (the ordeal's new flow), `docs/HEMOMANCY_REFERENCE.md` 17.1, the
  Chamber section for the new visit mode, `docs/OPEN_QUESTIONS.md` for the other six professors.
