# Manipulation powers: gameplay, acquisition, balance and casting visuals

> Follow-up: implementation areas 1–5 and the [Area 6 acceptance pass](manipulation_work_plan/06-acceptance-results.md) are recorded in the [work-plan index](manipulation_work_plan/README.md). Area 6 fixes Hematic Flare targeting/payment and Funeral Bell PvP leakage, records 263 passing dedicated GameTests, and retains explicit JVM/survival/visual gaps. The findings below remain the original audit baseline; current behavior, chosen balance values and captured effects are in the linked results.

**Actionable follow-up:** [Prioritized work plan — 32 tasks, dependencies and acceptance checks](manipulation_work_plan/README.md).

**Audit date:** 7 September 2026. **Scope:** the current working checkout, including uncommitted changes. **Method:** static tracing of registrations, player casting paths, progression grants, recipes, effects, entities, event handlers and presentation code. This is an implementation audit, not a live combat or visual playtest. Numerical damage is nominal before defenses, immunity frames and build modifiers. **One heart = two HP.**

## Overall assessment

The registry contains **112 powers: 97 active and 15 retired compatibility entries**. The active roster has **61 quick, 16 charged, 12 continuous and 8 passive** registrations. Most have a substantive playable action. The main weaknesses are inconsistent relative value, hidden acquisition/operating costs, uneven target validation, and several high-rank effects whose presentation is much less developed than their names and costs imply.

The strongest existing foundations include Avatar's staged manifestation, Blood Binding's persistent tendrils, Black Veil Covenant's dome, Lignum Mortis's selection presentation, staff weapon forms, and the conductive combat family. These need refinement and live verification, not wholesale replacement.

### Highest-priority findings

| Priority | Finding | Why it matters |
|---|---|---|
| 1 — behavior | **Endless Hour's deferred-damage and repayment methods have no production callers.** | Actual behavior is powerful temporary potion protection; the advertised death-deferral/debt loop is not integrated. |
| 1 — economy | **Exsanguinate restores 600 mL without requiring successful damage or a kill.** | A 300 mL cast can generate blood from a target that did not yield any. |
| 1 — charge | **Hematic Mortar and Blood Needle Lance keep their payload while charge fraction reduces cost.** | A one-tick release is about 5 mL before modifiers, greatly distorting their value. Funeral Bell also retains disproportionate status-bonus damage on short releases. |
| 1 — healing | **Blood Rush heals 0.5 HP every tick.** | Up to 10 HP/s from a Degree-0, 100 mL power overwhelms late healing comparisons, including Carrion Communion. |
| 1 — defense | **Ward costs only 10 mL per pulse and gets its initial shield again on channel start.** | It undercuts Ironhearted; verify that its own stop cooldown prevents immediate channel restarts. |
| 1 — targeting | **Ally, visibility and failed-cast rules differ substantially.** | Some attacks affect allies or reach through walls; Living Circuit/Lumen Suture may help opposing players; some rejected actions still spend blood. |
| 2 — boss balance | **Osseous Bloom deals percentage-current-health damage without an explicit boss cap in its action.** | Cheap memory production buys exceptional scaling compared with costly flat-damage powers. |
| 2 — progression | **The self-built Somatic Loom requires Degree III.** | Degree-0/I learning metadata does not mean players can self-craft those memories at that point. Early alternate routes are essential. |
| 2 — value | **Several Degree-V/VI powers lose comparisons with basic spells.** | Ironhearted/Ward, Carrion/Blood Rush, Drift/Shroud, Coronation/Halo, and Furnace/Living Torch need coherent roles and prices. |
| 2 — visuals | **White Verdict, Eclipse Well, Absolute Stillness and several reactive capstones lack clear world-space forms.** | Generic particles do not communicate beam, pull radius, stasis coverage, stored ammunition or readiness. |

These are source findings or explicit design assessments, not claims of reproduced live exploits. Per-power entries below distinguish the two.

## How to interpret the numbers

### Unlocking and advancement

| Registry rank | Earliest learning degree |
|---|---:|
| Humilis | 0 |
| Mediocritas | I |
| Summa | III |
| Magister | V |
| Perfectus | VI |

Learning also requires the appropriate active-blood path, a non-retired power, and any family prerequisites. **Alignment is principally a casting requirement**, not the same gate as absorbing a memory. A newly learned power can therefore remain unusable at the listed alignment threshold. Living Sickle has a stricter Degree-VII acquisition route despite its Degree-VI registry rank.

Family forms require the baseline and the indicated shared mastery level. Actual cumulative credits are **10 / 35 / 85 / 185** for levels I / II / III / IV; the code sums its threshold array. Mastery reduces base blood cost and cooldown by 5% per level, up to 20%. A paid quick/charged use normally earns a credit, while sustained channels/avatars earn one per five seconds. Credits are not proof of useful combat: some paid no-op casts still count. Later forms do not generally require purchasing every intervening form.

### Blood, cooldowns and opportunity cost

- **All displayed costs are registry base mL.** The baseline reservoir is 5,000 mL: 100 is 2%, 500 is 10%, 1,000 is 20%, and 2,000 is 40%. These percentages are context, not safe-cast counts; reserves, upgrades and repayment obligations matter.
- **Manipulation cooldowns are independent.** A successful cast starts only that registered manipulation's timer, allowing other equipped powers to be chained while it recovers. Family forms have separate timers.
- **Continuous cost is paid immediately, then once per second.** Its listed manipulation-specific cooldown is applied on stopping and prevents only that same channel from restarting until it expires.

> Cooldown labels in the per-manipulation entries below were captured when cooldowns were universal. Read every “shared cooldown” label as that manipulation's own cooldown; the numeric values and modifiers remain current unless otherwise noted.
- **Avatar passive cost is upkeep** with an initial payment and one-second pulses. Sovereign Instinct/Phoenix Debt instead pay on their reactive event. Blackhearted uses a separate event-driven mechanism with no blood drain.
- **Charged cost scales with held fraction**, normally up to the listed full-charge price. Ironhearted and Thread Ripper explicitly require full charge. Some other forms do not scale all output to match the discount.
- The common payment check requires **blood greater than the cost**, so holding exactly the displayed amount can fail. Some action-specific invalid cases are discovered after the shared system has paid and recorded a successful use.
- Runtime cost also reflects Efficiency, mastery, Dynamic Use/tendency, purity, pome effects, blood drunkenness, thurible/mnemonic effects and optional integrations. Blood moon and Qliphoth Bloom can each supply discounts. Compare builds with the actual cost ledger rather than treating this report's base price as final.
- Casting also creates section-specific vascular strain and shifts tendencies/advancement. The report lists the registered section; strain is configuration/progression dependent, not simply proportional to mL. Memorized-slot capacity adds opportunity cost, while absorption/projection have fixed slots and the sickle has special handling.

## What making a memory really costs

**85 active powers have a normal memory-weaving recipe.** The remaining twelve use staff forms, automatic/tool progression, or the Circus reward route. Absence of a woven recipe in those cases is intentional.

1. Build/access a **Somatic Loom**. Its self-build recipe requires Degree III, 150 projected blood, a held Sanguine Formation, bookshelves/iron bars and Hematic Iron Blocks. Shared or otherwise obtained infrastructure can change access; the self-build gate is the verified baseline.
2. Make a **blank Hematic Memory**: four Sanguine Formations, four Blood-Stained Stone and one Neurotic Enzyme. This blank is additional to the named catalysts and enzymes in each weaving JSON.
3. Supply the recipe's catalyst, enzyme quantities and projected blood, then complete the manual weaving interaction. Enzymes are spent incrementally; drift can consume extras and cancellation does not recover already spent blood/enzymes. Listed recipes are minimum successful costs.
4. Absorb the finished memory through a lit **Iron Brazier** using blood absorption. The rite takes approximately three seconds and consumes the memory on successful grant. Learning gates still apply.

### Enzyme production is a major hidden cost

Current Mycelial Lantern fruiting recipes take **2,400 ticks (two minutes)** and **0.25 mL/tick**, or **600 mL per enzyme**. The culture is retained. Looted enzymes and parallel lanterns can reduce active waiting and blood production requirements.

For a woven recipe with **E listed enzymes**, the per-power reference estimate below is:

> **Weaving blood + 600 × (E + 1) mL**, and **2 × (E + 1) minutes** of serial lantern production.

The extra one is the blank's Neurotic Enzyme. These estimates **exclude** catalyst production, infrastructure, acquisition travel, absorption/tool setup, weave drift and player time. They measure a production route, not guaranteed farming time or a simultaneous reservoir requirement.

Examples: a one-enzyme/50 mL recipe means **1,250 mL and four minutes** through this route; Blood Rush means **5,150 mL and sixteen minutes**; baseline Avatar means **5,800 mL and eighteen minutes plus a Nether Star**; complete Avatar means **8,550 mL and twenty-six minutes plus a Nether Star**. Collecting all five Avatar memories costs five stars, but all five are not mandatory to reach the complete form.

### Earlier and special acquisition routes

- **Crude memories:** Blood Shot, Blood Rush, Deadly Gaze, Sanguine Mending, Hemorrhage, Glacial Grasp, Sanguine Ignition and Void Shroud. Right-click learning costs 500 mL, avoiding loom/brazier work. All appear in Harbinger Outpost chest loot; Thirsters provide Shot/Rush routes and Hematic Constructs provide Gaze. Loot chances and actual travel time still determine practical access.
- **Scarred weaving alternatives:** Blood Rush, Blood Eclipse, Hemorrhage, Sanguine Ignition and Umbral Step have alternate scar catalysts. Exact alternatives appear in their entries; they still need a blank and the loom.
- **Staff grafts:** seven ordinary weapon memories use a blank plus Sanguine Formation, their catalyst and one physical enzyme. Earned activity/advancement and Living Staff absorption at the brazier gate the graft. The activity gate matters even if the item can be crafted first.
- **Automatic/tool grants:** Absorption and Projection are granted at Degree I. Conjure Staff is learned through the Degree-I Living Staff structure. Old retired memory items do not make these active powers missing content.
- **Living Sickle:** Degree VII, Silent Archon, Vesper defeat, staff bond and Memory of Vesper consumption. **Thread Ripper:** Circus Liberation reward, not a standard craft. The finale's direct grant result deserves verification; this audit does not establish an early-rank failed reward as reachable.

Crafting burden labels below describe the **catalyst bottleneck**, with the full enzyme/production cost shown separately. They are design judgments, not measured farming times. Vivianite has Bog Revenant/exploration-loot routes; Fervent Husk comes from Fervent Chitinite. A Scrying Dish itself takes three Hematic Iron Scrap; the vascular gauge and vitality chalice require additional mod materials. Saint residua make the four saint memories encounter-gated despite their small enzyme counts.

## Per-power audit

**Status key:** *Established* means the intended core action and supporting state are visible in source, not certified bug-free or playtested. *Functional* means it executes but has a material balance, targeting, lifecycle or readability concern. *Partially integrated* means a core advertised mechanic lacks its required connection. Visual recommendations are design proposals; existing FX are judged from source rather than screenshots.

Each entry lists its exact registry ID, degree, casting alignment, section, base operating price, charge/cooldown, acquisition recipe and an independent gameplay/balance/visual assessment. Family mastery is additional to degree.

### Enzyme names

| Weaving tendency | Physical enzyme |
|---|---|
| Animus | Vivacious |
| Ductilis | Neurotic |
| Ferric | Ferric |
| Lux | Incandescent |
| Flammeus | Fervent |
| Congeatio | Frigid |
| Tenebris | Umbral |
| Mortem | Ruinous |

### Navigation

[Animus](#animus) · [Ductilis](#ductilis) · [Ferric](#ferric) · [Lux](#lux) · [Flammeus](#flammeus) · [Congeatio](#congeatio) · [Tenebris](#tenebris) · [Mortem](#mortem) · [Saint memories](#saint-memories) · [Living Staff and conjured tools](#living-staff-and-conjured-tools)

## Animus

27 active registrations.

### Blood Shot

`blood_shot` · **Established; balance review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Quick. **100 mL**; **0.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_shot.json): 1 × Bleeding Bulb; Animus × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Fast blood projectile; applies 50 seconds of Blood Loss at amplifier 2. Damage inherits arrow/velocity behavior rather than a fixed spell value.
- **Effort versus utility / balance:** Cheap memory and casts make a strong baseline. Its bleed undermines Hemorrhage's shorter, weaker application. Immunity frames prevent simply multiplying damage attempts into DPS.
- **Casting and visual design:** Authored projectile/impact already suit it. Improve muzzle formation and a readable wound mark; retain the renderer.

### Guided Blood Shot

`guided_blood_shot` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Shot** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Head**.
- **Operation:** Quick. **140 mL**; **1 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_guided_blood_shot.json): 1 × Bleeding Bulb; Animus × 2 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Adds up to three seconds of homing toward a target within 24 blocks.
- **Effort versus utility / balance:** 40% extra blood buys reliability. Sensible mastery-I sidegrade; test target choice in crowds.
- **Casting and visual design:** Use a curved trail/acquisition cue. This variant lacks the baseline's explicit fallback cast-sound mapping.

### Hematic Mortar

`hematic_mortar` · **Functional; charge-cost defect** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Shot** and reach shared mastery **III** (85 cumulative credits). Vascular section: **Head**.
- **Operation:** Charged. **200 mL** at full charge; **3 s shared cooldown**; **2 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_mortar.json): 1 × Bleeding Bulb; Animus × 4 (**4 enzymes**); **250 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,250 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Arcing projectile explodes over roughly four blocks for nominal 4–8 damage. The payload does not scale with charge.
- **Effort versus utility / balance:** Shared charge discount permits about 5 mL for a one-tick release with the same explosion, instead of 200. Fix before comparing against Vitric Combustion.
- **Casting and visual design:** Show a swelling blood mass, descending trail and impact ring. Current projectile/explosion does not explain charge strength.

### Sanguine Halo

`sanguine_halo` · **Established; strong value** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Shot** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Head**.
- **Operation:** Quick. **220 mL**; **6 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_halo.json): 1 × Bleeding Bulb; Animus × 5 (**5 enzymes**); **400 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **4,000 mL + 12 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Five orbiting single-use projectiles launch sequentially at nearby eligible targets; unused shots expire after ten seconds. An existing halo blocks recasting.
- **Effort versus utility / balance:** 220 mL for five autonomous attacks is attractive. Mastery IV matters, but this Degree-0 form undermines premium Crimson Coronation.
- **Casting and visual design:** Orbiting ammunition is already legible. Add a formation sound and clear launch cadence.

### Blood Binding

`blood_binding` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path. Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **125 mL**; **3 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_binding.json): 1 × Foul Paste; Animus × 1, Ductilis × 1 (**2 enzymes**); **75 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,875 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Aimed eight-block root lasts six seconds, halved against bosses. Eligibility, allies and PvP are checked before payment.
- **Effort versus utility / balance:** 125 mL is generous but short reach and boss resistance give actual limitations.
- **Casting and visual design:** Crimson ground tendrils persist with the root. Preserve them; improve release feedback.

### Lingering Blood Binding

`lingering_blood_binding` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Binding** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **165 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_lingering_blood_binding.json): 1 × Foul Paste; Animus × 2, Ductilis × 1 (**3 enzymes**); **125 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,525 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Extends the same root to twelve seconds.
- **Effort versus utility / balance:** 32% more blood for double duration is efficient. Mastery I and another enzyme are modest gates; monitor elite control uptime.
- **Casting and visual design:** Keep tendrils, with slower recession or a denser knot to distinguish duration.

### Chain Blood Binding

`chain_blood_binding` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Binding** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **225 mL**; **5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_chain_blood_binding.json): 1 × Foul Paste; Animus × 3, Ductilis × 1 (**4 enzymes**); **175 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,175 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Binds the first target and up to three additional nearby targets.
- **Effort versus utility / balance:** 225 mL for four roots is strong. Mastery II and five seconds of shared cooldown distinguish it from the baseline.
- **Casting and visual design:** Briefly connect targets with a visible tendon during each jump.

### Blood Lattice

`blood_lattice` · **Established; strong control** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Binding** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **275 mL**; **7 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_lattice.json): 1 × Foul Paste; Animus × 5, Ductilis × 1 (**6 enzymes**); **425 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **4,625 mL + 14 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Roots the initial target plus up to seven nearby entities for eight seconds.
- **Effort versus utility / balance:** 275 mL is low for eight-target control; mastery IV and seven-second shared cooldown help. Compare with late-game cages.
- **Casting and visual design:** Connect existing bound-target tendrils into an actual lattice with a readable footprint.

### Hematic Rebuke

`hematic_rebuke` · **Functional; targeting review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/HematicCommandManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Animus alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Ductilis.
- **Operation:** Quick. **225 mL**; **5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_rebuke.json): 1 × Spider Eye; Animus × 1, Ductilis × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Makes an eligible blood-bearing, non-boss mob with at most 80 maximum HP flee for eight seconds; aimed range 24.
- **Effort versus utility / balance:** 225 mL is plausible disengagement. The ray does not itself stop at walls, and rejected-target paths can occur after payment.
- **Casting and visual design:** Give the target a command sigil and persistent fleeing mark.

### Hematic Impressment

`hematic_impressment` · **Established; edge-case testing needed** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/HematicCommandManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path; know **Hematic Rebuke** and reach shared mastery **III** (85 cumulative credits). Vascular section: **Head**; secondary tendency: Ductilis.
- **Operation:** Quick. **500 mL**; **10 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_impressment.json): 1 × Lead; Animus × 2, Ductilis × 1 (**3 enzymes**); **250 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,650 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Temporarily commandeers one eligible mob for roughly 15–30 seconds; it follows/fights for the player with boss/health restrictions.
- **Effort versus utility / balance:** 500 mL and mastery III buy a meaningful upgrade. Value depends on available mobs; test ownership expiry and friendly-damage protections in multiplayer.
- **Casting and visual design:** Show a control tether, owner identity and remaining possession state instead of relying on witch/spore cues.

### Summon Avatar

`summon_avatar` · **Established; strong presentation** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonAvatarManip.java)

- **Unlock:** Degree **III** (Summa); **50 Animus alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Passive. **75 mL initially and per second** while manifested; toggleable passive.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_summon_avatar.json): 1 × Nether Star; Animus × 1, Flammeus × 1, Ductilis × 1, Lux × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**8 enzymes**); **400 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **5,800 mL + 18 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Toggleable manifestation gives 25% damage reduction; pays initially and each second.
- **Effort versus utility / balance:** Nether Star plus all eight enzymes is a major Degree-III commitment. Upkeep and alignment 50 move its practical availability later than rank alone.
- **Casting and visual design:** Custom model/layer, first-person support and twenty-tick emergence/dismissal exist. Preserve them; check baseline cast audio repeated during upkeep.

### Summon Avatar Arms

`summon_avatar_arms` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonAvatarManip.java)

- **Unlock:** Degree **III** (Summa); **50 Animus alignment** to cast through the normal path; know **Summon Avatar** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Body**.
- **Operation:** Passive. **125 mL initially and per second** while manifested; toggleable passive.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_summon_avatar_arms.json): 1 × Nether Star; Animus × 2, Flammeus × 1, Ductilis × 1, Lux × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**9 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **6,450 mL + 20 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Retains 25% reduction and adds +4 attack damage, knockback resistance and block/entity reach.
- **Effort versus utility / balance:** 125 mL/s is useful combat utility. Another Nether Star is costly for an optional intermediate stage.
- **Casting and visual design:** Existing limbs communicate the upgrade. Test swing/reach contact synchronization.

### Summon Avatar Armor

`summon_avatar_armor` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonAvatarManip.java)

- **Unlock:** Degree **III** (Summa); **50 Animus alignment** to cast through the normal path; know **Summon Avatar** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Body**.
- **Operation:** Passive. **175 mL initially and per second** while manifested; toggleable passive.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_summon_avatar_armor.json): 1 × Nether Star; Animus × 3, Flammeus × 1, Ductilis × 1, Lux × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**10 enzymes**); **500 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **7,100 mL + 22 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Raises reduction to 45% while keeping the arms' bonuses.
- **Effort versus utility / balance:** 175 mL/s supports a tank role. Separate Nether Stars make optional stage collection expensive; all intermediate forms are not prerequisites.
- **Casting and visual design:** Authored armor growth is appropriate. Improve damage absorption/hit response.

### Summon Avatar Legs

`summon_avatar_legs` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonAvatarManip.java)

- **Unlock:** Degree **III** (Summa); **50 Animus alignment** to cast through the normal path; know **Summon Avatar** and reach shared mastery **III** (85 cumulative credits). Vascular section: **Body**.
- **Operation:** Passive. **250 mL initially and per second** while manifested; toggleable passive.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_summon_avatar_legs.json): 1 × Nether Star; Animus × 4, Flammeus × 1, Ductilis × 1, Lux × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**11 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **7,800 mL + 24 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Keeps 45% reduction and arm benefits, adding movement, jump and step-height bonuses.
- **Effort versus utility / balance:** 250 mL/s is a substantial mobility premium. Real terrain testing should establish whether it beats cheaper armor/arms for practical use.
- **Casting and visual design:** Improve footfalls, landing weight and movement synchronization on the existing body stage.

### Summon Avatar Complete

`summon_avatar_complete` · **Established; powerful and expensive** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonAvatarManip.java)

- **Unlock:** Degree **III** (Summa); **50 Animus alignment** to cast through the normal path; know **Summon Avatar** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Body**.
- **Operation:** Passive. **400 mL initially and per second** while manifested; toggleable passive.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_summon_avatar_complete.json): 1 × Nether Star; Animus × 5, Flammeus × 1, Ductilis × 1, Lux × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**12 enzymes**); **750 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **8,550 mL + 26 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** 65% reduction, +8 attack damage, strong knockback resistance, +35% movement/jump, increased step/reach and doubled scale.
- **Effort versus utility / balance:** 400 mL/s is 8% of the base reservoir each second. Mastery IV requires 185 credits, approximately 15m25s of sustained credited manifestation. Recipe/upkeep support a true family capstone.
- **Casting and visual design:** One of the strongest foundations. Check camera, clipping, first-person reach and transitions rather than rebuilding it.

### Blood Needle

`blood_needle` · **Established; balance review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Ferric.
- **Operation:** Charged. **100 mL** at full charge; **0.5 s shared cooldown**; **1 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_needle.json): 1 × Vivianite Cluster; Ductilis × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **moderate: mod exploration/combat**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Full charge releases roughly 10–20 needles with variable speed/spread and the long amplifier-2 Blood Loss.
- **Effort versus utility / balance:** 100 mL is generous. Collision and immunity frames determine actual volley damage; do not count every needle as a full hit. Vivianite adds exploration effort.
- **Casting and visual design:** Custom needles fit. Gather visible needles during charging and clarify release direction.

### Blood Needle Fan

`blood_needle_fan` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Needle** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Head**; secondary tendency: Ferric.
- **Operation:** Charged. **140 mL** at full charge; **1 s shared cooldown**; **1 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_needle_fan.json): 1 × Vivianite Cluster; Animus × 1, Ductilis × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: mod exploration/combat**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Widens the volley to as many as 24 needles.
- **Effort versus utility / balance:** 140 mL and mastery I fairly buy coverage; spreading can reduce single-target value.
- **Casting and visual design:** Form a visible fan before release instead of relying on projectile count alone.

### Blood Needle Lance

`blood_needle_lance` · **Functional; charge-cost defect** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path; know **Blood Needle** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Head**; secondary tendency: Ferric.
- **Operation:** Charged. **150 mL** at full charge; **1.25 s shared cooldown**; **1.5 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_needle_lance.json): 1 × Vivianite Cluster; Animus × 2, Ductilis × 1 (**3 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **moderate: mod exploration/combat**. Lantern-route reference: **2,550 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Fires exactly three fast needles with piercing level 3, independent of charge fraction.
- **Effort versus utility / balance:** One-tick release costs about 5 mL instead of 150 while retaining the lance. Fix cost/output scaling before tuning piercing.
- **Casting and visual design:** Converge the needles into a lance silhouette during charging.

### Blood Cloud

`blood_cloud` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodCloudManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Mortem.
- **Operation:** Quick. **300 mL**; **2 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_cloud.json): 1 × Spore Sac; Congeatio × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Radius-1.25 cloud lasts five seconds. Damage searches a tall column beneath it; repeated attempts still face immunity frames.
- **Effort versus utility / balance:** 300 mL is plausible area denial. Its one-enzyme recipe is inexpensive for Degree III; evaluate actual hits, not attempted tick damage.
- **Casting and visual design:** Rendered cloud exists. Show the damaging footprint and vertical reach.

### Expansive Blood Cloud

`expansive_blood_cloud` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodCloudManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path; know **Blood Cloud** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Head**; secondary tendency: Mortem.
- **Operation:** Quick. **375 mL**; **3 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_expansive_blood_cloud.json): 1 × Spore Sac; Animus × 1, Congeatio × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Radius increases to three and lifetime to six seconds.
- **Effort versus utility / balance:** 25% more blood buys a major coverage increase. Likely the preferred stationary-crowd form after mastery I.
- **Casting and visual design:** Scale a ground footprint with the real radius.

### Pursuing Blood Cloud

`pursuing_blood_cloud` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodCloudManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path; know **Blood Cloud** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Head**; secondary tendency: Mortem.
- **Operation:** Quick. **425 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_pursuing_blood_cloud.json): 1 × Spore Sac; Animus × 2, Congeatio × 1 (**3 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,550 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Radius-two cloud lasts eight seconds and slowly tracks its target horizontally.
- **Effort versus utility / balance:** 425 mL buys reliability, with slow tracking leaving counterplay. Mastery II is sensible.
- **Casting and visual design:** Use directed flow or a thin connection to identify the pursued target.

### Sanguine Tempest

`sanguine_tempest` · **Established; premium payoff present** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodCloudManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path; know **Blood Cloud** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Head**; secondary tendency: Mortem.
- **Operation:** Quick. **600 mL**; **6 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_tempest.json): 1 × Spore Sac; Animus × 4, Congeatio × 1 (**5 enzymes**); **400 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **4,000 mL + 12 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Radius-three cloud lasts eight seconds and adds periodic lightning. Lightning searches near the cloud, unlike its downward damage column.
- **Effort versus utility / balance:** 600 mL and mastery IV buy a genuine added mechanic. Verify tall-space encounters where lightning and rain can affect different target sets.
- **Casting and visual design:** Cloud plus lightning already communicates a storm. Clarify strike reach and formation escalation.

### Blood Rush

`blood_rush` · **Functional; severe balance outlier** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodRushManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Flammeus.
- **Operation:** Quick. **100 mL**; **3 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_rush.json): 1 × Blood Crystal Shard; Animus × 1, Flammeus × 1, Ductilis × 1, Mortem × 1, Congeatio × 1, Ferric × 1, Tenebris × 1 (**7 enzymes**); **350 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **5,150 mL + 16 min**, excluding catalyst/infrastructure work. [Scarred alternative](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_rush_scarred.json): 1 × Scar Heart; Animus × 1, Lux × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Lantern reference **1,900 mL / 6 min**. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Horizontal dash plus 12.5 seconds of Blood Rush. The effect heals 0.5 HP every tick, up to 10 HP/s while injured.
- **Effort versus utility / balance:** 100 mL can supply up to 125 HP over the duration under sustained injury. Seven-enzyme weaving is unusually costly, but crude memories bypass that burden. Correct healing cadence before recipe tuning.
- **Casting and visual design:** Use a directional dash streak and restrained healing pulse rather than capstone spectacle.

### Blood Aneurysm

`blood_aneurysm` · **Functional; targeting review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodAneurysmManip.java)

- **Unlock:** Degree **III** (Summa); **25 Animus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Mortem.
- **Operation:** Charged. **400 mL** at full charge; **2 s shared cooldown**; **2 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_aneurysm.json): 1 × Bone; Mortem × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Full charge deals eight damage, launches the nearest living target within ten blocks and adds a smaller nearby splash.
- **Effort versus utility / balance:** Cheap bone/one-enzyme memory buys substantial burst. Missing ally/visibility filtering makes it unreliable; resolve targeting first.
- **Casting and visual design:** Red dust/explosion lacks the anatomy. Trace vessels on the selected target, then rupture.

### Vital Effusion

`vital_effusion` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/VitalEffusionManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Animus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Ductilis.
- **Operation:** Quick. **350 mL**; **3 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_vital_effusion.json): 1 × Bleeding Bulb; Animus × 1 (**1 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,300 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Applies one to three growth attempts to eligible blocks in a targeted 5×3×5 patch within twelve blocks.
- **Effort versus utility / balance:** 350 mL is expensive per cast but useful for bulk renewable farming. Compare actual growth against bonemeal; the low recipe effort is appropriate.
- **Casting and visual design:** Replace generic happy-villager emphasis with branching blood-fed roots over the selected patch.

### Crimson Coronation

`crimson_coronation` · **Functional; weak premium value** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/CrimsonCoronationManip.java)

- **Unlock:** Degree **VI** (Perfectus); **70 Animus alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Charged. **1000 mL** at full charge; **6 s shared cooldown**; **4 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_crimson_coronation.json): 1 × Nether Star; Animus × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **very high: Nether Star**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Full charge stocks up to eight reactive needle shots against attackers over twenty seconds. Recasting replaces the remaining pool.
- **Effort versus utility / balance:** 1000 mL, Degree VI, alignment 70 and a Nether Star compare poorly with Halo's five shots for 220. Retaliation is a niche, not enough value by itself.
- **Casting and visual design:** Replace the anonymous dust crown with visible stored blood spears, consumed on retaliation, and a strong coronation buildup.

### Sovereign Instinct

`sovereign_instinct` · **Functional; shallow control state** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SovereignInstinctManip.java)

- **Unlock:** Degree **V** (Magister); **60 Animus alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Passive. **450 mL per eligible reactive trigger**; thirty-second internal cooldown (registry CD is zero).
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sovereign_instinct.json): 1 × Golden Helmet; Animus × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Reactive disruption redirects the attacker/nearby hostile blood mobs against each other; bosses receive weaker disruption. Internal cooldown thirty seconds.
- **Effort versus utility / balance:** 450 mL per trigger is plausible, but ordinary AI can reacquire targets without durable compulsion. Reliability matters more than a stronger number.
- **Casting and visual design:** Add domination pulse, target-to-target links and ready/spent indication.

## Ductilis

9 active registrations.

### Deadly Gaze

`deadly_gaze` · **Functional; targeting/FX transport review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/DeadlyGazeManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Ductilis alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Tenebris.
- **Operation:** Charged. **100 mL** at full charge; **1 s shared cooldown**; **2 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_deadly_gaze.json): 1 × Scrying Dish; Lux × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **moderate: three Hematic Iron Scrap upstream**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Charge extends aimed range roughly 12–32 blocks and lift strength. Full charge queues a six-damage slam after twelve ticks.
- **Effort versus utility / balance:** 100 mL is excellent control value. The ray lacks block occlusion; verify proxy-based lightning transmission on a dedicated server.
- **Casting and visual design:** Existing claws/lightning need a selected-target eye cue and clear lift-to-slam progression.

### Activation Potential

`activation_potential` · **Functional; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ActivationPotentialManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Ductilis alignment** to cast through the normal path; know **Synaptic Jolt** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Body**.
- **Operation:** Charged. **200 mL** at full charge; **1.5 s shared cooldown**; **1.5 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_activation_potential.json): 1 × Smouldering Ash Trail; Flammeus × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged self-centered strike deals up to five damage and triggers conductive interactions.
- **Effort versus utility / balance:** 200 mL and mastery II are fair, but it affects other living entities without ally filtering. Flammeus-only weaving is a thematic inconsistency to check.
- **Casting and visual design:** Show a charging radius and electrical discharge ring using existing lightning.

### Synaptic Jolt

`synaptic_jolt` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/SynapticJoltManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Ductilis alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Quick. **150 mL**; **1.25 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_synaptic_jolt.json): 1 × Redstone; Ductilis × 1 (**1 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,300 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Seven-block aimed strike deals three damage, slows/weakens and interrupts navigation; supports conductive interactions with ally/visibility checks.
- **Effort versus utility / balance:** 150 mL is fair reliable close disruption. Accessible redstone makes it a useful early entry.
- **Casting and visual design:** Custom lightning fits; a contact snap and nervous-system flicker are enough.

### Conductive Mark

`conductive_mark` · **Established combo tool** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ConductiveMarkManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Ductilis alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Ferric.
- **Operation:** Quick. **225 mL**; **2.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_conductive_mark.json): 1 × Lightning Rod; Ductilis × 1, Ferric × 1 (**2 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,950 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Eight-second mark at fourteen-block range; eligible Ductilis/Lux/Ferric hits arc two damage to up to three nearby targets, with a fifteen-tick interval.
- **Effort versus utility / balance:** 225 mL invests in follow-up attacks rather than instant damage. Lightning-rod catalyst fits the utility.
- **Casting and visual design:** Keep the mark visible for its full duration and originate arcs from it.

### Sanguine Ward

`sanguine_ward` · **Functional; severe cost/restart outlier** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/SanguineWardManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Ductilis alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Ferric.
- **Operation:** Continuous. **10 mL initially and per second**; 1 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_ward.json): 1 × Chitinous Husk; Tenebris × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channel starts a six-point shield, then adds two per pulse up to eight; shield briefly persists after refresh stops.
- **Effort versus utility / balance:** 10 mL/s undercuts Ironhearted. Channel START bypasses shared cooldown and grants initial shield, making repeated restart an exploit test priority.
- **Casting and visual design:** Custom lightning shielding exists. Show remaining strength, breakage and the difference between startup and refresh.

### Hemolymphal Pulse

`hemolymphal_pulse` · **Functional; weak cost/cooldown value** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/HemolymphalPulseManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Ductilis alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Animus.
- **Operation:** Quick. **400 mL**; **15 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hemolymphal_pulse.json): 1 × Vascular Status Gauge; Ductilis × 1 (**1 enzymes**); **125 mL weaving**; plus one blank. Catalyst burden: **moderate: crafted mod instrument**. Lantern-route reference: **1,325 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Reveals blood-bearing entities in a 24-block search volume for fifteen seconds; wounded targets at/below half HP receive conductive marks.
- **Effort versus utility / balance:** 400 mL plus fifteen seconds of shared cooldown is punitive for a scan. A crafted gauge adds effort; reduce lockout or improve tactical information.
- **Casting and visual design:** Spore rings/lightning need a clean scan shell and distinct wounded markers.

### Synaptic Storm

`synaptic_storm` · **Established; credible capstone** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/SynapticStormManip.java)

- **Unlock:** Degree **VI** (Perfectus); **65 Ductilis alignment** to cast through the normal path; know **Synaptic Jolt** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Head**.
- **Operation:** Charged. **900 mL** at full charge; **5 s shared cooldown**; **3 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_synaptic_storm.json): 1 × Lightning Rod; Ductilis × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged strike hits up to eight non-allied nearby entities for 2–8 damage with substantial slowing, reduced against bosses.
- **Effort versus utility / balance:** 900 mL and mastery IV buy broad disruption. Lightning-rod acquisition is much easier than some weaker capstones' catalysts.
- **Casting and visual design:** Build an acquisition lattice across targets, then synchronize the existing lightning discharge.

### Living Circuit

`living_circuit` · **Functional; recipient-filter defect** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/LivingCircuitManip.java)

- **Unlock:** Degree **V** (Magister); **55 Ductilis alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Continuous. **180 mL initially and per second**; 4 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_living_circuit.json): 1 × Redstone Lamp; Ductilis × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channels Speed II/Haste II to up to three nearby other players and arms conductive follow-up attacks; excludes the caster.
- **Effort versus utility / balance:** 180 mL/s can aid a party, but selection does not require alliance and can buff opponents. Solo value is minimal.
- **Casting and visual design:** Give existing arcs durable links/recipient indicators so expenditure is understandable.

### Thread Ripper

`thread_ripper` · **Established specialist** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ThreadRipperManip.java)

- **Unlock:** Degree **III** (Summa); **0 Ductilis alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Tenebris.
- **Operation:** Charged. **35 mL** at full charge; **12 s shared cooldown**; **1.5 s full charge** required before any cast.
- **Acquisition:** direct reward from the Circus **Liberation** finale. Encounter/route work replaces crafting materials; no woven memory recipe.
- **Implemented gameplay:** Full charge severs eligible enemy summons: high-health summons lose 25% maximum HP and are silenced; low-health summons can be discarded. Includes ownership/boss/trial protections.
- **Effort versus utility / balance:** 35 mL is intentionally cheap for a narrow counter, but twelve-second shared cooldown matters. Earned via Circus Liberation; ordinary mobs offer little utility. Test direct reward-grant success at the finale.
- **Casting and visual design:** Crimson spores/chain audio need an actual visible control thread being sliced.

## Ferric

6 active registrations.

### Sanguine Mending

`sanguine_mending` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/SanguineMendingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Ferric alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Continuous. **150 mL initially and per second**; 1.5 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_mending.json): 1 × Sanguine Salve; Ferric × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **moderate: crafted salve upstream**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Each paid pulse repairs fifty durability; undamaged/invalid item checks precede payment.
- **Effort versus utility / balance:** 150 mL means three mL per durability. Renewable repair is valuable and the salve recipe is reasonable.
- **Casting and visual design:** Dust/anvil audio work; trace closing fractures on the held item.

### Vascular Dowsing

`vascular_dowsing` · **Functional; readability/performance risk** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/VascularDowsingManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Ferric alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Lux.
- **Operation:** Continuous. **500 mL initially and per second**; 20 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_vascular_dowsing.json): 1 × Compass; Ferric × 1 (**1 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,350 mL + 4 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Scans 17×17×17 blocks every second for eight vanilla ore-tag families, emitting particles at matching blocks.
- **Effort versus utility / balance:** 500 mL/s and twenty-second shared cooldown are excessive if buried markers are invisible. Forced particle delivery is not proof of visibility through stone; coverage excludes many other resources.
- **Casting and visual design:** Highest-priority utility FX check: verify underground visibility, then use intentional silhouettes or directional scan markers.

### Iron Retort

`iron_retort` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/IronRetortManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Ferric alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Ductilis.
- **Operation:** Quick. **250 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_iron_retort.json): 1 × Iron Ingot; Ductilis × 1, Ferric × 1 (**2 enzymes**); **175 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,975 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Three-second guard halves one qualifying melee hit and retaliates for four damage.
- **Effort versus utility / balance:** 250 mL fits a timed defensive response, but Ward undermines its economy. Iron-ingot catalyst is suitable.
- **Casting and visual design:** Maintain an iron brace throughout the window, then show the directional retaliation.

### Ironhearted

`ironhearted` · **Established; poor relative economy** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/IronheartedManip.java)

- **Unlock:** Degree **V** (Magister); **45 Ferric alignment** to cast through the normal path. Vascular section: **Heart**; secondary tendency: Animus.
- **Operation:** Charged. **300 mL** at full charge; **2 s shared cooldown**; **2 s full charge** required before any cast.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_ironhearted.json): 1 × Iron Block; Animus × 1, Ferric × 2 (**3 enzymes**); **300 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,700 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Full two-second charge adds two HP to an iron buffer, capped at ten, with ten-minute expiry refresh.
- **Effort versus utility / balance:** Five casts cost 1500 mL plus charging for five hearts. Degree-V value is weak beside Ward/Lumen Suture, though long pre-combat storage is a real advantage.
- **Casting and visual design:** Custom HUD heart formation/cracking already exists. Improve external chest-forging cues, not the already-present internal meter.

### Sanguine Magnetism

`sanguine_magnetism` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/SanguineMagnetismManip.java)

- **Unlock:** Degree **III** (Summa); **20 Ferric alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Ductilis.
- **Operation:** Quick. **450 mL**; **7 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_magnetism.json): 1 × Lodestone; Ductilis × 1, Ferric × 2 (**3 enzymes**); **250 mL weaving**; plus one blank. Catalyst burden: **high: expensive constructed catalyst**. Lantern-route reference: **2,650 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Places a six-second iron pillar that pulls items/eligible hostiles within eight blocks and pins close targets.
- **Effort versus utility / balance:** 450 mL and lodestone are a meaningful investment. Six seconds is short for that catalyst, but grouping plus collection is genuinely useful.
- **Casting and visual design:** Authored pillar is a good focus. Add radial flow and a readable expiry.

### Iron Choir

`iron_choir` · **Functional; weak uptime value** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/IronChoirManip.java)

- **Unlock:** Degree **V** (Magister); **60 Ferric alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Continuous. **250 mL initially and per second**; 5 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_iron_choir.json): 1 × Iron Block; Ferric × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channel gives Resistance I and removes at most one nearby non-owned projectile per paid pulse, with retaliation against its shooter.
- **Effort versus utility / balance:** 250 mL/s buys intermittent interception, not an uninterrupted barrier. Degree V and five-second stop cooldown compare poorly with Ward.
- **Casting and visual design:** Use orbiting ferric plates with visible interception/replenishment instead of relying on crit particles.

## Lux

5 active registrations.

### Hematic Flare

`hematic_flare` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/HematicFlareManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Lux alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Flammeus.
- **Operation:** Quick. **125 mL**; **1.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_flare.json): 1 × Glow Ink Sac; Flammeus × 1, Lux × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Aimed strike deals three damage, five against invisible targets; removes invisibility and applies nine seconds of glowing.
- **Effort versus utility / balance:** 125 mL is good anti-stealth utility with baseline damage. Glow ink plus two enzymes is proportionate.
- **Casting and visual design:** Custom thin ray/impact already fit. Keep revelation clear for its duration.

### Prismatic Reproof

`prismatic_reproof` · **Functional; targeting review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/PrismaticReproofManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Lux alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Ferric.
- **Operation:** Quick. **325 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_prismatic_reproof.json): 1 × Amethyst Shard; Lux × 1, Ferric × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Broad fourteen-block cone deals two damage, four against glowing targets, with blindness and weakness.
- **Effort versus utility / balance:** 325 mL mainly purchases debuffs. Missing ally/visibility checks reduce reliability in teams and enclosed rooms.
- **Casting and visual design:** Organize existing glow into a refracted fan/shard pattern outlining the cone.

### Hematic Beacon

`hematic_beacon` · **Functional; regeneration timing concern** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/HematicBeaconManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Lux alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Animus.
- **Operation:** Quick. **350 mL**; **8 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hematic_beacon.json): 1 × Glowstone Dust; Ductilis × 1, Lux × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Eight-second rally area refreshes allied Regeneration I/Resistance I and reveals nearby enemies once per second.
- **Effort versus utility / balance:** 350 mL and glowstone are fair support costs. Verify whether repeatedly refreshed short regeneration durations actually reach vanilla healing ticks before counting healing value.
- **Casting and visual design:** Initial particles need a persistent central shaft and timed ground circle.

### Lumen Suture

`lumen_suture` · **Functional; recipient-filter review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/LumenSutureManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Lux alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ductilis.
- **Operation:** Quick. **250 mL**; **6 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_lumen_suture.json): 1 × Glistering Melon Slice; Animus × 1, Lux × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Heals two HP, grants Absorption II for twenty seconds and clears blindness/wither on the nearest injured player within twelve blocks, falling back to self.
- **Effort versus utility / balance:** 250 mL buys eight absorption HP plus healing/cleansing, strong at Degree I. Selection can include enemy players.
- **Casting and visual design:** Custom suture tendril/glow fit. Identify the recipient before payment and show a lingering protective seam.

### White Verdict

`white_verdict` · **Functional; major presentation gap** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/WhiteVerdictManip.java)

- **Unlock:** Degree **VI** (Perfectus); **65 Lux alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Charged. **800 mL** at full charge; **5 s shared cooldown**; **3 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_white_verdict.json): 1 × Beacon; Lux × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **very high: entire Beacon**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged beam grows roughly 8–24 blocks and deals 2–10 damage, increased against invisible targets; strips invisibility/reveals survivors.
- **Effort versus utility / balance:** 800 mL and an entire beacon demand premium payoff. The segment hit search does not clip against walls; fix occlusion before damage increases.
- **Casting and visual design:** Endpoint END_ROD particles do not draw the damaging beam. Add a visible core, outer rays, charge convergence and end impact. Top capstone FX priority.

## Flammeus

11 active registrations.

### Crimson Flame Conjuration

`crimson_flame_conjuration` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/CrimsonFlameConjurationManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Flammeus alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Quick. **150 mL**; **0.75 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_crimson_flame_conjuration.json): 1 × Blaze Powder; Animus × 1, Flammeus × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Places custom crimson flame at an aimed location within sixteen blocks.
- **Effort versus utility / balance:** 150 mL is a meaningful price for repeated environmental fire placement. Ignition is generally the better immediate combat comparison.
- **Casting and visual design:** Custom flame/glow fit; a traveling ignition filament would connect hand to placement.

### Pyretic Forge

`pyretic_forge` · **Functional; failed-use/output edge cases** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/PyreticForgeManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Flammeus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Ferric.
- **Operation:** Quick. **350 mL**; **1.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_pyretic_forge.json): 1 × Fervent Husk; Flammeus × 1, Ferric × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: Fervent Chitinite drop**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Instantly smelts up to eight held items, increased by Crimson Mastery, using furnace recipes.
- **Effort versus utility / balance:** 350 mL can justify portable batch smelting. Invalid inputs can still consume the cast, and result-count handling assumes one output per input. Check modded multi-output recipes.
- **Casting and visual design:** Heat the held stack and visibly cool the output; reject invalid input before starting visual buildup.

### Sanguine Ignition

`sanguine_ignition` · **Functional; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/SanguineIgnitionManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Flammeus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Animus.
- **Operation:** Quick. **125 mL**; **1.25 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_ignition.json): 1 × Fire Charge; Flammeus × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. [Scarred alternative](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_sanguine_ignition_scarred.json): 1 × Scar Phoenix; Animus × 1, Flammeus × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Lantern reference **1,900 mL / 6 min**. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Immediate nearby burst deals two damage and applies four seconds of custom crimson fire.
- **Effort versus utility / balance:** 125 mL is efficient early AoE. Other living entities are affected without ally filtering. Cheap weaving and crude memories both ease access.
- **Casting and visual design:** Turn colored glow into a brief expanding flame ring that matches the damage radius.

### Vitric Combustion

`vitric_combustion` · **Functional; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/VitricCombustionManip.java)

- **Unlock:** Degree **III** (Summa); **25 Flammeus alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Charged. **500 mL** at full charge; **3 s shared cooldown**; **3 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_vitric_combustion.json): 1 × Fervent Husk; Animus × 1, Flammeus × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: Fervent Chitinite drop**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Three-second charge produces an aimed explosion up to roughly radius four, eight damage, eight seconds of fire and knockback.
- **Effort versus utility / balance:** 500 mL is plausible Degree-III burst. Cheap unscaled Mortar releases undermine it; Fervent Husk adds exploration effort. Blast lacks ally filtering.
- **Casting and visual design:** Orange dust/explosion is generic. Form incandescent glass at impact and visibly shatter it.

### Cauterizing Rebuke

`cauterizing_rebuke` · **Functional; narrow/misleading name** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/CauterizingRebukeManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Flammeus alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Animus.
- **Operation:** Quick. **275 mL**; **4.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_cauterizing_rebuke.json): 1 × Magma Cream; Animus × 1, Flammeus × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Self-cleanses poison, wither and Blood Loss, paying two HP per removed effect without reducing below one; successful cleansing grants brief Absorption I. It does not attack nearby enemies.
- **Effort versus utility / balance:** 275 mL plus HP is expensive beside Lumen Suture, but poison/Blood Loss removal is distinct. Name and outward flames imply an attack that is absent.
- **Casting and visual design:** Show closing cauterized wounds and expelled corruption instead of an offensive-looking flame cloud.

### Scalding Updraft

`scalding_updraft` · **Established; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/ScaldingUpdraftManip.java)

- **Unlock:** Degree **0** (Humilis); **5 Flammeus alignment** to cast through the normal path. Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **225 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_scalding_updraft.json): 1 × Fire Charge; Flammeus × 1, Ductilis × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Upward/forward launch with five seconds of slow falling and a small nearby damage/fire burst.
- **Effort versus utility / balance:** 225 mL buys useful traversal and escape. Fire-charge catalyst/two enzymes fit; nearby allies can suffer the offensive portion.
- **Casting and visual design:** Use a directional foot jet and a clear transition into slow descent around existing glow.

### Soaring Updraft

`soaring_updraft` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/ScaldingUpdraftManip.java)

- **Unlock:** Degree **0** (Humilis); **5 Flammeus alignment** to cast through the normal path; know **Scalding Updraft** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **300 mL**; **5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_soaring_updraft.json): 1 × Fire Charge; Flammeus × 2, Ductilis × 1 (**3 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,550 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Stronger upward/forward launch with six seconds of slow falling.
- **Effort versus utility / balance:** 300 mL and mastery I are reasonable traversal upgrades; test actual terrain reach.
- **Casting and visual design:** Extend the jet and airborne trail to communicate height rather than increasing random particles.

### Suspended Updraft

`suspended_updraft` · **Established** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/ScaldingUpdraftManip.java)

- **Unlock:** Degree **0** (Humilis); **5 Flammeus alignment** to cast through the normal path; know **Scalding Updraft** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **350 mL**; **6 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_suspended_updraft.json): 1 × Fire Charge; Flammeus × 3, Ductilis × 1 (**4 enzymes**); **200 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,200 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Brief levitation phase plus eight seconds of slow falling.
- **Effort versus utility / balance:** 350 mL and mastery II purchase airtime/positioning rather than just height. Preserve that selectable distinction.
- **Casting and visual design:** Add a small hover vortex that collapses into a descent trail.

### Expulsive Updraft

`expulsive_updraft` · **Established; strong escape** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/ScaldingUpdraftManip.java)

- **Unlock:** Degree **0** (Humilis); **5 Flammeus alignment** to cast through the normal path; know **Scalding Updraft** and reach shared mastery **III** (85 cumulative credits). Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **450 mL**; **7 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_expulsive_updraft.json): 1 × Fire Charge; Flammeus × 4, Ductilis × 1 (**5 enzymes**); **300 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,900 mL + 12 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Adds outward/upward enemy displacement to the launch package.
- **Effort versus utility / balance:** 450 mL and mastery III fit a crowd escape. Ally safety in the fire portion remains a shared issue.
- **Casting and visual design:** Show an outward ground ring before the upward jet to explain knockback.

### Furnace Veins

`furnace_veins` · **Functional; poor relative efficiency** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/FurnaceVeinsManip.java)

- **Unlock:** Degree **V** (Magister); **55 Flammeus alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Continuous. **250 mL initially and per second**; 5 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_furnace_veins.json): 1 × Blaze Powder; Flammeus × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channel burns enemies for two damage per pulse, extinguishes/unfreezes allies and removes nearby frosted ice.
- **Effort versus utility / balance:** 250 mL/s is expensive at Degree V. Thawing is a niche, but Ignition/Living Torch are cheaper offense. Clarify support versus damage purpose.
- **Casting and visual design:** Replace flame-cloud emphasis with glowing body veins and a controlled heat boundary.

### Phoenix Debt

`phoenix_debt` · **Established reactive capstone** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/flammeus/PhoenixDebtManip.java)

- **Unlock:** Degree **VI** (Perfectus); **75 Flammeus alignment** to cast through the normal path. Vascular section: **Heart**.
- **Operation:** Passive. **2000 mL per eligible lifesaving trigger**; five-minute internal cooldown plus last-rite constraints (registry CD is zero).
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_phoenix_debt.json): 1 × Totem Of Undying; Flammeus × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **high: survival/encounter reward**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** On qualifying lethal damage, preserves one HP, cleanses harmful effects, grants fire resistance and bursts damage/fire nearby. Five-minute internal cooldown plus shared last-rite constraints.
- **Effort versus utility / balance:** 2000 mL requires more than 40% of base blood remaining under normal exact-cost rules. Totem catalyst/Degree VI fit a lifesaver, provided readiness is dependable.
- **Casting and visual design:** Flame burst needs a brief phoenix-wing silhouette and distinct armed, rebirth and spent states.

## Congeatio

6 active registrations.

### Glacial Grasp

`glacial_grasp` · **Established; situational combat** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/GlacialGraspManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Congeatio alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Tenebris.
- **Operation:** Quick. **125 mL**; **1 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_glacial_grasp.json): 1 × Blue Ice; Congeatio × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **moderate: ice acquisition or compaction**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Creates temporary frost disk/path and freezes/slows wet targets in the aimed area.
- **Effort versus utility / balance:** 125 mL fits terrain utility; combat depends on wetness. Blue Ice is heavy early crafting effort, partly mitigated by crude memories.
- **Casting and visual design:** Existing tendrils fit. Show ice growth and wet-target eligibility clearly.

### Cryogenic Pulse

`cryogenic_pulse` · **Functional; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/CryogenicPulseManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Congeatio alignment** to cast through the normal path; know **Glacial Grasp** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Body**; secondary tendency: Flammeus.
- **Operation:** Quick. **150 mL**; **1.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_cryogenic_pulse.json): 1 × Blue Ice; Congeatio × 2 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: ice acquisition or compaction**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Close-area three-damage pulse with strong slowing and mining fatigue.
- **Effort versus utility / balance:** 150 mL and mastery I give efficient control. Other allies are affected too; fix target policy before treating that as intentional balance.
- **Casting and visual design:** Use an expanding frost ring and short crystal growth around existing icy glow.

### Glacial Rampart

`glacial_rampart` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/GlacialRampartManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Congeatio alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **350 mL**; **2.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_glacial_rampart.json): 1 × Blue Ice; Ductilis × 1, Congeatio × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: ice acquisition or compaction**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Temporary three-wide, three-high ice wall at range; sneaking creates a bastion. Temporary-block management restores terrain.
- **Effort versus utility / balance:** 350 mL buys meaningful cover. Blue-Ice catalyst fits better here than in a starter wet-target spell; verify complex placement/expiry.
- **Casting and visual design:** Give wall/bastion a visible bottom-to-top growth sequence; bastion presentation is comparatively slight.

### Osseous Bloom

`osseous_bloom` · **Functional; major boss-balance risk** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/OsseousBloomManip.java)

- **Unlock:** Degree **III** (Summa); **25 Congeatio alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Mortem.
- **Operation:** Quick. **600 mL**; **3 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_osseous_bloom.json): 1 × Vivianite Cluster; Ductilis × 1, Congeatio × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: mod exploration/combat**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Area strike deals 25% of current target HP before scaling and strongly slows. No explicit boss damage cap in this action.
- **Effort versus utility / balance:** 600 mL can remove enormous boss HP, while weaving needs only two enzymes/Vivianite. Repeated current-health damage is not a fixed four-cast kill, but overwhelms flat-damage peers. Ally filtering also needs review.
- **Casting and visual design:** Custom tendrils/icy red glow need actual bone or crystal growth explaining the rupture.

### Absolute Stillness

`absolute_stillness` · **Functional; promise exceeds behavior** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/AbsoluteStillnessManip.java)

- **Unlock:** Degree **VI** (Perfectus); **65 Congeatio alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Continuous. **300 mL initially and per second**; 8 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_absolute_stillness.json): 1 × Blue Ice; Congeatio × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **moderate: ice acquisition or compaction**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channel multiplies nearby projectile velocity by 0.45 once per second and freezes/slows/extinguishes targets.
- **Effort versus utility / balance:** 300 mL/s at Degree VI is costly for periodic slowing. Fast projectiles can cross between pulses; eight-second stop cooldown hurts. It is not continuous time stop.
- **Casting and visual design:** Snowflakes need a persistent bounded stasis field with suspended fragments reflecting actual slowdown.

### Rimebound Sentence

`rimebound_sentence` · **Established; weak relative value** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/RimeboundSentenceManip.java)

- **Unlock:** Degree **V** (Magister); **55 Congeatio alignment** to cast through the normal path; know **Glacial Grasp** and reach shared mastery **IV** (185 cumulative credits). Vascular section: **Arms**.
- **Operation:** Charged. **750 mL** at full charge; **6 s shared cooldown**; **3.5 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_rimebound_sentence.json): 1 × Packed Ice; Congeatio × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged strike deals up to eight damage and severe slow; ordinary targets get temporary walls/roof, bosses reduced slow and no cage.
- **Effort versus utility / balance:** 750 mL, 3.5-second charge, Degree V and mastery IV are steep compared with Binding/Pulse. Physical containment is the differentiator.
- **Casting and visual design:** Visibly form the ice sentence around the target and show boss resistance, not only a weaker status.

## Tenebris

8 active registrations.

### Umbral Step

`umbral_step` · **Functional; validation-before-cost issue** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/UmbralStepManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Tenebris alignment** to cast through the normal path. Vascular section: **Legs**; secondary tendency: Ductilis.
- **Operation:** Quick. **300 mL**; **2 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_umbral_step.json): 1 × Ender Eye; Ductilis × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work. [Scarred alternative](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_umbral_step_scarred.json): 1 × Scar Shade; Tenebris × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Lantern reference **1,250 mL / 4 min**.
- **Implemented gameplay:** Teleports toward an aimed dark destination within 24 blocks, checking space/resetting fall distance; covenant/specialized progression relax restrictions.
- **Effort versus utility / balance:** 300 mL is fair mobility, but failed destinations can consume blood. Validate darkness on the final safe destination before cost.
- **Casting and visual design:** Departure/arrival effects exist. Add valid/invalid landing preview so darkness/collision failures are understandable.

### Void Shroud

`void_shroud` · **Established; very efficient** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/VoidShroudManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Tenebris alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Mortem.
- **Operation:** Quick. **100 mL**; **1 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_void_shroud.json): 1 × Ink Sac; Tenebris × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Five seconds of invisibility, Speed II and night vision.
- **Effort versus utility / balance:** 100 mL, cheap ink and crude access make excellent early escape. Penumbral Drift needs its repeated de-aggro to justify far greater upkeep.
- **Casting and visual design:** Purple glow is sufficient; add dissolving silhouette/expiry cues without an excessively conspicuous stealth aura.

### Gloam Laceration

`gloam_laceration` · **Established; status inconsistency** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/GloamLacerationManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Tenebris alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Mortem.
- **Operation:** Quick. **175 mL**; **1.75 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_gloam_laceration.json): 1 × Phantom Membrane; Mortem × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Close aimed strike deals 3.5 damage, six in darkness/invisibility, plus weakness and seven seconds of amplifier-0 Blood Loss.
- **Effort versus utility / balance:** 175 mL supports an ambush role; Phantom Membrane adds effort. Amplifier-0 Blood Loss drains no player blood under the effect formula, unlike its apparent bleed promise.
- **Casting and visual design:** Custom purple claw slash already has identity. Emphasize empowered cuts and a modest wound mark.

### Blood Eclipse

`blood_eclipse` · **Functional; ally/occlusion review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/BloodEclipseManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Tenebris alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Lux.
- **Operation:** Quick. **300 mL**; **2.25 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_eclipse.json): 1 × Fermented Spider Eye; Ductilis × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work. [Scarred alternative](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_eclipse_scarred.json): 1 × Scar Moon; Congeatio × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Lantern reference **1,900 mL / 6 min**.
- **Implemented gameplay:** Forward eighteen-block cone deals three damage with blindness/weakness.
- **Effort versus utility / balance:** 300 mL is plausible group debuff value, but broad targeting lacks ally/wall checks.
- **Casting and visual design:** Existing tendrils/purple cone need clear edges and visible blindness settling on valid targets.

### Black Veil Covenant

`black_veil_covenant` · **Established; strong environmental identity** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/BlackVeilCovenantManip.java)

- **Unlock:** Degree **I** (Mediocritas); **15 Tenebris alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Mortem.
- **Operation:** Quick. **425 mL**; **11 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_black_veil_covenant.json): 1 × Echo Shard; Ductilis × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **high: exploration loot**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Thirty-second dark-synergy zone of roughly eight-block radius with initial nearby blindness/slowing.
- **Effort versus utility / balance:** 425 mL is efficient persistent setup, but Echo Shard is premium effort. Valuable for darkness builds, less useful alone.
- **Casting and visual design:** Authored dome/tendrils/ink already establish the zone. Improve boundary fading and ally/enemy readability.

### Umbral Reversal

`umbral_reversal` · **Established; preview needed** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/UmbralReversalManip.java)

- **Unlock:** Degree **I** (Mediocritas); **15 Tenebris alignment** to cast through the normal path; know **Umbral Step** and reach shared mastery **II** (35 cumulative credits). Vascular section: **Legs**; secondary tendency: Animus.
- **Operation:** Quick. **375 mL**; **5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_umbral_reversal.json): 1 × Ender Pearl; Ferric × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Finds a safe dark point behind the caster, teleports there and blinds enemies near the origin.
- **Effort versus utility / balance:** 375 mL and mastery II buy defensive escape. Constraints need clear feedback to justify cost over forward Step.
- **Casting and visual design:** Show a backward shadow path/destination and brief origin decoy rather than only an origin burst.

### Penumbral Drift

`penumbral_drift` · **Functional; poor relative economy** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/PenumbralDriftManip.java)

- **Unlock:** Degree **V** (Magister); **55 Tenebris alignment** to cast through the normal path. Vascular section: **Legs**.
- **Operation:** Continuous. **175 mL initially and per second**; 3 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_penumbral_drift.json): 1 × Echo Shard; Tenebris × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **high: exploration loot**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Maintains invisibility/Speed II and repeatedly clears nearby mob targeting; attacking breaks the channel.
- **Effort versus utility / balance:** 175 mL/s, Degree V and Echo Shard compare poorly with Void Shroud. Repeated AI disengagement is its unique value.
- **Casting and visual design:** Add subtle dissolution and broken-aggro links; avoid a bright persistent stealth aura.

### Eclipse Well

`eclipse_well` · **Functional; spatial/presentation gap** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/EclipseWellManip.java)

- **Unlock:** Degree **VI** (Perfectus); **65 Tenebris alignment** to cast through the normal path. Vascular section: **Head**.
- **Operation:** Charged. **900 mL** at full charge; **7 s shared cooldown**; **4 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_eclipse_well.json): 1 × Crying Obsidian; Tenebris × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged distant field pulls, blinds/darkens and clears mob targeting for roughly 2–10 seconds; radius scales with charge. No direct damage.
- **Effort versus utility / balance:** 900 mL buys broad control, but forward placement does not block-ray clip. Enclosed fights may disconnect placement from aim; compare against cheaper binding groups.
- **Casting and visual design:** Scattered dust needs a dark central focus, spiraling tendrils and ground boundary.

## Mortem

10 active registrations.

### Hemorrhage

`hemorrhage` · **Functional; weak peer comparison** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/HemorrhageManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Mortem alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Quick. **100 mL**; **1 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hemorrhage.json): 1 × Wither Rose; Mortem × 1 (**1 enzymes**); **50 mL weaving**; plus one blank. Catalyst burden: **high/specialized: Wither Rose**. Lantern-route reference: **1,250 mL + 4 min**, excluding catalyst/infrastructure work. [Scarred alternative](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_hemorrhage_scarred.json): 1 × Scar Thorn; Mortem × 1, Ferric × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Lantern reference **1,900 mL / 6 min**. **Crude-memory alternative:** loot and learn for 500 mL; no weaving required.
- **Implemented gameplay:** Nearest other living entity within eight blocks receives eight seconds of amplifier-1 Blood Loss.
- **Effort versus utility / balance:** 100 mL is cheap, but Blood Shot gives much longer stronger bleed plus a ranged hit. Wither Rose makes weaving unattractive; crude access helps. Ally/visibility filtering is weak.
- **Casting and visual design:** Existing tendril/green particles need a persistent wound. Establish gameplay distinction before larger FX.

### Lignum Mortis

`lignum_mortis` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/LignumMortisManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Mortem alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Continuous. **100 mL initially and per second**; 1 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_lignum_mortis.json): 1 × Oak Log; Animus × 1, Mortem × 1 (**2 enzymes**); **75 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,875 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channels selection through connected organic blocks and dismantles on release; selection/range caps, block-entity exclusion and break-time permission checks exist.
- **Effort versus utility / balance:** 100 mL/s is useful bulk harvesting. Holding after useful selection completes continues expense; cheap log catalyst fits.
- **Casting and visual design:** Custom vein-band selection overlay/tendrils already work conceptually. Show completion/cap reached to prevent wasted channeling.

### Canopy Mortis

`canopy_mortis` · **Established; naming clarity needed** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/LignumMortisManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Mortem alignment** to cast through the normal path; know **Lignum Mortis** and reach shared mastery **I** (10 cumulative credits). Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Continuous. **125 mL initially and per second**; 1.5 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_canopy_mortis.json): 1 × Oak Log; Animus × 1, Mortem × 2 (**3 enzymes**); **125 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,525 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Traverses leaves to reach connected wood, but skips the leaves themselves for selection/removal.
- **Effort versus utility / balance:** 125 mL/s and mastery I are useful for connected trees. It is not a leaf-clearing spell.
- **Casting and visual design:** Use light transit veins on leaves and strong marks only on blocks that will break.

### Worked Lignum

`worked_lignum` · **Established utility** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/LignumMortisManip.java)

- **Unlock:** Degree **0** (Humilis); **0 Mortem alignment** to cast through the normal path; know **Lignum Mortis** and reach shared mastery **III** (85 cumulative credits). Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Continuous. **175 mL initially and per second**; 2 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_worked_lignum.json): 1 × Oak Log; Animus × 1, Mortem × 4 (**5 enzymes**); **275 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **3,875 mL + 12 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Extends the family's selection/release harvest to worked wooden materials.
- **Effort versus utility / balance:** 175 mL/s and mastery III reasonably price broader dismantling. Five enzymes add effort; test server permissions/owned builds.
- **Casting and visual design:** Keep selection overlay and clearly identify worked material before release.

### Exsanguinate

`exsanguinate` · **Functional; severe refund defect** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/ExsanguinateManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Mortem alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ductilis.
- **Operation:** Quick. **300 mL**; **2.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_exsanguinate.json): 1 × Vitality Chalice; Mortem × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **moderate: crafted mod vessel**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Targets a nearby entity below 30% HP, attempts damage at 1.5 times current HP, then restores 600 mL.
- **Effort versus utility / balance:** 300 mL creates nominal net +300. Restoration is not conditional on successful damage, death or blood-bearing target, enabling unintended gains from protected/immune victims. Fix before tuning.
- **Casting and visual design:** Inward tendril fits extraction. Play the successful siphon only when blood is actually earned.

### Insatiable Hunger

`insatiable_hunger` · **Established specialist** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/InsatiableHungerManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Mortem alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Animus.
- **Operation:** Quick. **225 mL**; **3.5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_insatiable_hunger.json): 1 × Rotten Flesh; Animus × 1, Mortem × 1 (**2 enzymes**); **150 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,950 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Roughly eleven seconds of 75% healing suppression plus hunger/exhaustion on a nearby non-allied target.
- **Effort versus utility / balance:** 225 mL and Rotten Flesh fit an anti-healing tool. Stronger in PvP/healing encounters than ordinary mobs; review missing line-of-sight restriction.
- **Casting and visual design:** Give green glow a persistent sealed-healing/hunger mark so invisible utility is readable.

### Grave Debt

`grave_debt` · **Established combo tool** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/GraveDebtManip.java)

- **Unlock:** Degree **I** (Mediocritas); **10 Mortem alignment** to cast through the normal path. Vascular section: **Heart**; secondary tendency: Animus.
- **Operation:** Quick. **325 mL**; **3.75 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_grave_debt.json): 1 × Bone; Animus × 1, Mortem × 1 (**2 enzymes**); **175 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **1,975 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Nine-second mark triggers a burst when a health threshold is crossed; timely death refunds 250 mL to the owner.
- **Effort versus utility / balance:** 325 mL becomes net 75 when the victim dies, including kills by others. Strong setup value; Bone is suitable effort.
- **Casting and visual design:** Show the health threshold and detonation readiness instead of only a green burst.

### Blackhearted

`blackhearted` · **Established reactive idiom** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/BlackheartedManip.java)

- **Unlock:** Degree **V** (Magister); **35 Mortem alignment** to cast through the normal path. Vascular section: **Heart**; secondary tendency: Animus.
- **Operation:** Passive. **0 mL**; reactive wither/saturation rules, including rupture recovery; registry CD is zero.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blackhearted.json): 1 × Wither Rose; Animus × 1, Mortem × 2 (**3 enzymes**); **300 mL weaving**; plus one blank. Catalyst burden: **high/specialized: Wither Rose**. Lantern-route reference: **2,700 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Converts much incoming wither damage into healing while accumulating necrotic saturation; excess causes a damaging rupture/recovery period.
- **Effort versus utility / balance:** Zero blood cost still has a downside. This is a real passive, not a stub. Event activation bypasses the registry's alignment-35 casting check; reconcile/document that exception.
- **Casting and visual design:** Saturation/HUD exists. Improve external necrotic buildup and rupture without replacing the meter.

### Funeral Bell

`funeral_bell` · **Functional; charge-scaling concern** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/FuneralBellManip.java)

- **Unlock:** Degree **VI** (Perfectus); **75 Mortem alignment** to cast through the normal path. Vascular section: **Heart**.
- **Operation:** Charged. **1000 mL** at full charge; **8 s shared cooldown**; **4 s full charge**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_funeral_bell.json): 1 × Wither Skeleton Skull; Mortem × 4 (**4 enzymes**); **600 mL weaving**; plus one blank. Catalyst burden: **high: rare combat drop**. Lantern-route reference: **3,600 mL + 10 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Charged AoE adds damage for supported existing debuffs, reaching roughly twelve nominal damage at full charge/four statuses; adds wither when appropriate.
- **Effort versus utility / balance:** 1000 mL, Degree VI, four-second charge and skull catalyst are steep for a setup finisher. Status-bonus damage is not charge-scaled, so cheap short releases retain disproportionate value.
- **Casting and visual design:** Bell audio exists. Add a suspended bell/ribbed resonance shape and rings passing through marked enemies.

### Carrion Communion

`carrion_communion` · **Functional; undercut by early healing** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/CarrionCommunionManip.java)

- **Unlock:** Degree **V** (Magister); **60 Mortem alignment** to cast through the normal path. Vascular section: **Body**.
- **Operation:** Continuous. **220 mL initially and per second**; 6 s shared cooldown on stop.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_carrion_communion.json): 1 × Rotten Flesh; Mortem × 3 (**3 enzymes**); **450 mL weaving**; plus one blank. Catalyst burden: **ordinary material/mod-resource acquisition; no special encounter gate in this recipe**. Lantern-route reference: **2,850 mL + 8 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Channels against up to four nearby debuffed targets, dealing two damage and healing one HP per successful damage event.
- **Effort versus utility / balance:** 220 mL/s provides at most roughly four HP/s under ideal conditions, versus Blood Rush's ten at Degree 0. Degree V and target setup make the mismatch severe.
- **Casting and visual design:** Replace victim-only dust with inward streams and failed-drain feedback for immune targets.

## Saint memories

4 active registrations.

### Crimson Tithe

`crimson_tithe` · **Functional; poor reward economy** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/saint/CrimsonTitheManip.java)

- **Unlock:** Degree **III** (Summa); **25 Mortem alignment** to cast through the normal path. Vascular section: **Heart**; secondary tendency: Lux.
- **Operation:** Quick. **400 mL**; **5 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_crimson_tithe.json): 1 × Hallowed Residuum Hemorath; Mortem × 1, Ferric × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **very high: named saint encounter reward**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Pays 400 mL to borrow 500 for thirty seconds. Unspent credit is returned; spent credit incurs double repayment plus health penalty.
- **Effort versus utility / balance:** Only about 100 mL immediate net liquidity before modifiers is weak for a saint catalyst and debt risk. Needs a clearer use case than costly temporary credit.
- **Casting and visual design:** Heartbeat/dust establish mood, not liability. Show borrowed blood and debt countdown.

### Unclosing Eye

`unclosing_eye` · **Established specialist** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/saint/UnclosingEyeManip.java)

- **Unlock:** Degree **III** (Summa); **20 Lux alignment** to cast through the normal path. Vascular section: **Head**; secondary tendency: Mortem.
- **Operation:** Quick. **350 mL**; **6 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_unclosing_eye.json): 1 × Hallowed Residuum Seraphae; Animus × 1, Lux × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **very high: named saint encounter reward**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Thirty seconds of repeated large-area reveal/invisibility stripping with night vision.
- **Effort versus utility / balance:** 350 mL is better sustained reconnaissance than Hemolymphal Pulse, but Seraphae's residuum makes access much harder.
- **Casting and visual design:** Shape the END_ROD halo into a recognizable eye with a steady reveal sweep.

### Bloom Of Rot

`bloom_of_rot` · **Functional; ally-safety review** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/saint/BloomOfRotManip.java)

- **Unlock:** Degree **III** (Summa); **25 Mortem alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Animus.
- **Operation:** Quick. **500 mL**; **4 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_bloom_of_rot.json): 1 × Hallowed Residuum Putriciel; Flammeus × 1, Mortem × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **very high: named saint encounter reward**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Ten seconds of wither, poison and slowing in a large nearby volume, with five seconds of self-poison.
- **Effort versus utility / balance:** 500 mL and Putriciel's residuum buy strong Funeral Bell/Carrion setup. Self-poison is real cost, but nearby allies are also included.
- **Casting and visual design:** Grow short fungal/blood petals around affected feet and collapse into rot marks instead of random green glow.

### Endless Hour

`endless_hour` · **Partially integrated; core promise missing** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/saint/EndlessHourManip.java)

- **Unlock:** Degree **III** (Summa); **30 Congeatio alignment** to cast through the normal path. Vascular section: **Body**; secondary tendency: Lux.
- **Operation:** Quick. **600 mL**; **10 s shared cooldown**.
- **Memory:** [Weaving recipe](../../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_endless_hour.json): 1 × Hallowed Residuum Velorum; Congeatio × 1, Tenebris × 1 (**2 enzymes**); **100 mL weaving**; plus one blank. Catalyst burden: **very high: named saint encounter reward**. Lantern-route reference: **1,900 mL + 6 min**, excluding catalyst/infrastructure work.
- **Implemented gameplay:** Applies ten seconds of Absorption V/Resistance IV and writes deferred-damage state. Accumulation and expiry/payback methods have no production callers.
- **Effort versus utility / balance:** 600 mL and Velorum's residuum currently buy powerful protection without the advertised repayment. Potion effects do not establish death deferral. This is an integration gap, not just balance.
- **Casting and visual design:** Blue activation glow exists; repayment FX are in an uncalled method. Wire behavior first, then show an hourglass/countdown and debt fracture.

## Living Staff and conjured tools

11 active registrations.

**Shared weapon-form costs:** the seven ordinary weapon forms and sickle register 0 mL/0 cooldown, but switching a wielded staff costs **250 mL**, reduced by Weapons Master by 50 per level to **50 mL** at level IV. This path is separate from the ordinary manipulation cost ledger. Shared living melee tools can also charge blood on hits, and unleashed attacks add their own costs; the torch and crossbow have distinct operating rules. Restoring the stored staff, paired claws, item metadata and low-blood reversion are implemented. Weapon damage depends on item state and attributes, so the registry cost is not an adequate weapon balance sheet.

### Conjure Blade

`conjure_blade` · **Established staff form** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Animus alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Hematic Iron Powder + Vivacious Enzyme**. Requires Living Staff access and earned **blood-rich arsenal kill**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Transforms a wielded Living Staff into the blade while preserving/restoring staff state. Melee has additional blood-sensitive and unleashed behavior.
- **Effort versus utility / balance:** Registry zero hides a 250 mL hot-swap cost, reduced by Weapons Master. Blade acquisition uses hematic iron powder and Vivacious Enzyme plus the blood-rich arsenal activity; useful general melee form.
- **Casting and visual design:** Authored weapon/morph effects are appropriate. Emphasize tame versus unleashed attack state and low-blood reversion.

### Conjure Axe

`conjure_axe` · **Established staff form** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Mortem alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Chalybeate Sclerite + Ruinous Enzyme**. Requires Living Staff access and earned **qualifying Mortem activity**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Axe tool/combat form with unleashed healing/cost behavior and ground-swing area functionality.
- **Effort versus utility / balance:** Graft needs Chalybeate Sclerite/Ruinous Enzyme and qualifying earned activity. Utility is both chopping and melee; account for shared hit blood drains and extra unleashed cost.
- **Casting and visual design:** Existing blood-break particles should emphasize the ground strike arc and impact footprint.

### Conjure Spear

`conjure_spear` · **Established staff form** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Lux alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Calcified Blood Spine + Incandescent Enzyme**. Requires Living Staff access and earned **glowing kill or qualifying luminous activity**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Spear form supports a short-held riptide-style impulse and spin attack; melee retains the shared living-tool behavior.
- **Effort versus utility / balance:** Calcified Blood Spine/Incandescent Enzyme and luminous activity gate a useful reach/mobility option. Release method has no separate blood payment, so assess repeated movement after the swap.
- **Casting and visual design:** Clarify thrust/impulse direction with a long narrow trail and contact point, retaining authored weapon visuals.

### Conjure Claws

`conjure_claws` · **Established staff form** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Tenebris alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Mortem.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Chitinous Husk + Umbral Enzyme**. Requires Living Staff access and earned **darkness/invisibility kill**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Fast melee form can pair an offhand claw when space permits; preserves/restores staff state.
- **Effort versus utility / balance:** Chitinous Husk/Umbral Enzyme plus darkness/invisibility activity suit the ambush role. Swap and shared melee upkeep mean it is not truly free.
- **Casting and visual design:** Custom purple claw slashes already have a distinct identity. Test paired-hand synchronization and avoid obscuring the target.

### Conjure Crossbow

`conjure_crossbow` · **Established staff form; item compatibility testing needed** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Ductilis alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Puppeteering Thread + Neurotic Enzyme**. Requires Living Staff access and earned **conductive-arc activity**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Living crossbow loads/fires projectiles with crossbow mechanics and custom stored projectile state. Blood-bolt shots can incur a 50 mL payment on a 50% roll, beyond swap cost.
- **Effort versus utility / balance:** Puppeteering Thread/Neurotic Enzyme plus conductive activity gate a ranged weapon, not a zero-cost spell. Ammo, draw time and enchantments determine practical damage; verify saved loaded state.
- **Casting and visual design:** Weapon draw/fire exists. Add readable blood-bolt charging/loading and distinct form-morph feedback rather than a generic spell burst.

### Conjure Torch

`conjure_torch` · **Established; strong utility/offense** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Flammeus alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Lux.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Fervent Spores + Fervent Enzyme**. Requires Living Staff access and earned **burning kill or qualifying fire activity**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Living Torch breath has six-tick windup, seven-block range, a cone, movement penalty and hostile/visibility filtering; damage pulses every four ticks.
- **Effort versus utility / balance:** Fervent Spores/Fervent Enzyme plus burning activity unlock a real flamethrower. Breath costs three mL/tick (60/s), far below Furnace Veins upkeep; compare effective coverage and aim requirements.
- **Casting and visual design:** Existing authored breath needs a clear windup, tapered cone and heat fade; retain its directional rather than random-cloud design.

### Conjure Flail

`conjure_flail` · **Established staff form** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Congeatio alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Memory:** one blank Hematic Memory + one Sanguine Formation + **Frigid Spores + Frigid Enzyme**. Requires Living Staff access and earned **slowed/frozen kill or qualifying frost activity**; absorb with the staff at a lit brazier. Two total enzymes including the blank imply **1,200 mL / four minutes** of reference lantern production, excluding catalysts/tool setup. No ordinary weaving recipe is expected.
- **Implemented gameplay:** Deployed/charged flail projectile with orbit/tether behavior; melee slows targets and held deployment slows the wielder.
- **Effort versus utility / balance:** Frigid Spores/Frigid Enzyme plus freezing/slowing activity buy a differentiated control weapon. Shared melee drain and swap cost matter alongside handling difficulty.
- **Casting and visual design:** Authored head/tether/orbit already distinguish it. Verify release, return and wall contact readability.

### Conjure Sickle

`conjure_sickle` · **Established endgame reward; registry gate mismatch** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/StaffWeaponFormManip.java)

- **Unlock:** Degree **VI** (Perfectus); **0 Mortem alignment** to cast through the normal path; **actual acquisition requires Degree VII**. Vascular section: **Arms**; secondary tendency: Ferric.
- **Operation:** Quick. **0 mL / 0 s in registry**, but **250→50 mL actual staff-form switch**, plus item-specific operating costs.
- **Acquisition:** Degree VII / Silent Archon, defeat Vesper, retain the staff bond and consume **Memory of Vesper**. Exceptional endgame reward, not a missing weaving recipe.
- **Implemented gameplay:** Living Sickle offers short-reap execution bonus, spinning AoE or blood-hook actions with its own cooldowns and authored effects.
- **Effort versus utility / balance:** Registry says Perfectus/Degree VI, but acquisition requires Degree VII, Silent Archon, Vesper defeated, staff bond and consuming Memory of Vesper. No ordinary woven memory is expected; major reward burden supports premium utility.
- **Casting and visual design:** Blood cells, dark glow, claw slashes and tendrils are already substantial. Refine mode/charge distinction rather than adding generic particles.

### Conjure Staff

`conjure_staff` · **Established progression tool** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/ConjurationManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Ferric alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Quick. **1000 mL**; **2 s shared cooldown**.
- **Acquisition:** build the Degree-I Living Staff structure: 150 blood, held Sanguine Formation, two Venous Stone and two Iron Bars. The completed staff route grants this power; no normal woven memory.
- **Implemented gameplay:** Manifests the Living Staff, which supports blood absorption/projection and earned weapon forms.
- **Effort versus utility / balance:** 1000 mL is a substantial 20% base-reservoir entry cost, but a persistent multipurpose tool merits more than a disposable attack. Learned by completing the Degree-I staff structure, not ordinary memory weaving.
- **Casting and visual design:** Existing staff/morph presentation is a foundation. Make manifestation and subsequent form changes visually distinct.

### Blood Absorption

`blood_absorption` · **Established core tool** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/ConjurationManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Ferric alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Quick. **1000 mL**; **2 s shared cooldown**.
- **Acquisition:** automatic Degree-I core-power grant. No ordinary memory crafting burden; manifestation still has the price above.
- **Implemented gameplay:** Manifests the absorption tool used in blood interactions and memory rites; auto-granted at Degree I and assigned a fixed slot.
- **Effort versus utility / balance:** 1000 mL is manifestation cost, not a universal price for every later absorbed item. Essential progression access should be explained clearly; no current ordinary memory recipe is missing here.
- **Casting and visual design:** Preserve authored absorption streams. Add valid-target and completion feedback where rituals otherwise seem stalled.

### Blood Projection

`blood_projection` · **Established core tool** · [Implementation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/ConjurationManip.java)

- **Unlock:** Degree **I** (Mediocritas); **0 Ferric alignment** to cast through the normal path. Vascular section: **Arms**; secondary tendency: Animus.
- **Operation:** Quick. **1000 mL**; **2 s shared cooldown**.
- **Acquisition:** automatic Degree-I core-power grant. No ordinary memory crafting burden; manifestation still has the price above.
- **Implemented gameplay:** Manifests the projection tool for supplying blood to structures/machines; auto-granted at Degree I with a fixed slot.
- **Effort versus utility / balance:** 1000 mL entry cost plus subsequently projected blood can surprise new players. It is progression infrastructure, not a combat spell to rate by damage.
- **Casting and visual design:** Existing projected blood should visibly terminate at the receiver, with clear full/invalid-state feedback.

## Retired registrations: compatibility, not active missing powers

These fifteen IDs remain registered but are retired from normal learning/use. They are excluded from the active balance verdicts. Their continued presence should not be mistaken for fifteen unfinished available spells.

| Retired ID | Registry rank / degree | Base mL | Implementation |
|---|---|---:|---|
| `venous_travel` | Mediocritas / I | 1000 | [BloodManipulation](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java) |
| `ferric_transmutation` | Summa / III | 1000 | [FerricTransmutationManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/FerricTransmutationManip.java) |
| `summon_thrall` | Mediocritas / I | 500 | [SummonThrallManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/SummonThrallManip.java) |
| `hemosynthesis` | Humilis / 0 | 200 | [HemosynthesisManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/HemosynthesisManip.java) |
| `blood_lamp` | Humilis / 0 | 75 | [BloodLampManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/BloodLampManip.java) |
| `crimson_harvest` | Humilis / 0 | 200 | [CrimsonHarvestManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/CrimsonHarvestManip.java) |
| `sanguine_excavation` | Mediocritas / I | 400 | [SanguineExcavationManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/SanguineExcavationManip.java) |
| `ferric_resonance` | Mediocritas / I | 600 | [FerricResonanceManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/FerricResonanceManip.java) |
| `crimson_sight` | Mediocritas / I | 250 | [CrimsonSightManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/CrimsonSightManip.java) |
| `vital_reservoir` | Mediocritas / I | 50 | [VitalReservoirManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/VitalReservoirManip.java) |
| `glacial_circulation` | Humilis / 0 | 175 | [GlacialCirculationManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/GlacialCirculationManip.java) |
| `glacial_bastion` | Mediocritas / I | 350 | [GlacialBastionManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/GlacialBastionManip.java) |
| `blood_eclipse_mantle` | Mediocritas / I | 325 | [BloodEclipseMantleManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/BloodEclipseMantleManip.java) |
| `vigil_of_glass` | Magister / V | 300 | [VigilOfGlassManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/lux/VigilOfGlassManip.java) |
| `hematic_ballast` | Magister / V | 200 | [HematicBallastManip](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/HematicBallastManip.java) |

## Recommended order of work

1. **Correct behavior/economy before buffing weaker powers:** connect Endless Hour's intended damage lifecycle; make Exsanguinate's reward conditional on the intended successful extraction; reconcile charged payload/cost; correct Blood Rush cadence; verify Ward restart behavior.
2. **Make targeting and payment predictable:** validate the final target/destination before payment, define ally/PvP policy consistently, and clip appropriate aimed attacks against world geometry. Preserve intentional specialist exceptions explicitly.
3. **Rebalance comparison pairs:** Ward–Ironhearted–Iron Retort; Blood Rush–Carrion–Lumen Suture; Shroud–Drift; Halo–Coronation; Torch/Ignition–Furnace; Binding–Rimebound/Stillness; Osseous Bloom–flat-damage finishers. A higher degree needs a distinct useful role, not necessarily higher damage.
4. **Align acquisition with actual usefulness:** document the Degree-III loom bottleneck and early alternatives; reconsider premium catalysts on weak outcomes; avoid repetitive Nether Star payments feeling mandatory for Avatar progression. Verify intentionally cross-school enzyme recipes rather than automatically changing them.
5. **Improve spatial/temporal visual information:** White Verdict's beam first; Dowsing's underground readability; Eclipse Well/Stillness boundaries; Coronation ammunition; reactive readiness; saint debt timers. Then refine already-authored low-rank effects.

### Casting visual direction

Use the existing authored blood cells, tendrils, glows, lightning, claws and slash renderers as the visual vocabulary. More vanilla particles will not solve missing shape or timing. Each important cast should communicate **preparation → release → impact → active duration → expiration**, with effect size matching gameplay reach. Charged powers already have a generic HUD charge bar; the gap is distinctive world-space buildup and target information, not a total absence of charge feedback.

Family variants should preserve their common identity while exposing the actual choice: guided trajectory, mortar mass, fan angle, lance convergence, binding jumps, cloud footprint, updraft hover, and Avatar body stage. Verify effects and sounds on a dedicated server: a server-side proxy call is not equivalent to a confirmed visible packet path. The fallback sound registry maps several baselines but omits their new Shot/Needle/Avatar variants, and baseline Avatar can replay its cue with upkeep; normalize event timing rather than merely assigning louder sounds.

### Focused live checks needed to confirm this audit

- Cast at walls, allies, enemy players, bosses, bloodless mobs and invalid destinations with blood measured before/after.
- Compare one-tick versus full-charge Mortar/Lance/Funeral Bell; measure actual damage and paid blood.
- Measure Blood Rush healing, Exsanguinate on damage-rejected targets, and Ward stop/restart shielding.
- Exercise Endless Hour expiry/lethal damage, Phoenix readiness and Blackhearted saturation on a dedicated server.
- Test Beacon regeneration while refreshed, projectile crossing through Stillness/Choir, and Dowsing behind solid stone.
- Follow an unassisted early acquisition route, including crude memory, Degree-I staff/graft and first Degree-III loom; verify blood/enzymes consumed during interrupted weaving.
- Review first/third-person charge, impact and duration visuals in ordinary combat, not only creative-mode showcases.

No gameplay files were modified and no build, automated combat suite or live client was run for this report. Source inspection establishes what is wired and the nominal mechanics; encounter difficulty, exact sustained DPS, network presentation and fun still need those focused runtime checks.

## Shared implementation evidence

Individual entries link their implementation classes and ordinary/scarred recipes. The following shared paths establish behavior that those classes inherit or delegate to:

- [Registry and base parameters](../../../src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java)
- [Rank gates](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationRankGates.java)
- [Learning checks](../../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulationGrantHelper.java)
- [Family prerequisites](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/family/ManipulationFamilyRegistry.java)
- [Mastery thresholds/discounts](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipLevel.java)
- [Shared casting/payment/cooldown](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java)
- [Channel startup/upkeep](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationChannelManager.java)
- [Input routing and cooldown order](../../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java)
- [Runtime cost ledger](../../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationCostLedger.java)
- [Reactive body idioms](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/BodyIdiomEvents.java)
- [Blood Rush healing cadence](../../../src/main/java/com/vincenthuto/hemomancy/common/effect/BloodRushEffect.java)
- [Blood Loss behavior](../../../src/main/java/com/vincenthuto/hemomancy/common/effect/BloodLossEffect.java)
- [Projectile shot behavior](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodShotEntity.java)
- [Needle behavior](../../../src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodNeedleEntity.java)
- [Avatar upkeep/state](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/AvatarManifestationManager.java)
- [Avatar staged stats](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/AvatarManifestationRules.java)
- [Avatar presentation transitions](../../../src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/AvatarManifestationTransition.java)
- [Cast sound mapping](../../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationCastSounds.java)
- [Charge and idiom HUD](../../../src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BodyIdiomOverlay.java)
- [Weaving interaction/consumption](../../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java)
- [Enzyme production](../../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MycelialLanternBlockEntity.java)
- [Memory item use](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java)
- [Crude-memory use](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/CrudeMemoryShardItem.java)
- [Brazier absorption](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/BrazierItemAbsorptionRite.java)
- [Staff forms and graft conditions](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java)
- [Staff form rules](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormRules.java)
- [Staff activity tracking](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormEvents.java)
- [Shared living-tool melee](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingToolItem.java)
- [Crossbow operating path](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingCrossbowItem.java)
- [Blank memory recipe](../../../src/main/resources/data/hemomancy/recipe/hematic_memory.json)
- [Degree-III loom recipe](../../../src/main/resources/data/hemomancy/recipe/blood_structure/somatic_loom.json)
- [Enzyme fruiting recipes](../../../src/main/resources/data/hemomancy/recipe/enzyme_fruiting)
