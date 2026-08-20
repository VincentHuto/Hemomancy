package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.BloodwoodGrowthHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryWorldEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.effect.ChummedWatersAreaManager;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleResonanceState;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TransientServerStateEvents {
	private TransientServerStateEvents() {
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		clearTransientState();
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		clearTransientState();
	}

	private static void clearTransientState() {
		BloodManipulation.clearSessionState();
		StillArt.clearSessionState();
		SporiticThuribleResonanceState.clearSessionState();
		BloodwoodGrowthHandler.clearSessionState();
		EquippedMorphlingEvents.clearSessionState();
		ChummedWatersAreaManager.clearSessionState();
		BlackVeilCovenantManager.clearSessionState();
		TemporaryIceManager.clearSessionState();
		SanguineFormationProjectionHandler.clear();
		BloodStructureFeedManager.clear();
		HematicSalvageEvents.clearSessionState();
		RootedStateHelper.clearSessionState();
		ConserveStateHelper.clearSessionState();
		MuscleMemoryEvents.clearSessionState();
		MuscleMemoryWorldEvents.clearSessionState();
	}
}
