package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.TaskChain;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class FireBolt extends Equipment {
	private static final String ID = "FireBolt";
	private static final ParticleContainer lightning = new ParticleContainer(Particle.FIREWORK).count(3).spread(0.3,
			0.3), fire = new ParticleContainer(Particle.FLAME).count(3).spread(0.3, 0.3);
	private static final TargetProperties tp = TargetProperties.line(7, 2, TargetType.ENEMY);

	private int damage, thres;

	public FireBolt(boolean isUpgraded) {
		super(ID, "Fire Bolt", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 10, 12, tp.range));
		damage = 200;
		thres = isUpgraded ? 50 : 60;

	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			double mana = data.getMana() + properties.get(PropertyType.MANA_COST);
			TaskChain chain = data.channel(20).then(new Runnable() {
				public void run() {
					fire(p, data, slot, true);
				}
			});

			if (mana >= thres) {
				chain.then(new Runnable() {
					public void run() {
						fire(p, data, slot, false);
					}
				}, 10);
			}
			return TriggerResult.keep();
		}));
	}

	private void fire(Player p, PlayerFightData data, int slot, boolean isLightning) {
		Location start = p.getLocation().add(0, 1, 0);
		Vector dir = p.getEyeLocation().getDirection();
		Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
		ParticleUtil.drawLine(p, isLightning ? lightning : fire, start, end, 0.3);
		if (isLightning)
			Sounds.firework.play(p, p);
		else
			Sounds.fire.play(p, p);
		for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
			FightInstance.dealDamage(new DamageMeta(data, damage, isLightning ? DamageType.LIGHTNING : DamageType.FIRE, DamageStatTracker.of(id + slot, this)),
					ent);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM,
				GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true)
						+ " in a line in front of you. If you are above " + DescUtil.yellow(thres) + " mana, also deal "
						+ GlossaryTag.FIRE.tag(this, damage, true) + " damage in a line slightly after.");
	}
}
