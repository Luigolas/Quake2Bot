import soc.qase.ai.waypoint.Waypoint;
import soc.qase.ai.waypoint.WaypointMap;
import soc.qase.file.bsp.BSPParser;
import soc.qase.tools.vecmath.Vector3f;

/** Clase mapa no funcional
 * 
 *  @author Luis M� Gonz�lez Medina - Rub�n Dom�nguez Falc�n - Fco. Yeray Marrero Cabrera
 *
 */
public class Mapa {
	//Variables internas
	/** Mapa de Puntos de tipo WaypointMap */
	private static WaypointMap MapaDePuntos;
	/** �ltimo punto guardado en el mapa */
	private Waypoint PuntoAnterior;
	/** BSParser para calculos de distancias, visibilidades... en el mapa */
	private BSPParser mibsp;
	/** Para calculo de distancias*/
	private Distancia distancia;
	/** Para acceder a valores sobre el bot como su posici�n o su direccion de movimiento actual */
	private MiBotseMueve jugador;
	
	//Constantes internas
	/** Constante que indica cuanto adelantamos el punto en la funci�n GuardarPunto para comprobar visibilidad y transitabilidad*/
	private static final float DistPuntoAdelantado = 60;
	/** Distancia maxima entre dos puntos para poder ser considerados equivalentes */	
	private static final float DistanciaEntrePuntos = 100;	
	
/**Constructor
 * 
 */
public Mapa(Distancia dist, MiBotseMueve bot){
	MapaDePuntos= new WaypointMap();
	jugador=bot;
	distancia=dist;
	//Al principio vale nulo
	PuntoAnterior=null; 	
	mibsp=jugador.mibsp;	
}

/** Lo primero es comprobar que los dos puntos son visibles. Si no son visibles algo raro ha pasado (probablemente ha muerto).   
 *  En ese caso llamaremos a la funci�n GuardarPuntoReaparici�n. En caso de que sean visibles:
 *  
 * Comprueba si el punto es candidato a ser guardado. Para ello, calcula un punto un poco m�s avanzado
 * del jugador en la direcci�n de movimiento de este y comprueba si es visible y transitable. Si es el caso,
 * comprueba si tiene una distancia suficiente con respecto al punto anterior guardado. En caso de
 * no serlo, el punto pasado se convierte en candidato a ser guardado. Tendremos que comprobar en ese caso  
 * si no existe un waypoint equivalente ya (ver funcion WaypointEquivalente). Si existe un equivalente simplemente
 * actualizariamos las conexiones, y si no existe waypoint equivalente lo guardamos en el mapa.
 * @param Punto Punto que queremos guardar
 */
public void GuardarPunto (Vector3f Punto) {	

	//Primera llamada a la funci�n
	if (PuntoAnterior==null) {
		//Guardamos el punto en el mapa sin conectarlo con ning�n otro
		PuntoAnterior = new Waypoint (Punto);
		MapaDePuntos.addNode(PuntoAnterior);		
		return;
	}
	
	//Obtenemos el Vector3f de PuntoAnterior
	Vector3f PuntoAnt=PuntoAnterior.getPosition();
	
	//En caso de que no sea visible se deber� a que hemos reaparecido (o una situaci�n equivalente 
	//no controlada e inesperada...)
	if (!mibsp.isVisible(Punto, PuntoAnt)){
		GuardarPuntoReaparicion(Punto);
		return;
	}
	
	//Cuidado si DirMov es 0,0,0
	//Calculamos el Punto al que se va a mover el bot	
	Vector3f NextPosBot=mibsp.getObstacleLocation(Punto, jugador.DirMov, DistPuntoAdelantado);
	//Comprobamos la visibilidad de este punto con respecto al punto anterior guardado:
	//Si no es alcanzable es candidato a ser guardado
	if (!distancia.alcanzable(PuntoAnt, NextPosBot)){
		//Tendr� que ser guardado a no ser que ya tengamos un punto equivalente
		Waypoint PuntoFinal = PuntoEquivalente(Punto);
		if (Punto.equals(PuntoFinal.getPosition())){ //Esta comprobacion no se si vale, creo que si
			//En este caso no existe un punto equivalente previamente y tenemos que guardarlo en el mapa
			MapaDePuntos.addNode(PuntoFinal);
		}
		//En este caso existe un punto equivalente, por lo que solo habr�a que actualizar las conexiones
		//Actualizamos las conexiones
		//int IndicePunto1=MapaDePuntos.indexOf(Wpunto);
		//int IndicePunto2=MapaDePuntos.indexOf(PuntoAnterior);
		//No estoy seguro de que funcione sin usar el indice de arriba
		MapaDePuntos.addEdge(PuntoAnterior, PuntoFinal, true);
		//Actualizamos PuntoAnterior
		PuntoAnterior=PuntoFinal;
	}
	//En este caso el punto no es candidato a ser guardado	
}


/** En este caso tenemos un punto "aislado" del punto anterior y probablemente aislado del mapa 
 * que ten�amos ya construido. Buscamos el mas cercano y miramos si podemos reconectarlo de alguna forma
 * Si no, se introduce sin m�s, sin conectarlo
 * 
 * @param Punto
 */
private void GuardarPuntoReaparicion(Vector3f Punto){
	//***********************************************************************
	
}


/** Comprueba si el waypoint que vamos a guardar, a pesar de cumplir las condiciones
 * de distancia y transitabilidad y visibilidad con el Punto anterior, est� demasiado
 * cerca de un waypoinyt ya guardado (y es visible y transitable) en cuyo caso se considerar�
 * equivalente al ya guardado y s�lo tendriamos que actualizar los edges.
 * 
 * @param Punto
 * @return El Waypoint Equivalente y si no lo hay lo crea
 */
private Waypoint PuntoEquivalente(Vector3f Punto){
	Waypoint WaypointMasCercano = MapaDePuntos.findClosestWaypoint(Punto);
	if (WaypointMasCercano == null) return null;
	Vector3f PuntoMasCercano = WaypointMasCercano.getPosition();
	
	if (Punto.distance(PuntoMasCercano)<DistanciaEntrePuntos){
		//En este caso podria ser considerado equivalente. Comprobamos
		//que sea alcanzable 
		if (distancia.alcanzable(PuntoMasCercano, Punto)) {		
			//Comprobamos que sea visible y transitable a su vez desde el PuntoAnterior
			Vector3f PuntoAnt=PuntoAnterior.getPosition();		
			if (distancia.alcanzable(PuntoMasCercano, PuntoAnt))
				return WaypointMasCercano;
		}
	}	
	//En este caso no hay un punto Equivalente
	return new Waypoint(Punto);	
}
	
	
}