# 4. Tune powers in meaningful comparison groups

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

These are design/tuning tickets, not automatic defect claims. Set a concrete role and measured target before changing values; keep lower-tier alternatives useful.

Server implementation and source comparisons are recorded in the [area 4 results](04-balance-results.md). M18–M25 retain the live acceptance checks below; the checkboxes do not imply that no code has landed.

## M18 — Balance defensive choices

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Sanguine Ward](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ward), [Ironhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#ironhearted), [Iron Retort](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#iron-retort), [Iron Choir](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#iron-choir), [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Summon Avatar Armor](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-armor), [Summon Avatar Complete](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-complete), [Endless Hour](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#endless-hour), [Phoenix Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#phoenix-debt)

**Depends on:** M04, M05, M12. **Work unit:** Several tuning patches.

**Do:** Compare shield HP, mitigation duration, preparation time, reactive reliability and total blood per encounter after lifecycle fixes. Establish distinct roles: sustained ward, stored protection, timed retaliation, projectile defense and premium transformation/revival. Tune one comparison at a time.

**Done when:** A short comparison table shows when each option is worth a slot and its acquisition burden. Ironhearted has a defensible advantage over Ward; Iron Choir is measured against real volleys. Values are tested under the shared cooldown and ordinary blood reserves.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M19 — Balance healing and cleansing

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Blood Rush](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-rush), [Carrion Communion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#carrion-communion), [Lumen Suture](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lumen-suture), [Cauterizing Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cauterizing-rebuke), [Hematic Beacon](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-beacon)

**Depends on:** M03, M07, M10. **Work unit:** Focused tuning batch.

**Do:** Compare HP restored, protection granted, cleanses, setup targets, self-damage and blood over the same encounter window. Preserve Carrion as conditional drain and Cauterizing as a specialized cleanse; avoid making every option interchangeable regeneration.

**Done when:** Each has a useful circumstance after Blood Rush correction. Report actual HP/s, total healing and costs, including absorption separately. Verify cleanse-only actions do not imply nonexistent attacks.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M20 — Balance blood projectiles, bleed and retaliation

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Guided Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#guided-blood-shot), [Hematic Mortar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-mortar), [Sanguine Halo](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-halo), [Blood Needle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle), [Blood Needle Fan](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-fan), [Blood Needle Lance](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-lance), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Crimson Coronation](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-coronation), [Blood Aneurysm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-aneurysm)

**Depends on:** M02, M07, M08, M09. **Work unit:** Several family tuning patches.

**Do:** Measure hit rate, damage actually accepted, bleed duration, target count and control for comparable blood budgets. Give Hemorrhage a reason to exist beside Shot and Coronation a reason to cost more than Halo. Preserve guided/fan/lance as purposeful choices.

**Done when:** No cheap partial-charge exploit drives the result. Damage estimates account for immunity frames and overkill. Each memory premium buys a concrete advantage or has its price adjusted.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M21 — Balance control against ordinary enemies and bosses

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-binding), [Lingering Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lingering-blood-binding), [Chain Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#chain-blood-binding), [Blood Lattice](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-lattice), [Glacial Grasp](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-grasp), [Cryogenic Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cryogenic-pulse), [Rimebound Sentence](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#rimebound-sentence), [Absolute Stillness](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#absolute-stillness), [Sanguine Magnetism](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-magnetism), [Synaptic Jolt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-jolt), [Activation Potential](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#activation-potential), [Synaptic Storm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-storm), [Hematic Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-rebuke), [Hematic Impressment](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-impressment), [Sovereign Instinct](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sovereign-instinct)

**Depends on:** M06, M08, M09, M12. **Work unit:** Several control-family patches.

**Do:** Compare reach, affected count, duration, escape conditions and boss reductions. Measure fast projectile crossing through Stillness instead of assuming constant protection. Keep cage, root, pull and command distinct; tune repeated lockdown and premium control value.

**Done when:** Ordinary enemies remain controllable while bosses retain meaningful resistance. Late control offers a demonstrable role beyond cheaper roots. Projectile response matches its stated cadence.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M22 — Balance stealth, revelation and shadow escape

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Void Shroud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#void-shroud), [Penumbral Drift](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#penumbral-drift), [Umbral Step](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-step), [Umbral Reversal](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-reversal), [Black Veil Covenant](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#black-veil-covenant), [Blood Eclipse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-eclipse), [Eclipse Well](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#eclipse-well), [Gloam Laceration](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#gloam-laceration), [Hematic Flare](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-flare), [Prismatic Reproof](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#prismatic-reproof), [Unclosing Eye](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#unclosing-eye)

**Depends on:** M07, M08, M09, M11. **Work unit:** Several tuning patches.

**Do:** Compare duration per blood, disengagement reliability, attack breakage, darkness setup and anti-stealth counters. Preserve Drift only if repeated aggro clearing provides distinct value; price the covenant and saint reveal against acquisition effort.

**Done when:** The player can explain when Drift beats Shroud and when Reversal beats Step. Teleport destinations are valid, stealth counterplay works, and Well remains useful without being misrepresented as damage.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M23 — Balance fire offense and traversal

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Crimson Flame Conjuration](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-flame-conjuration), [Sanguine Ignition](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ignition), [Vitric Combustion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vitric-combustion), [Furnace Veins](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#furnace-veins), [Scalding Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#scalding-updraft), [Soaring Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#soaring-updraft), [Suspended Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#suspended-updraft), [Expulsive Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expulsive-updraft), [Conjure Torch](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-torch)

**Depends on:** M02, M09, M16. **Work unit:** Several tuning patches.

**Do:** Compare aimed torch breath, close bursts, heavy charged blast and sustained support heat using actual enemy coverage and movement constraints. Measure Updraft terrain reach/airtime and make each form worth its additional cost.

**Done when:** Furnace has a clear support/offense role relative to cheaper Torch/Ignition. Vitric is competitive after Mortar correction. Updraft variants differ in practical movement/control, with safe allied handling.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M24 — Balance combo setup and saint rewards

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Conductive Mark](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conductive-mark), [Living Circuit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#living-circuit), [Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-cloud), [Expansive Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expansive-blood-cloud), [Pursuing Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pursuing-blood-cloud), [Sanguine Tempest](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-tempest), [Insatiable Hunger](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#insatiable-hunger), [Grave Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#grave-debt), [Funeral Bell](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#funeral-bell), [Bloom Of Rot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#bloom-of-rot), [Crimson Tithe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-tithe)

**Depends on:** M01, M02, M08, M10, M11. **Work unit:** Several combo-focused patches.

**Do:** Measure setup blood plus payoff blood for conductive, storm and debuff/finisher combinations. Account for shared cooldown when a setup must precede a finisher. Establish a worthwhile use for the Tithe net liquidity/debt rather than balancing its nominal deposit in isolation.

**Done when:** Combo windows are practically usable and rewards justify preparation. Tithe has a documented net-benefit scenario with visible risk. Mark/death refunds cannot create loops through rejected damage or repeated triggers.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M25 — Balance and finish noncombat utility

- [x] Implement the server balance pass and record [comparison evidence](04-balance-results.md).
- [ ] Complete the live comparisons described below and in M32.

**Scope:** [Vital Effusion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vital-effusion), [Sanguine Mending](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-mending), [Vascular Dowsing](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vascular-dowsing), [Hemolymphal Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemolymphal-pulse), [Pyretic Forge](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pyretic-forge), [Glacial Rampart](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-rampart), [Lignum Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lignum-mortis), [Canopy Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#canopy-mortis), [Worked Lignum](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#worked-lignum)

**Depends on:** M08, M11. **Work unit:** One utility per patch.

**Do:** Measure growth, repairs, scans, smelting, temporary cover and harvesting per blood/time. Fix Forge multi-output handling if confirmed. Verify Dowsing ore coverage and underground visibility. Show Lignum selection completion so holding after useful work is not silently wasteful; describe Canopy as traversal rather than leaf clearing.

**Done when:** Every utility has a measured successful result and a clear stop/failure state. Long shared cooldowns are justified or adjusted. Temporary blocks restore safely, harvesting respects permissions, and useful Dowsing information can actually be seen.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
