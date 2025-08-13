package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.BlackMarketRerollsPrestigePowerPower;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.world.data.PlayerBlackMarketData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(value = ServerboundResetBlackMarketTradesMessage.class, remap = false)
public class ServerboundResetBlackMarketTradesMessageMixin {

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData;get(Lnet/minecraft/server/MinecraftServer;)Liskallia/vault/world/data/PlayerBlackMarketData;"), cancellable = true)
    private static void shrinkItemStack(ServerboundResetBlackMarketTradesMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                BlockEntity be = serverPlayer.level.getBlockEntity(BlackMarketTweaks.getLastClickedPos(serverPlayer.getUUID()));
                if (be instanceof BlackMarketTileEntity) {
                    try {
                        PlayerBlackMarketData.BlackMarket playerMarket = PlayerBlackMarketData.get(context.getSender().server).getBlackMarket(context.getSender());
                        PrestigeTree prestige = PlayerPrestigePowersData.get(context.getSender().server).getPowers(context.getSender());

                        float chance = -1;

                        for (BlackMarketRerollsPrestigePowerPower power : prestige.getAll(BlackMarketRerollsPrestigePowerPower.class, Skill::isUnlocked)) {
                            chance = 0.25f;
                        }

                        boolean skip = new Random().nextFloat(0, 1) <= chance;

                        OverSizedInventory container = (OverSizedInventory) be.getClass().getDeclaredField("inventory").get(be);
                        ItemStack pearl = container.getItem(0);
                        pearl.shrink(skip ? 0 : 1);
                        container.setItem(0, pearl);
                        playerMarket.resetTradesWithoutTimer(context.getSender());
                    } catch (Exception e) {
                        BlackMarketTweaks.LOGGER.error(e.toString());
                    }
                }
            }
        });

        context.setPacketHandled(true);
        ci.cancel();
    }
}
