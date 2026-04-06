#!/usr/bin/env python3
import json
import os

ROOT = "src/main/resources"
M_NS = "assets/minecraft"
F_NS = "assets/flattersigns"

# Vanilla woods in 1.20.1
WOODS = [
    "oak", "spruce", "birch", "jungle", "acacia",
    "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped",
]

# Crop config for wall signs.
BOTTOM_TRIM_PX = 5                 # remove 5px from the bottom (stem)
CROP_HEIGHT = 16 - BOTTOM_TRIM_PX  # => 11px visible
CENTER_WALL_VERTICALLY = True      # center the 11px strip within 16px height

# Small thickness for wall sheet / hanging plate to avoid z-fighting.
SHEET_T = 0.05  # model units (0..16), very thin sheet

# Vertical offset just for wall signs (raise by 1.5 px).
WALL_Y_OFFSET = 1.5  # keeps the bottom aligned with the wall sign hitbox

# Derived vertical geometry (block-space Y).
if CENTER_WALL_VERTICALLY:
    GEO_Y_FROM = (16 - CROP_HEIGHT) / 2.0  # 2.5 for 11px crop
else:
    GEO_Y_FROM = 0.0
GEO_Y_TO = GEO_Y_FROM + CROP_HEIGHT        # 13.5 if centered


def clamp(v, lo=0.0, hi=16.0):
    return max(lo, min(hi, v))


WALL_Y_FROM = clamp(GEO_Y_FROM + WALL_Y_OFFSET)  # 4.0 if centered
WALL_Y_TO = clamp(GEO_Y_TO + WALL_Y_OFFSET)      # 15.0 if centered

# Ensure dirs exist.
for p in [
    f"{ROOT}/{M_NS}/blockstates",
    f"{ROOT}/{F_NS}/models/block",
]:
    os.makedirs(p, exist_ok=True)


def rot_variants(model_path: str):
    return {f"rotation={i}": {"model": model_path} for i in range(16)}


def rot_attached_variants(model_path: str):
    out = {}
    for i in range(16):
        out[f"rotation={i},attached=true"] = {"model": model_path}
        out[f"rotation={i},attached=false"] = {"model": model_path}
    return out


def standing_cross_model(item_tex: str, uv_v2: float, y_from: float, y_to: float):
    """
    Two thin quads at ±45° around Y, like minecraft:block/cross.
    Used for standing signs and ceiling-hanging signs.
    """

    def elem(angle: float):
        return {
            "from": [0, y_from, 8],
            "to": [16, y_to, 8],
            "rotation": {
                "origin": [8, 8, 8],
                "axis": "y",
                "angle": angle,
                "rescale": False,
            },
            "shade": False,
            "faces": {
                "north": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
                "south": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
            },
        }

    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [elem(45), elem(-45)],
    }


def wall_hanging_plate_model(item_tex: str, axis: str):
    """
    Flat plate for wall hanging signs.
    Single thin quad using the hanging sign item texture.
    axis="x"/"z" chooses orientation.
    """
    if axis not in ("x", "z"):
        raise ValueError("axis must be 'x' or 'z'")

    # Slightly raised so it visually connects to its bracket / ceiling.
    y_from = 1.0
    y_to = 17.0

    half_t = SHEET_T / 2.0

    if axis == "x":
        # Plane at almost fixed Z (vertical X/Y).
        from_vec = [0.0, y_from, 8.0 - half_t]
        to_vec = [16.0, y_to, 8.0 + half_t]
        face1, face2 = "south", "north"
    else:  # axis == "z"
        # Plane at almost fixed X (vertical Y/Z).
        from_vec = [8.0 - half_t, y_from, 0.0]
        to_vec = [8.0 + half_t, y_to, 16.0]
        face1, face2 = "east", "west"

    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [
            {
                "from": from_vec,
                "to": to_vec,
                "shade": False,
                "faces": {
                    face1: {"uv": [0, 0, 16, 16], "texture": "#tex"},
                    face2: {"uv": [0, 0, 16, 16], "texture": "#tex"},
                },
            }
        ],
    }


# Wall sheet models — one per facing, placed flush to the correct block face.
# Facing semantics in vanilla: "facing" is the direction the sign FRONT looks at.

def wall_model_south(item_tex: str, uv_v2: float, y_from: float, y_to: float):
    # facing=south -> attached to the north face -> sheet near Z = 0..T
    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [{
            "from": [0, y_from, 0.0],
            "to": [16, y_to, SHEET_T],
            "shade": False,
            "faces": {
                "south": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
                "north": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
            },
        }],
    }


def wall_model_north(item_tex: str, uv_v2: float, y_from: float, y_to: float):
    # facing=north -> attached to the south face -> sheet near Z = 16-T..16
    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [{
            "from": [0, y_from, 16.0 - SHEET_T],
            "to": [16, y_to, 16.0],
            "shade": False,
            "faces": {
                "south": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
                "north": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
            },
        }],
    }


def wall_model_east(item_tex: str, uv_v2: float, y_from: float, y_to: float):
    # facing=east -> attached to the west face -> sheet near X = 0..T
    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [{
            "from": [0.0, y_from, 0],
            "to": [SHEET_T, y_to, 16],
            "shade": False,
            "faces": {
                "east": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
                "west": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
            },
        }],
    }


def wall_model_west(item_tex: str, uv_v2: float, y_from: float, y_to: float):
    # facing=west -> attached to the east face -> sheet near X = 16-T..16
    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"tex": item_tex, "particle": item_tex},
        "elements": [{
            "from": [16.0 - SHEET_T, y_from, 0],
            "to": [16.0, y_to, 16],
            "shade": False,
            "faces": {
                "east": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
                "west": {"uv": [0, 0, 16, uv_v2], "texture": "#tex"},
            },
        }],
    }


# Common "fabric:load_conditions" used for all overridden minecraft blockstates.
def blockstate_with_condition(variants: dict) -> dict:
    return {
        "fabric:load_conditions": [
            {"condition": "flattersigns:flat_models_enabled"}
        ],
        "variants": variants,
    }


# Generate assets.
for w in WOODS:
    # Standing SIGNS (ground).
    sign_item_tex = f"minecraft:item/{w}_sign"
    standing_model = standing_cross_model(sign_item_tex, uv_v2=16, y_from=0.0, y_to=16.0)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_sign_flat.json", "w") as f:
        json.dump(standing_model, f, indent=2)

    standing_variants = rot_variants(f"flattersigns:block/{w}_sign_flat")
    with open(f"{ROOT}/{M_NS}/blockstates/{w}_sign.json", "w") as f:
        json.dump(blockstate_with_condition(standing_variants), f, indent=2)

    # Wall SIGNS (flush, cropped).
    south_m = wall_model_south(sign_item_tex, uv_v2=CROP_HEIGHT, y_from=WALL_Y_FROM, y_to=WALL_Y_TO)
    west_m = wall_model_west(sign_item_tex, uv_v2=CROP_HEIGHT, y_from=WALL_Y_FROM, y_to=WALL_Y_TO)
    north_m = wall_model_north(sign_item_tex, uv_v2=CROP_HEIGHT, y_from=WALL_Y_FROM, y_to=WALL_Y_TO)
    east_m = wall_model_east(sign_item_tex, uv_v2=CROP_HEIGHT, y_from=WALL_Y_FROM, y_to=WALL_Y_TO)

    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_sign_flat_south.json", "w") as f:
        json.dump(south_m, f, indent=2)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_sign_flat_west.json", "w") as f:
        json.dump(west_m, f, indent=2)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_sign_flat_north.json", "w") as f:
        json.dump(north_m, f, indent=2)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_sign_flat_east.json", "w") as f:
        json.dump(east_m, f, indent=2)

    wall_bs_variants = {
        "facing=south": {"model": f"flattersigns:block/{w}_wall_sign_flat_south"},
        "facing=west": {"model": f"flattersigns:block/{w}_wall_sign_flat_west"},
        "facing=north": {"model": f"flattersigns:block/{w}_wall_sign_flat_north"},
        "facing=east": {"model": f"flattersigns:block/{w}_wall_sign_flat_east"},
    }
    with open(f"{ROOT}/{M_NS}/blockstates/{w}_wall_sign.json", "w") as f:
        json.dump(blockstate_with_condition(wall_bs_variants), f, indent=2)

    # Hanging SIGNS (ceiling only, not wall).
    hanging_item_tex = f"minecraft:item/{w}_hanging_sign"
    # Raise the whole cross 1px upward so it visually touches the ceiling block.
    hanging_model = standing_cross_model(hanging_item_tex, uv_v2=16, y_from=1.0, y_to=17.0)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_hanging_sign_flat.json", "w") as f:
        json.dump(hanging_model, f, indent=2)

    hanging_variants = rot_attached_variants(f"flattersigns:block/{w}_hanging_sign_flat")
    with open(f"{ROOT}/{M_NS}/blockstates/{w}_hanging_sign.json", "w") as f:
        json.dump(blockstate_with_condition(hanging_variants), f, indent=2)

    # Wall hanging SIGNS (flat plate).
    wall_hanging_model_x = wall_hanging_plate_model(hanging_item_tex, axis="x")
    wall_hanging_model_z = wall_hanging_plate_model(hanging_item_tex, axis="z")

    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_hanging_sign_flat_x.json", "w") as f:
        json.dump(wall_hanging_model_x, f, indent=2)
    with open(f"{ROOT}/{F_NS}/models/block/{w}_wall_hanging_sign_flat_z.json", "w") as f:
        json.dump(wall_hanging_model_z, f, indent=2)

    wall_hanging_variants = {
        # facing = direction the front of the hanging sign looks.
        "facing=south": {"model": f"flattersigns:block/{w}_wall_hanging_sign_flat_x"},
        "facing=north": {"model": f"flattersigns:block/{w}_wall_hanging_sign_flat_x"},
        "facing=west": {"model": f"flattersigns:block/{w}_wall_hanging_sign_flat_z"},
        "facing=east": {"model": f"flattersigns:block/{w}_wall_hanging_sign_flat_z"},
    }
    with open(f"{ROOT}/{M_NS}/blockstates/{w}_wall_hanging_sign.json", "w") as f:
        json.dump(blockstate_with_condition(wall_hanging_variants), f, indent=2)

print("Generated assets for:", ", ".join(WOODS))
print(" - Standing signs: cross models using minecraft:item/<wood>_sign")
print(" - Wall signs: flush quads (cropped 11px, +1.5px up)")
print(" - Hanging signs (ceiling): cross models using minecraft:item/<wood>_hanging_sign, raised by 1px")
print(" - Wall hanging signs: flat plates using minecraft:item/<wood>_hanging_sign (axis X/Z)")
