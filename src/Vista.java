import soc.qase.tools.vecmath.Vector3f;

/** Proporciona información sobre todos los elementos que ve el bot
 * @author Luis Mª González Medina - Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 */

public class Vista {
	
	//**********************************************************
	//+++++++++++++++++++CONSTANTES+++++++++++++++++++++++++++++
	//**********************************************************	
	/**Indica la distancia hasta la que miramos */
	private final static float ConstDistanciaVista = 50;
	/**Radio de la esfera lanzada para detectar Paredes */
	private static final float RadioEsferaPared = 5;
	/**Profundidad hasta la que lanzaremos el Rayo para detectar si hay huecos */
	private static final float ConstProfundidadHueco = 60;
	/**Profundidad a la que consideraremos que no hay hueco en el suelo */
	private static final float ConstDistSuelo = 45; 
	/**Distancia que consideramos que el bot puede saltar */
	private float ConstDistHuecoSaltable = 160; 
	/** Indica cuanta distancia extra miramos en las comprobaciones para saber si hay un hueco en la pared */
	private static final float ConstProfHuecoPared = 25;
	/** Constante que indica la distancia máxima que se puede considerar una caida No recuperable y que no quita vida */
	private static final float ConstDistCaidaNoRecuperable = 245;
	/** Constante que indica la distancia máxima que se puede considerar una caida Recuperable*/
	public static final float ConstDistCaidaRecuperable = 80;
	/** Constante que indica la distancia máxima que se puede considerar Caida en el agua recuperable*/
	private static final float ConstDistAguaRecuperable = 44;
	/** Constante que indica la altura a la que se considera que hay un obstaculo que choca con la cabeza*/
	public static final float ConstDistCabeza = 35;
	
	//************
	//ParedDelante
	/** Constante que indica que hay una pared delante */
	public static final int HayPared = 1;
	/**Indica que hay una pared con un hueco delante */
	public static final int HayHueco = 2;
	/** Inidica que hay un obstaculo sobre el que podemos saltar delante */
	public static final int HayObstaculo = 3;
	
	
	//************	
	//HuecoSueloDelante
	/** Indica si un hueco lo podemos saltar sin caernos */
	public static final int HuecoSaltable = 1;	
	/** Indica que el hueco NO podemos saltarlo sin caernos */
	public static final int HuecoNoSaltable = 2;

	//************
	//CaidaDelante
	/** Constante que indica que hay un precipicio delante (tirarnos nos quitará vida)*/
	public static final int Precipicio = 1;
	/** Constante que indica que hay una caida pequeña, de la que podremos retornar saltando*/
	public static final int CaidaRecuperable = 2;
	/** Constante que indica que hay una caida de la que NO podremos volver saltando, 
	 * pero no nos quitará vida */
	public static final int CaidaNoRecuperable = 3;
	/** Constante que indica que hay agua delante y si nos tiramos podremos volver al suelo por el mismo
	 * sitio en que nos tiramos */
	public static final int AguaRecuperable = 4;
	/** Constante que indica que hay agua delante y que si nos tiramos no podremos volver (saltando) al mismo sitio
	 * donde estabamos antes de tirarnos */
	public static final int AguaNoRecuperable = 5;
	/** Constante que indica que hay lava en la caida */
	public static final int Lava = 6;

	//************
	//EscaleraDelante
	/** Constante que indica que hay una escalera delante y es de subida */
	public static final int EscaleraSubida = 1;
	/** Constante que indica que hay una escalera delante y es de bajada */
	public static final int EscaleraBajada = 2;
	
	/**Constante que indica que No Hay */
	public static final int NoHay = 0;
	
	
			
	//**********************************************************
	//++++++++++++++++ATRIBUTOS DE LA CLASE+++++++++++++++++++++
	//**********************************************************  
    private Distancia distancia;  
    private MiBotseMueve jugador;   
    
	//**********************************************************
	//++++++++++++++++MÉTODOS DE LA CLASE+++++++++++++++++++++++
	//**********************************************************
    
    /** Constructor de la clase
     * 
     * @param j
     * @param dist
     */
    public Vista(MiBotseMueve j, Distancia dist)
    {        
        distancia = dist;        
        jugador=j;        
    }  
    
    
    /** Actualiza los parámetros relacionados con la "vista" del Bot
     * 
     */
    public void actualizar(){        
            try{
            	CalculaDistanciaSaltable();
            	jugador.ParedDelante = TipoParedDelante(jugador.PosBot,jugador.DirMov);
            	jugador.HuecoSueloDelante = TipoHuecoSueloDelante(jugador.PosBot,jugador.DirMov);
            	jugador.CaidaDelante = TipoCaidaDelante(jugador.PosBot,jugador.DirMov);
            	jugador.RiesgoHueco=PrevisorHuecos(jugador.PosBot,jugador.DirMov);
          //  	jugador.EscaleraDelante = TipoEscaleraDelante();
            }
            catch (Exception ex){
                System.out.println("SITUACIÓN ANÓMALA: En Vista");
            }        
    }

    /**Calcula el valor de ConstDistHsuecoSaltable en función de la velocidad real del bot */
	private void CalculaDistanciaSaltable() {
		if (jugador.PosBotAnt.distance(jugador.PosBot)>24)
			ConstDistHuecoSaltable=190;
		else
			ConstDistHuecoSaltable=90;
		
	}

	/** Detecta si hay una pared delante (a distancia Vista.ConstDistanciaVista)
	 * @param dir 
	 * @param Pos 
     * 
     * @return HayPared en caso de que haya una pared, HayObstaculo en caso de ser un obtaculo saltable, HayHueco en caso de 
     * ser un hueco por el que podamos entrar agachados y NoHay en caso de que no haya nada
     */
	public int TipoParedDelante(Vector3f Pos, Vector3f dir) {	
		//SI no hay Pared devolverá ConstDistanciaVista. Si Hay Pared, devuelve un valor menor
		Vector3f Posaux = new Vector3f(Pos);
		float DistCabeza, DistPiernas, DistCintura;
		Posaux.z+=ConstDistCabeza;
		DistCabeza= distancia.DistanciaEsferaSolido(Posaux, dir, RadioEsferaPared, ConstDistanciaVista);		
		Posaux.z-=30;
		DistPiernas= distancia.DistanciaEsferaSolido(Posaux, dir, RadioEsferaPared, ConstDistanciaVista + ConstProfHuecoPared );
		
		if (DistCabeza < (ConstDistanciaVista -1)){
			//Hay algo en la cabeza: Puede ser Pared o HayHueco
			if (DistPiernas < (ConstDistanciaVista -1))
				//En este caso hay algo en los Pies: es una pared
				return HayPared;
			else {
				//Hay un hueco: Nos aseguramos de que cabemos realmente
				return (DistPiernas>(ConstDistanciaVista+ConstProfHuecoPared -2)) ? HayHueco: HayPared;				
			}			
		}
		else{ //No Hay nada en la cabeza
			Posaux.z+=20;
			DistCintura= distancia.DistanciaEsferaSolido(Posaux, dir, RadioEsferaPared, ConstDistanciaVista);	
			if (DistCintura < (ConstDistanciaVista -1))
				//Hay algo en la cintura.
				if (DistPiernas < (ConstDistanciaVista -1))
					//Si Hay algo en las piernas
					return HayPared;
				else 
					return HayHueco;				
			else //No hay nada en la cintura
				if (DistPiernas < (ConstDistanciaVista -1))
					//Si Hay algo en las piernas
					return HayObstaculo;
				else 
					return NoHay;
		}
	}

	/** Devuelve si Hay un hueco saltable, si hay un hueco no saltable, o si no hay hueco
	 *  Ante rampas empinadas y escaleras muy pegadas puede detectarlo como Hueco No Saltable
	 *  En general funciona bien ...
	 * @param dir 
	 * @param Pos 
	 * @return
	 */
	public int TipoHuecoSueloDelante(Vector3f Pos, Vector3f dir) {		
		float dist;
		//Si no hay una pared delante		
		if (jugador.ParedDelante == NoHay){
			dist =distancia.DistanciaHaciaAbajo(Pos, dir, ConstDistanciaVista, ConstProfundidadHueco);
			//Hay un hueco. Hay que determinar si es saltable o no saltable
			if (dist > ConstDistSuelo){
				//trazamos otro rayo en la misma direccion pero un poco más adelante para ver si el Hueco todavia existe o 
				//ya hay suelo. Tambien puede darse el caso de que haya una pared
				dist =distancia.DistanciaHaciaAbajo(Pos, dir, ConstDistanciaVista + ConstDistHuecoSaltable, ConstProfundidadHueco);
				//En el caso de que haya una pared, el rayo se lanzará mas cerca aun del bot, por lo que no hay problema
				if (dist>ConstDistSuelo)
					//En este caso sigue habiendo un hueco
					return HuecoNoSaltable;
				else //Solo en este caso se puede considerar un salto seguro
					return HuecoSaltable;								
			}
			//En este caso no hay hueco
			else
				return NoHay;
		}
		else {
			//En este caso no podemos lanzar el rayo sin más. Lo qu hacemos es lanzar el rayo un poco más cerca del bot.
			//Aun así sabemos que será un HuecoNoSaltable o un NoHay
			dist =distancia.DistanciaHaciaAbajo(Pos, dir, ConstDistanciaVista - ConstDistanciaVista/10, ConstProfundidadHueco);
			if (dist>ConstDistSuelo)
				return HuecoNoSaltable;
			else
				return NoHay;			
		}		
	}

	/** Clasifica el tipo de caida que hay delante
	 *  NoHay, Precipicio, CaidaRecuperable, CaidaNoRecuperable,
	 *  AguaRecuperable, AguaNoRecuperable, Lava.
	 * @param dir 
	 * @param Pos 
	 * @return
	 */
	public int TipoCaidaDelante(Vector3f Pos, Vector3f dir) {
		//En este caso no hay ningún hueco así que nos ahorramos el comprobarlo
		if (jugador.HuecoSueloDelante==NoHay)
			return NoHay;
		
		float Dist=0;
		float DistLava=0;
		float DistAgua=0;

		Dist= distancia.DistanciaHaciaAbajo(Pos, dir, ConstDistanciaVista, 300);
		DistLava = distancia.DistanciaHaciaAbajoLava(Pos, dir, ConstDistanciaVista, 2000);
		DistAgua = distancia.DistanciaHaciaAbajoAgua(Pos, dir, ConstDistanciaVista, 2000);
		
		if (Dist>ConstDistCaidaNoRecuperable){
			//Estamos ante Precipicio, AguaNoRecuperable o Lava
			if (DistLava<Dist)
				return Lava;
			if (DistAgua<Dist)
				return AguaNoRecuperable;
			else 
				return Precipicio;
		}
		else			
			if (Dist>ConstDistCaidaRecuperable){
				//Estamos en el caso de CaidaNoRecuperable, AguaNoRecuperable o Lava
				if (DistLava<Dist)
					return Lava;
				if (DistAgua<Dist)
					return AguaNoRecuperable;
				else 
					return CaidaNoRecuperable;				
			}
			else {
				//Estamos en el caso de CaidaRecuperable, AguaNoRecuperable AguaRecuperable o Lava
				if (DistAgua<Dist){
					//AguaRecuperable o AguaNoRecuperable
					if (DistAgua>ConstDistAguaRecuperable)
						return AguaNoRecuperable;
					else
						return AguaRecuperable;					
				}
				else
					if (DistLava<Dist)
						return Lava;
					else 
						//Estamos en el caso de que no hay caida o es una caida recuperable
						if (Dist<ConstDistSuelo)
							return NoHay;
						else
							return CaidaRecuperable;
			}
	}
	
	/** Detecta si hay huecos antes de lo normal, mirando un 50% más allá de la vista normal.
	 *  Devuelve true si cree que será un hueco no saltable.
	 * @param pos
	 * @param dir
	 * @return true si existe riesgo de hueco. False en otro caso
	 */
	public boolean PrevisorHuecos(Vector3f pos, Vector3f dir) {
		if (distancia.DistanciaHaciaAbajo(pos, dir, ConstDistanciaVista*1.5F, 100F)>ConstDistCaidaRecuperable)
			return (distancia.DistanciaHaciaAbajo(pos, dir, ConstDistanciaVista*1.5F + ConstDistHuecoSaltable, 100F)>ConstDistCaidaRecuperable)?
					true: false;
		else
			return false;
				
	}
	
	

	/**
	 * Este falta por hacerse... Pero por ahora no es escencial ya que no creamos el mapa... 
	 * @return
	 */
	public int TipoEscaleraDelante() {
		// TODO Auto-generated method stub
		return 0;
	}
    
    
    
  
}
