import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;
import java.util.Random;

public class EventoTerminoDispositivo extends EventOf2Entities<Dispositivo, Cliente> {

	/**
	 * Método construtor do EventoTerminoDispositivo.
	 *
	 * Cria um novo evento responsável por terminar
	 * a lavagem das roupas de um cliente da lavanderia.
	 * 
	 * A criação desse evento ocorre por meio da chamada
	 * do método construtor da super-classe, ou seja, da classe EventOf2Entities.
	 * 
	 * parâmetro owner: indica o modelo ao qual o evento está associado.
	 *                  
	 * parâmetro name: indica o nome do evento.
	 * 
	 * parâmetro showInTrace: é um flag que indica se o evento deve ou não
	 *                        produzir saídas para um trace de saída da simulação.
	 */
	public EventoTerminoDispositivo(Model owner, String name, boolean showInTrace) {
		super (owner, name, showInTrace);
	}

	@Override
	/**
	 * Esse método é invocado sempre que o relógio da simulação atinge o momento
	 * para o qual o evento relacionado ao término da lavagem das roupas de um cliente
	 * da lavanderia foi escalonado.
	 * Ele indica o que deve ser feito quando esse momento é alcançado.
	 * 
	 * Nesse caso, a máquina de lavar utilizada pelo cliente (passada como
	 * parâmetro para esse método) deve ser liberada.
	 */
	public void eventRoutine(Dispositivo dispositivo, Cliente cliente) {
		// Identificação do modelo ao qual o evento relacionado ao término da lavagem de roupas do cliente pertence.
		Servidor modeloServidor = (Servidor) getModel();
		modeloServidor.sendTraceNote(dispositivo + " terminou a função do dispositivo.");
		
		// A máquina de lavar que foi utilizada pelo cliente para lavar suas roupas deve ser liberada.
		if(dispositivo.codigo == 0 || dispositivo.codigo == 1){
			int valor = (int)(Math.random()*100);
			if(valor < 4){
				modeloServidor.liberarDispositivo(dispositivo, dispositivo.codigo, true); // LIBEROU PROCESSO 4%
			}else if(valor < 64){
				modeloServidor.liberarDispositivo(dispositivo, dispositivo.codigo, false);
				modeloServidor.servirCliente(cliente, 2); // FOI PRA DISCO UM 60 %
			}else{
				modeloServidor.liberarDispositivo(dispositivo, dispositivo.codigo, false);
				modeloServidor.servirCliente(cliente, 3); // FOI PRA DISCO DOIS 36%
			}
		}else{ // é um disco
			if((int)(Math.random()*100) < 68){
				modeloServidor.liberarDispositivo(dispositivo, dispositivo.codigo, false);
				modeloServidor.servirCliente(cliente, 0); // FOI PRA CPU UM 68 %
			}else{
				modeloServidor.liberarDispositivo(dispositivo, dispositivo.codigo, false);
				modeloServidor.servirCliente(cliente, 1); // FOIR PRA CPU DOIS 32%
			}
		}
	}
}
