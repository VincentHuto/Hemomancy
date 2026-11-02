# Endgame Foundations (Section 0) — Design

Date: 2026-10-27. Source: `docs/Endgame Path Asymmetry Fungal Gardens  Saint Mausoleums_V2.md` and the user's rulings recorded in that doc's changes list. This is Section 0 of the endgame split: the path, travel and reset groundwork every later section checks.

## Goal

Make D8 two equal ranks (Apotheos and Silentium), gate physical Fungal Gardens travel to Apotheos, make a Harbinger reset clear endgame state, and fix the Abhorrent Thought spelling.

## Decisions

### D1. Path state
- `EnumArchonPath.SILENT_ARCHON` is renamed `SILENTIUM`. The other values stay: `NONE`, `SILENT_PENDING`, `APOTHEOS_PENDING`, `APOTHEOS`.
- The mod is not live: no alias for the old saved name and no `PlayerDataMigrations` entry. A dev save that stored `SILENT_ARCHON` reads back as `NONE`.
- The existing legacy root-key migration (`migrateLegacyArchonChoice`) maps the old `silent` choice like it already maps Apotheos: `SILENTIUM` when the degree is 8, otherwise `SILENT_PENDING`.

### D2. How a player becomes Silentium
- Defeating Vesper no longer changes the path. Victory still seals the Bloom, grants `vesper_defeated` and queues the one-time Memory of Vesper. The player stays D7 `SILENT_PENDING`.
- Absorbing the Memory of Vesper (Iron Brazier, Living Staff Blood Absorption, `LivingWeaponGraftRite`) is the ascension. It is allowed only for a player who is degree 7, `SILENT_PENDING`, and has `vesper_defeated`, plus the existing blood-active and staff-bond checks.
- On completion the rite re-checks eligibility, awakens the memory as today, then calls `DegreeProgression.advance(player, 8, EnumArchonPath.SILENTIUM)`. The degree-8 rewards (skill points, advancement, Liber, Unstained reset, sync, message) run through that one path.
- `DegreeProgression.advance(player, 8)` keeps meaning Apotheos, so the Apotheos rite is untouched.
- Between victory and absorption the player stays "choice unresolved": manipulations stay blocked except the staff, exactly as before the fight.

### D3. Permissions
- `HarbingerPathPermissions.isProvenSilentArchon` becomes `isSilentium(int degree, EnumArchonPath path)`: degree ≥ 8 and `SILENTIUM`. Every current caller (Silent Archon armor death refusal, Will commandeer cost, bound summons) moves to it.
- A choice counts as resolved for `APOTHEOS` and `SILENTIUM`.
- `/hemo qliphoth silent complete` sets degree 8 and `SILENTIUM`.

### D4. D8 is no longer a synonym for Apotheos
Everything that read `degree >= 8` to mean Apotheos now reads the path:

| Site | New rule |
| --- | --- |
| Blood vessel HUD (halo, animated fill, meniscus, base frame) | Apotheos art only for `APOTHEOS`. Silentium draws the D7 vessel (dedicated art is later work). |
| Arbor of Will visual degree | `APOTHEOS` keeps 8; anyone else is capped at 7 |
| Monolith Fragment burden | `APOTHEOS` carriers only |
| Primal morphling (Morphic Nectar) | `APOTHEOS` only |
| Item inquiry context flags | `silentArchon` = `SILENTIUM` path, `apotheos` = `APOTHEOS` path (drop the legacy persistent-data flags) |
| Chamber progression | `SILENTIUM` → tier 3, theme `silent_archon`; `APOTHEOS` → tier 3, theme `apotheos`; the `apotheos` theme is offered only to Apotheos |
| Manipulation cost ledger block message | "Manipulation blocked until your path is complete" |

Already correct: the Mycophant hunt (checks `APOTHEOS`), `ProgressionGates.hasDegree(…, 8)` gates that both ranks should pass.

### D5. Titles and advancement
- `hemomancy.degree.8` becomes the neutral "Eighth Degree of the Hematic Order", used where a requirement names degree 8.
- New keys: `hemomancy.degree.8.apotheos` "Apotheos of the Hematic Order", `hemomancy.degree.8.silentium` "Silentium of the Hematic Order".
- `DegreeTitles.titleKey(int degree, EnumArchonPath path)` picks the personal title. The HUD, the degree-reached message and `/hemo degree` use it.
- New advancement `hemomancy:hemomancy/degree_8_silentium` (parent `degree_7_archon`, icon `memory_of_vesper`, challenge frame, impossible criterion). `grantDegree` grants it instead of `degree_8_apotheos` when the path is `SILENTIUM`. It unlocks the same Liber entry (`TRUTH`).

### D6. Ledger
- Blood Beneath Blood completes on `SILENTIUM` or `APOTHEOS`.
- New stage between the ordeal and completion: `SILENT_PENDING` with Vesper defeated shows `blood_beneath.absorb` ("Vesper's memory waits. Burn it in a lit Iron Brazier and draw it in with the Living Staff to finish your Silence."). The open-ledger packet gains `vesperDefeated`; `PROTOCOL_VERSION` 16 → 17.
- `blood_beneath.complete` drops "Silent Archon is yours" for Silentium wording.

### D7. Fungal Gardens travel gate
- `FungalProjectionRules.travelKind(boolean revelationWitnessed, int degree, boolean spineGranted, EnumArchonPath path)` returns `FIRST_PROJECTION`, `PHYSICAL` or `REFUSED`.
  - First projection: unchanged rule (not yet witnessed, D7+, Spine granted).
  - Physical: witnessed and `APOTHEOS`.
  - Otherwise refused.
- `handleTravelUse` refuses before draining blood, with `message.hemomancy.fungal_garden_travel_helper.the_spine_will_not_carry_you` ("The Spine will not carry a body the hive no longer claims."). The Spine item and the Fungal Podium share this path.
- Unchanged: Creative bypass, returning from the Gardens, the D7/Spine checks and their messages.

### D8. Harbinger reset clears endgame state
- New `IInitiatoryDegree.resetEndgameState()`: archon path `NONE`, revelation not witnessed, Spine not granted, Mycophant exposure, retry cooldown and defeated cleared.
- `PathMutualExclusionHelper.resetHarbingerProgress` counts a non-`NONE` path or a granted Spine as progress and calls it alongside `resetPomeCommunion`.
- Out of scope: the root `REVELATION_CHOICE_PENDING` key and the `mycophant_heir` flag (root persistent data, separate S-1 work).

### D9. Abhorrent Thought
`abhorent_thought` becomes `abhorrent_thought` everywhere it is ours: registry id, classes (`AbhorrentThought*`), sound events and folder, textures, models, bbmodels, spawn egg, loot table, clairaudiograph entry, biome spawn lists and their generator script, lang keys and the display name "Abhorrent Thought", frozen registry fixtures and docs. No save migration.

### D10. Docs
Update the mechanics and canon lines that describe Silent at D7: `HEMOMANCY_REFERENCE.md` degree table and §5 ending sections, the travel paragraph, the Monolith burden line, `wiki/Harbinger-Path.md`, and `LORE_REFERENCE.md` anchor 34 and line 352. Regenerate `docs/architecture/evidence/source-text-impact-map.csv` if test reads change.

## Out of scope
Silentium powers or HUD art, possession, spore load, any Gardens content, the wider lore rewrite (Section 10).
