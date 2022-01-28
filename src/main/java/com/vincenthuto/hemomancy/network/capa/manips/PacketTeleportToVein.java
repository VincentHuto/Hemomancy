package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.tile.BlockEntityEarthenVein;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.math.DimensionalPosition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
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

public class PacketTeleportToVein {

	public PacketTeleportToVein() {

	}

	public static void encode(PacketTeleportToVein msg, FriendlyByteBuf buf) {
	}

	public static PacketTeleportToVein decode(FriendlyByteBuf buf) {
		return new PacketTeleportToVein();
	}

	public static class Handler {
		public static void handle(final PacketTeleportToVein msg, Supplier<NetworkEvent.Context> ctx) {
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
							ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimRL);
							ServerLevel ovw = player.level.getServer().getLevel(key);
							if (ovw.getBlockEntity(bp)instanceof BlockEntityEarthenVein te) {
								if (!known.getSelectedVein().getName().equals(te.getLoc().getName())) {
									player.displayClientMessage(
											new TextComponent("VEIN NO LONGER PRESENT REMOVING FROM KNOWN"), true);
									known.getVeinList().remove(known.getSelectedVein());
									known.setSelectedVein(VeinLocation.BLANK);
									PacketHandler.CHANNELKNOWNMANIPS.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new PacketKnownManipulationServer(known));
								} else {
									ChunkPos chunkpos = new ChunkPos(bp);
									((ServerLevel) player.level).getChunkSource().addRegionTicket(
											TicketType.POST_TELEPORT, chunkpos, 1, ((ServerPlayer) player).getId());

									serverPlayer.teleportTo(ovw, bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5,
											serverPlayer.getYRot(), serverPlayer.getXRot());
									player.displayClientMessage(new TextComponent("TELEPORTING"), true);
									HLParticleUtils.spawnPoof(ovw, bp, ParticleTypes.CRIMSON_SPORE);
									HLParticleUtils.spawnPoof(ovw, bp, DustParticleOptions.REDSTONE);
									PacketHandler.CHANNELBLOODVOLUME.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
											new PacketBloodVolumeServer(volume));
								}

							} else {
								player.displayClientMessage(
										new TextComponent("VEIN NO LONGER PRESENT REMOVING FROM KNOWN"), true);
								known.getVeinList().remove(known.getSelectedVein());
								known.setSelectedVein(VeinLocation.BLANK);
								PacketHandler.CHANNELKNOWNMANIPS.send(
										PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
										new PacketKnownManipulationServer(known));
							}
						}else {
							player.displayClientMessage(
									new TextComponent("Please select a vein to teleport to!"), true);
						}
					}
				});

				ctx.get().setPacketHandled(true);
			});
		}
	}
}