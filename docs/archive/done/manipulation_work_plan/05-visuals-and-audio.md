# 5. Improve casting readability and presentation

[Work-plan index](README.md) · [Original audit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md)

Work after the relevant mechanics stabilize. One named power or family is a patch; a whole phase is not one implementation task.

Implementation pass recorded in [Area 5 results and all 97 active powers](05-visuals-results.md). The completion boxes remain open until the visual acceptance checks below are closed; implementation and captured subsets are distinguished in the results.

## Required visual standard for every manipulation

**The user's requirement is expressive, impressive spellcraft that actually depicts the manipulation.** Readability alone is not enough. Inspect all 97 active powers for mismatches between the promised action and what the player sees; Crimson Coronation is an example, not the limit of this pass.

- Before implementing each effect, state its visible subject, silhouette/material, motion and buildup → release → impact → lingering/expiry sequence. Scale the spectacle to its role and rank. Family variants must visibly express their mechanical differences.
- Use Hemomancy/HutosLib's authored toolkit where it fits. Add or revise custom models, textures, meshes, trails, particles, animation or renderers when the existing tools cannot depict the intended effect. Reuse is not a reason to leave a signature effect looking like a placeholder.
- Random vanilla dust, redstone-like dots, note-like specks, flame clouds or recolored particle bursts do **not** satisfy a spell's central visual identity. Vanilla particles may provide secondary accents when appropriate; the recognizable subject and main motion must carry the effect.
- **Crimson Coronation must summon recognizable bloody swords.** Show blade silhouettes forming into a crown/formation, holding their positions, launching in response to the actual trigger, and striking/dissolving. Visible remaining swords must match available retaliation charges. Red dots, generic needles or a renamed particle cloud are not an acceptable substitute. Use the user's sword direction here in place of the earlier audit's proposed spear shape.
- Apply the same standard elsewhere: White Verdict needs a visible beam; Thread Ripper a visibly severed thread; Vitric Combustion recognizable glass formation/shattering; Eclipse Well a convincing pulling vortex; Phoenix Debt a recognizable rebirth form. Evaluate the entire roster rather than stopping after these examples.
- Preserve authored visuals that already fulfill their concept, but improve weak shape, timing, material or animation even when a renderer technically exists. Icon borders and HUD indicators do not substitute for world-space casting effects.

**Visual completion gate:** capture a short in-game clip or equivalent before/after frame sequence covering buildup, action and expiry. Check first person, third person and remote-client visibility at ordinary combat distance. The effect must look intentional and compelling in motion, not merely emit the expected particle count. Record missing live access as an outstanding visual acceptance check; compilation or source inspection cannot close it. Keep silhouettes readable under combat clutter and reduced particles, with bounded rendering cost.

## M26 — Make aimed casts and charge buildup readable

- [ ] Complete this task and record evidence.

**Scope:** [White Verdict](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#white-verdict), [Deadly Gaze](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#deadly-gaze), [Hematic Flare](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-flare), [Prismatic Reproof](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#prismatic-reproof), [Blood Aneurysm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-aneurysm), [Vitric Combustion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vitric-combustion), [Umbral Step](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-step), [Umbral Reversal](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#umbral-reversal), [Thread Ripper](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#thread-ripper), [Osseous Bloom](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#osseous-bloom), [Rimebound Sentence](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#rimebound-sentence), [Blood Eclipse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-eclipse)

**Depends on:** M02, M08, M11, M16. **Work unit:** One power per patch.

**Do:** Implement one effect at a time, starting with White Verdict: charge buildup, correct line/cone/target indication, release and impact. Add valid destination cues for teleports and an actual severing connection for Thread Ripper. Reuse authored glow/tendril/lightning/slash tools.

**Done when:** First/third-person and remote clients see the same valid target and footprint. Visual reach matches hit tests. Charging remains readable with the existing HUD, particles at reduced settings and ordinary combat clutter.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M27 — Give persistent zones visible boundaries and expiry

- [ ] Complete this task and record evidence.

**Scope:** [Eclipse Well](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#eclipse-well), [Absolute Stillness](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#absolute-stillness), [Hematic Beacon](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-beacon), [Sanguine Magnetism](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-magnetism), [Black Veil Covenant](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#black-veil-covenant), [Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-cloud), [Expansive Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expansive-blood-cloud), [Pursuing Blood Cloud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pursuing-blood-cloud), [Sanguine Tempest](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-tempest), [Glacial Rampart](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-rampart), [Vascular Dowsing](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vascular-dowsing), [Hemolymphal Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemolymphal-pulse)

**Depends on:** M21, M22, M24, M25. **Work unit:** One zone per patch.

**Do:** Start with Well and Stillness: visible center, actual boundary, active behavior and expiry. Then improve rally/pillar/cloud/cover footprints and Dowsing markers. Keep the existing Covenant dome and pillar/cloud models where they already communicate the action.

**Done when:** Players can identify where the effect works and when it ends. Occlusion/particle settings do not erase essential information. Expiry removes associated visual state, with bounded particle/entity counts.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M28 — Show active statuses, debts and reactive readiness

- [ ] Complete this task and record evidence.

**Scope:** [Conductive Mark](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conductive-mark), [Hematic Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-rebuke), [Hematic Impressment](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-impressment), [Sovereign Instinct](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sovereign-instinct), [Sanguine Ward](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ward), [Iron Retort](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#iron-retort), [Ironhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#ironhearted), [Iron Choir](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#iron-choir), [Phoenix Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#phoenix-debt), [Blackhearted](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blackhearted), [Crimson Coronation](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-coronation), [Crimson Tithe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-tithe), [Endless Hour](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#endless-hour), [Hemorrhage](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hemorrhage), [Gloam Laceration](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#gloam-laceration), [Insatiable Hunger](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#insatiable-hunger), [Grave Debt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#grave-debt), [Carrion Communion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#carrion-communion), [Lumen Suture](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lumen-suture), [Cauterizing Rebuke](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cauterizing-rebuke), [Bloom Of Rot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#bloom-of-rot), [Funeral Bell](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#funeral-bell), [Living Circuit](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#living-circuit), [Exsanguinate](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#exsanguinate), [Unclosing Eye](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#unclosing-eye)

**Depends on:** M12, M18, M19, M20, M24. **Work unit:** One status or passive per patch.

**Do:** Add the missing information, not merely more particles: remaining shield/ammunition, control owner, wound/mark duration, borrowed blood, imminent repayment and passive readiness. Preserve existing Ironhearted/Blackhearted HUD elements. Build inward drain/suture and explicit bell/phoenix forms where the audit identifies weak identity.

**Crimson Coronation deliverable:** implement the bloody-sword formation, launch and impact sequence specified above. This is a custom world-space visual task as well as an ammunition/readiness task; a particle-only crown cannot close it.

**Done when:** Each revised effect visibly distinguishes ready, triggered, active and expired states as applicable. Debt/ammunition display agrees with server state. Status cleanup/reload does not leave false indicators.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M29 — Differentiate family variants and basic utility casts

- [ ] Complete this task and record evidence.

**Scope:** [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Guided Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#guided-blood-shot), [Hematic Mortar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-mortar), [Sanguine Halo](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-halo), [Blood Needle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle), [Blood Needle Fan](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-fan), [Blood Needle Lance](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-lance), [Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-binding), [Lingering Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lingering-blood-binding), [Chain Blood Binding](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#chain-blood-binding), [Blood Lattice](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-lattice), [Scalding Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#scalding-updraft), [Soaring Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#soaring-updraft), [Suspended Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#suspended-updraft), [Expulsive Updraft](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#expulsive-updraft), [Glacial Grasp](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#glacial-grasp), [Cryogenic Pulse](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#cryogenic-pulse), [Lignum Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#lignum-mortis), [Canopy Mortis](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#canopy-mortis), [Worked Lignum](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#worked-lignum), [Synaptic Jolt](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-jolt), [Activation Potential](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#activation-potential), [Synaptic Storm](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#synaptic-storm), [Vital Effusion](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#vital-effusion), [Sanguine Mending](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-mending), [Pyretic Forge](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#pyretic-forge), [Crimson Flame Conjuration](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#crimson-flame-conjuration), [Sanguine Ignition](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-ignition), [Furnace Veins](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#furnace-veins), [Blood Rush](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-rush), [Void Shroud](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#void-shroud), [Penumbral Drift](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#penumbral-drift)

**Depends on:** M19–M25. **Work unit:** One family per patch.

**Do:** Use family-specific visual choices: homing trails, mortar mass, fan/lance formation, linking roots, distinct hover/launch phases and selected-harvest marks. Give farm/repair/forge actions clear completion beats. Preserve native family identity and avoid rebuilding functional authored effects.

**Done when:** Variant differences are recognizable without reading a name, while the base form remains legible and useful. Selection marks match actual harvested blocks; effect duration and movement agree with gameplay.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M30 — Polish Avatar and Living Staff presentation

- [ ] Complete this task and record evidence.

**Scope:** [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Summon Avatar Arms](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-arms), [Summon Avatar Armor](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-armor), [Summon Avatar Legs](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-legs), [Summon Avatar Complete](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-complete), [Conjure Staff](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-staff), [Conjure Blade](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-blade), [Conjure Axe](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-axe), [Conjure Spear](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-spear), [Conjure Claws](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-claws), [Conjure Crossbow](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-crossbow), [Conjure Torch](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-torch), [Conjure Flail](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-flail), [Conjure Sickle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#conjure-sickle), [Blood Absorption](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-absorption), [Blood Projection](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-projection)

**Depends on:** M16, M18, M23. **Work unit:** One stage or form per patch.

**Do:** Retain existing models, emergence transitions, weapons and authored effects. Check first-person cameras, stage/armor clipping, reach contacts, paired-hand attacks, flail release/return, torch windup and staff reversion. Add receiver/completion feedback to absorption/projection where missing.

**Done when:** Each form/stage is recognizable and synchronized with its action in first and third person and on a remote client. Stored staff state survives visual transitions. No unrelated asset redesign is required.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.

## M31 — Correct cast and upkeep audio timing

- [ ] Complete this task and record evidence.

**Scope:** [Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-shot), [Guided Blood Shot](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#guided-blood-shot), [Hematic Mortar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#hematic-mortar), [Sanguine Halo](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#sanguine-halo), [Blood Needle](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle), [Blood Needle Fan](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-fan), [Blood Needle Lance](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#blood-needle-lance), [Summon Avatar](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar), [Summon Avatar Arms](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-arms), [Summon Avatar Armor](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-armor), [Summon Avatar Legs](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-legs), [Summon Avatar Complete](../MANIPULATION_GAMEPLAY_BALANCE_VISUAL_AUDIT.md#summon-avatar-complete)

**Depends on:** M04, M12. **Work unit:** Focused audio/network pass.

**Do:** Audit fallback mappings and explicit sound calls for all current powers, starting with these missing variants and Avatar upkeep. Separate initial activation from periodic payment and release/impact. Verify server-to-client delivery for proxy-driven effects and avoid double playback.

**Done when:** Each intended activation/release sound fires once. Passive upkeep does not replay a full summon cue each second. Family variants have appropriate cues, and remote clients hear the correct event.

**Evidence when closed:** changed paths; focused check/result; live check/result or remaining limitation; chosen values if tuned.
