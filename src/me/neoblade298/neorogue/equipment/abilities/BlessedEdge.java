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
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class BlessedEdge extends Equipment {
	private int damage, sanct;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.FIREWORKS_SPARK);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN),
			hitSound = new SoundContainer(Sound.BLOCK_ANVIL_LAND);
	
	public BlessedEdge(boolean isUpgraded) {
		super("blessedEdge", "Blessed Edge", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 5, 0));
		damage = 125;
		sanct = isUpgraded ? 9 : 6;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
		addTags(GlossaryTag.SANCTIFIED, GlossaryTag.SLASHING);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				LivingEntity target = ev.getTarget();
				FightInstance.getFightData(target.getUniqueId()).applyStatus(StatusType.SANCTIFIED, p.getUniqueId(), sanct, -1);
				FightInstance.dealDamage(data, DamageType.SLASHING, damage, target);
				hit.play(p, target.getLocation());
				hitSound.play(p, target.getLocation());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals <white>" + damage + " </white>" + GlossaryTag.SLASHING.tag(this) + " damage and applies <yellow>" + sanct +
				"</yellow> " + GlossaryTag.SANCTIFIED.tag(this) + ".");
	}
}
