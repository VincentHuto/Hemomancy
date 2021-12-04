package com.vincenthuto.hemomancy.network.keybind;

import java.util.Random;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class PacketBloodFormationKeyPress {
	public static PacketBloodFormationKeyPress decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketBloodFormationKeyPress();
	}

	public static void encode(final PacketBloodFormationKeyPress message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketBloodFormationKeyPress message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			ServerLevel sLevel = (ServerLevel) ctx.get().getSender().level;
			if (player.getMainHandItem().getItem() instanceof SwordItem) {
				if (bloodVolume.getBloodVolume() > 100) {
					player.displayClientMessage(new TextComponent("Blood has been drawn for a greater cause"), true);
					bloodVolume.subtractBloodVolume(100);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
							new PacketBloodVolumeServer(bloodVolume));
					BlockPos pos = player.blockPosition();
					Random random = player.level.random;
					for (int i = 0; i < 30; i++) {
						sLevel.sendParticles(
								GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
								pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
								pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f, sLevel.random.nextInt(3) * 0.015f);
					}
					player.drop(new ItemStack(ItemInit.sanguine_formation.get(), random.nextInt(4)), false);
				} else {
					player.displayClientMessage(new TextComponent("Not enough blood can be drawn for formation"), true);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}