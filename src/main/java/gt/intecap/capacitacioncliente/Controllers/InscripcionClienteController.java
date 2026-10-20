package gt.intecap.capacitacioncliente.Controllers;


import gt.intecap.capacitacioncliente.Models.CursoModel;
import gt.intecap.capacitacioncliente.Models.InscripcionModel;
import gt.intecap.capacitacioncliente.Models.ParticipanteModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
@Controller
@RequestMapping("/inscripciones")//ruta del navegador
public class InscripcionClienteController {

    private  final RestClient webClient;

    public InscripcionClienteController(@Value("${api.backend.url") String baseUrl){
        this.webClient = RestClient.builder().baseUrl(baseUrl).build();
    }
    //Muestra todos los alumnos registrados que sigan activos.
    @GetMapping
    public String listar(Model model){
        //hace la peticion Get a la api y trae la lista en formato JSON de participantes activos
        List<InscripcionModel> lista = webClient.get()
                .uri("/inscripciones")
                .retrieve()
                .body(new ParameterizedTypeReference<List<InscripcionModel>>() {
                });

        model.addAttribute("inscripciones", lista);
        return "inscripciones/lista";
    }

    //Muestra la pantalla con el formulario para registrar un nuevo participante.
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model){
        //consume la API para  Traer todos los cursos activos disponibles
        List<CursoModel> cursos = webClient.get()
                .uri("/cursos")
                    .retrieve().body(new ParameterizedTypeReference<List<CursoModel>>() {
        });
        //trae todos los participantes habilitados
        List<ParticipanteModel> participantes = webClient.get()
                .uri("/participantes")
                .retrieve().body(new ParameterizedTypeReference<List<ParticipanteModel>>() {
                });
        //agrega  al model de Spring para cargarlo en los menús desplegables del HTML
        model.addAttribute("inscripcion", new InscripcionModel());
        model.addAttribute("listaCursos", cursos);
        model.addAttribute("listaParticipantes", participantes);

        return "inscripçiones/formulario";
    }

    //Procesa la matricula  los valida y los manda a guardar a la api
    @PostMapping("/guardar")
    public  String inscribir(@Valid @ModelAttribute("incripcion") InscripcionModel inscripcion, BindingResult result, Model model){
        // Verifica si hay campos vacios en el formulario cliente, recarga las listas y  muestra los errores
        if (result.hasErrors()){
            List<CursoModel> cursos = webClient.get()
                    .uri("/cursos")
                    .retrieve().body(new ParameterizedTypeReference<List<CursoModel>>() {
                    });

            List<ParticipanteModel> participantes = webClient.get()
                    .uri("/participantes")
                    .retrieve().body(new ParameterizedTypeReference<List<ParticipanteModel>>() {
                    });
            model.addAttribute("listaCursos", cursos);
            model.addAttribute("listaParticipantes", participantes);
            return  "inscripciones/formulario";
        }

        //Envia el JSON mediante post al backend, la api validara si hay duplicados y cupos disponibles
        webClient.post()
                .uri("/inscripciones")
                .body(inscripcion)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/inscripciones";
    }


    //cancela la matricula liberando un cupo del curso de forma inmediata.
    @GetMapping("/cancelar/{id}")
    public String eliminar(@PathVariable Long id){

        // hace una llamada de tipo put
        webClient.delete()
                .uri("/inscripciones/" + id + "/cancelar")
                .retrieve()
                .toBodilessEntity();
        return "redirect:/inscripciones";
    }



}
