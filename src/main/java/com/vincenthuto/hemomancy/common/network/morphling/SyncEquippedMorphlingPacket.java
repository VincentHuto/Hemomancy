package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Server → Client packet that syncs a player's equipped morphling.
 * Carries the owner's UUID so that TRACKING_AND_SELF broadcasts can update
 * the correct player's capability on any watching client.
 */
public class SyncEquippedMorphlingPacket implements CustomPacketPayload {

	public static final Type<SyncEquippedMorphlingPacket> TYPE = new Type<>(Hemomancy.rloc("sync_equipped_morphling_packet"));
	public static final StreamCodec<FriendlyByteBuf, SyncEquippedMorphlingPacket> STREAM_CODEC = StreamCodec.of(SyncEquippedMorphlingPacket::encode, SyncEquippedMorphlingPacket::decode);

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

	public static void handle(final SyncEquippedMorphlingPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null) return;
			Player target = Minecraft.getInstance().level.getPlayerByUUID(msg.playerUUID);
			if (target == null) return;
			HemoCapabilityAccess.getEquippedMorphling(target)
					.ifPresent(cap -> cap.setEquippedMorphling(msg.morphlingStack));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
