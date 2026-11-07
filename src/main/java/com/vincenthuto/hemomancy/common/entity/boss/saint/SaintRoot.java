package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Holds a player rooted in a Saint's fight (Seraphae's Knotting, Putriciel's Overgrown, Velorum's preservation and
 * Still Floor): held movement-speed and, with {@code jump}, jump-strength modifiers through
 * {@link AttributeModifiers#sync}, removed when the root runs out, on release, death, logout and the trial's end. The
 * modifiers are transient, so a relog never carries one.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SaintRoot {
	private record Held(long until, boolean jump) {
	}

	private static final Map<UUID, Map<ResourceLocation, Held>> ROOTED = new HashMap<>();

	private SaintRoot() {
	}

	/** Roots {@code player} for {@code ticks} under {@code key}; a longer root under the same key replaces it. */
	public static void root(Player player, int ticks, ResourceLocation key, boolean jump) {
		if (player.level().isClientSide || ticks <= 0) return;
		long until = player.level().getGameTime() + ticks;
		Map<ResourceLocation, Held> roots = ROOTED.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
		Held held = roots.get(key);
		roots.put(key, new Held(held == null ? until : Math.max(held.until(), until), jump || held != null && held.jump()));
		apply(player, key, true, jump || held != null && held.jump());
	}

	public static boolean rooted(Player player) {
		Map<ResourceLocation, Held> roots = ROOTED.get(player.getUUID());
		return roots != null && !roots.isEmpty();
	}

	public static void release(Player player, ResourceLocation key) {
		Map<ResourceLocation, Held> roots = ROOTED.get(player.getUUID());
		if (roots == null || roots.remove(key) == null) return;
		apply(player, key, false, true);
		if (roots.isEmpty()) ROOTED.remove(player.getUUID());
	}

	/** Every root on the player (the trial's end). */
	public static void releaseAll(Player player) {
		Map<ResourceLocation, Held> roots = ROOTED.remove(player.getUUID());
		if (roots == null) return;
		for (ResourceLocation key : roots.keySet()) apply(player, key, false, true);
	}

	/** Logout: the transient modifiers leave with the player; only the record goes. */
	public static void end(Player player) {
		ROOTED.remove(player.getUUID());
	}

	public static void clearSessionState() {
		ROOTED.clear();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;
		Map<ResourceLocation, Held> roots = ROOTED.get(player.getUUID());
		if (roots == null) return;
		long now = player.level().getGameTime();
		Iterator<Map.Entry<ResourceLocation, Held>> it = roots.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ResourceLocation, Held> entry = it.next();
			if (entry.getValue().until() > now) continue;
			it.remove();
			apply(player, entry.getKey(), false, true);
		}
		if (roots.isEmpty()) ROOTED.remove(player.getUUID());
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof Player player && !player.level().isClientSide) releaseAll(player);
	}

	private static void apply(Player player, ResourceLocation key, boolean present, boolean jump) {
		var operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), key, -1.0D, operation, present);
		if (jump) {
			AttributeModifiers.sync(player.getAttribute(Attributes.JUMP_STRENGTH), key.withSuffix("_jump"), -1.0D,
					operation, present);
		}
	}
}
