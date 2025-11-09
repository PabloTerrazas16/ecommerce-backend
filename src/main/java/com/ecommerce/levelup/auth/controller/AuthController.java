package com.ecommerce.levelup.auth.controller;


import com.ecommerce.levelup.security.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/crear")
    public String crear(@RequestParam String user){
        return JwtUtil.crearToken(user);
    }
    @GetMapping("/leer")
    public String leer (@RequestParam String token){
        try{
            return "Usuario: "+JwtUtil.leerUsuario(token);
        }catch (Exception e){
            return "Token Invalido";
        }
    }
}
