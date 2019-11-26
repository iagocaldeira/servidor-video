import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class Cliente extends Entity{

	// Variavel responsável por armazenar o início do tempo de resposta do cliente.
	public double inicioTempoResposta = 0.0;
	
	/**
	 * Método construtor do Cliente.
	 *
	 * Cria um novo cliente que deseja utilizar os serviços da lavanderia.
	 * A criação do cliente ocorre por meio da chamada
	 * do método construtor da super-classe, ou seja, da classe Entity.
	 * Adicionalmente, o atributo inicioTempoResposta é inicializado.
	 * 
	 * parâmetro owner: indica o modelo ao qual o cliente está associado.
	 *                  
	 * parâmetro name: indica o nome do cliente.
	 * 
	 * parâmetro showInTrace: é um flag que indica se o cliente criado deve ou não
	 *                        produzir saídas para um trace de saída da simulação.
	 */    
	public Cliente(Model owner, String name, boolean showInTrace) {
				
		super (owner, name, showInTrace);
		
		// O início do tempo de resposta do cliente é inicializado com o tempo corrente da simulação.
		TimeInstant tempoCorrente;
		tempoCorrente = owner.presentTime();
		inicioTempoResposta = tempoCorrente.getTimeAsDouble();
	}
}