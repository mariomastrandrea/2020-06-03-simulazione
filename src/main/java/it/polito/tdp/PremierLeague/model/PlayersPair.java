package it.polito.tdp.PremierLeague.model;

public class PlayersPair
{
	private final Player player1;
	private final Player player2;
	private final int minutesDifference;
	
	public PlayersPair(Player player1, Player player2, int minutesDifference)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.minutesDifference = minutesDifference;
	}

	public Player getPlayer1()
	{
		return this.player1;
	}

	public Player getPlayer2()
	{
		return this.player2;
	}

	public int getMinutesDifference()
	{
		return this.minutesDifference;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((player1 == null) ? 0 : player1.hashCode());
		result = prime * result + ((player2 == null) ? 0 : player2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayersPair other = (PlayersPair) obj;
		if (player1 == null)
		{
			if (other.player1 != null)
				return false;
		}
		else
			if (!player1.equals(other.player1))
				return false;
		if (player2 == null)
		{
			if (other.player2 != null)
				return false;
		}
		else
			if (!player2.equals(other.player2))
				return false;
		return true;
	}
}
