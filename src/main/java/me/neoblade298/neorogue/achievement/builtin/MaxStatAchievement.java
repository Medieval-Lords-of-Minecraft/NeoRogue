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
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MaxStatAchievement implements Achievement {
	public enum StatType {
		HEALTH, MANA, STAMINA
	}

	private final String id;
	private final Component displayName;
	private final Material material;
	private final StatType statType;
	private final int[] thresholds;

	public MaxStatAchievement(String id, Component displayName, Material material, StatType statType, int... thresholds) {
		this.id = id;
		this.displayName = displayName;
		this.material = material;
		this.statType = statType;
		this.thresholds = thresholds;
	}

	@Override
	public String getId() {
		return id;
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
		return thresholds;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		String statName = switch (statType) {
			case HEALTH -> "HP";
			case MANA -> "Mana";
			case STAMINA -> "Stamina";
		};
		int target = mastery < thresholds.length ? thresholds[mastery] : thresholds[thresholds.length - 1];
		return List.of(Component.text("Reach " + target + " max " + statName + " in a run.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		check(data, progress);
		data.addTrigger(id, SessionTrigger.MAX_STAT_CHANGED, (pdata, in) -> {
			check(pdata, progress);
			return TriggerResult.keep();
		});
	}

	private void check(PlayerSessionData pdata, AchievementProgress progress) {
			double current = switch (statType) {
				case HEALTH -> pdata.getMaxHealth();
				case MANA -> pdata.getMaxMana();
				case STAMINA -> pdata.getMaxStamina();
			};
			int best = (int) Math.floor(current);
			if (best > progress.getProgress() && progress.addProgress(best - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
	}
}
