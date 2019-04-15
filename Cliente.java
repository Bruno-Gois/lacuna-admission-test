import java.net.Socket;
import java.util.Arrays;
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

            escreverMensagem(token, dataOutput);
            
            lerMensagemUTF(dataInput);
            
            escreverMensagem("tell me more", dataOutput);
            
            lerMensagemCript(dataInput);

        }
        catch (Exception e) {
            System.err.println("erroInit: " + e.toString());
        }
    }

    private void lerMensagemCript(DataInputStream dataInput) {
        try{
            int[] teste = new int[64];
            int iter = 0;

            for(int i = 0; i < teste.length; i++) {
                teste[i] = 1;
            }
            
            while(iter < 30) {
                teste[iter] = dataInput. read();
                iter++;
            }
            
            System.out.println(Arrays.toString(teste));
        }
        catch (Exception e) {
            System.err.println("erroEscrita: " + e.toString());
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

    private void lerMensagemUTF(DataInputStream dataInput) {
        try {
            System.out.println(dataInput.readUTF());
        } catch (Exception e) {
            System.err.println("erroLeituraUTF: " + e.toString());
        }     
    }
}