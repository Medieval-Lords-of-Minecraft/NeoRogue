package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Entropy extends Equipment implements Power {
	private static final String ID = "Entropy";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);;
	private SessionEquipment sessionEq;
	private int intel, riftThres;
	
	public Entropy(boolean isUpgraded) {
		super(ID, "Entropy", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		intel = 3;
		riftThres = isUpgraded ? 3 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(CatalystCrucible.get(), Convergence.get(), Brilliance.get(), IAmAtomic.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		this.sessionEq = sessionEq;
		boolean[] activated = {false};
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (activated[0]) return TriggerResult.remove();
			activated[0] = true;

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		data.addTrigger(id + "-active", Trigger.KILL, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			am.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p2, p2);
			pc.play(p2, p2);
			if (am.getCount() % riftThres == 0) {
				Sounds.fire.play(p2, p2);
				data.addRift(new Rift(data, p2.getLocation(), 160));
			}
			icon.setAmount(am.getCount());
			inst.setIcon(icon);
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SCRAP,
				GlossaryTag.POWER.tag(this) + ". Activates after killing an enemy. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill. Every " + DescUtil.yellow(riftThres) + " kills, spawn a " + 
				GlossaryTag.RIFT.tag(this) + " [<white>8s</white>] at your location.");
	}
}
