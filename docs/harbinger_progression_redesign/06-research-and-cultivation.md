# Phase 6: field research, cultivation, and the Circus

[Index](HARBINGER_PROGRESSION_REDESIGN.md) · Build on Phases 1-5 without changing Main certification.

## Outcome

Exploration connects the NPCs' interests to the world. Early discovery remains useful. No rare biome, faction encounter, or complete catalogue becomes an unannounced promotion requirement.

## Read before editing

- [Overworld Fungal Gardens region](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrablender/FungalGardensOverworldRegion.java) and its registration in BiomeInit.
- [Fungal dimension definition](../../src/main/resources/data/hemomancy/dimension/fungal_gardens.json); distinguish it from the survey destination.
- ClinicalBloodKnowledge, RedTaxonomyRewardRules, SpecimenBestiaryDefinitions/Progress/Events and SpecimenJarData.
- Mycelial Lantern and Morphling Incubator recipes/block entities, enzyme_fruiting and incubator resources.
- CircusIntroductionRules, current Circus progress/reward/dialogue and puppeteer trial implementation.
- AntecedentDialogue/Research/Events, blood profiles, Vagrant Mind/Choir Keeper data and current NPC inquiry resources.

## Assignment specifications

| Degree | Assignment and owner | Completion and treatment |
|---|---|---|
| D1 onward | Catalogue introductions, Alchemist | Track supported species/flora over time. Never require catalogue completion for a degree. |
| D2 | Overworld fungal survey, Vicar referral / Alchemist report | Default: observe the Overworld Fungal Gardens and present two different registered local flora/block specimens. Credit previously recorded discovery; inspect specimens without destroying them. |
| D3 | Voyager introduction, Vicar | Meet a valid Voyager in the established reef/vessel context and report one observation. Do not require ambergris or a whale kill. |
| D4 | Cultivation and Morphling handling, Alchemist | Produce one existing enzyme in a Lantern, and demonstrate a supported jar/care interaction. Keep as separate Vocation steps. |
| D4 | Circus referral, Mnemonist with Vicar context | Offer the established waybill/introduction and specialist branch. The Main chain remains independent. |
| D5 | Deep Dark commission, Alchemist | Continue the existing Antecedent/Vigil Beneath inquiry, recognize already completed stages, and request its actual supported sample/analysis. |
| D5 | Phlegethontic commission, Alchemist | Present a valid Excoriated OR Bombardier sample plus five Escharian Scyphus; count five items, not plant visual stages. Preserve specimen components. |
| D6 | Vagrant Mind inquiry, Mnemonist and Alchemist | Shared discovery evidence supports two reports: organized memory, and local biology/Choir Keeper/chorus. Do not require two copies of a unique item or a forced kill of every organism. |

The two-specimen Overworld survey is an implementation default, not an existing quest assertion. Select its exact accepted IDs from current natural biome placement and loot when implementing it; do not make a broad hand-written flora list that includes unavailable plants. Use a narrow data-driven acceptance set with at least two validated natural sources.

## Instructions

1. Make every assignment's Main/Side/Vocation/Catalogue classification explicit. New surveys/commissions are Side; sustained production/equipment use is Vocation; open-ended records are Catalogue.
2. Use actual collection, inspection, location, or dialogue events for proof. Do not award location discovery from simply holding a named item. Accept durable proof acquired before a referral.
3. Do not broaden jars into universal capture devices. Use existing specimen definitions; ordinary flora can be carried as blocks/items. Explain which examples are supported.
4. Keep the Lantern's D3 gate and teach its renewable loop at D4. Preserve the D3 centrifuge route to first-weave materials.
5. Keep incubation D5. D4 covers wild organisms, care, capture, and cultivation prerequisites; it must not ask the player to build a locked Incubator.
6. Distinguish temporary puppets from living Morphlings. Preserve ordinary D3 puppet trials. Circus D4 instruction deepens puppeteering; it does not retroactively own every puppet or become a rank gate.
7. Keep the Circus morally ambiguous and allow the existing alternative approaches. Do not force hostility, liberation, or participation merely to reach D5.
8. D5 is an official commission, not the first legal access to the Deep Dark or Nether. Antecedent dialogue already begins at D3, with later recognition stages. Preserve those gates and existing completion.
9. Use Escharian Scyphus, not an unregistered Eschar Lichen item. Keep the Nether's organic interpretation as a character's interpretation, not proof of the world's literal anatomy.
10. Keep the Deep Dark's Ahaematic/Incertae evidence distinct from ordinary blood and from a ninth tendency. Do not equate the Warden with an early Apotheos or solve the Entity's origin.
11. Frame D6 memory rewards through existing skill/loadout systems. Show the actual unlock/costs if eligible; do not silently add capacity, reset spent skills, or invent a new currency. Additional Vagrant-specific capacity upgrades remain a separate balancing feature.
12. Keep expedition rewards useful but nonessential: existing materials, recipe knowledge, or supported research progress. They must not be the only source of a Main chapter ingredient.
13. For inspection commissions, retain carried samples by default. If an established controlled experiment consumes one, state that before the action and use its existing recovery/replacement rules.
14. Give each commissioned destination a usable lead through existing maps, directions, or discovery systems. Rare generation must not turn a mandatory chapter into blind travel.

## Acceptance

- [ ] No survey offers travel to the fungal projection dimension at D2.
- [ ] Both fungal survey specimens have validated natural acquisition and valid loot.
- [ ] Prior reef/Deep Dark/Circus discoveries are acknowledged rather than reset.
- [ ] D4 cultivation/handling works without D5 incubation.
- [x] Either Nether sample satisfies the commission; other profiles and empty/unreadable samples do not. The focused mod-loaded test also checks five carried Scyphus items, non-consumption, and saved completion.
- [ ] Vagrant discovery can advance both investigations without consuming the same unique proof twice.
- [ ] All quests survive relog/restart and can be resumed with another valid teacher where appropriate.
- [ ] A player who skips all these branches can still complete the Main chain and choose either ending.

Natural generation, travel time, and supported capture presentation require fresh-world/live checks. Commands that place a structure prove only the placed encounter, not its natural discoverability.

The D6 server path now records a Vagrant Mind interior visit and a separate End biology observation (Choir Keeper, Myelin Borer, or nearby chorus growth), then lets the Mnemonist and Alchemist file independent non-consuming reports. A focused mod-loaded test seeds a prior Mind visit and observes End chorus before dispatching both reports; it does not yet prove that a naturally generated Mind records its interior visit, that the bearing finds one in a fresh world, or that the new ledger is readable in a live client. Keep the acceptance item open until those checks run.
