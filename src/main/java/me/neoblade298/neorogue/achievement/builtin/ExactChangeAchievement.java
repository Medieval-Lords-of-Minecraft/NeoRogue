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

public class ExactChangeAchievement implements Achievement {
	private static final String ID = "exact_change";
	private static final int[] THRESHOLDS = { 1 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Exact Change", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.GOLD_NUGGET; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.SESSION); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Leave a shop with exactly 0 " + PlayerSessionData.CURRENCY + ".", NamedTextColor.GRAY));
	}

	@Override
	public void registerSession(Session session, PlayerSessionData data, AchievementProgress progress) {
		data.addTrigger(ID, SessionTrigger.LEAVE_SHOP, (pdata, in) -> {
			if (pdata.getCurrency() == 0 && progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
