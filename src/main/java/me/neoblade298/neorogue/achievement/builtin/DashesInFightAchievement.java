package me.neoblade298.neorogue.achievement.builtin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import me.neoblade298.neorogue.achievement.Achievement;
import me.neoblade298.neorogue.achievement.AchievementManager;
import me.neoblade298.neorogue.achievement.AchievementProgress;
import me.neoblade298.neorogue.achievement.AchievementScope;
import me.neoblade298.neorogue.achievement.AchievementTriggerType;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Thief-only. Tracks the most dashes performed within a single fight. Mastery tiers at
// 50 / 100 / 200 dashes.
public class DashesInFightAchievement implements Achievement {
	private static final String ID = "shadow_dancer";
	private static final int[] THRESHOLDS = { 50, 100, 200 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Shadow Dancer", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.SUGAR;
	}

	@Override
	public int[] getMasteryThresholds() {
		return THRESHOLDS;
	}

	@Override
	public AchievementScope getScope() {
		return AchievementScope.CLASS;
	}

	@Override
	public EquipmentClass getRequiredClass() {
		return EquipmentClass.THIEF;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Dash " + target + " times in a single fight.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		// Fresh per-fight counter; progress tracks the best single-fight dash count ever reached.
		int[] fightCount = { 0 };
		data.addTrigger(ID, Trigger.DASH, (pdata, in) -> {
			fightCount[0]++;
			if (fightCount[0] > progress.getProgress()) {
				if (progress.addProgress(fightCount[0] - progress.getProgress())) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
