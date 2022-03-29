package com.vincenthuto.hemomancy.entity.brain;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SensorInit {
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES,
			Hemomancy.MOD_ID);

	public static final DeferredRegister<DataSerializerEntry> DATA_SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.Keys.DATA_SERIALIZERS, Hemomancy.MOD_ID);

	public static final RegistryObject<SensorType<SecondaryBrainPoiSensor>> secondary_brain_poi = SENSORS
			.register("secondary_brain_poi", () -> new SensorType<>(SecondaryBrainPoiSensor::new));

	public static final RegistryObject<DataSerializerEntry> brain_data = DATA_SERIALIZERS.register("brain_data",
			() -> new DataSerializerEntry(new EntityDataSerializer<EntityBrainData>() {
				public void write(FriendlyByteBuf p_135335_, EntityBrainData p_135336_) {
					p_135335_.writeVarInt(Registry.VILLAGER_PROFESSION.getId(p_135336_.getProfession()));
				}

				public EntityBrainData read(FriendlyByteBuf p_135341_) {
					return new EntityBrainData(Registry.VILLAGER_PROFESSION.byId(p_135341_.readVarInt()));
				}

				public EntityBrainData copy(EntityBrainData p_135329_) {
					return p_135329_;
				}
			}));

}
