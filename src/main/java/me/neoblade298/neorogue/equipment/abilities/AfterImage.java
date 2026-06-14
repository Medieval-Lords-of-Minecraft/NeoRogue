package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AfterImage extends Equipment implements Power {
	private static final String ID = "AfterImage";
	private int shields;
	
	public AfterImage(boolean isUpgraded) {
		super(ID, "After Image", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DASH, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.DASH, (pdata2, inputs) -> {
					Player p = data.getPlayer();
					data.addSimpleShield(p.getUniqueId(), shields, 100);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Activates after dashing once. Every time you " + GlossaryTag.DASH.tag(this) + ", gain " + 
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}