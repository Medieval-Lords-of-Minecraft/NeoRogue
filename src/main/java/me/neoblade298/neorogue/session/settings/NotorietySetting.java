package me.neoblade298.neorogue.session.settings;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

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
