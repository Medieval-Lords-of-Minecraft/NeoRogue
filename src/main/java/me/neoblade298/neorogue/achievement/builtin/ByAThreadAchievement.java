package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ByAThreadAchievement implements Achievement {
	private static final String ID = "by_a_thread";
	private static final int[] THRESHOLDS = { 1 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("By a Thread", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.SPIDER_EYE; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }
	@Override public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Win a fight alive with less than 10 HP.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (!pdata.isDead() && pdata.getPlayer().getHealth() > 0 && pdata.getPlayer().getHealth() < 10
					&& progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
