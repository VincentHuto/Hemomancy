# World Content

**Dynasty Castles** are modular surface structures in the configured fungal biome group. Their courtyards, keeps, gates, walls and towers vary between intact and ruined layouts, with authored Fargone and Thirster populations. Fargones have Scout, Attendant, Guard, Elder and Reclaimed variants; these share their base combat attributes. The [developer reference](../docs/HEMOMANCY_REFERENCE.md#dynasty-castle) records placement settings. Registration and templates are implemented; natural encounter frequency remains a separate acceptance check.

The Nether also contains the **[Phlegethontic Basin](Phlegethontic-Nether.md)**: dark ichor reservoirs joined by curved, scab-lined courses, with exposed venous-stone bands and basalt/blackstone arches. **Excoriated** guard the shores with arterial bows and Recall Barbs. Sealed ichor veins run independently through natural Nether stone close to the dimension floor. These features appear in newly generated chunks.

Progression is tied to exploration, not a straight tech tree. Custom biomes include the fungal-dimension regions **Fungal Gardens** and **Fungal Isles**, overworld infection zones such as **Sporecrown Thicket**, **Hyphal Spires**, and **Drifting Mycelium**, and the ocean biome **Erythrocoral Reef**, with its own deep-water palette, fauna, and structure hooks.

New Overworld oceans descend from **Rockpool Shore** benches and enclosed tide pools through **Erythrocoral Reef** shelves into open **Pelagic Ocean**. Below Y33, **Twilight Ocean** has scalloped pale ledges, **Midnight Ocean** cuts through dark branching canyons, **Carrion Depths** has sediment fans and scattered fossil ribs, and **Hydrothermal Depths** opens onto gently rolling low plains with raised mineral mounds beneath localized fields of 1–5 irregularly spaced chimneys. Sea level remains Y63. These are real water-column biomes with distinct animal communities; Brined Court faction and Morphling mechanics remain separate. Previously explored chunks keep their terrain.

With Pelagic worldgen enabled, `/locate biome hemomancy:hydrothermal_depths` uses closer height checks to find the thin depth layers. The same correction applies to Twilight, Midnight and Carrion searches, which vanilla's widely spaced height checks could miss.

| Community | What to find |
|---|---|
| Exposed Rockpool Shore | Chitons grazing Hematic Algal Crust and clamping to wet rock |
| Tide pools | Hemolymphopoda and retracting, mildly stinging Tidepool Anemones |
| Reef shelves | Bottom-associated Barbed Urchins, coral and ordinary reef fish |
| Open Pelagic water | Mnemonic Whales, hollow Pyrosomes and fast Herring schools |
| Twilight gullies | Camouflaging Prism Cuttles and Siphonophores with hanging feeding filaments |
| Midnight canyons | Blood Lantern Jellies and translucent Bloody-Belly Comb Jellies |
| Carrion fossils | Swimming Vampire Squids, Bone Worm colonies with 2–5 visible worms and bottom-foraging Hagfish, which accept rotten flesh |
| Heated mineral aprons | Swimming Vampire Squids, Chalybeate Snails and tall Giant Tube Worm colonies with 2–5 visible worms |

Pyrosomes spawn at Y28–59 in Pelagic Ocean and its upper Twilight overlap. Their independent native cap of eight prevents whales, squid, cuttles and siphonophores from blocking their population; ordinary water clearance, spawn distance and despawning still apply.

Reef shelves and Rockpool Shore gradually blend into neighboring ocean and coastal terrain, while reef interiors retain flat pockets for coral growth and shore interiors retain tide pools. Hematic Algal Crust uses four position-selected rotations to break up repeated patch patterns, including existing placed crust. These smoother edges appear in new chunks; already generated terrain is not rebuilt.

The existing geothermal vent fields that come with Chalybeate Snails (the source of Chalybeate Sclerites) now generate in **Hydrothermal Depths**, rather than reefs or the former vanilla deep-ocean locations. Their mineral aprons, chimneys and snail clusters are preserved. The snails are immune to magma-block damage and can crawl across the heated aprons without taking damage or retracting from that contact. This affects newly generated chunks; existing vents remain where they are.

All seven new animals fit Specimen Jars and the Living Bestiary. Vampire Squids fold their webbed arms into a defensive cloak when hurt and drop **Barbed Splinters**, the shared material also dropped by Barbed Urchins. They swim only in the two deepest layers. Vampire Squids and Comb Jellies smoothly angle their bodies toward actual swimming movement, including vertical travel, then ease upright when still. Captured mobs remain persistent after release, including specimens stored in older jars. Shears collect the four growths for placement; Bone Worms need bone, and Tube Worms need two submerged cells over mineral support near magma. Wildlife can populate suitable existing chunks. Generated growths appear in new chunks, with player placement available elsewhere and no retrogen. See [Pelagic ecology](../docs/PELAGIC_ECOLOGY.md) for habitat, collection and authoring details.

A placed Bone Worm or Giant Tube Worm colony starts with two worms. Add colony items to increase it one worm at a time, up to five; sneak to place separately. Either half of a Giant Tube Worm colony accepts additions and shares the count. Shears return the one to four colony items represented by that size once for the whole colony, preserving the water and support.

**Osteophage Vultures** roost on exposed bone blocks in **Soul Sand Valleys**. These skeletal bearded vultures climb above skeletons before swooping down to strike. They prefer loose bones, including player offerings, eat one bone at a time to regain health, then return to their fossil perch. They leave players alone. Player kills drop 1–2 **Venous Pinions**, plus Looting, sharing the material used for Venous Strider Sabatons. Their Mortem blood can be sampled normally, and Specimen Jars support capture and Living Bestiary research.

Structures drive exploration too. Early progression starts at landmarks like the **Blood Temple** and **Unstained Church**. Later travel can uncover **Harbinger Outposts**, **Voyager Wrecks**, **Active Voyager Vessels**, **Crimson Lodge Annexes**, **Bog-Body Ossuary Niches**, and the **Broken Church** encounter space. Endgame content reaches places like the **Qliphoth Fane** and saint-linked trial chambers.

These places contain roaming mobs, aquatic threats, bosses, progression NPCs, reagents, and relics. Discoveries include **Erythrocoral Fragments**, **Chalybeate Sclerites**, **Mnemonic Ambergris**, **Enthralling Filament**, **Sanguine Quintessence**, and route rewards such as **Annetta's Sanguis Lancea**. **Hematic Burrowers** hide near forest caves, color-shifting **Prism Cuttles** favor Twilight gullies while retaining their warm-water habitats, and rare **Venom-Rib Centipedes** shelter in damp temperate biomes.

**Mnemonic Ambergris farming:** Mnemonic Whales pursue nearby squid, glow squid, and Prism Cuttles in water, including outside the Erythrocoral Reef. Each prey killed by a whale drops exactly one ambergris immediately above the whale, with no feeding cooldown. Bring squid on leads, or place and break filled specimen jars to release Prism Cuttles near the whale. Supply more live prey for repeated production; player kills and dead specimens do not count. Prism Cuttles also spawn naturally in the reef. Bottle sampling and random shedding remain available on their existing shared cooldown. Keep the whale alive.

**Ice Fish** form passive schools in Cold Ocean, Deep Cold Ocean, Frozen Ocean, Deep Frozen Ocean, Frozen River and water-filled Ice Spikes pools. They can spawn under ice and above sea level where two clear water cells are available. Spawn packs contain 3–5 fish, with schools of up to six. Each fish drops one **Cleansing Hemolymph**, sharing Hemolymphopoda's existing material. Their pale, broad-headed form and haemoglobin-free blood reflect a natural affinity with Unstained purity. Sampling is ordinary and tendency-free with the aquatic property; Specimen Jars and the Living Bestiary accept them.

## Shared mob materials and foods

**Choir Keepers** now combine an owl's broad feathered face, forward-facing eyes, short beak and ear tufts with a peacock's fifteen-feather Eye-of-Ender fan. Their compact feathered neck replaces the former vulture silhouette.

Chitons and Phlegethontic Bombardiers provide Chitinous Husks; Peacock Spiders rarely provide a husk alongside Enthralling Filament. Surface Hemojellies also supply filament, which retains its Loom/Spindle uses. Chromatophores come from Prism Cuttles, Bloody-Belly Comb Jellies and Luminal Cicadas. Excoriated, Venous Striders and Vampire Bats provide Barbed Splinters alongside Urchins and Vampire Squids. Choir Keepers supply Venous Pinions, while deeper Ductilis Siphonophores supply ordinary Ganglion Clusters. New ordinary material pools require player kills; special organs retain their existing rules.

Pelagic Herring and Crimson Does supply cookable fish and venison. Hemojellies float at temperate/warm marine surfaces, gathering small fish with submerged tendrils; Siphonophores zap prey deeper below. Neither treats players as prey.

The **[Crimson Troupe](Crimson-Troupe.md)** teaches Puppeteering from five travelling camp variants and an expanded pavilion. Abandoned stages and the pavilion's records support its Side investigations. Existing pavilions keep their saved architecture.
