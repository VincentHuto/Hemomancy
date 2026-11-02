# Silent Archon Incorporeality Implementation Plan

1. Add source/resource coverage for the new registered ability and passive handler.
2. Register `hemomancy:silent_slipping` in `ArmorSetAbilityRegistry` with the full Silent Archon set and helmet icon.
3. Add `SilentArchonArmorAbilityHandler` for:
   - Silent Slipping activation, state, ticking, cancellation, and safe exit.
   - Incoming physical damage negation.
   - Outgoing mundane physical damage suppression.
   - Living weapon and Hemomancy projectile exceptions.
4. Update Silent Archon tooltip/lang and `docs/HEMOMANCY_REFERENCE.md`.
5. Run the focused source tests and `./gradlew.bat build`.
