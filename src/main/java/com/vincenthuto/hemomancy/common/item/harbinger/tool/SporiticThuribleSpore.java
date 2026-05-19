package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public enum SporiticThuribleSpore {
	VIVACIOUS("vivacious_spores", EnumBloodTendency.ANIMUS, 0xB83A4B) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, true, true));
		}
	},
	FERVENT("fervent_spores", EnumBloodTendency.FLAMMEUS, 0xD86A2C) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.igniteForSeconds(2);
		}
	},
	NEUROTIC("neurotic_spores", EnumBloodTendency.DUCTILIS, 0x68D6D7) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(EffectInit.neural_overload, 60, 0, true, true, true));
		}
	},
	INCANDESCENT("incandescent_spores", EnumBloodTendency.LUX, 0xF4D66E) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, true, true));
		}
	},
	RUINOUS("ruinous_spores", EnumBloodTendency.MORTEM, 0x5C3A77) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, true, true, true));
		}
	},
	FRIGID("frigid_spores", EnumBloodTendency.CONGEATIO, 0x74A9C8) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, true, true));
		}
	},
	FERRIC("ferric_spores", EnumBloodTendency.FERRIC, 0xA76A37) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, true, true));
		}
	},
	UMBRAL("umbral_spores", EnumBloodTendency.TENEBRIS, 0x3A254A) {
		@Override
		public void applySecondary(LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true, true));
		}
	};

	private final String itemPath;
	private final EnumBloodTendency tendency;
	private final int color;

	SporiticThuribleSpore(String itemPath, EnumBloodTendency tendency, int color) {
		this.itemPath = itemPath;
		this.tendency = tendency;
		this.color = color;
	}

	public String itemPath() {
		return itemPath;
	}

	public ResourceLocation itemId() {
		return Hemomancy.rloc(itemPath);
	}

	public EnumBloodTendency tendency() {
		return tendency;
	}

	public int color() {
		return color;
	}

	public float red() {
		return (color >> 16) & 0xFF;
	}

	public float green() {
		return (color >> 8) & 0xFF;
	}

	public float blue() {
		return color & 0xFF;
	}

	public boolean matches(ItemStack stack) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return itemId().equals(itemId);
	}

	public ItemStack displayStack() {
		return new ItemStack(BuiltInRegistries.ITEM.get(itemId()));
	}

	public abstract void applySecondary(LivingEntity target);

	public static Optional<SporiticThuribleSpore> byItemStack(ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return byItemId(itemId);
	}

	public static Optional<SporiticThuribleSpore> byItemId(ResourceLocation itemId) {
		if (itemId == null || !Hemomancy.MOD_ID.equals(itemId.getNamespace())) {
			return Optional.empty();
		}
		return byItemPath(itemId.getPath());
	}

	public static Optional<SporiticThuribleSpore> byItemPath(String itemPath) {
		return Arrays.stream(values())
				.filter(spore -> spore.itemPath.equals(itemPath))
				.findFirst();
	}

	public static Optional<SporiticThuribleSpore> byStoredId(String storedId) {
		ResourceLocation id = ResourceLocation.tryParse(storedId);
		if (id == null) {
			return Optional.empty();
		}
		return byItemId(id);
	}
}
