package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.AbsorptionRiders;
import com.vincenthuto.hemomancy.common.damage.AbsorptionRiders.Effect;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Applies {@link AbsorptionRiders} to a draw: which states the creature carries, how far it may be drawn from (a
 * strand's brand doubles it and picks it first), and the riders' effects after each pulse. The stripped-armour clock is
 * session state, cleared through {@code TransientServerStateEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AbsorptionRiderEffects {
	private static final ResourceLocation STRIP = Hemomancy.rloc("absorption_rider_strip");
	/** Creatures whose armour a Lodestone draw stripped, to the game time it comes back. */
	private static final Map<LivingEntity, Long> STRIPPED = new WeakHashMap<>();

	private AbsorptionRiderEffects() {
	}

	/** The rider states {@code target} carries now (Veiled, the caster's own, excepted). */
	public static Set<SchoolState> statesOn(LivingEntity target) {
		Set<SchoolState> states = EnumSet.noneOf(SchoolState.class);
		for (SchoolState state : SchoolState.values()) {
			if (state != SchoolState.VEILED && SchoolStates.has(target, state)) states.add(state);
		}
		return states;
	}

	public static AbsorptionRiders.Rider riderFor(LivingEntity target) {
		return AbsorptionRiders.combine(statesOn(target));
	}

	/** Whether {@code target} carries a strand brand that names {@code drawer}. */
	public static boolean brandedFor(LivingEntity target, Player drawer) {
		return StrandService.brandFor(target, drawer.getUUID()) != null;
	}

	/**
	 * How far {@code drawer} may draw {@code target} from: the verb's reach, twice it for their own brand, half again
	 * for an Illuminated creature.
	 */
	public static double reachFor(LivingEntity target, Player drawer, double reach) {
		double scale = riderFor(target).reach();
		if (brandedFor(target, drawer)) scale = Math.max(scale, BrandRules.ABSORPTION_REACH);
		return reach * scale;
	}

	/** The furthest any marked creature may be drawn from, for the gather's box. */
	public static double widestReach(double reach) {
		return reach * Math.max(BrandRules.ABSORPTION_REACH, AbsorptionRiders.of(SchoolState.ILLUMINATED).reach());
	}

	/** After a pulse that drew {@code drawn} from {@code target}: the riders' effects. */
	public static void after(ServerPlayer player, LivingEntity target, AbsorptionRiders.Rider rider, double drawn) {
		if (drawn <= 0.0D) return;
		if (rider.has(Effect.PULL) && target.isAlive()) {
			Vec3 toward = player.position().subtract(target.position());
			if (toward.lengthSqr() > 1.0E-4D) {
				Vec3 pull = toward.normalize().scale(AbsorptionRiders.PULL_SPEED);
				target.push(pull.x, Math.min(0.08D, Math.max(0.0D, pull.y)), pull.z);
				target.hurtMarked = true;
			}
		}
		if (rider.has(Effect.HOLD_STILL) && target.isAlive()) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, AbsorptionRiders.SLOW_TICKS, 1, false, false, true),
					player);
			if (SchoolStates.levels(target, SchoolState.RIME) >= 3) {
				target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
				target.hurtMarked = true;
			}
		}
		if (rider.has(Effect.STRIP_ARMOUR) && target.isAlive()) {
			AttributeModifiers.sync(target.getAttribute(Attributes.ARMOR), STRIP, AbsorptionRiders.STRIP_ARMOUR,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, true);
			STRIPPED.put(target, target.level().getGameTime() + AbsorptionRiders.STRIP_TICKS);
		}
		if (rider.has(Effect.UNTARGET_AND_VEIL)) {
			if (target instanceof Mob mob) {
				mob.setTarget(null);
				mob.setLastHurtByMob(null);
			}
			SchoolStates.apply(player, player, SchoolState.VEILED, AbsorptionRiders.VEIL_TICKS);
		}
		if (rider.has(Effect.SPREAD_ON_KILL) && !target.isAlive()) {
			for (LivingEntity near : target.level().getEntitiesOfClass(LivingEntity.class,
					target.getBoundingBox().inflate(AbsorptionRiders.SPREAD_RADIUS),
					near -> near != target && ManipulationCombatHelper.canHarm(player, near))) {
				SchoolStates.apply(player, near, SchoolState.NECROSIS, 0);
			}
		}
		if (rider.has(Effect.ARC)) arcTarget(player, target).ifPresent(other ->
				BloodAbsorptionItem.drawWithoutRiders(player.level(), player, other, drawn * AbsorptionRiders.ARC_SHARE));
	}

	/** The nearest other creature within the arc's range that carries a rider state or the drawer's brand. */
	static Optional<LivingEntity> arcTarget(ServerPlayer player, LivingEntity from) {
		return from.level().getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(AbsorptionRiders.ARC_RANGE),
						other -> other != from && other.isAlive() && ManipulationCombatHelper.canHarm(player, other)
								&& BloodAbsorptionItem.isValidAbsorptionTarget(player, other)
								&& (AbsorptionRiders.marks(statesOn(other)) || brandedFor(other, player)))
				.stream().min(Comparator.comparingDouble(from::distanceToSqr));
	}

	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		if (STRIPPED.isEmpty()) return;
		STRIPPED.entrySet().removeIf(entry -> {
			LivingEntity target = entry.getKey();
			if (target.isAlive() && target.level().getGameTime() < entry.getValue()) return false;
			AttributeModifiers.sync(target.getAttribute(Attributes.ARMOR), STRIP, AbsorptionRiders.STRIP_ARMOUR,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
			return true;
		});
	}

	/** Gives back every stripped armour and forgets the clock. */
	public static void clearSessionState() {
		STRIPPED.forEach((target, until) -> AttributeModifiers.sync(target.getAttribute(Attributes.ARMOR), STRIP,
				AbsorptionRiders.STRIP_ARMOUR, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false));
		STRIPPED.clear();
	}
}
