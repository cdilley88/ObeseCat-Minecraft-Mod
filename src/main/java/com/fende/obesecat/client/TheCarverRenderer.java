package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.client.model.TheCarverModel;
import com.fende.obesecat.entity.TheCarver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class TheCarverRenderer extends MobRenderer<TheCarver, TheCarverModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID, "textures/entity/the_carver.png"
    );

    public TheCarverRenderer(EntityRendererProvider.Context context) {
        super(context, new TheCarverModel(context.bakeLayer(TheCarverModel.LAYER_LOCATION)), 1.1F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(TheCarver carver, PoseStack poseStack, float partialTickTime) {
        super.scale(carver, poseStack, partialTickTime);
        poseStack.scale(1.35F, 1.35F, 1.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(TheCarver entity) {
        return TEXTURE;
    }
}
