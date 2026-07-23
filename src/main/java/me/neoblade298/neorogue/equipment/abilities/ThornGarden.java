package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ShieldsEvent;

public class ThornGarden extends Equipment implements Power {
	private static final String ID = "ThornGarden";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final int CUTOFF = 3;
	private int thorns;
	
	public ThornGarden(boolean isUpgraded) {
		super(ID, "Thorn Garden", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		
		thorns = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_SHIELDS, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		int[] shieldCount = {0};
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.RECEIVE_SHIELDS, (pdata2, in2) -> {
					ShieldsEvent ev = (ShieldsEvent) in2;
					shieldCount[0] += ev.getShield().getAmount();
					data.applyStatus(StatusType.THORNS, data, thorns * (shieldCount[0] / CUTOFF), -1, ThornGarden.this);
					shieldCount[0] = shieldCount[0] % CUTOFF;
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after receiving shields. For every " + DescUtil.val(CUTOFF) + " " + GlossaryTag.SHIELDS.tag + " that are granted to you, "
						+ "gain " + DescUtil.val(thorns) + " stacks of " + GlossaryTag.THORNS.tag(this) + ".");
	}
}
