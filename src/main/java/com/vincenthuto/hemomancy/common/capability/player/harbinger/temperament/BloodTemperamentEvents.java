package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTemperamentServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class BloodTemperamentEvents {

	private record TemperShift(Map<EnumBloodTemper, Float> amounts) {
		TemperShift(EnumBloodTemper temper, float amount) {
			this(Map.of(temper, amount));
		}

		TemperShift(EnumBloodTemper primary, float amount, EnumBloodTemper secondary, float secondaryAmount) {
			this(Map.of(primary, amount, secondary, secondaryAmount));
		}

		/** A profiled mob's blood teaches each of its tempers an even share of the base shift. */
		static @Nullable TemperShift fromProfile(List<EnumBloodTemper> tempers, float base) {
			if (tempers.isEmpty()) return null;
			Map<EnumBloodTemper, Float> amounts = new EnumMap<>(EnumBloodTemper.class);
			for (EnumBloodTemper temper : tempers) amounts.put(temper, base / tempers.size());
			return new TemperShift(amounts);
		}
	}

	@SubscribeEvent
	public static void onEntityKilled(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		if (source == null || !(source.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.TEMPER_SHIFT_ON_KILL_ENABLED.get()) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player).isActive();
		if (!bloodActive) return;

		LivingEntity victim = event.getEntity();
		float base = HemoServerConfig.TEMPER_SHIFT_AMOUNT.get().floatValue();
		float bossMult = (float) HemoServerConfig.TEMPER_BOSS_MULTIPLIER.get().doubleValue();

		TemperShift shift = determineTemperFromKill(victim, base, bossMult);
		if (shift == null) return;

		var cap = HemoCapabilityAccess.getBloodTemperament(player);
		shift.amounts().forEach(cap::addTemperAlignment);
		syncTemperament((ServerPlayer) player, cap);
	}

	/**
	 * Maps a killed entity to a TemperShift (temper + amount). Returns null for
	 * entities that yield no temper shift: the bloodless (e.g. Snow Golems) and
	 * any mob with no blood profile, which no longer defaults to Animus.
	 *
	 * Check order: specific bosses → general predicates → the entity's blood profile.
	 */
	private static TemperShift determineTemperFromKill(LivingEntity entity, float base, float bossMult) {
		// ── Specific bosses (surge amounts, most specific first) ──────────────────
		if (entity instanceof EnderDragon)   return new TemperShift(EnumBloodTemper.TENEBRIS, base * bossMult);
		// Wither is both death and fire — split the shift evenly
		if (entity instanceof WitherBoss)    return new TemperShift(EnumBloodTemper.MORTEM, base * bossMult * 0.5f,
				EnumBloodTemper.FLAMMEUS, base * bossMult * 0.5f);
		if (entity instanceof ElderGuardian) return new TemperShift(EnumBloodTemper.CONGEATIO, base * bossMult);
		if (entity instanceof Warden)        return new TemperShift(EnumBloodTemper.MORTEM, base * bossMult);

		// ── Specific single mobs (override before general Animal catch) ──────────
		if (entity instanceof Allay)         return new TemperShift(EnumBloodTemper.ANIMUS, base * 3f);
		if (entity instanceof Bee)           return new TemperShift(EnumBloodTemper.DUCTILIS, base);

		// ── General predicates ───────────────────────────────────────────────────
		// Fire-aligned (Blaze, Ghast, Hoglin, etc.)
		if (HemoEntityPredicates.INFERNALBLOOD.test(entity)) return new TemperShift(EnumBloodTemper.FLAMMEUS, base * 2f);
		// Ender-aligned (EnderMan, Shulker, Phantom, etc.)
		if (HemoEntityPredicates.ENDERBLOOD.test(entity))    return new TemperShift(EnumBloodTemper.TENEBRIS, base * 3f);
		// Cold / aquatic (Drowned, Stray, WaterAnimal)
		if (HemoEntityPredicates.COLDBLOODED.test(entity))   return new TemperShift(EnumBloodTemper.CONGEATIO, base * 2f);
		// Light-aligned (Guardian, GlowSquid, Witch, Allay)
		if (HemoEntityPredicates.LUMINOUS.test(entity))      return new TemperShift(EnumBloodTemper.LUX, base);
		// Iron/militant constructs (IronGolem, Vindicator, Pillager)
		if (HemoEntityPredicates.IRONCLAD.test(entity))      return new TemperShift(EnumBloodTemper.FERRIC, base * 2f);
		// All undead (catches Skeleton, WitherSkeleton, Zombie, Spider, Silverfish via isInvertedHealAndHarm or UNDEAD)
		if (entity.isInvertedHealAndHarm() || HemoEntityPredicates.UNDEAD.test(entity))
			return new TemperShift(EnumBloodTemper.MORTEM, base);
		// Warm-blooded animals and villagers
		if (HemoEntityPredicates.WARMBLOODED.test(entity))   return new TemperShift(EnumBloodTemper.ANIMUS, base);
		// Slimes/plant-like mobs
		if (HemoEntityPredicates.PLANTBLOOD.test(entity))    return new TemperShift(EnumBloodTemper.DUCTILIS, base);
		// Anything else shifts only by its data-driven blood profile; bloodless and unprofiled kills
		// teach the blood nothing (there is no Animus default).
		return TemperShift.fromProfile(BloodProfileData.profile(entity.getType(), false).tempers(), base);
	}

	/**
	 * Public helper so other systems (e.g. KnownManipulationEvents) can shift
	 * temper alignment when the player uses a manipulation of a given temper.
	 */
	public static void shiftTemperFromManipUse(ServerPlayer player, EnumBloodTemper temper) {
		shiftTemperFromManipUse(player, temper, null);
	}

	public static void shiftTemperFromManipUse(ServerPlayer player, EnumBloodTemper temper,
			@Nullable EnumBloodTemper secondaryTemper) {
		shiftTemperFromManipUse(player, temper, secondaryTemper, 1.0F);
	}

	/** {@code scale} is the cast's charge fraction for CHARGED manipulations, 1 otherwise. */
	public static void shiftTemperFromManipUse(ServerPlayer player, EnumBloodTemper temper,
			@Nullable EnumBloodTemper secondaryTemper, float scale) {
		if (!HemoServerConfig.TEMPER_SHIFT_ON_CAST_ENABLED.get()) return;
		float amount = HemoServerConfig.TEMPER_SHIFT_ON_MANIP_USE.get().floatValue()
				* net.minecraft.util.Mth.clamp(scale, 0.0F, 1.0F);
		if (amount <= 0) return;

		var cap = HemoCapabilityAccess.getBloodTemperament(player);
		cap.addTemperAlignment(temper, amount);
		if (secondaryTemper != null && secondaryTemper != temper) {
			cap.addTemperAlignment(secondaryTemper,
					com.vincenthuto.hemomancy.common.manipulation.TemperAffinityRules.secondaryTemperGain(amount));
		}
		syncTemperament(player, cap);
	}

	// Sync

	public static void syncTemperament(ServerPlayer player, IBloodTemperament temper) {
		PacketHandler.sendToPlayer(player, new BloodTemperamentServerPacket(temper.getTemper()));
	}

}
