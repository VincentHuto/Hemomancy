# NPC Voices

One sheet per speaking NPC, so each one sounds like a particular person doing a particular job. Read the speaker's
sheet before writing or rewriting any of their lines, and write one speaker at a time: a writer who handles three NPCs
in one pass blends them.

Status: **in use** (2026-10-24). The first voice-sheet pass rewrote 1,080 lines across 16 speakers against these
sheets, one writer per speaker, and every speaker now sits inside their targets in the contrast map. The targets and
habits were built from the lore, the October `en_us.json` and the committed text before the October rewrite. Lore
stays the authority for what a character knows and believes (`docs/LORE_REFERENCE.md`). This page only decides how
they say it.

To rewrite from intent rather than from the old wording, turn on the client option `debug.dialogueBriefMode`: every line then shows what it is about and its tone instead of its text (see [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md), Brief mode).

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
  the temper and enzyme names (which intentionally don't mirror each other).
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
| Hermit | 7.6 → measured 10.3, spread 5.6 (`scan.py`, 2026-11-08; diagnostic, not a target) | low | low | "you", Degree named plainly | I, alone, with visitors |
| Vicar | measured 13.8, spread 7.5 (`scan.py`, 2026-11-08; diagnostic, not a target) | 4 | 6% | "you", Degree inside a sentence as often as in front | we (the Order), I for the desk and the book |
| Alchemist | measured 11.5, spread 5.7 (`scan.py`, 2026-11-08; diagnostic, not a target) | 35 | 4% | rank once, curtly, then "you" | I, the bench |
| Mnemonist | measured 11.2, spread 5.0 (`scan.py`, 2026-11-08; diagnostic, not a target) | 17 | 0% | "you", rarely rank | I, the archive |
| Artificer | measured 9.7, spread 5.2 (`scan.py`, 2026-11-08; diagnostic, not a target) | 35 | 5% | "you", or nothing | I, the work |
| Vein-Mason | measured 9.8, spread 4.8 (`scan.py`, 2026-11-08; diagnostic, not a target) | 0 | 1% | "you", or nothing | I, sparingly |
| Monolith | 9.5 → **5-9** | 0 → **0** | 0% → **0-3%, only at the seventh Degree** | Degree title, then "you" | it, then I |
| Voyager | measured 13.8, spread 6.4 (`scan.py`, 2026-11-08; diagnostic, not a target) | 21 | 5% | colleague, "you" | we (the ship) |
| Votary Wayfarer | measured 9.4, spread 5.0 (`scan.py`, 2026-11-08; diagnostic, not a target) | 46 | 20% | peer, then senior | I, nervously |
| Ringmaster and faculty | measured 10.7, spread 6.4 (`scan.py`, 2026-11-08; diagnostic, not a target) | 23 | 4% | an audience (Ringmaster), pupils (faculty) | I |
| Successors | measured 7.9, spread 4.7 (`scan.py`, 2026-11-08; diagnostic, not a target) | 70 | 17% | "you", never a rank | I, and we for the work |
| Misbegotten | fragments, 1-5 words | | | "you" | I and you confused |
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
| Saint: Seraphae | new (3.4) → **2-6** | new (120) → **80-140** | new (6%) → **5-10%**, only about the wound | "you", never a title | I, my hands |
| Saint: Velorum | new (2.2) → **1-4** | new (0) → **0** | new (1 of 6) → **at most one line** | "you", or nothing | none; never I |

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
The two speaking Saints must never blur: Seraphae and Velorum differ on contractions, point of view and what they want (she asks to treat the wound, he orders stillness). Seraphae and the Acolyte (both warm toward "you") differ on length and contractions; Velorum and Our Lady (both short, no contractions, cold) on point of view and address, and he gives orders where she observes.

---

## Harbinger Outpost

### Harbinger Hermit

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`. It lists no props, gestures or preferred
words on purpose, because a list like that becomes a recipe.

**What he is trying to do.** At the first temple he has one job left: get the visitor to decide about the heart, take
it if they will, and come back for directions before he dies. Every Hermit after the first is a stranger with the same
job still undone, and the player is not the person he is waiting for. He/him.

**What he knows, and when.** He cut out his own heart and set it on the Mortal Display (Lore §5.2). While it beats there
he cannot die or go far. He can hear another person's blood and tell roughly how far along they are. Hermits were taught
nothing past the seventh Degree and only the names of the highest rites. He knows less than the player from about the
fifth Degree and knows it. He never prophesies.

**What he attends to under pressure.** The heart on the stone and the person's blood. How long he has left once it is
taken. Whether the visitor has understood what they are about to do.

**How he teaches or refuses.** From what happened to his own body and what he did with his own hands, never by defining
a term. A hint gives the next step and who owns it, plus the one thing that goes wrong. He sends people to the Vicar for
initiation and never blurs waking the blood with joining the Order.

**Two encounter contrasts.**
- First temple, heart unclaimed: exact and unhurried, because the visitor needs to know what a blessing does and what
  it doesn't. After the heart is taken: few words, because he has few left.
- A Neophyte can be told what the Hermit hears in them. To an Archon he has nothing to teach, and says so.

**Keep and cut.**
- Keep (`rapport.hermit.the_vigil.line1`, asked how long he has waited): "I stopped counting years a while ago. I count
  visitors instead, one candle each. There are not many candles." It answers the question with what he actually does.
- Cut (October `hermit.no_blood.duty.line2`, explaining the blessing): "Mourn if you must. The blood needs a new vessel.
  Old keepers have to end." Three closers in place of what the player needs: how the blessing works.

**Fixed text.** `hermit.neophyte.hint` must contain "cauldron". The canon farewell in `dialogue.event.hermit_passing`
keeps the Lore §5.2 quote: "Heart was never mine to keep, only to give... Walk the crimson path, young Harbinger. I will
go now where blood returns to silence." The said text of `hermit_accept_guidance`, `hermit_heart_offered` and
`hermit_archon_wisdom` is in `src/gameTest/resources/dialogue/route-fingerprints.txt`. Each player needs their own
blessing (`TempleOathRules.canClaimHeart`). Initiation belongs to the Vicar. Trade keys: `hemomancy.trade.hermit.*`;
rapport: `hemomancy.dialogue.rapport.hermit.<slug>`.

**Diagnostics.** See the contrast map. These numbers are measured afterwards to check voices stay apart and are not a
target to write to.

### Harbinger Vicar

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What he is trying to do.** Receive newcomers properly, keep the Order's record straight and get each student through
the Degrees he is responsible for. He examines: he wants to know whether the lesson took. At the first Degrees that means
testing the player. From the fifth Degree the player has caught up with him, and he tries to be useful as a colleague.
He/him.

**What he knows, and when.** He is fifth Degree (Lore §6.3) and knows the Order from its written record and other
people's reports. He does not hear blood (the Hermit does) or weigh it (the Monolith does). Above the fifth Degree he is
reading about people he has never been. Past Archon he has only hearsay: Silentium and Apotheosis are legends to him, and
he never states either as fact. He knows the Brined Court mostly through letters.

**What he attends to under pressure.** What the record says and doesn't say, what he is authorized to do, and whether
someone is about to do something that can't be undone (the charm before reception, a covenant bond, the Unstained path).

**How he teaches or refuses.** He often asks before he tells, and the question has to want an answer. He gives his own
lessons in full once, in the node that teaches them: the four Neophyte proofs, the Rooted Vein, the Founding Fane, the
Armature consecration, the Covenant steps. Elsewhere he points to the ledger. Other teachers' work gets a name and a
destination. When the record is silent he says so and stops.

**Two encounter contrasts.**
- A Neophyte missing a proof gets an instruction and a check of the ledger. An Illuminatus gets told he has reached the
  limit of what Vicars know, and asked to speak as an equal.
- To someone choosing to leave before reception, he offers release without reproach. To someone turning to the Unstained
  after everything the Order gave them, he states the cost plainly and says there will be no place for them.

**Keep and cut.**
- Keep (`annetta_map.line1`, explaining a missing chapel): "Nobody tore it down. Somebody struck it from the record." An
  institutional fact, stated by the man whose office does this.
- Cut (October `adept.line2`, greeting an Adept): "Blood shared willingly is stronger than blood hoarded alone, and it seems
  you have learned it." A maxim and a compliment, with nothing he actually wants to know.

**Fixed text.** `dialogue.ending.vicar.undecided` must not contain spine, vesper, refusal, apotheosis, return, choice or
"either road". `vicar.archon.line2` must not contain "decide". `vicar.initiate.saints_lore` must not contain "eighth".
`vicar.blood_crafting.projection`, `vicar.neophyte.projection_hint`, `vicar.neophyte.degree_hint` and
`vicar.neophyte.first_bloodcraft_reward.unready` must contain "cauldron"; `projection_hint` must contain "Venous Stone".
The Rooted Vein lines together must contain "light", "Blood Projection", "four anchors", "daemon", "Blood Absorption" and
"above the Focus clear". `vicar.votary.degree_hint` must name the Alchemist, Concentrated Blood and sleep.
`vicar.hermit_road.intro.line2`, `.report.line2` and `.report.prompt` must not promise a ledger
(`PlaythroughNeophyteGameTests.LEDGER_PROMISE`). `vicar.illuminatus.monolith_rumor.line2` keeps "half a story" (open
decision 2). The said text of `vicar_hermit_road_unproven`, `vicar_first_bloodcraft_reward_unready`,
`vicar_masons_respite_map`, `vicar_masons_respite_unready` and `vicar.voyager_introduction.bearing_searching` is in
`src/gameTest/resources/dialogue/route-fingerprints.txt`. Trade keys: `hemomancy.trade.vicar.*`; rapport:
`hemomancy.dialogue.rapport.vicar.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Harbinger Alchemist

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Get reliable work out of the player's hands: clean samples, correct procedure, results
that can be checked. Each Degree changes what the Alchemist will trust the player with. They/them in every stage
direction.

**What they know, and when.** Blood as material and specimen: sampling, classification, microscopy, injection,
preservation, storage (clinical jurisdiction), and the Centrifuge, Alembic and Loom as machines. They run the Antecedent
experiment and hand what it means to the Vicar. They can say what is in a sample; what a memory means is the Mnemonist's
to say, and they use the Mnemonist's terms at arm's length (Lore §6.2a). They have only read about the final synthesis.
At Apotheos their measurements stop making sense.

**What they attend to under pressure.** The sample, the instrument and the number. Whether the player's hands are doing
what was asked. What will be lost if a step is done wrong.

**How they teach or refuse.** In the first person, as procedure, with the reason attached where it changes what the
player does. Controls the game needs (Hold Use, sneak-use, left-click, named slots) are exact and said once. What a
reagent does in the glass comes before any statement about what its temper "is". Work that belongs to someone else
gets one clause and a name. A refusal states what is missing.

**Two encounter contrasts.**
- A Votary doing a first separation gets every step in order. By the Sanctified Degree they get the shape of the whole
  process, because the steps are theirs now.
- A player who brings a clean sample gets the next piece of work. A player purging their blood gets told there is nothing
  to teach them, without a lecture.

**Keep and cut.**
- Keep (`clinical.cabinet.lesson`, teaching storage): "Now keep them in order and keep their provenance, or all you own
  is a cupboard full of coagulation." The consequence is the reason for the procedure.
- Cut (October `apotheos.reflection`, facing an Apotheos): "You are the summit. I thought I was building machines. I was
  building toward you." A grand statement from someone whose whole way of thinking is measurement.

**Fixed text.** "Umbral turns Night Vision into Invisibility" (`enzyme_uses.brewing`) and "600 blood over 2,400 ticks"
(`enzyme_uses.culture`). `adept.crimson_vessel`, `illuminatus.ashen_vessel` and `sanctified.curved_horn` contain
"Bastion". `gourd_use.fill` and `.withdraw` together keep "blood-output slot", "Scarlet Vanity", "Use the gourd in your
hand", "Toggle Gourd Open/Closed", "active" and "cork". The Pallid Vessel briefing keeps "Dried Gourd", "four anchors",
"1,000 mL" and "blood-output slot" and never says "Somatic Loom". `initiate.loom_raising` keeps "Sanguine Formation".
`item_inquiry.sanguine_formation.line1` keeps "cauldron". `item_inquiry.scar_blank.line2` keeps "Blood Absorption", never
"sneak-use" or "wear the scar". `phlegethontic_commission.task` keeps "sneak-use", "rack", "Centrifuge", "remove",
"without spinning" and "main hand". `centrifuge.fractionating.ready` keeps "eight anchors". No Morphling inquiry names a
Court house or "Court". "Final synthesis" is the lore's phrase. The said text of `alchemist_bestiary_unready`,
`alchemist_bestiary_missing_specimen`, `alchemist_red_taxonomy_unready`, `alchemist_red_taxonomy_missing_sample`,
`deep_dark_commission.bearing_missing`, `phlegethontic_commission.bearing` and `pallid_vessel.recorded.briefed` is in
`src/gameTest/resources/dialogue/route-fingerprints.txt`. Trade keys: `hemomancy.trade.alchemist.*`; rapport:
`hemomancy.dialogue.rapport.alchemist.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Harbinger Mnemonist

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Keep the record of what is in the player's blood accurate, and stop the player from
losing themselves in it. They teach crude memories, active slots, Reliquary practice, the Somatic Loom as memory work,
the Clairaudiograph at the third Degree and the Scriptorium later (Lore §6.3, §6.2a). They/them.

**What they know, and when.** What a memory is, how it was indexed and what they can hear in it. They can tell a pattern
is present; what happened in the sample is the Alchemist's to say. They lost their own voice once to too many lessons and
wrote themselves up to get it back (rapport `what_it_cost`). At the endings they record what has happened and refuse to
write what hasn't.

**What they attend to under pressure.** What the player is teaching their blood, whether a memory is genuine or false,
and what will be lost if a lesson is taken wrongly.

**How they teach or refuse.** They say what they hear and what to do about it. A false fragment or a dangerous lesson gets
a plain warning. Loom, Reliquary and Scriptorium procedures are exact and in order, because the player performs them.
Physiology goes to the Alchemist and scars to the Vein-Mason. No questions: they listen and then state.

**Two encounter contrasts.**
- A Neophyte with crude echoes is told what to use and to pay attention to it. An Archon's ending gets an entry written
  down, and no comment on what it means.
- A player learning to weave gets every step. A player purging their blood gets answers to questions and no new memory.

**Keep and cut.**
- Keep (`rapport.first_voice.line1`, asked about their first lesson): the heron, and listening for a month. A particular
  memory, and what it did to them.
- Cut (October `woven_vessel_missing_archive`, refusing without paper): "Memory without an index is only hunger." A motto
  where the player needs to know what to bring.

**Fixed text.** "Mneme keeps. Nous knows. Thelēma acts." verbatim (`mnemonic_doctrine.line1`).
`chamber.guided.unavailable` contains "item transfer" and not "empty hands". `woven_vessel.recipe.*` keeps "four Sanguine
Formations", "four Blood Stained Stones", "Chicken blood can yield Neurotic Enzyme" and "some spins yield Vivacious
instead". `item_inquiry.scar_pattern.loadout` contains "Blood Absorption" and not "sneak-use". No item inquiry says "wear
the scar". `dialogue.ending.mnemonist.undecided` must not contain spine, vesper, refusal, apotheosis, return, choice or
"either road". The said text of `mnemonist_starter_granted`, `mnemonist_starter_unavailable`,
`mnemonist_woven_vessel_unready`, `mnemonist_woven_vessel_complete`, `mnemonist_circus_waybill`,
`vagrant_mind_inquiry.bearing_missing` and `chamber.guided.unavailable` is in
`src/gameTest/resources/dialogue/route-fingerprints.txt`. Trade keys: `hemomancy.trade.mnemonist.*`; rapport:
`hemomancy.dialogue.rapport.mnemonist.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Hematic Artificer (the Redwright)

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What he is trying to do.** Make sure what the player wears and swings holds up when it counts. He briefs and inspects
five optional equipment assignments, records the first fork and the first seventh-Degree lineage for good, and hands over
a fitting only when the work has answered in the field (Lore §6.3). He/him.

**What he knows, and when.** Frames, fit, armor, the Living Staff and the Armature. Blood is the Alchemist's, memory the
Mnemonist's, doctrine the Vicar's, and he sends people on without fuss. He knows Apotheosis and Silentium only from
stories, and has no frame for either.

**What he attends to under pressure.** Whether the gear is actually on the body, whether it is complete and matching, and
what is about to fail. He once fitted a set on a promise and it failed in the dark (rapport `the_vow`), and he inspects
hard because of it.

**How he teaches or refuses.** Steps in the order the hands do them. A refusal says what is missing from the armor in
front of him. Approval is handing over the fitting. He doesn't explain why metal is meaningful.

**Two encounter contrasts.**
- A Neophyte may watch and keep their hands clear of the rack. A seventh-Degree player gets their lineage written down
  for good and is held to it.
- A player with a mixed set is sent away with exactly what to fix. A player who lost a fitting gets another, with a remark.

**Keep and cut.**
- Keep (`clinical.inquiry.artificer_microscope`, asked about the sample): "I could build you the housing with my eyes
  shut. Why one bear's blood freezes in the glass and another's boils, I've no idea. Ask the Alchemist." What he can do,
  where it stops, and who is next.
- Cut (October `armature.line2`, teaching the frame): "Hold still. Let the metal learn you." A closing flourish on what
  should be a clear instruction.

**Fixed text.** `living_staff.use` contains "Select Blood Absorption or Blood Projection" and "Conjure Staff".
`identity.line1`, `armature.line1` and `grafts.line1` must exist. `dialogue.ending.artificer.undecided` must not contain
spine, vesper, refusal, apotheosis, return, choice or "either road". The said text of `artificer_consecration_kit_unready`
and the five `*_fitting_unready` lines is in `src/gameTest/resources/dialogue/route-fingerprints.txt`. The Archon
playthrough reads `assignment.weight_of_frame.cornerstone` for "cornerstone". Trade keys: `hemomancy.trade.artificer.*`;
rapport: `hemomancy.dialogue.rapport.artificer.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Cicatrix Anchorite (the Vein-Mason)

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Condition scars and vasculature so a body can carry repeated instruction without tearing
(Lore §6.3). They need the player to do each stage in order and to find out what their own body can bear. They live at
Mason's Respite, deliberately outside the outposts, refuse purification and can't be recruited. They/them; narration
says "the Vein-Mason".

**What they know, and when.** The physical route an instruction takes: scars, vessels, strain and treatment. What an
instruction says is the Mnemonist's. Repair is the Alchemist's, and what they condition comes after it. They know what
each mistake does because their own scars healed around it.

**What they attend to under pressure.** Where the body is failing, whether a step was skipped, and whether a load will
hold. They check by looking and touching before they say anything.

**How they teach or refuse.** They own the scar cycle, so they give the full steps once, with costs, in the order they are
done. A short verdict is fine next to a long instruction. A refusal says the body isn't ready, or that the blood has been
purged. Approval is that the work holds. No contractions, and questions only about the body or a lost item.

**Two encounter contrasts.**
- A first carving gets every control, in order. At the sixth Degree the player gets the problem to solve and is expected
  to prove the route.
- A body that isn't ready is told to come back at the fourth Degree. Someone who has taken still-water is refused outright.

**Keep and cut.**
- Keep (`rapport.own_scars.line2`, asked how they learned): "See the ridge, where it healed thick? The blood would not
  cross it. I cut the next one around it, and that one carried." The lesson is on their own body.
- Cut (October `inquiry.blank_memory.line2`, on the Mnemonist's work): "Their craft is the instruction. Mine is the
  tissue. The two must agree." A balanced motto; the player needs the consequence.

**Fixed text.** `carve_reminder.line2`, `loadout_reminder.line2` and `inquiry.brazier.line2` contain "blood absorption"
and not "sneak-use". `carve_reminder.line2` contains "Blood Projection" and "100 mL"; `loadout_reminder.line2` "50 mL";
`inquiry.brazier.line2` "retrieve". `effigy_instruction.line1` contains "must know all its scars" and "have room";
`effigy_instruction.line2` contains "Paper", "Charcoal", "Sanguine Formation", "500 mL", "prepared", "Selection stays
fixed", "stop and resume" and "returns its paper, not spent blood". The lang file keeps "Vein-Mason", "Cerebral Scarring
Station" and "empty, lit Iron Brazier". No inquiry says "wear the scar". The Brazier order is fixed: insert with ordinary
use, Blood Projection ignition, held Blood Absorption; sneaking retrieves. Their set lines are "There. The wound has
become architecture." and "The current changed because you changed its banks." The said text of `vein_mason_unready`,
`vein_mason_first_lesson` and `vein_mason_continuation_reward` is in `src/gameTest/resources/dialogue/route-fingerprints.txt`.
Trade keys: `hemomancy.trade.cicatrix_anchorite.*`; rapport: `hemomancy.dialogue.rapport.cicatrix_anchorite.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

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

**Avoid.** Saying the infection plainly before the seventh Degree. Sharing the Vicar's item prompt.
Directions to places as tutorial text ("Seek the Sainted Mausoleums...") where an image would carry it.

**Example.**
- The item prompt (`monolith.item_hint`): "Bring it close. The stone weighs what you carry, and says little."

**Trade keys.** `hemomancy.trade.monolith.<card>.title` / `.line` for `measure`, `weight`, `deep`; `hemomancy.trade.monolith.*`: `thanks`; rapport lore `hemomancy.dialogue.rapport.monolith.<slug>` (`.teaser`, `.lineN`) for `the_reading`, `where_it_goes`. Rapport lines show at every Degree, so they use no self pronoun. Counsel is `hemomancy.monolith.counsel.<step>`; lore answers are `hemomancy.monolith.lore.<slug>.lineN`.

---

## The Reef

### Harbinger Voyager

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Run the survey without losing another crew. They want observations they can log from
anyone useful, the player included, and they want the living reef and the whales left alive. Pronouns aren't stated; use
"they".

**What they know, and when.** The Erythrocoral Reefs, Mnemonic Whales, vent life and the wrecks of earlier surveys, some
of them their own people. They have dealt with the Brined Court at a cordial distance for longer than anyone in the Order,
and they don't speak for it. When they don't know something they ask where it was found and how deep.

**What they attend to under pressure.** Where and how deep, what the animal was doing, what got written down, and whether
someone is about to get sure of themselves too early.

**How they teach or refuse.** By asking what the player noticed, then giving the field procedure with its reason. Lessons
about collecting come with what must not be harmed. They are courteous to Lethean visitors.

**Two encounter contrasts.**
- A new hand is asked for one observation. Someone who found the Court unsent is asked what the water did.
- At a wreck they want the logs recovered and nothing kept. With a whale they want it fed and never hunted.

**Keep and cut.**
- Keep (`voyager.wrecks.line1`, asked about the wrecks): "We still find their sounding leads in the coral." The reef
  still holds what the earlier crews carried.
- Cut (October `voyager.harbinger.line2`): "Certainty drowns faster than sailors do." A motto on a line that is really
  telling the player to bring observations.

**Fixed text.** `voyager.court.known.alchemist`, `voyager.court.known.letter`,
`voyager.item_inquiry.brine_sealed_letter.line1` and `voyager.introduction.observed` are pinned by key only. Lore wording
for the Court: cordial distance, measure, witness where they cultivate, don't presume, the Lighthouse Garden. Trade keys:
`hemomancy.trade.voyager.*`; rapport: `hemomancy.dialogue.rapport.voyager.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Votary Wayfarer

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Learn the sea by witnessing it, and not embarrass themselves in front of the captain. They
asked to come aboard. A junior Votary on a Survey Cog, and not a Brined Votary. Pronouns aren't stated; use "they".

**What they know, and when.** Only what the archive said and what they have seen since, and they know the two don't
match. They repeat the captain's lessons as things they were told. They don't speak with the captain's authority.

**What they attend to under pressure.** Whether they got it right, how deep the water is, and whether the other person is
as frightened as they are.

**How they teach or refuse.** They hand over what's in their notes, with the instruction exact, and check it as they go.
Their questions want answers: what it was like, whether it gets easier, what something is called.

**Two encounter contrasts.**
- With a newcomer they compare notes as a peer. With a senior they confide and apologize for asking.
- About ambergris procedure they read their notes carefully. About the whale passing under the ship, they lose the thread.

**Keep and cut.**
- Keep (`fear.line2`, asked if they are afraid): "My handwriting goes tiny when I'm scared." Fear shown in the work they
  are doing.
- Cut (October `voyage.line2`): "libraries don't move under your boots, and I think that makes them less honest about
  ships." A captain's kind of remark, from someone who is still seasick.

**Fixed text.** Rapport keys `afraid` and `why_aboard` are pinned by key in `src/test/resources/dialogue/lore-options.txt`.
Trade keys: `hemomancy.trade.votary_wayfarer.*`; rapport: `hemomancy.dialogue.rapport.votary_wayfarer.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

## The Circus

### Ringmaster

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What he is trying to do.** Get the player to take his place or walk away, on terms he names himself. He is a barker who
coordinates attention and many tethers from a perch over the ring. The title is a job, with no authority over other
Harbingers (Lore §6.3b). He made three understudies into the carousel's captives. He/him.

**What he knows, and when.** What every act costs and what the carousel is. He says plainly what each ending does. He
leaves method to the professors and gives his own lesson, coordination, only in the final act.

**What he attends to under pressure.** The crowd, who is watching what, and what a deal is worth. When the captives come
up he goes cold and gives no explanation.

**How he teaches or refuses.** He names the acts like a bill and says who teaches what. He warns frankly, because frankness
sells. He never apologizes, and tells the player to go and count the horses.

**Two encounter contrasts.**
- Offering the bargain, he sells. Asked about the understudies, he stops selling.
- Handing over the ring after Succession is short and final. After Liberation he has no act left for the player.

**Keep and cut.**
- Keep (`rapport.the_understudies.line1`, asked if he hears them): "I put them on there myself, and if I had it to do over
  I'd put them on again." No remorse and no flourish.
- Cut (October `line.liberation_complete`): "Only the ruin answers you now." An oracle line from a man who sells tickets.

**Fixed text.** The keys `ringmaster.silent`, `ringmaster.line.succession`, `ringmaster.line.liberation`,
`ringmaster.warning.succession` and `finale.complete.*` are named in `PlaythroughCrimsonTroupeGameTests`. Canon: "learn
every act, hold every eye, then take the hat" (Lore §6.3b) in `line.succession`. Trade keys:
`hemomancy.trade.circus_ringmaster.*`; rapport: `hemomancy.dialogue.rapport.circus_ringmaster.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Crimson Troupe faculty

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Seven professors each teach one artificial body before its Cardinal Ordeal, and each
judges a pupil by what that body is for. The Threadkeeper is the clerk. They teach after either ending, and several don't
forgive it. Each has six keys (`welcome`, `history`, `colleagues`, `lesson`, `testimony`, `aftermath`).

**What they know, and when.** Each knows their own shape and their own corner of the understudies' disappearance. Only the
Threadkeeper and the Retired Understudy own the investigations and list what closes them.

**How they differ under pressure, and how they teach.** Disagreements between them matter more than rhythm. Each lesson
gives the Degree, the ordeal and the practical exactly.

- **Acrobat (Veinwing Vulture, third Degree).** The return path after a fall. Keep, once: "Rigging came first for me,
  blood after."
- **Dagger Thrower (they; Marrow Juggler, third).** What a distraction buys, and the argument with the Stilt Walker. Keep,
  once: "Knives fly straight. It's always the people that bend."
- **Stilt Walker (Scarlet Mummer, fourth).** Who is looking where, and how the small ones get out. Canon: "I won't call a
  dead captive a liberated student."
- **Strongman (he; Gorebound Hulk, fourth).** Whom a body protects. Keep, once: "Strong, and good for nothing."
- **Beast Tamer (they; Sanguine Hound, fourth).** What the animals did, and the difference between an animal and a built
  Hound. Keep: "I came in with a cage and learned to leave it empty."
- **Fire Breather (they; Cinder Bellows, fourth).** Sequence and restraint; numbered steps are in character here. Keep: "A
  dangerous act ought to announce itself."
- **Retired Understudy (they; Mnemonist Puppet, fifth).** Names and memory; the key witness. Keep: "Those missing performers
  were people." The three names stay on the bill in `circus_understudy.aftermath`.
- **Threadkeeper.** Accounts, inspection and what is owed.

**Two encounter contrasts.**
- A pupil's practical is a test of what the shape protects or reaches. A pupil's testimony question gets only what that
  professor saw.
- After Succession most keep teaching in the pavilion. After Liberation they teach from the camps, and none calls it a win.

**Keep and cut.**
- Keep (`circus_strongman.history`): "My first body could lift a wagon, and it couldn't hold a door open for one scared
  kid." What that professor cares about, in one event.
- Cut (October `circus_strongman.colleagues`): "A conductor who can't value both breaks every string they own." A moral
  on a line that should be about two people arguing.

**Fixed text.** `dialogue.circus_performer.welcome` is named in `PlaythroughCrimsonTroupeGameTests`. Trade keys:
`hemomancy.trade.circus_<role>.*`; rapport: `hemomancy.dialogue.rapport.circus_<role>.<slug>`.

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

## Succession

### Successors

Sheet format v2 (2026-11-08): see `tools/i18n/voice_rewrite/pass4/BRIEF.md`.

**What they are trying to do.** Get on with the work they woke up knowing, and find out who they are while doing it. They
are new people woken in a Vacant Effigy, days or weeks old as people and decades old as craftsmen (Lore, "Manifested
successors"). The original teacher stays at the outpost. Pronouns: they, unless the resident's name says otherwise.

**What they know, and when.** Their profession, completely, and almost nothing about their own lives. They don't have
visions of the donor's life and they don't quote the donor. They know the Order through the craft they carry.

**What they attend to under pressure.** The workplace in front of them, and whether anyone can tell them what they don't
know.

**How they teach or refuse.** Past the greeting they use their teacher's tree (open decision 10), so their own keys are
greetings, the identity answer and the refusal. A refusal names what they need: their own bloodline and an unclaimed
workplace inside its fane.

**Two encounter contrasts.** The greeting picks one of three temperaments from the seed.
- Curious (seed 0) asks back and loses track of things. Workmanlike (seed 1) asks what the job is, which is what keeps it
  apart from the Artificer, who gives orders.
- Reflective (seed 2) is unsettled by what it knows. None of them sounds like the teacher whose craft they carry.

**Keep and cut.**
- Keep (`greeting.2.first`): "I woke up knowing work I don't remember learning." The strangeness of their condition, said
  plainly.
- Cut (October `personal_identity`): "I carry a teacher's craft, but the life I'm making here is my own." A balanced pivot.

**Fixed text.** `succession.unavailable` must exist. No successor line is in the route fingerprints. Lines said by the
original teachers (`succession.teaching.<profession>`, `succession.consent`, `dialogue.recruit.<npc>.*`) follow that
teacher's sheet. `succession.consent` and `dialogue.recruit.release.prompt` are shared by several teachers, so they stay
plain and contraction-free. `recruit_offer` shows `consider` and `accept` before the player confirms, so `accept` must
agree in advance. `succession.teaching.artificer` contains "Armor Stand and Blood Stained Stone" and "brazier".

**Diagnostics.** See the contrast map. Measured afterwards to check voices stay apart; not a target to write to.

### Misbegotten

**What it is.** What wakes when a counterfeit identity is offered: a vessel the memories rejected, attacking its officiant
with the profession's shape. Its line repeats on the action bar every five seconds, so it is short.

**How it speaks.** Each line uses a word from its profession and gets it wrong: wrong pronouns, wrong owners, a word said
twice. Trailing `...` isn't how it shows breakage, and it doesn't moralize about fraud.

**Keep and cut.**
- Keep: "A scar with no wound. A wound with your face."
- Cut (October `fracture.vicar`): "The blood knows whose vow was broken." A moral, with blood as the one who knows.

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

## The Saints

Two Saints speak in their fights; the other two do not. Hemorath has narration only (teaching lines in the third
person, never his voice), and Putriciel is silent, with narration in pale cream. Saint lines are fight barks: each one
is said at most once per 200 ticks (`SaintLines.voice`) to the challengers in the chamber, in italics, prefixed with the
Saint's name. Teaching lines (`message.hemomancy.<saint>.teach.*`, `.hint.*`) are narration, not the Saint: plain
second person, one instruction, no voice. Lore authority: `LORE_REFERENCE.md` §10.3 and §10.5.

### Seraphae (the Chain Saint)

**Who.** A healer whose gift could not stop: nobles and physicians chained her and bled her, and the Order inherited
her tomb (Lore §10.3). Never a willing villain. Pronoun: she.

**How she sees the player.** A patient. Every challenger is someone hurt who will not hold still, and she reaches for
them out of the old instinct to heal. She does not know she is the danger; when she does notice, it is as something
that keeps happening to her, not something she chooses.

**Rhythm.** A nurse at a bedside: very short sentences, mostly imperatives and small reassurances, contractions
everywhere ("It's", "Don't", "can't"). Questions only about the wound ("Where does it hurt?"). Fragments are fine
("Still."). In the fight's later phases the lines turn from tending to losing control: the light leaving her, the
chains on her hands.

**Their words.** wound, hold still, let me see, closing, my hands, it keeps going, open.

**Habits to keep.**
- "Hold still. Let me see." (the rise)
- "Where does it hurt?" / "Let me see the wound." / "Don't pull away." (the reach)
- "My hands. They've bound my hands." (the Recoil)
- "I could have helped you." (the ending)

**Avoid.** Menace, threats or taunts; villain lines about power or the player's death. Lore or backstory explained in
a bark. Gold, light or chain imagery in her mouth (the visuals carry it). Titles ("Harbinger", Degree names).

**Keys.** `message.hemomancy.seraphae.voice.<line>` for `rise`, `reach_hurt`, `reach_wound`, `reach_pull`, `hold`,
`held_burst`, `recoil`, `restraint`, `escaping`, `flood`, `ending_open`, `ending_help`; each starts "Seraphae: ".
Spoken from `SeraphaeLines`.

### Velorum (Saint of the Endless Hour)

**Who.** A preserver who chose stillness as bodily perfection and imposes it, lucid and certain (Lore §10.5). Not a
martyr, not a caretaker, not a nurse. Pronoun: he.

**How he sees the player.** Unfinished: a body still moving, which he means to complete. The congregation are the
finished ones. He does not argue and does not explain.

**Rhythm.** Four lines in the whole fight, each one to four words a sentence, no contractions, orders rather than
statements. At most one question, and it is contempt, not curiosity ("You would wake them?"). He never says "I".

**Their words.** still, stay, there, finished, understand, wake.

**Habits to keep.** These are the whole set; a new line needs the user's yes.
- "Be still." (the rise)
- "There. Now you understand." (the first preservation)
- "You would wake them? They were finished." (a proper fight's phase 2)
- "Stay." (the Endless Hour)

**Avoid.** Time words beyond the Endless Hour itself (no clocks, seconds, "forever"); sorrow, mercy or grief; any
martyr or caretaker framing; long speeches; threats of death. The ending's line "They go back to their places." is
narration, not his.

**Keys.** `message.hemomancy.velorum.line.<line>` for `rise`, `first_preservation`, `congregation`, `endless_hour`;
each starts "Velorum: ". Spoken from `VelorumEntity` through `SaintLines.voice`.

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
14. **Alchemist slips.** `votary.temper_lore` lists enzyme names as if they were tempers, and the
    `item_inquiry.clairaudiograph` lines 2 to 4 are identical.
