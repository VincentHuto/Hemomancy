package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Winter Shroud strain that favors stillness, cleansing, and last-breath
 * survival instead of contact retaliation.
 */
public class WinterShroudMorphlingItem extends MorphlingItem {

	private static final int TUN_MOLT_COOLDOWN = 600;

	public WinterShroudMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.winter_shroud.binomial";
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		triggerCryptobiosis(playerIn, itemStack, worldIn);
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		if (!player.hasEffect(EffectInit.venomous_resilience)) {
			player.addEffect(new MobEffectInstance(EffectInit.venomous_resilience,
					100, amplifier, false, false, true));
		}

		boolean still = player.getDeltaMovement().lengthSqr() <= FoxfireCamouflageRules.MAX_STILL_DELTA_SQR
				&& !player.isSprinting();
		if (WinterShroudResilienceRules.shouldApplyHide(maturity, still,
				player.getHealth(), player.getMaxHealth())) {
			int resistance = WinterShroudResilienceRules.resistanceAmplifier(maturity, still,
					player.getHealth(), player.getMaxHealth());
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
					80, resistance, true, false, true));
		}

		if (maturity >= 3 && !player.level().isClientSide && player.tickCount % 60 == 0) {
			cleanse(player);
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		if (MorphlingItem.isPrimal(stack) && player.getHealth() - amount <= 0.0F
				&& triggerCryptobiosis(player, stack, player.level())) {
			return;
		}

		if (WinterShroudResilienceRules.canTunMolt(maturity,
				Math.max(0.0F, player.getHealth() - amount), player.getMaxHealth())) {
			long now = player.level().getGameTime();
			long lastMolt = getLastAbilityTick(stack, "TunMolt");
			if (now - lastMolt >= TUN_MOLT_COOLDOWN) {
				setLastAbilityTick(stack, "TunMolt", now);
				cleanse(player);
				player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
						80, 0, true, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
						100, 1, true, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						60, 1, true, false, true));
				player.invulnerableTime = Math.max(player.invulnerableTime, 20);
				spawnMoltParticles(player);
			}
		}
	}

	private boolean triggerCryptobiosis(Player player, ItemStack stack, Level level) {
		if (!LastRiteHelper.canFire(player, LastRiteHelper.CRYPTOBIOSIS_ID)) {
			return false;
		}
		if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "Cryptobiosis",
				650.0, 12000, 700, 1)) {
			return false;
		}
		LastRiteHelper.consume(player, LastRiteHelper.CRYPTOBIOSIS_ID);
		cleanse(player);
		player.setHealth(Math.max(player.getHealth(), 10.0F));
		player.invulnerableTime = Math.max(player.invulnerableTime, 100);
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
				220, 2, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
				180, 1, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
				260, 2, true, false, true));
		spawnCryptobiosisParticles(player, level);
		return true;
	}

	private static void cleanse(Player player) {
		player.removeEffect(MobEffects.POISON);
		player.removeEffect(MobEffects.WITHER);
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		player.removeEffect(MobEffects.WEAKNESS);
	}

	private static void spawnMoltParticles(Player player) {
		if (player.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF,
					player.getX(), player.getY() + 0.4, player.getZ(),
					24, 0.45, 0.35, 0.45, 0.04);
		}
	}

	private static void spawnCryptobiosisParticles(Player player, Level level) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF,
					player.getX(), player.getY() + 0.5, player.getZ(),
					48, 0.65, 0.45, 0.65, 0.03);
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Cryptobiotic Hide (Stillness or low health grants resilience)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Cold Cleanse (Sheds poison, wither, weakness, and slow)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Tun Molt (Low-health escape burst)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Cryptobiosis (Primal Last Rite survival state)", 5, currentMaturity));
		return list;
	}
}
