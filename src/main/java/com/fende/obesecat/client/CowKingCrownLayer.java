package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.client.model.CowKingModel;
import com.fende.obesecat.entity.CowKing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public class CowKingCrownLayer extends RenderLayer<CowKing, CowKingModel> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "cow_king_crown"),
            "main"
    );
    public static final ResourceLocation CROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "textures/entity/cow_king_crown.png"
    );

    private final ModelPart crown;

    public CowKingCrownLayer(RenderLayerParent<CowKing, CowKingModel> renderer, ModelPart crownRoot) {
        super(renderer);
        this.crown = crownRoot.getChild("crown");
    }

    public static LayerDefinition createCrownLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cow head is (-4, -4, -6) to (4, 4, 0). Horns are at x = -5 and +4, y = -5, z = -4.
        // Crown sits on top of head at y = -4.0F, fitting right between and above the horns.
        CubeListBuilder builder = CubeListBuilder.create()
                // Gold crown base headband encircling the skull
                .texOffs(0, 0).addBox(-4.5F, -6.0F, -6.0F, 9.0F, 2.0F, 6.0F)
                // Front center golden prong with ruby jewel
                .texOffs(0, 8).addBox(-1.0F, -8.5F, -6.2F, 2.0F, 3.0F, 1.0F)
                // Front left and right prongs
                .texOffs(6, 8).addBox(-4.2F, -7.5F, -6.2F, 1.5F, 2.0F, 1.0F)
                .texOffs(12, 8).addBox(2.7F, -7.5F, -6.2F, 1.5F, 2.0F, 1.0F)
                // Side left and right prongs
                .texOffs(18, 8).addBox(-4.7F, -7.5F, -3.5F, 1.0F, 2.0F, 2.0F)
                .texOffs(24, 8).addBox(3.7F, -7.5F, -3.5F, 1.0F, 2.0F, 2.0F)
                // Back center prong
                .texOffs(0, 12).addBox(-1.0F, -7.5F, -0.8F, 2.0F, 2.0F, 1.0F);

        root.addOrReplaceChild("crown", builder, PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       CowKing cowKing, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        // Transform into cow's head coordinate system so the crown moves seamlessly with head animations
        this.getParentModel().getHead().translateAndRotate(poseStack);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CROWN_TEXTURE));
        this.crown.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
