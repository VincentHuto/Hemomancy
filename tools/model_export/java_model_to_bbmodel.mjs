import { createHash } from "crypto";
import { readFileSync } from "fs";
import { mkdir, readFile, writeFile } from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(SCRIPT_DIR, "../..");
const OUT_DIR = "src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses";

const MODELS = [
  {
    name: "HollowVesselModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/HollowVesselModel.java",
    texture: "textures/entity/hollow_vessel/hollow_vessel.png",
  },
  {
    name: "SeraphaeModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/SeraphaeModel.java",
    texture: "textures/entity/seraphae/seraphae.png",
  },
  {
    name: "ContainmentAnchorModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/ContainmentAnchorModel.java",
    texture: "textures/entity/containment_anchor/containment_anchor.png",
  },
  {
    name: "SeraphaeFragmentModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/seraphae/SeraphaeFragmentModel.java",
    texture: "textures/entity/seraphae_fragment/seraphae_fragment.png",
  },
  {
    name: "AnnettaKnowlesModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/AnnettaKnowlesModel.java",
    texture: "textures/entity/boss/annetta/annetta_knowles.png",
  },
  {
    name: "StainedPriestessModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/StainedPriestessModel.java",
    texture: "textures/entity/boss/annetta/stained_priestess.png",
  },
  {
    name: "LatentAnnettaInfectionModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/LatentAnnettaInfectionModel.java",
    texture: "textures/entity/boss/annetta/latent_annetta_infection.png",
  },
  {
    name: "PutricielModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/putriciel/PutricielModel.java",
    texture: "textures/entity/boss/putriciel/putriciel.png",
  },
  {
    name: "VelorumModel",
    source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java",
    texture: "textures/entity/boss/velorum/velorum.png",
  },
];

const MODEL_SETS = {
  bosses: {
    outDir: OUT_DIR,
    models: MODELS,
  },
  morphling: {
    outDir: "src/main/resources/assets/hemomancy/models/entity/bbmodel/morphling",
    models: [
      {
        name: "MorphlingBatHeadAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingBatHeadAttachmentModel.java",
        texture: "textures/models/morphling/bat_head_attachment.png",
      },
      {
        name: "MorphlingSpiderBodyAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingSpiderBodyAttachmentModel.java",
        texture: "textures/models/morphling/spider_body_attachment.png",
      },
      {
        name: "MorphlingFungalHeadModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingFungalHeadModel.java",
        texture: "textures/models/morphling/fungal_head.png",
      },
      {
        name: "MorphlingLeechArmAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingLeechArmAttachmentModel.java",
        texture: "textures/models/morphling/leech_arm_attachment.png",
      },
      {
        name: "MorphlingChitiniteLegAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingChitiniteLegAttachmentModel.java",
        texture: "textures/models/morphling/chitinite_leg_attachment.png",
      },
      {
        name: "MorphlingSerpentLegAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingSerpentLegAttachmentModel.java",
        texture: "textures/models/morphling/serpent_leg_attachment.png",
      },
      {
        name: "MorphlingUrchinBodyAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingUrchinBodyAttachmentModel.java",
        texture: "textures/models/morphling/urchin_body_attachment.png",
      },
      {
        name: "MorphlingMothHeadAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingMothHeadAttachmentModel.java",
        texture: "textures/models/morphling/moth_head_attachment.png",
      },
      {
        name: "MorphlingCentipedeBodyAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingCentipedeBodyAttachmentModel.java",
        texture: "textures/models/morphling/centipede_body_attachment.png",
      },
      {
        name: "MorphlingPestsBodyAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingPestsBodyAttachmentModel.java",
        texture: "textures/models/morphling/pests_body_attachment.png",
      },
      {
        name: "MorphlingTickBodyAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingTickBodyAttachmentModel.java",
        texture: "textures/models/morphling/tick_body_attachment.png",
      },
      {
        name: "MorphlingMoleArmAttachmentModel",
        source: "src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingMoleArmAttachmentModel.java",
        texture: "textures/models/morphling/mole_arm_attachment.png",
      },
    ],
  },
};

const cli = parseCliArgs(process.argv.slice(2));

if (cli.options.help) {
  printUsage();
  process.exit(0);
}

const directSource = cli.options.source ?? cli.positionals[0];

if (directSource) {
  const modelSet = await directModelSet(directSource, cli.options);
  if (cli.options.check) {
    await checkGeneratedModels(modelSet);
  } else {
    await generateModels(modelSet);
  }
} else {
  const setName = cli.options.set ?? "bosses";
  const selectedSet = MODEL_SETS[setName];
  if (!selectedSet) {
    throw new Error(`Unknown model set '${setName}'. Expected one of: ${Object.keys(MODEL_SETS).join(", ")}`);
  }

  if (cli.options.check) {
    await checkGeneratedModels(selectedSet);
  } else {
    await generateModels(selectedSet);
  }
}

async function generateModels({ outDir, models }) {
  await mkdir(resolveFromRoot(outDir), { recursive: true });
  const written = [];

  for (const model of models) {
    const source = await readFile(resolveFromRoot(model.source), "utf8");
    const parsed = parseJavaModel(source, model);
    const outputPath = model.outputFile
      ? resolveFromRoot(model.outputFile)
      : path.join(resolveFromRoot(outDir), `${model.name}.bbmodel`);
    const bbmodel = toBlockbenchModel(model, parsed, outputPath);
    await mkdir(path.dirname(outputPath), { recursive: true });
    await writeFile(outputPath, `${JSON.stringify(bbmodel, null, 2)}\n`, "utf8");
    written.push(`${model.name}.bbmodel (${parsed.parts.length} parts, ${bbmodel.elements.length} cubes)`);
  }

  console.log(`Generated ${written.length} Blockbench models:`);
  for (const line of written) {
    console.log(`- ${line}`);
  }
}

async function checkGeneratedModels({ outDir, models }) {
  const resolvedOutDir = resolveFromRoot(outDir);
  const missing = [];

  for (const model of models) {
    const file = model.outputFile
      ? resolveFromRoot(model.outputFile)
      : path.join(resolvedOutDir, `${model.name}.bbmodel`);
    let rawData;
    try {
      rawData = await readFile(file, "utf8");
    } catch (error) {
      if (error?.code === "ENOENT") {
        missing.push(path.basename(file));
        continue;
      }
      throw error;
    }

    const data = JSON.parse(rawData);
    if (data.meta?.model_format !== "modded_entity") {
      throw new Error(`${model.name}.bbmodel is not a modded_entity model`);
    }
    if (data.meta?.box_uv !== true) {
      throw new Error(`${model.name}.bbmodel does not use box_uv`);
    }
    if (!Array.isArray(data.elements) || data.elements.length === 0) {
      throw new Error(`${model.name}.bbmodel has no cube elements`);
    }
    if (!Array.isArray(data.outliner) || data.outliner.length === 0) {
      throw new Error(`${model.name}.bbmodel has no outliner groups`);
    }
    const groupIds = new Set();
    collectOutlinerGroupIds(data.outliner, groupIds);
    const unresolvedGroupReference = findGroupUuidReference(data.outliner, groupIds);
    if (unresolvedGroupReference) {
      throw new Error(`${model.name}.bbmodel references nested group '${unresolvedGroupReference}' by UUID instead of embedding it`);
    }
    if (!Array.isArray(data.textures) || data.textures.length !== 1) {
      throw new Error(`${model.name}.bbmodel should have exactly one texture reference`);
    }
  }

  if (missing.length > 0) {
    throw new Error(`Missing generated model(s): ${missing.join(", ")}`);
  }

  console.log(`Validated ${models.length} generated Blockbench modded entity models.`);
}

function parseJavaModel(source, model) {
  const numericConstants = collectNumericConstants(source, model.source);
  const loopContexts = collectSimpleForLoopContexts(source, numericConstants);
  const resolutionMatch = source.match(/LayerDefinition\.create\s*\(\s*[^,]+,\s*(\d+)\s*,\s*(\d+)\s*\)/);
  if (!resolutionMatch) {
    throw new Error(`Could not find LayerDefinition.create resolution in ${model.source}`);
  }

  const parts = [];
  let searchAt = 0;
  while (true) {
    const callAt = source.indexOf(".addOrReplaceChild", searchAt);
    if (callAt === -1) {
      break;
    }

    const parentVar = parseParentVariable(source, callAt);
    const assignedVar = parseAssignedVariable(source, callAt);
    const openParen = source.indexOf("(", callAt);
    const closeParen = findMatching(source, openParen, "(", ")");
    const callBody = source.slice(openParen + 1, closeParen);
    const callArgs = splitTopLevel(callBody, ",");
    if (callArgs.length < 3) {
      throw new Error(`Could not parse addOrReplaceChild call in ${model.source} near ${callAt}`);
    }

    const loopContext = innermostLoopContextFor(loopContexts, callAt);
    const iterationLocals = loopContext?.iterations ?? [new Map()];
    for (const locals of iterationLocals) {
      const parseNumeric = (value) => parseNumber(value, numericConstants, locals);
      const name = parseNameExpression(callArgs[0], parseNumeric);
      const cubes = parseCubes(callArgs[1], parseNumeric);
      const pose = parsePose(callArgs.slice(2).join(","), parseNumeric);
      parts.push({
        name,
        varName: assignedVar ?? `${name}_${parts.length}`,
        parentVar,
        cubes,
        pose,
      });
    }
    searchAt = closeParen + 1;
  }

  if (parts.length === 0) {
    throw new Error(`No root.addOrReplaceChild parts found in ${model.source}`);
  }

  return {
    resolution: {
      width: Number(resolutionMatch[1]),
      height: Number(resolutionMatch[2]),
    },
    parts: withWorldOffsets(parts),
  };
}

function parseCubes(builderExpression, parseNumeric = parseNumber) {
  const cubes = [];
  let uv = [0, 0];
  let mirrored = false;
  const methodPattern = /\.(texOffs|mirror|addBox)\s*\(/g;
  let match;

  while ((match = methodPattern.exec(builderExpression)) !== null) {
    const method = match[1];
    const openParen = builderExpression.indexOf("(", match.index);
    const closeParen = findMatching(builderExpression, openParen, "(", ")");
    const body = builderExpression.slice(openParen + 1, closeParen);
    methodPattern.lastIndex = closeParen + 1;

    if (method === "texOffs") {
      const values = splitTopLevel(body, ",").map(parseNumeric);
      uv = [values[0], values[1]];
    } else if (method === "mirror") {
      mirrored = body.trim() === "" ? true : body.trim() !== "false";
    } else if (method === "addBox") {
      const values = splitTopLevel(body, ",");
      if (values.length < 6) {
        throw new Error(`Expected addBox to have at least 6 arguments, got ${values.length}`);
      }
      const [x, y, z, width, height, depth] = values.slice(0, 6).map(parseNumeric);
      const inflate = values.length > 6 ? parseCubeDeformation(values[6], parseNumeric) : 0;
      cubes.push({ x, y, z, width, height, depth, inflate, uv, mirrored });
    }
  }

  return cubes;
}

function parsePose(poseExpression, parseNumeric = parseNumber) {
  const pose = poseExpression.trim();
  if (pose === "PartPose.ZERO") {
    return { offset: [0, 0, 0], rotation: [0, 0, 0] };
  }

  const offsetAndRotation = pose.match(/PartPose\.offsetAndRotation\s*\(([\s\S]*)\)/);
  if (offsetAndRotation) {
    const values = splitTopLevel(offsetAndRotation[1], ",").map(parseNumeric);
    if (values.length !== 6) {
      throw new Error(`PartPose.offsetAndRotation expected 6 arguments, got ${values.length}`);
    }
    return { offset: values.slice(0, 3), rotation: values.slice(3, 6) };
  }

  const offset = pose.match(/PartPose\.offset\s*\(([\s\S]*)\)/);
  if (offset) {
    const values = splitTopLevel(offset[1], ",").map(parseNumeric);
    if (values.length !== 3) {
      throw new Error(`PartPose.offset expected 3 arguments, got ${values.length}`);
    }
    return { offset: values, rotation: [0, 0, 0] };
  }

  const rotation = pose.match(/PartPose\.rotation\s*\(([\s\S]*)\)/);
  if (rotation) {
    const values = splitTopLevel(rotation[1], ",").map(parseNumeric);
    if (values.length !== 3) {
      throw new Error(`PartPose.rotation expected 3 arguments, got ${values.length}`);
    }
    return { offset: [0, 0, 0], rotation: values };
  }

  throw new Error(`Unsupported PartPose expression: ${pose}`);
}

function toBlockbenchModel(model, parsed, outputPath) {
  const elements = [];
  const groups = [];
  const partToGroup = new Map();

  for (const [partIndex, part] of parsed.parts.entries()) {
    const partUuid = uuidFor(`${model.name}:${part.name}:part`);
    const cubeUuids = [];
    const origin = toBlockbenchOrigin(part.worldOffset);
    const rotation = toBlockbenchRotation(part.pose.rotation);

    for (const [cubeIndex, cube] of part.cubes.entries()) {
      const cubeUuid = uuidFor(`${model.name}:${part.name}:cube:${cubeIndex}`);
      elements.push(toElement(model, part, cube, cubeUuid, partIndex));
      cubeUuids.push(cubeUuid);
    }

    const group = {
      name: part.name,
      origin,
      color: partIndex % 8,
      uuid: partUuid,
      export: true,
      mirror_uv: false,
      isOpen: true,
      locked: false,
      visibility: true,
      autouv: 0,
      children: cubeUuids,
    };
    if (rotation.some((value) => Math.abs(value) > 0.000001)) {
      group.rotation = rotation;
    }
    groups.push(group);
    partToGroup.set(part.varName, group);
  }

  const childPartVars = new Set();
  for (const part of parsed.parts) {
    const group = partToGroup.get(part.varName);
    const parentGroup = partToGroup.get(part.parentVar);
    if (!group || !parentGroup) {
      continue;
    }

    parentGroup.children.push(group);
    childPartVars.add(part.varName);
  }

  const outliner = groups.filter((group) => {
    const part = parsed.parts.find((candidate) => partToGroup.get(candidate.varName) === group);
    return part && !childPartVars.has(part.varName);
  });

  return {
    meta: {
      format_version: "4.0",
      model_format: "modded_entity",
      box_uv: true,
    },
    name: model.name,
    model_identifier: model.name,
    modded_entity_version: "1.17",
    modded_entity_flip_y: true,
    visible_box: visibleBoxFor(elements),
    variable_placeholders: "",
    variable_placeholder_buttons: [],
    timeline_setups: [],
    resolution: parsed.resolution,
    elements,
    outliner,
    textures: [textureFor(model, outputPath)],
  };
}

function toElement(model, part, cube, uuid, color) {
  const [pivotX, pivotY, pivotZ] = part.worldOffset;
  const inflate = cube.inflate ?? 0;
  const from = [
    pivotX + cube.x,
    24 - (pivotY + cube.y + cube.height),
    pivotZ + cube.z,
  ];
  const to = [
    pivotX + cube.x + cube.width,
    24 - (pivotY + cube.y),
    pivotZ + cube.z + cube.depth,
  ];

  const element = {
    name: `${part.name}_${cube.uv[0]}_${cube.uv[1]}_${uuid.slice(0, 8)}`,
    rescale: false,
    locked: false,
    from: roundArray(from),
    to: roundArray(to),
    autouv: 0,
    color: color % 8,
    origin: toBlockbenchOrigin(part.worldOffset),
    uv_offset: cube.uv,
    faces: facesFor(cube),
    type: "cube",
    uuid,
  };

  if (Math.abs(inflate) > 0.000001) {
    element.inflate = round(inflate);
  }
  if (cube.mirrored) {
    element.mirror_uv = true;
  }

  return element;
}

function facesFor(cube) {
  const [u, v] = cube.uv;
  const w = cube.width;
  const h = cube.height;
  const d = cube.depth;
  return {
    north: { uv: roundArray([u + d, v + d, u + d + w, v + d + h]), texture: 0 },
    east: { uv: roundArray([u, v + d, u + d, v + d + h]), texture: 0 },
    south: { uv: roundArray([u + d + w + d, v + d, u + d + w + d + w, v + d + h]), texture: 0 },
    west: { uv: roundArray([u + d + w, v + d, u + d + w + d, v + d + h]), texture: 0 },
    up: { uv: roundArray([u + d + w, v + d, u + d, v]), texture: 0 },
    down: { uv: roundArray([u + d + w + w, v, u + d + w, v + d]), texture: 0 },
  };
}

function textureFor(model, outputPath) {
  const texturePath = resolveTexturePath(model.texture);
  return {
    path: texturePath,
    name: path.basename(model.texture),
    folder: path.dirname(model.texture).replaceAll("\\", "/"),
    namespace: "hemomancy",
    id: "0",
    particle: false,
    render_mode: "default",
    render_sides: "auto",
    frame_time: 1,
    frame_order_type: "loop",
    frame_order: "",
    frame_interpolate: false,
    visible: true,
    mode: "bitmap",
    saved: true,
    uuid: uuidFor(`${model.name}:texture:${model.texture}`),
    relative_path: relativeTexturePath(model.texture, outputPath),
  };
}

function visibleBoxFor(elements) {
  const minX = Math.min(...elements.map((element) => element.from[0]));
  const maxX = Math.max(...elements.map((element) => element.to[0]));
  const minY = Math.min(...elements.map((element) => element.from[1]));
  const maxY = Math.max(...elements.map((element) => element.to[1]));
  const width = Math.max(maxX - minX, 1) / 16;
  const height = Math.max(maxY - minY, 1) / 16;
  return [round(width), round(height), 0];
}

function toBlockbenchOrigin(offset) {
  return roundArray([offset[0], 24 - offset[1], offset[2]]);
}

function toBlockbenchRotation(rotation) {
  const [x, y, z] = rotation;
  return [radToDeg(-x), radToDeg(y), radToDeg(-z)];
}

function findMatching(text, openIndex, openChar, closeChar) {
  let depth = 0;
  let inString = false;
  let escape = false;

  for (let index = openIndex; index < text.length; index++) {
    const char = text[index];

    if (inString) {
      if (escape) {
        escape = false;
      } else if (char === "\\") {
        escape = true;
      } else if (char === "\"") {
        inString = false;
      }
      continue;
    }

    if (char === "\"") {
      inString = true;
    } else if (char === openChar) {
      depth++;
    } else if (char === closeChar) {
      depth--;
      if (depth === 0) {
        return index;
      }
    }
  }

  throw new Error(`Could not find matching ${closeChar} in text`);
}

function splitTopLevel(text, delimiter) {
  const parts = [];
  let start = 0;
  let parenDepth = 0;
  let bracketDepth = 0;
  let braceDepth = 0;
  let inString = false;
  let escape = false;

  for (let index = 0; index < text.length; index++) {
    const char = text[index];

    if (inString) {
      if (escape) {
        escape = false;
      } else if (char === "\\") {
        escape = true;
      } else if (char === "\"") {
        inString = false;
      }
      continue;
    }

    if (char === "\"") {
      inString = true;
    } else if (char === "(") {
      parenDepth++;
    } else if (char === ")") {
      parenDepth--;
    } else if (char === "[") {
      bracketDepth++;
    } else if (char === "]") {
      bracketDepth--;
    } else if (char === "{") {
      braceDepth++;
    } else if (char === "}") {
      braceDepth--;
    } else if (char === delimiter && parenDepth === 0 && bracketDepth === 0 && braceDepth === 0) {
      parts.push(text.slice(start, index).trim());
      start = index + 1;
    }
  }

  parts.push(text.slice(start).trim());
  return parts.filter((part) => part.length > 0);
}

function parseQuoted(value) {
  const match = value.match(/"([^"]+)"/);
  if (!match) {
    throw new Error(`Expected quoted string, got ${value}`);
  }
  return match[1];
}

function parseNameExpression(value, parseNumeric) {
  const terms = splitTopLevel(value, "+");
  if (terms.length === 1) {
    return parseQuoted(value);
  }

  return terms
    .map((term) => {
      const stringMatch = term.match(/^"([^"]*)"$/);
      if (stringMatch) {
        return stringMatch[1];
      }
      return String(parseNumeric(term));
    })
    .join("");
}

function parseCubeDeformation(value, parseNumeric = parseNumber) {
  if (value.trim() === "CubeDeformation.NONE") {
    return 0;
  }

  const match = value.match(/new\s+CubeDeformation\s*\(([\s\S]+)\)/);
  if (!match) {
    throw new Error(`Expected CubeDeformation, got ${value}`);
  }
  return parseNumeric(match[1]);
}

function collectNumericConstants(source, sourcePath) {
  const constants = new Map();
  const resolvedSourcePath = resolveFromRoot(sourcePath);
  const modelClassName = path.basename(resolvedSourcePath, path.extname(resolvedSourcePath));

  addNumericConstantsFromSource(source, modelClassName, constants, true);

  for (const importMatch of source.matchAll(/^\s*import\s+(?!static\b)([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+)\s*;/gm)) {
    const importName = importMatch[1];
    const className = importName.slice(importName.lastIndexOf(".") + 1);
    const importedSourcePath = resolveFromRoot(path.join("src/main/java", ...importName.split(".")) + ".java");

    let importedSource;
    try {
      importedSource = readFileSync(importedSourcePath, "utf8");
    } catch (error) {
      if (error?.code === "ENOENT") {
        continue;
      }
      throw error;
    }

    addNumericConstantsFromSource(importedSource, className, constants, false);
  }

  for (const importMatch of source.matchAll(
    /^\s*import\s+static\s+([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+)\.(\*|[A-Za-z_$][\w$]*)\s*;/gm
  )) {
    const importClassName = importMatch[1];
    const memberName = importMatch[2];
    const className = importClassName.slice(importClassName.lastIndexOf(".") + 1);
    const importedSourcePath = resolveFromRoot(path.join("src/main/java", ...importClassName.split(".")) + ".java");

    let importedSource;
    try {
      importedSource = readFileSync(importedSourcePath, "utf8");
    } catch (error) {
      if (error?.code === "ENOENT") {
        continue;
      }
      throw error;
    }

    addNumericConstantsFromSource(importedSource, className, constants, memberName === "*", memberName);
  }

  return constants;
}

function addNumericConstantsFromSource(source, className, constants, exposeSimpleNames, onlyName = null) {
  const sourceWithoutComments = stripJavaComments(source);
  const constantPattern =
    /\b(?:(?:public|private|protected)\s+)?static\s+final\s+(?:byte|short|int|long|float|double)\s+([A-Za-z_$][\w$]*)\s*=\s*([^;]+);/g;
  let match;

  while ((match = constantPattern.exec(sourceWithoutComments)) !== null) {
    const name = match[1];
    if (onlyName && onlyName !== name) {
      continue;
    }

    const value = parseNumberLiteral(match[2]);
    if (value === null) {
      continue;
    }

    constants.set(`${className}.${name}`, value);
    if (exposeSimpleNames) {
      constants.set(name, value);
    }
  }
}

function stripJavaComments(source) {
  return source.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\/\/.*$/gm, "");
}

function collectSimpleForLoopContexts(source, constants) {
  const contexts = [];
  const forPattern = /\bfor\s*\(([^)]*)\)\s*\{/g;
  let match;

  while ((match = forPattern.exec(source)) !== null) {
    const openBrace = source.indexOf("{", match.index);
    const closeBrace = findMatching(source, openBrace, "{", "}");
    const loop = parseSimpleForLoop(match[1], constants);
    if (loop) {
      contexts.push({
        start: openBrace,
        end: closeBrace,
        iterations: loop.values.map((value) => new Map([[loop.varName, value]])),
      });
    }
    forPattern.lastIndex = closeBrace + 1;
  }

  return contexts;
}

function parseSimpleForLoop(header, constants) {
  const pieces = splitTopLevel(header, ";");
  if (pieces.length !== 3) {
    return null;
  }

  const init = pieces[0].match(/(?:int|short|byte|long)\s+([A-Za-z_$][\w$]*)\s*=\s*([\s\S]+)/);
  if (!init) {
    return null;
  }

  const varName = init[1];
  const start = parseNumber(init[2], constants);
  const condition = pieces[1].match(new RegExp(`^\\s*${escapeRegExp(varName)}\\s*(<=|<|>=|>)\\s*([\\s\\S]+)$`));
  if (!condition) {
    return null;
  }

  const operator = condition[1];
  const limit = parseNumber(condition[2], constants, new Map([[varName, start]]));
  const update = pieces[2].trim();
  let step;
  const compoundUpdate = update.match(new RegExp(`^${escapeRegExp(varName)}\\s*([+-])=\\s*([\\s\\S]+)$`));
  if (compoundUpdate) {
    step = parseNumber(compoundUpdate[2], constants, new Map([[varName, start]]));
    if (compoundUpdate[1] === "-") {
      step = -step;
    }
  } else if (update === `${varName}++` || update === `++${varName}`) {
    step = 1;
  } else if (update === `${varName}--` || update === `--${varName}`) {
    step = -1;
  } else {
    return null;
  }

  if (step === 0 || !Number.isInteger(start) || !Number.isInteger(limit) || !Number.isInteger(step)) {
    return null;
  }

  const values = [];
  for (let value = start; loopCondition(value, operator, limit); value += step) {
    values.push(value);
    if (values.length > 1000) {
      throw new Error(`Refusing to expand for loop '${header}' beyond 1000 iterations`);
    }
  }

  return { varName, values };
}

function innermostLoopContextFor(contexts, index) {
  return contexts
    .filter((context) => context.start < index && index < context.end)
    .sort((left, right) => left.end - left.start - (right.end - right.start))[0] ?? null;
}

function loopCondition(value, operator, limit) {
  if (operator === "<=") {
    return value <= limit;
  }
  if (operator === "<") {
    return value < limit;
  }
  if (operator === ">=") {
    return value >= limit;
  }
  return value > limit;
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

async function directModelSet(sourcePath, options) {
  const resolvedSource = resolveFromRoot(sourcePath);
  const source = await readFile(resolvedSource, "utf8");
  const inferredName = inferModelName(source, resolvedSource);
  const name = options.name ?? inferredName;
  const outDir = options.out ?? "src/main/resources/assets/hemomancy/models/entity/bbmodel/imports";
  const texture = options.texture ?? "textures/entity/blank.png";
  const outputFile = options.output ?? null;

  return {
    outDir,
    models: [
      {
        name,
        source: resolvedSource,
        texture,
        outputFile,
      },
    ],
  };
}

function inferModelName(source, sourcePath) {
  const publicClassMatch = source.match(/\bpublic\s+(?:abstract\s+|final\s+)?class\s+([A-Za-z_$][\w$]*)\b/);
  if (publicClassMatch) {
    return publicClassMatch[1];
  }

  const sourceWithoutComments = source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "");
  const classMatch = sourceWithoutComments.match(/\bclass\s+([A-Za-z_$][\w$]*)\b/);
  return classMatch?.[1] ?? path.basename(sourcePath, path.extname(sourcePath));
}

function parseParentVariable(source, callAt) {
  let index = callAt - 1;
  while (index >= 0 && /\s/.test(source[index])) {
    index--;
  }
  let end = index + 1;
  while (index >= 0 && /[\w$]/.test(source[index])) {
    index--;
  }
  return source.slice(index + 1, end);
}

function parseAssignedVariable(source, callAt) {
  const statementStart = Math.max(source.lastIndexOf(";", callAt), source.lastIndexOf("{", callAt)) + 1;
  const prefix = source.slice(statementStart, callAt);
  const match = prefix.match(/\bPartDefinition\s+([A-Za-z_$][\w$]*)\s*=\s*[\s\S]*$/);
  return match?.[1] ?? null;
}

function collectOutlinerGroupIds(nodes, groupIds) {
  for (const node of nodes) {
    if (!node || typeof node !== "object") {
      continue;
    }
    if (typeof node.uuid === "string") {
      groupIds.add(node.uuid);
    }
    if (Array.isArray(node.children)) {
      collectOutlinerGroupIds(node.children.filter((child) => child && typeof child === "object"), groupIds);
    }
  }
}

function findGroupUuidReference(nodes, groupIds) {
  for (const node of nodes) {
    if (!node || typeof node !== "object" || !Array.isArray(node.children)) {
      continue;
    }
    const groupReference = node.children.find((child) => typeof child === "string" && groupIds.has(child));
    if (groupReference) {
      return groupReference;
    }
    const nestedReference = findGroupUuidReference(
      node.children.filter((child) => child && typeof child === "object"),
      groupIds
    );
    if (nestedReference) {
      return nestedReference;
    }
  }
  return null;
}

function withWorldOffsets(parts) {
  const byVar = new Map(parts.map((part) => [part.varName, part]));
  const resolving = new Set();

  function worldOffsetFor(part) {
    if (part.worldOffset) {
      return part.worldOffset;
    }
    if (resolving.has(part.varName)) {
      throw new Error(`Cyclic PartDefinition parent chain at ${part.varName}`);
    }

    resolving.add(part.varName);
    const parent = byVar.get(part.parentVar);
    const parentOffset = parent ? worldOffsetFor(parent) : [0, 0, 0];
    part.worldOffset = [
      parentOffset[0] + part.pose.offset[0],
      parentOffset[1] + part.pose.offset[1],
      parentOffset[2] + part.pose.offset[2],
    ];
    resolving.delete(part.varName);
    return part.worldOffset;
  }

  for (const part of parts) {
    worldOffsetFor(part);
  }

  return parts;
}

function parseCliArgs(rawArgs) {
  const options = {};
  const positionals = [];

  for (let index = 0; index < rawArgs.length; index++) {
    const arg = rawArgs[index];
    if (!arg.startsWith("--")) {
      positionals.push(arg);
      continue;
    }

    const withoutPrefix = arg.slice(2);
    const equalsAt = withoutPrefix.indexOf("=");
    if (equalsAt !== -1) {
      options[withoutPrefix.slice(0, equalsAt)] = withoutPrefix.slice(equalsAt + 1);
      continue;
    }

    const next = rawArgs[index + 1];
    if (next && !next.startsWith("--")) {
      options[withoutPrefix] = next;
      index++;
    } else {
      options[withoutPrefix] = true;
    }
  }

  return { options, positionals };
}

function printUsage() {
  console.log(`Usage:
  node tools/model_export/java_model_to_bbmodel.mjs --set=morphling
  node tools/model_export/java_model_to_bbmodel.mjs --set=morphling --check

  node tools/model_export/java_model_to_bbmodel.mjs --source path/to/Model.java [--texture textures/entity/model.png] [--out path/to/bbmodels] [--name ModelName]
  node tools/model_export/java_model_to_bbmodel.mjs "C:\\dragged\\Model.java" --texture textures/entity/model.png

Drag-and-drop:
  Drop one or more Model.java files onto tools/model_export/drop_java_model_to_bbmodel.bat.
  The wrapper exports to src/main/resources/assets/hemomancy/models/item/bbmodel by default.

Options:
  --source <path>   Java model source path. A positional file path also works for drag-and-drop.
  --name <name>     Output model name. Defaults to the Java class name.
  --texture <path>  Texture path under assets/hemomancy. Defaults to textures/entity/blank.png.
  --out <dir>       Output directory. Defaults to assets/hemomancy/models/entity/bbmodel/imports.
  --output <path>   Exact output .bbmodel file path.
  --set <name>      Generate a configured batch set: ${Object.keys(MODEL_SETS).join(", ")}.
  --check           Validate generated .bbmodel output instead of generating it.
`);
}

function resolveFromRoot(value) {
  return path.isAbsolute(value) ? value : path.join(ROOT, value);
}

function resolveTexturePath(texture) {
  return path.isAbsolute(texture)
    ? texture
    : path.join(ROOT, "src/main/resources/assets/hemomancy", texture);
}

function relativeTexturePath(texture, outputPath) {
  if (path.isAbsolute(texture)) {
    return texture;
  }
  const texturePath = resolveTexturePath(texture);
  return path.relative(path.dirname(outputPath), texturePath).replaceAll("\\", "/");
}

function parseNumber(value, constants = new Map(), locals = new Map()) {
  const normalized = value.trim();
  const literal = parseNumberLiteral(normalized);
  if (literal !== null) {
    return literal;
  }

  const constant = constants.get(normalized);
  if (typeof constant === "number") {
    return constant;
  }

  const local = locals.get(normalized);
  if (typeof local === "number") {
    return local;
  }

  const expression = parseNumericExpression(normalized, constants, locals);
  if (expression !== null) {
    return expression;
  }

  throw new Error(`Expected numeric literal or resolvable numeric constant, got ${value}`);
}

function parseNumberLiteral(value) {
  const normalized = value.trim().replace(/[fFdDlL]$/, "");
  if (!/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/.test(normalized)) {
    return null;
  }

  const number = Number(normalized);
  return Number.isFinite(number) ? number : null;
}

function parseNumericExpression(value, constants, locals) {
  const tokens = tokenizeNumericExpression(value);
  if (!tokens) {
    return null;
  }

  let index = 0;

  function parseExpression() {
    let result = parseTerm();
    while (matchSymbol("+") || matchSymbol("-")) {
      const operator = tokens[index - 1].value;
      const right = parseTerm();
      result = operator === "+" ? result + right : result - right;
    }
    return result;
  }

  function parseTerm() {
    let result = parseFactor();
    while (matchSymbol("*") || matchSymbol("/")) {
      const operator = tokens[index - 1].value;
      const right = parseFactor();
      result = operator === "*" ? result * right : result / right;
    }
    return result;
  }

  function parseFactor() {
    if (matchSymbol("+")) {
      return parseFactor();
    }
    if (matchSymbol("-")) {
      return -parseFactor();
    }
    if (matchSymbol("(")) {
      const result = parseExpression();
      if (!matchSymbol(")")) {
        throw new Error(`Expected ')' in numeric expression '${value}'`);
      }
      return result;
    }

    const token = tokens[index++];
    if (!token) {
      throw new Error(`Unexpected end of numeric expression '${value}'`);
    }
    if (token.type === "number") {
      return token.value;
    }
    if (token.type === "identifier") {
      if (locals.has(token.value)) {
        return locals.get(token.value);
      }
      if (constants.has(token.value)) {
        return constants.get(token.value);
      }
    }

    throw new Error(`Unknown numeric token '${token.value}' in expression '${value}'`);
  }

  function matchSymbol(symbol) {
    if (tokens[index]?.type === "symbol" && tokens[index].value === symbol) {
      index++;
      return true;
    }
    return false;
  }

  try {
    const result = parseExpression();
    return index === tokens.length && Number.isFinite(result) ? result : null;
  } catch {
    return null;
  }
}

function tokenizeNumericExpression(value) {
  const tokens = [];
  let index = 0;

  while (index < value.length) {
    const rest = value.slice(index);
    const whitespace = rest.match(/^\s+/);
    if (whitespace) {
      index += whitespace[0].length;
      continue;
    }

    const numberMatch = rest.match(/^(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?[fFdDlL]?/);
    if (numberMatch) {
      tokens.push({ type: "number", value: parseNumberLiteral(numberMatch[0]) });
      index += numberMatch[0].length;
      continue;
    }

    const identifierMatch = rest.match(/^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*/);
    if (identifierMatch) {
      tokens.push({ type: "identifier", value: identifierMatch[0] });
      index += identifierMatch[0].length;
      continue;
    }

    if ("+-*/()".includes(value[index])) {
      tokens.push({ type: "symbol", value: value[index] });
      index++;
      continue;
    }

    return null;
  }

  return tokens.length > 0 ? tokens : null;
}

function radToDeg(value) {
  return round((value * 180) / Math.PI);
}

function roundArray(values) {
  return values.map(round);
}

function round(value) {
  return Number(Number(value).toFixed(6));
}

function uuidFor(seed) {
  const hex = createHash("md5").update(seed).digest("hex");
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20, 32)}`;
}
