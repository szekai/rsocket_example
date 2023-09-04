package com.szekai.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        out.print(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        EchoClient client = new EchoClient();
        client.startConnection("127.0.0.1", 7001);
        String setupFrame =
                ("\u0000" +"\u0000" + "\u004b" + "\u0000" +
                        "\u0000" + "\u0000" + "\u0000" + "\u0004" + "\u0000" + "\u0000" + "\u0001" + "\u0000" + "\u0000" + "\u0000" + "\u0000" + "\u004e" + "\u0020" + "\u0000" +  "\u0001" + "\u005f" +
                        "\u0090" + "\u0027" + "\u006d" + "\u0065" + "\u0073" + "\u0073" + "\u0061" + "\u0067" + "\u0065" + "\u002f" + "\u0078" + "\u002e" + "\u0072" + "\u0073" +  "\u006f" + "\u0063" +
                        "\u006b" + "\u0065" + "\u0074" + "\u002e" + "\u0063" + "\u006f" + "\u006d" + "\u0070" + "\u006f" + "\u0073" + "\u0069" + "\u0074" + "\u0065" + "\u002d" +  "\u006d" + "\u0065" +
                        "\u0074" + "\u0061" + "\u0064" + "\u0061" + "\u0074" + "\u0061" + "\u002e" + "\u0076" + "\u0030" + "\u0010" + "\u0061" + "\u0070" + "\u0070" + "\u006c" +  "\u0069" + "\u0063" +
                        "\u0061" + "\u0074" + "\u0069" + "\u006f" + "\u006e" + "\u002f" + "\u006a" + "\u0073" + "\u006f" + "\u006e" + "\u0000" + "\u0000" + "\u001b" + "\u0000" +  "\u0000" + "\u0000" +
                        "\u0001" + "\u0011" + "\u0000" + "\u0000" + "\u0000" + "\u0009" + "\u00fe" + "\u0000" + "\u0000" + "\u0005" + "\u0004" + "\u0065" + "\u0063" + "\u0068" +  "\u006f" + "\u0074" +
                        "\u0065" + "\u0073" + "\u0074" + "\u0020" + "\u0064" + "\u0061" + "\u0074" + "\u0061" );
//        String response = client.sendMessage("..K............N .._.'message/x.rsocket.composite-metadata.v0.application/json.................echotest data");
        String response = client.sendMessage(setupFrame);
        System.out.println(response);

//        byte[] setupFrame =
//         {0x00, 0x00, 0x4b, 0x00,
//         0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x4e, 0x20, 0x00, 0x01, 0x5f,
//         0x90, 0x27, 0x6d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x2f, 0x78, 0x2e, 0x72, 0x73, 0x6f, 0x63,
//         0x6b, 0x65, 0x74, 0x2e, 0x63, 0x6f, 0x6d, 0x70, 0x6f, 0x73, 0x69, 0x74, 0x65, 0x2d, 0x6d, 0x65,
//         0x74, 0x61, 0x64, 0x61, 0x74, 0x61, 0x2e, 0x76, 0x30, 0x10, 0x61, 0x70, 0x70, 0x6c, 0x69, 0x63,
//         0x61, 0x74, 0x69, 0x6f, 0x6e, 0x2f, 0x6a, 0x73, 0x6f, 0x6e, 0x00, 0x00, 0x1b, 0x00, 0x00, 0x00,
//         0x01, 0x11, 0x00, 0x00, 0x00, 0x09, 0xfe, 0x00, 0x00, 0x05, 0x04, 0x65, 0x63, 0x68, 0x6f, 0x74,
//         0x65, 0x73, 0x74, 0x20, 0x64, 0x61, 0x74, 0x61};


    }
}
