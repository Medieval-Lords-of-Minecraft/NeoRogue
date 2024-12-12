package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkPact extends Equipment {
	private static final String ID = "darkPact";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int seconds;
	
	public DarkPact(boolean isUpgraded) {
		super(ID, "Dark Pact", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		seconds = isUpgraded ? 25 : 40;
		pc.count(25).spread(0.5, 0.5).speed(0.1);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, 0, -0.5, StatTracker.of(this, "Additional Damage Taken")), seconds * 20);
		data.addTrigger(id, Trigger.BASIC_ATTACK, new DarkPactTriggerAction());
	}
	
	class DarkPactTriggerAction implements TriggerAction {
		private int count = 0;

		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			count++;
			if (count >= 3) {
				Player p = data.getPlayer();
				p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F);
				pc.play(p, p);
				data.applyStatus(StatusType.STRENGTH, data, 2, -1);
				count = 0;
			}
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"Passive. Increase your " + GlossaryTag.STRENGTH.tag(this) + " by 2 every 3 basic attacks. In exchange, take "
				+ "<white>50%</white> increased damage for the first <yellow>" + seconds + "s</yellow> of a fight.");
	}
}
