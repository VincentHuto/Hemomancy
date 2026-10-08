package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import java.util.*;

public class PelagicCreatureModel<T extends Mob> extends HierarchicalModel<T> {
    public enum Style {
        CHITON("chiton", false, false), PYROSOME("pyrosome", true, true), HERRING("pelagic_herring", false, false),
        SIPHONOPHORE("siphonophore", true, true), COMB_JELLY("bloody_belly_comb_jelly", true, true), HAGFISH("hagfish", true, false),
        ANEMONE("tidepool_anemone", false, false), BONE_WORM("bone_worm_colony", false, false), TUBE_WORM("giant_tube_worm_colony", false, false);
        public final String id;
        public final boolean translucent, luminous;
        Style(String id, boolean translucent, boolean luminous) { this.id = id; this.translucent = translucent; this.luminous = luminous; }
        public ModelLayerLocation layer() { return new ModelLayerLocation(Hemomancy.rloc(id), "main"); }
    }
    private final ModelPart body;
    private final Style style;
    private final List<ModelPart> articulated = new ArrayList<>();
    public PelagicCreatureModel(ModelPart root, Style style) {
        this.body = root.getChild("body"); this.style = style;
        if (style == Style.HAGFISH) {
            ModelPart parent = body;
            for (int i = 0; i < 6; i++) { parent = parent.getChild("tail" + i); articulated.add(parent); }
        } else if (style == Style.SIPHONOPHORE) {
            for (int i = 0; i < 6; i++) {
                ModelPart parent = body;
                for (int j = 0; j < 4; j++) { parent = parent.getChild("filament" + i + "_" + j); articulated.add(parent); }
            }
        }
    }
    @Override public ModelPart root() { return body; }
    private void reset() { body.getAllParts().forEach(ModelPart::resetPose); }
    @Override public void setupAnim(T entity, float swing, float amount, float age, float yaw, float pitch) {
        reset();
        switch (style) {
            case CHITON -> {
                var chiton = (ChitonEntity)entity;
                body.yScale = chiton.isClamped() ? .55F : 1;
                if (chiton.isGrazing()) body.getChild("mouth").z -= .5F + Mth.sin(age * .35F) * .5F;
                for (int i = 0; i < 8; i++) body.getChild("plate" + i).xRot = chiton.isClamped() ? 0 : Mth.sin(age * .16F - i * .6F) * .035F;
            }
            case PYROSOME -> {
                for (int i = 0; i < 6; i++) {
                    var ring = body.getChild("ring" + i);
                    ring.xScale = ring.zScale = .96F + .055F * Mth.sin(age * .12F - i * .8F);
                }
                body.zRot = Mth.sin(age * .025F) * .08F;
            }
            case HERRING -> {
                body.getChild("tail").yRot = Mth.sin(age * .7F) * .5F;
                body.getChild("finn1").zRot -= Mth.sin(age * .4F) * .12F;
                body.getChild("fin1").zRot += Mth.sin(age * .4F) * .12F;
            }
            case SIPHONOPHORE -> {
                float feeding = ((SiphonophoreEntity)entity).actionTicks() > 0 ? .23F : 0;
                for (int i = 0; i < articulated.size(); i++) {
                    articulated.get(i).xRot = Mth.sin(age * .055F - i % 4 * .7F + i / 4F) * .10F + feeding;
                    articulated.get(i).zRot = Mth.cos(age * .045F - i * .25F) * .08F;
                }
                for (int i = 0; i < 5; i++) {
                    var bell = body.getChild("bell" + i);
                    bell.xScale = bell.zScale = 1 + Mth.sin(age * .13F - i) * .1F;
                }
                body.zRot = Mth.sin(age * .025F) * .07F;
            }
            case COMB_JELLY -> {
                var jelly = (BloodyBellyCombJellyEntity)entity;
                float partialTick = Mth.clamp(age-entity.tickCount, 0, 1);
                body.xRot = jelly.swimmingPose().leanRadians(partialTick);
                body.yRot = Mth.wrapDegrees(jelly.swimmingPose().headingDegrees(partialTick)
                        - Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)) * Mth.DEG_TO_RAD;
                body.xScale = body.zScale = 1 + Mth.sin(age * .09F) * .035F;
                body.getChild("lobe0").zRot += Mth.sin(age * .12F) * .13F;
                body.getChild("lobe1").zRot -= Mth.sin(age * .12F) * .13F;
                body.getChild("gut").yScale = .96F + Mth.sin(age * .1F) * .04F;
            }
            case HAGFISH -> {
                var hagfish = (HagfishEntity)entity;
                float strength = hagfish.isSliming() ? .3F : .18F;
                if (hagfish.isFeeding()) body.xRot = .08F + Mth.sin(age * .5F) * .05F;
                for (int i = 0; i < articulated.size(); i++) articulated.get(i).yRot = Mth.sin(age * .28F - i * .65F) * strength;
                body.getChild("slime").visible = hagfish.isSliming();
                for (int i = 0; i < 4; i++) body.getChild("barbel" + i).yRot += Mth.sin(age * .25F + i) * .13F;
            }
            default -> animateColony(age, 1);
        }
    }
    public void animateColony(float age, float extension) {
        animateColony(age, extension, 5);
    }
    public void animateColony(float age, float extension, int worms) {
        reset();
        int count = style == Style.ANEMONE ? 8 : 5;
        for (int i = 0; i < count; i++) {
            if (style == Style.BONE_WORM || style == Style.TUBE_WORM) body.getChild("tube" + i).visible = i < worms;
            ModelPart plume = style == Style.ANEMONE ? body.getChild("plume" + i) : body.getChild("tube" + i).getChild("plume" + i);
            plume.yScale = extension;
            plume.xRot += Mth.sin(age * .055F + i) * .1F * extension;
            plume.zRot += Mth.cos(age * .045F + i * .7F) * .09F * extension;
        }
    }
    public void renderGlow(PoseStack pose, VertexConsumer vertices, float age) {
        if (style == Style.PYROSOME || style == Style.COMB_JELLY) {
            pose.pushPose(); body.translateAndRotate(pose);
            int count = style == Style.PYROSOME ? 6 : 8;
            for (int i = 0; i < count; i++) {
                float wave = .35F + .65F * (.5F + .5F * Mth.sin(age * (style == Style.PYROSOME ? .12F : .25F) - i * .8F));
                int color = ((int)(255 * wave) << 24) | 0xffffff;
                body.getChild((style == Style.PYROSOME ? "ring" : "comb") + i).render(pose, vertices, LightTexture.FULL_BRIGHT, 0, color);
            }
            pose.popPose();
        } else body.render(pose, vertices, LightTexture.FULL_BRIGHT, 0);
    }
}
