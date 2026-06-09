package com.fende.obesecat.client.model;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public class FatManModel<T extends Cat> extends CatModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "fat_man"),
            "main"
    );

    public FatManModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation base = CubeDeformation.NONE;
        CubeDeformation soft = new CubeDeformation(0.08F);

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox("main", -3.0F, -2.4F, -3.2F, 6.0F, 5.0F, 6.0F, soft)
                        .texOffs(0, 26).addBox("muzzle", -2.0F, -0.2F, -4.35F, 4.0F, 2.2F, 2.0F, base)
                        .texOffs(18, 26).addBox("left_cheek", 1.55F, 0.0F, -3.8F, 1.5F, 2.0F, 2.0F, base)
                        .texOffs(26, 26).addBox("right_cheek", -3.05F, 0.0F, -3.8F, 1.5F, 2.0F, 2.0F, base)
                        .texOffs(0, 13).addBox("left_ear", 1.15F, -4.0F, -0.2F, 1.5F, 2.2F, 2.4F, base)
                        .texOffs(8, 13).addBox("right_ear", -2.65F, -4.0F, -0.2F, 1.5F, 2.2F, 2.4F, base)
                        .texOffs(36, 26).addBox("left_eye", 0.75F, -1.05F, -3.65F, 1.05F, 1.05F, 0.2F, base)
                        .texOffs(40, 26).addBox("right_eye", -1.8F, -1.05F, -3.65F, 1.05F, 1.05F, 0.2F, base)
                        .texOffs(44, 26).addBox("nose", -0.55F, 0.25F, -4.55F, 1.1F, 0.8F, 0.35F, base),
                PartPose.offset(0.0F, 15.0F, -9.0F)
        );

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(34, 0).addBox("torso", -3.0F, 2.6F, -8.6F, 6.0F, 17.0F, 7.2F, soft)
                        .texOffs(64, 0).addBox("belly", -3.35F, 7.5F, -7.9F, 6.7F, 9.5F, 5.6F, new CubeDeformation(0.02F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "tail1",
                CubeListBuilder.create().texOffs(0, 34).addBox("base", -0.75F, 0.0F, 0.0F, 1.5F, 8.5F, 1.5F, base),
                PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(8, 34).addBox("tip", -0.85F, 0.0F, 0.0F, 1.7F, 8.5F, 1.7F, base)
                        .texOffs(18, 34).addBox("tuft", -1.2F, 6.2F, -0.25F, 2.4F, 2.6F, 2.2F, base),
                PartPose.offset(0.0F, 20.0F, 14.0F)
        );

        CubeListBuilder hindLeg = CubeListBuilder.create()
                .texOffs(34, 34).addBox("leg", -1.15F, 0.0F, 0.8F, 2.3F, 6.0F, 2.3F, base)
                .texOffs(44, 34).addBox("paw", -1.45F, 5.0F, -0.15F, 2.9F, 1.2F, 3.4F, base);
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(1.35F, 18.0F, 5.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-1.35F, 18.0F, 5.0F));

        CubeListBuilder frontLeg = CubeListBuilder.create()
                .texOffs(58, 34).addBox("leg", -1.05F, 0.0F, -0.1F, 2.1F, 10.0F, 2.1F, base)
                .texOffs(68, 34).addBox("paw", -1.35F, 8.85F, -1.0F, 2.7F, 1.3F, 3.2F, base);
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(1.45F, 14.1F, -5.0F));
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-1.45F, 14.1F, -5.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }
}
