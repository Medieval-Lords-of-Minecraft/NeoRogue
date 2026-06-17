package me.neoblade298.neorogue.session.settings;

import java.util.ArrayList;

import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.TextComponent;

public abstract class NotorietySetting {
    public static ArrayList<NotorietySetting> settings = new ArrayList<NotorietySetting>();
    private int level; // Should be 1-10, no two settings should have the same level
    protected TextComponent header, lore;

    static {
        register(IncreaseDamageNotorietySetting.getInstance());
    }

    public NotorietySetting(TextComponent header, TextComponent lore) {
        this.header = header;
        this.lore = lore;
    }

    public static void register(NotorietySetting setting) {
        settings.add(setting);
        setting.setLevel(settings.size());
    }

    public TextComponent getHeader() {
        return header;
    }

    public TextComponent getLore() {
        return lore;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isActive(Session s) {
        return getLevel() <= s.getNotoriety();
    }
}
