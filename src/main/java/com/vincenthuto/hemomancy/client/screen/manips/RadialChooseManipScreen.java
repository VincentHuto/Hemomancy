package com.vincenthuto.hemomancy.client.screen.manips;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UpdateCurrentManipPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class RadialChooseManipScreen extends Screen {
	private static final int SELECTED_MANIP_SLICE_TINT = 0x9F7A0D0D;

	@SubscribeEvent
	public static void overlayEvent(RenderGuiLayerEvent.Pre event) {
		if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR))
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof RadialChooseManipScreen) {
			event.setCanceled(true);
		}
	}

	private ItemStack vascCharmEquipped;
	private BloodGourdItem gourdEquipped;
	private IScarsItemHandler inv;

	private Minecraft mc;
	private boolean needsRecheckStacks = true;
	private final List<RadialMenuItem> cachedMenuItems = Lists.newArrayList();
	private final List<RadialMenuItem> cachedMechanicalItems = Lists.newArrayList();

	private final GenericRadialMenu menu;

	public RadialChooseManipScreen(IScarsItemHandler invIn) {
		super(Component.literal("RADIAL MENU"));
		inv = invIn;
		this.mc = Minecraft.getInstance();

		this.vascCharmEquipped = inv.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX);
		if (inv.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX).getItem() instanceof BloodGourdItem gourd) {
			this.gourdEquipped = gourd;
		}
		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialChooseManipScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}
		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
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
		super.render(graphics, mouseX, mouseY, partialTicks);
		if (this.needsRecheckStacks) {
			this.cachedMenuItems.clear();
			this.cachedMechanicalItems.clear();
			if (mc.player == null) {
				this.menu.setCentralText(Component.empty());
				return;
			}

			IKnownManipulations manips = HemoCapabilityAccess.getKnownManipulations(mc.player)
					.orElseThrow();

			// Only show manipulations that are currently memorized (equipped) at the Mnemonic Reliquary
			List<BloodManipulation> allManips = manips.getManipList();
			List<String> equippedNames = manips.getEquippedManipNames();
			BloodManipulation selectedManip = allManips.isEmpty() ? null : manips.getSelectedManip();
			String selectedManipName = selectedManip != null ? selectedManip.getName() : "";

			addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_ABSORPTION, selectedManipName);
			addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_PROJECTION, selectedManipName);

			for (int i = 0; i < allManips.size(); i++) {
				BloodManipulation c = allManips.get(i);
				if (!equippedNames.contains(c.getName()) || ManipulationEquipHelper.isFixedMechanicalManip(c.getName())) {
					continue;
				}
				this.cachedMenuItems.add(createManipulationItem(c, i, selectedManipName));
			}
			this.menu.clear();
			this.menu.addAllInner(this.cachedMechanicalItems);
			this.menu.addAll(this.cachedMenuItems);
			this.needsRecheckStacks = false;
		}
		if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)
				&& this.cachedMechanicalItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			this.menu.setCentralText(Component.literal("No Memorized Manipulations"));
		} else if (gourdEquipped != null) {

			MutableComponent textComponents = Component.empty().copy();
			if (inv != null) {
				IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(inv.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX))
						.orElseThrow(NullPointerException::new);
				IBloodVolume volCap = HemoCapabilityAccess.getBloodVolume(mc.player)
						.orElseThrow(NullPointerException::new);
				textComponents.append(Component.literal("Self: " + volCap.getBloodVolume() + "ml"));
				textComponents.append(Component.literal("Gourd: " + bloodVolume.getBloodVolume() + "ml"));

			}
			this.menu.setCentralText(textComponents);
		} else {
			this.menu.setCentralText(Component.empty());

		}
		this.menu.draw(graphics, partialTicks, mouseX, mouseY);
	}

	@Override // tick
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!menu.isReady()) {
			return;
		}
		if (!(vascCharmEquipped.getItem() instanceof VasculariumCharmItem)) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!ClientEvents.isKeyDown(ClientEvents.openVascCharmMenu)) {
			this.processClick(false);
		}
	}

	private void addMechanicalManipulation(List<BloodManipulation> allManips, String manipName, String selectedManipName) {
		for (int i = 0; i < allManips.size(); i++) {
			BloodManipulation manipulation = allManips.get(i);
			if (manipulation != null && manipName.equals(manipulation.getName())) {
				this.cachedMechanicalItems.add(createManipulationItem(manipulation, i, selectedManipName));
				return;
			}
		}
	}

	private BlitRadialMenuItem createManipulationItem(BloodManipulation manipulation, int slot, String selectedManipName) {
		BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, slot,
				memoryOverlayTexture(manipulation),
				Hemomancy.rloc("textures/item/memories/memory_blank.png"),
				0, 0, 16, 16, 16, 16,
				Component.literal(manipulation.getProperName())) {
			@Override
			public boolean onClick() {
				PacketHandler.sendToServer(new UpdateCurrentManipPacket(slot));
				RadialChooseManipScreen.this.menu.close();
				return true;
			}
		};
		if (manipulation.getName().equals(selectedManipName)) {
			item.setBackgroundColor(SELECTED_MANIP_SLICE_TINT);
		}
		item.setVisible(true);
		return item;
	}

	private ResourceLocation memoryOverlayTexture(BloodManipulation manipulation) {
		String texture = switch (manipulation.getName()) {
			case "conjure_axe" -> "memory_living_axe_overlay";
			case "conjure_blade" -> "memory_living_blade_overlay";
			case "conjure_claws" -> "memory_living_claws_overlay";
			case "conjure_crossbow" -> "memory_living_crossbow_overlay";
			case "conjure_flail" -> "memory_living_flail_overlay";
			case "conjure_spear" -> "memory_living_spear_overlay";
			case "conjure_staff" -> "memory_living_staff_overlay";
			case "conjure_torch" -> "memory_living_torch_overlay";
			default -> "memory_" + manipulation.getName() + "_overlay";
		};
		return Hemomancy.rloc("textures/item/memories/" + texture + ".png");
	}
}
