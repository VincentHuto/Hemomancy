package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.CachedMeshModelView;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.worldgen.arbor.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Authored-procedural Arbor presentation. The renderer builds one gnarled organism:
 * textured heartwood and roots, six shared helical limbs, forked canopy boughs,
 * short hanging stems, leaves, and a distinct fruit anatomy for every skill family.
 */
public final class ArborOfWillRenderer extends EntityRenderer<ArborOfWillEntity> {
    // The animated blood-wood log atlas produced broad checker bands when wrapped
    // around a procedural tube. This finer grain remains legible after tinting.
    private static final ResourceLocation BARK = Hemomancy.rloc("textures/block/dark_oak_log.png");
    private static final ResourceLocation LEAVES = Hemomancy.rloc("textures/block/mycelium_top.png");
    private static final ResourceLocation FUNGUS = Hemomancy.rloc("textures/block/erythrocytic_mycelium_top.png");
    private static final RenderType BARK_TYPE = RenderType.entityCutoutNoCull(BARK);
    private static final RenderType MYCELIUM_TYPE = RenderType.entityCutoutNoCull(LEAVES);
    private static final RenderType FUNGUS_TYPE = RenderType.entityCutoutNoCull(FUNGUS);
    private static final int BARK_TINT = 0xFF9B4E4B;
    private static final int DARK_BARK_TINT = 0xFF572B32;
    private static final Map<String, Integer> FAMILY_COLORS = Map.of(
            "core", 0xFFDA2636, "living_staff", 0xFFE3A62F, "mycelial", 0xFF4FAE68,
            "scars", 0xFFC8D1DC, "covenant", 0xFFD64D75, "summons", 0xFF376FC7);

    public ArborOfWillRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 2.7F;
    }

    @Override
    public boolean shouldRender(ArborOfWillEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return entity.shouldRender(cameraX, cameraY, cameraZ)
                && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(entity.chamberRadius(), 5.0D, entity.chamberRadius()));
    }

    @Override
    public void render(ArborOfWillEntity arbor, float yaw, float partial, PoseStack stack,
            MultiBufferSource buffers, int light) {
        stack.pushPose();
        PoseStack.Pose pose = stack.last();
        float time = arbor.tickCount + partial;
        float height = (float) ArborOfWillVisualRules.treeHeight(arbor.degree());
        float rootRadius = (float) ArborOfWillVisualRules.rootRadius(arbor.degree(), arbor.chamberRadius());
        int whorls = ArborOfWillVisualRules.visibleWhorls(arbor.degree());

        SkillProgress progress = SkillProgressClientCache.current();
        List<ArborOfWillLayout.FruitPlacement> placements = ArborSkillPresentation.placements(arbor.chamberRadius());
        int unlocked = (int) placements.stream().map(p -> SkillPointInit.getById(p.skillId()))
                .filter(progress::isUnlocked).count();
        float foliage = (float) ArborOfWillVisualRules.foliageFraction(unlocked, placements.size(),
                arbor.pomesConsumed(), arbor.degree() >= 8);
        float pomeHealth = 1.0F - ArborOfWillVisualRules.woundCount(arbor.pomesConsumed()) / 9.0F * .92F;
        foliage = Math.max(foliage, whorls / 7.0F * .45F * pomeHealth);

        ArborMeshCache.renderStatic(stack, ArborStaticVisualKey.create(
                arbor.degree(), arbor.chamberRadius(), foliage, light), placements);
        renderSkillOrchard(arbor, stack, buffers, placements, progress, whorls, time, partial);
        renderHeartPulse(pose, buffers, height, time);
        renderPomeWounds(arbor, pose, buffers, height);
        if (arbor.degree() >= 8) renderApotheosisGlow(pose, buffers, height, time);

        stack.popPose();
        super.render(arbor, yaw, partial, stack, buffers, light);
    }

    public static void clearCaches() {
        ArborMeshCache.clear();
    }

    private static void renderRootsAndHeartwood(PoseStack.Pose pose, MeshConsumers consumers, int light,
            float height, float rootRadius, float foliage) {
        for (int i = 0; i < 7; i++) {
            texturedTerminalTube(pose, consumers.bark(), light,
                    ArborCanopyGeometry.root(i, rootRadius), .34F, .035F, DARK_BARK_TINT, 1.8F,.42F);
        }

        List<ArborCanopyGeometry.Point> trunk = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            double t = i / 13.0;
            trunk.add(new ArborCanopyGeometry.Point(
                    Math.sin(t * 4.8) * (.06 + .08 * t), height * t * .82,
                    Math.cos(t * 4.1) * (.05 + .06 * t)));
        }
        float trunkCrownSize=.82F+foliage*.72F;
        texturedTerminalTube(pose, consumers.bark(), light,
                trunk, .61F, .22F, BARK_TINT, 4.0F,trunkCrownSize*.18F);
        if (foliage > .06F) {
            renderTerminalCrown(pose,consumers.mycelium(),light,trunk,FAMILY_COLORS.get("core"),
                    trunkCrownSize,foliage,90);
        }

        // Strangler-fig cords visibly braid around the shared heartwood instead of stacking grey capsules.
        for (int strand = 0; strand < 4; strand++) {
            List<ArborCanopyGeometry.Point> cord = new ArrayList<>();
            for (int i = 0; i < 13; i++) {
                double t = i / 12.0;
                double angle = strand * Math.PI * .5 + t * Math.PI * 2.35;
                double radius = .32 - t * .12;
                cord.add(new ArborCanopyGeometry.Point(Math.cos(angle) * radius,
                        .08 + height * t * .77, Math.sin(angle) * radius));
            }
            texturedTube(pose, consumers.bark(), light,
                    cord, .19F, .070F, strand % 2 == 0 ? BARK_TINT : DARK_BARK_TINT, 3.0F);
        }

        // Split crown creates the heart-shaped aperture described in the design.
        for (int side : new int[]{-1, 1}) {
            List<ArborCanopyGeometry.Point> leader = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                double t = i / 6.0;
                leader.add(new ArborCanopyGeometry.Point(side * (.12 + .58 * Math.sin(t * Math.PI * .72)),
                        height * (.57 + .40 * t), .03 + .18 * Math.sin(t * Math.PI)));
            }
            float leaderCrownSize=.68F+foliage*.48F;
            texturedTerminalTube(pose, consumers.bark(), light,
                    leader, .22F, .055F, BARK_TINT, 2.2F,leaderCrownSize*.18F);
            if (foliage > .06F) {
                renderTerminalCrown(pose,consumers.mycelium(),light,leader,FAMILY_COLORS.get("core"),
                        leaderCrownSize,foliage,96+side);
            }
        }
    }

    private static List<List<ArborCanopyGeometry.Point>> renderBraidedCanopy(PoseStack.Pose pose,
            MeshConsumers consumers, int light, float height, float rootRadius, int whorls,
            float foliage) {
        List<List<ArborCanopyGeometry.Point>> result = new ArrayList<>();
        int visiblePoints = Math.max(4, 4 + Math.round(whorls / 7.0F * 11.0F));
        for (int family = 0; family < 6; family++) {
            String familyName = ArborOfWillLayout.orderedFamilies().get(family);
            int familyColor = FAMILY_COLORS.get(familyName);
            List<ArborCanopyGeometry.Point> complete = ArborCanopyGeometry.familyLimb(
                    family, Math.max(2.6, height), Math.max(1.5, rootRadius * .93));
            List<ArborCanopyGeometry.Point> limb = complete.subList(0, Math.min(visiblePoints, complete.size()));
            result.add(List.copyOf(limb));

            float limbCrownSize=.70F+foliage*.54F;
            texturedTerminalTube(pose, consumers.bark(), light,
                    limb, .19F, .045F, mix(BARK_TINT, familyColor, .18F), 3.1F,
                    limbCrownSize*.18F);
            List<ArborCanopyGeometry.Point> cambium = ArborCanopyGeometry.surfaceVein(
                    limb, .19, .045, .027, .010, .004);
            texturedTube(pose, consumers.mycelium(),
                    LightTexture.pack(13, 9), cambium, .027F, .010F,
                    mix(familyColor, 0xFFFFFFFF, .08F), 3.1F);

            if (limb.size() >= 11) {
                for (int sign : new int[]{-1, 1}) {
                    List<ArborCanopyGeometry.Point> fork = canopyFork(limb, sign, rootRadius * .30);
                    float forkCrownSize=.58F+foliage*.48F;
                    texturedTerminalTube(pose, consumers.bark(), light,
                            fork, .080F, .022F, mix(BARK_TINT, familyColor, .13F), 1.7F,
                            forkCrownSize*.18F);
                    renderLeavesAlong(pose, consumers.mycelium(), light, fork, familyColor, foliage, family * 7 + sign);
                    if (foliage > .10F) {
                        renderTerminalCrown(pose,consumers.mycelium(),light,fork,familyColor,
                                forkCrownSize,foliage,family*31+sign);
                    }
                }
            }
            renderLeavesAlong(pose, consumers.mycelium(), light, limb, familyColor, foliage, family);
            if (foliage > .06F) {
                renderTerminalCrown(pose,consumers.mycelium(),light,limb,familyColor,
                        limbCrownSize,foliage,family*37);
            }
        }
        return result;
    }

    private static void renderStaticOrchard(PoseStack.Pose pose, MeshConsumers consumers, int light,
            List<ArborOfWillLayout.FruitPlacement> placements, List<List<ArborCanopyGeometry.Point>> limbs,
            int whorls, float foliage) {
        int visibleIndex = 0;
        for (ArborOfWillLayout.FruitPlacement fruit : placements) {
            if (fruit.whorl() > Math.max(1, whorls)) continue;
            int familyIndex = Math.max(0, ArborOfWillLayout.orderedFamilies().indexOf(fruit.family()));
            int color = FAMILY_COLORS.getOrDefault(fruit.family(), FAMILY_COLORS.get("core"));
            ArborCanopyGeometry.Point fruitPoint = new ArborCanopyGeometry.Point(fruit.x(), fruit.y(), fruit.z());
            List<ArborCanopyGeometry.Point> limb = limbs.get(Math.min(familyIndex, limbs.size() - 1));
            List<ArborCanopyGeometry.Point> hanging = ArborCanopyGeometry.hangingStem(fruitPoint, .56, .26);
            List<ArborCanopyGeometry.Point> twig = curvedTwig(nearest(limb, hanging.get(0)), hanging.get(0), fruit.skillId());
            texturedTube(pose, consumers.bark(), light,
                    twig, .045F, .018F, mix(BARK_TINT, color, .14F), 1.0F);
            texturedTube(pose, consumers.bark(), light,
                    hanging, .025F, .010F, mix(color, 0xFF6B452B, .38F), .75F);
            if (visibleIndex++ < Math.round(placements.size() * foliage)) {
                renderLeafCluster(pose, consumers.mycelium(), light,
                        new ArborCanopyGeometry.Point(fruit.x() - .20, fruit.y() + .42, fruit.z() + .10),
                        color, .64F, Math.max(.16F, foliage * .72F), fruit.skillId());
            }
        }
    }

    private static void renderSkillOrchard(ArborOfWillEntity arbor, PoseStack stack,
            MultiBufferSource buffers, List<ArborOfWillLayout.FruitPlacement> placements,
            SkillProgress progress, int whorls, float time, float partial) {
        for (ArborOfWillLayout.FruitPlacement fruit : placements) {
            if (fruit.whorl() > Math.max(1, whorls)) continue;
            SkillPoint skill = SkillPointInit.getById(fruit.skillId());
            if (skill == null) continue;
            int color = FAMILY_COLORS.getOrDefault(fruit.family(), FAMILY_COLORS.get("core"));
            ArborOfWillVisualRules.GrowthState state = ArborOfWillVisualRules.growthState(
                    progress.isUnlocked(skill), progress.isEnabled(skill), skill.getRequiredDegree(), arbor.degree());
            float scale = state == ArborOfWillVisualRules.GrowthState.RIPE_FRUIT
                    || state == ArborOfWillVisualRules.GrowthState.CLOSED_CALYX
                    ? (float)ArborOfWillVisualRules.fruitScale(progress.getLevel(skill), skill.getMaxLevels()) : .50F;
            scale *= ArborGrowthAnimations.growthScale(skill, progress, arbor.tickCount, partial);
            scale *= 1.0F + .035F * (float)Math.sin(time * .085F + fruit.skillId());
            ArborFruitMeshKey key = ArborFruitMeshKey.create(fruit.family(), state,
                    progress.getLevel(skill), skill.getMaxLevels());
            stack.pushPose();
            stack.translate(fruit.x(), fruit.y(), fruit.z());
            stack.scale(scale, scale, scale);
            ArborMeshCache.renderFruit(stack, key);
            stack.popPose();
            renderFruitGlow(buffers, stack.last(), fruit, key, scale, color);
        }
    }

    private static void emitFruitBody(PoseStack.Pose pose, MeshConsumers consumers, ArborFruitMeshKey key) {
        ArborCanopyGeometry.Point p = new ArborCanopyGeometry.Point(0, 0, 0);
        int color = FAMILY_COLORS.getOrDefault(key.family(), FAMILY_COLORS.get("core"));
        if (key.state() == ArborOfWillVisualRules.GrowthState.DEGREE_SEALED_BUD
                || key.state() == ArborOfWillVisualRules.GrowthState.DORMANT_BUD) {
            emitBudBody(pose, consumers.mycelium(), p, color, key.state());
        } else if (key.state() == ArborOfWillVisualRules.GrowthState.CLOSED_CALYX) {
            emitCalyxBody(pose, consumers.mycelium(), p);
        } else {
            emitFamilyFruitBody(pose, consumers, p, key.family(), color);
        }
    }

    private static void emitFamilyFruitBody(PoseStack.Pose pose, MeshConsumers consumers,
            ArborCanopyGeometry.Point p, String family, int color) {
        ArborFruitGeometry.Profile profile = ArborFruitGeometry.profile(family);
        int fruitLight = LightTexture.pack(13, 10);
        float r = .24F;
        switch (profile.shape()) {
            case HEART_POME -> {
                texturedEllipsoid(pose, consumers.mycelium(), fruitLight, p.x() - r*.38, p.y()+r*.20, p.z(), r*.72, r*.72, r*.68, color);
                texturedEllipsoid(pose, consumers.mycelium(), fruitLight, p.x() + r*.38, p.y()+r*.20, p.z(), r*.72, r*.72, r*.68, color);
                texturedEllipsoid(pose, consumers.mycelium(), fruitLight, p.x(), p.y()-r*.28, p.z(), r*.84, r*.92, r*.76, color);
            }
            case HOOKED_PEAR -> {
                texturedEllipsoid(pose, consumers.mycelium(), fruitLight, p.x(), p.y()-r*.24, p.z(), r*.78, r*1.05, r*.72, color);
                texturedEllipsoid(pose, consumers.mycelium(), fruitLight, p.x(), p.y()+r*.55, p.z(), r*.48, r*.62, r*.46, color);
                List<ArborCanopyGeometry.Point> hook = List.of(
                        new ArborCanopyGeometry.Point(p.x(),p.y()+r*1.03,p.z()),
                        new ArborCanopyGeometry.Point(p.x()+r*.18,p.y()+r*1.28,p.z()),
                        new ArborCanopyGeometry.Point(p.x()+r*.42,p.y()+r*1.23,p.z()));
                texturedTube(pose, consumers.bark(), fruitLight,
                        hook, .035F, .018F, color, .4F);
            }
            case THREAD_BERRIES -> {
                double[][] offsets = {{0,0,0},{-.55,.26,.06},{.52,.22,-.08},{-.26,-.46,.10},{.28,-.50,-.06}};
                for (double[] o : offsets) texturedEllipsoid(pose,consumers.mycelium(),fruitLight,
                        p.x()+o[0]*r,p.y()+o[1]*r,p.z()+o[2]*r,r*.43,r*.48,r*.43,color);
            }
            case JOINED_FRUIT -> {
                texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x()-r*.42,p.y(),p.z(),r*.67,r*.92,r*.66,color);
                texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x()+r*.42,p.y(),p.z(),r*.67,r*.92,r*.66,color);
                texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x(),p.y()+r*.12,p.z(),r*.28,r*.42,r*.32,mix(color,0xFFFFFFFF,.22F));
            }
            case FISSURED_NUT -> texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x(),p.y(),p.z(),r*.94,r*.72,r*.82,color);
            case GILLED_POD -> {
                texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x(),p.y()-r*.25,p.z(),r*.52,r*.82,r*.50,color);
                texturedEllipsoid(pose,consumers.mycelium(),fruitLight,p.x(),p.y()+r*.36,p.z(),r*1.12,r*.25,r*1.05,mix(color,0xFFE8D7B0,.26F));
            }
        }
    }

    private static void renderFruitGlow(MultiBufferSource buffers, PoseStack.Pose pose,
            ArborOfWillLayout.FruitPlacement fruit, ArborFruitMeshKey key, float scale, int color) {
        if (key.state() != ArborOfWillVisualRules.GrowthState.RIPE_FRUIT) return;
        float r = .24F * scale;
        if (ArborFruitGeometry.profile(key.family()).shape() == ArborFruitGeometry.Shape.FISSURED_NUT) {
            for (int i=0;i<3;i++) {
                float dx=(i-1)*r*.27F;
                glowScar(buffers,pose,fruit.x()+dx,fruit.y()-r*.48,fruit.z()-r*.72,
                        fruit.x()-dx*.45,fruit.y()+r*.46,fruit.z()-r*.76);
            }
        } else if (ArborFruitGeometry.profile(key.family()).shape() == ArborFruitGeometry.Shape.GILLED_POD) {
            for(int i=0;i<6;i++) {
                double a=i*Math.PI/3.0;
                glowScar(buffers,pose,fruit.x(),fruit.y()+r*.27,fruit.z(),
                        fruit.x()+Math.cos(a)*r*.88,fruit.y()+r*.26,fruit.z()+Math.sin(a)*r*.88);
            }
        }
        int chambers = ArborOfWillVisualRules.seedChambers(key.level(), key.maxLevel());
        for (int i=0;i<chambers;i++) {
            double angle=i*Math.PI*2.0/chambers;
            colorEllipsoid(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW),pose.pose(),
                    (float)(fruit.x()+Math.cos(angle)*r*.48), (float)(fruit.y()-r*.08), (float)(fruit.z()+Math.sin(angle)*r*.48),
                    r*.075F,r*.075F,r*.075F, red(color),green(color),blue(color),.80F);
        }
    }

    private static void emitBudBody(PoseStack.Pose pose, VertexConsumer out,
            ArborCanopyGeometry.Point p, int familyColor, ArborOfWillVisualRules.GrowthState state) {
        int color = state == ArborOfWillVisualRules.GrowthState.DEGREE_SEALED_BUD ? 0xFFD1C4B2 : 0xFF6A303B;
        int budLight = LightTexture.pack(10, 7);
        float r=.14F;
        texturedEllipsoid(pose,out,budLight,p.x(),p.y(),p.z(),r*.70,r*1.05,r*.70,mix(color,familyColor,.24F));
        for(int i=0;i<3;i++) {
            double a=i*Math.PI*2/3.0;
            texturedEllipsoid(pose,out,budLight,p.x()+Math.cos(a)*r*.46,p.y()-r*.18,p.z()+Math.sin(a)*r*.46,
                    r*.34,r*.74,r*.24,mix(color,0xFF6E4B37,.25F));
        }
    }

    private static void emitCalyxBody(PoseStack.Pose pose, VertexConsumer out, ArborCanopyGeometry.Point p) {
        int fruitLight = LightTexture.pack(12, 9);
        float r=.22F;
        for(int i=0;i<10;i++) {
            double a=i*Math.PI*2/10.0;
            texturedEllipsoid(pose,out,fruitLight,p.x()+Math.cos(a)*r*.55,p.y()+Math.sin(a*2)*r*.18,
                    p.z()+Math.sin(a)*r*.55,r*.22,r*.62,r*.16,0xFFAA662B);
        }
        texturedEllipsoid(pose,out,fruitLight,p.x(),p.y(),p.z(),r*.50,r*.52,r*.50,0xFF6A3024);
    }

    private static void renderLeavesAlong(PoseStack.Pose pose, VertexConsumer out, int light,
            List<ArborCanopyGeometry.Point> path, int color, float foliage, int seed) {
        int sprays=foliage>.68F?2:foliage>.24F?1:0;
        for(int spray=0;spray<sprays;spray++) {
            int i=Math.min(path.size()-2,Math.max(2,(int)(path.size()*(spray==0?.68F:.84F))));
            ArborCanopyGeometry.Point p=path.get(i);
            renderLeafCluster(pose,out,light,p,
                    color,.58F+.24F*foliage,foliage,seed+i*13);
        }
    }

    private static void renderLeafCluster(PoseStack.Pose pose, VertexConsumer out, int light,
            ArborCanopyGeometry.Point p, int familyColor, float size, float foliage, int seed) {
        int leafLight=LightTexture.pack(11,8);
        float bud=ArborCanopyGeometry.foliageBudRadius(size,foliage);
        int petalColor=mix(0xFF642638,familyColor,.24F);
        int darkPetalColor=mix(petalColor,0xFF160A12,.30F);
        texturedEllipsoid(pose,out,light,p.x(),p.y(),p.z(),bud,bud*.90F,bud,darkPetalColor);
        List<ArborCanopyGeometry.Leaflet> crown=ArborCanopyGeometry.foliageCrown(p,size,foliage,seed);
        for(int i=0;i<crown.size();i++) {
            ArborCanopyGeometry.Leaflet leaf=crown.get(i);
            int tint=mix(i%3==0?0xFF9A2845:i%3==1?0xFF642638:0xFF3F202C,familyColor,.24F);
            texturedLeafBlade(pose,out,leafLight,leaf,tint);
        }
    }

    private static void renderTerminalCrown(PoseStack.Pose pose, VertexConsumer out, int light,
            List<ArborCanopyGeometry.Point> path, int familyColor, float size, float foliage, int seed) {
        ArborCanopyGeometry.Point center=ArborCanopyGeometry.terminalFoliageCenter(path,size*.18F);
        renderLeafCluster(pose,out,light,center,familyColor,size,foliage,seed);
    }

    private static void renderPomeWounds(ArborOfWillEntity arbor, PoseStack.Pose pose,
            MultiBufferSource buffers, float height) {
        for(int i=0;i<arbor.pomesConsumed();i++) {
            double a=i*2.399963;
            float y=.52F+(i%5)*height*.105F;
            float x=(float)Math.cos(a)*.57F,z=(float)Math.sin(a)*.57F;
            glowScar(buffers,pose,x,y,z,x*.82F,y+.32F,z*.82F);
        }
    }

    private static void renderHeartPulse(PoseStack.Pose pose, MultiBufferSource buffers, float height, float time) {
        float beat = .18F + .035F * (float)Math.sin(time * .24F);
        colorEllipsoid(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), pose.pose(),
                0, height * .69F, -.19F, beat * 1.25F, beat, beat * .72F, .96F, .02F, .035F, .78F);
    }

    private static void renderApotheosisCapStatic(PoseStack.Pose pose, VertexConsumer fungus, int light,
            float height) {
        for(int layer=0;layer<5;layer++) {
            float radius=3.2F-layer*.48F;
            texturedEllipsoid(pose,fungus,light,0,height-.28F+layer*.25F,0,
                    radius,.16F+layer*.015F,radius*.92F,mix(0xFFB92A40,0xFFE3C99A,layer/8F));
        }
    }

    private static void renderApotheosisGlow(PoseStack.Pose pose, MultiBufferSource buffers,
            float height, float time) {
        for(int ring=0;ring<3;ring++) for(int i=0;i<18;i++) {
            double a=i*Math.PI*2/18.0+ring*.17;
            float radius=1.05F+ring*.72F;
            glowScar(buffers,pose,0,height-.35F+ring*.12F,0,
                    Math.cos(a)*radius,height-.34F+ring*.12F,Math.sin(a)*radius);
        }
        for(int i=0;i<16;i++) {
            double a=i*Math.PI*2/16.0;
            float radius=2.45F;
            float bob=.07F*(float)Math.sin(time*.08F+i);
            colorEllipsoid(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW),pose.pose(),
                    (float)Math.cos(a)*radius,height-.05F+bob,(float)Math.sin(a)*radius,
                    .09F,.27F,.09F,.28F+(i%3)*.18F,.55F,.74F,.62F);
        }
    }

    private static List<ArborCanopyGeometry.Point> canopyFork(List<ArborCanopyGeometry.Point> limb, int sign, double reach) {
        ArborCanopyGeometry.Point start=limb.get(Math.max(0,limb.size()-5));
        double angle=start.angle()+sign*.58;
        List<ArborCanopyGeometry.Point> result=new ArrayList<>();
        for(int i=0;i<6;i++) {
            double t=i/5.0,r=reach*t;
            result.add(new ArborCanopyGeometry.Point(start.x()+Math.cos(angle)*r,
                    start.y()+Math.sin(t*Math.PI)*.42+t*.32,start.z()+Math.sin(angle)*r));
        }
        return result;
    }

    private static ArborCanopyGeometry.Point nearest(List<ArborCanopyGeometry.Point> path, ArborCanopyGeometry.Point target) {
        ArborCanopyGeometry.Point best=path.get(0); double distance=best.distanceTo(target);
        for(ArborCanopyGeometry.Point p:path) { double d=p.distanceTo(target); if(d<distance){best=p;distance=d;} }
        return best;
    }

    private static List<ArborCanopyGeometry.Point> curvedTwig(ArborCanopyGeometry.Point start,
            ArborCanopyGeometry.Point end, int seed) {
        List<ArborCanopyGeometry.Point> result=new ArrayList<>();
        double bend=((seed&1)==0?.18:-.18);
        for(int i=0;i<6;i++) {
            double t=i/5.0,u=1.0-t;
            result.add(new ArborCanopyGeometry.Point(start.x()*u+end.x()*t+bend*Math.sin(t*Math.PI),
                    start.y()*u+end.y()*t+Math.sin(t*Math.PI)*.24,
                    start.z()*u+end.z()*t-bend*Math.sin(t*Math.PI)));
        }
        return result;
    }

    private static void texturedTube(PoseStack.Pose pose, VertexConsumer out, int light,
            List<ArborCanopyGeometry.Point> path, float startRadius, float endRadius, int color, float vScale) {
        texturedTubeProfiled(pose,out,light,path,startRadius,endRadius,endRadius,color,vScale,1.0F);
    }

    private static void texturedTerminalTube(PoseStack.Pose pose,VertexConsumer out,int light,
            List<ArborCanopyGeometry.Point> path,float startRadius,float endRadius,int color,float vScale,
            float attachmentDistance) {
        List<ArborCanopyGeometry.Point> tapered=ArborCanopyGeometry.terminalTaper(path,attachmentDistance);
        float taperStart=(path.size()-1)/(float)Math.max(1,tapered.size()-1);
        texturedTubeProfiled(pose,out,light,tapered,startRadius,endRadius,0F,color,vScale,taperStart);
    }

    private static void texturedTubeProfiled(PoseStack.Pose pose,VertexConsumer out,int light,
            List<ArborCanopyGeometry.Point> path,float startRadius,float branchEndRadius,float tipRadius,
            int color,float vScale,float taperStart) {
        if(path.size()<2)return;
        path=ArborCanopyGeometry.smooth(path,3);
        int sides=10;
        double[][] n1=new double[path.size()][3],n2=new double[path.size()][3];
        for(int i=0;i<path.size();i++) {
            ArborCanopyGeometry.Point before=path.get(Math.max(0,i-1));
            ArborCanopyGeometry.Point after=path.get(Math.min(path.size()-1,i+1));
            double tx=after.x()-before.x(),ty=after.y()-before.y(),tz=after.z()-before.z();
            double len=Math.sqrt(tx*tx+ty*ty+tz*tz); if(len<1.0e-5){tx=0;ty=1;tz=0;len=1;}
            tx/=len;ty/=len;tz/=len;
            double rx=Math.abs(ty)>.92?1:0,ry=Math.abs(ty)>.92?0:1,rz=0;
            n1[i][0]=ty*rz-tz*ry;n1[i][1]=tz*rx-tx*rz;n1[i][2]=tx*ry-ty*rx;
            double n1l=Math.sqrt(n1[i][0]*n1[i][0]+n1[i][1]*n1[i][1]+n1[i][2]*n1[i][2]);
            n1[i][0]/=n1l;n1[i][1]/=n1l;n1[i][2]/=n1l;
            n2[i][0]=ty*n1[i][2]-tz*n1[i][1];
            n2[i][1]=tz*n1[i][0]-tx*n1[i][2];
            n2[i][2]=tx*n1[i][1]-ty*n1[i][0];
            if(i>0 && n1[i][0]*n1[i-1][0]+n1[i][1]*n1[i-1][1]+n1[i][2]*n1[i-1][2]<0) {
                for(int axis=0;axis<3;axis++){n1[i][axis]*=-1;n2[i][axis]*=-1;}
            }
        }
        for(int segment=0;segment<path.size()-1;segment++) {
            ArborCanopyGeometry.Point a=path.get(segment),b=path.get(segment+1);
            float t0=segment/(float)(path.size()-1),t1=(segment+1)/(float)(path.size()-1);
            float r0=ArborCanopyGeometry.pointedRadius(t0,taperStart,startRadius,branchEndRadius);
            float r1=ArborCanopyGeometry.pointedRadius(t1,taperStart,startRadius,branchEndRadius);
            for(int side=0;side<sides;side++) {
                double q0=side*Math.PI*2/sides,q1=(side+1)*Math.PI*2/sides;
                double c0=Math.cos(q0),s0=Math.sin(q0),c1=Math.cos(q1),s1=Math.sin(q1);
                tubeVertex(pose,out,light,a,r0,n1[segment],n2[segment],c0,s0,side/(float)sides,t0*vScale,color);
                tubeVertex(pose,out,light,b,r1,n1[segment+1],n2[segment+1],c0,s0,side/(float)sides,t1*vScale,color);
                tubeVertex(pose,out,light,b,r1,n1[segment+1],n2[segment+1],c1,s1,(side+1)/(float)sides,t1*vScale,color);
                tubeVertex(pose,out,light,a,r0,n1[segment],n2[segment],c1,s1,(side+1)/(float)sides,t0*vScale,color);
            }
        }
        ArborCanopyGeometry.Point startTangent=path.get(0).subtract(path.get(1));
        ArborCanopyGeometry.Point endTangent=path.get(path.size()-1).subtract(path.get(path.size()-2));
        tubeCap(pose,out,light,path.get(0),startTangent,startRadius,sides,color);
        if(tipRadius>1.0e-5F) {
            tubeCap(pose,out,light,path.get(path.size()-1),endTangent,tipRadius,sides,color);
        }
    }

    private static void tubeCap(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Point center,ArborCanopyGeometry.Point tangent,float radius,int sides,int color) {
        List<ArborCanopyGeometry.Point> ring=ArborCanopyGeometry.capRing(center,tangent,radius,sides);
        ArborCanopyGeometry.Point normal=tangent.normalized();
        for(int side=0;side<ring.size();side++) {
            int next=(side+1)%ring.size();
            double a0=side*Math.PI*2.0/ring.size(),a1=next*Math.PI*2.0/ring.size();
            texturedVertex(pose,out,light,center.x(),center.y(),center.z(),.5F,.5F,
                    normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,ring.get(side).x(),ring.get(side).y(),ring.get(side).z(),
                    (float)(.5+.5*Math.cos(a0)),(float)(.5+.5*Math.sin(a0)),normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,ring.get(next).x(),ring.get(next).y(),ring.get(next).z(),
                    (float)(.5+.5*Math.cos(a1)),(float)(.5+.5*Math.sin(a1)),normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,center.x(),center.y(),center.z(),.5F,.5F,
                    normal.x(),normal.y(),normal.z(),color);
        }
    }

    private static void tubeVertex(PoseStack.Pose pose,VertexConsumer out,int light,ArborCanopyGeometry.Point p,
            float radius,double[] n1,double[] n2,
            double cosine,double sine,float u,float v,int color) {
        double nx=n1[0]*cosine+n2[0]*sine,ny=n1[1]*cosine+n2[1]*sine,nz=n1[2]*cosine+n2[2]*sine;
        texturedVertex(pose,out,light,p.x()+nx*radius,p.y()+ny*radius,p.z()+nz*radius,u,v,nx,ny,nz,color);
    }

    private static void texturedEllipsoid(PoseStack.Pose pose, VertexConsumer out, int light,
            double cx,double cy,double cz,double rx,double ry,double rz,int color) {
        int latitudes=7,longitudes=12;
        for(int lat=0;lat<latitudes;lat++) {
            double t0=Math.PI*lat/latitudes,t1=Math.PI*(lat+1)/latitudes;
            for(int lon=0;lon<longitudes;lon++) {
                double p0=Math.PI*2*lon/longitudes,p1=Math.PI*2*(lon+1)/longitudes;
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t0,p0,lon/(float)longitudes,lat/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t1,p0,lon/(float)longitudes,(lat+1)/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t1,p1,(lon+1)/(float)longitudes,(lat+1)/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t0,p1,(lon+1)/(float)longitudes,lat/(float)latitudes,color);
            }
        }
    }

    private static void texturedLeafBlade(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Leaflet leaf,int color) {
        ArborCanopyGeometry.Point direction=leaf.direction().normalized();
        ArborCanopyGeometry.Point reference=Math.abs(direction.y())>.88
                ?new ArborCanopyGeometry.Point(1,0,0):new ArborCanopyGeometry.Point(0,1,0);
        ArborCanopyGeometry.Point side=direction.cross(reference).normalized();
        ArborCanopyGeometry.Point crown=side.cross(direction).normalized();
        for(int plane=0;plane<ArborRenderBudget.LEAF_RIBBON_PLANES;plane++) {
            double angle=plane*Math.PI/2.0;
            ArborCanopyGeometry.Point lateral=side.scale(Math.cos(angle)).add(crown.scale(Math.sin(angle))).normalized();
            ArborCanopyGeometry.Point normal=direction.cross(lateral).normalized();
            for(int segment=0;segment<ArborRenderBudget.LEAF_RIBBON_SEGMENTS;segment++) {
                double t0=segment/(double)ArborRenderBudget.LEAF_RIBBON_SEGMENTS;
                double t1=(segment+1)/(double)ArborRenderBudget.LEAF_RIBBON_SEGMENTS;
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t0,-1,
                        (float)t0,0,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t1,-1,
                        (float)t1,0,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t1,1,
                        (float)t1,1,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t0,1,
                        (float)t0,1,color);
            }
        }
    }

    private static void leafRibbonVertex(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Leaflet leaf,ArborCanopyGeometry.Point direction,
            ArborCanopyGeometry.Point crown,ArborCanopyGeometry.Point lateral,
            ArborCanopyGeometry.Point normal,double t,double sideSign,
            float u,float v,int color) {
        double taper=Math.pow(Math.sin(Math.PI*t),.72);
        double bend=Math.sin(Math.PI*t)*leaf.curl();
        ArborCanopyGeometry.Point center=leaf.center().add(direction.scale(leaf.length()*t)).add(crown.scale(bend));
        ArborCanopyGeometry.Point point=center.add(lateral.scale(sideSign*leaf.width()*taper));
        texturedVertex(pose,out,light,point.x(),point.y(),point.z(),u,v,
                normal.x(),normal.y(),normal.z(),color);
    }

    private static void ellipsoidVertex(PoseStack.Pose pose,VertexConsumer out,int light,double cx,double cy,double cz,
            double rx,double ry,double rz,double theta,double phi,float u,float v,int color) {
        double nx=Math.sin(theta)*Math.cos(phi),ny=Math.cos(theta),nz=Math.sin(theta)*Math.sin(phi);
        texturedVertex(pose,out,light,cx+nx*rx,cy+ny*ry,cz+nz*rz,u,v,nx,ny,nz,color);
    }

    private static void texturedVertex(PoseStack.Pose pose,VertexConsumer out,int light,double x,double y,double z,
            float u,float v,double nx,double ny,double nz,int color) {
        Matrix4f matrix=pose.pose();Matrix3f normals=pose.normal();
        Vector3f normal=new Vector3f((float)nx,(float)ny,(float)nz).mul(normals).normalize();
        out.addVertex(matrix,(float)x,(float)y,(float)z).setColor(argbR(color),argbG(color),argbB(color),argbA(color))
                .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(normal.x(),normal.y(),normal.z());
    }

    private static void glowScar(MultiBufferSource buffers,PoseStack.Pose pose,
            double x0,double y0,double z0,double x1,double y1,double z1) {
        VertexConsumer out=buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);Matrix4f m=pose.pose();float w=.018F;
        out.addVertex(m,(float)x0-w,(float)y0,(float)z0).setColor(.94F,.03F,.05F,.76F);
        out.addVertex(m,(float)x0+w,(float)y0,(float)z0).setColor(.94F,.03F,.05F,.76F);
        out.addVertex(m,(float)x1+w,(float)y1,(float)z1).setColor(.94F,.18F,.08F,.58F);
        out.addVertex(m,(float)x1-w,(float)y1,(float)z1).setColor(.94F,.18F,.08F,.58F);
    }

    private static void colorEllipsoid(VertexConsumer out,Matrix4f matrix,float cx,float cy,float cz,float rx,float ry,float rz,
            float red,float green,float blue,float alpha) {
        for(int lat=0;lat<6;lat++){double t0=Math.PI*lat/6,t1=Math.PI*(lat+1)/6;
            for(int lon=0;lon<10;lon++){double p0=Math.PI*2*lon/10,p1=Math.PI*2*(lon+1)/10;
                colorVertex(out,matrix,cx,cy,cz,rx,ry,rz,t0,p0,red,green,blue,alpha);
                colorVertex(out,matrix,cx,cy,cz,rx,ry,rz,t1,p0,red,green,blue,alpha);
                colorVertex(out,matrix,cx,cy,cz,rx,ry,rz,t1,p1,red,green,blue,alpha);
                colorVertex(out,matrix,cx,cy,cz,rx,ry,rz,t0,p1,red,green,blue,alpha);}}
    }

    private static void colorVertex(VertexConsumer out,Matrix4f matrix,float cx,float cy,float cz,float rx,float ry,float rz,
            double theta,double phi,float red,float green,float blue,float alpha) {
        out.addVertex(matrix,cx+(float)(Math.sin(theta)*Math.cos(phi))*rx,cy+(float)Math.cos(theta)*ry,
                cz+(float)(Math.sin(theta)*Math.sin(phi))*rz).setColor(red,green,blue,alpha);
    }

    private static int mix(int a,int b,float amount) {
        amount=Math.max(0,Math.min(1,amount));
        return ((int)(argbA(a)+(argbA(b)-argbA(a))*amount)<<24)|((int)(argbR(a)+(argbR(b)-argbR(a))*amount)<<16)
                |((int)(argbG(a)+(argbG(b)-argbG(a))*amount)<<8)|(int)(argbB(a)+(argbB(b)-argbB(a))*amount);
    }
    private static int argbA(int c){return c>>>24&255;}private static int argbR(int c){return c>>>16&255;}
    private static int argbG(int c){return c>>>8&255;}private static int argbB(int c){return c&255;}
    private static float red(int c){return argbR(c)/255F;}private static float green(int c){return argbG(c)/255F;}
    private static float blue(int c){return argbB(c)/255F;}

    private record MeshConsumers(VertexConsumer bark, VertexConsumer mycelium, VertexConsumer fungus) { }

    private static final class ArborMeshCache {
        private static final Map<ArborFruitMeshKey, FruitMesh> FRUITS = new HashMap<>();
        private static ArborStaticVisualKey staticKey;
        private static CachedLayer staticBark;
        private static CachedLayer staticMycelium;
        private static CachedLayer staticFungus;

        private ArborMeshCache() { }

        static void renderStatic(PoseStack stack, ArborStaticVisualKey key,
                List<ArborOfWillLayout.FruitPlacement> placements) {
            if (!key.equals(staticKey)) rebuildStatic(key, placements);
            draw(staticBark, stack);
            draw(staticMycelium, stack);
            draw(staticFungus, stack);
        }

        static void renderFruit(PoseStack stack, ArborFruitMeshKey key) {
            FruitMesh mesh = FRUITS.computeIfAbsent(key, ArborMeshCache::buildFruit);
            draw(mesh.bark(), stack);
            draw(mesh.mycelium(), stack);
        }

        static void clear() {
            close(staticBark);
            close(staticMycelium);
            close(staticFungus);
            staticBark = null;
            staticMycelium = null;
            staticFungus = null;
            staticKey = null;
            FRUITS.values().forEach(FruitMesh::close);
            FRUITS.clear();
        }

        private static void rebuildStatic(ArborStaticVisualKey key,
                List<ArborOfWillLayout.FruitPlacement> placements) {
            close(staticBark);
            close(staticMycelium);
            close(staticFungus);
            staticBark = buildLayer(BARK_TYPE, consumers -> emitStaticTree(key, placements, consumers));
            staticMycelium = buildLayer(MYCELIUM_TYPE, consumers -> emitStaticTree(key, placements, consumers));
            staticFungus = buildLayer(FUNGUS_TYPE, consumers -> emitStaticTree(key, placements, consumers));
            staticKey = key;
        }

        private static void emitStaticTree(ArborStaticVisualKey key,
                List<ArborOfWillLayout.FruitPlacement> placements, MeshConsumers consumers) {
            PoseStack buildStack = new PoseStack();
            PoseStack.Pose pose = buildStack.last();
            float height = (float) ArborOfWillVisualRules.treeHeight(key.degree());
            float rootRadius = (float) ArborOfWillVisualRules.rootRadius(key.degree(), key.chamberRadius());
            int whorls = ArborOfWillVisualRules.visibleWhorls(key.degree());
            float foliage = key.foliage();
            renderRootsAndHeartwood(pose, consumers, key.packedLight(), height, rootRadius, foliage);
            List<List<ArborCanopyGeometry.Point>> limbs = renderBraidedCanopy(
                    pose, consumers, key.packedLight(), height, rootRadius, whorls, foliage);
            renderStaticOrchard(pose, consumers, key.packedLight(), placements, limbs, whorls, foliage);
            if (key.degree() >= 8) {
                renderApotheosisCapStatic(pose, consumers.fungus(), key.packedLight(), height);
            }
        }

        private static FruitMesh buildFruit(ArborFruitMeshKey key) {
            CachedLayer bark = buildLayer(BARK_TYPE, consumers -> emitFruitBody(
                    new PoseStack().last(), consumers, key));
            CachedLayer mycelium = buildLayer(MYCELIUM_TYPE, consumers -> emitFruitBody(
                    new PoseStack().last(), consumers, key));
            return new FruitMesh(bark, mycelium);
        }

        private static CachedLayer buildLayer(RenderType target, java.util.function.Consumer<MeshConsumers> emitter) {
            BufferBuilder builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            VertexConsumer none = NoOpVertexConsumer.INSTANCE;
            emitter.accept(new MeshConsumers(
                    target == BARK_TYPE ? builder : none,
                    target == MYCELIUM_TYPE ? builder : none,
                    target == FUNGUS_TYPE ? builder : none));
            MeshData mesh = builder.build();
            if (mesh == null) return null;
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(mesh);
            VertexBuffer.unbind();
            return new CachedLayer(target, vertexBuffer, GameRenderer::getRendertypeEntityCutoutNoCullShader);
        }

        private static void draw(CachedLayer layer, PoseStack stack) {
            if (layer == null) return;
            ShaderInstance shader = layer.shader().get();
            if (shader == null) return;
            layer.renderType().setupRenderState();
            layer.buffer().bind();
            Matrix4f modelView = CachedMeshModelView.compose(
                    RenderSystem.getModelViewMatrix(), stack.last().pose());
            layer.buffer().drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);
            VertexBuffer.unbind();
            layer.renderType().clearRenderState();
        }

        private static void close(CachedLayer layer) {
            if (layer != null) layer.buffer().close();
        }
    }

    private record CachedLayer(RenderType renderType, VertexBuffer buffer, Supplier<ShaderInstance> shader) { }

    private record FruitMesh(CachedLayer bark, CachedLayer mycelium) {
        void close() {
            ArborMeshCache.close(bark);
            ArborMeshCache.close(mycelium);
        }
    }

    private enum NoOpVertexConsumer implements VertexConsumer {
        INSTANCE;

        @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }

    @Override public ResourceLocation getTextureLocation(ArborOfWillEntity entity) { return BARK; }
}
