package me.neoblade298.neorogue.session;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SessionSetting {
    public static HashMap<Integer, SessionSetting> settings = new HashMap<Integer, SessionSetting>();

    protected SettingEffect effect;
    protected final String title;
    private SettingIconUpdater iconUpdater;
    private SettingValueRetriever valueRetriever;

    static {
        settings.put(0, new SessionSetting("Endless Mode", (s, value) -> {
            s.setEndless(!s.isEndless());
        }, (s) -> {
            ItemStack icon = new ItemStack(Material.SCULK_CATALYST);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text("Endless Mode", NamedTextColor.GOLD));
            ArrayList<Component> lore = new ArrayList<Component>();
            if (s.isEndless()) {
                lore.add(Component.text("Enabled: ", NamedTextColor.WHITE)
                        .append(Component.text("Yes", NamedTextColor.GREEN)));
            }
            else {
                lore.add(Component.text("Enabled: ", NamedTextColor.WHITE)
                        .append(Component.text("No", NamedTextColor.RED)));
            }
            lore.add(Component.text("Enable to repeatedly cycle through regions.", NamedTextColor.GRAY));
            meta.lore(lore);
            if (s.isEndless()) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            icon.setItemMeta(meta);
            NBTItem nbti = new NBTItem(icon);
            nbti.setInteger("id", 0);
            return nbti.getItem();
        },
        (s) -> {
            return s.isEndless() ? 1 : 0;
        }));

        settings.put(18, new SessionNotorietySetting(18, "Enemy Health Scaling",
                Component.text("Enemy health scales up an additional ", NamedTextColor.GRAY)
                        .append(Component.text("5%", NamedTextColor.YELLOW))
                        .append(Component.text(" per level for every area you visit.")),
                Material.GREEN_DYE, 5, (s, level) -> {
                    s.setEnemyHealthScale(level * 0.05);
                }));

        settings.put(19, new SessionNotorietySetting(19, "Enemy Damage Scaling",
                Component.text("Enemy damage scales up an additional ", NamedTextColor.GRAY)
                        .append(Component.text("1%", NamedTextColor.YELLOW))
                        .append(Component.text(" per level for every area you visit.")),
                Material.RED_DYE, 5, (s, level) -> {
                    s.setEnemyDamageScale(level * 0.01);
                }));

        settings.put(20, new SessionNotorietySetting(20, "Coin Reduction", Component.text("Fights give ", NamedTextColor.GRAY)
                .append(Component.text("10%", NamedTextColor.YELLOW)).append(Component.text(" fewer coins per level.")),
                Material.GOLD_NUGGET, 3, (s, level) -> {
                    s.setGoldReduction(level * 0.1);
                }));

        settings.put(21, new SessionNotorietySetting(21, "Fight Time Reduction",
                Component.text("Decrease the time limit for fights by ", NamedTextColor.GRAY)
                        .append(Component.text("10%", NamedTextColor.YELLOW)).append(Component.text(" per level.")),
                Material.CLOCK, 3, (s, level) -> {
                    s.setFightTimeReduction(level * 0.1);
                }));
    }

    // Used for notoriety settings
    public SessionSetting(String title, SettingEffect effect) {
        this.title = title;
        this.effect = effect;
    }

    // Used for generic settings
    public SessionSetting(String title, SettingEffect effect, SettingIconUpdater iconUpdater, SettingValueRetriever valueRetriever) {
        this.title = title;
        this.effect = effect;
        this.iconUpdater = iconUpdater;
        this.valueRetriever = valueRetriever;
    }

    public ItemStack getItem(Session s) {
        return iconUpdater.onChange(s);
    }

    public static interface SettingEffect {
        public void onChange(Session s, int value);
    }

    public static interface SettingIconUpdater {
        public ItemStack onChange(Session s);
    }

    public static interface SettingValueRetriever {
        public int get(Session s);
    }

    public static SessionSetting getById(int id) {
        return settings.get(id);
    }
    public void leftClick(Session s) {
        effect.onChange(s, 0);
    }
    public void rightClick(Session s) {
        effect.onChange(s, 0);
    }
    // Only used for differentials in Session Settings inventory
    public int getValue(Session s) {
        return valueRetriever.get(s);
    }
    public String getTitle() {
        return title;
    }
}
