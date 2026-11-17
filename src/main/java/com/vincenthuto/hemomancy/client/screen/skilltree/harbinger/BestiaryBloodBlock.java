package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.widget.TemperPhials;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.EntityBloodProfile;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptQuirks;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatVigorRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/**
 * A kind's blood in the Living Bestiary (core-loop round two, P6), as far as the player has read it: hollow unread
 * phials until a sample of the kind is examined, then its Tempers and properties, and once three individuals are
 * studied, what a draw at full Vigor gives and the kind's quirks. Drawn under a met creature's name and under a roster
 * specimen's authored text.
 */
final class BestiaryBloodBlock {
	private static final int LABEL = 0xFF8CA88C;
	private static final int TEXT = 0xFFD0D8CC;
	private static final int DIM = 0xFF91A091;

	private BestiaryBloodBlock() {
	}

	static boolean profiled(ResourceLocation id) {
		return id != null && BloodProfileData.snapshot(true).json().containsKey(id);
	}

	/** A met creature's status line: studied, read, or met. */
	static String statusKey(ResourceLocation id) {
		Player player = Minecraft.getInstance().player;
		if (BloodResearch.studied(player, id)) return "bestiary.hemomancy.status.studied";
		if (BloodResearch.knows(player, id)) return "bestiary.hemomancy.status.read";
		return "bestiary.hemomancy.status.met";
	}

	/** Draws the block from {@code y}; returns the Y below its last line. */
	static int draw(GuiGraphics gfx, ProgressScreenContext ctx, ResourceLocation id, int x, int y, int w) {
		Player player = Minecraft.getInstance().player;
		EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
		EntityBloodProfile profile = BloodProfileData.profile(type, true);
		gfx.drawString(ctx.font(), Component.translatable("bestiary.hemomancy.creature.blood"), x, y, LABEL, false);
		y += 12;
		boolean knows = BloodResearch.knows(player, id);
		int phialX = x;
		if (!knows) {
			for (int i = 0; i < Math.max(1, Math.min(3, profile.tempers().size())); i++) {
				TemperPhials.drawUnread(gfx, phialX, y, 2);
				phialX += TemperPhials.WIDTH * 2 + 4;
			}
			y += 22;
			return wrap(gfx, ctx, Component.translatable("bestiary.hemomancy.creature.unread"), x, y, w, DIM);
		}
		for (EnumBloodTemper school : profile.tempers()) {
			TemperPhials.draw(gfx, phialX, y, 2, school, 1.0F, false);
			phialX += TemperPhials.WIDTH * 2 + 4;
		}
		y += 22;
		y = wrap(gfx, ctx, Component.translatable("message.hemomancy.menagerie.readout.schools",
				SpecimenVessels.schoolList(profile.tempers())), x, y, w, TEXT);
		if (!profile.properties().isEmpty()) {
			MutableComponent properties = Component.empty();
			for (int i = 0; i < profile.properties().size(); i++) {
				ResourceLocation property = profile.properties().get(i);
				if (i > 0) properties.append(Component.translatable("bestiary.hemomancy.creature.list_separator"));
				properties.append(Component.translatableWithFallback("blood_property." + property.getNamespace() + "."
						+ property.getPath().replace('/', '.'), property.toString()));
			}
			y = wrap(gfx, ctx, Component.translatable("bestiary.hemomancy.creature.properties", properties), x, y + 2, w, DIM);
		}
		if (!BloodResearch.studied(player, id)) {
			int remaining = Math.max(0, ClinicalBloodProgress.STUDY_INDIVIDUALS - BloodResearch.individuals(player, id));
			return wrap(gfx, ctx, Component.translatable("bestiary.hemomancy.creature.study_remaining", remaining), x, y + 4, w,
					DIM);
		}
		y = wrap(gfx, ctx, Component.translatable("bestiary.hemomancy.creature.yield",
				number(VatVigorRules.temperPerDraw(VatVigorRules.MAX, 1.0D)),
				number(VatVigorRules.bloodPerDraw(VatVigorRules.MAX, 1.0D))), x, y + 4, w, TEXT);
		var facts = SpecimenVessels.quirkFacts(type, SpecimenVessels.baseMaxHealth(type), null, true);
		y += 2;
		for (Component quirk : KeptQuirks.lines(facts, BloodManipulation::temperName)) {
			y = wrap(gfx, ctx, Component.translatable("screen.hemomancy.specimen_vessel.quirk", quirk), x, y, w, DIM);
		}
		return y;
	}

	private static int wrap(GuiGraphics gfx, ProgressScreenContext ctx, Component text, int x, int y, int w, int colour) {
		for (FormattedCharSequence line : ctx.font().split(text, w)) {
			gfx.drawString(ctx.font(), line, x, y, colour, false);
			y += 11;
		}
		return y;
	}

	private static String number(double value) {
		return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
	}
}
