package destiny.null_ouroboros.server.block.entity;

import destiny.null_ouroboros.server.block.MechanicalSirenBlock;
import destiny.null_ouroboros.server.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalSirenBlockEntity extends BlockEntity {
    public static final String ROTATION_ANGLE = "RotationAngle";
    public static final String ROTATION_SPEED = "RotationSpeed";

    private static final float MAX_RPM = 120f;
    private static final float MAX_SPEED = MAX_RPM * 360f / 60f / 20f;
    private static final float ACCELERATION = MAX_SPEED / (3f * 20f);
    private static final float DECELERATION = MAX_SPEED / (8f * 20f);

    private float rotationAngle = 0f;
    private float rotationSpeed = 0f;
    public MechanicalSirenBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MECHANICAL_SIREN_BLOCK_ENTITY.get(), pos, state);
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public static float getMaxSpeed() {
        return MAX_SPEED;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalSirenBlockEntity mechanicalSirenBlockEntity) {
        boolean active = state.getValue(MechanicalSirenBlock.ACTIVE);
        float targetSpeed = active ? MAX_SPEED : 0f;

        if (mechanicalSirenBlockEntity.rotationSpeed < targetSpeed) {
            mechanicalSirenBlockEntity.rotationSpeed = Math.min(mechanicalSirenBlockEntity.rotationSpeed + ACCELERATION, targetSpeed);
        } else if (mechanicalSirenBlockEntity.rotationSpeed > targetSpeed) {
            mechanicalSirenBlockEntity.rotationSpeed = Math.max(mechanicalSirenBlockEntity.rotationSpeed - DECELERATION, targetSpeed);
        }

        mechanicalSirenBlockEntity.rotationAngle = (mechanicalSirenBlockEntity.rotationAngle + mechanicalSirenBlockEntity.rotationSpeed) % 360f;
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