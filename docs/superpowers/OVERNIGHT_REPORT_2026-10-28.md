# Overnight run report — 2026-10-28

Every decision made on your behalf (`Ruling:` and spec-ruling lines) and every review finding deferred as minor, collected from the section ledgers in `.superpowers/sdd/`. Specs and plans are in `docs/superpowers/specs|plans/2026-10-28-*`. Nothing is committed; nothing has been seen in a client.

## Section 3 — Heart-node sanctum

**Decisions made for you**

- Spec rulings: plant only Apotheos/dimension/network ground; one node per player (replanting withers the old); recipe bleeding heart + 4 spore sacs + conscious mass; footprint r8 h±6; 8 calcified ribs r6 every 100 ticks into air only; bonus Regeneration I + 4 spores per 40 ticks; forced respawn on node.above; death cache only for an Apotheos with a node and no keepInventory, nearest air within 8; molting not built.
- Ruling: HeartNodeRulesTest and HeartNodeRules were written in one step, so RED was not observed — cost: the rule tests prove values, not that they would catch a regression they never saw fail.
- Ruling: the login respawn check clears only forced Gardens respawns not on a node (beds/anchors untouched) — cost: a forced respawn set by another mod in the Gardens would be cleared.
- fixed I-3 planting side effects before the event was final — refusal at HIGH, plant() at LOWEST (cancelled events never reach it) — Ruling: no automated test (event-bus ordering) — cost: proven by code reading only.

**Deferred minors**

- Creative overflow lost when opening a cache (Inventory.add discards in Creative).
- an ownerless cache is left if the BE is missing after setBlock.
- /setblock or structures place ownerless nodes anywhere (no canSurvive gate).
- privacy covers player break/place only (TNT, lava buckets, multi-block halves, owner's FakePlayers refused).
- cacheSpot accepts air only (deep water/lava deaths fall back to vanilla drops); claimed areas may lock a cache.
- a reset (non-Apotheos) player keeps their forced Gardens respawn.
- tests cover store/takeAll and rules, not onDrops/cacheSpot/login end-to-end.
- ribs start at the node's y regardless of slope.

## Section 4a — Silentium possession

**Decisions made for you**

- Spec rulings: offerings are loose items in the rite space (the shared brazier matcher demands an exact signature, so optional braziers would change every rite); surrogates are the 9 exact vanilla types; stats per SurrogateForm table (bat 3 ... golem 14); hit cost 1 (2 at >=6 dmg); shell 3; Ambergris regen 1/100 ticks; Mind Broken 6000 ticks, no cures, -2 attack damage, Will chance x2, whisper on application; return = hold Thread of Will sneaking 40 ticks; logout/login return cleanly; leaving the Gardens = ejection; donkey keeps inventory; armor stand gear left in the Gardens on a fresh stand; items found in the Gardens come home.
- Ruling: the rite's bloodCost is 0 (CardinalRiteProgressionResourceTest forbids a lump Harbinger blood cost; the spec's 400 is dropped) — cost: the rite costs only its ceremony.
- Ruling: the possession rite sits in the recipe map's Ancestral family — cost: one map family to move later.
- fixed I2 Thread of Will bypassed block use (chests, beds, chairs) — setUseBlock(FALSE) with the Thread, EntityInteract cancelled, ChamberVisitService.canStartVisit refuses a possessor, refusal refuses during a visit or projection — Ruling: no automated test (event wiring) — cost: proven by reading.
- Ruling (I4, decision for the user): a possessed will cannot attack other players (AttackEntityEvent cancelled on Player targets), so a possessor is not an unkillable PvP fighter — cost: one event to remove if PvP possession is wanted.
- fixed M1 (raised) the Thread always gets slot 0 when the inventory is full; the displaced stack is stashed in the attachment and comes home — Ruling: no test (needs registries) — cost: reading only.

**Deferred minors**

- M3 armor-stand gear left behind is what is worn at return, not the stand's original gear.
- M4 partly fixed (echo created first); other begin failures after side effects remain theoretical.
- M6 dying or removeAllEffects clears Mind Broken.
- M7 blaze fireball places fire.
- M8 totem uses vanilla totem particles.
- M9 whisper dialogue opens on the same tick as the cross-dimension teleport.
- M10 PossessionRules.mayBegin is tested but refusal() does not call it.
- M11 FlightArbiter revoke ignores other flight sources; casting while possessed is not blocked.
- M12 cached facsimile body never ticks (bat pose frozen), player-sized hitbox, no nameplate.

## Section 4b — Gaze/grab, adipocere, Visitation

**Decisions made for you**

- Spec rulings: gaze drains 1 Concentration / 20 ticks within 10 blocks with sight, shell does not stop it, Darkness 40 ticks + heartbeat as the readable edge, non-possessing players untouched; grab = ejection with Mind Broken 12000; adipocere: axe-scrape on unharvested bog body (regrows), 35% bog revenant drop, distillation honeycomb + ruinous enzyme catalyst (bleeding bulb dropped: distillation is one ingredient + one catalyst), +3 max Concentration as a possession offering; Visitation: Apotheos only, onto the heart-node else the Spine landing, stores the Spine return point.
- Ruling: Visitation takes 2 spore sacs in its braziers — the selector test refused two rites with identical selectors (possession and visitation) — cost: one offering per visit.

**Deferred minors**

- gaze drains creative/spectator possessors.
- Visitation skips the Spine's 500-blood cost (2 spore sacs instead) — design call for the user.
- bog body scrape uses vanilla wax-off particles (levelEvent 3004); an axe in the main hand always scrapes instead of harvesting vivianite.
- Visitation does not refuse during a Chamber visit.
- ADIPOCERE_BONUS asserted in the gaze test; no test of grab-vs-invulnerability.

## Section 5 — Mausoleum complexes

**Decisions made for you**

- Spec rulings: one structure per Saint on the existing mausoleum type, one random-spread set (40/12); biomes per Saint (badlands/savanna; meadow/cherry/plains; swamp/mangrove/dark forest; snowy); outer pieces are per-Saint copies of the hand-built top/stairs/antechamber/trialroom (2-way only) with pools re-pointed and palettes swapped, wool markers replaced; generated seal hall (treasure barrel + 9 seal blocks), 15-long nave (3 Chapel jigsaws, 9 ring-door blocks, Velorum's kneeling congregation), 3 Chapels (focus with Kind/Slot NBT; kinds per Saint table), chamber (Saint's sarcophagus + signature set piece); the old mausoleum structure stays disabled.
- Ruling: MausoleumSealRulesTest written with the rules in the same step (RED not observed) — cost: values only.
- Ruling: generator lives in git-ignored tools/worldgen like the Gardens generator — cost: the user must keep tools/ to regenerate.
- Ruling: MausoleumComplexResourceTest written after the generator (RED not observed) — cost: proves the generated output, not that it would catch a regression.
- fixed C2 seal walls were mineable — MausoleumEvents: no survival break and no explosion damage inside any hallowed_mausoleums structure piece — Ruling: no automated test (needs a placed structure) and the whole complex is consecrated (players cannot break blocks they place inside either) — cost: one event.
- fixed I2 site check sampled 32 of the ~76-block chain — MAUSOLEUM_FOOTPRINT_RADIUS 72 — Ruling: no test (constant) — cost: slower site checks.
- Ruling: Seraphae biomes swap plains for flower forest (while hunting a GameTest regression it was the first suspect; kept: plains is too common for a Saint's tomb) — cost: one biome list.

**Deferred minors**

- GameTest for the seal's D8 gate covers only mayPass; generator is git-ignored; back-connector jigsaws point at real pools (wasted placement attempts); copied rooms floor y=1 vs generated y=0 (one-block step).

## Section 6 — Chapels

**Decisions made for you**

- Spec rulings: one D8 challenger at a time; leaving (>12 blocks), dying or a wrong answer resets with a 100-tick cooldown; Beholding = 4 corner flashes (25 ticks apart, never the same corner twice running) then walk them in order within 400 ticks; Litany = 3 of {bone, rotten flesh, spider eye, ink sac, glowstone dust, redstone} named in an inscription veiled in the Abocipher font unless the reader has Abocipher literacy, offered in order by right-clicking; Downfall = 3 waves of 3 Saint mobs (Hemorath hematic constructs, Seraphae vexes, Putriciel bog revenants, Velorum strays); Repose = 600 progress of stillness within 3.5 blocks (Silentium counts double), a 2-damage magic pulse + Darkness every 60 ticks; completion lights the focus (LIT) permanently.
- Ruling: Downfall and Repose are covered by rules tests only (they need a live player) — cost: proven in play only.
- fixed I-5 placed blocks could not be removed — EntityPlaceEvent cancelled in hallowed pieces (survival) — Ruling: no automated test (structure) — cost: reading.

**Deferred minors**

- reload mid-trial loses timer/cooldown; Downfall wave UUIDs vs unloaded entities; Peaceful auto-completes Downfall; Downfall farmable by stepping out.
- chapelCompleted counts Chapels around the Chapel, not the door (agrees in this layout).
- hallowed_burn lacks bypasses_armor; ChapelRules/MausoleumSealRules compare degree literals (ProgressionGates.hasDegree preferred); failure cooldown is silent; STRAY Downfall branch unreachable (Velorum has no Downfall Chapel).

## Section 7 — Ring door + hallowed ground

**Decisions made for you**

- Spec rulings: a node lights per completed Chapel (Chapels within 24 blocks); a Silentium with all 3 nodes pays 3 x 300 blood (BloodAccounting source ring_door, category WORLD, counts as spend) and the door falls after 40 ticks (PROPER); an Apotheos shatters at once (FORCED) and the tomb records the skipped Chapel kinds; first opening wins; doors stay open. Hallowed ground: Apotheos in a hallowed_mausoleums structure piece loses 4 spore load/s, then takes 1 hallowed_burn/s; Silentium and lower Harbingers untouched.
- Ruling: the Silentium's projection and three blood rings are one click (no multi-tick cutscene yet) — cost: the door's opening is plain.

**Deferred minors**

- none

## Section 8 — Saint awakening

**Decisions made for you**

- Spec rulings: the Saint reads its tomb's DoorMode/SkippedChapels at spawn; FORCED = +30% attack damage, +15% speed, Downfall skipped +50% max health, Repose skipped +20% speed, Beholding skipped permanent Resistance I, Litany skipped permanent Regeneration I; FORCED rewards turn hallowed_residuum_<saint> into hallowed_residuum_fragment_<saint> (3 fragments craft one residuum); PROPER and NONE fight as designed; the door record is re-applied to the tomb when the Saint returns, so a shattered door wakes it enraged again; Velorum's congregation joining a side and Hemorath's swelling are NOT built (per-Saint overhaul stays deferred).
- fixed I-2 Velorum's stillness break removed the enrage's Resistance — SaintTrialEntity.reapplyEnrageEffects called after Velorum's removeEffect — Ruling: no automated test (needs a live stillness break) — cost: reading.

**Deferred minors**

- most Saint damage is fixed magic, so +30% attack mostly helps melee; Seraphae/Putriciel ignore the Downfall health bonus and Regeneration — enrage is strongest on Hemorath and Velorum.
- enrage dodges (a non-Apotheos wakes the tomb for an Apotheos; peaceful syringe draws give whole residuum after each return).
- Recollections are tradeable and the Loom has no degree gate; mausoleum_treasure still drops memory_crimson_tithe directly.
- recollection cysts are fragile (0.6 strength, piston DESTROY) and can be griefed; Creative discard can lose one.
- inline fully-qualified names and a duplicated Chapel-name parse in SaintTrialEntity; bindTomb javadoc detached.

## Section 9 — Recollections

**Decisions made for you**

- Spec rulings: one Recollection item per Saint, drawn from rare recollection cysts (rarity 1/24 chunks) in sporecrown thicket, hyphal spires and mycelial depths (the Archive biomes), on network ground; only an Apotheos or a possessing Silentium takes it (by touch, since a possessed body cannot use blocks); the four Canon Memory weaving recipes now need the Saint's Recollection as a second catalyst beside its Residuum — the full Memory -> gift rework stays the user's call.
- Ruling: the canon recipes were rebuilt from their committed text (SupportingLoreCanonSourceTest pins their exact formatting) — cost: none.

**Deferred minors**

- none

## Canon conflicts the docs pass found but left for you

- The Saints are still called "post-release WIP" in LORE_REFERENCE (Release scope), HEMOMANCY_REFERENCE ~1145, wiki/Public-Alpha-Readiness (pinned by a test) and wiki/Saints-and-Encounters, while the four complexes now generate.
- "Severance" now means the Qliphoth cut, but the Unstained founder cure route and the Silent Archon armour ability ("Silent Severance", a lang/code id) still use the word.
- The sarcophagus has no D8 gate: a relic, Foul Paste or a misaligned vial can still wake a Saint at any degree (the D8 gate is the seal and door in front of it).
- LORE_REFERENCE §3.3 "Transcendence (Late/DLC)" overlaps the Apotheos identity.
- Velorum keeps internal `MARTYR` names in code; wiki/Harbinger-Path says the Archon severs bloodline members where LORE says the D6 leader does.
- `abhorent_thought` -> `abhorrent_thought` (Section 0) has no registry alias, so thoughts and eggs in existing saves vanish (the mod is not live, per your ruling).
