package gt.intecap.capacitacioncliente.Controllers;

import gt.intecap.capacitacioncliente.Models.ParticipanteModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import javax.swing.*;
import java.util.List;

@Controller
@RequestMapping("/participantes")// ruta del navegador
public class ParticipanteClienteController {

    private  final RestClient webClient;

    public ParticipanteClienteController(@Value("${api.backend.url:http://localhost:8080/api}") String baseUrl){
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
        model.addAttribute("participante", new ParticipanteModel());
        return "participantes/formulario";
    }

    //Recibe los datos del formulario de participates, los valida y los manda a guardar a la api
    @PostMapping("/guardar")
    public  String guardar(@Valid @ModelAttribute("participante") ParticipanteModel participante, BindingResult result, Model model){
        // Verifica si hay errores de validación si hay mala escritura en correos,  teléfonos
        if (result.hasErrors()){
            return  "participantes/formulario";
        }
        try {
            // Hace un POST a la API enviando los datos del nuevo participante en formato JSON
            webClient.post()
                    .uri("/participantes")
                    .body(participante)
                    .retrieve()
                    .toBodilessEntity();
            return "redirect:/participantes";//redirige el listado

        } catch (HttpClientErrorException.BadRequest ex){
            //extrae el mensaje de correo duplicado de la api
            String cuerpoJson = ex.getResponseBodyAsString();
            String mensajeLimpio = "El correo electrónico ya está registrado";

            //analiza el JSON de forma segura
            if (cuerpoJson != null && cuerpoJson.contains("message")) {
                if (cuerpoJson.contains("El correo electrónico ya está registrado.")) {
                    mensajeLimpio = "El correo electrónico ya está registrado.";
                } else if (cuerpoJson.contains("Este participante ya se encuentra inscrito")) {
                    mensajeLimpio = "Este participante ya se encuentra inscrito activamente en este curso."; }
                }

            //pasa el mensaje de error y devuelve el objeto parcicipante
            model.addAttribute("errorCorreo", mensajeLimpio);
            model.addAttribute("participante", participante);

            return "participantes/formulario";
        }

    }

    //elimina  un participante y lo oculta del sistema
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        // llama al metodo Delete de la  api  para desactiva al participante sin borrar el historial.
        webClient.delete()
                .uri("/participantes/" + id)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/participantes";
    }

}
