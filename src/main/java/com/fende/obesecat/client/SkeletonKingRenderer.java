package com.fende.obesecat.client;

import com.fende.obesecat.entity.SkeletonKing;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class SkeletonKingRenderer extends MobRenderer<SkeletonKing, SkeletonModel<SkeletonKing>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/skeleton.png"
    );

    public SkeletonKingRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.8F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new SkeletonKingCrownLayer(this, context.bakeLayer(SkeletonKingCrownLayer.LAYER_LOCATION)));
    }

    @Override
    protected void scale(SkeletonKing entity, PoseStack poseStack, float partialTickTime) {
        super.scale(entity, poseStack, partialTickTime);
        poseStack.scale(1.85F, 1.85F, 1.85F);
    }

    @Override
    public ResourceLocation getTextureLocation(SkeletonKing entity) {
        return TEXTURE;
    }
}
