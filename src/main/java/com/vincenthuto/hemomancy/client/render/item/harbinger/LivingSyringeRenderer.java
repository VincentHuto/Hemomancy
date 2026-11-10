package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawClientState;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawTimeline;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class LivingSyringeRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ModelResourceLocation WRIST = part("living_syringe/wrist");
    private static final ModelResourceLocation CHAMBER = part("living_syringe/chamber");
    private static final ModelResourceLocation VIAL_EMPTY = part("living_syringe/vial_empty");
    private static final ModelResourceLocation VIAL_FILLED = part("living_syringe/vial_filled");
    private static final ModelResourceLocation[] RACK = new ModelResourceLocation[VialRackItem.MAX_VIALS + 1];
    private static final RenderType SKIN = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);

    static {
        RACK[0] = part("vial_rack/empty");
        for (int i = 1; i < RACK.length; i++) RACK[i] = part("vial_rack/filled_" + i);
    }

    private static LivingSyringeRenderer renderer;
    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return instance();
        }
    };

    private final WeakHashMap<LivingEntity, LivingSyringeMotion[]> motion = new WeakHashMap<>();
    // applyTransform runs immediately before the item renderer calls renderByItem.
    private LivingEntity renderOwner;
    private boolean leftHand;

    private LivingSyringeRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private static LivingSyringeRenderer instance() {
        if (renderer == null) renderer = new LivingSyringeRenderer();
        return renderer;
    }

    public static void registerModels(Consumer<ModelResourceLocation> register) {
        register.accept(WRIST);
        register.accept(CHAMBER);
        register.accept(VIAL_EMPTY);
        register.accept(VIAL_FILLED);
        for (var rack : RACK) register.accept(rack);
    }

    public static BakedModel withRenderOwner(BakedModel model) {
        return new BakedModelWrapper<>(model) {
            private final ItemOverrides overrides = new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel ignored, ItemStack stack, @Nullable ClientLevel level,
                        @Nullable LivingEntity entity, int seed) {
                    return new BakedModelWrapper<>(model) {
                        @Override
                        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poses, boolean left) {
                            var renderer = instance();
                            renderer.renderOwner = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? entity : null;
                            renderer.leftHand = left;
                            return model.applyTransform(context, poses, left);
                        }
                    };
                }
            };

            @Override
            public ItemOverrides getOverrides() {
                return overrides;
            }
        };
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        LivingEntity owner = renderOwner;
        renderOwner = null;
        var client = Minecraft.getInstance();
        int load = LivingSyringeItem.modelLoad(stack,
                client.level == null ? RegistryAccess.EMPTY : client.level.registryAccess());
        int drumLoad = load;
        // While a sample is being drawn the syringe plays it out, whatever the item already says.
        var draw = owner == null ? null : LivingSyringeDrawClientState.animation(owner);
        if (draw != null && draw.packet.right() != leftHand) {
            float tick = draw.elapsed(AnimationTime.partialTick());
            load = LivingSyringeDrawTimeline.shownLoad(draw.packet.load(), tick);
            drumLoad = LivingSyringeDrawTimeline.drumLoad(draw.packet.load(), tick);
        }
        var pose = owner == null ? LivingSyringeMotion.resting(drumLoad) : animate(owner, drumLoad);
        var vertices = ItemRenderer.getFoilBufferDirect(buffers, SKIN, true, stack.hasFoil());
        var items = client.getItemRenderer();
        var models = client.getModelManager();
        // Vanilla already mirrors the left display transform; this mirrors the off-centre geometry to match.
        if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            poses.translate(1, 0, 0);
            poses.scale(-1, 1, 1);
        }
        items.renderModelLists(models.getModel(WRIST), stack, light, overlay, poses, vertices);

        poses.pushPose();
        // Fixed hinge below the breech. The chamber, fang and installed vessel move together.
        poses.translate(6.5 / 16, 5.25 / 16, 4.25 / 16);
        poses.mulPose(Axis.XP.rotationDegrees(pose.hinge()));
        poses.translate(-6.5 / 16, -5.25 / 16, -4.25 / 16);
        items.renderModelLists(models.getModel(CHAMBER), stack, light, overlay, poses, vertices);
        if (load >= 3) {
            // Turn the detached drum onto the barrel axis, then index the next vial into the upper port.
            poses.translate(6.5 / 16, 9.25 / 16, .5);
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.mulPose(Axis.ZP.rotationDegrees(pose.drum()));
            poses.translate(-.5, -.5, -.5);
            items.renderModelLists(models.getModel(RACK[load - 3]), stack, light, overlay, poses, vertices);
        } else if (load != 0) {
            items.renderModelLists(models.getModel(load == 1 ? VIAL_EMPTY : VIAL_FILLED),
                    stack, light, overlay, poses, vertices);
        }
        poses.popPose();
    }

    private LivingSyringeMotion.Pose animate(LivingEntity owner, int load) {
        var hands = motion.computeIfAbsent(owner, ignored ->
                new LivingSyringeMotion[]{new LivingSyringeMotion(), new LivingSyringeMotion()});
        boolean mainHand = leftHand == (owner.getMainArm() == HumanoidArm.LEFT);
        int slot = mainHand && owner instanceof Player player ? player.getInventory().selected : -1;
        double tick = AnimationTime.gameTimeAsDouble(owner.level());
        return hands[leftHand ? 0 : 1].sample(load, slot, tick);
    }

    private static ModelResourceLocation part(String path) {
        return ModelResourceLocation.standalone(Hemomancy.rloc("item/" + path));
    }
}
