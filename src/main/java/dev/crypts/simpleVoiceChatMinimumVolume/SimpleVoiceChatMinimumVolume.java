package dev.crypts.simpleVoiceChatMinimumVolume;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleVoiceChatMinimumVolume implements ModInitializer {

    public static final String PLUGIN_ID = "simple-voice-chat-minimum-volume";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);
    public static final float MINIMUM_VOLUME = 0.05f;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Simple Voice Chat Minimum Volume");
    }
}
