package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingSpearCombatEvents {
	private static final Map<net.minecraft.world.damagesource.DamageSource, Map<UUID, Boolean>> QUALIFYING =
			new WeakHashMap<>();

	private LivingSpearCombatEvents() { }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void prepare(LivingIncomingDamageEvent event) {
		if (!(event.getSource() instanceof SchoolDamageSource source)
				|| !source.context().ability().getPath().equals("living_spear")) return;
		LivingEntity target = event.getEntity();
		boolean qualifies = LivingSpearLuxRules.qualifies(
				target.level().getMaxLocalRawBrightness(target.blockPosition()),
				SchoolStates.has(target, SchoolState.ILLUMINATED));
		QUALIFYING.computeIfAbsent(event.getSource(), ignored -> new HashMap<>())
				.put(target.getUUID(), qualifies);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void commit(LivingDamageEvent.Post event) {
		if (!(event.getSource() instanceof SchoolDamageSource source)
				|| !source.context().ability().getPath().equals("living_spear")
				|| !(event.getSource().getEntity() instanceof Player attacker)
				|| !(attacker.level() instanceof ServerLevel level)) return;
		Map<UUID, Boolean> byTarget = QUALIFYING.get(event.getSource());
		boolean qualifies = byTarget != null && Boolean.TRUE.equals(byTarget.remove(event.getEntity().getUUID()));
		float dealtDamage = event.getNewDamage() + event.getReduction(DamageContainer.Reduction.ABSORPTION);
		if (dealtDamage <= 0.0F) return;

		ItemStack spear = attacker.getMainHandItem();
		if (!(spear.getItem() instanceof LivingSpearItem)) return;
		float charge = LivingSpearItem.getLuxCharge(spear);
		if (LivingSpearLuxRules.shouldBurst(charge, source.context().charge(), true)) {
			LivingSpearItem.setLuxCharge(spear, 0.0F);
			burst(level, attacker, event.getEntity());
			return;
		}
		LivingSpearItem.setLuxCharge(spear,
				LivingSpearLuxRules.addCharge(charge, dealtDamage, qualifies));
	}

	private static void burst(ServerLevel level, Player attacker, LivingEntity center) {
		var lightCenter = center.position().add(0, center.getBbHeight() * 0.5, 0);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.FLARE,
				lightCenter, lightCenter, LivingSpearLuxRules.BLAST_RADIUS, 24);
		level.playSound(null, center.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
				SoundSource.PLAYERS, 1.2F, 1.35F);
		var damage = SchoolDamage.reaction(attacker, "living_spear_lux_burst", EnumBloodTendency.LUX);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				center.getBoundingBox().inflate(LivingSpearLuxRules.BLAST_RADIUS),
				target -> ManipulationCombatHelper.canHarm(attacker, target)
						&& target.distanceToSqr(center) <= LivingSpearLuxRules.BLAST_RADIUS
								* LivingSpearLuxRules.BLAST_RADIUS)) {
			// The strike has already started hurt immunity, including on the primary victim.
			int immunity = target.invulnerableTime;
			target.invulnerableTime = 0;
			try {
				target.hurt(damage, LivingSpearLuxRules.BLAST_DAMAGE);
			} finally {
				target.invulnerableTime = Math.max(immunity, target.invulnerableTime);
			}
			SchoolStates.apply(attacker, target, SchoolState.ILLUMINATED,
					LivingSpearLuxRules.ILLUMINATION_TICKS);
		}
	}
}
