package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFungalProjection;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.PriorRespawn;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;

import javax.annotation.Nullable;

public final class FungalGardenTravelHelper {
	private static final BloodAccounting.Source TRAVEL = BloodAccounting.Source.of("fungal_gardens_travel", Category.WORLD, false);
	public static final double TRAVEL_BLOOD_COST = 500.0;
	public static final int MIN_DEGREE = 7;
	private static final int GROUND_CEILING_Y = 160;
	private static final int SKY_GAP = 16;
	private static final int FLOATING_FLOOR_Y = 72;
	private static final int GROUND_SEARCH_RADIUS = 64;
	private static final int GROUND_SEARCH_STEP = 16;
	public static final int TRAVEL_COOLDOWN = 100;

	public static final String RETURN_X = "hemomancy:fungal_return_x";
	public static final String RETURN_Y = "hemomancy:fungal_return_y";
	public static final String RETURN_Z = "hemomancy:fungal_return_z";
	public static final String RETURN_YROT = "hemomancy:fungal_return_yrot";
	public static final String RETURN_XROT = "hemomancy:fungal_return_xrot";
	public static final String RETURN_DIMENSION = "hemomancy:fungal_return_dimension";
	public static final String PROJECTION_ACTIVE = "hemomancy:fungal_projection_active";
	public static final String PROJECTION_REMAINING = "hemomancy:fungal_projection_remaining";
	public static final String REVELATION_CHOICE_PENDING = "hemomancy:fungal_revelation_choice_pending";

	/** Old saves only: nothing writes it since F-21; PlayerDataMigrations reads it once (sunset 2027-04-23). */
	public static final String ARCHON_CHOICE_KEY = "hemomancy:archon_choice_made";
	public static final String ARCHON_CHOICE_SILENCE = "silent";
	public static final String ARCHON_CHOICE_APOTHEOS = "apotheos";

	/** The value the old Archon choice key held for a path, for the recipe gates that still name the key. */
	public static String legacyArchonChoice(EnumArchonPath path) {
		return switch (path) {
			// Silent Archon power is Silentium's alone; a Silence still pending (D7) has none.
			case SILENTIUM -> ARCHON_CHOICE_SILENCE;
			case APOTHEOS, APOTHEOS_PENDING -> ARCHON_CHOICE_APOTHEOS;
			default -> "";
		};
	}

	public static final ResourceKey<Level> FUNGAL_GARDENS = ResourceKey.create(Registries.DIMENSION,
			Hemomancy.rloc("fungal_gardens"));

	private FungalGardenTravelHelper() {
	}

	public static InteractionResult handleTravelUse(ServerPlayer player, Item cooldownItem) {
		return handleTravelUse(player, cooldownItem, false);
	}

	/**
	 * The Spine or a Podium ({@code bySpine} false) makes one crossing in, the first; after it either only brings its
	 * user home from the Gardens.
	 */
	public static InteractionResult handleTravelUse(ServerPlayer player, Item cooldownItem, boolean bySpine) {
		boolean inFungalGardens = player.level().dimension().equals(FUNGAL_GARDENS);

		if (!inFungalGardens && player.isCreative()) {
			storeReturnPosition(player);
			performFungalGardensTravel(player);
		} else if (inFungalGardens) {
			if (isProjectionActive(player)) {
				player.displayClientMessage(Component.translatable("message.hemomancy.fungal_garden_travel_helper.you_have_no_hands_here_the").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}
			returnHome(player);
		} else {
			if (isRevelationChoicePending(player)) {
				PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.coreWitnessDialogue()));
				return InteractionResult.SUCCESS;
			}
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (degree < MIN_DEGREE) {
				player.displayClientMessage(
						Component.translatable("message.hemomancy.fungal_garden_travel_helper.the_spine_still_needs_the_shape").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}
			var degreeState = HemoCapabilityAccess.getInitiatoryDegree(player);
			if (!degreeState.hasFungalSpineGranted()) {
				player.displayClientMessage(Component.translatable("message.hemomancy.fungal_garden_travel_helper.you_have_found_the_wrong_spine").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}

			FungalProjectionRules.TravelKind travel = FungalProjectionRules.travelKind(
					degreeState.hasWitnessedFungalRevelation(), degree, degreeState.hasFungalSpineGranted());
			if (travel == FungalProjectionRules.TravelKind.DORMANT) {
				boolean apotheos = degreeState.getArchonPath() == EnumArchonPath.APOTHEOS;
				String dormant = bySpine
						? apotheos ? "message.hemomancy.fungal_garden_travel_helper.spine_dormant_visitation"
								: "message.hemomancy.fungal_garden_travel_helper.spine_dormant"
						: apotheos ? "message.hemomancy.fungal_garden_travel_helper.podium_dormant_visitation"
								: "message.hemomancy.fungal_garden_travel_helper.podium_dormant";
				player.displayClientMessage(Component.translatable(dormant).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}
			if (travel == FungalProjectionRules.TravelKind.REFUSED) {
				player.displayClientMessage(Component.translatable("message.hemomancy.fungal_garden_travel_helper.the_spine_will_not_carry_you").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
				return InteractionResult.SUCCESS;
			}

			if (!drainTravelBlood(player)) {
				player.displayClientMessage(
						Component.translatable("message.hemomancy.fungal_garden_travel_helper.your_veins_run_too_thin_to").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			storeReturnPosition(player);
			startFirstProjection(player);
		}

		player.getCooldowns().addCooldown(cooldownItem, TRAVEL_COOLDOWN);
		return InteractionResult.SUCCESS;
	}

	/**
	 * Whether the Archon's revelation choice waits: the root flag, or (once a death cleared the root key) a witnessed
	 * revelation with no path chosen after Communion and nine pomes (LF-5).
	 */
	public static boolean isRevelationChoicePending(ServerPlayer player) {
		if (player.getPersistentData().getBoolean(REVELATION_CHOICE_PENDING)) return true;
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		return degree.hasWitnessedFungalRevelation() && degree.getArchonPath() == EnumArchonPath.NONE
				&& degree.isQliphothCommunionDone() && degree.getTotalPomesConsumed() >= 9;
	}

	public static boolean isProjectionActive(ServerPlayer player) {
		return player.getPersistentData().getBoolean(PROJECTION_ACTIVE);
	}

	public static int getProjectionRemainingTicks(ServerPlayer player) {
		return player.getPersistentData().getInt(PROJECTION_REMAINING);
	}

	/** The first projection's state as the client shows it: the time left, or none. */
	public static void syncProjection(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncFungalProjection(isProjectionActive(player),
				isProjectionActive(player) ? getProjectionRemainingTicks(player) : 0, FungalProjectionRules.FIRST_VISIT_TICKS));
	}

	public static void startFirstProjection(ServerPlayer player) {
		var data = player.getPersistentData();
		data.putBoolean(PROJECTION_ACTIVE, true);
		data.putInt(PROJECTION_REMAINING, FungalProjectionRules.FIRST_VISIT_TICKS);
		performFungalGardensTravel(player);
		PacketHandler.sendToPlayer(player, new PacketSyncFungalProjection(true,
				FungalProjectionRules.FIRST_VISIT_TICKS, FungalProjectionRules.FIRST_VISIT_TICKS));
		player.displayClientMessage(Component.translatable("message.hemomancy.fungal_garden_travel_helper.your_body_remains_behind_you_have").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	public static void performForcedProjectionReturn(ServerPlayer player) {
		if (!isProjectionActive(player)) return;
		player.getPersistentData().remove(PROJECTION_ACTIVE);
		player.getPersistentData().remove(PROJECTION_REMAINING);
		player.getPersistentData().putBoolean(REVELATION_CHOICE_PENDING, true);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setFungalRevelationWitnessed(true);
		InitiatoryDegreeEvents.syncDegree(player, degree);
		performReturnTravel(player);
		PacketHandler.sendToPlayer(player, new PacketSyncFungalProjection(false, 0,
				FungalProjectionRules.FIRST_VISIT_TICKS));
		PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.coreWitnessDialogue()));
	}

	public static void storeReturnPosition(ServerPlayer player) {
		var data = player.getPersistentData();
		data.putDouble(RETURN_X, player.getX());
		data.putDouble(RETURN_Y, player.getY());
		data.putDouble(RETURN_Z, player.getZ());
		data.putFloat(RETURN_YROT, player.getYRot());
		data.putFloat(RETURN_XROT, player.getXRot());
		data.putString(RETURN_DIMENSION, player.level().dimension().location().toString());
	}

	public static boolean drainTravelBlood(ServerPlayer player) {
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || volume.getBloodVolume() < TRAVEL_BLOOD_COST) return false;
		BloodAccounting.pay(player, TRAVEL_BLOOD_COST, TRAVEL);
		return true;
	}

	public static void performFungalGardensTravel(ServerPlayer player) {
		ServerLevel destination = player.server.getLevel(FUNGAL_GARDENS);
		if (destination == null) return;

		int x = (int) player.getX();
		int z = (int) player.getZ();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(x, 0, z));
		destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

		BlockPos targetPos = findSafePos(destination, x, z);
		player.teleportTo(destination, targetPos.getX() + 0.5, targetPos.getY(),
				targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());

		player.displayClientMessage(
				Component.translatable("message.hemomancy.fungal_garden_travel_helper.the_spine_opens_like_a_wound").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * The way out of the Gardens, free as the Spine's return always was (PT-80, NQ-12-4): the stored return point; with
	 * none (a death clears it), the respawn the player had before their heart-node, or their own respawn when it lies
	 * outside the Gardens; where that bed or anchor is gone, or there was none, the world spawn.
	 */
	public static void returnHome(ServerPlayer player) {
		if (player.getPersistentData().contains(RETURN_X)) {
			performReturnTravel(player);
			return;
		}
		player.changeDimension(homeTransition(player, homeRespawn(player)));
		player.displayClientMessage(
				Component.translatable("message.hemomancy.fungal_garden_travel_helper.the_membrane_closes_behind_you_you").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	/** Where a player with no stored return point goes home to, before the bed is checked; null for the world spawn. */
	@Nullable
	public static PriorRespawn homeRespawn(ServerPlayer player) {
		ServerLevel gardens = player.server.getLevel(FUNGAL_GARDENS);
		HeartNodeEntry node = gardens == null ? null : HeartNodeSavedData.get(gardens).entry(player.getUUID());
		if (node != null) return node.priorRespawn();
		PriorRespawn own = PriorRespawn.of(player);
		return own == null || own.dimension().equals(FUNGAL_GARDENS) ? null : own;
	}

	/** Vanilla's own respawn search, run on {@code home} (beds, anchors, a forced point), with the world spawn after. */
	public static DimensionTransition homeTransition(ServerPlayer player, @Nullable PriorRespawn home) {
		if (home == null) return new DimensionTransition(player.server.overworld(), player, DimensionTransition.DO_NOTHING);
		PriorRespawn current = PriorRespawn.of(player);
		PriorRespawn.restore(player, home);
		DimensionTransition way = player.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
		PriorRespawn.restore(player, current);
		return way;
	}

	public static void performReturnTravel(ServerPlayer player) {
		var data = player.getPersistentData();
		ResourceLocation returnDimension = ResourceLocation.tryParse(data.getString(RETURN_DIMENSION));
		ResourceKey<Level> returnKey = returnDimension == null
				? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, returnDimension);
		ServerLevel returnLevel = player.server.getLevel(returnKey);
		if (returnLevel == null) returnLevel = player.server.getLevel(Level.OVERWORLD);
		if (returnLevel == null) return;
		double rx = data.getDouble(RETURN_X);
		double ry = data.getDouble(RETURN_Y);
		double rz = data.getDouble(RETURN_Z);
		float yRot = data.getFloat(RETURN_YROT);
		float xRot = data.getFloat(RETURN_XROT);

		BlockPos returnPos = new BlockPos((int) rx, (int) ry, (int) rz);
		ChunkPos chunkPos = new ChunkPos(returnPos);
		returnLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

		player.teleportTo(returnLevel, rx, ry, rz, yRot, xRot);

		data.remove(RETURN_X);
		data.remove(RETURN_Y);
		data.remove(RETURN_Z);
		data.remove(RETURN_YROT);
		data.remove(RETURN_XROT);
		data.remove(RETURN_DIMENSION);

		player.displayClientMessage(
				Component.translatable("message.hemomancy.fungal_garden_travel_helper.the_membrane_closes_behind_you_you").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	public static BlockPos findSafePos(ServerLevel destination, int x, int z) {
		// Prefer dry ground under the floating islands and spire tips, searching outward off the sea.
		for (int radius = 0; radius <= GROUND_SEARCH_RADIUS; radius += GROUND_SEARCH_STEP) {
			int samples = radius == 0 ? 1 : 8;
			for (int i = 0; i < samples; i++) {
				double angle = Math.PI * 2 * i / samples;
				BlockPos ground = findGround(destination, x + (int) Math.round(Math.cos(angle) * radius),
						z + (int) Math.round(Math.sin(angle) * radius));
				if (ground != null) return ground;
			}
		}

		destination.getChunk(x >> 4, z >> 4);

		int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int minY = destination.getMinBuildHeight();
		int maxY = destination.getMaxBuildHeight();

		if (y > minY && y < maxY) {
			return new BlockPos(x, y, z);
		}

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, minY, z);
		for (int scanY = minY; scanY < maxY - 1; scanY++) {
			mutable.setY(scanY);
			boolean solidBelow = !destination.getBlockState(mutable).isAir();
			mutable.setY(scanY + 1);
			boolean airAtFeet = destination.getBlockState(mutable).isAir();
			mutable.setY(scanY + 2);
			boolean airAtHead = destination.getBlockState(mutable).isAir();

			if (solidBelow && airAtFeet && airAtHead) {
				return new BlockPos(x, scanY + 1, z);
			}
		}

		return new BlockPos(x, 64, z);
	}

	private static BlockPos findGround(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		int top = Math.min(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), GROUND_CEILING_Y);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, top, z);
		for (int y = top; y > level.getMinBuildHeight() + 1; y--) {
			cursor.set(x, y, z);
			if (!level.getBlockState(cursor).getFluidState().isEmpty()) return null;
			if (!standable(level, cursor)) continue;
			// Floating islands hang over open air: look for the ground beneath them.
			if (y > FLOATING_FLOOR_Y && hangsOverSky(level, x, y - 1, z)) continue;
			// The first real surface decides; a needle-thin spire tip sends the search to the next column.
			return broadFooting(level, x, y - 1, z) ? new BlockPos(x, y, z) : null;
		}
		return null;
	}

	private static boolean standable(ServerLevel level, BlockPos.MutableBlockPos feet) {
		BlockState floor = level.getBlockState(feet.below());
		return floor.blocksMotion() && floor.getFluidState().isEmpty() && level.getBlockState(feet).isAir()
				&& level.getBlockState(feet.above()).isAir();
	}

	private static boolean broadFooting(ServerLevel level, int x, int y, int z) {
		return level.getBlockState(new BlockPos(x + 1, y, z)).blocksMotion()
				&& level.getBlockState(new BlockPos(x - 1, y, z)).blocksMotion()
				&& level.getBlockState(new BlockPos(x, y, z + 1)).blocksMotion()
				&& level.getBlockState(new BlockPos(x, y, z - 1)).blocksMotion();
	}

	private static boolean hangsOverSky(ServerLevel level, int x, int floorY, int z) {
		int y = floorY;
		while (y > level.getMinBuildHeight() && !level.getBlockState(new BlockPos(x, y, z)).isAir()) y--;
		int gap = 0;
		while (y > level.getMinBuildHeight() && level.getBlockState(new BlockPos(x, y, z)).isAir()) {
			if (++gap >= SKY_GAP) return true;
			y--;
		}
		return false;
	}
}
