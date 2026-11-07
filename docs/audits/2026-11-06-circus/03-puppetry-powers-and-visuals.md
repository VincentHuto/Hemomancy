# Circus audit 3: the player's puppetry, mental powers and their visuals

Date: 2026-11-06. Scope: what a player gets from the Crimson Troupe as a power kit (the shaped bodies, Marionette
Crossbar, Spindle, commands, the Ringmaster Pattern, Thread Ripper), the mental verbs the design promises, the
perception/acclimation presentation, and how all of it looks against the current visual standard. Compared against one
Brined Court house (a Morphling strain). Method: source reads under `src/main/java`, `src/main/resources`,
`src/test/resources/architecture/` and `tools/circus/`, plus the docs the brief listed. Nothing was built, run or
launched, so "exists" means "exists in source", never "seen in game". Paths below are relative to
`src/main/java/com/vincenthuto/hemomancy/` unless they start with `docs/`, `tools/` or `src/`. Items already in the
2026-11-02 cleanup audit are cited by their `AN-` id rather than re-reported.

## Summary

The puppetry stack is solid plumbing: one definition list, one factory, one owner/upkeep/tether lifecycle, a Crossbar
with a generated model that reacts to its bodies, and arrival/departure geometry (`PuppetStringsGeometry`) built to the
Riposte bar. What sits on top of that plumbing is thin for a path meant to be the Court's equal:

- **The mental powers are missing.** The design promises attention, illusion, decoys, displacement, light and sound,
  and "shared perception can become local reality". In code the player gets exactly one of those verbs, and only
  second-hand: the Scarlet Mummer's automatic taunt. Decoys, displacement, flash and sound exist in the mod, but on
  enemies (Vesper, the Acrobat NPC, the Luminal Cicada) or on an armour set, never in the Circus kit.
- **The pavilion's "tangible manifestations" are documented as built and do not exist.** No private geometry at 500 or
  1000 acclimation, no Succession public reveal, no `circusDistortion` setting. Perception is a screen-edge tint,
  three vanilla particle types and a few flat quads near the carousel.
- **The D4 capstone is close to nothing.** The Ringmaster Pattern takes the slot it adds, so it gives no extra
  fighting body; its relay only matters when a second Crossbar is held in the other hand; nothing shows it working.
- **The roster is a damage roster.** Six of eight bodies are damage dealers; the Vulture and the Juggler are both D3
  ranged volleys. The professors' own acts (the Acrobat's vault, the Knife Thrower's knives, the Stilt Walker's height)
  never reach the player.
- **There is no strain-style table.** Per-body facts (component item, teacher, thread anchor, tooltip note, unlock kind)
  live in at least six separate switches, and one of them is already wrong in the Summons tab.
- **Presentation is a level below the Court.** No puppet HUD, no review client, no custom sounds, threads that look the
  same in every command state, a C+ Thread Ripper, and particle-only focal moments on the one mental body. The
  Crossbar and the Juggler carry brass, against the binding "no copper, brass or bronze blood-side" ruling. All nine
  Circus NPC models are vanilla `HumanoidModel` without elbows.

Counts: **16 findings** (3 High, 8 Medium, 5 Low). Vanilla `ParticleTypes` in the eight player-puppet entity classes:
**0**. In Circus NPC, perception and fauna files: **25 allow-listed uses in 10 files**, 22 of them "pending the sweep"
(Acrobat 3, Carousel 3, Fire Eater 3, Knife Thrower 1, Ringmaster 1, `CircusDemonstrations` 1,
`CircusPerceptionOverlay` 3, Luminal Cicada 2, Peacock Spider 5; Stilt Walker's 3 are block debris). Custom Circus or
puppet sound events in `common/init/SoundInit.java`: **0**.

## Power inventory

"Visual quality" grades use the manipulation catalogue's letters where one exists (`docs/archive/audits/MANIPULATION_VISUAL_CATALOGUE.md`);
otherwise a plain verdict against `docs/MANIPULATION_VISUALS.md`.

| Power | Promised by | Exists? | Code path | Visual quality |
| --- | --- | --- | --- | --- |
| Call / recall a prepared body | HR §17 | Yes | `common/item/harbinger/tool/MarionetteCrossbarItem.java:369-459` | Good: `PUPPET_DESCEND` / `PUPPET_UNSPOOL` at the Riposte bar (`client/render/world/PuppetStringsGeometry.java`), catalogue "2b", pending in-game review. Vanilla `EVOKER_PREPARE_SUMMON` / `CHAIN_BREAK` sounds |
| Command modes Follow / Guard / Hunt / Passive | HR §17 | Yes | `common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java:22-38`; radial `client/screen/summon/CrossbarRadialScreen.java:51-74` | Weak: text-only radial, action-bar line and a wooden button click. Nothing in the world changes with the mode |
| Focus (strike a hostile with the Crossbar) | HR §17 | Yes | `MarionetteCrossbarItem.java:500-532` | Weak: action-bar line only; no mark on the target, threads unchanged |
| Field transposition (Skein Transposition hot swap) | HR §17 | Yes | `PuppeteerCrossbarCommands.java:52-114` | Adequate: reuses descend/unspool. Radial ring is text only |
| Control threads | HR §17 | Yes | `client/render/world/PuppeteerThreadRenderer.java` | B: tapered Animus strand with a riding blood bead and Morphling "gnaw" gaps. One look for every state (mode, focus, starving, missing-Crossbar countdown aside from fade) |
| Veinwing Vulture (D3 flying striker) | HR §17, CIRCUS.md | Yes | `common/entity/summon/VeinwingVultureEntity.java:79-117` (4-6 feather volley + melee) | Generated rig; no shoot beat (AN-24); vex sounds (AN-31) |
| Marrow Juggler (D3 three-dagger volley) | HR §17 | Yes | `common/entity/summon/MarrowSpitterEntity.java:117-127` | Generated six-armed rig; juggling loop has no throw beat (AN-24); brass fittings (P-10) |
| Gorebound Hulk (D4 bruiser) | HR §17 | Yes | `common/entity/summon/GoreboundHulkEntity.java` | Generated jointed chains; wind-up pose; clock note AN-23 |
| Scarlet Mummer (D4 attention controller) | HR §17, track 03 "anchor" | Yes, automatic | `common/entity/summon/ScarletMummerEntity.java:86-180`, `ScarletMummerRules.java:4-8` | Poor for a focal move: gorget flare in the model, 18 `serpent` motes and vanilla `ILLUSIONER_CAST_SPELL`; no link to the redirected mobs; the evade is an instant `setPos` with 10 motes (P-6) |
| Sanguine Hound (D4 pursuit, ruptures into Blood Curs) | HR §17 | Yes | `common/entity/summon/SanguineHoundEntity.java:90-114` | Adequate: blood cells and glow, vanilla `SLIME_SQUISH_SMALL` |
| Cinder Bellows (D4 fire cone) | HR §17 | Yes | `common/entity/summon/CinderBellowsEntity.java:64-75` | Best of the set: Flammeus thermal flames with a synced breath pitch, ember glow layer |
| Mnemonist Puppet (D5 memory replay) | HR §17 | Yes | `common/entity/summon/MnemonistPuppetEntity.java:143-178` | Mixed: `MnemonistThreadLayer` tendril spool is good; the replay itself is the legacy `VisceralMesh` `MNEMONIC_REPLAY` path (`client/render/world/ManipulationSignatureEffects.java:119-126`) plus vanilla `SCULK_SHRIEKER_SHRIEK` |
| Ringmaster Pattern (D4 Conductor, Succession) | CIRCUS.md, HR §17 | Yes, near-zero effect | `common/entity/summon/RingmasterPatternEntity.java`, `puppeteer/RingmasterConductorRules.java:6-16`, `PuppeteerCrossbarCommands.java:40-50` | Static emissive regalia texture; no relay visual (P-3) |
| Thread Ripper (Liberation memory) | CIRCUS.md | Yes | `common/manipulation/ductilis/ThreadRipperManip.java`, `ThreadRipperRules.java` | C+ (catalogue row "thread_ripper"): three ribbons that part, drawn eye to eye, not on the visible thread (P-9) |
| Redirect attention as the player's own act | Track 03 verbs table, C-02 | No (puppet-only, automatic) | only the Mummer | n/a |
| Decoy (duplicate, false position, false wind-up) | Track 03 verbs, C-02 | No for the Circus player | exists elsewhere: `common/entity/summon/PhantasmalEchoEntity.java` (Phantasmal Bloodlust armour), Vesper's decoys (`common/entity/boss/endgame/VesperBorrowedAttacks.java`) | n/a |
| Displace (short teleport, swap, entrance/exit) | Track 03 verbs, "an acrobat uses ... safe teleport seams" | No for the player | enemy only: `common/entity/npc/circus/CircusAcrobatEntity.java:96-102` (vanilla `PORTAL` + `ENDERMAN_TELEPORT`); Mummer's 2.5-block sidestep | n/a |
| Flash (light burst, concealment) | Track 03 verbs | No for the player | fauna only: `common/entity/mob/animal/LuminalCicadaEntity.java:122-131` (vanilla `FLASH`, `END_ROD`) | n/a |
| Sound (echolocation, displaced cues) | Track 03 verbs | No | no Circus or puppet entry in `common/init/SoundInit.java` | n/a |
| Manifest (thought given a temporary server body) | Track 03 verbs, C-09 | Only as ordinary puppets | nothing ties a body to perception or acclimation | n/a |
| Private geometry at 500 / 1000 acclimation, Succession public reveal | CIRCUS.md:32, track 03 "Tangible pavilion manifestations" (marked IMPLEMENTED) | **No** | no code anywhere (P-2) | n/a |
| `circusDistortion` refraction toggle | CIRCUS.md:32, track 03 | **No** | not in `config/` or anywhere in `src` (P-2) | n/a |
| Professions through existing equipment (flame eater's torch form, juggler's orbit, acrobat's mobility) | Track 03 "Professions through existing equipment" (SETTLED) | No | the Circus teaches no staff form, manipulation or skill | n/a |
| Ringmaster staff form | Track 03 | Deferred | D-18 in `docs/OPEN_QUESTIONS.md` | n/a |

## Compared with a Brined Court house

One house's strain against one Circus body (or the Circus kit where no per-body equivalent exists).

| Aspect | A Court house (its Morphling strain) | The Circus | Gap |
| --- | --- | --- | --- |
| Single source of truth | `common/item/harbinger/morphlings/MorphlingStrains.java`: one row (tendencies, house, home, Cradle flag, Last Rite, polyp family, HUD art, order) read everywhere; `MorphlingStrainTableGuardrailTest` rejects ids outside it | `PuppeteerSummonDefinitions.java:17-42` carries stats only; component item, teacher, thread anchor, tooltip note, unlock kind and practical live in separate switches (P-5); no guardrail | Large |
| Power ladder | passive + Developing + Mature + Apex + Primal over six stages, bonding blood, home bonus (`docs/MORPHLING_STRAINS.md`) | one fixed behaviour per body; growth only through the shared Puppeteering skills (`common/init/skills/SummonSkillBranch.java`), none per body | Large |
| Player-triggered power | Primal actives with cost and cooldown; reactive procs | modes and focus only; no body has an act the player chooses to fire | Large |
| Mental/bodily identity | bodily, as designed | promised mental verbs mostly absent (see inventory) | Large |
| Death-save | Ink Cap, Hushbell Last Rites | none | Medium (may be right for a puppet path) |
| Generated models | `tools/model_export/morphling_strains.py`, 16 anchors, stage growth, motion answering synced bits | `tools/circus/build_*.py` on `puppet_rig.py`, eight rigs, 128 px atlases at 16 px/block (Pattern drawn at x1.12) | Comparable |
| World effects | `MorphlingStrainWorldRenderer` on `OutpouringBrushes` for strands, patches, roots, dissolve, frost veil | threads, descend/unspool, Bellows breath, Mnemonist spool; Mummer, Hound, Vulture, Juggler and Pattern are particle-only or nothing | Medium |
| HUD | `client/screen/overlay/EquippedMorphlingOverlay.java`, animated per-strain sprites, bond meter | none; Crossbar item bar plus action-bar text (P-7) | Large |
| Review client | `morphlingStrainReviewClient`, 256 shots and contact sheets | none for puppets, Crossbar, threads or the Pattern; CIRCUS.md:55 | Large |
| NPC rig | Court roles on the jointed `court_humanoid.py` rig, `_anim.java` per role | nine Circus NPC models on vanilla `HumanoidModel`, five hand-written (P-11) | Large |
| Sounds | none custom either (AN-13 covers the Court) | none custom; sound is one of the Circus's own verbs (P-14) | Small, but worse for the Circus |
| Material ruling | no copper/brass/bronze blood-side, honoured | brass on the Crossbar and Juggler (P-10) | Small, binding |

## Findings

### 1. Powers

**P-1 · High · The Circus gives the player no mental power of its own.**
Evidence: the promised verbs are listed in `docs/archive/tracks/circus/03-puppetry-and-circus-mechanics.md:160-173`
(redirect attention, decoy, displace, flash, sound, manifest) and the boundary rulings in `docs/CIRCUS.md:63,68`. The
only one a player can field is the Mummer's attention pulse, and it fires itself every 160 ticks when threats exist
(`common/entity/summon/ScarletMummerEntity.java:86-106`, `ScarletMummerRules.java:4-8`). `common/mission/circus/CircusCurriculum.java:10-17`
teaches only the seven bodies; nothing in `common/capability/player/harbinger/circus/` or `common/init/ManipulationInit.java`
is a Circus-taught act. The verbs exist elsewhere and on the other side of the table: decoys on Vesper
(`common/entity/boss/endgame/VesperBorrowedAttacks.java`) and the Phantasmal Bloodlust armour
(`common/armor/ability/PhanstmalBloodlustArmorAbilityHandler.java:92-101`, `PhantasmalEchoEntity`), displacement on the
Acrobat NPC (`common/entity/npc/circus/CircusAcrobatEntity.java:96-102`), flash on the Luminal Cicada
(`common/entity/mob/animal/LuminalCicadaEntity.java:122-131`). The "professions through existing equipment" section
(track 03:175-185, SETTLED) is not built either.
What is wrong: a Court house hands the player five powers on a ladder; the Circus hands them seven bodies that mostly
hit things. The path's identity ("shared perception can become local reality") never reaches the player's hands.
Proposed fix: give each body one player-fired **act** on the Crossbar (sneak-use, or a third radial ring), drawn from
the verbs and sited on the existing puppet lifecycle so no second command surface appears: Mummer "Spotlight" (forced
attention on one chosen target, the verb it already has), Vulture "Trapeze" (owner and Vulture swap places through
`StandingSpaces`-style safe checks), Juggler "Feint" (a false wind-up decoy that draws a melee swing), Bellows
"Smoke" (a concealment puff), Mnemonist "Encore" (replay on demand), Hound "Scent" (reveal), Hulk "Lift" (carry/throw
an ally). Each act gets a `<Body>ActRules` pure class, a `ManipulationVisuals.Form` with its own geometry class, and a
row in the table P-5 proposes. Effort: L.

**P-2 · High · The tangible pavilion manifestations and `circusDistortion` are documented as implemented but are not in code.**
Evidence: `docs/CIRCUS.md:32` ("From 500 private geometry (galleries, stairs) supports the player; at 1000 the upper
gallery and canopy are privately solid ... `circusDistortion` disables client refraction"); track 03:235-250 marks
the whole section **IMPLEMENTED**, including server-authorised masks, acknowledged revisions, deferred occupied cells and
the Succession public reveal. In source: no setting named `circusDistortion` in `config/` (`HemoClientConfig.java`
has no Circus entry); no class or field reads acclimation for collision, drawing or block placement
(`grep -i acclimation` hits only progress, routes, the overlay and `CircusPerformanceController`);
`common/network/circus/PacketSyncCircusPerception.java` carries only `(acclimation, active)`;
`common/entity/npc/circus/CircusPavilionSavedData.java` stores carousel masks but no reveal nodes; `git log -S circusDistortion`
finds only the 8d01a316c docs commit.
What is wrong: C-09 ("shared perception can become local reality") and C-10 ("calmer and more solid") are the
Circus's signature rulings, and the one mechanic that expresses them is missing while the docs say otherwise. Players
and agents will look for it.
Proposed fix: decide first (see Decisions). If building: a per-player `CircusManifestCells` set computed from the
authored template, synced by revision, consulted by a `CollisionContext`/`getCollisionShape` hook on a dedicated
"pavilion scaffold" air-like block placed by the template, drawn from `WorldEffectDispatcher` on a school material.
If not building: move the paragraph in CIRCUS.md and track 03 under "not built", and drop `circusDistortion` from both.
Effort: L to build, S to correct the docs.

**P-3 · High · The Ringmaster Pattern adds no fighting body and its relay almost never applies.**
Evidence: `common/entity/summon/puppeteer/RingmasterConductorRules.java:11-13` adds one to the shaped cap while a
Conductor is active, and the Conductor counts as a shaped body itself (`MarionetteCrossbarItem.java:406-418`,
`PuppeteerCrossbarCommands.java:76-83`), so the number of other bodies is unchanged. The mode relay writes the mode to
the *equipped Crossbar* of each relayed body (`PuppeteerCrossbarCommands.java:40-50` via `findEquippedCrossbar`,
`MarionetteCrossbarItem.java:169-182`), and bodies on the same Crossbar already obey it; bodies on an unequipped
Crossbar unravel after the grace countdown anyway. Focus relay (`MarionetteCrossbarItem.java:514-524`) has the same
two-Crossbar precondition. The body itself is 28 HP, 1 damage, call 52, upkeep 14/min
(`PuppeteerSummonDefinitions.java:35-37`), with plain melee goals and no attention (`RingmasterPatternEntity.java:34-64`).
Its only visual is a static emissive texture (`client/render/entity/summon/RingmasterPatternRenderer.java:86-99`).
What is wrong: the Succession capstone, earned through 1000 acclimation and five challenges, plays as a weak extra
body for one-Crossbar players, and nothing on screen says "the Ringmaster is conducting".
Proposed fix: make the office do something with one Crossbar: either (a) the Conductor is free (does not count against
the cap), or (b) it grants a group act: "Ring" — every body within 32 blocks performs its P-1 act together once per
cooldown, or simply all bodies' attacks land on one beat. Draw relays as a pulse travelling from the Pattern's baton
along each relayed thread (`PuppeteerThreadRenderer` already knows the groups). Keep "never chains, never changes
ownership". Effort: M.

**P-4 · Medium · Six of eight bodies are damage dealers, and the two D3 bodies overlap.**
Evidence: `PuppeteerSummonDefinitions.java:17-42`; the Vulture fires a 4-6 feather volley at 5-14 blocks and melees
(`VeinwingVultureEntity.java:79-117`); the Juggler fires a three-dagger volley every 35 ticks
(`MarrowSpitterEntity.java:117-127`). Hulk and Hound are both ground melee; the Mnemonist's "memory" is also damage
(`MnemonistPuppetEntity.java:143-166`). Only the Mummer controls. The teaching NPCs keep their own tricks: the Acrobat
vaults behind targets (`CircusAcrobatEntity.java:84-102`), the Knife Thrower throws, the Stilt Walker towers; none of
it reaches the body they teach.
What is wrong: the roster reads as "strike, strike, tank, taunt, chase, burn, replay", not as a set of distinct
mental tools. Track 03:158 warns new bodies off "generic damage"; the old ones are mostly that.
Proposed fix: keep each body's damage but give each a distinct non-damage role through its P-1 act, and re-role one
D3 body: make the Vulture the Acrobat's displacement body (carry/swap) and leave the volley to the Juggler. Effort: M.

**P-5 · Medium · There is no puppet table like `MorphlingStrains`; per-body facts are scattered and one copy is already wrong.**
Evidence: per-body switches in `common/entity/summon/puppeteer/PuppeteerSummonTrialEvents.java:29-40` (component
recipe), `client/screen/skilltree/harbinger/SummonsTabView.java:411-419` (primary offering),
`client/screen/item/PuppeteersSpindleScreen.java:365-369` (tooltip notes), `client/render/world/PuppeteerThreadRenderer.java:44,236-238`
(thread anchor height, Hound only), `common/mission/circus/CircusCurriculum.java:10-17` (teacher, degree, a copy of the
definition's degree), `SummonsTabView.java:213-215` and `PuppeteersSpindleScreen.java:365` (Ringmaster special-cases),
`common/entity/summon/puppeteer/PuppeteerSummonBodies.java:157-165` (entity type). The Summons tab switch lacks
`SANGUINE_HOUND` and `CINDER_BELLOWS`, so their detail panel shows Enthralling Filament as the primary offering
(drawn at `SummonsTabView.java:183` and `:377`) while the trial recipes need `sanguine_hound_bridle` and
`cinder_bellows_frame` (`src/main/resources/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_sanguine_hound.json`,
`puppeteer_trial_cinder_bellows.json`). The Ringmaster Pattern, which has no trial, also shows Filament.
What is wrong: adding a body means finding every switch; the next one will be missed too.
Proposed fix: a `PuppetShapes` table of `PuppetShape` rows (definition, entity type, component item, teacher, unlock
kind ORDEAL/SUCCESSION, thread anchor scale, tooltip note key, role key, act), read by all of the above, with a
guardrail in the style of `MorphlingStrainTableGuardrailTest` rejecting body ids outside the table, `EntityInit` and
`VesperPuppetRoster`. Fix the two missing offerings in the same pass. Effort: M.

### 2. Presentation and visuals

**P-6 · Medium · The Mummer, the one mental body, presents its focal moves with particles and a vanilla sound.**
Evidence: `ScarletMummerEntity.java:130-139` (attention pulse: 18 `HemoParticleData.serpent` motes and
`SoundEvents.ILLUSIONER_CAST_SPELL`), `:166-177` (evade: instant `setPos` and 10 motes at the old spot);
`client/model/entity/summon/ScarletMummerModel.java:54-63` (gorget flare and a small sway). Up to four mobs change
target with no on-screen link to the Mummer.
What is wrong: `docs/MANIPULATION_VISUALS.md` "Impact" says particles are accents and the geometry carries the
effect; here there is no geometry, and the player cannot read which threats were pulled or that the evade happened.
Proposed fix: a `ScarletMummerGeometry` with two forms: `MUMMER_ATTENTION` (the gorget fans flare open as a lit ring,
then a thin eye-line runs from each redirected mob to the Mummer for the performance window, on the Animus or Lux
material) and `MUMMER_SIDESTEP` (a fading afterimage of the body at the old position, which also plants the decoy
verb). Send the redirected entity ids in the packet so the lines track. Effort: M.

**P-7 · Medium · There is no puppet HUD, and the threads do not show command state.**
Evidence: `client/registry/GuiLayerRegistrar.java:39-126` registers no puppet layer; feedback for mode, focus,
call and swap is `displayClientMessage(..., true)` (`PuppeteerCrossbarCommands.java:35-36,111-112`,
`MarionetteCrossbarItem.java:453-458,526-529`). `PuppeteerThreadRenderer.java:165-194` draws every thread with the same
colour, sag, wobble and bead whatever the body is doing; only dismissal fades it and a Morphling gnaws it.
What is wrong: a player with three bodies cannot see their health, upkeep deadline or mode without opening the
Summons tab; Morphling owners get a dedicated overlay.
Proposed fix: a `puppet_roster` HUD layer (one small frame per active body: shape icon, health, upkeep deadline,
mode glyph, a countdown when the Crossbar is not held), in `client/screen/overlay/`, registered in
`GuiLayerRegistrar`. Give threads state: tighten and brighten toward a focused target, a knot at the Guard anchor,
fray and pale while upkeep is short. Sync the focus target and starving flag through `BoundSummonSync`. Effort: M.

**P-8 · Medium · Perception presentation leans on vanilla particles and flat quads, and gets busier as acclimation rises.**
Evidence: `client/screen/overlay/CircusPerceptionOverlay.java:56-61` (`CRIMSON_SPORE`, `REVERSE_PORTAL`, `END_ROD`),
`:67-75` (vanilla `AMBIENT_CAVE`, `ENCHANTMENT_TABLE_USE`, `AMETHYST_BLOCK_CHIME`), `:115` (world figures on
`HemoRenderTypes.QLIPHOTH_GLOW`, a Qliphoth material), `:157-201` (each "silhouette" is two untextured trapezoids, each
"cloth" one quad, each "light" two diamonds; alpha up to 104, 72 and 126). Particle interval drops 12 → 10 → 8 ticks
with stage (`common/capability/player/harbinger/circus/CircusProgressRules.java:37-44`) and figure counts rise
(`:54-70`). The world pass only draws within 32 blocks of a carousel (`CircusPerceptionOverlay.java:106-109`), so camps
get nothing.
Routing is compliant: the screen tint is drawn from the `screen_effects` layer (`GuiLayerRegistrar.java:48-49` →
`client/event/ScreenOverlayEvents.java:39-48`, filed inside `renderMorphicNectarScreenOverlay`), and the world pass
from `client/render/world/WorldEffectDispatcher.java:81`. The edge tint (alpha 34 / 22 / 14, edges only) meets the
"subtle overlay" rule.
What is wrong: C-10 says acclimation makes the site calmer; the particle rate and figure counts go the other way. The
figures are placeholder geometry on another faction's material. The track's own safeguard "keep flash and sound
effects configurable" (track 03:206) has no setting behind it.
Proposed fix: replace the three vanilla particles with `HemoParticleData` motes (fine glow motes, as the cursor rule
asks); hold particle rate flat or falling with stage; draw the figures as textured card silhouettes or reuse the
performer echo layer idea (`client/render/entity/npc/CircusPerformerRenderer.java:66-87`) on a Circus or Animus
material; add a client config `circusPresentation` (off / reduced / full) that also covers the cicada flash. Move the
call out of the Morphic Nectar helper. Effort: M.

**P-9 · Medium · Thread Ripper and the Liberation climax look generic and do not touch the drawn thread.**
Evidence: `common/manipulation/ductilis/ThreadRipperManip.java:66` and `:89-90` send `Form.THREAD` from the
controller's or caster's eye to the target's eye; the geometry is three short ribbons that part
(`client/render/world/FerricDuctilisGeometry.java:140-154`, catalogue grade C+). The live thread the player sees runs
from the Crossbar knot or hand to the body anchor (`PuppeteerThreadRenderer.java:145-157`) and does not react to a
disrupt. Finale completion is 80 vanilla `CRIMSON_SPORE` and `TOTEM_USE` (`common/entity/npc/circus/CircusRingmasterEntity.java:352-354`).
What is wrong: the anti-tether spell's whole idea is "cut the string you can see"; the cut lands somewhere else.
The route's last beat uses the effects AN-16 already flags.
Proposed fix: a `ThreadRipperGeometry` built to the Riposte bar that draws the cut on the real thread path (share
the endpoint maths in `PuppeteerThreadEndpointRules`), with the severed ends recoiling to controller and body and the
body slackening; add a `severedUntil` client state so the thread stays cut for the 100-tick silence. Use
`PuppetStringsGeometry`'s unspool for the carousel riders. Replace the completion burst with a bespoke scene beat.
Effort: M.

**P-10 · Medium · Brass on the Crossbar and the Juggler breaks the "no copper, brass or bronze blood-side" ruling.**
Evidence: `tools/circus/build_marionette_crossbar.py:3,33-47,80` ("bone-and-brass", brass caps and end blocks);
`tools/circus/build_marrow_juggler.py:45-80,131`; `tools/circus/build_faculty.py:158-159,226,466,532`; the reference
repeats it (`docs/HEMOMANCY_REFERENCE.md` §17, "Held appearance": "bone-and-brass marionette control"). The ruling:
`docs/BRINED_COURT.md` "Rulings that bind" ("No copper, brass or bronze on anything blood-side") and the owner's
feedback on Harbinger gear and blood machines.
What is wrong: the Crossbar is the most-seen object of the path and carries the banned accent.
Proposed fix: swap the `brass` material for hematic iron or lacquered bone in the three builders, regenerate, and fix
the §17 wording. Faculty costumes are NPC dress; confirm whether they are exempt. Effort: S.

**P-11 · Low · Circus NPC models are vanilla humanoids without elbows; five are hand-written.**
Evidence: `client/model/entity/npc/CircusAcrobatModel.java`, `CircusFireEaterModel.java`, `CircusKnifeThrowerModel.java`,
`CircusStiltWalkerModel.java`, `CircusRingmasterModel.java:18` all extend `HumanoidModel` with no generator; the four
faculty extend `CircusFacultyModel.java:10` (`HumanoidModel`, from `tools/circus/build_faculty.py`). None has a forearm
part; only the Ringmaster's legs have a knee and calf. The puppets themselves are generator-made: Mummer, Mnemonist
and Pattern add forearm and shin children on `HumanoidModel`; Hulk, Bellows, Juggler, Hound and Vulture are
`EntityModel` chains. Atlases are 128 px on 128 UV (16 px/block); `RingmasterPatternRenderer.java:79` draws at x1.12
(about 14 px/block).
What is wrong: the jointed-rig rule binds new NPCs and the Court was rebuilt to it; the Circus cast is the visible
counterexample and cannot be posed for the act animations P-1 and P-6 want.
Proposed fix: rebuild the five performers and the faculty on `tools/model_export/court_humanoid.py`'s rig with
`_anim.java` poses, keeping `PerformerHandAnchors` working from the new hand part. Effort: L.

**P-12 · Low · The radial disables valid swaps while a Conductor is out, and lists eight shapes.**
Evidence: `client/screen/summon/CrossbarRadialScreen.java:134-139` computes the cap with
`PuppeteerSummonRules.activeSummonCap` only; the server adds the Conductor's slot
(`PuppeteerCrossbarCommands.java:78-83`). The outer ring iterates all definitions (`CrossbarRadialScreen.java:85`),
so the Ringmaster Pattern appears; HR §17 "Field transposition" says "all seven ordinary artificial shapes".
Proposed fix: compute the cap through `RingmasterConductorRules.activeCap` (better, a shared helper both sides call);
decide whether the Pattern belongs in the ring and align the reference. Effort: S.

### 3. Puppet Interference with Morphlings

**P-13 · Medium · Puppet Interference taxes both sides, is documented in pieces, and the client shows it under different conditions than the server applies it.**
Evidence: Morphling side x1.25 blood upkeep and Sun-Fed cancelled (`common/item/harbinger/morphlings/MorphlingUpkeepRules.java:4,20-22`,
`common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java:112-117`, `SunburstRules.java:60-61`).
Puppet side: every body pays `ceil(upkeep x 1.5)` thread at 75% tether range (`common/entity/summon/puppeteer/PuppeteerSummonRules.java:17-18`,
`common/entity/summon/BoundSummonBehavior.java:509`), with one upside, the payment refreshes the Morphling's fed timer
(`BoundSummonBehavior.java:525-530`). `docs/CIRCUS.md:49,64` and `docs/MORPHLING_STRAINS.md` "Upkeep" name only the
x1.25; the x1.5 and 75% are only in HR §17. The server qualifies a tether through
`BoundSummonBehavior.qualifiesForMorphlingInterference` (`:436-445`: alive, loaded, same owner and Crossbar, not a
trial, same dimension, same session). The client shows gnawed threads whenever the owner wears a Morphling
(`PuppeteerThreadRenderer.java:158-161`), and the "Distracted" HUD line and radial "Gnawed" line whenever any owned
mob is within 128 blocks, trial bodies included (`client/screen/overlay/EquippedMorphlingOverlay.java:60-62`,
`CrossbarRadialScreen.java:157-160`, client branch of `MarionetteCrossbarItem.activeSummonsForOwner` at `:474-478`).
What is wrong: the lore is coherent (a strain on the body distracts the thread, a thread distracts the strain), but a
combined Court-and-Circus player pays three penalties and gets one hidden refund, and reads about one of them. The
only positive overlap is incidental: Morphling ally powers count puppets as bloodline allies
(`common/item/harbinger/morphlings/MorphlingAllies.java:15`); Kinbud's Second Head is explicitly exempt
(`KinbudSecondHead.java:17`). The ruling invites combination; the code discourages it.
Proposed fix: decide whether both sides stay (Decisions). Either way, state the full rule once in CIRCUS.md and
MORPHLING_STRAINS.md, sync an `interfered` flag per body in `BoundSummonSync` so the client draws gnaw gaps and the
"Distracted" line from the server's verdict, and consider one deliberate synergy (for example Dead Man's Rope strands
may link to the host's puppets, or Ink Cap's Veil hides the threads). Effort: S for docs and sync, M for a synergy.

### 4. Supporting gaps

**P-14 · Low · No custom sounds for any puppet or Circus act; AN-24 and AN-31 are still open.**
Evidence: `common/init/SoundInit.java` has Blood Drunk Puppeteer and Enthralled Doll entries (`:237-260`) and nothing
for the Circus. Puppets use vanilla sounds (`ARROW_SHOOT`, `TRIDENT_THROW`, `ILLUSIONER_CAST_SPELL`,
`SCULK_SHRIEKER_SHRIEK`, `CHAIN_BREAK`, `EVOKER_PREPARE_SUMMON`, `WOODEN_BUTTON_CLICK_ON`) and keep zombie, wolf and
vex ambient sounds (`common/entity/summon/GroundPuppetEntity.java`, no overrides; AN-31). The Juggler and Vulture still
have no release pose (AN-24: `MarrowSpitterEntity.java:117-118` unsynced `shotCooldown`).
What is wrong: sound is one of the six Circus verbs; the path has none of its own.
Proposed fix: a small Circus sound set (thread pluck, crossbar creak, call/recall, act stings, a calliope sting for
perception stages) with subtitles; silence the inherited mob sounds; sync a throw tick. Effort: M.

**P-15 · Low · No review client for puppets, the Crossbar, threads or the Pattern.**
Evidence: `gradle/review-clients.gradle` has no puppet, Crossbar or Circus row; CIRCUS.md:55 and :105 say native
appearance of the Bellows, Juggler, Hulk, faculty and threads is unreviewed.
Proposed fix: a `puppetReviewClient` row: each body idle/attack/act, descend and unspool, the Crossbar held in both
hands with 0-3 bodies, threads in each mode and gnawed, Thread Ripper cut, perception stages at the carousel.
Effort: S-M.

**P-16 · Low · Doc drift around the puppet tooling and numbers.**
Evidence: AGENTS.md's list of version-controlled tools omits `tools/circus/`, which is now tracked
(`.gitignore:73-75`, `git ls-files tools/circus` lists 23 files); the 2026-11-02 audit's AN-9 still calls it
gitignored (fixed as ledger A-28). HR §17 "Field transposition" says seven shapes (P-12). CIRCUS.md "Puppet
Interference" omits the puppet side (P-13). CIRCUS.md:32 and track 03 describe the missing manifestations (P-2).
Proposed fix: one docs pass with the decisions below. Effort: S.

## Not a problem

- Ownership, upkeep, tether, missing-Crossbar grace, session severing and the anti-stockpile cap are one shared path
  (`BoundSummonBehavior`, `PuppeteerSummonRules`), as the "no second summon registry" ruling asks.
- Player puppets use no vanilla particles; the Hound and Mummer use `HemoParticleData`, the Bellows uses the Flammeus
  thermal flames.
- Arrival and departure (`PuppetStringsGeometry`, `PuppetStringsRules`) follow the Riposte structure: own geometry
  class, pure rules, named phases, fray-out ending.
- The perception HUD and world passes go through `GuiLayerRegistrar` and `WorldEffectDispatcher`; no
  `RenderGuiEvent` or `RenderLevelStageEvent` subscriber was found for them.
- The Crossbar model and Spindle are generator-made and react to their bodies (`client/render/item/harbinger/crossbar/`,
  `tools/model_export/puppeteers_spindle.py`).
- Thread Ripper refuses Circus performers, the Ringmaster, bosses, trial bodies, the caster's and allies' bodies
  (`ThreadRipperManip.java:128-139`).

## Decisions the owner must make

1. **Where do the mental verbs live?** (a) one player-fired act per body on the Crossbar (P-1's proposal), (b) a small
   Circus-taught set of memories or staff forms, or (c) keep puppets as they are and rewrite the docs to say the
   Circus's mental side is presentation only.
2. **Private geometry and `circusDistortion`:** build the tangible manifestations (L), or mark them not built in
   CIRCUS.md and track 03 (S). Today the docs claim them.
3. **Ringmaster Pattern:** free slot, group act, or leave it as a weak extra body? And should it appear in the
   transposition ring?
4. **Puppet Interference:** keep both the Morphling x1.25 and the puppet x1.5 thread / 75% range, or keep one? Add a
   deliberate Court-Circus synergy, or none?
5. **Roster re-roles:** re-role the Vulture (or the Juggler) so the two D3 bodies stop overlapping; carry each
   professor's own act into the body they teach?
6. **Brass:** replace on the Crossbar and Juggler (the ruling says yes); are Circus faculty costumes exempt?
7. **Circus NPC rig:** rebuild the nine Circus NPCs on the jointed rig now, or with the Circus's own pass (D-18)?
8. **Thread Ripper versus Vesper's puppets:** Vesper's bodies are `BoundPuppeteerSummon`s owned by Vesper
   (`common/entity/boss/endgame/VesperMountMoves.java:269`) and are not protected, so the Liberation reward disrupts
   them (25% max health, 5 s silence) and unravels them at 30% or less. Intended counter, or should boss-owned puppets
   be protected like bosses?
9. **A `PuppetShapes` table with a guardrail** (P-5): agree to the shape of the row before the next body is added.
