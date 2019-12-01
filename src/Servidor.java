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
	
	public int quantidadeClientesPool = 1;
	public int contadorClientesGerados = 0;

	public int contadorVisitacaoCpu1 = 0;
	public int contadorVisitacaoCpu2 = 0;
	public int contadorVisitacaoDRapido = 0;
	public int contadorVisitacaoDLento = 0;
	public int contadorVisitacao = 0;


	public int contadorLiberacaoCpu1 = 0;
	public int contadorLiberacaoCpu2 = 0;
	public int contadorLiberacaoDRapido = 0;
	public int contadorLiberacaoDLento = 0;


	public double tempoLavandoTotal = 0.0;
	public double tempoLavandoD[] = { 0.0, 0.0, 0.0, 0.0};
	public double tempoTotalResposta = 0.0;


	// Definição do tempo de simulação.
	public double tempoSimulacao = 24;
	public TimeUnit unidadeSimulacao = TimeUnit.HOURS;
	
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
	public ContDistExponential distribuicaoTempoChegadasClientes;

	/**
	 * distribuicaoTempoChegadasClientesPool 750ms
	 */
	public ContDistExponential distribuicaoTempoChegadasClientesPool;
	
	/**
	 * distribuicaoTempoServicoMaquinaLavar: distribuição do tempo de serviço da máquina de lavar, ou seja,
	 * do tempo gasto pela máquina de lavar para servir (lavar as roupas) os clientes da lavanderia.
	 * Será usada uma distribuição uniforme, com valores entre 20 e 40 minutos.
	 */
	public ContDistUniform distribuicaoTempoServicoMaquinaLavar;
	public ContDistUniform distribuicaoTempoProcessador1;
	public double totalTempoDistribuicaoProcessador1 = 0;
	public double qtdTempoDistribuicaoProcessador1 = 0;
	public ContDistExponential distribuicaoTempoProcessador2;
	public double totalTempoDistribuicaoProcessador2 = 0;
	public double qtdTempoDistribuicaoProcessador2 = 0;
	public ContDistExponential distribuicaoTempoDiscoRapido;
	public double totalTempoDistribuicaoDiscoRapido = 0; 
	public double qtdTempoDistribuicaoDiscoRapido = 0; 
	public ContDistUniform distribuicaoTempoDiscoLento;
	public double totalTempoDistribuicaoDiscoLento = 0; 
	public double qtdTempoDistribuicaoDiscoLento = 0; 
	
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

	  Queue cpuum = new Queue<Cliente>(this, "Fila de cpu um aguardando serviço", true, true);
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
		
		distribuicaoTempoProcessador1 = new ContDistUniform (this, "Distribuição do tempo de serviço do CPU 1", 0.0, 35.0, true, true);
		distribuicaoTempoProcessador2 =  new ContDistExponential (this, "Distribuição do tempo de serviço do CPU 2", 25.0, true, true);
		distribuicaoTempoDiscoRapido =  new ContDistExponential (this, "Distribuição do tempo de serviço do Disco Rapido", 40.0, true, true);
		distribuicaoTempoDiscoLento = new ContDistUniform (this, "Distribuição do tempo de serviço do Disco lento", 15.0, 90.0, true, true);
		distribuicaoTempoProcessador1.setNonNegative(true);
		distribuicaoTempoProcessador2.setNonNegative(true);
		distribuicaoTempoDiscoRapido.setNonNegative(true);
		distribuicaoTempoDiscoLento.setNonNegative(true);
		/**
		 * Método que indica se os valores gerados por essa distribuição de probabilidade podem ser negativos ou apenas positivos.
		 * 
		 * Nesse caso, como o flag foi setado para "true", a distribuição deverá retornar apenas valores positivos.
		 */
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
		//this.gerarPrimeiroCliente();
		System.out.println(quantidadeClientesPool);
		for (int i = 0; i < quantidadeClientesPool; i++) {
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
		contadorClientesGerados++;

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
		double tempoLocal = 0;
		switch (listIndex) {
		case 0:
			tempoLocal = distribuicaoTempoProcessador1.sample();
			totalTempoDistribuicaoProcessador1 += tempoLocal;
			qtdTempoDistribuicaoProcessador1++;

			return tempoLocal;
		case 1:
			tempoLocal = distribuicaoTempoProcessador2.sample();
			totalTempoDistribuicaoProcessador2 += tempoLocal;
			qtdTempoDistribuicaoProcessador2++;

			return tempoLocal;
		case 2:
			tempoLocal = distribuicaoTempoDiscoRapido.sample();
			totalTempoDistribuicaoDiscoRapido += tempoLocal;
			qtdTempoDistribuicaoDiscoRapido++;

			return tempoLocal;
		case 3:
			tempoLocal = distribuicaoTempoDiscoLento.sample();
			totalTempoDistribuicaoDiscoLento += tempoLocal;
			qtdTempoDistribuicaoDiscoLento++;

			return tempoLocal;
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
		
		switch (indexDoRecurso) {
			case 0: contadorVisitacaoCpu1++; break;
			case 1: contadorVisitacaoCpu2++; break;
			case 2: contadorVisitacaoDRapido++; break;
			case 3: contadorVisitacaoDLento++; break;
			default: break;
		}

		tempoLavandoTotal += dispositivo.ultimoTempoLavando;
		tempoLavandoD[indexDoRecurso] += dispositivo.ultimoTempoLavando;
		
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
			cliente = (Cliente)conjuntoDeFilas.get(indexDoRecurso).first();
			conjuntoDeFilas.remove(cliente);
			
			// Utilização da máquina de lavar-roupas pelo primeiro cliente da fila de espera.
			dispositivo.lavar(cliente, indexDoRecurso);
		}
		if (liberar) {
			this.tempoTotalResposta += this.presentTime().getTimeAsDouble() - dispositivo.cliente.inicioTempoResposta;
			this.contadorVisitacao++;
			switch (indexDoRecurso) {
			case 0: contadorLiberacaoCpu1++; break;
			case 1: contadorLiberacaoCpu2++; break;
			case 2: contadorLiberacaoDRapido++; break;
			case 3: contadorLiberacaoDLento++; break;
			default: break;
		}
			this.gerarClientes(true);
		}

	}
	
public static void main(String[] args) {
		Servidor modeloServidor;
		Experiment experimento;
		String nomeD1 = "Cpu 1;", nomeD2 = "Cpu 2;", nomeD3 = "Disco Rapido;", nomeD4 = "Disco Lento;";
		Double visitaMediaD1 = 0.0, visitaMediaD2 = 0.0, visitaMediaD3 = 0.0, visitaMediaD4 = 0.0;
		Double utilizacaoD1 = 0.0, utilizacaoD2 = 0.0, utilizacaoD3 = 0.0, utilizacaoD4 = 0.0;
		Double filaD1 = 0.0, filaD2 = 0.0, filaD3 = 0.0, filaD4 = 0.0;
		Double tempoProcessador1=0.0, tempoProcessador2=0.0, tempoDiscoRapido=0.0, tempoDiscoLento=0.0;

		String questao1 = "", questao2 = "", questao3 = "", questao4 = "", questao5 = "", questao6 = "", questao7a = "", questao7b = "", questao7c = "";

		for (int i = 0; i <= 100; i++) {
			modeloServidor = new Servidor (null, "Modelo de um servidor de vídeos", true, true);
			modeloServidor.quantidadeClientesPool = i;
			experimento = new Experiment ("Experimento da servidor de vídeos");
			modeloServidor.connectToExperiment(experimento);
			experimento.setShowProgressBar(false);
			experimento.stop(new TimeInstant(modeloServidor.tempoSimulacao, modeloServidor.unidadeSimulacao));
			experimento.tracePeriod(new TimeInstant(0), new TimeInstant(modeloServidor.tempoSimulacao, modeloServidor.unidadeSimulacao));
			experimento.start();
			experimento.report();
			experimento.finish();
			double tempoTotal = experimento.getStopTime().getTimeAsDouble(TimeUnit.SECONDS);

			visitaMediaD1 = (double)modeloServidor.contadorVisitacaoCpu1/modeloServidor.contadorVisitacao;
			visitaMediaD2 = (double)modeloServidor.contadorVisitacaoCpu2/modeloServidor.contadorVisitacao;
			visitaMediaD3 = (double)modeloServidor.contadorVisitacaoDRapido/modeloServidor.contadorVisitacao;
			visitaMediaD4 = (double)modeloServidor.contadorVisitacaoDLento/modeloServidor.contadorVisitacao;
			
			tempoProcessador1 = (modeloServidor.totalTempoDistribuicaoProcessador1 / modeloServidor.qtdTempoDistribuicaoProcessador1);
			tempoProcessador2 = (modeloServidor.totalTempoDistribuicaoProcessador2 / modeloServidor.qtdTempoDistribuicaoProcessador2);
			tempoDiscoRapido = (modeloServidor.totalTempoDistribuicaoDiscoRapido / modeloServidor.qtdTempoDistribuicaoDiscoRapido);
			tempoDiscoLento = (modeloServidor.totalTempoDistribuicaoDiscoLento / modeloServidor.qtdTempoDistribuicaoDiscoLento);
			utilizacaoD1 = modeloServidor.totalTempoDistribuicaoProcessador1/tempoTotal;
			utilizacaoD2 = modeloServidor.totalTempoDistribuicaoProcessador2/tempoTotal;
			utilizacaoD3 = modeloServidor.totalTempoDistribuicaoDiscoRapido/tempoTotal;
			utilizacaoD4 = modeloServidor.totalTempoDistribuicaoDiscoLento/tempoTotal;
			
			
			questao3 += i+ ";"+utilizacaoD1 + ";"+utilizacaoD2 + ";"+utilizacaoD3 + ";"+utilizacaoD4 + ";\n";
			
			System.out.println("--------");
			// System.out.println(modeloServidor.tempoTotalResposta/ modeloServidor.contadorVisitacao);
			System.out.println("--------");

			// tempo médio de resposta do servidor de vídeos
			questao4 += i + ";" + (modeloServidor.tempoTotalResposta / modeloServidor.contadorVisitacao) + ";\n";

			// throughput médio do servidor de vídeos
			double thinktime = 0.2083; // (750/360)
			questao5 += i + ";" + (i / (modeloServidor.contadorVisitacao + thinktime)) + ";\n";

			// tamanho médio das filas dos dispositivos 750 / 360
			// questao6 += i + ";" + utilizacaoD1 + ";" + utilizacaoD2 + ";" + utilizacaoD3 + ";" + utilizacaoD4 + ";\n";
		}
		
		questao1 +=  nomeD1+visitaMediaD1+";\n";
		questao1 +=  nomeD2+visitaMediaD2+";\n";
		questao1 +=  nomeD3+visitaMediaD3+";\n";
		questao1 +=  nomeD4+visitaMediaD4+";\n";
		
		questao2 += nomeD1 + tempoProcessador1 + ";\n";
		questao2 += nomeD2 + tempoProcessador2 + ";\n";
		questao2 += nomeD3 + tempoDiscoRapido + ";\n";
		questao2 += nomeD4 + tempoDiscoLento + ";\n";

		EscreveArquivo.preparaArquivo(questao1, questao2, questao3, questao4, questao5, questao6, questao7a, questao7b, questao7c);
	}

}