package me.neoblade298.neorogue.session.fight.buff;

// Which fight-statistics column a StatTracker's contribution belongs to.
// Set explicitly by each StatTracker factory - never inferred from the display suffix.
public enum StatCategory {
    DAMAGE_DEALT,
    DAMAGE_TAKEN,
    STATUS,
    OTHER;
}
