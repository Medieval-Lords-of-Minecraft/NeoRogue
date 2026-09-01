package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class PyroCharm extends Equipment {
	private static final String ID = "PyroCharm";
	private static final int DAMAGE_THRESHOLD = 500;
	private static final ParticleContainer FIRE_FLARE = new ParticleContainer(Particle.FLAME).count(18)
			.spread(0.6, 0.7).offsetY(0.8).speed(0.01);
	private static final ParticleContainer EMBERS = new ParticleContainer(Particle.LAVA).count(5)
			.spread(0.4, 0.4).offsetY(0.8).speed(0);
	private double damageIncrease;

	public PyroCharm(boolean isUpgraded) {
		super(ID, "Pyro Charm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		damageIncrease = isUpgraded ? 0.5 : 0.3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta progress = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			double fireDamage = event.getMeta().getPostMitigationDamage().getOrDefault(DamageType.FIRE, 0D);
			if (fireDamage <= 0) return TriggerResult.keep();

			progress.addDouble(fireDamage);
			if (progress.getDouble() < DAMAGE_THRESHOLD) return TriggerResult.keep();

			data.addDamageBuff(DamageBuffType.of(DamageCategory.FIRE), Buff.multiplier(data, damageIncrease,
					StatTracker.damageBuffAlly(UUID.randomUUID().toString(), this)));
			Player player = data.getPlayer();
			FIRE_FLARE.play(player, player);
			EMBERS.play(player, player);
			Sounds.fire.play(player, player);
			Sounds.levelup.play(player, player);
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CHARGE, "After cumulatively dealing "
				+ GlossaryTag.FIRE.tag(this, DAMAGE_THRESHOLD) + " damage in a fight, increase "
				+ GlossaryTag.FIRE.tag(this) + " damage by " + DescUtil.val((int) (damageIncrease * 100) + "%")
				+ " for the rest of the fight.");
	}
}