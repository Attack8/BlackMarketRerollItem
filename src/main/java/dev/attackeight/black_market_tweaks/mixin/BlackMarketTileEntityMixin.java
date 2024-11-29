package dev.attackeight.black_market_tweaks.mixin;

import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlackMarketTileEntity.class)
public class BlackMarketTileEntityMixin {

    @Unique
    public final OverSizedInventory inventory = new OverSizedInventory(1, (BlackMarketTileEntity) (Object) this);

    @Unique
    public void load(@NotNull CompoundTag tag) {
        this.inventory.load(tag);
    }

    @Unique
    protected void saveAdditional(@NotNull CompoundTag tag) {
        this.inventory.save(tag);
    }
}
