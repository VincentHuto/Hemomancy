package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.DrawingContext;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * One slice of the possession wheel: the action's memory-style icon, a resting bar under it while it cools (or while
 * the return hold runs), a dim slice while resting and a lit, veined slice while a toggle is on.
 */
public class PossessionRadialMenuItem extends BlitRadialMenuItem {
	private static final int RESTING_SLICE = 0xBF2A1838;
	private static final int ON_SLICE = 0xA0704CB0;

	private final DoubleSupplier resting;
	private final BooleanSupplier on;
	private final Supplier<List<Component>> tooltip;
	private final Runnable click;

	public PossessionRadialMenuItem(GenericRadialMenu owner, PossessionAction action, DoubleSupplier resting,
			BooleanSupplier on, Supplier<List<Component>> tooltip, Runnable click) {
		super(owner, -1, icon(action), ManipulationIconResolver.MEMORY_BASE, 0, 0, 16, 16, 16, 16,
				Component.translatable("possession_action.hemomancy." + action.id()));
		this.resting = resting;
		this.on = on;
		this.tooltip = tooltip;
		this.click = click;
	}

	/** Each action borrows the memory overlay of the manipulation nearest to it. */
	private static ResourceLocation icon(PossessionAction action) {
		String memory = switch (action) {
			case RETURN -> "venous_travel";
			case STILL -> "absolute_stillness";
			case ECHOLOCATE -> "crimson_sight";
			case STEP -> "umbral_step";
			case SPIT -> "hematic_flare";
			case SLAM -> "lodestone_collapse";
			case LEAP -> "soaring_updraft";
			case LUNGE -> "blood_rush";
			case CLIMB -> "blood_lattice";
			case HOVER -> "suspended_updraft";
		};
		return Hemomancy.rloc("textures/item/memories/memory_" + memory + "_overlay.png");
	}

	@Override
	public int getBackgroundColor(int fallbackColor) {
		if (resting.getAsDouble() > 0.0) return RESTING_SLICE;
		return on.getAsBoolean() ? ON_SLICE : fallbackColor;
	}

	@Override
	public boolean hasVeinyBorder() {
		return on.getAsBoolean();
	}

	@Override
	public void draw(DrawingContext context) {
		super.draw(context);
		double fraction = Math.max(0.0, Math.min(1.0, resting.getAsDouble()));
		if (fraction <= 0.0) return;
		int left = (int) context.x - 8;
		int top = (int) context.y - 8;
		int filled = (int) Math.round(16.0 * fraction);
		context.graphics.fill(left, top, left + 16, top + 16, 0x70000000);
		context.graphics.fill(left - 1, top + 17, left + 17, top + 21, 0xD0100305);
		context.graphics.fill(left, top + 18, left + 16, top + 20, 0xC0241A30);
		context.graphics.fill(left, top + 18, left + filled, top + 20, 0xFFB8A0E0);
	}

	@Override
	public void drawTooltips(DrawingContext context) {
		context.graphics.renderTooltip(context.font, tooltip.get(), Optional.empty(), (int) context.x, (int) context.y);
	}

	@Override
	public boolean onClick() {
		click.run();
		return true;
	}
}
