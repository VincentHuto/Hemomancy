package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/** The body-anchored cast visuals stay hooked ahead of every shared tier and are sent from their real server moments. */
class CastBasicsVisualSourceTest {
    private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");

    @Test void rendererDrawsBodyAnchoredCastsFirstAndAdmitsTheirIron() throws IOException {
        String renderer = read("client/render/world/ManipulationVisualRenderer.java");
        int hook = renderer.indexOf("if(bodyAnchored(packet.form())) {");
        int ferric = renderer.indexOf("if(FerricDuctilisGeometry.handles(packet.form())) {");
        int shared = renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form()))");
        assertTrue(hook > 0 && hook < ferric && hook < shared, "body-anchored casts must precede the shared tiers");
        String solidsGate = renderer.substring(renderer.indexOf("if(solids && !FerricDuctilisGeometry.handles(packet.form())"));
        solidsGate = solidsGate.substring(0, solidsGate.indexOf("continue;"));
        assertTrue(solidsGate.contains("!WeaponConjureGeometry.handles(packet.form())")
                && solidsGate.contains("!SanguineMendingGeometry.handles(packet.form())"), "conjured and riveted iron needs the solid pass");
        assertTrue(renderer.contains("|| bodyAnchored(packet.form())\n"), "body-anchored casts need the body pose");
        assertTrue(read("client/render/world/ManipulationMotes.java").contains("ManipulationVisualRenderer.bodyAnchored(form)"),
                "body-anchored casts must not rain the default blood motes");
    }

    @Test void conjureCuesComeFromTheMorphNotTheUnreachedManipulationAction() throws IOException {
        String helper = read("common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java");
        assertTrue(helper.contains("ManipulationVisuals.Form.WEAPON_CONJURE"));
        assertTrue(helper.indexOf("new PacketLivingStaffMorph(") < helper.indexOf("Form.WEAPON_CONJURE"));
        String manip = read("common/manipulation/ferric/StaffWeaponFormManip.java");
        assertFalse(manip.contains("_CONJURE"), "the staff-form action is not reached from the key paths; the morph sends the cue");
        assertTrue(read("common/manipulation/ferric/ConjurationManip.java").contains("WeaponConjureRules.STAFF"));
    }

    @Test void cellHandsAndAvatarSendTheirCues() throws IOException {
        String conjuration = read("common/manipulation/ferric/ConjurationManip.java");
        assertTrue(conjuration.contains("Form.CELL_HAND") && conjuration.contains("CellHandRules.DISPEL"));
        assertTrue(read("common/network/capa/harbinger/manips/UseManipKeyPacket.java").contains("ConjurationManip.dispelled(player, mainStack)"));
        assertTrue(read("common/item/harbinger/tool/living/BloodProjectionItem.java").contains("CellHandRules.PROJECTING"));
        assertTrue(read("common/item/harbinger/tool/living/BloodAbsorptionItem.java").contains("CellHandRules.ABSORBING"));
        String avatar = read("common/manipulation/animus/AvatarManifestationManager.java");
        assertTrue(avatar.contains("AvatarManifestRules.manifest(") && avatar.contains("AvatarManifestRules.dismiss("));
        String visuals = read("common/manipulation/ManipulationVisuals.java");
        assertTrue(visuals.indexOf("CAST_IMPACT, WEAPON_CONJURE, CELL_HAND, AVATAR_MANIFEST") > 0,
                "new forms must be appended after the existing ordinals");
    }

    private static String read(String relative) throws IOException {
        return Files.readString(ROOT.resolve(relative)).replace("\r\n", "\n");
    }
}
