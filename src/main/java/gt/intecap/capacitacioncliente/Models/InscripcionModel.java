package gt.intecap.capacitacioncliente.Models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionModel {

    private Long id;

    @NotNull(message = "Debe seleccionar un curso")
    private CursoModel curso;

    @NotNull(message = "Debe seleccionar un participante")
    private ParticipanteModel participante;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fechaIncripcion;

    private String estado;

}
