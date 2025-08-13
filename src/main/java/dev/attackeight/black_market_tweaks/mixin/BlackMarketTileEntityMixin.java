package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.block.entity.base.InventoryRetainerTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlackMarketTileEntity.class, remap = false)
public class BlackMarketTileEntityMixin extends BlockEntity implements InventoryRetainerTileEntity, BlackMarketInventory {
    @Unique public final OverSizedInventory bmt$inventory = new OverSizedInventory(1, this);

    public BlackMarketTileEntityMixin(BlockPos pos, BlockState state) {
        super(ModBlocks.BLACK_MARKET_TILE_ENTITY, pos, state);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        this.bmt$inventory.load(tag);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        this.bmt$inventory.save(tag);
    }

    @Override
    public void storeInventoryContents(CompoundTag tag) {
        this.bmt$inventory.save("inventory", tag);
    }

    @Override
    public void loadInventoryContents(CompoundTag tag) {
        this.bmt$inventory.load("inventory", tag);
    }

    @Override
    public void clearInventoryContents() {
        this.bmt$inventory.clearContent();
    }

    @Override
    public OverSizedInventory bmt$get() {
        return this.bmt$inventory;
    }
}
