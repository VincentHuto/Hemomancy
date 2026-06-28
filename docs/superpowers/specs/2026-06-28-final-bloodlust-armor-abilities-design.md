# Final Bloodlust Armor Abilities Design

## Goal

Final Bloodlust armor sets add full-set active and passive abilities that feel like armor-born blood manipulations. Triggered abilities are selected through the existing manipulation radial menu, not through a separate keybind or item.

## Scope

This design covers the reusable armor ability system, radial menu integration, server-authoritative activation packet, and first three registered abilities:

- `hemomancy:edacious_bloodburst`
- `hemomancy:sheolic_bastion_stance`
- `hemomancy:phantasmal_step`

The design also covers the passive and reactive identities required for the three final Bloodlust sets where they are tied to the same full-set detection.

## Architecture

Add a reusable `ArmorSetAbility` interface and `ArmorSetAbilityRegistry` under common gameplay code. Each ability defines its id, display name, icon, required armor pieces, availability rules, optional cooldown, optional blood cost, and server activation handler.

The registry owns lookup and active ability selection. It determines whether the player is wearing a complete registered set and returns the first matching ability. Full-set detection is item-specific, not material-based, because all Bloodlust variants share the `BLOODLUST` armor tier.

The existing radial menu adds a conditional third inner-wheel entry after Blood Absorption and Blood Projection. The entry is an `ItemStackRadialMenuItem` that renders the display icon returned by the active armor ability, normally the equipped set helmet.

## Radial Flow

When `RadialChooseManipScreen` rebuilds its menu:

1. It adds Blood Absorption if available.
2. It adds Blood Projection if available.
3. It asks `ArmorSetAbilityRegistry.getActiveAbility(player)` for an active full-set ability.
4. If no ability is active, no armor wedge is added.
5. If an ability is active, the inner wheel receives a third item showing the ability icon and display name.
6. Clicking the armor wedge sends `ActivateArmorSetAbilityC2SPacket` with only the ability id.

The client uses registry display data only for presentation. It does not decide whether activation is valid.

## Server Activation

`ActivateArmorSetAbilityC2SPacket` sends a single `ResourceLocation` ability id.

Server handling:

1. Look up the ability id in `ArmorSetAbilityRegistry`.
2. Confirm the player still wears the complete required armor set.
3. Confirm the ability-specific validation predicate passes.
4. Confirm the player has enough blood if the ability has a blood cost.
5. Confirm cooldown has expired if the ability has a cooldown.
6. Drain blood, set cooldown, and run the ability activation handler.

Invalid requests fail silently or send a short action-bar message. The server remains authoritative for all effects.

## Registered Abilities

### Edacious Bloodburst

Required set:

- `edacious_blood_lust_helm`
- `edacious_blood_lust_chest`
- `edacious_blood_lust_legs`
- `edacious_blood_lust_boots`

Display icon: `edacious_blood_lust_helm`

Activation: spawn a radial burst of blood needles from the player. Needles apply Blood Loss, Hunger, and Wither and deal moderate direct damage. The ability has a cooldown and blood cost.

Passive: while the full set is worn, grant slow creative-style flight. Flight cleanup must preserve creative and spectator flight.

### Sheolic Bastion Stance

Required set:

- `sheolic_blood_lust_helm`
- `sheolic_blood_lust_chest`
- `sheolic_blood_lust_legs`
- `sheolic_blood_lust_boots`

Display icon: `sheolic_blood_lust_helm`

Activation: toggle a timed Bastion Stance. While active, the player is rooted, cannot jump or fly, has movement suppressed, and all incoming damage is negated. Activating again cancels the stance.

Passive and reactive behavior: full set grants fall damage immunity, fire/lava damage immunity, and persistent Fire Resistance. When damaged by an attacker, it applies Crimson Retribution by dealing magic fire-themed damage, spawning crimson spore particles, and placing existing `crimson_flames` at the attacker's block position when that position is replaceable.

### Phantasmal Step

Required set:

- `phantasmal_blood_lust_helm`
- `phantasmal_blood_lust_chest`
- `phantasmal_blood_lust_legs`
- `phantasmal_blood_lust_boots`

Display icon: `phantasmal_blood_lust_helm`

Activation: teleport the player to a visible target location using the existing Umbral Step raycast approach, without light-level or environmental restrictions. It has no ability cooldown and no blood cost, but the server still validates range, line of sight, and safe landing space.

Reactive behavior: when struck, a high-chance response blinds and outlines the attacker, then teleports the attacker roughly eight blocks away from the player if a safe displacement position exists.

## UI State

The first implementation uses the existing radial item background tinting to show unavailable/cooldown state. Ready abilities use the normal inner wedge. Cooldown or insufficient blood renders a muted/red-tinted wedge and keeps the tooltip/central text informative.

Cooldown arcs are out of scope for the first implementation. Cooldown state uses a clear unavailable tint and central text so it fits the current radial infrastructure without a larger UI rewrite.

## Testing

Tests should cover:

- Registry returns no ability for partial sets.
- Registry returns the correct ability for each full final Bloodlust set.
- Ability ids are stable and match the specified `hemomancy:*` ids.
- Packet activation rejects unknown ids and validates the full set server-side.
- Blood/cooldown rules are applied before activation.
- Radial integration uses an item-stack icon for the armor ability wedge when a full set is equipped.

Existing resource tests for final armor recipes and the grinning mask rename remain in scope for final verification.

## Documentation

Update `docs/HEMOMANCY_REFERENCE.md` with the final set ability summary, radial activation behavior, and full-set requirement before considering the feature complete.
