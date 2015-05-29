import java.util.Vector;

import soc.qase.file.bsp.BSPBrush;
import soc.qase.file.bsp.BSPParser;
import soc.qase.state.Entity;
import soc.qase.state.Inventory;
import soc.qase.state.World;

/** Actualiza el estado del bot.
 * 
 *  @author Luis Mª González Medina - Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 */

public class Estado {
	//**********************************************************
	//+++++++++++++++++++CONSTANTES+++++++++++++++++++++++++++++
	//**********************************************************
	/** Constante para indicar el nivel de vida que será crítico para el bot */
	public final static int ConstPocaVida=20;	
	/** Constante para indicar el nivel de escudo que será crítico para el bot */
	public final static int ConstPocoEscudo=15;
	
	//**********************************************************
	//++++++++++++++ATRIBUTOS DE LA CLASE+++++++++++++++++++++++
	//**********************************************************
	private MiBotseMueve jugador;
	private World w;
	private BSPParser mibsp;
	
	//**********************************************************
	//++++++++++++++++MÉTODOS DE LA CLASE+++++++++++++++++++++++
	//**********************************************************
	
	
	/**
	 * Constructor de la clase  
	 */
    public Estado (MiBotseMueve J,World W,BSPParser bsp)
    {        
    	jugador=J;
    	w=W;
    	mibsp=bsp;    	
    }

    
    /**
     * Función que actualiza los elementos de estado del bot
     */
	public void actualizar() {		
		jugador.PocaMunicion = ComprobarMunicion();
		jugador.PocaVida = ComprobarVida();
		jugador.PocoEscudo = ComprobarEscudo();
		jugador.Enemigo = DecidirEnemigo();
		jugador.Combatiendo = EnCombate();
		jugador.TengoArma=ComprobarArma();	
		ActualizaTiemposAcuaticos();
	}
	
	private void ActualizaTiemposAcuaticos() {
		//Si no estamos en el agua
		if ((jugador.BotUbicacion!=Ubicacion.BajoAgua) &&
		    (jugador.BotUbicacion!=Ubicacion.SobreAgua)) {
			
			jugador.BuscarTuberias = true;
			
			jugador.TiempoEnAgua=0;
			
			if (jugador.TiempoFueraDeAgua<1) //Lo iniciamos
				jugador.TiempoFueraDeAgua=System.currentTimeMillis();
			else
				if ((System.currentTimeMillis() - jugador.TiempoFueraDeAgua ) > MiBotseMueve.TiempoFueraDeAguaMin)
					jugador.ExploracioAcuaticaActiva=true;
				else
					jugador.ExploracioAcuaticaActiva=false;
		}
		else { //estamos en el agua
			jugador.TiempoFueraDeAgua=0;
			
			if (jugador.TiempoEnAgua<1) //Lo iniciamos
				jugador.TiempoEnAgua=System.currentTimeMillis();	
			
		}		
	}


	/** Devuelve true si el jugador ya ha recogido algún arma. False si sólo tiene la blaster
	 * 
	 * @return
	 */
	private boolean ComprobarArma() {
		int[] Elementos = w.getInventory().getCount(Inventory.SHOTGUN, Inventory.BFG10K);
		for (int i = 0; i< Elementos.length; i ++){
			if (i==Inventory.GRENADES-Inventory.SHOTGUN) //No contamos las granadas como arma
				continue;
			if (Elementos[i]!=0)
				return true;
		}	
		return false;
	}


	/**
	 * Función que determina el enemigo al que atacamos. En caso de ya disponer de un 
	 * enemigo visible, continuamos atacando al mismo. En otro caso elegimos un nuevo
	 * enemigo.
	 * @return indice del enemigo en el vector de entidades
	 */
	private int DecidirEnemigo() {
			Entity Enemigo;			
			// Si estabamos atacando a un enemigo, y este continua siendo visible
			// continuamos atacando a este enemigo
			if (jugador.Enemigo!=-1){							
				Enemigo=w.getEntity(jugador.Enemigo);
				//si es visible retornamos a este mismo
				if (Enemigo.hasDied() || !Enemigo.isRespawned()){
					jugador.Enemigo=-1;
				}
				else
				if (mibsp.isVisible(jugador.PosBot,Enemigo.getOrigin().toVector3f())) 
					return jugador.Enemigo;			
			}			
			// En caso de no estar enfrentándonos a un enemigo, o de que el 
			//enemigo deje de ser visible elegimos un nuevo enemigo.			
			return ElegirNuevoEnemigo();		
	}

	/**
	 * Función que nos devuelve el enemigo visible más cercano, 
	 *  si no existe enemigo visible nos devolverá el enemigo más cercano
	 * @return
	 */
	private int ElegirNuevoEnemigo() {
		int i;
		float dist=20000;
		
		@SuppressWarnings("unchecked")
		Vector<Entity> Enemigos = w.getOpponents(true);
		Entity aux;
		int result = -1;
		//obtenemos un vector con todos los enemigos posibles
		//y comprobamos enemigo a enemigo sus condiciones
		for(i=0;i<Enemigos.size();i++){
			aux=Enemigos.elementAt(i);
			//En caso de tratarse de un compañero de equipo
			//lo descartamos como enemigo
			if (aux.getName().contains("SALPICA")) continue;
			if (aux.hasDied() || !aux.isRespawned()) continue;				
			//comprobamos que el enemigo es visible, y si lo es, comprobamos
			//ademas que se trate del más cercano
			if (mibsp.isVisible(jugador.PosBot,aux.getOrigin().toVector3f())){
				
				float auxDist =jugador.PosBot.distance(aux.getOrigin().toVector3f());
				//comprobación de que efectivamente es el más cercano
				if(auxDist<dist){
					result = aux.getNumber();
					dist = auxDist;
				}
					
			}			
		}
		if (result != -1) return result;
		else return jugador.EnemigoMasCercano();
	}


	
	/**
	 * Función que devuelve true si te encuentra combatiendo.
	 * Devuelve false en cualquier otro caso.
	 * @return
	 */
	private boolean EnCombate() {
		if(jugador.Enemigo != -1){
			mibsp.setBrushType(BSPBrush.CONTENTS_SOLID);
			return mibsp.isVisible(jugador.PosBot, w.getEntity(jugador.Enemigo).getOrigin().toVector3f());
		}		
		return false;
	}

	/**
	 * Función que comprueba que el nivel de escudo no se encuentre por debajo de un mínimo
	 * @return
	 */
	private boolean ComprobarEscudo() {
	 		return (w.getPlayer().getArmor() < ConstPocoEscudo);
	}

	/**
	 * Función que comprueba que el nivel de vida no se encuentre por debajo de un mínimo
	 * @return
	 */
	private boolean ComprobarVida() {
		return (w.getPlayer().getHealth() < ConstPocaVida);
	}


	/**
	 * Función que comprueba el nivel de munición
	 * @return
	 */
	private boolean ComprobarMunicion() {
		
		switch (jugador.IndiceArmas()){
		case(Inventory.BLASTER): 
			return true;
		
		case(Inventory.SHOTGUN):
		case(Inventory.SUPER_SHOTGUN):	
			if (w.getInventory().getCount(Inventory.SHELLS)<2)
				return true;
			return false;
		
		case(Inventory.MACHINEGUN):
		case(Inventory.CHAINGUN):
			if (w.getInventory().getCount(Inventory.BULLETS)<5)
				return true;
			return false;
		
		case(Inventory.GRENADE_LAUNCHER):
			if (w.getInventory().getCount(Inventory.GRENADES)<2)
				return true;
			return false;
		
		case(Inventory.ROCKET_LAUNCHER):
			if (w.getInventory().getCount(Inventory.ROCKETS)<2)
				return true;
			return false;
		
		case(Inventory.HYPERBLASTER):
			if (w.getInventory().getCount(Inventory.CELLS)<5)
				return true;
			return false;
			
		case(Inventory.RAILGUN):
			if (w.getInventory().getCount(Inventory.SLUGS)<1)
				return true;
			return false;
			
		case(Inventory.BFG10K):
			if (w.getInventory().getCount(Inventory.CELLS)<50)
				return true;
			return false;
		default:
			return false;
		}	
		
	}
    
}
