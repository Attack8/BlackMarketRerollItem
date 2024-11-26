package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.network.message.ShardTradeMessage;
import iskallia.vault.world.data.PlayerBlackMarketData;
import org.checkerframework.common.reflection.qual.Invoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Map;

@Mixin(value = ShardTradeMessage.class, remap = false)
public class ShardTradeMessageMixin {

    @Inject(method = "<init>(IIJLjava/util/Map;Ljava/time/LocalDateTime;)V", at = @At("RETURN"))
    private void addLogging(int rerollsUsed, int randomTradeCost, long seed, Map<Integer, PlayerBlackMarketData.BlackMarket.SelectedTrade> trades, LocalDateTime nextReset, CallbackInfo ci) {

        BlackMarketTweaks.LOGGER.info("--------- Messaged Trades ---------");
        trades.forEach((i, t) -> {
            BlackMarketTweaks.LOGGER.info("{} : {}, {}", i, t.getStack().getDisplayName().getString(), t.getShardCost());
        });
        BlackMarketTweaks.LOGGER.info("------------------------------------");
    }
}
