# Hemomancy Ecological Mob Concepts

> **Status:** Concept document. None of the creatures described here are implementation commitments unless already identified as part of the current roster.

## Purpose

The useful question is not simply "what mobs sound like Hemomancy?" It is which parts of Minecraft still feel ecologically untouched by Hemomancy.

The current roster already has strong coverage in deserts, savannas, caves, damp forests, fungal biomes, and especially the Erythrocoral ocean ecosystem. Desiccants, Chthonians, Lantern Ticks, Scarlet Serpents, Venom-Rib Centipedes, reef fauna, and other existing creatures already give those places distinct threats and wildlife. New mobs should fill missing ecological roles rather than crowding those habitats with another conventional monster.

The strongest open spaces are:

- high mountains and cliffs;
- snow and frozen biomes;
- open sky and the nighttime Overworld;
- rivers and freshwater wetlands;
- the Nether;
- the End;
- the upper and open-air layers of the Fungal Dimension.

The Vampire Bat, Vulture Bee, and Mosquito Swarm establish the direction for this pass. They make Hemomancy feel like an ecology. Most are ordinary animals with blood-adjacent adaptations until bloodcraft, injury, or another environmental trigger changes their behavior.

## Design Rules

### Ecology before combat

Every creature should have something recognizable to do before it notices the player: roost, feed, migrate, parasitize, scavenge, nest, follow a host, tend another organism, or flee. Only a minority should use the standard "see player, attack player" loop.

### Hemomancy changes the encounter

Bloodshed and bloodcraft should alter otherwise natural behavior. A normal miner may pass safely beneath a bat roost while a Hemomancer using Blood Projection beneath it starts a frenzy. Mosquitoes become dangerous around exposed blood. Vulture Bees treat wounded animals differently from healthy ones.

### Natural adaptation remains distinct from infection

Overworld wildlife generally evolved alongside Hemomancy's influence. These creatures are not all fungal mutants and should not automatically count as factional blood monsters. Arthropods and other blood-adapted animals can express Hemomancy-like biology without consciously practicing blood magic.

The Fungal Dimension is different. Its native organisms should feel genuinely alien rather than like Earth animals with mushrooms attached.

### Avoid drop-table clutter

Ambient creatures do not each need a mandatory crafting drop. Prefer nonlethal harvesting, molting, hive production, specimen capture, or environmental interactions. A new material is justified when it supports a real loop, as Corpse Wax does for Vulture Bees.

### Use the right simulation scale

Small colony animals do not all need to be individual entities. Vampire Bats benefit from individual bodies because the player can disturb a physical roost. Mosquitoes are better represented by one swarm entity rendering many insects. This preserves the intended scale without filling the world with dozens of tiny pathfinding entities and hitboxes.

## Habitat Gap Map

| Environment | Current coverage | Remaining gap | Best direction |
| --- | --- | --- | --- |
| High mountains and cliffs | Very little | Severe | Large flying scavenger |
| Snow and frozen biomes | Very little ordinary wildlife | Severe | Dormant or cold-adapted blood ecology |
| Rivers and ordinary wetlands | Some swamp and shoreline overlap | Moderate | Freshwater parasites and insect swarms |
| Temperate forests | Several existing creatures | Moderate | Sparse colony insects only |
| Jungles and dark forests | Scarlet Serpents and some forest overlap | Moderate | Vulture Bee canopy colonies |
| Caves | Lantern Ticks, Chitinites, Burrowers, and Polyps | Moderate | Social flying roosts |
| Desert, badlands, and savanna | Desiccants, Scarlet Serpents, and Chthonian mounds | Strong | Do not crowd |
| Ordinary oceans | Broad Erythrocoral and aquatic roster | Strong | Avoid more unless filling a specific deep-sea role |
| Nether | Fervent Chitinites, but little broader fauna | Severe | Heat-, ash-, host-, and soul-adapted species |
| End | No active Hemomancy ecosystem | Very severe | Tenebris and void-adapted aerial life |
| Fungal Dimension | Several ground creatures and distinct biomes | Very severe above ground | Alien aerial organisms |
| Open Overworld sky | Almost nothing | Severe | Bats, carrion birds, moths, and swarms |

## Core Creature Concepts

### Vampire Bat

**Habitat:** Cave chambers with suitable ceilings, especially larger roosting spaces.

**Role:** Rare, conditional swarm predator.

Vampire Bats are a rare bat subspecies found in colonies of roughly 8–20 individuals. A resting colony hangs from the ceiling, keeps its distance from players, and scatters if approached. The bats are individually fragile and inflict very little damage.

The colony becomes dangerous when nearby activity raises a shared **Blood Frenzy**. Possible triggers include:

- a living entity taking sufficient damage;
- repeated bleeding in the roost's vicinity;
- Blood Projection or Blood Absorption;
- exposed or spilled blood sources;
- bloody items or entities;
- a player suffering Blood Drunkenness.

When frenzy begins, the entire roost converges. Each bite causes minor damage and a tiny blood drain, but ten or more bats attacking together can sustain the frenzy and overwhelm an unprepared player. They calm after losing access to blood for long enough, being driven away from the roost, or being subdued by an appropriate mnemonic effect.

The important result is asymmetry: an ordinary miner may be less likely to provoke them than a practicing Hemomancer.

Vampire Bats are ecological fauna, not automatically fungal monsters. They may be specimen-capturable, but they do not need a dedicated combat drop.

### Vulture Bee

**Habitat:** Jungle canopies and dark oak forests, with possible rare colonies in old-growth taiga or mangrove trees.

**Role:** Non-stinging colony harvester and renewable resource producer.

Vulture Bees do not search for flowers. They visit nearby animals, make a small bite, and carry the nourishment back to their hive. A fed bee's abdomen becomes visibly red, paralleling the pollen state of an ordinary bee.

Their cycle is:

> animal to blood or flesh trace to hive to Corpse Wax

Repeated visits should not normally kill healthy animals. Wounded, recently damaged, or dying creatures are much more attractive, and corpses or dropped meat may draw several workers at once.

Their hives produce **Corpse Wax** instead of honeycomb or ordinary beeswax. A hive can only be safely pacified with smoke from a **Mnemonic Candle**, which lulls the colony into a stupor. Ordinary campfire smoke does not work and may agitate them because it signals fire rather than mnemonic suppression.

Breaking or harvesting an awake hive causes the colony to mob the intruder with bites. They do not sting, but their collective harassment makes careless harvesting dangerous.

Vulture Bees should remain uncommon and canopy-bound. They should not be spread across every ordinary forest, which already has substantial Hemomancy wildlife.

### Mosquito Swarm

**Habitat:** Swamps, mangrove swamps, rivers, jungle water, and damp forests after rain, primarily around dusk and at night.

**Role:** Common aerial nuisance and environmental parasite.

The swarm is one entity rendered as a cloud of mosquitoes. It behaves more like localized weather than a conventional two-health mob.

An undisturbed swarm hangs over stagnant water, circles livestock and wildlife, and periodically settles onto one target for a tiny blood meal. A fed swarm becomes visibly redder and eventually returns toward water or vegetation to rest.

Fresh damage, exposed blood, active blood extraction, or a Blood Moon makes the swarm more focused and persistent. It can inflict small periodic blood drains, Hunger, or irritation, but it should be a nuisance rather than a lethal monster by itself.

Fire smoke, strong wind, rain, cold, and appropriate Unstained effects can disperse or suppress it. A blood-fed swarm may be worth capturing as a specimen; otherwise it needs no unique drop.

#### Distinction from the Fargone

The existing Fargone already occupies the identity of a large mosquito-like blood monster associated with Blood Moons and the Fungal Gardens. It is currently a ground-based hostile creature rather than ordinary aerial wildlife.

The separation should remain explicit:

- **Mosquito Swarm:** small, common, truly airborne, environmental, and normally nonlethal.
- **Fargone:** oversized, unnatural, directly hostile, and tied to major blood activity.

Mosquitoes should not replace, miniaturize, or dilute the Fargone.

## Overworld Expansion Concepts

### Marrowbeak

**Alternative names:** Carrion Kite, Red Lammergeier.

**Habitat:** High mountains, jagged peaks, windswept hills, and cliffs.

**Role:** Large neutral flying scavenger.

Marrowbeaks circle high above ridges and descend when an animal dies. One bird may arrive first, followed by others that circle the remains. They prefer bones and hard remains over flesh.

They collect loose bones, carry them into the air, and drop them onto exposed stone to crack them. Trace hematic iron accumulates in their feathers as they digest marrow. The player can find rare molted ferric quills around nests rather than needing to kill the bird.

They are normally neutral but defend nests and bone caches. Their circling silhouettes give mountains visible life from a great distance without adding another charging ground monster.

### Frozen-Biome Direction

The frozen roster should emphasize long dormancy and opportunistic feeding. Only one of the following concepts is necessary initially.

#### Rime Clot

**Related existing concept:** The dormant Frozen Clot identity may already cover this niche and should be evaluated before adding a new registry identity.

A congealed organism lies buried beneath snow or frozen into ice. Nearby blood, death, or heat wakes it, allowing it to move beneath powdered snow before emerging. Once fed, it slows and freezes again.

#### Rimeblood Midge

Tiny dark insects remain frozen inside snow and ice. Bloodshed or a local rise in temperature releases a short-lived black-red cloud. The cloud feeds briefly, then disappears back beneath the snow.

#### Pallid Carrion Crane

A tall white-and-red bird walks frozen rivers. It never attacks healthy creatures. Instead, it follows wounded mobs at a distance and waits for them to die. This is the most atmospheric option but overlaps the Marrowbeak's scavenger role.

### Hematic Lamprey

**Habitat:** Rivers, flooded caves, and freshwater marshes.

**Role:** Freshwater parasite.

Hematic Lampreys hide beneath gravel or mud and attach to fish or larger swimming entities. An attached lamprey swells visibly as it feeds, then releases the host when full. Players can see salmon and other animals swimming with lampreys attached.

They normally ignore players unless the player remains in the water or attempts to remove one. Several disturbed lampreys may redirect toward the player. Their value comes from the visible host-parasite interaction, not from direct combat.

### Bloodlick Oxpecker

**Habitat:** Plains, sparse forests, and livestock-rich areas.

**Role:** Beneficial ambient bird.

Bloodlick Oxpeckers perch on cattle, horses, sheep, and other large animals. They consume ticks, parasites, and dried blood around wounds. Their feeding can occasionally remove a minor harmful effect from livestock.

A player suffering Blood Loss may also attract one. The bird has no combat role. It exists to show that blood-adapted wildlife can be useful and mutually beneficial rather than universally predatory.

### Hushwing or Tympanic Mite Colony

**Habitat:** Deep Dark and ancient cities.

**Role:** Vibration-responsive ambient organism.

This creature should interact with vibration without competing with or trivializing the Warden.

One direction is a pale **Hushwing** moth that temporarily absorbs nearby vibrations, then releases the stored disturbance as one pulse when frightened or killed. The simpler direction is a colony of **Tympanic Mites** that pivots toward a vibration in perfect unison and slowly crawls toward it without necessarily attacking.

Blood Projection or similarly forceful bloodcraft sends the colony into frantic synchronized movement. The encounter is primarily eerie visual feedback, not another Deep Dark combatant.

## Nether Expansion Concepts

The Nether is one of the clearest ecological gaps. Its species should express heat, ash, souls, and dependence on large hosts rather than merely becoming red versions of Overworld animals.

### Sanguine Ash Moth

**Habitat:** Basalt deltas and soul sand valleys.

**Role:** Mid-sized flying ambient creature that becomes hazardous in groups.

The moth has charcoal-gray wings with dim red veins beneath the scales. It drifts around soul fire, Crimson Flames, Blood Projection, and active Cardinal Rites performed in the Nether.

A single moth is peaceful. Too many gathering around one blood source fill the area with dust that causes a mild bloodcraft-related impairment. The danger comes from continuing to use blood in an area that is steadily attracting more moths.

### Cinder Flea

**Habitat:** Hoglins and striders throughout hot Nether biomes.

**Role:** Small host parasite.

Cinder Fleas live on large Nether animals and use the host's heat to remain active. Their bodies are translucent black with visibly incandescent blood. Removing one causes it to cool into a brittle husk.

They are better collected from hosts than fought as free-roaming monsters.

### Marrow Hornet

**Habitat:** Rare nests on crimson stems.

**Role:** Predatory Nether colony.

Marrow Hornets repeatedly sting hoglins and other large animals, then carry small pieces of material back to their nest. Unlike Vulture Bees, they are genuinely aggressive and use stings.

The distinction must remain clear:

- **Vulture Bee:** non-stinging Overworld harvester producing Corpse Wax.
- **Marrow Hornet:** rare Nether predator that actively brings down prey.

### Glasswing Haemophage

**Habitat:** Warped forests.

**Role:** Blood-responsive ambient flyer.

Glasswings are nearly transparent insects resembling a cranefly or damselfly, with long black legs and a small luminous red abdomen. They hover in sparse groups and barely acknowledge the player.

When something nearby is wounded, every Glasswing turns toward it in unison and slowly drifts closer. Their synchronized interest should be more unsettling than an immediate attack.

## End Expansion Concepts

The End is the largest open opportunity. Its creatures should not behave like ordinary bloodsuckers transported into another dimension. Tenebris and void adaptation should make familiar biological processes strange.

### Void Skate

**Alternative name:** Suture Ray.

**Habitat:** End highlands, midlands, and gaps between outer islands.

**Role:** Large neutral aerial fauna.

Void Skates are manta-like creatures that move by slowly flexing rather than flapping. They drift between islands and feed around chorus growth. When injured, their black-red droplets orbit the wound instead of falling.

Small groups may briefly align into one larger false silhouette when threatened, resembling a creature stitched together from several bodies. They remain neutral unless attacked and provide the End with large distant movement without adding unfair knockback pressure over the void.

### Chorus Gnat

**Habitat:** Flowering chorus plants.

**Role:** Harmless ambient swarm.

Luminous gnat clouds orbit flowering chorus plants. Breaking the plant scatters the swarm, which reforms around another nearby chorus stalk. They add motion and signal healthy chorus growth without needing combat or loot.

### Umbral Proboscid

**Habitat:** Sparse outer-island encounters.

**Role:** Specialized Tenebris haemophage.

The Umbral Proboscid never pierces a target's body. It extends a thin black tether into the target's shadow and drains a small amount of blood without physical contact. Breaking the lighting relationship, entering cover, or severing line of sight interrupts the feeding.

This should be rare. Its purpose is to establish that even blood feeding obeys different rules in the End.

## Fungal Dimension Expansion Concepts

The Fungal Dimension, or Flesh Beyond, should not be populated with recognizable Earth animals wearing fungal growths. Its aerial creatures should feel like organs, cell colonies, membranes, or life cycles that the player cannot easily categorize.

### Vesicle Drifter

**Habitat:** Hyphal Spires and other tall open spaces.

**Role:** Large alien flying organism.

A transparent bladder-like body floats between spires with no obvious front or back. Dozens of corpuscle-like organisms circulate inside it. When the membrane is badly damaged, it ruptures and releases the internal swarm.

### Ciliary Veil

**Habitat:** Broad fungal valleys and floating terrain.

**Role:** Mobile environmental hazard.

A huge living membrane slides through the air along invisible currents. Thousands of cilia line its underside. It does not bite; it passes through occupied space and briefly disrupts movement or perception.

### Spore Shepherd

**Habitat:** Sporecrown Thickets and Drifting Mycelium.

**Role:** Specialized caretaker organism.

A tall terrestrial creature tends clusters of small floating spores. It is intentionally unclear whether the smaller organisms are offspring, parasites, organs, or food. Its response to the player depends on whether the player disturbs the cluster.

### Orbital Haemocyte

**Habitat:** Around larger native Fungal Dimension creatures.

**Role:** Host-linked colony organism.

Small spherical creatures form ordered rings around a host. They behave less like companions than circulating blood cells. If the host dies, the orbit collapses and the individual organisms scatter or become briefly vulnerable.

### Veinbell Drifter

**Habitat:** Fungal canopy layers where a somewhat more readable ambient creature is wanted.

**Role:** Passive vertical wildlife.

A jellyfish-mushroom organism rises and sinks through the canopy. When struck, it releases a defensive spore cloud and rapidly deflates toward the ground. This is the least alien option and works best as transitional fauna near the dimension's more recognizable regions.

## Chamber of Will Boundary

The Chamber of Will should not receive ordinary self-sustaining wildlife merely because it is another dimension. Random fauna would weaken its identity as a private, owner-shaped space.

Any creature found there should be a manifestation of the owner's discarded memory, bodily history, or current Arbor state. Such manifestations belong to Chamber progression design rather than this general ecology pass.

## Recommended Initial Roster

If this is kept to ten additions rather than becoming another enormous content front, the strongest roster is:

| Priority | Creature | Region | Role |
| --- | --- | --- | --- |
| 1 | Vampire Bat | Caves | Conditional colony predator |
| 2 | Vulture Bee | Jungle and dark forest | Colony harvester and Corpse Wax producer |
| 3 | Mosquito Swarm | Wet Overworld | Environmental parasite |
| 4 | Marrowbeak | Mountains | Large flying scavenger |
| 5 | Hematic Lamprey | Rivers | Freshwater parasite |
| 6 | Sanguine Ash Moth | Nether | Blood-attracted ambient hazard |
| 7 | Glasswing Haemophage | Warped forest | Blood-responsive ambient flyer |
| 8 | Void Skate | End | Large aerial ambient fauna |
| 9 | Chorus Gnat | End | Small ambient swarm |
| 10 | Vesicle Drifter | Fungal Dimension | Alien flying organism |

This produces a useful range of sizes:

- **Tiny or swarm-scale:** Mosquito Swarm, Chorus Gnat.
- **Small:** Vulture Bee, Vampire Bat, Glasswing Haemophage.
- **Medium:** Sanguine Ash Moth, Hematic Lamprey.
- **Large:** Marrowbeak, Void Skate, Vesicle Drifter.

It also distributes behavior:

- three parasites or blood feeders;
- two colony species;
- two scavengers or harvesters;
- several purely ambient or reactive creatures;
- only a small number of direct combat threats.

## Final Direction

Hemomancy already has creatures willing to kill the player. What the world needs more is wildlife the player can observe before becoming involved.

A bat colony hangs quietly until blood is spilled. Vulture Bees make repeated feeding trips between animals and a hive. Mosquitoes gather after rain. Marrowbeaks drop bones onto mountain stone. Lampreys ride living fish. Glasswings turn toward a wound in silence. Void Skates cross the gaps between islands. Vesicle Drifters circulate smaller lives inside themselves.

Those behaviors make Hemomancy's influence feel old and established. The creatures are not waiting to become encounters or loot containers. They are already living in the world.
