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
import me.neoblade298.neorogue.session.fight.trigger.event.ShieldsEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Warrior-only. Tracks the most shields generated within a single fight. Mastery tiers at
// 100 / 250 / 500 / 1000 shields.
public class ShieldsInFightAchievement implements Achievement {
	private static final String ID = "shieldsmith";
	private static final int[] THRESHOLDS = { 100, 250, 500, 1000 };

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getDisplayName() {
		return Component.text("Shieldsmith", NamedTextColor.GOLD);
	}

	@Override
	public Material getMaterial() {
		return Material.IRON_CHESTPLATE;
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
		return EquipmentClass.WARRIOR;
	}

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Generate " + target + " shields in a single fight.", NamedTextColor.GRAY));
	}

	@Override
	public EnumSet<AchievementTriggerType> getTriggerTypes() {
		return EnumSet.of(AchievementTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		// Fresh per-fight accumulator; progress tracks the best single-fight total ever reached.
		// carry keeps fractional shields between grants so nothing is lost to integer truncation.
		double[] fightTotal = { 0 };
		data.addTrigger(ID, Trigger.PRE_GRANT_SHIELDS, (pdata, in) -> {
			ShieldsEvent ev = (ShieldsEvent) in;
			double amt = ev.getShield().getTotal();
			if (amt <= 0) return TriggerResult.keep();
			fightTotal[0] += amt;
			int best = (int) fightTotal[0];
			if (best > progress.getProgress()) {
				if (progress.addProgress(best - progress.getProgress())) {
					AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
				}
			}
			return TriggerResult.keep();
		});
	}
}
