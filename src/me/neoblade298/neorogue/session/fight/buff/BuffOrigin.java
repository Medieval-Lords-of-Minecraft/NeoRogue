import java.util.HashMap;

import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BuffOrigin {
    private Component display;
    private String id;

    private static HashMap<StatusType, BuffOrigin> statusOrigins = new HashMap<StatusType, BuffOrigin>();

    static {
        StatusType[] damageStatuses = new StatusType[] { StatusType.STRENGTH, StatusType.INTELLECT,
            StatusType.INSANITY };
        StatusType[] defenseStatuses = new StatusType[] { StatusType.FROST, StatusType.CONCUSSED,
            StatusType.INJURY, StatusType.PROTECT, StatusType.SHELL }
        for (StatusType type : damageStatuses) {
            statusOrigins.put(type, new BuffOrigin(type, true));
        }
        for (StatusType type : defenseStatuses) {
            statusOrigins.put(type, new BuffOrigin(type, false));
        }
    }
    
    public BuffOrigin(String id, Component display) {
        this.display = display;
        this.id = id;
    }

    private BuffOrigin(StatusType type, boolean damage) {
        this(type.name(), type.ctag.append(
            Component.text(damage ? " - Damage Buffed" : " - Damage Mitigated", NamedTextColor.GRAY)));
    }
}
