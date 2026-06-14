package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class ThriveInChaos extends Equipment implements Power {
	private static final String ID = "ThriveInChaos";
	private static final TargetProperties tp = TargetProperties.radius(7, false, TargetType.ENEMY);
	private int insanityPerStack;
	
	public ThriveInChaos(boolean isUpgraded) {
		super(ID, "Thrive in Chaos", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		insanityPerStack = isUpgraded ? 8 : 12;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.INSANITY)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 5) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am2 = new ActionMeta();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in2) -> {
			am2.addCount(1);
			if (am2.getCount() >= 3) {
				am2.setCount(0);
				int totalInsanity = 0;
				Player p2 = data.getPlayer();
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, tp)) {
					FightData fd = FightInstance.getFightData(ent);
					if (fd.hasStatus(StatusType.INSANITY)) {
						totalInsanity += fd.getStatus(StatusType.INSANITY).getStacks();
					}
				}
				int stealthStacks = totalInsanity / insanityPerStack;
				if (stealthStacks > 0) {
					data.applyStatus(StatusType.STEALTH, data, stealthStacks, 60);
				}
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.CHORUS_FRUIT,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.INSANITY.tag(this) + " " + DescUtil.white(5) + " times. Every " + DescUtil.white("3s") + ", count the " + GlossaryTag.INSANITY.tag(this) + 
				" stacks of all enemies in range. For every " + 
				GlossaryTag.INSANITY.tag(this, insanityPerStack, true) + ", gain " + 
				GlossaryTag.STEALTH.tag(this, 1, false) + " " + DescUtil.duration(3, false) + ".");
	}
}
