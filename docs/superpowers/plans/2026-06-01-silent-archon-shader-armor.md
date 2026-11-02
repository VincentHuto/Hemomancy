# Silent Archon Shader Armor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a semi-translucent Monolith/Vesper shader overlay to worn Silent Archon armor and render all four Silent Archon armor item stacks as 3D custom armor-piece models.

**Architecture:** Keep the existing Silent Archon armor texture/model as the base pass. Add an entity-model-compatible Monolith Fragment shader holder and render type for a second overlay pass. Share Silent Archon model/texture/render-type selection through a small client helper used by both the worn armor layer and item renderer.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge client rendering, `IClientItemExtensions`, `BlockEntityWithoutLevelRenderer`, `RenderLayer`, custom `RenderType`, existing `SilentArchonArmorModel`.

---

### Task 1: Add Guard Tests

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/item/MonolithFragmentShaderResourceTest.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/HarbingerArmor3dModelResourceTest.java`

- [ ] **Step 1: Write failing source/resource assertions**

Add assertions that require:

```java
assertContains("entity shader holder exists", shaderInit,
		"MONOLITH_FRAGMENT_ENTITY = new ShaderHolder(Hemomancy.rloc(\"item/monolith_fragment\")");
assertContains("entity shader holder uses entity vertex format", shaderInit,
		"DefaultVertexFormat.NEW_ENTITY");
assertContains("armor overlay render type exists", renderTypes,
		"silentArchonArmorOverlay");
assertContains("armor overlay uses entity model vertex format", renderTypes,
		"DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS");
assertContains("armor overlay uses entity shader holder", renderTypes,
		"ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard()");
```

Add Silent Archon 3D item/overlay assertions:

```java
String silentItem = read(SOURCE_ROOT.resolve(
		"com/vincenthuto/hemomancy/common/item/harbinger/armor/SilentArchonArmorItem.java"));
assertContains("silent archon item exposes custom item renderer", silentItem,
		"new SilentArchonArmorItemRenderer(");
assertExists("silent archon item renderer", SOURCE_ROOT.resolve(
		"com/vincenthuto/hemomancy/client/render/item/SilentArchonArmorItemRenderer.java"));
assertExists("silent archon overlay layer", SOURCE_ROOT.resolve(
		"com/vincenthuto/hemomancy/client/render/layer/player/SilentArchonArmorOverlayLayer.java"));
assertExists("silent archon render helper", SOURCE_ROOT.resolve(
		"com/vincenthuto/hemomancy/client/render/armor/SilentArchonArmorRenderHelper.java"));
assertContains("player layer registration includes silent archon overlay", layerEvents,
		"new SilentArchonArmorOverlayLayer");
assertItemModelParent("silent_archon_helm", "builtin/entity");
assertItemModelParent("silent_archon_chestplate", "builtin/entity");
assertItemModelParent("silent_archon_leggings", "builtin/entity");
assertItemModelParent("silent_archon_boots", "builtin/entity");
```

Add helper method:

```java
private static void assertItemModelParent(String item, String parent) throws IOException {
	String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + item + ".json"));
	assertContains("model parent for " + item, model, "\"parent\": \"" + parent + "\"");
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew.bat compileTestJava`

Expected: compilation succeeds, but running the source tests directly will fail until implementation. If `compileTestJava` fails because the test references a missing helper, fix the test helper only.

Run focused source tests:

```powershell
./gradlew.bat test --tests com.vincenthuto.hemomancy.common.item.MonolithFragmentShaderResourceTest --tests com.vincenthuto.hemomancy.common.item.harbinger.HarbingerArmor3dModelResourceTest
```

Expected: failures mention missing `MONOLITH_FRAGMENT_ENTITY`, `silentArchonArmorOverlay`, `SilentArchonArmorItemRenderer`, `SilentArchonArmorOverlayLayer`, or `builtin/entity`.

### Task 2: Add Entity-Compatible Shader Render Type

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java`

- [ ] **Step 1: Add a NEW_ENTITY shader holder**

Add to `ShaderInit` near `MONOLITH_FRAGMENT`:

```java
public static ShaderHolder MONOLITH_FRAGMENT_ENTITY = new ShaderHolder(Hemomancy.rloc("item/monolith_fragment"),
		DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ShardSeed", "Burden", "Attuned",
		"FractalScale", "Snap", "GuiClamp");
```

Register it after `MONOLITH_FRAGMENT`:

```java
registerShader(event, MONOLITH_FRAGMENT_ENTITY.createInstance(provider));
```

- [ ] **Step 2: Add overlay render type**

Add to `HemoRenderTypes`:

```java
public static RenderType silentArchonArmorOverlay(float gameTime, float shardSeed, float burden, float attuned,
		float guiClamp) {
	RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
			"silent_archon_armor_overlay_uniforms",
			() -> {
				ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
				setUniform(shader, "HemoTime", gameTime);
				setUniform(shader, "ShardSeed", shardSeed);
				setUniform(shader, "Burden", burden);
				setUniform(shader, "Attuned", attuned);
				setUniform(shader, "GuiClamp", guiClamp);
				setUniform(shader, "FractalScale", guiClamp > 0.5f ? 9.0f : 11.0f);
				setUniform(shader, "Snap", 0.65f);
			},
			() -> {
				ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
				if (shader instanceof ExtendedShaderInstance extended) {
					extended.setUniformDefaults();
				}
			});
	return RenderType.create("silent_archon_armor_overlay",
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
			RenderType.CompositeState.builder()
					.setShaderState(ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard())
					.setTexturingState(uniforms)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.setOverlayState(RenderType.OVERLAY)
					.createCompositeState(false));
}
```

- [ ] **Step 3: Run focused tests**

Run the two focused tests from Task 1. Expected: shader/render-type assertions pass; item renderer/layer/model parent assertions still fail.

### Task 3: Add Shared Silent Archon Render Helper

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/armor/SilentArchonArmorRenderHelper.java`

- [ ] **Step 1: Create helper**

```java
package com.vincenthuto.hemomancy.client.render.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SilentArchonArmorRenderHelper {
	public static final ResourceLocation LAYER_1 = Hemomancy.rloc("textures/models/armor/silent_archon_layer_1.png");
	public static final ResourceLocation LAYER_2 = Hemomancy.rloc("textures/models/armor/silent_archon_layer_2.png");
	public static final int OVERLAY_COLOR = 0x66FFFFFF;
	private static final float TIME_CYCLE_MS = 240000.0f;

	private SilentArchonArmorRenderHelper() {
	}

	public static boolean isSilentArchon(ItemStack stack) {
		return slotFor(stack) != null;
	}

	public static EquipmentSlot slotFor(ItemStack stack) {
		Item item = stack.getItem();
		if (item == ItemInit.silent_archon_helm.get()) {
			return EquipmentSlot.HEAD;
		} else if (item == ItemInit.silent_archon_chestplate.get()) {
			return EquipmentSlot.CHEST;
		} else if (item == ItemInit.silent_archon_leggings.get()) {
			return EquipmentSlot.LEGS;
		} else if (item == ItemInit.silent_archon_boots.get()) {
			return EquipmentSlot.FEET;
		}
		return null;
	}

	public static SilentArchonArmorModel<LivingEntity> modelForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> SilentArchonArmorModel.helmet.get();
			case CHEST -> SilentArchonArmorModel.chest.get();
			case LEGS -> SilentArchonArmorModel.legs.get();
			case FEET -> SilentArchonArmorModel.boots.get();
			default -> SilentArchonArmorModel.chest.get();
		};
	}

	public static ResourceLocation textureForSlot(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS ? LAYER_2 : LAYER_1;
	}

	public static RenderType overlayRenderType(ItemStack stack, EquipmentSlot slot, boolean guiClamp) {
		float seed = Math.floorMod(System.identityHashCode(stack) + slot.ordinal() * 997, 4096) / 4096.0f;
		return HemoRenderTypes.silentArchonArmorOverlay(timeSeconds(), seed, 0.25f, 1.0f, guiClamp ? 1.0f : 0.0f);
	}

	public static float timeSeconds() {
		return (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
	}
}
```

- [ ] **Step 2: Run focused tests**

Expected: helper existence assertion passes; item renderer/layer/model parent assertions still fail.

### Task 4: Add Worn Armor Overlay Layer

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/SilentArchonArmorOverlayLayer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`

- [ ] **Step 1: Create layer**

```java
package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.model.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SilentArchonArmorOverlayLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	public SilentArchonArmorOverlayLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch) {
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack stack = entity.getItemBySlot(slot);
			if (!SilentArchonArmorRenderHelper.isSilentArchon(stack)) {
				continue;
			}
			renderSlot(poseStack, buffer, entity, stack, slot);
		}
	}

	private void renderSlot(PoseStack poseStack, MultiBufferSource buffer, T entity, ItemStack stack,
			EquipmentSlot slot) {
		SilentArchonArmorModel<LivingEntity> model = SilentArchonArmorRenderHelper.modelForSlot(slot);
		model.setAllVisible(true);
		this.getParentModel().copyPropertiesTo(model);
		MorphlingPlayerPartVisibility.applyToArmorModel(model, slot);
		VertexConsumer consumer = buffer.getBuffer(
				SilentArchonArmorRenderHelper.overlayRenderType(stack, slot, false));
		model.renderToBuffer(poseStack, consumer, packedLight(entity), OverlayTexture.NO_OVERLAY,
				SilentArchonArmorRenderHelper.OVERLAY_COLOR);
	}

	private int packedLight(T entity) {
		return 0x00F000F0;
	}
}
```

- [ ] **Step 2: Register for player skins**

In `addLayerToPlayerSkin`, after the normal player armor-related custom layers:

```java
livingRenderer.addLayer(new SilentArchonArmorOverlayLayer(livingRenderer));
```

- [ ] **Step 3: Run focused tests**

Expected: layer existence/registration assertions pass; item renderer/model parent assertions still fail.

### Task 5: Add 3D Item Renderer and Item Extension Wiring

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/item/SilentArchonArmorItemRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/armor/SilentArchonArmorItem.java`

- [ ] **Step 1: Create item renderer**

```java
package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SilentArchonArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
	public SilentArchonArmorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		EquipmentSlot slot = SilentArchonArmorRenderHelper.slotFor(stack);
		if (slot == null) {
			return;
		}
		boolean gui = displayContext == ItemDisplayContext.GUI;
		if (gui) {
			Lighting.setupForEntityInInventory();
		}
		poseStack.pushPose();
		applyDisplayTransform(displayContext, slot, poseStack);
		SilentArchonArmorModel<LivingEntity> model = SilentArchonArmorRenderHelper.modelForSlot(slot);
		model.setAllVisible(true);
		VertexConsumer base = buffer.getBuffer(RenderType.entityTranslucent(
				SilentArchonArmorRenderHelper.textureForSlot(slot)));
		model.renderToBuffer(poseStack, base, packedLight, OverlayTexture.NO_OVERLAY, -1);
		VertexConsumer overlay = buffer.getBuffer(
				SilentArchonArmorRenderHelper.overlayRenderType(stack, slot, gui));
		model.renderToBuffer(poseStack, overlay, packedLight, OverlayTexture.NO_OVERLAY,
				SilentArchonArmorRenderHelper.OVERLAY_COLOR);
		poseStack.popPose();
		if (gui) {
			Lighting.setupFor3DItems();
		}
	}

	private static void applyDisplayTransform(ItemDisplayContext context, EquipmentSlot slot, PoseStack poseStack) {
		poseStack.translate(0.5D, 0.5D, 0.5D);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		switch (context) {
			case GUI -> {
				poseStack.translate(0.0D, -0.35D, 0.0D);
				poseStack.mulPose(Axis.YP.rotationDegrees(-28.0F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F));
				scaleForSlot(poseStack, slot, 0.72F);
			}
			case GROUND -> {
				poseStack.translate(0.0D, -0.15D, 0.0D);
				scaleForSlot(poseStack, slot, 0.36F);
			}
			case FIXED -> {
				poseStack.translate(0.0D, -0.18D, 0.0D);
				poseStack.mulPose(Axis.YP.rotationDegrees(-25.0F));
				scaleForSlot(poseStack, slot, 0.45F);
			}
			case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
				poseStack.translate(0.0D, -0.55D, -0.18D);
				scaleForSlot(poseStack, slot, 0.36F);
			}
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
				poseStack.translate(0.0D, -0.45D, -0.10D);
				scaleForSlot(poseStack, slot, 0.32F);
			}
			default -> scaleForSlot(poseStack, slot, 0.45F);
		}
	}

	private static void scaleForSlot(PoseStack poseStack, EquipmentSlot slot, float scale) {
		float slotScale = slot == EquipmentSlot.CHEST ? scale * 0.82F : scale;
		poseStack.scale(slotScale, slotScale, slotScale);
	}
}
```

- [ ] **Step 2: Wire custom renderer**

In `SilentArchonArmorItem#hemomancy$getClientItemExtensions`, add:

```java
@Override
public BlockEntityWithoutLevelRenderer getCustomRenderer() {
	return new SilentArchonArmorItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
			Minecraft.getInstance().getEntityModels());
}
```

Add imports for `Minecraft`, `BlockEntityWithoutLevelRenderer`, and `SilentArchonArmorItemRenderer`.

- [ ] **Step 3: Run focused tests**

Expected: item renderer assertions pass; only item JSON parent assertions still fail.

### Task 6: Switch Silent Archon Item Models to builtin/entity

**Files:**
- Modify: `src/main/resources/assets/hemomancy/models/item/silent_archon_helm.json`
- Modify: `src/main/resources/assets/hemomancy/models/item/silent_archon_chestplate.json`
- Modify: `src/main/resources/assets/hemomancy/models/item/silent_archon_leggings.json`
- Modify: `src/main/resources/assets/hemomancy/models/item/silent_archon_boots.json`

- [ ] **Step 1: Change parent**

Each file keeps its existing `layer0` texture and uses:

```json
{
  "parent": "builtin/entity",
  "textures": {
    "layer0": "hemomancy:item/silent_archon_helm",
    "particle": "hemomancy:item/silent_archon_helm"
  },
  "gui_light": "front",
  "display": {}
}
```

Use the matching item name for each file.

- [ ] **Step 2: Run focused tests**

Expected: Task 1 focused tests pass.

### Task 7: Update Reference Docs and Verify

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Update Silent Archon rendering sentence**

Replace the existing texture-only sentence in section 22.7 with one that states runtime rendering uses `SilentArchonArmorModel`, the robe-derived textures, a semi-translucent Monolith Fragment shader overlay when worn, and 3D custom item-stack rendering.

- [ ] **Step 2: Run compile and build**

Run: `./gradlew.bat compileJava`

Expected: `BUILD SUCCESSFUL`.

Run: `./gradlew.bat build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Visual smoke test**

Run: `./gradlew.bat runClient`

Expected: client starts. Equip Silent Archon armor and inspect inventory/hand/item-frame/dropped stack views.
