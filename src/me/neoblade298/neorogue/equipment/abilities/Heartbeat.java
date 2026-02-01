package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Heartbeat extends Equipment {
	private static final String ID = "Heartbeat";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.END_ROD).offsetY(0.1);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_HURT);
	private static final Circle circ = new Circle(tp.range);
	private int damage;
	
	public Heartbeat(boolean isUpgraded) {
		super(ID, "Heartbeat", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 0, 10, 0, tp.range));
				damage = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			if (am.getObject() != null) {
				Rift old = (Rift) am.getObject();
				old.setLocation(p.getLocation());
			}
			else {
				Rift r = new Rift(data, p.getLocation(), -1);
				am.setObject(r);
				data.addRift(r);
			}
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			Player p = data.getPlayer();
			am.addCount(1);
			if (am.getCount() >= 3) {
				am.addCount(-3);
				for (Rift rift : data.getRifts().values()) {
					circ.play(pc, rift.getLocation(), LocalAxes.xz(), null);
					sc.play(p, rift.getLocation());
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, rift.getLocation(), tp)) {
						FightInstance.dealDamage(data, DamageType.DARK, damage, ent, DamageStatTracker.of(id + slot, this));
					}
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHORUS_FRUIT,
				"On first cast, drop a permanent " + GlossaryTag.RIFT.tag(this) + ". This rift gets moved to you on recast. Every <white>3s</white>, deal " +
					GlossaryTag.DARK.tag(this, damage, true) + " to all enemies near every " + GlossaryTag.RIFT.tag(this) + ".");
	}
}
