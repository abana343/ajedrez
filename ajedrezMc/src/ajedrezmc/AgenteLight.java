package ajedrezmc;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Javier Aros
 */
public class AgenteLight {
    Board b; 			/// a copy of the current board.


    Move bestoverallmove;  		/// the best move we have overall
    public ScheduleRunner timeout; 	/// this holds the method that we will execute once our time limit is up.
    public int nodesexpanded;	/// Number of nodes exanded
    public int maxdepthreached;	/// The max depth reached by the search
    

    public int algorithm; 		/// which algorithm to use in the search

    static final int MAXDEPTH=8; 	/// How deep we will search without limits
    static final int MINIMAX=1;	/// Do a minimax search
    static final int ALPHABETA=2; 	/// Do a minimax search with alpha-beta pruning
    static final int INFINITY=99999999;
    static final int WINVALUE=999999;
    static final int LOSSVALUE=-WINVALUE;
    Random r;
    
    int turn ;
    NodoMCTS raiz;    
    NodoMCTS movimiento = null;
    int maxProfundidad;
    int profundidad;
    
    ArrayList<NodoMCTS> lista;

    public AgenteLight(Board b){

        timeout = new ScheduleRunner();
        r=new Random();
        turn = 0;
        maxProfundidad = 80;
        profundidad = 0;
        lista = new ArrayList<NodoMCTS>();        
        raiz = new NodoMCTS(null,b);
    }
    
    public void select2(Board b)
    {                                             
        for (int i = 0; i < raiz.b.getValidMoves().length; i++) 
        {
            Board bClone = b.clone();
            bClone.makeMove(raiz.b.getValidMoves()[i]);
            this.lista.add(new NodoMCTS(raiz, bClone));
            this.lista.get(i).mov = raiz.b.getValidMoves()[i];
            //System.out.println(raiz.b.getValidMoves()[i]);
        }
        
        NodoMCTS evaluado = null;
        boolean seleccion = true;
        int aux = 0 ;
        while(seleccion && aux <1000)
        {
            profundidad = 0;
            evaluado = this.expand2();     
            if(this.movimiento == null)                
            {
                this.movimiento = evaluado;
                this.bestoverallmove = this.movimiento.mov;
            }
            else if(this.movimiento.mov != null && evaluado.mov !=null)
            {                
               // System.out.println("intentos: "+evaluado.intentos);
                //System.out.println("aciertos: "+evaluado.aciertos);
               // System.out.println("Fallos: "+evaluado.fallos);
                //System.out.println("intentos: "+movimiento.intentos);
              //  System.out.println("aciertos: "+movimiento.aciertos);
               // System.out.println("Fallos: "+movimiento.fallos);
             //   System.out.println("----");
                if(evaluado.aciertos/evaluado.intentos > this.movimiento.aciertos/this.movimiento.intentos)
                {
                    this.movimiento = evaluado;
                    this.bestoverallmove = this.movimiento.mov;
                }                
            }
            aux+=1;
        }
                
    }      
    
    public NodoMCTS expand2()
    {
        int a = r.nextInt(this.lista.size());                
        this.lista.get(a).intentos+=1; 
        turn = 0;
        simulate(this.lista.get(a));
        return this.lista.get(a);
    }
    
    public void simulate(NodoMCTS padre)
    {      
        if(profundidad > maxProfundidad)
        {
            padre.fallos+=1;
            update(padre.padre, padre);   
            return;
        }
        if(turn ==0 && padre.b.isCheckMate())
        {           
            padre.aciertos+=1;  
            update(padre.padre, padre);
            profundidad+=1;
            return;
        }
        else if(turn == 1 && padre.b.isCheckMate())
        {
           padre.fallos+=1;
           update(padre.padre, padre);
           profundidad+=1;
           return;
        }
        else if(padre.b.isStalemate())
        {         
           padre.fallos+=1;
           update(padre.padre, padre);
           profundidad+=1;
           return;
        }
        else
        {
            if(turn==0) turn = 1;
            else turn =0;            
            //System.out.println(padre.b.getValidMoves().length);
            int a = r.nextInt(padre.b.getValidMoves().length);
            Board bClone = padre.b.clone();
            bClone.makeMove(padre.b.getValidMoves()[a]);
            NodoMCTS hijo = new NodoMCTS(padre, bClone); 
            hijo.mov = padre.b.getValidMoves()[a];            
            profundidad+=1;
            simulate(hijo);        
            update(padre.padre, padre);              
            return;
        }
    }
    
    public void update(NodoMCTS padre , NodoMCTS hijo)
    {
        padre.aciertos += hijo.aciertos;
        padre.fallos += hijo.fallos;
        hijo = null;
    }
    
    
    class MoveValue
	{
	    public Move move;
	    public int value;
            public int numNodosVisitados = 0;
            public int valorTotal;
	    
	    MoveValue() 
	    {
		move=null;
		value=0;
	    }
	    
	    MoveValue(Move m) 
	    {
	      this.move=m;
	      value=0;
	    }
	    
	    MoveValue(int v)
	    {
	      this.value=v;
	      this.move=null;
	    }
	    
	    
	    MoveValue(Move m, int v)
	    {
	      this.value=v;
	      this.move=m;
	    }
	}
        
        
        //inner class
	class ScheduleRunner extends TimerTask
   	{
   		/**
    		* executed when time is up.
    		*/
		public void run()
		{
			//time's up. Print out the best move and exit.
			System.out.println(bestoverallmove);
			System.out.println("Nodes Expanded:" +nodesexpanded);
			System.out.println("Max Depth Reached:" + maxdepthreached);
			System.exit(0);
		}
	} // end inner class ScheduleRunner
        
        
        
        
         class NodoMCTS{
            ArrayList<NodoMCTS> hijos;
            
            Move mov;
            NodoMCTS padre;            
            int intentos;
            int aciertos;
            int fallos;
            Board b;
            
            public NodoMCTS(NodoMCTS padre, Board b){
                this.padre = padre;
                intentos = 0;
                aciertos = 0;
                fallos = 0;
                hijos =  new ArrayList<>();
                mov = null;
                this.b = b;
            }
            
            
        }
        
        public static void main(String[] args) {

		int[][] board = new int[8][8];
		Board b=new Board();
		//IDSAgent ids=new IDSAgent(Integer.parseInt(args[0]));
		//ids.utility=new MaterialValue();
                AgenteLight aL= new AgenteLight(b);
		Timer t = new Timer();
		
		//convert the numeric value given as parameter 3 into minutes, and then give 10 second leeway to return a response
//long limit=(Integer.parseInt(args[1]))*60000-10000;
		//schedule the timeout.. if we pass the timeout, the program will exit.
//t.schedule(aL.timeout, limit);
	
		try {                    
                    BufferedReader input =   new BufferedReader(new FileReader(args[0]));
			for (int i=0; i<8; i++) {
				String line=input.readLine();
				String[] pieces=line.split("\\s");
				for (int j=0; j<8; j++) {
					board[i][j]=Integer.parseInt(pieces[j]);
				}
			}
			String turn=input.readLine();
			b.fromArray(board);                        
			if (turn.equals("N")) b.setTurn(b.TURNBLACK);
			else b.setTurn(b.TURNWHITE);
			b.setShortCastle(b.TURNWHITE,false);
			b.setLongCastle(b.TURNWHITE,false);
			b.setShortCastle(b.TURNBLACK,false);
			b.setLongCastle(b.TURNBLACK,false);
		
			String st=input.readLine();
			while (st!=null) {
				if (st.equals("EnroqueC_B")) b.setShortCastle(b.TURNWHITE,true);
				if (st.equals("EnroqueL_B")) b.setLongCastle(b.TURNWHITE,true);
				if (st.equals("EnroqueC_N")) b.setShortCastle(b.TURNBLACK,true);
				if (st.equals("EnroqueL_N")) b.setLongCastle(b.TURNBLACK,true);
				st=input.readLine();
			}
		} catch (Exception e) {}
		
		//Move move=ids.getBestMove(b,MAXDEPTH);
                aL.raiz.b = b;
                aL.select2(b);
                /**para ver lo que captura.
                int cap = board[move.dest.x][move.dest.y];
                String captura = "C";
                if(cap<0) captura= "CN";
		System.out.println(move+"-"+captura+"-"+cap);**/  
                //System.out.println("intentos: "+aL.movimiento.intentos);
                //System.out.println("aciertos: "+aL.movimiento.aciertos);
                //System.out.println("Fallos: "+aL.movimiento.fallos);
                System.out.println(aL.movimiento.mov);
		System.out.println("Nodes Expanded:" +aL.nodesexpanded);
		System.out.println("Max Depth Reached:" + aL.maxdepthreached);
		System.exit(0);
	}
}




