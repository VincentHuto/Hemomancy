package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.network.rite.RiteSpaceSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Rite spaces: made when a Harbinger rite completes on a formed floor, ended when that floor stops being formed at
 * its Focus or the Focus is removed. {@link #spaceAt} answers on both sides, the client from the last sync.
 */
public final class RiteSpaces {
	private static volatile boolean changed;

	private RiteSpaces() {}

	/** Consecrates the cube above the floor a Harbinger rite just completed on. Unstained rites leave no space. */
	public static void consecrate(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.isUnstained() || recipe.getPurpose() == null) return;
		// A completion effect may have taken the Focus (Rooted Vein turns it to venous stone): with no Focus left to
		// run the floor check, the space could never be released.
		if (!level.getBlockState(rite.getCenterPos()).is(BlockInit.cardinal_focus.get())) return;
		ResourceLocation floorId = rite.getMatchedFloorId();
		CardinalRiteFloorDefinition floor = floorId == null ? null : CardinalRiteFloorRegistry.get(floorId).orElse(null);
		if (floor == null) return;
		RiteSpace space = new RiteSpace(rite.getCenterPos().immutable(), level.dimension(), rite.getPlayerUUID(),
				recipe.getPurpose(), floorId, floor.half(), recipe.getId(), level.getGameTime());
		RiteSpaceSavedData.get(level.getServer().overworld()).consecrate(space);
	}

	/** The Focus's periodic floor check: a space whose floor is no longer the one formed there is released. */
	public static void onFloorCheck(ServerLevel level, BlockPos focus, ResourceLocation formedFloor) {
		RiteSpaceSavedData data = RiteSpaceSavedData.get(level.getServer().overworld());
		Optional<RiteSpace> space = data.atFocus(level.dimension(), focus);
		if (space.isEmpty() || !RiteSpaceRules.shouldRelease(space.get().floor(), formedFloor)) return;
		data.release(level.dimension(), focus);
		level.playSound(null, focus, SoundInit.RITE_ANCHOR_BREAK.get(), SoundSource.BLOCKS, 0.4F, 0.55F);
	}

	/** The Focus block itself is gone. */
	public static void release(Level level, BlockPos focus) {
		if (level instanceof ServerLevel server) {
			RiteSpaceSavedData.get(server.getServer().overworld()).release(server.dimension(), focus);
		}
	}

	/** The rite space covering a position. */
	public static Optional<RiteSpace> spaceAt(ServerLevel level, BlockPos pos) {
		return RiteSpaceSavedData.get(level.getServer().overworld()).spaceAt(pos, level.dimension());
	}

	/** The purpose of the rite space covering a position, on either side: the client answers from the last sync. */
	public static Optional<CardinalRitePurpose> purposeAt(Level level, BlockPos pos) {
		if (level instanceof ServerLevel server) return spaceAt(server, pos).map(RiteSpace::purpose);
		return level.isClientSide() ? ClientBridge.get().riteSpacePurposeAt(pos) : Optional.empty();
	}

	static void markChanged() {
		changed = true;
	}

	static boolean consumeChanged() {
		boolean was = changed;
		changed = false;
		return was;
	}

	public static void broadcast(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendTo(player, player.level().dimension());
		}
	}

	public static void sendTo(ServerPlayer player, ResourceKey<Level> dimension) {
		List<RiteSpaceSyncPacket.Space> spaces = new ArrayList<>();
		for (RiteSpace space : RiteSpaceSavedData.get(player.server.overworld()).spacesIn(dimension)) {
			if (spaces.size() >= RiteSpaceSyncPacket.MAX_SPACES) break;
			spaces.add(RiteSpaceSyncPacket.Space.of(space));
		}
		PacketHandler.sendToPlayer(player, new RiteSpaceSyncPacket(spaces));
	}
}
