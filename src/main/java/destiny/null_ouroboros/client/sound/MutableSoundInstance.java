package destiny.null_ouroboros.client.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.concurrent.CompletableFuture;

public class MutableSoundInstance extends AbstractSoundInstance {
    private float volume = 1.0f;

    public MutableSoundInstance(ResourceLocation location, SoundSource source, boolean looping) {
        super(location, source, RandomSource.create());
        this.looping = looping;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
    }

    public void setVolume(float volume) {
        this.volume = Mth.clamp(volume, 0.0f, 1.0f);
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return soundBuffers.getStream(sound.getPath(), looping);
    }
}
