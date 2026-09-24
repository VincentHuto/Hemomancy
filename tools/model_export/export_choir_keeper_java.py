"""Convert the editable Choir Keeper bbmodel to a Mojang entity model and box-UV atlas.

Blockbench's face UVs cannot be represented by CubeListBuilder directly. This
exporter packs every cube's six painted faces into the Java model's box layout.
Geometry is multiplied by four while baking so quarter-unit cubes retain one
texture pixel per quarter unit; the renderer scales the model back down.
"""

import base64
import io
import json
import math
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "src/main/resources/assets/hemomancy/models/entity/bbmodel/ChoirKeeperModel.bbmodel"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/ChoirKeeperModel.java"
ATLAS = ROOT / "src/main/resources/assets/hemomancy/textures/entity/choir_keeper_java.png"

data = json.loads(SOURCE.read_text(encoding="utf-8"))
raw = base64.b64decode(data["textures"][0]["source"].split(",", 1)[1])
source_image = Image.open(io.BytesIO(raw)).convert("RGBA")
cube_by_id = {cube["uuid"]: cube for cube in data["elements"]}
group_by_id = {group["uuid"]: group for group in data.get("groups", [])}
assert len(cube_by_id) == len(data["elements"])

atlas = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
uv_by_id = {}
cursor_x = cursor_y = row_height = 0


def face_image(cube, face, size):
    if cube["name"].startswith("tail_") and cube["name"].endswith("_eye") and face in ("up", "down"):
        # Blockbench's Box UV toggle replaces the authored eye face mapping.
        number = int(cube["name"].split("_")[1])
        x = 16 if number in (1, 7, 12) else 0
        uv = [x, 16, x + 16, 32]
    else:
        uv = cube["faces"][face]["uv"]
    x0, y0, x1, y1 = (round(v) for v in uv)
    patch = source_image.crop((min(x0, x1), min(y0, y1), max(x0, x1), max(y0, y1)))
    if x1 < x0:
        patch = patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if y1 < y0:
        patch = patch.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    return patch.resize(size, Image.Resampling.NEAREST)


for cube in data["elements"]:
    x, y, z = [round((hi - lo) * 4) for lo, hi in zip(cube["from"], cube["to"])]
    assert min(x, y, z) >= 1
    width, height = 2 * (x + z), y + z
    if cursor_x + width + 2 > atlas.width:
        cursor_x = 0
        cursor_y += row_height + 2
        row_height = 0
    assert cursor_y + height + 2 <= atlas.height, "Choir Keeper UV atlas is full"
    u, v = cursor_x + 1, cursor_y + 1
    uv_by_id[cube["uuid"]] = (u, v)
    faces = {
        "east": (u, v + z, z, y),
        "north": (u + z, v + z, x, y),
        "west": (u + z + x, v + z, z, y),
        "south": (u + 2 * z + x, v + z, x, y),
        "up": (u + z, v, x, z),
        "down": (u + z + x, v, x, z),
    }
    for face, (fx, fy, fw, fh) in faces.items():
        atlas.paste(face_image(cube, face, (fw, fh)), (fx, fy))
    cursor_x += width + 2
    row_height = max(row_height, height)

atlas_height = 1 << (cursor_y + row_height + 2 - 1).bit_length()
atlas = atlas.crop((0, 0, 1024, atlas_height))
ATLAS.parent.mkdir(parents=True, exist_ok=True)
atlas.save(ATLAS)


def num(value):
    return f"{value * 4:g}F"


def radians(value):
    return f"{math.radians(value):.6f}F"


lines = []
counter = 0
converted_cubes = set()


def emit_group(node, parent_var, parent_origin):
    global counter
    counter += 1
    var = f"part{counter}"
    group = group_by_id.get(node["uuid"], node)
    origin = group["origin"]
    delta = [origin[i] - parent_origin[i] for i in range(3)]
    rotation = group.get("rotation", [0, 0, 0])
    lines.append(
        f'        PartDefinition {var} = {parent_var}.addOrReplaceChild("{group["name"]}", '
        f'CubeListBuilder.create(), PartPose.offsetAndRotation('
        f'{num(delta[0])}, {num(-delta[1])}, {num(delta[2])}, '
        f'{radians(-rotation[0])}, {radians(rotation[1])}, {radians(-rotation[2])}));'
    )
    for child in node["children"]:
        if isinstance(child, dict):
            emit_group(child, var, origin)
            continue
        cube = cube_by_id[child]
        assert child not in converted_cubes, cube["name"]
        converted_cubes.add(child)
        counter += 1
        cube_var = f"part{counter}"
        center = cube["origin"]
        delta = [center[i] - origin[i] for i in range(3)]
        low, high = cube["from"], cube["to"]
        rotation = cube.get("rotation", [0, 0, 0])
        u, v = uv_by_id[child]
        lines.append(
            f'        {var}.addOrReplaceChild("{cube["name"]}_{child[:8]}", '
            f'CubeListBuilder.create().texOffs({u}, {v}).addBox('
            f'{num(low[0] - center[0])}, {num(center[1] - high[1])}, {num(low[2] - center[2])}, '
            f'{num(high[0] - low[0])}, {num(high[1] - low[1])}, {num(high[2] - low[2])}, '
            f'new CubeDeformation(0.0F)), PartPose.offsetAndRotation('
            f'{num(delta[0])}, {num(-delta[1])}, {num(delta[2])}, '
            f'{radians(-rotation[0])}, {radians(rotation[1])}, {radians(-rotation[2])}));'
        )


# LivingEntityRenderer translates after the 0.25 scale; the talon soles must
# therefore land at Java model Y=24 before scaling.
talon_sole_y = min(cube["from"][1] for cube in data["elements"] if "talon" in cube["name"])
for node in data["outliner"]:
    emit_group(node, "root", [0, talon_sole_y + 6, 0])
assert converted_cubes == cube_by_id.keys(), "Every Blockbench cube must appear in the Java model"

java = '''package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
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

/** Generated from ChoirKeeperModel.bbmodel by export_choir_keeper_java.py. */
public final class ChoirKeeperModel extends EntityModel<ChoirKeeperEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("choir_keeper"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tailFan;
    private final ModelPart[] eyeFeathers = new ModelPart[15];

    public ChoirKeeperModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tailFan = body.getChild("tail_fan");
        for (int i = 0; i < eyeFeathers.length; i++) {
            eyeFeathers[i] = tailFan.getChild(String.format("eye_feather_%02d", i));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
''' + "\n".join(lines) + f'''
        return LayerDefinition.create(mesh, {atlas.width}, {atlas.height});
    }}

    @Override
    public void setupAnim(ChoirKeeperEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {{
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float flare = entity.getFlareAmount(partialTick);
        float flight = entity.getFlightPoseAmount(partialTick);
        float dive = Mth.clamp((float) -entity.getDeltaMovement().y * 0.75F, -0.15F, 0.30F);
        body.xRot = flight * (0.58F + dive);
        neck.xRot = flight * 0.55F;
        neck.z = -14.0F - flight * 8.0F;
        head.xRot = -flight * 0.32F;
        leftLeg.xRot = flight * 0.70F;
        rightLeg.xRot = flight * 0.70F;
        tailFan.xRot = Mth.lerp(flare, -0.3926991F, 0.9599311F);
        for (int i = 0; i < eyeFeathers.length; i++) {{
            float restYaw = (i - 7) * 0.05235988F;
            eyeFeathers[i].yRot = restYaw * (1.0F + flare * 2.35F);
        }}
        head.yRot = Mth.clamp(netHeadYaw, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
        float flap = flight * Mth.sin(ageInTicks * 1.2F) * 0.42F;
        leftWing.zRot = 0.10472F + flap;
        rightWing.zRot = -0.10472F - flap;
    }}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int packedColor) {{
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }}

}}
'''
JAVA.parent.mkdir(parents=True, exist_ok=True)
JAVA.write_text(java, encoding="utf-8")
print(f"Converted {len(data['elements'])} cubes to {JAVA}; atlas {atlas.width}x{atlas.height}")
