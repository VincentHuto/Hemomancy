package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MorphlingItem extends Item implements IMorphling {

	public int bloodCost;

	// Maturity thresholds based on effective EnzymePower
	public static final float[] MATURITY_THRESHOLDS = { 0f, 10f, 30f, 60f, 100f };
	public static final String[] MATURITY_NAMES = { "Unfed", "Fledgling", "Developing", "Mature", "Apex" };
	public static final ChatFormatting[] MATURITY_COLORS = {
			ChatFormatting.GRAY, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN,
			ChatFormatting.GOLD, ChatFormatting.LIGHT_PURPLE
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
		float power = stack.get(DataComponents.CUSTOM_DATA).copyTag().getFloat("EnzymePower");
		for (int i = MATURITY_THRESHOLDS.length - 1; i > 0; i--) {
			if (power >= MATURITY_THRESHOLDS[i]) return i;
		}
		return 0;
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
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);

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

				// Show progress to next level
				if (maturity < MATURITY_THRESHOLDS.length - 1) {
					float nextThreshold = MATURITY_THRESHOLDS[maturity + 1];
					tooltip.add(Component.literal("Next level at: " + String.format("%.0f", nextThreshold) + " power")
							.withStyle(ChatFormatting.DARK_GRAY));
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

	private static String formatTendencyName(EnumBloodTendency tendency) {
		String name = tendency.name();
		if (name.isEmpty()) return "Unknown";
		return name.charAt(0) + name.substring(1).toLowerCase();
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

}
