package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class AvalonianAnchor extends Artifact {
	private static final String ID = "avalonianAnchor";
	private static final ParticleContainer part = new ParticleContainer(Particle.CRIT).count(10).spread(0.5, 0.5).speed(0.1);
	public AvalonianAnchor() {
		super(ID, "Avalonian Anchor", Rarity.UNCOMMON, new EquipmentClass[] { EquipmentClass.WARRIOR, EquipmentClass.THIEF });
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			// Apparently standing still is roughly -0.078 downward velocity
			if (p.getVelocity().getY() < 0.1) {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				if (ev.isProjectile()) return TriggerResult.keep();
				Sounds.crit.play(p, p);
				part.play(p, ev.getTarget());
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, 0, 0.5, StatTracker.damageBuffAlly(this)));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RESPAWN_ANCHOR,
				"Landing a melee basic attack while falling increases its damage by <white>50%</white>");
	}
}
