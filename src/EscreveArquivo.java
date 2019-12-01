import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class EscreveArquivo{
	public static void preparaArquivo(String q1, String q2, String q3, String q4, String q5, String q6, String q7a, String q7b, String q7c){
    try{
      new FileWriter("relatorio.csv", false).close();
      try (
        FileWriter writer = new FileWriter("relatorio.csv");
        BufferedWriter bw = new BufferedWriter(writer)) {
          String question = "Questão ";
          bw.write(question+" 1;\n");
          bw.write("Dispositivo; Visitação média por req.; \n");
          bw.write(q1);
          bw.write("\n");
          
          bw.write(question+" 2;\n");
          bw.write("Dispositivo; Demanda média (ms); \n");
          bw.write(q2);
          bw.write("\n");
          
          bw.write(question+" 3;\n");
          bw.write("Nº Pool;Ui (cpu #1);Ui (cpu #2);Ui (disco rápido);Ui (disco lento);\n");
          bw.write(q3);
          bw.write("\n");
        
          bw.write(question+" 4;\n");
          bw.write("Nº Pool; Tempo médio servidor;\n");
          bw.write(q4);
          bw.write("\n");

          bw.write(question+" 5;\n");
          bw.write("Nº Pool; Throughput médio servidor;\n");
          bw.write(q5);
          bw.write("\n");

          bw.write(question+" 6;\n");
          bw.write("Nº Pool; Tam. Med. cpu #1; Tam. Med. cpu #2; Tam. Med. disco rápido; Tam. Med. disco lento;\n");
          bw.write(q6);
          bw.write("\n");

          bw.write(question+" 7a;\n");
          bw.write("Nº Pool; -;\n");
          bw.write(q7a);
          bw.write("\n");

          bw.write(question+" 7b;\n");
          bw.write("Nº Pool; -;\n");
          bw.write(q7b);
          bw.write("\n");

          bw.write(question+" 7c;\n");
          bw.write("Nº Pool; -;\n");
          bw.write(q7c);
          bw.write("\n");

      } catch (IOException ex) {
        System.err.format("IOException: %s%n", ex);
      }
    }catch(Exception e){}
  }
}
