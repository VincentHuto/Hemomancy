package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineMembership;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client to server: the leader's Admit or Refuse on a petition row of the ledger screen (PT-35, NQ-11-7). The screen
 * opens only from the ledger, so it must be in hand; leadership is checked by the service.
 */
public record PacketAnswerBloodlinePetition(UUID applicant, boolean admit) implements CustomPacketPayload {

	public static final Type<PacketAnswerBloodlinePetition> TYPE =
			new Type<>(Hemomancy.rloc("packet_answer_bloodline_petition"));
	public static final StreamCodec<FriendlyByteBuf, PacketAnswerBloodlinePetition> STREAM_CODEC =
			StreamCodec.of(PacketAnswerBloodlinePetition::encode, PacketAnswerBloodlinePetition::decode);

	public static void encode(FriendlyByteBuf buf, PacketAnswerBloodlinePetition msg) {
		buf.writeUUID(msg.applicant);
		buf.writeBoolean(msg.admit);
	}

	public static PacketAnswerBloodlinePetition decode(FriendlyByteBuf buf) {
		return new PacketAnswerBloodlinePetition(buf.readUUID(), buf.readBoolean());
	}

	public static void handle(final PacketAnswerBloodlinePetition msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer leader)) return;
			if (ServerboundGuards.heldItem(leader, UnsignedLedgerItem.class).isEmpty()) return;
			if (!ServerboundRateLimiter.tryAcquire(leader, "answer_bloodline_petition", 5)) return;
			BloodlineMembership.answerPetition(leader, msg.applicant, msg.admit);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
