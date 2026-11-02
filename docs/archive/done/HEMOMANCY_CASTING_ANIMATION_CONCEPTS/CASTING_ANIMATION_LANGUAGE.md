# Blood Manipulation Casting Animation Language

**Movement revision:** All 40 player tracks and bespoke overrides now keep gestures at chest/shoulder height and preserve vanilla torso pitch. Sustained casts hold one settled pose until release/cancellation. The original bodily pumping, large bends and repeated twitch/hauling storyboards below are historical concept direction, superseded by [the restrained implementation](../../../wiki/Casting-Animations.md). Animated blood anatomy remains separate from limb motion.

> **Status:** Implemented shared presentation system; concept storyboards retained  
> **Target:** Hemomancy for NeoForge 1.21.1  
> **Purpose:** Document the visual language and implementation contract. See [preview and validation](../../../wiki/Casting-Animations.md).

## 1. Design premise

Each of Hemomancy's eight blood tendencies should possess a recognizable casting language. Manipulations belonging to the same tendency should appear related even when their effects differ, while higher manipulation ranks should perform increasingly grand and anatomically unnatural versions of the same underlying motion.

The intended progression is broadly comparable to a school-based spellcasting system such as *Skyrim*, but it must remain specific to Hemomancy:

> A hemomancer is not summoning an external element. They are forcing the blood to remember an instruction, and the body performs that memory.

The animation system should therefore be primarily bodily. Particles, temporary anatomy, render layers, and environmental effects reinforce the casting motion; they do not replace it.

### 1.1 Blood-first revision

Every tendency remains hemomancy before it becomes elemental or conceptual. The visible medium is blood: liquid streams, droplets, clots, vessels, capillary membranes, hardened blood, or circulatory anatomy. Fire, neural electricity, light, deathly staining, frost, iron, and shadow alter that blood rather than replacing it. As a general visual target, blood material should occupy roughly 70–80% of the effect and the tendency-specific coloration or phenomenon roughly 20–30%.

## 2. Hard rig constraint

All base casting animations must be designed for the ordinary Minecraft player skeleton. The reliable articulated parts are:

- Head
- Torso/body
- Left arm
- Right arm
- Left leg
- Right leg

The design must not depend upon independent:

- Fingers or hands
- Wrists
- Elbows
- Knees
- Spine segments

Temporary model attachments may be used at advanced ranks, but the underlying pose must remain readable when reduced to the six ordinary player parts.

### 2.1 Readability rule

Animations should favor:

- Large rotations
- Strong silhouettes
- Deliberate symmetry or asymmetry
- Clear anticipation, release, and recovery poses
- Hard pauses at important frames
- Readability from approximately twenty blocks away

Subtle articulation will be lost on the Minecraft player model and should not carry essential information.

## 3. Layered animation grammar

The proposed animation language has five layers:

1. **Primary tendency** determines how the body moves.
2. **Cast purpose** determines where that motion is directed.
3. **Manipulation rank** determines how much of the body and environment participates.
4. **Cast type** controls playback duration and looping, independently of rank. Vein-condition variation is deferred.
5. **Secondary tendency**, when present, supplies a restrained accent rather than replacing the primary pose.

This creates visual consistency without requiring every manipulation to receive a completely unrelated animation.

## 4. The eight tendency casting languages

### 4.1 Animus — Life

**Core ideas:** Circulation, heartbeat, growth, expansion, living pressure.

**Base motion:**

- One or both arms draw inward toward the torso.
- The torso contracts or pitches slightly forward.
- The arms then sweep or thrust outward as the torso opens.
- Alternating arm movements can create a pumping rhythm.

**Silhouette:** Contracted body expanding into an open pose.

**Advanced manifestations:**

- Swollen vascular overlays along an arm or the torso
- Rhythmic enlargement synchronized to a heartbeat
- Temporary arterial tendrils emerging from the body
- A spectral vascular heart contracting around or behind the caster

Animus should feel vigorous rather than delicate. Even restorative or agricultural Animus manipulations are acts of forced vitality.

### 4.2 Flammeus — Fire

**Core ideas:** Ignition, friction, violent metabolism, pressure, expulsion.

**Base motion:**

- The casting arm or both arms recoil behind the torso.
- The torso twists or pitches into a violent forward or downward release.
- The head recoils slightly from the resulting heat or pressure.
- Area casts may use both arms in an explosive outward drive.

**Silhouette:** Coiled anticipation followed by abrupt projection.

**Advanced manifestations:**

- Ember-lit veins
- Smoke or flame issuing from the head and torso
- Charred vascular fissures across the limbs
- Pinkish-red flame with black-tipped motifs, matching established Flammeus effects

Flammeus should appear physically forceful and somewhat difficult to contain.

### 4.3 Ductilis — Lightning and Nerves

**Core ideas:** Neural conduction, reflex, excitation, involuntary response.

**Base motion:**

- Arms snap through several sharply separated poses.
- The head and torso jerk in opposing directions.
- Small pauses divide each impulse, creating a staccato rhythm.
- The final discharge occurs after the body completes the apparent circuit.

**Silhouette:** A body seized by controlled electrical impulses.

**Advanced manifestations:**

- Luminous nerve paths beneath the skin
- Nerve-like arcs joining the arms, torso, and nearby targets
- Brief pose afterimages showing successive impulses
- Increasingly violent involuntary movement during charged casts

Ductilis should never flow smoothly. Its defining trait is discontinuous, instantaneous movement.

### 4.4 Lux — Light

**Core ideas:** Revelation, exposure, symmetry, radiance, liturgical authority.

**Base motion:**

- Both arms rise, open, or part in deliberate symmetry.
- The torso becomes upright and centered.
- The head lifts toward the revealed subject or source of light.
- Offensive Lux casts preserve this formality even when directing power at a target.

**Silhouette:** Open, balanced, elevated, and impossible to conceal.

**Advanced manifestations:**

- Additional eyes rendered on the head, torso, or arms
- A vascular halo or radial light structure
- Translucent flesh revealing the skeleton or circulatory system
- Symmetrical luminous rays organized around the player pose

Lux should resemble a revelation or proclamation rather than conventional holy magic.

### 4.5 Mortem — Death

**Core ideas:** Extraction, debt, collapse, decay, the withdrawal of vitality.

**Base motion:**

- The torso hunches and the head lowers.
- One arm sweeps across the target or affected area.
- That arm retracts sharply toward the torso as though hauling something inward.
- Both arms may then fall or collapse toward the player's sides.

**Silhouette:** A hooked, contracted figure drawing life toward itself or pressing it downward.

**Advanced manifestations:**

- Gaunt or lengthened arm overlays
- Darkened veins spreading toward the torso
- Flesh appearing to recede from the model
- A larger cadaverous or skeletal silhouette repeating the player's motion

Mortem should look acquisitive and costly. Even when it benefits the caster, something is visibly being taken.

### 4.6 Congeatio — Cold

**Core ideas:** Arrest, compression, preservation, rigidity, delayed consequence.

**Base motion:**

- Arms move slowly inward or across the torso.
- The legs widen into a planted stance.
- The head lowers or fixes upon the target.
- Every animated part stops simultaneously at the release frame.

**Silhouette:** Gradual contraction ending in absolute immobility.

**Advanced manifestations:**

- Crystalline limb shells
- Frost spreading inward from the extremities
- Rigid blood plates or osseous growths
- The caster becoming a preserved figure inside a larger crystalline anatomy

The cessation of movement is more important than the movement itself. Congeatio should appear to halt a process already underway.

### 4.7 Ferric — Iron

**Core ideas:** Weight, forging, resonance, magnetic alignment, permanence.

**Base motion:**

- The legs plant apart.
- The torso lowers to establish weight.
- One arm braces while the other performs a hammering, hauling, or driving movement.
- Every movement ends in a hard mechanical stop.

**Silhouette:** A grounded worker, smith, or living piece of machinery.

**Advanced manifestations:**

- Armored vascular ridges
- Iron filings or fragments aligning around the limbs
- Temporary plated arm or torso overlays
- Nearby metallic matter bending or orienting toward the caster

Ferric should appear heavy even when the manipulation is instantaneous. Its recovery should communicate mass and resistance.

### 4.8 Tenebris — Shadow

**Core ideas:** Concealment, inversion, displacement, evasion, absence.

**Base motion:**

- The torso twists away from the intended direction.
- The head counter-rotates against the torso.
- One arm crosses the head or torso while the other trails behind.
- Teleportation and reversal effects unfold from a mirrored or displaced version of the departure pose.

**Silhouette:** Asymmetrical, partially concealed, and intentionally misdirected.

**Advanced manifestations:**

- Detached limb shadows
- Doubled or delayed silhouettes
- Temporary disappearance of portions of the rendered body
- An independent shadow performing the cast while the physical player remains still

Tenebris should frequently end somewhere other than where its initial motion implies.

## 5. Rank escalation

The rank ladder should not replace one tendency's pose language with another. Each rank expands the same recognizable motion.

| Rank | Degree gate | Animation scope | Intended impression |
|---|---:|---|---|
| **Humilis** | D0 | One dominant arm with minor head or torso assistance | A direct bodily command |
| **Mediocritas** | D1 | Both arms and torso | A practiced casting form |
| **Summa** | D3 | Full body, stance, recovery, and limited deformation | An imposition upon the world |
| **Magister** | D5 | Held or repeating full-body channel with progressive deformation | The tendency inhabiting the caster |
| **Perfectus** | D6 | Iconic pose plus large surrounding manifestation | The caster temporarily becoming the principle |

### 5.1 Humilis

- One dominant arm communicates the cast.
- The head or torso provides a small supporting movement.
- Legs usually retain the normal stance.
- Quick casts remain brief and combat-safe.
- Particles remain close to the active body part.

### 5.2 Mediocritas

- Both arms participate.
- The torso establishes the tendency's identity.
- The cast gains clear anticipation, release, and recovery phases.
- Particles may visibly travel across the body before release.
- This should become the most familiar tier of casting animation.

### 5.3 Summa

- All six player parts may participate.
- Legs establish a dedicated stance.
- The torso performs a stronger wind-up or contraction.
- Head direction becomes intentionally choreographed.
- Temporary render-layer deformation may appear at release.
- Nearby particles or minor environmental responses may begin during anticipation.

### 5.4 Magister

Magister has authored containment choreography. Charged, continuous and passive powers already exist across ranks, including charged Humilis powers; cast type never follows automatically from rank.

- The player enters a tendency-specific braced pose.
- A subtle looping motion communicates breathing, twitching, pressure, or strain.
- Deformation and particles intensify throughout the hold.
- Release uses a single decisive full-body action.
- Cancellation should produce a recoil, collapse, or reversed deformation.

The Magister identity should be expressed through held containment rather than merely a longer Summa animation.

### 5.5 Perfectus

Perfectus should avoid uncontrolled escalation into faster and busier movement. The player instead assumes an iconic pose and becomes unnaturally still while the world or a larger apparition performs the grand motion.

- **Animus:** A colossal vascular heart contracts around the caster.
- **Flammeus:** A circulatory inferno rises around the fixed body.
- **Ductilis:** Neural arcs force the body through several instantaneous poses.
- **Lux:** The player becomes a symmetrical orant or cruciform figure surrounded by opened eyes.
- **Mortem:** A cadaverous silhouette performs the extraction over the bowed caster.
- **Congeatio:** The player becomes motionless inside a growing crystalline anatomy.
- **Ferric:** The caster plants like an iron monument while surrounding matter aligns toward them.
- **Tenebris:** The physical player remains still while an independent shadow casts.

## 6. Cast-purpose endings

Each manipulation should combine its tendency opening with an ending determined by function.

| Cast purpose | Directional ending |
|---|---|
| **Projectile** | One arm aligns toward the aim direction while the torso drives the release. |
| **Self-buff** | Both arms return toward the torso or affected body section. |
| **Area pulse** | Legs brace while both arms and torso expand away from the center. |
| **Targeted control** | One arm maintains target direction while the other completes the tendency motion. |
| **Construct or wall** | Both arms establish the width, plane, or volume of the intended creation. |
| **Travel** | A counter-turn and narrowing silhouette convey departure or arrival. |
| **Conjuration** | The casting arm assumes the future weapon's carrying line before the weapon appears. |


These endings should remain broad categories. Individual manipulations only need bespoke clips when their gameplay cannot be communicated through an existing category.

## 7. Anatomical vein-strain expression

Deferred concept only: the current enum groups both arms and both legs. The implemented casting system does not infer sided damage or degrade choreography.

| Strained section | Animation initiation |
|---|---|
| **Head** | Head snaps, lowers, recoils, or fixes upon the target before the arms move. |
| **Body** | Torso contracts, twists, or arches before releasing through the limbs. |
| **Heart** | Both arms compress toward the chest while the torso performs a pronounced pulse. |
| **Arms (grouped)** | The associated arm leads the cast and carries the strongest particles. |
| **Legs (grouped)** | The associated leg changes the stance, stamps, recoils, or initiates movement. |

Animation degradation could later communicate vascular condition:

- **Healthy:** Exact, confident movement.
- **Stressed:** Minor tremor or delayed recovery.
- **Clotted:** Pronounced recoil, shortened range of motion, or compensating torso movement.
- **Dead:** The opposite side or full body awkwardly compensates for the failed section.

This is an optional later layer. It should not be allowed to multiply the initial animation scope beyond reason.

## 8. Mixed-tendency rule

For a dual-tendency manipulation:

- The **primary tendency controls the player pose**.
- The **secondary tendency contributes one restrained material accent** at the hands. The following motion examples remain optional future directions.
- The two complete pose languages should not be blended equally.

This visually reflects the existing 75% primary / 25% secondary mechanical weighting and avoids muddy silhouettes.

Examples:

- **Lux + Animus:** Lux symmetry and upright exposure, followed by a single Animus-like torso pulse.
- **Ferric + Mortem:** Ferric's planted hammering motion, followed by Mortem's inward collapse during recovery.
- **Tenebris + Ductilis:** Tenebris asymmetry interrupted by one sudden Ductilis body snap.

## 9. Concept storyboards (visual intent, not gameplay scheduling)

### 9.1 Blood Shot — Animus projectile

1. The casting arm draws toward the torso.
2. The torso contracts forward.
3. The arm thrusts toward the aim direction.
4. The torso snaps upright as the projectile leaves.
5. At higher ranks, a vascular pulse travels along an arm overlay before release.

### 9.2 Activation Potential — Ductilis area pulse

1. Both arms cant outward at uneven angles.
2. The arms snap through two or three alternating poses.
3. The head jerks against the current arm direction.
4. The torso abruptly arches.
5. Lightning discharges when the full body completes the circuit.

### 9.3 Glacial Bastion — Congeatio construct

1. Both arms slowly close toward the torso.
2. The legs spread into a planted stance.
3. The head lowers.
4. Every player part stops on the same frame.
5. The bastion erupts at the instant of total immobility.

### 9.4 Umbral Step — Tenebris movement

1. The torso twists away from the destination.
2. One arm crosses the head or torso while the other trails behind.
3. The head counter-rotates toward the destination.
4. The body disappears from the concealed pose.
5. A mirrored version appears at the destination and unfolds into the normal stance.

### 9.5 Ferric Resonance — Ferric self-buff

1. Both legs plant apart.
2. The torso lowers.
3. One arm braces horizontally before the torso.
4. The opposite arm drives downward across it like a hammer striking an anvil.
5. The torso shudders, then locks upright as metallic particles align around the body.

### 9.6 Unclosing Eye — Lux Saint Canon

1. Both arms begin lowered and symmetrical.
2. The arms part while the torso straightens completely.
3. The head tilts upward.
4. Temporary eye render layers open across the head, torso, and arms.
5. Radial light expands while the caster holds the exposed pose.

## 10. Implemented presentation contract

`CastingClips` stores 40 individually authored six-part compositions and separate first-person arm poses. Opening, hold, release, recovery and cancellation use explicit timing curves, including held Ductilis steps and stopped Congeatio poses. Purpose applies a restrained directional ending; bespoke overrides cover Ironhearted, Umbral Step/Reversal, Avatar, Living Staff forms and the four Saint Canons. Existing staff, weapon, microscope, injection and rite animations retain their occupied limbs.

Quick casts start at release only after successful server execution. Charge heartbeats retain their original clock, including reduced charge after damage. Channel upkeep never retriggers opening. Manual passive toggles gesture briefly; automatic passive triggers keep their existing effect accents without taking the limbs. Neither camera, collision, speed nor gameplay thresholds are changed.

A dedicated payload carries entity and player identity, manipulation, sequence, phase, server timing, charge, hand and resolved presentation. Tracking entry receives ongoing snapshots only. Client expiry, terminal ordering, entity removal, death and level changes prevent lingering poses. The client preview uses a separate local state and sends no gameplay packets.

Standing legs can participate. Walking blends them back to locomotion; riding, swimming, crawling and Elytra preserve required body posture. First-person rendering scopes each hand independently and suppresses the third-person pose while vanilla renders that hand. Armor and skin overlays follow the same model parts, and hidden Morphling hands remain hidden.

Blood materials, vessels and temporary anatomy reuse existing render primitives. The eight concept sheets are material references, not joint specifications. Advanced anatomy uses no additional entities or gameplay fields. Reduced effects simplify or remove large anatomy; recognizable body motion remains.

## 11. Review criteria

The implementation guide records automated and live evidence separately. Continue checking combat-distance readability, occupied hands, both player models, armor, locomotion and specialized animation priority when changing these tracks. A successful source or state test does not establish attachment placement or aesthetic quality.
