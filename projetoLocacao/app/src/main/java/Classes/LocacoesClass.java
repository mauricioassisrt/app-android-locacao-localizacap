package Classes;

public class LocacoesClass {
    private String clienteid;
    private String locacao;
    private String vencimento;
    private String endereco;
    private String equipamentoid;
    private String lat;
    private String lng;
    private String valor;

    public LocacoesClass(){

    }

    public String getClienteid() {
        return clienteid;
    }

    public void setClienteid(String clienteid) {
        this.clienteid = clienteid;
    }

    public String getLocacao() {
        return locacao;
    }

    public void setLocacao(String locacao) {
        this.locacao = locacao;
    }

    public String getVencimento() {
        return vencimento;
    }

    public void setVencimento(String vencimento) {
        this.vencimento = vencimento;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEquipamentoid() {
        return equipamentoid;
    }

    public void setEquipamentoid(String equipamentoid) {
        this.equipamentoid = equipamentoid;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
