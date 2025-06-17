package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.BasicStatus;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class HeadTrauma extends Equipment {
	private static final String ID = "headTrauma";
	private int stacks, damage;
	private static final int THRESHOLD = 100;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.DIRT.createBlockData()).count(20).spread(1, 1).offsetY(1);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_ROOTED_DIRT_BREAK);
	
	public HeadTrauma(boolean isUpgraded) {
		super(ID, "Head Trauma", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stacks = isUpgraded ? 6 : 4;
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			ev.getStacksBuffList().add(new Buff(data, stacks, 0, BuffStatTracker.ignored(this)));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.FROST)) return TriggerResult.keep();
			FightData fd = ev.getTarget();
			if (fd.getStatus(StatusType.FROST).getStacks() < THRESHOLD) return TriggerResult.keep();
			if (fd.hasStatus(p.getName() + "-bitterCold")) return TriggerResult.keep();
			Status s = new BasicStatus(p.getName() + "-bitterCold", data, StatusClass.NONE, true);
			fd.applyStatus(s, data, 1, -1);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.ICE), fd.getEntity());
			pc.play(p, fd.getEntity());
			sc.play(p, fd.getEntity());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARMOR_STAND,
				"Passive. Once per enemy, applying " + GlossaryTag.CONCUSSED.tag(this, THRESHOLD, false) +
				" to an enemy will deal " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage to them.");
	}
}
