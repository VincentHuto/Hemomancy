# Noetic Manipulation Family Audit

This is the disposition of the 91 manipulation IDs that existed before staged mastery forms were added. The 20 new form IDs are deliberately excluded from the count.

| Classification | Count | Meaning |
| --- | ---: | --- |
| Singleton | 8 | A self-contained manipulation. It still receives mastery efficiency, but has no shared-form family. |
| Family candidate | 16 | Similar executions that belong behind one baseline node. Seven families are implemented now; Living Armament remains a later migration candidate. |
| Lineage-only | 53 | Related through progression, tendency, or follow-up mechanics, but mechanically distinct enough to keep independent mastery. |
| Retired | 14 | Compatibility-only registry IDs already suppressed by `ManipulationRetirementRules`. |
| **Total** | **91** | Every pre-change registry ID appears exactly once below. |

## Singletons

- `venous_travel`
- `blood_rush`
- `vital_effusion`
- `sanguine_ward`
- `blood_absorption`
- `blood_projection`
- `sanguine_mending`
- `vascular_dowsing`

## Family candidates

Implemented in this pass:

- `blood_binding` -> Lingering, Chain, and Lattice forms
- `blood_needle` -> Fan and Lance forms
- `blood_shot` -> Guided, Mortar, and Halo forms
- `blood_cloud` -> Expansive, Pursuing, and Tempest forms
- `scalding_updraft` -> Soaring, Suspended, and Expulsive forms
- `lignum_mortis` -> Canopy and Worked forms
- `summon_avatar` -> Arms, Armor, Legs, and Complete forms

Deferred candidate, with no ID, save, recipe, or tree migration in this pass:

- `conjure_staff`
- `conjure_blade`
- `conjure_axe`
- `conjure_spear`
- `conjure_claws`
- `conjure_crossbow`
- `conjure_torch`
- `conjure_flail`
- `conjure_sickle`

The Living Armament entries are the clearest remaining duplication, but they also carry authored weapon identities and compatibility contracts. They should only become forms after their acquisition and equipped-state migration is designed explicitly.

## Lineage-only

These groups retain ordinary tree parent links. They do not share mastery, unlock one another, or collapse into one node.

| Lineage | IDs | Why they remain separate |
| --- | --- | --- |
| Animus authority | `blood_aneurysm`, `deadly_gaze`, `hematic_rebuke`, `hematic_impressment`, `sovereign_instinct`, `crimson_coronation` | Rupture, lift/slam, fear, control, instinct, and coronation are different actions rather than stronger cast profiles. |
| Neural conduction | `activation_potential`, `synaptic_jolt`, `conductive_mark`, `hemolymphal_pulse`, `living_circuit`, `synaptic_storm` | Their mark, setup, detection, and payoff roles form a combo chain. |
| Ferric defense | `iron_retort`, `ironhearted`, `sanguine_magnetism`, `iron_choir` | Guarding, temporary hearts, forced positioning, and capstone offense keep separate resource and timing contracts. |
| Lux judgment | `hematic_flare`, `hematic_beacon`, `lumen_suture`, `prismatic_reproof`, `unclosing_eye`, `white_verdict` | Illumination, rally support, healing, cone control, detection, and judgment are distinct roles. |
| Glacial discipline | `glacial_grasp`, `cryogenic_pulse`, `glacial_rampart`, `osseous_bloom`, `endless_hour`, `rimebound_sentence`, `absolute_stillness` | Target freezing, pulses, construction, health checks, delayed damage, and capstones should stay separately selectable. |
| Flammeus craft | `crimson_flame_conjuration`, `sanguine_ignition`, `vitric_combustion`, `cauterizing_rebuke`, `pyretic_forge`, `furnace_veins`, `phoenix_debt` | Placement, ignition, detonation, cleansing, smelting, passive heat, and revival are not tier replacements. |
| Umbral practice | `void_shroud`, `gloam_laceration`, `blood_eclipse`, `black_veil_covenant`, `umbral_step`, `umbral_reversal`, `penumbral_drift`, `eclipse_well` | Concealment, attack, debuff, defense, movement, reversal, drift, and area control remain independent tools. |
| Mortem practice | `hemorrhage`, `exsanguinate`, `insatiable_hunger`, `grave_debt`, `blackhearted`, `crimson_tithe`, `bloom_of_rot`, `carrion_communion`, `funeral_bell` | Blood Loss setup, execution, recovery denial, death marks, passive conversion, reserve economy, rot, communion, and capstone payoff form a lineage rather than one cast family. |

## Retired compatibility IDs

- `blood_lamp`
- `crimson_harvest`
- `hemosynthesis`
- `vital_reservoir`
- `sanguine_excavation`
- `ferric_resonance`
- `glacial_bastion`
- `blood_eclipse_mantle`
- `crimson_sight`
- `glacial_circulation`
- `ferric_transmutation`
- `vigil_of_glass`
- `hematic_ballast`
- `summon_thrall`

Retirement behavior is unchanged. This mastery pass does not resurrect, remap, or silently replace any retired ID.
