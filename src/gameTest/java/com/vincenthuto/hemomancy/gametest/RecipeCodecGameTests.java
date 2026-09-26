package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.*;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("recipe_codec_validation")
@PrefixGameTestTemplate(false)
public final class RecipeCodecGameTests {
    @GameTest(template = "empty")
    public static void nestedOfferingRetainsContextAndPropagatesErrors(GameTestHelper helper) {
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var sword = ItemStack.CODEC.parse(ops, JsonParser.parseString("""
                {"id":"minecraft:diamond_sword","components":{
                "minecraft:enchantments":{"levels":{"minecraft:unbreaking":2}}}}
                """)).getOrThrow();
        var ingredient = net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false, sword);
        var json = JsonParser.parseString("""
                {"bloodCost":10,"heldItem":"minecraft:stick","hitBlock":"minecraft:stone",
                 "pattern":[["S"]],"key":{"S":{"block":"minecraft:stone"}},
                 "result":"minecraft:diamond","offerings":[{"count":2}]}
                """).getAsJsonObject();
        json.getAsJsonArray("offerings").get(0).getAsJsonObject().add("ingredient",
                Ingredient.CODEC_NONEMPTY.encodeStart(ops, ingredient).getOrThrow());
        var codec = new BloodStructureRecipeSerializer().codec().codec();
        var recipe = codec.parse(ops, json).getOrThrow();
        helper.assertTrue(recipe.getOfferings().getFirst().ingredient().test(sword), "Offering components lost");
        var encoded = codec.encodeStart(ops, recipe).getOrThrow().getAsJsonObject();
        helper.assertTrue(encoded.get("heldItem").getAsString().equals("minecraft:stick"), "heldItem schema changed");
        var offering = encoded.getAsJsonArray("offerings").get(0).getAsJsonObject();
        var restored = Ingredient.CODEC_NONEMPTY.parse(ops, offering.get("ingredient")).getOrThrow();
        helper.assertTrue(restored.test(sword) && !restored.test(new ItemStack(Items.DIAMOND_SWORD))
                && offering.get("count").getAsInt() == 2, "Offering predicate or count changed");
        helper.assertTrue(codec.encodeStart(JsonOps.INSTANCE, recipe).error().isPresent(),
                "Nested offering encoding error silently omitted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void incubatorRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new IncubatorRecipeSerializer().codec().codec(), "incubator/morphling_bootlace");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void memoryWeavingRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new MemoryWeavingRecipeSerializer().codec().codec(), "memory_weaving/blood_thrall_effigy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void scarRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new ScarRecipeSerializer().codec().codec(), "scar/scar_anvil");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void armatureRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new ArmatureUpgradeRecipeSerializer().codec().codec(), "armature_upgrade/barbed_to_blood_lust_boots");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fungalScarRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new FungalScarCultivationSerializer().codec().codec(), "fungal_scar/antiphonomyces_resonans");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bloodStructureRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new BloodStructureRecipeSerializer().codec().codec(), "blood_structure/consecrated_bloodwell");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cardinalRiteRetainsResultFormatsAndRegistryContext(GameTestHelper helper) throws Exception {
        checkFamily(helper, new CardinalRiteRecipeSerializer().codec().codec(), "cardinal_rite/ancestral_communion");
        helper.succeed();
    }

    private static <R> void checkFamily(GameTestHelper helper, Codec<R> codec, String path) throws Exception {
        JsonObject json;
        try (var stream = RecipeCodecGameTests.class.getResourceAsStream("/data/hemomancy/recipe/" + path + ".json");
             var reader = new InputStreamReader(java.util.Objects.requireNonNull(stream), StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        // Exercise the shipped family schema before adding component-bearing results.
        codec.encodeStart(ops, codec.parse(ops, json).getOrThrow()).getOrThrow();
        for (String result : new String[] {
                "\"minecraft:diamond\"",
                "{\"id\":\"minecraft:diamond\",\"count\":3}",
                "{\"item\":\"minecraft:diamond\",\"count\":3}"}) {
            json.add("result", JsonParser.parseString(result));
            json.addProperty("count", 3);
            var encoded = codec.encodeStart(ops, codec.parse(ops, json).getOrThrow()).getOrThrow();
            var stack = ItemStack.CODEC.parse(ops, encoded.getAsJsonObject().get("result")).getOrThrow();
            helper.assertTrue(stack.is(Items.DIAMOND) && stack.getCount() == 3, path + " lost legacy count/item");
        }
        var enchanted = JsonParser.parseString("""
                {"id":"minecraft:diamond_sword","components":{
                "minecraft:enchantments":{"levels":{"minecraft:unbreaking":2}}}}
                """);
        json.add("result", enchanted);
        if (json.has("seed")) {
            json.add("seed", enchanted.deepCopy());
            json.add("immature_result", enchanted.deepCopy());
        }
        var recipe = codec.parse(ops, json).getOrThrow();
        var encoded = codec.encodeStart(ops, recipe).getOrThrow().getAsJsonObject();
        for (String field : new String[] {"result", "seed", "immature_result"}) {
            if (encoded.has(field)) {
                helper.assertTrue(ItemStack.CODEC.parse(ops, encoded.get(field)).getOrThrow().isEnchanted(),
                        path + " lost " + field + " registry components");
            }
        }
        helper.assertTrue(codec.encodeStart(JsonOps.INSTANCE, recipe).error().isPresent(),
                path + " silently discarded a component encoding error");
        var nbtOps = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        // Spatial encoders already omit patterns; use the complete input to exercise their NBT decode.
        var fromNbt = codec.parse(nbtOps, JsonOps.INSTANCE.convertTo(nbtOps, json)).getOrThrow();
        var nbt = codec.encodeStart(nbtOps, fromNbt).getOrThrow();
        var nbtJson = nbtOps.convertTo(ops, nbt).getAsJsonObject();
        helper.assertTrue(ItemStack.CODEC.parse(ops, nbtJson.get("result")).getOrThrow().isEnchanted(),
                path + " lost NBT registry context");
    }


    @GameTest(template = "empty")
    public static void registryComponentsSurviveJsonAndNbt(GameTestHelper helper) {
        var json = JsonParser.parseString("""
                {"catalysts":[{"item":"minecraft:stick"}],"result":{
                "item":"minecraft:diamond_sword","components":{
                "minecraft:enchantments":{"levels":{"minecraft:unbreaking":2}}}}}
                """);
        var codec = new IncubatorRecipeSerializer().codec().codec();
        var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var recipe = codec.parse(ops, json).getOrThrow();
        helper.assertTrue(recipe.getResultItemStack().isEnchanted(), "Enchantment was lost on decode");
        var encoded = codec.encodeStart(ops, recipe).getOrThrow();
        helper.assertTrue(ItemStack.matches(recipe.getResultItemStack(),
                codec.parse(ops, encoded).getOrThrow().getResultItemStack()), "JSON changed result");
        var nbtOps = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var nbt = codec.encodeStart(nbtOps, recipe).getOrThrow();
        helper.assertTrue(ItemStack.matches(recipe.getResultItemStack(),
                codec.parse(nbtOps, nbt).getOrThrow().getResultItemStack()), "NBT changed result");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidResultFailsEncoding(GameTestHelper helper) {
        var recipe = new IncubatorRecipe(ResourceLocation.parse("hemomancy:codec_test"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.STICK)), ItemStack.EMPTY);
        helper.assertTrue(new IncubatorRecipeSerializer().codec().codec()
                .encodeStart(JsonOps.INSTANCE, recipe).error().isPresent(), "Invalid result silently omitted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidCatalystFailsEncoding(GameTestHelper helper) {
        var recipe = new IncubatorRecipe(ResourceLocation.parse("hemomancy:codec_test"),
                NonNullList.of(Ingredient.EMPTY, Ingredient.EMPTY), new ItemStack(Items.DIAMOND));
        helper.assertTrue(new IncubatorRecipeSerializer().codec().codec()
                .encodeStart(JsonOps.INSTANCE, recipe).error().isPresent(), "Invalid catalyst silently omitted");
        helper.succeed();
    }
}
