# Resource and data orphans, lang hygiene and data validity

Date: 2026-11-02. Read-only audit of `neo-1.21.1` at `8d01a316c` plus the working tree. Scope: the non-Java half of dead
code. Every JSON, NBT, OGG, texture, shader, lang key and tag that is unreferenced, malformed, duplicated or
inconsistent. Nothing was built or run. Block and item model integrity belongs to another report; this one covers model
orphans only where they meet another asset class. Where a sibling report in this folder already holds a finding, the
entry here is short and names it (MT-, PG-, EC-, AN-, SY- ids).

## Summary

The resource tree holds 5,037 JSON files (2,806 data, 2,228 assets), 226 structure NBTs, 2,226 PNGs, 171 OGGs and one
locale, `en_us.json`, with 10,305 keys and no duplicate keys. Data is in good shape. Every JSON parses, and every
recipe, loot, tag, advancement, worldgen, biome-modifier, dimension and damage-type reference resolves against the
registries and the vanilla and NeoForge jars, with the exceptions listed below. Every palette block in all 226 NBTs is
registered. The clutter sits at the edges: **311 lang keys are verified dead** (58 with no reference at all, 164 for 41
potions that were never registered, 24 item-inquiry lines with no inquiry file, 48 name keys for items, entities and
blocks that no longer exist, 17 referenced only by tools, tests or excluded compat). Another 104 are suspected. Then
come **57 OGG files no sound event plays (35 MB of the 62.7 MB of sounds)**, 137 unreferenced textures plus 220 in `old/`
folders, 30 unreachable structure NBTs, two whole unused post-shader pipelines, and two recipe input conflicts that make
four items uncraftable. On the lang side, 16 keys the game shows are missing (death messages, a biome name, two banner
colours, a Consecration message pair, an advancement), four live keys use `%d`/`%.0f` (Minecraft shows them raw), and
two values are themselves keys. There are **40 findings**: 1 High, 10 Medium, 29 Low.

Method. Python scripts (run with `python -I`, kept in the session scratchpad) did the work:

- They built the registry set from `common/init` and checked it against the committed dump
  `src/gameTest/resources/frozen/registry-ids.txt`. They matched exactly, except for 8 morphling effects registered in
  a loop; those came from the dump.
- They read the vanilla, NeoForge 21.1.251 and HutosLib ids from the local Gradle-cache jars, in memory.
- They tokenised all 4 Java roots (main, gameTest, test, excluded compat): 17,443 main literals, 908 concatenation and
  `%s` patterns with variable expansion.
- They walked every JSON (13,235 distinct strings) and every NBT, with a small gzip NBT reader written for this.

Lang key tiers: 4,839 exact Java literals, 1,070 exact JSON strings, 961 registry naming conventions, 2,976 dynamic
patterns whose variable parts were found as real ids, and 459 left over for the orphan analysis below.

---

## 1. Lang (`assets/hemomancy/lang/en_us.json`)

**RS-1 (Medium). 311 lang keys are dead (Verified).**
- Evidence (full lists in Appendix A1 to A5):
  - 58 keys have no reference in Java, JSON, tools, tests, docs or HutosLib (A1). Among them: 12 `memo.hemomancy.antecedent_*`
    titles, 7 `message.hemomancy.memo.*`, 6 `hemomancy.unstained.*` (one, `purity_progress`, also has the `%.0f`
    problem of RS-3), 4 `hemomancy.brazier.*`, 3 `hemomancy.memory_of_vesper.*`, `screen.hemomancy.virtual_field_notes.{status,total}`
    (HutosLib now uses its own `screen.hutoslib.virtual_field_notes.*`), and the MnA leftovers `_comment_mna_spell_components`
    (`en_us.json:125`), `mechanic.mna:hemomancy` and `hemomancy:living_armor_set_bonus`.
  - 164 keys `item.minecraft.{potion,splash_potion,lingering_potion,tipped_arrow}.effect.<name>` cover 60 potion names,
    but only 19 potions are registered (`EffectInit.java:74-190`). The other 41 names have no `Potion` and no reference
    anywhere (A3; the block starts at `en_us.json:4351`).
  - 24 `hemomancy.mnemonist.item_inquiry.<item>.line1/2` and `hemomancy.vicar.item_inquiry.scar_binder.*` keys. Inquiry
    lines come only from `data/hemomancy/dialogue_inquiry/<npc>/` JSON (`ItemInquiryLoader`), and no file names them
    (A4).
  - 48 name keys for ids that are not registered: 34 `item.hemomancy.*` (MnA/Curios items, five removed spawn eggs,
    `field_notes`, `saint_enzyme`, `roasted_gourd_seeds`...), 6 `entity.hemomancy.*` (`abyssal_siphon`, `cruor_fiend`,
    `frozen_clot`, `harbinger_saint`, `synapse_hound`, `void_drinker`), 4 `block.hemomancy.*` and 4 `item.hemomancy.<block>`
    where the block item reads `block.hemomancy.<id>` (A5).
  - 17 keys are named only by `tools/` scripts, a test, a comment or the excluded `compat/mna` (`hemomancy.occulus.task.*`
    x7) (A2). Thirteen `tools/i18n/voice_rewrite` snapshot files (`en_us.before.json`, `diff.json`, `chunks/*`) were not
    counted as references.
- Confidence: Verified (each family grepped by its last segment and parent prefix in main Java; the item-inquiry and
  potion families checked against their only loaders).
- Action: delete. Effort: S (one scripted pass over the lists; re-run `EnglishLiteralAllowListTest` and
  `HardCodedEnglishGuardrailTest`).

**RS-2 (Medium). Sixteen keys the game shows are missing; 20 more are missing on rarer paths.**
- Evidence:
  - `hemomancy.consecration.not_ready` and `hemomancy.consecration.success`: `common/event/ConsecrationHandler.java:91,121` (live subscriber).
  - `hemomancy.jar.empty`: `common/item/harbinger/morphlings/ItemMorphlingJar.java:62` (tooltip of `morphling_jar`).
  - `advancements.hemomancy.first_bloodcraft_reward_claimed.{title,description}`: `data/hemomancy/advancement/hemomancy/first_bloodcraft_reward_claimed.json:10,19`.
  - `biome.hemomancy.cortical_drift`: `data/hemomancy/worldgen/biome/cortical_drift.json` exists and is registered (frozen dump), but there is no name.
  - `death.attack.pale_intercession`, `.player`, `death.attack.vesper_scute`, `.player`: `data/hemomancy/damage_type/pale_intercession.json`, `vesper_scute.json`; both are used (`common/damage/HemoDamageTypes.java:35,47`).
  - Banner and arm-banner colours: Minecraft 1.21 names the colours `light_blue` and `light_gray`
    (`DyeColor.getName()`, used by `HutosLib ItemArmBanner.getDescriptionId`). The lang has the pre-1.13 `lightBlue`
    and `silver` instead (`en_us.json:860,867,876,883,3620` and the arm-banner `silver` line). So
    `block.minecraft.banner.hemomancy_{heart,veins}.{light_blue,light_gray}` and
    `item.hemomancy.chitinite_arm_banner.{light_blue,light_gray}` are missing, and the six old keys are dead.
  - `fluid_type.hemomancy.white_humor`, `block.hemomancy.white_humor_block`, `entity.hemomancy.gore_wound_harpoon`:
    registered, unnamed. Low visibility.
  - Rarer branch: all 17 damage types lack the `.item` variant, which vanilla uses when the killer holds a renamed item.
- Confidence: Verified.
- Action: fix (add the keys; rename `lightBlue`/`silver` to `light_blue`/`light_gray`). Effort: S.

**RS-3 (Medium). Four live keys use `%d` or `%.0f`; Minecraft only formats `%s`, so the raw template is shown.**
- Evidence: `TranslatableContents` throws on any format other than `s` and `%`, and then shows the template text.
  - `hemomancy.dialogue.recruit.pool_increased` (`en_us.json:2168`), called at `common/entity/npc/dialogue/route/RecruitmentDialogueEvents.java:122`.
  - `hemomancy.ledger.disband.success` (`en_us.json:2566`), called at `common/network/capa/harbinger/PacketLedgerAction.java:335`.
  - `hemomancy.ledger.summon.success` (`en_us.json:2577`), called at `PacketLedgerAction.java:186,429`.
  - `hemomancy.unstained.clarity_progress` (`en_us.json:3167`), called at `common/block/harbinger/functional/UnstainedPodiumBlock.java:211`.
  - The dead twin `hemomancy.unstained.purity_progress` has the same problem.
  - Also counted: 77 values use a lone `%` ("5% per level"). These render correctly only because the whole template
    falls back to raw text. Eight other values escape it as `%%`, so the convention is mixed.
- Confidence: Verified (format rule); the on-screen result was not watched in a client.
- Action: fix (`%s`, with the number formatted in Java or passed through `HemoText`). Effort: S.

**RS-4 (Medium). Two values are lang keys themselves, so players read the key text.**
- Evidence:
  - `tooltip.hemomancy.living_crossbow.item_minecraft_crossbow_projectile` = `"item.minecraft.crossbow.projectile"` (`en_us.json:6974`), shown at `common/item/harbinger/tool/living/LivingCrossbowItem.java:383`.
  - `screen.hemomancy.radial_menu.text_toolbelt_empty` = `"text.toolbelt.empty"` (`en_us.json:7212`), shown at `client/screen/radial/RadialMenuScreen.java:99,116`.
  - Both look like the O-7 extraction wrapped a vanilla or foreign key as English.
- Confidence: Verified.
- Action: fix (`Component.translatable("item.minecraft.crossbow.projectile")`, or real English). Effort: S.

**RS-5 (Low). The lang guardrail has blind spots that let RS-2 through.**
- Evidence:
  - `HardCodedEnglishGuardrailTest.KEY` (`src/test/java/.../architecture/HardCodedEnglishGuardrailTest.java:39`) only
    checks keys shaped `<family>.hemomancy.<...>`. So `hemomancy.consecration.*` and `hemomancy.jar.empty` are never
    checked.
  - Nothing checks `"translate"` in data JSON (the advancement), or the convention keys (biome, damage type, banner
    colour, fluid type).
- Confidence: Verified.
- Action: fix (extend the test, or add a resource test that derives convention keys from the frozen registry dump and
  the data folders). Effort: S.

**RS-6 (Low). Argument count disagrees with the value.**
- Evidence:
  - `item.hemomancy.husk_effigy.tooltip` gets 2 arguments and has 0 placeholders (`HuskEffigyItem.java:61`, `en_us.json:3784`).
  - `item.hemomancy.memory_thread.tooltip` gets 1 argument and has 0 (`MemoryThreadItem.java:90`, `en_us.json:3962`).
  - The values carry no placeholder, so the arguments are ignored.
- Confidence: Verified.
- Action: fix (restore the placeholders or drop the arguments). Effort: S.

**RS-7 (Low). 932 keys share their English with another key (393 groups).**
- Evidence (top 40 in A7):
  - Copy-paste families from the O-7 extraction:
    - "That manipulation has gone dormant." under four packet-class keys (`message.hemomancy.{blood_manipulation,equip_manipulation_packet,update_current_manip_packet,use_manip_key_packet}.*`);
    - "That pattern exceeds your current scar capacity." x4;
    - "Your blood is not yet awakened." x4;
    - "Result:" and "Materials:" x4 each, across four screens.
  - Possible error: `milestone.hemomancy.chapter.first_distillation` = "The First Separation" (`en_us.json:8973`), the
    same as `chapter.first_separation`.
  - Many groups are legitimate (boss bar name equals entity name, "[Leave]" per NPC voice).
- Confidence: Verified (counts); Suspected for which groups should merge.
- Action: merge the copy-paste message families into one shared key each; check the milestone title. Effort: M.

**RS-8 (Low). Formatting noise in values.**
- Evidence:
  - 309 values carry raw `§` codes, mostly `§o`/`§4`/`§7`. The largest families are `hemomancy.lady` 61,
    `hemomancy.whisper` 61 and `message.hemomancy` 58 (A9).
  - 92 values have leading or trailing spaces. These are glue for concatenation in code, such as
    `commands.hemomancy.blood.blood` "Blood: " (A8).
  - 14 values have double spaces. `item.hemomancy.chitinite_arm_banner.red` reads "Red  Arm Banner" (`en_us.json:3626`)
    where the word "Chitinite" is missing. `...lightBlue` reads "Light Chitinite Arm Banner".
- Confidence: Verified.
- Action: fix (styles in code, one key per message instead of glued pieces; the arm-banner names). Effort: M.

**RS-9 (Low). 104 more keys are suspected dead.**
- Evidence:
  - These are matched only by a broad Java pattern whose variable part has no evidence, or only by a test's pattern (A6).
  - Spot checks moved 85 keys of this tier back to live (rapport `.teaser`, `blood_property.*`, Antecedent `initial_*`,
    `living_weapon_form.*.graft`).
  - The likeliest dead among the rest:
    - `block.hemomancy.consecrated_bloodwell.{cannot_deposit,deposited,duplicate_heart,empty_or_full,not_in_fane,withdrew}`;
    - `block.hemomancy.hematic_armature.{tier.*,upgrade_*,cornerstone_requires_vicar}` (the station-upgrade rework moved these);
    - `hemomancy.dialogue.event.alchemist_red_taxonomy_placeholder_reward`;
    - `key.hemomancy.{morphjarpickup,openjar,scarbinderpickup}.desc`.
- Confidence: Suspected.
- Action: read each family's builder before deleting. Effort: M.

**RS-10 (Low). 22 rites have no lang name or description; their English comes only from the recipe JSON fallback.**
- Evidence:
  - `CardinalRiteRecipe.displayName()` uses `translatableWithFallback("hemomancy.rite.<id>.name", riteName)`
    (`common/recipe/CardinalRiteRecipe.java:359-380`).
  - No keys exist for `druse_crystallization`, `first_clotting` and all 20 `cardinal_rite/working/*` crafts.
  - The derived key for a Working craft contains a slash (`hemomancy.rite.working/covenant_throne.name`).
- Confidence: Verified.
- Action: fix (map `/` to `.` in `riteKeyId`, add the 44 keys). Effort: S.

## 2. English literals

**RS-11 (Medium). Discovery inscriptions show datapack English through `Component.literal`.**
- Evidence:
  - `common/block/inscription/DiscoveryInscriptionBlock.java:204` sends `Component.literal(definition.title())` and
    the raw `revealed_text`/`obscured_text` strings of the 30 files under `data/hemomancy/discovery_inscription/`
    (21 titles, 8 obscured texts, 1 revealed text found by a three-word scan).
  - AGENTS.md says datapack JSON text goes through `Component.translatableWithFallback`.
  - The guardrail passes this because the argument is not a string literal.
  - Ichorian sigils do it correctly (`IchorianSigilDefinition.java:41,46`).
- Confidence: Verified.
- Action: fix (key per inscription with the JSON text as fallback, like sigils). Effort: S.

**RS-12 (Low). English built from ids, or drawn as a plain `String`.**
- Evidence:
  - `common/entity/npc/dialogue/inquiry/StackAwareInquiryRegistry.java:46` turns a blueprint's recipe id into Title
    Case English (`proper()`, `:94`) and sends it as an `@literal:` dialogue line.
  - `client/screen/tile/functional/ClairaudiographSpectrograph.java:43` draws `"-80 ... 0 dBFS"` with
    `drawString(font, String, ...)`.
  - `SummonsSubcommand.java:125` shows `Component.literal(definition.name())`, but it is operator-only.
- Confidence: Verified.
- Action: fix the first (show the recipe's own display name); the axis label is borderline. Effort: S.

**RS-13 (Low). The 138 Liber book JSON files are English-only.**
- Evidence:
  - `data/hemomancy/books/**`: `title`, `subtitle` and `text` are plain strings rendered by HutosLib's book pipeline,
    which has no key or fallback path (no `translatable` in `HutosLib/.../client/book`).
- Confidence: Verified.
- Action: keep for now; it is a HutosLib design decision (AGENTS.md: HutosLib owns the book pipeline). Record it in the
  i18n follow-up. Effort: L if pursued.

## 3. Data validity

**RS-14 (Medium). Two crafting recipe conflicts make four items uncraftable at a crafting table.**
- Evidence:
  - `data/hemomancy/recipe/{blank_mask,laughing_mask,mourning_mask,troupe_rigging}.json` are the identical shaped grid
    (8 sticks around 1 puppeteering thread). The table outputs one result.
  - `recipe/gourd_seeds.json` and `recipe/gourd_slice.json` are both shapeless 1 gourd. No conflict with vanilla
    recipes was found.
- Confidence: Verified.
- Action: fix (give each a distinct input). Effort: S.

**RS-15 (Medium). Four hidden marker advancements are roots with a display, so each opens its own advancement tab when earned.**
- Evidence:
  - `advancement/hemomancy/{artificer_weight_of_frame_lesson_ready,first_bloodcraft_reward_claimed,vein_mason_first_effigy_pattern,vein_mason_reward_claimed}.json`
    have `display` with `hidden: true`, no `parent` and no `background`. In 1.21.1 a completed root with a display gets
    a tab, drawn with the missing texture as background.
  - The other 29 marker roots have no `display`.
  - `first_bloodcraft_reward_claimed` also lacks its keys (RS-2).
- Confidence: Verified (data); Suspected (tab visuals, not watched in a client).
- Action: fix (drop `display`, as the 29 others do). Effort: S.

**RS-16 (Medium). The Crimson Lodge Annex places a template that does not exist (known: EC-26, SY-14).**
- Evidence:
  - `worldgen/structure/crimson_lodge_annex.json`, `worldgen/structure_set/crimson_lodge_annex.json` (spacing 40) and
    a non-empty biome tag `tags/worldgen/biome/has_structure/crimson_lodge_annex.json` (forest, taiga).
  - The pool element is `hemomancy:crimson_lodge_annex` (`worldgen/template_pool/crimson_lodge_annex/start_pool.json:8`),
    which has never been committed.
  - `StructureTemplateCoverageTest.java:27` records it as kept on purpose. The test comment says the structure "cannot
    generate", but its starts are still placed (empty), and `discovery_inscription/crimson_lodge/minutes_1..6.json` are
    unreachable.
- Confidence: Verified.
- Action: decide (author the template, or empty the biome tag so the set really is dormant). Effort: S to M.

**RS-17 (Low). A stale recipe advancement points at a recipe that moved to HutosLib.**
- Evidence:
  - `advancement/recipe/hemomancy/raw_clay_flask.json` rewards and requires `hemomancy:raw_clay_flask`, which does not
    exist (the recipe is `hutoslib:raw_clay_flask`).
  - It also uses the 1.20 predicate `{"item": "minecraft:clay_ball"}`, which 1.21 ignores, so it matches any item.
- Confidence: Verified.
- Action: delete. Effort: S.

**RS-18 (Low). Four item-inquiry files are keyed to items that do not exist.**
- Evidence: under `data/hemomancy/dialogue_inquiry/`:
  - `cicatrix_anchorite/hemomancy/masons_effigy.json` (the block is `mason_effigy`);
  - `mnemonist/hemomancy/scar_binder_upgraded.json`;
  - `vicar/hemomancy/blood_structure_hint.json`;
  - `votary_wayfarer/minecraft/boat.json` (1.21 has no `minecraft:boat`).
- Confidence: Verified (against the item registry and vanilla).
- Action: rename `masons_effigy` to `mason_effigy` and `boat` to `oak_boat`; delete the other two. Effort: S.

**RS-19 (Low). Three blood profiles sit in the wrong namespace and are never read.**
- Evidence:
  - `data/hemomancy/blood_profiles/{armadillo,bogged,husk}.json` resolve to `hemomancy:husk` etc., which are not
    entities. Profiles are looked up by entity id (`BloodProfileData.Snapshot.get`).
  - The live copies are in `data/minecraft/blood_profiles/`, and their content differs: the `hemomancy` husk adds
    `flammeus`.
- Confidence: Verified.
- Action: merge (decide which content is intended), then delete the `hemomancy` copies. Effort: S.

**RS-20 (Low). Dormant compat and test data ship in the jar.**
- Evidence:
  - `data/curios/tags/items/{belt,spell}.json` list `hemomancy:harbinger_grimore`, which is not registered;
  - `data/mna/tags/items/dusts/purified_vinteum.json` lists `hemomancy:befouled_vinteum_dust`, also not registered;
  - `data/forge/tags/fluids/test_fluid.json` lists `hemomancy:test_fluid`.
  - All three use pre-1.21 plural tag folders and the dead `forge` namespace, so 1.21 ignores them.
- Confidence: Verified.
- Action: delete (or move the curios/mna files under the excluded compat source sets). Effort: S.

**RS-21 (Low). Two worldgen files are referenced by nothing.**
- Evidence:
  - `worldgen/configured_feature/tree/selector/vanilla/vanilla_mushrooms.json` is used by no placed feature or Java.
  - `worldgen/noise/fungal_noise.json`: the live noises are `noise/fungal/*`.
- Confidence: Verified (grep of id and path).
- Action: delete. Effort: S.

**RS-22 (Low). 64 JSON files start with a UTF-8 byte-order mark.**
- Evidence:
  - 59 under `data/hemomancy/recipe/` (e.g. `barbed_blade.json`, `foul_paste.json`) and 5 under
    `assets/hemomancy/models/world/lowtide_ruin/` (A15).
  - Gson skips a leading BOM, so the game loads them. Python's `json.load` and other strict readers fail on them.
- Confidence: Verified.
- Action: fix (strip the BOM). Effort: S.

## 4. Structure NBTs (`data/hemomancy/structure/`)

**RS-23 (Low). 30 NBTs are reachable from nothing (extends MT-1 and PG-14).**
- Evidence (A12):
  - `antecedent/vigil/{archive,entrance,gallery,passage}.nbt`: the Vigil places only `vigil/modular/*` from
    `data/hemomancy/antecedent/vigil_layouts.json`. The six `modular/*/complete.nbt` are read only by
    `AntecedentGameTests`, so they stay.
  - The 8 `lowtide_ruin/*.nbt`: the Chamber of Will draws OBJ models of the same names.
  - `tower/blood_tower_core.nbt` and `tower/blood_tower_top_1.nbt`, with their loot table `loot_table/chests/blood_tower.json`.
  - `qliphoth_fane.nbt` (named only by excluded `compat/mna`), `qliphoth_bloom.nbt`, `harbinger_outpost.nbt` (the
    outpost uses `harbinger_outpost/*`) and `broken_unstained_church.nbt` (the pool uses the tainted variant).
  - The 11 legacy mausoleum NBTs (RS-24) and the generated `circus_pavilion.nbt` (RS-33).
  - `dynasty_castle/*` is live: `DynastyCastleStructure.java:93` builds the ids.
- Confidence: Verified.
- Action: delete (the user's call only for `lowtide_ruin`, which may be a regeneration source). Effort: S.

**RS-24 (Low). M-6 legacy `mausoleum` structure: the exact retirement list.**
- Evidence:
  - Data:
    - `worldgen/structure/mausoleum.json`;
    - `worldgen/structure_set/mausoleum.json`;
    - `tags/worldgen/biome/has_structure/mausoleum.json` (empty);
    - seven pools `worldgen/template_pool/mausoleum/{antechamber,sarcroom,stairs,start_pool,top,treasureroom,trialroom}.json`;
    - ten NBTs `structure/mausoleum/{antechamber2way,antechamber4way,sarcroom2way,sarcroom4way,stairs,top,treasureroom2way,treasureroom4way,trialroom2way,trialroom4way}.nbt`;
    - an eleventh, `structure/mausoleum.nbt`, reachable only through the unused `start_pool`. 21 files, 52 KB of NBT.
  - Code that names it:
    - `common/worldgen/structure/court/CourtLandmarkCells.java:25` (`Hemomancy.rloc("mausoleum")` set);
    - `StructureTemplateCoverageTest.EMPTY_BIOME_TAGS` (`"mausoleum"`).
  - Keep `StructureInit.mausoleum` and `MausoleumStructure`: the four Saint structures use the `hemomancy:mausoleum`
    type. Keep `loot_table/chests/mausoleum*.json` and `discovery_inscription/mausoleum/*`: the Saint pieces use them.
  - Also: the legacy `trialroom4way.nbt` jigsaw targets a pool that does not exist
    (`hemomancy:mausoleum/antechamber_trial_out`).
  - Seven of the ten NBTs are still DataVersion 3465 (1.20.1).
- Confidence: Verified.
- Action: delete per M-6 (default "Retire"). Effort: S.

**RS-25 (Low). 18 NBTs are saved at an older DataVersion and are upgraded by DataFixerUpper at every load.**
- Evidence:
  - 15 at 3465 (1.20.1): the five `*_hemopothecary.nbt` village houses, `harbinger_outpost.nbt`, `qliphoth_bloom.nbt`,
    `qliphoth_fane.nbt`, `mausoleum.nbt` and six legacy mausoleum rooms.
  - One at 3953, `hermitage_remnant.nbt`.
  - Two at 2975 (1.18.2): `tower/*`.
  - Everything else is 3955 (1.21.1) (A13).
- Confidence: Verified.
- Action: fix (re-save the live ones; the rest go with RS-23 and RS-24). Effort: S.

## 5. Assets

**RS-26 (High). 57 OGG files, 35 MB, are named by no sound event (known: AN-15).**
- Evidence:
  - `assets/hemomancy/sounds/entity/{beast_from_beyond,colin,dark_young,denizen,dread_bot,hastur,samples,seraphim,tentacle}/`
    and `sounds/item/{bow_blade,star_slug}/` (A11). That is 56% of the 62.7 MB sounds folder, and it ships in the jar.
  - Four music tracks are 31.5 MB of it: `hastur/music.ogg` 15.8 MB, `beast_from_beyond/music.ogg` 7.8 MB,
    `dark_young/music.ogg` 5.0 MB, `seraphim/music.ogg` 3.0 MB.
  - Several are byte-identical to live files: `hastur/{ambient,death,hit,hurt}` = `vesper/*`, `tentacle/*` =
    `abhorrent_thought/*`, `dark_young/{ambient,death,hurt}` = `mycophant/*`.
  - All 188 sound events are registered, all have `sounds.json` entries, and all are used.
- Confidence: Verified.
- Action: delete. Effort: S.

**RS-27 (Medium). Tooth Pecks names three OGG files that do not exist (known: AN-10).**
- Evidence: `sounds.json:996-1013` (`entity.tooth_pecks.{ambient,hurt,death}` -> `hemomancy:entity/tooth_pecks/*`);
  there is no `sounds/entity/tooth_pecks/`. Played at `ToothPecksEntity.java:161,208-218`.
- Confidence: Verified.
- Action: fix. Effort: S.

**RS-28 (Low). Authoring files ship in the jar.**
- Evidence (all outside the `build.gradle` excludes):
  - 3 `.wav` (`sounds/entity/colin/colin{ambient,death,hurt}.wav`, 0.75 MB);
  - 8 `.xcf` GIMP sources (`textures/entity/blood_gourd/gourd.xcf`, `textures/environment/chamber_of_will_sky.xcf`,
    `textures/gui/blood_overlay/base_degree_8.xcf`, `textures/gui/gui_mind_runes.xcf`,
    `textures/item/{asce,heart,moon}.xcf`, `textures/particle/blood_avatar_hit.xcf`);
  - `textures/mob_effect/vincent_huto_light_purple_hemolysis_43a4a5d2-....webp` (an image-generator export).
  - The 10 `.mp3` and the `colin/OLD*.ogg` are already excluded.
- Confidence: Verified.
- Action: delete, or add them to the resource excludes. Effort: S.

**RS-29 (Low). 137 textures are unreferenced (1.6 MB); 220 more sit in `old/` (2.5 MB, excluded from the jar).**
- Evidence (A10):
  - 29 `textures/font/abocipher/*`, byte-identical to `textures/particle/abocipher/*`, which the `abocipher` particle uses;
  - 14 `textures/mna/*` (excluded compat);
  - 11 `entity/drudge/*` colour variants and `*_old (n).png`;
  - 7 `environment/*` vanilla-copy skies (`clouds.png`, `end_sky.png`, `rain.png`, `snow.png`, `sun.png`, `moon_phases*.png`);
  - 17 `particle/*` (`sparkle_*`, `particle_glow*`, `blood_skull`...);
  - 23 `gui/*`, including `virtual_field_notes_{book,button}.png` (HutosLib draws its own copies);
  - `assets/minecraft/textures/entity/{banner,shield}/hemomancy_{heart,veins}.png`, byte-identical copies of the
    `hemomancy`-namespace files that the banner patterns actually use.
  - Every listed name was grepped in main Java and HutosLib source with no hit.
- Confidence: Verified for the A10 list; the `gui/sprites` family is excluded because `DialogueThemeStyle` builds its
  paths.
- Action: delete. Effort: S.

**RS-30 (Low). Two shader pipelines and nine core programs are never loaded.**
- Evidence:
  - Nothing in Java builds a `PostChain` or names `shaders/post`, so all 7 `shaders/post/*.json` and all 16
    `shaders/program/*` files are dead (91 KB).
  - Core programs named by no `ShaderInstance`/`ShaderHolder`: `core/{aoe_gun_projectile,lodestone_texture,radial_menu,runic_energy_flow}`,
    `core/particle/particle`, `core/screen/screen_particle` and `core/shapes/{scrolling_texture,scrolling_triangle_texture,triangle_texture}`
    (JSON and sources), plus the stray `core/generic.vsh`.
  - Unused include: `shaders/include/noise/color_sorting.glsl`.
  - Live, built from a name: `core/world/manipulation_*` (`ShaderInit.java:164`).
- Confidence: Verified.
- Action: delete. Effort: S.

**RS-31 (Low). Particle and atlas leftovers.**
- Evidence:
  - `particles/blue_sparkle_velocity.json` and `particles/trail_orbit.json` have no registered particle type.
  - `assets/minecraft/atlases/blocks.json` lists two sprites whose textures do not exist:
    `hemomancy:gui/entity/royal_guard_shield_base` and `hemomancy:gui/empty_banner_slot_background`.
- Confidence: Verified.
- Action: delete. Effort: S.

**RS-32 (Low). 34 item models are for ids that are not registered items (model auditor's area; listed for the orphan sweep).**
- Evidence (A14):
  - MnA/Curios leftovers (`harbinger_grimore`, `living_thread_*`, `mana_*`, `foul_vinteum_ingot`, `spell_texture`...);
  - seven removed spawn eggs (`spawn_egg_{abyssal_siphon,cruor_fiend,drudge,frozen_clot,synapse_hound,void_drinker,wretched_will}`);
  - `field_notes` (unregistered class, M-3);
  - four generated item models for itemless blocks (RS-34).
  - Separately, the `block/sanguine_pane_{noside,noside_alt,post,side,side_alt}` models are referenced by nothing, and
    they point at the 1.12 paths `hemomancy:blocks/sanguine_pane{,_top}`.
- Confidence: Verified (registry); Suspected for `arm_banner`, `living_grasp` and `runic_chisel_station`, whose names
  appear in Java.
- Action: delete. Effort: S.

## 6. Generated vs authored (`src/generated/resources`)

**RS-33 (Medium). Datagen still writes a Circus pavilion template that nothing places.**
- Evidence:
  - `common/data/gen/CircusPavilionStructureProvider.java:31` writes
    `src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt` (33x12x33, dated September).
  - The live structure starts from `crimson_troupe_pavilion/start_pool` (`worldgen/structure/circus_pavilion.json`) and
    the authored `crimson_troupe_pavilion.nbt` (65x14x65).
  - The generated NBT is reached only by the orphan pool `worldgen/template_pool/circus_pavilion/start_pool.json`,
    which `StructureTemplateCoverageTest.java:28` calls an "orphan pool".
  - AGENTS.md still lists the pavilion provider as one of three `runData` providers.
- Confidence: Verified.
- Action: delete the provider, its output and the orphan pool, or rewire the structure to the generated piece. Update
  AGENTS.md. Effort: S.

**RS-34 (Low). Four generated item models belong to blocks that have no item.**
- Evidence: `src/generated/resources/assets/hemomancy/models/item/{abocipher_emitter,placed_blood_stained_stone,qliphoth_bloom,warp_chair_filler}.json`.
  All four ids are in `BlockInit.ITEMLESS_BLOCKS` or `MANUAL_BLOCK_ITEMS` (`BlockInit.java:1377,1384`).
- Confidence: Verified.
- Action: fix (skip non-item blocks in `HemoItemModelProvider`) and delete. Effort: S.

**RS-35 (Low). 259 authored item models are byte-for-byte what datagen's `basicItem` would write.**
- Evidence: `assets/hemomancy/models/item/*.json` of the form `{parent: item/generated, layer0: hemomancy:item/<id>}`.
  `HemoItemModelProvider` generates exactly that for any item without an authored model (`:78,118-122`).
- Confidence: Verified.
- Action: keep. Deleting them would make datagen own them; that is a style call, not a defect. Effort: S if wanted.

## 7. Duplication

RS-14 (recipe conflicts) and the byte-identical OGG and texture copies in RS-26 and RS-29 are the actionable
duplicates. One more:

**RS-36 (Low). 20 entity loot tables are the same empty table.**
- Evidence: `loot_table/entities/{antibody,blood_lantern_jelly,cinder_bellows,circus_*(8),hagfish,mnemonic_whale,pyrosome,unstained_{acolyte,guardian,scout,zealot},verdigris_moth,will}.json`
  are all `{"type":"minecraft:entity","pools":[]}`. An absent table also drops nothing.
- Confidence: Verified.
- Action: keep (harmless, documents intent) or delete. Effort: S.

The other identical-JSON groups (140 groups, 699 files) are by design: 29 marker advancements in 2 groups, 143 blood
profiles in 19 shared shapes, 157 dialogue-inquiry files in 28 shared line sets, 64 tag files in 23 groups (`rite_bound`,
biome-tag pairs), 7 particle descriptions, the two `manipulation_animus` shader JSONs, and 56 model and blockstate
groups.

## 8. Naming

**RS-37 (Low). 1.12-style texture paths survive in 18 model references.**
- Evidence:
  - `hemomancy:items/liber_sanguinum` in `models/item/{harbinger_assignment_ledger,liber_sanguinum,living_axe,living_blade,living_sickle,living_spear,marrow_crown,sanguis_lancea}.json`;
  - `hemomancy:items/{field_notes,liber_immaculatus}`;
  - the `sanguine_pane_*` parts in RS-32.
  - None of these textures exist. Where it is a `particle` slot, the result is a missing-texture particle. The model
    report owns the fix.
- Confidence: Verified.
- Action: fix. Effort: S.

**RS-38 (Low). Plural folders and the `forge` namespace appear only in the dormant compat data of RS-20.**
- Evidence: `data/curios/tags/items/`, `data/mna/tags/items/`, `data/forge/tags/fluids/`. All Hemomancy paths use the
  singular 1.21 layout.
- Confidence: Verified.
- Action: delete with RS-20. Effort: S.

**RS-39 (Low). Authoring names inside resources.**
- Evidence:
  - 258 files have names that are not valid resource paths (`*.bbmodel` with capitals or spaces, `* - Copy.*`,
    `model_drudge_grey_old (n).png`, `OLD*.ogg`). All are covered by the `build.gradle` excludes, so none ship.
  - The only shipped odd name is the `.webp` in RS-28.
- Confidence: Verified.
- Action: keep, or move the Blockbench sources out of `src/main/resources`. Effort: S.

**RS-40 (Low). Recipe ids agree with their paths; file names mostly match results.**
- Evidence:
  - All 86 recipes that carry an `id` field match their path.
  - Crafting and smelting files whose name does not start with the result are deliberate groups:
    `scar_template/*` -> `scar_pattern`, `living_weapon_graft/*`, `clear_mnemonic_blueprint`,
    `befouling_ash` -> `befouling_ash_trail`, `smouldering_ash` -> `smouldering_ash_trail`.
- Confidence: Verified.
- Action: keep. Effort: none.

---

## Not a problem

- Every JSON under `src/main/resources` and `src/generated/resources` parses. The one exception, `pack.mcmeta`, is a
  `${...}` template expanded by `processResources`.
- `english-literal-allow-list.txt`: re-counted with the test's own rules, all 41 entries match their files exactly;
  there are no stale files and no counts to lower.
- Recipes: all 656 have a registered serializer, and every item and tag reference resolves. The `#hemomancy:` tags in
  rite floors and Working-craft subjects are block tags and exist. There are no conflicts with vanilla recipes.
- Loot tables: every function and condition is a valid 1.21.1 name (no `set_nbt` or `looting_enchant`). Every item
  resolves. No table exists for an unregistered block or entity, and every `random_sequence` matches its path.
- Tags: every entry in all 225 tag files resolves (6 are `required: false` by design).
- Advancements: every parent, trigger and icon resolves (apart from RS-17). Hemomancy registers no custom triggers.
- Worldgen: every placed-to-configured link resolves, and so does every biome feature, carver and spawner. Structures
  resolve to their pools and biome tags, sets to their structures, and pools to their NBTs and processors (apart from
  RS-16). All 42 biome modifiers and both dimensions with their types resolve.
- Damage types: all 17 JSON files are used from Java, and every Java key has a file.
- Structure NBTs: no palette, jigsaw `final_state`, block entity, entity or chest loot table names a missing id in
  any of the 226 files.
- Sounds: 188 registered events = 188 `sounds.json` entries; every subtitle key exists.
- Fonts: all 7 font JSONs are named in code or book themes; `OFL-Jacquard24.txt` is excluded and checked by the build.
- Particles: every registered particle type has its JSON.
- Textures loaded by naming convention and no literal: `textures/entity/{banner,shield}/hemomancy_{heart,veins}.png`
  (banner pattern `asset_id`), `textures/entity/{villager,zombie_villager}/profession/hemopothecary.png`, and
  `textures/mob_effect/<effect>.png` for registered effects.
- Generated resources: no file has an authored twin in `src/main/resources`; `.cache/` is not tracked.
- `hemomancy.configuration.boss_presentation` and `.microscope` are NeoForge config-section keys
  (`HemoClientConfig.java:60,66` `push(...)`). No `IConfigScreenFactory` is registered, so none of the 7 `configuration`
  keys is shown today.
- The 17 Unstained cardinal rites without `purpose` are outside the Harbinger-only rule (AGENTS.md; Unstained rites
  deferred).
- `hutoslib:dictation_table` in two recipes and a loot table resolves against the HutosLib 7.4.0 composite build
  (`docs/DEPENDENCY_PROVISIONING.md:14`); F-22 covers its future.
- One locale only, so there is no locale drift. Values that use `%%` without arguments render correctly.
- Non-ASCII text decodes cleanly: no `U+FFFD` and no invalid UTF-8 anywhere in data, assets or lang.
- `FieldNotesItem`'s five missing keys are already excused in `HardCodedEnglishGuardrailTest.UNREGISTERED`; the class
  is unregistered and belongs to M-3.

## Appendix

### A1. Lang keys with no reference anywhere (58; the two `hemomancy.configuration.<section>` keys are excluded, see Not a problem)

- `_comment_mna_spell_components`
- `block.minecraft.banner.hemomancy_heart.lightBlue`
- `block.minecraft.banner.hemomancy_heart.silver`
- `block.minecraft.banner.hemomancy_veins.lightBlue`
- `block.minecraft.banner.hemomancy_veins.silver`
- `dimension.hemomancy.chamber_of_will`
- `fluid.hemomancy.blood`
- `hemomancy.brazier.echo_consumed`
- `hemomancy.brazier.echo_max`
- `hemomancy.brazier.reagent_accepted`
- `hemomancy.brazier.sanguine_ready`
- `hemomancy.memory_of_vesper.already_awakened`
- `hemomancy.memory_of_vesper.inactive`
- `hemomancy.memory_of_vesper.no_bond`
- `hemomancy.mortal_display.already_invited`
- `hemomancy.screen.morphling_viewer`
- `hemomancy.succession.jei.form_life`
- `hemomancy.tutorial.sample_controls`
- `hemomancy.tutorial.starter_blood`
- `hemomancy.unstained.already_purified`
- `hemomancy.unstained.clarity_unlocked`
- `hemomancy.unstained.not_ready`
- `hemomancy.unstained.purification_begun`
- `hemomancy.unstained.purity_complete`
- `hemomancy.unstained.purity_progress`
- `hemomancy:living_armor_set_bonus`
- `mechanic.mna:hemomancy`
- `memo.hemomancy.antecedent_acoustic_response`
- `memo.hemomancy.antecedent_archive_response`
- `memo.hemomancy.antecedent_controlled_replay`
- `memo.hemomancy.antecedent_crossed_wool_terminus`
- `memo.hemomancy.antecedent_degraded_network`
- `memo.hemomancy.antecedent_epilogue`
- `memo.hemomancy.antecedent_replay_requested`
- `memo.hemomancy.antecedent_sample_analyzed`
- `memo.hemomancy.antecedent_sample_response`
- `memo.hemomancy.antecedent_severed_record_heard`
- `memo.hemomancy.antecedent_vicar_recognition`
- `memo.hemomancy.antecedent_vigil_record_read`
- `message.hemomancy.memo.field_notes_full`
- `message.hemomancy.memo.liber_placed`
- `message.hemomancy.memo.liber_removed`
- `message.hemomancy.memo.mixed_notes`
- `message.hemomancy.memo.no_field_notes`
- `message.hemomancy.memo.no_ink_path`
- `message.hemomancy.memo.wrong_ink_path`
- `message.hemomancy.saint_sarcophagus.fail`
- `message.hemomancy.saint_sarcophagus.success`
- `message.hemomancy.synaptic_step.blocked`
- `message.hemomancy.synaptic_step.target`
- `screen.hemomancy.bloodline_pool.bloodline_message_label`
- `screen.hemomancy.manip_tree.manipulations`
- `screen.hemomancy.manip_tree.skills`
- `screen.hemomancy.puppeteers_spindle.buffer`
- `screen.hemomancy.puppeteers_spindle.crossbar_thread`
- `screen.hemomancy.puppeteers_spindle.thread`
- `screen.hemomancy.virtual_field_notes.status`
- `screen.hemomancy.virtual_field_notes.total`

### A2. Lang keys referenced only from tools, a test, a comment or the excluded compat packages (17)

- `ability.hemomancy.masquerade_of_the_forgotten` (mention_only)
- `hemomancy.artificer.item_inquiry.unknown` (test)
- `hemomancy.cicatrix_anchorite.item_inquiry.unknown` (tools)
- `hemomancy.monolith.item_hint` (tools)
- `hemomancy.occulus.task.adept` (excluded)
- `hemomancy.occulus.task.archon` (excluded)
- `hemomancy.occulus.task.illuminatus` (excluded)
- `hemomancy.occulus.task.initiate` (excluded)
- `hemomancy.occulus.task.neophyte` (excluded)
- `hemomancy.occulus.task.sanctified` (excluded)
- `hemomancy.occulus.task.votary` (excluded)
- `hemomancy.whisper.archon.v2.truth1` (tools)
- `hemomancy.whisper.busy` (tools)
- `hemomancy.whisper.spine_growth.degree8` (tools)
- `hemomancy.zealot.item_inquiry.pallid_infusion.not_yet` (tools)
- `summon.hemomancy.cinder_bellows` (tools)
- `summon.hemomancy.marrow_spitter` (tools)

### A3. Potion names with four lang keys each and no registered potion (41 names, 164 keys)

Keys are `item.minecraft.{potion,splash_potion,lingering_potion,tipped_arrow}.effect.<name>`.
- `potion_of_arcane_resonance`
- `potion_of_blood_drunkenness`
- `potion_of_chummed_waters`
- `potion_of_conductive_mark`
- `potion_of_constricted`
- `potion_of_counterfeit_scar`
- `potion_of_cryoprotection`
- `potion_of_disrupted`
- `potion_of_earthen_mining`
- `potion_of_ender_resistance`
- `potion_of_explosion_resistance`
- `potion_of_ferric_transfusion`
- `potion_of_fungal_elytra`
- `potion_of_grave_debt`
- `potion_of_hematic_strain`
- `potion_of_hemophagy`
- `potion_of_illuminated`
- `potion_of_impaired_recovery`
- `potion_of_insatiable_hunger`
- `potion_of_iron_retort`
- `potion_of_lodestone`
- `potion_of_marked_by_canon`
- `potion_of_mnemonic_candle_aura`
- `potion_of_mnemonic_screams`
- `potion_of_morphic_strain`
- `potion_of_necrosis`
- `potion_of_neural_overload`
- `potion_of_obscured`
- `potion_of_paralysis`
- `potion_of_poison_resistance`
- `potion_of_rime`
- `potion_of_sanguine_clarity`
- `potion_of_sanguine_pressure`
- `potion_of_searing`
- `potion_of_silver_ward`
- `potion_of_sporitic_resonance`
- `potion_of_transfusion_saturation`
- `potion_of_veiled`
- `potion_of_verdigris_aura`
- `potion_of_web_mobility`
- `potion_of_wither_resistance`

### A4. Item-inquiry line keys with no inquiry JSON and no Java builder (24)

- `hemomancy.mnemonist.item_inquiry.crude_memory_blood_lamp.line1`
- `hemomancy.mnemonist.item_inquiry.crude_memory_blood_lamp.line2`
- `hemomancy.mnemonist.item_inquiry.crude_memory_crimson_harvest.line1`
- `hemomancy.mnemonist.item_inquiry.crude_memory_crimson_harvest.line2`
- `hemomancy.mnemonist.item_inquiry.memory_blackhearted.line1`
- `hemomancy.mnemonist.item_inquiry.memory_blackhearted.line2`
- `hemomancy.mnemonist.item_inquiry.memory_blood_absorption.line1`
- `hemomancy.mnemonist.item_inquiry.memory_blood_absorption.line2`
- `hemomancy.mnemonist.item_inquiry.memory_blood_eclipse_mantle.line1`
- `hemomancy.mnemonist.item_inquiry.memory_blood_eclipse_mantle.line2`
- `hemomancy.mnemonist.item_inquiry.memory_blood_projection.line1`
- `hemomancy.mnemonist.item_inquiry.memory_blood_projection.line2`
- `hemomancy.mnemonist.item_inquiry.memory_ferric_rampart.line1`
- `hemomancy.mnemonist.item_inquiry.memory_ferric_rampart.line2`
- `hemomancy.mnemonist.item_inquiry.memory_ferric_spikes.line1`
- `hemomancy.mnemonist.item_inquiry.memory_ferric_spikes.line2`
- `hemomancy.mnemonist.item_inquiry.memory_hematic_impressment.line1`
- `hemomancy.mnemonist.item_inquiry.memory_hematic_impressment.line2`
- `hemomancy.mnemonist.item_inquiry.memory_hematic_rebuke.line1`
- `hemomancy.mnemonist.item_inquiry.memory_hematic_rebuke.line2`
- `hemomancy.mnemonist.item_inquiry.memory_ironhearted.line1`
- `hemomancy.mnemonist.item_inquiry.memory_ironhearted.line2`
- `hemomancy.vicar.item_inquiry.scar_binder.line1`
- `hemomancy.vicar.item_inquiry.scar_binder.line2`

### A5. Name keys for ids that are not registered (48 Verified, 5 Suspected)

Items (`item.hemomancy.<id>`, id not an item or block):
- `item.hemomancy.abyssal_ichor`
- `item.hemomancy.ahaematic_unidentified`
- `item.hemomancy.befouled_vinteum_dust`
- `item.hemomancy.blood_shot_occulus`
- `item.hemomancy.blood_tendency_gauge`
- `item.hemomancy.bloodline_pool_monitor`
- `item.hemomancy.desiccated_membrane`
- `item.hemomancy.dicentra_sap`
- `item.hemomancy.faction_horn_harbingers`
- `item.hemomancy.field_notes`
- `item.hemomancy.foul_vinteum_ingot`
- `item.hemomancy.harbinger_grimore`
- `item.hemomancy.hematic_field_ink`
- `item.hemomancy.living_infused_thread`
- `item.hemomancy.living_thread_boots`
- `item.hemomancy.living_thread_hood`
- `item.hemomancy.living_thread_leggings`
- `item.hemomancy.living_thread_robes`
- `item.hemomancy.mana_infushed_memory_blank`
- `item.hemomancy.mana_memory_sanguine_transfusion`
- `item.hemomancy.mark_of_blood`
- `item.hemomancy.molten_scab`
- `item.hemomancy.mote_blood`
- `item.hemomancy.pale_field_ink`
- `item.hemomancy.roasted_gourd_seeds`
- `item.hemomancy.saint_enzyme`
- `item.hemomancy.scar_binder_upgraded`
- `item.hemomancy.spawn_egg_abyssal_siphon`
- `item.hemomancy.spawn_egg_cruor_fiend`
- `item.hemomancy.spawn_egg_frozen_clot`
- `item.hemomancy.spawn_egg_synapse_hound`
- `item.hemomancy.spawn_egg_void_drinker`
- `item.hemomancy.verdigris_censer`
- `item.hemomancy.void_ichor`

Entities (`entity.hemomancy.<id>`):
- `entity.hemomancy.abyssal_siphon`
- `entity.hemomancy.cruor_fiend`
- `entity.hemomancy.frozen_clot`
- `entity.hemomancy.harbinger_saint`
- `entity.hemomancy.synapse_hound`
- `entity.hemomancy.void_drinker`

Blocks (`block.hemomancy.<id>`):
- `block.hemomancy.broken_mana_trapazahedron`
- `block.hemomancy.eightfold_scriptorium`
- `block.hemomancy.monolithic_scriptorium`
- `block.hemomancy.serpent_scale_block`

`item.hemomancy.<block id>` (block items read `block.hemomancy.<id>`):
- `item.hemomancy.crucible_of_nether_ichor`
- `item.hemomancy.puppeteers_spindle`
- `item.hemomancy.resonant_forge`
- `item.hemomancy.voidtouched_vessel`

Suspected (the id appears in Java as a string, possibly built dynamically):
- `item.hemomancy.ferric_poise`
- `item.hemomancy.fervent_reprisal`
- `item.hemomancy.frozen_clot`
- `item.hemomancy.nerve_surge`
- `item.hemomancy.scar_binder`

### A6. Suspected orphans: matched only by a broad Java pattern whose dynamic part has no evidence, or only by a test pattern (104)

Spot checks moved 85 keys of this tier back to live (rapport `.teaser` via `DialogueHubFactory.TEASER_SUFFIX`, `blood_property.*` via property ids, `hemomancy.antecedent.dialogue.initial_*` via `"initial_" + speaker`, `living_weapon_form.*.graft` via `langKey() + ".graft"`). The rest need a per-family read before deletion.

- `screen.hemomancy.recipe_map_family.*` (15): `apparatus`, `body_will`, `communion_floors`, `constructs_effigies`, `domain_world`, `dominion_floors`, `floorless_crafts`, `ichorian_sigils`, `miscellaneous`, `puppetry`, `ritual_floors`, `ritual_infrastructure` ...
- `screen.hemomancy.harbinger_assignment_ledger.*` (12): `crimson_vestment.upgrade`, `step.craft_hematic_iron`, `step.craft_hematic_iron.desc`, `step.craft_liber`, `step.craft_liber.desc`, `step.initiation`, `step.initiation.desc`, `step.invitation`, `step.invitation.desc`, `the_three_answers.upgrade`, `the_worn_vow.upgrade`, `weight_of_the_frame.upgrade`
- `hemomancy.dialogue.artificer.*` (9): `option.claim_assumed_limb_reward`, `option.claim_blood_lust_fitting`, `option.claim_crimson_vestment_reward`, `option.claim_d7_fitting`, `option.claim_fork_fitting`, `option.claim_hematic_iron_fitting`, `option.claim_living_arsenal_fitting`, `option.claim_three_answers_reward`, `option.claim_worn_vow_reward`
- `hemomancy.dialogue.event.*` (9): `accept_church`, `accept_purification`, `alchemist_red_taxonomy_placeholder_reward`, `artificer_consecration_kit_granted`, `artificer_consecration_kit_known`, `artificer_consecration_kit_unready`, `monolith_cornerstone_granted`, `monolith_cornerstone_unready`, `vicar_hermit_road_ledger`
- `screen.hemomancy.unstained_progress_layout.*` (8): `rite.clarity_ascension.line1`, `rite.clarity_ascension.line2`, `rite.closed_vein.line1`, `rite.closed_vein.line2`, `rite.consecrated_copper.line1`, `rite.consecrated_copper.line2`, `rite.lethean_baptism.line1`, `rite.lethean_baptism.line2`
- `block.hemomancy.hematic_armature.*` (7): `cornerstone_requires_vicar`, `tier.base`, `tier.monolithic`, `tier.vicar_consecrated`, `upgrade_already_applied`, `upgrade_requires_degree`, `upgrade_tier_applied`
- `hemomancy.circus.school.*` (7): `exercise.cinder_bellows`, `exercise.gorebound_hulk`, `exercise.marrow_spitter`, `exercise.mnemonist_puppet`, `exercise.sanguine_hound`, `exercise.scarlet_mummer`, `exercise.veinwing_vulture`
- `block.hemomancy.consecrated_bloodwell.*` (6): `cannot_deposit`, `deposited`, `duplicate_heart`, `empty_or_full`, `not_in_fane`, `withdrew`
- `hemomancy.alchemist.phlegethontic_commission.*` (4): `direction.northeast`, `direction.northwest`, `direction.southeast`, `direction.southwest`
- `hemomancy.alchemist.initiate.*` (2): `pallid_vessel.fill`, `pallid_vessel.withdraw`
- `hemomancy.alchemist.neophyte.*` (2): `alembic_leak`, `alembic_lore`
- `hemomancy.dialogue.unstained.*` (2): `observances.line1`, `option.observances`
- `hemomancy.milestone.altar_blessing.*` (2): ``, `desc`
- `item.hemomancy.harbinger_assignment_ledger.*` (2): `hermit_road.first_found`, `hermit_road.seek`
- `block.hemomancy.hematic_stake.*` (1): `manifested`
- `gui.hemomancy.clairaudiograph.*` (1): `missing_audio`
- `hemomancy.alchemist.votary.*` (1): `tendency_lore`
- `hemomancy.dialogue.topic.*` (1): `held_item`
- `hemomancy.dialogue.zealot.*` (1): `option.accept_church`
- `hemomancy.whisper.archon.*` (1): `v1.line2`
- `hemomancy.whisper.sanctified.*` (1): `v2.line2`
- `item.hemomancy.bloody_vial.*` (1): `trait`
- `item.hemomancy.chitinite_arm_banner.*` (1): `lightBlue`
- `item.hemomancy.liber_immaculatus.*` (1): `tooltip.memos`
- `item.hemomancy.liber_sanguinum.*` (1): `tooltip.memos`
- `item.hemomancy.mnemonic_ambergris.*` (1): `tooltip.harvest`
- `key.hemomancy.morphjarpickup.*` (1): `desc`
- `key.hemomancy.openjar.*` (1): `desc`
- `key.hemomancy.scarbinderpickup.*` (1): `desc`
- `message.hemomancy.synaptic_step.*` (1): `source`
- `screen.hemomancy.virtual_field_notes.*` (1): `title`

### A7. Duplicate English values (top 40 of 393 groups covering 932 keys)

- 15 x "%s/%s": `screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.progress`, `screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.progress`, `screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.progress`, `screen.hemomancy.harbinger_assignment_ledger.first_distillation.progress` ...
- 8 x "%s mL": `hemomancy.jei.stat.blood`, `screen.hemomancy.blood_structure_hint.ml`, `screen.hemomancy.rite_hint.ml`, `screen.hemomancy.crafting_tab_view.ml` ...
- 7 x "Tendency: %s": `item.hemomancy.bloody_vial.tendency`, `tooltip.hemomancy.enzyme.tendency_s`, `tooltip.hemomancy.item_immature_fungal_scar.tendency_s`, `screen.hemomancy.scars_tab_view.tendency` ...
- 7 x "Prepare the offering and complete this shape's Cardinal Ordeal. You ke": `hemomancy.circus.school.ledger.veinwing_vulture.ordeal`, `hemomancy.circus.school.ledger.marrow_spitter.ordeal`, `hemomancy.circus.school.ledger.scarlet_mummer.ordeal`, `hemomancy.circus.school.ledger.gorebound_hulk.ordeal` ...
- 7 x "Practical passed. This professor can host a daily paid guest act.": `hemomancy.circus.school.ledger.veinwing_vulture.mastered`, `hemomancy.circus.school.ledger.marrow_spitter.mastered`, `hemomancy.circus.school.ledger.scarlet_mummer.mastered`, `hemomancy.circus.school.ledger.gorebound_hulk.mastered` ...
- 6 x "%ss": `hemomancy.jei.seconds`, `screen.hemomancy.still_art_cooldown_overlay.s`, `screen.hemomancy.rite_hint.seconds`, `screen.hemomancy.rites_tab_view.seconds` ...
- 5 x "Report to the Alchemist": `screen.hemomancy.harbinger_assignment_ledger.step.pallid_vessel_report`, `screen.hemomancy.harbinger_assignment_ledger.step.vagrant_biology_report`, `screen.hemomancy.harbinger_assignment_ledger.step.phlegethontic_report`, `screen.hemomancy.harbinger_assignment_ledger.step.deep_dark_report` ...
- 5 x "State: Unleashed": `tooltip.hemomancy.living_axe.state_unleashed`, `tooltip.hemomancy.living_blade.state_unleashed`, `tooltip.hemomancy.living_spear.state_unleashed`, `tooltip.hemomancy.sanguis_lancea.state_unleashed` ...
- 5 x "State: Tame": `tooltip.hemomancy.living_axe.state_tame`, `tooltip.hemomancy.living_blade.state_tame`, `tooltip.hemomancy.living_spear.state_tame`, `tooltip.hemomancy.sanguis_lancea.state_tame` ...
- 4 x "Saint Hemorath": `boss.hemomancy.hemorath.name`, `boss.hemomancy.hemorath_empties.name`, `entity.hemomancy.hemorath`, `entity.hemomancy.hollow_vessel`
- 4 x "The First Separation": `advancements.hemomancy.first_separation_started.title`, `screen.hemomancy.harbinger_assignment_ledger.first_separation.title`, `milestone.hemomancy.chapter.first_separation`, `milestone.hemomancy.chapter.first_distillation`
- 4 x "Unknown": `bestiary.hemomancy.status.unknown`, `screen.hemomancy.manipulations_tab_controller.unknown`, `screen.hemomancy.material_atlas.bucket.unknown`, `message.hemomancy.recipe_degree_gates.stage_unknown`
- 4 x "Your blood is not yet awakened.": `block.hemomancy.consecrated_bloodwell.inactive`, `block.hemomancy.covenant_throne.no_blood`, `block.hemomancy.sanguine_vigil.inactive`, `item.hemomancy.hematic_suture_needle.inactive`
- 4 x "Morphic Nectar": `block.hemomancy.morphic_nectar_block`, `fluid_type.hemomancy.morphic_nectar`, `fluid.hemomancy.morphic_nectar`, `hemomancy.jei.morphic_nectar`
- 4 x "I have a question about something I found...": `hemomancy.dialogue.alchemist.option.ask_about_item`, `hemomancy.dialogue.guardian.option.ask_about_item`, `hemomancy.dialogue.option.ask_about_item`, `hemomancy.dialogue.zealot.option.ask_about_item`
- 4 x "[Leave]": `hemomancy.dialogue.alchemist.option.leave`, `hemomancy.dialogue.hermit.option.leave`, `hemomancy.dialogue.vicar.option.leave`, `hemomancy.dialogue.zealot.option.leave`
- 4 x "Requires Degree %s": `hemomancy.jei.craft.degree_full`, `screen.hemomancy.manipulations_tab_controller.requires_degree`, `screen.hemomancy.material_gate.requires_degree`, `screen.hemomancy.recipe_map_tooltip.requires_degree`
- 4 x "Degree %s": `hemomancy.jei.stat.degree`, `screen.hemomancy.summons_tab_view.degree`, `screen.hemomancy.manipulations_tab_controller.degree_number`, `message.hemomancy.recipe_degree_gates.degree`
- 4 x "That pattern exceeds your current scar capacity.": `message.hemomancy.mason_effigy.too_many`, `message.hemomancy.packet_prepare_scar_pattern.that_pattern_exceeds_your_current_scar`, `message.hemomancy.masons_effigy_block.that_pattern_exceeds_your_current_scar`, `message.hemomancy.scar_brazier_rite.that_pattern_exceeds_your_current_scar`
- 4 x "Dormant": `hemomancy.succession.status.dormant`, `message.hemomancy.blood_flow.dormant`, `screen.hemomancy.scrying_diagnostics.dormant`, `saint_corpus_state.hemomancy.dormant`
- 4 x "Reset %s%s": `commands.hemomancy.organs.reset`, `commands.hemomancy.qliphoth.reset`, `commands.hemomancy.tendency.reset`, `commands.hemomancy.unstained.reset`
- 4 x "Result:": `screen.hemomancy.blood_structure_hint.result`, `screen.hemomancy.rite_hint.result`, `screen.hemomancy.crafting_tab_view.result`, `screen.hemomancy.rites_tab_view.result`
- 4 x "Materials:": `screen.hemomancy.blood_structure_hint.materials`, `screen.hemomancy.rite_hint.materials`, `screen.hemomancy.crafting_tab_view.materials`, `screen.hemomancy.rites_tab_view.materials`
- 4 x "That manipulation has gone dormant.": `message.hemomancy.blood_manipulation.that_manipulation_has_gone_dormant`, `message.hemomancy.equip_manipulation_packet.that_manipulation_has_gone_dormant`, `message.hemomancy.update_current_manip_packet.that_manipulation_has_gone_dormant`, `message.hemomancy.use_manip_key_packet.that_manipulation_has_gone_dormant`
- 4 x "Qliphoth": `screen.hemomancy.material_atlas.bucket.qliphoth_reagents.short`, `screen.hemomancy.recipe_map_family.qliphoth`, `screen.hemomancy.family_filter_labels.qliphoth`, `cardinal_rite_floor.hemomancy.style.qliphoth`
- 4 x "Bloodline": `screen.hemomancy.scrying_diagnostics.category.bloodline`, `screen.hemomancy.bloodline_pool.section.bloodline`, `screen.hemomancy.recipe_map_family.bloodline`, `screen.hemomancy.family_filter_labels.bloodline`
- 3 x "Tendencies": `screen.hemomancy.scriptorium.tendencies`, `gui.hemomancy.microscope.tendencies`, `screen.hemomancy.harbinger_progress.tab.tendencies`
- 3 x "The Mycophant": `boss.hemomancy.mycophant.name`, `boss.hemomancy.mycophant_split.name`, `entity.hemomancy.mycophant`
- 3 x "Annetta Knowles": `boss.hemomancy.annetta_knowles.name`, `boss.hemomancy.annetta_break.name`, `entity.hemomancy.annetta_knowles`
- 3 x "Naeglerophaeon": `boss.hemomancy.naeglerophaeon.name`, `boss.hemomancy.naeglerophaeon_transition.name`, `entity.hemomancy.naeglerophaeon`
- 3 x "Vesper": `boss.hemomancy.vesper_crowned_refusal.name`, `boss.hemomancy.vesper_cocoon.name`, `boss.hemomancy.vesper_evening_star.name`
- 3 x "Cleansing": `advancements.hemomancy.cleansing.title`, `hemomancy.purity.cleansing`, `screen.hemomancy.material_atlas.bucket.cleansing_facilities.short`
- 3 x "The Eightfold Centrifuge": `advancements.hemomancy.enzyme_mastery_complete.title`, `screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.side_title`, `screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.title`
- 3 x "Liber Sanguinium": `advancements.hemomancy.liber_sanguinium.title`, `hemomancy.milestone.liber_sanguinium`, `item.hemomancy.liber_sanguinum`
- 3 x "The Living Covenant": `advancements.hemomancy.living_covenant_complete.title`, `screen.hemomancy.harbinger_assignment_ledger.living_covenant.title`, `milestone.hemomancy.chapter.living_covenant`
- 3 x "The Woven Vessel": `advancements.hemomancy.mnemonist_woven_vessel_complete.title`, `screen.hemomancy.harbinger_assignment_ledger.woven_vessel.title`, `milestone.hemomancy.chapter.woven_vessel`
- 3 x "Our Lady of Still Waters": `advancements.hemomancy.our_lady_of_still_waters.title`, `hemomancy.lady.speaker_name`, `hemomancy.milestone.our_lady_of_still_waters`
- 3 x "The Red Taxonomy": `advancements.hemomancy.red_taxonomy_complete.title`, `screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.side_title`, `screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.title`
- 3 x "Unstained": `advancements.hemomancy.unstained.title`, `screen.hemomancy.material_atlas.unstained`, `screen.hemomancy.structure_spawner.tab_unstained`
- 3 x "Return to the Vein-Mason": `advancements.hemomancy.vein_mason_continuation_ready.title`, `advancements.hemomancy.vein_mason_d5_ready.title`, `advancements.hemomancy.vein_mason_d6_ready.title`

### A8. Values with leading or trailing whitespace (92)

- `block.hemomancy.active_befouling_ash_trail`
- `block.hemomancy.active_smouldering_ash_trail`
- `hud.hemomancy.mnemonic_blueprint.more_types`
- `tooltip.hemomancy.item_scar_pattern.s`
- `tooltip.hemomancy.scar_definition.s`
- `commands.hemomancy.bloodline.player_npc_online_reset`
- `commands.hemomancy.blood.blood`
- `commands.hemomancy.blood.active`
- `commands.hemomancy.degree.degree`
- `commands.hemomancy.manipulations.known_manipulations`
- `commands.hemomancy.morphling.and_updated_its_jar_item`
- `commands.hemomancy.organs.organs`
- `commands.hemomancy.organs.t_lv_3`
- `commands.hemomancy.organs.to_level`
- `commands.hemomancy.organs.all_organs_to_level_0`
- `commands.hemomancy.qliphoth.qliphoth_pome_progress_and_communion_gate`
- `commands.hemomancy.qliphoth.degree`
- `commands.hemomancy.qliphoth.qliphoth_pome_progress_to`
- `commands.hemomancy.qliphoth.at`
- `commands.hemomancy.skills.manip_uses_kills_rites_advancements`
- `commands.hemomancy.slots.slots`
- `commands.hemomancy.summons.known_puppeteer_summons`
- `commands.hemomancy.tendency.blood_tendencies`
- `commands.hemomancy.tendency.total`
- `commands.hemomancy.tendency.blood_tendencies_to_0`
- `commands.hemomancy.tendency.blood_tendencies_to_100`
- `commands.hemomancy.unstained.unstained_progress`
- `commands.hemomancy.unstained.phase`
- `commands.hemomancy.unstained.novitiate_vows`
- `commands.hemomancy.unstained.begun`
- `commands.hemomancy.unstained.purity`
- `commands.hemomancy.unstained.clarity_unlocked`
- `commands.hemomancy.unstained.clarity`
- `commands.hemomancy.unstained.silver_ward`
- `commands.hemomancy.unstained.verdigris_aura`
- `commands.hemomancy.unstained.purity_2`
- `commands.hemomancy.unstained.clarity_2`
- `commands.hemomancy.unstained.unstained_progress_purified_enlightened`
- `commands.hemomancy.unstained.unstained_progress_to_zero`
- `screen.hemomancy.vascular_status.s`
- `screen.hemomancy.radial_choose_manip.self_ml`
- `screen.hemomancy.radial_choose_manip.s_cooldown`
- `screen.hemomancy.radial_choose_manip.selected`
- `message.hemomancy.structure_scanner_item.blood_structure_hemomancy_scans`
- `message.hemomancy.structure_scanner_item.cardinal_rite_hemomancy_scans`
- `message.hemomancy.packet_scar_crafting_event.rank_degree_is_required_to_carve`
- `message.hemomancy.cardinal_rite_activation.first`
- `message.hemomancy.cardinal_rite_activation.unstained_requirement`
- `message.hemomancy.working_craft_rites.condenses_on_the_focus`
- `screen.hemomancy.crafting_tab_view.feed`
- `screen.hemomancy.crafting_tab_view.blood_cost`
- `message.hemomancy.blood_structure_crafting.purity_stage`
- `gui.hemomancy.casting_preview_commands.style`
- `gui.hemomancy.casting_preview_commands.charge`
- `message.hemomancy.place_structure_packet.working_floor_hint`
- `screen.hemomancy.rite_hint.style`
- `screen.hemomancy.rite_hint.tier`
- `screen.hemomancy.rite_hint.dimensions`
- `screen.hemomancy.rite_hint.braziers_count_anywhere_in`
- `tooltip.hemomancy.morphling.maturity_bonus`
- `screen.hemomancy.scrying_diagnostics.value_label`
- `screen.hemomancy.radial_choose_manip.staff_planted_in_active_rite`
- `screen.hemomancy.cardinal_rite_overlay.ichor`
- `screen.hemomancy.cardinal_rite_overlay.allies`
- `screen.hemomancy.cardinal_rite_overlay.pool`
- `screen.hemomancy.rites_tab_view.medium`
- `screen.hemomancy.rites_tab_view.preserved_medium`
- `screen.hemomancy.rite_hint.shove_the_living_staff`
- `screen.hemomancy.rite_hint.right_click_with_a_sanguine_formation`
- `screen.hemomancy.rite_hint.use_the_declared_focus`
- `screen.hemomancy.rite_hint.build_the_structure_shown`
- `screen.hemomancy.rite_hint.working_floor_instructions`
- `screen.hemomancy.rite_hint.place_the_exact_offerings`
- `screen.hemomancy.rite_hint.no_upper_structure`
- `screen.hemomancy.rite_hint.build_the_shown_upper_structure`
- `screen.hemomancy.rite_hint.build_the_shown_reusable_upper_structure`
- `screen.hemomancy.rite_hint.build_at_least_the_floor`
- `screen.hemomancy.rite_hint.feed_its_anchors`
- `screen.hemomancy.rite_hint.support_sigils_are_optional`
- `screen.hemomancy.rite_hint.awaken_the_required_support_sigil`
- `screen.hemomancy.rite_hint.awaken_all_required_support_sigils`
- `screen.hemomancy.scar_station.pattern_count`
- `screen.hemomancy.scar_binder_viewer.pattern_count`
- `screen.hemomancy.scar_binder_viewer.pattern_count_plural`
- `message.hemomancy.qliphoth_pome.husk_suffix`
- `screen.hemomancy.structure_spawner.prefix_craft`
- `screen.hemomancy.structure_spawner.prefix_structure`
- `screen.hemomancy.structure_spawner.prefix_rite`
- `screen.hemomancy.structure_spawner.prefix_floor`
- `commands.hemomancy.morphling.target_label`
- `screen.hemomancy.vascular_view.scar_strain_reduction`
- `commands.hemomancy.unstained.stage_suffix`

### A9. Values with a raw section sign (309 keys), by family

- `hemomancy.lady.*`: 61
- `hemomancy.whisper.*`: 61
- `message.hemomancy.*`: 58
- `hemomancy.communion.*`: 27
- `hemomancy.dialogue.*`: 15
- `hemomancy.vicar.*`: 14
- `hemomancy.hermit.*`: 13
- `hemomancy.alchemist.*`: 10
- `screen.hemomancy.*`: 10
- `hemomancy.altar.*`: 9
- `hemomancy.scout.*`: 8
- `tooltip.hemomancy.*`: 8
- `hemomancy.monolith.*`: 5
- `hemomancy.acolyte.*`: 2
- `hemomancy.mnemonist.*`: 2
- `hemomancy.blood_moon.*`: 1
- `hemomancy.guardian.*`: 1
- `hemomancy.lethean_tide.*`: 1
- `hemomancy.manipulation.*`: 1
- `hemomancy.silver_bells.*`: 1
- `hemomancy.zealot.*`: 1

### A10. Unreferenced textures outside `old/` (137)

- `assets/minecraft/textures/entity/banner/hemomancy_heart.png` (copy of the hemomancy-namespace file)
- `assets/minecraft/textures/entity/banner/hemomancy_veins.png` (copy of the hemomancy-namespace file)
- `assets/minecraft/textures/entity/shield/hemomancy_heart.png` (copy of the hemomancy-namespace file)
- `assets/minecraft/textures/entity/shield/hemomancy_veins.png` (copy of the hemomancy-namespace file)

- `textures/entity/abhorrent_thought/model_nightmare_thought.png`
- `textures/entity/blood_bullet/model_blood_bullet.png`
- `textures/entity/blood_shot/model_blood_shot.png`
- `textures/entity/chitinite/model_chitinite_warn.png`
- `textures/entity/chitinite/model_shiny_chitinite.png`
- `textures/entity/chitinite_shield/model_chitinite.png`
- `textures/entity/choir_keeper.png`
- `textures/entity/choir_keeper/choir_keeper.png`
- `textures/entity/crimson_serpent/model_crimson_serpent.png`
- `textures/entity/drudge/model_drudge_blue.png`
- `textures/entity/drudge/model_drudge_brown.png`
- `textures/entity/drudge/model_drudge_green.png`
- `textures/entity/drudge/model_drudge_grey_old (1).png`
- `textures/entity/drudge/model_drudge_grey_old (2).png`
- `textures/entity/drudge/model_drudge_grey_old (3).png`
- `textures/entity/drudge/model_drudge_grey_old (4).png`
- `textures/entity/drudge/model_drudge_grey_old (5).png`
- `textures/entity/drudge/model_drudge_grey_old (6).png`
- `textures/entity/drudge/model_drudge_purple.png`
- `textures/entity/drudge/model_drudge_yellow.png`
- `textures/entity/floating_eye/model_floating_eye.png`
- `textures/entity/glow_page.png`
- `textures/entity/liber_inclinatio.png`
- `textures/entity/liber_inclinatio_hidden.png`
- `textures/entity/model_cleansing_altar.png`
- `textures/entity/model_dictation_table.png`
- `textures/entity/model_ghastly_alembic.png`
- `textures/entity/model_living_blade_hip.png`
- `textures/entity/model_pallid_retort.png`
- `textures/entity/model_shiny_chitinite.png`
- `textures/entity/model_somatic_loom.png`
- `textures/entity/naeglerophaeon.png`
- `textures/entity/royal_guard_shield_base.png`
- `textures/environment/chamber_of_will_clouds_decent.png`
- `textures/environment/clouds.png`
- `textures/environment/end_sky.png`
- `textures/environment/moon_phases - Copy.png`
- `textures/environment/moon_phases.png`
- `textures/environment/rain.png`
- `textures/environment/snow.png`
- `textures/environment/sun.png`
- `textures/font/abocipher/a.png`
- `textures/font/abocipher/b.png`
- `textures/font/abocipher/c.png`
- `textures/font/abocipher/d.png`
- `textures/font/abocipher/e.png`
- `textures/font/abocipher/f.png`
- `textures/font/abocipher/g.png`
- `textures/font/abocipher/green.png`
- `textures/font/abocipher/h.png`
- `textures/font/abocipher/i.png`
- `textures/font/abocipher/j.png`
- `textures/font/abocipher/k.png`
- `textures/font/abocipher/l.png`
- `textures/font/abocipher/m.png`
- `textures/font/abocipher/n.png`
- `textures/font/abocipher/o.png`
- `textures/font/abocipher/p.png`
- `textures/font/abocipher/q.png`
- `textures/font/abocipher/r.png`
- `textures/font/abocipher/s.png`
- `textures/font/abocipher/t.png`
- `textures/font/abocipher/tan.png`
- `textures/font/abocipher/u.png`
- `textures/font/abocipher/v.png`
- `textures/font/abocipher/w.png`
- `textures/font/abocipher/white.png`
- `textures/font/abocipher/x.png`
- `textures/font/abocipher/y.png`
- `textures/font/abocipher/z.png`
- `textures/gui/blank_inv.png`
- `textures/gui/choose_vein.png`
- `textures/gui/guide/book.png`
- `textures/gui/guide/tendencybook.png`
- `textures/gui/guide/tendencybook_hidden.png`
- `textures/gui/guide/unstained_overlay.png`
- `textures/gui/harbingers_resource_bars.png`
- `textures/gui/inventory.png`
- `textures/gui/memory_border.png`
- `textures/gui/morphling_jar_gui.png`
- `textures/gui/pallid_retort_gui.png`
- `textures/gui/pallid_retort_gui_overlay.png`
- `textures/gui/primal_morphic_overlay.png`
- `textures/gui/rune_binder_gui.png`
- `textures/gui/rune_binder_upgrade_old.png`
- `textures/gui/rune_pattern.png`
- `textures/gui/unstained_clarity_fill_tiled.png`
- `textures/gui/unstained_fill_tiled.png`
- `textures/gui/unstained_gauge.png`
- `textures/gui/vein_selection.png`
- `textures/gui/vein_teleport.png`
- `textures/gui/virtual_field_notes_book.png`
- `textures/gui/virtual_field_notes_button.png`
- `textures/misc/laser.png`
- `textures/misc/laser2.png`
- `textures/misc/laser_glow.png`
- `textures/misc/vine.png`
- `textures/misc/wisp_large.png`
- `textures/misc/wisp_small.png`
- `textures/mna/blank.png`
- `textures/mna/blood_binding.png`
- `textures/mna/blood_loss.png`
- `textures/mna/blood_rush.png`
- `textures/mna/bloody_border.png`
- `textures/mna/broken_mana_trapazahedron.png`
- `textures/mna/dodo.png`
- `textures/mna/faction_icon_harbinger.png`
- `textures/mna/harbingers_resource_bars.png`
- `textures/mna/hemolysis.png`
- `textures/mna/mana_to_blood.png`
- `textures/mna/resource_bars.png`
- `textures/mna/sanguine_fertility.png`
- `textures/mna/summon_sanguilith.png`
- `textures/mob_effect/mana_to_blood.png`
- `textures/mob_effect/morphling_centipede.png`
- `textures/models/morphling/attachment_debug.png`
- `textures/particle/abocipher/green.png`
- `textures/particle/abocipher/tan.png`
- `textures/particle/abocipher/white.png`
- `textures/particle/blood_cell_rotations.png`
- `textures/particle/blood_cell_rotations75.png`
- `textures/particle/blood_skull.png`
- `textures/particle/chain.png`
- `textures/particle/complex_lightning_bolt.png`
- `textures/particle/particle_glow.png`
- `textures/particle/particle_glow_new.png`
- `textures/particle/particle_kunai.png`
- `textures/particle/particle_metal.png`
- `textures/particle/particle_mote.png`
- `textures/particle/sparkle_0.png`
- `textures/particle/sparkle_1.png`
- `textures/particle/sparkle_2.png`
- `textures/particle/sparkle_3.png`

`old/` archives (excluded from the jar by `build.gradle`): `textures/item/old` 178, `textures/gui/old` 22, `textures/block/old` 20

### A11. OGG files no sound event names (57)

- `sounds/entity/beast_from_beyond/ambient.ogg`
- `sounds/entity/beast_from_beyond/death.ogg`
- `sounds/entity/beast_from_beyond/howl.ogg`
- `sounds/entity/beast_from_beyond/hurt.ogg`
- `sounds/entity/beast_from_beyond/music.ogg`
- `sounds/entity/beast_from_beyond/step.ogg`
- `sounds/entity/colin/OLDambient.ogg`
- `sounds/entity/colin/OLDdeath.ogg`
- `sounds/entity/colin/OLDhurt.ogg`
- `sounds/entity/colin/ambient.ogg`
- `sounds/entity/colin/death.ogg`
- `sounds/entity/colin/hurt.ogg`
- `sounds/entity/dark_young/ambient.ogg`
- `sounds/entity/dark_young/death.ogg`
- `sounds/entity/dark_young/hurt.ogg`
- `sounds/entity/dark_young/hurtother.ogg`
- `sounds/entity/dark_young/music.ogg`
- `sounds/entity/dark_young/summon.ogg`
- `sounds/entity/dark_young/teleport.ogg`
- `sounds/entity/denizen/ambient.ogg`
- `sounds/entity/denizen/death.ogg`
- `sounds/entity/denizen/hurt.ogg`
- `sounds/entity/dread_bot/ambient.ogg`
- `sounds/entity/dread_bot/death.ogg`
- `sounds/entity/dread_bot/hurt.ogg`
- `sounds/entity/hastur/ambient.ogg`
- `sounds/entity/hastur/death.ogg`
- `sounds/entity/hastur/hit.ogg`
- `sounds/entity/hastur/hurt.ogg`
- `sounds/entity/hastur/music.ogg`
- `sounds/entity/hastur/music1.ogg`
- `sounds/entity/hastur/music2.ogg`
- `sounds/entity/samples/choir_g5.ogg`
- `sounds/entity/samples/clothed_hit.ogg`
- `sounds/entity/samples/distorted_attack.ogg`
- `sounds/entity/samples/distorted_hurt1.ogg`
- `sounds/entity/samples/distorted_laugh.ogg`
- `sounds/entity/samples/distorted_talk.ogg`
- `sounds/entity/samples/ghost_breath.ogg`
- `sounds/entity/samples/grunt1.ogg`
- `sounds/entity/samples/hastur_music.ogg`
- `sounds/entity/samples/leaf_walk.ogg`
- `sounds/entity/samples/man_dies.ogg`
- `sounds/entity/seraphim/ambient.ogg`
- `sounds/entity/seraphim/death.ogg`
- `sounds/entity/seraphim/flare.ogg`
- `sounds/entity/seraphim/hurt.ogg`
- `sounds/entity/seraphim/music.ogg`
- `sounds/entity/seraphim/thronesummon.ogg`
- `sounds/entity/tentacle/ambient.ogg`
- `sounds/entity/tentacle/death.ogg`
- `sounds/entity/tentacle/hurt.ogg`
- `sounds/entity/tentacle/summon.ogg`
- `sounds/item/bow_blade/close.ogg`
- `sounds/item/bow_blade/open.ogg`
- `sounds/item/star_slug/storm.ogg`
- `sounds/item/star_slug/strike.ogg`

### A12. Structure NBTs reachable from nothing

- `src/main/resources/data/hemomancy/structure/antecedent/vigil/archive.nbt`
- `src/main/resources/data/hemomancy/structure/antecedent/vigil/entrance.nbt`
- `src/main/resources/data/hemomancy/structure/antecedent/vigil/gallery.nbt`
- `src/main/resources/data/hemomancy/structure/antecedent/vigil/passage.nbt`
- `src/main/resources/data/hemomancy/structure/broken_unstained_church.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/arch_fragment.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/dome_chapel.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/foundation_rubble.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/kit_showcase.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/page_slab.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/spire_fragment.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/tower_broken.nbt`
- `src/main/resources/data/hemomancy/structure/lowtide_ruin/tower_short.nbt`
- `src/main/resources/data/hemomancy/structure/qliphoth_fane.nbt`
- `src/main/resources/data/hemomancy/structure/tower/blood_tower_core.nbt`
- `src/main/resources/data/hemomancy/structure/tower/blood_tower_top_1.nbt`
- `src/main/resources/data/hemomancy/structure/harbinger_outpost.nbt`
- `src/main/resources/data/hemomancy/structure/qliphoth_bloom.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/antechamber2way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/antechamber4way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/sarcroom2way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/sarcroom4way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/stairs.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/top.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/treasureroom2way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/treasureroom4way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/trialroom2way.nbt`
- `src/main/resources/data/hemomancy/structure/mausoleum/trialroom4way.nbt`
- `src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt`

### A13. NBTs saved at an older DataVersion (upgraded through DataFixerUpper at every load)

- `src/main/resources/data/hemomancy/structure/desert_hemopothecary.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/harbinger_outpost.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/hermitage_remnant.nbt` (3953)
- `src/main/resources/data/hemomancy/structure/mausoleum.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/sarcroom2way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/sarcroom4way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/treasureroom2way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/treasureroom4way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/trialroom2way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/mausoleum/trialroom4way.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/plains_hemopothecary.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/qliphoth_bloom.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/qliphoth_fane.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/savanna_hemopothecary.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/snowy_hemopothecary.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/taiga_hemopothecary.nbt` (3465)
- `src/main/resources/data/hemomancy/structure/tower/blood_tower_core.nbt` (2975)
- `src/main/resources/data/hemomancy/structure/tower/blood_tower_top_1.nbt` (2975)

### A14. Item models for ids that are not registered items

- `src/main/resources/assets/hemomancy/models/item/arm_banner.json`
- `src/main/resources/assets/hemomancy/models/item/blood_shot_occulus.json`
- `src/main/resources/assets/hemomancy/models/item/broken_mana_trapazahedron.json`
- `src/main/resources/assets/hemomancy/models/item/earthen_vein_open.json`
- `src/main/resources/assets/hemomancy/models/item/earthly_transfuser.json`
- `src/main/resources/assets/hemomancy/models/item/faction_horn_harbingers.json`
- `src/main/resources/assets/hemomancy/models/item/field_notes.json`
- `src/main/resources/assets/hemomancy/models/item/foul_vinteum_ingot.json`
- `src/main/resources/assets/hemomancy/models/item/harbinger_grimore.json`
- `src/main/resources/assets/hemomancy/models/item/house_marker_giving_light.json`
- `src/main/resources/assets/hemomancy/models/item/living_grasp.json`
- `src/main/resources/assets/hemomancy/models/item/living_infused_thread.json`
- `src/main/resources/assets/hemomancy/models/item/living_syringe_stent.json`
- `src/main/resources/assets/hemomancy/models/item/living_thread_boots.json`
- `src/main/resources/assets/hemomancy/models/item/living_thread_hood.json`
- `src/main/resources/assets/hemomancy/models/item/living_thread_leggings.json`
- `src/main/resources/assets/hemomancy/models/item/living_thread_robes.json`
- `src/main/resources/assets/hemomancy/models/item/mana_infushed_memory_blank.json`
- `src/main/resources/assets/hemomancy/models/item/mana_memory_sanguine_transfusion.json`
- `src/main/resources/assets/hemomancy/models/item/mark_of_blood.json`
- `src/main/resources/assets/hemomancy/models/item/mote_blood.json`
- `src/main/resources/assets/hemomancy/models/item/runic_chisel_station.json`
- `src/main/resources/assets/hemomancy/models/item/scar_mod_station.json`
- `src/main/resources/assets/hemomancy/models/item/serpent_scale_block.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_abyssal_siphon.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_cruor_fiend.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_drudge.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_frozen_clot.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_synapse_hound.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_void_drinker.json`
- `src/main/resources/assets/hemomancy/models/item/spawn_egg_wretched_will.json`
- `src/main/resources/assets/hemomancy/models/item/spell_texture.json`
- `src/main/resources/assets/hemomancy/models/item/stabilized_sanguine_formation.json`
- `src/main/resources/assets/hemomancy/models/item/verdigris_censer.json`

### A15. JSON files that start with a UTF-8 byte-order mark (64)

- `src/main/resources/assets/hemomancy/models/world/lowtide_ruin/arch_fragment.json`
- `src/main/resources/assets/hemomancy/models/world/lowtide_ruin/dome_chapel.json`
- `src/main/resources/assets/hemomancy/models/world/lowtide_ruin/foundation_rubble.json`
- `src/main/resources/assets/hemomancy/models/world/lowtide_ruin/spire_fragment.json`
- `src/main/resources/assets/hemomancy/models/world/lowtide_ruin/tower_short.json`
- `src/main/resources/data/hemomancy/recipe/barbed_blade.json`
- `src/main/resources/data/hemomancy/recipe/barbed_shield.json`
- `src/main/resources/data/hemomancy/recipe/blood_wood_planks.json`
- `src/main/resources/data/hemomancy/recipe/chiseled_hematic_iron_block.json`
- `src/main/resources/data/hemomancy/recipe/chiseled_polished_venous_stone.json`
- `src/main/resources/data/hemomancy/recipe/chitinite_mace.json`
- `src/main/resources/data/hemomancy/recipe/chitinite_shield.json`
- `src/main/resources/data/hemomancy/recipe/cleansed_blood_crystal_shard.json`
- `src/main/resources/data/hemomancy/recipe/cleansed_stone.json`
- `src/main/resources/data/hemomancy/recipe/engram_stamp.json`
- `src/main/resources/data/hemomancy/recipe/foul_paste.json`
- `src/main/resources/data/hemomancy/recipe/foul_paste_from_fungi.json`
- `src/main/resources/data/hemomancy/recipe/gourd_seeds.json`
- `src/main/resources/data/hemomancy/recipe/gourd_slice.json`
- `src/main/resources/data/hemomancy/recipe/gourd_stew.json`
- `src/main/resources/data/hemomancy/recipe/hematic_iron_block.json`
- `src/main/resources/data/hemomancy/recipe/hematic_iron_pillar.json`
- `src/main/resources/data/hemomancy/recipe/hematic_iron_scrap.json`
- `src/main/resources/data/hemomancy/recipe/hematic_iron_sword.json`
- `src/main/resources/data/hemomancy/recipe/hematic_memory.json`
- `src/main/resources/data/hemomancy/recipe/hemolymphopoda_headpiece.json`
- `src/main/resources/data/hemomancy/recipe/hemolytic_plating.json`
- `src/main/resources/data/hemomancy/recipe/infested_venous_stone.json`
- `src/main/resources/data/hemomancy/recipe/lethean_poppy_wreath.json`
- `src/main/resources/data/hemomancy/recipe/morphling_jar.json`
- `src/main/resources/data/hemomancy/recipe/neutralizing_gasket.json`
- `src/main/resources/data/hemomancy/recipe/pale_distillate.json`
- `src/main/resources/data/hemomancy/recipe/pale_silver_block.json`
- `src/main/resources/data/hemomancy/recipe/pale_silver_ingot.json`
- `src/main/resources/data/hemomancy/recipe/pale_silver_ingot_from_block.json`
- `src/main/resources/data/hemomancy/recipe/pallid_icon.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone_bricks.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone_brick_slab.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone_brick_stairs.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone_slab.json`
- `src/main/resources/data/hemomancy/recipe/polished_venous_stone_stairs.json`
- `src/main/resources/data/hemomancy/recipe/runic_motif_paper.json`
- `src/main/resources/data/hemomancy/recipe/sanguine_conduit.json`
- `src/main/resources/data/hemomancy/recipe/sanguine_glass.json`
- `src/main/resources/data/hemomancy/recipe/sanguine_pane.json`
- `src/main/resources/data/hemomancy/recipe/scrying_dish.json`
- `src/main/resources/data/hemomancy/recipe/self_reflection_mirror.json`
- `src/main/resources/data/hemomancy/recipe/silver_chalice.json`
- `src/main/resources/data/hemomancy/recipe/spore_sac.json`
- `src/main/resources/data/hemomancy/recipe/spore_sac_from_vivacious_spores.json`
- `src/main/resources/data/hemomancy/recipe/tears_of_silthmere.json`
- `src/main/resources/data/hemomancy/recipe/unstained_boots.json`
- `src/main/resources/data/hemomancy/recipe/unstained_chestplate.json`
- `src/main/resources/data/hemomancy/recipe/unstained_helm.json`
- `src/main/resources/data/hemomancy/recipe/unstained_leggings.json`
- `src/main/resources/data/hemomancy/recipe/unstained_shield.json`
- `src/main/resources/data/hemomancy/recipe/unstained_warhammer.json`
- `src/main/resources/data/hemomancy/recipe/venous_stone_slab.json`
- `src/main/resources/data/hemomancy/recipe/venous_stone_stairs.json`
- `src/main/resources/data/hemomancy/recipe/vial_rack.json`
- `src/main/resources/data/hemomancy/recipe/vivianite_glass.json`
- `src/main/resources/data/hemomancy/recipe/vivianite_pane.json`
- `src/main/resources/data/hemomancy/recipe/vivianite_scalpel.json`

### A16. Registered blocks with no loot table (80; many are intentional: liquids, fillers, set pieces)

`abocipher_emitter`, `altar_of_cleansing`, `antecedent_vessel`, `bell_sleeper`, `blood_basin`, `blood_echo_inscription`, `blood_pylon`, `blood_trial_altar`, `bog_body`, `brood_tumor`, `cap_bridge`, `chapel_focus`, `consecrated_bloodwell`, `containment_anchor_block`, `covenant_throne`, `crimson_flames`, `current_strand`, `current_tissue`, `engram_block`, `escharian_overgrowth`, `escharian_overgrowth_rim`, `filler_block`, `fold_veil`, `frozen_cruor`, `frozen_devotee`, `fruiting_body_cache`, `fungal_implantation_pylon`, `fungal_podium`, `hematic_stake`, `holdfast_root_mass`, `house_marker`, `hydra_crown`, `hyphae`, `kindled_tissue`, `lethean_poppy`, `litany_stone`, `marimo_heart`, `marimo_moss`, `marrow_plume`, `messenger_buoy`, `morphic_nectar_block`, `mycelial_lantern`, `nest_cup`, `non_euclidean_hallway`, `offering_gate`, `offering_stand`, `opened_niche`, `petrified_follower`, `phlegethontic_ichor_block`, `pulse_pylon`, `qliphoth_bloom`, `recollection_cyst`, `relay_marker`, `reliquary_sconce`, `rendering_bile_block`, `ring_door`, `rite_fragment_inscription`, `rope_pod`, `rot_pyre`, `sanguine_vigil`, `scar_station`, `sclerotium_shell`, `sealed_niche`, `spawn_mark`, `sunburst_bloom`, `sunburst_crust`, `sunburst_frond`, `sunburst_lamp`, `suspended_blood_crystal`, `suspended_cleansed_blood_crystal`, `suspended_vivianite`, `vent_offering_basin`, `visceral_mirror`, `ward_anchor`, `warden_bloom`, `warp_chair_filler`, `white_humor_block`, `witness_devotee`, `witness_organ`, `witness_stone`
