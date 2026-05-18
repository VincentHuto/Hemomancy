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
  ["converts ChalybeateSnailModel parts declared inside a simple for loop", testChalybeateSnailForLoopPartsConvert],
];

for (const [name, test] of tests) {
  await test();
  console.log(`ok - ${name}`);
}
