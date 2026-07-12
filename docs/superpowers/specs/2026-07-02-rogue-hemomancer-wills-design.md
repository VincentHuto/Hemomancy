# Rogue Hemomancer Wills — Ambusher System Design

> **Date:** 2026-07-02
> **Status:** Design / planned. Largest item in this cluster — a new enemy system, not a tweak. No code yet.
> **Implementation plan:** [2026-07-03-rogue-hemomancer-wills.md](../plans/2026-07-03-rogue-hemomancer-wills.md)
> **Parent audit:** [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md)
> **Counter-mechanic lives in:** [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) §5.3 (*Oculiflora reticularis*)
> **Also references:** [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md) · [LORE_REFERENCE.md](../../LORE_REFERENCE.md) · [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md)

This is the first doc in a chain. It links **back** to the [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) spec, whose *Oculiflora reticularis* scar is the intended counter to the ambush mechanic defined here; that scar spec in turn references the [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md).

---

## 1. Goal

Introduce **Wills** — tiered, semi-hidden rogue hemomancers that ambush the player on the fly using blood manipulations of a specific school. The system:

- turns the mod's rich manipulation kit into a solo **PvE experience that feels like PvP** (blood-mage duels), filling the thin "server-type interaction" gap;
- **reintegrates the orphaned Blood Drunk Puppeteer** from a random mob into a lore-coherent enemy class;
- gives *Oculiflora reticularis* a killer application (threat and counter introduced as a pair);
- **dramatizes the revelation arc** — the Fungal Whispers *tell* the truth; the Wills *show the stakes and test the player*; and
- **resolves that dread into power** — once the player's own Will is strong enough, the fallen can be bent, absorbed, or commandeered (§11).

## 2. Naming: build on "Will," a word the mod already owns

"Will" already means self/ego rendered as a blood-spirit in this mod: `blood_rush` summons a **Wretched Will**; the Degree-6 refuge is the **Chamber of Will**; the Silent Archon's identity is *keeping your will*. So the ambushers are hostile **Wills**, and the existing Wretched Will summon becomes the visible tip of a larger truth — the network is full of Wills, and the loose ones hunt you. No new noun required.

## 3. The two origins — identity, look, and feel

Both types are wanted; they are the two halves of the revelation dramatized. They should look, sound, and fight like opposites, because they *mean* opposites.

### 3.1 Broken Wills — "the Faded" (tragic echoes)

**Who:** dead or absorbed Harbingers whose ego collapsed under the revelation. They run their old school's combat muscle-memory with nobody home — the failure state made flesh. The Silent Archon *kept* their will; these could not.

**Look:** semi-transparent, desaturated, and **flickering** — an unstable dissolve/alpha shader (reuse the Hermit farewell dissolve or the Silent Archon translucency). The silhouette is a half-remembered Harbinger: fragments of their school's robes/armor, incomplete, parts missing or stuttering. A school tint bleeds off them — Flammeus a smoldering ember-ghost trailing ash, Congeatio frost-rimed and brittle, Tenebris a shadow-smear hard to track, Mortem withered and sloughing, Ductilis twitching too fast. They look *unfinished*.

**Feel:** tragic and uncanny. You are fighting an echo, not a mind. Killing one should read as a mercy, not a triumph.

**Behavior:** erratic and compulsive. They stutter, freeze mid-action as the muscle-memory skips, and often **perseverate** — repeating the last manipulation they knew in life, or cycling their school's kit with no strategy. They do not adapt, and their stats are **fixed per tier** — they never scale (see §6), which is the whole point.

**Audio:** whispered fragments of the self that's gone — a name, a broken degree-litany ("Votary… Adept… I was…"), looping and distorted. The Whisper voice, but shattered.

### 3.2 Sent Wills — "Proctors" (clinical examiners)

**Who:** tailor-made by the Fungal Entity to measure the player. Fully in character — the Entity already *watches practitioners ripen* and *reads degree as a sporulation assessment* (the Sanguine Monolith fragment). A Sent Will is the hive reaching out to grade the fruit.

**Look:** solid, coherent, and **wrong because it is too complete**. No flicker. Hive-marked: fungal growths, too-perfect symmetry, a void-register shimmer (the pome-darkness), sometimes a small fruiting crown. Critically, a Proctor **mirrors the player** — a distorted echo of the player's dominant tendency and equipment silhouette, an uncanny reflection of the person fighting it.

**Feel:** clinical and dreadful, not tragic. You are being measured by something that already knows the outcome. Inevitable, not pitiable.

**Behavior:** deliberate, economical, adaptive. A Proctor **counters the player's dominant tendency** — sent to a Flammeus player it resists fire and answers with Congeatio; it punishes your crutches. It **scales with the player** (§6) and fights like a hemomancer who has studied you.

**Audio:** the hive's register — the *same calm voice as the Fungal Whispers*. It may address the player directly and clinically: "Not yet." "Closer." "Show me."

### 3.3 The contrast at a glance

| | Broken Will (Faded) | Sent Will (Proctor) |
|---|---|---|
| Origin | Collapsed Harbinger ego | Purpose-built by the Entity |
| Render | Flickering, translucent, incomplete | Solid, symmetric, hive-marked, mirrors you |
| Combat | Compulsive muscle-memory; fixed stats | Adaptive; counters your tendency; scales |
| Voice | Broken fragments of a lost self | The Whisper voice — calm, clinical |
| Feel | Tragic echo; killing is mercy | Inevitable examiner; being graded |
| Role over time | Predator → chaff → resource (§6, §11) | The escalating true threat |

Broken Wills look backward (what you could become); Sent Wills look forward (whether you are ready for Apotheos).

## 4. Reintegrating the Blood Drunk Puppeteer

The Puppeteer is currently a random mob that drops Prismatic-armor material. Reframe it as **one type of Broken Will** — a Harbinger so lost to **Blood Drunkenness** (the existing foreign-blood backlash effect) that the hive pulls its strings. This explains the entire orphaned cluster: *Blood Drunk* = the failure mode that unmade it; *Puppeteering Thread* = the hive's strings, harvested off the corpse (Prismatic material link preserved); *Enthralled Dolls* = its combat gimmick, a puppet fighting a puppet. Nothing is cut — the Puppeteer gains a reason to exist and becomes the archetype for the wider class.

## 5. Tier ladder (scaling on Initiatory Degree)

The dread engine: the deeper the player goes, the more the hive notices — ambushes escalate in lockstep with the Whispers.

| Tier | Gate | Who | Kit | Feel |
|---|---|---|---|---|
| — | pre-D4 | nothing | — | beneath notice; Whispers not yet begun |
| **I** | D4–5 (Whispers begin) | rare single **Faded**, one school | Humilis manips, flickery | "did I just see something?" |
| **II** | D5–6 | **Broken Wills**, school-flavored, sometimes paired | Mediocritas manips | real duels; ambush pressure |
| **III** | D6–7 | first **Sent Wills** — counter the player's dominant tendency | school combos | the hive starts grading |
| **IV** | D7 / Archon+ (post-Communion) | elite Proctors that mirror the player's build | Summa manips | the final exam before Apotheos |

Strict degree-gating: a Tier-IV Proctor must never reach a D4 player.

## 6. Encounter cadence & the threat shift

The system's signature is that **the same enemy changes meaning as the player grows**, with almost no stat work: Broken Wills enter weak and **never scale**, while Sent Wills arrive later and **do**. As the player ascends, the Faded slide from predator to prey, and the danger migrates onto the Proctors.

**Per-tier feel:**

- **Tier I (D4–5) — the ghost story.** A single Faded, rare, flickering in at the edge of vision, gone fast. "Did I just see something?" It can barely hurt a prepared player; it exists to unsettle and to teach that the Whispers now have bodies.
- **Tier II (D5–6) — the haunting.** Broken Wills in ones and twos, school-flavored, pressing real duels. This is peak Broken-Will threat: they are roughly your equals, and being hunted by the fallen is genuinely dangerous.
- **Tier III (D6–7) — the turn.** The first **Sent Will** appears *alongside* Broken Wills — and the contrast reframes them instantly. The Faded you dreaded last tier now read as manageable adds while the Proctor is the real fight. This is the pivot: the threat visibly changes hands.
- **Tier IV (D7 / Archon+) — the exam.** The Proctor is the danger, scaling with you and countering your build. Broken Wills still appear, but now as **chaff and fodder** the powerful player swats aside — or, better, harvests and commandeers mid-fight (§11). The hive has stopped sending its failures to kill you (they cannot) and now sends purpose-built examiners, almost contemptuously leaving the failures as your fodder.

**Why Broken Wills persist forever.** They keep spawning at every tier past their introduction, but with **fixed, un-scaling stats**. Relative to the ascending player they decay from threat → nuisance → chaff → resource. This is a free difficulty/role transition — the *meaning* of the encounter transforms through the player's growth, not through new content — and it sets up §11: by the time the Faded are chaff, the player's Will is strong enough to *claim* them.

**Composition rule of thumb.** As tier rises, Broken-Will *count* can go up (more fodder) even as their threat goes down, while Sent-Will presence stays rare, deliberate, and scaled. A late-game ambush might be one Proctor escorted by a small pack of Faded — the pack is your resource, the Proctor is your test.

## 7. When and where they strike

- **Ambient chance scaling with degree**, amplified in fungal terrain, near Qliphoth Blooms, and in the Fungal Dimension.
- **Blood Moons** amplify spawns (lore already: "Thirsters and Fargones stir in the dark").
- **After a Fungal Whisper fires**, a Will may follow — the whisper becomes the *herald* of a test.
- **Vulnerability pressure:** deep **Blood Drunkenness** makes the player "smell riper" and draws Wills harder (ties the existing debuff into the system).

**Sanctuaries (no Wills spawn):** Founding Fanes, the Chamber of Will, and Harbinger Outposts are consecrated ground. This hands the territory systems a concrete new value — safety from the hive — and gives the player a reason to build a home, with no new systems designed.

## 8. Combat identity

- **They fight with the player's own systems** — school-keyed blood manipulations, not mob swings. A Flammeus Will opens Sanguine Ignition → Vitric Combustion; a Mortem Will runs Hemorrhage → Exsanguinate; a **Tenebris Will runs Void Shroud + Umbral Step** (double-cloaked — the nastiest ambusher and the strongest argument for Oculiflora).
- **Ambush approach:** a Will drifts in **semi-incorporeal** (hard to see/hit), closes distance, then **materializes and bursts**. Unseen, it is a jumpscare — the setup for the counter in §9.
- **Multiplayer rule:** target the ripest (highest-degree) player, or scale to the group as a shared threat.

## 9. The Oculiflora counter (the matched pair)

The ambush **telegraphs**. When a Will is selected to attack, a **pre-materialization anchor** exists for a few seconds — invisible to normal players, rendered as a **faint ghostly outline to an *Oculiflora reticularis* wearer** (see [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) §5.3). With the scar the ambush becomes a *prepared fight* — seconds to reposition, ward, pick an opener, or line up a bend (§11). Without it, the player eats the jumpscare.

This is the elegant property of the pairing: **the scar's value scales with the threat** — deeper degree → more/stronger Wills → the eye matters more. It is the one thing in any system that converts the hive's assassins from ambush into engagement.

**Guardrail:** Oculiflora must not be *mandatory*. Keep a non-scar cue for everyone (audio sting / Whisper line / HUD tremor) so unscarred players can react; the scar only grants the clean visual lead time. Edge, not entry fee.

*Impl sketch:* the ambush picks a spawn anchor entity; that anchor renders through-terrain only for scar-wearers (reuse Prismatic/Crimson Sight outline tech); a timer then materializes the Will and begins engagement.

## 10. Rewards — "to one it shall return"

Wills do not drop iron and rot. On death they **dissolve — blood returning to the one** — leaving school-keyed spoils:

- **Crude memory shards / enzyme essence of their tendency** — a Flammeus Will becomes a *source* of Flammeus material, plugging the ambushers into the progression economy instead of being pure nuisance.
- **Faded Memory** (rare) — a fragment of the unmade Harbinger, usable as a loom/scar catalyst.
- **Optional ripeness track:** defeating **Sent Wills** ticks a hidden readiness the Entity assesses, foreshadowing or soft-gating Apotheos.

Killing is the floor. Once the player is strong enough, **bending** a Broken Will (§11) yields more than killing it — which is the intended endgame incentive to engage rather than flee.

## 11. Commandeering: bending Broken Wills to your own

The dread has an intended **resolution**: once the player's own Will is strong enough, the fallen stop being a threat and become something to claim. This literalizes the mod's central question — *whose will survives the revelation?* — as combat verbs, and it is *why* a Broken Will is mechanically distinct from a Sent Will: a Faded is an **empty vessel** a dominant enough Will can overwrite; a Proctor is the hive's *active* construct and resists (§11.5).

**Gate.** Commandeering is **Broken Wills only**, and requires high player Will — entry at **Archon (Degree 7)**, deepening past Apotheos / into the Silent Archon path. Below that threshold, a Broken Will can only be fought and killed.

**Subdue first.** You cannot claim a Will at full strength. Reduce a Broken Will below a health threshold and it **falters** — its flicker spikes, its muscle-memory stalls, and it becomes bindable for a short window (a "kneeling"/execute-style opening, echoing Exsanguinate's low-HP gate). The bend must land in that window.

### 11.1 Absorb *(entry — Archon+)*
Channel Blood Absorption on a faltering Broken Will to reclaim it into yourself. The Will enters an `ABSORBING` struggle state with its own progress meter rather than losing ordinary health; completing the channel grants a permanent minor tendency-alignment gain in its school and the absorption reward chance. Dropping the channel lets the Will snap back angry instead of rematerializing helplessly at 1 HP. This is "to one it shall return" routed *through you* — reclamation. It consumes the Will and has no upkeep. **Apotheos-flavored:** a body that has half-joined the hive naturally pulls strays back into the network.

### 11.2 Redirect *(Archon+, blood cost)*
Seize a faltering Will and turn it loose as a **temporary ally** for a short duration: it fights other Wills and hostiles with its school's manipulation, then dissolves. A momentary puppet — ideal for turning a Faded pack against its own Proctor escort. No lasting bind, no cap cost.

### 11.3 Commandeer *(deep endgame — the capstone)*
Permanently bind a subdued Broken Will as a **Claimed Will**: a tethered servant that keeps its school's manipulation and fights for you, counting against a cap. **Reuse the existing Marionette Crossbar / Puppeteer's Spindle economy** — Crossbar charge upkeep, equipped-owner tether support, and the player-wide summon cap — so a Claimed Will is effectively a puppeteer summon you *harvested from the world* instead of unlocking through a trial. **Silent Archon-flavored:** a fixed, overpowering will is that path's whole identity, so it gets the edge here — higher Claimed-Will cap and cheaper Commandeer charge.

### 11.4 The path parallel (why it's elegant)
The two endgame identities get *different default verbs* on the same mechanic, matching their philosophies:
- **Apotheos → Absorb.** Return the stray to the one. Reclamation.
- **Silent Archon → Commandeer.** Impose a dominant will on the will-less. Domination.
- Both can Redirect. The endgame fork's meaning ("dissolve into the hive" vs. "keep your self against it") becomes a *playstyle* difference in how you treat the fallen.

### 11.5 Risk / backfire
Bending is not free. Attempting to bend a **Sent Will**, or fumbling a bind (acting outside the falter window, or over-cap), **bites back**: the Will inflicts Blood Drunkenness or Hematic Strain, and a failed attempt on a Proctor **flags the player to the hive**, escalating the next Sent Will. This preserves Sent Wills as the true threat — you cannot simply enslave the exam — and keeps commandeering a risk/reward play layered on surviving the ambush.

### 11.6 Why bending beats killing
Killing gives the standard dissolve loot (§10); *bending* gives more — essence, memories, or a servant — but demands you survive the ambush and hold the Will (degree) to do it. This is where **Oculiflora** compounds: seeing the Faded materialize early is not just survival, it is setup time to weaken-and-claim rather than weaken-and-die.

## 12. Why this is a keystone, not a side-mob

The Whispers *tell* the truth; the Wills *show* the stakes (fail = become the Faded) and *test* the player (the hive measuring the fruit). By the Silent-Archon-or-Apotheos fork, the player has spent the midgame fighting people whose will did not survive — so "keep yourself" vs. "dissolve into the one" is a choice they were educated into at swordpoint. Then the arc **completes**: the same fallen that once hunted the player become, at high Will, the player's to absorb or command (§11) — dread resolving into mastery, with the two endgame paths each claiming the fallen in their own idiom. Oculiflora ties the bow: the player uses the hive's own connective tissue to see the hive's own assassins coming.

## 13. Scope & balance flags (read before building)

- **Biggest build in this cluster:** new caster entity AI running the manipulation kit, a degree-scaled dynamic spawn/ambush system, the materialization + pre-spawn telegraph, sanctuary exclusion, a loot economy, and the bend/claim layer. Sequence it after the scar/morphling reshuffles.
- **Frequency is load-bearing.** On-the-fly assaults get infuriating if they interrupt building/exploring. The sanctuary rule and a per-player cooldown are mandatory, not optional. Everything behind server config (frequency, tier gates, enable/disable, bend enable).
- **Difficulty gating** strictly by player degree; never over-tier a player.
- **Fixed-stat Broken Wills, scaling Sent Wills** — the cheap engine behind §6's threat shift; keep Broken stats flat so they decay into fodder naturally.
- **Commandeer must not trivialize the game.** Cap Claimed Wills tightly (reuse the puppeteer cap), keep upkeep real, and make Redirect short — a Faded army should not replace the player's own kit. Sent Wills staying unbindable is the release valve.
- **Opt-in tone:** Wills should feel like the *cost of ascending*, avoidable by staying out of fungal terrain and using sanctuaries, not a grind wall. The Silent Archon path might draw fewer (they refused the hive) even as it commandeers better.
- **Don't make Oculiflora mandatory** (§9 guardrail).

## 14. Open questions

1. Do Broken and Sent Wills share one entity type with data-driven origin/school, or separate types? Lean one type, data-driven.
2. Should the "ripeness" track (§10) be real, or pure flavor? Decide before wiring Sent Wills.
3. Do Wills despawn if the player flees to a sanctuary mid-fight, or pursue to the boundary?
4. How many schools spawn per encounter at Tier IV — single mirrored school, or a small coven with a Faded escort?
5. **Commandeer routing:** Claimed Wills reuse the puppeteer summon route (Marionette Crossbar/Spindle economy), not a Drudge variant.
6. **Bend gating:** is Archon (D7) the right entry, or should Absorb open earlier (D6) with Commandeer reserved for D7/endgame?
7. Should a **Claimed Will decay** over time (the empty vessel unravels) even with upkeep paid, to keep them impermanent and thematic?
8. Testing: entity registration/AI, degree-gated spawn rules, fixed-vs-scaling stat split, sanctuary exclusion, the Oculiflora telegraph render, loot tables, and the subdue→bend state machine; update [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) (mob entities) and [LORE_REFERENCE.md](../../LORE_REFERENCE.md) (the Wills, the two origins, the Puppeteer reintegration, commandeering).
