package it.polito.tdp.PremierLeague;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.PremierLeague.model.Model;
import it.polito.tdp.PremierLeague.model.Player;
import it.polito.tdp.PremierLeague.model.PlayersPair;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController 
{
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCreaGrafo;

    @FXML
    private Button btnTopPlayer;

    @FXML
    private Button btnDreamTeam;

    @FXML
    private TextField txtK;

    @FXML
    private TextField txtGoals;

    @FXML
    private TextArea txtResult;
    
    private Model model;
    

    @FXML
    void doCreaGrafo(ActionEvent event) 
    {
    	String scoredGoalsInput = this.txtGoals.getText();
    	
    	if(scoredGoalsInput == null || scoredGoalsInput.isBlank())
    	{
    		this.txtResult.setText("Errore: inserire un valore di goal fatti (x)");
    		return;
    	}
    	
    	scoredGoalsInput = scoredGoalsInput.trim();
    	
    	double scoredGoals;
    	try
		{
			scoredGoals = Double.parseDouble(scoredGoalsInput);
		}
		catch(NumberFormatException nfe)
		{
			this.txtResult.setText("Errore: inserire un numero decimale valido di goal fatti");
    		return;
		}
    	
    	this.model.createGraph(scoredGoals);
    	
    	//print
    	int numVertices = this.model.getNumVertices();
    	int numEdges = this.model.getNumEdges();
    	
    	String output = this.printGraphInfo(numVertices, numEdges);
    	this.txtResult.setText(output);
    }

    private String printGraphInfo(int numVertices, int numEdges)
	{    	
		return String.format("Grafo creato\n#Vertici: %d\n#Archi: %d", numVertices, numEdges);
	}

    @FXML
    void doTopPlayer(ActionEvent event) 
    {
    	if(!this.model.isGraphCreated())
    	{
    		this.txtResult.setText("Errore: creare prima il grafo!");
    		return;
    	}
    	
    	Collection<Collection<PlayersPair>> topPlayersAndBeatenOpponents = 
    			this.model.getTopPlayersAndBeatenOpponents();
    	
    	String output = this.printOrdered(topPlayersAndBeatenOpponents);
    	this.txtResult.setText(output);
    }
    
	private String printOrdered(Collection<Collection<PlayersPair>> topPlayersAndBeatenOpponents)
	{
		if(topPlayersAndBeatenOpponents.isEmpty())
			return "Non esistono top players nel grafo così creato!";
		
		StringBuilder sb = new StringBuilder();
		
		for(Collection<PlayersPair> topPlayerBeatenOpponents : topPlayersAndBeatenOpponents)
		{
			if(topPlayerBeatenOpponents.isEmpty()) continue;
			
			Player topPlayer = topPlayerBeatenOpponents.iterator().next().getPlayer1();
			
			sb.append("\nTop player: ").append(topPlayer.toString()).append("\nAvversari battuti:");
			
			List<PlayersPair> orderedOpponentsPairs = new ArrayList<>(topPlayerBeatenOpponents);
			orderedOpponentsPairs.sort((pair1, pair2) -> 
					Integer.compare(pair2.getMinutesDifference(), pair1.getMinutesDifference()));
			
			for(var pair : orderedOpponentsPairs)
			{
				Player beatenOpponent = pair.getPlayer2();
				int minutesDiff = pair.getMinutesDifference();
				
				sb.append("\n - ").append(beatenOpponent).append(" --> differenza: ");
				sb.append(minutesDiff).append(" minuti");
			}
			
			sb.append("\n");
		}
		
		if(sb.length() > 0)
			sb.deleteCharAt(0); //remove start '\n'
		
		return sb.toString();
	}

	@FXML
    void doDreamTeam(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
    	{
    		this.txtResult.setText("Errore: creare prima il grafo!");
    		return;
    	}
		
		String numPlayersInput = this.txtGoals.getText();
    	
    	if(numPlayersInput == null || numPlayersInput.isBlank())
    	{
    		this.txtResult.setText("Errore: inserire un valore di # Giocatori (k)");
    		return;
    	}
    	
    	numPlayersInput = numPlayersInput.trim();
    	
    	int numPlayers;
    	try
		{
			numPlayers = Integer.parseInt(numPlayersInput);
		}
		catch(NumberFormatException nfe)
		{
			this.txtResult.setText("Errore: inserire un numero intero valido di # Giocatori");
    		return;
		}
    	
    	if(numPlayers < 1)
    	{
    		this.txtResult.setText("Errore: inserire un numero intero positivo di # Giocatori");
    		return;
    	}
		
		Collection<List<Player>> dreamTeams = this.model.computeDreamTeamsOf(numPlayers);
		int maxStartingGrade = this.model.getMaxStartingGrade();
		
		String output = this.print(dreamTeams, maxStartingGrade, numPlayers);
		this.txtResult.setText(output);
    }

    private String print(Collection<List<Player>> dreamTeams, int maxStartingGrade, int numPlayers)
	{
		if(dreamTeams.isEmpty())
			return "Non esistono dream team!";
		
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		for(List<Player> dreamTeam : dreamTeams)
		{
			count++;
			sb.append("\nDream team n.").append(count).append(" da ").append(numPlayers).append(" giocatori:");
			
			for(Player p : dreamTeam)
				sb.append("\n - ").append(p);
			
			sb.append("\n");
		}
		
		if(sb.length() > 0)
			sb.deleteCharAt(0); //remove start '\n'
		
		sb.append("Massimo grado di titolarità del/dei dream team: ").append(maxStartingGrade);
		
		return sb.toString();
	}

	@FXML
    void initialize() 
    {
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnTopPlayer != null : "fx:id=\"btnTopPlayer\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnDreamTeam != null : "fx:id=\"btnDreamTeam\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtK != null : "fx:id=\"txtK\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtGoals != null : "fx:id=\"txtGoals\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
    }
    
    public void setModel(Model model)
    {
    	this.model = model;
    }
}
