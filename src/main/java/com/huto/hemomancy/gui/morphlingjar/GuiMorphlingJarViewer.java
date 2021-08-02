package com.huto.hemomancy.gui.morphlingjar;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.PacketUpdateLivingStaffMorph;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
	Player player;
	public MorphlingJarItemHandler handler;

	@OnlyIn(Dist.CLIENT)
	public GuiMorphlingJarViewer(ItemStack currentBinderIn, Player playerIn) {
		super(new TextComponent("View All Morphs"));
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
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		int maxX = (int) (guiWidth * 0.86);
		int maxY = (int) (guiHeight * 0.86);
		this.renderBackground(matrixStack);

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindForSetup(texture);
			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).renderButton(matrixStack, mouseX, mouseY, 511);
			if (buttons.get(i).isHovered()) {
				ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
				@SuppressWarnings("unused")
				IItemHandler bindForSetuperHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseThrow(NullPointerException::new);
			}
		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
			IItemHandler bindForSetuperHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);

			if (bindForSetuperHandler instanceof MorphlingJarItemHandler) {
				handler = (MorphlingJarItemHandler) bindForSetuperHandler;
				handler.load();
				slotcount = handler.getSlots();
				itemKey = stack.getDescriptionId();

				for (int i = 0; i < buttons.size(); i++) {
					if (bindForSetuperHandler.getStackInSlot(i).getItem() instanceof ItemMorphling) {

						//GlStateManager._translatef(0, 0, 1);
						Lighting.turnBackOn();
						if (i == 0) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(bindForSetuperHandler.getStackInSlot(i), x, y);

						} else if (i == 1) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(bindForSetuperHandler.getStackInSlot(i), x, y);

						} else if (i == 2) {
							long testX = (Math.abs(Math.abs(player.level.getGameTime() % (maxX * 2) - maxX) - maxX));
							long testY = (Math
									.abs(Math.abs(player.level.getGameTime() / 3 % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(bindForSetuperHandler.getStackInSlot(i), x, y);

						} else if (i == 3) {
							long testX = (Math
									.abs(Math.abs(player.level.getGameTime() / 2 % (maxX * 2) - maxX) - maxX));
							long testY = (Math.abs(Math.abs(player.level.getGameTime() % (maxY * 2) - maxY) - maxY));
							int x = (int) (centerX + testX);
							int y = (int) (centerY + testY);
							buttons.get(i).x = x;
							buttons.get(i).y = y;
							Minecraft.getInstance().getItemRenderer()
									.renderAndDecorateItem(bindForSetuperHandler.getStackInSlot(i), x, y);

						}

					}
					if (buttons.get(i).isHovered()) {
						renderTooltip(matrixStack,
								bindForSetuperHandler.getStackInSlot(i).getItem().getName(bindForSetuperHandler.getStackInSlot(i)),
								mouseX, mouseY);
					}
				}
			}
		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._translatef(centerX, centerY, 0);
			//GlStateManager._translatef(3, 3, 10);
			//GlStateManager._scalef(1.9f, 1.7f, 1.9f);
			Lighting.turnBackOn();
			// Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(icon,
			// -1, -1);

		}
		//GlStateManager._popMatrix();

	}

	public int slotcount = 0;
	public String itemKey = "";

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttons.clear();
		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		IItemHandler bindForSetuperHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.orElseThrow(NullPointerException::new);

		if (bindForSetuperHandler instanceof MorphlingJarItemHandler) {
			handler = (MorphlingJarItemHandler) bindForSetuperHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();

			for (int i = 0; i < bindForSetuperHandler.getSlots(); i++) {
				this.addButton(new GuiButtonTextured(texture, i, 0, 0, 20, 20, 174, 98, null, new IPressable() {
					@Override
					public void onPress(Button press) {
						if (press instanceof GuiButtonTextured) {
							player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
							ItemStack morphStack = bindForSetuperHandler.getStackInSlot(((GuiButtonTextured) press).getId());
							if (morphStack.getItem() instanceof ItemMorphling) {
								PacketHandler.CHANNELMAIN.sendToServer(
										new PacketUpdateLivingStaffMorph(((GuiButtonTextured) press).getId()));
							}

						}
						onClose();

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
