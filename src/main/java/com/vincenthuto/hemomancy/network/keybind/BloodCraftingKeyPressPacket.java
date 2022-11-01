package com.vincenthuto.hemomancy.network.keybind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodCraftingKeyPressPacket {

	public ItemStack heldStack;

	public BloodCraftingKeyPressPacket(ItemStack stack) {
		this.heldStack = stack;
	}

	public static BloodCraftingKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodCraftingKeyPressPacket(buffer.readItem());
	}

	public static void encode(final BloodCraftingKeyPressPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeItem(message.heldStack);
	}

	public static List<BloodStructureRecipe> getMatchingRecipes(ItemStack stack, Level level) {
		List<BloodStructureRecipe> matchedRecipes = new ArrayList<BloodStructureRecipe>();
		for (BloodStructureRecipe recipe : BloodStructureRecipe.getAllRecipes(level)) {
			if (recipe.getHeldItem().getItem() == stack.getItem()) {
				matchedRecipes.add(recipe);
			}
		}
		return matchedRecipes;
	}

	public static void handle(final BloodCraftingKeyPressPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;

			for (BloodStructureRecipe pattern : BloodStructureRecipe.getAllRecipes(player.level)) {
				if (player.getMainHandItem().getItem() == pattern.getHeldItem().getItem()) {
					IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					List<BloodStructureRecipe> matchedPatterns = getMatchingRecipes(message.heldStack, player.level);
					if (matchedPatterns != null) {
						if (!matchedPatterns.isEmpty()) {
							for (BloodStructureRecipe targetPattern : matchedPatterns) {
								ServerLevel sLevel = (ServerLevel) ctx.get().getSender().level;
								if (player.getMainHandItem().getItem() == targetPattern.getHeldItem().getItem()) {
									if (bloodVolume.getBloodVolume() > targetPattern.getBloodCost()) {
										HitResult rayTrace = player.pick(3, 102, false);
										if (rayTrace.getType() == HitResult.Type.BLOCK) {
											BlockHitResult blockResult = (BlockHitResult) rayTrace;
											BlockPos hitPos = blockResult.getBlockPos();
											Block hitBlock = sLevel.getBlockState(hitPos).getBlock();
											if (hitBlock == targetPattern.getHitBlock()) {
												BlockPattern.BlockPatternMatch patternHelper = targetPattern
														.getPattern().getBlockPattern().find(sLevel, hitPos);
												if (patternHelper != null) {
													for (int i = 0; i < targetPattern.getPattern().getBlockPattern()
															.getWidth(); ++i) {
														for (int j = 0; j < targetPattern.getPattern().getBlockPattern()
																.getHeight(); ++j) {
															for (int k = 0; k < targetPattern.getPattern()
																	.getBlockPattern().getDepth(); ++k) {
																BlockInWorld cachedBlockInfo = patternHelper.getBlock(i,
																		j, k);
																sLevel.setBlock(cachedBlockInfo.getPos(),
																		Blocks.AIR.defaultBlockState(), 2);
																sLevel.levelEvent(2001, cachedBlockInfo.getPos(),
																		Block.getId(cachedBlockInfo.getState()));
															}
														}
													}
													sLevel.playLocalSound(hitPos.getX(), hitPos.getY(), hitPos.getZ(),
															SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 1, 1,
															true);
													sLevel.addFreshEntity(new ItemEntity(sLevel, hitPos.getX(),
															hitPos.getY(), hitPos.getZ(), targetPattern.getResult()));
													ItemStack oldStack = player.getMainHandItem().copy();
													player.setItemInHand(InteractionHand.MAIN_HAND,
															new ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
													bloodVolume.drain(targetPattern.getBloodCost());
													PacketHandler.CHANNELBLOODVOLUME.send(
															PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
															new BloodVolumeServerPacket(bloodVolume));
												}
											}
										}
									} else {
										player.displayClientMessage(
												Component.literal("Not enough blood can be drawn for formation"), true);
										sLevel.playLocalSound(player.blockPosition().getX(),
												player.blockPosition().getY(), player.blockPosition().getZ(),
												SoundEvents.ENDERMAN_SCREAM, null, 1f, 1f, false);
									}
								}

							}
						}
					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}
}