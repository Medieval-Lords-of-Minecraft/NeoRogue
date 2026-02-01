package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HolySpear extends Equipment {
	private static final String ID = "HolySpear";
	private static final ParticleContainer lancePart = new ParticleContainer(Particle.ELECTRIC_SPARK).count(5).spread(0.1, 0.1);
	private static final TargetProperties tp = TargetProperties.line(6, 1, TargetType.ENEMY);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5F);
	private int damage, bonus, sanct, thres;

	public HolySpear(boolean isUpgraded) {
		super(
				ID, "Holy Spear", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofUsable(20, 35, 12, tp.range)
		);
		damage = 200;
		bonus = isUpgraded ? 500 : 350;
		sanct = isUpgraded ? 150 : 100;
		thres = 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, (pdata, in) -> {
			data.charge(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, tp);
					sc.play(p, p);
					Location start = p.getLocation().add(0, 1, 0);
					Vector v = p.getLocation().getDirection().setY(0).normalize().multiply(tp.range);
					ParticleUtil.drawLine(p, lancePart, p.getLocation().add(0, 1, 0), start.clone().add(v), 0.5);
					DamageStatTracker tracker = DamageStatTracker.of(id + slot, eq);
					for (LivingEntity target : targets) {
						DamageMeta dm = new DamageMeta(data, damage, DamageType.PIERCING, tracker);
						FightData fd = FightInstance.getFightData(target);
						if (fd.getStatus(StatusType.SANCTIFIED).getStacks() >= thres) {
							dm.addDamageSlice(new DamageSlice(data, bonus, DamageType.PIERCING, tracker));
						}
						FightInstance.applyStatus(target, StatusType.SANCTIFIED, data, sanct, -1);
						FightInstance.dealDamage(dm, target);
					}
				}
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.GLOWSTONE_DUST,
				"On cast, after " + DescUtil.charge(this, 1, 1) + ", deal " + GlossaryTag.PIERCING.tag(this, damage, false) + " to all enemies in a line. " +
				"Deal " + DescUtil.yellow(bonus) + " bonus damage to enemies with over " + GlossaryTag.SANCTIFIED.tag(this, thres, false) + ". Apply " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + 
				" to all enemies hit after.");
	}
}
