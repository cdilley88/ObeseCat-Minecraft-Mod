import os
from PIL import Image

output_dir = "src/main/resources/assets/obesecat/textures"
block_dir = os.path.join(output_dir, "block")
item_dir = os.path.join(output_dir, "item")
os.makedirs(block_dir, exist_ok=True)
os.makedirs(item_dir, exist_ok=True)

# -----------------------------------------------------------------
# 1. TABBYRIUM SPORE POD (16x80, 5 frames of 16x16)
# -----------------------------------------------------------------
# Frame colors ranging from resting to peak pulse
pod_frames = []

for frame_idx in range(5):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # Pulse factors
    pulse = [0.4, 0.7, 1.0, 0.75, 0.5][frame_idx]
    
    # Outer husk: bio-fused mutated bark/chitin
    husk_dark = (14, 26, 18, 255)
    husk_mid = (22, 42, 28, 255)
    husk_edge = (32, 60, 40, 255)
    
    # Inner glow colors interpolated by pulse
    glow_core = (
        int(140 + 115 * pulse),
        255,
        int(160 + 95 * pulse),
        255
    )
    glow_bright = (
        int(40 + 50 * pulse),
        int(200 + 55 * pulse),
        int(50 + 60 * pulse),
        255
    )
    glow_mid = (
        int(20 + 30 * pulse),
        int(140 + 60 * pulse),
        int(30 + 40 * pulse),
        255
    )
    glow_deep = (
        int(10 + 15 * pulse),
        int(80 + 40 * pulse),
        int(20 + 20 * pulse),
        255
    )
    
    for y in range(16):
        for x in range(16):
            # Distance from center (7.5, 7.5)
            dx = abs(x - 7.5)
            dy = abs(y - 7.5)
            dist = (dx*dx + dy*dy) ** 0.5
            
            # Corner rivets / outer husk
            if dist > 6.5:
                # Husk pattern with subtle corner plates
                if (x in (0, 15) and y in (0, 15)) or (x in (1, 14) and y in (1, 14)):
                    img.putpixel((x, y), husk_dark)
                else:
                    img.putpixel((x, y), husk_edge if (x+y)%2 == 0 else husk_mid)
            elif dist > 4.5:
                # Tabby stripe pattern over outer vein membrane
                if (x + y*2) % 4 == 0 or (x - y) % 5 == 0:
                    img.putpixel((x, y), husk_dark) # Dark tabby vein
                else:
                    img.putpixel((x, y), glow_deep)
            elif dist > 2.5:
                # Mid-membrane glow
                if (x == 7 or x == 8) and (y in (3, 4, 11, 12)):
                    img.putpixel((x, y), husk_dark) # Vertical feline slit anchor
                else:
                    img.putpixel((x, y), glow_mid if dist > 3.5 else glow_bright)
            else:
                # Central bio-eye / nucleus
                if (x in (7, 8) and y in (6, 7, 8, 9)):
                    # Feline vertical slitted pupil core!
                    if y in (7, 8) and pulse > 0.8:
                        img.putpixel((x, y), (255, 255, 255, 255)) # Hot white iris flash
                    else:
                        img.putpixel((x, y), glow_core)
                else:
                    img.putpixel((x, y), glow_bright)
    pod_frames.append(img)

# Combine 5 frames into 16x80 vertical strip
spore_pod_strip = Image.new("RGBA", (16, 80))
for i, frame in enumerate(pod_frames):
    spore_pod_strip.paste(frame, (0, i * 16))
spore_pod_strip.save(os.path.join(block_dir, "tabbyrium_spore_pod.png"))
print("Saved tabbyrium_spore_pod.png (16x80)")


# -----------------------------------------------------------------
# 2. TABBYRIUM STALK (SIDE & TOP)
# -----------------------------------------------------------------
# Side: Mutated dark wood bark with vivid glowing tabby veins
stalk_side = Image.new("RGBA", (16, 16))
bark_1 = (18, 26, 20, 255)
bark_2 = (26, 38, 28, 255)
bark_3 = (34, 50, 38, 255)
vein_glow = (57, 255, 20, 255)
vein_mid = (34, 197, 94, 255)
vein_dark = (16, 100, 45, 255)

for y in range(16):
    for x in range(16):
        # Vertical undulating tabby stripe wave
        wave = int(1.5 * ((y % 6) - 2.5))
        is_tabby_vein = ((x + wave) % 5 == 0)
        is_sub_vein = ((x - wave) % 7 == 0 and y % 3 == 0)
        
        if is_tabby_vein:
            if y % 4 == 0:
                stalk_side.putpixel((x, y), vein_glow)
            elif y % 2 == 0:
                stalk_side.putpixel((x, y), vein_mid)
            else:
                stalk_side.putpixel((x, y), vein_dark)
        elif is_sub_vein:
            stalk_side.putpixel((x, y), vein_dark)
        else:
            base = bark_1 if (x * 3 + y * 7) % 5 < 2 else (bark_2 if (x + y) % 2 == 0 else bark_3)
            stalk_side.putpixel((x, y), base)

stalk_side.save(os.path.join(block_dir, "tabbyrium_stalk_side.png"))

# Top: Cross-section log with glowing radioactive heartwood
stalk_top = Image.new("RGBA", (16, 16))
for y in range(16):
    for x in range(16):
        dx = abs(x - 7.5)
        dy = abs(y - 7.5)
        dist = (dx*dx + dy*dy) ** 0.5
        
        if dist > 6.8:
            stalk_top.putpixel((x, y), bark_1) # Outer bark
        elif dist > 5.2:
            stalk_top.putpixel((x, y), bark_3 if (x+y)%2==0 else bark_2)
        elif dist > 3.8:
            stalk_top.putpixel((x, y), vein_dark if int(dist*2)%2==0 else bark_3)
        elif dist > 2.0:
            stalk_top.putpixel((x, y), vein_mid if (x+y)%3==0 else vein_dark)
        else:
            # Radiant radioactive feline heart
            stalk_top.putpixel((x, y), (134, 239, 172, 255) if dist < 1.2 else vein_glow)

stalk_top.save(os.path.join(block_dir, "tabbyrium_stalk_top.png"))
print("Saved tabbyrium_stalk textures")


# -----------------------------------------------------------------
# 3. TABBYRIUM PLANKS
# -----------------------------------------------------------------
planks = Image.new("RGBA", (16, 16))
p_seam = (10, 18, 12, 255)
p_dark = (25, 42, 30, 255)
p_mid = (35, 60, 42, 255)
p_light = (48, 80, 56, 255)
p_grain = (22, 120, 50, 255)

for y in range(16):
    is_seam_h = (y in (3, 7, 11, 15))
    plank_row = y // 4
    for x in range(16):
        # Vertical joint seams staggered per plank row
        is_seam_v = False
        if plank_row == 0 and x == 10: is_seam_v = True
        elif plank_row == 1 and x == 4: is_seam_v = True
        elif plank_row == 2 and x == 12: is_seam_v = True
        elif plank_row == 3 and x == 6: is_seam_v = True
        
        if is_seam_h or is_seam_v:
            planks.putpixel((x, y), p_seam)
        else:
            grain = (x * 7 + y * 3) % 7
            if grain == 0 and (x+y)%2 == 0:
                planks.putpixel((x, y), p_grain) # Subtle tabby green fleck
            elif grain < 2:
                planks.putpixel((x, y), p_dark)
            elif grain < 5:
                planks.putpixel((x, y), p_mid)
            else:
                planks.putpixel((x, y), p_light)

planks.save(os.path.join(block_dir, "tabbyrium_planks.png"))
print("Saved tabbyrium_planks.png")


# -----------------------------------------------------------------
# 4. TABBYRIUM BLOSSOM SAPLING (Proper transparency & plant structure)
# -----------------------------------------------------------------
sapling = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Colors:
c_root = (20, 32, 22, 255)
c_stem_dark = (14, 45, 20, 255)
c_stem_mid = (30, 110, 40, 255)
c_glow = (57, 255, 20, 255)
c_leaf = (74, 222, 128, 255)
c_core = (167, 243, 208, 255)
c_stripe = (10, 25, 12, 255)

# Roots (y=13..15)
sapling.putpixel((5, 15), c_root); sapling.putpixel((6, 15), c_root)
sapling.putpixel((9, 15), c_root); sapling.putpixel((10, 15), c_root)
sapling.putpixel((6, 14), c_root); sapling.putpixel((7, 14), c_stem_dark); sapling.putpixel((8, 14), c_stem_dark); sapling.putpixel((9, 14), c_root)
sapling.putpixel((7, 13), c_stem_mid); sapling.putpixel((8, 13), c_stem_dark)

# Stem & side branches (y=8..12)
sapling.putpixel((7, 12), c_stripe); sapling.putpixel((8, 12), c_stem_mid)
sapling.putpixel((5, 11), c_leaf); sapling.putpixel((6, 11), c_stem_mid); sapling.putpixel((7, 11), c_stem_dark); sapling.putpixel((8, 11), c_stem_mid)
sapling.putpixel((4, 10), c_glow); sapling.putpixel((5, 10), c_leaf); sapling.putpixel((7, 10), c_stripe); sapling.putpixel((8, 10), c_stem_mid); sapling.putpixel((9, 10), c_stem_dark); sapling.putpixel((10, 10), c_leaf)
sapling.putpixel((7, 9), c_stem_mid); sapling.putpixel((8, 9), c_stem_dark); sapling.putpixel((11, 9), c_glow)
sapling.putpixel((7, 8), c_stripe); sapling.putpixel((8, 8), c_stem_mid)

# Pod Crown / Blossom Bud (y=2..7)
# Outer petals & tabby calyx
sapling.putpixel((6, 7), c_stem_dark); sapling.putpixel((7, 7), c_glow); sapling.putpixel((8, 7), c_glow); sapling.putpixel((9, 7), c_stem_dark)
sapling.putpixel((5, 6), c_stripe); sapling.putpixel((6, 6), c_leaf); sapling.putpixel((7, 6), c_core); sapling.putpixel((8, 6), c_leaf); sapling.putpixel((9, 6), c_leaf); sapling.putpixel((10, 6), c_stripe)
sapling.putpixel((5, 5), c_leaf); sapling.putpixel((6, 5), c_core); sapling.putpixel((7, 5), (255, 255, 255, 255)); sapling.putpixel((8, 5), c_core); sapling.putpixel((9, 5), c_leaf); sapling.putpixel((10, 5), c_leaf)
sapling.putpixel((5, 4), c_stripe); sapling.putpixel((6, 4), c_leaf); sapling.putpixel((7, 4), c_core); sapling.putpixel((8, 4), c_core); sapling.putpixel((9, 4), c_leaf); sapling.putpixel((10, 4), c_stripe)
sapling.putpixel((6, 3), c_glow); sapling.putpixel((7, 3), c_core); sapling.putpixel((8, 3), c_glow); sapling.putpixel((9, 3), c_glow)
sapling.putpixel((7, 2), c_glow); sapling.putpixel((8, 2), c_glow)

sapling.save(os.path.join(block_dir, "tabbyrium_blossom_sapling.png"))
sapling.save(os.path.join(item_dir, "tabbyrium_blossom_sapling.png"))
print("Saved tabbyrium_blossom_sapling textures")


# -----------------------------------------------------------------
# 5. TABBYRIUM CLUSTERS (STAGES 0, 1, 2, 3)
# -----------------------------------------------------------------
# Colors for Tiberium crystals with Tabby stripes:
cr_highlight = (187, 247, 208, 255) # Mint hot facet
cr_electric = (57, 255, 20, 255)   # Toxic C&C Green
cr_bright = (34, 197, 94, 255)     # Radiant Emerald
cr_mid = (22, 130, 55, 255)        # Deep Jade
cr_dark = (14, 75, 30, 255)        # Shadow facet
cr_tabby = (6, 22, 10, 255)        # Obsidian Tabby Stripe

# STAGE 0: Tiny crystal sprout (3-4px high)
st0 = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Sprout 1 (left)
st0.putpixel((5, 15), cr_tabby); st0.putpixel((6, 15), cr_dark)
st0.putpixel((5, 14), cr_bright); st0.putpixel((6, 14), cr_electric)
st0.putpixel((6, 13), cr_highlight)

# Sprout 2 (center, taller)
st0.putpixel((7, 15), cr_tabby); st0.putpixel((8, 15), cr_dark); st0.putpixel((9, 15), cr_tabby)
st0.putpixel((7, 14), cr_bright); st0.putpixel((8, 14), cr_electric); st0.putpixel((9, 14), cr_mid)
st0.putpixel((8, 13), cr_bright); st0.putpixel((8, 12), cr_highlight)

# Sprout 3 (right)
st0.putpixel((10, 15), cr_dark); st0.putpixel((11, 15), cr_tabby)
st0.putpixel((10, 14), cr_electric); st0.putpixel((11, 14), cr_mid)
st0.putpixel((10, 13), cr_highlight)
st0.save(os.path.join(block_dir, "tabbyrium_cluster_stage0.png"))

# STAGE 1: Small growing cluster (7-8px high)
st1 = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Base
for x in range(4, 12):
    st1.putpixel((x, 15), cr_tabby if x in (5, 8, 11) else cr_dark)

# Center spire (y=8..14)
for y in range(8, 15):
    st1.putpixel((7, y), cr_tabby if y % 3 == 0 else cr_bright)
    st1.putpixel((8, y), cr_electric if y % 2 == 0 else cr_highlight)
    st1.putpixel((9, y), cr_dark if y % 3 == 1 else cr_mid)
st1.putpixel((8, 8), cr_highlight)

# Left angled shard (y=11..14)
st1.putpixel((5, 14), cr_bright); st1.putpixel((6, 14), cr_electric)
st1.putpixel((4, 13), cr_tabby); st1.putpixel((5, 13), cr_bright); st1.putpixel((6, 13), cr_mid)
st1.putpixel((4, 12), cr_electric); st1.putpixel((5, 12), cr_highlight)
st1.putpixel((4, 11), cr_highlight)

# Right angled shard (y=10..14)
st1.putpixel((10, 14), cr_bright); st1.putpixel((11, 14), cr_dark)
st1.putpixel((10, 13), cr_electric); st1.putpixel((11, 13), cr_tabby)
st1.putpixel((10, 12), cr_highlight); st1.putpixel((11, 12), cr_bright)
st1.putpixel((11, 11), cr_electric)
st1.putpixel((11, 10), cr_highlight)
st1.save(os.path.join(block_dir, "tabbyrium_cluster_stage1.png"))

# STAGE 2: Medium multi-spire formation (11-12px high)
st2 = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for x in range(3, 13):
    st2.putpixel((x, 15), cr_tabby if x in (4, 7, 10) else cr_dark)
    st2.putpixel((x, 14), cr_tabby if x in (3, 6, 9, 12) else cr_mid)

# Tall central needle (y=4..13)
for y in range(5, 14):
    st2.putpixel((7, y), cr_tabby if y in (6, 9, 12) else cr_bright)
    st2.putpixel((8, y), cr_highlight if y % 2 == 0 else cr_electric)
    st2.putpixel((9, y), cr_tabby if y in (5, 8, 11) else cr_dark)
st2.putpixel((8, 4), cr_highlight)

# Secondary left needle (y=7..13)
st2.putpixel((5, 13), cr_tabby); st2.putpixel((6, 13), cr_bright)
st2.putpixel((4, 12), cr_bright); st2.putpixel((5, 12), cr_electric); st2.putpixel((6, 12), cr_dark)
st2.putpixel((4, 11), cr_tabby); st2.putpixel((5, 11), cr_bright)
st2.putpixel((4, 10), cr_electric); st2.putpixel((5, 10), cr_highlight)
st2.putpixel((3, 9), cr_bright); st2.putpixel((4, 9), cr_highlight)
st2.putpixel((3, 8), cr_highlight)
st2.putpixel((3, 7), cr_highlight)

# Secondary right needle (y=6..13)
st2.putpixel((10, 13), cr_bright); st2.putpixel((11, 13), cr_tabby)
st2.putpixel((10, 12), cr_dark); st2.putpixel((11, 12), cr_bright); st2.putpixel((12, 12), cr_tabby)
st2.putpixel((10, 11), cr_electric); st2.putpixel((11, 11), cr_tabby)
st2.putpixel((11, 10), cr_bright); st2.putpixel((12, 10), cr_electric)
st2.putpixel((11, 9), cr_highlight); st2.putpixel((12, 9), cr_bright)
st2.putpixel((12, 8), cr_electric)
st2.putpixel((12, 7), cr_highlight)
st2.putpixel((12, 6), cr_highlight)
st2.save(os.path.join(block_dir, "tabbyrium_cluster_stage2.png"))

# STAGE 3: Full mature crystal formation (15-16px high)
st3 = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Ground base
for x in range(2, 14):
    st3.putpixel((x, 15), cr_tabby if x in (3, 6, 9, 12) else cr_dark)
    st3.putpixel((x, 14), cr_tabby if x in (2, 5, 8, 11) else cr_mid)

# Main towering spire (y=0..13)
for y in range(2, 14):
    st3.putpixel((7, y), cr_tabby if y in (3, 6, 9, 12) else cr_bright)
    st3.putpixel((8, y), cr_highlight if y % 2 == 0 else cr_electric)
    st3.putpixel((9, y), cr_tabby if y in (2, 5, 8, 11) else cr_dark)
st3.putpixel((8, 1), cr_highlight)
st3.putpixel((8, 0), cr_highlight)

# Left massive crystal spire (y=3..13)
for y in range(4, 14):
    st3.putpixel((4, y), cr_tabby if y in (4, 7, 10, 13) else cr_bright)
    st3.putpixel((5, y), cr_electric if y % 2 == 1 else cr_highlight)
    st3.putpixel((6, y), cr_dark if y in (5, 8, 11) else cr_mid)
st3.putpixel((4, 3), cr_highlight)

# Far-left sharp angled horn (y=7..13)
st3.putpixel((2, 13), cr_tabby); st3.putpixel((3, 13), cr_dark)
st3.putpixel((2, 12), cr_bright); st3.putpixel((3, 12), cr_tabby)
st3.putpixel((1, 11), cr_tabby); st3.putpixel((2, 11), cr_electric)
st3.putpixel((1, 10), cr_electric); st3.putpixel((2, 10), cr_highlight)
st3.putpixel((1, 9), cr_highlight)
st3.putpixel((0, 8), cr_bright); st3.putpixel((1, 8), cr_highlight)
st3.putpixel((0, 7), cr_highlight)

# Right massive crystal spire (y=2..13)
for y in range(3, 14):
    st3.putpixel((10, y), cr_tabby if y in (3, 6, 9, 12) else cr_bright)
    st3.putpixel((11, y), cr_electric if y % 2 == 0 else cr_highlight)
    st3.putpixel((12, y), cr_tabby if y in (4, 7, 10, 13) else cr_dark)
st3.putpixel((11, 2), cr_highlight)

# Far-right sharp horn (y=6..13)
st3.putpixel((13, 13), cr_dark); st3.putpixel((14, 13), cr_tabby)
st3.putpixel((13, 12), cr_tabby); st3.putpixel((14, 12), cr_bright)
st3.putpixel((14, 11), cr_electric); st3.putpixel((15, 11), cr_tabby)
st3.putpixel((14, 10), cr_highlight); st3.putpixel((15, 10), cr_electric)
st3.putpixel((15, 9), cr_highlight)
st3.putpixel((15, 8), cr_highlight)
st3.putpixel((15, 7), cr_electric)
st3.putpixel((15, 6), cr_highlight)

st3.save(os.path.join(block_dir, "tabbyrium_cluster_stage3.png"))
st3.save(os.path.join(block_dir, "tabbyrium_cluster.png"))
st3.save(os.path.join(item_dir, "tabbyrium_cluster.png"))
print("Saved all tabbyrium_cluster stage textures")
