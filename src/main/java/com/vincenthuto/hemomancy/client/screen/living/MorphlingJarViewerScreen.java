package com.vincenthuto.hemomancy.client.screen.living;

import java.util.List;

//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

@OnlyIn(Dist.CLIENT)
public class MorphlingJarViewerScreen extends Screen {
	public static MorphlingJarViewerScreen screen;
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

	public MorphlingJarViewerScreen() {
		super(Component.literal("View All Morphs"));

	}
	
	public static void openScreenViaItem() {
		openScreen(true);
	}

	public static void openScreen(boolean ignoreNextMouseClick) {
		if (screen == null) {
			screen = new MorphlingJarViewerScreen();
		}
		Minecraft.getInstance().setScreen(screen);
	}

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		renderables.clear();
		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.orElseThrow(NullPointerException::new);

		if (binderHandler instanceof MorphlingJarItemHandler) {
			handler = (MorphlingJarItemHandler) binderHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();

			for (int i = 0; i < binderHandler.getSlots(); i++) {
				this.addRenderableWidget(new HLButtonTextured(texture, i, 0, 0, 20, 20, 174, 98, null, (press) -> {
					if (press instanceof HLButtonTextured) {
						player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
						ItemStack morphStack = binderHandler.getStackInSlot(((HLButtonTextured) press).getId());
						if (morphStack.getItem() instanceof MorphlingItem) {
							PacketHandler.CHANNELMORPHLINGJAR.sendToServer(
									new PacketUpdateLivingStaffMorph(((HLButtonTextured) press).getId()));
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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		int maxX = (int) (guiWidth * 0.86);
		int maxY = (int) (guiHeight * 0.86);
		this.renderBackground(graphics);

		// GlStateManager._pushMatrix();
		{
			// GlStateManager._color4f(1, 1, 1, 1);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int i = (this.width - this.guiWidth) / 2;
			int j = (this.height - this.guiHeight) / 2;
			graphics.blit(texture, i, j, 0, 0, this.guiWidth, this.guiHeight);
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		for (Renderable renderable : renderables) {
			renderable.render(graphics, mouseX, mouseY, 511);
			if (((HLButtonTextured) renderable).isHoveredOrFocused()) {
				ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
				@SuppressWarnings("unused")
				IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
						.orElseThrow(NullPointerException::new);
			}
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		{
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
			IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
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
									.abs(Math.abs(player.level().getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level().getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((HLButtonTextured) renderables.get(i)).posX = x;
							((HLButtonTextured) renderables.get(i)).posY = y;

							graphics.renderItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 1) {
							long testX = (Math
									.abs(Math.abs(player.level().getGameTime() / 3 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level().getGameTime() / 2 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((HLButtonTextured) renderables.get(i)).posX = x;
							((HLButtonTextured) renderables.get(i)).posY = y;
							graphics.renderItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 2) {
							long testX = (Math.abs(Math.abs(player.level().getGameTime() % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level().getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((HLButtonTextured) renderables.get(i)).posX = x;
							((HLButtonTextured) renderables.get(i)).posY = y;
							graphics.renderItem(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 3) {
							long testX = (Math
									.abs(Math.abs(player.level().getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math.abs(Math.abs(player.level().getGameTime() % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							((HLButtonTextured) renderables.get(i)).posX = x;
							((HLButtonTextured) renderables.get(i)).posY = y;
							graphics.renderItem(binderHandler.getStackInSlot(i), x, y);

						}

					}
					if (((HLButtonTextured) renderables.get(i)).isHoveredOrFocused()) {
						graphics.renderComponentTooltip(font, List
								.of(binderHandler.getStackInSlot(i).getItem().getName(binderHandler.getStackInSlot(i))),
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
