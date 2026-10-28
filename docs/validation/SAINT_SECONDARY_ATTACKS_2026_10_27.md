# Second Saint attack pass

Two secondary moves per Saint now alternate alongside the first power pass.
Each cast snapshots the Saint's position and the target's position. Warning
geometry and server hit checks share `SaintAttackPattern` footprints; moving
after the warning does not move the attack. All attacks exclude dead, Creative,
spectator and allied players and require line of sight at selection and impact.

| Saint | Move | Warning / active / recovery ticks | Behavior and counterplay |
|---|---|---|---|
| Hemorath | Iron Rake | 28 / 12 / 20 | Three parallel 14-block lanes, half-width 0.5, spaced two blocks apart. Bone-rooted iron spikes deal six magic damage and knockback. Step into the gaps. |
| Hemorath | Marrow Rupture | 32 / 24 / 24 | A blood wave travels outward to ten blocks, with a 0.8-block contact band. Four damage and knockback; jump above 0.9 blocks or leave the outer warning. |
| Seraphae | Nerve Lance | 24 / 12 / 20 | A committed 14-block line, half-width 0.65, deals four damage with 40 ticks of Weakness and 15 ticks of Slowness III. White axons and gold branches follow the marked line. Sidestep before release. |
| Seraphae | Regrowth Knot | 36 / 60 / 20 | A 2.75-block circle at the committed target pulses every 20 active ticks. Each pulse heals one missing health, or deals three damage at full health; either case induces Weakness and a brief slow. Leave the rooted light. |
| Putriciel | Spore Garden | 32 / 80 / 20 | Three two-block fungal colonies grow at the committed target and offsets (+3,+2), (-3,+2). Pulses every 20 active ticks deal two damage and 40 ticks of Wither. Leave the marked colonies. No fire. |
| Putriciel | Censer Sweep | 26 / 20 / 20 | A rot lash sweeps through a fixed 120-degree fan from one to eight blocks away. Five damage and 40 ticks of Wither, once per player per cast. Its contact band stays inside the warned fan. Move behind it or leave the fan. |
| Velorum | Frozen Advance | 34 / 36 / 24 | Three narrow fronts advance down ten-block lanes, half-width 0.45 and spacing two blocks. Four damage and 30 ticks of Slowness III. Use the gaps or jump above 0.9 blocks. |
| Velorum | Shadow Cross | 24 / 16 / 22 | Two perpendicular shadow cuts cross at the committed target. Each arm extends five blocks with half-width 0.65. Four damage and 40 ticks of Darkness. Escape diagonally. |

Non-pulsing moves hit each player at most once per cast. Pulsing moves can hit
once per scheduled pulse. Player feet must be within 2.5 vertical blocks of the
marked floor, except the two jumpable waves. There is an initial 140-tick delay,
100 ticks between completed casts, and an 80-tick reset after a primary phase
interrupts secondary casting. Interrupted moves are retried rather than skipped.

Seraphae casts these moves only while Stable; Putriciel cancels them during
absolution; Velorum cancels them when concentration breaks. Rising, terminal
overload, trial completion and removal cannot leave a damaging queued cast.
Cancellation retires the keyed client cue immediately. The two additional synced
entity-data channels use protocol 16, requiring matching client and server builds.

The models blend independent windup, release and recovery over existing attack
poses. The shared renderer freezes these attached cues at their committed origin;
other attached manipulation effects continue to follow their actors. Iron uses
Ferric geometry, marrow uses Animus, nerves use Lux/Ductilis, colonies and censers
use Putriciel's rot material, and the cold cuts use rime and Umbra. No terrain is
replaced, and no lasting world hazard entities or player data are added.

## Verification

Timing, safe gaps, committed aim, boundary containment, animation envelopes and
finite bounded meshes have focused JVM coverage. The ten loaded power tests
include both moves landing for each Saint, refusal during warning/recovery, a
real dodged lance, cancellation on a fresh rise, and immediate cancellation when
Hemorath overloads. The overload regression failed before the fix and passed after it. Native reviews now require
both secondary moves to reach their active phase as well as their earlier checks.
Final verification on 2026-10-27:

- `test jar compileGameTestJava`: successful, 4,343 JVM tests, zero failures/errors/skips.
- `runSaintPowerGameTestServer`: all ten required loaded tests passed.
- All four native fight reports passed, including both secondary attacks per Saint,
  previous primary-power checks, and encounter endings.
- Four model reconciliation checks passed. The packaged eight Saint textures and
  four shader resources match the authored source bytes.
- Read-only secondary-attack code review found no further actionable defects.
- `git diff --check` passed. Liber JSON and all eight warning translations validate.

Native Hemorath staging now waits for chunk/entity loading and holds its stage
chunk for the review, restoring its previous forced status afterward. This avoids
scripted actors unloading during the camera's travel between review stages.
Representative captured frames were inspected for the rake, marrow ring, nerve
lance, regrowth circle, fungal colonies, censer sweep and frozen advance.

The client fixtures use scripted cameras and targets with Minimal particles.
They exercise the real entity state machines and renderer, while the loaded-server
pair tests isolate secondary casting from primary attacks to measure hit timing.
These checks do not establish natural Survival balance, multiplayer acceptance,
or the complete alpha GameTest gate. Existing primary attacks can overlap these
secondary casts outside the phase interruptions described above.
