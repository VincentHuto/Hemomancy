package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.brewing.AdvancedBrewingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class AdvancedBrewingRecipeSerializer implements RecipeSerializer<AdvancedBrewingRecipe> {
    private static final MapCodec<AdvancedBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("unknown_brew")).forGetter(AdvancedBrewingRecipe::id),
            Potion.CODEC.fieldOf("input_potion").forGetter(AdvancedBrewingRecipe::inputPotion),
            Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(AdvancedBrewingRecipe::catalyst),
            MobEffect.CODEC.fieldOf("output_effect").forGetter(AdvancedBrewingRecipe::effect),
            Codec.INT.fieldOf("duration").forGetter(AdvancedBrewingRecipe::duration),
            Codec.INT.fieldOf("amplifier").forGetter(AdvancedBrewingRecipe::amplifier),
            Codec.INT.fieldOf("blood").forGetter(AdvancedBrewingRecipe::blood),
            Codec.INT.fieldOf("ticks").forGetter(AdvancedBrewingRecipe::ticks),
            Codec.INT.fieldOf("tier").forGetter(AdvancedBrewingRecipe::tier),
            Codec.STRING.optionalFieldOf("attachment", "").forGetter(AdvancedBrewingRecipe::attachment)
    ).apply(instance, AdvancedBrewingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, AdvancedBrewingRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                buffer.writeResourceLocation(recipe.id());
                Potion.STREAM_CODEC.encode(buffer, recipe.inputPotion());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.catalyst());
                MobEffect.STREAM_CODEC.encode(buffer, recipe.effect());
                buffer.writeVarInt(recipe.duration());
                buffer.writeVarInt(recipe.amplifier());
                buffer.writeVarInt(recipe.blood());
                buffer.writeVarInt(recipe.ticks());
                buffer.writeVarInt(recipe.tier());
                buffer.writeUtf(recipe.attachment());
            }, buffer -> new AdvancedBrewingRecipe(buffer.readResourceLocation(),
                    Potion.STREAM_CODEC.decode(buffer), Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    MobEffect.STREAM_CODEC.decode(buffer), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf()));

    @Override public MapCodec<AdvancedBrewingRecipe> codec() { return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, AdvancedBrewingRecipe> streamCodec() { return STREAM_CODEC; }
}
