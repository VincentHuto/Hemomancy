package com.vincenthuto.hemomancy.client.render.outpouring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.layer.player.AvatarManifestationTransition;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

/**
 * Shader veins spreading from the cast's vascular section, drawn over the skin and, on armoured parts, over a shell baked
 * with the skin's UVs just outside the armour, so skin and armour read as one vein field. The vein shape comes from the
 * body atlas, so it never depends on the skin's pixels.
 */
public final class CastVeinsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static PlayerModel<AbstractClientPlayer> wideShell, slimShell;

    public CastVeinsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) { super(parent); }

    @Override public void render(PoseStack poses, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                                 float limbSwing, float limbSwingAmount, float partial, float age, float yaw, float pitch) {
        if (player.isInvisible() || avatar(player, partial)) return;
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int setting = Minecraft.getInstance().options.particles().get().getId();
        if (OutpouringRules.detail(setting, camera.distanceToSqr(player.position())) == OutpouringRules.Detail.NONE) return;
        var veins = OutpouringClientState.veins(player, partial);
        if (!veins.active()) return;
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        OutpouringRenderTypes.begin(AnimationTime.gameTime(player.level(), partial));
        var consumer = buffer.getBuffer(OutpouringRenderTypes.veins(slim));
        var packed = veins.packed();
        boolean head = armoured(player, EquipmentSlot.HEAD), chest = armoured(player, EquipmentSlot.CHEST);
        boolean legs = armoured(player, EquipmentSlot.LEGS) || armoured(player, EquipmentSlot.FEET);
        boolean body = chest || armoured(player, EquipmentSlot.LEGS);

        PlayerModel<AbstractClientPlayer> model = getParentModel();
        boolean[] saved = visibility(model);
        // Veins sit on the base parts; the hat, jacket, sleeves and pants share their body points, so drawing both doubles them.
        // Parts something else hid (morphling growths, held-item rules) stay hidden for the veins too.
        setVisible(model, saved, !head, !body, !chest, !legs);
        model.renderToBuffer(poses, consumer, packed.light(), packed.overlay(), packed.color());
        restore(model, saved);

        if (head || body || chest || legs) {
            var shell = shell(slim);
            model.copyPropertiesTo(shell);
            setVisible(shell, saved, head, body, chest, legs);
            shell.renderToBuffer(poses, consumer, packed.light(), packed.overlay(), packed.color());
        }
    }

    /** Veins on the posed first-person arm, in the same pose the vanilla arm was just drawn with. */
    public static void firstPersonArm(PlayerModel<?> model, HumanoidArm arm, PoseStack poses, MultiBufferSource buffer,
                                      AbstractClientPlayer player, float partial) {
        if (player.isInvisible() || avatar(player, partial)) return;
        var veins = OutpouringClientState.veins(player, partial);
        if (!veins.active()) return;
        OutpouringRenderTypes.begin(AnimationTime.gameTime(player.level(), partial));
        var packed = veins.packed();
        ModelPart part = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        part.render(poses, buffer.getBuffer(OutpouringRenderTypes.veins(player.getSkin().model() == PlayerSkin.Model.SLIM)),
                packed.light(), packed.overlay(), packed.color());
    }

    private static boolean avatar(Player player, float partial) {
        String form = HemoCapabilityAccess.getKnownManipulations(player).getActiveAvatarForm();
        return form != null && !form.isEmpty()
                && AvatarManifestationTransition.sample(player.getUUID(), form, player.tickCount, partial).renders();
    }

    private static boolean armoured(Player player, EquipmentSlot slot) {
        return player.getItemBySlot(slot).getItem() instanceof ArmorItem;
    }

    private static PlayerModel<AbstractClientPlayer> shell(boolean slim) {
        if (slim ? slimShell == null : wideShell == null) {
            // Outer armour sits a pixel out; the shell sits a tenth further so the veins draw on top of it.
            var root = LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(1.1f), slim), 64, 64).bakeRoot();
            var shell = new PlayerModel<AbstractClientPlayer>(root, slim);
            if (slim) slimShell = shell; else wideShell = shell;
        }
        return slim ? slimShell : wideShell;
    }

    private static void setVisible(PlayerModel<?> model, boolean[] shown, boolean head, boolean body, boolean arms, boolean legs) {
        model.head.visible = shown[0] && head; model.body.visible = shown[1] && body;
        model.rightArm.visible = shown[2] && arms; model.leftArm.visible = shown[3] && arms;
        model.rightLeg.visible = shown[4] && legs; model.leftLeg.visible = shown[5] && legs;
        model.hat.visible = false; model.jacket.visible = false;
        model.leftSleeve.visible = false; model.rightSleeve.visible = false;
        model.leftPants.visible = false; model.rightPants.visible = false;
    }

    private static boolean[] visibility(PlayerModel<?> m) {
        return new boolean[]{m.head.visible, m.body.visible, m.rightArm.visible, m.leftArm.visible, m.rightLeg.visible,
                m.leftLeg.visible, m.hat.visible, m.jacket.visible, m.leftSleeve.visible, m.rightSleeve.visible,
                m.leftPants.visible, m.rightPants.visible};
    }

    private static void restore(PlayerModel<?> m, boolean[] v) {
        m.head.visible = v[0]; m.body.visible = v[1]; m.rightArm.visible = v[2]; m.leftArm.visible = v[3];
        m.rightLeg.visible = v[4]; m.leftLeg.visible = v[5]; m.hat.visible = v[6]; m.jacket.visible = v[7];
        m.leftSleeve.visible = v[8]; m.rightSleeve.visible = v[9]; m.leftPants.visible = v[10]; m.rightPants.visible = v[11];
    }
}
