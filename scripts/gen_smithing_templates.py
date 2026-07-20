#!/usr/bin/env python3
"""Generate 16x16 PNG textures for the three Progression Overhaul smithing upgrade templates."""

import struct
import zlib
import os

OUT_DIR = os.path.join(
    os.path.dirname(__file__),
    "../src/main/resources/assets/progressionmod/textures/item",
)


def make_png(pixels: list) -> bytes:
    """Encode a 16x16 RGBA pixel grid to PNG bytes (no external deps)."""
    def pack_row(row):
        return b'\x00' + bytes(c for px in row for c in px)

    raw = b''.join(pack_row(pixels[y]) for y in range(16))
    compressed = zlib.compress(raw, 9)

    def chunk(ctype: bytes, data: bytes) -> bytes:
        return (struct.pack('>I', len(data))
                + ctype + data
                + struct.pack('>I', zlib.crc32(ctype + data) & 0xFFFFFFFF))

    ihdr = struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)
    return (b'\x89PNG\r\n\x1a\n'
            + chunk(b'IHDR', ihdr)
            + chunk(b'IDAT', compressed)
            + chunk(b'IEND', b''))


T = (0, 0, 0, 0)


def px(r, g, b, a=255):
    return (r, g, b, a)


def darken(col, factor=0.55):
    return px(int(col[0] * factor), int(col[1] * factor), int(col[2] * factor))


def lighten(col, factor=1.45):
    return px(min(255, int(col[0] * factor)), min(255, int(col[1] * factor)), min(255, int(col[2] * factor)))


def build_template(frame_col, center_col):
    """
    Builds a 16x16 smithing-template-style icon.
    frame_col  = (r,g,b) for the outer ring / 'from' tier
    center_col = (r,g,b) for the inner diamond / 'to' tier
    """
    F  = px(*frame_col)           # frame
    C  = px(*center_col)          # center
    FL = lighten(frame_col)       # frame highlight
    FD = darken(frame_col)        # frame shadow
    CL = lighten(center_col)      # center highlight
    CD = darken(center_col)       # center shadow
    O  = px(10, 10, 10)          # outline

    # fmt: off
    grid = [
      # 0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
        [T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T ],
        [T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T ],
        [T,  T,  T,  O,  O,  O,  O,  O,  O,  O,  O,  O,  O,  T,  T,  T ],
        [T,  T,  O,  FL, FL, F,  F,  F,  F,  F,  F,  F,  FD, O,  T,  T ],
        [T,  T,  O,  FL, F,  C,  C,  C,  C,  C,  C,  F,  FD, O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  C,  C,  C,  C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  CL, C,  C,  C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  C,  CL, CL, C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  C,  CL, CL, C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  C,  C,  C,  C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  CL, C,  C,  C,  C,  C,  C,  CD, F,  O,  T,  T ],
        [T,  T,  O,  F,  F,  C,  C,  C,  C,  C,  C,  F,  FD, O,  T,  T ],
        [T,  T,  O,  FL, FL, F,  F,  F,  F,  F,  F,  F,  FD, O,  T,  T ],
        [T,  T,  T,  O,  O,  O,  O,  O,  O,  O,  O,  O,  O,  T,  T,  T ],
        [T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T ],
        [T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T ],
    ]
    # fmt: on
    return grid


# ── Color palettes ─────────────────────────────────────────────────────────────
# Diamond blue   → Amethyst purple
DIAMOND_COL   = (79, 199, 228)
AMETHYST_COL  = (113, 36, 195)

# Amethyst purple → Netherite dark
NETHERITE_COL = (55, 50, 62)

# Netherite dark  → Endium void-purple
ENDIUM_COL    = (28, 12, 58)

templates = [
    ("diamond_amethyst_upgrade",  DIAMOND_COL,  AMETHYST_COL),
    ("amethyst_netherite_upgrade", AMETHYST_COL, NETHERITE_COL),
    ("netherite_endium_upgrade",   NETHERITE_COL, ENDIUM_COL),
]

os.makedirs(OUT_DIR, exist_ok=True)
for name, frame, center in templates:
    grid = build_template(frame, center)
    data = make_png(grid)
    path = os.path.join(OUT_DIR, f"{name}.png")
    with open(path, 'wb') as f:
        f.write(data)
    print(f"Written: {path}")

print("Done.")
