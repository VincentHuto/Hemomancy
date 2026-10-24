package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Animates the Resonant Forge's moving parts over the baked stage body. Pivots are model pixels in
 * the north-facing controller frame and mirror tools/model_export/resonant_forge.py.
 */
public class ResonantForgeRenderer implements BlockEntityRenderer<ResonantForgeBlockEntity> {
    public static final String[] PART_MODELS = {"helve", "helve_iron", "head", "head_iron", "crank", "pitman",
            "treadle", "wheel", "stylus", "carriage", "governor", "heart", "cylinder_wax", "cylinder_ambergris",
            "spring_pole", "spring_tip"};

    private static final float[] HELVE_PIVOT = {-6.5F, 18F, 12F};
    private static final float[] SPRING_ANCHOR = {-6.5F, 28.75F, 1F};
    private static final float[] SPRING_BEND = {-6.5F, 28.5F, 14F};
    /** Top of the cord on the base hammer head, where the spring pole's tip takes hold. */
    private static final float[] CORD_TOP = {-6.5F, 24.75F, 22F};
    /** The pole and its tip flex by the same angle: the cord's reach from the lash plus from the bend. */
    private static final float SPRING_REACH = (CORD_TOP[2] - SPRING_ANCHOR[2]) + (CORD_TOP[2] - SPRING_BEND[2]);
    private static final float[] HAMMER_AXIS = {-6.5F, 12.5F, 5.5F};
    private static final float CRANK_RADIUS = 2.5F;
    private static final float[] TREADLE_PIVOT = {-14F, 8F, 24F};
    /** The pitman's crank-pin joint at rest; its foot joint rides the treadle tail level with the pivot. */
    private static final float[] PITMAN_PIN = {-15.5F, HAMMER_AXIS[1] + CRANK_RADIUS, HAMMER_AXIS[2]};
    private static final float PITMAN_LENGTH = PITMAN_PIN[1] - TREADLE_PIVOT[1];
    private static final float TREADLE_REACH = TREADLE_PIVOT[2] - HAMMER_AXIS[2];
    private static final float[] HEAD_FACE = {-6.5F, 12.75F, 22.5F};
    private static final float[] WHEEL_AXIS = {24F, 10F, 22F};
    private static final float[] RECORD_AXIS = {22.5F, 19.5F, 8.5F};
    private static final float[] STYLUS_PIVOT = {22.5F, 27F, 3.75F};
    private static final float CARRIAGE_TRAVEL = 4F;
    private static final float[] GOVERNOR_AXIS = {27F, 0F, 12.5F};
    private static final float[] HEART_CENTER = {8F, 9F, 7.5F};

    public ResonantForgeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public boolean shouldRenderOffScreen(ResonantForgeBlockEntity forge) { return true; }
    @Override public int getViewDistance() { return 96; }
    @Override public AABB getRenderBoundingBox(ResonantForgeBlockEntity forge) {
        return new AABB(forge.getBlockPos()).inflate(2);
    }

    @Override public void render(ResonantForgeBlockEntity forge, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        if (forge.getLevel() == null) return;
        Direction facing = forge.getBlockState().getValue(ResonantForgeBlock.FACING);
        int stage = StationTierProperty.stage(forge.getBlockState());
        float time = AnimationTime.gameTime(forge.getLevel(), partial);
        float phase = forge.totalTicks() == 0 ? 0 : Math.min(1, (forge.progress() + partial) / forge.totalTicks());
        Minecraft mc = Minecraft.getInstance();
        Operation operation = forge.operation();
        boolean hammering = operation == Operation.APPLY;
        boolean grinding = operation == Operation.GRIND || operation == Operation.STABILIZE;

        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        pose.translate(-.5, 0, -.5);

        // Hammer bay: the treadle turns the pattern cylinder; its lands lift the helve and let it fall.
        float crank = hammering ? time * 9F : 0;
        float crankRad = (float) Math.toRadians(crank);
        float lobe = (crank % 180F) / 180F;
        float lift = hammering ? (lobe < .8F ? lobe / .8F : (1 - lobe) / .2F) : 0;
        float helve = -11F * lift;
        String helveModel = stage > 0 ? "helve_iron" : "helve";
        pose.pushPose();
        pivot(pose, HELVE_PIVOT, Axis.XP, helve);
        renderModel(mc, helveModel, pose, buffers, light, overlay);
        float hammerWear = Math.min(1F, forge.hammerUses() / (float) ResonantForgeRules.HAMMER_SERVICE_INTERVAL);
        pose.pushPose();
        px(pose, HEAD_FACE);
        pose.scale(1F - hammerWear * .18F, 1F - hammerWear * .35F, 1F - hammerWear * .12F);
        pxBack(pose, HEAD_FACE);
        renderModel(mc, stage > 0 ? "head_iron" : "head", pose, buffers, light, overlay);
        pose.popPose();
        pose.popPose();

        if (stage == 0) {
            // The spring pole rides the cord: lashed at the mast, it flexes so its tip follows the head.
            float helveRad = (float) Math.toRadians(helve);
            float dy = CORD_TOP[1] - HELVE_PIVOT[1], dz = CORD_TOP[2] - HELVE_PIVOT[2];
            float rise = dy * (float) Math.cos(helveRad) - dz * (float) Math.sin(helveRad) - dy;
            float flex = (float) -Math.toDegrees(rise / SPRING_REACH);
            pose.pushPose();
            pivot(pose, SPRING_ANCHOR, Axis.XP, flex);
            renderModel(mc, "spring_pole", pose, buffers, light, overlay);
            pivot(pose, SPRING_BEND, Axis.XP, flex);
            renderModel(mc, "spring_tip", pose, buffers, light, overlay);
            pose.popPose();
        }

        pose.pushPose();
        pivot(pose, HAMMER_AXIS, Axis.XP, crank);
        renderModel(mc, "crank", pose, buffers, light, overlay);
        pose.popPose();

        // Crank pin, pitman and treadle tail are one linkage: the pitman keeps its length between the
        // pin and the tail, so the tail is found where that length meets the treadle's arc.
        float pinY = HAMMER_AXIS[1] + CRANK_RADIUS * (float) Math.cos(crankRad);
        float pinZ = HAMMER_AXIS[2] + CRANK_RADIUS * (float) Math.sin(crankRad);
        float toPinY = pinY - TREADLE_PIVOT[1], toPinZ = pinZ - TREADLE_PIVOT[2];
        float span = (float) Math.sqrt(toPinY * toPinY + toPinZ * toPinZ);
        float along = (TREADLE_REACH * TREADLE_REACH - PITMAN_LENGTH * PITMAN_LENGTH + span * span) / (2 * span);
        float across = (float) Math.sqrt(Math.max(0, TREADLE_REACH * TREADLE_REACH - along * along));
        float tailY = TREADLE_PIVOT[1] + (along * toPinY + across * toPinZ) / span;
        float tailZ = TREADLE_PIVOT[2] + (along * toPinZ - across * toPinY) / span;

        pose.pushPose();
        pose.translate(0, (pinY - PITMAN_PIN[1]) / 16F, (pinZ - PITMAN_PIN[2]) / 16F);
        pivot(pose, PITMAN_PIN, Axis.XP, (float) Math.toDegrees(Math.atan2(pinZ - tailZ, pinY - tailY)));
        renderModel(mc, "pitman", pose, buffers, light, overlay);
        pose.popPose();

        pose.pushPose();
        pivot(pose, TREADLE_PIVOT, Axis.XP, (float) Math.toDegrees(Math.atan2(
                tailY - TREADLE_PIVOT[1], TREADLE_PIVOT[2] - tailZ)));
        renderModel(mc, "treadle", pose, buffers, light, overlay);
        pose.popPose();

        renderCylinder(mc, forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER), HAMMER_AXIS, crank,
                pose, buffers, light, overlay);

        // Grinder bay: the stone drives the recording cylinder; the stylus cuts what the grind says.
        float wheelWear = Math.min(1F, forge.wheelUses() / (float) ResonantForgeRules.WHEEL_SERVICE_INTERVAL);
        pose.pushPose();
        if (wheelWear > .5F) pivot(pose, WHEEL_AXIS, Axis.YP, (float) Math.sin(time * .45F) * 7 * (wheelWear - .5F) * 2);
        pivot(pose, WHEEL_AXIS, Axis.ZP, grinding ? -time * 24F : 0);
        renderModel(mc, "wheel", pose, buffers, light, overlay);
        pose.popPose();

        renderCylinder(mc, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER), RECORD_AXIS,
                grinding ? time * 12F : 0, pose, buffers, light, overlay);

        float chatter = grinding ? (float) Math.sin(time * 2.3F) * .6F + (float) Math.sin(time * 5.1F) * .3F : 0;
        if (stage == 0) {
            pose.pushPose();
            pivot(pose, STYLUS_PIVOT, Axis.XP, chatter);
            renderModel(mc, "stylus", pose, buffers, light, overlay);
            pose.popPose();
        } else {
            pose.pushPose();
            float travel = grinding ? CARRIAGE_TRAVEL * (2 * phase - 1) : 0;
            pose.translate(travel / 16F, chatter * .05F / 16F, 0);
            renderModel(mc, "carriage", pose, buffers, light, overlay);
            pose.popPose();

            pose.pushPose();
            pivot(pose, GOVERNOR_AXIS, Axis.YP, grinding ? time * 30F : 0);
            renderModel(mc, "governor", pose, buffers, light, overlay);
            pose.popPose();
        }

        if (stage == 2) {
            // The heart keeps a slow resting beat and quickens while the forge works.
            float rate = operation == Operation.NONE ? .16F : .42F;
            float beat = (float) Math.pow(Math.max(0, Math.sin(time * rate)), 6);
            pose.pushPose();
            px(pose, HEART_CENTER);
            pose.scale(1 + beat * .07F, 1 + beat * .05F, 1 + beat * .07F);
            pxBack(pose, HEART_CENTER);
            renderModel(mc, "heart", pose, buffers, light, overlay);
            pose.popPose();
        }

        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM), pose, buffers, light, overlay,
                -6.5, 12.1, 22, .45F);
        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM), pose, buffers, light, overlay,
                15.75, 10.1, 22, .4F);
        if (forge.hammerIronDeposited()) renderItem(mc, new ItemStack(BlockInit.hematic_iron_block.get()),
                pose, buffers, light, overlay, -13, 3.5, 15, .35F);
        if (forge.hammerAshDeposited()) renderItem(mc, new ItemStack(BlockInit.smouldering_ash_trail.get()),
                pose, buffers, light, overlay, -10.25, 7.1, 16.5, .28F);
        if (forge.wheelAshDeposited()) renderItem(mc, new ItemStack(BlockInit.befouling_ash_trail.get()),
                pose, buffers, light, overlay, 27, 4.4, 20, .28F);

        if (forge.getBloodVolume() > 0) {
            float height = (3.25F + 5.25F * (float) (forge.getBloodVolume() / ResonantForgeRules.BLOOD_CAPACITY)) / 16F;
            Matrix4f matrix = pose.last().pose();
            // Held just off the trough's inner walls (x 3..13, z 16.5..28.5) so the sides never z-fight.
            box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix,
                    3.05F / 16, height, 16.55F / 16, 12.95F / 16, height + .025F, 28.45F / 16, 155, 12, 18, 205);
            if (operation != Operation.NONE)
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix,
                        3.5F / 16, height + .026F, 17F / 16, 12.5F / 16, height + .04F, 28F / 16, 235, 45, 54, 175);
        }
        pose.popPose();
    }

    private static void renderCylinder(Minecraft mc, ItemStack stack, float[] axis, float spin, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        if (stack.isEmpty()) return;
        pose.pushPose();
        px(pose, axis);
        pose.mulPose(Axis.XP.rotationDegrees(spin));
        renderModel(mc, stack.is(ItemInit.ambergris_cylinder.get()) ? "cylinder_ambergris" : "cylinder_wax",
                pose, buffers, light, overlay);
        pose.popPose();
    }

    private static void renderModel(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/resonant_forge_" + name)));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
                model, 1, 1, 1, light, overlay);
    }

    private static void px(PoseStack pose, float[] at) { pose.translate(at[0] / 16F, at[1] / 16F, at[2] / 16F); }
    private static void pxBack(PoseStack pose, float[] at) { pose.translate(-at[0] / 16F, -at[1] / 16F, -at[2] / 16F); }

    private static void pivot(PoseStack pose, float[] at, Axis axis, float degrees) {
        px(pose, at);
        pose.mulPose(axis.rotationDegrees(degrees));
        pxBack(pose, at);
    }

    private static void renderItem(Minecraft mc, ItemStack stack, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay, double x, double y, double z, float scale) {
        if (stack.isEmpty()) return;
        pose.pushPose(); pose.translate(x / 16, y / 16, z / 16);
        pose.mulPose(Axis.XP.rotationDegrees(90)); pose.scale(scale, scale, scale);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, null, 0);
        pose.popPose();
    }

    private static void box(VertexConsumer out, Matrix4f matrix, float x0, float y0, float z0,
            float x1, float y1, float z1, int r, int g, int b, int a) {
        quad(out,matrix,x0,y1,z0,x1,y1,z0,x1,y1,z1,x0,y1,z1,r,g,b,a);
        quad(out,matrix,x0,y0,z0,x1,y0,z0,x1,y1,z0,x0,y1,z0,r,g,b,a);
        quad(out,matrix,x1,y0,z1,x0,y0,z1,x0,y1,z1,x1,y1,z1,r,g,b,a);
        quad(out,matrix,x0,y0,z1,x0,y0,z0,x0,y1,z0,x0,y1,z1,r,g,b,a);
        quad(out,matrix,x1,y0,z0,x1,y0,z1,x1,y1,z1,x1,y1,z0,r,g,b,a);
    }

    private static void quad(VertexConsumer out, Matrix4f matrix,
            float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,
            int r,int g,int b,int a) {
        out.addVertex(matrix,ax,ay,az).setColor(r,g,b,a); out.addVertex(matrix,bx,by,bz).setColor(r,g,b,a);
        out.addVertex(matrix,cx,cy,cz).setColor(r,g,b,a); out.addVertex(matrix,dx,dy,dz).setColor(r,g,b,a);
    }
}
