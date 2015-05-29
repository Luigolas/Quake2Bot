import soc.qase.state.Player;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;

/**
 * Clase encargada de gestionar la ubicación del bot.
 * @author Luis Mª González Medina - Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 *
 */
public class Ubicacion{

	//******************************
	//++++++++CONSTANTES +++++++++++ 
	//******************************
	
	public static final int EnAire = 1;
	public static final int BajoAgua = 2;
	public static final int SobreAgua = 3;
	public static final int EnSuelo = 4;
	public static final int SubiendoAscensor = 5;
	public static final int BajandoAscensor = 6;

	public static final int NoAtascado = 0;
	public static final int AtascadoNivel1 = 1;
	public static final int AtascadoNivel2 = 2;
	/**Tolerancia para salir de atasco: Distancia mínima que ha de ser recorrida */
	public static final float TOL_SALIR_ATASCO = 3F;  
	public static long TOL_TIEMPO_ATASCO = 1000; //1 segundo
	
	
	
    private Distancia distancia;
    private MiBotseMueve jugador;
    private World w;
	

	//******************************
	//++++++++VARIABLES+++++++++++++ 
	//******************************
    /**Mide el tiempo que llevamos en el mismo nivel de atascamiento */
	private long TiempoAtasco=0;

	
    
    /**
     * Constructor de la clase Ubicación. Inicializa los atributos
     * @param j 
     * @param world 
     * @param dist
     */
    public Ubicacion (MiBotseMueve j, World world, Distancia dist){
    	jugador=j;    	  	
        distancia = dist;        
        w=world;
    }
    
    /**
     * Función que actualizar el parámetro BotUbicación del bot.
     */
    public void actualizar(){
    	jugador.BotUbicacion = DondeEstoy();   
    	jugador.Atascado= EstoyAtascado();   
    }
    
    /**
     * Función que retorna uno de los valores posibles de ubicaciones del bot
     * @return
     * + No se contempla lo de BajandoAscensor en esta version
     */
    private int DondeEstoy() {
    	Player P = w.getPlayer();
		if(P.isUnderWater())
			return BajoAgua;
		if (jugador.EnAscensor() && ((jugador.PosBot.z-jugador.PosBotAnt.z) > 2))
			return SubiendoAscensor;
		Vector3f dir = new Vector3f (0,0,-0.9999F);
		if(distancia.DistanciaEsferaSolido(null, dir, 10, 33) < 33)
			return EnSuelo;
		Vector3f pos = new Vector3f(jugador.PosBot);
		pos.z+=30;
		if(distancia.DistanciaRayoAgua(pos, dir, 60) < 55)
			return SobreAgua;
		else
			if (jugador.EnAscensor())
				return EnSuelo;
			else
				return EnAire;		
	}
    
    /** Detecta si el bot está atascado*/
    private int EstoyAtascado(){    	
    	if (jugador.BotUbicacion==SubiendoAscensor){
        	this.TiempoAtasco = 0;
        	return NoAtascado;
    	}
        //si no se movió desde la última vez    	
        if (jugador.PosBot.distance(jugador.PosBotAnt) < TOL_SALIR_ATASCO){        	
            //Después de X segundos en la misma posición se aumenta el nivel del estado
            //de atascamiento para que se actúe en consecuencia
        	if (TiempoAtasco <= 1){ //Inicializamos el timer   	
        		TiempoAtasco=System.currentTimeMillis();        		
        	}//En este caso el temporizador ya está iniciado        	
        	else if ((System.currentTimeMillis()-TiempoAtasco) > TOL_TIEMPO_ATASCO){        		
        		//En este caso llevamos demasiado tiempo atascados
        		//Reiniciamos el contador y aumentamos en 1 el nivel de atascamiento        		
                TiempoAtasco = 0;
                //tras X segundos atascado activa el nivel de atascamiento correspondiente
                if (jugador.Atascado==AtascadoNivel2) return AtascadoNivel1;  
                else return (jugador.Atascado+1);
            } 
        }
        //En este caso no está atascado
        else{          	
        	this.TiempoAtasco = 0;
        	return NoAtascado;
        }       
        return jugador.Atascado;
    }
}
