package destiny.null_ouroboros.server.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RedstickPhysics {
    public static final float STICK_LENGTH = 0.5F;
    public static final double GRAVITY = -0.04D;
    public static final int CONSTRAINT_ITERATIONS = 3;

    private static final double FLAT_AXIS_Y_THRESHOLD = 0.35D * STICK_LENGTH;
    private static final double EDGE_SLIDE_STEP = 0.03125D;
    private static final double EDGE_SLIDE_MAX = 0.25D;
    private static final double MAX_CONSTRAINT_CORRECTION = 0.0625D;
    private static final double FALLING_VELOCITY_THRESHOLD = 0.02D;

    private RedstickPhysics() {}

    public static void snapToStickLength(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd) {
        Vec3 topPos = topEnd.position();
        Vec3 bottomPos = bottomEnd.position();
        Vec3 axis = topPos.subtract(bottomPos);
        Vec3 midpoint = topPos.add(bottomPos).scale(0.5D);
        Vec3 halfAxis;

        if (axis.lengthSqr() < 1.0E-6D) {
            halfAxis = new Vec3(0.0D, STICK_LENGTH * 0.5D, 0.0D);
        } else {
            halfAxis = axis.normalize().scale(STICK_LENGTH * 0.5D);
        }

        topEnd.setPos(midpoint.add(halfAxis));
        bottomEnd.setPos(midpoint.subtract(halfAxis));
        topEnd.resolvePenetration();
        bottomEnd.resolvePenetration();
    }

    public static void enforceStickLength(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd) {
        for (int i = 0; i < CONSTRAINT_ITERATIONS; i++) {
            Vec3 topPos = topEnd.position();
            Vec3 bottomPos = bottomEnd.position();
            Vec3 axis = topPos.subtract(bottomPos);
            double length = axis.length();
            if (length < 1.0E-6D) {
                moveEndpoint(topEnd, new Vec3(0.0D, STICK_LENGTH, 0.0D));
                continue;
            }

            double error = length - STICK_LENGTH;
            if (Math.abs(error) < 1.0E-4D) {
                continue;
            }

            Vec3 correction = axis.scale(error / length);
            double correctionLength = correction.length();
            if (correctionLength > MAX_CONSTRAINT_CORRECTION) {
                correction = correction.scale(MAX_CONSTRAINT_CORRECTION / correctionLength);
            }

            boolean topBlocked = isBlocked(topEnd, bottomEnd, topEnd);
            boolean bottomBlocked = isBlocked(topEnd, bottomEnd, bottomEnd);

            if (topBlocked && !bottomBlocked) {
                moveEndpoint(bottomEnd, correction);
            } else if (bottomBlocked && !topBlocked) {
                moveEndpoint(topEnd, correction.scale(-1.0D));
            } else {
                moveEndpoint(topEnd, correction.scale(-0.5D));
                moveEndpoint(bottomEnd, correction.scale(0.5D));
            }
        }

        topEnd.resolvePenetration();
        bottomEnd.resolvePenetration();
    }

    public static void resolveFlatEdgeWedging(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd) {
        if (topEnd.getDeltaMovement().y < -FALLING_VELOCITY_THRESHOLD || bottomEnd.getDeltaMovement().y < -FALLING_VELOCITY_THRESHOLD) {
            return;
        }

        if (hasFootprintSupport(topEnd) && hasFootprintSupport(bottomEnd)) {
            return;
        }

        Vec3 topPos = topEnd.position();
        Vec3 bottomPos = bottomEnd.position();
        Vec3 axis = topPos.subtract(bottomPos);
        if (Math.abs(axis.y) > FLAT_AXIS_Y_THRESHOLD) {
            return;
        }

        Vec3 horizontalAxis = new Vec3(axis.x, 0.0D, axis.z);
        if (horizontalAxis.lengthSqr() < 1.0E-6D) {
            horizontalAxis = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            horizontalAxis = horizontalAxis.normalize();
        }

        Vec3 halfAxis = horizontalAxis.scale(STICK_LENGTH * 0.5D);
        Vec3 perpendicular = new Vec3(-horizontalAxis.z, 0.0D, horizontalAxis.x);
        Vec3 midpoint = topPos.add(bottomPos).scale(0.5D);

        Vec3 bestOffset = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Vec3 direction : new Vec3[] {Vec3.ZERO, horizontalAxis, horizontalAxis.scale(-1.0D), perpendicular, perpendicular.scale(-1.0D)}) {
            for (double distance = 0.0D; distance <= EDGE_SLIDE_MAX + 1.0E-6D; distance += EDGE_SLIDE_STEP) {
                Vec3 offset = direction.scale(distance);
                if (!canRestFlatAt(topEnd, bottomEnd, midpoint.add(offset), halfAxis)) {
                    continue;
                }

                double score = -offset.lengthSqr();
                if (score > bestScore) {
                    bestScore = score;
                    bestOffset = offset;
                }
            }
        }

        if (bestOffset == null || bestOffset.lengthSqr() < 1.0E-8D) {
            return;
        }

        topEnd.moveWithCollision(bestOffset);
        bottomEnd.moveWithCollision(bestOffset);
        snapToStickLength(topEnd, bottomEnd);
    }

    private static void moveEndpoint(RedstickEndEntity end, Vec3 correction) {
        if (correction.lengthSqr() < 1.0E-8D) return;
        end.moveWithCollision(correction);
    }

    public static boolean hasBlockSupport(RedstickEndEntity end) {
        return hasPartialFootprintSupport(end.getBoundingBox(), end.level());
    }

    public static boolean hasFootprintSupport(RedstickEndEntity end) {
        return hasFootprintSupport(end.getBoundingBox(), end.level());
    }

    public static boolean hasPartialFootprintSupport(RedstickEndEntity end) {
        return hasPartialFootprintSupport(end.getBoundingBox(), end.level());
    }

    private static boolean hasFootprintSupport(AABB box, Level level) {
        return sampleFootprint(box, level, true);
    }

    private static boolean hasPartialFootprintSupport(AABB box, Level level) {
        return sampleFootprint(box, level, false);
    }

    private static boolean sampleFootprint(AABB box, Level level, boolean requireAll) {
        double probeY = box.minY - 0.0625D;
        double[][] samples = {
                {box.minX, box.minZ},
                {box.minX, box.maxZ},
                {box.maxX, box.minZ},
                {box.maxX, box.maxZ},
                {box.getCenter().x, box.getCenter().z}
        };

        boolean anySupported = false;
        for (double[] sample : samples) {
            if (hasSupportAt(level, sample[0], probeY, sample[1])) {
                anySupported = true;
            } else if (requireAll) {
                return false;
            }
        }

        return requireAll || anySupported;
    }

    private static boolean hasSupportAt(Level level, double x, double probeY, double z) {
        BlockPos pos = BlockPos.containing(x, probeY, z);
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean canRestFlatAt(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd, Vec3 midpoint, Vec3 halfAxis) {
        AABB topBox = boxAt(topEnd, midpoint.add(halfAxis));
        AABB bottomBox = boxAt(bottomEnd, midpoint.subtract(halfAxis));
        Level level = topEnd.level();
        return hasFootprintSupport(topBox, level) && hasFootprintSupport(bottomBox, level);
    }

    private static AABB boxAt(RedstickEndEntity end, Vec3 position) {
        AABB box = end.getBoundingBox();
        return box.move(position.subtract(end.position()));
    }

    private static boolean isBlocked(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd, RedstickEndEntity end) {
        if (isFlatCornerStraddle(topEnd, bottomEnd)) {
            return end.verticalCollision;
        }

        RedstickEndEntity partnerEnd = end == topEnd ? bottomEnd : topEnd;
        boolean thisFull = hasFootprintSupport(end);
        boolean partnerFull = hasFootprintSupport(partnerEnd);

        if (thisFull != partnerFull) {
            return thisFull;
        }

        if (thisFull) {
            if (end.horizontalCollision && !partnerEnd.horizontalCollision) {
                return false;
            }

            if (end.horizontalCollision && partnerEnd.horizontalCollision) {
                return end.verticalCollision;
            }

            return true;
        }

        return end.verticalCollision;
    }

    private static boolean isFlatCornerStraddle(RedstickEndEntity topEnd, RedstickEndEntity bottomEnd) {
        Vec3 axis = topEnd.position().subtract(bottomEnd.position());
        if (Math.abs(axis.y) > FLAT_AXIS_Y_THRESHOLD) {
            return false;
        }

        if (!topEnd.onGround() || !bottomEnd.onGround()) {
            return false;
        }

        boolean topFoot = hasFootprintSupport(topEnd);
        boolean bottomFoot = hasFootprintSupport(bottomEnd);

        if (topFoot && bottomFoot && (topEnd.horizontalCollision || bottomEnd.horizontalCollision)) {
            return true;
        }

        return !topFoot && !bottomFoot;
    }
}
