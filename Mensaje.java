import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class Mensaje {

    private byte[] contenido;
    private DatagramPacket respuesta;
    //Encabezado
    private short id = 0;
    private short flags = 0;
    //RepresentarBanderas
    private int QR = 0;
    private int opCode = 0;
    private int AA = 0;
    private int TC = 0;
    private int RD = 0;
    private int RA = 0;
    private int Z = 0;
    private int RCODE = 0;
    //----------------------

    private short QDCOUNT = 1;
    private short ANCOUNT = 0;
    private short NSCOUNT = 0;

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    public DatagramPacket getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(DatagramPacket respuesta) {
        this.respuesta = respuesta;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public int getQR() {
        return QR;
    }

    public void setQR(int QR) {
        this.QR = QR;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

    public int getAA() {
        return AA;
    }

    public void setAA(int AA) {
        this.AA = AA;
    }

    public int getTC() {
        return TC;
    }

    public void setTC(int TC) {
        this.TC = TC;
    }

    public int getRD() {
        return RD;
    }

    public void setRD(int RD) {
        this.RD = RD;
    }

    public int getRA() {
        return RA;
    }

    public void setRA(int RA) {
        this.RA = RA;
    }

    public int getZ() {
        return Z;
    }

    public void setZ(int z) {
        Z = z;
    }

    public int getRCODE() {
        return RCODE;
    }

    public void setRCODE(int RCODE) {
        this.RCODE = RCODE;
    }

    public short getQDCOUNT() {
        return QDCOUNT;
    }

    public void setQDCOUNT(short QDCOUNT) {
        this.QDCOUNT = QDCOUNT;
    }

    public short getANCOUNT() {
        return ANCOUNT;
    }

    public void setANCOUNT(short ANCOUNT) {
        this.ANCOUNT = ANCOUNT;
    }

    public short getNSCOUNT() {
        return NSCOUNT;
    }

    public void setNSCOUNT(short NSCOUNT) {
        this.NSCOUNT = NSCOUNT;
    }

    public short getARCOUNT() {
        return ARCOUNT;
    }

    public void setARCOUNT(short ARCOUNT) {
        this.ARCOUNT = ARCOUNT;
    }

    public String getQNAME() {
        return QNAME;
    }

    public void setQNAME(String QNAME) {
        this.QNAME = QNAME;
    }

    public int getLongEtiqueta() {
        return longEtiqueta;
    }

    public void setLongEtiqueta(int longEtiqueta) {
        this.longEtiqueta = longEtiqueta;
    }

    public short getQTYPE() {
        return QTYPE;
    }

    public void setQTYPE(short QTYPE) {
        this.QTYPE = QTYPE;
    }

    public short getQCLASS() {
        return QCLASS;
    }

    public void setQCLASS(short QCLASS) {
        this.QCLASS = QCLASS;
    }

    public Map<String, String> getDominioIp() {
        return dominioIp;
    }

    public void setDominioIp(Map<String, String> dominioIp) {
        this.dominioIp = dominioIp;
    }

    public short getTYPE() {
        return TYPE;
    }

    public void setTYPE(short TYPE) {
        this.TYPE = TYPE;
    }

    public short getCLASS() {
        return CLASS;
    }

    public void setCLASS(short CLASS) {
        this.CLASS = CLASS;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public int getRDLENGTH() {
        return RDLENGTH;
    }

    public void setRDLENGTH(int RDLENGTH) {
        this.RDLENGTH = RDLENGTH;
    }

    private short ARCOUNT = 0;

    //Pregunta
    private String QNAME;
    private int longEtiqueta;
    private short QTYPE;
    private short QCLASS;

    //Respuesta, Autoridad, Adicional

    //NAME
    private Map<String, String> dominioIp = new HashMap<>();
    //------------------------------
    private short TYPE = 0;
    private short CLASS = 0;
    private int TTL = 0;
    private int RDLENGTH = 0;

    public Mensaje(byte[] msg, int tam){
        this.contenido = msg;
        this.respuesta = new DatagramPacket(contenido, contenido.length);
    }

    public void imprimirBytes(){
        System.out.println("\n\nReceived: " + respuesta.getLength() + " bytes");
        for (int i = 0; i < respuesta.getLength(); i++) {
            System.out.print(String.format("%s", contenido[i]) + " ");
        }
        System.out.println("\n");
    }



}
