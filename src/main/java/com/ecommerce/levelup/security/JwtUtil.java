package com.ecommerce.levelup.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    public static String crearToken(String usuario){
        return Jwts.builder().setSubject(usuario).setExpiration(new Date(System.currentTimeMillis()+60000)).signWith(key).compact();
    }
    public static String leerUsuario(String token){
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
}
