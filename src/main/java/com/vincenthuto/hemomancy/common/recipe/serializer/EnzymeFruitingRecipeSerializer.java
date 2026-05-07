package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.EnzymeFruitingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EnzymeFruitingRecipeSerializer implements RecipeSerializer<EnzymeFruitingRecipe> {

    public static final MapCodec<EnzymeFruitingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("enzyme_fruiting/unknown"))
                            .forGetter(EnzymeFruitingRecipe::getId),
                    Ingredient.CODEC_NONEMPTY.fieldOf("culture").forGetter(EnzymeFruitingRecipe::getCulture),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(EnzymeFruitingRecipe::getResultItemRaw),
                    Codec.INT.optionalFieldOf("duration", EnzymeFruitingRecipe.DEFAULT_DURATION)
                            .forGetter(EnzymeFruitingRecipe::getDuration),
                    Codec.FLOAT.optionalFieldOf("blood_per_tick", EnzymeFruitingRecipe.DEFAULT_BLOOD_PER_TICK)
                            .forGetter(EnzymeFruitingRecipe::getBloodPerTick)
            ).apply(instance, EnzymeFruitingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnzymeFruitingRecipe> STREAM_CODEC =
            StreamCodec.of(EnzymeFruitingRecipeSerializer::toNetwork, EnzymeFruitingRecipeSerializer::fromNetwork);

    private static EnzymeFruitingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Ingredient culture = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
        int duration = buf.readVarInt();
        float bloodPerTick = buf.readFloat();
        return new EnzymeFruitingRecipe(id, culture, result, duration, bloodPerTick);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, EnzymeFruitingRecipe recipe) {
        buf.writeResourceLocation(recipe.getId());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getCulture());
        ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItemRaw());
        buf.writeVarInt(recipe.getDuration());
        buf.writeFloat(recipe.getBloodPerTick());
    }

    @Override
    public MapCodec<EnzymeFruitingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EnzymeFruitingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
