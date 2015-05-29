import java.util.Vector;
import soc.qase.file.bsp.BSPParser;
import soc.qase.state.Entity;
import soc.qase.state.Inventory;
import soc.qase.state.PlayerGun;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;
/**
 * Clase que implementa los métodos de toma de decisiones necesarios para
 * determinar las acciones a realizar por parte de bot.
 * 
 * @author Luis Mª González Medina -  Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 *
 */
public class Decision{
	private MiBotseMueve jugador;
	private World w;
	private BSPParser mibsp;
	private Combate combatir;
	private Distancia distancia;
	private Explorar explorar;	
	
	/**
	 * Constructor de la clase Decision
	 * @param J
	 * @param W
	 * @param bsp
	 */
    public Decision (MiBotseMueve J,World W,BSPParser bsp,Distancia d,Vista v)
    {     
    	jugador=J;
    	w=W;
    	mibsp=bsp;
    	distancia =d;
    	combatir = new Combate(J,W,mibsp,v);
    	explorar = new Explorar(jugador,w,distancia);
    }
	
    /**
     * Función que según la información de la que dispone el bot tomará
     * una decisión sobre la siguiente acción a realizar.
     * 
     */
	public void accion(){
		
		ListaArmamento();
		
		DecideMovimiento();
		
		DecideVista();
		
	}

	/**	Selecciona el arma según la presencia de enemigos,			     
	 *	La distancia de estos, si posee el arma deseada y si tiene       
	 *	munición suficiente
	 */											     		
	private void ListaArmamento() {			
		try{
			final float DistanciaCorta=340;
			final float DistanciaLarga=510;
					
			//BFG es la más prioritaria siempre, sea la situación que sea
			if (w.getInventory().getCount(PlayerGun.BFG10K)>=1)	{
				if (w.getInventory().getCount(PlayerGun.CELLS)>=50){
					jugador.CambiarArma(PlayerGun.BFG10K);
					return;
				}
			}	
			
			//Si no tambien intentamos con la Railgun, sea la situación que sea
			if (w.getInventory().getCount(PlayerGun.RAILGUN)>=1)	{
				if (w.getInventory().getCount(PlayerGun.SLUGS)>=1){
					jugador.CambiarArma(PlayerGun.RAILGUN);
					return;
				}
			}
			
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//NO hay enemigo
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++
			if (jugador.Enemigo==-1){
				if (w.getInventory().getCount(PlayerGun.CELLS)>=50)jugador.CambiarArma(PlayerGun.BFG10K);
				else
				if (w.getInventory().getCount(PlayerGun.SLUGS)>=1)jugador.CambiarArma(PlayerGun.RAILGUN);
				else				
				if (w.getInventory().getCount(PlayerGun.ROCKETS)>=1)jugador.CambiarArma(PlayerGun.ROCKET_LAUNCHER);
				else
				if (w.getInventory().getCount(PlayerGun.CELLS)>=1)jugador.CambiarArma(PlayerGun.HYPERBLASTER);
				else
				if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SUPER_SHOTGUN);
				else
				if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.CHAINGUN);
				else
				if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.MACHINEGUN);
				else
				if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SHOTGUN);
				else
				if ((w.getInventory().getCount(PlayerGun.GRENADES)>=1) && 
					(w.getInventory().getCount(PlayerGun.GRENADE_LAUNCHER)>0))jugador.CambiarArma(PlayerGun.GRENADE_LAUNCHER);
				else 
					jugador.CambiarArma(PlayerGun.BLASTER);
			}
			else { //Hay un enemigo cerca
				//En este caso el enemigo no es visible. 
				//Nos basamos en la distancia para seleccionar el arma
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//Hay enemigo NO visible
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++
				if (!jugador.Combatiendo){					
					//Calculamos la distancia
					float distancia = jugador.PosBot.distance(w.getEntity(jugador.Enemigo).getOrigin().toVector3f()) ;
					//++++++++++++++++++++++++++++++++++++++++++++++++++++++
					//Estamos en la CORTA distancia
					//++++++++++++++++++++++++++++++++++++++++++++++++++++++
					if (distancia<DistanciaCorta){
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SUPER_SHOTGUN);
						else
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SHOTGUN);				
					} 
					else
					if(distancia>=DistanciaCorta && distancia<=DistanciaLarga)
					{
						//++++++++++++++++++++++++++++++++++++++++++++++++++++++
						//Estamos en la MEDIA distancia
						//++++++++++++++++++++++++++++++++++++++++++++++++++++++
						if (w.getInventory().getCount(PlayerGun.CELLS)>=1)jugador.CambiarArma(PlayerGun.HYPERBLASTER);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.CHAINGUN);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.MACHINEGUN);											
					}
					else
					{	//Vamos a definir la estrategia de armas a usar en  distancias largas
						//++++++++++++++++++++++++++++++++++
						//Estamos en LARGA distancia
						//++++++++++++++++++++++++++++++++++
						if (w.getInventory().getCount(PlayerGun.ROCKETS)>=1)jugador.CambiarArma(PlayerGun.ROCKET_LAUNCHER);
						else
						if (w.getInventory().getCount(PlayerGun.CELLS)>=5)jugador.CambiarArma(PlayerGun.HYPERBLASTER);						
					}
					
					
					//Si llegados a este punto seguimos con la blaster o granadas en la mano, escogemos por prioridades:
					if (jugador.IndiceArmas()==PlayerGun.BLASTER || jugador.IndiceArmas()==PlayerGun.GRENADES)						
						if (w.getInventory().getCount(PlayerGun.ROCKETS)>=1)jugador.CambiarArma(PlayerGun.ROCKET_LAUNCHER);
						else
						if (w.getInventory().getCount(PlayerGun.CELLS)>=1)jugador.CambiarArma(PlayerGun.HYPERBLASTER);
						else
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SUPER_SHOTGUN);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.CHAINGUN);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.MACHINEGUN);
						else
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SHOTGUN);
						else
						if ((w.getInventory().getCount(PlayerGun.GRENADES)>=1) && 
							(w.getInventory().getCount(PlayerGun.GRENADE_LAUNCHER)>0))jugador.CambiarArma(PlayerGun.GRENADE_LAUNCHER);
						else 
							jugador.CambiarArma(PlayerGun.BLASTER);	
					
				} else{ 
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++
				//Hay enemigo  visible
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++	
					//En este caso el enemigo es visible y estamos atacandole
					//En principio solo cambiaremos de arma si tenemos la blaster o las granadas de mano.	
					if (jugador.IndiceArmas()==PlayerGun.BLASTER || jugador.IndiceArmas()==PlayerGun.GRENADES)						
						if (w.getInventory().getCount(PlayerGun.ROCKETS)>=1)jugador.CambiarArma(PlayerGun.ROCKET_LAUNCHER);
						else
						if (w.getInventory().getCount(PlayerGun.CELLS)>=1)jugador.CambiarArma(PlayerGun.HYPERBLASTER);
						else
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SUPER_SHOTGUN);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.CHAINGUN);
						else
						if (w.getInventory().getCount(PlayerGun.BULLETS)>=1)jugador.CambiarArma(PlayerGun.MACHINEGUN);
						else
						if (w.getInventory().getCount(PlayerGun.SHELLS)>=1)jugador.CambiarArma(PlayerGun.SHOTGUN);
						else
						if ((w.getInventory().getCount(PlayerGun.GRENADES)>=1) && 
							(w.getInventory().getCount(PlayerGun.GRENADE_LAUNCHER)>0))jugador.CambiarArma(PlayerGun.GRENADE_LAUNCHER);
						else 
							jugador.CambiarArma(PlayerGun.BLASTER);
				}
			}	
		}
		catch (Exception ex){
			System.out.println(ex);
		}
	}

	/** Esta función decidirá la dirección de movimiento del bot. Ello dpenderá de si ha de hacer movimientos
	 * evasivos de combate, ir a buscar al´gun objeto, explorar...
	 * 
	 */
	private void DecideMovimiento() {
		//En primer lugar hemos de comprobar si disponemos de un arma o no disponemos de ella.
		//Puesto que entrar en combate usando el arma por defecto se concidera casi un suicidio
		if(jugador.TengoArma && !jugador.PocaMunicion){
			//Tenemos arma y municion suficiente y ademas estamos combatiendo
			if (jugador.Combatiendo){				
				if (RecogeEntidadEscencial())
					return;
				else{					
					combatir.Ataque();
				}
			}
			else{ //Tenemos arma y municion suficiente pero no estamos combatiendo				
				if(RecogeEntidad())
					return;				
				else{					
					explorar.explorar();
					return;
				}
			}
		}
		//No tenemos arma o municion suficiente
		else{
			//Independientemente de si estamos combatiendo o no:
			//Buscamos un arma cercana
			if (RecogeArmaCercana())
				return;
			//No hay armas cercanas: Intentamos coger cualquier otro objeto cercano
			if (RecogeEntidad())
				return;	
			
			//Si estamos combatiendo, atacamos (movimiento), si no, exploramos
			if (jugador.Combatiendo) {			
				combatir.Ataque();			
			}
			else{ //No estamos combatiendo. Buscamos un arma cuanto antes				
				explorar.explorar();
			}
		}	
	}

	/** Se llama cuando estamos inmersos en un combate pero queremos seguir recogiendo algún objeto escencial
	 * del escenario. Sólo recoge el objeto si el enemigo es visible desde el punto de recogida de ese objeto.
	 * 
	 * @return true si vamos a ir a recoger un objeto
	 */
	private boolean RecogeEntidadEscencial() {
		Entity entidad;
		entidad=BuscaVidas();
		if (entidad!=null){			
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}
		
		entidad=BuscaEscudo();
		if (entidad!=null){			
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}
		
		entidad=BuscaMunicion();
		if (entidad!=null){			
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}	
		
		//En este caso vamos a coger entidades SÓLO si mantienen la linea de visión con el enemigo
		entidad=this.BuscaEntidadPorNombre(null, null, null, false, 500);
		if (entidad!=null){
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}	
		
		//En otro caso retornamos false: No hay entidades escenciales que recoger 
		return false;
	}

	/** Decide si ir a buscar munición.
	 * 
	 * @return La entidad escogida. null si no hay ninguna
	 */
	private Entity BuscaMunicion() {
		switch (jugador.IndiceArmas()){
		//Si tenemos la blaster equipada vamos a coger cualquier munición
		case(Inventory.BLASTER): 
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,null,jugador.PocaMunicion,1500);
		
		case(Inventory.SHOTGUN):
		case(Inventory.SUPER_SHOTGUN):
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,Entity.SUBTYPE_SHELLS,jugador.PocaMunicion,1500);
			
		case(Inventory.MACHINEGUN):
		case(Inventory.CHAINGUN):
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,Entity.SUBTYPE_BULLETS,jugador.PocaMunicion,1500);
		
		case(Inventory.GRENADE_LAUNCHER):
			return BuscaEntidadPorNombre(null,Entity.TYPE_GRENADES,null,jugador.PocaMunicion,1500);
		
		case(Inventory.ROCKET_LAUNCHER):
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,Entity.SUBTYPE_ROCKETS,jugador.PocaMunicion,1500);
		
		case(Inventory.HYPERBLASTER):
		case(Inventory.BFG10K):
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,Entity.SUBTYPE_CELLS,jugador.PocaMunicion,1500);
		
		case(Inventory.RAILGUN):
			return BuscaEntidadPorNombre(null,Entity.TYPE_AMMO,Entity.SUBTYPE_SLUGS,jugador.PocaMunicion,1500);
		
		default: //Este caso en teoría no se da nunca...
			return null;
		}	
	}

	/** Busca el escudo visible más cercana y sólo la devuelve si ademas es visible para el enemigo,
	 * de forma que al ir a recogerla sigamos atacando al enemigo
	 * 
	 * @return La entidad escogida. null si no hay ninguna 
	 */
	private Entity BuscaEscudo() {
		return BuscaEntidadPorNombre(null, Entity.TYPE_ARMOR,null,jugador.PocoEscudo,1500);
	}
	
	/** Usada para buscar armas
	 * 
	 * @return true si se decide ir a recoger un arma
	 */
	private boolean RecogeArmaCercana() {
		Entity entidad;
		entidad=this.BuscaEntidadPorNombre(Entity.CAT_WEAPONS, null, null,true,1500); //Siempre es importante coger el arma
		if (entidad!=null){
			System.out.println("--Voy a recoger arma: " +entidad.getType());
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}
		else
			return false;
	}



	/** Va a recoger cualquier entidad alcanzable que sea necesaria
	 * 
	 * @return true si va a recoger alguna entidad.
	 */
	private boolean RecogeEntidad() {
		Entity entidad;
		entidad=BuscaEntidad();
		if (entidad!=null){		
			explorar.irA(entidad.getOrigin().toVector3f());
			return true;
		}
		else
			return false;		
	}

	/** Busca entidades cercanas alcanzables y necesarias.
	 * 
	 * @return La entidad encontrada. Si no encuentra ninguna interesante devuelve null
	 */
	@SuppressWarnings("unchecked")
	private Entity BuscaEntidad() {		
		Vector<Entity> entidades;
		entidades= w.getEntities(null, null, null, true);
		
		Entity aux;
		Entity result = null;
		float dist= 1500;
		
		for(int i=0;i<entidades.size();i++){
			aux=entidades.elementAt(i);
			//Comprobamos que no estén demasiado lejos o no hayamos encontrado otra ya más cerca
			float auxDist =jugador.PosBot.distance(aux.getOrigin().toVector3f());
			if(aux.getActive())
			if(auxDist<dist){
				//Comprobamos si es necesaria y es alcanzable				
				if (EntidadNecesaria(aux) && distancia.alcanzable(jugador.PosBot,aux.getOrigin().toVector3f())) {
					//En este acso nos interesa
					result = aux;
					dist = auxDist;
				}					
			}			
		}	
		return result;	
	}

	/**Busca la entidad por nombre visible más cercana y sólo la devuelve si ademas es visible para el enemigo,
	 * de forma que al ir a recogerla sigamos atacando al enemigo 
	 * 
	 * @param cat
	 * @param type
	 * @param subtype
	 * @param Importante Indica si es tan importante que no nos importa perder de vista al enemigo para reocger el objeto
	 * @param dist Parámetro que indica la máxima distancia a la que puede estar el objeto en cuestión
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Entity BuscaEntidadPorNombre(String cat, String type, String subtype, boolean Importante, float dist) {		
		Vector<Entity> entidades;
		//Recogemos todas las entidades que cumplen los requisitos
		entidades= w.getEntities(cat, type, subtype, true);
		
		Entity aux;
		Entity result = null;
		if (dist<50)
			dist=1500;			
		
		for(int i=0;i<entidades.size();i++){
			aux=entidades.elementAt(i);		
			//Comprobamos que no estén demasiado lejos o no hayamos encontrado otra ya más cerca
			float auxDist =jugador.PosBot.distance(aux.getOrigin().toVector3f());
			if(aux.getActive())		
			if(auxDist<dist){
				//Comprobamos si es necesaria y alcanzable para el jugador
				if ( EntidadNecesaria(aux) &&					 
					 (distancia.alcanzable(jugador.PosBot,aux.getOrigin().toVector3f()))){
					//En este caso tendremos que ver si nos importa perder de vsta al enemigo o no
					if (Importante){						
							//En este caso nos interesa sea visible por el enemigo o no
							result = aux;
							dist = auxDist;					
					}
					//En este caso tenemos que comprobar si es visible el enemigo desde el objeto
					else if((jugador.Enemigo!=-1) &&  //comprobamos esto por si se llama sin tener enemigos, un error
							(mibsp.isVisible(w.getEntity(jugador.Enemigo).getOrigin().toVector3f(),aux.getOrigin().toVector3f()))){
						result = aux;
						dist = auxDist;		
					}
				}					
			}			
		}
		return result;
	}

	/** Busca la vida visible más cercana y sólo la devuelve si ademas es visible para el enemigo,
	 * de forma que al ir a recogerla sigamos atacando al enemigo
	 * @return
	 */
	private Entity BuscaVidas() {				
		return BuscaEntidadPorNombre(null, Entity.TYPE_HEALTH, null, jugador.PocaVida,1500);
	}
	
	
	
	/** Determina si una entidad es necesaria en base a la cantidad de este que tenemos ya.
	 *  
	 * @param Entidad a comprobar
	 * @return True si es necesaria
	 */
	private boolean EntidadNecesaria(Entity Entidad) {
					
		//++++++++++++++++++++++++
		//CATEGORÍA ITEMS
		//++++++++++++++++++++++++
		if (Entidad.isItemEntity()) {
			//---------------
			//TIPO HEALING
			//---------------
			if (Entidad.getType().contains("healing")) {	
				if (Entidad.getSubType().contains("stimpack") &&
					(w.getPlayer().getHealth() < 300 )) { //El stimpack te da vidas aunque tengas 100 ya
					return true;
				}
				if (w.getPlayer().getHealth() < 100 ) {
					return true;
				}
											
			}
			else{
				//---------------
				//TIPO ARMOR
				//---------------
				if (Entidad.getType().contains("armor")) {						
					if (Entidad.getSubType().contains("jacket"))
						if (w.getPlayer().getArmor() < 50 )return true;					
					if (Entidad.getSubType().contains("combat") ||
					    Entidad.getSubType().contains("body")   ||
					    Entidad.getSubType().contains("shard")  ||
					    Entidad.getSubType().contains("shield") ) {
							if (w.getPlayer().getArmor() < 100 )return true;					
					}
				}
				else{
					//---------------
					//TIPO AMMO
					//---------------
					if (Entidad.getType().contains("ammo")) {
						if (Entidad.getSubType().contains("bullets")){
							if (w.getInventory().getCount(Inventory.BULLETS)<200)
								return true;
						}
						else if (Entidad.getSubType().contains("grenades")) {
							if (w.getInventory().getCount(Inventory.GRENADES)<50)
								return true;
						}
						else if (Entidad.getSubType().contains("rockets")) {
						if (w.getInventory().getCount(Inventory.ROCKETS)<50)
							return true;						
						}
						else if (Entidad.getSubType().contains("slugs")) {
							if (w.getInventory().getCount(Inventory.SLUGS)<50)
								return true;							
						}						
						else if (Entidad.getSubType().contains("shells")){
							if (w.getInventory().getCount(Inventory.SHELLS)<100) 
								return true;	
						}
						else if (Entidad.getSubType().contains("cells")) {	
							if (w.getInventory().getCount(Inventory.CELLS)<200)
								return true;						
						}
						else {
							//---------------
							//OTROS-> Items especiales
							//---------------
							//Otros items
							if (Entidad.getType().contains("quaddama") ||
								Entidad.getType().contains("mega_h")   ||
								Entidad.getType().contains("pack")     ||
								Entidad.getType().contains(Entity.TYPE_INVULNERABILITY) ||
								Entidad.getType().contains(Entity.TYPE_AMMOPACK) ||
								Entidad.getType().contains(Entity.TYPE_BANDOLIER) ||
								Entidad.getType().contains(Entity.TYPE_REBREATHER)																
								)
								return true;
						}
					}					
				}
			}
		}
		else{
			//++++++++++++++++++++++++
			//CATEGORÍA WEAPONS
			//++++++++++++++++++++++++					
			if (Entidad.isWeaponEntity()) {
				if (Entidad.getType().contains("g_rocket")) {
					if ((w.getInventory().getCount(Inventory.ROCKET_LAUNCHER)<1) ||
						(w.getInventory().getCount(Inventory.ROCKETS)<50))
						return true;
				}
				else if (Entidad.getType().contains("g_chain")){
					if ((w.getInventory().getCount(Inventory.CHAINGUN)<1) ||
							(w.getInventory().getCount(Inventory.BULLETS)<200))
						return true;
				}
				else if (Entidad.getType().contains("g_hyperb")) {
					if ((w.getInventory().getCount(Inventory.HYPERBLASTER)<1) ||
							(w.getInventory().getCount(Inventory.CELLS)<200))
						return true;
				}	
				else if (Entidad.getType().contains("g_machn")){
					if ((w.getInventory().getCount(Inventory.MACHINEGUN)<1) ||
					(w.getInventory().getCount(Inventory.BULLETS)<200))
						return true;
				}
				else if (Entidad.getType().contains("g_launch")){
					if ((w.getInventory().getCount(Inventory.GRENADE_LAUNCHER)<1) ||
							(w.getInventory().getCount(Inventory.GRENADES)<50))
						return true;
				}
				else if (Entidad.getType().contentEquals("g_shotg")) {
					if ((w.getInventory().getCount(Inventory.SHOTGUN)<1) || 
						(w.getInventory().getCount(Inventory.SHELLS)<100))
						return true;
				}
				else if (Entidad.getType().contentEquals("g_shotg2")) {
					if ((w.getInventory().getCount(Inventory.SUPER_SHOTGUN)<1) ||
						(w.getInventory().getCount(Inventory.SHELLS)<100))
						return true;					
				}
				else if (Entidad.getType().contentEquals("g_bfg")) {
					if ((w.getInventory().getCount(Inventory.BFG10K)<1) ||
						(w.getInventory().getCount(Inventory.CELLS)<200))
						return true;
				}
				else if (Entidad.getType().contentEquals("g_rail")) {
						if ((w.getInventory().getCount(Inventory.RAILGUN)<1) ||
							(w.getInventory().getCount(Inventory.SLUGS)<50))
							return true;
				}
			}
		}
		return false;
	}

	/** Modifica la dirección de vista del bot. En caso de combate apunta al enemigo.
	 * 
	 */
	private void DecideVista() {
		if (jugador.Combatiendo){
			//En el caso de estar combatiendo en el agua nos aseguramos de estar saltando
			if ((jugador.BotUbicacion==Ubicacion.BajoAgua) || (jugador.BotUbicacion==Ubicacion.SobreAgua))
				jugador.postura=PlayerMove.POSTURE_JUMP;
			//Apuntamos al enemigo
			jugador.dirVista=combatir.corregirApuntar(w.getEntity(jugador.Enemigo));
			
		}
		else{
			combatir.EnemigoPosAnterior.set(0,0,0);

			//Da igual donde miremos... Miramos hacia donde nos movemos por defecto
			jugador.dirVista=jugador.DirMov;			
		}		
	}
	
}