package net.reimaden.lockon.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.reimaden.lockon.LockOn;

@Config(name = LockOn.MOD_ID)
public class LockOnConfig implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 8, max = 96)
    @ConfigEntry.Gui.Tooltip
    public int range = 16;

    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();
        this.validate();
    }

    public void validate() {
        if (range < 8 || range > 96) {
            range = 16;
            LockOn.LOGGER.warn("Lock on range is out of bounds, resetting to default value");
        }
    }
}
