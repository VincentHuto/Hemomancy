package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Comparator;

public class MorphlingItem extends Item implements IMorphling {

	public int bloodCost;
	public static final String PRIMALIZED_KEY = "Primalized";
	public static final String WILD_BOUND_KEY = "WildBound";
	public static final String PRIMAL_ABILITY_PREFIX = "Primal:";
	public static final String LAST_FED_GAME_TIME_KEY = "LastFedGameTime";
	public static final String EXPERIENCE_PROGRESS_KEY = "ExperienceProgress";

	// Maturity thresholds based on effective EnzymePower
	public static final float[] MATURITY_THRESHOLDS = { 0f, 10f, 30f, 60f, 100f, 100f };
	public static final String[] MATURITY_NAMES = { "Unfed", "Fledgling", "Developing", "Mature", "Apex", "Primal" };
	public static final ChatFormatting[] MATURITY_COLORS = {
			ChatFormatting.GRAY, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN,
			ChatFormatting.GOLD, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE
	};

	public MorphlingItem(Properties prop) {
		super(prop);
		prop.stacksTo(1);
		bloodCost= 0;
	}
	public MorphlingItem(Properties prop, int bloodCostIn) {
		super(prop);
		prop.stacksTo(1);
		bloodCost= bloodCostIn;
	}

	@Override
	public int getBloodCost() {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
	}

	/**
	 * Returns the maturity level (0-4) based on the morphling's accumulated
	 * EnzymePower. Higher maturity enhances the morphling's granted effect.
	 */
	public static int getMaturityLevel(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) return 0;
		CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
		float power = tag.getFloat("EnzymePower");
		if (PrimalMorphlingRules.isPrimalMaturity(power, tag.getBoolean(PRIMALIZED_KEY))) {
			return PrimalMorphlingRules.PRIMAL_LEVEL;
		}
		int maturity = 0;
		for (int i = PrimalMorphlingRules.APEX_LEVEL; i > 0; i--) {
			if (power >= MATURITY_THRESHOLDS[i]) {
				maturity = i;
				break;
			}
		}
		maturity = MorphlingHungerRules.capMaturityByHusbandry(maturity,
				tag.getInt(EXPERIENCE_PROGRESS_KEY), husbandryStageQuota());
		return WildMorphlingRules.applyMaturityCap(maturity, tag.getBoolean(WILD_BOUND_KEY),
				tag.getBoolean(PRIMALIZED_KEY));
	}

	public static boolean isPrimal(ItemStack stack) {
		return getMaturityLevel(stack) >= PrimalMorphlingRules.PRIMAL_LEVEL;
	}

	public static void setPrimalized(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(PRIMALIZED_KEY, true);
		tag.remove(WILD_BOUND_KEY);
		float power = Math.max(tag.getFloat("EnzymePower"), MATURITY_THRESHOLDS[PrimalMorphlingRules.APEX_LEVEL]);
		tag.putFloat("EnzymePower", power);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean isWildBound(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) {
			return false;
		}
		return stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(WILD_BOUND_KEY);
	}

	public static void setWildBound(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(WILD_BOUND_KEY, true);
		tag.putFloat("EnzymePower", Math.max(tag.getFloat("EnzymePower"),
				MATURITY_THRESHOLDS[WildMorphlingRules.WILD_BOUND_MAX_MATURITY]));
		tag.putInt("EnzymeFeedings", Math.max(tag.getInt("EnzymeFeedings"), 1));
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clearWildBound(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) {
			return;
		}
		CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
		tag.remove(WILD_BOUND_KEY);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void markEquipped(Player player, ItemStack stack) {
		if (player == null || stack.isEmpty() || !isHungerEnabled()) {
			return;
		}
		if (getMaturityLevel(stack) >= 3 && !isWildBound(stack) && getLastFedGameTime(stack) <= 0L) {
			writeLong(stack, LAST_FED_GAME_TIME_KEY, 0L);
		}
	}

	public static void markFedNow(ItemStack stack, long gameTime) {
		if (stack.isEmpty()) {
			return;
		}
		writeLong(stack, LAST_FED_GAME_TIME_KEY, Math.max(0L, gameTime));
	}

	public static long getLastFedGameTime(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) {
			return 0L;
		}
		return stack.get(DataComponents.CUSTOM_DATA).copyTag().getLong(LAST_FED_GAME_TIME_KEY);
	}

	public static int getHusbandryProgress(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) {
			return 0;
		}
		return Math.max(0, stack.get(DataComponents.CUSTOM_DATA).copyTag().getInt(EXPERIENCE_PROGRESS_KEY));
	}

	public static int addHusbandryProgress(ItemStack stack, int amount) {
		if (stack.isEmpty() || amount <= 0) {
			return getHusbandryProgress(stack);
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		int next = Math.max(0, tag.getInt(EXPERIENCE_PROGRESS_KEY) + amount);
		tag.putInt(EXPERIENCE_PROGRESS_KEY, next);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return next;
	}

	public static MorphlingHungerRules.HungerState hungerState(ItemStack stack, long now) {
		return MorphlingHungerRules.state(isHungerEnabled(), getMaturityLevel(stack), isWildBound(stack),
				now, getLastFedGameTime(stack), fedDurationTicks());
	}

	public static int passiveAmplifier(Player player, ItemStack stack, int baseAmplifier) {
		long now = player == null ? 0L : player.level().getGameTime();
		return MorphlingHungerRules.adjustedAmplifier(baseAmplifier, hungerState(stack, now));
	}

	public static void applyHungerTick(Player player, ItemStack stack) {
		if (player == null || player.level().isClientSide || !isHungerEnabled()) {
			return;
		}
		MorphlingHungerRules.HungerState state = hungerState(stack, player.level().getGameTime());
		if (!MorphlingHungerRules.shouldApplyStarvingDrain(state)) {
			return;
		}
		int interval = Math.max(1, drainIntervalTicks());
		if (player.tickCount % interval != 0) {
			return;
		}
		double rate = starvingDrainRate();
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (!volume.isActive()) {
				return;
			}
			if (rate > 0.0D && volume.getBloodVolume() > 0.0D) {
				double drained = Math.min(rate, volume.getBloodVolume());
				if (volume.drain(drained) && player instanceof ServerPlayer serverPlayer) {
					BloodVolumeEvents.syncVolume(serverPlayer, volume);
				}
			}
		});
		applyMorphicStrain(player, 80, 0);
	}

	/**
	 * Returns the maturity level name for display purposes.
	 */
	public static String getMaturityName(int level) {
		if (level < 0 || level >= MATURITY_NAMES.length) return MATURITY_NAMES[0];
		return MATURITY_NAMES[level];
	}

	/**
	 * Calculates the effective power contribution of an enzyme based on tendency
	 * match. Preferred = 100%, Secondary = 75%, Other = 50%.
	 */
	public static float calculateEffectivePower(float rawPower, EnumBloodTendency enzymeTendency,
			EnumBloodTendency preferred, EnumBloodTendency secondary) {
		if (enzymeTendency == preferred) {
			return rawPower;
		} else if (enzymeTendency == secondary) {
			return rawPower * 0.75f;
		} else {
			return rawPower * 0.5f;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);

		String binomial = binomialKey();
		if (!binomial.isBlank()) {
			tooltip.add(Component.translatable(binomial)
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		// Show preferred enzyme info
		EnumBloodTendency preferred = this.getPreferredTendency();
		EnumBloodTendency secondary = this.getSecondaryTendency();
		tooltip.add(Component.literal("Preferred: " + formatTendencyName(preferred))
				.withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.literal("Secondary: " + formatTendencyName(secondary))
				.withStyle(ChatFormatting.DARK_AQUA));

		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag morphTag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
			float power = morphTag.getFloat("EnzymePower");
			int feedings = morphTag.getInt("EnzymeFeedings");
			if (feedings > 0) {
				int maturity = getMaturityLevel(stack);
				tooltip.add(Component.literal("Maturity: " + getMaturityName(maturity))
						.withStyle(MATURITY_COLORS[maturity]));
				tooltip.add(Component.literal("Enzyme Power: " + String.format("%.1f", power))
						.withStyle(ChatFormatting.DARK_GREEN));
				tooltip.add(Component.literal("Feedings: " + feedings)
						.withStyle(ChatFormatting.GOLD));
				if (morphTag.getBoolean(WILD_BOUND_KEY)) {
					tooltip.add(Component.literal("Wild-bound: Incubate to unlock further maturity")
							.withStyle(ChatFormatting.DARK_AQUA));
				}
				if (isHungerEnabled() && maturity >= 3 && !morphTag.getBoolean(WILD_BOUND_KEY)) {
					long now = context.level() == null ? 0L : context.level().getGameTime();
					tooltip.add(Component.literal("Hunger: " + formatHungerState(hungerState(stack, now)))
							.withStyle(ChatFormatting.DARK_RED));
				}

				// Show progress to next level
				if (maturity < PrimalMorphlingRules.APEX_LEVEL) {
					float nextThreshold = MATURITY_THRESHOLDS[maturity + 1];
					tooltip.add(Component.literal("Next level at: " + String.format("%.0f", nextThreshold) + " power")
							.withStyle(ChatFormatting.DARK_GRAY));
				} else if (maturity == PrimalMorphlingRules.APEX_LEVEL) {
					tooltip.add(Component.literal("Primalization: Morphic Nectar after Apotheos")
							.withStyle(ChatFormatting.DARK_PURPLE));
				}

				// Show maturity bonuses
				List<Component> bonuses = this.getMaturityBonusDescriptions(maturity);
				if (!bonuses.isEmpty()) {
					tooltip.add(Component.empty());
					tooltip.add(Component.literal("Maturity Bonuses:")
							.withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
					tooltip.addAll(bonuses);
				}
			}
		}
	}

	protected String binomialKey() {
		return "";
	}

	private static String formatTendencyName(EnumBloodTendency tendency) {
		String name = tendency.name();
		if (name.isEmpty()) return "Unknown";
		return name.charAt(0) + name.substring(1).toLowerCase();
	}

	private static String formatHungerState(MorphlingHungerRules.HungerState state) {
		String name = state.name().toLowerCase();
		return name.charAt(0) + name.substring(1);
	}

	/**
	 * Helper to create a maturity bonus tooltip line. Unlocked bonuses are shown
	 * in green, locked ones in dark gray with strikethrough.
	 */
	public static Component maturityBonusLine(String description, int requiredLevel, int currentLevel) {
		String prefix = " " + MATURITY_NAMES[requiredLevel] + ": ";
		if (currentLevel >= requiredLevel) {
			return Component.literal(prefix + description)
					.withStyle(ChatFormatting.GREEN);
		} else {
			return Component.literal(prefix + description)
					.withStyle(ChatFormatting.DARK_GRAY);
		}
	}

	public static boolean tryBeginPrimalAbility(Player player, ItemStack stack, String abilityKey,
			double bloodCost, int cooldownTicks, int strainTicks, int strainAmplifier) {
		if (!isPrimal(stack) || player.level().isClientSide) return false;
		long now = player.level().getGameTime();
		long lastUse = getLastAbilityTick(stack, PRIMAL_ABILITY_PREFIX + abilityKey);
		if (now - lastUse < cooldownTicks) {
			player.displayClientMessage(Component.literal("The primal morphling is still recoiling.")
					.withStyle(ChatFormatting.DARK_PURPLE), true);
			return false;
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() < bloodCost) {
			player.displayClientMessage(Component.literal("The primal morphling hungers for more blood.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		if (!volume.drain(bloodCost)) return false;
		if (player instanceof ServerPlayer serverPlayer) {
			BloodVolumeEvents.syncVolume(serverPlayer, volume);
		}
		applyMorphicStrain(player,
				PrimalMorphlingRules.morphicStrainDuration(strainTicks,
						SkillPointHelper.getPrimalMorphogenesisLevel(player)),
				strainAmplifier);
		setLastAbilityTick(stack, PRIMAL_ABILITY_PREFIX + abilityKey, now);
		return true;
	}

	public static void applyMorphicStrain(Player player, int durationTicks, int amplifier) {
		if (durationTicks <= 0) return;
		int cappedAmplifier = Math.max(0, Math.min(amplifier, 2));
		player.addEffect(new MobEffectInstance(EffectInit.morphic_strain,
				durationTicks, cappedAmplifier, true, true, true));
	}

	public static LivingEntity findLookTarget(Player player, double range) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 eye = player.getEyePosition();
		AABB area = player.getBoundingBox().inflate(range);
		return player.level().getEntitiesOfClass(LivingEntity.class, area,
						target -> target != player && target.isAlive()
								&& target.distanceToSqr(player) <= range * range
								&& target.getBoundingBox().getCenter().subtract(eye).normalize().dot(look) > 0.85D)
				.stream()
				.min(Comparator.comparingDouble(target -> target.distanceToSqr(player)))
				.orElse(null);
	}

	/**
	 * Returns the game time (tick) at which an ability was last triggered.
	 * Stored in the morphling's NBT under "Cooldowns" compound. Returns 0 if
	 * the ability has never been triggered.
	 */
	public static long getLastAbilityTick(ItemStack stack, String abilityKey) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) return 0;
		var tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
		if (!tag.contains("Cooldowns")) return 0;
		return tag.getCompound("Cooldowns").getLong(abilityKey);
	}

	/**
	 * Stores the game time (tick) at which an ability was last triggered.
	 */
	public static void setLastAbilityTick(ItemStack stack, String abilityKey, long tick) {
		var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains("Cooldowns")) {
			tag.put("Cooldowns", new net.minecraft.nbt.CompoundTag());
		}
		tag.getCompound("Cooldowns").putLong(abilityKey, tick);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static void writeLong(ItemStack stack, String key, long value) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putLong(key, value);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static boolean isHungerEnabled() {
		return HemoServerConfig.MORPHLING_HUNGER_ENABLED != null
				&& HemoServerConfig.MORPHLING_HUNGER_ENABLED.get();
	}

	private static int fedDurationTicks() {
		return HemoServerConfig.MORPHLING_FED_DURATION_TICKS == null
				? 24000 : HemoServerConfig.MORPHLING_FED_DURATION_TICKS.get();
	}

	private static double starvingDrainRate() {
		return HemoServerConfig.MORPHLING_STARVING_DRAIN_RATE == null
				? 0.0D : HemoServerConfig.MORPHLING_STARVING_DRAIN_RATE.get();
	}

	private static int husbandryStageQuota() {
		return HemoServerConfig.MORPHLING_HUSBANDRY_STAGE_QUOTA == null
				? 0 : HemoServerConfig.MORPHLING_HUSBANDRY_STAGE_QUOTA.get();
	}

	private static int drainIntervalTicks() {
		return HemoServerConfig.MORPHLING_DRAIN_INTERVAL == null
				? 60 : HemoServerConfig.MORPHLING_DRAIN_INTERVAL.get();
	}

}
