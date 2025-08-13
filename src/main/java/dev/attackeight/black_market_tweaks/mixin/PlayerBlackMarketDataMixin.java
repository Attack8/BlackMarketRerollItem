package dev.attackeight.black_market_tweaks.mixin;

import iskallia.vault.config.OmegaSoulShardConfig;
import iskallia.vault.config.SoulShardConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.BlackMarketRerollsPrestigePowerPower;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.world.data.PlayerBlackMarketData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerBlackMarketData.BlackMarket.class, remap = false)
public abstract class PlayerBlackMarketDataMixin {
    @Unique private static final int[] bmt$NORMAL = new int[] { 0, 1, 3, 4 };
    @Unique private static final int[] bmt$OMEGA = new int[] { 2, 5 };

    @Shadow @Final PlayerBlackMarketData this$0;
    @Shadow @Final private Map<Integer, PlayerBlackMarketData.BlackMarket.SelectedTrade> trades;
    @Shadow private int resetRolls;

    @Inject(method = "resetTrades", at = @At("HEAD"), cancellable = true)
    private void resetTrades(UUID playerUuid, CallbackInfo ci) {
        bmt$resetTrades(playerUuid, ci);
    }

    @Inject(method = "resetTradesWithoutTimer", at = @At("HEAD"), cancellable = true)
    private void resetTradesWithoutTimer(ServerPlayer player, CallbackInfo ci) {
        bmt$resetTrades(player.getUUID(), ci);
    }

    @Unique
    private void bmt$resetTrades(UUID uuid, CallbackInfo ci) {
        this.trades.clear();
        this.resetRolls = 0;

        int playerLevel = PlayerVaultStatsData.getServer().getVaultStats(uuid).getVaultLevel();

        SoulShardConfig.Trades normalTrades = null;
        for (SoulShardConfig.Trades trades : ModConfigs.SOUL_SHARD.getTrades()) {
            if (playerLevel >= trades.getMinLevel() && (normalTrades == null || normalTrades.getMinLevel() < trades.getMinLevel())) {
                normalTrades = trades;
            }
        }
        if (normalTrades != null) {
            for (int i : bmt$NORMAL) {
                this.trades.put(i, new PlayerBlackMarketData.BlackMarket.SelectedTrade(normalTrades.getRandomTrade()).initialize(playerLevel));
            }
        }

        OmegaSoulShardConfig.Trades omegaTrades = null;
        PrestigeTree prestige = PlayerPrestigePowersData.get(ServerLifecycleHooks.getCurrentServer()).getPowers(uuid);
        boolean hasPrestige = !prestige.getAll(BlackMarketRerollsPrestigePowerPower.class, Skill::isUnlocked).isEmpty();
        for (OmegaSoulShardConfig.Trades trades : ModConfigs.OMEGA_SOUL_SHARD.getTrades()) {
            if (hasPrestige) {
                if (trades.getMinLevel() >= 101 && (omegaTrades == null || trades.getMinLevel() > omegaTrades.getMinLevel())) {
                    omegaTrades = trades;
                }
            } else {
                if (playerLevel >= trades.getMinLevel() && (omegaTrades == null || omegaTrades.getMinLevel() < trades.getMinLevel())) {
                    omegaTrades = trades;
                }
            }
        }
        if (omegaTrades != null) {
            for (int i : bmt$OMEGA) {
                this.trades.put(i, new PlayerBlackMarketData.BlackMarket.SelectedTrade(omegaTrades.getRandomTrade()).initialize(playerLevel));
            }
        }

        setNextReset(uuid);
        this$0.setDirty();
        syncToClient(ServerLifecycleHooks.getCurrentServer());

        ci.cancel();
    }

    @Shadow public abstract void setNextReset(UUID playerUuid);
    @Shadow public abstract void syncToClient(MinecraftServer server);
}
