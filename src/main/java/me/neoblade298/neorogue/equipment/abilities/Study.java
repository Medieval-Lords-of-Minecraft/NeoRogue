package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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

public class Study extends Equipment implements Power {
	private static final String ID = "Study";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);;
	private int intel;
	
	public Study(boolean isUpgraded) {
		super(ID, "Study", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		intel = isUpgraded ? 3 : 2;
	}

	@Override
	public void setupReforges() {
		addReforge(Intuition.get(), Study2.get());
		addReforge(Manabending.get(), Discharge.get(), Entropy.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	private void activate(PlayerFightData data) {
		data.addTrigger(id + "-active", Trigger.KILL, (pdata2, in2) -> {
			ActionMeta am = new ActionMeta();
			Player p2 = data.getPlayer();
			am.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p2, p2);
			pc.play(p2, p2);
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				activate(data);
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.WRITABLE_BOOK,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after killing an enemy. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill.");
	}
}
