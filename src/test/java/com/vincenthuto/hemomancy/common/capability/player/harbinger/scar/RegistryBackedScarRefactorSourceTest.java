package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class RegistryBackedScarRefactorSourceTest {
	@Test
	void registryBackedScarRefactorSourceContracts() {
		scarRegistryOwnsDefinitions();
		itemScarsPointAtDefinitions();
		scarsCapabilityStoresCerebralIdsAndFungalItem();
		scarAndEquipmentTypesStaySeparate();
		fungalImplantationScreenUsesScarSlotCount();
		ironBrazierAndMasonsEffigyAreWired();
		masonsEffigyScreenMatchesConceptLayout();
		masonEffigyMotifPaperPreparesDynamicPattern();
		dynamicScarPatternReplacesPerScarPatternItems();
	}

	private static void scarRegistryOwnsDefinitions() {
		String source = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java");

		assertContains("scar registry key", source, "ResourceKey<Registry<ScarDefinition>>");
		assertContains("scar deferred register", source, "DeferredRegister<ScarDefinition>");
		assertContains("registry helper lookup", source, "getByName(String name)");
		assertContains("cerebral scar definition", source, "scar_heart");
		assertContains("blood-honed definition", source, "scar_blood_honed");
		assertContains("fungal scar definition", source, "rhizovitta_communis");
	}

	private static void itemScarsPointAtDefinitions() {
		String itemScar = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScar.java");
		String itemInit = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");

		assertContains("item scar stores scar holder", itemScar, "DeferredHolder<ScarDefinition, ScarDefinition>");
		assertContains("item scar exposes definition", itemScar, "getScarDefinition()");
		assertNotContains("item scar no local modifier builder", itemScar, "ItemScar withModifier");
		assertNotContains("item scar no local effect builder", itemScar, "ItemScar withEffect");
		assertNotContains("item init no constructor-built scar effects", itemInit, "new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency");
		assertContains("item init uses scar registry", itemInit, "ScarInit.scar_heart");
	}

	private static void scarsCapabilityStoresCerebralIdsAndFungalItem() {
		String keys = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityKeys.java");
		String attachments = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java");
		String scars = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsContainer.java");

		assertContains("SCARS key stays scar-domain", keys, "EntityCapability<IScars, Void> SCARS");
		assertContains("ITEM_SCAR key stays scar-domain", keys, "ItemCapability<IScarItem, Void> ITEM_SCAR");
		assertContains("SCARS attachment uses ScarsContainer", attachments, "AttachmentType<ScarsContainer>> SCARS");
		assertContains("known cerebral ids are persisted", scars, "knownCerebralScars");
		assertContains("active cerebral ids are persisted", scars, "activeCerebralScars");
		assertContains("fungal item stack is persisted", scars, "fungalScar");
	}

	private static void scarAndEquipmentTypesStaySeparate() {
		String scarType = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarType.java");
		String equipmentType = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentType.java");

		assertContains("scar type has cerebral domain", scarType, "CEREBRAL");
		assertContains("scar type has fungal domain", scarType, "FUNGAL");
		assertNotContains("scar type does not mirror equipment override slots", scarType, "OVERRIDE");
		assertNotContains("equipment type does not contain normal scar slots", equipmentType, "OVERRIDE");
	}

	private static void fungalImplantationScreenUsesScarSlotCount() {
		String menu = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/SporeImplantMenu.java");
		String screen = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SporeImplantScreen.java");

		assertContains("spore implant menu declares scar slot count", menu, "public static final int SLOT_COUNT = 1");
		assertContains("spore implant screen anchors inventory after scar slots", screen,
				"this.menu.slots.get(SporeImplantMenu.SLOT_COUNT)");
		assertContains("spore implant screen only draws scar slot backgrounds", screen,
				"i < SporeImplantMenu.SLOT_COUNT");
		assertNotContains("spore implant screen does not use stale five-slot inventory anchor", screen,
				"this.menu.slots.get(5)");
	}

	private static void ironBrazierAndMasonsEffigyAreWired() {
		String blockInit = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		String blockEntityInit = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java");
		String containerInit = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java");
		String clientEvents = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/registry/MenuScreenRegistrar.java");
		String pattern = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScarPattern.java");
		String brazier = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/rite/ScarBrazierRite.java");
		String brazierBlock = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BrazierBlock.java");
		String menu = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/MasonsEffigyMenu.java");
		String screen = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java");

		assertContains("mason effigy block registered", blockInit, "mason_effigy");
		assertContains("iron brazier block registered", blockInit, "iron_brazier");
		assertContains("mason effigy block entity registered", blockEntityInit, "MasonsEffigyBlockEntity");
		assertContains("iron brazier block entity registered", blockEntityInit, "IronBrazierBlockEntity");
		assertContains("mason effigy menu registered", containerInit, "MenuType<MasonsEffigyMenu>");
		assertContains("mason effigy screen registered", clientEvents, "MasonsEffigyScreen::new");
		assertContains("scar pattern stores selected scar ids", pattern, "TAG_SCAR_IDS");
		assertContains("scar pattern creates prepared loadouts", pattern, "createPreparedPattern");
		assertContains("iron brazier routes scar offerings through absorption", brazierBlock,
				"BrazierRites.absorb");
		assertContains("brazier rites include the scar rite",
				SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/rite/brazier/BrazierRites.java"),
				"ScarBrazierRite.INSTANCE");
		assertContains("brazier learns from scar items", brazier, "tryLearnScar");
		assertContains("brazier commits scar pattern loadouts", brazier, "tryCommitLoadout");
		assertContains("brazier clears loadouts from blank motif paper", brazier, "tryClearLoadout");
		assertContains("brazier accepts blank motif paper for clearing", brazier, "ItemInit.runic_motif_paper");
		assertContains("brazier unlocks known scars", brazier, "addKnownCerebralScar");
		assertContains("brazier activates selected scars", brazier, "activateCerebralScar");
		assertContains("brazier deactivates active scars", brazier, "deactivateCerebralScar");
		assertContains("effigy shares pattern storage limit", menu, "ItemScarPattern.MAX_SCAR_IDS");
		assertContains("effigy screen renders known scars", screen, "renderScarList");
	}

	private static void masonsEffigyScreenMatchesConceptLayout() {
		String menu = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/menu/tile/functional/MasonsEffigyMenu.java");
		String screen = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java");

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
		assertContains("effigy screen renders the centrifuge-style venous background", screen,
				"VeinBackgroundRenderer.Field.MACHINE.count(12), VeinBackgroundRenderer.Palette.MACHINE.background(BG)");
		assertContains("effigy screen draws its veins under the top panel", screen, "veins.draw(gfx, x, y, TOP_W, TOP_H, animTime);");
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
		assertNotContains("effigy screen no longer draws noisy portrait overlay", screen, "renderVeinOverlay");
		assertNotContains("effigy screen no longer uses symbolic scar glyphs", screen, "drawScarGlyph");
		assertNotContains("effigy screen no longer uses vanilla button builder", screen, "Button.builder");
	}

	private static void masonEffigyMotifPaperPreparesDynamicPattern() {
		String block = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/MasonsEffigyBlock.java");
		String blockEntity = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/MasonsEffigyBlockEntity.java");
		String packet = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/scars/PacketPrepareScarPattern.java");
		String renderer = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/render/item/ScarPatternItemRenderer.java");
		String effigyRenderer = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/MasonsEffigyRenderer.java");
		String clientEvents = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/registry/EntityRendererRegistrar.java");
		String projection = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");

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
		assertShowsEnglish("effigy reports blood charge feedback", blockEntity, "Effigy blood charge:");
		assertContains("effigy completion creates scar pattern", blockEntity, "ItemScarPattern.createPreparedPattern");
		assertContains("effigy completion ejects scar pattern", blockEntity, "level.addFreshEntity(patternEntity)");
		assertContains("effigy completion bursts red particles", blockEntity, "PacketHandler.sendSanguineOmenEffect");
		assertContains("effigy consumes motif paper", blockEntity, "stack.shrink(1)");
		assertContains("blood projection charges sinks such as the effigy", projection, "ProjectedBloodSink");
		assertContains("effigy is a projected-blood sink", blockEntity, "implements MenuProvider, ProjectedBloodSink");
		assertContains("effigy sink charges its motif ritual", blockEntity,
				"receiveProjectedBlood(player, rate, livingStaff)");
		assertContains("effigy renderer displays pending motif", effigyRenderer, "getPendingMotifDisplayStack");
		assertContains("effigy renderer floats item above block", effigyRenderer, "renderStatic");
		assertContains("effigy renderer registered", clientEvents,
				"event.registerBlockEntityRenderer(BlockEntityInit.mason_effigy.get(), MasonsEffigyRenderer::new)");
		assertContains("prepare packet writes selected scars to effigy", packet, "effigy.setSelectedScarIds(selected)");
		assertNotContains("prepare packet no longer gives free scar pattern", packet, "player.getInventory().add(pattern)");
		assertNotContains("right click no longer directly creates scar pattern", block, "ItemScarPattern.createPreparedPattern");
		assertContains("scar pattern renderer reads dynamic ids", renderer, "ItemScarPattern.getScarIds(stack)");
		assertContains("scar pattern renderer lays out four quadrants", renderer, "QUADRANT_OFFSETS");
		assertContains("scar pattern renderer renders actual scar item overlays", renderer, "scarStackFor");
		assertContains("scar pattern renderer renders only overlay layer from scar item model", renderer, "quad.getTintIndex() == 1");
		assertContains("scar pattern renderer rotates dynamic overlays around z", renderer, "Axis.ZP.rotationDegrees");
		assertNotContains("scar pattern renderer no longer renders whole scar items", renderer, "renderStatic(scarStack");
	}

	private static void dynamicScarPatternReplacesPerScarPatternItems() {
		String itemInit = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String pattern = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ItemScarPattern.java");
		String slot = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/common/menu/slot/ScarPatternSlot.java");
		String screen = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/scar/ScarStationScreen.java");
		String renderer = SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/render/item/ScarPatternItemRenderer.java");

		assertContains("single scar pattern item remains registered", itemInit, "BASEITEMS.register(\"scar_pattern\"");
		assertNotContains("old heart scar pattern item is removed", itemInit, "scar_pattern_heart");
		assertNotContains("old blood-honed scar pattern item is removed", itemInit, "scar_pattern_blood_honed");
		assertContains("scar pattern can create single-template stacks", pattern, "createTemplatePattern");
		assertContains("scar pattern exposes multiple station entries", pattern, "getTemplateEntries");
		assertNotContains("scar pattern no longer stores a fixed scar holder", pattern, "DeferredHolder<Item, Item>");
		assertNotContains("scar pattern no longer stores a fixed recipe path", pattern, "String path");
		assertContains("scar pattern slot accepts scar patterns", slot, "stack.getItem() instanceof ItemScarPattern");
		assertNotContains("scar pattern slot rejects retired binders", slot, "ItemScarBinder");
		assertContains("scar station rebuilds pattern entries from dynamic item", screen, "ItemScarPattern.getTemplateEntries");
		assertNotContains("scar station no longer scans binders for patterns", screen, "ScarBinderItemHandler");
		assertContains("scar pattern renderer resolves actual scar items", renderer, "BuiltInRegistries.ITEM.get(scarId)");
		assertNotContains("scar pattern renderer no old pattern item lookup", renderer, "scar_pattern_");
	}
}
