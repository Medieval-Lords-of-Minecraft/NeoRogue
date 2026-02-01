package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class BlessedEdge extends Equipment {
	private static final String ID = "BlessedEdge";
	private int damage, sanct;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.FIREWORK);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN),
			hitSound = new SoundContainer(Sound.BLOCK_ANVIL_LAND);
	
	public BlessedEdge(boolean isUpgraded) {
		super(ID, "Blessed Edge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 5, 0));
		damage = 100;
		sanct = isUpgraded ? 60 : 40;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				LivingEntity target = ev.getTarget();
				FightInstance.getFightData(target.getUniqueId()).applyStatus(StatusType.SANCTIFIED, data, sanct, -1);
				FightInstance.dealDamage(data, DamageType.LIGHT, damage, target, DamageStatTracker.of(id + slot, this));
				hit.play(p, target.getLocation());
				hitSound.play(p, target.getLocation());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST,
				"On cast, your next basic attack deals <white>" + damage + " </white>" + GlossaryTag.LIGHT.tag(this) + " damage and applies <yellow>" + sanct +
				"</yellow> " + GlossaryTag.SANCTIFIED.tag(this) + ".");
	}
}
