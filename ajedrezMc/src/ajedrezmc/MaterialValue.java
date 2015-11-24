/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ajedrezmc;

import java.util.ArrayList;

public class MaterialValue extends Heuristic
{
	public MaterialValue()
	{
	}
	
	/**
	    Takes a board and returns the heuristic value of the board
	**/
	public int evaluate(Board inb) 
	{
	   ArrayList<Coord> blackpieces=inb.getBlackPieces();
	   ArrayList<Coord> whitepieces=inb.getWhitePieces();
	   
	   int blacksum=0;
	   int whitesum=0;
	   
	   for (int i=0; i<blackpieces.size(); i++) 
	   {
		Coord current=(Coord)blackpieces.get(i);
		switch (inb.getPiece(current))
		{
		  case Board.BLACK_QUEEN:
		    blacksum+=9;
		    break;
		  case Board.BLACK_ROOK:
		    blacksum+=5;
		    break;
		  case Board.BLACK_PAWN:
		    blacksum+=1;
		    break;
		  case Board.BLACK_KNIGHT:
		    blacksum+=3;
		    break;
		  case Board.BLACK_BISHOP:
		    blacksum+=3;
		    break;  
		}
	   }
	   
	   for (int i=0; i<whitepieces.size(); i++) 
	   {
		Coord current=(Coord)whitepieces.get(i);
		switch (inb.getPiece(current))
		{
		  case Board.WHITE_QUEEN:
		    whitesum+=9;
		    break;
		  case Board.WHITE_ROOK:
		    whitesum+=5;
		    break;
		  case Board.WHITE_PAWN:
		    whitesum+=1;
		    break;
		  case Board.WHITE_KNIGHT:
		    whitesum+=3;
		    break;
		  case Board.WHITE_BISHOP:
		    whitesum+=3;
		    break;  
		}
	   }
	   return (whitesum-blacksum);
	}

}
