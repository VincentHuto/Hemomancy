package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.registry.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodFormationKeyPressPacket {
	public static BloodFormationKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodFormationKeyPressPacket();
	}

	public static void encode(final BloodFormationKeyPressPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final BloodFormationKeyPressPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			ServerLevel sLevel = (ServerLevel) ctx.get().getSender().level();
			if (player.getMainHandItem().getItem() instanceof SwordItem) {
				if (bloodVolume.getBloodVolume() > 100) {
					player.displayClientMessage(Component.literal("Blood has been drawn for a greater cause"), true);
					bloodVolume.drain(100);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
							new BloodVolumeServerPacket(bloodVolume));
					BlockPos pos = player.blockPosition();
					RandomSource random = player.level().random;
					for (int i = 0; i < 30; i++) {
						sLevel.sendParticles(
								GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
								pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
								pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f, sLevel.random.nextInt(3) * 0.015f);
					}
					player.drop(new ItemStack(ItemInit.sanguine_formation.get(), random.nextInt(4)), false);
				} else {
					player.displayClientMessage(Component.literal("Not enough blood can be drawn for formation"), true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}