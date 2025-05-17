package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpikeTrap extends Equipment {
	private static final String ID = "spikeTrap";
	private static TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static ParticleContainer spike = new ParticleContainer(Particle.FIREWORK).count(50).spread(1, 0.4);
	private int damage = 40;
	
	public SpikeTrap(boolean isUpgraded) {
		super(ID, "Spike Trap", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, isUpgraded ? 10 : 13, tp.range));
		properties.addUpgrades(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			data.charge(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					initTrap(p, data);
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		}));
	}

	private void initTrap(Player p, PlayerFightData data) {
		Sounds.equip.play(p, p);
		Location loc = p.getLocation();
		data.addTrap(new Trap(data, loc, 400) {
			@Override
			public void tick() {
				spike.play(p, loc);
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.PIERCING, DamageOrigin.TRAP), TargetHelper.getEntitiesInRadius(p, loc, tp));
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_TRAPDOOR,
				"On cast, " + DescUtil.charge(this, 1, 2) + ". Then drop a " + GlossaryTag.TRAP.tag(this) + 
				" that repeatedly deals " + GlossaryTag.PIERCING.tag(this, damage, false) +
				" damage to enemies on it every second for <white>20s</white>.");
	}
}
