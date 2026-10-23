package com.vincenthuto.hemomancy.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.item.SpecialBloodLustClientExtensions;
import com.vincenthuto.hemomancy.client.model.armor.BarbedArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.ChalybeateFortressArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.ChitiniteArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.CovenantLeaderArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.HemolymphopodaHeadArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.LanternTickHeadArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.MarrowCrownModel;
import com.vincenthuto.hemomancy.client.model.armor.PrismaticArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.UnstainedArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.VestmentOfTheFinalMoltArmorModel;
import com.vincenthuto.hemomancy.client.player.LivingWeaponPlayerPose;
import com.vincenthuto.hemomancy.client.player.UnstainedWeaponPlayerPose;
import com.vincenthuto.hemomancy.client.render.item.BloodGourdItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.HeartyCompassItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.HemolymphopodaHeadpieceItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.LanternTickHelmetItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.MarrowCrownItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.MemoryOfVesperItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.ModelBackedArmorItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.MonolithFragmentItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.MonolithImbuedClothItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.MonolithicCornerstoneItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.QliphothPomeItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.QliphothSeedItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.ScarPatternItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.SilentArchonArmorItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.SporiticThuribleItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.AnnettasSanguisLanceaItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.BarbedShieldItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.ChitiniteShieldItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingAxeItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingBladeItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingFlailItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingPistolItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingSickleItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingSpearItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingTorchItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.SanguisLanceaItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar.MarionetteCrossbarItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting.HematicArmatureItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting.PuppeteersSpindleItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting.VialCentrifugeItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.CovenantThroneItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.EarthenVeinItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.FungalImplantationPylonItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.HematicStakeItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.MasonsEffigyItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.MortalDisplayItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.SanguineMonolithItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.SpecimenJarItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.functional.VisceralMirrorItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.rite.SuspendedBloodCrystalItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.rite.SuspendedVivianiteItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.tile.unstained.SuspendedCleansedBloodCrystalItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.unstained.AbsolutionDaggerItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.unstained.SilthmereGlaiveItemRenderer;
import com.vincenthuto.hemomancy.client.render.item.unstained.UnstainedWarhammerItemRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.AmbergrisCylinderItem;
import com.vincenthuto.hemomancy.common.item.harbinger.MemoryOfVesperItem;
import com.vincenthuto.hemomancy.common.item.harbinger.MonolithFragmentItem;
import com.vincenthuto.hemomancy.common.item.harbinger.MonolithImbuedClothItem;
import com.vincenthuto.hemomancy.common.item.harbinger.MonolithicCornerstoneItem;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothSeedItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.AbstractSpecialBloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.MarrowCrownArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.SilentArchonArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HeartyCompassItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarBlockItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBaghnakhItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBladeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingPistolItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.SanguisLanceaItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.BarbedArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.BarbedShieldItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.ChitiniteArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.ChitiniteShieldItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.CovenantMantleArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.HemolymphopodaHeadpieceArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.LanternTickHelmetArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.PrismaticArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.VenousStriderSabatonsItem;
import com.vincenthuto.hemomancy.common.item.unstained.armor.UnstainedArmorItem;
import com.vincenthuto.hemomancy.common.item.unstained.armor.VestmentOfTheFinalMoltArmorItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AnnettasSanguisLanceaItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * O-6: every Hemomancy item's client renderer and armour model, registered by {@code ClientExtensionsRegistrar}
 * in {@code RegisterClientExtensionsEvent}. An item class maps to its factory here (subclasses share it); a block item
 * with only a custom renderer is a plain {@code BlockItem} and maps by its block id. Common item classes name no
 * client class.
 */
public final class ItemClientExtensions {
	static final Map<Class<? extends Item>, Function<Item, IClientItemExtensions>> BY_CLASS = Map.ofEntries(
			Map.entry(AmbergrisCylinderItem.class, ItemClientExtensions::ambergrisCylinderItem),
			Map.entry(AbstractSpecialBloodLustArmorItem.class, ItemClientExtensions::abstractSpecialBloodLustArmorItem),
			Map.entry(BloodLustArmorItem.class, ItemClientExtensions::bloodLustArmorItem),
			Map.entry(MarrowCrownArmorItem.class, ItemClientExtensions::marrowCrownArmorItem),
			Map.entry(SilentArchonArmorItem.class, ItemClientExtensions::silentArchonArmorItem),
			Map.entry(HeartyCompassItem.class, ItemClientExtensions::heartyCompassItem),
			Map.entry(MemoryOfVesperItem.class, ItemClientExtensions::memoryOfVesperItem),
			Map.entry(MonolithFragmentItem.class, ItemClientExtensions::monolithFragmentItem),
			Map.entry(MonolithicCornerstoneItem.class, ItemClientExtensions::monolithicCornerstoneItem),
			Map.entry(MonolithImbuedClothItem.class, ItemClientExtensions::monolithImbuedClothItem),
			Map.entry(QliphothPomeItem.class, ItemClientExtensions::qliphothPomeItem),
			Map.entry(QliphothSeedItem.class, ItemClientExtensions::qliphothSeedItem),
			Map.entry(ItemScarPattern.class, ItemClientExtensions::itemScarPattern),
			Map.entry(SpecimenJarBlockItem.class, ItemClientExtensions::specimenJarBlockItem),
			Map.entry(BloodGourdItem.class, ItemClientExtensions::bloodGourdItem),
			Map.entry(BloodAbsorptionItem.class, ItemClientExtensions::bloodAbsorptionItem),
			Map.entry(BloodProjectionItem.class, ItemClientExtensions::bloodProjectionItem),
			Map.entry(LivingAxeItem.class, ItemClientExtensions::livingAxeItem),
			Map.entry(LivingBaghnakhItem.class, ItemClientExtensions::livingBaghnakhItem),
			Map.entry(LivingBladeItem.class, ItemClientExtensions::livingBladeItem),
			Map.entry(LivingFlailItem.class, ItemClientExtensions::livingFlailItem),
			Map.entry(LivingPistolItem.class, ItemClientExtensions::livingPistolItem),
			Map.entry(LivingSickleItem.class, ItemClientExtensions::livingSickleItem),
			Map.entry(LivingSpearItem.class, ItemClientExtensions::livingSpearItem),
			Map.entry(LivingTorchItem.class, ItemClientExtensions::livingTorchItem),
			Map.entry(SanguisLanceaItem.class, ItemClientExtensions::sanguisLanceaItem),
			Map.entry(MarionetteCrossbarItem.class, ItemClientExtensions::marionetteCrossbarItem),
			Map.entry(SporiticThuribleItem.class, ItemClientExtensions::sporiticThuribleItem),
			Map.entry(BarbedArmorItem.class, ItemClientExtensions::barbedArmorItem),
			Map.entry(BarbedShieldItem.class, ItemClientExtensions::barbedShieldItem),
			Map.entry(ChitiniteArmorItem.class, ItemClientExtensions::chitiniteArmorItem),
			Map.entry(ChitiniteShieldItem.class, ItemClientExtensions::chitiniteShieldItem),
			Map.entry(CovenantMantleArmorItem.class, ItemClientExtensions::covenantMantleArmorItem),
			Map.entry(HemolymphopodaHeadpieceArmorItem.class, ItemClientExtensions::hemolymphopodaHeadpieceArmorItem),
			Map.entry(LanternTickHelmetArmorItem.class, ItemClientExtensions::lanternTickHelmetArmorItem),
			Map.entry(PrismaticArmorItem.class, ItemClientExtensions::prismaticArmorItem),
			Map.entry(VenousStriderSabatonsItem.class, ItemClientExtensions::venousStriderSabatonsItem),
			Map.entry(UnstainedArmorItem.class, ItemClientExtensions::unstainedArmorItem),
			Map.entry(VestmentOfTheFinalMoltArmorItem.class, ItemClientExtensions::vestmentOfTheFinalMoltArmorItem),
			Map.entry(AbsolutionDaggerItem.class, ItemClientExtensions::absolutionDaggerItem),
			Map.entry(AnnettasSanguisLanceaItem.class, ItemClientExtensions::annettasSanguisLanceaItem),
			Map.entry(SilthmereGlaiveItem.class, ItemClientExtensions::silthmereGlaiveItem),
			Map.entry(UnstainedWarhammerItem.class, ItemClientExtensions::unstainedWarhammerItem));
	static final Map<String, Function<Item, IClientItemExtensions>> BY_ITEM_ID = Map.ofEntries(
			Map.entry("hematic_armature", ItemClientExtensions::hematicArmatureBlockItem),
			Map.entry("puppeteers_spindle", ItemClientExtensions::puppeteersSpindleBlockItem),
			Map.entry("vial_centrifuge", ItemClientExtensions::vialCentrifugeBlockItem),
			Map.entry("covenant_throne", ItemClientExtensions::covenantThroneBlockItem),
			Map.entry("earthen_vein", ItemClientExtensions::earthenVeinBlockItem),
			Map.entry("fungal_implantation_pylon", ItemClientExtensions::fungalImplantationPylonBlockItem),
			Map.entry("hematic_stake", ItemClientExtensions::hematicStakeBlockItem),
			Map.entry("mason_effigy", ItemClientExtensions::masonsEffigyBlockItem),
			Map.entry("mortal_display", ItemClientExtensions::mortalDisplayBlockItem),
			Map.entry("sanguine_monolith", ItemClientExtensions::sanguineMonolithBlockItem),
			Map.entry("visceral_mirror", ItemClientExtensions::visceralMirrorBlockItem),
			Map.entry("suspended_blood_crystal", ItemClientExtensions::suspendedBloodCrystalBlockItem),
			Map.entry("suspended_vivianite", ItemClientExtensions::suspendedVivianiteBlockItem),
			Map.entry("suspended_cleansed_blood_crystal", ItemClientExtensions::suspendedCleansedBloodCrystalBlockItem));

	private ItemClientExtensions() {
	}

	/** The item's extensions: by item id, else by the nearest class (or superclass) with a factory; null for none. */
	public static IClientItemExtensions create(Item item) {
		var byId = BY_ITEM_ID.get(BuiltInRegistries.ITEM.getKey(item).getPath());
		if (byId != null) return byId.apply(item);
		for (Class<?> type = item.getClass(); type != null && type != Item.class; type = type.getSuperclass()) {
			var byClass = BY_CLASS.get(type);
			if (byClass != null) return byClass.apply(item);
		}
		return null;
	}

	static IClientItemExtensions ambergrisCylinderItem(Item item) {return com.vincenthuto.hemomancy.client.render.item.harbinger.AmbergrisCylinderRenderer.EXTENSIONS;}

	static IClientItemExtensions abstractSpecialBloodLustArmorItem(Item item) {
		return SpecialBloodLustClientExtensions.create(((AbstractSpecialBloodLustArmorItem) item).texturePrefix());
	}

	static IClientItemExtensions bloodLustArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				BloodLustArmorModel<?> model;
				if (itemStack.getItem() == ItemInit.blood_lust_helm.get()) {
					model = BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_grinning.get()) {
					model = BloodLustArmorModel.grinning.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_tengu.get()) {
					model = BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_lodestone.get()) {
					model = BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_velorum.get()) {
					model = BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_chest.get()) {
					model = BloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_legs.get()) {
					model = BloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_boots.get()) {
					model = BloodLustArmorModel.boots.get();
				} else {
					return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
				}
				model.setLineage(BloodLustArmorItem.getLineage(itemStack));
				return model;
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions marrowCrownArmorItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.marrow_crown.get()) {
					return MarrowCrownModel.helmet.get();
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MarrowCrownItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions silentArchonArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SilentArchonArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.silent_archon_helm.get()) {
					return SilentArchonArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.silent_archon_chestplate.get()) {
					return SilentArchonArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.silent_archon_leggings.get()) {
					return SilentArchonArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.silent_archon_boots.get()) {
					return SilentArchonArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions heartyCompassItem(Item item) {
	    return RenderPropHeartyCompass.INSTANCE;
	}

	static IClientItemExtensions memoryOfVesperItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MemoryOfVesperItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions monolithFragmentItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MonolithFragmentItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions monolithicCornerstoneItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MonolithicCornerstoneItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions monolithImbuedClothItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MonolithImbuedClothItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions qliphothPomeItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new QliphothPomeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions qliphothSeedItem(Item item) {
	    return new IClientItemExtensions() {
	        @Override
	        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
	            return new QliphothSeedItemRenderer(
	                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
	                    Minecraft.getInstance().getEntityModels());
	        }
	    };
	}

	static IClientItemExtensions itemScarPattern(Item item) {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer renderer = new ScarPatternItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions hematicArmatureBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new HematicArmatureItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions puppeteersSpindleBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new PuppeteersSpindleItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions vialCentrifugeBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new VialCentrifugeItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions covenantThroneBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new CovenantThroneItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions earthenVeinBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new EarthenVeinItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions fungalImplantationPylonBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new FungalImplantationPylonItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions hematicStakeBlockItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new HematicStakeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions masonsEffigyBlockItem(Item item) {
	    return new IClientItemExtensions() {
	        private final BlockEntityWithoutLevelRenderer renderer = new MasonsEffigyItemRenderer(null, null);

	        @Override
	        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
	            return renderer;
	        }
	    };
	}

	static IClientItemExtensions mortalDisplayBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new MortalDisplayItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions sanguineMonolithBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SanguineMonolithItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions specimenJarBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SpecimenJarItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions visceralMirrorBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new VisceralMirrorItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions suspendedBloodCrystalBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SuspendedBloodCrystalItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions suspendedVivianiteBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SuspendedVivianiteItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions bloodGourdItem(Item item) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new BloodGourdItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	static IClientItemExtensions bloodAbsorptionItem(Item item) {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer myRenderer = new CellHandItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return myRenderer;
			}
		};
	}

	static IClientItemExtensions bloodProjectionItem(Item item) {
		return new IClientItemExtensions() {
			final BlockEntityWithoutLevelRenderer myRenderer = new CellHandItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return myRenderer;
			}
		};
	}

	static IClientItemExtensions livingAxeItem(Item item) {
		return RenderPropLivingAxe.INSTANCE;
	}

	static IClientItemExtensions livingBaghnakhItem(Item item) {
		return LivingBaghnakhItemClientExtensions.INSTANCE;
	}

	static IClientItemExtensions livingBladeItem(Item item) {
		return RenderPropLivingBlade.INSTANCE;
	}

	static IClientItemExtensions livingFlailItem(Item item) {
		return RenderPropLivingFlail.INSTANCE;
	}

	static IClientItemExtensions livingPistolItem(Item item) {
		return RenderPropPistol.INSTANCE;
	}

	static IClientItemExtensions livingSickleItem(Item item) {
		return LivingSickleItemClientExtensions.INSTANCE;
	}

	static IClientItemExtensions livingSpearItem(Item item) {
		return RenderPropLivingSpear.INSTANCE;
	}

	static IClientItemExtensions livingTorchItem(Item item) {
		return RenderPropLivingTorch.INSTANCE;
	}

	static IClientItemExtensions sanguisLanceaItem(Item item) {
		return RenderPropSanguisLancea.INSTANCE;
	}

	static IClientItemExtensions marionetteCrossbarItem(Item item) {
		return MarionetteCrossbarItemRenderer.EXTENSIONS;
	}

	static IClientItemExtensions sporiticThuribleItem(Item item) {
		return RenderPropSporiticThurible.INSTANCE;
	}

	static IClientItemExtensions barbedArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,

					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.barbed_helm.get()) {
					return BarbedArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.barbed_chestplate.get()) {
					return BarbedArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.barbed_leggings.get()) {
					return BarbedArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.barbed_boots.get()) {
					return BarbedArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions barbedShieldItem(Item item) {
		return RenderPropBarbedShield.INSTANCE;
	}

	static IClientItemExtensions chitiniteArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.chitinite_helm.get()) {
					return ChitiniteArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_chestplate.get()) {
					return ChitiniteArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_leggings.get()) {
					return ChitiniteArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_boots.get()) {
					return ChitiniteArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions chitiniteShieldItem(Item item) {
		return RenderPropChitiniteShield.INSTANCE;
	}

	static IClientItemExtensions covenantMantleArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.covenant_mantle.get()) {
					return CovenantLeaderArmorModel.chest.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions hemolymphopodaHeadpieceArmorItem(Item item) {
		return new IClientItemExtensions() {
			private BlockEntityWithoutLevelRenderer renderer;

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				// Return an empty model to suppress vanilla helmet rendering.
				// HemolymphopodaHeadpieceLayer handles the actual crab rendering.
				return HemolymphopodaHeadArmorModel.HELMET.get();
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (renderer == null) {
					net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
					renderer = new HemolymphopodaHeadpieceItemRenderer(
							minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
				}
				return renderer;
			}
		};
	}

	static IClientItemExtensions lanternTickHelmetArmorItem(Item item) {
		return new IClientItemExtensions() {
			private BlockEntityWithoutLevelRenderer renderer;

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				return LanternTickHeadArmorModel.HELMET.get();
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (renderer == null) {
					net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
					renderer = new LanternTickHelmetItemRenderer(
							minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
				}
				return renderer;
			}
		};
	}

	static IClientItemExtensions prismaticArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.prismatic_helm.get()) {
					return PrismaticArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.prismatic_chestplate.get()) {
					return PrismaticArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.prismatic_leggings.get()) {
					return PrismaticArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.prismatic_boots.get()) {
					return PrismaticArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions venousStriderSabatonsItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.venous_strider_sabatons.get()) {
					return ChalybeateFortressArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions unstainedArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,

					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.unstained_helm.get()) {
					return UnstainedArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.unstained_chestplate.get()) {
					return UnstainedArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.unstained_leggings.get()) {
					return UnstainedArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.unstained_boots.get()) {
					return UnstainedArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot,
					Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					float netHeadYaw, float headPitch) {
				if (model instanceof UnstainedArmorModel<?> unstained) {
					unstained.animateRobes(limbSwingAmount, ageInTicks);
				}
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions vestmentOfTheFinalMoltArmorItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.vestment_of_the_final_molt.get()) {
					VestmentOfTheFinalMoltArmorModel<LivingEntity> model = VestmentOfTheFinalMoltArmorModel.chest.get();
					model.setHoodRaised(entityLiving.getItemBySlot(EquipmentSlot.HEAD).isEmpty());
					return model;
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot,
					Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					float netHeadYaw, float headPitch) {
				if (model instanceof VestmentOfTheFinalMoltArmorModel<?> vestment) {
					vestment.animateCloak(limbSwing, limbSwingAmount, ageInTicks);
				}
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions suspendedCleansedBloodCrystalBlockItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SuspendedCleansedBloodCrystalItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions absolutionDaggerItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new AbsolutionDaggerItemRenderer(null, null);

			@Override
			public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
					ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
				UnstainedWeaponPlayerPose.firstPersonDagger(poses, swingProgress, equipProgress, arm == HumanoidArm.RIGHT);
				return true;
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions annettasSanguisLanceaItem(Item item) {
		return RenderPropAnnettasSanguisLancea.INSTANCE;
	}

	static IClientItemExtensions silthmereGlaiveItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SilthmereGlaiveItemRenderer(null, null);

			@Override
			public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
					ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
				UnstainedWeaponPlayerPose.firstPersonGlaive(poses, swingProgress, equipProgress, arm == HumanoidArm.RIGHT);
				return true;
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static IClientItemExtensions unstainedWarhammerItem(Item item) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new UnstainedWarhammerItemRenderer(null, null);

			@Override
			public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
					ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
				UnstainedWeaponPlayerPose.firstPersonHammer(poses, swingProgress, equipProgress, arm == HumanoidArm.RIGHT);
				return true;
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	static final class RenderPropHeartyCompass implements IClientItemExtensions {
	    static final RenderPropHeartyCompass INSTANCE = new RenderPropHeartyCompass();

	    @Override
	    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
	        return new HeartyCompassItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
	                Minecraft.getInstance().getEntityModels());
	    }
	}

	static final class RenderPropLivingAxe implements IClientItemExtensions {

		public static RenderPropLivingAxe INSTANCE = new RenderPropLivingAxe();

		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}



		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingAxeItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	/** The claws keep their baked model; only the rake motion replaces vanilla's swing. */
	private static final class LivingBaghnakhItemClientExtensions implements IClientItemExtensions {
		private static final LivingBaghnakhItemClientExtensions INSTANCE = new LivingBaghnakhItemClientExtensions();

		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}
	}

	static final class RenderPropLivingBlade implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}


		public static RenderPropLivingBlade INSTANCE = new RenderPropLivingBlade();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingBladeItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropLivingFlail implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}

		public static final RenderPropLivingFlail INSTANCE = new RenderPropLivingFlail();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingFlailItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropPistol implements IClientItemExtensions {

		public static RenderPropPistol INSTANCE = new RenderPropPistol();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingPistolItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	private static final class LivingSickleItemClientExtensions implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}

		private static final LivingSickleItemClientExtensions INSTANCE = new LivingSickleItemClientExtensions();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingSickleItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropLivingSpear implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}


		public static RenderPropLivingSpear INSTANCE = new RenderPropLivingSpear();


		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingSpearItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropLivingTorch implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}

		public static final RenderPropLivingTorch INSTANCE = new RenderPropLivingTorch();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new LivingTorchItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropSanguisLancea implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}


		public static RenderPropSanguisLancea INSTANCE = new RenderPropSanguisLancea();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new SanguisLanceaItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropSporiticThurible implements IClientItemExtensions {
		public static final RenderPropSporiticThurible INSTANCE = new RenderPropSporiticThurible();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new SporiticThuribleItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	static final class RenderPropBarbedShield implements IClientItemExtensions {

		public static RenderPropBarbedShield INSTANCE = new RenderPropBarbedShield();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new BarbedShieldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	static final class RenderPropChitiniteShield implements IClientItemExtensions {

		public static RenderPropChitiniteShield INSTANCE = new RenderPropChitiniteShield();


		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new ChitiniteShieldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}

	static final class RenderPropAnnettasSanguisLancea implements IClientItemExtensions {
		@Override
		public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
				ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
			return LivingWeaponPlayerPose.applyFirstPerson(poses, player, arm, stack, equipProgress, swingProgress);
		}


		public static RenderPropAnnettasSanguisLancea INSTANCE = new RenderPropAnnettasSanguisLancea();

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new AnnettasSanguisLanceaItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}
}
