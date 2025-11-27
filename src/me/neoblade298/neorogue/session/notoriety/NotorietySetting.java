package me.neoblade298.neorogue.session.notoriety;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neorogue.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class NotorietySetting {
    public static ArrayList<NotorietySetting> settings = new ArrayList<NotorietySetting>();

    protected final int id;
    protected String title;
    protected TextComponent desc;
    protected Material mat;
    protected int level, maxLevel;
    protected ItemStack icon;
    protected NotorietyEffect effect;

    static {
        settings.add(new NotorietySetting(0, "Enemy Health Scaling",
            Component.text("Enemy health scales up an additional ", NamedTextColor.GRAY)
            .append(Component.text("5%", NamedTextColor.YELLOW)).append(Component.text( " per level for every area you visit.")),
            Material.GREEN_DYE, 5, (s, level) -> {
                    s.setEnemyHealthScale(level * 0.05);
            }));
            
        settings.add(new NotorietySetting(1, "Enemy Damage Scaling",
                Component.text("Enemy damage scales up an additional ", NamedTextColor.GRAY)
                        .append(Component.text("1%", NamedTextColor.YELLOW))
                        .append(Component.text(" per level for every area you visit.")),
                Material.RED_DYE, 5, (s, level) -> {
                    s.setEnemyDamageScale(level * 0.01);
                }));

        settings.add(new NotorietySetting(2, "Coin Reduction",
                Component.text("Fights give ", NamedTextColor.GRAY)
                        .append(Component.text("10%", NamedTextColor.YELLOW))
                        .append(Component.text(" fewer coins per level.")),
                Material.GOLD_NUGGET, 3, (s, level) -> {
                    s.setGoldReduction(level * 0.1);
                }));

        settings.add(new NotorietySetting(3, "Fight Time Reduction", Component.text("Decrease the time limit for fights by ", NamedTextColor.GRAY)
                .append(Component.text("10%", NamedTextColor.YELLOW)).append(Component.text(" per level.")),
                Material.CLOCK, 3, (s, level) -> {
                    s.setFightTimeReduction(level * 0.1);
                }));
    }

    public NotorietySetting(int id, String title, TextComponent desc, Material mat, int maxLevel, NotorietyEffect effect) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.mat = mat;
        this.maxLevel = maxLevel;
        this.effect = effect;
    }

    public ItemStack getItem() {
        icon = CoreInventory.createButton(mat, Component.text(title, NamedTextColor.GOLD));
        icon.addEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
        icon.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        NBTItem nbti = new NBTItem(icon);
        nbti.setInteger(title, null);
        return icon;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevel() {
        return level;
    }

    public void increase(Session s) {
        level++;
        effect.onChangeLevel(s, level);
        updateIcon();
    }

    public void decrease(Session s) {
        level--;
        effect.onChangeLevel(s, level);
        updateIcon();
    }

    private ItemStack updateIcon() {
        if (level == 0) {
            icon.removeEnchantments();
        }
        else {
            icon.addEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
            icon.setAmount(level);
        }
        
        ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
        lore.add(Component.text("Level: ", NamedTextColor.YELLOW)
                .append(Component.text(level + " / " + maxLevel, NamedTextColor.WHITE)));
        lore.add(desc);
        icon.lore(lore);
        return icon;
    }

    public static interface NotorietyEffect {
        public void onChangeLevel(Session s, int level);
    }

    public static NotorietySetting getById(int id) {
        return settings.get(id);
    }
}
