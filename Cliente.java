import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Cliente {
    
    Socket L314;
    DataInputStream dataInput;
    DataOutputStream dataOutput;
    String token = "caff5e93-cae0-4b85-8466-0d2c8f8c5323";

    Cliente(String endereco, int porta) {
        initCliente(endereco, porta);        
    }

    private void initCliente(String endereco, int porta) {
        try {
            L314 = new Socket(endereco, porta);
            System.out.println("Conectado ao servidor " + endereco + ", na porta: " + porta);

            dataInput = new DataInputStream(L314.getInputStream());
            dataOutput = new DataOutputStream(L314.getOutputStream());
            dataOutput.flush();
            
            //escrevendo a mensagem para o servidor
            escreverMensagem(token, dataOutput);
            
            //lendo msg do servidor
            lerMensagem(dataInput);
        }
        catch (Exception e) {
            System.err.println("erroInit: " + e.toString());
        }
    }

    private void escreverMensagem(String msg, DataOutputStream dataOutput) {
        try {
            dataOutput.write(msg.getBytes());
            dataOutput.flush();
        }
        catch (Exception e) {
            System.err.println("erroEscrita: " + e.toString());
        }
    }

    private void lerMensagem(DataInputStream dataInput) {
        try {
            System.out.println(dataInput.readUTF());
        } catch (Exception e) {
            System.err.println("erroLeitura: " + e.toString());
        }
            
    }
}