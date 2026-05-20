package me.neoblade298.neorogue.session;

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

public class SessionNotorietySetting extends SessionSetting{
    public static ArrayList<SessionSetting> settings = new ArrayList<SessionSetting>();
    private final int id;
    protected String title;
    protected TextComponent desc;
    protected Material mat;
    protected int maxLevel;
    protected ItemStack icon;
    protected SettingEffect effect;

    static {
    }

    public SessionNotorietySetting(int id, String title, TextComponent desc, Material mat, int maxLevel, SettingEffect effect, SettingValueRetriever valueRetriever) {
        super(title, effect, valueRetriever);
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.mat = mat;
        this.maxLevel = maxLevel;
        this.effect = effect;
    }

    @Override
    public ItemStack getItem(Session s) {
        icon = CoreInventory.createButton(mat, Component.text(title, NamedTextColor.GOLD));
        icon.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        int level = getValue(s);
        if (level > 0) {
            icon.setAmount(level);
            ItemMeta meta = icon.getItemMeta();
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            icon.setItemMeta(meta);
        }

        ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
        lore.add(Component.text("Level: ", NamedTextColor.YELLOW)
                .append(Component.text(level + " / " + maxLevel, NamedTextColor.WHITE)));
        lore.add(desc);
        icon.lore(lore);
        NBTItem nbti = new NBTItem(icon);
        nbti.setInteger("id", id);
        return nbti.getItem();
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getValue(Session s) {
        return valueRetriever.get(s);
    }

    public static SessionSetting getById(int id) {
        return settings.get(id);
    }

    @Override
    public boolean canLeftClick(Session s) {
        return getValue(s) < maxLevel;
    }

    @Override
    public boolean canRightClick(Session s) {
        return getValue(s) > 0;
    }
}
