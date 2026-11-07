# Saint Velorum: combat and visual overhaul

Date: 2026-11-05. Written from the umbrella design `2026-11-05-saint-overhauls-design.md` (its kit, rules, fight-shape
targets, lanes and anti-goals bind this one) for OPEN_QUESTIONS D-2. Status: design, for the user's review before the
build run. The user skipped the question round, so every call not quoted from a ruling is listed in "Calls made".

Thematic sentence: *Velorum preserved.*

## Revision 2026-11-06: Frozen Cruor and liquid dark

The user, 2026-11-06: "Velorum needs WAY more 'blood ice'/Cruor ice themed attacks right now he reads way to much like
a generic ice wizard, he also needs more things to do with the umbral/tenebris parts of his aspects." The approved
direction is below. This section overrides every older section of this design where they disagree (sections 0 to 20,
the Build notes, the Verification and the three Visual pass 2 sections). What it does not mention stands: the umbrella
(`2026-11-05-saint-overhauls-design.md`: the Kit API, its Resolutions, the corrected Velorum rule), the stillness
meter, the three openings, the health floors and the Still Guard, the wakeable congregation, the ending, the deeds'
worked numbers (D-4), no literal time stopping, dignified rigidity, 16 px per block, Java animation on
`BossReplayClock`, the umbrella's bands (`BossBands.STANDARD`), wind-up floors (12 melee, 20 area, after the phase-3
cut), `Height.JUMPABLE` (0 to 0.9), the 1 + 0.5 x (n - 1) scaling and the gated-never-timed rule.

Canon (`docs/LORE_REFERENCE.md`, the tendency paragraph and §10.5): Congeatio "arrests it as dark cruor under pale
frost; structures raised by cold memories are Frozen Cruor, temporary blood-ice rather than ordinary winter ice".
Tenebris "thins it into liquid darkness with torn, smoky edges". Velorum is Congeatio and Tenebris: suppressed
circulation and warmth, isolated awareness and sensation. Every move below says which of the two it speaks.

### R.1 What this overrides

| Older text | Now |
|---|---|
| §0 D-2, §12 "Frozen cruor... the only red on him", Visual pass 2 effects "There is no warm colour; his only red stays the frozen cruor on his halo links" | Every piece of his ice is Frozen Cruor (R.2). Red is his ice's own colour; it stays dark, never bright or arterial |
| §12 palette and Visual pass 2 effects palette: clear blue-white ice, black ice, "ice blue" | Cruor glass and black-red cruor; blue appears only as frost light (rims, glints, the lantern) |
| §7 `BLACK_ICE`, Black-Ice Splinters, "the black ice" hazard | `CRUOR_RELEASE`, the Release of Cruor; the standing hazard is the cruor field. Same numeric id (4) and numbers |
| §7 `LONG_NIGHT` uses `OBSCURED` for 120 ticks; §20 "the Long Night uses the existing OBSCURED fog" | Velorum never applies `OBSCURED` (its fog blacks out past 10 blocks, Verification "Still open"). His dark is the gloam (R.4), and the Long Night's dark runs out as pools |
| §7 `SPLINTER_CROWN` icicles falling from the halo | Cruor splinters burst up out of the blood the players spilled where they stood still |
| §8 phase 2 closes on a FAR target by gliding; §8 and §3 phase 3 "he glides to the centre" | Phase 2 closes by the Shadow Step; at the phase-3 card he steps to the ring through the dark and is planted there |
| §3, §7, §12, §13, §14, §16, §17 tables | Extended by R.5 to R.15; the build list for this revision is R.16 |

### R.2 The material rules

**Frozen Cruor (all his ice).** Blood arrested where it stood: glassy oxblood with darker clots held inside it, black-red
where it is thick, pale frost crusted on its upper faces, its edges and its points.

| Token | Colour | Where |
|---|---|---|
| `CRUOR_DEEP` | 0x1A0508 | Thick cruor, a blade's heart, the field's bed |
| `CRUOR` | 0x3A0E14 | The body of every shard, slab, blade and pane |
| `CRUOR_LIT` | 0x6A1C26 | Thin cruor with light behind it (a pane, a shaft's edge, a blade's tip) |
| `CLOT` | 0x12030A | Nodules inside the glass, one to three a shard, never on the surface |
| `HOAR` | 0xDCE8F4 | The frost crust on up-facing faces and points; hoarfrost feathers on seams |
| `FROST_LIGHT` | 0x9ACCF0 into 0xE8F4FF | Fresnel rims, commit glints, the lantern's light. The only blue |
| `CRUOR_GLOW` | 0x5A1A22 into 0x8A2430 | The cruor's own dull glow: faint always (0.15), strong only in the Eclipse (0.8) |

- No clear blue-white ice anywhere in his kit, on him, his devotees or his floor work. The ice shader's body colours
  are red-dominant; blue lives only in its rim, glint and frost-light terms (a guardrail, R.15).
- **The cruor comes from somewhere.** Nothing of his freezes out of empty air. Every shard, blade, slab, spine and
  javelin is shown leaving its source first, and the source is one of three:
  - **the floor's seams.** The chamber floor's block seams hold the old blood of the congregation. Before a floor move
    freezes, dark blood wells up in the seams it will use (a glossy black-red bead along each seam, 3 ticks), then
    sets and breaks out as cruor;
  - **the congregation's veins.** The frozen figures have black-red veins under their rime. They flare when he draws on
    them (Veins of Frost, Call to Prayer, the Eclipse);
  - **the players' own wounds.** The preserved shell, the Arrested Pulse's spines, the Draw of Silence, Set in Place's
    icicles and the Splinter Crown's splinters are the challenger's own blood.

  The two exceptions are drawn from a source too: the Cruor Javelins are drawn up the staff from the seams under his
  heel into the lantern cage, and the cruor on his own body was always there.
- **Frost** is the crust on cruor and the feathers on a freezing seam. It is pale, never a white rug: the Visual pass 2
  polish rule (a crazed glaze you see through) stands.
- **The preserved shell is the player's own blood.** Over its 3-tick growth, `HemoParticleData.bloodCell` motes leave
  the player's chest and the seven panes set from the feet up as cruor glass at 0.55 alpha (`CRUOR_LIT` with `HOAR`
  edges), so the player shows through dark red. It breaks into fourteen falling cruor shards.

**Liquid dark (Tenebris).** Darkness thinned to a liquid: a flat, glossy near-black surface (0x07040C) with a slow
violet-black sheen (0x1A1030) and a slow ripple, never a hard-edged disc. Its edges are torn: broad tatters of smoke
(the frame's `TENEBRIS` 0x140A1E) lift off the rim, fray upward and tear away. It is drawn as layered geometry: the
liquid surface, a rim of torn smoke cards, and slow gloom above it. No contour lines, no webs of thin strands.

**The lantern's light against the dark.** His staff lantern holds a radius of cold light, `L`, round the floor point
under it. The dark cannot hold inside `L`: pools are drawn torn back at its edge and have no effect there.

| State | `L` |
|---|---|
| Phase 1 / 2 / 3 at rest | 6 / 5 / 4 blocks |
| The Long Night's wind-up | Shrinks to 3 by the commit |
| The Guttering Lantern | 1 within its first 10 ticks; every live pool grows +0.25 blocks a tick toward its cap of 7: the dark floods in to his feet |
| After the Gutter | Back to the phase value over 20 ticks; pools ease back to their own curve over 40 |
| The Eclipse | 0 |
| Relit (the Eclipse broken) | Flares to 8 for 20 ticks, then the phase value |
| The ending | The phase value, then full as "the frost falls out of the light" |

`L` is a pure rule of the synced phase, move and opening (`VelorumDarkRules.lanternRadius`), so the server and client
compute the same value without a new synced field.

**Bold, not faint.** Telegraphs and attacks are never hidden by the dark. Every telegraph draws after the dark, its
fragments take the fog at 0.35 strength only, and the cruor carries its own glow. "Subtle" applies only to the overlays
on bodies (the veins under a figure's rime, frost on a player) and on screens (the gloam vignette).

### R.3 The dark pools

**What makes them.** Pools are owned `BossGroundHazardEntity`s (a new kind, `DARK_POOL`) through `arena.own`, so they
go with the trial. Each carries its seed, birth, cap radius and life; its radius grows from age by
`VelorumDarkRules.poolRadius` on both sides.

| Source | Pools | Radius (grows over) | Life (phase 1 / 2 / 3) |
|---|---|---|---|
| The Long Night's commit | Four, each seeded in the lantern-shadow of one of the four rank pillars nearest the target (the side away from his lantern; frozen figures if fewer pillars) | 3.5 in phase 1, 4.5 after (30 ticks) | 300 / 400 / 500 |
| The Shadow Step | One where he sank, one where he rose | 2.5 (8 ticks) | 200 |
| Each Gloam Cut | One where the cut ends | 1.5 (6 ticks) | 160, 240 in phase 3 |
| The Procession | One where the shades rise; a strip along the road behind each shade (half-width 0.6) | 2.5; strip at once | the move's span + 100; strips 120 |
| The Eclipse | The whole chamber box | at once | its hold |

Pools stay on the boss floor and the leash ring round it; they never run into the aisles, where the congregation kneels
and a player goes to wake a figure. Outside the Eclipse the dark covers at most 45% of the boss floor: past it, the
oldest pool fades first (`expire`, 20 ticks).

**What the dark does.** A challenger is *in the dark* when inside a live pool, outside `L`, not within 1.5 blocks of an
awake ally devotee (their thawed blood is warm) and not carrying `SEARING` (their own heat). The Eclipse puts everyone in
the dark except inside an ally's warmth.

| Effect | Rule |
|---|---|
| Stillness | The standing, sneaking and item-use fill terms ×1.4; the drain ×0.9. Applied after the presence and before the shelter. The Slowness and Rime terms are not multiplied |
| Sight | The gloam: a torn-smoke vignette whose inner edge sits 55% of the way from centre to edge (35% in the Eclipse), 0.7 alpha at the screen edge, never inside the crosshair's middle third; and a violet-black fog, near 6 and far 18 blocks (4 and 14 in the Eclipse), crossfaded by the player's darkness-effect setting. Never black |
| Sound | Muffle 0.55 on every sound but his (the kit's `muffle`); his tells always carry |
| Openings | Fill is still zero in any opening. Pools do not grow in an opening (the Repose deed excepted) |

**Numbers.** From 0, standing in the dark fills in 102 ticks, 68 in his presence (from 143 and 95). Walking in the dark
and his presence nets −0.396 a tick. **Never a lock:** Velorum never takes a challenger's Rime past two levels, so
the worst stack (presence, the dark, Stilling's Slowness I, two Rime levels) still nets −0.096 a tick walking and −0.35
sprinting.

**Pools and the rest of the fight.**
- **Openings.** The Gutter is where the dark reaches him: to take the window a player stands at his feet in the dark,
  muffled and in the gloam. When it closes the light pushes the dark back over 20 ticks, and a player who lingers pays
  ×1.4 for those ticks.
- **The congregation.** Under the Beholding deed a figure's gaze reaches 10 blocks in the dark (from 8). Hostile
  devotees move ×1.25 in the dark and their grip fills +2.0 a tick (from +1.5). Awake allies carve 1.5-block warm holes.
- **The lantern.** The safest light in the chamber is his own, and it is in reach of the Toll, the Sweep and the Hanging
  Lantern. The dark is away from him. The fight's spacing lives between the two.

### R.4 The gloam, and why no `OBSCURED`

His sensory dark is his own: the in-the-dark rule above, plus a 60-tick gloam that the Long Night (on those with sight
of the lantern at its commit), the Gloam Cuts and the Shadow Step's arrival put on whom they strike. A gloamed challenger
gets the dark's sight and sound but not its stillness multiplier. The fog is a new client subscriber,
`client/event/VelorumGloamFog` (`ViewportEvent.RenderFog` and `ComputeFogColor`), the pattern of
`SchoolDarknessPresentation`. Velorum applies `RIME` (cruor at the feet, the shared school-state art, now on theme) and
never `OBSCURED`.

### R.5 The moveset

Ticks are wind-up / active / recovery as authored; the phase-3 cut and the 12/20 clamp still apply. Damage is for
Normal before the enrage, ×(attack / 9), through `ManipulationCombatHelper.canHarmFor` with `hemomancy:saint_velorum`.
"Lead" is the tendency the move speaks: Cruor, Tenebris, Mixed, or Light (his lantern and invocation). Every move tracks
its aim until its commit and then locks, unless the row says otherwise.

| Id | Name | Status | Lead | Phases | Band and sector (weight) | Tell | Footprint | Ticks | Damage and effect | Counterplay | Follow-ups | Borrows from |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `STAFF_TOLL` | Staff Toll | Rethemed | Cruor | 1–3 | HUG 30, NEAR 24; FRONT (FLANK ×0.5) | The staff rises to the shoulder; across the cone the floor seams darken as old blood wells into them, frost crusting their lips | Cone 70°, 3.5, height 0–2.5 | 16 / 3 / 18 | 9; +12 stillness. The welled blood sets and nine cruor blade clusters heave up across the cone | Step out of the cone or behind him | `LANTERN_SWEEP` (60% if NEAR, link 4); phase 3 `HOLLOW_TOLL` (55%, link 6) | `CryogenicPulseManip` (a burst of crystallised blood-ice), `GlacialRampartManip` (ice raised from a surface) |
| `LANTERN_SWEEP` | Lantern Sweep | Rethemed | Mixed | 1–3 | HUG 10, NEAR 26, MID 8; FRONT, FLANK | The lantern drawn back low behind his hip; the dark it leaves behind gathers at his heel | Sweep 160°, 1 to 5, over 6 ticks, height 0–2.4 | 14 / 6 / 16 | 8; Slowness I 40. The lantern drags its own shadow through the arc as a dark shell; the floor behind the band glazes to cruor and blades heave up | Back off past 5 or move with the sweep | Phase 2–3 `CRUOR_RELEASE` (50% if MID, link 6): string A | `BloodEclipseManip` (a cone of void-drenched blood) for the shell; `CryogenicPulseManip` for the blades |
| `HANGING_LANTERN` | The Hanging Lantern | Rethemed | Cruor | 1–3 | HUG 30, NEAR 18 REAR; FLANK 8 | The side chain gathers at his hip; the rear arc's seams bleed dark | Rear cone 120°, 3.5, height 0–2 | 14 / 4 / 16 | 6; +15; Slowness I 30. Seven cruor blades heave up behind him | Leave the arc to the flank or stay past 3.5 | None | `CryogenicPulseManip` |
| `CRUOR_RELEASE` (was `BLACK_ICE`) | The Release of Cruor | Rethemed, renamed | Cruor | 1–3 | NEAR 10, MID 26, FAR 22; FRONT (FLANK 8) | The claw rises and closes on the gathered cold; five seams split into trenches running out from his feet and old blood wells into them | Five lanes fanned 15°, 0.9 wide, 12 long (14 in phase 3), front 1 block a tick | 24 / 12 / 22 | 10; +20; a 0.4 toss. Blades of frozen blood break out along each lane; the filled trenches stand 60 ticks as the cruor field (Slowness II inside) | Go through the gaps before they close; never fight inside the field | None | `CruorJavelinManip` (a shaft freezing together round a spine of clots) for each blade; `TemporaryIceManager`'s `CRUOR_FORM` for the field |
| `STILL_FLOOR` | The Still Floor | Rethemed | Cruor | 1–3 | HUG 12, NEAR 18, MID 16, FAR 4; any; +12 if a challenger in the ring is at 60 or more | The heel planted; the blood in every seam from 2.5 to 10 darkens and frosts ring by ring | Annulus 2.5 to 10; the grounded and unmoving at the commit | 30 / 3 / 30 | 6; rooted 20; +25. The seams' blood freezes into a ring of cruor slabs | Be moving at the commit, stand in the eye, or be past 10 | Phase 2–3 `SPLINTER_CROWN` (50% if anyone was rooted, link 6): string C | `AbsoluteStillnessManip` (the pulse that holds), `GlacialRampartManip` |
| `BID_TO_KNEEL` | Bid to Kneel | Kept | Light | 1–3 | MID 14, FAR 26; FRONT, FLANK; kite pressure | The raised lantern's cold cone; the dark presses in at its sides | Cone 20, 50°, line of sight | 28 / 30 / 16 | +1.2 stillness a held tick; at 60 or more, kneel (Slowness IV 30). A kneeling challenger's blood freezes at the knees as a cruor collar | Break sight behind a figure or pillar; overhold him | `CRUOR_RELEASE` at the kneeling (80%, link 4): string B | `RimeboundSentenceManip` (a sentence held on one body) |
| `LONG_NIGHT` | The Long Night | Rethemed | Tenebris | 1–3 | Any; base 6, +6 per 100 ticks since an opening, cap 48; never within 300 | The staff high; eight folds of liquid dark rise round the lantern, smoking and tearing at their tops; `L` shrinks to 3; the pillar lanterns flicker | Sight of the lantern at the commit (for the gloam); the pools seed on the commit | 32 / 10 / 0, then the Gutter | 2 and the gloam to those with sight; the folds sink and the dark runs out as four pools (R.3); the ten pillar lanterns go dark 180 ticks | Break sight at the commit; keep out of the pools; take the Gutter knowing the dark comes to his feet | Always the Guttering Lantern | `EclipseWellManip` (a pit of dark that holds), `VoidShroudManip` |
| `SET_IN_PLACE` | Set in Place | Rethemed | Cruor | 1–3 | Only against a preserved or kneeling challenger within 6: 60 | He glides to the figure and lifts the claw; their own blood rises out of the shell or the knee collar as five cruor icicles over them | Disc 1.2, height 0–2.5 | 20 / 2 / 24 | 10 (15 against the preserved); frees them | Others break the ice first (two hits) or warm it out | None | `RimeboundSentenceManip` (the sentence carried out) |
| `RECALL` | Recall to Stillness | Kept | Cruor | 2–3, proper and NONE | 30 with an awake ally in 14 and in sight | The pale threads draw; the ally's veins frost black-red from the hands up | Tether to the ally, line of sight | 20 / 40 / 18 | The ally falls still (300 lockout, double cost) | Break the line; overhold him | None | `AbsoluteStillnessManip` (freezing the blood of what is held) |
| `CALL_TO_PRAYER` | Call to Prayer | Kept | Light | 2–3, forced | 16 under the cap | The lantern raised to an aisle; the chosen figures' veins glow dull red through their rime before they rise | The two unpinned figures nearest a challenger | 24 / 6 / 20 | They rise against the challengers | Pin them first; kill the risen | None | None |
| `PROCESSION` | The Procession of Shades | Rethemed | Tenebris | 2–3 | MID 14, FAR 20; any; +16 with no sight of him for 40 ticks | A pool opens at the road's far end and three shapes of liquid dark rise out of it, hooded, smoke tearing off hood and hem | Path 1.2 wide, up to 18 long, square to his view; three shades 2 apart at 0.5 a tick | 26 / 44 / 14 | 4 a shade; +30. Each shade leaves a strip of dark; one that touches breaks into smoke and frost | Step off the road | None | `PenumbralDriftManip` (dark carried with a moving body), `VoidShroudManip` |
| `SPLINTER_CROWN` | The Splinter Crown | Rethemed | Cruor | 2–3 | NEAR 10, MID 18, FAR 12; any | The halo's icicles crack free and fade into frost light; at each stain of a challenger's spilled blood the blood beads up and darkens; rings from tick 8, locked at 14 | Discs of 1.5 at each challenger's stillest spot of the last 100 ticks, plus the two places his hits spilled most blood in the last 200 (at most eight) | 26 / 4 / 20 | 7 each; +10. Cruor splinters burst up out of the stain (1.6 to 2.3 tall, leaning out); no standing hazard | Step off the stain once it locks; do not stand still where you bled | None | `OsseousBloomManip` (a crystallisation cascade through the blood) |
| `ENDLESS_HOUR` | The Endless Hour | Rethemed | Mixed | 3 | State (D-7); never within 400 of an Eclipse's end | On the cruor ring, utterly still; every seam fills with frozen cruor from his feet outward while the dark gathers at the floor's rim | The chamber box | 30 / 200 / 30 | As written (fill ×2, drain ×0.5, no guard; 8% breaks it into a 100-tick Bow; unbroken, 50 or more are preserved, the rest take 6, the Gutter at half) | Circle while landing hits; shelter; Flammeus | None | `AbsoluteStillnessManip` |
| `HOLLOW_TOLL` | The Hollow Toll | Rethemed | Cruor | 3 | HUG 30, NEAR 22; any | The staff upright, then driven straight down; a ring of cruor freezes up out of the seams as it passes | Ring 1 thick to 9 at 0.45 a tick; strikes anyone under walking pace | 24 / 20 / 20 | 7; +15 | Keep moving through it | A second ring (55%, link 8): string D | `CryogenicPulseManip` |
| `SILENCE_DRAW` | The Draw of Silence | Rethemed | Cruor | 1–3, Litany skipped | MID 12, NEAR 10; any | The claw closes slowly; from each blood-active challenger their own blood is drawn out in a dark stream that freezes into cruor beads gathering at his fist | Disc 10, line of sight | 24 / 20 / 18 | 6 blood a tick (`velorum_silence`, up to 120); the inactive take 1 every 5 ticks (their stream stays pale) | Leave the 10 blocks or break sight; overhold | None | `CruorJavelinManip`'s arrest (the drawn blood sets as it travels) |
| `ARRESTED_PULSE` | Arrested Pulse | New | Cruor | 1–3 | NEAR 10, MID 14, FAR 12; any; +12 if the target is at 40 or more; never a marked challenger | The claw reaches out palm up and closes slowly as round a heart; the target's veins show black-red under the skin from the wrists up, frost crusts at the wrists; a heartbeat slows | The mark; at each beat a Disc of 1.5 round the marked, shown 12 ticks before | 22 / 4 / 16; beats at +30, +60 and +90 after the commit | No damage at the commit. At each beat: a marked challenger who averaged walking pace (0.13 blocks a tick or more over the 10 ticks before) and is under 70 stillness sheds the frost harmlessly; otherwise cruor spines burst out of them: 5, +15, Slowness I 20, and 5 and +10 to anyone else in the disc. Beats inside an opening pass. Phase 3 with two or more challengers: two marks | Walk (not sneak) through each beat and keep the meter under 70; stand clear of a marked ally at a beat; Searing ends it; another challenger's Projection on the marked for 10 fed ticks warms it out (`velorum_thawing`) | Phase 3 `GLOAM_CUTS` (45% if NEAR or MID, link 8): string G | `RimeboundSentenceManip` (a sentence on one body), `AbsoluteStillnessManip` (freezing the blood of the held), the `FROZEN_VEINS` form; `CruorJavelinGeometry`'s frost fingers for the spines |
| `CRUOR_JAVELINS` | Cruor Javelins | New | Cruor | 2–3 | MID 18, FAR 24, NEAR 6; FRONT (FLANK ×0.5) | The staff lifted level, the cage aimed; cruor drawn up the staff from the seams under the heel into the cage; three shafts (five in phase 3) freeze together round it; their lines score the floor, tracking until the commit | A `Union` of lanes, 18 long, half-width 0.45, front 1.5 a tick, loosed 4 ticks apart, each cut at the first pillar, figure or wall (`Occluded`). Aims: the target, its lead (position + velocity × 10 ticks), its mirrored lead. Phase 3 adds a double lead and two side shafts at ±2, split three and two over two targets | 24 / 20 / 18 (phase 3 24 / 28 / 18) | 7 a shaft, at most two a challenger; Rime raised to two levels (120 ticks); +12. Lodged shafts stand 40 ticks, then shatter | Cut square across your line as the shafts lock (stopping meets the first, running on the lead, turning back the mirror); or put a frozen figure or a pillar between: a shaft shatters harmlessly on rime | None | `CruorJavelinManip` (the shaft, two Rime levels, the 16-block reach), `CruorJavelinRules` and `CruorJavelinGeometry` (form, flight, frost fingers, shatter) |
| `VEINS_OF_FROST` | Veins of Frost | New | Cruor | 2–3 | MID 16, FAR 18, NEAR 8; any; +6 a feeding figure past two (cap +12); needs two FROZEN or STILLED figures within 14 of the target | The claw lowers palm down over the floor; the feeding figures' veins flare black-red through their rime; from each figure's base a frozen vein runs along the floor's block seams toward the target, tracking until tick 18 | A `Union`: one `Path` a vein (half-width 0.5, height 0–1.6, straight runs with at most two right-angle turns) and a Disc of 2.0 at the locked meeting point | 30 / 6 / 22 | One strike a challenger, the larger: 10 in the disc, 7 on a vein; +18; Slowness II 30. Cruor spines erupt along every vein at once | Step across and off a vein before it erupts; leave the meeting disc; fight away from the figures; wake (proper) or pin (forced) them to shut their veins; three hits or 4% in its 36 ticks overholds it (the Overheld Invocation) | None | `CryogenicPulseManip`, `OsseousBloomManip`, the `FROZEN_VEINS` form |
| `UMBRAL_STEP` | The Shadow Step | New | Tenebris | 2 (and the phase-3 card) | FAR 22, +16 once the target has stayed FAR 60 ticks; HUG 10 and NEAR 10 REAR; +16 while two allies grip him | He bows; liquid dark wells up under him and he sinks straight down into it, the lantern last; a pool opens at a congregation place and a torn column of dark rises from it | Destination: a floor-edge cell within 1.5 of a frozen or stilled figure, 4 to 6 from the target, beside or behind them, inside the leash. Arrival `Ring` 0.6 to 3.5, band 1.0, 6 ticks, `JUMPABLE` | 24 / 10 / 18 | 6; +10; the gloam. Pools at both ends. Untouchable from tick 14 to commit +4; his presence is off while he is under | Leave the destination pool's 3.5 or jump the ring; reposition while he is under | `CRUOR_RELEASE` from the arrival (60% if NEAR or MID, link 6): string E; else `GLOAM_CUTS` (40%, link 6) | `UmbralStepManip` (a step through shadow to a shadowed place), `UmbralTearGeometry` (`TELEPORT`, `UMBRA_ARRIVAL`) |
| `GLOAM_CUTS` | Gloam Cuts | New | Tenebris | 2–3 (weight ×1.3 in phase 3) | NEAR 16, MID 20, FAR 8; FRONT (FLANK 6) | The raised claw turns palm out and liquid dark gathers on the fingers, smoking; the paths show on the floor: the low cuts as torn dark bands lying on it, the high cut as a frost line under a hanging smoke edge | Three lanes from his hand, 10 long, half-width 1.6, front 0.8 a tick, at +0, +6 and +12, angled −15°, +15°, 0°; cuts 1 and 2 low (`JUMPABLE`), cut 3 high (1.55 to 2.6) | 22 / 26 / 16 (the last cut flies until +24.5) | 6 a cut, at most two a challenger; +8; the gloam. A pool where each cut ends | Jump the low cuts; stoop (sneak) under the high one at the meter's cost, or sidestep its 3.2-wide front | `CRUOR_JAVELINS` (55% if MID or FAR, link 6): string F | `GloamLacerationManip` (a smoky slash), `UmbralRendManip` (the dark closing like a wound), the `UMBRA_SLASH` form |
| `ECLIPSE` | The Eclipse | New | Tenebris (centrepiece) | 3 | State, never weight (R.7) | He lifts the lantern before his face and closes the claw over the cage, finger by finger; `L` shrinks to nothing; every lantern in the chamber goes out; the floor's seams and the figures' veins glow dull red | `None`: the move's logic and owned reach hazards | 40 / 160 / 30 | R.7 | Relight his lantern; keep moving; keep clear of the figures' reach; wake or pin figures beforehand | Always the Guttering Lantern (full if relit, half if not) | `EclipseWellManip` (a charged pit of dark that holds), `BloodEclipseManip` |

That is 21 moves: 12 cruor-led (Staff Toll, the Hanging Lantern, the Release, the Still Floor, Set in Place, Recall, the
Splinter Crown, the Hollow Toll, the Draw of Silence, Arrested Pulse, Cruor Javelins, Veins of Frost), 5 Tenebris-led
(the Long Night, the Procession, the Shadow Step, Gloam Cuts, the Eclipse), 2 mixed (the Sweep, the Endless Hour) and
2 of the light (Bid to Kneel, Call to Prayer). Any one fight has 19 at most (Recall or Call by awakening; the Draw by
deed).

**Ids.** The fifteen keep theirs (`CRUOR_RELEASE` keeps 4). New: `ARRESTED_PULSE` 16, `CRUOR_JAVELINS` 17,
`VEINS_OF_FROST` 18, `UMBRAL_STEP` 19, `GLOAM_CUTS` 20, `ECLIPSE` 21. Kinds: all six new are AREA.

**Wind-up floors after the phase-3 cut (×0.85):** Arrested Pulse 22 to 20 (clamped), Javelins 24 to 20.4, Veins 30 to
25.5, Gloam Cuts 22 to 20 (clamped), the Eclipse 40 to 34. The Shadow Step is not picked in phase 3.

**Cooldowns (from the move's end):** Arrested Pulse 260, Cruor Javelins 180, Veins of Frost 220, the Shadow Step 200,
Gloam Cuts 120. The Eclipse has none: it is gated (R.7). The older fifteen keep theirs.

**Strings.** A to D stand (A: Toll, Sweep, then the Release from phase 2; B: Bid into the Release; C: the Still Floor
into the Crown, which now reads as cause and effect, since the rooted stood still where they bled; D: Toll, Hollow,
Hollow). Three new strings mix the two tendencies:
- **E, "Out of the dark"** (phase 2): the Shadow Step, then the Release of Cruor from where he rose, so the lanes come
  across the floor from the congregation's side.
- **F, "Gloam into cruor"** (phase 2–3): Gloam Cuts make the target jump, stoop or sidestep; Cruor Javelins lead the
  dodge.
- **G, "A pulse in the dark"** (phase 3): Arrested Pulse, then Gloam Cuts whose high cut tempts a stoop, and a stooping
  challenger is not walking at the next beat. The answer is the sidestep.

The kit's two-deep follow-up cap stands.

**Picker.** Every band keeps at least two answers in every phase (HUG: Toll, Sweep, the Hanging Lantern, the Still
Floor; NEAR: Toll, Sweep, the Release, the Still Floor, Arrested Pulse, from phase 2 Gloam Cuts; MID: the Release, the
Still Floor, Bid, Arrested Pulse, from phase 2 Javelins, Veins, Cuts; FAR: the Release, Bid, the Long Night, Arrested
Pulse, from phase 2 Javelins, Veins, the Procession and the Step). New pressures: the Pulse's +12 on a cold target,
the Veins' feeding figures, the Step's FAR-60 and grip bonuses.

### R.6 The phases

| Phase | What it adds now | Tenebris in it |
|---|---|---|
| 1. The Hour | Everything of §3 phase 1, every ice as cruor from the first move; Arrested Pulse; the lantern's light against the dark (`L` 6) | The Long Night's pools, the Gutter's flood to his feet, the Sweep's dragged shadow. The player learns the dark exists and that his light holds it back |
| 2. The Congregation | Everything of §3 phase 2; Cruor Javelins; Veins of Frost (the congregation becomes his source); the Shadow Step replaces the glide's FAR close; strings E and F; `L` 5, pools to 4.5 and 400 ticks | The Shadow Step, Gloam Cuts and the Procession out of the dark. He is no longer only where he stands |
| 3. The Endless Hour | Everything of §3 phase 3; at the card he sinks at the end of the Bow and rises on the ring (the Step's tracks, no arrival ring), then is planted; Javelins in fives; two Pulse marks with two or more challengers; string G; `L` 4, pools 500 ticks, cut pools 240 | Gloam Cuts from the ring (weight ×1.3), the Eclipse as the late centrepiece after the Endless Hour |

The Endless Hour (cold) stays the phase's opening centrepiece; the Eclipse (dark) is its late one. They never overlap
and never follow each other within 400 ticks.

### R.7 The Eclipse

**Gate (state, never a cooldown).** Phase 3; the first Endless Hour has ended; not within 400 ticks of an Hour's end or
600 of the last Eclipse's end; and either the dark covers 35% or more of the boss floor or his health is at or under
15% of his maximum. Once the gate holds it starts at the next boundary through the same forced start as the Hour
(Kit follow-ups: "A pick veto and a forced pick by state"). D-7 gains the mirror rule: no Hour within 400 ticks of an
Eclipse's end.

**Wind-up (40).** The claw closes over the cage a finger at a time; `L` falls from 4 to 0; every rime lantern in the
chamber goes dark, the aisle-end votive stands and the vault's crown lamps included (the arena's `swapFor`, restored
with everything else). The room is dark but for the cruor's glow: the floor's block seams fill with dull glowing cruor
(a red grid across the whole floor), the figures' veins glow, the cruor field and lodged shafts glow, and every
challenger's own Rime crust glows at their feet. With the gloam's fog at 4 to 14, the floor stays readable: never a
blackout.

**The hold (160).** Every challenger is in the dark (R.3), except inside an ally's warmth. His presence holds from the
ring; the Still Guard holds.
- **The congregation becomes torn umbral shapes that reach.** Out of every FROZEN, STILLED and PINNED figure a shape of
  liquid dark stands up (the block stays; the shape is drawn above it and backed by an owned hazard). A shape within 4
  blocks of a challenger reaches for them: a 16-tick tell (its arm stretches along the floor as torn smoke, a cruor-red
  hand forming at its end), then a grip (a `Tether` from the figure, range 6: walk 6 from the figure to tear free):
  Slowness II and +1.2 stillness a tick for up to 30 ticks, 4 at the grip. A shape grips one challenger at a time and
  rests 60 ticks after.
- **The congregation reaches together** at 40, 90 and 140 into the hold: the three shapes nearest each challenger throw
  their arms along the floor at that challenger's locked position (owned hazards, `Lane` 9 long, half-width 0.6, front
  0.9 a tick, revealed 20 ticks): roots 15 ticks (`SaintRoot`), 4, +15 stillness.
- PINNED figures' shapes rise but cannot reach: the hyphae hold their arms. Awake allies never become shapes. So both
  paths can prepare: wake figures (proper) or pin them (forced).
- **Relight.** Any challenger's held Blood Projection on him feeds the cage: 20 fed ticks (30 under the Litany deed) at
  the lesser of 3 and the item's rate (`velorum_kindling`, a new named source), with the waking's 4-tick grace and
  half-speed bleed-off. Relit: the light bursts through his fingers, `L` flares to 8, the dark tears back to the walls,
  the shapes fall back into their figures, the lanterns return, and the Guttering Lantern opens at 60 ticks whatever the
  phase.
- **Unbroken.** The light seeps back on its own: the shapes sink, every challenger at 60 or more stillness is preserved,
  the rest take 6, and the Gutter follows at half length (20).

The Eclipse is the fight's most Tenebris moment and stays readable and answerable: every reach is telegraphed on a red-lit
floor, the relight is a Hemomancy verb (held Projection) that co-op shares (one feeds, the others draw the reaches), and
the cost is honest (feeding while standing in the dark and his presence fills 2.1 a tick, so a player comes in under 50
or uses the grace to step away from a reach).

### R.8 The openings, the congregation and the lantern

- **The Guttering Lantern.** Entries: the Long Night's end; an unbroken Hour (half); the Eclipse relit (60, full) or
  unbroken (half). Tell adds: the dark floods in to his feet (R.2's `L` table). Numbers unchanged.
- **The Overheld Invocation.** Also opens from Veins of Frost: three hits or 4% of his maximum over its wind-up and
  active (36 ticks) snaps the veins dead.
- **The Bow.** Unchanged; pools hold still in it.
- **The congregation as his source.** The figures' veins are what Veins of Frost and the Eclipse draw on. FROZEN and
  STILLED figures feed the veins; PINNED and AWAKE do not. Waking a figure is warm blood into frozen cruor: as the rime
  sheds, its veins run bright for 6 ticks, the one warm red in the fight, then dull. The block (`FrozenDevoteeBlock`) is
  not edited: the veins on figures are world geometry from his pass.
- **The forced fight.** Pinning now also shuts a figure's veins and holds its Eclipse shape's arms: the Apotheos's
  preparation pays twice.
- **The lantern** is the axis of the fight's space: his light near him (where his melee is), the dark away from him
  (where the stillness climbs), the Gutter collapsing one into the other.
- **Shadow Step and the grip.** Two gripping allies give the Step +16, so he can step out of a grip, at most once each
  200 ticks (its cooldown).
- **Spilled blood.** The server keeps each challenger's stillest spot of the last 100 ticks (A-2) and the spots where his
  hits drew blood in the last 200. Every 20 ticks a `STAIN` cue shows them as dark stains that deepen with time, so a
  player sees where the Splinter Crown will find them before it comes.

### R.9 The deeds

The three stand as written, with D-4's numbers. Each gains one line for the new moves:
- **Beholding.** In the dark a figure's gaze reaches 10 blocks; in the Eclipse a shape's reach is 5 (from 4).
- **Litany.** The Eclipse's relight takes 30 fed ticks (no opening is lengthened; this is not an extension, it is the
  door to one).
- **Repose.** The dark never rests: pools keep growing through openings. No settle after the Shadow Step's rise.

### R.10 Multiplayer and both paths

Per-player meters and marks do not scale; his health does (1 + 0.5 x (n - 1)). With two or more challengers: two Pulse
marks in phase 3, Javelins split over two targets in phase 3, Eclipse reaches for each challenger. A Silentium's verbs
gain the relight and the waking that shuts veins and shapes; an Apotheos's gain the pinning that does the same. The
worked fight length (§15) gains about one 60-tick Gutter per Eclipse: about 4 minutes 10 seconds solo, inside the
target.

### R.11 Visual spec per move

Geometry rules for every row: cruor solids in his `ICE` and `ICE_CLEAR` materials, re-paletted (R.2); floor work and
cuts in `FROST`; liquid dark in `FROST`'s new dark shapes (or a split `velorum_dark` shader, R.16). Accents are school
flows only: `LuxUmbraBatch.Material.CRUOR` and `RIME` for frost wisps and clot cores, `UMBRA` for smoke, `LUX` for frost
light. Particles: `HemoParticleData.glow` (frost light motes) and `bloodCell` (blood leaving a body) only. Veins, limbs
and threads are bold and few: straight runs and right-angle turns along block seams, tapering ridges and limbs of 0.22 to
0.6 wide in at most four straight segments. Never noodles, never contour webs, never vanilla particle clouds.

| Move | Geometry and material | Timeline |
|---|---|---|
| Staff Toll | Seams in the cone fill with glossy black-red beads under a frost lip; nine cruor blade clusters (flat four-sided blades, clots inside, frost on their points) | Build: the welling runs by 65% of the reveal. Commit: the beads set in 1 tick and the blades punch to 1.12 over 2.5 ticks. Linger: sublime from the point, frost-white front |
| Lantern Sweep | The dark shell is liquid dark with torn smoke tatters dragging off it; the frost blade and core stay; behind the band the floor glazes cruor; eighteen blades lean with the sweep | As Visual pass 2, the shell now `UMBRA` liquid dark |
| Hanging Lantern | Rear seams bleed; seven blades behind him | As written |
| The Release | Trenches sunk along five seams; blood wells up the bed from the feet outward (a glossy surface rising); blades of frozen blood break out every 1.45 blocks with the front, each forming as a shaft does in `CruorJavelinGeometry` (frost strands round a spine of clots) before it punches. The field: filled trenches of glossy black-red cruor, rimed lips | Build: trenches sink with the grow, the bed's blood shows. Active: the front fills, blades break out behind it. Field 60: frost greys it over the last 30, blades crumble over the last 14, the trench heals over the last 16 |
| The Still Floor | Ring by ring, seams from 2.5 to 10 darken with blood and frost; on the commit the seams' blood freezes into cruor slabs (one every 2.1 blocks, 0.8 to 1.2 tall, clusters of two or three) | 30 build, 4-tick flash, 30 held, then sublime |
| Bid to Kneel | The light as written (streaks, cold edges); the dark at its sides is liquid dark with torn edges, no longer faint; a kneeling collar of cruor at the shins | As written |
| The Long Night | Eight folds of liquid dark (two leaves each, torn smoke at the top, a cold hairline down the crests); on the commit they sink and the dark runs out along the floor to the four pillar shadows, where the pools spread | 32 build; folds sink over 10; pools grow over 30 |
| Set in Place | Blood rises out of the shell or collar as five dark beads that stretch into cruor icicles 1.25 long over the figure; they fall and drive into the floor as stubs | 20 build (the beads rise from tick 4), fall in 2, stubs sublime over the recovery |
| Recall | Threads pale as written; the ally's veins frost black-red, climbing from the hands | With the hold |
| Call to Prayer | The two figures' veins glow dull red through their rime; frost rings as written | With the wind-up |
| The Procession | A pool at the far end; three shapes of liquid dark rise out of it (the devotee model in `poseShade`, translucent dark, with torn smoke cards at hood and hem and two pale eye points) and walk the road; behind each, a strip of dark | Rise 12 ticks; walk; break into smoke tatters and frost in 6 |
| The Splinter Crown | Stains (dark, glossy, spreading slowly from the stillest spots) bead up; landing rings freeze round them; cruor splinters (a cluster of five to seven, 1.6 to 2.3 tall, leaning out) burst up out of each | Stains always (cue every 20 ticks); rings 8 to 14; burst at the commit; sublime |
| The Endless Hour | Every seam on the floor fills with frozen cruor from his feet out; gloom at the rim as written | As written |
| The Hollow Toll | The ring's front is a band of seams freezing cruor; slabs heave up behind it and crumble within 14 ticks | As written |
| The Draw of Silence | Each blood-active challenger's stream is their blood: a dark-red ribbon body with a frost skin, beads of cruor travelling in and gathering into a knot at his fist. The inactive's stream stays pale | With the hold |
| Arrested Pulse | Mark: the `FROZEN_VEINS` skin on the target (black-red veins from the wrists up, shown through the shared `ThermalSkinLayer`) and a frost crust at the wrists; a small cruor ring round their feet that tightens with each beat. Beat tell (12 ticks): cruor points push out of the body's silhouette and the disc's rim freezes. Burst: five to seven spines out of the body (`CruorJavelinGeometry`'s frost fingers, scaled to 0.9 long), chips. Passed beat: the wrist frost cracks off in pale motes | Mark at the commit; beats at +30, +60, +90 |
| Cruor Javelins | A ribbon of cruor climbs the staff from the heel (4 ticks); shafts freeze together at the cage (`CruorJavelinRules` form); the aim lines are scored frost grooves with a cruor bead running along them; flight point-first shedding powder; impact frost fingers; lodged shafts stand in the floor with a rime collar; on rime they shatter into cruor chips | Draw 0 to 8, form 8 to 22, loose every 4 from the commit, flight up to 12, lodge 40, shatter 10 |
| Veins of Frost | On each feeding figure: veins flaring through its rime (an overlay, so restrained). On the floor: a raised cruor ridge 0.3 wide with a frost crust running along the block seams (straight runs, right-angle turns), its front a bright bead; the meeting disc freezes a ring. Eruption: spines 1.2 to 1.6 tall along every vein, a cluster in the disc | Flare 0 to 6, run 6 to 30 (all fronts arrive together), erupt at 30, sublime over the recovery |
| The Shadow Step | Departure: the dark wells up under him as a pool and he sinks through its surface (the floor hides what is under it); torn smoke tatters rise round him as he goes. Destination: a pool opens, a torn column of liquid dark rises from it (`UmbralTearGeometry`'s arrival tear, scaled to his height). Arrival ring: a low wave of torn dark | Pools at tick 2 and 4; sink 8 to 24; rise 10; ring at +1 over 6 |
| Gloam Cuts | Each cut is a crescent of liquid dark with a torn trailing edge and a cold hairline at its leading edge: low cuts lie close along the floor with a knee-high smoke crest; the high cut stands at face height with its own shadow on the floor; each sheds smoke as it flies and settles into a small pool where it ends | Gather 0 to 22; cuts at +0, +6, +12, each flying 12.5 ticks; pools as they end |
| The Eclipse | The cage closing; the chamber's lights out; the red seam grid (`CRUOR_GLOW` along every floor seam); shapes of liquid dark standing up out of every figure (the devotee model, `poseShapeRise`, then `poseShapeReach`), their reaching arms torn smoke limbs (tapering 0.6 to 0.25, four straight segments) ending in a cruor-red hand; relit: light bursting through his fingers and a ring of torn dark blown back to the walls | Wind-up 40; shapes rise over 20 from tick 40; reaches as R.7; relit: burst 4 ticks, the dark tears back over 12 |

### R.12 Animation beats for the new moves

All on the move's own clock through `BossReplayClock`, as layered deltas over the stance (Visual pass 2, animation):
long still holds, one decisive motion with the body behind it, a slow weighted settle. Strikes run at least 2.5 times
faster through the commit than the wind-up.

| Track | Beats |
|---|---|
| Arrested Pulse (42) | 0–6 the hood turns to the target. 4–18 the crooked hand leaves his face and reaches out to arm's length at chest height, palm up, the fingers curling as round a heart. 18–21.5 they close ever more slowly to a loose cage. 21.5–23 one hard clench: the forearm drops 4°, the chest dips 2°. 23–26 held. 26–42 the hand opens and returns to his face |
| Cruor Javelins (62; 70 in phase 3) | 0–8 the staff comes off the floor and is levelled, pitched 60° forward, the cage aimed at the target; the hand slides up the shaft. 8–22 the claw passes slowly over the cage, palm down, three passes, drawing up. 22–24 the claw lifts a hand's breadth and tenses. From 24, each loose: a flick of the claw outward and down (1.5 ticks), back in 2.5; the cage rocks back 3° with each; the head follows each shaft a beat late. Then 18 ticks: the staff is set down and the hand returns |
| Veins of Frost (58) | 0–6 the hood lowers to the floor. 2–14 the claw lowers to waist height, palm down, fingers spread wide; the staff planted, tilting toward the target. 14–28 the fingers draw in slowly as the veins run, the body leaning 3° in. 28–30 the hand turns palm up and lifts sharply, pulling the spines up. 30–36 held raised. 36–58 it returns to his face |
| The Shadow Step (52) | 0–6 the hood bows. 8–24 he sinks straight down 2.8 blocks, slowly then faster, upright, the claw still raised and the lantern held level so it goes under last; the robe and mantle heap on the surface (`heap()`) and follow him under. At the commit he is moved. 0–10 he rises: the hood first, the shoulders by 4, the hem by 10, the claw raised as it comes up. 10–28 the cloth falls, a slow straightening, the side lantern swings once on its spring. The phase-3 card plays the same sink and rise over the card's 50 ticks |
| Gloam Cuts (64) | 0–8 the claw turns palm out, fingers spread. 8–20 the hand draws back to the left shoulder, coiled, the head turning to the target, the staff planted. 20–22 a held breath. Cut 1 (commit, 2 ticks): an underhand slash down and across, left hip to right knee, the chest bowing 6°. Cut 2 (+6): the low backhand return. Cut 3 (+12): the claw rises over the head and slashes flat across at face height, right to left, the chest rising 4°, the head back. +14–26 the follow-through, held with the arm across his chest while the cuts fly. +26–42 the hand returns to his face |
| The Eclipse (230) | 0–10 the staff comes off the floor; the lantern is lifted before his face at arm's length, the head bowing to it. 10–30 the claw comes over the cage and closes a finger every 5 ticks. 30–38 held. 38–40 a last tight clench. 40–200 utterly still, the dark lantern held in the closed claw before his face; at each reach beat (80, 130, 180) the head turns 10° toward the reaching shapes and back, the one motion. Relit: 0–3 a jolt, the claw flies open off the cage, the head snaps back 8°, the staff knocked back 4°, then into the Gutter's pose. Unbroken: 0–12 the fingers open slowly; 12–30 the lantern is lowered into the Gutter's pose |
| The Splinter Crown (changed) | 26–27.5 the palm-up claw lifts sharply (drawing the splinters up out of the floor), instead of pulling down |
| The devotee shapes | `poseShapeRise` (20 ticks: from the block's own pose to an upright figure stretched to 1.25 times its height, the head last), `poseShapeReach` (leaning forward, one arm raised pointing at the challenger), `poseShapeGrip` (both arms forward, leaning in), `poseShade` (the Procession), each blended through `VelorumDevoteeAnimState` |

### R.13 Sound

No new audio files. Additions to §14:

| Beat | Sound |
|---|---|
| Blood welling in seams (every cruor floor move) | `HONEY_BLOCK_SLIDE` pitch 0.4 at 0.4 |
| A pool spreading / the dark receding | `SCULK_BLOCK_SPREAD` pitch 0.5 at 0.4 / `rite.subject_sink` pitch 0.8 at 0.3 |
| Arrested Pulse | Mark: `rite.ambient.heartbeat` pitch 0.6, heard by the marked; each beat lower (0.55, 0.5, 0.45). Burst: `GLASS_BREAK` pitch 0.4, then `PLAYER_HURT_FREEZE` pitch 0.6. Passed: `AMETHYST_BLOCK_CHIME` pitch 0.8 |
| Cruor Javelins | The draw: `rite.offering_drawn` pitch 0.5. Each shaft forming: `GLASS_PLACE` pitch 0.5. Each loose: `TRIDENT_THROW` pitch 0.6. Impact: `GLASS_BREAK` pitch 0.6; on rime: `GLASS_HIT` pitch 0.8 |
| Veins of Frost | The figures' veins: `POWDER_SNOW_BREAK` pitch 0.5. Eruption: `GLASS_BREAK` pitch 0.45 and `rite.staff_impact` pitch 0.6 |
| The Shadow Step | Sink: `entity.will.dissolve` pitch 0.5. Rise: `entity.will.materialize` pitch 0.5, then `rite.subject_rise` pitch 0.6. Ring: `PLAYER_ATTACK_SWEEP` pitch 0.5 |
| Gloam Cuts | Each cut: `PLAYER_ATTACK_SWEEP`, pitch 0.5 for a low cut and 0.8 for the high one, so the ear tells them apart |
| The Eclipse | Onset: `rite.subject_sink` pitch 0.5 as the fingers close, `BEACON_DEACTIVATE` pitch 0.35 as the light goes. Hold: `rite.ambient.heartbeat` pitch 0.45 every 40 ticks, slow. A reach: `entity.will.materialize` pitch 0.4. A grip: `entity.vesper.grab_telegraph` pitch 0.7. Feeding: `rite.anchor_fill` pitch 0.6. Relit: `rite.sigil_awaken` pitch 0.7. Unbroken: `rite.collapse` pitch 0.6 |

### R.14 Lang and lines

Add-only under the Velorum prefixes: the blood source `blood_source.hemomancy.velorum_kindling`; hints
`message.hemomancy.velorum.hint.dark` ("The dark settles faster. Keep to his light, or keep moving."),
`.hint.pulse` ("Your blood slows. Keep walking.") and `.hint.eclipse` ("Warm blood might light it again."); any
renamed move or hazard name keys for `cruor_release` and the cruor field. Velorum still speaks only his four lines.

### R.15 Tests

**JVM (pure rules, `support/` helpers):**
- `VelorumDarkRulesTest` (new): pool growth and life per source and phase; the 45% cap fading the oldest; `L` per
  state, the Gutter's flood and the 20-tick return; in the dark only outside `L`, outside ally warmth and without
  Searing; the multipliers' order (presence, dark, shelter); 102 and 68 ticks; walking always nets −0.08 or better and
  sprinting −0.3 or better in the worst stack; the Rime cap of two; pools on the boss floor and leash only; coverage
  sampling for the Eclipse gate.
- `VelorumPulseRulesTest` (new): beats at +30, +60, +90; the walking-pace and under-70 pass; the burst's damage and
  disc; passes in an opening; ends on Searing, 10 fed ticks of Projection, preservation's release; two marks only in
  phase 3 with two challengers.
- `VelorumEclipseRulesTest` (new): the gate (state only; two seeds give different start ticks), the Hour and Eclipse
  400-tick mirror, the relight's 20 (30) fed ticks with grace and bleed, relit into a 60-tick Gutter, unbroken
  preserving at 60 and the half Gutter, shapes' 4-block reach, 60-tick rest, pinned shapes never reaching, allies never
  shapes, reach beats at 40, 90, 140.
- `VelorumMovesTest` (extended): the six new timelines meet the floors after the phase-3 cut; every new footprint shows
  before its commit; javelin lanes cut at the first occluder; aims (current, lead, mirror; the phase-3 five); veins as
  grid paths with at most two right-angle turns arriving together; the low cuts clear a jumping box and the high cut
  clears a sneaking box (1.5) but strikes a standing one (1.8); the Step's arrival ring is `JUMPABLE`; destinations
  inside the leash and never in the aisles; untouchable while under; `CRUOR_RELEASE` keeps id 4.
- `VelorumSelectionRulesTest` (extended): weights and pressures of the new moves; two answers a band a phase; no move over
  a third of picks; the Tenebris share of picks and the dark's simulated coverage both grow from phase 1 to phase 3.
- `VelorumCongregationRulesTest` (extended): veins fed by FROZEN and STILLED only; the Eclipse darkens every lantern and
  restores each; vein flare on waking.
- `VelorumStillnessRulesTest`, `VelorumOpeningRulesTest`, `VelorumDeedRulesTest` (extended): the dark's terms; the
  Gutter's new entries and the Veins' Overheld; the three deed lines of R.9.
- Client: `VelorumFrostRulesTest` and `VelorumEffectGeometryTest` (extended): pool edges torn (no closed hard ring),
  limbs within four segments, stains deepening, the javelin form and flight clocks agreeing with `CruorJavelinRules`,
  the red seam grid only in the Eclipse; `VelorumAnimationTest` (extended): the six new tracks rise out of the stance
  and settle back, strike speed through each commit, the Step's sink keeping him upright, the Eclipse's hold flat but for
  the three head turns; `VelorumDevoteeModelTest` (extended): the shape poses start from the block's pose.
- `architecture/VelorumPresentationGuardrailTest` (extended): the ice shader's body colours are red-dominant, blue only
  in its rim, glint and frost-light constants; no Velorum file applies `OBSCURED`; no "black ice" in Velorum lang; the
  dark drawn before the telegraphs; still no vanilla particles, no `hitGlow`.

**GameTests** (`velorum_fight_validation`, real `ServerPlayer`s where a meter is read):
`darkPoolsFillFasterAndRecedeFromTheLantern`, `theGutterFloodsTheDarkToHisFeet`,
`arrestedPulseBurstsOnAStillChallengerAndPassesAWalkingOne`, `cruorJavelinsShatterOnAFrozenFigure`,
`veinsRunFromFrozenFiguresAndNotFromPinnedOnes`, `shadowStepStaysInTheLeashAndCannotBeHitUnder`,
`gloamCutsMissAJumperAndAStoopingChallenger`, `eclipseRelitByProjectionOpensTheGutter` (blood paid through
`velorum_kindling`), `eclipseUnbrokenPreservesAtSixty`, `eclipseRestoresEveryLantern`.

**Review client** (`VelorumFightClientReview`): stops for each new move at tell, commit and impact; `dark_pools`,
`gutter_flood`, `pulse_pass`, `pulse_burst`, `javelins_on_cover`, `veins_run`, `step_sink`, `step_rise`,
`phase3_step_to_ring`, `gloam_cuts_low_high`, `eclipse_dark` (from the `LOW` camera too: the red seam grid must make the
floor readable), `eclipse_reach`, `eclipse_relit`, `eclipse_unbroken`; and the cruor retheme on every existing stop.

### R.16 Build list, in three lanes

No new payload (pools and reaches are owned hazards; marks, stains and steps go through `VelorumEffects` cues and the
kit's synced move), so `PacketHandler.PROTOCOL_VERSION` stays. No new registry entry. Shared files stay add-only.

**SERVER** (`common/entity/boss/saint/velorum/`, lang, GameTests):
1. `VelorumMove`: rename `BLACK_ICE` to `CRUOR_RELEASE` (id 4, key `cruor_release`); add the six moves of R.5 with
   their timelines, footprints, aims, bands, weights, cooldowns, phases, follow-ups (strings E, F, G) and `onTick` /
   `onHit`; retheme the rows' hit effects (Splinter Crown points from the stain record; the Long Night's pools instead of
   `OBSCURED`).
2. New pure `VelorumDarkRules` (pools, growth, `L`, holes, coverage, multipliers), `VelorumPulseRules`,
   `VelorumEclipseRules`; with their JVM tests.
3. New `VelorumDark` (pools as owned `DARK_POOL` hazards, the cap, the in-the-dark test for the meter and the HUD);
   the mark in `VelorumStillness`; the Rime cap of two in `VelorumMoves`; the new source `velorum_kindling`.
4. `VelorumStillnessRules`, `VelorumOpeningRules`, `VelorumSelectionRules`, `VelorumDeedRules`,
   `VelorumLocomotionRules` / `VelorumLocomotion` (the Step as phase 2's close, the phase-3 step to the ring, the
   untouchable span through `damageTakenFactor`).
5. `VelorumCongregationRules` / `VelorumCongregation`: vein sources, the Eclipse's shapes and reach hazards, the
   Eclipse's full lantern swap and restore, ally warmth.
6. `VelorumEntity`: the Eclipse's state trigger beside the Hour's in `tickSaint`; the relight in `onChannel`; the stain
   record. Calls the `VelorumEffects` cues named in the EFFECTS lane.
7. Lang (R.14), the GameTests of R.15.

**EFFECTS** (`client/render/world/Velorum*`, his shaders, `VelorumEffects`, `VelorumVisualRules`, the telegraph pass,
hazards, materials):
1. `shaders/core/world/velorum_ice.fsh`: re-palette to cruor (R.2 tokens; the clot term; frost crust on up faces;
   frost-light rim and glint only). `velorum_frost.fsh`: its "black ice" channel becomes cruor; new shapes for liquid
   dark (surface, torn rim, gloom), the cruor seam bead and ridge, the stain, the gloam cut, the red seam grid. If the
   frost shader grows past about 24 KB, the dark shapes split into a new `velorum_dark` (an add-only `ShaderInit` line
   and a `DARK` type in `VelorumRenderTypes`, built once). Every telegraph fragment takes fog at 0.35. A shader change
   needs a client launch to prove it compiles.
2. `VelorumTelegraphPass`, `VelorumFrostGeometry`, `VelorumIceGeometry` (blades, beads, clots, splinters, slabs, the
   cruor shell), `VelorumFrostRules` (the new clocks), `VelorumRimeGeometry` (the lantern's `L` edge, the seam disc's
   blood), new `VelorumDarkGeometry` (pools, folds, shades' and shapes' smoke, reaching limbs, cuts, the seam grid) and
   `VelorumCruorGeometry` (javelins through `CruorJavelinGeometry`, veins, pulse spines, stains);
   `VelorumTelegraphStyle` claims the new moves and the `DARK_POOL` and reach hazards.
3. `common/.../velorum/VelorumEffects`: cues `stain`, `pulseMark`, `pulseBeat`, `pulseBurst`, `javelinDraw`,
   `veinFlare`, `stepSink`, `stepRise`, `gloamCut`, `eclipseOut`, `eclipseRelit`, `shapeReach`. New pure
   `common/.../velorum/VelorumVisualRules` (a new file; the old one is in `removed_assets/`): the javelin, pulse, step,
   shape and Eclipse timelines, free of Minecraft types like `CruorJavelinRules`.
4. `client/event/VelorumGloamFog` (R.4); `VelorumMeterStyle` (the gloam vignette in `paintScreen`, `muffle` 0.55 in the
   dark, a small cruor heart glyph left of the vessel while marked, slowing and cracking at a burst, the frost teeth as
   cruor tipped with frost); `VelorumFrame` (rail icicles as cruor); `VelorumFrostGlowLayer` (the cruor parts' glow,
   0.15, 0.8 in the Eclipse).

**ANIMATION** (`tools/model_export/velorum.py` and `_anim.java`, the devotee generator, `client/model/entity/boss/velorum/*`,
the renderer's pose code):
1. `velorum.py`: the `ice` material becomes cruor (R.2), on the shoulder, stole and sleeve spires, the claw's ice
   tips and the halo icicles; the glow atlas carries the cruor parts at a dull red and keeps the lantern crystals as
   frost light; regenerate at 16 px per block.
2. `velorum_anim.java`: the six tracks of R.12, the Splinter Crown's changed pull, the phase-3 step; regenerate
   `VelorumModel.java`.
3. `velorum_devotee.py` and `velorum_devotee_anim.java`: black-red veins under the rime in the glow atlas; the shape
   poses (`poseShapeRise`, `poseShapeReach`, `poseShapeGrip`) beside `poseShade`; regenerate.
4. `VelorumAnimState` (priorities for the new tracks, the sink offset), `VelorumDevoteeAnimState`; `VelorumRenderer`
   pose code: the sink, the shades and Eclipse shapes drawn with `VelorumDevoteeModel` through one translucent dark
   render type built once, `shouldRender` true while they are live, `lanternAt` and `clawAt` feeding the javelins and
   cuts.

### R.17 Calls made in this revision (for review)

- `BLACK_ICE` renamed `CRUOR_RELEASE`; "black ice" leaves the player text.
- The dark's numbers (×1.4 on standing terms, ×0.9 drain, 45% cap, `L` 6/5/4) and the Rime cap of two that keeps walking
  a way out.
- The gloam instead of `OBSCURED`, with its own fog.
- The Eclipse's relight by held Projection, its gate (35% dark or 15% health), and putting out the crown lamps and
  votive stands too, trusting the red seam grid to keep it readable.
- The Shadow Step as phase 2's close and the phase-3 transition; phase 3 stays planted.
- Pinning shuts veins and holds shapes; waking does the same by removing the figure.
- Javelins at the target, its lead and its mirror, so the answer is a cut across your own line.

## 0. Resolved 2026-11-05

The user went ahead with every default on 2026-11-05, and the umbrella's "Resolutions that override the designs"
settle the cross-Saint notes. Where this design and the resolutions disagree, the resolutions win; each change below is
also marked "Resolved 2026-11-05" where it lands in the sections that follow.

| Resolution | What changes here |
|---|---|
| Answer 2 | Kept: 30% damage outside openings (the Still Guard), health floors at 60% and 25% that only an opening passes, the last 10% only inside one. |
| Answer 4 | Velorum speaks four lines: the rise ("Be still."), the first preservation ("There. Now you understand."), the proper phase 2 ("You would wake them? They were finished."), the Endless Hour ("Stay."). His voice sheet goes into `docs/NPC_VOICES.md` at the final review. |
| Answer 6 | When his body fails, the woken devotees walk back to their places, kneel, and are preserved again (section 11 as written). |
| Answer 7 | He glides; no legs (section 8 as written). |
| Defaults | The Apotheos pins figures with hyphae; every other question in section 19 takes its default. |
| A-1 | `BLACK_ICE` is a fan: five narrow leaning splinter rows fanned 15 degrees apart from his feet (lanes 0.9 wide, 12 long, the front at 1 block a tick), which then stand as black ice for 60 ticks (Slowness II inside), so the answer is to move through the gaps before they close, not to wait in one. The same fan from phase 1; phase 3 lengthens the rows to 14. |
| A-2 | `SPLINTER_CROWN` rises where each challenger stood still longest in the last 5 seconds (100 ticks), plus two scattered points, and shatters at once: no standing hazard. The black ice is his only standing hazard. |
| A-3 | `HOLLOW_TOLL` strikes anyone whose horizontal speed is below walking pace as the wave passes (full height band; keep moving and it passes through you). `STILL_FLOOR` roots anyone grounded and not moving at its commit; neither is jumped. Velorum has no jump ring. |
| A-4 | `BID_TO_KNEEL` is a gaze, not threads: while his raised lantern's cold cone (20 blocks, 50 degrees, line of sight) holds a challenger, their stillness climbs 1.2 a tick and they are slowed; at the end of the hold a challenger at 60 or more kneels (Slowness IV for 30 ticks). Break sight behind a devotee or a pillar. No thread is drawn; Recall and the Draw of Silence keep their pale threads. |
| A-5 | His planted staff stays the hold of his invocations (the Still Floor's planted recovery, the Endless Hour). |
| A-6 | `LANTERN_SWEEP` is one slow sweep with no return. |
| B-1 | One set of bands for every Saint: HUG under 3, NEAR under 6.5, MID under 12, FAR beyond (`BossBands.STANDARD`). His own bands (2.5, 5.5, 11) go; the weights keep their band names. Re-checked moves: `STAFF_TOLL` (3.5 cone) stays HUG and NEAR; `HANGING_LANTERN` (3.5) stays HUG and NEAR; `SET_IN_PLACE` reads 6 blocks, inside NEAR. |
| B-2 | AREA is any footprint not attached to the body or reaching past 7 blocks. `SET_IN_PLACE` (a disc at the target) becomes AREA with a 20-tick wind-up (from 18). Also AREA: Black-Ice, the Still Floor, Bid to Kneel, the Long Night, the Procession, the Splinter Crown, the Hollow Toll, the Draw of Silence, the Endless Hour. |
| B-3 | Reveals clamp to 20 (area) and 12 (melee), the Beholding deed included. |
| B-4 | The router passes the item's own reach and rate; he never takes more blood a tick than the held item moves: warming (4 a tick), waking (3) and pinning (2) are each the lesser of that number and the item's rate. |
| B-5 | His health scales by 1 + 0.5 x (n - 1) with n from the kit's ratcheted `challengerCount()` (from 1 + 0.6 x (n - 1)); devotee caps still rise by one per extra challenger. |
| B-6 | A worked proper-awakening fight length is in section 15 ("Worked fight length"): about four and a half minutes solo. |
| B-8 | Every animation track blends from the last pose over 5 ticks (from 6). |
| B-10 | The ending resumes on reload (`resumeOnLoad`). |
| B-12 | His hitbox (0.9 by 2.6) is a `getDefaultDimensions` override in `VelorumEntity`; `EntityInit`'s existing line is not edited (K-13 withdrawn). |
| C-1 | "Gutter" stays his: the Guttering Lantern keeps its name. |
| C-2 | The Tithe of Silence becomes the Draw of Silence, `SILENCE_DRAW` ("Silence draws"). |
| C-4 | Death messages are `death.attack.hemomancy.saint_velorum` and `.player` (the kit shipped them). |
| D-2 | The frozen cruor on his halo links stays (the lore's own appearance text); dark, desaturated and small. |
| D-3 | The Endless Hour slows the lantern motes to a crawl (a tenth) and never stops them; the damping is on the players (the doubled stillness fill), not on the world. Sound thins; nothing freezes in mid-air. His own secondary motion still damps to near nothing, since he is the still thing. |
| D-4 | Harder, not longer, with the worked numbers in section 10: the Repose deed's Bow is 40 ticks at x2.625 damage taken, so the damage a Bow admits is unchanged (70 x 1.5 = 40 x 2.625); the Litany deed removes opening extensions but brings the Long Night (and so the Guttering Lantern) about a fifth more often (its spacing 300 to 240 ticks, its pressure +7.2 per 100 ticks). |
| D-5 | Every drain is a named `BloodAccounting.Source` in his package: `velorum_warming` (Projection into him in the Gutter), `velorum_waking` (a figure woken), `velorum_pinning` (a figure pinned), `velorum_thawing` (a preserved ally warmed out) and `velorum_silence` (the Draw of Silence). |
| D-7 | The Endless Hour is triggered by state, never by a cooldown: on entering phase 3, then again when the fight calls for it (two or more challengers, or one challenger above half stillness, or the 10% floor reached outside an opening), never within 400 ticks of the last Hour's end. |
| E | `FrozenDevoteeBlock` is not edited (K-11 withdrawn): the channel router hands him the block a held Projection aims at. Removing dead lang keys and the old Velorum cases in the shared tests is the compile gate's. |

## 1. The fight in one paragraph

Velorum fights from stillness and asks the player for the opposite. Every challenger carries a stillness meter under his
bar: it climbs while they stand, sneak, hold an item or are slowed, and only travel brings it down; at full they are
preserved, shut in clear rime for a moment and hurt badly, one more figure in his congregation. Outside his openings his
still body barely answers a blow, so the player learns within the first minute that the fight is not won by trading
hits: it is won by staying in motion through everything he does to slow them, reading the three moments when his
stillness slips (the lantern that gutters after he spends its light on darkness, the invocation he holds a breath too
long, the bow he makes beneath a wound) and spending those windows well. The frozen congregation around the floor is
the arena: the figures are cover from his gaze and his darkness, and a player who woke him properly can pour warm blood
into them during an opening and wake them to fight beside them, sheltering the living from the cold and holding him in
place, while he tries to call each one back to stillness. An Apotheos who forced the door finds the congregation turning
on the hive in them instead. Winning feels like warmth returning: the last hit lands in a bow, the crooked hand lowers
for the first time in the fight, the hood bows down and the frost falls out of his lanterns' light, and the woken
faithful walk back to their places and kneel, as he always said they would.

## 2. What stays and what goes

Paths are under `src/main/java/com/vincenthuto/hemomancy/` unless they start with `tools/`, `docs/` or `src/`.

**Stays (extended or regenerated):**

| What | Where | What happens to it |
|---|---|---|
| The Saint body: id `hemomancy:velorum`, `saintType()`, the bar binding to `BossPresentationIds.FRAME_VELORUM`, `announceTrialEnd` and its line `message.hemomancy.velorum.returns_to_stillness` | `common/entity/boss/saint/velorum/VelorumEntity.java` | Rewritten onto the kit (controller, arena, meter, phases as fields of `SaintTrialEntity`); its own `aiStep` timers go |
| The shared Saint plumbing: rise, tomb binding, `endTrial`, enrage record and skipped Chapels, save/load | `common/entity/boss/saint/SaintTrialEntity.java`, `SaintRise.java`, `SaintEnrageRules.java` | Unchanged except where the kit changes it (kit stage, not this builder) |
| The blood source `velorum_silence` (`BloodAccounting.Source`, `Category.HAZARD`) and its lang key `blood_source.hemomancy.velorum_silence` | `VelorumEntity.SILENCE` | Moves to `VelorumMoves` and is used only by the Litany deed's Tithe of Silence (section 10) |
| The world material `ManipulationVisuals.Form.VELORUM_RIME`, the `RIME` and `UMBRA` LuxUmbra materials, the `spire` primitive | `client/render/world/VelorumRimeGeometry.java` | Extended per move and per impact (section 12); the frost-nova square, veil curtains and stillness ribbons go |
| The frost glow layer on the eyes render type | `client/render/layer/mob/VelorumFrostGlowLayer.java` | Kept; its alpha reads the lantern and opening state instead of nova envelopes |
| The rig, atlases and Blockbench project | `tools/model_export/velorum.py`, `tools/model_export/velorum_anim.java`, generated `client/model/entity/boss/velorum/VelorumModel.java` | Regenerated at 16 px per block with new parts; `velorum_anim.java` rewritten as Java keyframe tracks |
| The bar frame | `client/screen/overlay/boss/frame/VelorumFrame.java` | Three phases and three notches instead of two and one |
| The renderer | `client/render/entity/boss/velorum/VelorumRenderer.java` | Kept; it also draws the Procession's shades and records the lantern and wound anchors |
| The congregation set piece | `common/block/harbinger/decoration/FrozenDevoteeBlock.java`, `tools/model_export/frozen_devotee.py` | Block and models unchanged; the block gains a blood-endpoint hook that routes into Velorum's congregation (kit request K-11) |
| The chamber's congregation | `tools/worldgen/mausoleum/themes/velorum.py` (`chamber_sockets`, `dress_chamber`) | Kept; `chamber_sockets` also fills the two `aisle_end` sockets (section 9) |
| The fight review client | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/VelorumFightClientReview.java`, `-PvelorumFightReview` on `bossPresentationReviewClient` | Rewritten with the stops in section 16 |

**Goes (deleted outright; the mod is not released, so no save migration. Removed assets go to the untracked
`removed_assets/` folder during the build, not now):**

| What | Where | Replaced by |
|---|---|---|
| The fixed-cadence Frost Nova (every 100 ticks, 10 blocks, 3 damage, Slowness III and a 20-tick Slowness VII root) | `VelorumEntity.fireFrostNova`, `FROST_NOVA_*` | The Still Floor (a committed move with a jump answer) |
| The Veil of Darkness (every 160 ticks: Blindness 60 ticks, Nausea 80 for the blood-active) | `VelorumEntity.fireVeilOfDarkness`, `DARKNESS_*`, lang `message.hemomancy.velorum.darkness_closes_in_silence_answers` | The Long Night, which narrows sight through the existing `OBSCURED` school state and is never a blackout |
| The 25%-health Silence drain (200 blood every 80 ticks within 8 blocks) | `VelorumEntity.fireSilenceDrain`, `SILENCE_*` | The Litany deed's Tithe of Silence (section 10) |
| The Still Guard Resistance I after each hit, and its synced flags | `VelorumEntity.hurt`, `DATA_STILL_GUARD`, `DATA_STILL_GUARD_TICKS`, `stillGuardTimer` | The Still Guard damage rule (section 6): a constant, not a rhythm of potions |
| The integer stillness pressure (plus 1 still, minus 2 moving, minus 5 interrupted; rooted at 80 with Slowness VII each second) and its action-bar line every 20 ticks | `VelorumStillnessRules.java`, `VelorumEntity.tickStillness`, `DATA_STILLNESS_PRESSURE`, `DATA_STILLNESS_BREAK`, lang `message.hemomancy.velorum.stillness_pressure` | The per-player `SaintMeter` (section 4); `VelorumStillnessRules.java` is rewritten in place with the new rules |
| The hit-opened 60-tick "stillness break" with its 180-tick cooldown | `VelorumEntity.hurt`, `VelorumStillnessRules.BREAK_*` | The three openings (section 6) |
| The presentation envelopes (`FROST_INTERVAL`, `VEIL_INTERVAL`, `DRAIN_INTERVAL`, the `Phase` and `Pose` records) and the seven `DATA_*_UNTIL` / `DATA_*_RELEASE` accessors | `VelorumVisualRules.java`, `VelorumEntity` | The kit's synced move block read through `BossReplayClock`; `VelorumVisualRules.java` is deleted |
| The four parallel clocks | `client/model/entity/boss/velorum/VelorumAnimState.java` | One move clock, one opening clock, one phase clock |
| The two secondary moves Frozen Advance and Shadow Cross, and their cue lines `message.hemomancy.saint_pattern.frozen_advance` / `.shadow_cross` | `SaintAttackPattern`, `SaintSecondaryAttacks` (the kit removes both) | Frozen Advance survives reshaped as Black-Ice Splinters; Shadow Cross is retired |
| `registerTrialGoals(0.5D)`: the vanilla `MeleeAttackGoal`, stroll and look goals | `VelorumEntity.registerGoals` | The controller and the near-stillness locomotion (section 8) |
| `HemoParticleData.hitGlow` on every burst, including the stillness pulses on players | `VelorumEffects.burst` | Fine `HemoParticleData.glow` motes as accents only; geometry carries every effect |
| Guardian ambient, generic hurt, elder guardian death | `VelorumEntity.getAmbientSound`, `getHurtSound`, `getDeathSound` | Section 14 |
| Tests pinned to the old powers | `src/test/.../saint/velorum/VelorumVisualRulesTest.java`, the Velorum case of `SaintPowerRulesTest`, the two Velorum cases of `SaintPowerGameTests`, the strings `"100, 2, false, false, true"` / `"60, 0, false, false, true"` in `architecture/VelorumPresentationGuardrailTest.java`, the Velorum rows of `client/render/world/SaintPatternGeometryTest.java` and `VelorumRimeGeometryTest.java` | The tests in section 16 |

**Internal MARTYR names.** A search of `src/` finds none left: `DATA_MARTYRDOM` (SAINT_MAUSOLEUMS open question,
OPEN_QUESTIONS C-5) is already gone, and the only hit is the guard in `SaintLiberPagesTest`. The build adds a source
assertion that no Velorum file says "martyr", so the code half of C-5 can close. `VelorumFrame`'s javadoc name "the
Frozen StillGuard" goes with the frame edit.

## 3. Phases

Health is the bar, and health is his body: "his body fails" is the ending. Two things make it a stillness fight and not
a damage race. Outside an opening he takes 30% damage (the Still Guard), and each phase floor holds until an opening is
taken: outside openings his health cannot fall below 60%, then 25%, and below 10% only hits inside an opening hurt him
at all (section 6).

| Phase | Trigger | What it adds | Bar colour and name | Phase card |
|---|---|---|---|---|
| 1. The Hour | The rise ends (100% health) | The meter, the presence, the Still Guard; Staff Toll, Lantern Sweep, the Hanging Lantern, Black-Ice Splinters, the Still Floor, Bid to Kneel, the Long Night, Set in Place; the Guttering Lantern and the Overheld Invocation; waking (proper) or pinning (forced) the congregation; string A (Toll into Sweep) and string B (Bid into Release) | `BLUE`, "Saint Velorum" (`boss.hemomancy.velorum.name`) | None; the rise card stays |
| 2. The Congregation | Health reaches the 60% floor; he bows (the Bow is the transition) and the card plays as the Bow ends | Recall to Stillness (proper) or Call to Prayer (forced); the Procession of Shades; the Splinter Crown; string A grows a third hit (Toll, Sweep, Release); string C (Still Floor into Crown); in a forced fight two devotees rise against the challengers; glide and turn a little faster | `WHITE`, "Velorum, Before His Congregation" (`boss.hemomancy.velorum.phase.congregation`) | "The Congregation" (`SCENE_VELORUM_CONGREGATION_CARD`, 50 ticks, frost white 0xB8DCF4) |
| 3. The Endless Hour | Health reaches the 25% floor; he bows; the card plays; he glides to the centre of the floor's blue-ice ring and begins the Endless Hour | The Endless Hour (once at entry, then at most every 1,200 ticks); the Hollow Toll; string D (Toll into Hollow Toll, Hollow into a second Hollow); he is planted and only turns; wind-ups 15% shorter, clamped to the floors (12 for melee, 20 for area moves); in a forced fight two more devotees rise | `PURPLE`, "Velorum, in the Endless Hour" (`boss.hemomancy.velorum.phase.endless_hour`) | "The Endless Hour" (`SCENE_VELORUM_HOUR_CARD`, 50 ticks, ice 0x9ACCF0) |
| Failing | Health reaches 0 from a hit inside an opening (below 10% nothing else can hurt him) | The ending (section 11) | The bar empties; the frame's orb goes dark | None |

The cards are `PhaseCardScene`s registered in `BossSceneCatalog` beside Hemorath's `hemorath_empties`, with their title
keys in the pattern that card uses. A phase change never cuts an active move: the floor is reached, the hit that reached
it lands, the current move finishes its recovery, then the Bow starts (kit rule).

## 4. The signature meter: stillness

**Owner.** Each challenger has their own; it is a per-player `SaintMeter` synced through the kit's `SaintMeterPacket`.
It is fight state, not player data: a UUID-keyed map on the Velorum entity, dropped when the player leaves the arena or
logs out and cleared with the trial; nothing durable is added. It does not scale with the number of challengers.

**Who it applies to.** Every challenger inside the arena's chamber box (`SaintArena`), alive and not a spectator,
whatever their path and whether their blood is active. It is not limited by line of sight; the presence (section 5) is.

**Fill, per tick (summed).** Travel is horizontal distance since the last tick.

| State | Fill |
|---|---|
| Standing: travel under 0.08 blocks, on the ground or in the air (jumping in place does not help) | +0.70 |
| Sneaking, moving or not (replaces standing) | +0.70 |
| Using an item or holding a channel: eating, drinking, a raised shield, a drawn bow, a charging cast, held Blood Absorption or Projection | +0.30 (adds to standing; walking while using an item is slow enough to count as standing) |
| Slowed | +0.10 per Slowness level, +0.05 per level of the `RIME` school state |
| In his presence with line of sight (section 5) | every fill term ×1.5 |
| Within 3 blocks of an awake ally devotee (proper fights) | every fill term ×0.5, applied after the presence |
| Inside a watching devotee's gaze (Beholding deed, section 10) | counts as standing unless sprinting |

**Drain, per tick.** Only travel lowers it: walking (0.08 blocks or more, not sneaking) −0.55, sprinting −0.90; in the
presence the drain is ×0.8. Nothing else lowers it except the Bow (below) and a release.

**Openings halt it.** While any opening is open (section 6), every fill term is zero for every challenger; the drain
still runs. During the Bow it also drains −0.8 a tick for everyone, moving or not.

**Thresholds.**

| Value | Name | Effect |
|---|---|---|
| 0–39 | Warm | None |
| 40 | Cold | Healing from every source ×0.5 (a new `HealModifier` entry, kit request K-10); the HUD frost creeps past its first tick; a faint frost at the screen's edges |
| 70 | Stilling | Healing ×0 (the same entry) and Slowness I while at or above 70, which feeds the fill: the spiral the player must walk out of (walking still nets −0.45 a tick, −0.29 in his presence) |
| 100 | Preserved | See below |

The healing rule is what "stops players resting": standing to eat or regenerate is how the meter fills, and once it is
cold the rest does not work anyway.

**Preserved.** At 100 the player is shut in clear rime for 30 ticks: movement and jump strength held to zero through
`AttributeModifiers.sync` (transient, removed on release, logout and death), 6 damage at once (`hemomancy:saint_velorum`,
death message "was preserved by Velorum"), and Velorum's moves deal ×1.5 to them while they are held. The ice breaks
early if the player takes 14 damage in it, if anyone else (a challenger or an ally devotee) hits the ice twice, or if
another challenger holds Blood Projection on them for 15 fed ticks (2 blood a tick, warming them out). On release the
meter is set to 50 and their fill is suspended for 60 ticks ("thawed"). The meter cannot pass 95 again until 200 ticks
after the last preservation, so it can never lock a player.

**Numbers in short.** From 0, standing still fills in 143 ticks (7 seconds), 95 in his presence; holding Projection
while standing fills in 100, 67 in his presence. Walking empties it from full in 182 ticks, sprinting in 111.

**Teaching.** The meter appears with the bar at the end of the rise. Velorum's first 40 ticks are stillness: he does not
move, and a player who stands to look at him watches the frost creep. One-time action-bar hints per player per fight,
all lang keys under `message.hemomancy.velorum.hint.*`: at the first Cold ("The cold settles while you stand. Keep
moving."), at the first opening ("His light gutters. The cold holds off."), at the first preservation, and in reach of a
frozen devotee, which differs by awakening (proper: "Warm blood might wake them."; forced: "They will not rise for you.").
The opening pressure on the Long Night (section 7) makes the first opening come inside the first minute.

**HUD meter (`VelorumMeterStyle`, the kit's `SaintMeterStyle`).** Painted with `GuiCanvas` like the bar frames, in the
frame's palette (`FROST_DARK` 0xFF0E1626, `FROST` 0xFF2A3E5C, `FROST_LIP` 0xFFB8DCF4, `ICE` 0x9ACCF0, `TENEBRIS`
0x140A1E), centred 4 px under the Velorum frame:

- **The vessel.** A slender rime-glass tube 120 by 7 px: a dark rail with a one-pixel pale lip along its top, and at each
  end a small hematic-iron cap drawn as a lantern cage (two bars and a crystal dot), echoing his three lanterns. Iron is
  dark blue-grey (0xFF24262F with a 0xFF5A6078 bevel); no silver or brass.
- **The fill is frost, not liquid.** It creeps in from both ends toward the centre, each end reaching `value / 100` of
  half the tube. The front is crystalline: alternating small teeth (`tri`) in `ICE` turning toward 0xE8F4FF as it
  rises, with a few six-armed flakes (the frame's `flake`, moved into a shared `VelorumFrostPaint` helper in the frame
  package) turning slowly.
- **The pupil.** Under the centre sits the frame's dark Tenebris orb at 3.4 px. At 100 the two fronts meet over it: the
  vessel frosts white, a hairline crack runs across, and the frost thaws back from the centre as the 30 preserved ticks
  run out.
- **Ticks.** One pale hairline tick from each end at 40, two at 70.
- **Halted.** In an opening the flakes stop turning, the front's sparkle stops, and a hairline dark seam marks each
  front: the cold is held. In the Bow the fronts visibly recede.
- **Glyphs.** Left of the vessel, an 8 px kneeling silhouette lights pale while the player is sheltered by an ally
  devotee. Right of it, a small eye glyph opens while they stand in a watching devotee's gaze (Beholding deed only).
- No number and no clock face: nothing on the HUD reads as time.
- **Screen edge.** At Cold and above, the style paints a frost border at the screen's edges, 0.10 alpha at 40 rising to
  0.25 at 99, never over the crosshair's middle third (kit request K-8 for the full-screen paint hook).

## 5. Presence: the chill

| | |
|---|---|
| Radius | 10 blocks from his feet (horizontal), within 4 blocks above or below |
| Line of sight | Required, by the standard collider ray from his eyes to the player's body centre. Frozen devotees are blocks with outlines and pillars are pillars, so kneeling behind a figure or a rank pillar takes a player out of the presence: the congregation is cover |
| Cadence | The meter multipliers apply every tick; the chill pulse every 20 ticks |
| Effect | Fill ×1.5 and drain ×0.8 (section 4). Each chill pulse refreshes one level of the `RIME` school state for 40 ticks on every challenger inside (`SchoolStates.apply`, so the existing rime visuals show on them and the existing Searing rule clears it: a Flammeus player can burn their own chill off) |
| Ground read | A disc of hairline frost seams 10 blocks across on the floor under him (a `ground` card with the `RIPPLE` sprite in `RIME`), alpha 0.06 plus 0.10 times the local player's stillness, so the read strengthens as the player grows still; fine `HemoParticleData.glow` motes rise from the rim every 10 ticks on the client. No ribbons: the six lines the current code draws from the rim to his core go |

## 6. Openings

Three windows, as the brief names them. The Saint decides when he is open; the player decides whether to take it.

**The Still Guard (outside openings).** He takes 30% of incoming damage. His health cannot be brought below the next
floor (60%, then 25%) except by damage dealt inside an opening; at a floor, damage outside an opening is refused
entirely. Below 10% nothing outside an opening hurts him. Congeatio school states on him are refused (he is the cold);
Searing on him is accepted and matters in openings.

| Opening | Entry | Tell | Length | What counts | Damage taken | Feeds |
|---|---|---|---|---|---|---|
| **The Guttering Lantern** | Always, when the Long Night ends (and when an unbroken Endless Hour closes, at half length) | The staff lantern's crystal shrinks to a point; the glow layer's crystals drop to a quarter; the folds of darkness sink into the floor; the pillar lanterns stay dark; he lowers the staff and leans his head toward its light. HUD: the frost halts | 60 ticks in phase 1, 50 in phase 2, 40 in phase 3 | Any hit. Held Blood Projection on him: each fed tick pays 4 blood (`velorum_warming` source, kit request K-6), deals 1 damage without the guard and adds a tick to the window for every four fed, up to +20. The first Searing applied to him in the window adds 10 ticks, once | ×1.0 | Every challenger's meter halts; damage passes the floors; if challengers deal 6% of his maximum health inside it, it runs straight into the Bow |
| **The Overheld Invocation** | During the active span of Bid to Kneel, Recall to Stillness or the Tithe of Silence: three hits from any challengers, or 4% of his maximum health in damage | His raised hand shakes; the pale threads snap back into the palm with one fine glint each; his head jerks up and the hood falls back a little; the move ends at once | 50 ticks | Any hit; school casts deal ×1.25 | ×1.0, school casts ×1.25 | The meter halts; a devotee he was recalling is spared; 6% inside runs into the Bow |
| **The Bow** | A phase floor is reached; a Gutter or Overheld window is punished for 6% of his maximum; an Endless Hour is broken | "He bows beneath a wound without losing the invocation": head and hood dip to the chest, the staff leans on his shoulder, the crooked hand stays raised; a pale seam opens across his breast (the new `breast_seam` part lit in the glow layer); the body's frost dulls | 70 ticks (100 after a broken Endless Hour; 40 if the Repose was skipped) | Any hit ×1.5. Held Blood Absorption on the seam draws the cold blood out: 1.5 damage a tick without the guard, every challenger's meter −1 a tick more, and the absorber gains 2 blood a tick | ×1.5 | Every meter drains −0.8 a tick; a phase floor passes when it ends; the absorbing player is the clearest Absorption verb in the fight |

Only one opening is open at a time; a Gutter or Overheld that runs into the Bow ends where the Bow begins. No opening can
follow an opening except into the Bow. All thresholds are fractions of his maximum health, so they scale with
challengers.

## 7. Moveset

Damage is for Normal before the enrage; every move's damage is multiplied by his attack attribute over its base of 9,
so the forced awakening's +30% reaches every move. Damage uses `hemomancy:saint_velorum` through
`ManipulationCombatHelper.canHarmFor`. Ticks are wind-up / active / recovery. Bands (resolved 2026-11-05: the shared `BossBands.STANDARD`, HUG under 3, NEAR under 6.5, MID under 12; the original read: Velorum's): HUG under 2.5 blocks,
NEAR 2.5 to 5.5, MID 5.5 to 11, FAR over 11. Sectors: FRONT to 60°, FLANK 60° to 120°, REAR beyond. "Stillness" is added
to the struck player's meter on a hit. Every move aims by tracking until its commit tick and then locking, unless the row
says otherwise.

| Id | Name | Phases | Band and sector (base weight) | Tell: animation beat and world geometry | Footprint | Timeline | Damage and effect | Counterplay | Follow-ups |
|---|---|---|---|---|---|---|---|---|---|
| `STAFF_TOLL` | Staff Toll | 1–3 | HUG 30, NEAR 24; FRONT (FLANK ×0.5) | Both hands lift the lantern staff to the shoulder, heel forward; hairline frost seams creep out of his hem across the cone | Cone 70°, 3.5 blocks, height 0–2.5 | 16 / 3 / 18 | 9; +12 stillness | Step out of the cone to either side, or get behind him | `LANTERN_SWEEP` (60% if the target is still NEAR, link 4); in phase 3 `HOLLOW_TOLL` instead (55%, link 6) |
| `LANTERN_SWEEP` | Lantern Sweep | 1–3 | HUG 10, NEAR 26, MID 8; FRONT, FLANK | The staff is drawn back across his body, the lantern swinging low behind his left hip, its crystals brightening; a ring segment of fine glow motes marks the arc's outer edge on the floor | Cone sweep 160°, 1 to 5 blocks, swept left to right over the 6 active ticks, height 0–2.4 | 14 / 6 / 16 | 8; Slowness I for 40 ticks (which feeds the meter) | Back off past 5 blocks or move with the sweep's direction | In phase 2 and 3, `BLACK_ICE` (50% if the target is MID after the sweep, link 6): string A's third hit |
| `HANGING_LANTERN` | The Hanging Lantern | 1–3 | HUG 30, NEAR 18 REAR; FLANK 8 | He does not turn. The side chain gathers at his left hip and its small lantern brightens; a rear arc of seams opens behind him | Rear cone 120° (centred on his back), 3.5 blocks, height 0–2 | 14 / 4 / 16 | 6; +15 stillness; Slowness I for 30 ticks | Leave the arc to the flank, or stay further than 3.5 | None |
| `BLACK_ICE` | Black-Ice Splinters (the Release) | 1–3 | NEAR 10, MID 26, FAR 22; FRONT (FLANK 8) | "Cold gathers in fractured seams under his feet before the raised claw releases it": the crooked claw rises, a knot of dark seams forms at his hem, then three seam lanes race ahead along the floor toward the aim | Three lanes (the centre and side lanes at ±2 blocks), 0.9 wide, 12 long; the eruption front runs out 1 block a tick from his feet. Phase 3: five lanes fanned ±30° | 24 / 12 / 22 | 10; +20 stillness; a 0.4 toss. The splinters stand 60 ticks as a field (Slowness II inside a lane) | Stand in a gap (1.1 blocks between lanes) or move diagonally; do not fight inside the splinters | None |
| `STILL_FLOOR` | The Still Floor | 1–3 | HUG 12, NEAR 18, MID 16, FAR 4; any sector; +12 if a challenger in the ring is at 60 stillness or more | He plants the staff's heel; the lantern crystals ring; frost seams spread radially across the floor to 10 blocks over the wind-up, a bare eye of 2.5 blocks round him staying clear; on the commit tick the seams flash white | Annulus 2.5 to 10 blocks; hits only players on the ground on the commit tick and the two after it | 30 / 3 / 30 | 6; rooted for 20 ticks (held modifiers); +25 stillness | Jump as the seams flash, stand in the eye, or be beyond 10. The 30-tick recovery has the staff planted and him not turning: a free flank or rear punish, still guarded | In phase 2 and 3, `SPLINTER_CROWN` (50% if anyone was rooted, link 6): string C |
| `BID_TO_KNEEL` | Bid to Kneel | 1–3 | MID 14, FAR 26; FRONT, FLANK; kite pressure +3 per 20 ticks the target spends in MID or FAR, capped at +24, reset when it starts | "Silence draws pale threads inward through the crooked hand": the hood turns to the target, the hand lifts palm out, and five hairline threads begin to draw from the target's chest toward the palm, lengthening over the wind-up | A held line from his hand to the target, tested by line of sight each tick, out to 20 blocks. Phase 3: two targets | 28 / 30 (held) / 16 | No damage. Each held tick the target has line of sight to him: +1.2 stillness. At the end of the hold, if their meter is 60 or more they kneel: Slowness IV for 30 ticks. Litany deed: also the Tithe's drain (section 10) | Break line of sight behind a pillar or a frozen devotee; or hit him: three hits or 4% of his maximum in the hold is the Overheld Invocation | `BLACK_ICE` at the kneeling target (80%, link 4): string B |
| `LONG_NIGHT` | The Long Night | 1–3 | Any band and sector; base 6, +6 for every 100 ticks since the last opening ended, capped at 48; never within 300 ticks of the last | He lifts the lantern staff high; "darkness rises in narrow folds around the lantern light": eight narrow umbral folds rise round him as the pillar lanterns flicker | Everyone in the chamber box with line of sight to the staff lantern on the commit tick | 32 / 10 / 0, then the Guttering Lantern | 2; `OBSCURED` for 120 ticks (the existing school-state fog: near 4, far 12 blocks; narrowed sight, never black). The ten rank-pillar rime lanterns go dark for 180 ticks through the arena (section 9) | Break line of sight to the lantern on the commit tick (pillars, figures). Then take the opening | Always ends in the Guttering Lantern |
| `SET_IN_PLACE` | Set in Place | 1–3 | Only against a preserved or kneeling challenger within 6 blocks: 60, overriding the others | He glides to the figure and lifts the claw over it; the rime on it brightens | Disc 1.2 at the target, height 0–2.5 | 18 / 2 / 24 | 10 (×1.5 against the preserved: 15); frees the preserved player | Others break the ice first (two hits) or warm it out with Projection; the kneeling can still walk out at Slowness IV | None |
| `RECALL` | Recall to Stillness | 2–3, proper and NONE awakenings | 30 when an awake ally devotee is within 14 blocks in his line of sight | He turns to the devotee, lowers the lantern and raises the claw; pale threads draw from the devotee's chest; the devotee slows and begins to sink toward kneeling | A held line to the devotee, tested by line of sight | 20 / 40 (held) / 18 | The devotee falls still where it stands (section 9), with a 300-tick lockout before it can be rewoken, at double cost | Break the line (lead the devotee behind a pillar) or overhold him: three hits or 4% in the hold | None |
| `CALL_TO_PRAYER` | Call to Prayer | 2–3, forced awakenings | 16 while fewer hostile devotees are awake than the cap | He lifts the lantern toward an aisle; the rime on the chosen figures glints; he lowers it slowly | The two unpinned frozen devotees nearest any challenger | 24 / 6 / 20 | They rise against the challengers (section 9) | Pin the figures beforehand (section 9); kill the risen (30 health) | None |
| `PROCESSION` | The Procession of Shades | 2–3 | MID 14, FAR 20; any sector; +16 if the target has had no line of sight to him for 40 ticks | He turns his hood along a line and lifts the lantern; at the line's far end three dark figures fade in, the Procession's striding pose, their bare feet frosting the floor | A path 1.2 wide and up to 18 blocks long through the target's position, square to his view; three shades 2 blocks apart walk it at 0.5 blocks a tick | 26 / 44 / 14 | 4 per shade touched; +30 stillness; the shade breaks into rime | Step off the lane; the shades walk straight and pass through figures and pillars | None |
| `SPLINTER_CROWN` | The Splinter Crown | 2–3 | NEAR 10, MID 18, FAR 12; any sector | The halo's hanging icicles crack free and lean outward as the halo turns slowly; landing rings appear under every challenger and at two scattered points from tick 8, lock at tick 14 | Scattered discs of radius 1.5 (one under each challenger plus two, at most eight), locked at tick 14, landing at the commit | 26 / 4 / 20 | 7 each; +10 stillness; an icicle stump stands 40 ticks (geometry only) | Step out of the ring after it locks | None |
| `ENDLESS_HOUR` | The Endless Hour | 3 | Forced at phase 3's start; after that weight 10, at most every 1,200 ticks | He stands at the centre of the floor's blue-ice ring utterly still, claw raised; every secondary motion on his body (cloth, chains, crystals) damps to nothing; every rime lantern in the chamber goes dark, the sea lanterns stay; the lantern light shrinks to its core; sound thins | The chamber box | 30 / 200 (held) / 30 | For the 200 ticks every challenger's fill is ×2 and drain ×0.5, and he takes damage without the Still Guard. If challengers deal 8% of his maximum in the hold (Searing hits count double toward it), the invocation breaks into a 100-tick Bow. If the hold runs out, the Hour closes: every challenger at 50 or more is preserved, the rest take 6, and the Guttering Lantern follows at half length | Keep running circles while landing hits; fight close to ally devotees for their shelter; Flammeus casts break it fastest | None |
| `HOLLOW_TOLL` | The Hollow Toll | 3 | HUG 30, NEAR 22; any sector (he is planted and needs a rear answer) | The staff is lifted upright and brought straight down; he does not turn; a ring of frost leaves his feet | Expanding ring wave 1 block thick, out to 9 blocks at 0.45 blocks a tick, height 0–0.8 | 24 / 20 / 20 (20 after the phase-3 cut) | 7; +15 stillness | Jump the ring as it passes | A second ring (55%, link 8): string D |
| `TITHE` | The Tithe of Silence | 1–3, forced with the Litany skipped | MID 12, NEAR 10; any sector | The crooked hand closes slowly; threads draw from every challenger within 10 blocks | Disc 10 round him, line of sight each tick | 24 / 20 (held) / 18 | Blood-active challengers pay 6 blood a tick (`velorum_silence`, up to 120); the inactive take 1 damage every 5 ticks | Leave the 10 blocks or break line of sight; overhold it | None |

> Resolved 2026-11-05: the table's rows for `LANTERN_SWEEP` (one slow sweep, no return), `BLACK_ICE` (the five-row
> fan from phase 1, standing 60 ticks as black ice; rows 14 long in phase 3), `STILL_FLOOR` (roots the grounded and
> unmoving at its commit; not jumped), `BID_TO_KNEEL` (the lantern's gaze cone, 20 blocks and 50 degrees, no threads),
> `SET_IN_PLACE` (AREA, 20 / 2 / 24), `SPLINTER_CROWN` (where each challenger stood still longest in the last 100
> ticks, plus two; no stump hazard), `ENDLESS_HOUR` (triggered by state, section 0 D-7; not picked by weight),
> `HOLLOW_TOLL` (strikes anyone below walking pace as it passes; full height; 24 / 20 / 20) and `TITHE`, now
> `SILENCE_DRAW`, the Draw of Silence, read as section 0 says. Bands are `BossBands.STANDARD` (B-1).

That is fifteen moves (thirteen in any one fight, since Recall and Call to Prayer belong to different awakenings and the
Tithe to one deed), with every phase adding moves, and four strings.

**Cooldowns (ticks, from the move's end):** Staff Toll 0, Lantern Sweep 40, Hanging Lantern 60, Black-Ice 80, Still
Floor 140, Bid to Kneel 160, Long Night 300, Set in Place 60, Recall 200, Call to Prayer 300, Procession 240, Splinter
Crown 160, Hollow Toll 60, Endless Hour 1,200, Tithe 220.

**Decision delay** (between a recovery's end and the next wind-up, outside strings): 24 ticks in phase 1, 18 in
phase 2, 12 in phase 3. **Link gaps** are in the table.

**Picker rules.** The kit's anti-repeat history and pressure apply. Velorum's own pressures: kite pressure (Bid to
Kneel, above), opening pressure (the Long Night, above), hiding pressure (the Procession, above), and stillness pressure
(the Still Floor, above). A move whose weight is zero in the target's band and sector is not eligible. Every band has at
least two answers in every phase (HUG: Toll, Sweep, the Hanging Lantern behind, the Still Floor; NEAR: Toll, Sweep,
Black-Ice, the Still Floor; MID: Black-Ice, the Still Floor, Bid; FAR: Black-Ice, Bid, the Long Night, the Procession
from phase 2). The target is the challenger he last chose, kept for at least 60 ticks unless they leave line of sight
and range; he prefers whoever has the highest stillness when two are equally placed. Allies are targets only for
Recall, and for Staff Toll or the Hanging Lantern while they grip him.

**Hit rules.** Non-pulsing moves strike each player once; the Procession strikes once per shade. Footprints also strike
ally devotees (they are in his way) but never hostile ones.

## 8. Locomotion: near-stillness

Velorum never runs and never walks: he has no stride. He glides on the rime, the hem's frost trail drawn along the
floor, his robe hanging still.

- **Glide.** 0.10 blocks a tick in phase 1, 0.12 in phase 2, none in phase 3. Turn rate 5° a tick in phase 1, 6° in
  phase 2, 8° in phase 3 (planted, turning is all he has). The forced enrage's speed bonus and the Repose deed's
  apply to both.
- **Hold.** His preferred spacing is MID, 6 to 9 blocks from the target. He closes only to begin a melee move he has
  picked, or when the target has stayed FAR for 60 ticks; he never chases a target that is moving away at a sprint.
- **Settle.** After each recovery he holds 8 ticks of complete stillness, not even turning (the Repose deed removes it).
  The settle is part of his read: it is the one moment a rear player is safe to reposition.
- **The way down.** The rise leaves him on the dais (the tomb's rise point at x 31, y 4). His first 40 ticks after the
  rise he stands still there (the meter lesson), then glides down the three steps to the floor.
- **Leash.** He keeps to the boss floor and a block round it (chamber x 8 to 24, z 5 to 21) and the dais. He never
  enters the aisles behind the pillar ranks, so the aisles, where the congregation kneels, are where a player goes to
  break his line of sight or to wake a figure. The kit's leash resets the trial after 600 ticks with no challenger in
  the chamber.
- **Phase 3.** During the card he glides to the centre of the blue-ice inlay ring (x 16, z 13 in the chamber) and is
  planted there for the rest of the fight.
- **Gripped.** While two or more ally devotees grip him he cannot glide, and his turn rate halves.

## 9. Arena

**What `SaintArena` finds at the rise** (one scan of the chamber box; positions are chamber coordinates from
`tools/worldgen/mausoleum/pieces.py` and `themes/velorum.py`, the tomb at (30, 4, 13), the floor x 9 to 23, z 6 to 20):

| Set piece | Where | Count | Part in the fight |
|---|---|---|---|
| Aisle rows (`dress_chamber`) | z 4 and 22, x 10 to 22 every 2, kneeling or bowed, facing east, every third head turned toward the floor | 14 | Cover from his sight through the arcades; the nearest figures to wake or pin |
| Aisle alcoves (`chamber_sockets`, `aisle_alcove`) | z 1 and 25 at x 11, 15, 19, 23, facing into the aisle | 8 | Cover; to wake or pin |
| Dais flanks (`dais_flank`) | (28, 5, 7) and (28, 5, 19), reaching, facing west | 2 | To wake or pin; the first figures beside the tomb |
| The faithful round the tomb (`dress_chamber`) | x 30 and 32, z 8 and 18 on the apse floor, prostrate | 4 | To wake or pin |
| The first who stilled with him (`apse_niche`, middle) | (35, 4, 13), kneeling toward the tomb | 1 | Never wakes; in the ending he is the one figure that turns his head toward the Saint |
| Rank-pillar rime lanterns (`dress_chamber`, `PILLAR_LAMP`) | y 6 on z 5 and 21 at the five pillar x | up to 10 | Go dark through the Long Night and the Endless Hour |
| Other chamber rime lanterns (alcove and cell votive lights, the apse niches) | as generated | as found | Go dark only in the Endless Hour |
| The blue-ice inlay ring | radius 5 round (16, 13) | 1 | Where he is planted in phase 3 |

**The congregation's states.** Each figure the scan finds is a record in `VelorumCongregation` (new): `FROZEN` (the
block), `PINNED` (the block, with hyphae; forced fights), `AWAKE` (a `VelorumDevoteeEntity`, its block removed through
the arena), `STILLED` (a devotee entity fallen still where it stood, kneeling under rime), `HOME` (the ending, restored).

**Waking (proper and NONE awakenings).** Any challenger holds Blood Projection on a frozen figure (either cell of the
two-tall block, or a stilled devotee entity): 40 fed ticks at 3 blood a tick (120 in all, paid through the block's
endpoint with a named source, kit requests K-6 and K-11), with a 4-tick grace and then a bleed-off at half speed, the
ring door's pattern. Warm blood pours into the rime from the strike; the rime cracks and sheds over the last 10 ticks;
the arena removes the block (upper cell first, without drops or neighbour updates that would break the other cell) and
spawns the devotee in the block's pose, facing as it faced, and it thaws to standing (30 ticks; 40 from prostrate). A
recalled or stilled figure costs double and has a 300-tick lockout. At most four allies are awake at once, plus one per
challenger after the first (seven at most). Because holding Projection fills the meter (+0.30, and the figure is a place
to stand), the natural time to wake one is inside an opening. Waking a figure also removes a piece of cover.

**Allies.** An awake ally walks stiffly to Velorum and fights him: a two-handed blow for 4 (the Still Guard applies),
every 30 ticks. Two or more allies within 1.5 blocks grip him (section 8). A challenger within 3 blocks of an awake ally
is sheltered (fill ×0.5). Allies take his footprints and are 30 health with 2 armour. An ally brought to 0 health, or
recalled, falls still where it stands (`STILLED`), kneeling under new rime; it is not removed and can be rewoken there.
Allies never harm challengers.

**Forced awakenings.** "Woken devotees fight against an Apotheos who forced the door" (`LORE_REFERENCE.md` §10.8). The
cold that held them broke with the door; they wake to a body the ground refuses and go for it. Two rise at the phase-2
floor and two at the phase-3 floor (the unpinned figures nearest the challengers), and Call to Prayer raises two more,
up to four awake hostiles plus one per extra challenger. A hostile devotee walks to the nearest challenger and grips
them: Slowness II and +1.5 stillness a tick while gripped, until the devotee takes any damage or 40 ticks pass, then a
blow for 4. At 0 health it falls still. Velorum's footprints pass through hostiles. **Pinning:** any challenger holds
Blood Projection on a frozen figure for 30 fed ticks at 2 blood a tick (60): for an Apotheos the projection is drawn as
fine pale hyphae (as it is at the ring door), which thread into the figure's base and pin it to the floor. A pinned
figure never rises. Pinning a figure is the Apotheos's preparation, paid for in blood and in stillness.

**Lights.** The Long Night darkens the rank-pillar rime lanterns for 180 ticks and the Endless Hour darkens every rime
lantern in the chamber for its hold: the arena records each lantern and sets it to `minecraft:packed_ice` (ice without
light), restoring each when its time ends. The vault's sea lanterns, the wall lights and the new aisle-end lanterns
(below) stay lit, so the chamber is never black.

**Restoration.** Every block the arena changed (devotee blocks removed, lanterns swapped) is restored in `endTrial`
through the ending (section 11), in `remove`, and on load of a Velorum whose trial has ended; every devotee entity it
spawned is discarded at the same moment its block comes back. A devotee entity carries its Saint's UUID and its record's
position; one that loads with no live Velorum fight discards itself (its block was already restored). A reload in the
middle of a fight keeps the arena record with the Saint's save data, so a mid-fight stop restores the chamber on the
next load.

**Chamber edit through the generator (permanent, the only one).** In `tools/worldgen/mausoleum/themes/velorum.py`,
`chamber_sockets` fills the two `aisle_end` sockets (the arched recesses closing each aisle at the dais end, unused today)
with a votive lantern stand: a rimed deepslate-brick wall post two high with a rime lantern on top and an icicle under
its cap. They keep the aisles readable when the Long Night darkens the pillars, which is where the congregation kneels.
Re-run the mausoleum generator for Velorum only, per the umbrella's rule.

## 10. Deeds

A forced awakening keeps the existing floor (+30% attack, +15% speed; Repose +20% speed, Beholding Resistance I,
Litany Regeneration I; the Downfall's health is never skipped at Velorum, who has no Downfall). On top, each skipped
Chapel lends a deed in his own language: what the player refused to see comes back as a rule.

| Skipped Chapel | Deed | What it does | Seen as |
|---|---|---|---|
| **Beholding** (the Procession, watched correctly) | *They watch you.* You would not watch them, so they watch you | Every frozen or pinned figure has a gaze: a cone 8 blocks long and 50° wide from its face, line of sight required. A challenger standing in any gaze counts as standing for the meter unless sprinting (walking no longer drains there). Turned heads look where they are turned | Two fine pale glow motes light at a figure's face while a challenger is in its gaze, as the witness devotees' eyes do; the HUD eye glyph opens |
| **Litany** (the Rule, read in order) | *The Rule kept in silence.* The words you did not read are paid in blood | Adds the Tithe of Silence (section 7) from phase 1, and Bid to Kneel also drains as the Tithe does. Openings can no longer be lengthened by Projection or Searing, and each opening ends with a toll that interrupts every challenger's held channel within 12 blocks (`DISRUPTED`, 10 ticks) | The threads of every invocation are paler and more numerous (seven, not five); the toll is a single low bell at each opening's end |
| **Repose** (stillness under threat) | *He never pauses.* "Skip the Repose and it never pauses" (`LORE_REFERENCE.md` §10) | Every recovery ×0.7 (never under 10 ticks), no settle after moves, the decision delay −8 ticks, and the Bow lasts 40 ticks instead of 70 | His cloth and chains never come to rest; the hem's frost trail is continuous |

A forced fight with all three skipped is the hardest version of the fight, and is meant to be: a fight against the sum
of what the player refused to see.

## 11. The ending

Velorum's body fails only from a hit inside an opening (below 10% nothing else hurts him). The sequence is a server
window on the Saint (kit request K-4), 200 ticks at most, saved so a reload resumes it.

| Ticks | What happens |
|---|---|
| 0 | The killing hit lands. His health is held at 1 and he cannot be hurt; the controller stops; every footprint and cue is cancelled; the bar shows empty and the frame's orb goes dark. Every challenger's fill stops. |
| 0–16 | The crooked hand lowers, for the first time in the fight (it stayed raised even in the Bow). The ice on the claw cracks and falls. |
| 16–40 | The hood bows down until the head rests on the chest; the halo's chains go slack; the staff's heel settles on the floor. |
| 40–64 | "The frost falls out of the light": the rime on his body (the spires on his shoulders, stoles and sleeves) slides down and falls as fine rime motes through the light of each of his three lanterns; the glow layer fades to 0.15; the crystals keep a faint light. |
| 40–80 | Every challenger's meter drains to 0; `OBSCURED`, roots and preservation end; the arena's lanterns come back on. |
| 64–80 | The awake devotees, ally or hostile, stop where they stand and turn toward him. In the apse niche, the first who stilled with him turns his head toward the Saint (a client-only head turn on that block's position, drawn as a brief pair of glints; no block change). Line: "They go back to their places." (`message.hemomancy.velorum.congregation_returns`) |
| 80–200 | The devotees walk back to their records' positions and kneel into their own poses (the thaw track backwards); as each arrives, the arena restores its block and discards it. Stilled devotees rise and walk back the same way. At 200, any not yet home are restored where their records are, with a small rime burst where the entity stood. |
| 200 (or as soon as every figure is home) | `endTrial(victor)`: the one loot roll (a fragment if forced), his line `returns_to_stillness`, and the sarcophagus restored dormant (the existing `returnToTomb`); the existing rest burst at the tomb as the lid closes over him. |

He sits in the tomb "as lucid and as certain as before". The faithful going back is the horror of the ending: the player
woke them, and his stillness takes them back. Nothing in the chamber is left changed.

## 12. Visual pass

**Model and generator (`tools/model_export/velorum.py`):**

- **Texel density.** `kit.SCALE = 1` (it is 2 today, so his atlas is 32 px per block at render scale 1; the
  texel-density audit lists Velorum as too fine). `layout(rig, 256)` at scale 1.
- **No silver.** The `silver` material (chest trims, staff bands and foot, halo spokes) becomes `rime_iron`: hematic iron
  (dark blue-grey patina, as `iron` today) with a one-texel pale frost edge on its upper faces. Blood-side bodies carry
  no silver, brass, bronze or copper.
- **Frozen cruor.** A new `cruor` material for the halo's hanging links: "its hanging links crusted with frozen cruor",
  dark oxblood (0x3A0E14 into 0x5A1A22) under a thin frost highlight. It is the only red on him.
- **New parts.** `breast_seam` (three thin crack cubes on the chest front, base-dark, lit in the glow atlas; the glow
  layer shows it only in the Bow); `halo_icicle_0` to `_3` as their own parts under the halo chains (today they are cubes
  on the chain parts), so the Splinter Crown can tilt and hide them; `staff_lantern_core` split from the crystal so the
  Gutter can scale the light without scaling the cage; `side_lantern` keeps its chain for the Hanging Lantern.
- **Proportions and pose.** Height stays (about 2.3 blocks to the hood, 2.6 to the halo). The rest pose is more upright
  and more rigid than today: chest pitch 1° (from 3°), the crooked hand held a little higher and nearer the face (the
  invocation is his constant), the staff vertical. He has no legs; the robe and mantle panels hang straight and their
  idle sway drops by half: dignity, not drift.
- **Hitbox** 0.9 by 2.6 (kit request K-13; today 0.7 by 2.0, well inside the model).
- **Texture palette.** Charcoal vestments (25, 28, 35) and mantle (40, 43, 51), the void face (10, 12, 23), the pale
  cord, bone hands (180, 186, 201), ice (154, 181, 218) and crystal (75, 119, 214 into 213, 242, 255) stay; frost on the
  mantle's lower edges stays; `rime_iron` and `cruor` are added. Glow atlas: crystals, the claw ice tips, the breast seam,
  a hairline of frost lines in the mantle folds.

**Layers and shaders.** No new shader. `VelorumFrostGlowLayer` keeps the eyes render type on the glow atlas; its alpha
reads three things: the lantern state (Gutter: crystals at a quarter; Endless Hour: core only), the Bow (breast seam
lit), and the ending fade. The preserved shell, the threads, the folds and every telegraph are world geometry in the
`RIME` (`manipulation_rime` shader) and `UMBRA` materials.

**World geometry per move and per impact** (all in `client/render/world/VelorumRimeGeometry.java`, which keeps its
`handles(Form)` and `collect` hook under `ManipulationVisualRenderer`; the kit's `SaintTelegraphRenderer` draws the
footprints themselves in the `RIME` family). Base tint white; the two accents are a pale core 0xE8F4FF and a frost-dark
0x0E1626, the Riposte rule of exactly two. Every line is hairline (0.004 to 0.012 wide) and low alpha (0.2 to 0.45);
there are never more than seven threads in one effect. Overlays stay subtle and partly clear.

| Move or beat | Geometry | Toolkit pieces |
|---|---|---|
| Presence | Hairline seam disc, rim motes (section 5) | `ground` + `RIPPLE` in `RIME`; `HemoParticleData.glow` |
| Staff Toll | Wind-up: seams creeping across the cone. Impact: four short leaning spires at the heel and a radial crack fan, fraying out over 10 ticks | `ribbonPath` hairlines in `RIME`; `spire` |
| Lantern Sweep (focal, to the Riposte bar) | Shell: a broad `UMBRA` ribbon at 0.25 alpha along the lantern's arc, `WISP` cards trailing against the motion. Sinew: three thin `RIME` ribbons twisting round the arc's spine with a slow cold shimmer (his blood barely moves, so no heartbeat). Knots: `FOCUS` cards at the lantern cage, brightest at mid-swing. Business end: the lantern head, a camera card glow with four short spires trailing. Motion: a trail ribbon sampled from the last five lantern positions. Impact on a player: a three-stroke frost slash and a pale flash | `ribbonPath`, `card`, `WISP`, `FOCUS`, `slashOffsets`; HutosLib claw slash via `PacketHandler.sendClawSlash` in ice colours as the accent |
| Hanging Lantern | The side lantern's short arc behind him: one thin `RIME` ribbon and a small trailing glow | `ribbonPath`, `card` |
| Black-Ice Splinters | Wind-up: a knot of dark seams at his hem and three seam lanes racing out (hairline `UMBRA` with `RIME` glints). Active: leaning black-ice splinters erupting front to back, each a `spire` in `UMBRA` with a thin `RIME` core edge. They crumble from the tips over their last 20 ticks | `spire`, `ribbonPath` |
| The Still Floor | Radial hairline seams from the staff's heel out to 10, the bare eye round him; on commit a white flash along the seams (alpha 0.6 for 3 ticks), then short frost spires along the ring for the root | `ribbonPath`, `spire`, `ground` |
| Bid to Kneel / Recall / Tithe | Five (seven with the Litany deed) pale hairline threads from the target's chest to his palm, with a slight sag, drawn inward along their length (a moving brighter segment, not a wavy line). Overheld: each thread snaps back with one glint at the palm | `ribbonPath` in `RIME`, `card` glints |
| The Long Night | Eight narrow umbral folds rising round the lantern, `UMBRA` curtains 0.3 alpha, narrow at the floor, widening and fading upward; on commit they sink into the floor over 10 ticks | `ribbonPath` with `WISP` in `UMBRA` |
| The Guttering Lantern | The lantern's camera card shrinks to a point; the folds sink; a faint dark ring at his feet | `card`, `ground` |
| Set in Place | The claw's ice brightening over the figure; impact: the shell shatters into falling spire fragments | `spire`, `card` |
| Procession of Shades | Three shades drawn by `VelorumRenderer` with the devotee model in its striding pose, through a translucent entity render type tinted frost-dark at 0.35 alpha, their bare feet leaving small frost prints that fade over 30 ticks | `VelorumDevoteeModel`; `ground` prints in `RIME` |
| Splinter Crown | The halo icicle parts tilting out; landing rings as hairline circles; each icicle falling as a `spire` from 6 blocks up and standing as a stump | `ribbonPath`, `spire` |
| The Endless Hour | A ground card the size of the floor in `RIME` at 0.08 alpha; every secondary motion on his model damped to zero; fine motes in the lanterns' light frozen where they hang (stopped drifting) | `ground`, `HemoParticleData.glow` |
| The Hollow Toll | The ring: a thin `RIME` ribbon loop with a low row of spires riding its front | `ribbonPath`, `spire` |
| The Bow | The breast seam lit in the glow layer; under held Absorption, a thin stream of dark cold blood (a few `HemoParticleData.bloodCell` in frost-dark red) to the absorber's hand along the existing cell-hand stream | glow layer, `bloodCell`, the existing absorption stream |
| Preserved player | A faceted ice shell round the player's box: six to eight `surface` panels in `RIME` at 0.35 alpha, slightly tilted, so the player shows through; short spires at the feet; on release the panels crack and fall | `surface`, `spire` |
| Devotee waking (proper) | Warm blood from the projection into the figure, the existing Blood Projection pour; the rime cracking (hairline cracks over the figure) and shedding as falling fragments | the existing projection stream; `ribbonPath`, `spire` |
| Devotee pinning (forced) | Fine pale hyphae threading into the figure's base, at most five, branching once | `BossTelegraphs.tendril` in pale bone and grey, thin |
| Ending | The claw ice falling, the frost falling out of the light (falling spire fragments and motes through each lantern's glow) | `spire`, `HemoParticleData.glow` |

Every server cue goes through `ManipulationVisuals` with `Form.VELORUM_RIME` (`VelorumEffects`), never a vanilla
particle; the only particles are `HemoParticleData.glow` motes and the Bow's few `bloodCell`s as accents. `hitGlow`
is not used.

**The woken devotee (new mob, `hemomancy:velorum_devotee`).** A new generator `tools/model_export/velorum_devotee.py`
with `velorum_devotee_anim.java`, emitting `client/model/entity/boss/velorum/VelorumDevoteeModel.java`, its atlas
`textures/entity/boss/velorum/velorum_devotee.png` (+ `_glow.png`) and a Blockbench project. It builds the jointed body
plan by importing `humanoid()` and `check_rig()` from `tools/model_export/court_humanoid.py` (root, hips, waist, head and
arms; elbows 4 px below the shoulder, knees halfway; inset fillers so bent joints close), never the vanilla humanoid.
`SCALE = 1`, 16 px per block, the player's size (two blocks, like the block figure). Dress: the frozen devotee's undyed
grey-blue robe as thin panels (front and back per leg, so it walks), the hood, the pale cord, sleeves, bare pallid
frost-bitten hands and feet, rime crust thickest on the shoulders, hood and hem, faint frost lines in the folds (glow);
the palette is read from `frozen_devotee.py`'s constants so block and mob are the same people. States drawn: frozen
(rime thick, one of the five block poses exactly, so the swap from block to mob does not jump), thawing, awake ally
(the rime has fallen from the face and hands; a faint white breath mote), awake hostile (the rime stays thick; two pale
eye glints like the witness devotees'), stilled (kneeling under new rime). Renderer
`client/render/entity/boss/velorum/VelorumDevoteeRenderer.java`.

**HUD meter art:** section 4. **Bar frame (`VelorumFrame`):** `phaseOf` returns three phases at 0.60 and 0.25;
notches at 0.60, 0.25 and 0.10; phase 2 lengthens the rail's icicles by a third; phase 3 keeps today's frost closing over
the fill from both ends; below 0.10 the rime closes the rails to a hairline gap round the fill; the orb's halo goes out
at the ending.

## 13. Animation

All Java, Catmull-Rom keyframes in Annetta's style on each move's own clock, read through `BossReplayClock` from the
kit's synced move block (`VelorumAnimState` rewritten: one move clock keyed on move id and start, one opening clock, one
phase clock). Tracks live in `tools/model_export/velorum_anim.java`; poses blend move to move through a 6-tick blend
from the last pose, and string hits blend hit to hit. Beats are in ticks from the move's start.

**Velorum (`velorum_anim.java`):**

| Track | Beats |
|---|---|
| `idle` | A breath every 140 ticks, chest 0.6°; the claw's fingers flex once every 200; lantern crystals turn slowly; veil, mantle and robe sway at half today's amplitude |
| `glide` | Body leans 2° into the travel; robe panels trail 3°; no stride; the hem's frost trail on |
| `turn` | The head leads by 8 ticks, the body follows; the staff stays vertical |
| `settle` | 8 ticks of everything coming to rest: secondary sways damp to zero and resume |
| `staff_toll` | 0–16 lift (staff to the shoulder, chest back 6°); 16 commit (heel driven forward and down, chest forward 10°); 16–19 hold; 19–37 recover to rest |
| `lantern_sweep` | 0–14 draw back (staff across the body, lantern low behind the left hip, chest turned 25° right); 14–20 sweep left to right (chest turns 50°); 20–36 recover |
| `hanging_lantern` | 0–14 hip rolls 6°, the side chain gathers back; 14–18 the chain swings through the rear arc; 18–34 the chain settles |
| `black_ice` | 0–24 the claw rises to the face, fingers spread, the head tips 6° down; 24 commit (claw opens and drops forward); 24–36 claw held low; 36–58 recover |
| `still_floor` | 0–10 the staff lifts; 10–30 lowered slowly, the heel planted at 30 with a 2° shudder through the body; 30–63 held planted, the body perfectly still |
| `bid_to_kneel` | 0–28 the hood turns to the target, the claw lifts palm out; 28–58 held, fingers drawing in a little every 10 ticks; 58–74 lowered |
| `long_night` | 0–32 the staff lifted high in both hands, head back 8°; 32–42 the staff lowered and the lantern brought before the face |
| `gutter` (opening) | The staff lowered to the hip, the head leaning toward the lantern 12°, the claw still raised; a slow drift lower over the window |
| `overheld` (opening) | 0–6 the hand shakes (a 3-tick tremor, 4°), the head jerks up 10°, the hood falls back; 6–50 a stiff, wary hold |
| `bow` (opening) | 0–12 head and hood dip to the chest (hips 14°, chest 18°), the staff leans onto the shoulder, the claw stays raised; held; the last 12 ticks rise back |
| `set_in_place` | 0–18 the claw lifted over the figure; 18 it comes down; 20–44 recover |
| `recall` / `call_to_prayer` | `bid_to_kneel`'s shape aimed at a devotee; Call raises the lantern, not the claw |
| `procession` | 0–26 the hood turns along the line, the lantern raised to the side; 26–70 held; 70–84 lowered |
| `splinter_crown` | 0–26 the head tips back 5° and the halo turns 20°, the halo icicles tilting out 35° and hiding as they fall at 26; 26–50 the halo settles |
| `endless_hour` | 0–30 the claw raised high and every secondary motion damped to zero; 30–230 nothing moves at all (the one absolutely still pose in the mod); 230–260 the release or the bow |
| `hollow_toll` | 0–24 the staff lifted upright (0–20 after the phase-3 cut, as every phase-3 track scales its wind-up keys); 24 driven straight down; 24–44 held; 44–64 recover |
| `tithe` | 0–24 the claw closes slowly into a fist; 24–44 held; 44–62 opens |
| `phase_change` | The bow, then a slow rise to full height over the card's 50 ticks |
| `failing` | 0–16 the claw lowers to the side; 16–40 the hood bows down to the chest, the halo chains slack; 40–200 held |
| `rise` | The existing `SaintRise` time, unchanged |

**Devotee (`velorum_devotee_anim.java`):** `frozen_<pose>` for the five block poses (kneeling, bowed, reaching,
prostrate, striding); `thaw_<pose>` (30 ticks, 40 from prostrate: stiff, in three hitches, as joints that have not moved
for centuries); `idle_awake` (a stiff sway, the head turning slowly); `walk` (short stride, knees barely bending, arms
held close); `strike` (both arms raised, then down, 12 / 4 / 14); `grip` (arms clamped forward, the body leaning in);
`stilled` (30 ticks down into kneeling, rime climbing); `return` (walk, then `thaw_<pose>` played backwards into its own
pose); `shade` (the striding walk, used for the Procession).

## 14. Sound

No new audio files. Mod events are `SoundInit` entries; vanilla events at the pitch given. Volumes are 1.0 unless noted.

| Beat | Sound |
|---|---|
| Ambient (rare, he is silent) | `mausoleum.velorum.creak` at 0.6 volume, pitch 0.8, every 200 to 400 ticks |
| Hurt (guarded) | `GLASS_HIT` pitch 0.6, 0.6 volume |
| Hurt in an opening | `GLASS_BREAK` pitch 0.7, 0.5 volume |
| Glide | `POWDER_SNOW_STEP` pitch 0.6, 0.3 volume, every 12 ticks of travel |
| Staff Toll and Hollow Toll, commit | `rite.staff_impact` pitch 0.7, then `BELL_RESONATE` pitch 0.5 at 0.5 volume |
| Lantern Sweep | `CHAIN_STEP` pitch 0.6 at the draw; `LANTERN_HIT` pitch 0.7 at the sweep |
| Hanging Lantern | `CHAIN_PLACE` pitch 0.7 |
| Black-Ice wind-up / eruption | `AMETHYST_BLOCK_RESONATE` pitch 0.5 at the seams; `GLASS_BREAK` pitch 0.5 per lane as it erupts |
| The Still Floor | `POWDER_SNOW_BREAK` pitch 0.5 as the seams spread; `PLAYER_HURT_FREEZE` pitch 0.6 for a rooted player |
| Bid to Kneel / Recall / Tithe | `rite.offering_drawn` pitch 0.6 as the threads draw; at the end `AMETHYST_BLOCK_CHIME` pitch 0.5 |
| Overheld | `rite.cancel_draw` pitch 0.8 |
| The Long Night | `rite.ambient.drone` pitch 0.5 rising over the wind-up; `BEACON_DEACTIVATE` pitch 0.5 on commit |
| The Guttering Lantern | `LANTERN_PLACE` pitch 0.5 at 0.4 volume, once |
| The Bow | `rite.subject_thud` pitch 0.6 |
| Procession | `entity.will.materialize` pitch 0.6 as the shades appear; `POWDER_SNOW_STEP` pitch 0.8 per shade footfall at 0.3; `entity.will.dissolve` when one breaks |
| Splinter Crown | `CHAIN_BREAK` pitch 0.8 as the icicles part; `GLASS_BREAK` pitch 0.6 per landing |
| The Endless Hour | `CONDUIT_DEACTIVATE` pitch 0.5 at onset; then no sound from him; a broken Hour: `rite.collapse` pitch 0.8 |
| Preserved | `PLAYER_HURT_FREEZE` pitch 0.7, then `GLASS_PLACE` pitch 0.5; freed: `GLASS_BREAK` pitch 0.9 |
| Devotee waking | `rite.anchor_fill` pitch 0.7 through the projection; `GLASS_BREAK` pitch 0.6 as the rime sheds |
| Devotee pinned | `rite.seal` pitch 0.6 |
| Devotee stilled | `entity.will.falter` pitch 0.7 |
| Phase cards | the card's own presentation sounds; plus `BELL_RESONATE` pitch 0.4 |
| Litany deed toll | `BELL_BLOCK` pitch 0.5 |
| Ending | `rite.collapse` pitch 0.6 at the bow (16); `AMETHYST_BLOCK_CHIME` pitch 0.6 three times as the frost falls (40, 48, 56); nothing as the faithful walk home |

The sensory darkness muffles: inside his presence, footsteps already sound muffled in his halls
(`MausoleumAmbience.muffleOwnStep`); the presence asks the kit for a wider muffle on other sounds than his own (K-8).

## 15. Multiplayer and both paths

**One to four players.** Per-player meters do not scale. His maximum health is ×(1 + 0.5 per challenger after the first; resolved 2026-11-05, was 0.6),
raised (never lowered) when a new challenger enters the chamber, and every opening threshold is a fraction of it. Ally
and hostile caps rise by one per extra challenger. The Splinter Crown rings every challenger; Bid to Kneel takes two
targets in phase 3. A preserved player is freed by anyone else's two hits on the ice or 15 fed ticks of Projection, which
makes co-op the clearest answer to preservation. Hits from every challenger count toward every opening. In a mixed party
the awakening, not each player's path, decides whether the congregation helps or hunts (a proper fight's devotees help an
Apotheos in the party too; the ground still burns that Apotheos).

**A Silentium (usually a proper awakening).** Tolerated by the hallowed ground; all three Chapels done, so no deeds. The
fight gives them the congregation: wake figures during openings for shelter and a grip, keep them out of Recall's line,
and break his invocations. Their verbs: travel, held Projection into figures and into him in the Gutter, held Absorption
on the Bow's seam, school casts in openings (Flammeus breaks the Endless Hour fastest).

**An Apotheos (usually a forced awakening).** Burned by hallowed ground (4 spore load a second, then 1 `hallowed_burn` a
second) and fighting the deeds of every skipped Chapel and the existing stat floor, with the congregation rising against
them at each phase floor and to Call to Prayer. Their verbs: pin figures with hyphae before and between phases (blood
and stillness as the price), kill risen devotees fast (30 health), spend spore load for health (U), and the same
openings. Harder, not longer: the hostile figures are fragile and the floors and openings are the same.

Neither path gets a fight it cannot finish: every win condition runs through openings both paths can take; the allies
make the proper fight kinder but are never required; the forced fight's devotees can always be pinned or killed.

### Worked fight length (resolved 2026-11-05, B-6)

A prepared solo D8 player against 640 health on Normal, proper awakening, no deeds. Assume 9 effective damage a
second while free to hit (after his 14 armour), and half that uptime outside openings because the meter makes the
player keep moving.

- Outside openings: 9 x 0.5 uptime x 0.3 (the Still Guard) = about 1.35 a second.
- Openings: the Long Night is eligible every 300 ticks and its weight climbs +6 per 100 ticks without an opening, so
  a Guttering Lantern (60, 50, 40 ticks by phase) comes about every 20 seconds; Bid to Kneel and Recall offer an
  Overheld Invocation (50 ticks) about as often to a player who strikes into the hold. Call it one window of about
  2.5 seconds every 20 seconds at full uptime: about 22 damage.
- A 20-second cycle: 22 + 17.5 x 1.35 = about 46 damage, so 2.3 a second over the fight.
- The three Bows (two at the floors, one from a broken Endless Hour): 70, 70 and 100 ticks at x1.5, about 47, 47
  and 68 damage, about 160 in all, worth about 70 seconds of cycles.
- 640 - 160 = 480 at 2.3 a second is about 210 seconds, plus two 50-tick cards, the 40-tick lesson at the rise and
  the Endless Hour's 30-tick wind-up: about 4 minutes 20 seconds. A player who chains a window into a Bow (6% inside
  it) finishes nearer 3.5 minutes; one who never takes the openings meets the floors and cannot finish without them.

A forced fight is harder, not longer: the stat floor (+30% attack, +15% speed) changes how much he hurts, not how much
he takes, and each deed keeps the damage a window admits (section 10, D-4).

## 16. Tests

**JVM rules tests** (new files under `src/test/java/com/vincenthuto/hemomancy/common/entity/boss/saint/velorum/` and the
client mirrors, through the `support/` helpers):

- `VelorumStillnessRulesTest`: every fill and drain term; presence and shelter multipliers in order; the gaze rule;
  openings halting fill but not drain; the Bow's drain; thresholds 40, 70, 100; preservation's 30 ticks, the 14-damage
  break, the 95 cap for 200 ticks, the release to 50 and the 60-tick thaw; the numbers in section 4 (143, 95, 100, 67,
  182, 111 ticks).
- `VelorumGuardRulesTest`: 30% outside openings; floors at 60% and 25% held outside openings; the last 10% only inside;
  multipliers ×1.0, ×1.25 for casts in the Overheld window, ×1.5 in the Bow; Congeatio states refused.
- `VelorumOpeningRulesTest`: each opening's entry (Long Night always into the Gutter; three hits or 4% in an
  invocation's hold; the Bow's three entries), lengths per phase and per deed, the Projection extension (+1 per 4, up to
  +20), the Searing extension once, the 6% chain into the Bow, one opening at a time.
- `VelorumMovesTest`: every timeline meets the wind-up floors (melee 12, area 20, after the phase-3 cut); every area move
  shows its footprint before its commit; footprint hit tests (the cone, the sweep's angle by age, the rear cone, the three
  and five lanes and their gaps, the annulus and its ground-only band, the scattered discs, the path and shade spacing,
  the ring wave and its jump band); phase and awakening membership; damage scaling with the attack attribute.
- `VelorumSelectionRulesTest`: weights per band and sector; the four pressures; cooldowns; a simulated fight of a few
  thousand decisions per phase with a scripted player in every band and sector asserting no move takes more than a third
  of picks, every band has at least two answers per phase, and an opening comes within 600 ticks from any state; follow-up
  conditions and the two-deep cap; no move on a fixed interval (two seeds give different schedules).
- `VelorumCongregationRulesTest`: wake cost and grace (40 fed ticks, 3 a tick, 4-tick grace, half-speed bleed), double
  cost and lockout after recall, pin cost, allegiance by awakening (PROPER and NONE ally, FORCED hostile), caps by
  challenger count, the rises at each floor, the gaze cone.
- `VelorumDeedRulesTest`: each skipped Chapel's rules, alone and together, on top of the existing floor.
- `VelorumEndingRulesTest`: the ending's beats, the 200-tick cap, early finish when every figure is home.
- `VelorumModelTest` (updated) and `VelorumDevoteeModelTest` (new): 1 texture px per model unit; no `silver` material;
  the new parts resolve; the devotee rig passes `check_rig` (elbow 4 below the shoulder, knee halfway, fillers present).
- `VelorumMeterStyleTest`: the frost reach per value, the tick positions, the halted and preserved states.
- `VelorumPresentationGuardrailTest` (rewritten): no `ParticleTypes`, no `hitGlow`, no `BLINDNESS` or `CONFUSION`, and no
  "martyr" in any Velorum file; the rime geometry still hooks before the shared branch; the frame has three notches.

**GameTests** (new `src/gameTest/java/com/vincenthuto/hemomancy/gametest/VelorumFightGameTests.java`, batch
`velorum_fight_validation`, on a generated Velorum chamber or a fixture with the same set pieces; the old Velorum cases in
`SaintPowerGameTests` are removed):

1. `stillnessFillsStandingAndDrainsWalking` (a real `ServerPlayer`).
2. `preservationFreezesHurtsAndThawsWithItsCap`.
3. `anOpeningHaltsEveryChallengersFill`.
4. `longNightDarkensPillarLanternsAndRestoresThem`.
5. `heldProjectionWakesADevoteeInAProperFight` (the block removed both cells, the ally spawned in its pose, 120 blood paid
   through the named source).
6. `allyShelterHalvesFillAndAlliesGripHim`.
7. `recallStillsAnAllyAndOverholdingSparesIt`.
8. `forcedFightRaisesHostilesAtTheFloorAndPinnedFiguresStay`.
9. `stillGuardHoldsTheFloorsOutsideOpenings`.
10. `theBowStartsAtTheSixtyPercentFloor`.
11. `endlessHourBreaksIntoTheBowOrClosesIntoPreservation`.
12. `theLastTenthOnlyFallsInsideAnOpening`.
13. `endingReturnsEveryFigureAndEndsTheTrialOnce` (one loot roll, tomb restored dormant, every devotee block and lantern
    back, no devotee entity left).
14. `removeMidFightRestoresTheChamber` and `reloadMidFightRestoresOnLoad`.
15. `eachDeedAppliesItsRule` (three cases).
16. `twoToFourChallengersScaleHealthNotMeters`.

**Review-client stops** (`VelorumFightClientReview`, run by `./gradlew.bat runBossPresentationReviewClient
-PvelorumFightReview=true`; stills land in the game dir): `idle`, `glide`, `settle`, each of the fifteen moves at its
tell, commit and impact frames, `gutter`, `overheld`, `bow` (with held Absorption), `preserved_player` and
`preserved_freed`, `meter_0`, `meter_40`, `meter_70`, `meter_100`, `meter_halted`, `meter_sheltered`, `meter_watched`,
`devotee_wake` (proper, mid-projection and thawed), `devotee_rise` (forced), `devotee_pin`, `recall`, `card_congregation`,
`card_endless_hour`, `endless_hour_broken`, `endless_hour_closed`, `long_night_from_the_aisle` (the aisle-end lanterns
still lit), `ending_frost_falls`, `ending_return`, `tomb_restored`. The scenes run with Minimal particles so the geometry
carries every effect.

## 17. Build list

In order, inside Velorum's lane (the umbrella's "Lanes and shared files"); no Gradle until the serialized stages.

1. **Rules first, pure and JVM-tested** in `common/entity/boss/saint/velorum/`: rewrite `VelorumStillnessRules.java`
   (the meter); new `VelorumGuardRules.java`, `VelorumOpeningRules.java`, `VelorumSelectionRules.java`,
   `VelorumCongregationRules.java`, `VelorumDeedRules.java`, `VelorumEndingRules.java`, `VelorumLocomotionRules.java`;
   with the JVM tests of section 16.
2. **Moves**: new `VelorumMove.java` (the enum implementing the kit's `SaintMove`, with each move's `BossMoveTimeline`,
   `BossFootprint`, aim rule, bands, weights, cooldown, phases, follow-ups, opening, `onTick` and `onHit`); new
   `VelorumMoves.java` for the hit effects and the `velorum_silence` source; damage type
   `src/main/resources/data/hemomancy/damage_type/saint_velorum.json` and its death message keys (add-only).
3. **The meter and presence**: new `VelorumStillness.java` (the per-player `SaintMeter` and the preserved hold),
   `VelorumPresence.java` (the kit's `SaintPresence` effect hook and the chill pulse), `VelorumPhases.java` (the kit's
   `SaintPhaseTable` entries), `VelorumDeeds.java` (the kit's `SaintDeeds` for Beholding, Litany, Repose).
4. **The congregation**: new `VelorumCongregation.java` (the arena records, wake, pin, recall, rises, shelter, gaze,
   lights), and `VelorumDevoteeEntity.java` with its AI and states; register `hemomancy:velorum_devotee` in `EntityInit`,
   its attributes and renderer (add-only); an empty loot table `entities/velorum_devotee` if the coverage test asks.
   The `FrozenDevoteeBlock` endpoint hook per K-11.
5. **The Saint**: rewrite `VelorumEntity.java` on the kit (attributes: maximum health 640, armour 14, knockback
   resistance 1, attack 9; the Still Guard in `hurt`; the ending window; lines under `message.hemomancy.velorum.*`);
   rewrite `VelorumEffects.java` to send `VELORUM_RIME` cues and glow motes only; delete `VelorumVisualRules.java`.
6. **Model and textures**: edit `tools/model_export/velorum.py` (scale 1, `rime_iron`, `cruor`, the new parts, the rest
   pose) and rewrite `tools/model_export/velorum_anim.java` (section 13); run it to regenerate `VelorumModel.java`, the
   atlases and the project. New `tools/model_export/velorum_devotee.py` and `velorum_devotee_anim.java`; run it.
7. **Client**: rewrite `client/model/entity/boss/velorum/VelorumAnimState.java`; new `VelorumDevoteeAnimState.java`;
   update `client/render/entity/boss/velorum/VelorumRenderer.java` (anchors, shades); new `VelorumDevoteeRenderer.java`;
   update `client/render/layer/mob/VelorumFrostGlowLayer.java`; extend `client/render/world/VelorumRimeGeometry.java`
   (section 12), with the preserved shell in a new `client/render/world/VelorumPreservedGeometry.java` if it grows past a
   screen.
8. **HUD and frame**: new `VelorumMeterStyle.java` (the kit's `SaintMeterStyle`) and `VelorumFrostPaint.java` in
   `client/screen/overlay/boss/frame/`; update `VelorumFrame.java`.
9. **Presentation**: `SCENE_VELORUM_CONGREGATION_CARD` and `SCENE_VELORUM_HOUR_CARD` in `BossPresentationIds`, their
   `PhaseCardScene`s in `BossSceneCatalog` (add-only).
10. **Chamber**: `tools/worldgen/mausoleum/themes/velorum.py` (`aisle_end` lantern stands); re-run the generator for
    Velorum only.
11. **Lang** (add-only under the Velorum prefixes): phase names, card titles, hints, lines, death messages, the devotee's
    name; remove the dead keys of section 2.
12. **Tests**: the GameTests of section 16; rewrite `VelorumFightClientReview.java`; remove the old Velorum tests listed
    in section 2; record any kit follow-ups in this spec's "Kit follow-ups" (below) for the compile gate.

### Kit follow-ups

Written by the build on 2026-11-05; each is worked round inside Velorum's package for now.

- **A pick veto and a forced pick by state.** The opening pressure's guarantee (a Long Night past 480 ticks without an
  opening) and the Endless Hour's state trigger both go through `SaintCombatController.start(move, Cut.QUEUE)` from
  `VelorumEntity.tickSaint`, which bypasses `SaintLocomotion.mayPick` and does not set the move's cooldown. A kit
  `SaintLocomotion.forcedPick(saint)` hook (or `start` setting the cooldown) would keep them inside the picker.
- **A per-hit factor for channel hits.** The Bow's held-Absorption seam deals its 1.5 a tick through `hurtByChannel`,
  so the Bow's x1.5 opening factor applies on top (2.25 a tick). Either the kit lets a channel hit skip the opening's
  factor, or the design accepts 2.25.
- **Channel ticks and invulnerability frames.** A held channel lands every tick; `VelorumEntity` clears its own
  `invulnerableTime` before each channel hit so the 10-tick vanilla grace does not swallow nine of ten. `hurtByChannel`
  could do it for every Saint.
- **A Saint-side read of who struck a challenger.** The "two blows from another challenger break the ice" rule is a
  Velorum `AttackEntityEvent` subscriber (`VelorumIceEvents`), which finds the live Velorum by a box search. A kit
  hook for blows between challengers would serve Seraphae's growth too.
- **Synced congregation state for the client.** The Beholding deed's watching glints and the pinned figures' hyphae
  need each figure's state on the client; the kit's synced block has no room for it. Velorum draws the pinning only as
  the `PINNING` cue while it is fed; a small per-Saint compound field (as Putriciel asked) would carry the rest.

## 18. Kit requests

| Id | Request | Why | Shape |
|---|---|---|---|
| K-1 | Per-player `SaintMeter` values are floats with a synced `halted` flag, a `preservedTicks` count and a small bitset (sheltered, watched) in `SaintMeterPacket` | The HUD shows the halt, the preserved thaw and the two glyphs; integer meters cannot carry 0.55-a-tick drains | `SaintMeter.PlayerState(float value, boolean halted, int preservedTicks, int flags)` |
| K-2 | `SaintOpening` can chain into a named next opening on a condition, and can be extended by a feed while open | The Gutter and Overheld run into the Bow on 6%; Projection lengthens the Gutter | `SaintOpening.next(condition, opening)`, `extend(int ticks, int cap)` |
| K-3 | `SaintPhaseTable` phase floors that hold outside openings, a damage-taken multiplier outside openings, and a final fraction below which only openings hurt | The Still Guard and the floors make openings the win condition | Per-phase `floor`, Saint-wide `guard` and `openingsOnlyBelow` |
| K-4 | An ending window on `SaintTrialEntity` (a saved `BossIntroWindow`-like run before `endTrial`, invulnerable, controller stopped, ending early on a condition) | "The hood bows and the frost falls out of the light" and the faithful going home need up to 200 ticks before the tomb closes | `beginEnding(int maxTicks, BooleanSupplier done)`; `tickEnding()` in the shared tick |
| K-5 | `SaintArena` records 2-tall set-piece removals and transient block swaps with a timer, owns companion entities (discarded as their block returns), and saves its records with the Saint | Devotees and lantern dimming; a mid-fight stop must restore the chamber | `arena.remove(pos, storeys)`, `arena.swapFor(pos, state, ticks)`, `arena.own(entity, pos)` |
| K-6 | A living-entity blood-contact hook for held Blood Projection and Absorption on the Saint and its companions, routed to the Saint's opening and paying through a named `BloodAccounting.Source` | Projection into him in the Gutter, Absorption on the Bow's seam, Projection on a stilled devotee; Vesper has a private version (`VesperBloodAbsorptionInteractions`) | `SaintBloodContact { double project(Player, double rate); double absorb(Player, double rate); }` |
| K-7 | `BossFootprint`: an annulus with a ground-only height band; lanes fanned by angle; a path whose hit segments move (shades); a held line tested by line of sight | The Still Floor's jump answer, phase-3 Black-Ice, the Procession, Bid to Kneel and Recall | `Annulus(inner, outer, groundOnly)`, `Lanes(count, spacing or fan)`, `MovingPath(segments, speed, spacing)`, `SightLine(range)` |
| K-8 | `SaintPresence` client hooks: a sound-muffle factor for sounds other than the Saint's own, and a `SaintMeterStyle` full-screen paint pass for the frost edge | "Darkness is sensory: narrowed sight, muffled sound"; the frost edge at Cold | `presence.muffle(float)`, `SaintMeterStyle.paintScreen(...)` |
| K-9 | The picker accepts fight-state weight bonuses and gates (opening pressure, the target's meter, allies present, awakening mode, deed) | The Long Night's opening pressure, the Still Floor's stillness pressure, Recall and Call by awakening, the Tithe by deed | `SaintMove.weight(context)` with `context.openingAge()`, `targetMeter()`, `awakening()`, `deeds()` |
| K-10 | Two `HealModifier` entries, Cold (×0.5) and Stilling (×0), reading the Velorum meter (shared enum, add-only) | "Stops players resting" | `VELORUM_COLD(0.5F, VelorumStillness::isCold)`, `VELORUM_STILLING(0F, VelorumStillness::isStilling)` |
| K-11 | Lane extension: `FrozenDevoteeBlock` implements `BlockBloodEndpoint`, delegating both calls to `VelorumCongregation` (and returning 0 outside a fight). The block is Velorum's set piece; the change is add-only | Waking and pinning by held Projection | `projectBloodIntoBlock(...) -> VelorumCongregation.project(...)`; absorb returns 0 |
| K-12 | `ManipulationCombatHelper.canHarmFor` learns companions: a Saint's allies of the challengers are harmed by the Saint and harm only the Saint; its hostile companions are never harmed by the Saint | Ally and hostile devotees | A `SaintCompanion` interface with `allegiance()` that `canHarmFor` reads |
| K-13 | Velorum's hitbox in `EntityInit` to 0.9 by 2.6 (an edit to an existing line, not add-only) | Today's 0.7 by 2.0 sits well inside a 2.3-block model | One-line change at the integration stage |
| K-14 | Optional: a "preserved" third-person pose claim in `PlayerAnimationRegistry.THIRD_PERSON` (arms drawn in, head bowed) | A preserved player reading as one of his figures | A registry entry after the warp chair; the fight works without it |

## 19. Questions for the user

| Question | Default I would take |
|---|---|
| At the end, should the woken devotees walk back to their places and be preserved again (the arena must restore the chamber either way), or crumble to rime while the figures come back unseen? | They walk back and kneel: his stillness takes them back. It is the horror of "Velorum preserved", and the player sees it |
| Should Velorum speak? Four short lines (the rise: "Be still."; the first preservation: "There. Now you understand."; the proper phase 2: "You would wake them? They were finished."; the Endless Hour: "Stay.") | Yes, four lines; a Velorum voice sheet goes into `docs/NPC_VOICES.md` at the final review stage |
| Should damage outside openings be cut to 30% with floors at 60% and 25%, so the openings are the only real way to win? | Yes. Without it the fight is a damage race with stillness on top |
| For a forced fight, should an Apotheos pin figures with hyphae (they stay whole), or shatter them (the arena restores them after)? | Pin with hyphae: it says the same thing without the player smashing preserved, conscious people |
| Should he glide without stepping (no legs, as the model has today), or walk? | Glide. He has no stride; it reads as rigid and certain |

## 20. Calls made

Decisions no ruling covers, all for review:

- Health stays his bar ("his body fails"); 640 health (from 450) for a four-to-six-minute proper solo fight with the Still
  Guard at 30%; floors at 60% and 25%; only openings hurt him below 10%.
- The meter's numbers: fill 0.70 standing or sneaking, +0.30 using an item, +0.10 per Slowness level and +0.05 per Rime
  level; drain 0.55 walking and 0.90 sprinting; presence ×1.5 fill and ×0.8 drain; shelter ×0.5; thresholds 40, 70, 100;
  travel threshold 0.08 blocks a tick; jumping in place counts as standing.
- Cold halves healing and Stilling stops it (two `HealModifier` entries); Stilling adds Slowness I.
- Preservation: 30 ticks, 6 damage, ×1.5 from his moves while held, broken by 14 damage, two hits from another or 15
  fed ticks of another's Projection; release to 50, a 60-tick thaw, a 95 cap for 200 ticks.
- Presence radius 10 with line of sight; the chill pulse refreshes the `RIME` school state, so Searing clears it.
- The Long Night uses the existing `OBSCURED` fog rather than Blindness and drops Nausea; narrowed sight, never black.
- Three openings with their tells, lengths (60/50/40, 50, 70), entries (three hits or 4% in a hold; 6% chains into the
  Bow) and multipliers (×1.0, ×1.25 casts, ×1.5); Projection into him in the Gutter at 4 blood a tick; Absorption on the
  Bow's seam gives the absorber 2 blood a tick.
- Fifteen moves, their bands, weights, ticks, damage, cooldowns, decision delays and link gaps; four strings; Frozen
  Advance kept as Black-Ice Splinters and Shadow Cross retired; the Procession's shades drawn with the devotee model
  as a Tenebris memory of the Beholding's Procession, not as time.
- Velorum's bands: HUG 2.5, NEAR 5.5, MID 11.
- Locomotion: a glide at 0.10/0.12/0 with turn rates 5°/6°/8°, an 8-tick settle, the MID hold, never into the aisles,
  planted on the blue-ice ring in phase 3.
- The congregation: waking by 40 fed ticks of Projection at 3 a tick; pinning by 30 at 2; caps of four plus one per extra
  challenger; allies with 30 health and 4 damage that shelter, grip and fall still rather than vanish; NONE awakenings
  count as proper for allegiance; two hostiles rise at each floor in forced fights; the apse niche's figure never wakes.
- Lights dimmed by swapping rime lanterns for packed ice through the arena; the aisle-end lantern stands as the one
  permanent chamber edit.
- The deeds: Beholding "They watch you" (gaze cones), Litany "The Rule kept in silence" (the Tithe, no extensions, the
  closing toll), Repose "He never pauses" (recoveries ×0.7, no settle, a 40-tick Bow).
- The ending: 200 ticks at most, the hand lowering first, the faithful walking home before `endTrial`, the apse figure's
  head turn as client-only glints.
- Model: scale 1 to meet 16 px per block, `silver` replaced by `rime_iron`, frozen `cruor` on the halo links as his only
  red, the new parts, a more upright rest pose, half the idle sway, hitbox 0.9 by 2.6.
- The devotee mob built on `court_humanoid.humanoid()` and coloured from `frozen_devotee.py`'s palette.
- Health scaling ×(1 + 0.6 per extra challenger), raised and never lowered.
- Bar colours blue, white and purple and the phase names; the two card names and colours.
- Every sound pick in section 14.
- Meters are fight state on the entity (no player data); a reload restarts them at 0.

## Build notes 2026-11-05

Written by the Velorum builder (run 2, stage 2). No Gradle was run in this stage: nothing here has been compiled or
tested yet. The compile gate compiles it; the verify stage runs `velorum_fight_validation` and the review client.

### What is built

**Server, `common/entity/boss/saint/velorum/`:**

- `VelorumEntity` rewritten on the kit: `createKit()` returns his kit (fifteen moves, `VelorumLocomotion`, a
  per-player `SaintMeter` over `VelorumStillness`, `VelorumPresence`, `VelorumPhases.table()`, `saint_velorum`, the 0.5
  scaling, `VelorumEndingRules.SPEC`). No goals, no `tickRise()`, no `SaintSecondaryAttacks`, `SaintAttackPattern`,
  `setVisualState` or `sendFloorRing`; no `actuallyHurt` or `die` override. 640 health, hitbox 0.9 by 2.6 by a
  `getDefaultDimensions` override. The Still Guard and floors are `damageTakenFactor`/`healthFloor`; the Bow at each
  phase floor and the ending come from `onFloorReached`; the Overheld Invocation and the 6% chain from `onHitTaken`;
  the Endless Hour's state trigger and the opening pressure's guarantee from `tickSaint`; the Litany's closing toll
  from `onOpeningChanged`; the channel targets from `onChannel` (Projection into him in the Gutter, Absorption on the
  Bow's seam, Projection warming a preserved challenger, Projection on a figure through the congregation); his four
  lines through `SaintLines.voice`; the black ice is an owned `BossGroundHazardEntity` with `hazardTick`.
- Pure, JVM-tested rules: `VelorumStillnessRules`, `VelorumGuardRules`, `VelorumOpeningRules`, `VelorumSelectionRules`,
  `VelorumCongregationRules`, `VelorumDeedRules`, `VelorumEndingRules`, `VelorumLocomotionRules`.
- `VelorumMove` (the fifteen moves on the kit's `SaintMove`, every resolution applied), `VelorumMoves` (the damage rule,
  stillness, slows, sounds and the five named blood sources `velorum_warming`, `velorum_waking`, `velorum_pinning`,
  `velorum_thawing`, `velorum_silence`), `VelorumStillness`, `VelorumPresence`, `VelorumPhases`, `VelorumLocomotion`,
  `VelorumCongregation` (figures by one arena scan, waking, pinning, rises, Recall, shelter, grip, the Beholding gaze,
  the lanterns through `swapFor`, the walk home), `VelorumDevoteeEntity` (no goals: its own tick; it falls still through
  a `setHealth` floor rather than a `die` override, which guardrail rule 2 forbids), `VelorumIceEvents` and
  `VelorumEffects` (every cue a `VELORUM_RIME` world-geometry beat; only fine glow motes as particles).
  `VelorumVisualRules` is deleted (copied to `removed_assets/` first, with the old `VelorumEntity`, `VelorumEffects`,
  `VelorumStillnessRules` and `VelorumVisualRulesTest`).
- Opening pressure: past 480 ticks without an opening (384 under the Litany) the Long Night is called at the next
  boundary, so an opening always comes within 600 ticks; the simulated picker shows the Long Night otherwise lands at
  about 15% of picks with gaps up to about 1,000 ticks, which the design did not allow.

**Shared, add-only:** `EntityInit.velorum_devotee` and its attributes, its renderer in `EntityRendererRegistrar` and
its layer in `LayerEvents`; `BossPresentationIds.SCENE_VELORUM_CONGREGATION_CARD` and `SCENE_VELORUM_HOUR_CARD` with
their `PhaseCardScene`s in `BossSceneCatalog`; about 45 lang keys under `blood_source.hemomancy.velorum_*`,
`boss.hemomancy.velorum*`, `entity.hemomancy.velorum_devotee` and `message.hemomancy.velorum.*`; an empty loot table
`entities/velorum_devotee`.

**Model and art:** `tools/model_export/velorum.py` at `kit.SCALE = 1` (16 px per block), `silver` replaced by
`rime_iron`, frozen `cruor` (dark, desaturated) on the halo's hanging links, the new parts `breast_seam`,
`halo_icicle_0..3` and the lantern `_core`s, chest pitch 1 degree; `velorum_anim.java` rewritten as Java Catmull-Rom
tracks for all fifteen moves, the three openings and the failing, with a 5-tick blend through `VelorumAnimState`.
Regenerated `VelorumModel.java`, the atlases and the Blockbench project. New `tools/model_export/velorum_devotee.py` and
`velorum_devotee_anim.java` on `court_humanoid.humanoid()` with the frozen devotee's palette; generated
`VelorumDevoteeModel.java`, `velorum_devotee.png` and `_glow.png`, and the project.

**Client:** `VelorumAnimState` (move, opening and ending clocks from `SaintMoveView`, phase-3 wind-up key scaling),
`VelorumRenderer` (adds the seam anchor), `VelorumFrostGlowLayer` (Gutter, Hour, Bow seam, ending fade),
`VelorumRimeGeometry` (lantern light, presence seam disc, Hour floor, Gutter ring, the preserved shell on any
challenger from `DATA_CHALLENGERS`, and every cue beat), `VelorumTelegraphStyle` (RIME fills, UMBRA edges, the Still
Floor's creeping seams, the gaze's dark wash, the black-ice hazard), `VelorumMeterStyle` (the vessel, frost fronts,
ticks, halted seams, preserved crack, glyphs, the screen-edge frost and the muffle), `VelorumFrostPaint`,
`VelorumFrame` (three phases, notches at 60%, 25% and 10%), `VelorumDevoteeRenderer`.

**Chamber:** `tools/worldgen/mausoleum/themes/velorum.py` fills the two `aisle_end` sockets with votive lantern stands.

**Tests:** JVM `VelorumStillnessRulesTest`, `VelorumGuardRulesTest`, `VelorumOpeningRulesTest`, `VelorumMovesTest`,
`VelorumSelectionRulesTest` (a simulated picker: no move over a third of picks, two answers per band per phase, an
opening within 600 ticks, two seeds two schedules), `VelorumCongregationRulesTest`, `VelorumDeedRulesTest`,
`VelorumEndingRulesTest`, `VelorumLocomotionRulesTest`; client `VelorumModelTest` (rewritten), `VelorumDevoteeModelTest`,
`VelorumRimeGeometryTest` (rewritten), `VelorumMeterStyleTest`; `architecture/VelorumPresentationGuardrailTest`
(rewritten: no vanilla particles, hit glow, Blindness, Nausea or "martyr" in any Velorum file; the generators keep 16
px and no silver; the devotee is on the jointed rig). GameTests `VelorumFightGameTests` (namespace and template
`velorum_fight_validation`, one batch per case). `VelorumFightClientReview` rewritten with a stop per move, opening,
meter state, devotee wake and rise, card and the ending.

### What is not built

- The Procession's shades are the path footprint and its cues; `VelorumRenderer` does not yet draw the three shades
  with the devotee model (section 12).
- The Beholding deed's watching glints and the pinned figures' standing hyphae (no client copy of the congregation;
  see the kit follow-up above). The gaze itself works on the server and lights the HUD eye glyph.
- The apse figure's head turn in the ending (client-only glints).
- Bid to Kneel's second target in phase 3; the gaze cone takes everyone in it instead.
- Allies striking the preserved ice (only challengers' blows and warming break it early).
- The devotee's `shade` track; its `return` is the walk then the kneeling pose, not the thaw played backwards.
- The aisle-end lantern stands are in the theme generator, not yet in the structure files (see the compile-gate
  follow-ups).
- Sound picks from section 14 not yet wired: the Bow's absorption stream and the shades' footfalls are silent.

### Compile-gate follow-ups

- Remove `velorum/VelorumEntity.java` from `src/test/resources/architecture/saint-kit-legacy-allow-list.txt` (it no
  longer uses a legacy piece; `SaintCombatKitGuardrailTest.saintsUseTheKitNotTheLegacyPieces` reports it stale).
- Shared tests that pin the old Velorum: delete the Velorum case of `SaintPowerRulesTest` (it calls the removed
  `VelorumStillnessRules.next/rooted`), the Velorum cases of `SaintPowerGameTests` (`tickSecondaryAttacks` on a
  Velorum) and the Velorum rows of `client/render/world/SaintPatternGeometryTest`; update
  `BossBarFramePhaseSaintTest.velorumFrostsBelowAQuarter` to the three phases (`phaseOf(0.3F) == 2`,
  `phaseOf(0.25F) == 3`); `SaintRiseTest.everySaintHoldsThroughItsRise` expects `tickRise()` in each Saint's `aiStep`,
  which a kit Saint must not call (the kit holds the rise); `SaintEncounterGameTests`' Velorum fall and reload cases
  call `boss.die(blow)`, which a kit Saint refuses, so they need the kit's ending (open an opening, strike to 1).
- Add the suite row `'velorum_fight_validation': [run: 'velorumFightGameTestServer', directory:
  'build/velorum-fight-gametest']` to `gradle/game-tests.gradle` (a shared file this builder did not edit).
- Re-run the mausoleum generator for Velorum: its CLI rebuilds all four Saints, so this stage did not run it.
  `build.write_saint('velorum')` in `tools/worldgen/mausoleum/build.py` rebuilds Velorum alone (it writes only his
  structures, pools, structure JSON and biome tag).
- Remove the dead lang keys `message.hemomancy.velorum.darkness_closes_in_silence_answers`,
  `message.hemomancy.velorum.stillness_pressure` and `message.hemomancy.saint_pattern.frozen_advance` /
  `.shadow_cross` once the legacy pieces go (lang is add-only for builders).
- Regenerate `docs/architecture/evidence/source-text-impact-map.csv`: `VelorumPresentationGuardrailTest` now reads
  every `Velorum*.java` and the two generators.
- `docs/NPC_VOICES.md` needs Velorum's voice sheet for his four lines (final review stage).
- `HudAndWorldDrawingGuardrailTest`, `AnimationTimeGuardrailTest` and `EnglishLiteralAllowListTest` were not re-read
  against the new files; check them at the gate.


### Compile gate 2026-11-06

The whole tree compiles (`compileJava compileTestJava compileGameTestJava`) and the JVM suite is green. Shared edits
made at the gate: the legacy Saint pieces are deleted (`SaintSecondaryAttacks`, `SaintAttackPattern`,
`SaintPatternGeometry` and their two tests, `registerTrialGoals`, `sendFloorRing`, the visual-state and secondary synced
fields, `reapplyEnrageEffects`), `createKit()` is abstract, the legacy allow-list is gone, `SaintPowerRulesTest` and
`SaintPowerGameTests` (every case drove a legacy piece; the `saint_power_validation` suite row and its template went
with them) are deleted, and the four fight suites have rows in `gradle/game-tests.gradle` (alpha). All deleted files
are under `removed_assets/` with MANIFEST lines. In this Saint:
- `SaintEncounterGameTests.velorumFallClosesTheTombOnceAndRewardsOnce` is now
  `velorumEndingClosesTheTombOnceAndRewardsOnce`: after the rise, health at 8%, the Gutter opening, one blow, then
  the ending runs `endTrial` once; a reloaded copy remembers the ended trial and a later blow rewards nothing.
- `BossBarFramePhaseSaintTest` reads three phases (0.7 is 1, 0.6 and 0.3 are 2, 0.25 is 3); `VelorumFrame.notches()`
  lists 0.1, 0.25, 0.6 (ascending).
- The mausoleum generator was re-run for Velorum alone (`build.write_saint('velorum')`, then `build.check`). The votive
  stand's two rimed wall posts were placed without wall properties, which the layout check reported; they now use
  `kit.post()`. Only `structure/mausoleum/velorum/chamber.nbt` changed in content; byte-only gzip rewrites of the other
  rooms were put back.
- `SaintKitGameTests.aSaintsBodyRefusesTheOrdinaryDrain` uses Velorum as its real Saint.
- Lang keys removed: `darkness_closes_in_silence_answers`, `stillness_pressure` and the eight
  `message.hemomancy.saint_pattern.*` lines.
- Not done: the voice sheet in `docs/NPC_VOICES.md` (final review stage).

## Verification 2026-11-05

Run on 2026-11-06 by the Velorum verify stage, the only Gradle user at the time. Nothing is committed.

### What ran

- **JVM.** The Velorum, Saint, BossBar and BossFramework selections ran first. After the fixes, the full
  `./gradlew.bat test` passed 5,308 tests with 0 failures.
- **GameTests.**
  - `velorum_fight_validation`: 9 of 9.
  - `saint_kit_validation`: 8 of 8.
  - The combined gate (`runGameTestServer`, 1,424 tests) went from 4 failures to 1. The one left is
    `everyHemomancyRegistryIdMatchesTheFrozenSnapshot`: `registry-ids.txt` lacks the kit's `saint_*` damage types, and
    the file says only an approved migration may replace it. That is the user's decision.
- **Review client.** `runBossPresentationReviewClient -PvelorumFightReview=true` ran five rounds. Every shader compiled
  on this GPU. `logs/latest.log` has no shader or render errors, only the old sampler and uniform warnings for
  `rendertype_entity_translucent_emissive` and `vesper_wound_draw`. The last round's report
  (`build/boss-presentation-review/operator/velorum-fight-review.json`) passed:
  - all 15 moves reached their active span (the Endless Hour through its own state trigger);
  - all three openings opened;
  - the quarry was preserved;
  - the Long Night darkened the 10 pillar lanterns and the Hour darkened 26 lanterns;
  - a devotee woke under held Projection;
  - Call to Prayer raised 2 hostile devotees;
  - both floors bowed into phases 2 and 3;
  - the ending rewarded and ended the trial once;
  - the chamber came back block for block: 0 cells changed, the tomb closed, no devotee left.

### The review harness (`VelorumFightClientReview`, rewritten)

The harness now follows the Hemorath and Putriciel harnesses:

- It places the real `mausoleum/velorum/chamber` template and runs the encounter's own rise from the sarcophagus.
- A survival quarry with a player-info entry stands in front of him.
- The review player is a Creative camera. Its own stillness is held at 0, except at the HUD stops. Its Long Night fog is
  kept only around the commit, so the other stills stay legible.
- Each scene has its own camera, and stills are taken at chosen ticks.
- Gamma is bright, the hand is hidden and the onboarding screen is dismissed.
- The run ends with a block-for-block restore check of the whole chamber box.

The 38 scenes run in fight order:

- The chamber at rest, the rise, idle and a portrait.
- Every phase-1 move.
- The Long Night into the Gutter, fed by held Projection.
- The Overheld Invocation, struck out of Bid to Kneel.
- The Bow under held Absorption.
- A preserved player, and the meter at 0, 40, 70, 95 and halted.
- A devotee woken in the north aisle.
- The 60% floor with its Bow and card.
- Phase 2: Lantern Sweep, Recall, Procession, Splinter Crown.
- The 25% floor with its card.
- The Endless Hour held for its full 200 ticks while the quarry circles.
- Phase 3: Hollow Toll, the long Black Ice, Staff Toll.
- A forced Call to Prayer, the ending and the walk home, and the restore check.

Stills: `build/boss-presentation-review/screenshots/velorum-fight-NN-<scene>-TTT.png`, 146 from the last round. The
earlier rounds are kept beside them in `velorum-round1` to `velorum-round4`.

### What I fixed

- **GameTests.**
  - The mock players kept their 60-tick spawn invulnerability, so preservation never hurt. The fixture now clears it, as
    Hemorath's fixture does.
  - `velorumEndingClosesTheTombOnceAndRewardsOnce` never added its player to the level. The victor lookup found nobody,
    so the loot table's player-only quintessence never rolled. The player is now added.
- **Kit: two fixes in `SaintTrialEntity.actuallyHurt`, rerun green and noted in the umbrella's Kit API section.**
  - The health floor was read after the blow landed, so Velorum's 60% and 25% floors never held. It is now read before
    the blow.
  - A `/kill` was floored like any other blow. It now skips the factor and the floor, so `die()` sees it and the Saint
    goes back to its tomb.
- **Blood profile.** `velorum_devotee` (30 health) gets a Living Syringe restriction
  (`blood_profiles/velorum_devotee.json` and `restricted_entities.json`). The
  `highHealthRestrictionsRespectApprovedSamplingExceptions` test requires it.
- **No red on him.** The vanilla hurt tint turned Velorum pink-red through every held channel (the fed Gutter, the
  absorbed Bow) and after every blow. `VelorumRenderer` now renders with `hurtTime` held at 0, and a blow reads as frost
  (`VelorumEffects.guarded`).
- **Glow layers.** `RenderType.eyes` is additive and ignores alpha. `VelorumFrostGlowLayer` therefore always drew at full
  strength, so the Gutter, the Hour and the ending never dimmed it, and the devotee's frost specks glared. Both layers
  now scale RGB instead, the pattern Putriciel uses.
- **The Endless Hour was a blackout.** The chamber has no sea lanterns: every light is a rime lantern, and the Hour
  darkened all 37. `VelorumCongregation` now spares the aisle-end votive stands (a lantern on a wall post) and the vault's
  crown lamps (local y 12 and up). The crown lamps stand in for the sea lanterns the design names. The Hour is now dim,
  not black, and 26 lanterns go dark.
- **Readability.**
  - Bid to Kneel's gaze cone was a dark wash on a dark floor, so it could not be seen. It is now a pale RIME wash.
  - The Still Floor seams were pale on a pale wash. They are now UMBRA cracks at the canvas's widest edge, and they
    flash RIME at the commit.
  - The standing black ice is now visible. Its hazard edges are stronger (0.7). The splinters are taller and wider, and
    they are drawn as solid frost (shape EYE, the rime shader's lit ice) instead of the stippled ribbon grain, which had
    thinned them to dashes.
  - The Staff Toll and Still Floor spires use the same solid frost.
  - The Long Night's folds get a solid cold hairline, and their alpha goes up to 0.42.
- **Lantern Sweep.**
  - The track barely moved the staff. It now draws the lantern back past his hip and sweeps it out, with a deeper lift,
    a wider yaw and a staff tilt. The change is in both `tools/model_export/velorum_anim.java` and `VelorumModel.java`.
  - The focal arc from section 12 had never been built. `VelorumRimeGeometry.sweep` now draws a broad UMBRA shell along
    the lantern's last nine positions, three twisting solid-frost sinews and a FOCUS knot at the cage.
- **Woken devotee.** The swap from the rimed block figure to the navy mob jumped. `tools/model_export/velorum_devotee.py`
  now lays rime crust in two-texel clumps, thicker lower down the robe, with the hem and the up faces rimed, and the
  frost-line glow is rarer. The texture is regenerated. `VelorumDevoteeModel.java` came out byte-identical.

### Still open

- **The Lantern Sweep arc does not read in the stills.** A diagnostic round logged that the trail is collected and
  drawn, but at fight distance the shell and the sinews are lost. It needs an in-game look, or a longer trail and a
  brighter knot.
- **The Still Floor's dark seam cracks still do not read** from the MID camera; the wash and the bare eye carry the
  telegraph. Wider seams would have to be drawn in the world pass, because the telegraph canvas caps edges at 0.012.
- **Not built, or not visible in these stills:**
  - the procession shades (the path moves, but no shade is drawn);
  - the Bow's absorption stream;
  - the breast seam's glow, hidden under the bowed hood;
  - the woken figure's crack-and-shed;
  - the Projection pour (the router drives the channel without the item's beam);
  - glints on the Beholding and pinned figures;
  - the head-turn glint in the ending.
- **Red at the challengers' feet.** The presence puts Congeatio's RIME school state on challengers. Its shared visual is
  dark oxblood frozen-blood crystals at the feet, so there is red on his challengers, though not on him. It is shared
  school-state art, so I left it.
- **The Long Night's `OBSCURED` fog** (near 4, far 12) turns everything past about 10 blocks nearly black. This is the
  existing school state, but the design asks for "narrowed sight, never black".
- **The forced scene.** The health reads 100% after `setAwakening("FORCED")` in the review, because it rescales his
  health; that comes from the review's order, not from the fight. Two hostiles rose, and the ally from the proper wake
  stayed an ally.
- **Set in Place** does not glide him to the figure; he strikes from where he stands, and the disc is faint.
- **The registry snapshot failure** (above) waits for the user.
- **Not covered by GameTests:** the Endless Hour breaking into the long Bow at 8%, Recall's lockout, the Litany toll,
  and four challengers.

### What the user should look at

1. `velorum-fight-05-lantern_sweep-*` and `velorum-fight-25-p2_lantern_sweep-*`: the new sweep pose, and whether the arc
   needs to be stronger in game.
2. `velorum-fight-30-endless_hour-*`: the Hour is now dim, with the crown lamps and the aisle-end stands lit. Is that the
   darkness you want?
3. `velorum-fight-07-black_ice-049` and `velorum-fight-32-p3_black_ice-*`: the standing black ice as solid dark shards.
4. `velorum-fight-23-floor_sixty-*` and `velorum-fight-35-ending-*`: the woken devotee's new rime crust next to the
   block figures.
5. The two kit fixes (the floor read before the blow, and `/kill` with no floor). They change behaviour for any Saint
   whose floor depends on its health.
6. Whether the red frozen-blood crystals of the shared RIME school state belong on Velorum's challengers.

## Visual pass 2 (2026-11-06), animation

Carried over from Hemorath's pass 2 (his spec, "Visual pass 2 (2026-11-06), animation"), in Velorum's own character:
he barely moves, so every track is restraint. Long still holds, then one decisive motion with the whole body behind it
(a coil, a fast arc through the commit, follow-through past the line), then a slow weighted settle. Nothing here has
been compiled or launched; the poses were checked in an offline Python mirror of the pose code (key-frame renders of
every move, a heel-contact and skating sweep at quarter ticks, the claw height, the cloth tips), not in the game.

### What changed in the machinery

- **Layers, not a crossfade.** A frame is the stance, then the glide and the turn, then the playing track as deltas
  over both, then the blend, then the springs, then `pose()`. Every move's keys start and end at zero, so it rises
  out of the stance and settles back into it. The one exception is the Long Night, whose last pose is the Guttering
  Lantern's first.
- **The stance blends across the phases.** The Hour, the Congregation and the Endless Hour are smooth weights
  (`VelorumAnimState.congregation()` and `hour()`, springs over the synced phase), so a phase change never steps. The
  breath (every 140 ticks, every 200 in the Endless Hour) and the crystals' turn are phases carried in the state, not
  recomputed from the age.
- **Inertial blend, shared with Hemorath.** `VelorumAnimState` calls `HemorathPoseBlend` (moved nowhere). A changed
  track (a new move, the same move again by its synced serial, an opening, the ending) keeps the gap from the last
  shown pose and the body's velocity, and the gap dies away over 5 ticks (resolution B-8); the ending's blend is 3.
  A phase change starts a 50-tick blend, so he rises slowly out of the Bow that took him there while the card plays.
  `Frame` keeps its crossfade weights only for the glow layer and the world geometry.
- **Springs, shared with Hemorath.** `HemorathSpring`, 40 slots in `VelorumAnimState`, stepped once a frame. Each spring
  writes how far a part trails what carries it:
  - the head held a beat behind the shoulders;
  - the claw's forearm and fingers trailing the arm;
  - the side lantern as a pendulum following the track's own swing, kicked by the chest and dragged by the glide;
  - the pendant lantern swinging off the staff's tilt and turn;
  - the waist chain, and the halo's four chains, ringing when the icicles snap free;
  - the mantle and the robe trailing the glide, settling forward when he stops and dragging round after a turn;
  - the stoles, both sleeves, the back veil and the knotted cord's tails.

  He is the still thing, so the springs are tight and well damped, and every trail is scaled by his calm. The Endless
  Hour, the Still Floor's held recovery and the ending take the calm to nothing. Under the Repose deed the calm never
  falls under 0.45 and a slow drift keeps the cloth and chains moving.
- **The staff is one rigid pole.** Planted, its heel stays where the stance or the move puts it, and the grip slides up
  to 8 units along the shaft, so the staff tilts as he leans and as his hand moves. Lifted, it holds the move's angle:
  a pitch, a turn about the vertical (the Lantern Sweep's arc) and a roll, and it slides through the hand by its own
  grip channel. Gliding or turning, it is carried a hair off the floor and trails a little. The pendant lantern
  always hangs plumb off it.
- **Grounded contact.** A planted heel stands on the floor and does not skate. Every move, opening and the ending were
  swept at quarter ticks in the mirror: no planted frame strays more than 0.6 units from the floor or moves more than
  0.35 units between frames. The mantle's trains and the sixteen robe panels fold outward to meet the floor instead of
  passing through it when he sinks or bows (`heap()`). The chains, the claw's ice, the sleeves and the cord's tails hang
  plumb; the mantle, robe, stoles and veil hang 75, 85, 60 and 50 percent toward how they hung at rest.
- **Limb trails for the effects.** The renderer feeds `VelorumAnimState` each frame with the staff lantern, the heel,
  the claw and the side lantern in world space (`VelorumLimbTrail`, about a second of frames). `lanternAt`, `heelAt`,
  `clawAt` and `sideLanternAt` read them back at any render age, so the sweep, the toll, the rear arc and the claw's
  reach follow the animated limb. `heel()` is the new anchor.
- **Clocks.** Every track still runs on the kit's replayed move, opening and ending clocks (`SaintMoveView`, through
  `BossReplayClock`). `trackTime` now also plays a recovery shortened by the Repose deed at the faster pace, as it
  already did for a phase-3 wind-up, so a move always settles.
- **Rig** (`tools/model_export/velorum.py`, regenerated: 498 cubes, 184 joints, atlas 256 by 256, 16 px a block). The
  knotted cord's tails are their own part (`knotted_cord_tails`), so they hang and swing.

### Track beats

Times are on the authored clock. The commit is the end of the authored wind-up.

| Track | Beats |
| --- | --- |
| Stance, the Hour | Upright and rigid, the crooked hand raised by his face, the staff planted at his side, a breath every 140 ticks. Every 200 ticks the hour is counted on the fingers: they fold one at a time, hold, and open together |
| Stance, the Congregation | The hand higher on the shaft so the lantern stands taller, the palm opening toward them, the hood turning slowly between the aisles and holding each look for 160 ticks |
| Stance, the Endless Hour | The heel set a little ahead, the claw higher, the head lowered, the chest forward 2 degrees, the cloth stiller, a breath every 200 ticks |
| Glide | A 2-degree lean into the travel, the staff lifted a hair and trailing, the claw a little back. The mantle and robe trail and settle forward when he stops |
| Turn | The hood leads, the shoulders follow, the hips lag. The trains drag round after the turn and the side lantern swings out |
| Staff Toll (37) | 0-5 the hood lowers to the target. 3-16 the staff comes off the floor, the hand rises to the shoulder and the heel swings forward and up while the lantern goes back over his shoulder; he draws up and back, the left shoulder cocked, held at 14-16. 16-17.5 the heel is driven down onto the floor a block ahead and the chest thrown after it. 17.5-23 he carries on over the planted staff, a shudder through him, the hem lifting. 25-31.5 he lifts the staff, sets it back at his side and draws himself upright |
| Lantern Sweep (36) | 0-11 the staff comes up and is drawn back across his body, the hand sliding down the shaft so the lantern swings far out and low behind his left hip, the light brightening; he coils to his left and sinks into it. 11-14 held. 14-20 one wide low sweep carries the lantern across his front from his left to his right, at chest and waist height, about two blocks out, the chest turning through the line. 20-23 on past it to his right, the hips following late. 23-31 the staff is righted and set down |
| Hanging Lantern (34) | 0-14 he does not turn: the hips twist left and roll, drawing the side chain and its brightening lantern forward along his right hip, held at 12-14. 14-16.5 the hips snap back and the chain is flung back and up through the arc behind him. 16.5-22 it swings on high behind him, then falls back and swings itself out on its spring |
| Black-Ice Splinters (58) | 0-8 the claw rises to his face and the head tips down. 8-20 the fingers fold shut one at a time on the gathered cold and the fist trembles; he draws up taller. 20-23.5 the claw rises a last hand's breadth. 23.5-25.5 it flings open and drops forward toward the floor ahead, the chest bowing after it. 25.5-36 held low and splayed over the racing seams. 36-58 it rises slowly back to his face |
| The Still Floor (63) | 0-10 the staff comes up off the floor and he draws up tall. 10-28 it is lowered with immense control. 28-30 the heel is driven home. 30 the crystals ring, a shudder runs through him, the claw flinches open and the cold lifts the mantle's hem. 33-58 he holds the planted staff perfectly still, even his cloth. 58-63 he lets it go |
| Bid to Kneel (74) | 0-6 the hood turns to the target. 2-22 the staff comes off the floor and the lantern is lifted out toward them at head height, the hand low on the shaft, while the crooked hand rises palm out. 28-29.5 the palm pushes once and the gaze locks. 30-58 he leans in, utterly still, the palm pressing down a step every ten ticks. 58-60.5 he presses it down hard: the bid to kneel. 62-74 everything lowers and the staff is set down |
| The Long Night (42) | 0-20 the staff is lifted off the floor and raised high overhead, the hand sliding down the shaft so the light stands far above him; the head goes back and the claw rises beside it, palm up. 20-30 held. 30-32 a last inch higher. 32-34 brought down before his face in two ticks, the claw cupping the light as it gutters. 34-42 the pose he hands to the Gutter |
| Set in Place (46) | 0-6 the hood looks down at the figure. 6-17 the claw rises high over it, fingers spread, as he draws up. 17-20 a hitch at the top. 20-21.5 the claw is driven down onto the figure with his weight behind it, closing into a fist. 21.5-27 it carries on low past the figure. 27-46 he rises and the hand opens and returns to his face |
| Recall to Stillness (78) | 0-18 the lantern is lowered toward the floor (the staff stays planted and tilts) and the claw rises palm out, the fingers beckoning twice. 18-20.5 they snap half shut on the threads. 20-60 the hand draws back toward his breast in four pulls, ten ticks apart, a finger folding at each. 60-78 it lets go |
| Call to Prayer (50) | 0-8 the hood turns to an aisle. 2-20 the staff comes off the floor and the lantern is raised high and out toward it, the claw lifting palm up beside it. 24-25.5 the light is lifted a hand's span higher and brightens. 30-50 it is lowered slowly and the staff set down |
| Procession of Shades (84) | 2-22 the hood turns along the line; the lantern is lifted out to his side at head height and the claw reaches out low before him, palm down, fingers trailing (never at his side). 26 the head ticks toward the line's end. 28-70 held still, watching them come. 70-84 lowered |
| Splinter Crown (50) | 0-18 the head tips back and the claw rises palm up; the halo turns slowly and its icicles lean outward, shivering in three wobbles. 25.5-26.2 they snap free and vanish (the world draws them falling). 26-27.5 the hand closes into a fist and pulls down hard, the head dropping, the halo ringing on its chains. 36-49 the icicles grow back, overshooting to 1.18 at 44 before they set |
| The Endless Hour (260) | 0-14 the claw rises high, palm out. 0-30 the breath stops and every secondary motion damps to nothing. 18-30 the head stops following anything. 26-30 a last small settling. 30-230 nothing moves at all; the keys are held flat, so not one channel changes. 230-260, if the hold runs out, the hand lowers, he breathes out once at 233 and the cloth wakes |
| The Hollow Toll (64) | 3-20 the staff is lifted straight up off the floor and he rises with it, the claw too. 20-23 a held breath at the top. 23.5-24.5 it is driven straight down and his whole body drops with it (sinking 1 unit), then rebounds at 29; a shudder; the hem lifts. 30-44 held planted and still while the ring runs. 44-64 settles |
| The Draw of Silence (62) | 0-10 the claw rises and opens wide, the fingers bent back. 10-22 it turns palm up and closes slowly, a finger every three ticks. 24-26 the fist closes hard and is drawn to his breast, the chest and head bowing round it. 26-44 held there trembling with the draw. 44-62 it opens and is let go |
| The Guttering Lantern | Picks up the Long Night's last pose: the staff lowered to his hip with the lantern held before him, the light shrunk to a quarter, the head leaning and turning toward it, the claw still raised. Over the window he drifts lower and his breath shows. In the last ten ticks he straightens |
| The Overheld Invocation | 0-1.5 a jolt: the head snaps up, the hood falls back, the raised arm jerks back and the fingers fling open. 0-8 the hand shakes, fading. Then a stiff, wary hold, the claw drawn in before his chest, breathing hard, the hood slipping forward |
| The Bow | 0-3 a hit-stop shudder. 3-13 he bows from hips, chest and hood with weight, sagging a little past at 13 and sinking 1.6 units so the robe heaps. The staff's heel stays where it stood while it leans in onto his shoulder; the crooked hand stays raised. Held, breathing heavily. In the last 14 ticks the chest rises first and the head last. The 40- and 100-tick Bows keep these beats |
| Phase change | The Bow, then a 50-tick inertial rise out of it into the new phase's stance while the card plays. In phase 3 he glides to the ring meanwhile |
| Failing (200) | 0-3 hit-stop. 3-16 the crooked hand lowers for the first time in the fight: slowly at first, then falling, unbending, bouncing once at his side at 16.5. 16-40 the hood bows to the chest and he settles 1.8 units lower, the hand sliding down the planted staff, the halo sagging and tilting on its slackening chains. 30-64 everything on him comes to rest. 64-72 a last settling breath. Then held |

**The woken devotees** (`tools/model_export/velorum_devotee_anim.java`, rig regenerated: 44 cubes, 16 joints, atlas
128 by 128). They gain their own hanging parts: the hood's back as a drape (`hood_tail`), the cord's ends
(`cord_ends`) and the sleeves' loose hems (`right_cuff`, `left_cuff`). They have their own state
(`VelorumDevoteeAnimState`), which reuses `HemorathPoseBlend` (6 ticks; 3 for a blow) and `HemorathSpring`. They walk on
`VelorumDevoteeGait`, keyed to the distance walked.

| Track | Beats |
| --- | --- |
| Frozen | One of the block's five poses exactly (kneeling, bowed, reaching, prostrate, striding), so the swap from block to mob does not jump |
| Thaw (30, prostrate 40) | Three stiff hitches at 5, 38 and 70 percent of the thaw. Each cracks free in a short ease, overshoots a little and settles back, with a shiver of falling rime. The head comes first, then the arms, then the body, and the legs last |
| Awake, ally | Upright and stiff, the arms held close and a little forward, the head turning slowly to what it watches |
| Awake, hostile | Hunched: the body forward 14 degrees, the head jutting, the arms lower and forward, the knees bent, the hips sunk so the soles stay on the floor |
| Walk | Short stiff steps, keyed to distance (a 1-block cycle, each foot off the floor for 40 percent of it). A planted foot sweeps back at exactly the body's speed. The knees bend only enough for the swinging foot to clear the floor. The hips sink so the lower planted sole stays on the floor. The arms make a small counter-swing and the body a small roll |
| Blow (30) | 0-11.5 both arms rise overhead and the body arches back, up onto its toes. 11.5-13.5 the arms come down with the whole body folding after them and the knees giving. 13.5-17 on past the line, low. 17-30 it straightens |
| Grip | The arms clamp forward and it leans in on bent, braced knees, straining (a shiver). Blended in and out |
| Stilled (30) | It slows into kneeling, quick at first and then ever slower: the legs fold first (0-18), then the body bows (4-24), the arms (6-26) and the head last (10-30), with rime shivering on it |
| Returning | Stands, turns toward him for 16 ticks (the head first, the hood following on its spring), then walks home |
| Kneeling home | The kneeling block pose. The entity is replaced by its block on arrival, so the swap is instant |
| Shade | `poseShade(stride, age)`: the striding walk, unrimed, for the Procession's shades. The renderer does not draw the shades yet |

### Tests

- `VelorumAnimationTest` (new):
  - every move rises out of the stance and settles back into it;
  - the planted heel stands on the floor and does not skate, across every move, opening and the ending at quarter ticks;
  - each strike runs at least 2.5 times faster through its commit than through its wind-up (the Toll's heel, the
    sweep's lantern, Set in Place's and Black-Ice's claw, the Hollow Toll's heel, the Hanging Lantern's side lantern);
  - the crooked hand stays above his waist in every track but the ending, and lowers in the ending;
  - the Endless Hour holds every channel still, with the calm, the breath and the look at nothing;
  - the Long Night hands its pose to the Gutter;
  - the robe and the mantle's trains rest on the floor in the Bow, the ending and the Hollow Toll's drop;
  - the Splinter Crown's icicles vanish and grow back with an overshoot.
- `VelorumAnimStateTest` (new): the track priority; a cut wind-up and a Repose-shortened recovery both keep their
  authored beats.
- `VelorumLimbTrailTest` (new): interpolation, no reading ahead, a second render skipped, a reload restarting.
- `VelorumDevoteeGaitTest` (new): no skating; the feet take turns; a planted sole on the floor and the swinging one
  clear of it through a whole cycle; the hips' sink.
- `VelorumDevoteeModelTest` (extended): a thaw starts from the block's own pose for all five poses, and the stilled end
  kneeling; the blow comes down more than three times faster than it rose; awake soles on the floor, upright and
  hunched.
- `VelorumModelTest` is unchanged and still holds: the rest pose comes back, the staff is plumb at rest, the sleeves
  hang plumb at every commit.

### For the review

- Run `runBossPresentationReviewClient -PvelorumFightReview=true` and look in motion at:
  - the Lantern Sweep and the Staff Toll's arcs and settles;
  - the Hanging Lantern's ring-out;
  - the Endless Hour's stillness;
  - the 50-tick rise out of the Bow at each phase change;
  - the ending's falling hand;
  - the devotees' thaw hitches, walk and blow.
- Tune points:
  - the spring frequencies and gains in `secondary()` (both anim files);
  - `TRAIL_LIMIT`;
  - `GRIP_SLIDE`;
  - the cloth hang shares in `hang()`;
  - `VelorumAnimState.PHASE_RISE`;
  - `VelorumDevoteeGait.CYCLE` and `KNEE_LIFT`.
- Not done:
  - the claw has no inverse kinematics, so "both hands on the staff" is not shown and the Toll lifts the staff in the
    left hand alone;
  - a blow on him causes no flinch (his guard is the point, and the renderer holds `hurtTime` at 0 while it poses him);
  - the Procession's head does not sweep along the line, because the shades' direction is not read;
  - the hostile blow out of a grip is synced at the strike, so its coil plays after the hit;
  - the devotee's kneel home is instant, because the entity becomes its block on arrival.

## Visual pass 2 (2026-11-06), effects

Hemorath's pass 2 set the bar after the user found the first Saint effects plain ("debug lines", "SUPER basic" ground
telegraphs). Velorum's pass-1 effects had the same faults: flat RIME and UMBRA tints under the kit's subtlety budget,
hairline seams that vanished on his dark floor, and black ice drawn as pale stripes (see `velorum-fight-07-black_ice-032`).
This pass carries Hemorath's method over in Velorum's own language: the cold arresting the floor (seams freezing shut,
rime spreading plate by plate, black ice growing) and the dark narrowing sight. It also carries over the idea Hemorath's
pass left open for his Iron Rake: a lane drawn as a filled, sunken trench instead of parallel cracks. Velorum's
Black-Ice lanes are now trenches that black ice wells up to fill. "Subtle" still applies to overlays on his body and on
screens. His attacks, telegraphs and impacts are bold, layered, physical geometry. Nothing here has been compiled or
launched; this stage could not run Gradle. His animation is the section above.

### How it is drawn

- **His own pass.** `VelorumTelegraphPass` (new, `client/render/world/`) draws every Velorum move, the black ice, the
  bodies he strikes, the challengers he holds and the woken devotees' ice. It is one add-only line in
  `ManipulationVisualRenderer` and runs in both of the visuals' passes, in three phases a frame:
  - his solid ice, in the solid pass;
  - his floor and the cuts through the air, in his frost material, with the school flows as accents;
  - the clear panes of a preserving shell.

  It reads the same synced move, `FootprintFrame`, timeline and reveal tick as the kit's `SaintTelegraphRenderer`, so
  every drawn edge is the hit test's edge. `VelorumTelegraphStyle` claims every `VelorumMove` and the black ice and
  paints nothing, so the kit's flat tints never show for him. The kit itself is unchanged. The pass-1 style and rime
  geometry are kept in `removed_assets/`.
- **His own materials** (`VelorumRenderTypes`, built once on one clock):
  - `FROST` on `shaders/core/world/velorum_frost`: translucent, depth-tested, no depth write.
  - `ICE` on `shaders/core/world/velorum_ice`: solid and depth-writing.
  - `ICE_CLEAR` on the same ice shader: translucent and depth-writing, for the shell panes.

  Both shaders are registered add-only in `ShaderInit` (`VELORUM_FROST`, `VELORUM_ICE`) and pass `glslangValidator`
  offline. The vertex colour is state, never a tint:
  - frost: red is frost, green black ice (or a trail's dragged shadow), blue the commit's cold glint;
  - ice: red is rime, green how black the ice is, blue its glint, and alpha the formation.

  Palette: ice blue, pale frost and a pale core (0xE8F4FF) against black and deep violet shadow. There is no warm
  colour; his only red stays the frozen cruor on his halo links. Fine pattern snaps to sixteen texels a block.
- **The frost shapes:**
  - a frozen seam: a dark gap with the cold glowing at its bottom, a rimed lip, and hoarfrost feathers (slanted
    needles, each its own length) growing out across the floor; as the ice rises the gap narrows and fills with clear
    black ice, so it freezes shut;
  - rime: plates of frost appearing one by one as the front (carried per vertex) passes each plate's threshold, fine
    dendrites creeping ahead and the floor stained cold before it arrives, black ice filming between the plates;
  - a black-ice sheet: near black with cold depth, fracture planes and trapped air under a glossy skin, a slow sheen;
  - a trench wall and a trench bed (below);
  - a fold of darkness: a pleated curtain eroding at its top like slow smoke, a cold hairline down each crest;
  - a pool of shadow;
  - a shade: a hooded robed silhouette with a frost rim and two pale eye points;
  - the lantern's light on the floor, with a slow caustic and motes;
  - gloom: slow smoke whose depth is carried per vertex;
  - a trail cut through the air.
- **The ice** (`VelorumIceGeometry`): real faceted shards in three twisting, tapering stages and a point, each with
  its own girth (0.17 times the square root of its scale, so a big shard has mass), lean and cross-section.
  - Round six-sided crystals for rime; flat four-sided blades for black-ice splinters and heaved plates.
  - Clusters are a tall centre shard with shorter ones splaying out, in a low rime collar.
  - Punch: up past full height to 1.12 over 2.5 ticks, then settling to 1 over 3, with the cold light of its birth
    dying away over about 3 ticks.
  - Crumble: it sublimes from the point down over 10 ticks, the front frosting white.
  - Chips: small tumbling octahedra. Each flies on an arc for 6 ticks, skitters a little further across the ice for 4,
    and is gone by 16.
  - Shader: clear ice is blue-white with a milky heart and pale fractures; black ice is near black and a mirror for
    the light. Both carry a cold fresnel rim, hoarfrost crusting the foot and 16-a-block grain.
- **The life of a telegraph** (`VelorumFrostRules.look`), the same for every move:
  - Build (reveal to commit): the seams run their length by 65% of the reveal, the frost thickens from 0.2, black ice
    films over the second half, the glint climbs.
  - Flash (4 ticks): white, seams 30% wider.
  - Active: full frost and ice.
  - Linger (16 ticks, shortened to fit a short recovery so the Repose deed never cuts the frost off): the seams freeze
    shut, the ice sublimes, the whole fades over the last 65%.
- **The trench** (`VelorumFrostRules.trench`, JVM-tested). A sunken face cannot be drawn under the floor, because the
  floor's blocks would hide it, so the pass draws each face on the floor plane where it would show. A point under the
  floor shows where the view ray to it crosses the floor plane: a scaling about the camera's foot by
  height / (height + depth). Every face is clipped to the trench's mouth, so the floor hides what it would: the near
  walls and the bed under the near lip.
  - Only walls whose inner face is turned to the camera are drawn: the far wall shows as a band of layered ice
    darkening with depth, rimed over the lip with icicles hanging.
  - The bed is deep shadow with the cold glowing up through cracks. Black ice wells up through it in pools until it is
    a glossy sheet.
  - Two trenches of different depth meet without end walls.
- **Cuts through the air** follow the animation's own world-space trail: `VelorumAnimState.lanternAt`,
  `sideLanternAt` and `clawAt`, which `VelorumRenderer` records every frame. The effect can therefore never disagree
  with the animation, and a limb that barely moved cuts nothing. The layers, on the Riposte recipe:
  - a dark UMBRA shell he drags;
  - a pale frost blade tapering from tail to head;
  - a cold LUX core;
  - two cold sinews twisting slowly round the spine (his blood barely moves, so a shimmer and no heartbeat);
  - a knot of light at the lantern or the hand.

  A cut runs from 3 or 4 ticks before the commit to the end of its sweep. Then it holds where it was cut (the path is
  kept once the sweep ends, so it needs no history), drifts up, widens and frays over 8 ticks, shedding frost that
  drifts down slowly. The Staff Toll's burst stands where `heelAt` says the heel struck at the commit.
- **Impacts on a struck body** are read from the hurt flash every client already has (the body's hurt time and the
  damage event's source, which must be him), so no packet is added. Over 10 ticks:
  - four clear ice splinters bite in along the blow over 1.5 ticks and shatter out of it from 3 to 8, with chips flung
    on;
  - a cold flash and a dark shell at the chest;
  - frost spray along the blow;
  - the floor frosting at the feet with a small star of seams.
- **Held challengers.** A preserved challenger is shut in a faceted shell of seven clear panes leaning in to a crown,
  which grows from the feet up in 3 ticks. It has crystals at its foot and rime on the floor, and when it opens it
  breaks into 14 falling clear shards over 12 ticks. A kneeling challenger has rime round them and crystals at the
  shins.
- **Woken devotees.** While a devotee thaws, chips of rime crack off it and fall, and a rime puddle at its feet fades
  over 44 ticks. Stilled, or kneeling home, rime crystals climb round it over 30 ticks. This is read from the devotee's
  synced state and its start.
- **Accents** are the school flows only: LUX cold light, RIME frost wisps and EYE sinews, UMBRA shadow. There is no
  vanilla particle and no hit glow.
- **`VelorumRimeGeometry`** keeps three things:
  - the lanterns' light on his body;
  - the presence's seam disc;
  - the cue beats his synced state cannot show: waking, pinning, kneeling home, the Bow and Overheld, the broken Hour
    and the Litany's toll, a guarded blow, the rest.

  The cues the pass now draws from state draw nothing there: Toll, Frost Slash, Still Floor, Long Night, Black Ice,
  Shatter, Preserved, Freed and Gutter.

### Each move and hazard

Ticks are wind-up / active / recovery as authored.

| Move or moment | Build (wind-up) | Commit and active | Linger |
| --- | --- | --- | --- |
| Staff Toll (16/3/18) | Rime spreads across the 70° cone from his hem, plate by plate. Seams fan from his heel, edge seams on the cone's edges, a broken outer arc. | A crystal cluster (six shards, 0.95 tall) bursts up where the heel struck, with chips. Nine blade clusters heave up across the cone at 1.7, 2.55 and 3.4 blocks, half a tick a block. A white frost shock (ring seam, rime band, spray, cold flash) runs to the rim. The lantern's fall is cut through the air from 4 ticks before the commit. | The ice sublimes before the recovery ends; the frost freezes shut and fades. |
| Lantern Sweep (14/6/16), focal | Faint rime over the whole 160° arc; its outer and inner arcs freeze. | The floor behind the sweeping band freezes hard and glazes. Eighteen crystal-blade clusters heave up as the band passes, leaning with the sweep. The lantern's real path is cut through the air at 1.4 scale: shell, blade, core, sinews, knot. | The cut holds, drifts and frays, shedding frost; the blades sublime. |
| Hanging Lantern (14/4/16) | Rime and seams open across the rear arc. | The side lantern's swing is cut through the air. Seven blade clusters heave up across his back at 2 to 2.6 blocks, half a tick apart. | Sublimes. |
| Black-Ice Splinters (24/12/22), the trench | A knot of dark seams at his hem. Each of the five fanned lanes sinks into a trench running out from his feet with the grow, 0.42 deep, its lips freezing and feathering, rime on the floor beside them. The far wall and the empty bed of shadow show under the lip. | The claw's drop is cut through the air. The front runs out at a block a tick. Over the 2.5 blocks behind it the trench is welling (half full, lit by the front's cold); behind that the black ice lies filled, 0.12 under the floor, so the lips still read as a trench. A cold flash and frost spray ride the front. A black-ice blade cluster (three blades, 0.95 to 1.45 tall, leaning on along the lane) breaks out every 1.15 blocks as the front passes, with chips. | Handed to the standing black ice the moment it stands. |
| Black ice standing (hazard, 60) | | The filled trenches, glossy black, their lips rimed, a slow cold haze drifting along each lane; the blades still standing. Sites and seeds are by lane and place only, so the hand-over changes nothing. | Over the last 30 ticks rime greys the ice over. Over the last 14 the blades crumble from the point, the far ones a little later, and over the last 16 the trench closes as the floor heals. |
| The Still Floor (30/3/30) | Rime spreads ring by ring from the bare eye (2.5) out to 10. Sixteen seams run out through it, every other one forking. The eye's edge is a dark frozen ring; the reach is a fainter one. Black ice films between the seams in the second half. | The ring flashes cold white and the seams freeze shut. Three rows of ice plates (at 4, 6.5 and 9 blocks, about 82 in all) heave up rippling out from the eye, a quarter tick a block. A frost shock runs to the rim. | He stands planted; the plates hold, then sublime; the frost fades. |
| Bid to Kneel (28/30/16) | The lantern's cold light on the floor in its 50° cone, firming as it rises. Seventeen rays are each stopped where a pillar or a figure blocks it, so the cover reads. Darkness closes in at both sides of the light (24° of gloom each side). A faint shaft runs from the lantern, and motes are drawn in toward it. | Through the hold, rime spreads at the feet of whoever stands in the light and crystals climb their shins (0.25 to 0.7 tall with the hold). | The light fades with the look. |
| The Long Night (32/10/0) | A pool of shadow spreads under him to 7 blocks while eight narrow folds of darkness rise round him to 2.2 to 3.8 blocks. Each fold is two leaves of pleated dark with a cold hairline down its crests. | The folds sink into the floor over 10 ticks, moving outward, and the dark floods out: a band of gloom with a frost crest runs to 16 blocks. | The Guttering Lantern follows. |
| Set in Place (20/2/24) | Rime gathers in a disc under the held figure, with a ring and a star of seams. Five icicles form in the air 2.8 blocks above it, glinting. | The icicles fall in 2 ticks, the claw's stroke cut after them, and drive into the floor round the figure. A frost shock, stubs 0.7 to 0.9 tall, and chips. | The stubs sublime through the recovery. |
| Recall (20/40/18) | | Five pale threads draw from the nearest awake ally's chest to his raised hand, a bright point running in along each. Rime grows at the ally's feet and climbs its shins with the hold. | Fades. |
| Call to Prayer (24/6/20) | The lantern flares as it is raised. | Two cold frost rings run out to 12 blocks, 4 ticks apart. | Fades. |
| The Procession (26/44/14) | A road of rime is laid along the line, lips freezing; shadow gathers in a pool where the shades come in. | Three shades walk it, each a hooded figure of shadow 2 blocks tall with a dark wisp trailing. Their bare feet leave frost prints, alternating, that fade over 30 ticks. | Fades. |
| The Splinter Crown (26/4/20) | From tick 8 a landing ring freezes under each mark, locking at 14. An icicle 1.6 long grows 6.5 blocks above each, glinting, then falls under gravity through the last 4 ticks. | It strikes at the commit: a frost shock, the icicle standing as a 1.05 stump in a rime collar, six shards splaying out round it, and chips. | Sublimes. |
| The Endless Hour (30/200/30) | Rime spreads from his feet over the whole floor. Twenty-four seams run out and rings at 4 and 8 freeze. | Through the hold the frost thickens and black ice grows between the plates. Gloom creeps in from the floor's rim, 1 to 4 blocks deep, as the dark narrows sight. | The release sublimes it all over the recovery. |
| The Hollow Toll (24/20/20) | Frost gathers at the heel (a rime disc and a star); the reach at 9 shows faintly. | The staff's drive is cut through the air. A ring of frost leaves his feet: a bright frozen front with frost spray lifting off it, and the floor rimed and glazed behind it. Five rows of ice plates (at 2, 3.5, 5, 6.5 and 8 blocks) heave up as the front passes and crumble behind it within about 14 ticks. | Fades. |
| The Draw of Silence (24/20/18) | The disc's frost and seams run in from the rim at 10. | The frost is drawn in toward him through the hold. From each challenger he can see (four at most), a pale stream of drawn silence runs to his closing hand, with body: a dark shell, a frost body, a cold core and slow beads of rime travelling in. | Fades. |
| The Guttering Lantern | | A breathing pool of shadow round his feet: the folds sunk into the floor. | |
| The Bow | | The frost at his feet thaws back over the window. | |
| The Overheld Invocation | | A crack of frost snaps out across the floor from him and fades by 30 ticks. | |
| The ending | The rime round him draws back and fades from tick 16 to 80 as the hood bows. | From tick 40 the frost falls out of the light: twelve fragments of rime slide off his shoulders, stoles and sleeves to the floor and settle. | |

### Tests

- JVM: `VelorumFrostRulesTest` (new) covers:
  - a telegraph's life, including the linger fitted to a short recovery;
  - seam networks staying on their footprints over 40 seeds and being stable per seed;
  - the trench: its far wall's foot exactly where the view ray crosses the floor, nothing outside the mouth, the
    turned-away wall never drawn, end walls only where asked, nothing for a camera under the floor;
  - polygon clipping carrying V;
  - ice punching, holding and subliming;
  - clusters with mass and the centre tallest;
  - chips landing and fading;
  - the icicle, fold, wave, shock, fill, cut and shell clocks.
- JVM: `VelorumEffectGeometryTest` (new) covers:
  - seams and rime keeping their shader tiles;
  - nothing drawn unrevealed or before a seam runs;
  - the trench writing only walls and a bed on the floor plane;
  - ice hidden until it breaks out, standing about its scale, gone once it sublimes, and an icicle hanging point down;
  - one quad a ray for the lantern's light;
  - a bounded trail.
- `VelorumRimeGeometryTest` (rewritten case): the cues the pass draws from state draw nothing there, and a spire has
  four faces.
- `VelorumPresentationGuardrailTest` gains `hisOwnPassDrawsEveryMoveInHisOwnMaterials`: the pass hooked in both passes,
  both shaders registered, three render types built once, and the style claiming every move without the kit painter.
- None of these has been run.

### Needs a launch (not done here)

- Compile the tree; no Gradle or Java build ran in this stage. Then run the JVM suite.
- Launch `runBossPresentationReviewClient -PvelorumFightReview=true` to prove `velorum_frost` and `velorum_ice` compile
  on the GPU (offline validators miss driver errors), and to look at every move, the black ice, the impacts and the
  shells.
- Check the trench from the review cameras and from a player's eye height. It is drawn for the camera, so it should read
  as sunk from any angle; a camera very low over the floor sees mostly the bed.
- Check the frame cost of the Still Floor's 82 plates and the Hollow Toll's 126, and of the five Black-Ice lanes with
  their blades. A shard is 12 to 18 quads; a collar is 12.
- Check that the frost material sorts acceptably with the seams laid over a trench's lip. All of it is translucent and
  sorted by quad, so a lip seam may draw under or over the bed at its edge.
- The animation's trails keep 64 frames, about a second at 60 frames a second but under half a second at 144 or more.
  A cut needs about 7 ticks of history while it runs (it keeps its own path once its sweep ends). At very high frame
  rates its tail may be clipped; spacing the trail's samples by age (a fifth of a tick) would fix that.

## Visual pass 2 (2026-11-06), polish

The effects and animation sections above were written without a build. This stage compiled them, ran them, and
launched the review client five times. Each still was judged against Hemorath's accepted pass 2, the Riposte and the
outpouring, and against what the user said about Hemorath's first build: "debug lines", "SUPER basic" ground
telegraphs, "stiff and disjointed".

### What ran

- JVM: before any change, the Velorum package passed together with the shader-include, render-type caching, easing,
  boss-clock and vanilla-particle guardrails (113 tests). The Velorum package passed again after each round. The full
  `test --continue` run is in `build/velorum-pass2/final-test.log`.
- GameTests: `runVelorumFightGameTestServer` passed 9/9 before the polish and again at the end
  (`build/velorum-pass2/gametest1.log`, `final-gametest.log`).
- Review client: five full runs of `runBossPresentationReviewClient -PvelorumFightReview=true`, each 59 scenes and
  about 973 stills.
  - Every report says `passed`: every move went active, the ending rewarded, and the chamber came back block for block.
  - `velorum_frost` and `velorum_ice` compile on this GPU. The log's only shader line about them is a harmless warning:
    `velorum_ice` declares `HemoTime` but never reads it.

### The harness (`VelorumFightClientReview`)

It now matches Hemorath's harness:

- Moves are forced 6 ticks after the camera settles. They are shot every 3 ticks through wind-up, commit,
  follow-through and settle.
- The Black Ice scene runs on 66 ticks past the move's active span, to see the standing hazard grey and heal. The Long
  Night and the forced Call are also shot every 3 ticks.
- Two options, both in `gradle/review-clients.gradle`:
  - `-PvelorumReviewOnly=a,b` keeps the rise, the named scenes, and the phase floors before a kept scene of a later
    phase;
  - `-PvelorumReviewParticles=minimal|decreased|all` (default all) sets the particle level.
- Added views:
  - `THREE_QUARTER`, for the limb-driven moves: Staff Toll, Lantern Sweep, Hanging Lantern, Black Ice, Set in Place,
    Hollow Toll, Bid to Kneel, Draw of Silence;
  - `LOW`, at a player's eye height: Black Ice, Still Floor, Hollow Toll, Staff Toll;
  - `FAR`, high over the west of the floor: Still Floor, Hollow Toll, Black Ice;
  - `ROAD`, overhead, for the Procession. It hunts the challenger he cannot see, and in the review that is the camera,
    so the road runs under the camera. Earlier rounds missed the road or framed it inside the south wall.
- In the review game dir only (`build/boss-presentation-review/config/theoneprobe-common.toml`, a disposable dir),
  `needsProbe` is now 2. The One Probe's block readout was covering the top left of every still.

### What I fixed

- **The rime was a white rug.** `velorum_frost` shape 1 painted every plate as opaque frost, so any field read as a flat
  sheet of snow: the Still Floor, the Draw of Silence, the Lantern Sweep, the Endless Hour.
  - It is now a cold glaze you can partly see through, with the frost gathered white along the walls between plates.
    A frozen floor reads as a crazed lattice, and the stone and the chamber's blue-ice ring show through.
  - Plates are finer: 3.2 a block (was 2.4).
  - The per-texel sparkle is cut to 0.04 (was 0.1).
- **The trench was a sheet of snow.** The black-ice fill and the empty bed carried 0.25 to 0.6 frost, and the crust on
  black ice was a solid plate mask, so the welled lanes went pale.
  - The crust is now a lattice.
  - The empty bed carries 0.15 frost and the welled ice 0.06, greying to 0.66 only as the hazard ends.
  - The steel sheen on black ice is halved (0.16).

  The filled lanes now read glossy blue-black between rimed lips. The sunken wall shows under the far lip, and the
  greying at the hazard's end is a visible change of state.
- **The five lanes merged into one fan.** Each lip's rime ran 0.6 blocks out over the floor, so neighbouring lanes bled
  together. `LIP_RIME` is now 0.3. The black-ice blades were dense and small. They now stand every 1.45 blocks (was
  1.15) and are 1.1 to 1.6 tall (was 0.95 to 1.45). They also carry less hoarfrost (rime 0.08, was 0.22), so they read
  black.
- **The plates read as confetti.** They are now fewer and bigger:
  - Still Floor: one slab every 2.1 blocks of ring, 0.8 to 1.2 tall, in clusters of 2 or 3.
  - Hollow Toll: one every 1.7 blocks, 0.75 to 1.05 tall, 0.2 girth.
  - Splinter Crown: icicles 2.3 long with 0.26 girth, stumps 1.45 tall, the splay 0.75 to 1.05.
  - Set in Place: icicles 1.25 long, stubs 0.95 to 1.2.
- **The Hollow Toll showed nothing at range.** Through the wind-up there was only a heel disc. Now the reach at 9 blocks
  freezes first: a stronger ring seam with a band of rime just inside it.
- **Bid to Kneel's light was a flat grey wedge, and its shaft fell outside the cone.** The light now has streaks from the
  lantern's cage, sharp cold edges where the gloom begins, and more strength (0.62 fall). The shaft now runs down the
  cone's own bearing, not the frame's forward.
- **The Draw of Silence's streams were hairlines** from the high camera. They are now thick where they leave the
  challenger (0.2) and drawn thin into his hand.
- **The Long Night's folds were black on black.** They are wider (0.6 to 0.95), with a cold crest line and a frost rim
  where the smoke frays at the top.

### Sheets

- Per scene, round 5: `build/velorum-pass2/sheets/` (59 sheets, frames in order, ticks labelled). Rounds 1 to 5 keep
  their own `roundN/sheets/`.
- Side by side with the Riposte and Hemorath's pass 2, round 1 above round 5:
  `build/velorum-pass2/compare-pass2-riposte-hemorath.png`.
- Scripts: `build/velorum-pass2/sheets.py` and `compare.py`.

### Animation as seen

All of this was judged from 3-tick strips, not at real time.

- Staff Toll, three-quarter strip: one body. The lantern draws back and up in a coil, the staff falls in an arc over 3
  ticks, the heel plants a block ahead with the crystal burst at it, and he settles upright.
- Lantern Sweep: the lantern arcs at chest height across his front, and the cut follows it, so `lanternAt` is fed.
- Hanging Lantern: it reads only from his side. The side lantern swings back and the blades heave up behind him.
- Endless Hour: he holds flat.
- Devotees: they thaw and walk.

### Still open

- **The floor work is legible but not yet bold.** From the boss camera his floor work is mostly a pattern on the floor:
  rime lattice, seams, black ice. Hemorath's blood is a saturated hue on dark stone; Velorum's frost is pale blue on
  blue-black. Without the ice that rises (blades, slabs, icicles), a wind-up reads quieter than Hemorath's. The weakest
  are the Lantern Sweep and Staff Toll wind-ups and the Draw of Silence's disc.
- **The rime glaze still shows 16-texel speckle.** At full frost the Still Floor looks granular.
- **The black-ice blades still light up at eye height.** From the high camera they are dark, but from eye height they
  are pale blue with white collars at their feet. The ice shader's key light and fresnel favour clear ice.
- **The Procession's shades read as dark dashes from above.** They are upright cards. From a player's eye they should
  read as figures, but no still shows that.
- **Bid to Kneel's light does not look like his lantern's.** It reads as a lit wedge, and the gloom at its sides is faint
  on his dark floor.
- **The Long Night is a dark scene in a dark chamber.** The folds now carry a cold rim, but the flood of dark reads
  mostly through the screen effect.
- **Two moves are small in frame.** Hanging Lantern and Set in Place are small, and the Set in Place quarry view puts
  the challenger in front of the icicles.
- **A dark pool rises late in the Black Ice scene.** About 90 ticks in, a dark pool and folds rise round him. It looks
  like another move starting under the review's long delay. I did not trace it.
- **Not checked:**
  - motion at real time;
  - frame cost of the slabs (about 50 for the Still Floor and 60 for the Hollow Toll, at 12 to 18 quads each) and of
    the 17-ray light clip;
  - the trail clipping at 144 fps noted under effects;
  - first person when struck;
  - multiplayer.

## Revision build notes (animation)

The ANIMATION lane of R.16, built 2026-11-06 while the SERVER and EFFECTS lanes were mid-edit. No Gradle or Java build
ran. Both generators ran, and the new tracks were checked in an offline Python mirror of the pose code: the main
joints, the claw, a lifted staff and the heel; key frames were rendered from three views. Nothing has been compiled or
launched.

### What is built

- **Frozen Cruor on his body** (`tools/model_export/velorum.py`, regenerated: 498 cubes, 184 joints, 256 by 256 at
  16 px a block). Every `ice` cube is now cruor glass: the shoulder, stole and sleeve spires, the claw's ice, the halo
  icicles, the mantle and lantern icicles and the hem's trail. The halo links are the same cruor. Each shard has a
  black-red heart (`CRUOR_DEEP` over `CRUOR`), edges lit thin (`CRUOR_LIT`) and one to three clots inside the glass
  (`CLOT`, never on a rim). The hoarfrost (`HOAR`) sits on the up faces, the top edge and every point; an icicle's tip
  cube carries `point=True`. There is no blue but the lantern crystals.
  - Glow: `velorum_glow.png` carries the cruor at a dull red, pre-scaled to 0.2 of `CRUOR_GLOW`, so at the layer's rest
    strength (0.72) it reads at about 0.15. Beside it are the crystals and the Bow's seam. 640 lit texels against 8192
    dark, so `VelorumModelTest`'s ratio holds.
  - New `velorum_cruor_glow.png` holds the cruor alone at full `CRUOR_GLOW`, for the Eclipse's 0.8. One glow atlas at
    one strength cannot give the cruor 0.8 while the crystals go out, so the EFFECTS lane's `VelorumFrostGlowLayer`
    should draw this atlas as a second pass in the Eclipse. That pass is not wired.
- **The tracks** (`tools/model_export/velorum_anim.java`, regenerated into `VelorumModel.java`). Every new track was
  authored as data, checked in the mirror and emitted as Java, so the mirror and the file agree.

  | Track | Notes |
  | --- | --- |
  | Arrested Pulse (42) | The R.12 beats. The hard clench also pulls the forearm in a little, to give the strike speed |
  | Cruor Javelins (62, 70 in phase 3) | The staff is levelled at 60 degrees with the cage forward; the claw makes three passes over it; one flick per shaft at 24 + 4i (three, or five by `javelins(phase)`); the cage rocks back 3 degrees and the head nods a beat late. `track()` and `move()` now take the phase |
  | Veins of Frost (58) | The palm-down claw at the waist; the fingers draw in over 14 to 28; the palm turns up and lifts sharply at 28 to 30; the planted staff leans in |
  | The Shadow Step (52) | The body work only: the bow, the staff lifted plumb, the claw raised, the side lantern's swing on the rise. The sink is a new channel `UNDER` and the pure `VelorumModel.stepUnder(t)`, `STEP_DEPTH` 3.0 blocks |
  | Gloam Cuts (64) | The coil at the left shoulder; cut 1 down to the right knee at 22; cut 2 back across low at 28; over the head at 32.5; cut 3 flat across at face height from 34 to 35.5; held across the chest to 48 |
  | The Eclipse (230) | The lantern before his face at arm's length, the claw over the cage closing one finger every 5 ticks, the light out at 40 (`CORE` and `SIDE_CORE` to 0). The hold is exactly flat: duplicate keys at 16/194 and 46/194. The head turns 10 degrees at 80, 130 and 180. Unbroken, the recovery lowers him into the Gutter's first pose |
  | Relit | `relit(a, r)`, a 20-tick jolt laid over whatever plays next: the claw flies open, the head snaps back, the staff is knocked back, the light bursts |
  | The Splinter Crown | The palm-up claw is held low over the stains, then lifts sharply and closes at 26 to 27.5, instead of pulling down |
  | The Release of Cruor | `blackIce` is renamed `cruorRelease`; the beats are unchanged |

  - The heel test needed two fixes. The javelins' grip slide waits until the staff has tilted, and the Eclipse slides
    its staff up 1.5, so a lifted heel never dips below the floor. The Eclipse's worst heel in the mirror is now 24.0.
  - Strike speed through the commit, against the wind-up's mean (mirror): Pulse 3.8x, Javelins 6.0x, Veins 8.5x, Cuts
    14.3x, Crown 16.7x.
  - The claw's lowest point is 10.7 units, in the Cuts' first cut (the limit is 12).
- **The sink** (`VelorumAnimState.under(boss, partial)`). It is read from the synced move clock: the Step on its move,
  and the phase-3 card on a card clock that starts on the frame the synced phase first reads 3, never for a body first
  seen in phase 3. It reads the synced clocks because the renderer's `scale()` runs before `setupAnim`.
  - `VelorumRenderer.scale` translates the body down by it, as `SaintRiseRender` does, so the floor's depth hides him.
  - The anchors (core, claw, seam, heel, lanterns) and their world trails go down with him.
  - `heap(under)` raises the floor the cloth meets: the trains and robe fold out on the surface, and a panel whose root
    has gone under follows him down instead of folding upward through the floor.
- **Track priority** (`VelorumAnimState`). The selection is unchanged: the ending over an opening over a move. The six
  new moves are ordinary move tracks. Two overlays ride on top without stealing the blend:
  - `cardTime()`: the Step's keys played over the card's 50 ticks;
  - `relitTime()`: set on the frame the Eclipse is cut off inside its hold (authored 40 to 199), by a new move or by an
    opening over it.
- **Eclipse shape lifecycle** (`VelorumAnimState`). `shapes()` (0 to 1) rises over 40 to 60 on the Eclipse's authored
  clock and holds. Unbroken, it sinks over 200 to 220; relit, it falls over 12 ticks. `eclipseTime()` is exposed for
  the effects.
- **The devotee** (`velorum_devotee.py` and `_anim.java`, regenerated: 44 cubes, 16 joints, 128 by 128).
  - Black-red veins run under the bare hands, feet and face: straight runs and right-angle branches a texel wide, dark
    in the base atlas and dull red in the glow atlas.
  - New shade atlases, `velorum_devotee_shade.png` and `_shade_glow.png`. The body is a glossy near-black (`DARK`) with
    a violet-black sheen (`SHEEN`); the hood's and hem's lower rows tear open (an alpha cut, broad and few). The glow
    carries two pale eye points, cruor-red hands and the veins.
  - Poses: `poseShapeRise`, `poseShapeReach` and `poseShapeGrip` sit beside `poseShade`, built on `shapeChannels`.
    - The figure's joint groups free legs, then body, then arms, then head, so the shape starts exactly in the block's
      own pose and the head comes last.
    - The reach leans in with one arm raised to point. The grip reuses the woken devotee's grip.
    - `shapeStretch(rise)` runs from 1 to 1.25.
    - The thaw and the shapes now share one `figure(a, pose, head, arms, body, legs, hunch)` helper.
  - `VelorumDevoteeAnimState.shape(key, now)` gives each shape its own state and blend, keyed by its figure and pruned
    when unused.
- **The renderer** (`VelorumRenderer`). One translucent dark type (`entityTranslucent` over the shade atlas) and its
  eyes type, both static constants, draw the shapes with a baked `VelorumDevoteeModel`.
  - The Procession's shades rise out of the far pool over their first 12 ticks of walk (from 2.2 blocks down), walk
    the road posed by `poseShade` at their distance, face along it, and fade over their last 6 ticks.
  - The Eclipse's shapes:
    - **Where they stand.** One stands out of every lower-half `FrozenDevoteeBlock` in the frame's chamber box (scanned
      once per Eclipse, by move serial) and out of every stilled or kneeling devotee.
    - **Reach and grip.** A Velorum-owned hazard with a `Tether` or `Lane` footprint whose frame origin is within 1.5
      blocks of a figure is that figure's reach. A lane reaches toward its aim. A tether reaches through its 16-tick
      tell, then grips toward its target. The body turns toward the target as it reaches.
    - **Pinned figures.** A pinned figure's shape gets no reach, so it never reaches.
  - `shouldRender` stays true while a Procession or an Eclipse plays, or while shapes stand.
  - The smoke at the hoods and hems and the reaching limbs are the EFFECTS lane's (`VelorumDarkGeometry`).
- **Tests** (written, not run):
  - `VelorumAnimationTest`:
    - the new moves join the existing walks (stance in and out, heel, claw, cloth);
    - strike speed is checked for the Release, the Pulse, the Javelins, the Veins and the Cuts;
    - the unbroken Eclipse hands its pose to the Gutter;
    - the Eclipse holds flat from 60 to 193 but for three 10-degree head turns, and the lantern is out;
    - the Step sinks him upright and brings him back hood first, and `cardUnder` sinks him too.
  - `VelorumAnimStateTest`: `CRUOR_RELEASE`.
  - `VelorumDevoteeModelTest`: a shape starts in the block's own pose for all five poses, rises legs first and head last,
    stretches to 1.25, and reaches and grips.

### What this lane assumes of the others

- **SERVER: move names.** `VelorumMove.CRUOR_RELEASE`, `ARRESTED_PULSE`, `CRUOR_JAVELINS`, `VEINS_OF_FROST`,
  `UMBRAL_STEP`, `GLOAM_CUTS` and `ECLIPSE` exist with R.5's timelines. The switch in `velorum_anim.java` names all
  seven, so it does not compile until they do.
- **SERVER: Javelins in phase 3.** Five shafts at 4 ticks apart in an active span of 28.
- **SERVER: the Step.** The move to the destination happens at the commit (tick 24).
- **SERVER: the phase-3 card.** The move to the ring happens at about card tick 23 (24 of the Step's 52, scaled to 50),
  while he is fully under.
- **SERVER: the relit Eclipse.** It ends the move inside its hold, by the Gutter opening or a new move. An unbroken
  Eclipse plays its 30-tick recovery before the Gutter.
- **SERVER: the Eclipse's reaches.** They are Velorum-owned hazards. A grip is a `Tether` from the figure (frame
  origin within 1.5 blocks of the figure's base, target the challenger, its tell its first 16 ticks); a "reach
  together" is a `Lane` from the figure toward its aim. No kind id is read. A Velorum-owned lane or tether that starts
  within 1.5 blocks of a figure during the Eclipse would pose that shape too.
- **SERVER: which figures become shapes.** The client cannot tell a figure's server state (FROZEN, PINNED, HOME), or
  the excluded first figure, from its block. Every frozen block in the chamber gets a shape; only the reaches tell a
  pinned figure apart.
- **EFFECTS.** The Javelins and the Cuts read `lanternAt` and `clawAt`, which now follow the sunk body too. The
  Eclipse's cruor glow at 0.8 needs `velorum_cruor_glow.png` drawn by `VelorumFrostGlowLayer`. The smoke cards and the
  limbs follow the shapes, which this lane does not expose beyond the renderer.

### Still open

- The phase-3 sink is keyed to the synced phase change. If the server moves him at another card tick, he will pop.
- The relit jolt rides over the Gutter's 5-tick blend. Whether it reads as one jolt needs a launch.
- The shades walk with a real stride, but the road can bend, and they face along it only per frame.
- The Step's depth is 3.0 blocks, not R.12's 2.8, so the lantern's cage goes under.
- To check in the review client:
  - the Cuts' arm path;
  - the javelin's levelled staff from the boss camera;
  - the Eclipse's lantern before his face;
  - how the shapes read in the red seam grid.

## Revision build notes (effects)

The EFFECTS lane of R.16, built 2026-11-06 while the SERVER and ANIMATION lanes were mid-edit. No Gradle or Java build
ran. Both shaders pass `glslangValidator` offline with their includes inlined. Nothing has been compiled or launched, so
none of the tests below has run and no shader is proven on a GPU.

### What is built

- **The materials** (`velorum_ice.fsh`, `velorum_frost.fsh`, `VelorumRenderTypes`).
  - **Ice.** The ice shader is now Frozen Cruor. Its body runs from `CRUOR_LIT` (thin, lit) to `CRUOR_DEEP` (thick).
    - One to three `CLOT` nodules sit inside each face, under the specular. Fracture planes show as lighter cruor.
    - `HOAR` crusts the foot, the face edges and the points. The geometry also raises a face's frost with how level it
      lies (`VelorumIceGeometry.crusted`), so slab tops, collars and leaning points crust pale.
    - Blue appears only as frost light: the rim, the specular, the glint.
    - Shell panes are thin cruor glass at 0.55 alpha with `HOAR` edges.
  - **Frost.** Its "black ice" channel is now frozen cruor. Seams hold dark blood under a frost lip, and the trench walls
    and bed are cruor strata. The rime glaze is dark red under a pale lattice. Six new shapes:
    - 11, liquid dark: a ground patch whose alpha is its coverage, torn by noise at every edge;
    - 12, a smoke tatter;
    - 13, the floor's own block seams with blood welling and setting (the patch counts from a whole-block corner, so the
      seams land on the real ones);
    - 14, a stain;
    - 15, a gloam cut;
    - 16, a vein ridge.

    The frost shader is 21.9 KB, under the 24 KB split line, so there is no `velorum_dark` shader, no `ShaderInit` line
    and still three render types.
  - **Fog.** Telegraph fragments take the fog at 0.35; the dark shapes take it whole.
  - **Glow.** Both shaders read a new uniform, `CruorGlow`: 0.15 at rest, 0.8 in the Eclipse. It is declared in both
    `.json` files and set by `VelorumRenderTypes.begin(ticks, glow)` from the bosses' synced Eclipse. The three types
    are still static constants. Their composite state is `SchoolRenderTypes.create`'s, with the glow set beside the
    clock.
- **The dark is drawn first.** `VelorumTelegraphPass` has a new `DARK` phase, batched and ended before the `FLOOR` batch,
  so every telegraph draws after the dark. It draws:
  - every `HAZARD_DARK_POOL` (a growing `Disc`, a `Lane` strip up to its front, the Eclipse's `Region`), via
    `VelorumDarkGeometry.pool`;
  - the Eclipse's dark from his state when no `Region` pool stands for it;
  - the Gutter's flood to his feet.

  A pool is the liquid surface, a rim of torn smoke cards that lift, fray and tear away (one every 0.9 blocks of rim),
  and slow gloom over a big disc's middle. `VelorumDarkGeometry.Light` pushes it back: the lantern's `L` round its floor
  point, and a 1.5-block hole round each awake ally. Both are torn across about a block, not cut.
- **The cruor comes from somewhere** (`VelorumCruorGeometry`).
  - Floor moves: old blood wells along the block seams through the wind-up and sets at the commit. This is drawn under
    the Toll's and the Hanging Lantern's cones, round the Release's feet, ring by ring for the Still Floor, behind the
    Sweep's band, on the Hollow Toll's front, over the whole floor for the Endless Hour, and under his heel for the
    Javelins. The presence's disc darkens with blood as the viewer grows still.
  - The Release's blades form as Cruor Javelin shafts do: three frost strands twist up round the site over the three
    ticks before the front arrives, then the blade punches.
  - Set in Place's icicles rise out of the body as beads on a thread of blood before they stretch and fall.
  - The Splinter Crown no longer drops icicles from the halo. The stain beads up through the wind-up, and five to seven
    cruor splinters, 1.6 to 2.3 tall and leaning out, burst up out of it at the commit.
  - The Draw of Silence's stream is the challenger's blood: a cruor ribbon with a frost skin, with cruor beads that
    travel in and a knot at his fist that grows with what is drawn.
- **The six new moves**, each drawn from the server's footprint (read in `VelorumMove`):
  - **Arrested Pulse.** In the wind-up a cruor ring tightens to the beat's disc at the target's feet, blood darkens in
    the seams under them and frost crusts at their wrists. The mark is drawn from the `PULSE_MARK` cue:
    - the shared `FROZEN_VEINS` skin, which `VelorumEffects.pulseMark` now attaches;
    - the wrist frost;
    - a ring that tightens beat by beat;
    - through each 12-tick tell, the 1.5 disc's rim freezing and cruor points pushing out of the silhouette.

    `PULSE_BURST` draws five to seven spines (0.9 long) with frost fingers, chips and a frost shock. `PULSE_BEAT` (a
    beat walked through) cracks the wrist frost off in pale chips.
  - **Cruor Javelins.** Each part of the server's `Union` is a `Tether` to `anchors[i]`, with its loose as the part's
    delay.
    - The draw: a cruor ribbon climbs from the heel to the cage over four ticks.
    - The shafts freeze together at the cage one after another, from tick 8 to 22.
    - Each line is a scored frost groove to its anchor, with a cruor bead running along it.
    - Each shaft flies point-first at 1.5 a tick, shedding powder, and dives into the floor over the last stretch.
    - A shaft that lands stands 40 ticks in a rime collar with frost fingers, then shatters over 10. A shaft whose line
      a block cut short (pillar, figure, wall) shatters at once into chips. Lodged shafts are kept by Saint, serial and
      shaft, so they outlive the move.
  - **Veins of Frost.** Each vein is read out of `points` (meeting point first, out to each anchor and back) and drawn
    from its figure: a raised cruor ridge with a frost crust and a bright bead at its front. All the fronts arrive
    together at the commit. The meeting disc freezes a ring and its seams fill. Spines erupt every 1.3 blocks along each
    vein, with a cluster in the disc. Each feeding figure's veins flare from its `VEIN_FLARE` cue, as an overlay.
  - **The Shadow Step.**
    - As he sinks (the renderer's `under`), torn smoke rises round him.
    - At `points[0]` a torn column of dark rises as the tell.
    - At the commit `UmbralTearGeometry`'s arrival plays at the destination, scaled to his 2.6.
    - The `Ring` (its part delay honoured) is a low wave of torn dark with knee-high smoke.
    - The phase-3 card's sink and rise are drawn from the `STEP_SINK` and `STEP_RISE` cues, and only when the Step move
      is not playing.
  - **Gloam Cuts.** Each part is a single `Spokes`.
    - Liquid dark gathers smoking on the raised claw.
    - The low paths lie on the floor as torn dark bands. The high path is a frost line under hanging smoke at 1.55.
    - A low cut is a crescent of liquid dark at knee height with a smoke crest, a torn trailing edge and a cold hairline
      at its front.
    - The high cut is a crescent at face height, a curtain of torn dark from 1.55 to 2.6 at its front, and its shadow
      on the floor.
  - **The Eclipse.**
    - The red seam grid fills across the boss floor as the light goes.
    - A cold flash dies at the cage.
    - Each challenger's crust glows dull red at their feet.
    - `HAZARD_ECLIPSE_SHAPE` and `_HELD` stand smoke and glowing veins round each figure. A held shape gets the
      Apotheos's pale hyphae, four straight threads.
    - `HAZARD_ECLIPSE_REACH` reveals the arm's line for its 20-tick part delay (a torn band over glowing seams), then
      throws a torn smoke limb along it: four straight segments, 0.6 to 0.25 wide, ending in a cruor-red hand (a palm
      and four finger shards).
    - `SHAPE_REACH` draws a single shape's 16-tick reach and then its grip while the challenger stays within 6.
    - `ECLIPSE_RELIT` bursts light through his fingers (eight rays) and blows a ring of torn dark back to 16 blocks.
- **Retheme of the old moves.**
  - The Sweep's shell drags torn smoke tatters.
  - Bid to Kneel's sides are liquid dark, no longer faint gloom.
  - The Long Night's pool under him is liquid dark, torn back where his shrinking light holds. At the commit the dark
    runs out along the floor to each new pool; the old wave is kept only while no pool has come.
  - The Endless Hour's rim gloom is liquid dark.
  - The Procession's pool is liquid dark. Its shades are the renderer's devotee model, so the pass adds only smoke at
    hood and hem; the eyes are in the shade atlas.
  - The Gutter's old shadow card is gone, replaced by the flood.
  - The accents' dark is violet-black (`0x140A1E`), not blue-black.
- **Cues and pure rules** (`VelorumEffects`, `VelorumVisualRules`, `VelorumCueLog`).
  - New beats 20 to 32: `STAIN`, `PULSE_MARK`, `PULSE_BEAT`, `PULSE_BURST`, `PULSE_END`, `JAVELIN_DRAW`, `VEIN_FLARE`,
    `STEP_SINK`, `STEP_RISE`, `GLOAM_CUT`, `ECLIPSE_OUT`, `ECLIPSE_RELIT`, `SHAPE_REACH`.
  - A beat about a body packs its entity id into the count (`VelorumVisualRules.pack`), so no payload was added and
    `PROTOCOL_VERSION` stays.
  - `VelorumRimeGeometry.draw` hands the new beats (and `THAW`, whose veins now run warm red for six ticks) to
    `VelorumCueLog`. The pass draws them from there in every phase, in the solid cruor as well.
  - `VelorumEffects.preserved` now sends blood-cell motes from the chest. `cruorRelease` and a `Vec3` `shapeReach` were
    added for the server lane's calls.
  - `VelorumVisualRules` is the timelines: javelin, pulse, step, cut, shape, Eclipse, stain, vein and pool tatters. Its
    `lanternRadius`, `poolRadius`, `floodedRadius` and `poolGrowTicks` delegate to the server's `VelorumDarkRules`, so
    the drawn edge is the tested edge.
- **The viewer.**
  - `VelorumDarkSight` decides from synced state whether the viewer is in the dark: pools, `L`, ally warmth, Searing,
    the Eclipse's hold.
  - It tracks the 60-tick gloam from the hurt flash during the Long Night, the Cuts and the Step.
  - It reads a pulse mark from the cue log.
  - It is the one place the hazard kinds are named.
- **The screen.**
  - New `client/event/VelorumGloamFog` (`RenderFog` and `ComputeFogColor`). Near and far are 6 and 18 blocks, 4 and 14
    in the Eclipse, crossfaded by the darkness-effect setting and eased over about 5 ticks. The colour is violet-black,
    never black.
  - `VelorumMeterStyle` gains:
    - the gloam vignette in `paintScreen`: liquid dark at 0.7 at the edge, its inner edge at 55% (35% in the Eclipse),
      torn smoke tongues along that edge;
    - a 0.55 muffle in the dark;
    - the fronts' teeth as cruor tipped with frost;
    - a cruor heart left of the vessel while marked, beating slower with each beat, frosting before one and cracking
      at a burst.
  - `VelorumFrame`'s rail icicles are cruor under a frost crust.
  - `VelorumFrostGlowLayer` puts the lantern pass out with the Eclipse and draws the ANIMATION lane's
    `velorum_cruor_glow.png` as a second pass, up to 0.8.
  - `VelorumRimeGeometry` lays the lantern's `L` on the floor as a faint pool of cold light.
- **Tests** (written, not run):
  - `VelorumVisualRulesTest` (new):
    - cue packing;
    - `L` and the pools agreeing with `VelorumDarkRules`, and pool rims torn rather than hard;
    - the javelin clocks against `CruorJavelinRules` and the move's numbers;
    - the beats, tells and ring;
    - the Step's upright sink and its 0.6-to-3.5 ring;
    - the cuts' delays;
    - the Eclipse's light, grid and glow;
    - stains deepening, and veins on the seams with at most two turns.
  - `VelorumEffectGeometryTest`, one case:
    - dark, seams, stain and ridge keep their shapes, and dry seams draw nothing;
    - a limb has five joints;
    - `veinPaths` reads each vein from its figure.
  - `VelorumPresentationGuardrailTest`, two cases:
    - both shaders' body colours are red-dominant (or the liquid dark) and blue only in frost light, and both read
      `CruorGlow`;
    - no Velorum file names `OBSCURED`, the gloam fog exists, the dark's batch ends before the floor's, and the style
      claims the pools and reaches.

### What this lane assumes of the others

- **SERVER: hazard kinds.** `VelorumEntity.HAZARD_CRUOR_FIELD`, `HAZARD_DARK_POOL`, `HAZARD_ECLIPSE_SHAPE`, `_HELD` and
  `HAZARD_ECLIPSE_REACH` are read in `VelorumDarkSight` only.
- **SERVER: hazard shapes.**
  - A pool is a `Disc(ORIGIN, cap)` whose radius grows from its age.
  - A strip is a `Lane` whose front runs out.
  - The Eclipse's dark is a `Region(CHAMBER)`.
  - A reach is a `Union` of one `Lane` part whose delay is its reveal.
  - A shape is a `Disc` at the figure's foot.
- **SERVER: footprints.**
  - The Javelins: `Tether` parts to `anchors[i]`, the anchors cut at the occluder.
  - The Veins: `points[0]` is the meeting point, `points` runs out to each anchor and back, and the anchors are the
    figures.
  - The Step: `points[0]` is the destination from tick 0, and `origin` is the destination at the commit.
  - The Cuts: single `Spokes` parts, `height.max() > 1.5` for the high cut.
  - The Pulse: a `Disc` at `TARGET`.
- **SERVER: cues** are called with this lane's signatures, as `VelorumCongregation`, `VelorumStillness` and
  `VelorumEntity` already do. `pulseEnd` ends a mark early, and its `FROZEN_VEINS` skin with it.
- **ANIMATION: the Step's sink.** The body's sink is the renderer's (`under`). The smoke here uses R.12's 2.8, not the
  rig's 3.0.
- **ANIMATION: the Procession's shades and the Eclipse's shapes** are the renderer's, with their eyes in the shade
  atlas. Here only the smoke, the veins, the hyphae and the limbs are drawn.
- **ANIMATION: the grip.** The renderer poses a shape's grip from a `Tether` hazard. The server sends only the
  `SHAPE_REACH` cue for a grip and spawns no tether, so the limb shows here but the shape may not lean into its grip.
- **ANIMATION: the glow atlas.** `velorum_glow.png` carries the cruor pre-scaled to read at about 0.15. The layer keeps
  the first build's colour (205, 225, 255) for that pass.

### Needs a launch (not done here)

- Compile the tree and run the JVM suite (`VelorumVisualRulesTest`, `VelorumEffectGeometryTest`,
  `VelorumPresentationGuardrailTest`, `RenderTypeCachingGuardrailTest`).
- Launch the review client to prove the new `velorum_frost` and `velorum_ice` compile on the GPU (the new `CruorGlow`
  uniform, the `clots` loop with its `break`). Then look at:
  - every R.15 stop;
  - the Eclipse from the `LOW` camera, where the seam grid must keep the floor readable.
- Look at the torn edge of the liquid dark at the lantern's `L`. The coverage is carried per vertex at the patches'
  1-block step, so the cut is torn at about a block. If it reads blocky, the pool annulus needs a finer step near `L`.
- Frame cost: each pool is about 200 dark patches and 30 smoke cards. The Eclipse's box is a patch a block, about 900.
  The seam grid is another 900.
- Draw order inside the `FLOOR` batch is still sorted by quad. Only the dark is guaranteed to come before the
  telegraphs.

### Still open

- The Bid to Kneel light, the frost light and the bar's fill are still the old blue. They are frost light by R.2, but
  worth a look beside the cruor.
- The Draw of Silence draws every challenger's stream as blood. The client cannot tell a remote challenger's blood
  opt-in, so the inactive's pale stream is not drawn.
- `docs/architecture/evidence/source-text-impact-map.csv` needs regenerating: the guardrail test now reads both
  shaders, `VelorumGloamFog` and `VelorumTelegraphStyle`. This was not run while the other lanes were editing tests.

## Revision build notes (server)

Written by the server lane of the 2026-11-06 revision, alongside the animation and effects lanes. No Gradle or Java
build was run: nothing here has been compiled or tested. The compile gate compiles it, and the verify stage runs the JVM
suite and `velorum_fight_validation`.

### What is built

**Moves (`VelorumMove`, 21 in all).**
- `BLACK_ICE` is now `CRUOR_RELEASE`, still id 4, key `cruor_release`.
- Its standing field is the kind `HAZARD_CRUOR_FIELD` (1), raised by `standCruorField`. It uses the effects lane's
  `cruorRelease` cue.
- The six new moves have R.5's numbers: ids 16 to 21, all AREA. Their footprints follow the effects lane's reading
  exactly:
  - **Arrested Pulse.** A `Disc(TARGET, 1.5)` that strikes nothing. At the commit `arrestPulse` marks the target. In
    phase 3 with two or more challengers it also marks the stillest other unmarked one.
  - **Cruor Javelins.** A `Union` of `Tether(BODY, ANCHOR i)` parts with delays `4 i`: three shafts, or five in phase 3,
    where the active span is 28.
    - `aimJavelins` re-reads the aims every 2 ticks of the wind-up and at the commit: the target, its lead (position
      plus 10 ticks of motion) and its mirrored lead.
    - Phase 3 adds two side shafts 2 blocks off the target, or, with two challengers, the stillest other's position
      and lead.
    - Each anchor is the shaft's end, cut by a collider ray at 0.8 above the floor (`shaftReach`), so a frozen figure,
      pillar or wall stops it.
  - **Veins of Frost.** A `Union` of a `Path` (half-width 0.5, height 0 to 1.6) and a `Discs(2.0)` whose landing gap
    (`NEVER_LANDS`) lets only `points[0]` land.
    - `layVeins` lays the veins every 3 ticks until tick 18: `points[0]` is the meeting point, then each vein runs out
      to its figure and back.
    - The corners are `VelorumVisualRules.veinPath`'s, with the vein index as the seed, so the drawn path is the tested
      path. `anchors` are the figures.
  - **The Shadow Step.** A `Union` of one `Ring(0.6 to 3.5, band 1.0, 6 ticks, JUMPABLE)` part at delay 1.
    - `points[0]` is the destination from tick 0.
    - He is moved on the tick before the commit, so the commit's origin is the destination and the ring spreads from
      there.
  - **Gloam Cuts.** A `Union` of three one-spoke `Spokes` at -15, +15 and 0 degrees with delays 0, 6 and 12: 10 long,
    half-width 1.6, 0.8 a tick. The first two are `JUMPABLE`; the third's height band is 0 to 2.6.
  - **The Eclipse.** `None`. Its reach lives in owned hazards.
- **The flying fronts.** The javelins and the cuts strike only at their head (`VelorumMove.head`): from the front back
  one tick's travel plus the body's depth (1.4 for a shaft, 1.0 for a cut), never behind where it left, and nothing
  once it has reached its end.
  - The high cut strikes a body whose top reaches 1.55 (`cutStrikes`). A sneak (1.5) stoops under it; a standing
    body (1.8) or a jumper does not.
  - `strikeOnce` caps strikes per move: two shafts and two cuts a challenger, one strike from the veins (10 in the
    disc, 7 on a vein).
- **Strings.** E: the Step into the Release (60% NEAR or MID, link 6), else Gloam Cuts (40%, link 6). F: the Cuts into
  the Javelins (55% MID or FAR, link 6). G: the Pulse into the Cuts (phase 3, 45% NEAR or MID, link 8).
- **The older moves.**
  - The Long Night no longer applies `OBSCURED`. At its commit it puts the gloam on those with sight of the lantern and
    seeds four pools in the lantern-shadow of the four rank pillars nearest the target (`seedNightPools`).
  - The Procession leaves a rising pool at the road's far end and a strip along the road behind the shades.
  - The Splinter Crown's two extra points come from where his hits spilled most blood in the last 200 ticks
    (`crownSpills`); it falls back to scattered points.
  - Every cruor floor move plays the welling sound at its start.

**The liquid dark (`VelorumDark`, `VelorumDarkRules`).**
- Pools, strips and the Eclipse's dark are owned hazards of kind `HAZARD_DARK_POOL` (2): a `Disc(ORIGIN, cap)`, a
  `Lane` and a `Region(CHAMBER)`. Their centres are held on the leash box (the boss floor and a block round it).
- The radius grows from the hazard's age over 30, 8 or 6 ticks, the effects lane's `poolGrowTicks`.
- The Gutter's flood (+0.25 a tick toward 7, easing back over 40) is tracked from the open opening.
- `L` (`VelorumDarkRules.lanternRadius`) is the same function as `VelorumVisualRules.lanternRadius`. The server feeds
  it from the move, the stage and the Gutter's age.
- A challenger is in the dark inside a live pool on the leash box, outside `L`, not within 1.5 of an awake ally and
  not Searing; in the Eclipse everyone is, but for an ally's warmth.
- Every 10 fight ticks the oldest pool fades (20 ticks) while the pools' own curves cover more than 45% of the boss
  floor.

**The meter (`VelorumStillnessRules`, `VelorumStillness`).**
- `Inputs` gains `dark`; the 14-argument constructor is kept. The dark multiplies the standing, sneaking and item-use
  terms by 1.4 after the presence and before the shelter (not the Slowness and Rime terms), and the drain by 0.9.
- New flags: `FLAG_DARK` (1 << 7), `FLAG_GLOAMED`, `FLAG_MARKED`, `FLAG_PULSE_TELL` and `FLAG_GRIPPED` (1 << 11).
- The gloam (60 ticks) and the Pulse's mark are side state counted by the fight's own tick:
  - beats at 30, 60 and 90, each told 12 ticks before;
  - a pass at an average of 0.13 blocks a tick over the last 10 and under 70; inside an opening every beat passes;
  - the burst is 5, +15 and Slowness I for 20, with 5 and +10 within 1.5;
  - Searing, 10 fed ticks of another's Projection (`velorum_thawing`) or a preservation's release ends a mark early.
- Velorum never takes a challenger's Rime past two levels (`VelorumMoves.rime`). The presence pulse uses it.

**The Shadow Step and the phase-3 card (`VelorumLocomotionRules`, `VelorumLocomotion`, `VelorumEntity`).**
- Phase 2 no longer glides to close on a FAR target.
- The Step's destination (`stepDestination`) is a leash cell 4 to 6 from the target, at 60 degrees or more from the
  line to him, within 1.5 of a frozen or stilled figure; failing that, any such cell. If there is none the Step is not
  eligible.
- Pools open at ticks 2 and 4, and the sink cue plays at 8. He is untouchable (`damageTakenFactor` 0) and his presence
  is paused from tick 14 to four past the commit.
- At the phase-3 card he sinks and is moved to the ring at card tick 23, under from 14 to 27, with no arrival ring.
  `plant` only glides when a fight was loaded in phase 3.

**The congregation (`VelorumCongregation`).**
- `feeding` lists the frozen and stilled figures within 14 of a point, nearest first, at most four. Pinned and awake
  figures never feed.
- Waking a figure flares its veins.
- `warms` (1.5 from an ally) and a gaze of 10 blocks in the dark under the Beholding deed.
- Hostile devotees go ×1.25 in the dark, and their grip fills 2.0 a tick there.
- The Eclipse puts out every rime lantern, the votive stands and crown lamps included (`darkenEverything`), and brings
  each back (`relightEverything`).
- Each frozen, stilled or pinned figure stands up as a shape: `HAZARD_ECLIPSE_SHAPE` (3), or `_HELD` (4) for a pinned
  one, a `Disc` at its foot.
  - A free shape, once risen (20 ticks), reaches for the nearest challenger within 4 (5 under the Beholding): a
    16-tick tell, then a grip of at most 30 ticks (4 at the grip, then Slowness II and +1.2 a tick), torn free at 6
    from the figure, and a rest of 60.
  - Each reach is backed by a `Tether` hazard of kind `HAZARD_ECLIPSE_REACH` (5) from the figure's foot toward the
    challenger, so the renderer can pose the grip. The server's hazard tick ignores a tether.
  - At 40, 90 and 140 into the hold, the three free shapes nearest each challenger throw arms: a `Union` of one
    `Lane(9, half 0.6, 0.9 a tick, JUMPABLE)` part at delay 20. An arm roots for 15 (`velorum_eclipse_reach`), deals
    4 and adds +15, once a challenger.

**The Eclipse (`VelorumEclipseRules`, `VelorumEntity`).**
- `tickEclipseGate` starts it at the next boundary once R.7's gate holds. The coverage is sampled every 10 fight ticks.
  The Hour's trigger gains the mirror rule.
- The Hour, the Long Night and the Eclipse are queued through one `queue` field, because the controller keeps a single
  forced move and a second start would replace the first.
- At tick 0 the lanterns go out for the wind-up and hold (the hint and the `eclipseOut` cue play). The hold begins at
  the commit.
- Held Projection on his body in the hold feeds the relight, at the lesser of 3 and the item's rate, through the new
  source `velorum_kindling`, with the waking's grace and bleed. 20 fed ticks relight it, 30 under the Litany.
- Relit: the shapes and the chamber's dark end, every pool tears back, `L` flares, and `skipToRecovery` opens the
  60-tick Gutter.
- Unbroken: everyone at 60 or more is preserved, the rest take 6, and the Gutter is half the phase's length.

**Stains and spills.** Every Velorum hit that draws blood (`VelorumMoves.hurt`, both overloads) is recorded for 200
ticks. Every 20 fight ticks, from phase 2, the `stain` cue shows each challenger's stillest spot (depth = its share of
the last 100 ticks) and the four heaviest spills.

**Lang (add-only).**
- `blood_source.hemomancy.velorum_kindling`.
- The hints `.hint.dark`, `.hint.pulse` and `.hint.eclipse`.
- Move names for `cruor_release` and the six new moves.
- `boss.hemomancy.velorum.hazard.cruor_field` and `.dark_pool`.

**Tests.**
- JVM, new: `VelorumDarkRulesTest`, `VelorumPulseRulesTest` and `VelorumEclipseRulesTest`.
- JVM, extended:
  - `VelorumMovesTest`: 21 ids, the floors after the cut, the javelins' tethers and heads, the veins' seams and disc,
    the cuts' heights, the Step's ring and untouchable span, strings E to G.
  - `VelorumSelectionRulesTest`: the new weights; the Tenebris share and the simulated coverage grow from phase 1.
  - `VelorumStillnessRulesTest`, `VelorumCongregationRulesTest`, `VelorumDeedRulesTest`, `VelorumOpeningRulesTest`
    and `VelorumLocomotionRulesTest`.
- GameTests: the ten R.15 cases in `VelorumFightGameTests`, each in its own batch. They tick the owned hazards with
  him (`tickWithHazards`), since a pool's age is its own tick count.
- `VelorumFightClientReview`'s seven `BLACK_ICE` references are renamed.

### Calls made here

- **Phase-3 Javelins are five shafts, not six.** R.5 lists the target, lead, mirror, a double lead and two side shafts,
  but says five. The double lead is dropped: target, lead, mirror and the two side shafts (or a second target's two).
- **Pools grow from their age even in openings.** R.3's "pools do not grow in an opening" is not built: the hazard's
  age is the clock both sides share. Only the Gutter floods them, so the Repose line holds trivially.
- **The Step moves him on the tick before its commit.** The kit takes the frame's origin at the commit, before
  `onTick`, so the arrival ring can only stand where he rose if he is there already. The animation lane assumed the
  commit tick itself.
- **The meter's numbers.** The dark fills from 0 in 103 ticks standing and 69 in his presence. R.3's 102 and 68 round
  down, as section 4's "95" did.
- **The Eclipse's lantern curve.** The server's `eclipse` input runs linearly over the wind-up. The client's runs by
  the fingers (`VelorumVisualRules.lightOut`), so `L` can differ by up to about a block during the 40-tick wind-up,
  when nothing of the dark's strikes.
- **The relit flare's input.** The server counts its own ticks since the relight. The client must do the same.
- **Hazard and hint names.** Two hazard name keys (`cruor_field`, `dark_pool`) were added though nothing reads them
  yet.

### Still open

- **Lang.** `boss.hemomancy.velorum.move.black_ice` ("Black-Ice Splinters") is still in `en_us.json`; lang is
  add-only for this lane. Nothing reads it. R.15's "no black ice in Velorum lang" rule is not in
  `VelorumPresentationGuardrailTest` yet; the key has to go before it is added.
- **Docs.** `docs/HEMOMANCY_REFERENCE.md` §5.8, `docs/SAINT_MAUSOLEUMS.md` and `docs/OPEN_QUESTIONS.md` still
  describe the black ice and the `OBSCURED` Long Night. They are shared files, left to the final review.
- **Duplicated rules.** `VelorumVisualRules.lanternRadius`, `poolGrowTicks`, `poolRadius` and `floodedRadius` mirror
  `VelorumDarkRules`. Make the visual rules delegate once the lanes settle.
- **The arrival ring's easing.** `VelorumVisualRules.arrivalRing` eases the radius, but the hit `Ring` is linear.
- **The Step's destination.** It is random among the qualifying cells. A look in the chamber should confirm it lands
  among the congregation and not at a pillar's foot.
- **Not covered by GameTests:**
  - the Eclipse's shapes and arms (no figures in the cases);
  - the gate's start through the state trigger;
  - the phase-3 card step;
  - the Pulse's warming and its two marks;
  - hostiles in the dark;
  - the stains.
- **Not run.** JVM tests, GameTests and the compile itself.

## Revision polish (2026-11-06)

The three revision lanes above were written without a build. This stage compiled them, ran the tests and the GameTests,
and launched the review client six times (five full runs, one filtered). Each still was judged against the user's words:
blood ice (dark cruor under frost) and liquid darkness, not a generic ice wizard; bold and physical; legible from the boss
camera.

### What ran

- Compile: every source set compiled on the first try. Another session had already built `main` with these sources.
- JVM: the Velorum package passed at once. The full suite had one failure, `AnimationTimeGuardrailTest`: the renderer
  read `hazard.tickCount + partial`. The final `test --continue` run passed: 5512 tests, 0 failures
  (`build/velorum-cruor/final-test.log`).
- GameTests: `runVelorumFightGameTestServer` passed 19 of 19 before the polish and again after the server fixes below
  (`gametest1.log`, `gametest2.log`).
- Review client: rounds 1 to 4 and round 6 are full runs (86 scenes, about 2,060 stills each). Round 5 is filtered to the
  Gloam Cuts and the relit Eclipse. Every full run's report says `passed`:
  - every one of the 21 moves went active;
  - the Pulse marked the quarry;
  - the Eclipse held with all 37 lanterns dark, was relit by Projection, and preserved the quarry when left unbroken;
  - the ending rewarded, and the chamber came back block for block.
- Shaders: `velorum_frost` and `velorum_ice` compile on this GPU, `CruorGlow` uniform included. The only shader line in
  the log is the old harmless warning that `velorum_ice` declares `HemoTime` and never reads it.
- Frame cost: the harness now logs the lowest frame rate per scene (`round6/fps.txt`). In round 6 every scene stayed at
  or above 102 fps on this machine, the Eclipse from the wide camera included (112). Round 3 dropped most Eclipse stills
  while another session was building at the same time. Round 4 did not repeat that, so it was not the Eclipse's cost.

### The harness (`VelorumFightClientReview`)

- Scenes for every new move, shot every 3 ticks:
  - phase 1: `arrested_pulse` (still, so it bursts) with `_side`, `pulse_pass` (walking), and
    `dark_pools_gutter_flood` (the Long Night from `FAR`);
  - phase 2: Cruor Javelins, Veins of Frost, the Shadow Step and Gloam Cuts, each with `_side` (three-quarter) plus
    `_low` or `_far` views, and `javelins_on_cover`;
  - phase 3: Gloam Cuts and the five javelins;
  - the Eclipse four ways: `eclipse_unbroken` (`WIDE`), `eclipse_dark_low` (`LOW`), `eclipse_reach` (`AISLE`) and
    `eclipse_relit` (`CLOSE`).
- The 25% floor's card is now shot every 3 ticks, to see the Shadow Step to the ring.
- New views:
  - `NORTH`, high behind him looking north: the Veins, the Step and the Procession, whose road now runs toward the quarry;
  - `ACROSS`, from the west across the line from him to the quarry: the javelins and the cuts.
- Each scene now resets three things:
  - **The target.** It hands his controller the quarry as target, through reflection, review only. He keeps a target in
    sight for good, and after the HUD scenes raised the camera's own stillness, every later move went at the lens. In
    round 1 the Pulse's spines burst in the camera's face and the javelins and veins ran under it.
  - **The opening clock.** It opens and closes a one-tick opening of no kind, so his opening pressure never calls a Long
    Night in the middle of another move's scene.
  - **The recalled ally.** It stands any devotee within 5 blocks of him at the floor's south-west corner. The ally that
    Recall stilled at his feet had stood in front of every near camera and caught the quarry's Projection.
- The Pulse's quarry is pinned in place, because a burst's blow knocked it out of frame before the next beat.

### What I fixed

Server:
- **The Long Night never opened the Gutter.** Its recovery was 0. The kit opens a recovery opening on the recovery's
  first tick, so with no recovery the Gutter never opened, and the opening pressure then called the Long Night again
  every 42 ticks. This is the "dark pool rising late" that Visual pass 2 left open. The recovery is now 1, and
  `VelorumMovesTest` checks that the Long Night and the Eclipse each have a recovery tick.
- **A stain could hang in the air.** Stains (each challenger's stillest spot, and where his hits drew blood) are now
  laid on the arena floor. The hovering camera had left a red quad floating in the air.

Material (cruor, not ice):
- **At eye height every splinter read as clear white ice.** The ice shader's foot crust covered the lower half of any
  shard with frost 0.5 or more, so Splinter Crown, Still Floor and Staff Toll blades and the Pulse spines came out white.
  Now the crust is a ragged band at the foot, the last tenth of the point and a hairline edge, and level faces crust only
  in patches. The cluster collars are dark cruor with frost only where they lie level (`CRUOR_COLLAR`; they were
  `FROSTED`, which read as white blotches). The level-face frost raise is 130 (was 170).
- **The preserved shell read as rose quartz.** Its panes are now thick oxblood glass: state 0.3 frost and 0.6 thick
  (was 0.45 and 0.05).
- **The rime glaze was salt on a red cloth.** The walls between plates are thinner (0.05) and greyer (0.55 of HOAR at
  0.8), and the twinkle is rarer and dimmer.

The Eclipse:
- **The red grid was a neon lattice.** The seam is now a hairline, 1 to 1.5 px. Its glow is mixed from `CRUOR_LIT` and
  `GLOW`, and runs unevenly along each seam with clotted dark spots. A soft under-light falls on the stone beside it.
  The lips of a frosted seam are patchy instead of a white grid.
- **The shapes stood unseen against the dark, and the first fix outlined every cube like a wireframe.** The shade glow
  atlas (`velorum_devotee.py`, regenerated; the Java model is unchanged) now lights only the lower robe with a dull cruor
  glow rising up the cloth, as if from the seams.

Bolder moves:
- **Cruor Javelins were hairlines.** The shafts are twice the girth (`SHAFT_GIRTH` 0.15), with a wider frost sheath, a
  heavier powder wake, bolder grooves (2.2x) and a bigger running bead. The draw up the staff is wider, and the shafts are
  held further apart at the cage.
- **Veins of Frost were lines on the floor.** The ridge is 0.6 wide, and a crest of cruor knuckles (0.18 to 0.3 tall)
  heaves up along the seam behind the running front.
- **Arrested Pulse.** The burst spines have a blade's girth (0.14) and break out from inside the body. The tell points
  are larger. The mark's ring is a band of blood with a lighter frost lip; the white rim stays only as the beat's tell.
- **Gloam Cuts were dark on a dark floor.** Each cut now lays a shadow of liquid dark on the floor, carries a bold
  frost-light leading edge (a `rime` ribbon 0.16 wide) and has a fuller smoke crest. The crescent has a violet-black
  sheen rising toward its edge.
- **The gloam vignette drew a hard line across the screen.** Its tongues were triangles based on the band's inner edge,
  where the band's own gradient is already zero. They are now diamonds that thin out on every side.

From the polish notes of the lanes:
- the dead lang key `boss.hemomancy.velorum.move.black_ice` is removed (archived in `removed_assets/`), and
  `VelorumPresentationGuardrailTest.noBlackIceInHisLang` keeps it out;
- `VelorumVisualRules.arrivalRing` is linear, like the hit `Ring` (a test checks the midpoint). Its lantern and pool rules
  already called `VelorumDarkRules`;
- a reserved GLSL word (`patch`) is renamed;
- `docs/HEMOMANCY_REFERENCE.md` (the Velorum paragraph), `LORE_REFERENCE.md` (his appearance), `SAINT_MAUSOLEUMS.md`,
  `OPEN_QUESTIONS.md` and `TESTING.md` are updated, and the source-text impact map is regenerated.

### Sheets

- Per scene, final round: `build/velorum-cruor/sheets/` (86 sheets, frames in order, ticks labelled).
- Every round's stills are kept in `build/velorum-cruor/round1` to `round6`, with their own `sheets/`. Round 6 is the
  final full run; round 5 holds only the Gloam Cuts and the relit Eclipse.
- Before and after: `build/velorum-cruor/compare-before-after.png`. It sets the same scenes from
  `build/velorum-pass2/round5` (Visual pass 2) beside round 6, at the same ticks (for Black Ice, its successor the
  Release of Cruor).
- The new moves, polish round 1 against round 6: `build/velorum-cruor/compare-new-moves-round1-round6.png`.
- Scripts: `build/velorum-cruor/sheets.py` and `compare.py`.

### Still open

- **The Eclipse from the wide camera is a red grid in a black room.** The gloam's fog (4 near, 14 far) hides the aisles,
  so from high and far the umbral shapes do not read at all. They read from the aisle and at eye height, as dark hooded
  figures lit red from below with pale eyes. Whether the fog should spare the shapes is a call for the user.
- **The reaching hands read pale.** The Eclipse limbs' cruor hands are small and come out whitish in the dark. They need
  a darker state or a glow of their own.
- **The Shadow Step's arrival is quiet.** He sinks into a torn pool and rises out of another, but the risen column of
  dark and the arrival ring barely show from `NORTH`. The tear needs more body.
- **Gloam Cuts are readable, not loud.** Low and close they read as violet-black crescents with cold edges. From the boss
  camera across the floor they are still modest.
- **Staff Toll and the Hanging Lantern are small at range.** Their cruor blade clusters are dark and smaller than the
  old white crystal heaps, so the wind-ups read quieter than before from the far cameras. At eye height they read as
  blood ice.
- **The mark's ring and every welled seam keep frost-white lips.** This is the shared seam shape (shape 0). Changing it
  would change every seam in the fight.
- **Not checked:**
  - motion at real time;
  - first person when marked, gloamed or gripped;
  - multiplayer: two Pulse marks, javelins split over two targets, Eclipse reaches for each challenger;
  - a fight on Fabulous graphics;
  - any GPU other than this one.

### Verdict

Velorum no longer reads as an ice wizard. Every cut, blade, slab, spine, javelin and shell is dark oxblood cruor with
frost only on its lips, points and level faces. The before and after sheet shows a blue-white fight become a blood-red
one. The cruor shows where it comes from: blood welling in the seams before a floor move, veins running out of the
figures, and spines and a shell out of the challenger's own blood.

The Tenebris half is now present and works: pools of liquid dark that flood in when the lantern gutters, the Shadow
Step, smoky gloam cuts, and an Eclipse that turns the room dark and stands the congregation up as shapes. It is weaker
than the cruor half. The liquid dark is dark on a dark floor by nature, and it reads at a player's eye and up close
better than from the boss camera.

The strongest stills:
- the Release of Cruor at eye height;
- the javelins flying across (`ACROSS`);
- the Veins erupting under `NORTH`;
- the Long Night's flood and recession;
- the Eclipse from the aisle.

The weakest:
- the Eclipse from high and far;
- the Shadow Step's arrival;
- the Staff Toll at range.

Real-time feel has not been seen.
