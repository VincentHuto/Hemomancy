package com.vincenthuto.hemomancy.compat.curios;

import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

public class CuriosPlugin {

public static void initCuriosSlots(InterModEnqueueEvent event) {
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.HEAD);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.BRACELET);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.BELT);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.NECKLACE);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.CHARM);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.CURIO);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.BACK);
CuriosPlugin.IMCRegisterCurioSlot(SlotTypePreset.RING, 2);
}

public static void clientCurioSetup(FMLClientSetupEvent evt) {
// TODO(MnA-compat): register Harbinger Grimoire renderer in Curios.
// Requires both MnA (WandRenderer) and Curios to be present.
// Restore when a NeoForge 1.21.1 MnA build is available:
//   CuriosRendererRegistry.register(MnAPluginItemInit.harbinger_grimore.get(),
//       WandRenderer::new);
}

private static void IMCRegisterCurioSlot(SlotTypePreset slot) {
InterModComms.sendTo((String) "curios", (String) "register_type",
() -> new SlotTypeMessage.Builder(slot.getIdentifier()).build());
}

public static void IMCRegisterCurioSlot(SlotTypePreset slot, int quantity) {
InterModComms.sendTo((String) "curios", (String) "register_type",
() -> new SlotTypeMessage.Builder(slot.getIdentifier()).size(quantity).build());
}

}
