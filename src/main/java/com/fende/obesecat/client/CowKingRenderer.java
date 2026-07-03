package com.fende.obesecat.client;

import com.fende.obesecat.entity.CowKing;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.animal.Cow;

public class CowKingRenderer extends CowRenderer {
    public CowKingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Cow cowKing, PoseStack poseStack, float partialTickTime) {
        super.scale(cowKing, poseStack, partialTickTime);
        poseStack.scale(5.0F, 5.0F, 5.0F);
    }
}
