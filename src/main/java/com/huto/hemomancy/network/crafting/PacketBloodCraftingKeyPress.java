package com.huto.hemomancy.network.crafting;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;
import com.huto.hemomancy.recipes.BaseBloodCraftingRecipe;
import com.huto.hemomancy.recipes.ModBloodCraftingRecipes;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketBloodCraftingKeyPress {

	public ItemStack heldStack;

	public PacketBloodCraftingKeyPress(ItemStack stack) {
		this.heldStack = stack;
	}

	public static PacketBloodCraftingKeyPress decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketBloodCraftingKeyPress(buffer.readItemStack());
	}

	public static void encode(final PacketBloodCraftingKeyPress message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeItemStack(message.heldStack);
	}

	public static BaseBloodCraftingRecipe getMatchingRecipe(ItemStack stack) {
		for (BaseBloodCraftingRecipe recipe : ModBloodCraftingRecipes.RECIPES) {
			if (recipe.getHeldItem() == stack.getItem()) {
				return recipe;
			}
		}
		return null;
	}

	public static void handle(final PacketBloodCraftingKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);

			BaseBloodCraftingRecipe targetPattern = getMatchingRecipe(message.heldStack);
			ServerWorld sWorld = (ServerWorld) ctx.get().getSender().world;
			if (player.getHeldItemMainhand().getItem() == targetPattern.getHeldItem()) {
				if (bloodVolume.getBloodVolume() > targetPattern.getCost()) {
					RayTraceResult rayTrace = player.pick(3, 102, false);
					if (rayTrace.getType() == RayTraceResult.Type.BLOCK) {
						BlockRayTraceResult blockResult = (BlockRayTraceResult) rayTrace;
						BlockPos hitPos = blockResult.getPos();
						Block hitBlock = sWorld.getBlockState(hitPos).getBlock();
						if (hitBlock == targetPattern.getHitBlock()) {
							BlockPattern.PatternHelper patternHelper = targetPattern.getBundledPattern()
									.getBlockPattern().match(sWorld, hitPos);
							if (patternHelper != null) {
								for (int i = 0; i < targetPattern.getBundledPattern().getBlockPattern()
										.getPalmLength(); ++i) {
									for (int j = 0; j < targetPattern.getBundledPattern().getBlockPattern()
											.getThumbLength(); ++j) {
										for (int k = 0; k < targetPattern.getBundledPattern().getBlockPattern()
												.getFingerLength(); ++k) {
											CachedBlockInfo cachedBlockInfo = patternHelper.translateOffset(i, j, k);
											sWorld.setBlockState(cachedBlockInfo.getPos(), Blocks.AIR.getDefaultState(),
													2);
											sWorld.playEvent(2001, cachedBlockInfo.getPos(),
													Block.getStateId(cachedBlockInfo.getBlockState()));
										}
									}
								}
								sWorld.playSound(hitPos.getX(), hitPos.getY(), hitPos.getZ(),
										SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.BLOCKS, 1, 1, true);
								sWorld.addEntity(new ItemEntity(sWorld, hitPos.getX(), hitPos.getY(), hitPos.getZ(),
										new ItemStack(targetPattern.getCreation())));
								ItemStack oldStack = player.getHeldItemMainhand().copy();
								player.setHeldItem(Hand.MAIN_HAND,
										new ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
								bloodVolume.subtractBloodVolume(targetPattern.getCost());
								PacketHandler.CHANNELBLOODVOLUME.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
										new BloodVolumePacketServer(bloodVolume.getBloodVolume()));
							}
						}
					}
				} else {
					player.sendStatusMessage(new StringTextComponent("Not enough blood can be drawn for formation"),
							true);
					sWorld.playSound(player.getPosition().getX(), player.getPosition().getY(),
							player.getPosition().getZ(), SoundEvents.ENTITY_ENDERMAN_SCREAM, null, 1f, 1f, false);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}