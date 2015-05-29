import soc.qase.file.bsp.BSPParser;
import soc.qase.state.Entity;
import soc.qase.state.Inventory;
import soc.qase.state.PlayerGun;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;

/** Clase en la que se implementa todo lo relacionado con el combate.
 * Tanto movimiento como apuntado
 * 
 * @author Luis Mª González Medina -  Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 *
 */
public class Combate{
	
	private static final int ConstCortaDistancia = 170;
	private static final int ConstMediaDistancia = 300;
	private static final int ConstLargaDistancia = 500;
	
	private MiBotseMueve jugador;
	private World w;
	private BSPParser mibsp;
	private Vista vista;
	
	/** Indica la posición del enemigo en la iteración anterior */
	public Vector3f EnemigoPosAnterior= new Vector3f(0,0,0);
	/**Controla cuanto llevamos combatiendo (se reinicia cada ConstTiempoCombate) */
    public long TiempoEnCombate=0;
    /**Constante que controlara cada cuanto hacemos un cambio de sentido aleatorio */
    private final long ConstCambioSentido=1000;
    /** Usado para invertir el movimiento de combate */
	private Vector3f cieloTierra = new Vector3f(0,0,-1);
	

	/** Constructor de la clase */
	public Combate(MiBotseMueve J,World W, BSPParser b, Vista v){
		jugador = J;
		w = W;
    	mibsp=b;
    	vista = v;
	}
	

/**
 * Función que se encarga de realizar el movimiento de combate sobre el enemigo selecionado
 */
public void Ataque()
{	
	/** Variable que almacenará las coordenadas de la nueva dirección */
	Vector3f PosFinal = new Vector3f(0,0,0);
			
	Entity enemigo = w.getEntity(jugador.Enemigo); 
	
	if (enemigo!=null){ //En principio no puede ser pero por si acaso...
				
		//Actualizamos la variable de tiempo en combate
		if(TiempoEnCombate<1) //No está inicializada
			TiempoEnCombate=System.currentTimeMillis();	
		
		//Calculamos la dirección a la que el bot ha que moverse
		//Para trazar un círculo.
		PosFinal.add(movimientoDeCombate(enemigo));
			
		//Además intentamos mantener una distancia constante con 
		//el enemigo	
		Vector3f dif = new Vector3f();
		dif.sub(enemigo.getOrigin().toVector3f(),jugador.PosBot);
		
		//Calculamos la distancia optima
		int Dist = CalculaDistanciaOptima();
		
		PosFinal.add(MantieneDistancia(enemigo,Dist));
		
		//Iniciamos el movimiento y la acción de disparar.
		jugador.DirMov=PosFinal;		
		if (jugador.HuecoSueloDelante==Vista.HuecoSaltable)
			jugador.postura=PlayerMove.POSTURE_JUMP;
		else
			jugador.postura=PlayerMove.POSTURE_NORMAL;				
				
		EnemigoPosAnterior.set(enemigo.getOrigin());
	}

}



/** Calcula la distancia óptima a mantener según el arma que tenemos equipada
 * 
 * @return Distancia óptima
 */
private int CalculaDistanciaOptima() {
	int Arma=jugador.IndiceArmas();
	
	if (Arma==PlayerGun.BLASTER)
		return ConstLargaDistancia; //Tendencia a huir
	if (Arma==PlayerGun.BFG10K)
		return ConstMediaDistancia;
	if (Arma==PlayerGun.CHAINGUN)
		return ConstCortaDistancia;
	if (Arma==PlayerGun.GRENADE_LAUNCHER)
		return ConstMediaDistancia;
	if (Arma==PlayerGun.HYPERBLASTER)
		return ConstMediaDistancia;
	if (Arma==PlayerGun.MACHINEGUN)
		return ConstMediaDistancia;
	if (Arma==PlayerGun.RAILGUN)
		return ConstLargaDistancia;
	if (Arma==PlayerGun.ROCKET_LAUNCHER)
		return ConstMediaDistancia;
	if (Arma==PlayerGun.SHOTGUN)
		return ConstCortaDistancia;
	if (Arma==PlayerGun.SUPER_SHOTGUN)
		return ConstCortaDistancia;	
	return ConstMediaDistancia;
}



/** Para el cálculo de la distancia entre 2 puntos haremos uso de la distancia euclídea, 
*que nos dice que la distancia  entre dos puntos es igual a la raiz cuadrada de 
*la suma de la diferencia de sus componentes al cuadrado. */	
private double distancia(Entity enemigo){
	Vector3f resta = new Vector3f();
	resta.sub(enemigo.getOrigin().toVector3f(),jugador.PosBot);
	resta.x =(resta.x*resta.x);
	resta.y = (resta.y*resta.y);
	return Math.sqrt((resta.x+resta.y));
}

/**Funcion que gira en círculos alrededor de un enemigo
 * 
 * @param Enemigo
 * @return Vector3f con la dirección de movimiento
 */
private Vector3f movimientoDeCombate(Entity Enemigo)

{
	Vector3f movimiento = new Vector3f();
	Vector3f dif = new Vector3f();
	dif.sub(Enemigo.getOrigin().toVector3f(),jugador.PosBot);
   
    //Lo multiplicamos vectorialmente por un vector que apunta al cielo
    //y obtendremos un vector que apunta hacia el lateral del jugador
	if(necesitoCambiarSentido())
		cieloTierra.negate(); 
	
    //Empezamos moviéndonos a la derecha. Luego cambiaremos de sentido cuando nos bloquemos        
	movimiento.cross(dif, cieloTierra);          
           
	return movimiento;	    
}

/** Determina si el movimiento de combate es necesario cambiarlo de sentido.
 * Necesario si hay obstaculos, caidas, o de forma aleatoria cada cierto tiempo.
 * 
 * @return true si necesita cambiar de sentido
 */
private boolean	necesitoCambiarSentido(){	
		
	//Comprobamos que haya un muro precipicio o similares
	if ( (jugador.ParedDelante!=Vista.NoHay) || //Hay pared o algo
		 (jugador.RiesgoHueco)) //Hay un hueco que no podemos saltar
		return true;
	
	//Si dejaremos de ver al enemigo cambiamos de sentido
	if (!VereEnemigo())
		return true;
	
	//Coeficiente de cambio de sentido aleatorio, para que el sentido 
	//de giro sea menos predecible.		
	if ((System.currentTimeMillis()- TiempoEnCombate) > ConstCambioSentido){
		TiempoEnCombate=System.currentTimeMillis();//Reiniciamos esta variable			
		double numeroAleatorio = Math.random();			
		if(numeroAleatorio > 0.5F){ //Mitad de probabilidades de cambio				
			return true;
		}		
	}
	
	return false;		
}

/** Calcula el punto en el que va a estar el bot y si el enemigo deja de ser visible cambia de sentido
 * 
 * @return true si deja de ver al enemigo
 */
private boolean VereEnemigo() {
	//Calculamos el punto siguiente	
    Vector3f NextPosBot= new Vector3f (mibsp.getObstacleLocation(jugador.PosBot, jugador.DirMov, 60));
    //Calculamos la posicion del enmiego    
    Vector3f PosEnemy= new Vector3f(w.getEntity(jugador.Enemigo).getOrigin());
    
	return mibsp.isVisible(NextPosBot, PosEnemy);
}



/**Función que se encarga de  calcular la posición que debe tomar
 * el bot para mantener una distancia constante con su enemigo  
 * @param Enemigo
 * @param d
 * @return Vector3f que indica la dirección de movimiento
 */	
private Vector3f MantieneDistancia(Entity Enemigo,float d){
	
	Vector3f dif = new Vector3f();
	dif.sub(Enemigo.getOrigin().toVector3f(),jugador.PosBot);
	
	double distance = distancia(Enemigo);
	
	if(distance > (d+5)){ //Si tenemos que acercarnos		
		if (vista.PrevisorHuecos(jugador.PosBot, dif)) //Si vemos que hay un hueco no mantenemos distancia
			dif.set(0,0,0);		
		return dif;
	}
	else if (distance < (d-5)) { //Si tenemos que alejarnos		
		//Invertimos la direccion de movimiento
		dif.x=-dif.x;
		dif.y=-dif.y;
		if (vista.PrevisorHuecos(jugador.PosBot, dif)) //Si vemos que hay un hueco no mantenemos distancia
			dif.set(0,0,0);		
		return dif;
	}	
	else { //En otro caso no nos alejamos ni nos acercamos
		dif.set(0,0,0);		
		return dif;
	}
		
}


/** Devuelve el ángulo de disparo ideal, teniendo en cuenta el sentido de giro del bot, 
 * su distancia al enemigo y el movimiento de este.
 * 
 * @param Enemigo
 * @return Vector3f indicando la direción de vista ideal apra disparar.
 */

public Vector3f corregirApuntar(Entity Enemigo)
{
	Vector3f PosBot = new Vector3f();
	Vector3f NextPosBot;
	Vector3f PosEnemy = new Vector3f();
	Vector3f DirEnemy= new Vector3f();
	float CoeficienteBotPos=7.5F;
	float CoeficienteEnemyPosVel=1.55F;
	float CoeficienteEnemyPosDist=0;
	    	    	
   	//Corrección según el sentido y la velocidad del propio bot. 
	//"Prevemos" la posicion futura del bot
    NextPosBot=mibsp.getObstacleLocation(jugador.PosBot, jugador.DirMov, jugador.velocidad/CoeficienteBotPos); //40        	
    			
    //Ahora calculamos la futura posición del enemigo:
    //Inicializamos PosEnemy a la posicion del enemigo
    PosEnemy.set(Enemigo.getOrigin());
  
    //Si esta inicializada la posicion anterior la usamos. Si no dejaremos pos como la posicion actual del enemigo
    if (!(EnemigoPosAnterior.x==0 && EnemigoPosAnterior.y==0 && EnemigoPosAnterior.z==0)){
    	//Calculamos la direccion de movimiento del enemigo
    	DirEnemy.sub(PosEnemy,EnemigoPosAnterior); //Direccion
    	//Calculamos la distancia a la que está
		float Distancia = PosBot.distance(PosEnemy);
		//Calculamos el coeficinete dependiente de su velocidad
		CoeficienteEnemyPosVel=CalcCoeficienteEnemyPosVel(jugador.IndiceArmas());
		//Claculamos el coeficinete dependiente de la distancia
		CoeficienteEnemyPosDist=CalcCoeficienteEnemyPosDist(jugador.IndiceArmas());
		//Ahora calculamos el punto en el que esperamos que esté    		
		
    	if (DirEnemy.length()>1)//Apenas se ha movido si es menor que 1 así que despreciamos el movimiento   
    		//El ultimo parámetro de esta funcion represente la distancia a la que suponemos estará el bot cuando le alcanze el proyectil
			//Depende de la velocidad que lleve el enemigo (DirEnemy.leght) y de la distancia a la que se encuentre
    		PosEnemy.set(mibsp.getObstacleLocation(PosEnemy,DirEnemy,(DirEnemy.length()*CoeficienteEnemyPosVel) + Distancia*CoeficienteEnemyPosDist));
	
    }
    
    //Si está agachado movemos un poco la z de la posición del enemigo para disparar más abajo
    if (Enemigo.isCrouching()){
    	PosEnemy.z-=6;
    }
	
	//Si le restamos la altura a la posicion del enemigo antes de hacer la prediccion futura de la posicion del enemigo
	//hara que no funcione dicha prediccion asi que hacemos la correccion postprediccion
	if(jugador.IndiceArmas()==Inventory.ROCKET_LAUNCHER){
		if(Enemigo.isCrouching()){
			PosEnemy.z-=17;
		}
		else
			PosEnemy.z-=30;    		
	}
    
    //Ahora calculamos el vector entre las posiciones previstas del bot y del enemigo
    DirEnemy.sub(PosEnemy,NextPosBot);       
    
    return DirEnemy;
}


/** Calcula los factores de velocidad en el apuntado
 * 
 * @param Weapon
 * @return Float indicando el factor
 */
private float CalcCoeficienteEnemyPosVel(int Weapon) {
	if(Weapon==Inventory.BLASTER)
		return 0.75F;
	if(Weapon==Inventory.BFG10K)
		return 1.55F;
	if(Weapon==Inventory.CHAINGUN)
		return 1F;
	if(Weapon==Inventory.GRENADE_LAUNCHER)
		return 1.55F;
	if(Weapon==Inventory.HYPERBLASTER)
		return 1.4F;
	if(Weapon==Inventory.MACHINEGUN)
		return 0.85F;
	if(Weapon==Inventory.RAILGUN)
		return 1F;
	if(Weapon==Inventory.ROCKET_LAUNCHER)
		return 2F;
	if(Weapon==Inventory.SHOTGUN)
		return 1F;
	if(Weapon==Inventory.SUPER_SHOTGUN)
		return 1F;		
	
	System.out.println("Arma no comtemplada para Coeficiente de EnemyPos en corregir apuntar: " + Weapon);
	return 3F; //Return por defecto
}

/** Calcula los factores de distancia con el enemigo en el apuntado
 * 
 * @param Weapon
 * @return Float indicando el factor
 */
private float CalcCoeficienteEnemyPosDist(int Weapon) {
//Los factores de distancia estan bien para el supuesto de que seguiran moviendose hacia la misma dirección.
//A mayor distancia mas facilidad de esquivar nuestras balas
	if(Weapon==Inventory.BLASTER)
		return 0.3F;
	if(Weapon==Inventory.BFG10K)
		return 0.4F;
	if(Weapon==Inventory.CHAINGUN)
		return 0.025F;
	if(Weapon==Inventory.GRENADE_LAUNCHER)
		return 0.04F;
	if(Weapon==Inventory.HYPERBLASTER)
		return 0.24F;
	if(Weapon==Inventory.MACHINEGUN)
		return 0.025F;
	if(Weapon==Inventory.RAILGUN)
		return 0.001F;
	if(Weapon==Inventory.ROCKET_LAUNCHER)
		return 2F;
	if(Weapon==Inventory.SHOTGUN)
		return 0.04F;
	if(Weapon==Inventory.SUPER_SHOTGUN)
		return 0.04F;		
	
	System.out.println("Arma no comtemplada para Coeficiente de EnemyPos en corregir apuntar: " + Weapon);
	return 1F; //Return por defecto
}

}
