import java.net.DatagramPacket;

public class Mensaje {
    
    private byte[] contenido;
    private DatagramPacket respuesta;
    //Encabezado
    short id = 0;
    short flags = 0;
    //RepresentarBanderas
    int QR = 0;
    int opCode = 0;
    int AA = 0;
    int TC = 0;
    int RD = 0;
    int RA = 0;
    int Z = 0;
    int RCODE = 0;
    //----------------------

    short QDCOUNT = 1;
    short ANCOUNT = 0;
    short NSCOUNT = 0;
    short ARCOUNT = 0;

    //Pregunta
    String QNAME;
    int longLabel;
    short QTYPE;
    short QCLASS;

    //Respuesta, Autoridad, Adicional

    //NAME

    //------------------------------
    short TYPE = 0;
    short CLASS = 0;
    int TTL = 0;
    int RDLENGTH = 0;

    public Mensaje(byte[] msg, int tam){
        this.contenido = msg;
        this.respuesta = new DatagramPacket(contenido, contenido.length);
    }

    public void imprimirBytes(){

    }
}
