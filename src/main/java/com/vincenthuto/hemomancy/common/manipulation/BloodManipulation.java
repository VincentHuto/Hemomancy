package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationUpkeep;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.CrawlingChoirHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.IBloodTemperament;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.PurityGainEvents;
import com.vincenthuto.hemomancy.common.effect.MnemonicCandleRules;
import com.vincenthuto.hemomancy.common.effect.MnemonicPotionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.event.BorrowedBloodRules;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.CheapBloodInfusionHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleResonanceState;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipCooldownPacket;
import com.vincenthuto.hemomancy.common.util.HemoText;
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

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.function.Supplier;

public class BloodManipulation implements EntityCastableManipulation {
	private static final BloodAccounting.Source CAST = BloodAccounting.Source.of("manipulation_cast", Category.CAST, false);
	/** A Sculpting pulse: the selected manipulation working through Projection or Absorption. */
	public static final BloodAccounting.Source SCULPTING = BloodAccounting.Source.of("sculpting", Category.CAST, false);
    public com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation getCastPresentation() {
        return com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation.forId(getName());
    }
	public static BloodManipulation BLANK = new BloodManipulation("No Selected", 0, 0, 0, EnumManipulationType.QUICK,
			EnumManipulationRank.HUMILIS, EnumBloodTemper.ANIMUS, EnumVeinSections.HEAD);
	/**
	 * Reads a NBT tag and converts it to a manipulation. Returns null for a tag this build cannot read: a missing key
	 * (saves from before the Temper rename) or an enum name that no longer exists.
	 */
	@Nullable
	public static BloodManipulation deserialize(CompoundTag nbt) {
		if (nbt == null || nbt.isEmpty() || !(nbt.contains("name") && nbt.contains("cost") && nbt.contains("level")
				&& nbt.contains("type") && nbt.contains("temper") && nbt.contains("rank") && nbt.contains("section"))) {
			return null;
		}
		try {
			BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
					nbt.getDouble("level"), nbt.getDouble("xpcost"),
					EnumManipulationType.valueOf(nbt.getString("type")),
					EnumManipulationRank.valueOf(nbt.getString("rank")),
					EnumBloodTemper.valueOf(nbt.getString("temper")),
					EnumVeinSections.valueOf(nbt.getString("section")));
			if (nbt.contains("cooldown")) {
				manip.cooldownTicks = nbt.getInt("cooldown");
			}
			if (nbt.contains("secondary_temper")) {
				manip.secondaryTend = EnumBloodTemper.valueOf(nbt.getString("secondary_temper"));
			}
			return manip;
		} catch (IllegalArgumentException unknownEnumName) {
			return null;
		}
	}
	String name;
	double cost, alignLevel, xpCost;
	EnumBloodTemper tend;
	@Nullable
	EnumBloodTemper secondaryTend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	EnumManipulationType type;

	private static final double TICKS_PER_SECOND = 20.0;

	private static final ManipulationCooldownState COOLDOWNS = new ManipulationCooldownState();

	private static final ThreadLocal<Double> CAST_PAID = new ThreadLocal<>();

	/**
	 * The blood the player paid for the cast whose {@code getAction} is running now. Zero for every action nobody paid
	 * for: an echo, a direct {@code getAction}, a Drudge or an entity cast. Refunds are a share of this.
	 */
	public static double paidForCurrentCast() {
		Double paid = CAST_PAID.get();
		return paid == null ? 0.0D : paid;
	}

	public static void clearSessionState() {
		COOLDOWNS.clear();
		ManipulationChannelManager.clearSessionState();
		ManipulationReactiveEvents.clearSessionState();
		com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteEvents.clearSessionState();
	}

	int cooldownTicks;
	int requiredDegree;

	/**
	 * Transient — set at registration time via {@link #setDrudgeAction(DrudgeAction, Component)}.
	 * Not serialized to NBT; must be resolved to the registry instance after loading.
	 */
	private transient DrudgeAction drudgeAction = null;
	private transient Component drudgeDescription = null;
	/** What this does when an entity casts it, set at registration; transient like the Drudge action. */
	private transient EntityCast entityCast = null;
	/** What this makes of Projection and of Absorption while selected, set at registration; see {@link VerbSculpting}. */
	private transient VerbSculpting projectionSculpting = null;
	private transient VerbSculpting absorptionSculpting = null;
	/** Narrow Sculptings tried ahead of the main one, in the order attached (core-loop round two P4). */
	private final transient java.util.List<VerbSculpting> narrowProjection = new java.util.ArrayList<>();
	private final transient java.util.List<VerbSculpting> narrowAbsorption = new java.util.ArrayList<>();

	public BloodManipulation(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTemper temper, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.type = type;
		this.tend = temper;
		this.rank = rank;
		this.section = section;
		this.cooldownTicks = 0;
	}

	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {

	}

	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		getAction(player, world, heldItemMainhand, position);
	}

	/**
	 * Whether a cast or Sculpting pulse of this manipulation burns Temper in its school (core loop Track 4). False for
	 * the hands and forms that only conjure a tool: the verbs' cell hands, the Staff and its weapon forms.
	 */
	public boolean chargesTemper() {
		return true;
	}

	public boolean usesDefaultActivationParticles() {
		return true;
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

	public Component getBaseCostLabel() {
		if (com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules.isStaffWeaponFormManip(name)) return Component.translatable("manipulation.hemomancy.base_cost.switch");
		if (type == EnumManipulationType.CONTINUOUS || name.startsWith("summon_avatar")) return Component.translatable("manipulation.hemomancy.base_cost.upkeep");
		return Component.translatable(type == EnumManipulationType.PASSIVE ? "manipulation.hemomancy.base_cost.trigger" : "manipulation.hemomancy.base_cost.cast");
	}

	public Component getBaseCostText() {
		if (com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules.isStaffWeaponFormManip(name)) {
			return Component.translatable("manipulation.hemomancy.base_cost.ml",
					(int) com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules.BASE_HOT_SWAP_COST_ML);
		}
		return Component.translatable(type == EnumManipulationType.CONTINUOUS || name.startsWith("summon_avatar")
						? "manipulation.hemomancy.base_cost.ml_per_second" : "manipulation.hemomancy.base_cost.ml",
				java.math.BigDecimal.valueOf(cost).stripTrailingZeros().toPlainString());
	}

	/** "Base cast: 100 mL" and its siblings. */
	public net.minecraft.network.chat.MutableComponent getBaseCostLine() {
		return Component.translatable("manipulation.hemomancy.base_cost.line", getBaseCostLabel(), getBaseCostText());
	}

	public String getName() {
		return name;
	}

	/** The ID-derived name; the fallback for {@link #getDisplayName()} and for logs/save data. */
	public String getProperName() {
		return HLTextUtils.convertInitToLang(name);
	}

	public static String displayNameKey(String manipulationName) {
		return "hemomancy.manipulation." + manipulationName;
	}

	/**
	 * Player-facing name: the lang entry when the reader's language has one, otherwise {@link #getProperName()}.
	 * Safe on both sides; a server-built message resolves on the receiving client.
	 */
	public net.minecraft.network.chat.MutableComponent getDisplayName() {
		return displayName(name);
	}

	public static net.minecraft.network.chat.MutableComponent displayName(String manipulationName) {
		return Component.translatableWithFallback(displayNameKey(manipulationName),
				HLTextUtils.convertInitToLang(manipulationName));
	}

	public EnumManipulationRank getRank() {
		return rank;
	}

	public Component getTypeName() {
		return Component.translatable("manip.type.hemomancy." + type.name().toLowerCase(java.util.Locale.ROOT));
	}

	public Component getRankName() {
		return Component.translatable("manipulation_rank.hemomancy." + rank.name().toLowerCase(java.util.Locale.ROOT));
	}

	public static Component temperName(EnumBloodTemper temper) {
		return Component.translatable("blood_temper.hemomancy." + temper.name().toLowerCase(java.util.Locale.ROOT));
	}

	public static Component sectionName(EnumVeinSections section) {
		return Component.translatable("vein_section.hemomancy." + section.name().toLowerCase(java.util.Locale.ROOT));
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public EnumBloodTemper getTend() {
		return tend;
	}

	@Nullable
	public EnumBloodTemper getSecondaryTend() {
		return secondaryTend;
	}

	public EnumManipulationType getType() {
		return type;
	}

	public int getCooldownTicks() {
		return cooldownTicks;
	}

	public int getRequiredChargeTicks() {
		return type == EnumManipulationType.CHARGED ? 20 : 0;
	}

	public BloodManipulation setCooldownTicks(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
		return this;
	}

	/** The lowest degree this is learned at, over its rank's gate; read through {@code ManipulationRankGates}. */
	public int getRequiredDegree() {
		return requiredDegree;
	}

	public BloodManipulation setRequiredDegree(int requiredDegree) {
		this.requiredDegree = requiredDegree;
		return this;
	}

	public BloodManipulation setSecondaryTend(@Nullable EnumBloodTemper secondaryTend) {
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
	 *                    item tooltip
	 * @return {@code this} for fluent chaining
	 */
	public BloodManipulation setDrudgeAction(DrudgeAction action, Component description) {
		this.drudgeAction = action == null || action == DrudgeAction.DRUDGE_UNSUPPORTED ? action : (caster, world, position, radius) -> {
            if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(caster)) return false;
            try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, caster, 1)) {
                return action.execute(caster, world, position, radius);
            }
        };
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

	/** Attaches what this manipulation does when an entity (a Will, Vesper) casts it. */
	public BloodManipulation setEntityCast(EntityCast cast) {
		this.entityCast = cast;
		return this;
	}

	public Optional<EntityCast> getEntityCast() {
		return Optional.ofNullable(entityCast);
	}

	/** Attaches what this manipulation makes of Projection while it is selected. */
	public BloodManipulation setProjectionSculpting(Supplier<? extends VerbSculpting> factory) {
		this.projectionSculpting = factory == null ? null : factory.get();
		return this;
	}

	/** Attaches what this manipulation makes of Absorption while it is selected. */
	public BloodManipulation setAbsorptionSculpting(Supplier<? extends VerbSculpting> factory) {
		this.absorptionSculpting = factory == null ? null : factory.get();
		return this;
	}

	/**
	 * Attaches a narrow Sculpting of Projection, tried ahead of the main one ({@link #setProjectionSculpting}) in the order
	 * attached: it takes a few targets the main one would otherwise take (Glacial Grasp freezes water ahead of its
	 * plant strand).
	 */
	public BloodManipulation addProjectionSculpting(Supplier<? extends VerbSculpting> factory) {
		if (factory != null) narrowProjection.add(factory.get());
		return this;
	}

	/** Attaches a narrow Sculpting of Absorption, tried ahead of the main one in the order attached. */
	public BloodManipulation addAbsorptionSculpting(Supplier<? extends VerbSculpting> factory) {
		if (factory != null) narrowAbsorption.add(factory.get());
		return this;
	}

	/** This manipulation's main Sculpting of {@code verb}; read through {@code SculptingDispatcher} only. */
	public Optional<VerbSculpting> getSculpting(Verb verb) {
		return Optional.ofNullable(verb == Verb.ABSORB ? absorptionSculpting : projectionSculpting);
	}

	/**
	 * Every Sculpting of {@code verb} in the order they claim: the narrow ones first, then the main one. Read through
	 * {@code SculptingDispatcher} only.
	 */
	public java.util.List<VerbSculpting> getSculptings(Verb verb) {
		return com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules.claimOrder(
				verb == Verb.ABSORB ? narrowAbsorption : narrowProjection,
				verb == Verb.ABSORB ? absorptionSculpting : projectionSculpting);
	}

	/**
	 * Returns the short drudge-behavior description, or empty if none was
	 * set.
	 */
	public Optional<Component> getDrudgeDescription() {
		return Optional.ofNullable(drudgeDescription);
	}

	public boolean isOnCooldown(Player player) {
		// No cooldownTicks short-circuit: imposed cooldowns (e.g. a Synaptic pattern swap) bind zero-cooldown powers too.
		if (ignoresCooldown(player)) {
			return false;
		}
		return COOLDOWNS.isOnCooldown(player.getUUID(), name, player.level().getGameTime());
	}

	/** Caps an already-running cooldown (for example after a successful parry) and resyncs the client. */
	public final void capCooldown(ServerPlayer player, long maxRemainingTicks) {
		long left = COOLDOWNS.capRemaining(player.getUUID(), name, player.level().getGameTime(), maxRemainingTicks);
		PacketHandler.sendToPlayer(player, new ManipCooldownPacket(name, (int) left));
	}

	/** Starts this manipulation's own cooldown again from now, with the usual reductions, and resyncs the client. */
	public final void restartCooldown(ServerPlayer player) {
		if (ignoresCooldown(player)) return;
		PacketHandler.sendToPlayer(player, new ManipCooldownPacket(name, (int) startCooldown(player)));
	}

	/** Holds this manipulation on cooldown for at least {@code minTicks} (never shortening one) and resyncs the client. */
	public final void imposeCooldown(ServerPlayer player, long minTicks) {
		if (ignoresCooldown(player)) return;
		long left = COOLDOWNS.extendTo(player.getUUID(), name, player.level().getGameTime(), minTicks);
		PacketHandler.sendToPlayer(player, new ManipCooldownPacket(name, (int) left));
	}

	public boolean ignoresCooldown(Player player) {
		return false;
	}

	public long getRemainingCooldownTicks(Player player) {
		return COOLDOWNS.remainingTicks(player.getUUID(), name, player.level().getGameTime());
	}


	private long startCooldown(Player player) {
		if (cooldownTicks > 0) {
			// ── Skill: Blood Flow — reduce cooldown duration ──
			long effectiveCooldown = (long) (cooldownTicks
					* SkillPointHelper.getBloodFlowMultiplier(player));

			// ── ManipLevel — per-use mastery further reduces cooldown ──
			ManipLevel manipLevel = HemoCapabilityAccess.getKnownManipulations(player).getManipLevel(this);
			double levelCooldownMultiplier = manipLevel != null ? manipLevel.getCooldownMultiplier() : 1.0;
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

			COOLDOWNS.start(player.getUUID(), name, player.level().getGameTime(), effectiveCooldown);
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
		var unstained = HemoCapabilityAccess.getUnstainedProgress(player);
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
		performAction(player, world, heldItemMainhand, position, 0.0F);
	}

	public void performAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		tryPerformAction(player, world, heldItemMainhand, position, chargeTicks);
	}

	public boolean tryPerformAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		return tryPerformAction(player, world, heldItemMainhand, position, chargeTicks, true);
	}

	public boolean tryPerformContinuousPulse(Player player, Level world, ItemStack heldItemMainhand,
			BlockPos position) {
		return type == EnumManipulationType.CONTINUOUS
				&& tryPerformAction(player, world, heldItemMainhand, position, 0.0F, false, false, false);
	}

	public boolean tryPerformSustainedPassivePulse(ServerPlayer player) {
		return type == EnumManipulationType.PASSIVE
				&& tryPerformAction(player, player.level(), ItemStack.EMPTY, player.blockPosition(), 0.0F,
						false, false, false);
	}

	/**
	 * The equipped-cast gate shared by the charge visual and the casting animation: this manipulation is equipped and
	 * absorbed, the player's blood is active, Unstained progress does not lock blood powers, and the player is aligned
	 * enough with its temper. Callers add their own selection, paralysis, cooldown and life checks.
	 */
	public boolean isEquippedCastReady(ServerPlayer player, IKnownManipulations known) {
		return known.isManipEquipped(this) && known.isManipulationAvailable(this)
				&& HemoCapabilityAccess.getBloodVolume(player).isActive()
				&& !com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player))
				&& (HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(tend) >= alignLevel);
	}

	public boolean isPassiveReady(ServerPlayer player) {
		return type == EnumManipulationType.PASSIVE && player.isAlive()
				&& HemoCapabilityAccess.getBloodVolume(player).isActive()
				&& HemoCapabilityAccess.getKnownManipulations(player).isPassiveActive(name)
				&& (HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(tend) >= alignLevel)
				&& !com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player));
	}

	public boolean tryPerformPassiveTrigger(ServerPlayer player) {
		return isPassiveReady(player)
				&& tryPerformAction(player, player.level(), ItemStack.EMPTY, player.blockPosition(), 0.0F,
						false, false);
	}

	public boolean canContinueChannel(Player player, Level world) {
		return true;
	}

	public void tickContinuousAction(Player player, Level world) {
	}

	public void clearContinuousSession(UUID playerId) {
	}

	public void finishContinuousAction(Player player) {
		if (type != EnumManipulationType.CONTINUOUS || ignoresCooldown(player)) return;
		long appliedCooldown = startCooldown(player);
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new ManipCooldownPacket(name, (int) appliedCooldown));
		}
	}

	public void finishContinuousAction(Player player, boolean released) {
		finishContinuousAction(player);
	}

	private boolean tryPerformAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks, boolean applyCooldown) {
		return tryPerformAction(player, world, heldItemMainhand, position, chargeTicks, applyCooldown, true);
	}

	private boolean tryPerformAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks, boolean applyCooldown, boolean enforceCooldown) {
		return tryPerformAction(player, world, heldItemMainhand, position, chargeTicks, applyCooldown,
				enforceCooldown, true);
	}

	private boolean tryPerformAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks, boolean applyCooldown, boolean enforceCooldown, boolean creditUse) {
		if (type != EnumManipulationType.PASSIVE && com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(player)) return false;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				;
		IBloodTemperament temper = HemoCapabilityAccess.getBloodTemperament(player)
				;

		if (!player.level().isClientSide) {
			if (!canPerformAction(player, heldItemMainhand, chargeTicks)) return false;
			if (!passesFormGate(player)) return false;
			if (enforceCooldown && isOnCooldown(player)) {
				long remaining = getRemainingCooldownTicks(player);
				double seconds = remaining / TICKS_PER_SECOND;
				player.displayClientMessage(
						HemoText.translatable("message.hemomancy.blood_manipulation.manipulation_on_cooldown_s", String.format("%.1f", seconds)).withStyle(ChatFormatting.RED),
						true);
				return false;
			}
			double costMultiplier = passBodyGates(player);
			if (costMultiplier < 0) {
				return false;
			}

			if (volume.isActive()) {
				// Temper: worked out before pricing (a dry cast costs more), settled only once the blood is paid.
				TemperCharge.Charge temperCharge = TemperCharge.forCast(player, this,
						ManipulationCastingRules.rewardScale(type, chargeTicks, getRequiredChargeTicks()), !creditUse);
				// Unified manipulation cost ledger covers skill, rites, effects, world, tool and Temper modifiers.
				ManipulationCostSnapshot costSnapshot = ManipulationCostLedger.collect(player, this, costMultiplier,
						temperCharge);
				double effectiveCost = costSnapshot.effectiveCost();
				if (type == EnumManipulationType.CHARGED) {
					var charged = ManipulationCastingRules.chargedCost(effectiveCost, chargeTicks,
							getRequiredChargeTicks());
					if (charged.isEmpty()) {
						player.displayClientMessage(Component.translatable("message.hemomancy.blood_manipulation.not_enough_charge").withStyle(ChatFormatting.RED), true);
						return false;
					}
					effectiveCost = charged.getAsDouble();
				}

				boolean hasRequiredAlignment = temper.getAlignmentByTemper(tend) >= alignLevel;
				if (!hasRequiredAlignment) {
					player.displayClientMessage(HemoText.translatable("message.hemomancy.blood_manipulation.requires_alignment_current",
							(int) alignLevel,
							tend.name(),
							(int) temper.getAlignmentByTemper(tend)).withStyle(ChatFormatting.RED), true);
					return false;
				}

				// Emergency cover: when a valid cast would fail for lack of blood,
				// the borrowed-blood reserve closes the gap first.
				double deficit = BorrowedBloodRules.castDeficitToCover(hasRequiredAlignment,
						volume.getBloodVolume(), effectiveCost);
				if (deficit > 0.0D && BorrowedBloodReserve.get(player) >= deficit) {
					volume.fill(BorrowedBloodReserve.drainToCover(player, deficit));
				}
				if (volume.getBloodVolume() >= effectiveCost) {
					BloodAccounting.pay(player, effectiveCost, CAST);
					TemperCharge.commit((ServerPlayer) player, temperCharge);
					HemorathEntity
							.onPlayerBloodSpend(player, effectiveCost);
					RootedStateHelper.refundManipulationCost(player, effectiveCost);
					ConserveStateHelper.markManipulationCast(player);
					Double outerPaid = CAST_PAID.get();
					CAST_PAID.set(effectiveCost);
					try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player,
                            getRequiredChargeTicks() <= 0 ? 1 : chargeTicks / getRequiredChargeTicks(),
							temperCharge == null ? 1.0F : temperCharge.strength())) {
					    getAction(player, world, heldItemMainhand, position, chargeTicks);
                    } finally {
						CAST_PAID.set(outerPaid);
					}
					if (creditUse && type != EnumManipulationType.CONTINUOUS) {
						if (type != EnumManipulationType.PASSIVE) {
                            com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager.release((ServerPlayer) player, this);
                            com.vincenthuto.hemomancy.common.manipulation.outpouring.OutpouringWorldEffects.release((ServerPlayer) player, this);
                        }
						ManipulationCastSounds.play(world, player, this);
						ManipulationParticles.activate(player, this);
					}

					// Crawling Choir: chance to echo-cast at no additional blood cost
					CrawlingChoirHandler.tryEchoCast(player, world, heldItemMainhand, position, this, chargeTicks);

					// Apply cross-system consequences: vascular strain, temper shift, XP
					if (creditUse) recordSuccessfulUse((ServerPlayer) player,
							ManipulationCastingRules.rewardScale(type, chargeTicks, getRequiredChargeTicks()));
					if (type == EnumManipulationType.CHARGED) ManipulationChargeClock.release(player);

					// MnA Combo System: Grant Sanguine Clarity (reduces next spell mana cost)
					// and consume Arcane Resonance if present (it already reduced this manipulation's cost)
					long appliedCooldown = !applyCooldown || ignoresCooldown(player) ? 0L : startCooldown(player);
					PacketHandler.sendToPlayer((ServerPlayer) player, new ManipCooldownPacket(name, (int) appliedCooldown));
					return true;
				} else {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.blood_manipulation.not_enough_blood_to_be_shed").withStyle(ChatFormatting.RED), true);
				}
			} else {
				player.displayClientMessage(Component.translatable("message.hemomancy.blood_manipulation.you_strain_your_body_but_nothing_happens")
						.withStyle(ChatFormatting.RED), true);
			}
		}
		return false;
	}

	/** A family form must have been absorbed before it can be cast or sculpt a verb. Shows the refusal. */
	private boolean passesFormGate(Player player) {
		if (ManipulationFamilyRegistry.form(getName()).isPresent()
				&& (!HemoCapabilityAccess.getKnownManipulations(player).isManipulationAvailable(this))) {
			player.displayClientMessage(Component.translatable("message.hemomancy.blood_manipulation.you_have_not_absorbed_this_form").withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		return true;
	}

	/**
	 * The body's gates every cast and Sculpting pulse shares, in order: a dead vein section, Unstained purity, and the
	 * ninth-pome power lock. Shows the refusal. Returns the purity cost multiplier, or -1 when refused.
	 */
	private double passBodyGates(Player player) {
		if (com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents
				.isSectionDead(player, section)) {
			player.displayClientMessage(HemoText.translatable("message.hemomancy.blood_manipulation.your_veins_are_dead_nothing_flows",
					section.name().toLowerCase(java.util.Locale.ROOT)).withStyle(ChatFormatting.RED), true);
			return -1.0D;
		}

		if (tend == EnumBloodTemper.LUX && com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeManip.inLapse(player)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.unclosing_eye.lapse_no_lux")
					.withStyle(ChatFormatting.YELLOW), true);
			return -1.0D;
		}

		if (com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip.bowing(player)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.endless_hour.bowing")
					.withStyle(ChatFormatting.DARK_AQUA), true);
			return -1.0D;
		}

		// Check Unstained purity penalty — must happen before isActive check
		// so that purified players are fully blocked even if volume is still active
		double costMultiplier = getPurityCostMultiplier(player);
		if (costMultiplier < 0) {
			return -1.0D;
		}

		// Unresolved ninth-pome choices restrict powers, but retain Staff recovery.
		var degreeData = HemoCapabilityAccess.getInitiatoryDegree(player);
		boolean permitted = HarbingerPathPermissions.canUsePower(
				degreeData.getTotalPomesConsumed(), degreeData.getArchonPath(), "conjure_staff".equals(getName()));
		if (!permitted) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.blood_manipulation.your_blood_no_longer_answers_to").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					true);
			return -1.0D;
		}
		return costMultiplier;
	}

	/**
	 * One pulse of this manipulation's {@code sculpting}: the front half of {@link #tryPerformAction} (paralysis, an
	 * absorbed family form, a dead vein section, purity, the power lock, an active volume, the temper alignment gate,
	 * the cost ledger), then payment through {@link BloodAccounting#payUpkeep} as {@link #SCULPTING}, then the pulse
	 * itself under this manipulation's school. A pulse starts no cooldown and sends no cooldown packet. Every
	 * {@code creditedUses} counts as one ordinary use (mastery, temper shift, vascular strain, skill points); the
	 * dispatcher hands out one per {@link com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules#TICKS_PER_CREDITED_USE}
	 * ticks of paid work.
	 *
	 * The same possession and Unstained refusals as the cast key ({@code UseManipKeyPacket}) come first, so a held verb
	 * is never a way round them. {@code onPaid} runs once the blood is paid and before the pulse (the dispatcher opens
	 * the session there, so a Sculpting's {@code begin} always precedes its first pulse).
	 *
	 * @return the blood paid, or empty when a gate refused (and said why)
	 */
	public OptionalDouble tryPerformVerbPulse(ServerPlayer player, VerbSculpting sculpting, VerbTarget target,
			SculptingPulse pulse, int creditedUses, Runnable onPaid) {
		if (com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionWheelRules
				.refusesManipulation(HemoCapabilityAccess.isPossessing(player), false)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.possession.cannot_cast")
					.withStyle(ChatFormatting.DARK_GRAY), true);
			return OptionalDouble.empty();
		}
		if (com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules
				.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player))) return OptionalDouble.empty();
		if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(player)) return OptionalDouble.empty();
		if (!passesFormGate(player)) return OptionalDouble.empty();
		double costMultiplier = passBodyGates(player);
		if (costMultiplier < 0) return OptionalDouble.empty();
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.blood_manipulation.you_strain_your_body_but_nothing_happens")
					.withStyle(ChatFormatting.RED), true);
			return OptionalDouble.empty();
		}
		IBloodTemperament temper = HemoCapabilityAccess.getBloodTemperament(player);
		if (temper.getAlignmentByTemper(tend) < alignLevel) {
			player.displayClientMessage(HemoText.translatable("message.hemomancy.blood_manipulation.requires_alignment_current",
					(int) alignLevel, tend.name(), (int) temper.getAlignmentByTemper(tend)).withStyle(ChatFormatting.RED), true);
			return OptionalDouble.empty();
		}
		// The Sculpting's own refusal comes before any price, so a refused pulse costs nothing.
		if (!sculpting.admits(player, target)) return OptionalDouble.empty();
		double basePrice = sculpting.bloodPerPulse(player, target);
		TemperCharge.Charge temperCharge = TemperCharge.forPulse(player, this, sculpting.cadence(), basePrice);
		double price = basePrice
				* ManipulationCostLedger.collect(player, this, costMultiplier, temperCharge).totalMultiplier();
		double paid = 0.0D;
		if (price > 0.0D) {
			BloodAccounting.Payment payment = BloodAccounting.payUpkeep(player, price, SCULPTING, sculpting.cadence(), true);
			if (!payment.satisfied()) {
				player.displayClientMessage(Component.translatable("message.hemomancy.blood_manipulation.not_enough_blood_to_be_shed")
						.withStyle(ChatFormatting.RED), true);
				return OptionalDouble.empty();
			}
			paid = payment.paid();
			HemorathEntity.onPlayerBloodSpend(player, paid);
		}
		TemperCharge.commit(player, temperCharge);
		float strength = temperCharge == null ? 1.0F : temperCharge.strength();
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_SCULPTED);
		if (onPaid != null) onPaid.run();
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1, strength)) {
			sculpting.pulse(player, target, new SculptingPulse(pulse.verb(), paid, pulse.workScale() * strength, pulse.index()));
		}
		for (int use = 0; use < creditedUses; use++) recordSuccessfulUse(player);
		return OptionalDouble.of(paid);
	}

	public void recordSuccessfulUse(ServerPlayer player) {
		recordSuccessfulUse(player, 1.0F);
	}

	/** {@code rewardScale} is the charge fraction for CHARGED casts and 1 otherwise. */
	public void recordSuccessfulUse(ServerPlayer player, float rewardScale) {
		KnownManipulationEvents.onManipulationUsed(player, this, rewardScale);
		PurityGainEvents.onBloodManipulationUsed(player);
	}

	protected boolean canPerformAction(Player player, float chargeTicks) {
		return type != EnumManipulationType.CHARGED || chargeTicks > 0.0F;
	}

	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		return canPerformAction(player, chargeTicks);
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
		nbt.putString("temper", tend.name());
		if (secondaryTend != null) {
			nbt.putString("secondary_temper", secondaryTend.name());
		}
		nbt.putString("section", section.name());
		nbt.putInt("cooldown", cooldownTicks);
		return nbt;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

	public void setTend(EnumBloodTemper tend) {
		this.tend = tend;
		if (secondaryTend == tend) {
			secondaryTend = null;
		}
	}

	/**
	 * Saved copies keep the metadata they were learned with. Adopts the live registry entry's school, rank, prices
	 * and section so rebalanced or re-homed manipulations display and save their current values.
	 *
	 * @return whether anything changed
	 */
	public boolean adoptRegisteredMetadata(BloodManipulation registered) {
		if (registered == null || registered == this || !name.equals(registered.name)) return false;
		boolean changed = tend != registered.tend || secondaryTend != registered.secondaryTend
				|| rank != registered.rank || type != registered.type || section != registered.section
				|| cost != registered.cost || alignLevel != registered.alignLevel || xpCost != registered.xpCost
				|| cooldownTicks != registered.cooldownTicks || requiredDegree != registered.requiredDegree;
		tend = registered.tend;
		secondaryTend = registered.secondaryTend;
		rank = registered.rank;
		type = registered.type;
		section = registered.section;
		cost = registered.cost;
		alignLevel = registered.alignLevel;
		xpCost = registered.xpCost;
		cooldownTicks = registered.cooldownTicks;
		requiredDegree = registered.requiredDegree;
		return changed;
	}

	@Override
	public String toString() {
		return HLTextUtils.convertInitToLang(name);
	}

}
