# Recipes, serializers, and machine execution

## Actual paths

| Layer | Current owner | What varies |
| --- | --- | --- |
| Type and serializer IDs | [RecipeInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/RecipeInit.java) | Some special crafting recipes have a serializer without a separate machine recipe type. Keep the existing IDs. |
| Recipe schema | [DistillationRecipe](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/DistillationRecipe.java), [CardinalRiteRecipe](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/CardinalRiteRecipe.java), [MemoryWeavingRecipe](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/MemoryWeavingRecipe.java), others | Inputs, outputs, costs, spatial patterns, and side effects are genuinely different. |
| JSON and network codecs | `common/recipe/serializer/`, including [DistillationRecipeSerializer](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/DistillationRecipeSerializer.java), [ScarRecipeSerializer](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/ScarRecipeSerializer.java), and [RecipeResultStackParser](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/RecipeResultStackParser.java) | `RecordCodecBuilder` and custom `MapCodec` coexist. The result parser already accepts both `id` and legacy `item` forms for several recipes. |
| Runtime resource | [recipe data](../../src/main/resources/data/hemomancy/recipe) | Machine families have their own folders: distillation, advanced brewing, incubator, memory weaving, scar, cardinal rite, and more. |
| Execution | Block entities, [BloodInfusionManager](../../src/main/java/com/vincenthuto/hemomancy/common/event/BloodInfusionManager.java), rite handlers | Lookup, input checks, cost payment, output insertion, XP/progression, and visual feedback happen in different places. |

## Shared codec plumbing

`RecipeCodecJson` bridges the seven legacy JSON-backed serializers while retaining the caller's `RegistryOps` lookup. Nested ingredient and item-component codecs receive that context, including when the outer input is NBT. `RecipeResultStackParser` retains object `id`, legacy object `item`, and string result plus root `count` formats.

Encoders feed nested `DataResult` values directly into record/list builders, so invalid ingredients, results, or registry-backed components fail the recipe encode instead of disappearing. Cardinal Rites still allow no result. Blood Structure `heldItem` remains an item-ID string. Family-specific costs, aliases, patterns, validation, and network formats remain owned by their serializers. The existing partial spatial JSON encoders still omit pattern/ceremony data; complete spatial synchronization remains in their stream codecs.

`runRecipeCodecGameTestServer` covers all seven families and is included in `alphaCheck` through the central suite catalog.

## Highest-value overlap: two distillation machines

[GhastlyAlembicBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java) and [PallidRetortBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/unstained/crafting/PallidRetortBlockEntity.java) both scan the distillation recipe type, match ingredients/catalysts, check output capacity, burn, track `RecipesUsed`, save it in NBT, and award XP. Their [menus](../../src/main/java/com/vincenthuto/hemomancy/common/menu/tile/crafting/GhastlyAlembicMenu.java) and [Retort menu](../../src/main/java/com/vincenthuto/hemomancy/common/menu/tile/crafting/PallidRetortMenu.java) also share much of the slot geometry. The Alembic has blood and advanced-brew behavior; the Retort has white-humor behavior. Those differences must remain visible.

The [Stage 3 transaction audit](stage-3-distillation/README.md) compares lookup, capacity, commit, saved ledger, XP, and extraction in detail. Extract **ordinary output capacity** first: the two `canBurn` implementations are identical, while the two `burn` implementations differ. The Alembic uses `DistillationConsumptionRules` to consume input, catalyst, and optional blood input; the Retort shrinks only the main input. Both capacity checks currently compare result stacks with `ItemStack.isSameItem`; review component-bearing results before changing that rule. Recipe matching needs a later, separate extraction because the Alembic has a dedicated blood-input stack and advanced-brewing fallback. Keep tick timing, blood/white-humor costs, upgraded tiers, and advancement effects in each machine until their behavior is mapped.

## Other recipe families to inspect

- [MorphlingIncubatorBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MorphlingIncubatorBlockEntity.java), [MycelialCrucibleBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MycelialCrucibleBlockEntity.java), [MycelialLanternBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MycelialLanternBlockEntity.java), and [SomaticLoomBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java) each call `getAllRecipesFor` or `byKey`. Check reload invalidation and deterministic choice if multiple recipes match before sharing a cache.
- [CardinalRiteRecipeSerializer](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/CardinalRiteRecipeSerializer.java) and [BloodStructureRecipeSerializer](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/BloodStructureRecipeSerializer.java) handle spatial/ingredient formats. Share only parsing primitives that have identical field semantics; preserve resource compatibility.
- [AdvancedBrewingRecipe](../../src/main/java/com/vincenthuto/hemomancy/common/brewing/AdvancedBrewingRecipe.java) is paired with [BrewingResolver](../../src/main/java/com/vincenthuto/hemomancy/common/brewing/BrewingResolver.java), [BrewingMatch](../../src/main/java/com/vincenthuto/hemomancy/common/brewing/BrewingMatch.java), and [AdvancedBrewingReload](../../src/main/java/com/vincenthuto/hemomancy/common/brewing/AdvancedBrewingReload.java). Audit its data reload and vanilla stand integration as a separate pipeline.

## Migration order and invariants

1. Make a recipe-family sheet: type ID, serializer ID, JSON folder, input, output, cost, runtime executor, menu, JEI surface, and discovery gate.
2. Start with the Alembic/Retort ordinary result-capacity predicate; keep both saved NBT keys and recipe IDs unchanged. Audit extraction and the Retort's player-award path before moving ledger/XP code.
3. Then normalize result-stack parsing only where the schemas promise the same legacy aliases and component support. Confirm JSON and stream codecs round-trip the same values.
4. Check crafted item count/components, catalyst consumption, fluid balance, output-full behavior, XP and progression, reload of old worlds, and recipe reload. These are gameplay outcomes, not a request to inventory test files.
