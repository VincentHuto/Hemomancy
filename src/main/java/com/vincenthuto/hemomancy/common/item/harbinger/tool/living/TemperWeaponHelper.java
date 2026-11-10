package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.manipulation.TemperAffinityRules;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TemperWeaponHelper {
	public static final float BASE_DAMAGE_MULTIPLIER = 1.25f;
	public static final float MAX_ALIGNMENT_BONUS = 0.75f;
	public static final float FULL_ALIGNMENT_THRESHOLD = 100.0f;
	public static final float MAX_DAMAGE_MULTIPLIER = BASE_DAMAGE_MULTIPLIER + MAX_ALIGNMENT_BONUS;

	private TemperWeaponHelper() {
	}

	public static DamageSource createWeaponDamageSource(LivingEntity target, LivingEntity attacker) {
		return attacker instanceof Player player ? target.damageSources().playerAttack(player)
				: target.damageSources().mobAttack(attacker);
	}

	public static Optional<EnumBloodTemper> getWeaponTemper(ItemStack stack) {
		if (stack.getItem() instanceof ITemperAlignedWeapon alignedWeapon) {
			return Optional.ofNullable(alignedWeapon.hemomancy$getWeaponTemper());
		}
		return Optional.empty();
	}

	public static Optional<EnumBloodTemper> getWeaponSecondaryTemper(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return Optional.empty();
		}
		CompoundTag root = data.copyTag();
		String secondaryName = root.getString(LivingStaffWeaponFormHelper.SECONDARY_TEMPER_KEY);
		if (secondaryName.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(EnumBloodTemper.valueOf(secondaryName));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	public static float getAlignmentValue(Player player, EnumBloodTemper weaponTemper) {
		return Math.max(0.0f, HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(weaponTemper));
	}

	public static float getAlignmentProgress(Player player, EnumBloodTemper weaponTemper) {
		return Mth.clamp(getAlignmentValue(player, weaponTemper) / FULL_ALIGNMENT_THRESHOLD, 0.0f, 1.0f);
	}

	public static float getDamageMultiplier(Player player, EnumBloodTemper weaponTemper) {
		return BASE_DAMAGE_MULTIPLIER + (MAX_ALIGNMENT_BONUS * getAlignmentProgress(player, weaponTemper));
	}

	public static float getDamageMultiplier(Player player, LivingEntity target, EnumBloodTemper weaponTemper,
			@Nullable EnumBloodTemper secondaryTemper) {
		return TemperAffinityRules.damageMultiplier(player, target, weaponTemper, secondaryTemper);
	}

	public static boolean isOpposingTarget(LivingEntity target, EnumBloodTemper weaponTemper) {
		if (target instanceof WillEntity will) {
			return isOpposingTemper(weaponTemper, will.getSchool());
		}
		EnumBloodTemper opposingTemper = getOpposingTemper(weaponTemper);
		return BloodProfileData.profile(target.getType(),
                target.level().isClientSide).tempers().contains(opposingTemper);
	}

	public static boolean isOpposingTemper(@Nullable EnumBloodTemper weaponTemper,
			@Nullable EnumBloodTemper targetTemper) {
		return weaponTemper != null && targetTemper != null
				&& getOpposingTemper(weaponTemper) == targetTemper;
	}

	public static EnumBloodTemper getOpposingTemper(EnumBloodTemper temper) {
		return switch (temper) {
			case ANIMUS -> EnumBloodTemper.MORTEM;
			case MORTEM -> EnumBloodTemper.ANIMUS;
			case DUCTILIS -> EnumBloodTemper.FERRIC;
			case FERRIC -> EnumBloodTemper.DUCTILIS;
			case LUX -> EnumBloodTemper.TENEBRIS;
			case TENEBRIS -> EnumBloodTemper.LUX;
			case FLAMMEUS -> EnumBloodTemper.CONGEATIO;
			case CONGEATIO -> EnumBloodTemper.FLAMMEUS;
		};
	}

	public static String getTemperDisplayName(EnumBloodTemper temper) {
		return switch (temper) {
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

	public static String getEntityTagDisplayName(@Nullable EnumBloodTemper temper) {
		if (temper == null) {
			return "Unknown";
		}
		return switch (temper) {
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
	public static void appendTemperTooltip(ItemStack stack, List<Component> tooltip) {
		EnumBloodTemper weaponTemper = getWeaponTemper(stack).orElse(null);
		if (weaponTemper == null) {
			return;
		}

		EnumBloodTemper opposingTemper = getOpposingTemper(weaponTemper);
		if (opposingTemper == null) {
			return;
		}
		String opposingTagName = getEntityTagDisplayName(opposingTemper);
		String opposingTemperName = getTemperDisplayName(opposingTemper);
		tooltip.add(HemoText.translatable("message.hemomancy.temper_weapon_helper.aligned_temper",
				getTemperDisplayName(weaponTemper)).withStyle(ChatFormatting.DARK_RED));
		getWeaponSecondaryTemper(stack).ifPresent(secondaryTemper ->
				tooltip.add(HemoText.translatable("message.hemomancy.temper_weapon_helper.secondary_temper",
						getTemperDisplayName(secondaryTemper)).withStyle(ChatFormatting.DARK_PURPLE)));
		tooltip.add(HemoText.translatable("message.hemomancy.temper_weapon_helper.opposes_entities",
				opposingTagName,
				opposingTemperName).withStyle(ChatFormatting.GRAY));
		tooltip.add(HemoText.translatable("message.hemomancy.temper_weapon_helper.opposing_damage_x_x", String.format(Locale.ROOT, "%.2f", BASE_DAMAGE_MULTIPLIER), String.format(Locale.ROOT, "%.2f", MAX_DAMAGE_MULTIPLIER)).withStyle(ChatFormatting.GRAY));

		Player player = ClientBridge.get().localPlayer();
		if (player != null) {
			float alignment = getAlignmentValue(player, weaponTemper);
			float multiplier = getDamageMultiplier(player, weaponTemper);
			tooltip.add(HemoText.translatable("message.hemomancy.temper_weapon_helper.current_multiplier_x", String.format(Locale.ROOT, "%.2f", multiplier), getTemperDisplayName(weaponTemper), String.format(Locale.ROOT, "%.1f", alignment)).withStyle(ChatFormatting.AQUA));
		}
	}
}


