package dev.attackeight.black_market_tweaks.mixin;

import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlackMarketTileEntity.class, remap = false)
public class BlackMarketTileEntityMixin extends BlockEntity  {

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
}
