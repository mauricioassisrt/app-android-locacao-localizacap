package Classes;

public class EquipamentoClass {

    private String nome;
    private String codigo;
    private String status;

    public EquipamentoClass(){

    }
    public EquipamentoClass(String nome, String codigo, String status) {
        this.nome = nome;
        this.codigo = codigo;
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
