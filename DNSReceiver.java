import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DNSReceiver {
    public static void main(String[] args) {
        int port = 53; // Puerto en el que escucha el servidor DNS
        try (DatagramSocket socket = new DatagramSocket(port)) {

            while (true) {
                byte[] response = new byte[1024];
                DatagramPacket packet = new DatagramPacket(response, response.length);

                // Espera a recibir un datagrama
                socket.receive(packet);

                // Procesa el datagrama recibido utilizando el código que has proporcionado
                Mensaje reqDNS = processDNSResponse(response, packet.getLength());

                // Agregar respuesta DNS
                byte[] dnsResponse = generateDNSResponse(response, packet.getLength(), reqDNS);
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                sendDNSResponse(dnsResponse, clientAddress, clientPort, socket);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Mensaje processDNSResponse(byte[] resp, int tam) {

        Mensaje reqDNS = new Mensaje(resp, tam);

        short QDCOUNT = 1;
        short ANCOUNT = 0;
        short NSCOUNT = 0;
        short ARCOUNT = 0;

        try {
            System.out.println("\n\nbytes recibidos: " + reqDNS.getRespuesta().getLength() + " bytes");
            reqDNS.imprimirBytes();

            System.out.println("\n");
            //Leer Encabezado
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(resp));
            System.out.println("\n\nDecodificar Respuesta");

            short id = dataInputStream.readShort();
            reqDNS.setId(id);
            System.out.println("ID: " + id); // ID


            short flags = dataInputStream.readByte();
            reqDNS.setFlags(flags);
            int QR = (flags & 0b10000000) >>> 7;
            reqDNS.setQR(QR);
            int opCode = ( flags & 0b01111000) >>> 3;
            reqDNS.setOpCode(opCode);
            int AA = ( flags & 0b00000100) >>> 2;
            reqDNS.setAA(AA);
            int TC = ( flags & 0b00000010) >>> 1;
            reqDNS.setTC(TC);
            int RD = flags & 0b00000001;
            reqDNS.setRD(RD);

            System.out.println("QR "+QR);
            System.out.println("Opcode "+opCode);
            System.out.println("AA "+AA);
            System.out.println("TC "+TC);
            System.out.println("RD "+RD);


            flags = dataInputStream.readByte();
            reqDNS.setFlags(flags);
            int RA = (flags & 0b10000000) >>> 7;
            reqDNS.setRA(RA);
            int Z = ( flags & 0b01110000) >>> 4;
            reqDNS.setZ(Z);
            int RCODE = flags & 0b00001111;
            reqDNS.setRCODE(RCODE);

            System.out.println("RA "+RA);
            System.out.println("Z "+ Z);
            System.out.println("RCODE " +RCODE);

            QDCOUNT = dataInputStream.readShort();
            reqDNS.setQDCOUNT(QDCOUNT);
            ANCOUNT = dataInputStream.readShort();
            reqDNS.setANCOUNT(ANCOUNT);
            NSCOUNT = dataInputStream.readShort();
            reqDNS.setNSCOUNT(NSCOUNT);
            ARCOUNT = dataInputStream.readShort();
            reqDNS.setARCOUNT(ARCOUNT);

            System.out.println("Questions: " + String.format("%s",QDCOUNT ));
            System.out.println("Answers RRs: " + String.format("%s", ANCOUNT));
            System.out.println("Authority RRs: " + String.format("%s", NSCOUNT));
            System.out.println("Additional RRs: " + String.format("%s", ARCOUNT));

            //PREGUNTA
            String QNAME = "";
            int recLen;
            while ((recLen = dataInputStream.readByte()) > 0) {
                byte[] record = new byte[recLen];
                for (int i = 0; i < recLen; i++) {
                    record[i] = dataInputStream.readByte();
                }
                //Se coloca el tamaño
                QNAME+= recLen;
                //Se coloca el nombre
                QNAME += new String(record, StandardCharsets.UTF_8);
            }
            QNAME+= recLen;
            reqDNS.setQNAME(QNAME);
            short QTYPE = dataInputStream.readShort();
            short QCLASS = dataInputStream.readShort();
            System.out.println("Record: " + QNAME);
            System.out.println("Record Type: " + String.format("%s", QTYPE));
            System.out.println("Class: " + String.format("%s", QCLASS));


            //RESPUESTA
            System.out.println("\n\nstart answer, authority, and additional sections\n");

            byte firstBytes = dataInputStream.readByte();
            int firstTwoBits = (firstBytes & 0b11000000) >>> 6;

            ByteArrayOutputStream label = new ByteArrayOutputStream();
            Map<String, String> domainToIp = new HashMap<>();

            for(int i = 0; i < ANCOUNT; i++) {
                if(firstTwoBits == 3) {
                    byte currentByte = dataInputStream.readByte();
                    boolean stop = false;
                    byte[] newArray = Arrays.copyOfRange(resp, currentByte, resp.length);
                    DataInputStream sectionDataInputStream = new DataInputStream(new ByteArrayInputStream(newArray));
                    ArrayList<Integer> RDATA = new ArrayList<>();
                    ArrayList<String> DOMAINS = new ArrayList<>();
                    while(!stop) {
                        byte nextByte = sectionDataInputStream.readByte();
                        if(nextByte != 0) {
                            byte[] currentLabel = new byte[nextByte];
                            for(int j = 0; j < nextByte; j++) {
                                currentLabel[j] = sectionDataInputStream.readByte();
                            }
                            label.write(currentLabel);
                        } else {
                            stop = true;
                            short TYPE = dataInputStream.readShort();
                            short CLASS = dataInputStream.readShort();
                            int TTL = dataInputStream.readInt();
                            int RDLENGTH = dataInputStream.readShort();
                            for(int s = 0; s < RDLENGTH; s++) {
                                int nx = dataInputStream.readByte() & 255;// and with 255 to
                                RDATA.add(nx);
                            }

                            System.out.println("Type: " + TYPE);
                            System.out.println("Class: " + CLASS);
                            System.out.println("Time to live: " + TTL);
                            System.out.println("Rd Length: " + RDLENGTH);
                        }

                        DOMAINS.add(label.toString(StandardCharsets.UTF_8));
                        label.reset();
                    }

                    StringBuilder ip = new StringBuilder();
                    StringBuilder domainSb = new StringBuilder();
                    for(Integer ipPart:RDATA) {
                        ip.append(ipPart).append(".");
                    }

                    for(String domainPart:DOMAINS) {
                        if(!domainPart.equals("")) {
                            domainSb.append(domainPart).append(".");
                        }
                    }
                    String domainFinal = domainSb.toString();
                    String ipFinal = ip.toString();
                    domainToIp.put(ipFinal.substring(0, ipFinal.length()-1), domainFinal.substring(0, domainFinal.length()-1));

                }else if(firstTwoBits == 0){
                    System.out.println("It's a label");
                }

                firstBytes = dataInputStream.readByte();
                firstTwoBits = (firstBytes & 0b11000000) >>> 6;
            }

            domainToIp.forEach((key, value) -> System.out.println(key + " : " + value));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reqDNS;
    }

    public static byte[] generateDNSResponse(byte[] msgRequest, int length, Mensaje reqDNS) throws Exception {
        //Res
        ByteArrayOutputStream respuesta = new ByteArrayOutputStream();
        DataOutputStream respuestaIn = new DataOutputStream(respuesta);
        //Request
        DataInputStream dataInRequest = new DataInputStream(new ByteArrayInputStream(msgRequest));

        //Encabezado de la respuesta
        respuestaIn.writeShort(reqDNS.getId());
        respuestaIn.writeShort((short) 0b1000000000000000);
        respuestaIn.writeShort(reqDNS.getQDCOUNT());
        respuestaIn.writeShort(0);
        respuestaIn.writeShort(0);
        //Banderas


        //QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT
        respuestaIn.writeShort(reqDNS.getQDCOUNT());
        respuestaIn.writeShort(1);
        respuestaIn.writeShort(0);
        respuestaIn.writeShort(0);

        //Pregunta
        escribirPregunta(respuestaIn,reqDNS.getQNAME());
        respuestaIn.writeByte(0); //Final de QNAME


        return respuesta.toByteArray();
    }

    public static void escribirPregunta(DataOutputStream dataOutputStream, String queryDomain) throws Exception {
        // Escribir el nombre del dominio de la pregunta
        String[] parteDominio = queryDomain.split("\\.");
        for (String dominio : parteDominio) {
            byte[] dominioBytes = dominio.getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeByte(dominioBytes.length);
            dataOutputStream.write(dominioBytes);
        }

        dataOutputStream.writeShort(1); //Tipo de registro A

        dataOutputStream.writeShort(1); //Clase va a ser IN
    }

    public static void sendDNSResponse(byte[] responseData, InetAddress clientAddress, int clientPort, DatagramSocket socket) {
        try {
            System.out.println("Enviando Respuesta");
            DatagramPacket packet = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
