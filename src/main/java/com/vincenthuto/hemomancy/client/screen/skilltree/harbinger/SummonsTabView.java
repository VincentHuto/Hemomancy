package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public final class SummonsTabView {
	private SummonsTabView() {
	}

	static final int TAB_COLOR = 0xFFBB3355;
	private static final int DETAIL_VALUE_INDENT = 10;
	private static final int DETAIL_ROW_GAP = 2;

	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx,
							SummonsTabState state, int mouseX, int mouseY, float partial) {
		int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
		int contentW = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;
		int contentTop = ctx.guiTop() + 28;
		int contentH = ctx.guiHeight() - 38;

		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);
		drawDegreeSidebar(gfx, ctx, state, mouseX, mouseY);

		if (state.selectedDefinition().isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "Select a bound construct",
					contentX + contentW / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF666666);
			gfx.pose().popPose();
			return;
		}

		PuppeteerSummonDefinition definition = state.selectedDefinition().orElseThrow();
		int previewW = Math.max(150, contentW / 2);
		int detailX = contentX + previewW + 12;
		int detailW = Math.max(100, contentW - previewW - 18);

		drawPreviewPanel(gfx, ctx, state, definition, contentX, contentTop, previewW, contentH, mouseX, mouseY);
		drawDetailPanel(gfx, ctx, state, definition, detailX, contentTop, detailW, contentH);

		gfx.pose().popPose();
	}

	private static void drawDegreeSidebar(GuiGraphics gfx, ProgressScreenContext ctx,
										  SummonsTabState state, int mouseX, int mouseY) {
		int sx = ctx.guiLeft() + 4;
		int sy = ctx.guiTop() + 24;
		int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(ctx.font(), Component.literal("Bound Summons")
				.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF441A24);
		sy += 4;

		int clipTop = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);
		sy -= state.sidebarScroll;

		for (Map.Entry<Integer, List<PuppeteerSummonDefinition>> entry : state.summonsByDegree.entrySet()) {
			int degree = entry.getKey();
			List<PuppeteerSummonDefinition> definitions = entry.getValue();
			boolean locked = ctx.playerDegree() < degree;
			boolean selected = entry.getKey().equals(state.selectedDegree);
			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? 0xDD18080E : (hovered ? 0xBB16060C : 0x990F0508);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);
			int bc = locked ? 0xFF333333 : (selected ? TAB_COLOR : 0xFF66404A);
			ScreenDrawUtils.drawSimpleBorder(gfx, sx, sy, sw, rowH, bc);

			String label = "Degree " + degree + " (" + definitions.size() + ")";
			int labelColor = locked ? 0xFF555555 : (selected ? 0xFFFFAAB8 : 0xFFAA8A92);
			gfx.drawString(ctx.font(), label, sx + 4, sy + (rowH - 8) / 2, labelColor, false);

			if (selected) {
				sy += rowH + 2;
				for (int i = 0; i < definitions.size(); i++) {
					PuppeteerSummonDefinition definition = definitions.get(i);
					boolean known = state.isKnown(definition);
					boolean degreeOk = ctx.playerDegree() >= definition.requiredDegree();
					boolean recipeUnlocked = hasTrialRecipe(definition);
					boolean summonSelected = i == state.selectedSummonIndex;
					boolean summonHovered = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int rowBg = summonSelected ? 0xCC241018 : (summonHovered ? 0xAA1A0A10 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, rowBg);
					if (summonSelected) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, TAB_COLOR);
					}

					int textColor = !degreeOk ? 0xFF444444 : (known ? 0xFFFFCED6 : (recipeUnlocked ? 0xFFB07A86 : 0xFF73545C));
					String name = Component.translatable(definition.translationKey()).getString();
					gfx.drawString(ctx.font(), ScreenDrawUtils.truncateText(ctx.font(), name, sw - 18),
							sx + 8, sy + 4, textColor, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		int contentH = state.sidebarContentH();
		int visibleH = ctx.tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (state.sidebarScroll > 0) {
				gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (state.sidebarScroll < contentH - visibleH) {
				gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private static void drawPreviewPanel(GuiGraphics gfx, ProgressScreenContext ctx, SummonsTabState state,
										 PuppeteerSummonDefinition definition, int x, int y, int w, int h,
										 int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + h, 0x6610080A);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, 0x88441A24);

		String name = Component.translatable(definition.translationKey()).getString();
		gfx.drawCenteredString(ctx.font(), Component.literal(name).withStyle(s -> s.withColor(TAB_COLOR).withBold(true)),
				x + w / 2, y + 8, 0);

		int statusY = y + 22;
		boolean degreeOk = ctx.playerDegree() >= definition.requiredDegree();
		boolean known = state.isKnown(definition);
		boolean recipeUnlocked = hasTrialRecipe(definition);
		Component status = statusComponent(degreeOk, recipeUnlocked, known, definition.requiredDegree());
		gfx.drawCenteredString(ctx.font(), status, x + w / 2, statusY, 0);

		int entityTop = y + 34;
		int entityBottom = y + h - 30;
		if (!degreeOk) {
			gfx.drawCenteredString(ctx.font(), "Rite-shape withheld",
					x + w / 2, y + h / 2 - 4, 0xFF555555);
			ScreenDrawUtils.renderScaledItem(gfx, new ItemStack(ItemInit.sanguine_quintessence.get()),
					x + w / 2, y + h / 2 + 20, 14);
		} else {
			renderPreviewEntity(gfx, ctx, state, definition, x + 12, entityTop, x + w - 12, entityBottom);
		}

		if (isOverPreview(ctx, mouseX, mouseY)) {
			gfx.drawCenteredString(ctx.font(), "Drag to turn",
					x + w / 2, y + h - 18, 0xFF996677);
		}
	}

	private static Component statusComponent(boolean degreeOk, boolean recipeUnlocked, boolean known, int requiredDegree) {
		if (!degreeOk) {
			return Component.literal("Locked: Degree " + requiredDegree).withStyle(ChatFormatting.DARK_GRAY);
		}
		if (known) {
			return Component.literal("Unlocked").withStyle(ChatFormatting.DARK_RED);
		}
		if (!recipeUnlocked) {
			return Component.literal("Recipe locked").withStyle(ChatFormatting.GRAY);
		}
		return Component.literal("Trial required").withStyle(ChatFormatting.GRAY);
	}

	private static boolean hasTrialRecipe(PuppeteerSummonDefinition definition) {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && mc.player.getRecipeBook()
				.contains(Hemomancy.rloc("puppeteer_trial/" + definition.name()));
	}

	private static void renderPreviewEntity(GuiGraphics gfx, ProgressScreenContext ctx, SummonsTabState state,
											PuppeteerSummonDefinition definition,
											int left, int top, int right, int bottom) {
		LivingEntity entity = previewEntity(state, definition);
		if (entity == null) {
			drawPreviewFallback(gfx, ctx, left, top, right, bottom);
			return;
		}

		int centerX = (left + right) / 2;
		int centerY = top + Math.round((bottom - top) * 0.56F);
		int scale = previewEntityScale(entity, bottom - top);
		float yaw = 180.0F + (float) Math.toDegrees(state.previewRotationAngle);
		PreviewRotations rotations = capturePreviewRotations(entity);
		boolean scissorEnabled = false;
		try {
			applyPreviewYaw(entity, yaw);
			gfx.enableScissor(left, top, right, bottom);
			scissorEnabled = true;
			float entityScale = entity.getScale();
			Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
			Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
			InventoryScreen.renderEntityInInventory(gfx, centerX, centerY, scale / entityScale, translate, pose,
					new Quaternionf(), entity);
			gfx.disableScissor();
			scissorEnabled = false;
		} catch (Throwable ignored) {
			if (scissorEnabled) {
				gfx.disableScissor();
			}
			drawPreviewFallback(gfx, ctx, left, top, right, bottom);
		} finally {
			restorePreviewRotations(entity, rotations);
		}
	}

	private static int previewEntityScale(LivingEntity entity, int availableH) {
		float entityH = Math.max(entity.getBbHeight(), 1.0F);
		int fitScale = Mth.floor((availableH - 26) / entityH);
		return Mth.clamp(fitScale, 24, 52);
	}

	private record PreviewRotations(float yBodyRot, float yRot, float xRot, float yHeadRotO, float yHeadRot) {
	}

	private static PreviewRotations capturePreviewRotations(LivingEntity entity) {
		return new PreviewRotations(entity.yBodyRot, entity.getYRot(), entity.getXRot(), entity.yHeadRotO, entity.yHeadRot);
	}

	private static void applyPreviewYaw(LivingEntity entity, float yaw) {
		entity.yBodyRot = yaw;
		entity.setYRot(yaw);
		entity.setXRot(0.0F);
		entity.yHeadRot = yaw;
		entity.yHeadRotO = yaw;
	}

	private static void restorePreviewRotations(LivingEntity entity, PreviewRotations rotations) {
		entity.yBodyRot = rotations.yBodyRot();
		entity.setYRot(rotations.yRot());
		entity.setXRot(rotations.xRot());
		entity.yHeadRotO = rotations.yHeadRotO();
		entity.yHeadRot = rotations.yHeadRot();
	}

	private static LivingEntity previewEntity(SummonsTabState state, PuppeteerSummonDefinition definition) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return null;
		}
		if (state.previewEntity != null && definition.name().equals(state.previewName)
				&& state.previewEntity.level() == mc.level) {
			return state.previewEntity;
		}
		LivingEntity entity = switch (definition.name()) {
			case PuppeteerSummonDefinitions.VEINWING_VULTURE -> EntityInit.veinwing_vulture.get().create(mc.level);
			case PuppeteerSummonDefinitions.MARROW_SPITTER -> EntityInit.marrow_spitter.get().create(mc.level);
			case PuppeteerSummonDefinitions.GOREBOUND_HULK -> EntityInit.gorebound_hulk.get().create(mc.level);
			case PuppeteerSummonDefinitions.MNEMONIST_PUPPET -> EntityInit.mnemonist_puppet.get().create(mc.level);
			default -> null;
		};
		if (entity != null) {
			if (entity instanceof Mob mob) {
				mob.setNoAi(true);
			}
			entity.setYRot(180.0F);
			entity.setXRot(0.0F);
		}
		state.previewName = definition.name();
		state.previewEntity = entity;
		return entity;
	}

	private static void drawPreviewFallback(GuiGraphics gfx, ProgressScreenContext ctx,
											int left, int top, int right, int bottom) {
		int centerX = (left + right) / 2;
		int centerY = (top + bottom) / 2;
		ScreenDrawUtils.renderScaledItem(gfx, new ItemStack(ItemInit.puppeteering_thread.get()), centerX, centerY - 8, 20);
		gfx.drawCenteredString(ctx.font(), "Preview unavailable", centerX, centerY + 18, 0xFF777777);
	}

	private static void drawDetailPanel(GuiGraphics gfx, ProgressScreenContext ctx, SummonsTabState state,
										PuppeteerSummonDefinition definition, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + h, 0x5510080A);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, 0x88441A24);

		int lineH = 12;
		int textX = x + 8;
		int textW = w - 16;
		int drawY = y + 8 - state.infoScroll;
		int clipTop = y + 2;
		int clipBottom = y + h - 2;
		gfx.enableScissor(x + 2, clipTop, x + w - 2, clipBottom);

		drawY = drawWrapped(gfx, ctx, Component.translatable(definition.translationKey())
				.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), textX, drawY, textW, lineH);
		drawY += 4;
		gfx.fill(textX, drawY, textX + textW, drawY + 1, 0xFF441A24);
		drawY += 8;

		boolean degreeOk = ctx.playerDegree() >= definition.requiredDegree();
		boolean known = state.isKnown(definition);
		boolean recipeUnlocked = hasTrialRecipe(definition);
		drawY = drawStackedDetailLine(gfx, ctx, "Status", statusComponent(degreeOk, recipeUnlocked, known, definition.requiredDegree()).getString(), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Role", definition.role(), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Required", "Degree " + definition.requiredDegree(), textX, drawY, textW, lineH);
		drawY += 4;

		int sinew = SkillPointHelper.getLivingSinewLevel();
		double health = definition.baseHealth() * PuppeteerSummonRules.healthMultiplier(sinew);
		double damage = definition.baseDamage() * PuppeteerSummonRules.damageMultiplier(sinew);
		drawY = drawStackedDetailLine(gfx, ctx, "Base Health", String.format("%.1f", definition.baseHealth()), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Current Health", String.format("%.1f", health), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Base Damage", String.format("%.1f", definition.baseDamage()), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Current Damage", String.format("%.1f", damage), textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Mobility", String.format("%.2f", definition.movementSpeed()), textX, drawY, textW, lineH);
		drawY += 4;

		drawY = drawStackedDetailLine(gfx, ctx, "Summon Thread", definition.threadSummonCost() + " charge", textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Upkeep", definition.threadUpkeepPerMinute() + " charge/min", textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Trial Catalyst", Component.translatable("item.hemomancy.sanguine_quintessence").getString(),
				textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Unlock", "Blood Crafting effigy trial", textX, drawY, textW, lineH);
		drawY += 4;

		int skein = SkillPointHelper.getPuppetSkeinLevel();
		int tether = SkillPointHelper.getFarTetherLevel();
		drawY = drawStackedDetailLine(gfx, ctx, "Active Cap", String.valueOf(PuppeteerSummonRules.activeSummonCap(skein)),
				textX, drawY, textW, lineH);
		drawY = drawStackedDetailLine(gfx, ctx, "Command Range", String.format("%.0f blocks", PuppeteerSummonRules.commandRange(tether)),
				textX, drawY, textW, lineH);
		drawY += 6;

		Component lore = Component.translatable(definition.loreKey())
				.copy().withStyle(s -> s.withColor(0xFFAA6677).withItalic(true));
		drawY = drawWrapped(gfx, ctx, lore,
				textX, drawY, textW, lineH);

		gfx.disableScissor();

		if (drawY - y > h && state.infoScroll > 0) {
			gfx.drawCenteredString(ctx.font(), "\u25B2", x + w / 2, y + 2, 0xAAFFFFFF);
		}
	}

	private static int drawStackedDetailLine(GuiGraphics gfx, ProgressScreenContext ctx, String label, String value,
											 int x, int y, int totalW, int lineH) {
		Component labelText = Component.literal(label + ":").withStyle(s -> s.withColor(0xFF888888));
		Component valueText = Component.literal(value).withStyle(s -> s.withColor(0xFFDDC0C7));
		int labelH = drawWrappedValue(gfx, ctx, labelText, x, y, totalW, lineH);
		int valueY = y + labelH;
		int valueW = Math.max(24, totalW - DETAIL_VALUE_INDENT);
		int valueH = drawWrappedValue(gfx, ctx, valueText, x + DETAIL_VALUE_INDENT, valueY, valueW, lineH);
		return valueY + valueH + DETAIL_ROW_GAP;
	}

	private static int drawWrappedValue(GuiGraphics gfx, ProgressScreenContext ctx, Component text,
										int x, int y, int width, int lineH) {
		int startY = y;
		List<String> lines = ScreenDrawUtils.wrapText(ctx.font(), text.getString(), width);
		if (lines.isEmpty()) {
			lines = List.of("");
		}
		for (String line : lines) {
			gfx.drawString(ctx.font(), Component.literal(line).withStyle(text.getStyle()), x, y, 0);
			y += lineH;
		}
		return y - startY;
	}

	private static int drawWrapped(GuiGraphics gfx, ProgressScreenContext ctx, Component text,
								   int x, int y, int width, int lineH) {
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), text.getString(), width)) {
			gfx.drawString(ctx.font(), Component.literal(line).withStyle(text.getStyle()), x, y, 0);
			y += lineH;
		}
		return y;
	}

	public static boolean isOverPreview(ProgressScreenContext ctx, double mx, double my) {
		int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
		int contentW = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;
		int previewW = Math.max(150, contentW / 2);
		int y = ctx.guiTop() + 28;
		int h = ctx.guiHeight() - 38;
		return mx >= contentX && mx <= contentX + previewW && my >= y && my <= y + h;
	}

	public static Integer degreeUnder(ProgressScreenContext ctx, SummonsTabState state, double mx, double my) {
		int sx = ctx.guiLeft() + 4;
		int sy = ctx.guiTop() + 24 + 14 + 4;
		int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) {
			return null;
		}
		sy -= state.sidebarScroll;
		for (Map.Entry<Integer, List<PuppeteerSummonDefinition>> entry : state.summonsByDegree.entrySet()) {
			int degree = entry.getKey();
			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) {
				return degree;
			}
			sy += degreeEntryHeight(entry, state);
		}
		return null;
	}

	private static int degreeEntryHeight(Map.Entry<Integer, List<PuppeteerSummonDefinition>> entry,
										 SummonsTabState state) {
		int rowH = 22;
		int height = rowH + 2;
		if (entry.getKey().equals(state.selectedDegree)) {
			height += entry.getValue().size() * 18 + rowH + 2;
		}
		return height;
	}

	public static int summonUnder(ProgressScreenContext ctx, SummonsTabState state, double mx, double my) {
		if (state.selectedDegree == null) {
			return -1;
		}
		int sx = ctx.guiLeft() + 4;
		int sy = ctx.guiTop() + 24 + 14 + 4;
		int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
		int rowH = 22;
		int clipTop = sy;
		int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
		if (my < clipTop || my > clipBottom) {
			return -1;
		}
		sy -= state.sidebarScroll;
		for (Map.Entry<Integer, List<PuppeteerSummonDefinition>> entry : state.summonsByDegree.entrySet()) {
			sy += rowH + 2;
			if (entry.getKey().equals(state.selectedDegree)) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					if (mx >= sx + 4 && mx <= sx + sw - 4
							&& my >= sy && my <= sy + 16
							&& my >= clipTop && my <= clipBottom) {
						return i;
					}
					sy += 18;
				}
				return -1;
			}
			if (entry.getKey().equals(state.selectedDegree)) {
				sy += entry.getValue().size() * 18;
			}
		}
		return -1;
	}
}
