package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Consecrate extends Equipment {
	private static final String ID = "Consecrate";
	private static final TargetProperties tp = TargetProperties.radius(5, false);
	private static final ParticleContainer edge = new ParticleContainer(Particle.CLOUD),
			center = new ParticleContainer(Particle.FIREWORK).count(100).spread(tp.range / 2, 0.5).speed(0.01).offsetY(0.5);
	private static final Circle circ = new Circle(tp.range);
	private int damage, sanct, shields;
	
	public Consecrate(boolean isUpgraded) {
		super(ID, "Consecrate", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 20, 14, 0, tp.range));
		damage = isUpgraded ? 350 : 250;
		sanct = isUpgraded ? 150 : 100;
		shields = isUpgraded ? 20 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack chargedIcon = item.clone().withType(Material.NETHER_STAR);
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);

		inst.setAction((pdata, in) -> {
			icon.setAmount(1);
			inst.setIcon(icon);
			am.setCount(0);
			data.charge(20).then(new Runnable() {
				public void run() {
					Sounds.explode.play(p, p);
					circ.play(edge, p.getLocation(), LocalAxes.xz(), null);
					center.play(p, p);
					data.addSimpleShield(p.getUniqueId(), shields, 160);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightInstance.dealDamage(
								new DamageMeta(data, damage, DamageType.LIGHT, DamageStatTracker.of(id + slot, eq))
										.setKnockback(1),
								ent);
						FightInstance.applyStatus(ent, StatusType.SANCTIFIED, data, sanct, -1);
					}
				}
			});
			return TriggerResult.keep();
		});

		inst.setCondition((pdata2, pl, in) -> {
			return am.getCount() >= 5;
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			if (am.getCount() == 4) {
				am.addCount(1);
				inst.setIcon(chargedIcon);
			}
			else if (am.getCount() < 4) {
				am.addCount(1);
				icon.setAmount(am.getCount());
				inst.setIcon(icon);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHITE_GLAZED_TERRACOTTA,
				"Can be cast after landing basic attacks on enemies with " + GlossaryTag.SANCTIFIED.tag(this) + " <white>5</white> times. " +
				"On cast, " + DescUtil.charge(this, 1, 1) + " before dealing " + GlossaryTag.LIGHT.tag(this, damage, true) + " damage, gaining " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>8s</white>], " +
				"applying " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ", and knocking back nearby enemies.");
	}
}
