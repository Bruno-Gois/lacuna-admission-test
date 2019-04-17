import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            
            lerUTF(dataInput);

            escreverMensagem("tell me more", dataOutput);

            ChaveDecodificacao(lerMensagemCript(dataInput));
            
            // escreverMensagem("stop", dataOutput);
        } catch (Exception e) {
            System.err.println("erroInit: " + e.toString());
        }
    }
    
    private void ChaveDecodificacao(byte[] msgCript) {
        byte[] vader = {86,97,100,101,114}; //Vader in ASCII
        int chave;
        int chaveTemp;
        int nroInteracoes = 0;

        // for(int i = 0; i < (msgCript.length-vader.length); i++) {
        //     while() {
        //         chave = vader[i] ^ msgCript[i];
        //         chaveTemp = vader[i++] ^ msgCript[i++];
        //         System.out.println("CHAVE " + chave);
        //         System.out.println("TEMP " + chaveTemp);

        //     }
        // }
    }


    private void lerUTF(DataInputStream dataInput) {
        byte[] byteMsg = lerMensagemCript(dataInput);

        System.out.println(new String(byteMsg, StandardCharsets.UTF_8));
    }

    private byte[] lerMensagemCript(DataInputStream dataInput) {
        int len;
        byte[] buffer = new byte[1024];
        int padraoMsgImperio = 0;

        ArrayList<Byte> tamanhoMsgLida = new ArrayList<>();
        ArrayList<Byte> msgLida = new ArrayList<>();
        byte checkSumByte = -1;
        try {
            while ((len = dataInput.read(buffer)) > 0) {

                int i = 0;
                while (i < len) {
                    // System.out.print(buffer[i] + " ");
                    if (padraoMsgImperio == 0)
                        tamanhoMsgLida.add(buffer[i]);
                    if (padraoMsgImperio == 1)
                        msgLida.add(buffer[i]);
                    if (padraoMsgImperio == 2)
                        checkSumByte = buffer[i];

                    i++;
                }
                // de acordo com o padrao de msg do imperio, o loop so ira rodar 3 vezes,
                // capturando na primeira iteracao
                // o tamanho, na segunda a msg em si, e na terceira o checksum
                if (padraoMsgImperio > 1)
                    break;

                padraoMsgImperio++;
            }

        } catch (Exception e) {
            System.err.println("erroLeitura: " + e.toString());
        }

        if (analisarChecksum(msgLida, tamanhoMsgLida, checkSumByte)) {
            System.out.println(msgLida);
            return converterParaPrimitivo(msgLida.toArray(new Byte[0]));
        } else {
            System.out.println("Erro no checksum!");
            return null;
        }
    }

    private void escreverMensagem(String msg, DataOutputStream dataOutput) {
        try {
            dataOutput.write(msg.getBytes());
            dataOutput.flush();
        } catch (Exception e) {
            System.err.println("erroEscrita: " + e.toString());
        }
    }

    private byte[] converterParaPrimitivo(Byte [] arrayRecebido) {
        byte[] bytes = new byte[arrayRecebido.length];

        int j=0;
        // Unboxing Byte values. (Byte[] to byte[])
        for(Byte by: arrayRecebido)
            bytes[j++] = by.byteValue();

        return bytes;
    }

    private boolean analisarChecksum(ArrayList<Byte> msgLida, ArrayList<Byte> tamanhoMsg, byte checksum) {

        Integer somatorio = 0;

        for (Byte i : msgLida) {
            somatorio = somatorio + i;
        }
        for (Byte i : tamanhoMsg) {
            somatorio = somatorio + i;
        }

        if (somatorio.byteValue() == checksum) {
            return true;
        }
        return false;
    }
}