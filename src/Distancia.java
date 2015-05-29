
import soc.qase.file.bsp.BSPBrush;
import soc.qase.state.World;
import soc.qase.tools.Utils;
import soc.qase.tools.vecmath.Vector2f;
import soc.qase.tools.vecmath.Vector3f;

/**
 *  Contiene métodos relacionados con el calculo de las distancias
 *  @author Luis Mª González Medina - Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 */
public class Distancia 
{ 
    private static final int ConstDistTransitable = 10;
	private static final float ConstProfunidadMaxima = 40;
	/** Referencia a las variables y funciones globales*/
    private MiBotseMueve jugador; 
    private World w;

    public Distancia(MiBotseMueve j, World W)
    {
        jugador=j;
        w=W;
    }

    
    /**Lanza un rayo desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho. Sólo encuentra objetos sólidos.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo.
     * 			Si se pasa null se toma la dirección de movimiento del bot.
     * @param limite Distancia máximima a la que se va a llegar con el rayo.
     * @return Distancia a la que se encuentra el objeto si es menor que limite,
     * si no, devuelve "limite".
     * */
    public float DistanciaRayoSolido (Vector3f start, Vector3f dir, float limite){
    	//Establecemos el Brush a Solido, para sólo encontrar objetos sólidos    
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID);    			 
    	return jugador.mibsp.getObstacleDistance(start, dir, limite);
    }
    
    /**Lanza una esfera de Radio indicado desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho. Sólo encuentra objetos sólidos.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot 
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo. 
     * 			Si se pasa null se toma la dirección de movimiento del bot
     * @param Radio Radio de la esfera
     * @param limite Distancia máximima a la que se va a llegar con el rayo
     * @return Distancia a la que se encuentra el objeto si es menor que limite,
     * si no, devuelve "limite".
     * */
    public float DistanciaEsferaSolido (Vector3f start, Vector3f dir, float Radio, float limite){
    	//Establecemos el Brush a Solido, para sólo encontrar objetos sólidos
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID);
		return jugador.mibsp.getObstacleDistance(start, dir, Radio, limite);    	
    }
    
    
    /**Lanza un rayo desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho, devolviendo el punto exacto decolisión.
     * Sólo encuentra agua, no objetos sólidos u otros.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot 
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo.
     * 			Si se pasa null se toma la dirección de movimiento del bot
     * @param limite Distancia máximima a la que se va a llegar con el rayo
     * @return Punto en Vector3f en el que se encuentra el agua si es menor que limite,
     * si no, devuelve el punto limite.
     * */
    public Vector3f PuntoRayoAgua (Vector3f start, Vector3f dir, float limite){
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_WATER);
    	Vector3f aDevolver=jugador.mibsp.getObstacleLocation(start, dir, limite);
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID); //Volvemos a ponerlo a solido para no tener probemas
    	return aDevolver;
    }
    
    /**Lanza un rayo desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho, devolviendo la distancia.
     * Sólo encuentra agua, no objetos sólidos u otros.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot 
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo.
     * 			Si se pasa null se toma la dirección de movimiento del bot
     * @param limite Distancia máximima a la que se va a llegar con el rayo
     * @return distancia a la que se encuentra el agua si es menor que limite,
     * si no, devuelve "limite"
     * */
    public float DistanciaRayoAgua (Vector3f start, Vector3f dir, float limite){
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_WATER);
		float aDevolver= jugador.mibsp.getObstacleDistance(start, dir, limite);  
		jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID); //Volvemos a ponerlo a solido para no tener probemas
		return aDevolver;
    }
    
    /**Lanza un rayo desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho, devolviendo el la distancia.
     * Sólo encuentra lava o slime, no objetos sólidos u otros.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot 
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo. 
     * 			Si se pasa null se toma la dirección de movimiento del bot
     * @param limite Distancia máximima a la que se va a llegar con el rayo
     * @return Distancia en la que encuentra la lava o slime si es menor que limite,
     * si no, devuelve "limite"
     * */
    public float DistanciaRayoLava (Vector3f start, Vector3f dir, float limite){
    	float DistLava;
    	float DistSlime;
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_LAVA);
		DistLava = jugador.mibsp.getObstacleDistance(start, dir, limite);
		jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SLIME);
		DistSlime = jugador.mibsp.getObstacleDistance(start, dir, limite);
		jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID); //Volvemos a ponerlo a solido para no tener probemas
		return (DistSlime<DistLava)? DistSlime : DistLava;
    }
    
    /** Calcula un punto en la direccion indicada a la distancia indicada, tomando como base
     * el punto que se le pasa. A partir de este punto calculado lanza un rayo hacia abajo (z=-1)
     * hasta la distancia limite, devolviendo la primera colisión con un objeto sólido 
     * @param start Punto inicial. Si se pasa null se coge la posicion del bot
     * @param dir Dirección en la que calcular el punto. Si se pasa null se coge la direccion de movimiento del bot
     * @param dist Distancia a la que queremos que se cree el punto de referencia para lanzar el rayo
     * @param limite Hasta donde queremos que llegue el rayo en profundidad
     * @return Devuelve la primera colisión con un objeto sólido. Si el punto no se puede calcular porque
     * se sale del escenario o está en una pared se hace el punto un poco hacia atras y se calcula igualmente
     */
    public float DistanciaHaciaAbajo (Vector3f start, Vector3f dir, float dist, float limite){
    	//Si no proporciona un start cogemos el del bot    	
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}    	
    	//Si el punto no se puede calcular porque hay una pared se hace retroceder
    	dist = (DistanciaRayoSolido(start,dir,dist+1) - 1F);    	    	
    	
    	//Obtenemos el punto delante del player
    	Vector3f punto=jugador.mibsp.getObstacleLocation(start, dir, dist);
    	return DistanciaRayoSolido(punto,new Vector3f(0,0,-0.9999F),limite);    	
    }
    
    
    
    /** Función que determina si entre un punto y otro el bot puede transitar sin problemas.
     * Para ello busca si existen desniveles muy altos. Sólo será transitable si hay como mucho huecos saltables.
     * Se presupone que son visibles, sino el resultado puede ser inesperado...Advertido quedas!!
     * @param A Punto de Origen
     * @param B Punto Destino
     * @return
     */
    public boolean Transitable(Vector3f A, Vector3f B) {
    	
    	float dist = A.distance(B);    
				
		Vector3f dir = new Vector3f(B);
		dir.sub(A);
		float prof;
		int seguidos=0;
		
		for(int i=1; i*ConstDistTransitable<dist; i++){
			prof=DistanciaHaciaAbajo(A,dir, i*ConstDistTransitable,100);
			if (prof>ConstProfunidadMaxima){
				seguidos++;
				if (seguidos>16) //Si hay demasiadas "caidas" seguidas se considera insalvable por el bot.
					return false;
			}		
			else
				seguidos=0;
		}   	
    	return true;
    }
    
    
    /** Calcula un punto en la direccion indicada a la distancia indicada, tomando como base
     * el punto que se le pasa. A partir de este punto calculado lanza un rayo hacia abajo (z=-1)
     * hasta la distancia limite, devolviendo la primera colisión con agua.
     * @param start Punto inicial. Si se pasa null se coge la posicion del bot
     * @param dir Dirección en la que calcular el punto. Si se pasa null se coge la direccion de movimiento del bot
     * @param dist Distancia a la que queremos que se cree el punto de referencia para lanzar el rayo
     * @param limite Hasta donde queremos que llegue el rayo en profundidad
     * @return Devuelve la primera colisión con agua. Si el punto no se puede calcular porque
     * se sale del escenario o está en una pared se hace el punto un poco hacia atras y se calcula igualmente
     */
    public float DistanciaHaciaAbajoAgua (Vector3f start, Vector3f dir, float dist, float limite){
    	//Si no proporciona un start cogemos el del bot
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	//Si el punto no se puede calcular porque hay una pared se devuelve -1
    	//Si el punto no se puede calcular porque hay una pared se hace retroceder
    	dist = DistanciaRayoSolido(start,dir,dist+1) - 1F;
    	//En este caso se puede calcular
    	
    	//Obtenemos el punto delante del player
    	Vector3f punto=jugador.mibsp.getObstacleLocation(start, dir, dist);
    	float aDevolver = DistanciaRayoAgua(punto,new Vector3f(0,0,-0.9999F),limite);   
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID); //Volvemos a ponerlo a solido para no tener probemas
    	return aDevolver;
    }
    
    /** Calcula un punto en la direccion indicada a la distancia indicada, tomando como base
     * el punto que se le pasa. A partir de este punto calculado lanza un rayo hacia abajo (z=-1)
     * hasta la distancia limite, devolviendo la primera colisión con lava o slime.
     * @param start Punto inicial. Si se pasa null se coge la posicion del bot
     * @param dir Dirección en la que calcular el punto. Si se pasa null se coge la direccion de movimiento del bot
     * @param dist Distancia a la que queremos que se cree el punto de referencia para lanzar el rayo
     * @param limite Hasta donde queremos que llegue el rayo en profundidad
     * @return Devuelve la primera colisión con lava o slime. Si el punto no se puede calcular porque
     * se sale del escenario o está en una pared se hace el punto un poco hacia atras y se calcula igualmente
     */
    public float DistanciaHaciaAbajoLava (Vector3f start, Vector3f dir, float dist, float limite){
    	//Si no proporciona un start cogemos el del bot
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	//Si el punto no se puede calcular porque hay una pared lo movemos un poco hacia atrás
    	dist = DistanciaRayoSolido(start,dir,dist+1) - 1F;	
    	
    	//Obtenemos el punto delante del player
    	Vector3f punto=jugador.mibsp.getObstacleLocation(start, dir, dist);
    	return DistanciaRayoLava(punto,new Vector3f(0,0,-0.9999F),limite);    	
    }
    
    /** Igual que DistanciaRayoSolido pero con un nuevo parámetro, el angulo, que indica cuantos grados a 
     * derecha o izquierda queremos desviar el rayo antes de lanzarlo 
     * 
     * @param start
     * @param dir
     * @param grados Cuanto desviaremos el rayo antes de lanzarlo. Negativo para la derecha.
     * @param limite
     * @return
     * @see distancia.DistanciaRayoSolido()
     */
    public float DistanciaRayoSolidoAngular (Vector3f start, Vector3f dir, float grados, float limite) {
    	//Si no proporciona un start cogemos el del bot
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = jugador.DirMov;        	
    	}
    	float dirGradosXY = Utils.calcAngles(dir)[0];
        dirGradosXY += grados;
        Vector2f v = new Vector2f(Utils.degreesToVector2f(dirGradosXY));
        Vector3f diraux = new Vector3f(dir);
        diraux.x = v.x;
        diraux.y = v.y;
        return DistanciaRayoSolido(start,diraux,limite);    	
    }
    
    /** Indica si un punto es alcanzable por el bot desde otro punto de partida.
     *  Lo será si es visible estricto y transitable.
     * @param A, posición inicial
     * @param B, punto objetivo
     * @return true si es alcanzable. False en otro caso
     */
	public boolean alcanzable(Vector3f A, Vector3f B) {
		if (VisibleEstricto(A, B))			
			return (jugador.BotUbicacion==Ubicacion.SobreAgua ||
					jugador.BotUbicacion==Ubicacion.BajoAgua) ? true : Transitable(A,B);		
		else
			return false;

	}
	
	/** Similar a BSPParser.isVisible(), pero en lugar de lanzar un rayo
	 * lanza una esfera. 
	 * 
	 * @param a Origen
	 * @param b Destino
	 * @return True si es visible estricto. False en otro caso.
	 */
	private boolean VisibleEstricto(Vector3f a, Vector3f b) {
		return jugador.mibsp.traceSphere(a, b, 10).equals(b);
	}
	
	/**Metodo que sólo será usado por explorar para detectar a que lado conviene moverse cuando hay un precipicio
	 * en frente
	 * 
	 * @param pos posicion actual
	 * @param dir direccion a la que mirar
	 * @return
	 */
	public float DistanciaHaciaAbajoLejano(Vector3f pos, Vector3f dir) {
		
    	if (DistanciaRayoSolido(pos,dir,100)<40) //Hay un muro al lado
    		return 100; //decimos que hay precipicio para que no tome este lado
    	
    	//Obtenemos el punto delante del player
    	Vector3f punto=jugador.mibsp.getObstacleLocation(pos, dir, 60);
    	return DistanciaRayoSolido(punto,new Vector3f(0,0,-0.9999F),100);    
	}
	
	
    /**Lanza una esfera (radio 8) desde la posición indicada, hacia la dirección indicada llegando 
     * al limite indicado como mucho, devolviendo el punto exacto de colisión.
     * Sólo encuentra objetos sólidos.
     * @param start Vector3f indicando la posición de inicio. Si se pasa null se toma la posición del bot 
     * @param dir Vector3f que indica la dirección hacia la que lanzar el rayo.
     * 			Si se pasa null se toma la dirección de movimiento del bot
     * @param limite Distancia máximima a la que se va a llegar con el rayo
     * @return Punto en Vector3f en el que se encuentra la primera colisión si es menor que limite,
     * si no, devuelve el punto limite.
     * */	
	public Vector3f PuntoEsferaSolido(Vector3f start, Vector3f dir, float limite){
    	if (start==null){
        	start = new Vector3f(jugador.PosBot);
        	start.set(w.getPlayer().getPlayerMove().getOrigin());
    	}
    	if (dir==null){
        	dir = new Vector3f(jugador.DirMov);        	
    	}
    	jugador.mibsp.setBrushType(BSPBrush.CONTENTS_SOLID);
    	Vector3f aDevolver=jugador.mibsp.getObstacleLocation(start, dir, 8 , limite);    	
    	return aDevolver;
    }	

    
    
}