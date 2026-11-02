# Saint Reconciliation Pass — Design

Date: 2026-10-26. Source: the user's *Four Saints of the Hematic Order* lore document (uploaded 2026-10-25) and the user's rulings on 2026-10-26.

This is the **generic reconciliation pass**: data, lore, text, endings and structure. Each Saint later gets its own complete combat-and-visual overhaul. Those overhauls are recorded under "Deferred per-Saint work" below and are out of scope here.

## Rulings (user, 2026-10-26)

### Tendencies (everywhere: `EnumSaintType`, loom recipe, Canon Memory manipulation, relic recipe, residuum, docs, Liber)

| Saint | Primary | Secondary | Canon Memory |
|---|---|---|---|
| Hemorath | ANIMUS | FERRIC | Crimson Tithe |
| Seraphae | LUX | DUCTILIS | Unclosing Eye |
| Putriciel | MORTEM | FLAMMEUS | Bloom of Rot |
| Velorum | CONGEATIO | TENEBRIS | Endless Hour |

Every Canon Memory takes its Saint's pair. Bloom of Rot and Endless Hour lose their stray secondaries (Animus and Lux).

### Doctrine tags are removed
`EnumDoctrineTag`, the doctrine fields on `EnumSaintType`, the residuum doctrine tooltip and the `saint_doctrine.*` lang keys all go. Saints are not randomly generated, so the sarcophagus no longer rolls a random Saint. An unassigned sarcophagus stays inert until a relic names it.

### A Saint cannot be killed
Every fight ends with the Saint returned to its tomb, and the sarcophagus is restored in place, dormant.
- **Seraphae** is immortal by nature. She can only be contained.
- **Hemorath** overloads and is exsanguinated again. He reforms in time, because the marrow never stops producing blood.
- **Putriciel** can be purified and rests for a while. The rot always reclaims them.
- **Velorum** always reverts to his dormant state.

Rewards (Hallowed Residuum plus each Saint's extras) roll once from the entity's loot table `entities/<saint>` through one shared path, whatever the ending.

### Titles (title-card epithets follow the lore document's headings)
| Saint | Epithet | Pronouns |
|---|---|---|
| Hemorath | The First Saint (phase card keeps "The Vessel Empties") | he |
| Seraphae | The Chain Saint | she |
| Putriciel | The Befouled Saint | he in life; **they** now. They were a man, but the rot malformed them into an agender monstrosity |
| Velorum | Saint of the Endless Hour | he |

"Hollow Vessel" was Hemorath's old working title. Player text and the loot table use Hemorath now. Velorum is no longer a martyr: the "Frozen Martyr" epithet and the martyr framing are dropped.

### Lore
- The lore document's four biographies, the ascension/recognition doctrine (§7) and the continuity notes become canon in `docs/LORE_REFERENCE.md` §10, as amended by these rulings.
- Hemorath is the first Harbinger. He is **not** "one of the original great Archons": the Archon concept has moved on, and he predates the mature Order and Erythravane's Fane. Feeding made him stronger until the absorption overloaded and killed him. That is consistent with both the Liber and the fight.
- Seraphae's story includes the nobles and physicians who chained her and drained her blood. That coercion is what keeps her from being a villain.
- Putriciel was a wandering purifier. They burned out infection, Erythrocytic Mycelium included. The censers are their tools, and the offering hand is part of their kit. Each cleansing drew corruption into their own blood until the rot overran them. Eschar wounds and shelf fungus mark the change.
- Velorum is a lucid ascetic who forced stillness on his congregation. Near-time wording is fine. Literal chronomancy is not.

### Structural
`SeraphaeEntity` extends `SaintTrialEntity` like the other three. Its containment fight is preserved exactly; there are no new mechanics.

## In scope (this pass)
1. Tendency data and every consumer: enum, loom, manipulations, relic recipe, residuum tooltip, docs, Liber, tests.
2. Doctrine removal and removal of the random Saint roll.
3. The shared tomb-return ending and one shared reward roll. Loot tables for all four Saints: rename `hollow_vessel.json` to `hemorath.json`, and add `putriciel.json` and `velorum.json`.
4. `SeraphaeEntity` extends `SaintTrialEntity`.
5. Player text: title cards, Hemorath naming, Putriciel title and pronouns, Velorum epithet and defeat line, and the Vicar's `saints_lore` line (the Saints were sealed by their own people and inherited by the Order), in the Vicar's voice.
6. Lore and reference docs, wiki, and the Liber: rewrite the Saints and Hemorath pages, and add Seraphae, Putriciel and Velorum pages with discovery entries. Fix the Canon Memory route text and Crimson Tithe's enzyme-page placement.

## Deferred per-Saint work (each its own later overhaul)
- **Hemorath.** A passive blood drain on anyone near him (his signature). The absorbed-blood meter leaks over time, so the overload win means out-feeding the leak. Iron-spike patches sprout from areas of bone. The `HEMORATH_RELIQUARY` Mortem material may need retinting for Animus/Ferric.
- **Seraphae.** A full mechanics cleanup: growth and over-healing hazards, nerve convulsions, distorted regrowth, and her reach. The wings become wing-like nervous projections. White light runs in the vessels, with separate gold flashes along the nerves.
- **Putriciel.** A full fight cleanup. Fire appears only in a dedicated cleansing interval; the absolution window is the natural home for it. A palette pass removes the red.
- **Velorum.** A stillness meter that stops players resting, with openings made by halting it for a moment. A frozen congregation that the player can wake to help in the fight.
- **All Saints.** A dedicated trial chamber per Saint, drawing on 1.21.1 vanilla structures.
- **All Canon Memories.** A manipulation cleanup pass, Unclosing Eye included.
