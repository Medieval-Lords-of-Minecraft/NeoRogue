package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class WinterCloak extends Equipment {
	private static final String ID = "WinterCloak";
	private static final int DAMAGE_REDUCTION = 1;
	private static final int SHIELD_DURATION_TICKS = 100;
	private static final int SHIELD_COOLDOWN_MILLIS = 4000;
	private static final Circle SHIELD_RING = new Circle(1.2);
	private static final ParticleContainer SHIELD_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(160, 230, 255), 1F));
	private int shields;

	public WinterCloak(boolean isUpgraded) {
		super(ID, "Winter Cloak", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ARMOR);
		shields = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent event = (ReceiveDamageEvent) in;
			event.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data,
					DAMAGE_REDUCTION, StatTracker.defenseBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});

		ActionMeta cooldown = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.FROST)) return TriggerResult.keep();
			long now = System.currentTimeMillis();
			if (now - cooldown.getTime() < SHIELD_COOLDOWN_MILLIS) return TriggerResult.keep();

			cooldown.setTime(now);
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), shields, SHIELD_DURATION_TICKS, this);
			SHIELD_RING.play(SHIELD_PARTICLE, player.getLocation(), LocalAxes.xz(), null);
			Sounds.block.play(player, player);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce " + GlossaryTag.DIRECT.tag(this)
				+ " damage received by " + DescUtil.white(DAMAGE_REDUCTION) + ". Whenever you apply "
				+ GlossaryTag.FROST.tag(this) + ", gain " + GlossaryTag.SHIELDS.tag(this, shields) + " ["
				+ DescUtil.white("5s") + "], with a " + DescUtil.white("4s") + " cooldown.");
	}
}