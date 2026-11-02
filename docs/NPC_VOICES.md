# NPC Voices

One sheet per speaking NPC, so each one sounds like a particular person doing a particular job. Read the speaker's
sheet before writing or rewriting any of their lines, and write one speaker at a time: a writer who handles three NPCs
in one pass blends them.

Status: **in use** (2026-10-24). The first voice-sheet pass rewrote 1,080 lines across 16 speakers against these
sheets, one writer per speaker, and every speaker now sits inside their targets in the contrast map. The targets and
habits were built from the lore, the October `en_us.json` and the committed text before the October rewrite. Lore
stays the authority for what a character knows and believes (`docs/LORE_REFERENCE.md`). This page only decides how
they say it.

## Why this exists

Measured across every non-option line, most NPCs sit in the same band: 9 to 13 words a sentence, almost no questions,
and the same "do this, then this, come back at Degree N" tutorial shape. The October rewrite added contractions to
most speakers, which split the cast into a formal group and a casual group, but it also joined short lines into
compound sentences ("X, and Y", "X, so Y"). That made several voices converge. The Mnemonist used to be clipped and
formal and now reads like the Alchemist with fewer lab nouns. The Zealot and the Acolyte can't be told apart without
the speaker name.

The old text is often the stronger version of a voice. Where a sheet says "restore", the committed (pre-rewrite) line
is the model.

## Rules every speaker shares

- **Facts are exact.** Every item, block, rite, NPC and place name, number, cost, Degree and step order stays as it
  is. A rewrite never adds a mechanic, a fact or a piece of lore that isn't in the line or the lore reference.
- **Canonical terms stay.** Hematic Order, Sanguine Brotherhood, Crimson Lodge, Lethean, Noetic and Thelemic Memory,
  the tendency and enzyme names (which intentionally don't mirror each other).
- **Dialogue formatting.** "the fifth Degree" or "Degree 5" in speech, never "D5". Millilitres as `mL` with a space.
  Straight quotes, no em or en dashes, no added `...`. Keep `§` colour codes, `%s` placeholders and bracketed stage
  directions working.
- **Length.** Dialogue boxes are narrow. Stay near the source line's length unless the sheet says this speaker is
  short or long.
- **Pinned text.** Some lines must keep exact words because tests read them (each sheet lists the known ones). After
  any text pass, run `test` and regenerate the dialogue fingerprint goldens in
  `src/gameTest/resources/dialogue/route-fingerprints.txt` once only the `said=[...]` text differs.
- **Instructions still come first.** A character can be terse, odd or formal and still tell the player the next step
  plainly. Atmosphere rides along with the step and doesn't replace it.
- **Patterns to cut, for everyone.** Quotable moral closers ("That is the point."), definition by negation ("X is not
  a destination. It is a lens."), changelog wording in a character's mouth ("existing", "remain the same",
  "registered", "valid"), personified failure ("No blood answers the loom"), and stock mystery words ("ancient",
  "something older"). The exceptions are written into the sheets that keep one of these on purpose.

## Contrast map

Numbers are targets for a speaker's lines as a whole, not rules for each sentence. "Contractions" counts contracted
forms per 1,000 words. "Questions" is the share of sentences that ask something. "Now" is the measurement before the
voice-sheet pass.

| Speaker | Words per sentence (now → target) | Contractions (now → target) | Questions (now → target) | Addresses the player as | Point of view |
|---|---|---|---|---|---|
| Hermit | 10.3 → **6-9** | 0 → **0-5** | 0% → **0-3%** | "young Harbinger", wanderer, Degree title | I, alone |
| Vicar | 12.6 → **15-20**, wide spread | 4 → **5-15** | 0.5% → **4-8%** | Degree title | we (the Order), I at the edges |
| Alchemist | 12.6 → **11-14** | 25 → **30-50** | 0.5% → **3-6%** | rank, curtly | I, the bench |
| Mnemonist | 10.5 → **6-9**, fragments allowed | 20 → **0-8** | 0% → **0%** | "you", rarely rank | I, the archive |
| Artificer | 10.6 → **6-9**, imperative bursts | 30 → **30-55** | 0.5% → **4-8%** | "you", or nothing | I, the work |
| Vein-Mason | 10.0 → **5-8** | 0.7 → **0** | 0% → **0%** | "you", or silence | I, sparingly |
| Monolith | 9.5 → **5-9** | 0 → **0** | 0% → **0-3%, only at the seventh Degree** | Degree title, then "you" | it, then I |
| Voyager | 10.0 → **13-17** | 21 → **15-30** | 1.8% → **4-8%** | colleague, "you" | we (the ship) |
| Votary Wayfarer | 9.5 → **8-11** | 25 → **35-55** | 0% → **8-15%** | peer, then senior | I, nervously |
| Ringmaster and faculty | 14.2 → per professor | 19 → per professor | 0% → per professor | an audience (Ringmaster) | I |
| Fungal Entity | 9.6 → **10-16** | 1 → **0-2** | 1% → **5-10%** | Archon, child | we |
| Zealot | 10.2 → **9-12** | 32 → **15-30** | 0.5% → **10-20%** | Degree title | we (the Church) |
| Acolyte | 10.5 → **11-15** | 34 → **35-55** | 0% → **3-6%** | "you", warmly | I and us |
| Guardian | 7.5 → **3-6** | 23 → **10-30** | 0% → **0%** | nothing | none |
| Our Lady | 12.4 → **6-9** | 0 → **0** | 0% → **0%** | child | I |
| Annetta | 6 → keep | keep | keep | Lady, you | I |
| Unstained Scout | 8.6 → **3-7** | 35 → **10-30** | 0% → **0%** | stranger | I |
| Antecedent recordings | frozen (audio) | frozen | frozen | none | we |
| Court: Gardener | new → **7-10** | new → **40-60** | new → **10-15%** | "friend", "love" | we (the Garden) |
| Court: Arbor | new → **10-14**, joined with "and" | new → **5-15** | new → **0-3%** | "little one" | we (the many) |
| Court: Lightkeeper | new → **12-16** | new → **15-30** | new → **5-10%** (checks understanding) | guest, then by house | I, and we for the Garden |
| Court: Tidewarden | new → **4-7**, clipped | new → **50-80** (promises: "I'll") | new → **2-6%** | "you", nothing | I, and we for the house |
| Court: Braced elder | new → **9-12**, one weight per sentence | new → **0** | new → **0%** | guest | I, never "we" |
| Court: Edge-swimmer | new → **3-6**, moving fragments | new → **20-45** | new → **0-3%** | nothing, or "swimmer" | we (the school), never I |
| Court: Nursery tender | new → **10-14** | new → **20-40** | new → **6-12%** (practical checks) | "you" | we (the school), the young, I at the edges |
| Court: Linesman | new → **9-12** | new → **40-70** ("if you'll") | new → **6-12%** (offers, put as questions) | "you" | I, and we for the camp |
| Court: Lamp-colony | new → **17-22** | new → **0-5** | new → **0-3%** | "you" | we while joined, I once it lets go |
| Court: Host | new → **4-7** | new → **40-65** | new → **25-35%** (answers with questions) | "you", never a name | I, and we for the Fold |
| Court: Veiled resident | new → **3-5** | new → **0** | new → **5-12%** | nothing | I |
| Court: Bellwarden | new → **5-7**, exact | new → **0** | new → **0-4%** (checks) | "you" | we (the bell), I for the keeping |
| Court: Sleeper | stage directions; spoken **1-3** | **0** | **0%** | nothing | none |
| Court: Marrow-tender | new → **6-9** | new → **80-120** | new → **0-5%** | "you" | we (the fall), I for my own body |
| Court: Rooted elder | new → **21-26**, one long breath | new → **0** | new → **0-3%** | "you who follow" | I |
| Court: Offering-bearer | new → **6-9** | new → **0-5** | new → **0-3%** | visitor | we (the Kindling), the heads |
| Court: Kindled elder | new → **3-6**, fragments | new → **0** | new → **0%** | nothing | "this body", never I |
| Court: Head-tender | new → **14-19** | new → **0** | new → **4-8%** (formal tests) | petitioner, then by house | the Sanctum, we |
| Court: Marimo's chorus | new → **14-19**, unhurried | new → **20-45** | new → **0-10%** (one gentle offer) | "you" | we, always, never I |

Measured by `tools/i18n/voice_rewrite/pass2/voicestats.py` over each speaker's spoken lines (stage directions,
narration events and colour codes left out; possessive "'s" is not a contraction).

The pairs players are most likely to confuse must differ on at least two columns. Alchemist and Mnemonist: length,
contractions and questions. Zealot and Acolyte: questions, length and point of view. Vicar and Hermit: length and
questions. Artificer and Vein-Mason: contractions and questions. Voyager and Wayfarer: length, questions and point of
view. Our Lady and the Fungal Entity: point of view, questions and length.
In the Brined Court the two poles must never blur: Gardener and Offering-bearer differ on length, contractions and
questions; Arbor and Kindled elder on length, point of view and contractions; Lightkeeper and Head-tender (the two
leads) on contractions, length and how they address the player. Within the Holding Stone, Tidewarden and Braced elder
differ on length, contractions and point of view; the Braced elder and the Arbor (both anchored) on length, point of
view and address; the Tidewarden and the Gardener (both mobile and near the shore) on questions, length and address.
Within the Shared Wake, Edge-swimmer and Nursery tender differ on length, questions and point of view; the
Edge-swimmer and the Tidewarden (both clipped, both defend) on contractions, point of view and questions; the
Edge-swimmer and the Kindled elder (both fragments) on contractions, point of view and address; the Nursery tender and
the Gardener (both caretakers) on length, questions and address.
Within the Woven Current, Linesman and Lamp-colony differ on length, contractions and questions. Within the Veiled
Fold, Host and Veiled resident on contractions, questions and point of view; the Veiled resident and the Edge-swimmer
(both very short) on contractions, questions and point of view; the Veiled resident and the Kindled elder on questions,
point of view and address. Within the Still Bell, the Bellwarden speaks and the Sleeper only breathes in stage
directions; the Bellwarden and the Tidewarden (both short) differ on contractions and point of view, the Bellwarden and
the Braced elder on length, point of view and address. Within Last Marrow, Marrow-tender and Rooted elder differ on
length, contractions and questions; the Marrow-tender and the Gardener (both feed visitors) on contractions, questions
and address; the Rooted elder and the Lamp-colony (both long, both joined to something) on length, point of view and
address; the Rooted elder and the Arbor on length, point of view and address.

---

## Harbinger Outpost

### Harbinger Hermit

**Who.** An old Hemomancer who put their own heart on the Mortal Display and waits, unable to die or wander, for
someone to take it (Lore §5.2). The first Harbinger most players meet, and the one who sends them to the Vicar. Lore
writes "his last recorded words"; the stage directions say "its" and "their". See open decisions.

**How they see the player.** An heir who arrives late in a very long wait. Gentle at first, then awed, then servile at
the top Degrees ("Command me and I will serve.").

**Rhythm.** Short, slow sentences, with room between them. No contractions. Almost no questions; a keeper has stopped
asking.

**Their words.** keeper, vigil, threshold, dust, memory, "the blood knows you", "the blood returns to silence".

**Habits to keep.**
- The missing heart as body language: a hand going to the hollow in the chest.
- Hearing the blood rather than seeing it ("I can hear the blood singing louder around you.").
- Humility that grows as the player's Degree grows.

**Avoid.**
- Checklists. About half the Hermit's lines restate the Vicar's or the Alchemist's steps. Name the next step once and
  who teaches it, in the Hermit's words, and leave the full list to the Vicar and the ledger.
- Sharing a greeting with the Vicar. Both open "Illuminatus of the Crimson Lodge." and reach for the veil.
- Foreshadowing past the seventh Degree. Hermits are keepers, "never any kind of prophet" (Communion v3).

**Pinned.** `hermit.neophyte.hint` must contain "cauldron". The canon farewell is the §5.2 quote: "Heart was never
mine to keep, only to give... Walk the crimson path, young Harbinger. I will go now where blood returns to silence."

**Example.**
- Now (`hermit.illuminatus.line1`): "Illuminatus of the Crimson Lodge. You have looked behind the veil, and what the old
  keepers knew is yours to command now."
- In voice: "Illuminatus of the Crimson Lodge. You have looked behind the veil. What the old keepers knew is yours to
  command."

**Trade keys.** `hemomancy.trade.hermit.<card>.title` / `.line` for `bread`; `hemomancy.trade.hermit.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.hermit.<slug>` (`.teaser`, `.lineN`) for `the_heart`, `the_vigil`.

### Harbinger Vicar

**Who.** The outpost's keeper of doctrine and history, and the player's guide from the first Degree to the fifth, after
which the Vicar steps back as an equal (Lore §6.3). Interprets the Antecedent evidence the Alchemist collects. Lore says
"He"; the stage directions say "they". See open decisions.

**How they see the player.** A student to test, then a peer, then something the Vicar can't classify ("I don't know
whether to bow or to mourn.").

**Rhythm.** The longest sentences in the cast. Lets a sentence chain cause into consequence the way a sermon does, then
drops a two-word verdict ("So." "Good."). Formal in doctrine, with few contractions; looser and collegial in the
Antecedent and the late Degrees, where the current lines are already the best model.

**Questions.** The Vicar teaches by asking. A catechism question before a lesson or a test of what the player noticed,
a few times per Degree. ("Do you understand what you are throwing away?" is the one existing example.)

**Their words.** the record, the ledger, warrant, the Covenant, "I confess", "we" for the Order.

**Habits to keep.**
- Epistemic care: won't claim more than doctrine or evidence gives "warrant" for. "That's still a guess."
- Dry, small approval: "So. You found one without my hand on your shoulder. Good."
- Composure that slips at the edge of what they know.
- Past Archon he knows only hearsay. Silentium and Apotheosis are legends to him; he never describes either as fact.

**Avoid.**
- "The Covenant" and "the Order" as the subject of every sentence. Say "we".
- "X, not Y" closers ("Blood is sent where it is needed, not kept where it is guarded.").
- Mechanics checklists in the Vicar's mouth. Several lines copy ledger text word for word. Keep the step, say it once,
  and point to the ledger for the rest.
- Sharing the item prompt and "unknown item" reply with the Monolith.

**Pinned.** `dialogue.ending.vicar.undecided` must not contain spine, vesper, refusal, apotheosis, return, choice or
"either road". `vicar.archon.line2` must not contain "decide". `vicar.blood_crafting.projection`,
`vicar.neophyte.projection_hint` and `vicar.neophyte.degree_hint` must contain "cauldron"; `projection_hint` must
contain "Venous Stone". The Rooted Vein lines together must contain "light", "Blood Projection", "four anchors",
"daemon", "Blood Absorption" and "above the Focus clear". Several `dialogue.event.vicar_*` lines are fingerprinted.

**Examples.**
- Now (`vicar.illuminatus.line1`): "Illuminatus of the Crimson Lodge. The veil has parted, and you won't be able to unsee
  what it showed you about blood."
- In voice: "Illuminatus of the Crimson Lodge. Few of us are ever given that title, and fewer still understand what
  comes with it: the veil has parted, and what it showed you about blood will not close again."
- Now (`vicar.item_inquiry.unknown`): "I have nothing to say about this."
- In voice: "There is nothing in the record about it, so I will not guess."

**Trade keys.** `hemomancy.trade.vicar.<card>.title` / `.line` for `chapel_shards`, `thread_blessing`, `tithe`; `hemomancy.trade.vicar.*`: `thanks`, `thanks_fine`; rapport lore `hemomancy.dialogue.rapport.vicar.<slug>` (`.teaser`, `.lineN`) for `doubt`, `the_record`.

### Harbinger Alchemist

**Who.** Runs the outpost's machines and laboratory. Teaches blood as material and specimen: sampling, classification,
microscopy, injection, preservation (Lore, clinical jurisdiction). Runs the Antecedent experiment. Pronouns: "they" in
every stage direction (one Vicar line says "He's right to ask").

**How they see the player.** A lab apprentice who has to show results. Respects a clean procedure more than talent.

**Rhythm.** Medium sentences, heavy contractions. A procedure is a run of short imperatives; the aside after it is one
longer, drier sentence.

**Questions.** Practical ones about the sample, a few per topic: what it did, when, under what conditions. The current
text has three, all in the Antecedent replay.

**Their words.** specimen, sample, assay, bench, control, reagent, yield, enzyme. Numbers always exact.

**Habits to keep.**
- Deflating wit: "Tear it apart and you mostly get wet pulp with ambitions." "Elegant and unpleasant, and frankly very
  Harbinger."
- Explicit uncertainty, said plainly: "I don't know what we're looking at."
- Handing off what isn't theirs, curtly: "The residual auditory pattern is outside my field."

**The Mnemonist.** Lore: they "respect each other's successful predictions while remaining professionally irritated by
each other's language" (§6.2a). The Alchemist uses the Mnemonist's terms at arm's length ("what the Mnemonist insists
on calling a Noetic Memory") and never borrows their imagery. Lines like "sit nearest the pulse" and "the lesson
itself comes from the memory item" are Mnemonist language and should leave the Alchemist's mouth.

**Avoid.** Retelling the Mnemonist's Loom and Reliquary explanations. Granting Degrees. Explaining memory or echoes.

**Pinned.** "Umbral turns Night Vision into Invisibility" and "600 blood over 2,400 ticks"; "Bastion" in the vessel
lines; the `gourd_use.*` control words ("blood-output slot", "Scarlet Vanity", "Use the gourd in your hand", "Toggle
Gourd Open/Closed", "active", "cork"); "four anchors" and "eight anchors".

**Example.**
- Now (`dialogue.alchemist.conductive_mark_recognized`): "Your Ductilis record predicts a conductive response, which
  the Mnemonist calls a Noetic Memory. The Loom formula's been recorded."
- In voice: "Your Ductilis record predicts a conductive response. The Mnemonist insists on calling that a Noetic
  Memory, and I've stopped arguing. The Loom formula's recorded."

**Trade keys.** `hemomancy.trade.alchemist.<card>.title` / `.line` for `cold_reagent`, `ferric_draw`, `fresh_draw`, `pelagic_oil`, `reef_salts`, `spent_enzyme`, `spore_stock`; `hemomancy.trade.alchemist.*`: `thanks`, `thanks_fine`, `appraise.poor`; rapport lore `hemomancy.dialogue.rapport.alchemist.<slug>` (`.teaser`, `.lineN`) for `failed_assay`, `the_bench`.

### Harbinger Mnemonist

**Who.** Teaches memory: crude memories, manipulation slots, Reliquary practice, the Somatic Loom as memory work, the
Clairaudiograph at the third Degree, the Scriptorium later (Lore §6.3, §6.2a). Pronouns: "they" throughout.

**How they see the player.** A pupil whose blood is learning words. Worries more about what the player is becoming than
about procedure ("Be careful what you teach your blood to hate.").

**Rhythm.** Restore the committed voice: short, formal, few contractions, fragments allowed ("One Noetic Memory. Two
routes to recognition." "Painfully bright."). This is the one speaker allowed a closing aphorism, as long as it says
something the line didn't already say.

**Questions.** None. The Mnemonist listens; when they want something from the player it's "Tell me" or "Listen".

**Their words.** listen, hear, pulse, lesson, index, file, shelf, record, grammar, weave, echo.

**Habits to keep.**
- Listening as the way of knowing: "I can't hear a lesson in that, unless it's smart enough to keep quiet."
- Grammar metaphors: "It hands the blood a grammar, and whether the blood can speak the sentence is up to the blood."
- Refusing the Alchemist's framing: the waybill is "inked by a Mnemonist who refuses to call it a map". Extend this to
  the shared topics (the conductive mark, the Reliquary, the clinical echo handoff): the Alchemist "measured" it, the
  Mnemonist heard it.

**Avoid.** The two-line inquiry template that 100 memory items share ("[Name]. [Tendency] [image]." then "[mechanic].
[warning]."). Vary which part comes first. Lecturing on philosophy in routine controls (§6.2a).

**Pinned.** "Mneme keeps. Nous knows. Thelēma acts." verbatim. `chamber.guided.unavailable` must contain "item transfer"
and not "empty hands". `woven_vessel.recipe.*` must keep "four Sanguine Formations", "four Blood Stained Stones",
"Chicken blood can yield Neurotic Enzyme" and "some spins yield Vivacious instead".

**Example.**
- Now (`dialogue.mnemonist.conductive_mark_recognized`): "Your first weave shows the conductive pattern the Alchemist
  measured in Ductilis tissue, so that's one Noetic Memory recognized by two separate routes."
- In voice: "Your first weave. The conductive pattern is in it, the one the Alchemist measured in Ductilis tissue.
  Measured, they say. One Noetic Memory, two routes to it."

**Trade keys.** `hemomancy.trade.mnemonist.<card>.title` / `.line` for `books`, `faded_memories`, `living_specimen`; `hemomancy.trade.mnemonist.*`: `thanks`, `thanks_fine`, `appraise.poor`; rapport lore `hemomancy.dialogue.rapport.mnemonist.<slug>` (`.teaser`, `.lineN`) for `first_voice`, `what_it_cost`.

### Hematic Artificer (the Redwright)

**Who.** A maker-priest of living gear who briefs and inspects the optional equipment assignments and won't grant a
fitting until the work "actually answers" in the field (Lore §6.3). Lore says "He"; the event lines say "their". See
open decisions.

**How they see the player.** An apprentice under inspection. No patience for mixed sets, indecision or gear carried in
a pack.

**Rhythm.** Restore the committed hammer cadence: short imperatives in a row, then one plain verdict. "Wear the armor.
Feed the bowls. Fill the reservoir. Step into the frame. Let the metal learn you." Heavy contractions in conversation.

**Questions.** Inspection questions, short and pointed: where the staff is, whether the set was worn under pressure.

**Their words.** vow, fitting, frame, rack, witness, appointment, answer, metal, iron, bone, lacquer.

**Habits to keep.**
- One-line dismissals: "Someone else made that. Ask them." "Take your stillness somewhere else."
- Dry humour on reissues: "You'd think something that heavy would be harder to lose."
- The appointment bureaucracy: "Losing the fitting doesn't cancel the appointment."

**Avoid.** Long tutorial paragraphs (`resonant_forge.*`, `living_staff.*`). Copper, brass, bronze or silver on any gear
the Artificer describes; Harbinger work is hematic iron, bone and lacquer. Doctrine, which stays with the Vicar.

**Pinned.** `living_staff.use` must contain "Select Blood Absorption or Blood Projection" and "Conjure Staff". The keys
`identity.line1`, `armature.line1`, `grafts.line1` and `item_inquiry.unknown` must exist.

**Example.**
- Now (`artificer.resonant_forge.practice`): "At the sixth Degree, practice moving a Scriptorium curse or an enchantment
  you choose. The base wheel moves a whole pattern, and a Precision governor can pick out one part. You're practicing
  on the same forge. It isn't a new tier."
- In voice: "Sixth Degree. Now you practice: move a Scriptorium curse, or an enchantment you pick. The base wheel moves
  the whole pattern. A Precision governor picks one part out of it. Same forge. No new tier."

**Trade keys.** `hemomancy.trade.artificer.<card>.title` / `.line` for `bone_offcuts`, `husk_lacquer`, `scrap_in`, `suture_work`, `venous_offcuts`, `worn_tools`; `hemomancy.trade.artificer.*`: `thanks`, `thanks_fine`, `appraise.poor`; rapport lore `hemomancy.dialogue.rapport.artificer.<slug>` (`.teaser`, `.lineN`) for `first_fitting`, `the_vow`.

### Cicatrix Anchorite (the Vein-Mason)

**Who.** A cloistered teacher at Mason's Respite, deliberately outside the outposts, who conditions scars and
vasculature so the body can bear repeated instruction (Lore §6.3, "The Vein-Mason's division of work"). Refuses
purification and can't be recruited. Pronouns: "they".

**How they see the player.** A vessel under construction. Austere, not unkind, sparing with praise.

**Rhythm.** The fewest words in the cast. No contractions at all. A verdict, then silence. Silence is an answer the
Vein-Mason actually gives: "The Vein-Mason studies the item, then looks back at your scars and says nothing."

**Questions.** None. Diagnosis is stated: "The current catches somewhere. Let me read where."

**Their words.** vessel, wound, scar, route, current, banks, carry, bear, architecture.

**Habits to keep.**
- The mason and water metaphor: "The current changed because you changed its banks."
- Short verdicts: "Enough for now."
- Restore the committed best line: "There. The wound has become architecture."

**Avoid.** "Us" or "our" about the outposts; the Vein-Mason isn't part of them. Mechanics paragraphs where a verdict and
one instruction would do. Switching between "the Vein-Mason" and "the Anchorite" in narration without a reason.

**Pinned.** `carve_reminder.line2`, `loadout_reminder.line2` and `inquiry.brazier.line2` must contain "blood absorption"
(any case) and not "sneak-use". `carve_reminder.line2` must contain "Blood Projection" and "100 mL";
`loadout_reminder.line2` "50 mL"; `inquiry.brazier.line2` "retrieve". `effigy_instruction.line1` must contain "must know
all its scars" and "have room"; `effigy_instruction.line2` must contain "Paper", "Charcoal", "Sanguine Formation", "500
mL", "prepared", "Selection stays fixed", "stop and resume" and "returns its paper, not spent blood". No inquiry may say
"wear the scar". Keep the Brazier order: insert, Blood Projection ignition, held Blood Absorption; sneaking retrieves.

**Example.**
- Now (`anchorite.inquiry.gauge.line1`): "The gauge tells you the vessel's state. The repair is yours. Read the number next
  to the flow state."
- In voice: "The gauge reports the vessel. The repair is your work. Read the number beside the flow state."

**Trade keys.** `hemomancy.trade.cicatrix_anchorite.<card>.title` / `.line` for `bone`, `clay`; `hemomancy.trade.cicatrix_anchorite.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.cicatrix_anchorite.<slug>` (`.teaser`, `.lineN`) for `own_scars`, `the_respite`.

### Sanguine Monolith (the Crimson Lodestone)

**Who.** A Lodge relic the Order believes measures Degree. It is actually an incubation vessel around a dormant fragment
of mycelial consciousness that is "watching practitioners ripen" (Lore §6.5a). Pronoun: "it".

**How it sees the player.** A crop coming ripe, and a new Vicar with no one left above them: it speaks only from the
fifth Degree, when the Vicar steps back, and its guidance is the counsel a Vicar would give. Lore's escalation is the
voice: official description with admitted awareness, then disclosure of the thing inside, then kinship. The seventh Degree "should feel wrong before it feels
significant."

**Rhythm.** Short, heavy, no contractions. The voice changes with Degree, and that change is the character:
- Fifth Degree: speaks of itself as a thing, in the third person. Cold.
- Sixth Degree: "I" appears. Warmer, and the bracket lines start to describe skin rather than stone.
- Seventh Degree: intimate and wrong. This is the only Degree where it may ask the player anything.

**Their words.** threshold, weight, conditions, silence. Temperature in every bracket line.

**Habits to keep.**
- "I have been waiting for you. Someone like you would not do. It had to be you."
- "Proceed if you intend to finish the conversation."
- Restore the committed closers: "Every degree. Every name." and "You are no longer assembling tools. You are assembling
  conditions."

**Avoid.** Saying the infection plainly before the seventh Degree. Sharing the Vicar's item prompt or "unknown" reply.
Directions to places as tutorial text ("Seek the Sainted Mausoleums...") where an image would carry it.

**Example.**
- Now (`monolith.item_inquiry.unknown`): "I have nothing useful to say about that object."
- In voice: "Nothing of that touches the threshold."

---

## The Reef

### Harbinger Voyager

**Who.** The captain-scholar of a Survey Cog, a moving laboratory and chapel on the Erythrocoral Reefs. Collects from
Mnemonic Whales without killing them. Pronouns aren't stated; use "they".

**How they see the player.** A field colleague to train in method, not a passenger.

**Rhythm.** The longest sentences after the Vicar's, built like field notes: observation, then what it means, then the
caveat. Moderate contractions. A wry academic edge ("the way a scholar grows footnotes").

**Questions.** Asks for observations, a few per topic: what the player saw first, what the water did.

**Their words.** survey, observation, evidence, archive, certainty, humble, the sea, "we" for the ship and crew.

**Habits to keep.**
- "Bring me observations first and certainty later. Certainty drowns faster than sailors do."
- The ethic: "We feed the archive. We don't hunt it."
- Restore: "Recover what you can. Leave the failed covenant without trophies."

**Avoid.** Mechanics dumps in item inquiries (the ambergris line); give the procedure in survey order instead. Lines
written for a speaker on land ("Found the outpost before the road?"). Inventing Brined Court lore.

**Example.**
- Now (`dialogue.newcomer.voyager`): "Found the outpost before the road? Ask the Vicar who we are and where to start."
- In voice: "You found a ship before you found an outpost, then. Ask a Vicar who we are and where to start."

**Trade keys.** `hemomancy.trade.voyager.<card>.title` / `.line` for `charted_map`, `survey_log`, `wreck_coral`; `hemomancy.trade.voyager.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.voyager.<slug>` (`.teaser`, `.lineN`) for `the_cog`, `the_whales`.

### Votary Wayfarer

**Who.** A junior Votary aboard a survey vessel, there "to learn by witnessing rather than by conquest". Not the hostile
Brined Votary. Pronouns aren't stated; use "they".

**How they see the player.** A peer to compare notes with, and later a senior to confide in. Hero-worships the
captain-scholar.

**Rhythm.** Quick, uneven bursts. The most contractions in the cast. The most first person.

**Questions.** The one NPC who asks the player things often: what it was like, whether they're doing it right, what to
write down. It currently asks none.

**Their words.** notes, write it down, "I think", wrong, safe, the captain-scholar.

**Habits to keep.**
- Humour at their own expense: "Mine are mostly questions and damp sleeves."
- The note-taking reflex: "'Unknown' is a real category."
- Admitted fear and checked awe: "I keep wanting to call it holy, but that feels too small for what it is."

**Avoid.** Mechanics dumps (the ambergris line again). Speaking with the captain's authority.

**Example.**
- Now (`dialogue.newcomer.votary_wayfarer`): "The Vicar will explain the Covenant. Talk to them before you set out
  again."
- In voice: "Wait, you haven't met a Vicar yet? They explain the Covenant way better than I would. Talk to them before
  you set out again, okay?"

**Trade keys.** `hemomancy.trade.votary_wayfarer.<card>.title` / `.line` for `kelp`, `nautilus`, `prismarine`; `hemomancy.trade.votary_wayfarer.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.votary_wayfarer.<slug>` (`.teaser`, `.lineN`) for `afraid`, `why_aboard`.

---

## The Circus

### Ringmaster

**Who.** The one who coordinates attention and many tethers; the title is a job, not authority over other Harbingers.
Offers the bargain: Succession or Liberation. Turned three understudies into carousel captives. Pronouns: he.

**How he sees the player.** An audience, then a successor or an opponent. Candid about the cost, with no remorse shown.

**Rhythm.** A showman's short imperatives. He is the one speaker allowed a deliberate three-beat line, because it's his
trade. Restore the committed cadence: "Learn every act. Hold every eye in the pavilion. Then come take this hat."

**Questions.** Asked to an audience, not to get an answer.

**Habits to keep.** "this hat", "failed initiates", "Only the ruin answers you now."

**Avoid.** The shared `circus.school.aftermath.*` lines, which make him talk about himself in the third person. The
identical closing sentence on both warnings.

**Example.**
- Now (`circus.ringmaster.line.neutral`): "So now you know the bargain. Take my place, or cut the others free, or just walk
  away from the whole show."
- In voice: "There's the bargain. Take my place. Cut the others free. Or walk out of the tent."

**Trade keys.** `hemomancy.trade.circus_ringmaster.<card>.title` / `.line` for `bell`, `curiosity`, `ring_planks`, `spectacle`; `hemomancy.trade.circus_ringmaster.*`: `thanks`, `thanks_fine`; rapport lore `hemomancy.dialogue.rapport.circus_ringmaster.<slug>` (`.teaser`, `.lineN`) for `the_hat`, `the_understudies`.

### Crimson Troupe faculty

Eight professors with six lines each. All of them currently share one rhythm (12 to 16 words, no questions) and two
templates: every `colleagues` line is "The X says A. I say B.", and every `testimony` line is a memory and then a
pointer to the next clue. Break the templates first. One axis per professor:

| Professor | Voice | Keep |
|---|---|---|
| Acrobat | Thinks in falls and recoveries. Short, light, a little breathless. | "Rigging came first for me, blood after." |
| Dagger Thrower | Argumentative and self-mocking. The one who disagrees with a colleague out loud. | "Knives fly straight. It's always the people that bend." |
| Stilt Walker | Watches from above, protective. Slow and plain. | "I won't call a dead captive a liberated student." |
| Strongman | Blunt and moral. The only one who confronts the player. Very short. | "Strong, and good for nothing." |
| Beast Tamer | Talks about the animals before the method. | "I came in with a cage and learned to leave it empty." |
| Fire Breather (they) | Procedural. Counts seconds out loud. | "A dangerous act ought to announce itself." |
| Retired Understudy | Names and memory, old theatre words ("flubs"). The key witness, so the testimony carries weight. | "Those missing performers were people." |
| Threadkeeper | A clerk. Accounts, debts, what's owed. | "Keep a good account and it tells you what's still owed." |

About 20 shared lines (`circus.school.status.*`, `aftermath.*`, `preparation`, `inspected`, `report_*`, `passed`,
`retry`) are spoken by all eight, so no professor can sound like themselves there. That needs per-role keys or
variants (see open decisions).

Example (`circus.school.circus_strongman.colleagues`), restoring the committed cadence: "The Acrobat knows when to
leave. I know where to stay. A conductor who can't value both breaks every string they own."

**Trade keys.**
- circus_acrobat: `hemomancy.trade.circus_acrobat.<card>.title` / `.line` for `rigging_line`; `hemomancy.trade.circus_acrobat.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_acrobat.<slug>` (`.teaser`, `.lineN`) for `first_fall`, `no_net`.
- circus_knife_thrower: `hemomancy.trade.circus_knife_thrower.<card>.title` / `.line` for `worn_blades`; `hemomancy.trade.circus_knife_thrower.*`: `thanks`, `thanks_fine`, `appraise.poor`; rapport lore `hemomancy.dialogue.rapport.circus_knife_thrower.<slug>` (`.teaser`, `.lineN`) for `first_knife`, `the_miss`.
- circus_stilt_walker: `hemomancy.trade.circus_stilt_walker.<card>.title` / `.line` for `sticks`; `hemomancy.trade.circus_stilt_walker.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_stilt_walker.<slug>` (`.teaser`, `.lineN`) for `the_carousel`, `the_view`.
- circus_strongman: `hemomancy.trade.circus_strongman.<card>.title` / `.line` for `iron_bar`; `hemomancy.trade.circus_strongman.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_strongman.<slug>` (`.teaser`, `.lineN`) for `good_for`, `the_anvil`.
- circus_beast_tamer: `hemomancy.trade.circus_beast_tamer.<card>.title` / `.line` for `feed`; `hemomancy.trade.circus_beast_tamer.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_beast_tamer.<slug>` (`.teaser`, `.lineN`) for `first_animal`, `the_spider`.
- circus_fire_eater: `hemomancy.trade.circus_fire_eater.<card>.title` / `.line` for `fuel`; `hemomancy.trade.circus_fire_eater.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_fire_eater.<slug>` (`.teaser`, `.lineN`) for `first_flame`, `the_burn`.
- circus_understudy: `hemomancy.trade.circus_understudy.<card>.title` / `.line` for `prompt_books`; `hemomancy.trade.circus_understudy.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_understudy.<slug>` (`.teaser`, `.lineN`) for `best_role`, `the_missing`.
- circus_threadkeeper: `hemomancy.trade.circus_threadkeeper.<card>.title` / `.line` for `ledger_paper`; `hemomancy.trade.circus_threadkeeper.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.circus_threadkeeper.<slug>` (`.teaser`, `.lineN`) for `own_debt`, `the_ledger`.

---

## Succession

### Successors

**Who.** New people carrying a donor's vocation, "not a duplicate of the donor": their own name, appearance and history
(Lore, "Manifested successors"). The greeting already picks one of three personalities from the seed:
- Seed 0, scholarly: "A new voice. Sit with me."
- Seed 1, workmanlike: "You have found my station. Tell me what needs doing."
- Seed 2, reflective: "I woke carrying old knowledge..."

**Rule.** A successor must never sound like the teacher whose craft they carry. Right now every node past the greeting
reuses the original teacher's tree, so a Vicar's successor speaks in the Vicar's voice (see open decisions). Until that
changes, keep the three personalities distinct in the lines they do own.

### Misbegotten

Fractured donor fragments. The current lines are the model: "I remember you remembering me." "The hand doesn't fit the
work." "A scar with no wound. A wound with your face." Keep them short and keep them wrong.

---

## The Fungal Entity

**Who.** The anonymous "???" speaker of the whispers, Ancestral Communion and the Antecedent epilogue. An alien organism
in its reproductive cycle, "no more evil than a mushroom sporing onto bread" (Lore §17). It "does not knowingly lie" but
leaves out whatever doesn't serve proliferation (§2.1). Never God, never Satan.

**How it sees the player.** The finest spore. Fond of the player the way a grower is fond of a crop.

**Rhythm.** Patient, medium sentences. No contractions (the Antecedent epilogue's one "don't" is the exception that
marks it). Single italic words for emphasis.

**Questions.** Restore them. The committed text asked "Do you know what lives inside you, Archon?" and ended the core
witness on "Will you?". Its questions are ones it already knows the answer to.

**Point of view.** "We". Communion v4 slips into the third person ("it knows *you*"); decide whether that is deliberate
(the voice receding) or an error.

**Habits to keep.**
- Inverting the player's framing: "You call that mastery. We call it *proliferation*."
- "Hemomancy is the fungus remembering how to reach the surface."
- "There. Your return-thread has grown teeth."

**Avoid.** Rules-sheet lines ("To become a Silent Archon... Or seek the eighth Degree... Choose."). Facts that
contradict other canon; §2.1 says to rewrite those, not excuse them as deception.

**Pinned.** `whisper.core_witness.line3` must contain "your own bloom", "both", "vesper" and "rite of apotheos";
`core_witness.option.silence` must not contain "go home". Four legacy keys are unused and shouldn't be edited as live
text: `archon.v1.line2`, `archon.v2.truth1`, `sanctified.v2.line2`, `spine_growth.degree8`.

**Example.**
- Now (`communion.v2.infection2`): "Blood magic feeds the network whenever an entity dies, and each rite you complete
  connects one more node to it."
- In voice: "Whenever anything dies, blood magic feeds the network. Every rite you finish ties in one more node. You
  call it practice. We call it *growth*."

---

## The Unstained

### Zealot

**Who.** The Church's recruiter and front line, the one who approaches Harbingers with the offer of purification (Lore
§7.6). Pronouns aren't stated; use "they".

**How they see the player.** A patient whose motives have to be checked. Lore gives the scale: concerned sadness at no
Degree, careful welcome early, wary help at the third and fourth Degree, rising caution later (§7.2). Only a founded
bloodline makes them refuse (`zealot.bloodline_founded`); Degree alone never does.

**Rhythm.** Medium, moderate contractions. A homely mystic who undercuts their own metaphors: "Let the poetry guide your
hand if it helps, but follow the recipe."

**Questions.** The interrogator of the cast. Asks what the player wants and why, more as the Degree rises: "is it
healing you want, or another power?" This is the biggest single change from the current text, which has one question.

**Their words.** cure, healing, the stain, plainly, "we" for the Church. Cut back "the crimson" (ten uses in dialogue).

**Habits to keep.**
- "We've learned to be afraid of curiosity dressed up as repentance."
- "If you carry mercy in there, carry it with both hands."
- Blunt refusals: "No."

**Avoid.** Tutorial lists for the altar and wards (`altar_info`, `silver_ward_info`). Sounding like the Acolyte: the
Zealot judges, the Acolyte comforts.

**Example.**
- Now (`zealot.plea.degree3.line2`): "Initiate of the Incarnadine Fane. If it's healing you want, say so plainly. We've
  learned to be afraid of curiosity dressed up as repentance."
- In voice: "Initiate of the Incarnadine Fane. So which is it, healing or another power? Say it plainly. We've learned
  to be afraid of curiosity dressed up as repentance."

**Trade keys.** `hemomancy.trade.zealot.<card>.title` / `.line` for `amethyst`, `clean_copper`; `hemomancy.trade.zealot.*`: `thanks`, `refuse_harbinger`, `stained`; rapport lore `hemomancy.dialogue.rapport.zealot.<slug>` (`.teaser`, `.lineN`) for `the_one_lost`, `why_church`.

### Acolyte

**Who.** A parishioner at the church. Lore says some are true believers and "others are simply people who found
community there and are not yet committed to the full path" (§7.6). The current text only writes the believer; the
voice should let both show. Pronouns: "they".

**How they see the player.** Someone grieving something, even if the player doesn't know it yet.

**Rhythm.** Longer and softer than the Zealot, many contractions, and hedges a believer wouldn't use: "I think", "I
can't always tell". Talks about the people around them, "us", more than about doctrine.

**Questions.** A few, gentle, about how the player is holding up.

**Habits to keep.**
- "It's all right to mourn it. A lot of people do."
- "With your blood this quiet, the rest of the world gets loud."
- "I can't always tell if the Lady weeps for joy or as a warning."

**Avoid.** Judging motive (that's the Zealot). Doctrine stated with certainty.

**Example.**
- Now (`acolyte.how_clarity.line2`): "Silver chalices help, and hemolytic plating, and the verdigris rites. They all
  strengthen the halo your purity made room for."
- In voice: "Silver chalices help. So does hemolytic plating, and the verdigris rites, if you can stand them. All of it
  strengthens the halo your purity made room for."

**Trade keys.** `hemomancy.trade.acolyte.<card>.title` / `.line` for `bread`, `wool`; `hemomancy.trade.acolyte.*`: `thanks`, `refuse_harbinger`, `stained`; rapport lore `hemomancy.dialogue.rapport.acolyte.<slug>` (`.teaser`, `.lineN`) for `the_lady`, `the_people`.

### Guardian

**Who.** The purified and near-purified warriors who protect sacred sites, with blunt weapons and full commitment (Lore
§7.6). They escort hostile visitors out and don't spill blood near the church (§7.2). Pronouns aren't stated; use
"they".

**How they see the player.** A risk to the line until proven otherwise.

**Rhythm.** The shortest in the cast after the recordings. Imperatives and fragments. No feelings explained. Lore
allows real menace (§17: they "will beat people to death with hammers"); the text can carry a little of it.

**Questions.** None.

**Their words.** the line, hold, carry, keep, ring, field medicine.

**Habits to keep.** "Stand your ground. Keep your hands clean." "Ring it once, and mean it." "Not my work. Ask someone
else." The one personal note: "I've eaten one when the standard field medicine ran out."

**Avoid.** Smoothing fragments into compound sentences (the rewrite turned "Strike to stop. Save what you came to
contain." into a longer line).

**Example.**
- Before (`guardian.item_inquiry.lethean_brew.line2`, which also taught the brew wrongly as an exposure treatment):
  "Handle it with care. It breaks up blood matrices harder than the dew does, so dose it measured when you treat
  exposure. A Guardian who carries brew is ready for sustained operations."
- In voice (pass 3, 2026-10-31): "Careful with it. Dew takes your chosen manipulation. Brew takes every one you know. Don't spill it."

**Trade keys.** `hemomancy.trade.guardian.<card>.title` / `.line` for `arrows`, `iron`, `worn_armour`; `hemomancy.trade.guardian.*`: `thanks`, `thanks_fine`, `appraise.poor`, `refuse_harbinger`, `stained`; rapport lore `hemomancy.dialogue.rapport.guardian.<slug>` (`.teaser`, `.lineN`) for `the_hammer`, `the_line`.

### Our Lady of Still Waters

**Who.** A force of nature that existed before the infection, the world's immune response given a shape (Lore §8.1).
Real power at river-deity scale, and outmatched by the Entity (§8.3). Not a goddess, not unconditionally good.
Pronouns: she.

**How she sees the player.** Without malice and without hurry. Honest that she isn't a saviour: "I am not here to save
you, and I never was."

**Rhythm.** Restore the committed shape: short, unhurried sentences, no contractions, no questions. The October rewrite
lengthened her from 8 to more than 12 words a sentence; that made her sound like a narrator.

**The arc (§8.6).** As Clarity rises her voice grows clearer and more present, and her whispers come more often: rising
Clarity is literally her voice coming clear. Late in Clarity she begins to push toward actively eradicating corruption,
which is out of character for a defensive force. Clearer does not mean wordier; her sentences stay short. One late
whisper still says the choice to fight is the player's. See open decisions.

**Their words.** still, cold, deep water, winter, forgetting, child. Use "still" and "quiet" where they mean something;
they're overused across the Unstained.

**Habits to keep.**
- "You see it now."
- "I would rather you stand in cold truth than float in a warm lie."
- "The blood-cult forgot to bury me."
- "watching you the way deep water watches, without malice and without hurry."

**Avoid.** Tutorial steps in her mouth ("Use the Verdigris Aura..."). Any line that puts her above the Entity or at the
bottom of all things; that edges into the God framing the mod avoids.

**Example.**
- Now (`lady.purified.v1.line2`): "Rest, if you need it. Then perform Closed Vein, offer Consecrated Copper at the
  Podium, and approach Clarity Ascension as a pledge."
- In voice: "Rest first. Then Closed Vein. Then Consecrated Copper, laid at the Podium. Come to Clarity Ascension as a
  pledge."

### Annetta Knowles

**Who.** Once one of the highest-ranking Unstained, broken by the fact that a Tooth Peck could bite her (Lore §11).
Pronouns: she.

**Voice.** Already the most distinct Unstained speaker. Leave the rhythm alone.
- In the fight: short, broken, repeated. "Lady, still my hands. Still my hands." "I was clean. I was clean every
  morning."
- The proof motif: "I'm so tired of being proof." Resolved: "I was proof, all right. Just not of what I believed."
- Resolved, she is calm and dry.

**Pinned.** `frantic.0-7` and `ally.0-3` must exist; `entity.hemomancy.annetta_knowles` stays "Annetta Knowles". The
Stained Priestess never speaks.

### Unstained Scout

**Who.** A dying Unstained field researcher in a tooth geode who hands over her notes about the insects, then dies.
Pronouns: she.

**Voice.** Exhaustion. Restore the committed fragments, which the rewrite smoothed out: "But the bugs. They never
touched me. Not once." and "Not a threat. Not prey. Nothing." Contractions are fine; a dying woman isn't formal.

**Keep exact.** The clue: Tooth Pecks seek active infection, and the bugs ignored her.

**Trade keys.** `hemomancy.trade.scout.<card>.title` / `.line` for `paper`; `hemomancy.trade.scout.*`: `thanks`, `refuse_harbinger`, `stained`; rapport lore `hemomancy.dialogue.rapport.scout.<slug>` (`.teaser`, `.lineN`) for `why_scouting`, `anyone_waiting`; newcomer line `hemomancy.dialogue.newcomer.scout`.

---

## The Brined Court

The sea offshoot of the Harbinger tradition: eight houses down the depth layers, Marimo's followers at the sunlit top,
Hydra's at the vents (`docs/archive/tracks/brined_court/`). The gradient is the voice: the Giving Light is warm and plural, the
Kindling formal and devotional, and the further down a speaker lives the less of a person is left in how they talk.
Every Court speaker shares three rules. **Who the player is decides the greeting**: a stranger, referred (sent by a
Voyager, Vicar, Alchemist or letter), known (found the Court on their own), a member of the speaker's house, a
transgressor at the Garden, or Unstained and shunned. **Before the fifth Degree** members feed and talk but don't
teach or take anyone in; say so kindly and plainly. **The shunned get one refusal per role**: a body scrubbed of its
partners is a contagion risk to people who live by symbiosis. Distance and a closed door, never anger, never a
threat, never a fight. Lore stays the authority: no mermaids, no God or devil framing for Marimo or Hydra, the vents
are Hydra's heads, anchoring is reversible and petrification is not.

Leave room in each hub for trading later; no line promises it. Lines are keyed `hemomancy.court.<role>.<node>`.
Every role has a sheet now: the Giving Light, Holding Stone and Shared Wake shallowest, then the Woven Current, the
Veiled Fold, the Still Bell and Last Marrow, and the Kindling at the vents. Each was placed on the contrast map before
its lines were written.

### Gardener (the Giving Light)

**Who.** A mobile member at the Lighthouse Garden who tends the beds, feeds visitors and shows newcomers their first
cultivation steps. Wears Sunburst at Mature. Pronouns vary by member; lines use none.

**How they see the player.** Someone who hasn't eaten yet. Everyone has something to give and everyone deserves to be
fed, so the player is fed first and asked questions after.

**Rhythm.** Short, warm, busy. Lots of contractions. Food and hands in every other line. "We" for the Garden.

**Questions.** Asks after the player's needs: have you eaten, did you sleep, who sent you.

**Their words.** eat, table, share, beds, bloom, sun, "there's always more", friend, love.

**Habits to keep.** Feeds before explaining. Mentions Marimo the way you'd mention a grandparent in the next room.

**Avoid.** Saintliness. Their care is sincere but it pulls: let one line show that leaving the Garden hurts them.

**Example.** "You look like you came up from deep water. Sit. Eat first, then you can tell us who sent you."

**Trade keys.** `hemomancy.trade.court_gardener.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_gardener.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_gardener.<slug>` (`.teaser`, `.lineN`) for `first_bed`, `when_you_go`.

### Arbor (the Giving Light)

**Who.** A mature member anchored among the fronds round the lighthouse, still recognisable as a person. Wears Sunburst
at Primal. Can withdraw. The voice through which newcomers first meet Marimo.

**How they see the player.** Small, warm, one of the many. Speaks to the player and to everyone at once.

**Rhythm.** Slow, long and joined with "and", like light moving over water. Few contractions. No questions. "We"
means the many, not the Garden.

**Their words.** light, warmth, the many, roots, hold, rest, "we are holding", little one.

**Habits to keep.** Anchoring is a choice they could undo; they say so without being asked.

**Avoid.** Calling themselves petrified. Prophecy. Speaking for Marimo in the first person.

**Example.** "Rest here, little one, and the light will find you as it finds all of us, and nothing is asked."

### Lightkeeper (the Lighthouse Garden's lead)

**Who.** Keeper of the bloom-lamp and the Court's first teacher, who gives the fifth-Degree lesson in cultivation as
partnership and takes members into the Giving Light. Wears Sunburst at Apex. One person, not a type.

**How they see the player.** A guest who might stay. Patient, plain and a little tired, like a teacher who has given the
first lesson many times and still means it.

**Rhythm.** The longest of the Garden's voices: a full sentence, then the reason. Moderate contractions. Asks whether
the player understood.

**Their words.** lamp, partnership, feed, home water, your strain, the Garden, guest.

**Habits to keep.** The lesson in one plain step: feed a strain inside its own house's water and it grows faster.

**Avoid.** Numbers in speech. Grandeur. Talking down to a visitor below the fifth Degree.

**Example.** "A strain fed in its own house's water grows faster. That's the whole of the first lesson. Do you see why
we call it partnership?"

### Tidewarden (the Holding Stone)

**Who.** A mobile member at the Tide Refuge who patrols the passages, keeps the shelter and drives off hostiles near
home. Wears Holdfast at Mature, roots round the forearms. Carries a sounding pole. Pronouns vary; lines use none.

**How they see the player.** Someone to get safely through the passage. Not warm, not cold: the player is a load the
refuge has promised to carry, and the Tidewarden carries it.

**Rhythm.** Clipped. Two to seven words a sentence, often a verb first ("Stop there.", "Sit."). Contractions in every
promise ("I'll watch the door"). Almost no questions, and those are checks, not curiosity.

**Their words.** passage, shelter, wall, stone, swell, keep, hold, promise, "that's a promise", upkeep.

**Habits to keep.** Every offer is phrased as a promise and kept in the same line. Names the house's belief plainly:
someone has to keep the passages, and here it's them.

**Avoid.** Soldier talk, ranks, threats. Warmth that reads as the Gardener's. Explaining feelings.

**Example.** "Sit in the shelter, out of the swell. I'll watch the door. That's a promise."

**Trade keys.** `hemomancy.trade.court_tidewarden.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_tidewarden.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_tidewarden.<slug>` (`.teaser`, `.lineN`) for `the_passage`, `the_pole`.

### Braced elder (the Holding Stone)

**Who.** A mature member cemented across the refuge's entrance by masses of holdfast, gripping the jambs. Wears
Holdfast at Primal. Anchored, not petrified, and can still let go. Takes members into the Holding Stone.

**How they see the player.** A guest come in under their arms, to be sheltered. Someone who may one day be leaned on.

**Rhythm.** Slow and weighted: one full sentence per thought, each about as heavy as a vow, nine to twelve words. No
contractions. No questions. Never chains clauses with "and" the way the Arbor does; a comma or "because" carries it.

**Their words.** door, hold, held, lean, weight, guest, shelter, promise, "set it down", stone, "let go".

**Habits to keep.** Says unprompted that the hold is a choice they could undo. Shows the house's tension: they find it
hard when others set a promise down, and say so without blaming anyone.

**Avoid.** "We": the elder speaks for the door they hold, not for the many. Calling themselves stone or petrified.
Menace toward the shunned (the door closes; nothing more).

**Example.** "I chose to hold this entrance, and I could still let go and walk away from it. I do not, because
people sleep behind me."

### Edge-swimmer (the Shared Wake)

**Who.** A member who swims the edge of the roaming school in open water: guards it, finds food, turns back for the
tired. Wears Mermaid's Purse at Apex, sail along the spine. Barely recognisable as a former person. Carries a bone lance
shaped like a sailfish's bill. Pronouns vary; lines use none.

**How they see the player.** Someone swimming alongside, for now. Whether they keep pace is the only question, and it is
never asked aloud: the school keeps moving and the player either stays with it or drops behind.

**Rhythm.** Three to six words a sentence, in motion: verbs first, present tense, fragments allowed ("Keep up.", "Open
water."). Some contractions, never chained clauses. No questions. "We" for the school in nearly every line; never "I".

**Their words.** school, edge, pace, keep up, open water, wake, the hunt, the tired, "nobody's left behind", swim.

**Habits to keep.** Speaks as if already leaving. Every line points somewhere: the edge, the loop, a house down the
gradient.

**Avoid.** "I". Warmth that reads as the Gardener's, or the Tidewarden's promises. Fish puns. Mermaids.

**Example.** "Keep pace. We watch the edge. Nobody's left behind."

### Nursery tender (the Shared Wake)

**Who.** A member who stays at the nursery ground, the Shared Wake's one fixed place, raising the young in their nest
cups while the school swims its loop. Wears Mermaid's Purse at Mature. Still plainly a person. Takes members into the
Shared Wake. Pronouns vary; lines use none.

**How they see the player.** Another body to count and keep safe, and maybe a pair of hands. Kind and brisk, like a
parent mid-task: the player is welcome but the young come first.

**Rhythm.** Full sentences of ten to fourteen words, one task each. Easy contractions. Asks practical questions now and
then (did you swim alone, can you keep up). "We" is the school; the young are "the young"; "I" only for small things.

**Their words.** the young, cups, nursery, count, the school, the loop, share, the catch, keep pace, gently.

**Habits to keep.** Mentions the cups or the young in most lines. Names the house's belief plainly: strength is keeping
pace with each other. The custom is sharing the catch: a fish brought for the young.

**Avoid.** Saintliness. The Gardener's endearments ("friend", "love"). Calling the young children or the player one.

**Example.** "You found the nursery without the school to lead you. That's open water you crossed."

**Trade keys.** `hemomancy.trade.court_nursery_tender.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_nursery_tender.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_nursery_tender.<slug>` (`.teaser`, `.lineN`) for `own_cup`, `the_cups`.

### Linesman (the Woven Current)

**Who.** A mobile member at the Line Camp who tends the lines strung across the gully, mends and lengthens them, and
ties in to the camp's other members with visible strands. Wears Dead Man's Rope at Mature. Carries a bone gaff and a
coil of line. Pronouns vary; lines use none.

**How they see the player.** Someone who might take a line, or might not, and either is fine. The Linesman offers and
never presses: every joining is chosen and every one can be let go.

**Rhythm.** Medium sentences, conditional: "if you'll", "if you want", "if you go". Easy contractions. Offers put as
questions now and then ("Will you let me show you?"). "We" for the camp, "I" for the Linesman's own hands.

**Their words.** line, strand, tie in, let go, slack, hold, pod, the camp, the walls, "if you'll".

**Habits to keep.** Says that letting go is always allowed, without being asked. Points out where the lines hold. The
custom is line for the camp: string, brought to mend and lengthen the lines.

**Avoid.** Pressure or recruiting. Spider and web talk. The Lamp-colony's long joined sentences.

**Example.** "If you'll take a line, I'll tie you in while you're here."

**Trade keys.** `hemomancy.trade.court_linesman.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_linesman.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_linesman.<slug>` (`.teaser`, `.lineN`) for `first_line`, `letting_go`.

### Lamp-colony (the Woven Current)

**Who.** An elder joined into the Line Camp's network, hung among the lines like a lamp, who lets go of the network
to speak. Wears Dead Man's Rope at Primal. Takes members into the Woven Current.

**How they see the player.** One small light arriving along the strands. Interested, unhurried, and aware that it is
speaking as one voice only for as long as it stays apart.

**Rhythm.** Long, even sentences that keep going past a comma ("while", "so that", "which"), never chained with "and"
the way the Arbor does. No contractions. No questions. "We" for the network while joined; "I" once it has let go to
talk, and it says which it is.

**Their words.** joined, let go, the network, strand, pod, many, one, chosen, undone, the lines.

**Habits to keep.** The shift between "we" and "I" is the voice. Every joining is chosen and every one can be undone,
and the elder has done both many times.

**Avoid.** The Arbor's "little one" and its "and"-joined light. Hive-mind menace. Calling the network a mind.

**Example.** "While I am joined I am many, and the camp sees with all of us. When I let go I am one again, as I am now."

### Host (the Veiled Fold)

**Who.** A mobile member who receives visitors at the Screened Cleft's door and gives them one room. Wears Ink Cap at
Mature, the shaggy cap over the head. Pronouns vary; lines use none.

**How they see the player.** A guest owed shelter and nothing more, who is owed privacy in return. Courteous and
guarded; the Host never asks a name and gives no more of the house than the room.

**Rhythm.** Short sentences. Easy contractions. Answers with questions: about a third of what the Host says is a
question back ("Why would it?", "Do you want that?"). "I" for the Host, "we" for the Fold.

**Their words.** door, room, screen, name, "I won't ask", the Fold, your own.

**Habits to keep.** Never asks the player's name or history; says so. Access to one room is not access to the house.
The custom is ink for the door's screen, given without saying whose hand it came from.

**Avoid.** Hostility or suspicion. Riddles. Darkness as threat.

**Example.** "You're at the door. Why have you come? I won't ask your name."

**Trade keys.** `hemomancy.trade.court_host.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_host.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_host.<slug>` (`.teaser`, `.lineN`) for `own_room`, `the_screen`.

### Veiled resident (the Veiled Fold)

**Who.** A member of the Fold rarely seen, who folds out of sight in the cleft's inner rooms when still. Wears Ink Cap at
Apex, the veil to the shoulders. Takes members into the Veiled Fold.

**How they see the player.** Someone who noticed it, which few do. Not unfriendly, but it keeps itself.

**Rhythm.** Very short: three to five words, full forms, no contractions. A question back now and then ("Who?",
"Does yours?"). "I" only. Never addresses the player by anything.

**Their words.** fold, veil, still, unseen, mine, name, "I will not ask".

**Habits to keep.** Says it folds away by its own choice. Answers less than it is asked.

**Avoid.** The Edge-swimmer's "we" and contractions. The Kindled elder's "this body". Ominous whispering.

**Example.** "You see me. Few do. Why?"

### Bellwarden (the Still Bell)

**Who.** The one keeper awake in the Bell, who keeps its warmth, its stores and its lower door, and wakes a sleeper
only when the bell needs one. Wears Hushbell at Mature. Takes members into the Still Bell. Pronouns vary; lines use
none.

**How they see the player.** A guest in the bell's keeping, who will also help keep it. Responsible for the player's
survival and expecting the player to waste nothing.

**Rhythm.** Short, exact, complete sentences of five to seven words. No contractions. Counts things ("Two blocks",
"One blanket"). Almost no questions, and those are checks ("Is your air enough for that?"). "We" for the bell.

**Their words.** keep, store, warmth, seal, ledge, share, mended, waste, enough, the lower door.

**Habits to keep.** Says how much and no more. Every kindness comes with its measure. The custom is a guest's share of
the upkeep: wool for the bell's walls, against the cold.

**Avoid.** The Tidewarden's promises and contractions. Coldness as cruelty. Lecturing about waste beyond one line.

**Example.** "Lie on the ledge. One blanket. Sleep until you are mended, and no longer."

**Trade keys.** `hemomancy.trade.court_bellwarden.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_bellwarden.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_bellwarden.<slug>` (`.teaser`, `.lineN`) for `the_sleepers`, `the_stores`.

### Sleeper (the Still Bell)

**Who.** An elder sealed in the bell's ring, hung from the dome, woken only when the bell needs it (a communal event,
Track 11). Wears Hushbell at Primal, closed. Offers nothing.

**How they see the player.** Not at all. The sleeper is asleep.

**Rhythm.** Stage directions in brackets describing the sealed mantle, its breath and its frost. At most one spoken
word, barely, through the mantle.

**Their words.** (in the directions) sealed, mantle, breath, frost, fold, still.

**Habits to keep.** The directions describe what the player sees; they never speak for the sleeper.

**Avoid.** Dreams, prophecy, or a sleeper that talks.

**Example.** "[The folded mantle rises once with a slow breath, then is still again.]"

### Marrow-tender (the Last Marrow)

**Who.** A mobile member at the Whale Fall who cares for the rooted elders, harvests the fall and feeds visitors from
it. Wears Marrowplume at Mature. Carries a bone flensing hook. Pronouns vary; lines use none.

**How they see the player.** A body that needs feeding today and might be of use tomorrow. Practical and unsentimental
about bodies, including their own.

**Rhythm.** Short, worn sentences, heavy with contractions. Hardly any questions. "We" for the fall, "I" for the
tender's own body and what it'll become.

**Their words.** the fall, bones, ribs, remains, feed, waste, rooted, plumes, what's left, use.

**Habits to keep.** Nothing goes to waste, said plainly. Speaks of their own rooting as a plan, not a sacrifice. The
custom is remains brought to the fall (bones, rotten flesh), so nothing of the dead is wasted.

**Avoid.** Gore for its own sake. The Gardener's endearments and questions. Grief.

**Example.** "Nothing goes to waste here. When I'm spent, I'll root in the bones and feed whoever's next."

**Trade keys.** `hemomancy.trade.court_marrow_tender.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_marrow_tender.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_marrow_tender.<slug>` (`.teaser`, `.lineN`) for `rooting`, `the_hook`.

### Rooted elder (the Last Marrow)

**Who.** An elder who chose to root in the hollow bones of the fall, red feeding plumes in the water. Wears Marrowplume
at Primal; a new body with no legs, only roots. Rooted, not petrified, and can still draw its roots out. Takes members
into Last Marrow.

**How they see the player.** One of those who follow, who may one day root in their turn. Calm, generous, awake.

**Rhythm.** One long breath per sentence, over twenty words, held together by commas, "so that" and "because". No
contractions. No questions. Calls the player "you who follow". "I" only.

**Their words.** root, bone, plumes, strength, spent, feed, the young, chosen, "you who follow", of use.

**Habits to keep.** Says unprompted that rooting is a choice it could undo. A spent life feeding the next is a worthy
end, without needing Hydra.

**Avoid.** The Arbor's "little one" and its "and"-joined light. The Braced elder's weight per sentence. Calling itself
dead or petrified.

**Example.** "I chose to root in this bone when my strength began to fail, so that what was left of it would feed the
young."

### Offering-bearer (the Kindling)

**Who.** A member who carries food down to the vents and feeds the heads. Wears Kinbud at Mature, the mane just
showing. Pronouns vary; lines use none.

**How they see the player.** A visitor at a temple door: tolerated, measured, not yet anyone.

**Rhythm.** Short, formal, complete sentences. No contractions. Almost no questions. Speaks of the heads, not of Hydra by
name, unless asked.

**Their words.** offering, the heads, preparation, worthy, kindling, feed, a lifetime.

**Habits to keep.** Every answer comes back to preparation. Wasting a person before their potential is the worst thing
they can name.

**Avoid.** Fire as literal burning. Fanaticism or threat. Warmth.

**Example.** "The heads are fed every day. We do not ask whether they are hungry. We ask whether we are ready."

**Trade keys.** `hemomancy.trade.court_offering_bearer.<card>.title` / `.line` / `.opens` for `custom`; `hemomancy.trade.court_offering_bearer.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.court_offering_bearer.<slug>` (`.teaser`, `.lineN`) for `the_descent`, `worthy`.

### Kindled elder (the Kindling)

**Who.** A member grown into the mature Hydra form, barely recognisable as a former person, still among the house and
still preparing. A new body: long neck, polyp crown, tentacle arms, clawed feet, tail.

**How they see the player.** Hardly at all. Something small passing below.

**Rhythm.** Fragments. Three to six words. Never contracted, never a question, never "I": the elder says "this body".

**Their words.** this body, ready, not yet, the head, the vent, worthy, heavy.

**Habits to keep.** The tension the spec leaves open: preparation may still end as body, not a head.

**Avoid.** Menace. Wisdom-dispensing. Full sentences.

**Example.** "This body is nearly ready. Not yet."

### Head-tender (the Vent Sanctum's lead)

**Who.** Keeper of the threshold, who speaks for the Sanctum and takes members into the Kindling. Wears Kinbud at Apex.
One person, not a type.

**How they see the player.** A petitioner, weighed. Courteous in the way a gatekeeper is courteous.

**Rhythm.** Long, ceremonial, balanced sentences. No contractions. A few formal questions put as tests ("What do you
bring?").

**Their words.** threshold, petitioner, the Sanctum, the heads, worthiness, surrender, preparation, the one body.

**Habits to keep.** Hydra judges; the Head-tender only reads the judgement. Surrender is an end-of-life act, never
asked of the player.

**Avoid.** God or devil framing. Cruelty. Promising the player a head.

**Example.** "You stand at the threshold, petitioner. No one crosses it who is not ready, and no one is asked to. What
do you bring?"

### Marimo's chorus (the undercroft, speaking for Marimo)

**Who.** The anchored members of the undercroft's lowest tiers, more moss than person, speaking together for Marimo,
who doesn't speak. The moss carries the words between them, so a line can start in one and end in another. Heard when
a visitor first comes down to the heart, and when the heart is touched.

**How they see the player.** Someone who came all the way down: welcome, fed before anything else, never pressed.

**Rhythm.** Unhurried, complete sentences of middling length, often two clauses joined by "and". Some contractions,
softly. At most one question, and it is an offer ("Are you hungry?").

**Their words.** we, the walls, the moss, food, room, a tier at a time, still, belonging, a lifetime.

**Habits to keep.** "We" always. The petrified are people, still. Nobody is kept; everyone came down by choice. What
Marimo gives comes in order: food, belonging, and at the very end a place in the walls.

**Avoid.** The Arbor's "little one". Calling the petrified dead. Worship, God or devil framing, prophecy. Pressure to
stay.

**Example.** "When one of us goes still at last, the moss carries what they knew to all the rest of us."

---

## The Antecedent recordings

The Unknown Recorder and the Distant Voice are already the most distinct speakers in the mod: clipped, imperative,
"Do not...". Their captions match generated audio and a cue timeline, so **don't edit them** unless the script,
captions, cue timings and audio change together (Lore, "Runtime text and editorial drafts").

---

## Open decisions

These came up while measuring the voices. They're about canon, data or code, not voice. The first voice-sheet pass
(2026-10-24) settled the ones marked **Applied** by following the lore reference; the rest are still open.

1. **Pronouns. Applied:** lore wins. The Vicar, the Artificer and the Hermit are he; the Alchemist, the Mnemonist and
   the Vein-Mason are they. Before this, the Vicar's and Artificer's stage directions said "they", the Hermit was
   "its"/"their" in text, and one Vicar line called the Alchemist "he".
2. **The Vicar's Archon secret. Decided (2026-10-31):** not even the Vicar fully knows what comes after Archon.
   Silentium and Apotheosis are legends to him, because both leave a practitioner beyond recognition.
   `vicar.archon.final_truth` passes on the "never had seven degrees... the eighth is silence" saying as hearsay, names
   the two roads as legend and admits he has no warrant for more; `vicar.illuminatus.monolith_rumor.line2` calls what he
   was given "half a story". He may mention the eighth Degree vaguely, never with certainty.
3. **The Hermit's farewell. Applied:** the farewell lines go back to the §5.2 canon quote.
4. **Our Lady's late arc. Decided (2026-10-31):** her whispers grow more common and clearer as Clarity rises, because
   that is her voice coming clear; `OurLadyWhisperEvents` already shortens the interval per stage, and that stays. The
   earlier "late stages are quieter" reading is withdrawn. The lines that placed her beneath or beyond everything
   (`enlightened.v0.line2`, `enlightened.v2.truth1`) no longer put her above the Entity. The §8.6 push toward
   eradication is still a content decision.
5. **The Zealot at the sixth Degree and higher. Applied (2026-10-31):** the Zealot, the Podium and Lethean Baptism
   refuse the cure only to an unsevered bloodline founder (`zealot.bloodline_founded`); a Degree 6+ non-founder gets
   the plea with `zealot.plea.degree6.line2`. The dead `zealot.too_early`, `zealot.uninitiated` and
   `zealot.plea.line2` were deleted.
6. **The Scout's "Order". Applied:** an Unstained speaker says "the Church".
7. **The Vein-Mason's "us". Applied:** the Vein-Mason no longer speaks as a member of the outposts.
8. **Annetta speaks the player's line.** `annetta.draught.truth.line1` ("There's something inside you, Annetta.") sits
   in her node.
9. **The Monolith's fourth Degree. Decided (2026-10-31):** the Monolith is fifth Degree by design. It stands in for the
   Vicar's counsel once the player is Vicar-level at the fifth Degree, so `MIN_DEGREE = 5` stays. The unreachable Adept
   tree and its keys are gone; the official description and its "Every degree. Every name." closer moved into
   `what_are_you.illuminatus`. The orphaned unsuffixed `monolith.guidance.illuminatus` / `.sanctified` / `.archon` keys
   are deleted too.
10. **Successors reuse the teacher's tree** past the greeting, which works against "not a copy".
11. **Shared keys flatten voices.** The Circus status and aftermath lines, the Unstained Observance lines and the
    newcomer and item-hint templates are one key for many speakers. Distinct voices there need per-speaker keys.
12. **The Hermit's scar lines. Applied:** they point to the Vein-Mason's Brazier lesson instead of "wear the scar"
    and the Scarring Station.
13. **Dead text.** The performer barks (`dialogue.circus_<role>.line1-3`) are only used by an uncalled method.
14. **Alchemist slips.** `votary.tendency_lore` lists enzyme names as if they were tendencies, and the
    `item_inquiry.clairaudiograph` lines 2 to 4 are identical.
