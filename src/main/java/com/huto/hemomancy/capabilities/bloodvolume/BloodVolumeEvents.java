package com.huto.hemomancy.capabilities.bloodvolume;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.utils.Vector3;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.item.tool.ItemBloodGourd;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.GlowParticleData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodVolumeEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesItemStack(final AttachCapabilitiesEvent<ItemStack> event) {
		if (((ItemStack) event.getObject()).getItem() instanceof ItemBloodGourd) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		float amount = BloodVolumeProvider.getPlayerbloodVolumeS(player);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodVolumePacketServer(amount));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Blood Volume: " + TextFormatting.GOLD + amount + "ml"),
				false);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		float amount = BloodVolumeProvider.getPlayerbloodVolumeS(player);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodVolumePacketServer(amount));
		player.sendStatusMessage(
				new StringTextComponent("Welcome! Current Blood Volume: " + TextFormatting.GOLD + amount + "ml"),
				false);
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		IBloodVolume bloodVolumeOld = event.getOriginal().getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		IBloodVolume bloodVolumeNew = event.getEntity().getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		bloodVolumeNew.setBloodVolume(bloodVolumeOld.getBloodVolume() - 20f);
		((PlayerEntity) event.getEntity()).sendStatusMessage(
				new StringTextComponent(TextFormatting.ITALIC + "Upon death, your blood volume has decreased to: "
						+ TextFormatting.RED + TextFormatting.ITALIC + bloodVolumeNew.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void regainBloodVolume(PlayerTickEvent e) {
		/*
		 * Vector3 centerVec = Vector3.fromEntityCenter(e.player); if
		 * (e.player.getActivePotionEffect(PotionInit.blood_binding.get()) != null) {}
		 * 
		 * if (!e.player.world.isRemote) { ServerWorld sWorld = (ServerWorld)
		 * e.player.world; sWorld.spawnParticle(GlowParticleData.createData(new
		 * ParticleColor(255, 0, 0)), centerVec.x + Math.sin(e.player.ticksExisted) +
		 * ParticleUtil.inRange(-0.1, 0.1), centerVec.y + ParticleUtil.inRange(-0.1,
		 * 0.1), centerVec.z + Math.cos(e.player.ticksExisted) +
		 * ParticleUtil.inRange(-0.1, 0.1), 1, 0f, 0.2f, 0f, 0); }else {
		 * e.player.world.addParticle(GlowParticleData.createData(new ParticleColor(255,
		 * 0, 0)), centerVec.x + Math.sin(e.player.ticksExisted) +
		 * ParticleUtil.inRange(-0.1, 0.1), centerVec.y + ParticleUtil.inRange(-0.1,
		 * 0.1), centerVec.z + Math.cos(e.player.ticksExisted) +
		 * ParticleUtil.inRange(-0.1, 0.1), 0, 0, 0); }
		 * 
		 * IBloodVolume bloodVolume =
		 * e.player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
		 * .orElseThrow(NullPointerException::new); if (bloodVolume.getBloodVolume() <
		 * 5000) { bloodVolume.addBloodVolume(0.5f); }
		 */
	}

	@SubscribeEvent
	public static void bloodBindingEffect(LivingEvent e) {
		if (e.getEntityLiving() != null) {
			Vector3 centerVec = Vector3.fromEntityCenter(e.getEntityLiving());
			if (e.getEntityLiving().getActivePotionEffect(PotionInit.blood_binding.get()) != null) {
				e.getEntityLiving().setMotion(0, 0, 0);
				if (e.getEntityLiving().world.isRemote) {

					/*
					 * e.getEntityLiving().getEntityWorld().addParticle(ParticleTypes.ASH),
					 * centerVec.x + Math.sin(e.getEntityLiving().ticksExisted) +
					 * ParticleUtil.inRange(-0.1, 0.1), centerVec.y + ParticleUtil.inRange(-0.1,
					 * 0.1), centerVec.z + Math.cos(e.getEntityLiving().ticksExisted) +
					 * ParticleUtil.inRange(-0.1, 0.1), 0, 0.005, 0);
					 */
				} else if (!e.getEntityLiving().world.isRemote) {

					ServerWorld sWorld = (ServerWorld) e.getEntityLiving().world;
					sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(e.getEntityLiving().ticksExisted*0.1), centerVec.y,
							centerVec.z + Math.cos(e.getEntityLiving().ticksExisted*0.1), 6, 0f, 0.0f, 0f, 0);
					sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(255, 0, 0)),
							centerVec.x - Math.sin(e.getEntityLiving().ticksExisted*0.1), centerVec.y,
							centerVec.z - Math.cos(e.getEntityLiving().ticksExisted*0.1), 6, 0f, 0.0f, 0f, 0);
					sWorld.spawnParticle(RedstoneParticleData.REDSTONE_DUST,
							centerVec.x + Math.sin(e.getEntityLiving().ticksExisted * 0.1), centerVec.y,
							centerVec.z + Math.cos(e.getEntityLiving().ticksExisted * 0.1), 3, 0f, 0.0f, 0f, 0);
					sWorld.spawnParticle(RedstoneParticleData.REDSTONE_DUST,
							centerVec.x - Math.sin(e.getEntityLiving().ticksExisted * 0.1), centerVec.y,
							centerVec.z - Math.cos(e.getEntityLiving().ticksExisted * 0.1), 3, 0f, 0.0f, 0f, 0);

					/*
					 * sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(255, 0,
					 * 0)), centerVec.x + Math.sin(e.getEntityLiving().ticksExisted), centerVec.y,
					 * centerVec.z + Math.cos(e.getEntityLiving().ticksExisted), 6, 0f, 0.0f, 0f,
					 * 0);
					 */
				}
			}
		}
	}

}
