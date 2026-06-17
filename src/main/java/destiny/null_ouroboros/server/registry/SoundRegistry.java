package destiny.null_ouroboros.server.registry;

import destiny.null_ouroboros.NullOuroboros;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NullOuroboros.MODID);

    public static final RegistryObject<SoundEvent> DROPLIGHT_ON = registerSoundEvent("droplight_on");
    public static final RegistryObject<SoundEvent> DROPLIGHT_OFF = registerSoundEvent("droplight_off");
    public static final RegistryObject<SoundEvent> DROPLIGHT_FLICKER = registerSoundEvent("droplight_flicker");
    public static final RegistryObject<SoundEvent> DROPLIGHT_DROP = registerSoundEvent("droplight_drop");

    public static final RegistryObject<SoundEvent> STROBELIGHT_ALARM = registerSoundEvent("strobelight_alarm");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound) {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NullOuroboros.MODID, sound)));
    }
}
