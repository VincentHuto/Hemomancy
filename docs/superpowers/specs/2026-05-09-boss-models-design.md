# Boss Models Design

## Approved Direction
Use a readable first-release art pass rather than waiting for final Blockbench or GeckoLib assets. The goal is to remove effectively invisible boss rendering while giving each major encounter a lore-specific silhouette.

## Scope
- Add dedicated Java model layers for Annetta Knowles, Stained Priestess, Latent Annetta Infection, Putriciel, and Velorum.
- Replace `blank.png` texture use in their renderers with opaque placeholder textures under boss-specific texture paths.
- Register the new layer definitions through the existing `LayerEvents` path.
- Preserve existing Hollow Vessel and Seraphae custom models unless build or manual inspection reveals a separate issue.

## Visual Targets
- **Annetta Knowles:** silver/white Unstained cleric, hunched when cowering, teal verdigris eyes, subtle blood fissures.
- **Stained Priestess:** taller corrupted phase, white robe overtaken by blood-crystal geometry, keratin/hair/nail accents, Sanguis Lancea silhouette.
- **Latent Annetta Infection:** externalized nonhuman infection mass, pale tissue, crimson pressure, teal/pale fungal bloom, tendrils instead of a clean humanoid profile.
- **Putriciel:** heavy rotting saint, fungal/ash mantle, rancid green and rust-red bloom pockets, broken votive halo.
- **Velorum:** narrow frozen martyr, pale silver-blue veil, dark internal body, quiet ice-mantle silhouette.

## Validation
- Run `./gradlew.bat build`.
- Confirm generated texture assets are nontransparent by script inspection.
- Manual in-game spawn inspection remains recommended after the compile pass.
