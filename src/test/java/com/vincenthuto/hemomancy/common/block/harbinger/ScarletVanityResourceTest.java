package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class ScarletVanityResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private ScarletVanityResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String blockInit = readSource("com/vincenthuto/hemomancy/common/init/BlockInit.java");
		String tileInit = readSource("com/vincenthuto/hemomancy/common/init/BlockEntityInit.java");
		String clientEvents = readSource("com/vincenthuto/hemomancy/client/registry/EntityRendererRegistrar.java");
		String vanityBlock = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScarletVanityBlock.java");
		String vanityTile = readSource(
				"com/vincenthuto/hemomancy/common/tile/harbinger/functional/ScarletVanityBlockEntity.java");
		String vanityRenderer = readSource(
				"com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/ScarletVanityRenderer.java");
		String scryingPodium = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScryingPodiumBlock.java");
		String blockstate = readResource("assets/hemomancy/blockstates/scarlet_vanity.json");
		String blockModel = readResource("assets/hemomancy/models/block/scarlet_vanity.json");
		String lootTable = readResource("data/hemomancy/loot_table/blocks/scarlet_vanity.json");
		String lang = readResource("assets/hemomancy/lang/en_us.json");

		assertContains("scarlet vanity block should be registered", blockInit,
				"scarlet_vanity = MODELEDBLOCKS.register(\"scarlet_vanity\"");
		assertContains("scarlet vanity should use its own block class", blockInit,
				"new ScarletVanityBlock(");
		assertContains("scarlet vanity block entity should be registered", tileInit,
				"BlockEntityType<ScarletVanityBlockEntity>");
		assertContains("scarlet vanity block entity should bind to the vanity block", tileInit,
				".of(ScarletVanityBlockEntity::new, BlockInit.scarlet_vanity.get()).build(null)");
		assertContains("client should register scarlet vanity renderer", clientEvents,
				"event.registerBlockEntityRenderer(BlockEntityInit.scarlet_vanity.get(), ScarletVanityRenderer::new);");

		assertContains("scarlet vanity opens the existing equipment menu packet", vanityBlock,
				"PacketHandler.sendToServer(new PacketOpenScarsInv(desk));");
		assertContains("scarlet vanity should render its baked JSON model", vanityBlock,
				"return RenderShape.MODEL;");
		assertContains("scarlet vanity should keep facing, half and waterlogged state", vanityBlock,
				"pBuilder.add(FACING, HALF, WATERLOGGED);");
		assertContains("scarlet vanity should create its block entity", vanityBlock,
				"new ScarletVanityBlockEntity(pos, state)");
		assertContains("scarlet vanity block entity should use its registry type", vanityTile,
				"super(BlockEntityInit.scarlet_vanity.get(), pos, state);");

		assertNotContains("scrying podium should no longer open the equipment menu", scryingPodium,
				"PacketOpenScarsInv");
		assertNotContains("scrying podium should no longer send the equipment menu packet", scryingPodium,
				"new PacketOpenScarsInv");
		assertContains("scarlet vanity renderer should render equipped gourd", vanityRenderer,
				"HarbingerEquipmentMenu.GOURD_SLOT_INDEX");
		assertContains("scarlet vanity renderer should render equipped charm", vanityRenderer,
				"HarbingerEquipmentMenu.CHARM_SLOT_INDEX");
		assertContains("scarlet vanity renderer should render equipped morphling jar", vanityRenderer,
				"HarbingerEquipmentMenu.JAR_SLOT_INDEX");
		assertContains("scarlet vanity renderer should lay item stacks onto the table with world item context",
				vanityRenderer, "renderStatic(stack, ItemDisplayContext.GROUND");
		assertNotContains("scarlet vanity renderer should not use fixed display lighting for tabletop items",
				vanityRenderer, "renderStatic(stack, ItemDisplayContext.FIXED");
		assertContains("scarlet vanity renderer should sample world light for tabletop items", vanityRenderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("scarlet vanity renderer should cap display item light so equipment is not fullbright",
				vanityRenderer, "DISPLAY_ITEM_MAX_LIGHT");
		assertNotContains("scarlet vanity renderer should not pass block-entity light directly to item stacks",
				vanityRenderer, "renderEquippedItems(te, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);");
		assertContains("scarlet vanity renderer should pass facing into all tabletop item transforms",
				vanityRenderer, "renderEquippedItem(te, facing, equipment");
		assertContains("scarlet vanity renderer should mirror item yaw on north/south facings",
				vanityRenderer, "case NORTH, SOUTH -> -yaw;");
		assertNotContains("scarlet vanity renderer should not use raw decorative yaw for tabletop items",
				vanityRenderer, "Axis.YP.rotationDegrees(yaw)");

		assertContains("scarlet vanity blockstate should support north-facing rotation", blockstate,
				"\"facing=north,half=lower\"");
		assertContains("scarlet vanity static model should use the cutout block render path", blockModel,
				"\"render_type\": \"cutout\"");
		assertNotContains("scarlet vanity static model should not blend the room through the cabinet",
				blockModel, "\"render_type\": \"translucent\"");
		assertContains("scarlet vanity model should have a red cabinet face", blockModel,
				"\"name\": \"red_cabinet\"");
		assertContains("scarlet vanity model should have diamond trim like the reference vanity", blockModel,
				"\"name\": \"diamond_trim_left\"");
		assertContains("scarlet vanity model should have a raised back rail", blockModel,
				"\"name\": \"backsplash\"");
		assertContains("scarlet vanity should drop itself", lootTable,
				"\"name\": \"hemomancy:scarlet_vanity\"");
		assertContains("scarlet vanity should have a display name", lang,
				"\"block.hemomancy.scarlet_vanity\": \"Scarlet Vanity\"");
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
	}
}
