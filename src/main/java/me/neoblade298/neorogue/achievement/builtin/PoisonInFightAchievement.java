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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PoisonInFightAchievement implements Achievement {
	private static final String ID = "toxicologist";
	private static final int[] THRESHOLDS = { 100, 250, 1000 };

	@Override public String getId() { return ID; }
	@Override public Component getDisplayName() { return Component.text("Toxicologist", NamedTextColor.GOLD); }
	@Override public Material getMaterial() { return Material.SPIDER_EYE; }
	@Override public int[] getMasteryThresholds() { return THRESHOLDS; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return EquipmentClass.THIEF; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < THRESHOLDS.length ? THRESHOLDS[mastery] : THRESHOLDS[THRESHOLDS.length - 1];
		return List.of(Component.text("Apply " + target + " Poison in a single fight.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		int[] applied = { 0 };
		data.addTrigger(ID, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.POISON) || event.getStacks() <= 0) return TriggerResult.keep();
			applied[0] += event.getStacks();
			if (applied[0] > progress.getProgress()
					&& progress.addProgress(applied[0] - progress.getProgress())) {
				AchievementManager.notifyMastery(pdata.getPlayer(), this, progress);
			}
			return TriggerResult.keep();
		});
	}
}
