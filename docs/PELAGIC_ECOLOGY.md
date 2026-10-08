# Pelagic ecological descent

The seven Pelagic biomes support eight ecological communities. Exposed coastal stone and enclosed tide pools share Rockpool Shore. This population pass keeps the existing bathymetry, fossils and mineral chimneys.

| Community | Inhabitants and behavior |
|---|---|
| Exposed rocky shore | Hematic Algal Crust forms wet, irregular stone patches. Chitons graze without removing the rock or crust and clamp against approaching players or attacks. |
| Tide pools | Hemolymphopoda crawl along shallow margins and bottoms. Waterlogged Tidepool Anemones sway, retract and mildly sting on contact. |
| Erythrocoral Reef | Barbed Urchins favor the bottom among existing coral and ordinary reef fish. Cuttles, lantern jellies and whales are sparse visitors. |
| Open Pelagic Ocean | Fast Pelagic Herring travel in spawn packs of 6–10. Single or paired hollow Pyrosomes carry traveling pulses. Mnemonic Whales cruise open water and may hunt toward Twilight. |
| Twilight Ocean | Camouflaging Prism Cuttles favor the gullies. Single or paired Siphonophores trail feeding filaments that catch nearby small fish. |
| Midnight Ocean | Blood Lantern Jellies retain their pulsing bells. Bloody-Belly Comb Jellies have transparent lobes, red interiors and moving comb highlights. |
| Carrion Depths | Vampire Squids cruise above the floor. Bone Worm colonies grow on existing fossil bone. Hagfish forage along the bottom near these sites, eat hand-offered rotten flesh and briefly slime attackers before retreating. |
| Hydrothermal Depths | Vampire Squids share the deep water column. Chalybeate Snails spawn naturally near mineral chimneys. Two-block Giant Tube Worm colonies occupy submerged mineral support near magma and retract red plumes into pale tubes. |

## Collection

All seven new creatures support Specimen Jar capture, placement, pickup, release and repeated capture. Stored creature NBT preserves names, health and behavior timers. Captured creatures retain persistence; releasing an older filled jar also protects its occupant from natural distance despawning. Ordinary natural populations use native mob caps and despawning. The Living Bestiary records and accepts all seven through its existing specimen surrender reward. Vampire Squids drop 1–2 shared Barbed Splinters on player kills, with the existing Looting bonus; the other six new animals retain their empty kill tables. No extra processing chain is added.

| Creature | Blood tendency | Sampling |
|---|---|---|
| Chiton | Ferric | Living Syringe required |
| Pyrosome | Animus | Ordinary |
| Pelagic Herring | Ductilis | Ordinary |
| Siphonophore | Congeatio | Ordinary |
| Bloody-Belly Comb Jelly | Tenebris | Ordinary |
| Hagfish | Mortem | Ordinary |
| Vampire Squid | Tenebris, aquatic property | Ordinary |

Existing creatures keep their existing blood profiles. Environmental affiliation does not replace biological tendencies.

Shears collect the four growths as placeable specimens. Bone Worm and Giant Tube Worm colonies have a `worms=2..5` blockstate. One placed colony item starts with two worms; using another colony item on it adds one worm, up to five. Sneaking bypasses stacking. Shearing returns one through four colony items, matching the items used to build that size, and restores the source water. Generated fossil and vent colonies choose a coordinate-deterministic count from two through five. Existing colonies without the new property load at the default of two; there is no retrogen. Bone Worms require bone support. Giant Tube Worms require two source-water cells over mineral support near magma; their halves share the count, and adding to either half preserves retraction and the two-block height. Shearing either half returns the invested items once for the whole colony and restores both water cells. Placing or harvesting growths preserves support blocks.

## Vampire Squid

Vampire Squids spawn in single or paired packs only in Carrion and Hydrothermal Depths, between Y-53 and Y-6, using the native water-ambient cap. They need clear water around their webbed arms but no bone or vent substrate. Both depth bands remain comfortable swimming habitat. Their compact crimson mantle, eight four-joint arms, inward-facing cirri and webbing borrow Naeglerophaeon's radial silhouette and traveling curl. Damage triggers a three-second arm cloak and a gentle retreat, with a five-second cooldown; they have no attack goals.

The Barbed Splinter shares `hemomancy:calcified_blood_spine` with Barbed Urchins. Only its English display name changes, so saved stacks, loot and recipes retain their registry identity. Author the squid through `tools/model_export/vampire_squid.py`; it exports `VampireSquidMesh.java`, a matching atlas, and `models/entity/bbmodel/pelagic/vampire_squid.bbmodel` with editable swim/cloak previews. Runtime animation resets the connected rig every frame.

Comb Jellies and Vampire Squids tilt their bodies toward actual three-dimensional swimming movement, including climbs, dives and squid retreats. Heading and lean are smoothed per animal, with shortest-path turns and interpolation between ticks. Nearly stationary swimmers ease upright; unticked Specimen Jar display copies retain a neutral pose. The comb highlights, lobes, mantle and connected arms rotate with their parent body.

## Population and world generation

`PelagicHabitatRules` defines depth windows and neighboring overlap. `PelagicHabitat` checks loaded chunks, water clearance, bottom distance, wet stone, fossil bone and vent heat. Movement targets prefer suitable local habitat but permit travel outside the home biome. No creature is teleported back across a biome boundary.

`PelagicPopulation` and the biome JSON resources describe the same spawn packs. The reef's former cuttle modifier entry is replaced by one low-weight direct entry; its vanilla-biome modifier members remain intact. Existing vanilla fauna habitats remain available. Hydrothermal snails use normal spawning; no chunk-load hook creates persistent inhabitants. The existing `deep_ocean_vent` feature now generates exclusively in Hydrothermal Depths through `deep_ocean_vent_spawnlist`, retaining its one-in-96-chunk placement attempts, mineral aprons, chimneys and one-time persistent snail clusters. It no longer generates in Erythrocoral Reef or the former vanilla deep-ocean targets. Already generated vents and inhabitants remain untouched; no retrogen or chunk-load refill is added.

Chitons, Hagfish and Chalybeate Snails share the native `hemomancy:pelagic_benthos` mob category, with a cap of 12 per player-sized spawning area, normal local/global cap accounting and ordinary despawning. This reserves room for animals restricted to narrow bottom surfaces without slowing them to the land-animal spawn cadence. Pyrosomes use their own `hemomancy:pyrosomes` category with a cap of 8 and the same native accounting, ordinary spawn cadence and despawning: whales, squid, cuttles and siphonophores cannot fill their capacity. They spawn in Pelagic Ocean in groups of 1–2 and as singles in the upper Twilight overlap, at Y28–59 with clear water at their position and directly above. Their independent category makes spawn weights relative only to other Pyrosome entries; Twilight visitors are limited by the narrower depth overlap and singleton packs. Fish and other jellies retain the existing aquatic caps. These categories use [NeoForge's 1.21.1 enum extension mechanism](https://docs.neoforged.net/docs/1.21.1/advanced/extensibleenums/); there is no separate population manager.

`PelagicHabitatFeature` runs after geology, writes only inside its owning chunk, and places on actual pool floors, fossil bones and heated aprons. It never generates replacement fossils or chimneys. Growth generation is coordinate deterministic and does not depend on entity state. New fauna can populate existing suitable chunks. Growth generation affects new chunks; existing chunks receive growths through player placement, without retrogen.

Whale prey remain squid, glow squid and Prism Cuttles. Nonlethal blood sampling, bottle sampling and ordinary prey loot are preserved. Each successful lethal prey bite yields exactly one ambergris, independently of the bottle/shedding cooldown. Cuttles and lantern jellies honor `NoAI`, which keeps deliberately immobilized fixtures stationary. Jar-released cuttles remain persistent, so a distant player cannot cause a captured prey specimen to despawn before a whale reaches it.

## Authoring and verification

`tools/model_export/pelagic_ecology.py` authors nine connected rigs on a quarter-unit grid, matching UV atlases and editable projects under `assets/hemomancy/models/entity/bbmodel/pelagic`. Textures are preserved unless `--textures` is supplied. Runtime animation is in `PelagicCreatureModel`; it resets poses each frame. The creatures use native translucent and emissive rendering and the existing authored effect toolkit.

Focused commands: `./gradlew.bat test --tests '*Pelagic*' --tests '*SpecimenJarRendererFitTest'`, `./gradlew.bat runPelagicEcologyGameTestServer`, and `./gradlew.bat runMnemonicWhaleGameTestServer`.

Initial population acceptance recorded on 2026-10-05:

| Check | Result |
|---|---|
| Habitat boundaries, loaded chunk edges, physical clearance and substrate | Passed pure predicates and loaded placement/target checks, including shallow pools and absent fossil/vent support |
| Focused ecology GameTests | All 14 passed: native caps, bottom association, Twilight steering, schooling, defensive reactions, feeding, colony retraction/harvest, water preservation, blood profiles and collection |
| Whale GameTests | All 17 passed, including both previously failing feeding cases, ordinary prey loot, exactly one ambergris per kill, and legacy jar persistence |
| Natural populations, seeds 42 / 1337 / 8675309 | All seven home communities observed in each seed, with neighboring visitors and all four generated growth types |
| Actual vent chunk unload/reload | Three cycles per seed, 25 chunks per cycle; the same 44 / 43 / 43 inhabitants and UUIDs returned each time, without accumulation |
| Terrain regression | All 72 sampled geometry and biome hashes match both reverse generation order and the pre-ecology terrain-foundation reports |
| Native client | Reviewed 19 scenes, each lit and without night vision, with no shader packs: six creatures, defensive states, colonies/retraction, a moving school and all six occupied jars |
| Full JVM suite | 2,446 run; 2,443 passed and three unrelated failures remain |
| Full dedicated GameTest suite | 743 run; 740 passed and three unrelated failures remain; all ecology and whale tests passed together |
| Compilation, assembly and packaged resources | JAR assembled; asset paths, PNG headers and font license passed. Overall `alphaCheck` and `build` remain red because of the failures below |

Population probes use a registered observer, native local/global caps, shuffled chunk order, one spawning pass per ordinary AI tick and 1,000 ticks per cohort. Each site received 4–11 independent cohorts; the GameTest server runs these without real-time pacing. Home/neighbor counts use the biome at first observation, including the other depth zones within a site's water column. Every site's final natural population had zero persistent inhabitants. Population counts fluctuate and spawning packs can overshoot native caps; these runs establish bounded native spawning and the recorded reload identities, not a constant population size. Only the final vent cohort was frozen during the unload/reload comparison so migration would not count as loss or duplication.

The visual scenes are deliberately placed fixtures, separate from natural-spawn evidence. The final depth review places Siphonophores at Y18, Comb Jellies at Y-8, carrion inhabitants at Y-24 and tube colonies at Y-44. Both extended and withdrawn plumes were inspected, along with translucent bodies, emissive highlights, articulated movement and jar fit. The gallery is `build/pelagic-ecology/native-review.html`; captures are under `build/pelagic-client/screenshots/ecology-*.png`.

The whale investigation found two causes: Cuttles with disabled AI still applied swimming motion, and captured older species lacked protection from distance despawning. Cuttle and Lantern Jelly travel now respects disabled AI. Capturing or releasing any specimen Mob preserves persistence, including older filled jars. A loaded wild-animal control still despawns normally. Whale prey selection, lethal reward and bottle cooldown logic remain intact. A separate hagfish fixture failure reproduced normal despawning around an unregistered mock player; only its controlled test actors were made persistent, and its retreat assertion checks movement away from the attacker.

Finished decoration remains order dependent in 18 / 15 / 17 sampled patches for the three seeds, including existing reef and vanilla decoration. The matching terrain and biome hashes do not imply identical finished blocks everywhere. Growth generation remains confined to its owning chunk and uses coordinate-based placement.

Seed reports are `build/pelagic-worlds/<seed>-ecology-shuffled/pelagic-ecology-report.json`, with reverse terrain reports under `<seed>-ecology-reverse`. `build/pelagic-ecology/terrain-regression.json` records both terrain comparisons. To repeat population acceptance, run `./gradlew.bat runPelagicValidationServer -PpelagicSeed=42 -PpelagicRun=<new-name> -PpelagicEcology=true` for each seed; use a fresh run name. Focused and broad gate output is retained in `build/pelagic-ecology/final-verification.log`; earlier whale RED/GREEN evidence is in `jar-persistence-red.log` and `jar-persistence-green.log` in the same directory.

The final gate command was `./gradlew.bat runPelagicEcologyGameTestServer test runGameTestServer alphaCheck build --continue --no-parallel --console=plain`. Remaining failures were present before this ecology work:

- Two `GameTestHarnessSourceContractTest` checks cannot decode the existing invalid UTF-8 byte in `docs/TESTING.md`; the byte also exists in `HEAD`.
- `MorphlingLumenlaceRenameResourceTest` finds the historical name in an ignored `docs/OrchestratorReports` discussion archive.
- `fullCorpusBindsOnDedicatedServer` expects 81 Liber entries but loads 82.
- `blockedCentrifugeCompletionCannotProduceFreeBloodOrPowder` and `cowAndGoatBatchesConserveContainersThroughReload` fail their material-accounting assertions.

These unrelated files, station behavior and historical archives were preserved. Focused ecology acceptance is green; this is not a claim that the repository's release gates pass.

### Bone worm count follow-up, 2026-10-05

The added colony test first failed because the `worms` state was absent, then all 15 ecology GameTests passed with stacking, the five-worm limit, sneak bypass, exact item consumption/refunds, shears-only collection and water/support preservation. Fresh seed-42 generation found 4 two-worm, 4 three-worm, 10 four-worm and 8 five-worm colonies in the sampled fossil areas. All 24 sampled geometry and biome hashes still match the preceding ecology report. Five additional native-client scenes verified each size and all four sizes together, in lit and dark water; screenshots are `ecology-19` through `ecology-23` under `build/pelagic-client/screenshots`.

The follow-up broad run completed 2,446 JVM tests with the same three failures listed above, and 744 GameTests with four failures: the existing book and two centrifuge checks plus `bloodBindingRootsAnAimedTargetThroughTheSharedEconomy` ("Blood Binding rejected a valid aimed target"). That aiming test and Blood Binding implementation were unchanged; it was not repaired as part of the colony change. All ecology tests passed in the combined run. Compilation, JAR assembly and packaged resource checks passed; overall gates remain red. Evidence: `build/pelagic-ecology/bone-worm-count-red.log`, `bone-worm-count-green.log`, `bone-worm-final-gates.log`, `bone-worm-native.log`, and `build/pelagic-worlds/42-bone-worm-count/pelagic-report.json`.

### Giant tube worm count follow-up, 2026-10-05

Both tube-worm checks first failed on the missing `worms` property. All 16 ecology GameTests then passed, including stacking through either half, synchronized counts and retraction, blockstate serialization, the five-worm limit, sneak bypass, all four harvest quantities from either half, and preservation of both water cells and the mineral support. The root block entity survives stacking, and adding to the upper half cannot create a third block.

The initial natural sample contained only two colonies, so the fresh-world probe now scans the complete sampled chunks and a bounded set of additional natural vent chunks. Fresh seed 42 found counts `{2: 1, 3: 1, 4: 2, 5: 2}` after two additional vent chunks, with matching upper/lower states. All 24 sampled noise-column and biome hashes match the preceding locate-fix report. Natural count selection uses separate sample bits from the existing one-in-four placement check, preserving placement sites while allowing all four sizes.

Six native-client scenes verified each count, all four sizes together and synchronized plume retraction, including views without night vision or shaders. The five-worm and group scenes were recaptured from the opposite side to expose a short tube hidden by the first camera angle. Screenshots are `ecology-24` through `ecology-29` in `build/pelagic-client/screenshots`; the gallery's `tube-worm-counts` section includes lit and dark pairs. Client logs are `build/pelagic-ecology/tube-worm-native.log` and `tube-worm-native-detail.log`.

The broad follow-up ran `runPelagicValidationServer test runGameTestServer alphaCheck build --continue --no-parallel --console=plain` with seed 42 and run name `tube-worm-expanded`. It passed 2,443 of 2,446 JVM tests and 740 of 745 loaded tests. The same three documentation/archive JVM failures and the existing book/two centrifuge failures remain. Two additional loaded failures occurred in unchanged tests and production code: `bloodBindingRootsAnAimedTargetThroughTheSharedEconomy` rejected its aimed target, and `veinwingVultureFlightControllerPursuesAssignedTarget` lost its actor to ordinary despawning after 43 ticks. These were not repaired in the colony change. All ecology and whale tests passed; compilation, JAR assembly and packaged resource checks passed. Overall release gates remain red. Logs are `build/pelagic-ecology/tube-worm-count-red.log`, `tube-worm-count-green.log` and `tube-worm-final-gates.log`; the natural report is `build/pelagic-worlds/42-tube-worm-expanded/pelagic-report.json`.

### Snail-bearing vent relocation, 2026-10-05

The existing `deep_ocean_vent` feature now targets only Hydrothermal Depths, retaining its geometry, rarity and one-time generated snail clusters. The resource regression failed before the tag change and passed afterward. A loaded test checks every registered biome's modified feature list: exactly one copy in Hydrothermal Depths and none elsewhere. Fresh seed-42 validation passed, and all 24 sampled geology/biome hashes match the preceding report; finished decoration differs in ten sampled patches. This verifies routing and terrain stability, not a separate observation of natural vent snail populations.

The combined run passed 2,443 of 2,446 JVM tests and 742 of 746 loaded tests. The same documentation/archive checks, book count and two centrifuge checks remain red; the unchanged vulture pursuit fixture again lost its actor to ordinary despawning. Compilation, JAR assembly and packaged resource checks passed. Overall release gates remain red. Evidence: `build/pelagic-ecology/vent-relocation-red.log`, `vent-relocation-gates.log` and `build/pelagic-worlds/42-vent-relocation/pelagic-report.json`. Existing generated vents and snails are unchanged; there is no retrogen.

Brined Court factions, Morphling changes, progression chains, dynamic whale corpses, pressure, temperature and global currents remain outside this pass.

## Vampire Squid verification (2026-10-06)

- 36 focused unit tests passed across Pelagic habitat/population/resources and Vampire Squid pose/resources. The cloak regression check failed for the original wide spread and passed after the arm tips were curled above and around the mantle. The editable preview is checked against the same enclosure requirement with Blockbench's reversed X-rotation convention.
- All 22 Pelagic ecology GameTests passed, including six Vampire Squid cases: native biome/placement/tag/blood registration; cloak expiry/cooldown with disabled AI; open-water navigation and survival in both depth layers; nonaggressive retreat; specimen capture/reload/Bestiary support with an immediately restored cloak pose for unticked jar render copies; and actual shared loot from player kills. Navigation uses isolated water tanks and an explicit route, rather than depending on a random wander starting during the fixture.
- `assemble` and `verifyPackagedClientResources` passed.
- Native client fixtures were inspected for swimming, the corrected arm cloak and both open/cloaked specimen-jar fits, with both lit and unlit views. Screenshots and fixture metadata are in `build/pelagic-client/screenshots/ecology-30-vampire_squid-*.png`, `ecology-31-vampire_squid_cloaked-*.png`, `ecology-32-jar_vampire_squid-*.png`, `ecology-33-jar_vampire_squid_cloaked-*.png`, and `build/pelagic-client/pelagic-ecology-visuals.json`.

The full unit-suite attempt ran 2,457 tests and hit three unrelated existing failures: two UTF-8 reads of `docs/TESTING.md` and an old Foxfire reference in an Orchestrator report. The testing guide's invalid encoding is also present in HEAD. Those files were left intact. Fresh-world natural population frequency and multiplayer acceptance were not rerun for this addition.

Repeat the focused checks with `.\gradlew.bat test --tests '*Pelagic*Test' --tests '*VampireSquid*Test'`, then `.\gradlew.bat runPelagicEcologyGameTestServer assemble verifyPackagedClientResources`. Repeat from the first squid visual scene with `.\gradlew.bat runPelagicReviewClient -PpelagicEcologyReview=true -PpelagicReviewStartScene=30 '-PpelagicReviewWorld=Pelagic Ecology Review'`.

## Swimming orientation verification (2026-10-06)

Eight focused swimming-pose and Vampire Squid unit tests passed, followed by all 23 loaded Pelagic ecology GameTests. The added loaded check exercises actual diving displacement, settling upright and neutral serialized specimen copies. Four native client fixtures were inspected for horizontal swimming and diving on both mobs, with connected appendages following the body rotation. Screenshots are `ecology-38` through `ecology-41` in `build/pelagic-client/screenshots`. Aquatic review fixtures explicitly select the Overworld, including when a preceding Nether review left the saved player elsewhere.

`assemble` and `verifyPackagedClientResources` passed. Evidence logs are `build/pelagic-swimming-green.log`, `build/pelagic-swimming-server-isolated.log`, `build/pelagic-swimming-client-final.log` and `build/pelagic-swimming-packaged.log`. These are controlled fixture checks; natural-population and multiplayer acceptance were not rerun for this pose change.
