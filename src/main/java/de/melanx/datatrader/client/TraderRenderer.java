package de.melanx.datatrader.client;

import de.melanx.datatrader.trader.Trader;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class TraderRenderer extends MobRenderer<Trader, VillagerModel<Trader>> {

    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.parse("textures/entity/villager/villager.png");

    public TraderRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new TraderSkinLayer<>(this));
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull Trader entity) {
        return VILLAGER_BASE_SKIN;
    }
}
