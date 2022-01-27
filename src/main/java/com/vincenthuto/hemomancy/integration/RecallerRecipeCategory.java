
package com.vincenthuto.hemomancy.integration;

import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3d;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.RecipeRecaller;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

//GlStateManager;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RecallerRecipeCategory implements IRecipeCategory<RecipeRecaller> {
	public static final ResourceLocation UID = new ResourceLocation(Hemomancy.MOD_ID, "visceral_artificial_recaller");
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable overlay;
	private final IDrawable icon;
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_overlay.png");
	IGuiHelper guiHelper;
	int guiWidth = 170;
	int guiHeight = 100;

	public RecallerRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(170, 100);
		localizedName = I18n.get("hemomancy.jei.recaller");
		overlay = guiHelper.createDrawable(GUI_RECALLER, 0, 0, 170, 100);
		icon = guiHelper.createDrawableIngredient(new ItemStack(BlockInit.visceral_artificial_recaller.get()));
		this.guiHelper = guiHelper;
	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends RecipeRecaller> getRecipeClass() {
		return RecipeRecaller.class;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(RecipeRecaller recipe, IIngredients ingredients) {

		ingredients.setInput(VanillaTypes.ITEM, new ItemStack(ItemInit.hematic_memory.get()));
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
	}

	@Nonnull
	@Override
	public TextComponent getTitle() {
		return new TextComponent(localizedName);
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(RecipeRecaller recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		overlay.draw(matrixStack);
		int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - guiWidth / 2;
		int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - guiHeight / 2;
		drawCenter(matrixStack, recipe.getTendency(), centerX, centerY);
	}

	private int zLevel = 10;

	private void drawCenter(PoseStack stack, Map<EnumBloodTendency, Float> tends, int xOff, int yOff) {
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int iconDiameter = 70;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff + 90;
			int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff + 90;
			int cy1 = (int) (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff + 72;
			int cy2 = (int) (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff + 72;
			double depthDist = ((float) (iconDiameter - diameter) * tends.get(tend) + (float) diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff + 90;
			int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff + 72;
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			fracLine(stack, lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset, this.zLevel,
					tend.getColor(), displace, 1.1);
			fracLine(stack, lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset, this.zLevel,
					tend.getColor(), displace, 1.1);
			fracLine(stack, cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			fracLine(stack, cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			mc.getItemRenderer().renderGuiItem(new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX + xOff + 90,
					newY + yOff + 72);
			rotAngle += 45;
		}
		// GlStateManager._popMatrix();
	}

	public static void fracLine(PoseStack matrix, double src_x, double src_y, double dst_x, double dst_y, int zLevel,
			ParticleColor color, int displace, double detail) {
		if (displace < detail) {
			drawLine(matrix, src_x, src_y, dst_x, dst_y, color, displace);
		} else {
			Random rand = new Random();
			double mid_x = (dst_x + src_x) / 2;
			double mid_y = (dst_y + src_y) / 2;
			mid_x = (int) (mid_x + (rand.nextFloat() - 0.25) * displace * 0.25);
			mid_y = (int) (mid_y + (rand.nextFloat() - 0.25) * displace * 0.25);
			fracLine(matrix, src_x, src_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);
			fracLine(matrix, dst_x, dst_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);

		}
	}

	private static void drawLine(PoseStack stack, double x1, double y1, double x2, double y2, ParticleColor color,
			int displace) {
		// RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._disableTexture();
		GlStateManager._depthMask(false);
		GlStateManager._disableCull();
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		Tesselator var4 = RenderSystem.renderThreadTesselator();
		BufferBuilder var5 = var4.getBuilder();
		RenderSystem.lineWidth(1.0F);
		var5.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		Vector3d vector3f = new Vector3d(x2 - x1, y2 - y1, 0);
		Vector3d vector3f2 = new Vector3d(x1 - x2, y1 - y2, 0);
		int red = (int) color.getRed();
		int green = (int) color.getGreen();
		int blue = (int) color.getBlue();
		var5.vertex(x1, y1, 0.0D).color(red, green, blue, 255).normal((float) vector3f.x, (float) vector3f.y, 0.0F)
				.endVertex();
		var5.vertex(x2, y2, 0.0D).color(red, green, blue, 255).normal((float) vector3f2.x, (float) vector3f2.y, 0.0F)
				.endVertex();
		var4.end();
		GlStateManager._enableCull();
		GlStateManager._depthMask(true);
		GlStateManager._enableTexture();
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RecipeRecaller recipe,
			@Nonnull IIngredients ingredients) {
		int runeIn = 1;
		int outputRune = 3;
		recipeLayout.getItemStacks().init(runeIn, true, 4, 10);
		recipeLayout.getItemStacks().set(runeIn, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		recipeLayout.getItemStacks().init(outputRune, false, 148, 76);
		recipeLayout.getItemStacks().set(outputRune, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

	}

	Minecraft mc = Minecraft.getInstance();

}