package dev.crypts.simpleVoiceChatMinimumVolume;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class SimpleVoiceChatMinimumVolumePlugin implements VoicechatPlugin {

    private double voiceChatDistance;

    private OpusDecoder decoder;
    private OpusEncoder encoder;

    @Override
    public String getPluginId() {
        return SimpleVoiceChatMinimumVolume.PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.voiceChatDistance = api.getVoiceChatDistance();
        this.decoder = api.createDecoder();
        this.encoder = api.createEncoder();
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::onLocationalSoundPacket);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;

        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof ServerPlayerEntity speaker)) return;

        Vec3d speakerPos = speaker.getEntityPos();

        for (ServerPlayerEntity listener : Objects.requireNonNull(speaker.getEntityWorld().getServer())
                .getPlayerManager().getPlayerList()) {

            if (listener.getUuid().equals(speaker.getUuid())) continue;

            VoicechatConnection listenerConnection = event.getVoicechat().getConnectionOf(listener.getUuid());
            if (listenerConnection == null) continue;

            double distance = speakerPos.distanceTo(listener.getEntityPos());

            if (distance <= this.voiceChatDistance * (1F - SimpleVoiceChatMinimumVolume.MINIMUM_VOLUME)) continue;

            float distanceVolume = (float) (1F - distance / this.voiceChatDistance);
            float finalVolume = Math.max(distanceVolume, SimpleVoiceChatMinimumVolume.MINIMUM_VOLUME);

            short[] pcm = decoder.decode(event.getPacket().getOpusEncodedData());

            for (int i = 0; i < pcm.length; i++) {
                pcm[i] = (short) (pcm[i] * finalVolume);
            }

            byte[] scaledOpus = encoder.encode(pcm);

            StaticSoundPacket staticSoundPacket = event.getPacket().staticSoundPacketBuilder()
                    .opusEncodedData(scaledOpus)
                    .build();

            event.getVoicechat().sendStaticSoundPacketTo(listenerConnection, staticSoundPacket);
        }
    }

    private void onLocationalSoundPacket(LocationalSoundPacketEvent event) {
        if (event.getSenderConnection() == null || event.getReceiverConnection() == null) return;

        float distance = event.getPacket().getDistance();
        float volume = 1F - distance / (float) this.voiceChatDistance;

        if (volume <= SimpleVoiceChatMinimumVolume.MINIMUM_VOLUME) event.cancel();
    }
}
