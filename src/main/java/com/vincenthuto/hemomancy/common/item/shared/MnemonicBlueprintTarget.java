package com.vincenthuto.hemomancy.common.item.shared;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Objects;

public record MnemonicBlueprintTarget(Type type, ResourceLocation recipeId) {
	public static final Codec<MnemonicBlueprintTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Type.CODEC.fieldOf("type").forGetter(MnemonicBlueprintTarget::type),
			ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(MnemonicBlueprintTarget::recipeId))
			.apply(instance, MnemonicBlueprintTarget::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MnemonicBlueprintTarget> STREAM_CODEC =
			StreamCodec.of((buffer, target) -> {
				buffer.writeEnum(target.type);
				buffer.writeResourceLocation(target.recipeId);
			}, buffer -> new MnemonicBlueprintTarget(buffer.readEnum(Type.class), buffer.readResourceLocation()));

	public MnemonicBlueprintTarget {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(recipeId, "recipeId");
	}

	public enum Type implements StringRepresentable {
		CARDINAL_RITE,
		BLOOD_STRUCTURE;

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
