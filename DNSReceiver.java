import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DNSReceiver {
    public static void main(String[] args) {
        try {
            int port = 53; // Puerto en el que escucha el servidor DNS
            try (DatagramSocket socket = new DatagramSocket(port)) {

                while (true) {
                    byte[] response = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(response, response.length);

                    // Espera a recibir un datagrama
                    socket.receive(packet);

                    // Procesa el datagrama recibido utilizando el código que has proporcionado
                    processDNSResponse(response, packet.getLength());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processDNSResponse(byte[] response, int length) {

        short QDCOUNT = 1;
        short ANCOUNT = 0;
        short NSCOUNT = 0;
        short ARCOUNT = 0;

        try {
            DatagramPacket packet = new DatagramPacket(response, length);

            System.out.println("\n\nReceived: " + packet.getLength() + " bytes");
            for (int i = 0; i < packet.getLength(); i++) {
                System.out.print(String.format("%s", response[i]) + " ");
            }
            System.out.println("\n");

            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(response));
            System.out.println("\n\nStart response decode");
            System.out.println("Transaction ID: " + dataInputStream.readShort()); // ID
            short flags = dataInputStream.readByte();
            int QR = (flags & 0b10000000) >>> 7;
            int opCode = ( flags & 0b01111000) >>> 3;
            int AA = ( flags & 0b00000100) >>> 2;
            int TC = ( flags & 0b00000010) >>> 1;
            int RD = flags & 0b00000001;
            System.out.println("QR "+QR);
            System.out.println("Opcode "+opCode);
            System.out.println("AA "+AA);
            System.out.println("TC "+TC);
            System.out.println("RD "+RD);
            flags = dataInputStream.readByte();
            int RA = (flags & 0b10000000) >>> 7;
            int Z = ( flags & 0b01110000) >>> 4;
            int RCODE = flags & 0b00001111;
            System.out.println("RA "+RA);
            System.out.println("Z "+ Z);
            System.out.println("RCODE " +RCODE);

            QDCOUNT = dataInputStream.readShort();
            ANCOUNT = dataInputStream.readShort();
            NSCOUNT = dataInputStream.readShort();
            ARCOUNT = dataInputStream.readShort();

            System.out.println("Questions: " + String.format("%s",QDCOUNT ));
            System.out.println("Answers RRs: " + String.format("%s", ANCOUNT));
            System.out.println("Authority RRs: " + String.format("%s", NSCOUNT));
            System.out.println("Additional RRs: " + String.format("%s", ARCOUNT));

            String QNAME = "";
            int recLen;
            while ((recLen = dataInputStream.readByte()) > 0) {
                byte[] record = new byte[recLen];
                for (int i = 0; i < recLen; i++) {
                    record[i] = dataInputStream.readByte();
                }
                QNAME = new String(record, StandardCharsets.UTF_8);
            }
            short QTYPE = dataInputStream.readShort();
            short QCLASS = dataInputStream.readShort();
            System.out.println("Record: " + QNAME);
            System.out.println("Record Type: " + String.format("%s", QTYPE));
            System.out.println("Class: " + String.format("%s", QCLASS));

            System.out.println("\n\nstart answer, authority, and additional sections\n");

            byte firstBytes = dataInputStream.readByte();
            int firstTwoBits = (firstBytes & 0b11000000) >>> 6;

            ByteArrayOutputStream label = new ByteArrayOutputStream();
            Map<String, String> domainToIp = new HashMap<>();

            for(int i = 0; i < ANCOUNT; i++) {
                if(firstTwoBits == 3) {
                    byte currentByte = dataInputStream.readByte();
                    boolean stop = false;
                    byte[] newArray = Arrays.copyOfRange(response, currentByte, response.length);
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
    }
}