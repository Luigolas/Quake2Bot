//import java.util.ArrayList;
import java.util.Date;
import soc.qase.bot.ObserverBot;
import soc.qase.file.bsp.BSPParser;
import soc.qase.state.Entity;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;
import soc.qase.state.*;
import java.util.Random;


//Cualquier bot debe extender a la clase ObserverBot, para hacer uso de sus funcionalidades
public final class MiBotseMueve extends ObserverBot
{
	//**********************************************************
	//++++++++++++++CLASES LOCALES DEL BOT++++++++++++++++++++++
	//**********************************************************
	private World world = null;
	public BSPParser mibsp = null;
	private Vista vista= null;
	private Ubicacion ubicacion = null;
	private Estado estado = null;
	private Distancia distancia;
	private Decision decidir;
		
	
	//**********************************************************
	//+++++++++++++VARIABLES LOCALES DEL BOT++++++++++++++++++++
	//**********************************************************

	//*********************
	//++++++++VISTA++++++++ 
	//*********************
	// Indica lo que hay delante. Para usar con las constantes de la clase Vista	 
	/** Indica si hay una pared delante o no (para usar con las constantes de la clase Vista): 
	 * HayPared, NoHay, HayHueco, HayObstaculo
	 * */
	public int ParedDelante; 
	
	/** Indica el tipo de hueco que hay delante (para usar con las constantes de la clase Vista):
	 *  NoHay, HuecoSaltable, HuecoNoSaltable 
	 *  */
	public int HuecoSueloDelante; 
		
	/** Indica que tipo de caida hay delante (para usar con las constantes de la clase Vista):
	 *  NoHay, Precipicio, CaidaRecuperable, CaidaNoRecuperable,
	 *  AguaRecuperable, AguaNoRecuperable, Lava.
	 */
	public int CaidaDelante; 
	
	/**Indica si en esa direccion hay una escalera y el tipo (para usar con las constantes de la clase Vista):
	 *  EscaleraSubida, EscaleraBajada, NoHay 
	 *  No implementado porque sólo resultaba útil para el mapa, que finalmente no se ha realizado
	 *  */
//	public int EscaleraDelante;
	
	/** Indica que existe un cierto riesgo de que existe un hueco más adelante. Mira más lejos de lo normal
	 * para anticiparse con tiempo cuando el bot va muy rápido. De tipo boolean.
	 */
	public boolean RiesgoHueco;
	
	
	//*********************
	//++++++++UBICACIÓN++++ 
	//*********************
	/**Indica donde está el bot (en el suelo, en el agua...). Para usar con las constantes de la clase Estoy:
	 * EnAire, BajoAgua, SobreAgua, EnSuelo, SubiendoAscensor, BajandoAscensor	
	 */
	public int BotUbicacion; 
	
	/** Indica si el bot está atascado o no y su nivel de atascamiento (en principio hay 2) */
	public int Atascado=Ubicacion.NoAtascado;	
	
	
	//*********************
	//+++++++ESTADO++++++++ 
	//*********************
	/** Booleano que indica si ha cogido algún arma o sólo tiene la Blaster */
	public boolean TengoArma=false;	
	/** Indica que el bot tiene poca vida */
	public boolean PocaVida=false; 
	/** Indica que el bot tiene poco escudo */
	public boolean PocoEscudo=true;
	/** Indica que la suma de escudo y vida del bot es baja */
	public boolean EstadoCritico=false;
	/** Indica que el bot tiene muy poca municion para el arma equipada */
	public boolean PocaMunicion=true;
	/**	True si hay algún enemigo visible */
	public boolean Combatiendo=false;
	/** Indica el índice del enemigo al que actualmente queremos atacar.
	 * Pensadas para ser usadas con "world.getEntity(Enemigo)" y "Entity.getNumber()" */
	public int Enemigo=-1; 
	
	//*********************
	//+++++++ESTADO-AGUA+++ 
	//*********************
    /**Máximo tiempo permitido en agua*/
    static final long TiempoEnAguaMax = 20000; //20 segundos	
    /**Tiempo máximo de exploracion en agua*/
	static final long ConstTiempoExploracionAcuatica = 10000;
    /** Tiempo minimo que debemos estar fuera del agua una vez salgamos */
    public static final long TiempoFueraDeAguaMin = 10000; //10 segundos
	/**Tiempo que llevamos en el agua*/
    public long TiempoEnAgua = 0;
    /**Tiempo que llevamos fuera del agua */
    public long TiempoFueraDeAgua = 0;
    /** Activada si tenemos que buscar tuberias acuaticas */
    public boolean BuscarTuberias = true;
    /**Indica si la exploracion acuatica está activada o no*/
	public boolean ExploracioAcuaticaActiva = false;
	
	//*********************
	//+++++MOVIMIENTO++++++ 
	//*********************	
	public Vector3f DirMov = new Vector3f(-1,0,0);
	public float velocidad = 0;
	public int postura = PlayerMove.POSTURE_NORMAL;
	public Vector3f PosBot = new Vector3f(-1,0,0);
	public Vector3f PosBotAnt = new Vector3f(-1,0,0);
	public Vector3f dirVista = new Vector3f(-1,0,0);
	
	
	
	//*********************
	//+++++DEPURACION++++++ 
	//*********************	
	private Depuracion uDep;
	public boolean depuracion=true;
	public int ArmaDep=1;
	
	
//La lista negra finalmente no ha sido utilizada. Sirve para guardar aquellos objetos
//a los que no podemos llegar.
//	public ArrayList<Vector3f> ListaNegra;
//	private static final int VaciaListaNegra= 180000; //Vaciamos la lista negra cada 3 minutos
//	private long TempListaNegra=0; //Temporizador para saber cuanto hace que borramos la lista negra
	private long TiempoImpresion=System.currentTimeMillis(); //para depuración
		
	

/*-------------------------------------------------------------------*/
/**	Constructor que permite especificar el nombre y aspecto del bot
 *	@param botName Nombre del bot durante el juego
 *	@param botSkin Aspecto del bot */
/*-------------------------------------------------------------------*/
	public MiBotseMueve(String botName, String botSkin)
	{
		super((botName == null ? "MiBotseMueve" : botName), botSkin);
		initBot();		
		
	}

/*-------------------------------------------------------------------*/
/**	Constructor que permite ade más de especificar el nombre y aspecto 
 *	del bot, indicar si éste analizará manualmente su inventario.
 *	@param botName Nombre del bot durante el juego
 *	@param botSkin Aspecto del bot
 *	@param trackInv Si true, El agente analizará manualmente su inventario */
/*-------------------------------------------------------------------*/
	public MiBotseMueve(String botName, String botSkin, boolean trackInv)
	{
		super((botName == null ? "MiBotseMueve" : botName), botSkin, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor que permite además de especificar el nombre y aspecto 
 *	del bot, indicar si éste analizará manualmente su inventario y
 *  si hará uso de un hilo en modo seguro.
 *	@param botName Nombre del bot durante el juego
 *	@param botSkin Aspecto del bot
 *	@param highThreadSafety Si true, permite el modo de hilo seguro
 *	@param trackInv Si true, El agente analizará manualmente su inventario */
/*-------------------------------------------------------------------*/
	public MiBotseMueve(String botName, String botSkin, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiBotseMueve" : botName), botSkin, highThreadSafety, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor que permite además de especificar el nombre, aspecto 
 *	del bot y la clave del servidor, indicar si éste analizará manualmente 
 *  su inventario y si hará uso de un hilo en modo seguro.
 *	@param botName Nombre del bot durante el juego
 *	@param botSkin Aspecto del bot
 *	@param password clave del servidor
 *	@param highThreadSafety Si true, permite el modo de hilo seguro
 *	@param trackInv Si true, El agente analizará manualmente su inventario */
/*-------------------------------------------------------------------*/
	public MiBotseMueve(String botName, String botSkin, String password, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiBotseMueve" : botName), botSkin, password, highThreadSafety, trackInv);
		initBot();
	}

/*-------------------------------------------------------------------*/
/**	Constructor que permite además de especificar el nombre, aspecto 
 *	del bot, ratio de comunicación, tipo de mensajes y la clave del servidor,
 *  indicar si éste analizará manualmente 
 *  su inventario y si hará uso de un hilo en modo seguro.
 *  @param botName Nombre del bot durante el juego
 *	@param botSkin Aspecto del bot
 *	@param recvRate Ratio de comunicación 
 *	@param msgLevel Tipo de mensajes
 *	@param fov Campo de visión del agente
 *	@param hand Indica la mano en la que se lleva el arma
 *	@param password Clave del servidor
 *	@param highThreadSafety Si true, permite el modo de hilo seguro
 *	@param trackInv Si true, El agente analizará manualmente su inventario */
/*-------------------------------------------------------------------*/
	public MiBotseMueve(String botName, String botSkin, int recvRate, int msgLevel, int fov, int hand, String password, boolean highThreadSafety, boolean trackInv)
	{
		super((botName == null ? "MiBotseMueve" : botName), botSkin, recvRate, msgLevel, fov, hand, password, highThreadSafety, trackInv);
		initBot();
	}

	//Inicialización del bot
	private void initBot()
	{				
		//Autorefresco del inventario
		this.setAutoInventoryRefresh(true);		
	}

/*-------------------------------------------------------------------*/
/**	Rutina central del agente para especificar su comportamiento
 *	@param w Objeto de tipo World que contiene el estado actual del juego */
/*-------------------------------------------------------------------*/
	public void runAI(World w)	
	{
		world=w;
		//La primera vez se ejecuta esto
		if (mibsp==null) {
			System.out.println("Iniciandome");
			
			//INICIALIZACIóN			
			Random r;
			r=new Random();
			r.setSeed(new Date().getTime());			
						
			mibsp = this.getBSPParser();
			distancia= new Distancia(this, world);
			vista= new Vista(this, distancia);
	        //exploracion = new Explorar(this,world,distancia,vista);	        
	        ubicacion= new Ubicacion(this, world, distancia);
	        estado= new Estado(this, world, mibsp); 
	        decidir= new Decision(this, world, mibsp, distancia,vista);
	//        ListaNegra = new ArrayList<Vector3f>();
	//        TempListaNegra = System.currentTimeMillis();//La inicializamos	     
	        		
			//this.sendConsoleCommand("god");	//Bot inmortal		        
			
	        if (depuracion){
	        	uDep = new Depuracion(this, this.getPlayerInfo().getName());
	        	uDep.recolectorEventos.setSize(400,100);
	        	uDep.recolectorEventos.setAlwaysOnTop(true);
	        	uDep.recolectorEventos.setVisible(true);
	        	uDep.asignarTeclas();	        	
	        }
		}
				
		Actualizar(w);	
		
		//movimientos de depuracion
		if (depuracion){
			//En este caso llamrá al código de depuración, en el que podemos añadir lo que queramos para hacer pruebas
			Depuracion();
			return;			
		}
		
		decidir.accion();
		this.setBotMovement(DirMov, dirVista, velocidad, postura);		
		setAction(Action.ATTACK, Combatiendo);
		
		//Lo resucitamos si ha muerto
		if (!this.isBotAlive() || (this.getHealth() < 1)){			
			this.respawn();
			setAction(Action.ATTACK, Combatiendo);
		}
		
		//this.sendConsoleCommand("give all"); //Todas las armas  y objetos de forma infinita
	}
	
	
	/** Metodo que sólo será llamado si tenemos la depuración activa.
	 *  En esta función puedes poner el codigo que quieras para realizar pruebas
	 */
	private void Depuracion() {		
		//Cambiamos el arma según ArmaDep, cambiada en la clase Depuracion
		this.changeWeaponByKeyboardIndex(ArmaDep);		
		//Establecemos el movimiento,, cambiada en la clase Depuracion
		setBotMovement(DirMov, DirMov, velocidad, postura);
		
		//Aquí el código que desees probar:		
		
		//Este if para que sólo se muestre por pantalla los datos cada 2 segundos
		if((System.currentTimeMillis()-TiempoImpresion)>2000) {
			TiempoImpresion=System.currentTimeMillis();
			
			
			
			return;
		}
	
	}

	/** Actualiza todo el estado del bot: Estoy, Vista, Estado..
	 * Tambien llevará el control de la lista negra, actuliza la posición del bot y
	 * actualiza el world 
	 * @param w
	 */
	private void Actualizar(World w) {
		//Información del juego almacenada en una variable miembro		
		world = w;
		
		//Obtiene información del bot
		PosBotAnt.set(PosBot);
		PosBot.set(this.getPosition().toVector3f());
		
		//Controlamos el vaciado de la ListaNegra
		//ControlListaNegra();
		
		//Actualizamos la vista, el estado y el estoy
	try{
		vista.actualizar();
		ubicacion.actualizar();
		estado.actualizar();
	}
	catch (Exception ex){
		System.out.println(ex);
	}
	
	}
	
/*
	private void ControlListaNegra() {
	//  Controla si hace falta vaciar la ListaNegra
		if ((System.currentTimeMillis()-TempListaNegra)>VaciaListaNegra){
			//Vaciamos la lista
		//	ListaNegra.clear();
			//Reiniciamos el contador
			TempListaNegra=System.currentTimeMillis();
			System.out.println("Lista Negra vaciada");
		}	
	}
*/

	/**
	 * Función que nos devuelve el índice del enemigo más cercano.
	 * En caso de no obtener enemigo nos devuelve un -1
	 * @return
	 */
	public int EnemigoMasCercano() {
		Entity aux=this.getNearestOpponent();		
		if(aux!= null)
			if(aux.getName().contains("SALPICA"))
				return -1;
			else
				return aux.getNumber();
		else 
			return -1;
	}
	
		
	/**
	 * Función que devuelve verdadero si está en un ascensor
	 * @return
	 */
	public boolean EnAscensor() {	
		return ((this.isOnLift()!=null));
	}

	public int IndiceArmas() {
		return this.getWeaponIndex();
	}	
	
	public void CambiarArma(int arma){
		changeWeaponByInventoryIndex(arma);
	}
	
	public long TiempoParaAhogarse(){
		return this.timeUntilDrowning();
	}
}