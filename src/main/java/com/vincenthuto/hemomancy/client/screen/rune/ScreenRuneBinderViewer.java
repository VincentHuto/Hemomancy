package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.itemhandler.RuneBinderItemHandler;
import com.vincenthuto.hemomancy.common.registry.ItemInit;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class ScreenRuneBinderViewer extends Screen {
	private static ScreenRuneBinderViewer screen;

	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_binder_gui.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	ItemStack icon = new ItemStack(ItemInit.rune_binder.get());
	Minecraft mc = Minecraft.getInstance();
	Player player = HLClientUtils.getClientPlayer();
	public RuneBinderItemHandler handler;

	@OnlyIn(Dist.CLIENT)
	public ScreenRuneBinderViewer() {
		super(Component.translatable("View All Patterns"));

	}

	public static void openScreenViaItem() {
		openScreen(true);
	}

	public static void openScreen(boolean ignoreNextMouseClick) {
		if (screen == null) {
			screen = new ScreenRuneBinderViewer();
		}
		screen = new ScreenRuneBinderViewer();
		Minecraft.getInstance().setScreen(screen);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(graphics);

		//
		// GlStateManager._color4f(1, 1, 1, 1);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		HLGuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		//

		//
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(graphics, mouseX, mouseY, 511);
			if (((HLButtonTextured) renderables.get(i)).isHoveredOrFocused()) {
				ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
				IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
						.orElseThrow(NullPointerException::new);
				if (binderHandler.getStackInSlot(i).getItem() instanceof ItemRunePattern) {
					ItemRunePattern pat = (ItemRunePattern) binderHandler.getStackInSlot(i).getItem();
					List<Component> text = new ArrayList<Component>();
					text.add(Component
							.translatable(I18n.get(pat.getRecipe().getResultItem().getHoverName().getString())));
					graphics.renderComponentTooltip(font, text, mouseX, mouseY);
				}
			}
		}
		//

		//
		{
			ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
			IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
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
						graphics.renderItem(pat.getRecipe().getResultItem(),
								(((HLButtonTextured) renderables.get(i)).posX + 2),
								((HLButtonTextured) renderables.get(i)).posY + 2);
					}
				}
			}
		}
		//

		//
		{
			// GlStateManager._translatef(centerX, centerY, 0);
			// GlStateManager._translatef(3, 3, 10);
			// GlStateManager._scalef(1.9f, 1.7f, 1.9f);
			Lighting.setupFor3DItems();
			graphics.renderItem(icon, -1, -1);

		}
		//

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
		IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.orElseThrow(NullPointerException::new);

		if (binderHandler instanceof RuneBinderItemHandler) {
			handler = (RuneBinderItemHandler) binderHandler;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();
			for (int i = 0; i < binderHandler.getSlots(); i++) {
				if (i < 9) {
					this.addRenderableWidget(new HLButtonTextured(texture, i, sideLoc - (guiWidth - 18),
							(verticalLoc - 203) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof HLButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((HLButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((HLButtonTextured) press).getId()).getItem();
										// pat.getPatternGui();
										Minecraft.getInstance()
												.setScreen(new ScreenRunePattern(pat.getRune(), pat.getRecipe(),
														I18n.get(Hemomancy.MOD_ID + "."
																+ ForgeRegistries.ITEMS.getKey(pat.getRune().get())
																+ ".pattern.text")));
									}
								}
							}));
				} else if (i < 18) {
					this.addRenderableWidget(new HLButtonTextured(texture, i, sideLoc - (guiWidth - (5 + 55)),
							(verticalLoc - 401) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof HLButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((HLButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((HLButtonTextured) press).getId()).getItem();
										// pat.getPatternGui();
										Minecraft.getInstance()
												.setScreen(new ScreenRunePattern(pat.getRune(), pat.getRecipe(),
														I18n.get(Hemomancy.MOD_ID + "."
																+ ForgeRegistries.ITEMS.getKey(pat.getRune().get())
																+ ".pattern.text")));
									}
								}
							}));
				} else if (i < 27) {
					this.addRenderableWidget(new HLButtonTextured(texture, i, sideLoc - (guiWidth - 115),
							(verticalLoc - 599) + (i * 22), 20, 20, 174, 98, null, (press) -> {
								if (press instanceof HLButtonTextured) {
									player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
									if (binderHandler.getStackInSlot(((HLButtonTextured) press).getId())
											.getItem() instanceof ItemRunePattern) {
										ItemRunePattern pat = (ItemRunePattern) binderHandler
												.getStackInSlot(((HLButtonTextured) press).getId()).getItem();
										// pat.getPatternGui();
										Minecraft.getInstance()
												.setScreen(new ScreenRunePattern(pat.getRune(), pat.getRecipe(),
														I18n.get(Hemomancy.MOD_ID + "."
																+ ForgeRegistries.ITEMS.getKey(pat.getRune().get())
																+ ".pattern.text")));
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

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

}
