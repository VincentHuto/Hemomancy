package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FungalGardenTravelHelper {
	public static final double TRAVEL_BLOOD_COST = 500.0;
	public static final int MIN_DEGREE = 2;
	public static final int TRAVEL_COOLDOWN = 100;

	public static final String RETURN_X = "hemomancy:fungal_return_x";
	public static final String RETURN_Y = "hemomancy:fungal_return_y";
	public static final String RETURN_Z = "hemomancy:fungal_return_z";
	public static final String RETURN_YROT = "hemomancy:fungal_return_yrot";
	public static final String RETURN_XROT = "hemomancy:fungal_return_xrot";

	public static final String ARCHON_CHOICE_KEY = "hemomancy:archon_choice_made";
	public static final String ARCHON_CHOICE_SILENCE = "silent";
	public static final String ARCHON_CHOICE_APOTHEOS = "apotheos";

	public static final ResourceKey<Level> FUNGAL_GARDENS = ResourceKey.create(Registries.DIMENSION,
			Hemomancy.rloc("fungal_gardens"));

	private FungalGardenTravelHelper() {
	}

	public static InteractionResult handleTravelUse(ServerPlayer player, Item cooldownItem) {
		boolean inFungalGardens = player.level().dimension().equals(FUNGAL_GARDENS);

		if (inFungalGardens) {
			if (!player.getPersistentData().contains(RETURN_X)) {
				player.displayClientMessage(
						Component.literal("The spine twitches, but finds no thread back to the surface world.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (degree == 7 && !player.getPersistentData().contains(ARCHON_CHOICE_KEY)) {
				PacketHandler.sendToPlayer(player,
						new OpenDialoguePacket(FungalWhisperDialogueTrees.coreWitnessDialogue()));
				player.getCooldowns().addCooldown(cooldownItem, TRAVEL_COOLDOWN);
				return InteractionResult.SUCCESS;
			}

			performReturnTravel(player);
		} else {
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (degree < MIN_DEGREE) {
				player.displayClientMessage(
						Component.literal("The mycelium rejects you - you are not yet sworn.")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			if (!drainTravelBlood(player)) {
				player.displayClientMessage(
						Component.literal("Your veins run too thin to feed the rift.")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						true);
				return InteractionResult.SUCCESS;
			}

			storeReturnPosition(player);
			performFungalGardensTravel(player);
		}

		player.getCooldowns().addCooldown(cooldownItem, TRAVEL_COOLDOWN);
		return InteractionResult.SUCCESS;
	}

	public static void storeReturnPosition(ServerPlayer player) {
		var data = player.getPersistentData();
		data.putDouble(RETURN_X, player.getX());
		data.putDouble(RETURN_Y, player.getY());
		data.putDouble(RETURN_Z, player.getZ());
		data.putFloat(RETURN_YROT, player.getYRot());
		data.putFloat(RETURN_XROT, player.getXRot());
	}

	public static boolean drainTravelBlood(ServerPlayer player) {
		var opt = HemoCapabilityAccess.getBloodVolume(player);
		if (!opt.isPresent()) return false;
		var volume = opt.orElseThrow(IllegalStateException::new);
		if (!volume.isActive() || volume.getBloodVolume() < TRAVEL_BLOOD_COST) return false;
		volume.drain(TRAVEL_BLOOD_COST);
		BloodVolumeEvents.syncVolume(player, volume);
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
				Component.literal("The spine opens like a wound. You descend into the dreaming root.")
						.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	public static void performReturnTravel(ServerPlayer player) {
		ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
		if (overworld == null) return;

		var data = player.getPersistentData();
		double rx = data.getDouble(RETURN_X);
		double ry = data.getDouble(RETURN_Y);
		double rz = data.getDouble(RETURN_Z);
		float yRot = data.getFloat(RETURN_YROT);
		float xRot = data.getFloat(RETURN_XROT);

		BlockPos returnPos = new BlockPos((int) rx, (int) ry, (int) rz);
		ChunkPos chunkPos = new ChunkPos(returnPos);
		overworld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

		player.teleportTo(overworld, rx + 0.5, ry, rz + 0.5, yRot, xRot);

		data.remove(RETURN_X);
		data.remove(RETURN_Y);
		data.remove(RETURN_Z);
		data.remove(RETURN_YROT);
		data.remove(RETURN_XROT);

		player.displayClientMessage(
				Component.literal("The membrane closes behind you. You surface from the blood-dream.")
						.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),
				false);
	}

	public static BlockPos findSafePos(ServerLevel destination, int x, int z) {
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
}
