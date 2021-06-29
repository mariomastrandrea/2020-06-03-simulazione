package it.polito.tdp.PremierLeague.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import it.polito.tdp.PremierLeague.model.Action;
import it.polito.tdp.PremierLeague.model.Player;
import it.polito.tdp.PremierLeague.model.PlayersPair;

public class PremierLeagueDAO 
{	
	public List<Player> listAllPlayers(){
		String sql = "SELECT * FROM Players";
		List<Player> result = new ArrayList<Player>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Player player = new Player(res.getInt("PlayerID"), res.getString("Name"));
				
				result.add(player);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> listAllActions(){
		String sql = "SELECT * FROM Actions";
		List<Action> result = new ArrayList<Action>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Action action = new Action(res.getInt("PlayerID"),res.getInt("MatchID"),res.getInt("TeamID"),res.getInt("Starts"),res.getInt("Goals"),
						res.getInt("TimePlayed"),res.getInt("RedCards"),res.getInt("YellowCards"),res.getInt("TotalSuccessfulPassesAll"),res.getInt("totalUnsuccessfulPassesAll"),
						res.getInt("Assists"),res.getInt("TotalFoulsConceded"),res.getInt("Offsides"));
				
				result.add(action);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Collection<Player> getPlayersWithAvgGoalsHigherThan(double minAvgScoredGoals,
			Map<Integer, Player> playersIdMap)
	{
		final String sqlQuery = String.format("%s %s %s %s %s",
				"SELECT p.PlayerID AS id, p.Name AS name, AVG(a.Goals) AS avgGoals",
				"FROM Actions a, Players p",
				"WHERE a.PlayerID = p.PlayerID",
				"GROUP BY p.PlayerID, p.Name",
				"HAVING avgGoals > ?");
		
		Collection<Player> players = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setDouble(1, minAvgScoredGoals);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int playerId = queryResult.getInt("id");
				
				if(!playersIdMap.containsKey(playerId))
				{
					String playerName = queryResult.getString("name");
					Player newPlayer = new Player(playerId, playerName);
					playersIdMap.put(playerId, newPlayer);
				}
				
				players.add(playersIdMap.get(playerId));
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return players;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getPlayersWithAvgGoalsHigherThan()", sqle);
		}
	}
	
	public Collection<PlayersPair> getAllPlayersPairs(double minAvgScoredGoals, Map<Integer, Player> playersIdMap)
	{
		final String sqlQuery = String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
				"SELECT a1.PlayerID AS id1, a2.PlayerID AS id2,",
					"(SUM(a1.TimePlayed) - SUM(a2.TimePlayed)) AS diffMins",
				"FROM Actions a1, Actions a2",
				"WHERE  a1.MatchID = a2.MatchID AND a1.TeamID <> a2.TeamID",
					"AND a1.Starts = 1 AND a2.Starts = 1",
					"AND a1.PlayerID IN (SELECT PlayerID",
										"FROM Actions",
										"GROUP BY PlayerID",
										"HAVING AVG(Goals) > ?)",
					"AND a2.PlayerID IN (SELECT PlayerID",
										"FROM Actions",
										"GROUP BY PlayerID",
										"HAVING AVG(Goals) > ?)",
				"GROUP BY a1.PlayerID, a2.PlayerID",
				"HAVING SUM(a1.TimePlayed) > SUM(a2.TimePlayed)");
		
		Collection<PlayersPair> playersPairs = new HashSet<>();
		
		try
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setDouble(1, minAvgScoredGoals);
			statement.setDouble(2, minAvgScoredGoals);
			ResultSet queryResult = statement.executeQuery();
			
			while(queryResult.next())
			{
				int playerId1 = queryResult.getInt("id1");
				int playerId2 = queryResult.getInt("id2");
				int minutesDiff = queryResult.getInt("diffMins");
				
				if(!playersIdMap.containsKey(playerId1) ||
					!playersIdMap.containsKey(playerId2))
					throw new RuntimeException("playerId not found in id map"); //for debug
				
				Player playerSource = playersIdMap.get(playerId1);
				Player playerTarget = playersIdMap.get(playerId2);
				
				PlayersPair newPair = new PlayersPair(playerSource, playerTarget, minutesDiff);
				playersPairs.add(newPair);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return playersPairs;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllPlayersPairs()", sqle);
		}
	}
}
