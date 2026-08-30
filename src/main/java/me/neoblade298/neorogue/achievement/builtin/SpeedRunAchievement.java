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

public class SpeedRunAchievement implements Achievement {
	private static final int[] THRESHOLDS = { 1, 2, 3 };
	private static final int[] MINUTES = { 90, 60, 30 };
	private final boolean notorious;

	public SpeedRunAchievement(boolean notorious) {
		this.notorious = notorious;
	}

	@Override public String getId() { return notorious ? "notorious_pace" : "against_the_clock"; }
	@Override public Component getDisplayName() {
		return Component.text(notorious ? "Notorious Pace" : "Against the Clock", NamedTextColor.GOLD);
	}
	@Override public Material getMaterial() { return notorious ? Material.RECOVERY_COMPASS : Material.CLOCK; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int index = Math.min(mastery, MINUTES.length - 1);
		String suffix = notorious ? " at notoriety 10 or higher." : ".";
		return List.of(Component.text("Win a run in under " + MINUTES[index] + " minutes" + suffix, NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(getId(), SessionTrigger.WIN_RUN, (pdata, in) -> {
			if (notorious && session.getNotoriety() < 10) return TriggerResult.keep();
			long playtime = session.getPlaytime();
			int achieved = playtime < 30 * 60_000L ? 3 : playtime < 60 * 60_000L ? 2 : playtime < 90 * 60_000L ? 1 : 0;
			while (progress.getProgress() < achieved) {
				if (progress.addProgress(1)) AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
