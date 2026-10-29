package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.common.entity.mob.monster.HunterFungusEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AntibodyEntity;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Gardens' attention, per player while they are in the dimension: footsteps the network has heard, and how
 * foreign the body reads. Session state only; it is cleared when the server stops and when the player leaves.
 */
public final class GardensNetwork {
	private static final ParticleColor CRIMSON_SPORE = new ParticleColor(150, 40, 58);
	private static final Map<UUID, Trace> TRACES = new ConcurrentHashMap<>();
	/** The Gardens' own monsters, the ones its network calls; never a player's bound puppets or an encounter's mobs. */
	public static final TagKey<EntityType<?>> RESPONDERS =
			TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("gardens_network_responders"));

	private GardensNetwork() {}

	private static final class Trace {
		double walked;
		long lastAlarm = Long.MIN_VALUE / 2;
		long lastSummon = Long.MIN_VALUE / 2;
		float suspicion;
		boolean hasPosition;
		double lastX;
		double lastZ;
	}

	public static void tick(ServerPlayer player) {
		if (!player.isAlive() || player.isCreative() || player.isSpectator()
				|| !FungalGardensPlaces.inDimension(player.level())) {
			TRACES.remove(player.getUUID());
			return;
		}
		boolean apotheos = ApotheosBodyEvents.isApotheos(player);
		Trace trace = TRACES.computeIfAbsent(player.getUUID(), id -> new Trace());
		long now = player.level().getGameTime();

		double moveSqr = trace.hasPosition
				? square(player.getX() - trace.lastX) + square(player.getZ() - trace.lastZ) : 0.0;
		trace.lastX = player.getX();
		trace.lastZ = player.getZ();
		trace.hasPosition = true;
		if (!apotheos) {
			boolean onNetwork = !player.isPassenger() && player.onGround() && player.level().getBlockState(player.blockPosition().below())
					.is(RootedStateHelper.FUNGAL_NETWORK_GROUND);
			trace.walked += GardensNetworkRules.footfall(player.isCrouching(), onNetwork, moveSqr);
			if (GardensNetworkRules.shouldAlarm(trace.walked, now - trace.lastAlarm)) {
				trace.walked = 0;
				trace.lastAlarm = now;
				alarm(player);
			}
		}

		if (player.tickCount % 20 == 0) {
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			trace.suspicion += GardensNetworkRules.bloodSuspicionPerSecond(apotheos,
					volume.isActive() ? volume.getBloodVolume() : 0, volume.getMaxBloodVolume());
		}
		if (player.tickCount % 200 == 0) {
			trace.suspicion = Math.max(0f, trace.suspicion - GardensNetworkRules.DECAY_PER_10S);
		}
		respond(player, trace, now);
	}

	/** A credited manipulation cast inside the Gardens reads as autonomy. */
	public static void onCast(ServerPlayer player) {
		if (player.isCreative() || player.isSpectator() || !FungalGardensPlaces.inDimension(player.level())) return;
		Trace trace = TRACES.computeIfAbsent(player.getUUID(), id -> new Trace());
		trace.suspicion += GardensNetworkRules.castSuspicion(ApotheosBodyEvents.isApotheos(player));
		respond(player, trace, player.level().getGameTime());
	}

	private static void respond(ServerPlayer player, Trace trace, long now) {
		int count = GardensNetworkRules.antibodiesFor(trace.suspicion);
		if (count == 0 || !GardensNetworkRules.canSummon(now - trace.lastSummon)) return;
		trace.suspicion = 0f;
		trace.lastSummon = now;
		AntibodyEntity.summonFor(player, count);
		player.displayClientMessage(Component.translatable("message.hemomancy.gardens_network.antibodies")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
	}

	private static void alarm(ServerPlayer player) {
		double radius = GardensNetworkRules.ALERT_RADIUS;
		for (Monster monster : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius),
				monster -> monster.getTarget() == null && monster.getType().is(RESPONDERS))) {
			monster.setTarget(player);
		}
		HunterFungusEntity.converge(player);
		player.serverLevel().sendParticles(HemoParticleData.sporiticSpore(CRIMSON_SPORE), player.getX(), player.getY() + 0.05,
				player.getZ(), 18, 0.6, 0.02, 0.6, 0.01);
		player.displayClientMessage(Component.translatable("message.hemomancy.gardens_network.footsteps")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
	}

	private static double square(double value) {
		return value * value;
	}

	public static void clearSessionState() {
		TRACES.clear();
	}

	public static void logout(Player player) {
		TRACES.remove(player.getUUID());
	}
}
