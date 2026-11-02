# Circus Creatures, Encounters, and Rewards

## Roster boundaries

### Current implementation

**IMPLEMENTED:** The seven ordinary artificial puppet shapes are Veinwing Vulture, Marrow Juggler (the retained `marrow_spitter` identity), Gorebound Hulk, Scarlet Mummer, Sanguine Hound, Cinder Bellows, and Mnemonist Puppet. They are player-facing summons with established definitions and must not be relabeled as wild Circus animals.

**IMPLEMENTED:** Scarlet Serpents and Prism Cuttles are natural Hemomancy-reactive animals, not Circus members or agents.

- Scarlet Serpents contribute hood display, warning color, sudden attention, and Serpentine Guile.
- Prism Cuttles contribute chromatophores, camouflage, broken memory flashes, and visual misdirection.
- Their visual and behavioral language may inspire Circus practice without changing their ecology or faction ownership.

**IMPLEMENTED:** The pavilion keeps two living Prism Cuttles in Specimen Jars as a narrow Chromatist reference. Their natural aquatic ecology, ownership, and later sea-content boundary are unchanged.

## Circus fauna

### Peacock jumping spider

**IMPLEMENTED:** Peacock Jumping Spiders are small neutral animals that occur rarely along sunny forest edges. A trained specimen appears in the Ringmaster's nonlethal attention act; holding it in view completes the act and awards acclimation. It is an animal, not a puppet, boss, or damaging Scarlet Mummer substitute.

### Vampire-bat swarm

**IMPLEMENTED:** Vampire Bats are cave-roost animals that notice wounded living creatures and naturally spawn in colonies of 8-20. The Ringmaster manifests the Circus pressure as one aggregate entity surrounded by swarm particles, avoiding a second colony of independent pathfinders. The aggregate pressures only the active encounter initiator and cleans up with the attempt.

### Luminal Cicada

**SELECTED:** An ambient defensive creature that clings to exposed tree trunks, buzzes conspicuously, and flashes when a player comes within four blocks.

The first implementation is non-hostile: the flash is presentation only, has a five-second cooldown, and does not blind or damage the player. Sound supplies an ambient warning but the full-bright body and particle burst keep the reaction visible without audio. Luminal Cicadas naturally spawn only in dark forests near trees and in lush caves. They drop nothing on death and can be captured and released through Specimen Jars.

Tree-clinging, proximity response, natural spawning, and Specimen Jar behavior require live verification before this slice is complete.

### Snake

**STRONG CANDIDATE:** Snake imagery belongs in the attention/phobia vocabulary.

Use the implemented Scarlet Serpent where possible. A separate snake boss or puppet needs a role that the Scarlet Serpent and Scarlet Mummer do not already cover.

## Encounter ladder

**IMPLEMENTED:** Succession uses four controlled-real acts: the peacock-spider attention trial, a troupe-and-doll pressure act, the knife/displacement act, and whole-troupe carousel coordination. Leaving the ring or dying dissolves an attempt cleanly while retaining completed acts. Troupe-owned dolls supply the act body when the player has no equivalent summon prepared. Liberation can proceed directly after the explicit pact rejection.

## Ringmaster

**IMPLEMENTED:** The Ringmaster becomes coherent for dialogue at 500 acclimation and offers an explicit neutral exit plus warning-and-confirmation branches for Succession and Liberation. One pre-completion route repair is permitted and then removed from dialogue. A site-global saved state locks an active attempt and its eventual outcome to one initiator.

The finale shares rafter, carousel, and descent phases. The Ringmaster uses the real troupe, knife pressure, and a manifested Vampire-Bat aggregate rather than receiving Vesper's catalogue. Succession requires 1000 acclimation and all five challenge bits; its troupe is downed nonlethally and the office is captured as the low-damage Ringmaster Pattern. Liberation makes the troupe lethal and requires destruction of the carousel before the Ringmaster can be killed.

## Carousel

**IMPLEMENTED:** The Impaled Procession is the pavilion's roughly seven-block-wide central arena mechanism. Its three scarred horse puppets rotate and bob at an ambient pace, then wind up and accelerate when a nearby Circus performer enters ALERT. Their arcs only strike that troupe's current targets.

The three captives are explicitly the three failed initiates visibly bound atop those carousel horses. They are not people elsewhere in the pavilion. On Liberation, Thread Ripper mercifully kills and severs each rider, exposing the matching anchor under that horse. Breaking all three exposed anchors destroys the carousel and unlocks descent. The destroyed carousel and conservative air-only cobweb ruin persist without replacing player-placed blocks. Succession leaves every rider and horse intact.

**REJECTED/JOKE:** Enumclaw/Mr. Hands names, explicit anatomy, and a one-percent random attack chance are excluded.

## Route rewards

### Succession

**IMPLEMENTED:** Ringmaster Pattern is a Degree-4, low-damage Conductor. It occupies one ordinary body slot, grants one additional shaped-body slot while active, and relays an owner's mode and focus commands once to same-session summons within 32 blocks. It does not chain relays or bypass damage, upkeep, ownership, or tether rules.

### Liberation

**IMPLEMENTED:** Thread Ripper is a 30-tick, 24-block, 35-blood manipulation with a 12-second cooldown. Besides its captive/anchor role, it disrupts hostile tethered puppets above 30% health for 25% maximum-health damage and five seconds of command silence, and unravels them at or below 30%. Owners, allies, bosses, trials, performers, and route-critical entities are immune.

Both outcomes also grant the fire-resistant Ringmaster's Topper, a head-equippable cosmetic with no armor value.

### Shared requirements

- Explain the irreversible route point before it occurs.
- Grant exactly one intended route reward and synchronize it server-side.
- Define death, disconnect, multiplayer credit, retry, and duplicate-reward behavior.
- Keep both rewards useful outside the Circus.
- Do not hide a mandatory progression requirement behind one moral route.
