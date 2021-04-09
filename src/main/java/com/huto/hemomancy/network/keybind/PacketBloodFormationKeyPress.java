package com.huto.hemomancy.network.keybind;

import java.util.Random;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.particle.factory.GlowParticleFactory;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketBuffer;
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
				if (bloodVolume.getBloodVolume() > 100) {
					player.sendStatusMessage(new StringTextComponent("Blood has been drawn for a greater cause"), true);
					bloodVolume.subtractBloodVolume(100);
					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
							new PacketBloodVolumeServer(bloodVolume.getMaxBloodVolume(), bloodVolume.getBloodVolume()));
					BlockPos pos = player.getPosition();
					Random random = player.world.rand;
					for (int i = 0; i < 30; i++) {
						sWorld.spawnParticle(
								GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
								pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
								pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
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