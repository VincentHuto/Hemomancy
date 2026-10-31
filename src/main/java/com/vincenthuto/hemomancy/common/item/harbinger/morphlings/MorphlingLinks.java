package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dead Man's Rope's strands: up to {@link DeadMansRopeRules#maxLinks} per host, each to an ally, a tamed companion
 * or a block anchor. A strand breaks beyond {@link DeadMansRopeRules#LINK_LENGTH}, when its partner dies or leaves
 * the dimension, when either side releases it, and when the host stops wearing the strain. Session state keyed by
 * host UUID; cleared on logout and server stop through {@code TransientServerStateEvents}. The strands reach
 * tracking clients as entity ids and anchor positions in {@code SyncEquippedMorphlingPacket}.
 */
public final class MorphlingLinks {
	private static final Map<UUID, List<Link>> LINKS = new ConcurrentHashMap<>();

	/** One strand: to an entity (by UUID) or to a block anchor. */
	public record Link(UUID entity, BlockPos anchor) {
		public static Link toEntity(Entity entity) {
			return new Link(entity.getUUID(), null);
		}

		public static Link toBlock(BlockPos pos) {
			return new Link(null, pos.immutable());
		}

		public boolean isBlock() {
			return anchor != null;
		}
	}

	private MorphlingLinks() {
	}

	public static List<Link> links(Player host) {
		return List.copyOf(LINKS.getOrDefault(host.getUUID(), List.of()));
	}

	public static int count(Player host) {
		return LINKS.getOrDefault(host.getUUID(), List.of()).size();
	}

	/** Toggles a strand: releases it when already held, otherwise ties it if the host has a strand to spare. */
	public static boolean toggle(Player host, Link link, int maxLinks) {
		List<Link> held = LINKS.computeIfAbsent(host.getUUID(), id -> new ArrayList<>());
		if (held.remove(link)) return true;
		if (maxLinks <= 0) return false;
		while (held.size() >= maxLinks) held.removeFirst();
		held.add(link);
		return true;
	}

	/** The partner side releasing: drops every strand any host holds to {@code partner}. */
	public static boolean releasePartner(Player host, Entity partner) {
		List<Link> held = LINKS.get(host.getUUID());
		return held != null && held.removeIf(link -> partner.getUUID().equals(link.entity()));
	}

	public static boolean isLinked(Player host, Entity partner) {
		return LINKS.getOrDefault(host.getUUID(), List.of()).stream()
				.anyMatch(link -> partner.getUUID().equals(link.entity()));
	}

	/** Living partners currently held by strands, in order. */
	public static List<LivingEntity> partners(Player host) {
		List<LivingEntity> out = new ArrayList<>();
		if (!(host.level() instanceof ServerLevel level)) return out;
		for (Link link : LINKS.getOrDefault(host.getUUID(), List.of())) {
			if (!link.isBlock() && level.getEntity(link.entity()) instanceof LivingEntity living) out.add(living);
		}
		return out;
	}

	/** Hosts holding a strand to {@code partner}. */
	public static List<Player> hostsOf(LivingEntity partner) {
		List<Player> hosts = new ArrayList<>();
		for (Player player : partner.level().players()) {
			if (player != partner && isLinked(player, partner)) hosts.add(player);
		}
		return hosts;
	}

	/** Breaks strands that are out of range, dead or gone, and trims to {@code maxLinks}. Returns true if any broke. */
	public static boolean prune(Player host, int maxLinks) {
		List<Link> held = LINKS.get(host.getUUID());
		if (held == null || held.isEmpty()) return false;
		int before = held.size();
		double reach = DeadMansRopeRules.LINK_LENGTH;
		held.removeIf(link -> {
			if (link.isBlock()) {
				return Vec3.atCenterOf(link.anchor()).distanceToSqr(host.position()) > reach * reach
						|| host.level().getBlockState(link.anchor()).isAir();
			}
			Entity partner = host.level() instanceof ServerLevel level ? level.getEntity(link.entity()) : null;
			return !(partner instanceof LivingEntity living) || !living.isAlive()
					|| living.distanceToSqr(host) > reach * reach;
		});
		while (held.size() > Math.max(0, maxLinks)) held.removeFirst();
		return held.size() != before;
	}

	public static void clear(Player host) {
		LINKS.remove(host.getUUID());
	}

	public static void logout(UUID host) {
		LINKS.remove(host);
		LINKS.values().forEach(held -> held.removeIf(link -> host.equals(link.entity())));
	}

	public static void clearSessionState() {
		LINKS.clear();
	}
}
