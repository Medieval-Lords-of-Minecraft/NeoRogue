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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StealthFightAchievement implements Achievement {
	private static final String ID = "unseen_victory";
	private static final int[] THRESHOLDS = { 1 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Unseen Victory", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.ENDER_EYE; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return EquipmentClass.THIEF; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		return List.of(Component.text("Win a fight while remaining in Stealth for the entire fight.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		boolean[] continuouslyStealthed = { true };
		boolean[] observedTick = { false };
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			observedTick[0] = true;
			if (!pdata.hasStatus(StatusType.STEALTH)) continuouslyStealthed[0] = false;
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (observedTick[0] && continuouslyStealthed[0] && progress.addProgress(1)) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
