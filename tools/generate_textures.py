#!/usr/bin/env python3
"""Generate Soulkeeper's pixel-art textures without external dependencies."""

import math
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/soulkeeper"


def write_png(path: Path, width: int, height: int, rows: list[list[tuple[int, int, int, int]]]) -> None:
    raw = b"".join(b"\x00" + bytes(channel for pixel in row for channel in pixel) for row in rows)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def blank(width: int, height: int, color=(0, 0, 0, 0)) -> list[list[tuple[int, int, int, int]]]:
    return [[color for _ in range(width)] for _ in range(height)]


def put(rows, x, y, color):
    if 0 <= y < len(rows) and 0 <= x < len(rows[0]):
        rows[y][x] = color


def rect(rows, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(rows, x, y, color)


def anchor_texture():
    rows = blank(16, 16)
    frame_outer = (49, 35, 60, 255)
    frame_inner = (74, 55, 91, 255)
    panel_top = (25, 29, 48, 255)
    panel_bottom = (18, 21, 36, 255)

    rect(rows, 0, 0, 15, 15, frame_outer)
    rect(rows, 1, 1, 14, 14, frame_inner)
    rect(rows, 2, 2, 13, 13, panel_top)

    for y in range(3, 14):
        shade = (25 - (y - 3), 29 - (y - 3), 48 - (y - 3), 255)
        for x in range(3, 13):
            put(rows, x, y, shade)

    # Rivets in the corners of the frame.
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        put(rows, x, y, (190, 150, 235, 255))

    # Outer rune ring: a diamond of purple soul-fire.
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = abs(x - cx) + abs(y - cy)
            if 3.5 <= d <= 4.5:
                put(rows, x, y, (99, 67, 168, 255))
            elif 2.5 <= d < 3.5:
                put(rows, x, y, (55, 40, 100, 255))

    # Cardinal sparks on the rune ring.
    for x, y in ((7, 2), (8, 2), (7, 13), (8, 13), (2, 7), (2, 8), (13, 7), (13, 8)):
        put(rows, x, y, (103, 235, 255, 255))

    # Glowing soul core.
    core = [
        (6, 6, (118, 249, 255, 255)),
        (7, 6, (210, 255, 255, 255)),
        (8, 6, (118, 249, 255, 255)),
        (9, 6, (60, 151, 230, 255)),
        (5, 7, (60, 151, 230, 255)),
        (6, 7, (172, 255, 255, 255)),
        (7, 7, (245, 255, 255, 255)),
        (8, 7, (172, 255, 255, 255)),
        (9, 7, (118, 249, 255, 255)),
        (10, 7, (60, 151, 230, 255)),
        (5, 8, (60, 151, 230, 255)),
        (6, 8, (118, 249, 255, 255)),
        (7, 8, (172, 255, 255, 255)),
        (8, 8, (118, 249, 255, 255)),
        (9, 8, (60, 151, 230, 255)),
        (6, 9, (60, 151, 230, 255)),
        (7, 9, (118, 249, 255, 255)),
        (8, 9, (60, 151, 230, 255)),
    ]
    for x, y, color in core:
        put(rows, x, y, color)

    # Tiny soul wisps in the corners of the panel.
    for x, y in ((3, 4), (12, 4), (4, 12), (11, 12), (3, 9), (12, 9)):
        put(rows, x, y, (78, 60, 122, 255))

    write_png(ASSETS / "textures/block/soul_anchor.png", 16, 16, rows)
    return rows


def compass_texture():
    rows = blank(16, 16)
    cx, cy = 7.5, 7.5

    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= 7.1:
                put(rows, x, y, (30, 35, 55, 255))
            if d <= 6.6:
                put(rows, x, y, (94, 107, 136, 255))
            if d <= 5.9:
                put(rows, x, y, (16, 20, 35, 255))
            if 4.9 <= d <= 5.4:
                put(rows, x, y, (46, 55, 86, 255))

    # Tick marks.
    for x in range(5, 11):
        put(rows, x, 2, (145, 158, 196, 255))
        put(rows, x, 13, (145, 158, 196, 255))
    for y in range(5, 11):
        put(rows, 2, y, (145, 158, 196, 255))
        put(rows, 13, y, (145, 158, 196, 255))

    # Needle: north is cyan, south is purple.
    needle = [
        (7, 3, (118, 249, 255, 255)),
        (8, 3, (60, 151, 230, 255)),
        (7, 4, (172, 255, 255, 255)),
        (8, 4, (118, 249, 255, 255)),
        (7, 5, (118, 249, 255, 255)),
        (8, 5, (60, 151, 230, 255)),
        (7, 6, (60, 151, 230, 255)),
        (8, 6, (60, 151, 230, 255)),
        (7, 9, (78, 60, 122, 255)),
        (8, 9, (55, 40, 100, 255)),
        (7, 10, (99, 67, 168, 255)),
        (8, 10, (78, 60, 122, 255)),
        (7, 11, (99, 67, 168, 255)),
        (8, 11, (78, 60, 122, 255)),
        (7, 12, (55, 40, 100, 255)),
        (8, 12, (40, 28, 75, 255)),
    ]
    for x, y, color in needle:
        put(rows, x, y, color)

    # Soul gem at the pivot.
    rect(rows, 7, 7, 8, 8, (245, 255, 255, 255))
    put(rows, 7, 7, (255, 255, 255, 255))
    put(rows, 8, 8, (172, 255, 255, 255))

    # A small binding chain/loop on top.
    put(rows, 6, 0, (94, 107, 136, 255))
    put(rows, 7, 0, (145, 158, 196, 255))
    put(rows, 8, 0, (145, 158, 196, 255))
    put(rows, 9, 0, (94, 107, 136, 255))
    put(rows, 5, 1, (94, 107, 136, 255))
    put(rows, 10, 1, (94, 107, 136, 255))

    write_png(ASSETS / "textures/item/soul_compass.png", 16, 16, rows)


def icon_texture(anchor_rows):
    size = 128
    rows = blank(size, size)
    for y in range(size):
        for x in range(size):
            dx = (x - size / 2) / (size / 2)
            dy = (y - size / 2) / (size / 2)
            d = min(1.0, math.hypot(dx, dy))
            glow = max(0.0, 1.0 - d)
            r = int(12 + 43 * glow)
            g = int(8 + 31 * glow)
            b = int(24 + 70 * glow)
            rows[y][x] = (r, g, b, 255)

    # Subtle soul sparks on the background.
    for i in range(46):
        angle = i * 2.39996
        radius = 26 + (i * 17) % 44
        x = int(64 + math.cos(angle) * radius)
        y = int(64 + math.sin(angle) * radius)
        strength = 70 + (i * 29) % 110
        put(rows, x, y, (strength, strength + 30, 255, 255))

    scale = 6
    offset = (size - 16 * scale) // 2
    for y in range(16):
        for x in range(16):
            color = anchor_rows[y][x]
            for py in range(scale):
                for px in range(scale):
                    put(rows, offset + x * scale + px, offset + y * scale + py, color)

    # Soft inner rim so the block sits on the background.
    for i in range(size):
        put(rows, offset - 1, i, (88, 60, 160, 255))
        put(rows, offset + 16 * scale, i, (88, 60, 160, 255))
        put(rows, i, offset - 1, (88, 60, 160, 255))
        put(rows, i, offset + 16 * scale, (88, 60, 160, 255))

    write_png(ASSETS / "icon.png", size, size, rows)


if __name__ == "__main__":
    anchor = anchor_texture()
    compass_texture()
    icon_texture(anchor)
    print("Generated textures in", ASSETS)
