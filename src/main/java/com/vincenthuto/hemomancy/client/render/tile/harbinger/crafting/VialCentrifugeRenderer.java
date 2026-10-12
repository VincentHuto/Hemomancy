package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.VialCentrifugeBlock;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeRotorRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Spins the Vial Centrifuge's rotor over its baked stage body: swing buckets carrying the loaded
 * vials, the base crank or upgraded flywheel, governor, dial needle, Fractionating spleen and the
 * reservoir jar's blood. Pivots are model pixels in the north-facing frame and mirror
 * tools/model_export/vial_centrifuge.py.
 */
public class VialCentrifugeRenderer implements BlockEntityRenderer<VialCentrifugeBlockEntity> {
    public static final String[] PART_MODELS = {"rotor", "rotor_calibrated", "rotor_fractionating", "bucket",
            "vial_glass", "vial_blood", "vial_spent", "vial_hallowed", "crank", "flywheel", "governor",
            "governor_crowned", "flyball", "needle", "spleen", "reservoir_blood"};

    private static final float[] CENTRE = {8F, 0F, 8F};
    private static final float[] PIVOT = {12F, 14.75F, 8F};
    private static final float[] CRANK_AXIS = {0F, 4.5F, 8F};
    private static final float[] DIAL = {8F, 4.75F, 1.375F};
    private static final float[] HINGE = {8.5F, 22F, 8F};
    private static final float[] SPLEEN = {8F, 4.5F, 14.125F};
    /** Reservoir fluid model: one pixel tall from the jar floor, stretched to the stored level. */
    private static final float[] JAR_FLOOR = {3F, 2.4F, 3F};
    private static final float JAR_DEPTH = 6.2F - 2.4F;

    public VialCentrifugeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public AABB getRenderBoundingBox(VialCentrifugeBlockEntity centrifuge) {
        // The governor rises well above the block and the drive handle reaches past its east face.
        return new AABB(centrifuge.getBlockPos()).inflate(.25).expandTowards(0, 1, 0);
    }

    @Override
    public void render(VialCentrifugeBlockEntity te, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (te.getLevel() == null) return;
        Direction facing = te.getBlockState().getValue(VialCentrifugeBlock.FACING);
        int stage = StationTierProperty.stage(te.getBlockState());
        float rotor = Mth.lerp(partial, te.prevRotorAngle, te.rotorAngle);
        float speed = Mth.lerp(partial, te.prevRotorSpeed, te.rotorSpeed);
        float drive = Mth.lerp(partial, te.prevDriveAngle, te.driveAngle);
        String[] vials = new String[8];
        for (int slot = 2; slot <= 9; slot++) vials[slot - 2] = contents(te.getItem(slot));

        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        pose.translate(-.5, 0, -.5);
        float time = te.getLevel().getGameTime() + partial;
        renderMachine(Minecraft.getInstance(), stage, rotor, drive,
                VialCentrifugeRotorRules.speedFraction(stage, speed), time, vials, pose, buffers, light, overlay);

        double max = te.getMaxBloodVolume();
        float fill = max <= 0 ? 0 : (float) Math.min(1, te.getBloodVolume() / max);
        if (fill > 0) {
            pose.pushPose();
            px(pose, JAR_FLOOR);
            pose.scale(1, JAR_DEPTH * fill, 1);
            pxBack(pose, JAR_FLOOR);
            renderModel(Minecraft.getInstance(), "reservoir_blood", pose, buffers, light, overlay, false);
            pose.popPose();
        }
        pose.popPose();
    }

    /** Which vial contents a loaded slot shows, or null for an empty bucket. */
    public static String contents(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (!BloodSampleData.isFilled(stack)) return "vial_spent";
        return stack.getItem() instanceof ConsecratedSyringeItem ? "vial_hallowed" : "vial_blood";
    }

    /**
     * Draws every moving part in the north-facing block frame. `rotor` and `drive` are angles in
     * degrees; `fraction` is the rotor's share of its top speed. Shared with the item renderer.
     */
    public static void renderMachine(Minecraft mc, int stage, float rotor, float drive, float fraction, float time,
            String[] vials, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pivot(pose, CENTRE, Axis.YP, rotor);
        renderModel(mc, switch (stage) { case 1 -> "rotor_calibrated"; case 2 -> "rotor_fractionating"; default -> "rotor"; },
                pose, buffers, light, overlay, false);
        if (stage > 0) renderModel(mc, stage == 2 ? "governor_crowned" : "governor", pose, buffers, light, overlay, false);
        pose.popPose();

        float swing = VialCentrifugeRotorRules.swing(fraction);
        for (int k = 0; k < 8; k++) {
            pose.pushPose();
            pivot(pose, CENTRE, Axis.YP, rotor + VialCentrifugeRotorRules.slotAngle(k + 2));
            pivot(pose, PIVOT, Axis.ZP, swing);
            renderModel(mc, "bucket", pose, buffers, light, overlay, false);
            String vial = vials == null ? null : vials[k];
            if (vial != null) {
                renderModel(mc, vial, pose, buffers, light, overlay, false);
                renderModel(mc, "vial_glass", pose, buffers, light, overlay, true);
            }
            pose.popPose();
        }

        if (stage > 0) {
            float lift = VialCentrifugeRotorRules.governorLift(fraction);
            for (int k = 0; k < 2; k++) {
                pose.pushPose();
                pivot(pose, CENTRE, Axis.YP, rotor + k * 180F);
                pivot(pose, HINGE, Axis.ZP, lift);
                renderModel(mc, "flyball", pose, buffers, light, overlay, false);
                pose.popPose();
            }
            pose.pushPose();
            pivot(pose, DIAL, Axis.ZP, VialCentrifugeRotorRules.needle(fraction));
            renderModel(mc, "needle", pose, buffers, light, overlay, false);
            pose.popPose();
        }

        pose.pushPose();
        pivot(pose, CRANK_AXIS, Axis.XP, drive);
        renderModel(mc, stage == 0 ? "crank" : "flywheel", pose, buffers, light, overlay, false);
        pose.popPose();

        if (stage == 2) {
            // The grafted spleen keeps a slow resting pulse and labours while the rotor runs.
            float rate = .12F + .3F * fraction;
            float beat = (float) Math.pow(Math.max(0, Math.sin(time * rate)), 4);
            pose.pushPose();
            px(pose, SPLEEN);
            pose.scale(1 + beat * .05F, 1 + beat * .08F, 1 + beat * .06F);
            pxBack(pose, SPLEEN);
            renderModel(mc, "spleen", pose, buffers, light, overlay, false);
            pose.popPose();
        }
    }

    private static void renderModel(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay, boolean translucent) {
        var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/vial_centrifuge_" + name)));
        var buffer = buffers.getBuffer(translucent ? RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)
                : RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, model, 1, 1, 1, light, overlay);
    }

    private static void px(PoseStack pose, float[] at) { pose.translate(at[0] / 16F, at[1] / 16F, at[2] / 16F); }
    private static void pxBack(PoseStack pose, float[] at) { pose.translate(-at[0] / 16F, -at[1] / 16F, -at[2] / 16F); }

    private static void pivot(PoseStack pose, float[] at, Axis axis, float degrees) {
        px(pose, at);
        pose.mulPose(axis.rotationDegrees(degrees));
        pxBack(pose, at);
    }
}
