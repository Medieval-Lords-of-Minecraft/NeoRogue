package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FlowState2 extends Equipment {
	private static final String ID = "FlowState2";
	private int thres;
	private double inc, dmgInc;
	
	public FlowState2(boolean isUpgraded) {
		super(ID, "Flow State II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 0, 0));
		thres = 30;
		inc = 0.8;
		dmgInc = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.PLAYER_TICK, (pdata2, in2) -> {
				if (data.getStamina() < thres) return TriggerResult.keep();
				data.addStamina(inc);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				GlossaryTag.POWER.tag(this) + ". Increase stamina regen by " + DescUtil.yellow(inc) + " when above "
				+ DescUtil.yellow(thres) + " stamina, further increased by " + DescUtil.yellow(dmgInc)
				+ " if you've dealt damage within " + DescUtil.white("2s") + ".");
	}
}
