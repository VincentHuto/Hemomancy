package com.vincenthuto.hemomancy.capa.player.charm;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.CharmSlotMenu;
import com.vincenthuto.hutoslib.HutosLib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class CharmSlotScreen extends EffectRenderingInventoryScreen<CharmSlotMenu>
		implements RecipeUpdateListener {
	private static final ResourceLocation SCREEN_BACKGROUND = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/charm_slot.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation(
			"textures/gui/recipe_button.png");
	private float oldMouseX;
	private float oldMouseY;
	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean removeRecipeBookGui;
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public CharmSlotScreen(CharmSlotMenu container, Inventory playerInventory, Component title) {
		super(container, playerInventory, title);
		this.passEvents = true;
		this.titleLabelX = 97;
	}

	@Override
	public void containerTick() {
		this.recipeBookComponent.tick();
	}

	@Override
	protected void init() {
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.removeRecipeBookGui = true;
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19,
				RECIPE_BUTTON_TEXTURE, (button) -> {
					this.recipeBookComponent.initVisuals();
					this.recipeBookComponent.toggleVisibility();
					this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
					((ImageButton) button).setPosition(this.leftPos + 104, this.height / 2 - 22);
					this.buttonClicked = true;
				}));
		this.addWidget(this.recipeBookComponent);
		this.setInitialFocus(this.recipeBookComponent);
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
		this.font.draw(matrixStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		this.passEvents = !this.recipeBookComponent.isVisible();
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
			this.recipeBookComponent.render(matrixStack, mouseX, mouseY, partialTicks);
		} else {
			this.recipeBookComponent.render(matrixStack, mouseX, mouseY, partialTicks);
			super.render(matrixStack, mouseX, mouseY, partialTicks);
			this.recipeBookComponent.renderGhostRecipe(matrixStack, this.leftPos, this.topPos, false, partialTicks);
		}

		this.renderTooltip(matrixStack, mouseX, mouseY);
		this.recipeBookComponent.renderTooltip(matrixStack, this.leftPos, this.topPos, mouseX, mouseY);
		this.oldMouseX = (float) mouseX;
		this.oldMouseY = (float) mouseY;
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, SCREEN_BACKGROUND);

		int i = this.leftPos;
		int j = this.topPos;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
		renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX,
				(float) (j + 75 - 50) - this.oldMouseY, this.minecraft.player);
	}

	@Override
	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
		return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible())
				&& super.isHovering(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false
					: super.mouseClicked(mouseX, mouseY, button);
		}
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.buttonClicked) {
			this.buttonClicked = false;
			return true;
		} else {
			return super.mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < (double) guiLeftIn || mouseY < (double) guiTopIn
				|| mouseX >= (double) (guiLeftIn + this.imageWidth) || mouseY >= (double) (guiTopIn + this.imageHeight);
		return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth,
				this.imageHeight, mouseButton) && flag;
	}

	@Override
	protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.slotClicked(slotIn, slotId, mouseButton, type);
		this.recipeBookComponent.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookComponent.recipesUpdated();
	}

	@Override
	public void removed() {
		if (this.removeRecipeBookGui) {
			this.recipeBookComponent.removed();
		}

		super.removed();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}

	@SuppressWarnings("deprecation")
	public static void renderEntityInInventory(int p_98851_, int p_98852_, int p_98853_, float p_98854_, float p_98855_,
			LivingEntity p_98856_) {
		float f = (float) Math.atan((double) (p_98854_ / 40.0F));
		float f1 = (float) Math.atan((double) (p_98855_ / 40.0F));
		PoseStack posestack = RenderSystem.getModelViewStack();
		posestack.pushPose();
		posestack.translate((double) p_98851_, (double) p_98852_, 1050.0D);
		posestack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack posestack1 = new PoseStack();
		posestack1.translate(0.0D, 0.0D, 1000.0D);
		posestack1.scale((float) p_98853_, (float) p_98853_, (float) p_98853_);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
		quaternion.mul(quaternion1);
		posestack1.mulPose(quaternion);
		float f2 = p_98856_.yBodyRot;
		float f3 = p_98856_.getYRot();
		float f4 = p_98856_.getXRot();
		float f5 = p_98856_.yHeadRotO;
		float f6 = p_98856_.yHeadRot;
		p_98856_.yBodyRot = 180.0F + f * 20.0F;
		p_98856_.setYRot(180.0F + f * 40.0F);
		p_98856_.setXRot(-f1 * 20.0F);
		p_98856_.yHeadRot = p_98856_.getYRot();
		p_98856_.yHeadRotO = p_98856_.getYRot();
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		quaternion1.conj();
		entityrenderdispatcher.overrideCameraOrientation(quaternion1);
		entityrenderdispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers()
				.bufferSource();
		RenderSystem.runAsFancy(() -> {
			entityrenderdispatcher.render(p_98856_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1,
					multibuffersource$buffersource, 15728880);
		});
		multibuffersource$buffersource.endBatch();
		entityrenderdispatcher.setRenderShadow(true);
		p_98856_.yBodyRot = f2;
		p_98856_.setYRot(f3);
		p_98856_.setXRot(f4);
		p_98856_.yHeadRotO = f5;
		p_98856_.yHeadRot = f6;
		posestack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}
}