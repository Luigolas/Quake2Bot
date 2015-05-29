
public class LanzaBots {

	//Declaración de variables de nuestro tipo particular de bots
	static MiBotseMueve MiBot; //,MiBot2; //,MiBot3; //,MiBot4; //Si creamos más de un bot propio debemos usar más variables 

	
	//Enemigos dummies
	//static SampleObserverBot Bot1,Bot2,Bot3;
	
	public static void main (String[] args) 
	{				
		Init();	
	}
	
	public static void Init()
	{		
		//Establece la ruta del quake2, necesaria para tener información sobre los mapas.
		//Observa la doble barra
		String quake2_path="C:\\quake2";
		System.setProperty("QUAKE2", quake2_path); 

//		Creación de bots enemigos dummies
		//Bot1 = new SampleObserverBot("Bot1","male/athena");	
		//Bot2 = new SampleObserverBot("Bot2","male/athena");
		//Bot3 = new SampleObserverBot("Bot3","male/athena");
		
		//Bot1.connect("127.0.0.1",27910);
		//Bot2.connect("127.0.0.1",27910);
		//Bot3.connect("127.0.0.1",27910);
		//Bot1.connect("10.22.144.69",27910);
		//Bot2.connect("10.22.144.69",27910);
		//Bot3.connect("10.22.144.69",27910);
		//Fin dummies

		
		//Creación del bot (pueden crearse múltiples bots)
		MiBot = new MiBotseMueve("[SALPICA]1","cyborg/ps9000");		
//		MiBot2 = new MiBotseMueve("[SALPICA]2","female/jungle");
//		MiBot3 = new MiBotseMueve("S3","male/viper");
//		MiBot4 = new MiBotseMueve("MiBotseMueve4","male/sniper"); 
		
		//Conecta con el localhost (el servidor debe estar ya lanzado para que se produzca la conexión)
//		MiBot.connect("10.22.146.172",27910);//Ejemplo de conexión a la máquina local
//		MiBot2.connect("10.22.146.172",27910);//Ejemplo de conexión a la máquina local
//		MiBot3.connect("10.22.146.185",27910);//Ejemplo de conexión a la máquina local
	    MiBot.connect("127.0.0.1",27910);//Ejemplo de conexión a la máquina local
//		MiBot2.connect("127.0.0.1",27910);//Ejemplo de conexión a la máquina loca
//		MiBot3.connect("127.0.0.1",27910);//Ejemplo de conexión a la máquina local
		//MiBot4.connect("127.0.0.1",27910);//Ejemplo de conexión a la máquina local
		//MiBot.connect("10.22.144.69",27910);//Ejemplo de conexión a una dirección IP (puede ser la de otro ordenador)
		
		//Si tenemos diversos bots, haríamos las mismas acciones con ellos
		//MiBot2 = new MiBotseMueve("MiBotseMueve2","female/athena");
		//MiBot2.connect("127.0.0.1",27910);//Ejemplo para conectar más bots al servidor
		
	}
}
