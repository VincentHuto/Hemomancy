package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: Syncs all active cardinal rite positions, sizes, and
 * progress so the client can render glowing boundary circles.
 */
public class PacketSyncActiveRites implements CustomPacketPayload {

	public static final Type<PacketSyncActiveRites> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "packet_sync_active_rites"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncActiveRites> STREAM_CODEC = StreamCodec.of(PacketSyncActiveRites::encode, PacketSyncActiveRites::decode);

	private final List<ActiveRiteClientData.RiteEntry> entries;

	public PacketSyncActiveRites(List<ActiveRiteClientData.RiteEntry> entries) {
		this.entries = List.copyOf(entries);
	}

	List<ActiveRiteClientData.RiteEntry> entries() {
		return entries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncActiveRites msg) {
		buf.writeInt(msg.entries.size());
		for (ActiveRiteClientData.RiteEntry entry : msg.entries) {
			buf.writeBlockPos(entry.getCenter());
			buf.writeInt(entry.getRiteSize());
			buf.writeDouble(entry.getProgress());
			buf.writeResourceLocation(entry.getRecipeId());
			buf.writeBoolean(entry.isUnstained());
			buf.writeUtf(entry.getPhase());
			buf.writeVarInt(entry.getPhaseTicks());
			buf.writeVarInt(entry.getInstability());
			buf.writeVarInt(entry.getCurrentWave());
			buf.writeVarInt(entry.getTotalWaves());
			buf.writeVarInt(entry.getCompletedRings());
			buf.writeVarInt(entry.getTotalRings());
			buf.writeVarInt(entry.getCommittedBloodMl());
			buf.writeVarInt(entry.getUpfrontBloodMl());
			buf.writeVarInt(entry.getCarriedIchorMl());
			buf.writeVarInt(entry.getAllyCount());
			buf.writeInt(entry.getSharedBloodMl());
			buf.writeUtf(entry.getCue());
			buf.writeFloat(entry.getFootprintRadius());
			buf.writeBoolean(entry.hasPlantedStaff());
			buf.writeUUID(entry.getOwner() == null ? new java.util.UUID(0L, 0L) : entry.getOwner());
			buf.writeVarInt(entry.getCancellationTicks());
			buf.writeInt(entry.getStaffPlantingTicks());
			buf.writeUtf(entry.getFogProfile());
			buf.writeBoolean(entry.hasFogLightning());
			buf.writeBoolean(entry.hasBoundaryDome());
			buf.writeVarInt(entry.getChecklist().size());
			for (String line : entry.getChecklist()) buf.writeUtf(line);
			buf.writeVarInt(entry.getBoundarySegments().size());
			for (CardinalRiteBoundaryProgress.Segment segment : entry.getBoundarySegments()) {
				buf.writeVarInt(segment.ring());
				buf.writeDouble(segment.startAngle());
				buf.writeDouble(segment.sweepAngle());
				buf.writeInt(segment.startAnchorIndex());
				buf.writeFloat(segment.integrity());
			}
			buf.writeVarInt(entry.getSigilSegments().size());
			for (ActiveRiteClientData.SigilSegment segment : entry.getSigilSegments()) {
				buf.writeDouble(segment.startX());
				buf.writeDouble(segment.startY());
				buf.writeDouble(segment.startZ());
				buf.writeDouble(segment.endX());
				buf.writeDouble(segment.endY());
				buf.writeDouble(segment.endZ());
				buf.writeInt(segment.color());
			}
			buf.writeVarInt(entry.getSanguineBlobs().size());
			for (ActiveRiteClientData.SanguineBlob blob : entry.getSanguineBlobs()) {
				buf.writeDouble(blob.x());
				buf.writeDouble(blob.y());
				buf.writeDouble(blob.z());
				buf.writeFloat(blob.radius());
				buf.writeInt(blob.color());
				buf.writeLong(blob.seed());
				buf.writeFloat(blob.integrity());
				buf.writeEnum(blob.kind());
				buf.writeEnum(blob.role());
			}
		}
	}

	public static PacketSyncActiveRites decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos center = buf.readBlockPos();
			int riteSize = buf.readInt();
			double progress = buf.readDouble();
			ResourceLocation recipeId = buf.readResourceLocation();
			boolean unstained = buf.readBoolean();
			String phase = buf.readUtf();
			int phaseTicks = buf.readVarInt();
			int instability = buf.readVarInt();
			int currentWave = buf.readVarInt();
			int totalWaves = buf.readVarInt();
			int completedRings = buf.readVarInt();
			int totalRings = buf.readVarInt();
			int committedBlood = buf.readVarInt();
			int upfrontBlood = buf.readVarInt();
			int carriedIchor = buf.readVarInt();
			int allyCount = buf.readVarInt();
			int sharedBlood = buf.readInt();
			String cue = buf.readUtf();
			float footprintRadius = buf.readFloat();
			boolean plantedStaff = buf.readBoolean();
			java.util.UUID owner = buf.readUUID();
			int cancellationTicks = buf.readVarInt();
			int staffPlantingTicks = buf.readInt();
			String fogProfile = buf.readUtf();
			boolean fogLightning = buf.readBoolean();
			boolean boundaryDome = buf.readBoolean();
			int checklistCount = buf.readVarInt();
			List<String> checklist = new ArrayList<>(checklistCount);
			for (int lineIndex = 0; lineIndex < checklistCount; lineIndex++) checklist.add(buf.readUtf());
			int segmentCount = buf.readVarInt();
			List<CardinalRiteBoundaryProgress.Segment> boundarySegments = new ArrayList<>(segmentCount);
			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
				boundarySegments.add(new CardinalRiteBoundaryProgress.Segment(
						buf.readVarInt(), buf.readDouble(), buf.readDouble(),
						buf.readInt(), buf.readFloat()));
			}
			int sigilSegmentCount = buf.readVarInt();
			List<ActiveRiteClientData.SigilSegment> sigilSegments = new ArrayList<>(sigilSegmentCount);
			for (int segmentIndex = 0; segmentIndex < sigilSegmentCount; segmentIndex++) {
				sigilSegments.add(new ActiveRiteClientData.SigilSegment(
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readInt()));
			}
			int blobCount = buf.readVarInt();
			List<ActiveRiteClientData.SanguineBlob> sanguineBlobs = new ArrayList<>(blobCount);
			for (int blobIndex = 0; blobIndex < blobCount; blobIndex++) {
				sanguineBlobs.add(new ActiveRiteClientData.SanguineBlob(
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readFloat(), buf.readInt(), buf.readLong(), buf.readFloat(),
						buf.readEnum(ActiveRiteClientData.NodeKind.class),
						buf.readEnum(IchorianSigilAnatomy.Role.class)));
			}
			entries.add(new ActiveRiteClientData.RiteEntry(center, riteSize, progress, recipeId, unstained,
					phase, instability, currentWave, totalWaves, completedRings, totalRings,
					committedBlood, upfrontBlood, carriedIchor, allyCount, sharedBlood, cue,
					footprintRadius, checklist,
					boundarySegments, sigilSegments, sanguineBlobs, plantedStaff, owner,
					cancellationTicks, staffPlantingTicks,
					fogProfile, fogLightning, boundaryDome, phaseTicks));
		}
		return new PacketSyncActiveRites(entries);
	}

	public static void handle(final PacketSyncActiveRites msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ActiveRiteClientData.set(msg.entries);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
