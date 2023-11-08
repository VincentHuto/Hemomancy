package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodBoltRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodBulletRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodCloudCarrierRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodCloudRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodNeedleRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodOrbDirectedRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodOrbTrackingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.BloodShotRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.WretchedWillRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.AbhorentThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.BarbedUrchinRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.BloodDrunkPuppeteerRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChitiniteRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChthonianQueenRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ChthonianRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.EnthralledDollRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ErythromyceliumEruptusRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.FargoneRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.FunglingRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.LeechRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.LumpOfThoughtRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.MorphlingPolypRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.ThirsterRenderer;
import com.vincenthuto.hemomancy.client.render.entity.projectile.TrackingPestsRenderer;
import com.vincenthuto.hemomancy.client.render.entity.projectile.TrackingSerpentRenderer;
import com.vincenthuto.hemomancy.client.render.item.MorphlingPolypItemRenderer;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class EntityEvents {


	@SubscribeEvent
	public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EntityInit.directed_blood_orb.get(), BloodOrbDirectedRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_cloud_carrier.get(), BloodCloudCarrierRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_cloud.get(), BloodCloudRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_blood_orb.get(), BloodOrbTrackingRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_snake.get(), TrackingSerpentRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_pests.get(), TrackingPestsRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_bolt.get(), BloodBoltRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_needle.get(), BloodNeedleRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_shot.get(), BloodShotRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_bullet.get(), BloodBulletRenderer::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), MorphlingPolypItemRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
		event.registerEntityRenderer(EntityInit.wretched_will.get(), WretchedWillRenderer::new);
		event.registerEntityRenderer(EntityInit.leech.get(), LeechRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
		event.registerEntityRenderer(EntityInit.fargone.get(), FargoneRenderer::new);
		event.registerEntityRenderer(EntityInit.thirster.get(), ThirsterRenderer::new);
		event.registerEntityRenderer(EntityInit.fungling.get(), FunglingRenderer::new);
		event.registerEntityRenderer(EntityInit.chitinite.get(), ChitiniteRenderer::new);
		event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
		event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
		event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
		event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
		event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
		event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
		event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
		event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);

	}

}
