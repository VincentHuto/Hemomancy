package com.vincenthuto.hemomancy.compat.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.CraftingMapRecipe;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern.MaterialCount;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import com.vincenthuto.hutoslib.math.Quaternion;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI recipe category for structure crafts — fully programmatic rendering.
 * Reads left to right: what anchors the craft (the Cardinal Focus and its
 * seated medium, or a floorless structure's activate block and catalyst) and
 * the blocks to lay, a rotating preview captioned with the floor it needs,
 * then the result and the brazier offerings. A footer gives the blood drawn;
 * hovering the title or preview shows the rite's description and steps.
 * One instance serves floorless Blood Structures, and one each the Harbinger rites of
 * every purpose: Threshold, Dominion, Working (crafts included) and Communion.
 */
public class BloodStructureRecipeCategory implements IRecipeCategory<CraftingMapRecipe> {

	// ── Layout constants ──
	private static final int BG_W = 164;
	private static final int BG_H = 142;

	private static final int TITLE_Y = 4;
	private static final int DIVIDER_Y = 14;
	private static final int HEADER_Y = 18;
	private static final int TOP_SLOT_Y = 27;
	private static final int GRID_HEADER_Y = 49;
	private static final int GRID_SLOT_Y = 58;
	private static final int SLOT_SPACING = 20;
	/** Block materials and offerings each fill a two-wide, three-tall grid. */
	private static final int MAX_GRID_SLOTS = 6;

	private static final int LEFT_X = 5;
	private static final int RIGHT_X = 124;
	private static final int OUTPUT_SLOT_X = 134;

	// Preview area (3D multiblock render)
	private static final int PREVIEW_X = 47;
	private static final int PREVIEW_Y = 18;
	private static final int PREVIEW_W = 72;
	private static final int PREVIEW_H = 98;
	private static final int CAPTION_Y = 121;
	private static final int FOOTER_Y = 131;

	// Colors
	private static final JeiPanel.Theme THEME = JeiPanel.BLOOD;
	private static final int PREVIEW_BG = 0xFF1A0808;
	private static final int TITLE_COLOR = 0xFFE8D0C8;
	private static final int COST_COLOR = 0xFFCC2222;
	private static final int LABEL_COLOR = 0xFF9A7A74;
	private static final int CAPTION_COLOR = 0xFFB06A60;

	private final IDrawable icon;
	/** The purpose whose rites this category lists, or null for the floorless Blood Structures. */
	private final CardinalRitePurpose purpose;

	public BloodStructureRecipeCategory(IGuiHelper guiHelper) {
		this(guiHelper, null);
	}

	public BloodStructureRecipeCategory(IGuiHelper guiHelper, CardinalRitePurpose purpose) {
		this.purpose = purpose;
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconFor(purpose));
	}

	/** Each rite category shows the block its floor style is studded with. */
	private static ItemStack iconFor(CardinalRitePurpose purpose) {
		if (purpose == null) return new ItemStack(ItemInit.sanguine_formation.get());
		return new ItemStack(switch (purpose) {
			case SELF -> BlockInit.hematic_iron_block.get();
			case WORLD -> BlockInit.chiseled_hematic_iron_block.get();
			case MAKING -> Blocks.BONE_BLOCK;
			case BOND -> BlockInit.blood_wood_planks.get();
		});
	}

	// ═══════════════════════════════════════════════════════════════
	//  IRecipeCategory overrides
	// ═══════════════════════════════════════════════════════════════

	@Override
	public int getWidth() {
		return BG_W;
	}

	@Override
	public int getHeight() {
		return BG_H;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<CraftingMapRecipe> getRecipeType() {
		return purpose == null ? JEIPlugin.blood_structure_recipe_type : JEIPlugin.riteTypeFor(purpose);
	}

	@Nonnull
	@Override
	public Component getTitle() {
		if (purpose == null) return Component.translatable("hemomancy.jei.blood_crafting");
		return Component.translatable(switch (purpose) {
			case SELF -> "hemomancy.jei.threshold_rite";
			case WORLD -> "hemomancy.jei.dominion_rite";
			case MAKING -> "hemomancy.jei.working_craft";
			case BOND -> "hemomancy.jei.communion_rite";
		});
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing
	// ═══════════════════════════════════════════════════════════════

	@Override
	public void draw(CraftingMapRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {

		float time = JeiPanel.time();
		Font font = Minecraft.getInstance().font;

		// ── Panel, with the floor caption and blood in the footer band ──
		JeiPanel.panel(gfx, THEME, BG_W, BG_H, CAPTION_Y - 4);
		JeiPanel.glow(gfx, PREVIEW_X + PREVIEW_W / 2, PREVIEW_Y + PREVIEW_H / 2, 50, 56, 0xFF60080A,
				0.5f + 0.5f * JeiPanel.pulse(time, 1.8f));

		// ── Title row: name and degree gate ──
		int titleRight = BG_W - 5;
		if (recipe.requiredDegree() > 0) {
			Component degree = Component.translatable("hemomancy.jei.craft.degree", recipe.requiredDegree());
			titleRight -= font.width(degree);
			gfx.drawString(font, degree, titleRight, TITLE_Y, COST_COLOR, false);
			titleRight -= 6;
		}
		drawTrimmed(gfx, font, recipe.name(), LEFT_X, TITLE_Y, titleRight - LEFT_X, TITLE_COLOR);
		gfx.fill(4, DIVIDER_Y, BG_W - 4, DIVIDER_Y + 1, THEME.ring());

		// ── Left column: the anchor, then the blocks to lay ──
		JeiPanel.labelLeft(gfx, font, Component.translatable(recipe.isWorking()
				? "hemomancy.jei.craft.focus" : "hemomancy.jei.craft.activate"), LEFT_X, HEADER_Y + 2, LABEL_COLOR);
		if (recipe.isWorking() || recipe.getHitBlock() != null) {
			drawSlotFrame(gfx, LEFT_X - 1, TOP_SLOT_Y - 1);
		}
		if (!recipe.getHeldItem().isEmpty()) {
			drawSlotFrame(gfx, LEFT_X + SLOT_SPACING - 1, TOP_SLOT_Y - 1);
		}
		int materials = materialSlots(recipe).size();
		if (materials > 0) {
			JeiPanel.labelLeft(gfx, font, Component.translatable(recipe.showsStructure()
					? "hemomancy.jei.craft.build" : "hemomancy.jei.craft.floor"), LEFT_X, GRID_HEADER_Y + 2, LABEL_COLOR);
			for (int i = 0; i < materials; i++) {
				drawSlotFrame(gfx, gridX(LEFT_X, i) - 1, gridY(i) - 1);
			}
		}

		// ── Preview ──
		JeiPanel.bevel(gfx, PREVIEW_X - 1, PREVIEW_Y - 1, PREVIEW_W + 2, PREVIEW_H + 2, THEME.slotDark(), THEME.slotLight());
		render3DPreview(recipe, gfx);

		// ── Right column: the result, then the brazier offerings ──
		JeiPanel.labelLeft(gfx, font, Component.translatable("hemomancy.jei.craft.result"), RIGHT_X, HEADER_Y + 2, LABEL_COLOR);
		JeiPanel.tintedSlot(gfx, THEME, OUTPUT_SLOT_X, TOP_SLOT_Y, THEME.accent());
		JeiProgressArrow.draw(gfx, PREVIEW_X + PREVIEW_W + 2, TOP_SLOT_Y + 7, OUTPUT_SLOT_X - 4, time, THEME);
		int offerings = Math.min(MAX_GRID_SLOTS, recipe.getOfferings().size());
		if (offerings > 0) {
			JeiPanel.labelLeft(gfx, font, Component.translatable("hemomancy.jei.craft.braziers"), RIGHT_X, GRID_HEADER_Y + 2,
					LABEL_COLOR);
			for (int i = 0; i < offerings; i++) {
				drawSlotFrame(gfx, gridX(RIGHT_X, i) - 1, gridY(i) - 1);
			}
		}

		// ── Footer: the floor it is performed on, and the blood drawn ──
		Component caption = caption(recipe);
		int captionW = Math.min(font.width(caption), BG_W - 10);
		drawTrimmed(gfx, font, caption, (BG_W - captionW) / 2, CAPTION_Y, BG_W - 10, CAPTION_COLOR);
		Component cost = costLine(recipe);
		int costX = (BG_W - font.width(cost) + 8) / 2;
		JeiPanel.icon(gfx, JeiPanel.Icon.BLOOD, costX - 8, FOOTER_Y, COST_COLOR);
		gfx.drawString(font, cost, costX, FOOTER_Y, COST_COLOR, false);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, CraftingMapRecipe recipe, IRecipeSlotsView recipeSlotsView,
			double mouseX, double mouseY) {
		boolean overTitle = mouseY >= TITLE_Y - 1 && mouseY < DIVIDER_Y;
		boolean overPreview = mouseX >= PREVIEW_X && mouseX < PREVIEW_X + PREVIEW_W
				&& mouseY >= PREVIEW_Y && mouseY < PREVIEW_Y + PREVIEW_H;
		if (!overTitle && !overPreview) {
			return;
		}
		tooltip.add(recipe.name().copy().withStyle(ChatFormatting.RED));
		if (recipe.description() != null && !recipe.description().getString().isBlank()) {
			Font font = Minecraft.getInstance().font;
			for (FormattedText line : font.getSplitter().splitLines(recipe.description(), 180, Style.EMPTY)) {
				tooltip.add(Component.literal(line.getString()).withStyle(ChatFormatting.GRAY));
			}
		}
		if (recipe.requiredDegree() > 0) {
			tooltip.add(Component.translatable("hemomancy.jei.craft.degree_full", recipe.requiredDegree())
					.withStyle(ChatFormatting.DARK_RED));
		}
		tooltip.add(caption(recipe).copy().withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(Component.translatable(steps(recipe)).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CraftingMapRecipe recipe, IFocusGroup focuses) {
		// Anchor: the Cardinal Focus and its medium, or the activate block and held catalyst
		if (recipe.isWorking()) {
			builder.addSlot(RecipeIngredientRole.CATALYST, LEFT_X, TOP_SLOT_Y)
					.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.cardinal_focus.get()))
					.addRichTooltipCallback((view, tip) -> tip.add(hint("hemomancy.jei.craft.tip.focus")));
		} else if (recipe.getHitBlock() != null) {
			builder.addSlot(RecipeIngredientRole.CATALYST, LEFT_X, TOP_SLOT_Y)
					.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.getHitBlock().asItem()))
					.addRichTooltipCallback((view, tip) -> tip.add(hint("hemomancy.jei.craft.tip.activate")));
		}
		if (!recipe.getHeldItem().isEmpty()) {
			String heldHint = recipe.isWorking() ? "hemomancy.jei.craft.tip.medium" : "hemomancy.jei.craft.tip.held";
			builder.addSlot(RecipeIngredientRole.INPUT, LEFT_X + SLOT_SPACING, TOP_SLOT_Y)
					.addIngredient(VanillaTypes.ITEM_STACK, recipe.getHeldItem())
					.addRichTooltipCallback((view, tip) -> tip.add(hint(heldHint)));
		}

		// Blocks to lay: the structure or subject, or a making rite's floor
		String materialHint = !recipe.isWorking() ? "hemomancy.jei.craft.tip.structure"
				: recipe.hasStructure() ? "hemomancy.jei.craft.tip.subject"
				: recipe.showsStructure() ? "hemomancy.jei.craft.tip.structure" : "hemomancy.jei.craft.tip.floor";
		List<List<ItemStack>> materials = materialSlots(recipe);
		for (int i = 0; i < materials.size(); i++) {
			builder.addSlot(recipe.showsStructure() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST,
					gridX(LEFT_X, i), gridY(i))
					.addIngredients(VanillaTypes.ITEM_STACK, materials.get(i))
					.addRichTooltipCallback((view, tip) -> tip.add(hint(materialHint)));
		}

		if (!recipe.getResult().isEmpty()) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, TOP_SLOT_Y)
					.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResult());
		}

		for (int i = 0; i < Math.min(MAX_GRID_SLOTS, recipe.getOfferings().size()); i++) {
			List<ItemStack> stacks = ingredientStacksFor(recipe.getOfferings().get(i));
			if (!stacks.isEmpty()) {
				builder.addSlot(RecipeIngredientRole.INPUT, gridX(RIGHT_X, i), gridY(i))
						.addIngredients(VanillaTypes.ITEM_STACK, stacks)
						.addRichTooltipCallback((view, tip) -> tip.add(hint("hemomancy.jei.craft.tip.offering")));
			}
		}
	}

	// ═══════════════════════════════════════════════════════════════
	//  Text
	// ═══════════════════════════════════════════════════════════════

	/** The floor a Working craft is performed on, or a floorless structure's note. */
	private static Component caption(CraftingMapRecipe recipe) {
		if (!recipe.isWorking()) {
			return Component.translatable("hemomancy.jei.craft.floorless");
		}
		return CardinalRiteFloorRegistry.get(recipe.floorId())
				.<Component>map(floor -> Component.literal(floor.displayName()))
				.orElse(Component.translatable("hemomancy.jei.craft.floor"));
	}

	private static Component costLine(CraftingMapRecipe recipe) {
		int feed = (int) recipe.getBloodCost();
		int rite = (int) recipe.riteCost();
		if (!recipe.isWorking()) {
			return Component.translatable("hemomancy.jei.craft.blood", feed);
		}
		if (!recipe.hasStructure()) {
			return rite > 0 ? Component.translatable("hemomancy.jei.craft.rite", rite)
					: Component.translatable("hemomancy.jei.craft.rite_free");
		}
		return rite > 0 ? Component.translatable("hemomancy.jei.craft.feed_and_rite", feed, rite)
				: Component.translatable("hemomancy.jei.craft.feed", feed);
	}

	private String steps(CraftingMapRecipe recipe) {
		if (!recipe.isWorking()) {
			return "hemomancy.jei.craft.steps.floorless";
		}
		if (recipe.hasStructure()) return "hemomancy.jei.craft.steps.working";
		return purpose == null || purpose == CardinalRitePurpose.MAKING ? "hemomancy.jei.craft.steps.making"
				: "hemomancy.jei.craft.steps." + purpose.id();
	}

	private static Component hint(String key) {
		return Component.translatable(key).withStyle(ChatFormatting.DARK_RED);
	}

	private static void drawTrimmed(GuiGraphics gfx, Font font, Component text, int x, int y, int maxWidth,
			int color) {
		if (font.width(text) <= maxWidth) {
			gfx.drawString(font, text, x, y, color, false);
			return;
		}
		String trimmed = font.plainSubstrByWidth(text.getString(), maxWidth - font.width("..")) + "..";
		gfx.drawString(font, trimmed, x, y, color, false);
	}

	// ═══════════════════════════════════════════════════════════════
	//  3D multiblock preview
	// ═══════════════════════════════════════════════════════════════

	private void render3DPreview(CraftingMapRecipe recipe, GuiGraphics gfx) {
		if (recipe.getPattern() == null) return;

		try {
			gfx.fill(PREVIEW_X, PREVIEW_Y, PREVIEW_X + PREVIEW_W, PREVIEW_Y + PREVIEW_H, PREVIEW_BG);

			MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
			double scale = Minecraft.getInstance().getWindow().getGuiScale();
			Matrix4f matrix = gfx.pose().last().pose();
			FloatBuffer buf = BufferUtils.createFloatBuffer(16);
			matrix.get(buf);
			Lighting.setupLevel();

			Vec3 translation = new Vec3(buf.get(12) * scale, buf.get(13) * scale, buf.get(14) * scale);

			// Scissor rect in screen-space pixels
			int scaledX = (int) (PREVIEW_X * scale);
			int scaledY = (int) (PREVIEW_Y * scale);
			int scaledW = (int) (PREVIEW_W * scale);
			int scaledH = (int) (PREVIEW_H * scale);
			int scissorX = Math.round(Math.round(translation.x + scaledX));
			int scissorY = Math.round(Math.round(
					Minecraft.getInstance().getWindow().getHeight() - scaledY - scaledH - translation.y));

			RenderSystem.enableScissor(scissorX, scissorY, Math.round(scaledW), Math.round(scaledH));

			gfx.pose().pushPose();
			gfx.pose().translate(
					PREVIEW_X + PREVIEW_W / 2.0,
					PREVIEW_Y + PREVIEW_H / 2.0,
					100);

			// Scale based on structure dimensions
			int patW = recipe.getPattern().getBlockPattern().getWidth();
			int patH = recipe.getPattern().getBlockPattern().getHeight();
			int patD = recipe.getPattern().getBlockPattern().getDepth();
			float avgDim = (float) Math.sqrt(patW * patW + patH * patH + patD * patD);
			// Fit the exploded structure (1.5 block spacing) to most of the preview box.
			float previewScale = Mth.clamp(Math.min(PREVIEW_W, PREVIEW_H) * 0.7f / (avgDim * 1.5f), 4f, 22f);
			gfx.pose().scale(previewScale, -previewScale, previewScale);

			drawRotatingMultiblock(recipe, gfx, buffers);

			gfx.pose().popPose();
			buffers.endBatch();
			RenderSystem.disableScissor();
		} catch (RuntimeException ex) {
			Hemomancy.LOGGER.warn("Error rendering blood structure preview", ex);
		}
	}

	private void drawRotatingMultiblock(CraftingMapRecipe recipe, GuiGraphics gfx,
			MultiBufferSource.BufferSource buffers) {

		double rotation = Math.toDegrees(HLClientUtils.getWorld().getGameTime()) / 15.0;
		gfx.pose().mulPose(new Quaternion(35f, (float) rotation, 0, true).toMoj());
		gfx.pose().mulPose(new Quaternion(0, 35f, 0, true).toMoj());

		int patW = recipe.getPattern().getBlockPattern().getWidth();
		int patH = recipe.getPattern().getBlockPattern().getHeight();
		int patD = recipe.getPattern().getBlockPattern().getDepth();
		double explodeSpacing = 1.5;

		for (BlockPosBlockPair pair : recipe.getPattern().getDisplayBlockPosBlockList(materialCycleIndex())) {
			if (pair.getPos().getY() >= patH) continue;
			if (pair.getBlock() == null || pair.getBlock() == Blocks.AIR) continue;

			gfx.pose().pushPose();
			gfx.pose().translate(
					explodeSpacing * (pair.getPos().getX() - patW / 2) - 0.5,
					explodeSpacing * (pair.getPos().getY() - patH / 2) - 0.5,
					explodeSpacing * (pair.getPos().getZ() - patD / 2) - 0.5);

			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
					pair.getBlock().defaultBlockState(), gfx.pose(), buffers,
					15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
					RenderType.cutoutMipped());
			gfx.pose().popPose();
		}
	}

	// ═══════════════════════════════════════════════════════════════
	//  Drawing helpers
	// ═══════════════════════════════════════════════════════════════

	private static int gridX(int columnX, int index) {
		return columnX + (index % 2) * SLOT_SPACING;
	}

	private static int gridY(int index) {
		return GRID_SLOT_Y + (index / 2) * SLOT_SPACING;
	}

	/** One slot per distinct block of the pattern, carrying its count; any overflow cycles in the last slot. */
	private static List<List<ItemStack>> materialSlots(CraftingMapRecipe recipe) {
		List<List<ItemStack>> slots = new ArrayList<>();
		if (recipe.getPattern() == null || recipe.getPattern().getSymbolList() == null) {
			return slots;
		}
		for (MaterialCount material : recipe.getPattern().getMaterialCounts(false)) {
			List<ItemStack> stacks = itemStacksFor(material.key(), material.count());
			if (stacks.isEmpty()) {
				continue;
			}
			if (slots.size() < MAX_GRID_SLOTS) {
				slots.add(new ArrayList<>(stacks));
			} else {
				slots.get(MAX_GRID_SLOTS - 1).addAll(stacks);
			}
		}
		return slots;
	}

	private long materialCycleIndex() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			return minecraft.level.getGameTime() / 40L;
		}
		return System.currentTimeMillis() / 2000L;
	}

	private static List<ItemStack> itemStacksFor(MultiblockPatternKey key, int count) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Block block : key.displayBlocks()) {
			if (block == null || block == Blocks.AIR) {
				continue;
			}
			ItemStack stack = new ItemStack(block.asItem(), count);
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		return stacks;
	}

	private static List<ItemStack> ingredientStacksFor(CraftingMapRecipe.Offering offering) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : offering.ingredient().getItems()) {
			if (!stack.isEmpty()) {
				stacks.add(stack.copyWithCount(offering.count()));
			}
		}
		return stacks;
	}

	/** Draws an 18x18 item slot frame whose top-left is (x, y). */
	private void drawSlotFrame(GuiGraphics gfx, int x, int y) {
		JeiPanel.slot(gfx, THEME, x + 1, y + 1);
	}

}
