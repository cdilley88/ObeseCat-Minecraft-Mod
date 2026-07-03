package com.fende.obesecat.client;

import com.fende.obesecat.entity.CowKing;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.ModelLayers;

public class CowKingRenderer extends MobRenderer<CowKing, CowModel<CowKing>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");

    public CowKingRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 3.5F);
    }

    @Override
    protected void scale(CowKing cowKing, PoseStack poseStack, float partialTickTime) {
        super.scale(cowKing, poseStack, partialTickTime);
        poseStack.scale(5.0F, 5.0F, 5.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CowKing entity) {
        return TEXTURE;
    }
}
