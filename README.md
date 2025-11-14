# E-Commerce Backend - Spring Boot API

## 📋 Descripción

Backend completo de e-commerce desarrollado con **Spring Boot 3.2.0** y **Java 17**. Implementa autenticación JWT, gestión de productos, categorías, usuarios y pagos con arquitectura de capas (Controller - Service - Repository).

---

## Tecnologías Utilizadas

- **Java**: 17 (Eclipse Adoptium)
- **Spring Boot**: 3.2.0
- **Spring Security**: Autenticación y autorización con JWT
- **Spring Data JPA**: Persistencia de datos
- **MySQL**: Base de datos relacional (vía Laragon)
- **Maven**: Gestión de dependencias
- **Lombok**: Reducción de código boilerplate
- **JJWT**: Generación y validación de tokens JWT (v0.12.3)

---

## Estructura del Proyecto

```
ecommerce-backend/
├── src/main/java/com/ecommerce/levelup/
│   ├── EcommerceBackendApplication.java    # Clase principal
│   │
│   ├── auth/                                # Módulo de Autenticación
│   │   ├── controller/
│   │   │   ├── AuthController.java          # Login, Register, Refresh Token
│   │   │   └── DebugController.java         # Endpoints de debug (temporal)
│   │   ├── dto/
│   │   │   ├── LoginRequest.java            # DTO para login
│   │   │   ├── LoginResponse.java           # DTO respuesta con JWT y roles
│   │   │   └── RegisterRequest.java         # DTO para registro
│   │   └── service/
│   │       ├── AuthService.java             # Lógica de autenticación
│   │       └── CustomUserDetailsService.java # Carga de usuarios para Spring Security
│   │
│   ├── config/                              # Configuraciones Globales
│   │   ├── CorsConfig.java                  # Configuración CORS para React
│   │   ├── DataInitializer.java             # Datos iniciales (roles, admin user)
│   │   └── GlobalExceptionHandler.java      # Manejo centralizado de excepciones
│   │
│   ├── security/                            # Módulo de Seguridad
│   │   ├── JwtUtil.java                     # Utilidad para JWT (generar, validar)
│   │   ├── JwtFilter.java                   # Filtro para interceptar y validar JWT
│   │   └── SecurityConfig.java              # Configuración de Spring Security
│   │
│   ├── user/                                # Módulo de Usuarios
│   │   ├── controller/
│   │   │   └── UserController.java          # CRUD de usuarios
│   │   ├── dto/
│   │   │   └── UserDTO.java                 # DTO de usuario
│   │   ├── model/
│   │   │   ├── User.java                    # Entidad Usuario
│   │   │   └── Role.java                    # Entidad Rol
│   │   ├── repository/
│   │   │   ├── UserRepository.java          # Acceso a datos de usuarios
│   │   │   └── RoleRepository.java          # Acceso a datos de roles
│   │   └── service/
│   │       └── UserService.java             # Lógica de negocio de usuarios
│   │
│   ├── product/                             # Módulo de Productos
│   │   ├── controller/
│   │   │   ├── ProductController.java       # CRUD de productos
│   │   │   └── CategoryController.java      # CRUD de categorías
│   │   ├── dto/
│   │   │   ├── ProductDTO.java              # DTO de producto
│   │   │   └── CategoryDTO.java             # DTO de categoría
│   │   ├── model/
│   │   │   ├── Product.java                 # Entidad Producto
│   │   │   └── Category.java                # Entidad Categoría
│   │   ├── repository/
│   │   │   ├── ProductRepository.java       # Acceso a datos de productos
│   │   │   └── CategoryRepository.java      # Acceso a datos de categorías
│   │   └── service/
│   │       ├── ProductService.java          # Lógica de productos + SKU auto
│   │       └── CategoryService.java         # Lógica de categorías + código auto
│   │
│   └── payment/                             # Módulo de Pagos
│       ├── controller/
│       │   └── PaymentController.java       # Procesar pagos
│       ├── dto/
│       │   ├── PaymentDTO.java              # DTO de pago
│       │   └── ProcessPaymentRequest.java   # DTO solicitud de pago
│       ├── model/
│       │   └── Payment.java                 # Entidad Pago
│       ├── repository/
│       │   └── PaymentRepository.java       # Acceso a datos de pagos
│       └── service/
│           └── PaymentService.java          # Lógica de pagos
│
└── src/main/resources/
    └── application.properties               # Configuración de la aplicación
```

---

## 🔐 Módulo de Autenticación (auth)

### **JWT (JSON Web Token)**

El sistema utiliza tokens JWT para autenticar usuarios sin mantener sesiones en el servidor.

#### **Tipos de Tokens**

1. **TOKEN USER**: Para autenticación de usuarios (login, acceso a recursos)
2. **TOKEN PAYMENT**: Para autorizar transacciones de pago

#### **Flujo de Autenticación**

```
1. Usuario → POST /autenticacion/login (username, password)
2. AuthService valida credenciales
3. JwtUtil genera token JWT con username y roles
4. Cliente recibe: { token, type: "Bearer", username, email, roles: [...] }
5. Cliente incluye token en headers: Authorization: Bearer <token>
6. JwtFilter intercepta requests y valida token
7. Si válido, extrae usuario y roles → permite acceso
```

#### **Endpoints de Autenticación**

| Método | Endpoint                  | Descripción                   | Auth |
| ------ | ------------------------- | ----------------------------- | ---- |
| POST   | `/autenticacion/register` | Registrar nuevo usuario       | ❌   |
| POST   | `/autenticacion/login`    | Iniciar sesión (devuelve JWT) | ❌   |
| POST   | `/autenticacion/refresh`  | Refrescar token expirado      | ✅   |

#### **Ejemplo de Login**

**Request:**

```json
POST /autenticacion/login
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@ecommerce.com",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

#### **Componentes Clave**

- **JwtUtil.java**:

  - `generateToken(username)`: Crea JWT con expiración de 24h
  - `validateToken(token)`: Verifica firma y expiración
  - `getUsernameFromToken(token)`: Extrae usuario del payload
  - `getTokenType(token)`: Identifica tipo de token (USER/PAYMENT)

- **JwtFilter.java**:

  - Intercepta TODOS los requests
  - Extrae token del header `Authorization`
  - Valida token y carga usuario en `SecurityContext`

- **AuthService.java**:
  - `register()`: Crea usuario nuevo con rol `ROLE_USER`
  - `login()`: Autentica y devuelve JWT + roles
  - Usa `@Transactional` para manejar lazy loading de roles

---

## 🔒 Módulo de Seguridad (security)

### **Spring Security + JWT**

Configuración que combina Spring Security con autenticación JWT stateless.

#### **SecurityConfig.java**

Define qué rutas son públicas y cuáles requieren autenticación/roles:

```java
// Rutas públicas (sin token)
.requestMatchers("/autenticacion/**").permitAll()
.requestMatchers("/debug/**").permitAll()
.requestMatchers("/productos").permitAll()  // GET público
.requestMatchers("/categorias").permitAll() // GET público

// Rutas autenticadas (requieren token válido)
.requestMatchers("/api/users/**").authenticated()
.requestMatchers("/api/payments/**").authenticated()

// Rutas de admin (requieren ROLE_ADMIN)
.requestMatchers("/api/admin/**").hasRole("ADMIN")

// Todo lo demás requiere autenticación
.anyRequest().authenticated()
```

#### **Roles del Sistema**

| Rol              | Descripción      | Permisos                                 |
| ---------------- | ---------------- | ---------------------------------------- |
| `ROLE_USER`      | Usuario estándar | Ver productos, comprar, gestionar perfil |
| `ROLE_ADMIN`     | Administrador    | CRUD productos/categorías, ver pagos     |
| `ROLE_MODERATOR` | Moderador        | Revisar contenido, gestionar usuarios    |

#### **Anotaciones de Seguridad**

```java
@PreAuthorize("hasRole('ADMIN')")  // Solo admin
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // Admin o Moderator
```

#### **Filtro de Autenticación**

1. **JwtFilter** se ejecuta ANTES de cualquier controller
2. Extrae token: `Authorization: Bearer <token>`
3. Valida token con `JwtUtil`
4. Si válido, carga `UserDetails` y establece `Authentication`
5. Spring Security permite/deniega acceso según roles

---

## Módulo de Productos (product)

### **Entidades**

#### **Product.java**

```json
{
  "id": 1,
  "code": "JM001",
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "category": "Juegos de Mesa",
  "image": "https://...",
  "description": "...",
  "stock": 10,
  "featured": false,
  "active": true
}
```

#### **Category.java**

```json
{
  "id": 1,
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "...",
  "active": true
}
```

### **Generación Automática de SKU**

El sistema genera códigos únicos automáticamente:

1. **Categoría se crea con código**: `"code": "JM"` (Juegos de Mesa)
2. **Producto se crea sin code**: Se genera automáticamente
3. **Algoritmo**: Busca último número en esa categoría + 1
4. **Formato**: `CODIGO_CATEGORIA + 3 dígitos` → `JM001`, `JM002`, etc.

**Ejemplo:**

```
Categoría: Juegos de Mesa (code: "JM")
Productos: JM001, JM002, JM003...

Categoría: Accesorios (code: "AC")
Productos: AC001, AC002, AC003...
```

#### **ProductService.java - Lógica Principal**

- `createProduct()`: Crea producto y genera SKU automáticamente
- `generateSku(category)`: Busca último SKU de la categoría y genera siguiente
- `updateProduct()`: Actualiza producto (no regenera SKU)
- `searchProducts()`: Búsqueda por nombre, descripción o código
- `getActiveProducts()`: Solo productos activos

### **Endpoints de Productos**

| Método | Endpoint                           | Descripción                 | Auth     |
| ------ | ---------------------------------- | --------------------------- | -------- |
| GET    | `/productos`                       | Listar todos los productos  | ❌       |
| GET    | `/productos/{id}`                  | Obtener producto por ID     | ❌       |
| GET    | `/productos/categoria/{id}`        | Productos de una categoría  | ❌       |
| GET    | `/productos/buscar?consulta=texto` | Buscar productos            | ❌       |
| GET    | `/productos/activos`               | Solo productos activos      | ❌       |
| POST   | `/productos`                       | Crear producto (genera SKU) | ✅ ADMIN |
| PUT    | `/productos/{id}`                  | Actualizar producto         | ✅ ADMIN |
| DELETE | `/productos/{id}`                  | Eliminar producto           | ✅ ADMIN |

### **Endpoints de Categorías**

| Método | Endpoint              | Descripción                   | Auth     |
| ------ | --------------------- | ----------------------------- | -------- |
| GET    | `/categorias`         | Listar todas las categorías   | ❌       |
| GET    | `/categorias/{id}`    | Obtener categoría por ID      | ❌       |
| GET    | `/categorias/activas` | Solo categorías activas       | ❌       |
| POST   | `/categorias`         | Crear categoría (genera code) | ✅ ADMIN |
| PUT    | `/categorias/{id}`    | Actualizar categoría          | ✅ ADMIN |
| DELETE | `/categorias/{id}`    | Eliminar categoría            | ✅ ADMIN |

---

## Módulo de Usuarios (user)

### **Entidades**

#### **User.java**

- Relación `@ManyToMany` con `Role`
- Tabla intermedia: `user_roles`
- **Problema resuelto**: Lazy loading de roles con `@Transactional` y `JOIN FETCH`

#### **Role.java**

- `@JsonIgnore` en relación inversa para evitar ciclos JSON
- `@EqualsAndHashCode(exclude = "users")` para evitar StackOverflow

### **Relación User-Role**

```sql
users (id, username, email, password, ...)
roles (id, name, description)
user_roles (user_id, role_id)  -- Tabla intermedia
```

**Fix aplicado para roles vacíos:**

1. `UserRepository.findByUsernameWithRoles()` con `@Query` + `JOIN FETCH`
2. `@Transactional(readOnly = true)` en `AuthService.login()`
3. `user.getRoles().size()` para forzar carga dentro de transacción

---

## 💳 Módulo de Pagos (payment)

### **Payment.java**

```json
{
  "id": 1,
  "userId": 1,
  "amount": 59990,
  "paymentMethod": "CREDIT_CARD",
  "cardType": "VISA",
  "status": "COMPLETED",
  "products": [{ "productId": 1, "quantity": 2 }],
  "transactionId": "TXN123456",
  "createdAt": "2025-11-13T18:00:00"
}
```

### **Token de Pago**

Flujo de pago seguro con token temporal:

1. Usuario autenticado solicita token de pago
2. Backend genera `PAYMENT_TOKEN` con duración corta
3. Frontend usa este token para procesar pago
4. Backend valida token + usuario + productos
5. Procesa pago y actualiza stock

---

## Módulo de Configuración (config)

### **CorsConfig.java**

Permite requests desde frontend React:

```java
setAllowedOrigins("http://localhost:5173", "http://localhost:3000")
setAllowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
setAllowedHeaders("*")
setAllowCredentials(false)  // Importante para evitar 403
```

### **DataInitializer.java**

Ejecuta al iniciar la aplicación:

```java
@Component
public class DataInitializer implements CommandLineRunner {
    // Crea roles: ROLE_ADMIN, ROLE_USER, ROLE_MODERATOR
    // Crea usuario admin: username=admin, password=admin123
    // Crea usuario regular: username=user, password=user123
}
```

### **GlobalExceptionHandler.java**

Manejo centralizado de errores con `@RestControllerAdvice`:

- `MethodArgumentNotValidException`: Errores de validación (400)
- `BadCredentialsException`: Credenciales incorrectas (401)
- `AccessDeniedException`: Acceso denegado (403)
- `RuntimeException`: Errores de negocio (400)
- `Exception`: Errores internos (500)

**Response estándar:**

```json
{
  "timestamp": "2025-11-13T18:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "details": "..."
}
```

---

## 📦 DTOs (Data Transfer Objects)

### **¿Por qué DTOs?**

1. **Seguridad**: No exponer campos sensibles (password)
2. **Flexibilidad**: Estructura de respuesta diferente al modelo
3. **Validación**: Anotaciones `@NotBlank`, `@NotNull`, `@Email`
4. **Desacoplamiento**: Cambios en DB no afectan API

### **DTOs Principales**

#### **LoginResponse.java**

```json
{
  "token": "eyJ...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@ecommerce.com",
  "roles": ["ROLE_ADMIN"]
}
```

#### **ProductDTO.java**

```json
{
  "id": 1,
  "code": "JM001",
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "category": "Juegos de Mesa",
  "image": "https://...",
  "description": "...",
  "stock": 10,
  "featured": false
}
```

#### **CategoryDTO.java**

```json
{
  "id": 1,
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "...",
  "active": true
}
```

---

## Base de Datos

### **Configuración (application.properties)**

```properties
# MySQL (Laragon)
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=miSuperSecretoSeguroQueDebeSerMuyLargoYComplejo123456789
jwt.expiration=86400000  # 24 horas
```

### **Tablas Generadas**

```sql
users
roles
user_roles (ManyToMany)
categories
products
payments
```

---

## Instalación y Ejecución

### **Prerrequisitos**

- Java 17
- Maven 3.9.11
- MySQL (Laragon)
- Postman (para testing)

### **Pasos**

1. **Clonar repositorio**

```bash
cd C:\Users\FullHackerMIX\OneDrive\Desktop\Projects\ecommerce-backend
```

2. **Crear base de datos**

```sql
CREATE DATABASE ecommerce_db;
```

3. **Compilar proyecto**

```bash
mvn clean install -DskipTests
```

4. **Ejecutar aplicación**

```bash
mvn spring-boot:run
```

5. **Verificar**

```
http://localhost:8080
```

---

## 🧪 Testing con Postman

### **1. Login como Admin**

```
POST http://localhost:8080/autenticacion/login
Body:
{
  "username": "admin",
  "password": "admin123"
}

Response → Copiar token
```

### **2. Crear Categoría**

```
POST http://localhost:8080/categorias
Headers:
  Authorization: Bearer <token>
Body:
{
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "Juegos de estrategia"
}
```

### **3. Crear Producto**

```
POST http://localhost:8080/productos
Headers:
  Authorization: Bearer <token>
Body:
{
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "image": "https://i.imgur.com/Azw0XR8.jpeg",
  "description": "Juego de estrategia",
  "stock": 10
}

Response → Code generado automáticamente: "JM001"
```

### **4. Listar Productos (Sin token)**

```
GET http://localhost:8080/productos
```

---

## 🔧 Endpoints de Debug (Temporal)

| Endpoint                  | Descripción                        |
| ------------------------- | ---------------------------------- |
| POST `/debug/reset-admin` | Resetea password del admin         |
| GET `/debug/admin-info`   | Info del usuario admin             |
| GET `/debug/all-users`    | Lista todos los usuarios con roles |

**⚠️ Eliminar en producción**

---

## 🐛 Problemas Resueltos

### **1. Roles vacíos en LoginResponse**

**Problema**: `"roles": []` aunque existen en DB

**Causa**: Lazy loading + transacción cerrada

**Solución**:

- `@Query` con `JOIN FETCH` en `UserRepository`
- `@Transactional(readOnly = true)` en `AuthService.login()`
- `user.getRoles().size()` para forzar carga

### **2. StackOverflowError en User-Role**

**Problema**: Ciclo infinito al serializar JSON

**Causa**: Relación bidireccional sin anotaciones

**Solución**:

- `@JsonManagedReference` en User.roles
- `@JsonBackReference` + `@JsonIgnore` en Role.users
- `@EqualsAndHashCode(exclude = "roles")` en ambos

### **3. CORS 403 Forbidden**

**Problema**: Frontend no puede hacer requests

**Causa**: `setAllowCredentials(true)` con orígenes específicos

**Solución**:

- `setAllowCredentials(false)` en `CorsConfig`
- Remover `@CrossOrigin` de controllers

### **4. Maven no reconocido en Windows**

**Problema**: PATH con locales español

**Solución**:

```
C:\Archivos de programa\Apache\maven\apache-maven-3.9.11\bin
```

---

## 📝 Convenciones del Proyecto

### **Nomenclatura**

- **Controllers**: `XxxController.java`
- **Services**: `XxxService.java`
- **Repositories**: `XxxRepository.java`
- **DTOs**: `XxxDTO.java`, `XxxRequest.java`, `XxxResponse.java`
- **Models/Entities**: `Xxx.java` (sin sufijo)

### **Estructura de Paquetes**

```
feature/
  ├── controller/    # REST endpoints
  ├── service/       # Lógica de negocio
  ├── repository/    # Acceso a datos
  ├── model/         # Entidades JPA
  └── dto/           # Data Transfer Objects
```

### **Anotaciones Comunes**

- `@RestController` + `@RequestMapping`: Controllers
- `@Service`: Servicios
- `@Repository`: Repositorios
- `@Entity` + `@Table`: Entidades
- `@Data` + `@NoArgsConstructor`: Lombok
- `@PreAuthorize`: Seguridad a nivel de método

---

## 🎯 Próximos Pasos

- [ ] Implementar paginación en listados
- [ ] Agregar filtros avanzados (precio, stock)
- [ ] Sistema de imágenes (upload a servidor/S3)
- [ ] Carrito de compras persistente
- [ ] Historial de pedidos por usuario
- [ ] Integración con pasarelas de pago reales
- [ ] Notificaciones por email
- [ ] Dashboard de administración
- [ ] Tests unitarios y de integración
- [ ] Documentación con Swagger/OpenAPI

---

## 👥 Credenciales de Prueba

| Usuario | Contraseña | Rol        |
| ------- | ---------- | ---------- |
| admin   | admin123   | ROLE_ADMIN |
| user    | user123    | ROLE_USER  |

---

## 📚 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security + JWT](https://spring.io/guides/topicals/spring-security-architecture)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Lombok](https://projectlombok.org/)

---

## 📄 Licencia

Este proyecto es educativo y no tiene licencia comercial.

---

## ✨ Autor

Desarrollado como proyecto de aprendizaje de Spring Boot + React.

**Fecha**: Noviembre 2025
