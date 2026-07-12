# Hemomancy Lore Consistency Review

> **Status:** Author decisions settled, 2026-07-11
>
> **Purpose:** Combine the repository-wide lore audit into one decision document before any broad canon-sync pass.
>
> **Important:** R-01 through R-03 and S-01 through S-16 are settled author rulings. Their decision paragraphs are canonical patch instructions. Remaining `P-*` entries are either explicitly adopted, intentionally provisional, or deferred as stated.

## 1. Canon authority contract

The following hierarchy governs the cleanup:

1. [`LORE_REFERENCE.md`](LORE_REFERENCE.md) is the authority for world history, factions, metaphysics, character identity, and narrative sequence.
2. [`HEMOMANCY_REFERENCE.md`](HEMOMANCY_REFERENCE.md) is the authority for current mechanics, implemented progression, item behavior, and system status, provided that it does not contradict settled lore in `LORE_REFERENCE.md` or a direct author ruling.
3. Focused reference documents are subordinate sources only where their material has been explicitly adopted into either primary reference.
4. NPC dialogue, inquiry responses, in-game books, item descriptions, tooltips, advancements, inscriptions, and the wiki are downstream presentations. They must be rewritten to match the reference docs, never used to rewrite the reference docs merely because they are currently implemented.
5. Plans, specifications, brainstorms, audits, briefs, and roadmaps are design provenance. They are not canon unless the primary references adopt their content.
6. `Notes.txt`, the packaged legacy `Hemomancy Lore.txt`, and the untouched NeoForge root `README.md` are noncanonical.

When the two primary references make incompatible narrative or metaphysical claims and neither one explicitly supersedes the other, the issue is an **actual schism** and requires an authorial decision. A stale status, count, gate, or geometry description can remain **canonical drift** when a later adopted reference passage or explicit implementation note clearly replaces it. When a downstream surface disagrees with settled canon, it is also canonical drift and can be corrected without a new lore decision.

## 2. Classification used in this review

| Classification | Meaning | Patch rule |
|---|---|---|
| **Resolved ruling** | The author has already selected the canonical interpretation. | Patch every conflicting source in the direction stated here. |
| **Canonical drift** | Canon is sufficiently clear, but one or more lower-authority surfaces are stale, misleading, mechanically obsolete, or incorrectly phrased. | Correct the downstream surface; do not reopen canon. |
| **Actual schism** | Primary references conflict internally, two adopted systems encode mutually incompatible truths, or narrator reliability would materially change the story. | Obtain a decision, update the primary references first, then synchronize all downstream surfaces. |
| **Provisional lore** | A downstream source adds lore that is neither supported nor directly contradicted by the primary references. | Do not silently promote it. Mark it as speaker belief, remove it, or adopt it into the primary references deliberately. |
| **Archive/tooling drift** | The content is noncanonical or unused but can still mislead maintainers. | Label, relocate, regenerate, or remove only after its archival purpose is decided. |

## 3. Audit coverage

The documentation pass covered 109 targets in total, in addition to the runtime/data surfaces listed below.

| Surface | Coverage |
|---|---:|
| Primary/general documentation | 18 targets |
| Historical implementation plans | 39 targets |
| Historical design specifications | 34 targets |
| Wiki | 15 pages |
| Root and packaged legacy prose | 3 targets |
| Runtime NPC dialogue | 17 dialogue factories; 831 referenced dialogue keys |
| Runtime localization | 3,384 language keys |
| Item inquiry data | 317 JSON entries across 9 NPC roles |
| HutosLib books | 70 JSON files; 57 prose pages |
| Lore/progression advancements | 106 non-recipe JSON entries (139 total including 33 recipe-unlock advancements) |
| Cardinal rites | 48 descriptions |
| Discovery inscriptions | 30 entries |
| Materials reference data | 343 active `MaterialEntry` constructions (353 textual constructions including 10 commented entries; 336 unique active IDs because seven living-weapon IDs are repeated) |
| Hardcoded item/block tooltips | 114 implementations |
| Legacy dialogue-editor snapshots | 10 snapshots |

Inquiry coverage by speaker: Alchemist 48, Artificer 21, Guardian 32, Mnemonist 83, Monolith 33, Vicar 28, Votary/Wayfarer 13, Voyager 13, and Zealot 46.

## 4. Resolved author rulings

These are no longer open questions.

### R-01 - The wiki is downstream of the docs

**Canonical ruling:** The wiki must be based on the docs files. Wiki text cannot overrule or retroactively redefine the reference documents.

**Required patch direction:** Resolve canon in the reference documents first, synchronize game-facing prose second, and rebuild the wiki from those settled references last.

### R-02 - Annetta's canonical creature is the Tooth Peck

**Canonical ruling:** A **Tooth Peck** seeks Annetta because Tooth Pecks seek out infection. Its interest in her causes Annetta's horrifying realization that she herself is infected.

**Consequences:**

- Replace the Chthonian in [`LORE_REFERENCE.md`](LORE_REFERENCE.md) section 11 and any matching downstream retellings with a Tooth Peck.
- State clearly that the creature's infection-seeking behavior, not merely the bite itself, exposes the truth to Annetta.
- Keep Chthonians as a separate species: iron-mandible termite creatures associated with wood-chewing, termite mounds, biological hematic iron, and their Queen.
- Do not transfer Tooth Peck infection-detection behavior to Chthonians.
- Correct the implementation-status sentence that currently calls the Tooth Peck in the jar a "termite."
- Preserve Annetta's established routes: mutation into the Stained Priestess on the Harbinger route, or cure/ally transition with the expelled Latent Infection on the Unstained route.

### R-03 - Verdigris Aura precedes Silver Ward

**Canonical ruling:** **Silver Ward is the advanced form of Verdigris Aura.**

The intended progression is:

1. Early Purity: **Verdigris Aura**.
2. Advanced Clarity: **Silver Ward**.

**Consequences:**

- Correct the reversed mapping in [`HEMOMANCY_REFERENCE.md`](HEMOMANCY_REFERENCE.md), especially the Purity and Clarity progression descriptions near its current lines 834, 876, and 933.
- Audit rites, status toggles, tooltips, books, NPC explanations, advancements, and inquiry answers for the same ordering.
- Where both effects are granted by a late rite, explain Silver Ward as the advanced state layered on or developed from Verdigris Aura, rather than two unrelated blessings.

## 5. Canonical drift - settled corrections

The entries in this section do not need a new lore choice. They need careful synchronization after the resolved rulings and actual schisms are incorporated into the primary references.

### 5.1 Primary reference maintenance

| ID | Drift | Settled correction | Main affected sources |
|---|---|---|---|
| D-001 | Votary prose says there are seven blood tendencies, while the adopted system contains eight. | Use all eight: Animus/Vivacious, Flammeus/Fervent, Ductilis/Neurotic, Lux/Incandescent, Mortem/Ruinous, Congeatio/Frigid, Ferric, and Tenebris/Umbral. Remove the obsolete generic `Fungal` tendency from the normal eightfold list. | `LORE_REFERENCE.md` section 6.4a; `HEMOMANCY_REFERENCE.md` Vicar table near line 484; dialogue and books. |
| D-002 | Rank prose places Humilis and Mediocritas at stale degrees. | Follow the adopted gates in `ManipulationRankGates`: Humilis D0, Mediocritas D1, Summa D3, Magister D5, Perfectus D6. | Both primary references; Vicar/Alchemist teaching text; wiki. |
| D-003 | The Fane is still described as a fixed 5x5 chunk square. | Use the later implemented Flexible Founding Fane / Soft Envelope model and describe its heart, stakes, ownership, boundary preview, and cleanup consistently. The mechanics reference explicitly supersedes the earlier fixed footprint; this is geometry/status drift rather than a new lore choice. | `LORE_REFERENCE.md` sections 6.4a and 6.5 versus `HEMOMANCY_REFERENCE.md` section 5.7. |
| D-005 | Field Notes are described once as dormant or future content. | Use their implemented stack-local memo notebook and faction-ink behavior documented in `HEMOMANCY_REFERENCE.md` section 19. | Early status summary, books, NPC hints, wiki. |
| D-006 | Manual Blood Moon triggering remains marked as future in lore prose. | Mark the implemented trigger and synchronization behavior accurately, and explain it through S-09's settled fungal-surge/Unstained-containment model. | `LORE_REFERENCE.md` Blood Moon section; mechanics status tables. |
| D-007 | Manipulation totals and audit totals disagree with the current registry. | Generate or recount the active/retired totals from the registry and use one number everywhere. The current reference claims 66 while other audit prose reports 60/62. | `HEMOMANCY_REFERENCE.md`, `BLOOD_MANIPULATION_EXAMINATION.md`, wiki Developer Reference. |
| D-008 | Old Fungal Implantation Pylon / Scar Binder / Fungal Gardens scar acquisition remains in prose. | Use the adopted Mycelial Crucible cultivation path and distinguish it from unrelated Fungal Dimension identifiers. | `fungalscar.md`, older reference passages, dialogue, inquiries, wiki. |
| D-009 | Morphling prose calls the progression "five-stage" while listing `Unfed`, `Fledgling`, `Developing`, `Mature`, `Apex`, and `Primal`; primary references also retain the superseded twelve-animal roster. | Call these **six code-facing states**, with **five earned maturity levels after the Unfed baseline**. Update all current-facing prose to the implemented roster of eight original fungal strains: Deadman's Purse, Gravecap, Witch's Ear, Foxfire, Bootlace, Irontooth, Emberfang, and Winter Shroud. Historical plans may retain the old twelve-animal roster as provenance. | `MORPHLING_REFERENCE.md`, `HEMOMANCY_REFERENCE.md` section 16, books, inquiries, wiki, and current MaterialsData. |
| D-010 | The reference documents contain mojibake and malformed punctuation. | Normalize encoding without changing semantics. The mechanical reference contains roughly 1,193 suspicious encoded sequences. | Primarily `HEMOMANCY_REFERENCE.md`; generated/legacy prose secondarily. |
| D-011 | The Tooth Peck/Chthonian and Verdigris/Silver ordering are reversed in central prose. | Apply R-02 and R-03 directly. | Both primary references and all dependent surfaces. |
| D-012 | Canon gives fully Purified Unstained several major biological traits, but mechanics/status documentation does not clearly mark most of them implemented or planned. | Track the coma/bloodlessness transition, halted normal aging, altered hunger, illness sense, healing touch, and plant-growth response individually as implemented, partial, or planned. Runtime should ultimately follow the settled lore unless that lore is deliberately narrowed. | `LORE_REFERENCE.md` section 7.5; Unstained mechanics/status sections in `HEMOMANCY_REFERENCE.md`; runtime effects and progression. |

### 5.2 NPC dialogue drift

The runtime dialogue sources are under `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/`, with spoken strings in `src/main/resources/assets/hemomancy/lang/en_us.json`.

| ID | Drift | Settled correction |
|---|---|---|
| D-101 | Hermit dialogue describes the Mortal Display as an ancient, preserved shared vessel or relic of old Archons. | Each Hermit builds their own temple, removes their own heart to create the Display, and dies immediately. Later Hermit dialogue belongs to other Hermits who cannot initiate the same player again. |
| D-102 | The Vicar gives the wrong Degree 1 title. | Use **Neophyte of the Crimson Veil**. |
| D-103 | The Vicar reveals beyond-seven information at D5, then behaves as if the secret remains unknown at D7. | Reserve the explicit "always eight" / eighth-silence disclosure for the Archon threshold, consistent with the degree arc. |
| D-104 | Vicar, Hermit, and Alchemist dialogue teaches seven tendencies or uses the obsolete tendency set. | Use the canonical eightfold list in D-001. |
| D-105 | Scars are described as ordinary worn items or inventory equipment. | Describe scars as burned into memory and managed through the Mason's Effigy / Anastomotic Brazier loadout ritual. |
| D-106 | Monolith D6 dialogue still directs the player to an organ-plus-engram Somatic Loom recipe. | Use the current Loom pipeline: blank Hematic Memory, the proper catalyst/residuum, stored enzymes, projected blood, and the orb-weaving interaction. |
| D-107 | Fungal whispers sometimes turn the local world into a literal thin crust over the Entity and imply that Hermit temples grew organically over millennia. | Preserve the Entity's canonical vastness: it is distributed across dimensions/universes and extends in directions humans cannot perceive. Remove only the unsupported claim that the ordinary world is literally its crust, and keep the blood temples as Hermit-built rather than ancient fungal growths. |
| D-108 | Alchemist dialogue introduces the Ghastly Alembic at D1. | D1 introduces the Vial Centrifuge; D2 introduces the Ghastly Alembic and broader tendency work. |
| D-109 | Alchemist and Mnemonist lines retain obsolete Loom and manipulation-loadout workflows. | Use the Somatic Loom for deliberate memory weaving and the Dendritic Distributor for Degree 5 Synaptic Loadouts. |
| D-110 | Morphlings are called obedient constructs. | Present them as cultivated fungal symbionts/parasites. Naturally occurring wild polyps exist, but that does not make every finished cultivated Morphling ordinary wild fauna. |
| D-111 | Our Lady is said to have been buried for millennia. | Reconcile dialogue with the roughly 800-year history currently established in the primary lore. |
| D-112 | Annetta mutation dialogue says Annetta dies. | On the Harbinger route she becomes the Stained Priestess; on the Unstained route she can be cured while the Latent Infection becomes the boss. |
| D-113 | Late Our Lady dialogue omits the autoimmune danger established for advanced purification. | Add the warning at the appropriate late threshold without spoiling it during early recruitment. |
| D-114 | Zealot dialogue gives a stale Hemolytic Solution recipe. | Match the implemented Lethean Dew + Ghost Pipe recipe. |
| D-115 | Annetta's infection detector is sometimes identified as a Chthonian. | Apply R-02: it is a Tooth Peck attracted to infection. |
| D-116 | Purity and Clarity dialogue reverse the two defensive effects. | Apply R-03: Verdigris Aura first, Silver Ward as its advanced form. |

### 5.3 Item inquiry and localization drift

Inquiry data lives under `src/main/resources/data/hemomancy/dialogue_inquiry/`.

| ID | Drift | Settled correction |
|---|---|---|
| D-201 | Six paired inquiry responses reference twelve missing localization keys. | Add both `.line1` and `.line2` keys for `alchemist.minecraft_reagent`, `guardian.minecraft_arms`, `vicar.minecraft_relic`, `votary_wayfarer.minecraft_fieldkit`, `voyager.minecraft_salvage`, and `zealot.minecraft_ritegoods`, or remove the orphaned inquiry references. |
| D-202 | The hidden `first_bloodcraft_reward_claimed` advancement lacks its two localization keys. | Add a deliberately hidden but valid title and description. |
| D-203 | Alchemist inquiries retain an obsolete enzyme pipeline, vial/flask loop, Hematic Iron smelting, recycled enzyme process, old Ductilis definition, worn scars, and obsolete Loom behavior. | Rewrite from the current D1/D2 taxonomy, eightfold tendency, scar-loadout, and Somatic Loom systems. |
| D-204 | Guardian and Zealot inquiry coating lore conflicts with the implemented White/Pale Humor coating path. | Follow S-05/S-16: allowed Unstained blades require infection-safe design, and the universal Absolution Dagger carries a specialized hemolytic coating that destroys blood/infection in its wound. Mechanics and inquiry text must be synchronized to that doctrine rather than treating every coating as interchangeable. |
| D-205 | Guardian inquiry claims ownership of the Sporitic Thurible and says the Guardian does not walk purification. | Keep the thurible on the Harbinger/fungal support side and describe the Guardian consistently with the Unstained path. |
| D-206 | Pallid Infusion is said to initiate Clarity. | Describe its actual curative effect: clearing Blood Loss and providing regeneration. Clarity is readied at the Podium and activated by the Rite of Clarity Ascension under S-07. |
| D-207 | An inquiry says purification begins at the Altar of Cleansing. | The Altar has a later blessing/purification role. Podium treatment first suppresses infection; the Rite of Lethean Baptism begins the Unstained path and Purity progression under S-14. |
| D-208 | Pale Humor is described as purified blood. | It is purified lymph/White Humor, not blood made holy. |
| D-209 | Tears of Silthmere are described as naturally shed or found through grief. | Match the current crafted/distilled acquisition and P-10's settled meaning of Silthmere as a title of Our Lady, not a mortal source. |
| D-210 | Vicar inquiries retain Mortal Display organ echoes, universal Qliphoth use, `seed` where the object is a pome, memory integration into a bloodline, worn scar storage, and a blood key instead of Blood Projection. | Rewrite each to the adopted systems and terminology. |
| D-211 | Hallowed Residuum is said to exist only when a Saint is overcome. | Include peaceful aligned extraction as well as hostile awakening/fight routes. |
| D-212 | Inquiry text reverses Verdigris Aura and Silver Ward. | Apply R-03. |

### 5.4 In-game book drift

Book data is under `src/main/resources/data/hemomancy/books/fanesanguinium/` and `src/main/resources/data/hemomancy/books/liberimmaculatus/`.

| ID | Drift | Settled correction |
|---|---|---|
| D-301 | The Fane introduction reveals the fungal truth during initiation. | Preserve the revelation curve; early initiates should understand blood, covenant, and doctrine, not the Order's reproductive purpose. |
| D-302 | A historical record lets the Crimson Lodge speak as though it knows the complete reproductive/fungal truth. | Preserve the Lodge's canonical **limited** knowledge: it documented the link between hemomancy and the mycelial network and concealed that link so others would arrive at it independently. It does not yet possess the full sporulation/reproductive revelation reserved for later thresholds. |
| D-303 | Hemolytic recipe pages and descriptions use stale ingredients or tone. | Synchronize to the settled implemented recipe and the Unstained medical/ritual voice. |
| D-304 | Lethean gifts say dew is simply gathered at dawn and Bells grow from seeds. | Match implemented acquisition and clearly distinguish poetic symbolism from literal instructions. |
| D-305 | A page says Our Lady does not intervene. | Reconcile with her established immune-response activity and direct whisper/apparition role. |
| D-306 | Saint memories are described as directly imprinted on contact. | Use the Somatic Loom's deliberate weaving process. |
| D-307 | The manipulation chapter uses obsolete three-rank language and retired or misstated abilities. | Regenerate its catalog from current rank gates and active manipulation definitions. |
| D-308 | The tendency source omits adopted tendencies. | Use the eightfold list and explain common/formal names consistently. |
| D-309 | Virid Salis is called Degree 5 Perfected. | Follow the adopted degree and manipulation-rank vocabulary; `PERFECTUS` is a D6 manipulation gate, not the D5 title. |
| D-310 | The Fane's `annetta_knowles` biography still names a Chthonian, while both `annetta_geode_memo` pages correctly use a Tooth Peck. | Apply R-02 to the biography and preserve the geode memos' infection-detection setup. |
| D-311 | Purity and Clarity pages reverse the defensive progression. | Apply R-03. |

The book claim that infection or memory is universal must now follow S-02: widespread mundane residue, rare blood-power activation, and still rarer capacity to survive ascension.

### 5.5 Materials, tooltip, advancement, rite, and inscription drift

| ID | Drift | Settled correction |
|---|---|---|
| D-401 | Materials data repeats the old Mortal Display organ-echo provenance. | Match the Hermit's self-extracted heart, Hermit-built temple origin, and immediate death under S-06. |
| D-402 | Hematic Iron Scrap is said to be smelted by an obsolete route. | Match the current recipe/data path once recipe generation is verified. |
| D-403 | Fungal Spine is described as a creature part and Curved Horn as an ordinary animal part. | Describe their actual narrative and creature provenance. |
| D-404 | Flask and jug capacities are listed as 250/1,000 where implemented capacities are 2,500/5,000. | Use the current units consistently in tooltips, inquiries, wiki, and reference tables. |
| D-405 | Early item text reveals the Entity and fungal secret before the player could know it. | Use material observation, Order doctrine, or an explicitly unreliable voice until the relevant revelation threshold. |
| D-406 | Hematic fixtures are described as literal fungal growths. | Treat them as crafted Order architecture unless an individual block is explicitly organic. |
| D-407 | Pale Humor is credited with restoring Purity. | Describe its implemented medical/status effect and reserve path progression for the actual Purity systems. |
| D-408 | Saint sarcophagi are casually labeled Unstained structures. | Describe them as Saint trial/containment structures whose relationship to either faction must follow the primary lore. |
| D-409 | Several materials entries use generic "ancient relic" language in place of the item's actual role. The Sanguine Monolith is also said to have merely survived an unspecified collapse. | Replace with specific, source-backed descriptions. The Crimson Lodge built the Monolith as an incubation vessel around a dormant fragment of mycelial consciousness extracted from a deep Erythromycelium vein; its later reflection/will symbolism does not replace that origin. See `LORE_REFERENCE.md` section 6.5a. |
| D-410 | Rite, advancement, and inscription text contains stale rank, recipe, and reveal timing. | Run a keyed sync after primary-doc decisions, preserving mystery where the player has not earned the truth. |
| D-411 | Defensive status descriptions reverse the early and advanced effects. | Apply R-03 throughout status names, tooltips, rite results, and the progress screen. |
| D-412 | `MaterialsData` repeats all seven Living Weapon IDs. The later copies are categorized as `Materials` and have empty descriptions, while the earlier `Equipment` copies contain actual prose. | Keep one authoritative entry per ID, or give intentional alternate views distinct IDs and synchronized descriptions so lookup/order behavior cannot erase the lore text. |

### 5.6 Wiki drift

The wiki is entirely downstream of the docs under R-01.

| ID | Page | Main drift requiring rewrite |
|---|---|---|
| D-501 | [`Home.md`](../wiki/Home.md) | Legacy degree ladder and reversed/obsolete tendency identities. |
| D-502 | [`Getting-Started.md`](../wiki/Getting-Started.md) | Wrong Mortal Display locations, 1,000-blood requirement, old degrees/ranks, and old Field Notes/Recall behavior. |
| D-503 | [`Harbinger-Path.md`](../wiki/Harbinger-Path.md) | Legacy ladder, old morphling/scar systems, invented callings presented as settled, and misordered Qliphoth endgame. |
| D-504 | [`Blood-Systems.md`](../wiki/Blood-Systems.md) | Wrong starting volume, stale ranks, obsolete `CHARGED` state, fictitious catalog elements, and reversed tendency identities. |
| D-505 | [`Unstained-Path.md`](../wiki/Unstained-Path.md) | Old phase ladder, Copper Altar/spring progression, stale path-switching, and the reversed Verdigris/Silver relationship. |
| D-506 | [`Lore-and-Story.md`](../wiki/Lore-and-Story.md) | Seven-degree history, only two Saints, a falsely clean Vesper ending, and the Mycophant framed as the player's transformation rather than a distinct endgame figure. |
| D-507 | [`Developer-Reference.md`](../wiki/Developer-Reference.md) | Missing Magister rank and other current registry/status omissions. |
| D-508 | [`Public-Alpha-Readiness.md`](../wiki/Public-Alpha-Readiness.md) | Lists non-temple Mortal Display sources and stale journey assumptions. |
| D-509 | [`Advanced-Mechanics.md`](../wiki/Advanced-Mechanics.md), [`Saints-and-Encounters.md`](../wiki/Saints-and-Encounters.md), and [`The-Qliphoth.md`](../wiki/The-Qliphoth.md) | Comparatively current, but must be rechecked against the decisions in sections 4 and 6 rather than treated as authorities. |

## 6. Settled schisms - canonical decisions

These conflicts now have author decisions. The conflict/options/recommendation text is retained as decision provenance; each **Author decision - settled** paragraph is the canonical patch direction.

### S-01 - Exact Qliphoth, Fungal Spine, realm visit, choice, and Apotheos order

**Conflict:**

- `LORE_REFERENCE.md` currently says one must reach Archon, complete Qliphoth Communion, and complete the Rite of Apotheos before receiving the Fungal Spine and entering the fungal realm.
- Later in the same document, Communion unlocks an Apotheos choice, while first exit from the realm triggers the witness dialogue and choice fork.
- Mechanics text and runtime progression also split the trigger among Cult Pruning, the Fungal Podium, realm travel, and `archon_choice_made`.
- Cult Pruning is described as the closing step, but its current state changes can reset or conflict with the flag treated elsewhere as completed Communion.
- The Fungal Podium is available much earlier for meditation, but some prose makes it sound like early access to the endgame realm.
- Both primary references describe a bodyless astral visit to a spherical three-dimensional sliver of the Entity. The mechanics reference also documents `fungal_gardens` as a conventional traversable terrain dimension with biomes, seas, caves, carved stone, and physical-looking travel. Those presentations need one diegetic relationship.

**Decision needed:** Choose one exact sequence; decide whether the runtime `fungal_gardens` identifier is the same place as the Flesh Beyond/Fungal Dimension or a separate place; and decide whether its terrain is an astral body-schema/perceptual translation of the Entity's surface or proof that the player physically enters an ordinary world-like dimension.

**Recommended sequence:**

1. Reach D7 Archon.
2. Shatter the Sanguine Monolith.
3. Perform Bloom of the Qliphoth.
4. Eat all nine pomes from that bloom.
5. Perform Cult Pruning, which seals Communion and expels or awards the Fungal Spine.
6. Use the Spine to visit the Flesh Beyond.
7. The first attempted exit presents the witness choice.
8. Choosing to continue deeper unlocks the Rite of Apotheos; completing it grants D8. Refusal establishes the Silent Archon route.
9. D2 Fungal Podium use remains meditation/resonance only. `fungal_gardens` becomes an internal legacy ID for the same Flesh Beyond unless a separate realm is desired.
10. Preserve the bodyless visit: the traversable biomes, seas, and caves are the player's three-dimensional perception of a locally navigable patch of the Entity, with a temporary body-schema as a gameplay abstraction. They are not a separate fungal planet. If that interpretation is unwanted, split `fungal_gardens` into a separate ecology realm rather than silently rewriting the spherical Entity-surface lore.

**Author decision - settled:** Consuming the ninth and final Qliphoth Pome causes the Fungal Spine to erupt from the Archon. Using it is required once and sends only the player's consciousness into the Fungal Gardens for roughly two minutes. The projection can move and explore but has no carried items, armor, or normal equipment; the physical body retains them. An increasingly rapid red vignette warns that the projection is collapsing, after which consciousness is forcibly returned to the physical body. This first visit presents the revelation and the two endgame responses: perform the Rite of Apotheosis to become the secret D8 Apotheos, or reject the revelation through Cult Pruning and remain D7 as a Silent Archon. The traversable dimension is the temporary three-dimensional experience of the projected consciousness, not the player's physical relocation to a fungal planet.

### S-02 - Universal inherited blood memory versus rare infection

**Conflict:** `LORE_REFERENCE.md` says everyone carries dormant inherited memories, but also says natural infection is rare/acquired and parent-child transmission is unreliable. Some books and fungal dialogue state universal inheritance as literal omniscient truth.

**Options:**

- **A. Rare infection:** Only infected hosts carry the Entity's actionable fungal instructions. Claims of universal inheritance are Harbinger/fungal theology.
- **B. Universal memory, rare activation:** Everyone inherits traces, but only rare infection activates them into hemomancy.
- **C. Universal infection:** Everyone is infected to some degree, contradicting the current rarity and cure framing and requiring a broad rewrite.

**Recommendation:** **A**, because it preserves the horror of detection, the value of purification, and unreliable Order doctrine. If **B** is preferred, explicitly distinguish harmless ancestral residue from active infection.

**Author decision - settled:** Combine rare activation with widespread residue. Most people carry mundane traces because the infection has circulated through the world for at least a millennium, but only a rare minority manifests blood powers. A still smaller minority has the lifelong infectious load and bodily resilience needed to ascend. In theory, an active Harbinger can deliberately increase their load; in practice, most who lack lifelong tolerance perish. Most Harbingers understand blood manifestation and the shared mnemonic link without knowing their fungal source.

### S-03 - Can a Degree 6+ Harbinger become Unstained?

**Conflict:** One lore passage cautiously permits even an Archon to return and describes high-degree defectors as valuable; later lore, mechanics reference text, and runtime gates reject D6+ conversion.

**Options:**

- **A. Hard cutoff:** D6 bloodline integration is biologically irreversible.
- **B. Exceptional cure:** Normal initiation rejects D6+, but a special late quest/rite can sever the Covenant and begin purification.
- **C. Open conversion:** Any degree can begin the standard Unstained route.

**Recommendation:** **B**. It preserves mechanical gating and the lore promise that the Unstained would help a willing defector, while making such a cure appropriately consequential.

**Author decision - settled:** Use the exceptional-cure route, but the hard line is **founding a bloodline**, not merely reaching D5 or joining an existing bloodline. Before founding, ordinary Unstained treatment remains possible. After founding, ordinary rites cannot sever the integration. The player must spare and cure Annetta so she can lead a special cure capable of breaking a founder's Covenant integration.

### S-04 - Saint tendency identities

**Conflict:** Lore calls Hemorath Ferric and Seraphae raw life/radiance. The mechanics reference assigns Hemorath `MORTEM + ANIMUS` and Seraphae `LUX + DUCTILIS`, including their Loom recipes and Canon Memories.

**Decision needed:** Select the official pair for every Saint, then make narrative symbolism, residue recipes, memories, UI colors, and encounter mechanics agree.

**Recommendation:** Consider Hemorath **Ferric + Mortem** and Seraphae **Animus + Lux**. Those pairs more directly express iron permanence/death and uncontrolled life/radiance, but recipe balance must be checked before adoption.

**Author decision - settled:** Hemorath is **Ferric + Mortem**. Seraphae is **Animus + Lux**. Their narrative symbolism, residue recipes, Canon Memories, UI presentation, and encounter mechanics must be synchronized to those pairs.

### S-05 - Is the Unstained prohibition on blades absolute?

**Conflict:** Lore repeatedly describes blunt-only doctrine and says the Unstained do not use blades. Implemented content includes the Silthmere Glaive, Absolution Dagger, Annetta's Absolution Dagger, bows, crossbows, and arrows, with inquiry dialogue endorsing some of them.

**Options:**

- **A. Absolute doctrine:** Remove or re-faction all edged/ranged weapons.
- **B. General doctrine with named exceptions:** Ordinary Unstained avoid cutting blood-bearing flesh, while execution, surgery, relic, or emergency weapons are rare exceptions.
- **C. Guardian-specific doctrine:** Blunt-only applies to a particular order or office, not every Unstained practitioner.
- **D. Field ideal, not law:** Nonlethal/blunt practice is preferred, but the Unstained arm themselves pragmatically.

**Recommendation:** **B** or **C**, because either preserves the distinctive doctrine without discarding a substantial implemented arsenal.

**Author decision - settled:** Blades are allowed, but Unstained doctrine controls infection exposure through weapon length and specialized coatings. Every initiated Unstained carries an **Absolution Dagger** as a last-resort self-defense and mercy tool. Its special hemolytic coating immediately destroys blood and infection in the wound it creates. The Silthmere Glaive is acceptable because its pole length keeps the wielder farther from infectious blood. Other edged weapons must receive an equally explicit doctrinal safety rationale or be removed/reassigned.

### S-06 - When does the Hermit die?

**Conflict:** Lore says the Hermit dies when their heart is taken to form the Mortal Display. Runtime keeps the Hermit present through D8 unless an optional farewell event occurs, and the mechanics reference documents those branches.

**Options:**

- **A. Immediate death:** The initiation heart extraction is final.
- **B. Lingering body:** Removing the heart makes the Hermit a temporary, blood-sustained mentor who dies at a later fixed threshold.
- **C. Choice/branch:** The Hermit can die at initiation or persist for a farewell depending on player action.

**Recommendation:** **B** with a clearly defined final threshold. It retains the body-horror cost and existing mentor content without making the original statement false.

**Author decision - settled:** The Hermit who gives the player a heart dies immediately. Later Hermit dialogue represents other Hermits encountered elsewhere in the world, not the original heart donor surviving. Those Hermits can teach and converse but cannot give the same player another initiating heart.

### S-07 - How is Clarity activated?

**Conflict:** Several mechanics passages say Consecrated Copper is used at the Unstained Podium, while another section and Our Lady dialogue define a dedicated `clarity_ascension` multiblock rite.

**Options:**

- **A. Podium interaction only.**
- **B. Multiblock rite only.**
- **C. Both stages:** the Podium diagnoses/readies the practitioner, while the multiblock rite performs the actual ascension.

**Recommendation:** **C**, because it reconciles the current objects and creates a clearer narrative threshold. The exact item consumption and advancement trigger must then be documented once.

**Author decision - settled:** Use the staged route. The Podium and Consecrated Copper ready the fully Purified practitioner; the Rite of Clarity Ascension performs the actual transition and sets `clarityUnlocked`.

### S-08 - Is Ancestral Communion/Fungal Voice telling objective truth?

**Conflict:** Dialogue branches are labeled as the full truth while making claims that contradict established lore, including that the mycelium pruned the Unstained and that Our Lady is merely scar tissue. Current canon says former Harbingers founded the Unstained and Our Lady predates the fungal arrival.

**Options:**

- **A. Objective developer truth:** Rewrite primary lore to accept the voice's account.
- **B. Self-serving fungal theology:** Keep the voice persuasive but explicitly unreliable in author-facing documentation.
- **C. Mixed truth:** Identify, line by line, which revelations are factual and which are manipulation.

**Recommendation:** **C**, with speaker-facing prose never labeled "full truth" unless all literal contradictions are removed. The Entity should know more than the Orders without becoming an infallible exposition device.

**Author decision - settled:** The fungal voice does not knowingly lie, but it never expresses a truth that is not self-serving. This is not calculated deception: its worldview sincerely treats whatever best serves the Entity as best for the practitioner and the world. Its factual claims must still agree with canon; contradictions should be rewritten as truthful observations interpreted through this absolute, alien value system rather than preserved as literal falsehoods.

### S-09 - What causes the Blood Moon's mixed effects?

**Conflict:** Primary lore describes the Blood Moon as Our Lady's immune burst. Mechanics strengthen Harbingers, discount the Loom, spawn fungal creatures, and drain or weaken others, which reads like a fungal surge. The Rite of Lethean Tide can also terminate an active Blood Moon and rewards Purity, which is difficult to explain if the event is already Our Lady's own desired purge.

**Options:**

- **A. Lady's purge with fungal counter-surge:** Her attack forces the infection to bloom defensively, explaining both sets of effects.
- **B. Fungal surge contained by Our Lady:** The Moon is primarily the Entity's event; pale effects are her containment response.
- **C. Two alternating event types:** Separate red/fungal and pale/immune moons mechanically and narratively.

**Recommendation:** **A**. It preserves the current name/event and turns the apparent contradiction into ecological conflict.

**Author decision - settled:** The Blood Moon is a fungal surge. Its Harbinger empowerment, Loom discount, and fungal spawning are direct expressions of that surge. Pale and Unstained effects are Our Lady's containment response, and the Rite of Lethean Tide ends the event by strengthening that response.

### S-10 - What is Pale Silver made from?

**Conflict:** Lore says silver does not occur naturally and Pale Silver is consecrated copper. Current mechanics include iron plus Pale Distillate, and White Humor purification can transform Hematic Iron Blocks into Pale Silver Blocks. Some inquiries describe natural ore.

**Options:**

- **A. Consecrated copper only:** Rewrite recipes and all iron-conversion mechanics.
- **B. Purified hematic iron:** Rewrite lore's copper claim and define the resulting pale metal.
- **C. Ritual state, multiple substrates:** "Pale Silver" is a purified material phase attainable from copper or hematic iron through different rites; it never occurs as natural ore.

**Recommendation:** **A**, because the established lore is explicit and game mechanics are downstream of it. Choose **C** only if retaining both implemented conversion paths is an intentional new canon decision. Natural ore should be rejected under all three options unless explicitly added.

**Author decision - settled:** Pale Silver is made only as the next ritual material tier after Consecrated Copper. It is never natural ore and is not purified iron or Hematic Iron. Recipes and transformations that currently create it from iron must be replaced with the Consecrated Copper progression.

### S-11 - Is the Pallid Icon unique or ordinarily craftable?

**Conflict:** Lore presents a rare original relic found at the bottom of a forgotten river. Mechanics allow standard crafting and place Icons broadly with Guardians.

**Options:**

- **A. Unique relic:** Remove ordinary crafting and common placement.
- **B. Devotional type:** Rewrite the river account as one famous example among many.
- **C. Original plus copies:** The river relic is the original; crafted icons are consecrated devotional reproductions with limited apparitions.

**Recommendation:** **C**.

**Author decision - settled:** The river relic is the original Pallid Icon. Later Unstained learned to make and consecrate devotional copies. Downstream prose must distinguish the historically unique original from reproducible consecrated icons and define any difference in apparition strength as a mechanical balance detail rather than a second origin.

### S-12 - Degree 8 name: Silence or Apotheos?

**Conflict:** The degree table says the eighth threshold is "The Silence" with no formal title, while mechanics and other lore call Degree 8 `Apotheos` and name its rite accordingly.

**Options:**

- **A. Apotheos is the formal Degree 8 title.**
- **B. The Silence is the title; Apotheos names only the fungal route/rite.**
- **C. No Order title:** Apotheos is a technical state/rite label, while "the Silence" is the secret in-world description shared by both endgame responses.

**Recommendation:** **C**. It preserves both terms and reinforces the claim that the ordinary Order has no public rank beyond Archon.

**Author decision - settled:** Degree 8 is **Apotheos**, reserved solely for a player who performs the Rite of Apotheosis after the revelation. **Silent Archon** is not Degree 8: it is the title and flagged state of a D7 Archon who reached the revelation through Qliphoth Communion and then rejected it through Cult Pruning. That D7 state unlocks its own crafting and powers.

### S-13 - When is a bloodline founded, and what changes at Degree 6?

**Conflict:** Canon clearly reserves the full Bloodline Covenant - merged pools/capabilities and Covenant machinery - for D6. Founding is less consistent: lore associates ordinary bloodline founding and the Founding Fane with D5, the `bloodline_founding` rite is D5, but direct use of an Unsigned Ancestral Ledger currently creates a bloodline without a degree gate, and some progression surfaces present joining/creating a bloodline earlier.

**Options:**

- **A. D5 founding, D6 Covenant:** D5 permits creation/joining and the Founding Fane; D6 unlocks shared pools, capability merging, Covenant Throne, and full ritual severance.
- **B. Early social bloodline, D5 Fane, D6 Covenant:** D2 or another chosen early degree permits names/membership only; D5 consecrates territory; D6 activates supernatural sharing.
- **C. D6 for everything:** no true bloodline exists before Sanctified.

**Recommendation:** **A**, because it most directly follows `LORE_REFERENCE.md` sections 6.4a/6.5 and the D5 founding rite while preserving D6 as a major transformation. Gate or repurpose the direct Ledger shortcut accordingly.

**Author decision - settled:** Use D5 founding and D6 Covenant deepening. D5 permits bloodline creation and the Founding Fane. D6 expands the bloodline's supernatural powers, shared systems, and Covenant machinery.

### S-14 - How does Purity begin: Podium or Lethean Baptism?

**Conflict:** `HEMOMANCY_REFERENCE.md` says Hemolytic Solution at the Unstained Podium sets `begunPurification` and grants starting Purity. Its rite table separately says the Rite of Lethean Baptism performs the same initiation. Inquiry prose adds a third, incorrect Altar-of-Cleansing claim.

**Options:**

- **A. Podium only:** Hemolytic Solution at the Podium is the sole initiation; Baptism becomes a later blessing or tutorial rite.
- **B. Rite only:** the Podium prepares/diagnoses the candidate; Lethean Baptism performs initiation.
- **C. Equivalent routes:** either the Podium interaction or the rite can begin Purity, explicitly documented as clinical versus liturgical entry.
- **D. Two required stages:** Podium treatment makes the biological break, then Baptism formally admits the practitioner and exposes the Purity progression.

**Recommendation:** **D** for the strongest narrative threshold, or **A** for the smallest mechanics correction. The Altar should remain later under either choice.

**Author decision - settled:** Use two required stages. Podium treatment slows or suppresses the existing infection to a manageable level but does **not** begin the Unstained path. The Rite of Lethean Baptism formally starts the path and its Purity progression. The Altar of Cleansing remains a later station.

### S-15 - Who teaches early blood structures: Alchemist or Vicar?

**Conflict:** Older mechanics prose assigns early Blood Absorption/Projection and structures to the Alchemist. Current lore, runtime dialogue, and the recent Degree 2 journey design assign the Vicar a larger teaching role.

**Options:**

- **A. Vicar teaches doctrine and core blood structures; Alchemist teaches processing/tendencies.**
- **B. Alchemist teaches all physical bloodcraft; Vicar handles only rank and doctrine.**
- **C. Joint lesson with clearly divided steps.**

**Recommendation:** **A**, matching the most recent primary lore and current journey structure.

**Author decision - settled:** The Vicar teaches the actual Blood Absorption/Projection process and the first useful recipes. Other specialist NPCs may supply contextual hints, reminders, or recipe unlocks when the player has not learned the relevant recipe or has not yet seen it in the Sanguine Conduit interface.

### S-16 - Defensive Unstained doctrine versus offensive/kill progression

**Conflict:** The Unstained are framed as defensive, curative, and nonconversionist, yet gameplay rewards some kills and includes war/dominion language and effects in rites such as Lethean Judgment and Lethe Covenant. This also affects S-05's weapon doctrine.

**Options:**

- **A. Strictly defensive:** Replace kill rewards and dominion language with containment, cure, protection, or mercy objectives.
- **B. Defensive faction with militant orders:** General doctrine rejects conquest, but Guardians/Zealots use lethal force against active infection and irrecoverable threats.
- **C. Open holy war:** The faction is willing to conquer and eradicate Hemomancy broadly.

**Recommendation:** **B**, provided dialogue distinguishes mercy toward infected people from violence against uncontrolled manifestations.

**Author decision - settled:** The Unstained are primarily defensive and conservative about violence. Guardians, and more rarely Scouts, form the faction's military response when force is unavoidable. Every initiated Unstained carries an Absolution Dagger for last-resort self-defense or mercifully ending an irrecoverably suffering individual; this does not turn the general faction into a conquering holy army.

## 7. Provisional, adopted, or deferred lore rationalizations

This section records the final treatment of lore that began the review as unsupported. An entry marked adopted must be promoted into `LORE_REFERENCE.md`; an entry marked provisional remains observation, rumor, doctrine, or speaker theory; a deferred detail must not be invented while its broader premise is synchronized.

| ID | Unsupported addition | Safe interim treatment |
|---|---|---|
| P-01 | Dragon Eggs embody special life potential within Hemomancy cosmology. | Alchemist speculation unless added to the cosmology section. |
| P-02 | Enchanting schools have fixed one-to-one blood-tendency correspondences. | Practical classification used by a speaker, not a universal law. |
| P-03 | Water and blood share a literal primordial origin. | Liturgical metaphor or hypothesis. |
| P-04 | The Nether is a skinless organ or exposed interior of the world. | Voyager/surveyor interpretation, not developer narration. |
| P-05 | A beacon attracts everything sharing a single inheritance. | Observed behavior unless the inheritance model is settled under S-02. |
| P-06 | Only one person has ever reached the eighth threshold. | **Premise adopted; identity deferred.** One individual completed Apotheosis and returned in a state that still remotely resembled their former self. Other Apotheos cease to resemble former Harbingers and lose meaningful human communication, which is why Apotheos is not recognized publicly as an ordinary degree. The survivor's name and full story remain intentionally unwritten for now. |
| P-07 | Various creatures have precise curative, ecological, or factional properties stated only in one inquiry. | **Ecological rationale adopted.** Most Hemomancy creatures are not directly infected or manufactured by the Entity. They evolved alongside the infection's millennium-scale influence and incorporated its ecological pressures into natural adaptations such as hematic-iron shells/spikes, synaptic abilities, and specialized venom. Individual curative claims still require species-specific support. |
| P-08 | Invented Harbinger callings and offices found primarily in the wiki. | Remove from the factual ladder or label as local/optional roles after adoption. |
| P-09 | The proposed consolidation from twelve morphlings to eight original fungal strains. | **Implemented and adopted.** Current-facing documentation must use the eight strain identities and remove claims that Morphlings map one-to-one to twelve animal species. Historical plans remain provenance. See D-009. |
| P-10 | Silthmere is treated downstream as a specific historical person, while primary lore leaves the name undefined. | **Adopted:** Silthmere is a title of Our Lady of Still Waters - a shorter, name-like liturgical title - not a mortal historical person. Tears of Silthmere, the Glaive, and the remembrance rite derive from that title and must not invent a separate Silthmere biography. |
| P-11 | Rogue Hemomancer Will origin theories are spoken as fact even though `DEFERRED_IDEAS.md` explicitly leaves their origin open. | **Adopt the implemented design origins:** Broken Wills/Faded are remnants of failed former Harbingers; Sent Wills/Proctors are purpose-built by the Fungal Entity to assess ripening practitioners; Blood Drunk Puppeteers are a type of Broken Will pulled by the hive's strings. Their commandeering and observable mechanics should be incorporated into `LORE_REFERENCE.md`. |

## 8. Archive and tooling drift

| ID | Source | Finding | Recommended handling |
|---|---|---|---|
| A-01 | `src/main/resources/assets/hemomancy/Hemomancy Lore.txt` | Stale prose still presents itself as authoritative, but no current runtime path depends on it. | Add a prominent legacy/noncanonical header, relocate it to an archive, or remove it after confirming packaging intent. |
| A-02 | `Notes.txt` | Working ideation mixed with old lore claims. | Label as notes/proposals; never use as automatic canon input. |
| A-03 | Root `README.md` | Largely an untouched NeoForge/template readme. | Keep as project setup documentation or replace separately; do not mine it for lore. |
| A-04 | `docs/superpowers/plans/` and `docs/superpowers/specs/` | Valuable design history, including superseded concepts. | Keep historical wording intact and add an archive/proposal convention rather than rewriting old plans to pretend they always matched canon. |
| A-05 | Legacy dialogue-editor snapshots | Ten snapshots are stale; two are empty; seven current factories are absent; snapshot localization contains 1,645 keys versus 3,384 runtime keys. | Treat snapshots as unused archives or regenerate from live sources. The modern studio already reads live Java/runtime language data. |
| A-06 | Generated/copied language resources | Some copies lag behind the main resource language file. | Make `src/main/resources/assets/hemomancy/lang/en_us.json` the generation source and validate copies during build/tooling checks. |

## 9. Implementation dependency order

The schisms are settled. Applying them in this order will minimize repeated edits:

1. **S-01:** endgame sequence, realm identity, embodiment, and geometry.
2. **S-02:** infection and inherited-memory model.
3. **S-13:** bloodline founding versus the D6 Covenant.
4. **S-03 and S-14:** late Harbinger conversion and the initial Purity route.
5. **S-07:** Clarity activation.
6. **S-05 and S-16:** Unstained weapon and force doctrine.
7. **S-10:** Pale Silver composition.
8. **S-08 and S-09:** fungal narrator reliability and Blood Moon causality.
9. **S-04:** Saint tendency pairs.
10. **S-06, S-11, S-12, and S-15:** remaining character, item, terminology, and teacher-ownership decisions.

## 10. Patch order after review

1. Record every accepted schism decision in `LORE_REFERENCE.md` first.
2. Reconcile mechanics and implementation status in `HEMOMANCY_REFERENCE.md` without allowing current runtime behavior to overrule lore.
3. Update focused reference documents that contain adopted material.
4. Synchronize runtime NPC dialogue and localization.
5. Synchronize inquiry JSON, all in-game books, MaterialsData, advancements, rites, inscriptions, and hardcoded tooltips.
6. Rebuild the wiki from the settled reference documents.
7. Label or regenerate archives and editor snapshots.
8. Run structural validation for JSON, missing localization keys, orphaned dialogue keys, book page references, inquiry references, and advancement descriptions.
9. Run focused tests for any mechanics changed to honor a lore decision, followed by the normal project verification suite.

## 11. Settlement summary

- R-01 through R-03 are settled and require synchronization.
- S-01 through S-16 are settled and require primary-reference updates before downstream patching.
- P-06, P-07, P-09, P-10, and P-11 now contain adopted premises or rationalizations.
- P-06's survivor identity/story remains deliberately deferred.
- P-01 through P-05 and P-08 remain provisional unless separately adopted later.
