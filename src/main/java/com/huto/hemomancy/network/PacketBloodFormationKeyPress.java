package com.huto.hemomancy.network;

import java.util.Random;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
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

	public static void handle(final PacketBloodFormationKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			ServerWorld sWorld = (ServerWorld) ctx.get().getSender().world;
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