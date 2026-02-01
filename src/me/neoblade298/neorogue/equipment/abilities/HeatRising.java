package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HeatRising extends Equipment {
	private static final String ID = "HeatRising";
	private double mult;
	private int burn, multDisplay;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public HeatRising(boolean isUpgraded) {
		super(ID, "Heat Rising", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 15, 0));
		mult = isUpgraded ? 0.5 : 0.3;
		multDisplay = (int) (mult * 100);
		burn = 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.channel(20).then(new Runnable() {
				public void run() {
					Sounds.fire.play(p, p);
					data.applyStatus(StatusType.BURN, data, burn, -1);
					data.addDamageBuff(DamageBuffType.of(DamageCategory.FIRE), Buff.multiplier(data, mult, StatTracker.damageBuffAlly(buffId, eq)), 200);
				}
			});
			pc.play(p, p);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TORCHFLOWER,
				"On cast, " + GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before applying " +
				GlossaryTag.BURN.tag(this, burn, false) + " to yourself and increase your " + GlossaryTag.FIRE.tag(this) + " damage by "
						+ DescUtil.yellow(multDisplay + "%") + " [<white>10s</white>].");
	}
}
