package com.vincenthuto.hemomancy.common.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record EarthenVeinDestination(UUID id, String name, ResourceLocation dimension, BlockPos position, int cost) {
	public EarthenVeinDestination {
		name = name == null || name.isBlank() ? "Unnamed Earthen Vein" : name;
		position = position.immutable();
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
