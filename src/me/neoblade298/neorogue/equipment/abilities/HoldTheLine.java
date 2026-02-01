package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class HoldTheLine extends Equipment {
	private static final String ID = "HoldTheLine";
	private int shields, concussed;
	private static final SoundContainer hit = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);
	
	public HoldTheLine(boolean isUpgraded) {
		super(ID, "Hold the Line", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 15, 0));
		shields = isUpgraded ? 15 : 10;
		concussed = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			long endTime = System.currentTimeMillis() + 10000;
			data.addSimpleShield(p.getUniqueId(), shields, 200);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {				if (endTime < System.currentTimeMillis()) return TriggerResult.remove();
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				FightInstance.getFightData(ev.getTarget()).applyStatus(StatusType.CONCUSSED, data, concussed, -1);
				hit.play(p, p);
				return TriggerResult.keep();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_BRICK_WALL,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for 10 seconds."
						+ " During this time, your basic attacks apply " + GlossaryTag.CONCUSSED.tag(this, concussed, true) + ".");
	}
}
