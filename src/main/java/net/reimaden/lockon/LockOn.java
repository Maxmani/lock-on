package net.reimaden.lockon;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.reimaden.lockon.config.LockOnConfig;

public class LockOn implements ClientModInitializer {

	public static final String MOD_ID = "lockon";
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(LockOnConfig.class, GsonConfigSerializer::new);
		LockOnHandler.registerKeys();
	}
}