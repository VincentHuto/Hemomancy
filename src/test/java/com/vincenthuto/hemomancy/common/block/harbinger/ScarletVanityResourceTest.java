package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScarletVanityResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private ScarletVanityResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockInit = readSource("com/vincenthuto/hemomancy/common/init/BlockInit.java");
		String tileInit = readSource("com/vincenthuto/hemomancy/common/init/BlockEntityInit.java");
		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String vanityBlock = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScarletVanityBlock.java");
		String vanityTile = readSource(
				"com/vincenthuto/hemomancy/common/tile/functional/ScarletVanityBlockEntity.java");
		String vanityRenderer = readSource(
				"com/vincenthuto/hemomancy/client/render/tile/functional/ScarletVanityRenderer.java");
		String vanityReflectionYaw = extractMethod(vanityRenderer, "playerReflectionYawForFacing");
		String scryingPodium = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScryingPodiumBlock.java");
		String blockstate = readResource("assets/hemomancy/blockstates/scarlet_vanity.json");
		String blockModel = readResource("assets/hemomancy/models/block/scarlet_vanity.json");
		String itemModel = readResource("assets/hemomancy/models/item/scarlet_vanity.json");
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
				"BlockEntityRenderers.register(BlockEntityInit.scarlet_vanity.get(), ScarletVanityRenderer::new);");

		assertContains("scarlet vanity opens the existing equipment menu packet", vanityBlock,
				"PacketHandler.sendToServer(new PacketOpenScarsInv(pos));");
		assertContains("scarlet vanity should render its baked JSON model", vanityBlock,
				"return RenderShape.MODEL;");
		assertContains("scarlet vanity should keep facing and waterlogged state", vanityBlock,
				"pBuilder.add(FACING, WATERLOGGED);");
		assertContains("scarlet vanity should create its block entity", vanityBlock,
				"return new ScarletVanityBlockEntity(pos, state);");
		assertContains("scarlet vanity block entity should use its registry type", vanityTile,
				"super(BlockEntityInit.scarlet_vanity.get(), pos, state);");

		assertDoesNotContain("scrying podium should no longer open the equipment menu", scryingPodium,
				"PacketOpenScarsInv");
		assertDoesNotContain("scrying podium should no longer send the equipment menu packet", scryingPodium,
				"new PacketOpenScarsInv");

		assertContains("scarlet vanity renderer should draw the blood reflection bowl", vanityRenderer,
				"renderBloodBowl");
		assertContains("scarlet vanity renderer should draw the player reflection", vanityRenderer,
				"renderPlayerReflection(te, partialTicks, poseStack);");
		assertContains("scarlet vanity player reflection should use the entity renderer", vanityRenderer,
				"getEntityRenderDispatcher()");
		assertContains("scarlet vanity player reflection should use its own facing yaw", vanityRenderer,
				"playerReflectionYawForFacing(facing)");
		assertContains("scarlet vanity north-facing reflection should face out from the vanity", vanityReflectionYaw,
				"case NORTH -> 180.0F;");
		assertContains("scarlet vanity south-facing reflection should face out from the vanity", vanityReflectionYaw,
				"case SOUTH -> 0.0F;");
		assertContains("scarlet vanity player reflection should neutralize live player yaw", vanityRenderer,
				"player.setYRot(0.0F);");
		assertContains("scarlet vanity player reflection should neutralize live player body yaw", vanityRenderer,
				"player.yBodyRot = 0.0F;");
		assertContains("scarlet vanity player reflection should restore live player yaw", vanityRenderer,
				"player.setYRot(previousYRot);");
		assertContains("scarlet vanity player reflection should restore live player body yaw", vanityRenderer,
				"player.yBodyRot = previousBodyRot;");
		assertDoesNotContain("scarlet vanity player reflection should not reuse tabletop item rotation", vanityRenderer,
				"rotationForFacing(facing) + 180.0F");
		assertContains("scarlet vanity player reflection should render without a shadow", vanityRenderer,
				"entityRenderDispatcher.setRenderShadow(false);");
		assertContains("scarlet vanity player reflection should use fullbright lighting", vanityRenderer,
				"entityRenderDispatcher.render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, playerBuffers, FULL_BRIGHT);");
		assertContains("scarlet vanity renderer should use the same water shader path as the scrying podium",
				vanityRenderer, "RadiantPortalRendertype.textWithWaterShader");
		assertContains("scarlet vanity renderer should render equipped gourd", vanityRenderer,
				"HarbingerEquipmentMenu.GOURD_SLOT_INDEX");
		assertContains("scarlet vanity renderer should render equipped charm", vanityRenderer,
				"HarbingerEquipmentMenu.CHARM_SLOT_INDEX");
		assertContains("scarlet vanity renderer should render equipped morphling jar", vanityRenderer,
				"HarbingerEquipmentMenu.JAR_SLOT_INDEX");
		assertContains("scarlet vanity renderer should lay item stacks onto the table with world item context",
				vanityRenderer, "renderStatic(stack, ItemDisplayContext.GROUND");
		assertDoesNotContain("scarlet vanity renderer should not use fixed display lighting for tabletop items",
				vanityRenderer, "renderStatic(stack, ItemDisplayContext.FIXED");
		assertContains("scarlet vanity renderer should sample world light for tabletop items", vanityRenderer,
				"LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above())");
		assertContains("scarlet vanity renderer should cap display item light so equipment is not fullbright",
				vanityRenderer, "DISPLAY_ITEM_MAX_LIGHT");
		assertDoesNotContain("scarlet vanity renderer should not pass block-entity light directly to item stacks",
				vanityRenderer, "renderEquippedItems(te, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);");
		assertContains("scarlet vanity renderer should tint the reflection as blood instead of room beige",
				vanityRenderer, "BLOOD_POOL_RED");
		assertContains("scarlet vanity renderer should pass facing into all tabletop item transforms",
				vanityRenderer, "renderEquippedItem(te, facing, scars");
		assertContains("scarlet vanity renderer should mirror item yaw on north/south facings",
				vanityRenderer, "case NORTH, SOUTH -> -yaw;");
		assertDoesNotContain("scarlet vanity renderer should not use raw decorative yaw for tabletop items",
				vanityRenderer, "Axis.YP.rotationDegrees(yaw)");

		assertContains("scarlet vanity blockstate should support north-facing rotation", blockstate,
				"\"facing\": \"north\"");
		assertContains("scarlet vanity static model should use the cutout block render path", blockModel,
				"\"render_type\": \"cutout\"");
		assertDoesNotContain("scarlet vanity static model should not blend the room through the cabinet",
				blockModel, "\"render_type\": \"translucent\"");
		assertContains("scarlet vanity model should have a center blood bowl", blockModel,
				"\"name\": \"blood_bowl\"");
		assertContains("scarlet vanity model should have a red cabinet face", blockModel,
				"\"name\": \"red_cabinet\"");
		assertContains("scarlet vanity model should have diamond trim like the reference vanity", blockModel,
				"\"name\": \"diamond_trim_left\"");
		assertContains("scarlet vanity model should have a raised back rail", blockModel,
				"\"name\": \"backsplash\"");
		assertContains("scarlet vanity item model should parent the block model", itemModel,
				"\"parent\": \"hemomancy:block/scarlet_vanity\"");
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

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}

	private static String extractMethod(String source, String methodName) {
		int start = source.indexOf(methodName + "(Direction");
		if (start < 0) {
			throw new AssertionError("missing method " + methodName);
		}
		int openBrace = source.indexOf('{', start);
		if (openBrace < 0) {
			throw new AssertionError("missing method body for " + methodName);
		}
		int depth = 0;
		for (int i = openBrace; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return source.substring(openBrace, i + 1);
				}
			}
		}
		throw new AssertionError("unterminated method body for " + methodName);
	}
}
