package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.entity.mob.GardensRosterRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class GardensRosterSpawnEvents {
	private GardensRosterSpawnEvents() {
	}

	@SubscribeEvent
	public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
		apply(event.getEntity(), event.getLevel().getLevel().dimension(), event.getSpawnType());
	}

	/** Raises a roster mob that spawned naturally in the Gardens dimension. Returns whether it was raised. */
	public static boolean apply(Mob mob, ResourceKey<Level> dimension, MobSpawnType spawnType) {
		boolean natural = spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION;
		if (!GardensRosterRules.appliesTo(FungalGardenTravelHelper.FUNGAL_GARDENS.equals(dimension), natural)) {
			return false;
		}
		var id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
		if (!Hemomancy.MOD_ID.equals(id.getNamespace())) return false;
		var stats = GardensRosterRules.stats(id.getPath());
		if (stats.isEmpty()) return false;
		setBase(mob.getAttribute(Attributes.MAX_HEALTH), stats.get().maxHealth());
		setBase(mob.getAttribute(Attributes.ARMOR), stats.get().armor());
		setBase(mob.getAttribute(Attributes.ATTACK_DAMAGE), stats.get().attackDamage());
		mob.setHealth(mob.getMaxHealth());
		return true;
	}

	private static void setBase(AttributeInstance attribute, double value) {
		if (attribute != null) attribute.setBaseValue(value);
	}
}
