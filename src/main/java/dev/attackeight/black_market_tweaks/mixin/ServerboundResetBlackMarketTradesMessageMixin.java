package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.client.gui.screen.ShardTradeScreen;
import iskallia.vault.container.inventory.ShardTradeContainer;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
            if (serverPlayer != null) {
                BlockEntity be = serverPlayer.level.getBlockEntity(BlackMarketTweaks.getLastClickedPos(serverPlayer.getUUID()));
                if (be instanceof BlackMarketTileEntity) {
                    try {
                        OverSizedInventory container = (OverSizedInventory) be.getClass().getDeclaredField("inventory").get(be);
                        ItemStack pearl = container.getItem(0);
                        pearl.shrink(1);
                        container.setItem(0, pearl);
                    } catch (Exception e) {
                        BlackMarketTweaks.LOGGER.error(e.toString());
                    }
                }
            }
        });
    }
}
