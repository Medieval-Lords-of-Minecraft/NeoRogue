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

public class ReachStatusAchievement implements Achievement {
	private final String id;
	private final Component displayName;
	private final Material material;
	private final EquipmentClass requiredClass;
	private final StatusType status;
	private final String statusName;
	private final int[] thresholds;

	public ReachStatusAchievement(String id, String displayName, Material material, EquipmentClass requiredClass,
			StatusType status, String statusName, int... thresholds) {
		this.id = id;
		this.displayName = Component.text(displayName, NamedTextColor.GOLD);
		this.material = material;
		this.requiredClass = requiredClass;
		this.status = status;
		this.statusName = statusName;
		this.thresholds = thresholds;
	}

	@Override public String getId() { return id; }
	@Override public Component getDisplayName() { return displayName; }
	@Override public Material getMaterial() { return material; }
	@Override public int[] getMasteryThresholds() { return thresholds; }
	@Override public AchievementScope getScope() { return AchievementScope.CLASS; }
	@Override public EquipmentClass getRequiredClass() { return requiredClass; }
	@Override public EnumSet<AchievementTriggerType> getTriggerTypes() { return EnumSet.of(AchievementTriggerType.FIGHT); }

	@Override
	public List<Component> getDescription(int progress, int mastery) {
		int target = mastery < thresholds.length ? thresholds[mastery] : thresholds[thresholds.length - 1];
		return List.of(Component.text("Reach " + target + " " + statusName + " in a fight.", NamedTextColor.GRAY));
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data, AchievementProgress progress) {
		check(data, progress);
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (event.isStatus(status)) update(event.getStatus().getStacks(), pdata, progress);
			return TriggerResult.keep();
		});
	}

	private void check(PlayerFightData data, AchievementProgress progress) {
		update(data.getStatus(status).getStacks(), data, progress);
	}

	private void update(int stacks, PlayerFightData data, AchievementProgress progress) {
		if (stacks > progress.getProgress() && progress.addProgress(stacks - progress.getProgress())) {
			AchievementManager.notifyMastery(data.getPlayer(), this, progress);
		}
	}
}
