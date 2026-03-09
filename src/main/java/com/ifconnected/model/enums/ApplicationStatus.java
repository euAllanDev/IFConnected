package com.ifconnected.model.enums;

public enum ApplicationStatus {
    PENDING,    // Enviado, empresa ainda não viu
    REVIEWED,   // Empresa abriu o perfil
    INTERVIEW,  // Chamado para entrevista
    OFFER,      // Proposta feita
    REJECTED,   // Não passou
    WITHDRAWN   // Aluno desistiu da vaga
}
