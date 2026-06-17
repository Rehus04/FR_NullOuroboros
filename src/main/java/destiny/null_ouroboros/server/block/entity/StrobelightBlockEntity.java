package destiny.null_ouroboros.server.block.entity;

import destiny.null_ouroboros.server.block.StrobelightBlock;
import destiny.null_ouroboros.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StrobelightBlockEntity extends BlockEntity {
    public static final String ROTATION_ANGLE = "RotationAngle";
    public static final String ROTATION_SPEED = "RotationSpeed";

    // Animation constants
    private static final float MAX_RPM = 40f;      // you can change this
    private static final float MAX_SPEED = MAX_RPM * 360f / 60f / 20f;   // degrees per tick (at 20 tps)
    private static final float ACCELERATION = MAX_SPEED / (2f * 20f);    // reach max in 2 seconds

    // Rotation state (in degrees)
    private float rotationAngle = 0f;
    private float rotationSpeed = 0f;               // degrees per tick

    public StrobelightBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.STROBELIGHT_BLOCK_ENTITY.get(), pos, state);
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StrobelightBlockEntity be) {
        boolean lit = state.getValue(StrobelightBlock.LIT);
        float targetSpeed = lit ? MAX_SPEED : 0f;

        // Smooth acceleration / deceleration
        if (be.rotationSpeed < targetSpeed) {
            be.rotationSpeed = Math.min(be.rotationSpeed + ACCELERATION, targetSpeed);
        } else if (be.rotationSpeed > targetSpeed) {
            be.rotationSpeed = Math.max(be.rotationSpeed - ACCELERATION, targetSpeed);
        }

        be.rotationAngle = (be.rotationAngle + be.rotationSpeed) % 360f;
        // No need to markDirty every tick – we'll save on chunk unload / world save
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putFloat(ROTATION_ANGLE, rotationAngle);
        tag.putFloat(ROTATION_SPEED, rotationSpeed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        rotationAngle = tag.getFloat(ROTATION_ANGLE);
        rotationSpeed = tag.getFloat(ROTATION_SPEED);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();

        tag.putFloat(ROTATION_ANGLE, rotationAngle);
        tag.putFloat(ROTATION_SPEED, rotationSpeed);

        return tag;
    }
}