#!/usr/bin/env python3
from pathlib import Path
import json
import math
import shutil

import numpy as np
from PIL import Image, ImageEnhance, ImageFilter, ImageOps

ROOT = Path(__file__).resolve().parents[1]
RP = ROOT / "resource-pack"
DP = ROOT / "datapack"

PANORAMA_SOURCE = ROOT / "Loading-screen-panorama-1.png"
LOADING_SOURCE = ROOT / "Loading-screen.png"

PANORAMA_DIR = RP / "assets/minecraft/textures/gui/title/background"
GABCON_TEX = RP / "assets/gabcon/textures/font"
GABCON_FONT = RP / "assets/gabcon/font"

CONTAINER_DIR = RP / "assets/minecraft/textures/gui/container"
HUD_DIR = RP / "assets/minecraft/textures/gui/sprites/hud"
BASE_CONTAINER = ROOT / "sources/gui-base/container"
BASE_HUD = ROOT / "sources/gui-base/hud"

RESAMPLE = Image.Resampling.LANCZOS


def ensure_parent(path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)


def save_png(img: Image.Image, path: Path):
    ensure_parent(path)
    img.save(path, "PNG", optimize=True)


def scale_alpha(img: Image.Image, factor: float) -> Image.Image:
    rgba = img.convert("RGBA")
    arr = np.array(rgba, dtype=np.uint8)
    alpha = arr[:, :, 3].astype(np.float32)
    alpha = np.clip(alpha * factor, 0, 255).astype(np.uint8)
    arr[:, :, 3] = alpha
    return Image.fromarray(arr, "RGBA")


def backup_once(src: Path, backup: Path):
    if backup.exists():
        return
    backup.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, backup)


def restore_and_transparent(src: Path, backup: Path, opacity: float):
    backup_once(src, backup)
    base = Image.open(backup).convert("RGBA")
    save_png(scale_alpha(base, opacity), src)


def equirect_to_faces(img: Image.Image, face_size: int):
    src = np.array(img.convert("RGB"))
    h, w = src.shape[:2]

    u = np.linspace(-1.0, 1.0, face_size, dtype=np.float32)
    v = np.linspace(-1.0, 1.0, face_size, dtype=np.float32)
    uu, vv = np.meshgrid(u, v)

    faces = []
    for face in range(6):
        if face == 0:      # front
            x, y, z = uu, -vv, np.ones_like(uu)
        elif face == 1:    # right
            x, y, z = np.ones_like(uu), -vv, -uu
        elif face == 2:    # back
            x, y, z = -uu, -vv, -np.ones_like(uu)
        elif face == 3:    # left
            x, y, z = -np.ones_like(uu), -vv, uu
        elif face == 4:    # up
            x, y, z = uu, np.ones_like(uu), vv
        else:              # down
            x, y, z = uu, -np.ones_like(uu), -vv

        norm = np.sqrt(x*x + y*y + z*z)
        x, y, z = x / norm, y / norm, z / norm
        lon = np.arctan2(x, z)
        lat = np.arcsin(y)

        sx = ((lon / (2.0 * math.pi) + 0.5) * (w - 1)).astype(np.int32) % w
        sy = ((0.5 - lat / math.pi) * (h - 1)).astype(np.int32)
        sy = np.clip(sy, 0, h - 1)

        faces.append(Image.fromarray(src[sy, sx], "RGB"))
    return faces


def flat_image_to_faces(img: Image.Image, face_size: int):
    # Fallback for normal 16:9 / 3:2 artwork: create a coherent mirrored
    # horizontal strip and atmospheric top/bottom faces.
    base = img.convert("RGB")
    resized = ImageOps.contain(base, (face_size * 2, face_size), RESAMPLE)
    if resized.height != face_size:
        scale = face_size / resized.height
        resized = resized.resize((max(face_size, int(resized.width * scale)), face_size), RESAMPLE)

    strip = Image.new("RGB", (face_size * 4, face_size))
    tiles = [resized, ImageOps.mirror(resized), resized, ImageOps.mirror(resized)]
    x = 0
    for tile in tiles:
        if tile.width < face_size:
            tile = ImageOps.fit(tile, (face_size, face_size), RESAMPLE)
        crop = ImageOps.fit(tile, (face_size, face_size), RESAMPLE)
        strip.paste(crop, (x, 0))
        x += face_size

    horizontal = [strip.crop((i*face_size, 0, (i+1)*face_size, face_size)) for i in range(4)]

    atmospheric = ImageOps.fit(base, (face_size, face_size), RESAMPLE)
    atmospheric = atmospheric.filter(ImageFilter.GaussianBlur(radius=max(6, face_size // 40)))
    atmospheric = ImageEnhance.Brightness(atmospheric).enhance(0.72)

    up = atmospheric.copy()
    down = ImageOps.flip(atmospheric)
    return [horizontal[0], horizontal[1], horizontal[2], horizontal[3], up, down]


def panorama_faces(img: Image.Image, face_size: int = 512):
    w, h = img.size

    # 6-square horizontal strip.
    if w == h * 6:
        return [img.crop((i*h, 0, (i+1)*h, h)).resize((face_size, face_size), RESAMPLE) for i in range(6)]

    # 3x2 grid of six square faces, interpreted row-major.
    if w % 3 == 0 and h % 2 == 0 and w // 3 == h // 2:
        s = w // 3
        faces = []
        for row in range(2):
            for col in range(3):
                faces.append(img.crop((col*s, row*s, (col+1)*s, (row+1)*s)).resize((face_size, face_size), RESAMPLE))
        return faces[:6]

    # Conventional 4x3 cubemap cross:
    #        up
    # left front right back
    #       down
    if w % 4 == 0 and h % 3 == 0 and w // 4 == h // 3:
        s = w // 4
        front = img.crop((s, s, 2*s, 2*s))
        right = img.crop((2*s, s, 3*s, 2*s))
        back = img.crop((3*s, s, 4*s, 2*s))
        left = img.crop((0, s, s, 2*s))
        up = img.crop((s, 0, 2*s, s))
        down = img.crop((s, 2*s, 2*s, 3*s))
        return [front, right, back, left, up, down]

    # 2:1 equirectangular panorama.
    if abs((w / h) - 2.0) < 0.08:
        return equirect_to_faces(img, face_size)

    # Ordinary artwork: use an atmospheric pseudo-panorama.
    return flat_image_to_faces(img, face_size)


def build_panorama():
    img = Image.open(PANORAMA_SOURCE).convert("RGB")
    PANORAMA_DIR.mkdir(parents=True, exist_ok=True)
    faces = panorama_faces(img, 512)
    for i, face in enumerate(faces):
        save_png(face.convert("RGB"), PANORAMA_DIR / f"panorama_{i}.png")

    # Keep the artwork visible: a fully transparent overlay.
    overlay = Image.new("RGBA", (16, 128), (255, 255, 255, 0))
    save_png(overlay, PANORAMA_DIR / "panorama_overlay.png")


def build_join_loading_screen():
    GABCON_TEX.mkdir(parents=True, exist_ok=True)
    GABCON_FONT.mkdir(parents=True, exist_ok=True)

    loading = Image.open(LOADING_SOURCE).convert("RGBA")
    save_png(loading, GABCON_TEX / "loading_screen.png")

    font = {
        "providers": [
            {
                "type": "bitmap",
                "file": "gabcon:font/loading_screen.png",
                "ascent": 135,
                "height": 270,
                "chars": ["\ue100"]
            }
        ]
    }
    (GABCON_FONT / "loading.json").write_text(
        json.dumps(font, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8"
    )

    start = DP / "data/gabcon/function/loading/start.mcfunction"
    text = start.read_text(encoding="utf-8")
    old = 'title @s title {"text":"\\ue001","color":"white"}'
    new = 'title @s title {"text":"\\ue100","font":"gabcon:loading","color":"white"}'
    if old in text:
        text = text.replace(old, new)
    elif new not in text:
        raise RuntimeError("Could not find Gab Con loading title command to update.")
    start.write_text(text, encoding="utf-8")


def build_transparent_gui():
    # Inventory/container interfaces: semi-transparent, keeping enough opacity
    # for slots and labels to remain readable.
    for src in sorted(CONTAINER_DIR.rglob("*.png")):
        rel = src.relative_to(CONTAINER_DIR)
        restore_and_transparent(src, BASE_CONTAINER / rel, 0.62)

    # HUD: only panel/background pieces become translucent. Hearts, hunger,
    # armor, crosshair and progress fills stay crisp.
    hud_opacity = {
        "hotbar.png": 0.55,
        "hotbar_offhand_left.png": 0.55,
        "hotbar_offhand_right.png": 0.55,
        "hotbar_selection.png": 0.78,
        "effect_background.png": 0.55,
        "effect_background_ambient.png": 0.55,
        "experience_bar_background.png": 0.62,
        "jump_bar_background.png": 0.62,
        "hotbar_attack_indicator_background.png": 0.62,
        "crosshair_attack_indicator_background.png": 0.62,
    }
    for name, opacity in hud_opacity.items():
        src = HUD_DIR / name
        if src.exists():
            restore_and_transparent(src, BASE_HUD / name, opacity)


def main():
    if not PANORAMA_SOURCE.exists():
        raise SystemExit(f"Missing {PANORAMA_SOURCE.name}")
    if not LOADING_SOURCE.exists():
        raise SystemExit(f"Missing {LOADING_SOURCE.name}")

    p = Image.open(PANORAMA_SOURCE)
    l = Image.open(LOADING_SOURCE)
    print(f"Panorama source: {p.size[0]}x{p.size[1]}")
    print(f"Join loading source: {l.size[0]}x{l.size[1]}")

    build_panorama()
    build_join_loading_screen()
    build_transparent_gui()
    print("Gab Con visuals generated successfully.")


if __name__ == "__main__":
    main()
