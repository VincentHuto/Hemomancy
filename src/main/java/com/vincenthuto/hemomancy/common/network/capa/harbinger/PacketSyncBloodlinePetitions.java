package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server to client: the petitions waiting on the leader's line, oldest first (PT-35). Sent to the leader at login, on
 * a new petition, after each answer and when the ledger is opened; an empty list to anyone who leads no line.
 */
public record PacketSyncBloodlinePetitions(List<Petition> petitions) implements CustomPacketPayload {

	public record Petition(UUID applicant, String name) {
	}

	/** Past this many a list is cut short; a line never keeps more ({@code BloodlineSavedData.MAX_PETITIONS}). */
	private static final int MAX_SENT = 64;
	private static final int MAX_NAME = 64;

	public static final Type<PacketSyncBloodlinePetitions> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_bloodline_petitions"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBloodlinePetitions> STREAM_CODEC =
			StreamCodec.of(PacketSyncBloodlinePetitions::encode, PacketSyncBloodlinePetitions::decode);

	public static PacketSyncBloodlinePetitions of(Map<UUID, String> waiting) {
		List<Petition> petitions = new ArrayList<>();
		waiting.forEach((applicant, name) -> petitions.add(new Petition(applicant, name)));
		return new PacketSyncBloodlinePetitions(List.copyOf(petitions));
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncBloodlinePetitions msg) {
		int count = Math.min(msg.petitions.size(), MAX_SENT);
		buf.writeVarInt(count);
		for (int i = 0; i < count; i++) {
			buf.writeUUID(msg.petitions.get(i).applicant());
			buf.writeUtf(msg.petitions.get(i).name(), MAX_NAME);
		}
	}

	public static PacketSyncBloodlinePetitions decode(FriendlyByteBuf buf) {
		int count = Math.min(buf.readVarInt(), MAX_SENT);
		List<Petition> petitions = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			petitions.add(new Petition(buf.readUUID(), buf.readUtf(MAX_NAME)));
		}
		return new PacketSyncBloodlinePetitions(List.copyOf(petitions));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
