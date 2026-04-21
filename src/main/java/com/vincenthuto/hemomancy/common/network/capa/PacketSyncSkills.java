package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.init.SkillPointInit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client packet: synchronises the full skill tree state
 * (every skill's {@code state} and {@code currentLevel}).
 */
public class PacketSyncSkills {

	private final ListTag data;

	public PacketSyncSkills(ListTag data) {
		this.data = data;
	}

	public static void encode(PacketSyncSkills msg, FriendlyByteBuf buf) {
		CompoundTag wrapper = new CompoundTag();
		wrapper.put("skills", msg.data);
		buf.writeNbt(wrapper);
	}

	public static PacketSyncSkills decode(FriendlyByteBuf buf) {
		CompoundTag wrapper = buf.readNbt();
		ListTag list = (wrapper != null) ? wrapper.getList("skills", 10) : new ListTag();
		return new PacketSyncSkills(list);
	}

	public static void handle(PacketSyncSkills msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			SkillPointInit.deserializeAll(msg.data);
		});
		ctx.get().setPacketHandled(true);
	}
}
