package com.vincenthuto.hemomancy.client.screen.radial;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.client.event.ClientEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialMenuScreen extends Screen {
	@SubscribeEvent
	public static void overlayEvent(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;

		if (Minecraft.getInstance().screen instanceof RadialMenuScreen) {
			event.setCanceled(true);
		}
	}

	private ItemStack stackEquipped;

	private IItemHandler inventory;
	private boolean needsRecheckStacks = true;
	private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
	private final TextRadialMenuItem insertMenuItem;

	private final GenericRadialMenu menu;

	public RadialMenuScreen(ItemStack getter) {
		super(Component.literal("RADIAL MENU"));

		this.stackEquipped = getter;
		inventory = stackEquipped.getCount() > 0
				? stackEquipped.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(null)
				: null;
		this.insertMenuItem = null;
		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialMenuScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}
		});
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		processClick(true);
		return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
	}

	protected void processClick(boolean triggeredByMouse) {
		menu.clickItem();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.pose().pushPose();
		super.render(graphics, mouseX, mouseY, partialTicks);
		graphics.pose().popPose();

		if (inventory == null)
			return;

		if (needsRecheckStacks) {
			cachedMenuItems.clear();
			for (int i = 0; i < inventory.getSlots(); i++) {
				ItemStack inSlot = inventory.getStackInSlot(i);
				ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(menu, i, inSlot,
						Component.literal("text.toolbelt.empty")) {
				};
				cachedMenuItems.add(item);
			}

			menu.clear();
			menu.addAll(cachedMenuItems);
			menu.add(insertMenuItem);

			needsRecheckStacks = false;
		}

		boolean hasAddButton = false;
		insertMenuItem.setVisible(hasAddButton);

		if (cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			List<Component> textComponents = new ArrayList<>();
			textComponents.add(Component.literal("text.toolbelt.empty"));
		} else {
			menu.setCentralText(null);
		}

		menu.draw(graphics, partialTicks, mouseX, mouseY);
	}

	@Override // tick
	public void tick() {
		super.tick();

		menu.tick();

		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!menu.isReady() || inventory == null) {
			return;
		}

		if (inventory == null) {
			Minecraft.getInstance().setScreen(null);
		} else if (!ClientEvents.isKeyDown(ClientEvents.OPEN_CHARM_SLOT_KEYBIND)) {
			menu.close();
		}
	}
}
