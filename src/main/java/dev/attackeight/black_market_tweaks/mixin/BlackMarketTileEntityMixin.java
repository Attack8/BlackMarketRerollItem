package dev.attackeight.black_market_tweaks.mixin;

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
public class BlackMarketTileEntityMixin extends BlockEntity implements InventoryRetainerTileEntity {

    @Unique
    public final OverSizedInventory inventory = new OverSizedInventory(1, (BlackMarketTileEntity) (Object) this);

    public BlackMarketTileEntityMixin(BlockPos pos, BlockState state) {
        super(ModBlocks.BLACK_MARKET_TILE_ENTITY, pos, state);
    }

    @Unique
    @Override
    public void load(@NotNull CompoundTag tag) {
        this.inventory.load(tag);
    }

    @Unique
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        this.inventory.save(tag);
    }

    @Override public void storeInventoryContents(CompoundTag tag) {
        this.inventory.save("inventory", tag);
    }

    @Override public void loadInventoryContents(CompoundTag tag) {
        this.inventory.load("inventory", tag);
    }

    @Override public void clearInventoryContents() {
        this.inventory.clearContent();
    }
}
