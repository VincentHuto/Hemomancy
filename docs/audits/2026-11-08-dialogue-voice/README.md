# Dialogue voice pass 4, 2026-11-08: cast-wide summary

Voice pass 4 rewrote the Hermit first as the worked example (applied: `en_us.json` and his `docs/NPC_VOICES.md` sheet
are in the working tree). Then it ran one writer and one reviewer for each of the other fifteen speaker groups, against
`tools/i18n/voice_rewrite/pass4/BRIEF.md`. Each group's folder holds `old.json`, `proposed.json`, both scans, the old
and new sheet and a `comparison.md`. **Nothing outside the Hermit has been applied.** Every `old.json` still matches
`en_us.json` key for key, and every proposed key already exists there.

Checked for this summary: I reran `python -I tools/i18n/voice_rewrite/pass4/scan.py metrics` on every `old.json`
(the Hermit's included) and on every `old.json` with its `proposed.json` laid over it. Every before figure matches the
writers' reports, and every after figure matches the reviewers' (writers' after figures are older where a reviewer
edited). Two counts in the writers' reports are out of date: court_a's `proposed.json` holds 101 lines (the report says
100) and voyager_wayfarer's holds 58 (the report says 55). The scanner counts spoken lines after stripping, so its
`lines` can be a little lower than the key count (Alchemist 349 against 356 keys).

## Corrections after independent review (2026-11-08)

Read `independent-review-codex.md` beside this audit. Its main findings were checked against the code and the proposals
and the following changed. Figures and tables below this section are from before these changes unless marked.

- **The scanner is a set of search leads, not a quality score.** The first `scan.py` hid stage directions, counted
  possessives as contractions and matched numbers as substrings. It is fixed, and `scan_v1.py` is kept. 131 of the 202
  hits the pass removed came from the one short-closer rule, so the before and after hit rates below are not evidence
  of quality. The statement that the scanner "catches about one pattern in six" compared two different counting systems
  and is withdrawn. Totals here pool group entries, with shared keys counted once per group and stage directions
  excluded.
- **The root cause is only partly established.** The October sheets plausibly caused much of the problem, and copied
  example lines are direct evidence. Shared keys, repeated UI states and writers' own defaults were not controlled for.
- **The first brief repeated the fault.** Asking for "what fills their days (concrete objects and habits)" and giving one
  worked example made a new recipe: dry joke tags, softeners and added anecdotes. `BRIEF.md` is now v2 (`BRIEF_v1.md`
  is kept), with a sheet format built on intent, knowledge limits and what the speaker attends to.
- **The Hermit was revised.** Three factual errors of the first rewrite are fixed ("most" for "many", an invented "The
  others are expecting you", the per-player blessing), and his opening and rank greetings were rewritten around the heart
  and his knowledge limits. His sheet is in the v2 format.
- **Monolith and Fungal Entity were redone from brief v2.** The Monolith now changes 8 of 62 lines instead of 47. The
  Fungal Entity changes 18 of 88 instead of 68. The first attempts are kept as `proposed_v1.json`.
- **Confirmed errors and softeners were patched** in 12 other proposals (35 lines, listed in each `comparison.md` as
  "After independent review"; `proposed_pre_review.json` is kept).
- **Not yet done.** The other thirteen groups' `sheet_new.md` files are still in the v1 format, and need converting when
  each speaker is applied. All five recruit key conflicts are settled.
- **Current totals (non-Hermit proposals):** 1336 entries, 1331 unique keys, 39
  fingerprint rows (this replaces the figure of 47 in section 5).
- **Pins missing from section 5.** `CondenserTeachingResourceTest` needs lowercase "cauldron" in seven lesson and ledger
  keys and "Venous Stone" in `vicar.neophyte.projection_hint`. `PallidVesselDialogueTest` needs "Sanguine Formation" in
  `alchemist.initiate.loom_raising`. `PlaythroughVotaryGameTests` reads `vicar.votary.degree_hint`. The "half a story" /
  no "eighth" pin listed for `vicar.archon.final_truth` is wrong: `AuditDecisionsResourceTest` bans "eighth" in other
  keys and the Archon playthrough only checks that the key is reachable.

**Applied so far (2026-11-08):** Hermit, Vicar, Alchemist, Mnemonist, Artificer, Vein-Mason, Voyager, Votary Wayfarer, the Circus and the Successors are in `en_us.json` with v2 sheets in `docs/NPC_VOICES.md`.
Each went in after a line-by-line read that cut further additions under brief v2 (about 70 lines across the four; their
folders keep `proposed_pre_apply.json`). All five recruit conflicts are settled. Every other speaker is still a proposal.

## 1. Verdict

The problem is cast-wide, not the Hermit's alone. Of 2,189 unique keys read outside the Hermit, the writers rewrote
1,430 (65%). Every group needed at least a third of its lines rewritten, and seven groups needed three quarters or
more. Across the cast, 40.8% of sentences were six words or fewer, and the writers classified 2,145 pattern hits by
reading, about one per line. The scanner found only 356 of them (16.9 per 100 lines), because its word lists catch the
Hermit's oracle vocabulary ("veil", "you alone", "You have proven") and miss the forms the rest of the cast fell into:
bare "Not X." fragments, cadence, and one sentence copied across many mouths. The cause is the same everywhere. The
October sheets prescribed a cadence ("short", "hammer cadence", "fewest words, verdict then silence"), a word list and
"habits to keep", and the writers followed them literally. That turned the habit lines into tics and the cadence into
a metronome, and the example lines were pasted into the dialogue. The proposals bring the cast to 10.6 words a sentence
with 29% short sentences and 8.4 scanner hits per 100 lines, with no unexplained fact lost (each `scan.py facts` loss is
restored or noted as a Content change).

## 2. Worst offenders

Ranked by my reading of the comparison docs and the reviewers' notes, weighing rewrite share, cadence (short-sentence
share and spread) and how much a player would notice. "Scanner" is pattern hits per 100 lines before, with its rank in
brackets. "Read" is the writer's P1-P13 count per 100 lines (writers counted a little differently, so treat it as
rough). Words per sentence are before the pass.

| Rank | Speaker | Lines read | Rewritten | Words/sentence (sd) | 6 words or fewer | Scanner | Read | Top patterns | Reviewer |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Hermit | 48 | 48 (applied) | 7.60 (3.45) | 42% | 89.7 (1) | n/a | P1, P2, P3, P4, P8 | worked example |
| 2 | Guardian and Unstained Scout | 84 | 69 | 4.06 (2.45) | 85% | 53.7 (2) | 144 | P13, P3, P1, P5, P9 | good |
| 3 | Mnemonist | 236 | 210 | 7.58 (3.85) | 43% | 14.5 (10) | 109 | P9, P5, P3, P1, P10 | good |
| 4 | Artificer | 155 | 144 | 6.65 (3.64) | 55% | 19.4 (5) | 117 | P5, P9, P1, P3, P10 | good |
| 5 | Sanguine Monolith | 62 | 47 | 6.42 (2.70) | 56% | 10.2 (14) | 135 | P3, P5, P1, P9, P12 | good |
| 6 | Vein-Mason | 81 | 72 | 7.12 (2.92) | 46% | 12.3 (13) | 117 | P5, P1, P9, P10, P3 | good |
| 7 | Zealot and Acolyte | 150 | 122 | 9.93 (5.49) | 30% | 15.0 (8) | 151 | P9, P1, P5, P3, P7 | good |
| 8 | Court B (deep houses, chorus) | 205 | 122 | 7.56 (6.04) | 60% | 25.3 (3) | 104 | P9, P5, P3, P1, P13 | acceptable |
| 9 | Our Lady and Annetta | 109 | 50 | 6.85 (3.27) | 45% | 6.7 (16) | 92 | P9, P3, P5, P1, P7 | good |
| 10 | Fungal Entity | 88 | 68 | 11.22 (5.69) | 20% | 14.5 (10) | 132 | P9, P1, P2, P3, P7 | good |
| 11 | Court A (shore houses) | 195 | 101 | 8.93 (5.81) | 43% | 14.9 (9) | 77 | P9, P5, P3, P12, P13 | acceptable |
| 12 | Crimson Troupe | 141 | 81 | 8.23 (5.35) | 46% | 15.6 (7) | 107 | P9, P5, P3, P1, P11 | good |
| 13 | Vicar | 223 | 149 | 16.51 (9.75) | 15% | 16.4 (6) | 63 | P1, P3, P9, P4, P7 | good |
| 14 | Voyager and Wayfarer | 83 | 58 | 12.01 (6.14) | 25% | 9.6 (15) | 135 | P3, P9, P10, P1, P7 | good |
| 15 | Successors and recruits | 27 | 20 | 9.21 (5.97) | 40% | 22.2 (4) | 130 | P12, P1, P6, P3, P13 | good |
| 16 | Alchemist | 356 | 122 | 11.53 (5.92) | 22% | 13.2 (12) | 45 | P6, P1, P7, P5, P4 | good |

Our Lady alone is worse than her row: 46 of her 64 lines were rewritten, and Annetta's 45 needed only 4. The Vicar and
Alchemist rank low because their rhythm was already varied. Their damage sat in particular families (Degree greetings,
item inquiries, clinical hints).

**Why the top five are worst.**
1. **Hermit.** The voice that started the pass. Every pattern on the brief came from him: exclusivity, closers and
   pivots stacked in one line. `hermit.no_blood.duty.line2`: "Accept my blessing, and this temple's heart will answer
   you alone. Choose the path first. Claim it after, and I will tell you where the road goes. Mourn if you must. The
   blood needs a new vessel. Old keepers have to end." Fixed and applied.
2. **Guardian and Unstained Scout.** The worst still in the game. A telegram at 4 words a sentence, 85% of sentences
   six words or fewer, and two speakers with the same numbers (4.0 and 4.2 words, about 28 contractions, no
   questions) and a shared newcomer line. The Guardian reads 19 of his 24 item names aloud as headings. Even after the
   pass they are the highest-scoring group (25.6), because the short closers are in character.
   `guardian.item_inquiry.crossbow.line1`: "Crossbow. Hits hard at distance. Slow reload. That's the price."
3. **Mnemonist.** The October sheet made them the one speaker allowed a closing aphorism, so most nodes end on a moral.
   Their 110 item inquiries share one template, and 89% of the lines needed rewriting. The scanner ranked them tenth.
   `dialogue.event.mnemonist_woven_vessel_missing_archive`: "A book, ink, and three sheets of paper. Memory without an
   index is only hunger."
4. **Artificer.** The "hammer cadence" sheet made 55% of his sentences six words or fewer, gave him a verdict closer
   on nearly every line and a private mythology of metal that listens and learns. One line appeared word for word under
   two keys. 93% rewritten, the highest share. `artificer.armature.line2`: "Wear the armor. Load the bowls with
   reagent. Fill the reservoir. Step into the frame. Hold still. Let the metal learn you."
5. **Sanguine Monolith.** Short and uniform: 6.4 words with a 2.7 spread, the tightest metronome after the Guardian.
   The scanner ranked it fourteenth, but 18 of its 62 lines end on an oracle closer and its row sat on top of the
   Vein-Mason's and Our Lady's. `monolith.item_inquiry.rite_sites.line2`: "You are no longer assembling tools. You are
   assembling conditions."

The Vein-Mason (6th) fails the same way as the Monolith, with the same numbers. Court B (8th) is the worst for
cross-role copying: one sentence per situation, pasted into up to six mouths.

## 3. The repeated patterns across the cast

Totals are the writers' counts over the fifteen groups (the Hermit's lines were not classified by id).

| id | Total | Concentrates in | Real old lines |
|---|---|---|---|
| P9 repetition | 420 | Court B 68, Court A 58, Mnemonist 52, Zealot/Acolyte 42 | "scrubbed (clean) of every partner" in 11 refusals across both Court groups; Fungal Entity `whisper.pome_drop.*`, nine copies of "The fruit of Golachab has fallen, and it fell for you." with the husk name swapped; Vicar "I confess" in 6 lines |
| P5 uniform cadence | 319 | Court B 55, Mnemonist 45, Artificer 34, Zealot/Acolyte 30 | Tidewarden `greet.referred`: "They said you'd come. You came. Good. The passage is clear. I checked it this morning." |
| P3 quotable closer | 284 | Mnemonist 42, Zealot/Acolyte 28, Court B 27, Voyager/Wayfarer 23 | Voyager `harbinger.line2`: "Certainty drowns faster than sailors do."; Marrow-tender `strain.own` ends "Let it.", the Hermit's banned closer word for word |
| P1 contrast pivot | 272 | Zealot/Acolyte 38, Mnemonist 36, Vicar 27, Artificer 22 | Vein-Mason `inquiry.blank_memory.line2`: "Their craft is the instruction. Mine is the tissue. The two must agree."; Head-tender `challenge.line1`: "It is an honor to be asked, and a greater one to be refused by strength." |
| P6 checklist wording | 162 | Alchemist 33, Mnemonist 18, Zealot/Acolyte 18, Circus 12 | `clinical.next.iron_artificer`: "At Degree 2, obtain Hematic Iron Scraps and meet the Artificer. Return to the Alchemist after crafting, filling, and withdrawing from your Cabinet." |
| P13 other machine tells | 128 | Guardian/Scout 24, Mnemonist, Artificer and Alchemist 12 each | item name as a heading then fragments (Guardian); third-person self-reference (Alchemist, 9 lines); rhetorical questions answered at once (Host); "Minecraft day" and "NPCs" in speech |
| P10 personified abstraction | 119 | Mnemonist 15, Artificer 15, Voyager/Wayfarer 13, Zealot/Acolyte 12 | Mnemonist `woven.line1`: "Your blood knows enough words now."; Artificer `hematic_iron_scrap.line1`: "The metal's started listening."; Ringmaster `line.succession_complete`: "The pavilion knows its successor." |
| P7 explains its own metaphor | 105 | Alchemist 17, Zealot/Acolyte 16, Vicar 14, Mnemonist 10 | Our Lady `vigilant.v2.follow`: "Clarity is a lens. You carry it and never set it down." |
| P11 tricolon for rhythm | 98 | Circus 14, Voyager/Wayfarer 10, Alchemist 10, Guardian/Scout 9 | Guardian `absolution_dagger.line1`: "Last resort. Mercy. Self-defense." |
| P12 sounds like another speaker | 82 | Court A 10, Zealot/Acolyte, Mnemonist and Court B 8 each | the Scout's newcomer line copied the Guardian's; the Vein-Mason borrowed the Mnemonist's listening imagery ("Your blood shouted the first one. Listen for the voice beneath it"); the Monolith borrowed the Vein-Mason's "become architecture" |
| P8 stock fantasy wording | 74 | Zealot/Acolyte 10, Mnemonist 8, Lady 7, Alchemist 7 | Vicar `uninitiated.line2`: "A wanderer, not yet on the path of blood." |
| P4 praise or title formula | 57 | Alchemist 13, Vicar 8, Fungal Entity 6, Zealot/Acolyte 5 | Vicar `initiate.line1`: "Initiate of the Incarnadine Fane. The blood moves in you now and speaks, and the Fane has recognized in you something most practitioners never develop." |
| P2 exclusivity | 25 | Fungal Entity 12, Vicar 4 | "it fell for you" nine times; Fungal `communion.v3.line3`: "You, Archon, are what the whole house was built to bloom." |

**Phrases and openers shared by different speakers** (my count over every `old.json`, Hermit included):
- Balanced negation ("not X, a Y", ", not Y.") appears in 13 of 16 groups, 47 times by a plain regex, and bare
  "Not X." fragments add more that no regex catches ("Not me. Not now.", "Not a threat. Not prey. Nothing.").
- A sentence opening on "Then" chains steps in 15 of 16 groups (60 times; Circus 10, Court B 8, Mnemonist 8).
- "The blood" opens 27 sentences in 7 groups. "You have" opens 43 sentences in 12 groups.
- The Brazier step list ("light it with Blood Projection", "hold Blood Absorption on the", "in a lit Iron Brazier")
  appears word for word in the Alchemist, Mnemonist, Vicar and Vein-Mason, and "on a Greater Working floor" in four
  groups. Every teacher reads the ledger's steps aloud.
- "the vessel" for the player's body: the Vein-Mason, Alchemist, Mnemonist and Vicar (8 times).
- Court refusals: "harm / strike / hurt you" 9 times in 4 groups, "grow into the blood" in both Court groups, the
  one-move refusal "The custom allows one move. Yours is spent." and the home-water hint in up to six roles each.
- Sheet example lines pasted into dialogue: six Court A roles, and the Gardener reads her sheet's Avoid note aloud.

**Confusable pairs before the pass.** Guardian and Scout (same on every column). Zealot and Acolyte (the old sheet
already said so; the Acolyte also borrowed the Zealot's step lists). Mnemonist, Vein-Mason and Monolith (all 6-7 words,
no contractions, no questions). The Monolith, Our Lady and the Vein-Mason (same short, cold row). Our Lady and the
Fungal Entity ("child"). The Voyager and the Wayfarer (the junior speaking the captain's aphorisms). The Acrobat and
Strongman (same fragment drum), the Fire Breather and Threadkeeper (both opened lines on "Two..."). The Edge-swimmer and
Tidewarden. The Arbor and Lamp-colony against the Rooted elder. The Bellwarden, Veiled resident and Kindled elder (one
refusal between them). The Offering-bearer and Head-tender (identical `strain.own`). Every successor against their
donor teacher. Each sheet in the proposals separates its pair on at least two contrast-map columns.

## 4. How to avoid them

Proposed text for "Rules every speaker shares" in `docs/NPC_VOICES.md`, to paste after the existing "Patterns to cut"
bullet:

```markdown
- **Give each line a job.** Start from what the speaker sees, needs, refuses or can teach at that moment, and end when
  that job is done. A sheet says what the speaker is trying to do, what they know at each stage and what they attend to
  under pressure. It never prescribes a rhythm or lists props, gestures or words to reuse. Contrast-map numbers are
  measured afterwards to check two voices stay apart.
- **No habit lines.** A sheet may pin a line that tests or canon need, and may quote one before/after example. It does
  not list "habits to keep" for writers to reuse. A phrase, opener or gesture appears at most once in a speaker's whole
  tree, and a signature line (the Hermit's farewell) is named as the one exception.
- **Sheet text is not dialogue.** Never copy a sheet's example, Avoid note or description into a line.
- **One situation, many voices.** Where several NPCs share a tree (the Court's refusal, one-switch refusal, newcomer
  pointer and home-water hint; the station-upgrade prompt, ready, claimed and requirements families; the Degree
  greetings; the item inquiries), each one says it in their own words. Read the whole family side by side before
  calling it done. Swapping the house, item or rank into one sentence is a template.
- **Step lists belong to the owner.** A hint names the next step, who teaches it and at most the one detail people get
  wrong. The ledger and the NPC who owns the lesson carry the full list. No teacher recites another's steps.
- **Steps as speech.** Join steps with "and", "then" and "once" in the order they happen. No run of imperative
  fragments ("Frame... Hold... Offer... Feed it 500 mL.").
- **No contrast pivots, closers or explained metaphors by default.** That covers bare "Not X." fragments, "X, not Y",
  "X is another matter", and a line that ends on a moral or a motto. A contrast stays when it separates two real actions
  or records a real disagreement. A short order stays when timing or danger calls for it. Don't swap a closer for a joke
  tag or a softener ("I won't pretend", "for what it is worth", "I expect").
- **People act, things don't, by default.** Blood, metal, the sea and a tendency do not know, remember or answer. The
  exceptions are things the lore makes alive or a stage of the character: the Fungal Entity's tissue, the Monolith's
  move from "the stone" to "I".
- **Greet the person, not the rank.** No full rank title plus compliment openers. A Degree greeting says what the
  speaker notices or wants to know.
- **No tricolons for rhythm.** A list of three is fine when there are three things.
- **The speaker's own words.** No UI or data words in speech (tooltip, inventory, NPCs, entity type, Minecraft day,
  initialized), unless the term is how the player finds the action. The player is "you", never "the vessel".
- **Questions are real.** A question demands an answer, tests a pupil or exposes what the speaker cares about. Drop
  one that only sets up a punchline.
- **Borrowed language is a sign of drift.** Each sheet says which neighbours it must not sound like. Listening and
  grammar are the Mnemonist's, stone and mortar the Vein-Mason's, "child" nobody's.
- **The scanner is for leads.** `scan.py metrics` and `facts` find places to read. A low hit rate is not an acceptance
  criterion, and no line is reworded to lower a count. Any line that grows by 40 characters and 50% is read at native
  dialogue scale.
```

Proposed replacement for the second sentence of "Why this exists" (the old sheets' cause):

```markdown
The October sheets prescribed a cadence and a word list for each speaker, and writers followed them literally: the
cadence became a metronome, the "habits to keep" became tics and the example lines were pasted into dialogue. Pass 4
(2026-11-08) is replacing them, speaker by speaker, with sheets built on intent and knowledge limits.
```

Also decide which tool the contrast map quotes. The table's "now" column comes from `pass2/voicestats.py`, and pass 4
measured with `pass4/scan.py`. They disagree (the Guardian reads 7.5 in one and 4.0 in the other). I suggest moving the
map to `scan.py` and noting the tool under the table.

## 5. Cleanup plan

**Order: worst first, with the shared keys last.** The worst groups have the fewest fingerprint lines (Guardian and
Scout, Monolith and Vein-Mason change none), so they prove the apply steps cheaply and fix what players notice most.
The two Court groups wait for a short joint pass, because both reviewers rated them only "acceptable" and found the
cross-role template partly survived. The Successors go last because five of their keys conflict with the Vicar,
Alchemist and Mnemonist.

**Each step does the same five things.**
1. Copy the speaker's `proposed.json` into `src/main/resources/assets/hemomancy/lang/en_us.json` (existing keys only;
   no key is new). Hold back any key in the conflict list below until it is settled.
2. Replace the speaker's section of `docs/NPC_VOICES.md` with `sheet_new.md`, and update their contrast-map row the way
   the Hermit's was ("now" measured, new target, "pass 4, 2026-11-08").
3. Check the speaker's entries in `src/main/resources/assets/hemomancy/dialogue_briefs/`. Every proposed key already
   has a brief. A rewrite that keeps intent keeps its brief. Update `about` for every Content change and `tone` where
   the brief describes the old fault (the Acolyte's "hedging", the Mnemonist's oracle tones).
4. Run `./gradlew.bat test` and fix any pin. Once only `said=[...]` text differs, regenerate the affected lines in
   `src/gameTest/resources/dialogue/route-fingerprints.txt`.
5. Run `./gradlew.bat runGameTestServer` for `dialogue_route_validation` and the playthrough GameTests named below. Then
   do a native read in `runClient`: turn on `debug.dialogueBriefMode` to check each changed key's brief still describes
   the line, and off to read the new lines in the box for length. One Gradle run at a time.

Update the topic doc (`docs/NPC_DIALOGUE_AND_TRADE.md`, and `docs/HEMOMANCY_REFERENCE.md` where a Content change
touches a mechanic) in the same step.

| Step | Speaker | Lines | Brief files | Fingerprint lines | Pins and tests to watch |
|---|---|---|---|---|---|
| 1 | Guardian and Scout | 69 | `unstained_church.json` 67, `vicar_succession.json` 2 | none (`scout_gives_notes` kept) | `NpcTradeCardsTest` (`trade.scout.refuse_harbinger`), `lore-options.txt` rapport keys, `DialogueInquiryCanonTest` forbidden phrases |
| 2 | Sanguine Monolith | 47 | `circus_monolith.json` | none | `counsel.press_further`, `counsel.past_the_seed` (Seed pin), `monolith_cornerstone_unready`, `rapport.monolith.where_it_goes.line1`, the `shatterBeforeCornerstone` GameTest |
| 3 | Vein-Mason | 72 | `artificer_anchorite_clinical.json` 69, `alchemist.json` 2, `vicar_succession.json` 1 | none | `VeinMasonTeachingResourceTest`, `VeinMasonMissionSourceTest`, 13 pinned keys, the AGENTS.md Scar teaching lifecycle |
| 4 | Mnemonist | 209 (hold `recruit.mnemonist.consider`) | `mnemonist_antecedent.json` 202, `artificer_anchorite_clinical.json` 8 | 13 lines: `mnemonist_starter_granted`, `_starter_unavailable`, `_woven_vessel_unready`, `_woven_vessel_complete`, `vagrant_mind_inquiry.bearing_missing` | `mnemonic_doctrine.line1`, `woven_vessel.recipe.line1/2`, `scar_pattern.loadout`, `ending.mnemonist.undecided` |
| 5 | Artificer | 144 | `artificer_anchorite_clinical.json` 142, `vicar_succession.json` 1, `circus_monolith.json` 1 | 12 lines: `consecration_kit_unready` and the five `*_fitting_unready` | `PlaythroughArchonGameTests` ("cornerstone" in `assignment.weight_of_frame.cornerstone`), 11 pinned keys |
| 6 | Zealot and Acolyte | 122 | `unstained_church.json` 121, `vicar_succession.json` 1 | 3 lines: `reject_help` | `trade.zealot.refuse_harbinger` |
| 7 | Our Lady and Annetta | 50 | `lady_fungal_wanderers.json` | none | `AnnettaDialogueSourceTest`, `AnnettaModelSourceTest`, `AnnettaCanonSourceTest`, five sheet pins kept verbatim |
| 8 | Fungal Entity | 68 | `lady_fungal_wanderers.json` | 6 lines: `qliphoth_communion_done`, `whisper_truth_acknowledged` | `ArchonEndingDialogueTest` (12 keys) |
| 9 | Court A and Court B together | 101 + 122 | `court.json` | none | `CourtFollowerGameTests` ("planted" in `warden.named.growth`), `PlaythroughBrinedCourtGameTests` ("Vent Sanctum" in `head_tender.greet.stranger`), `champion.named` verbatim. First: write the nine missing Court A newcomer lines and vary the kept `greet.referred` / `greet.known` openers across both groups |
| 10 | Crimson Troupe | 81 | `circus_monolith.json` | none | `ringmaster.silent`, `line.succession`, `line.liberation`, `warning.succession`, both `finale.complete.*`, `circus_performer.welcome` |
| 11 | Vicar | 147 (hold `recruit.vicar.consider`, `.accept`) | `vicar_succession.json` 145, `court.json` 4 | 3 lines: `vicar_hermit_road_report` route (said text of `vicar_hermit_road_unproven`) | 17 pinned keys, `archon.final_truth` ("half a story", no "decide", no "eighth"), the Rooted Vein tokens |
| 12 | Voyager and Wayfarer | 58 | `lady_fungal_wanderers.json` 49, `court.json` 6, `unstained_church.json` 3 | none | `CourtDiscoveryDialogueTest`, `PlaythroughBrinedCourtGameTests`, `PlaythroughInitiateGameTests`, `lore-options.txt` |
| 13 | Alchemist | 120 (hold `recruit.alchemist.consider`, `.accept`) | `alchemist.json` 98, `artificer_anchorite_clinical.json` 24 | 10 lines: `alchemist_bestiary_unready` | 22 pinned keys, the AGENTS.md First Draws and First Separation contracts |
| 14 | Successors and recruits | 20, after settling the conflicts | `vicar_succession.json` 14, `alchemist.json` 4, `mnemonist_antecedent.json` 2 | none | `succession.teaching.artificer` ("Armor Stand and Blood Stained Stone", "brazier"), `succession.unavailable`, `recruit.outpost_already_member` |
| 15 | Follow-up pass | see "Missing speakers" | | | |

That is 47 fingerprint lines in all, every one a `said=[...]` text change only.

### Key conflicts

Five keys are in two `proposed.json` files with different text. Pick one per key before step 4:

| Key | Owners | Difference |
|---|---|---|
| `hemomancy.dialogue.recruit.alchemist.consider` | alchemist, successors_recruits | both keep the Lodge doctrine at arm's length; the Alchemist's is in its new sheet |
| `hemomancy.dialogue.recruit.alchemist.accept` | alchemist, successors_recruits | the Alchemist's still says the blood is "bound to your pool now", which `recruit_offer` shows before the player confirms; the Successors' version fixes that timing |
| `hemomancy.dialogue.recruit.mnemonist.consider` | mnemonist, successors_recruits | both keep 1,000 mL |
| `hemomancy.dialogue.recruit.vicar.consider` | vicar, successors_recruits | the Vicar's drops "respect ... obedience"; the Successors' keeps it |
| `hemomancy.dialogue.recruit.vicar.accept` | vicar, successors_recruits | the Successors' adds "Accept me, and" for the timing |

`recruit.mnemonist.accept` is in both `old.json` files but only one proposal. My suggestion: take the teacher's text and
apply the Successors' timing fix to `alchemist.accept`.

### Decisions for the author, by speaker

Every new piece of colour is marked in its line's note. Each speaker also has a new contrast-map row to approve.

- **Vicar.** Content change in `neophyte.first_bloodcraft_reward.unready` (points to the ledger, keeps only the
  cauldron detail). Format fix for `uninitiated.line1` (bracketed stage direction). `illuminatus.degree_hint` and
  `sanctified.degree_hint` look dead (`withCovenantGuidance` replaces them): delete or keep. New row 13-18 words,
  3-12 contractions, 5-10% questions, or join some lessons back. Closers "Take a good weapon." and "That is the whole of
  what I was given.". `potion.line2` joke naming the Alchemist. The 21 Antecedent lines kept as they are. Colour: desk
  book and the player's page, the rubric, chapel windows, "take more food", the lost night, "our old books are full of
  the wrong names", "I still work for Erythravane", the Heart of the Sea treatises.
- **Alchemist.** `purifying.explain` now points to the Vicar (Content change). "label" for tooltip, "people" for NPCs,
  "lets it go in a hurry" for burst vessel. Read all eight Degree greetings together (five title openers remain).
  Questions at 4.0%, the floor of the target. Colour: forceps, valve, the flask, the documentation joke, the Vicars
  joke, the touchy Artificer, the ruined reagent, the enzyme bench images, "under the bench".
- **Mnemonist.** New row 9-12 words, 10-25 contractions, 0% questions, and withdraw the aphorism exception. Grave Debt
  wording corrected by the reviewer against `SchoolHitHelper.tryRefundGraveDebt`. `chamber.line1` now assumes the
  player dreamt of the Chamber. Four station-upgrade `.prompt` keys rewritten as spoken offers: confirm they display as
  speech. Colour: glow ink at night, packing the index on recruitment, the heron vigil, blotted pages, "I can hear them
  both stumble".
- **Artificer.** Assignment reminders written in his voice (briefs call them plain labels). Inspection records left as
  system text. The vow story (the wearer lived, the set was lost). "done on the consecrated frame" in
  `crimson_vestment.first_upgrade`. Kept dismissals "Done. Next." and "Take your stillness somewhere else.". Colour: coat
  on the hook, the customer who chose by colour, the mask opinion, lacquer smell, drawer, kit shelf, nose.
- **Vein-Mason.** Two questions now allowed ("Lost it already?", "See the ridge...?"). `completed.line2` reading.
  "Enough for now." gone everywhere: bring one back? Colour, especially the nails in `refusal.line1` (implies
  Still-water leaves a mark). Four bracketed stage directions in 81 lines.
- **Monolith.** Restore "It had to be you." and "Every degree. Every name."? Should the fifth-Degree greeting name the
  mycelial network? A fragment warning in `press_again` for `shatterBeforeCornerstone` would be a content change.
  `armature_cornerstone.line2` hints at Silentium at the seventh Degree. Faces detail dropped from
  `who_stood_here.line1`. Colour: the sleeper turning over, "I can say I to you now", the cold, the warmth, "Every rite
  leaves a little".
- **Voyager and Wayfarer.** Keep "We feed the archive. We don't hunt it." once as a signature line? Content change in
  `voyager.court.known.alchemist`. "the captain-scholar" in four Wayfarer lines. Colour on both sides (tar, the bench,
  sounding leads, seasickness, the lead line, the whale under the Cog).
- **Zealot and Acolyte.** The Zealot's two registers (uncontracted doctrine, contracted talk). Bare Degree names in
  greetings. `acolyte.corrupted.line2` claims blood work during purification sets the player back: check the mechanic.
  Lines 1.4 to 2.5 times their old length (`acolyte.tainted.line2` 154 characters). Colour: wrists, verdigris, the Lily
  rite, buckets, sugar in tea, stacking bowls, the older women, the soup woman, the halos.
- **Guardian and Scout.** Restore the old habits "Stand your ground. Keep your hands clean." and "Not a threat. Not
  prey. Nothing."? `guardian.item_inquiry.warhammer.line2` reading of "contain" (the person). "what they eat" cut from
  `scout.what_happened.line1`. Both now ask questions, which the old sheets forbade. Colour: the boot, "I wear the same
  set", the wrist gap, the plank, "every recruit's hands".
- **Our Lady and Annetta.** New row reverses the October short-sentence rule. "I keep a few of them" plants the §8.6
  doubt at Cleansing. `resolute.v1.line2` read as a conditional. The §8.6 eradication push is still unwritten.
  `annetta.event.cured` "out of her back" against the spawn position. `annetta.draught.truth.line1` is the player's line
  in Annetta's box (open decision 8). Colour: the river in winter, abstinence, "everyone who left".
- **Fungal Entity.** `spine_growth.degree8.canon` can never show: delete or wire. Delete the dead
  `whisper.archon.v1.line2` and `whisper.sanctified.v2.line2`. `core_witness.line3` now ends "Which road will you
  take?". "We thinned you" in `communion.v1.schism1`. The thagirion taste and the altar growths that come back. "child of
  the Hematic Order" cut from `communion.v0.line2`.
- **Crimson Troupe.** Restore "Only the ruin answers you now."? `status.mastered` per-school guest act (Content change).
  Per-role keys for the shared status and aftermath lines (decision 11). Dead performer barks (decision 13). A speaker
  for `challenge.*`. Pronouns for the Acrobat, Stilt Walker and Threadkeeper. Colour list in the comparison intro.
- **Court A.** Rapport teasers in the speaker's voice or UI? The Lightkeeper's amends line. One "That's a promise" left.
  Both transgressor greetings point to the table. Move the "Shared Court lines" rule into the Court preamble so both
  groups follow it. Colour: the member row, the soot, the camp guests, the small fish, the swell count.
- **Court B.** Who owns Marimo's chorus (taken by court_b). Host questions down to 12-20%, Rooted elder to 15-21 words
  (overlaps the Lamp-colony), Head-tender 13-18 (overlaps the Lightkeeper). The Veiled resident `strain.other` reading.
  Nine Veiled resident lines 1.7 to 2.9 times their old length. Content changes in `head_tender.join.standing`,
  `challenge.refused_standing` and `rooted_elder.join.standing` (they name each house's custom). "new to the blood"
  still opens four newcomer lines. Colour list under the writer's `colour additions`.
- **Successors and recruits.** The key conflicts above. `recruit.release.prompt` is shown on the player's option:
  confirm how `DialogueScreen` shows it before applying the first-person text. `succession.rejection` voice pass. The
  three temperaments only reach greetings until successors get their own trees (decision 10).

### Unsure keys and shared keys

- **Shared between speakers:** `hemomancy.clinical.inquiry.later` (Alchemist, Artificer, Mnemonist; in the
  Alchemist's voice, needs per-teacher keys), the Antecedent `sample.identified`, `sample.unknown` and
  `cylinder.description` (Vicar and Alchemist), `dialogue.event.gives_stained_church_map` (Vicar and Zealot),
  `dialogue.unstained.observance.*` (Acolyte, Zealot, Guardian), `dialogue.recruit.release.prompt`.
- **Narration and system lines in a speaker's old shape:** the Monolith husk messages (`monolith.gate.*`,
  `already_breaking`, `dormant`, `overgrown`), `voyager.introduction.observed`, `dialogue.event.relay_chart_*`, the
  Artificer inspection records, the Guardian's dagger messages, `circus.challenge.*`, `circus.finale.*`,
  `circus.school.passed` and the other school system lines, `court.champion.won` / `.buds`, `court.warden.turned_away`,
  `whisper.silenced`, `succession.rejection`, `chamber.guided.unavailable`.
- **Ending lines** `hemomancy.dialogue.ending.*` belong to the Vicar, Alchemist, Artificer and Mnemonist; check each
  folder took its own.

### Missing speakers and uncovered keys

- **The two speaking Saints.** Seraphae (12 `message.hemomancy.seraphae.voice.*` barks) and Velorum (4
  `message.hemomancy.velorum.line.*`) were not in this workflow. Their sheets are already person-first and short by
  design, but they need a read. Hemorath and Putriciel have narration only.
- **Nine Court A newcomer lines** (`dialogue.newcomer.court_gardener`, `_arbor`, `_lightkeeper`, `_tidewarden`,
  `_braced_elder`, `_edge_swimmer`, `_nursery_tender`, `_linesman`, `_lamp_colony`) carry the old tics and were never
  collected.
- **All 60 rapport teasers** (`dialogue.rapport.*.teaser`) and the 8 trade `.opens` lines were left out by every
  writer. Several are in the old voice ("The Guardian doesn't talk. Not to strangers.", "Some answers the Zealot only
  gives to people who've earned them."). They need one owner decision and one pass.
- **The Antecedent recordings** are frozen with their audio and stay out of scope.

### Reviewer verdicts

No reviewer returned "weak". Thirteen groups were "good". Court A and Court B were "acceptable": their reviewers rewrote
23 and 15 lines to break skeletons the writers had reproduced in new words (five `shunned.line1` refusals on "There is
nothing living on you", three `join.settled` lines on "You have changed houses once already"). Court A's per-role
metrics table in its comparison was not recomputed after review. Both groups get the joint pass in step 9.

## 6. Comparison docs

`tools/` is git-ignored (`/tools/*` in `.gitignore`), so these are local files on the author's machine and will not
resolve on another checkout.

- [Hermit](../../../tools/i18n/voice_rewrite/pass4/hermit.md) (applied)
- [Vicar](../../../tools/i18n/voice_rewrite/pass4/vicar/comparison.md)
- [Alchemist](../../../tools/i18n/voice_rewrite/pass4/alchemist/comparison.md)
- [Mnemonist](../../../tools/i18n/voice_rewrite/pass4/mnemonist/comparison.md)
- [Artificer](../../../tools/i18n/voice_rewrite/pass4/artificer/comparison.md)
- [Vein-Mason](../../../tools/i18n/voice_rewrite/pass4/vein_mason/comparison.md)
- [Sanguine Monolith](../../../tools/i18n/voice_rewrite/pass4/monolith/comparison.md)
- [Voyager and Wayfarer](../../../tools/i18n/voice_rewrite/pass4/voyager_wayfarer/comparison.md)
- [Zealot and Acolyte](../../../tools/i18n/voice_rewrite/pass4/zealot_acolyte/comparison.md)
- [Guardian and Scout](../../../tools/i18n/voice_rewrite/pass4/guardian_scout/comparison.md)
- [Our Lady and Annetta](../../../tools/i18n/voice_rewrite/pass4/lady_annetta/comparison.md)
- [Fungal Entity](../../../tools/i18n/voice_rewrite/pass4/fungal_entity/comparison.md)
- [Crimson Troupe](../../../tools/i18n/voice_rewrite/pass4/circus/comparison.md)
- [Court A](../../../tools/i18n/voice_rewrite/pass4/court_a/comparison.md)
- [Court B](../../../tools/i18n/voice_rewrite/pass4/court_b/comparison.md)
- [Successors and recruits](../../../tools/i18n/voice_rewrite/pass4/successors_recruits/comparison.md)
- The brief: [BRIEF.md](../../../tools/i18n/voice_rewrite/pass4/BRIEF.md); the scanner: `tools/i18n/voice_rewrite/pass4/scan.py`
