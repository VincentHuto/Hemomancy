# Puppeteer Summon Visuals Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the three puppeteer summon vanilla visuals with distinct Hemomancy construct models, renderers, and texture atlases.

**Architecture:** Keep the existing entity classes and gameplay inheritance intact, but stop using vanilla `VexRenderer`, `SkeletonRenderer`, and `ZombieRenderer`. Add custom client `EntityModel` classes, wire each existing renderer class through `MobRenderer`, register model layers in `LayerEvents`, and add dedicated PNG atlases under `assets/hemomancy/textures/entity/puppeteer_summon/`.

**Tech Stack:** Java 21, Minecraft 1.21.1/NeoForge 21.1 client rendering, `ModelPart`/`LayerDefinition`, existing plain Java source/resource tests, PowerShell PNG generation for local atlas assets.

---

## File Structure

- Create `src/test/java/com/vincenthuto/hemomancy/common/entity/summon/PuppeteerSummonVisualResourceTest.java`: source/resource guard that fails until models, renderer rewrites, layer registrations, and textures exist.
- Create `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/VeinwingVultureModel.java`: small flying blood-thread construct model and hover/wing animation.
- Create `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MarrowSpitterModel.java`: ranged marrow-frame construct model and spitter/nozzle animation.
- Create `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/GoreboundHulkModel.java`: heavy clot brute construct model and lurch/breathing animation.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/VeinwingVultureRenderer.java`: replace vanilla renderer inheritance with `MobRenderer`.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/MarrowSpitterRenderer.java`: replace vanilla skeleton renderer inheritance with `MobRenderer`; this hides the bow layer by omission.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/GoreboundHulkRenderer.java`: replace vanilla zombie renderer inheritance with `MobRenderer`.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`: register the three new model layer definitions.
- Create `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/veinwing_vulture.png`.
- Create `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/marrow_spitter.png`.
- Create `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/gorebound_hulk.png`.

---

### Task 1: Add A Failing Source/Resource Guard

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/entity/summon/PuppeteerSummonVisualResourceTest.java`

- [ ] **Step 1: Add the test file**

```java
package com.vincenthuto.hemomancy.common.entity.summon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PuppeteerSummonVisualResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private PuppeteerSummonVisualResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertSummonVisuals("veinwing_vulture", "VeinwingVulture", "VexRenderer");
		assertSummonVisuals("marrow_spitter", "MarrowSpitter", "SkeletonRenderer");
		assertSummonVisuals("gorebound_hulk", "GoreboundHulk", "ZombieRenderer");

		String layerEvents = readSource("com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		assertContains("veinwing layer registration", layerEvents, "VeinwingVultureModel.LAYER_LOCATION");
		assertContains("marrow layer registration", layerEvents, "MarrowSpitterModel.LAYER_LOCATION");
		assertContains("gorebound layer registration", layerEvents, "GoreboundHulkModel.LAYER_LOCATION");

		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		assertContains("veinwing renderer registration", clientEvents, "VeinwingVultureRenderer::new");
		assertContains("marrow renderer registration", clientEvents, "MarrowSpitterRenderer::new");
		assertContains("gorebound renderer registration", clientEvents, "GoreboundHulkRenderer::new");
	}

	private static void assertSummonVisuals(String id, String className, String forbiddenRenderer) throws IOException {
		Path modelPath = SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/entity/summon/" + className + "Model.java");
		Path rendererPath = SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/summon/" + className + "Renderer.java");
		Path texturePath = RESOURCE_ROOT.resolve(
				"assets/hemomancy/textures/entity/puppeteer_summon/" + id + ".png");

		assertExists(className + " model source", modelPath);
		assertExists(className + " renderer source", rendererPath);
		assertPng(className + " texture", texturePath);

		String model = read(modelPath);
		String renderer = read(rendererPath);

		assertContains(className + " model exposes layer", model, "public static final ModelLayerLocation LAYER_LOCATION");
		assertContains(className + " model uses entity id", model, "Hemomancy.rloc(\"" + id + "\")");
		assertContains(className + " renderer uses MobRenderer", renderer, "extends MobRenderer<");
		assertContains(className + " renderer uses custom texture path", renderer,
				"textures/entity/puppeteer_summon/" + id + ".png");
		assertContains(className + " renderer preserves dismissal skip", renderer,
				"PuppeteerSummonRenderHelper.shouldSkipRender(entity)");
		assertContains(className + " renderer preserves dismissal scale", renderer,
				"PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack)");
		assertDoesNotContain(className + " renderer no longer imports vanilla renderer", renderer, forbiddenRenderer);
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertPng(String label, Path path) throws IOException {
		assertExists(label, path);
		byte[] bytes = Files.readAllBytes(path);
		byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		for (int i = 0; i < pngHeader.length; i++) {
			if (bytes.length <= i || bytes[i] != pngHeader[i]) {
				throw new AssertionError(label + ": not a PNG file " + path);
			}
		}
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
}
```

- [ ] **Step 2: Compile the test**

Run:

```powershell
./gradlew.bat compileTestJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the test and verify it fails before implementation**

Run:

```powershell
java -cp "build/classes/java/test" com.vincenthuto.hemomancy.common.entity.summon.PuppeteerSummonVisualResourceTest
```

Expected: failure mentioning the first missing source file:

```text
java.lang.AssertionError: VeinwingVulture model source: missing src/main/java\com\vincenthuto\hemomancy\client\model\entity\summon\VeinwingVultureModel.java
```

---

### Task 2: Add Custom Model Classes

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/VeinwingVultureModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MarrowSpitterModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/GoreboundHulkModel.java`

- [ ] **Step 1: Add `VeinwingVultureModel`**

```java
package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 9.0F, 3.0F, new CubeDeformation(0.15F))
						.texOffs(18, 0).addBox(-1.0F, -7.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.5F, -2.5F, -3.5F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(20, 16).addBox(-1.0F, -0.5F, -7.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(33, 16).addBox(-0.5F, 1.0F, -9.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -6.0F, -0.8F));

		root.addOrReplaceChild("left_wing", CubeListBuilder.create()
						.texOffs(0, 28).addBox(0.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 34).addBox(2.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(6, 34).addBox(5.0F, -0.5F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(12, 34).addBox(8.0F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, -4.0F, 0.0F));

		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(32, 28).addBox(-12.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(32, 34).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(38, 34).addBox(-6.0F, -0.5F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(44, 34).addBox(-9.0F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, -4.0F, 0.0F));

		root.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(48, 0).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(54, 0).addBox(-1.0F, 9.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 3.0F, 1.0F));

		root.addOrReplaceChild("left_talon", CubeListBuilder.create()
						.texOffs(64, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(70, 0).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.2F, 3.0F, -0.4F));

		root.addOrReplaceChild("right_talon", CubeListBuilder.create()
						.texOffs(64, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(70, 6).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.2F, 3.0F, -0.4F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float flap = Mth.sin(ageInTicks * 0.55F) * 0.45F;
		this.root.y = 15.0F + Mth.sin(ageInTicks * 0.16F) * 0.6F;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.7F;
		this.leftWing.zRot = 0.25F + flap;
		this.rightWing.zRot = -0.25F - flap;
		this.leftWing.yRot = -0.18F;
		this.rightWing.yRot = 0.18F;
		this.tail.xRot = 0.18F + Mth.sin(ageInTicks * 0.22F) * 0.08F;
		this.leftTalon.xRot = Mth.cos(ageInTicks * 0.18F) * 0.12F;
		this.rightTalon.xRot = -this.leftTalon.xRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
```

- [ ] **Step 2: Add `MarrowSpitterModel`**

```java
package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("marrow_spitter"), "main");

	private final ModelPart root;
	private final ModelPart nozzle;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rearLeg;
	private final ModelPart tubing;

	public MarrowSpitterModel(ModelPart root) {
		this.root = root.getChild("root");
		this.nozzle = this.root.getChild("nozzle");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.rearLeg = this.root.getChild("rear_leg");
		this.tubing = this.root.getChild("tubing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-3.0F, -9.0F, -2.5F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.1F))
						.texOffs(24, 0).addBox(-2.0F, -6.5F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.2F))
						.texOffs(36, 0).addBox(-4.5F, -8.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(44, 0).addBox(3.5F, -8.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		root.addOrReplaceChild("nozzle", CubeListBuilder.create()
						.texOffs(0, 20).addBox(-1.5F, -1.5F, -9.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
						.texOffs(26, 20).addBox(-2.0F, -2.0F, -11.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, -5.5F, -2.5F));

		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(10, 38).addBox(-1.0F, 7.0F, -4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, -1.0F, -1.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(24, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(34, 38).addBox(-1.0F, 7.0F, -4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, -1.0F, -1.0F));

		root.addOrReplaceChild("rear_leg", CubeListBuilder.create()
						.texOffs(48, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(58, 38).addBox(-2.0F, 6.0F, 0.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.0F));

		root.addOrReplaceChild("tubing", CubeListBuilder.create()
						.texOffs(52, 0).addBox(-4.0F, -10.0F, 1.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(52, 4).addBox(-0.5F, -10.0F, -4.5F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(52, 12).addBox(-5.0F, -5.0F, 1.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
		this.nozzle.xRot = headPitch * Mth.DEG_TO_RAD * 0.45F + Mth.sin(ageInTicks * 0.2F) * 0.025F;
		this.tubing.xScale = 1.0F + Mth.sin(ageInTicks * 0.18F) * 0.04F;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.65F * limbSwingAmount;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.65F * limbSwingAmount;
		this.rearLeg.xRot = Mth.sin(limbSwing * 0.6662F) * 0.35F * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
```

- [ ] **Step 3: Add `GoreboundHulkModel`**

```java
package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("gorebound_hulk"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart backMass;

	public GoreboundHulkModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftArm = this.root.getChild("left_arm");
		this.rightArm = this.root.getChild("right_arm");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.backMass = this.root.getChild("back_mass");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-6.0F, -13.0F, -4.0F, 12.0F, 13.0F, 8.0F, new CubeDeformation(0.45F))
						.texOffs(42, 0).addBox(-4.0F, -15.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.25F))
						.texOffs(72, 0).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.1F))
						.texOffs(26, 26).addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -13.0F, -3.5F));

		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(0, 42).addBox(-1.0F, -1.0F, -2.0F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(22, 42).addBox(0.0F, 12.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
				PartPose.offset(6.0F, -11.0F, 0.0F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(50, 42).addBox(-4.0F, -1.0F, -2.0F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(72, 42).addBox(-6.0F, 12.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
				PartPose.offset(-6.0F, -11.0F, 0.0F));

		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2F))
						.texOffs(18, 70).addBox(-2.5F, 6.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(42, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2F))
						.texOffs(60, 70).addBox(-2.5F, 6.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("back_mass", CubeListBuilder.create()
						.texOffs(84, 0).addBox(-4.5F, -10.0F, 3.0F, 9.0F, 11.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(84, 18).addBox(-2.5F, -13.0F, 5.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.2F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float breath = 1.0F + Mth.sin(ageInTicks * 0.11F) * 0.025F;
		this.root.xScale = breath;
		this.root.zScale = breath;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.45F;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.35F;
		this.leftArm.xRot = Mth.cos(limbSwing * 0.45F + Mth.PI) * 0.85F * limbSwingAmount - 0.08F;
		this.rightArm.xRot = Mth.cos(limbSwing * 0.45F) * 0.85F * limbSwingAmount - 0.08F;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.45F) * 0.45F * limbSwingAmount;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.45F + Mth.PI) * 0.45F * limbSwingAmount;
		this.backMass.xRot = Mth.sin(ageInTicks * 0.09F) * 0.025F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
```

- [ ] **Step 4: Compile model code**

Run:

```powershell
./gradlew.bat compileJava
```

Expected: failure in renderer files because they still use vanilla renderer types and do not yet use the new models. If compilation succeeds, still continue to Task 3 because the visual objective is not complete until renderer inheritance changes.

---

### Task 3: Replace Vanilla Renderer Inheritance

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/VeinwingVultureRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/MarrowSpitterRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/GoreboundHulkRenderer.java`

- [ ] **Step 1: Replace `VeinwingVultureRenderer`**

```java
package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.VeinwingVultureModel;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VeinwingVultureRenderer extends MobRenderer<VeinwingVultureEntity, VeinwingVultureModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/veinwing_vulture.png");

	public VeinwingVultureRenderer(EntityRendererProvider.Context context) {
		super(context, new VeinwingVultureModel(context.bakeLayer(VeinwingVultureModel.LAYER_LOCATION)), 0.25F);
	}

	@Override
	public ResourceLocation getTextureLocation(VeinwingVultureEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(VeinwingVultureEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
```

- [ ] **Step 2: Replace `MarrowSpitterRenderer`**

```java
package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MarrowSpitterModel;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MarrowSpitterRenderer extends MobRenderer<MarrowSpitterEntity, MarrowSpitterModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/marrow_spitter.png");

	public MarrowSpitterRenderer(EntityRendererProvider.Context context) {
		super(context, new MarrowSpitterModel(context.bakeLayer(MarrowSpitterModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(MarrowSpitterEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(MarrowSpitterEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
```

- [ ] **Step 3: Replace `GoreboundHulkRenderer`**

```java
package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.GoreboundHulkModel;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoreboundHulkRenderer extends MobRenderer<GoreboundHulkEntity, GoreboundHulkModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/gorebound_hulk.png");

	public GoreboundHulkRenderer(EntityRendererProvider.Context context) {
		super(context, new GoreboundHulkModel(context.bakeLayer(GoreboundHulkModel.LAYER_LOCATION)), 0.65F);
	}

	@Override
	public ResourceLocation getTextureLocation(GoreboundHulkEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(GoreboundHulkEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) {
			return;
		}
		poseStack.pushPose();
		PuppeteerSummonRenderHelper.applyDismissalScale(entity, partialTicks, poseStack);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
```

- [ ] **Step 4: Compile renderer code**

Run:

```powershell
./gradlew.bat compileJava
```

Expected: failure from missing layer registration when the client tries to bake layers is runtime-only, so Java compilation should either pass now or surface direct type/signature mistakes. Fix any compile errors in the files touched in Tasks 2 and 3 before continuing.

---

### Task 4: Register Model Layers

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`

- [ ] **Step 1: Add layer registrations**

In `registerModelLayers`, directly after the existing `BloodThrallModel.LAYER_LOCATION` registration, add:

```java
		event.registerLayerDefinition(VeinwingVultureModel.LAYER_LOCATION, VeinwingVultureModel::createBodyLayer);
		event.registerLayerDefinition(MarrowSpitterModel.LAYER_LOCATION, MarrowSpitterModel::createBodyLayer);
		event.registerLayerDefinition(GoreboundHulkModel.LAYER_LOCATION, GoreboundHulkModel::createBodyLayer);
```

The surrounding section should become:

```java
		event.registerLayerDefinition(BloodThrallModel.LAYER_LOCATION, BloodThrallModel::createBodyLayer);
		event.registerLayerDefinition(VeinwingVultureModel.LAYER_LOCATION, VeinwingVultureModel::createBodyLayer);
		event.registerLayerDefinition(MarrowSpitterModel.LAYER_LOCATION, MarrowSpitterModel::createBodyLayer);
		event.registerLayerDefinition(GoreboundHulkModel.LAYER_LOCATION, GoreboundHulkModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypModel.LAYER_LOCATION, MorphlingPolypModel::createBodyLayer);
```

- [ ] **Step 2: Run the source/resource guard and verify it now fails on textures**

Run:

```powershell
./gradlew.bat compileTestJava
java -cp "build/classes/java/test" com.vincenthuto.hemomancy.common.entity.summon.PuppeteerSummonVisualResourceTest
```

Expected: the Java main fails with a missing PNG path under `assets/hemomancy/textures/entity/puppeteer_summon/`.

---

### Task 5: Add Summon Texture Atlases

**Files:**
- Create: `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/veinwing_vulture.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/marrow_spitter.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/gorebound_hulk.png`

- [ ] **Step 1: Generate the three PNG atlases**

Run this PowerShell script from the repository root:

```powershell
Add-Type -AssemblyName System.Drawing

$dir = 'src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon'
New-Item -ItemType Directory -Force -Path $dir | Out-Null

function New-Atlas {
	param(
		[string] $Path,
		[string] $Base,
		[string] $Dark,
		[string] $Bright,
		[string] $Bone
	)

	$bitmap = New-Object System.Drawing.Bitmap 128, 128
	$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
	$graphics.Clear([System.Drawing.ColorTranslator]::FromHtml($Base))
	$random = New-Object System.Random 481516

	$darkBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($Dark))
	$brightBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($Bright))
	$boneBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml($Bone))
	$inkPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(190, 22, 4, 8)), 2
	$shinePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(145, 255, 126, 118)), 1

	for ($i = 0; $i -lt 90; $i++) {
		$x = $random.Next(0, 128)
		$y = $random.Next(0, 128)
		$w = $random.Next(5, 20)
		$h = $random.Next(3, 16)
		if (($i % 3) -eq 0) {
			$graphics.FillEllipse($darkBrush, $x, $y, $w, $h)
		} elseif (($i % 5) -eq 0) {
			$graphics.FillRectangle($boneBrush, $x, $y, $w, [Math]::Max(2, [int]($h / 2)))
		} else {
			$graphics.FillEllipse($brightBrush, $x, $y, $w, $h)
		}
	}

	for ($i = 0; $i -lt 34; $i++) {
		$x = $random.Next(0, 128)
		$y = $random.Next(0, 128)
		$x2 = [Math]::Min(127, [Math]::Max(0, $x + $random.Next(-30, 31)))
		$y2 = [Math]::Min(127, [Math]::Max(0, $y + $random.Next(8, 36)))
		$graphics.DrawLine($inkPen, $x, $y, $x2, $y2)
	}

	for ($i = 0; $i -lt 18; $i++) {
		$x = $random.Next(0, 120)
		$y = $random.Next(0, 120)
		$graphics.DrawArc($shinePen, $x, $y, $random.Next(10, 28), $random.Next(5, 18), 10, 120)
	}

	$bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
	$graphics.Dispose()
	$bitmap.Dispose()
}

New-Atlas "$dir/veinwing_vulture.png" '#451017' '#190407' '#8f2024' '#d7b7a2'
New-Atlas "$dir/marrow_spitter.png" '#5b1720' '#21070a' '#a33435' '#e3d0b6'
New-Atlas "$dir/gorebound_hulk.png" '#3f0d12' '#160305' '#7d171d' '#b98d78'
```

- [ ] **Step 2: Run the source/resource guard**

Run:

```powershell
./gradlew.bat compileTestJava
java -cp "build/classes/java/test" com.vincenthuto.hemomancy.common.entity.summon.PuppeteerSummonVisualResourceTest
```

Expected: exit code `0` and no output.

---

### Task 6: Final Verification And Commit

**Files:**
- Verify all files from Tasks 1 through 5.

- [ ] **Step 1: Compile main Java**

Run:

```powershell
./gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Compile tests**

Run:

```powershell
./gradlew.bat compileTestJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the summon visual resource guard**

Run:

```powershell
java -cp "build/classes/java/test" com.vincenthuto.hemomancy.common.entity.summon.PuppeteerSummonVisualResourceTest
```

Expected: exit code `0` and no output.

- [ ] **Step 4: Run full build**

Run:

```powershell
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Inspect the final diff**

Run:

```powershell
git -c safe.directory='C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy' status --short
git -c safe.directory='C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy' diff --stat
```

Expected: only the planned test, model, renderer, layer event, and texture files are changed.

- [ ] **Step 6: Commit implementation**

Run:

```powershell
git -c safe.directory='C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy' add src/test/java/com/vincenthuto/hemomancy/common/entity/summon/PuppeteerSummonVisualResourceTest.java src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/VeinwingVultureModel.java src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MarrowSpitterModel.java src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/GoreboundHulkModel.java src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/VeinwingVultureRenderer.java src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/MarrowSpitterRenderer.java src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/GoreboundHulkRenderer.java src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/veinwing_vulture.png src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/marrow_spitter.png src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon/gorebound_hulk.png
git -c safe.directory='C:/Users/Vince/Desktop/My MC Mods/NeoForge/1.21.1/Hemomancy' commit -m "Add puppeteer summon visuals"
```

Expected: commit succeeds with only the planned files.
