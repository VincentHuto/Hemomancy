package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Locale;

public enum LivingWeaponForm {
	BLADE("blade", "Blade", "edge-pattern", "conjure_blade"),
	AXE("axe", "Axe", "cleaving-pattern", "conjure_axe"),
	SPEAR("spear", "Spear", "reach-pattern", "conjure_spear"),
	CLAWS("claws", "Claw", "grasping-pattern", "conjure_claws"),
	CROSSBOW("crossbow", "Crossbow", "tension-pattern", "conjure_crossbow"),
	TORCH("torch", "Torch", "burning-pattern", "conjure_torch"),
	FLAIL("flail", "Flail", "chained-cold pattern", "conjure_flail");

	public static final Codec<LivingWeaponForm> CODEC = Codec.STRING.comapFlatMap(
			value -> fromSerializedName(value)
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "Unknown living weapon form: " + value)),
			LivingWeaponForm::serializedName);
	public static final StreamCodec<RegistryFriendlyByteBuf, LivingWeaponForm> STREAM_CODEC = StreamCodec.of(
			(buf, form) -> buf.writeUtf(form.serializedName()),
			buf -> bySerializedName(buf.readUtf()));

	private final String serializedName;
	private final String displayName;
	private final String patternName;
	private final String manipulationName;

	LivingWeaponForm(String serializedName, String displayName, String patternName, String manipulationName) {
		this.serializedName = serializedName;
		this.displayName = displayName;
		this.patternName = patternName;
		this.manipulationName = manipulationName;
	}

	public String serializedName() {
		return serializedName;
	}

	public String displayName() {
		return displayName;
	}

	public String graftName() {
		return displayName + " Graft";
	}

	public String patternName() {
		return patternName;
	}

	public String manipulationName() {
		return manipulationName;
	}

	public String manipulationDisplayName() {
		String base = manipulationName.startsWith("conjure_") ? manipulationName.substring("conjure_".length())
				: manipulationName;
		return "Conjure " + base.substring(0, 1).toUpperCase(Locale.ROOT) + base.substring(1);
	}

	public DeferredHolder<BloodManipulation, BloodManipulation> manipulationHolder() {
		return switch (this) {
		case BLADE -> ManipulationInit.conjure_blade;
		case AXE -> ManipulationInit.conjure_axe;
		case SPEAR -> ManipulationInit.conjure_spear;
		case CLAWS -> ManipulationInit.conjure_claws;
		case CROSSBOW -> ManipulationInit.conjure_crossbow;
		case TORCH -> ManipulationInit.conjure_torch;
		case FLAIL -> ManipulationInit.conjure_flail;
		};
	}

	public static LivingWeaponForm bySerializedName(String name) {
		return fromSerializedName(name).orElse(BLADE);
	}

	public static java.util.Optional<LivingWeaponForm> fromSerializedName(String name) {
		if (name == null) {
			return java.util.Optional.empty();
		}
		for (LivingWeaponForm form : values()) {
			if (form.serializedName.equals(name)) {
				return java.util.Optional.of(form);
			}
		}
		return java.util.Optional.empty();
	}
}
