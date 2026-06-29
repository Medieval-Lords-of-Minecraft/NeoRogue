package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FlowState extends Equipment implements Power {
	private static final String ID = "FlowState";
	private int thres;
	private double inc;
	
	public FlowState(boolean isUpgraded) {
		super(ID, "Flow State", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 40 : 30;
		inc = isUpgraded ? 0.8 : 0.5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Dexterity.get(), Rushdown.get(), FlowState2.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.PLAYER_TICK, (pdata2, in2) -> {
					if (data.getStamina() < thres) return TriggerResult.keep();
					data.addStamina(inc);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after reaching " + DescUtil.white("50%") + " max stamina. Increase stamina regen by " + DescUtil.yellow(inc) + " when above "
				+ DescUtil.yellow(thres) + " stamina.");
	}
}
