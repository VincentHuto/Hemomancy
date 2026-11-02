# Track 2 — The possessed radial wheel

**Goal:** no Thread of Will. While possessing, the manipulation radial wheel becomes the possession wheel: the inner ring holds the visit's controls (leave the Gardens, and any other visit-level action), the outer ring holds the body's abilities (toggle or trigger), and the centre shows Concentration, a small render of the surrogate body and its name.

## Current state
- `ThreadOfWillItem` (slot 0 during possession): right-click uses `PossessionAbilities.use(form)`; sneak-hold 40 ticks returns cleanly; it cannot be dropped or kept.
- The radial wheel: `client/screen/manips/RadialChooseManipScreen` over `client/screen/radial/GenericRadialMenu` (inner ring `addInner`, outer ring `add`, `setRadii`, central text through `setCentralText` / `RadialMenuItem.getCentralText`, the veiny border). Items are `BlitRadialMenuItem` / `ItemStackRadialMenuItem` / `MuscleMemoryRadialMenuItem`. The key that opens it is in `client/registry/KeyMappings`; selection goes to the server through the existing manipulation payloads.
- `PossessionEvents` already cancels block use and entity use while possessed; `PossessionAbilities` holds the per-form gifts with cooldowns on the Thread item.

## Target
- **Wheel swap.** When `HemoCapabilityAccess.isPossessing(player)`, the wheel key opens `PossessedRadialScreen` instead of the manipulation wheel. Manipulations, Absorption, Projection and the other loadout entries are not shown and cannot be cast (the server refuses them too: `KnownManipulationEvents` / the cast payload handler check possession, with a one-line refusal).
- **Inner ring:** *Return* (begins the clean-return hold: the wheel closes and a 40-tick channel runs with the Concentration pips pulsing; moving cancels it), *Still* (hold position; useful for the Repose Chapel later), and a slot for Track 3's *Linger* if the cinematics need it.
- **Outer ring:** one item per ability of the form: bat *Echolocate*, enderman *Step*, blaze *Spit*, golem *Slam*, horse *Leap*, wolf *Lunge*; toggles for spider *Climb* (on/off) and blaze/bat *Hover* (flight on/off). Items grey out on cooldown with the cooldown arc drawn on the slice (reuse the muscle-memory item's cooldown drawing).
- **Centre:** the Concentration count (`current / max`, shell pips in pale), a 3D render of the surrogate's facsimile body turning slowly (an `InventoryScreen.renderEntityInInventory`-style draw of Track 4's cached body), and the form's name (`surrogate.hemomancy.<form>`).
- **Server side.** A new serverbound payload `PossessionActionPacket(action)` validated by `ServerboundGuards` (possessing, action allowed for the form, cooldown clear) that calls `PossessionAbilities.use` or `PossessionService.returnClean`. Cooldowns move from `ItemCooldowns` into the `Possession` attachment (`abilityReadyAt` per ability), synced in `PacketSyncPossession`.
- **Rules.** `PossessionWheelRules` (pure, tested): which actions a form offers, toggle vs trigger, cooldown ticks, which manipulation-side actions are refused while possessed.

## Tasks
1. Delete `ThreadOfWillItem`, its assets, lang, the slot-0 stash logic in `PossessionService` (`stash`/`takeStash` go too) and the Thread checks in `PossessionEvents`; update the recipe-map icon (Sanguine Quintessence) and HR §5.6e.
2. `PossessionWheelRules` + tests (RED first).
3. The payload, guard, registry entry, protocol bump; cooldowns in the attachment.
4. `PossessedRadialScreen` + items; the key swap in `ClientInputEvents`; the manipulation-cast refusal server-side.
5. `possessionReviewClient` row in `gradle/review-clients.gradle` plus a driver that starts a possession in each form and screenshots the wheel; this scaffold is shared by Tracks 3 and 4.

## Acceptance
- No Thread of Will exists; a possessed player's hotbar is whatever the form allows (empty for mob forms, the inventory for the donkey, the stand's gear for the armor stand).
- The wheel shows only possession entries while possessed, with Concentration, the body and the name in the centre.
- Every ability and the return work from the wheel, with cooldowns visible.
