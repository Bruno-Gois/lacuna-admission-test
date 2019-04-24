import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Cliente {

    Socket R2D2;
    DataInputStream dataInput;
    DataOutputStream dataOutput;

    Socket L314;
    DataInputStream dataInputRebel;
    DataOutputStream dataOutputRebel;
    int portaRebelde = 31959;
    byte[] teste;

    String token = "1457accc-251f-44e8-be52-9f2099738e00";

    Cliente(String endereco, int porta) {
        initCliente(endereco, porta);
    }

    private void initCliente(String endereco, int porta) {
        try {
            L314 = new Socket(endereco, portaRebelde);
            R2D2 = new Socket(endereco, porta);

            dataInput = new DataInputStream(R2D2.getInputStream());
            dataOutput = new DataOutputStream(R2D2.getOutputStream());
            dataOutput.flush();

            dataInputRebel = new DataInputStream(L314.getInputStream());
            dataOutputRebel = new DataOutputStream(L314.getOutputStream());
            dataOutputRebel.flush();

            escreverMensagem(token, dataOutput);         
            lerUTF(dataInput);
            
            escreverMensagem("tell me more", dataOutput);
            teste = ChaveDecodificacao(lerMensagemCript(dataInput));

            if(verificarPresencaCoordenada(teste)){
                System.out.println("Msg eh coordenada do imperio!");
                System.out.println(new String(teste, StandardCharsets.UTF_8));
            }
    
            else {
                System.out.println("Msg NAO eh coordenada");
                System.out.println(new String(teste, StandardCharsets.UTF_8));
            }

            // escreverMensagem("tell me more", dataOutput);
            // teste = ChaveDecodificacao(lerMensagemCript(dataInput));

            // if(verificarPresencaCoordenada(teste)){
            //     System.out.println("Msg eh coordenada do imperio!");
            //     System.out.println(new String(teste, StandardCharsets.UTF_8));
            // }
    
            // else {
            //     System.out.println("Msg NAO eh coordenada");
            //     System.out.println(new String(teste, StandardCharsets.UTF_8));
            // }

            
        } catch (Exception e) {
            System.err.println("erroInit: " + e.toString());
        }
    }
    
    private byte[] ChaveDecodificacao(byte[] msgCript) {
        byte[] vader = {86,97,100,101,114}; //Vader in ASCII
        int tamanhoMsg = msgCript.length;
        int tamanhoVader = vader.length;
        byte chave = -1;
        int verificadorDeChave = 0;

        int i = 0;
        int j = 0;
        int nroInteracoes = 0;

        testeLoop:
        while(j < tamanhoMsg && nroInteracoes < (tamanhoMsg - tamanhoVader) + 1) {
            chave = (byte) (vader[i] ^ msgCript[j]);

            while(i < tamanhoVader) {    
                if(chave == (vader[i] ^ msgCript[j])) {
                    verificadorDeChave++;
                }
                i++;
                j++;

                if(verificadorDeChave == 4) {//achou chave
                    break testeLoop;
                }
            }
            verificadorDeChave = 0;
            nroInteracoes++;
            i = 0;
            j = nroInteracoes;
        }

        if(verificadorDeChave == 4) { //chave foi encontrada
            return decodificarMensagem(msgCript, chave);
        }
        else {
            System.out.println("Chave nao encontrada!");
            return null;
        }
    }

    private boolean verificarPresencaCoordenada(byte[] msg) {
        String msgRecebida = new String(msg, StandardCharsets.UTF_8);

        Pattern p = Pattern.compile("x[0-9]+y[0-9]+");

        Matcher m = p.matcher(msgRecebida);

        return m.find();
    }

    private byte[] decodificarMensagem(byte[] msg, byte chave) {
        byte[] novaMsg = new byte[msg.length];

        for(int i = 0; i < msg.length; i++) {
            novaMsg[i] = (byte) (msg[i] ^ chave);
        }

        return novaMsg;
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
                    if (padraoMsgImperio == 0)
                        tamanhoMsgLida.add(buffer[i]);
                    if (padraoMsgImperio == 1)
                        msgLida.add(buffer[i]);
                    if (padraoMsgImperio == 2)
                        checkSumByte = buffer[i];
                    i++;
                }
                // de acordo com o padrao de msg do imperio, o loop so ira rodar 3 vezes,
                // capturando na primeira iteracao o tamanho, na segunda a msg em si 
                // e na terceira o checksum

                if (padraoMsgImperio > 1)
                    break;

                padraoMsgImperio++;
            }

        } catch (Exception e) {
            System.err.println("erroLeitura: " + e.toString());
        }

        if (analisarChecksum(msgLida, tamanhoMsgLida, checkSumByte)) {
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