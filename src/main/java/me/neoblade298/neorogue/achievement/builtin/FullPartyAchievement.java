package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.session.fight.BossFightInstance;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.MinibossFightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FullPartyAchievement implements Achievement {
	private static final String ID = "full_party";
	private static final int[] THRESHOLDS = { 1, 1, 1 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Strength in Numbers", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.GOLDEN_HELMET;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public int getSortPriority() {
		return 20;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(
				Component.text("Beat a fight, miniboss, and boss as a party of 4.", NamedTextColor.GRAY),
				Component.empty(),
				Component.text((progress >= 1 ? "\u2714" : "\u2718") + " Beat a fight as a party of 4", progress >= 1 ? NamedTextColor.GREEN : NamedTextColor.GRAY),
				Component.text((progress >= 2 ? "\u2714" : "\u2718") + " Beat a miniboss as a party of 4", progress >= 2 ? NamedTextColor.GREEN : NamedTextColor.GRAY),
				Component.text((progress >= 3 ? "\u2714" : "\u2718") + " Beat a boss as a party of 4", progress >= 3 ? NamedTextColor.GREEN : NamedTextColor.GRAY)
		);
	}

	@Override
	public AchievementScope getScope() {
		return AchievementScope.ALL;
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (pdata.getInstance().getSession().getParty().size() < 4) return TriggerResult.keep();

			int current = progress.getProgress();
			FightInstance inst = pdata.getInstance();

			if (current == 0) {
				// Any fight counts for tier 1
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			} else if (current == 1 && inst instanceof MinibossFightInstance) {
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			} else if (current == 2 && inst instanceof BossFightInstance) {
				if (progress.addProgress(1)) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
