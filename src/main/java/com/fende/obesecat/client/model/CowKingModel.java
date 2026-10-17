package com.fende.obesecat.client.model;

import com.fende.obesecat.entity.CowKing;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;

public class CowKingModel extends CowModel<CowKing> {
    public CowKingModel(ModelPart root) {
        super(root);
    }

    public ModelPart getHead() {
        return this.head;
    }
}
