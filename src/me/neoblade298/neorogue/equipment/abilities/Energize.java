package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class Energize extends Equipment {
	private static final String ID = "energize";
	private int damage, elec;
	private static final ParticleContainer pc = new ParticleContainer(Particle.REDSTONE)
			.dustOptions(new DustOptions(Color.WHITE, 1F)).count(50).spread(1, 2).offsetY(1);
	
	public Energize(boolean isUpgraded) {
		super(ID, "Energize", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 20, 0));
		damage = isUpgraded ? 50 : 30;
		elec = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void setupReforges() {
		addSelfReforge(Burst.get(), Discipline.get(), Ferocity.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, your next basic attack deals an additional " + GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage " +
		"and marks the enemy. For the next <white>5</white> seconds, the marked enemy gains " + GlossaryTag.ELECTRIFIED.tag(this, elec, true)
		+ " each time you deal " + GlossaryTag.LIGHTNING.tag(this) + " damage to any enemy, including this ability.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EnergizeInstance inst = new EnergizeInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
		
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (inst.mark == null || !inst.mark.isValid()) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.LIGHTNING)) return TriggerResult.keep();
			FightInstance.applyStatus(inst.mark, StatusType.ELECTRIFIED, p, elec, -1);
			return TriggerResult.keep();
		});
	}
	
	private class EnergizeInstance extends EquipmentInstance {
		private LivingEntity mark;
		public EnergizeInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, in) -> {
				pdata.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata2, in2) -> {
					BasicAttackEvent ev2 = (BasicAttackEvent) in2;
					mark = ev2.getTarget();
					ev2.getMeta().addDamageSlice(new DamageSlice(pdata, damage, DamageType.LIGHTNING));
					pc.play(p, mark);
					pdata.addTask(new BukkitRunnable() {
						public void run() {
							mark = null;
						}
					}.runTaskLater(NeoRogue.inst(), 100L));
					return TriggerResult.remove();
				});
				return TriggerResult.keep();
			};
		}
		
	}
}
