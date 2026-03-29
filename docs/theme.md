# Towerdef Theme: Ward 13

## Setting
The game takes place in a hospital basement known as Ward 13, a sealed research floor where experiments on the human nervous system went wrong. Patients were exposed to invasive signal amplifiers and nerve grafts. The results were violent mutations that now roam the floor. The only person left inside is a nurse who never abandoned her post.

## Player Goal
Defend the nurse, the last human on the floor. The mutants are drawn to her light and want to harm her. The base in this theme is her triage station. If the nurse falls, the floor is lost.

## Visual Direction
- Top-down 2D pixel art with hard edges and readable silhouettes.
- Cold hospital colors with sickly accents: mint greens, faded blues, lead gray, and warning yellow.
- Lighting is harsh and clinical; enemies carry small glow hints (nerve sparks, IV fluid).
- Blood is minimal. The horror is in the mutations and medical gear, not gore.

## Enemy Roster (mapped to game classes)

### Ward Runner (BasicEnemy)
A fast, common patient in a torn hospital gown with bandaged arms. Veins glow faint red where the nervous system was over-stimulated.
- Silhouette: slim torso, exposed head, two clear legs for readable gait.
- Key details: bandage stripe on the torso, thin red nerve lines on forearms.
- Palette: mint gown, pale skin, white bandages, muted red veins.
- Variants:
  - v1: cleaner gown, subtle vein glow.
  - v2: greener gown, heavier bandage wrap and stronger red glow.
- Design prompt: Top-down 2D pixel art, 1024x1024 frame, lean patient sprinting in a torn mint hospital gown, pale skin, bandaged forearms, thin glowing red nerve lines, small grime smudges, readable silhouette, soft floor shadow, clean outline, detailed fabric folds and stitching, subtle rim light, limited palette, 4-frame row (idle, step-left, step-right, hit), no gore.

### Lead Apron Brute (ArmoredEnemy)
A bulky patient wrapped in a radiation lead apron with a cracked helmet mask. Slow but stubborn, the apron absorbs damage.
- Silhouette: wide shoulders, heavy chest plate, thick legs.
- Key details: hazard stripe hem on the apron, dark metal plates, slit visor glow.
- Palette: lead gray, charcoal plates, warning yellow stripes, muted visor glow.
- Variants:
  - v1: intact mask, clean stripe pattern.
  - v2: cracked visor and darker apron, more worn metal.
- Design prompt: Top-down 2D pixel art, 1024x1024 frame, bulky patient wrapped in a heavy lead apron with hazard stripes, thick boots, reinforced shoulder plates, cracked helmet mask with a narrow glowing visor, worn metal textures and rivets, wide stance, strong shadow, subtle grime gradients, limited palette, 4-frame row (idle, step-left, step-right, hit), no gore.

### IV Drifter (FlyingEnemy)
A lightweight patient floating above the tiles, tethered to a drifting IV bag. The tubes pulse like nerves as it glides.
- Silhouette: small torso and head, hover ring, trailing tube to the IV bag.
- Key details: floating bag, tube line to torso, faint hover glow under the body.
- Palette: pale skin, cool gown, teal or amber IV fluid glow.
- Variants:
  - v1: teal IV fluid and blue hover glow.
  - v2: amber IV fluid and warmer hover glow.
- Design prompt: Top-down 2D pixel art, 1024x1024 frame, small floating patient with a drifting IV bag, clear tube tether to torso, faint hover ring glow under the body, cool hospital gown, subtle bobbing motion, detailed tubing clamps and IV bag seams, limited palette, 4-frame row (idle, step-left, step-right, hit), no gore.

## Sprite Sheet Specs
- Frame size: 1024x1024 pixels.
- Layout: 4 frames in a single row (sheet size 4096x1024).
- Frames: 0 idle, 1 step-left, 2 step-right, 3 hit or flare.
- Files are stored in experimental_pictures/:
  - basic_wardrunner_v1.png
  - basic_wardrunner_v2.png
  - armored_leadbrute_v1.png
  - armored_leadbrute_v2.png
  - flying_ivdrifter_v1.png
  - flying_ivdrifter_v2.png

## Nurse Target
Nurse Mara keeps the triage light alive and signals the evacuation route. The mutants fixate on her glow, so the player must build defenses to keep her safe until extraction.
