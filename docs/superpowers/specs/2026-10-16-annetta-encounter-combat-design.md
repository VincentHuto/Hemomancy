# Annetta encounter: combat, choreography and dialogue design

Date: 2026-10-16. These are sub-projects 2 (the Harbinger route) and 3 (the Unstained route) of the Annetta rework.
The user asked for the full animation and in-game combat and initiation code end to end, with dialogue, starting
combat, purifying and killing Annetta, and the phase and combat animations, "based on our previous discussion"
without further questions. Everything below is either the user's words from that discussion or marked as
Claude's call.

## Harbinger route (the user's description)

1. **Start.** A Harbinger approaches Annetta holding a Tooth Pecks specimen jar. The jar shatters, the Tooth Peck
   latches onto her neck as a tick, and she screams. Phase 1 begins.
2. **Phase 1.**
   - She looks almost exactly as she did cowering, but fights quickly and frantically.
   - She uses her Absolution Dagger and Still arts for offence and for movement.
   - She defends with her Unstained Shield.
   - The tick on her neck swells as the fight goes on.
   - When she is hit or moves around, a mix of yellow-orange infected blood and white-blue glowing Unstained
     humor is knocked out of her, forming lasting pools on the ground.
3. **The break.** Once the player has lasted at least 2 minutes and she has taken at least 50 HP of damage:
   - She falters and staggers, and drops her shield.
   - She drives her dagger into the tick's swollen body. It bursts in a violent deluge of blood, spore and
     humor, and her head disappears with it.
   - After a second or two she drops her dagger and falls forward onto her stomach.
   - A pool of blood forms around her and grows, creeping up her fallen body over about 10 seconds. It slowly
     reanimates her, and she gets back up.
   - A writhing mass of tendrils and blood vessels bursts from her back as crude wings, and a ball of writhing
     thorns and tentacles sprouts from her neck, roughly head-shaped, with 3 glowing yellow eyes.
   - As she gets up, a blue-and-red intertwined Sanguis Lancea is pulled from the bloody pool and equipped in
     her hand.
4. **Phase 2.** She flies around firing bloody fungus-and-blood projectiles and swoops down at the player with
   her spear. She sometimes lands for melee combos, and uses her wings both as a shield and to blast the player
   back with a sickly wind.
5. **End.** Defeating the Priestess keeps the existing rewards: the Annetta's Sanguis Lancea drop, the scrap, and
   the founder's severance unlock.

## Unstained route

1. **Start.** An Unstained player with Clarity approaches holding a Draught of Still Mercy. Dialogue offers it to
   her. She drinks.
2. **The purge** (Claude's call on timing). She convulses, and the red drains from her hair into white while her
   eyes go from gold to blue. The infection tears itself out of her back and rises as the Latent Infection.
3. **Ally phase.** She fights beside the player with only her dagger, weakened. She heals the player (already
   implemented, now with the healing pose). She strikes the infection with Still arts and her dagger.
4. **The Latent Infection** is a separate boss:
   - It slithers.
   - It lashes with the coiled-vessel whip arm, which uncoils to reach far.
   - It thrusts with the lance held by its clawed arms.
   - It rakes with the claws.
   - It rears back for the existing Infection Bloom.
   - It sends roots erupting under the player.
   - It sometimes turns on Annetta.
5. **End.** The infection collapses and wilts. Annetta becomes RESOLVED, and the existing drops stay. Dialogue
   follows.

## Dialogue (Claude's call; the tone follows LORE_REFERENCE §11)

- **Cowering:** the existing tree.
- **Harbinger Phase 1:** she never understands what is happening to her. She cries out in fragments:
  disbelief, prayer to the Lady, apology. These go to nearby players as italic chat.
- **The break:** a last, half-finished line before the stab.
- **The Priestess:** never speaks. Event lines describe her.
- **Unstained purge:** a short exchange. The player offers the Draught, she hesitates and then drinks. There are
  ally callouts during the fight, and a resolution tree afterwards (thanks, guilt, and what she will do now).

## Implementation calls (Claude's)

- **Synced action and client timing.** Each entity syncs an `action` id. The client stamps its own start tick
  when the id changes, so animations run from local elapsed time with partial ticks. The server keeps the
  authoritative timers.
- **Pure rules classes.** Thresholds, timelines and attack choice live in pure rules classes with JVM tests.
- **Ground pools.** A lightweight `AnnettaPoolEntity` holds the lasting pools. It comes in three kinds:
  - **Infected blood:** Blood Loss to anyone but her.
  - **Humor:** slows and weakens blood-active players.
  - **Reanimation blood:** grows under her during the break.

  Pools are capped per encounter and rendered as flat, animated discs.
- **Early-death guard.** Annetta cannot die before the break. Damage that would kill her leaves her at 1 HP until
  both break conditions are met.
- **Rising Priestess.** The Priestess spawns lying face-down where Annetta fell. It plays an emerge animation:
  push up, kneel, rise, with the tendril wings and head growing in. The lancea appears in her hand when the
  pool releases it. She is invulnerable with no AI until she stands.
- **Flight.** The Priestess flies with a custom steering controller (no gravity while airborne), switching
  between hover volleys, swoops, landed melee combos, wing shield and wind blast. Her wing pose follows the
  entity's synced state.
- **Particles.** All effects use Hemomancy particles. No vanilla particle clouds.
