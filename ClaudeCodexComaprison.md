Codex Astra 6 medium 
**Yes—I’d split the teaching between the NPCs.** Your draft gives the outpost a stronger purpose: the player learns bloodcraft through several practitioners, each with their own interests. The main adjustment I’d make is to avoid turning every degree into four mandatory checklists.

Give each degree **one central achievement**, with the other NPCs offering useful lessons and optional projects around it.

I checked the [GitHub repository](https://github.com/VincentHuto/Hemomancy), your local reference documents, and the relevant current code and recipes. The checkout contains newer uncommitted progression changes, so the comparison below follows those where older documentation disagrees. Nothing was edited.

**The important differences from the current setup**

| Your proposal | Current setup and suggested adjustment |
|---|---|
| D1 animal sampling contributes to D2 | Currently, **First Bloodcraft grants D2 through the Vicar**. The Alchemist’s separation/distillation work, Concentrated Blood injection, and sleep now lead to **D3**. Your sampling task can join D1, but should support the bloodcraft lesson rather than replace it. |
| First crude memory at D2 | This works as the first *directed* memory lesson. Crude-memory teaching already appears earlier, and Absorption/Projection are already manipulations. Call it the first **chosen combat or utility memory**. |
| Loom and Reliquary at D4 | Both currently unlock at **D3**, and the first weave certifies promotion to D4. Moving all weaving instruction later would require replacing that promotion proof. |
| Morphling incubation at D4 | The Incubator currently requires **D5**. This is a real unlock change, not just a dialogue move. |
| Anchorite introduction at D5 | Scar instruction currently belongs to **D4**, and the Vein-Mason chapter certifies D5. Keep the first visit at D4; deepen it at D5. |
| Chamber rite at D5 | Current code allows early bed dreams at D1–D2, chair visits from D3, and independent rite access at D6. I would preserve D6 as the moment the player gains reliable, independent access. |
| Forge introduction D4, upgrade D6 | Current Forge tiers are **D3 / D5 / D7**. Teaching them later is possible, but changing the actual gates should be deliberate. |
| Bloodwood from any sapling | Current projection growth accepts a **dead bush only**. Supporting saplings would be an additional mechanic. |

The main dependencies are explicit in [the chapter milestones](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/mission/shared/HarbingerChapterMilestone.java>), [Concentrated Blood progression](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/ConcentratedBlood.java>), and [Chamber access](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberVisitService.java>).

**Give the four teachers consistent responsibilities**

- **Vicar:** initiation, doctrine, Cardinal rites, bloodlines, and responsibility toward other practitioners.
- **Alchemist:** sampling, identification, processing, tinctures, cultivation, and living organisms.
- **Artificer:** equipment, living implements, armor specialization, and transferring power into objects.
- **Mnemonist:** learning and arranging memories, weaving, recording, and understanding the Chamber.

They should refer the player to one another when their work intersects. That connection will do more for the outpost than simply distributing quest counts evenly.

**D1 — Neophyte: learn to handle blood**

Your proposed three-NPC introduction fits here.

- The **Vicar** teaches Absorption and Projection, then guides the Liber Sanguinum construction.
- The **Artificer** teaches Hematic Iron through a small practical project.
- The **Alchemist** supplies five empty vials and asks for five animal samples. I’d require **at least three different common species**, rather than five rare species or five samples from one cow.

Introduce the ongoing specimen collection here, but keep it separate from advancement. Blood samples, preserved creatures, and flora should have appropriate containers and handling instructions; don’t imply every discovery can go into the same jar.

**Promotion:** retain First Bloodcraft as the central D2 proof, with specialist NPCs supplying its lessons. The sample task prepares the next degree.

One practical issue: clinical lesson tracking currently starts at D2. A formal D1 sampling assignment would need that tracking adjusted.

**D2 — Votary: understand what you collected**

This should be the degree where blood stops being one interchangeable resource.

- **Alchemist:** identify a sample, centrifuge it, extract an enzyme, then complete one simple Alembic distillation. Teach bloodwood cultivation alongside the first useful reason to need it.
- **Artificer:** introduce Hematic Iron armor and the Armature. Require one meaningful piece for the lesson; make completing the set a separate reward.
- **Mnemonist:** offer a small choice of crude memories, then have the player use their chosen memory. Explain equipping and capacity without unloading the whole memory system.
- **Vicar:** teach the Scarlet Vanity and first Blood Gourd as practical preparation for longer expeditions.

The optional Overworld Fungal Gardens collection trip fits well here. Describe it explicitly as an **Overworld biome survey**, so it cannot be confused with the much later consciousness projection into the fungal realm.

**Promotion:** preserve the current sequence of separation, personal collection of an Alembic result, Concentrated Blood injection, and completed sleep.

**D3 — Initiate: shape blood deliberately**

This is where I would keep the first Loom lesson.

- **Alchemist:** introduce Thelemic tinctures and the Living Syringe as improved field equipment. Use the player’s old vial collection task to demonstrate why the syringe is valuable.
- **Artificer:** teach one biological armor specialization—Barbed, Chitinite, or Prismatic—and the Living Staff.
- **Mnemonist:** teach the Reliquary and one guided Somatic Loom weave. Introduce the Enzymatic Scriptorium through a small demonstration rather than requiring another full workstation project immediately.
- **Vicar:** teach the first player-performed Cardinal rite after the player has a staff.

Your temporary Chamber visit belongs here. Make it **a lesson the player knowingly begins, followed by an involuntary return**, rather than an unexpected teleport halfway through ordinary dialogue.

The Erythrocoral Reef/Voyager referral is also a good D3 expedition. Let the player return with an observation or useful specimen; meeting the Voyagers should feel like encountering another branch of the same tradition.

**Promotion:** retain **The Woven Vessel**. The first deliberate memory is a clear achievement that brings the Alchemist’s materials and Mnemonist’s teaching together.

**D4 — Adept: choose a practice**

This degree should broaden player choice.

- **Mnemonist:** move from a guided weave to planning a useful selection of memories. Teach the Somnolent Seat and more deliberate Chamber exploration.
- **Alchemist:** introduce larger tinctures and renewable enzyme cultivation. Teach wild Morphling observation, capture, and care.
- **Artificer:** give the full Resonant Forge lesson, connecting the Mnemonist’s recorded patterns to equipment.
- **Vicar:** send the player to the Cicatrix Anchorite for their first scar work.

I would **keep full incubation at D5**. D4 can teach what Morphlings are and how to handle them; D5 gives the player the means to cultivate them. If you strongly prefer D4 incubation, I’d limit that first lesson to a modest organism and reserve more demanding work for later.

Keep the Circus introduction here. Your sentence says both “if they wish” and “will not be optional”; I’ve interpreted the intended meaning as **optional but powerful**.

I would have the Vicar acknowledge the Circus, with the Mnemonist providing the detailed referral. Established lore treats them as a specialist Harbinger sect, not inherently hostile fanatics. Let their conduct earn the player’s suspicion.

**Promotion:** retain the Vein-Mason chapter. Moving its first scar lesson to D5 would otherwise block reaching D5.

**D5 — Illuminatus: become responsible for a place and its people**

This is the strongest section of your draft.

One terminology correction: the formal degree is **Illuminatus**. The player can now perform a Vicar’s founding and initiation responsibilities, but “Vicar” should describe that role rather than replace the degree title.

- **Vicar:** teach Bloodline Founding, then the separate Founding Fane ceremony, recruitment, and receiving another initiate.
- **Alchemist:** teach incubation and commission the Deep Dark and Phlegethontic investigations.
- **Artificer:** award the Armature upgrade materials and teach Blood Lust armor, masks, and how the earlier biological specializations feed into them.
- **Mnemonist:** introduce the **Dendritic Distributor**, your existing loadout station, and upgraded Scriptorium work.
- **Anchorite:** teach the next scar application or loadout refinement, building on D4.

**Armature Consecration Kit** would be a straightforward replacement for *Vicar’s Consecration Kit*. The Artificer supplies the equipment; the Vicar can still explain what consecration means.

For the Alchemist’s expeditions, give each a research question:

- **Deep Dark:** what does this sample remember or respond to?
- **Phlegethon:** how does exposed tissue survive its environment? Accept blood from either proposed creature, plus the five Eschar Lichens.

I would keep those as substantial research branches rather than making both expeditions mandatory before the player can establish their first Fane.

For the Chamber, D5 should improve supported access—especially through a recruited Mnemonist. A 75% nighttime chance is fine as atmosphere, but avoid making mandatory progress depend on repeated failed nights.

**Promotion:** retain **A Covenant Written in Place**: found a bloodline and establish its usable Fane.

**D6 — Sanctified: investigate beyond the teachers’ experience**

Your paired Vagrant Mind expedition is the right centerpiece.

The Mnemonist and Alchemist should want different evidence from the same place:

- **Mnemonist:** investigate whether memory can remain organized without a familiar body. Bring back evidence relevant to advanced memories or loadout capacity.
- **Alchemist:** study the Choir Keeper, surrounding organisms, and the relationship between that ecology and chorus growth.

Those proposed rewards would need to be authored; I would not describe Vagrant Mind materials as already upgrading loadout slots.

Then give the other teachers supporting work:

- **Artificer:** teach precision Forge work and prepare equipment for the expedition. There is already a Precision tier, so D6 does not require inventing another machine tier.
- **Vicar:** teach covenant coordination and a ceremony where another participant’s survival matters.
- **Mnemonist:** teach the independent Chamber rite here.

That preserves a strong progression: **glimpse at D3, practice at D4, support at D5, independent access at D6**. Your existing [Chamber integration brief](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/docs/CHAMBER_OF_WILL_MNEMONIST_LORE_IMPLEMENTATION_BRIEF.md>) already argues for this distinction.

**Promotion:** retain **The Living Covenant**—safe Chamber return, Covenant Throne, and a successful Covenant Vigil with the assigned helper surviving.

**D7 — Archon: discover where the teaching fails**

I wouldn’t fill D7 with another normal round of machine tutorials. Most practitioners never reach beyond D5; that limit should become visible.

- **Mnemonist:** helps interpret the Chamber and the Monolith, but cannot promise that the player’s understanding is correct.
- **Alchemist:** can describe changes in the player’s biology without claiming to understand their ultimate purpose.
- **Artificer:** teaches Masterwork Forge use and the final pre-ending armor framework.
- **Vicar:** provides the final doctrinal disclosure and steps back.

Then let the existing sequence take over:

**Monolith → Qliphoth Seed → Bloom → nine Pomes → Fungal Spine → temporary projection → return and choice.**

Keep Silent Archon equipment or benefits behind the actual refusal outcome where appropriate; don’t award an ending’s identity merely for reaching D7.

**D8 — Apotheos: the player has exceeded instruction**

Your line works in spirit, but I’d revise it to:

> No living teacher can tell the player what they are becoming. Their former teachers can still recognize them, help them, or fear for them.

That keeps the outpost emotionally relevant. Losing access to ordinary services would make transcendence feel like punishment.

Also preserve the distinction: **Silent Archon remains D7. Apotheos is the hidden D8 branch.** The current [lore decisions](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/docs/LORE_CONSISTENCY_REVIEW.md>) make that separation central.

**Wax cylinders: I agree with you**

The current [cylinder recipe](</C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy/src/main/resources/data/hemomancy/recipe/ambergris_cylinder.json>) uses Mnemonic Ambergris, Active Befouling Ash, honeycomb, and paper. The base Resonant Forge also consumes that cylinder, so the whale material affects more than optional audio recording.

I’d use:

- **Ordinary Wax Cylinder:** honeycomb as the available vanilla wax ingredient, plus the supporting materials needed for ordinary recordings.
- **Ambergris Cylinder:** a specialty medium for advanced mnemonic work, preserved impressions, or master patterns.

That gives ambergris a distinctive purpose without making whale access a prerequisite for basic recording. Its lore should continue to treat it as a shed or offered secretion rather than a reason to hunt whales.

**My strongest recommendation is to preserve the existing promotion achievements while redistributing who teaches their steps.** Keep the first weave at D3, first scar cycle at D4, founding at D5, and independent Chamber mastery at D6. Your NPC split can make that progression much richer without requiring every player to finish every specialist branch.

This was a source-and-document review; no gameplay tests were run. Several older lore/wiki passages need reconciliation with current code, particularly early advancement, Chamber access, and deferred Saints content.



CLAUDE OPUS 5.5 medium
Still at it: I'm reading the current progression docs and chapter chain so I can map your draft against them. Next I'll check where each system you mention is currently gated.

Nearly done gathering: checking the remaining gates (Scriptorium, Resonant Forge, Circus, gourds, Living Staff, cylinders), then I'll write up the comparison.

I compared your draft with the current code and docs: `HarbingerChapterMilestone`, recipe `required_degree` gates, the NPC dialogue tables in `HEMOMANCY_REFERENCE.md` §5, and `wiki/Harbinger-Path.md`. Splitting lessons across the four outpost NPCs is a good idea, and about 60% of your draft already matches what's implemented. The real problems are a handful of gate conflicts and one missing piece: the draft never says what actually promotes the player at each degree.

## The main missing piece: what earns each rank

Right now every rank is certified by exactly one **Main chapter**. The other assignments are Side, Vocation or Catalogue work in the ledger:

| Rank-up | Main chapter (current) | Owner |
|---|---|---|
| D1→2 | First Bloodcraft | Vicar |
| D2→3 | First Separation + First Distillation → Concentrated Blood + sleep | Alchemist |
| D3→4 | The Woven Vessel | Mnemonist |
| D4→5 | The Vein-Mason | Anchorite |
| D5→6 | A Covenant Written in Place (bloodline + fane) | Vicar |
| D6→7 | The Living Covenant (Chamber return, Throne, Vigil) | Vicar/Mnemonist |

Your draft lists lessons but never marks which one is the Main. I'd tag every lesson as Main, Side, Vocation or Catalogue. Otherwise the draft can't be mapped onto the ledger, and D4 ends up with no rank-up proof at all (see below).

## Where the draft conflicts with current gates

1. **Living Staff at D3.** The `living_staff` recipe is `required_degree: 1`. Rooted Vein (a D2 rite) and Pallid Vessel (D3) both plant a staff, and the wiki says the staff enters the rite loop at D2–3. **Suggestion:** have the Artificer teach the staff at **D2** alongside the Worn Vow. That also fits his "living gear" role better than a D3 lesson.
2. **"Fungal Gardens biome in the overworld."** Fungal Gardens is the **D7, Spine-gated dimension**. The overworld infection biomes are **Sporecrown Thicket**, **Hyphal Spires** and **Drifting Mycelium**. Use Sporecrown Thicket.
   - There's also a lore problem with the Vicar pointing a D2 player at fungal infection. The Crimson Lodge's secret (D5) is the link between blood and mycelium, which is meant to be "arrived at independently."
   - Have the Vicar call it "blighted ground" or "the old sickness" without explaining it, and let the Alchemist be the one who wants the samples.
3. **First gourd and Scarlet Vanity at D2.** Currently the Scarlet Vanity is D3 and the gourd rites are Pallid Vessel D3 → Crimson D4 → Ashen D5 → Horn of Culmination D6. Your draft has gourds at D2, D3 and D5. Either:
   - lower `scarlet_vanity` and `pallid_vessel_rite` to D2, or
   - keep the first gourd at D3.

   I'd keep D3, because the gourd rite needs the staff and rite vocabulary first.
4. **Chamber of Will timeline.** The Somnolent Seat already works from **D3**; its lang file says "will not answer before Initiatory Degree 3". The Chamber *rite* is **D6** and is part of the Living Covenant chapter.
   - Your D3 forced glimpse fits. Your D5 "enter any time" rite conflicts with the D6 rite and chapter.
   - I'd also push back on making the seat a percentage roll at night. A failed roll wastes a night for nothing.
   - **Suggestion:** the seat always works, but visits grow longer and more stable with degree. Involuntary dreaming in any bed could be the chance-based part.
5. **The Vein-Mason moving to D5 leaves D4 without a Main chapter.** The Anchorite is currently the D4→5 proof, and the Vicar hands out the Masons Respite map at D4. Scars also fit D4's Sanguine Brotherhood theme ("martial and surgical practice"). Either keep the Anchorite at D4, or invent a new D4 Main such as Circus initiation.
6. **Loom at D4.** The Woven Vessel, which is the loom, is the D3→4 Main. Keep the first weave at D3 and make D4 the deeper loom work plus the Reliquary. Also, "create their own manipulations" isn't what the loom does. It weaves authored recipes, so reword that or it will promise a feature that doesn't exist.
7. **"Rank of vicar."** Vicar is an NPC role, not a degree; D5 is Illuminatus of the Crimson Lodge. Frame it as "the Lodge judges you fit to keep a fane," which puts you on equal footing with a Vicar without holding the title.
8. **Names that don't exist yet:**
   - **Eschar lichen:** the Basin has **Escharian Scyphus / Overgrowth**. Use those or add a new block.
   - **Deep Dark Vigil:** the existing content is the optional *Vigil Beneath* inquiry, which currently opens around D2–3.
   - **Thelemic tinctures:** this is currently the Alchemist's **Body Answers** (advanced brewing → the Sanguine Fists Thelemic Memory).
   - **"Nexus/nerve block":** this is the **Dendritic Distributor**, which is already D5. That part of your draft matches.

## Parts that already match (keep them)

- Alchemist's centrifuge and alembic at D2 = First Separation + First Distillation.
- Artificer's Hematic Iron set at D2 = **The Worn Vow**; specialized armor at D3 = **The Three Answers**; Blood Lust and masks at D5 = **Crimson Vestment**; final armor at D7 = **Weight of the Frame**.
- Enzymatic Scriptorium at D3, its upgrade at D5, and presumably a D7 Monolithic upgrade. This matches the design packet's D3/D5/D7 tiers, so fill D7 in.
- The Circus at D4 matches `CircusIntroductionRules.MINIMUM_DEGREE = 4`.
- Bloodwood from projecting into a Dead Bush already exists. "Any sapling" would be new.
- Voyagers at D3 work: the Erythrocoral Reef and active vessels exist.
- The Vagrant Mind and Choir Keeper exist.

## Filled-out ladder (my recommendation)

**D1, Neophyte.** Main: First Bloodcraft.
- **Vicar:** Liber Sanguinum, 500 ml absorbed, Sanguine Formation, Venous Stone, plus the crude rite floor.
- **Artificer:** Hematic Iron. The current Liber *or* Hematic Iron choice becomes both, split between the two NPCs.
- **Alchemist:** "First Draws," 5 animal samples in issued vials. **Open question:** how does a D1 player fill a vial before having a syringe? Nice continuity: D2's First Separation could spin *these* samples.
- Bloodwood from a Dead Bush.

**D2, Votary.** Main: First Separation + Distillation.
- **Alchemist:** the existing chain, plus the Red Taxonomy and Living Bestiary catalogues. Issue 2 Specimen Jars up front; right now jars are the *reward* for finishing Red Taxonomy, which is backwards if the player is supposed to collect specimens in them.
- **Artificer:** The Worn Vow plus the **Living Staff**.
- **Mnemonist:** the starter crude memory, moved from D1.
- **Vicar:** Rooted Vein, plus a Side assignment to the "blighted ground" in Sporecrown Thicket.

**D3, Initiate.** Main: The Woven Vessel.
- **Alchemist:** Thelemic preparations (Body Answers, renamed or promoted) and syringe-only samples. 131 blood profiles already have `requires_living_syringe`, which makes a natural gate if the syringe moves to D3.
- **Artificer:** The Three Answers.
- **Mnemonist:** Woven Vessel, the Scriptorium, and the first forced Chamber glimpse.
- **Vicar:** Pallid Vessel (first gourd) and the Voyager side trip.

**D4, Adept.** Main: The Vein-Mason (kept).
- **Alchemist:** Mycelial Lantern, the first real morphling incubation and jarring, and larger preparations.
- **Artificer:** Resonant Forge (the recipe is already D3), tied to the Mnemonist's work.
- **Mnemonist:** Reliquary, deeper loom work, and the Somnolent Seat.
- **Vicar:** the Masons Respite map, the Circus waybill and Crimson Vessel.

**D5, Illuminatus.** Main: A Covenant Written in Place.
- **Vicar:** bloodline, fane, recruitment and Hematic Succession, plus Ashen Vessel.
- **Alchemist:** official expeditions to the Vigil Beneath and the Phlegethontic Basin (Excoriated or Bombardier sample, plus 5 Escharian Scyphus).
- **Artificer:** Crimson Vestment, The Assumed Limb, and the Consecration Kit. Rename it, e.g. *Redwright's Consecration Kit*, and update its lang file, item inquiry, and the §22 docs.
- **Mnemonist:** Dendritic Distributor and the Consecrated Scriptorium rite.

**D6, Sanctified.** Main: The Living Covenant.
- **Mnemonist:** the Chamber of Will rite, plus the Vagrant Mind as a source of memory. It could feed `skill_synaptic_memory` and manipulation slots, which are currently `3 + degree/2 + skill`.
- **Alchemist:** Vagrant Mind biology, the Choir Keeper and chorus.
- **Artificer:** Resonant Forge upgrade. This is new and needs designing; alternatively, have him start the Monolithic Cornerstone groundwork.
- **Vicar:** Covenant Throne, Vigil, Eternal Covenant and Horn of Culmination. The draft leaves the Vicar out of D6–7, but he already has Sanctified and Archon lore.

**D7, Archon.**
- **Mnemonist:** Monolithic Scriptorium and the pome memos.
- **Alchemist:** Qliphoth Seed, Bloom and pome biology.
- **Artificer:** Weight of the Frame.
- **Vicar:** "the eighth degree is silence."

**D8.** Agreed that nobody teaches anymore. The existing lines already play that note: the Alchemist's "the machines were always pointing at something." One distinction to keep: a **Silent Archon stays D7**, so NPCs can still talk to them in a separate "refusal" register. Only Apotheos gets the full silence.

## Misc: the ambergris cylinder

The current recipe is `ambergris + active befouling ash + honeycomb + paper → 2 Ambergris Cylinders`. I agree it's too hard to get for an everyday recording medium. Suggestion:
- Add a **Wax Cylinder** made from honeycomb, ash and paper as the ordinary Clairaudiograph medium.
- Keep the Ambergris Cylinder for special uses: longer or voiced recordings, the three ancient Deep Dark cylinders, and the Mnemonist Puppet Ordeal flavor.

That change also touches the D3 Mnemonist Clairaudiograph instruction, `wiki/Clairaudiograph.md` and `wiki/Clinical-Blood-Tools.md`.

## Doc drift I found

The §23 block table says the Mycelial Lantern is "Degree 5," but `mycelial_lantern.json` has `required_degree: 3`.

## Decisions I need from you

1. How does a D1 player fill a vial?
2. Does the Anchorite stay at D4?
3. Does the Chamber rite stay at D6?
4. Is the first gourd D2 or D3?

Once those are settled, I can write this up as `docs/HARBINGER_PROGRESSION_REDESIGN.md` with a per-change list of the recipe gates, dialogue trees and docs each move would touch.