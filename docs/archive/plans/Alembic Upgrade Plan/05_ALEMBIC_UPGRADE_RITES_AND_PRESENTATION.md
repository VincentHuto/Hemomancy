# Alembic Upgrade Rites and Presentation — Implementation Steps

> **Depends on:** Documents 01–04 and the existing Cardinal Rite infrastructure.  
> **Deliverable:** Two safe utility rites, one-time Alchemist lessons, visible tier growth, secure kit recovery, and full progression validation.

## 1. Reuse the Cardinal Rite framework

The current rite anchors are `ActiveCardinalRite`, `HarbingerCardinalRiteEvents`, `CardinalRiteInteractionHandler`, `CardinalRiteStationMatcher`, `RecipeDegreeGates`, `HarbingerAlchemistDialogueTrees`, and `common.network.capa.harbinger.BloodCraftingKeyPressPacket`. The Scriptorium's subject sits two blocks from the Focus; the Alembic sits on the adjacent plinth. Add **narrow rite-ID-specific behavior**, not a second ritual controller or changed rules for rank-up rites. Recipe resources:

```text
src/main/resources/data/hemomancy/recipe/cardinal_rite/first_condensation.json
src/main/resources/data/hemomancy/recipe/cardinal_rite/sanguine_athanor.json
```

These paths are resource targets; confirm the actual current repository layout before editing. Both have `rankup: false` and explicit required degrees. The Alembic subject occupies an authored **seat beside the Focus**, not the Focus block. Capture the matched rite's rotated/mirrored local seat coordinate, subject block position and tier, exact Living Staff, offerings, inventory snapshot, and blood tank state on activation.

## 2. Safe rite lifecycle

These two recipes retain the shared floor match, Focus, Living Staff medium, anchors, daemon seal, optional/required support sigils, phase sync, and offering procession. They explicitly **disable** enemy waves, instability collapse, timed failure, and losing offerings on interruption. Wrong/inactive projection does nothing and consumes no blood. Lack of blood pauses progress. Leaving the boundary, logout, or a repairable floor disruption pauses progress; resumption keeps completed work.

Implement a rite-specific **recoverable escrow** for the exact kit and all other offerings. Do not consume offerings merely because the ordinary rite would normally commit them at sealing. Success atomically verifies the original subject tier, consumes escrow once, and upgrades the existing block entity in place. Cancellation through the existing 80-tick Blood Absorption gesture, subject loss, Focus loss, or a rejected final check safely unwinds visual entities and returns escrow plus the exact Living Staff. Already-projected blood remains spent. If the caster is offline or the area is unloaded, persist a one-time recovery claim and deliver it on return rather than dropping items where they may disappear. Distinguish `ESCROWED`, `CONSUMED`, and `RETURNED` in persisted rite state to make reload recovery idempotent.

**Invariant:** A one-time kit can end up in exactly one place: committed in the upgraded block, returned to the player/recovery claim, or held in a live rite's escrow. Never in two places, and never nowhere. Normal rank-up failure and offering rules are unaffected.

## 3. D3: Rite of the First Condensation

| Field | Requirement |
| --- | --- |
| Access | Degree 3+; ordinary Alembic; Alchemist has logged a normal distillation |
| Form | Lesser safe utility rite; one four-anchor ring; no combat waves |
| Offering | Exact one Hematic Condenser Kit in a lit brazier |
| Seat | Marked Alembic plinth beside the Cardinal Focus |
| Special work | 50 mL into intake terminal; 50 mL into return terminal, in order |
| Result | Same Alembic block entity becomes CONDENSER; old inventory/tank preserved |

The Alchemist checks degree, an ordinary distillation record, a finished potion, and two different enzyme types shown by the player; he records a one-time kit claim without consuming the shown samples. He teaches the floor/Focus pattern and the two-terminal interaction. The rite itself checks degree, subject tier, offering, and floor; a recovered kit may be used without repeating the lesson.

During anchors, send a thin red strand inward to the subject plinth. At daemon seal, show a narrow glasslike sternum/filament, then spectral condenser coils. Floor-space terminal targets must be easy to aim at, not tiny model pixels. First projection drives a meniscus through the boiler; second returns it to the receiving bulb. A wrong target is inert. The offering procession draws the kit inward, the ghost parts become solid coil/tubes/bulb, then tier changes once at the final check. No free potion is produced.

### D3 checks

- [ ] Wrong degree, wrong subject tier, no kit, wrong seat, or unmatched floor refuses activation **before escrow**.
- [ ] Four anchors and two 50 mL terminals work in the rotated and mirrored rite footprint; no hardcoded world-north click targets.
- [ ] Boundary exit or logout pauses without losing kit, Staff, inventory, or completed terminal progress.
- [ ] Cancellation/subject loss returns the same kit and Staff exactly once; successful culmination consumes the kit into station tier exactly once.
- [ ] D3 model, GUI, and ability appear together, including after save/reload; original Alembic behavior still works.

## 4. D5: Rite of the Sanguine Athanor

| Field | Requirement |
| --- | --- |
| Access | Degree 5+; CONDENSER subject; Alchemist recorded one enzyme refinement and one D3 compound |
| Form | Greater safe utility rite; two four-anchor rings; Reservoir sigil required; no combat waves |
| Exact offerings | Sanguine Athanor Kit, Sanguine Quintessence, Vivacious Enzyme, Neurotic Enzyme, Ferric Enzyme |
| Special work | 50 mL into each of three floor marks: flow → impulse → containment |
| Result | Same block entity becomes ATHANOR; previous contents retained; Catalyst 2 becomes accessible |

The Alchemist grants the Athanor Kit once after the two D3 mastery records; it is recoverable on rite abort and on later upgraded-block break. A recovered kit allows a fresh rite without another NPC grant.

As the two rings close, thin blood streams fill the D3 coils. Complete the Reservoir sigil and the normal daemon seal. Three large surface-aware floor bulbs appear within the rotated local rite frame. Only the lit next bulb accepts blood; inactive targets do not spend blood or reset work. Each 50 mL projection draws one colored tendril into the corresponding physical vessel. No enemy can interrupt the step, and completed bulbs stay lit through a pause.

At culmination, escrowed offerings draw into the bulbs; the daemon's red stream passes through the old Condenser coil and divides into three, then rejoins in the upper crown. Apply the tier change only after the last server check. Show the taller crowned glasswork, asymmetric bulbs, animated channels, and second-catalyst GUI together. The original Alembic footprint/collision remain; renderer bounds include the taller art without clipping or leaking into unrelated blocks.

### D5 checks

- [ ] Base and already-ATHANOR stations refuse activation; only CONDENSER at the authored seat passes.
- [ ] All five offerings are escrowed and returned exactly once on cancellation, subject/Focus loss, death, or rejected final validation.
- [ ] Eight boundary anchors, Reservoir sigil, and three floor projections obey rotated/mirrored local coordinates and persist across logout.
- [ ] No ordeal enemy, instability failure, destructive cancellation, or timeout fires in either upgrade rite.
- [ ] D5 culmination keeps the same tank/inventory, exposes Catalyst 2, and returns the original Living Staff.
- [ ] Breaking an ATHANOR gives one Alembic block, one Condenser Kit, and one Athanor Kit; reinstalling needs D3 then D5 rites but no repeated NPC claims.

## 5. Presentation and dialogue

Keep one base block ID and three tier-render variants:

- **Base:** current silhouette and functions.
- **Condenser:** two return tubes and a receiving bulb visibly added, wet pulsing blood at the coil.
- **Athanor:** D3 lower machinery retained beneath three upper distillation bulbs and a crown. The third visible vessel corresponds to Catalyst 2.

Ghost parts appear only during the active rite and dissolve on safe abort. They become the installed model parts only on server-confirmed success. Expose current tier, recipe operation, exact cost, blocked reason, and live rite progress through synchronized UI/world cues. Dialogue says what the player must bring and what the rite does in plain language; one-time reward claims cannot duplicate through repeated conversation.

## 6. Integration gate and acceptance run

1. Place an old-world BASE Alembic; check distillation, cured flask filling, gourd filling, hopper behavior, blood seep, and Pallid Retort recipes.
2. Complete the D3 lesson and rite; cancel once mid-rite and confirm kit/Staff return, then complete it and brew Ferric Poise plus one two-effect compound.
3. Complete the D5 lesson and rite; log out mid-bulb, resume, safely abort once, complete it, and bind/refill a three-effect vessel.
4. Use the vessel with controlled random outcomes: charge-spend path and 1,000 mL blood-success path; check zero-dose behavior and Mnemonic Whispers backlash.
5. Break and reinstall the station; confirm one of each installed kit, rite tier order, preserved menu slot gating, and no NPC-reward duplication.
6. Run the project’s relevant test and resource validation commands from the real 1.21.1 repo. Record the commands and output in the implementation PR; do not claim tests passed from this design packet alone.
