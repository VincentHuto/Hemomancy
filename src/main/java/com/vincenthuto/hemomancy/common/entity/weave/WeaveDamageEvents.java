package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Low-rank weaves cannot kill: a low-rank thread's own blow, and in the window after a thread harmed its weaver
 * the burning, frost and poison it left (or the landing from a Ferric launch, or a spike outliving its thread),
 * stops at the floor instead. Only the weaver blacks out, pausing the weave. Lava, fire blocks, other attackers
 * and every lethal-rank weave are untouched.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WeaveDamageEvents {
	private WeaveDamageEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
		CompoundTag data = player.getPersistentData();
		DamageSource source = event.getSource();
		BlockPos loomPos;
		boolean weaver;
		if (source.getEntity() instanceof FrayedThreadEntity thread) {
			if (SomaticWeaveRules.lethal(thread.rank())) return;
			loomPos = thread.loomPos();
			weaver = thread.hunts(player);
		} else {
			long now = player.level().getGameTime();
			if (data.getLong("hemomancy:weave_harm_until") < now || data.getBoolean("hemomancy:weave_lethal")
					|| !lingering(source, data.getLong("hemomancy:weave_launched_until") >= now)) return;
			loomPos = BlockPos.of(data.getLong("hemomancy:weave_loom"));
			weaver = true;
		}
		float health = player.getHealth();
		if (!SomaticWeaveRules.blacksOut(health, event.getAmount())) return;
		event.setAmount(SomaticWeaveRules.flooredDamage(health, event.getAmount()));
		if (!weaver) return;
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
		if (player.level().getBlockEntity(loomPos) instanceof SomaticLoomBlockEntity loom) {
			loom.blackout();
			Vec3 away = player.position().subtract(Vec3.atCenterOf(loomPos)).multiply(1, 0, 1);
			if (away.lengthSqr() > 1.0E-4D) {
				player.setDeltaMovement(away.normalize().scale(0.6D).add(0, 0.3D, 0));
				player.hurtMarked = true;
			}
		}
		data.remove("hemomancy:weave_harm_until");
		data.remove("hemomancy:weave_launched_until");
	}

	/** Harm a thread can leave behind with no attacker: its burning, frost, poison, spikes, and a launch's landing. */
	private static boolean lingering(DamageSource source, boolean launched) {
		if (source.getDirectEntity() instanceof EntityIronSpike) return source.getEntity() == null;
		if (source.getEntity() != null) return false;
		return source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.FREEZE) || source.is(DamageTypes.MAGIC)
				|| launched && source.is(DamageTypes.FALL);
	}
}
