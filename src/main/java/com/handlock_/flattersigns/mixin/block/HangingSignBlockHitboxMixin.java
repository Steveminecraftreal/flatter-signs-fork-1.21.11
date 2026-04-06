package com.handlock_.flattersigns.mixin.block;

import com.handlock_.flattersigns.FlatterSignsConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingSignBlock.class)
public abstract class HangingSignBlockHitboxMixin {

    @Inject(
            method = "getOutlineShape",
            at = @At("RETURN"),
            cancellable = true
    )
    private void flattersigns$adjustHangingSignHitbox(
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

        cir.setReturnValue(flattersigns$transformShape(original));
    }

    /**
     * Adjusts the hanging sign collider to sit a bit higher and narrower,
     * roughly matching the flat model.
     */
    private static VoxelShape flattersigns$transformShape(VoxelShape shape) {
        // Box/VoxelShape use 0..1 coordinates => 1px = 1/16.
        final double px = 1.0 / 16.0;

        Box bb = shape.getBoundingBox();

        double x1 = bb.minX;
        double y1 = bb.minY;
        double z1 = bb.minZ;
        double x2 = bb.maxX;
        double y2 = bb.maxY;
        double z2 = bb.maxZ;

        // Raise the top by 1px and the bottom by 2px so the board is a bit shorter and higher.
        y1 += 2.0 * px;
        y2 += px;

        // Equalize X/Z extents by extending the shorter side.
        double widthX = x2 - x1;
        double widthZ = z2 - z1;

        if (widthX < widthZ) {
            double diff = widthZ - widthX;
            x1 -= diff / 2.0;
            x2 += diff / 2.0;
        } else if (widthZ < widthX) {
            double diff = widthX - widthZ;
            z1 -= diff / 2.0;
            z2 += diff / 2.0;
        }

        // Shrink every side laterally by 3 px (no height change here).
        double sideExtrude = 3.0 * px;
        x1 += sideExtrude;
        x2 -= sideExtrude;
        z1 += sideExtrude;
        z2 -= sideExtrude;

        // Clamp inside the block, just in case.
        x1 = Math.max(0.0, x1);
        y1 = Math.max(0.0, y1);
        z1 = Math.max(0.0, z1);

        x2 = Math.min(1.0, x2);
        y2 = Math.min(1.0, y2);
        z2 = Math.min(1.0, z2);

        Box newBox = new Box(x1, y1, z1, x2, y2, z2);
        return VoxelShapes.cuboid(newBox);
    }
}
