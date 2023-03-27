package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.tile.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class TeleportToVeinPacket {

	public static class Handler {
		public static void handle(final TeleportToVeinPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {

				ctx.get().enqueueWork(() -> {
					Player player = ctx.get().getSender();
					if (player == null)
						return;
					if (!player.level.isClientSide) {
						IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
								.orElseThrow(NullPointerException::new);
						IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
								.orElseThrow(NullPointerException::new);
						ServerPlayer serverPlayer = (ServerPlayer) player;
						if (known.getSelectedVein() != null && known.getSelectedVein() != VeinLocation.BLANK) {
							DimensionalPosition p = known.getSelectedVein();
							BlockPos bp = p.getPosition();
							ResourceLocation dimRL = p.getDimension();
							ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimRL);
							ServerLevel ovw = player.level.getServer().getLevel(key);
							if (ovw.getBlockEntity(bp)instanceof EarthenVeinBlockEntity te) {
								if (!known.getSelectedVein().getName().equals(te.getLoc().getName())) {
									player.displayClientMessage(
											Component.literal("VEIN NO LONGER PRESENT REMOVING FROM KNOWN"), true);
									known.getVeinList().remove(known.getSelectedVein());
									known.setSelectedVein(VeinLocation.BLANK);
									PacketHandler.CHANNELKNOWNMANIPS.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new KnownManipulationServerPacket(known));
								} else {
									ChunkPos chunkpos = new ChunkPos(bp);
									((ServerLevel) player.level).getChunkSource().addRegionTicket(
											TicketType.POST_TELEPORT, chunkpos, 1, ((ServerPlayer) player).getId());

									serverPlayer.teleportTo(ovw, bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5,
											serverPlayer.getYRot(), serverPlayer.getXRot());
									player.displayClientMessage(Component.literal("TELEPORTING"), true);
									HLParticleUtils.spawnPoof(ovw, bp, ParticleTypes.CRIMSON_SPORE);
									HLParticleUtils.spawnPoof(ovw, bp, DustParticleOptions.REDSTONE);
									PacketHandler.CHANNELBLOODVOLUME.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new BloodVolumeServerPacket(volume));
								}

							} else {
								player.displayClientMessage(
										Component.literal("VEIN NO LONGER PRESENT REMOVING FROM KNOWN"), true);
								known.getVeinList().remove(known.getSelectedVein());
								known.setSelectedVein(VeinLocation.BLANK);
								PacketHandler.CHANNELKNOWNMANIPS.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
										new KnownManipulationServerPacket(known));
							}
						}else {
							player.displayClientMessage(
									Component.literal("Please select a vein to teleport to!"), true);
						}
					}
				});

				ctx.get().setPacketHandled(true);
			});
		}
	}

	public static TeleportToVeinPacket decode(FriendlyByteBuf buf) {
		return new TeleportToVeinPacket();
	}

	public static void encode(TeleportToVeinPacket msg, FriendlyByteBuf buf) {
	}

	public TeleportToVeinPacket() {

	}
}