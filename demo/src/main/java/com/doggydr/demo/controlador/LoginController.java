package com.doggydr.demo.controlador;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doggydr.demo.entidad.Client;
import com.doggydr.demo.entidad.LoginRequest;
import com.doggydr.demo.entidad.UserEntity;
import com.doggydr.demo.servicio.ClientService;

import com.doggydr.demo.entidad.Vet;
import com.doggydr.demo.repositorio.UserRepository;
import com.doggydr.demo.security.JWTGenerator;
import com.doggydr.demo.servicio.VetService;
import com.doggydr.demo.DTOs.AdminDTO;
import com.doggydr.demo.DTOs.AdminMapper;
import com.doggydr.demo.DTOs.VetDTO;
import com.doggydr.demo.DTOs.VetMapper;
import com.doggydr.demo.entidad.Admin;
import com.doggydr.demo.servicio.AdminService;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:4200")
public class LoginController {

    @Autowired
    ClientService clientService;

    @Autowired
    VetService vetService;

    @Autowired
    AdminService adminService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTGenerator jwtGenerator;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepo;

    @GetMapping("/client")
    public String login(Model model) {
        return "clientLogin";
    }

    @PostMapping("/client")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login endpoint reached");
        /*
         * System.out.println("\n\n\nDocumento:"+ document);
         * 
         * // Buscar cliente en la base de datos por su documento
         * Client client = clientService.SearchByDocument(document);
         * 
         * // Si el cliente se encuentra, devolver información del cliente en JSON
         * if (client != null) {
         * return ResponseEntity.ok(client);
         * } else {
         * // Si no se encuentra, devolver un error con un código de estado 404
         * return
         * ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
         * }
         * 
         */
        Long document = loginRequest.getDocument();
        System.out.println("\n\nDocumento:" + document);

        Client client = clientService.SearchByDocument(document);
        if (client == null) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        // Log para mostrar la contraseña en el cliente y el hash almacenado
        System.out.println("Contraseña del cliente para comparación: " + client.getUsername());

        Optional<UserEntity> user = userRepo.findByDocument(document);

        System.out.println("Hash en base de datos: " + user.get().getPassword());

        if (passwordEncoder.matches("123", user.get().getPassword())) {
            String token = jwtGenerator.generateToken(
                    new UsernamePasswordAuthenticationToken(client.getUsername(), null));
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Credenciales incorrectas", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/vet")
    public String vetlogin(Model model) {
        return "vetLogin";
    }

    @PostMapping("/vet")
    public ResponseEntity vetLogin(@RequestBody LoginRequest loginRequest) {
        Vet vet = vetService.findByUserName(loginRequest.getUsername());

        if (vet == null) {
            System.out.println("\n\nVet username no encontrado: " + loginRequest.getUsername());

            return new ResponseEntity<>("Veterinario no encontrado", HttpStatus.NOT_FOUND);
        }
        System.out.println("\n\nVet username: " + vet.getUserName());

        Optional<UserEntity> user = userRepo.findByUsername(vet.getUserName());
        System.out.println("Vet user: " + user.get().getUsername());

        if (passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(vet.getMail(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            // Crear un objeto con el token y el nombre del veterinario
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", vet.getId());
            System.out.println("\nid: " + vet.getId());
            response.put("name", vet.getName());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Contraseña incorrecta", HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/admin")
    public String adminlogin(Model model) {
        return "adminLogin";
    }

    @PostMapping("/admin")
    public ResponseEntity adminLogin(@RequestBody LoginRequest loginRequest) {
        Admin admin = adminService.findByUsername(loginRequest.getUsername());

        if (admin == null) {
            return new ResponseEntity<>("Admin no encontrado", HttpStatus.NOT_FOUND);
        }

        // Log para verificación
        System.out.println("Admin username: " + admin.getUsername());
        System.out.println("Hash en base de datos: " + admin.getUser().getPassword());

        // Verificar la contraseña proporcionada
        if (passwordEncoder.matches(loginRequest.getPassword(), admin.getUser().getPassword())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(admin.getUsername(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Contraseña incorrecta", HttpStatus.UNAUTHORIZED);
        }
    }

}
