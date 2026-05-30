package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class LivingStaffFocusRules {
	public static final String VESPER_MEMORY_AWAKENED_KEY = "VesperMemoryAwakened";
	public static final String BLOOD_HANDLED_KEY = "BloodHandled";
	public static final double ADEPT_FOCUS_BLOOD_HANDLED = 500.0D;
	public static final double DEEP_FOCUS_BLOOD_HANDLED = 3000.0D;
	public static final double GRAND_FOCUS_BLOOD_HANDLED = 12000.0D;

	private static final int BARE_ABSORPTION_TARGET_CAP = 1;
	private static final int STAFF_BASE_ABSORPTION_TARGET_CAP = 3;
	private static final int STAFF_ADEPT_ABSORPTION_TARGET_CAP = 4;
	private static final int STAFF_GRAND_ABSORPTION_TARGET_CAP = 5;
	private static final int VESPER_ABSORPTION_TARGET_CAP = 6;

	private static final double BARE_STRUCTURE_PROJECTION_RATE = 5.0D;
	private static final double STAFF_STRUCTURE_PROJECTION_RATE = 8.0D;
	private static final double STAFF_ADEPT_STRUCTURE_PROJECTION_RATE = 10.0D;
	private static final double STAFF_DEEP_STRUCTURE_PROJECTION_RATE = 12.0D;
	private static final double STAFF_GRAND_STRUCTURE_PROJECTION_RATE = 15.0D;
	private static final double VESPER_STRUCTURE_PROJECTION_RATE = 20.0D;

	private static final double BARE_TILE_PROJECTION_RATE = 100.0D;
	private static final double STAFF_TILE_PROJECTION_RATE = 150.0D;
	private static final double STAFF_ADEPT_TILE_PROJECTION_RATE = 200.0D;
	private static final double STAFF_DEEP_TILE_PROJECTION_RATE = 250.0D;
	private static final double STAFF_GRAND_TILE_PROJECTION_RATE = 325.0D;
	private static final double VESPER_TILE_PROJECTION_RATE = 450.0D;

	private LivingStaffFocusRules() {
	}

	public static int absorptionTargetCap(boolean usingLivingStaff, boolean vesperAwakened, double bloodHandled) {
		if (!usingLivingStaff) {
			return BARE_ABSORPTION_TARGET_CAP;
		}
		if (vesperAwakened) {
			return VESPER_ABSORPTION_TARGET_CAP;
		}
		if (bloodHandled >= GRAND_FOCUS_BLOOD_HANDLED) {
			return STAFF_GRAND_ABSORPTION_TARGET_CAP;
		}
		if (bloodHandled >= ADEPT_FOCUS_BLOOD_HANDLED) {
			return STAFF_ADEPT_ABSORPTION_TARGET_CAP;
		}
		return STAFF_BASE_ABSORPTION_TARGET_CAP;
	}

	public static double absorptionDamagePerTarget(boolean vesperAwakened, double bloodHandled) {
		if (vesperAwakened) {
			return 3.5D;
		}
		if (bloodHandled >= DEEP_FOCUS_BLOOD_HANDLED) {
			return 3.0D;
		}
		if (bloodHandled >= ADEPT_FOCUS_BLOOD_HANDLED) {
			return 2.5D;
		}
		return 2.0D;
	}

	public static int absorptionPulseIntervalTicks(boolean vesperAwakened, double bloodHandled) {
		if (vesperAwakened) {
			return 4;
		}
		if (bloodHandled >= DEEP_FOCUS_BLOOD_HANDLED) {
			return 5;
		}
		if (bloodHandled >= ADEPT_FOCUS_BLOOD_HANDLED) {
			return 6;
		}
		return 8;
	}

	public static double structureProjectionRate(boolean usingLivingStaff, boolean vesperAwakened,
			double bloodHandled) {
		if (!usingLivingStaff) {
			return BARE_STRUCTURE_PROJECTION_RATE;
		}
		if (vesperAwakened) {
			return VESPER_STRUCTURE_PROJECTION_RATE;
		}
		if (bloodHandled >= GRAND_FOCUS_BLOOD_HANDLED) {
			return STAFF_GRAND_STRUCTURE_PROJECTION_RATE;
		}
		if (bloodHandled >= DEEP_FOCUS_BLOOD_HANDLED) {
			return STAFF_DEEP_STRUCTURE_PROJECTION_RATE;
		}
		if (bloodHandled >= ADEPT_FOCUS_BLOOD_HANDLED) {
			return STAFF_ADEPT_STRUCTURE_PROJECTION_RATE;
		}
		return STAFF_STRUCTURE_PROJECTION_RATE;
	}

	public static double bloodTileProjectionRate(boolean usingLivingStaff, boolean vesperAwakened,
			double bloodHandled) {
		if (!usingLivingStaff) {
			return BARE_TILE_PROJECTION_RATE;
		}
		if (vesperAwakened) {
			return VESPER_TILE_PROJECTION_RATE;
		}
		if (bloodHandled >= GRAND_FOCUS_BLOOD_HANDLED) {
			return STAFF_GRAND_TILE_PROJECTION_RATE;
		}
		if (bloodHandled >= DEEP_FOCUS_BLOOD_HANDLED) {
			return STAFF_DEEP_TILE_PROJECTION_RATE;
		}
		if (bloodHandled >= ADEPT_FOCUS_BLOOD_HANDLED) {
			return STAFF_ADEPT_TILE_PROJECTION_RATE;
		}
		return STAFF_TILE_PROJECTION_RATE;
	}

	public static boolean isVesperAwakened(ItemStack stack) {
		return getCustomData(stack).getBoolean(VESPER_MEMORY_AWAKENED_KEY);
	}

	public static double getBloodHandled(ItemStack stack) {
		return getCustomData(stack).getDouble(BLOOD_HANDLED_KEY);
	}

	public static void addBloodHandled(ItemStack stack, double amount) {
		if (amount <= 0.0D) {
			return;
		}
		CompoundTag tag = getCustomData(stack);
		tag.putDouble(BLOOD_HANDLED_KEY, tag.getDouble(BLOOD_HANDLED_KEY) + amount);
		setCustomData(stack, tag);
	}

	public static void markVesperAwakened(ItemStack stack) {
		CompoundTag tag = getCustomData(stack);
		tag.putBoolean(VESPER_MEMORY_AWAKENED_KEY, true);
		setCustomData(stack, tag);
	}

	private static CompoundTag getCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static void setCustomData(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
