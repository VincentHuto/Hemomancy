package com.vincenthuto.hemomancy.client.screen.manips;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.ItemStackRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbility;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryPrimingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ActivateArmorSetAbilityC2SPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UpdateCurrentManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UpdateCurrentMemoryPacket;
import net.minecraft.ChatFormatting;
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

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

@EventBusSubscriber(Dist.CLIENT)
public class RadialChooseManipScreen extends Screen {
	private static final int SELECTED_MANIP_SLICE_TINT = 0x9F7A0D0D;
	private static final int RECHARGING_ABILITY_SLICE_TINT = 0xBFA00000;
	private static final int UNAVAILABLE_ABILITY_SLICE_TINT = 0x7F3F1010;

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
	private IHarbingerEquipmentItemHandler inv;

	private Minecraft mc;
	private boolean needsRecheckStacks = true;
	private final List<RadialMenuItem> cachedMenuItems = Lists.newArrayList();
	private final List<RadialMenuItem> cachedMechanicalItems = Lists.newArrayList();

	private final GenericRadialMenu menu;

	public RadialChooseManipScreen(IHarbingerEquipmentItemHandler invIn) {
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
			MemorySlotRef selectedMemory = manips.getSelectedMemoryRef();
			BloodManipulation selectedManip = allManips.isEmpty() ? null : manips.getSelectedManip();
			String selectedManipName = selectedMemory.kind() == MemoryEntryKind.MANIPULATION
					&& selectedManip != null ? selectedManip.getName() : "";

			addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_ABSORPTION, selectedManipName);
			addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_PROJECTION, selectedManipName);
			addMechanicalManipulation(allManips, ManipulationEquipHelper.CONJURE_SICKLE, selectedManipName);
			addArmorSetAbility();

			for (int i = 0; i < allManips.size(); i++) {
				BloodManipulation c = allManips.get(i);
				if (!equippedNames.contains(c.getName())
						|| ManipulationEquipHelper.isFixedMechanicalManip(c.getName())
						|| ManipulationRetirementRules.isRetiredManipulation(c)) {
					continue;
				}
				this.cachedMenuItems.add(createManipulationItem(c, i, selectedManipName));
			}
			var memoryState = mc.player.getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.MUSCLE_MEMORY);
			EnumSet<EnumVeinSections> shownSections = EnumSet.noneOf(EnumVeinSections.class);
			for (String key : equippedNames) {
				MemorySlotRef ref = MemorySlotRef.fromStorageKey(key);
				ref.muscleMemory().filter(memoryState::knows).filter(memory -> shownSections.add(memory.section()))
						.ifPresent(memory -> this.cachedMenuItems.add(createMuscleMemoryItem(memory, ref, selectedMemory)));
			}
			this.menu.clear();
			this.menu.addAllInner(this.cachedMechanicalItems);
			this.menu.addAll(this.cachedMenuItems);
			this.needsRecheckStacks = false;
		}
		if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)
				&& this.cachedMechanicalItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			this.menu.setCentralText(Component.literal("No Memorized Memories"));
		} else if (gourdEquipped != null) {

			MutableComponent textComponents = Component.empty().copy();
			if (inv != null) {
				IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(inv.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX))
						.orElseThrow(NullPointerException::new);
				IBloodVolume volCap = HemoCapabilityAccess.getBloodVolume(mc.player)
						.orElseThrow(NullPointerException::new);
				textComponents.append(Component.literal("Self: "
						+ BloodGourdItem.formatBloodAmount(volCap.getBloodVolume()) + " mL\n"));
				textComponents.append(Component.literal("Gourd: "
						+ BloodGourdItem.formatBloodAmount(bloodVolume.getBloodVolume()) + " mL"));

			}
			this.menu.setCentralText(textComponents);
		} else {
			this.menu.setCentralText(Component.empty());

		}
		this.menu.draw(graphics, partialTicks, mouseX, mouseY);
	}

	@Override
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

	private void addArmorSetAbility() {
		ArmorSetAbilityRegistry.getActiveAbility(mc.player).ifPresent(ability -> {
			ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(this.menu, -1,
					ability.getDisplayIcon(mc.player), ability.displayName(), () -> armorAbilityTooltip(ability)) {
				@Override
				public boolean onClick() {
					PacketHandler.sendToServer(new ActivateArmorSetAbilityC2SPacket(ability.id()));
					RadialChooseManipScreen.this.menu.close();
					return true;
				}

				@Override
				public int getBackgroundColor(int fallbackColor) {
					return isArmorAbilityRecharging(ability) ? RECHARGING_ABILITY_SLICE_TINT
							: super.getBackgroundColor(fallbackColor);
				}
			};
			if (!ability.canActivate(mc.player)) {
				item.setBackgroundColor(UNAVAILABLE_ABILITY_SLICE_TINT);
			}
			item.setVisible(true);
			this.cachedMechanicalItems.add(item);
		});
	}

	private List<Component> armorAbilityTooltip(ArmorSetAbility ability) {
		List<Component> tooltip = new ArrayList<>(ability.tooltip());
		long cooldownUntil = ArmorSetAbilityRegistry.getClientCooldownUntil(mc.player, ability);
		long now = mc.level != null ? mc.level.getGameTime() : 0L;
		if (cooldownUntil > now) {
			long remainingSeconds = Math.max(1L, (cooldownUntil - now + 19L) / 20L);
			tooltip.add(Component.translatable("ability.hemomancy.armor_set.recharging", remainingSeconds)
					.withStyle(ChatFormatting.RED));
		}
		return tooltip;
	}

	private boolean isArmorAbilityRecharging(ArmorSetAbility ability) {
		long cooldownUntil = ArmorSetAbilityRegistry.getClientCooldownUntil(mc.player, ability);
		long now = mc.level != null ? mc.level.getGameTime() : 0L;
		return cooldownUntil > now;
	}

	private BlitRadialMenuItem createManipulationItem(BloodManipulation manipulation, int slot, String selectedManipName) {
		boolean staffUnavailable = mc.player != null
				&& ActiveRiteClientData.isStaffPlanted(mc.player.getUUID())
				&& ("conjure_staff".equals(manipulation.getName())
						|| LivingStaffWeaponFormRules.isStaffWeaponFormManip(manipulation.getName()));
		BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, slot,
				memoryOverlayTexture(manipulation),
				Hemomancy.rloc("textures/item/memories/memory_blank.png"),
				0, 0, 16, 16, 16, 16,
				Component.literal(manipulation.getProperName()
						+ (staffUnavailable ? " (staff planted in active rite)" : ""))) {
			@Override
			public boolean onClick() {
				if (staffUnavailable) return false;
				PacketHandler.sendToServer(new UpdateCurrentManipPacket(slot));
				RadialChooseManipScreen.this.menu.close();
				return true;
			}
		};
		if (staffUnavailable) item.setBackgroundColor(UNAVAILABLE_ABILITY_SLICE_TINT);
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
			case "conjure_sickle" -> "memory_living_sickle_overlay";
			case "ironhearted" -> "memory_iron_retort_overlay";
			default -> "memory_" + manipulation.getName() + "_overlay";
		};
		return Hemomancy.rloc("textures/item/memories/" + texture + ".png");
	}

	private MuscleMemoryRadialMenuItem createMuscleMemoryItem(MuscleMemory memory, MemorySlotRef ref,
			MemorySlotRef selected) {
		var state = mc.player.getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.MUSCLE_MEMORY);
		java.util.function.Supplier<EnumBloodFlow> flow = () -> HemoCapabilityAccess.getVascularSystem(mc.player)
				.map(vascular -> vascular.getBloodFlowBySection(memory.section())).orElse(EnumBloodFlow.STABLE);
		java.util.function.Supplier<MuscleMemoryRadialPresentation.State> presentation = () -> {
			long now = mc.level != null ? mc.level.getGameTime() : 0L;
			return MuscleMemoryRadialPresentation.resolve(state.reserveTicks(memory), state.isEnabled(memory),
					state.isOverexertionArmed(memory, now), flow.get(), now);
		};
		long seconds = (state.reserveTicks(memory) + 19L) / 20L;
		EnumBloodFlow currentFlow = flow.get();
		String reserve = String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
		String status = currentFlow == EnumBloodFlow.DEAD ? "Blocked: dead vascular section"
				: state.reserveTicks(memory) <= 0 ? "Blocked: reserve empty"
				: state.isEnabled(memory) ? "Active" : "Prepared; activate to use";
		List<Component> tooltip = List.of(
				Component.literal(HLTextUtils.convertInitToLang(memory.section().name()) + " Vascular Section").withStyle(ChatFormatting.RED),
				Component.translatable("muscle_memory.hemomancy." + memory.id()),
				Component.literal("Thelemic Memory"),
				Component.literal("Reserve " + reserve + " • " + trimNumber(memory.bloodCost())
						+ " blood • " + trimNumber(memory.vascularStrain()) + " strain"),
				Component.literal(status));
		MuscleMemoryRadialMenuItem item = new MuscleMemoryRadialMenuItem(this.menu,
				Hemomancy.rloc("textures/item/vascular_status_gauge.png"), tooltip, presentation,
				() -> state.reserveTicks(memory) / (double) MuscleMemoryPrimingRules.TICKS_PER_DOSE,
				memory.section().name().substring(0, 1), () -> {
					PacketHandler.sendToServer(new UpdateCurrentMemoryPacket(ref.storageKey()));
					RadialChooseManipScreen.this.menu.close();
				});
		if (ref.equals(selected)) item.setCentralText(Component.translatable(
				"muscle_memory.hemomancy." + memory.id()).append(Component.literal("\nSelected")));
		item.setVisible(true);
		return item;
	}

	private static String trimNumber(double value) {
		return value == Math.rint(value) ? Integer.toString((int) value)
				: String.format(java.util.Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
	}
}
