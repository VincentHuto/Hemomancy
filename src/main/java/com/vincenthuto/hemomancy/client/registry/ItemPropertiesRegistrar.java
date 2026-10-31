package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.DrudgeElectrodeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingCrossbowItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFittingHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ItemPropertiesRegistrar {

    private ItemPropertiesRegistrar() {
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void registerItemPropertyOverrides(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // The Court house marker's stack picks its house's model (item/house_marker overrides).
            ItemProperties.register(com.vincenthuto.hemomancy.common.init.BlockInit.house_marker.get().asItem(),
                    Hemomancy.rloc("house"), (stack, world, entity, seed) ->
                            com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.HouseMarkerBlockItem
                                    .house(stack).ordinal());
            // The Saint Sarcophagus stack picks its Saint's palette (item/saint_sarcophagus overrides).
            ItemProperties.register(com.vincenthuto.hemomancy.common.init.BlockInit.saint_sarcophagus.get().asItem(),
                    Hemomancy.rloc("saint"), (stack, world, entity, seed) ->
                            com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SaintSarcophagusBlockItem
                                    .palette(stack));

            ItemProperties.register(ItemInit.unsigned_ancestral_ledger.get(), Hemomancy.rloc("unsigned"),
                    HemoItemProperties.booleanTag("state"));

            ItemProperties.register(ItemInit.bloody_vial.get(), Hemomancy.rloc("state"),
                    (stack, world, entity, seed) -> com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.isFilled(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ItemInit.ahaematic_colloid.get(),Hemomancy.rloc("colloid_motion"),
                    (stack,world,entity,seed)->com.vincenthuto.hemomancy.common.item.component.AhaematicSample.is(stack)?com.vincenthuto.hemomancy.client.sound.AntecedentClientEffects.sampleFrame():0);

            ItemProperties.register(ItemInit.vial_rack.get(), Hemomancy.rloc("state"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) -> {
                        int emptyCount = VialRackItem.countEmptyVials(stack);
                        if (emptyCount == VialRackItem.MAX_VIALS) {
                            return 0.0F;
                        }
                        if (emptyCount == 0) {
                            return 2.0F;
                        }
                        return 1.0F;
                    });

            ItemProperties.register(ItemInit.barbed_shield.get(), ResourceLocation.withDefaultNamespace("blocking"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(ItemInit.chitinite_shield.get(), ResourceLocation.withDefaultNamespace("blocking"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) -> {
                        if (ent == null) {
                            return 0.0F;
                        }
                        return LivingCrossbowItem.isCharged(stack) ? 0.0F
                                : (float) (stack.getUseDuration(ent) - ent.getUseItemRemainingTicks())
                                  / (float) LivingCrossbowItem.getChargeTime(stack);
                    });
            ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            ent != null && ent.isUsingItem() && ent.getUseItem() == stack
                                    && !LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("charged"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            stack != null && LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ItemInit.living_crossbow.get(), ResourceLocation.withDefaultNamespace("firework"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            ent != null && LivingCrossbowItem.isCharged(stack)
                                    && LivingCrossbowItem.hasChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);

            ItemProperties.register(ItemInit.living_syringe.get(), Hemomancy.rloc("open"),
                    HemoItemProperties.booleanTag("state"));
            ItemProperties.register(ItemInit.curved_horn.get(), Hemomancy.rloc("open"),
                    HemoItemProperties.booleanTag("state"));
            ItemProperties.register(ItemInit.blood_gourd_white.get(), Hemomancy.rloc("open"),
                    HemoItemProperties.booleanTag("state"));
            ItemProperties.register(ItemInit.blood_gourd_red.get(), Hemomancy.rloc("open"),
                    HemoItemProperties.booleanTag("state"));
            ItemProperties.register(ItemInit.blood_gourd_black.get(), Hemomancy.rloc("open"),
                    HemoItemProperties.booleanTag("state"));
            ItemProperties.register(ItemInit.morphling_jar.get(), Hemomancy.rloc("size"),
                    HemoItemProperties.intTag("size"));

            ItemProperties.register(ItemInit.living_weapon_graft.get(), Hemomancy.rloc("form"),
                    (ItemStack stack, ClientLevel world, LivingEntity ent, int seed) ->
                            LivingWeaponGraftData.fromStack(stack)
                                    .map(data -> (float) data.form().ordinal() + 1.0F)
                                    .orElse(1.0F));

            ItemProperties.register(ItemInit.drudge_electrode.get(), Hemomancy.rloc("mode"),
                    HemoItemProperties.booleanTag(DrudgeElectrodeItem.TAG_MODE));

            ItemProperties.register(ItemInit.living_staff.get(), Hemomancy.rloc("staff_visual"), new ItemPropertyFunction() {
                @Override
                public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int seed) {
                    Player player = ent instanceof Player livingPlayer ? livingPlayer : Minecraft.getInstance().player;
                    int fittingVisual = LivingStaffFittingHelper.staffVisualFor(player);
                    if (fittingVisual > 0) {
                        return fittingVisual;
                    }
                    return staffVisualFromMorphlingInventory(stack, world);
                }

                private float staffVisualFromMorphlingInventory(ItemStack stack, ClientLevel world) {
                    if (!stack.has(DataComponents.CUSTOM_DATA)) {
                        return 0.0F;
                    }
                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    CompoundTag items = (CompoundTag) tag.get("Inventory");
                    if (items == null || !items.contains("Items", 9)) {
                        return 0.0F;
                    }

                    ItemStack selectedStack = ItemStack.parseOptional(
                            world != null ? world.registryAccess() : RegistryAccess.EMPTY,
                            ((ListTag) items.get("Items")).getCompound(0));
                    // Staff visuals 1-8 follow the strain table's order.
                    return MorphlingStrains.of(selectedStack)
                            .map(strain -> (float) (MorphlingStrains.ALL.indexOf(strain) + 1))
                            .orElse(0.0F);
                }
            });
        });
    }
}
