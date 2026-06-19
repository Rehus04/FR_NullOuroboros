package destiny.null_ouroboros.client.sound;

import destiny.null_ouroboros.server.capability.ClientManifoldingHolder;
import destiny.null_ouroboros.server.capability.ManifoldingPhase;
import destiny.null_ouroboros.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;

public class ManifoldingSoundManager {
    private static final int START_DELAY = 20 * 5;
    private static final int END_BUFFER = 20 * 6;

    private static final float START_NORMAL_VOL = 0.8f, START_MUFFLED_VOL = 0.3f;
    private static final float LOOP_NORMAL_VOL = 0.8f, LOOP_MUFFLED_VOL = 0.3f;
    private static final float END_NORMAL_VOL = 0.8f, END_MUFFLED_VOL = 0.3f;

    private static final SoundPair startPair = new SoundPair(
            SoundRegistry.MANIFOLDING_START.getId(), SoundRegistry.MANIFOLDING_START_MUFFLED.getId(),
            START_NORMAL_VOL, START_MUFFLED_VOL, false);
    private static final SoundPair loopPair = new SoundPair(
            SoundRegistry.MANIFOLDING_LOOP.getId(), SoundRegistry.MANIFOLDING_LOOP_MUFFLED.getId(),
            LOOP_NORMAL_VOL, LOOP_MUFFLED_VOL, true);
    private static final SoundPair endPair = new SoundPair(
            SoundRegistry.MANIFOLDING_END.getId(), SoundRegistry.MANIFOLDING_END_MUFFLED.getId(),
            END_NORMAL_VOL, END_MUFFLED_VOL, false);

    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;

        ClientManifoldingHolder.updateExposure();
        float exposure = ClientManifoldingHolder.getExposureLevel();

        ManifoldingPhase phase = ClientManifoldingHolder.getPhase();
        long now = mc.level.getGameTime();
        long elapsed = now - ClientManifoldingHolder.getPhaseStartTime();
        int preDur = ClientManifoldingHolder.getPreDuration();
        int activeDur = ClientManifoldingHolder.getActiveDuration();

        boolean playStart = false, playLoop = false, playEnd = false;

        switch (phase) {
            case PRE_EVENT:
                if (preDur > 0 && (preDur - elapsed) <= START_DELAY && (preDur - elapsed) > 0)
                    playStart = true;
                break;
            case ACTIVE:
                playLoop = true;
                if (activeDur > 0 && (activeDur - elapsed) <= END_BUFFER && (activeDur - elapsed) > 0)
                    playEnd = true;
                break;
        }

        startPair.update(mc, playStart, exposure);
        loopPair.update(mc, playLoop, exposure);
        endPair.update(mc, playEnd, exposure);
    }

    private static class SoundPair {
        MutableSoundInstance normal, muffled;
        final ResourceLocation normalId, muffledId;
        final float normalBaseVol, muffledBaseVol;
        final boolean looping;

        SoundPair(ResourceLocation normalId, ResourceLocation muffledId, float nVol, float mVol, boolean looping) {
            this.normalId = normalId;
            this.muffledId = muffledId;
            this.normalBaseVol = nVol;
            this.muffledBaseVol = mVol;
            this.looping = looping;
        }

        void update(Minecraft mc, boolean active, float exposure) {
            if (active) {
                if (normal == null) {
                    normal = new MutableSoundInstance(normalId, SoundSource.AMBIENT, looping);
                    muffled = new MutableSoundInstance(muffledId, SoundSource.AMBIENT, looping);
                    mc.getSoundManager().play(normal);
                    mc.getSoundManager().play(muffled);
                }

                float nVol = exposure * normalBaseVol;
                float mVol = (1.0f - exposure) * muffledBaseVol;

                normal.setVolume(nVol);
                muffled.setVolume(mVol);

                mc.getSoundManager().updateSourceVolume(normal.getSource(), nVol);
                mc.getSoundManager().updateSourceVolume(muffled.getSource(), mVol);
            } else {
                if (normal != null) {
                    if (looping) {
                        mc.getSoundManager().stop(normal);
                        mc.getSoundManager().stop(muffled);

                        normal = null;
                        muffled = null;
                    } else {
                        if (!mc.getSoundManager().isActive(normal)) {
                            normal = null;
                            muffled = null;
                        }
                    }
                }
            }
        }
    }
}