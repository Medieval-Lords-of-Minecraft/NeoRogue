package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Chaos extends Equipment {
	private static final String ID = "Chaos";
	private static final TargetProperties tp = TargetProperties.line(12, 2, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.END_ROD).count(20).spread(0.2, 0.2).speed(0.1);
	private int damage, stacks;

	public Chaos(boolean isUpgraded) {
		super(ID, "Chaos", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 8, tp.range));
		damage = isUpgraded ? 300 : 200;
		stacks = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			LivingEntity target = TargetHelper.getNearestInSight(p, tp);
			if (target == null) return TriggerResult.keep();

			int roll = (int) (Math.random() * 4);
			DamageType type;
			StatusType status;
			switch (roll) {
			case 0:
				type = DamageType.FIRE;
				status = StatusType.CORRUPTION;
				Sounds.fire.play(p, p);
				break;
			case 1:
				type = DamageType.LIGHTNING;
				status = StatusType.ELECTRIFIED;
				Sounds.thunder.play(p, p);
				break;
			case 2:
				type = DamageType.DARK;
				status = StatusType.INSANITY;
				Sounds.wither.play(p, p);
				break;
			default:
				type = DamageType.BLUNT;
				status = StatusType.CONCUSSED;
				Sounds.explode.play(p, p);
				break;
			}

			Location start = p.getEyeLocation();
			Location end = target.getLocation().add(0, 1, 0);
			ParticleUtil.drawLine(p, pc, start, end, 0.3);
			FightInstance.dealDamage(new DamageMeta(data, damage, type, DamageStatTracker.of(id + slot, eq)), target);
			FightInstance.applyStatus(target, status, data, stacks, -1);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"On cast, fire a bolt of magic that deals random " + GlossaryTag.FIRE.tag(this) + ", " +
				GlossaryTag.ELECTRIFIED.tag(this) + ", " + GlossaryTag.DARK.tag(this) + ", or " +
				GlossaryTag.CONCUSSED.tag(this) + " damage for " +
				"<yellow>" + damage + "</yellow> and applies <yellow>" + stacks + "</yellow> of that element's status.");
	}
}
