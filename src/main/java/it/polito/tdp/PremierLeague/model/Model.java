package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model 
{
	private final PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> graph;
	private final Map<Integer, Player> playersIdMap;
	
	private Collection<List<Player>> dreamTeams;
	private int maxStartingGrade; 
	
	
	public Model()
	{
		this.dao = new PremierLeagueDAO();
		this.playersIdMap = new HashMap<>();
	}

	public void createGraph(double minAvgScoredGoals)
	{
		this.graph = GraphTypeBuilder.<Player, DefaultWeightedEdge>directed()
									.allowingMultipleEdges(false)
									.allowingSelfLoops(false)
									.weighted(true)
									.edgeClass(DefaultWeightedEdge.class)
									.buildGraph();
		
		//add vertices
		Collection<Player> vertices = 
				this.dao.getPlayersWithAvgGoalsHigherThan(minAvgScoredGoals, this.playersIdMap);
		
		Graphs.addAllVertices(this.graph, vertices);
		
		//add edges
		Collection<PlayersPair> allPairs = 
				this.dao.getAllPlayersPairs(minAvgScoredGoals, this.playersIdMap);
		
		for(var pair : allPairs)
		{
			Player source = pair.getPlayer1();
			Player target = pair.getPlayer2();
			int weight = pair.getMinutesDifference();
			
			Graphs.addEdge(this.graph, source, target, (double)weight);
		}
	}

	public int getNumVertices() { return this.graph.vertexSet().size(); }
	public int getNumEdges() { return this.graph.edgeSet().size(); }
	public boolean isGraphCreated() { return this.graph != null; }

	public Collection<Collection<PlayersPair>> getTopPlayersAndBeatenOpponents()
	{
		if(this.graph == null) return null;
		
		Collection<Collection<PlayersPair>> topPlayersAndBeatenOpponents = new HashSet<>();
		int maxBeatenOpponents = Integer.MIN_VALUE;
		
		for(Player player : this.graph.vertexSet())
		{
			Collection<PlayersPair> beatenOpponentsPairs = new HashSet<>();
			
			for(var edge : this.graph.outgoingEdgesOf(player))
			{
				int minutesDiff = (int)this.graph.getEdgeWeight(edge);
				Player beatenOpponent = Graphs.getOppositeVertex(this.graph, edge, player);
				PlayersPair pair = new PlayersPair(player, beatenOpponent, minutesDiff);
				beatenOpponentsPairs.add(pair);
			}
			
			int numBeatenOpponents = beatenOpponentsPairs.size();
			
			if(numBeatenOpponents >= maxBeatenOpponents)
			{
				if(numBeatenOpponents > maxBeatenOpponents)
				{
					maxBeatenOpponents = numBeatenOpponents;
					topPlayersAndBeatenOpponents.clear();
				}
				topPlayersAndBeatenOpponents.add(beatenOpponentsPairs);
			}
		}

		return topPlayersAndBeatenOpponents;
	}

	public Collection<List<Player>> computeDreamTeamsOf(int numPlayers)
	{
		if(this.graph == null) return null;
		
		this.dreamTeams = new HashSet<>();
		this.maxStartingGrade = Integer.MIN_VALUE;
		
		List<Player> partialSolution = new ArrayList<>();
		List<Player> allPlayers = new ArrayList<>(this.graph.vertexSet());
		
		this.recursiveDreamTeamsComputation(partialSolution, allPlayers, 0, numPlayers, 0);
		
		return this.dreamTeams;
	}
	
	/**
	 * internal recursive call to compute dream teams
	 * @param partialSolution
	 * @param allPlayers	
	 * @param level  * it represents the index(in allPlayers list) of next player to add to partial solution *
	 * @param numPlayers	length of dream team
	 * @param currentTeamStartingGrade
	 */
	private void recursiveDreamTeamsComputation(List<Player> partialSolution, 
			List<Player> allPlayers, int level,	int numPlayers, int currentTeamStartingGrade)
	{
		if(allPlayers.size() == numPlayers)	//terminal case
		{
			if(currentTeamStartingGrade >= this.maxStartingGrade)
			{
				if(currentTeamStartingGrade > this.maxStartingGrade)
				{
					this.maxStartingGrade = currentTeamStartingGrade;
					this.dreamTeams.clear();
				}
				List<Player> newDreamTeam = new ArrayList<>(partialSolution);
				this.dreamTeams.add(newDreamTeam);
			}
			
			return; //stop recursion
		}
		
		for(int i = level; i < allPlayers.size(); i++)
		{
			Player playerToAdd = allPlayers.get(i);
			
			if(partialSolution.contains(playerToAdd) ||
					this.hasBeenBeatenFrom(partialSolution, playerToAdd))
				continue;	//filter
			
			partialSolution.add(playerToAdd);
			int newPlayerStartingGrade = this.computeStartingGradeOf(playerToAdd);
			
			//recursive call
			this.recursiveDreamTeamsComputation(partialSolution, allPlayers, 
					i+1, numPlayers, currentTeamStartingGrade + newPlayerStartingGrade);
			
			//backtracking
			partialSolution.remove(partialSolution.size() - 1);
		}
	}

	private boolean hasBeenBeatenFrom(List<Player> partialSolution, Player playerToAdd)
	{
		for(Player player : partialSolution)
		{
			Set<Player> beatenOpponents = new HashSet<>(Graphs.successorListOf(this.graph, player));
			
			if(beatenOpponents.contains(playerToAdd))
				return true;
		}
		//player not beaten
		return false;
	}

	private int computeStartingGradeOf(Player player)
	{
		if(!this.graph.containsVertex(player))
			throw new IllegalArgumentException("player not found in the graph");
		
		int totOutgoingWeight = 0;
		int totIncomingWeight = 0;
		
		for(var outgoingEdge : this.graph.outgoingEdgesOf(player))
		{
			int weight = (int)this.graph.getEdgeWeight(outgoingEdge);
			totOutgoingWeight += weight;
		}
		
		for(var incomingEdge : this.graph.incomingEdgesOf(player))
		{
			int weight = (int)this.graph.getEdgeWeight(incomingEdge);
			totIncomingWeight += weight;
		}
		
		return totOutgoingWeight - totIncomingWeight;
	}

	public int getMaxStartingGrade()
	{
		return this.maxStartingGrade;
	}

}
