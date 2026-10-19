import os
from PIL import Image

block_dir = "src/main/resources/assets/obesecat/textures/block"
item_dir = "src/main/resources/assets/obesecat/textures/item"
os.makedirs(block_dir, exist_ok=True)
os.makedirs(item_dir, exist_ok=True)

# -------------------------------------------------------------
# 1. OVERDRIVE ROD (16x16)
# Double helix of Blaze (Fire) & Breeze (Cyan wind) locked
# by dark Trinitite/Sculk rings with an acoustic shockwave tip
# -------------------------------------------------------------
rod = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

# Colors:
fire_bright = (255, 200, 50, 255)
fire_mid = (255, 110, 10, 255)
fire_dark = (180, 50, 5, 255)

wind_bright = (190, 245, 255, 255)
wind_mid = (56, 189, 248, 255)
wind_dark = (14, 116, 144, 255)

ring_dark = (10, 25, 22, 255)      # Sculk alloy
ring_green = (34, 197, 94, 255)    # Trinitite resonance
tip_glow = (255, 255, 255, 255)    # Hot sonic core

# Diagonal trajectory from (2, 13) to (13, 2)
# Pixels along the shaft:
# (2, 13) Base
rod.putpixel((2, 13), ring_dark); rod.putpixel((3, 13), ring_green)
rod.putpixel((2, 12), ring_green); rod.putpixel((3, 12), ring_dark)

# (4, 11), (5, 10) Fire helix crossing wind
rod.putpixel((3, 11), fire_dark); rod.putpixel((4, 11), fire_mid); rod.putpixel((5, 11), wind_dark)
rod.putpixel((4, 10), fire_bright); rod.putpixel((5, 10), wind_mid); rod.putpixel((6, 10), wind_bright)

# (6, 9), (7, 8) Central Trinitite harmonic containment ring
rod.putpixel((5, 9), ring_dark); rod.putpixel((6, 9), ring_green); rod.putpixel((7, 9), ring_dark)
rod.putpixel((6, 8), ring_green); rod.putpixel((7, 8), ring_dark); rod.putpixel((8, 8), ring_green)

# (8, 7), (9, 6) Wind helix crossing fire
rod.putpixel((7, 7), wind_dark); rod.putpixel((8, 7), wind_mid); rod.putpixel((9, 7), fire_bright)
rod.putpixel((8, 6), wind_bright); rod.putpixel((9, 6), fire_mid); rod.putpixel((10, 6), fire_dark)

# (10, 5), (11, 4) Secondary containment ring
rod.putpixel((9, 5), ring_dark); rod.putpixel((10, 5), ring_green); rod.putpixel((11, 5), ring_dark)
rod.putpixel((10, 4), ring_green); rod.putpixel((11, 4), ring_dark); rod.putpixel((12, 4), ring_green)

# (12, 3), (13, 2) Radiant sonic shockwave tip
rod.putpixel((11, 3), fire_bright); rod.putpixel((12, 3), tip_glow); rod.putpixel((13, 3), wind_bright)
rod.putpixel((12, 2), tip_glow); rod.putpixel((13, 2), tip_glow); rod.putpixel((14, 2), wind_bright)
rod.putpixel((13, 1), tip_glow)

rod.save(os.path.join(item_dir, "overdrive_rod.png"))
print("Saved overdrive_rod.png")


# -------------------------------------------------------------
# 2. OVERDRIVE POWDER (16x16)
# Radiant pulverized dust with embers, sculk acoustic sparks,
# and emerald Trinitite granules
# -------------------------------------------------------------
powder = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

# Coordinates of the powder mound
pixels = [
    # Top wisps & sparks
    (7, 2, tip_glow), (8, 3, wind_mid), (5, 4, fire_bright), (10, 4, ring_green),
    (6, 5, wind_bright), (7, 5, fire_mid), (8, 5, ring_dark), (9, 5, fire_bright),
    # Upper mound
    (4, 6, fire_dark), (5, 6, ring_green), (6, 6, fire_bright), (7, 6, tip_glow), (8, 6, wind_bright), (9, 6, wind_mid), (10, 6, ring_green), (11, 6, fire_mid),
    # Center mass
    (3, 7, wind_dark), (4, 7, fire_mid), (5, 7, ring_dark), (6, 7, fire_bright), (7, 7, tip_glow), (8, 7, ring_green), (9, 7, wind_bright), (10, 7, fire_mid), (11, 7, ring_dark), (12, 7, wind_mid),
    (2, 8, fire_dark), (3, 8, ring_green), (4, 8, tip_glow), (5, 8, fire_bright), (6, 8, ring_dark), (7, 8, wind_bright), (8, 8, ring_green), (9, 8, fire_mid), (10, 8, tip_glow), (11, 8, wind_mid), (12, 8, ring_dark), (13, 8, fire_dark),
    # Base spread
    (2, 9, ring_dark), (3, 9, wind_mid), (4, 9, fire_mid), (5, 9, ring_green), (6, 9, fire_bright), (7, 9, ring_dark), (8, 9, wind_bright), (9, 9, ring_green), (10, 9, fire_bright), (11, 9, ring_dark), (12, 9, wind_dark), (13, 9, ring_green),
    (1, 10, ring_dark), (2, 10, fire_mid), (3, 10, ring_dark), (4, 10, wind_bright), (5, 10, ring_green), (6, 10, fire_bright), (7, 10, tip_glow), (8, 10, fire_mid), (9, 10, ring_dark), (10, 10, wind_mid), (11, 10, ring_green), (12, 10, fire_dark), (13, 10, ring_dark), (14, 10, wind_dark),
    # Flat foundation
    (2, 11, ring_dark), (3, 11, ring_green), (4, 11, fire_dark), (5, 11, ring_dark), (6, 11, wind_dark), (7, 11, ring_green), (8, 11, fire_mid), (9, 11, ring_dark), (10, 11, wind_dark), (11, 11, ring_dark), (12, 11, ring_green), (13, 11, ring_dark),
    (3, 12, ring_dark), (4, 12, ring_dark), (5, 12, ring_dark), (6, 12, ring_dark), (7, 12, ring_dark), (8, 12, ring_dark), (9, 12, ring_dark), (10, 12, ring_dark), (11, 12, ring_dark), (12, 12, ring_dark)
]

for x, y, col in pixels:
    powder.putpixel((x, y), col)

powder.save(os.path.join(item_dir, "overdrive_powder.png"))
print("Saved overdrive_powder.png")


# -------------------------------------------------------------
# 3. ECHOING BLAST CHAMBER BLOCK TEXTURES
# -------------------------------------------------------------
c_metal_dark = (22, 28, 32, 255)
c_metal_mid = (36, 44, 50, 255)
c_metal_light = (55, 66, 75, 255)
c_bolt = (80, 95, 105, 255)
c_trinitite = (34, 197, 94, 255)
c_sculk = (14, 116, 144, 255)
c_glass_off = (16, 26, 30, 255)
c_glass_frame = (28, 48, 54, 255)

# --- SIDE TEXTURE ---
side = Image.new("RGBA", (16, 16))
for y in range(16):
    for x in range(16):
        # Outer border
        if x in (0, 15) or y in (0, 15):
            side.putpixel((x, y), c_metal_dark)
        elif (x in (1, 14) and y in (1, 14)):
            side.putpixel((x, y), c_bolt) # Corner bolts
        elif x in (3, 12):
            side.putpixel((x, y), c_trinitite if y in (6, 7, 8, 9) else c_metal_dark) # Vertical Trinitite conduit
        elif y in (4, 11):
            side.putpixel((x, y), c_metal_dark) # Horizontal plate seams
        elif 5 <= x <= 10 and 5 <= y <= 10:
            # Central sculk acoustic resonator plate
            if x in (5, 10) or y in (5, 10):
                side.putpixel((x, y), c_sculk)
            elif (x + y) % 2 == 0:
                side.putpixel((x, y), (10, 45, 55, 255))
            else:
                side.putpixel((x, y), (18, 70, 85, 255))
        else:
            base = c_metal_mid if (x + y) % 2 == 0 else c_metal_light
            side.putpixel((x, y), base)

side.save(os.path.join(block_dir, "echoing_blast_chamber_side.png"))

# --- TOP TEXTURE ---
top = Image.new("RGBA", (16, 16))
for y in range(16):
    for x in range(16):
        dx = abs(x - 7.5)
        dy = abs(y - 7.5)
        dist = (dx*dx + dy*dy) ** 0.5
        if x in (0, 15) or y in (0, 15):
            top.putpixel((x, y), c_metal_dark)
        elif (x in (1, 14) and y in (1, 14)):
            top.putpixel((x, y), c_bolt)
        elif dist > 5.5:
            top.putpixel((x, y), c_metal_mid if (x+y)%2==0 else c_metal_light)
        elif dist > 3.5:
            # Concentric acoustic baffle ring
            top.putpixel((x, y), c_sculk if (x*y)%3==0 else c_metal_dark)
        elif dist > 1.8:
            top.putpixel((x, y), c_trinitite if (x+y)%2==0 else (20, 120, 50, 255))
        else:
            top.putpixel((x, y), (8, 25, 30, 255)) # Central dampener hole

top.save(os.path.join(block_dir, "echoing_blast_chamber_top.png"))

# --- FRONT (OFF) TEXTURE ---
front_off = Image.new("RGBA", (16, 16))
for y in range(16):
    for x in range(16):
        if x in (0, 15) or y in (0, 15):
            front_off.putpixel((x, y), c_metal_dark)
        elif (x in (1, 14) and y in (1, 14)):
            front_off.putpixel((x, y), c_bolt)
        elif x in (2, 13) and 3 <= y <= 12:
            front_off.putpixel((x, y), c_trinitite if y in (7, 8) else (20, 80, 40, 255))
        elif 4 <= x <= 11 and 4 <= y <= 11:
            # Reinforced tinted viewport
            if x in (4, 11) or y in (4, 11):
                front_off.putpixel((x, y), c_glass_frame)
            elif (x, y) in ((7, 6), (8, 6)):
                front_off.putpixel((x, y), (140, 60, 15, 255)) # Upper Blaze clamp
            elif (x, y) in ((7, 9), (8, 9)):
                front_off.putpixel((x, y), (25, 90, 115, 255)) # Lower Breeze clamp
            else:
                front_off.putpixel((x, y), c_glass_off)
        else:
            front_off.putpixel((x, y), c_metal_mid if (x+y)%2==0 else c_metal_light)

front_off.save(os.path.join(block_dir, "echoing_blast_chamber_front.png"))

# --- FRONT (ON) TEXTURE ---
# Blinding active harmonic singularity with superheated Blaze fire and Breeze wind
front_on = front_off.copy()
for y in range(5, 11):
    for x in range(5, 11):
        dy = y - 7.5
        dx = x - 7.5
        dist = (dx*dx + dy*dy) ** 0.5
        
        if dist < 1.4:
            # Unstable white-hot harmonic fusion core!
            front_on.putpixel((x, y), (255, 255, 255, 255))
        elif y <= 7:
            # Top half: Blaze superheat
            if dist < 2.5:
                front_on.putpixel((x, y), (255, 220, 80, 255))
            else:
                front_on.putpixel((x, y), (255, 100, 15, 255))
        else:
            # Bottom half: Breeze turbulence
            if dist < 2.5:
                front_on.putpixel((x, y), (180, 245, 255, 255))
            else:
                front_on.putpixel((x, y), (56, 189, 248, 255))

# Lit Trinitite conduits glow neon green
for y in range(3, 13):
    front_on.putpixel((2, y), (57, 255, 20, 255) if y in (6, 7, 8, 9) else (34, 197, 94, 255))
    front_on.putpixel((13, y), (57, 255, 20, 255) if y in (6, 7, 8, 9) else (34, 197, 94, 255))

front_on.save(os.path.join(block_dir, "echoing_blast_chamber_front_on.png"))
print("Saved all echoing_blast_chamber block textures!")
