package com.vincenthuto.hemomancy.common.network;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

public class PlaceStructurePacket {

	public enum StructureType {
		BLOOD_STRUCTURE,
		CARDINAL_RITE
	}

	private final ResourceLocation recipeId;
	private final StructureType type;

	public PlaceStructurePacket(ResourceLocation recipeId, StructureType type) {
		this.recipeId = recipeId;
		this.type = type;
	}

	public static void encode(PlaceStructurePacket msg, FriendlyByteBuf buf) {
		buf.writeResourceLocation(msg.recipeId);
		buf.writeEnum(msg.type);
	}

	public static PlaceStructurePacket decode(FriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		StructureType type = buf.readEnum(StructureType.class);
		return new PlaceStructurePacket(id, type);
	}

	public static void handle(PlaceStructurePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			// Creative-only check
			if (!player.isCreative()) {
				player.sendSystemMessage(Component.literal("§cStructure Spawner requires creative mode!"));
				return;
			}

			ServerLevel level = player.serverLevel();
			MultiblockPattern pattern = null;

			if (msg.type == StructureType.BLOOD_STRUCTURE) {
				BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(level, msg.recipeId);
				if (recipe != null) {
					pattern = recipe.getPattern();
				}
			} else if (msg.type == StructureType.CARDINAL_RITE) {
				CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, msg.recipeId);
				if (recipe != null) {
					pattern = recipe.getPattern();
				}
			}

			if (pattern == null) {
				player.sendSystemMessage(Component.literal("§cCould not find recipe: " + msg.recipeId));
				return;
			}

			// Get structure dimensions for centering
			List<BlockPosBlockPair> blockPairs = pattern.getBlockPosBlockList();
			if (blockPairs.isEmpty()) {
				player.sendSystemMessage(Component.literal("§cRecipe has no blocks to place!"));
				return;
			}

			// Find bounding box
			int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
			int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
			for (BlockPosBlockPair pair : blockPairs) {
				BlockPos pos = pair.getPos();
				minX = Math.min(minX, pos.getX());
				maxX = Math.max(maxX, pos.getX());
				minZ = Math.min(minZ, pos.getZ());
				maxZ = Math.max(maxZ, pos.getZ());
			}

			// Center offsets
			int centerX = (minX + maxX) / 2;
			int centerZ = (minZ + maxZ) / 2;

			BlockPos playerPos = player.blockPosition();
			int placed = 0;

			for (BlockPosBlockPair pair : blockPairs) {
				Block block = pair.getBlock();
				if (block == null) continue; // Skip air symbols

				BlockPos relativePos = pair.getPos();
				BlockPos worldPos = playerPos.offset(
						relativePos.getX() - centerX,
						relativePos.getY(),
						relativePos.getZ() - centerZ
				);

				level.setBlockAndUpdate(worldPos, block.defaultBlockState());
				placed++;
			}

			player.sendSystemMessage(Component.literal("§aPlaced " + placed + " blocks for: " + msg.recipeId.getPath()));
		});
		ctx.get().setPacketHandled(true);
	}
}
