package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ArmorSetBonusRules;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Mermaid's Purse, the Shared Wake's strain (Sanguibursa communis): open-ocean speed, hunting, and a blood economy
 * shared with the school. Purse Siphon and swim speed; Feed Banking (more when hunting at speed); School's Share
 * spends banked blood to heal whoever is lowest; Raise the Sail lends Dolphin's Grace to allies in the wake and arms a
 * bill strike; Shared Wake links the group for twenty seconds. Numbers in {@link MermaidsPurseRules}.
 */
public class MermaidsPurseMorphlingItem extends HouseMorphlingItem {
	public static final ResourceLocation SWIM_SPEED = Hemomancy.rloc("purse_swim_speed");
	private static final BloodAccounting.Source SCHOOLS_SHARE = BloodAccounting.Source.of("schools_share",
			Category.MORPHLING, false, false);
	private static final BloodAccounting.Source SHARED_WAKE = MorphlingItem.primalSource("shared_wake");
	private static final String SCHOOL_COOLDOWN = "SchoolsShare";
	private static final String LAST_SPRINT_SWIM = "purse.last_sprint_swim";
	private static final String BILL_READY = "purse.bill_ready_at";
	private static final String WAKE_WINDOW = "purse.shared_wake_until";
	private static final String IN_BILL = "purse.in_bill";
	private static final int SAIL_REFRESH = 10;

	public MermaidsPurseMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.MERMAIDS_PURSE;
	}

	public static boolean isSprintSwimming(Player player) {
		return player.isSwimming() && player.isInWater();
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		boolean home = home(player);
		double swim = MermaidsPurseRules.swimBonus(stage, home, ArmorSetBonusRules.pelagicSwimMultiplier(
				ArmorSetHelper.hasFullPrismaticSet(player), ArmorSetHelper.hasFullPhantasmalBloodlust(player)));
		AttributeModifiers.sync(player.getAttribute(NeoForgeMod.SWIM_SPEED), SWIM_SPEED, swim,
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE, player.isInWater() && swim > 0.0D);
		if (isSprintSwimming(player)) MorphlingSession.put(player, LAST_SPRINT_SWIM, player.level().getGameTime());
		if (proc(player, MermaidsPurseRules.PROC_TICKS) && player instanceof ServerPlayer server) {
			BloodFlowLedger.applyCirculationIncome(server, HemoCapabilityAccess.getBloodVolume(player), "purse_siphon",
					"blood_source.hemomancy.purse_siphon", Category.MORPHLING, MermaidsPurseRules.siphon(stage),
					MermaidsPurseRules.PROC_TICKS, CirculationIncomeHelper.IncomeChannel.MORPHLING);
		}
		if (stage >= 3 && proc(player, 10)) schoolsShare(player, stack, stage);
		if (stage >= 4 && isSprintSwimming(player) && proc(player, SAIL_REFRESH)) raiseTheSail(player);
	}

	private void schoolsShare(Player player, ItemStack stack, int stage) {
		if (!ready(player, stack, SCHOOL_COOLDOWN, MermaidsPurseRules.SCHOOL_COOLDOWN)) return;
		List<LivingEntity> school = new ArrayList<>(MorphlingAllies.allies(player, MermaidsPurseRules.SCHOOL_RANGE));
		school.add(player);
		LivingEntity lowest = school.stream()
				.filter(member -> member.getHealth() <= member.getMaxHealth() * MermaidsPurseRules.SCHOOL_TRIGGER)
				.min(Comparator.comparingDouble(member -> member.getHealth() / member.getMaxHealth())).orElse(null);
		if (lowest == null || !payFromPurse(player, MermaidsPurseRules.SCHOOL_COST)) return;
		spend(player, stack, SCHOOL_COOLDOWN);
		float heal = MermaidsPurseRules.schoolHeal(stage);
		if (lowest == player) MorphlingHealing.healHost(player, heal);
		else lowest.heal(heal);
	}

	/** Pays from banked blood first, then the host's own volume. */
	private static boolean payFromPurse(Player player, double cost) {
		double banked = Math.min(cost, BorrowedBloodReserve.get(player));
		double rest = cost - banked;
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		if (rest > 0.0D && (!volume.isActive() || volume.getBloodVolume() <= rest)) return false;
		double covered = BorrowedBloodReserve.drainToCover(player, banked);
		rest = cost - covered;
		return rest <= 0.0D || BloodAccounting.pay(player, rest, SCHOOLS_SHARE).satisfied();
	}

	private static void raiseTheSail(Player player) {
		int ticks = SAIL_REFRESH + 20;
		player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, ticks, 0, true, false, true));
		Vec3 facing = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
		for (LivingEntity ally : MorphlingAllies.allies(player, MermaidsPurseRules.SAIL_ALLY_RANGE)) {
			Vec3 offset = ally.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
			if (offset.lengthSqr() > 1.0E-4D && offset.normalize().dot(facing) > 0.0D) continue;
			ally.addEffect(ally.isInWater()
					? new MobEffectInstance(MobEffects.DOLPHINS_GRACE, ticks, 0, true, false, true)
					: new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 0, true, false, true));
		}
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player)) return false;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "SharedWake", MermaidsPurseRules.WAKE_COST,
					MermaidsPurseRules.WAKE_COOLDOWN, MermaidsPurseRules.WAKE_STRAIN, 0, SHARED_WAKE)) return false;
			MorphlingSession.open(player, WAKE_WINDOW, MermaidsPurseRules.WAKE_TICKS);
			player.displayClientMessage(power("shared_wake_opens"), true);
			return true;
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		if (player.level().isClientSide || MorphlingSession.get(player, IN_BILL) > 0L) return;
		int stage = stage(stack);
		long now = player.level().getGameTime();
		boolean sprintSwimming = isSprintSwimming(player)
				|| now - MorphlingSession.get(player, LAST_SPRINT_SWIM) <= MermaidsPurseRules.BILL_WINDOW;
		boolean bill = stage >= 4 && sprintSwimming && now >= MorphlingSession.get(player, BILL_READY);
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, player.damageSources().magic())) {
			if (bill) {
				MorphlingSession.put(player, BILL_READY, now + MermaidsPurseRules.BILL_LIMIT);
				MorphlingSession.put(player, IN_BILL, 1L);
				try {
					target.invulnerableTime = 0;
					target.hurt(player.damageSources().playerAttack(player), MermaidsPurseRules.BILL_DAMAGE);
				} finally {
					MorphlingSession.remove(player, IN_BILL);
				}
			}
			if (stage >= 2) {
				double accepted = BorrowedBloodReserve.deposit(player,
						MermaidsPurseRules.bankDeposit(stage, amount, isSprintSwimming(player), bill));
				if (accepted > 0.0D) MorphlingItem.addHusbandryProgress(stack, 1);
			}
			if (MorphlingSession.isOpen(player, WAKE_WINDOW)) {
				float shared = Math.min(MermaidsPurseRules.WAKE_HEAL_CAP, amount * 0.18F);
				List<LivingEntity> group = new ArrayList<>(MorphlingAllies.allies(player, MermaidsPurseRules.WAKE_RANGE));
				group.add(player);
				for (LivingEntity member : group) {
					if (member.getHealth() >= member.getMaxHealth()) continue;
					if (member == player) MorphlingHealing.healHost(player, shared);
					else member.heal(shared);
				}
				BorrowedBloodReserve.deposit(player, Math.min(MermaidsPurseRules.WAKE_BANK_CAP, amount * 8.0D));
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		if (player.level().isClientSide) return;
		double accepted = BorrowedBloodReserve.deposit(player,
				MorphlingFeedBankRules.killDeposit(stage(stack), victim.getMaxHealth()));
		if (accepted > 0.0D) MorphlingItem.addHusbandryProgress(stack, 2);
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.SAIL_RAISED,
				stage(stack) >= 4 && isSprintSwimming(player));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.SHARED_WAKE, MorphlingSession.isOpen(player, WAKE_WINDOW));
		double cap = HemoServerConfig.BORROWED_BLOOD_CAP == null ? 0.0D : HemoServerConfig.BORROWED_BLOOD_CAP.get();
		return MorphlingActiveState.withFill(flags, cap <= 0.0D ? 0.0D : BorrowedBloodReserve.get(player) / cap);
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "feed_banking", "schools_share", "raise_the_sail", "shared_wake");
	}
}
