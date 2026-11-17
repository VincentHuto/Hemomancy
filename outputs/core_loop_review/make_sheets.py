"""Builds labelled portrait sheets from the core-loop review-client screenshots (copied to raw/)."""
import os
from PIL import Image, ImageDraw, ImageFont

R = 'outputs/core_loop_review/raw/'
O = 'outputs/core_loop_review/'
W = 1280


def font(size, bold=False):
    return ImageFont.truetype(r'C:\Windows\Fonts\segoeui' + ('b' if bold else '') + '.ttf', size)


SHEETS = [
    ('01-verbs', 'Two verbs (Track 1)', [
        ('verb/verb-0-plain-projection.png', 'Plain Projection held on stone, nothing shaping it'),
        ('verb/verb-1-shaping-claims-stone.png', 'A selected Shaping claims the target: label and red reticle ticks'),
        ('verb/verb-6-staff-absorbs-pig.png', 'Living Staff channelling Absorption from a pig'),
        ('verb/verb-7-radial-inner-verbs.png', 'Radial: both verbs back on the inner ring, hand item icons')]),
    ('02-first-shapings', 'First Shapings (Track 2)', [
        ('shapings/shapings-0-lignum-crawl-44.png', 'Lignum Mortis: rot crawling up a trunk under Projection'),
        ('shapings/shapings-1-lignum-felled-14.png', 'Lignum Mortis: the tree felled'),
        ('shapings/shapings-2-pyretic-items-25.png', 'Pyretic Forge: smelting loose ore on the ground'),
        ('shapings/shapings-6-tendon-haul-34.png', 'Tendon Line: hauling along the cord'),
        ('shapings/shapings-7-binding-held-40.png', 'Blood Binding held on a creature')]),
    ('03-brazier', 'Brazier transmutation (Track 2)', [
        ('shapings/shapings-4-brazier-transmutes-46.png', 'A lit brazier transmuting an item under a held verb'),
        ('jei/jei-0-brazier_transmutation.png', 'JEI category for brazier transmutation')]),
    ('04-living-blood', 'Living blood (Track 3)', [
        ('living-blood/living-blood-0-tank-fresh.png', 'Tank: fresh'),
        ('living-blood/living-blood-2-tank-clotting.png', 'Tank: clotting'),
        ('living-blood/living-blood-3-tank-clotted-through.png', 'Tank: clotted through'),
        ('living-blood/living-blood-4-gauge-stalled-clotting.png', 'Gauge reading a stalled, clotting machine')]),
    ('05-temper', 'Temper (Track 4)', [
        ('temper/temper-0-hud-eight-schools.png', 'HUD: all eight Temper pools'),
        ('temper/temper-2-cast-dry.png', 'Casting dry: the notice'),
        ('temper/temper-3-cast-catalyst.png', 'Casting with a catalyst in the off hand'),
        ('temper/temper-4-scrying-temper-tab.png', 'Scrying diagnostics: Temper tab')]),
    ('06-ground-network', 'Ground network (Track 6)', [
        ('ground-network/ground-0-stains-40.png', 'Tier 1: stains'),
        ('ground-network/ground-3-gouge-lit-60.png', 'Tier 2: a gouge run lit by a pulse'),
        ('ground-network/ground-4-functions-40.png', 'Gouge functions from different catalysts'),
        ('ground-network/ground-5-lux-trace-74.png', 'Lux trace'),
        ('ground-network/ground-6-vein-50.png', 'Tier 3: living vein')]),
    ('07-farming-water', 'Farming and water (Track 7)', [
        ('farming/farming-0-drip-field-70.png', 'Scab spikes dripping onto a field'),
        ('farming/farming-1-planting-basins-50.png', 'Planting basins'),
        ('farming/farming-2-cloche-30.png', 'The cloche'),
        ('farming/farming-4-chum-92.png', 'Blood Chum drawing fish')]),
    ('08-menagerie', 'Menagerie (Track 5)', [
        ('menagerie/menagerie-0-vats-40.png', 'Vats with kept creatures'),
        ('menagerie/menagerie-2-leash-70.png', 'A creature on the leash'),
        ('menagerie/menagerie-4-draw-80.png', 'Drawing from a vat'),
        ('menagerie/menagerie-5-readout-40.png', 'Vessel readout (chat lines for now)')]),
    ('09-strands-a', 'Strand combat, 1 of 2 (Track 12)', [
        ('strand/strand-1-lash-third-person-35.png', 'Lash: Searing Lash, third person'),
        ('strand/strand-2-blood-lash-crack-37.png', 'Lash: Blood Lash (the Animus teaching lash), the crack'),
        ('strand/strand-3-jet-held-66.png', 'Jet: Hematic Flare held')]),
    ('10-strands-b', 'Strand combat, 2 of 2 (Track 12)', [
        ('strand/strand-5-spray-crowd-34.png', 'Spray: Blood Eclipse into a crowd'),
        ('strand/strand-6-plant-sinking-70.png', 'Plant: Glacial Grasp erupting'),
        ('strand/strand-7-brand-held-34.png', 'Brand: Hemorrhage held on a target')]),
    ('11-staff-mining', 'Living Staff and mining (Track 8)', [
        ('staff-mining/staffmining-2-staff-five-rings-side-34.png', 'Staff with five growth rings'),
        ('staff-mining/staffmining-3-pickaxe-digging-46.png', 'Living pickaxe form digging'),
        ('staff-mining/staffmining-6-lode-draw-held-56.png', 'Lode Draw held on an ore vein'),
        ('staff-mining/staffmining-8-hewing-tunnel-50.png', 'Hematic Hewing cutting a tunnel')]),
    ('12-living-land', 'Living Land (Track 9)', [
        ('living-land/livingland-0-floor-teeth-37.png', 'Floor teeth'),
        ('living-land/livingland-3-wall-lances-side-37.png', 'Wall lances, side view'),
        ('living-land/livingland-5-ceiling-crush-side-47.png', 'Ceiling crush, side view'),
        ('living-land/livingland-6-raise-step-78.png', 'Raising a step')]),
    ('13-cling', 'Venous Cling (Track 9)', [
        ('living-land/livingland-8-cling-wall-side-60.png', 'Clinging to a wall, side view'),
        ('living-land/livingland-9-cling-wall-climb-front-50.png', 'Climbing, front view'),
        ('living-land/livingland-10-cling-wall-cleats-30.png', 'With hematic iron cleats'),
        ('living-land/livingland-11-cling-ceiling-side-60.png', 'On the ceiling')]),
    ('14-daily-loop', 'Daily loop and world answer (Track 10)', [
        ('daily-loop/daily-loop-1-instruments-seething-30.png', 'Barometer and Ossuary Clock: seething'),
        ('daily-loop/daily-loop-3-instrument-closeup-30.png', 'Instrument close-up'),
        ('daily-loop/daily-loop-5-scavenger-45.png', 'A scavenger drawn to a stain'),
        ('daily-loop/daily-loop-6-meal-52.png', 'A meal feeding blood')]),
    ('15-teaching', 'Teaching (Track 11)', [
        ('teaching/core-loop-teaching-00-toast-shaped.png', 'First-time advancement toast'),
        ('teaching/core-loop-teaching-01-liber-the_two_verbs.png', 'Liber page: The Two Verbs'),
        ('teaching/core-loop-teaching-33-liber-temper.png', 'Liber page: Temper'),
        ('teaching/core-loop-teaching-49-advancements-tab.png', 'Advancements tab')]),
]

BG, BAR, BONE, RED = (22, 10, 12), (40, 16, 20), (236, 224, 203), (255, 72, 84)

for slug, title, shots in SHEETS:
    images = []
    for name, caption in shots:
        image = Image.open(R + name).convert('RGB')
        images.append((image.resize((W, round(image.height * W / image.width)), Image.LANCZOS), caption))
    height = 110 + sum(image.height + 64 for image, _ in images) + 20
    sheet = Image.new('RGB', (W, height), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((28, 18), 'HEMOMANCY  CORE LOOP  REVIEW CLIENT CAPTURES', font=font(18, True), fill=RED)
    draw.text((28, 46), title, font=font(40, True), fill=BONE)
    y = 110
    for index, (image, caption) in enumerate(images, 1):
        draw.rectangle((0, y, W, y + 56), fill=BAR)
        draw.text((28, y + 10), '%d.' % index, font=font(28, True), fill=RED)
        draw.text((70, y + 10), caption, font=font(28), fill=BONE)
        sheet.paste(image, (0, y + 56))
        y += image.height + 64
    sheet.save(O + slug + '.jpg', quality=88)
    print(slug, sheet.size, os.path.getsize(O + slug + '.jpg') // 1024, 'KB')
