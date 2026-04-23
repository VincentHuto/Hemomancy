package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.PurityGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.ManipCooldownPacket;
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
import net.neoforged.neoforge.network.PacketDistributor;

public class BloodManipulation  {
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

				return manip;
			}
		}
		return null;
	}
	String name;
	double cost, alignLevel, xpCost;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	EnumManipulationType type;

	private static final double TICKS_PER_SECOND = 20.0;
	private static final String MNA_MANIP_COMBO_HELPER = "com.vincenthuto.hemomancy.compat.mna.spell.ManipComboHelper";

	private static final Map<UUID, Long> UNIVERSAL_COOLDOWN_MAP = new ConcurrentHashMap<>();

	int cooldownTicks;

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

	public boolean isOnCooldown(Player player) {
		if (cooldownTicks <= 0) {
			return false;
		}
		return isAnyManipOnCooldown(player);
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

	private void startCooldown(Player player) {
		if (cooldownTicks > 0) {
			// ── Skill: Blood Flow — reduce cooldown duration ──
			long effectiveCooldown = (long) (cooldownTicks
					* com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper.getBloodFlowMultiplier());

			// ── ManipLevel — per-use mastery further reduces cooldown ──
			double levelCooldownMultiplier = HemoCapabilityAccess.getKnownManipulations(player)
					.map(k -> k.getManipLevel(this))
					.map(ManipLevel::getCooldownMultiplier)
					.orElse(1.0);
			effectiveCooldown = (long) (effectiveCooldown * levelCooldownMultiplier);

			UNIVERSAL_COOLDOWN_MAP.put(player.getUUID(), player.level().getGameTime() + effectiveCooldown);
		}
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
			if (isAnyManipOnCooldown(player)) {
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

			if (volume.isActive()) {
				// Apply Efficiency skill discount to manipulation cost
				double effectiveCost = cost * com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper.getEfficiencyMultiplier() * costMultiplier;

				// ── ManipLevel — per-use mastery reduces cost ──
				double levelCostMultiplier = HemoCapabilityAccess.getKnownManipulations(player)
						.map(k -> k.getManipLevel(this))
						.map(ManipLevel::getCostMultiplier)
						.orElse(1.0);
				effectiveCost *= levelCostMultiplier;

				// ── Skill: Dynamic Use — reduce cost further when this manipulation's
				//    tendency matches the player's strongest tendency ──
				EnumBloodTendency strongest = null;
				float highestVal = 0;
				for (EnumBloodTendency t : EnumBloodTendency.values()) {
					float val = tendency.getAlignmentByTendency(t);
					if (val > highestVal) {
						highestVal = val;
						strongest = t;
					}
				}
				if (strongest != null && strongest.equals(tend)) {
					// Dynamic Use returns e.g. 1.2 at level 2 — invert to
					// get a discount: cost / 1.2 ≈ 17% discount.
					effectiveCost /= com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper.getDynamicUseMultiplier();
				}

				// MnA Combo System: Arcane Resonance reduces blood cost
				if (ModList.get().isLoaded("mna")
						&& player.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.arcane_resonance)) {
					try {
						double reduction = com.vincenthuto.hemomancy.config.HemoMnAConfig.ARCANE_RESONANCE_BLOOD_REDUCTION.get();
						effectiveCost *= (1.0 - reduction);
					} catch (Exception ignored) {}
				}

				// Qliphoth Bloom: 25% cost reduction when within a bloom's radius
				if (player instanceof ServerPlayer serverPlayer
						&& com.vincenthuto.hemomancy.common.rite.QliphothBloomEvents.isInQliphothBloom(serverPlayer)) {
					effectiveCost *= 0.75;
				}

				// Qliphoth Pome Empowerment: 25% cost reduction for 3 minutes after eating a Pome
				long pomeExpiry = player.getPersistentData()
						.getLong(com.vincenthuto.hemomancy.common.item.QliphothPomeItem.POME_EMPOWERMENT_KEY);
				if (player.level().getGameTime() < pomeExpiry) {
					effectiveCost *= 0.75;
				}

				// Blood Moon: 25% cost reduction while the blood moon is active
				if (com.vincenthuto.hemomancy.common.worldevent.BloodMoonEvents.isBloodMoonActive(world)) {
					effectiveCost *= 0.75;
				}

				if (volume.getBloodVolume() > effectiveCost) {
					if (tendency.getAlignmentByTendency(tend) >= alignLevel) {
						volume.drain(effectiveCost);
						volume.addBloodSpend(effectiveCost);
						com.vincenthuto.hemomancy.common.entity.boss.HollowVesselEntity
								.onPlayerBloodSpend(player, effectiveCost);
						PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(volume));
						getAction(player, world, heldItemMainhand, position);

						// Apply cross-system consequences: vascular strain, tendency shift, XP
						KnownManipulationEvents.onManipulationUsed((ServerPlayer) player, this);
						PurityGainEvents.onBloodManipulationUsed((ServerPlayer) player);

						// MnA Combo System: Grant Sanguine Clarity (reduces next spell mana cost)
						// and consume Arcane Resonance if present (it already reduced this manipulation's cost)
						if (ModList.get().isLoaded("mna")) {
							invokeMnAComboHelper(player);
						}

						startCooldown(player);
						PacketHandler.sendToPlayer((ServerPlayer) player, new ManipCooldownPacket(cooldownTicks));
					} else {
						player.displayClientMessage(Component.translatable("Not Enough Alignment for Manipulation!")
								.withStyle(ChatFormatting.RED), true);
					}
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

}
