package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.UUID;
import java.util.function.Supplier;


import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client packet that syncs a player's equipped morphling.
 * Carries the owner's UUID so that TRACKING_AND_SELF broadcasts can update
 * the correct player's capability on any watching client.
 */
public class SyncEquippedMorphlingPacket {

	private final UUID playerUUID;
	private final ItemStack morphlingStack;

	public SyncEquippedMorphlingPacket(UUID playerUUID, ItemStack stack) {
		this.playerUUID = playerUUID;
		this.morphlingStack = stack == null ? ItemStack.EMPTY : stack;
	}

	public static void encode(SyncEquippedMorphlingPacket msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.playerUUID);
		buf.writeItem(msg.morphlingStack);
	}

	public static SyncEquippedMorphlingPacket decode(FriendlyByteBuf buf) {
		UUID uuid = buf.readUUID();
		ItemStack stack = buf.readItem();
		return new SyncEquippedMorphlingPacket(uuid, stack);
	}

	public static void handle(SyncEquippedMorphlingPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().level == null) return;
			Player target = Minecraft.getInstance().level.getPlayerByUUID(msg.playerUUID);
			if (target == null) return;
			HemoCapabilityAccess.getEquippedMorphling(target)
					.ifPresent(cap -> cap.setEquippedMorphling(msg.morphlingStack));
		});
		ctx.get().setPacketHandled(true);
	}

}
