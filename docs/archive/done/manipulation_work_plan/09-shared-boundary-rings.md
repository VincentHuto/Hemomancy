# Established blood boundary rings

Manipulation range markers now directly reuse BloodCraftRingRenderer's existing ring and inward-vein geometry, with the existing RITE_BOUNDARY_GLOW and RITE_BOUNDARY_CORE passes. The crafting renderer calls the extracted geometry with its original parameters, preserving its collapse and coloring. Manipulations supply their own tint and nominal radius.

The replacement covers Eclipse Well, Absolute Stillness, Hematic Beacon, Furnace Veins, Blood Cloud's ground range, Magnet, the Rupture ground pulse and the two existing Updraft ground-ring variants. The prior square outlines and cylindrical range rings are no longer drawn. Beacon's redundant expanding tube ring is removed. Object details such as crown/forge rings and projectile bands remain part of those subjects.

This follows the requested circular presentation even for fields whose server targeting uses a square. Their gameplay targeting and dimensions are unchanged; the ring uses the existing nominal radius and is not a new circular hit test. Blood Cloud retains its ten-block vertical ground offset. Stillness holds the ring animation at a fixed time. Range rings disappear immediately upon cue retirement and remain independent of decorative particle settings.

Both boundary passes run after the manipulation material batches finish. Each pass acquires one consumer, submits all active rings, then flushes it before obtaining the next type. This preserves the existing renderer's buffer lifetime requirements.

Verification uses the focused radius/timing/exclusion tests together with existing mesh/lifecycle tests, plus a dedicated eight-form client fixture: `./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PvisceralBoundaries runClient --console=plain`. It captures six phases per form, including refresh and cancellation. Log: `build/manipulation-boundary-acceptance.log`.

All 21 focused tests passed. The final live run completed successfully in `build/manipulation-boundary-live3.log`, with 48 captures saved under `evidence/visceral/study-boundaries/`. Representative active rings were inspected for Well, Furnace, Beacon and Cloud. Independent review found no remaining buffer, transform or duplicate-border defects. Earlier live attempts stopped at first-run onboarding; the disposable review directory now has onboarding completed and a fresh VisceralReview save. No player save was modified.

Examples: [Eclipse Well](evidence/visceral/study-boundaries/visceral-boundary-well-16.png), [Furnace Veins](evidence/visceral/study-boundaries/visceral-boundary-furnace-16.png), [Hematic Beacon](evidence/visceral/study-boundaries/visceral-boundary-beacon-16.png), [Blood Cloud](evidence/visceral/study-boundaries/visceral-boundary-cloud-16.png).
