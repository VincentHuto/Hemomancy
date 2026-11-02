# Cardinal Rite ring gating: one ring per phase

Date: 2026-10-13
Status: approved in conversation (sections 1 and 2)
Builds on: `2026-10-13-cardinal-rite-world-escalation-design.md` (the world stage)

## Goal

A multi-ring Cardinal Rite no longer opens every anchor ring at once. Ring 0 opens in Consecration; each later ring
wakes at a later phase of the rite, must be fed before that phase can end, costs more blood than the last, and opens
with a bigger moment than the last. Filling each ring still steps the world stage. One-ring rites play exactly as
today.

## Decisions (from brainstorming)

- Later rings open at **phase boundaries** after the seal.
- Later rings are **bigger and cost more blood** (+25% per ring).
- The phase a ring opens in **runs, but cannot end** until that ring is full (no separate hold).
- Harbinger-side only in spirit; Unstained gets its own ritual system later. Puppeteer trials and succession have no
  anchors and are untouched.

## Section 1: rules (server)

### Ring identity

A ring is the authored `ring` value on each anchor (`CardinalRiteCeremonyDefinition.Anchor.ring()`), sorted
ascending and renumbered densely to a **rank** 0, 1, 2 ... The server currently groups anchors as `index / 4`
(`ActiveCardinalRite.totalAnchorRings`, `completedRings`, `balanceAnchors`) while the client uses `anchor.ring()`.
All server grouping moves to rank. Every current recipe lists anchors ring-sorted with four per ring, so no current
rite changes shape.

### The opening schedule

When a rite with anchors starts, it plans its **openers**: every phase entry after the seal and before Culmination,
in the order the rite will meet them:

- Station projection (station-upgrade rites)
- each Ordeal wave
- each Still interval
- the Offering procession

Ring 0 is open from Consecration. Ring *k* (k >= 1) opens on entering opener *k - 1*. If there are more later rings
than openers, every remaining ring opens together at the last opener. If a rite has **no** openers, all its later
rings open on entering Inscription.

The open count only rises (a high-water mark, like the world stage) and is saved with the rite.

### Gating: the phase runs but cannot end

A ring that opened in a phase blocks that phase from finishing while any open anchor is below its requirement:

| Phase the ring opened in | What is held |
|---|---|
| Ordeal wave | the wave does not complete (no next wave, interval or finale) |
| Still interval | already requires every open anchor full |
| Station projection | the last circuit does not hand off to the finale |
| Offering procession | the procession does not move into Culmination |
| Inscription (fallback only) | the seal is refused |

### Filling

- Blood projection targeting, the nearest-anchor fallback, sacrifice blood spread and ordeal refills reach **open**
  rings only.
- Dormant anchors cannot take blood, cannot be drained or broken by instability, and do not count toward "all anchors
  consecrated".
- `fillAnchor` additionally accepts Inscription, Station projection and Offering procession while an open anchor is
  below its requirement (today: Consecration, Ordeal, Still interval only).

### Cost

The requirement for an anchor in rank *r* is `round(50 * (1 + 0.25 * r))` mL: 50, 63, 75, 88, 100, 113, 125, 138.
Every reader of `BLOOD_PER_ANCHOR_ML` that means "this anchor's requirement" reads the per-anchor value instead:
fill caps, `missing`, consecration checks, totals and fractions sent to the client, anchor sores, jets, boundary
arcs, status lines and the cancellation refund.

### World stage

Unchanged in meaning: the stage steps when a ring is **first filled**. Opening a ring sends its own beat and is not a
stage step.

### Older saves

A rite saved before this change (no open-ring key in NBT) loads with every ring open at the flat 50 mL, so nothing
changes mid-rite.

### Sync

`PacketSyncActiveRites` carries the open ring count per rite. The per-anchor requirement is derived on the client from
the anchor's rank (same formula; flat for legacy rites, so a legacy flag is synced too).

## Section 2: presentation (client)

### Dormant rings

- Sockets of unopened rings draw dark and closed: no jets, no arcs, no sores.
- The floor membrane shows a faint scar under each sleeping anchor, so the rite's depth is legible from the start.
- The FULL HUD panel draws sleeping rings as hollow pips.

### The ring opening

A new `CardinalRiteEventKind.RING_OPENED` beat carries the ring's rank and the ring total. It plays:

- **The tear:** each anchor of the new ring rips up out of its scar over about 2 seconds, with blood-cell bursts
  (`HemoParticleData`, no vanilla particles) and a brief jet flare.
- **The shock:** the floor's staff-strike shock, which now also fires on ring openings.
- **The sting:** a new `rite.ring_open` sound (placeholder mapped to an existing deep vanilla sound), heard rite-wide
  like the step sting.

Each factor scales with the ring's place in the rite (first later ring least, last ring most): burst count, tear
height and duration, shock strength and reach, sting pitch (lower) and volume (louder), and for deeper rings a brief
fog surge through the world-stage air layer. The last ring of a multi-ring rite also flickers the sky wound if it has
formed.

All scale factors live in one tuning class (`RingOpeningMath`, beside `RiteWorldMath`).

### Guidance without the HUD

- On a ring opening, the owner gets one action-bar line: "The second ring wakes — feed its anchors" (ordinals up to
  eighth).
- When the phase is held only by unfilled ring anchors, a new problem says so: "The new ring hungers: N anchors
  empty".

## Out of scope

- New recipes or re-authored ring layouts.
- Unstained rites.
- Custom audio (placeholders only).

## Testing

- JVM: schedule planning (no openers, fewer openers than rings, more openers than rings, station and procession
  rites), rank grouping, per-rank cost, gating of every listed phase end, fill/drain/instability refusal on dormant
  anchors, legacy NBT load, packet round trip, guidance lines, `RingOpeningMath` monotonic scaling.
- GameTest: a two-ring rite with an ordeal: ring 1 is dormant through Consecration, opens on the first wave, the wave
  will not complete until it is fed, and the rite survives a save mid-hold.
- Native client launch to prove any shader change compiles on the user's NVIDIA GPU.
