package com.vincenthuto.hemomancy.gui.mindrunes;

import java.util.ArrayList;
import java.util.List;

import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.itemhandler.RuneBinderItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
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
public class GuiRuneBinderViewer extends Screen {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_binder_gui.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	ItemStack icon;
	Minecraft mc = Minecraft.getInstance();
	Player player;
	public RuneBinderItemHandler handler;

	@OnlyIn(Dist.CLIENT)
	public GuiRuneBinderViewer(ItemStack currentBinderIn, Player playerIn) {
		super(new TextComponent("View All Patterns"));
		this.icon = currentBinderIn;
		this.player = playerIn;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);

		// GlStateManager._pushMatrix();
		{
			// GlStateManager._color4f(1, 1, 1, 1);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, texture);
			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(matrixStack, mouseX, mouseY, 511);
			// if (((GuiButtonTextured)renderables.get(i)).isHovered()) {
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
			IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);
			if (binderHandler.getStackInSlot(i).getItem() instanceof ItemRunePattern) {
				ItemRunePattern pat = (ItemRunePattern) binderHandler.getStackInSlot(i).getItem();
				List<Component> text = new ArrayList<Component>();
				text.add(new TextComponent(I18n.get(pat.getRecipe().getOutput().getHoverName().getString())));
				renderComponentTooltip(matrixStack, text, mouseX, mouseY);
			}
			// }
		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		{
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
			IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);

			if (binderHandler instanceof RuneBinderItemHandler) {
				handler = (RuneBinderItemHandler) binderHandler;
				handler.load();
				slotcount = handler.getSlots();
				itemKey = stack.getDescriptionId();
				for (int i = 0; i < renderables.size(); i++) {
					// GlStateManager._translatef(0, 0, 1);
					Lighting.setupFor3DItems();
					if (binderHandler.getStackInSlot(i).getItem() instanceof ItemRunePattern) {
						ItemRunePattern pat = (ItemRunePattern) binderHandler.getStackInSlot(i).getItem();
						Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(pat.getRecipe().getOutput(),
								(((GuiButtonTextured) renderables.get(i)).x + 2),
								((GuiButtonTextured) renderables.get(i)).y + 2);
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
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(icon, -1, -1);

		}
		// GlStateManager._popMatrix();

	}

	public int slotcount = 0;
	public String itemKey = "";

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		int sideLoc = left + guiWidth;
		int verticalLoc = top + guiHeight;
		renderables.clear();
		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
		IItemHandler binderHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.orElseThrow(NullPointerException::new);

		if (binderHandler instanceof RuneBinderItemHandler) {
			handler = (RuneBinderItemHandler) binderHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();
			for (int i = 0; i < binderHandler.getSlots(); i++) {
				if (i < 9) {
					this.addRenderableWidget(new GuiButtonTextured(texture, i, sideLoc - (guiWidth - 18),
							(verticalLoc - 203) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((GuiButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((GuiButtonTextured) press).getId()).getItem();
										Minecraft.getInstance().setScreen(pat.getPatternGui());
									}
								}
							}));
				} else if (i < 18) {
					this.addRenderableWidget(new GuiButtonTextured(texture, i, sideLoc - (guiWidth - (5 + 55)),
							(verticalLoc - 401) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((GuiButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((GuiButtonTextured) press).getId()).getItem();
										Minecraft.getInstance().setScreen(pat.getPatternGui());
									}
								}
							}));
				} else if (i < 27) {
					this.addRenderableWidget(new GuiButtonTextured(texture, i, sideLoc - (guiWidth - 115),
							(verticalLoc - 599) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((GuiButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((GuiButtonTextured) press).getId()).getItem();
										Minecraft.getInstance().setScreen(pat.getPatternGui());
									}
								}
							}));
				}
			}
		}
		super.init();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
