package destiny.null_ouroboros.client.network;

import destiny.null_ouroboros.server.capability.ClientManifoldingHolder;
import destiny.null_ouroboros.server.capability.ManifoldingPhase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundManifoldingPacket {
    private final ManifoldingPhase phase;
    private final float windStrength;
    private final float windAngle;
    private final float thunderPulse;
    private final int riftTicks;
    private final float lightDim;
    private final long phaseStartTime;
    private final int preEventDuration;
    private final int activeDuration;
    private final int postEventDuration;
    private final boolean exposed;

    public ClientBoundManifoldingPacket(ManifoldingPhase phase, float windStrength, float windAngle, float thunderPulse, int pulseTicks, int riftTicks,
                                        float lightDim, long phaseStartTime, int preEventDuration, int activeDuration, int postEventDuration, boolean exposed) {
        this.phase = phase;
        this.windStrength = windStrength;
        this.windAngle = windAngle;
        this.thunderPulse = thunderPulse;
        this.riftTicks = riftTicks;
        this.lightDim = lightDim;
        this.phaseStartTime = phaseStartTime;
        this.preEventDuration = preEventDuration;
        this.activeDuration = activeDuration;
        this.postEventDuration = postEventDuration;
        this.exposed = exposed;
    }

    public ClientBoundManifoldingPacket(FriendlyByteBuf buffer) {
        this.phase = ManifoldingPhase.valueOf(buffer.readUtf());
        this.windStrength = buffer.readFloat();
        this.windAngle = buffer.readFloat();
        this.thunderPulse = buffer.readFloat();
        this.riftTicks = buffer.readInt();
        this.lightDim = buffer.readFloat();
        this.phaseStartTime = buffer.readLong();
        this.preEventDuration = buffer.readVarInt();
        this.activeDuration = buffer.readVarInt();
        this.postEventDuration = buffer.readVarInt();
        this.exposed = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(phase.name());
        buffer.writeFloat(windStrength);
        buffer.writeFloat(windAngle);
        buffer.writeFloat(thunderPulse);
        buffer.writeInt(riftTicks);
        buffer.writeFloat(lightDim);
        buffer.writeLong(phaseStartTime);
        buffer.writeVarInt(preEventDuration);
        buffer.writeVarInt(activeDuration);
        buffer.writeVarInt(postEventDuration);
        buffer.writeBoolean(exposed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientManifoldingHolder.set(phase, windStrength, windAngle, thunderPulse, riftTicks, lightDim, phaseStartTime, preEventDuration, activeDuration, postEventDuration, exposed);
        });
        return true;
    }
}
