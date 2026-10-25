package gt.intecap.capacitacioncliente.Controllers;

import gt.intecap.capacitacioncliente.Models.CursoModel;
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
@RequestMapping("/cursos")// ruta del navegador: http://localhost:8081/cursos
public class CursoClienteController {

    //declaramos la variable que guarda el cliente http de Spring para conectarse a la API
    private final RestClient webClient;

    //contructor recibe la url de properties e inicializa el cliente de comunicacion
    public CursoClienteController(@Value("${api.backend.url:http://localhost:8080/api}") String baseUrl) {
        this.webClient = RestClient.builder().baseUrl(baseUrl).build();
    }
    @GetMapping("/")
    public String irAlInicio(){
        return "index";
    }

    //Muestra la pantalla principal con  la tabla de todos los cursos activos.
    @GetMapping
    public String listar(Model model){
        //hace la peticion Get a la api y trae la lista en formato JSON
        List<CursoModel> lista = webClient.get()
                .uri("/cursos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CursoModel>>() {
                });

        model.addAttribute("cursos", lista);//retorna la vista HTML
        return "cursos/lista";//retorna la vista HTML

    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {

        CursoModel curso = this.webClient.get()
                .uri("/cursos/{id}", id)
                .retrieve()
                .body(CursoModel.class);
        model.addAttribute("curso", curso);
        return "cursos/detalle";
    }

    //Muestra la pantalla con el formulario para registrar un curso nuevo.

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model){

        // Envía un objeto vacío de CursoModel  para agregar al formulario
        model.addAttribute("curso", new CursoModel());
        return "cursos/formulario"; // Retorna la vista HTML
    }

    //Recibe los datos del formulario de cursos, los valida y los manda a guardar a la api
    @PostMapping("/guardar")
    public  String guardar(@Valid @ModelAttribute("curso") CursoModel curso, BindingResult result){
        // Si el usuario dejó campos vacíos o inválidos
        if (result.hasErrors()){
            return  "cursos/formulario";//retorna la pantalla del formulario y muestra los msj de error
        }
        //si es ok envia el objeto en JSON a la api usanso  post
        webClient.post()
                .uri("/cursos")
                .body(curso)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/cursos";//listado general para ver el nuevo curso
    }

    //elimina  un curso por id.
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){

        // Envía una señal Delete a la api  para cambiar el estado de activo a false
        webClient.delete()
                .uri("/cursos" + id)
                .retrieve()
                .toBodilessEntity();
        return "redirect:/cursos";//el listado general de cursos
    }

}
