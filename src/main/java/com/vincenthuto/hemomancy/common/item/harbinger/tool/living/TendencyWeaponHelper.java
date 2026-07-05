package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TendencyWeaponHelper {
	public static final float BASE_DAMAGE_MULTIPLIER = 1.25f;
	public static final float MAX_ALIGNMENT_BONUS = 0.75f;
	public static final float FULL_ALIGNMENT_THRESHOLD = 100.0f;
	public static final float MAX_DAMAGE_MULTIPLIER = BASE_DAMAGE_MULTIPLIER + MAX_ALIGNMENT_BONUS;

	private TendencyWeaponHelper() {
	}

	public static DamageSource createWeaponDamageSource(LivingEntity target, LivingEntity attacker) {
		return attacker instanceof Player player ? target.damageSources().playerAttack(player)
				: target.damageSources().mobAttack(attacker);
	}

	public static Optional<EnumBloodTendency> getWeaponTendency(ItemStack stack) {
		if (stack.getItem() instanceof ITendencyAlignedWeapon alignedWeapon) {
			return Optional.ofNullable(alignedWeapon.hemomancy$getWeaponTendency());
		}
		return Optional.empty();
	}

	public static Optional<EnumBloodTendency> getWeaponSecondaryTendency(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return Optional.empty();
		}
		CompoundTag root = data.copyTag();
		String secondaryName = root.getString(LivingStaffWeaponFormHelper.SECONDARY_TENDENCY_KEY);
		if (secondaryName.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(EnumBloodTendency.valueOf(secondaryName));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	public static float getAlignmentValue(Player player, EnumBloodTendency weaponTendency) {
		return HemoCapabilityAccess.getBloodTendency(player)
				.map(tendency -> Math.max(0.0f, tendency.getAlignmentByTendency(weaponTendency)))
				.orElse(0.0f);
	}

	public static float getAlignmentProgress(Player player, EnumBloodTendency weaponTendency) {
		return Mth.clamp(getAlignmentValue(player, weaponTendency) / FULL_ALIGNMENT_THRESHOLD, 0.0f, 1.0f);
	}

	public static float getDamageMultiplier(Player player, EnumBloodTendency weaponTendency) {
		return BASE_DAMAGE_MULTIPLIER + (MAX_ALIGNMENT_BONUS * getAlignmentProgress(player, weaponTendency));
	}

	public static float getDamageMultiplier(Player player, LivingEntity target, EnumBloodTendency weaponTendency,
			@Nullable EnumBloodTendency secondaryTendency) {
		return TendencyAffinityRules.damageMultiplier(player, target, weaponTendency, secondaryTendency);
	}

	public static boolean isOpposingTarget(LivingEntity target, EnumBloodTendency weaponTendency) {
		if (target instanceof WillEntity will) {
			return isOpposingTendency(weaponTendency, will.getSchool());
		}
		EnumBloodTendency opposingTendency = getOpposingTendency(weaponTendency);
		TagKey<EntityType<?>> opposingTag = getEntityTagForTendency(opposingTendency);
		return opposingTag != null && target.getType().is(opposingTag);
	}

	public static boolean isOpposingTendency(@Nullable EnumBloodTendency weaponTendency,
			@Nullable EnumBloodTendency targetTendency) {
		return weaponTendency != null && targetTendency != null
				&& getOpposingTendency(weaponTendency) == targetTendency;
	}

	public static EnumBloodTendency getOpposingTendency(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> EnumBloodTendency.MORTEM;
			case MORTEM -> EnumBloodTendency.ANIMUS;
			case DUCTILIS -> EnumBloodTendency.FERRIC;
			case FERRIC -> EnumBloodTendency.DUCTILIS;
			case LUX -> EnumBloodTendency.TENEBRIS;
			case TENEBRIS -> EnumBloodTendency.LUX;
			case FLAMMEUS -> EnumBloodTendency.CONGEATIO;
			case CONGEATIO -> EnumBloodTendency.FLAMMEUS;
		};
	}

	@Nullable
	public static TagKey<EntityType<?>> getEntityTagForTendency(@Nullable EnumBloodTendency tendency) {
		if (tendency == null) {
			return null;
		}
		return switch (tendency) {
			case ANIMUS -> EntityInit.VIVACIOUS_TAG;
			case MORTEM -> EntityInit.RUINOUS_TAG;
			case DUCTILIS -> EntityInit.NEUROTIC_TAG;
			case FERRIC -> EntityInit.FERRIC_TAG;
			case LUX -> EntityInit.INCANDESCENT_TAG;
			case TENEBRIS -> EntityInit.UMBRAL_TAG;
			case FLAMMEUS -> EntityInit.FERVENT_TAG;
			case CONGEATIO -> EntityInit.FRIGID_TAG;
		};
	}

	public static String getTendencyDisplayName(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> "Animus";
			case MORTEM -> "Mortem";
			case DUCTILIS -> "Ductilis";
			case FERRIC -> "Ferric";
			case LUX -> "Lux";
			case TENEBRIS -> "Tenebris";
			case FLAMMEUS -> "Flammeus";
			case CONGEATIO -> "Congeatio";
		};
	}

	public static String getEntityTagDisplayName(@Nullable EnumBloodTendency tendency) {
		if (tendency == null) {
			return "Unknown";
		}
		return switch (tendency) {
			case ANIMUS -> "Vivacious";
			case MORTEM -> "Ruinous";
			case DUCTILIS -> "Neurotic";
			case FERRIC -> "Ferric";
			case LUX -> "Incandescent";
			case TENEBRIS -> "Umbral";
			case FLAMMEUS -> "Fervent";
			case CONGEATIO -> "Frigid";
		};
	}

	@OnlyIn(Dist.CLIENT)
	public static void appendTendencyTooltip(ItemStack stack, List<Component> tooltip) {
		EnumBloodTendency weaponTendency = getWeaponTendency(stack).orElse(null);
		if (weaponTendency == null) {
			return;
		}

		EnumBloodTendency opposingTendency = getOpposingTendency(weaponTendency);
		if (opposingTendency == null) {
			return;
		}
		String opposingTagName = getEntityTagDisplayName(opposingTendency);
		String opposingTendencyName = getTendencyDisplayName(opposingTendency);
		tooltip.add(Component.literal("Aligned Tendency: " + getTendencyDisplayName(weaponTendency))
				.withStyle(ChatFormatting.DARK_RED));
		getWeaponSecondaryTendency(stack).ifPresent(secondaryTendency ->
				tooltip.add(Component.literal("Secondary Tendency: " + getTendencyDisplayName(secondaryTendency))
						.withStyle(ChatFormatting.DARK_PURPLE)));
		tooltip.add(Component.literal("Opposes: " + opposingTagName + " / " + opposingTendencyName + " entities")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal(String.format(Locale.ROOT, "Opposing damage: x%.2f - x%.2f",
				BASE_DAMAGE_MULTIPLIER, MAX_DAMAGE_MULTIPLIER)).withStyle(ChatFormatting.GRAY));

		Player player = Minecraft.getInstance().player;
		if (player != null) {
			float alignment = getAlignmentValue(player, weaponTendency);
			float multiplier = getDamageMultiplier(player, weaponTendency);
			tooltip.add(Component.literal(String.format(Locale.ROOT, "Current multiplier: x%.2f (%s %.1f)",
					multiplier, getTendencyDisplayName(weaponTendency), alignment)).withStyle(ChatFormatting.AQUA));
		}
	}
}


