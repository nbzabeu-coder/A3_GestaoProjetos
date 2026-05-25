// Package: pacote dedicado aos relatórios (separa essa responsabilidade do model)
package com.gestaoprojetos.relatorio;

// Interface Relatorio: o "contrato" que todo relatório do sistema deve cumprir.
// Cada tipo de relatório (projeto, equipe, colaborador) implementa esses 2 métodos
// do seu próprio jeito — isso é POLIMORFISMO: tratamos todos como "Relatorio",
// mas cada um se monta diferente.
public interface Relatorio {

    // Monta o conteúdo do relatório como texto e devolve
    String gerar();

    // Exporta o relatório pra um arquivo no formato indicado (ex: "txt")
    void exportar(String formato);
}
