package me.neoblade298.neorogue.session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightStatistics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SessionStatistics {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private double damageDealt;
	private double damageTakenHealth;
	private double damageTakenShields;
	private double shieldsApplied;
	private double healingDone;
	private double damageBarriered;
	private int fightsCompleted;
	private int deaths;
	private int statusesApplied;
	private double damageTakenHealthAtRegionStart;

	public void aggregate(FightStatistics fs) {
		// Sum all damage dealt
		for (double amt : fs.getDamageDealt().values()) {
			damageDealt += amt;
		}

		// Sum all damage taken (across all mobs and types)
		double totalTaken = 0;
		for (HashMap<DamageType, Double> mobDmg : fs.getDamageTaken().values()) {
			for (double amt : mobDmg.values()) {
				totalTaken += amt;
			}
		}
		double healthTaken = fs.getHealthDamageTaken();
		damageTakenHealth += healthTaken;
		damageTakenShields += Math.max(0, totalTaken - healthTaken);

		shieldsApplied += fs.getShieldsApplied();
		healingDone += fs.getHealingGiven() + fs.getSelfHealing();
		damageBarriered += fs.getDamageBarriered();
		deaths += fs.getDeaths();

		// Sum all status stacks applied
		for (int stacks : fs.getStatusesApplied().values()) {
			statusesApplied += stacks;
		}

		fightsCompleted++;
	}

	public void load(ResultSet rs) throws SQLException {
		damageDealt = rs.getDouble("statDamageDealt");
		damageTakenHealth = rs.getDouble("statDamageTakenHealth");
		damageTakenShields = rs.getDouble("statDamageTakenShields");
		shieldsApplied = rs.getDouble("statShieldsApplied");
		healingDone = rs.getDouble("statHealingDone");
		damageBarriered = rs.getDouble("statDamageBarriered");
		fightsCompleted = rs.getInt("statFightsCompleted");
		deaths = rs.getInt("statDeaths");
		statusesApplied = rs.getInt("statStatusesApplied");
		damageTakenHealthAtRegionStart = rs.getDouble("statDmgHealthRegionStart");
	}

	public double getDamageDealt() { return damageDealt; }
	public double getDamageTakenHealth() { return damageTakenHealth; }
	public double getDamageTakenShields() { return damageTakenShields; }
	public double getShieldsApplied() { return shieldsApplied; }
	public double getHealingDone() { return healingDone; }
	public double getDamageBarriered() { return damageBarriered; }
	public int getFightsCompleted() { return fightsCompleted; }
	public int getDeaths() { return deaths; }
	public int getStatusesApplied() { return statusesApplied; }
	public double getDamageTakenHealthAtRegionStart() { return damageTakenHealthAtRegionStart; }

	public void markRegionStart() {
		damageTakenHealthAtRegionStart = damageTakenHealth;
	}

	public void sendTo(Player p) {
		p.sendMessage(Component.text("=== Session Statistics ===", NamedTextColor.GOLD));
		p.sendMessage(statLine("Fights Completed", String.valueOf(fightsCompleted)));
		p.sendMessage(statLine("Deaths", String.valueOf(deaths)));
		p.sendMessage(statLine("Damage Dealt", df.format(damageDealt)));
		p.sendMessage(statLine("Damage Taken (Health)", df.format(damageTakenHealth)));
		p.sendMessage(statLine("Damage Taken (Shields)", df.format(damageTakenShields)));
		p.sendMessage(statLine("Shields Applied", df.format(shieldsApplied)));
		p.sendMessage(statLine("Healing Done", df.format(healingDone)));
		p.sendMessage(statLine("Damage Barriered", df.format(damageBarriered)));
		p.sendMessage(statLine("Statuses Applied", String.valueOf(statusesApplied)));
	}

	// Builds the full list of stat lines used as item lore in the session stats inventory UI.
	public List<Component> buildLore() {
		List<Component> lore = new ArrayList<Component>();
		lore.add(loreLine("Fights Completed", String.valueOf(fightsCompleted)));
		lore.add(loreLine("Deaths", String.valueOf(deaths)));
		lore.add(loreLine("Damage Dealt", df.format(damageDealt)));
		lore.add(loreLine("Damage Taken (Health)", df.format(damageTakenHealth)));
		lore.add(loreLine("Damage Taken (Shields)", df.format(damageTakenShields)));
		lore.add(loreLine("Shields Applied", df.format(shieldsApplied)));
		lore.add(loreLine("Healing Done", df.format(healingDone)));
		lore.add(loreLine("Damage Barriered", df.format(damageBarriered)));
		lore.add(loreLine("Statuses Applied", String.valueOf(statusesApplied)));
		return lore;
	}

	private Component statLine(String label, String value) {
		return Component.text(" " + label + ": ", NamedTextColor.GRAY)
				.append(Component.text(value, NamedTextColor.WHITE));
	}

	private Component loreLine(String label, String value) {
		return Component.text(label + ": ", NamedTextColor.GRAY)
				.append(Component.text(value, NamedTextColor.WHITE))
				.decoration(TextDecoration.ITALIC, false);
	}
}
