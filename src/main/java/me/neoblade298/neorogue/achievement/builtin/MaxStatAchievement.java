package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MaxStatAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1 };

	public enum StatType {
		HEALTH, MANA, STAMINA
	}

	private final String id;
	private final Component displayName;
	private final Material material;
	private final StatType statType;
	private final double threshold;

	public MaxStatAchievement(String id, Component displayName, Material material, StatType statType, double threshold) {
		this.id = id;
		this.displayName = displayName;
		this.material = material;
		this.statType = statType;
		this.threshold = threshold;
	}

	@Override
	public String getId() {
		return id;
	}

    @Override
    public int getSortPriority() {
        return 40 + statType.ordinal();
    }

	@Override
	public Component getDisplayName() {
		return displayName;
	}

	@Override
	public Material getMaterial() {
		return material;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		String statName = switch (statType) {
			case HEALTH -> "HP";
			case MANA -> "Mana";
			case STAMINA -> "Stamina";
		};
		return List.of(Component.text("Reach " + (int) threshold + " max " + statName + " in a run.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(id, SessionTrigger.ACQUIRE_EQUIPMENT, (pdata, in) -> {
			double current = switch (statType) {
				case HEALTH -> pdata.getMaxHealth();
				case MANA -> pdata.getMaxMana();
				case STAMINA -> pdata.getMaxStamina();
			};
			if (current >= threshold) {
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
		});
	}
}
