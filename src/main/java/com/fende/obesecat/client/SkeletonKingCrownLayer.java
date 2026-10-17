package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.SkeletonKing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SkeletonKingCrownLayer extends RenderLayer<SkeletonKing, SkeletonModel<SkeletonKing>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "skeleton_king_crown"),
            "main"
    );
    public static final ResourceLocation CROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "textures/entity/skeleton_king_crown.png"
    );

    private final ModelPart crown;

    public SkeletonKingCrownLayer(RenderLayerParent<SkeletonKing, SkeletonModel<SkeletonKing>> renderer, ModelPart crownRoot) {
        super(renderer);
        this.crown = crownRoot.getChild("crown");
    }

    public static LayerDefinition createCrownLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Skeleton skull in Minecraft is (-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).
        // Top of the head is at y = -8.0F.
        CubeListBuilder builder = CubeListBuilder.create()
                // Gold crown base band encircling the skull
                .texOffs(0, 0).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 2.0F, 9.0F)
                // Front center golden prong with jewel (taller)
                .texOffs(0, 16).addBox(-1.5F, -12.0F, -4.7F, 3.0F, 3.0F, 1.0F)
                // Front left and right prongs
                .texOffs(8, 16).addBox(-4.3F, -10.5F, -4.7F, 2.0F, 2.0F, 1.0F)
                .texOffs(8, 16).addBox(2.3F, -10.5F, -4.7F, 2.0F, 2.0F, 1.0F)
                // Side left and right prongs
                .texOffs(16, 16).addBox(-4.7F, -10.5F, -1.0F, 1.0F, 2.0F, 2.0F)
                .texOffs(16, 16).addBox(3.7F, -10.5F, -1.0F, 1.0F, 2.0F, 2.0F)
                // Back center prong
                .texOffs(0, 16).addBox(-1.5F, -10.5F, 3.7F, 3.0F, 2.0F, 1.0F);

        root.addOrReplaceChild("crown", builder, PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       SkeletonKing entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        // Transform into skeleton's head coordinate system so the crown moves seamlessly with head animations
        this.getParentModel().getHead().translateAndRotate(poseStack);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEXTURE));
        this.crown.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
