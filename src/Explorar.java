
import soc.qase.tools.Utils;
import soc.qase.tools.vecmath.Vector2f;
import soc.qase.tools.vecmath.Vector3f;
import java.util.Random;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;


/**
 *  Contiene todos los métodos para generar el comportamiento de exploración.
 *  @author Luis Mª González Medina - Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 */
public class Explorar 
{
    /** Distancia del bot a un objeto al frente y un poco a la derecha */ 
    private float distADer;
    /** Distancia del bot a un objeto al frente y un poco a la izquierda */
    private float distAIzq;
    /** Peso que favorece el giro a la derecha */    
    private float pesoDer = 1;
    /** Peso que favorece el giro a la izquierda */    
    private float pesoIzq = 1;
    /** Penalización del sentido de giro no tomado */
    private float decrementoPeso = 0.1733F;
    /** Para el cálculo de distancias*/
    private Distancia distancia;
            
    
    // ---> Constantes internas <---
    /** Velocidad óptima para girar ante un muro */
    private static final int velAnteMuro = 100;
    /** Mínimo giro, en grados, que puede realizar el bot */
    private int giroMinimo = 12;
    
    //Variables propias
    private MiBotseMueve jugador;
    
 

    private static final long ConstTimeGiroAleatorio = 3000; //3 segundos
	private static final float ConstGrados = 25;
	private float dirGradosXY;
	private long TimeGiroAleatorio;

             
 
    /**Constructor: Inicializa el jugador con el que se explora
     * 
     * @param J
     * @param world
     * @param dist
     */
    public Explorar( MiBotseMueve J, World world, Distancia dist)    
    {
    	jugador=J;
    	distancia = dist;    	
    }
    
    /** Genera el comportamiento de exploración en el bot. Consiste en un 
     * conjunto de movimientos que se relizan si se dan ciertas condiciones.
     * Único método público.
     */
    public void explorar()
    {   
        //Calculamos los grados en XY de la direccion de movimiento del bot
		dirGradosXY = Utils.calcAngles(jugador.DirMov)[0];
		
		//Controlamos la situación de estar en el agua
		if(jugador.BotUbicacion == Ubicacion.BajoAgua ||
		   jugador.BotUbicacion == Ubicacion.SobreAgua){
						
			if ((System.currentTimeMillis() - jugador.TiempoEnAgua)< MiBotseMueve.ConstTiempoExploracionAcuatica)					 
				exploracionAcuatica();
			else //Es momento de salir del agua
				if (!EstoyBajoTecho())
					salirDeAgua(); //Si puedo salgo cuanto antes.
				else { //En este caso no podemos salir directamente, nos movemos hasta que podamos.
					//Colocamos DirMov.z por si esta mirando hacia arriba o abajo
					jugador.DirMov.z=0;
					explorarAcuatico();
				}
			return;
		}
        
        //Establecemos el ángulo Z en 0 para que se mueva y mire hacia el frente
        jugador.DirMov.z=0;
        
        //Si estamos en el aire no hacemos nada, hasta tocar suelo, o agua, para no desviarnos en los saltos...
        if (jugador.BotUbicacion==Ubicacion.EnAire)
        	return;
        
        //Preajustamos la velocidad por si hay huecos edlante del bot
        PreajusteVel();

        ///////////////////////////////////////////////////////////
        /////   ASCENSOR									  /////
        ///////////////////////////////////////////////////////////
        if (jugador.BotUbicacion==Ubicacion.SubiendoAscensor){
        	ControlAscensor();
        	return;
        }

        ///////////////////////////////////////////////////////////
        /////      Comprobamos si es neesario salir de atasco /////
        ///////////////////////////////////////////////////////////
        if (jugador.Atascado != Ubicacion.NoAtascado) {
        	salirDeAtasco();
        	return;
        }
            
        
        ///////////////////////////////////////////////////////////
        /////Saltos o agacharse para evitar huecos u ostáculos/////
        ///////////////////////////////////////////////////////////        
        if (EsquivarSiNecesario()) {
        	return;
        }
        
           
        ///////////////////////////////////////////////////////////
        /////        Giros para caminar correctamente         /////
        ///////////////////////////////////////////////////////////
        //Si se da una de las muchas condiciones que hacen que el bot 
        //deba girar, giramos
        if(rectificarRuta())
        {   
            giroSuave();
            return;
        }
        else
        {
            //Reinicializamos los pesos que deciden el sentido de giro
            pesoDer = pesoIzq = 1;
        }
                
        //En este caso, no se produce ningúna situación especial a tratar,
        //Ejecutamos giro aleatorio
        GiroAleatorio();
    }

/** Controla la situación en la que el bot se encuentra explorando en el agua
 * 
 */
    private void exploracionAcuatica() {

    	//Si nos estamos ahogando salimos a la superficie.
    	if ((jugador.TiempoParaAhogarse()>0) && 
    		(jugador.TiempoParaAhogarse()<2000)){ //Si quedan dos segundos para ahogarse que salga del agua
    		SalirASuperficie();
    		return;
    	}
    	    	
    	//Nos colocamos en un punto intermedio del agua
    	ColocacionAcuatica();    	
    	
    	if (jugador.BuscarTuberias && DetectaTuberias()){
    		//Comprobamos que no estamos bajo techo, en cuyo caso lo desactivamos para la vez siguiente
    		jugador.BuscarTuberias=false;
    		//Manipulo el timer para que en 2 seg quiera salir del agua
    		jugador.TiempoEnAgua=System.currentTimeMillis()-MiBotseMueve.ConstTiempoExploracionAcuatica + 2000;
    	}
    	else    	
    		explorarAcuatico();
    	
    	salirDeAtasco(); //si se queda atascado que intente salir del atasco
	}

    /** Lanza rayos desde el bot a una distancia cercana para detectar posibles tuberias.
     * Si las encuentra, devuelve el punto mas cercano que se considera una posible tuberia...
     * Si no devuelve null
     * @return
     */
    private boolean DetectaTuberias() {		
    	Vector3f dir= new Vector3f();
    	Vector3f posSuperficie= new Vector3f();
    	float dist=3000;
    	boolean aDevolver=false;
    	
    	//Calculamos el punto de la superficie del agua
    	posSuperficie.set(distancia.PuntoEsferaSolido(null,new Vector3f(0,0,0.99999F),2000));			
		posSuperficie.set(distancia.PuntoRayoAgua(posSuperficie, new Vector3f(0,0,-0.99999F), 2100));
		
		//En este caso no se ha calculado bien el punto de la superficie del agua
		if(jugador.PosBot.z>posSuperficie.z) 
			return false;
    	
		//miramos en las 8 direcciones
		for(int x=-1; x<2; x+=1)
			for(int y=-1; y<2; y+=1)
				if ((x==0) && (y==0)) continue; //El caso 0 0 no interesa 
				else {			
					dir.x=(float)x;
					dir.y=(float)y;
					//Hacemos esta resta porque en ocasiones se observan comportamientos "extraños"
					//al lanzar rayos en direcciones como 0 1, pero se comporta de forma correcta si su valor es
					//por ejemplo 0 0.999 ... (Razón desconocida...)
					dir.x-=0.01F; 
					dir.y-=0.01F;
					
					//Distancia a ras del agua				
					float DistSuperficie=distancia.DistanciaEsferaSolido(posSuperficie, dir,5 ,500);
					
					if (DistSuperficie>499) //En este caso no interesa
						continue;
					
					//Distancia desde la posicion del bot	
					float DistBot=distancia.DistanciaEsferaSolido(null, dir , 8 ,500);

					//Comprobamos si es candidata, es decir, existe una tuberia
					if ((DistBot - DistSuperficie) > 60){
						
						//Calculamos el punto concreto, para ver si está bajo techo realmente o no
						Vector3f PuntoReferencia = new Vector3f(jugador.mibsp.getObstacleLocation(jugador.PosBot, dir, DistBot-10));
						Vector3f Punto = new Vector3f();
						
						//Calculamos si está bajo techo
						Punto.set(distancia.PuntoEsferaSolido(PuntoReferencia,new Vector3f(0,0,0.99999F),1000));
						float distaux=distancia.DistanciaRayoAgua(Punto, new Vector3f(0,0,-0.99999F), 1000);
											
						if (distaux>999){
							//En este caso está bajo techo
							if(DistBot<dist){	
								//Es más interesante (cercana) que las candidatas que teníamos antes
								dist=DistBot;
								jugador.DirMov.x=dir.x;
								jugador.DirMov.y=dir.y;
								aDevolver=true;
							}
						}						
					}					
				}   	
		return aDevolver;
	}

	/**Mantiene al bot siempre al mismo nivel en el agua 
	 */
    private void ColocacionAcuatica() {
		if(!EstoyBajoTecho()){
			Vector3f pos;		

			pos=distancia.PuntoEsferaSolido(jugador.PosBot, new Vector3f(0,0,0.99999F), 1000);
			//Hallamos primero el punto del agua, esto es, conservando las cordenadas x e y del jugador
			//calcular la coordenada z en la que el jugador toca el agua.
			pos.set(distancia.PuntoRayoAgua(pos,new Vector3f(0,0,-0.999F),1000));
			
			float dist =distancia.DistanciaRayoSolido(pos, new Vector3f(0,0,-0.999F), 1000);

			//esta es la distancia ideal estimada
			dist/=2;
			
			//Distnacia del bot
			float distBot=distancia.DistanciaRayoSolido(null, new Vector3f(0,0,-0.999F), 1000);
			
			if(distBot>dist)
				jugador.postura=PlayerMove.POSTURE_CROUCH;
			else
				jugador.postura=PlayerMove.POSTURE_JUMP;			
		}	
		else{ //En este caso estoy bajo techo: Nos mantenemos a una distancia fija del techo: 50
			float dist =distancia.DistanciaRayoSolido(null, new Vector3f(0,0,0.999F), 1000);
			if (dist<70)
				jugador.postura=PlayerMove.POSTURE_CROUCH;
			else
				jugador.postura=PlayerMove.POSTURE_JUMP;				
		}
	}

	/** Detrmina si puede salir a la superficie o no (EstoyBajoTecho). Si es el caso, sale a la superficie (salta).
	 * En otro caso se pone a explorar (explorarAcuatico).
     */
	private void SalirASuperficie() {
		if (EstoyBajoTecho())
			explorarAcuatico(); //Seguimos explorando con la esperanza de encontrar una salida
		else{
			jugador.postura=PlayerMove.POSTURE_JUMP;
		}		
	}

	/** Evita las paredes en el agua.
	 * 
	 */
	private void explorarAcuatico() {
		jugador.velocidad=300;
		if(rectificarRuta())
	    {   
			giroSuave();
			return;
	    }
	    else
	    {
	    	//Reinicializamos los pesos que deciden el sentido de giro
	        pesoDer = pesoIzq = 1;
	    }	
	}
	
	/** Indica si el bot se encuentra en el agua y bajo techo, de forma que no puede salir 
	 * a la superficie del agua y respirar.
	 * 
	 */
	private boolean EstoyBajoTecho() {		
		Vector3f Punto= distancia.PuntoEsferaSolido(null,new Vector3f(0,0,0.99999F),1000);			
		return (distancia.DistanciaRayoAgua(Punto, new Vector3f(0,0,-0.99999F), 1000)<1000)? false: true;		
	}
	
	/**Decide un giro aleatoriamente.
	 * 
	 */
	private void GiroAleatorio() {
    	if (TimeGiroAleatorio<1)
    		TimeGiroAleatorio=System.currentTimeMillis();
    	else if ((System.currentTimeMillis()-TimeGiroAleatorio)>ConstTimeGiroAleatorio){
    		TimeGiroAleatorio=0;
    		if (Math.random()>0.5)
    			girarDerecha(giroMinimo + new Random().nextInt(20));
    		else
    			girarIzquierda(giroMinimo + new Random().nextInt(20));
    	}    		
	}
	
/**
 * Si estamos en un ascensor, simplemente nos paramos, es decir, ponemos velocidad a 0.
 */
	private void ControlAscensor() {
    	//Simplemente nos paramos 
    	jugador.velocidad=0;		
	}

	/** HAce un preajuste de la velocidad según diversas situaciones.
	 * 
	 */
	private void PreajusteVel() {
    	if      (jugador.HuecoSueloDelante==Vista.HuecoSaltable)
    		jugador.velocidad=300;
    	else if (jugador.CaidaDelante==Vista.CaidaRecuperable)
    		jugador.velocidad=100;
    	else if (jugador.CaidaDelante!=Vista.NoHay)
    		jugador.velocidad=50;
    	else if (jugador.ParedDelante==Vista.HayPared)
    		jugador.velocidad=velAnteMuro;
    	else if (jugador.RiesgoHueco)
    		jugador.velocidad=150;
    	else //En otro caso velocidad máxima
    		jugador.velocidad=300;	
	}

	
	/** Hace que el bot salte o se agache cuando haga falta para esquivar distintos obstáculos
	 */
    private boolean EsquivarSiNecesario()
    {
        //Si delante hay un objeto saltable, lo saltamos y retornamos
    	if (jugador.ParedDelante==Vista.HayObstaculo){
            jugador.postura = PlayerMove.POSTURE_JUMP;
            return true;
    	}    		
        if  (jugador.HuecoSueloDelante == Vista.HuecoSaltable)
        {
        	//Nos aseguramos de que lleve la velocidad correcta
        	if (jugador.PosBotAnt.distance(jugador.PosBot)>20){
        		jugador.postura = PlayerMove.POSTURE_JUMP;
            	return true;
        	}
        }
         //Si hay un hueco en la pared intentamos entrar por el
        if (jugador.ParedDelante==Vista.HayHueco) {    		  
        	jugador.postura = PlayerMove.POSTURE_CROUCH;
            return true;
        }
        else{ //En otro caso simplemente nos movemos de forma normal
        	jugador.postura =  PlayerMove.POSTURE_NORMAL;
        	return false;
        }    	
    } 

    /** Calcula si es necesario realizar un giro (hay un muro u objeto no 
     * saltable, precipicio, etc). Ajusta los parámetros de decisión de giro 
     * (distADer y distAIzq) y la velocidad del bot dependiendo del motivo
     * de giro
     */
    private boolean rectificarRuta()
    {        
        //Dos posibles caso: Precipicios y paredes. Precipicios es más prioritario.    
    
    	//Caso de precipicios
        if((jugador.BotUbicacion==Ubicacion.EnSuelo)) //Comprobamos que esté en suelo, en otro caso no tiene sentido 
        {  //Si hay un hueco no saltable y se trata de Precipio, caida no recuperable, agua o lava giramos
            if ( (jugador.HuecoSueloDelante==Vista.HuecoNoSaltable) &&
            	 ((jugador.CaidaDelante==Vista.Precipicio) 			||
                  ((jugador.CaidaDelante==Vista.CaidaNoRecuperable) && DecidirCaidaAleatorio())	||
                  (jugador.CaidaDelante==Vista.Lava) 				||
                  (!jugador.ExploracioAcuaticaActiva && ((jugador.CaidaDelante==Vista.AguaNoRecuperable) ||
                  (jugador.CaidaDelante==Vista.AguaRecuperable)))
                 )
               )
            {
                
            	//Lanzaremos rayos deviados en ciertos grados para determinar que nos encontramos delante       
            	//Calculamos los grados de la posicion actual para rotarlo
            	Vector3f diraux = new Vector3f();
            	Vector3f pos = new Vector3f(jugador.PosBot);
            	Vector2f v = new Vector2f();
            	
            	float GradosXYDer = dirGradosXY;
            	float GradosXYIzq = dirGradosXY;
            	for(int i=1; i<8;i++){

            		//A la derecha
            		GradosXYDer -= ConstGrados;
            		if(GradosXYDer < 0)
            		{
            			GradosXYDer += 360F;
            		} 
            		else               	
            			if(GradosXYDer > 360)
            			{
            				GradosXYDer -= 360F;
            			}
            		
            		v.set(Utils.degreesToVector2f(GradosXYDer));             
            		diraux.x = v.x;
            		diraux.y = v.y;
            		float CaidaDer=distancia.DistanciaHaciaAbajoLejano(pos,diraux);
            		

            		//A la izquierda
            		GradosXYIzq += ConstGrados;            		
            		if(GradosXYIzq < 0)
            		{
            			GradosXYIzq += 360F;
            		} 
            		else               	
            			if(GradosXYIzq > 360)
            			{
            				GradosXYIzq -= 360F;
            			}
            		
            		v.set(Utils.degreesToVector2f(GradosXYIzq));               
            		diraux.x = v.x;
            		diraux.y = v.y;
            		float CaidaIzq=distancia.DistanciaHaciaAbajoLejano(pos,diraux);          	
            	
            		if(CaidaDer<40){
            			if (CaidaIzq<40){
            				//Si ambos son iguales nos da igual hacia donde valla
            				distADer=(float) Math.random()*100 +1;
            				distAIzq=(float) Math.random()*100 +1;
            				break;
            			}            		
            			else{ //En este caso hay caida a la izquierda
            				distADer=(float) 100;
            				distAIzq=(float) 10;
            				break;
            			}
            		}
            		else //Hay caida a la derecha
            			if (CaidaIzq<40){
            				//No hay caida a la izquierda
            				distADer=(float) 10;
            				distAIzq=(float) 100;
            				break;
            			}
            			else{ //En este caso hay caida en ambos, no podemos decidir aun
            				distADer=(float) 0;
            				distAIzq=(float) 0;
            			}
            	}
            	if (distADer == 0){ //en este caso no se pudo decidir, nos damos la vuelta
            		jugador.DirMov.negate();
            	}
            	return true;
            }        
        }
        
        //Caso de una pared       
        if(jugador.ParedDelante==Vista.HayPared)
        {        	
        	//Calculamos primero el punto desde el que lanzaremos los rayos, esto es, la cabeza:
        	Vector3f pos = new Vector3f(jugador.PosBot);
        	pos.z+= Vista.ConstDistCabeza;            	
        	
            distAIzq = distancia.DistanciaRayoSolidoAngular(pos, null, ConstGrados, 100);
            distADer = distancia.DistanciaRayoSolidoAngular(pos, null, -ConstGrados, 100);
            
            //Para dar cierta aleatoriedad en la desición le sumamos o restamos a estos 
            //valores un parámetro que no sobrepasará el 50% del propio valor
            double IncrIzq= Math.random()*0.5 * Math.pow(-1, new Random().nextInt(20));
            double IncrDer= Math.random()*0.5 * Math.pow(-1, new Random().nextInt(20));
            distAIzq+= distAIzq*IncrIzq;
            distADer+= distADer*IncrDer;
            
            return true;
        }
        
        return false;
    }
    
    /** Cuando devuelve true quiere decir que no debe tirarse por CaidasNorecuperables. Sirve
     * Para evitar situaciones en las que se queda "encerrado" en algunas zonas del mapa */
    private boolean DecidirCaidaAleatorio() {
    	return  (Math.random() > 0.7F) ? true : false;
	}

	/** Realiza un giro dependiendo del ángulo con el que llegue a un objeto.
     * Da algo de aleatoriedad tanto en la decisión del sentido del giro 
     * como en el de los grados que gira.
     */     
    private void giroSuave()
    {
        //Giramos hacia aquel lado cuya distancia sea mayor. Cada vez que 
        //se gira en un sentido, dicho sentido incrementa su peso, siendo más
        //probable que se escoja en la siguiente llamada para continuar el giro. 
    	//(Evita atcascarse en esquinas)
              
        if((pesoDer * distADer) >= (pesoIzq *distAIzq))
        {
            pesoIzq -= decrementoPeso;
            girarDerecha(giroMinimo + new Random().nextInt(20));
        }
        else
        {
            pesoDer -= decrementoPeso;
            girarIzquierda(giroMinimo + new Random().nextInt(20));
        }
    }
    
    /** Gira el vector de dirección de movimiento a la derecha en 'grados' */
    private void girarDerecha(float grados)
    {
        //Se suman los grados en módulo, pero no se puede usar el 
        //operador de módulo con "floats"        
    	dirGradosXY -= grados;
        if(dirGradosXY < 0)
        {
        	dirGradosXY += 360F;
        }
        Vector2f dirMovXY= new Vector2f(Utils.degreesToVector2f(dirGradosXY));
        jugador.DirMov.x = dirMovXY.x;
        jugador.DirMov.y = dirMovXY.y;
    }
    
    /** Gira el vector de dirección de movimiento a la izquierda en 'grados' */
    private void girarIzquierda(float grados)
    {
        //Se suman los grados en módulo, pero no se puede usar el 
        //operador de módulo con "floats"        
    	dirGradosXY += grados;
        if(dirGradosXY > 360)
        {
        	dirGradosXY -= 360F;
        }
        Vector2f dirMovXY= new Vector2f(Utils.degreesToVector2f(dirGradosXY));
        jugador.DirMov.x = dirMovXY.x;
        jugador.DirMov.y = dirMovXY.y;
    }
    
    /** Si el bot está atascado en un punto, intenta salir de él*/
    private void salirDeAtasco(){
        //Del primer nivel se intenta salir girando a un lado
        if (jugador.Atascado == Ubicacion.AtascadoNivel1){
        	Vector3f dir = new Vector3f();
        	dirGradosXY += 25;
        	Vector2f dir2f= new Vector2f(Utils.degreesToVector2f(dirGradosXY));
        	dir.x=dir2f.x;
        	dir.y=dir2f.y;
        	float distIzq= distancia.DistanciaRayoSolido(null, dir, 100);
        	dirGradosXY -= 50;
        	dir2f.set(Utils.degreesToVector2f(dirGradosXY));
        	dir.x=dir2f.x;
        	dir.y=dir2f.y;
        	float distDer= distancia.DistanciaRayoSolido(null, dir, 100);
        	
        	if (distDer>distIzq){
        		this.girarDerecha(25);
        		System.out.println("Giro derecha:"+ distDer + " - " + distIzq);
        	}
        	else{
        		this.girarIzquierda(25);
        		System.out.println("Giro izquierda:"+ distDer + " - " + distIzq);
        	}
        }
        else
            //del segundo nivel de atasco se intenta salir haciendo un giro en la dirección
            if (jugador.Atascado == Ubicacion.AtascadoNivel2){
            	jugador.DirMov.negate();
            	jugador.postura=PlayerMove.POSTURE_NORMAL;
            }
    }

    
    /** Rutina que hará que el bot intente salir del agua llendo a la superficie 
     * y buscando el borde saltable más cercano 
     */
    private void salirDeAgua(){   	
        //Si tarda mucho tiempo en salir del agua lo ahogamos
        if ((System.currentTimeMillis()-jugador.TiempoEnAgua)>MiBotseMueve.TiempoEnAguaMax){        	
        	System.out.println("INTENTANDO AHOGARME");
            jugador.postura = PlayerMove.POSTURE_CROUCH;   
            jugador.velocidad=0;
            return;
        } 
        //Intentamos salir del agua        	 
        rectificarRutaAcuatica();
        jugador.velocidad = 300;
        jugador.postura = PlayerMove.POSTURE_JUMP;
        jugador.dirVista.z=0.9999F;
    }
    
    /** Rutina que determinará cual es la mejor via para salir del agua. Modificará DirMov y dirVista.
     *  Para hacer esto se basa en la posición (x y) del bot, calculando dos puntos desplazados en z 
     *  pero conservando las cordenadas x y.
     *  Estos puntos serán la superficie del agua y un punto un poco por encima de esta. A partir de ella
     *  se lanzan rayos en todas las direcciones (8 direcciones) y en base a estos valores se determina
     *  cual es el borde saltable más cercano.
     * */
    private void rectificarRutaAcuatica(){
		Vector3f dir = new Vector3f(0,0,-0.99999F); //Apunta hacia abajo
		Vector3f pos = new Vector3f(jugador.PosBot);							
		//pos.set(w.getPlayer().getPlayerMove().getOrigin());
    	
		//Aumentamos mucho la coordenada z por si el bot se encuentra bajo el agua
		pos.z+=300;
		
		//Hallamos primero el punto del agua, esto es, conservando las cordenadas x e y del jugador
		//calcular la coordenada z en la que el jugador toca el agua.
		pos.set(distancia.PuntoRayoAgua(pos,dir,320));
		
		//Ya tenemos el punto del agua. Ahora trazaremos rayos en todas las direcciones para determinar 
		//cual es el mejor camino:
		//Colocamos z
		dir.z=0;
		
		//Lanzaremos rayos en x e y en todas las combinaciones para valores {-1, 0, 1} (exepto 0 0)
		float DistMin=999;			
		for(int x=-1; x<2; x+=1)
			for(int y=-1; y<2; y+=1)
				if ((x==0) && (y==0)) continue; //El caso 0 0 no interesa 
				else {			
					dir.x=(float)x;
					dir.y=(float)y;
					//Hacemos esta resta porque en ocasiones se observan comportamientos "extraños"
					//al lanzar rayos en direcciones como 0 1, pero se comporta de forma correcta si su valor es
					//por ejemplo 0 0.999 ... (Razón desconocida...)
					dir.x-=0.01F; 
					dir.y-=0.01F;
					//Distancia a ras del agua 
					float DistCintura=distancia.DistanciaEsferaSolido(pos, dir,2 ,1000);
					//Distancia un poco más arriba
					pos.z+=20;	
					float DistCabeza=distancia.DistanciaEsferaSolido(pos, dir,2 ,1000);
					//Recolocamos pos.z en su posición original, a ras del agua, para la siguiente iteración
					pos.z-=20;
					//Comprobamos si es candidata, es decir, existe un hueco al que podamos subirnos
					if (DistCabeza-DistCintura>40) { //40 parece un buen valor, aunque podría ser algo más pequeño
						//Si es candidata comprobamos que no haya una mejor ya
						if (DistCintura<DistMin){							
							//Cambiamos la dirección de movimiento del Bot
							jugador.DirMov.x=dir.x;
							jugador.DirMov.y=dir.y;
							DistMin= DistCintura;
						}
					}
				}
		jugador.DirMov.normalize();
		jugador.dirVista.x=jugador.DirMov.x;
		jugador.dirVista.y=jugador.DirMov.y;		
    }

    /** En teoria sólo tiene que seguir en linea recta y si acaso saltar para esquivar huecos del suelo
     *  
     * @param dirEntidad
     */
	public void irA(Vector3f posEntidad) {
		Vector3f dir= new Vector3f(posEntidad);		
		dir.sub(jugador.PosBot);
		jugador.DirMov.x=dir.x;
		jugador.DirMov.y=dir.y;
		jugador.velocidad=300;		
		EsquivarSiNecesario();	
		salirDeAtasco();
	}

}


    

