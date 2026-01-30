package me.neoblade298.neorogue.session;

import java.text.DecimalFormat;
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

    private static final DecimalFormat df = new DecimalFormat("#");
    protected SettingEffect effect;
    protected final String title;
    protected SettingValueRetriever valueRetriever;
    private SettingIconUpdater iconUpdater;

    static {
        settings.put(0, new SessionSetting("Endless Mode", (s, leftClick) -> {
                s.setEndless(!s.isEndless());
            },
            (s) -> {
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
                        .append(Component.text(df.format(Session.ENEMY_HEALTH_SCALE_PER_LEVEL) + "%", NamedTextColor.YELLOW))
                        .append(Component.text(" per level for every region you visit.")),
                Material.GREEN_DYE, 5, (s, leftClick) -> {
                    s.setEnemyHealthScale(s.getEnemyHealthScale() + (leftClick ? 1 : -1));
                },
                (s) -> {
                    return s.getEnemyHealthScale();
                }));

        settings.put(19, new SessionNotorietySetting(19, "Enemy Damage Scaling",
                Component.text("Enemy damage scales up an additional ", NamedTextColor.GRAY)
                        .append(Component.text(df.format(Session.ENEMY_DAMAGE_SCALE_PER_LEVEL) + "%", NamedTextColor.YELLOW))
                        .append(Component.text(" per level for every region you visit.")),
                Material.RED_DYE, 5, (s, leftClick) -> {
                    s.setEnemyDamageScale(s.getEnemyDamageScale() + (leftClick ? 1 : -1));
                },
                (s) -> {
                    return s.getEnemyDamageScale();
                }));

        settings.put(20, new SessionNotorietySetting(20, "Coin Reduction", Component.text("Fights give ", NamedTextColor.GRAY)
                .append(Component.text(df.format(Session.COIN_REDUCTION_PER_LEVEL) + "%", NamedTextColor.YELLOW)).append(Component.text(" fewer coins per level.")),
                Material.GOLD_NUGGET, 5, (s, leftClick) -> {
                    s.setCoinReduction(s.getCoinReduction() + (leftClick ? 1 : -1));
                },
                (s) -> {
                    return s.getCoinReduction();
                }));

        settings.put(21, new SessionNotorietySetting(21, "Fight Time Reduction",
                Component.text("Decrease the time limit for fights by ", NamedTextColor.GRAY)
                        .append(Component.text(df.format(Session.FIGHT_TIME_REDUCTION_PER_LEVEL) + "%", NamedTextColor.YELLOW)).append(Component.text(" per level.")),
                Material.CLOCK, 5, (s, leftClick) -> {
                    s.setFightTimeReduction(s.getFightTimeReduction() + (leftClick ? 1 : -1));
                },
                (s) -> {
                    return s.getFightTimeReduction();
                }));
    }

    // Used for notoriety settings
    public SessionSetting(String title, SettingEffect effect, SettingValueRetriever valueRetriever) {
        this.title = title;
        this.effect = effect;
        this.valueRetriever = valueRetriever;
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
        public void onChange(Session s, boolean leftClick);
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
        effect.onChange(s, true);
    }
    public void rightClick(Session s) {
        effect.onChange(s, false);
    }
    public int getValue(Session s) {
        return valueRetriever.get(s);
    }
    public String getTitle() {
        return title;
    }
    public boolean canLeftClick(Session s) {
        return true;
    }
    public boolean canRightClick(Session s) {
        return true;
    }
}
