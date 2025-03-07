package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EarthenWall extends Equipment {
	private static final String ID = "earthenWall";
	private static final ParticleContainer earth = new ParticleContainer(Particle.BLOCK).blockData(Material.DIRT.createBlockData());
	private static final TargetProperties tp = TargetProperties.line(7, 2, TargetType.ENEMY);
	private int damage, def;
	
	public EarthenWall(boolean isUpgraded) {
		super(ID, "Earthen Wall", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 18, tp.range));
		damage = isUpgraded ? 120 : 80;
		def = isUpgraded ? 4 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, (pdata, in) -> {
			data.charge(20).then(new Runnable() {
				public void run() {
					Vector left = p.getEyeLocation().getDirection().setY(0).normalize();
					Vector forward = left.clone().crossProduct(new Vector(0, 1, 0));
					Vector dir = left.clone().multiply(tp.range);
					Location start = p.getLocation().add(0, 1, 0);
					Location end = start.clone().add(dir);
					ParticleUtil.drawLine(p, earth, start, end, 0.5);
					Sounds.explode.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN), ent);
					}
					Barrier.stationary(p, tp.range, 2, 4, start.clone().add(dir.clone().multiply(0.5)), new LocalAxes(left, new Vector(0, 1, 0), forward), null, earth);
				}
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MUD_BRICK_WALL,
				"On cast, " + GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage in a horizontal line and creating a " +
				GlossaryTag.BARRIER.tag(this) + " [<white>10s</white>] where your line is that blocks the next " + DescUtil.yellow(def) + " projectiles.");	
	}
}
