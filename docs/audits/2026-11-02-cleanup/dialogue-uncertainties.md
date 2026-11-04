# Dialogue uncertainties audit (2026-11-02)

Read-only audit of NPC dialogue trees (`common/entity/npc/dialogue/`), routes (`.../dialogue/route/`), item inquiries
(`data/hemomancy/dialogue_inquiry/`, 385 files, 27 speakers), trade cards (`data/hemomancy/npc_trade/`, 56 cards, 29
speakers), whispers, Communion and Our Lady text, against `en_us.json`, `docs/LORE_REFERENCE.md`, `docs/NPC_VOICES.md`
and the gating code. No build or test was run. Structural checks were static scripts over the Java sources and JSON
(862 `DialogueOption` sites, 99 posted event ids, every route `case`/`equals`/`startsWith`, every dialogue-prefix lang
key), then hand verification of each hit.

**Counts:** 35 findings: 1 High, 9 Medium, 25 Low. 33 Verified, 2 Suspected. 7 need the user's call (DL-1, 6, 9,
13, 17, 23, 30); DL-29 joins a question already open. Items already listed in `docs/OPEN_QUESTIONS.md` §1.3, §1.4 and §1.6 (PT-n) are cited, not re-reported;
DL-11 and DL-19 report two of those open items as now resolved or stale in code.

Severity: **High** breaks canon at a key moment or blocks a step; **Medium** misleads a player, hides a Main step or
leaves a system silently broken; **Low** dead text, wording, or polish.

---

## 1. Structural

### DL-1 (Medium) Our Lady and Ancestral Communion options post events no route handles
- **Claim:** Nine event ids are posted by options but no route, Liber unlock or memo handler takes them, so they do
  nothing. Three of them have narration lines that are never shown, one of which promises an effect that does not
  happen.
- **Evidence:** `OurLadyWhisperDialogueTrees.java:247` (`lady_clarity_rite_memory`), `:440` (`lady_enlightened_truth_1`),
  `:462` (`lady_enlightened_truth_2`); `AncestralCommunionDialogueTrees.java:59,65,80,...` (`communion_end`,
  `communion_lore_origin`, `_schism`, `_infection`, `_harbingers`, `_true_name`). No `case`/`equals` for any of them in
  `route/*.java`; `LiberEntryDefinitions.registerDialogue` only registers `hermit_first_rite`; none use the memo
  prefix. Dead lines: `hemomancy.dialogue.event.lady_clarity_rite_memory` (`en_us.json:2066`): "The words sink into
  your mind ... the Rite of Clarity Ascension. You know how to perform it now." and `lady_enlightened_truth_1/2`
  (`:2067-2068`). Git history (`68a2e5e46`) shows the lady ids were only ever posted, never routed.
- **Confidence:** Verified.
- **Action:** decide: either add a small route (e.g. a `LadyWhisperDialogueEvents` entry in `DialogueEventRouter.ROUTES`
  that shows the three lines) or drop the event ids and cut the three keys. The Communion ids can become `null`.
- **User call:** yes (whether these choices should leave a message or record anything).

### DL-2 (Low) A Zealot route nothing posts
- **Claim:** `ZealotDialogueEvents` handles `zealot_accept_church`, but no tree offers it. Its option and two old
  event lines are dead.
- **Evidence:** `route/ZealotDialogueEvents.java:40`; no `DialogueOption` posts it. Dead keys:
  `hemomancy.dialogue.zealot.option.accept_church` (`:2233`), `hemomancy.dialogue.event.accept_church` (`:2013`,
  "Follow the silver bells..."), `hemomancy.dialogue.event.accept_purification` (the live
  `zealot_accept_purification` case shows `unstained_starter_granted` instead).
- **Confidence:** Verified.
- **Action:** cut the case and the three keys (the `DialogueEvent`/`DialogueOption` Javadoc examples name
  `accept_church` too).
- **User call:** no.

### DL-3 (Low) Liber unlock registered for an event nothing posts
- **Claim:** `registerDialogue("hermit_first_rite", HERMITS)` can never fire.
- **Evidence:** `LiberEntryDefinitions.java:178`; no option posts `hermit_first_rite` and nothing uses the
  `liber_unlock:` prefix.
- **Confidence:** Verified.
- **Action:** cut, or post it from the Hermit's neophyte hint option if the Hermit-tradition page should unlock there.
- **User call:** no.

### DL-4 (Low) Dead lang keys under dialogue and inquiry prefixes
- **Claim:** These keys are referenced by no Java literal, no dynamic prefix that can build them, and no JSON.
- **Evidence (key, `en_us.json` line):**
  - `hemomancy.alchemist.initiate.pallid_vessel.fill` / `.withdraw` (1316-1317), superseded by `gourd_use.fill/withdraw`
    in `PallidVesselDialogue.gourdUse()`.
  - `hemomancy.alchemist.neophyte.alembic_leak` (1437), `hemomancy.dialogue.alchemist.option.alembic_leak`,
    `hemomancy.alchemist.neophyte.alembic_lore` (1438).
  - `hemomancy.alchemist.votary.tendency_lore` (1476): see DL-19.
  - `hemomancy.dialogue.event.vicar_hermit_road_ledger` (2101): the old ledger reward; see DL-33.
  - `hemomancy.dialogue.event.alchemist_red_taxonomy_placeholder_reward` (2029): see DL-35.
  - `hemomancy.dialogue.option.ask_about_item` (2153), `hemomancy.dialogue.topic.held_item`.
  - `hemomancy.dialogue.unstained.observances.line1` (2186), `hemomancy.dialogue.unstained.option.observances`
    (the decorator now builds per-observance cards from `observance.ready/active/available`).
  - `hemomancy.monolith.item_hint` (3024): the Monolith's held-item path has no `item_hint` node.
  - `hemomancy.whisper.busy` (parent of the live `busy.machine/item/combat/rite`).
  - `hemomancy.zealot.item_inquiry.pallid_infusion.not_yet` (3502): `zealot/hemomancy/pallid_infusion.json` uses only
    `line1/line2`.
  - `hemomancy.vicar.item_inquiry.scar_binder.line1/2` (3275): the Scar Binder has no recipe, loot or creative-tab
    entry (a retired practice; the line itself says so).
  - Retired-memory inquiry lines: `hemomancy.mnemonist.item_inquiry.crude_memory_blood_lamp.*` (2826),
    `crude_memory_crimson_harvest.*`, `memory_blood_absorption.*`, `memory_blood_projection.*`,
    `memory_blood_eclipse_mantle.*`, all ids in `ManipulationRetirementRules.RETIRED_MEMORY_ITEM_IDS`.
  - Already known and not re-reported: the four legacy whisper keys in the Fungal Entity sheet, the performer barks
    (NPC_VOICES open decision 13).
- **Confidence:** Verified (the 15 key-shaped literals my scan could not match were all NBT keys or
  `claimArmatureTier` prefixes, checked by hand).
- **Action:** cut, after DL-19/33/35 are settled.
- **User call:** no.

### DL-5 (Low) Inquiry files for items that do not exist
- **Claim:** Two inquiry files name item ids with no registered item, so they never load into a reachable entry.
- **Evidence:** `votary_wayfarer/minecraft/boat.json` (`minecraft:boat` is not a 1.21.1 item; the boats are
  `oak_boat` etc., checked against the 1.21.1 client resources jar); `vicar/hemomancy/blood_structure_hint.json`
  (no `blood_structure_hint` item; the Mnemonic Blueprint replaced it and has its own stack-aware Vicar answer), which
  also strands `hemomancy.vicar.item_inquiry.blood_structure.line1/2` (3228-3229). The third such file is DL-25.
- **Confidence:** Verified.
- **Action:** fix (`oak_boat` or an `#minecraft:boats` approach is not supported by the loader, so one file per boat or
  drop it); cut the blueprint-era file and its two keys.
- **User call:** no.

## 2. Reachability

### DL-6 (Medium) The newcomer card's gate and its text disagree, and it never leaves a never-infected Unstained player
- **Claim:** Every non-Circus speaker adds "Who are the Harbingers?" while `degree == 0 && !hasMetHermit`. The lines
  say "you haven't met the Vicar", but the gate is the Hermit, so a D0 player sent on by the Vicar still hears "talk
  to him". A player who enters the Church through the Zealot's vows without ever touching blood stays D0 and never
  meets a Hermit, so the card shows forever at the Zealot, Acolyte and Guardian, and at every Harbinger NPC.
- **Evidence:** `DialogueHubFactory.java:51-54`; `hemomancy.dialogue.newcomer.votary_wayfarer` (6311): "Wait, you
  haven't met the Vicar yet?"; `newcomer.court_gardener` (9748): "You haven't met a Vicar yet?"; the Vicar's own
  `newcomer.vicar_path` (6301) sends the player to a Hermit. Zealot vows for an unmarked player:
  `ZealotDialogueTrees.java:217,252` (`zealot_begin_novitiate`), `hemomancy.zealot.no_blood`.
- **Confidence:** Verified.
- **Action:** fix the gate (also skip players on the Unstained path) and rewrite the lines toward "start at a Hermit's
  temple, the Vicar takes it from there", or decide the Vicar is the intended gate.
- **User call:** yes (which gate is intended).

### DL-7 (Low) The Vicar teaches the Qliphoth sequence to any Degree
- **Claim:** The Vicar's Qliphoth Pome and Seed inquiries have no degree branch, so a low-Degree player holding a
  dropped or traded Pome hears the nine-Pome and Fungal Spine sequence, in a line that also says it should not be
  taught below that threshold.
- **Evidence:** `vicar/hemomancy/qliphoth_pome.json`, `qliphoth_seed.json` (plain `lines`);
  `hemomancy.vicar.item_inquiry.qliphoth.line2` (3272): "Nine Pomes from one Bloom, all eaten, complete Communion,
  and the ninth tears the Fungal Spine free. No one below that threshold should be taught it as ordinary practice."
- **Confidence:** Verified.
- **Action:** fix: add a `min_degree: 7` branch and a vaguer line below it.
- **User call:** no.

## 3. Canon contradictions

### DL-8 (High) The core witness choice buttons call Silence a refusal of the Eighth Degree
- **Claim:** Canon makes Apotheos and Silentium two equal ends of Degree 8. The line right above the buttons says so,
  but the buttons frame only Apotheosis as "the Eighth Degree" and Silence as refusing it.
- **Evidence:** `hemomancy.whisper.core_witness.line3` (3389): "...you stand at the eighth Degree as a Silentium...";
  `core_witness.option.eighth_degree` (3390): "[I seek the Eighth Degree. Show me.]";
  `core_witness.option.silence` (3391): "[Choose Silence. Refuse the Eighth Degree.]". LORE_REFERENCE Canon
  Governance: "the eighth Degree splits into two equal ends". The same framing recurs in
  `hemomancy.rite.apotheos_rite.desc` (6726, "The Eighth Degree rite").
- **Confidence:** Verified.
- **Action:** rewrite both buttons (e.g. "[Seek Apotheosis.]" / "[Choose Silence. Cut the Bloom.]"); pins only forbid
  "go home" in `option.silence`.
- **User call:** no (wording only), unless the old framing was deliberate.

### DL-9 (Medium) The eighth Degree is named to Initiates
- **Claim:** Lore says the eighth Degree "reaches Archons only as a saying", and "no one below the eighth Degree knows
  what it is, the Vicar included". Several player-facing lines state it plainly to Degree 3-6 players.
- **Evidence:** `hemomancy.vicar.initiate.saints_lore` (3226, Degree 3 tree, `HarbingerVicarDialogueTrees.java:391-403`):
  "a ward turns back anyone below the eighth Degree"; `message.hemomancy.mausoleum_ward.refused` (690, shown at the
  outer-hall threshold, `MausoleumEvents.java:217`): "Only the eighth degree goes further."; `message.hemomancy.chapel.refused`
  (669); `hemomancy.rite.archon_rite.desc` (6727, the rite description, which JEI lists; I did not check whether JEI hides it
  by Degree): "preparing the way for the hidden Eighth Degree."
- **Confidence:** Verified (text and where it shows); the design intent is the open part.
- **Action:** decide: either soften these to "a later threshold" / "the ward does not know you yet", or record a ruling
  that the ward may name the Degree.
- **User call:** yes.

### DL-10 (Medium) LORE_REFERENCE puts the Concentrated Blood wake at Degree 2; code and text say Degree 3
- **Claim:** The lore doc disagrees with the code, the reference and the dialogue. The dialogue is right.
- **Evidence:** `docs/LORE_REFERENCE.md:345`: "...it needs a chance to settle during sleep; the player wakes at Degree
  2." Code: `ConcentratedBlood.canInject` needs `isDegree(player, 2)` and `completeSleep` calls
  `DegreeProgression.advance(player, 3)` (`ConcentratedBlood.java:43,66`); `HEMOMANCY_REFERENCE.md:587` (Degree 3 row);
  `hemomancy.initiation.awakened` (6294): "You wake as an Initiate of the Incarnadine Fane."; Vicar
  `votary.degree_hint`: "you will wake at the third Degree."
- **Confidence:** Verified.
- **Action:** fix the lore doc (move the sentence to the Votary block and say Degree 3).
- **User call:** no.

### DL-11 (Medium) Two §1.4 writers' questions are already resolved in code
- **Claim:** `docs/OPEN_QUESTIONS.md` §1.4 still lists them as open.
  1. "Archon whispers go to Degree 7 and above, so a D8 Apotheos or Silentium is still called Archon; Ancestral
     Communion has the same issue." Now: a Silentium hears nothing (`FungalWhisperEvents.java:87`,
     `PendingWhispers.hearsTheHive`), and an Apotheos gets the `.apotheos` variant of every line that names the
     Archon (`HarbingerPathPermissions.hiveAddressSuffix`; `FungalWhisperEvents.java:91-92`;
     `CardinalRiteCompletionEffects.java:714` for Communion; keys `whisper.archon.v0.line2.apotheos`,
     `v2.truth2.apotheos`, `communion.v2.line2.apotheos`, `v3.line3.apotheos` all exist).
  2. "The Artificer offers the Silent Archon vestments at D7 in the Weight of the Frame lineage." Now: the vestment
     recipes need `required_degree: 8` and the silent path (`armature_upgrade/blood_lust_to_silent_archon_*.json`),
     and the D7 recovery option is offered to a Silentium only (`HarbingerArtificerDialogueTrees.java:239-240`).
- **Residual text:** `hemomancy.artificer.assignment.weight_of_frame.first_upgrade` / `.full_set` still say
  "seventh-Degree lineage" for a list that includes Silent Archon; `hemomancy.artificer.item_inquiry.monolith_imbued_cloth.line2`
  ("It goes into Silent Archon vestment work once the frame's taken the Cornerstone") omits the Silentium
  requirement; `tooltip.hemomancy.silent_archon_set_bonus` (5238) says "Requires the Silent Archon choice" where the
  ability handler requires Silentium (`only_a_silentium`).
- **Confidence:** Verified.
- **Action:** update §1.4 and the pass-3 notes; rewrite the three residual lines. `spine_growth.degree8.canon` (still
  unsendable) and the `coreWitnessDialogue` Javadoc stay open.
- **User call:** no.

### DL-12 (Low) The Vesper advancement grants the Silent Archon's name before the memory is drawn in
- **Evidence:** `advancements.hemomancy.vesper_defeated.description` (342): "...endure both faces of Vesper, and claim
  the Silent Archon's name." Lore and `degree_8_silentium.description` put Silentium after absorbing the Memory of
  Vesper; the NPC `silent_absorb` endings say the refusal is unfinished at that point.
- **Confidence:** Verified.
- **Action:** rewrite (e.g. "...and carry his memory out").
- **User call:** no.

### DL-13 (Low) "Rite of Apotheos" in game, "Rite of Apotheosis" in lore
- **Evidence:** `hemomancy.rite.apotheos_rite.name` (6725) "Rite of Apotheos", also
  `advancements.hemomancy.degree_8_apotheos.description`, `dialogue.event.archon_choice_eighth_degree`,
  `whisper.core_witness.line3`; LORE_REFERENCE uses "Rite of Apotheosis" (3 places), the reference uses both. The
  `core_witness.line3` pin ("rite of apotheos") is a substring of "Rite of Apotheosis", so a rename is free.
- **Confidence:** Verified.
- **Action:** decide one name.
- **User call:** yes.

### DL-14 (Low) Non-canonical "Harbinger Order"
- **Evidence:** `hemomancy.vicar.adept.brotherhood_lore` (3176): "the early wars of the Harbinger Order". The only use
  in lang; canon is Hematic Order.
- **Confidence:** Verified. **Action:** fix. **User call:** no.

### DL-15 (Low) "Severance" for the bloodline Unbinding
- **Evidence:** `hemomancy.rite.hematic_unbinding.desc` (6772): "A severance rite that dissolves the caster's
  bloodline". Extends OPEN_QUESTIONS C-2 (which names only `silent_severance` and the cure route).
- **Confidence:** Verified. **Action:** rewrite ("A rite of unbinding..."); fold into C-2. **User call:** no.

### DL-16 (Low) Templates that say the wrong thing about the item
- **Evidence:** Monolith `fragment` template is reused for `memory_of_vesper.json`:
  `hemomancy.monolith.item_inquiry.fragment.line2` (3026) "Alone, it teaches nothing. It only resonates." The Memory
  of Vesper is exactly what teaches the Silentium refusal. Vicar `qliphoth.line1` (3271) opens "A Qliphoth Pome." and
  is also the Qliphoth Seed's answer (`vicar/hemomancy/qliphoth_seed.json`).
- **Confidence:** Verified. **Action:** give each item its own line. **User call:** no.

### DL-17 (Suspected, Low) "The Schism" exists only in the Communion
- **Evidence:** Communion variant 1 (`communion.option.the_schism`, `communion.v1.schism1-3`, from 1675) describes a
  Schism in which the Unstained are "what a cut branch becomes". LORE_REFERENCE has no Schism; §7.1 says the
  Unstained began as Harbingers who left. The Entity may slant facts (§2.1) but should not invent an event the
  canon lacks.
- **Confidence:** Suspected. **Action:** decide: add the Schism to LORE_REFERENCE §7.1/§2 as the hive's account, or
  reword. **User call:** yes.

### DL-18 (Low) An Acolyte frames the Church-wide refusal as personal
- **Evidence:** `hemomancy.trade.acolyte.refuse_harbinger` (1929): "I can't trade with you, and that's my own choice,
  nobody else's." Ruling Q-2 makes the refusal uniform for every Unstained speaker at Degree 1+.
- **Confidence:** Verified (text vs ruling). **Action:** rewrite ("It's how we keep the church clean, and I agree
  with it"). **User call:** no.

### DL-19 (Low) NPC_VOICES open decision 14 is stale
- **Evidence:** No `item_inquiry.clairaudiograph` key exists any more (the three clairaudiograph inquiries now use
  distinct `hemomancy.clinical.inquiry.*` keys), and `hemomancy.alchemist.votary.tendency_lore` (1476, the line that
  lists enzyme names as tendencies) is dead text (DL-4); the live tendency line is the Vicar's
  `votary.tendency_lore`, which keeps the two name sets apart.
- **Confidence:** Verified. **Action:** cut the dead key and close decision 14 in NPC_VOICES and §1.4. **User call:** no.

## 4. Voice

Aggregate metrics for every speaker, computed the same way as `voicestats.py`, sit inside the contrast-map targets;
the only out-of-range readings were rapport *option* titles (player text) that my first pass counted. Line-level
slips:

### DL-20 (Low) The Vein-Mason reads a control manual
- **Evidence:** `hemomancy.anchorite.d5.activation` (5703): "Distill and drink a Thelemic dose. Equip its memory and
  select it. Press your manipulation-use key to enable it. ... Crouch and press that key ... two-and-a-half times
  the strain." and `anchorite.d5.stop`. The sheet: "Avoid. Mechanics paragraphs where a verdict and one instruction
  would do."
- **Confidence:** Verified. **Action:** rewrite to a verdict plus one instruction and point to the ledger, keeping the
  pinned facts. **User call:** no.

### DL-21 (Low) Stock phrasing and unbracketed narration
- **Evidence:** Monolith `guidance.illuminatus.line2` (3021) "the mycelial network whispers of dormant power";
  `item_inquiry.fragment.line2` and `minecraft_relic.line1` "resonates"; `hemomancy.monolith.unworthy` (3037) is
  narration without brackets ("The monolith is silent... You have not yet proven yourself worthy."). Vicar
  `uninitiated.line1` (3302) is a stage direction without brackets.
- **Confidence:** Verified. **Action:** rewrite / bracket. **User call:** no.

### DL-22 (Low) The Alchemist and the Voyager share one image
- **Evidence:** `hemomancy.alchemist.court.lead.line1` (9657) "Our Latin's an outsider's catalog";
  `hemomancy.voyager.court.known.alchemist` (9653) "The Order's Latin is an outsider's catalog". Same metaphor in two
  voices, and "Latin" is a real-world name in-world.
- **Confidence:** Verified. **Action:** rewrite the Voyager's. **User call:** no.

### DL-23 (Low) Formatting against the shared rules
- **Evidence:** `vicar.item_inquiry.monolith.line2` (3264) "at about degree five"; `clinical.inquiry.later` (5974) and
  siblings "Degree-1", "Degree-2"; `message.hemomancy.mausoleum_ward.refused` / `chapel.refused` "eighth degree"
  (lower case); `whisper.sanctified.v1.follow` (3419) "the fargone" (proper noun elsewhere); "Masons Respite" (8 keys,
  no apostrophe) beside "Mason's Effigy". Rules: "the fifth Degree" or "Degree 5".
- **Confidence:** Verified. **Action:** fix; decide the Respite spelling (lore uses both). **User call:** yes (spelling only).

### DL-24 (Low) Shared inquiry templates that misfit their items
- **Evidence:** `mnemonist.item_inquiry.minecraft_archive.line1` (2922) "Ordinary pages..." answers amethyst shards,
  experience bottles and chiseled bookshelves; `alchemist.item_inquiry.minecraft_reagent.line1` (1374) "Common
  apparatus ... heat tolerance" answers gunpowder, magma cream and spider eyes; `vicar.item_inquiry.minecraft_relic.line1`
  (3250) "An old instrument of measure, direction or return" answers amethyst and bookshelves;
  `guardian.item_inquiry.absolution_dagger.line1` (2255) "Issued to every pledged Unstained" answers
  `annettas_absolution_dagger`; `mnemonist.item_inquiry.scar_pattern_general.line1` (2936) "A scar pattern schema"
  answers runic motif paper and the retired `scar_binder_upgraded`; `votary_wayfarer...minecraft_fieldkit` answers
  cooked cod. 31 refined memories share `memory_general` (sheet: vary the memory template). Extends NPC_VOICES
  decision 11.
- **Confidence:** Verified. **Action:** split the worst (amethyst, xp bottle, Annetta's dagger, motif paper).
  **User call:** no.

## 5. Item inquiries

### DL-25 (Medium) The Vein-Mason's Effigy inquiry never loads
- **Claim:** The file names `hemomancy:masons_effigy`; the block item is `hemomancy:mason_effigy`. Holding the Effigy,
  the D4 Main step's own station, gets nothing from its teacher.
- **Evidence:** `cicatrix_anchorite/hemomancy/masons_effigy.json`; `BlockInit.java:1069` and `:1370` register
  `mason_effigy`. Stranded lines: `hemomancy.anchorite.inquiry.effigy.line1/2` (5401-5402).
- **Confidence:** Verified.
- **Action:** fix (rename the file to `mason_effigy.json`); recheck the line against the Brazier lifecycle (it says
  "Burn the prepared motif at an Iron Brazier", which is fine).
- **User call:** no.

### DL-26 (Medium) Mnemonist memory coverage: six written answers are unwired, 41 live memories have none
- **Claim:** The memory teacher answers 70-odd memories, but six live ones have finished lines and no JSON, and 41
  more live memory items have no Mnemonist inquiry at all.
- **Evidence:** written but unwired (keys exist, no file): `memory_ironhearted` (2908), `memory_blackhearted`,
  `memory_hematic_rebuke`, `memory_hematic_impressment`, `memory_ferric_rampart`, `memory_ferric_spikes`, all
  registered in `ItemInit` and not retired. No inquiry: `memory_activation_potential`,
  `memory_crimson_flame_conjuration`, `memory_lignum_mortis`, `memory_crimson_coronation`, `memory_sovereign_instinct`,
  `memory_synaptic_storm`, `memory_living_circuit`, `memory_white_verdict`, `memory_furnace_veins`,
  `memory_phoenix_debt`, `memory_absolute_stillness`, `memory_rimebound_sentence`, `memory_lingering_blood_binding`,
  `memory_chain_blood_binding`, `memory_blood_lattice`, `memory_sanguine_marionette`, `memory_blood_needle_fan`,
  `memory_blood_needle_lance`, `memory_guided_blood_shot`, `memory_hematic_mortar`, `memory_sanguine_halo`,
  `memory_expansive_blood_cloud`, `memory_pursuing_blood_cloud`, `memory_sanguine_tempest`, `memory_soaring_updraft`,
  `memory_suspended_updraft`, `memory_expulsive_updraft`, `memory_canopy_mortis`, `memory_worked_lignum`,
  `memory_summon_avatar_arms/armor/legs/complete`, `memory_iron_choir`, `memory_funeral_bell`,
  `memory_carrion_communion`, `memory_penumbral_drift`, `memory_eclipse_well`, `memory_thread_ripper`,
  `memory_of_vesper` (plus `memory_thread`, a material).
- **Confidence:** Verified.
- **Action:** fix the six (add JSON files); write lines for the rest one speaker-pass at a time, or point them at
  `memory_general` as an interim.
- **User call:** no.

### DL-27 (Low) Important items no teacher answers
- **Evidence:** no inquiry anywhere for the Mycophant Tendril, the Covenant Waybill (the Hermit's first gift), the
  Charm of Vascularium, the Memory Thread (wanted by the Vicar's `thread_blessing` card) or the ancestral/unsigned
  ledger; the Voyager, who draws Relay Charts, has no `relay_chart` inquiry (three Court roles do); the Vicar, who
  teaches the Saints at Degree 3, answers Hallowed Residuum but not `saint_relic_*`. Already known: Fungal Spine
  (PT-78), Sanguine Quintessence (PT-39), Sporitic Thurible for Harbingers (PT-31; the Guardian does answer it).
- **Confidence:** Verified. **Action:** add inquiries where the speaker plainly owns the item. **User call:** no.

### DL-28 (Low) A Mnemonist inquiry promises a service no data offers
- **Evidence:** `hemomancy.mnemonist.item_inquiry.minecraft_archive.line2` (2923): "Bring me enough of these and I
  can index blood-memory instead of guessing at it." Shown for amethyst, paper, enchanted and written books,
  bookshelves and xp bottles; the only related trade is `mnemonist/books.json` (2 books for glow ink).
- **Confidence:** Verified. **Action:** rewrite. **User call:** no.

### DL-29 (Low) A second squid-on-leads line
- **Evidence:** `hemomancy.votary_wayfarer.item_inquiry.mnemonic_ambergris.line2` (3320): "So bring squid on leads".
  §1.4 names only the Voyager's `spawn_egg_mnemonic_whale.line2`; fix both together.
- **Confidence:** Verified (text). **Action:** fold into the §1.4 item. **User call:** already open.

## 6. Trade cards

### DL-30 (Suspected, Low) The Ringmaster's curiosity card and tag
- **Evidence:** `hemomancy.trade.circus_ringmaster.curiosity.line` (1980): "Bring me something strange: a relic, a
  trophy, wreck salvage." `tags/item/trade/curiosity.json` holds no relic (Saint relics are `never_sold`), and it
  includes goods other systems want: `minecraft:nautilus_shell` (the Wayfarer's card and the Founding Fane offering,
  DL-32), `minecraft:echo_shard` (the Chamber of Will rite offering), `hemomancy:salt_stained_voyager_log` (the
  Voyager's card).
- **Confidence:** Suspected (balance and wording). **Action:** decide the tag; drop "a relic" from the line.
  **User call:** yes.

## 7. Teaching text against the gate

### DL-31 (Medium) The Hermit's Degree 5 hint omits what seals the Degree 6 rite
- **Claim:** The Rite of the Bloodline Covenant stays sealed until the chapter `COVENANT_WRITTEN_IN_PLACE`: founding a
  Fane, or projecting your own blood into your line's Bloodwell while inside its Fane at Degree 5. The Hermit says
  only "Find allies who share your blood ... perform the Rite of the Bloodline Covenant". This also answers the
  pass-3 note that left the line unchecked.
- **Evidence:** `hemomancy.hermit.illuminatus.hint` (2316); gate: `HarbingerChapterMilestone.java:15`
  (`COVENANT_WRITTEN_IN_PLACE(6)`), `CardinalRiteActivation.java:164-172`, grants at
  `ConsecratedBloodwellBlock.java:261-267` and `CardinalRiteCompletionEffects.java:1127`. The Vicar's
  `covenant.found_bloodline` / `found_fane` lines do teach it.
- **Confidence:** Verified.
- **Action:** rewrite in the Hermit's voice: name the Fane and the Bloodwell once and send the player to the Vicar.
- **User call:** no.

### DL-32 (Medium) No line names the Founding Fane's Nautilus Shell
- **Claim:** The Founding Fane rite needs a Nautilus Shell in its brazier; the Vicar's two teaching lines list the
  Quintessence, Bloodwell and Stakes and stop.
- **Evidence:** `recipe/cardinal_rite/founding_fane.json:50` (`minecraft:nautilus_shell` in `brazier_signature`);
  `hemomancy.vicar.covenant.found_fane` (3211), `hemomancy.vicar.illuminatus.founding_fane_lore` (3217). No lang line
  mentions a nautilus except the Wayfarer's trade card, which competes for the same shell.
- **Confidence:** Verified.
- **Action:** fix: add the offering to `covenant.found_fane`.
- **User call:** no.

### DL-33 (Low) The Hermit Road "known" line still describes the old ledger reward
- **Evidence:** `VicarDialogueEvents.java:103-109` shows `hemomancy.dialogue.event.vicar_hermit_road_known` (2100) when
  the report is already filed: "The Vicar taps the ledger once. 'You already carry the proper record. Use it.'" The
  reward is now four Befouling Ash Trails (`:114`); the ledger-reward line (2101) is dead.
- **Confidence:** Verified. **Action:** rewrite ("This road is already in the record."). **User call:** no.

### DL-34 (Low) Our Lady names the Purity defense at the Clarity stage
- **Evidence:** `hemomancy.lady.discerning.v0.follow` (2511): "Let the Verdigris Aura weaken the blood-touched." At
  Discerning, Silver Ward has just switched on (`UnstainedMilestoneHandler.java:73-75`); canon: "Verdigris Aura is the
  early Purity defense; Silver Ward is its advanced Clarity form."
- **Confidence:** Verified. **Action:** rewrite to Silver Ward. **User call:** no.

## 8. Placeholder text

### DL-35 (Low) A placeholder reward line
- **Evidence:** `hemomancy.dialogue.event.alchemist_red_taxonomy_placeholder_reward` (2029): "The Red Taxonomy reward
  has not been decided yet." Unreferenced; the live reward line is `alchemist_red_taxonomy_reward`.
- **Confidence:** Verified. **Action:** cut. **User call:** no.

---

## Noticed in passing (outside dialogue, not counted)

- `recipe/gourdvine_tap.json` uses `minecraft:copper_ingot` in a blood machine, against the Vicar's own "Use no
  copper, because it is the Unstained metal" (`votary.rooted_vein.floor`) and the no-copper-on-blood-tech rule.
- `armature_consecration.json` asks for Hemolytic Plating and `sanguine_athanor.json` for Hemolytic Solution, both
  Unstained reagents, in Harbinger Working rites. May be deliberate.
- `zealot_accept_purification` (starter supply) and `scout_give_notes` are not in `DialogueEventSpeakers`, so a forged
  packet can claim them away from the speaker (the supply is once only). Same class as the playthrough suite's
  forged-event checks.
- Many recipe JSON files start with a UTF-8 BOM (e.g. `recipe/foul_paste.json`); harmless to the game loader as far as
  the tests show, but plain `json.load` rejects them.

## Not a problem (checked and fine)

- **Option targets:** every literal `nextNodeId` resolves to a node in its tree; the dynamic Circus school and Court
  node ids (`root + "history"`, `"join_" + refusal`, ...) are always added beside their options.
- **Events:** every other posted event id (90) has a route, including the dynamic families (station claims, clinical
  lessons, antecedent, Artificer briefs and fittings, Vein-Mason replacements, Court `court_member_*`, observances,
  starter memories, succession). Routes for `antecedent_*`, `artificer_*`, `vein_mason_*`, `circus_*` all match posted
  ids.
- **Lang keys:** no key referenced by a tree, inquiry, trade card or rapport line is missing from `en_us.json`; all
  dynamic families exist for their reachable states (clinical lessons and next steps, antecedent nodes, ending lines
  for 4 NPCs x 6 states, newcomer lines for every decorated speaker, Communion and whisper `.apotheos` variants,
  Court role keys). The Court roles other than the Giving Light lack `greet.transgressor` (and the Marrow-tender
  `amends.line1`), but `CourtMembershipRules.turnsAway` only ever turns players away at the Giving Light, so those
  states are unreachable.
- **Inquiry branches:** no branch is shadowed by an earlier unconditional branch, no empty or impossible degree range.
  Every inquiry speaker folder is queried by a live speaker id.
- **Trade data:** every card's title, line and `opens` key exists; each Court `opens` line names the role that
  actually offers JOIN (`CourtRole` offers); requirement lines match `requires` (Artificer husk "third Degree",
  Vicar shards "fourth Degree", Ringmaster bell "fourth Degree", Zealot amethyst for "our own", Alchemist Ferric draw
  "pay double" against `give_fine`). Trade-only goods (masks, curtain, rigging) are decor blocks, which is fine.
- **Canon:** no "Hollow Vessel" in player text (code comments only, D-17); no pronoun slips for Vesper, the Mycophant,
  Annetta, Erythravane or the four Saints; no God/Satan framing (only "holy" in denial or comparison); Unstained
  speakers say "the Church" for themselves; the founder-only cure (`zealot.bloodline_founded`, `plea.degree6.line2`)
  and the Monolith's fifth-Degree stand-in role (`guidance.illuminatus.line2`, `what_are_you.illuminatus`) follow the
  rulings; the Vicar's past-Archon lines stay hearsay; Our Lady never claims to outrank the Entity.
- **Teaching checked against code and data:** First Draws (5 samples, 3 of the six common species), First Separation
  (cattle, balanced spin, then syringe and rack, then one Alembic distillation), Concentrated Blood (inject at
  Degree 2, completed sleep, wake at Degree 3), the Brazier lifecycle (insert in either state, Blood Projection lights
  for 50 mL, held Absorption, 100 mL to learn, 50 mL to commit, sneak retrieves), the Effigy (500 mL per scar),
  Chamber visits (dream 1-2 minutes, guided 2 minutes, Seat 5/10/20 minutes, attunement only by the Degree 6 rite),
  the Woven Vessel turn-in (book, ink sac, three paper), the Blood Shot weave (bulb, 50 mL, one Vivacious), Rooted
  Vein's Dominion floor (eight chiseled Hematic Iron), Working floor blocks, the Bloodline Founding / Chamber /
  Vigil floors and offerings, the station upgrade kit rites (offering and anchor counts), Hemolytic Solution
  (Lethean Dew + Ghost Pipe), and the Court lesson and join gates (Degree 5).
- **Formatting:** no em or en dashes and no "D5" abbreviations in dialogue; `mL` spacing is consistent; the only
  added ellipses are the Scout's and the Communion name scene, both in the source text; no empty values or values
  equal to their key.
