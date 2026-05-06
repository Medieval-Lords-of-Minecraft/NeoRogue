package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Finale extends Equipment {
	private static final String ID = "Finale";
	private int damage, bonusDamage, thres;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Finale(boolean isUpgraded) {
		super(ID, "Finale", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 30 : 40, 12, 0));
		properties.addUpgrades(PropertyType.STAMINA_COST);
		damage = 360;
		bonusDamage = 50;
		thres = isUpgraded ? 20 : 30;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta staminaUsed = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);

		// Track stamina spent on ability casts and update icon
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			staminaUsed.addCount((int) ev.getStaminaCost());
			int stacks = staminaUsed.getCount() / thres;
			if (stacks > 0) {
				icon.setAmount(stacks);
				inst.setIcon(icon);
			}
			return TriggerResult.keep();
		});

		inst.setAction((pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
				Player p2 = data.getPlayer();
				int stacks = staminaUsed.getCount() / thres;
				int totalDamage = damage + bonusDamage * stacks;
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				ev.getMeta().addDamageSlice(new DamageSlice(data, totalDamage, DamageType.PIERCING, DamageStatTracker.of(id + slot, Finale.this)));
				hit.play(p2, ev.getTarget());
				Sounds.anvil.play(p2, ev.getTarget());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, deal " + GlossaryTag.PIERCING.tag(this, damage, false) +
				" damage on your next basic attack plus an additional " + DescUtil.white(bonusDamage) +
				" for every " + DescUtil.yellow(thres) + " stamina you've used on abilities this fight.");
	}
}
