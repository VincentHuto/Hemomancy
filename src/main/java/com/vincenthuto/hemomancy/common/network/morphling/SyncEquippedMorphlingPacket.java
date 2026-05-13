package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

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
		this.morphlingStack = stack == null ? ItemStack.EMPTY : stack.copy();
	}

	public static void encode(FriendlyByteBuf buf, SyncEquippedMorphlingPacket msg) {
		buf.writeUUID(msg.playerUUID);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, msg.morphlingStack);
	}

	public static SyncEquippedMorphlingPacket decode(FriendlyByteBuf buf) {
		UUID uuid = buf.readUUID();
		ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new SyncEquippedMorphlingPacket(uuid, stack);
	}

	public static void handle(final SyncEquippedMorphlingPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player receiver = ctx.player();
			if (receiver == null || receiver.level() == null) return;
			Player target = receiver.level().getPlayerByUUID(msg.playerUUID);
			if (target == null) return;
			HemoCapabilityAccess.getEquippedMorphling(target)
					.ifPresent(cap -> cap.setEquippedMorphling(msg.morphlingStack.copy()));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
