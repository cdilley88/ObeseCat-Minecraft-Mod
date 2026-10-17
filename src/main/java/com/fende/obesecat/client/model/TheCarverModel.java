package com.fende.obesecat.client.model;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.TheCarver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class TheCarverModel extends HumanoidModel<TheCarver> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "the_carver"),
            "main"
    );

    public TheCarverModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation base = CubeDeformation.NONE;

        // 1. HEAD (offset forward and sunken into muscular traps)
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        // Main skull: 10 wide, 9 high, 9 deep
                        .texOffs(0, 0).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 9.0F, 9.0F, base)
                        // Heavy Demonic Brow Ridge
                        .texOffs(0, 20).addBox(-5.5F, -7.0F, -5.8F, 11.0F, 2.0F, 2.0F, base)
                        // Protruding Underbite / Demonic Jaw
                        .texOffs(30, 20).addBox(-4.0F, -3.0F, -6.0F, 8.0F, 3.0F, 3.0F, base)
                        // Right Horn: base + curved spike
                        .texOffs(60, 0).addBox(-6.5F, -9.5F, -1.0F, 2.0F, 3.0F, 2.0F, base)
                        .texOffs(60, 6).addBox(-7.5F, -12.5F, 0.5F, 2.0F, 4.0F, 2.0F, base)
                        // Left Horn: base + curved spike
                        .texOffs(75, 0).addBox(4.5F, -9.5F, -1.0F, 2.0F, 3.0F, 2.0F, base)
                        .texOffs(75, 6).addBox(5.5F, -12.5F, 0.5F, 2.0F, 4.0F, 2.0F, base),
                PartPose.offset(0.0F, -11.0F, -2.5F)
        );

        // Empty hat child required by HumanoidModel contract
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // 2. TORSO / BODY (Warden-inspired wide barrel chest, traps hunch, and bloated butcher gut)
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        // Main Chest: 16 wide, 12 high, 8 deep
                        .texOffs(0, 32).addBox(-8.0F, -12.0F, -4.0F, 16.0F, 12.0F, 8.0F, base)
                        // Traps / Neck Hump behind skull
                        .texOffs(70, 32).addBox(-6.0F, -14.0F, -1.0F, 12.0F, 3.0F, 6.0F, base)
                        // Massive Butcher Gut / Belly protruding forward
                        .texOffs(0, 56).addBox(-7.0F, -6.0F, -6.2F, 14.0F, 8.0F, 3.0F, base)
                        // Apron Flap / Skirt hanging in front of thighs
                        .texOffs(40, 56).addBox(-6.0F, 0.0F, -4.8F, 12.0F, 7.0F, 1.0F, base),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        // 3. RIGHT ARM (Heavy butcher arm with bicep bulk)
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        // Arm core: 6 wide, 14 high, 6 deep
                        .texOffs(0, 72).addBox(-4.0F, -2.0F, -3.0F, 6.0F, 14.0F, 6.0F, base)
                        // Shoulder muscle / pad
                        .texOffs(26, 72).addBox(-4.5F, -2.5F, -3.5F, 7.0F, 4.0F, 7.0F, base),
                PartPose.offset(-11.0F, -10.0F, 0.0F)
        );

        // 4. LEFT ARM (Heavy butcher arm with bicep bulk)
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        // Arm core: 6 wide, 14 high, 6 deep
                        .texOffs(56, 72).addBox(-2.0F, -2.0F, -3.0F, 6.0F, 14.0F, 6.0F, base)
                        // Shoulder muscle / pad
                        .texOffs(82, 72).addBox(-2.5F, -2.5F, -3.5F, 7.0F, 4.0F, 7.0F, base),
                PartPose.offset(11.0F, -10.0F, 0.0F)
        );

        // 5. RIGHT LEG (Heavy stocky pillar leg with iron boot cuff)
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        // Leg core: 6 wide, 12 high, 6 deep
                        .texOffs(0, 96).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F, base)
                        // Boot Cuff
                        .texOffs(26, 96).addBox(-3.5F, 7.0F, -3.5F, 7.0F, 5.0F, 7.0F, base),
                PartPose.offset(-4.5F, 0.0F, 0.0F)
        );

        // 6. LEFT LEG (Heavy stocky pillar leg with iron boot cuff)
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        // Leg core: 6 wide, 12 high, 6 deep
                        .texOffs(56, 96).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F, base)
                        // Boot Cuff
                        .texOffs(82, 96).addBox(-3.5F, 7.0F, -3.5F, 7.0F, 5.0F, 7.0F, base),
                PartPose.offset(4.5F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart modelPart = this.getArm(arm);
        modelPart.translateAndRotate(poseStack);
        if (arm == HumanoidArm.RIGHT) {
            poseStack.translate(-0.06F, 0.12F, 0.0F);
        } else {
            poseStack.translate(0.06F, 0.12F, 0.0F);
        }
    }

    @Override
    public void setupAnim(TheCarver carver, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(carver, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // Menacing attack stance when target locked: raise the Cursed Cleaver high!
        if (carver.isAggressive()) {
            this.rightArm.xRot = -1.15F + Mth.sin(ageInTicks * 0.25F) * 0.12F;
            this.rightArm.yRot = -0.22F;
        }
    }
}
