package dev.attackeight.black_market_tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.block.BlackMarketBlock;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.block.render.BlackMarketRenderer;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.init.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlackMarketRenderer.class, remap = false)
public class BlackMarketRendererMixin {

    @Inject(method = "render(Liskallia/vault/block/entity/BlackMarketTileEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), cancellable = true)
    private void renderMoreTradeItems(BlackMarketTileEntity blackMarketTile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, CallbackInfo ci) {
        BlackMarketRendererAccessor bmr = (BlackMarketRendererAccessor) this;
        Level world = blackMarketTile.getLevel();
        if (world != null) {
            Direction dir = blackMarketTile.getBlockState().getValue(BlackMarketBlock.FACING);
            ItemStack itemStack;
            if (ClientShardTradeData.getAvailableTrades().containsKey(2)) {
                itemStack = ClientShardTradeData.getTradeInfo(2).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 0);
                matrixStack.popPose();
            }

            if (ClientShardTradeData.getAvailableTrades().containsKey(0)) {
                itemStack = ClientShardTradeData.getTradeInfo(0).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 1);
                matrixStack.popPose();
            }

            if (ClientShardTradeData.getAvailableTrades().containsKey(1)) {
                itemStack = ClientShardTradeData.getTradeInfo(1).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 2);
                matrixStack.popPose();
            }
            if (ClientShardTradeData.getAvailableTrades().containsKey(5)) {
                itemStack = ClientShardTradeData.getTradeInfo(5).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 3);
                matrixStack.popPose();
            }

            if (ClientShardTradeData.getAvailableTrades().containsKey(3)) {
                itemStack = ClientShardTradeData.getTradeInfo(3).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 4);
                matrixStack.popPose();
            }

            if (ClientShardTradeData.getAvailableTrades().containsKey(4)) {
                itemStack = ClientShardTradeData.getTradeInfo(4).getA();
                matrixStack.pushPose();
                bmr.invokeRenderInputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, itemStack, dir, 5);
                matrixStack.popPose();
            }

            matrixStack.pushPose();
            bmr.invokeRenderOutputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, new ItemStack(ModItems.SOUL_SHARD), dir, 0);
            matrixStack.popPose();
            matrixStack.pushPose();
            bmr.invokeRenderOutputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, new ItemStack(ModItems.SOUL_SHARD), dir, 1);
            matrixStack.popPose();
            matrixStack.pushPose();
            bmr.invokeRenderOutputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, new ItemStack(ModItems.SOUL_SHARD), dir, 2);
            matrixStack.popPose();
            matrixStack.pushPose();
            bmr.invokeRenderOutputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, new ItemStack(ModItems.SOUL_SHARD), dir, 3);
            matrixStack.popPose();
            matrixStack.pushPose();
            bmr.invokeRenderOutputItem(matrixStack, buffer, combinedLight, combinedOverlay, 0.64F, 0.35F, new ItemStack(ModItems.SOUL_SHARD), dir, 4);
            matrixStack.popPose();
        }
        ci.cancel();
    }

    @Inject(method = "renderInputItem", at = @At(value = "HEAD"))
    private void allowRenderingMoreInputs(PoseStack matrixStack, MultiBufferSource buffer, int lightLevel, int overlay, float yOffset, float scale, ItemStack itemStack, Direction dir, int i, CallbackInfo ci) {
//        if (i > 2) {
//            i = i - 3;
//            matrixStack.pushPose();
//            matrixStack.translate(-0.8 , i == 0 ? -0.01 : 0.0,  i == 0 ? -0.8 : (i == 1 ? -1.6 : 0.0));
//            matrixStack.popPose();
//        }
        blackMarketTweaks$counter = i;
    }

    @Unique private static int blackMarketTweaks$counter = -1;

    @Redirect(method = "renderInputItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 1), remap = true)
    private void appositionMoreItemsCorrectly(PoseStack instance, double x, double y, double z) {
        int i = blackMarketTweaks$counter;
        if (i < 3) {
            instance.translate(x, y, z);
        } else {
            instance.translate(i == 3 ? 0.0 : (i == 4 ? 0.8 : -0.8), -0.4 + 0.7 + (i == 3 ? 0.0 : -0.05), (i == 3 ? -0.01 : 0.0));
            // instance.translate(i == 4 ? -0.8 : (i == 5 ? -1.6 : 0.0), -0.4, i == 4 ? -0.01 : 0.0 );
        }
    }

}
