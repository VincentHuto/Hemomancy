package com.huto.hemomancy.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketBloodFormationKeyPress {
	public static PacketBloodFormationKeyPress decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketBloodFormationKeyPress();
	}

	public static void encode(final PacketBloodFormationKeyPress message, final PacketBuffer buffer) {
		buffer.writeByte(0);
	}

	public static boolean checkStructure(World world, BlockState state, BlockPos centerPos) {
		List<BlockPos> circlePos = new ArrayList<>();
		BlockPos ul = centerPos.add(new Vector3i(-1, 0, 1));
		BlockPos um = centerPos.add(new Vector3i(0, 0, 1));
		BlockPos ur = centerPos.add(new Vector3i(1, 0, 1));
		BlockPos ml = centerPos.add(new Vector3i(-1, 0, 0));
		BlockPos mr = centerPos.add(new Vector3i(1, 0, 0));
		BlockPos bl = centerPos.add(new Vector3i(1, 0, -1));
		BlockPos bm = centerPos.add(new Vector3i(0, 0, -1));
		BlockPos br = centerPos.add(new Vector3i(-1, 0, -1));
		Collections.addAll(circlePos, ul, um, ur, ml, mr, bl, bm, br);
		for (BlockPos currentPos : circlePos) {
			if (world.getBlockState(currentPos).getBlock() != Blocks.REDSTONE_WIRE) {
				System.out.println(world.getBlockState(currentPos).getBlock());
				return false;
			}
		}
		for (BlockPos currentPos : circlePos) {
			if (world.getBlockState(currentPos).getBlock() == Blocks.REDSTONE_WIRE) {
				Random random = world.rand;
				ServerWorld sWorld = (ServerWorld) world;
				for (int i = 0; i < 3; i++) {
					sWorld.spawnParticle(RedstoneParticleData.REDSTONE_DUST, currentPos.getX() + random.nextDouble(),
							currentPos.getY() + random.nextDouble(), currentPos.getZ() + random.nextDouble(), 3, 0f,
							0.2f, 0f, 2);
					sWorld.spawnParticle(ParticleTypes.ASH, currentPos.getX() + random.nextDouble(),
							currentPos.getY() + random.nextDouble(), currentPos.getZ() + random.nextDouble(), 1, 0f,
							0.2f, 0f, 2);
				}
				world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
			}
		}
		return true;
	}

	public static void handle(final PacketBloodFormationKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
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
							if (checkStructure(sWorld, sWorld.getBlockState(hitPos), hitPos)) {
								sWorld.setBlockState(hitPos, Blocks.AIR.getDefaultState());
								sWorld.addEntity(new ItemEntity(sWorld, hitPos.getX(), hitPos.getY(), hitPos.getZ(),
										new ItemStack(ItemInit.liber_sanguinum.get())));
								bloodVolume.subtractBloodVolume(100);
								PacketHandler.CHANNELBLOODVOLUME.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
										new BloodVolumePacketServer(bloodVolume.getBloodVolume()));
							}
						}
					}
				}
			}
			if (player.getHeldItemMainhand().getItem() instanceof SwordItem) {
				if (bloodVolume.getBloodVolume() > 500) {
					player.sendStatusMessage(new StringTextComponent("Blood has been drawn for a greater cause"), true);
					bloodVolume.subtractBloodVolume(500);
					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
							new BloodVolumePacketServer(bloodVolume.getBloodVolume()));
					BlockPos pos = player.getPosition();
					Random random = player.world.rand;
					for (int i = 0; i < 30; i++) {
						sWorld.spawnParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX() + random.nextDouble(),
								pos.getY() + random.nextDouble() + 1, pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f,
								2);
						sWorld.spawnParticle(ParticleTypes.ASH, pos.getX() + random.nextDouble(),
								pos.getY() + random.nextDouble() + 1, pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f,
								2);
					}
					player.dropItem(new ItemStack(ItemInit.sanguine_formation.get(), random.nextInt(4)), false);
				} else {
					player.sendStatusMessage(new StringTextComponent("Not enough blood can be drawn for formation"),
							true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}