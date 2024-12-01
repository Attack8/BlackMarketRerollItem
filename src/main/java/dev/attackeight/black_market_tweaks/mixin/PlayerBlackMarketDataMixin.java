package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.config.OmegaSoulShardConfig;
import iskallia.vault.config.SoulShardConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.PlayerBlackMarketData;
import iskallia.vault.world.data.PlayerBlackMarketData.BlackMarket.SelectedTrade;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = PlayerBlackMarketData.BlackMarket.class, remap = false)
public abstract class PlayerBlackMarketDataMixin {

    @Shadow @Final private Map<Integer, PlayerBlackMarketData.BlackMarket.SelectedTrade> trades;

    @Shadow public abstract void setNextReset(UUID playerUuid);

    @Shadow private int resetRolls;

    @Shadow public abstract void syncToClient(MinecraftServer server);

    @Shadow @Final PlayerBlackMarketData this$0;

    @Inject(method = "resetTrades", at = @At("HEAD"), cancellable = true)
    private void rollSixTrades(UUID playerUuid, CallbackInfo ci) {
        this.trades.clear();
        BlackMarketTweaks.LOGGER.info("log1");

        for (int i = 0; i < 5; i++) {
            if(i == 2) i = 3;
            int playerLevel = PlayerVaultStatsData.getServer().getVaultStats(playerUuid).getVaultLevel();
            Set<SoulShardConfig.Trades> tradesList = ModConfigs.SOUL_SHARD.getTrades();
            SoulShardConfig.Trades tradesUsed = null;
            for (SoulShardConfig.Trades trades : tradesList) {
                if(playerLevel >= trades.getMinLevel() && (tradesUsed == null || tradesUsed.getMinLevel() < trades.getMinLevel())){
                    tradesUsed = trades;
                }
            }
            if(tradesUsed != null) {
                SelectedTrade trade = new SelectedTrade(tradesUsed.getRandomTrade());
                trade = trade.initialize(playerLevel);
                this.trades.put(i, trade);
            }
        }
        int playerLevel = PlayerVaultStatsData.getServer().getVaultStats(playerUuid).getVaultLevel();
        Set<OmegaSoulShardConfig.Trades> omegaTradesList = ModConfigs.OMEGA_SOUL_SHARD.getTrades();
        OmegaSoulShardConfig.Trades tradesUsed = null;
        for (OmegaSoulShardConfig.Trades trades : omegaTradesList) {
            if(playerLevel >= trades.getMinLevel() && (tradesUsed == null || tradesUsed.getMinLevel() < trades.getMinLevel())){
                tradesUsed = trades;
            }
        }
        if(tradesUsed != null) {
            SelectedTrade trade = new SelectedTrade(tradesUsed.getRandomTrade());
            trade = trade.initialize(playerLevel);
            this.trades.put(2, trade);
            trade = new SelectedTrade(tradesUsed.getRandomTrade());
            trade = trade.initialize(playerLevel);
            this.trades.put(5, trade);
        }

        setNextReset(playerUuid);
        this.resetRolls = 0;
        this$0.setDirty();
        syncToClient(ServerLifecycleHooks.getCurrentServer());

        ci.cancel();
    }


    @Inject(method = "resetTradesWithoutTimer", at = @At("HEAD"), cancellable = true)
    private void rollSixTradesWithoutTimer(ServerPlayer player, CallbackInfo ci) {
        this.trades.clear();
        BlackMarketTweaks.LOGGER.info("log2");

        for (int i = 0; i < 5; i++) {
            if(i == 2) i = 3;
            int playerLevel = PlayerVaultStatsData.getServer().getVaultStats(player.getUUID()).getVaultLevel();
            Set<SoulShardConfig.Trades> tradesList = ModConfigs.SOUL_SHARD.getTrades();
            SoulShardConfig.Trades tradesUsed = null;
            for (SoulShardConfig.Trades trades : tradesList) {
                if(playerLevel >= trades.getMinLevel() && (tradesUsed == null || tradesUsed.getMinLevel() < trades.getMinLevel())){
                    tradesUsed = trades;
                }
            }
            if(tradesUsed != null) {
                SelectedTrade trade = new SelectedTrade(tradesUsed.getRandomTrade());
                trade = trade.initialize(playerLevel);
                this.trades.put(i, trade);
            }
        }
        int playerLevel = PlayerVaultStatsData.getServer().getVaultStats(player.getUUID()).getVaultLevel();
        Set<OmegaSoulShardConfig.Trades> omegaTradesList = ModConfigs.OMEGA_SOUL_SHARD.getTrades();
        OmegaSoulShardConfig.Trades tradesUsed = null;
        for (OmegaSoulShardConfig.Trades trades : omegaTradesList) {
            if(playerLevel >= trades.getMinLevel() && (tradesUsed == null || tradesUsed.getMinLevel() < trades.getMinLevel())){
                tradesUsed = trades;
            }
        }
        if(tradesUsed != null) {
            SelectedTrade trade = new SelectedTrade(tradesUsed.getRandomTrade());
            trade = trade.initialize(playerLevel);
            this.trades.put(2, trade);
            trade = new SelectedTrade(tradesUsed.getRandomTrade());
            trade = trade.initialize(playerLevel);
            this.trades.put(5, trade);
        }

        setNextReset(player.getUUID());
        this.resetRolls = 0;
        this$0.setDirty();
        syncToClient(ServerLifecycleHooks.getCurrentServer());

        ci.cancel();
    }
}
