package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: Syncs all active cardinal rite positions, sizes, and
 * progress so the client can render glowing boundary circles.
 */
public class PacketSyncActiveRites implements CustomPacketPayload {

	public static final Type<PacketSyncActiveRites> TYPE = new Type<>(
			Hemomancy.rloc("packet_sync_active_rites"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncActiveRites> STREAM_CODEC = StreamCodec.of(PacketSyncActiveRites::encode, PacketSyncActiveRites::decode);

	private final List<ActiveRiteData.RiteEntry> entries;

	public PacketSyncActiveRites(List<ActiveRiteData.RiteEntry> entries) {
		this.entries = List.copyOf(entries);
	}

	public List<ActiveRiteData.RiteEntry> entries() {
		return entries;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncActiveRites msg) {
		buf.writeInt(msg.entries.size());
		for (ActiveRiteData.RiteEntry entry : msg.entries) {
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
			for (Component line : entry.getChecklist()) ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, line);
			buf.writeVarInt(entry.getBoundarySegments().size());
			for (CardinalRiteBoundaryProgress.Segment segment : entry.getBoundarySegments()) {
				buf.writeVarInt(segment.ring());
				buf.writeDouble(segment.startAngle());
				buf.writeDouble(segment.sweepAngle());
				buf.writeInt(segment.startAnchorIndex());
				buf.writeFloat(segment.integrity());
			}
			buf.writeVarInt(entry.getSigilSegments().size());
			for (ActiveRiteData.SigilSegment segment : entry.getSigilSegments()) {
				buf.writeDouble(segment.startX());
				buf.writeDouble(segment.startY());
				buf.writeDouble(segment.startZ());
				buf.writeDouble(segment.endX());
				buf.writeDouble(segment.endY());
				buf.writeDouble(segment.endZ());
				buf.writeInt(segment.color());
			}
			buf.writeVarInt(entry.getSanguineBlobs().size());
			for (ActiveRiteData.SanguineBlob blob : entry.getSanguineBlobs()) {
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
			buf.writeVarIntArray(entry.getAnchorStates());
			buf.writeVarInt(entry.getEscalation());
			buf.writeVarInt(entry.getEscalationTotal());
			buf.writeVarInt(entry.getProblems().size());
			for (var problem : entry.getProblems()) {
				buf.writeUtf(problem.key());
				buf.writeVarInt(problem.args().size());
				for (String arg : problem.args()) buf.writeUtf(arg);
			}
			buf.writeVarInt(entry.getSocketMarkers().size());
			for (ActiveRiteData.SocketMarker marker : entry.getSocketMarkers()) {
				buf.writeBlockPos(marker.pos());
				buf.writeUtf(marker.sigil());
			}
			buf.writeVarInt(entry.getOpenRings());
			buf.writeVarIntArray(entry.getAnchorRequirements());
			buf.writeVarInt(entry.getSubjectPositions().size());
			for (BlockPos pos : entry.getSubjectPositions()) buf.writeBlockPos(pos);
			buf.writeVarIntArray(entry.getSubjectStateIds());
			buf.writeFloat(entry.getSubjectFill());
		}
	}

	private static List<BlockPos> readPositions(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<BlockPos> positions = new ArrayList<>(count);
		for (int i = 0; i < count; i++) positions.add(buf.readBlockPos());
		return positions;
	}

	public static PacketSyncActiveRites decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<ActiveRiteData.RiteEntry> entries = new ArrayList<>(count);
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
			List<Component> checklist = new ArrayList<>(checklistCount);
			for (int lineIndex = 0; lineIndex < checklistCount; lineIndex++) {
				checklist.add(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf));
			}
			int segmentCount = buf.readVarInt();
			List<CardinalRiteBoundaryProgress.Segment> boundarySegments = new ArrayList<>(segmentCount);
			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
				boundarySegments.add(new CardinalRiteBoundaryProgress.Segment(
						buf.readVarInt(), buf.readDouble(), buf.readDouble(),
						buf.readInt(), buf.readFloat()));
			}
			int sigilSegmentCount = buf.readVarInt();
			List<ActiveRiteData.SigilSegment> sigilSegments = new ArrayList<>(sigilSegmentCount);
			for (int segmentIndex = 0; segmentIndex < sigilSegmentCount; segmentIndex++) {
				sigilSegments.add(new ActiveRiteData.SigilSegment(
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readInt()));
			}
			int blobCount = buf.readVarInt();
			List<ActiveRiteData.SanguineBlob> sanguineBlobs = new ArrayList<>(blobCount);
			for (int blobIndex = 0; blobIndex < blobCount; blobIndex++) {
				sanguineBlobs.add(new ActiveRiteData.SanguineBlob(
						buf.readDouble(), buf.readDouble(), buf.readDouble(),
						buf.readFloat(), buf.readInt(), buf.readLong(), buf.readFloat(),
						buf.readEnum(ActiveRiteData.NodeKind.class),
						buf.readEnum(IchorianSigilAnatomy.Role.class)));
			}
			entries.add(new ActiveRiteData.RiteEntry(center, riteSize, progress, recipeId, unstained,
					phase, instability, currentWave, totalWaves, completedRings, totalRings,
					committedBlood, upfrontBlood, carriedIchor, allyCount, sharedBlood, cue,
					footprintRadius, checklist,
					boundarySegments, sigilSegments, sanguineBlobs, plantedStaff, owner,
					cancellationTicks, staffPlantingTicks,
					fogProfile, fogLightning, boundaryDome, phaseTicks).withAnchorStates(buf.readVarIntArray())
					.withEscalation(buf.readVarInt(), buf.readVarInt())
					.withGuidance(readProblems(buf), readSocketMarkers(buf))
					.withRings(buf.readVarInt(), buf.readVarIntArray())
					.withSubject(readPositions(buf), buf.readVarIntArray(), buf.readFloat()));
		}
		return new PacketSyncActiveRites(entries);
	}

	private static final int MAX_SYNCED_PROBLEMS = 16;
	private static final int MAX_SYNCED_MARKERS = 32;

	private static List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> readProblems(
			FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String key = buf.readUtf();
			int argCount = buf.readVarInt();
			List<String> args = new ArrayList<>(Math.min(argCount, 8));
			for (int arg = 0; arg < argCount; arg++) args.add(buf.readUtf());
			if (i < MAX_SYNCED_PROBLEMS) {
				problems.add(new com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem(key, args));
			}
		}
		return problems;
	}

	private static List<ActiveRiteData.SocketMarker> readSocketMarkers(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<ActiveRiteData.SocketMarker> markers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			BlockPos pos = buf.readBlockPos();
			String sigil = buf.readUtf();
			if (i < MAX_SYNCED_MARKERS) markers.add(new ActiveRiteData.SocketMarker(pos, sigil));
		}
		return markers;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
