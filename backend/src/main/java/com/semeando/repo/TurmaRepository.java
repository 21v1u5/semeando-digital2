package com.semeando.repo;

import com.semeando.model.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByProfessorOrderByNome(Usuario professor);

    /**
     * Carrega a turma com trava de escrita (RN02). Serializa efetivações
     * concorrentes na mesma turma, evitando que o limite de vagas seja estourado
     * por uma condição de corrida (contar vagas → inserir sem lock).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    Optional<Turma> lockById(@Param("id") Long id);
}
