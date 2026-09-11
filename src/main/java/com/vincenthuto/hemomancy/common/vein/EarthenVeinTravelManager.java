package com.vincenthuto.hemomancy.common.vein;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.TerrestrialSpeculumItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.vein.EarthenVeinTravelVisualPacket;
import com.vincenthuto.hemomancy.common.network.vein.OpenEarthenVeinDisplayPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class EarthenVeinTravelManager {
	private static final ParticleColor HUNGRY_VOID = new ParticleColor(7, 0, 5);
	private static final ParticleColor SATED_BLOOD = new ParticleColor(224, 4, 20);
	private static final Map<UUID, EarthenVeinTravelSession> BY_PLAYER = new HashMap<>();
	private static final Map<SourceKey, UUID> SOURCE_OWNERS = new HashMap<>();
	private static final Map<UUID, Vec3> CINEMATIC_STARTS = new HashMap<>();
	private static final Map<UUID, Long> FALL_PROTECTION_UNTIL = new HashMap<>();

	private EarthenVeinTravelManager() {
	}

	public static boolean begin(ServerPlayer player, BlockPos sourcePos) {
		if (!(player.level() instanceof ServerLevel sourceLevel)
				|| !(sourceLevel.getBlockEntity(sourcePos) instanceof EarthenVeinBlockEntity source)) return false;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null || !ownsSource(player, sourceLevel, sourcePos, source, known)) {
			player.displayClientMessage(Component.literal("You do not control this vein!").withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}

		EarthenVeinTravelSession current = BY_PLAYER.get(player.getUUID());
		if (current != null && current.sourceDimension().equals(sourceLevel.dimension().location())
				&& current.sourcePos().equals(sourcePos)) {
			cancel(player.getUUID(), player.server, true);
			return true;
		}

		SourceKey sourceKey = new SourceKey(sourceLevel.dimension().location(), sourcePos);
		UUID reservingPlayer = SOURCE_OWNERS.get(sourceKey);
		if (reservingPlayer != null && !reservingPlayer.equals(player.getUUID())) {
			player.displayClientMessage(Component.literal("The vein is already feeding another traveler.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}

		List<EarthenVeinDestination> destinations = destinations(known, source, sourceLevel.dimension().location(), sourcePos);
		if (destinations.isEmpty()) {
			player.displayClientMessage(Component.literal("No other claimed veins answer this opening.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}

		cancel(player.getUUID(), player.server, false);
		EarthenVeinTravelSession session = EarthenVeinTravelSession.selecting(player.getUUID(),
				sourceLevel.dimension().location(), sourcePos, source.isTemporary(),
				source.isTemporary() ? null : source.getLoc().getUUID(), sourceLevel.getGameTime());
		BY_PLAYER.put(player.getUUID(), session);
		SOURCE_OWNERS.put(sourceKey, player.getUUID());
		keepTemporaryAlive(player, session, sourceLevel.getGameTime());
		PacketHandler.sendToPlayer(player, new OpenEarthenVeinDisplayPacket(session.nonce(),
				session.sourceDimension(), sourcePos, destinations));
		sourceLevel.playSound(null, sourcePos, SoundEvents.SCULK_SHRIEKER_SHRIEK,
				SoundSource.BLOCKS, 0.65F, 0.65F);
		return true;
	}

	public static void select(ServerPlayer player, UUID nonce, UUID destinationId) {
		EarthenVeinTravelSession session = BY_PLAYER.get(player.getUUID());
		if (session == null || !session.nonce().equals(nonce)
				|| session.phase() != EarthenVeinTravelPhase.SELECTING || !nearSource(player, session)) return;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) return;
		VeinLocation claimed = known.getVeinList().stream()
				.filter(vein -> vein.getUUID().equals(destinationId)).findFirst().orElse(null);
		if (claimed == null) return;
		EarthenVeinDestination destination = validateDestination(player, known, claimed, true);
		if (destination == null) return;
		int cost = EarthenVeinTravelRules.cost(session.sourcePos(), session.sourceDimension(),
				destination.position(), destination.dimension());
		destination = new EarthenVeinDestination(destination.id(), destination.name(), destination.dimension(),
				destination.position(), cost);
		if (!session.select(player.getUUID(), destination, player.level().getGameTime())) return;
		keepTemporaryAlive(player, session, player.level().getGameTime());
		sync(player, session.sourceDimension(), session.sourcePos(), EarthenVeinTravelPhase.FEEDING, 0.0F);
		player.displayClientMessage(Component.literal("Project blood into the opening: 0 / " + cost + " mB")
				.withStyle(ChatFormatting.DARK_RED), true);
	}

	public static double project(ServerPlayer player, ServerLevel level, BlockPos pos, double offered) {
		EarthenVeinTravelSession session = BY_PLAYER.get(player.getUUID());
		if (session == null || session.phase() != EarthenVeinTravelPhase.FEEDING
				|| !session.sourceDimension().equals(level.dimension().location())
				|| !session.sourcePos().equals(pos) || !nearSource(player, session)) return 0.0D;
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (blood == null || !blood.isActive()) return 0.0D;
		double accepted = session.feed(player.getUUID(), offered, blood.getBloodVolume(), level.getGameTime());
		if (accepted <= 0.0D || !blood.drain(accepted)) return 0.0D;
		keepTemporaryAlive(player, session, level.getGameTime());
		blood.addBloodSpend(accepted);
		HemorathEntity.onPlayerBloodSpend(player, accepted);
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));
		float progress = session.destination() == null ? 0.0F
				: (float) (session.fedBlood() / session.destination().cost());
		sync(player, session.sourceDimension(), pos, session.phase(), progress);
		if (player.tickCount % 5 == 0 || session.phase() == EarthenVeinTravelPhase.READY) {
			int required = session.destination() == null ? 0 : session.destination().cost();
			String text = session.phase() == EarthenVeinTravelPhase.READY
					? "The opening is sated. Stand upon it."
					: Math.round(session.fedBlood()) + " / " + required + " mB";
			player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.DARK_RED), true);
		}
		return accepted;
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		long now = player.level().getGameTime();
		FALL_PROTECTION_UNTIL.computeIfPresent(player.getUUID(),
				(id, protectedUntil) -> EarthenVeinTravelRules.hasFallProtection(protectedUntil, now)
						? protectedUntil : null);
		EarthenVeinTravelSession session = BY_PLAYER.get(player.getUUID());
		if (session == null) return;
		if (!player.isAlive()) {
			cancel(player.getUUID(), player.server, true);
			return;
		}
		if (session.phase() != EarthenVeinTravelPhase.EJECTING && !nearSource(player, session)) {
			cancel(player.getUUID(), player.server, true);
			return;
		}

		switch (session.phase()) {
			case SELECTING -> {
				if (now - session.createdAt() > EarthenVeinTravelRules.DISPLAY_TIMEOUT_TICKS) {
					cancel(player.getUUID(), player.server, true);
				}
			}
			case FEEDING -> {
				if (EarthenVeinTravelRules.feedExpired(session.lastFedAt(), now)) {
					cancel(player.getUUID(), player.server, true);
				} else {
					spawnFeedingVortex((ServerLevel) player.level(), session.sourcePos(), now,
							(float) (session.fedBlood() / Math.max(1, session.destination().cost())),
							EarthenVeinTravelPhase.FEEDING);
				}
			}
			case READY -> {
				spawnFeedingVortex((ServerLevel) player.level(), session.sourcePos(), now, 1.0F,
						EarthenVeinTravelPhase.READY);
				if (EarthenVeinTravelRules.readyExpired(session.phaseStartedAt(), now)) {
					cancel(player.getUUID(), player.server, true);
				} else if (standingOnSource(player, session.sourcePos())) {
					CINEMATIC_STARTS.put(player.getUUID(), player.position());
					session.beginSwallow(player.getUUID(), now);
					keepTemporaryAlive(player, session, now);
					sync(player, session.sourceDimension(), session.sourcePos(), EarthenVeinTravelPhase.SWALLOWING, 0.0F);
				}
			}
			case SWALLOWING -> tickSwallow(player, session, now);
			case EJECTING -> tickEjection(player, session, now);
			default -> { }
		}
	}

	private static void tickSwallow(ServerPlayer player, EarthenVeinTravelSession session, long now) {
		float progress = Mth.clamp((now - session.phaseStartedAt()) / (float) EarthenVeinTravelRules.SWALLOW_TICKS,
				0.0F, 1.0F);
		Vec3 start = CINEMATIC_STARTS.getOrDefault(player.getUUID(), player.position());
		Vec3 mouth = Vec3.atBottomCenterOf(session.sourcePos()).add(0.0D, 0.22D, 0.0D);
		Vec3 current = start.lerp(mouth, smooth(progress));
		player.teleportTo(current.x, current.y, current.z);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		if ((now - session.phaseStartedAt()) % 3L == 0L) {
			sync(player, session.sourceDimension(), session.sourcePos(), EarthenVeinTravelPhase.SWALLOWING, progress);
		}
		if (!session.swallowComplete(now)) return;
		teleportToDestination(player, session, now);
	}

	private static void teleportToDestination(ServerPlayer player, EarthenVeinTravelSession session, long now) {
		EarthenVeinDestination destination = session.destination();
		if (destination == null) {
			cancel(player.getUUID(), player.server, true);
			return;
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		VeinLocation claimed = known == null ? null : known.getVeinList().stream()
				.filter(vein -> vein.getUUID().equals(destination.id())).findFirst().orElse(null);
		EarthenVeinDestination validated = claimed == null ? null : validateDestination(player, known, claimed, false);
		if (validated == null) {
			cancel(player.getUUID(), player.server, true);
			return;
		}
		ServerLevel sourceLevel = (ServerLevel) player.level();
		sync(player, session.sourceDimension(), session.sourcePos(), EarthenVeinTravelPhase.IDLE, 0.0F);
		if (session.temporarySource()) removeTemporarySource(player, session);
		ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, destination.dimension()));
		if (targetLevel == null) {
			cancel(player.getUUID(), player.server, false);
			return;
		}
		targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT,
				new ChunkPos(destination.position()), 1, player.getId());
		BlockState targetState = targetLevel.getBlockState(destination.position());
		float yaw = targetState.hasProperty(EarthenVeinBlock.FACING)
				? targetState.getValue(EarthenVeinBlock.FACING).toYRot() : player.getYRot();
		player.teleportTo(targetLevel, destination.position().getX() + 0.5D,
				destination.position().getY() + 0.18D, destination.position().getZ() + 0.5D, yaw, 0.0F);
		session.beginEjection(targetLevel.getGameTime());
		CINEMATIC_STARTS.put(player.getUUID(), player.position());
		sync(player, destination.dimension(), destination.position(), EarthenVeinTravelPhase.EJECTING, 0.0F);
		targetLevel.playSound(null, destination.position(), SoundEvents.ENDERMAN_TELEPORT,
				SoundSource.PLAYERS, 0.85F, 0.72F);
		sourceLevel.playSound(null, session.sourcePos(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
				SoundSource.BLOCKS, 0.45F, 0.55F);
	}

	private static void tickEjection(ServerPlayer player, EarthenVeinTravelSession session, long now) {
		EarthenVeinDestination destination = session.destination();
		if (destination == null || !player.level().dimension().location().equals(destination.dimension())) {
			cancel(player.getUUID(), player.server, false);
			return;
		}
		float progress = Mth.clamp((now - session.phaseStartedAt()) / (float) EarthenVeinTravelRules.EJECTION_TICKS,
				0.0F, 1.0F);
		Vec3 start = CINEMATIC_STARTS.getOrDefault(player.getUUID(), player.position());
		Vec3 end = Vec3.atBottomCenterOf(destination.position()).add(0.0D, 1.05D, 0.0D);
		Vec3 current = start.lerp(end, smooth(progress));
		player.teleportTo(current.x, current.y, current.z);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		spawnFeedingVortex((ServerLevel) player.level(), destination.position(), now, 1.0F - progress,
				EarthenVeinTravelPhase.EJECTING);
		if ((now - session.phaseStartedAt()) % 3L == 0L) {
			sync(player, destination.dimension(), destination.position(), EarthenVeinTravelPhase.EJECTING, progress);
		}
		if (!session.ejectionComplete(now)) return;
		BlockState state = player.level().getBlockState(destination.position());
		var facing = state.hasProperty(EarthenVeinBlock.FACING)
				? state.getValue(EarthenVeinBlock.FACING) : net.minecraft.core.Direction.SOUTH;
		player.setDeltaMovement(facing.getStepX() * 0.34D, 0.88D, facing.getStepZ() * 0.34D);
		player.hurtMarked = true;
		player.fallDistance = 0.0F;
		FALL_PROTECTION_UNTIL.put(player.getUUID(), now + 80L);
		sync(player, destination.dimension(), destination.position(), EarthenVeinTravelPhase.IDLE, 0.0F);
		player.getCooldowns().addCooldown(ItemInit.terrestrial_speculum.get(), 20);
		finish(player.getUUID());
	}

	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Long until = FALL_PROTECTION_UNTIL.get(player.getUUID());
		if (until != null && EarthenVeinTravelRules.hasFallProtection(until, player.level().getGameTime())) {
			event.setCanceled(true);
			FALL_PROTECTION_UNTIL.remove(player.getUUID());
		} else if (until != null) {
			FALL_PROTECTION_UNTIL.remove(player.getUUID());
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) cancel(player.getUUID(), player.server, false);
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		BY_PLAYER.clear();
		SOURCE_OWNERS.clear();
		CINEMATIC_STARTS.clear();
		FALL_PROTECTION_UNTIL.clear();
	}

	private static boolean ownsSource(ServerPlayer player, ServerLevel level, BlockPos pos,
			EarthenVeinBlockEntity source, IKnownManipulations known) {
		if (source.isTemporary()) return source.isTemporaryOwnedBy(player.getUUID());
		if (!level.getBlockState(pos).getValue(EarthenVeinBlock.STENTED)) return false;
		return known.getVeinList().stream().anyMatch(vein -> vein.getUUID().equals(source.getLoc().getUUID())
				&& vein.getDimension().equals(level.dimension().location()) && vein.getPosition().equals(pos));
	}

	private static List<EarthenVeinDestination> destinations(IKnownManipulations known, EarthenVeinBlockEntity source,
			ResourceLocation sourceDimension, BlockPos sourcePos) {
		List<EarthenVeinDestination> result = new ArrayList<>();
		for (VeinLocation vein : known.getVeinList()) {
			if ((!source.isTemporary() && vein.getUUID().equals(source.getLoc().getUUID()))
					|| vein.getDimension().equals(sourceDimension) && vein.getPosition().equals(sourcePos)) continue;
			int cost = EarthenVeinTravelRules.cost(sourcePos, sourceDimension, vein.getPosition(), vein.getDimension());
			result.add(new EarthenVeinDestination(vein.getUUID(), vein.getName(), vein.getDimension(),
					vein.getPosition(), cost));
		}
		return List.copyOf(result);
	}

	@Nullable
	private static EarthenVeinDestination validateDestination(ServerPlayer player, IKnownManipulations known,
			VeinLocation claimed, boolean reportFailure) {
		ServerLevel destination = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, claimed.getDimension()));
		if (destination == null) return null;
		destination.getChunkAt(claimed.getPosition());
		EarthenVeinBlockEntity target = destination.getBlockEntity(claimed.getPosition()) instanceof EarthenVeinBlockEntity vein
				? vein : null;
		BlockState state = destination.getBlockState(claimed.getPosition());
		boolean stented = state.hasProperty(EarthenVeinBlock.STENTED) && state.getValue(EarthenVeinBlock.STENTED);
		boolean matchingId = target != null && target.getLoc().getUUID().equals(claimed.getUUID());
		if (EarthenVeinDestinationValidation.shouldPrune(target != null,
				target != null && target.isTemporary(), stented, matchingId)) {
			known.getVeinList().remove(claimed);
			PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
			if (reportFailure) player.displayClientMessage(Component.literal("That vein has ruptured.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return null;
		}
		if (!hasExitSpace(destination, claimed.getPosition())) {
			if (reportFailure) player.displayClientMessage(Component.literal("That vein's opening is obstructed.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return null;
		}
		if (!claimed.getName().equals(target.getLoc().getName())) {
			claimed.setName(target.getLoc().getName());
			PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		}
		return new EarthenVeinDestination(claimed.getUUID(), claimed.getName(), claimed.getDimension(),
				claimed.getPosition(), 0);
	}

	private static boolean hasExitSpace(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
				&& level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty();
	}

	private static boolean nearSource(ServerPlayer player, EarthenVeinTravelSession session) {
		if (!player.level().dimension().location().equals(session.sourceDimension())) return false;
		if (player.distanceToSqr(Vec3.atCenterOf(session.sourcePos()))
				> EarthenVeinTravelRules.MAX_SOURCE_DISTANCE_SQUARED) return false;
		if (!(player.level().getBlockEntity(session.sourcePos()) instanceof EarthenVeinBlockEntity source)) return false;
		if (session.temporarySource()) return source.isTemporaryOwnedBy(player.getUUID());
		BlockState state = player.level().getBlockState(session.sourcePos());
		return !source.isTemporary() && state.hasProperty(EarthenVeinBlock.STENTED)
				&& state.getValue(EarthenVeinBlock.STENTED) && session.sourceVeinId() != null
				&& session.sourceVeinId().equals(source.getLoc().getUUID());
	}

	private static boolean standingOnSource(Player player, BlockPos source) {
		double dx = player.getX() - (source.getX() + 0.5D);
		double dz = player.getZ() - (source.getZ() + 0.5D);
		return dx * dx + dz * dz <= 0.55D * 0.55D
				&& player.getY() >= source.getY() + 0.72D && player.getY() <= source.getY() + 2.0D;
	}

	private static void spawnFeedingVortex(ServerLevel level, BlockPos pos, long time, float intensity,
			EarthenVeinTravelPhase phase) {
		EarthenVeinFeedingVortexProfile profile = EarthenVeinFeedingVortexProfile.forPhase(phase, intensity);
		ParticleOptions glow = profile.effect() == EarthenVeinFeedingVortexProfile.Effect.BLOOD_GLOW
				? HemoParticleData.glow(SATED_BLOOD)
				: HemoParticleData.darkGlow(HUNGRY_VOID);
		Vec3 mouth = Vec3.atBottomCenterOf(pos).add(0.0D, 0.72D, 0.0D);
		int count = intensity >= 0.99F ? 4 : 2;
		for (int index = 0; index < count; index++) {
			double angle = time * profile.angularSpeed() + index * Math.PI * 2.0D / count;
			double radius = 0.65D + index * 0.08D;
			Vec3 from = mouth.add(Math.cos(angle) * radius, 0.55D + index * 0.14D, Math.sin(angle) * radius);
			Vec3 velocity = mouth.subtract(from).normalize().scale(profile.inwardSpeed());
			level.sendParticles(glow, from.x, from.y, from.z, 0,
					velocity.x, velocity.y, velocity.z, 1.0D);
		}
	}

	private static void sync(ServerPlayer traveler, ResourceLocation dimension, BlockPos pos,
			EarthenVeinTravelPhase phase, float progress) {
		ServerLevel level = traveler.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
		if (level == null) return;
		var packet = new EarthenVeinTravelVisualPacket(dimension, pos, traveler.getId(), phase,
				Mth.clamp(progress, 0.0F, 1.0F));
		PacketDistributor.sendToPlayersNear(level, null, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, 64.0D, packet);
	}

	private static void cancel(UUID playerId, net.minecraft.server.MinecraftServer server, boolean removeTemporary) {
		EarthenVeinTravelSession session = BY_PLAYER.remove(playerId);
		if (session == null) return;
		SOURCE_OWNERS.remove(new SourceKey(session.sourceDimension(), session.sourcePos()), playerId);
		Vec3 cinematicStart = CINEMATIC_STARTS.remove(playerId);
		ServerPlayer player = server.getPlayerList().getPlayer(playerId);
		if (player != null) {
			if (session.phase() == EarthenVeinTravelPhase.SWALLOWING && cinematicStart != null
					&& player.level().dimension().location().equals(session.sourceDimension())) {
				player.teleportTo(cinematicStart.x, cinematicStart.y, cinematicStart.z);
				player.setDeltaMovement(Vec3.ZERO);
				player.fallDistance = 0.0F;
			}
			sync(player, session.sourceDimension(), session.sourcePos(), EarthenVeinTravelPhase.IDLE, 0.0F);
			if (removeTemporary && session.temporarySource()) removeTemporarySource(player, session);
		}
	}

	private static void removeTemporarySource(ServerPlayer player, EarthenVeinTravelSession session) {
		ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, session.sourceDimension()));
		if (level == player.level()) {
			TerrestrialSpeculumItem.dismissTemporaryOrigin(player, session.sourcePos());
		} else if (level != null && level.getBlockEntity(session.sourcePos()) instanceof EarthenVeinBlockEntity vein
				&& vein.isTemporaryOwnedBy(player.getUUID())) {
			level.removeBlock(session.sourcePos(), false);
		}
	}

	private static void keepTemporaryAlive(ServerPlayer player, EarthenVeinTravelSession session, long now) {
		if (!session.temporarySource()) return;
		ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, session.sourceDimension()));
		if (level != null && level.getBlockEntity(session.sourcePos()) instanceof EarthenVeinBlockEntity vein
				&& vein.isTemporaryOwnedBy(player.getUUID())) {
			vein.makeTemporary(player.getUUID(), EarthenVeinTravelRules.temporaryExpiry(session.phase(), now));
		}
	}

	private static void finish(UUID playerId) {
		EarthenVeinTravelSession session = BY_PLAYER.remove(playerId);
		if (session != null) SOURCE_OWNERS.remove(new SourceKey(session.sourceDimension(), session.sourcePos()), playerId);
		CINEMATIC_STARTS.remove(playerId);
	}

	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	private record SourceKey(ResourceLocation dimension, BlockPos pos) {
		private SourceKey { pos = pos.immutable(); }
	}
}
