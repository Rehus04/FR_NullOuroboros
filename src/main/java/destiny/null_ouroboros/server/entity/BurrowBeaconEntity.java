package destiny.null_ouroboros.server.entity;

import destiny.null_ouroboros.client.network.ClientBoundBurrowBeaconSyncPacket;
import destiny.null_ouroboros.server.capability.ManifoldingCapability;
import destiny.null_ouroboros.server.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class BurrowBeaconEntity extends LivingEntity {
    public enum State {
        DEPLOY,
        LAND,
        DRILL,
        DRILL_IDLE
    }

    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(BurrowBeaconEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM_START_TIME =
            SynchedEntityData.defineId(BurrowBeaconEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<CompoundTag> CONNECTIONS =
            SynchedEntityData.defineId(BurrowBeaconEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Float> PULSE_OFFSET =
            SynchedEntityData.defineId(BurrowBeaconEntity.class, EntityDataSerializers.FLOAT);

    private static final int LAND_DURATION = 10;
    private static final int DRILL_DURATION = 40;

    public BurrowBeaconEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE, State.DEPLOY.ordinal());
        this.entityData.define(ANIM_START_TIME, 0);
        this.entityData.define(CONNECTIONS, new CompoundTag());
        this.entityData.define(PULSE_OFFSET, this.random.nextFloat());
    }

    public State getAnimationState() {
        return State.values()[this.entityData.get(ANIMATION_STATE)];
    }

    public int getAnimationStartTime() {
        return this.entityData.get(ANIM_START_TIME);
    }

    private void setAnimationState(State state) {
        this.entityData.set(ANIM_START_TIME, this.tickCount);
        this.entityData.set(ANIMATION_STATE, state.ordinal());
    }

    public Set<BlockPos> getConnectedPositions() {
        CompoundTag tag = this.entityData.get(CONNECTIONS);
        long[] packed = tag.getLongArray("list");
        Set<BlockPos> set = new HashSet<>();
        for (long l : packed) set.add(BlockPos.of(l));
        return set;
    }

    public void addConnection(BlockPos pos) {
        CompoundTag tag = this.entityData.get(CONNECTIONS).copy();
        long[] old = tag.getLongArray("list");
        long newVal = pos.asLong();
        for (long l : old) if (l == newVal) return;
        long[] newArr = Arrays.copyOf(old, old.length + 1);
        newArr[old.length] = newVal;
        tag.putLongArray("list", newArr);
        this.entityData.set(CONNECTIONS, tag);
    }

    public void removeConnection(BlockPos pos) {
        CompoundTag tag = this.entityData.get(CONNECTIONS).copy();
        long[] old = tag.getLongArray("list");
        List<Long> list = new ArrayList<>();
        long target = pos.asLong();
        for (long l : old) if (l != target) list.add(l);
        long[] newArr = new long[list.size()];
        for (int i = 0; i < newArr.length; i++) newArr[i] = list.get(i);
        tag.putLongArray("list", newArr);
        this.entityData.set(CONNECTIONS, tag);
    }

    public float getPulseOffset() {
        return this.entityData.get(PULSE_OFFSET);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        State state = getAnimationState();
        int elapsed = this.tickCount - getAnimationStartTime();

        switch (state) {
            case DEPLOY -> {
                if (elapsed == 1) {
                    this.playSound(SoundRegistry.BURROW_BEACON_DEPLOY.get(), 1.0f, 1.0f);

                    double sphereRadius = ManifoldingCapability.BEACON_PROTECTION_RANGE;
                    double sphereRadiusSq = sphereRadius * sphereRadius;

                    List<BurrowBeaconEntity> nearby = this.level().getEntitiesOfClass(BurrowBeaconEntity.class, this.getBoundingBox().inflate(sphereRadius));

                    for (BurrowBeaconEntity other : nearby) {
                        if (other == this) continue;

                        if (this.distanceToSqr(other) <= sphereRadiusSq) {
                            this.addConnection(other.blockPosition());
                            other.addConnection(this.blockPosition());

                            ClientBoundBurrowBeaconSyncPacket.send(this);
                            ClientBoundBurrowBeaconSyncPacket.send(other);
                        }
                    }
                }

                if (this.onGround()) {
                    setAnimationState(State.LAND);
                    this.setNoGravity(true);
                    this.playSound(SoundRegistry.BURROW_BEACON_LAND.get(), 1.0f, 1.0f);
                }
            }
            case LAND -> {
                if (elapsed >= LAND_DURATION) {
                    setAnimationState(State.DRILL);
                    this.playSound(SoundRegistry.BURROW_BEACON_DRILL.get(), 1.0f, 1.0f);
                }
            }
            case DRILL -> {
                if (elapsed >= DRILL_DURATION) {
                    setAnimationState(State.DRILL_IDLE);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        this.remove(RemovalReason.KILLED);
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            for (BlockPos pos : getConnectedPositions()) {
                List<BurrowBeaconEntity> list = this.level().getEntitiesOfClass(BurrowBeaconEntity.class, new AABB(pos).inflate(0.5));

                for (BurrowBeaconEntity other : list) {
                    if (other != this) {
                        other.removeConnection(this.blockPosition());
                        ClientBoundBurrowBeaconSyncPacket.send(other);
                    }
                }
            }
        }
        super.remove(reason);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override public EntityDimensions getDimensions(Pose pose) {
        return new EntityDimensions(4.0F, 2.0F, false);

    }

    @Override public boolean isPushable() {
        return false;

    }

    @Override public boolean isPickable() {
        return true;

    }

    @Override public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(ANIMATION_STATE, tag.getInt("AnimationState"));
        this.entityData.set(ANIM_START_TIME, tag.getInt("AnimationStartTime"));
        this.entityData.set(CONNECTIONS, tag.getCompound("Connections"));
        this.entityData.set(PULSE_OFFSET, tag.getFloat("PulseOffset"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AnimationState", getAnimationState().ordinal());
        tag.putInt("AnimationStartTime", getAnimationStartTime());
        tag.put("Connections", this.entityData.get(CONNECTIONS));
        tag.putFloat("PulseOffset", this.entityData.get(PULSE_OFFSET));
    }
}