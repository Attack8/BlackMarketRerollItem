package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.init.ModConfig;
import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerboundResetBlackMarketTradesMessage.class, remap = false)
public class ServerboundResetBlackMarketTradesMessageMixin {

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData$BlackMarket;resetTradesWithoutTimer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private static void shrinkItemStack(ServerboundResetBlackMarketTradesMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

            BlockEntity blockEntity = serverPlayer.level.getBlockEntity(BlackMarketTweaks.getLastClickedPos(serverPlayer.getUUID()));
            if (blockEntity instanceof BlackMarketTileEntity) {
                OverSizedInventory container = ((BlackMarketInventory) blockEntity).bmt$get();
                ItemStack reRollItem = container.getItem(0);
                reRollItem.shrink(ModConfig.COST.get());
                container.setItem(0, reRollItem);
            }
        });
    }
}
