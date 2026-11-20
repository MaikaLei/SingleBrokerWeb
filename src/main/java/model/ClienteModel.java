package model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "cliente")
public class ClienteModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String telefone;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String email;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String cep;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 1024)
    private String rua;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String numero;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 1024)
    private String complemento;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 1024)
    private String bairro;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 1024)
    private String cidade;

    @Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String estado;

    private Boolean ativo = true;

    public ClienteModel() {
    }


    private java.time.LocalDate dataCadastro;
    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name="usuario_cadastro_id")
    private UsuarioModel usuarioCadastro;
    public java.time.LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(java.time.LocalDate v) { dataCadastro=v; }
    public UsuarioModel getUsuarioCadastro() { return usuarioCadastro; }
    public void setUsuarioCadastro(UsuarioModel v) { usuarioCadastro=v; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
