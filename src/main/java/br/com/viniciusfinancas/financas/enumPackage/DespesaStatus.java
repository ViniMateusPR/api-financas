package br.com.viniciusfinancas.financas.enumPackage;

public enum DespesaStatus {
    PAGA("Paga"),
    PENDENTE("Pendente"),
    AGENDADA("Agendada"),
    ATRASADA("Atrasada");

    private final String descricao;

    // Construtor para armazenar a descrição
    DespesaStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Método para buscar o Enum pela descrição
    public static DespesaStatus valorDoDescricao(String descricao) {
        for (DespesaStatus status : values()) {
            if (status.getDescricao().equalsIgnoreCase(descricao)) {
                return status;
            }
        }
        return null; // ou lançar uma exceção, dependendo da sua lógica
    }
}
