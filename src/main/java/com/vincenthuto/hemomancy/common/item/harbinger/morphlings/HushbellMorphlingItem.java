package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Hushbell, the Still Bell's strain (Campanospora silens): stasis, conservation, survival. Hush slows hunger and air
 * loss and keeps poison off; Cryptobiotic Hide hardens the still or wounded host; Cold Cleanse strips poisons; Fold
 * Shut closes the mantle when a hit leaves the host low; Cryptobiosis is the Primal death-save
 * ({@code hemomancy:cryptobiosis}). Numbers in {@link HushbellRules}.
 */
public class HushbellMorphlingItem extends HouseMorphlingItem {
	private static final BloodAccounting.Source CRYPTOBIOSIS = MorphlingItem.primalSource("cryptobiosis");
	private static final String FOLD_COOLDOWN = "FoldShut";
	private static final String FOLD_WINDOW = "hushbell.fold_until";
	private static final String CRYPTO_WINDOW = "hushbell.cryptobiosis_until";
	private static final int HIDE_REFRESH = 10;

	public HushbellMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.HUSHBELL;
	}

	@Override
	public float exhaustionMultiplier(Player player, ItemStack stack) {
		return (float) (1.0D - HushbellRules.hush(stage(stack)));
	}

	@Override
	public double airLossSkipChance(Player player, ItemStack stack) {
		if (MorphlingSession.isOpen(player, FOLD_WINDOW) || MorphlingSession.isOpen(player, CRYPTO_WINDOW)) return 1.0D;
		return HushbellRules.hush(stage(stack));
	}

	@Override
	public boolean resistsEffect(Player player, ItemStack stack, Holder<MobEffect> effect) {
		return effect.is(MobEffects.POISON);
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		player.removeEffect(MobEffects.POISON);
		if (stage >= 2 && proc(player, HIDE_REFRESH)) {
			int amplifier = HushbellRules.hideAmplifier(stage,
					MorphlingAnchorState.isStill(player, MorphlingHomeConditions.STILL_HOME_TICKS),
					player.getHealth(), player.getMaxHealth());
			if (amplifier >= 0) {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, HIDE_REFRESH + 20, amplifier,
						true, false, true));
			}
		}
		if (stage >= 3 && proc(player, HushbellRules.COLD_CLEANSE_TICKS)) {
			player.removeEffect(MobEffects.POISON);
			player.removeEffect(MobEffects.WITHER);
			player.removeEffect(MobEffects.WEAKNESS);
			player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		}
		unfoldWhenDue(player, FOLD_WINDOW, false);
		unfoldWhenDue(player, CRYPTO_WINDOW, true);
	}

	private static void unfoldWhenDue(Player player, String window, boolean cryptobiosis) {
		long until = MorphlingSession.get(player, window);
		if (until <= 0L || player.level().getGameTime() < until) return;
		MorphlingSession.remove(player, window);
		MorphlingLock.release(player);
		cleanseHarmful(player, Integer.MAX_VALUE);
		if (cryptobiosis) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 2, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 180, 1, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 260, 2, true, false, true));
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		onEquippedHurt(player, stack, source, amount, true);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount,
			boolean allowReactions) {
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, source)) {
			if (player.getHealth() <= 0.0F) {
				if (MorphlingItem.isPrimal(stack)) triggerCryptobiosis(player, stack);
				return;
			}
			int stage = stage(stack);
			if (HushbellRules.foldShuts(stage, player.getHealth(), player.getMaxHealth())
					&& !MorphlingSession.isOpen(player, FOLD_WINDOW)
					&& ready(player, stack, FOLD_COOLDOWN, HushbellRules.FOLD_COOLDOWN)) {
				spend(player, stack, FOLD_COOLDOWN);
				int ticks = home(player) ? HushbellRules.FOLD_HOME_TICKS : HushbellRules.FOLD_TICKS;
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 3, true, false, true));
				MorphlingLock.pin(player, ticks);
				MorphlingLock.seal(player, ticks);
				MorphlingSession.open(player, FOLD_WINDOW, ticks);
			}
		}
	}

	private static boolean triggerCryptobiosis(Player player, ItemStack stack) {
		if (!LastRiteHelper.canFire(player, LastRiteHelper.CRYPTOBIOSIS_ID)
				|| !MorphlingItem.tryBeginPrimalAbility(player, stack, "Cryptobiosis", HushbellRules.CRYPTOBIOSIS_COST,
						MorphlingBalanceBands.LAST_RITE_COOLDOWN_TICKS, HushbellRules.CRYPTOBIOSIS_STRAIN, 1,
						CRYPTOBIOSIS)) {
			return false;
		}
		LastRiteHelper.consume(player, LastRiteHelper.CRYPTOBIOSIS_ID);
		cleanseHarmful(player, Integer.MAX_VALUE);
		player.setHealth(Math.max(player.getHealth(), HushbellRules.CRYPTOBIOSIS_HEALTH));
		player.invulnerableTime = Math.max(player.invulnerableTime, HushbellRules.CRYPTOBIOSIS_SEALED_TICKS);
		MorphlingLock.pin(player, HushbellRules.CRYPTOBIOSIS_SEALED_TICKS);
		MorphlingLock.seal(player, HushbellRules.CRYPTOBIOSIS_SEALED_TICKS);
		MorphlingSession.open(player, CRYPTO_WINDOW, HushbellRules.CRYPTOBIOSIS_SEALED_TICKS);
		return true;
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.FOLD_SHUT, MorphlingSession.isOpen(player, FOLD_WINDOW));
		return MorphlingActiveState.set(flags, MorphlingActiveState.CRYPTOBIOSIS, MorphlingSession.isOpen(player, CRYPTO_WINDOW));
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "cryptobiotic_hide", "cold_cleanse", "fold_shut", "cryptobiosis");
	}
}
