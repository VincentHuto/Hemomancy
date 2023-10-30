package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
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
					if (!player.level().isClientSide) {
						IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
								.orElseThrow(NullPointerException::new);
						IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
								.orElseThrow(NullPointerException::new);
						ServerPlayer serverPlayer = (ServerPlayer) player;
						VeinLocation selected = msg.selected;
						if (selected != null && selected != VeinLocation.BLANK) {
							DimensionalPosition p = selected;
							BlockPos bp = p.getPosition();
							ResourceLocation dimRL = p.getDimension();
							ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimRL);
							ServerLevel ovw = player.level().getServer().getLevel(key);
							if (ovw.getBlockEntity(bp) instanceof EarthenVeinBlockEntity te) {
								if (!selected.getName().equals(te.getLoc().getName())
										&& selected.getPosition().equals(te.getLoc().getPosition())) {
									player.displayClientMessage(Component.literal("Vein has been renamed from '"
											+ selected.getName() + "' to '" + te.getLoc().getName() + "'"), true);
									for (VeinLocation loc : known.getVeinList()) { 
										if (loc.getPosition().equals(selected.getPosition())) {
											known.getVeinList().add(known.getVeinList().indexOf(loc), te.getLoc());
											known.getVeinList().remove(loc);
											PacketHandler.CHANNELKNOWNMANIPS.send(
													PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
													new KnownManipulationServerPacket(known));
										}
									}
								}

								if (!selected.getName().equals(te.getLoc().getName())) {
									player.displayClientMessage(
											Component.literal("Vein has been ruptured or otherwise gone missing!"),
											true);
									known.getVeinList().remove(selected);
									known.setSelectedVein(VeinLocation.BLANK);
									PacketHandler.CHANNELKNOWNMANIPS.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new KnownManipulationServerPacket(known));
								} else {
									ChunkPos chunkpos = new ChunkPos(bp);
									((ServerLevel) player.level()).getChunkSource().addRegionTicket(
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
										Component.literal("Vein has been ruptured or otherwise gone missing!"), true);
								for (VeinLocation loc : known.getVeinList()) {
									if (loc.getPosition().equals(selected.getPosition())) {
										known.getVeinList().remove(known.getVeinList().indexOf(loc));
										PacketHandler.CHANNELKNOWNMANIPS.send(
												PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
												new KnownManipulationServerPacket(known));
									}
								}
							}
						} else {
							player.displayClientMessage(Component.literal("Please select a vein to teleport to!"),
									true);
						}
					}
				});

				ctx.get().setPacketHandled(true);
			});
		}
	}

	public static TeleportToVeinPacket decode( FriendlyByteBuf buf) {
		return new TeleportToVeinPacket(VeinLocation.deserializeFromBuf(buf));
	}

	public static void encode(TeleportToVeinPacket msg, FriendlyByteBuf buf) {
		msg.selected.serializeToBuf(buf);
	}

	private VeinLocation selected;

	public TeleportToVeinPacket(VeinLocation current) {
		this.selected = current;
	}
}