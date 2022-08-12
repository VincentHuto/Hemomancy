package com.vincenthuto.hemomancy.radial;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.ConfigData;
import com.vincenthuto.hemomancy.item.ItemVasculariumCharm;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.radial.gui.GenericRadialMenu;
import com.vincenthuto.hemomancy.radial.gui.IRadialMenuHost;
import com.vincenthuto.hemomancy.radial.gui.RadialMenuItem;
import com.vincenthuto.hemomancy.radial.gui.TextRadialMenuItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CharmRadialMenuScreen extends Screen {
	private final CharmFinder.CharmGetter getter;
	private ItemStack stackEquipped;
	private IItemHandler inventory;
	private Minecraft mc;

	private boolean keyCycleBeforeL = false;
	private boolean keyCycleBeforeR = false;

	private boolean needsRecheckStacks = true;
	private final List<BlitRadialMenuItem> cachedMenuItems = Lists.newArrayList();
	private final GenericRadialMenu menu;

	private ItemRenderer getItemRenderer() {
		return itemRenderer;
	}

	public CharmRadialMenuScreen(CharmFinder.CharmGetter getter) {
		super(new TextComponent("RADIAL MENU"));

		this.mc = Minecraft.getInstance();

		this.getter = getter;
		this.stackEquipped = getter.getCharm();
		inventory = stackEquipped.getCount() > 0
				? stackEquipped.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(null)
				: null;
		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(PoseStack matrixStack, ItemStack stack, int mouseX, int mouseY) {
				CharmRadialMenuScreen.this.renderTooltip(matrixStack, stack, mouseX, mouseY);
			}

			@Override
			public void renderTooltip(PoseStack matrixStack, Component component, int mouseX, int mouseY) {
				CharmRadialMenuScreen.this.renderTooltip(matrixStack, component, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return CharmRadialMenuScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}

			@Override
			public ItemRenderer getItemRenderer() {
				return CharmRadialMenuScreen.this.getItemRenderer();
			}

		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
	}

	@SubscribeEvent
	public static void overlayEvent(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.LAYER) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof CharmRadialMenuScreen) {
			event.setCanceled(true);
		}
	}

	@Override // removed
	public void removed() {
		super.removed();
		RadialClientEvents.wipeOpen();
	}

	@Override // tick
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			RadialClientEvents.wipeOpen();
		}
		if (!menu.isReady()) {
			return;
		}
		if (!(stackEquipped.getItem() instanceof ItemVasculariumCharm)) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!RadialClientEvents.isKeyDown(RadialClientEvents.radialMenuOpen)) {
			this.processClick(false);
		}
	}

	@Override // mouseReleased
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		processClick(true);
		return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
	}

	protected void processClick(boolean triggeredByMouse) {
		menu.clickItem();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if (this.needsRecheckStacks) {
			this.cachedMenuItems.clear();
			ItemVasculariumCharm.Commands[] values = ItemVasculariumCharm.Commands.values();
			for (int i = 0; i < values.length; ++i) {
				ItemVasculariumCharm.Commands c = values[i];
				final int index = i;

				BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, i, c.getIcon(), 0, 0, 16, 16, 16, 16,
						new TranslatableComponent("mna.construct.command.incidental." + c.toString().toLowerCase())) {
					@Override
					public boolean onClick() {
						CharmRadialMenuScreen.this.menu.close();
						return true;
					}
				};
				item.setVisible(true);
			this.cachedMenuItems.add(item);
			}
			this.menu.clear();
			this.menu.addAll(this.cachedMenuItems);
			this.needsRecheckStacks = false;
		}
		if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			this.menu.setCentralText((Component) new TranslatableComponent("gui.mna.spellbook.empty"));
		} else {
			this.menu.setCentralText(null);
		}
		this.menu.draw(matrixStack, partialTicks, mouseX, mouseY);
	}

	@Override // isPauseScreen
	public boolean isPauseScreen() {
		return false;
	}
}
