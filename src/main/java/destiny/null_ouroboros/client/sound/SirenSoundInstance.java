package destiny.null_ouroboros.client.sound;

import destiny.null_ouroboros.server.block.entity.MechanicalSirenBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SirenSoundInstance extends AbstractTickableSoundInstance {
    private final MechanicalSirenBlockEntity blockEntity;
    private final float minDist;
    private final float maxDist;
    private final boolean distantLayer;

    public SirenSoundInstance(SoundEvent soundEvent, SoundSource source, MechanicalSirenBlockEntity blockEntity, boolean looping,
                              float minDist, float maxDist, boolean distantLayer) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.blockEntity = blockEntity;
        this.looping = looping;
        this.relative = false;
        this.x = blockEntity.getBlockPos().getX() + 0.5;
        this.y = blockEntity.getBlockPos().getY() + 0.5;
        this.z = blockEntity.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.NONE;
        this.volume = 0f;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.distantLayer = distantLayer;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved()) {
            stop();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isRemoved()) {
            this.volume = 0f;
            return;
        }

        double dist = Math.sqrt(player.distanceToSqr(x, y, z));
        float vol = distantLayer ? getDistantVolume(dist) : getNormalVolume(dist);
        this.volume = Mth.clamp(vol, 0f, 1f);
    }

    private float getNormalVolume(double dist) {
        if (dist <= minDist) return 1f;
        if (dist >= maxDist) return 0f;
        return 1f - (float) ((dist - minDist) / (maxDist - minDist));
    }

    private float getDistantVolume(double dist) {
        if (dist <= minDist || dist >= maxDist) return 0f;

        double midpoint = (minDist + maxDist) / 2d;
        if (dist <= midpoint) {
            return (float) ((dist - minDist) / (midpoint - minDist));
        }
        return 1f - (float) ((dist - midpoint) / (maxDist - midpoint));
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
