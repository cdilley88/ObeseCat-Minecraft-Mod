import os
from PIL import Image

# 16x16 Pixel Art for BIG Fat Man Boom Stick
# Diagonal rod with an oversized, super-heavy tactical nuclear implosion warhead.
# Colors:
# Shaft: Dark tungsten steel, obsidian grip, hazard yellow warning collars
# Warhead: Heavy olive-drab/gunmetal bulbous core, bright hazard yellow/black stripes, glowing radioactive plutonium green tip and vents

PALETTE = {
    ' ': (0, 0, 0, 0),
    'G': (20, 20, 25, 255),       # Deep Gunmetal outline
    'S': (45, 50, 58, 255),       # Tungsten shaft base
    'H': (75, 82, 95, 255),       # Steel highlight
    'O': (55, 68, 50, 255),       # Olive Drab casing
    'D': (38, 48, 35, 255),       # Dark Olive shadow
    'L': (85, 105, 78, 255),      # Olive highlight
    'Y': (255, 204, 0, 255),      # Hazard Yellow
    'K': (25, 25, 25, 255),       # Hazard Black
    'P': (57, 255, 88, 255),      # Glowing Plutonium Green
    'R': (140, 255, 160, 255),    # White-green core radiation
    'W': (240, 255, 240, 255),    # Brightest glint
    'C': (180, 50, 50, 255),      # Red arming LED / collar
}

# 16 rows of 16 characters
PIXELS = [
    "           GWWG ",  # 0: Radioactive warhead tip
    "          GRPPG ",  # 1: Glowing plutonium core
    "         GYKYYG ",  # 2: Hazard striped detonation band
    "        GOLLOYKG",  # 3: Bulbous heavy implosion shell
    "       GDDOOLLG ",  # 4: Olive casing armor
    "      GGDOOLLG  ",  # 5: Heavy containment collar
    "     GCYDDDG    ",  # 6: Arming circuit / red LED
    "    GSHCGS      ",  # 7: Heavy reinforced collar
    "   GSHSG        ",  # 8: Tungsten shaft
    "  GSHSG         ",  # 9: Tungsten shaft
    " GSHSG          ",  # 10: Grip segment
    "GSHSG           ",  # 11: Grip segment
    "GKYG            ",  # 12: Hazard warning base collar
    "GSG             ",  # 13: Pommel base
    "GG              ",  # 14: Counterweight
    "                "   # 15
]

img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
for y in range(16):
    for x in range(16):
        ch = PIXELS[y][x]
        img.putpixel((x, y), PALETTE[ch])

out_path = 'src/main/resources/assets/obesecat/textures/item/big_fat_man_boom_stick.png'
os.makedirs(os.path.dirname(out_path), exist_ok=True)
img.save(out_path)
print("Saved BIG Fat Man Boom Stick texture to:", out_path)
