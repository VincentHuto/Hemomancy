# Cardinal Rite Refactor Sample Guide

The four recipes whose names begin with `[TEST]` are repeatable ceremony
fixtures. Their old one-shot `bloodCost` is zero and
`breakBlocksOnCreation` is false, so the structure remains available after a
success or collapse. The blood paid into anchors, sigils, repairs, allies, and
profession acts is still real.

## Quick setup

1. Use `/hemo blood activate`, `/hemo blood setmax 10000`, and
   `/hemo blood fill`.
2. Set the degree needed by the sample with `/hemo degree set <degree>`.
3. Build the flat pattern below, or use the creative Structure Spawner to
   place the recipe.
4. For the Degree 0 Circulation sample, hold a Sanguine Formation and right-click
   the occupied block nearest the pattern's ground-level center; the item is
   consumed when the rite begins. For the later Harbinger samples, use a Living
   Staff on that block instead. Clicking another block in the completed pattern
   identifies the correct activation block. Unstained rites retain the Blood
   Crafting key (`C` by default).
5. Project blood into the glowing red anchors in their displayed order. On the
   Degree One sample, an empty-hand right-click also fills an anchor at the
   cost of health. Each anchor is framed by a dim recessed socket; while blood
   fills it, small droplets circulate inward before the completed anchor settles
   into the gap. Each pair of adjacent completed anchors draws the
   quarter-circle inward from both sockets, so four completed anchors close one
   boundary ring. Each completed line leaves a faint recessed blood stain
   beneath it. When the fronts meet, a brief seal pulse and heartbeat mark the
   join before a blood bolus runs back toward both anchors. Damaged sockets
   visibly twitch and deform as instability approaches failure. Rolling
   black/red ground fog encloses the full Harbinger rite
   footprint for the entire ceremony, with intermittent black and purple
   lightning arcing cloud-to-cloud within the bank. The engulfing Fane exterior
   remains a separate effect that appears only on Degree 3+ rites.
6. During Inscription, optionally fill any differently colored Ichorian sigil
   sockets and assign allies where available, then keep the projection tool in
   hand and project into the daemon manifested over the altar's center to seal
   it. Sigil nodes follow the highest nearby block
   surface; interact with the lit surface node rather than the pattern's
   original vertical layer. In the Degree One abbreviated rite, empty-hand
   right-click each active sigil node to bloodlet 50ml into it; later rites
   use normal blood projection.

Unknown ordeal sigils deliberately do not explain their full pattern. Complete
one experimentally and its shape/color entry will be recorded in the Rites tab
for that player. Every correctly completed node after the first draws a
pulsing, color-matched connection from the previous node, leaving the traced
portion visible without revealing any unfinished lines.

## Samples

| Rite | Player degree | Upfront anchors | Main coverage |
| --- | ---: | ---: | --- |
| `[TEST] First Circulation` | 0 | 4 / 200ml | Abbreviated model, bare-hand Degree One consecration, one ring, boundary/HUD, Reservoir discovery |
| `[TEST] Inscription Crucible` | 1+ | 8 / 400ml | Diagonal rings, four optional sockets, discovery and response sigils, Bloodlicker siphoning, repair and still intervals |
| `[TEST] Bloodline Vigil` | 4+ | 20 / 1000ml | Degree Five ally quota, shared reserve behavior, all three ally roles, Fargone/Rogue Will/false-omen threats, severe profession recovery |
| `[TEST] Grand Ordeal Gauntlet` | 6+ | 28 / 1400ml | Degree Seven stress test, three allies, all five ordeal types plus discovery, fastest decay, fragile damage, collapse-on-profession-error |

The two upper samples are marked as prototype rank rites only so they enter the
Profession phase and use degree-scaled ally/failure rules. They are not mapped
to a real Hematic Order promotion and therefore do not grant a permanent degree
on completion.

## Flat build patterns

Each row shown is viewed from above. Rotation and mirroring are accepted by the
multiblock matcher.

### First Circulation

```text
C R C     C = Cut Copper
R G R     R = Redstone Block
C R C     G = Gold Block
```

### Inscription Crucible

```text
D A D     D = Polished Deepslate
A L A     A = Amethyst Block
D A D     L = Lapis Block
```

### Bloodline Vigil

```text
B Q B Q B     B = Polished Blackstone Bricks
Q C O C Q     Q = Quartz Block
B O H O B     C = Crying Obsidian
Q C O C Q     O = Obsidian
B Q B Q B     H = Hematic Iron Block
```

### Grand Ordeal Gauntlet

```text
T M T M T     T = Tuff Bricks
M G G G M     M = Magma Block
T G H G T     G = Gilded Blackstone
M G G G M     H = Chiseled Hematic Iron Block
T M T M T
```

## Ally checks

- Player allies must already belong to the caster's bloodline. During
  Inscription they empty-hand right-click one of the three colored role
  stations. Shift-right-clicking their station toggles deliberate access to
  the shared blood pool.
- The caster assigns a recruited NPC by empty-hand right-clicking the NPC
  during Inscription. Repeating the interaction cycles Anchor, Attendant, and
  Warden.
- NPCs spend the bloodline pool first, then their own reserve. Emptying both
  makes that NPC Bloodspent for one full Minecraft day.
- Anchor allies top off cardinal stations, Attendants can catch one missed
  response, and Wardens hinder rite-bound enemies.

For the Grand Gauntlet, all six waves are included every run but the five
non-discovery waves reshuffle. This makes it suitable for repeated stress tests
without losing feature coverage.
