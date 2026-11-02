# Track 3 — Rite cinematics

**Goal:** possession is shown, not teleported. Four scenes: the **departure** at the rite, the **arrival** in the Gardens, and two **returns** (broken and clean), each with a Gardens half and a rite-space half. Built on the boss scene system (`client/render/cinematic/`: `BossScene`, `CinematicShot`, `BossSceneDirector`, `BossSceneTracks`, `BossSceneRegistry`, the scene catalog; played from the server through `BossScenes.play` and `BossScenePacket`) so they skip, clock and hold input the same way the boss intros do.

Depends on Track 1 (the medium beat) and Track 4 (bodies that animate). Read `docs/MANIPULATION_VISUALS.md` and `SaintRiseScene` / `MycophantWakingScene` before starting; the daemon is `HumanitySpriteEntity` drawn by `HumanitySpriteRenderer` (`cardinal_daemon_haze` shader, `DaemonHazeQueue`), tendrils come from the mod's tendril geometry (`BloodBindingTendrilGeometry`, the Mycophant's tendril work), spores from `HemoParticleData.sporiticSpore`, and the monolith-shatter effect exists in the Apotheosis / monolith visuals (`CrimsonCoronationGeometry` area; find the shatter cue and reuse it through a public bridge).

## Scene 1 — Departure (rite space, on completion)
1. The caster's body **de-manifests** into the echo: over ~40 ticks the player model goes translucent and dark (the echo's shadow material) while its outline stays; the echo entity is spawned under it at the same pose so the hand-off is invisible.
2. The rite's **daemon** (the `HumanitySpriteEntity` bound to this rite, which the ceremony already raised) flies through the echo and into the surrogate (a ~30-tick arc, haze trailing).
3. Surrogate and daemon **dissolve together** into spores; **tendrils** erupt from the floor around the surrogate, close over the spore mass and draw it into the ground; the echo is left standing. The Sanguine Quintessence in the Focus is spent at the tendrils' close.
4. Cut to black for the dimension change (the server teleports at the cut, not before).
Camera: a slow orbit from behind the caster to a three-quarter view of the surrogate, then down to floor level for the tendrils.

## Scene 2 — Arrival (Gardens)
1. Fade in on Gardens ground at the landing; **tendrils** rise and seize a **freshly spawned facsimile** of the form (spawned by the server for the scene, stilled, invulnerable, not the player yet).
2. The tendrils **inject**: spores and daemon haze pour in along them; the facsimile's growth overlay (Track 4) brightens from the injection points outward.
3. The tendrils release; the facsimile shudders upright; the camera snaps into first person and the possession begins (the server swaps control at this tick: the scene facsimile is removed and the player's own body, rendered as the form, stands in its place).

## Scene 3 — Broken return (ejection: Concentration gone, grab, death, leaving)
Gardens half: the possessed body **bursts** into spores and goo; hyphae tendrils from the ground **reabsorb** the spill. Cut to black.
Rite half: at the echo, tendrils **erupt** from the floor and **blast** the daemon back out in a burst of spores with the **monolith-shatter** effect; the daemon slams into the echo; the player re-manifests thrown back a block or two and **dazed** (Mind Broken is applied here, with a short Darkness/nausea tint as the camera settles). The scene ends on the player standing, Mind Broken.

## Scene 4 — Clean return (the wheel's *Return*)
Gardens half: the body **stammers**, staggers, and **collapses** into a pool of goo; hyphae reabsorb it slowly. Cut to black.
Rite half: tendrils rise around the echo and **leak spores** that gather and re-form the daemon over ~60 ticks; the daemon drifts down into the echo; the player **calmly re-manifests** (the echo's shadow drains back to the player's skin), standing where the echo stood.

## Design
- Each scene is a `BossScene` with `CinematicShot`s and tracks; the server plays it with `BossScenes.play(anchor, SCENE_x)` where the anchor is the echo (rite half) or the player (Gardens half). The server sequences the real state changes (stilling, teleport, control swap, Mind Broken) at fixed ticks in `PossessionService`, mirrored by the scene's tracks so the visuals land on the same ticks; a skipped scene jumps the server sequence to its end.
- World-space effects (tendrils, spores, shatter) are cues through `WorldEffectDispatcher` with `AnimationTime`; nothing allocates render types per frame.
- The echo entity gains a `manifest` synced value (0..1) that its renderer uses for the shadow-to-skin blend in both directions.
- Rules class `PossessionSceneRules` (pure): the tick table for each scene (when the teleport, the control swap, the Mind Broken application and the skip-jump happen), tested.
- Sounds: the rite's existing daemon and tendril sounds, a wet collapse, a spore hiss, the monolith shatter; no new vanilla sound reuse where a Hemomancy sound fits.

## Tasks
1. `PossessionSceneRules` + tests.
2. Echo `manifest` value and renderer blend.
3. The four scenes in `client/render/cinematic/scene/` and the catalog; the scene ids in `BossPresentationIds`.
4. `PossessionService` sequencing (begin/finish split into staged steps driven by a server-side tick table); the scene facsimile for arrival.
5. Effects cues: departure tendrils, injection, burst/reabsorb, eruption/blast, leak/re-form.
6. Proof: the possession review client (Track 2) records all four scenes in at least two forms.

## Acceptance
- A possession never pops: the player sees departure and arrival; every return plays its variant.
- Skipping a scene leaves the game state identical to letting it play.
- Logout/crash recovery (`PossessionEvents.onLogin`) still returns cleanly without a scene.
