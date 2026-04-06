package com.handlock_.flattersigns.mixin.block;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Custom outline for wall signs to better match the flat models.
 * The collider is slightly shifted, thinned, and pushed into the wall.
 */
@Mixin(WallSignBlock.class)
public abstract class WallSignBlockHitboxMixin {

    // Vertical bounds (in pixels).
    private static final double Y_MIN = 4.0;
    private static final double Y_MAX = 13.0;

    // Thickness in pixels, pushed slightly into the wall.
    private static final double T = 1.0;

    // Precomputed shapes for each facing.
    private static final VoxelShape SOUTH_SHAPE =
            Block.createCuboidShape(2.0, Y_MIN, -T, 15.0, Y_MAX, 0.0);

    private static final VoxelShape NORTH_SHAPE =
            Block.createCuboidShape(1.0, Y_MIN, 16.0, 14.0, Y_MAX, 16.0 + T);

    private static final VoxelShape EAST_SHAPE =
            Block.createCuboidShape(-T, Y_MIN, 1.0, 0.0, Y_MAX, 14.0);

    private static final VoxelShape WEST_SHAPE =
            Block.createCuboidShape(16.0, Y_MIN, 2.0, 16.0 + T, Y_MAX, 15.0);

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void flattersigns$customWallSignShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx, CallbackInfoReturnable<VoxelShape> cir) {
        if (!FlatterSignsConfig.isHitboxTweaksEnabled()) {
            return;
        }

        Direction facing = state.get(Properties.HORIZONTAL_FACING);
        switch (facing) {
            case SOUTH -> cir.setReturnValue(SOUTH_SHAPE);
            case NORTH -> cir.setReturnValue(NORTH_SHAPE);
            case EAST  -> cir.setReturnValue(EAST_SHAPE);
            case WEST  -> cir.setReturnValue(WEST_SHAPE);
        }
    }
}
