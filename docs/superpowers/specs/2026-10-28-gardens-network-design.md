# Gardens Network & Immune Response (Section 2b) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry …_V2.md`, "Shared systems, split outcomes" (mycelium network, terrain) and "Threat that remains" (antibodies). Builds on Sections 1 (spore load, `FungalGardensPlaces`) and 2a.

## Goal

The Gardens notice who walks and casts in them. An Apotheos is read as part of the organism; anyone else is a host the network hunts. An overcasting Apotheos is read as foreign.

## Decisions

### D1. Network alarm (footsteps)
- **Who:** a living, non-creative, non-spectator, non-Apotheos player in the Gardens **dimension**, walking on a `hemomancy:fungal_network_ground` block. Sneaking is silent.
- **Counting:** horizontal distance walked on fungal ground accumulates. At **8 blocks**, with at least **100 ticks** since the last alarm, the network alarms and the count resets.
- **Effect:** every hostile Hemomancy mob (`Monster`, `hemomancy` namespace) within **24 blocks** that has no target turns on the player.
- **Signal:** a ring of crimson spore particles at the player's feet, and the action-bar line "The ground carries your footsteps to something."
- State lives in a UUID-keyed session map, cleared and removed at logout through `TransientServerStateEvents`.

### D2. Apotheos sense
- On an Apotheos's own client, in the Gardens dimension, every other living entity standing on fungal network ground within 32 blocks appears outlined.
- Done through a client mixin on `Minecraft#shouldEntityAppearGlowing`. Other players see nothing.

### D3. Puffballs burst
- `puffball_fungus` reacts when a non-creative player walks into it **in the Gardens dimension**. Elsewhere, the Overworld biome included (where D2 players survey on foot), it stays decorative. (Narrowed in the review fix pass.)
- **Apotheos:** the puffball is absorbed, giving +3 spore load and a pale spore puff.
- **Anyone else:** it bursts, giving Nausea for 120 ticks, Weakness for 100 ticks and a crimson spore cloud.
- In both cases the block is removed.

### D4. Immune suspicion and antibodies
- **Suspicion** is a per-player session value, kept only while in the Gardens dimension. It rises in two ways:
  - **Autonomy:** each credited manipulation cast in the Gardens adds 2 (1 for an Apotheos).
  - **Blood:** each second, a non-Apotheos with blood above 75% of maximum gains 1/4 point. The Apotheos's blood is the hive's own.
- Suspicion decays by 1 every 10 seconds. Leaving the dimension clears it.
- **Response:** at **12** suspicion, the network summons **2 antibodies** (plus 1 per further 6 points, at most 4) within 6 blocks of the player and resets suspicion to 0. The next summon needs **600 ticks** to pass.
- **Antibody** (`hemomancy:antibody`, MONSTER, 0.6×0.9):
  - a small flying Y-shaped protein: 10 health, 4 attack, flying speed 0.6;
  - it hunts only the player it was summoned for, and dissolves after 1,200 ticks or when that player dies or leaves;
  - no drops (an empty loot table), no natural spawning, and a spawn egg for testing;
  - a hand-written model (stem, two arms, binding tips) with a generated 32×32 texture.

## Out of scope (2c)
Rendering pools, cap bridges, gills, the hunter-fungi mob.
