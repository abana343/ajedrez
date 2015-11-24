/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ajedrezmc;

import static ajedrezmc.AgenteLight.WINVALUE;
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
public class AgentJ
{
    Board b; 			/// a copy of the current board.


    Move bestoverallmove;  		/// the best move we have overall
    public ScheduleRunner timeout; 	/// this holds the method that we will execute once our time limit is up.
    public int nodesexpanded;	/// Number of nodes exanded
    public int maxdepthreached;	/// The max depth reached by the search
    

    public int algorithm; 		/// which algorithm to use in the search

    static final int MAXDEPTH=4; 	/// How deep we will search without limits
    static final int MINIMAX=1;	/// Do a minimax search
    static final int ALPHABETA=2; 	/// Do a minimax search with alpha-beta pruning
    static final int INFINITY=99999999;
    static final int WINVALUE=999999;
    static final int LOSSVALUE=-WINVALUE;
    Random r;
    public Heuristic utility;
    int turn ;
    NodoMCTS raiz;    
    NodoMCTS movimiento = null;
    int maxProfundidad;
    int profundidad;
    
    ArrayList<NodoMCTS> lista;
    NodoMCTS nodoMejorEvaluado;
    
    static final int TURNBLACK=-1; 
    static final int TURNWHITE=1;
    private int jugador;

    public AgentJ(Board b){
        utility = new MaterialValue();
        timeout = new ScheduleRunner();
        r=new Random();
        turn = 0;
        maxProfundidad = 80;

        lista = new ArrayList<NodoMCTS>();        
        raiz = new NodoMCTS(null,b);
        Move mov = new Move();
        raiz.mov=mov;
        //lista.add(raiz);
        
        //mejor jugada random //jugada inicial
        int a = r.nextInt(b.getValidMoves().length);
        Move movimiento = b.getValidMoves()[a];
        Board clone = b.clone();
        clone.makeMove(movimiento);
        nodoMejorEvaluado = new NodoMCTS(raiz, clone);
        nodoMejorEvaluado.mov=movimiento;
        nodoMejorEvaluado.intentos=1;
        jugador = b.turn;
    }

    
    
    
    
    public void inicializarArbol(){
        for(Move move:raiz.b.getValidMoves()){
            Board clone = raiz.b.clone();
            clone.makeMove(move);
            NodoMCTS hijo = new NodoMCTS(raiz, clone);
            hijo.mov= move;
            
            raiz.hijos.add(hijo);
            simulacion(hijo);
            upgrade(hijo,hijo.aciertos,hijo.fallos,hijo.intentos);
            
            if(hijo.aciertos>0);   //lista de nodos de con algun interes;
                lista.add(hijo);
                actualizaMejorNodo(hijo);                              
        }
    }
    
    //lista con nodos de interes
    public NodoMCTS selectListaNodo(){
        int a = r.nextInt(lista.size());
        return lista.get(a);
    }
    
    //sin nodos buenos
    public NodoMCTS selectSinNodoLista(){
        int a = r.nextInt(raiz.hijos.size());
        return raiz.hijos.get(a);
    }
    
    public NodoMCTS expand(NodoMCTS nodo){
        if (nodo.b.getValidMoves().length!= 0)
        {
            int a = r.nextInt(nodo.b.getValidMoves().length);
            Move movimiento = nodo.b.getValidMoves()[a];
            Board clone = nodo.b.clone();
            clone.makeMove(movimiento);
            NodoMCTS hijo = new NodoMCTS(nodo, clone);
            hijo.mov= movimiento;
            nodo.hijos.add(hijo);
            
            return hijo;
        }
        return null;
    }
    
    
    
    public void MCTS(){
        NodoMCTS nodo;
        inicializarArbol();
        
        for(int i = 0; i <10000 ;i++){
             if(lista.size() >= 3){
                nodo= selectListaNodo();
             }
             else{
                 nodo = selectSinNodoLista();
             }

             if( nodo != null){
                 NodoMCTS hijo = expand(nodo);
                 if (hijo !=null){
                     simulacion(hijo);
                     upgrade(hijo,hijo.aciertos,hijo.fallos,hijo.intentos);
                     actualizaMejorNodo(hijo);
                 }
             }
        }
        
        
    }
    
    
    public void actualizaMejorNodo(NodoMCTS nodo){
        
        
        int profundidadSolucionEncontrada = nodo.profundidadMejorSolucion;
        
        while(nodo.padre.profundidad != 0)
            nodo = nodo.padre;
        if (valorPorcentualNodo(nodo)>valorPorcentualNodo(nodoMejorEvaluado))
        {
            nodoMejorEvaluado = nodo;
        }
        if(valorPorcentualNodo(nodo)==valorPorcentualNodo(nodoMejorEvaluado)){
            
                
            if(nodoMejorEvaluado.profundidadMejorSolucion > profundidadSolucionEncontrada){
                
                nodoMejorEvaluado = nodo;
            }
        }
            
            
    }
    
    public void muestraEstadisticasNodo(NodoMCTS nodo){
        System.out.println("Aciertos: "+ nodo.aciertos + "  Fallas: " + nodo.fallos + "  Intentos: " + nodo.intentos);
        System.out.println("Profundidad: "+nodo.profundidadMejorSolucion);
    }
    
    
    public void muestra3(){
        System.out.println("---------------------");
        
        System.out.println(nodoMejorEvaluado.aciertos+"|"+nodoMejorEvaluado.fallos+"|"+nodoMejorEvaluado.intentos);
        System.out.println(nodoMejorEvaluado.hijos.size() + " mov " + nodoMejorEvaluado.mov.toString());
        System.out.println("------------");
        muestra(raiz);
    }
    
    
    public void muestra2(NodoMCTS nodo){
        System.out.println(nodo.aciertos+"|"+nodo.fallos+"|"+nodo.intentos);
        System.out.println(nodo.hijos.size() + " mov " + nodo.mov.toString());
        for(NodoMCTS hijos: nodo.hijos){
            
            muestra(hijos);
            
        }
    }
    
    public void muestra(NodoMCTS nodo){
        System.out.println(nodo.aciertos+"|"+nodo.fallos+"|"+nodo.intentos);        
        System.out.println(nodo.hijos.size() + " mov " + nodo.mov.toString());
        float mejormov = 0;
        float evaluado;
        NodoMCTS mejornodo = null;
        for(NodoMCTS hijos: nodo.hijos){
            System.out.println(hijos.aciertos+"|"+hijos.fallos+"|"+hijos.intentos);
            System.out.println(hijos.hijos.size() + " mov " + hijos.mov.toString() + " profundidad: " +hijos.profundidadMejorSolucion);
            evaluado = (float)hijos.aciertos/(float)hijos.intentos;
            if (evaluado > mejormov){
                mejormov = evaluado;
                mejornodo = hijos;
            }
        }
        System.out.println("------***-------");   
        if(mejornodo != null){
            System.out.println(mejornodo.aciertos+"|"+mejornodo.fallos+"|"+mejornodo.intentos);    
            System.out.println(mejornodo.hijos.size() + " mov " + mejornodo.mov.toString());
            System.out.println(mejormov);
        }
        
        
    }
   
    
    public void simulacion(NodoMCTS nodo){
        if (nodo.b.turn == 1)
            simulateJugador(nodo);
        else
            simulateEnemigo(nodo);
        
    }
    
    public void upgrade(NodoMCTS nodo,int aciertos,int fallos,int intentos){
        if (nodo.padre != null)
        {
            nodo.padre.aciertos += aciertos;
            nodo.padre.fallos += fallos;
            nodo.padre.intentos += intentos;
            upgrade(nodo.padre,aciertos,fallos,intentos);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //simulacion para piesas negras
    public void simulateEnemigo(NodoMCTS nodo){
        MoveValue bestmove=new MoveValue();
         nodo.intentos += 1;
	    for (int depth=1; depth<MAXDEPTH; depth++) 
	    {
		  
		  bestmove=alphabeta(nodo.b, 1,depth,-INFINITY, INFINITY);  // save the best move for this depth
		  
		  //record the best move for this level in case we need to exit early
		  bestoverallmove=bestmove.move;
		 
		  //sure win, return this move as the best
		  if (bestmove.value==WINVALUE)
                  {
                      if (jugador == TURNWHITE){
                          nodo.fallos += 1;
                      }
                      else{
                          nodo.aciertos += 1;                         
                        //  nodo.profundidadMejorSolucion = depth + nodo.profundidad;
                      }
                      nodo.profundidadMejorSolucion = depth + nodo.profundidad;
                      //System.out.println(bestmove.move +"  " + bestmove.value);
                      return;
                 }
                     
		  //no hope, should resign here
		  if (bestmove.value==LOSSVALUE)
		  {
                      if (jugador == TURNWHITE){
                          nodo.aciertos += 1;
                          
                      }
                      else{
                          
                          nodo.fallos += 1;
                      } 
                      nodo.profundidadMejorSolucion = depth + nodo.profundidad;
		    Move m=new Move();
		    m.setResign(true);
                    //System.out.println(m +"  resign: " + m.resign);
                    
		    return ;
		  }
		  //Record some statistics about the search
		  maxdepthreached=depth;
            }
       
    }
    
    //para piesas blancas
    public void simulateJugador(NodoMCTS nodo){
        MoveValue bestmove=new MoveValue();
         nodo.intentos = 1;
         
	    for (int depth=1; depth<MAXDEPTH; depth++) 
	    {
		  
		  bestmove=alphabeta(nodo.b, 1,depth,-INFINITY, INFINITY);  // save the best move for this depth
		  
		  //record the best move for this level in case we need to exit early
		  bestoverallmove=bestmove.move;
		 
		  //sure win, return this move as the best
		  if (bestmove.value==WINVALUE)
                  {
                      if (jugador == TURNWHITE){
                          nodo.aciertos += 1;                
                      }
                      else{                         
                          nodo.fallos += 1;
                      } 
                       nodo.profundidadMejorSolucion = depth + nodo.profundidad;                  
                      
                      //System.out.println(bestmove.move +"  " + bestmove.value);
                      return;
                 }
                     
		  //no hope, should resign here
		  if (bestmove.value==LOSSVALUE)
		  {
                    if (jugador == TURNWHITE){
                          nodo.fallos += 1;
                      }
                      else{                       
                          nodo.aciertos += 1;
                      } 
                      
                    
		    Move m=new Move();
		    m.setResign(true);
                    //System.out.println(m +"  resign: " + m.resign);
                    nodo.profundidadMejorSolucion = depth + nodo.profundidad;
		    return ;
		  }
		  //Record some statistics about the search
		  maxdepthreached=depth;
            }
       
    }
    


    
    public MoveValue alphabeta(Board b, int currentdepth, int maxdepth, int alpha, int beta) 
	{
	    nodesexpanded++;
	  /*	      
	      if node is a terminal node or depth == 0:
	      return the heuristic value of node
	  */
	    if (b.isCheckMate()) return new MoveValue(LOSSVALUE);
	    if (b.isStalemate()) return new MoveValue(0); 
	    if (currentdepth==maxdepth) return new MoveValue(utility.evaluate(b)*b.turn);
	    Move m=new Move();
	    MoveValue best= new MoveValue(-INFINITY);
	    Move[] moves=b.getValidMoves();
	    //Arrays.sort(moves, m);
	    for (Move currentmove : moves) 
	    {
		Board child=b.clone();
		child.makeMove(currentmove);
		
		MoveValue childmove = alphabeta(child, currentdepth+1,maxdepth,-beta, -alpha);
		childmove.move=currentmove;
		//reverse the sign since we're really evaluating our opponent's board state
		childmove.value=-childmove.value;
		
		if (childmove.value>best.value) {
		    best=childmove;
		    alpha=childmove.value;
		} else if (childmove.value==best.value) 
		// if we have a tie, replace the best move at random (to avoid the problem of loops)
		  //if (r.nextDouble()>0.5)
		    //best=childmove;
		

		if (beta<=alpha) break; 
	    }
            best.profundidad=currentdepth;
	    return best;
	}
    
    
    
    
    class MoveValue
	{
	    public Move move;
	    public int value;
            public int numNodosVisitados = 0;
            public int valorTotal;
	    public int profundidad;
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
            int profundidad;
            int profundidadMejorSolucion = INFINITY;
            
            public NodoMCTS(NodoMCTS padre, Board b){
                this.padre = padre;
                intentos = 0;
                aciertos = 0;
                fallos = 0;
                hijos =  new ArrayList<>();
                mov = null;
                this.b = b;
                
                if(padre != null)   //profundidad del nodo
                    profundidad = padre.profundidad + 1;
                else
                    profundidad =0;
            }
            
            
        }
         
        public float valorPorcentualNodo(NodoMCTS nodo){
            return (float)nodo.aciertos/(float)nodo.intentos;
        }
        
        public static void main(String[] args) {

		int[][] board = new int[8][8];
		Board b=new Board();
		//IDSAgent ids=new IDSAgent(Integer.parseInt(args[0]));
		//ids.utility=new MaterialValue();
                AgentJ aL= new AgentJ(b);
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
                aL.MCTS();
                /**para ver lo que captura.
                int cap = board[move.dest.x][move.dest.y];
                String captura = "C";
                if(cap<0) captura= "CN";
		System.out.println(move+"-"+captura+"-"+cap);**/  
                //System.out.println("intentos: "+aL.movimiento.intentos);
                //System.out.println("aciertos: "+aL.movimiento.aciertos);
                //System.out.println("Fallos: "+aL.movimiento.fallos);
//                System.out.println(aL.movimiento.mov);
                System.out.println(aL.nodoMejorEvaluado.mov);
                aL.muestraEstadisticasNodo(aL.nodoMejorEvaluado);
		//System.out.println("Nodes Expanded:" +aL.nodesexpanded);
		//System.out.println("Max Depth Reached:" + aL.maxdepthreached);
		System.exit(0);
	}
}
