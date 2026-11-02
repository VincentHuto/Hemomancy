# Desiccant Telson Restoration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the desert scorpion mob as `hemomancy:desiccant`, add Telson as its drop, and move Aculeate Vitriol from Fargone Proboscis to Telson.

**Architecture:** Reuse the dormant scorpion entity/model/renderer path, correcting the misspelled `dessicant` API and resource ids to `desiccant`. Add data-driven spawning via a NeoForge biome modifier and keep recipe/material behavior covered by existing resource-source tests.

**Tech Stack:** Java 21, NeoForge 1.21.1, JSON datapacks/resources, plain Java source/resource tests, Gradle.

---

### Task 1: Red Tests For Desiccant And Telson Coverage

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/entity/mob/monster/TendencyMobSpawnRulesSourceTest.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/recipe/ArmorPreparationResourceTest.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/loot/EntityLootTableCoverageResourceTest.java`

- [ ] **Step 1: Update dormant/source test expectations**

Remove `dessicant` from `DORMANT_ENTITY_IDS` and add active assertions:

```java
private static final String[] ACTIVE_DESICCANT_SNIPPETS = {
        "register(\"desiccant\"",
        "DesiccantEntity::canSpawnHere",
        "event.put(EntityInit.desiccant.get(), DesiccantEntity.setAttributes().build())"
};
```

Add a method called from `main`:

```java
private static void assertDesiccantActive() throws IOException {
    String entityInit = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
    String itemInit = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
    String clientEvents = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
    String layerEvents = stripLineComments(read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
    for (String snippet : ACTIVE_DESICCANT_SNIPPETS) {
        assertContains("active desiccant source " + snippet, entityInit, snippet);
    }
    assertContains("desiccant spawn egg", itemInit, "register(\"spawn_egg_desiccant\"");
    assertContains("desiccant renderer", clientEvents, "EntityInit.desiccant.get(), DesiccantRenderer::new");
    assertContains("desiccant model layer", layerEvents, "DesiccantModel.LAYER_LOCATION");
    assertPathExists("desiccant biome modifier",
            Path.of("src/main/resources/data/hemomancy/neoforge/biome_modifier/add_desiccant.json"));
    assertPathExists("desiccant biome tag",
            Path.of("src/main/resources/data/hemomancy/tags/worldgen/biome/desiccant_spawnlist.json"));
    assertPathExists("desiccant loot table",
            Path.of("src/main/resources/data/hemomancy/loot_table/entities/desiccant.json"));
}
```

Also add helper methods:

```java
private static void assertPathExists(String label, Path path) {
    if (!Files.exists(path)) {
        throw new AssertionError(label + ": missing " + path);
    }
}

private static void assertContains(String label, String text, String expected) {
    if (!text.contains(expected)) {
        throw new AssertionError(label + ": missing " + expected);
    }
}
```

- [ ] **Step 2: Update armor preparation test expectations**

Add `telson` to item ids, lang/model/texture checks, assert the desiccant Telson drop, and change Aculeate Vitriol from Fargone Proboscis to Telson:

```java
"telson",
```

```java
assertContains("telson lang", lang, "\"item.hemomancy.telson\": \"Telson\"");
```

```java
assertContains("desiccant telson drop",
        read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/desiccant.json")),
        "\"name\": \"hemomancy:telson\"");
assertContains("fargone proboscis drop remains",
        read(RESOURCE_ROOT.resolve("data/hemomancy/loot_table/entities/fargone.json")),
        "\"name\": \"hemomancy:fargone_proboscis\"");
```

```java
assertRecipeUses("aculeate_vitriol", "hemomancy:telson");
assertRecipeDoesNotUse("aculeate_vitriol", "hemomancy:fargone_proboscis");
```

Add helper:

```java
private static void assertRecipeDoesNotUse(String recipeId, String itemId) throws IOException {
    String recipe = read(RECIPES.resolve(recipeId + ".json"));
    assertDoesNotContain(recipeId + " should not use " + itemId, recipe, "\"item\": \"" + itemId + "\"");
}

private static void assertDoesNotContain(String label, String text, String forbidden) {
    if (text.contains(forbidden)) {
        throw new AssertionError(label + ": found " + forbidden);
    }
}
```

- [ ] **Step 3: Update loot coverage expectations**

Add:

```java
new GuaranteedDrop("desiccant", "hemomancy:telson"),
```

- [ ] **Step 4: Run red tests**

Run:

```powershell
./gradlew.bat test --tests com.vincenthuto.hemomancy.common.entity.mob.monster.TendencyMobSpawnRulesSourceTest --tests com.vincenthuto.hemomancy.common.recipe.ArmorPreparationResourceTest --tests com.vincenthuto.hemomancy.common.loot.EntityLootTableCoverageResourceTest
```

Expected: FAIL because `desiccant`, `telson`, the desiccant biome modifier/tag, loot table, and recipe swap do not exist yet.

### Task 2: Restore And Rename Desiccant Code Wiring

**Files:**
- Rename: `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/DessicantEntity.java` to `src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/DesiccantEntity.java`
- Rename: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/DessicantModel.java` to `src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/DesiccantModel.java`
- Rename: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/monster/DessicantRenderer.java` to `src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/monster/DesiccantRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/SoundInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`

- [ ] **Step 1: Rename classes and references**

Rename class names and imports from `Dessicant*` to `Desiccant*`.

- [ ] **Step 2: Register active entity**

In `EntityInit`, register:

```java
public static final DeferredHolder<EntityType<?>, EntityType<DesiccantEntity>> desiccant = ENTITY_TYPES.register(
        "desiccant",
        () -> EntityType.Builder.of(DesiccantEntity::new, MobCategory.MONSTER)
                .sized(0.8F, 0.5F).build(Hemomancy.rloc("desiccant").toString()));
```

Add active spawn placement:

```java
event.register(EntityInit.desiccant.get(), SpawnPlacementTypes.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DesiccantEntity::canSpawnHere,
        RegisterSpawnPlacementsEvent.Operation.OR);
```

Add active attributes:

```java
event.put(EntityInit.desiccant.get(), DesiccantEntity.setAttributes().build());
```

- [ ] **Step 3: Register spawn egg**

In `ItemInit`, add:

```java
public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_desiccant = SPAWNEGGS.register(
        "spawn_egg_desiccant",
        () -> new DeferredSpawnEggItem(EntityInit.desiccant, 0xC2A66E, 0x8B1A1A, new Item.Properties()));
```

- [ ] **Step 4: Register client layer and renderer**

In `LayerEvents`:

```java
event.registerLayerDefinition(DesiccantModel.LAYER_LOCATION, DesiccantModel::createBodyLayer);
```

In `ClientEvents`:

```java
event.registerEntityRenderer(EntityInit.desiccant.get(), DesiccantRenderer::new);
```

- [ ] **Step 5: Expand spawn surfaces**

In `DesiccantEntity.canSpawnHere`, accept dry hot surfaces including sand, red sand, sandstone, red sandstone, terracotta, and hardened clay variants:

```java
private static boolean isDryHotSurface(BlockState state) {
    return state.is(Blocks.SAND)
            || state.is(Blocks.RED_SAND)
            || state.is(Blocks.SANDSTONE)
            || state.is(Blocks.RED_SANDSTONE)
            || state.is(Blocks.TERRACOTTA)
            || state.is(BlockTags.TERRACOTTA);
}
```

### Task 3: Add Telson Resources, Loot, Spawn Data, And Recipe Swap

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Create: `src/main/resources/assets/hemomancy/models/item/telson.json`
- Create: `src/main/resources/assets/hemomancy/textures/item/telson.png`
- Rename/copy: `src/main/resources/assets/hemomancy/textures/entity/dessicant/model_dessicant.png` to `src/main/resources/assets/hemomancy/textures/entity/desiccant/model_desiccant.png`
- Create: `src/main/resources/data/hemomancy/neoforge/biome_modifier/add_desiccant.json`
- Create: `src/main/resources/data/hemomancy/tags/worldgen/biome/desiccant_spawnlist.json`
- Create: `src/main/resources/data/hemomancy/loot_table/entities/desiccant.json`
- Modify: `src/main/resources/data/hemomancy/recipe/aculeate_vitriol.json`

- [ ] **Step 1: Add Telson item registry**

Add beside `fargone_proboscis`:

```java
public static final DeferredHolder<Item, Item> telson = BASEITEMS.register("telson",
        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
```

- [ ] **Step 2: Add Telson item assets and lang**

Model:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "hemomancy:item/telson"
  }
}
```

Lang:

```json
"item.hemomancy.telson": "Telson"
```

Texture: create `telson.png` by deriving a small stinger-and-bulb item texture from the existing Fargone Proboscis texture palette or a simple new 16x16 raster.

- [ ] **Step 3: Add material screen entry**

Add near Fargone Proboscis:

```java
list.add(new MaterialEntry("telson", "Telson",
        "A desiccant scorpion stinger and venom bulb prepared for corrosive armor baths.",
        "Materials", () -> new ItemStack(ItemInit.telson.get()),
        List.of("Dropped by Desiccants", "Used in Aculeate Vitriol")));
```

- [ ] **Step 4: Add desiccant spawn data**

Biome modifier:

```json
{
  "type": "neoforge:add_spawns",
  "biomes": "#hemomancy:desiccant_spawnlist",
  "spawners": {
    "type": "hemomancy:desiccant",
    "weight": 6,
    "minCount": 1,
    "maxCount": 2
  }
}
```

Biome tag:

```json
{
  "replace": false,
  "values": [
    "minecraft:desert",
    "minecraft:badlands",
    "minecraft:eroded_badlands",
    "minecraft:wooded_badlands",
    "minecraft:savanna",
    "minecraft:savanna_plateau",
    "minecraft:windswept_savanna"
  ]
}
```

- [ ] **Step 5: Add desiccant loot**

Create a player-kill loot table that drops `hemomancy:telson` with `minecraft:set_count` min 1 max 1 and `minecraft:enchanted_count_increase` for `minecraft:looting`, matching Fargone's material drop structure.

- [ ] **Step 6: Swap recipe ingredient**

Change `src/main/resources/data/hemomancy/recipe/aculeate_vitriol.json` to use:

```json
{ "item": "hemomancy:telson" }
```

instead of `hemomancy:fargone_proboscis`.

### Task 4: Documentation Updates

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`
- Modify: `docs/LORE_REFERENCE.md`

- [ ] **Step 1: Update active/dormant status**

Remove Desiccant from dormant placeholder lists and add it as an active dry-hot desert/badlands scorpion mob with Telson loot.

- [ ] **Step 2: Update armor preparation text**

Change Barbed armor and Aculeate Vitriol descriptions from Fargone Proboscis to Telson, while preserving Fargone Proboscis as a separate Fargone material.

### Task 5: Green Verification And Cleanup

**Files:**
- All touched files

- [ ] **Step 1: Run focused tests**

Run:

```powershell
./gradlew.bat test --tests com.vincenthuto.hemomancy.common.entity.mob.monster.TendencyMobSpawnRulesSourceTest --tests com.vincenthuto.hemomancy.common.recipe.ArmorPreparationResourceTest --tests com.vincenthuto.hemomancy.common.loot.EntityLootTableCoverageResourceTest
```

Expected: PASS.

- [ ] **Step 2: Run broader verification**

Run:

```powershell
./gradlew.bat test
```

Expected: PASS, unless unrelated pre-existing tests fail. If unrelated failures occur, report them with exact names.

- [ ] **Step 3: Inspect changed files**

Run:

```powershell
git diff --stat
git diff -- src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java src/main/resources/data/hemomancy/recipe/aculeate_vitriol.json
```

Expected: diffs only cover desiccant/Telson restoration, recipe swap, and documentation/test updates.
