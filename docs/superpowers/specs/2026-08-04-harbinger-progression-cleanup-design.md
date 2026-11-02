# Harbinger Progression Cleanup and Brainstorm Catalogue

- **Status:** Design catalogue, not an implementation specification
- **Path covered:** First Harbinger Hermit encounter through the Silent Archon outcome
- **Implementation baseline:** NeoForge 1.21.1 checkout as of 2026-08-04
- **Design posture:** Targeted additions and redistribution, not a wholesale rewrite

## Purpose

This document evaluates how the implemented Harbinger journey currently feels to a survival player and collects concrete ways to improve it. It deliberately separates four kinds of statements:

- **Confirmed current state** describes behavior present in the checkout.
- **Recommended direction** is the preferred progression shape for a later implementation specification.
- **Alternate idea** is worth retaining but is not the default proposal.
- **Playtest question** cannot be settled reliably through source inspection alone.

The central recommendation is a **degree-chapter spine**. Each degree receives one clearly presented main chapter which teaches or proves that degree's identity. Completing that chapter records one persisted milestone and opens the next rank rite. Equipment paths, field research, puppetry, morphlings, and other careers remain optional; none may become a hidden prerequisite for a rank rite.

This catalogue does not change gameplay, recipes, dialogue, world generation, or persistence.

## Current Player Experience

### Executive diagnosis

The Harbinger journey now has a strong beginning and a strong collection of individual systems, but it does not yet feel like one evenly paced ascent.

- **The Hermit and Sanguine Initiation are the strongest onboarding sequence.** Consent is explicit, the Hermit's heart and death give the choice weight, the supplied blueprint provides a physical objective, and the first rite intentionally omits every ritual verb the player has not learned.
- **Degree 1 turns powers into actions effectively.** First Bloodcraft teaches Absorption, Projection, guide access, and Hematic Iron through concrete production rather than a lecture.
- **Degree 2 is assignment-heavy.** The ledger exposes five Degree-2 sections while the player is also learning alchemy, field sampling, Bestiary capture, armor upgrading, and their first self-built rank rite.
- **Degree 3 is dense but thematically coherent.** Memory weaving, armor forks, and practical puppetry all express blood-memory becoming external craft, but they compete for the player's attention.
- **Degree 4 is the best complete middle chapter.** The Vein-Mason has a destination, specialist teacher, hands-on sequence, visible completion state, and continuation reward.
- **Degree 5 is system-heavy but under-focused.** Bloodline Founding, Fanes, Blood Lust, morphlings, advanced puppetry, recruitment, deep skills, and many utility rites arrive together. The nominal Crimson Lodge and covenant identity is not the main journal experience.
- **Degree 6 has systems but no journey.** The Chamber of Will, Covenant Throne, Covenant Vigil, helper roles, advanced covenant abilities, and the Archon rite exist, but the ledger has no Degree-6 section connecting them.
- **Degree 7 has spectacle but not a final refusal ordeal.** The Monolith, Bloom, nine pomes, Fungal Spine, projection, and revelation form a coherent sequence. Current Pruning, however, immediately changes `SILENT_PENDING` to `SILENT_ARCHON`; the implemented two-phase Vesper boss is not integrated into that path.

The desired emotional curve is:

> Invitation -> apprenticeship -> experimentation -> specialization -> covenant leadership -> stewardship -> revelation -> refusal.

The current curve is closer to:

> Invitation -> strong tutorial -> several parallel catalogues -> strong scar quest -> many unlocks -> sparse guidance -> revelation and gear.

## Sources of Truth Consulted

The conclusions below are grounded primarily in:

- Hermit, Vicar, Alchemist, Mnemonist, Vein-Mason, Artificer, Monolith, and Fungal Whisper dialogue trees.
- `HarbingerAssignmentLedgerScreen.AssignmentSection` and its underlying advancement flags.
- Cardinal rank-rite JSON from Sanguine Initiation through the Archon rite.
- `HarbingerCardinalRiteEvents`, `DialogueEventHandler`, and `InitiatoryDegree` path state.
- `QliphothBloomEvents`, `QliphothBloomSavedData`, Fungal Spine travel, and Chamber of Will state.
- `VesperTheCrownedRefusalEntity`, `VesperTheEveningStarEntity`, renderers, sounds, loot, and Living Staff memory integration.
- `wiki/Harbinger-Path.md`, `docs/HEMOMANCY_REFERENCE.md`, and older readiness notes where they describe player-facing expectations.

## Progression Density Matrix

The counts below are experiential, not raw registry totals. Armor-piece variants count as one equipment family; a multi-step assignment counts as one chapter.

| Stage | Current identity | Ledger load | Major new systems | Rank-rite vocabulary | Guidance strength | Player-feel risk |
| --- | --- | ---: | --- | --- | --- | --- |
| Hermit / D0 | Explicit invitation and bodily awakening | None | Mortal Display, blueprint, prepared Focus | Temple medium; no anchors, offerings, sigils, fog, waves, staff, or helpers | Strong inside the temple | Finding the next Outpost is not materially supported |
| D1 Neophyte | Learn to carry and shape blood | 2 sections | Absorption, Projection, Liber, Hematic Iron, first structures | Player-built floor, iron medium, four anchors | Strong through Vicar dialogue and First Bloodcraft | Ledger arrives through the Hermit Road side route instead of at the start of formal assignments |
| D2 Votary | Sampling, separation, and workshop formation | 5 sections | Centrifuge, samples, enzymes, Bestiary, first Armature tier | Four anchors; Living Staff begins at the next rite; one offering and faint fog | Individually good, collectively noisy | Too many equally visible jobs before a clear main loop settles |
| D3 Initiate | Deliberate memory and externalized blood forms | 2 sections plus puppetry | Somatic Loom, indexed memory, armor forks, two puppet shapes | Living Staff, four anchors, one offering, faint fog | Good NPC teaching, but several headline systems overlap | Main chapter can be lost among gear and summon branches |
| D4 Adept | Deliberate cerebral alteration | 1 section | Vein-Mason journey, scars, Effigy loadouts, stronger staff use | Required support sigil, dense fog, offering-loss stakes | Strongest middle-tier guidance | Rank-up still does not prove scar practice occurred |
| D5 Illuminatus | Lodge secrets and covenant founding | 2 sections plus many optional systems | Bloodline, Fane, Blood Lust, morphlings, recruitment, advanced puppet, deep branches | Eight anchors, storm fog, lightning, first ordeal pressure | Fragmented across NPCs and tabs | Most crowded degree; covenant identity is skippable |
| D6 Sanctified | Covenant stewardship | 0 sections | Chamber, Throne, helper roles, Covenant Vigil, Eternal Covenant, advanced scars/covenant skills | Twelve anchors, two required sigils, dome, three waves, required helper | Weak despite substantial content | Feels like a recipe tier, not a lived rank |
| D7 Archon | Truth beneath the Order and the final choice | 1 equipment section | Monolith, Bloom, nine pomes, Spine, Fungal Gardens projection, path choice | Grand ceremonial mastery | Strong event-to-event clues | Passive repetition and no mandatory refusal confrontation |
| Silent Archon | Willpower after rejecting Apotheosis | No journey section | Vestments, Chamber theme, Severance, Slipping, refusal, Will bonuses | Current state is finalized by small Pruning | Strong mechanical reward | Title is granted before Vesper and lacks a final earned ordeal |

### Current ledger distribution

The current ledger contains:

- Degree 1: First Bloodcraft and Hermit Road.
- Degree 2: First Separation, Red Taxonomy, Living Bestiary, Eightfold Centrifuge, and The Worn Vow.
- Degree 3: The Woven Vessel and The Three Answers.
- Degree 4: The Vein-Mason.
- Degree 5: Crimson Vestment and The Assumed Limb.
- Degree 6: no assignment section.
- Degree 7: Weight of the Frame.

This distribution is the clearest structural evidence for the crowded-D2 / empty-D6 problem.

## Design Principles

### One chapter proves one degree

Every degree should answer four player questions:

1. What new relationship to blood did I earn?
2. Who or what teaches it?
3. What must I actually do with it?
4. Why does that prove I am ready for the next rite?

The main chapter should require only the central lesson. Optional mastery, collections, equipment completion, and build specialization remain side content.

### Rites certify; they do not replace the chapter

The rank rite remains the ceremonial climax of a degree, but it should not be the entire progression gate. The intended flow is:

> Reach degree -> receive main chapter -> perform defining proof -> unlock next rank blueprint -> complete rank rite.

The rank rite should check one chapter-completion milestone. It should not independently inspect a long collection of unrelated advancements, items, or side-career state.

### Optional means visibly optional

The ledger should distinguish four categories:

| Category | Purpose | Can gate rank? | Presentation |
| --- | --- | --- | --- |
| Main Chapter | Teaches the degree's defining verb | Yes, exactly one chapter per degree | First and visually dominant |
| Side Assignment | Authored expedition or compact specialist task | No | Nested beneath its degree |
| Vocation | Long-running equipment, puppetry, morphling, or specialist career | No | Separate continuous section grouped by teacher |
| Background Catalogue | Passive collection such as Bestiary or enzyme mastery | No | Collapsed by default; progresses without becoming a task wall |

### Mandatory shared play must remain solo-compatible

Where a chapter teaches helpers or covenant roles, either another player or an eligible recruited Harbinger NPC must satisfy it. The design must never require a second human account.

### New ritual vocabulary must be taught before it is tested

The existing ceremony progression remains the correct foundation:

- D0 teaches consent and a prepared Focus.
- D1 teaches building and four boundary anchors.
- D2-D3 teach Living Staff activation, a small offering, and faint atmosphere.
- D4 teaches required inscription.
- D5 teaches ordeal pressure, storm atmosphere, and larger formations.
- D6 teaches required helper presence, Grand structure, domes, and collapse risk.
- D7 assumes mastery and may combine the full vocabulary.

### Existing saves must receive credit

Any later implementation must derive chapter completion for players who already possess the defining achievement. Examples include an existing founded bloodline and Fane, an already completed first Loom weave, or a recorded first scar loadout. The cleanup must not force established players to destroy and recreate persistent world structures.

## Recommended Degree-Chapter Spine

### D0 -> D1: The Invitation

#### Confirmed current state

The Hermit first offers a temple-bound blessing. Claiming the Mortal Display produces a dormant Vascular Charm but does not initiate the player. Accepting guidance leads to the Hermit's farewell and a filled Sanguine Initiation blueprint. The temple rite uses its prepared Focus, an iron nugget medium, a nonlethal health payment, no anchors, and no other ceremony machinery.

#### Recommended direction

Preserve this sequence almost exactly. It correctly separates choice, consequence, instruction, and transformation.

Add one post-completion handoff: successful Sanguine Initiation should expose a concrete route to a Harbinger Outpost. The Hermit's final words already name the Outpost; the world must now answer that instruction.

#### Outpost breadcrumb ideas

**Recommended:** Award a one-use or persistent explorer-map-style Covenant Waybill when Sanguine Initiation succeeds. It should point to a real generated Harbinger Outpost and remain useful until the player reaches one.

**Alternate idea A:** The initiation blueprint gains a second, sealed memory after completion which projects a directional blood thread toward the nearest Outpost.

**Alternate idea B:** A brief post-initiation blood echo marks the nearest Outpost in chat/advancement guidance and grants a standard map with a Harbinger icon.

**Alternate idea C:** Add a true Harbinger structure compass, distinct from the current Hearty Compass, and grant its starter form at initiation.

#### Reward possibilities

- The route item alone; avoid adding another combat or crafting reward to an already generous transformation.
- A small ash/formation travel cache only if early material starvation appears in playtests.

#### Risks and dependencies

- Structure-map generation must handle worlds with no nearby eligible Outpost without hanging or pointing at an invalid location.
- The reward should occur after initiation, not merely after accepting the Hermit, so declining the rite preserves meaningful consent.
- Multiplayer temples must not grant duplicate progression items endlessly.

#### Solo compatibility

Complete. No helper is required.

#### Playtest questions

- Does the player understand the difference between claiming the heart and becoming initiated?
- Is the blueprint legible without opening the full Sanguine Conduit interface?
- How far can the first Outpost be before the handoff becomes travel fatigue rather than anticipation?

### D1 -> D2: First Bloodcraft

#### Confirmed current state

First Bloodcraft tracks reaching Degree 1, holding at least 5,000 ml, producing a Liber Sanguinium, and producing a Hematic Iron Block. Its Vicar hand-in grants a concrete starter cache. Hermit Road separately tracks a wild remnant and currently grants the assignment ledger after the player reports that remnant.

#### Problem

The formal assignment journal is obtained through a side expedition after the player may already have met the Vicar and begun First Bloodcraft. The player can therefore receive the central tracking interface only after making progress it was supposed to organize.

#### Recommended direction

- The first Degree-1 Vicar conversation grants the ledger once.
- First Bloodcraft is the D1 Main Chapter and is expanded by default.
- Hermit Road is immediately visible as a D1 Side Assignment.
- Completing the First Bloodcraft hand-in records the single milestone that opens or reveals the Votary rite blueprint.
- Previously completed item/advancement steps remain credited.

#### Alternate ideas

**Alternate idea A:** The Hermit's blueprint transforms into the ledger after initiation. This creates a clean physical continuity, but loses the Vicar's role as the person who formally tasks the player.

**Alternate idea B:** Keep the ledger as the Hermit Road reward, but give First Bloodcraft a separate temporary task card. This preserves the current reward but needlessly duplicates guidance surfaces.

#### Reward possibilities

- Preserve the existing First Bloodcraft material reward unless survival testing finds it excessive.
- Let Hermit Road reward exploration-specific materials, a remnant folio page, or cosmetic record rather than the essential ledger.

#### Risks and dependencies

- The Vicar event must be one-time and recoverable if the ledger is lost.
- Existing players with `hermit_road_ledger_granted` must not receive contradictory dialogue.
- First Bloodcraft should not require Hermit Road completion.

#### Solo compatibility

Complete.

#### Playtest questions

- Is 5,000 ml an appropriate first explicit capacity target with an unskilled vessel?
- Do players discover reliable Sanguine Formation projection without external documentation?
- Does the material hand-in naturally prepare the four-anchor Votary rite?

### D2 -> D3: The First Separation

#### Confirmed current state

Degree 2 currently presents the highest ledger density. First Separation teaches the Vial Centrifuge and rewards a sampling kit. Red Taxonomy records four flora categories but has an explicitly placeholder final reward. Eightfold Centrifuge records all eight enzymes. Living Bestiary records ecology captures. The Worn Vow begins the Armature equipment spine.

#### Problem

All five sections appear at the same degree and several read as equivalent assignments. The player cannot easily tell which activity advances their understanding of the rank and which is a collection, a vocation, or optional ecology study.

#### Recommended direction

- First Separation becomes the only D2 Main Chapter.
- Its existing compact lesson remains sufficient: obtain the station, carry a valid sample, begin a separation, recover any enzyme, and return to the Alchemist.
- Red Taxonomy and Eightfold Centrifuge move under a collapsed **Alchemical Fieldwork** group.
- Living Bestiary becomes a Background Catalogue and remains accessible from the Progress screen.
- The Worn Vow leaves the degree's main assignment list and begins a persistent **Artificer Vocation** section.
- Claiming the First Separation reward records the milestone that opens the Initiate rite blueprint.

#### Alternate ideas

**Alternate idea A:** Move Red Taxonomy to D3, where deliberate memory work already needs specific enzymes. This reduces D2 density but risks making D3 even busier.

**Alternate idea B:** Introduce Living Bestiary only after the first Specimen Jar is filled. This avoids presenting an empty catalogue before the player has encountered its verb.

**Alternate idea C:** Keep all content at D2 but present only the next recommended card; completed or deliberately expanded cards reveal the rest. This fixes UI pressure without fixing thematic density.

#### Red Taxonomy reward ideas

**Recommended:** A practical Mycological Assay Kit made from existing progression items: several empty Specimen Jars, enzyme primers, and a small supply of sampling consumables. Exact counts should be tuned in survival testing.

**Alternate reward A:** Unlock a minor Alchemist-only recipe that converts duplicate registered flora into an enzyme primer.

**Alternate reward B:** Grant a reusable field-sampling tool with no combat value. This is more distinctive but adds an item solely to solve a reward gap.

**Alternate reward C:** Grant a decorative catalogue specimen set for players who prefer collection prestige over economy.

#### Risks and dependencies

- Grouping changes must not obscure passive progress already being recorded.
- The reward cannot invalidate centrifuge sampling or make all eight enzyme types trivial.
- Optional fieldwork should remain discoverable after the main chapter is complete.

#### Solo compatibility

Complete.

#### Playtest questions

- How long does one valid sample-to-enzyme cycle take without JEI knowledge?
- Is the sampling kit arriving after the first separation satisfying or backwards?
- Do Red Taxonomy and Bestiary feel meaningfully different in play, or like two lists of nearby organisms?

### D3 -> D4: The Woven Vessel

#### Confirmed current state

The Woven Vessel asks the player to craft a blank Hematic Memory, have the Mnemonist index it using archive materials, and complete a first Somatic Loom weave. Degree 3 also opens two puppetry ordeals and the Barbed/Chitinite/Prismatic armor forks.

#### Problem

The degree has a strong conceptual center, but three headline systems compete: deliberate memory, equipment identity, and summoned bodies. A player can also perform the next rank rite without proving that the Loom lesson happened.

#### Recommended direction

- The Woven Vessel remains the D3 Main Chapter.
- The Mnemonist explicitly frames puppetry as memory given a body and unlocks its trial guidance after the first weave.
- The Three Answers stays in the Artificer Vocation rather than beside the main chapter.
- Completing the first deliberate weave records the milestone that opens the Sanguine Brotherhood rite blueprint.
- Puppet trials remain optional permanent-shape unlocks.

#### Alternate ideas

**Alternate idea A:** Require one puppet ordeal after the first weave. This makes the degree more dramatic but forces a combat/summon career on players who may prefer manipulation craft.

**Alternate idea B:** Make the first weave itself produce a choice among an early manipulation memory, a puppetry starter, or an armor reagent. This gives expression but complicates tutorial consistency.

#### Reward possibilities

- Preserve the current Blood Shot starter materials if they reliably lead to a valid first Loom recipe.
- Grant the first blank blueprint or Folio imprint for a selected memory family.
- Avoid granting a completed high-rank manipulation; the chapter should teach creation rather than skip it.

#### Risks and dependencies

- The selected first weave must have ingredients realistically obtainable at D3.
- Mnemonist dialogue must distinguish memory-item preparation from permanent manipulation learning.
- Puppetry recipe discovery should not appear before the Crossbar and Spindle loop is explainable.

#### Solo compatibility

Complete. Puppet trials, if attempted, are solo-capable ordeals.

#### Playtest questions

- Can a new player complete the Loom minigame after only NPC, ledger, and recipe-map guidance?
- Does delaying practical puppetry until after the first weave improve comprehension?
- Are the three armor forks meaningful choices or merely completion burdens at this point?

### D4 -> D5: The Vein-Mason

#### Confirmed current state

The Vicar provides a map to the remote Cicatrix Anchorite. The current journey has four strong actions: find the teacher and receive a tendency-shaped pattern, carve and learn the first scar, prepare an Effigy pattern, and burn it to change the active loadout. Returning grants a continuation kit.

#### Recommended direction

Preserve the content and make completion of the first active scar loadout the D4 chapter milestone that opens the Crimson Lodge rite blueprint.

The player is not required to master every scar, fill all available sockets, or complete fungal scar cultivation. The proof is that they deliberately changed their cerebral architecture once.

#### Alternate ideas

**Alternate idea A:** Require only learning the first scar, not applying a loadout. This is shorter but fails to teach the Effigy and active-loadout distinction.

**Alternate idea B:** Let the player choose between the Vein-Mason scar proof and a manipulation mastery proof. This supports more play styles but weakens Degree 4's strongest authored identity.

#### Reward possibilities

- Preserve the continuation kit.
- Add a cosmetic scar-pattern record to the Chamber or ledger rather than more power.
- Reveal the first fungal-scar rumor without granting access early.

#### Risks and dependencies

- Players with an existing recorded active scar loadout must receive retroactive completion.
- The map must remain recoverable if lost.
- The tendency-selected first pattern must always have obtainable ingredients.

#### Solo compatibility

Complete.

#### Playtest questions

- Is travel to the Vein-Mason memorable or merely long?
- Does the player understand known scars versus currently active scars?
- Is the continuation kit enough to make scars feel like a lasting system rather than a one-quest station?

### D5 -> D6: Found the Covenant

#### Confirmed current state

Degree 5 unlocks Bloodline Founding, Founding Fane eligibility, recruitment, Blood Lust progression, the Morphling Incubator, additional bloodline and world rites, the Mnemonist Puppet ordeal, and deep skill branches. Current Degree-5 ledger content emphasizes Crimson Vestment and The Assumed Limb. A player can perform the Sanctified rite without founding a bloodline or Fane.

#### Problem

The rank is named for the Crimson Lodge and leads directly to **Sanctified of the Bloodline Covenant**, yet permanent covenant formation is optional. The largest cluster of systems in the progression therefore lacks one central reason to exist.

#### Recommended direction

Create a D5 Main Chapter tentatively titled **A Covenant Written in Place**:

1. Learn Bloodline Founding from the Vicar/Lodge guidance.
2. Complete the Bloodline Founding rite and become the valid founder of a covenant.
3. Prepare the Fane catalyst and complete Founding Fane.
4. Confirm the first Fane heart and usable boundary.
5. Return to the Vicar or interact with the established heart to record the chapter milestone.
6. Reveal the Sanctified rite blueprint.

A one-person bloodline is valid. Recruitment is encouraged afterward but is not required to become Sanctified.

Crimson Vestment and The Assumed Limb move into their continuous Artificer Vocation. Morphlings become an optional Lodge ecology career. Advanced puppetry remains a summon career.

#### Alternate ideas

**Alternate idea A:** Require Bloodline Founding but not a Fane. This reduces base-building pressure but fails to teach the bloodline's physical home.

**Alternate idea B:** Allow either a personal Fane or participation in another player's Fane. This is socially flexible but creates ambiguous ownership and advancement credit.

**Alternate idea C:** Replace permanent founding with one Covenant Vigil helper trial. This avoids base commitment but belongs more naturally to Degree 6 stewardship.

#### Reward possibilities

- The Sanctified rite blueprint is the primary reward.
- A small starter set of Hematic Stakes can reduce repetitive construction without trivializing the Fane.
- Unlock a Fane-status ledger page showing heart, boundary, bloodwell, members, and helper readiness.

#### Risks and dependencies

- Existing founders with a valid Fane must receive credit automatically.
- Fane relocation, collapse, and bloodline dissolution must not permanently corrupt the chapter flag after it is earned.
- Server rules must handle a former member, successor, or disbanded founder without allowing rank loss.
- The chapter must explain the distinction between founding the social bloodline and consecrating its territorial Fane.

#### Solo compatibility

Complete by design. A founder alone can establish the bloodline and Fane.

#### Playtest questions

- Is building the first Fane an inspiring home project or an abrupt material wall?
- Does Founding Fane provide enough immediate utility to justify becoming mandatory?
- Should the chapter give temporary boundary previews before the player commits the catalyst?

### D6 -> D7: The Living Covenant

#### Confirmed current state

Degree 6 provides the Chamber of Will, Covenant Throne, Covenant Vigil, helper-role ceremony support, Eternal Covenant, advanced covenant skills, deeper scar capacity, and the most complex public rank rite. No Degree-6 ledger assignment currently binds these together.

#### Problem

This is the clearest missing chapter. The player moves from founding a covenant to becoming Archon primarily by gathering Archon-rite materials. The supposed stewardship rank has no authored proof of stewardship.

#### Recommended direction

Create a D6 Main Chapter titled **The Living Covenant**:

1. Complete the Chamber of Will rite and enter the personal Chamber once.
2. Return safely, proving the Chamber loop is understood.
3. Craft/place and bind the Covenant Throne as the covenant's Progenitor.
4. Assign one eligible helper: another player or a recruited Harbinger NPC.
5. Complete Covenant Vigil with the assigned helper surviving in role.
6. Return to the Chamber or Throne for a short covenant reflection and record the chapter milestone.
7. Reveal the Archon rite blueprint.

Eternal Covenant remains an optional high-degree utility rite rather than an additional hidden requirement.

#### Alternate ideas

**Alternate idea A:** Require a fixed amount of shared blood moved through the Fane and bloodwell. This demonstrates infrastructure but becomes passive grind and is harder to explain.

**Alternate idea B:** Require defending the Fane against a dedicated Will incursion. This gives D6 a dramatic ordeal but adds another encounter system before Vesper.

**Alternate idea C:** Make the Chamber itself contain a short stewardship trial with echoes of recruited members. This is narratively rich but expands the Chamber beyond its current caster-only refuge role.

#### Reward possibilities

- Archon rite blueprint.
- A permanent Chamber memorial representing the helper who survived the Vigil.
- One high-tier ritual support resource as recognition, not enough to skip Archon preparation.

#### Risks and dependencies

- NPC helper assignment must be reliable across chunk unloads, logout, and server restart.
- Losing a recruited helper cannot permanently block retries.
- Covenant Vigil must identify the caster and assigned survivor unambiguously.
- The chapter cannot require another human player.
- Existing players who completed Chamber and Covenant Vigil should receive derived credit where possible.

#### Solo compatibility

Mandatory. A recruited Harbinger NPC must be accepted anywhere the chapter refers to a helper.

#### Playtest questions

- Is requiring both Chamber and Throne too much structure work in one chapter?
- Can recruited NPCs reliably reach and hold authored helper stations?
- Does Covenant Vigil feel like covenant leadership or like escorting fragile AI?
- Is the Archon rite still a meaningful escalation after a shared D6 ordeal?

### D7 -> Silent Archon: The Blood Beneath the Blood

#### Confirmed current state

An Archon can receive the Sanguine Monolith blueprint, build and shatter the Monolith, recover a Qliphoth Seed and fragments, complete Bloom of the Qliphoth, and consume nine owner-bound pomes. The ninth pome grants the Fungal Spine. Its first valid use begins a two-minute consciousness projection into the Fungal Gardens; forced return presents the Silence or Eighth Degree response.

Choosing Silence records `SILENT_PENDING`. Current successful Pruning removes the Bloom and immediately records `SILENT_ARCHON`. Silent Archon then gates its vestment recipes and Chamber theme.

Pome ripening currently checks every 40 ticks with a 1-in-80 chance. With the Bloom loaded and no unconsumed pending pome, the statistical mean is approximately 3,200 ticks, or 160 seconds, per pome. Nine sequential pomes therefore average roughly 24 minutes before travel and collection overhead. The next pome cannot ripen while one remains pending.

#### Recommended direction

Preserve the Monolith, Seed, Bloom, nine-pome canon, Fungal Spine, projection, and choice. Improve state communication rather than replacing the passive cadence with nine new trials.

The chapter should visibly report:

- Current husk index and total consumed.
- Whether the Bloom is waiting to ripen or a pome is ready.
- That only one pome can be pending.
- That the Bloom must remain loaded for ripening checks.
- That the ninth pome grows the player's return-thread into the Fungal Spine.
- After choosing Silence, that Pruning begins the final refusal rather than immediately granting the title.

#### Alternate pome ideas retained

**Alternate idea A:** Let large blood contributions accelerate the next ripening roll without guaranteeing it.

**Alternate idea B:** Let Fane activity, completed rites, or Will victories apply a bounded acceleration bonus.

**Alternate idea C:** Give each husk a different ambient omen or collection presentation while leaving timing unchanged.

These alternatives are not the recommended baseline. The approved direction is to preserve mostly passive sequential ripening and first improve clarity, feedback, and collection reliability.

#### Reward possibilities before the final choice

- Keep the temporary pome empowerment effects and Fungal Spine.
- Use unique whispers, visual tree changes, and Chamber omens as intermediate rewards rather than adding nine material prizes.

#### Risks and dependencies

- The UI must not promise an exact countdown for a random process.
- Chunk-loading language must be accurate; the design should not silently encourage permanent force-loading.
- Lost owner-bound pomes need a recovery rule that does not duplicate progression.
- Blood Tower or other stray Fungal Spine loot must not appear to bypass the nine-pome capability gate.

#### Solo compatibility

Complete through the revelation. The recommended Vesper ordeal is intentionally a personal Chamber encounter.

#### Playtest questions

- Does approximately 24 minutes of expected passive ripening feel contemplative or simply idle?
- Are unique whispers enough to make nine repetitions feel cumulative?
- Do players remain near the Bloom naturally because its buffs are useful?
- Does the Spine clearly communicate why an externally found copy cannot open the first projection?

## The Final Refusal: Severed Qliphoth and Vesper

### Narrative purpose

Silent Archon should not be awarded merely because the player selected a dialogue response and performed a small cleanup rite. Silence is the decision; Vesper is the proof that the player can hold that decision against the embodied pressure of surrender, memory, and self-erasure.

The final sequence becomes:

1. The Archon witnesses the fungal truth and chooses Silence.
2. The path records `SILENT_PENDING`.
3. Pruning cuts the Bloom's living trunk but does not erase it.
4. The Bloom becomes a **Severed Qliphoth**, opening a wound between the world and the owner's Chamber of Will.
5. The owner enters that wound and confronts Vesper, the Crowned Refusal.
6. Defeating phase one releases Vesper, the Evening Star.
7. Defeating phase two records `SILENT_ARCHON`, grants the Memory of Vesper, transforms the Chamber, and seals the world wound.
8. The Severed Qliphoth remains as a permanent trophy.

### Severed Qliphoth states

The later implementation specification should preserve three explicit world states:

| State | World presentation | Interaction | Progress meaning |
| --- | --- | --- | --- |
| Living Bloom | Current complete Qliphoth canopy/trunk and pome lifecycle | Existing Bloom behavior | Communion in progress or complete |
| Severed, portal open | Trunk cut across an authored plane; black/red lightning vents from the wound; swirling monolith-face surface fills the cut | Owner in `SILENT_PENDING` enters the Chamber; others receive refusal feedback | Vesper ordeal available or retryable |
| Severed, sealed trophy | Portal motion collapses into an inert dark monolith scar; lightning stops or falls to a very rare residual pulse | No ordinary portal travel | Silent Archon victory permanently memorialized |

The trophy remains owner-protected. Administrative/creative recovery must still exist so a server operator can remove broken or abandoned world state.

### Portal visual direction

- The cut plane should read as a physical severance, not a nether-portal rectangle placed inside the model.
- The portal surface should borrow the Monolith Fragment/charged monolith shader language: black depth, red internal current, slow face-like distortion, and no literal humanoid portrait.
- Black and blood-red lightning should originate along the bark/cut boundary and crawl outward through the severed trunk.
- The open state should be visible at range but avoid continuous excessive particle or lightning cost.
- The sealed trophy should retain the authored cut silhouette and a glassy black-red scar, making the player's decision readable after the encounter is over.

### Portal interaction rules

**Recommended behavior:**

- Only the Bloom owner with Degree 7 and `SILENT_PENDING` can enter.
- Interaction is explicit right-click on the rendered cut plane or its server-side interaction face.
- Non-owners receive a short message that the wound does not remember them.
- Apotheos-aligned players cannot use the refusal portal.
- If the Chamber level is temporarily unavailable, the portal remains open and reports failure without consuming state.
- Entering stores a safe return position using the same principles as existing Chamber/Fungal travel.
- The portal is a retry anchor, not a general reusable Chamber entrance.

### Vesper arena

The encounter occurs in the owner's Chamber of Will.

Before victory, the Chamber uses its Archon-revelation presentation. Entering through the Severed Qliphoth starts or resumes the refusal ordeal. The fight is caster-only; helpers and other players do not transfer into the owner-scoped Chamber.

The Chamber is preferred over the overworld because it:

- Prevents boss grief around a permanent player base.
- Already has owner-specific persistent state and progression-aware themes.
- Makes Vesper an interior confrontation with the player's will rather than a random outdoor boss.
- Allows victory to transform the same space directly into the Silent Archon Chamber theme.

### Failure and retry direction

Pruning and nine-pome Communion are paid once.

- Player death ejects or respawns the player outside the Chamber through safe existing return rules.
- Logout, dimension unload, or server restart leaves the Severed Qliphoth portal open.
- Re-entry resets the encounter to Crowned Refusal at full health unless a later encounter specification deliberately supports phase checkpoints.
- No additional pome, Seed, or Monolith is required.
- Vesper must not remain freely active in an unloaded Chamber without its owner.
- Multiple Vesper instances for the same owner are forbidden.

Death, logout, and server restart therefore leave a safe retry path without repeating Communion.

**Alternate failure cost:** A small recommitment offering at the severed portal could be considered if consequence-free retries feel too soft. This is not the recommended initial behavior; tune the boss first.

### Victory contract

Final Evening Star defeat should resolve progression atomically:

- Verify the Vesper instance belongs to the active Chamber ordeal and owner.
- Record `SILENT_ARCHON` and clear `SILENT_PENDING` encounter state.
- Preserve Degree 7; Silent Archon is not Degree 8.
- Unlock Silent Archon vestment eligibility and Chamber theme.
- Award the Memory of Vesper exactly once.
- Seal the Severed Qliphoth portal and sync its trophy state.
- Return or allow the owner to leave the Chamber safely.
- Present a final refusal message and advancement/ledger completion.

Only final Vesper victory grants `SILENT_ARCHON`; Pruning alone must never finalize the path.

If item delivery fails because the inventory is full, the progression result must still succeed and the Memory should be delivered through a recoverable owner-bound drop or later claim. The item drop cannot be the sole source of truth for victory.

### Reusable Vesper foundation

#### Already implemented and reusable

- Registered Crowned Refusal and Evening Star entities.
- Two-phase transition from Crowned Refusal death into Evening Star.
- Boss bars, attributes, targeting, attack-pattern hooks, death timing, and particles.
- Dedicated models, textures, renderers, and line overlay.
- Ambient, hurt, hit, death, and music audio assets.
- Final Memory of Vesper item/loot.
- Living Staff memory awakening and Vesper's Refusal skill interactions.

#### Missing encounter and progression integration

- Severed Qliphoth persistent states and rendering.
- Portal interaction and Chamber transfer. THE CHAMBER SHOULD NOT BE THE PLAYERS CURRENT ONE WITH STORED DATA BUT INSTEAD A LARGER FLAT TEMPORARY 50x50 SQUARE AREA WITH A FLOOR OF INVISIBLE UNBREAKABLE BLOCKS TO FIGHT VESPER IN
- Owner-bound ordeal identity and duplicate-spawn protection.
- Death/logout/restart cleanup and retry behavior.
- Chamber arena bounds, safe spawn, and encounter reset.
- Victory-to-`SILENT_ARCHON` transition.
- Atomic Memory reward and advancement/ledger completion.
- Final boss balance, telegraph, exploit, and multiplayer-isolation testing.

### Vesper encounter brainstorm

The present entities provide a functional combat foundation, but their later encounter specification should make the phases express different meanings rather than only higher statistics.

**Crowned Refusal ideas:**

- Heavy, controlled monolith pressure and attacks that punish hiding behind ordinary physical defense.
- Chamber pillars that repeat the player's earlier rank titles as accusations or restraints.
- Blood-anchor mechanics that reuse learned ritual responses rather than inventing an unrelated puzzle.

**Evening Star ideas:**

- Faster exposed form centered on memory echoes and false copies.
- Attacks that temporarily suppress ordinary weapon damage but remain answerable by blood manipulations and Living Staff forms, foreshadowing Silent Archon tradeoffs.
- A final absorption/refusal interaction with the planted or held Living Staff instead of ending on an ordinary last hit.

**Guardrail:** Do not require a particular manipulation school, scar, puppet shape, armor fork, or multiplayer composition. The fight can reward mastery of those systems without invalidating players who followed other optional careers.

## Cross-Cutting Cleanup

### Rank-proof milestone model

Recommended milestone chain:

| Current degree | Defining proof | Opens |
| ---: | --- | --- |
| 1 | First Bloodcraft reward claimed | Votary rite |
| 2 | First Separation reward claimed | Initiate rite |
| 3 | First deliberate Loom weave completed | Sanguine Brotherhood rite |
| 4 | First Effigy-prepared scar loadout activated | Illuminatus rite |
| 5 | Founder owns a valid bloodline and first Fane has been established | Sanctified rite |
| 6 | Living Covenant chapter completed with a surviving player-or-NPC helper | Archon rite |
| 7 / Silent Pending | Owner-bound Vesper Evening Star defeated | Silent Archon state |

The subsequent implementation specification should decide whether these use new advancements, capability fields, or a general assignment-progress attachment. The behavior is selected here; the storage mechanism is not.

### Artificer vocation

The Worn Vow, Three Answers, Crimson Vestment, Assumed Limb, and Weight of the Frame should read as one optional equipment career spanning degrees rather than several competing main assignments.

Recommended presentation:

- One Artificer Vocation header.
- Degree-gated steps appear as the player qualifies.
- Current target and completed fittings remain visible.
- Cosmetic fitting rewards remain clearly cosmetic.
- No full armor set or all-seven-weapon requirement gates initiatory rank.

### Fieldwork grouping

Red Taxonomy, Eightfold Centrifuge, and Living Bestiary should share an Alchemical Fieldwork umbrella while retaining distinct progress:

- Taxonomy: flora diagnosis.
- Eightfold: enzyme-process mastery.
- Bestiary: ecology specimens and morphling layers.

The grouping should clarify their relationship without merging their persistence or rewards.

### Guidance hierarchy

When several surfaces can explain the same content, use this priority:

1. NPC gives purpose and the immediate next action.
2. Ledger tracks the chapter and return condition.
3. Sanguine Conduit shows the complete recipe/rite network.
4. Mnemonic Blueprint shows the spatial build.
5. Liber provides durable lore and system explanation.
6. Advancement toast confirms completion or tells the player to return.

No individual surface should attempt to reproduce the full content of every other surface.

### Documentation drift to clean

The following claims should be audited and synchronized when the gameplay cleanup lands:

- Older wiki text describes a Blood Compass that finds Harbinger structures; the current Hearty Compass remembers the player's last death.
- Older readiness notes still describe the Hermit/first-hour onboarding as absent or abrupt even though the temple-bound oath, blueprint, and simplified initiation now exist.
- Some references use broad or historical manipulation and morphling counts that no longer match the release-facing canonical sets.
- Old material may imply the Fungal Spine works at Votary or before nine-pome Communion; current travel requires Degree 7 and the capability's granted-Spine flag.
- Drudge and Saint material must remain clearly marked post-release/WIP rather than appearing to be required Harbinger progression.
- Silent Archon documentation must change from “successful Pruning grants the state” to “Pruning opens the Vesper ordeal; final victory grants the state” if this direction is implemented.

## Prioritized Cleanup Waves

### P0: Repair the player-facing spine

- Grant/recover the ledger at first Degree-1 Vicar contact.
- Move Hermit Road to a true side assignment.
- Add a reliable temple-to-Outpost breadcrumb.
- Reclassify ledger sections into Main, Side, Vocation, and Catalogue.
- Group D2 fieldwork and finalize the Red Taxonomy reward.
- Design and add the missing D6 Living Covenant chapter.

**Why first:** These changes improve the entire journey without requiring a new boss, renderer, or world-state system.

### P1: Connect chapters to rank rites

- Add or derive one persisted completion milestone per degree.
- Gate each subsequent rank rite on its defining chapter only.
- Provide explicit locked feedback naming the unfinished chapter.
- Migrate existing players from current achievements and persistent world state.
- Rebalance D2/D5 presentation after the new gates are visible in survival play.

**Why second:** Rank gating should follow the repaired guidance, not precede it.

### P2: Build the Final Refusal

- Transform Pruning into Severed Qliphoth creation.
- Add open/sealed trunk states, lightning, cut-plane portal rendering, and owner interaction.
- Add Chamber transfer and persistent owner-bound ordeal state.
- Integrate both Vesper phases, safe retries, victory, Memory reward, and Silent Archon promotion.
- Leave the sealed Severed Qliphoth as a permanent protected trophy.

**Why third:** This is a substantial self-contained endgame feature that can be specified and tested after the earlier progression contract is stable.

### P3: Tune and synchronize

- Run timed survival journeys across solo and multiplayer servers.
- Tune material cost, travel distance, pome pacing, helper reliability, and Vesper difficulty.
- Polish dialogue, ledger copy, advancement toasts, ritual hints, and reward quantities.
- Synchronize wiki, reference, roadmap, and release-scope documentation.

## Acceptance Questions for Later Survival Testing

The cleanup is successful only if playtesting can answer yes to the following:

### Direction

- After every rank gain, can the player identify one main next chapter without checking a wiki?
- Can the player distinguish main progression from optional vocation and catalogue work?
- Does a locked rank rite explain the exact defining chapter still required?

### Pacing

- Does any degree introduce more than one mandatory system at once?
- Do D2 and D5 still feel busy after presentation is redistributed?
- Does D6 feel like covenant stewardship rather than preparation shopping?

### Autonomy

- Can a solo player complete every mandatory step using recruited NPC helpers where needed?
- Can players ignore armor, puppetry, morphlings, or collection careers without becoming stuck?
- Are existing worlds credited without rebuilding a Fane, reweaving a memory, or relearning a scar?

### Endgame

- Does the nine-pome loop feel cumulative despite remaining primarily passive?
- Does the Severed Qliphoth clearly communicate that the final refusal is still unfinished?
- Can death, logout, restart, or dimension unload recover cleanly through the persistent portal?
- Does Vesper test broad Harbinger mastery without demanding one optional build?
- Does becoming Silent Archon feel earned at the moment Evening Star dies?
- Does the sealed trunk remain an evocative trophy without becoming permanent visual/performance noise?

## Explicitly Deferred Implementation Decisions

These decisions belong in focused implementation specifications after the direction is selected and playtest targets are agreed:

- Exact persistence schema for chapter milestones and Severed Qliphoth state.
- Exact class/block-state split for living, open-severed, and sealed-severed rendering.
- Exact Outpost locator item or map implementation.
- Exact Red Taxonomy reward counts.
- Exact D6 chapter copy, reward quantities, and NPC dialogue branches.
- Vesper arena geometry, attack kit, health/damage tuning, and final Living Staff interaction.
- Whether the Vesper fight resets fully or later earns a phase checkpoint after testing.
- Administrative cleanup command or interaction for abandoned permanent trophies.

Deferral here means “requires its own implementation design,” not “optional to solve.” The chapter spine, mandatory D5 founding/Fane proof, D6 Living Covenant chapter, Severed Qliphoth portal, Chamber Vesper encounter, and victory-gated Silent Archon state are the selected foundation of this catalogue.

## Final Recommended Journey

1. Meet the Hermit, freely accept the temple oath, claim the heart, and complete Sanguine Initiation.
2. Follow a real Outpost breadcrumb, receive the ledger from the Vicar, and complete First Bloodcraft.
3. Learn sampling through First Separation while field research and equipment appear as optional branches.
4. Complete The Woven Vessel; allow Loom mastery to introduce puppetry and deeper manipulation craft.
5. Follow the Vein-Mason journey and deliberately activate the first scar loadout.
6. Found a bloodline and establish its first Fane before becoming Sanctified.
7. Build a living covenant through the Chamber, Throne, helper role, and Covenant Vigil before attempting Archon.
8. Shatter the Monolith, grow the Bloom, consume nine pomes, receive the Spine, and witness the truth.
9. Choose Silence and prune the living tree into a Severed Qliphoth portal.
10. Enter the Chamber, defeat both faces of Vesper, and only then become a Silent Archon.
11. Return to a transformed Chamber and a sealed severed trunk that permanently records the refusal in the world.

That journey preserves the current mod's strongest authored systems while turning them into a readable ascent rather than a collection of adjacent unlocks.
