import java.util.Random;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class EventoGeradorCliente extends ExternalEvent{
	
	/**
	 * Método construtor do EventoGeradorCliente.
	 *
	 * Cria um novo evento externo, responsável por gerar clientes
	 * que chegam à lavanderia.
	 * A criação do evento gerador de cliente ocorre por meio da chamada
	 * do método construtor da super-classe, ou seja, da classe ExternalEvent.
	 * 
	 * parâmetro owner: indica o modelo ao qual o evento externo está associado.
	 *                  
	 * parâmetro name: indica o nome do evento externo.
	 * 
	 * parâmetro showInTrace: é um flag que indica se o evento deve ou não
	 *                        produzir saídas para um trace de saída da simulação.
	 */      
	Boolean isPool = false;

	public EventoGeradorCliente(Model owner, String name, boolean showInTrace, boolean isPool) {
		super (owner, name, showInTrace);
		this.isPool = isPool;
	}

	public EventoGeradorCliente(Model owner, String name, boolean showInTrace) {
		super (owner, name, showInTrace);
	}

	@Override
	/**
	 * Esse método é invocado sempre que o relógio da simulação atinge o momento
	 * para o qual o evento externo de geração de clientes da lavanderia foi escalonado.
	 * Ele indica o que deve ser feito quando esse momento é alcançado.
	 * 
	 * Nesse caso, um novo cliente deve ser gerado.
	 * Além disso, ele deve ser servido pela máquina de lavar da lavanderia ou
	 * entrar em uma fila de espera.
	 * O momento em que um novo cliente deve
	 * chegar à lavanderia também é determinado e o evento correspondente
	 * (chegada desse cliente à lavanderia) criado e escalonado (para dar continuidade à simulação).
	 */
	public void eventRoutine() {
		
		Servidor modeloServidor;
		Cliente cliente;
		EventoGeradorCliente eventoGeradorCliente;
		double instanteChegadaCliente;
		
		// Identificação do modelo ao qual o evento de geração de cliente pertence.
		modeloServidor = (Servidor) getModel();
		
		// Criação de um novo cliente que chega à lavanderia.
		cliente = new Cliente (modeloServidor, "Cliente", true);
		modeloServidor.sendTraceNote ("Cliente chegou à lavanderia em " + modeloServidor.presentTime());
		
		// O cliente que chegou à lavanderia deve ser servido.

		Random gerador = new Random();
		if(gerador.nextInt(101) < 68){
			modeloServidor.servirCliente(cliente, 0); // cpu um
		}else{
			modeloServidor.servirCliente(cliente,  1); // cpu 2
		}
		
		
		/**
		 * O instante em que um novo cliente deve chegar à lavanderia é determinado, 
		 * de acordo com a distribuição de probabilidade do tempo entre chegadas de clientes sucessivos.
		 */
		//instanteChegadaCliente = modeloServidor.getTempoEntreChegadasClientes(this.isPool);
		
		// O evento correspondente à chegada do próximo cliente à lavanderia é criado.
		//eventoGeradorCliente = new EventoGeradorCliente (modeloServidor, "Evento externo responsável por gerar um cliente que chega à lavanderia", true);
		// E a chegada desse próximo cliente é escalonada.
		//eventoGeradorCliente.schedule (new TimeSpan(instanteChegadaCliente));
	}
}