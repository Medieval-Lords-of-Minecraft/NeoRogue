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

public class LightningBolt extends Equipment {
	private static final String ID = "LightningBolt";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORK).count(3).spread(0.3, 0.3);
	private static final TargetProperties tp = TargetProperties.line(7, 2, TargetType.ENEMY);

	private int damage, thres, bonusDamage;

	public LightningBolt(boolean isUpgraded) {
		super(ID, "Lightning Bolt", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 0, 7, tp.range));
		damage = 70;
		thres = isUpgraded ? 40 : 30;
		bonusDamage = isUpgraded ? 140 : 70;

	}

	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), LightningStrike.get(), FireBolt.get());
		addReforge(Intuition.get(), DrainLightning.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			double cost = properties.get(PropertyType.MANA_COST);
			double currMana = data.getMana() + cost;
			boolean hasBonus = currMana >= thres;
			data.channel(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					Location start = p.getLocation().add(0, 1, 0);
					Vector dir = p.getEyeLocation().getDirection();
					Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
					ParticleUtil.drawLine(p, tick, start, end, 0.3);
					double fDamage = damage + (hasBonus ? bonusDamage : 0);
					Sounds.firework.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, fDamage, DamageType.LIGHTNING,
								DamageStatTracker.of(id + slot, eq)), ent);
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.YELLOW_DYE, GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing "
				+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " in a line in front of you. If you are above "
				+ DescUtil.yellow(thres) + " mana, increase the damage by " + DescUtil.yellow(bonusDamage) + ".");
	}
}
