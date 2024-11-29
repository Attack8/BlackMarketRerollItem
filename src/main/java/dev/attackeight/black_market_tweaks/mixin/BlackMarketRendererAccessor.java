package dev.attackeight.black_market_tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.block.render.BlackMarketRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BlackMarketRenderer.class, remap = false)
public interface BlackMarketRendererAccessor {

    @Invoker
    void invokeRenderOutputItem(PoseStack matrixStack, MultiBufferSource buffer, int lightLevel, int overlay, float yOffset, float scale, ItemStack itemStack, Direction dir, int i);

    @Invoker
    void invokeRenderInputItem(PoseStack matrixStack, MultiBufferSource buffer, int lightLevel, int overlay, float yOffset, float scale, ItemStack itemStack, Direction dir, int i);

}
