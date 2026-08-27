package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HighStrungEvents {
	private HighStrungEvents() {
	}

	@SubscribeEvent
	public static void onSummonDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof Mob mob)
				|| !(mob instanceof BoundPuppeteerSummon bound) || bound.hemomancy$isTrialSummon()) return;
		BoundSummonBehavior.ownerFor(mob, bound).filter(ServerPlayer.class::isInstance)
				.map(ServerPlayer.class::cast).ifPresent(owner -> {
			int level = SkillPointHelper.getHighStrungLevel(owner);
			float recoil = PuppeteerSummonRules.highStrungRecoilDamage(level);
			if (recoil <= 0.0F) return;
			owner.hurt(owner.damageSources().magic(), recoil);
			owner.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 + level * 20, 0, false, true));
		});
	}
}
