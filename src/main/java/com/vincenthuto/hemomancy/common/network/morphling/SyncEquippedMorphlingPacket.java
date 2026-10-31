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
 * the correct player's capability on any watching client, plus the strain's
 * {@code MorphlingActiveState} bits and Dead Man's Rope strands (entity ids and
 * block anchors) for Track 3's visuals.
 */
public class SyncEquippedMorphlingPacket implements CustomPacketPayload {

	public static final Type<SyncEquippedMorphlingPacket> TYPE = new Type<>(Hemomancy.rloc("sync_equipped_morphling_packet"));
	public static final StreamCodec<FriendlyByteBuf, SyncEquippedMorphlingPacket> STREAM_CODEC = StreamCodec.of(SyncEquippedMorphlingPacket::encode, SyncEquippedMorphlingPacket::decode);

	private final UUID playerUUID;
	private final ItemStack morphlingStack;
	private final int activeFlags;
	private final int[] linkedEntityIds;
	private final long[] linkedAnchors;

	public SyncEquippedMorphlingPacket(UUID playerUUID, ItemStack stack) {
		this(playerUUID, stack, 0, new int[0], new long[0]);
	}

	public SyncEquippedMorphlingPacket(UUID playerUUID, ItemStack stack, int activeFlags, int[] linkedEntityIds,
			long[] linkedAnchors) {
		this.playerUUID = playerUUID;
		this.morphlingStack = stack == null ? ItemStack.EMPTY : stack.copy();
		this.activeFlags = activeFlags;
		this.linkedEntityIds = linkedEntityIds == null ? new int[0] : linkedEntityIds.clone();
		this.linkedAnchors = linkedAnchors == null ? new long[0] : linkedAnchors.clone();
	}

	public int activeFlags() {
		return activeFlags;
	}

	public UUID playerUUID() {
		return playerUUID;
	}

	public int[] linkedEntityIds() {
		return linkedEntityIds.clone();
	}

	public long[] linkedAnchors() {
		return linkedAnchors.clone();
	}

	public static void encode(FriendlyByteBuf buf, SyncEquippedMorphlingPacket msg) {
		buf.writeUUID(msg.playerUUID);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, msg.morphlingStack);
		buf.writeVarInt(msg.activeFlags);
		buf.writeVarIntArray(msg.linkedEntityIds);
		buf.writeLongArray(msg.linkedAnchors);
	}

	public static SyncEquippedMorphlingPacket decode(FriendlyByteBuf buf) {
		UUID uuid = buf.readUUID();
		ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		int flags = buf.readVarInt();
		int[] ids = buf.readVarIntArray(MAX_LINKS);
		long[] anchors = buf.readLongArray(null, MAX_LINKS);
		return new SyncEquippedMorphlingPacket(uuid, stack, flags, ids, anchors);
	}

	private static final int MAX_LINKS = 8;

	public static void handle(final SyncEquippedMorphlingPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player receiver = ctx.player();
			if (receiver == null || receiver.level() == null) return;
			Player target = receiver.level().getPlayerByUUID(msg.playerUUID);
			if (target == null) return;
			var equipped = HemoCapabilityAccess.getEquippedMorphling(target);
			equipped.setEquippedMorphling(msg.morphlingStack.copy());
			equipped.setActiveFlags(msg.activeFlags);
			equipped.setLinks(msg.linkedEntityIds, msg.linkedAnchors);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
