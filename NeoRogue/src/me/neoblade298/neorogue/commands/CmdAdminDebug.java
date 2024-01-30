package me.neoblade298.neorogue.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neorogue.area.Node;
import me.neoblade298.neorogue.area.NodeType;

public class CmdAdminDebug extends Subcommand {
	HashMap<String, HashMap<String, Integer>> results = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> failedResults = new HashMap<String, HashMap<String, Integer>>();
	HashSet<String> resultKeys = new HashSet<String>();

	public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
		super(key, desc, perm, runner);
	}

	@Override
	public void run(CommandSender s, String[] args) {
		// Hashmap to check randomness
		int successes = 0, failures = 0;
		
		// 2-sets
		for (int i = 0; i < 5; i++) {
			for (int j = i + 1; j < 5; j++) {
				attemptSet(new int[] {i, j}, 2);
				attemptSet(new int[] {i, j}, 3);
				attemptSet(new int[] {i, j}, 4);
			}
		}
		
		// 3-sets
		for (int i = 0; i < 5; i++) {
			for (int j = i + 1; j < 5; j++) {
				for (int k = j + 1; k < 5; k++) {
					attemptSet(new int[] {i, j, k}, 2);
					attemptSet(new int[] {i, j, k}, 3);
					attemptSet(new int[] {i, j, k}, 4);
					attemptSet(new int[] {i, j, k}, 5);
				}
			}
		}
		
		// 4-sets
		for (int i = 0; i < 5; i++) {
			for (int j = i + 1; j < 5; j++) {
				for (int k = j + 1; k < 5; k++) {
					for (int l = k + 1; l < 5; l++) {
						attemptSet(new int[] {i, j, k, l}, 3);
						attemptSet(new int[] {i, j, k, l}, 4);
						attemptSet(new int[] {i, j, k, l}, 5);
					}
				}
			}
		}

		for (String key : resultKeys) {
			if (results.containsKey(key)) {
				for (Entry<String, Integer> e : results.get(key).entrySet()) {
					successes += e.getValue();
				}
			}
			if (failedResults.containsKey(key)) {
				for (Entry<String, Integer> e : failedResults.get(key).entrySet()) {
					failures += e.getValue();
				}
			}
		}
		System.out.println("Total successes: " + successes);
		System.out.println("Total failures: " + failures);
		System.out.println("Success rate: " + (double) successes / (successes + failures));
		double variability = 0;
		for (Entry<String, HashMap<String, Integer>> e : results.entrySet()) {
			variability += e.getValue().size();
		}
		System.out.println("Success variability: " + (variability / results.size()));
		System.out.println("Failures:");
		for (Entry<String, HashMap<String, Integer>> e : failedResults.entrySet()) {
			System.out.println("- " + e.getKey());
			for (Entry<String, Integer> e2 : e.getValue().entrySet()) {
				System.out.println(e2.getKey() + ": " + e2.getValue());
			}
		}
	}
	
	private void attemptSet(int[] startNodes, int toGen) {
		for (int attempt = 0; attempt < 100; attempt++) {
			Node[] start = new Node[5];
			String key = "";
			for (int i : startNodes) {
				start[i] = new Node(NodeType.FIGHT, 0, i);
				key += i;
			}
			key += "->" + toGen;
			int endSize = 0;
			String resultStr = "";
			for (Node n : start) {
				if (n == null) continue;
				resultStr += n.getLane() + ">";
				for (Node dest : n.getDestinations()) {
					endSize++;
					resultStr += dest.getLane();
				}
				resultStr += "|";
			}
			
			resultKeys.add(key);
			if (endSize < toGen) {
				HashMap<String, Integer> failedResult = failedResults.getOrDefault(key, new HashMap<String, Integer>());
				failedResult.put(resultStr, failedResult.getOrDefault(resultStr, 0) + 1);
				failedResults.putIfAbsent(key, failedResult);
			}
			else {
				HashMap<String, Integer> result = results.getOrDefault(key, new HashMap<String, Integer>());
				result.put(resultStr, result.getOrDefault(resultStr, 0) + 1);
				results.putIfAbsent(key, result);
			}
		}
	}
}
