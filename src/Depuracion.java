import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import soc.qase.state.PlayerMove;
import soc.qase.tools.Utils;
import soc.qase.tools.vecmath.Vector2f;

/**
 *  Clase útil para probar las características del bot.
 *  Da el control del bot al teclado para que el usuario
 * lo pueda mover y dirigir a zonas específicas.
 *  Para conseguir este comportamiento, la nueva ventana 
 * que crea debe tener el foco del teclado 
 * @author Pablo Amoedo Paz - Álvaro López Espinosa
 * @modified by Luis Mª González Medina -  Rubén Domínguez Falcón - Fco. Yeray Marrero Cabrera
 */

public class Depuracion 
{
    /** Referencia a las variables y funciones globales*/
    private MiBotseMueve BOT; 
    /** Indica que se está trabajando en modo depuración. Lo usan
     * las distintas clases para, por ejemplo, imprimir datos por 
     * pantalla.
     */
    public boolean depurando = false;
    /** Ventana que recoje los eventos de teclado */
    public JFrame recolectorEventos;
    
    private float dirGradosXY;
    
    public Depuracion (MiBotseMueve bot, String nomBot)
    {
        recolectorEventos = new JFrame("Recolector de eventos de "+nomBot);
        BOT=bot;
    }
    /**
     *  Asigna teclas al movimiento y disparo. Bastante limitada.
     * Su única utilidad es probar distintas funcionalidades del bot
     */
    public void asignarTeclas()
    {
        recolectorEventos.addKeyListener
        (
            new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    dirGradosXY = Utils.calcAngles(BOT.DirMov)[0];                    
                            
                    int tPul = e.getKeyCode();
                    if(tPul == KeyEvent.VK_P) 
                    {
                    	if(BOT.depuracion) BOT.depuracion=false;
                    	else {
                    		BOT.depuracion=true;
                    		BOT.velocidad=0;
                    	}
                        System.out.println("Cambiado modo de depuracion");                
                    }  
                    if (BOT.depuracion)
                    if(tPul == KeyEvent.VK_UP) //Caminar hacia delante
                    {
                        Vector2f v = new Vector2f(Utils.degreesToVector2f(dirGradosXY));
                        BOT.DirMov.x = v.x;
                        BOT.DirMov.y = v.y;
                        BOT.velocidad = 300;                        
                    }
                    else if(tPul == KeyEvent.VK_DOWN) //Hacia detrás
                    {
                        Vector2f v = new Vector2f(Utils.degreesToVector2f(dirGradosXY));
                        BOT.DirMov.x = -v.x;
                        BOT.DirMov.y = -v.y;                       
                        BOT.velocidad = 300;
                    }
                    else if(tPul == KeyEvent.VK_RIGHT) //Curvar derecha
                    {                    	
                        dirGradosXY -= 4;
                        Vector2f v = new Vector2f(Utils.degreesToVector2f(dirGradosXY));
                        BOT.DirMov.x = v.x;
                        BOT.DirMov.y = v.y;                        
                    }
                    else if(tPul == KeyEvent.VK_LEFT) //Curvar izquierda
                    {
                        dirGradosXY += 4;
                        Vector2f v = new Vector2f(Utils.degreesToVector2f(dirGradosXY));
                        BOT.DirMov.x = v.x;
                        BOT.DirMov.y = v.y;                       
                    }       
                    else if(tPul == KeyEvent.VK_W) //mira a 1 0
                    {
                        BOT.DirMov.x = 1F;
                        BOT.DirMov.y = 0F;
                    }      
                    else if(tPul == KeyEvent.VK_S) //mira a 1 0
                    {
                        BOT.DirMov.x = -1F;
                        BOT.DirMov.y = 0F;       
                    }  
                    else if(tPul == KeyEvent.VK_A) //mira a 1 0
                    {
                        BOT.DirMov.x = 0.0001F;
                        BOT.DirMov.y = 0.9999F;
                    }      
                    else if(tPul == KeyEvent.VK_D) //mira a 1 0
                    {
                        BOT.DirMov.x = -0.0001F;
                        BOT.DirMov.y = -1.0001F;                                    
                    }                    
                    else if(tPul == KeyEvent.VK_SPACE) //Saltar
                    {
                        BOT.postura = PlayerMove.POSTURE_JUMP;
                    }
                    else if(tPul == KeyEvent.VK_C) //Agacharse
                    {
                    	BOT.postura = PlayerMove.POSTURE_CROUCH;
                    }
                    else if(tPul == KeyEvent.VK_1) 
                    {
                        BOT.ArmaDep=1;
                    } 
                    else if(tPul == KeyEvent.VK_2) 
                    {
                    	BOT.ArmaDep=2;
                    } 
                    else if(tPul == KeyEvent.VK_3) 
                    {
                    	BOT.ArmaDep=3;
                    } 
                    else if(tPul == KeyEvent.VK_4) 
                    {
                    	BOT.ArmaDep=4;
                    } 
                    else if(tPul == KeyEvent.VK_5) 
                    {
                    	BOT.ArmaDep=5;
                    } 
                    else if(tPul == KeyEvent.VK_6) 
                    {
                    	BOT.ArmaDep=6;
                    } 
                    else if(tPul == KeyEvent.VK_7) 
                    {
                    	BOT.ArmaDep=7;
                    } 
                    else if(tPul == KeyEvent.VK_8)
                    {
                    	BOT.ArmaDep=8;
                    } 
                    else if(tPul == KeyEvent.VK_9)
                    {
                    	BOT.ArmaDep=9;
                    } 
                    else if(tPul == KeyEvent.VK_0) 
                    {
                    	BOT.ArmaDep=0;
                    } 
                }
                @Override
                public void keyReleased(KeyEvent e)
                {
                    int tPul = e.getKeyCode();
                    
                    if(BOT.depuracion)
                    if( (tPul == KeyEvent.VK_UP) || (tPul == KeyEvent.VK_DOWN) ) //Se para
                    {
                       BOT.velocidad = 0;
                    }
                    else if(tPul == KeyEvent.VK_SPACE) //Deja de saltar
                    {
                        BOT.postura = PlayerMove.POSTURE_NORMAL;
                    }
                }
            }
        );
    }

}
