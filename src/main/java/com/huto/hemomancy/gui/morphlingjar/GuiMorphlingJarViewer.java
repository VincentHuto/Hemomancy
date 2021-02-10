package com.huto.hemomancy.gui.morphlingjar;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.gui.GuiButtonTextured;
import com.huto.hemomancy.gui.GuiUtil;
import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.PacketUpdateLivingStaffMorph;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@OnlyIn(Dist.CLIENT)
public class GuiMorphlingJarViewer extends Screen {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/scaledjar.png");
	int guiWidth = 91;
	int guiHeight = 130;
	int left, top;
	ItemStack icon;
	Minecraft mc = Minecraft.getInstance();
	PlayerEntity player;
	public MorphlingJarItemHandler handler;

	@OnlyIn(Dist.CLIENT)
	public GuiMorphlingJarViewer(ItemStack currentBinderIn, PlayerEntity playerIn) {
		super(new StringTextComponent("View All Morphs"));
		this.icon = currentBinderIn;
		this.player = playerIn;
	}

	int x;
	int y;
	int xLeftBorder = 8;
	int xRightBorder = 60;
	int topYBorder = +100;
	int bottomYBorder = 200;
	int velX = 2;
	int velY = 2;

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		int maxX = (int) (guiWidth * 0.86);
		int maxY = (int) (guiHeight * 0.86);
		this.renderBackground(matrixStack);

		GlStateManager.pushMatrix();
		{
			GlStateManager.color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
			GuiUtil.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).renderButton(matrixStack, mouseX, mouseY, 511);
			if (buttons.get(i).isHovered()) {
				ItemStack stack = Hemomancy.findMorphlingJar(player);
				@SuppressWarnings("unused")
				IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseThrow(NullPointerException::new);
			}
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		{
			ItemStack stack = Hemomancy.findMorphlingJar(player);
			IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);

			if (binderHandler instanceof MorphlingJarItemHandler) {
				handler = (MorphlingJarItemHandler) binderHandler;
				handler.load();
				slotcount = handler.getSlots();
				itemKey = stack.getTranslationKey();

				for (int i = 0; i < buttons.size(); i++) {
					if (binderHandler.getStackInSlot(i).getItem() instanceof ItemMorphling) {

						GlStateManager.translatef(0, 0, 1);
						RenderHelper.enableStandardItemLighting();
						if (i == 0) {
							long testX = (Math
									.abs(Math.abs(player.world.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.world.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderItemAndEffectIntoGUI(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 1) {
							long testX = (Math
									.abs(Math.abs(player.world.getGameTime() / 3 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.world.getGameTime() / 2 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderItemAndEffectIntoGUI(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 2) {
							long testX = (Math.abs(Math.abs(player.world.getGameTime() % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.world.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderItemAndEffectIntoGUI(binderHandler.getStackInSlot(i), x, y);

						} else if (i == 3) {
							long testX = (Math
									.abs(Math.abs(player.world.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math.abs(Math.abs(player.world.getGameTime() % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderItemAndEffectIntoGUI(binderHandler.getStackInSlot(i), x, y);

						}

					}
					if (buttons.get(i).isHovered()) {
						renderTooltip(matrixStack, binderHandler.getStackInSlot(i).getItem()
								.getDisplayName(binderHandler.getStackInSlot(i)), mouseX, mouseY);
					}
				}
			}
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef(centerX, centerY, 0);
			GlStateManager.translatef(3, 3, 10);
			GlStateManager.scalef(1.9f, 1.7f, 1.9f);
			RenderHelper.enableStandardItemLighting();
			// Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(icon,
			// -1, -1);

		}
		GlStateManager.popMatrix();

	}

	public int slotcount = 0;
	public String itemKey = "";

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttons.clear();
		ItemStack stack = Hemomancy.findMorphlingJar(player);
		IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.orElseThrow(NullPointerException::new);

		if (binderHandler instanceof MorphlingJarItemHandler) {
			handler = (MorphlingJarItemHandler) binderHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getTranslationKey();

			for (int i = 0; i < binderHandler.getSlots(); i++) {
				this.addButton(new GuiButtonTextured(texture, i, 0, 0, 20, 20, 174, 98, null, new IPressable() {
					@Override
					public void onPress(Button press) {
						if (press instanceof GuiButtonTextured) {
							player.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.40f, 1F);
							ItemStack morphStack = binderHandler.getStackInSlot(((GuiButtonTextured) press).getId());
							if (morphStack.getItem() instanceof ItemMorphling) {
								PacketHandler.HANDLER.sendToServer(
										new PacketUpdateLivingStaffMorph(((GuiButtonTextured) press).getId()));
							}

						}
					}
				}));

			}
		}
		super.init();

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
