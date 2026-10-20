package gt.intecap.capacitacioncliente.Controllers;

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
@RequestMapping("/participantes")// ruta del navegador
public class ParticipanteClienteController {

    private  final RestClient webClient;

    public ParticipanteClienteController(@Value("${api.backend.url") String baseUrl){
        this.webClient = RestClient.builder().baseUrl(baseUrl).build();
    }
    //Muestra la tabla visual con todos los alumnos registrados que sigan activos.
    @GetMapping
    public String listar(Model model){
        //hace un solicitud al backend la lista JSON de paticipantes acitvos
        List<ParticipanteModel> lista = webClient.get()
                .uri("/participantes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ParticipanteModel>>() {
                });
        //comparte la lista con la pantalla HML
        model.addAttribute("participantes", lista);
        return "participantes/lista";

    }

    //Muestra el formulario para registrar un nuevo participante.
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model){

        // Envía un objeto vacío de ParticipanteModel  para agregar al formulario
        model.addAttribute("/participante",new ParticipanteModel());
        return "participantes/formulario";
    }

    //Recibe los datos del formulario de participates, los valida y los manda a guardar a la api
    @PostMapping("/guardar")
    public  String guardar(@Valid @ModelAttribute("participante") ParticipanteModel participante, BindingResult result){
        // Verifica si hay errores de validación si hay mala escritura en correos,  teléfonos
        if (result.hasErrors()){
            return  "participantes/formulario";
        }
        // Hace un POST a la API enviando los datos del nuevo participante en formato JSON
        webClient.post()
                .uri("/participantes")
                .body(participante)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/participantes";
    }


    //elimina  un participante y lo oculta del sistema
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        // llama al metodo Delete de la  api  para desactiva al participante sin borrar el historial.
        webClient.delete()
                .uri("/participantes" + id)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/participantes";
    }


}
