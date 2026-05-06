package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MorphicNectarMutationRules {
	public static final String MUTATED_KEY = "MorphicNectarMutated";
	public static final int MUTATED_BORDER_COLOR = 0xAA516414;
	public static final int PRIMAL_BORDER_COLOR = 0xDDD13218;
	private static final int PRIMAL_TENDRIL_COUNT = 5;
	private static final int PRIMAL_TENDRIL_FRAME_MILLIS = 55;
	private static final int PRIMAL_TENDRIL_SEGMENTS = 18;
	private static final int PRIMAL_OVERLAY_FRAME_COUNT = 16;
	private static final int PRIMAL_OVERLAY_FRAME_SIZE = 64;
	private static final int PRIMAL_OVERLAY_SPRITE_COUNT = 4;
	private static final int PRIMAL_PROCEDURAL_CURVE_SAMPLES = 42;
	private static final int PRIMAL_PROCEDURAL_LAYER_COUNT = 3;
	private static final int PRIMAL_PROCEDURAL_CORE_FILL_ALPHA = 0;
	private static final int PRIMAL_PROCEDURAL_OVERLAY_Z = 0;
	private static final boolean PRIMAL_PROCEDURAL_USES_MANAGED_GUI_BUFFER = true;
	private static final boolean PRIMAL_PROCEDURAL_USES_VANILLA_GUI_RENDER_TYPE = true;
	private static final boolean PRIMAL_PROCEDURAL_USES_VANILLA_GUI_QUAD_WINDING = true;
	private static final int PRIMAL_PROCEDURAL_BODY_WIDTH_CENTIPIXELS = 125;
	private static final int PRIMAL_PROCEDURAL_ENDPOINT_TRIM_PERCENT = 8;
	private static final int[] TENDRIL_COLORS = new int[] {
			0xDDD13218,
			0xCC60791C,
			0xFFE0B536
	};

	private MorphicNectarMutationRules() {
	}

	public static boolean shouldShowMutation(boolean mutated, boolean primal) {
		return mutated || primal;
	}

	public static int borderColor(boolean primal) {
		return primal ? PRIMAL_BORDER_COLOR : MUTATED_BORDER_COLOR;
	}

	public static int tendrilColor(int phase) {
		int index = Math.floorMod(phase, TENDRIL_COLORS.length);
		return TENDRIL_COLORS[index];
	}

	public static int primalTendrilCount() {
		return PRIMAL_TENDRIL_COUNT;
	}

	public static int primalTendrilFrameMillis() {
		return PRIMAL_TENDRIL_FRAME_MILLIS;
	}

	public static int primalTendrilSegments() {
		return PRIMAL_TENDRIL_SEGMENTS;
	}

	public static int primalOverlayFrameCount() {
		return PRIMAL_OVERLAY_FRAME_COUNT;
	}

	public static int primalOverlayFrameSize() {
		return PRIMAL_OVERLAY_FRAME_SIZE;
	}

	public static int primalOverlaySpriteCount() {
		return PRIMAL_OVERLAY_SPRITE_COUNT;
	}

	public static int primalProceduralCurveSamples() {
		return PRIMAL_PROCEDURAL_CURVE_SAMPLES;
	}

	public static int primalProceduralLayerCount() {
		return PRIMAL_PROCEDURAL_LAYER_COUNT;
	}

	public static int primalProceduralCoreFillAlpha() {
		return PRIMAL_PROCEDURAL_CORE_FILL_ALPHA;
	}

	public static int primalProceduralOverlayZ() {
		return PRIMAL_PROCEDURAL_OVERLAY_Z;
	}

	public static boolean primalProceduralUsesManagedGuiBuffer() {
		return PRIMAL_PROCEDURAL_USES_MANAGED_GUI_BUFFER;
	}

	public static boolean primalProceduralUsesVanillaGuiRenderType() {
		return PRIMAL_PROCEDURAL_USES_VANILLA_GUI_RENDER_TYPE;
	}

	public static boolean primalProceduralUsesVanillaGuiQuadWinding() {
		return PRIMAL_PROCEDURAL_USES_VANILLA_GUI_QUAD_WINDING;
	}

	public static int primalProceduralBodyWidthCentipixels() {
		return PRIMAL_PROCEDURAL_BODY_WIDTH_CENTIPIXELS;
	}

	public static int primalProceduralEndpointTrimPercent() {
		return PRIMAL_PROCEDURAL_ENDPOINT_TRIM_PERCENT;
	}

	public static void markMutated(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(MUTATED_KEY, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean hasMutationMarker(ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_DATA)
				&& stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(MUTATED_KEY);
	}

	public static boolean shouldShowMutation(ItemStack stack) {
		return shouldShowMutation(hasMutationMarker(stack), MorphlingItem.isPrimal(stack));
	}
}
