import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Cliente {
    
    Socket L314;
    DataInputStream entrada;
    DataOutputStream saida;
    String mensagemCLIENTE = "";
    byte[] mensagemSERVIDOR;

    Cliente(String endereco, int porta) {
        initCliente(endereco, porta);        
    }

    private void initCliente(String endereco, int porta) {
        try {
            L314 = new Socket(endereco, porta);
            System.out.println("Conectado ao servidor " + endereco + ", na porta: " + porta);

            // ligando as conexoes de saida e de entrada
            entrada = new DataInputStream(L314.getInputStream());
            saida = new DataOutputStream(L314.getOutputStream());
            saida.flush();
            
            //escrevendo a mensagem para o servidor
            mensagemCLIENTE = "caff5e93-cae0-4b85-8466-0d2c8f8c5323";
            saida.writeBytes(mensagemCLIENTE);
            saida.flush();

             //lendo a resposta do servidor 
             System.out.println("Servidor>> " + entrada.read(mensagemSERVIDOR));


        }
        catch (Exception e) {
            System.err.println("erro: " + e.toString());
        }
    }
}