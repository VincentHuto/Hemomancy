package com.vincenthuto.hemomancy.gui.living;

//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@OnlyIn(Dist.CLIENT)
public class MorphlingJarViewerScreen extends Screen {
	final ResourceLocation texture = Hemomancy.rloc("textures/gui/scaledjar.png");
	int guiWidth = 91;
	int guiHeight = 130;
	int left, top;
	ItemStack icon = new ItemStack(ItemInit.morphling_jar.get());
	Minecraft mc = Minecraft.getInstance();
	public MorphlingJarItemHandler handler;
	Player player = HLClientUtils.getClientPlayer();

	int x;

	int y;
	int xLeftBorder = 8;
	int xRightBorder = 60;
	int topYBorder = +100;
	int bottomYBorder = 200;
	int velX = 2;
	int velY = 2;
	public int slotcount = 0;

	public String itemKey = "";

	@OnlyIn(Dist.CLIENT)
	public MorphlingJarViewerScreen() {
		super(Component.literal("View All Morphs"));

	}
	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		renderables.clear();
		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.orElseThrow(NullPointerException::new);

		if (binderHandler instanceof MorphlingJarItemHandler) {
			handler = (MorphlingJarItemHandler) binderHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();

			for (int i = 0; i < binderHandler.getSlots(); i++) {
				this.addRenderableWidget(new GuiButtonTextured(texture, i, 0, 0, 20, 20, 174, 98, null, (press) -> {
					if (press instanceof GuiButtonTextured) {
						player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
						ItemStack morphStack = binderHandler.getStackInSlot(((GuiButtonTextured) press).getId());
						if (morphStack.getItem() instanceof MorphlingItem) {
							PacketHandler.CHANNELMORPHLINGJAR.sendToServer(
									new PacketUpdateLivingStaffMorph(((GuiButtonTextured) press).getId()));
						}

					}
					onClose();

				}));

			}
		}
		super.init();

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		int maxX = (int) (guiWidth * 0.86);
		int maxY = (int) (guiHeight * 0.86);
		this.renderBackground(matrixStack);

		// GlStateManager._pushMatrix();
		{
			// GlStateManager._color4f(1, 1, 1, 1);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, texture);
			int i = (this.width - this.guiWidth) / 2;
			int j = (this.height - this.guiHeight) / 2;
			this.blit(matrixStack, i, j, 0, 0, this.guiWidth, this.guiHeight);
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		for (Widget renderable : renderables) {
			renderable.render(matrixStack, mouseX, mouseY, 511);
			if (((GuiButtonTextured) renderable).isHoveredOrFocused()) {
				ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
				@SuppressWarnings("unused")
				IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseThrow(NullPointerException::new);
			}
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		{
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
			IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);

			if (binderHandler instanceof MorphlingJarItemHandler) {
				handler = (MorphlingJarItemHandler) binderHandler;
				handler.load();
				slotcount = handler.getSlots();
				itemKey = stack.getDescriptionId();

				for (int i = 0; i < renderables.size(); i++) {
					if (binderHandler.getStackInSlot(i).getItem() instanceof MorphlingItem) {

						// GlStateManager._translatef(0, 0, 1);
						Lighting.setupFor3DItems();
						if (i == 0) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((GuiButtonTextured) renderables.get(i)).x = x;
							((GuiButtonTextured) renderables.get(i)).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 1) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((GuiButtonTextured) renderables.get(i)).x = x;
							((GuiButtonTextured) renderables.get(i)).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 2) {
							long testX = (Math.abs(Math.abs(player.level.getGameTime() % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((GuiButtonTextured) renderables.get(i)).x = x;
							((GuiButtonTextured) renderables.get(i)).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 3) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math.abs(Math.abs(player.level.getGameTime() % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((GuiButtonTextured) renderables.get(i)).x = x;
							((GuiButtonTextured) renderables.get(i)).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(binderHandler.getStackInSlot(i), x, y);

						}

					}
					if (((GuiButtonTextured) renderables.get(i)).isHoveredOrFocused()) {
						renderTooltip(matrixStack,
								binderHandler.getStackInSlot(i).getItem().getName(binderHandler.getStackInSlot(i)),
								mouseX, mouseY);
					}
				}
			}
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		{
			// GlStateManager._translatef(centerX, centerY, 0);
			// GlStateManager._translatef(3, 3, 10);
			// GlStateManager._scalef(1.9f, 1.7f, 1.9f);
			Lighting.setupFor3DItems();
			// Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(icon,
			// -1, -1);

		}
		// GlStateManager._popMatrix();

	}

}
