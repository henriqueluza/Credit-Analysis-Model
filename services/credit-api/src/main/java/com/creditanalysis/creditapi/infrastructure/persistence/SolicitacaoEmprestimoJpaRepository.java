package com.creditanalysis.creditapi.infrastructure.persistence;

import com.creditanalysis.creditapi.domain.model.StatusAnalise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoEmprestimoJpaRepository extends JpaRepository<SolicitacaoEmprestimoJpaEntity, Long> {

    long countByResultadoStatus(StatusAnalise status);
}
