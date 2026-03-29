from pathlib import Path

from PIL import Image, ImageDraw

FRAME = 64
SCALE = 16
OUTLINE = (25, 29, 36, 255)
OUTPUT_DIR = Path(__file__).resolve().parent


def make_frame():
    return Image.new("RGBA", (FRAME, FRAME), (0, 0, 0, 0))


def upscale_sheet(frames):
    sheet = Image.new("RGBA", (FRAME * 4, FRAME), (0, 0, 0, 0))
    for idx, frame in enumerate(frames):
        sheet.paste(frame, (idx * FRAME, 0), frame)
    return sheet.resize((FRAME * SCALE * 4, FRAME * SCALE), Image.Resampling.NEAREST)


def stripe_fill(draw, x0, y0, x1, y1, a, b):
    for y in range(y0, y1 + 1):
        color = a if ((y - y0) // 2) % 2 == 0 else b
        draw.line((x0, y, x1, y), fill=color)


def add_notches(img, y, xs):
    px = img.load()
    for x in xs:
        if 0 <= x < FRAME and 0 <= y < FRAME:
            px[x, y] = (0, 0, 0, 0)


def draw_wardrunner_frame(p, frame_idx):
    img = make_frame()
    draw = ImageDraw.Draw(img)

    lean = (-2, 0, 2, 1)[frame_idx]
    bob = (0, -1, 0, 1)[frame_idx]
    left_step = (0, -2, 2, 1)[frame_idx]
    right_step = (0, 2, -2, -1)[frame_idx]
    left_swing = (0, -2, 2, 0)[frame_idx]
    claw_reach = (0, 3, -2, 2)[frame_idx]
    head_tilt = (-1, 0, 1, 0)[frame_idx]
    hit = frame_idx == 3

    # Longer shadow plus asymmetry gives a stronger mutant sprint read.
    draw.ellipse((18 + lean, 50 + bob, 48 + lean, 58 + bob), fill=p["shadow"])

    # Legs are intentionally uneven: one normal stride leg and one warped talon leg.
    draw.rectangle((24 + left_step + lean, 47 + bob, 30 + left_step + lean, 60 + bob), fill=p["leg"], outline=OUTLINE)
    draw.rectangle((34 + right_step + lean, 46 + bob, 41 + right_step + lean, 58 + bob), fill=p["leg"], outline=OUTLINE)
    draw.polygon(
        [
            (33 + right_step + lean, 58 + bob),
            (43 + right_step + lean, 58 + bob),
            (42 + right_step + lean, 60 + bob),
            (35 + right_step + lean, 60 + bob),
        ],
        fill=p["claw"],
        outline=OUTLINE,
    )

    # Hunched torso and torn gown shape.
    draw.polygon(
        [
            (18 + lean, 31 + bob),
            (44 + lean, 28 + bob),
            (47 + lean, 53 + bob),
            (17 + lean, 55 + bob),
        ],
        fill=p["gown_dark"],
        outline=OUTLINE,
    )
    draw.polygon(
        [
            (20 + lean, 22 + bob),
            (43 + lean, 21 + bob),
            (42 + lean, 33 + bob),
            (22 + lean, 34 + bob),
        ],
        fill=p["gown_light"],
        outline=OUTLINE,
    )

    # Infected chest patch.
    draw.polygon(
        [
            (27 + lean, 30 + bob),
            (36 + lean, 30 + bob),
            (38 + lean, 37 + bob),
            (29 + lean, 39 + bob),
        ],
        fill=p["lesion"],
        outline=OUTLINE,
    )

    # Left arm remains mostly human and wrapped.
    draw.rectangle((15 + left_swing + lean, 24 + bob, 19 + left_swing + lean, 40 + bob), fill=p["skin"], outline=OUTLINE)
    stripe_fill(
        draw,
        16 + left_swing + lean,
        28 + bob,
        18 + left_swing + lean,
        37 + bob,
        p["bandage_a"],
        p["bandage_b"],
    )

    # Right arm is mutated into a larger claw profile.
    draw.polygon(
        [
            (43 + lean, 24 + bob),
            (49 + claw_reach + lean, 28 + bob),
            (47 + claw_reach + lean, 39 + bob),
            (42 + lean, 35 + bob),
        ],
        fill=p["mutant_flesh"],
        outline=OUTLINE,
    )
    draw.polygon(
        [
            (47 + claw_reach + lean, 31 + bob),
            (52 + claw_reach + lean, 30 + bob),
            (49 + claw_reach + lean, 34 + bob),
        ],
        fill=p["claw"],
        outline=OUTLINE,
    )
    draw.polygon(
        [
            (46 + claw_reach + lean, 35 + bob),
            (51 + claw_reach + lean, 36 + bob),
            (47 + claw_reach + lean, 39 + bob),
        ],
        fill=p["claw"],
        outline=OUTLINE,
    )

    # Head keeps nurse-world readability, but with a single bright infected eye.
    draw.ellipse((24 + lean + head_tilt, 8 + bob, 39 + lean + head_tilt, 23 + bob), fill=p["skin"], outline=OUTLINE)
    draw.rectangle((23 + lean + head_tilt, 7 + bob, 39 + lean + head_tilt, 9 + bob), fill=p["hair"], outline=OUTLINE)
    draw.rectangle((32 + lean + head_tilt, 14 + bob, 34 + lean + head_tilt, 15 + bob), fill=p["eye"])
    draw.line((26 + lean + head_tilt, 14 + bob, 30 + lean + head_tilt, 18 + bob), fill=p["stitch"])

    draw.line((22 + lean, 24 + bob, 19 + left_swing + lean, 30 + bob), fill=p["vein"])
    draw.line((39 + lean, 24 + bob, 45 + claw_reach + lean, 31 + bob), fill=p["vein"])
    draw.line((28 + lean, 34 + bob, 36 + lean, 42 + bob), fill=p["vein_dim"])

    # Exposed spine seam highlights mutation without adding gore.
    draw.line((31 + lean, 30 + bob, 33 + lean, 45 + bob), fill=p["spine"])
    for y in range(31 + bob, 47 + bob, 3):
        draw.point((32 + lean, y), fill=p["spine_glow"])

    draw.line((22 + lean, 23 + bob, 41 + lean, 23 + bob), fill=p["trim"])
    draw.line((22 + lean, 34 + bob, 41 + lean, 34 + bob), fill=p["trim"])

    for x in range(20 + lean, 45 + lean, 4):
        draw.point((x, 54 + bob), fill=OUTLINE)

    add_notches(img, 55 + bob, [21 + lean, 25 + lean, 29 + lean, 33 + lean, 37 + lean, 41 + lean])

    if hit:
        draw.ellipse((17 + lean, 6 + bob, 47 + lean, 32 + bob), outline=p["hit"], width=2)
        draw.line((48 + claw_reach + lean, 30 + bob, 53 + claw_reach + lean, 27 + bob), fill=p["hit"])
        draw.line((46 + claw_reach + lean, 36 + bob, 52 + claw_reach + lean, 39 + bob), fill=p["hit"])
        draw.line((30 + lean, 16 + bob, 34 + lean, 12 + bob), fill=p["hit"])

    return img


def draw_leadbrute_frame(p, frame_idx):
    img = make_frame()
    draw = ImageDraw.Draw(img)

    sway = (0, -1, 1, 0)[frame_idx]
    stomp = (0, 1, 1, 0)[frame_idx]
    left_step = (0, -1, 1, 0)[frame_idx]
    right_step = (0, 1, -1, 0)[frame_idx]
    hit = frame_idx == 3

    draw.ellipse((16 + sway, 53 + stomp, 48 + sway, 60 + stomp), fill=p["shadow"])

    draw.rectangle((13 + sway, 24, 20 + sway, 49), fill=p["arm_back"], outline=OUTLINE)
    draw.rectangle((44 + sway, 24, 51 + sway, 49), fill=p["arm_back"], outline=OUTLINE)

    draw.rectangle((18 + sway, 20, 46 + sway, 56), fill=p["apron_dark"], outline=OUTLINE)
    draw.rectangle((19 + sway, 20, 45 + sway, 41), fill=p["apron_light"])

    draw.rectangle((22 + sway + left_step, 47, 30 + sway + left_step, 60), fill=p["boot"], outline=OUTLINE)
    draw.rectangle((34 + sway + right_step, 47, 42 + sway + right_step, 60), fill=p["boot"], outline=OUTLINE)

    draw.rectangle((11 + sway, 22, 19 + sway, 31), fill=p["plate"], outline=OUTLINE)
    draw.rectangle((45 + sway, 22, 53 + sway, 31), fill=p["plate"], outline=OUTLINE)

    draw.rectangle((21 + sway, 14, 43 + sway, 33), fill=p["helmet"], outline=OUTLINE)
    draw.rectangle((22 + sway, 10, 42 + sway, 20), fill=p["helmet_top"], outline=OUTLINE)
    draw.rectangle((24 + sway, 12, 40 + sway, 13), fill=p["visor_glow"])

    draw.rectangle((20 + sway, 20, 44 + sway, 35), fill=p["chest_plate"], outline=OUTLINE)
    for x in range(22 + sway, 43 + sway, 4):
        draw.point((x, 21), fill=p["rivet"])

    for x in range(20 + sway, 46 + sway):
        draw.point((x, 34), fill=p["apron_seam"])

    stripe_fill(draw, 19 + sway, 57, 45 + sway, 59, p["hazard_a"], p["hazard_b"])

    if hit:
        draw.line((31 + sway, 13, 35 + sway, 16), fill=p["crack"])
        draw.line((35 + sway, 16, 38 + sway, 15), fill=p["crack"])
        draw.line((24 + sway, 40, 20 + sway, 43), fill=p["spark"])
        draw.line((40 + sway, 41, 45 + sway, 44), fill=p["spark"])

    return img


def draw_ivdrifter_frame(p, frame_idx):
    img = make_frame()
    draw = ImageDraw.Draw(img)

    bob = (0, -1, 0, 1)[frame_idx]
    drift = (0, -1, 1, 0)[frame_idx]
    hit = frame_idx == 3

    glow_alpha = 150 if hit else 115
    draw.ellipse((18 + drift, 51 + bob, 46 + drift, 59 + bob), fill=(p["hover"][0], p["hover"][1], p["hover"][2], glow_alpha))
    draw.ellipse((23 + drift, 55 + bob, 41 + drift, 59 + bob), fill=p["shadow"])

    draw.ellipse((24 + drift, 9 + bob, 40 + drift, 24 + bob), fill=p["skin"], outline=OUTLINE)
    draw.rectangle((24 + drift, 23 + bob, 40 + drift, 24 + bob), fill=p["neck_wrap"])

    draw.rectangle((22 + drift, 24 + bob, 42 + drift, 46 + bob), fill=p["gown"], outline=OUTLINE)
    draw.rectangle((21 + drift, 24 + bob, 43 + drift, 28 + bob), fill=p["strap"], outline=OUTLINE)
    draw.rectangle((18 + drift, 27 + bob, 21 + drift, 41 + bob), fill=p["arm"], outline=OUTLINE)
    draw.rectangle((43 + drift, 27 + bob, 46 + drift, 41 + bob), fill=p["arm"], outline=OUTLINE)

    draw.ellipse((20 + drift, 43 + bob, 44 + drift, 52 + bob), outline=p["ring"], width=2)

    bag_x = 48 + drift
    bag_y = 19 + bob
    draw.rectangle((bag_x, bag_y, bag_x + 10, bag_y + 22), fill=p["bag"], outline=OUTLINE)
    draw.rectangle((bag_x + 1, bag_y + 10, bag_x + 9, bag_y + 22), fill=p["fluid"])
    draw.line((bag_x + 2, bag_y + 2, bag_x + 8, bag_y + 2), fill=p["bag_highlight"])

    draw.line((43 + drift, 30 + bob, bag_x, bag_y + 14), fill=p["tube"])
    draw.point((45 + drift, 31 + bob), fill=p["tube_glow"])
    draw.point((46 + drift, 32 + bob), fill=p["tube_glow"])

    draw.polygon(
        [
            (25 + drift, 46 + bob),
            (22 + drift, 50 + bob),
            (27 + drift, 50 + bob),
        ],
        fill=p["cloth"],
        outline=OUTLINE,
    )
    draw.polygon(
        [
            (39 + drift, 46 + bob),
            (43 + drift, 50 + bob),
            (37 + drift, 50 + bob),
        ],
        fill=p["cloth"],
        outline=OUTLINE,
    )

    if hit:
        draw.ellipse((18 + drift, 15 + bob, 46 + drift, 40 + bob), outline=p["hit"], width=2)
        draw.line((bag_x + 11, bag_y + 8, bag_x + 14, bag_y + 5), fill=p["hit"])

    return img


def build_sheets():
    ward_palettes = {
        "basic_wardrunner_v1.png": {
            "skin": (202, 185, 166, 255),
            "hair": (95, 67, 50, 255),
            "gown_light": (122, 188, 179, 255),
            "gown_dark": (86, 150, 142, 255),
            "leg": (66, 82, 94, 255),
            "bandage_a": (238, 231, 220, 255),
            "bandage_b": (215, 202, 185, 255),
            "trim": (219, 244, 239, 255),
            "vein": (233, 96, 96, 255),
            "vein_dim": (183, 84, 84, 255),
            "shadow": (95, 132, 139, 145),
            "hit": (235, 74, 66, 255),
            "mutant_flesh": (157, 118, 122, 255),
            "claw": (190, 167, 135, 255),
            "lesion": (164, 70, 78, 255),
            "eye": (241, 95, 95, 255),
            "stitch": (102, 78, 76, 255),
            "spine": (143, 64, 68, 255),
            "spine_glow": (229, 122, 118, 255),
        },
        "basic_wardrunner_v2.png": {
            "skin": (191, 171, 158, 255),
            "hair": (63, 73, 89, 255),
            "gown_light": (133, 189, 108, 255),
            "gown_dark": (99, 155, 79, 255),
            "leg": (70, 77, 83, 255),
            "bandage_a": (226, 215, 196, 255),
            "bandage_b": (197, 183, 161, 255),
            "trim": (225, 245, 206, 255),
            "vein": (247, 78, 78, 255),
            "vein_dim": (188, 70, 70, 255),
            "shadow": (96, 140, 86, 145),
            "hit": (230, 70, 54, 255),
            "mutant_flesh": (145, 126, 112, 255),
            "claw": (181, 167, 117, 255),
            "lesion": (149, 85, 65, 255),
            "eye": (255, 132, 72, 255),
            "stitch": (108, 88, 73, 255),
            "spine": (129, 77, 60, 255),
            "spine_glow": (236, 141, 96, 255),
        },
    }

    brute_palettes = {
        "armored_leadbrute_v1.png": {
            "shadow": (92, 109, 128, 150),
            "arm_back": (62, 66, 70, 255),
            "apron_dark": (70, 84, 95, 255),
            "apron_light": (82, 98, 111, 255),
            "boot": (58, 60, 65, 255),
            "plate": (103, 106, 109, 255),
            "helmet": (64, 66, 70, 255),
            "helmet_top": (58, 60, 64, 255),
            "visor_glow": (150, 212, 232, 255),
            "chest_plate": (63, 65, 69, 255),
            "rivet": (109, 142, 161, 255),
            "apron_seam": (91, 104, 116, 255),
            "hazard_a": (224, 195, 72, 255),
            "hazard_b": (54, 53, 52, 255),
            "crack": (129, 144, 158, 255),
            "spark": (178, 223, 240, 255),
        },
        "armored_leadbrute_v2.png": {
            "shadow": (112, 96, 73, 150),
            "arm_back": (63, 56, 48, 255),
            "apron_dark": (108, 92, 67, 255),
            "apron_light": (120, 103, 76, 255),
            "boot": (61, 54, 47, 255),
            "plate": (100, 89, 75, 255),
            "helmet": (64, 57, 50, 255),
            "helmet_top": (57, 51, 45, 255),
            "visor_glow": (203, 112, 104, 255),
            "chest_plate": (63, 55, 48, 255),
            "rivet": (135, 96, 92, 255),
            "apron_seam": (138, 113, 84, 255),
            "hazard_a": (232, 149, 60, 255),
            "hazard_b": (57, 50, 43, 255),
            "crack": (146, 121, 108, 255),
            "spark": (238, 176, 93, 255),
        },
    }

    drifter_palettes = {
        "flying_ivdrifter_v1.png": {
            "hover": (123, 187, 245, 255),
            "shadow": (91, 134, 176, 190),
            "skin": (197, 200, 211, 255),
            "neck_wrap": (170, 177, 194, 255),
            "gown": (120, 141, 178, 255),
            "strap": (98, 116, 151, 255),
            "arm": (192, 197, 209, 255),
            "ring": (141, 215, 252, 255),
            "bag": (95, 191, 161, 255),
            "fluid": (68, 162, 137, 255),
            "bag_highlight": (191, 247, 228, 255),
            "tube": (220, 228, 231, 255),
            "tube_glow": (135, 234, 209, 255),
            "cloth": (93, 113, 153, 255),
            "hit": (132, 236, 255, 255),
        },
        "flying_ivdrifter_v2.png": {
            "hover": (226, 174, 108, 255),
            "shadow": (151, 112, 69, 190),
            "skin": (202, 186, 186, 255),
            "neck_wrap": (175, 157, 157, 255),
            "gown": (136, 118, 170, 255),
            "strap": (113, 96, 146, 255),
            "arm": (196, 179, 182, 255),
            "ring": (245, 195, 130, 255),
            "bag": (213, 150, 88, 255),
            "fluid": (190, 120, 67, 255),
            "bag_highlight": (247, 214, 165, 255),
            "tube": (228, 220, 208, 255),
            "tube_glow": (246, 188, 109, 255),
            "cloth": (114, 96, 144, 255),
            "hit": (255, 191, 99, 255),
        },
    }

    for filename, palette in ward_palettes.items():
        frames = [draw_wardrunner_frame(palette, idx) for idx in range(4)]
        upscale_sheet(frames).save(OUTPUT_DIR / filename)

    for filename, palette in brute_palettes.items():
        frames = [draw_leadbrute_frame(palette, idx) for idx in range(4)]
        upscale_sheet(frames).save(OUTPUT_DIR / filename)

    for filename, palette in drifter_palettes.items():
        frames = [draw_ivdrifter_frame(palette, idx) for idx in range(4)]
        upscale_sheet(frames).save(OUTPUT_DIR / filename)


if __name__ == "__main__":
    build_sheets()
