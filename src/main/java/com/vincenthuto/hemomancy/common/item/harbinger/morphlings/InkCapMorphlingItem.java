package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ink Cap, the Veiled Fold's strain (Atramentaria velata): concealment and escape. Veil shortens how far hostiles
 * notice the host; Ink Wake blinds hostiles behind a sprinting or swimming host; Folded Away hides a still host at home;
 * Ink Reprieve is the Apex death-save ({@code hemomancy:ink_mantle}); Deliquesce dissolves the host into ink to escape.
 * Numbers in {@link InkCapRules}.
 */
public class InkCapMorphlingItem extends HouseMorphlingItem {
	private static final BloodAccounting.Source INK_REPRIEVE = BloodAccounting.Source.of("ink_mantle_reprieve",
			Category.MORPHLING, false, false);
	private static final BloodAccounting.Source DELIQUESCE = MorphlingItem.primalSource("deliquesce");
	private static final String INK_REPRIEVE_KEY = "InkMantleReprieve";
	private static final String WAKE_PREFIX = "inkcap.wake.";
	private static final String FOLDED_LOCKOUT = "inkcap.folded_lockout";
	private static final String DELIQUESCE_WINDOW = "inkcap.deliquesce_until";
	private static final int FOLDED_REFRESH = 10;
	private static final double DELIQUESCE_DROP_RANGE = 16.0D;

	public InkCapMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.INK_CAP;
	}

	@Override
	public double visibilityMultiplier(Player player, ItemStack stack) {
		return 1.0D - InkCapRules.veilReduction(stage(stack), home(player));
	}

	public static boolean isDissolved(Player player) {
		return MorphlingSession.isOpen(player, DELIQUESCE_WINDOW);
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		boolean home = home(player);
		if (stage >= 2 && (player.isSprinting() || player.isSwimming()) && proc(player, 5)) {
			inkWake(player, stage, home);
		}
		if (proc(player, 200)) MorphlingSession.prune(player, WAKE_PREFIX);
		if (stage >= 3 && home && proc(player, FOLDED_REFRESH) && isFoldedAway(player)) {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, FOLDED_REFRESH + 10, 0, true, false, true));
		}
		if (isDissolved(player)) {
			if (proc(player, 10)) {
				for (LivingEntity hostile : MorphlingAllies.hostiles(player, InkCapRules.DELIQUESCE_TRAIL_RADIUS)) {
					hostile.addEffect(new MobEffectInstance(MobEffects.DARKNESS, InkCapRules.DELIQUESCE_DARKNESS_TICKS, 0,
							false, true, true), player);
				}
				dropTargets(player);
			}
		} else if (MorphlingSession.get(player, DELIQUESCE_WINDOW) > 0L) {
			endDeliquesce(player);
		}
	}

	private static boolean isFoldedAway(Player player) {
		return MorphlingAnchorState.isStill(player, InkCapRules.FOLDED_STILL_TICKS)
				&& !MorphlingSession.isOpen(player, FOLDED_LOCKOUT);
	}

	private static void inkWake(Player player, int stage, boolean home) {
		long now = player.level().getGameTime();
		double radius = MorphlingHomeBonus.radius(InkCapRules.INK_WAKE_RADIUS, home);
		int ticks = MorphlingHomeBonus.duration(InkCapRules.inkWakeTicks(stage), home);
		for (LivingEntity hostile : MorphlingAllies.hostiles(player, radius)) {
			String key = WAKE_PREFIX + hostile.getUUID();
			if (now < MorphlingSession.get(player, key)) continue;
			hostile.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks, 0, false, true, true), player);
			MorphlingSession.put(player, key, now + InkCapRules.INK_WAKE_REBLIND);
		}
	}

	private static void dropTargets(Player player) {
		for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(DELIQUESCE_DROP_RANGE),
				mob -> mob.getTarget() == player)) {
			mob.setTarget(null);
		}
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player)) return false;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "Deliquesce", InkCapRules.DELIQUESCE_COST,
					InkCapRules.DELIQUESCE_COOLDOWN, InkCapRules.DELIQUESCE_STRAIN, 0, DELIQUESCE)) return false;
			int ticks = MorphlingHomeBonus.duration(InkCapRules.DELIQUESCE_TICKS, home(player));
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 1, true, false, true));
			MorphlingLock.seal(player, ticks);
			MorphlingSession.open(player, DELIQUESCE_WINDOW, ticks);
			dropTargets(player);
			return true;
		}
	}

	/** Ends a Deliquesce early (an attack swing) or on time: the host re-forms. */
	public static void endDeliquesce(LivingEntity entity) {
		if (!(entity instanceof Player player) || MorphlingSession.get(player, DELIQUESCE_WINDOW) <= 0L) return;
		MorphlingSession.remove(player, DELIQUESCE_WINDOW);
		player.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.morphling_sealed);
		removeOwn(player, MobEffects.INVISIBILITY);
		removeOwn(player, MobEffects.MOVEMENT_SPEED);
	}

	/** Removes an effect only when the strain granted it (ambient), never a potion's. */
	private static void removeOwn(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
		MobEffectInstance instance = player.getEffect(effect);
		if (instance != null && instance.isAmbient()) player.removeEffect(effect);
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		if (player.level().isClientSide) return;
		MorphlingSession.open(player, FOLDED_LOCKOUT, InkCapRules.FOLDED_ATTACK_LOCKOUT);
		if (stage(stack) >= 3 && home(player)) removeOwn(player, MobEffects.INVISIBILITY);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		onEquippedHurt(player, stack, source, amount, true);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount,
			boolean allowReactions) {
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, source)) {
			if (player.getHealth() > 0.0F
					|| MorphlingItem.getMaturityLevel(stack) < strain().lastRiteStage()
					|| !LastRiteHelper.canFire(player, LastRiteHelper.INK_MANTLE_ID)
					|| !ready(player, stack, INK_REPRIEVE_KEY, MorphlingBalanceBands.LAST_RITE_COOLDOWN_TICKS)) {
				return;
			}
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			if (!volume.isActive() || volume.getBloodVolume() <= InkCapRules.REPRIEVE_COST
					|| !BloodAccounting.pay(player, InkCapRules.REPRIEVE_COST, INK_REPRIEVE).satisfied()) {
				return;
			}
			player.setHealth(InkCapRules.REPRIEVE_HEALTH);
			player.invulnerableTime = InkCapRules.REPRIEVE_INVULNERABLE;
			spend(player, stack, INK_REPRIEVE_KEY);
			LastRiteHelper.consume(player, LastRiteHelper.INK_MANTLE_ID);
			for (LivingEntity hostile : MorphlingAllies.hostiles(player, InkCapRules.REPRIEVE_BLIND_RADIUS)) {
				hostile.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, InkCapRules.REPRIEVE_BLIND_TICKS, 0, false,
						true, true), player);
			}
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, InkCapRules.REPRIEVE_INVISIBLE_TICKS, 0, true,
					false, true));
		}
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.FOLDED_AWAY,
				stage(stack) >= 3 && home(player) && isFoldedAway(player));
		return MorphlingActiveState.set(flags, MorphlingActiveState.DELIQUESCE, isDissolved(player));
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "ink_wake", "folded_away", "ink_reprieve", "deliquesce");
	}
}
