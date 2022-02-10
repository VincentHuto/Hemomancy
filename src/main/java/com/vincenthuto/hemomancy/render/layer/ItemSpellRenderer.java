///*
// * Decompiled with CFR 0.151.
// * 
// * Could not load the following classes:
// *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
// *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
// *  com.mojang.blaze3d.platform.Lighting
// *  com.mojang.blaze3d.systems.RenderSystem
// *  com.mojang.blaze3d.vertex.PoseStack
// *  com.mojang.math.Vector3f
// *  net.minecraft.client.Minecraft
// *  net.minecraft.client.model.geom.EntityModelSet
// *  net.minecraft.client.player.AbstractClientPlayer
// *  net.minecraft.client.player.LocalPlayer
// *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
// *  net.minecraft.client.renderer.MultiBufferSource
// *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
// *  net.minecraft.client.renderer.block.model.ItemTransforms$TransformType
// *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
// *  net.minecraft.client.renderer.entity.EntityRenderer
// *  net.minecraft.client.renderer.entity.ItemRenderer
// *  net.minecraft.client.renderer.entity.player.PlayerRenderer
// *  net.minecraft.client.renderer.texture.OverlayTexture
// *  net.minecraft.client.resources.model.BakedModel
// *  net.minecraft.core.particles.ParticleOptions
// *  net.minecraft.core.particles.ParticleType
// *  net.minecraft.resources.ResourceLocation
// *  net.minecraft.world.InteractionHand
// *  net.minecraft.world.entity.Entity
// *  net.minecraft.world.entity.HumanoidArm
// *  net.minecraft.world.entity.player.Player
// *  net.minecraft.world.item.ItemStack
// *  net.minecraft.world.phys.Vec3
// *  net.minecraftforge.client.ForgeHooksClient
// */
//package com.vincenthuto.hemomancy.render.layer;
//
//import java.util.HashMap;
//
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.platform.Lighting;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Vector3f;
//import com.vincenthuto.hemomancy.init.ParticleInit;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.model.geom.EntityModelSet;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.block.model.ItemTransforms;
//import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
//import net.minecraft.client.renderer.entity.EntityRenderer;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.client.renderer.entity.player.PlayerRenderer;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.client.resources.model.BakedModel;
//import net.minecraft.core.particles.ParticleOptions;
//import net.minecraft.core.particles.ParticleType;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.HumanoidArm;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.client.ForgeHooksClient;
//
//public class ItemSpellRenderer extends BlockEntityWithoutLevelRenderer {
//	private static HashMap<Integer, BakedModel> bakedSpellModels;
//	private BakedModel defaultSpellModel;
//	private final ResourceLocation location = new ResourceLocation("mna", "item/spell_texture");
//
//	public ItemSpellRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
//		super(berd, ems);
//		if (bakedSpellModels == null) {
//			bakedSpellModels = new HashMap();
//		}
//	}
//
//	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack,
//			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
//		if (transformType != ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND
//				&& transformType != ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
//			if (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
//				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT);
//				this.spawnFirstPersonParticlesForStack(stack, HumanoidArm.RIGHT);
//			} else if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
//				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT);
//				this.spawnFirstPersonParticlesForStack(stack, HumanoidArm.LEFT);
//			} else {
//				this.renderDefaultSpellItem(stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
//			}
//		}
//	}
//
//	private void renderDefaultSpellItem(ItemStack stack, ItemTransforms.TransformType transformType,
//			PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
//		if (!stack.isEmpty()) {
//			BakedModel spellModel;
//			BakedModel bakedModel = spellModel = transformType == ItemTransforms.TransformType.GUI
//					? this.getSpellModel(stack)
//					: this.getDefaultSpellModel();
//			if (spellModel.isLayered()) {
//				ForgeHooksClient.drawItemLayered((ItemRenderer) Minecraft.getInstance().getItemRenderer(),
//						(BakedModel) this.defaultSpellModel, (ItemStack) stack, (PoseStack) matrixStack,
//						(MultiBufferSource) buffer, (int) combinedLight, (int) combinedOverlay, (boolean) true);
//			} else {
//				matrixStack.popPose();
//				matrixStack.pushPose();
//				if (transformType != ItemTransforms.TransformType.GUI) {
//					Minecraft.getInstance().getItemRenderer().render(stack, transformType,
//							transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
//							combinedLight, combinedOverlay, spellModel);
//				} else {
//					boolean flag;
//					RenderSystem.enableBlend();
//					RenderSystem.blendFunc((GlStateManager.SourceFactor) GlStateManager.SourceFactor.SRC_ALPHA,
//							(GlStateManager.DestFactor) GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//					MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().m_91269_().m_110104_();
//					boolean bl = flag = !spellModel.usesBlockLight();
//					if (flag) {
//						Lighting.setupForFlatItems();
//					}
//					Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.GUI, false,
//							matrixStack, (MultiBufferSource) bufferSource, 0xF000F0, OverlayTexture.f_118083_,
//							spellModel);
//					bufferSource.endBatch();
//					RenderSystem.enableDepthTest();
//					if (flag) {
//						Lighting.setupFor3DItems();
//					}
//				}
//			}
//		}
//	}
//
//	private BakedModel getSpellModel(ItemStack stack) {
//		BakedModel customModel;
//		int customIconIndex = ItemSpell.getCustomIcon(stack);
//		if (customIconIndex < 0 || customIconIndex >= SpellIconList.ALL.length) {
//			return this.getDefaultSpellModel();
//		}
//		if (!bakedSpellModels.containsKey(customIconIndex)) {
//			bakedSpellModels.put(customIconIndex,
//					Minecraft.getInstance().getModelManager().getModel(SpellIconList.ALL[customIconIndex]));
//		}
//		return (customModel = bakedSpellModels.get(customIconIndex)) != null ? customModel
//				: this.getDefaultSpellModel();
//	}
//
//	private BakedModel getDefaultSpellModel() {
//		if (this.defaultSpellModel == null) {
//			this.defaultSpellModel = Minecraft.getInstance().getModelManager().getModel(this.location);
//		}
//		return this.defaultSpellModel;
//	}
//
//	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side) {
//		Minecraft mc = Minecraft.getInstance();
//		EntityRenderer playerrenderer = mc.getEntityRenderDispatcher().getRenderer((Entity) mc.player);
//		if (!(playerrenderer instanceof PlayerRenderer)) {
//			return;
//		}
//		RenderSystem.setShaderTexture((int) 0, (ResourceLocation) mc.player.getSkinTextureLocation());
//		matrixStackIn.pushPose();
//		if (side == HumanoidArm.RIGHT) {
//			matrixStackIn.translate(0.75, -0.25, 0.0);
//			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(95.0f));
//			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-30.0f));
//			((PlayerRenderer) playerrenderer).renderRightHand(matrixStackIn, bufferIn, combinedLightIn,
//					(AbstractClientPlayer) mc.player);
//		} else {
//			matrixStackIn.translate(0.25, -0.25, 0.0);
//			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(260.0f));
//			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(30.0f));
//			((PlayerRenderer) playerrenderer).renderLeftHand(matrixStackIn, bufferIn, combinedLightIn,
//					(AbstractClientPlayer) mc.player);
//		}
//		matrixStackIn.popPose();
//	}
//
//	private void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand) {
//		if (Minecraft.getInstance().isPaused()) {
//			return;
//		}
//		if (!(stack.getItem() instanceof ItemSpell)) {
//			return;
//		}
//		Minecraft mc = Minecraft.getInstance();
//		LocalPlayer player = mc.player;
//		boolean playerIsRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
//		boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
//		InteractionHand activeHand = player.getUsedItemHand();
//		if (!itemIsInUse || playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND && hand == HumanoidArm.LEFT
//				|| !playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND && hand == HumanoidArm.RIGHT) {
//			Vec3 particlePos = player.position().add(0.0, (double) (player.getEyeHeight() - 0.2f), 0.0);
//			Vec3 look = player.getLookAngle().normalize().scale(0.5);
//			Vec3 perp = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize()
//					.scale(hand == HumanoidArm.LEFT ? (double) -0.4f : (double) 0.4f);
//			particlePos = particlePos.add(look).add(perp);
//			SpellRecipe recipe = SpellRecipe
//					.fromNBT(((ItemSpell) stack.getItem()).getSpellCompound(stack, (Player) player));
//			Affinity[] affs = recipe.getAffinity().keySet().toArray(new Affinity[0]);
//			switch (affs[(int) (Math.random() * (double) affs.length)]) {
//			case ARCANE: {
//				Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
//				Vec3 offset = new Vec3(player.level.random.nextGaussian(), player.level.random.nextGaussian(),
//						player.level.random.nextGaussian()).normalize().scale((double) 0.3f);
//				origin = origin.add(offset);
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.ARCANE_LERP.get())),
//						origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
//				break;
//			}
//			case EARTH: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.DUST.get())),
//						particlePos.x, particlePos.y, particlePos.z, (double) -0.005f + Math.random() * (double) 0.01f,
//						(double) 0.03f, (double) -0.005f + Math.random() * (double) 0.01f);
//				break;
//			}
//			case ENDER: {
//				Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
//				Vec3 offset = new Vec3(player.level.random.nextGaussian(), player.level.random.nextGaussian(),
//						player.level.random.nextGaussian()).normalize().scale((double) 0.3f);
//				origin = origin.add(offset);
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.ENDER.get())),
//						origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
//				break;
//			}
//			case FIRE: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.FLAME.get())),
//						particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//				break;
//			}
//			case HELLFIRE: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.HELLFIRE.get())),
//						particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//				break;
//			}
//			case LIGHTNING: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.LIGHTNING_BOLT.get())),
//						particlePos.x, particlePos.y, particlePos.z,
//						particlePos.x - (double) 0.2f + Math.random() * (double) 0.4f,
//						particlePos.y - (double) 0.2f + Math.random() * (double) 0.4f,
//						particlePos.z - (double) 0.2f + Math.random() * (double) 0.4f);
//				break;
//			}
//			case WATER: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.WATER.get())),
//						particlePos.x, particlePos.y, particlePos.z, -0.05 * Math.random() * 0.1, Math.random() * 0.05,
//						-0.05 * Math.random() * 0.1);
//				break;
//			}
//			case ICE: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.FROST.get())),
//						particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//				break;
//			}
//			case WIND: {
//				player.level.addParticle(
//						(ParticleOptions) new MAParticleType(
//								(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.AIR_ORBIT.get())),
//						particlePos.x, particlePos.y, particlePos.z, (double) 0.3f, (double) 0.01f, (double) 0.05f);
//				break;
//			}
//			}
//		}
//	}
//}
