package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RegistryBackedScarRefactorSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private RegistryBackedScarRefactorSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		scarRegistryOwnsDefinitions();
		itemScarsPointAtDefinitions();
		scarsCapabilityStoresCerebralIdsAndFungalItem();
		scarAndEquipmentTypesStaySeparate();
		fungalImplantationScreenUsesScarSlotCount();
		anastomoticBrazierAndMasonsEffigyAreWired();
		masonsEffigyScreenMatchesConceptLayout();
		masonEffigyMotifPaperPreparesDynamicPattern();
		dynamicScarPatternReplacesPerScarPatternItems();
	}

	private static void scarRegistryOwnsDefinitions() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java");

		assertContains("scar registry key", source, "ResourceKey<Registry<ScarDefinition>>");
		assertContains("scar deferred register", source, "DeferredRegister<ScarDefinition>");
		assertContains("registry helper entries", source, "getAllEntries()");
		assertContains("registry helper lookup", source, "getByName(String name)");
		assertContains("cerebral scar definition", source, "scar_heart");
		assertContains("blood-honed definition", source, "scar_blood_honed");
		assertContains("fungal scar definition", source, "rhizovitta_communis");
	}

	private static void itemScarsPointAtDefinitions() throws IOException {
		String itemScar = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScar.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");

		assertContains("item scar stores scar holder", itemScar, "DeferredHolder<ScarDefinition, ScarDefinition>");
		assertContains("item scar exposes definition", itemScar, "getScarDefinition()");
		assertDoesNotContain("item scar no local modifier builder", itemScar, "ItemScar withModifier");
		assertDoesNotContain("item scar no local effect builder", itemScar, "ItemScar withEffect");
		assertDoesNotContain("item init no constructor-built scar effects", itemInit, "new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency");
		assertContains("item init uses scar registry", itemInit, "ScarInit.scar_heart");
	}

	private static void scarsCapabilityStoresCerebralIdsAndFungalItem() throws IOException {
		String keys = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityKeys.java");
		String attachments = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java");
		String scars = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsContainer.java");

		assertContains("SCARS key stays scar-domain", keys, "EntityCapability<IScars, Void> SCARS");
		assertContains("ITEM_SCAR key stays scar-domain", keys, "ItemCapability<IScarItem, Void> ITEM_SCAR");
		assertContains("SCARS attachment uses ScarsContainer", attachments, "AttachmentType<ScarsContainer>> SCARS");
		assertContains("known cerebral ids are persisted", scars, "knownCerebralScars");
		assertContains("active cerebral ids are persisted", scars, "activeCerebralScars");
		assertContains("fungal item stack is persisted", scars, "fungalScar");
	}

	private static void scarAndEquipmentTypesStaySeparate() throws IOException {
		String scarType = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarType.java");
		String equipmentType = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentType.java");

		assertContains("scar type has cerebral domain", scarType, "CEREBRAL");
		assertContains("scar type has fungal domain", scarType, "FUNGAL");
		assertDoesNotContain("scar type does not mirror equipment override slots", scarType, "OVERRIDE");
		assertDoesNotContain("equipment type does not contain normal scar slots", equipmentType, "OVERRIDE");
	}

	private static void fungalImplantationScreenUsesScarSlotCount() throws IOException {
		String menu = read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/SporeImplantMenu.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SporeImplantScreen.java");

		assertContains("spore implant menu declares scar slot count", menu, "public static final int SLOT_COUNT = 1");
		assertContains("spore implant screen anchors inventory after scar slots", screen,
				"this.menu.slots.get(SporeImplantMenu.SLOT_COUNT)");
		assertContains("spore implant screen only draws scar slot backgrounds", screen,
				"i < SporeImplantMenu.SLOT_COUNT");
		assertDoesNotContain("spore implant screen does not use stale five-slot inventory anchor", screen,
				"this.menu.slots.get(5)");
	}

	private static void anastomoticBrazierAndMasonsEffigyAreWired() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		String blockEntityInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java");
		String containerInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String pattern = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScarPattern.java");
		String brazier = read("src/main/java/com/vincenthuto/hemomancy/common/tile/functional/AnastomoticBrazierBlockEntity.java");
		String menu = read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/MasonsEffigyMenu.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java");

		assertContains("mason effigy block registered", blockInit, "mason_effigy");
		assertContains("anastomotic brazier block registered", blockInit, "anastomotic_brazier");
		assertContains("mason effigy block entity registered", blockEntityInit, "MasonsEffigyBlockEntity");
		assertContains("anastomotic brazier block entity registered", blockEntityInit, "AnastomoticBrazierBlockEntity");
		assertContains("mason effigy menu registered", containerInit, "MenuType<MasonsEffigyMenu>");
		assertContains("mason effigy screen registered", clientEvents, "MasonsEffigyScreen::new");
		assertContains("scar pattern stores selected scar ids", pattern, "TAG_SCAR_IDS");
		assertContains("scar pattern creates prepared loadouts", pattern, "createPreparedPattern");
		assertContains("brazier learns from scar items", brazier, "tryLearnScar");
		assertContains("brazier commits scar pattern loadouts", brazier, "tryCommitLoadout");
		assertContains("brazier clears loadouts from blank motif paper", brazier, "tryClearLoadout");
		assertContains("brazier accepts blank motif paper for clearing", brazier, "ItemInit.runic_motif_paper");
		assertContains("brazier unlocks known scars", brazier, "addKnownCerebralScar");
		assertContains("brazier activates selected scars", brazier, "activateCerebralScar");
		assertContains("brazier deactivates active scars", brazier, "deactivateCerebralScar");
		assertContains("effigy limits prepared loadouts", menu, "MAX_SELECTED_SCARS = 4");
		assertContains("effigy screen renders known scars", screen, "renderScarList");
	}

	private static void masonsEffigyScreenMatchesConceptLayout() throws IOException {
		String menu = read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/MasonsEffigyMenu.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java");

		assertContains("effigy menu exposes inventory origin x", menu, "PLAYER_INV_X = 32");
		assertContains("effigy menu exposes inventory origin y", menu, "PLAYER_INV_Y = 150");
		assertContains("effigy menu carries opening active scar snapshot", menu, "getOpeningActiveScarIds()");
		assertContains("effigy menu marks opening snapshot as authoritative", menu, "hasOpeningSnapshot");
		assertContains("effigy menu uses opening known scars when snapshotted", menu,
				"if (hasOpeningSnapshot) {\n\t\t\treturn openingKnownScarIds;");
		assertContains("effigy menu uses opening active scars when snapshotted", menu,
				"if (hasOpeningSnapshot) {\n\t\t\treturn openingActiveScarIds;");
		assertContains("effigy menu reads opening known scar snapshot before block pos", menu,
				"List<ResourceLocation> known = readIds(data);");
		assertContains("effigy menu reads opening active scar snapshot before block pos", menu,
				"List<ResourceLocation> active = readIds(data);");
		assertContains("effigy menu reads block pos after scar snapshots", menu,
				"return new OpeningData(known, active, data.readBlockPos());");
		assertContains("effigy screen seeds selected sockets from opening snapshot", screen,
				"selected.addAll(menu.getOpeningActiveScarIds())");
		assertContains("effigy screen uses fungal inventory panel texture", screen, "InventoryPanelTextures.FUNGAL");
		assertContains("effigy screen renders player head display", screen, "InventoryScreen.renderEntityInInventoryFollowsMouse");
		assertContains("effigy screen renders venous background", screen, "renderVenousBackground");
		assertContains("effigy screen uses seeded vein params like centrifuge", screen, "veinParams = new float[VEIN_COUNT][9]");
		assertContains("effigy screen draws centrifuge-style tendrils", screen, "drawVeinTendril");
		assertContains("effigy screen resolves scar item stacks", screen, "scarStackFor");
		assertContains("effigy screen renders item stacks", screen, "gfx.renderItem(scarStack");
		assertContains("effigy screen uses thematic icon button", screen, "PrepareIconButton");
		assertContains("effigy prepare icon sits after sockets", screen, "new PrepareIconButton(leftPos + 204");
		assertContains("effigy known scar row slot is aligned", screen, "drawItemSlot(gfx, x, rowY");
		assertContains("effigy known scar icon is aligned", screen, "gfx.renderItem(scarStack, x + 1, rowY + 1");
		assertContains("effigy known scar text is aligned", screen, "x + 26, rowY + 6");
		assertContains("effigy screen renders active socket strip", screen, "renderActiveScarSockets");
		assertContains("effigy screen renders known scars heading", screen, "label.hemomancy.mason_effigy.known_scars");
		assertContains("effigy screen keeps known scar list renderer", screen, "renderScarList");
		assertDoesNotContain("effigy screen no longer draws noisy portrait overlay", screen, "renderVeinOverlay");
		assertDoesNotContain("effigy screen no longer uses symbolic scar glyphs", screen, "drawScarGlyph");
		assertDoesNotContain("effigy screen no longer uses vanilla button builder", screen, "Button.builder");
	}

	private static void masonEffigyMotifPaperPreparesDynamicPattern() throws IOException {
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/MasonsEffigyBlock.java");
		String blockEntity = read("src/main/java/com/vincenthuto/hemomancy/common/tile/functional/MasonsEffigyBlockEntity.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/scars/PacketPrepareScarPattern.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/ScarPatternItemRenderer.java");
		String effigyRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/MasonsEffigyRenderer.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String projection = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");

		assertContains("effigy stores selected scar ids", blockEntity, "selectedScarIds");
		assertContains("effigy exposes selected scar ids", blockEntity, "getSelectedScarIds()");
		assertContains("effigy saves selected scar ids", blockEntity, "TAG_SELECTED_SCARS");
		assertContains("effigy menu writes opening scar snapshot", blockEntity, "writeClientSideData");
		assertContains("effigy menu writes known scar ids before active ids", blockEntity,
				"writeIds(buffer, effigyMenu.getOpeningKnownScarIds())");
		assertContains("effigy menu writes active scar ids", blockEntity,
				"writeIds(buffer, effigyMenu.getOpeningActiveScarIds())");
		assertContains("effigy right click accepts motif paper", block, "ItemInit.runic_motif_paper");
		assertContains("effigy is a blood projection endpoint", block, "implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint");
		assertContains("effigy endpoint forwards projected blood", block, "projectBloodIntoBlock");
		assertContains("effigy endpoint charges pending motif", block, "effigy.receiveProjectedBlood");
		assertContains("effigy lets blood tools start projection", block, "PASS_TO_DEFAULT_BLOCK_INTERACTION");
		assertContains("effigy places motif paper for charging", block, "beginMotifRitual(player, stack)");
		assertContains("effigy stores pending motif", blockEntity, "pendingMotif");
		assertContains("effigy requires five hundred blood per scar", blockEntity, "BLOOD_PER_SCAR = 500.0D");
		assertContains("effigy charges with projected blood", blockEntity, "receiveProjectedBlood");
		assertContains("effigy reports blood charge feedback", blockEntity, "Effigy blood charge:");
		assertContains("effigy completion creates scar pattern", blockEntity, "ItemScarPattern.createPreparedPattern");
		assertContains("effigy completion ejects scar pattern", blockEntity, "level.addFreshEntity(patternEntity)");
		assertContains("effigy completion bursts red particles", blockEntity, "PacketHandler.sendSanguineOmenEffect");
		assertContains("effigy consumes motif paper", blockEntity, "stack.shrink(1)");
		assertContains("blood projection charges effigy", projection, "MasonsEffigyBlockEntity");
		assertContains("blood projection calls effigy ritual charging", projection, "receiveProjectedBlood");
		assertContains("effigy renderer displays pending motif", effigyRenderer, "getPendingMotifDisplayStack");
		assertContains("effigy renderer floats item above block", effigyRenderer, "renderStatic");
		assertContains("effigy renderer registered", clientEvents,
				"BlockEntityRenderers.register(BlockEntityInit.mason_effigy.get(), MasonsEffigyRenderer::new)");
		assertContains("prepare packet writes selected scars to effigy", packet, "effigy.setSelectedScarIds(selected)");
		assertDoesNotContain("prepare packet no longer gives free scar pattern", packet, "player.getInventory().add(pattern)");
		assertDoesNotContain("right click no longer directly creates scar pattern", block, "ItemScarPattern.createPreparedPattern");
		assertContains("scar pattern renderer reads dynamic ids", renderer, "ItemScarPattern.getScarIds(stack)");
		assertContains("scar pattern renderer lays out four quadrants", renderer, "QUADRANT_OFFSETS");
		assertContains("scar pattern renderer renders actual scar item overlays", renderer, "scarStackFor");
		assertContains("scar pattern renderer renders only overlay layer from scar item model", renderer, "quad.getTintIndex() == 1");
		assertContains("scar pattern renderer rotates dynamic overlays around z", renderer, "Axis.ZP.rotationDegrees");
		assertDoesNotContain("scar pattern renderer no longer renders whole scar items", renderer, "renderStatic(scarStack");
	}

	private static void dynamicScarPatternReplacesPerScarPatternItems() throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String pattern = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScarPattern.java");
		String slot = read("src/main/java/com/vincenthuto/hemomancy/common/menu/slot/ScarPatternSlot.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/scar/ScarStationScreen.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/ScarPatternItemRenderer.java");

		assertContains("single scar pattern item remains registered", itemInit, "BASEITEMS.register(\"scar_pattern\"");
		assertDoesNotContain("old heart scar pattern item is removed", itemInit, "scar_pattern_heart");
		assertDoesNotContain("old blood-honed scar pattern item is removed", itemInit, "scar_pattern_blood_honed");
		assertContains("scar pattern can create single-template stacks", pattern, "createTemplatePattern");
		assertContains("scar pattern exposes multiple station entries", pattern, "getTemplateEntries");
		assertDoesNotContain("scar pattern no longer stores a fixed scar holder", pattern, "DeferredHolder<Item, Item>");
		assertDoesNotContain("scar pattern no longer stores a fixed recipe path", pattern, "String path");
		assertContains("scar pattern slot accepts scar patterns", slot, "stack.getItem() instanceof ItemScarPattern");
		assertDoesNotContain("scar pattern slot rejects retired binders", slot, "ItemScarBinder");
		assertContains("scar station rebuilds pattern entries from dynamic item", screen, "ItemScarPattern.getTemplateEntries");
		assertDoesNotContain("scar station no longer scans binders for patterns", screen, "ScarBinderItemHandler");
		assertContains("scar pattern renderer resolves actual scar items", renderer, "BuiltInRegistries.ITEM.get(scarId)");
		assertDoesNotContain("scar pattern renderer no old pattern item lookup", renderer, "scar_pattern_");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
