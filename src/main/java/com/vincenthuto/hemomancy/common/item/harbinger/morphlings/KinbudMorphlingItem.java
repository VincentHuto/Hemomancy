package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Kinbud, the Kindling's strain (Pyrrhiza gemmans): stinging and grown regeneration. Cultivated Mending regrows a
 * share of each wound over ten seconds; Nematocyst Sting applies Stung; Grasping Crown seizes a thrice-struck target;
 * Second Head buds a striking polyp head in a fight; Kindled regrows everything taken in a window, up to a cap. The
 * Second Head is body, not a summon (see {@link KinbudSecondHead}). Numbers in {@link KinbudRules}; heat from
 * {@link MorphlingHeatRules} through {@link MorphlingHomeConditions#kinbud}.
 */
public class KinbudMorphlingItem extends HouseMorphlingItem {
	private static final String LAST_COMBAT = "kinbud.last_combat";
	private static final String CROWN_TARGET = "kinbud.crown.target";
	private static final String CROWN_HITS = "kinbud.crown.hits";
	private static final String CROWN_LAST = "kinbud.crown.last";
	private static final String RESEIZE = "kinbud.reseize.";
	private static final String HEAD_COOLDOWN = "SecondHead";
	private static final String KINDLED_HOME = "kinbud.kindled_home";

	public KinbudMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.KINBUD;
	}

	private static boolean kindled(Player player) {
		MorphlingRegrowthQueue queue = MorphlingRegrowthQueue.peek(player.getUUID());
		return queue != null && queue.isKindled(player.level().getGameTime());
	}

	private int stingTicks(Player player, ItemStack stack) {
		return KinbudRules.stingTicks(stage(stack), home(player));
	}

	private static int stingAmplifier(Player player) {
		return KinbudRules.stingAmplifier(kindled(player) && MorphlingSession.get(player, KINDLED_HOME) > 0L);
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		long now = player.level().getGameTime();
		MorphlingRegrowthQueue queue = MorphlingRegrowthQueue.peek(player.getUUID());
		if (queue != null) {
			float due = queue.regrow(now);
			if (due > 0.0F) MorphlingHealing.healHost(player, due);
		}
		int stage = stage(stack);
		if (stage >= 4) {
			boolean recent = now - MorphlingSession.get(player, LAST_COMBAT) <= KinbudRules.HEAD_RECENT_COMBAT
					&& MorphlingSession.get(player, LAST_COMBAT) > 0L;
			boolean home = home(player);
			if (recent && KinbudSecondHead.heads(player) == 0
					&& ready(player, stack, HEAD_COOLDOWN, home ? KinbudRules.HEAD_HOME_COOLDOWN : KinbudRules.HEAD_COOLDOWN)) {
				spend(player, stack, HEAD_COOLDOWN);
				int heads = kindled(player) ? KinbudRules.kindledHeads(MorphlingSession.get(player, KINDLED_HOME) > 0L) : 1;
				KinbudSecondHead.bud(player, heads, KinbudRules.HEAD_TICKS);
			}
			try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
				KinbudSecondHead.tick(player, stingTicks(player, stack), stingAmplifier(player));
			}
		}
		if (proc(player, 200)) MorphlingSession.prune(player, RESEIZE);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		onEquippedHurt(player, stack, source, amount, true);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount,
			boolean allowReactions) {
		long now = player.level().getGameTime();
		MorphlingSession.put(player, LAST_COMBAT, now);
		MorphlingRegrowthQueue.of(player.getUUID()).queueHit(amount, KinbudRules.mendingShare(stage(stack), home(player)),
				player.getHealth() <= 0.0F, now, KinbudRules.REGROWTH_TICKS);
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		if (player.level().isClientSide) return;
		long now = player.level().getGameTime();
		MorphlingSession.put(player, LAST_COMBAT, now);
		int stage = stage(stack);
		if (stage < 2) return;
		target.addEffect(new MobEffectInstance(EffectInit.stung, stingTicks(player, stack), stingAmplifier(player), false,
				true, true), player);
		if (stage < 3) return;
		boolean same = MorphlingSession.get(player, CROWN_TARGET) == target.getId()
				&& now - MorphlingSession.get(player, CROWN_LAST) <= KinbudRules.CROWN_WINDOW;
		long hits = same ? MorphlingSession.get(player, CROWN_HITS) + 1L : 1L;
		MorphlingSession.put(player, CROWN_TARGET, target.getId());
		MorphlingSession.put(player, CROWN_LAST, now);
		MorphlingSession.put(player, CROWN_HITS, hits);
		String reseize = RESEIZE + target.getUUID();
		if (hits >= KinbudRules.CROWN_HITS && now >= MorphlingSession.get(player, reseize)) {
			MorphlingSession.put(player, CROWN_HITS, 0L);
			MorphlingSession.put(player, reseize, now + KinbudRules.CROWN_RESEIZE);
			MorphlingLock.pin(target, KinbudRules.CROWN_ROOT_TICKS);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, KinbudRules.CROWN_WITHER_TICKS, 1, false, true, true),
					player);
		}
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player)) return false;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "Kindled", KinbudRules.KINDLED_COST,
					KinbudRules.KINDLED_COOLDOWN, KinbudRules.KINDLED_STRAIN, 1,
					MorphlingItem.primalSource("kindled"))) return false;
			boolean home = home(player);
			long now = player.level().getGameTime();
			MorphlingRegrowthQueue.of(player.getUUID()).kindle(now + KinbudRules.KINDLED_TICKS, KinbudRules.kindledCap(home));
			if (home) MorphlingSession.put(player, KINDLED_HOME, 1L);
			else MorphlingSession.remove(player, KINDLED_HOME);
			KinbudSecondHead.bud(player, KinbudRules.kindledHeads(home), KinbudRules.HEAD_TICKS);
			return true;
		}
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int heads = KinbudSecondHead.heads(player);
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.SECOND_HEAD, heads >= 1);
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.SECOND_HEAD_TWIN, heads >= 2);
		return MorphlingActiveState.set(flags, MorphlingActiveState.KINDLED, kindled(player));
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "nematocyst_sting", "grasping_crown", "second_head", "kindled");
	}
}
