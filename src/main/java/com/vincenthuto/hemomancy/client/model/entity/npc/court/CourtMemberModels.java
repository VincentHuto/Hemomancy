package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Each built Court role's generated model: its layer, definition, factory and textures. LayerEvents registers the
 * layers and EntityRendererRegistrar builds a {@code CourtMemberRenderer} per row.
 */
public final class CourtMemberModels {
	public record Row(CourtRole role, ModelLayerLocation layer, Supplier<LayerDefinition> definition,
			Function<ModelPart, CourtMemberModel> factory, ResourceLocation texture, @Nullable ResourceLocation glow,
			float shadow) {
	}

	public static final List<Row> ALL = List.of(
			new Row(CourtRole.GARDENER, CourtGardenerModel.LAYER_LOCATION, CourtGardenerModel::createBodyLayer,
					CourtGardenerModel::new, CourtGardenerModel.TEXTURE, CourtGardenerModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.ARBOR, CourtArborModel.LAYER_LOCATION, CourtArborModel::createBodyLayer,
					CourtArborModel::new, CourtArborModel.TEXTURE, CourtArborModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.LIGHTKEEPER, CourtLightkeeperModel.LAYER_LOCATION, CourtLightkeeperModel::createBodyLayer,
					CourtLightkeeperModel::new, CourtLightkeeperModel.TEXTURE, CourtLightkeeperModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.TIDEWARDEN, CourtTidewardenModel.LAYER_LOCATION, CourtTidewardenModel::createBodyLayer,
					CourtTidewardenModel::new, CourtTidewardenModel.TEXTURE, CourtTidewardenModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.BRACED_ELDER, CourtBracedElderModel.LAYER_LOCATION, CourtBracedElderModel::createBodyLayer,
					CourtBracedElderModel::new, CourtBracedElderModel.TEXTURE, CourtBracedElderModel.GLOW_TEXTURE, 0.6F),
			new Row(CourtRole.EDGE_SWIMMER, CourtEdgeSwimmerModel.LAYER_LOCATION, CourtEdgeSwimmerModel::createBodyLayer,
					CourtEdgeSwimmerModel::new, CourtEdgeSwimmerModel.TEXTURE, CourtEdgeSwimmerModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.NURSERY_TENDER, CourtNurseryTenderModel.LAYER_LOCATION, CourtNurseryTenderModel::createBodyLayer,
					CourtNurseryTenderModel::new, CourtNurseryTenderModel.TEXTURE, CourtNurseryTenderModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.LINESMAN, CourtLinesmanModel.LAYER_LOCATION, CourtLinesmanModel::createBodyLayer,
					CourtLinesmanModel::new, CourtLinesmanModel.TEXTURE, CourtLinesmanModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.LAMP_COLONY, CourtLampColonyModel.LAYER_LOCATION, CourtLampColonyModel::createBodyLayer,
					CourtLampColonyModel::new, CourtLampColonyModel.TEXTURE, CourtLampColonyModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.HOST, CourtHostModel.LAYER_LOCATION, CourtHostModel::createBodyLayer,
					CourtHostModel::new, CourtHostModel.TEXTURE, CourtHostModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.VEILED_RESIDENT, CourtVeiledResidentModel.LAYER_LOCATION, CourtVeiledResidentModel::createBodyLayer,
					CourtVeiledResidentModel::new, CourtVeiledResidentModel.TEXTURE, CourtVeiledResidentModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.BELLWARDEN, CourtBellwardenModel.LAYER_LOCATION, CourtBellwardenModel::createBodyLayer,
					CourtBellwardenModel::new, CourtBellwardenModel.TEXTURE, CourtBellwardenModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.SLEEPER, CourtSleeperModel.LAYER_LOCATION, CourtSleeperModel::createBodyLayer,
					CourtSleeperModel::new, CourtSleeperModel.TEXTURE, CourtSleeperModel.GLOW_TEXTURE, 0.3F),
			new Row(CourtRole.MARROW_TENDER, CourtMarrowTenderModel.LAYER_LOCATION, CourtMarrowTenderModel::createBodyLayer,
					CourtMarrowTenderModel::new, CourtMarrowTenderModel.TEXTURE, CourtMarrowTenderModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.ROOTED_ELDER, CourtRootedElderModel.LAYER_LOCATION, CourtRootedElderModel::createBodyLayer,
					CourtRootedElderModel::new, CourtRootedElderModel.TEXTURE, CourtRootedElderModel.GLOW_TEXTURE, 0.7F),
			new Row(CourtRole.OFFERING_BEARER, CourtOfferingBearerModel.LAYER_LOCATION, CourtOfferingBearerModel::createBodyLayer,
					CourtOfferingBearerModel::new, CourtOfferingBearerModel.TEXTURE, CourtOfferingBearerModel.GLOW_TEXTURE, 0.5F),
			new Row(CourtRole.KINDLED_ELDER, CourtKindledElderModel.LAYER_LOCATION, CourtKindledElderModel::createBodyLayer,
					CourtKindledElderModel::new, CourtKindledElderModel.TEXTURE, CourtKindledElderModel.GLOW_TEXTURE, 0.8F),
			new Row(CourtRole.HEAD_TENDER, CourtHeadTenderModel.LAYER_LOCATION, CourtHeadTenderModel::createBodyLayer,
					CourtHeadTenderModel::new, CourtHeadTenderModel.TEXTURE, CourtHeadTenderModel.GLOW_TEXTURE, 0.5F));

	/**
	 * The Court's followers who fight (Track 10, Tier 2), on their own entity types: the Bloom Warden on the Arbor's rig
	 * (its row's role is the Arbor, so it wears Sunburst at Primal) and the Kindled Champion on the Kindled elder's body.
	 */
	public static final Row BLOOM_WARDEN = new Row(CourtRole.ARBOR, CourtBloomWardenModel.LAYER_LOCATION,
			CourtBloomWardenModel::createBodyLayer, CourtBloomWardenModel::new, CourtBloomWardenModel.TEXTURE,
			CourtBloomWardenModel.GLOW_TEXTURE, 0.6F);
	public static final Row KINDLED_CHAMPION = new Row(CourtRole.KINDLED_ELDER, CourtKindledChampionModel.LAYER_LOCATION,
			CourtKindledChampionModel::createBodyLayer, CourtKindledChampionModel::new, CourtKindledChampionModel.TEXTURE,
			CourtKindledChampionModel.GLOW_TEXTURE, 0.9F);
	public static final List<Row> FOLLOWERS = List.of(BLOOM_WARDEN, KINDLED_CHAMPION);

	private CourtMemberModels() {
	}
}
