package br.com.viniciusfinancas.financas.enumPackage;

public enum ReceitaStatus {
    RECEBIDA("Recebida"),
    PENDENTE("Pendente"),
    AGENDADA("Agendada"),
    ATRASADA("Atrasada");

    private final String descricao;

    // Construtor para armazenar a descrição
    ReceitaStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método para buscar o Enum pela descrição
    public static ReceitaStatus valorDoDescricao(String descricao) {
        for (ReceitaStatus status : values()) {
            if (status.getDescricao().equalsIgnoreCase(descricao)) {
                return status;
            }
        }
        return null; // ou lançar uma exceção, dependendo da sua lógica
    }
}
// "RECEBIDA","PENDENTE","AGENDADA","ATRASADA"