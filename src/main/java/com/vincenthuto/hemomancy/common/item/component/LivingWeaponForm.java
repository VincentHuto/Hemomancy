package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

public enum LivingWeaponForm {
	BLADE("blade", "conjure_blade"),
	AXE("axe", "conjure_axe"),
	SPEAR("spear", "conjure_spear"),
	CLAWS("claws", "conjure_claws"),
	CROSSBOW("crossbow", "conjure_crossbow"),
	TORCH("torch", "conjure_torch"),
	FLAIL("flail", "conjure_flail");

	public static final Codec<LivingWeaponForm> CODEC = Codec.STRING.comapFlatMap(
			value -> fromSerializedName(value)
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "Unknown living weapon form: " + value)),
			LivingWeaponForm::serializedName);
	public static final StreamCodec<RegistryFriendlyByteBuf, LivingWeaponForm> STREAM_CODEC = StreamCodec.of(
			(buf, form) -> buf.writeUtf(form.serializedName()),
			buf -> bySerializedName(buf.readUtf()));

	private final String serializedName;
	private final String manipulationName;

	LivingWeaponForm(String serializedName, String manipulationName) {
		this.serializedName = serializedName;
		this.manipulationName = manipulationName;
	}

	public String serializedName() {
		return serializedName;
	}

	public MutableComponent displayName() {
		return Component.translatable(langKey());
	}

	public MutableComponent graftName() {
		return Component.translatable(langKey() + ".graft");
	}

	public MutableComponent patternName() {
		return Component.translatable(langKey() + ".pattern");
	}

	public String manipulationName() {
		return manipulationName;
	}

	public MutableComponent manipulationDisplayName() {
		return Component.translatable(langKey() + ".manipulation");
	}

	private String langKey() {
		return "living_weapon_form.hemomancy." + serializedName;
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
