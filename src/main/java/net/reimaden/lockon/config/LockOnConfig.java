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

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean enableForInteractionEntities = true;

    @Override
    public void validatePostLoad() throws ValidationException {
        this.validate();
    }

    public void validate() throws ValidationException {
        if (range < 8 || range > 96) {
            range = 16;
            throw new ValidationException("Range must be between 8 and 96");
        }
    }
}
