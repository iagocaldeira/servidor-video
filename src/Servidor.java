import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Servidor extends Model{
	
	private static int contadorUtilizacao = 0;

	private static double tempoLavandoTotal = 0.0;
	private static double tempoTotalResposta = 0.0;

	// Definição do tempo de simulação.
	private static double tempoSimulacao = 60*1000;
	
	/**
	 * filaClientes: variável responsável por armazenar todos os clientes
	 * que estão aguardando a máquina de lavar ser liberada para utilizarem-na.
	 */
	
	public ArrayList<Queue> conjuntoDeFilas = new ArrayList<Queue>(4);
	public ArrayList<Dispositivo> conjuntoDeDispositivos = new ArrayList<Dispositivo>(4);
	
	/**
	 * distribuicaoTempoChegadasClientes: distribuição do tempo entre chegadas sucessivas de
	 * clientes à lavanderia.
	 * Será usada uma distribuição exponencial, com média de 40 minutos.
	 */
	private ContDistExponential distribuicaoTempoChegadasClientes;

	/**
	 * distribuicaoTempoChegadasClientesPool 750ms
	 */
	private ContDistExponential distribuicaoTempoChegadasClientesPool;
	
	/**
	 * distribuicaoTempoServicoMaquinaLavar: distribuição do tempo de serviço da máquina de lavar, ou seja,
	 * do tempo gasto pela máquina de lavar para servir (lavar as roupas) os clientes da lavanderia.
	 * Será usada uma distribuição uniforme, com valores entre 20 e 40 minutos.
	 */
	private ContDistUniform distribuicaoTempoServicoMaquinaLavar;
	
	/**
	    * Método construtor da Lavanderia.
	    *
	    * Cria um novo modelo, que representa o modelo de eventos discretos da lavanderia.
	    * A criação do modelo da lavanderia ocorre por meio da chamada
	    * do método construtor da super-classe, ou seja, da classe Model.
	    * 
	    * parâmetro owner: indica o modelo do qual esse modelo é parte.
	    * 				   Deve ser setado para null quando não existir um modelo,
	    *                  do qual esse novo modelo criado faz parte.
	    *                  
	    * parâmetro name: indica o nome do modelo.
	    * 
	    * parâmetro showInReport: é um flag booleano que indica se o modelo deve ou não
	    * 						  produzir saídas para um relatório da simulação.
	    *
	    * parâmetro showInTrace: é um flag que indica se o modelo deve ou não
	    *                        produzir saídas para um trace de saída.
	    */
	public Servidor(Model owner, String name, boolean showInReport, boolean showIntrace) {
		super (owner, name, showInReport, showIntrace);
	}
	public Servidor(Model owner) {
		super(owner,"", false, false);
	}
	
	@Override
	/**
	 * Método description
	 * 
	 * Retorna uma string que descreve o modelo criado.
	 * Essa descrição é automaticamente incluída 
	 * no relatório da simulação que é criado.
	 */
	public String description() {
		return ("Esse é o modelo de eventos discretos de uma lavanderia. " +
				"Clientes chegam em uma lavanderia self-service para lavarem suas roupas. " +
				"Eles esperam em uma fila do tipo FIFO até que a máquina de lavar-roupas fique disponível " +
				"para ser utilizada. " +
				"Depois que a lavagem das roupas trazidas pelo cliente termina, " +
				"o cliente deixa a lavanderia " +
				"e a máquina de lavar que foi utilizada torna-se disponível para servir o próximo cliente.");
	}
	
	@Override
	/**
	 * Método responsável por instanciar todos os componentes estáticos associados ao modelo (filas, entidades e distribuições).
	 * Assim, é necessário indicar, nesse método, todas as inicializações e instanciações necessárias.
	 * 
	 * Nesse caso, os componentes estáticos do modelo são a fila de clientes, a máquina de lavar, a distribuição
	 * do tempo entre chegadas sucessivas de clientes à lavanderia e a distribuição do tempo de serviço da máquina de lavar.
	 * 
	 * Esse método não deve ser utilizado para fazer os escalonamentos iniciais de eventos,
	 * necessários para o escalonador iniciar sua execução. O método doInitialSchedules()
	 * é que deve ser utilizado para isso.
	 */
	public void init() {
		
		/**
		 * Criação da fila de clientes aguardando sua vez de utilizarem a máquina de lavar.
		 * Parâmetros:
		 * Modelo ao qual a fila está associada.
		 * Nome da fila.
		 * Flag booleano que indica se a fila deve ou não produzir saídas para um relatório da simulação.
	     * Flag que indica se a fila deve ou não produzir saídas para um trace de saída.
	     */

	  Queue cpuum = new Queue<Cliente> (this, "Fila de cpu um aguardando serviço", true, true);
		conjuntoDeFilas.add(cpuum);
	  Queue cpudois = new Queue<Cliente> (this, "Fila de cpu dois aguardando serviço", true, true);
		conjuntoDeFilas.add(cpudois);
	  Queue discoum = new Queue<Cliente> (this, "Fila de disco um aguardando serviço", true, true);
		conjuntoDeFilas.add(discoum);
	  Queue discodois = new Queue<Cliente> (this, "Fila de disco dois aguardando serviço", true, true);
		conjuntoDeFilas.add(discodois);
	    
		/**
		 * Criação da entidade responsável por lavar as roupas trazidas pelos clientes à lavanderia.
		 * Parâmetros:
		 * Modelo ao qual a entidade está associada.
		 * Nome da entidade.
		 * Flag que indica se a entidade deve ou não produzir saídas para um trace de saída da simulação.
	     */

	  Dispositivo dCpu1 = new Dispositivo(this, "Cpu 1", true, 0);
		conjuntoDeDispositivos.add(dCpu1);
	  Dispositivo dCpu2 = new Dispositivo(this, "Cpu 2", true, 1);
		conjuntoDeDispositivos.add(dCpu2);
	  Dispositivo dDisco1 = new Dispositivo(this, "Disco 1", true, 2);
		conjuntoDeDispositivos.add(dDisco1);
	  Dispositivo dDisco2 = new Dispositivo(this, "Disco 2", true, 3);
		conjuntoDeDispositivos.add(dDisco2);
		
		/**
		 * Criação da distribuição do tempo entre chegadas sucessivas de clientes à lavanderia.
		 * Parâmetros:
		 * Modelo ao qual a distribuição está associada.
		 * Nome da distribuição.
		 * Double que indica a média dos valores dessa distribuição de probabilidade.
		 * Flag booleano que indica se a distribuição deve ou não produzir saídas para um relatório da simulação.
	     * Flag que indica se a distribuição deve ou não produzir saídas para um trace de saída.
	     */
		distribuicaoTempoChegadasClientes = new ContDistExponential (this, "Distribuição do tempo entre chegadas sucessivas de clientes à lavanderia", 40.0, true, true);
		/**
		 * Método que indica se os valores gerados por essa distribuição de probabilidade podem ser negativos ou apenas positivos.
		 * 
		 * Nesse caso, como o flag foi setado para "true", a distribuição deverá retornar apenas valores positivos.
		 */
		distribuicaoTempoChegadasClientes.setNonNegative(true);
		
		
		// Tempo dos novos clientes
		distribuicaoTempoChegadasClientesPool = new ContDistExponential (this, "Distribuição do tempo entre chegadas sucessivas de clientes à lavanderia", 750.0, true, true);


		/**
		 * Criação da distribuição do tempo de serviço da máquina de lavar.
		 * Parâmetros:
		 * Modelo ao qual a distribuição está associada.
		 * Nome da distribuição.
		 * Double que indica o limite inferior dos valores dessa distribuição de probabilidade.
		 * Double que indica o limite superior dos valores dessa distribuição de probabilidade.
		 * Flag booleano que indica se a distribuição deve ou não produzir saídas para um relatório da simulação.
	     * Flag que indica se a distribuição deve ou não produzir saídas para um trace de saída.
	     */
		
		distribuicaoTempoServicoMaquinaLavar = new ContDistUniform (this, "Distribuição do tempo de serviço da máquina de lavar roupas", 20.0, 40.0, true, true);
		
		/**
		 * Método que indica se os valores gerados por essa distribuição de probabilidade podem ser negativos ou apenas positivos.
		 * 
		 * Nesse caso, como o flag foi setado para "true", a distribuição deverá retornar apenas valores positivos.
		 */
		distribuicaoTempoServicoMaquinaLavar.setNonNegative(true);
	}
	
	@Override
	/** 
	 * Método responsável por ativar os componentes dinâmicos do modelo, ou seja, 
	 * basicamente, os eventos que iniciam a simulação.
	 * 
	 * Esse método é utilizado para escalonar todos os eventos, na lista interna de eventos 
	 * do simulador, necessários para iniciar a simulação.
	 * 
	 * Nesse caso, o evento externo gerador de clientes que chegam à lavanderia para
	 * lavarem suas roupas é criado e escalonado para ocorrer logo no início da simulação. 
     */
	public void doInitialSchedules() {
		this.gerarPrimeiroCliente();
		for (int i = 0; i < 100; i++) {
			this.gerarClientes(false);
		}
	}
	
	public void gerarPrimeiroCliente() {
		EventoGeradorCliente eventoGeradorCliente;
		
		eventoGeradorCliente = new EventoGeradorCliente (this, "Evento externo responsável por gerar um cliente que chega à lavanderia", true);
		
		/**
		 * Escalona o evento externo "eventoGeradorCliente" para ocorrer em um ponto específico da simulação.
		 * Nesse caso, o evento externo deverá ocorrer logo no início da simulação.
		 */
		eventoGeradorCliente.schedule(new TimeSpan(0.0));
	}
	
	public void gerarClientes(boolean isPool) {
		EventoGeradorCliente eventoGeradorCliente;
		
		double instanteChegadaCliente = this.getTempoEntreChegadasClientes(isPool);
				
		// O evento correspondente à chegada do próximo cliente à lavanderia é criado.
		eventoGeradorCliente = new EventoGeradorCliente (this, "Evento externo responsável por gerar um cliente que chega à lavanderia", true);
		// E a chegada desse próximo cliente é escalonada.
		eventoGeradorCliente.schedule (new TimeSpan(instanteChegadaCliente));

	}
	
	/**
	 * Método responsável por retornar uma amostra
	 * da distribuição de probabilidade utilizada para determinar o momento de chegada,
	 * na lavanderia, do próximo cliente.
	 */
	public double getTempoEntreChegadasClientes(Boolean isPool){
		if(isPool){
			return distribuicaoTempoChegadasClientesPool.sample();
		}
		return (distribuicaoTempoChegadasClientes.sample());
	}
	
	/**
	 * Método responsável por retornar uma amostra
	 * da distribuição de probabilidade utilizada para determinar
	 * o tempo de serviço da máquina de lavar-roupas.
	 */
	public double getTempoLavagem(int listIndex) {
		
		switch (listIndex) {
		case 0:
			return 0.002;
		case 1:
			return 0.002;
		case 2:
			return 0.002;
		case 3:
			return 0.004;
			
		}
		return 0;
		//return (distribuicaoTempoServicoMaquinaLavar.sample());	
	}

	/**
	 * Esse método verifica se a máquina de lavar está livre para ser utilizada pelo 
	 * novo cliente (passado como parâmetro) que chegou à lavanderia ou ocupada.
	 * No primeiro caso, a máquina de lavar roupas é alocada para esse cliente.
	 * No segundo caso, esse cliente entra em uma fila de espera, onde irá aguardar
	 * a liberação da máquina de lavar roupas. 
	 * @throws InterruptedException 
	 */
	public void servirCliente(Cliente cliente, int listIndex) {
			
		/**
		 * Verifica se a máquina de lavar roupas está livre,
		 * o que indica que o novo cliente que chegou à lavanderia pode utilizá-la.
		 */

		Dispositivo localDispo = conjuntoDeDispositivos.get(listIndex);

		if ((localDispo.getOcupada()) == false){
			
			// Modifica o estado da máquina de lavar-roupas indicando que, a partir desse momento, ela está sendo utilizada por um cliente.
			localDispo.setOcupada(true);

			// Utilização da máquina de lavar roupas pelo novo cliente que chegou à lavanderia.
			localDispo.lavar(cliente, listIndex);
		}else{
			/**
			 * Caso a máquina de lavar roupas esteja ocupada, lavando as roupas trazidas
			 * por outro cliente da lavanderia, o novo cliente que chegou à lavanderia
			 * entra em uma fila de espera para utilizar a máquina de lavar.
			 */
			conjuntoDeFilas.get(listIndex).insert(cliente);
		}
	}

	/**
	 * Método responsável por liberar a máquina de lavar-roupas passada como parâmetro.
	 * Caso exista algum cliente aguardando, na fila de espera, para utilizar a máquina de lavar,
	 * essa é realocada ao primeiro cliente da fila de espera.
	 */
	public void liberarDispositivo(Dispositivo dispositivo, int indexDoRecurso, boolean liberar) {
		Cliente cliente;
		
		sendTraceNote("Liberando dispositivo...");
		contadorUtilizacao++;
		tempoLavandoTotal += dispositivo.ultimoTempoLavando;
		tempoTotalResposta += this.presentTime().getTimeAsDouble() - dispositivo.cliente.inicioTempoResposta;
		
		// Verifica se existe algum cliente aguardando na fila de espera para utilizar a máquina de lavar.
		if (conjuntoDeFilas.get(indexDoRecurso).isEmpty()){
			
			/**
			 * Caso não exista nenhum cliente aguardando para utilizar a máquina de lavar roupas,
			 * essa é liberada.
			 */
			sendTraceNote("Dispositivo esperando clientes...");
			dispositivo.setOcupada(false);
			
		}else{
			sendTraceNote("Dispositivo será realocada.");
			
			// O primeiro cliente da fila de espera para utilizar a máquina de lavar é retirado dessa fila.
			Queue<Cliente> filinha = conjuntoDeFilas.get(indexDoRecurso);
			cliente = filinha.first();
			filinha.remove(cliente);
			
			// Utilização da máquina de lavar-roupas pelo primeiro cliente da fila de espera.
			dispositivo.lavar(cliente, indexDoRecurso);
		}
		if (liberar) {
			this.gerarClientes(true);
		}

	}
	
	


public static void main(String[] args) {
		Servidor modeloServidor;
		Experiment experimento;

		//Criação do modelo da lavanderia.
		modeloServidor = new Servidor (null, "Modelo de um servidor de vídeos", true, true);
		 
		// Criação do experimento da lavanderia self-service.
		experimento = new Experiment ("Experimento da servidor de vídeos");
		
		// Conecta o modelo da lavanderia ao experimento.
		modeloServidor.connectToExperiment(experimento);
		   
		// Determinação dos parâmetros do experimento:
		
		// Indica que, durante a simulação, uma barra de progresso deve ser mostrada.
		experimento.setShowProgressBar(true);
		
		// Indica quando a simulação deve ser interrompida.
		// Em nossa simulação, a unidade de tempo será um minuto. 
		experimento.stop(new TimeInstant(tempoSimulacao));   
		   
		// Indica o período durante o qual as informações da simulação devem ser armazenadas no trace da simulação.
		experimento.tracePeriod(new TimeInstant(0.0), new TimeInstant(tempoSimulacao));
		
		// Inicia o experimento no instante zero da simulação.
		experimento.start();

		// Gera um relatório, e outros arquivos de saída, relacionados ao modelo conectado ao experimento.
		experimento.report();

		// Interrompe todos os eventos que ainda estão escalonados e fecha todos os arquivos de saída.
		experimento.finish();


		System.out.println("");
		System.out.println("_____________");
		System.out.println("");

		Double tempoTotal = experimento.getStopTime().getTimeAsDouble();

		// taxa de processamento
		// troughput : tempo de simulação em relação a quantidade de clientes que sairam (resposta)
		
	/* 	System.out.println("Contador de saída : "+contadorUtilizacao);
		System.out.println("Tempo final do experimento : "+ tempoTotal);
		System.out.println("Troughput : "+(contadorUtilizacao/tempoTotal)+" clientes por minuto");
		System.out.println("");
		System.out.println(""); */

		// utilização
		// tempo que ficou ocupado - tempo de lavagem / tempo total (só somar quando acabar);

	/* 	System.out.println("Tempo total lavando (ocupado): "+tempoLavandoTotal);
		System.out.println("Tempo final do experimento : "+ tempoTotal);
		System.out.println("Utilização : "+ tempoLavandoTotal/tempoTotal);
		System.out.println("");
		System.out.println(""); */

		// tempo médio de resposta

	/* 	System.out.println("Contador de saída : "+contadorUtilizacao);
		System.out.println("Tempo final do experimento : "+ tempoTotalResposta);
		System.out.println("Tempo de resposta médio : "+(tempoTotalResposta/contadorUtilizacao)+"");
		System.out.println("");
		System.out.println(""); */

		System.out.println("");
		System.out.println("_____________");
		System.out.println("");

	}

}