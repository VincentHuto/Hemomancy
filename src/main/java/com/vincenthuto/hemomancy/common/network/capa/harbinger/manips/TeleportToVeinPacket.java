package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.TerrestrialSpeculumItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.TerrestrialSpeculumRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TeleportToVeinPacket(BlockPos origin, VeinLocation selected, boolean dismiss)
		implements CustomPacketPayload {
	public static final Type<TeleportToVeinPacket> TYPE = new Type<>(Hemomancy.rloc("teleport_to_vein_packet"));
	public static final StreamCodec<FriendlyByteBuf, TeleportToVeinPacket> STREAM_CODEC =
			StreamCodec.of(TeleportToVeinPacket::encode, TeleportToVeinPacket::decode);

	public TeleportToVeinPacket(BlockPos origin, VeinLocation selected) {
		this(origin, selected, false);
	}

	public static TeleportToVeinPacket dismiss(BlockPos origin) {
		return new TeleportToVeinPacket(origin, VeinLocation.BLANK, true);
	}

	public static void handle(TeleportToVeinPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) handleServer(msg, player);
		});
	}

	private static void handleServer(TeleportToVeinPacket msg, ServerPlayer player) {
		if (!(player.level().getBlockEntity(msg.origin) instanceof EarthenVeinBlockEntity originVein)
				|| !originVein.isTemporaryOwnedBy(player.getUUID())) return;
		if (msg.dismiss) {
			TerrestrialSpeculumItem.dismissTemporaryOrigin(player, msg.origin);
			return;
		}
		if (player.distanceToSqr(Vec3.atCenterOf(msg.origin))
				> TerrestrialSpeculumRules.MAX_ORIGIN_DISTANCE_SQR) return;

		boolean hasSpeculum = player.getMainHandItem().is(ItemInit.terrestrial_speculum.get())
				|| player.getOffhandItem().is(ItemInit.terrestrial_speculum.get());
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		TerrestrialSpeculumItem.dismissTemporaryOrigin(player, msg.origin);
		if (known == null || blood == null || !hasSpeculum || msg.selected == null
				|| HemoCapabilityAccess.getPlayerDegreeNumber(player) < TerrestrialSpeculumRules.REQUIRED_DEGREE) return;

		VeinLocation claimed = known.getVeinList().stream()
				.filter(vein -> vein.getUUID().equals(msg.selected.getUUID()))
				.findFirst().orElse(null);
		if (claimed == null) return;
		ServerLevel destination = player.server.getLevel(ResourceKey.create(Registries.DIMENSION,
				claimed.getDimension()));
		if (destination == null) return;
		BlockPos targetPos = claimed.getPosition();
		if (!(destination.getBlockEntity(targetPos) instanceof EarthenVeinBlockEntity targetVein)
				|| !destination.getBlockState(targetPos).getValue(EarthenVeinBlock.STENTED)
				|| !targetVein.getLoc().getUUID().equals(claimed.getUUID())) {
			known.getVeinList().remove(claimed);
			PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
			player.displayClientMessage(Component.literal("Vein has been ruptured or otherwise gone missing!"), true);
			return;
		}
		if (!claimed.getName().equals(targetVein.getLoc().getName())) {
			claimed.setName(targetVein.getLoc().getName());
			PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		}
		if (!TerrestrialSpeculumRules.canTravel(hasSpeculum, true,
				player.distanceToSqr(Vec3.atCenterOf(msg.origin)), true, true,
				blood.isActive() ? blood.getBloodVolume() : 0)) {
			player.displayClientMessage(Component.translatable("item.hemomancy.hematic_memory_tool.blood"), true);
			return;
		}

		blood.drain(TerrestrialSpeculumRules.BLOOD_COST);
		blood.addBloodSpend(TerrestrialSpeculumRules.BLOOD_COST);
		HemorathEntity.onPlayerBloodSpend(player, TerrestrialSpeculumRules.BLOOD_COST);
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));
		player.getCooldowns().addCooldown(ItemInit.terrestrial_speculum.get(), 20);
		destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(targetPos), 1,
				player.getId());
		HLParticleUtils.spawnPoof((ServerLevel) player.level(), msg.origin, ParticleTypes.CRIMSON_SPORE);
		HLParticleUtils.spawnPoof((ServerLevel) player.level(), msg.origin, DustParticleOptions.REDSTONE);
		player.teleportTo(destination, targetPos.getX() + 0.5, targetPos.getY() + 1,
				targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());
		destination.playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
		HLParticleUtils.spawnPoof(destination, targetPos, ParticleTypes.CRIMSON_SPORE);
		HLParticleUtils.spawnPoof(destination, targetPos, DustParticleOptions.REDSTONE);
	}

	private static TeleportToVeinPacket decode(FriendlyByteBuf buf) {
		BlockPos origin = buf.readBlockPos();
		boolean dismiss = buf.readBoolean();
		VeinLocation selected = new VeinLocation(buf.readUUID(), buf.readUtf(), buf.readResourceLocation(),
				buf.readBlockPos());
		return new TeleportToVeinPacket(origin, selected, dismiss);
	}

	private static void encode(FriendlyByteBuf buf, TeleportToVeinPacket msg) {
		buf.writeBlockPos(msg.origin);
		buf.writeBoolean(msg.dismiss);
		buf.writeUUID(msg.selected.getUUID());
		buf.writeUtf(msg.selected.getName());
		buf.writeResourceLocation(msg.selected.getDimension());
		buf.writeBlockPos(msg.selected.getPosition());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
