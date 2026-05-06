package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.recipe.FungalScarCultivationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.stream.Stream;

public class FungalScarCultivationSerializer implements RecipeSerializer<FungalScarCultivationRecipe> {

    // ── JSON helpers ──────────────────────────────────────────────────────────

    private static <T> JsonObject toJsonObject(DynamicOps<T> ops, MapLike<T> input) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        input.entries().forEach(pair -> {
            String key = ops.getStringValue(pair.getFirst()).getOrThrow(IllegalStateException::new);
            com.google.gson.JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
            json.add(key, value);
        });
        return json;
    }

    private static FungalScarCultivationRecipe fromJsonObject(ResourceLocation recipeId, JsonObject json) {
        String tendencyStr = GsonHelper.getAsString(json, "tendency");
        EnumBloodTendency tendency;
        try {
            tendency = EnumBloodTendency.valueOf(tendencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonSyntaxException("Unknown tendency: " + tendencyStr);
        }
        float bloodCostPhase1   = GsonHelper.getAsFloat(json,  "blood_cost_phase1",   600f);
        int   phase1Duration    = GsonHelper.getAsInt(json,    "phase1_duration",      600);
        int   matThreshold      = GsonHelper.getAsInt(json,    "maturation_threshold", 2000);
        ItemStack seed          = json.has("seed")
                ? RecipeResultStackParser.parseResultStack(json, "seed")
                : RecipeResultStackParser.parseResultStack(json, "result");
        ItemStack immature      = RecipeResultStackParser.parseResultStack(json, "immature_result");
        ItemStack result        = RecipeResultStackParser.parseResultStack(json, "result");
        return new FungalScarCultivationRecipe(recipeId, tendency, bloodCostPhase1,
                phase1Duration, matThreshold, seed, immature, result);
    }

    // ── MapCodec ──────────────────────────────────────────────────────────────

    private static final MapCodec<FungalScarCultivationRecipe> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("tendency", "blood_cost_phase1", "phase1_duration",
                    "maturation_threshold", "seed", "immature_result", "result", "id")
                    .map(ops::createString);
        }

        @Override
        public <T> DataResult<FungalScarCultivationRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
            try {
                JsonObject json = toJsonObject(ops, input);
                ResourceLocation id = json.has("id")
                        ? ResourceLocation.parse(json.get("id").getAsString())
                        : Hemomancy.rloc("fungal_scar/unknown");
                return DataResult.success(fromJsonObject(id, json));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to decode FungalScarCultivationRecipe: " + e.getMessage());
            }
        }

        @Override
        public <T> RecordBuilder<T> encode(FungalScarCultivationRecipe recipe,
                DynamicOps<T> ops, RecordBuilder<T> prefix) {
            prefix.add("id",                   ops.createString(recipe.getId().toString()));
            prefix.add("tendency",             ops.createString(recipe.getTendency().name()));
            prefix.add("blood_cost_phase1",    ops.createFloat(recipe.getBloodCostPhase1()));
            prefix.add("phase1_duration",      ops.createInt(recipe.getPhase1Duration()));
            prefix.add("maturation_threshold", ops.createInt(recipe.getMaturationThreshold()));
            ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getSeedItemStack()).result()
                    .ifPresent(e -> prefix.add("seed", JsonOps.INSTANCE.convertTo(ops, e)));
            ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getImmatureResult()).result()
                    .ifPresent(e -> prefix.add("immature_result", JsonOps.INSTANCE.convertTo(ops, e)));
            ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItemStack()).result()
                    .ifPresent(e -> prefix.add("result", JsonOps.INSTANCE.convertTo(ops, e)));
            return prefix;
        }
    };

    // ── StreamCodec (network) ─────────────────────────────────────────────────

    private static final StreamCodec<RegistryFriendlyByteBuf, FungalScarCultivationRecipe> STREAM_CODEC =
            StreamCodec.of(FungalScarCultivationSerializer::toNetwork,
                           FungalScarCultivationSerializer::fromNetwork);

    @Override
    public MapCodec<FungalScarCultivationRecipe> codec()             { return CODEC; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FungalScarCultivationRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static FungalScarCultivationRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation id          = buf.readResourceLocation();
        EnumBloodTendency tendency   = EnumBloodTendency.valueOf(buf.readUtf());
        float bloodCostPhase1        = buf.readFloat();
        int   phase1Duration         = buf.readVarInt();
        int   matThreshold           = buf.readVarInt();
        ItemStack seed               = ItemStack.STREAM_CODEC.decode(buf);
        ItemStack immature           = ItemStack.STREAM_CODEC.decode(buf);
        ItemStack result             = ItemStack.STREAM_CODEC.decode(buf);
        return new FungalScarCultivationRecipe(id, tendency, bloodCostPhase1,
                phase1Duration, matThreshold, seed, immature, result);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, FungalScarCultivationRecipe recipe) {
        buf.writeResourceLocation(recipe.getId());
        buf.writeUtf(recipe.getTendency().name());
        buf.writeFloat(recipe.getBloodCostPhase1());
        buf.writeVarInt(recipe.getPhase1Duration());
        buf.writeVarInt(recipe.getMaturationThreshold());
        ItemStack.STREAM_CODEC.encode(buf, recipe.getSeedItemStack());
        ItemStack.STREAM_CODEC.encode(buf, recipe.getImmatureResult());
        ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItemStack());
    }
}
