## At a glance

**Request:** Which reading? States on targets, the player's own pool, or both? Id say a bit of both we need to keep track of the states per target for mixed and stacked bonues's when built up but I also think its worth it to reward to some lesser extent maybe in efficency…

**Recommendation (excerpt):** Both. Keep school states and stacks on each enemy for the larger payoffs. Give the player a short-lived combat record so changing schools across enemies can earn a smaller reward even after a one-shot kill. | Mechanic | Proposed first pass | |---|---| | Switching | Count the first qualifying, confirmed direct hit per attack root, including killing blows and finishers that spend a state. Start by requiring a change…

**Decisions:**

- Codex has not endorsed plan v1.
- Claude endorsed plan v1.
- No implementation approval recorded.

**Outcome:** Discussion only · complete · stage: complete

---

# Final report endorsed by both agents

## Codex findings
**Both.** Keep the actual school states on each target, where their depth and combinations determine the larger payoffs. Give the player a short-lived record of their last qualifying school hit so changing both school and target earns a smaller bonus, even if the first hit killed. I’d start with one bonus stack on the next eligible application. That fits the existing hit pipeline, since projectile targets may only be known after the cast has already been priced. Temper remains the cast resource. [SchoolCombatEvents.java:182](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:182>) · [TemperCharge.java:19](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/TemperCharge.java:19>)

| Ruling | What the checkout shows and how I’d apply it |
|---|---|
| **Edacious reads the deepest state** | Read the target’s live states before the hit applies or spends anything. Pressure and Rime have stack depth. Most other states are single-level, and Necrosis banks damage in a separate field. I’d define depth as stack count for the first pass, exclude self-concealment (`Veiled`), and prefer the incoming school on a tie, then use a fixed order. [SchoolStates.java:24](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStates.java:24>) · [SchoolStates.java:95](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStates.java:95>) · [SchoolCombatState.java:99](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatState.java:99>) |
| **Phantasmal rewards free mixing, with major reactions concentrated on opposites** | Use the existing pairs: Animus/Mortem, Ductilis/Ferric, Lux/Tenebris, and Flammeus/Congeatio. Evaluate the incoming school against the target’s pre-hit states. Heat and cold can cancel during application, so a rule that waits for both statuses to coexist would miss that reaction. Reserve one payoff per target per root attack, give authored finishers first claim, and match combinations by their required states so a future three-state recipe can take precedence over a pair. [EnumBloodTemper.java:51](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/EnumBloodTemper.java:51>) · [SchoolStates.java:79](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStates.java:79>) · [SchoolCombatEvents.java:203](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:203>) |
| **Bosses can reach the raised caps for states they accept** | Disrupted already rejects tagged bosses, and Pressure rejects bloodless targets. I’d allow applicable damage reactions through the existing encounter defenses and preserve those immunities. Hemorath already gains **60 feed per newly observed Pressure level**, and Rime already has a move-dependent interaction during Wells Answer. Raising the Pressure cap therefore changes his fight today. Any later phase-specific Pressure tuning belongs in his encounter rules. [SchoolStates.java:38](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStates.java:38>) · [HemorathEntity.java:107](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/HemorathEntity.java:107>) · [HemorathEntity.java:857](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/HemorathEntity.java:857>) |

Two implementation details matter most:

- **A cap change has to reach the payoff rules and sync.** Living Blade’s Pressure rupture and Ferric’s Rime shatter both check for exactly three. Pressure rupture damage also clamps at three. The attachment decoder clamps stacks to three on both save reload and client sync. Define stack capacity, payoff thresholds, and native effect strength explicitly. Rime currently slows movement by 15% per level, so raising its amplifier would also deepen the slow. [SchoolCombatEvents.java:91](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:91>) · [SchoolCombatEvents.java:138](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:138>) · [SchoolStateRules.java:6](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolStateRules.java:6>) · [SchoolCombatState.java:125](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatState.java:125>) · [SyncSchoolCombatS2CPacket.java:18](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncSchoolCombatS2CPacket.java:18>) · [EffectInit.java:185](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java:185>)
- **Credit the killing hit and deduplicate by root.** Record an eligible landed hit even when the victim dies before it can receive a status. Limit the player-side reward to once per root so one volley or area cast cannot earn it from every target. I’m reading Edacious and Phantasmal as armor-lineage mechanics. If their signature actives should trigger these payoffs too, they need explicit integration: Bloodburst spawns flagged needles without assigning a school context in its activation handler, and Masquerade echoes use their own damage source. [SchoolCombatEvents.java:182](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:182>) · [SchoolCombatState.java:67](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatState.java:67>) · [EdaciousBloodburstArmorAbilityHandler.java:29](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/armor/ability/EdaciousBloodburstArmorAbilityHandler.java:29>) · [PhantasmalEchoEntity.java:222](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/entity/summon/PhantasmalEchoEntity.java:222>)

The first GameTest I’d add would kill target A with one school, then hit target B with another and verify that B earns the small switch bonus while the major reaction still requires setup on B. I’d also cover levels above three through save and sync, and exercise Hemorath’s feed alongside the Disrupted immunity. This was a read-only source investigation. No project files were edited and no tests were run.

## Claude findings
## Findings: wiring your four answers into school combat

**Assumption up front.** I don't have the proposal these questions came from. I'm reading "Edacious" and "Phantasmal" as the two Blood Lust armour lineages getting state-aware bonuses, because they don't touch school states anywhere today (`EdaciousBloodburstArmorAbilityHandler.java`, `PhanstmalBloodlustArmorAbilityHandler.java`). If they mean something else, the plumbing below still applies; only who calls it changes.

**A name clash to settle first.** In code and docs, "the Phantasmal reaction" already means Distorted Presence. That is a defender-stage effect (blindness, glow and a shove) with its own cooldown, `PlayerCooldowns.PHANTASMAL_REACTIVE` (`PhanstmalBloodlustArmorAbilityHandler.java:59-89`, `ARCHITECTURE_STANDARDS.md:198`). State-on-state reactions need a different name, or the docs will blur the two.

### 1. Both sides: keep states per target, put the swap reward on the player

**Per-target tracking already exists.** `SchoolCombatState` keeps one entry per state on each living target: levels, stored, expires, lastBuild (`SchoolCombatState.java:15-32`). It also keeps per-target ledgers, `applications` and `payoffs`, keyed by root attack (`:33-34, :67-76`). Mixed and stacked build-ups can read straight from these, with no new storage.

**What's missing is any memory on the caster.** `hasPaid` and `hasApplied` are per target (`SchoolCombatEvents.java:84, 88, 206, 221`). Nothing remembers "I just put Rime on A, now I'm putting Searing on B", which is what a swap reward needs.

**Recommendation for the swap reward:**
- Store it as a session map keyed by player UUID: last target, last state, and the tick. Register its clears with `TransientServerStateEvents`. AGENTS.md requires that for short-lived per-player state, and a combat rhythm doesn't need to survive a relog.
- Credit it only after a real application. The place is the `recordApplication(..., primaryState, now)` call at `SchoolCombatEvents.java:227`, so a cancelled, blocked or ally hit earns nothing.
- Pay it in Temper only through `TemperCharge`, either a discount in `plan` (`TemperCharge.java:83`) or a fill. Pay it in stacks through `SchoolStates.apply(..., levels)`.
- Keep it smaller than a same-target payoff, and require a different target inside a short window. That covers "don't punish fast kills": if the last target died, the next one still gets credit.

### 2. Edacious: "whichever is deepest" needs a definition

The states don't share one measure:
- Pressure and Rime count levels, capped at 3 (`SchoolStateRules.java:7, 15`).
- Necrosis is a stored debt, 0 to 6 (`:19`).
- Searing is remaining burn time, worth at most 4 (`:23-29`).
- Lodestone, Illuminated, Obscured and Veiled are on or off (`SchoolStates.apply` `default -> 1`, `:102`).

**Recommendation:** add one pure helper, for example `SchoolStateRules.depth(state, entry, now)`, that returns each state's fill as a fraction of its cap. Then `SchoolStates.deepest(target)` picks the highest, with a fixed tie order. Keep it pure so a unit test can pin it.

### 3. Opposites drive the major reactions, any mix gives a small bonus

**The opposite pairs already exist.** `EnumBloodTemper.opposite()` defines them (`EnumBloodTemper.java:50-62`): Animus/Mortem, Ductilis/Ferric, Lux/Tenebris, Flammeus/Congeatio. Each school maps to its state through `SchoolStates.primaryState`/`school` (`SchoolStates.java:59-70, 142-153`). Tenebris maps to two states, Obscured and Veiled, so the table has to say which of them reacts.

**What already exists, and how it fits your rule:**

| Pair | Today | Fits "majors on opposites"? |
|---|---|---|
| Searing / Rime | Searing on Rime drops one Rime level; Rime on Searing clears Searing (`SchoolStates.java:79-90`) | Yes, but it's a cancel with no damage |
| Ferric hit on Rime at 3 | +4 damage (`SchoolCombatEvents.java:139-144`) | No, Ferric's opposite is Ductilis |
| Bloom of Rot (Flammeus) on Necrosis | releases the debt (`:112-118`) | No |
| Conductive arc | fires on Conductive Mark *or* Lodestone (`:170, :232-244`) | Close to Ductilis/Ferric, but it's a mark, not the Disrupted state |
| Illuminated vs Obscured | Illuminated beats concealment (`SchoolStateRules.canPerceive`, `:39-41`) | Yes, but only for perception |

**Open question for you:** should the off-pair reactions (Ferric on Rime, Flammeus on Necrosis) stay as named exploits, or be moved down to "minor mix" bonuses?

**Disrupted is a weak member of the Ductilis/Ferric pair.** It lasts 10 ticks (`SchoolStates.java:95`) and never lands on bosses (`:45`). Using the Conductive Mark as the Ductilis side of that reaction is probably cleaner.

**Recommendation for the reaction table:**
- Build one table (`SchoolReactions`) keyed by an unordered set of states. Opposite pairs are the major rows. A generic minor bonus covers any other two states. Rows for three or more states can be added later without a new mechanism.
- Run it from `SchoolCombatEvents.prepare`/`commit` so it inherits the once-per-root-attack payoff rule (`:84-88, :206`) and the REACTION damage kind, which stops reactions from chaining.
- Don't add more `ability.equals(...)` branches to `prepare`. It's already a long ladder of them (`:92-137`).

### 4. Raising caps, bosses included

The cap of 3 is hard-coded in many places, and all of them would move together:
- **Rules:** `SchoolStateRules.java:7, 11, 15`.
- **Hit parameters:** `SchoolHitContext.java:25` clamps levels to 1–3.
- **Saving and loading:** `SchoolCombatState.java:135` clamps loaded levels to 1–3, and `:136` clamps the stored debt to 0–6.
- **Exact-3 checks:** `SchoolCombatEvents.java:93` (Living Blade's `pressure == 3`; a fourth level would silently skip the rupture), `:140` (`RIME == 3`), `AbsorptionRiderEffects.java:94` and `AbsoluteStillnessManip.java:43` (`>= 3`).
- **Velorum:** keeps its own `RIME_CAP = 2` (`VelorumMoves.java:46`), which is fine to leave.
- **Visuals:** Pressure's drops are spaced at 120° (`SchoolStateVisuals.java:58`, `i * 2π / 3`), so a fourth drop would draw on top of the first.

**Recommendation:** add `SchoolStateRules.cap(state, caster, target)` and replace every literal with it. The exact-3 checks become "at cap". The gear or set that raises the cap comes in as an argument.

**Bosses:** a raised cap applies to them through the same path. Disrupted stays excluded at `SchoolStates.java:45`, and boss checks must keep going through `HemoEntityPredicates.isBoss`. Hemorath already reacts to his own states each tick: `pollSchools` lets Pressure feed him, Lodestone tear his patches, and Rime break his Well's Answer (`HemorathEntity.java:857-877`). That's the pattern for your "Pressure does more or less depending on his state" idea. Keep each boss's reactions in its own class rather than adding boss branches to `SchoolStates`.

### Tests and docs this would touch
- **Tests:** the GameTests that pin today's caps and reactions (`SchoolCombatGameTests`, `ManipulationBalancePass2StateGameTests`, `HemorathFightGameTests`) and `AbsorptionRidersTest`.
- **Docs:** `wiki/School-Combat.md`, the school-combat sections of `HEMOMANCY_REFERENCE.md`, and `MANIPULATIONS_AND_CASTING.md`. Record each answer above as a ruling there, and delete its row from `OPEN_QUESTIONS.md` if it has one.

## Comparison and final result
## Final answer

**Both.** Keep school states and stacks on each enemy for the larger payoffs. Give the player a short-lived combat record so changing schools across enemies can earn a smaller reward even after a one-shot kill.

| Mechanic | Proposed first pass |
|---|---|
| Switching | Count the first qualifying, confirmed direct hit per attack root, including killing blows and finishers that spend a state. Start by requiring a change of both school and enemy. Deduplicate history updates as well as rewards so a volley's late hits cannot overwrite newer history. |
| Reward | Bank one small, expiring Temper discount for the next eligible cast or Sculpting pulse. Spend it after successful payment. The proposal moved from bonus stacks to efficiency because only Pressure and Rime currently build levels, and cast pricing can precede projectile impact. |
| Edacious | Read the deepest live hostile state before impact. The proposed full-set passive starts with stack count, excludes Veiled, and breaks ties by incoming school, then fixed order. Ranking by percentage of cap would let a binary mark outrank two Pressure stacks. |
| Phantasmal | Give free mixing a modest payoff. Center major reactions on Animus/Mortem, Ferric/Ductilis, Lux/Tenebris, and Flammeus/Congeatio, with room for later three-state combinations. Match the incoming school against pre-hit states so heat and cold can react alongside ordinary cancellation. Distorted Presence remains the defensive ability. |
| Bosses | Favor raised caps for states the boss already accepts, with Disrupted immunity intact. Hemorath is offered 60 feed per newly observed Pressure level. Encounter gates can refuse it, phase affects intake, and accepted feed can heal him during Thirst. |

## Code evidence and validation

`SchoolCombatEvents.commit` records attempted applications, and `SchoolStates.canApply` rejects dead targets. Confirmed damage therefore supplies the history needed to count lethal hits. Living Blade rupture and Ferric shatter both check for exactly three stacks. The attachment decoder clamps save and sync to three, Rime slows by 15% per level, and a fourth Pressure visual overlaps the first. Cap changes need explicit rules for application strength, finisher thresholds and damage, slow strength, and display. A same-target `REACTION` under an already-paid root is canceled by `SchoolCombatEvents.prepare`.

Validation remains pending. Cover a one-shot kill followed by a school switch, a delayed volley hit, a blocked hit, one-time discount settlement, both heat/cold orders, stacks above three through save and sync, and Hemorath's feed during Thirst. The investigation was read-only. No project files were modified and no validation commands ran.

## Unresolved disagreement

Both agents agreed on the design direction. Claude approved the shared proposal as a first pass. Codex withheld implementation approval because Edacious's payoff, Phantasmal's minor and major effects, precedence with existing finishers and conduction, and the raised caps and finisher thresholds still need concrete rules. Other open choices include same-school and status-only switches, whether Necrosis debt or Searing time contributes to depth, signature-active integration, and boss eligibility for generic reactions, including those involving Conductive Mark. The discount amount and expiry can be tuned in playtests.

## Final report assessments
### codex
Endorsed as a read-only design report. It accurately captures the owner's intent to keep stacks and mixes on each target and reward the player for switching across enemies, including after a one-shot kill. SchoolStates rejects dead targets and resolves heat and cold during application, supporting the proposed use of confirmed hits for switch history and pre-hit states for reactions. The report accurately qualifies Hemorath's Pressure feed and identifies the effects of raising caps on finishers, sync, Rime slowdown, and visuals. Implementation readiness remains open pending concrete Edacious and Phantasmal payoffs, their precedence with existing finishers and conduction, and explicit cap and threshold rules. No runtime validation was performed.

### claude
Endorsed. I checked each code claim in the report against the source, and all of them hold.

- Commit records the application whether or not the state landed (SchoolCombatEvents.java:221-227). canApply rejects dead targets (SchoolStates.java:39). So the switch history has to come from confirmed damage, which keeps killing blows counting.
- Living Blade rupture and the Ferric shatter test for exactly 3 stacks (SchoolCombatEvents.java:93, :140).
- The decoder clamps stacks to 1–3 (SchoolCombatState.java:135). Client sync goes through the same deserializeNBT (SyncSchoolCombatS2CPacket.java:22), so sync clamps too.
- Rime is a −0.15 ADD_MULTIPLIED_TOTAL speed modifier, which scales with the amplifier (EffectInit.java:185-188).
- A fourth Pressure drop draws on top of the first, because the angle is i·2π/3 (SchoolStateVisuals.java:58).
- A REACTION hit under an already-paid root is cancelled (SchoolCombatEvents.java:84-86).
- Only Pressure and Rime build levels; every other state is fixed at 1 (SchoolStates.java:98-103).
- Temper is planned before a cast is priced (TemperCharge.java:18-22), so a discount can't apply to the hit that earned it. That supports moving the reward from stacks to efficiency.
- Hemorath's feed() checks its gates, scales intake by phase, and heals him while thirsting (HemorathEntity.java:318-326). Each newly seen Pressure level offers 60 (:107, :859-864).
- The objection to ranking depth by percentage of cap is correct: an on/off state would score 1.0 and outrank Pressure at 2 of 3.

The report also describes the disagreement fairly. I approved the proposal as a first pass, and Codex is waiting for concrete rules for the reactions, finisher precedence and caps.

Three omissions, none blocking:
- The cap list leaves out the 1–3 clamp in SchoolHitContext.java:25. The report's 'damage' presumably covers ruptureDamage's clamp at 3, but the clamp isn't named.
- The report dropped two constraints the proposal had on the Temper credit. It must live in a UUID session map cleared through TransientServerStateEvents, not on the blood_temper attachment (TemperGuardrailTest). And, as a new pricing rule, it is a core-loop change that needs CORE_LOOP.md and CORE_LOOP_TEACHING_LEDGER.md updated alongside it.
- Glacial Grasp applies Rime directly through SchoolStates.apply, outside commit (GlacialGraspSculpting.java:142). That case should be named under the open question on status-only switches.

## Exact shared proposal
# School combat proposal

## Objective

**Use both.** Each target keeps its own school states and stacks. Those states drive Edacious’s deepest-state payoff and Phantasmal’s mixed-state reactions. The player keeps a short-lived combat record so switching schools across enemies earns a smaller reward, even when the first enemy dies in one hit.

**Writer:** Codex, synthesizing both agents’ investigation and critiques.

## Approach

| Mechanic | Proposed first pass |
|---|---|
| **Switching targets** | Reward a change of both enemy and school within a short window. The first confirmed, qualifying direct hit from an attack counts, including a killing blow or a finisher that spends a state. Track processed attack roots on the player so a volley cannot earn multiple rewards or let a late hit overwrite newer history. |
| **Switch reward** | Bank one small Temper discount for the next eligible paid cast or Sculpting pulse. Consume it only after payment and let it expire quickly. This gives every school a usable reward. Pressure and Rime can support bonus stacks with explicit rules, while most other states currently have only one level. |
| **Edacious** | Use the deepest live hostile state already on the target before impact. Start with actual stack count, break ties in favor of the incoming school and then by a fixed order, and exclude `Veiled`. Treat the bonus as a full-set passive for the first pass. |
| **Phantasmal** | Give a modest payoff for mixing schools on one enemy. Put the major reactions primarily on the existing pairs: Animus/Mortem, Ferric/Ductilis, Lux/Tenebris, and Flammeus/Congeatio. Match the incoming school against the target’s pre-hit states so heat and cold can react even when ordinary application cancels one of them. Allow future recipes to require three or more states. Treat this as an offensive full-set passive, with Distorted Presence retaining its defensive role. |
| **Bosses** | Let bosses reach raised caps for states they already accept. Keep Disrupted immunity and existing encounter defenses. Approve new major reactions per encounter. Hemorath already offers his meter 60 feed per newly observed Pressure level, subject to encounter gates and phase scaling. Accepted feed can heal him during Thirst, so verify the higher cap against that behavior before adding a custom Pressure reaction. [HemorathEntity.java:318](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/HemorathEntity.java:318>) · [HemorathEntity.java:858](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/HemorathEntity.java:858>) |

## Affected files and areas

- **School combat.** Use `SchoolCombatEvents.prepare` to read pre-hit state and `commit` to record confirmed outcomes. Preserve authored finishers, ordinary thermal cancellation, and the once-per-target payoff rule. Decide precedence with conduction explicitly. A same-target reaction carrying the original root will be canceled if that root is marked paid before the reaction lands. [SchoolCombatEvents.java:84](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:84>)
- **Player history and pricing.** Put the short-lived history and credit in server session state managed by `TransientServerStateEvents`. Price the credit through `TemperCharge`. The hit that earns the credit may land after its own cast has already been paid. [TemperCharge.java:18](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/TemperCharge.java:18>)
- **Caps, persistence, and visuals.** Update `SchoolStateRules`, `SchoolStates`, the attachment decoder, and the client display together. Keep maximum stacks, finisher thresholds, authored application strength, and Rime’s movement penalty explicit. Living Blade rupture and Ferric shatter currently require exactly three stacks. Save and sync currently clamp to three, and a fourth Pressure visual overlaps the first. [SchoolCombatEvents.java:91](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java:91>) · [SchoolCombatState.java:135](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatState.java:135>) · [SchoolStateVisuals.java:57](<C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/client/render/world/SchoolStateVisuals.java:57>)
- **Documentation.** Record the rulings in `MANIPULATIONS_AND_CASTING.md` and update the school combat wiki and reference. If the Temper discount is adopted, update `CORE_LOOP.md` and `CORE_LOOP_TEACHING_LEDGER.md` with the pricing rule.

## Validation

After implementation, run these from the project root.

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat alphaCheck
```

Cover a one-shot kill on A followed by a different school on B, a late projectile from A, and a multi-target cast that earns one player reward. Verify that an unpaid cast keeps the credit, a paid cast consumes it once, and a blocked hit earns nothing. Exercise minor and major reactions on the same target, both heat/cold orders, stacks above three through save and sync, and Hemorath’s Pressure feed alongside Disrupted immunity.

## Unresolved questions

- Whether switching targets with the same school or using a deliberate status-only cast qualifies.
- Whether Necrosis debt or remaining Searing time should influence Edacious depth, and whether Bloodburst and Masquerade should trigger the new bonuses.
- The exact raised caps, reward window, discount size, and reaction effects.
- Which bosses accept which major reactions, and whether Conductive Mark qualifies for the Ferric/Ductilis pair alongside Disrupted.

This proposal was prepared read-only. No project files were changed and no validation commands were run.

## Plan assessments

### Codex

**Decision:** Objections remain.

The proposal captures the intended design: target-local stacks and mixes, a smaller player-side reward that survives a killing blow, and raised caps for eligible boss states. It still needs first-pass combat rules. Specify what Edacious does with the deepest state, what Phantasmal’s minor and major payoffs do, and which payoff wins when an authored finisher or conduction also qualifies. Define which states receive raised caps and whether existing finishers trigger at three stacks or at the new cap. The exact discount size and expiry can remain playtest values. Boss-specific reactions and signature-active integration can remain follow-up decisions.

### Claude

**Decision:** Endorsed this plan version.

Approved. Each of the owner's four answers maps to a concrete first pass. (1) Both: states and stacks stay per target, and a short-lived history on the player rewards changing enemy and school. Killing blows and state-spending finishers count, which closes the gap where a payoff hit applies no new state (SchoolCombatEvents.java:223-224). The reward is limited to once per attack root, so a multi-target cast cannot pay several times. A banked Temper discount is the right default, because a bonus stack only means something for Pressure and Rime (SchoolStates.java:99-103). Applying the discount to the next paid cast matches TemperCharge, which prices a cast before payment and settles it after (TemperCharge.java:18-22). (2) Edacious uses the deepest pre-hit state, excludes Veiled, and has a deterministic tie rule. How Necrosis debt and Searing time count is correctly left open. (3) Phantasmal puts its major reactions on the EnumBloodTemper.opposite() pairs and matches the incoming school against the target's pre-hit states. That correctly catches Flammeus/Congeatio, which never coexist on a target (SchoolStates.java:79-90). The plan also spots that a REACTION hit is cancelled once its root has paid (SchoolCombatEvents.java:84-86). (4) Bosses can reach raised caps, Disrupted keeps its boss exclusion, and the Hemorath feed is described accurately: it is gated by feed() and heals him during Thirst (HemorathEntity.java:318-326). Non-blocking notes for implementation: (a) The cap list leaves out the SchoolHitContext.java:25 clamp of levels to 1-3 and the ruptureDamage clamp in SchoolStateRules.java:11. Only Living Blade and the Ferric shatter test == 3; AbsorptionRiderEffects.java:94 and AbsoluteStillnessManip.java:43 use >= 3 and keep working. (b) The Temper credit has to be a UUID session map, not a field on the blood_temper attachment, to satisfy TemperGuardrailTest. (c) 'Phantasmal reaction' already names the Distorted Presence defender reaction (ARCHITECTURE_STANDARDS.md:198), so the new mechanic needs a separate name. (d) New player-facing text needs en_us keys and probably a Liber mention. Record the rulings under 'Rulings that bind' and delete any matching OPEN_QUESTIONS rows. (e) The boss item 'approve new major reactions per encounter' goes further than the owner's answer, which said nothing about generic reactions on bosses. That is acceptable only because it is listed as unresolved. The remaining open questions are correctly scoped to the owner: whether a same-school or status-only switch counts, and what Glacial Grasp (which calls SchoolStates.apply directly) and the Conductive Mark do.

## Implementation and verification

Discussion only. No implementation or validation commands were run by the implementation stage.

## Shared memory changes
- fact (active, revision 1): Living Blade's Pressure rupture currently requires exactly three Pressure levels, so a fourth level would miss the check.
- preference (active, revision 1): The owner wants school states tracked per target for stacked and mixed bonuses, plus a smaller reward for switching statuses across enemies so fast kills still support the combat loop.
- preference (active, revision 1): Edacious should focus on whichever school state is deepest on the target.
- preference (active, revision 1): Phantasmal should reward free mixing modestly and concentrate major reactions on opposite schools, with room for combinations of three or more states later.
- preference (active, revision 1): The owner leans toward raised stack caps on bosses and is open to future encounter-specific reactions, such as Pressure behaving differently according to Hemorath's state.

## Full request

Which reading? 

States on targets, the player's own pool, or both? 


Id say a bit of both we need to keep track of the states per target for mixed and stacked bonues's when built up but I also think its worth it to reward to some lesser extent maybe in efficency or bonus stacks when swapping status's between targets not just the same target so the player isnt punished for killing enemies too quickly either


Which state does Edacious focus on? 
Whichever is deepest on the target

Opposites or free mixing? Should Phantasmal reactions follow the opposite pairs, or should any mix count?
Free mixing can provide some bonus but major reactions should be mostly on oppisites, potentially allowing for 3+ combos later as well

Bosses: do the raised caps and reactions apply to them? Disrupted already excludes bosses (SchoolStates.java:45).
raised caps probably yea , eventually maybe unique reactions like using pressure on Hemorath does more or less depending on his state etc

---

# Addendum 2026-11-23: Sheolic

Added after the discussion above. Nothing here is built.

## The three sets

| Set | Reads | Feel |
|---|---|---|
| Edacious | Depth: many levels of one state | Power and violence |
| Phantasmal | Breadth: different states together, opposites strongest | Spectacle |
| Sheolic | Count: every level of every state, spent one at a time | Small wards for the wearer |

## Owner rulings

- Sheolic reads state build-ups on enemies and turns them into beneficial effects on the player.
- A stacked state counts one charge per level. Rime at 3 with Lodestone is four charges.
- Spending a charge removes it from the enemy and pays a small bonus by its school. Ferric gives Resistance. Ductilis gives Haste.
- The Conductive Mark counts as the Ductilis charge beside Disrupted.
- Sheolic gets no special Hemorath rule for now.
- The owner listed possible triggers (enemy death, enemy hits player, player hits enemy, a timer, several states at once, an armour button) and left the choice, and the rest of the buff table, to the builder.

## Calls made without the owner

**Charges.** An enemy's charges are the levels of its hostile states added together, plus one for a Conductive Mark. Veiled never counts. Necrosis is one charge whatever debt it holds.

**Triggers.** All need the full set.

| Trigger | Spends |
|---|---|
| A charged enemy hits the wearer | One charge from that enemy |
| A charged enemy dies with the wearer as kill credit, within 16 blocks | All its charges |
| Bastion Stance begins | All charges on every enemy within 8 blocks that the wearer may harm |

- The wearer's own hits never spend a charge. They would strip levels as they are built, and Living Blade's rupture and the Ferric shatter would never reach their threshold.
- There is no timer. It would pay without play and drain setups.
- The harvest is part of Bastion Stance, not a second button. Each set has one active in `ArmorSetAbilityRegistry`, and the Stance's 18 second cooldown limits the harvest.
- A hit the Stance swallows spends nothing.

**Which charge goes first.** When one charge is spent, it is the one whose bonus has the least time left on the wearer. Ties go in this order: Ductilis, Tenebris, Lux, Ferric, Flammeus, Congeatio, Animus, Mortem. Mortem is last because spending Necrosis gives up its banked debt.

**Payout.** Each charge adds 4 seconds to its school's bonus, to a ceiling of 20 seconds. Every bonus stays at level I. More charges mean a longer ward, never a stronger one.

| Charge | Bonus on the wearer |
|---|---|
| Ferric (Lodestone) | Resistance I |
| Ductilis (Disrupted or Conductive Mark) | Haste I |
| Animus (Pressure) | Regeneration I |
| Mortem (Necrosis) | Absorption I, refilled by each charge and never stacked |
| Flammeus (Searing) | Strength I |
| Congeatio (Rime) | Knockback resistance, as a small effect of the mod's own |
| Lux (Illuminated) | Removes one harmful effect from the wearer at once |
| Tenebris (Obscured) | Veiled on the wearer |

The Flammeus bonus is not Fire Resistance because the set already gives that all the time.

**Limits.**

- Spending a charge deals no damage and is not a payoff on any attack root, so it cannot start or block a reaction.
- Bosses give up charges like any other enemy, for the states they accept.
- Crimson Retribution stays as it is. It lays no school state, so the set makes no charges of its own.
- In multiplayer a Sheolic wearer spends levels that another player built. That is accepted for the first pass.

## Where it would live

- A pure `SheolicChargeRules` (the table, 4 and 20 seconds, the tie order) with a unit test, and a `SheolicCharges` service in `common/armor/ability`.
- The hit trigger is called from the set's existing defender-stage hook (`SheolicBastionBloodlustArmorAbilityHandler.onPlayerDamage`). The death trigger must read states before `SchoolCombatEvents.death` clears them at `LOWEST`.
- A level is removed with `SchoolStates.setLevels`, and the mark with `removeEffect`.
- The bonuses are mob effects, so there is no new saved state and no new payload.
- The cue from enemy to wearer uses the mod's own particles.

## Play and tune

The 4 and 20 seconds, the 8 and 16 block ranges, and whether Strength I is too much for a defensive set.