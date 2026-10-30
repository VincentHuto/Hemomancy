package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Blood Binding as the Rite of Possession reads it. A binding cast on a would-be surrogate remembers its caster, and
 * the rite takes only a surrogate its caster holds. Cast on a surrogate standing on a formed Qliphoth grand floor, the
 * binding holds without end, so the body cannot wander off the floor while the rite is prepared: it lets go when the
 * floor stops forming, or when a rite run on that floor ends.
 */
public final class SurrogateTethers {
	public static final ResourceLocation QLIPHOTH_GRAND = Hemomancy.rloc("qliphoth_grand");

	private SurrogateTethers() {
	}

	/** Whether {@code caster}'s Blood Binding holds {@code body} now. */
	public static boolean boundBy(LivingEntity body, UUID caster) {
		return body.hasEffect(EffectInit.blood_binding)
				&& caster.equals(HemoCapabilityAccess.getSurrogateTether(body).binder());
	}

	/** The binding to lay on {@code target}: the timed one, or one without end for a surrogate on a Qliphoth grand floor. */
	public static MobEffectInstance binding(LivingEntity target, MobEffectInstance timed) {
		if (!(target.level() instanceof ServerLevel level) || PossessionService.formOf(target) == null
				|| qliphothFocus(level, target) == null) {
			return timed;
		}
		return new MobEffectInstance(EffectInit.blood_binding, MobEffectInstance.INFINITE_DURATION,
				timed.getAmplifier(), timed.isAmbient(), timed.isVisible(), timed.showIcon());
	}

	/** Records who laid {@code applied} on a would-be surrogate, and the floor holding it if it holds without end. */
	public static void bound(Player caster, LivingEntity target, MobEffectInstance applied) {
		if (!(target.level() instanceof ServerLevel level) || PossessionService.formOf(target) == null) return;
		BlockPos focus = applied.isInfiniteDuration() ? qliphothFocus(level, target) : null;
		HemoCapabilityAccess.getSurrogateTether(target).bind(caster.getUUID(), focus);
	}

	/** Binds {@code target} for {@code caster} as Blood Binding would, outside a cast. */
	public static boolean bind(Player caster, LivingEntity target, int ticks) {
		MobEffectInstance binding = binding(target,
				new MobEffectInstance(EffectInit.blood_binding, ticks, 0, false, false, true));
		if (!target.addEffect(binding, caster)) return false;
		bound(caster, target, binding);
		return true;
	}

	/**
	 * A held binding's check, from the effect's own tick: it lets go once its floor stops forming or a rite run on the
	 * floor has ended.
	 */
	public static void tick(LivingEntity body, MobEffectInstance binding) {
		if (!binding.isInfiniteDuration() || !(body.level() instanceof ServerLevel level)
				|| body.tickCount % SurrogateTetherRules.CHECK_TICKS != 0) {
			return;
		}
		SurrogateTether tether = HemoCapabilityAccess.getSurrogateTether(body);
		BlockPos focus = tether.focus();
		if (focus == null) {
			// Held by something this class did not lay; leave it alone.
			return;
		}
		boolean running = riteAt(level, focus);
		if (running) tether.markRite();
		boolean formed = level.isLoaded(focus) && isQliphothGrand(level, focus);
		if (SurrogateTetherRules.hold(formed, tether.ritePassed(), running) == SurrogateTetherRules.Hold.RELEASE) {
			release(body);
		}
	}

	/** Lets go of a held binding and forgets its caster. A timed binding runs out on its own. */
	public static void release(LivingEntity body) {
		MobEffectInstance binding = body.getEffect(EffectInit.blood_binding);
		if (binding != null && binding.isInfiniteDuration()) body.removeEffect(EffectInit.blood_binding);
		HemoCapabilityAccess.getSurrogateTether(body).clear();
	}

	/** The binding came off (expired, cured, released): its caster is forgotten. */
	public static void unbound(LivingEntity body) {
		HemoCapabilityAccess.getSurrogateTether(body).clear();
	}

	/** The Focus of a formed Qliphoth grand floor {@code body} stands on, or null. */
	@Nullable
	public static BlockPos qliphothFocus(ServerLevel level, LivingEntity body) {
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(QLIPHOTH_GRAND).orElse(null);
		if (floor == null) return null;
		int half = CardinalRiteFloorGeometry.halfWidth(floor);
		BlockPos feet = body.blockPosition();
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= SurrogateTetherRules.HEIGHT; dy++) {
			for (int dx = -half; dx <= half; dx++) {
				for (int dz = -half; dz <= half; dz++) {
					at.set(feet.getX() + dx, feet.getY() - dy, feet.getZ() + dz);
					if (!level.getBlockState(at).is(BlockInit.cardinal_focus.get())) continue;
					BlockPos focus = at.immutable();
					if (SurrogateTetherRules.onFloor(feet.getX() - focus.getX(), feet.getY() - focus.getY(),
							feet.getZ() - focus.getZ(), half) && isQliphothGrand(level, focus)) {
						return focus;
					}
				}
			}
		}
		return null;
	}

	private static boolean isQliphothGrand(ServerLevel level, BlockPos focus) {
		return CardinalRiteStationMatcher.formedFloor(level, focus)
				.map(formed -> formed.floor().id().equals(QLIPHOTH_GRAND)).orElse(false);
	}

	private static boolean riteAt(ServerLevel level, BlockPos focus) {
		return CardinalRiteSavedData.get(level).getActiveRites().values().stream()
				.anyMatch(rite -> focus.equals(rite.getCenterPos()));
	}
}
