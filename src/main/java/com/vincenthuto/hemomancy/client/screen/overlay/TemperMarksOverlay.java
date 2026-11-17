package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.HudAssembly;
import com.vincenthuto.hemomancy.client.screen.overlay.bloodhud.TemperReading;
import com.vincenthuto.hemomancy.client.screen.widget.TemperPhials;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The hematic HUD's Temper pool reader and the real-cursor tooltip over its vials. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class TemperMarksOverlay {
	private TemperMarksOverlay() {
	}

	/** One entry per school in enum order, null where the player has never held or run dry in that school. */
	public static TemperReading[] readings(Player player) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		float castCost = TemperRules.castCost(EnumManipulationRank.HUMILIS);
		EnumBloodTemper[] schools = EnumBloodTemper.values();
		TemperReading[] readings = new TemperReading[schools.length];
		for (EnumBloodTemper school : schools) {
			if (!temper.hasSeen(school)) continue;
			float cap = TemperRules.cap(temperament.getAlignmentByTemper(school));
			float held = temper.get(school);
			readings[school.ordinal()] = new TemperReading(school.ordinal(), held, cap,
					cap <= 0 ? 0 : Math.min(1.0F, held / cap), held < castCost, TemperPhials.schoolColour(school, 1.0F));
		}
		return readings;
	}

	static boolean hasSeen(Player player) {
		return HemoCapabilityAccess.getBloodTemper(player).seenMask() != 0;
	}

	/** With the cursor free over the HUD, name the vial under it. */
	@SubscribeEvent
	public static void onScreenRender(ScreenEvent.Render.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		Screen screen = event.getScreen();
		if (minecraft.options.hideGui || !BloodVolumeOverlay.isHudAvailable(player)) return;
		int mouseX = event.getMouseX();
		int mouseY = event.getMouseY();
		if (screen instanceof AbstractContainerScreen<?> container) {
			if (container.getSlotUnderMouse() != null) return;
			if (mouseX >= container.getGuiLeft() && mouseX < container.getGuiLeft() + container.getXSize()
					&& mouseY >= container.getGuiTop() && mouseY < container.getGuiTop() + container.getYSize()) return;
		} else if (!(screen instanceof ChatScreen)) {
			return;
		}
		BloodHudLayout layout = BloodVolumeOverlay.layout(player);
		if (!layout.temper() || player.isSpectator()) return;
		BloodHudLayout.Placement anchor = BloodVolumeOverlay.placement(player, screen.width, screen.height);
		HudAssembly assembly = BloodVolumeOverlay.assembly(player);
		if (assembly == null) return;
		int school = assembly.schoolAt(anchor.localX(mouseX), anchor.localY(mouseY));
		TemperReading[] readings = readings(player);
		if (school < 0 || readings[school] == null) return;
		// The upper vials can sit close to the screen edge; leave room for vanilla's upward tooltip offset.
		event.getGuiGraphics().renderComponentTooltip(minecraft.font, tooltip(readings[school]), mouseX, Math.max(16, mouseY));
	}

	private static List<Component> tooltip(TemperReading reading) {
		List<Component> lines = new ArrayList<>();
		EnumBloodTemper school = EnumBloodTemper.values()[reading.school()];
		Component name = Component.translatable("blood_temper.hemomancy." + school.name().toLowerCase(Locale.ROOT));
		int colour = reading.colour() & 0xFFFFFF;
		lines.add(Component.translatable("screen.hemomancy.temper_rack.title", name)
				.withStyle(style -> style.withColor(colour)));
		lines.add(Component.translatable("screen.hemomancy.temper_rack.held", amount(reading.held()), amount(reading.cap()))
				.withStyle(ChatFormatting.GRAY));
		if (reading.dry()) {
			lines.add(Component.translatable("screen.hemomancy.temper_rack.dry").withStyle(ChatFormatting.RED));
		} else if (reading.full()) {
			lines.add(Component.translatable("screen.hemomancy.temper_rack.full").withStyle(ChatFormatting.GOLD));
		}
		lines.add(Component.translatable("screen.hemomancy.temper_rack.hint").withStyle(ChatFormatting.DARK_GRAY,
				ChatFormatting.ITALIC));
		return lines;
	}

	private static String amount(float value) {
		float rounded = Math.round(value * 10.0F) / 10.0F;
		return rounded == Math.floor(rounded) ? Integer.toString((int) rounded) : String.format(Locale.ROOT, "%.1f", rounded);
	}
}
