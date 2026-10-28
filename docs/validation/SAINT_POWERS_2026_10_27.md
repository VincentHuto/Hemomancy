# Saint attack and power pass

The existing collapse, pulse, fracture, nova, veil, silence and melee animations
remain layered with the new powers. Windups and recovery use synced countdowns
and the existing replay clocks. No new payload layout or form ordinal is needed.
Protocol 15 requires matching builds because Hemorath and Velorum now synchronize
additional entity-data channels for the new powers.

## Hemorath

Within six blocks and line of sight, eligible players lose 20 active blood every
20 ticks. Every 100 ticks a larger 12-block siphon releases after a 24-tick
warning, taking 60 blood instead of the smaller aura payment on that tick.
Inactive or empty blood suffers 1 aura damage or 2 siphon damage. Creative,
spectator, dead and allied players are excluded through the shared targeting
helper. Only blood actually paid enters his absorbed-blood meter, once; this
hazard payment does not also create spend debt. The usual phase-two absorption
multiplier remains. Stored blood leaks at 0.25 per tick outside terminal overload.
Iron growths swell as the claw reaches; the outer warning contracts into intake
strands when the siphon releases. Ordinary wound/casting debt remains available.

## Seraphae

Stable transfusions release every 80 ticks, with a 24-tick warning and a 10-block
line-of-sight reach. They heal up to two missing health. With no wound left to
close, a first full-health transfusion warns of excess growth; the next causes
three magic damage, 20 ticks of Slowness IV and 40 ticks of Weakness. Leaving
reach clears that encounter-local exposure. Healing a wound clears it too.
Dispersed nerve impulses replace random arena-wide wisp damage: every 40 ticks,
eligible players in the same reach convulse. White transfusion threads branch
into gold nerve impulses. Containment, fragments, anchors and the win condition
remain intact.

## Putriciel

Rot novas apply their existing damage and Wither without ignition. During the
80-tick absolution window novas are suspended and melee is refused. After a
20-tick warning, cleansing pulses at remaining ticks 60, 40 and 20. Eligible
players within three blocks and line of sight take two magic damage and two
seconds of crimson ignition. This close-range hazard gives the open palm and
burning colonies a mechanical role while preserving five-hit absolution victory.
Colony contraction, body recoil, flame tongues and a ground warning share the
existing window countdown. The dedicated rot material remains olive and bone.

## Velorum

Eligible players within 12 blocks and line of sight accumulate one point of
stillness per tick, up to 100. Moving more than 0.05 blocks per tick removes two
instead. At 80, the field applies ten ticks of Slowness VII once each second and
wakes sleepers. Moving or leaving the field provides an escape between pulses.
An accepted player hit opens a 60-tick interruption, reducing pressure by five
per tick and suspending nova, veil and silence releases. The opening has a
180-tick cooldown; subsequent hits keep the earlier brief resistance rhythm.
Pressure is shown in an action-bar cue; the field uses static dark seams, cold
threads and a bowed recovery pose. Exposure is local to this encounter and is
discarded when a player leaves its valid reach. No durable player data is added.

Dedicated chambers and a rescuable frozen congregation are separate environmental
work; this pass changes the four Saints' attacks and attached presentation.

## Verification

- Full JVM suite: 4,335 tests passed, zero failures/errors. This includes the
  four new power-rule cases and expanded bounded-mesh checks for the new cues.
- Focused mod-loaded server: all 4 `saint_power_validation` GameTests passed.
  These exercise actual blood payment/absorption and creative refusal, wound
  healing and convulsion plus containment victory, rot versus cleansing fire
  with its warning boundary, and stillness release/repeated-hit cooldown behavior.
- The loaded fixture explicitly clears sight lines and removes login damage grace
  before testing; it asserts valid eligibility and line of sight before proceeding.
- Generated models and embedded atlases passed the existing model tests and the
  four generator reconciliation checks.
- Native Hemorath review: all 8 scenes passed with the new siphon layer.
- Native Seraphae review: healing, convulsion, dispersal, condensation and final
  containment all passed during a natural state cycle. The fixture disables
  natural regeneration, so its healing check comes from the actual transfusion.
- Native Putriciel review: all 9 scenes passed. Rot is explicitly checked for
  absence of ignition; the close cleansing pulse is observed during the opening.
  Novas are rejected if they overlap absolution.
- Native Velorum review: all 8 scenes passed, including the new interruption,
  existing paid silence and concurrent casts after the opening ends.
- Final protocol/registration/power-rule checks and JAR assembly passed after
  moving to protocol 15. Eight Saint textures and four shader resources in the
  assembled JAR match their authored bytes.

Evidence: `build/saint-powers-verification.log`, the
`build/saint-powers-<saint>-client.log` files and the JSON reports/screenshots under
`build/boss-presentation-review/`. Native review fixtures are assisted and do not
establish natural Survival balance or multiplayer acceptance. The complete alpha
GameTest gate has not been rerun for this pass.
