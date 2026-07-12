package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client packet: synchronises the player's current initiatory degree.
 */
public class PacketSyncDegree implements CustomPacketPayload {

	public static final Type<PacketSyncDegree> TYPE = new Type<>(Hemomancy.rloc("packet_sync_degree"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncDegree> STREAM_CODEC = StreamCodec.of(PacketSyncDegree::encode, PacketSyncDegree::decode);

	private final int degreeNumber;
	private final boolean hasFoundedBloodline, founderIntegrationSevered;
	private final boolean fungalRevelationWitnessed, fungalSpineGranted;
	private final EnumArchonPath archonPath;

	public PacketSyncDegree(IInitiatoryDegree degree) {
		this(degree.getDegreeNumber(), degree.hasFoundedBloodline(), degree.isFounderIntegrationSevered(),
				degree.hasWitnessedFungalRevelation(), degree.hasFungalSpineGranted(), degree.getArchonPath());
	}

	private PacketSyncDegree(int degreeNumber, boolean hasFoundedBloodline, boolean founderIntegrationSevered,
			boolean fungalRevelationWitnessed, boolean fungalSpineGranted, EnumArchonPath archonPath) {
		this.degreeNumber = degreeNumber;
		this.hasFoundedBloodline = hasFoundedBloodline;
		this.founderIntegrationSevered = founderIntegrationSevered;
		this.fungalRevelationWitnessed = fungalRevelationWitnessed;
		this.fungalSpineGranted = fungalSpineGranted;
		this.archonPath = archonPath;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncDegree msg) {
		buf.writeInt(msg.degreeNumber);
		buf.writeBoolean(msg.hasFoundedBloodline);
		buf.writeBoolean(msg.founderIntegrationSevered);
		buf.writeBoolean(msg.fungalRevelationWitnessed);
		buf.writeBoolean(msg.fungalSpineGranted);
		buf.writeEnum(msg.archonPath);
	}

	public static PacketSyncDegree decode(FriendlyByteBuf buf) {
		return new PacketSyncDegree(buf.readInt(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean(), buf.readBoolean(), buf.readEnum(EnumArchonPath.class));
	}

	public static void handle(final PacketSyncDegree msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
					degree.setDegreeNumber(msg.degreeNumber);
					degree.setHasFoundedBloodline(msg.hasFoundedBloodline);
					degree.setFounderIntegrationSevered(msg.founderIntegrationSevered);
					degree.setFungalRevelationWitnessed(msg.fungalRevelationWitnessed);
					degree.setFungalSpineGranted(msg.fungalSpineGranted);
					degree.setArchonPath(msg.archonPath);
				});
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
