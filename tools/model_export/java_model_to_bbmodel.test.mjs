import assert from "assert/strict";
import { execFile } from "child_process";
import { spawn } from "child_process";
import { mkdtemp, readFile, rm, writeFile } from "fs/promises";
import { tmpdir } from "os";
import path from "path";
import { fileURLToPath } from "url";
import { promisify } from "util";

const execFileAsync = promisify(execFile);
const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(SCRIPT_DIR, "../..");

async function runConverter(args) {
  return execFileAsync("node", ["tools/model_export/java_model_to_bbmodel.mjs", ...args], {
    cwd: ROOT,
    windowsHide: true,
  });
}

async function runDropWrapper(sourceFile) {
  return new Promise((resolve, reject) => {
    const child = spawn("cmd", ["/c", "tools\\model_export\\drop_java_model_to_bbmodel.bat", sourceFile], {
      cwd: ROOT,
      windowsHide: true,
    });
    let stdout = "";
    let stderr = "";
    const timeout = setTimeout(() => {
      child.kill();
      reject(new Error(`drop wrapper timed out\nstdout:\n${stdout}\nstderr:\n${stderr}`));
    }, 10000);

    child.stdout.on("data", (chunk) => {
      stdout += chunk;
    });
    child.stderr.on("data", (chunk) => {
      stderr += chunk;
    });
    child.on("error", (error) => {
      clearTimeout(timeout);
      reject(error);
    });
    child.on("close", (code) => {
      clearTimeout(timeout);
      if (code === 0) {
        resolve({ stdout, stderr });
      } else {
        reject(new Error(`drop wrapper exited ${code}\nstdout:\n${stdout}\nstderr:\n${stderr}`));
      }
    });

    child.stdin.end("\n");
  });
}

async function testMnemonicWhaleTuningConstantsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const outputFile = path.join(outputDir, "MnemonicWhaleModel.bbmodel");

  try {
    await runConverter([
      "--source",
      "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/MnemonicWhaleModel.java",
      "--texture",
      "textures/entity/mnemonic_whale/model_mnemonic_whale.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const bodyCubes = bbmodel.elements.filter((element) => element.name.startsWith("body_"));
    const bodyWidths = bodyCubes.map((element) => element.to[0] - element.from[0]);

    assert.equal(bbmodel.name, "MnemonicWhaleModel");
    assert.deepEqual(bbmodel.resolution, { width: 128, height: 128 });
    assert.ok(bodyWidths.includes(20), "head width constant should resolve to 20");
    assert.ok(bodyWidths.includes(16), "mid-body width constant should resolve to 16");
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testDropWrapperOutputsBesideDroppedFile() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-model-drop-"));
  const sourceFile = path.join(outputDir, "TempDropModel.java");
  const outputFile = path.join(outputDir, "TempDropModel.bbmodel");
  const oldOutputFile = path.join(
    ROOT,
    "src/main/resources/assets/hemomancy/models/item/bbmodel/TempDropModel.bbmodel"
  );

  const javaModel = `
package temp;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TempDropModel {
  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild("body", CubeListBuilder.create()
      .texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
      PartPose.offset(0.0F, 0.0F, 0.0F));
    return LayerDefinition.create(mesh, 16, 16);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");
    await rm(oldOutputFile, { force: true });

    await runDropWrapper(sourceFile);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    assert.equal(bbmodel.name, "TempDropModel");
  } finally {
    await rm(oldOutputFile, { force: true });
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testPartPoseRotationConverts() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "TempRotationModel.java");
  const outputFile = path.join(outputDir, "TempRotationModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TempRotationModel {
  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild("tilted_ring", CubeListBuilder.create()
      .texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
      PartPose.rotation(0.0F, 0.0F, 0.7854F));
    return LayerDefinition.create(mesh, 16, 16);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/item/sporitic_thurible.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const group = bbmodel.outliner.find((node) => node.name === "tilted_ring");

    assert.ok(group, "rotated part should be exported as an outliner group");
    assert.deepEqual(group.rotation, [0, 0, -45.000105]);
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testCubeDeformationDoesNotBakeIntoPositionAndSize() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "TempDeformationModel.java");
  const outputFile = path.join(outputDir, "TempDeformationModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TempDeformationModel {
  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild("bowl", CubeListBuilder.create()
      .texOffs(0, 0).addBox(-3.5F, 3.4F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(-0.25F)),
      PartPose.ZERO);
    return LayerDefinition.create(mesh, 16, 16);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/item/sporitic_thurible.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const cube = bbmodel.elements.find((element) => element.name.startsWith("bowl_"));

    assert.ok(cube, "deformed cube should be exported");
    assert.deepEqual(cube.from, [-3.5, 19.6, -3.5]);
    assert.deepEqual(cube.to, [3.5, 20.6, 3.5]);
    assert.equal(cube.inflate, -0.25);
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testAddOrReplaceChildReplacesEarlierSiblingDefinition() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "TempArmorReplacementModel.java");
  const outputFile = path.join(outputDir, "TempArmorReplacementModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class TempArmorReplacementModel {
  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild("right_leg", CubeListBuilder.create()
      .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
      PartPose.offset(-1.9F, 12.0F, 0.0F));
    root.addOrReplaceChild("left_leg", CubeListBuilder.create()
      .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
      PartPose.offset(1.9F, 12.0F, 0.0F));
    PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
      .texOffs(16, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F),
      PartPose.offset(-1.9F, 12.0F, 0.0F));
    rightLeg.addOrReplaceChild("right_leg2", CubeListBuilder.create()
      .texOffs(59, 48).addBox(-3.1F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F),
      PartPose.offset(0.0F, 4.15F, 0.0F));
    PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
      .texOffs(0, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F),
      PartPose.offset(1.9F, 12.0F, 0.0F));
    leftLeg.addOrReplaceChild("left_leg2", CubeListBuilder.create()
      .texOffs(59, 48).addBox(-2.9F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F),
      PartPose.offset(0.0F, 4.15F, 0.0F));
    return LayerDefinition.create(mesh, 128, 128);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/models/armor/prismatic_layer_1.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const rightLegGroups = groupNames.filter((name) => name === "right_leg");
    const leftLegGroups = groupNames.filter((name) => name === "left_leg");

    assert.deepEqual(rightLegGroups, ["right_leg"], "right_leg placeholder should be replaced");
    assert.deepEqual(leftLegGroups, ["left_leg"], "left_leg placeholder should be replaced");
    assert.ok(groupNames.includes("right_leg2"), "replacement right leg child should remain");
    assert.ok(groupNames.includes("left_leg2"), "replacement left leg child should remain");
    assert.equal(
      bbmodel.elements.filter((element) => element.name.startsWith("right_leg_")).length,
      1,
      "right leg should only export replacement cube geometry"
    );
    assert.equal(
      bbmodel.elements.filter((element) => element.name.startsWith("left_leg_")).length,
      1,
      "left leg should only export replacement cube geometry"
    );
    assert.equal(
      bbmodel.elements.some((element) => element.name.startsWith("right_leg_0_0_")),
      false,
      "right leg placeholder cube should not remain"
    );
    assert.equal(
      bbmodel.elements.some((element) => element.name.startsWith("left_leg_0_0_")),
      false,
      "left leg placeholder cube should not remain"
    );
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testSlotBranchesKeepSameNamedLegPartsDistinct() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "TempSlotBranchArmorModel.java");
  const outputFile = path.join(outputDir, "TempSlotBranchArmorModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public class TempSlotBranchArmorModel {
  public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
    MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
    PartDefinition root = mesh.getRoot();
    if (slot.equals(EquipmentSlot.LEGS)) {
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
        .texOffs(50, 116).addBox(-1.4F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F)
        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror(false),
        PartPose.offset(1.9F, 12.0F, 0.0F));
      leftLeg.addOrReplaceChild("left_shin_plate", CubeListBuilder.create()
        .texOffs(29, 88).addBox(-0.4F, 6.6F, 3.25F, 1.0F, 3.0F, 1.0F),
        PartPose.ZERO);
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
        .texOffs(50, 116).addBox(-1.6F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F)
        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
        PartPose.offset(-1.9F, 12.0F, 0.0F));
      rightLeg.addOrReplaceChild("right_shin_plate", CubeListBuilder.create()
        .texOffs(43, 74).addBox(-0.6F, 6.6F, 3.25F, 1.0F, 3.0F, 1.0F),
        PartPose.ZERO);
    }
    if (slot.equals(EquipmentSlot.FEET)) {
      root.addOrReplaceChild("left_leg", CubeListBuilder.create()
        .texOffs(38, 91).addBox(-1.9F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F),
        PartPose.offset(1.9F, 12.0F, 0.0F));
      root.addOrReplaceChild("right_leg", CubeListBuilder.create()
        .texOffs(39, 97).addBox(-2.1F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F),
        PartPose.offset(-1.9F, 12.0F, 0.0F));
    }
    return LayerDefinition.create(mesh, 256, 256);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/models/armor/sheolic_blood_lust_layer_1.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const uuids = bbmodel.elements.map((element) => element.uuid);

    assert.ok(groupNames.includes("left_leg_legs"), "leggings left_leg group should be kept");
    assert.ok(groupNames.includes("right_leg_legs"), "leggings right_leg group should be kept");
    assert.ok(groupNames.includes("left_leg_feet"), "boots left_leg group should be kept");
    assert.ok(groupNames.includes("right_leg_feet"), "boots right_leg group should be kept");
    assert.ok(groupNames.includes("left_shin_plate"), "left shin child should remain attached");
    assert.ok(groupNames.includes("right_shin_plate"), "right shin child should remain attached");
    assert.equal(new Set(uuids).size, uuids.length, "same-named slot branch cubes should not reuse UUIDs");
    assert.ok(
      bbmodel.elements.some((element) => element.name.startsWith("left_leg_legs_50_116_")),
      "leggings left shin cube should export under the leggings group name"
    );
    assert.ok(
      bbmodel.elements.some((element) => element.name.startsWith("left_leg_feet_38_91_")),
      "boots left foot cube should export under the feet group name"
    );
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testChalybeateSnailForLoopPartsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const outputFile = path.join(outputDir, "ChalybeateSnailModel.bbmodel");

  try {
    await runConverter([
      "--source",
      "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/ChalybeateSnailModel.java",
      "--texture",
      "textures/entity/chalybeate_snail/model_chalybeate_snail.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);

    assert.equal(bbmodel.name, "ChalybeateSnailModel");
    assert.ok(groupNames.includes("front_plate_-4"), "loop should expand first front plate");
    assert.ok(groupNames.includes("front_plate_4"), "loop should expand last front plate");
    assert.ok(groupNames.includes("left_plate_0"), "loop should resolve string concatenation with zero");
    assert.ok(groupNames.includes("right_plate_2"), "loop should resolve string concatenation with positive values");
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testPrismCuttleLoopGeneratedArmsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const outputFile = path.join(outputDir, "PrismCuttleModel.bbmodel");

  try {
    await runConverter([
      "--source",
      "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/PrismCuttleModel.java",
      "--texture",
      "textures/entity/prism_cuttle/model_prism_cuttle_deep.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const segment4 = findGroup(bbmodel.outliner, "arm_0_segment_4");

    assert.equal(bbmodel.name, "PrismCuttleModel");
    assert.ok(groupNames.includes("root"), "root group should export");
    assert.ok(groupNames.includes("mantle"), "mantle child should export");
    assert.ok(groupNames.includes("arm_0"), "loop should expand first arm");
    assert.ok(groupNames.includes("arm_7"), "loop should expand last arm");
    assert.ok(groupNames.includes("arm_0_segment_4"), "nested arm segments should export");
    assert.ok(groupNames.includes("arm_7_segment_4"), "last nested arm segments should export");
    assert.ok(groupNames.includes("left_fin_4"), "split left fin panels should export");
    assert.ok(groupNames.includes("right_fin_4"), "split right fin panels should export");
    assert.ok(segment4.origin[1] < 6, "nested arm segment origins should remain attached to the face");
    assert.ok(bbmodel.elements.length >= 50, "cuttle model should export segmented tentacles and fins");
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testVenomRibCentipedeNestedSegmentsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const outputFile = path.join(outputDir, "VenomRibCentipedeModel.bbmodel");

  try {
    await runConverter([
      "--source",
      "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/VenomRibCentipedeModel.java",
      "--texture",
      "textures/entity/venom_rib_centipede/model_venom_rib_centipede.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const segment0 = bbmodel.outliner.find((node) => node.name === "segment_0");
    const segment1 = segment0.children.find((node) => node.name === "segment_1");

    assert.equal(bbmodel.name, "VenomRibCentipedeModel");
    assert.ok(groupNames.includes("left_antenna_0"), "left antenna root segment should export from the head");
    assert.ok(groupNames.includes("right_antenna_0"), "right antenna root segment should export from the head");
    assert.ok(groupNames.includes("left_antenna_5"), "left antenna should export tunable segment chain");
    assert.ok(groupNames.includes("right_antenna_5"), "right antenna should export tunable segment chain");
    assert.ok(groupNames.includes("tail"), "tail segment should export at the end of the body");
    assert.ok(groupNames.includes("left_tail_feeler_0"), "left rear feeler root should export from the tail");
    assert.ok(groupNames.includes("right_tail_feeler_0"), "right rear feeler root should export from the tail");
    assert.ok(groupNames.includes("left_tail_feeler_3"), "left rear feeler should export segmented chain");
    assert.ok(groupNames.includes("right_tail_feeler_3"), "right rear feeler should export segmented chain");
    assert.ok(groupNames.includes("segment_10"), "loop should resolve imported segment count alias");
    assert.ok(groupNames.includes("left_legs_10"), "nested loop child parts should export");
    assert.ok(segment1, "segment_1 should be nested under segment_0");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("left_antenna_5_")),
      "left antenna should export as cube geometry");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("right_antenna_5_")),
      "right antenna should export as cube geometry");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("tail_")),
      "tail should export as cube geometry");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("left_tail_feeler_3_")),
      "left rear feeler should export as cube geometry");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("right_tail_feeler_3_")),
      "right rear feeler should export as cube geometry");
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testLanternTickHelperGeneratedLegsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const outputFile = path.join(outputDir, "LanternTickModel.bbmodel");

  try {
    await runConverter([
      "--source",
      "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/LanternTickModel.java",
      "--texture",
      "textures/entity/lantern_tick.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);

    assert.equal(bbmodel.name, "LanternTickModel");
    assert.ok(groupNames.includes("left_leg_0"), "helper-generated first left leg should export");
    assert.ok(groupNames.includes("left_leg_3"), "helper-generated last left leg should export");
    assert.ok(groupNames.includes("right_leg_0"), "helper-generated first right leg should export");
    assert.ok(groupNames.includes("right_leg_3"), "helper-generated last right leg should export");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("left_leg_0_")),
      "helper-generated left leg should export cube geometry");
    assert.ok(bbmodel.elements.some((element) => element.name.startsWith("right_leg_0_")),
      "helper-generated right leg should export cube geometry");
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testLegacyObfuscatedModelRendererArmorConverts() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "LegacyArmorModel.java");
  const outputFile = path.join(outputDir, "LegacyArmorModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class LegacyArmorModel extends ModelBiped {
  ModelRenderer Helmet;
  ModelRenderer[] Mask;

  public LegacyArmorModel(float f) {
    super(f, 0, 128, 64);
    this.field_78090_t = 128;
    this.field_78089_u = 64;
    this.Mask = new ModelRenderer[2];
    for (int a = 0; a < 2; ++a) {
      this.Mask[a] = new ModelRenderer((ModelBase)this, 52 + a * 24, 2);
      this.Mask[a].func_78789_a(-4.5f, -5.0f, -4.6f, 9, 5, 1);
      this.Mask[a].func_78793_a(0.0f, 0.0f, 0.0f);
      this.Mask[a].func_78787_b(128, 64);
      this.setRotation(this.Mask[a], 0.0f, 0.0f, 0.0f);
    }
    this.Helmet = new ModelRenderer((ModelBase)this, 41, 8);
    this.Helmet.func_78789_a(-4.5f, -9.0f, -4.5f, 9, 9, 9);
    this.Helmet.func_78793_a(0.0f, 0.0f, -2.5f);
    this.Helmet.field_78809_i = true;
    this.setRotation(this.Helmet, 0.2268928f, 0.0f, 0.0f);
    this.field_78116_c.field_78804_l.clear();
    this.field_78116_c.func_78792_a(this.Helmet);
    this.field_78116_c.func_78792_a(this.Mask[0]);
    this.field_78115_e.func_78792_a(this.Mask[1]);
  }

  private void setRotation(ModelRenderer model, float x, float y, float z) {
    model.field_78795_f = x;
    model.field_78796_g = y;
    model.field_78808_h = z;
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/item/sporitic_thurible.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const helmet = bbmodel.outliner
      .find((node) => node.name === "head")
      .children.find((node) => node.name === "Helmet");
    const helmetCube = bbmodel.elements.find((element) => element.name.startsWith("Helmet_"));

    assert.equal(bbmodel.name, "LegacyArmorModel");
    assert.deepEqual(bbmodel.resolution, { width: 128, height: 64 });
    assert.ok(groupNames.includes("head"), "biped head root should be exported");
    assert.ok(groupNames.includes("body"), "biped body root should be exported");
    assert.ok(groupNames.includes("Mask_0"), "array model renderer in loop should expand first element");
    assert.ok(groupNames.includes("Mask_1"), "array model renderer in loop should expand second element");
    assert.deepEqual(helmet.rotation, [-13, 0, 0]);
    assert.equal(helmetCube.mirror_uv, true);
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testLegacyNamedModelRendererMethodsConvert() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "LegacyNamedModel.java");
  const outputFile = path.join(outputDir, "LegacyNamedModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class LegacyNamedModel extends ModelBiped {
  ModelRenderer HeadBand;

  public LegacyNamedModel(float f) {
    super(f, 0, 64, 32);
    this.textureWidth = 64;
    this.textureHeight = 32;
    this.HeadBand = new ModelRenderer(this, 4, 6);
    this.HeadBand.mirror = true;
    this.HeadBand.addBox(-1.0F, -2.0F, -3.0F, 2, 3, 4, 0.5F);
    this.HeadBand.setRotationPoint(1.0F, 2.0F, 3.0F);
    this.HeadBand.rotateAngleX = 0.7853982F;
    this.bipedHead.addChild(this.HeadBand);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/item/sporitic_thurible.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const groupNames = [];
    collectGroupNames(bbmodel.outliner, groupNames);
    const cube = bbmodel.elements.find((element) => element.name.startsWith("HeadBand_"));

    assert.deepEqual(bbmodel.resolution, { width: 64, height: 32 });
    assert.ok(groupNames.includes("HeadBand"), "named legacy ModelRenderer part should be exported");
    assert.deepEqual(cube.from, [0, 21, 0]);
    assert.deepEqual(cube.to, [2, 24, 4]);
    assert.equal(cube.inflate, 0.5);
    assert.equal(cube.mirror_uv, true);
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

async function testLegacyRuntimeRotationAssignmentsAreIgnored() {
  const outputDir = await mkdtemp(path.join(tmpdir(), "hemomancy-bbmodel-"));
  const sourceFile = path.join(outputDir, "LegacyAnimatedModel.java");
  const outputFile = path.join(outputDir, "LegacyAnimatedModel.bbmodel");

  const javaModel = `
package temp;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class LegacyAnimatedModel extends ModelBiped {
  ModelRenderer Cloth;

  public LegacyAnimatedModel(float f) {
    super(f, 0, 64, 32);
    this.Cloth = new ModelRenderer(this, 0, 0);
    this.Cloth.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2);
    this.Cloth.rotateAngleX = 0.25F;
    this.bipedBody.addChild(this.Cloth);
  }

  public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
    float c = limbSwingAmount * 0.5F;
    this.Cloth.rotateAngleX = c - 0.1047198F;
    this.bipedHead.rotateAngleY = yaw * ((float)Math.PI / 180);
  }
}
`;

  try {
    await writeFile(sourceFile, javaModel, "utf8");

    await runConverter([
      "--source",
      sourceFile,
      "--texture",
      "textures/item/sporitic_thurible.png",
      "--output",
      outputFile,
    ]);

    const bbmodel = JSON.parse(await readFile(outputFile, "utf8"));
    const body = bbmodel.outliner.find((node) => node.name === "body");
    const cloth = body.children.find((node) => node.name === "Cloth");

    assert.deepEqual(cloth.rotation, [-14.323945, 0, 0]);
  } finally {
    await rm(outputDir, { recursive: true, force: true });
  }
}

function findGroup(nodes, name) {
  for (const node of nodes) {
    if (!node || typeof node !== "object") {
      continue;
    }
    if (node.name === name) {
      return node;
    }
    if (Array.isArray(node.children)) {
      const child = findGroup(
        node.children.filter((candidate) => candidate && typeof candidate === "object"),
        name
      );
      if (child) {
        return child;
      }
    }
  }
  return null;
}

function collectGroupNames(nodes, names) {
  for (const node of nodes) {
    if (!node || typeof node !== "object") {
      continue;
    }
    if (typeof node.name === "string") {
      names.push(node.name);
    }
    if (Array.isArray(node.children)) {
      collectGroupNames(
        node.children.filter((child) => child && typeof child === "object"),
        names
      );
    }
  }
}

const tests = [
  ["converts MnemonicWhaleModel dimensions that reference tuning constants", testMnemonicWhaleTuningConstantsConvert],
  ["drop wrapper writes the bbmodel beside the dropped Java file", testDropWrapperOutputsBesideDroppedFile],
  ["converts PartPose.rotation without an offset", testPartPoseRotationConverts],
  ["keeps CubeDeformation in inflate instead of baking it into position and size", testCubeDeformationDoesNotBakeIntoPositionAndSize],
  ["honors addOrReplaceChild replacement for repeated sibling names", testAddOrReplaceChildReplacesEarlierSiblingDefinition],
  ["keeps same-named leg parts distinct across equipment slot branches", testSlotBranchesKeepSameNamedLegPartsDistinct],
  ["converts ChalybeateSnailModel parts declared inside a simple for loop", testChalybeateSnailForLoopPartsConvert],
  ["converts PrismCuttleModel loop-generated arms and rule constants", testPrismCuttleLoopGeneratedArmsConvert],
  ["converts VenomRibCentipedeModel nested loop-generated segments", testVenomRibCentipedeNestedSegmentsConvert],
  ["converts LanternTickModel helper-generated angled legs", testLanternTickHelperGeneratedLegsConvert],
  ["converts obfuscated legacy ModelRenderer armor parts", testLegacyObfuscatedModelRendererArmorConverts],
  ["converts named legacy ModelRenderer methods", testLegacyNamedModelRendererMethodsConvert],
  ["ignores legacy runtime rotation assignments that are not static model data", testLegacyRuntimeRotationAssignmentsAreIgnored],
];

for (const [name, test] of tests) {
  await test();
  console.log(`ok - ${name}`);
}
