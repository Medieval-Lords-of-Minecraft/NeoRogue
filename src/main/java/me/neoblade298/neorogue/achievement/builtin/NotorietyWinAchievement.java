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

// Tracks the highest notoriety a run was won at. Mastery tiers correspond to winning at
// notoriety 1, 5, and 10.
public class NotorietyWinAchievement implements Achievement {
	private static final String ID = "notorious_victor";
	private static final int[] THRESHOLDS = { 1, 5, 10 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Notorious Victor", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.NETHER_STAR;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Win a run at notoriety " + target + ".", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.SESSION);
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.WIN_RUN, (pdata, in) -> {
			// Progress tracks the best notoriety ever won at, so mastery reflects the highest tier reached.
			int notoriety = session.getNotoriety();
			if (notoriety > progress.getProgress()) {
				if (progress.addProgress(notoriety - progress.getProgress())) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
