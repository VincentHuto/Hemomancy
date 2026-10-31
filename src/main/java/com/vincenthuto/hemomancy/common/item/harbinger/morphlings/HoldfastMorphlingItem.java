package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holdfast, the Holding Stone's strain (Hapterophyta tenax): anchoring, sheltering others, holding ground. Grip gives
 * knockback resistance and stone-only breaking speed; Cling holds walls and currents; Brace hardens a sneaking host
 * against a hit; Breakwater anchors the host to shelter allies; The Holding pins everything near, then breaks loose in a
 * shockwave. Numbers in {@link HoldfastRules}.
 */
public class HoldfastMorphlingItem extends HouseMorphlingItem {
	public static final ResourceLocation GRIP_KNOCKBACK = Hemomancy.rloc("holdfast_grip_knockback");
	private static final String BRACE_COOLDOWN = "Brace";
	private static final String BRACE_WINDOW = "holdfast.brace_until";
	private static final String HOLDING_WINDOW = "holdfast.holding_until";
	private static final Map<UUID, List<UUID>> HELD = new ConcurrentHashMap<>();
	private static final int BREAKWATER_REFRESH = 10;

	public HoldfastMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.HOLDFAST;
	}

	/** Breakwater holds: Apex or better and anchored for its threshold. */
	public static boolean isBreakwater(Player player, ItemStack stack) {
		return stage(stack) >= 4 && MorphlingAnchorState.isAnchored(player, HoldfastRules.BREAKWATER_ANCHOR_TICKS);
	}

	public static boolean isBraced(Player player) {
		return MorphlingSession.isOpen(player, BRACE_WINDOW);
	}

	/** Cling: pressed against a wall, or submerged (currents stop pushing), from Developing. Read on both sides. */
	public static boolean clings(Player player, ItemStack stack) {
		return stage(stack) >= 2 && (player.horizontalCollision || player.isUnderWater());
	}

	@Override
	public float breakSpeedMultiplier(Player player, ItemStack stack, BlockState state) {
		return MorphlingHomeConditions.isStoneType(state) ? (float) (1.0D + HoldfastRules.gripBreakSpeed(stage(stack))) : 1.0F;
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		AttributeInstance knockback = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (knockback != null) {
			AttributeModifier held = knockback.getModifier(GRIP_KNOCKBACK);
			double others = knockback.getValue() - (held == null ? 0.0D : held.amount());
			double grip = Math.max(0.0D, Math.min(HoldfastRules.gripKnockback(stage), 1.0D - others));
			AttributeModifiers.sync(knockback, GRIP_KNOCKBACK, grip, AttributeModifier.Operation.ADD_VALUE, grip > 0.0D);
		}
		if (isBreakwater(player, stack) && proc(player, BREAKWATER_REFRESH)) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BREAKWATER_REFRESH + 10, 0, true, false,
					true));
		}
		long holdingUntil = MorphlingSession.get(player, HOLDING_WINDOW);
		if (holdingUntil > 0L && player.level().getGameTime() >= holdingUntil) release(player, stack);
	}

	/** Cling's movement, run on both sides from the player tick: no sliding down a wall, no fall damage banked. */
	public static void applyCling(Player player, ItemStack stack) {
		if (!clings(player, stack) || player.isUnderWater() || player.onGround()) return;
		Vec3 motion = player.getDeltaMovement();
		if (motion.y < 0.0D) player.setDeltaMovement(motion.x, 0.0D, motion.z);
		player.resetFallDistance();
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		onEquippedHurt(player, stack, source, amount, true);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount,
			boolean allowReactions) {
		if (stage(stack) < 3 || player.getHealth() <= 0.0F || !player.isShiftKeyDown()
				|| !ready(player, stack, BRACE_COOLDOWN, HoldfastRules.BRACE_COOLDOWN)) return;
		spend(player, stack, BRACE_COOLDOWN);
		boolean home = home(player);
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, HoldfastRules.BRACE_TICKS, home ? 1 : 0,
				true, false, true));
		MorphlingSession.open(player, BRACE_WINDOW, HoldfastRules.BRACE_TICKS);
		Vec3 facing = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
		for (LivingEntity ally : MorphlingAllies.allies(player, HoldfastRules.BRACE_ALLY_RANGE)) {
			Vec3 offset = ally.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
			if (offset.lengthSqr() > 1.0E-4D && offset.normalize().dot(facing) > 0.0D) continue;
			ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, HoldfastRules.BRACE_TICKS, 0, true, false,
					true));
		}
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player) || !(level instanceof ServerLevel)) return false;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "TheHolding", HoldfastRules.HOLDING_COST,
					HoldfastRules.HOLDING_COOLDOWN, HoldfastRules.HOLDING_STRAIN, 0,
					MorphlingItem.primalSource("the_holding"))) return false;
			double range = home(player) ? HoldfastRules.HOLDING_HOME_RANGE : HoldfastRules.HOLDING_RANGE;
			int ticks = HoldfastRules.HOLDING_PIN_TICKS;
			List<UUID> held = new ArrayList<>();
			MorphlingLock.pin(player, ticks);
			for (LivingEntity hostile : MorphlingAllies.hostiles(player, range)) {
				MorphlingLock.pin(hostile, ticks);
				held.add(hostile.getUUID());
			}
			for (LivingEntity ally : MorphlingAllies.allies(player, range)) {
				ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 1, true, false, true));
			}
			HELD.put(player.getUUID(), held);
			MorphlingSession.open(player, HOLDING_WINDOW, ticks);
			return true;
		}
	}

	/** The Holding lets go: a shockwave hurts and launches every hostile it pinned. */
	private void release(Player player, ItemStack stack) {
		MorphlingSession.remove(player, HOLDING_WINDOW);
		List<UUID> held = HELD.remove(player.getUUID());
		if (held == null || !(player.level() instanceof ServerLevel level)) return;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			for (UUID id : held) {
				Entity entity = level.getEntity(id);
				if (!(entity instanceof LivingEntity hostile) || !hostile.isAlive()) continue;
				hostile.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.pinned);
				hostile.invulnerableTime = 0;
				hostile.hurt(player.damageSources().indirectMagic(player, player), HoldfastRules.HOLDING_DAMAGE);
				Vec3 away = hostile.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
				Vec3 push = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize().scale(0.6D);
				hostile.push(push.x, 0.7D, push.z);
				hostile.hurtMarked = true;
			}
		}
	}

	@Override
	public void onUnequipped(Player player, ItemStack stack) {
		HELD.remove(player.getUUID());
	}

	public static void clearSessionState() {
		HELD.clear();
	}

	public static void logout(UUID host) {
		HELD.remove(host);
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.CLING, clings(player, stack));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.BRACE, isBraced(player));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.BREAKWATER, isBreakwater(player, stack));
		return MorphlingActiveState.set(flags, MorphlingActiveState.HOLDING, MorphlingSession.isOpen(player, HOLDING_WINDOW));
	}

	/** True when {@code entity} is the anchored host or an ally within Breakwater's reach of one. */
	public static Player shelteringBreakwater(LivingEntity entity) {
		for (Player host : entity.level().players()) {
			if (host == entity || host.distanceToSqr(entity) > HoldfastRules.BREAKWATER_RANGE * HoldfastRules.BREAKWATER_RANGE) {
				continue;
			}
			var cap = HemoCapabilityAccess.getEquippedMorphling(host);
			ItemStack stack = cap.getEquippedMorphling();
			if (MorphlingStrains.is(stack, MorphlingStrains.HOLDFAST) && isBreakwater(host, stack)
					&& MorphlingAllies.isAlly(host, entity)) {
				return host;
			}
		}
		return null;
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "cling", "brace", "breakwater", "the_holding");
	}
}
