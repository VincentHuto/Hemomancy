# Track 4: Strain lore, Bestiary, books and reference docs

**Status:** DONE 2026-10-30 (unattended run). Strain descriptions, six stage lines, passive lines and Court lines; Alchemist Bestiary field notes with Court paragraphs; eight Alchemist inquiries; three Liber Morphling pages; HR §16, `docs/MORPHLING_STRAINS.md`, LORE_REFERENCE §6.3c, the wiki and the spec's Morphling directions updated; `MorphlingStrainTextResourceTest` and the full JUnit suite (4,758 tests) pass. Left: the Court lines are gated by a `CourtKnowledge` stub that stays false until Track 5 backs it; no client capture of the new text. See "Handover".

Phase A. Depends on Tracks 1 and 2 (ids and final ability names). Spec sections: "Houses as strain keepers", "The new strains", "Classification names", "Real-world references", each house's text.

## Goal

Everything the player reads about Morphlings names the new strains, says what they do, and carries the Court's framing without giving the Court away before the player has found it. Reference docs match the code.

## Two voices (SETTLED 2026-10-29, M7)

The player meets strains through the Order long before the Court. So the text runs in two registers:

- **The Order's catalogue** (always shown): the folk name, the greyed binomial, what the strain does, and the Alchemist's clinical tone. It treats the strains as lines the Order has collected and classified, with no house names.
- **The Court's line** (shown only once the Court is known): one extra tooltip line and Bestiary paragraph naming the house that raises it and the partner organism ("Raised by the Giving Light. Its partner is a green alga that feeds it sunlight."). This turns discovery into something the player sees on items they already own.


## Work

1. **Item text** (`en_us.json`): folk name, binomial subtitle, a one-line description, and `getMaturityBonusDescriptions` lines for each stage, all from Track 2's final ability names. Keep it short; the Conduit and Bestiary carry detail.
2. **Living Bestiary** (`SpecimenBestiaryDefinitions.MORPHLING_ENTRIES`, its lang keys): a field-note entry per strain in the Alchemist's voice, plus the Court paragraph behind discovery. The Bestiary is the natural home for the "Real-world references" material: fungus, convergent form, partner, phrased as the Order's observations, never as real-world citations.
3. **Alchemist dialogue and inquiries** (`data/hemomancy/dialogue_inquiry/alchemist/`): inquiries for each strain item, rewritten. The D5 "the best cultivators aren't in the Order" line belongs to Track 5's discovery route; this track only makes sure inquiry text doesn't contradict it.
4. **Conduit / Liber**: books mention Morphlings only generically today. Add the eight strain names where the Morphling cultivation page lists types, and check the Morphic Nectar and Cradle pages (the Cradle list changed: Track 1).
5. **Mycophant Tendril references**: its text calls the fungal head "the fungal Morphling head". Rename to match Track 3's neutral model name, or drop the Morphling wording.
6. **Lore reference** (`docs/LORE_REFERENCE.md`): add a Morphling strains section (house, fungus, convergent form, partner, belief in one line each). Note the anchoring versus petrification distinction and that strains are fungi convergent on animals. Leave Court and Votary changes to Track 5.
7. **Mechanics reference**: rewrite `docs/HEMOMANCY_REFERENCE.md` §16 (≈2572–2706): 16.1 types table from the strain table, home conditions, stage ladder, death-saves (Ink Reprieve at Ink Cap Apex, Cryptobiosis at Hushbell Primal, Last-Light retired), Cradle list, balance rules. Point to the new `docs/MORPHLING_STRAINS.md` for per-stage numbers.
8. **`docs/MORPHLING_STRAINS.md`**: the current-stats reference replacing `docs/archive/done/CURRENT_MORPHLING_STATS_AND_ABILITIES.md` (leave the old one in `done/` as history). Same shape: stage table, per-strain passive values, ability tables, upkeep, Cradle list, source list.
9. **Wiki**: the Morphling page under `wiki/`, if present.
10. **Spec housekeeping**: in `design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`, reconcile the inner houses' "Morphling direction" text with their strains (spec "Still open" item 1). Holding Stone and Last Marrow already match; Shared Wake, Woven Current, Veiled Fold and Still Bell still describe pre-strain directions.

## Voice

Item and Bestiary text follow the Alchemist's sheet in `docs/NPC_VOICES.md`. The Court lines are written once Track 9 adds Court speaker sheets; until then use plain field-note prose. Avoid "God", "demon", "devil" framing (the octopus stinkhorn's folk name is off-limits), and don't use *polycephalum* or "many-headed" for Dead Man's Rope: many heads belong to Hydra.

## Tests

- `HardCodedEnglishGuardrailTest`, `EnglishLiteralAllowListTest` stay green.
- A resource test: every strain has folk name, binomial, description, six maturity lines, a Bestiary entry and a Court line key.
- Track 1's old-name scan covers the lang file and books.


## Handover (written 2026-10-30, unattended run)

### What landed

- **Item text.** Each strain has `morphling.hemomancy.<id>.description` (the Order's one line), six maturity lines `morphling.hemomancy.<id>.stage.<unfed|fledgling|developing|mature|apex|primal>` (what the organism looks like at that stage, from the spec's appearance tables) and a Court line `morphling.hemomancy.<id>.court`. `MorphlingStrain` gained `descriptionKey()`, `stageKey(maturity)`, `courtKey()` and `STAGE_NAMES`. `MorphlingItem.appendHoverText` calls a new `appendStrainText` hook under the binomial; `HouseMorphlingItem` fills it with the description, the current stage's line and, only when `CourtKnowledge.isKnown(localPlayer)`, the Court line. The maturity bonus list now opens with the passive from Unfed (`message.hemomancy.<id>_morphling_item.passive`: Sun-Fed, Grip, Purse Siphon, Lateral Strands, Veil, Hush, Marrow Feeding, Cultivated Mending), then Track 2's four ladder lines. Ink Reprieve and Cryptobiosis lines and the Ink Reprieve blood source name were rewritten.
- **Court-known stub.** `common/capability/player/harbinger/court/CourtKnowledge.isKnown(Player)` always returns false until Track 5 backs it; a note with what to do is in `05-court-discovery.md` ("Note from Track 4").
- **Living Bestiary.** The eight family entries (`bestiary.hemomancy.morphling.<family>.description/source`) are field notes in the Alchemist's voice: fungus, convergent form and partner as the Order's observations, where the family is found, and the incubation catalysts. Each family has a Court paragraph (`.court`); `SpecimenBestiaryDefinitions.MorphlingEntry` carries `courtKey` and `morphlingCourtKey(layerId)` builds it; `BestiaryTabView` draws it under the source once the Court is known. The polyp specimen text and the Alchemist's wild-morphling line now talk about partner organisms instead of appendage layers.
- **Alchemist inquiries.** `dialogue_inquiry/alchemist/hemomancy/morphling_<id>.json` for all eight, two lines each (`hemomancy.alchemist.item_inquiry.morphling_<id>.line1/2`), in the Alchemist's sheet. None names a house or claims the Order raised the strains, so Track 5's "the best cultivators aren't in the Order" line stands.
- **Liber Sanguinium.** Three new pages in the Hematic Order chapter: `morphling_cultivation` (11), `morphling_strains` (12, all eight with Latin, tendency, role and home) and `morphling_cradle_and_nectar` (13, Morphic Nectar, the two death-saves and the Cradle's Primal list from the table). No Morphling pages existed before.
- **Mycophant Tendril.** Its lang text never named a Morphling; the HR (§Mycophant reward) and LORE_REFERENCE lines now describe a mushroom-head form under `FungalHeadAttachmentModel`, "not a Morphling strain".
- **Other lang.** "as a Ink Cap" fixed; Hyphal Cultivation's description now says what it does (3% per level on Whale Fall's Spore Sac chance); the 17 orphaned first-prototype keys (`chitinite_`, `pests_`, `tick_`, `urchin_morphling_item.*`, unused by any code) removed.
- **Docs.** HR §16 rewritten: overview, 16.1 types table, a ladders-and-home table, death-saves, balance rules, boundaries, shared effects and a "Strain text" subsection (keys and the Court gate); 16.2/16.3 wording fixed (partner families, Incubator enzyme weights, stage table). Stale HR lines fixed: Infected Fungus and Puffball no longer claim to be incubator catalysts, and the Hemopothecary sells a Marrowplume Morphling. `docs/MORPHLING_STRAINS.md` gained a stages table, a what-it-is paragraph per strain, upkeep, the Cradle list and the source list. LORE_REFERENCE has a new §6.3c "Morphling strains" (house, fungus, convergent form, partner and belief per strain; anchoring vs petrification; naming guardrails), the canon bullet points at it, and the stale "Urchin Morphling" mention is gone. Wiki: Advanced-Mechanics (the eight strains), Harbinger-Path (its stale "Infected Creatures" section rewritten), School-Combat (the strain table and an old-name line), Brined-Court (a house strains section). Spec: the Giving Light, Shared Wake, Woven Current, Veiled Fold and Still Bell "Morphling direction" text now names its strain, and "Still open" item 1 is reduced to tuning.
- **Tests.** New `MorphlingStrainTextResourceTest` (every strain's name, binomial, description, six stage lines, passive plus four ladder lines, Bestiary title/description/source/court, Court line, inquiry; the Order's text names no house or "Court"; the Liber lists match the table and its Cradle flags; no God/devil framing or "many-headed" in strain text or Liber pages). The source-text impact map was regenerated.

### Calls made unattended

- "Six maturity lines" read as one appearance line per stage (Unfed to Primal), shown in the tooltip for the current stage only. The bonus list keeps its existing shape (one line per power) with the passive added at Unfed, since the tooltip had never described the passive.
- The Court line and Court paragraph are plain field-note prose, as this file says, until Track 9 adds Court speaker sheets. The Court paragraph doesn't wait on a Bestiary record of the Court itself, only on the entry being discovered and the Court known.
- The stub lives in `common/capability/player/harbinger/court/` (the README's home for Court player state) rather than in Track 5's eventual `CourtProgress`, so Track 5 only replaces one method body.
- Bestiary source lines name where each family is found (from `MorphlingPolypLayerRules`) and its incubation catalysts, so the entry is useful without the Conduit.
- The Liber pages went in the Hematic Order chapter (ordinality 11-13, after Druse Crystallization) because the Order is the voice that catalogues strains; there was no existing Morphling page to extend.
- The inquiries are not degree-gated: wild-bound strains reach players from Degree 2.
- The 17 first-prototype lang keys were deleted as dead text (nothing reads them); they were the "legacy Tick, Pests, Urchin and Chitinite" classes the wiki called unregistered.
- School-Combat's strain table now gives each strain's preferred school and notes that no strain calls `MorphlingCombat.afflict` yet, instead of the old per-strain payload claims, which described the deleted strains.
- Material Atlas descriptions (`material.hemomancy.morphling_<id>.desc`, Track 1) were left as they are; they already match.

### What remains

- Track 5: back `CourtKnowledge.isKnown` with the Court-known state and sync it, then prove a known player sees the Court line and paragraph (nothing shows them today).
- Track 9: once Court speaker sheets exist, the Court lines may be rewritten in a house voice.
- Not done here: no client capture of the tooltip, Bestiary dossier or Liber pages (text only; the resource test and the book binding test cover the data). Numbers in the text follow Track 2's first pass; if in-play tuning (M8) changes a number quoted in a page, inquiry or doc, update it with the tuning.
