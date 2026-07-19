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

// Counts how many runs have been won at maximum notoriety (10). Mastery tiers at 3, 10, and 25 wins.
public class NotorietyTenWinsAchievement implements Achievement {
	private static final String ID = "infamous_champion";
	private static final int MAX_NOTORIETY = 10;
	private static final int[] THRESHOLDS = { 3, 10, 25 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Infamous Champion", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.WITHER_SKELETON_SKULL;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		String desc = target == 1 ? "Win a run at notoriety " + MAX_NOTORIETY + "."
				: "Win " + target + " runs at notoriety " + MAX_NOTORIETY + ".";
		return List.of(Component.text(desc, NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_RUN, (pdata, in) -> {
			if (session.getNotoriety() >= MAX_NOTORIETY) {
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
