package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.event.ModBlockPatterns;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketBookCraftingKeyPress {
	public static PacketBookCraftingKeyPress decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketBookCraftingKeyPress();
	}

	public static void encode(final PacketBookCraftingKeyPress message, final PacketBuffer buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketBookCraftingKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			ServerWorld sWorld = (ServerWorld) ctx.get().getSender().world;
			if (player.getHeldItemMainhand().getItem() instanceof AxeItem) {
				if (bloodVolume.getBloodVolume() > 100) {
					RayTraceResult rayTrace = player.pick(3, 102, false);
					if (rayTrace.getType() == RayTraceResult.Type.BLOCK) {
						BlockRayTraceResult blockResult = (BlockRayTraceResult) rayTrace;
						BlockPos hitPos = blockResult.getPos();
						Block hitBlock = sWorld.getBlockState(hitPos).getBlock();
						if (hitBlock == Blocks.BOOKSHELF) {
							BlockPattern.PatternHelper patternHelper = ModBlockPatterns.getBookPatten().match(sWorld,
									hitPos);
							if (patternHelper != null) {
								for (int i = 0; i < ModBlockPatterns.getBookPatten().getPalmLength(); ++i) {
									for (int j = 0; j < ModBlockPatterns.getBookPatten().getThumbLength(); ++j) {
										for (int k = 0; k < ModBlockPatterns.getBookPatten().getFingerLength(); ++k) {
											CachedBlockInfo cachedBlockInfo = patternHelper.translateOffset(i, j, k);
											sWorld.setBlockState(cachedBlockInfo.getPos(), Blocks.AIR.getDefaultState(),
													2);
											sWorld.playEvent(2001, cachedBlockInfo.getPos(),
													Block.getStateId(cachedBlockInfo.getBlockState()));
										}
									}
								}
								ClientEventSubscriber.getClientPlayer().playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 1,
										1);
								sWorld.addEntity(new ItemEntity(sWorld, hitPos.getX(), hitPos.getY(), hitPos.getZ(),
										new ItemStack(ItemInit.liber_sanguinum.get())));
								bloodVolume.subtractBloodVolume(100);
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