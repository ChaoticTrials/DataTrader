package de.melanx.datatrader.trader;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TraderSkinLayer<T extends Trader, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {

    public TraderSkinLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, T trader, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!trader.isInvisible()) {
            ResourceLocation skinLocation = trader.getSkinLocation();
            if (skinLocation == null) {
                return;
            }

            M model = this.getParentModel();
            model.hatVisible(true);
            RenderLayer.renderColoredCutoutModel(model, skinLocation, poseStack, buffer, packedLight, trader, 1.0F, 1.0F, 1.0F);
        }
    }
}
