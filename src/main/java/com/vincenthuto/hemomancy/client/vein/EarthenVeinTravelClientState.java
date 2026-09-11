package com.vincenthuto.hemomancy.client.vein;

import com.vincenthuto.hemomancy.common.network.vein.EarthenVeinTravelVisualPacket;
import com.vincenthuto.hemomancy.common.network.vein.OpenEarthenVeinDisplayPacket;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinDestination;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class EarthenVeinTravelClientState {
	private static final EarthenVeinTravelVisuals VISUALS = new EarthenVeinTravelVisuals();
	@Nullable private static Selection selection;
	private static boolean localCinematic;

	private EarthenVeinTravelClientState() {
	}

	public static void open(OpenEarthenVeinDisplayPacket packet) {
		Minecraft minecraft = Minecraft.getInstance();
		long openedAt = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		selection = new Selection(packet.nonce(), packet.sourceDimension(), packet.source(), packet.destinations(), openedAt);
	}

	public static void update(EarthenVeinTravelVisualPacket packet) {
		VISUALS.update(packet.dimension(), packet.vein(), packet.travelerEntityId(), packet.phase(), packet.progress());
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null && packet.travelerEntityId() == minecraft.player.getId()) {
			if (packet.phase() != EarthenVeinTravelPhase.SELECTING) selection = null;
			localCinematic = packet.phase() == EarthenVeinTravelPhase.SWALLOWING
					|| packet.phase() == EarthenVeinTravelPhase.EJECTING;
		}
	}

	@Nullable public static Selection selection() { return selection; }

	@Nullable
	public static EarthenVeinTravelVisuals.Visual visual(ResourceLocation dimension, BlockPos pos) {
		return VISUALS.visual(dimension, pos);
	}

	public static boolean isLocalCinematic() { return localCinematic; }

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) {
			selection = null;
			VISUALS.clear();
			localCinematic = false;
			return;
		}
		if (selection != null && (!minecraft.level.dimension().location().equals(selection.sourceDimension)
				|| minecraft.player.distanceToSqr(selection.source.getX() + 0.5D,
				selection.source.getY() + 0.5D, selection.source.getZ() + 0.5D) > 64.0D
				|| minecraft.level.getGameTime() - selection.openedAt > 600L)) selection = null;
		ResourceLocation dimension = minecraft.level.dimension().location();
		VISUALS.retainDimension(dimension);
	}

	public static void closeSelection() { selection = null; }

	public record Selection(UUID nonce, ResourceLocation sourceDimension, BlockPos source,
			List<EarthenVeinDestination> destinations, long openedAt) {
		public Selection {
			source = source.immutable();
			destinations = List.copyOf(destinations);
		}
	}

}
