# School combat states

School identity now comes from the registered manipulation, the living weapon at launch, the Will's school, the authored Vesper action, or the equipped Morphling. Color, drops, and the weapon held when a projectile lands do not assign a school.

## Mechanics and counters

Damage numbers are health points (two points equal one heart). These are default durations; longer or shorter explicitly authored durations remain in their attack paths.

| School | State | Behavior | Counters and follow-ups |
| --- | --- | --- | --- |
| Animus | Sanguine Pressure, 6 s | Direct hits add one level, up to three. No passive damage. | Bloodless targets cannot build pressure. Blood Aneurysm consumes it for 2 extra damage per level. A fully cooled Living Blade hit ruptures three levels instead of adding another. |
| Mortem | Necrosis, 8 s | Healing received is reduced by 25%. Stores 25% of subsequent direct health damage, up to 6; absorption is not stored. | Natural expiry releases the bank once. Cleansing discards it safely. Funeral Bell releases it early, scaled by charge. Carrion Communion recognizes it. |
| Ferric | Lodestone, 8 s | Subsequent Ferric hits gain 25% knockback. An owned magnetic pillar redirects that displacement toward itself and pulls marked bodies 1.5 times harder. | Knockback resistance still applies. A mark supplies an iron signature without requiring iron equipment. |
| Flammeus | Searing, 4 s | One attributed crimson burn, 1 damage each second. Refresh extends its duration without restarting its tick clock or adding another stream. | Water, Fire Resistance, and fire immunity counter it. Vitric Combustion consumes remaining scheduled damage, capped at 4, without reigniting. |
| Congeatio | Rime, 6 s | Three levels reduce movement by 15/30/45%. At most one build per victim per ten ticks. | It does not interrupt attacks or use. Wet Glacial Grasp adds two; full Rimebound Sentence establishes three and keeps its enclosure. Maximum Rime can shatter. |
| Ductilis | Disrupted, 10 ticks | Interrupts action/use/channel and prevents restarting briefly. Movement remains available. | Strong authored Paralysis remains separate, capped at 20 ticks for players and 60 for ordinary mobs, followed by recovery. Ordinary Ductilis living-weapon hits establish Conductive Mark instead. |
| Lux | Illuminated, 6 s | Exposes a target through school concealment. Its outline does not consume or replace vanilla Glowing. | Reproof consumes prior Illumination for its 4-versus-2 distinction. White Verdict gains its 1.5 multiplier once against prior Illumination or concealment; consuming hits do not reapply exposure. |
| Tenebris | Obscured / Veiled | Obscured limits reliable perception beyond four blocks. Veiled is the caster's separate concealment. | Mobs retain close defense; Illumination suppresses concealment. Attacking breaks Veiled. Player darkness respects the accessibility intensity setting and imposes no movement/attack penalty. |

Only four new cross-school interactions exist:

- Illumination defeats school concealment while active, without removing an entire darkness zone or unrelated Glowing.
- Searing into Rime removes one Rime level instead of igniting. Rime into Searing extinguishes it instead of adding stiffness. Neither causes an explosion.
- A Ferric hit consumes maximum Rime for 4 extra damage and a shatter cue.
- Lodestone supplies an additional conductive endpoint. Existing Conductive Mark triggers, Lux conduction, relay limits, and discharge budgets remain.

Black Veil and Eclipse Well share the synthetic-darkness query used by shadow movement and ambushes; they do not change world light. Ordinary target goals and Brain targeting both obey awareness. Lost targets leave a fixed last-known investigation point, not a tracker of their subsequent movement. Scripted boss observers keep their encounter targets.

## Damage contract

`SchoolHitContext` captures the ability, primary/secondary tendencies, owner, direct entity, root attack, direct/periodic/reaction kind, application/payoff role, charge, and authored application parameters. Projectiles and summons save their captured context. A summoned body starts a new attack identity on a later swing.

`SchoolDamageSource` preserves the original damage type, source position, attacker and direct projectile. Armor, shields, damage immunity, encounter defenses, and affinity weighting stay in their existing pipelines. Only confirmed health or absorption damage commits a state or consumption. A shielded, canceled, or immune hit cannot build or spend a state. Non-damaging applications use a separate eligibility/effect-immunity path.

The resolver evaluates the pre-hit state, applies ordinary affinity once, and commits after mitigation. Additional fixed state damage is independent of affinity. One victim can receive one state application and one state-generated payoff per root attack. An ability's own payoff takes precedence over shatter/conduction. Periodic damage does not build states; reactions do not trigger another damage reaction. Natural Necrosis expiry is a new timed release, so an arc paid by the original hit cannot swallow it.

Visible status effects identify the state; the entity attachment owns level, meter, owner, expiry and cooldown data. Refresh does not erase stored wounds or reset a burn/build clock. Save/reload resumes with the native effect's remaining duration. Tracking snapshots and subsequent changes synchronize these values, including to a late observer. Expiry, cleansing and death remove matching data.

Blood-volume drain/transfer, vascular damage, Blood Loss, Grave Debt, Insatiable Hunger, and Hemolysis purification remain separate. Healing suppression uses the strongest applicable reduction instead of multiplying penalties. Cauterizing Rebuke, Lumen Suture, and Winter Shroud cures remove their relevant replacement states; Night Vision, mining Haste, and authored defensive trade-offs remain.

## Sanguine Marionette

The Blood Binding family now includes Marionette at mastery three, between Chain and Lattice. It acquires one eligible blood-bearing mob within eight blocks and maintains a mobile tendon for at most eight seconds. It never applies stationary Blood Binding to the moving body.

Every five server ticks, aim resolves out to sixteen blocks. A permitted enemy becomes an attack order; reachable ground becomes a movement order. Invalid aim leaves the previous order intact. Both ordinary goals and Brain-based bodies use their existing navigation, attack behavior and cooldowns.

The caster can control one body across Impressment and Marionette. Players, bosses, bloodless bodies, mobs above 80 maximum health, owned allies/pets, stationary captives, and another caster's controlled body are excluded. Release, interruption, another cast or attack, unpaid blood, excessive separation, death, logout, dimension change, or unload releases the order and restores AI control flags. Natural projectiles and delayed area attacks retain protection for the caster and allies after release.

Base cost is 50 mL per existing one-second channel pulse, including the first, with a six-second base cooldown on ending. Existing mastery/cost modifiers still apply. The weaving recipe consumes a blank memory, Foul Paste, four Animus enzymes, two Ductilis enzymes, and 325 mL. Memory item/model/glyph, localization, Reliquary availability and radial family selection are wired through the existing family registry.

## Registered manipulation source inventory

This table is an inventory of authored assignments, not proof that a combat scenario passed. It was checked against the live registry. **Apply** means an ordinary direct hit applies its primary state; a non-damaging utility does not acquire damage merely because it has school metadata. **Exploit** is the named pre-state follow-up. Deferred periodic/reaction children are **damage only** regardless of the launch role.

Every listed manipulation has its player path. Entity indicates the currently supported `EntityManipulationEffects` path used by non-player casters; Drudge indicates an actual supported action, not the unsupported sentinel. Family forms retain their existing availability restrictions. No additional alternate casting path was enabled merely to fill this table.

| Ability ID | Primary | Secondary | Direct role | Entity | Drudge |
| --- | --- | --- | --- | --- | --- |
| `absolute_stillness` | CONGEATIO | — | Apply | — | — |
| `activation_potential` | DUCTILIS | — | Apply | Yes | Yes |
| `black_veil_covenant` | TENEBRIS | MORTEM | Apply | — | — |
| `blackhearted` | MORTEM | ANIMUS | Apply | — | — |
| `blood_absorption` | FERRIC | ANIMUS | Apply | — | — |
| `blood_aneurysm` | ANIMUS | MORTEM | Exploit | Yes | Yes |
| `blood_binding` | ANIMUS | DUCTILIS | Apply | — | — |
| `blood_cloud` | ANIMUS | MORTEM | Apply | Yes | Yes |
| `blood_eclipse` | TENEBRIS | LUX | Apply | Yes | Yes |
| `blood_eclipse_mantle` | TENEBRIS | FLAMMEUS | Apply | — | Yes |
| `blood_lamp` | LUX | — | Apply | — | Yes |
| `blood_lattice` | ANIMUS | DUCTILIS | Apply | — | — |
| `blood_needle` | ANIMUS | FERRIC | Apply | Yes | Yes |
| `blood_needle_fan` | ANIMUS | FERRIC | Apply | — | — |
| `blood_needle_lance` | ANIMUS | FERRIC | Apply | — | — |
| `blood_projection` | FERRIC | ANIMUS | Apply | — | — |
| `blood_rush` | ANIMUS | FLAMMEUS | Apply | — | Yes |
| `blood_shot` | ANIMUS | — | Apply | Yes | Yes |
| `bloom_of_rot` | MORTEM | ANIMUS | Apply | Yes | Yes |
| `canopy_mortis` | MORTEM | ANIMUS | Apply | — | — |
| `carrion_communion` | MORTEM | — | Apply | — | — |
| `cauterizing_rebuke` | FLAMMEUS | ANIMUS | Apply | — | Yes |
| `chain_blood_binding` | ANIMUS | DUCTILIS | Apply | — | — |
| `conductive_mark` | DUCTILIS | FERRIC | Apply | Yes | Yes |
| `conjure_axe` | MORTEM | FERRIC | Apply | — | — |
| `conjure_blade` | ANIMUS | FERRIC | Apply | — | — |
| `conjure_claws` | TENEBRIS | MORTEM | Apply | — | — |
| `conjure_crossbow` | DUCTILIS | FERRIC | Apply | — | — |
| `conjure_flail` | CONGEATIO | FERRIC | Apply | — | — |
| `conjure_sickle` | MORTEM | FERRIC | Apply | — | — |
| `conjure_spear` | LUX | FERRIC | Apply | — | — |
| `conjure_staff` | FERRIC | ANIMUS | Apply | — | — |
| `conjure_torch` | FLAMMEUS | LUX | Apply | — | — |
| `crimson_coronation` | ANIMUS | — | Apply | — | — |
| `crimson_flame_conjuration` | FLAMMEUS | ANIMUS | Apply | Yes | Yes |
| `crimson_harvest` | DUCTILIS | ANIMUS | Apply | — | Yes |
| `crimson_sight` | LUX | TENEBRIS | Apply | — | Yes |
| `crimson_tithe` | MORTEM | LUX | Apply | — | — |
| `cryogenic_pulse` | CONGEATIO | FLAMMEUS | Apply | Yes | Yes |
| `deadly_gaze` | DUCTILIS | TENEBRIS | Apply | Yes | Yes |
| `eclipse_well` | TENEBRIS | — | Apply | — | — |
| `endless_hour` | CONGEATIO | LUX | Apply | — | Yes |
| `expansive_blood_cloud` | ANIMUS | MORTEM | Apply | — | — |
| `expulsive_updraft` | FLAMMEUS | DUCTILIS | Apply | — | — |
| `exsanguinate` | MORTEM | DUCTILIS | Apply | Yes | Yes |
| `ferric_rampart` | FERRIC | DUCTILIS | Apply | — | — |
| `ferric_resonance` | FERRIC | FLAMMEUS | Apply | — | — |
| `ferric_spikes` | FERRIC | DUCTILIS | Apply | — | — |
| `ferric_transmutation` | FERRIC | DUCTILIS | Apply | — | Yes |
| `funeral_bell` | MORTEM | — | Exploit | — | — |
| `furnace_veins` | FLAMMEUS | — | Apply | — | — |
| `glacial_bastion` | CONGEATIO | FERRIC | Apply | — | Yes |
| `glacial_circulation` | CONGEATIO | ANIMUS | Apply | — | — |
| `glacial_grasp` | CONGEATIO | TENEBRIS | Apply | Yes | Yes |
| `glacial_rampart` | CONGEATIO | FERRIC | Apply | Yes | Yes |
| `gloam_laceration` | TENEBRIS | MORTEM | Apply | Yes | Yes |
| `grave_debt` | MORTEM | ANIMUS | Apply | Yes | Yes |
| `guided_blood_shot` | ANIMUS | — | Apply | — | — |
| `hematic_ballast` | FERRIC | — | Apply | — | — |
| `hematic_beacon` | LUX | ANIMUS | Apply | Yes | Yes |
| `hematic_flare` | LUX | FLAMMEUS | Apply | Yes | Yes |
| `hematic_impressment` | ANIMUS | DUCTILIS | Apply | — | — |
| `hematic_mortar` | ANIMUS | — | Apply | — | — |
| `hematic_rebuke` | ANIMUS | DUCTILIS | Apply | — | — |
| `hemolymphal_pulse` | DUCTILIS | ANIMUS | Apply | Yes | Yes |
| `hemorrhage` | MORTEM | ANIMUS | Apply | Yes | Yes |
| `hemosynthesis` | LUX | ANIMUS | Apply | — | Yes |
| `insatiable_hunger` | MORTEM | ANIMUS | Apply | Yes | Yes |
| `iron_choir` | FERRIC | — | Apply | — | — |
| `iron_retort` | FERRIC | DUCTILIS | Apply | Yes | Yes |
| `ironhearted` | FERRIC | ANIMUS | Apply | — | — |
| `lignum_mortis` | MORTEM | ANIMUS | Apply | — | — |
| `lingering_blood_binding` | ANIMUS | DUCTILIS | Apply | — | — |
| `living_circuit` | DUCTILIS | — | Apply | — | — |
| `lumen_suture` | LUX | DUCTILIS | Apply | — | Yes |
| `osseous_bloom` | CONGEATIO | MORTEM | Apply | — | — |
| `penumbral_drift` | TENEBRIS | — | Apply | — | — |
| `phoenix_debt` | FLAMMEUS | — | Apply | — | — |
| `prismatic_reproof` | LUX | FERRIC | Exploit | Yes | Yes |
| `pursuing_blood_cloud` | ANIMUS | MORTEM | Apply | — | — |
| `pyretic_forge` | FLAMMEUS | FERRIC | Apply | Yes | Yes |
| `rimebound_sentence` | CONGEATIO | — | Apply | — | — |
| `sanguine_excavation` | FERRIC | MORTEM | Apply | — | Yes |
| `sanguine_halo` | ANIMUS | — | Apply | — | — |
| `sanguine_ignition` | FLAMMEUS | ANIMUS | Apply | Yes | Yes |
| `sanguine_magnetism` | FERRIC | DUCTILIS | Apply | Yes | Yes |
| `sanguine_marionette` | ANIMUS | DUCTILIS | Apply | — | — |
| `sanguine_mending` | FERRIC | ANIMUS | Apply | — | Yes |
| `sanguine_tempest` | ANIMUS | MORTEM | Apply | — | — |
| `sanguine_ward` | DUCTILIS | FERRIC | Apply | — | Yes |
| `scalding_updraft` | FLAMMEUS | DUCTILIS | Apply | Yes | Yes |
| `soaring_updraft` | FLAMMEUS | DUCTILIS | Apply | — | — |
| `sovereign_instinct` | ANIMUS | — | Apply | — | — |
| `summon_avatar` | ANIMUS | — | Apply | — | — |
| `summon_avatar_armor` | ANIMUS | — | Apply | — | — |
| `summon_avatar_arms` | ANIMUS | — | Apply | — | — |
| `summon_avatar_complete` | ANIMUS | — | Apply | — | — |
| `summon_avatar_legs` | ANIMUS | — | Apply | — | — |
| `summon_thrall` | ANIMUS | — | Apply | — | — |
| `suspended_updraft` | FLAMMEUS | DUCTILIS | Apply | — | — |
| `synaptic_jolt` | DUCTILIS | — | Apply | Yes | Yes |
| `synaptic_storm` | DUCTILIS | — | Apply | — | — |
| `thread_ripper` | DUCTILIS | TENEBRIS | Apply | — | — |
| `umbral_reversal` | TENEBRIS | ANIMUS | Apply | — | — |
| `umbral_step` | TENEBRIS | DUCTILIS | Apply | Yes | Yes |
| `unclosing_eye` | LUX | MORTEM | Apply | Yes | Yes |
| `vascular_dowsing` | FERRIC | LUX | Apply | — | — |
| `venous_travel` | DUCTILIS | ANIMUS | Apply | — | — |
| `vigil_of_glass` | LUX | — | Apply | — | — |
| `vital_effusion` | ANIMUS | DUCTILIS | Apply | — | Yes |
| `vital_reservoir` | MORTEM | ANIMUS | Apply | — | Yes |
| `vitric_combustion` | FLAMMEUS | — | Exploit | Yes | Yes |
| `void_shroud` | TENEBRIS | MORTEM | Apply | Yes | Yes |
| `white_verdict` | LUX | — | Exploit | — | — |
| `worked_lignum` | MORTEM | ANIMUS | Apply | — | — |

Non-damaging exceptions retain their specific jobs: Binding forms bind; Marionette/Impressment command; Conductive Mark and Living Circuit mark; ward/guard/interception forms defend; travel, mining, healing, crafting and conjuration utility remains utility. Blood Cloud variants carry a periodic damage-only cloud. Crimson Coronation counter-lances, Iron Retort retaliation and Phoenix Debt are reaction damage only.

## Other migrated sources

| Source | Captured school / role |
| --- | --- |
| Living Blade | Animus; apply Pressure, or exploit three levels with a fully cooled hit. |
| Living Axe / Living Sickle | Mortem; apply Necrosis. Hook/projectile attribution retains the real direct entity. |
| Living Spear | Lux; apply Illumination. |
| Living Claws | Tenebris; apply Obscured. |
| Living Crossbow / bolts | Ductilis; establish Conductive Mark on ordinary weapon contact. |
| Living Flail / head impact | Congeatio; apply Rime with authored impact duration. Shared impact/splash identity prevents repeated buildup. |
| Living Torch / breath | Flammeus; apply Searing. No duplicate school-applied vanilla burn stream. |
| Living Staff | Uses its weapon assignment, with captured secondary metadata where present. Holding it does not reschool another spell. |
| Blood Needle, Shot, Bolt, Bullet | Use captured manipulation/weapon metadata, including family form identity. Legacy untyped launches remain untyped rather than taking the current held weapon's school. |
| Blood cloud carrier / blood cloud | Carrier passes the launch identity to its persisted cloud; cloud ticks are periodic damage only. |
| Tracking blood orb, pests, serpent; sickle hook; iron spike | Captured source/owner and actual projectile retained through custom collision damage. |
| Ferric constructs / magnetic pillar | Ferric origins apply Lodestone on damaging strikes; magnetism exploits the mark and preserves construct ownership and terrain collision. |
| Summoned attackers | Retain cast origin/owner; later natural melee attacks receive fresh root identities. |
| Wills | Use the Will's authored school for melee and weapon-controller actions. Ductilis ordinary weapon hits mark; other ordinary school hits apply their primary state. |
| Hemorath | Registered manipulation dispatch retains the chosen ability's authored primary/secondary schools. Unaligned encounter mechanics remain unchanged. |

Vesper actions use their existing `VesperWeaponAction.tendency()`, captured at action start:

| School | Actions | Role |
| --- | --- | --- |
| Animus | Ichimonji, Crosscut | Apply Pressure |
| Mortem | Leaping Cleave, Reaper Sweep | Apply Necrosis |
| Lux | Sky Lance, Lance Flurry | Apply Illumination |
| Tenebris | Twin Rend, Predator Pounce | Apply Obscured |
| Ductilis | Conductive Volley, Storm Lock | Apply their Ductilis state; preserve authored bounded hard control |
| Flammeus | Branding Thrusts, Updraft Impalement, Flammeus Concentration | Apply Searing |
| Congeatio | Chain Sweep, Hook and Crush | Apply Rime |
| Ferric | Magnetic Axis, Iron Retort | Apply Lodestone |

The null-assigned sickle actions and Sanguine Crescents remain untyped. Other boss/fauna/armor attacks without an authored school assignment were not classified from colors, themes, or enzyme drops.

The eight registered Morphlings use these item assignments:

| Morphling | Primary / secondary | School payload |
| --- | --- | --- |
| Deadman's Purse | Animus / Congeatio | Pressure on authored direct damage; blood storage/transfer remains separate |
| Gravecap | Mortem / Animus | Necrosis replaces offensive poison/wither bundles |
| Witch's Ear | Ductilis / Tenebris | Disruption replaces incidental slowing |
| Lumenlace | Lux / Ductilis | Illumination replaces offensive blindness/weakness |
| Bootlace | Tenebris / Lux | Obscured replaces offensive darkness packages |
| Irontooth | Ferric / Mortem | Lodestone replaces incidental offensive impairment |
| Emberfang | Flammeus / Ductilis | Searing replaces its unrelated poison/slow/darkness package |
| Winter Shroud | Congeatio / Ferric | Rime replaces offensive slowing/freezing packages; defensive cold resilience remains |

Active Morphling damage is direct. On-hit/on-hurt secondary damage uses the triggering root and is reaction damage only; periodic/reaction kills retain non-offensive rewards without creating another offensive chain. Winter Shroud, Lumenlace and Irontooth keep their defensive responses to periodic damage; those responses cannot launch another offensive reaction. The legacy Tick, Pests, Urchin and Chitinite classes also use the replacement paths where applicable, but they are not registered current specimens.

## Presentation and verification

Each state has its own status icon and counter tooltip. Body-relative buildup uses the existing authored school materials: pressure droplets, Necrosis wounds, iron signatures, crimson burns, layered Rime, a broken action cue, a Lux halo, and shadow wisps. Marionette has a moving hand-to-body tendon and separate move/attack order markers. These world cues remain visible with Minimal particles.

Dedicated tests cover actual native damage/absorption, raised shields, immunity, captured projectile attribution and weapon switching, affinity, state expiry/refresh/cleanse/save/reload, thermal opposition, rupture/shatter/combustion/exposure, conduction budgets, skeleton ranged perception, melee pursuit, Brain perception, boss continuity, and real Marionette navigation and attacks. Pure JVM rules test numeric caps and channel limits. Source inventory assertions are bookkeeping only, not combat acceptance.

An opt-in disposable harness is in `SchoolCombatClientAcceptance`, configured by `tools/school-combat-acceptance.init.gradle`. Use a fresh `-PschoolRun` directory for each full scene run, precompile once before launching clients, and skip compilation on concurrent client launches to avoid shared Gradle output races. On fresh test profiles, complete Minecraft accessibility onboarding before Quick Play. It never activates in ordinary play. Three real clients (caster, observer, then a late observer) verified matching state owners/meters and empty cleanup snapshots. Minimal particles and darkness intensity 0/1 were exercised. Saved client frames exposed and guided fixes for subtle state cues and the first-person tether anchor. These controlled scenes are not a complete survival-progression or long-session multiplayer soak test.

Verification on 2026-09-12:

- Focused `test --tests '*School*Test' --tests '*SanguineMarionetteRulesTest' runGameTestServer`: passed, including all 386 required GameTests.
- `alphaCheck build`: failed in the repository-wide JVM suite. After updating the remaining obsolete Flare assertion, `alphaCheck build --continue` ran 1,930 JVM tests with 60 failures, then passed all 386 required GameTests, `verifyGameTestResourceLog`, and `assemble`. The overall build is **not green**.
- The remaining JVM failures concern relocated block/tile/item source paths, existing render/resource expectations, two Iron Heart offsets, the Scar Tree layout, Circus entrance geometry, summon-count expectations and the legacy test count (359 versus 361). No school-state or Marionette JVM test is failing. See `build/reports/tests/test/index.html` for all individual failures; these unrelated systems were not rewritten as part of this combat change.
- Whitespace validation for the edited source/resource/test/wiki paths passed. The unscoped Git check encounters an unrelated missing staged `gource.webm` object, which was left untouched.

Client evidence is under `build/school-acceptance3-{Caster,Observer,Late}/screenshots` and the matching logs. The separate `school-acceptance5` run completed the final dominant-hand Marionette anchor check after serial recompilation. Frames 10-35 and 10-50 show the continuous crimson connection following the moving body. Server-driven screenshots sample movement and confirm tracking/cleanup, but do not establish frame-time stability over a long multiplayer session.
