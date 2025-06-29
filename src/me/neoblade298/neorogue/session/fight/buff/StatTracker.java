package me.neoblade298.neorogue.session.fight.buff;

import java.util.HashMap;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StatTracker {
    private Component display;
    private String id;
    private boolean invert, ignore; // Quick easy way to invert the stat number

    private static HashMap<StatusType, BuffStatTracker> statusOrigins = new HashMap<StatusType, BuffStatTracker>();

    static {
        for (StatusType type : new StatusType[] { StatusType.STRENGTH, StatusType.INTELLECT }) {
            statusOrigins.put(type, new BuffStatTracker(type, true));
        }
        for (StatusType type : new StatusType[] { StatusType.PROTECT, StatusType.SHELL }) {
            statusOrigins.put(type, new BuffStatTracker(type, false));
        }
        
        // Inverted statuses (negative defense buff or negative damage buff)
        statusOrigins.put(StatusType.FROST, new BuffStatTracker(StatusType.FROST, false, true));
        statusOrigins.put(StatusType.BURN, new BuffStatTracker(StatusType.BURN, true, true));
        statusOrigins.put(StatusType.CONCUSSED, new BuffStatTracker(StatusType.CONCUSSED, false, true));
        statusOrigins.put(StatusType.INSANITY, new BuffStatTracker(StatusType.INSANITY, true, true));
    }
    
    protected StatTracker(String id, Component display) {
        this.display = display;
        this.id = id;
    }
    
    protected StatTracker(String id, Component display, boolean invert) {
        this.display = display;
        this.id = id;
        this.invert = invert;
    }

    protected StatTracker(String id, Equipment eq) {
        this.id = id;
        this.ignore = true;
    }
    
    protected StatTracker(String id, Equipment eq, String sfx) {
        this.id = id;
        this.display = eq.getDisplay().append(Component.text(" - " + sfx, NamedTextColor.GRAY));
    }
    
    // Inverted 
    protected StatTracker(String id, Equipment eq, String sfx, boolean invert) {
        this.id = id;
        this.display = eq.getDisplay().append(Component.text(" - " + sfx, NamedTextColor.GRAY));
        this.invert = invert;
    }
    
    protected StatTracker(String id) {
        this.id = id;
        this.ignore = true;
    }

    public static BuffStatTracker of(StatusType type) {
        return statusOrigins.get(type);
    }

    public boolean isSimilar(StatTracker other) {
        return this.id != null && this.id.equals(other.id);
    }

    public String getId() {
        return id;
    }

    public boolean isInverted() {
        return invert;
    }
    
    public boolean isIgnored() {
        return ignore;
    }

    public static StatTracker of(String id, Equipment eq, String statTitle) {
        return new StatTracker(id, eq, statTitle);
    }

    public static StatTracker ignored(String id) {
        return new StatTracker(id);
    }

    public static StatTracker ignored(String id, Equipment eq) {
        return new StatTracker(id, eq);
    }

    public static BuffStatTracker damageBuffAlly(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Damage Buffed", true);
    }

    public static BuffStatTracker damageBuffAlly(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Damage Buffed", shouldCombine);
    }

    public static BuffStatTracker damageDebuffAlly(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Damage Reduced", true);
    }

    public static BuffStatTracker damageDebuffAlly(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Damage Reduced", shouldCombine);
    }

    public static BuffStatTracker defenseDebuffAlly(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Self Defense Reduced", true);
    }

    public static BuffStatTracker defenseDebuffAlly(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Self Defense Reduced", shouldCombine);
    }

    public static BuffStatTracker defenseBuffAlly(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Damage Mitigated", true);
    }

    public static BuffStatTracker defenseBuffAlly(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Damage Mitigated", shouldCombine);
    }

    public static BuffStatTracker damageDebuffEnemy(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Damage Mitigated", true, true);
    }

    public static BuffStatTracker damageDebuffEnemy(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Damage Mitigated", true, shouldCombine);
    }

    public static BuffStatTracker defenseDebuffEnemy(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Defense Debuffed", true, true);
    }

    public static BuffStatTracker defenseDebuffEnemy(String id, Equipment eq, boolean shouldCombine) {
        return new BuffStatTracker(id, eq, "Defense Debuffed", true, shouldCombine);
    }

    public static BuffStatTracker damageBarriered(String id, Equipment eq) {
        return new BuffStatTracker(id, eq, "Damage Barriered");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatTracker other = (StatTracker) obj;
        if (display == null) {
            if (other.display != null)
                return false;
        } else if (!display.equals(other.display))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Component getDisplay() {
        return display;
    }

    
}
