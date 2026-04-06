package com.handlock_.flattersigns.mixin.block;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Slightly raises and shortens the board part of wall hanging signs.
 * The upper bar is left as-is.
 */
@Mixin(WallHangingSignBlock.class)
public abstract class WallHangingSignBlockHitboxMixin {

    @Inject(
            method = "getOutlineShape",
            at = @At("RETURN"),
            cancellable = true
    )
    private void flattersigns$adjustWallHangingSignBoardShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        if (!FlatterSignsConfig.isHitboxTweaksEnabled()) {
            return;
        }

        VoxelShape original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }

        // Decompose into individual boxes (bar + board).
        List<Box> boxes = new ArrayList<>(original.getBoundingBoxes());
        if (boxes.isEmpty()) {
            return;
        }

        // Find the lowest box: treat that as the sign board.
        int lowestIdx = 0;
        double lowestY = boxes.get(0).minY;
        for (int i = 1; i < boxes.size(); i++) {
            Box b = boxes.get(i);
            if (b.minY < lowestY) {
                lowestY = b.minY;
                lowestIdx = i;
            }
        }

        Box board = boxes.get(lowestIdx);

        // 1px in voxel coordinates (0..1).
        final double px = 1.0 / 16.0;

        double x1 = board.minX;
        double y1 = board.minY;
        double z1 = board.minZ;
        double x2 = board.maxX;
        double y2 = board.maxY;
        double z2 = board.maxZ;

        // Move the whole rectangle up by 1px.
        y1 += px;
        y2 += px;

        // Raise the bottom face by an extra 1px (board becomes 1px shorter).
        y1 += px;

        // Clamp inside the block space just in case.
        if (y1 < 0.0) y1 = 0.0;
        if (y2 > 1.0) y2 = 1.0;

        boxes.set(lowestIdx, new Box(x1, y1, z1, x2, y2, z2));

        // Rebuild the shape as union of all boxes.
        VoxelShape rebuilt = VoxelShapes.empty();
        for (Box b : boxes) {
            rebuilt = VoxelShapes.union(rebuilt, VoxelShapes.cuboid(b));
        }

        cir.setReturnValue(rebuilt);
    }
}
