package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Dead Man's Rope, the Woven Current's strain (Funiculospora nexilis): perception and linking. Lateral Strands reveal
 * the living nearby; Lifeline catches falls; Strand Link tethers an ally, a companion or a block (a grapple when
 * alone); Colony holds three strands and spreads damage among the linked; Red Rope strings a looked-at target, pulling
 * a hostile in and rooting it or hauling an ally out of danger. The rope is physical: it never controls the target.
 * Numbers in {@link DeadMansRopeRules}.
 */
public class DeadMansRopeMorphlingItem extends HouseMorphlingItem {
	private static final String LIFELINE_WINDOW = "rope.lifeline_until";
	private static final int REVEAL_TICKS = 45;

	public DeadMansRopeMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.DEAD_MANS_ROPE;
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		if (proc(player, 10) && MorphlingLinks.prune(player, DeadMansRopeRules.maxLinks(stage))) {
			MorphlingHomeConditions.forget(player);
		}
		if (proc(player, DeadMansRopeRules.PROC_TICKS)) {
			double range = DeadMansRopeRules.revealRange(stage, home(player));
			for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(range), other -> other != player && other.isAlive()
							&& other.distanceToSqr(player) <= range * range)) {
				living.addEffect(new MobEffectInstance(MobEffects.GLOWING, REVEAL_TICKS, 0, true, false, false));
			}
			for (LivingEntity partner : MorphlingLinks.partners(player)) {
				if (partner instanceof Player) {
					partner.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis, REVEAL_TICKS, stage, true,
							false, true));
				}
			}
		}
		if (isReeling(player)) reel(player);
	}

	private static BlockPos reelAnchor(Player player) {
		return MorphlingLinks.links(player).stream().filter(MorphlingLinks.Link::isBlock)
				.map(MorphlingLinks.Link::anchor).findFirst().orElse(null);
	}

	private static boolean isReeling(Player player) {
		BlockPos anchor = reelAnchor(player);
		return anchor != null && player.isShiftKeyDown()
				&& Vec3.atCenterOf(anchor).distanceToSqr(player.getEyePosition()) > 4.0D;
	}

	private static void reel(Player player) {
		BlockPos anchor = reelAnchor(player);
		Vec3 pull = Vec3.atCenterOf(anchor).subtract(player.getEyePosition()).normalize().scale(DeadMansRopeRules.REEL_SPEED);
		player.setDeltaMovement(player.getDeltaMovement().scale(0.5D).add(pull));
		player.resetFallDistance();
		player.hurtMarked = true;
	}

	@Override
	public boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		if (!DeadMansRopeRules.lifelineCatches(stage(stack), distance)) return false;
		MorphlingSession.open(player, LIFELINE_WINDOW, DeadMansRopeRules.LIFELINE_STRAND_TICKS);
		return true;
	}

	@Override
	public boolean onEquippedSneakUse(Player player, ItemStack stack, Entity target, BlockPos pos) {
		int max = DeadMansRopeRules.maxLinks(stage(stack));
		if (max <= 0) return false;
		if (target instanceof LivingEntity living) {
			if (!MorphlingAllies.isAlly(player, living)
					|| living.distanceToSqr(player) > DeadMansRopeRules.LINK_LENGTH * DeadMansRopeRules.LINK_LENGTH) {
				return false;
			}
			MorphlingLinks.toggle(player, MorphlingLinks.Link.toEntity(living), max);
		} else if (pos != null) {
			if (Vec3.atCenterOf(pos).distanceToSqr(player.position())
					> DeadMansRopeRules.LINK_LENGTH * DeadMansRopeRules.LINK_LENGTH) return false;
			MorphlingLinks.toggle(player, MorphlingLinks.Link.toBlock(pos), max);
		} else {
			return false;
		}
		MorphlingHomeConditions.forget(player);
		return true;
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player)) return false;
		LivingEntity target = lookTarget(player, DeadMansRopeRules.RED_ROPE_RANGE);
		if (target == null) {
			player.displayClientMessage(power("red_rope_no_target"), true);
			return false;
		}
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "RedRope", DeadMansRopeRules.RED_ROPE_COST,
					DeadMansRopeRules.RED_ROPE_COOLDOWN, DeadMansRopeRules.RED_ROPE_STRAIN, 0,
					MorphlingItem.primalSource("red_rope"))) return false;
			Vec3 toHost = player.position().subtract(target.position());
			if (MorphlingAllies.isAlly(player, target)) {
				target.setDeltaMovement(toHost.normalize().scale(Math.min(2.0D, toHost.length() * 0.25D)).add(0.0D, 0.3D, 0.0D));
				target.resetFallDistance();
			} else {
				if (!ManipulationReactiveEvents.isBoss(target)) {
					target.setDeltaMovement(toHost.normalize().scale(Math.min(1.6D, toHost.length() * 0.2D)).add(0.0D, 0.25D, 0.0D));
				}
				MorphlingLock.pin(target, DeadMansRopeRules.RED_ROPE_ROOT_TICKS);
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DeadMansRopeRules.RED_ROPE_ROOT_TICKS, 0, false,
						true, true), player);
			}
			target.hurtMarked = true;
			return true;
		}
	}

	/** The nearest living body in the host's sight line: an ally to rescue or something the host may harm. */
	static LivingEntity lookTarget(Player player, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		AABB area = player.getBoundingBox().inflate(range);
		return player.level().getEntitiesOfClass(LivingEntity.class, area,
						target -> target != player && target.isAlive()
								&& target.distanceToSqr(player) <= range * range
								&& (MorphlingAllies.isAlly(player, target) || ManipulationCombatHelper.canHarm(player, target))
								&& target.getBoundingBox().getCenter().subtract(eye).normalize().dot(look) > 0.9D
								&& player.hasLineOfSight(target))
				.stream().min(Comparator.comparingDouble(target -> target.distanceToSqr(player))).orElse(null);
	}

	@Override
	public void onUnequipped(Player player, ItemStack stack) {
		MorphlingLinks.clear(player);
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.REELING, isReeling(player));
		return MorphlingActiveState.set(flags, MorphlingActiveState.LIFELINE, MorphlingSession.isOpen(player, LIFELINE_WINDOW));
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "lifeline", "strand_link", "colony", "red_rope");
	}
}
