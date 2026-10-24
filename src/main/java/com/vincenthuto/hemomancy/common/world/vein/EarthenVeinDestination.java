package com.vincenthuto.hemomancy.common.world.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record EarthenVeinDestination(UUID id, String name, ResourceLocation dimension, BlockPos position, int cost) {
	public EarthenVeinDestination {
		name = name == null || name.isBlank() ? "" : name;
		position = position.immutable();
	}

	public MutableComponent displayName() {
		return name.isEmpty() ? Component.translatable("gui.hemomancy.earthen_vein.unnamed") : Component.literal(name);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeUtf(name);
		buffer.writeResourceLocation(dimension);
		buffer.writeBlockPos(position);
		buffer.writeVarInt(cost);
	}

	public static EarthenVeinDestination read(FriendlyByteBuf buffer) {
		return new EarthenVeinDestination(buffer.readUUID(), buffer.readUtf(), buffer.readResourceLocation(),
				buffer.readBlockPos(), buffer.readVarInt());
	}
}
