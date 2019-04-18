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
    String token = "1457accc-251f-44e8-be52-9f2099738e00";

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
            
        } catch (Exception e) {
            System.err.println("erroInit: " + e.toString());
        }
    }
    
    private void ChaveDecodificacao(byte[] msgCript) {
        byte[] vader = {86,97,100,101,114}; //Vader in ASCII
        int tamanhoMsg = msgCript.length;
        int tamanhoVader = vader.length;
        byte chave = -1;
        int verificadorDeChave = 0;

        int i = 0;
        int j = 0;
        int nroInteracoes = 0;

        System.out.println(Arrays.toString(vader));

        testeLoop:
        while(j < tamanhoMsg && nroInteracoes < (tamanhoMsg - tamanhoVader) + 1) {
            chave = (byte) (vader[i] ^ msgCript[j]);

            while(i < tamanhoVader) {
                //System.out.println("i=" + i + " j=" + j);
                
                if(chave == (vader[i] ^ msgCript[j])) {
                    //System.out.println("IGUAIS " + chave + " " + (vader[i] ^ msgCript[j]));
                    verificadorDeChave++;
                }
                // else
                //     System.out.println("DIFERENTES " +chave + " " + (vader[i] ^ msgCript[j]));
                    i++;
                    j++;

                    if(verificadorDeChave == 4) {//achou chave
                        System.out.println("CHAVE = " + chave);
                        break testeLoop;
                    }
                }
            verificadorDeChave = 0;
            nroInteracoes++;
            i = 0;
            j = nroInteracoes;
        }

        if(verificadorDeChave == 4) { //chave foi encontrada
            decodificarMensagem(msgCript, chave);
        }
        else {
            System.out.println("Chave nao encontrada!");
        }
    }

    private void decodificarMensagem(byte[] msg, byte chave) {
        byte[] novaMsg = new byte[msg.length];

        for(int i = 0; i < msg.length; i++) {
            novaMsg[i] = (byte) (msg[i] ^ chave);
        }
        
        System.out.println("MSG CRIPTO");
        System.out.println(Arrays.toString(msg));
        System.out.println(new String(msg, StandardCharsets.UTF_8));

        System.out.println("MSG DECODIFICADA");
        System.out.println(Arrays.toString(novaMsg));
        System.out.println(new String(novaMsg, StandardCharsets.UTF_8));
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