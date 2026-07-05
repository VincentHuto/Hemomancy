package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.CrawlingChoirHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.PurityGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.effect.MnemonicCandleRules;
import com.vincenthuto.hemomancy.common.effect.MnemonicPotionRules;
import com.vincenthuto.hemomancy.common.event.BorrowedBloodRules;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.CheapBloodInfusionHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleResonanceState;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipCooldownPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public class BloodManipulation implements EntityCastableManipulation {
	public static BloodManipulation BLANK = new BloodManipulation("No Selected", 0, 0, 0, EnumManipulationType.QUICK,
			EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	/*
	 * Reads a NBT tag and converts it to a manipulation
	 */
	public static BloodManipulation deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("cost") && nbt.contains("level") && nbt.contains("type")
					&& nbt.contains("tendency") && nbt.contains("rank") && nbt.contains("section")) {
				BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
						nbt.getDouble("level"), nbt.getDouble("xpcost"),
						EnumManipulationType.valueOf(nbt.getString("type")),
						EnumManipulationRank.valueOf(nbt.getString("rank")),
						EnumBloodTendency.valueOf(nbt.getString("tendency")),
						EnumVeinSections.valueOf(nbt.getString("section")));
				if (nbt.contains("cooldown")) {
					manip.cooldownTicks = nbt.getInt("cooldown");
				}
				if (nbt.contains("secondary_tendency")) {
					manip.secondaryTend = EnumBloodTendency.valueOf(nbt.getString("secondary_tendency"));
				}

				return manip;
			}
		}
		return null;
	}
	String name;
	double cost, alignLevel, xpCost;
	EnumBloodTendency tend;
	@Nullable
	EnumBloodTendency secondaryTend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	EnumManipulationType type;

	private static final double TICKS_PER_SECOND = 20.0;
	private static final String MNA_MANIP_COMBO_HELPER = "com.vincenthuto.hemomancy.compat.mna.spell.ManipComboHelper";

	private static final Map<UUID, Long> UNIVERSAL_COOLDOWN_MAP = new ConcurrentHashMap<>();

	int cooldownTicks;

	/**
	 * Transient — set at registration time via {@link #setDrudgeAction(DrudgeAction, String)}.
	 * Not serialized to NBT; must be resolved to the registry instance after loading.
	 */
	private transient DrudgeAction drudgeAction = null;
	private transient String drudgeDescription = null;

	public BloodManipulation(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.type = type;
		this.tend = tendency;
		this.rank = rank;
		this.section = section;
		this.cooldownTicks = 0;
	}

	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {

	}

	@Override
	public boolean castFromEntity(ManipulationCastContext context) {
		return EntityManipulationEffects.cast(this, context);
	}

	public double getAlignLevel() {
		return alignLevel;
	}

	public double getCost() {
		return cost;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return HLTextUtils.convertInitToLang(name);
	}

	public EnumManipulationRank getRank() {
		return rank;
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	@Nullable
	public EnumBloodTendency getSecondaryTend() {
		return secondaryTend;
	}

	public EnumManipulationType getType() {
		return type;
	}

	public double getXpCost() {
		return xpCost;
	}

	public int getCooldownTicks() {
		return cooldownTicks;
	}

	public BloodManipulation setCooldownTicks(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
		return this;
	}

	public BloodManipulation setSecondaryTend(@Nullable EnumBloodTendency secondaryTend) {
		this.secondaryTend = secondaryTend != tend ? secondaryTend : null;
		return this;
	}

	/**
	 * Registers the drudge-specific behavior for this manipulation.
	 *
	 * @param action      the {@link DrudgeAction} lambda; use
	 *                    {@link DrudgeAction#DRUDGE_UNSUPPORTED} to mark the
	 *                    manipulation as incompatible with Drudges
	 * @param description a short player-facing description shown in the Memory
	 *                    item tooltip (e.g. {@code "Slows all hostiles in area"})
	 * @return {@code this} for fluent chaining
	 */
	public BloodManipulation setDrudgeAction(DrudgeAction action, String description) {
		this.drudgeAction = action;
		this.drudgeDescription = description;
		return this;
	}

	/**
	 * Returns the registered {@link DrudgeAction} for this manipulation, or an
	 * empty Optional if none has been registered yet.
	 */
	public Optional<DrudgeAction> getDrudgeAction() {
		return Optional.ofNullable(drudgeAction);
	}

	/**
	 * Returns the short drudge-behavior description string, or empty if none was
	 * set.
	 */
	public Optional<String> getDrudgeDescription() {
		return Optional.ofNullable(drudgeDescription);
	}

	public boolean isOnCooldown(Player player) {
		if (cooldownTicks <= 0) {
			return false;
		}
		if (ignoresCooldown(player)) {
			return false;
		}
		return isAnyManipOnCooldown(player);
	}

	public boolean ignoresCooldown(Player player) {
		return false;
	}

	public static boolean isAnyManipOnCooldown(Player player) {
		Long expiryTick = UNIVERSAL_COOLDOWN_MAP.get(player.getUUID());
		if (expiryTick == null) {
			return false;
		}
		if (player.level().getGameTime() >= expiryTick) {
			UNIVERSAL_COOLDOWN_MAP.remove(player.getUUID());
			return false;
		}
		return true;
	}

	public long getRemainingCooldownTicks(Player player) {
		Long expiryTick = UNIVERSAL_COOLDOWN_MAP.get(player.getUUID());
		if (expiryTick == null) {
			return 0;
		}
		long remaining = expiryTick - player.level().getGameTime();
		return Math.max(0, remaining);
	}

	public static long getUniversalCooldownExpiry(Player player) {
		Long expiryTick = UNIVERSAL_COOLDOWN_MAP.get(player.getUUID());
		return expiryTick != null ? expiryTick : 0;
	}

	private void invokeMnAComboHelper(Player player) {
		try {
			Class<?> helperClass = Class.forName(MNA_MANIP_COMBO_HELPER);
			Method onManipulationUsed = helperClass.getMethod("onManipulationUsed", Player.class);
			onManipulationUsed.invoke(null, player);
		} catch (Exception ignored) {
			// MnA compat classes unavailable or incompatible — skip silently
		}
	}

	private long startCooldown(Player player) {
		if (cooldownTicks > 0) {
			// ── Skill: Blood Flow — reduce cooldown duration ──
			long effectiveCooldown = (long) (cooldownTicks
					* SkillPointHelper.getBloodFlowMultiplier(player));

			// ── ManipLevel — per-use mastery further reduces cooldown ──
			double levelCooldownMultiplier = HemoCapabilityAccess.getKnownManipulations(player)
					.map(k -> k.getManipLevel(this))
					.map(ManipLevel::getCooldownMultiplier)
					.orElse(1.0);
			effectiveCooldown = (long) (effectiveCooldown * levelCooldownMultiplier);
			effectiveCooldown = (long) (effectiveCooldown
					* CheapBloodInfusionHelper.getManipulationCooldownMultiplier(player));
			effectiveCooldown = (long) (effectiveCooldown
					* SporiticThuribleResonanceState.getCooldownMultiplier(player, tend));
			effectiveCooldown = (long) (effectiveCooldown
					* MnemonicPotionRules.manipulationCooldownMultiplier(player.hasEffect(EffectInit.mnemonic_whispers)));
			effectiveCooldown = (long) (effectiveCooldown
					* MnemonicCandleRules.manipulationCooldownMultiplier(
							player.hasEffect(EffectInit.mnemonic_candle_aura)));

			UNIVERSAL_COOLDOWN_MAP.put(player.getUUID(), player.level().getGameTime() + effectiveCooldown);
			return effectiveCooldown;
		}
		return 0L;
	}

	/**
	 * Checks the Unstained purity state and returns the cost multiplier to apply.
	 * Returns -1.0 if the manipulation is completely blocked.
	 * Sends the appropriate chat feedback to the player.
	 */
	private double getPurityCostMultiplier(Player player) {
		var optUnstained = HemoCapabilityAccess.getUnstainedProgress(player);
		if (!optUnstained.isPresent()) {
			return 1.0;
		}
		var unstained = optUnstained.orElseThrow(IllegalStateException::new);
		if (!unstained.hasBegunPurification()) {
			return 1.0;
		}
		EnumPurityStage stage = EnumPurityStage.byPurity(unstained.getPurity());
		if (stage == EnumPurityStage.PURIFIED) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.manipulation_blocked")
							.withStyle(ChatFormatting.GRAY), true);
			return -1.0;
		}
		if (stage != EnumPurityStage.CORRUPTED) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.manipulation_weakened")
							.withStyle(ChatFormatting.GRAY), true);
			return 1.0 + stage.getBloodMagicPenalty();
		}
		return 1.0;
	}

	public void performAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player)
				.orElseThrow(NullPointerException::new);

		if (!player.level().isClientSide) {
			if (!ignoresCooldown(player) && isAnyManipOnCooldown(player)) {
				long remaining = getRemainingCooldownTicks(player);
				double seconds = remaining / TICKS_PER_SECOND;
				player.displayClientMessage(
						Component.literal(String.format("Manipulation on cooldown! (%.1fs)", seconds))
								.withStyle(ChatFormatting.RED),
						true);
				return;
			}

			// Check Unstained purity penalty — must happen before isActive check
			// so that purified players are fully blocked even if volume is still active
			double costMultiplier = getPurityCostMultiplier(player);
			if (costMultiplier < 0) {
				return;
			}

			// Qliphoth Pome Corruption: at 9 pomes manipulations are disabled until Apotheos.
			// for 1–8 pomes the variable is reused below inside volume.isActive() to scale cost.
			var degreeData = HemoCapabilityAccess.getInitiatoryDegree(player);
			int pomesConsumed = degreeData
					.map(d -> d.getTotalPomesConsumed())
					.orElse(0);
			boolean hasCompletedApotheos = degreeData
					.map(d -> d.getDegreeNumber() >= 8)
					.orElse(false);
			if (pomesConsumed >= 9 && !hasCompletedApotheos) {
				player.displayClientMessage(
						Component.literal("Your blood no longer answers to you. It belongs to the void now.")
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
						true);
				return;
			}

			if (volume.isActive()) {
				// Unified manipulation cost ledger covers skill, rites, effects, world, and tool modifiers.
				ManipulationCostSnapshot costSnapshot = ManipulationCostLedger.collect(player, this, costMultiplier);
				double effectiveCost = costSnapshot.effectiveCost();

				boolean hasRequiredAlignment = tendency.getAlignmentByTendency(tend) >= alignLevel;
				if (!hasRequiredAlignment) {
					player.displayClientMessage(Component.translatable("Not Enough Alignment for Manipulation!")
							.withStyle(ChatFormatting.RED), true);
					return;
				}

				// Emergency cover: when a valid cast would fail for lack of blood,
				// the borrowed-blood reserve closes the gap first.
				double deficit = BorrowedBloodRules.castDeficitToCover(hasRequiredAlignment,
						volume.getBloodVolume(), effectiveCost);
				if (deficit > 0.0D && BorrowedBloodReserve.get(player) >= deficit) {
					volume.fill(BorrowedBloodReserve.drainToCover(player, deficit));
				}
				if (volume.getBloodVolume() > effectiveCost) {
					volume.drain(effectiveCost);
					volume.addBloodSpend(effectiveCost);
					HemorathEntity
							.onPlayerBloodSpend(player, effectiveCost);
					RootedStateHelper.refundManipulationCost(player, effectiveCost);
					ConserveStateHelper.markManipulationCast(player);
					PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(volume));
					getAction(player, world, heldItemMainhand, position);

					// Crawling Choir: chance to echo-cast at no additional blood cost
					CrawlingChoirHandler.tryEchoCast(player, world, heldItemMainhand, position, this);

					// Apply cross-system consequences: vascular strain, tendency shift, XP
					KnownManipulationEvents.onManipulationUsed((ServerPlayer) player, this);
					PurityGainEvents.onBloodManipulationUsed((ServerPlayer) player);

					// MnA Combo System: Grant Sanguine Clarity (reduces next spell mana cost)
					// and consume Arcane Resonance if present (it already reduced this manipulation's cost)
					if (ModList.get().isLoaded("mna")) {
						invokeMnAComboHelper(player);
					}

					long appliedCooldown = ignoresCooldown(player) ? 0L : startCooldown(player);
					PacketHandler.sendToPlayer((ServerPlayer) player, new ManipCooldownPacket((int) appliedCooldown));
				} else {
					player.displayClientMessage(
							Component.translatable("Not Enough Blood to be Shed!").withStyle(ChatFormatting.RED), true);
				}
			} else {
				player.displayClientMessage(Component.translatable("You strain your body but nothing happens!")
						.withStyle(ChatFormatting.RED), true);
			}
		}
	}

	/*
	 * Writes a NBT tag from this manipulation
	 */
	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("name", name);
		nbt.putDouble("cost", cost);
		nbt.putDouble("level", alignLevel);
		nbt.putDouble("xpcost", xpCost);
		nbt.putString("type", type.name());
		nbt.putString("rank", rank.name());
		nbt.putString("tendency", tend.name());
		if (secondaryTend != null) {
			nbt.putString("secondary_tendency", secondaryTend.name());
		}
		nbt.putString("section", section.name());
		nbt.putInt("cooldown", cooldownTicks);
		return nbt;
	}

	public void setAlignLevel(float alignLevel) {
		this.alignLevel = alignLevel;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRank(EnumManipulationRank rank) {
		this.rank = rank;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

	public void setTend(EnumBloodTendency tend) {
		this.tend = tend;
		if (secondaryTend == tend) {
			secondaryTend = null;
		}
	}

	public void setType(EnumManipulationType type) {
		this.type = type;
	}

	public void setXpCost(double xpCost) {
		this.xpCost = xpCost;
	}

	@Override
	public String toString() {
		return HLTextUtils.convertInitToLang(name);
	}

	/**
	 * Executes this manipulation on behalf of a Drudge entity rather than a
	 * player. Bypasses all player-specific checks (purity, degree, MnA combos)
	 * and draws from the drudge's internal blood charge instead.
	 *
	 * <p>Cost is multiplied by {@code HemoServerConfig.DRUDGE_ACTION_COST_MULTIPLIER}.
	 * Returns {@code true} if the action was executed, {@code false} if the drudge
	 * had insufficient blood charge or no {@link DrudgeAction} is registered.
	 *
	 * @deprecated Prefer letting {@code DrudgeExecuteMemoryGoal} handle dispatch.
	 *             This helper is provided for external callers that need a one-shot
	 *             invocation without going through the goal scheduler.
	 */
	@Deprecated
	public boolean performDrudgeAction(com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity drudge, Level world, BlockPos position) {
		if (world.isClientSide) return false;
		DrudgeAction action = getDrudgeAction().orElse(null);
		if (action == null || action == DrudgeAction.DRUDGE_UNSUPPORTED) return false;
		double costMult = com.vincenthuto.hemomancy.config.HemoServerConfig.DRUDGE_ACTION_COST_MULTIPLIER.get();
		double effectiveCost = cost * costMult;
		if (drudge.getBloodCharge() < effectiveCost) return false;
		boolean fired = action.execute(drudge, world, position, com.vincenthuto.hemomancy.config.HemoServerConfig.DRUDGE_WORK_RADIUS.get());
		if (fired) {
			drudge.drainBloodCharge((float) effectiveCost);
		}
		return fired;
	}

}
