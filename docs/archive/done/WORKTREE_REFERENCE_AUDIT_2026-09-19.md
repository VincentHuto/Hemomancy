# Working-tree documentation audit — 2026-09-19

This snapshot covers Git-visible staged, unstaged, deleted, and untracked paths in the current checkout. It describes the working files, not a committed release. Status is the two-column Git porcelain status; `??` means untracked and `AD` means added to the index but deleted from the working directory. New documentation and local authoring files can be ignored by current rules and are listed separately below. Build outputs, saved worlds, and the entire ignored filesystem are not treated as new mod features.

## Reading map

| Change family | Current documentation and scope |
| --- | --- |
| Deep Dark and audio | [Developer reference](../../HEMOMANCY_REFERENCE.md#antecedent-inquiry-and-audio-analysis), [lore](../../LORE_REFERENCE.md#the-antecedent-the-first-incursion), [player route](../../../wiki/Antecedent-Inquiry.md), [Clairaudiograph](../../../wiki/Clairaudiograph.md): samples, evidence, teachers, recordings, spectral analysis, books, chamber, terrain, fixtures, vessel, talisman and editorial status. |
| Succession and rite helpers | [Developer integration](../../HEMOMANCY_REFERENCE.md#other-working-checkout-integration-details-2026-09-19), [player guide](../../../wiki/Hematic-Succession.md), [configuration](../../../wiki/Configuration.md): authenticated samples, consent, body creation, six phases, resident identities, services, workplaces, restoration, dismissal, automatic helper travel and return. |
| Clinical teaching and discovery | [Clinical progression](../../HEMOMANCY_REFERENCE.md#clinical-blood-tools-knowledge-progression-2026-09-17), [guide](../../../wiki/Clinical-Blood-Tools.md): Alchemist/Artificer/Mnemonist ownership, evidence, rewards, assignments, JEI, Materials, Liber and save recovery. |
| Blood profiles and sampling | [Profile reference](../../../wiki/Blood-Profiles.md): per-entity JSON, whole-file overrides, server synchronization, syringe restrictions, legacy tag removal, current profile lookup, migration fixtures and unchanged injection response IDs. |
| Specimen tools and presentation | [Developer integration](../../HEMOMANCY_REFERENCE.md#other-working-checkout-integration-details-2026-09-19), [microscope](../../../wiki/Hematic-Microscope.md), [Cabinet](../../../wiki/Phlebotomists-Cabinet.md), [Field Case](../../../wiki/Phlebotomists-Field-Case.md), [PNG guide](../../../src/main/resources/assets/hemomancy/textures/gui/microscope/README.md): special specimens, creative defaults/order, rack compatibility, stack limits, reload-safe examination, visible cell alignment and editable sprites/models. |
| Bombardier | [Developer reference](../../HEMOMANCY_REFERENCE.md): nearby-growth spawning/territory, complete looping clips, six-tick transitions, nozzle pitch/geometry and denser flame visuals; associated unit and server fixtures. |
| Other art | [Developer integration](../../HEMOMANCY_REFERENCE.md#other-working-checkout-integration-details-2026-09-19): portraits, Pallid Infusion and Hearty Compass presentation. Asset changes alone do not establish new lore. |
| Shared runtime wiring | Registration, components, network packets, event dispatch, NPC base classes/render integration, reload listeners, sound/loot/mixin lists, translations and test fixtures connect the above features. Shared files may serve several families; the listing assigns one primary home, not exclusive ownership. |
| Build and authoring | Isolated clinical/Antecedent/succession GameTest runs, disposable review clients and observer, tool relocation/deletions, and ignore changes. See [testing](../../TESTING.md). |
| Documentation | Existing and new reference/wiki coverage, including this audit. Historical test results retain their dates and are not recast as fresh passes. |
| Local remnants and absent files | Editor `*~` backups and Python caches are not runtime features. `SpecimenJarData.java` is absent from the working tree despite an index addition; it is not documented as an installed implementation. No remnants were deleted or staged during this audit. |

## Local authoring and drafts outside ordinary Git visibility

The current `.gitignore` broadly excludes docs and tools, with additional local/build exclusions. This audit does not change those rules or stage files. A local file's existence does not establish that it will be included in a commit or release.

The Antecedent generators now reside in `tools/oneoff/antecedent/`. The microscope, Cabinet and Clairaudiograph generators reside in `tools/oneoff/`. Former tracked `tools/model_export/` files are shown as deleted below; where a same-named local replacement exists, documentation links were updated after checking that destination. The retained Java-to-Blockbench converter remains under `tools/model_export/`.

The seventeen Markdown files under `docs/archive/concept/antecedent-fresh-draft/` remain editorial drafts. They have not replaced runtime dialogue, books, captions or recordings. The First Incursion brief, exported texts, rewrite instructions, and fresh drafts are distinct from current packaged assets. See [draft status](antecedent-fresh-draft/README.md) and [local authoring instructions](../../../tools/oneoff/antecedent/README.md).

## Validation boundaries

This documentation audit checks current source/resource facts, local links, relocated paths, and coverage of the dirty-file snapshot. It does not rerun all gameplay suites or claim a release build. Existing dated validation remains in [TESTING](../../TESTING.md). The terrain fix's September 18 run passed all ten Antecedent GameTests, including the twenty-city density comparison. The subsequent brick restoration verified unchanged block positions, air, fixtures and block-entity data, with 2,993 masonry substitutions. Neither result proves a fresh live survival or multiplayer playthrough.

## File coverage

Audit checks passed: all 545 Git-visible paths appear exactly once; the documented 131 blood profiles, 80 syringe-restricted profiles, and seventeen fresh-draft files match disk. All newly introduced local link targets resolve, and the changed documents pass `git diff --check`. A broader scan checked 507 local reference occurrences and found 39 already-broken references in older portions of the developer reference, primarily historical asset illustrations. Those pre-existing references are recorded in `build/documentation-audit-validation.json`; this pass does not claim the entire historical reference is free of stale links. No runtime code or asset was changed for this documentation audit.

The snapshot contains **545 Git-visible paths**. Paths below are repository-relative; deleted paths are retained as historical identifiers.

### Blood profiles and sampling

170 paths.

| Status | Path |
| --- | --- |
| ` M` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/BloodInjectionGameTests.java` |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/BloodProfileGameTests.java` |
| ` D` | `src/gameTest/resources/data/blood_injection_validation/tags/entity_type/blood_properties/test_unknown.json` |
| `??` | `src/gameTest/resources/data/minecraft/blood_profiles/pig.json` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/data/gen/HemoEntityTagProvider.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodInjectionEvents.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodProfileData.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSampleData.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSamplingResult.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSamplingRules.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/EntityBloodProfile.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/TendencyWeaponHelper.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/BloodProfileSyncPacket.java` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/annetta_knowles.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/blood_lantern_jelly.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/bog_revenant.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/brined_votary.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/chalybeate_snail.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/chitinite.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_acrobat.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_carousel.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_fire_eater.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_knife_thrower.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_ringmaster.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/circus_stilt_walker.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/crimson_doe.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/drudge.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/excoriated_sagittary.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/fervent_chitinite.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/fungling.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/gorebound_hulk.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_alchemist.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_artificer.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_cicatrix_anchorite.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_hermit.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_mnemonist.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_vicar.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_votary_wayfarer.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/harbinger_voyager.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/hematic_burrower.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/hematic_construct.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/hemojelly.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/hemorath.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/latent_annetta_infection.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/marrow_spitter.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/mnemonic_whale.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/mnemonist_puppet.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/mycophant.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/phlegethontic_bombardier.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/prism_cuttle.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/putriciel.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/sanguine_hound.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/scarlet_mummer.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/scarlet_serpent.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/seraphae.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/stained_priestess.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/unstained_acolyte.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/unstained_guardian.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/unstained_scout.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/unstained_zealot.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/velorum.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/venom_rib_centipede.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/venous_strider.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/vesper_crowned_refusal.json` |
| `??` | `src/main/resources/data/hemomancy/blood_profiles/vesper_evening_star.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/aquatic.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/arthropod.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/burrowing.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/cold_native.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/ender.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/explosive.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/flying.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/nether_native.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/undead.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/blood_properties/venomous.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/ferric.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/fervent.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/frigid.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/fungal.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/incandescent.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/neurotic.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/ruinous.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/umbral.json` |
| ` D` | `src/main/resources/data/hemomancy/tags/entity_type/vivacious.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/allay.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/armadillo.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/axolotl.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/bat.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/bee.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/blaze.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/bogged.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/breeze.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/camel.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/cat.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/cave_spider.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/chicken.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/cod.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/cow.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/creeper.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/dolphin.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/donkey.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/drowned.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/elder_guardian.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/ender_dragon.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/enderman.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/endermite.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/evoker.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/fox.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/frog.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/ghast.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/glow_squid.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/goat.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/guardian.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/hoglin.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/horse.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/husk.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/illusioner.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/iron_golem.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/llama.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/magma_cube.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/mooshroom.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/mule.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/ocelot.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/panda.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/parrot.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/phantom.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/pig.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/piglin.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/piglin_brute.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/pillager.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/polar_bear.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/pufferfish.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/rabbit.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/ravager.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/salmon.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/sheep.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/shulker.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/silverfish.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/skeleton.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/skeleton_horse.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/sniffer.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/snow_golem.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/spider.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/squid.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/stray.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/strider.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/tadpole.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/trader_llama.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/tropical_fish.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/turtle.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/vex.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/villager.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/vindicator.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/wandering_trader.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/warden.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/witch.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/wither.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/wither_skeleton.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/zoglin.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/zombie.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/zombie_horse.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/zombie_villager.json` |
| `??` | `src/main/resources/data/minecraft/blood_profiles/zombified_piglin.json` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodProfileDataTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodProfileMigrationTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSamplingMessageResourceTest.java` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodSamplingRulesTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/EntityBloodProfileTest.java` |
| `??` | `src/test/resources/blood_profiles/legacy_memberships.json` |
| `??` | `src/test/resources/blood_profiles/restricted_entities.json` |

### Bombardier

13 paths.

| Status | Path |
| --- | --- |
| ` M` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PhlegethonticBombardierGameTests.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/BombardierAnimationBlend.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/PhlegethonticBombardierAnimations.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/PhlegethonticBombardierModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/world/ThermalGeometry.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BombardierFlameGeometry.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BombardierHabitat.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/PhlegethonticBombardier.java` |
| ` M` | `src/main/resources/assets/hemomancy/models/entity/bbmodel/phlegethontic_bombardier.bbmodel` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/BombardierAnimationBlendTest.java` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/PhlegethonticBombardierModelTest.java` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/BombardierFlameGeometryTest.java` |

### Build and authoring

26 paths.

| Status | Path |
| --- | --- |
| ` M` | `.gitignore` |
| ` M` | `build.gradle` |
| ` D` | `tools/model_export/BloodDrunkPuppeteerModel.java` |
| ` D` | `tools/model_export/InspectPhlegethonticLayouts.java` |
| ` D` | `tools/model_export/PUPPET_ASSETS.md` |
| ` D` | `tools/model_export/add_evening_star_hit_animation.mjs` |
| ` D` | `tools/model_export/add_vesper_keyframe_animations.mjs` |
| ` D` | `tools/model_export/add_vesper_weapon_reference_layers.mjs` |
| ` D` | `tools/model_export/author_crowned_mount_arsenal.mjs` |
| ` D` | `tools/model_export/author_crowned_winged_flight.mjs` |
| ` D` | `tools/model_export/author_evening_star_presentation.mjs` |
| ` D` | `tools/model_export/author_puppet_summons.mjs` |
| ` D` | `tools/model_export/bbmodel_to_mojang_animations.mjs` |
| ` D` | `tools/model_export/bbmodel_to_mojang_animations.test.mjs` |
| ` D` | `tools/model_export/compare_phlegethontic_worlds.py` |
| ` D` | `tools/model_export/hematic_microscope.py` |
| ` D` | `tools/model_export/normalize_blockbench_animation_java.mjs` |
| ` D` | `tools/model_export/normalize_blockbench_animation_java.test.mjs` |
| ` D` | `tools/model_export/pack_phlegethontic_ichor.ps1` |
| ` D` | `tools/model_export/polish_crowned_mount_locomotion.mjs` |
| ` D` | `tools/model_export/polish_crowned_vulnerable_animation.mjs` |
| ` D` | `tools/model_export/prepare_phlegethontic_resources.py` |
| ` D` | `tools/model_export/puppet_materials.png` |
| ` D` | `tools/model_export/vesper_bbmodel_animations.test.mjs` |
| ` D` | `tools/model_export/vesper_concept_bbmodel_generator.py` |
| ` D` | `tools/model_export/vesper_weapon_reference_layers.test.mjs` |

### Clinical teaching and discovery

51 paths.

| Status | Path |
| --- | --- |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/ClinicalBloodProgressionGameTests.java` |
| `??` | `src/gameTest/resources/data/clinical_validation/structure/empty.nbt` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsTabController.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/MemoDefinitions.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ClinicalBloodDialogue.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/BodyAnswersAssignment.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/ClinicalBloodKnowledge.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/ClinicalBloodProgress.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/FirstSeparationAssignment.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/PacketSyncClinicalBlood.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/compat/jei/ClinicalRecipeVisibility.java` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_cabinet.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_collected.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_echo_referral.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_examined.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_field_case.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_field_referral.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_injection.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_microscope.json` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_three_sources.json` |
| ` M` | `src/main/resources/data/hemomancy/advancement/hemomancy/first_separation_started.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_cabinet.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_echo_referral.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_field_case.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_field_referral.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_injection.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_microscope.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_microscopy.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_ordered_collection.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_specimen.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/ambergris_cylinder.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/bloody_vial.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/ghastly_alembic.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/hematic_microscope.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/living_syringe.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/phlebotomists_field_case.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/vial_centrifuge.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/vial_rack.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/minecraft/echo_shard.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/minecraft/glass_pane.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/artificer/hemomancy/hematic_microscope.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/artificer/hemomancy/phlebotomists_cabinet.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/artificer/hemomancy/phlebotomists_field_case.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/ambergris_cylinder.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/minecraft/echo_shard.json` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/mission/ClinicalBloodProgressTest.java` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentSourceTest.java` |

### Deep Dark and audio

102 paths.

| Status | Path |
| --- | --- |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/AntecedentClientReview.java` |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/AntecedentGameTests.java` |
| `??` | `src/gameTest/resources/data/antecedent_validation/structure/empty.nbt` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/AmbergrisCylinderRenderer.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/AntecedentVesselRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/ClairaudiographRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/ClairaudiographScreen.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/ClairaudiographSpectrograph.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/sound/AntecedentClientEffects.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/sound/ClairaudiographAnalysis.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/sound/ClairaudiographSounds.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/sound/SpectrogramAnalysis.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/AhaematicColloidItem.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/component/AhaematicSample.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/loot/modifier/AncientRecordingLootModifier.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/component/AncientRecordings.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AntecedentDialogue.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/event/AntecedentEvents.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/AntecedentKnowledge.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/AntecedentObservation.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/AntecedentPlayback.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/AntecedentResearch.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/ListeningScarItem.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/TalismanSocket.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/VigilArchiveBlock.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/VigilArchiveBlockEntity.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vigil/VigilLayout.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/vigil/VigilPlacement.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/VigilSites.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/AmbergrisCylinderItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/ClairaudiographMenu.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/AntecedentEffectPacket.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/network/ClairaudiographSoundPacket.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/PacketAntecedentResearch.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/ClairaudiographBlockEntity.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinAntecedentCity.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinListeningScar.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilAmbience.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilCatalyst.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilFixtureRemoval.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilReservation.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilShrieker.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilTerrain.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinVigilVibrations.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/mixin/core/SinglePoolElementAccessor.java` |
| `??` | `src/main/resources/assets/hemomancy/blockstates/antecedent_vessel.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/antecedent_vessel.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/bbmodel/antecedent_vessel.bbmodel` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_1.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_10.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_2.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_3.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_4.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_5.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_6.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_7.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_8.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/ahaematic_colloid_9.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/antecedent_vessel.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/listening_scar.json` |
| `??` | `src/main/resources/assets/hemomancy/sounds/antecedent/lower_district_survey.ogg` |
| `??` | `src/main/resources/assets/hemomancy/sounds/antecedent/severed_record.ogg` |
| `??` | `src/main/resources/assets/hemomancy/sounds/antecedent/the_quieting.ogg` |
| `??` | `src/main/resources/assets/hemomancy/textures/block/antecedent_colloid.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/colloid_direction.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/colloid_inclusion.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/colloid_signal.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_1.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_10.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_2.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_3.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_4.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_5.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_6.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_7.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_8.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/ahaematic_colloid_9.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/listening_scar.png` |
| `??` | `src/main/resources/data/hemomancy/advancement/hemomancy/clinical_clairaudiograph.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_acoustic_response.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_archive_response.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_controlled_replay.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_crossed_wool_terminus.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_degraded_network.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_epilogue.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_replay_requested.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_sample_analyzed.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_sample_response.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_severed_record_heard.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_vicar_recognition.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/antecedent_vigil_record_read.json` |
| ` M` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clairaudiograph.json` |
| `??` | `src/main/resources/data/hemomancy/books/libersanguinium/the_infection/pages/clinical_clairaudiograph.json` |
| ` M` | `src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/clairaudiograph.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/artificer/hemomancy/clairaudiograph.json` |
| `??` | `src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/clairaudiograph.json` |
| `??` | `src/main/resources/data/hemomancy/loot_modifiers/ancient_recordings.json` |
| `??` | `src/main/resources/data/hemomancy/structure/antecedent/vigil.nbt` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/client/sound/SpectrogramAnalysisTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/AntecedentResearchTest.java` |

### Documentation

16 paths.

| Status | Path |
| --- | --- |
| ` M` | `docs/HEMOMANCY_REFERENCE.md` |
| ` M` | `docs/LORE_REFERENCE.md` |
| ` M` | `docs/TESTING.md` |
| `??` | `wiki/Antecedent-Inquiry.md` |
| `??` | `wiki/Blood-Profiles.md` |
| ` M` | `wiki/Clairaudiograph.md` |
| `??` | `wiki/Clinical-Blood-Tools.md` |
| ` M` | `wiki/Configuration.md` |
| ` M` | `wiki/Harbinger-Path.md` |
| ` M` | `wiki/Hematic-Microscope.md` |
| `??` | `wiki/Hematic-Succession.md` |
| ` M` | `wiki/Home.md` |
| ` M` | `wiki/Injectable-Blood-Vials.md` |
| ` M` | `wiki/Phlebotomists-Cabinet-Validation.md` |
| ` M` | `wiki/Phlebotomists-Cabinet.md` |
| ` M` | `wiki/Phlebotomists-Field-Case.md` |

### Local remnants and absent files

8 paths.

| Status | Path |
| --- | --- |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java~` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/CardinalFocusBlock.java~` |
| `AD` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/SpecimenJarData.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingSickleItem.java~` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/menu/tile/crafting/StillwaterCondenserMenu.java~` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/PlaceStructurePacket.java~` |
| `??` | `tools/model_export/__pycache__/compare_phlegethontic_worlds.cpython-310.pyc` |
| `??` | `tools/model_export/__pycache__/vesper_concept_bbmodel_generator.cpython-310.pyc` |

### Other art

11 paths.

| Status | Path |
| --- | --- |
| ` M` | `src/main/resources/assets/hemomancy/models/item/pallid_infusion.json` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_alchemist/harbinger_alchemist_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_artificer/harbinger_artificer_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_hermit/harbinger_hermit_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_mnemonist/harbinger_mnemonist_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_vicar/harbinger_vicar_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_votary_wayfarer/harbinger_votary_wayfarer_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/harbinger_voyager/harbinger_voyager_portrait.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/item/hearty_compass_renderer.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/item/pallid_infusion.png` |

### Shared runtime wiring

43 paths.

| Status | Path |
| --- | --- |
| ` M` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerAlchemistModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerArtificerModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerCicatrixAnchoriteModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerMnemonistModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/HarbingerVicarModel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerAlchemistRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerArtificerRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerCicatrixAnchoriteRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerMnemonistRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerVicarRenderer.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/HarbingerEquipmentScreen.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueItemInquiryNodes.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerArtificerEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerCicatrixAnchoriteEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/DataComponentInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/LootModifierInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/init/SoundInit.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/menu/HarbingerEquipmentMenu.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/network/dialogue/DialogueOptionPacket.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/testing/DevTestHooks.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/compat/jei/JEIPlugin.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java` |
| ` M` | `src/main/resources/assets/hemomancy/lang/en_us.json` |
| ` M` | `src/main/resources/assets/hemomancy/sounds.json` |
| ` M` | `src/main/resources/data/neoforge/loot_modifiers/global_loot_modifiers.json` |
| ` M` | `src/main/resources/hemomancy.mixins.json` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/common/mission/HarbingerAssignmentStructureSourceTest.java` |

### Specimen tools and presentation

49 paths.

| Status | Path |
| --- | --- |
| ` M` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/HematicMicroscopeGameTests.java` |
| ` M` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/PhlebotomistsCabinetGameTests.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/player/HematicMicroscopeClientState.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/HematicMicroscopeOverlay.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/PhlebotomistsCabinetScreen.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodVialInjectionAnimation.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodVialItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/ConsecratedSyringeItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/HematicMicroscopeItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MicroscopeExamination.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MicroscopeSpecimenVisual.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingSyringeItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/VialRackItem.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/menu/slot/LivingSyringeSlot.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/PhlebotomistsCabinetMenu.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/StartCentrifugeButtonPacket.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/CabinetInspection.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/CabinetStorage.java` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_0.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_2.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_3.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_5.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_6.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/block/phlebotomists_cabinet_specimen_8.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/item/bbmodel/hematic_microscope.bbmodel` |
| ` M` | `src/main/resources/assets/hemomancy/models/item/bloody_vial.json` |
| ` M` | `src/main/resources/assets/hemomancy/models/item/hematic_microscope.json` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/README.md` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/frame.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/plasma.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/progress_fill.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/progress_track.png` |
| ` M` | `src/main/resources/assets/hemomancy/textures/gui/microscope/readout.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemolymph_cell.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemolymph_filament.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemolymph_plasma.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemolymph_spot.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemorath_iron.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/hemorath_void.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/putriciel_ember.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/putriciel_spore.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/seraphae_halo.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/seraphae_mote.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/velorum_crystal.png` |
| `??` | `src/main/resources/assets/hemomancy/textures/gui/microscope/special/velorum_shadow.png` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/client/screen/overlay/MicroscopeSpecialSampleVisualSourceTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/block/CabinetSpecimenLayoutResourceTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/LivingSyringeTooltipSourceTest.java` |

### Succession and rite helpers

56 paths.

| Status | Path |
| --- | --- |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SuccessionClientReview.java` |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SuccessionGameTests.java` |
| `??` | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SuccessionTestFixtures.java` |
| `??` | `src/gameTest/resources/data/succession_validation/structure/empty.nbt` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/SuccessionBrazierLabels.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/SuccessionIdentityLayer.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/SuccessionModelPose.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/item/BloodlinePoolScreen.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/client/screen/item/SuccessionResidentsScreen.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/CardinalRiteOverlay.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/Bloodline.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/rite/ActiveCardinalRite.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteAllyService.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteNpcTravel.java` |
| ` M` | `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BoundMnemonicRemnantItem.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/MisbegottenBehavior.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/ProfessionalHarbingerEntity.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/succession/ResidentsRequestPacket.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/network/succession/ResidentsSnapshotPacket.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/SuccessionDialogue.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/rite/succession/SuccessionEffects.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionLedger.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionProfessions.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionReconciliation.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionResidents.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/rite/succession/SuccessionRites.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/rite/succession/SuccessionSamples.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionSavedData.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionWorkplaces.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessorRecord.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/VacantEffigyBlock.java` |
| `??` | `src/main/java/com/vincenthuto/hemomancy/compat/jei/SuccessionRecipeCategory.java` |
| `??` | `src/main/resources/assets/hemomancy/blockstates/vacant_effigy.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/bbmodel/vacant_effigy.bbmodel` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_0.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_1.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_2.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_3.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_4.json` |
| `??` | `src/main/resources/assets/hemomancy/models/block/vacant_effigy_5.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/bound_mnemonic_remnant.json` |
| `??` | `src/main/resources/assets/hemomancy/models/item/vacant_effigy.json` |
| `??` | `src/main/resources/assets/hemomancy/textures/mob_effect/counterfeit_scar.png` |
| `??` | `src/main/resources/data/hemomancy/loot_table/blocks/vacant_effigy.json` |
| `??` | `src/main/resources/data/hemomancy/recipe/blood_structure/vacant_effigy.json` |
| `??` | `src/main/resources/data/hemomancy/recipe/cardinal_rite/hematic_succession.json` |
| `??` | `src/main/resources/data/hemomancy/recipe/cardinal_rite/mnemonic_restoration.json` |
| `??` | `src/main/resources/data/hemomancy/succession_profession/alchemist.json` |
| `??` | `src/main/resources/data/hemomancy/succession_profession/artificer.json` |
| `??` | `src/main/resources/data/hemomancy/succession_profession/cicatrix_anchorite.json` |
| `??` | `src/main/resources/data/hemomancy/succession_profession/mnemonist.json` |
| `??` | `src/main/resources/data/hemomancy/succession_profession/vicar.json` |
| ` M` | `src/test/java/com/vincenthuto/hemomancy/common/recipe/CardinalRiteProgressionResourceTest.java` |
| `??` | `src/test/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/succession/SuccessionLedgerTest.java` |

