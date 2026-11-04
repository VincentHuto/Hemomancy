# Docs, guidance and test drift audit (2026-11-02)

Read-only audit of where the written word (AGENTS.md, the two references, the topic docs, the wiki, the in-game books,
tooltips and advancement text) disagrees with the code, with itself or with the tests. Nothing was built, run or
edited; evidence comes from reading both sides with grep/sed and small read-only scripts. Line numbers are against
HEAD `8d01a316c` plus the working tree (which is clean apart from two `AD` index entries, see DC-78).

**Summary.** 89 findings: 11 High, 41 Medium, 37 Low; 12 need the user's call. AGENTS.md is close to the code (every
class, path and test it names exists); its drift is a few rules (`Mth.smoothstep` still used, archive-update instruction).
The reference has ~68 wrong numbers out of ~1,800 sampled, concentrated in §6 Unstained rules (purity values, the cure
trigger), §11 skills, §20-§22 items and armour (unbreakable armour, a Loom Blood Moon discount that does not exist) and
unmaintained rosters. LORE_REFERENCE keeps the 2026-10-31 rulings but carries invented gameplay (Blood Conduit tiers) and
three puppet counts; one ruling is broken in code (the Antecedent epilogue reaches a Silentium). The older wiki pages
(Blood-Systems, Getting-Started, Unstained-Path, Harbinger-Path) describe dozens of manipulations, items and abilities that
do not exist, list GeckoLib as required and give wrong default keys. Topic docs resolve but seven carry stale
"uncommitted" status lines. Tests are healthy (no broken doc pins), but two staged files would break the next commit and
one owed launcher does not exist. Area 6 (books, tooltips, advancement text) is only partly covered.

Conventions: **Severity** High = a player or contributor would be misled into wrong work; Medium; Low.
**Confidence** Verified = both sides read; Suspected = one side inferred. **Action** fix doc / fix code / decide.
Paths: `J/` = `src/main/java/com/vincenthuto/hemomancy/`, `R/` = `src/main/resources/`, `GT/` =
`src/gameTest/java/com/vincenthuto/hemomancy/gametest/`, `HR` = `docs/HEMOMANCY_REFERENCE.md`, `LR` =
`docs/LORE_REFERENCE.md`, `OQ` = `docs/OPEN_QUESTIONS.md`. "Known: X" means the item is already in OQ and is cited, not
re-reported.

---

## 1. AGENTS.md vs code

AGENTS.md is in good shape: every one of its ~70 class names, ~45 package/resource/tool paths and 39 test class names
resolves (list under "Not a problem"). The drift is in a handful of rules that the code or the index has moved past.

**DC-1** · Medium · Verified · fix code (or extend the guardrail) · user's call: no
"Never `Mth.smoothstep`" is broken in three renderers, and the guardrail that is meant to enforce it cannot see calls.
- AGENTS.md:40 "`HemoEasing` for easing and lerp (never `Mth.smoothstep`, which is quintic)".
- `J/client/render/layer/mob/endgame/VesperTransitionCocoonGeometry.java:38`, `J/client/render/world/CardinalRiteStaffTendrilGeometry.java:75`, `J/client/render/world/EarthenVeinFeedingTendrilGeometry.java:35`: `double animatedWeight = Mth.smoothstep(progress);` (and `pull = Mth.smoothstep(progress)`).
- `src/test/java/.../architecture/EasingCopyGuardrailTest.java:35` only matches private *definitions* (`\b(?:float|double)\s+(smoothstep|...)\s*\(`), so a direct `Mth.smoothstep(` call passes.

**DC-2** · Low · Verified · fix doc (OQ) · user's call: no
OQ C-8's first half is already resolved: AGENTS.md no longer names "three special rites".
- OQ:71 C-8 "AGENTS.md names three special rites (Apotheos, Bloom, Ancestral) but `CardinalRitePurposeDataTest.SPECIAL` holds four".
- AGENTS.md:79 now gives no count ("its floor style must match unless it is `"special": true`"). The count survives in HR:638 and `wiki/Harbinger-Path.md:138` ("The three Degree 7 rites that stand apart"), and `src/test/.../common/recipe/CardinalRitePurposeDataTest.java:50-51` holds four (`possession_rite` is D8, `recipe/cardinal_rite/possession_rite.json` `"required_degree": 8`, floor `qliphoth_grand`). Reword C-8 to the remaining HR/wiki/Liber part.

**DC-3** · Low · Verified · fix doc · user's call: no
"The `Hemomancy` constructor adds only mod-lifecycle listeners" is not what the guardrail pins.
- AGENTS.md:43.
- `J/Hemomancy.java:97` `NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);` (a game-bus event); `src/test/.../architecture/RegistrationStyleGuardrailTest.java:54` pins `List.of("this::commonSetup", "this::onAddReloadListeners")`. Say "mod-lifecycle listeners plus the reload-listener hook".

**DC-4** · Low · Verified · fix doc · user's call: no
`/hemo` is not one root with one `<Name>Subcommand` per branch: the dev-only `test` branch is a second registration.
- AGENTS.md:20 "`/hemo` is one root in `common/command/HemoCommand` (operators only) wiring one `<Name>Subcommand` class per branch".
- `J/common/event/CommonEvents.java:15-16` also calls `DevTestHooks.registerCommandsIfPresent`, which reflects into `GT/HemoTestCommands.java:18` `dispatcher.register(Commands.literal("hemo")...then(Commands.literal("test")...` (journey, scenario and `MausoleumTestCommand` branches). AGENTS.md:27 and `docs/TESTING.md` rely on it. Add one sentence naming the gameTest-only `test` branch.

**DC-5** · Low · Verified · fix doc · user's call: no
"The style-level selector test" names no test.
- AGENTS.md:79 (repeated word for word at `docs/CARDINAL_RITES.md:58`) "Keep `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest` and the style-level selector test green".
- No class matches; the nearest is `src/test/.../common/rite/CardinalRitePurposeTest.java:28` `floorStyleIsThePathBeforeTheTier()` / `:42` `floorMustMatchPurposeUnlessSpecial()`. Name it.

**DC-6** · Low · Verified · fix doc · user's call: no
ARCHITECTURE_STANDARDS is described as "the index of every guardrail test", but four guardrails AGENTS.md itself names are not in it.
- AGENTS.md:8; AGENTS.md:79-80 name `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest`, `RecipeMapPurposeGroupDataTest`, `SpecialFloorDataTest`.
- `docs/architecture/ARCHITECTURE_STANDARDS.md` has none of the four (grep). `src/test/.../architecture/ArchitectureStandardsIndexTest.java:25-35` only enforces classes inside `architecture/`, so these live in `common/recipe`, `common/rite/floor` and `client/screen/skilltree/shared` unchecked.

**DC-7** · Low · Verified · fix doc or move code · user's call: no
"Manipulation implementations live under `common/manipulation/<tendency>/`" has two exceptions beyond the shared folders.
- AGENTS.md:46.
- `J/common/manipulation/ferric/FerricBarrierManip.java` serves `ferric_rampart`, registered DUCTILIS; `J/common/manipulation/animus/SovereignInstinctManip.java` serves `sovereign_instinct`, registered DUCTILIS (`J/common/init/ManipulationInit.java:738-740`). The shared conjure class `ferric/StaffWeaponFormManip.java` (seven tendencies) and the `saint/` folder are reasonable exceptions that the rule should mention.

**DC-8** · Low · Verified · fix doc · user's call: no
Two small wording slips.
- AGENTS.md:46 "many also define Drudge AI helpers with `setDrudgeAction(...)`": the helpers live in `J/common/manipulation/DrudgeActions.java` and are attached in `ManipulationInit` (e.g. `:54 .setDrudgeAction(DrudgeActions::bloodShot, ...)`), as AGENTS.md:42 already says.
- AGENTS.md:22 lists the send facade as six methods; `J/common/network/PacketHandler.java:535` also has `sendToPlayerAndTrackers`.

AGENTS.md:81 (update `docs/archive/done/MNA_COMPATIBILITY_BRAINSTORM.md`) is reported under area 8 (DC-85).

---

## 2. Topic docs vs code

Every "Where it lives" pointer in the 18 topic docs resolves to a real class, data folder, generator, review-client row
or GameTest namespace, with three exceptions that are git-ignored (DC-89). The drift is in status lines, a few
counts and a handful of rulings the code has moved past. Samples: ≥5 rulings per doc, about 400 numbers across the
group (matched list at the end).

**DC-9** · High · Verified · fix doc · user's call: no
HEMATIC_SUCCESSION_DESIGN_SPEC, which the reference calls "the design contract", still makes the Vacant Effigy a floorless Blood Structure; it is a Working craft.
- `docs/HEMATIC_SUCCESSION_DESIGN_SPEC.md:76` "D5 blood-structure craft using Sanguine Formation and 250 mL personal blood. The horizontal 3×3 pattern…"; `:77` "The manufacturing structure is external to the Cardinal Rite and does not occupy its ritual floor"; also `:49`, `:294`. HR:5374 cites the spec as the contract.
- `R/data/hemomancy/recipe/cardinal_rite/working/vacant_effigy.json`: `"purpose": "making"`, `"floor": "hemomancy:working_greater"`, `"required_degree": 5`, `subject.feed 250`; not in the `WorkingCraftRosterDataTest` floorless set. Lang `hemomancy.succession.teaching.artificer` and `wiki/Hematic-Succession.md:9` already say "Greater Working floor".

**DC-10** · Medium · Verified · fix doc · user's call: no
Succession offerings are described as floor sockets; they are brazier offerings.
- `docs/HEMATIC_SUCCESSION_DESIGN_SPEC.md:141` "The first three marked floor sockets hold donor blood, officiant blood, and Ambergris"; `wiki/Hematic-Succession.md:49` "Leave socket 3 empty".
- `R/data/hemomancy/recipe/cardinal_rite/hematic_succession.json` and `mnemonic_restoration.json` carry a `brazier_signature` (2 `bloody_vial` + 1 `mnemonic_ambergris`); HR:682 reads braziers clockwise; the spec's own `:241` says "remain in their braziers".

**DC-11** · Medium · Verified · fix doc · user's call: no
"Uncommitted" status lines on work that is committed. The working tree is clean (only the two `AD` index entries), and OQ:4 says "Everything those passes built is committed".
- `docs/BRINED_COURT.md:3` "all 11 tracks built, uncommitted, in the working tree since 2026-10-30" (committed `62f5b38da`).
- `docs/CIRCUS.md:3` "built and uncommitted in parts".
- `docs/NPC_DIALOGUE_AND_TRADE.md:3` "uncommitted" (committed `ea7bd732d`).
- `docs/MANIPULATIONS_AND_CASTING.md:3, 34-36, 120, 137, 138` (cast impacts, basics, infusion veins: committed `3fae723d0`, `71f06de03`).
- `docs/PLAYTHROUGH_TESTS.md:3, 136, 156, 172, 184, 202` (34 files under `GT/playthrough/` committed in `8d01a316c`).
- `docs/SAINT_MAUSOLEUMS.md:126` "Uncommitted in the tree: only two generator docstrings…" (both committed).
- `docs/CARDINAL_RITES.md:3, 113` "the seven design specs under `docs/superpowers/specs/` are still untracked" (all 86 specs are tracked) and "rite packages were last touched by `31e640b83`…" (latest is `8d01a316c`).

**DC-12** · Medium · Verified · fix doc; decide the axe · user's call: yes (axe only)
The manipulation roster table is copied from the archived balance plan and no longer matches `ManipulationInit`.
- `docs/MANIPULATIONS_AND_CASTING.md:13` "Animus | 23 | 7 | 7 | 6 | 2 | 1", `:15` "Ferric | 15 | 5 | 4 | 3 | 2 | 1", `:16` "Mortem | 15 | 4 | 4 | 3 | 2 | 2" (source: `docs/archive/audits/MANIPULATION_BALANCE_PLAN.md` §11.2).
- `J/common/init/ManipulationInit.java:848` `crimson_tithe` is `EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS`; `:300` `conjure_axe` is FERRIC. Real counts of the 108 active: Animus 24 (7/7/7/2/1), Ferric 16 (5/5/3/2/1), Mortem 13 (4/3/2/2/2). HR:1489 still files `crimson_tithe` under Mortem and HR:1959 says "axe is Mortem" while HR:1828 says Ferric (see DC-43).

**DC-13** · Medium · Verified · fix doc · user's call: no
The casting review harness path is wrong in the topic doc and the wiki.
- `docs/MANIPULATIONS_AND_CASTING.md:58` and `wiki/Casting-Animations.md:306, 309` "`tools/oneoff/casting-animation-review.init.gradle`".
- The tracked file is `tools/review/casting-animation-review.init.gradle` (`tools/review/README.md:10`); nothing under `tools/oneoff/` has that name. `docs/MANIPULATIONS_AND_CASTING.md:58` also writes the row key as `runLoomReviewClient` (row is `loomReviewClient`, `gradle/review-clients.gradle:105`).

**DC-14** · Medium · Verified · fix doc and resources · user's call: no
STATION_UPGRADES says "nine" upgrade lessons and that dense station pages set `decal: false`; the Condenser added two lessons and its pages lack the flag.
- `docs/STATION_UPGRADES.md:95` "The nine upgrade pages … use this setting"; `:97` "The Liber's nine authored upgrade lessons…".
- `J/.../LiberEntryDefinitions.java:100-101` also registers `first_clotting` and `druse_crystallization` (11 lessons). `R/data/hemomancy/books/libersanguinium/the_hematic_order/pages/first_clotting.json`, `druse_crystallization.json`, `sanguine_condenser.json` have no `"decal": false`; the nine listed pages do.

**DC-15** · Medium · Verified · decide · user's call: yes
OQ D-15 asks a question the NPC topic doc records as already ruled and shipped.
- `docs/NPC_DIALOGUE_AND_TRADE.md:87` ruling: "The Unstained Scout goes through the dialogue hub (one card, `scout/paper`, and two rapport lines)".
- OQ:40 D-15 "does she get trade cards and lore lines of her own?" default "No cards". Data ships `R/data/hemomancy/npc_trade/scout/paper.json` and `J/.../DialogueRapportLore.java:61 add(lines, "scout", ...)`. Confirm the ruling, then delete D-15.

**DC-16** · Medium · Verified · fix doc · user's call: no
The Siphonophore's tendency is wrong in the ecology doc and the lore.
- `docs/PELAGIC_ECOLOGY.md:25` "| Siphonophore | Congeatio | Ordinary |"; LR:1243 "Siphonophore Congeatio" (LR:1431 says Ductilis).
- `R/data/hemomancy/blood_profiles/siphonophore.json` `"tendencies": ["ductilis"]`; HR:5480 agrees.

**DC-17** · Medium · Verified · fix doc · user's call: no
PELAGIC_ECOLOGY says the new animals keep empty kill tables; four of six have drops.
- `docs/PELAGIC_ECOLOGY.md:18` "the other six new animals retain their empty kill tables. No extra processing chain is added."
- `R/data/hemomancy/loot_table/entities/chiton.json` (`chitinous_husk`), `pelagic_herring.json` (`raw_pelagic_herring`), `siphonophore.json` (`ganglion_cluster`), `bloody_belly_comb_jelly.json` (`cuttlefish_chromatophores`); only `pyrosome` and `hagfish` are empty.

**DC-18** · Medium · Verified · fix doc · user's call: no
The Wake nursery placement rule is current in BRINED_COURT but stale in the reference and PELAGIC_WORLDGEN.
- HR:4638 and `docs/PELAGIC_WORLDGEN.md:167` "floor at or above Y 13, searching 64 blocks round its cell; every valid cell".
- `J/common/worldgen/structure/court/CourtPlacementRules.java:39` `NURSERY_MIN_FLOOR = 18`, `:47` `NURSERY_SEARCH_RADIUS = 8`; `CourtSite.WAKE_NURSERY` rarity 6; `docs/BRINED_COURT.md:63, 89` has these.

**DC-19** · Low · Verified · fix doc · user's call: no
OQ §1.4's whisper item is stale.
- OQ:79 "`FungalWhisperEvents` sends the Archon whispers to Degree 7 and above, so a D8 Apotheos or Silentium is still called 'Archon' in them".
- `J/common/event/.../FungalWhisperEvents.java:87` `if (!PendingWhispers.hearsTheHive(serverPlayer)) return false;` (a Silentium hears nothing) and `:91-92` passes `PendingWhispers.hiveAddress(serverPlayer)`; `J/common/capability/player/harbinger/degree/HarbingerPathPermissions.java:26-27` returns `".apotheos"` for an Apotheos. Only the Ancestral Communion half of that bullet may still stand.

**DC-20** · Low · Verified · fix doc (OQ) · user's call: no
Known: C-6, half resolved. `R/data/hemomancy/tags/item/trade/never_sold.json:11-14` already lists `recollection_hemorath/seraphae/putriciel/velorum`; only the Loom D8 gate half of C-6 remains.

**DC-21** · Low · Verified · fix doc · user's call: no
OQ §1.6 says "the topic doc has the evidence and the probe test names", but five PT rows point at topic docs that carry nothing for them.
- OQ:99; PT-3/PT-5 → NPC_DIALOGUE_AND_TRADE (`:106` lists only D-35…D-87), PT-7 → NPC_VOICES, PT-13 → STATION_UPGRADES, PT-25 → MANIPULATIONS_AND_CASTING, PT-32 → CARDINAL_RITES. The evidence is only in `docs/PLAYTHROUGH_TESTS.md:232-262`. Point those rows at PLAYTHROUGH_TESTS or add the rows.

**DC-22** · Low · Verified · fix doc · user's call: no
Half the docs the README lists as topic docs do not have the topic-doc shape, so their open items have nowhere to live.
- `docs/README.md:18-21` "Each one says what the system is, where it lives …, the rulings that bind it, the calls that were made without the user, what is still open, what proof is owed, and where the history is"; `:70-72` new topic docs use those headings.
- None of the seven headings exist in MORPHLING_STRAINS, PELAGIC_WORLDGEN, PELAGIC_ECOLOGY, NPC_VOICES, STATION_UPGRADES, MANIPULATION_VISUALS, HEMATIC_SUCCESSION_DESIGN_SPEC or DEEP_DARK_RECORDING_REWRITE (grep for `## …`). Either add the sections (STATION_UPGRADES and HEMATIC_SUCCESSION at least, which OQ points at) or mark these in the README as contracts/specs rather than topic docs.

**DC-23** · Low · Verified · fix doc · user's call: no
NPC_DIALOGUE_AND_TRADE counts and ids that drifted after `ea7bd732d`.
- `:33`, `:102` "56 rapport lines over 28 speakers" vs `J/.../DialogueRapportLore.java:49-80` 29 rows (58 lines; the Scout row at `:61` is new).
- `:55` "payload-registry.txt (protocol 30)" vs `J/common/network/PacketHandler.java:74` `PROTOCOL_VERSION = "31"`, `src/test/resources/network/payload-registry.txt:3` `version 31`.
- `:35` "Six cross-NPC links … (four pairs)" vs seven links over five pairs (`npc_trade/votary_wayfarer/prismarine.json` gives `erythrocoral_fragment`, wanted by `alchemist/reef_salts.json`).
- `:97` "`DialogueLoreFlagTest` floor 66 to 64" vs `DialogueLoreFlagTest.java:37 MIN_REACHED = 62`.
- `:11` says `DialogueEventSpeakers` pins the Vicar for `vicar_begin_initiation`/`vicar_release_charm`; those are enforced inline at `J/common/network/dialogue/DialogueOptionPacket.java:81-86`, and the doc omits `recruit_harbinger`/`expel_harbinger` (`DialogueEventSpeakers.java:43-44`) and drops the `vicar_`/`alchemist_`/`mnemonist_` prefixes from several event ids.

**DC-24** · Low · Verified · fix doc · user's call: no
CARDINAL_RITES states the special-floor rule without its exception and omits one suite.
- `docs/CARDINAL_RITES.md:18` "only a `special` rite of that purpose may name it", then Possession (`bond`) sits on `qliphoth_grand` (`world`). `J/common/rite/CardinalRitePurpose.java:68 if (special) return null;` lets any special rite use any floor; the purpose pairing is only `SpecialFloorDataTest.java:31 BORROWED_FLOORS = Map.of("possession_rite", "qliphoth_grand")`. HR:666 has the same wording.
- `:50`, `:115` omit `GT/RiteSpaceGameTests` (`rite_space_validation`, alpha, `gradle/game-tests.gradle:30`).

**DC-25** · Low · Verified · fix doc · user's call: no
CIRCUS detail drift.
- `:29` "at most four per pavilion": challenge bits are player-global and never reset (`CircusPlayerProgress.completeChallenge`), so the cap is four per player.
- `:25` "all five challenge bits (…four names…)": bit 1 is the route choice (`CircusRingmasterEntity.handleChoice` → `completeChallenge(player, 1, 50)`).
- `:23` milestone list omits 50 per performer first talk (`CircusPerformerEntity.java:276`), 150 for the attention act (`CircusPerformanceController.java:120`) and 50 for the route choice (`CircusRingmasterEntity.java:197`).
- `:45` "`/hemo test circus run`": the command is `/hemo test journey circus run` (`GT/HemoTestCommands.java:53-60`).
- `:49` omits `worldgen/structure_set/troupe_abandoned_stages.json` (32/10), which PT-58 depends on; `:81` omits PT-21 and D-14, which OQ files under this doc; `:65` C-04 ruling ("Saints are the 1.1 expansion; the sea faction follows") is overtaken (related to known C-1).
- `docs/PLAYTHROUGH_TESTS.md:61` "The D5 referral": the referral gate is D4 (`J/.../CircusIntroductionRules.java:4 MINIMUM_DEGREE = 4`); the band tests it at D5.

**DC-26** · Low · Verified · fix doc · user's call: no
SAINT_MAUSOLEUMS, ENDGAME_PATHS, BRINED_COURT and PELAGIC_WORLDGEN pointer slips.
- `docs/SAINT_MAUSOLEUMS.md:112` and HR:1248 name `DATA_MARTYRDOM`; the field is now `DATA_STILL_GUARD` (`VelorumEntity.java:69`); the MARTYR names left are `VelorumVisualRules.MARTYRDOM/MARTYR` (known C-5 for the rename itself).
- `docs/SAINT_MAUSOLEUMS.md:76` ruling "the outer trial halls open D3 to D4" vs its own `:13` "there is no trial out there any more"; `:32` "Eleven generator-authored blocks" omits `witness_devotee`, and `sealed_niche`/`opened_niche` share `tools/model_export/mausoleum_niche.py`.
- `docs/ENDGAME_PATHS.md:37` omits `J/common/world/GardensTerrainRules.java` (bridges, gills); `:46` "LORE_REFERENCE.md lines 34 to 41, §3 degree table": the degree table is LR §6.4 (322-336).
- `docs/BRINED_COURT.md:25` "HR §29, 'Court discovery'": the heading is "Learning of the Court" (HR:4687); `:28` "each with … inquiries": `CourtRole.SLEEPER` has none (17 inquiry folders for 18 roles); `:38` omits `CourtSeatSearch` (Relay Chart search).
- `docs/PELAGIC_WORLDGEN.md:13` "adds six mobile species" vs `PELAGIC_ECOLOGY.md:18` seven; `:179` floor-variation list omits the nursery (8).

**DC-27** · Low · Verified · fix doc · user's call: no
BOSSES, NPC_VOICES, MANIPULATION_VISUALS and the engram-weaving guide.
- `docs/BOSSES.md:7` Saints and Court "(both being written separately)" (both topic docs exist); `:20` wound thresholds are in `VesperCombatRules.java:19 ANCHOR_THRESHOLDS`, not `VesperWoundRules`; `:14` "HR §Naeglerophaeon" is a paragraph inside §28.5 (HR:4565). Pronoun mix at `:70, 73, 91` is known D-16.
- `docs/NPC_VOICES.md:203` lists `hemomancy.trade.vicar.thanks_fine`; no Vicar card has a FINE grader, so the line can never show.
- `docs/MANIPULATION_VISUALS.md:15` "`smooth` … `easeInOut`" vs `HemoEasing.smoothstep` / `easeInOutCubic` (`HematicRiposteGeometry.java:50, 54`).
- `docs/engram-weaving/README.md:154-156` valve counts per rank: `EngramBoardGenerator.java:392-403` adds a 50% extra off-path valve (Summa 1-3, Magister 2-4) and a decoy prism from Magister; Perfectus fragments and scars are omitted; `:69, 144` "keyed faces (Summa and up)" is not guaranteed (`:73, 79` fallback drops keying).

DEEP_DARK_RECORDING_REWRITE, DEPENDENCY_PROVISIONING and MORPHLING_STRAINS have no findings (see "Not a problem").

---

## 3. HEMOMANCY_REFERENCE.md

About 1,800 values sampled across the three thirds of the doc (§1-§5: ~316, 294 match; §6-§19: ~975, 950 match;
§20-end: ~504, 483 match), so roughly 68 wrong numbers, plus about 60 list, name and status mismatches. The big errors are concentrated: §6 Unstained rules, the §11 skill table, the §20-§22 item and
armour numbers, and roster/list sections (§14, §26, §31-§36) that stopped being maintained.

### Preamble and §1-§5

**DC-28** · High · Verified · fix doc · user's call: no
§5.9 still says Qliphoth pomes drop as invulnerable items; they ripen on the Bloom and only the owner picks them.
- HR:1282 "may attempt `trySpawnPome()` for each bloom (1-in-80 chance)"; HR:1286 "Pomes are invulnerable (fire/lava/void) and never despawn (`lifespan = Integer.MAX_VALUE`)".
- `J/common/rite/harbinger/QliphothBloomEvents.java:112-127` `tryRipenPome` → `data.setPendingPome(bloom, huskIndex)` (no item spawned); `J/common/block/harbinger/functional/QliphothBloomBlock.java:153-245` `pickPendingPome` gives it to the owner; `J/common/init/ItemInit.java:984-986` the pome is a plain food item. The 1-in-80 chance, 40-tick cadence and cap of 9 match.

**DC-29** · Medium · Verified · fix doc · user's call: no
Contributors are pointed at classes that no longer hold the code.
- `CardinalRiteEvents` (HR:592, 698, 1274, 1288, 1320, 4909, 4947) does not exist: `completeBloomOfQliphoth` is in `J/common/rite/harbinger/CardinalRiteCompletionEffects.java:870`, `syncQliphothBlooms` in `HarbingerCardinalRiteEvents.java:853`, the degree/communion gates in `CardinalRiteActivation.java:71-73, 211`; `BloodCraftingKeyPressPacket.java:47` only calls `CardinalRiteActivation.tryStart`.
- HR:1269 Monolith shatter "interacts … twice (`SHATTER_INTERACTION_THRESHOLD = 2`)": the constant does not exist; shatter is the dialogue option `monolith_press_further` (`SanguineMonolithDialogueTrees.java:24, 140` → `SanguineMonolithBlock.java:230-241`).
- HR:1515 `ClientEvents.renderEntities` (renderers are in `J/client/registry/EntityRendererRegistrar`); HR:1213 `SarcophagusBakedModel` (no class; `client/render/tile/SectionedBakedModel.java` is the likely successor); HR:688 `SHOW_FOCUS_BRAID` "implemented but switched off" (removed in `f20ec4165`).

**DC-30** · Medium · Verified · fix doc · user's call: no
§3.1 server config: one wrong default, one wrong key, six wrong ranges, and whole sections missing.
- HR:402 `vascularDebuffsEnabled` default `true` vs `J/config/HemoServerConfig.java:265` `.define("vascularDebuffsEnabled", false)`.
- HR:456 `claimedWillBonusCapSilentArchon` vs `:557` `defineInRange("claimedBonusCapSilentArchon", 1, 0, 8)` (an admin would edit a key that does nothing).
- Ranges: `terrainMultiplier`, `bloodMoonMultiplier`, `heraldMultiplier` HR:442-445 `0.0-16.0` vs `:501, 505, 513` `0.0, 25.0`; `bloodDrunkennessMultiplierPerAmplifier` `0.0-8.0` vs `:509` `10.0`; `anchorLifetimeTicks` HR:446 `20-600` vs `:517 1, 1200`; `falterWindowTicks` HR:449 `20-1200` vs `:529 1, 1200`.
- Missing sections: `succession`, `hematic_salvage`, `tendencyBossMultiplier`, morphling hunger/fed/cradle keys, `mycophant_encounter`, `alembic_leak`, `drudge`, guardrail bandwidth keys, `adipocere_crust` (`HemoServerConfig.java:133-582`); HR:409-411 "*(see code)*" for 0.5 / 100 / 0.25. §3.2 omits `microscope`, `boss_presentation`, the `powergen` HUD keys, `useTexturedDialogueStyle` and eight `world_rendering` keys (`HemoClientConfig.java:60-179`); HR:470 `location` is under `powergen`.

**DC-31** · Medium · Verified · fix doc · user's call: no
§5.4 Station Upgrades describes four stations; the catalog has seven.
- HR:785 "these four", :791, :793 "The Ghastly Alembic, Resonant Forge, Hematic Armature, and Enzymatic Scriptorium share one upgrade system", :817 "Every upgrade rite is `greater` (the Alembic's first is `lesser`)" and "exempts these eight rites"; table HR:803-814 has 10 rows.
- `J/common/tile/harbinger/crafting/upgrade/StationUpgradeCatalog.java:31-73` 14 tiers including `LOOM` (ganglionic_weave D5, cortical_weave D6) and `CONDENSER` (first_clotting D3, druse_crystallization D5); `recipe/cardinal_rite/first_clotting.json` is `"riteType": "lesser"`. `docs/STATION_UPGRADES.md` has the full table.

**DC-32** · Medium · Verified · fix doc (or code) · user's call: yes
`vitality_chalice` is listed as an emergency blood restore; it is a plain item with no use.
- HR:573 "Direct emergency restores (`blood_rock`, `bloody_flask`, `vitality_chalice`) apply **Blood Drunkenness**".
- `J/common/init/ItemInit.java:294-295` `vitality_chalice … () -> new Item(new Item.Properties())`; only `BloodyFlaskItem` (blood_rock 250, bloody_flask 2500, `:711-714`) calls `CheapBloodInfusionHelper`.

**DC-33** · Medium · Verified · fix doc · user's call: no
Small §2 and §5 rule errors.
- HR:347 Last Rite ids "`hemomancy:ink_mantle`, `hemomancy:last_light`, `hemomancy:silent_refusal`" vs `J/common/event/LastRiteHelper.java:20-23` `INK_MANTLE_ID`, `SILENT_REFUSAL_ID`, `CRYPTOBIOSIS_ID`, `PHOENIX_DEBT_ID` (no `last_light`; HR:2656 and :1892 already use the new two).
- HR:712 the Hermit's D1 row "grants Degree 2 on return" vs HR:206-207, :223 (the Vicar grants D2) and `HarbingerHermitDialogueTrees.java:114-137` (hint and lore only).
- HR:1064 Founding Fane "40-second activation" vs `recipe/cardinal_rite/founding_fane.json:143` `"target_duration_ticks": 3600` (180 s).
- HR:900-904 Fungal Dimension bullets "The player has no physical body here", "Whispers occur almost constantly", "puncturing the core severs the connection" vs HR:894, :1022 and `FungalProjectionRules.java:20` (an Apotheos travels in the body); no Gardens whisper cadence or core code (the Core is known D-7).

**DC-34** · Low · Verified · fix doc (or code for the theme) · user's call: yes (theme)
§5.7a Chamber details.
- HR:1145 "`BASE_ROOM_RADIUS = 4`" vs `ChamberOfWillManager.java:85 = 3` (the tier base 4 is `ChamberProgressionRules.java:8`); see DC-70 for the degree-vs-tier sizing.
- HR:1158 Lowtide theme "Degree 6 plus the Vein-Mason first-effigy loadout | … | 1" vs `ChamberProgressionRules.java:19` `if (facts.firstScarLearned()) return new State(0, "mnemonic_lowtide");` (tier 0, no degree check; the manager uses `isVeinMasonFirstScarLearned`, `ChamberOfWillManager.java:330`).

**DC-35** · Low · Verified · fix doc · user's call: no
The dated preamble and protocol numbers read as current.
- HR:13 "(`a4ab49edb`, current HEAD) … There are no committed changes after October 1" (HEAD is `8d01a316c`); HR:43 "153 profiles, with 87 restricted" (183 profiles, 114 restricted); HR:828 "317 inquiry entries" (385; the NPC list omits `cicatrix_anchorite` and 18 `court_*` ids); HR:824 Voyager inquiry list 5 items (14); HR:132 "99 active powers" (123 registered, 108 active).
- HR:1239 "Protocol 16 requires matching client/server builds" (and history at :997, :1252-1258) vs `PacketHandler.java:74` `"31"`; HR:540 "Bidirectional / mixed" (only `AntecedentEffectPacket`).
- HR:322 "7 vein sections" vs five (`EnumVeinSections.java:4`; HR:2107 says five).
- Smaller: HR:729 lists a separate "Chisel Station" (lang `en_us.json:1207`: the old name of the Scarring Station); HR:203 "(advancement: *Strange Seeds*)" (the root is "The Red Thread"; "Strange Seeds" is a `HemoMilestone`); HR:210 Liber "(bookshelf + Sanguine Formation)" vs `blood_structure/liber_sanguinum.json` (8 Befouling Ash Trails, 100 blood, D1); HR:663 Communion floor list omits Visitation (`visitation_rite.json`, `communion_lesser`); HR:1340 omits the `pome_communion_progress_by_bloom` key; HR:1526-1534 omits four boss opening scenes (Phlegethosoma, Bloom Warden, Kindled Champion, Hydra).

Known, cited: C-1 (HR:1246, 1250), C-3 (HR:1182), C-5 (HR:1248, 1252), C-8 (HR:638, 666), §1.4 whisper interval (HR:864), PT-67 (HR:1131), D-7, D-5, D-1.


### §6-§19

**DC-36** · High · Verified · fix doc (and the class Javadoc) · user's call: no
§6.4 Purity Sources: 11 of 19 values are wrong.
- HR:1631 "+2.0" Hemomancy mob, :1632 "+0.5" undead, :1633 "+0.25" hostile, :1634 "+0.5 extra" flawless, :1645 "+3.0" sleep, :1652 "+0.3" breeding, :1653 "+0.05" planting, :1654 "+0.2" tamed heal, :1661 "+0.5" abstinence, :1662 "+0.15/min" empty blood, :1655 "2+ hearts".
- `J/common/capability/player/unstained/PurityGainEvents.java:62-82` `0.75f`, `0.25f`, `0.10f`, `0.25f`, `5.0f`, `0.75f`, `0.10f`, `1.0f`, `1.0f`, `0.30f`; `:308` `getAmount() >= 2.0f` (1 heart). The class Javadoc (`:29-56`) still lists the old values (constants changed in `084d2fc46`). The pacing cooldowns in `UnstainedPacingRules.java:4-6` (XP 100t, crops 200t, pet heal 600t) are undocumented.

**DC-37** · High · Verified · fix doc (§6.5, §6.7, §7, §15.2) · user's call: no
The reference puts the cure (blood magic stripped) on Clarity Ascension; the code does it at the first Rite of the Closed Vein.
- HR:1670 the Clarity rite "permanently disables blood magic (`BloodVolume.active = false`)"; :1712, :1727 stripping "when Clarity is confirmed"; :1729 Clarity "calls `enforceHarbingerResetOnClarity()`"; :2525 Closed Vein is just "Reusable, non-breaking".
- `J/common/rite/unstained/UnstainedCardinalRiteEvents.java:362-363` first Closed Vein at Purified calls `PathMutualExclusionHelper.completeUnstainedCure` (`PathMutualExclusionHelper.java:117-160`: degree stripped, bloodline left, `setActive(false)`, `setBloodVolume(0)`, loadout and summons cleared, `baselineRestored`). `enforceHarbingerResetOnClarity` is called only from `J/common/command/UnstainedSubcommand.java:152, 186, 219`. HR:2600 already has the right order.

**DC-38** · High · Verified · decide · user's call: yes
Tier-3 scars: server, UI and reference give three different gates.
- HR:2306 "available at Degree 5 (planned: move gate to Degree 6)".
- Server: `J/common/network/capa/harbinger/scars/PacketScarCraftingEvent.java:71-72` refuses tier ≥ 3 without `skill_deep_inscription`, which is `setRequiredDegree(6)` (`J/common/init/skills/ScarSkillBranch.java:30-31`). UI: `ScarsTabView.java:269`, `ScarsTabController.java:155` show `tier >= 3 ? 5 : 4`. Deep Inscription is missing from §11.

**DC-39** · Medium · Verified · fix doc · user's call: no
§15 says several Unstained rites grant Still Arts; none do, and Silver Veil grants Verdigris Aura, not Silver Ward.
- HR:2496 "directly grants **Silver Rebuke**", :2525, :2534, :2535, :2537 (Lethean Mute, Still Pulse, Pale Diagnosis, Memory Shear, Absolving Step, Quietus Bell, Autoimmune Edge); HR:2521 Silver Veil "Grants Silver Ward effect (30 min, amplifier 1)".
- Still Arts come only from advancements and the login backfill (`KnownStillArtEvents.java:62-72` via `StillArtRewardTable.java`); `UnstainedCardinalRiteEvents.java:305-310` Silver Veil is `addPurity(10.0f)` plus `EffectInit.verdigris_aura`.

**DC-40** · Medium · Verified · fix doc · user's call: no
§11 skill table: 7 wrong degrees, 2 wrong parents, and 37 of 74 skills missing.
- Feeding Frenzy HR:2167 3 → 4 (`CoreSkillBranch.java:52`); Crimson Mastery :2180 4 → 5 (`:64`); Vital Link :2182 5 → 6 (`:68`); Unbound Siphon :2172 5 → 6 (`LivingStaffSkillBranch.java:54`); Arterial Cadence :2173 5 → 6 (`:58`); Scar Affinity :2184 4 → 3 (`ScarSkillBranch.java:18`); Scar Mastery :2186 5 → 6 (`:26`). Parents: Dynamic Use :2159 "Efficiency" → `base_skill`; Scar Affinity "Crimson Mastery" → `skill_dynamic_use`. Missing include all of the Covenant (5), Mycelial (6) and Summon (10) branches.

**DC-41** · Medium · Verified · fix doc · user's call: no
§14 effect table omits 28 registered effects, and six rows name deleted Morphlings.
- HR:2435-2440 "Bat/Cuttlefish/Tick/Urchin/Centipede/Mole morphling effect" (those strains were deleted; the effects now come only from potions).
- `J/common/init/EffectInit.java` also registers chummed_waters, constricted, counterfeit_scar, cryoprotection, disrupted, earthen_mining, ender_resistance, explosion_resistance, ferric_transfusion, illuminated, impaired_recovery, lodestone, mind_broken, mnemonic_candle_aura, monolithic_dislocation, morphling_sealed, necrosis, obscured, pinned, poison_resistance, rime, sanguine_pressure, searing, stung, transfusion_saturation, veiled, web_mobility, wither_resistance. Colours: Arcane Resonance HR:2449 `0x8800AA` vs `EffectInit.java:231 0x8B0020`; Sanguine Clarity :2450 `0xAA0022` vs `:234 0x4020A0`; Morphic Strain :2456 "Fungal green" vs `:297 0x5A2A65`.

**DC-42** · Medium · Verified · fix doc · user's call: no
Stale class and gate names in §6-§16.
- `getPlayerUnstainedLevel` (HR:1688, 1690, 2546, 2608, 4010) does not exist; it is `ProgressionGates.unstainedStage` (`J/common/util/ProgressionGates.java:57, 75-92`).
- `DialogueEventHandler` (HR:2248, 2257) does not exist; `recruit_harbinger` is in `J/common/entity/npc/dialogue/route/RecruitmentDialogueEvents.java`.
- Primal Morphling gate HR:2698 "`IInitiatoryDegree >= 8`" vs `J/common/event/MorphicNectarEvents.java:189` `getArchonPath() == EnumArchonPath.APOTHEOS` (a D8 Silentium is refused).

**DC-43** · Medium · Verified · fix doc · user's call: no
"Axe is Mortem" contradicts the rest of the reference and the registry (see DC-12).
- HR:1959 "axe is Mortem"; HR:1828 and :2851 say Ferric; `J/common/init/ManipulationInit.java:298-301` registers `conjure_axe` FERRIC with secondary ANIMUS.

**DC-44** · Low · Verified · fix doc · user's call: no
§8 and §17 counts.
- HR:1795 "Twelve active families containing 28 deeper forms" vs `ManipulationFamilyRegistry.java:13-29` 29 forms.
- HR:2787 "All seven registered shapes", table at :2793 lacks Ringmaster Pattern (`PuppeteerSummonDefinitions.java:35-37`, 8 definitions); HR:2798 and :3949 Scarlet Mummer "900" vs `puppeteer_trial_scarlet_mummer.json:3 "bloodCost": 950`; HR:5266 "Six layered Puppetry Cardinal Ordeals" vs HR:2776 "Seven" (7 trial JSONs).
- HR:1827-1837 conjuration rows omit secondary tendencies that `ManipulationInit.java:292-360` assigns.

**DC-45** · Low · Verified · fix doc · user's call: no
§13, §15, §18 smaller mismatches.
- Blood-Honed Scar missing from HR:2293 (`ScarInit.java:140`, `scar_blood_honed.json` tier 2); Mind Spike listed as a scar (HR:2283) is only an item (`ItemInit.java:889`); HR:2349 fungal scar example `"tendency": "LUX"` vs `oculiflora_reticularis.json` `TENEBRIS`.
- Pale Intercession (`StillArtRewardTable.java:24-25`) missing from HR:2483, :2503; HR:2478 path `common/unstained/stillarts/StillArt` is `J/common/manipulation/stillarts/StillArt.java`; HR:2514 "Purity-Phase Rites (levels 0-5)" includes two level-8 rites; `severed_covenant.json` unlisted; HR:1561 Tears of Silthmere "distilled … at an Altar of Cleansing" is a shapeless craft (`recipe/tears_of_silthmere.json`).
- HR:1730 quotes two messages that are not in `en_us.json`; the real Clarity line is `en_us.json:3168`.
- HR:2997 "Illuminatus (Degree ≥ 3" (Illuminatus is D5; same slip in `J/config/HemoServerConfig.java:417` comment); HR:3004 SSC "500 mL per second" vs 500 mL every 10 ticks (`SemiSentientConstructBlockEntity.java:62`); HR:2953 shows retired `glacial_bastion` as a working Drudge action; HR:3033 item id `dsd` is `drudge_submission_device`.
- HR:5264 lists Drudge tendering as Implemented while §18 (:2867) and §19 (:3070) say post-alpha WIP.

### §20-end

**DC-46** · High · Verified · fix doc or fix code · user's call: yes
The Somatic Loom Blood Moon discount is documented as implemented; it does not exist.
- HR:4183 "During Blood Moons the loom applies the existing 25% ritual blood discount from `SomaticLoomBlockEntity.startRitual()`"; also :4480, :4487 ("implemented"), :5287.
- `J/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java:577` `double needed = Math.max(0.0D, curRecipe.getBloodCost() - ritualBloodCharged);`; no Blood Moon reference in the Loom or engram code (grep). The only Blood Moon discount is on manipulations (`ManipulationCostLedger.java:90`).

**DC-47** · High · Verified · decide (likely fix code) · user's call: yes
Every Hemomancy armour set is unbreakable; the reference says "Durability ×37".
- HR:3452, 3472, 3485, 3500, 3513, 3529 "Durability ×37".
- `J/common/item/harbinger/armor/HematicIronArmorItem.java:13` `super(materialIn, slot, new Item.Properties().fireResistant().stacksTo(1));` (no `.durability(...)`); Barbed, Chitinite, Prismatic, Unstained and BloodLust items are the same. In 1.21 `ArmorItem` does not set `MAX_DAMAGE` itself (vanilla passes `Type.getDurability(n)` in `Items`). Suspected side effect: without `MAX_DAMAGE` the items are not table-enchantable, so "Enchantability 15" is moot.

**DC-48** · Medium · Verified · fix doc (and check the two swords) · user's call: no
Weapon damage figures read the mining-speed argument as damage.
- HR:3322 Living Blade "(25 base dmg, +3 speed)", :3332 Sanguis Lancea "(25 base dmg)", :3399 Knapper "(42 dmg)", :3405 Barbed Blade / Chitinite Mace "+3 speed, +25 dmg".
- `J/common/init/ItemInit.java:653` `new LivingBladeItem(25f, 3, ...)` with `LivingBladeItem.java:33 (float speedIn, float attackDamageIn, ...)`; `:644` Lancea `(25f, 1, ...)`; `:773` Knapper `(42f, 1, 0, ...)` (HutosLib `ItemKnapper(float speedIn, float attackDamageIn, ...)`); `:777`, `:790` Barbed Blade and Chitinite Mace are `new SwordItem(EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1))` with no attributes (Suspected: effectively 1-damage swords).

**DC-49** · Medium · Verified · fix doc · user's call: no
Retired memories and wrong recipes listed as live in §20 and §25.
- HR:4205 `memory_ferric_resonance` as a live Loom recipe (no JSON; `ManipulationRetirementRules.java:17, 39`); gallery HR:3201-3213 and :3221, :5256 show Blood Lamp, Crimson Harvest, Hemosynthesis, Vital Reservoir, Sanguine Excavation, Ferric Resonance, Glacial Bastion, Crimson Sight, Glacial Circulation, Summon Thrall as active.
- HR:4066, :4073, :4077, :3716-3717 put Ghost Pipe and Lethean Poppy in the Ghastly Alembic; both are `"pallid": true` (`distillation/ghost_pipe.json:10`, `lethean_poppy.json:10`) and the reference's own :3818 routes those to the Pallid Retort. HR:4084 Pale Distillate "Lethean Dew + Consecrated Copper Ingot" vs `recipe/pale_distillate.json:4-5` `lethean_dew` + `lethean_poppy`.
- HR:3515 Prismatic repair "Serpent Scale" vs `EnumModArmorTiers.java:28-29` `Items.GLOW_INK_SAC`.
- HR:4212, :3171-3174 and §5.8 (:1218, :1224) give Canon Memory recipes one Hallowed Residuum catalyst; the four `memory_weaving/memory_<canon>.json` take `hallowed_residuum_<saint>` + `recollection_<saint>` (`docs/SAINT_MAUSOLEUMS.md:47` and HR §5.6h are right).

**DC-50** · Medium · Verified · fix doc · user's call: no
§33 keybindings and §32/§34 lists do not match what is registered.
- HR:4975-4984 list "Use Quick Manipulation", "Use Continuous Manipulation", "Open Morphling Jar", "Toggle Scar Binder Pickup"; `J/client/registry/KeyMappings.java:69-82` registers none of them (the first two are defined at :31, :33 but never registered), and the doc omits Burn Spores (U), Use/Select Still Art (R/Z), Drop Blood Ball (V) and the charm-slot keys B and X (:37-51).
- §32: "Strange Seeds" (HR:4890) does not exist (root is `advancement/hemomancy/root.json`, "The Red Thread", `en_us.json:311`); `degree_8_silentium.json` is missing from HR:4920; 120 of 164 advancement files are undocumented; HR:4909, :4947 cite a nonexistent `CardinalRiteEvents` (grants are in `DegreeProgression.java:24`).
- §34 omits `chamber`, `mycophant`, `vesper` (`J/common/command/HemoCommand.java:23-40`) and Qliphoth `tree <stage>`/`archon` (`QliphothSubcommand.java:41, 52`).

**DC-51** · Medium · Verified · fix doc · user's call: no
Wrong degrees and status words in §20-§28.
- HR:4474 Rite of the Sanguine Eclipse "(Greater rite, Degree 3+)" vs `cardinal_rite/sanguine_eclipse.json:7 "required_degree": 4` (and §25.2 has no row for it).
- HR:3440, :3465 Velorum Mask "Post-release", "a universal Saint-drop" vs `armature_upgrade/blood_lust_velorum_mask.json:3 "required_degree": 6` and a shapeless craft in `recipe/velorum_mask.json` (lodestone faceplate + `hallowed_residuum_velorum` + pale silver); nothing drops it. A D6 mask needing a D8 residuum is a design oddity to decide.
- HR:4038 Vascular Mending "all seven vascular sections" vs five (`EnumVeinSections`: HEAD, ARMS, HEART, BODY, LEGS; HR:5328 says five).
- HR:3266 lists the Verdigris Censer as a live item; it is unregistered (HR:5313 says so).
- HR:4878, :5239 Liber Sanguinium "Partially functional — `HemoProgressionScreen.setupEntries()` is still commented out"; no such class exists and `books/libersanguinium/` holds 11 chapters / 114 files.
- HR:4858 "Eight-tab Harbinger suite" vs `HarbingerProgressScreen.java:19-25` six tabs (also :3167, :5238).

**DC-52** · Medium · Verified · fix doc · user's call: no
Rosters in §25-§36 that stopped being maintained.
- §25.2 lacks chamber_of_will, founding_fane, hematic_succession, mnemonic_restoration, sanguine_eclipse, sanguine_fervor, possession_rite, visitation_rite, pallid_vessel_rite, crimson_vessel_rite, ashen_vessel_rite, horn_of_culmination_rite; HR:4010 "groups recipes by required degree/stage" contradicts HR:3880 and `docs/CARDINAL_RITES.md:32` (grouped by purpose). HR:4018-4019 blank lines break the table.
- §25 recipe types: `recaller_recipe_type` / Visceral Recaller (HR:3819, :5199) does not exist; `incubator_recipe_type` (HR:3821) is `hemomancy:incubator`; advanced_brewing, morphic_nectar, rendering, scale_grip_binding are missing; §37.3 omits most JEI categories (`JEIPlugin.java:31-91`).
- §26/§27 cover ~75 of the ~165 registered entity types (167 `DeferredHolder<EntityType` fields) (missing: the 11 `circus_*` entities, 8 puppet summons, unstained_scout, bloodlicker, mortarbound, hunter_fungus, antibody, hematic_construct; plus Saints, Court fighters and the Pelagic species absent from the tables).
- §36 names six data classes that do not exist (`SerpentParticleData`, `BloodAvatarHitParticleData`, `BloodCellData`, `BloodClawData`, `AbsorbedBloodCellData`, `SporiticSporeParticleData`; `ParticleInit.java:20-50` uses `ColorParticleOptions`, as AGENTS.md requires) and lists 7 of 12 types.
- §31.1 names `ManipCooldownOverlay` (does not exist) and shows 4 of ~23 layers in `GuiLayerRegistrar`.

**DC-53** · Low · Verified · fix doc · user's call: no
Stale counts and small mismatches in §20-end.
- Entity loot tables HR:4364, :5243 "44" vs 86 files; sounds HR:5087 "104" vs 188 (`SoundInit`, `sounds.json`); incubator recipes HR:3593 "13" vs 8; Observances HR:5314 "Nine" vs 14 (`UnstainedObservances.java:28-41`); summon definitions HR:3956 "six" vs 8; tool tiers HR:3311-3314 list 2 of 4 (`EnumModToolTiers.java:16-23`).
- HR:4613 Overworld `fungal_gardens` biome "which no dimension currently uses" vs `BiomeInit.java:451` `FungalGardensOverworldRegion`; HR:4440 Fungal Isles precipitation "None" vs `fungal_isles.json:4 "has_precipitation": true`; `phlegethontic_basin` is missing from §28.1; §29 omits Hermitage Remnant, Mason's Respite, Circus Pavilion, the troupe camps and the abandoned stage.
- HR:3272 "Initiatory Degree 5 (Perfected)" (D5 is Illuminatus); HR:3272 Virid Salis checks three classes the handler does not (`ViridSalisTrailHandler.java:123-128`); HR:3148 Bloody Jug list includes cruor fiends (not in `bloody_jug_drop_candidates.json`); HR:4343 Hemolymphopoda `ON_GROUND` vs `NO_RESTRICTIONS` (`EntityInit.java:1088`); HR:3488 Barbed set omits Poison 80 ticks (`ArmorSetBonusHandler.java:234-235`); HR:5260, :5263 still call two Working crafts "Blood Structure recipe".
- HR:3548 "equivalent to Netherite-tier" for a 3/6/8/3 spread with legs 8 / chest 6 (`EnumModArmorTiers.java:62-65`; Netherite is chest 8 / legs 6). Suspected swapped slots in code; decide.

**DC-54** · Low · Suspected · decide · user's call: no
HR:5288 "Fungal Dimension … terrain feature population … remain WIP" vs the terrain rebuild described at HR:4454 (and `wiki/Public-Alpha-Readiness.md:39`).

Known, cited once: C-1 (HR:5285, :4621 Saints/Mausoleum "post-release WIP"), C-7 (HR:5290 Annetta "Partial").

### Internal contradictions (whole doc)

- Puppet ordeals: HR:2776 seven, HR:5266 six (DC-44).
- Axe tendency: HR:1828/:2851 Ferric vs HR:1959 Mortem (DC-43).
- Canon Memory catalysts: HR §5.6h two vs §5.8 and §25.7.2 one (DC-49).
- Drudge tendering: HR:5264 implemented vs HR:2867, :3070 post-alpha WIP.
- Vascular sections: HR:4038 seven vs HR:5328 five.
- Rite grouping: HR:4010 by degree vs HR:3880 by purpose.
- Fungal Gardens biome: HR:4613 unused vs HR:4438 placed.
- Unstained cure order: HR:1670/1712/1729 vs HR:2600 (DC-37).

---

## 4. LORE_REFERENCE.md

Seven tests pin LR lines; every pinned string is present and every forbidden string absent. The 2026-10-31 rulings hold
in the doc except where noted; the drift is older gameplay text and a few internal tensions.

**DC-55** · High · Verified · fix doc · user's call: no
A "Blood Conduit pipeline network" and Basic/Advanced/Grand blood-structure tiers are described as gameplay; neither exists.
- LR:352 "Advanced patterns unlock at Degree 4 (Adept), and Grand-tier patterns requiring sustained Blood Conduit flow unlock at Degree 5"; LR:367 "(2) **Blood Structure Grand tier** — the Blood Conduit pipeline network becomes a mandatory crafting input".
- No `blood_conduit` in code, lang or HR (the only "conduit" is the `sanguine_conduit` oath-post and a Javadoc in `BlockBloodEndpoint.java:11`); `R/data/hemomancy/recipe/blood_structure/` has no tier field, and machines are Working crafts (AGENTS.md:79).

**DC-56** · High · Verified · fix doc · user's call: no
Three different puppet counts; the code has seven ordeals.
- LR:58 "six artificial bodies share the Dominion Lesser floor"; LR:482 "one of the four learned artificial shapes"; LR:486 "The four canonical artificial shapes"; `wiki/Advanced-Mechanics.md:5` "Shapes are learned through four **Puppetry Cardinal Ordeals**".
- `R/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_*.json`: veinwing_vulture, marrow_spitter, gorebound_hulk, scarlet_mummer, sanguine_hound, cinder_bellows, mnemonist_puppet (all `dominion_lesser`); HR:2776 "Seven".

**DC-57** · Medium · Verified · fix doc · user's call: no
Endgame lines that break the equal D8 split and the Silentium rulings.
- LR:381 "Choosing Silence leads to the Rite of Pruning … Choosing the eighth Degree opens the Rite of Apotheosis" (implies Silence is not the eighth Degree; contradicts LR:34, 154, 336, 384 and `docs/ENDGAME_PATHS.md:52`).
- LR:152 "Later visits use the Spine as a normal return-thread after the player has answered the revelation" vs LR:40, 137 and `J/common/encounter/FungalProjectionRules.java:20` (PHYSICAL only when `path == EnumArchonPath.APOTHEOS`).

**DC-58** · Medium · Verified · fix code · user's call: no
The code breaks the "Silentium is cut from the hive" ruling once: the Antecedent epilogue still reaches a Silentium.
- LR:386 "A Silentium can no longer get information from the hivemind"; the epilogue is the hive's voice (lang speaker "???", "You found us under the stone…").
- `J/common/event/AntecedentEvents.java:66-70` gates only on `ProgressionGates.hasDegree(player, 7)` and active blood, then sends `AntecedentEffectPacket(4, ...)`; no `hearsTheHive` check (every other hive line goes through `PendingWhispers.hearsTheHive`).

**DC-59** · Medium · Verified · fix doc · user's call: no
Stale gameplay lines for D1, D3 and the mausoleums.
- LR:347 the first Neophyte assignment "asks the player to carry 5000 ml, project blood into a Liber Sanguinium, and harden a Hematic Iron Block" vs lang `vicar.neophyte.degree_hint` (500 mL absorbed, cauldron Formation, Venous Stone, Liber or Iron Block); `wiki/Harbinger-Path.md:35` is right.
- LR:357, :657 put Hallowed Residuum and Canon Memories at D3 with residuum as the only catalyst, and LR:1097 treats the Recollection as future; the recipes already need both (see DC-49) and Recollections go only to an Apotheos or possessing Silentium (`docs/SAINT_MAUSOLEUMS.md:47`). Related to known C-3, C-6.
- LR:52 "only one generic mausoleum exists, and it does not generate"; LR:757 basin-and-gate prototype trial; LR:849 "Seraphae's trial room is still WIP"; LR:647, :357 "outer trial chambers" vs four generating `worldgen/structure/mausoleum_<saint>.json` and `docs/SAINT_MAUSOLEUMS.md:13` "no trial out there".
- LR:964-965 Putriciel "crimson vestments … palette pass is planned", cleansing interval "planned" vs LR:968, :1097 and `PutricielCleansingRules.java`.

**DC-60** · Medium · Verified · decide · user's call: yes
Three canon-policy questions only the user can settle.
- Per-Saint overhauls: LR:1097 "Much of it is now built" vs `docs/SAINT_MAUSOLEUMS.md:3` "not started" and OQ:27 D-2 (the named rules exist: `HemorathSiphonRules`, `SeraphaeGrowthRules`, `PutricielCleansingRules`, `VelorumStillnessRules`).
- God framing: LR:109 "It is a deity in an infinite universe … the Pale Lady is the pagan god"; LR:593 "She is the river deity" vs LR:575 "She is not a goddess", LR:1309 and the AGENTS.md "no God/Satan framing" rule.
- Authority rule: LR:23, :27 "LORE_REFERENCE.md is the authority for canon … Game code … must be corrected" (pinned by `LoreCanonAuthoritySourceTest.java:18`, which also forbids "Current code and data are authoritative" in HR) vs AGENTS.md:6 "Treat current code/resources as authoritative when docs drift". Suggested split: lore wins on narrative, code wins on mechanics.

**DC-61** · Medium · Verified · fix doc · user's call: no
Internal contradictions in LR.
- Degree 5: LR:324 "Most Harbingers … only ever reach **Degree 5**. To the average practitioner, this is the summit" vs LR:365, :430 "Most Harbingers never reach Degree 5" (lang Vicar "Few of us are given that title").
- Siphonophore: LR:1243 Congeatio vs LR:1431 Ductilis (DC-16).

**DC-62** · Low · Verified · fix doc · user's call: no
Smaller LR drift.
- LR:1204, :1205 boss rewards "Guaranteed loot-table drop" vs pending claims (`VesperOrdealManager.java:58`, `MycophantEncounterManager.java:59`; LR:398, :410 agree with code).
- LR:372 Chamber "begins as a small 9x9 inner room" (see DC-70).
- LR:476-480 "Faded" and "Proctor" Wills as implemented origins vs `WillOrigin` BROKEN/SENT only.
- LR:350, :1206, :1270 enzyme names used as tendency names; lang "Ferric Enzyme" mirrors `FERRIC` against the AGENTS.md rule (NPC_VOICES open decision 14; decide).
- Names: LR:36 "Unsigned Ledger" (lang "Unsigned Ancestral Ledger"), LR:213 "outpost waybill" (shown as "Harbinger Lodestone"), LR:616 "Pallid Icon" ("Icon of Our Lady"), LR:34/154/381/390 "Rite of Apotheosis" (lang "Rite of Apotheos").
- LR:1203, :450 Silent Archon material "belongs to Archons who reject Apotheos" vs LR:426 "There is no seventh-Degree Silent Archon" (OQ §1.4 Artificer item; decide).
- Lang `hemomancy.rite.hematic_unbinding.desc` "A severance rite that dissolves the caster's bloodline": a "severance" use not listed in known C-2.
- LR:123 MnA "compounding resonances" presented as live (compat excluded); LR:694, :763, :855, :976 extra Saint epithets not in lang (related D-17); LR:258 "Voluntas Sola" has no content.
- Gaps: no Pallid Vessel (ruling 6), Covenant Throne, heart-node sanctum or Visitation coverage; header dates LR:13, 15, 23, 44 still "2026-10-03"; LR:1397 broken link (DC-87); LR:1399 cites untracked `tools/oneoff/antecedent/build_vigil.py`.

Known, cited: C-1 (LR:661), C-2 (LR:35, 496, 510, 1121), C-3 (LR:655), C-4 (LR:156-158), C-7 (LR:51, 1119, 1130).

---

## 5. wiki/*.md

37 pages. Tests pin lines on 9 of them (table below); all pinned strings are present. The biggest player-facing problems
are 32 invented manipulation names in Blood-Systems, invented items and abilities in Getting-Started, Unstained-Path and
Harbinger-Path, retired manipulations listed as live, a wrong dependency list,
wrong default keys, stale Unstained stage names and Saint status, and broken tool paths.

**DC-63** · High · Verified · fix doc · user's call: no
Blood-Systems' reference tables are largely invented: 32 "Key Manipulations" that do not exist (the Ductilis section even describes iron), plus made-up vein sections, skill branches and effects.
- `wiki/Blood-Systems.md:227-230` Animus "Sanguine Touch", "Vital Surge", "Life Ward", "Phoenix Blood"; `:252-255` Flammeus "Ember Touch", "Flame Bolt", "Conflagration", "Solar Flare"; `:277-280` Ductilis "Iron Dart", "Ferrous Shield", "Blade Storm", "Arsenal" (with "Philosophy: Blood contains iron" at `:274`); `:302-305` Lux; `:329-332` Mortem; `:354-357` Congeatio; `:379-382` Ferric; `:404-407` Tenebris ("Shadow Step", "Veil of Night", "Umbral Chains", "Eclipse").
- None of the 32 matches any of the 123 ids in `J/common/init/ManipulationInit.java` or their lang/memory names (script check; nearest real ids are `umbral_step`, `blood_eclipse`, `hematic_beacon`, `sanguine_magnetism`, `phoenix_debt`). The real roster is `docs/MANIPULATIONS_AND_CASTING.md` and HR §8.2.
- The same page invents the rest of its system tables: vein sections `:480-484` "Heart / Artery / Capillary / Vein Section" vs `EnumVeinSections` HEAD, ARMS, HEART, BODY, LEGS; skill branches `:506-531` "Blood Efficiency / Tendency Mastery / Combat / Defense / Utility / Scar Branch" vs `setBranch` values core, living_staff, scars, covenant, mycelial, summons (`J/common/init/skills/*`); effects `:640-667` "Sanguine Vitality", "Iron Will", "Radiant", "Exsanguination", "Withered", "Frozen" (none registered in `EffectInit`); "Blood Wall" construct `:622` (no such entity).

**DC-64** · High · Verified · fix doc · user's call: no
Getting-Started, Unstained-Path and Harbinger-Path describe items, stations and abilities that do not exist.
- `wiki/Harbinger-Path.md:475` "**Visceral Recaller** - Stores your respawn point in blood", `:481` "**Blood Compass** - Points toward nearest Harbinger structure" (the registered compass is `hearty_compass`, "Hearty Compass"), `:455` "**Sanguis Lance** (Annetta's Weapon)" (items are `sanguis_lancea` and `annettas_sanguis_lancea`); `:263-267` "NPC Callings" **Warblood**, **Fleshwright**, **Cordwainer**, **Bloodletter** appear nowhere in code or lang (succession professions are the five `data/hemomancy/succession_profession/*.json`).
- `wiki/Getting-Started.md:196` "**Hemoclysm**: Portable blood container (holds 1000 mL)", `:199` "**Sanguine Chisel**: Used at the Chisel Station to craft scars", `:200` "**Visceral Recaller**: Stores your respawn point in blood", `:156`, `:220` "Copper Altar(s)", `:280` "Bloodwort", `:194` "**Blood Vial**: Stores 250 mL" (the item is "Bloody Vial").
- `wiki/Unstained-Path.md:319-340` "Combat Abilities" **Crushing Strike**, **Pallid Shield**, **Purging Touch**, **Aura of Stillness**; `:463` "Copper Guardian Plate", `:470` "Purifier's Vestments", `:487` "Consecrated Copper Mace", `:498` "Purifier's Staff".
- No item, block, Still Art or lang display name matches any of these (script over `en_us.json` values and registry ids). The real Unstained set is the nine Still Arts (`hemomancy.still_art.*`: Silver Rebuke, Lethean Mute, Still Pulse, Pale Diagnosis, Memory Shear, Absolving Step, Quietus Bell, Autoimmune Edge, Pale Intercession) and `unstained_helm/chestplate/leggings/boots`, `unstained_warhammer`, `unstained_shield`; the Visceral Recaller does not exist (HR:3819 has the same ghost, and `wiki/Mod-Compatibility.md:164` lists a "Visceral Recaller" JEI category; `JEIPlugin` registers 14 category classes, none of them a recaller). Rewrite these sections from HR §15 and §22.6.

**DC-65** · Medium · Verified · fix doc · user's call: no
School-Combat's assignment table lists all 15 retired manipulations as live, under "Every listed manipulation has its player path".
- `wiki/School-Combat.md:57` "Every listed manipulation has its player path"; rows `blood_eclipse_mantle` (:70), `blood_lamp` (:71), `crimson_harvest` (:96), `crimson_sight` (:97), `ferric_resonance` (:108), `ferric_transmutation` (:110), `glacial_bastion` (:114), `glacial_circulation` (:115), `hematic_ballast` (:121), `hemosynthesis` (:130), `sanguine_excavation` (:148), `summon_thrall` (:165), `venous_travel` (:175), `vigil_of_glass` (:176), `vital_reservoir` (:178).
- HR:1931-1952 and `ManipulationRetirementRules`: these ids are "hidden, inert, non-lootable, non-recipeable, absent from the manipulation tree". Mark or drop the rows. (`wiki/Casting-Animations.md:64` has the same slip, DC-74.)

**DC-66** · Medium · Verified · fix doc · user's call: no
GeckoLib is listed as a required dependency; the mod does not declare or use it.
- `wiki/Home.md:130` "Required Dependencies: … GeckoLib: Entity animations"; `wiki/Getting-Started.md:15` "GeckoLib 4.8.4+"; `wiki/Mod-Compatibility.md:24-28` "### GeckoLib … **Required:** Yes"; `wiki/Developer-Reference.md:435`.
- `R/META-INF/neoforge.mods.toml:76-81` the geckolib dependency is commented out; `build.gradle:226-228` "GeckoLib, Curios and MnA are compiled only by the excluded compat packages"; only `J/compat/mna/**` imports `software.bernie`.

**DC-67** · Medium · Verified · fix doc · user's call: no
Getting-Started gives wrong default keys.
- `wiki/Getting-Started.md:190` Field Notes "Keybind: `B` by default"; `:305` "**`B`**: Open Field Notes journal"; `:306` "**`V`**: Manipulation selection wheel".
- `J/client/registry/KeyMappings.java:41` B is `key.charm_slot.slot` (charm slot), `:43` X opens the vascular charm menu, `:51` V is "Drop Blood Ball", `:29` C cycles manipulations; no Field Notes or wheel key is registered (`:69-82`).

**DC-68** · Medium · Verified · fix doc · user's call: no
Unstained-Path stage names and bands match neither the code nor the reference.
- `wiki/Unstained-Path.md:59` "Stage 1: Initiate (0-25% Purity)", `:85` "Stage 2: Purified (25-50%)", `:113` "Stage 3: Guardian (50-75%)", `:146` "Stage 4: Absolved (75-100%)", `:187` "Stage 5: Ascended (95-100%)" (overlapping); `:58` "Purity levels, measured by accumulated **Clarity** points".
- HR:1690-1702 / `ProgressionGates.unstainedStage`: Begun <25, Tainted ≥25, Cleansing ≥50, Absolved ≥75, Purified 100, then Clarity Discerning/Vigilant/Enlightened (lang `unstained.hemomancy.purity.tainted.desc`, `.cleansing.desc`, `clarity.discerning.desc`, …).

**DC-69** · Medium · Verified · fix doc · user's call: no
Wiki tool and launcher paths that moved to `tools/review/` or no longer exist.
- `wiki/Ferric-Ductilis.md:58-59` `tools/ferric-ductilis-review.init.gradle`; `wiki/Flammeus-Congeatio-Visuals.md:28-30` `tools/manipulation-visceral-review.init.gradle`, `:35` `./tools/review-thermal-observer.ps1`; `wiki/School-Combat.md:277` `tools/school-combat-acceptance.init.gradle`; `wiki/Casting-Animations.md:306, 309` `tools/oneoff/casting-animation-review.init.gradle` → all are under `tools/review/` (tracked).
- Missing entirely: `wiki/Ferric-Ductilis.md:57` `tools/validate_ferric_ductilis_shaders.py`, `:63` `tools/assemble_ferric_review.py`; `wiki/Flammeus-Congeatio-Visuals.md:26` `tools/validate_thermal_shaders.py`; `wiki/Naeglerophaeon-Visual-Validation.md:76` `tools/model_export/naeglerophaeon-tissue-prompt.txt`.

**DC-70** · Medium · Verified · decide · user's call: yes
The Chamber of Will's size: the docs give tier sizes 9x9/13/17/21, but the code builds the room from the degree (`degree + 2`) and keeps a separate tier rule.
- `wiki/Harbinger-Path.md:84` "9x9 at tier 0, 13x13 after the Vein-Mason Degree-6 milestone or Degree 7, 17x17 once Qliphoth begins…, and 21x21 at Apotheos"; `wiki/Lore-and-Story.md:207`; LR:372; HR:1145 "Tier 0 starts at a 9x9 floor (`BASE_ROOM_RADIUS = 4`)".
- `J/common/encounter/ChamberOfWillManager.java:85` `BASE_ROOM_RADIUS = 3`; `:383-385` the built radius requested for an online owner is `ChamberVisitRules.radiusForDegree(degree)` = `Math.max(3, Math.min(10, degree + 2))` (`ChamberVisitRules.java:27-28`), so a D6 owner gets radius 8 (17x17); `ChamberProgressionRules.java:8, 40-41` keeps the tier rule `BASE_RADIUS 4 + tier*2` used by `radiusFor`. Decide which rule is canon, then align code and the four docs.

**DC-71** · Low · Verified · fix doc · user's call: no
Harbinger-Path statements the code or the rulings have moved past.
- `:29` "Apotheos is the hidden eighth threshold reached only after the fungal revelation" vs the equal split at `:100` and LR:275 ("The eighth degree is silence").
- `:43` "the seven blood tendencies" vs eight (`EnumBloodTendency.java:10-13`; `:17` says eight).
- `:17` "Hundreds of blood manipulations" vs 123 registered (108 active).
- `:138` "The three Degree 7 rites that stand apart" (four special rites; related C-8).
- `:52` "Drudges and every Saint trial, including Hemorath, are post-release expansion content" (known C-1).

**DC-72** · Low · Verified · fix doc · user's call: no
Saint status in wiki pages that C-1 does not list.
- Known: C-1 names only `wiki/Public-Alpha-Readiness` (pinned by `src/test/.../common/creative/HemomancyCreativeTabsSourceTest.java:127-128` "**The entire Saints suite is post-release WIP.**"). The same claim also sits in `wiki/Home.md:46` ("the planned trial chambers"), `:84` ("Saint mausoleums and trial chambers are planned"), `wiki/Getting-Started.md:263-265`, and `wiki/Saints-and-Encounters.md:12`, which contradicts its own `:16` (the generated complexes). Add these to C-1.

**DC-73** · Low · Verified · fix doc · user's call: no
Navigation and link rot.
- `wiki/Home.md:44` `[[Factions]]` and `:54` `[[Contributing]]` have no pages; 20 pages are not reachable from Home (Brined-Court, Crimson-Troupe, School-Combat, Casting-Animations, Clinical-Blood-Tools, Injectable-Blood-Vials, Phlegethontic-Nether, Hematic-Microscope, Mortarbound, …); `wiki/README.md:9-20` indexes 12 of 37 pages.
- 12 GitHub links use `blob/main` or `tree/main` (`wiki/Developer-Reference.md:41, 51, 58, 64, 71, 532`, `wiki/Lore-and-Story.md:11, 517, 518`, `wiki/Mod-Compatibility.md:115, 326`, `wiki/README.md:182`); origin has `master` and `neo-1.21.1`, no `main`. `wiki/Developer-Reference.md:64` also points at `docs/PUBLIC_ALPHA_READINESS.md`, which is `wiki/Public-Alpha-Readiness.md`.
- `wiki/Casting-Animation-Validation.md:29-59` and `wiki/Flammeus-Congeatio-Visuals.md:59` embed images from `build/` (untracked).

**DC-74** · Low · Verified/Suspected · fix doc · user's call: no
Smaller wiki drift.
- `wiki/Casting-Animations.md:64` uses retired Vigil of Glass and Hematic Ballast as examples of live passives (HR:1931-1952 retired list).
- `wiki/Blood-Systems.md:36` "Starting Volume: 1000 mL (after Mortal Display activation)": `BloodVolume.java:9` starts at `0.0` and `EarlyInitiation.activate` (`J/common/mission/vicar/EarlyInitiation.java:51-55`) only sets `active` (Suspected; no other setter found).
- `wiki/Public-Alpha-Readiness.md:39` "Fungal Dimension is partial … deeper terrain … WIP" (Suspected stale; terrain rebuilt 2026-10-18).
- `wiki/Hematic-Succession.md:49` "Leave socket 3 empty" (DC-10).

**Missing pages for shipped features** (a section elsewhere is noted):
- The D8 endgame body: Apotheos body, Fungal Gardens network, heart-node sanctum, possession, adipocere, Visitation ("Adipocere" and "Visitation" appear on no wiki page; `wiki/World-Content.md:52` has one possession paragraph).
- NPC trade (no page; mentions in Getting-Started:302, Unstained-Path:17, Crimson-Troupe:5).
- Station upgrades (0 hits for "station upgrade").
- Cardinal Rites, Working crafts and rite spaces (only a section of Harbinger-Path).
- Somatic Loom engram weaving (the player guide is `docs/engram-weaving/README.md`, outside the wiki; one wiki hit).
- Morphling strains (one paragraph in Advanced-Mechanics), Recollections (0 hits), Pallid Vessel (1 hit), the Pelagic oceans (no page).

**Pinned wiki lines**

| Test | Wiki file | Pinned string | Present |
| --- | --- | --- | --- |
| `common/lore/WikiLoreDownstreamSourceTest.java:15-21` | Home, The-Qliphoth, Harbinger-Path, Unstained-Path, Lore-and-Story | "wiki is a downstream player-facing presentation", "two-minute consciousness projection", "### Degree 8: Apotheos", "Lethean Baptism"; forbids three old phrases | yes / absent |
| `common/lore/SettledLoreSynchronizationSourceTest.java:47-50` | Harbinger-Path | "Degree 6: Sanctified"; forbids "Degree 1: Pledged", "Degree 5: Apostle", "Degree 6: Archon" | yes / absent |
| `common/entity/summon/puppeteer/PuppeteerIntegrationSourceTest.java:105-108` | Harbinger-Path, Advanced-Mechanics | "Skein Transposition"; forbids "sneak-use cycles learned shapes" | yes / absent |
| `common/creative/HemomancyCreativeTabsSourceTest.java:125-128` | Public-Alpha-Readiness | "**Drudges are post-alpha WIP.**", "**The entire Saints suite is post-release WIP.**" | yes (the second pins stale text: known C-1) |
| `client/player/CastingDocsSourceTest.java:28-34` | Casting-Animations | every non-school `CastStyle` and ending title | yes |
| `common/manipulation/animus/HematicRiposteSourceTest.java:81` | School-Combat | "`hematic_riposte`" | yes |

---

## 6. In-game books, tooltips and advancement text

Coverage is partial: the dedicated book and tooltip pass did not finish before this report was due, so this section holds
only what was verified along the way. A full pass over `data/hemomancy/books/**`, the Rites & Crafts map
(`HarbingerRecipeMapDefinitions`), item tooltips and advancement criteria is still owed.

**DC-75** · Medium · Verified · fix resources · user's call: no
The Condenser's three Liber pages break the dense-page rule (`decal: false`) that every other station page follows (also the STATION_UPGRADES finding in area 2).
- `R/data/hemomancy/books/libersanguinium/the_hematic_order/pages/first_clotting.json`, `druse_crystallization.json`, `sanguine_condenser.json`: no `"decal": false`.
- The other upgrade pages set it (`alembic/pages/first_condensation.json`, `centrifuge/pages/second_fraction.json`, `manipulations/pages/ganglionic_weave.json`, `resonant_forge/pages/upgrades.json`, …).

**DC-76** · Low · Verified · fix doc · user's call: no
Advancement and milestone names in the reference do not match the shipped text.
- HR:203, :4890 "Strange Seeds" as an advancement; the root advancement `R/data/hemomancy/advancement/hemomancy/root.json` is titled "The Red Thread" (`en_us.json:311`), and "Strange Seeds" is the milestone `hemomancy.milestone.strange_seeds` (`en_us.json:2813`). 164 advancement files exist under `advancement/hemomancy/`; HR §32 documents 44.

**DC-77** · Low · Verified · fix doc · user's call: no
Book-facing status in the reference is stale: HR:4878 says the Liber Sanguinium is "Partially functional — `HemoProgressionScreen.setupEntries()` is still commented out"; that class does not exist and `books/libersanguinium/` holds 11 chapters / 114 files (see also area 3).

Still owed (not checked here): mechanics and costs stated in book pages vs code; book entries for removed ids; manipulations, rites and Working crafts with no Liber / Rites & Crafts entry; the Apotheos Liber section (known: C-8); tooltip numbers and missing tooltip keys.

---

## 7. Test health

No `@Disabled` tests and one assumption skip; the 300-odd legacy `main()` test classes do run (through
`src/test/.../testing/LegacyMainTestAdapterTest.java`). A static check of 97 doc/wiki pins in 27 test classes found no
broken pin. The problems are a git index that would break a clean build, one launcher named by tests and docs that does
not exist, and an impact map that misses tests with computed paths.

**DC-78** · Medium · Verified · fix repo (`git rm --cached`) · user's call: no
Two staged files would break compilation on the next commit.
- `git status`: `AD src/main/java/.../morphlings/MorphlingCamouflageRules.java`, `AD .../MorphlingColdResilienceRules.java` (added to the index, deleted from the working tree; not in HEAD).
- `git show :…/MorphlingCamouflageRules.java` declares `public final class LumenlaceCamouflageRules`; the other declares `public final class WinterShroudResilienceRules`. A public class in a file of another name fails javac; `docs/archive/tracks/brined_court/02-strain-powers.md:96` records both as removed.

**DC-79** · Medium · Verified · fix doc or add the row · user's call: no
`chamberReviewClient` is named as the owed proof but no such launcher exists.
- `docs/PLAYTHROUGH_TESTS.md:81` "Owed by `chamberReviewClient`, `vesperReviewClient`"; `GT/playthrough/PlaythroughNotRun.java:16` `CHAMBER = "chamberReviewClient"`; `GT/playthrough/PlaythroughStates.java:351`.
- `gradle/review-clients.gradle` has no `chamber*` row (the other names used there, `vesperReviewClient` and `heartNodeReviewClient`, exist).

**DC-80** · Low · Verified · fix doc · user's call: no
The playthrough suite is still described as D0 to D5.
- `gradle/game-tests.gradle:36` reason "Long player-centric Harbinger D0-D5 playthroughs"; `docs/README.md:55` "The player-centric Harbinger D0 to D5 GameTest suite".
- `docs/PLAYTHROUGH_TESTS.md:3-8` "426 GameTests … the D0 to D5 bands, the D5 to D8 bands …".

**DC-81** · Low · Verified · fix doc / regenerate · user's call: no
`docs/architecture/evidence/source-text-impact-map.csv` (3,564 rows, 581 test files, 1,846 targets; every listed file exists) misses 13 test classes that read repository files through computed paths.
- Missing, e.g.: `common/item/harbinger/memories/CanonMemoryRecipeResourceTest.java:30` (`ResourceReader.data("recipe/memory_weaving/memory_" + memory + ".json")`), `common/recipe/SilentArchonArmatureDegreeTest.java:12`, `architecture/ShaderNoiseIncludeGuardrailTest`, `PendingDeliveryRegistryTest`, `ServerboundGuardsCoverageTest`, `SessionStateRegistryTest`, `RenderTypeCachingGuardrailTest`, `PlayerAnimationGuardrailTest`, `ParticleDataGuardrailTest`, `PayloadDirectionTest`, `JigsawStructureGuardrailTest`, `AnimationTimeGuardrailTest`.
- `gradle/source-text-impact-map.py:27-35` resolves only literal arguments. Either teach it `+`-joined prefixes or list these as `by-name` rows.

**DC-82** · Low · Verified · decide · user's call: no
One test passes vacuously on every clean checkout.
- `src/test/.../common/recipe/PuppeteerCardinalOrdealResourceTest.java:70-72` `assumeTrue(Files.isRegularFile(parserPath), "tools/skill_tree_editor is not version-controlled…")`; `tools/skill_tree_editor` is git-ignored (`.gitignore:70`), so CI always skips it. The reason holds; the check only protects the author's machine.

**DC-83** · Low · Verified · fix doc (and rename the fixtures) · user's call: no
The `/hemo test` pilot scenarios are described as Blood Structure checks; the Covenant Throne they use is a Working craft, and the TESTING.md section that lists them sits under the wrong heading.
- `docs/TESTING.md:573-579` "`blood_structure_locked` — degree 5 must not satisfy the degree-6 Covenant Throne recipe", "`uninitiated_cannot_pass_bloodcraft_degree_gate` — … the loaded degree-6 Covenant Throne bloodcraft gate".
- `GT/HemoTestScenarioCatalog.java:26` `Hemomancy.rloc("cardinal_rite/working/covenant_throne")` (`recipe/cardinal_rite/working/covenant_throne.json`, `"required_degree": 6`); `docs/TESTING.md:545` "## In-game scenario commands" is an empty heading, and the command list follows under "## Chamber progression and perspective checks" (`:547`).

**DC-84** · Low · Verified · fix doc · user's call: no
Thirteen review-client rows are mentioned in no doc: `axonalReviewClient`, `bookKitPeerClient`, `brazierReviewClient`, `crimsonDoeReviewClient`, `eruptusReviewClient`, `funglingReviewClient`, `harbingerSitesReviewClient`, `jeiReviewClient`, `scriptoriumReviewClient`, `sickleStrokeReviewClient`, `speculumPlantingReviewClient`, `speculumReviewClient`, `venousStriderReviewClient` (`gradle/review-clients.gradle`, 50 rows). `docs/TESTING.md` does not claim to list all, but it is the doc AGENTS.md points to; a one-line table would close it.

---

## 8. The archive

The archive README's folder map is right and every live link into `docs/archive/` resolves on this machine. The
problems are live guidance that still treats archived files as current, a live asset pipeline inside a git-ignored
archive folder, and links into `archive/concept/`, which is not in git.

**DC-85** · Medium · Verified · decide · user's call: yes
AGENTS.md and the agent profile tell contributors to keep an archived file up to date.
- AGENTS.md:81 "check whether … `docs/archive/done/MNA_COMPATIBILITY_BRAINSTORM.md` … also need updates"; `docs/agents/hemomancy-mod-agent.md:67` "Update `docs/archive/done/MNA_COMPATIBILITY_BRAINSTORM.md` for Mana and Artifice design changes", `:129`. HR:5179 ("see `MNA_COMPATIBILITY_BRAINSTORM.md` for full details"), HR:5274, `wiki/Developer-Reference.md:58`, `wiki/Mod-Compatibility.md:115` send readers there for the design.
- `docs/README.md:63-64` "Nothing in it is authoritative"; `docs/archive/README.md:3-5` "Archived files are not updated when mechanics change". Either move the MnA design back to a live doc or drop the instruction.

**DC-86** · Medium · Verified · decide · user's call: yes
A live asset pipeline lives in a git-ignored archive folder.
- HR:2748 the Morphling HUD sprites "are exported … by `docs/archive/concept/output/brined-strain-icons-16x16/export_hud_sprites.py`"; HR:5272 the item textures come from the same folder; `docs/archive/README.md` table "`brined-strain-icons-16x16/export_hud_sprites.py` still exports the Morphling HUD sprites"; `docs/BRINED_COURT.md:127`.
- `.gitignore:62` `/docs/archive/concept/`; `git ls-files docs/archive/concept` returns 0. A clean checkout cannot regenerate committed sprites. Move the exporter and its pixel maps to a tracked `tools/` folder.

**DC-87** · Low · Verified · fix doc · user's call: no
Links into untracked or moved archive files.
- `docs/README.md:83-84` "Markdown under `docs/` is tracked by git everywhere; only the archive's images, generated output and concept-art folders are ignored" vs `.gitignore:62` ignoring all of `archive/concept/` (61 `.md` files untracked).
- Links that resolve only on the author's machine: HR:5458 and LR:1397 (`archive/concept/antecedent-fresh-draft/README.md`), `wiki/Antecedent-Inquiry.md:58`, `docs/BRINED_COURT.md:123` (`archive/concept/Underwater faction ideas/`).
- Broken anywhere: HR:5458 and LR:1397 link `HEMOMANCY_FIRST_INCURSION_DEEP_DARK_BRIEF.md` relative to `docs/`; the file is `docs/archive/concept/antecedent-fresh-draft/HEMOMANCY_FIRST_INCURSION_DEEP_DARK_BRIEF.md`. HR:130 links `PUBLIC_ALPHA_READINESS.md` (it is `wiki/Public-Alpha-Readiness.md`).
- HR also has 28 broken image links: the `textures/ref doc images/` folder was deleted (`eba4a434c`; HR:3582-3626, 4851-4853) and NPC textures moved under `textures/entity/npc/<faction>/` (HR:4315-4324); also `monolith_imbued_cloth.png`, `vial_rack.png`, `lethean_poppy_wreath.png`, `blood_lust_helm_lodestone.png`, `gui/blood_fill_tiled.png`, `gui/recaller_gui.png` (HR:3087, 3144, 3262, 3461, 4833, 4851).

**DC-88** · Low · Verified · fix comments · user's call: no
Code and tests cite archived track files as the source of a rule.
- `src/test/.../architecture/NpcTradeGuardrailTest.java:69` "Trades never grant progression (docs/archive/tracks/npc_trade/00)"; `J/common/entity/npc/trade/NpcTradeAppraisals.java:21`, `NpcTradeGiveRules.java:12`, `NpcTradeLoader.java:34`; seven tests under `src/test/.../npc/trade/`, `NpcTradeLedgerTest.java:10`, `DialogueRapportLoreTest.java:22`; `GT/NpcTradeGameTests.java:70, 78`. The rules are in `docs/NPC_DIALOGUE_AND_TRADE.md` "Rulings that bind"; point there.
- `wiki/Developer-Reference.md:71` sends contributors to `archive/done/DIALOGUE_ITEM_QUERY_GUIDE.md` as the inquiry guide; the live doc is `docs/NPC_DIALOGUE_AND_TRADE.md`.

**DC-89** · Low · Verified · decide · user's call: yes
Topic docs and the reference point contributors at generators that are not version-controlled.
- `docs/ENDGAME_PATHS.md:43` `tools/worldgen/fungal_gardens_worldgen.py` (the Gardens terrain source) is git-ignored (`.gitignore:75` `/tools/worldgen/*`, no whitelist line) though every other worldgen generator is tracked (AGENTS.md:29).
- `docs/CIRCUS.md:44` and HR §17.1 `tools/circus/build_*.py`; OQ §1.4 `tools/i18n/voice_rewrite/pass3/notes/`; LR:1399 `tools/oneoff/antecedent/build_vigil.py`; `wiki/Hematic-Microscope.md:29` `tools/oneoff/hematic_microscope.py` - all local only. Either track them or mark "local only".

**Content that lived only in the archive:** sampled 25 items. All Brined Court open questions (tracks 02, 03, 05, 07, 08,
10) are in `docs/BRINED_COURT.md:95-111` and OQ D-13/D-22; all six mausoleum §3.8 calls are OQ M-1 to M-6; the
npc_trade Q-ids are in NPC_DIALOGUE_AND_TRADE rulings. One number copied from the archive has since changed in code:
the manipulation roster (DC-12, from `archive/audits/MANIPULATION_BALANCE_PLAN.md` §11.2).

---

## Not a problem

Checked and consistent (abbreviated; each line was read on both sides).

**AGENTS.md (area 1)**
- Versions: MC 1.21.1, NeoForge 21.1.251, Java 21, mod id `hemomancy` (`gradle.properties`, `build.gradle:116`).
- Every named class exists: `HemoAttachmentTypes`, `HemoCapabilityKeys/Registrar/Access` (Player accessors return the attachment; `Optional` only for `Entity`/`BlockEntity`/`ItemStack`), `PlayerStateSync`, `TransientServerStateEvents`, `PlayerDataMigrations`, `PlayerDataVersion`, `PlayerCooldowns`, `RiteKind` (exactly SUCCESSION, STATION_UPGRADE, WORKING_CRAFT, PUPPETEER_TRIAL, COUNTDOWN, CEREMONY), `CardinalRiteTermination/Completion/Activation`, `ActiveCardinalRite`, `DialogueEventRouter` (only `DialogueEvent` subscriber; 17 `<Npc>DialogueEvents` in `route/`), `NpcTradeService/Sides/Ledger`, `ServerboundGuards`, `ClientPayloadHandlers`, `Clientbound*Handlers` (4), `PendingDelivery` (claims VIALS, TOOLS, BRIEFING_VIALS, TENDRIL, MEMORY, FUNGAL_SPINE, plus Pallid Vessel REWARD), `ChamberVisitService`, `MaterialsData`/`MaterialAtlasSpec` (Seed/Bloom/Pome/Spine at D7), `Reach` presets, `SchoolRenderTypes.Clock`, `VeinBackgroundRenderer.Style/Field/Palette/Speckles`, `BossReplayClock` (`sample`/`sync`/`at`), `AnimationTime` (four methods), `PlayerAnimationRegistry.THIRD_PERSON` (order exactly as listed), `MaterialQuads.seedTile`, Ferric 128 constant (`FerricDuctilisGeometry.java:28`), 12 shared noise includes.
- All 39 test classes named exist; `PROTOCOL_VERSION "31"` = `payload-registry.txt` `version 31`; only `AntecedentEffectPacket` is `playBidirectional` (`PacketHandler.java:338`; 111 toClient, 61 toServer); `runData` runs exactly three providers (`DataGeneration.java:19-23`; `DynastyCastlePieceProvider` dormant); `src/generated/resources` has no lang/recipe twins; `hemomancy.mna.mixins.json` is not listed in `neoforge.mods.toml`; `build.gradle:76-77` excludes `compat/mna/**` and `compat/curios/**`; local jars for TerraBlender, JEI, Create, Building Gadgets 2, WorldEdit; `duplicatesStrategy EXCLUDE`; `settings.gradle:11-14` composite HutosLib; `alphaCheck` = test + `verifyGameTestResourceLog` + `verifyPackagedClientResources` (asset paths, PNG headers, font license), as AGENTS.md says.
- The tracked-tools list matches `.gitignore:69-85` exactly; `gradle/verify-dependency-bundle.ps1`, `gradle/source-text-impact-map.py`, `review-clients.gradle` table exist.
- Forbidden patterns absent from core code: `net.minecraftforge`, `SimpleChannel` (comments only), `BlockEntityRenderers.register` (only in excluded MnA compat), `bus =`, `RenderGuiEvent`; `WorldEffectDispatcher` is the only `RenderLevelStageEvent` subscriber; no `net.minecraft.client` under `common/`; top-level packages equal `PackageLayoutGuardrailTest` lists; 94 `getPersistentData()` files = 94 allow-list rows; `CreativeTabInit` WIP list; `RegistryLookup.byName`; `registerComponentType`; `/hemo` requires permission 2 with 18 subcommand classes; dialogue inquiry layout `<npc>/<namespace>/<item>.json` (385 files); five GameTest namespaces AGENTS.md names exist.

**Topic docs (area 2)**
- Every class, data folder, review-client row and GameTest namespace in the 18 "Where it lives" tables resolves; all 50 review rows used by docs exist except `chamberReviewClient` (DC-79); every `-P` property in the docs is defined in `build.gradle`, `gradle/*.gradle`, `tools/review/*.gradle` or `src/test/*.init.gradle`.
- ENDGAME_PATHS rulings: `DegreeProgression.advance(p, 8)` path defaults, `hearsTheHive = !isSilentium`, PHYSICAL travel only for Apotheos, Apotheos body numbers (hunger 17, burn 25/6/100 t), Gardens network 8/24/32/12/600/4, heart-node 12/8/6/3 min/+4, possession forms and Mind Broken 6000, adipocere rates, Visitation cost.
- SAINT_MAUSOLEUMS: set 40/12, ward D8 radius 12, hallowed ground 4/s then 1/s, Chapel numbers, ring door 200 × 5 = 1000 and 4-tick grace, enrage values, three fragments, tendency pairs, Recollection cysts and `mayTake`.
- BRINED_COURT / PELAGIC_*: set spacings and salts, landmark radius 96, schools 1024 and 4-6, members' heal 8 / 1200 t, Warden 160, Champion 220, lesson ×1.5 at D5, polyp 1 in 40; Pelagic layer bands and mob caps.
- MORPHLING_STRAINS: every sampled per-stage number in all eight `<Strain>Rules`, bands 250-450 / 12-90 s, home bonus 1.5/1.25, bonding 50/100/200.
- CIRCUS: acclimation 150/500/1000, Thread Ripper 30 t / 24 blocks / 35 blood / 240 t, pavilion 48/16, camps 24/8, professor degrees (and `wiki/Crimson-Troupe.md:7-15`, which matches the seven trial JSONs).
- CARDINAL_RITES: 20 Working crafts (all `making`, `bloodCost 0`), floorless roster of 11, four special rites, bed sizes 3x1-9x4, ring cost `50·(4+r)/4`, rite-space storage and the single `rite_bound/world` member.
- STATION_UPGRADES: all 14 catalog tiers (degrees, kits, rites, circuits, 50/250 mL), the 14 brazier signatures, Alembic 5000→7500 and ×2.
- NPC_DIALOGUE_AND_TRADE / NPC_VOICES: route order, 56 cards in 29 speakers (= `NpcTradeGameTests.SHIPPED_CARDS`), tiers 0/10/30/60, Unstained refusal `hasDegree(player, 1)`, `never_sold` contents, migrations 10-11; every trade-speaker key block matches `en_us.json`.
- BOSSES: Vesper 520/640 HP, wounds 0.72/0.42/0.12, Mycophant 720/20/16, roar 150 / rush 190, Priestess 420, Annetta break 2400 t / 50 damage; DEEP_DARK_RECORDING_REWRITE: all 16 transcript lines equal the `hemomancy.antecedent.recording.*` keys and three OGG entries; DEPENDENCY_PROVISIONING: the bundle lists exactly the nine active `libs/` jars.

**Reference (area 3)** — matched values, by section:
- §2-§3: max blood 5000 (+500 Eternal Covenant, ×1000 Spleen, ×500 Capacity), slots 3 / cap 9, 58 of 58 §3.1 rows except those in DC-30 (all `wills` defaults), all §3.2 render rows, all 13 MnA rows.
- §5: D1 proof 500 mL, First Draws 5 vials, Condenser 12 values, gourd tiers, rank-rite degrees 3-7, Rooted Vein (D2, lesser Dominion, 4 anchors, 50 mL), recall 50+30, instability 40/70, Exsanguination 200 mL, 19 floor JSONs, whisper intervals, first projection 2400 t, Mycophant hunt 18000 / retry 6000 and claim rules, Fane lobes 40/32 and stakes 3/12, Chamber tier radii 4/6/8/10 and chair budgets 5/10/20 min, Vesper coverage table (all 123 manipulations).
- §6-§19: purity stages and penalties, starting purity `50 − 10 × degree`, Tears +25 / Wreath +5, clarity stages, §7 reset fields; all 108 active §8.2 rows (cost, cooldown, rank, type, tendency, section) and 15 retired rows; rank gates 0/1/3/5/6; charge rules; Avatar table; skill formulas; 24 scar attribute values; Still Waters and every Unstained rite duration; Morphling bands and the eight strain rows; puppet summon rows (6 of 7); Drudge config defaults.
- §20-end: armour defence 3/6/8/3, toughness 3.0, set bonuses; gourd/flask/jug capacities; staff rates and weapon-form rules; centrifuge, incubator, lantern, loadout, Earthen Vein costs; all 9 floorless Blood Structures and all 20 Working crafts; anchor costs 50-138; Blood Moon cadence and effects; structure set spacings; Hemopothecary trades; keybinds J/K/C/Left Alt/N; command ranges; all 44 documented advancement keys.

**Lore reference (area 4)**
- The 2026-10-31 rulings: Zealot cure founder-only (LR:35, 496, 507-510 = `UnstainedEntryRules.canBeginCure`), Silentium cut from the hive (LR:386 = `hearsTheHive`, apart from DC-58), Monolith D5 stand-in (LR:442, 452 = `MIN_DEGREE = 5`), Silent Archon powers D8 Silentium only (LR:426-428 = `SilentArchonArmorRules.hasPower`), the Lady clearer (LR:620). Saints: D8 ward and D3-4 outer halls, unkillable and tomb return, titles (lang 107-115), tendency pairs, no doctrine framing. Vesper he/him throughout. Degree titles (LR:326-336) match lang. Seven tests' pins all hold (`LoreCanonAuthoritySourceTest`, `SettledLoreSynchronizationSourceTest`, `ApotheosFloorFunnelSourceTest:1047-1050`, `HarbingerArtificerNpcSourceTest:106`, `PuppeteerIntegrationSourceTest:104`, `HarbingerArtificerAssignmentSourceTest:211`, `HarbingerArtificerFittingSourceTest:173-185`).

**Wiki (area 5)**
- Crimson-Troupe professor/shape/degree table, Harbinger-Path D1 proofs (`:35`), the D8 table (`:100-106`), Brined-Court membership at D5, Mortarbound numbers, Configuration's common/client keys all match code. Every pinned wiki string is present.
- A script comparing every bold name against `en_us.json` display names and registry ids found no invented names in Phlegethontic-Nether, Clinical-Blood-Tools, Injectable-Blood-Vials, Crimson-Troupe, Cortical-Drift, Hematic-Succession, Advanced-Mechanics, The-Qliphoth, Saints-and-Encounters or Brined-Court (the newer pages); Blood-Systems' enzyme names all match lang.
- No wiki page is about a removed feature: every feature page maps to registered code (Mortarbound, Naeglerophaeon, Clairaudiograph, Phlebotomist's Cabinet and Field Case, Hematic Microscope, Ferric/Ductilis); the `*-Validation` pages are dated evidence logs. Retired manipulations appear only as "retired" (`wiki/Flammeus-Congeatio-Visuals.md:11`) apart from DC-65 and DC-74.

**Tests (area 7)**
- No `@Disabled`; the only skip is DC-82 (skill tree editor). `LegacyMainTestAdapterTest` runs every `*Test` with a `public static void main`; `PrivateTestHelperRatchetTest` caps private helpers at 593.
- 97 positive and negative doc/wiki pins in 27 test classes checked by script against the current text: none broken. `LoreCanonAuthoritySourceTest`'s six strings checked by hand: present.
- GameTest catalogue: every `@GameTestHolder` namespace is in `gradle/game-tests.gradle` and vice versa; every `*_validation` namespace named in AGENTS.md, the topic docs, the wiki and TESTING.md exists. Every `run<Task>` named in TESTING.md, PLAYTHROUGH_TESTS.md and the topic docs resolves (`runCastingCaster/Observer` come from `tools/review/casting-animation-review.init.gradle`); `-PsuiteProbe` is in `src/test/gametest-suite-registration.init.gradle`; `/hemo test` command tree in TESTING.md matches `HemoTestCommands`.
- The impact map names no missing file (all 581 tests and 1,846 targets exist). Allow-lists (`persistent-data-allow-list.txt`, `english-literal-allow-list.txt`, `payload-registry.txt`) exist.

**Archive (area 8)**
- `docs/archive/README.md`'s folder map matches the folders; every relative link into `archive/` from live docs resolves on this machine (paths with spaces included); history pointers in the topic docs resolve. Brined Court track questions, the mausoleum §3.8 calls and the npc_trade Q-ids were all lifted into live docs.
