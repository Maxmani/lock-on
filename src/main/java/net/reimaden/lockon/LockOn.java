package net.reimaden.lockon;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.reimaden.lockon.config.LockOnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockOn implements ClientModInitializer {

	public static final String MOD_ID = "lockon";
	public static final String MOD_NAME = "Lock On";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(LockOnConfig.class, GsonConfigSerializer::new);
		LockOnHandler.registerKeys();
	}
}