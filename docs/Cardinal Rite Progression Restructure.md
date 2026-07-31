# Cardinal Rite Progression Restructure

## Status

This document is the design contract for the Harbinger Cardinal Rite progression pass.
It covers Degrees 0-7. Apotheos and Degree 8 remain a separate pass because the
player's relationship to rites changes after Apotheosis.

## Problem

The earlier ceremony redesign made every rite use nearly the entire ritual
vocabulary at once: a constructed floor, a Cardinal Focus, offerings, boundary
anchors, sigil inscription, fog, refreshes, ordeals, helpers, and severe failure.
That produced impressive individual ceremonies but a poor learning curve.

At initiation the player does not yet know Blood Absorption, Blood Projection, how
to make a Cardinal Focus, or how to use a Living Staff. Requiring all of those
systems before teaching any of them turns the first rite into an out-of-game
research problem.

The redesign therefore follows one rule:

> A rite may use only mechanics the player has already learned, and each new
> mechanic must first appear in a ceremony simple enough to teach it clearly.

Later rites may intentionally remain simpler than their degree ceiling. Degree is
a maximum ritual vocabulary, not a mandatory checklist.

## Opt-in entry

Hemomancy must not begin because a player accidentally clicked a block.

1. Each Blood Temple spawns one Harbinger Hermit tied to that temple's Mortal
   Display.
2. The Display cannot be claimed until that Hermit has granted the player the
   blessing to take their heart.
3. The blessing is bound to that exact temple. A blessing from one Hermit cannot
   be used to bypass another living Hermit.
4. After the oath, the player may claim the heart from that temple's Display.
5. The Hermit's farewell tells the player to follow the initiation blueprint and
   then seek the Harbinger Outpost for further instruction.
6. An already initiated player may still use the Display that belongs to the
   temple they legitimately opened, but cannot use that state to short-circuit
   other temples.

This makes dialogue and the explicit oath the opt-in point. The Display is the
physical consequence of that choice, not the choice itself.

## Degree 0: Sanguine Initiation

The Blood Temple contains the complete first station:

- a small prebuilt threshold floor;
- a Cardinal Focus already in the floor;
- enough clear space for the ceremony;
- no anchors;
- no support sockets or sigils;
- no brazier offerings;
- no fog, lightning, dome, ordeal, refresh, or helper.

The blueprint tells the player to seat an iron nugget in the Focus as a crude
hematic medium. Activating it takes a small, nonlethal amount of health. A small
daemon appears briefly and enters the player, completing initiation.

The nugget lodges in and breaks the temple Focus when the rite succeeds. This is
deliberate: the temple demonstrates a Focus once, then sends the player outward to
learn how to build and reuse one.

Completion:

- grants Degree 1;
- activates the player's blood volume;
- grants the Sanguine Conduit;
- unlocks the first progression guidance;
- does not use passive sacrifice or produce a dummy material result.

## Progressive ritual vocabulary

| Degree available | Expected rite vocabulary | New lesson |
|---|---|---|
| D0 | Temple-provided floor and Focus; iron-nugget medium; no anchors, sigils, offerings, fog, ordeal, or helper | Explicit opt-in and the meaning of initiation |
| D1 | Player-built floor; crude hematic medium; one ring of four anchors; no sigils, fog, ordeal, or offering required | Build a station and fill boundary anchors |
| D2-D3 | Living Staff; one ring/four anchors; faint fog; normally one brazier offering; no support sigils or ordeals | Reusable activation and a small material commitment |
| D4 | One required support sigil; dense fog; up to three offerings; safe offering-loss failure | Inscription and prepared support |
| D5 | Up to two rings/eight anchors; up to three support sockets with one required; storm fog and lightning; first ordeal waves; up to five offerings | Active pressure, repairs, and optional bloodline assistance |
| D6 | Greater rites are normal and Grand rites appear; up to three rings/twelve anchors; up to five support sockets with two required; dome; up to six wave options; one required helper; up to nine offerings | Shared ceremonies, role stations, and collapse risk |
| D7 | Grand rites are normal; twelve anchors; up to six support sockets with three required; lengthy ordeals; up to three required helpers; up to nine offerings | Full ceremonial mastery |

The serializer enforces these as hard ceilings for every shipped Harbinger rite.
Apotheos is explicitly exempt until its separate pass.

## Activation progression

There are three authored Focus modes:

- `temple_medium` — Degree 0 only. Uses the temple's prepared Focus and the
  Hermit's temple-bound oath.
- `hematic_medium` — Degree 1. The player seats an iron nugget in a crafted Focus.
- `living_staff` — Degree 2 onward. The exact staff is planted, held in escrow
  during the rite, and returned after success, safe recall, or handled failure.

Rank-up rites can be attempted only in their exact advancement window. Completing
a rank rite removes it from station resolution for players who already hold that
degree, preventing shared floor-and-offering signatures from selecting an obsolete
rite.

## Ceremony phases

Interactive ceremonies use only phases supported by their authored definition:

1. **Consecration** — fill authored anchors. Missing anchors do not exist as
   invisible requirements.
2. **Inscription** — complete required support sockets. A rite with no sockets
   skips this lesson entirely.
3. **Sealing** — confirm the altar and committed offerings.
4. **Ordeal** — run authored response waves, if any.
5. **Culmination** — draw committed offerings inward and resolve the result.

The HUD and Rite Hint show authored values, including upfront blood, ceremony
duration, anchors, support sockets, required sigils, helpers, fog, and failure
profile. They must not infer complexity from rite form alone.

## Blood and offerings

Interactive Harbinger rites do not also charge a hidden lump-sum `bloodCost`.
Blood is paid visibly through boundary filling, sigil work, repairs, helper
actions, or a rite-specific effect. Legacy and Unstained countdown rites retain
their existing completion drain.

Offerings are exact unordered brazier signatures. They are committed at sealing
and consumed only through the captured offering itinerary. Floors are reusable;
only explicitly consumable upper structures may be removed.

## Support sigils

Prepared support sigils have concrete jobs:

- **Reservoir** stores and returns rite blood.
- **Bastion** damages manifested rite threats.
- **Hematic Lattice** balances blood across anchors.
- **Mnemonic** exposes false omens.
- **Cage** strongly binds manifested threats in place.
- **Lens** exposes false omens as an alternate late-game answer.

Response sigils such as Suture, Shunt, Seal, Cage, and Lens resolve their authored
ordeal wave when correctly drawn. Repeated response waves keep separate progress.

## Helpers

Helpers begin at Degree 5 as optional assistance and become an authored
requirement only at Degree 6+.

- **Anchor** helpers feed depleted anchors.
- **Attendants** may correct one missed response per wave.
- **Wardens** spend blood to hinder manifested threats.

Both recruited NPCs and other players can occupy stations. Required helpers must
remain available; merely assigning a UUID does not satisfy the ceremony.

The Covenant Vigil is the dedicated first shared-ordeal demonstration. It requires
one helper and rewards every assigned survivor—not just the caster—with ten
minutes of Resistance and Regeneration.

## Failure and safety

Failure severity also progresses:

- D0-D3: safe retry;
- D4: offering loss;
- D5: fragile floor damage;
- D6-D7: full collapse may be authored.

Only Exsanguination permits passive sacrifice. Players, allies, tamed creatures,
bosses, and entities bound as rite threats are never eligible passive sacrifices.

An active Harbinger rite can be safely recalled by holding Blood Absorption on its
Focus or planted staff for four uninterrupted seconds. Recall returns the exact
staff and dismisses rite threats, but does not refund committed blood or offerings.

Irreversible or hostile utility rites have additional safeguards:

- Hematic Unbinding requires two completed performances against the same bloodline
  within ten minutes.
- Moving a Founding Fane requires a second activation of the exact new formation
  within thirty seconds.
- Pallid Shadow requires server PvP, a valid non-allied survival target, and real
  Unstained progress.
- Crimson Beacon, Sanguine Dominion, and Founding Fane replace or relocate their
  prior owner-bound location instead of silently accumulating duplicates.

## Rite-specific outcomes retained by the pass

Removing placeholder outputs must not remove a rite's real reward:

- Bloodline Founding produces a presigned Ancestral Ledger.
- Exsanguination produces Sanguine Quintessence.
- Initiate grants the first Sanguine Blob used by subsequent practice.
- Vessel rites still return their upgraded vessel and require the prior vessel.
- Hematic Fortification permanently reduces vascular and manipulation strain by
  fifteen percent.
- Ancestral Communion advances through its lore variants in persistent sequence.
- Covenant Vigil rewards all assigned survivors.
- Effect-only rites such as Beacon, Mending, Fervor, Dominion, Eclipse, Chamber,
  Fane, Bloom, and Pruning resolve through their dedicated completion behavior and
  do not emit placeholder items.

## Player guidance

The Sanguine Conduit is the durable in-game manual for this progression. Its Rites
tab explains:

- the three activation modes;
- which mechanics unlock at each degree;
- the difference between required and optional support sockets;
- helper roles;
- ordeal responses;
- failure and safe recall;
- the authored requirements and reward of each known rite.

The Rite Hint remains the spatial blueprint for the currently selected rite. NPC
dialogue points the player toward those two tools instead of requiring wiki
knowledge.

## Explicitly deferred

The Degree 8 / Apotheos ceremony remains mechanically intact and outside these
ceilings. Its economy, ritual vocabulary, and post-human activation logic require
a dedicated design pass rather than being normalized to the D0-D7 teaching curve.
