# Hemomancy Small-Feature Handoff

This handoff has four jobs: finish the NPC portraits, build out the underwater Hemomancy thread, collect the current Saint designs in one place, and give the puppeteering Harbingers a usable faction direction.

Anything marked **current canon** already exists in code, dialogue, resources, or the main lore reference. Anything marked **open direction** is there to be developed. Keep that split intact. Do not turn an idea from this document into settled canon just because it has a name.

## 1. NPC portraits

### What to make

Make one **48×48 PNG** portrait for every concrete speaking NPC listed below. Use a headshot or head-and-shoulders view by default. A full body is fine when the clothing or silhouette says more than the face, but it still has to read at 48×48.

- Draw at 48×48. Do not draw large and shrink it later.
- Use hard pixel edges, a transparent background, and no frame baked into the image.
- Keep the face, mask, or main identifying feature inside the central 40×40 area.
- Match the existing model and skin. This is portrait work, not a character redesign.
- Make the factions visually related without making everybody look like a recolor.
- Harbingers use blood-dark reds, hematic iron, worn leather, living tissue, surgical details, and practical ritual clothing.
- The Unstained use white cloth, pale silver, oxidized copper, medical/defensive equipment, and much cleaner silhouettes.
- Do not fall back on modern lab coats, generic wizard robes, or spotless fantasy heroes. These people do physical work.

The dialogue screen already looks for `_portrait.png` beside the normal entity texture, and the two assignment ledgers use the same files directly.

### Harbingers

| NPC | What to draw | Asset path |
| --- | --- | --- |
| **Harbinger Hermit** | The isolated keeper who exposes newcomers to the Hermit Heart and starts the Harbinger path. Hermits tend blood structures like organs, not machines. Make this one weathered, private, and watchful, already a little too close to the thing they serve. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_hermit/harbinger_hermit_portrait.png` |
| **Harbinger Vicar** | The outpost's lore-keeper and the player's guide through the degrees. Authority without royalty. They carry doctrine, old vows, and truths they cannot always say plainly, so the portrait should feel formal and burdened rather than grand. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_vicar/harbinger_vicar_portrait.png` |
| **Harbinger Alchemist** | Field biologist, physician, and machine instructor. They deal with centrifuges, alembics, enzymes, specimens, and actual bodily reactions. Use stained work gear and sample/vessel details. They should look like someone examining evidence, not posing with potions. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_alchemist/harbinger_alchemist_portrait.png` |
| **Harbinger Mnemonist** | The memory specialist. They teach crude memories, manipulations, the Somatic Loom, the Mnemonic Reliquary, and projected Noetic forms. Make them attentive and cerebral, but still physical. This is someone reading meaning from blood, not a floating psychic. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_mnemonist/harbinger_mnemonist_portrait.png` |
| **Hematic Artificer / Redwright** | A maker-priest of living gear. They build frames, fit armor, witness equipment lineages, and decide whether the finished work actually answers its wearer. Use armature parts, reinforcement, fitting tools, and a severe workshop posture. Do not give them the Alchemist's glassware. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_artificer/harbinger_artificer_portrait.png` |
| **Cicatrix Anchorite / Vein-Mason** | A remote ascetic at Mason's Respite who teaches scars, Effigy loadouts, and how the body survives repeated instruction. They are not an outpost official and cannot be recruited. Scar tissue, iron-brazier ash, diagrammed incisions, controlled posture. A surgical mason living alone. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite_portrait.png` |
| **Harbinger Voyager** | Captain-scholar of a Survey Cog, so part researcher, part covenant chaplain, part sailor. Voyagers chart reefs, vents, whales, and wrecks, and their rule is observation before harvest. Use salt-worn field gear, wet charts, specimen tools, and calm caution instead of decorative nautical clutter. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_voyager/harbinger_voyager_portrait.png` |
| **Harbinger Votary Wayfarer** | A junior Harbinger aboard the Voyager's ship to witness before deciding what the sea means. Curious, honest about being afraid, and still learning how to separate reverence from evidence. Less settled than the Voyager, less decorated, with practical deck gear and something that suggests field notes. | `src/main/resources/assets/hemomancy/textures/entity/harbinger_votary_wayfarer/harbinger_votary_wayfarer_portrait.png` |

### The Unstained

| NPC | What to draw | Asset path |
| --- | --- | --- |
| **Unstained Zealot** | Recruiter, first contact, and practical guide into purification. Zealots offer the cure; they do not hunt people into taking it. Make them direct and weathered by frontline ministry, with pale ritual tools and enough edge that they do not read as harmless clergy. | `src/main/resources/assets/hemomancy/textures/entity/unstained_zealot/unstained_zealot_portrait.png` |
| **Unstained Acolyte** | Keeper of still water and interpreter of Purity, Clarity, the Silver Veil, and Our Lady. More pastoral than the Zealot, but not naive. Their posture should read as patience and grief work, like they are still looking for the person underneath the infection. | `src/main/resources/assets/hemomancy/textures/entity/unstained_acolyte/unstained_acolyte_portrait.png` |
| **Unstained Guardian** | Purified or nearly purified protector of sacred places. Guardians avoid bloodshed until protection fails, then they commit. Give them a planted stance, practical pale-silver plate, verdigris/white accents, and a hammer, mace, staff, or shield. Defensive veteran, not crusader. | `src/main/resources/assets/hemomancy/textures/entity/unstained_guardian/unstained_guardian_portrait.png` |
| **Unstained Scout** | A dying field researcher found in a tooth geode, trying to preserve what she learned about Annetta before she dies. Exhausted, injured, still working. She needs to remain recognizably Unstained underneath expedition gear and damage. | `src/main/resources/assets/hemomancy/textures/entity/unstained_scout/unstained_scout_portrait.png` |

### Other speakers

Only make these if the portrait pass includes dialogue presences beyond ordinary NPC entities. They should NOT all become normal human faces.

| Speaker | What to draw | Suggested asset |
| --- | --- | --- |
| **Annetta Knowles** | A former high-ranking Unstained cleric whose suppressed infection erupts into a crystalline relapse. Keep the pale-silver remnants, verdigris-blue eyes, and the sense that her blood spear came out of her own body. She is tragic and sometimes lucid, not a generic corrupted priestess. | Replace the blank icon with a dedicated 48×48 portrait beside Annetta's entity texture. |
| **Our Lady of Still Waters** | The world's immune response given a patient face. Veiled, pale, white-haired, surrounded by still water, tears, poppies, silver, and verdigris. She should feel defensive and inhumanly calm, not angelic or conventionally maternal. A partial apparition works better than a literal goddess standing for a portrait. | `src/main/resources/assets/hemomancy/textures/gui/our_lady_portrait.png` if it is later wired into dialogue. |
| **The Fungal Whisper / Ancestral Communion** | These are the same fungal consciousness reaching the player through blood and mycelium. Do not give it one stable face. Use red hyphae, vascular tissue, layered silhouettes, and eye-like empty spaces, something that can still honestly be labeled `???`. | Replace or add a companion for `src/main/resources/assets/hemomancy/textures/gui/mystery_speaker.png`. |
| **The Crimson Lodestone** | The Lodestone is a Sanguine Monolith with a dormant fungal fragment inside it. Draw the stone face or the hollow behind it, not a person. The whole point is that an incubation vessel has started assessing whether the Harbinger is ripe. | Add a 48×48 companion portrait beside `textures/entity/model_sanguine_monolith.png`. |

The **Drudge** sits in the NPC package but does not have dialogue, so it does not need a portrait in this pass. The Saints are bosses and have their own section below.

## 2. Underwater Hemomancy and fungal life

### Current canon

The sea is not just another place the infection reached. Water carries memory, dilutes it, preserves it, mixes it together, and eventually erases the difference between one source and another. That is the actual question behind the ocean content: can blood-memory survive in something that refuses to keep it separate?

**Erythrocoral Reefs** are warm-ocean fungal coral. A reef is tissue, garden, skeleton, and archive at the same time, with red-violet living mass growing around pale calcified sections, fans, and tendrils. It is wrong-looking, but it is still a functioning habitat. Shearing it cleanly preserves a gentler memory. Tearing the reef apart mostly teaches it pain.

**Hydrothermal vent fields** are the deep-ocean side of that ecology. Basalt, blackstone, deepslate, magma, then restrained blood-organic growth around it. Chalybeate Snails grow iron-sulfide sclerites there. Voyagers call the snails neighbors instead of stock and prefer knapping a loose sclerite from a withdrawn living animal over killing it.

**Mnemonic Whales** are slow deep-reef drifters and living archives. Their bodies hold migration paths, reef pulse, vent weather, current changes, and other patterns that take years to become visible. Their resonance is not human speech, and the whale is not a book sitting there for the player to open. Watch it, chart it, keep some distance.

**Mnemonic Ambergris** is waxy memory matter shed or offered by a calm whale. It records passage in layers, basically scripture written through motion instead of ink. The boundary here needs to stay hard: bottle what the whale sheds or gives. Never hunt one for another sample. Ambergris currently connects the sea to the Mnemonist Puppet Ordeal and the Phantasmal Blood Lust lineage, so it already sits between oceanic memory, projected bodies, and prismatic display.

**Voyagers** turn Survey Cogs into moving laboratories and chapels. They compare reef tissue, vent sclerites, drowned logs, and whale records to find where the ocean accepts Hemomancy and where it pushes back. Failed expeditions remain as wrecks and Brined Votaries, people held inside the last shape of their duty, not a separate evil navy.

**Votary Wayfarers** are the junior witnesses on those ships. The Voyager has training and authority. The Wayfarer has useful uncertainty. They write down jelly drift, reef pulse, changes in the rope, fear, bad guesses, and things that are still unknown. Keep that relationship as mentor and field apprentice, not captain and disposable deckhand.

### Open direction: the Pelagic Rosary

**The Pelagic Rosary** is a working name for the possible deep-ocean cult. It is not current canon.

The Rosary tends free-rolling colonies called **memory beads**, dense balls of kelp, red algae, hyphae, whale-sloughed tissue, and mineral grit. Deep currents keep turning them across the sea floor, smoothing the outside while the center holds older and older layers. A mature bed looks like a rosary scattered across the abyss, except the beads are big enough to walk between.

These are not whale eggs, cut-out brains, or just another storage block. They are filters. A bead catches whatever the water would have pulled apart: migration resonance, a dying reef pulse, fear from a wreck, loose fungal signals, traces left by an old blood rite. None of those pieces is a complete person. The dangerous part starts when somebody decides enough pieces MUST be one voice.

The cult believes the ocean has the only honest memory because nobody can own it. Bloodlines keep memory inside bodies, books pin it to words, but the sea mixes every version together until no witness gets to claim the whole truth. Keepers roll and prune the beads, sing into them, and eventually braid some of their own memory into the colony so the next generation can hear a current they never stood in.

Visually they should look like abyssal gardeners who have been listening too long. Weighted kelp veils, cords of smaller beads, vent-iron and shell weights, ambergris lamps, round gardens tethered against the current, masks without a fixed mouth. Do not make Atlantis, pirates, or a pack of generic drowned cultists.

The cost is identity. The beads may preserve things the Order cannot measure, but repeated communion makes it harder to tell which memory belongs to the keeper and which one came from the colony. Some accept that as humility. Some use the mixed voices as authority. Leave the source unresolved: whale memory, fungal memory, mortal residue, or all of it tangled together.

Voyagers would take the Rosary seriously without accepting every impression as revelation. Wayfarers make the best bridge because they are young enough to live with the keepers, listen honestly, and still write `unknown` in the margin.

That gives the three groups different jobs:

- The Rosary preserves memory by joining it.
- Voyagers preserve memory by measuring and witnessing it.
- Wayfarers move between both and get changed by both.

Keep the first implementation small: environmental lore, one memory-bead plant/block family, a field note, and Voyager/Wayfarer inquiries. This does not need its own progression tree.

## 3. The four Harbinger Saints

### What the Saints are

The Saints are ancient Hemomancers who became too powerful or too uncontrolled for an ordinary burial. “Saint” is an Order term. It does not mean they were good.

Each one has two blood tendencies, two doctrinal aspects, a bodily relic, Hallowed Residuum, and a Canon Memory. A Harbinger whose tendencies align may get a sample peacefully. Rejection wakes the Saint. The entities and prototype mechanics exist, but the full chambers and encounter placement are still **post-release expansion content**.

### Hemorath, the First Saint

**Aspects:** Ferric + Mortem, Judgment + Silence  
**Relic:** Hemorath's Rib  
**Canon Memory:** Crimson Tithe

Hemorath is tall, lanky, dry, hollow-looking, and somehow still moving. The existing model already gives him a heavy collar, built-up shoulders, hanging front cloth, and a long segmented cloak. Keep the flesh pale and emptied, with old dark-crimson cloth around it. He should look drained, not like a bare skeleton.

He was one of the first great Archons and believed blood could be made permanent, solid, inevitable. He became so good at holding it that he could no longer release anything.

The fight is that belief collapsing on top of him. Blood spent nearby becomes debt for the player and fuel for Hemorath, but feeding him enough overloads the hollow frame until it bursts. He dies exsanguinated by the blood he refused to let go.

### Seraphae, the Chain Saint

**Aspects:** Animus + Lux, Witness + Absolution  
**Relic:** Seraphae's Bound Eye  
**Canon Memory:** Unclosing Eye

Seraphae was a healer who found she could mend people with a touch. Then her veins started glowing, plants grew around her, and the gift kept accelerating until everything she touched bloomed past the point where it could survive. Her own vessels burned through. The Order could not kill her normally, so they chained the whole event back into containment.

Use white-gold and pale cyan light, luminous veins, an eye/halo shape, wing-like glow, chains, and four external anchors. She can be beautiful, but in the same way uncontrolled growth is beautiful right before it destroys the thing growing.

She fractures during the encounter and containment begins failing. The player binds the fragments, works through the anchors, then strikes when she condenses. Winning means putting the light back in chains and returning the room to darkness. It is containment, not an execution.

### Putriciel, the Rotting Saint

**Aspects:** Mortem + Flammeus, Absolution + Witness  
**Relic:** Putriciel's Blighted Heart  
**Canon Memory:** Bloom of Rot

Putriciel is a heavy, overgrown figure whose death-work burns instead of going cold. Use charred vestments, fungal bloom, ember-lit rot, and a heart/sternum focus. Keep the color around black, dried red, and deep furnace orange, with green-brown decay underneath it. He needs to read as rot-fire, not a generic plague boss.

What is settled is the connection between decomposition and absolution. His blood forces ruin to flower, turning death into something that keeps spreading. His life before sainthood is still open. Do not fill it in through incidental flavor text and accidentally make it canon.

His rot novas spread Wither and fire through the room. Ordinary lethal damage cannot end the fight; the player has to recognize the brief absolution windows and strike then. The actual test is knowing when to grant an ending.

### Velorum, the Frozen Martyr

**Aspects:** Congeatio + Tenebris, Martyrdom + Silence  
**Relic:** Velorum's Frozen Tongue  
**Canon Memory:** Endless Hour

Velorum is tall, layered, hooded, and almost completely closed off. The existing model has segmented waist panels, heavy shoulder plates, a long cloak, and a deep hood with one visible eye area. Use black, iron-gray, and midnight blue. Frost should look like age and restraint collecting on the body, not decorative ice armor. The mouth/tongue needs to feel deliberately sealed.

Velorum treated silence and voluntary suffering as the highest devotion, then pushed both so far that the stillness became permanent. Like Putriciel, the earlier mortal history is still open.

Every successful hit gives Velorum a short martyrdom resistance window. Frost locks down movement, darkness blinds and sickens blood-active Harbingers, then the last phase starts draining blood nearby. The rhythm should feel withheld and oppressive. Attack, wait, breathe, then decide when to hit again.

## 4. The Circus

> **Expanded working handoff:** See [`docs/archive/tracks/circus/README.md`](circus/README.md) for the September 1, 2026 decision ledger and the implementation-sized Circus documents. That suite preserves implemented, settled, candidate, open, deferred, and rejected material as separate statuses.

### Current puppetry canon

Blood Drunk Puppeteers are Broken Wills being pulled by the fungal hive. They are failed people, not a separate species, and the visible strings are literal.

Harbinger puppetry copies that principle without agreeing that the hive owns the puppeteer. The Puppeteer's Spindle prepares a learned artificial body. An owner-attuned Marionette Crossbar holds the thread tension used to call, sustain, recall, and direct it. The six current artificial bodies are learned through Cardinal Ordeals where the manifested puppet itself is the test. The Crossbar survives because the rite is testing the relationship, not consuming the tool.

The six current definitions are the Veinwing Vulture, Marrow Spitter, Gorebound Hulk, Scarlet Mummer, Sanguine Hound, and Mnemonist Puppet. A faltering Broken Will can also be Commandeered, but that does not turn a failed person into one of the artificial shapes.

Scarlet Serpents and Prism Cuttles are natural Hemomancy-reactive animals. They are not Circus members or faction agents. Scarlet Serpents use hood display, warning color, sudden attention, and Serpentine Guile. Prism Cuttles use chromatophores, camouflage, broken blood-memory flashes, and visual misdirection. Prismatic armor already pulls Prism Cuttle and Blood Drunk Puppeteer imagery into the same family, alongside the Lantern Tick.

### Open direction: “The Circus”

**The Circus** is the current working name for a mobile Harbinger subculture built around puppeteering. Keep it inside the Order. It is a practice and a social identity, not a new class, another degree ladder, or a faction that replaces normal Harbinger progression.

People call it a circus because these Harbingers travel with folded frames, hanging crossbars, masks, warning-colored cloth, and bodies that unfold into the fight like performers entering a ring. They may use the name themselves, but the actual work is serious. They study the instant where attention, posture, color, and imposed movement make somebody surrender control of the next decision.

The **serpent side** is about directing attention. A cobra hood builds a stage around the head, the warning bands tell the eye where to look, then the rest of the body attacks from somewhere else. Circus collars, masks, and puppet silhouettes should borrow that shape. Serpentine Guile is not magic hypnosis. It is threat posture, rhythm, and control of what the target expects to happen.

The **cuttle side** is about making the whole body lie. Chromatophores can hide an outline, flash a false intention, or make several moving parts read as one organism. Circus cloth and puppet skins copy those pulses. A cuttle-trained puppeteer makes the thread hard to find because every visible surface seems to be the thing issuing the command.

The **Blood Drunk Puppeteer** is the nightmare at the center of the practice: a puppeteer who is also the puppet. Some Circus members destroy them as mercy. Some Commandeer a faltering one and call the service a rescue. Others wear part of the mask or glow pattern to show that they know what waits at the far end of the string. That disagreement should be the fracture running through the group.

If the group needs internal roles, use these as working terms:

- **Ringmaster:** controls the field's attention and coordinates several tethers. It is a job, not authority over every Harbinger nearby.
- **Chromatist:** works on cuttle-derived cloth, shifting puppet skins, masks, and false tells.
- **Charmer:** studies serpent display, target fixation, and threat posture.
- **String-Surgeon:** maintains Crossbars, Spindles, artificial joints, and the puppeteer's body under long-term strain.
- **Blood Drunk:** a slur or diagnosis for somebody whose tether has started pulling backward. Never use it as a glamorous promotion.

Use layered reality. The physical site remains a rough occult pavilion of dark red cloth, blackened hematic frames, bone-white face plates, serpent-warning yellow, cuttle iridescence, long jointed silhouettes, scraps, bones, pelts, and visible thread. An acclimated viewer can perceive the fuller impossible circus: coherent tents, richer color, duplicate performers, inverted movement, and recognizable carnival forms made frightening by collective manifestation. Cheap comedy, greasepaint caricature, and random carnival clutter are not the physical baseline. The discomfort should come from disciplined performance and the question of who is actually moving whom.

Small pieces that fit the existing mod:

- A ruined traveling pavilion or roadside ring built around a Puppeteer's Spindle.
- Field notes comparing serpent hood displays to cuttle chromatophore pulses.
- One Circus mask or cosmetic fitting, without attaching another progression system to it.
- A named Blood Drunk Puppeteer whose notes show the tether reversing over time.
- Mnemonist and Artificer dialogue arguing over whether the Circus preserves will or only makes domination look deliberate.

## Source anchors

Use `docs/LORE_REFERENCE.md` as the main canon source, especially sections 6.2a, 6.3, 6.7, 7.6, 8, 10, 11, 15, and 16. `docs/HEMOMANCY_REFERENCE.md` has the implemented mechanics and exact Saint tendency pairs. NPC voice and the ocean field ethic live in `src/main/resources/assets/hemomancy/lang/en_us.json`. Concrete NPCs are under `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/`, with their dialogue factories in the `dialogue/` package.
