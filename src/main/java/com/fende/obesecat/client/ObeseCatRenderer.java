package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.client.model.FatManModel;
import com.fende.obesecat.entity.ObeseCat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public class ObeseCatRenderer extends CatRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "textures/entity/obese_cat.png"
    );

    public ObeseCatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FatManModel<>(context.bakeLayer(FatManModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(Cat cat) {
        return TEXTURE;
    }

    @Override
    protected void scale(Cat cat, PoseStack poseStack, float partialTickTime) {
        super.scale(cat, poseStack, partialTickTime);
        if (cat instanceof ObeseCat obeseCat) {
            poseStack.scale(obeseCat.getWidthScale(), obeseCat.getHeightScale(), obeseCat.getWidthScale());
        } else {
            poseStack.scale(1.5F, 1.2F, 1.5F);
        }
    }
}
