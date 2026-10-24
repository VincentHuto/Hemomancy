package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.CachedMeshModelView;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborCanopyGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborFruitGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillLayout;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillVisualRules;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborSkillPresentation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.FAMILY_COLORS;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.argbB;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.argbG;
import static com.vincenthuto.hemomancy.client.render.entity.misc.ArborMeshEmitter.argbR;

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
    private static final int SPORE_MOTES = 14;
    // Closed solids are back-face culled; only the flat leaf blades need both sides drawn.
    private static final RenderType BARK_TYPE = RenderType.entityCutout(BARK);
    private static final RenderType MYCELIUM_TYPE = RenderType.entityCutout(LEAVES);
    private static final RenderType LEAF_TYPE = RenderType.entityCutoutNoCull(LEAVES);
    private static final RenderType FUNGUS_TYPE = RenderType.entityCutout(FUNGUS);

    public ArborOfWillRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 2.7F;
    }

    @Override
    public boolean shouldRender(ArborOfWillEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return entity.shouldRender(cameraX, cameraY, cameraZ)
                && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(entity.chamberRadius(), 8.0D, entity.chamberRadius()));
    }

    @Override
    public void render(ArborOfWillEntity arbor, float yaw, float partial, PoseStack stack,
            MultiBufferSource buffers, int light) {
        stack.pushPose();
        PoseStack.Pose pose = stack.last();
        float time = AnimationTime.age(arbor, partial);
        float height = (float) ArborOfWillVisualRules.treeHeight(arbor.degree());
        float rootRadius = (float) ArborOfWillVisualRules.rootRadius(arbor.degree(), arbor.chamberRadius());
        int whorls = ArborOfWillVisualRules.visibleWhorls(arbor.degree());

        SkillProgress progress = SkillProgressClientCache.current();
        List<ArborOfWillLayout.FruitPlacement> placements = ArborSkillPresentation.placements(arbor.chamberRadius(), arbor.degree());
        int unlocked = (int) placements.stream().map(p -> SkillPointInit.getById(p.skillId()))
                .filter(progress::isUnlocked).count();
        float foliage = (float) ArborOfWillVisualRules.foliageFraction(unlocked, placements.size(),
                arbor.pomesConsumed(), arbor.degree() >= 8);
        foliage = Math.max(foliage, (float) ArborOfWillVisualRules.minimumFoliage(whorls, arbor.pomesConsumed()));

        ArborMeshCache.renderStatic(stack, ArborStaticVisualKey.create(
                arbor.degree(), arbor.chamberRadius(), foliage, arbor.pomesConsumed(), light), placements);
        renderSkillOrchard(arbor, stack, buffers, placements, progress, whorls, time, partial);
        renderHeartPulse(pose, buffers, height, time);
        renderPomeWounds(arbor, pose, buffers);
        if (arbor.degree() >= 8) {
            renderApotheosisGlow(pose, buffers, arbor.degree(), time);
        }

        stack.popPose();
        super.render(arbor, yaw, partial, stack, buffers, light);
    }

    public static void clearCaches() {
        ArborMeshCache.clear();
    }

    private static void renderSkillOrchard(ArborOfWillEntity arbor, PoseStack stack,
            MultiBufferSource buffers, List<ArborOfWillLayout.FruitPlacement> placements,
            SkillProgress progress, int whorls, float time, float partial) {
        Map<ArborFruitMeshKey, List<Matrix4f>> instances = new LinkedHashMap<>();
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
            instances.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new Matrix4f(stack.last().pose()));
            stack.popPose();
            renderFruitGlow(buffers, stack.last(), fruit, key, scale, color);
        }
        ArborMeshCache.renderFruits(instances);
    }

    /** Glow accents drawn on the cached fruit anatomy; points come from {@link ArborFruitMeshes} in fruit-local units. */
    private static void renderFruitGlow(MultiBufferSource buffers, PoseStack.Pose pose,
            ArborOfWillLayout.FruitPlacement fruit, ArborFruitMeshKey key, float scale, int color) {
        if (key.state() != ArborOfWillVisualRules.GrowthState.RIPE_FRUIT) return;
        ArborFruitGeometry.Shape shape = ArborFruitGeometry.profile(key.family()).shape();
        if (shape == ArborFruitGeometry.Shape.FISSURED_NUT) {
            for (int i = 0; i < ArborFruitMeshes.scarFissureCount(); i++) {
                glowLine(buffers, pose, fruit, scale, ArborFruitMeshes.scarFissurePoint(i, .22),
                        ArborFruitMeshes.scarFissurePoint(i, .78));
            }
        } else if (shape == ArborFruitGeometry.Shape.GILLED_POD) {
            for (int i = 0; i < ArborFruitMeshes.gillCount(); i += 2) {
                ArborCanopyGeometry.Point[] gill = ArborFruitMeshes.gillGlowLine(i);
                glowLine(buffers, pose, fruit, scale, gill[0], gill[1]);
            }
        }
        int beads = ArborOfWillVisualRules.seedChambers(key.level(), key.maxLevel());
        float size = .018F * scale;
        for (int i = 0; i < beads; i++) {
            ArborCanopyGeometry.Point node = ArborFruitMeshes.levelNode(key.family(), i, beads);
            colorEllipsoid(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), pose.pose(),
                    (float) (fruit.x() + node.x() * scale), (float) (fruit.y() + node.y() * scale),
                    (float) (fruit.z() + node.z() * scale), size, size, size,
                    red(color), green(color), blue(color), .85F);
        }
    }

    private static void glowLine(MultiBufferSource buffers, PoseStack.Pose pose, ArborOfWillLayout.FruitPlacement fruit,
            float scale, ArborCanopyGeometry.Point from, ArborCanopyGeometry.Point to) {
        ArborCanopyGeometry.Point origin = new ArborCanopyGeometry.Point(fruit.x(), fruit.y(), fruit.z());
        glowStrip(buffers, pose, origin.add(from.scale(scale)), origin.add(to.scale(scale)), .009 * scale);
    }

    private static void glowStrip(MultiBufferSource buffers, PoseStack.Pose pose,
            ArborCanopyGeometry.Point a, ArborCanopyGeometry.Point b, double halfWidth) {
        glowStrip(buffers, pose, a, b, halfWidth, .94F, .03F, .05F, .76F);
    }

    /** A thin glowing strip between two points, widened sideways so it reads from any angle but straight above. */
    private static void glowStrip(MultiBufferSource buffers, PoseStack.Pose pose,
            ArborCanopyGeometry.Point a, ArborCanopyGeometry.Point b, double halfWidth,
            float red, float green, float blue, float alpha) {
        ArborCanopyGeometry.Point side = b.subtract(a).cross(new ArborCanopyGeometry.Point(0, 1, 0));
        if (side.length() < 1.0e-6) side = new ArborCanopyGeometry.Point(1, 0, 0);
        side = side.normalized().scale(halfWidth);
        VertexConsumer out = buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
        Matrix4f m = pose.pose();
        // The far end fades a little and warms toward orange, as the old wound scars did.
        float endGreen = Math.min(1, green + .15F), endAlpha = alpha * .76F;
        out.addVertex(m, (float) (a.x() - side.x()), (float) (a.y() - side.y()), (float) (a.z() - side.z())).setColor(red, green, blue, alpha);
        out.addVertex(m, (float) (a.x() + side.x()), (float) (a.y() + side.y()), (float) (a.z() + side.z())).setColor(red, green, blue, alpha);
        out.addVertex(m, (float) (b.x() + side.x()), (float) (b.y() + side.y()), (float) (b.z() + side.z())).setColor(red, endGreen, blue, endAlpha);
        out.addVertex(m, (float) (b.x() - side.x()), (float) (b.y() - side.y()), (float) (b.z() - side.z())).setColor(red, endGreen, blue, endAlpha);
    }

    /** Glowing seams inside the Pome gashes cut into the cached bark. */
    private static void renderPomeWounds(ArborOfWillEntity arbor, PoseStack.Pose pose, MultiBufferSource buffers) {
        for (int i = 0; i < Math.min(9, arbor.pomesConsumed()); i++) {
            ArborCanopyGeometry.Point[] seam = ArborBarkMeshes.pomeWoundLine(i, arbor.degree());
            glowStrip(buffers, pose, seam[0], seam[1], .014);
        }
    }

    private static void renderHeartPulse(PoseStack.Pose pose, MultiBufferSource buffers, float height, float time) {
        float beat = .18F + .035F * (float)Math.sin(time * .24F);
        colorEllipsoid(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), pose.pose(),
                0, height * .69F, -.19F, beat * 1.25F, beat, beat * .72F, .96F, .02F, .035F, .78F);
    }

    /** Apotheosis crown accents: gill edges glow in a slow travelling pulse and spores drift down from the cap. */
    private static void renderApotheosisGlow(PoseStack.Pose pose, MultiBufferSource buffers, int degree, float time) {
        for (int gill = 0; gill < ArborApotheosisMeshes.gillCount(); gill += 2) {
            ArborCanopyGeometry.Point[] edge = ArborApotheosisMeshes.gillGlowLine(gill, degree);
            float pulse = .35F + .3F * (float) Math.max(0, Math.sin(time * .05F + gill * .35F));
            glowStrip(buffers, pose, edge[0], edge[1], .012, .95F, .62F, .42F, pulse);
        }
        VertexConsumer glow = buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
        for (int i = 0; i < SPORE_MOTES; i++) {
            ArborCanopyGeometry.Point origin = ArborApotheosisMeshes.sporeOrigin(i, degree);
            float life = (time * .004F + i * .137F) % 1.0F;
            float fall = life * 3.2F;
            float sway = .12F * (float) Math.sin(time * .03F + i * 1.7F);
            float alpha = .7F * (float) Math.sin(Math.PI * life);
            colorEllipsoid(glow, pose.pose(), (float) origin.x() + sway, (float) origin.y() - fall, (float) origin.z() - sway * .5F,
                    .035F, .035F, .035F, .95F, .78F, .58F, alpha);
        }
    }

    private static void colorEllipsoid(VertexConsumer out,Matrix4f matrix,float cx,float cy,float cz,float rx,float ry,float rz,
            float red,float green,float blue,float alpha) {
        for (int vertex = 0; vertex < ArborGlowGeometry.vertexCount(); vertex++) {
            out.addVertex(matrix, cx + ArborGlowGeometry.x(vertex) * rx,
                    cy + ArborGlowGeometry.y(vertex) * ry, cz + ArborGlowGeometry.z(vertex) * rz)
                    .setColor(red, green, blue, alpha);
        }
    }

    private static float red(int c){return argbR(c)/255F;}private static float green(int c){return argbG(c)/255F;}
    private static float blue(int c){return argbB(c)/255F;}

    private static final class ArborMeshCache {
        private static final int INITIAL_LAYER_BYTES = 1 << 16;
        private static final Map<ArborFruitMeshKey, MeshLayers> FRUITS = new HashMap<>();
        private static final Matrix4f INSTANCE_MODEL_VIEW = new Matrix4f();
        private static ArborStaticVisualKey staticKey;
        private static MeshLayers staticLayers;

        private ArborMeshCache() { }

        static void renderStatic(PoseStack stack, ArborStaticVisualKey key,
                List<ArborOfWillLayout.FruitPlacement> placements) {
            if (!key.equals(staticKey)) {
                if (staticLayers != null) staticLayers.close();
                staticLayers = buildLayers(consumers -> ArborMeshEmitter.emitStaticTree(key, placements, consumers));
                staticKey = key;
            }
            draw(staticLayers.bark(), stack);
            draw(staticLayers.mycelium(), stack);
            draw(staticLayers.leaves(), stack);
            draw(staticLayers.fungus(), stack);
        }

        /** Fruits share one render state per layer; each instance only changes the model-view. */
        static void renderFruits(Map<ArborFruitMeshKey, List<Matrix4f>> instances) {
            for (ArborFruitMeshKey key : instances.keySet()) {
                FRUITS.computeIfAbsent(key, fruit -> buildLayers(consumers ->
                        ArborFruitMeshes.emit(consumers, fruit)));
            }
            drawInstances(instances, MeshLayers::bark);
            drawInstances(instances, MeshLayers::mycelium);
        }

        static void clear() {
            if (staticLayers != null) staticLayers.close();
            staticLayers = null;
            staticKey = null;
            FRUITS.values().forEach(MeshLayers::close);
            FRUITS.clear();
        }

        /** Emits once into every layer instead of re-running the emitter per texture. */
        private static MeshLayers buildLayers(Consumer<ArborMeshEmitter.MeshConsumers> emitter) {
            ByteBufferBuilder[] memory = new ByteBufferBuilder[4];
            try {
                BufferBuilder[] builders = new BufferBuilder[memory.length];
                for (int i = 0; i < memory.length; i++) {
                    memory[i] = new ByteBufferBuilder(INITIAL_LAYER_BYTES);
                    builders[i] = new BufferBuilder(memory[i], VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
                }
                emitter.accept(new ArborMeshEmitter.MeshConsumers(builders[0], builders[1], builders[2], builders[3]));
                return new MeshLayers(
                        upload(BARK_TYPE, builders[0], GameRenderer::getRendertypeEntityCutoutShader),
                        upload(MYCELIUM_TYPE, builders[1], GameRenderer::getRendertypeEntityCutoutShader),
                        upload(LEAF_TYPE, builders[2], GameRenderer::getRendertypeEntityCutoutNoCullShader),
                        upload(FUNGUS_TYPE, builders[3], GameRenderer::getRendertypeEntityCutoutShader));
            } finally {
                for (ByteBufferBuilder buffer : memory) if (buffer != null) buffer.close();
            }
        }

        private static CachedLayer upload(RenderType target, BufferBuilder builder, Supplier<ShaderInstance> shader) {
            MeshData mesh = builder.build();
            if (mesh == null) return null;
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(mesh);
            VertexBuffer.unbind();
            return new CachedLayer(target, vertexBuffer, shader);
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

        private static void drawInstances(Map<ArborFruitMeshKey, List<Matrix4f>> instances,
                Function<MeshLayers, CachedLayer> layerOf) {
            Matrix4f camera = RenderSystem.getModelViewMatrix();
            CachedLayer active = null;
            ShaderInstance shader = null;
            for (Map.Entry<ArborFruitMeshKey, List<Matrix4f>> batch : instances.entrySet()) {
                CachedLayer layer = layerOf.apply(FRUITS.get(batch.getKey()));
                if (layer == null) continue;
                if (active == null) {
                    shader = layer.shader().get();
                    if (shader == null) return;
                    active = layer;
                    layer.renderType().setupRenderState();
                    shader.setDefaultUniforms(VertexFormat.Mode.QUADS, camera,
                            RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                    shader.apply();
                }
                layer.buffer().bind();
                for (Matrix4f local : batch.getValue()) {
                    if (shader.MODEL_VIEW_MATRIX != null) {
                        shader.MODEL_VIEW_MATRIX.set(INSTANCE_MODEL_VIEW.set(camera).mul(local));
                        shader.MODEL_VIEW_MATRIX.upload();
                    }
                    layer.buffer().draw();
                }
            }
            if (active == null) return;
            shader.clear();
            VertexBuffer.unbind();
            active.renderType().clearRenderState();
        }

        private static void close(CachedLayer layer) {
            if (layer != null) layer.buffer().close();
        }
    }

    private record CachedLayer(RenderType renderType, VertexBuffer buffer, Supplier<ShaderInstance> shader) { }

    private record MeshLayers(CachedLayer bark, CachedLayer mycelium, CachedLayer leaves, CachedLayer fungus) {
        void close() {
            ArborMeshCache.close(bark);
            ArborMeshCache.close(mycelium);
            ArborMeshCache.close(leaves);
            ArborMeshCache.close(fungus);
        }
    }

    @Override public ResourceLocation getTextureLocation(ArborOfWillEntity entity) { return BARK; }
}
