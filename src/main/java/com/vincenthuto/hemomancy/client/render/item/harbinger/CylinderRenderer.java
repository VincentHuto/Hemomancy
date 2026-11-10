package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class CylinderRenderer extends BlockEntityWithoutLevelRenderer {
    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        private CylinderRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new CylinderRenderer();
            return renderer;
        }
    };
    private static final RenderType SKIN = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);

    private enum MediaModel {
        WAX("wax_cylinder"), AMBERGRIS("ambergris_cylinder"), CORPSE_WAX("corpse_wax_cylinder");

        private final ModelResourceLocation blank;
        private final ModelResourceLocation recorded;

        MediaModel(String path) {
            blank = ModelResourceLocation.standalone(Hemomancy.rloc("item/cylinder/" + path));
            recorded = ModelResourceLocation.standalone(Hemomancy.rloc("item/cylinder/" + path + "_recorded"));
        }

        static MediaModel forStack(ItemStack stack) {
            if (stack.is(ItemInit.wax_cylinder.get())) return WAX;
            if (stack.is(ItemInit.corpse_wax_cylinder.get())) return CORPSE_WAX;
            return AMBERGRIS;
        }
    }

    private CylinderRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void registerModels(Consumer<ModelResourceLocation> register) {
        for (MediaModel model : MediaModel.values()) {
            register.accept(model.blank);
            register.accept(model.recorded);
        }
    }

    /** Draw the loaded medium around the machine's spindle origin. */
    public static void drawMounted(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(-.5, -.5, -.5);
        draw(stack, pose, buffers, light, overlay);
        pose.popPose();
    }

    private static void draw(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var pattern = stack.get(DataComponentInit.RESONANT_PATTERN.get());
        boolean ancient = stack.has(DataComponentInit.ANCIENT_RECORDING.get());
        boolean recorded = stack.has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get());
        var medium = MediaModel.forStack(stack);
        var mc = Minecraft.getInstance();
        var model = mc.getModelManager().getModel(pattern != null || ancient || recorded ? medium.recorded : medium.blank);
        var renderer = mc.getItemRenderer();
        renderer.renderModelLists(model, stack, light, overlay, pose, buffers.getBuffer(SKIN));
        if (pattern != null || recorded && !ancient)
            renderer.renderModelLists(model, stack, light, overlay, pose, buffers.getBuffer(RenderTypeInit.getCrimsonGlint()));
        if (pattern != null && pattern.master())
            renderer.renderModelLists(model, stack, light, overlay, pose, buffers.getBuffer(RenderType.entityGlint()));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        draw(stack, pose, buffers, light, overlay);
    }
}
