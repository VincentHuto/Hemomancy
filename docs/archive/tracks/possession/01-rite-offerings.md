# Track 1 — Rite offerings through the braziers

**Goal:** the Rite of Possession is a special case of the brazier system. Its medium is a **Sanguine Quintessence** seated in the Cardinal Focus on a **Qliphoth grand** floor. The standard brazier-signature match is bypassed for this rite: instead, each brazier is read for one of the rite's **optional** offerings, and whatever is found tunes the possession. No loose items.

## Current state
- `possession_rite.json`: purpose `bond`, floor `communion_lesser`, no medium, `brazier_signature: []`, bloodCost 0.
- `PossessionService.begin` scans `ItemEntity`s inside the rite bounds for Mnemonic Ambergris, a Hematic Iron Block, a Totem of Undying and Adipocere (`consumeOffering`) and consumes one of each found.
- The shared matcher (`CardinalRiteStationMatcher.matchedBraziers`) demands `offered.size() == sum(count)` with an exact assignment, so an optional offering in a brazier fails every rite's match. That is why the first pass used loose items.
- The Focus medium is matched through `CardinalRiteMediumRules.matches(required, seated)` and consumed at completion (`CardinalRiteCompletion.consumeRiteMedium`).
- `qliphoth_grand` floor exists (`cardinal_rite_floor/qliphoth_grand.json`, style `qliphoth`, purpose `world`); `CardinalRitePurpose.violation` requires a bond rite to sit on a communion floor unless the rite is `"special": true` (only `apotheos_rite`, `bloom_of_qliphoth`, `ancestral_communion` may set it today; `CardinalRitePurposeDataTest` pins the set).

## Target
1. **Recipe.** `possession_rite.json`: `"floor": "hemomancy:qliphoth_grand"`, `"special": true`, `"medium": {"item": "hemomancy:sanguine_quintessence"}` (consumed on success), purpose stays `bond`. Add it to the pinned `SPECIAL` set and to `SpecialFloorDataTest` if it checks special floors declare their purpose.
2. **Optional brazier offerings.** A new recipe field `"optional_offerings": [{"ingredient": …, "effect": "concentration_regen" | "shell" | "totem" | "max_concentration"}]`. The station matcher gets one hook: when a recipe declares optional offerings, the brazier stage does not require an exact signature; every lit brazier holding one of the listed ingredients is captured as an optional match and the rest are ignored (a brazier holding something unrelated is simply not counted, never a refusal). Keep the existing exact-signature path for every other rite; the guardrail is that `CardinalRiteStationMatcherTest` (or its nearest existing test) shows an unchanged verdict for a rite without the field.
3. **Capture and consume.** The optional matches ride the `ActiveCardinalRite` offering itinerary (`RiteOffering` with `consume = true`) so the existing brazier draw-down during the ceremony shows them burning, and `CardinalRiteCompletionEffects`' possession entry reads `ctx.rite().getAbsorbedOfferings()` to decide regen / shell / totem / wax. No scan of the floor.
4. **The Qliphoth grand floor as the place.** The surrogate must stand inside the floor's footprint (as now); the player's echo appears where the caster stood at completion; the medium's consumption is the visual beat Track 3's departure starts from.
5. **Teaching.** `hemomancy.rite.possession_rite.desc`, the Liber page for the Rites & Crafts map (`HarbingerRecipeMapDefinitions` icon stays the Thread until Track 2 replaces it with a Sanguine Quintessence), and the Vicar's line if one mentions the rite.

## Tasks
1. Recipe field + serializer + `CardinalRiteRecipe.getOptionalOfferings()`; JSON; purpose/special test updates.
2. Matcher hook and its tests (RED first: a rite with optional offerings and two of four braziers lit must match; a rite without the field must refuse an extra brazier as before).
3. `PossessionService.begin(level, caster, surrogate, absorbed)` takes the absorbed offerings; delete `consumeOffering` and the loose-item scan.
4. Lang, Liber, docs (HR §5.6e).
5. Client proof: a possession started with Ambergris + Adipocere in two braziers shows regen and +3 max on the HUD.

## Acceptance
- Items dropped on the floor do nothing.
- The rite refuses without a Sanguine Quintessence in the Focus and without a Qliphoth grand floor.
- Every other rite's brazier verdicts are unchanged (its tests prove it).
