package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * How far a verb reaches: one rule for Projection and Absorption, for the cell hands and the Living Staff. The bare
 * hand reaches {@link #BASE}; the Staff reaches its focus range (6 blocks, plus 1.5 per Living Conduit level and
 * 0.75 per Hematic Focus level, plus the Vesper terms, plus what its growth rings add: 0.5, 1.5 or 2.5 blocks,
 * core-loop Track 8); Sanguine Reach multiplies either. A Sculpting may scale the result ({@link #scaled}) but never
 * replaces it.
 */
public final class VerbReach {
	public static final double BASE = SanguineProjectionTargeting.PROJECTION_REACH;
	/** The most and least a Sculpting may scale reach by. */
	public static final double MIN_SCALE = 0.25D;
	public static final double MAX_SCALE = 3.0D;

	private VerbReach() {
	}

	/** The reach of {@code verb} for {@code user} with what they are channelling through now. */
	public static double of(LivingEntity user, Verb verb) {
		if (user == null) return BASE;
		// Another player on this client: their skills and Staff are not synced here, so use what their server sent.
		if (user.level().isClientSide && user instanceof Player player) {
			java.util.OptionalDouble sent = com.vincenthuto.hemomancy.common.util.ClientBridge.get().onlookerReach(player, verb);
			if (sent.isPresent()) return sent.getAsDouble();
		}
		double multiplier = user instanceof Player player ? SkillPointHelper.getSanguineReachMultiplier(player) : 1.0D;
		LivingStaffFocusProfile focus = staffFocus(user);
		return compute(focus != null, focus, multiplier);
	}

	/** {@code staff} chooses the Staff's focus range over the bare hand's; {@code reachMultiplier} is Sanguine Reach. */
	public static double compute(boolean staff, LivingStaffFocusProfile focus, double reachMultiplier) {
		double base = staff ? LivingStaffFocusRules.absorptionRange(focus) : BASE;
		double multiplier = Double.isFinite(reachMultiplier) ? Math.max(1.0D, reachMultiplier) : 1.0D;
		return base * multiplier;
	}

	/** A Sculpting's reach: the verb's reach times the Sculpting's scale, held between {@link #MIN_SCALE} and {@link #MAX_SCALE}. */
	public static double scaled(double reach, double scale) {
		double safe = Double.isFinite(scale) ? Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale)) : 1.0D;
		return reach * safe;
	}

	/** The Staff's focus when {@code user} channels (or, idle, holds) a Living Staff; null for a bare hand. */
	public static LivingStaffFocusProfile staffFocus(LivingEntity user) {
		ItemStack tool = user.isUsingItem() ? user.getUseItem() : user.getMainHandItem();
		if (!(tool.getItem() instanceof LivingStaffItem) || !(user instanceof Player player)) return null;
		return LivingStaffFocusProfile.fromPlayer(player, HemoCapabilityAccess.getLivingStaffProgress(player));
	}
}
