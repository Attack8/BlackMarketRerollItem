package dev.attackeight.black_market_tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BlackMarketTweaks.ID)
public class ModEvents {

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(BlackMarketTweaks.ID)) {
            String configValue = event.getConfig().getConfigData().get("item");
            ResourceLocation itemLocation = ResourceLocation.tryParse(configValue);
            if (itemLocation == null) {
                BlackMarketTweaks.LOGGER.error("Error Loading Config: Entry Given is not a Resource Location (" + configValue + ")");
                return;
            }
            if (!ForgeRegistries.ITEMS.containsKey(itemLocation)) {
                BlackMarketTweaks.LOGGER.error("Error Loading Config: Item not Present with Resource Location (" + configValue + ")");
                return;
            }
            BlackMarketTweaks.item = ForgeRegistries.ITEMS.getValue(itemLocation);
            BlackMarketTweaks.LOGGER.info("Successfully loaded re-roll item at resource location " + itemLocation);
        }
    }
}
