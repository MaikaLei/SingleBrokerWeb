package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente_pj")
public class ClientePjModel extends ClienteModel {

    private String razaoSocial;

    @Column(unique = true, length = 128)
    @jakarta.persistence.Convert(converter = util.ConversorDadoPessoal.class)
    private String cnpj;

    @jakarta.persistence.Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 256)
    private String inscricaoEstadual;

    @jakarta.persistence.Convert(converter = util.ConversorDadoPessoal.class)
    @Column(length = 512)
    private String nomeResponsavel;

    public ClientePjModel() {
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }
}
