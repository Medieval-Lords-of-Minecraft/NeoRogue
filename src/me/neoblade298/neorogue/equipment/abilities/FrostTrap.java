package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FrostTrap extends Equipment {
	private static final String ID = "frostTrap";
	private static TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CLOUD).count(50).spread(1, 0.2),
		hit = new ParticleContainer(Particle.EXPLOSION).count(10).spread(1, 1);
	private int damage, frost;
	
	public FrostTrap(boolean isUpgraded) {
		super(ID, "Frost Trap", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 8, 0));
		
		damage = 220;
		frost = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			data.channel(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Location loc = p.getLocation();
					data.addTrap(new Trap(data, loc, 200) {
						@Override
						public void tick() {
							trap.play(p, loc);
							LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
							if (trg != null) {
								Sounds.breaks.play(p, trg);
								hit.play(p, trg);
								DamageMeta dm = new DamageMeta(data, damage, DamageType.ICE, DamageOrigin.TRAP);
								FightInstance.dealDamage(dm, trg);
								FightInstance.applyStatus(trg, StatusType.FROST, data, frost, -1);
								data.removeTrap(this);
							}
						}
					});
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_TRAPDOOR,
				"On cast, " + DescUtil.charge(this, 1, 2) + ". Then drop a " + GlossaryTag.TRAP.tag(this) + 
				" that lasts for " + DescUtil.white("10s") +
				". If an enemy steps on the trap, they take " + GlossaryTag.ICE.tag(this, damage, true) +
				" damage, apply " + GlossaryTag.FROST.tag(this, frost, true) +", and deactivate the trap.");
	}
}
