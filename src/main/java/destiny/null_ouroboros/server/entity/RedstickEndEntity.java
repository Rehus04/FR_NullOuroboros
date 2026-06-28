package destiny.null_ouroboros.server.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;
import java.util.UUID;

public class RedstickEndEntity extends Entity {
    public enum EndType {
        TOP,
        BOTTOM
    }

    private static final EntityDataAccessor<Integer> PARENT_ID =
            SynchedEntityData.defineId(RedstickEndEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> PARENT_UUID =
            SynchedEntityData.defineId(RedstickEndEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> END_TYPE =
            SynchedEntityData.defineId(RedstickEndEntity.class, EntityDataSerializers.INT);

    public static final int LIGHT_LEVEL = 12;
    private static final int NO_PARENT = -1;
    private static final int MISSING_PARENT_GRACE_TICKS = 120;
    private static final double MAX_MOVE_STEP = 0.015625D;
    private static final int MAX_PENETRATION_ATTEMPTS = 8;
    private int missingParentTicks;

    public RedstickEndEntity(EntityType<? extends RedstickEndEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public RedstickEndEntity(EntityType<? extends RedstickEndEntity> type, Level level, int parentId, UUID parentUuid, EndType endType, double x, double y, double z) {
        this(type, level);
        this.setParentId(parentId);
        this.setParentUuid(parentUuid);
        this.setEndType(endType);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PARENT_ID, NO_PARENT);
        this.entityData.define(PARENT_UUID, Optional.empty());
        this.entityData.define(END_TYPE, EndType.BOTTOM.ordinal());
    }

    public int getParentId() {
        return this.entityData.get(PARENT_ID);
    }

    public void setParentId(int parentId) {
        this.entityData.set(PARENT_ID, parentId);
    }

    public Optional<UUID> getParentUuid() {
        return this.entityData.get(PARENT_UUID);
    }

    public void setParentUuid(UUID parentUuid) {
        this.entityData.set(PARENT_UUID, Optional.ofNullable(parentUuid));
    }

    public EndType getEndType() {
        return EndType.values()[this.entityData.get(END_TYPE)];
    }

    public void setEndType(EndType endType) {
        this.entityData.set(END_TYPE, endType.ordinal());
    }

    public int getLightLevel() {
        return LIGHT_LEVEL;
    }

    public void moveWithCollision(Vec3 delta) {
        double length = delta.length();
        if (length < 1.0E-8D) {
            return;
        }

        int steps = Math.max(1, (int) Math.ceil(length / MAX_MOVE_STEP));
        Vec3 step = delta.scale(1.0D / steps);
        for (int i = 0; i < steps; i++) {
            this.move(MoverType.SELF, step);
            resolvePenetration();
        }
    }

    public void resolvePenetration() {
        for (int attempt = 0; attempt < MAX_PENETRATION_ATTEMPTS; attempt++) {
            AABB box = getBoundingBox();
            if (level().noCollision(this, box)) {
                return;
            }

            Vec3 push = computeMinimumPushOut(box);
            if (push.lengthSqr() < 1.0E-10D) {
                return;
            }

            setPos(getX() + push.x, getY() + push.y, getZ() + push.z);
        }
    }

    private Vec3 computeMinimumPushOut(AABB entityBox) {
        Level level = level();
        BlockPos minPos = BlockPos.containing(entityBox.minX - 0.5D, entityBox.minY - 0.5D, entityBox.minZ - 0.5D);
        BlockPos maxPos = BlockPos.containing(entityBox.maxX + 0.5D, entityBox.maxY + 0.5D, entityBox.maxZ + 0.5D);

        Vec3 bestPush = Vec3.ZERO;
        double bestLength = Double.MAX_VALUE;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    VoxelShape shape = state.getCollisionShape(level, pos);
                    if (shape.isEmpty()) {
                        continue;
                    }

                    for (AABB blockPart : shape.toAabbs()) {
                        AABB blockBox = blockPart.move(pos);
                        if (!entityBox.intersects(blockBox)) {
                            continue;
                        }

                        Vec3 push = minimumSeparation(entityBox, blockBox);
                        double pushLength = push.lengthSqr();
                        if (pushLength > 1.0E-10D && pushLength < bestLength) {
                            bestLength = pushLength;
                            bestPush = push;
                        }
                    }
                }
            }
        }

        return bestPush;
    }

    private static Vec3 minimumSeparation(AABB entity, AABB block) {
        double[][] options = {
                {block.minX - entity.maxX, 0.0D, 0.0D},
                {block.maxX - entity.minX, 0.0D, 0.0D},
                {0.0D, block.minY - entity.maxY, 0.0D},
                {0.0D, block.maxY - entity.minY, 0.0D},
                {0.0D, 0.0D, block.minZ - entity.maxZ},
                {0.0D, 0.0D, block.maxZ - entity.minZ},
        };

        Vec3 best = Vec3.ZERO;
        double bestAbs = Double.MAX_VALUE;
        for (double[] option : options) {
            double abs = Math.abs(option[0]) + Math.abs(option[1]) + Math.abs(option[2]);
            if (abs < 1.0E-10D || abs >= bestAbs) {
                continue;
            }

            bestAbs = abs;
            best = new Vec3(option[0], option[1], option[2]);
        }

        return best;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (findParent() != null) {
            missingParentTicks = 0;
            return;
        }

        missingParentTicks++;
        if (missingParentTicks > MISSING_PARENT_GRACE_TICKS) {
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy() && reason != RemovalReason.DISCARDED) {
            notifyParentRemoved();
        }
        super.remove(reason);
    }

    private void notifyParentRemoved() {
        RedstickEntity redstick = findParent();
        if (redstick != null) {
            redstick.onEndRemoved(this);
        }
    }

    private RedstickEntity findParent() {
        int parentId = getParentId();
        if (parentId != NO_PARENT) {
            Entity entity = this.level().getEntity(parentId);
            if (entity instanceof RedstickEntity redstick) return redstick;
        }

        Optional<UUID> parentUuid = getParentUuid();
        if (parentUuid.isEmpty()) return null;

        for (RedstickEntity redstick : this.level().getEntitiesOfClass(RedstickEntity.class, this.getBoundingBox().inflate(16.0D))) {
            if (redstick.getUUID().equals(parentUuid.get())) {
                setParentId(redstick.getId());
                return redstick;
            }
        }

        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Parent")) {
            setParentId(tag.getInt("Parent"));
        }
        if (tag.hasUUID("ParentUuid")) {
            setParentUuid(tag.getUUID("ParentUuid"));
        }
        if (tag.contains("EndType")) {
            setEndType(EndType.values()[tag.getInt("EndType")]);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (getParentId() != NO_PARENT) {
            tag.putInt("Parent", getParentId());
        }
        getParentUuid().ifPresent(uuid -> tag.putUUID("ParentUuid", uuid));
        tag.putInt("EndType", getEndType().ordinal());
        tag.putUUID("EndUuid", this.getUUID());
    }

    @Override
    public Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }

        if (!this.level().isClientSide) {
            RedstickEntity parent = findParent();
            if (parent != null) {
                parent.discardWithEnds();
            } else {
                this.discard();
            }
        }

        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
