package com.vincenthuto.hemomancy.common.item.tool;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class FungalSpineItem extends Item {

	public static final ResourceKey<Level> FUNGAL_GARDENS = ResourceKey.create(Registries.DIMENSION,
			Hemomancy.rloc("fungal_gardens"));

	public FungalSpineItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			boolean inFungalGardens = level.dimension().equals(FUNGAL_GARDENS);

			ServerLevel destination;
			if (inFungalGardens) {
				destination = serverPlayer.server.getLevel(Level.OVERWORLD);
			} else {
				destination = serverPlayer.server.getLevel(FUNGAL_GARDENS);
			}

			if (destination != null) {
				// Use the player's current X/Z in the destination dimension
				int x = (int) player.getX();
				int z = (int) player.getZ();

				ChunkPos chunkPos = new ChunkPos(new BlockPos(x, 0, z));
				destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1,
						serverPlayer.getId());

				BlockPos targetPos = findSafePos(destination, x, z);
				serverPlayer.teleportTo(destination, targetPos.getX() + 0.5, targetPos.getY(),
						targetPos.getZ() + 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());

				player.getCooldowns().addCooldown(this, 100);
			}
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	private BlockPos findSafePos(ServerLevel destination, int x, int z) {
		// Force chunk generation
		destination.getChunk(x >> 4, z >> 4);

		// Try heightmap first
		int y = destination.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		int minY = destination.getMinBuildHeight();
		int maxY = destination.getMaxBuildHeight();

		// If heightmap returned a valid position, use it
		if (y > minY && y < maxY) {
			return new BlockPos(x, y, z);
		}

		// Otherwise scan upward to find solid ground with 2 air blocks above
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

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("A calcified tendril from somewhere deep below. It hums faintly, as if listening.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}

