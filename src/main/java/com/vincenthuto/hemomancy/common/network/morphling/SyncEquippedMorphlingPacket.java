package com.vincenthuto.hemomancy.common.network.morphling;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client packet that syncs the player's equipped morphling.
 */
public class SyncEquippedMorphlingPacket {

	private final ItemStack morphlingStack;

	public SyncEquippedMorphlingPacket(ItemStack stack) {
		this.morphlingStack = stack == null ? ItemStack.EMPTY : stack;
	}

	public static void encode(SyncEquippedMorphlingPacket msg, FriendlyByteBuf buf) {
		buf.writeItem(msg.morphlingStack);
	}

	public static SyncEquippedMorphlingPacket decode(FriendlyByteBuf buf) {
		return new SyncEquippedMorphlingPacket(buf.readItem());
	}

	public static void handle(SyncEquippedMorphlingPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA)
						.ifPresent(cap -> cap.setEquippedMorphling(msg.morphlingStack));
			}
		});
		ctx.get().setPacketHandled(true);
	}

}
