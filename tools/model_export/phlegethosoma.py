"""Author Phlegethosoma's long soft-body rig and native UV atlas; export with java_model_to_bbmodel.mjs."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
MODEL = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/phlegethosoma/PhlegethosomaModel.java'
TEXTURES = ROOT / 'src/main/resources/assets/hemomancy/textures/entity/phlegethosoma'


def f(value):
    return f'{round(value * 4) / 4:.2f}F'


def part(parent, name, boxes, pivot=(0, 0, 0), variable=None):
    cubes = 'CubeListBuilder.create()'
    for uv, bounds in boxes:
        cubes += f'.texOffs({uv[0]}, {uv[1]}).addBox(' + ', '.join(f(v) for v in bounds) + ')'
    prefix = f'PartDefinition {variable} = ' if variable else ''
    return f'        {prefix}{parent}.addOrReplaceChild("{name}", {cubes}, PartPose.offset(' + ', '.join(f(v) for v in pivot) + '));'


lines = ['''package com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSwimmingRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Articulated blood-fluke ribbon: every joint carries the following body sections. */
public final class PhlegethosomaModel extends EntityModel<PhlegethosomaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("phlegethosoma"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart[] segments = new ModelPart[PhlegethosomaSwimmingRules.SEGMENTS];

    public PhlegethosomaModel(ModelPart baked) {
        root = baked.getChild("root");
        head = root.getChild("head");
        mouth = head.getChild("mouth");
        ModelPart parent = root;
        for (int i = 0; i < segments.length; i++) {
            segments[i] = parent.getChild("segment_" + i);
            parent = segments[i];
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 18.5F, 0));''']
lines.append(part('root', 'head', [((144, 0), (-10, -5, -6, 20, 10, 12)), ((144, 0), (-8, -6, -8, 16, 12, 8))], variable='head'))
lines.append(part('head', 'mouth', [((0, 128), (-7, -4.5, -1, 14, 9, 2))], (0, 0, -10), 'mouth'))
for name, bounds in [('upper_lip',(-6,-6,-2,12,2,3)), ('lower_lip',(-6,4,-2,12,2,3)), ('left_lip',(-9,-3,-2,3,6,3)), ('right_lip',(6,-3,-2,3,6,3))]:
    lines.append(part('mouth', name, [((144, 40), bounds)]))
for x in [-6.5,6.5]:
    for y in [-3.75,3.75]:
        lines.append(part('mouth',f'lip_corner_{len(lines)}',[((144,40),(-1.5,-1.5,-1.75,3,3,3))],(x,y,0)))
for i in range(6):
    for side,y in [('upper',-4),('lower',2)]:
        lines.append(part('mouth',f'{side}_rasp_{i}',[((224,0),(-.5,0,-1.25,1,2,2))],(-5+i*2,y,-1.5)))
for side,x in [('left',-6.5),('right',4.5)]:
    for i in range(3):lines.append(part('mouth',f'{side}_rasp_{i}',[((224,0),(0,-.5,-1,2,1,2))],(x,-2+i*2,-1.5)))
lines.append(part('head','ventral_sucker',[((144,96),(-6,4.5,-2,12,1,10))]))
widths=[17,18,18,17,16,15,13.5,12,10,8,6,4]
heights=[9,10,10,9.5,9,8.5,8,7,6,5,4,2.5]
for i,(width,height) in enumerate(zip(widths,heights)):
    parent='root' if i==0 else f'segment{i-1}'
    pivot=(0,0,4.5 if i==0 else 9.5)
    lines.append(part(parent,f'segment_{i}',[],pivot,f'segment{i}'))
    lines.append(part(f'segment{i}','flesh',[((0,(i%3)*24),(-width/2,-height/2,-1,width,height,11))],variable=None))
    lines.append(part(f'segment{i}','ventral_band',[((64,0),(-width/4,height/2-.25,-.5,width/2,.5,10))]))
    lines.append(part(f'segment{i}','artery',[((112,0),(-.75,-height/2-.5,-.5,1.5,.5,10))]))
    for side in [-1,1]:
        fringe=3.5 if i<8 else 2
        x=-width/2-fringe if side<0 else width/2-.25
        lines.append(part(f'segment{i}',f'frill_{side}',[((0,88),(x,.5,-.5,fringe,.5,10))]))
        if i in [1,3,5,7]:
            x=-width/2-1 if side<0 else width/2-1
            lines.append(part(f'segment{i}',f'blood_vesicle_{side}',[((64,32),(x,-1,2,2,3,4))]))
lines.append(part('segment11','tail_ribbon',[((0,88),(-1.5,-.5,8,3,1,13)),((112,0),(-.5,-.75,8,1,.5,12))]))
lines.append('''        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(PhlegethosomaEntity entity, float limbSwing, float limbAmount, float age,
                          float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partial = age - entity.tickCount;
        float attackTime = entity.animationTime(partial);
        float movement = (entity.isIchorSwimming() || entity.isBreachAttack()) ? entity.swimAmount() : Math.min(.6F, limbAmount * 2);
        if (entity.action() == PhlegethosomaEntity.Action.LUNGE && attackTime >= 20 && attackTime < 28) movement = 1;
        root.xRot = (entity.isIchorSwimming() || entity.isBreachAttack()) ? entity.swimPitch() : 0;
        root.y += entity.isIchorSwimming() ? Mth.sin(age * .17F) * .5F : 0;
        if (entity.introTime() >= 0) head.xRot = -.15F * Mth.sin(entity.introTime() / 60F * Mth.PI);
        head.yRot = Mth.clamp(yaw * Mth.DEG_TO_RAD, -.3F, .3F);
        for (int i = 0; i < segments.length; i++) {
            ModelPart segment = segments[i];
            segment.yRot = PhlegethosomaSwimmingRules.segmentYaw(i, age, movement);
            segment.xRot = Mth.sin(age * .25F - i * .6F) * .025F * movement;
            ModelPart flesh = segment.getChild("flesh");
            if (entity.swollen() && i >= 1 && i <= 4) {
                flesh.xScale = 1.12F;
                flesh.yScale = 1.20F + Mth.sin(age * .18F - i) * .03F;
            }
            if (entity.phase() == 3) {
                flesh.yScale *= .88F;
                for (String side : new String[]{"frill_-1", "frill_1"})
                    segment.getChild(side).zRot = Mth.sin(age * .5F - i) * .12F;
            }
        }
        boolean suction = entity.action() == PhlegethosomaEntity.Action.SUCTION_WINDUP
                || entity.action() == PhlegethosomaEntity.Action.SUCTION_ACTIVE;
        mouth.xScale = suction || entity.action() == PhlegethosomaEntity.Action.BREACH ? 1.25F : 1;
        mouth.yScale = suction || entity.action() == PhlegethosomaEntity.Action.BREACH ? 1.35F : 1;
        if (entity.action() == PhlegethosomaEntity.Action.LATCH_ATTACK) mouth.z = -10 - Mth.sin(Math.min(attackTime,30)/30*Mth.PI)*3;
        if (entity.action() == PhlegethosomaEntity.Action.STAGGERED) head.zRot = Mth.sin(attackTime * .8F) * .12F;
        head.xRot += Mth.sin(entity.hurtTime * .6F) * .1F;
        if (entity.deathTime > 0) root.zRot = Math.min(1, entity.deathTime / 20F) * .9F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(pose, vertices, light, overlay, color);
    }
}
''')
MODEL.write_text('\n'.join(lines),encoding='utf-8')
TEXTURES.mkdir(parents=True,exist_ok=True)
image=Image.new('RGBA',(256,256),(72,12,26,255))
for y in range(256):
    for x in range(256):
        noise=(x*13+y*23+x*y)%7-3
        vein=(x+int(3*((y//5)%3)))%17==0
        color=(90+noise,18+noise,35+noise,255)
        if vein:color=(54+noise,9,24,255)
        if 64<=x<106 and y<24:color=(143+noise,55+noise,65+noise,255)
        if 0<=x<64 and 88<=y<114:color=(128+noise,35+noise,54+noise,255)
        if 144<=x<224 and 40<=y<88:color=(128+noise,33+noise,50+noise,255)
        if y>=128 and x<96:color=(29+noise,3,13,255)
        if x>=224 and y<24:color=(23+noise,15+noise,24+noise,255)
        if 112<=x<140 and y<16:color=(193+noise,43+noise,48+noise,255)
        image.putpixel((x,y),color)
image.save(TEXTURES/'phlegethosoma.png')
glow=Image.new('RGBA',(256,256),(0,0,0,0))
for y in range(12):
    for x in range(112,140):glow.putpixel((x,y),(168,26,31,255))
glow.save(TEXTURES/'phlegethosoma_glow.png')
print('Authored 12 articulated body joints, soft frills, terminal ribbon, oral sucker and native UV atlas.')
